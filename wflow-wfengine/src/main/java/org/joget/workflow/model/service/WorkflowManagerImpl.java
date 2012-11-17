package org.joget.workflow.model.service;

import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.workflow.model.*;
import com.lutris.dods.builder.generator.query.DataObjectException;
import com.lutris.dods.builder.generator.query.NonUniqueQueryException;
import com.lutris.dods.builder.generator.query.QueryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.enhydra.dods.jts.LocalContextFactory;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttributeIterator;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmodel.WfActivity;
import org.enhydra.shark.api.client.wfmodel.WfActivityIterator;
import org.enhydra.shark.api.client.wfmodel.WfAssignment;
import org.enhydra.shark.api.client.wfmodel.WfProcess;
import org.enhydra.shark.api.client.wfmodel.WfProcessIterator;
import org.enhydra.shark.api.client.wfmodel.WfProcessMgr;
import org.enhydra.shark.api.client.wfmodel.WfRequester;
import org.enhydra.shark.api.client.wfmodel.WfResource;
import org.enhydra.shark.api.client.wfservice.AdminMisc;
import org.enhydra.shark.api.client.wfservice.ExecutionAdministration;
import org.enhydra.shark.api.client.wfservice.PackageAdministration;
import org.enhydra.shark.api.client.wfservice.SharkConnection;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.client.wfservice.WMEntityIterator;
import org.enhydra.shark.api.client.wfservice.WfProcessMgrIterator;
import org.enhydra.shark.api.client.wfservice.XPDLBrowser;
import org.enhydra.shark.api.common.ActivityFilterBuilder;
import org.enhydra.shark.api.common.ProcessFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.utilities.MiscUtilities;
import org.enhydra.shark.utilities.WMEntityUtilities;

import org.joget.workflow.shark.JSPClientUtilities;
import org.joget.workflow.util.WorkflowUtil;
import com.lutris.dods.builder.generator.query.QueryBuilder;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import javax.transaction.TransactionManager;
import org.apache.commons.collections.SequencedHashMap;

import org.enhydra.shark.CustomWfActivityWrapper;
import org.enhydra.shark.CustomWfResourceImpl;
import org.enhydra.shark.api.client.wfmodel.WfAssignmentIterator;
import org.enhydra.shark.api.common.AssignmentFilterBuilder;
import org.enhydra.shark.instancepersistence.data.AssignmentQuery;
import org.enhydra.shark.instancepersistence.data.ProcessQuery;
import org.enhydra.shark.instancepersistence.data.ProcessStateDO;
import org.enhydra.shark.instancepersistence.data.ProcessStateQuery;
import org.enhydra.shark.xpdl.XMLUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.PagedList;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.dao.WorkflowProcessLinkDao;
import org.joget.workflow.util.DeadlineThreadManager;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class WorkflowManagerImpl implements WorkflowManager {

    static boolean initialized = false;
    private WorkflowUserManager userManager;
    private SetupManager setupManager;
    private DataSource dataSource;
    private JtaTransactionManager transactionManager;
    protected TransactionTemplate transactionTemplate;
    private WorkflowProcessLinkDao workflowProcessLinkDao;
    private Map processStateMap;
    private String previousProfile;

    /*--- Spring bean getters and setters ---*/
    public WorkflowUserManager getWorkflowUserManager() {
        return userManager;
    }

    public void setWorkflowUserManager(WorkflowUserManager userManager) {
        this.userManager = userManager;
    }

    public SetupManager getSetupManager() {
        return setupManager;
    }

    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
        DeadlineThreadManager.initThreads(this);
    }

    public WorkflowProcessLinkDao getWorkflowProcessLinkDao() {
        return workflowProcessLinkDao;
    }

    public void setWorkflowProcessLinkDao(WorkflowProcessLinkDao workflowProcessLinkDao) {
        this.workflowProcessLinkDao = workflowProcessLinkDao;
    }

    /*--- Constructors and initialization methods ---*/
    public WorkflowManagerImpl() {
        setPath("/");
    }

    public WorkflowManagerImpl(DataSource dataSource, JtaTransactionManager transactionManager) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        setPath("/");
    }

    /**
     * Initializes Shark based on a path to the Shark.conf file.
     * @param path
     */
    private void setPath(String path) {

        if (!initialized) {
            try {
                JSPClientUtilities.initProperties(path);
                InitialContext ic = null;
                try {
                    // try to use InitialContext provided by app server
                    ic = new InitialContext();
                    ic.rebind("wflowInitialContextTest", "InitialContext test");
                    ic.unbind("wflowInitialContextTest");
                } catch (NamingException e) {
                    // create custom InitialContext
                    LocalContextFactory.setup("sharkdb");
                }

                if (this.dataSource != null) {
                    // set Spring datasource, hardcoded to Shark's JNDI binding
                    ic = new InitialContext();
                    String jndiName = "jwdb";
                    try {
                        ic.rebind(jndiName, this.dataSource);
                    } catch(Exception ne) {
                        // workaround for Websphere as it does not allow non-serializable object binding, so bind to java:comp/
                        try {
                            jndiName = "java:comp/jwdb";
                            ic.rebind(jndiName, this.dataSource);
                        } catch(Exception nee) {
                            jndiName = "jwdb";
                        }
                    }
                    // set shark datasource name
                    JSPClientUtilities.setProperty("DatabaseManager.DB.sharkdb.Connection.DataSourceName", jndiName);
                    LogUtil.info(getClass().getName(), "Datasource bound to " + jndiName);
                }

                if (this.transactionManager != null) {
                    // set Spring tx manager, hardcoded to Shark's JNDI binding
                    TransactionManager tm = this.transactionManager.getTransactionManager();
                    if (tm != null) {
                        ic.rebind("javax.transaction.TransactionManager", tm);
                    }
                }

                // configure shark
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {

                    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                        try {
                            JSPClientUtilities.init();
                        } catch (Exception ex) {
                            LogUtil.error(getClass().getName(), ex, "Error initializing Shark");
                        }
                    }
                });

                initialized = true;
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, "");
            }
        }
    }

    /*--- Process definition information methods ---*/
    /**
     * Checks to see whether or not package exists.
     * @param packageId
     * @return true if the package exists, false otherwise.
     */
    public Boolean isPackageIdExist(String packageId) {
        SharkConnection sc = null;
        try {
            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);
            String[] openedPackageIds = pa.getOpenedPackageIds(sessionHandle);

            for (int i = 0; i < openedPackageIds.length; i++) {
                if (packageId.equals(openedPackageIds[i])) {
                    return Boolean.TRUE;
                }
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        return Boolean.FALSE;
    }

    /**
     * Returns a list of packages currently in the system.
     * @return
     */
    public Collection<WorkflowPackage> getPackageList() {
        SharkConnection sc = null;
        Collection<WorkflowPackage> packageList = new ArrayList<WorkflowPackage>();
        try {
            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);
            String[] openedPackageIds = pa.getOpenedPackageIds(sessionHandle);

            Map<String, WorkflowPackage> sortedPackages = new TreeMap<String, WorkflowPackage>();
            for (int i = 0; i < openedPackageIds.length; i++) {
                String packageVersion = pa.getCurrentPackageVersion(sessionHandle, openedPackageIds[i]);
                WMEntity entity = pa.getPackageEntity(sessionHandle, openedPackageIds[i], packageVersion);
                String packageName = entity.getName();

                WorkflowPackage wflowPackage = new WorkflowPackage();
                wflowPackage.setPackageId(openedPackageIds[i]);
                wflowPackage.setPackageName(packageName);
                sortedPackages.put(packageName, wflowPackage);
            }
            packageList = sortedPackages.values();
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        return packageList;
    }

    /**
     * Returns the XPDL content for a package version.
     * @param packageId
     * @param version
     * @return
     */
    public byte[] getPackageContent(String packageId, String version) {

        SharkConnection sc = null;
        byte[] data = null;

        try {

            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);
            data = pa.getPackageContent(sessionHandle, packageId, version);


        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        return data;
    }

    /**
     * Retrieve a specific workflow package.
     * @param packageId
     * @param version
     * @return
     */
    public WorkflowPackage getPackage(String packageId, String version) {
        SharkConnection sc = null;
        WorkflowPackage workflowPackage = null;
        try {
            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);
            WMEntity entity = pa.getPackageEntity(sessionHandle, packageId, version);
            workflowPackage = new WorkflowPackage();
            workflowPackage.setPackageId(packageId);
            workflowPackage.setPackageName(entity.getName());
            workflowPackage.setVersion(version);

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return workflowPackage;
    }

    /**
     * Returns the latest package version for the given package ID
     * @param packageId
     * @return null if the package is not available.
     */
    public String getCurrentPackageVersion(String packageId) {

        SharkConnection sc = null;
        String packageVersion = null;
        try {
            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);
            packageVersion = pa.getCurrentPackageVersion(sessionHandle, packageId);

        } catch (Exception ex) {
            //comment out the log because of exception will always throw when new package deploy
            //LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return packageVersion;
    }

    /**
     * Returns a list of process definitions.
     * @param packageId Optional, to show only processes with the specified package ID
     * @return
     */
    public Collection<WorkflowProcess> getProcessList(String packageId) {
        return getProcessList(packageId, null);
    }

    /**
     * Returns a list of process definitions.
     * @param packageId Optional, to show only processes with the specified package ID
     * @param version Optional, to show only for the specified version
     * @return
     */
    public Collection<WorkflowProcess> getProcessList(String packageId, String version) {
        SharkConnection sc = null;
        Collection<WorkflowProcess> processList = new ArrayList<WorkflowProcess>();
        Map<String, WorkflowProcess> processMap = new TreeMap<String, WorkflowProcess>();
        try {
            sc = connect();

            WfProcessMgrIterator pmi = sc.get_iterator_processmgr();

            //filter by package Id
            if (packageId != null && packageId.trim().length() > 0) {
                String sql = " /*sql PackageId='" + packageId + "'";
                if (version != null && version.trim().length() > 0) {
                    sql += " AND ProcessDefinitionVersion='" + version + "'";
                }
                sql += " sql*/";
                pmi.set_query_expression(sql);
            }
            WfProcessMgr[] processMgrs = pmi.get_next_n_sequence(0);
            Shark shark = Shark.getInstance();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            AdminMisc admin = shark.getAdminMisc();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);
            for (int i = 0; i < processMgrs.length; i++) {
                WfProcessMgr pm = processMgrs[i];
                WMEntity ent = admin.getProcessDefinitionInfoByUniqueProcessDefinitionName(sessionHandle, processMgrs[i].name());

                WorkflowProcess wp = new WorkflowProcess();

                //find package name
                String packageVersion = (version != null && !version.isEmpty()) ? version : pa.getCurrentPackageVersion(sessionHandle, ent.getPkgId());
                WMEntity entity = pa.getPackageEntity(sessionHandle, ent.getPkgId(), packageVersion);
                String packageName = entity.getName();

                wp.setId(pm.name());
                wp.setPackageId(MiscUtilities.getProcessMgrPkgId(pm.name()));
                wp.setPackageName(packageName);
                wp.setName(ent.getName());
                wp.setVersion(pm.version());
                wp.setDescription(pm.description());
                wp.setCategory(pm.category());

                String currentVersion = pa.getCurrentPackageVersion(sessionHandle, wp.getPackageId());
                wp.setLatest(currentVersion == null || (currentVersion.equals(wp.getVersion())));

                processMap.put(wp.getName(), wp);
            }

            processList = new ArrayList<WorkflowProcess>(processMap.values());
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        return processList;
    }

    /**
     * Returns a list of process definitions
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @param packageId
     * @param all
     * @param checkWhiteList
     * @return
     */
    public PagedList<WorkflowProcess> getProcessList(String sort, Boolean desc, Integer start, Integer rows, String packageId, Boolean all, Boolean checkWhiteList) {
        List<WorkflowProcess> processList = (List<WorkflowProcess>) getProcessList(packageId);

        // filter by packageId and versions
        boolean filterByPackage = packageId != null && packageId.trim().length() > 0;
        boolean latestVersion = all == null || !all;

        if (filterByPackage || latestVersion || (checkWhiteList != null && checkWhiteList)) {
            for (Iterator<WorkflowProcess> i = processList.iterator(); i.hasNext();) {
                WorkflowProcess proc = i.next();
                if (latestVersion && !proc.isLatest()) {
                    i.remove();
                } else if (filterByPackage && !packageId.equals(proc.getPackageId())) {
                    i.remove();
                } else {
                    if (checkWhiteList != null && checkWhiteList) {
                        if (!isUserInWhiteList(proc.getId())) {
                            i.remove();
                        }
                    }
                }
            }
        }

        // set total
        Integer total = new Integer(processList.size());

        // perform sorting and paging
        PagedList<WorkflowProcess> pagedList = new PagedList<WorkflowProcess>(true, processList, sort, desc, start, rows, total);

        return pagedList;
    }

    /**
     * Returns a process definition ID based on a process instance ID.
     * @param instanceId
     * @return
     */
    public String getProcessDefIdByInstanceId(String instanceId) {

        SharkConnection sc = null;
        String processDefId = null;

        try {

            sc = connect();

            WfProcess wfProcess = sc.getProcess(instanceId);
            processDefId = (wfProcess != null ? wfProcess.manager().name() : null);


        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        return processDefId;
    }

    /**
     * Returns a process definition by its definition ID.
     * @param processDefId
     * @return
     */
    public WorkflowProcess getProcess(String processDefId) {

        SharkConnection sc = null;
        WorkflowProcess wp = null;


        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            AdminMisc admin = shark.getAdminMisc();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);

            WfProcessMgr pm = sc.getProcessMgr(processDefId);
            WMEntity ent = admin.getProcessDefinitionInfoByUniqueProcessDefinitionName(sessionHandle, processDefId);

            if (pm != null && ent != null) {
                wp = new WorkflowProcess();
                wp.setId(pm.name());
                wp.setPackageId(MiscUtilities.getProcessMgrPkgId(pm.name()));

                //find package name
                String packageVersion = pa.getCurrentPackageVersion(sessionHandle, ent.getPkgId());
                WMEntity entity = pa.getPackageEntity(sessionHandle, ent.getPkgId(), packageVersion);
                String packageName = entity.getName();

                wp.setPackageName(packageName);
                wp.setName(ent.getName());
                wp.setVersion(pm.version());
                wp.setDescription(pm.description());
                wp.setCategory(pm.category());

                String currentVersion = pa.getCurrentPackageVersion(sessionHandle, wp.getPackageId());
                wp.setLatest(currentVersion == null || (currentVersion.equals(wp.getVersion())));
            }



        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        return wp;
    }

    /**
     * Returns the activity definitions for a process definition ID.
     * @param processId
     * @return
     */
    public Collection<WorkflowActivity> getProcessActivityDefinitionList(String processDefId) {
        Collection<WorkflowActivity> activityList = new ArrayList<WorkflowActivity>();

        SharkConnection sc = null;

        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            AdminMisc admin = shark.getAdminMisc();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            getSharkPackageAdmin(sessionHandle); // invoke this to clear xpdl cache

            WMEntity ent = admin.getProcessDefinitionInfoByUniqueProcessDefinitionName(sessionHandle, processDefId);
            XPDLBrowser xpdlBrowser = shark.getXPDLBrowser();
            WMEntity[] activities = WMEntityUtilities.getAllActivities(sessionHandle, xpdlBrowser, ent);
            for (int i = 0; i < activities.length; i++) {
                WMEntity entity = activities[i];
                WorkflowActivity wa = new WorkflowActivity();
                wa.setId(entity.getId());
                wa.setActivityDefId(entity.getId());
                wa.setName(entity.getName());
                wa.setType(WorkflowActivity.TYPE_NORMAL);
                wa.setProcessVersion(entity.getPkgVer());
                //check activity type

                //check for tool
                WMEntityIterator activityEntityIterator = xpdlBrowser.listEntities(sessionHandle, entity, null, true);
                while (activityEntityIterator.hasNext()) {
                    WMEntity actEnt = (WMEntity) activityEntityIterator.next();
                    if (actEnt.getType().equalsIgnoreCase("tool")) {
                        wa.setType(WorkflowActivity.TYPE_TOOL);
                        break;
                    } else if (actEnt.getType().equalsIgnoreCase("route")) {
                        wa.setType(WorkflowActivity.TYPE_ROUTE);
                        break;
                    }
                }

                activityList.add(wa);
            }

        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return activityList;
    }

    /**
     * Returns the activity definitions for a process definition ID and actvity definition ID.
     * @param processDefId
     * @param activityDefId
     * @return
     */
    public WorkflowActivity getProcessActivityDefinition(String processDefId, String activityDefId) {
        Collection<WorkflowActivity> activityList = getProcessActivityDefinitionList(processDefId);

        for (Iterator<WorkflowActivity> i = activityList.iterator(); i.hasNext();) {
            WorkflowActivity wa = (WorkflowActivity) i.next();
            if (wa.getId().equals(activityDefId)) {
                return wa;
            }
        }
        return null;
    }

    /**
     * Returns the participant definitions for a process definition ID.
     * @param processDefId
     * @return
     */
    public Collection<WorkflowParticipant> getProcessParticipantDefinitionList(String processDefId) {
        Collection<WorkflowParticipant> participantList = new ArrayList<WorkflowParticipant>();
        try {
            Map<String, WorkflowParticipant> participantMap = getParticipantMap(processDefId);
            participantList = new ArrayList(participantMap.values());
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        }
        return participantList;
    }

    /**
     * Returns the participant definitions for a process definition ID in a map.
     * @param processDefId
     * @return
     */
    public Map<String, WorkflowParticipant> getParticipantMap(String processDefId) {
        Map<String, WorkflowParticipant> participantMap = new SequencedHashMap();

        SharkConnection sc = null;

        try {
            sc = connect();

            Shark shark = Shark.getInstance();
            XPDLBrowser xpdlBrowser = shark.getXPDLBrowser();
            AdminMisc admin = shark.getAdminMisc();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);

            // get package participants
            WMEntity ent = admin.getProcessDefinitionInfoByUniqueProcessDefinitionName(sessionHandle, processDefId);
            WMEntity packageEntity = pa.getPackageEntity(sessionHandle, ent.getPkgId(), ent.getPkgVer());
            WMEntity[] pParticipants = WMEntityUtilities.getAllParticipants(sessionHandle, xpdlBrowser, packageEntity);
            Map<String, WorkflowParticipant> tempParticipantMap = new SequencedHashMap();
            for (int i = 0; i < pParticipants.length; i++) {
                WMEntity entity = pParticipants[i];
                WMEntity entityType = WMEntityUtilities.getSubEntity(sessionHandle, xpdlBrowser, entity, "ParticipantType");
                String entityTypeValue = WMEntityUtilities.getAttributeValue(sessionHandle, xpdlBrowser, entityType, "Type");
                if ("ROLE".equals(entityTypeValue)) {
                    WorkflowParticipant participant = new WorkflowParticipant();
                    participant.setId(entity.getId());
                    participant.setName(entity.getName());
                    participant.setPackageLevel(false);
                    tempParticipantMap.put(participant.getId(), participant);
                }
            }

            String participantIdsInProcess = WMEntityUtilities.findEAAndGetValue(sessionHandle, xpdlBrowser, ent, "JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER");
            String ids[] = participantIdsInProcess.split(";");
            if (ids.length > 0) {
                for (String id : ids) {
                    if (tempParticipantMap.get(id) != null) {
                        participantMap.put(id, tempParticipantMap.get(id));
                    }
                }
            }

        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return participantMap;
    }

    /**
     * Returns the variable definitions for a process definition ID.
     * @param processId
     * @return
     */
    public Collection<WorkflowVariable> getProcessVariableDefinitionList(String processDefId) {
        Collection<WorkflowVariable> variableList = new ArrayList<WorkflowVariable>();

        SharkConnection sc = null;

        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            AdminMisc admin = shark.getAdminMisc();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);
            WMEntity processEntity = admin.getProcessDefinitionInfoByUniqueProcessDefinitionName(sessionHandle, processDefId);
            WMEntity packageEntity = pa.getPackageEntity(sessionHandle, processEntity.getPkgId(), processEntity.getPkgVer());
            XPDLBrowser xpdlBrowser = shark.getXPDLBrowser();

            // get package variables
            WMFilter vfilter = new WMFilter("Type", WMFilter.EQ, "DataField");
            vfilter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
            WMEntityIterator ei = xpdlBrowser.listEntities(sessionHandle, packageEntity, vfilter, true);
            WMEntity[] variables = ei.getArray();
            for (int i = 0; i < variables.length; i++) {
                WMEntity entity = variables[i];
                WorkflowVariable wv = new WorkflowVariable();
                wv.setId(entity.getId());
                wv.setName(entity.getName());
                wv.setPackageLevel(true);
                variableList.add(wv);
            }

            // get process variables
            WMEntityIterator ei2 = xpdlBrowser.listEntities(sessionHandle, processEntity, vfilter, true);
            WMEntity[] variables2 = ei2.getArray();
            for (int i = 0; i < variables2.length; i++) {
                WMEntity entity = variables2[i];
                WorkflowVariable wv = new WorkflowVariable();
                wv.setId(entity.getId());
                wv.setName(entity.getName());
                wv.setPackageLevel(false);
                variableList.add(wv);
            }

        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return variableList;
    }

    /**
     * Returns the application definitions for a process definition ID.
     * @param processId
     * @return
     */
    public Collection<WorkflowTool> getProcessToolDefinitionList(String processDefId) {
        Collection<WorkflowTool> toolList = new ArrayList<WorkflowTool>();

        SharkConnection sc = null;

        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            AdminMisc admin = shark.getAdminMisc();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            WMEntity processEntity = admin.getProcessDefinitionInfoByUniqueProcessDefinitionName(sessionHandle, processDefId);
            XPDLBrowser xpdlBrowser = shark.getXPDLBrowser();
            WMEntity[] applications = WMEntityUtilities.getAllApplications(sessionHandle, xpdlBrowser, processEntity);
            for (int i = 0; i < applications.length; i++) {
                WMEntity entity = applications[i];
                WorkflowTool wa = new WorkflowTool();
                wa.setId(entity.getId());
                wa.setName(entity.getName());
                toolList.add(wa);
            }



        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return toolList;
    }


    /*--- Process definition update methods ---*/
    /**
     * Upload a package XPDL without updating mapping information.
     * @param packageId
     * @param processDefinitionData
     * @return
     * @throws Exception
     */
    public String processUploadWithoutUpdateMapping(String packageId, byte[] processDefinitionData) throws Exception {
        SharkConnection sc = null;
        String instanceId = null;

        try {
            sc = connect();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);
            WMEntity entity = null;

            //update package
            if (packageId != null && packageId.trim().length() > 0) {
                entity = pa.updatePackage(sessionHandle, packageId, processDefinitionData);
            } else {
                entity = pa.uploadPackage(sessionHandle, processDefinitionData);
            }

            pa.synchronizeXPDLCache(sessionHandle);

            if (entity != null) {
                instanceId = entity.getId();
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
            throw new RuntimeException(ex);
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return instanceId;
    }

    /**
     * Upload a package XPDL together with forms, participant and activity mapping information.
     * @param packageId
     * @param processDefinitionData
     * @return
     * @throws Exception
     */
    public String processUpload(String packageId, byte[] processDefinitionData) throws Exception {

        SharkConnection sc = null;
        String instanceId = null;

        try {

            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);

            WMEntity entity = null;
            //update package
            if (packageId != null && packageId.trim().length() > 0) {

                // update package
                entity = pa.updatePackage(sessionHandle, packageId, processDefinitionData);

            } else {
                entity = pa.uploadPackage(sessionHandle, processDefinitionData);
            }
            pa.synchronizeXPDLCache(sessionHandle);

            if (entity != null) {
                instanceId = entity.getId();
            }



        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
            throw new RuntimeException(ex);
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return instanceId;
    }

    /**
     * Reads package ID from XPDL definition
     * @param processDefinitionData
     * @return
     */
    public String getPackageIdFromDefinition(byte[] processDefinitionData) {
        try {
            String packageId = XMLUtil.getIdFromFile(new String(processDefinitionData, "UTF-8"));
            return packageId;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Deletes a specific package version together with its process instances.
     * @param packageId
     * @param version
     */
    public void processDeleteAndUnloadVersion(String packageId, String version) {

        SharkConnection sc = null;

        try {

            sc = connect();

            // delete process instances
            LogUtil.info(getClass().getName(), "Deleting running processes for " + packageId + " version " + version);
            Shark shark = Shark.getInstance();
            ExecutionAdministration ea = shark.getExecutionAdministration();
            WAPI wapi = shark.getWAPIConnection();
            WfProcessIterator wpi = sc.get_iterator_process();
            ProcessFilterBuilder fb = shark.getProcessFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            WMFilter filter1 = fb.addPackageIdEquals(sessionHandle, packageId);
            WMFilter filter2 = fb.addVersionEquals(sessionHandle, version);
            WMFilter filter = fb.and(sessionHandle, filter1, filter2);
            wpi.set_query_expression(fb.toIteratorExpression(sessionHandle, filter));
            WfProcess[] procs = wpi.get_next_n_sequence(0);
            for (int i = 0; i < procs.length; i++) {
                String instanceId = procs[i].key();
                try {
                    if (procs[i].state().startsWith(SharkConstants.STATEPREFIX_OPEN)) {
                        wapi.terminateProcessInstance(sessionHandle, instanceId);
                        LogUtil.info(getClass().getName(), " -- Terminated open process " + instanceId);
                    }
                    ea.deleteProcesses(sessionHandle, new String[]{instanceId});
                    LogUtil.info(getClass().getName(), " -- Deleted process " + instanceId);
                } catch (Exception e) {
                    LogUtil.info(getClass().getName(), " -- Could not delete process " + instanceId + ": " + e.toString());
                }
            }

            // unload
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);
            pa.closePackage(sessionHandle, packageId, version);



        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Deletes all versions for a package together with its associated process instances.
     * @param packageId
     */
    public void processDeleteAndUnload(String packageId) {

        SharkConnection sc = null;

        try {

            sc = connect();

            // delete process instances
            LogUtil.info(getClass().getName(), "Deleting all running processes for " + packageId);
            Shark shark = Shark.getInstance();
            ExecutionAdministration ea = shark.getExecutionAdministration();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            PackageAdministration pa = getSharkPackageAdmin(sessionHandle);
            WAPI wapi = shark.getWAPIConnection();
            WfProcessIterator wpi = sc.get_iterator_process();
            ProcessFilterBuilder fb = shark.getProcessFilterBuilder();
            WMFilter filter = fb.addPackageIdEquals(sessionHandle, packageId);
            wpi.set_query_expression(fb.toIteratorExpression(sessionHandle, filter));
            WfProcess[] procs = wpi.get_next_n_sequence(0);
            for (int i = 0; i < procs.length; i++) {
                String instanceId = procs[i].key();
                if (procs[i].state().startsWith(SharkConstants.STATEPREFIX_OPEN)) {
                    wapi.terminateProcessInstance(sessionHandle, instanceId);
                    LogUtil.info(getClass().getName(), " -- Terminated open process " + instanceId);
                }
                ea.deleteProcesses(sessionHandle, new String[]{instanceId});
                LogUtil.info(getClass().getName(), " -- Deleted process " + instanceId);
            }

            // unload
            pa.closeAllPackagesForId(sessionHandle, packageId);



        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /*--- Process and activity monitoring information methods ---*/
    /**
     * Returns a list of running processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public Collection<WorkflowProcess> getRunningProcessList(String packageId, String processId, String processName, String version, String sort, Boolean desc, Integer start, Integer rows) {

        SharkConnection sc = null;
        Collection<WorkflowProcess> runningProcessList = new ArrayList<WorkflowProcess>();
        try {

            sc = connect();

            WfProcessIterator pi = sc.get_iterator_process();

            String sharkExpression = "stateequals.(\"open.running\")";
            String sqlExpression = "State = (SELECT  oid  FROM SHKProcessStates WHERE  Name  = 'open.running')";

            if (packageId != null && packageId.trim().length() > 0) {
                sharkExpression += " && packageIdequals.(\"" + packageId + "\")";
                sqlExpression += " AND ProcessDefinition IN (SELECT  oid  FROM SHKProcessDefinitions WHERE  PackageId  = '" + packageId + "')";
            }
            if (processId != null && processId.trim().length() > 0) {
                sharkExpression += " && key.indexOf(\"" + processId + "\") != -1)";
                sqlExpression += " AND Id LIKE '%" + processId + "%'";
            }
            if (processName != null && processName.trim().length() > 0) {
                sharkExpression += " && name.indexOf(\"" + processName + "\") != -1)";
                sqlExpression += " AND Name LIKE '%" + processName + "%'";
            }
            if (version != null && version.trim().length() > 0) {
                sharkExpression += " && versionequals.(\"" + version + "\")";
                sqlExpression += " AND ProcessDefinition IN (SELECT oid FROM SHKProcessDefinitions WHERE ProcessDefinitionVersion = '" + version + "')";
            }

            if (start == null) {
                start = 0;
            }
            String queryExpression = "/*startAt " + start + " startAt*/";
            if (rows != null && rows > 0) {
                queryExpression += "/*limit " + rows + " limit*/";
            }

            String sortStr = "";
            if (sort != null && sort.trim().length() > 0) {
                sortStr += " ORDER BY " + sort;
                sortStr += (desc != null && desc.booleanValue()) ? " DESC" : "";
            }
            String query_expression = "(" + sharkExpression + ")" + " /*sql (" + sqlExpression + ") " + sortStr + " sql*/ " + queryExpression;
            pi.set_query_expression(query_expression);

            WfProcess[] wfRunningProcessList = pi.get_next_n_sequence(0);

            for (int i = 0; i < wfRunningProcessList.length; ++i) {
                WfProcess wfProcess = wfRunningProcessList[i];
                WfProcessMgr manager = wfProcess.manager();

                WorkflowProcess workflowProcess = new WorkflowProcess();
                workflowProcess.setId(manager.name());
                workflowProcess.setInstanceId(wfProcess.key());
                workflowProcess.setName(wfProcess.name());
                workflowProcess.setState(wfProcess.state());
                workflowProcess.setPackageId(MiscUtilities.getProcessMgrPkgId(manager.name()));
                workflowProcess.setVersion(manager.version());
                workflowProcess.setRequesterId(getUserByProcessIdAndActivityDefId(workflowProcess.getId(), workflowProcess.getInstanceId(), WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS));

                WorkflowProcess trackWflowProcess = getRunningProcessInfo(wfProcess.key());
                workflowProcess.setStartedTime(trackWflowProcess.getStartedTime());
                workflowProcess.setDue(trackWflowProcess.getDue());

                runningProcessList.add(workflowProcess);
            }


        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return runningProcessList;
    }

    /**
     * Returns the number of running processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @return
     */
    public int getRunningProcessSize(String packageId, String processId, String processName, String version) {

        SharkConnection sc = null;
        try {

            sc = connect();
            int size = 0;
            Map stateMap = getProcessStateMap();
            ProcessQuery pq = new ProcessQuery();
            BigDecimal processStateId = ((ProcessStateDO) stateMap.get(SharkConstants.STATE_OPEN_RUNNING)).get_OId().toBigDecimal();
            pq.setQueryState(ProcessStateDO.createExisting(processStateId), QueryBuilder.EQUAL);
            if (packageId != null && packageId.trim().length() > 0) {
                pq.setQueryPDefName(packageId + "#", QueryBuilder.CASE_INSENSITIVE_STARTS_WITH);
            }

            if (processId != null && processId.trim().length() > 0) {
                pq.setQueryId(processId, QueryBuilder.CASE_INSENSITIVE_CONTAINS);
            }

            if (processName != null && processName.trim().length() > 0) {
                pq.setQueryName(processName, QueryBuilder.CASE_INSENSITIVE_CONTAINS);
            }

            if (version != null && version.trim().length() > 0) {
                pq.setQueryPDefName("#" + version + "#", QueryBuilder.CASE_SENSITIVE_CONTAINS);
            }

            size = pq.getCount();
            return size;
        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return 0;
    }

    /**
     * Returns a list of completed processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public Collection<WorkflowProcess> getCompletedProcessList(String packageId, String processId, String processName, String version, String sort, Boolean desc, Integer start, Integer rows) {

        SharkConnection sc = null;
        Collection<WorkflowProcess> runningProcessList = new ArrayList<WorkflowProcess>();
        try {

            sc = connect();

            WfProcessIterator pi = sc.get_iterator_process();

            String sharkExpression = "statenotequals.(\"open.running\")";
            String sqlExpression = "State IN (SELECT  oid  FROM SHKProcessStates WHERE  Name  <> 'open.running')";

            if (packageId != null && packageId.trim().length() > 0) {
                sharkExpression += " && packageIdequals.(\"" + packageId + "\")";
                sqlExpression += " AND ProcessDefinition IN (SELECT  oid  FROM SHKProcessDefinitions WHERE  PackageId  = '" + packageId + "')";
            }

            if (processId != null && processId.trim().length() > 0) {
                sharkExpression += " && key.indexOf(\"" + processId + "\") != -1)";
                sqlExpression += " AND Id LIKE '%" + processId + "%'";
            }

            if (processName != null && processName.trim().length() > 0) {
                sharkExpression += " && name.indexOf(\"" + processName + "\") != -1)";
                sqlExpression += " AND Name LIKE '%" + processName + "%'";
            }

            if (version != null && version.trim().length() > 0) {
                sharkExpression += " && versionequals.(\"" + version + "\")";
                sqlExpression += " AND ProcessDefinition IN (SELECT oid FROM SHKProcessDefinitions WHERE ProcessDefinitionVersion = '" + version + "')";
            }

            if (start == null) {
                start = 0;
            }
            String queryExpression = "/*startAt " + start + " startAt*/";
            if (rows != null && rows > 0) {
                queryExpression += "/*limit " + rows + " limit*/";
            }

            String sortStr = "";
            if (sort != null && sort.trim().length() > 0) {
                sortStr += " ORDER BY " + sort;
                sortStr += (desc != null && desc.booleanValue()) ? " DESC" : "";
            }

            String query_expression = "(" + sharkExpression + ")" + " /*sql (" + sqlExpression + ") " + sortStr + " sql*/ " + queryExpression;
            pi.set_query_expression(query_expression);
            WfProcess[] wfRunningProcessList = pi.get_next_n_sequence(0);

            for (int i = 0; i < wfRunningProcessList.length; ++i) {
                WfProcess wfProcess = wfRunningProcessList[i];
                WfProcessMgr manager = wfProcess.manager();

                WorkflowProcess workflowProcess = new WorkflowProcess();
                workflowProcess.setId(manager.name());
                workflowProcess.setInstanceId(wfProcess.key());
                workflowProcess.setName(wfProcess.name());
                workflowProcess.setState(wfProcess.state());
                workflowProcess.setPackageId(MiscUtilities.getProcessMgrPkgId(manager.name()));
                workflowProcess.setVersion(manager.version());
                workflowProcess.setRequesterId(getUserByProcessIdAndActivityDefId(workflowProcess.getId(), workflowProcess.getInstanceId(), WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS));

                WorkflowProcess trackWflowProcess = getRunningProcessInfo(wfProcess.key());
                workflowProcess.setStartedTime(trackWflowProcess.getStartedTime());
                workflowProcess.setDue(trackWflowProcess.getDue());

                runningProcessList.add(workflowProcess);
            }


        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return runningProcessList;
    }

    /**
     * Returns the number of completed processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @return
     */
    public int getCompletedProcessSize(String packageId, String processId, String processName, String version) {

        SharkConnection sc = null;
        try {

            sc = connect();
            int size = 0;
            Map stateMap = getProcessStateMap();
            ProcessQuery pq = new ProcessQuery();
            pq.setQueryState(ProcessStateDO.createExisting(((ProcessStateDO) stateMap.get(SharkConstants.STATE_OPEN_RUNNING)).get_OId().toBigDecimal()), QueryBuilder.NOT_EQUAL);
            if (packageId != null && packageId.trim().length() > 0) {
                pq.setQueryPDefName(packageId + "#", QueryBuilder.CASE_INSENSITIVE_STARTS_WITH);
            }

            if (processId != null && processId.trim().length() > 0) {
                pq.setQueryId(processId, QueryBuilder.CASE_INSENSITIVE_CONTAINS);
            }

            if (processName != null && processName.trim().length() > 0) {
                pq.setQueryName(processName, QueryBuilder.CASE_INSENSITIVE_CONTAINS);
            }

            if (version != null && version.trim().length() > 0) {
                pq.setQueryPDefName("#" + version + "#", QueryBuilder.CASE_SENSITIVE_CONTAINS);
            }

            size = pq.getCount();
            return size;
        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return 0;
    }

    /**
     * Returns a running process by process instance ID.
     * @param processId
     * @return
     */
    public WorkflowProcess getRunningProcessById(String processId) {

        SharkConnection sc = null;
        WorkflowProcess workflowProcess = new WorkflowProcess();
        try {
            if (processId == null || processId.trim().length() == 0) {
                return null;
            }

            sc = connect();

            Shark shark = Shark.getInstance();
            WfProcessIterator pi = sc.get_iterator_process();
            ProcessFilterBuilder pieb = shark.getProcessFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();

            WMFilter filter = new WMFilter();

            if (processId != null && processId.trim().length() > 0) {
                filter = pieb.addIdEquals(sessionHandle, processId);
            }

            pi.set_query_expression(pieb.toIteratorExpression(sessionHandle, filter));
            WfProcess[] wfProcessList = pi.get_next_n_sequence(0);

            if (wfProcessList.length > 0) {
                WfProcess wfProcess = wfProcessList[0];
                WfProcessMgr manager = wfProcess.manager();

                workflowProcess.setId(manager.name());
                workflowProcess.setInstanceId(wfProcess.key());
                workflowProcess.setName(wfProcess.name());
                workflowProcess.setState(wfProcess.state());
                workflowProcess.setPackageId(MiscUtilities.getProcessMgrPkgId(manager.name()));
                workflowProcess.setVersion(manager.version());
                workflowProcess.setRequesterId(getUserByProcessIdAndActivityDefId(workflowProcess.getId(), workflowProcess.getInstanceId(), WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS));
            }


        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return workflowProcess;
    }

    /**
     * Returns a list of running or completed activities for a process instance ID.
     * @param processId
     * @param start
     * @param rows
     * @param sort
     * @param desc
     * @return
     */
    public Collection<WorkflowActivity> getActivityList(String processId, Integer start, Integer rows, String sort, Boolean desc) {

        SharkConnection sc = null;
        Collection<WorkflowActivity> activityList = new ArrayList<WorkflowActivity>();

        int activitySize = 0;

        try {
            activitySize = getActivitySize(processId);


            sc = connect();

            Shark shark = Shark.getInstance();
            WfActivityIterator ai = sc.get_iterator_activity();
            ActivityFilterBuilder aieb = shark.getActivityFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            AdminMisc admin = shark.getAdminMisc();

            WMFilter filter = new WMFilter();

            if (processId != null && processId.trim().length() > 0) {
                filter = aieb.addProcessIdEquals(sessionHandle, processId);
            }

            if (sort != null && sort.trim().length() > 0) {
                if (desc == null) {
                    desc = false;
                }
                if (sort.equals("id")) {
                    filter = aieb.setOrderById(sessionHandle, filter, !desc);
                } else {
                    filter = aieb.setOrderByName(sessionHandle, filter, !desc);
                }
            }

            if (start == null) {
                start = 0;
            }

            if (rows == null) {
                rows = 10;
            }

            filter = aieb.setStartPosition(sessionHandle, filter, start);
            String strSize = String.valueOf(activitySize);
            Integer size = Integer.parseInt(strSize.substring(strSize.length() - 1, strSize.length()));
            if ((start + rows) <= activitySize || Integer.parseInt(strSize) < rows) {
                filter = aieb.setLimit(sessionHandle, filter, rows);
            } else {
                filter = aieb.setLimit(sessionHandle, filter, size);
            }

            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
            WfActivity[] wfActivityList = ai.get_next_n_sequence(0);

            for (int i = 0; i < wfActivityList.length; ++i) {
                WfActivity wfActivity = wfActivityList[i];
                WorkflowActivity workflowActivity = getActivityById(wfActivity.key());

                WorkflowActivity trackWflowActivity = getRunningActivityInfo(sc, wfActivity, false);
                if (trackWflowActivity != null) {
                    workflowActivity.setCreatedTime(trackWflowActivity.getCreatedTime());
                }

                activityList.add(workflowActivity);
            }


        } catch (Exception ex) {

            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return activityList;
    }

    /**
     * Returns the number of running or completed activities for a process instance ID.
     * @param processId
     * @return
     */
    public int getActivitySize(String processId) {

        SharkConnection sc = null;
        int size = 0;
        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            WfActivityIterator ai = sc.get_iterator_activity();
            ActivityFilterBuilder aieb = shark.getActivityFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();

            WMFilter filter = new WMFilter();

            if (processId != null && processId.trim().length() > 0) {
                filter = aieb.addProcessIdEquals(sessionHandle, processId);
            }

            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
            size = ai.how_many();


        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return size;
    }

    /**
     * Returns an activity instance based on the activity instance ID.
     * @param activityId
     * @return
     */
    public WorkflowActivity getActivityById(String activityId) {

        SharkConnection sc = null;
        WorkflowActivity workflowActivity = new WorkflowActivity();
        try {
            if (activityId == null || activityId.trim().length() == 0) {
                return null;
            }

            sc = connect();

            Shark shark = Shark.getInstance();
            AdminMisc admin = shark.getAdminMisc();
            WfActivityIterator ai = sc.get_iterator_activity();
            ActivityFilterBuilder aieb = shark.getActivityFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();

            WMFilter filter = new WMFilter();

            if (activityId != null && activityId.trim().length() > 0) {
                filter = aieb.addIdEquals(sessionHandle, activityId);
            }

            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
            WfActivity[] wfActivityList = ai.get_next_n_sequence(0);

            if (wfActivityList.length > 0) {
                WfActivity wfActivity = wfActivityList[0];

                workflowActivity.setId(wfActivity.key());
                WMEntity entity = admin.getActivityDefinitionInfo(sessionHandle, wfActivity.container().key(), wfActivity.key());
                workflowActivity.setActivityDefId(entity.getId());

                WfProcess process = wfActivity.container();
                WfProcessMgr manager = process.manager();

                workflowActivity.setName(wfActivity.name());
                workflowActivity.setState(wfActivity.state());
                workflowActivity.setProcessDefId(wfActivity.container().manager().name());
                workflowActivity.setProcessId(wfActivity.container().key());
                workflowActivity.setProcessName(wfActivity.container().name());
                workflowActivity.setProcessVersion(manager.version());
                workflowActivity.setPriority(String.valueOf(wfActivity.priority()));
                workflowActivity.setProcessStatus(process.state());
                // check for hash variable
                if (WorkflowUtil.containsHashVariable(wfActivity.name())) {
                    WorkflowAssignment ass = new WorkflowAssignment();
                    ass.setProcessId(workflowActivity.getProcessId());
                    ass.setProcessDefId(workflowActivity.getProcessDefId());
                    ass.setProcessName(workflowActivity.getProcessName());
                    ass.setProcessVersion(workflowActivity.getProcessVersion());
                    ass.setProcessRequesterId(getUserByProcessIdAndActivityDefId(workflowActivity.getProcessDefId(), workflowActivity.getProcessId(), WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS));
                    ass.setDescription(workflowActivity.getDescription());
                    ass.setActivityId(activityId);
                    ass.setActivityName(workflowActivity.getName());
                    ass.setActivityDefId(workflowActivity.getActivityDefId());
                    ass.setAssigneeId(workflowActivity.getPerformer());

                    ass.setProcessVariableList(new ArrayList(getProcessVariableList(workflowActivity.getProcessId())));
                    //process activity name variable
                    workflowActivity.setName(WorkflowUtil.processVariable(wfActivity.name(), null, ass));
                }
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return workflowActivity;
    }

    /**
     * Returns the variable value based on a process instance ID.
     * @param processInstanceId
     * @param variableId
     * @return
     */
    public String getProcessVariable(String processInstanceId, String variableId) {

        SharkConnection sc = null;

        try {
            if (processInstanceId == null || processInstanceId.trim().length() == 0) {
                return null;
            }

            sc = connect();

            WfProcess wfProcess = sc.getProcess(processInstanceId);

            if (wfProcess != null) {
                Map varMap = wfProcess.process_context();
                LogUtil.debug(getClass().getName(), "varMap: " + varMap);

                if (!varMap.isEmpty()) {
                    return String.valueOf(varMap.get(variableId));
                }
            }
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        return null;
    }

    /**
     * Returns a list of workflow variables for the specified activity instance ID (for any user)
     * @param activityId
     * @return
     */
    public Collection<WorkflowVariable> getActivityVariableList(String activityId) {
        Collection<WorkflowVariable> variableList = new ArrayList<WorkflowVariable>();

        SharkConnection sc = null;

        try {

            sc = connect();

            WfActivityIterator ai = sc.get_iterator_activity();
            Shark shark = Shark.getInstance();
            ActivityFilterBuilder aieb = shark.getActivityFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            WMFilter filter = aieb.addIdEquals(sessionHandle, activityId);
            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
            WfActivity[] acts = ai.get_next_n_sequence(0);

            if (acts.length > 0) {
                Map varMap = acts[0].process_context();
                LogUtil.debug(getClass().getName(), "varMap: " + varMap);
                for (Iterator i = varMap.keySet().iterator(); i.hasNext();) {
                    String key = (String) i.next();
                    Object val = varMap.get(key);
                    WorkflowVariable wv = new WorkflowVariable();
                    wv.setId(key);
                    wv.setName(key);
                    wv.setVal(val);
                    variableList.add(wv);
                }
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return variableList;
    }

    /**
     * Returns a list of workflow variables for the specified process instance ID (for any user)
     * @param processId
     * @return
     */
    public Collection<WorkflowVariable> getProcessVariableList(String processId) {
        Collection<WorkflowVariable> variableList = new ArrayList<WorkflowVariable>();

        SharkConnection sc = null;

        try {

            sc = connect();

            WfProcess wfProcess = sc.getProcess(processId);

            if (wfProcess != null) {
                Map varMap = wfProcess.process_context();
                LogUtil.debug(getClass().getName(), "varMap: " + varMap);
                for (Iterator i = varMap.keySet().iterator(); i.hasNext();) {
                    String key = (String) i.next();
                    Object val = varMap.get(key);
                    WorkflowVariable wv = new WorkflowVariable();
                    wv.setId(key);
                    wv.setName(key);
                    wv.setVal(val);
                    variableList.add(wv);
                }
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return variableList;
    }

    /**
     * Gets the service level for a specific process instance ID.
     * @param processInstanceId
     * @return
     */
    public double getServiceLevelMonitorForRunningProcess(String processInstanceId) {
        WorkflowProcess process = getRunningProcessInfo(processInstanceId);
        return getServiceLevelValue(process.getStartedTime(), process.getFinishTime(), process.getDue());
    }

    /**
     * Gets the service level for a specific activity instance ID.
     * @param activityInstanceId
     * @return
     */
    public double getServiceLevelMonitorForRunningActivity(String activityInstanceId) {
        WorkflowActivity activity = getRunningActivityInfo(activityInstanceId);
        return getServiceLevelValue(activity.getCreatedTime(), activity.getFinishTime(), activity.getDue());
    }

    /**
     * Returns process monitoring info (eg date creation, due dates, etc) for a process instance ID.
     * @param processInstanceId
     * @return
     */
    public WorkflowProcess getRunningProcessInfo(String processInstanceId) {

        SharkConnection sc = null;

        try {
            if (processInstanceId == null || processInstanceId.trim().length() == 0) {
                return null;
            }

            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            Shark shark = Shark.getInstance();

            WfProcessIterator pi = sc.get_iterator_process();
            ProcessFilterBuilder pfb = shark.getProcessFilterBuilder();

            XPDLBrowser xpdl = shark.getXPDLBrowser();

            WMFilter filter = new WMFilter();

            if (processInstanceId != null && processInstanceId.trim().length() > 0) {
                filter = pfb.addIdEquals(sessionHandle, processInstanceId);
            }

            pi.set_query_expression(pfb.toIteratorExpression(sessionHandle, filter));
            WfProcess[] wfProcessArray = pi.get_next_n_sequence(0);
            WorkflowProcess wfProcess = new WorkflowProcess();


            int limit = -1;

            //get process limit
            AdminMisc admin = shark.getAdminMisc();
            WMEntity processLimitEnt = admin.getProcessDefinitionInfo(sessionHandle, processInstanceId);


            filter = new WMFilter();
            filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
            filter.setAttributeName("Type");
            filter.setFilterString("ProcessHeader");

            WMEntityIterator processLimitEntityIterator = xpdl.listEntities(sessionHandle, processLimitEnt, filter, true);
            WMEntity[] processLimitEntityList = null;
            if (processLimitEntityIterator != null) {
                processLimitEntityList = processLimitEntityIterator.getArray();
            }

            if (processLimitEntityList != null) {
                WMEntity processLimitEntity = processLimitEntityList[0];

                filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
                filter.setAttributeName("Name");
                filter.setFilterString("Limit");

                WMAttributeIterator procAttributeIterator = xpdl.listAttributes(sessionHandle, processLimitEntity, filter, true);
                WMAttribute[] procAttributeList = null;
                if (procAttributeIterator != null) {
                    procAttributeList = procAttributeIterator.getArray();
                }

                if (procAttributeList[0].getValue() != null && !procAttributeList[0].getValue().equals("")) {
                    limit = Integer.parseInt((String) procAttributeList[0].getValue());
                }
            }

            String durationUnit = "";
            if (limit != -1) {
                //get duration unit
                filter = new WMFilter();
                filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
                filter.setAttributeName("Type");
                filter.setFilterString("ProcessHeader");

                WMEntity processDurationEnt = admin.getProcessDefinitionInfo(sessionHandle, processInstanceId);
                WMEntityIterator processDurationEntityIterator = xpdl.listEntities(sessionHandle, processDurationEnt, filter, true);
                WMEntity[] processDurationEntityList = null;
                if (processDurationEntityIterator != null) {
                    processDurationEntityList = processDurationEntityIterator.getArray();
                }

                if (processDurationEntityList != null) {
                    WMEntity entity = processDurationEntityList[0];

                    filter = new WMFilter();
                    filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
                    filter.setAttributeName("Name");
                    filter.setFilterString("DurationUnit");

                    WMAttributeIterator procAttributeIterator = xpdl.listAttributes(sessionHandle, entity, filter, true);
                    WMAttribute[] procAttributeList = null;
                    if (procAttributeIterator != null) {
                        procAttributeList = procAttributeIterator.getArray();
                    }

                    if (procAttributeList != null) {
                        durationUnit = (String) procAttributeList[0].getValue();
                    }
                }
            }

            Calendar startedTimeCal = Calendar.getInstance();

            if (wfProcessArray != null && wfProcessArray.length > 0) {

                long startedTime = admin.getProcessStartedTime(sessionHandle, processInstanceId);

                startedTimeCal.setTimeInMillis(startedTime);

                wfProcess.setStartedTime(startedTimeCal.getTime());

                if (limit != -1) {
                    if (!durationUnit.equals("")) {
                        long limitInSecond = 0;
                        if (durationUnit.equals("D")) {
                            limitInSecond = limit * 24 * 60 * 60;
                            wfProcess.setLimit(limit + " day");
                        } else if (durationUnit.equals("h")) {
                            limitInSecond = limit * 60 * 60;
                            wfProcess.setLimit(limit + " hour(s)");
                        } else if (durationUnit.equals("m")) {
                            limitInSecond = limit * 60;
                            wfProcess.setLimit(limit + " minute(s)");
                        } else if (durationUnit.equals("s")) {
                            limitInSecond = limit;
                            wfProcess.setLimit(limit + " second(s)");
                        }

                        wfProcess.setDue(getDueDateProceedByPlugin(processInstanceId, "", limitInSecond, startedTimeCal.getTime(), startedTimeCal.getTime()));
                    }
                }
            }

            Date currentDate = new Date();

            if (wfProcessArray[0].state().equals(SharkConstants.STATE_CLOSED_COMPLETED)) {
                Calendar completionCal = Calendar.getInstance();
                Calendar dueCal = Calendar.getInstance();

                long finishTime = admin.getProcessFinishTime(sessionHandle, processInstanceId);

                completionCal.setTimeInMillis(finishTime);
                wfProcess.setFinishTime(completionCal.getTime());
                //completion minus due if completion date is after due date, vice versa otherwise
                if (wfProcess.getDue() != null && wfProcess.getFinishTime().after(wfProcess.getDue())) {
                    dueCal.setTime(wfProcess.getDue());
                    long delayInMilliseconds = completionCal.getTimeInMillis() - dueCal.getTimeInMillis();
                    long delayInSeconds = (long) delayInMilliseconds / 1000;
                    
                    wfProcess.setDelayInSeconds(delayInSeconds);
                    wfProcess.setDelay(convertTimeInSecondsToString(delayInSeconds));
                }


                //time taken for completion from date started
                long timeTakenInMilliSeconds = (wfProcess != null && wfProcess.getFinishTime() != null && wfProcess.getStartedTime() != null) ? wfProcess.getFinishTime().getTime() - wfProcess.getStartedTime().getTime() : 0;
                long timeTakenInSeconds = (long) timeTakenInMilliSeconds / 1000;

                wfProcess.setTimeConsumingFromDateStartedInSeconds(timeTakenInSeconds);
                wfProcess.setTimeConsumingFromDateStarted(convertTimeInSecondsToString(timeTakenInSeconds));

                //time taken for completion from date created
                timeTakenInMilliSeconds = (wfProcess != null && wfProcess.getFinishTime() != null && wfProcess.getCreatedTime() != null) ? wfProcess.getFinishTime().getTime() - wfProcess.getCreatedTime().getTime() : 0;
                timeTakenInSeconds = (long) timeTakenInMilliSeconds / 1000;
                
                wfProcess.setTimeConsumingFromDateCreatedInSeconds(timeTakenInSeconds);
                wfProcess.setTimeConsumingFromDateCreated(convertTimeInSecondsToString(timeTakenInSeconds));

            } else if (wfProcess.getDue() != null && currentDate.after(wfProcess.getDue())) {
                long delayInMilliseconds = ((wfProcess != null && wfProcess.getDue() != null) ? currentDate.getTime() - wfProcess.getDue().getTime() : 0);
                long delayInSeconds = (long) delayInMilliseconds / 1000;

                wfProcess.setDelayInSeconds(delayInSeconds);
                wfProcess.setDelay(convertTimeInSecondsToString(delayInSeconds));
            }

            return wfProcess;
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return null;
    }

    /**
     * Returns activity monitoring info (eg date creation, limit, due (creation + limit), delay and completion) for a process instance ID.
     * @param activityInstanceId
     * @return
     */
    public WorkflowActivity getRunningActivityInfo(String activityInstanceId) {
        SharkConnection sc = null;

        WorkflowActivity wfAct = new WorkflowActivity();

        try {

            if (activityInstanceId == null || activityInstanceId.trim().length() == 0) {
                return null;
            }

            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            Shark shark = Shark.getInstance();

            WfActivityIterator ai = sc.get_iterator_activity();
            ActivityFilterBuilder aieb = shark.getActivityFilterBuilder();

            WMFilter filter = new WMFilter();

            if (activityInstanceId != null && activityInstanceId.trim().length() > 0) {
                filter = aieb.addIdEquals(sessionHandle, activityInstanceId);
            }

            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
            WfActivity[] wfActivityArray = ai.get_next_n_sequence(0);

            if (wfActivityArray.length == 0) {
                return null;
            }

            //to get participant mapping
            WfActivity wfActivity = wfActivityArray[0];

            wfAct = getRunningActivityInfo(sc, wfActivity, true);

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        return wfAct;
    }

    /**
     * Returns activity monitoring info (eg date creation, limit, due (creation + limit), delay and completion) for a process instance ID.
     * @param activityInstanceId
     * @return
     */
    protected WorkflowActivity getRunningActivityInfo(SharkConnection sc, WfActivity wfActivity, boolean includeAssignees) {

        WorkflowActivity wfAct = new WorkflowActivity();

        try {

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            Shark shark = Shark.getInstance();
            XPDLBrowser xpdl = shark.getXPDLBrowser();

            String activityInstanceId = wfActivity.key();
            WfProcess wfProcess = wfActivity.container();
            WfProcessMgr wfProcessMgr = wfProcess.manager();
            String processInstanceId = wfProcess.key();
            String processDefId = wfProcessMgr.name();

            //get limit
            AdminMisc admin = shark.getAdminMisc();
            WMEntity actEnt = admin.getActivityDefinitionInfo(sessionHandle, processInstanceId, activityInstanceId);

            WMFilter filter = new WMFilter();
            filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
            filter.setAttributeName("Name");
            filter.setFilterString("Limit");

            WMAttributeIterator actAttributeIterator = xpdl.listAttributes(sessionHandle, actEnt, filter, true);
            WMAttribute[] actAttributeList = null;
            if (actAttributeIterator != null) {
                actAttributeList = actAttributeIterator.getArray();
            }

            // retrieve the user who accepted the particular activity
            String username = admin.getActivityResourceUsername(sessionHandle, processInstanceId, activityInstanceId);
            wfAct.setNameOfAcceptedUser(username);

            int limit = -1;
            if (actAttributeList != null) {
                if (actAttributeList[0].getValue() != null && !actAttributeList[0].getValue().equals("")) {
                    limit = Integer.parseInt((String) actAttributeList[0].getValue());
                }
            }

            filter = new WMFilter();
            filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
            filter.setAttributeName("Name");
            filter.setFilterString("Performer");

            actAttributeIterator = xpdl.listAttributes(sessionHandle, actEnt, filter, true);
            actAttributeList = null;
            if (actAttributeIterator != null) {
                actAttributeList = actAttributeIterator.getArray();
            }

            if (actAttributeList != null) {
                String state = wfActivity.state();
                if (state.equals(SharkConstants.STATE_OPEN_NOT_RUNNING_NOT_STARTED)) {
                    wfAct.setStatus("Pending");
                } else if (state.equals(SharkConstants.STATE_CLOSED_COMPLETED)) {
                    wfAct.setStatus("Completed");
                } else if (state.equals(SharkConstants.STATE_CLOSED_ABORTED)) {
                    wfAct.setStatus("Aborted");
                } else {
                    wfAct.setStatus("Accepted");
                }

                wfAct.setPerformer((String) actAttributeList[0].getValue());
            }

            if (includeAssignees) {
                List<String> users = getAssignmentResourceIds(processDefId, processInstanceId, activityInstanceId);
                Collections.sort(users);
                if (users != null) {
                    wfAct.setAssignmentUsers(users.toArray(new String[users.size()]));
                }
            }

            String durationUnit = "";
            if (limit != -1) {
                //get duration unit
                filter = new WMFilter();
                filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
                filter.setAttributeName("Type");
                filter.setFilterString("ProcessHeader");

                WMEntity procEnt = admin.getProcessDefinitionInfo(sessionHandle, processInstanceId);
                WMEntityIterator procEntityIterator = xpdl.listEntities(sessionHandle, procEnt, filter, true);
                WMEntity[] procEntityList = null;
                if (procEntityIterator != null) {
                    procEntityList = procEntityIterator.getArray();
                }

                if (procEntityList != null) {
                    WMEntity ent = procEntityList[0];

                    filter = new WMFilter();
                    filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
                    filter.setAttributeName("Name");
                    filter.setFilterString("DurationUnit");

                    WMAttributeIterator procAttributeIterator = xpdl.listAttributes(sessionHandle, ent, filter, true);
                    WMAttribute[] procAttributeList = null;
                    if (procAttributeIterator != null) {
                        procAttributeList = procAttributeIterator.getArray();
                    }

                    if (procAttributeList != null) {
                        durationUnit = (String) procAttributeList[0].getValue();
                    }
                }
            }

            Calendar calendar = Calendar.getInstance();

            long createdTime = admin.getActivityCreatedTime(sessionHandle, processInstanceId, activityInstanceId);
            long startedTime = admin.getActivityStartedTime(sessionHandle, processInstanceId, activityInstanceId);

            if (startedTime != SharkConstants.UNDEFINED_TIME) {
                calendar.setTimeInMillis(startedTime);
                wfAct.setStartedTime(calendar.getTime());
            }

            calendar.setTimeInMillis(createdTime);
            wfAct.setCreatedTime(calendar.getTime());

            if (limit != -1) {
                if (!durationUnit.equals("")) {
                    if (durationUnit.equals("D")) {
                        wfAct.setLimitInSeconds(limit * 24 * 60 * 60);
                        wfAct.setLimit(limit + " day");
                    } else if (durationUnit.equals("h")) {
                        wfAct.setLimitInSeconds(limit * 60 * 60);
                        wfAct.setLimit(limit + " hour(s)");
                    } else if (durationUnit.equals("m")) {
                        wfAct.setLimitInSeconds(limit * 60);
                        wfAct.setLimit(limit + " minute(s)");
                    } else if (durationUnit.equals("s")) {
                        wfAct.setLimitInSeconds(limit);
                        wfAct.setLimit(limit + " second(s)");
                    }
                    wfAct.setDue(getDueDateProceedByPlugin(processInstanceId, activityInstanceId, wfAct.getLimitInSeconds(), wfAct.getCreatedTime(), wfAct.getStartedTime()));
                }
            }

            Date currentDate = new Date();

            if (wfActivity.state().equals(SharkConstants.STATE_CLOSED_COMPLETED)) {
                Calendar completionCal = Calendar.getInstance();
                Calendar dueCal = Calendar.getInstance();

                long finishTime = admin.getActivityFinishTime(sessionHandle, processInstanceId, activityInstanceId);

                completionCal.setTimeInMillis(finishTime);
                wfAct.setFinishTime(completionCal.getTime());
                //completion minus due if completion date is after due date, vice versa otherwise
                if (wfAct.getDue() != null && wfAct.getFinishTime().after(wfAct.getDue())) {
                    dueCal.setTime(wfAct.getDue());
                    long delayInMilliseconds = completionCal.getTimeInMillis() - dueCal.getTimeInMillis();
                    long delayInSeconds = (long) delayInMilliseconds / 1000;

                    //set delay in seconds
                    wfAct.setDelayInSeconds(delayInSeconds);
                    wfAct.setDelay(convertTimeInSecondsToString(delayInSeconds));
                }

                //time taken for completion from date started
                long timeTakenInMilliSeconds = (wfAct != null && wfAct.getFinishTime() != null && wfAct.getStartedTime() != null) ? wfAct.getFinishTime().getTime() - wfAct.getStartedTime().getTime() : 0;
                long timeTakenInSeconds = (long) timeTakenInMilliSeconds / 1000;

                //set time consuming from date started in seconds
                wfAct.setTimeConsumingFromDateStartedInSeconds(timeTakenInSeconds);
                wfAct.setTimeConsumingFromDateStarted(convertTimeInSecondsToString(timeTakenInSeconds));

                //time taken for completion from date created
                timeTakenInMilliSeconds = (wfAct != null && wfAct.getFinishTime() != null && wfAct.getCreatedTime() != null) ? wfAct.getFinishTime().getTime() - wfAct.getCreatedTime().getTime() : 0;
                timeTakenInSeconds = (long) timeTakenInMilliSeconds / 1000;

                //set time consuming from date created in seconds
                wfAct.setTimeConsumingFromDateCreatedInSeconds(timeTakenInSeconds);
                wfAct.setTimeConsumingFromDateCreated(convertTimeInSecondsToString(timeTakenInSeconds));

            } else if (wfAct.getDue() != null && currentDate.after(wfAct.getDue())) {
                long delayInMilliseconds = currentDate.getTime() - wfAct.getDue().getTime();
                long delayInSeconds = (long) delayInMilliseconds / 1000;

                //set delay in seconds
                wfAct.setDelayInSeconds(delayInSeconds);
                wfAct.setDelay(convertTimeInSecondsToString(delayInSeconds));
            }

            short priority = wfActivity.priority();
            wfAct.setPriority(String.valueOf(priority));
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        }
        return wfAct;
    }

    public Map getActivityInstanceByProcessIdAndStatus(String processId, Boolean accepted) {

        SharkConnection sc = null;
        Map processMap = new HashMap();

        try {

            sc = connect();

            WfAssignment[] wItems = sc.getResourceObject().get_sequence_work_item(0);
            if (wItems != null) {
                for (int i = 0; i < wItems.length; ++i) {
                    String tempProcessId = wItems[i].activity().container().manager().name().split("#")[2];
                    if (processId.equals(tempProcessId)) {
                        WfAssignment wfa = wItems[i];
                        boolean acceptedStatus = wfa.get_accepted_status();
                        boolean validItem = ((accepted == null) || (accepted.booleanValue() && acceptedStatus) || (!accepted.booleanValue() && !acceptedStatus));
                        if (validItem) {
                            WfActivity activity = wfa.activity();
                            processMap.put(activity.name(), activity.key());
                        }
                    }
                }
            }



        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return processMap;
    }

//    public Collection<ParticipantDirectory> getNextParticipantInATool(String activityId) {
//        SharkConnection sc = null;
//
//        try {
//
//            sc = connect();
//
//            Shark shark = Shark.getInstance();
//
//            WorkflowActivity wfActivity = getActivityById(activityId);
//
//            AdminMisc adminMisc = shark.getAdminMisc();
//
//            WMSessionHandle sessionHandle = sc.getSessionHandle();
//            XPDLBrowser xpdlBrowser = shark.getXPDLBrowser();
//
//            WMEntity processEntity = adminMisc.getProcessDefinitionInfo(sessionHandle, wfActivity.getProcessId());
//            WMEntity[] activityEntities = (xpdlBrowser.listEntities(sessionHandle, processEntity, null, true)).getArray();
//
//            for (int i = 0; i < activityEntities.length; i++) {
//                WMEntity activityEntity = activityEntities[i];
//                if (wfActivity.getActivityDefId().equals(activityEntity.getActId())) {
//                    WMFilter filter = new WMFilter();
//                    filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
//                    filter.setAttributeName("Name");
//                    filter.setFilterString("Performer");
//
//                    WMAttributeIterator actAttributeIterator = xpdlBrowser.listAttributes(sessionHandle, activityEntity, filter, true);
//                    WMAttribute[] actAttributeList = null;
//
//                    if (actAttributeIterator != null) {
//                        actAttributeList = actAttributeIterator.getArray();
//                    }
//                    if (actAttributeList != null) {
//                        String processDefId = getProcessDefIdByInstanceId(wfActivity.getProcessId());
//                        String participantId = (String) actAttributeList[0].getValue();
//
//                        return participantDirectoryDao.getMappingByParticipantId(processDefId.split("#")[0], processDefId.split("#")[2], Integer.parseInt(processDefId.split("#")[1]), participantId);
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            LogUtil.error(getClass().getName(), ex, "");
//        } finally {
//            try {
//                disconnect(sc);
//            } catch (Exception e) {
//                LogUtil.error(getClass().getName(), e, "");
//            }
//        }
//
//        return null;
//    }

    /*--- Process and activity monitoring update methods ---*/
    /**
     * Set the workflow variable based on an activity instance ID.
     * @param activityInstanceId
     * @param variableId
     * @param variableValue
     */
    public void activityVariable(String activityInstanceId, String variableId, Object variableValue) {

        SharkConnection sc = null;

        try {
            if (activityInstanceId == null || activityInstanceId.trim().length() == 0) {
                return;
            }

            sc = connect();

            Shark shark = Shark.getInstance();
            WfActivityIterator ai = sc.get_iterator_activity();
            ActivityFilterBuilder aieb = shark.getActivityFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();

            WMFilter filter = new WMFilter();

            if (activityInstanceId != null && activityInstanceId.trim().length() > 0) {
                filter = aieb.addIdEquals(sessionHandle, activityInstanceId);
            }

            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
            WfActivity[] wfActivityList = ai.get_next_n_sequence(0);


            if (wfActivityList.length > 0) {
                WfActivity wfActivity = wfActivityList[0];
                Map var = new HashMap();
                var.put(variableId, variableValue);
                wfActivity.set_result(var);
            }



        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Set the workflow variable based on an process instance ID.
     * @param processInstanceId
     * @param variableId
     * @param variableValue
     */
    public void processVariable(String processInstanceId, String variableId, Object variableValue) {

        SharkConnection sc = null;

        try {
            if (processInstanceId == null || processInstanceId.trim().length() == 0) {
                return;
            }

            sc = connect();

            Shark shark = Shark.getInstance();
            WfProcessIterator pi = sc.get_iterator_process();
            ProcessFilterBuilder pfb = shark.getProcessFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();

            WMFilter filter = new WMFilter();

            if (processInstanceId != null && processInstanceId.trim().length() > 0) {
                filter = pfb.addIdEquals(sessionHandle, processInstanceId);
            }

            pi.set_query_expression(pfb.toIteratorExpression(sessionHandle, filter));
            WfProcess[] wfProcessList = pi.get_next_n_sequence(0);


            if (wfProcessList.length > 0) {
                WfProcess wfProcess = wfProcessList[0];
                Map processContext = wfProcess.process_context();
                processContext.put(variableId, variableValue);
                wfProcess.set_process_context(processContext);
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Reevaluate assignments for an activity based on an activity instance ID.
     * @param activityInstanceId
     */
    public void reevaluateAssignmentsForActivity(String activityInstanceId) {

        SharkConnection sc = null;

        try {

            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            Shark shark = Shark.getInstance();
            ExecutionAdministration ea = shark.getExecutionAdministration();

            ActivityFilterBuilder aieb = shark.getActivityFilterBuilder();
            WMFilter filter = aieb.addIdEquals(sessionHandle, activityInstanceId);

            ea.reevaluateAssignmentsWithFiltering(sessionHandle, filter, true);


        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    public void reevaluateAssignmentsForUser(String username) {
        SharkConnection sc = null;

        try {

            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            Shark shark = Shark.getInstance();
            ExecutionAdministration ea = shark.getExecutionAdministration();

            ActivityFilterBuilder aieb = shark.getActivityFilterBuilder();
            WMFilter filter = aieb.addHasAssignmentForUser(sessionHandle, username, 0);
            WMFilter filter2 = aieb.addStateEquals(sessionHandle, "open.not_running.not_started");
            WMFilter filter3 = aieb.addStateEquals(sessionHandle, "open.not_running.suspended");
            WMFilter filter4 = aieb.or(sessionHandle, filter2, filter3);
            filter = aieb.and(sessionHandle, filter, filter4);

            ea.reevaluateAssignmentsWithFiltering(sessionHandle, filter, true);


        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Reevaluate assignments for a process based on an process instance ID.
     * @param procInstanceId
     */
    public void reevaluateAssignmentsForProcess(String procInstanceId) {

        SharkConnection sc = null;

        try {

            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            Shark shark = Shark.getInstance();
            ExecutionAdministration ea = shark.getExecutionAdministration();

            ProcessFilterBuilder pieb = shark.getProcessFilterBuilder();
            WMFilter filter = pieb.addIdEquals(sessionHandle, procInstanceId);

            ea.reevaluateAssignmentsWithFiltering(sessionHandle, filter, true);


        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Reevaluate assignments for an array of processes based on the process instance IDs.
     * @param procInstanceId
     */
    public void reevaluateAssignmentsForProcesses(String[] procInstanceId) {

        SharkConnection sc = null;

        try {

            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            ExecutionAdministration ea = Shark.getInstance().getExecutionAdministration();

            ea.reevaluateAssignmentsForProcesses(sessionHandle, procInstanceId, true);


        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Deletes a process instance.
     * @param procInstanceId
     */
    public void removeProcessInstance(String procInstanceId) {

        SharkConnection sc = null;

        try {

            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            Shark shark = Shark.getInstance();
            ExecutionAdministration ea = shark.getExecutionAdministration();

            WfProcessIterator pi = sc.get_iterator_process();
            ProcessFilterBuilder pieb = shark.getProcessFilterBuilder();
            WMFilter filter = pieb.addIdEquals(sessionHandle, procInstanceId);

            pi.set_query_expression(pieb.toIteratorExpression(sessionHandle, filter));

            WfProcess[] wfProcessList = pi.get_next_n_sequence(0);
            WfProcess wfProcess = null;
            if (wfProcessList != null && wfProcessList.length > 0) {
                wfProcess = wfProcessList[0];
            }

            if (wfProcess != null && !wfProcess.state().startsWith(SharkConstants.STATEPREFIX_CLOSED)) {
                WAPI wapi = Shark.getInstance().getWAPIConnection();
                wapi.terminateProcessInstance(sessionHandle, procInstanceId);
            }

            ea.deleteProcessesWithFiltering(sessionHandle, filter);


        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Abort a process instance.
     * @param processId
     * @return
     */
    public boolean processAbort(String processId) {

        SharkConnection sc = null;
        boolean aborted = false;

        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            WAPI wapi = shark.getWAPIConnection();
            WMSessionHandle sessionHandle = sc.getSessionHandle();

            wapi.abortProcessInstance(sessionHandle, processId);
            aborted = true;

            // audit trail for aborted activity instances
            Collection<WorkflowActivity> activityList = getActivityList(processId, 0, 1000, null, false); //getProcessActivityInstanceList(processId);
            for (WorkflowActivity activity : activityList) {
                WorkflowUtil.addAuditTrail(this.getClass().getName(), "processAbort", activity.getId());
            }
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
            return aborted;
        }
    }

    /**
     * Create a process instance without starting any activities.
     * @param processDefId
     * @return The created process instance ID
     */
    public String processCreateWithoutStart(String processDefId) {
        String processInstanceId = null;
        WorkflowProcessResult result = processStart(processDefId, null, null, null, null, true);
        if (result.getProcess() != null) {
            processInstanceId = result.getProcess().getInstanceId();
        }
        return processInstanceId;
    }

    /**
     * Starts a process based on the process definition ID.
     * @param processDefId
     * @return
     */
    public WorkflowProcessResult processStart(String processDefId) {
        return processStart(processDefId, null, null, null, null, false);
    }

    /**
     * Starts a process based on the process definition ID, while setting workflow variables values
     * @param processDefId
     * @param variables
     * @return
     */
    public WorkflowProcessResult processStart(String processDefId, Map<String, String> variables) {
        return processStart(processDefId, null, variables, null, null, false);
    }

    /**
     * Starts a process based on the process definition ID, while setting workflow variables values and start process username.
     * @param processDefId
     * @param variables
     * @param startProcUsername
     * @return
     */
    public WorkflowProcessResult processStart(String processDefId, Map<String, String> variables, String startProcUsername) {
        return processStart(processDefId, null, variables, startProcUsername, null, false);
    }

    /**
     * Starts a process based on the process definition ID, while setting workflow variables values, start process username and parent process id.
     * @param processDefId
     * @param variables
     * @param startProcUsername
     * @param parentProcessId
     * @return
     */
    public WorkflowProcessResult processStartWithLinking(String processDefId, Map<String, String> variables, String startProcUsername, String parentProcessId) {
        return processStart(processDefId, null, variables, startProcUsername, parentProcessId, false);
    }

    /**
     * Start a new process while copying variables, form data and running activities from a previous running process instance.
     * @param currentProcessId The current running process instance
     * @param newProcessDefId The new process definition ID to start
     * @param abortCurrentProcess Set to true to abort the current running process
     * @return
     */
    public WorkflowProcessResult processCopyFromInstanceId(String currentProcessId, String newProcessDefId, boolean abortCurrentProcess) {
        WorkflowProcessResult result = new WorkflowProcessResult();
        Collection<WorkflowActivity> activitiesStarted = new ArrayList<WorkflowActivity>();

        // find running activities in current process
        Collection<WorkflowActivity> activityList = getActivityList(currentProcessId, 0, 1000, null, null);
        for (Iterator<WorkflowActivity> i = activityList.iterator(); i.hasNext();) {
            WorkflowActivity act = i.next();
            if (act.getState() == null || !act.getState().startsWith("open")) {
                i.remove();
            }
        }

        if (activityList.size() > 0) {
            // get current variable values
            Collection<WorkflowVariable> variableList = getProcessVariableList(currentProcessId);
            Map<String, String> variableMap = new HashMap<String, String>();
            for (WorkflowVariable variable : variableList) {
                String val = (variable.getVal() != null) ? variable.getVal().toString() : null;
                variableMap.put(variable.getId(), val);
            }

            // start new process with variable values
            String starter = getUserByProcessIdAndActivityDefId(newProcessDefId, currentProcessId, WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
            result = processStart(newProcessDefId, null, variableMap, starter, currentProcessId, true);
            WorkflowProcess processStarted = result.getProcess();
            if (processStarted != null) {
                String newProcessId = processStarted.getInstanceId();

                // start matching activities
                boolean abortFirstActivity = true;
                for (WorkflowActivity act : activityList) {
                    String activityDef = act.getActivityDefId();
                    boolean actStarted = activityStart(newProcessId, activityDef, abortFirstActivity);
                    abortFirstActivity = false;
                    if (actStarted) {
                        activitiesStarted.add(act);
                    }
                }
                result.setActivities(activitiesStarted);

                // abort old process if required
                if (abortCurrentProcess) {
                    processAbort(currentProcessId);
                }
            }
        }

        // return process result
        return result;
    }

    /**
     * Starts a process based on the process definition ID, while setting workflow variables values, start process username and parent process id.
     * @param processDefId
     * @param variables
     * @param startProcUsername
     * @param parentProcessId
     * @return
     */
    public WorkflowProcessResult processStartWithInstanceId(String processDefId, String processId, Map<String, String> variables) {
        return processStart(processDefId, processId, variables, null, null, false);
    }

    /**
     * Generic method to start a process with various options
     * @param processDefId The process definition ID of the process to start
     * @param processId The process instance ID of a current running process to start
     * @param variables Workflow variables values to set for the process
     * @param startProcUsername The username of the person starting the process
     * @param parentProcessId The process instance ID of a parent or calling process
     * @param startManually Set to true to prevent beginning activities from being started.
     * @return
     */
    public WorkflowProcessResult processStart(String processDefId, String processId, Map<String, String> variables, String startProcUsername, String parentProcessId, boolean startManually) {
        processDefId = getConvertedLatestProcessDefId(processDefId);

        SharkConnection sc = null;

        WorkflowProcessResult result = new WorkflowProcessResult();
        WorkflowProcess processStarted = new WorkflowProcess();
        Collection<WorkflowActivity> activitiesStarted = new ArrayList<WorkflowActivity>();
        String processInstanceId = "";
        try {

            if (startProcUsername != null && startProcUsername.trim().length() > 0) {
                sc = connect(startProcUsername);
            } else {
                sc = connect();
            }

            // start process
            WfProcessMgr mgr = sc.getProcessMgr(processDefId);
            WfProcess wfProcess = null;

            if (processId != null && processId.trim().length() > 0) {
                wfProcess = sc.getProcess(processId);
                processInstanceId = processId;
            } else {
                wfProcess = mgr.create_process(null);
                processInstanceId = wfProcess.key();
            }

            if (variables != null) {
                //set workflow variables if the key is found
                Set<String> keys = variables.keySet();
                if (keys != null && keys.size() > 0) {
                    Map contextSignature = wfProcess.manager().context_signature();
                    for (String key : keys) {
                        Object value = variables.get(key);

                        String signature = (String) contextSignature.get(key);
                        if (signature != null && signature.trim().length() > 0) {
                            if (signature.equals("java.lang.Long")) {
                                value = Long.parseLong((String) value);
                            } else if (signature.equals("java.lang.Boolean")) {
                                value = Long.parseLong((String) value);
                            } else if (signature.equals("java.lang.Double")) {
                                value = Double.parseDouble((String) value);
                            }
                        }

                        this.processVariable(processInstanceId, key, value);
                    }
                }
            }

            //process linking
            if (parentProcessId != null && parentProcessId.trim().length() > 0) {
                internalAddWorkflowProcessLink(parentProcessId, processInstanceId);
            }

            if (!startManually) {
                wfProcess.start();
            }

            // set started process in result
            processStarted.setId(processDefId);
            processStarted.setInstanceId(processInstanceId);
            result.setProcess(processStarted);

            //redirect to assignment view accordingly
            if (wfProcess != null && !startManually) {
                Shark shark = Shark.getInstance();
                AdminMisc admin = shark.getAdminMisc();
                WMSessionHandle sessionHandle = sc.getSessionHandle();

                XPDLBrowser xpdl = shark.getXPDLBrowser();
                WfActivity[] activityList = wfProcess.get_sequence_step(0);
                WorkflowActivity activity = getNextActivity(sessionHandle, mgr, admin, xpdl, wfProcess.key(), activityList);
                
                if (activity != null) {
                    activitiesStarted.add(activity);
                    result.setActivities(activitiesStarted);
                }
            }
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return result;
    }

    /**
     * Check an assignment is exist or not based on an activity instance ID.
     * @param activityId
     * @return
     */
    public Boolean isAssignmentExist(String activityId) {

        if (activityId == null || activityId.trim().length() == 0) {
            return false;
        }

        SharkConnection sc = null;

        try {
            sc = connect();

            WfAssignment wfa = getSharkAssignment(sc, activityId);
            if (wfa != null) {
                return true;
            }
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        return false;
    }

    /**
     * Returns an assignment for the current user based on an activity instance ID.
     * @param activityId
     * @return null if the assignment does not exist.
     */
    public WorkflowAssignment getAssignment(String activityId) {

        SharkConnection sc = null;
        WorkflowAssignment ass = null;

        try {

            sc = connect();

            WfAssignment wfa = getSharkAssignment(sc, activityId);
            if (wfa != null) {
                WfActivity activity = wfa.activity();
                WfResource assignee = wfa.assignee();
                WfProcess process = activity.container();
                WfProcessMgr manager = process.manager();
                ass = new WorkflowAssignment();
                ass.setAccepted(wfa.get_accepted_status());
                ass.setActivityId(activity.key());
                ass.setActivityName(activity.name());
                ass.setAssigneeId(assignee.resource_key());
                ass.setAssigneeName(assignee.resource_name());
                ass.setDescription(activity.description());
                ass.setPriority(new Short(activity.priority()).toString());
                ass.setProcessId(process.key());
                ass.setProcessName(process.name());
                ass.setProcessVersion(manager.version());
                ass.setProcessDefId(manager.name());
                WorkflowActivity wfActivity = getRunningActivityInfo(activityId);
                ass.setDateCreated(wfActivity.getCreatedTime());
                ass.setDueDate(wfActivity.getDue());

                ass.setAssigneeList(getAssignmentResourceIds(ass.getProcessDefId(), ass.getProcessId(), ass.getActivityId()));

                if (WorkflowUtil.containsHashVariable(ass.getActivityName())) {
                    Collection<WorkflowVariable> variableList = JSPClientUtilities.getVariableData(sc, activity, false);
                    ass.setProcessVariableList((List<WorkflowVariable>) variableList);

                    //process activity name variable
                    ass.setActivityName(WorkflowUtil.processVariable(ass.getActivityName(), null, ass));
                }

                Shark shark = Shark.getInstance();
                AdminMisc admin = shark.getAdminMisc();
                WMSessionHandle sessionHandle = sc.getSessionHandle();
                WMEntity ent = admin.getActivityDefinitionInfo(sessionHandle, process.key(), activity.key());
                ass.setActivityDefId(ent.getId());
                ass.setProcessRequesterId(getUserByProcessIdAndActivityDefId(ass.getProcessDefId(), ass.getProcessId(), WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS));

                WfRequester requester = process.requester();
                if (requester != null) {
                    boolean isSubflow = (requester instanceof WfActivity);
                    if (isSubflow) {
                        WfActivity act = (WfActivity) requester;
                        ass.setSubflow(true);
                        if (getWorkflowProcessLink(process.key()) == null) {
                            internalAddWorkflowProcessLink(act.container().key(), process.key());
                        }
                    }
                }

                // get participant
                Collection<WorkflowAssignment> tempList = new ArrayList<WorkflowAssignment>();
                tempList.add(ass);
                participantsForAssignment(tempList);
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        return ass;
    }

    /**
     * Returns a mock assignment based on an activity instance ID.
     * @param activityId
     * @return null if the assignment does not exist.
     */
    public WorkflowAssignment getMockAssignment(String activityId) {
        WorkflowAssignment mockAss = new WorkflowAssignment();

        WorkflowActivity act = getActivityById(activityId);

        if (act != null && activityId.equals(act.getId())) {
            mockAss.setProcessId(act.getProcessId());
            mockAss.setProcessDefId(act.getProcessDefId());
            mockAss.setProcessName(act.getProcessName());
            mockAss.setProcessVersion(act.getProcessVersion());
            mockAss.setProcessRequesterId(getUserByProcessIdAndActivityDefId(act.getProcessDefId(), act.getProcessId(), WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS));
            mockAss.setDescription(act.getDescription());
            mockAss.setActivityId(activityId);
            mockAss.setActivityName(act.getName());
            mockAss.setActivityDefId(act.getActivityDefId());
            mockAss.setAssigneeId(act.getPerformer());

            mockAss.setProcessVariableList(new ArrayList(getProcessVariableList(act.getProcessId())));

            return mockAss;
        }

        return null;
    }

    /**
     * Returns the first assignment for the current user based on a process instance ID.
     * @param processId
     * @return null if the assignment does not exist.
     */
    public WorkflowAssignment getAssignmentByProcess(String processId) {

        SharkConnection sc = null;
        WorkflowAssignment ass = null;

        if (processId != null && !processId.trim().isEmpty()) {
            try {

                sc = connect();

                WfProcess wfProcess = sc.getProcess(processId);
                if (wfProcess != null) {
                    Shark shark = Shark.getInstance();
                    AdminMisc admin = shark.getAdminMisc();
                    WMSessionHandle sessionHandle = sc.getSessionHandle();
                    XPDLBrowser xpdl = shark.getXPDLBrowser();

                    WfActivity[] activityList = wfProcess.get_sequence_step(0);
                    WorkflowActivity activity = getNextActivity(sessionHandle, wfProcess.manager(), admin, xpdl, wfProcess.key(), activityList);
                    if (activity != null) {
                        ass = getAssignment(activity.getId());
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, "");
            } finally {
                try {
                    disconnect(sc);
                } catch (Exception e) {
                    LogUtil.error(getClass().getName(), e, "");
                }
            }
        }
        return ass;
    }

    /**
     * Returns a list of assignments for the current user.
     * @param accepted
     * @param processDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public Collection<WorkflowAssignment> getAssignmentList(Boolean accepted, String processDefId, String sort, Boolean desc, Integer start, Integer rows) {
        SharkConnection sc = null;
        Collection<WorkflowAssignment> assignmentList = new ArrayList<WorkflowAssignment>();

        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            AssignmentFilterBuilder aieb = shark.getAssignmentFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            // filter by user
            String username = getWorkflowUserManager().getCurrentUsername();
            WMFilter filter = aieb.addUsernameEquals(sessionHandle, username);

            // filter by acceptance
            if (accepted != null && accepted.booleanValue()) {
                filter = aieb.and(sessionHandle, filter, aieb.addIsAccepted(sessionHandle));
            } else if (accepted != null && !accepted.booleanValue()) {
                WMFilter tmpFilter = aieb.and(sessionHandle, filter, aieb.addIsAccepted(sessionHandle));
                tmpFilter = aieb.not(sessionHandle, tmpFilter);
                filter = aieb.and(sessionHandle, filter, tmpFilter);
            }

            // filter by process id
            if (processDefId != null && processDefId.trim().length() > 0) {
                String processKey = MiscUtilities.getProcessMgrProcDefId(processDefId);
                String processVersion = MiscUtilities.getProcessMgrVersion(processDefId);
                filter = aieb.and(sessionHandle, filter, aieb.addProcessDefIdEquals(sessionHandle, processKey));
                filter = aieb.and(sessionHandle, filter, aieb.addPackageVersionEquals(sessionHandle, processVersion));
            }

            // set sort
            if (sort != null && sort.trim().length() > 0) {
                boolean asc = (desc == null) || !desc;
                filter = aieb.setOrderByCreatedTime(sessionHandle, filter, asc);
            }

            if (start != null) {
                filter.setStartPosition(start);
            }

            if (rows != null && rows > 0) {
                filter.setLimit(rows);
            }

            // execute
            WfAssignmentIterator ai = sc.get_iterator_assignment();
            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
            WfAssignment[] wItems = ai.get_next_n_sequence(0);
            for (int i = 0; i < wItems.length; ++i) {
                WfAssignment wfa = wItems[i];
                boolean acceptedStatus = wfa.get_accepted_status();
                WorkflowAssignment ass = new WorkflowAssignment();
                ass.setAccepted(acceptedStatus);
                WfActivity activity = wfa.activity();
                WfResource assignee = wfa.assignee();
                WfProcess process = activity.container();
                WfProcessMgr manager = process.manager();
                ass.setActivityId(activity.key());
                ass.setActivityName(activity.name());
                ass.setAssigneeId(assignee.resource_key());
                ass.setAssigneeName(assignee.resource_name());
                ass.setDescription(activity.description());
                ass.setPriority(new Short(activity.priority()).toString());
                ass.setProcessId(process.key());
                ass.setProcessName(process.name());
                ass.setProcessVersion(manager.version());
                ass.setProcessDefId(manager.name());
                WorkflowActivity wfActivity = getRunningActivityInfo(activity.key());
                ass.setDateCreated(wfActivity.getCreatedTime());
                ass.setDueDate(wfActivity.getDue());

                WfRequester requester = process.requester();
                if (requester != null) {
                    boolean isSubflow = (requester instanceof WfActivity);
                    if (isSubflow) {
                        WfActivity act = (WfActivity) requester;
                        ass.setSubflow(true);
                        ass.setProcessRequesterId(act.container().key());
                        if (getWorkflowProcessLink(process.key()) == null) {
                            internalAddWorkflowProcessLink(act.container().key(), process.key());
                        }
                    }
                }

                assignmentList.add(ass);
            }

            // set participant info
            participantsForAssignment(assignmentList);

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return assignmentList;
    }

    /**
     * Returns a list of assignments for the current user.
     * @param packageId
     * @param processDefId
     * @param processId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public Collection<WorkflowAssignment> getAssignmentList(String packageId, String processDefId, String processId, String sort, Boolean desc, Integer start, Integer rows) {
        return getAssignmentList(packageId, processDefId, processId, null, sort, desc, start, rows);
    }

    /**
     * Returns a list of assignments for the current user.
     * @param packageId
     * @param processDefId
     * @param processId
     * @param activityDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public Collection<WorkflowAssignment> getAssignmentList(String packageId, String processDefId, String processId, String activityDefId, String sort, Boolean desc, Integer start, Integer rows) {
        if (processDefId != null) {
            processDefId = getConvertedLatestProcessDefId(processDefId);
        }
        
        SharkConnection sc = null;
        Collection<WorkflowAssignment> assignmentList = new ArrayList<WorkflowAssignment>();

        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            AssignmentFilterBuilder aieb = shark.getAssignmentFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            // filter by user
            String username = getWorkflowUserManager().getCurrentUsername();
            WMFilter filter = aieb.addUsernameEquals(sessionHandle, username);

            // filter by package id
            if (packageId != null && packageId.trim().length() > 0) {
                filter = aieb.and(sessionHandle, filter, aieb.addPackageIdEquals(sessionHandle, packageId));
            }

            // filter by process definition id
            if (processDefId != null && processDefId.trim().length() > 0) {
                String processKey = MiscUtilities.getProcessMgrProcDefId(processDefId);
                String processVersion = MiscUtilities.getProcessMgrVersion(processDefId);
                filter = aieb.and(sessionHandle, filter, aieb.addProcessDefIdEquals(sessionHandle, processKey));
                filter = aieb.and(sessionHandle, filter, aieb.addPackageVersionEquals(sessionHandle, processVersion));
            }

            // filter by process instance id
            if (processId != null && processId.trim().length() > 0) {
                filter = aieb.and(sessionHandle, filter, aieb.addProcessIdEquals(sessionHandle, processId));
            }

            // filter by activityDefId
            if (activityDefId != null && activityDefId.trim().length() > 0) {
                filter = aieb.and(sessionHandle, filter, aieb.addActivityDefIdEquals(sessionHandle, activityDefId));
            }

            // set sort
            if (sort != null && sort.trim().length() > 0) {
                boolean asc = (desc == null) || !desc;
                filter = aieb.setOrderByCreatedTime(sessionHandle, filter, asc);
            }

            if (start != null) {
                filter.setStartPosition(start);
            }

            if (rows != null && rows > 0) {
                filter.setLimit(rows);
            }

            // execute
            WfAssignmentIterator ai = sc.get_iterator_assignment();
            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
            WfAssignment[] wItems = ai.get_next_n_sequence(0);
            for (int i = 0; i < wItems.length; ++i) {
                WfAssignment wfa = wItems[i];
                boolean acceptedStatus = wfa.get_accepted_status();
                WorkflowAssignment ass = new WorkflowAssignment();
                ass.setAccepted(acceptedStatus);
                WfActivity activity = wfa.activity();
                WfResource assignee = wfa.assignee();
                WfProcess process = activity.container();
                WfProcessMgr manager = process.manager();
                ass.setActivityId(activity.key());
                ass.setActivityName(activity.name());
                ass.setAssigneeId(assignee.resource_key());
                ass.setAssigneeName(assignee.resource_name());
                ass.setDescription(activity.description());
                ass.setPriority(new Short(activity.priority()).toString());
                ass.setProcessId(process.key());
                ass.setProcessName(process.name());
                ass.setProcessVersion(manager.version());
                ass.setProcessDefId(manager.name());
                WorkflowActivity wfActivity = getRunningActivityInfo(sc, activity, false);
                ass.setDateCreated(wfActivity.getCreatedTime());
                ass.setDueDate(wfActivity.getDue());

                // check for hash variable
                if (WorkflowUtil.containsHashVariable(ass.getActivityName())) {
                    Collection<WorkflowVariable> variableList = JSPClientUtilities.getVariableData(sc, activity, false);
                    ass.setProcessVariableList((List<WorkflowVariable>) variableList);

                    //process activity name variable
                    ass.setActivityName(WorkflowUtil.processVariable(ass.getActivityName(), null, ass));
                }

                WfRequester requester = process.requester();
                if (requester != null) {
                    boolean isSubflow = (requester instanceof WfActivity);
                    if (isSubflow) {
                        WfActivity act = (WfActivity) requester;
                        ass.setSubflow(true);
                        ass.setProcessRequesterId(act.container().key());
                        if (getWorkflowProcessLink(process.key()) == null) {
                            internalAddWorkflowProcessLink(act.container().key(), process.key());
                        }
                    }
                }
                assignmentList.add(ass);
            }

            // set participant info
            participantsForAssignment(assignmentList);

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return assignmentList;
    }

    /**
     * Returns a list of assignments for the current user filter by processDefIds.
     * @param processDefIds
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public Collection<WorkflowAssignment> getAssignmentListFilterByProccessDefIds(String[] processDefIds, String sort, Boolean desc, Integer start, Integer rows) {
        SharkConnection sc = null;
        Collection<WorkflowAssignment> assignmentList = new ArrayList<WorkflowAssignment>();

        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            AssignmentFilterBuilder aieb = shark.getAssignmentFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            // filter by user
            String username = getWorkflowUserManager().getCurrentUsername();
            WMFilter filter = aieb.addUsernameEquals(sessionHandle, username);

            // filter by processDefIds
            if (processDefIds != null && processDefIds.length > 0) {
                WMFilter idsFilter = null;
                int i = 0;
                for (String id : processDefIds) {
                    String temp[] = id.split("#");

                    WMFilter tempFilter = aieb.addPackageIdEquals(sessionHandle, temp[0]);
                    tempFilter = aieb.and(sessionHandle, tempFilter, aieb.addPackageVersionEquals(sessionHandle, temp[1]));
                    tempFilter = aieb.and(sessionHandle, tempFilter, aieb.addProcessDefIdEquals(sessionHandle, temp[2]));

                    if (i == 0) {
                        idsFilter = tempFilter;
                        i++;
                    } else {
                        idsFilter = aieb.or(sessionHandle, idsFilter, tempFilter);
                    }
                }

                filter = aieb.and(sessionHandle, filter, idsFilter);
            }

            // set sort
            if (sort != null && sort.trim().length() > 0) {
                boolean asc = (desc == null) || !desc;
                filter = aieb.setOrderByCreatedTime(sessionHandle, filter, asc);
            }

            if (start != null) {
                filter.setStartPosition(start);
            }

            if (rows != null && rows > 0) {
                filter.setLimit(rows);
            }

            // execute
            WfAssignmentIterator ai = sc.get_iterator_assignment();
            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
            WfAssignment[] wItems = ai.get_next_n_sequence(0);
            for (int i = 0; i < wItems.length; ++i) {
                WfAssignment wfa = wItems[i];
                boolean acceptedStatus = wfa.get_accepted_status();
                WorkflowAssignment ass = new WorkflowAssignment();
                ass.setAccepted(acceptedStatus);
                WfActivity activity = wfa.activity();
                WfResource assignee = wfa.assignee();
                WfProcess process = activity.container();
                WfProcessMgr manager = process.manager();
                ass.setActivityId(activity.key());
                ass.setActivityName(activity.name());
                ass.setAssigneeId(assignee.resource_key());
                ass.setAssigneeName(assignee.resource_name());
                ass.setDescription(activity.description());
                ass.setPriority(new Short(activity.priority()).toString());
                ass.setProcessId(process.key());
                ass.setProcessName(process.name());
                ass.setProcessVersion(manager.version());
                ass.setProcessDefId(manager.name());
                WorkflowActivity wfActivity = getRunningActivityInfo(activity.key());
                ass.setDateCreated(wfActivity.getCreatedTime());
                ass.setDueDate(wfActivity.getDue());

                // check for hash variable
                if (WorkflowUtil.containsHashVariable(ass.getActivityName())) {
                    Collection<WorkflowVariable> variableList = JSPClientUtilities.getVariableData(sc, activity, false);
                    ass.setProcessVariableList((List<WorkflowVariable>) variableList);

                    //process activity name variable
                    ass.setActivityName(WorkflowUtil.processVariable(ass.getActivityName(), null, ass));
                }

                WfRequester requester = process.requester();
                if (requester != null) {
                    boolean isSubflow = (requester instanceof WfActivity);
                    if (isSubflow) {
                        WfActivity act = (WfActivity) requester;
                        ass.setSubflow(true);
                        ass.setProcessRequesterId(act.container().key());
                    }
                }

                assignmentList.add(ass);
            }

            // set participant info
            participantsForAssignment(assignmentList);

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return assignmentList;
    }

    /**
     * Returns the number of assignments for the current user filter by processDefIds.
     * @param processDefIds
     * @return
     */
    public int getAssignmentListFilterByProccessDefIdsSize(String[] processDefIds) {
        SharkConnection sc = null;
        int size = 0;

        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            AssignmentFilterBuilder aieb = shark.getAssignmentFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            // filter by user
            String username = getWorkflowUserManager().getCurrentUsername();
            WMFilter filter = aieb.addUsernameEquals(sessionHandle, username);

            // filter by processDefIds
            if (processDefIds != null && processDefIds.length > 0) {
                WMFilter idsFilter = null;
                int i = 0;
                for (String id : processDefIds) {
                    String temp[] = id.split("#");

                    WMFilter tempFilter = aieb.addPackageIdEquals(sessionHandle, temp[0]);
                    tempFilter = aieb.and(sessionHandle, tempFilter, aieb.addPackageVersionEquals(sessionHandle, temp[1]));
                    tempFilter = aieb.and(sessionHandle, tempFilter, aieb.addProcessDefIdEquals(sessionHandle, temp[2]));

                    if (i == 0) {
                        idsFilter = tempFilter;
                        i++;
                    } else {
                        idsFilter = aieb.or(sessionHandle, idsFilter, tempFilter);
                    }
                }

                filter = aieb.and(sessionHandle, filter, idsFilter);
            }

            // execute
            WfAssignmentIterator ai = sc.get_iterator_assignment();
            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));

            return ai.how_many();
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return size;
    }

    /**
     * Returns the all (pending and accepted) assignments for the current user
     * @param packageId
     * @param processDefId
     * @param processId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public PagedList<WorkflowAssignment> getAssignmentPendingAndAcceptedList(String packageId, String processDefId, String processId, String sort, Boolean desc, Integer start, Integer rows) {
        List<WorkflowAssignment> assignmentList = (List<WorkflowAssignment>) getAssignmentList(packageId, processDefId, processId, sort, desc, start, rows);

        // set total
        Integer total = new Integer(getAssignmentSize(packageId, processDefId, processId));

        // perform sorting and paging
        PagedList<WorkflowAssignment> pagedList = new PagedList<WorkflowAssignment>(assignmentList, sort, desc, start, rows, total);

        return pagedList;
    }

    /**
     * Returns pending assignments for the current user
     * @param processDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public PagedList<WorkflowAssignment> getAssignmentPendingList(String processDefId, String sort, Boolean desc, Integer start, Integer rows) {
        List<WorkflowAssignment> assignmentList = (List<WorkflowAssignment>) getAssignmentList(Boolean.FALSE, processDefId, sort, desc, start, rows);

        // set total
        Integer total = new Integer(getAssignmentSize(Boolean.FALSE, processDefId));

        // perform sorting and paging
        PagedList<WorkflowAssignment> pagedList = new PagedList<WorkflowAssignment>(assignmentList, sort, desc, start, rows, total);

        return pagedList;
    }

    /**
     * Returns accepted assignments for the current user
     * @param processDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    public PagedList<WorkflowAssignment> getAssignmentAcceptedList(String processDefId, String sort, Boolean desc, Integer start, Integer rows) {
        List<WorkflowAssignment> assignmentList = (List<WorkflowAssignment>) getAssignmentList(Boolean.TRUE, processDefId, sort, desc, start, rows);

        // set total
        Integer total = new Integer(getAssignmentSize(Boolean.TRUE, processDefId));

        // perform sorting and paging
        PagedList<WorkflowAssignment> pagedList = new PagedList<WorkflowAssignment>(assignmentList, sort, desc, start, rows, total);

        return pagedList;
    }

    /**
     * Returns the number of assignments for the current user.
     * @param accepted
     * @param processDefId
     * @return
     */
    public int getAssignmentSize(Boolean accepted, String processDefId) {
        SharkConnection sc = null;
        int size = 0;

        try {

            sc = connect();

            AssignmentQuery aq = new AssignmentQuery();
            // filter by user
            String username = getWorkflowUserManager().getCurrentUsername();
            aq.setQueryResourceId(username);

            // filter by process def id
            if (processDefId != null && processDefId.trim().length() > 0) {
                aq.setQueryActivityProcessDefName(processDefId);
            }

            // filter by acceptance
            if (accepted != null) {
                aq.setQueryIsAccepted(accepted.booleanValue());
            }

            size = aq.getCount();

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return size;
    }

    /**
     * Returns the number of assignments for the current user.
     * @param packageId
     * @return
     */
    public int getAssignmentSize(String packageId, String processDefId, String processId) {
        return getAssignmentSize(packageId, processDefId, processId, null);
    }

    /**
     * Returns the number of assignments for the current user.
     * @param packageId
     * @return
     */
    public int getAssignmentSize(String packageId, String processDefId, String processId, String activityDefId) {
        if (processDefId != null) {
            processDefId = getConvertedLatestProcessDefId(processDefId);
        }
        
        SharkConnection sc = null;
        int size = 0;

        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            AssignmentFilterBuilder aieb = shark.getAssignmentFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            // filter by user
            String username = getWorkflowUserManager().getCurrentUsername();
            WMFilter filter = aieb.addUsernameEquals(sessionHandle, username);

            // filter by packageId id
            if (packageId != null && packageId.trim().length() > 0) {
                filter = aieb.and(sessionHandle, filter, aieb.addPackageIdEquals(sessionHandle, packageId));
            }

            // filter by process definition id
            if (processDefId != null && processDefId.trim().length() > 0) {
                String processKey = MiscUtilities.getProcessMgrProcDefId(processDefId);
                String processVersion = MiscUtilities.getProcessMgrVersion(processDefId);
                filter = aieb.and(sessionHandle, filter, aieb.addProcessDefIdEquals(sessionHandle, processKey));
                filter = aieb.and(sessionHandle, filter, aieb.addPackageVersionEquals(sessionHandle, processVersion));
            }

            // filter by process instance id
            if (processId != null && processId.trim().length() > 0) {
                filter = aieb.and(sessionHandle, filter, aieb.addProcessIdEquals(sessionHandle, processId));
            }

            // filter by ActivityDefId
            if (activityDefId != null && activityDefId.trim().length() > 0) {
                filter = aieb.and(sessionHandle, filter, aieb.addActivityDefIdEquals(sessionHandle, activityDefId));
            }

            // execute
            WfAssignmentIterator ai = sc.get_iterator_assignment();
            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));

            return ai.how_many();
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return size;
    }

    /**
     * Accept an assignment (for the current user) based on the activity instance ID.
     * @param activityId
     */
    public void assignmentAccept(String activityId) {

        SharkConnection sc = null;

        try {

            sc = connect();

            WfAssignment wfa = getSharkAssignment(sc, activityId);
            wfa.set_accepted_status(true);

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Withdraw an assignment (for the current user) based on the activity instance ID.
     * @param activityId
     */
    public void assignmentWithdraw(String activityId) {

        SharkConnection sc = null;

        try {

            sc = connect();

            WfAssignment wfa = getSharkAssignment(sc, activityId);
            wfa.set_accepted_status(false);

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Complete an assignment (for the current user) based on the activity instance ID.
     * @param activityId
     */
    public void assignmentComplete(String activityId) {

        SharkConnection sc = null;

        try {
            sc = connect();

            WfAssignment wfa = getSharkAssignment(sc, activityId);
            wfa.activity().complete();

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Complete an assignment while setting workflow variable values
     * @param activityId
     * @param variableMap key=variable name and value=variable value.
     */
    public void assignmentComplete(String activityId, Map<String, String> variableMap) {
        // set workflow variables
        assignmentVariables(activityId, variableMap);

        // complete assignment
        assignmentComplete(activityId);
    }

    /**
     * Abort an activity based on the process instance Id and activity definition ID.
     * @param processId
     * @param activityDefId
     */
    public void activityAbort(String processId, String activityDefId) {

        if (processId == null || processId.trim().length() == 0 || activityDefId == null || activityDefId.trim().length() == 0) {
            return;
        }

        SharkConnection sc = null;

        try {
            sc = connect();

            Shark shark = Shark.getInstance();
            WfActivityIterator ai = sc.get_iterator_activity();
            ActivityFilterBuilder aieb = shark.getActivityFilterBuilder();
            WMSessionHandle sessionHandle = sc.getSessionHandle();

            WMFilter filter = new WMFilter();
            if (activityDefId != null && activityDefId.trim().length() > 0) {
                filter = aieb.addDefinitionIdEquals(sessionHandle, activityDefId);
            }

            WMFilter filter2 = new WMFilter();
            if (processId != null && processId.trim().length() > 0) {
                filter2 = aieb.addProcessIdEquals(sessionHandle, processId);
            }

            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, aieb.and(sessionHandle, filter, filter2)));
            WfActivity[] wfActivityList = ai.get_next_n_sequence(0);

            if (wfActivityList.length > 0) {
                for (WfActivity wfActivity : wfActivityList) {
                    String actState = wfActivity.state();
                    if (SharkConstants.STATE_OPEN_RUNNING.equals(actState) || SharkConstants.STATE_OPEN_NOT_RUNNING_NOT_STARTED.equals(actState)) {
                        wfActivity.abort();
                    }
                }
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Start a specific activity for a running process instance.
     * @param processId
     * @param activityDefId
     * @param abortRunningActivities Set to true to abort the current running activities
     * @return
     */
    public boolean activityStart(String processId, String activityDefId, boolean abortRunningActivities) {

        boolean result = false;
        SharkConnection sc = null;

        try {
            if (processId == null || processId.trim().length() == 0 || activityDefId == null || activityDefId.trim().length() == 0) {
                return false;
            }

            sc = connect();

            WfActivityIterator ai;
            ActivityFilterBuilder aieb;
            WMFilter filter;
            WfActivity[] wfActivityArray;

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            Shark shark = Shark.getInstance();

            // get required activity def to start
            AdminMisc admin = shark.getAdminMisc();
            WMEntity procEntity = admin.getProcessDefinitionInfo(sessionHandle, processId);
            XPDLBrowser xpdlBrowser = shark.getXPDLBrowser();
            WMEntity[] entities = WMEntityUtilities.getOverallActivities(sessionHandle, xpdlBrowser, procEntity);
            WMEntity activityDef = null;
            for (WMEntity ent : entities) {
                if (ent.getId().equals(activityDefId) && "Activity".equals(ent.getType())) {
                    activityDef = ent;
                    break;
                }
            }
            LogUtil.debug(getClass().getName(), "Get required activity definition to start for " + activityDefId + ": " + activityDef);
            if (activityDef == null) {
                return false;
            }

            if (abortRunningActivities) {
                LogUtil.debug(getClass().getName(), "aborting running activities for " + processId);
                // get running activities for process
                ai = sc.get_iterator_activity();
                aieb = shark.getActivityFilterBuilder();
                filter = new WMFilter();
                filter = aieb.addProcessIdEquals(sessionHandle, processId);
                filter = aieb.and(sessionHandle, filter, aieb.addStateStartsWith(sessionHandle, SharkConstants.STATEPREFIX_OPEN));
                ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
                wfActivityArray = ai.get_next_n_sequence(0);

                // abort running activities
                for (WfActivity wfAct : wfActivityArray) {
                    String actState = wfAct.state();
                    if (SharkConstants.STATE_OPEN_RUNNING.equals(actState) || SharkConstants.STATE_OPEN_NOT_RUNNING_NOT_STARTED.equals(actState)) {
                        wfAct.abort();
                    }
                }
            }

            // start required activity
            String blockActivityId = null; // TODO: handle block activity?
            ExecutionAdministration ea = shark.getExecutionAdministration();
            ea.startActivity(sessionHandle, processId, blockActivityId, activityDef);

            result = true;

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "Error starting activity " + activityDefId + " for process " + processId);
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        LogUtil.debug(getClass().getName(), "activityStart for activity " + activityDefId + " in process " + processId + ": " + result);
        return result;
    }

    public void assignmentReassign(String processDefId, String processId, String activityId, String username, String replaceUser) {
        SharkConnection sc = null;

        try {
            sc = connect();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            WfAssignment wfa = null;
            Shark shark = Shark.getInstance();
            AssignmentFilterBuilder aieb = shark.getAssignmentFilterBuilder();

            WMFilter filter = aieb.addActivityIdEquals(sessionHandle, activityId);
            filter = aieb.and(sessionHandle, filter, aieb.addUsernameEquals(sessionHandle, replaceUser));

            // execute
            WfAssignmentIterator ai = sc.get_iterator_assignment();
            ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
            WfAssignment[] wItems = ai.get_next_n_sequence(0);
            if (wItems != null && wItems.length > 0) {
                wfa = wItems[0];
            }

            WfResource res = sc.getResource(username);

            if (res == null) {
                CustomWfResourceImpl.createResource(sessionHandle, username);
                res = sc.getResource(username);
            }

            if (wfa.get_accepted_status()) {
                wfa.set_accepted_status(false);
            }

            if (wfa.assignee() == null || (wfa.assignee() != null && !res.resource_key().equals(wfa.assignee().resource_key()))) {
                wfa.set_assignee(res);
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    public void assignmentForceComplete(String processDefId, String processId, String activityId, String username) {
        SharkConnection sc = null;

        try {
            sc = connect();
            WMSessionHandle sessionHandle = sc.getSessionHandle();
            WfAssignment wfa = getWfAssignmentByActivityId(sc, activityId);
            WfResource res = sc.getResource(username);

            if (res == null) {
                CustomWfResourceImpl.createResource(sessionHandle, username);
                res = sc.getResource(username);
            }

            if (wfa.assignee() == null || (wfa.assignee() != null && !res.resource_key().equals(wfa.assignee().resource_key()))) {
                wfa.set_assignee(res);
            }

            if (!wfa.get_accepted_status()) {
                wfa.set_accepted_status(true);
            }

            wfa.activity().complete();


        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Set workflow variable value based on activity instance ID. This only works when the current user is assigned to the activity.
     * @param activityId
     * @param variableName
     * @param variableValue
     */
    public void assignmentVariable(String activityId, String variableName, String variableValue) {

        SharkConnection sc = null;

        try {

            sc = connect();

            WfAssignment a = getSharkAssignment(sc, activityId);
            if (!JSPClientUtilities.isMine(sc, a)) {
                throw new Exception("I don't own activity " + activityId);
            }

            Map _m = new HashMap();
            Object c = a.activity().process_context().get(variableName);

            if (c instanceof Long) {
                c = new Long(variableValue);
            } else if (c instanceof Boolean) {
                c = Boolean.valueOf(variableValue);
            } else if (c instanceof Double) {
                c = Double.valueOf(variableValue);
            } else {
                c = variableValue;
            }

            _m.put(variableName, c);
            a.activity().set_result(_m);

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Set workflow variables based on activity instance ID. This only works when the current user is assigned to the activity.
     * @param processId
     * @param variableMap key=variable name and value=variable value
     */
    public void assignmentVariables(String activityId, Map<String, String> variableMap) {
        if (variableMap != null) {
            for (Iterator<String> i = variableMap.keySet().iterator(); i.hasNext();) {
                String key = i.next();
                String value = variableMap.get(key);
                assignmentVariable(activityId, key, value);
            }
        }
    }

    /**
     * Returns the name of the user that accepted/completed activity.
     * @param processDef Id Unused for now
     * @param processInstanceId
     * @param activityDefId
     * @return
     */
    public String getUserByProcessIdAndActivityDefId(String processDefId, String processId, String activityDefId) {
        String username = getPerformer(processId, activityDefId);
        if (username == null || username.trim().length() == 0) {
            // look for performer in linked origin process
            WorkflowProcessLink link = getWorkflowProcessLink(processId);
            if (link != null) {
                String originProcessId = link.getOriginProcessId();
                if (originProcessId != null && !originProcessId.equals(processId)) {
                    username = getPerformer(originProcessId, activityDefId);
                }
            }
        }
        return username;
    }

    /**
     * Returns the name of the user that accepted/completed an activity.
     * @param processInstanceId
     * @param activityDefId
     * @return
     */
    protected String getPerformer(String processId, String activityDefId) {
        SharkConnection sc = null;

        try {

            sc = connect();

            Shark shark = Shark.getInstance();
            AdminMisc admin = shark.getAdminMisc();
            WMSessionHandle sessionHandle = sc.getSessionHandle();

            WfProcess process = sc.getProcess(processId);

            if (process != null) {
                if (activityDefId.equals(WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS)) {
                    return admin.getProcessRequesterUsername(sessionHandle, processId);
                } else {
                    WfActivityIterator iter = process.get_iterator_step();

                    for (int i = 0; i < iter.how_many(); i++) {
                        WfActivity activity = iter.get_next_object();

                        if (activity != null) {
                            WMEntity entity = admin.getActivityDefinitionInfo(sessionHandle, activity.container().key(), activity.key());

                            if (entity.getId().equals(activityDefId)) {
                                return admin.getActivityResourceUsername(sessionHandle, activity.container().key(), activity.key());
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }

        return "";
    }

    /**
     * Returns a list of workflow variables for the specified activity instance ID (only if assigned to the current user)
     * @param activityId
     * @return
     */
    public Collection<WorkflowVariable> getAssignmentVariableList(String activityId) {
        Collection<WorkflowVariable> variableList = new ArrayList<WorkflowVariable>();

        SharkConnection sc = null;

        try {

            sc = connect();

            WfAssignment wfa = getSharkAssignment(sc, activityId);
            variableList = JSPClientUtilities.getVariableData(sc, wfa.activity());

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return variableList;
    }

    /**
     * Returns a list of usernames that are assigned to a specific activity instance.
     * @param processId
     * @param processInstanceId
     * @param activityInstanceId
     * @return
     */
    public List<String> getAssignmentResourceIds(String processId, String processInstanceId, String activityInstanceId) {
        SharkConnection sc = null;
        List<String> resourceIds = null;
        try {

            sc = connect();
            WMSessionHandle shandle = sc.getSessionHandle();
            resourceIds = CustomWfActivityWrapper.getAssignmentResourceIds(shandle, processId, processInstanceId, activityInstanceId);

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
        return resourceIds;
    }

    protected SharkConnection connect() throws Exception {
        return connect(null);
    }

    /*--- Internal methods to Shark ---*/
    /**
     * Connect to the Shark engine using the current username.
     * @return
     * @throws Exception
     */
    protected SharkConnection connect(String username) throws Exception {
        SharkConnection sConn = Shark.getInstance().getSharkConnection();
        if (username == null) {
            username = getWorkflowUserManager().getCurrentUsername();
        }
        WMConnectInfo wmconnInfo = new WMConnectInfo(username, username, "WorkflowManager", "");
        sConn.connect(wmconnInfo);
        return sConn;
    }

    /**
     * Disconnect from the Shark engine. Must be called in a finally block.
     * @param sConn
     * @throws Exception
     */
    protected void disconnect(SharkConnection sConn) throws Exception {
        if (sConn != null) {
            sConn.disconnect();
        }
    }

    /**
     * Returns internal process state IDs used by Shark.
     * @return
     * @throws NonUniqueQueryException
     * @throws DataObjectException
     * @throws QueryException
     */
    protected Map getProcessStateMap() throws NonUniqueQueryException, DataObjectException, QueryException {
        // get states
        if (this.processStateMap == null) {
            this.processStateMap = new HashMap();
            for (int i = 0; i < SharkConstants.POSSIBLE_PROCESS_STATES.length; i++) {
                ProcessStateQuery psq = new ProcessStateQuery();
                psq.setQueryKeyValue(SharkConstants.POSSIBLE_PROCESS_STATES[i]);
                psq.requireUniqueInstance();
                ProcessStateDO obj = psq.getNextDO();
                if (obj != null) {
                    processStateMap.put(SharkConstants.POSSIBLE_PROCESS_STATES[i], obj);
                }
            }
        }
        return this.processStateMap;
    }

    /**
     * Populates the 'participant' property for each assignment in the Collection based on the XPDL definition.
     * @param assignmentList
     */
    protected void participantsForAssignment(Collection<WorkflowAssignment> assignmentList) {
        SharkConnection sc = null;

        try {
            sc = connect();

            Shark shark = Shark.getInstance();
            AdminMisc admin = shark.getAdminMisc();
            XPDLBrowser xpdlBrowser = shark.getXPDLBrowser();
            for (Iterator<WorkflowAssignment> i = assignmentList.iterator(); i.hasNext();) {
                WorkflowAssignment assignment = (WorkflowAssignment) i.next();


                // get activity and process
                WMSessionHandle sessionHandle = sc.getSessionHandle();
                WMEntity activityEntity = admin.getActivityDefinitionInfo(sessionHandle, assignment.getProcessId(), assignment.getActivityId());

                // get performer
                WMFilter filter = new WMFilter("Name", WMFilter.EQ, "Performer");
                filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
                WMAttribute[] performers = xpdlBrowser.listAttributes(sessionHandle, activityEntity, filter, true).getArray();
                if (performers.length > 0) {
                    WMAttribute performer = performers[0];

                    // get participant
                    String performerId = performer.getValue().toString();
                    Map<String, WorkflowParticipant> participantMap = getParticipantMap(assignment.getProcessDefId());
                    WorkflowParticipant participant = (WorkflowParticipant) participantMap.get(performerId);
                    if (participant != null) {
                        assignment.setParticipant(participant.getName());
                    }
                }
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    /**
     * Retrieves a Shark assignment object based on an activity instance ID.
     * @param sc
     * @param activityId
     * @return
     * @throws Exception
     */
    protected WfAssignment getSharkAssignment(SharkConnection sc, String activityId) throws Exception {

        Shark shark = Shark.getInstance();
        AssignmentFilterBuilder aieb = shark.getAssignmentFilterBuilder();
        WMSessionHandle sessionHandle = sc.getSessionHandle();
        // filter by user
        String username = getWorkflowUserManager().getCurrentUsername();
        WMFilter filter = aieb.addUsernameEquals(sessionHandle, username);

        // filter by activity id
        filter = aieb.and(sessionHandle, filter, aieb.addActivityIdEquals(sessionHandle, activityId));

        // execute
        WfAssignmentIterator ai = sc.get_iterator_assignment();
        ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
        WfAssignment[] wItems = ai.get_next_n_sequence(0);
        if (wItems != null && wItems.length > 0) {
            return wItems[0];
        } else {
            return null;
        }
    }

    /**
     * Retrieves the next Shark assignment based on a process instance ID.
     * @param sc
     * @param processId
     * @return
     * @throws Exception
     */
    protected WfAssignment getSharkAssignmentByProcess(SharkConnection sc, String processId) throws Exception {

        Shark shark = Shark.getInstance();
        AssignmentFilterBuilder aieb = shark.getAssignmentFilterBuilder();
        WMSessionHandle sessionHandle = sc.getSessionHandle();
        // filter by user
        String username = getWorkflowUserManager().getCurrentUsername();
        WMFilter filter = aieb.addUsernameEquals(sessionHandle, username);

        // filter by process id
        filter = aieb.and(sessionHandle, filter, aieb.addProcessIdEquals(sessionHandle, processId));

        // execute
        WfAssignmentIterator ai = sc.get_iterator_assignment();
        ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
        WfAssignment[] wItems = ai.get_next_n_sequence(0);
        if (wItems != null && wItems.length > 0) {
            return wItems[0];
        } else {
            return null;
        }
    }

    /**
     * Retrieves the next Shark assignment based on a activity ID.
     * @param sc
     * @param activityId
     * @return
     * @throws Exception
     */
    protected WfAssignment getWfAssignmentByActivityId(SharkConnection sc, String activityId) throws Exception {

        Shark shark = Shark.getInstance();
        AssignmentFilterBuilder aieb = shark.getAssignmentFilterBuilder();
        WMSessionHandle sessionHandle = sc.getSessionHandle();

        WMFilter filter = aieb.addActivityIdEquals(sessionHandle, activityId);

        // execute
        WfAssignmentIterator ai = sc.get_iterator_assignment();
        ai.set_query_expression(aieb.toIteratorExpression(sessionHandle, filter));
        WfAssignment[] wItems = ai.get_next_n_sequence(0);
        if (wItems != null && wItems.length > 0) {
            return wItems[0];
        } else {
            return null;
        }
    }

    /*--- Internal methods (audit trail not captured) ---*/
    public void internalUpdateDeadlineChecker() {
        String deadlineCheckerInterval = getSetupManager().getSettingValue("deadlineCheckerInterval");
        long deadlineCheckerIntervalValue = 0;
        if (deadlineCheckerInterval != null && deadlineCheckerInterval.trim().length() > 0) {
            try {
                deadlineCheckerIntervalValue = Long.parseLong(deadlineCheckerInterval);
            } catch (Exception ex) {
                deadlineCheckerIntervalValue = 0;
            }
        }

        long intervalInMillis = deadlineCheckerIntervalValue * 1000;
        DeadlineThreadManager.startThread(intervalInMillis);
    }

    public void internalCheckDeadlines(int instancesPerTransaction, int failuresToIgnore) {
        SharkConnection sc = null;

        try {
            sc = connect();

            WMSessionHandle shandle = sc.getSessionHandle();
            Shark shark = Shark.getInstance();

            shark.getExecutionAdministration().checkDeadlinesWithFiltering(shandle, null);
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    public void internalRemoveProcessOnComplete(String procInstanceId) {
        SharkConnection sc = null;

        try {

            sc = connect();

            WMSessionHandle sessionHandle = sc.getSessionHandle();
            Shark shark = Shark.getInstance();
            ExecutionAdministration ea = shark.getExecutionAdministration();

            WfProcessIterator pi = sc.get_iterator_process();
            ProcessFilterBuilder pieb = shark.getProcessFilterBuilder();
            WMFilter filter = pieb.addIdEquals(sessionHandle, procInstanceId);

            pi.set_query_expression(pieb.toIteratorExpression(sessionHandle, filter));

            WfProcess[] wfProcessList = pi.get_next_n_sequence(0);
            WfProcess wfProcess = null;

            if (wfProcessList != null && wfProcessList.length > 0) {
                wfProcess = wfProcessList[0];
            }

            if (wfProcess != null && wfProcess.state().startsWith(SharkConstants.STATEPREFIX_CLOSED)) {
                WorkflowUtil.addAuditTrail(this.getClass().getName(), "processCompleted", procInstanceId);
                
                Boolean deleteProcessOnCompletion = Boolean.valueOf(WorkflowUtil.getSystemSetupValue("deleteProcessOnCompletion"));
                if (deleteProcessOnCompletion != null && deleteProcessOnCompletion) {
                    ea.deleteProcessesWithFiltering(sessionHandle, filter);
                }
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                disconnect(sc);
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }

    public WorkflowProcessLink getWorkflowProcessLink(String processId) {
        return workflowProcessLinkDao.getWorkflowProcessLink(processId);
    }

    public void internalAddWorkflowProcessLink(String parentProcessId, String processInstanceId) {
        WorkflowProcessLink wfProcessLink = new WorkflowProcessLink();
        WorkflowProcessLink parentWfProcessLink = getWorkflowProcessLink(parentProcessId);
        wfProcessLink.setParentProcessId(parentProcessId);

        if (parentWfProcessLink != null) {
            wfProcessLink.setOriginProcessId(parentWfProcessLink.getOriginProcessId());
        } else {
            wfProcessLink.setOriginProcessId(parentProcessId);
        }

        wfProcessLink.setProcessId(processInstanceId);

        workflowProcessLinkDao.addWorkflowProcessLink(wfProcessLink);
    }

    public void internalDeleteWorkflowProcessLink(WorkflowProcessLink wfProcessLink) {
        workflowProcessLinkDao.delete(wfProcessLink);
    }

    public String getConvertedLatestProcessDefId(String processDefId) {
        if (processDefId.contains(":")) {
            processDefId = processDefId.replaceAll(":", "#");
        }
        
        if (processDefId != null && processDefId.contains(LATEST)) {
            ApplicationContext appContext = WorkflowUtil.getApplicationContext();
            WorkflowHelper workflowMapper = (WorkflowHelper) appContext.getBean("workflowHelper");
        
            String currentVersion = workflowMapper.getPublishedPackageVersion(processDefId.split("#")[0]);

            if (currentVersion != null && currentVersion.trim().length() > 0) {
                processDefId = processDefId.replace(LATEST, currentVersion);
            }
        }
        return processDefId;
    }

    public Boolean isUserInWhiteList(String processDefId) {
        String temp[] = processDefId.split("#");
        if (temp.length != 3) {
            return true;
        }

        processDefId = getConvertedLatestProcessDefId(processDefId);

        String tempInfo[] = processDefId.split("#");

        String currentUsername = getWorkflowUserManager().getCurrentUsername();

        List<String> userlist = WorkflowUtil.getAssignmentUsers(tempInfo[0], processDefId, "", tempInfo[1], "", "", "processStartWhiteList");
        if (userlist != null && userlist.size() > 0) {
            boolean inWhiteList = false;
            for (String username : userlist) {
                if (username.equals(currentUsername)) {
                    inWhiteList = true;
                    break;
                }
            }

            return inWhiteList;
        } else {
            return true;
        }
    }

    protected PackageAdministration getSharkPackageAdmin(WMSessionHandle sessionHandle) throws Exception {
        Shark shark = Shark.getInstance();
        PackageAdministration pa = shark.getPackageAdministration();

        // check for changed profile
        String currentProfile = DynamicDataSourceManager.getCurrentProfile();
        boolean profileChanged = (currentProfile != null && !currentProfile.equals(previousProfile));
        previousProfile = currentProfile;
        if (profileChanged) {
            // clear process definition cache
            synchronized (this) {
                pa.clearXPDLCache(sessionHandle);
            }
        }

        return pa;
    }

    protected Date getDueDateProceedByPlugin(String processId, String activityId, long limitInSecond, Date createdTime, Date startTime) {
        WorkflowDeadline deadline = new WorkflowDeadline();
        deadline.setDeadlineLimit((int) limitInSecond * 1000);

        ApplicationContext appContext = WorkflowUtil.getApplicationContext();
        WorkflowHelper workflowMapper = (WorkflowHelper) appContext.getBean("workflowHelper");

        WorkflowDeadline newDeadline = workflowMapper.executeDeadlinePlugin(processId, activityId, deadline, null, startTime, createdTime);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createdTime);
        calendar.add(Calendar.MILLISECOND, newDeadline.getDeadlineLimit());
        return calendar.getTime();
    }

    protected double getServiceLevelValue(Date startedDate, Date finishDate, Date dueDate) {
        Date todayDate = new Date();
        if (startedDate != null && dueDate != null) {
            try {
                long currentTime = todayDate.getTime();
                long startedTime = startedDate.getTime();
                long dueTime = dueDate.getTime();

                if (finishDate != null) {
                    long completedTime = finishDate.getTime();
                    if (completedTime < dueTime) {
                        return 100 - ((((double) (completedTime - startedTime)) / ((double) (dueTime - startedTime))) * 100);
                    } else {
                        return 0;
                    }
                } else {
                    if (currentTime < dueTime) {
                        return 100 - ((((double) (currentTime - startedTime)) / ((double) (dueTime - startedTime))) * 100);
                    } else {
                        return 0;
                    }
                }
            } catch (Exception e) {
                LogUtil.error(WorkflowManagerImpl.class.getName(), e, "");
            }
        }
        return -1;
    }
    
    protected String convertTimeInSecondsToString(long timeInSeconds) {
        long timeInMinutes = (long) timeInSeconds / 60;
        long timeInHours = (long) timeInMinutes / 60;
        long timeInDays = (long) timeInHours / 24;
                    
        String temp = "";
        
        if (timeInSeconds < 60) {
            temp = timeInSeconds + " second(s)";
        } else if (timeInSeconds >= 60 && timeInMinutes < 60) {
            temp = timeInMinutes + " minutes(s) " + (timeInSeconds % 60) + " second(s)";
        } else if (timeInMinutes >= 60 && timeInHours < 24) {
            temp = timeInHours + " hour(s) " + (timeInMinutes % 60) + " minute(s) " + (timeInSeconds % 60) + " second(s)";
        } else if (timeInHours >= 24) {
            temp = timeInDays + " day(s) " + (timeInHours % 24) + " hour(s) " + (timeInMinutes % 60) + " minutes(s) " + (timeInSeconds % 60) + " second(s)";
        }
        
        return temp;
    }
    
    protected WorkflowActivity getNextActivity(WMSessionHandle sessionHandle, WfProcessMgr mgr, AdminMisc admin, XPDLBrowser xpdl, String processId, WfActivity[] activityList) {
        try {
            for (WfActivity wfAct : activityList) {
                String activityId = wfAct.key();
                WMEntity activityEntity = admin.getActivityDefinitionInfo(sessionHandle, processId, activityId);

                //check for tool
                WMEntityIterator activityEntityIterator = xpdl.listEntities(sessionHandle, activityEntity, null, true);
                while (activityEntityIterator.hasNext()) {
                    WMEntity entity = (WMEntity) activityEntityIterator.next();
                    if (entity.getType().equalsIgnoreCase("tool") || entity.getType().equalsIgnoreCase("route")) {
                        break;
                    } else if (entity.getType().equalsIgnoreCase("subflow")) { //redirect to the first activity id from sub flow
                        WfProcess[] wfProcesses = wfAct.get_sequence_performer(0);
                        WfProcess wfProcess = (wfProcesses.length > 0 ? wfProcesses[0] : null);
                        if (wfProcess != null) {
                            WfActivity[] wfActivityTempList = wfProcess.get_sequence_step(0);
                            return getNextActivity(sessionHandle, mgr, admin, xpdl, wfProcess.key(), wfActivityTempList);
                        }
                    } else {
                        WfAssignment ass = getSharkAssignment(connect(), activityId);
                        if (ass != null) {
                            WorkflowActivity activityStarted = new WorkflowActivity();
                            activityStarted.setId(activityId);
                            return activityStarted;
                        }
                    }
                }
            }
        } catch (Exception e) {}
        return null;
    }
}
