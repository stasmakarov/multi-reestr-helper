package com.digitilius.multireestr.legacy.notice;

import java.util.Optional;
import java.util.regex.Pattern;

public record NoticeNumber(String value) {

    private static final Pattern DIGITS_19 = Pattern.compile("\\d{19}");
    private static final Pattern STRIP = Pattern.compile("[\\s\\u00A0]+");
    private static final Pattern PREFIX = Pattern.compile("(?i)^(?:№|no\\.?|n)\\s*");
    private static final Pattern NOISE = Pattern.compile("[\\s\\u00A0\\u2010-\\u2015\\-.]+");

    public NoticeNumber {
        if (value == null || !DIGITS_19.matcher(value).matches()) {
            throw new IllegalArgumentException("Номер извещения должен содержать 19 цифр: " + value);
        }
    }

    public static NoticeNumber parse(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Номер извещения не задан");
        }
        String stripped = STRIP.matcher(raw).replaceAll("");
        String withoutPrefix = PREFIX.matcher(stripped).replaceFirst("");
        String cleaned = NOISE.matcher(withoutPrefix).replaceAll("");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Номер извещения не задан");
        }
        if (!DIGITS_19.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Номер извещения должен содержать 19 цифр: " + raw);
        }
        return new NoticeNumber(cleaned);
    }

    public static Optional<NoticeNumber> tryParse(String raw) {
        try {
            return Optional.of(parse(raw));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
