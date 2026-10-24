package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.KtruCharacteristicDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ11RowMapper implements RowMapper<NoticeCardKtruRow> {

    private final Metadata metadata;

    public NoticeCardQ11RowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public NoticeCardKtruRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        KtruCharacteristicDto characteristic = metadata.create(KtruCharacteristicDto.class);
        characteristic.setId(rs.getInt("id"));
        characteristic.setCode(rs.getString("haractcode"));
        characteristic.setName(rs.getString("haractname"));
        characteristic.setValue(rs.getString("vals"));
        characteristic.setKtruCode(rs.getString("ktrucode"));
        characteristic.setFromEis(LegacyResultSets.flag(rs, "isfromoos"));
        characteristic.setRequired(LegacyResultSets.flag(rs, "isrequired"));
        characteristic.setCharType(LegacyResultSets.integer(rs, "haracttype"));
        characteristic.setCharKind(LegacyResultSets.integer(rs, "haractkind"));
        return new NoticeCardKtruRow(LegacyResultSets.integer(rs, "noticespecid"), characteristic);
    }
}
