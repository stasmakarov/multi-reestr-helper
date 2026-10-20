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
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "MTR_VIOLATION", indexes = {
        @Index(name = "IDX_MTR_VIOLATION_UNQ", columnList = "FINGERPRINT", unique = true),
        @Index(name = "IDX_MTR_VIOLATION_OBJECT", columnList = "OBJECT_TYPE, OBJECT_KEY"),
        @Index(name = "IDX_MTR_VIOLATION_STATUS", columnList = "STATUS")
})
@Entity(name = "mtr_Violation")
public class Violation {

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

    @JoinColumn(name = "RULE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Rule rule;

    @JoinColumn(name = "RULE_VERSION_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RuleVersion ruleVersion;

    @JoinColumn(name = "LAST_CHECK_RUN_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private CheckRun lastCheckRun;

    @Column(name = "OBJECT_TYPE", nullable = false, length = 50)
    private String objectType;

    @Column(name = "OBJECT_KEY", nullable = false, length = 50)
    private String objectKey;

    @Column(name = "LEGACY_ID")
    private Integer legacyId;

    @Column(name = "OBJECT_CAPTION")
    private String objectCaption;

    @Column(name = "DETAIL")
    private String detail;

    @Column(name = "FINGERPRINT", nullable = false, length = 64)
    private String fingerprint;

    @Column(name = "SEVERITY", nullable = false, length = 50)
    private String severity;

    @InstanceName
    @Lob
    @Column(name = "MESSAGE", nullable = false)
    private String message;

    @Column(name = "LEGAL_REF", length = 1000)
    private String legalRef;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;

    @Column(name = "FACT_DATE")
    private LocalDate factDate;

    @Column(name = "DEVIATION_DAYS")
    private Integer deviationDays;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "FIRST_DETECTED_AT", nullable = false)
    private OffsetDateTime firstDetectedAt;

    @Column(name = "LAST_DETECTED_AT", nullable = false)
    private OffsetDateTime lastDetectedAt;

    @Column(name = "RESOLVED_AT")
    private OffsetDateTime resolvedAt;

    @Column(name = "ACKNOWLEDGED_BY")
    private String acknowledgedBy;

    @Column(name = "ACKNOWLEDGED_AT")
    private OffsetDateTime acknowledgedAt;

    @Lob
    @Column(name = "COMMENT_")
    private String comment;

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

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public RuleVersion getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(RuleVersion ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    public CheckRun getLastCheckRun() {
        return lastCheckRun;
    }

    public void setLastCheckRun(CheckRun lastCheckRun) {
        this.lastCheckRun = lastCheckRun;
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

    public Integer getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public String getObjectCaption() {
        return objectCaption;
    }

    public void setObjectCaption(String objectCaption) {
        this.objectCaption = objectCaption;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public Severity getSeverity() {
        return severity == null ? null : Severity.fromId(severity);
    }

    public void setSeverity(Severity severity) {
        this.severity = severity == null ? null : severity.getId();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLegalRef() {
        return legalRef;
    }

    public void setLegalRef(String legalRef) {
        this.legalRef = legalRef;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getFactDate() {
        return factDate;
    }

    public void setFactDate(LocalDate factDate) {
        this.factDate = factDate;
    }

    public Integer getDeviationDays() {
        return deviationDays;
    }

    public void setDeviationDays(Integer deviationDays) {
        this.deviationDays = deviationDays;
    }

    public ViolationStatus getStatus() {
        return status == null ? null : ViolationStatus.fromId(status);
    }

    public void setStatus(ViolationStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public OffsetDateTime getFirstDetectedAt() {
        return firstDetectedAt;
    }

    public void setFirstDetectedAt(OffsetDateTime firstDetectedAt) {
        this.firstDetectedAt = firstDetectedAt;
    }

    public OffsetDateTime getLastDetectedAt() {
        return lastDetectedAt;
    }

    public void setLastDetectedAt(OffsetDateTime lastDetectedAt) {
        this.lastDetectedAt = lastDetectedAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public OffsetDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(OffsetDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
