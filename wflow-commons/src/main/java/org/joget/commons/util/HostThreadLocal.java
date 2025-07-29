package org.joget.commons.util;

public final class HostThreadLocal<T> extends ThreadLocal<T> {
    
    @Override
    public void set(T value) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new HostPermission("HostThreadLocal"));
        }
        
        super.set(value);
    }
    
    @Override
    public void remove() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new HostPermission("HostThreadLocal"));
        }
        
        super.remove();
    }
}
