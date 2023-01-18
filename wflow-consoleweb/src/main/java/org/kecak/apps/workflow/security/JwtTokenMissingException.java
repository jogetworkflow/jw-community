package org.kecak.apps.workflow.security;

import org.springframework.security.core.AuthenticationException;

public class JwtTokenMissingException extends AuthenticationException {
    public JwtTokenMissingException(String message) {
        super(message);
    }
}
