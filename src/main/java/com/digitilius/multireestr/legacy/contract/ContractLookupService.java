package com.digitilius.multireestr.legacy.contract;

import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.legacy.notice.LegacySqlLoader;
import com.digitilius.multireestr.legacy.notice.NoticeCardQ13RowMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContractLookupService {

    private static final String SQL = "classpath:sql/legacy/contract-lookup.sql";

    private final NamedParameterJdbcTemplate legacyJdbc;
    private final NoticeCardQ13RowMapper rowMapper;
    private final String sql;

    public ContractLookupService(
            @Qualifier("legacyJdbc") NamedParameterJdbcTemplate legacyJdbc,
            NoticeCardQ13RowMapper rowMapper,
            ResourceLoader resourceLoader
    ) {
        this.legacyJdbc = legacyJdbc;
        this.rowMapper = rowMapper;
        this.sql = LegacySqlLoader.load(resourceLoader, SQL);
    }

    public Optional<ContractDto> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return search(id, null, null).stream().findFirst();
    }

    public List<ContractDto> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String trimmed = query.trim();
        Integer id = parseId(trimmed);
        String like = "%" + escapeIlike(trimmed) + "%";
        return search(id, like, like);
    }

    public List<ContractDto> search(Integer id, String contractNumber, String purchaseNumber) {
        if (id == null && isBlank(contractNumber) && isBlank(purchaseNumber)) {
            return List.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("contract_number", blankToNull(contractNumber));
        params.addValue("purchase_number", blankToNull(purchaseNumber));
        return legacyJdbc.query(sql, params, rowMapper);
    }

    private static Integer parseId(String query) {
        if (query.chars().allMatch(Character::isDigit) && query.length() <= 9) {
            try {
                return Integer.valueOf(query);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String escapeIlike(String fragment) {
        return fragment
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static String blankToNull(String value) {
        return isBlank(value) ? null : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
