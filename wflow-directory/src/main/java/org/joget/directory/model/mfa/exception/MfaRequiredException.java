package org.joget.directory.model.mfa.exception;

public class MfaRequiredException extends RuntimeException {
    public MfaRequiredException(String message) {
        super(message);
    }
}
