package org.kecak.apps.app.model;

import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.ExtDefaultPlugin;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Default Email Processor Plugin
 *
 * Default implementation template for {@link EmailProcessorPlugin}
 */
public abstract class DefaultEmailProcessorPlugin extends ExtDefaultPlugin implements EmailProcessorPlugin {
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

    /**
     * Overwrite this method to implement custom filter during incoming email
     * @param properties
     * @return
     */
    @Override
    public boolean filter(@Nonnull Map<String, Object> properties) {
        return true;
    }

    /**
     * Parse current email content
     * @param properties
     */
    @Override
    public void parse(Map<String, Object> properties) {
        parse(properties.get(PROPERTY_FROM).toString(), properties.get(PROPERTY_SUBJECT).toString(), properties.get(PROPERTY_BODY).toString(), properties);
    }

    @Override
    public void onError(Map<String, Object> properties, Exception e) {
        LogUtil.error(getClassName(), e, e.getMessage());
    }

    public abstract void parse(String from, String subject, String body, Map<String, Object> properties);
}
