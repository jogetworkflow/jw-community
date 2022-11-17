package org.joget.workflow.shark;

import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.logging.StandardLoggingManager;

public class CustomLoggingManager extends StandardLoggingManager {

    @Override
    public void configure(CallbackUtilities cus) throws Exception {
        // do nothing, to fix log appender being overriden in JBoss EAP OpenShift container
    }
    
}
