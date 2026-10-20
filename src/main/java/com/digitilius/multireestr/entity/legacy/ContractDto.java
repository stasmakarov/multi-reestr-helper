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
import java.util.List;

@JmixEntity(name = "mtr_ContractDto")
public class ContractDto {

    @JmixId
    @JmixProperty(mandatory = true)
    private Integer id;

    @InstanceName
    @JmixProperty(mandatory = true)
    private String number;

    private String purchaseNumber;

    private String registryNumber;

    private String ikz;

    private LocalDate date;

    private LocalDate endDate;

    private BigDecimal price;

    private BigDecimal reductionPct;

    private BigDecimal paidTotal;

    private OrganizationDto customer;

    private OrganizationDto supplier;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private List<ContractItemDto> items;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private List<ExecutionDto> executions;

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

    public String getPurchaseNumber() {
        return purchaseNumber;
    }

    public void setPurchaseNumber(String purchaseNumber) {
        this.purchaseNumber = purchaseNumber;
    }

    public String getRegistryNumber() {
        return registryNumber;
    }

    public void setRegistryNumber(String registryNumber) {
        this.registryNumber = registryNumber;
    }

    public String getIkz() {
        return ikz;
    }

    public void setIkz(String ikz) {
        this.ikz = ikz;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getReductionPct() {
        return reductionPct;
    }

    public void setReductionPct(BigDecimal reductionPct) {
        this.reductionPct = reductionPct;
    }

    public BigDecimal getPaidTotal() {
        return paidTotal;
    }

    public void setPaidTotal(BigDecimal paidTotal) {
        this.paidTotal = paidTotal;
    }

    public OrganizationDto getCustomer() {
        return customer;
    }

    public void setCustomer(OrganizationDto customer) {
        this.customer = customer;
    }

    public OrganizationDto getSupplier() {
        return supplier;
    }

    public void setSupplier(OrganizationDto supplier) {
        this.supplier = supplier;
    }

    public List<ContractItemDto> getItems() {
        return items;
    }

    public void setItems(List<ContractItemDto> items) {
        this.items = items;
    }

    public List<ExecutionDto> getExecutions() {
        return executions;
    }

    public void setExecutions(List<ExecutionDto> executions) {
        this.executions = executions;
    }
}
