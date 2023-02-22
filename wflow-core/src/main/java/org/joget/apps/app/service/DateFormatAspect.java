package org.joget.apps.app.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.directory.dao.UserMetaDataDao;
import org.joget.directory.model.UserMetaData;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.context.i18n.LocaleContextHolder;

@Aspect
public class DateFormatAspect {
    
    @Pointcut("execution(* org.joget.commons.util.TimeZoneUtil.convertToTimeZoneWithLocale(..))")
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
        
        obj = pjp.proceed();
       
        if ("true".equalsIgnoreCase(value)) {
            Object[] args = pjp.getArgs();
            if (args[3] == null || args[3].toString().isEmpty() || args[3] != null && args[3].toString().equals(AppUtil.getAppLocale())) {
                obj = TimeZoneUtil.convertDateDigitsFromLocaleToEnglish((String) obj, LocaleContextHolder.getLocale());
            }
        }
        
        return obj;
    }
    
}
