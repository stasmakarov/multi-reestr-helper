package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.LotDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ05RowMapper implements RowMapper<LotDto> {

    private final Metadata metadata;

    public NoticeCardQ05RowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public LotDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        LotDto lot = metadata.create(LotDto.class);
        lot.setId(rs.getInt("id"));
        lot.setNumber(LegacyResultSets.integer(rs, "lotnumber"));
        lot.setMaxPrice(rs.getBigDecimal("maxprice"));
        lot.setCurrency(rs.getString("currencycode"));
        return lot;
    }
}
