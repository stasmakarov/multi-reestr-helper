package com.digitilius.multireestr.rules;

import com.digitilius.multireestr.entity.ReferenceList;
import com.digitilius.multireestr.entity.RuleParameter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RuleEvalParams {

    private final Map<String, RuleParameter> byCode;

    public RuleEvalParams(List<RuleParameter> parameters) {
        Map<String, RuleParameter> map = new LinkedHashMap<>();
        if (parameters != null) {
            for (RuleParameter parameter : parameters) {
                if (parameter != null && parameter.getCode() != null) {
                    map.put(parameter.getCode(), parameter);
                }
            }
        }
        this.byCode = Collections.unmodifiableMap(map);
    }

    public ReferenceList list(String code) {
        RuleParameter parameter = byCode.get(code);
        return parameter == null ? null : parameter.getReferenceList();
    }

    public boolean bool(String code, boolean defaultValue) {
        RuleParameter parameter = byCode.get(code);
        if (parameter == null || parameter.getValue() == null || parameter.getValue().isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(parameter.getValue().trim());
    }
}
