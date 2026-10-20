package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum CheckRunMode implements EnumClass<String> {
    ON_DEMAND("ON_DEMAND"),
    SCHEDULED("SCHEDULED"),
    DRY_RUN("DRY_RUN");

    private final String id;

    CheckRunMode(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static CheckRunMode fromId(String id) {
        for (CheckRunMode value : CheckRunMode.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
