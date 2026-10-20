package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class NoticeLookupGoldenTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    NoticeLookupService noticeLookupService;

    @ParameterizedTest(name = "{0} -> {1} ({3})")
    @CsvFileSource(resources = "/legacy/notice-golden.csv", delimiter = ';', numLinesToSkip = 1)
    void findByNumberChoosesGoldenOriginal(
            String number,
            int expectedOriginalId,
            int candidates,
            String note
    ) {
        List<NoticeCandidateDto> found = noticeLookupService.findByNumber(NoticeNumber.parse(number));

        assertThat(found).as(note).hasSize(candidates);
        assertThat(found.stream().filter(c -> Boolean.TRUE.equals(c.getChosen()))).hasSize(1);

        NoticeCandidateDto original = found.get(0);
        assertThat(original.getChosen()).isTrue();
        assertThat(original.getId()).isEqualTo(expectedOriginalId);
        assertThat(original.getReason()).isEqualTo("Оригинал");

        assertThat(found.subList(1, found.size()))
                .allSatisfy(candidate -> {
                    assertThat(candidate.getChosen()).isFalse();
                    assertThat(candidate.getReason()).isNotBlank();
                    assertThat(candidate.getReason()).isNotEqualTo("Оригинал");
                });
    }
}
