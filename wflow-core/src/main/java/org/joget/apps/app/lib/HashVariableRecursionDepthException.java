package org.joget.apps.app.lib;

public class HashVariableRecursionDepthException extends RuntimeException {
    public static final int MAX_RECURSION = 3;
    
    public HashVariableRecursionDepthException(String message) {
        super(message);
    }
}
