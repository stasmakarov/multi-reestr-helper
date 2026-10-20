package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ViolationStatus implements EnumClass<Integer> {
    NEW(10),
    ACKNOWLEDGED(20),
    FALSE_POSITIVE(30),
    FIXED(40);

    private final Integer id;

    ViolationStatus(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Nullable
    public static ViolationStatus fromId(Integer id) {
        for (ViolationStatus value : ViolationStatus.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
