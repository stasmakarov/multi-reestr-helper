package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeDocumentDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NoticeCardQ09RowMapper implements RowMapper<NoticeDocumentDto> {

    private final Metadata metadata;

    public NoticeCardQ09RowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public NoticeDocumentDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        NoticeDocumentDto document = metadata.create(NoticeDocumentDto.class);
        document.setId(rs.getInt("id"));
        document.setName(rs.getString("name"));
        document.setUrl(rs.getString("url"));
        return document;
    }
}
