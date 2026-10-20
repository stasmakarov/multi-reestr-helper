package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ExecutionKind implements EnumClass<String> {
    ACCEPTANCE("ACCEPTANCE"),
    PAYMENT("PAYMENT");

    private final String id;

    ExecutionKind(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ExecutionKind fromId(String id) {
        for (ExecutionKind value : ExecutionKind.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
