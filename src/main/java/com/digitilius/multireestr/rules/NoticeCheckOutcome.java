package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.Rule;
import com.digitilius.multireestr.entity.RuleVersion;
import com.digitilius.multireestr.entity.legacy.CheckResultDto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record NoticeCheckOutcome(
        List<CheckResultDto> hits,
        Set<UUID> evaluatedRuleIds,
        Map<String, HitBinding> bindings
) {
    public record HitBinding(Rule rule, RuleVersion version) {
    }

    public static NoticeCheckOutcome empty() {
        return new NoticeCheckOutcome(List.of(), Set.of(), Map.of());
    }
}
