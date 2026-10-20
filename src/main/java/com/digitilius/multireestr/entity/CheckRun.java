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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "MTR_CHECK_RUN", indexes = {
        @Index(name = "IDX_MTR_CHECK_RUN_STARTED", columnList = "STARTED_AT")
})
@Entity(name = "mtr_CheckRun")
public class CheckRun {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

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

    @Column(name = "MODE_", nullable = false, length = 50)
    private String mode;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @InstanceName
    @Column(name = "STARTED_AT", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "FINISHED_AT")
    private OffsetDateTime finishedAt;

    @Column(name = "OBJECT_TYPE", length = 50)
    private String objectType;

    @Column(name = "OBJECT_KEY", length = 50)
    private String objectKey;

    @Column(name = "PERIOD_FROM")
    private LocalDate periodFrom;

    @Column(name = "PERIOD_TO")
    private LocalDate periodTo;

    @JoinColumn(name = "RULE_VERSION_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private RuleVersion ruleVersion;

    @Column(name = "OBJECTS_CHECKED")
    private Integer objectsChecked;

    @Column(name = "VIOLATIONS_FOUND")
    private Integer violationsFound;

    @Lob
    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public CheckRunMode getMode() {
        return mode == null ? null : CheckRunMode.fromId(mode);
    }

    public void setMode(CheckRunMode mode) {
        this.mode = mode == null ? null : mode.getId();
    }

    public CheckRunStatus getStatus() {
        return status == null ? null : CheckRunStatus.fromId(status);
    }

    public void setStatus(CheckRunStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(OffsetDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public RuleObjectType getObjectType() {
        return objectType == null ? null : RuleObjectType.fromId(objectType);
    }

    public void setObjectType(RuleObjectType objectType) {
        this.objectType = objectType == null ? null : objectType.getId();
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public LocalDate getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(LocalDate periodFrom) {
        this.periodFrom = periodFrom;
    }

    public LocalDate getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(LocalDate periodTo) {
        this.periodTo = periodTo;
    }

    public RuleVersion getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(RuleVersion ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    public Integer getObjectsChecked() {
        return objectsChecked;
    }

    public void setObjectsChecked(Integer objectsChecked) {
        this.objectsChecked = objectsChecked;
    }

    public Integer getViolationsFound() {
        return violationsFound;
    }

    public void setViolationsFound(Integer violationsFound) {
        this.violationsFound = violationsFound;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
