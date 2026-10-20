package com.digitilius.multireestr.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
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
@Table(name = "MTR_PLAN_SCHEDULE_EVENT", indexes = {
        @Index(name = "IDX_MTR_PLAN_SCHEDULE_EVENT_UNQ", columnList = "FIN_YEAR, PLAN_VERSION", unique = true)
})
@Entity(name = "mtr_PlanScheduleEvent")
public class PlanScheduleEvent {

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

    @InstanceName
    @Column(name = "FIN_YEAR", nullable = false)
    private Integer finYear;

    @Column(name = "PLAN_VERSION")
    private Integer planVersion;

    @Column(name = "LEGACY_PLAN_ID")
    private Integer legacyPlanId;

    @Column(name = "LBO_DATE", nullable = false)
    private LocalDate lboDate;

    @Column(name = "APPROVAL_DATE")
    private LocalDate approvalDate;

    @Column(name = "BASIS_DOCUMENT")
    private String basisDocument;

    @Lob
    @Column(name = "NOTE")
    private String note;

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

    public Integer getFinYear() {
        return finYear;
    }

    public void setFinYear(Integer finYear) {
        this.finYear = finYear;
    }

    public Integer getPlanVersion() {
        return planVersion;
    }

    public void setPlanVersion(Integer planVersion) {
        this.planVersion = planVersion;
    }

    public Integer getLegacyPlanId() {
        return legacyPlanId;
    }

    public void setLegacyPlanId(Integer legacyPlanId) {
        this.legacyPlanId = legacyPlanId;
    }

    public LocalDate getLboDate() {
        return lboDate;
    }

    public void setLboDate(LocalDate lboDate) {
        this.lboDate = lboDate;
    }

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getBasisDocument() {
        return basisDocument;
    }

    public void setBasisDocument(String basisDocument) {
        this.basisDocument = basisDocument;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
