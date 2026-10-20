package com.digitilius.multireestr.entity.legacy;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@JmixEntity(name = "mtr_NoticeDto")
public class NoticeDto {

    @JmixId
    @JmixProperty(mandatory = true)
    private Integer id;

    @InstanceName
    @JmixProperty(mandatory = true)
    private String number;

    private LocalDateTime publishedAt;

    private Integer modifVersion;

    private String subject;

    private String law;

    private String methodCode;

    private String methodName;

    private Integer contractKindCode;

    private String platform;

    private String eisUrl;

    private BigDecimal nmck;

    private String ikz;

    private String planPosition;

    private LocalDateTime applicationsFrom;

    private LocalDateTime applicationsTo;

    private LocalDate biddingDate;

    private LocalDate summarizingDate;

    private BigDecimal appGuaranteeAmount;

    private BigDecimal appGuaranteePct;

    private BigDecimal contractGuaranteePct;

    private Integer executionTermDays;

    private LocalDate executionEndDate;

    private String preferences;

    private OrganizationDto customer;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private List<LotDto> lots;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private List<SpecPositionDto> items;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private List<NoticeDocumentDto> documents;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private List<ProtocolDto> protocols;

    private ContractDto contract;

    private List<String> warnings;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Integer getModifVersion() {
        return modifVersion;
    }

    public void setModifVersion(Integer modifVersion) {
        this.modifVersion = modifVersion;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getLaw() {
        return law;
    }

    public void setLaw(String law) {
        this.law = law;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public void setMethodCode(String methodCode) {
        this.methodCode = methodCode;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Integer getContractKindCode() {
        return contractKindCode;
    }

    public void setContractKindCode(Integer contractKindCode) {
        this.contractKindCode = contractKindCode;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getEisUrl() {
        return eisUrl;
    }

    public void setEisUrl(String eisUrl) {
        this.eisUrl = eisUrl;
    }

    public BigDecimal getNmck() {
        return nmck;
    }

    public void setNmck(BigDecimal nmck) {
        this.nmck = nmck;
    }

    public String getIkz() {
        return ikz;
    }

    public void setIkz(String ikz) {
        this.ikz = ikz;
    }

    public String getPlanPosition() {
        return planPosition;
    }

    public void setPlanPosition(String planPosition) {
        this.planPosition = planPosition;
    }

    public LocalDateTime getApplicationsFrom() {
        return applicationsFrom;
    }

    public void setApplicationsFrom(LocalDateTime applicationsFrom) {
        this.applicationsFrom = applicationsFrom;
    }

    public LocalDateTime getApplicationsTo() {
        return applicationsTo;
    }

    public void setApplicationsTo(LocalDateTime applicationsTo) {
        this.applicationsTo = applicationsTo;
    }

    public LocalDate getBiddingDate() {
        return biddingDate;
    }

    public void setBiddingDate(LocalDate biddingDate) {
        this.biddingDate = biddingDate;
    }

    public LocalDate getSummarizingDate() {
        return summarizingDate;
    }

    public void setSummarizingDate(LocalDate summarizingDate) {
        this.summarizingDate = summarizingDate;
    }

    public BigDecimal getAppGuaranteeAmount() {
        return appGuaranteeAmount;
    }

    public void setAppGuaranteeAmount(BigDecimal appGuaranteeAmount) {
        this.appGuaranteeAmount = appGuaranteeAmount;
    }

    public BigDecimal getAppGuaranteePct() {
        return appGuaranteePct;
    }

    public void setAppGuaranteePct(BigDecimal appGuaranteePct) {
        this.appGuaranteePct = appGuaranteePct;
    }

    public BigDecimal getContractGuaranteePct() {
        return contractGuaranteePct;
    }

    public void setContractGuaranteePct(BigDecimal contractGuaranteePct) {
        this.contractGuaranteePct = contractGuaranteePct;
    }

    public Integer getExecutionTermDays() {
        return executionTermDays;
    }

    public void setExecutionTermDays(Integer executionTermDays) {
        this.executionTermDays = executionTermDays;
    }

    public LocalDate getExecutionEndDate() {
        return executionEndDate;
    }

    public void setExecutionEndDate(LocalDate executionEndDate) {
        this.executionEndDate = executionEndDate;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public OrganizationDto getCustomer() {
        return customer;
    }

    public void setCustomer(OrganizationDto customer) {
        this.customer = customer;
    }

    public List<LotDto> getLots() {
        return lots;
    }

    public void setLots(List<LotDto> lots) {
        this.lots = lots;
    }

    public List<SpecPositionDto> getItems() {
        return items;
    }

    public void setItems(List<SpecPositionDto> items) {
        this.items = items;
    }

    public List<NoticeDocumentDto> getDocuments() {
        return documents;
    }

    public void setDocuments(List<NoticeDocumentDto> documents) {
        this.documents = documents;
    }

    public List<ProtocolDto> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<ProtocolDto> protocols) {
        this.protocols = protocols;
    }

    public ContractDto getContract() {
        return contract;
    }

    public void setContract(ContractDto contract) {
        this.contract = contract;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
