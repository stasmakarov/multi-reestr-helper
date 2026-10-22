package com.digitilius.multireestr.legacy.notice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoticeNumberTest {

    private static final String CANONICAL = "0373100000123000001";

    static Stream<String> validInputs() {
        return Stream.of(
                "0373100000123000001",
                " 0373100000123000001 ",
                "№0373100000123000001",
                "№ 0373 1000 0012 3000 001",
                "N 0373100000123000001",
                "0373\u00A0100000123000001",
                "0373-1000-0012-3000-001",
                " № 0373100000123000001",
                "\u00A0№0373100000123000001",
                "0373–1000–0012–3000–001"
        );
    }

    static Stream<String> invalidDigitInputs() {
        return Stream.of(
                "037310000012300000",
                "03731000001230000011",
                "03731000001230000AB"
        );
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    void parseAcceptsCanonicalForms(String raw) {
        assertThat(NoticeNumber.parse(raw).value()).isEqualTo(CANONICAL);
        assertThat(NoticeNumber.parse(raw).toString()).isEqualTo(CANONICAL);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "№"})
    void parseRejectsBlank(String raw) {
        assertThatThrownBy(() -> NoticeNumber.parse(raw))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Номер извещения не задан");
    }

    @ParameterizedTest
    @MethodSource("invalidDigitInputs")
    void parseRejectsWrongDigitCount(String raw) {
        assertThatThrownBy(() -> NoticeNumber.parse(raw))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Номер извещения должен содержать 19 цифр: " + raw);
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    void tryParseReturnsValueForValid(String raw) {
        Optional<NoticeNumber> parsed = NoticeNumber.tryParse(raw);
        assertThat(parsed).isPresent();
        assertThat(parsed.get().value()).isEqualTo(CANONICAL);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "№", "037310000012300000", "03731000001230000011", "03731000001230000AB"})
    void tryParseReturnsEmptyForInvalid(String raw) {
        assertThat(NoticeNumber.tryParse(raw)).isEmpty();
    }

    @Test
    void constructorRejectsInvalidValue() {
        assertThatThrownBy(() -> new NoticeNumber("short"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
