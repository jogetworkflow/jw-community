package org.joget.directory.model.idp;

public class IdpPluginUnlinkException extends Exception {
    private final boolean displayMessageToUser;

    public IdpPluginUnlinkException(String message) {
        super(message);
        this.displayMessageToUser = false;
    }

    public IdpPluginUnlinkException(String message, boolean displayMessageToUser) {
        super(message);
        this.displayMessageToUser = displayMessageToUser;
    }

    public boolean isDisplayMessageToUser() {
        return displayMessageToUser;
    }
}
