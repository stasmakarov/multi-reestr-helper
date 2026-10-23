package com.digitilius.multireestr.legacy.notice;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ04RowMapper implements RowMapper<NoticeCardProcedure> {

    @Override
    public NoticeCardProcedure mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new NoticeCardProcedure(
                LegacyResultSets.localDateTime(rs, "collectingstartdate"),
                LegacyResultSets.localDateTime(rs, "collectingenddate"),
                LegacyResultSets.localDate(rs, "biddingdate"),
                LegacyResultSets.localDate(rs, "summarizingdate")
        );
    }
}
