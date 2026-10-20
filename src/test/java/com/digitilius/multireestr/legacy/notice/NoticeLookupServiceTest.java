package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
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
                LocalDate.of(2026, 1, 21),
                null
        );
        assertThat(found).extracting(NoticeCandidateDto::getNumber)
                .containsExactly("0173100017026000003");
        assertThat(found.get(0).getChosen()).isTrue();
    }

    @Test
    void findBySubjectWithoutDate() {
        List<NoticeCandidateDto> found = noticeLookupService.findByDateAndSubject(
                null,
                "криминалист"
        );
        assertThat(found).extracting(NoticeCandidateDto::getNumber)
                .containsExactly("0173100017026000003");
        assertThat(found.get(0).getChosen()).isTrue();
    }

    @Test
    void findByDateAndSubjectRequiresAtLeastOneArgument() {
        assertThatThrownBy(() -> noticeLookupService.findByDateAndSubject(null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> noticeLookupService.findByDateAndSubject(null, "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
