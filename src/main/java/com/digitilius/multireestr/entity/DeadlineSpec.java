package com.digitilius.multireestr.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@JmixEntity(name = "mtr_DeadlineSpec")
@Embeddable
public class DeadlineSpec {

    @Column(name = "ANCHOR", length = 50)
    private String anchor;

    @Column(name = "OFFSET_VALUE")
    private Integer offsetValue;

    @Column(name = "UNIT", length = 50)
    private String unit;

    @Column(name = "START_NEXT_DAY")
    private Boolean startNextDay = true;

    @Column(name = "SHIFT_RULE", length = 50)
    private String shiftRule;

    @Column(name = "TARGET", length = 50)
    private String target;

    @Column(name = "COMPARE_OPERATOR", length = 50)
    private String compareOperator;

    @Column(name = "WARN_BEFORE_DAYS")
    private Integer warnBeforeDays;

    public DeadlineAnchor getAnchor() {
        return anchor == null ? null : DeadlineAnchor.fromId(anchor);
    }

    public void setAnchor(DeadlineAnchor anchor) {
        this.anchor = anchor == null ? null : anchor.getId();
    }

    public Integer getOffsetValue() {
        return offsetValue;
    }

    public void setOffsetValue(Integer offsetValue) {
        this.offsetValue = offsetValue;
    }

    public DayCountUnit getUnit() {
        return unit == null ? null : DayCountUnit.fromId(unit);
    }

    public void setUnit(DayCountUnit unit) {
        this.unit = unit == null ? null : unit.getId();
    }

    public Boolean getStartNextDay() {
        return startNextDay;
    }

    public void setStartNextDay(Boolean startNextDay) {
        this.startNextDay = startNextDay;
    }

    public ShiftRule getShiftRule() {
        return shiftRule == null ? null : ShiftRule.fromId(shiftRule);
    }

    public void setShiftRule(ShiftRule shiftRule) {
        this.shiftRule = shiftRule == null ? null : shiftRule.getId();
    }

    public DeadlineAnchor getTarget() {
        return target == null ? null : DeadlineAnchor.fromId(target);
    }

    public void setTarget(DeadlineAnchor target) {
        this.target = target == null ? null : target.getId();
    }

    public CompareOperator getCompareOperator() {
        return compareOperator == null ? null : CompareOperator.fromId(compareOperator);
    }

    public void setCompareOperator(CompareOperator compareOperator) {
        this.compareOperator = compareOperator == null ? null : compareOperator.getId();
    }

    public Integer getWarnBeforeDays() {
        return warnBeforeDays;
    }

    public void setWarnBeforeDays(Integer warnBeforeDays) {
        this.warnBeforeDays = warnBeforeDays;
    }
}
