package org.joget.apps.app.service;

import org.joget.commons.util.LogUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.joget.workflow.util.WorkflowUtil;

/**
 * AOP interceptor to perform audit trail logging.
 */
public class AuditTrailInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object returnObject = null;
        try {
            returnObject = methodInvocation.proceed();
        } finally {
            String className = methodInvocation.getThis().getClass().getName();
            String methodName = methodInvocation.getMethod().getName();
            Object[] args = methodInvocation.getArguments();
            Class[] param = methodInvocation.getMethod().getParameterTypes();
            
            LogUtil.debug(getClass().getName(), "INTERCEPTED: " + className + "." +methodName);
            
            // add audit trail record
            WorkflowUtil.addAuditTrail(className, methodName, null, param, args, returnObject);
        }
        return returnObject;
    }
}
