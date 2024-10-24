package org.joget.apps.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.workflow.security.AuthenticationTokenWrapper;
import org.joget.apps.workflow.security.WorkflowUserDetails;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManagerProxyImpl;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;

@Service
public final class UserAuthenticationService {
    public static final int PASSWORD_MAX_LENGTH = 512;
    private final WorkflowUserManager workflowUserManager;
    private final DirectoryManagerProxyImpl directoryManager;

    @Autowired
    public UserAuthenticationService(WorkflowUserManager workflowUserManager, DirectoryManagerProxyImpl directoryManager) {
        this.workflowUserManager = workflowUserManager;
        this.directoryManager = directoryManager;
    }

    /**
     * Method to log in user programmatically
     *
     * @param user the user to be logged in
     * @return true if successfully logged in; false otherwise
     * @see <a href="https://dev.joget.org/community/display/DX8/Single+Sign+On+-+SSO#SingleSignOnSSO-LoginanUserProgrammatically">
     * Joget KB: Single Sign On - SSO
     * </a>
     */
    public boolean loginUser(User user) {
        String username = user.getUsername();
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request == null) {
            LogUtil.warn(getClass().getName(), "Unable to log in user " + username + " because request is null.");
            return false;
        }
        try {
            // Change session ID to avoid session fixation vulnerability
            HttpSession oldSession = request.getSession(false);
            String oldSessionId = oldSession == null ? "" : oldSession.getId();
            String newSessionId = request.changeSessionId();
            if (oldSessionId.equals(newSessionId)) {
                LogUtil.warn(getClass().getName(), "Unable to change session ID, cannot log in user.");
                return false;
            }

            // Generate an authentication token
            WorkflowUserDetails userDetail = new WorkflowUserDetails(user);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, userDetail.getPassword(), userDetail.getAuthorities());
            auth.setDetails(userDetail);

            // Login the user. First set SecurityContext
            // see: https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html#servlet-authentication-securitycontextholder
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            HttpSession session = request.getSession(false);
            if (session == null) {
                LogUtil.debug(getClass().getName(), "Unable to save security context as session is null");
                return false;
            }

            /*
             * Add SecurityContext to session. Required step, otherwise user will not be logged in.
             *
             * Since Spring Security 6 / DX 9
             * See source in:
             *   org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.successfulAuthentication
             *   Line: "this.securityContextRepository.saveContext(context, request, response);"
             */
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            workflowUserManager.setCurrentThreadUser(user);

            // Add audit trail
            loginAuditTrailLogging(true, username, request);

        } catch (Exception e) {
            LogUtil.error(UserAuthenticationService.class.getName(), e, "Failed to login");
            return false;
        }
        return true;
    }

    public Authentication loginUser(Authentication authentication) {
        // Determine username
        String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
        String password = authentication.getCredentials().toString();
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        // check credentials
        boolean validLogin = false;
        try {
            // Prevent DoS attacks by refusing to hash large passwords
            if (password.length() <= PASSWORD_MAX_LENGTH) {
                // authenticate
                validLogin = directoryManager.authenticate(username, password);
            }
        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
        if (!validLogin) {
            loginAuditTrailLogging(false, username, request);
            return null;
        }

        // return result
        User user = directoryManager.getUserByUsername(username);
        UserDetails details = new WorkflowUserDetails(user);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password, details.getAuthorities());
        token.setDetails(details);
        workflowUserManager.setCurrentThreadUser(user);

        // add audit trail
        loginAuditTrailLogging(true, username, request);
        return new AuthenticationTokenWrapper(token);
    }

    public boolean logoutUser(HttpServletRequest request, HttpServletResponse response) {
        try {
            // generate new session to avoid session fixation vulnerability
            HttpSession session = request.getSession(false);
            if (session != null) {
                SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
                try {
                    session.invalidate();
                } catch (IllegalStateException ignored) {
                    // session is already invalidated
                }

                // create new session
                HttpSession newSession = request.getSession(true);
                if (newSession == null) {
                    throw new IllegalStateException("New session is null");
                }
                if (newSession.equals(session)) {
                    throw new IllegalStateException("New session is same as old session");
                }
                if (savedRequest != null) {
                    new HttpSessionRequestCache().saveRequest(request, response);
                }

                // logout from Joget and Spring services
                workflowUserManager.setCurrentThreadUser(WorkflowUserManager.ROLE_ANONYMOUS);
                new CookieClearingLogoutHandler(AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY).logout(request, response, null);
                new SecurityContextLogoutHandler().logout(request, response, null);
            }
        } catch (Exception e) {
            LogUtil.error(UserAuthenticationService.class.getName(), e, "Failed to logout");
            return false;
        }
        return true;
    }

    public boolean loginUser(String username) {
        User user = directoryManager.getUserByUsername(username);
        if (user == null) {
            throw new NullPointerException("User " + username + "not found. Unable to login.");
        }
        return loginUser(user);
    }

    private void loginAuditTrailLogging(boolean loginSuccess, String username, HttpServletRequest request) {
        String ip = "null";
        if (request != null) {
            ip = AppUtil.getClientIp(request);
        }
        LogUtil.info(getClass().getName(), "Authentication for user " + username + " ("+ip+") : " + loginSuccess);
        WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
        workflowHelper.addAuditTrail(this.getClass().getName(), "authenticate", "Authentication for user " + username + " ("+ip+") : " + loginSuccess, new Class[]{username.getClass()}, new Object[]{username}, loginSuccess);
    }
}
