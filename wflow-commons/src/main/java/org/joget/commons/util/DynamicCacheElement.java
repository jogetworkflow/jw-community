package org.joget.commons.util;

public class DynamicCacheElement {
    private Object value;
    private Long expiry = 20l;
    
    public DynamicCacheElement(Object value) {
        this.value = value;
    }

    public DynamicCacheElement(Object value, Long expiry) {
        this.value = value;
        if (expiry != null && expiry > 0) {
            this.expiry = expiry;
        }
    }

    public Object getValue() {
        return value;
    }
    
    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }
}
