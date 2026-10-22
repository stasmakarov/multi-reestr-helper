package com.digitilius.multireestr.legacy.notice;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ06RowMapper implements RowMapper<NoticeCardRequirements> {

    @Override
    public NoticeCardRequirements mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new NoticeCardRequirements(
                rs.getString("ikz"),
                rs.getString("pglotreestrnumber"),
                rs.getBigDecimal("aguarantee_amount"),
                rs.getBigDecimal("aguarantee_part"),
                rs.getBigDecimal("cguarantee_part"),
                LegacyResultSets.integer(rs, "relativelen"),
                LegacyResultSets.localDate(rs, "nonrelativeenddate")
        );
    }
}
