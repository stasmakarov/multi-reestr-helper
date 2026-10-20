package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum RuleObjectType implements EnumClass<String> {
    NOTICE("NOTICE"),
    CONTRACT("CONTRACT"),
    PLAN_SCHEDULE("PLAN_SCHEDULE");

    private final String id;

    RuleObjectType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static RuleObjectType fromId(String id) {
        for (RuleObjectType value : RuleObjectType.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
