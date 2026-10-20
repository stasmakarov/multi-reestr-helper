package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum RuleTemplateType implements EnumClass<String> {
    OKPD2_FOREIGN_BAN("OKPD2_FOREIGN_BAN"),
    KTRU_REQUIRED("KTRU_REQUIRED"),
    KTRU_EXTRA_CHARACTERISTICS("KTRU_EXTRA_CHARACTERISTICS"),
    NON_KTRU_NO_DESCRIPTION("NON_KTRU_NO_DESCRIPTION"),
    CONTRACT_KIND_MISMATCH("CONTRACT_KIND_MISMATCH"),
    DEADLINE("DEADLINE"),
    LATE_DELIVERY_PENALTY("LATE_DELIVERY_PENALTY"),
    DOCUMENT_STRUCTURE_MISMATCH("DOCUMENT_STRUCTURE_MISMATCH");

    private final String id;

    RuleTemplateType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static RuleTemplateType fromId(String id) {
        for (RuleTemplateType value : RuleTemplateType.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
