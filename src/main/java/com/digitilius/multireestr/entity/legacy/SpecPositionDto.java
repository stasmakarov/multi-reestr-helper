package com.digitilius.multireestr.entity.legacy;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.math.BigDecimal;
import java.util.List;

@JmixEntity(name = "mtr_SpecPositionDto")
public class SpecPositionDto {

    @JmixId
    @JmixProperty(mandatory = true)
    private Integer id;

    private Integer lotId;

    private Integer pos;

    @InstanceName
    @JmixProperty(mandatory = true)
    private String name;

    private String okpd2;

    private String okpd4Ktru;

    private String ktru;

    private BigDecimal qty;

    private String unit;

    private Integer okei;

    private BigDecimal price;

    private BigDecimal amount;

    private Boolean foreignBan;

    private Boolean foreignRestriction;

    private Boolean rfPreference;

    private Boolean banImpossible;

    private String banImpossibleReason;

    private String characteristicsText;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private List<KtruCharacteristicDto> characteristics;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLotId() {
        return lotId;
    }

    public void setLotId(Integer lotId) {
        this.lotId = lotId;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
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

    public String getOkpd4Ktru() {
        return okpd4Ktru;
    }

    public void setOkpd4Ktru(String okpd4Ktru) {
        this.okpd4Ktru = okpd4Ktru;
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

    public Integer getOkei() {
        return okei;
    }

    public void setOkei(Integer okei) {
        this.okei = okei;
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

    public Boolean getForeignBan() {
        return foreignBan;
    }

    public void setForeignBan(Boolean foreignBan) {
        this.foreignBan = foreignBan;
    }

    public Boolean getForeignRestriction() {
        return foreignRestriction;
    }

    public void setForeignRestriction(Boolean foreignRestriction) {
        this.foreignRestriction = foreignRestriction;
    }

    public Boolean getRfPreference() {
        return rfPreference;
    }

    public void setRfPreference(Boolean rfPreference) {
        this.rfPreference = rfPreference;
    }

    public Boolean getBanImpossible() {
        return banImpossible;
    }

    public void setBanImpossible(Boolean banImpossible) {
        this.banImpossible = banImpossible;
    }

    public String getBanImpossibleReason() {
        return banImpossibleReason;
    }

    public void setBanImpossibleReason(String banImpossibleReason) {
        this.banImpossibleReason = banImpossibleReason;
    }

    public String getCharacteristicsText() {
        return characteristicsText;
    }

    public void setCharacteristicsText(String characteristicsText) {
        this.characteristicsText = characteristicsText;
    }

    public List<KtruCharacteristicDto> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<KtruCharacteristicDto> characteristics) {
        this.characteristics = characteristics;
    }
}
