package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ParameterType implements EnumClass<String> {
    STRING("STRING"),
    INTEGER("INTEGER"),
    DECIMAL("DECIMAL"),
    BOOLEAN("BOOLEAN"),
    DATE("DATE"),
    LIST("LIST");

    private final String id;

    ParameterType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ParameterType fromId(String id) {
        for (ParameterType value : ParameterType.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
