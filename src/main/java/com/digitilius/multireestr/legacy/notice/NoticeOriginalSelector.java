package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class NoticeOriginalSelector {

    static final Comparator<NoticeCandidateDto> COMPARATOR =
            Comparator.comparing((NoticeCandidateDto c) -> Boolean.TRUE.equals(c.getDeleted()))
                    .thenComparing(c -> Boolean.TRUE.equals(c.getLocalCopy()))
                    .thenComparing(c -> Boolean.TRUE.equals(c.getCanceled()))
                    .thenComparing(NoticeCandidateDto::getModifVersion,
                            Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(NoticeCandidateDto::getPublishedAt,
                            Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(NoticeCandidateDto::getId,
                            Comparator.nullsLast(Comparator.naturalOrder()));

    public Optional<NoticeCandidateDto> chooseOriginal(List<NoticeCandidateDto> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        NoticeCandidateDto winner = candidates.stream().min(COMPARATOR).orElseThrow();
        for (NoticeCandidateDto candidate : candidates) {
            if (candidate == winner || Objects.equals(candidate.getId(), winner.getId())) {
                candidate.setChosen(true);
                candidate.setReason("Оригинал");
            } else {
                candidate.setChosen(false);
                candidate.setReason(loseReason(candidate, winner));
            }
        }
        return Optional.of(winner);
    }

    public List<NoticeCandidateDto> markAndSort(List<NoticeCandidateDto> candidates) {
        chooseOriginal(candidates);
        List<NoticeCandidateDto> sorted = new ArrayList<>(candidates);
        sorted.sort(COMPARATOR);
        return sorted;
    }

    private static String loseReason(NoticeCandidateDto candidate, NoticeCandidateDto winner) {
        if (Boolean.TRUE.equals(candidate.getDeleted())) {
            return "Удалено";
        }
        if (Boolean.TRUE.equals(candidate.getLocalCopy())) {
            return "Локальная копия";
        }
        if (Boolean.TRUE.equals(candidate.getCanceled())) {
            return "Отменено";
        }
        if (worseModifVersion(candidate.getModifVersion(), winner.getModifVersion())) {
            return "Более старая редакция";
        }
        if (publishedEarlier(candidate.getPublishedAt(), winner.getPublishedAt())) {
            return "Опубликовано раньше";
        }
        return "Дубль (больший id)";
    }

    private static boolean worseModifVersion(Integer candidate, Integer winner) {
        if (Objects.equals(candidate, winner)) {
            return false;
        }
        if (candidate == null) {
            return true;
        }
        if (winner == null) {
            return false;
        }
        return candidate < winner;
    }

    private static boolean publishedEarlier(LocalDateTime candidate, LocalDateTime winner) {
        if (candidate == null || winner == null) {
            return false;
        }
        return candidate.isBefore(winner);
    }
}
