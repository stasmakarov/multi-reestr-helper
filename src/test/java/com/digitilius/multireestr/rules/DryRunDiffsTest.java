package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.DryRunDiff;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DryRunDiffsTest {

    @Test
    void classifiesNewUnchangedAndDisappeared() {
        CheckResultDto shared = hit("fp-same", "same");
        CheckResultDto onlyDraft = hit("fp-new", "new");
        CheckResultDto onlyActive = hit("fp-gone", "gone");

        List<CheckResultDto> compared = DryRunDiffs.compare(
                List.of(shared, onlyDraft),
                List.of(hit("fp-same", "active-same"), onlyActive)
        );

        assertThat(compared).extracting(CheckResultDto::getDiff)
                .containsExactly(DryRunDiff.UNCHANGED, DryRunDiff.NEW, DryRunDiff.DISAPPEARED);
        assertThat(compared).extracting(CheckResultDto::getMessage)
                .containsExactly("same", "new", "gone");
    }

    @Test
    void emptyActiveMakesAllDraftNew() {
        List<CheckResultDto> compared = DryRunDiffs.compare(List.of(hit("fp-1", "a")), List.of());
        assertThat(compared).hasSize(1);
        assertThat(compared.get(0).getDiff()).isEqualTo(DryRunDiff.NEW);
    }

    @Test
    void emptyDraftMakesAllActiveDisappeared() {
        List<CheckResultDto> compared = DryRunDiffs.compare(List.of(), List.of(hit("fp-1", "a")));
        assertThat(compared).hasSize(1);
        assertThat(compared.get(0).getDiff()).isEqualTo(DryRunDiff.DISAPPEARED);
    }

    @Test
    void duplicateFingerprintsKeepFirst() {
        CheckResultDto first = hit("fp-1", "first");
        CheckResultDto second = hit("fp-1", "second");
        List<CheckResultDto> compared = DryRunDiffs.compare(List.of(first, second), List.of(hit("fp-1", "active")));
        assertThat(compared).hasSize(1);
        assertThat(compared.get(0).getMessage()).isEqualTo("first");
        assertThat(compared.get(0).getDiff()).isEqualTo(DryRunDiff.UNCHANGED);
    }

    private static CheckResultDto hit(String fingerprint, String message) {
        CheckResultDto dto = new CheckResultDto();
        dto.setId(UUID.randomUUID());
        dto.setFingerprint(fingerprint);
        dto.setMessage(message);
        dto.setRuleCode("R03_PP1875");
        dto.setObjectKey("0173100017026000001");
        return dto;
    }
}
