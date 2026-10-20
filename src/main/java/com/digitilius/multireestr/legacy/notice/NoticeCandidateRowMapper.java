package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.regex.Pattern;

@Component
public class NoticeCandidateRowMapper implements RowMapper<NoticeCandidateDto> {

    private static final Pattern LOCAL_COPY_SUBJECT = Pattern.compile("(?i)^\\s*Копия");

    private final Metadata metadata;

    public NoticeCandidateRowMapper(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public NoticeCandidateDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        NoticeCandidateDto dto = metadata.create(NoticeCandidateDto.class);
        dto.setId(rs.getInt("id"));
        String purchaseNumber = rs.getString("purchasenumber");
        dto.setNumber(purchaseNumber == null ? null : purchaseNumber.trim());
        Timestamp publishedAt = rs.getTimestamp("docpublishdate");
        dto.setPublishedAt(publishedAt == null ? null : publishedAt.toLocalDateTime());
        dto.setSubject(rs.getString("predmet"));
        Integer modifVersion = rs.getObject("modifversion", Integer.class);
        dto.setModifVersion(modifVersion);
        dto.setDeleted(flag(rs.getObject("isdeleted", Integer.class)));
        dto.setCanceled(flag(rs.getObject("iscanceled", Integer.class)));
        dto.setLocalCopy(isLocalCopy(rs.getString("srcguid"), dto.getSubject()));
        return dto;
    }

    private static boolean flag(Integer value) {
        return value != null && value != 0;
    }

    private static boolean isLocalCopy(String srcGuid, String subject) {
        boolean delphiGuid = srcGuid != null && srcGuid.startsWith("{");
        boolean copySubject = subject != null && LOCAL_COPY_SUBJECT.matcher(subject).find();
        return delphiGuid || copySubject;
    }
}
