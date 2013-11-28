package org.joget.commons.util;

import java.util.List;


public class ThreadSessionUtil {
    private static List<OpenSessionInThread> sessions;

    public void setSessions(List sessions) {
        ThreadSessionUtil.sessions = sessions;
    }
    
    public static void initSession() {
        if (sessions != null && !sessions.isEmpty()) {
            for (OpenSessionInThread s : sessions) {
                s.sessionStart();
            }
        }
    }
    
    public static void closeSession() {
        if (sessions != null && !sessions.isEmpty()) {
            for (OpenSessionInThread s : sessions) {
                s.sessionStop();
            }
        }
    }
}
