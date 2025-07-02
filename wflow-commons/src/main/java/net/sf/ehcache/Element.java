package net.sf.ehcache;

import java.io.Serializable;
import java.util.Date;
import javax.cache.CacheException;

public class Element implements Serializable, Cloneable {
    
    private String key;
    private Object objectValue;
    private long creationTime;
    
    public Element(Serializable key, Serializable objectValue) {
        this.key = (String) key;
        this.objectValue = (Object) objectValue;
        this.creationTime = (new Date()).getTime();
    } 
    
    public Element(String key, Object objectValue, long creationTime) {
        this.key = key;
        this.objectValue = objectValue;
        this.creationTime = creationTime;
    } 

    public final String getKey() {
        return key;
    }
    
    public final Object getObjectKey() {
        return key;
    }
    
    public final Object getObjectValue() {
        return objectValue;
    }
    
    public final Serializable getValue() throws CacheException {
        try {
            return (Serializable) getObjectValue();
        } catch (ClassCastException e) {
            throw new CacheException("The value " + getObjectValue() + " for key " + getObjectKey() +
                    " is not Serializable. Consider using Element.getObjectValue()");
        }
    }
    
    public final long getCreationTime() {
        return creationTime;
    }
    
    public void setTimeToLive(final int timeToLiveSeconds) {
        //ignore
    }

    public void setTimeToIdle(final int timeToIdleSeconds) {
        //ignore
    }
}
