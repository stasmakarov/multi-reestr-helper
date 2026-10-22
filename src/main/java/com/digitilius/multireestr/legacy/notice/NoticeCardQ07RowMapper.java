package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeFinanceDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ07RowMapper implements RowMapper<NoticeFinanceDto> {

    private final Metadata metadata;

    public NoticeCardQ07RowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public NoticeFinanceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        NoticeFinanceDto finance = metadata.create(NoticeFinanceDto.class);
        finance.setId(rs.getInt("id"));
        finance.setLotId(LegacyResultSets.integer(rs, "lotid"));
        finance.setKbk(rs.getString("kbk"));
        finance.setFinYear(LegacyResultSets.integer(rs, "finyear"));
        finance.setAmount(rs.getBigDecimal("summa"));
        return finance;
    }
}
