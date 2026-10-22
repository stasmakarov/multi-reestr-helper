package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.KtruCharacteristicDto;
import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.NoticeFinanceDto;
import com.digitilius.multireestr.entity.legacy.ProtocolDto;
import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NoticeCardService {

    private final NamedParameterJdbcTemplate legacyJdbc;
    private final NoticeLookupService noticeLookupService;
    private final NoticeCardQ03RowMapper q03RowMapper;
    private final NoticeCardQ04RowMapper q04RowMapper;
    private final NoticeCardQ05RowMapper q05RowMapper;
    private final NoticeCardQ06RowMapper q06RowMapper;
    private final NoticeCardQ07RowMapper q07RowMapper;
    private final NoticeCardQ08RowMapper q08RowMapper;
    private final NoticeCardQ09RowMapper q09RowMapper;
    private final NoticeCardQ10RowMapper q10RowMapper;
    private final NoticeCardQ11RowMapper q11RowMapper;
    private final NoticeCardQ12RowMapper q12RowMapper;
    private final String q03Sql;
    private final String q04Sql;
    private final String q05Sql;
    private final String q06Sql;
    private final String q07Sql;
    private final String q08Sql;
    private final String q09Sql;
    private final String q10Sql;
    private final String q11Sql;
    private final String q12Sql;

    public NoticeCardService(
            @Qualifier("legacyJdbc") NamedParameterJdbcTemplate legacyJdbc,
            NoticeLookupService noticeLookupService,
            ResourceLoader resourceLoader,
            NoticeCardQ03RowMapper q03RowMapper,
            NoticeCardQ04RowMapper q04RowMapper,
            NoticeCardQ05RowMapper q05RowMapper,
            NoticeCardQ06RowMapper q06RowMapper,
            NoticeCardQ07RowMapper q07RowMapper,
            NoticeCardQ08RowMapper q08RowMapper,
            NoticeCardQ09RowMapper q09RowMapper,
            NoticeCardQ10RowMapper q10RowMapper,
            NoticeCardQ11RowMapper q11RowMapper,
            NoticeCardQ12RowMapper q12RowMapper
    ) {
        this.legacyJdbc = legacyJdbc;
        this.noticeLookupService = noticeLookupService;
        this.q03RowMapper = q03RowMapper;
        this.q04RowMapper = q04RowMapper;
        this.q05RowMapper = q05RowMapper;
        this.q06RowMapper = q06RowMapper;
        this.q07RowMapper = q07RowMapper;
        this.q08RowMapper = q08RowMapper;
        this.q09RowMapper = q09RowMapper;
        this.q10RowMapper = q10RowMapper;
        this.q11RowMapper = q11RowMapper;
        this.q12RowMapper = q12RowMapper;
        this.q03Sql = LegacySqlLoader.load(resourceLoader, NoticeCardSql.Q03);
        this.q04Sql = LegacySqlLoader.load(resourceLoader, NoticeCardSql.Q04);
        this.q05Sql = LegacySqlLoader.load(resourceLoader, NoticeCardSql.Q05);
        this.q06Sql = LegacySqlLoader.load(resourceLoader, NoticeCardSql.Q06);
        this.q07Sql = LegacySqlLoader.load(resourceLoader, NoticeCardSql.Q07);
        this.q08Sql = LegacySqlLoader.load(resourceLoader, NoticeCardSql.Q08);
        this.q09Sql = LegacySqlLoader.load(resourceLoader, NoticeCardSql.Q09);
        this.q10Sql = LegacySqlLoader.load(resourceLoader, NoticeCardSql.Q10);
        this.q11Sql = LegacySqlLoader.load(resourceLoader, NoticeCardSql.Q11);
        this.q12Sql = LegacySqlLoader.load(resourceLoader, NoticeCardSql.Q12);
    }

    public Optional<NoticeDto> loadOriginal(NoticeNumber number) {
        return noticeLookupService.findByNumber(number).stream()
                .filter(candidate -> Boolean.TRUE.equals(candidate.getChosen()))
                .map(NoticeCandidateDto::getId)
                .findFirst()
                .flatMap(this::loadNotice);
    }

    public Optional<NoticeDto> loadNotice(Integer noticeId) {
        Map<String, Object> params = Map.of("nid", noticeId);
        List<NoticeDto> found = legacyJdbc.query(q03Sql, params, q03RowMapper);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        NoticeDto notice = found.get(0);

        List<NoticeCardProcedure> procedures = legacyJdbc.query(q04Sql, params, q04RowMapper);
        if (!procedures.isEmpty()) {
            NoticeCardProcedure procedure = procedures.get(0);
            notice.setApplicationsFrom(procedure.applicationsFrom());
            notice.setApplicationsTo(procedure.applicationsTo());
            notice.setBiddingDate(procedure.biddingDate());
            notice.setSummarizingDate(procedure.summarizingDate());
        }

        notice.setLots(legacyJdbc.query(q05Sql, params, q05RowMapper));

        List<NoticeCardRequirements> requirements = legacyJdbc.query(q06Sql, params, q06RowMapper);
        if (!requirements.isEmpty()) {
            applyRequirements(notice, requirements.get(0));
        }

        List<NoticeFinanceDto> finances = legacyJdbc.query(q07Sql, params, q07RowMapper);
        notice.setFinances(finances);

        List<String> preferenceNames = legacyJdbc.query(q08Sql, params, q08RowMapper);
        if (!preferenceNames.isEmpty()) {
            notice.setPreferences(String.join("; ", preferenceNames));
        }

        notice.setDocuments(legacyJdbc.query(q09Sql, params, q09RowMapper));

        List<SpecPositionDto> items = legacyJdbc.query(q10Sql, params, q10RowMapper);
        if (!items.isEmpty()) {
            attachCharacteristics(items, legacyJdbc.query(q11Sql, params, q11RowMapper));
        }
        notice.setItems(items);
        notice.setProtocols(loadProtocols(notice.getNumber()));
        notice.setContract(null);
        notice.setWarnings(NoticeCardAssembler.warnings(notice, requirements.size()));
        return Optional.of(notice);
    }

    private List<ProtocolDto> loadProtocols(String purchaseNumber) {
        if (purchaseNumber == null) {
            return List.of();
        }
        return legacyJdbc.query(q12Sql, Map.of("npn", purchaseNumber), q12RowMapper);
    }

    private static void applyRequirements(NoticeDto notice, NoticeCardRequirements requirements) {
        notice.setIkz(requirements.ikz());
        notice.setPlanPosition(requirements.planPosition());
        notice.setAppGuaranteeAmount(requirements.appGuaranteeAmount());
        notice.setAppGuaranteePct(requirements.appGuaranteePct());
        notice.setContractGuaranteePct(requirements.contractGuaranteePct());
        Integer relativeLen = requirements.relativeLen();
        if (relativeLen != null && relativeLen != 0) {
            notice.setExecutionTermDays(relativeLen);
        }
        notice.setExecutionEndDate(requirements.executionEndDate());
    }

    private static void attachCharacteristics(List<SpecPositionDto> items, List<NoticeCardKtruRow> rows) {
        Map<Integer, List<KtruCharacteristicDto>> bySpecId = rows.stream()
                .filter(row -> row.noticeSpecId() != null)
                .collect(Collectors.groupingBy(
                        NoticeCardKtruRow::noticeSpecId,
                        Collectors.mapping(NoticeCardKtruRow::characteristic, Collectors.toList())
                ));
        for (SpecPositionDto item : items) {
            List<KtruCharacteristicDto> characteristics = bySpecId.getOrDefault(item.getId(), List.of());
            item.setCharacteristics(characteristics);
            if (!characteristics.isEmpty()) {
                item.setCharacteristicsText(characteristics.stream()
                        .map(NoticeCardService::characteristicText)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("; ")));
            }
        }
    }

    private static String characteristicText(KtruCharacteristicDto characteristic) {
        if (characteristic.getName() == null) {
            return null;
        }
        String value = characteristic.getValue() == null ? "" : characteristic.getValue();
        return characteristic.getName() + ": " + value;
    }

}
