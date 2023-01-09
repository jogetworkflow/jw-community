package org.joget.apps.app.model;

public class ImportAppException extends RuntimeException {
    
    public ImportAppException(String msg) {
        super(msg);
    }
    
    public ImportAppException(String msg, Throwable e) {
        super(msg, e);
    }
}
