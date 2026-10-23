package com.digitilius.multireestr.legacy.contract;

import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.legacy.notice.LegacySqlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ContractLookupService {

    private final NamedParameterJdbcTemplate legacyJdbc;
    private final ContractLookupRowMapper rowMapper;
    private final String searchSql;
    private final String byIdSql;

    public ContractLookupService(
            @Qualifier("legacyJdbc") NamedParameterJdbcTemplate legacyJdbc,
            ResourceLoader resourceLoader,
            ContractLookupRowMapper rowMapper
    ) {
        this.legacyJdbc = legacyJdbc;
        this.rowMapper = rowMapper;
        this.searchSql = LegacySqlLoader.load(resourceLoader, "classpath:sql/legacy/contract-lookup.sql");
        this.byIdSql = LegacySqlLoader.load(resourceLoader, "classpath:sql/legacy/contract-by-id.sql");
    }

    public Optional<ContractDto> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            List<ContractDto> found = legacyJdbc.query(byIdSql, Map.of("id", id), rowMapper);
            return found.stream().findFirst();
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public List<ContractDto> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String trimmed = query.trim();
        Integer id = parseId(trimmed);
        Map<String, Object> params = Map.of(
                "id", id == null ? -1 : id,
                "like", "%" + trimmed.toLowerCase() + "%"
        );
        try {
            return legacyJdbc.query(searchSql, params, rowMapper);
        } catch (DataAccessException e) {
            return List.of();
        }
    }

    private static Integer parseId(String query) {
        try {
            return Integer.valueOf(query);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
