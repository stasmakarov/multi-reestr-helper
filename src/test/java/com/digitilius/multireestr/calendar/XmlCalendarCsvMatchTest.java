package com.digitilius.multireestr.calendar;

import com.digitilius.multireestr.entity.DayType;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XmlCalendarCsvMatchTest {

    @Test
    void csv2024MatchesParserOnSampleXml() throws Exception {
        XmlCalendarParser parser = new XmlCalendarParser();
        List<ParsedCalendarDay> parsed;
        try (InputStream xml = XmlCalendarCsvMatchTest.class.getResourceAsStream("/calendar/xmlcalendar-2024.xml")) {
            assertThat(xml).isNotNull();
            parsed = parser.parse(xml, 2024);
        }
        parsed = parsed.stream().sorted(Comparator.comparing(ParsedCalendarDay::date)).toList();

        List<ParsedCalendarDay> csvRows = readCsvYear(2024);
        assertThat(csvRows).containsExactlyElementsOf(parsed);
    }

    private static List<ParsedCalendarDay> readCsvYear(int year) throws Exception {
        List<ParsedCalendarDay> rows = new ArrayList<>();
        try (InputStream in = XmlCalendarCsvMatchTest.class.getResourceAsStream(
                "/com/digitilius/multireestr/liquibase/data/mtr-calendar-day.csv"
        )) {
            assertThat(in).isNotNull();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String header = reader.readLine();
            assertThat(header).startsWith("ID,DATE_,YEAR_,DAY_TYPE,SOURCE,MARK");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (Integer.parseInt(parts[2]) != year) {
                    continue;
                }
                rows.add(new ParsedCalendarDay(
                        LocalDate.parse(parts[1]),
                        DayType.fromId(parts[3]),
                        parts[5]
                ));
            }
        }
        rows.sort(Comparator.comparing(ParsedCalendarDay::date));
        return rows;
    }
}
