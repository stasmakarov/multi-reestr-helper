package com.digitilius.multireestr.legacy.planschedule;

import com.digitilius.multireestr.entity.legacy.PlanScheduleDto;
import com.digitilius.multireestr.legacy.notice.LegacySqlLoader;
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

    private final NamedParameterJdbcTemplate legacyJdbc;
    private final PlanScheduleRowMapper rowMapper;
    private final String listSql;

    public PlanScheduleLookupService(
            @Qualifier("legacyJdbc") NamedParameterJdbcTemplate legacyJdbc,
            ResourceLoader resourceLoader,
            PlanScheduleRowMapper rowMapper
    ) {
        this.legacyJdbc = legacyJdbc;
        this.rowMapper = rowMapper;
        this.listSql = LegacySqlLoader.load(resourceLoader, "classpath:sql/legacy/plan-schedule-packets.sql");
    }

    public List<PlanScheduleDto> listCurrent() {
        try {
            return legacyJdbc.query(listSql, Map.of(), rowMapper);
        } catch (DataAccessException e) {
            log.debug("План-график из legacy недоступен: {}", e.getMessage());
            return List.of();
        }
    }
}
