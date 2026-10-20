package com.digitilius.multireestr.entity.legacy;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.time.LocalDateTime;

@JmixEntity(name = "mtr_PlanScheduleDto")
public class PlanScheduleDto {

    @JmixId
    @JmixProperty(mandatory = true)
    private Integer id;

    @InstanceName
    @JmixProperty(mandatory = true)
    private Integer finYear;

    private Integer version;

    private LocalDateTime publishDate;

    private LocalDateTime approveDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFinYear() {
        return finYear;
    }

    public void setFinYear(Integer finYear) {
        this.finYear = finYear;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    public LocalDateTime getApproveDate() {
        return approveDate;
    }

    public void setApproveDate(LocalDateTime approveDate) {
        this.approveDate = approveDate;
    }
}
