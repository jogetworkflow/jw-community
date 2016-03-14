package org.joget.apps.app.service;

import java.lang.reflect.Method;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;

public class SqlFilterAspect implements MethodInterceptor {
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        Object args[] = methodInvocation.getArguments();
        String methodName = method.getName();
        int length = (args != null)?args.length:0;
        
        if (methodName.equals("activityVariable") || methodName.equals("processVariable") || methodName.equals("assignmentVariable")) {
            length--;
        }
        
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                if (args[i] != null && args[i] instanceof String) {
                    if (isInjection((String) args[i])) {
                        HttpServletRequest request= WorkflowUtil.getHttpServletRequest();
                        Exception e = new SQLException("Possible SQLi from IP:" + request.getRemoteAddr() + ". Query string is " + request.getQueryString());
                        LogUtil.error(SqlFilterAspect.class.getName(), e, e.getMessage());
                        
                        throw e;
                    }
                }
            }
        }
        
        return methodInvocation.proceed();
    }
    
    boolean isInjection(String value) {
        return value.contains(" ") || value.contains("(") || value.contains(")") 
                || value.contains(";") || value.contains("/*");
    }

}