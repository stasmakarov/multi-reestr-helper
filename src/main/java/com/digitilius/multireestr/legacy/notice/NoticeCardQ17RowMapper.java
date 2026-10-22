package com.digitilius.multireestr.legacy.notice;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ17RowMapper implements RowMapper<BigDecimal> {

    @Override
    public BigDecimal mapRow(ResultSet rs, int rowNum) throws SQLException {
        BigDecimal paidTotal = rs.getBigDecimal("paid_total");
        return paidTotal == null ? BigDecimal.ZERO : paidTotal;
    }
}
