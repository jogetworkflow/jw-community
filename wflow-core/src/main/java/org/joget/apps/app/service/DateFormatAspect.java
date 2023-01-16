package org.joget.apps.app.service;

import java.util.Date;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.directory.dao.UserMetaDataDao;
import org.joget.directory.model.UserMetaData;
import org.joget.workflow.model.service.WorkflowUserManager;

@Aspect
public class DateFormatAspect {
    
    @Pointcut("execution(* org.joget.commons.util.TimeZoneUtil.convertToTimeZone(..))")
    private void convertToTimeZoneMethod() {
    }

    /**
     * Use to inject the locale parameter when even the TimeZoneUtil.convertToTimeZone method is used
     * based on user data "dateFormatUseEnglish"
     * @param pjp
     * @return
     * @throws Throwable 
     */
    @Around("org.joget.apps.app.service.DateFormatAspect.convertToTimeZoneMethod()")
    public Object convertToTimeZone(ProceedingJoinPoint pjp) throws Throwable {
        Object obj = null;
        
        WorkflowUserManager wum = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        String value = "";
        if (!wum.isCurrentUserAnonymous()) {
            value = (String) wum.getCurrentUserTempData("dateFormatUseEnglish");
            if (value == null) {
                UserMetaDataDao umdd = (UserMetaDataDao) AppUtil.getApplicationContext().getBean("userMetaDataDao");
                UserMetaData data = umdd.getUserMetaData(wum.getCurrentUsername(), "dateFormatUseEnglish");
                if (data != null) {
                    value = data.getValue();
                } else {
                    value = "";
                }
                wum.setCurrentUserTempData("dateFormatUseEnglish", value);
            }
        }
        
        if ("true".equalsIgnoreCase(value)) {
            Object[] args = pjp.getArgs();
            obj = TimeZoneUtil.convertToTimeZoneWithLocale((Date) args[0], (String) args[1], (String) args[2], "en");
        } else {
            obj = pjp.proceed();
        }
        
        return obj;
    }
    
}
