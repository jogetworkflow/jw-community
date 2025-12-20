package org.joget.directory.model.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collection;
import java.util.UUID;
import org.joget.commons.util.LogUtil;
import org.joget.directory.dao.UserMetaDataDao;
import org.joget.directory.model.UserMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionInvalidationService {

    public static final String KEY_USER_SECURITY_VERSION = "userSecurityVersion";
    public static final String KEY_CURRENT_SESSION_ID = "currentSessionId";
    public static final String KEY_ACTIVE_SESSION_PREFIX = "active_session:";

    @Autowired
    private UserMetaDataDao userMetaDataDao;

    public void setUserMetaDataDao(UserMetaDataDao userMetaDataDao) {
        this.userMetaDataDao = userMetaDataDao;
    }

    @Transactional
    public void incrementUserSecurityVersion(String username) {
        try {
            UserMetaData data = userMetaDataDao.getUserMetaData(username, KEY_USER_SECURITY_VERSION);
            int version = 0;
            if (data != null) {
                try {
                    version = Integer.parseInt(data.getValue());
                } catch (NumberFormatException e) {
                    // ignore
                }
            } else {
                data = new UserMetaData();
                data.setUsername(username);
                data.setKey(KEY_USER_SECURITY_VERSION);
            }

            version++;
            data.setValue(Integer.toString(version));

            userMetaDataDao.updateUserMetaData(data);
        } catch (Exception e) {
            LogUtil.error(SessionInvalidationService.class.getName(), e,
                    "Error incrementing user security version for " + username);
        }
    }

    @Transactional(readOnly = true)
    public String getUserSecurityVersion(String username) {
        try {
            UserMetaData data = userMetaDataDao.getUserMetaData(username, KEY_USER_SECURITY_VERSION);
            if (data != null) {
                return data.getValue();
            }
        } catch (Exception e) {
            LogUtil.error(SessionInvalidationService.class.getName(), e,
                    "Error retreiving user security version for " + username);
        }
        return null;
    }

    public boolean validateUserSession(String username, HttpServletRequest request) {
        boolean valid = true;

        try {
            HttpSession session = request.getSession();
            
            // 1. Check User Security Version
            String sessionVersion = (String) session
                    .getAttribute(SessionInvalidationService.KEY_USER_SECURITY_VERSION);
            String currentVersion = getUserSecurityVersion(username);

            if (sessionVersion != null && !sessionVersion.equals(currentVersion)) {
                valid = false;
                LogUtil.info(SessionInvalidationService.class.getName(),
                        "Session invalidated for user " + username + ": Security version mismatch (Session: "
                                + sessionVersion + ", DB: " + currentVersion + ")");
            }

            // 2. Check Concurrent Session Control
            if (valid) {
                boolean enableConcurrentSessionControl = "true"
                        .equals(DirectoryUtil.getUserSecurity().getProperties().get("enableConcurrentSessionControl"));
                if (enableConcurrentSessionControl) {
                    String sessionId = (String) session
                            .getAttribute(SessionInvalidationService.KEY_CURRENT_SESSION_ID);
                    if (!isActiveSession(username, sessionId, request)) {
                        valid = false;

                        LogUtil.info(SessionInvalidationService.class.getName(),
                                "Session invalidated for user " + username + ": Concurrent session mismatch");
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(SessionInvalidationService.class.getName(), e, "Error validating session for " + username);
        }

        return valid;
    }

    @Transactional(readOnly = true)
    public boolean isActiveSession(String username, String sessionId, HttpServletRequest request) {
        if (sessionId != null) {
            try {
                UserMetaData data = userMetaDataDao.getUserMetaData(username, KEY_ACTIVE_SESSION_PREFIX + sessionId);
                if (data != null) {
                    return true;
                }
            } catch (Exception e) {
                LogUtil.error(SessionInvalidationService.class.getName(), e,
                        "Error retreiving current session ID for " + username);
            }
        } else {
            //the setting may enabled after login, check if there is other active session.
            Collection<UserMetaData> sessions = userMetaDataDao.getUserMetaDatasByUsernameKeyPrefix(username, KEY_ACTIVE_SESSION_PREFIX);
            if (sessions == null || sessions.isEmpty()) {
                //setup an session now
                setupUserSession(request, username);
                return true;
            }
        }
        return false;
    }

    public void invalidateAllUsers() {
        userMetaDataDao.deleteUserMetaDataByKey(KEY_USER_SECURITY_VERSION);
        userMetaDataDao.deleteUserMetaDataByKeyPrefix(KEY_ACTIVE_SESSION_PREFIX);
    }

    public void removeActiveSessions(String username) {
        userMetaDataDao.deleteUserMetaDatasByUsernameKeyPrefix(username, KEY_ACTIVE_SESSION_PREFIX);
    }

    public void setupUserSession(HttpServletRequest request, String username) {
        try {
            HttpSession session = request.getSession();
            
            // Setup User Security Version
            String version = getUserSecurityVersion(username);
            if (version == null) {
                incrementUserSecurityVersion(username);
                version = getUserSecurityVersion(username);
            }
            session.setAttribute(SessionInvalidationService.KEY_USER_SECURITY_VERSION, version);

            // Track Active Session if force single session is enable
            UserSecurity userSecurity = DirectoryUtil.getUserSecurity();
            if (userSecurity != null) {
                boolean enableConcurrentSessionControl = "true"
                        .equals(userSecurity.getProperties().get("enableConcurrentSessionControl"));
                if (enableConcurrentSessionControl) {
                    String browserInfo = request.getHeader("User-Agent");
                    
                    // Invalidate previous sessions 
                    removeActiveSessions(username);

                    String currentSessionId = UUID.randomUUID().toString();
                    session.setAttribute(SessionInvalidationService.KEY_CURRENT_SESSION_ID, currentSessionId);
                    addActiveSession(username, currentSessionId, browserInfo);
                }
            }
        } catch (Exception e) {
            LogUtil.error(SessionInvalidationService.class.getName(), e,
                    "Error setting up session for user " + username);
        }
    }

    @Transactional
    public void addActiveSession(String username, String sessionId, String browserInfo) {
        try {
            String key = KEY_ACTIVE_SESSION_PREFIX + sessionId;
            UserMetaData data = userMetaDataDao.getUserMetaData(username, key);
            if (data == null) {
                data = new UserMetaData();
                data.setUsername(username);
                data.setKey(key);
            }
            // Store creation time and browser info
            try {
                org.json.JSONObject json = new org.json.JSONObject();
                json.put("creationTime", System.currentTimeMillis());
                if (browserInfo != null) {
                    json.put("browser", browserInfo);
                }
                data.setValue(json.toString());
            } catch (Exception ex) {
                data.setValue(Long.toString(System.currentTimeMillis()));
            }

            userMetaDataDao.updateUserMetaData(data);
        } catch (Exception e) {
            LogUtil.error(SessionInvalidationService.class.getName(), e, "Error adding active session for " + username);
        }
    }

    @Transactional
    public void removeActiveSession(String username, String sessionId) {
        try {
            String key = KEY_ACTIVE_SESSION_PREFIX + sessionId;
            userMetaDataDao.deleteUserMetaData(username, key);
        } catch (Exception e) {
            LogUtil.error(SessionInvalidationService.class.getName(), e,
                    "Error removing active session for " + username);
        }
    }

    @Transactional(readOnly = true)
    public Collection<UserMetaData> getActiveSessions(String username) {
        return userMetaDataDao.getUserMetaDatasByUsernameKeyPrefix(username, KEY_ACTIVE_SESSION_PREFIX);
    }
}
