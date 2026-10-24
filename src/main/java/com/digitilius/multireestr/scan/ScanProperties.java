package com.digitilius.multireestr.scan;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mtr.scan")
public class ScanProperties {

    private int noticeLookbackDays = 365;
    private int maxNotices = 200;
    private String cron = "0 0 6 * * ?";
    private String zone = "Europe/Moscow";

    public int getNoticeLookbackDays() {
        return noticeLookbackDays;
    }

    public void setNoticeLookbackDays(int noticeLookbackDays) {
        this.noticeLookbackDays = noticeLookbackDays;
    }

    public int getMaxNotices() {
        return maxNotices;
    }

    public void setMaxNotices(int maxNotices) {
        this.maxNotices = maxNotices;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}
