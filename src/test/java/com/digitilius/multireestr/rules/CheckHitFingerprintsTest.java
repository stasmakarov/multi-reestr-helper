package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CheckHitFingerprintsTest {

    @Test
    void keepsFirstHitAndSkipsBlankFingerprint() {
        CheckResultDto first = hit("fp-1", "first");
        CheckResultDto duplicate = hit("fp-1", "second");
        CheckResultDto blank = hit("  ", "blank");
        CheckResultDto missing = hit(null, "missing");
        CheckResultDto other = hit("fp-2", "other");

        List<CheckResultDto> unique = CheckHitFingerprints.uniqueHits(
                List.of(first, duplicate, blank, missing, other));

        assertThat(unique).containsExactly(first, other);
        assertThat(CheckHitFingerprints.uniqueByFingerprint(null)).isEmpty();
    }

    private static CheckResultDto hit(String fingerprint, String message) {
        CheckResultDto dto = new CheckResultDto();
        dto.setFingerprint(fingerprint);
        dto.setMessage(message);
        return dto;
    }
}
