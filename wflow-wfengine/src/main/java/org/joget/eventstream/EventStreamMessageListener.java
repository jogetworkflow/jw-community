package org.joget.eventstream;

import java.util.Map;

/**
 * Listener for an event stream messages.
 */
public interface EventStreamMessageListener {

    /**
     * Called when a new event stream message is received for a topic.
     * @param topic
     * @param key
     * @param data 
     */
    public void onMessage(String topic, String key, Map data);
    
}
