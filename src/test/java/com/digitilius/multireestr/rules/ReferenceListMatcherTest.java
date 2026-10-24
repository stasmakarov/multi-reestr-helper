package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.entity.ReferenceListItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceListMatcherTest {

    private static final LocalDate DAY = LocalDate.of(2025, 6, 1);

    @Test
    void prefixIncludesLongerCode() {
        ReferenceList list = list(item("27.20", true, false));
        assertThat(ReferenceListMatcher.matches(list, "27.20.23.130", DAY)).isTrue();
        assertThat(ReferenceListMatcher.matches(list, "28.23.25.000", DAY)).isFalse();
    }

    @Test
    void excludedWinsOverPrefix() {
        ReferenceList list = list(
                item("26.80", true, false),
                item("26.80.12.000", false, true)
        );
        assertThat(ReferenceListMatcher.matches(list, "26.80.12.000", DAY)).isFalse();
        assertThat(ReferenceListMatcher.matches(list, "26.80.11.000", DAY)).isTrue();
    }

    @Test
    void validityWindow() {
        ReferenceListItem item = item("14.1", true, false);
        item.setValidFrom(LocalDate.of(2024, 1, 1));
        item.setValidTo(LocalDate.of(2024, 12, 31));
        ReferenceList list = list(item);
        assertThat(ReferenceListMatcher.matches(list, "14.12.30.150", LocalDate.of(2024, 6, 1))).isTrue();
        assertThat(ReferenceListMatcher.matches(list, "14.12.30.150", LocalDate.of(2025, 1, 1))).isFalse();
    }

    @Test
    void blankCandidateDoesNotMatch() {
        assertThat(ReferenceListMatcher.matches(list(item("27.20", true, false)), "  ", DAY)).isFalse();
        assertThat(ReferenceListMatcher.matches(list(item("27.20", true, false)), null, DAY)).isFalse();
    }

    private static ReferenceList list(ReferenceListItem... items) {
        ReferenceList list = new ReferenceList();
        list.setItems(List.of(items));
        return list;
    }

    private static ReferenceListItem item(String value, boolean prefix, boolean excluded) {
        ReferenceListItem item = new ReferenceListItem();
        item.setValue(value);
        item.setPrefixMatch(prefix);
        item.setExcluded(excluded);
        return item;
    }
}
