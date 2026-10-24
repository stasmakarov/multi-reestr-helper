package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.SpecPositionDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ10RowMapper implements RowMapper<SpecPositionDto> {

    private final Metadata metadata;

    public NoticeCardQ10RowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public SpecPositionDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        SpecPositionDto item = metadata.create(SpecPositionDto.class);
        item.setId(rs.getInt("id"));
        item.setLotId(LegacyResultSets.integer(rs, "lotid"));
        item.setPos(LegacyResultSets.integer(rs, "pos_no"));
        item.setName(rs.getString("articlename"));
        item.setOkpd2(rs.getString("okpdcode"));
        item.setOkpd4Ktru(rs.getString("okpd4ktru"));
        item.setKtru(rs.getString("ktru_code"));
        item.setQty(rs.getBigDecimal("kol_vo"));
        item.setOkei(LegacyResultSets.integer(rs, "okeicode"));
        item.setUnit(rs.getString("unit"));
        item.setPrice(rs.getBigDecimal("cena"));
        item.setAmount(rs.getBigDecimal("summa"));
        item.setForeignBan(LegacyResultSets.flag(rs, "isprohibitionforeignpo"));
        item.setForeignRestriction(LegacyResultSets.flag(rs, "isrestrictforeignpo"));
        item.setRfPreference(LegacyResultSets.flag(rs, "ispreferenserfpo"));
        item.setBanImpossible(LegacyResultSets.flag(rs, "isimposibilityprohibition"));
        item.setBanImpossibleReason(rs.getString("reasonimposibilityprohibition"));
        return item;
    }
}
