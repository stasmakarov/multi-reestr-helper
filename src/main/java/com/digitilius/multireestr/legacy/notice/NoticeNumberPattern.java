package com.digitilius.multireestr.legacy.notice;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Разбор поля «номер» на экране поиска: фрагмент или маска с {@code *}.
 * Канон из 19 цифр ({@link NoticeNumber}) не подменяет.
 */
public final class NoticeNumberPattern {

    public enum Kind {
        EXACT,
        FRAGMENT,
        MASK
    }

    private static final Pattern STRIP = Pattern.compile("[\\s\\u00A0]+");
    private static final Pattern PREFIX = Pattern.compile("(?i)^(?:№|no\\.?|n)\\s*");
    private static final Pattern NOISE = Pattern.compile("[\\s\\u00A0\\u2010-\\u2015\\-.]+");
    private static final Pattern DIGITS_19 = Pattern.compile("\\d{19}");

    private final Kind kind;
    private final String value;

    private NoticeNumberPattern(Kind kind, String value) {
        this.kind = kind;
        this.value = value;
    }

    public Kind kind() {
        return kind;
    }

    public String value() {
        return value;
    }

    public Optional<NoticeNumber> asExactNumber() {
        if (kind != Kind.EXACT) {
            return Optional.empty();
        }
        return NoticeNumber.tryParse(value);
    }

    /**
     * Шаблон для {@code ILIKE … ESCAPE '\'}, или {@code null} если сравнение точное.
     */
    public String ilikePattern() {
        return switch (kind) {
            case EXACT -> null;
            case FRAGMENT -> "%" + escapeIlike(value) + "%";
            case MASK -> toMaskIlike(value);
        };
    }

    public static Optional<NoticeNumberPattern> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String stripped = STRIP.matcher(raw.trim()).replaceAll("");
        String withoutPrefix = PREFIX.matcher(stripped).replaceFirst("");
        String cleaned = NOISE.matcher(withoutPrefix).replaceAll("");
        if (cleaned.isEmpty() || "*".equals(cleaned) || cleaned.chars().allMatch(ch -> ch == '*')) {
            return Optional.empty();
        }
        if (cleaned.indexOf('*') >= 0) {
            return Optional.of(new NoticeNumberPattern(Kind.MASK, cleaned));
        }
        if (DIGITS_19.matcher(cleaned).matches()) {
            return Optional.of(new NoticeNumberPattern(Kind.EXACT, cleaned));
        }
        return Optional.of(new NoticeNumberPattern(Kind.FRAGMENT, cleaned));
    }

    static String escapeIlike(String fragment) {
        return fragment
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static String toMaskIlike(String cleanedWithStars) {
        StringBuilder ilike = new StringBuilder();
        for (int i = 0; i < cleanedWithStars.length(); i++) {
            char ch = cleanedWithStars.charAt(i);
            if (ch == '*') {
                ilike.append('%');
            } else {
                ilike.append(escapeIlike(String.valueOf(ch)));
            }
        }
        return ilike.toString();
    }
}
