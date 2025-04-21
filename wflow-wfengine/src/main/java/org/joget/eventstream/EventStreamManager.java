package org.joget.eventstream;

import java.util.Collection;
import java.util.Map;
import org.joget.commons.util.SecurityUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;

/**
 * Generic interface to interact with event stream platforms e.g. Apache Kafka.
 */
public interface EventStreamManager {

    public static final String SYSTEM_PROPERTY_EVENT_STREAM_MANAGER = "wflow.eventStreamManager";
    
    /**
     * Convenience method to retrieve the currently configured event stream manager.
     * @return
     */
    public static EventStreamManager getEventStreamManager() {
        EventStreamManager eventStreamManager = null;
        PluginManager pluginManager = (PluginManager)SecurityUtil.getApplicationContext().getBean("pluginManager");
        String eventStreamManagerClassName = System.getProperty(SYSTEM_PROPERTY_EVENT_STREAM_MANAGER);
        if (eventStreamManagerClassName != null && !eventStreamManagerClassName.trim().isEmpty()) {
            // return defined plugin
            eventStreamManager = (EventStreamManager)pluginManager.getPlugin(eventStreamManagerClassName);
        } else {
            // return first plugin in list
            Collection<Plugin> plugins = pluginManager.list(EventStreamManager.class);
            if (plugins != null && !plugins.isEmpty()) {
                eventStreamManager = (EventStreamManager)plugins.iterator().next();
            }
        }
        if (eventStreamManager == null) {
            eventStreamManager = new NoEventStreamManager();
        } else if (!eventStreamManager.isEnabled()) {
            eventStreamManager.init();
        }
        return eventStreamManager;
    }

    /**
     * Called at startup for initialization.
     */
    void init();
    
    /**
     * Called at shutdown.
     */
    void shutdown();
    
    /**
     * Checks to see whether the event stream platform is enabled.
     * @return true if the event stream platform is enabled.
     */
    boolean isEnabled();

    /**
     * Create an event stream topic.
     * @param topic 
     */
    void createTopic(String topic);

    /**
     * Send a Map to an event stream topic.
     * @param topic
     * @param key
     * @param data 
     */
    void send(String topic, String key, Map data);
    
    /**
     * Listen for event stream messages, to be handled by the EventStreamMessageListener.
     * @param topic
     * @param group
     * @param listener 
     */
    void listen(String topic, String group, EventStreamMessageListener listener);
    
    /**
     * Stop listening for event stream messages for a topic and group.
     * @param topic
     * @param group 
     */
    void stop(String topic, String group);
    
}
