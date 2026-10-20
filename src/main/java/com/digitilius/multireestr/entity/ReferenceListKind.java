package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ReferenceListKind implements EnumClass<String> {
    OKPD2("OKPD2"),
    KTRU("KTRU"),
    KEYWORD("KEYWORD"),
    TEXT("TEXT");

    private final String id;

    ReferenceListKind(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ReferenceListKind fromId(String id) {
        for (ReferenceListKind value : ReferenceListKind.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
