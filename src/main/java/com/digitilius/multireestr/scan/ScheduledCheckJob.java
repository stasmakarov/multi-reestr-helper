package com.digitilius.multireestr.scan;

import io.jmix.core.security.SystemAuthenticator;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("mtr_ScheduledCheckJob")
@DisallowConcurrentExecution
public class ScheduledCheckJob implements Job {

    @Autowired
    private SystemAuthenticator systemAuthenticator;
    @Autowired
    private ScheduledCheckService scheduledCheckService;

    @Override
    public void execute(JobExecutionContext context) {
        systemAuthenticator.runWithUser("admin", scheduledCheckService::runScheduled);
    }
}
