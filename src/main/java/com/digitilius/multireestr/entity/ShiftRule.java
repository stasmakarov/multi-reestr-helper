package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ShiftRule implements EnumClass<String> {
    NONE("NONE"),
    NEXT_WORKDAY("NEXT_WORKDAY"),
    PREVIOUS_WORKDAY("PREVIOUS_WORKDAY");

    private final String id;

    ShiftRule(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ShiftRule fromId(String id) {
        for (ShiftRule value : ShiftRule.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
