package com.digitilius.multireestr.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "MTR_RULE", indexes = {
        @Index(name = "IDX_MTR_RULE_UNQ", columnList = "CODE", unique = true)
})
@Entity(name = "mtr_Rule")
public class Rule {

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

    @Column(name = "CODE", nullable = false, length = 50)
    private String code;

    @InstanceName
    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "TEMPLATE_TYPE", nullable = false, length = 50)
    private String templateType;

    @Column(name = "OBJECT_TYPE", nullable = false, length = 50)
    private String objectType;

    @Column(name = "SEVERITY", nullable = false, length = 50)
    private String severity;

    @Column(name = "LEGAL_REF", length = 1000)
    private String legalRef;

    @Lob
    @Column(name = "EXPLANATION_TEMPLATE")
    private String explanationTemplate;

    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "rule")
    private List<RuleVersion> versions;

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

    public RuleTemplateType getTemplateType() {
        return templateType == null ? null : RuleTemplateType.fromId(templateType);
    }

    public void setTemplateType(RuleTemplateType templateType) {
        this.templateType = templateType == null ? null : templateType.getId();
    }

    public RuleObjectType getObjectType() {
        return objectType == null ? null : RuleObjectType.fromId(objectType);
    }

    public void setObjectType(RuleObjectType objectType) {
        this.objectType = objectType == null ? null : objectType.getId();
    }

    public Severity getSeverity() {
        return severity == null ? null : Severity.fromId(severity);
    }

    public void setSeverity(Severity severity) {
        this.severity = severity == null ? null : severity.getId();
    }

    public String getLegalRef() {
        return legalRef;
    }

    public void setLegalRef(String legalRef) {
        this.legalRef = legalRef;
    }

    public String getExplanationTemplate() {
        return explanationTemplate;
    }

    public void setExplanationTemplate(String explanationTemplate) {
        this.explanationTemplate = explanationTemplate;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RuleVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<RuleVersion> versions) {
        this.versions = versions;
    }
}
