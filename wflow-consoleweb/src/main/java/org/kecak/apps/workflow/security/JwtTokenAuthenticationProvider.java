package org.kecak.apps.workflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.kecak.apps.app.service.AuthTokenService;
import org.joget.apps.workflow.security.WorkflowUserDetails;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JwtTokenAuthenticationProvider implements AuthenticationProvider, MessageSourceAware {

    private DirectoryManager directoryManager;

    private AuthTokenService authTokenService;

    private WorkflowUserManager workflowUserManager;

    private WorkflowHelper workflowHelper;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        HostManager.initHost();

        JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken)authentication;
        String token = jwtAuthentication.getCredentials().toString();

        try {
            User user = Optional.ofNullable(token)
                    .map(authTokenService::getClaims)
                    .map(Claims::getSubject)
                    .map(directoryManager::getUserByUsername)
                    .orElseThrow(() -> new BadCredentialsException("Invalid token [" + token + "]"));

            String username = user.getUsername();
            workflowUserManager.setCurrentThreadUser(username);

            // add audit trail
            LogUtil.info(getClass().getName(), "Authentication for user " + username + ": " + true);
            workflowHelper.addAuditTrail(this.getClass().getName(), "authenticate", "Authentication for user " + username + ": " + true, new Class[]{String.class}, new Object[]{username}, true);

            // get authorities
            Collection<Role> roles = directoryManager.getUserRoles(username);
            List<GrantedAuthority> gaList = new ArrayList<>();
            if (roles != null && !roles.isEmpty()) {
                for (Role role : roles) {
                    GrantedAuthority ga = new SimpleGrantedAuthority(role.getId());
                    gaList.add(ga);
                }
            }

            // return result
            UserDetails details = new WorkflowUserDetails(user);
            Authentication authenticatedJwtAuthenticationToken = new JwtAuthenticationToken(token, details, gaList);
            return authenticatedJwtAuthenticationToken;
        } catch (ExpiredJwtException e) {
            LogUtil.info(getClass().getName(), "Authentication for token " + token + ": " + false);
            throw new BadCredentialsException(e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setDirectoryManager(DirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    public void setAuthTokenService(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    public WorkflowUserManager getWorkflowUserManager() {
        return workflowUserManager;
    }

    public void setWorkflowUserManager(WorkflowUserManager workflowUserManager) {
        this.workflowUserManager = workflowUserManager;
    }

    public void setWorkflowHelper(WorkflowHelper workflowHelper) {
        this.workflowHelper = workflowHelper;
    }
}
