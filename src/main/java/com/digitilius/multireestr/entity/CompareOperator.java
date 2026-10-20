package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum CompareOperator implements EnumClass<String> {
    NOT_LATER("NOT_LATER"),
    NOT_EARLIER("NOT_EARLIER");

    private final String id;

    CompareOperator(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static CompareOperator fromId(String id) {
        for (CompareOperator value : CompareOperator.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
