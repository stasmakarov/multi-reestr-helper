package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.ProtocolDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ12RowMapper implements RowMapper<ProtocolDto> {

    private final Metadata metadata;

    public NoticeCardQ12RowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public ProtocolDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProtocolDto protocol = metadata.create(ProtocolDto.class);
        protocol.setId(rs.getInt("id"));
        protocol.setNumber(rs.getString("protocolnumber"));
        protocol.setDate(LegacyResultSets.localDate(rs, "protocoldate"));
        protocol.setTypeCode(LegacyResultSets.integer(rs, "protocoltype"));
        protocol.setTypeName(rs.getString("protocol_type_name"));
        return protocol;
    }
}
