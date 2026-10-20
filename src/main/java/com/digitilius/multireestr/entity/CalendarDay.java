package com.digitilius.multireestr.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "MTR_CALENDAR_DAY", indexes = {
        @Index(name = "IDX_MTR_CALENDAR_DAY_UNQ", columnList = "DATE_", unique = true),
        @Index(name = "IDX_MTR_CALENDAR_DAY_YEAR", columnList = "YEAR_")
})
@Entity(name = "mtr_CalendarDay")
public class CalendarDay {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

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
    @Column(name = "DATE_", nullable = false)
    private LocalDate date;

    @Column(name = "YEAR_", nullable = false)
    private Integer year;

    @Column(name = "DAY_TYPE", nullable = false, length = 50)
    private String dayType;

    @Column(name = "SOURCE", nullable = false, length = 50)
    private String source;

    @Column(name = "MARK")
    private String mark;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public DayType getDayType() {
        return dayType == null ? null : DayType.fromId(dayType);
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType == null ? null : dayType.getId();
    }

    public CalendarSource getSource() {
        return source == null ? null : CalendarSource.fromId(source);
    }

    public void setSource(CalendarSource source) {
        this.source = source == null ? null : source.getId();
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }
}
