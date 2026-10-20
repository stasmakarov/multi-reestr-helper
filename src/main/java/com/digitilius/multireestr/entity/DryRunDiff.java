package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum DryRunDiff implements EnumClass<String> {
    NEW("NEW"),
    UNCHANGED("UNCHANGED"),
    DISAPPEARED("DISAPPEARED");

    private final String id;

    DryRunDiff(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static DryRunDiff fromId(String id) {
        for (DryRunDiff value : DryRunDiff.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
