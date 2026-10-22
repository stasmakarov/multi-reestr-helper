package com.digitilius.multireestr.legacy.notice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeCandidateRowMapperTest {

    @Test
    void localCopyMatchesCaseInsensitiveCyrillic() {
        assertThat(NoticeCandidateRowMapper.isLocalCopy(null, "копия 1")).isTrue();
        assertThat(NoticeCandidateRowMapper.isLocalCopy(null, "КОПИЯ 2")).isTrue();
        assertThat(NoticeCandidateRowMapper.isLocalCopy(null, "\u00A0Копия 16")).isTrue();
        assertThat(NoticeCandidateRowMapper.isLocalCopy(null, "Оригинал")).isFalse();
        assertThat(NoticeCandidateRowMapper.isLocalCopy("{guid}", "Оригинал")).isTrue();
    }
}
