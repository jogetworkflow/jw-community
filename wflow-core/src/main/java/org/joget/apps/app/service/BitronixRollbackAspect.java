package org.joget.apps.app.service;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.joget.commons.util.LogUtil;

@Aspect
public class BitronixRollbackAspect {
    private static final String CLASS_NAME = BitronixRollbackAspect.class.getName();

    @After("execution(* bitronix.tm.BitronixTransaction.setRollbackOnly(..))")
    public void afterSetRollbackOnly(JoinPoint jp) {
        LogUtil.warn(CLASS_NAME, "BitronixTransaction.setRollbackOnly() called by thread: " + Thread.currentThread().getName());

        if (LogUtil.isDebugEnabled(CLASS_NAME)) {
            BitronixRollbackAspectStackTrace e = new BitronixRollbackAspectStackTrace();
            LogUtil.error(CLASS_NAME, e, e.getMessage());
        }
    }

    private static class BitronixRollbackAspectStackTrace extends RuntimeException {
        public BitronixRollbackAspectStackTrace() {
            super("=== BitronixTransaction.setRollbackOnly() Stack Trace ===");
        }
    }
}
