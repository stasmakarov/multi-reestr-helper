package com.digitilius.multireestr.legacy.planschedule;

import com.digitilius.multireestr.entity.legacy.PlanScheduleDto;
import com.digitilius.multireestr.legacy.notice.LegacyResultSets;
import com.digitilius.multireestr.legacy.notice.LegacySqlLoader;
import io.jmix.core.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PlanScheduleLookupService {

    private static final Logger log = LoggerFactory.getLogger(PlanScheduleLookupService.class);
    private static final String SQL = "classpath:sql/legacy/plan-schedules.sql";

    private final NamedParameterJdbcTemplate legacyJdbc;
    private final Metadata metadata;
    private final String sql;

    public PlanScheduleLookupService(
            @Qualifier("legacyJdbc") NamedParameterJdbcTemplate legacyJdbc,
            Metadata metadata,
            ResourceLoader resourceLoader
    ) {
        this.legacyJdbc = legacyJdbc;
        this.metadata = metadata;
        this.sql = LegacySqlLoader.load(resourceLoader, SQL);
    }

    public List<PlanScheduleDto> listAll() {
        try {
            return legacyJdbc.query(sql, Map.of(), (rs, rowNum) -> {
                PlanScheduleDto dto = metadata.create(PlanScheduleDto.class);
                dto.setId(rs.getInt("id"));
                dto.setFinYear(LegacyResultSets.integer(rs, "finyear"));
                dto.setVersion(LegacyResultSets.integer(rs, "oosversion"));
                dto.setPublishDate(LegacyResultSets.localDateTime(rs, "publishdate"));
                dto.setApproveDate(LegacyResultSets.localDateTime(rs, "approvedate"));
                return dto;
            });
        } catch (DataAccessException e) {
            log.warn("planzakuppackets is not available", e);
            return List.of();
        }
    }
}
