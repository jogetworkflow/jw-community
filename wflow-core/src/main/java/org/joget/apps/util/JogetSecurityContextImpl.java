package org.joget.apps.util;

import org.joget.apps.workflow.security.AuthenticationTokenWrapper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;

public class JogetSecurityContextImpl extends SecurityContextImpl {

    @Override
    public void setAuthentication(Authentication authentication) {
        
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && !(authentication instanceof AuthenticationTokenWrapper)) {
            authentication = new AuthenticationTokenWrapper(authentication);
        }
        
        super.setAuthentication(authentication);
    }
}
