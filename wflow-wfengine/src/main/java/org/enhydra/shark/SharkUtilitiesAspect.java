package org.enhydra.shark;

import com.lutris.appserver.server.sql.ObjectId;
import com.lutris.appserver.server.sql.ObjectIdAllocationError;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.SharkConnection;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.XMLInterface;
import org.enhydra.shark.xpdl.XMLInterfaceForJDK13;
import org.enhydra.shark.xpdl.elements.Package;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.shark.migrate.model.MigrateActivity;
import org.joget.workflow.shark.model.SharkCounter;
import org.joget.workflow.shark.model.SharkObjectId;
import org.joget.workflow.shark.model.dao.SharkCounterDao;
import org.joget.workflow.shark.model.dao.SharkObjectIdDao;
import org.joget.workflow.shark.model.dao.WorkflowAssignmentDao;
import org.joget.workflow.util.WorkflowUtil;


/**
 * AOP aspect to intercept calls to SharkUtilities.synchronizeXPDLCache() to
 * customize cache implementation.
 */
@Aspect
public class SharkUtilitiesAspect {

    protected static Map<String, Map<String, String>> currentPkgVersions = new HashMap<String, Map<String, String>>();

    @Pointcut("execution(* org.enhydra.shark.SharkUtilities.getEntity(..))")
    private void getEntityMethod() {
    }

    @Around("org.enhydra.shark.SharkUtilitiesAspect.getEntityMethod()")
    public Object getEntity(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Exception e) {
            XMLComplexElement cel = null;
            Object[] args = pjp.getArgs();
            String actId = (String) args[3];
            if (actId != null) {
                WorkflowAssignmentDao dao = (WorkflowAssignmentDao) WorkflowUtil.getApplicationContext().getBean("workflowAssignmentDao");
                MigrateActivity act = dao.getActivityProcessDefId(actId);

                String pkgId = WorkflowUtil.getProcessDefPackageId(act.getProcessDefId());
                String pkgVer = WorkflowUtil.getProcessDefVersion(act.getProcessDefId());
                String wpId = WorkflowUtil.getProcessDefIdWithoutVersion(act.getProcessDefId());
            
                cel = SharkUtilities.getActivityDefinition((WMSessionHandle) args[0], pkgId, pkgVer, wpId, act.getDefId());
            }
            if (cel == null) {
                throw new Exception("Can't find entity for parameters: mgrName=" + args[1] + ", procId=" + args[2] + ", actId=" + actId);
            }
            
            return SharkUtilities.createBasicEntity(cel);
        }
    }
    
    @Pointcut("execution(* org.enhydra.shark.SharkUtilities.synchronizeXPDLCache(..))")
    private void synchronizeXPDLCacheMethod() {
    }

    @Around("org.enhydra.shark.SharkUtilitiesAspect.synchronizeXPDLCacheMethod()")
    public Object synchronizeXPDLCache(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        WMSessionHandle shandle = (WMSessionHandle) args[0];

        return synchronizeXPDLCache(shandle);
    }

    @Pointcut("execution(* org.enhydra.shark.SharkUtilities.getPackage(..))")
    private void getPackageMethod() {
    }

    @Around("org.enhydra.shark.SharkUtilitiesAspect.getPackageMethod()")
    public Object getPackage(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        WMSessionHandle shandle = (WMSessionHandle) args[0];
        String pkgId = (String) args[1];
        String pkgVer = (String) args[2];

        XMLInterface xmlInterface = SharkEngineManager.getInstance().getXMLInterface();

        Package pkg = null;

        if (pkgVer != null) {
            pkg = xmlInterface.getPackageByIdAndVersion(pkgId, pkgVer);
        } else {
            pkg = xmlInterface.getPackageById(pkgId);
        }

        if (pkg == null) {
            SharkEngineManager.getInstance().getCallbackUtilities().info(shandle, "Package [" + pkgId + "," + pkgVer + "] is not found - synchronizing XPDL caches ...");
            
            SharkUtilities.restorePackage(shandle, xmlInterface, pkgId, pkgVer);
            if (pkgVer != null) {
                pkg = xmlInterface.getPackageByIdAndVersion(pkgId, pkgVer);
            } else {
                pkg = xmlInterface.getPackageById(pkgId);
            }
            if (pkg == null) {
                throw new Exception("Package with Id=" + pkgId + " and version=" + pkgVer + " can't be found!");
            }
        }

        return pkg;
    }
    
    @Pointcut("execution(* org.enhydra.shark.xpdl.XMLInterface.getPackageByIdAndVersion(..))")
    private void getPackageByIdAndVersionMethod() {
    }

    @Around("org.enhydra.shark.SharkUtilitiesAspect.getPackageByIdAndVersionMethod()")
    public Object getPackageByIdAndVersion(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        String pkgId = (String) args[0];
        String pkgVer = (String) args[1];

        XMLInterface xmlInterface = SharkEngineManager.getInstance().getXMLInterface();

        Package pkg = (Package) pjp.proceed();

        if (pkg == null) {
            SharkConnection sConn = Shark.getInstance().getSharkConnection();
            try {
                WorkflowUserManager userManager = (WorkflowUserManager) WorkflowUtil.getApplicationContext().getBean("workflowUserManager");
                String username = userManager.getCurrentUsername();
                WMConnectInfo wmconnInfo = new WMConnectInfo(username, username, "WorkflowManager", "");
                sConn.connect(wmconnInfo);
                SharkUtilities.restorePackage(sConn.getSessionHandle(), xmlInterface, pkgId, pkgVer);
            } finally {
                sConn.disconnect();
            }
            pkg = (Package) pjp.proceed();
            if (pkg == null) {
                throw new Exception("Package with Id=" + pkgId + " and version=" + pkgVer + " can't be found!");
            }
        }

        return pkg;
    }

    protected static synchronized boolean synchronizeXPDLCache(WMSessionHandle shandle) throws Exception {
        boolean hasChanges = false;
        SharkEngineManager.getInstance().getCallbackUtilities().info(shandle, "SharkUtilities -> synchronizing XPDL cache");

        Map<String, String> newCurrentVersions = new HashMap<String, String>();

        XMLInterface xmlInterface = SharkEngineManager.getInstance().getXMLInterface();
        XMLInterface xpdlHandler = new XMLInterfaceForJDK13();
        xpdlHandler.setValidation(false);

        xpdlHandler.synchronizePackages(xmlInterface);
        
        Set<String> enginePkgIds = new HashSet<String>(xpdlHandler.getAllPackageIds());
        Set<String> enginePkgIdsWithVersion = new HashSet<String>();
        Iterator prep = enginePkgIds.iterator();
        while (prep.hasNext()) {
            String epid = (String) prep.next();
            Collection c = xpdlHandler.getAllPackageVersions(epid);
            Iterator prepc = c.iterator();
            while (prepc.hasNext()) {
                String epidWithVersion = createPkgIdWithVersion(epid, (String) prepc.next());
                enginePkgIdsWithVersion.add(epidWithVersion);
            }
        }
        
        Set<String> reposPkgIdsWithVersion = new HashSet<String>(); // latest version of each app version
        WorkflowHelper workflowMapper = (WorkflowHelper) WorkflowUtil.getApplicationContext().getBean("workflowHelper");
        Map<String, String> allPublishedPackageVersions = workflowMapper.getPublishedPackageVersions();
        for (String s : allPublishedPackageVersions.keySet()) {
            reposPkgIdsWithVersion.add(createPkgIdWithVersion(s, allPublishedPackageVersions.get(s)));
        }
        newCurrentVersions = allPublishedPackageVersions;

        Set<String> pkgsToLoad = new HashSet<String>(reposPkgIdsWithVersion);
        pkgsToLoad.removeAll(enginePkgIdsWithVersion);

        Set<String> pkgsToUnload = new HashSet<String>(enginePkgIdsWithVersion); //upload version tht seldom used
        pkgsToUnload.removeAll(reposPkgIdsWithVersion);

        Iterator it = pkgsToLoad.iterator();
        while (it.hasNext()) {
            String pkgIdWithVersion = (String) it.next();
            String pkgId = getPkgId(pkgIdWithVersion);
            String pkgVer = getPkgVersion(pkgIdWithVersion);

            if (SharkUtilities.restorePackage(shandle, xpdlHandler, pkgId, pkgVer) == null) {
                throw new Exception("Problems while restoring packages!");
            }
        }

        if (pkgsToLoad.size() > 0) {
            hasChanges = true;
        }

        xmlInterface.synchronizePackages(xpdlHandler);
        setProfileCurrentPkgVersions(newCurrentVersions);
        xpdlHandler.closeAllPackages();
        xpdlHandler = null;

        return hasChanges;
    }

    protected static final String createPkgIdWithVersion(String pkgId, String pkgVersion) {
        return pkgId + "::" + pkgVersion;
    }

    protected static final String getPkgId(String pkgIdWithVersion) {
        String[] temp = pkgIdWithVersion.split("::");
        return temp[0];
    }

    protected static final String getPkgVersion(String pkgIdWithVersion) {
        String[] temp = pkgIdWithVersion.split("::");
        return temp[1];
    }

    @Pointcut("execution(* org.enhydra.shark.SharkUtilities.getCurrentPkgVersion(..))")
    private void getCurrentPkgVersionMethod() {
    }

    @Around("org.enhydra.shark.SharkUtilitiesAspect.getCurrentPkgVersionMethod()")
    public Object getCurrentPkgVersion(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        WMSessionHandle shandle = (WMSessionHandle) args[0];
        String pkgId = (String) args[1];
        Boolean fromCache = (Boolean) args[2];

        String curVer = null;
        if (fromCache) {
            curVer = (String) getProfileCurrentPkgVersions().get(pkgId);
        }
        if (curVer != null) {
            return curVer;
        }

        curVer = SharkEngineManager.getInstance().getRepositoryPersistenceManager().getCurrentVersion(shandle, pkgId);
        getProfileCurrentPkgVersions().put(pkgId, curVer);

        return curVer;
    }

    protected static Map<String, String> getProfileCurrentPkgVersions() {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        if (currentPkgVersions.containsKey(profile)) {
            return currentPkgVersions.get(profile);
        } else {
            return new HashMap<String, String>();
        }
    }

    protected static void setProfileCurrentPkgVersions(Map<String, String> profileCurrentPkgVersions) {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        currentPkgVersions.put(profile, profileCurrentPkgVersions);
    }
    
    @Pointcut("execution(* org.enhydra.shark.api.client.wfservice.PackageAdministration.closeAllPackagesForId(..))")
    private void closeAllPackagesForIdMethod() {
    }

    @Around("org.enhydra.shark.SharkUtilitiesAspect.closeAllPackagesForIdMethod()")
    public Object closeAllPackagesForId(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        WMSessionHandle shandle = (WMSessionHandle) args[0];
        String pkgId = (String) args[1];
        
        Object object = pjp.proceed();
        
        XMLInterface xmlInterface = SharkEngineManager.getInstance().getXMLInterface();
        xmlInterface.closePackages(pkgId);

        return object;
    }
    
    @Pointcut("execution(* org.enhydra.shark.api.client.wfservice.PackageAdministration.closePackage(..))")
    private void closePackageMethod() {
    }

    @Around("org.enhydra.shark.SharkUtilitiesAspect.closePackageMethod()")
    public Object closePackage(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        WMSessionHandle shandle = (WMSessionHandle) args[0];
        String pkgId = (String) args[1];
        String pkgVer = (String) args[2];
        
        Object object = pjp.proceed();
        
        XMLInterface xmlInterface = SharkEngineManager.getInstance().getXMLInterface();
        xmlInterface.closePackageVersion(pkgId, pkgVer);

        return object;
    }
    
    @Pointcut("execution(* org.enhydra.shark.SharkUtilities.getProcess(..))")
    private void getProcessMethod() {
    }

    @Around("org.enhydra.shark.SharkUtilitiesAspect.getProcessMethod()")
    public Object getProcess(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        WMSessionHandle shandle = (WMSessionHandle) args[0];
        String processId = (String) args[1];
        int mode = (int) args[2];
        
        String key = "processId_" + processId;
        if (mode == 1) {
            // write mode, clear cache and proceed without caching
            WorkflowUtil.writeRequestCache(key, null);
            return pjp.proceed();
        }
        Object result = WorkflowUtil.readRequestCache(key);
        if (result != null) {
            return result;
        } else {
            result = pjp.proceed();
            WorkflowUtil.writeRequestCache(key, result);
        }        
        return result;
    }

    @Pointcut("execution(* org.enhydra.shark.SharkUtilities.getProcessMgr(..))")
    private void getProcessMgrMethod() {
    }

    @Around("org.enhydra.shark.SharkUtilitiesAspect.getProcessMgrMethod()")
    public Object getProcessMgr(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        WMSessionHandle shandle = (WMSessionHandle) args[0];
        String name = (String) args[1];

        String key = "processMgr_" + name;
        Object result = WorkflowUtil.readRequestCache(key);
        if (result != null) {
            return result;
        } else {
            result = pjp.proceed();
            if (result != null) {
                WorkflowUtil.writeRequestCache(key, result);
            }
        }        
        return result;
    }

    @Pointcut("execution(* org.enhydra.shark.SharkUtilities.getActivity(..))")
    private void getActivityMethod() {
    }

    @Around("org.enhydra.shark.SharkUtilitiesAspect.getActivityMethod()")
    public Object getActivity(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        WMSessionHandle shandle = (WMSessionHandle) args[0];
        String processId = (String) args[1];
        String activityId = (String) args[2];
        int mode = (int) args[3];
        
        String key = "activityId_" + activityId;
        if (mode == 1) {
            // write mode, clear cache and proceed without caching
            WorkflowUtil.writeRequestCache(key, null);
            return pjp.proceed();
        }
        Object result = WorkflowUtil.readRequestCache(key);
        if (result != null) {
            return result;
        } else {
            result = pjp.proceed();
            WorkflowUtil.writeRequestCache(key, result);
        }        
        return result;
    }
    
    @Pointcut("execution(* com.lutris.appserver.server.sql.standard.StandardObjectIdAllocator.updateCache(String))")
    private void standardObjectIdAllocatorUpdateCacheMethods() {
        
    }

    /**
     * Override the StandardObjectIdAllocator.updateCache of shark engine to lock table and update
     * to prevent unique constraint exception in clustering environment
     */
    @Around("org.enhydra.shark.SharkUtilitiesAspect.standardObjectIdAllocatorUpdateCacheMethods()")
    public void standardObjectIdAllocatorUpdateCache(ProceedingJoinPoint pjp) throws Throwable {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        Object thisObj = pjp.getThis();
        
        Map next = (Map) FieldUtils.readField(thisObj, "next", true);
        Map max = (Map) FieldUtils.readField(thisObj, "max", true);
        
        ObjectId currentMaxObjectId = (ObjectId) max.get(profile);
        Long currentMax = null;
        if (currentMaxObjectId != null) {
            currentMax = currentMaxObjectId.toBigDecimal().longValue();
        }

        SharkObjectIdDao dao = (SharkObjectIdDao) WorkflowUtil.getApplicationContext().getBean("sharkObjectIdDao");
        SharkObjectId nextoid = dao.getNext(currentMax);

        if (nextoid != null) {
            ObjectId nextObjectid = new ObjectId(nextoid.getNextoid());
            ObjectId maxObjectid = new ObjectId(nextoid.getMaxoid());
            
            LogUtil.info(SharkUtilitiesAspect.class.getName(), "Cache object id range : [" + nextObjectid + " - " + maxObjectid + "]");
            
            max.put(null, maxObjectid);
            next.put(null, nextObjectid);
            max.put(profile, maxObjectid);
            next.put(profile, nextObjectid);
        } else {
            throw new ObjectIdAllocationError("Failed to allocate object id.");
        }
    }
    
    @Pointcut("execution(* org.enhydra.shark.utilities.dods.DODSUtilities.updateCaches(String))")
    private void dodsUtilitiesUpdateCachesMethods() {
        
    }

    /**
     * Override the StandardObjectIdAllocator.updateCache of shark engine to lock table and update
     * to prevent unique constraint exception in clustering environment
     */
    @Around("org.enhydra.shark.SharkUtilitiesAspect.dodsUtilitiesUpdateCachesMethods()")
    public void dodsUtilitiesUpdateCaches(ProceedingJoinPoint pjp) throws Throwable {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        String objectName = (String) pjp.getArgs()[0];
        
        Class dodsUtilities = Class.forName("org.enhydra.shark.utilities.dods.DODSUtilities");
        
        Map counterCachesMax = (Map) FieldUtils.readStaticField(dodsUtilities, "counterCachesMax", true);
        Map counterCachesNext = (Map) FieldUtils.readStaticField(dodsUtilities, "counterCachesNext", true);
        
        BigDecimal currentMaxCounter = (BigDecimal) counterCachesMax.get(profile + "::" + objectName);
        Long currentMax = null;
        if (currentMaxCounter != null) {
            currentMax = currentMaxCounter.longValue();
        }

        SharkCounterDao dao = (SharkCounterDao) WorkflowUtil.getApplicationContext().getBean("sharkCounterDao");
        SharkCounter result = dao.getNext(objectName, currentMax);

        if (result != null) {
            BigDecimal nextCounter = new BigDecimal(result.getNextNumber());
            BigDecimal maxCounter = new BigDecimal(result.getMaxNumber());
            
            LogUtil.info(SharkUtilitiesAspect.class.getName(), "Cache counter range for "+objectName+" : [" + nextCounter + " - " + maxCounter + "]");
            
            counterCachesMax.put(objectName, maxCounter);
            counterCachesNext.put(objectName, nextCounter);
            counterCachesMax.put(profile + "::" + objectName, maxCounter);
            counterCachesNext.put(profile + "::" + objectName, nextCounter);
        } else if (result == null) {
            //counter record not exist
            throw new ObjectIdAllocationError("Failed to allocate counter for "+objectName+".");
        }
    }
}
