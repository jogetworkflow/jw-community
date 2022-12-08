package org.kecak.apps.app.model;

import org.joget.plugin.property.model.PropertyEditable;
import org.quartz.JobExecutionContext;

import javax.annotation.Nonnull;
import java.util.Map;

public interface SchedulerPlugin extends PropertyEditable {
    /**
     * Property for Application Definition
     */
    String PROPERTY_APP_DEFINITION = "appDefinition";

    /**
     * Property for Plugin Manager
     */
    String PROPERTY_PLUGIN_MANAGER = "pluginManager";



    /**
     * Filter method, return true to run plugin during Cron Job
     *
     * @param properties
     * @return
     */
    boolean filter(@Nonnull JobExecutionContext context, @Nonnull Map<String, Object> properties);

    /**
     * Filter method, return true to run plugin during Cron Job
     * Job will only run for PUBLISHED application
     *
     * @param properties
     * @return
     */
    void jobRun(@Nonnull JobExecutionContext context, @Nonnull Map<String, Object> properties);

    /**
     * When error executing job
     *
     * @param context
     * @param properties
     * @param exception
     */
    void onJobError(@Nonnull JobExecutionContext context, @Nonnull Map<String, Object> properties, @Nonnull  Exception exception);
}
