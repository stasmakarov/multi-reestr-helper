package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.ContractItemDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ14RowMapper implements RowMapper<ContractItemDto> {

    private final Metadata metadata;

    public NoticeCardQ14RowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public ContractItemDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        ContractItemDto item = metadata.create(ContractItemDto.class);
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("articlename"));
        item.setOkpd2(rs.getString("okpdcode"));
        item.setKtru(rs.getString("ktru_code"));
        item.setQty(rs.getBigDecimal("kol_vo"));
        item.setUnit(rs.getString("unit"));
        item.setPrice(rs.getBigDecimal("cena"));
        item.setAmount(rs.getBigDecimal("summa"));
        return item;
    }
}
