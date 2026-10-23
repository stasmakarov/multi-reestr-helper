package com.digitilius.multireestr.legacy.planschedule;

import com.digitilius.multireestr.entity.legacy.PlanScheduleDto;
import com.digitilius.multireestr.legacy.notice.LegacyResultSets;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class PlanScheduleRowMapper implements RowMapper<PlanScheduleDto> {

    private final Metadata metadata;

    public PlanScheduleRowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public PlanScheduleDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        PlanScheduleDto dto = metadata.create(PlanScheduleDto.class);
        dto.setId(rs.getInt("id"));
        dto.setFinYear(LegacyResultSets.integer(rs, "finyear"));
        dto.setVersion(LegacyResultSets.integer(rs, "oosversion"));
        dto.setPublishDate(LegacyResultSets.localDateTime(rs, "publishdate"));
        dto.setApproveDate(LegacyResultSets.localDateTime(rs, "approvedate"));
        return dto;
    }
}
