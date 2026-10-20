package com.digitilius.multireestr.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "MTR_RULE_PARAMETER", indexes = {
        @Index(name = "IDX_MTR_RULE_PARAMETER_UNQ", columnList = "RULE_VERSION_ID, CODE", unique = true)
})
@Entity(name = "mtr_RuleParameter")
public class RuleParameter {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private OffsetDateTime lastModifiedDate;

    @JoinColumn(name = "RULE_VERSION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RuleVersion ruleVersion;

    @InstanceName
    @Column(name = "CODE", nullable = false, length = 50)
    private String code;

    @Column(name = "PARAM_TYPE", nullable = false, length = 50)
    private String paramType;

    @Column(name = "VALUE_", length = 4000)
    private String value;

    @JoinColumn(name = "REFERENCE_LIST_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ReferenceList referenceList;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public OffsetDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public RuleVersion getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(RuleVersion ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ParameterType getParamType() {
        return paramType == null ? null : ParameterType.fromId(paramType);
    }

    public void setParamType(ParameterType paramType) {
        this.paramType = paramType == null ? null : paramType.getId();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ReferenceList getReferenceList() {
        return referenceList;
    }

    public void setReferenceList(ReferenceList referenceList) {
        this.referenceList = referenceList;
    }
}
