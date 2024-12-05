package net.sf.ehcache;

import java.io.Serializable;

public class Element {
    
    private String key;
    private Object objectValue;
    
    public Element(Serializable key, Serializable objectValue) {
        this.key = (String) key;
        this.objectValue = (Object) objectValue;
    } 

    public String getKey() {
        return key;
    }
    
    public Object getObjectValue() {
        return objectValue;
    }
    
    public void setTimeToLive(final int timeToLiveSeconds) {
        //ignore
    }

    public void setTimeToIdle(final int timeToIdleSeconds) {
        //ignore
    }
}
