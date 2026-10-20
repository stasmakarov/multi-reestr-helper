package com.digitilius.multireestr.entity.legacy;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

@JmixEntity(name = "mtr_KtruCharacteristicDto")
public class KtruCharacteristicDto {

    @JmixId
    @JmixProperty(mandatory = true)
    private Integer id;

    private String code;

    @InstanceName
    @JmixProperty(mandatory = true)
    private String name;

    private String value;

    private String ktruCode;

    private Boolean fromEis;

    private Boolean required;

    private Integer charType;

    private Integer charKind;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKtruCode() {
        return ktruCode;
    }

    public void setKtruCode(String ktruCode) {
        this.ktruCode = ktruCode;
    }

    public Boolean getFromEis() {
        return fromEis;
    }

    public void setFromEis(Boolean fromEis) {
        this.fromEis = fromEis;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getCharType() {
        return charType;
    }

    public void setCharType(Integer charType) {
        this.charType = charType;
    }

    public Integer getCharKind() {
        return charKind;
    }

    public void setCharKind(Integer charKind) {
        this.charKind = charKind;
    }
}
