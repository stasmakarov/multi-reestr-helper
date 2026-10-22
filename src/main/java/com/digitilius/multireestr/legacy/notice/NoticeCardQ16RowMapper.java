package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.ExecutionKind;
import com.digitilius.multireestr.entity.legacy.ExecutionDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ16RowMapper implements RowMapper<ExecutionDto> {

    private final Metadata metadata;

    public NoticeCardQ16RowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public ExecutionDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        ExecutionDto execution = metadata.create(ExecutionDto.class);
        execution.setId(rs.getInt("id"));
        Integer executionType = LegacyResultSets.integer(rs, "executiontype");
        boolean payment = executionType != null && executionType == 1;
        execution.setKind(payment ? ExecutionKind.PAYMENT : ExecutionKind.ACCEPTANCE);
        execution.setStageNumber(LegacyResultSets.integer(rs, "ordinalnumber"));
        execution.setStageEndDate(LegacyResultSets.localDate(rs, "stageenddate"));
        execution.setPenaltyInfo(rs.getString("penaltyinfo"));
        execution.setDocName(rs.getString("pd_documentname"));
        if (payment) {
            execution.setDocNumber(rs.getString("pd_documentnum"));
            execution.setDocDate(LegacyResultSets.localDate(rs, "pd_documentdate"));
            execution.setAmount(rs.getBigDecimal("paid"));
        } else {
            execution.setDocNumber(rs.getString("de_documentnum"));
            execution.setDocDate(LegacyResultSets.localDate(rs, "de_documentdate"));
            execution.setAmount(rs.getBigDecimal("de_fulfilmentsum"));
        }
        execution.setAcceptDate(LegacyResultSets.localDate(rs, "de_deliveryacceptdate"));
        execution.setImproperExecution(rs.getString("improperexecutiontext"));
        return execution;
    }
}
