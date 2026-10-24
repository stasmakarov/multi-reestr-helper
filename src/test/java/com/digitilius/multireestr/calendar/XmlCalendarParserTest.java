package com.digitilius.multireestr.calendar;

import com.digitilius.multireestr.entity.DayType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XmlCalendarParserTest {

    private final XmlCalendarParser parser = new XmlCalendarParser();

    @Test
    void parsesReal2024Sample() throws Exception {
        List<ParsedCalendarDay> days;
        try (InputStream xml = resource()) {
            days = parser.parse(xml, 2024);
        }
        Map<LocalDate, ParsedCalendarDay> byDate = days.stream()
                .collect(Collectors.toMap(ParsedCalendarDay::date, Function.identity()));

        assertThat(days).hasSize(26);

        assertThat(byDate.get(LocalDate.of(2024, 1, 1)))
                .isEqualTo(new ParsedCalendarDay(LocalDate.of(2024, 1, 1), DayType.HOLIDAY, "Новогодние каникулы"));
        assertThat(byDate.get(LocalDate.of(2024, 3, 7)))
                .isEqualTo(new ParsedCalendarDay(LocalDate.of(2024, 3, 7), DayType.SHORTENED, "Предпраздничный день"));
        assertThat(byDate.get(LocalDate.of(2024, 4, 27)))
                .isEqualTo(new ParsedCalendarDay(LocalDate.of(2024, 4, 27), DayType.WORKING_TRANSFER, "Перенос на 29.04"));
        assertThat(byDate.get(LocalDate.of(2024, 4, 29)))
                .isEqualTo(new ParsedCalendarDay(LocalDate.of(2024, 4, 29), DayType.WEEKEND, "Перенос с 27.04"));
        assertThat(byDate.get(LocalDate.of(2024, 5, 9)))
                .isEqualTo(new ParsedCalendarDay(LocalDate.of(2024, 5, 9), DayType.HOLIDAY, "День Победы"));
        assertThat(byDate.get(LocalDate.of(2024, 12, 28)))
                .isEqualTo(new ParsedCalendarDay(LocalDate.of(2024, 12, 28), DayType.WORKING_TRANSFER, "Перенос на 30.12"));
    }

    @Test
    void unknownTypeFails() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <calendar year="2024" lang="ru">
                    <holidays/>
                    <days>
                        <day d="01.01" t="9"/>
                    </days>
                </calendar>
                """;
        assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), 2024))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("t=9");
    }

    @Test
    void yearMismatchFails() throws Exception {
        try (InputStream xml = resource()) {
            assertThatThrownBy(() -> parser.parse(xml, 2025))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("2024");
        }
    }

    private static InputStream resource() {
        InputStream xml = XmlCalendarParserTest.class.getResourceAsStream("/calendar/xmlcalendar-2024.xml");
        assertThat(xml).isNotNull();
        return xml;
    }
}
