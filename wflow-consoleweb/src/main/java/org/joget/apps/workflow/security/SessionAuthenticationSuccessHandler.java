package org.joget.apps.workflow.security;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.joget.commons.util.SetupManager;
import org.joget.directory.model.service.SessionInvalidationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public class SessionAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private SessionInvalidationService sessionInvalidationService;
    private SetupManager setupManager;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String username = authentication.getName();

        if (session != null) {
            sessionInvalidationService.setupUserSession(request, username);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    public void setSessionInvalidationService(SessionInvalidationService sessionInvalidationService) {
        this.sessionInvalidationService = sessionInvalidationService;
    }

    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }
}
