package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.entity.legacy.OrganizationDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ13RowMapper implements RowMapper<ContractDto> {

    private final Metadata metadata;

    public NoticeCardQ13RowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public ContractDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        ContractDto contract = metadata.create(ContractDto.class);
        contract.setId(rs.getInt("id"));
        contract.setNumber(rs.getString("contract_number"));
        contract.setPurchaseNumber(rs.getString("purchase_number"));
        contract.setRegistryNumber(rs.getString("reestrnumber"));
        contract.setIkz(rs.getString("ikz"));
        contract.setDate(LegacyResultSets.localDate(rs, "contractdate"));
        contract.setEndDate(LegacyResultSets.localDate(rs, "contractdateend"));
        contract.setPrice(rs.getBigDecimal("contractcena"));
        contract.setReductionPct(rs.getBigDecimal("reduction_pct"));
        contract.setCustomer(mapOrganization(rs, "customer_id", "customer_name", "customer_full_name",
                "customer_inn", "customer_kpp"));
        contract.setSupplier(mapOrganization(rs, "supplier_id", "supplier_name", "supplier_full_name",
                "supplier_inn", "supplier_kpp"));
        return contract;
    }

    private OrganizationDto mapOrganization(
            ResultSet rs,
            String idColumn,
            String nameColumn,
            String fullNameColumn,
            String innColumn,
            String kppColumn
    ) throws SQLException {
        Integer id = LegacyResultSets.integer(rs, idColumn);
        if (id == null) {
            return null;
        }
        OrganizationDto organization = metadata.create(OrganizationDto.class);
        organization.setId(id);
        organization.setName(rs.getString(nameColumn));
        organization.setFullName(rs.getString(fullNameColumn));
        organization.setInn(rs.getString(innColumn));
        organization.setKpp(rs.getString(kppColumn));
        return organization;
    }
}
