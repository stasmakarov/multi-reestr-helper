package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum DayType implements EnumClass<String> {
    WORKING("WORKING"),
    SHORTENED("SHORTENED"),
    WEEKEND("WEEKEND"),
    HOLIDAY("HOLIDAY"),
    WORKING_TRANSFER("WORKING_TRANSFER");

    private final String id;

    DayType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }


    public boolean isBusiness() {
        return this == WORKING || this == SHORTENED || this == WORKING_TRANSFER;
    }


    @Nullable
    public static DayType fromId(String id) {
        for (DayType value : DayType.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
