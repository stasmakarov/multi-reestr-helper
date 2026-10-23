package com.digitilius.multireestr.scan;

import com.digitilius.multireestr.entity.CheckRun;
import com.digitilius.multireestr.entity.CheckRunMode;
import com.digitilius.multireestr.entity.CheckRunStatus;
import com.digitilius.multireestr.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class ScheduledCheckServiceTest {

    @Autowired
    ScheduledCheckService scheduledCheckService;
    @Autowired
    NewViolationCountService newViolationCountService;
    @Autowired
    Scheduler scheduler;
    @Autowired
    DataManager dataManager;

    @Test
    void missingLegacyFinishesFailedWithoutThrowing() {
        CheckRun run = scheduledCheckService.runScheduled();
        assertThat(run.getMode()).isEqualTo(CheckRunMode.SCHEDULED);
        assertThat(run.getStatus()).isEqualTo(CheckRunStatus.FAILED);
        assertThat(run.getErrorMessage()).contains("БД MultiReestr недоступна");
        dataManager.remove(run);
    }

    @Test
    void quartzDoesNotAutoStartAndJobIsRegisteredOnce() throws Exception {
        assertThat(scheduler.isStarted()).isFalse();
        JobKey key = JobKey.jobKey(ScheduledCheckJobRegistrar.JOB_NAME, ScheduledCheckJobRegistrar.JOB_GROUP);
        assertThat(scheduler.checkExists(key)).isTrue();
        assertThat(scheduler.getTriggersOfJob(key)).isNotEmpty();
        String cron = scheduler.getTriggersOfJob(key).get(0).getKey().getName();
        new ScheduledCheckJobRegistrar(scheduler, scanProperties()).register();
        assertThat(scheduler.getTriggersOfJob(key)).hasSize(1);
        assertThat(scheduler.getTriggersOfJob(key).get(0).getKey().getName()).isEqualTo(cron);
    }

    @Test
    void newCountDoesNotThrow() {
        assertThat(newViolationCountService.countNew()).isGreaterThanOrEqualTo(0);
    }

    private static ScanProperties scanProperties() {
        ScanProperties properties = new ScanProperties();
        properties.setCron("0 0 7 * * ?");
        properties.setZone("Europe/Moscow");
        return properties;
    }
}
