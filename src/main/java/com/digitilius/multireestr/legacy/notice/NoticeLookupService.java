package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NoticeLookupService {

    public static final int SEARCH_NUMBER_LIMIT = 500;
    public static final int SEARCH_PAGE_SIZE = 20;

    public record SearchCriteria(
            NoticeNumberPattern number,
            LocalDate from,
            LocalDate to,
            String subject
    ) {
        public SearchCriteria {
            if (from != null && to != null && from.isAfter(to)) {
                throw new IllegalArgumentException("Дата «с» позже даты «по»");
            }
            subject = blankToNull(subject);
        }

        public static SearchCriteria empty() {
            return new SearchCriteria(null, null, null, null);
        }

        public static SearchCriteria of(String rawNumber, LocalDate from, LocalDate to, String subject) {
            return new SearchCriteria(
                    NoticeNumberPattern.parse(rawNumber).orElse(null),
                    from,
                    to,
                    subject
            );
        }

        public boolean exactNumber() {
            return number != null && number.kind() == NoticeNumberPattern.Kind.EXACT;
        }
    }

    public record NumberCount(int raw, int capped, boolean truncated) {
        public static NumberCount of(int raw) {
            boolean truncated = raw > SEARCH_NUMBER_LIMIT;
            return new NumberCount(raw, truncated ? SEARCH_NUMBER_LIMIT : raw, truncated);
        }
    }

    public record SearchPage(List<NoticeCandidateDto> originals, NumberCount count) {
    }

    private static final String BY_NUMBER_SQL = "classpath:sql/legacy/notice-candidates-by-number.sql";
    private static final String SEARCH_SQL = "classpath:sql/legacy/notice-candidates-search.sql";
    private static final String SEARCH_COUNT_SQL = "classpath:sql/legacy/notice-candidates-search-count.sql";
    private static final String BY_PERIOD_SQL = "classpath:sql/legacy/notice-candidates-by-publish-period.sql";

    private final NamedParameterJdbcTemplate legacyJdbc;
    private final NoticeCandidateRowMapper rowMapper;
    private final NoticeOriginalSelector selector;
    private final String byNumberSql;
    private final String searchSql;
    private final String searchCountSql;
    private final String byPeriodSql;

    public NoticeLookupService(
            @Qualifier("legacyJdbc") NamedParameterJdbcTemplate legacyJdbc,
            NoticeCandidateRowMapper rowMapper,
            ResourceLoader resourceLoader
    ) {
        this.legacyJdbc = legacyJdbc;
        this.rowMapper = rowMapper;
        this.selector = new NoticeOriginalSelector();
        this.byNumberSql = LegacySqlLoader.load(resourceLoader, BY_NUMBER_SQL);
        this.searchSql = LegacySqlLoader.load(resourceLoader, SEARCH_SQL);
        this.searchCountSql = LegacySqlLoader.load(resourceLoader, SEARCH_COUNT_SQL);
        this.byPeriodSql = LegacySqlLoader.load(resourceLoader, BY_PERIOD_SQL);
    }

    public List<NoticeCandidateDto> findByNumber(NoticeNumber number) {
        List<NoticeCandidateDto> candidates = legacyJdbc.query(
                byNumberSql,
                Map.of("pn", number.value()),
                rowMapper
        );
        return selector.markAndSort(candidates);
    }

    public List<NoticeCandidateDto> findOriginalsPublishedBetween(LocalDate from, LocalDate to, int max) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Нужен период публикации");
        }
        if (max <= 0) {
            return List.of();
        }
        int maxGroups = max;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("fromDate", from);
        params.addValue("toDate", to);
        params.addValue("maxGroups", maxGroups);
        List<NoticeCandidateDto> candidates = legacyJdbc.query(byPeriodSql, params, rowMapper);
        return markByNumberGroups(selector, candidates).stream()
                .filter(candidate -> Boolean.TRUE.equals(candidate.getChosen()))
                .limit(maxGroups)
                .toList();
    }

    public NumberCount countOriginals(SearchCriteria criteria) {
        Integer raw = legacyJdbc.queryForObject(searchCountSql, searchParams(criteria), Integer.class);
        return NumberCount.of(raw == null ? 0 : raw);
    }

    public SearchPage findOriginals(SearchCriteria criteria, int offset, int limit) {
        NumberCount count = countOriginals(criteria);
        int first = Math.max(offset, 0);
        int pageSize = limit <= 0 ? SEARCH_PAGE_SIZE : limit;
        int remaining = count.capped() - first;
        if (remaining <= 0) {
            return new SearchPage(List.of(), count);
        }
        int bounded = Math.min(pageSize, remaining);
        MapSqlParameterSource params = searchParams(criteria);
        params.addValue("offset", first);
        params.addValue("limit", bounded);
        params.addValue("cap", SEARCH_NUMBER_LIMIT);
        List<NoticeCandidateDto> rows = legacyJdbc.query(searchSql, params, rowMapper);
        List<NoticeCandidateDto> originals = markByNumberGroups(selector, rows).stream()
                .filter(candidate -> Boolean.TRUE.equals(candidate.getChosen()))
                .toList();
        return new SearchPage(originals, count);
    }

    public Optional<NoticeCandidateDto> chooseOriginal(List<NoticeCandidateDto> candidates) {
        return selector.chooseOriginal(candidates);
    }

    static List<NoticeCandidateDto> markByNumberGroups(
            NoticeOriginalSelector selector,
            List<NoticeCandidateDto> candidates
    ) {
        Map<String, List<NoticeCandidateDto>> groups = candidates.stream()
                .collect(Collectors.groupingBy(
                        NoticeLookupService::groupKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        List<NoticeCandidateDto> result = new ArrayList<>();
        for (List<NoticeCandidateDto> group : groups.values()) {
            result.addAll(selector.markAndSort(group));
        }
        return result;
    }

    static String groupKey(NoticeCandidateDto candidate) {
        return Objects.toString(candidate.getNumber(), "#" + candidate.getId());
    }

    private MapSqlParameterSource searchParams(SearchCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        NoticeNumberPattern number = criteria.number();
        if (number == null) {
            params.addValue("numberExact", null);
            params.addValue("numberLike", null);
        } else if (number.kind() == NoticeNumberPattern.Kind.EXACT) {
            params.addValue("numberExact", number.value());
            params.addValue("numberLike", null);
        } else {
            params.addValue("numberExact", null);
            params.addValue("numberLike", number.ilikePattern());
        }
        params.addValue("fromDate", criteria.from());
        params.addValue("toDate", criteria.to());
        params.addValue("subject", criteria.subject() == null ? null : NoticeNumberPattern.escapeIlike(criteria.subject()));
        return params;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
