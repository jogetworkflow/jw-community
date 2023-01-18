package org.kecak.apps.scheduler;

import org.kecak.apps.scheduler.dao.SchedulerDetailsDao;
import org.kecak.apps.scheduler.model.SchedulerDetails;
import org.kecak.apps.scheduler.model.TriggerTypes;
import org.joget.plugin.base.PluginManager;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

/**
 * Kecak Exclusive
 */
public class SchedulerManager {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerManager.class);

    private Scheduler schedulerFactory;
    private SchedulerJobListener schedulerJobListener;
    private SchedulerDetailsDao schedulerDetailsDao;
    private PluginManager pluginManager;

    private volatile boolean initialized = false;

    public void initManager() {
        if (initialized) {
            return;
        }
        try {
            schedulerFactory.getListenerManager().addJobListener(schedulerJobListener);
            initialized = true;

        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void startScheduler() throws SchedulerException {
        schedulerFactory.start();
    }

    @SuppressWarnings("unchecked")
    public Date insertNewJob(SchedulerDetails details) throws Exception {

        Date result = null;
        Class<?> jobClass = Class.forName(details.getJobClassName());

        JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) jobClass)
                .withIdentity(details.getJobName(), details.getGroupJobName()).build();

        if (details.getTriggerTypes().getCode().equals(TriggerTypes.CRON.getCode())) {
            CronTriggerImpl trigger = new CronTriggerImpl();
            trigger.setCronExpression(details.getCronExpression());
            trigger.setGroup(details.getGroupTriggerName());
            trigger.setName(details.getTriggerName());
            result = schedulerFactory.scheduleJob(jobDetail, trigger);
        } else {
            @SuppressWarnings("deprecation")
            SimpleTriggerImpl simpleTrigger = new SimpleTriggerImpl(details.getTriggerName(), details.getGroupTriggerName());
            simpleTrigger.setRepeatInterval(details.getInterval());
            simpleTrigger.setRepeatCount(100);
            result = schedulerFactory.scheduleJob(jobDetail, simpleTrigger);
        }

        return result;
    }

    public boolean deleteJob(SchedulerDetails details) {
        boolean result = false;
        try {
            TriggerKey triggerKey = new TriggerKey(details.getTriggerName(), details.getGroupTriggerName());
            schedulerFactory.unscheduleJob(triggerKey);

            JobKey jobKey = new JobKey(details.getJobName(), details.getGroupJobName());
            result = schedulerFactory.deleteJob(jobKey);
            schedulerDetailsDao.delete(details);
        } catch (SchedulerException e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    public void runManualyJob(SchedulerDetails details) throws SchedulerException {
        JobKey jobKey = new JobKey(details.getJobName(), details.getGroupJobName());
        schedulerFactory.triggerJob(jobKey);
    }

    @SuppressWarnings("unchecked")
    public Date modifyJobAndTrigger(SchedulerDetails details)
            throws SchedulerException, ClassNotFoundException, ParseException {
        Date result = null;
        Class<?> jobClass = null;
        JobDetail detail = null;

        TriggerKey triggerKey = new TriggerKey(details.getTriggerName(), details.getGroupTriggerName());
        schedulerFactory.unscheduleJob(triggerKey);

        jobClass = Class.forName(details.getJobClassName());
        detail = JobBuilder.newJob((Class<? extends Job>) jobClass)
                .withIdentity(details.getJobName(), details.getGroupJobName()).build();

        if (details.getTriggerTypes().getCode().equals(TriggerTypes.CRON.getCode())) {
            CronTriggerImpl trigger = new CronTriggerImpl();
            trigger.setCronExpression(details.getCronExpression());
            trigger.setGroup(details.getGroupTriggerName());
            trigger.setName(details.getTriggerName());
            result = schedulerFactory.scheduleJob(detail, trigger);
        } else {
            @SuppressWarnings("deprecation")
            SimpleTriggerImpl simpleTrigger = new SimpleTriggerImpl(details.getTriggerName(), details.getGroupTriggerName());
            simpleTrigger.setRepeatInterval(details.getInterval());
            simpleTrigger.setRepeatCount(100);
            result = schedulerFactory.scheduleJob(detail, simpleTrigger);
        }

        return result;
    }

    public Date changeTriggerValue(SchedulerDetails jobDetails)
            throws SchedulerException, ParseException {
        Date result = null;
        TriggerKey triggerKey = new TriggerKey(jobDetails.getTriggerName(), jobDetails.getGroupTriggerName());

        if (jobDetails.getTriggerTypes().getCode().equals(TriggerTypes.CRON.getCode())) {
            CronTriggerImpl trigger = (CronTriggerImpl) schedulerFactory.getTrigger(triggerKey);
            trigger.setCronExpression(jobDetails.getCronExpression());
            result = schedulerFactory.rescheduleJob(triggerKey, trigger);
        } else {
            SimpleTriggerImpl simpleTrigger = (SimpleTriggerImpl) schedulerFactory.getTrigger(triggerKey);
            simpleTrigger.setRepeatInterval(jobDetails.getInterval());
            result = schedulerFactory.rescheduleJob(triggerKey, simpleTrigger);
        }
        return result;
    }

    public void saveOrUpdateJobDetails(SchedulerDetails jobDetails)
            throws Exception {
        Date nextFireTime = null;
        nextFireTime = insertNewJob(jobDetails);

        if (nextFireTime != null) {
            jobDetails.setNextFireTime(nextFireTime);
            modifyJobAndTrigger(jobDetails);
            schedulerDetailsDao.saveOrUpdate(jobDetails);
        }
    }

    public void updateJobDetails(SchedulerDetails jobDetails) throws Exception {
        modifyJobAndTrigger(jobDetails);
        schedulerDetailsDao.saveOrUpdate(jobDetails);
    }

    public void changeTriggerValueOnDetails(SchedulerDetails jobDetails) throws SchedulerException, ParseException {
        Date nextFireTime = changeTriggerValue(jobDetails);
        if (nextFireTime != null) {
            jobDetails.setNextFireTime(nextFireTime);
            schedulerDetailsDao.saveOrUpdate(jobDetails);
        }
    }

    public void fireNow(SchedulerDetails jobDetails) throws SchedulerException {
        runManualyJob(jobDetails);
    }

    public Set<SchedulerDetails> getAllJobDetails() {
        return null;
    }

    public Scheduler getSchedulerFactory() {
        return schedulerFactory;
    }

    public void setSchedulerFactory(Scheduler schedulerFactory) {
        this.schedulerFactory = schedulerFactory;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public SchedulerJobListener getSchedulerJobListener() {
        return schedulerJobListener;
    }

    public void setSchedulerJobListener(SchedulerJobListener schedulerJobListener) {
        this.schedulerJobListener = schedulerJobListener;
    }

    public SchedulerDetailsDao getSchedulerDetailsDao() {
        return schedulerDetailsDao;
    }

    public void setSchedulerDetailsDao(SchedulerDetailsDao schedulerDetailsDao) {
        this.schedulerDetailsDao = schedulerDetailsDao;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

}
