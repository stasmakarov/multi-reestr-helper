package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.legacy.notice.NoticeCardService;
import com.digitilius.multireestr.legacy.notice.NoticeNumber;
import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
@EnabledIf("com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport#dockerAvailable")
class NoticeOnDemandGoldenTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    NoticeCardService noticeCardService;
    @Autowired
    NoticeRuleEvaluator noticeRuleEvaluator;
    @Autowired
    OnDemandCheckService onDemandCheckService;

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(resources = "/legacy/notice-check-golden.csv", delimiter = ';', numLinesToSkip = 1)
    void evaluateGoldenNoticeRuleCodes(String number, String codes) {
        NoticeDto notice = noticeCardService.loadOriginal(NoticeNumber.parse(number)).orElseThrow();
        List<CheckResultDto> hits = noticeRuleEvaluator.evaluate(notice);
        List<String> expected = parseCodes(codes);
        assertThat(hits.stream().map(CheckResultDto::getRuleCode).distinct().toList())
                .containsExactlyInAnyOrderElementsOf(expected);
        assertThat(hits).allSatisfy(hit -> {
            assertThat(hit.getFingerprint()).hasSize(64);
            assertThat(hit.getMessage()).isNotBlank();
            assertThat(hit.getObjectKey()).isEqualTo(number);
        });

        OnDemandCheckService.OnDemandCheckResult result = onDemandCheckService.check(notice);
        assertThat(result.checkRun().getViolationsFound()).isEqualTo(hits.size());
        assertThat(result.checkRun().getObjectKey()).isEqualTo(number);
    }

    private static List<String> parseCodes(String codes) {
        if (codes == null || codes.isBlank() || "-".equals(codes.trim())) {
            return List.of();
        }
        return Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
