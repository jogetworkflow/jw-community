package org.kecak.apps.exception;

import org.joget.apps.userview.model.UserviewPermission;

/**
 * @author aristo
 */
public class UnauthorizedException extends Exception {
    private UserviewPermission permission;

    UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(UserviewPermission permission) {
        super("Unauthorized by permission " + permission.getClassName());
    }
}
