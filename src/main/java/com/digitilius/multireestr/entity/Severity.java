package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum Severity implements EnumClass<String> {
    ERROR("ERROR"),
    WARNING("WARNING"),
    INFO("INFO");

    private final String id;

    Severity(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static Severity fromId(String id) {
        for (Severity value : Severity.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
