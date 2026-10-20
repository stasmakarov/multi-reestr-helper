package com.digitilius.multireestr.entity;

import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
@Table(name = "MTR_CONTRACT_TERM", indexes = {
        @Index(name = "IDX_MTR_CONTRACT_TERM_UNQ", columnList = "CONTRACT_LEGACY_ID, TERM_TYPE", unique = true)
})
@Entity(name = "mtr_ContractTerm")
public class ContractTerm {

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

    @Column(name = "CONTRACT_LEGACY_ID", nullable = false)
    private Integer contractLegacyId;

    @Column(name = "CONTRACT_NUMBER", length = 100)
    private String contractNumber;

    @Column(name = "PURCHASE_NUMBER", length = 19)
    private String purchaseNumber;

    @InstanceName
    @Column(name = "TERM_TYPE", nullable = false, length = 50)
    private String termType;

    @Embedded
    @EmbeddedParameters(nullAllowed = true)
    @AttributeOverrides({
            @AttributeOverride(name = "anchor", column = @Column(name = "TERM_ANCHOR", length = 50)),
            @AttributeOverride(name = "offsetValue", column = @Column(name = "TERM_OFFSET_VALUE")),
            @AttributeOverride(name = "unit", column = @Column(name = "TERM_UNIT", length = 50)),
            @AttributeOverride(name = "startNextDay", column = @Column(name = "TERM_START_NEXT_DAY")),
            @AttributeOverride(name = "shiftRule", column = @Column(name = "TERM_SHIFT_RULE", length = 50)),
            @AttributeOverride(name = "target", column = @Column(name = "TERM_TARGET", length = 50)),
            @AttributeOverride(name = "compareOperator", column = @Column(name = "TERM_COMPARE_OPERATOR", length = 50)),
            @AttributeOverride(name = "warnBeforeDays", column = @Column(name = "TERM_WARN_BEFORE_DAYS"))
    })
    private DeadlineSpec term;

    @Column(name = "FIXED_DATE")
    private LocalDate fixedDate;

    @Column(name = "SOURCE_CLAUSE")
    private String sourceClause;

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

    public Integer getContractLegacyId() {
        return contractLegacyId;
    }

    public void setContractLegacyId(Integer contractLegacyId) {
        this.contractLegacyId = contractLegacyId;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getPurchaseNumber() {
        return purchaseNumber;
    }

    public void setPurchaseNumber(String purchaseNumber) {
        this.purchaseNumber = purchaseNumber;
    }

    public ContractTermType getTermType() {
        return termType == null ? null : ContractTermType.fromId(termType);
    }

    public void setTermType(ContractTermType termType) {
        this.termType = termType == null ? null : termType.getId();
    }

    public DeadlineSpec getTerm() {
        return term;
    }

    public void setTerm(DeadlineSpec term) {
        this.term = term;
    }

    public LocalDate getFixedDate() {
        return fixedDate;
    }

    public void setFixedDate(LocalDate fixedDate) {
        this.fixedDate = fixedDate;
    }

    public String getSourceClause() {
        return sourceClause;
    }

    public void setSourceClause(String sourceClause) {
        this.sourceClause = sourceClause;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
