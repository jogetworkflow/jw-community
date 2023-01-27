package org.kecak.apps.exception;

public class MessengerWebhookException extends Exception {
    public MessengerWebhookException(String message) {
        super(message);
    }

    public MessengerWebhookException(Throwable cause) {
        super(cause);
    }

    public MessengerWebhookException(String message, Throwable cause) {
        super(message, cause);
    }
}
