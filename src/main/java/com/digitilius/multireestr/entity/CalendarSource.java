package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum CalendarSource implements EnumClass<String> {
    XMLCALENDAR("XMLCALENDAR"),
    LEGACY("LEGACY"),
    GENERATED("GENERATED"),
    MANUAL("MANUAL");

    private final String id;

    CalendarSource(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static CalendarSource fromId(String id) {
        for (CalendarSource value : CalendarSource.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
