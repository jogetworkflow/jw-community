package org.kecak.apps.incomingEmail.model;

public enum EmailProtocol {
    IMAP, IMAPS;
    @Override
    public String toString() {
        return toString().toLowerCase();
    }
}
