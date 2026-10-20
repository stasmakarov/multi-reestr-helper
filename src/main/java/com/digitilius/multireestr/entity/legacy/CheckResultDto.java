package com.digitilius.multireestr.entity.legacy;

import com.digitilius.multireestr.entity.DryRunDiff;
import com.digitilius.multireestr.entity.RuleObjectType;
import com.digitilius.multireestr.entity.Severity;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.util.UUID;

@JmixEntity(name = "mtr_CheckResultDto")
public class CheckResultDto {

    @JmixId
    @JmixGeneratedValue
    @JmixProperty(mandatory = true)
    private UUID id;

    @JmixProperty(mandatory = true)
    private String ruleCode;

    private String ruleName;

    @JmixProperty(mandatory = true)
    private Severity severity;

    @JmixProperty(mandatory = true)
    private RuleObjectType objectType;

    @JmixProperty(mandatory = true)
    private String objectKey;

    private String detail;

    @InstanceName
    @JmixProperty(mandatory = true)
    private String message;

    private String legalRef;

    @JmixProperty(mandatory = true)
    private String fingerprint;

    private DryRunDiff diff;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public RuleObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(RuleObjectType objectType) {
        this.objectType = objectType;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
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

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public DryRunDiff getDiff() {
        return diff;
    }

    public void setDiff(DryRunDiff diff) {
        this.diff = diff;
    }
}
