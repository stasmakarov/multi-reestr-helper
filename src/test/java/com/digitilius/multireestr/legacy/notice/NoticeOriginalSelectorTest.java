package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeOriginalSelectorTest {

    private final NoticeOriginalSelector selector = new NoticeOriginalSelector();

    @Test
    void emptyListReturnsEmpty() {
        assertThat(selector.chooseOriginal(List.of())).isEmpty();
    }

    @Test
    void prefersNotDeleted() {
        NoticeCandidateDto deleted = candidate(1, false, false, true, 5, LocalDateTime.of(2026, 2, 2, 0, 0));
        NoticeCandidateDto original = candidate(2, false, false, false, 1, LocalDateTime.of(2026, 1, 1, 0, 0));

        Optional<NoticeCandidateDto> chosen = selector.chooseOriginal(List.of(deleted, original));

        assertThat(chosen).contains(original);
        assertThat(original.getChosen()).isTrue();
        assertThat(original.getReason()).isEqualTo("Оригинал");
        assertThat(deleted.getChosen()).isFalse();
        assertThat(deleted.getReason()).isEqualTo("Удалено");
    }

    @Test
    void prefersNotLocalCopy() {
        NoticeCandidateDto copy = candidate(1, true, false, false, 5, LocalDateTime.of(2026, 2, 2, 0, 0));
        NoticeCandidateDto original = candidate(2, false, false, false, 1, LocalDateTime.of(2026, 1, 1, 0, 0));

        selector.chooseOriginal(List.of(copy, original));

        assertThat(original.getChosen()).isTrue();
        assertThat(original.getReason()).isEqualTo("Оригинал");
        assertThat(copy.getReason()).isEqualTo("Локальная копия");
    }

    @Test
    void prefersNotCanceled() {
        NoticeCandidateDto canceled = candidate(1, false, true, false, 5, LocalDateTime.of(2026, 2, 2, 0, 0));
        NoticeCandidateDto original = candidate(2, false, false, false, 1, LocalDateTime.of(2026, 1, 1, 0, 0));

        selector.chooseOriginal(List.of(canceled, original));

        assertThat(original.getChosen()).isTrue();
        assertThat(canceled.getReason()).isEqualTo("Отменено");
    }

    @Test
    void prefersGreaterModifVersionAndNullsLast() {
        NoticeCandidateDto older = candidate(1, false, false, false, 1, LocalDateTime.of(2026, 2, 2, 0, 0));
        NoticeCandidateDto newer = candidate(2, false, false, false, 3, LocalDateTime.of(2026, 1, 1, 0, 0));
        NoticeCandidateDto missing = candidate(3, false, false, false, null, LocalDateTime.of(2026, 3, 3, 0, 0));

        List<NoticeCandidateDto> marked = selector.markAndSort(List.of(missing, older, newer));

        assertThat(marked).extracting(NoticeCandidateDto::getId).containsExactly(2, 1, 3);
        assertThat(newer.getReason()).isEqualTo("Оригинал");
        assertThat(older.getReason()).isEqualTo("Более старая редакция");
        assertThat(missing.getReason()).isEqualTo("Более старая редакция");
    }

    @Test
    void prefersLaterPublishedAtAndNullsLast() {
        NoticeCandidateDto earlier = candidate(1, false, false, false, 1, LocalDateTime.of(2026, 1, 1, 0, 0));
        NoticeCandidateDto later = candidate(2, false, false, false, 1, LocalDateTime.of(2026, 2, 1, 0, 0));
        NoticeCandidateDto missing = candidate(3, false, false, false, 1, null);

        List<NoticeCandidateDto> marked = selector.markAndSort(List.of(missing, earlier, later));

        assertThat(marked).extracting(NoticeCandidateDto::getId).containsExactly(2, 1, 3);
        assertThat(later.getReason()).isEqualTo("Оригинал");
        assertThat(earlier.getReason()).isEqualTo("Опубликовано раньше");
        assertThat(missing.getReason()).isEqualTo("Дубль (больший id)");
    }

    @Test
    void prefersSmallerIdWhenOtherwiseEqual() {
        NoticeCandidateDto first = candidate(10, false, false, false, 1, LocalDateTime.of(2026, 1, 1, 0, 0));
        NoticeCandidateDto duplicate = candidate(20, false, false, false, 1, LocalDateTime.of(2026, 1, 1, 0, 0));

        List<NoticeCandidateDto> marked = selector.markAndSort(List.of(duplicate, first));

        assertThat(marked).extracting(NoticeCandidateDto::getId).containsExactly(10, 20);
        assertThat(first.getReason()).isEqualTo("Оригинал");
        assertThat(duplicate.getReason()).isEqualTo("Дубль (больший id)");
    }

    @Test
    void groupKeyUsesIdWhenNumberIsNull() {
        NoticeCandidateDto dto = candidate(7, false, false, false, 1, LocalDateTime.of(2026, 1, 1, 0, 0));
        dto.setNumber(null);
        assertThat(NoticeLookupService.groupKey(dto)).isEqualTo("#7");
    }

    @Test
    void markByNumberGroupsDoesNotFailWhenNumberIsNull() {
        NoticeCandidateDto missingNumber = candidate(1, false, false, false, 1, LocalDateTime.of(2026, 1, 1, 0, 0));
        missingNumber.setNumber(null);
        NoticeCandidateDto otherMissing = candidate(2, false, false, false, 1, LocalDateTime.of(2026, 1, 1, 0, 0));
        otherMissing.setNumber(null);
        NoticeCandidateDto named = candidate(3, false, false, false, 1, LocalDateTime.of(2026, 1, 1, 0, 0));

        List<NoticeCandidateDto> marked = NoticeLookupService.markByNumberGroups(
                selector,
                List.of(missingNumber, otherMissing, named)
        );

        assertThat(marked).hasSize(3);
        assertThat(missingNumber.getChosen()).isTrue();
        assertThat(otherMissing.getChosen()).isTrue();
        assertThat(named.getChosen()).isTrue();
    }

    private static NoticeCandidateDto candidate(
            int id,
            boolean localCopy,
            boolean canceled,
            boolean deleted,
            Integer modifVersion,
            LocalDateTime publishedAt
    ) {
        NoticeCandidateDto dto = new NoticeCandidateDto();
        dto.setId(id);
        dto.setNumber("0173100017026000015");
        dto.setLocalCopy(localCopy);
        dto.setCanceled(canceled);
        dto.setDeleted(deleted);
        dto.setModifVersion(modifVersion);
        dto.setPublishedAt(publishedAt);
        return dto;
    }
}
