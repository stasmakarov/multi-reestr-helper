package com.digitilius.multireestr.legacy.notice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeNumberPatternTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "№", "*", "***", " * "})
    void parseTreatsBlankAndStarOnlyAsAbsent(String raw) {
        assertThat(NoticeNumberPattern.parse(raw)).isEmpty();
    }

    @Test
    void parseExactNineteenDigits() {
        NoticeNumberPattern pattern = NoticeNumberPattern.parse("№ 0173 1000 1702 6000 015").orElseThrow();
        assertThat(pattern.kind()).isEqualTo(NoticeNumberPattern.Kind.EXACT);
        assertThat(pattern.value()).isEqualTo("0173100017026000015");
        assertThat(pattern.asExactNumber()).contains(NoticeNumber.parse("0173100017026000015"));
        assertThat(pattern.ilikePattern()).isNull();
    }

    @Test
    void parseFragment() {
        NoticeNumberPattern pattern = NoticeNumberPattern.parse("6000015").orElseThrow();
        assertThat(pattern.kind()).isEqualTo(NoticeNumberPattern.Kind.FRAGMENT);
        assertThat(pattern.ilikePattern()).isEqualTo("%6000015%");
        assertThat(pattern.asExactNumber()).isEmpty();
    }

    @Test
    void parseMaskKeepsUserWildcards() {
        NoticeNumberPattern pattern = NoticeNumberPattern.parse("0173*000015").orElseThrow();
        assertThat(pattern.kind()).isEqualTo(NoticeNumberPattern.Kind.MASK);
        assertThat(pattern.ilikePattern()).isEqualTo("0173%000015");
    }

    @ParameterizedTest
    @CsvSource({
            "%all, \\%all",
            "a_b, a\\_b",
            "a\\b, a\\\\b"
    })
    void fragmentEscapesLikeMetacharacters(String raw, String escaped) {
        NoticeNumberPattern pattern = NoticeNumberPattern.parse(raw).orElseThrow();
        assertThat(pattern.kind()).isEqualTo(NoticeNumberPattern.Kind.FRAGMENT);
        assertThat(pattern.ilikePattern()).isEqualTo("%" + escaped + "%");
    }

    @Test
    void maskEscapesPercentAndUnderscoreButConvertsStar() {
        NoticeNumberPattern pattern = NoticeNumberPattern.parse("01%*02_").orElseThrow();
        assertThat(pattern.ilikePattern()).isEqualTo("01\\%%02\\_");
    }
}
