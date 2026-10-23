package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.entity.ReferenceListItem;

import java.time.LocalDate;
import java.util.List;

public final class ReferenceListMatcher {

    private ReferenceListMatcher() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(" ", "").trim();
    }

    public static boolean matches(ReferenceList list, String candidate, LocalDate date) {
        if (list == null) {
            return false;
        }
        String normalized = normalize(candidate);
        if (normalized.isEmpty()) {
            return false;
        }
        List<ReferenceListItem> items = list.getItems();
        if (items == null || items.isEmpty()) {
            return false;
        }
        boolean included = false;
        for (ReferenceListItem item : items) {
            if (item == null || !appliesOn(item, date)) {
                continue;
            }
            if (!itemMatches(item, normalized)) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getExcluded())) {
                return false;
            }
            included = true;
        }
        return included;
    }

    static boolean appliesOn(ReferenceListItem item, LocalDate date) {
        if (date == null) {
            return item.getValidFrom() == null && item.getValidTo() == null;
        }
        if (item.getValidFrom() != null && date.isBefore(item.getValidFrom())) {
            return false;
        }
        return item.getValidTo() == null || !date.isAfter(item.getValidTo());
    }

    private static boolean itemMatches(ReferenceListItem item, String normalizedCandidate) {
        String value = normalize(item.getValue());
        if (value.isEmpty()) {
            return false;
        }
        if (Boolean.TRUE.equals(item.getPrefixMatch())) {
            return normalizedCandidate.startsWith(value);
        }
        return normalizedCandidate.equals(value);
    }
}
