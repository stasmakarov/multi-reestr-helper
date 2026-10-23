package com.digitilius.multireestr.legacy;

import com.digitilius.multireestr.legacy.test_support.DockerAvailability;
import com.digitilius.multireestr.legacy.test_support.DockerRequiredCondition.RequireDocker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@RequireDocker
class LegacyDataSourceIntegrationTest {

    private static final String RO_USER = "reestr_ro";
    private static final String RO_PASSWORD = "ro_pass";

    @SuppressWarnings("resource")
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:9.6").asCompatibleSubstituteFor("postgres")
    );

    static {
        if (DockerAvailability.dockerAvailable()) {
            POSTGRES.start();
            prepareReadOnlyRole();
        }
    }

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        registry.add("legacy.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("legacy.datasource.username", () -> RO_USER);
        registry.add("legacy.datasource.password", () -> RO_PASSWORD);
    }

    @Autowired
    LegacyDbInfo legacyDbInfo;

    @Autowired
    @Qualifier("legacyJdbc")
    NamedParameterJdbcTemplate legacyJdbc;

    @Test
    void postgresVersionContains96() {
        assertThat(legacyDbInfo.fetchPostgresVersion())
                .isPresent()
                .get()
                .asString()
                .contains("9.6");
    }

    @Test
    void insertThroughLegacyJdbcFailsAsReadOnly() {
        assertThatThrownBy(() -> legacyJdbc.update(
                "INSERT INTO legacy_probe(id) VALUES (2)",
                Collections.emptyMap()
        )).hasMessageMatching("(?s).*(read-only|read only|cannot execute).*");
    }

    @Test
    void createTableThroughLegacyJdbcFailsAsReadOnly() {
        assertThatThrownBy(() -> legacyJdbc.getJdbcTemplate()
                .execute("CREATE TABLE ro_fail(id int)"))
                .hasMessageMatching("(?s).*(read-only|read only|cannot execute|permission denied).*");
    }

    private static void prepareReadOnlyRole() {
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE ROLE " + RO_USER + " LOGIN PASSWORD '" + RO_PASSWORD + "'");
            statement.execute("GRANT CONNECT ON DATABASE " + POSTGRES.getDatabaseName() + " TO " + RO_USER);
            statement.execute("GRANT USAGE ON SCHEMA public TO " + RO_USER);
            statement.execute("CREATE TABLE legacy_probe(id int)");
            statement.execute("INSERT INTO legacy_probe(id) VALUES (1)");
            // Как в dev/legacy/grant-readonly.sql: SELECT только на нужные таблицы, без ON ALL TABLES.
            statement.execute("GRANT SELECT ON legacy_probe TO " + RO_USER);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to prepare reestr_ro on Testcontainers PostgreSQL", e);
        }
    }
}
