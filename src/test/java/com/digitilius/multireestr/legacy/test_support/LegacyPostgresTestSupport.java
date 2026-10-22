package com.digitilius.multireestr.legacy.test_support;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
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
        }
    }

    private LegacyPostgresTestSupport() {
    }

    public static boolean dockerAvailable() {
        return DockerClientFactory.instance().isDockerAvailable();
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
            loadGoldenFixture(connection);
            statement.execute("GRANT SELECT ON ALL TABLES IN SCHEMA public TO " + RO_USER);
            statement.execute("GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO " + RO_USER);
            statement.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO " + RO_USER);
            statement.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON SEQUENCES TO " + RO_USER);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to prepare reestr_ro on Testcontainers PostgreSQL", e);
        }
    }

    private static void loadGoldenFixture(Connection connection) {
        ClassPathResource fixture = new ClassPathResource("legacy/reestr-golden-fixture.sql");
        if (!fixture.exists()) {
            throw new IllegalStateException(
                    "Нет фикстуры classpath:legacy/reestr-golden-fixture.sql"
            );
        }
        ScriptUtils.executeSqlScript(connection, new EncodedResource(fixture, StandardCharsets.UTF_8));
    }
}
