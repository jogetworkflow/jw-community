package org.joget.apps.app.model;

import java.util.Date;
import org.joget.commons.spring.model.Auditable;

public class AuditTrail {

    private String id;
    private String username;
    private AppDefinition appDef;
    private String appId;
    private String appVersion;
    private Date timestamp;
    private String clazz;
    private String method;
    private String message;
    private Class[] paramTypes;
    private Object[] args;
    private Object returnObject;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AppDefinition getAppDef() {
        return appDef;
    }

    public void setAppDef(AppDefinition appDef) {
        this.appDef = appDef;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        if (message == null && paramTypes != null && args != null) {
            message = "";
            boolean auditableObjExists = false;
            
            int i = 0;
            for (Class c : paramTypes) {
                if (args[i] instanceof Auditable) {
                    Auditable obj = (Auditable) args[i];
                    message += c.getName() + "@" + obj.getAuditTrailId() + ";";
                    auditableObjExists = true;
                }

                i++;
            }

            if (!auditableObjExists) {
                //get first argument (usually is ID)
                if ("delete".equals(getMethod()) && args.length > 1 && args[1] instanceof AppDefinition) {
                    AppDefinition appDef = (AppDefinition) args[1];
                    message = "{" + "id=" + args[0].toString() + ", appId=" + appDef.getAppId() + ", appVersion=" + appDef.getVersion() + '}';
                }else if (args[0] != null) {
                    message = args[0].toString();
                }
                
            } else if (message.endsWith(";")) {
                message = message.substring(0, message.length() - 1);
            }
        }
        
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Class[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object getReturnObject() {
        return returnObject;
    }

    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }
}
