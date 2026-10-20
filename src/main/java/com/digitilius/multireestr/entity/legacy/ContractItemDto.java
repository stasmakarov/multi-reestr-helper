package com.digitilius.multireestr.entity.legacy;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.math.BigDecimal;

@JmixEntity(name = "mtr_ContractItemDto")
public class ContractItemDto {

    @JmixId
    @JmixProperty(mandatory = true)
    private Integer id;

    @InstanceName
    @JmixProperty(mandatory = true)
    private String name;

    private String okpd2;

    private String ktru;

    private BigDecimal qty;

    private String unit;

    private BigDecimal price;

    private BigDecimal amount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOkpd2() {
        return okpd2;
    }

    public void setOkpd2(String okpd2) {
        this.okpd2 = okpd2;
    }

    public String getKtru() {
        return ktru;
    }

    public void setKtru(String ktru) {
        this.ktru = ktru;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
