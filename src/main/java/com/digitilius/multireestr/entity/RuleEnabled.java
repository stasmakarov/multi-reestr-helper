package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum RuleEnabled implements EnumClass<String> {
    YES("YES"),
    NO("NO"),
    EMPTY("EMPTY");

    private final String id;

    RuleEnabled(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static RuleEnabled fromId(String id) {
        for (RuleEnabled value : RuleEnabled.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
