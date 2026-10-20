package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum DayCountUnit implements EnumClass<String> {
    CALENDAR_DAYS("CALENDAR_DAYS"),
    WORKING_DAYS("WORKING_DAYS"),
    MONTHS("MONTHS");

    private final String id;

    DayCountUnit(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static DayCountUnit fromId(String id) {
        for (DayCountUnit value : DayCountUnit.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
