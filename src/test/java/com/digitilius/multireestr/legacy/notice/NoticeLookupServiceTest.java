package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIf("com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport#dockerAvailable")
class NoticeLookupServiceTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    NoticeLookupService noticeLookupService;

    @Test
    void findByNumberMarksSingleOriginal() {
        List<NoticeCandidateDto> found = noticeLookupService.findByNumber(
                NoticeNumber.parse("0173100017026000015")
        );

        assertThat(found).hasSize(2);
        assertThat(found.stream().filter(c -> Boolean.TRUE.equals(c.getChosen()))).hasSize(1);
        NoticeCandidateDto original = found.get(0);
        assertThat(original.getId()).isEqualTo(3);
        assertThat(original.getChosen()).isTrue();
        assertThat(original.getReason()).isEqualTo("Оригинал");
        assertThat(found.get(1).getId()).isEqualTo(42);
        assertThat(found.get(1).getChosen()).isFalse();
        assertThat(found.get(1).getReason()).isEqualTo("Локальная копия");
    }

    @Test
    void findByUnknownNumberReturnsEmpty() {
        List<NoticeCandidateDto> found = noticeLookupService.findByNumber(
                NoticeNumber.parse("0173100017026999999")
        );
        assertThat(found).isEmpty();
    }

    @Test
    void findByDateWithoutSubject() {
        List<NoticeCandidateDto> found = noticeLookupService.findByDateAndSubject(
                LocalDate.of(2026, 4, 3),
                null
        );
        assertThat(found).extracting(NoticeCandidateDto::getNumber)
                .containsExactly("0173100017026000014");
        assertThat(found.get(0).getId()).isEqualTo(4);
        assertThat(found.get(0).getChosen()).isTrue();
    }

    @Test
    void findBySubjectWithoutDate() {
        List<NoticeCandidateDto> found = noticeLookupService.findByDateAndSubject(
                null,
                "папки"
        );
        assertThat(found).extracting(NoticeCandidateDto::getNumber)
                .containsOnly("0173100017026000015");
        assertThat(found.get(0).getId()).isEqualTo(3);
        assertThat(found.get(0).getChosen()).isTrue();
        assertThat(found.stream().filter(c -> Boolean.TRUE.equals(c.getChosen()))).hasSize(1);
    }

    @Test
    void findByDateSkipsDraftsWithoutNumber() throws Exception {
        // 200 опубликованных номеров на одну дату + черновик без номера, опубликованный позже всех.
        // Без условия purchasenumber IS NOT NULL черновик занимал бы одно из 200 мест в CTE keys
        // и один настоящий номер пропадал бы из выдачи.
        LocalDate day = LocalDate.of(2099, 1, 1);
        try (Connection admin = adminConnection()) {
            try (PreparedStatement insert = admin.prepareStatement(
                    "INSERT INTO notices (id, purchasenumber, docpublishdate, predmet, isdeleted, iscanceled, modifversion) "
                            + "VALUES (?, ?, ?, 'Проверка лимита', 0, 0, 1)")) {
                for (int i = 0; i < 200; i++) {
                    insert.setInt(1, 900_000 + i);
                    insert.setString(2, String.format("0173100017099%06d", i));
                    insert.setTimestamp(3, java.sql.Timestamp.valueOf(day.atTime(10, 0)));
                    insert.addBatch();
                }
                insert.setInt(1, 900_999);
                insert.setString(2, null);
                insert.setTimestamp(3, java.sql.Timestamp.valueOf(day.atTime(23, 0)));
                insert.addBatch();
                insert.executeBatch();
            }
            try {
                List<NoticeCandidateDto> found = noticeLookupService.findByDateAndSubject(day, null);

                assertThat(found).hasSize(200);
                assertThat(found).extracting(NoticeCandidateDto::getNumber).doesNotContainNull();
                assertThat(found).extracting(NoticeCandidateDto::getId).doesNotContain(900_999);
            } finally {
                try (Statement statement = admin.createStatement()) {
                    statement.execute("DELETE FROM notices WHERE id BETWEEN 900000 AND 900999");
                }
            }
        }
    }

    private static Connection adminConnection() throws Exception {
        return DriverManager.getConnection(
                LegacyPostgresTestSupport.POSTGRES.getJdbcUrl(),
                LegacyPostgresTestSupport.POSTGRES.getUsername(),
                LegacyPostgresTestSupport.POSTGRES.getPassword());
    }

    @Test
    void findByDateAndSubjectRequiresAtLeastOneArgument() {
        assertThatThrownBy(() -> noticeLookupService.findByDateAndSubject(null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> noticeLookupService.findByDateAndSubject(null, "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
