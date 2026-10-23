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

    static final int DATE_SUBJECT_NUMBER_LIMIT = 200;

    public record DateSubjectLookup(List<NoticeCandidateDto> candidates, boolean truncated) {
    }

    private static final String BY_NUMBER_SQL = "classpath:sql/legacy/notice-candidates-by-number.sql";
    private static final String BY_DATE_SUBJECT_SQL = "classpath:sql/legacy/notice-candidates-by-date-subject.sql";

    private final NamedParameterJdbcTemplate legacyJdbc;
    private final NoticeCandidateRowMapper rowMapper;
    private final NoticeOriginalSelector selector;
    private final String byNumberSql;
    private final String byDateSubjectSql;

    public NoticeLookupService(
            @Qualifier("legacyJdbc") NamedParameterJdbcTemplate legacyJdbc,
            NoticeCandidateRowMapper rowMapper,
            ResourceLoader resourceLoader
    ) {
        this.legacyJdbc = legacyJdbc;
        this.rowMapper = rowMapper;
        this.selector = new NoticeOriginalSelector();
        this.byNumberSql = LegacySqlLoader.load(resourceLoader, BY_NUMBER_SQL);
        this.byDateSubjectSql = LegacySqlLoader.load(resourceLoader, BY_DATE_SUBJECT_SQL);
    }

    public List<NoticeCandidateDto> findByNumber(NoticeNumber number) {
        List<NoticeCandidateDto> candidates = legacyJdbc.query(
                byNumberSql,
                Map.of("pn", number.value()),
                rowMapper
        );
        return selector.markAndSort(candidates);
    }

    public DateSubjectLookup findByDateAndSubject(LocalDate date, String subjectFragment) {
        String fragment = blankToNull(subjectFragment);
        if (date == null && fragment == null) {
            throw new IllegalArgumentException("Нужна дата публикации или фрагмент объекта закупки");
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("date", date);
        params.addValue("subject", fragment == null ? null : escapeIlike(fragment));
        boolean[] truncated = {false};
        List<NoticeCandidateDto> candidates = legacyJdbc.query(byDateSubjectSql, params, (rs, rowNum) -> {
            int rn = rs.getInt("rn");
            if (rn > DATE_SUBJECT_NUMBER_LIMIT) {
                truncated[0] = true;
                return null;
            }
            return rowMapper.mapRow(rs, rowNum);
        }).stream().filter(Objects::nonNull).toList();
        return new DateSubjectLookup(markByNumberGroups(selector, candidates), truncated[0]);
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

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private static String escapeIlike(String fragment) {
        return fragment
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

}
