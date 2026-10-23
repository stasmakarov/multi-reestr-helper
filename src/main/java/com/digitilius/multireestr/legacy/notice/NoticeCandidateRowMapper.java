package com.digitilius.multireestr.legacy.notice;

import com.digitilius.multireestr.entity.legacy.NoticeCandidateDto;
import io.jmix.core.Metadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

@Component
public class NoticeCandidateRowMapper implements RowMapper<NoticeCandidateDto> {

    private static final Pattern LOCAL_COPY_SUBJECT = Pattern.compile(
            "^[\\s\\u00A0]*Копия",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

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
        dto.setPublishedAt(LegacyResultSets.localDateTime(rs, "docpublishdate"));
        dto.setSubject(rs.getString("predmet"));
        dto.setModifVersion(LegacyResultSets.integer(rs, "modifversion"));
        dto.setDeleted(LegacyResultSets.flag(rs, "isdeleted"));
        dto.setCanceled(LegacyResultSets.flag(rs, "iscanceled"));
        dto.setLocalCopy(isLocalCopy(rs.getString("srcguid"), dto.getSubject()));
        return dto;
    }

    static boolean isLocalCopy(String srcGuid, String subject) {
        boolean delphiGuid = srcGuid != null && srcGuid.startsWith("{");
        boolean copySubject = subject != null && LOCAL_COPY_SUBJECT.matcher(subject).find();
        return delphiGuid || copySubject;
    }
}
