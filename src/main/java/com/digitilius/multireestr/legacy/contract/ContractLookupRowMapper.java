package com.digitilius.multireestr.legacy.contract;

import com.digitilius.multireestr.entity.legacy.ContractDto;
import com.digitilius.multireestr.legacy.notice.LegacyResultSets;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ContractLookupRowMapper implements RowMapper<ContractDto> {

    private final Metadata metadata;

    public ContractLookupRowMapper(Metadata metadata) {
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
        return contract;
    }
}
