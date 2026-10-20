package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum DeadlineAnchor implements EnumClass<String> {
    LBO_DATE("LBO_DATE"),
    PLAN_APPROVAL_DATE("PLAN_APPROVAL_DATE"),
    NOTICE_PUBLISHED("NOTICE_PUBLISHED"),
    NOTICE_CHANGE_PUBLISHED("NOTICE_CHANGE_PUBLISHED"),
    APPLICATIONS_END("APPLICATIONS_END"),
    CONTRACT_SIGNED("CONTRACT_SIGNED"),
    DELIVERY_DOCUMENT("DELIVERY_DOCUMENT"),
    ACCEPTANCE("ACCEPTANCE"),
    PAYMENT("PAYMENT"),
    STAGE_END("STAGE_END");

    private final String id;

    DeadlineAnchor(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static DeadlineAnchor fromId(String id) {
        for (DeadlineAnchor value : DeadlineAnchor.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
