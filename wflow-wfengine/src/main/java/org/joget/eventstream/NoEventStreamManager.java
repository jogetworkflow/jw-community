package org.joget.eventstream;

import java.util.Map;

/**
 * Dummy implementation for EventStreamManager
 */
public class NoEventStreamManager implements EventStreamManager {

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void createTopic(String topic) {
    }

    @Override
    public void send(String topic, String key, Map data) {
    }

    @Override
    public void listen(String topic, String group, EventStreamMessageListener listener) {
    }

    @Override
    public void stop(String topic, String group) {
    }
    
}
