package com.digitilius.multireestr.legacy;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LegacyDbInfoCheckTest {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:9.6").asCompatibleSubstituteFor("postgres")
    );

    static {
        POSTGRES.start();
    }

    @Test
    void checkAgainstPostgres96IsReadOnly() {
        HikariDataSource dataSource = readOnlyDataSource(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        );
        try {
            LegacyDbInfo info = new LegacyDbInfo(new NamedParameterJdbcTemplate(dataSource), dataSource);
            LegacyDbStatus status = info.check();

            assertThat(status.ok()).isTrue();
            assertThat(status.readOnly()).isTrue();
            assertThat(status.serverVersion()).contains("9.6");
            assertThat(status.error()).isNull();
            assertThat(status.host()).isNotBlank();
            assertThat(status.port()).isPositive();
            assertThat(status.database()).isEqualTo(POSTGRES.getDatabaseName());
        } finally {
            dataSource.close();
        }
    }

    @Test
    void checkUnreachableHostReturnsErrorWithoutThrowing() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://127.0.0.1:1/reestrdb");
        dataSource.setUsername("reestr_ro");
        dataSource.setPassword("unused");
        dataSource.setConnectionTimeout(2_000);
        dataSource.setInitializationFailTimeout(-1);
        dataSource.setMaximumPoolSize(1);
        dataSource.setReadOnly(true);
        dataSource.addDataSourceProperty("readOnly", "true");
        dataSource.addDataSourceProperty("readOnlyMode", "always");
        try {
            LegacyDbInfo info = new LegacyDbInfo(new NamedParameterJdbcTemplate(dataSource), dataSource);
            LegacyDbStatus[] holder = new LegacyDbStatus[1];
            assertThatCode(() -> holder[0] = info.check()).doesNotThrowAnyException();
            assertThat(holder[0].ok()).isFalse();
            assertThat(holder[0].error()).isNotBlank();
        } finally {
            dataSource.close();
        }
    }

    private static HikariDataSource readOnlyDataSource(String jdbcUrl, String username, String password) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setReadOnly(true);
        dataSource.addDataSourceProperty("readOnly", "true");
        dataSource.addDataSourceProperty("readOnlyMode", "always");
        dataSource.setMaximumPoolSize(2);
        dataSource.setConnectionTimeout(10_000);
        dataSource.setInitializationFailTimeout(-1);
        return dataSource;
    }
}
