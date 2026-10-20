package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ContractTermType implements EnumClass<String> {
    DELIVERY("DELIVERY"),
    ACCEPTANCE("ACCEPTANCE"),
    PAYMENT("PAYMENT");

    private final String id;

    ContractTermType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ContractTermType fromId(String id) {
        for (ContractTermType value : ContractTermType.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
