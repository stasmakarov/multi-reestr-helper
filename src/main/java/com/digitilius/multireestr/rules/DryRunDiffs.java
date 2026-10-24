package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.DryRunDiff;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DryRunDiffs {

    private DryRunDiffs() {
    }

    public static List<CheckResultDto> compare(List<CheckResultDto> draftHits, List<CheckResultDto> activeHits) {
        Map<String, CheckResultDto> draftByFp = byFingerprint(draftHits);
        Map<String, CheckResultDto> activeByFp = byFingerprint(activeHits);
        List<CheckResultDto> result = new ArrayList<>();
        for (CheckResultDto hit : draftByFp.values()) {
            if (activeByFp.containsKey(hit.getFingerprint())) {
                hit.setDiff(DryRunDiff.UNCHANGED);
            } else {
                hit.setDiff(DryRunDiff.NEW);
            }
            result.add(hit);
        }
        for (CheckResultDto hit : activeByFp.values()) {
            if (draftByFp.containsKey(hit.getFingerprint())) {
                continue;
            }
            hit.setDiff(DryRunDiff.DISAPPEARED);
            result.add(hit);
        }
        return result;
    }

    private static Map<String, CheckResultDto> byFingerprint(List<CheckResultDto> hits) {
        Map<String, CheckResultDto> unique = new LinkedHashMap<>();
        if (hits == null) {
            return unique;
        }
        for (CheckResultDto hit : hits) {
            if (hit == null || hit.getFingerprint() == null || hit.getFingerprint().isBlank()) {
                continue;
            }
            unique.putIfAbsent(hit.getFingerprint(), hit);
        }
        return unique;
    }
}
