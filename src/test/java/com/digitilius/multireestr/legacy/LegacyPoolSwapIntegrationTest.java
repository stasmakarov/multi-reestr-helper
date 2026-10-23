package com.digitilius.multireestr.legacy;

import com.digitilius.multireestr.entity.LegacyConnectionSettings;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import io.jmix.core.UnconstrainedDataManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIf("com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport#dockerAvailable")
class LegacyPoolSwapIntegrationTest {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer SECOND = new PostgreSQLContainer(
            DockerImageName.parse("postgres:9.6").asCompatibleSubstituteFor("postgres")
    );

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
        registry.add("mtr.secret-key", () -> "test-secret-key-for-legacy");
    }

    @Autowired
    LegacyConnectionService legacyConnectionService;

    @Autowired
    LegacyDbInfo legacyDbInfo;

    @Autowired
    @Qualifier("legacyJdbc")
    NamedParameterJdbcTemplate legacyJdbc;

    @Autowired
    UnconstrainedDataManager unconstrainedDataManager;

    @Test
    void swapBetweenTwoContainersKeepsReadOnly() {
        SECOND.start();
        try {
            prepareSecond();
            assertThat(legacyDbInfo.fetchPostgresVersion()).isPresent();

            legacyConnectionService.applyRuntime(
                    SECOND.getHost(),
                    SECOND.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                    SECOND.getDatabaseName(),
                    SECOND.getUsername(),
                    SECOND.getPassword()
            );

            Integer marker = legacyJdbc.queryForObject(
                    "select id from swap_marker",
                    Collections.emptyMap(),
                    Integer.class
            );
            assertThat(marker).isEqualTo(7);
            assertThatThrownBy(() -> legacyJdbc.update(
                    "INSERT INTO swap_marker(id) VALUES (8)",
                    Collections.emptyMap()
            )).hasMessageMatching("(?s).*(read-only|read only|cannot execute).*");
        } finally {
            restoreFirstContainer();
            SECOND.stop();
        }
    }

    @Test
    void saveAndApplyPersistsEncryptedPasswordAndRebuildsPool() {
        SECOND.start();
        try {
            prepareSecond();
            LegacyConnectionSettings settings = unconstrainedDataManager.create(LegacyConnectionSettings.class);
            settings.setCode(LegacyConnectionSettings.DEFAULT_CODE);
            settings.setEnabled(true);
            settings.setHost(SECOND.getHost());
            settings.setPort(SECOND.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
            settings.setDatabaseName(SECOND.getDatabaseName());
            settings.setUsername(SECOND.getUsername());

            LegacyDbStatus status = legacyConnectionService.saveAndApply(settings, SECOND.getPassword());
            assertThat(status.ok()).isTrue();

            LegacyConnectionSettings stored = unconstrainedDataManager.load(LegacyConnectionSettings.class)
                    .query("select e from mtr_LegacyConnectionSettings e where e.code = :code")
                    .parameter("code", LegacyConnectionSettings.DEFAULT_CODE)
                    .one();
            assertThat(stored.getPasswordEncrypted()).isNotBlank().isNotEqualTo(SECOND.getPassword());
            assertThat(Boolean.TRUE.equals(stored.getLastCheckOk())).isTrue();

            Integer marker = legacyJdbc.queryForObject(
                    "select id from swap_marker",
                    Map.of(),
                    Integer.class
            );
            assertThat(marker).isEqualTo(7);

            LegacyDbStatus reset = legacyConnectionService.resetToProperties();
            LegacyConnectionSettings afterReset = unconstrainedDataManager.load(LegacyConnectionSettings.class)
                    .id(stored.getId())
                    .one();
            assertThat(afterReset.getEnabled()).isFalse();
            assertThat(reset.ok()).isTrue();
        } finally {
            unconstrainedDataManager.load(LegacyConnectionSettings.class)
                    .query("select e from mtr_LegacyConnectionSettings e where e.code = :code")
                    .parameter("code", LegacyConnectionSettings.DEFAULT_CODE)
                    .optional()
                    .ifPresent(unconstrainedDataManager::remove);
            restoreFirstContainer();
            SECOND.stop();
        }
    }

    private void restoreFirstContainer() {
        legacyConnectionService.applyRuntime(
                LegacyPostgresTestSupport.POSTGRES.getHost(),
                LegacyPostgresTestSupport.POSTGRES.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                LegacyPostgresTestSupport.POSTGRES.getDatabaseName(),
                LegacyPostgresTestSupport.RO_USER,
                LegacyPostgresTestSupport.RO_PASSWORD
        );
    }

    private static void prepareSecond() {
        try (var connection = java.sql.DriverManager.getConnection(
                SECOND.getJdbcUrl(), SECOND.getUsername(), SECOND.getPassword());
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS swap_marker(id int)");
            statement.execute("DELETE FROM swap_marker");
            statement.execute("INSERT INTO swap_marker(id) VALUES (7)");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
