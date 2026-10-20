package com.digitilius.multireestr.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "MTR_RULE_VERSION", indexes = {
        @Index(name = "IDX_MTR_RULE_VERSION_UNQ", columnList = "RULE_ID, VERSION_NUMBER", unique = true),
        @Index(name = "IDX_MTR_RULE_VERSION_RULE_STATUS", columnList = "RULE_ID, STATUS")
})
@Entity(name = "mtr_RuleVersion")
public class RuleVersion {

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

    @InstanceName
    @Column(name = "VERSION_NUMBER", nullable = false)
    private Integer versionNumber;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "VALID_FROM", nullable = false)
    private LocalDate validFrom;

    @Column(name = "VALID_TO")
    private LocalDate validTo;

    @Embedded
    @EmbeddedParameters(nullAllowed = true)
    @AttributeOverrides({
            @AttributeOverride(name = "anchor", column = @Column(name = "DEADLINE_ANCHOR", length = 50)),
            @AttributeOverride(name = "offsetValue", column = @Column(name = "DEADLINE_OFFSET_VALUE")),
            @AttributeOverride(name = "unit", column = @Column(name = "DEADLINE_UNIT", length = 50)),
            @AttributeOverride(name = "startNextDay", column = @Column(name = "DEADLINE_START_NEXT_DAY")),
            @AttributeOverride(name = "shiftRule", column = @Column(name = "DEADLINE_SHIFT_RULE", length = 50)),
            @AttributeOverride(name = "target", column = @Column(name = "DEADLINE_TARGET", length = 50)),
            @AttributeOverride(name = "compareOperator", column = @Column(name = "DEADLINE_COMPARE_OPERATOR", length = 50)),
            @AttributeOverride(name = "warnBeforeDays", column = @Column(name = "DEADLINE_WARN_BEFORE_DAYS"))
    })
    private DeadlineSpec deadline;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "ruleVersion")
    private List<RuleParameter> parameters;

    @Lob
    @Column(name = "CHANGE_NOTE")
    private String changeNote;

    @Column(name = "APPROVED_BY")
    private String approvedBy;

    @Column(name = "APPROVED_AT")
    private OffsetDateTime approvedAt;

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

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public RuleVersionStatus getStatus() {
        return status == null ? null : RuleVersionStatus.fromId(status);
    }

    public void setStatus(RuleVersionStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public DeadlineSpec getDeadline() {
        return deadline;
    }

    public void setDeadline(DeadlineSpec deadline) {
        this.deadline = deadline;
    }

    public List<RuleParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<RuleParameter> parameters) {
        this.parameters = parameters;
    }

    public String getChangeNote() {
        return changeNote;
    }

    public void setChangeNote(String changeNote) {
        this.changeNote = changeNote;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public OffsetDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}
