package org.kecak.apps.scheduler;

import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.kecak.apps.app.model.SchedulerPlugin;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Kecak Exclusive
 *
 * @author aristo
 *
 * Job for Scheduler Plugin
 */
public class SchedulerPluginJob implements Job {
    /**
     * Execute
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final PluginManager pluginManager = (PluginManager) applicationContext.getBean("pluginManager");
        final AppDefinitionDao appDefinitionDao = (AppDefinitionDao) applicationContext.getBean("appDefinitionDao");
        final boolean manualTrigger = context.getTrigger() instanceof SimpleTrigger; // Triggered from "Fire Now"

        Optional.ofNullable(appDefinitionDao.findPublishedApps(null, null, null, null))
                .map(Collection::stream)
                .orElseGet(Stream::empty)

                // set current app definition
                .peek(AppUtil::setCurrentAppDefinition)

                .forEach(appDefinition -> Optional.ofNullable(appDefinition.getPluginDefaultPropertiesList())
                        .map(Collection::stream)
                        .orElse(Stream.empty())
                        .forEach(pluginDefaultProperty -> Stream.of(pluginDefaultProperty)
                            .map(p -> pluginManager.getPlugin(p.getId()))
                            .filter(p -> p instanceof SchedulerPlugin && p instanceof ExtDefaultPlugin)
                            .map(p -> (ExtDefaultPlugin)p)
                            .forEach(plugin -> {
                                Map<String, Object> pluginProperties = PropertyUtil.getPropertiesValueFromJson(pluginDefaultProperty.getPluginProperties());
                                plugin.setProperties(pluginProperties);

                                Map<String, Object> parameterProperties = new HashMap<>(pluginProperties);
                                parameterProperties.put(SchedulerPlugin.PROPERTY_APP_DEFINITION, appDefinition);
                                parameterProperties.put(SchedulerPlugin.PROPERTY_PLUGIN_MANAGER, pluginManager);

                                try {
                                    if (((SchedulerPlugin) plugin).filter(context, parameterProperties) || manualTrigger) {
                                        ((SchedulerPlugin) plugin).jobRun(context, parameterProperties);
                                    } else {
                                        LogUtil.debug(getClass().getName(), "Skipping Scheduler Job Plugin [" + plugin.getName() + "] : Not meeting filter condition");
                                    }
                                } catch (Exception e) {
                                    ((SchedulerPlugin) plugin).onJobError(context, parameterProperties, e);
                                }
                            })));
    }
}
