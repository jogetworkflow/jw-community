package org.joget.apps.app.service;

import org.joget.commons.spring.model.Auditable;
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
        String methodName = methodInvocation.getMethod().getName();
        if (methodName.startsWith("get")
                || methodName.startsWith("is")
                || methodName.startsWith("internal")) {
            returnObject = methodInvocation.proceed();
        } else {
            try {
                returnObject = methodInvocation.proceed();
            } finally {
                LogUtil.debug(getClass().getName(), "INTERCEPTED: " + methodName);

                Object[] args = methodInvocation.getArguments();
                Class[] param = methodInvocation.getMethod().getParameterTypes();

                boolean auditableObjExists = false;

                String message = "";
                int i = 0;
                for (Class clazz : param) {
                    if (args[i] instanceof Auditable) {
                        Auditable obj = (Auditable) args[i];
                        message += clazz.getName() + "@" + obj.getAuditTrailId() + ";";
                        auditableObjExists = true;
                    }

                    i++;
                }

                if (!auditableObjExists) {
                    //get first argument (usually is ID)
                    Object obj = args[0];
                    if (obj != null) {
                        message = args[0].toString();
                    } else {
                        message = "";
                    }
                }

                //remove trailing ';'
                if (auditableObjExists && message.length() > 0) {
                    message = message.substring(0, message.length() - 1);
                }

                // add audit trail record
                WorkflowUtil.addAuditTrail(methodInvocation.getThis().getClass().getSimpleName(), methodName, message);
            }
        }
        return returnObject;
    }
}
