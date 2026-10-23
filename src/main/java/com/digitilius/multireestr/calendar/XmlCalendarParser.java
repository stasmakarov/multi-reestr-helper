package com.digitilius.multireestr.calendar;

import com.digitilius.multireestr.entity.DayType;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class XmlCalendarParser {

    static final String SHORTENED_MARK = "Предпраздничный день";
    static final String WORKING_TRANSFER_MARK = "Рабочий день (перенос)";
    static final String TRANSFER_FROM_PREFIX = "Перенос с ";
    static final String TRANSFER_TO_PREFIX = "Перенос на ";

    private static final DateTimeFormatter MARK_DAY = DateTimeFormatter.ofPattern("dd.MM");

    public List<ParsedCalendarDay> parse(InputStream xml, int expectedYear) {
        Document document = parseDocument(xml);
        Element root = document.getDocumentElement();
        if (!"calendar".equals(root.getTagName())) {
            throw new IllegalArgumentException("Корневой элемент XML должен быть <calendar>");
        }
        int year = Integer.parseInt(root.getAttribute("year"));
        if (year != expectedYear) {
            throw new IllegalArgumentException("Год в XML " + year + " не совпадает с запрошенным " + expectedYear);
        }

        Map<String, String> holidays = readHolidays(root);
        Element days = (Element) root.getElementsByTagName("days").item(0);
        if (days == null) {
            throw new IllegalArgumentException("В XML нет элемента <days>");
        }
        NodeList dayNodes = days.getElementsByTagName("day");
        Map<String, List<String>> reverseFrom = reverseFromLinks(dayNodes);

        List<ParsedCalendarDay> result = new ArrayList<>();
        for (int i = 0; i < dayNodes.getLength(); i++) {
            Element day = (Element) dayNodes.item(i);
            result.add(toParsedDay(year, day, holidays, reverseFrom));
        }
        return result;
    }

    private static Document parseDocument(InputStream xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setExpandEntityReferences(false);
            return factory.newDocumentBuilder().parse(xml);
        } catch (Exception e) {
            throw new IllegalArgumentException("Не удалось разобрать XML календаря: " + e.getMessage(), e);
        }
    }

    private static Map<String, String> readHolidays(Element root) {
        Map<String, String> holidays = new HashMap<>();
        NodeList holidayNodes = root.getElementsByTagName("holiday");
        for (int i = 0; i < holidayNodes.getLength(); i++) {
            Element holiday = (Element) holidayNodes.item(i);
            holidays.put(holiday.getAttribute("id"), holiday.getAttribute("title"));
        }
        return holidays;
    }

    private static Map<String, List<String>> reverseFromLinks(NodeList dayNodes) {
        Map<String, List<String>> reverse = new HashMap<>();
        for (int i = 0; i < dayNodes.getLength(); i++) {
            Element day = (Element) dayNodes.item(i);
            String from = blankToNull(day.getAttribute("f"));
            if (from != null) {
                reverse.computeIfAbsent(from, key -> new ArrayList<>()).add(day.getAttribute("d"));
            }
        }
        return reverse;
    }

    private static ParsedCalendarDay toParsedDay(
            int year,
            Element day,
            Map<String, String> holidays,
            Map<String, List<String>> reverseFrom
    ) {
        String xmlDate = day.getAttribute("d");
        LocalDate date = LocalDate.parse(year + "." + xmlDate, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        String type = day.getAttribute("t");
        String holidayId = blankToNull(day.getAttribute("h"));
        String from = blankToNull(day.getAttribute("f"));

        return switch (type) {
            case "1" -> new ParsedCalendarDay(
                    date,
                    holidayId != null ? DayType.HOLIDAY : DayType.WEEKEND,
                    markForNamedOrTransfer(holidayId, from, holidays)
            );
            case "2" -> new ParsedCalendarDay(
                    date,
                    DayType.SHORTENED,
                    firstNonBlank(
                            markForNamedOrTransfer(holidayId, from, holidays),
                            SHORTENED_MARK
                    )
            );
            case "3" -> new ParsedCalendarDay(
                    date,
                    DayType.WORKING_TRANSFER,
                    transferToMark(xmlDate, reverseFrom)
            );
            default -> throw new IllegalArgumentException("Неизвестный тип дня xmlcalendar t=" + type + " для " + xmlDate);
        };
    }

    private static String markForNamedOrTransfer(String holidayId, String from, Map<String, String> holidays) {
        if (holidayId != null) {
            String title = holidays.get(holidayId);
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("В XML нет праздника h=" + holidayId);
            }
            return title;
        }
        if (from != null) {
            return TRANSFER_FROM_PREFIX + formatMarkDate(from);
        }
        return "";
    }

    private static String transferToMark(String xmlDate, Map<String, List<String>> reverseFrom) {
        List<String> destinations = reverseFrom.get(xmlDate);
        if (destinations == null || destinations.isEmpty()) {
            return WORKING_TRANSFER_MARK;
        }
        List<String> marks = new ArrayList<>();
        for (String destination : destinations) {
            marks.add(formatMarkDate(destination));
        }
        return TRANSFER_TO_PREFIX + String.join(", ", marks);
    }

    private static String formatMarkDate(String mmDd) {
        return LocalDate.parse("2000." + mmDd, DateTimeFormatter.ofPattern("yyyy.MM.dd")).format(MARK_DAY);
    }

    private static String firstNonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
