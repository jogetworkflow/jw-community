package org.kecak.apps.exception;

public class ApiException extends Exception {
    private int httpErrorCode;

    public ApiException(int httpErrorCode, String message) {
        super(message);
        this.httpErrorCode = httpErrorCode;
    }

    public ApiException(int httpErrorCode, Throwable throwable) {
        super(throwable.getMessage(), throwable);
        this.httpErrorCode = httpErrorCode;
    }

    public int getErrorCode() {
        return httpErrorCode;
    }
}
