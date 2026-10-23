package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeDto;
import com.digitilius.multireestr.entity.legacy.OrganizationDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ03RowMapper implements RowMapper<NoticeDto> {

    private final Metadata metadata;

    public NoticeCardQ03RowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public NoticeDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        NoticeDto notice = metadata.create(NoticeDto.class);
        notice.setId(rs.getInt("id"));
        notice.setNumber(rs.getString("purchase_number"));
        notice.setPublishedAt(LegacyResultSets.localDateTime(rs, "docpublishdate"));
        notice.setModifVersion(LegacyResultSets.integer(rs, "modifversion"));
        notice.setSubject(rs.getString("predmet"));
        notice.setLaw(rs.getString("law"));
        notice.setMethodCode(rs.getString("placingway_code"));
        String methodName = rs.getString("method_sp_name");
        if (methodName == null || methodName.isBlank()) {
            methodName = rs.getString("placing_way_name");
        }
        notice.setMethodName(methodName);
        notice.setContractKindCode(LegacyResultSets.integer(rs, "dogovortype"));
        notice.setPlatform(rs.getString("etp_name"));
        notice.setEisUrl(rs.getString("dochref"));
        notice.setNmck(rs.getBigDecimal("nmck"));
        notice.setCustomer(mapCustomer(rs));
        return notice;
    }

    private OrganizationDto mapCustomer(ResultSet rs) throws SQLException {
        Integer customerId = LegacyResultSets.integer(rs, "customer_id");
        if (customerId == null) {
            return null;
        }
        OrganizationDto customer = metadata.create(OrganizationDto.class);
        customer.setId(customerId);
        customer.setName(rs.getString("customer_short"));
        customer.setFullName(rs.getString("customer_full"));
        customer.setInn(rs.getString("customer_inn"));
        customer.setKpp(rs.getString("customer_kpp"));
        return customer;
    }
}
