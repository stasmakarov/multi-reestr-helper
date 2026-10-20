package com.digitilius.multireestr.entity.legacy;

import com.digitilius.multireestr.entity.ExecutionKind;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

@JmixEntity(name = "mtr_ExecutionDto")
public class ExecutionDto {

    @JmixId
    @JmixProperty(mandatory = true)
    private Integer id;

    @JmixProperty(mandatory = true)
    private ExecutionKind kind;

    private Integer stageNumber;

    private LocalDate stageEndDate;

    private String penaltyInfo;

    private String docName;

    @InstanceName
    private String docNumber;

    private LocalDate docDate;

    private LocalDate acceptDate;

    private BigDecimal amount;

    private String improperExecution;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ExecutionKind getKind() {
        return kind;
    }

    public void setKind(ExecutionKind kind) {
        this.kind = kind;
    }

    public Integer getStageNumber() {
        return stageNumber;
    }

    public void setStageNumber(Integer stageNumber) {
        this.stageNumber = stageNumber;
    }

    public LocalDate getStageEndDate() {
        return stageEndDate;
    }

    public void setStageEndDate(LocalDate stageEndDate) {
        this.stageEndDate = stageEndDate;
    }

    public String getPenaltyInfo() {
        return penaltyInfo;
    }

    public void setPenaltyInfo(String penaltyInfo) {
        this.penaltyInfo = penaltyInfo;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public LocalDate getDocDate() {
        return docDate;
    }

    public void setDocDate(LocalDate docDate) {
        this.docDate = docDate;
    }

    public LocalDate getAcceptDate() {
        return acceptDate;
    }

    public void setAcceptDate(LocalDate acceptDate) {
        this.acceptDate = acceptDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getImproperExecution() {
        return improperExecution;
    }

    public void setImproperExecution(String improperExecution) {
        this.improperExecution = improperExecution;
    }
}
