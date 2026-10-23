package com.digitilius.multireestr.legacy.planschedule;

import com.digitilius.multireestr.entity.legacy.PlanScheduleDto;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
@EnabledIf("com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport#dockerAvailable")
class PlanScheduleLookupServiceTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    PlanScheduleLookupService planScheduleLookupService;

    @Test
    void missingTableReturnsEmptyListWithoutException() {
        assertThatCode(() -> planScheduleLookupService.listCurrent()).doesNotThrowAnyException();
        assertThat(planScheduleLookupService.listCurrent()).isEmpty();
    }

    @Test
    void mapsVersionToPlanVersionWhenTableExists() throws Exception {
        try (Connection connection = adminConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE planzakuppackets (
                        id integer PRIMARY KEY,
                        finyear integer,
                        oosversion integer,
                        publishdate timestamp without time zone,
                        approvedate timestamp without time zone
                    )
                    """);
            statement.execute("GRANT SELECT ON TABLE planzakuppackets TO reestr_ro");
            statement.execute("""
                    INSERT INTO planzakuppackets (id, finyear, oosversion, publishdate, approvedate)
                    VALUES (11, 2024, 12, '2024-12-01 10:00:00', NULL),
                           (22, 2025, 3, '2025-03-15 09:30:00', NULL)
                    """);
            try {
                List<PlanScheduleDto> rows = planScheduleLookupService.listCurrent();
                assertThat(rows).hasSize(2);
                PlanScheduleDto y2025 = rows.stream()
                        .filter(row -> Integer.valueOf(2025).equals(row.getFinYear()))
                        .findFirst()
                        .orElseThrow();
                assertThat(y2025.getId()).isEqualTo(22);
                assertThat(y2025.getVersion()).isEqualTo(3);
                assertThat(y2025.getPublishDate()).isEqualTo(LocalDateTime.of(2025, 3, 15, 9, 30));
                assertThat(y2025.getApproveDate()).isNull();

                PlanScheduleDto y2024 = rows.stream()
                        .filter(row -> Integer.valueOf(2024).equals(row.getFinYear()))
                        .findFirst()
                        .orElseThrow();
                assertThat(y2024.getVersion()).isEqualTo(12);
                assertThat(y2024.getPublishDate()).isEqualTo(LocalDateTime.of(2024, 12, 1, 10, 0));
            } finally {
                statement.execute("DROP TABLE planzakuppackets");
            }
        }
    }

    private static Connection adminConnection() throws Exception {
        return DriverManager.getConnection(
                LegacyPostgresTestSupport.POSTGRES.getJdbcUrl(),
                LegacyPostgresTestSupport.POSTGRES.getUsername(),
                LegacyPostgresTestSupport.POSTGRES.getPassword()
        );
    }
}
