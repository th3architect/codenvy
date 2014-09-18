/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.im;

import com.codenvy.im.restlet.InstallationManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import java.io.IOException;

/**
 * Checks and downloads updates by schedule.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class UpdateManager {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateManager.class);

    private final String              updateSchedule;
    private final InstallationManager manager;

    private Scheduler scheduler;

    @Inject
    public UpdateManager(@Named("installation-manager.daemon.schedule") String updateSchedule,
                         InstallationManager manager) throws IOException {
        this.updateSchedule = updateSchedule;
        this.manager = manager;
    }

    @PostConstruct
    public void init() throws SchedulerException {
        scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();

        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(IMJob.class.getName()));
        jobDetail.setJobClass(IMJob.class);
        jobDetail.setDurability(true);

        scheduler.scheduleJob(jobDetail, TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(updateSchedule)).build());

    }

    @PreDestroy
    public void destroy() throws SchedulerException {
        if (scheduler != null) {
            scheduler.shutdown(true);
        }
    }

    /**
     * Job to check and download updates.
     */
    public class IMJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            // do nothing yet
        }
    }
}
