package org.joget.apps.workflow.security;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.util.UserAuthenticationService;
import org.joget.commons.util.HostManager;
import org.joget.directory.model.service.DirectoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;

public class WorkflowAuthenticationProvider implements AuthenticationProvider, MessageSourceAware {
    
    transient
    @Autowired
    @Qualifier("main")
    private DirectoryManager directoryManager;
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    public DirectoryManager getDirectoryManager() {
        return directoryManager;
    }

    public void setDirectoryManager(DirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // reset profile and set hostname
        HostManager.initHost();

        Authentication authToken = ((UserAuthenticationService) AppUtil.getApplicationContext().getBean("userAuthenticationService")).loginUser(authentication);
        if (authToken == null) {
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        return authToken;
    }

    public boolean supports(Class authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }
}
