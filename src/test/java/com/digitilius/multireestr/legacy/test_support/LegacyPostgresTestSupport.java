package com.digitilius.multireestr.legacy.test_support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
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
            statement.execute("""
                    CREATE TABLE notices (
                        id integer PRIMARY KEY,
                        purchasenumber varchar,
                        docnumber varchar,
                        internalnumber varchar,
                        docpublishdate timestamp,
                        predmet text,
                        isdeleted integer,
                        iscanceled integer,
                        modifversion integer,
                        srcguid varchar
                    )
                    """);
            statement.execute("""
                    INSERT INTO notices (
                        id, purchasenumber, docnumber, internalnumber, docpublishdate,
                        predmet, isdeleted, iscanceled, modifversion, srcguid
                    ) VALUES
                    (3, '0173100017026000015', NULL, NULL, '2026-02-01 10:00:00',
                     'Поставка оборудования', 0, 0, 1, '084C00000000000000000000000000000000'),
                    (42, '0173100017026000015', NULL, 'РЗ-2026.30.000044', '2026-10-07 10:00:00',
                     'Копия 16 извещения', 0, 0, 0, '{084C0000-0000-0000-0000-000000000000}'),
                    (15, '0173100017026000003', NULL, NULL, '2026-01-21 12:00:00',
                     'Услуги криминалиста', 0, 0, 1, 'abc'),
                    (99, '0173100017026000009', NULL, NULL, '2026-03-01 12:00:00',
                     'Другое', 0, 0, 1, 'abc'),
                    (33, '0173100017025000006', NULL, NULL, '2025-02-01 10:00:00',
                     'Удалённое извещение', 1, 0, 1, 'abc'),
                    (41, '0173100017025000006', NULL, NULL, '2025-02-02 10:00:00',
                     'Актуальное извещение', 0, 0, 1, 'abc'),
                    (21, '0173100017025000015', NULL, NULL, '2025-03-01 10:00:00',
                     'Редакция 2025-0015', 0, 0, 2, 'abc'),
                    (10, '0173100017026000008', NULL, NULL, '2026-03-01 10:00:00',
                     'Редакция 2026-0008', 0, 0, 2, 'abc'),
                    (40, '0173100017024000008', NULL, NULL, '2024-03-01 10:00:00',
                     'Извещение 2024-0008', 0, 0, 1, 'abc'),
                    (38, '0173100017024000010', NULL, NULL, '2024-04-01 10:00:00',
                     'Извещение 2024-0010', 0, 0, 1, 'abc'),
                    (37, '0173100017025000001', NULL, NULL, '2025-01-10 10:00:00',
                     'Извещение 2025-0001', 0, 0, 1, 'abc'),
                    (18, '0173100017025000020', NULL, NULL, '2025-05-01 10:00:00',
                     'Извещение 2025-0020', 0, 0, 1, 'abc'),
                    (17, '0173100017026000001', NULL, NULL, '2026-01-10 10:00:00',
                     'Извещение 2026-0001', 0, 0, 1, 'abc'),
                    (4, '0173100017026000014', NULL, NULL, '2026-04-01 10:00:00',
                     'Извещение 2026-0014', 0, 0, 1, 'abc')
                    """);
            statement.execute("GRANT SELECT ON ALL TABLES IN SCHEMA public TO " + RO_USER);
            statement.execute("GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO " + RO_USER);
            statement.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO " + RO_USER);
            statement.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON SEQUENCES TO " + RO_USER);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to prepare reestr_ro on Testcontainers PostgreSQL", e);
        }
    }
}
