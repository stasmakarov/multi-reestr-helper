package com.digitilius.multireestr.scan;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

@Component
public class ScheduledCheckJobRegistrar {

    static final String JOB_NAME = "mtr-scheduled-check";
    static final String JOB_GROUP = "mtr";
    static final String TRIGGER_NAME = "mtr-scheduled-check-cron";

    private static final Logger log = LoggerFactory.getLogger(ScheduledCheckJobRegistrar.class);

    private final Scheduler scheduler;
    private final ScanProperties scanProperties;

    public ScheduledCheckJobRegistrar(Scheduler scheduler, ScanProperties scanProperties) {
        this.scheduler = scheduler;
        this.scanProperties = scanProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void register() throws SchedulerException {
        JobDetail job = JobBuilder.newJob(ScheduledCheckJob.class)
                .withIdentity(JOB_NAME, JOB_GROUP)
                .withDescription("Плановая проверка сроков и извещений")
                .storeDurably()
                .build();
        scheduler.addJob(job, true);
        if (scheduler.getTriggersOfJob(JobKey.jobKey(JOB_NAME, JOB_GROUP)).isEmpty()) {
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(TRIGGER_NAME, JOB_GROUP)
                    .forJob(job)
                    .withSchedule(CronScheduleBuilder.cronSchedule(scanProperties.getCron())
                            .inTimeZone(TimeZone.getTimeZone(scanProperties.getZone())))
                    .build();
            scheduler.scheduleJob(trigger);
            log.info("Registered scheduled check job with default cron {}", scanProperties.getCron());
        } else {
            log.info("Scheduled check job already has a trigger; leaving UI cron unchanged");
        }
    }
}
