package org.kecak.apps.app.model;

import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.quartz.JobExecutionContext;

import javax.annotation.Nonnull;
import java.util.Calendar;
import java.util.Map;

/**
 * @author aristo
 *
 * Default Implementation template for {@link SchedulerPlugin}
 */
public abstract class DefaultSchedulerPlugin extends ExtDefaultPlugin implements SchedulerPlugin {
    /**
     * Return this method in {@link DefaultSchedulerPlugin#filter(JobExecutionContext, Map)} to run this
     * plugin once every 30 minutes
     *
     * @param context
     * @return
     */
    final protected boolean everyHalfAnHour(@Nonnull JobExecutionContext context) {

        Calendar c = Calendar.getInstance();
        c.setTime(context.getScheduledFireTime());

        return c.get(Calendar.MINUTE) == 0 || c.get(Calendar.MINUTE) == 30;
    }

    /**
     * Return this method in {@link DefaultSchedulerPlugin#filter(JobExecutionContext, Map)} to run this
     * plugin once every hour
     *
     * @param context
     * @return
     */
    final protected boolean everyHour(@Nonnull JobExecutionContext context) {
        Calendar c = Calendar.getInstance();
        c.setTime(context.getScheduledFireTime());

        return c.get(Calendar.MINUTE) == 0;
    }

    /**
     * Return this method in {@link DefaultSchedulerPlugin#filter(JobExecutionContext, Map)} to run this
     * plugin once a day at 00:00 AM
     *
     * @param context
     * @return
     */
    final protected boolean everyDay(@Nonnull JobExecutionContext context) {

        Calendar c = Calendar.getInstance();
        c.setTime(context.getScheduledFireTime());

        return c.get(Calendar.HOUR_OF_DAY) == 0 && c.get(Calendar.MINUTE) == 0;
    }

    /**
     * Overwrite this method to implement custom filter during scheduler execution
     * For default timing implementation and example please refer to
     * {@link DefaultSchedulerPlugin#everyHalfAnHour(JobExecutionContext)} or
     * {@link DefaultSchedulerPlugin#everyHour(JobExecutionContext)} or
     * {@link DefaultSchedulerPlugin#everyDay(JobExecutionContext)}
     *
     * @param context
     * @return
     */
    @Override
    public boolean filter(@Nonnull JobExecutionContext context, @Nonnull Map<String, Object> properties) {
        return true;
    }

    @Override
    public void onJobError(@Nonnull JobExecutionContext context, @Nonnull Map<String, Object> properties, @Nonnull Exception e) {
        LogUtil.error(getClassName(), e, e.getMessage());
    }

    /**
     * NOT USED !!!! For future use !!!
     *
     * @param properties
     * @return NaN
     */
    @Override
    public final Object execute(Map properties) {
        return null;
    }
}
