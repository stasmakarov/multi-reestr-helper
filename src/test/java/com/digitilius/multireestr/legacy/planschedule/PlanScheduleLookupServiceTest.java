package com.digitilius.multireestr.legacy.planschedule;

import com.digitilius.multireestr.entity.legacy.PlanScheduleDto;
import com.digitilius.multireestr.legacy.test_support.DockerRequiredCondition.RequireDocker;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
@RequireDocker
class PlanScheduleLookupServiceTest {

    private static final Object TABLE_LOCK = new Object();

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    PlanScheduleLookupService planScheduleLookupService;

    @Test
    void missingTableReturnsEmptyList() throws Exception {
        synchronized (TABLE_LOCK) {
            try (Connection connection = LegacyPostgresTestSupport.openAdminConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS planzakuppackets");
                assertThat(planScheduleLookupService.listAll()).isEmpty();
            }
        }
    }

    @Test
    void mapsVersionToPlanVersionAndKeepsPublishDate() throws Exception {
        synchronized (TABLE_LOCK) {
            try (Connection connection = LegacyPostgresTestSupport.openAdminConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS planzakuppackets");
                statement.execute("""
                        CREATE TABLE planzakuppackets (
                            id integer PRIMARY KEY,
                            finyear integer,
                            oosversion integer,
                            publishdate timestamp,
                            approvedate timestamp
                        )
                        """);
                statement.execute("""
                        INSERT INTO planzakuppackets (id, finyear, oosversion, publishdate, approvedate)
                        VALUES
                          (101, 2025, 3, timestamp '2025-03-01 12:00:00', NULL),
                          (102, 2024, 1, timestamp '2024-02-15 09:30:00', timestamp '2024-02-20 10:00:00')
                        """);
                statement.execute("GRANT SELECT ON TABLE planzakuppackets TO reestr_ro");
                try {
                    List<PlanScheduleDto> rows = planScheduleLookupService.listAll();
                    assertThat(rows).hasSize(2);
                    assertThat(rows.get(0).getId()).isEqualTo(101);
                    assertThat(rows.get(0).getFinYear()).isEqualTo(2025);
                    assertThat(rows.get(0).getVersion()).isEqualTo(3);
                    assertThat(rows.get(0).getPublishDate()).isEqualTo(LocalDateTime.of(2025, 3, 1, 12, 0));
                    assertThat(rows.get(0).getApproveDate()).isNull();
                    assertThat(rows.get(1).getId()).isEqualTo(102);
                    assertThat(rows.get(1).getVersion()).isEqualTo(1);
                    assertThat(rows.get(1).getFinYear()).isEqualTo(2024);
                    assertThat(rows.get(1).getApproveDate()).isEqualTo(LocalDateTime.of(2024, 2, 20, 10, 0));
                } finally {
                    statement.execute("DROP TABLE IF EXISTS planzakuppackets");
                }
            }
        }
    }
}
