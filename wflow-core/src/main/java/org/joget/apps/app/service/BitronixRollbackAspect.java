package org.joget.apps.app.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.joget.commons.util.LogUtil;

@Aspect
public class BitronixRollbackAspect {
    private static final String CLASS_NAME = BitronixRollbackAspect.class.getName();
    private static final ThreadLocal<Throwable> ROLLBACK_CAUSE = new ThreadLocal<>();

    @Around("execution(* org.springframework.transaction.interceptor.TransactionAspectSupport.completeTransactionAfterThrowing(..)) && args(txInfo, ex)")
    public Object captureRollbackCause(ProceedingJoinPoint pjp, Object txInfo, Throwable ex) throws Throwable {
        Throwable previous = ROLLBACK_CAUSE.get();
        ROLLBACK_CAUSE.set(ex);
        try {
            return pjp.proceed();
        } finally {
            if (previous != null) {
                ROLLBACK_CAUSE.set(previous);
            } else {
                ROLLBACK_CAUSE.remove();
            }
        }
    }

    @After("execution(* bitronix.tm.BitronixTransaction.setRollbackOnly(..))")
    public void afterSetRollbackOnly() {
        LogUtil.warn(CLASS_NAME, "BitronixTransaction.setRollbackOnly() called by thread: " + Thread.currentThread().getName());
        Throwable cause = ROLLBACK_CAUSE.get();
        if (cause != null) {
            LogUtil.error(CLASS_NAME, cause, "=== Real exception causing BitronixTransaction.setRollbackOnly() ===");
        } else {
            LogUtil.warn(CLASS_NAME, "No Spring rollback exception captured for this BitronixTransaction.setRollbackOnly() call.");
        }

        BitronixRollbackAspectStackTrace e = new BitronixRollbackAspectStackTrace();
        LogUtil.error(CLASS_NAME, e, e.getMessage());
    }

    private static class BitronixRollbackAspectStackTrace extends RuntimeException {
        public BitronixRollbackAspectStackTrace() {
            super("=== BitronixTransaction.setRollbackOnly() Stack Trace ===");
        }
    }
}
