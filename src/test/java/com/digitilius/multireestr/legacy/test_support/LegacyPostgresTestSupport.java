package com.digitilius.multireestr.legacy.test_support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public final class LegacyPostgresTestSupport {

    public static final String RO_USER = "reestr_ro";
    public static final String RO_PASSWORD = "ro_pass";

    @SuppressWarnings("resource")
    public static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:9.6").asCompatibleSubstituteFor("postgres")
    );

    static {
        if (dockerAvailable()) {
            POSTGRES.start();
            prepareReadOnlySchema();
        } else if (!allowNoDocker()) {
            throw new ExceptionInInitializerError(new IllegalStateException(
                    "Docker is required for legacy tests. Re-run with ./gradlew test -PallowNoDocker to skip them."
            ));
        }
    }

    private LegacyPostgresTestSupport() {
    }

    public static boolean dockerAvailable() {
        return DockerAvailability.dockerAvailable();
    }

    public static boolean allowNoDocker() {
        return DockerAvailability.allowNoDocker();
    }

    public static void registerLegacyProperties(DynamicPropertyRegistry registry) {
        registry.add("legacy.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("legacy.datasource.username", () -> RO_USER);
        registry.add("legacy.datasource.password", () -> RO_PASSWORD);
    }

    private static void prepareReadOnlySchema() {
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
