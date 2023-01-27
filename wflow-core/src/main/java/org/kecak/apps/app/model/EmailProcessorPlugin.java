package org.kecak.apps.app.model;

import org.joget.plugin.property.model.PropertyEditable;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author aristo
 */
public interface EmailProcessorPlugin extends PropertyEditable {
    String PROPERTY_APP_DEFINITION = "appDefinition";
    String PROPERTY_FROM = "from";
    String PROPERTY_SUBJECT = "subject";
    String PROPERTY_BODY = "body";
    String PROPERTY_EXCHANGE = "exchange";

    /**
     * Filter method, return true to run plugin when new unread mail arrived
     *
     * @param properties
     * @return
     */
    boolean filter(@Nonnull Map<String, Object> properties);

    /**
     * Parse email
     * @param properties
     */
    void parse(Map<String, Object> properties);

    /**
     * When error occurs
     *
     * @param properties
     */
    void onError(Map<String, Object> properties, Exception e);
}
