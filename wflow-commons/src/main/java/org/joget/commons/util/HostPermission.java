package org.joget.commons.util;

import java.security.BasicPermission;

public final class HostPermission extends BasicPermission {
    
    public HostPermission(String name) {
        super(name);
    }
    
}
