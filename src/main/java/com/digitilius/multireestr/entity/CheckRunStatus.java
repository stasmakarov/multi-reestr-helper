package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum CheckRunStatus implements EnumClass<Integer> {
    RUNNING(10),
    SUCCESS(20),
    PARTIAL(30),
    FAILED(40);

    private final Integer id;

    CheckRunStatus(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Nullable
    public static CheckRunStatus fromId(Integer id) {
        for (CheckRunStatus value : CheckRunStatus.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
