package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.legacy.notice.NoticeLookupService.SearchCriteria;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import org.junit.jupiter.api.Test;
import com.digitilius.multireestr.legacy.test_support.DockerRequiredCondition.RequireDocker;
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
@RequireDocker
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
    void findOriginalsByDateRange() {
        var page = noticeLookupService.findOriginals(
                SearchCriteria.of(null, LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 7), null),
                0,
                20
        );
        assertThat(page.count().truncated()).isFalse();
        assertThat(page.originals()).extracting(NoticeCandidateDto::getId).containsExactly(3, 4);
        assertThat(page.originals()).allMatch(c -> Boolean.TRUE.equals(c.getChosen()));
    }

    @Test
    void findOriginalsBySubject() {
        var page = noticeLookupService.findOriginals(
                SearchCriteria.of(null, null, null, "папки"),
                0,
                20
        );
        assertThat(page.originals()).extracting(NoticeCandidateDto::getNumber)
                .containsOnly("0173100017026000015");
        assertThat(page.originals().get(0).getId()).isEqualTo(3);
    }

    @Test
    void findOriginalsByNumberFragment() {
        var page = noticeLookupService.findOriginals(
                SearchCriteria.of("6000015", null, null, null),
                0,
                20
        );
        assertThat(page.originals()).extracting(NoticeCandidateDto::getId).containsExactly(3);
        assertThat(page.originals()).allMatch(c -> Boolean.TRUE.equals(c.getChosen()));
    }

    @Test
    void findOriginalsByNumberMask() {
        var page = noticeLookupService.findOriginals(
                SearchCriteria.of("0173*000015", null, null, null),
                0,
                20
        );
        assertThat(page.originals()).extracting(NoticeCandidateDto::getNumber)
                .containsExactlyInAnyOrder("0173100017026000015", "0173100017025000015");
    }

    @Test
    void percentInNumberDoesNotMatchEverything() {
        var page = noticeLookupService.findOriginals(
                SearchCriteria.of("%", null, null, null),
                0,
                20
        );
        assertThat(page.originals()).isEmpty();
        assertThat(page.count().raw()).isZero();
    }

    @Test
    void emptyCriteriaReturnsFixtureOriginals() {
        var page = noticeLookupService.findOriginals(SearchCriteria.empty(), 0, 20);
        assertThat(page.count().truncated()).isFalse();
        assertThat(page.count().raw()).isGreaterThanOrEqualTo(10);
        assertThat(page.originals()).hasSize(Math.min(20, page.count().raw()));
        assertThat(page.originals()).extracting(NoticeCandidateDto::getNumber)
                .contains(
                        "0173100017026000015",
                        "0173100017025000006",
                        "0173100017026000014"
                );
        assertThat(page.originals()).allMatch(c -> Boolean.TRUE.equals(c.getChosen()));
        assertThat(page.originals()).extracting(NoticeCandidateDto::getId).doesNotContain(42, 33);
    }

    @Test
    void searchSkipsDraftsWithoutNumber() throws Exception {
        LocalDate day = LocalDate.of(2099, 1, 1);
        try (Connection admin = adminConnection()) {
            insertNumbers(admin, 900_000, 200, day, "0173100017099", "Проверка лимита");
            try (PreparedStatement draft = admin.prepareStatement(
                    "INSERT INTO notices (id, purchasenumber, docpublishdate, predmet, isdeleted, iscanceled, modifversion) "
                            + "VALUES (900999, NULL, ?, 'Черновик', 0, 0, 1)")) {
                draft.setTimestamp(1, java.sql.Timestamp.valueOf(day.atTime(23, 0)));
                draft.executeUpdate();
            }
            try {
                var page = noticeLookupService.findOriginals(
                        SearchCriteria.of(null, day, day, null),
                        0,
                        200
                );
                assertThat(page.count().truncated()).isFalse();
                assertThat(page.count().raw()).isEqualTo(200);
                assertThat(page.originals()).hasSize(200);
                assertThat(page.originals()).extracting(NoticeCandidateDto::getNumber).doesNotContainNull();
                assertThat(page.originals()).extracting(NoticeCandidateDto::getId).doesNotContain(900_999);
            } finally {
                try (Statement statement = admin.createStatement()) {
                    statement.execute("DELETE FROM notices WHERE id BETWEEN 900000 AND 900999");
                }
            }
        }
    }

    @Test
    void searchTruncatesAtFiveHundredNumbers() throws Exception {
        LocalDate day = LocalDate.of(2098, 1, 1);
        try (Connection admin = adminConnection()) {
            insertNumbers(admin, 910_000, 501, day, "0173100017098", "Проверка усечения");
            try {
                var page = noticeLookupService.findOriginals(
                        SearchCriteria.of(null, day, day, null),
                        0,
                        20
                );
                assertThat(page.count().truncated()).isTrue();
                assertThat(page.count().capped()).isEqualTo(500);
                assertThat(page.originals()).hasSize(20);

                var lastPage = noticeLookupService.findOriginals(
                        SearchCriteria.of(null, day, day, null),
                        480,
                        20
                );
                assertThat(lastPage.originals()).hasSize(20);

                var beyondCap = noticeLookupService.findOriginals(
                        SearchCriteria.of(null, day, day, null),
                        500,
                        20
                );
                assertThat(beyondCap.originals()).isEmpty();
            } finally {
                try (Statement statement = admin.createStatement()) {
                    statement.execute("DELETE FROM notices WHERE id BETWEEN 910000 AND 910999");
                }
            }
        }
    }

    @Test
    void searchCriteriaRejectsInvertedDates() {
        assertThatThrownBy(() -> SearchCriteria.of(
                null,
                LocalDate.of(2026, 4, 7),
                LocalDate.of(2026, 4, 3),
                null
        )).isInstanceOf(IllegalArgumentException.class);
    }

    private static void insertNumbers(
            Connection admin,
            int idFrom,
            int count,
            LocalDate day,
            String prefix11,
            String subject
    ) throws Exception {
        try (PreparedStatement insert = admin.prepareStatement(
                "INSERT INTO notices (id, purchasenumber, docpublishdate, predmet, isdeleted, iscanceled, modifversion) "
                        + "VALUES (?, ?, ?, ?, 0, 0, 1)")) {
            for (int i = 0; i < count; i++) {
                insert.setInt(1, idFrom + i);
                insert.setString(2, prefix11 + String.format("%06d", i));
                insert.setTimestamp(3, java.sql.Timestamp.valueOf(day.atTime(10, 0)));
                insert.setString(4, subject);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static Connection adminConnection() throws Exception {
        return DriverManager.getConnection(
                LegacyPostgresTestSupport.POSTGRES.getJdbcUrl(),
                LegacyPostgresTestSupport.POSTGRES.getUsername(),
                LegacyPostgresTestSupport.POSTGRES.getPassword());
    }
}
