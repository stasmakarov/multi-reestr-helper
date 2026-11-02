package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.legacy.CheckResultDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CheckHitFingerprints {

    private CheckHitFingerprints() {
    }

    public static Map<String, CheckResultDto> uniqueByFingerprint(List<CheckResultDto> hits) {
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

    public static List<CheckResultDto> uniqueHits(List<CheckResultDto> hits) {
        return new ArrayList<>(uniqueByFingerprint(hits).values());
    }
}
