package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum RuleVersionStatus implements EnumClass<Integer> {
    DRAFT(10),
    ACTIVE(20),
    ARCHIVED(30);

    private final Integer id;

    RuleVersionStatus(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Nullable
    public static RuleVersionStatus fromId(Integer id) {
        for (RuleVersionStatus value : RuleVersionStatus.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
