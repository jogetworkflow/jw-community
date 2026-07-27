package org.joget.governance.lib;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AbstractAppVersionedObject;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.DependenciesUtil;
import org.joget.apps.app.service.DependenciesUtil.NamedDefinitionList;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.dao.FormDataDaoImpl;
import org.joget.apps.form.service.CustomFormDataTableUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckAction;
import org.joget.governance.model.GovHealthCheckActionProvider;
import org.joget.governance.model.GovHealthCheckActionResult;
import org.joget.governance.model.GovHealthCheckResult;

public class OrphanedFormDataCheck extends GovHealthCheckAbstract implements GovHealthCheckActionProvider {
    
    private List<GovHealthCheckAction> actions;

    @Override
    public String getName() {
        return "OrphanedFormDataCheck";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "Orphaned Form Data";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    @Override
    public String getCategory() {
        return ResourceBundleUtil.getMessage("governance.security");
    }

    @Override
    public String getSortPriority() {
        return "5";
    }

    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        result.setSuppressable(true);
        
        Set<String> tables = getOrphanedTables();
        
        if (!tables.isEmpty()) {
            result.setStatus(GovHealthCheckResult.Status.WARN);
            String list = "<ol><li>" + FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + StringUtils.join(tables, "</li><li>" + FormDataDaoImpl.FORM_PREFIX_TABLE_NAME) + "</li></ol>";
            result.addDetail(ResourceBundleUtil.getMessage("orphanedFormDataCheck.warn", new String[]{list}));
        } else {
            result.setStatus(GovHealthCheckResult.Status.PASS);
        }

        return result;
    }
    
    protected Set<String> getOrphanedTables() {
        Set<String> tables = getTables();
        if (tables.isEmpty()) {
            return tables;
        }

        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        Collection<AppDefinition> latestAppDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", Boolean.FALSE, null, null);
        Collection<AppDefinition> appDefinitions = getUniqueAppDefinitions(appDefinitionList, latestAppDefinitionList);

        removeDefinedTables(appDefinitions, tables);

        if (!tables.isEmpty()) {
            //find usages
            for (AppDefinition appDef : appDefinitions) {
                if (tables.isEmpty()) {
                    break;
                }
                try {
                    checkDefinitionUsages(appDef, tables);
                } catch (Exception e) {
                    LogUtil.error(OrphanedFormDataCheck.class.getName(), e, "Failed checking usages for app " + appDef.getAppId() + "::" + appDef.getVersion());
                }
            }
        }
        return tables;
    }

    private static Collection<AppDefinition> getUniqueAppDefinitions(Collection<AppDefinition> appDefinitionList, Collection<AppDefinition> latestAppDefinitionList) {
        Map<String, AppDefinition> appDefinitions = new LinkedHashMap<String, AppDefinition>();
        addUniqueAppDefinitions(appDefinitions, appDefinitionList);
        addUniqueAppDefinitions(appDefinitions, latestAppDefinitionList);
        return appDefinitions.values();
    }

    private static void addUniqueAppDefinitions(Map<String, AppDefinition> appDefinitions, Collection<AppDefinition> appDefinitionList) {
        if (appDefinitionList != null) {
            for (AppDefinition appDef : appDefinitionList) {
                String appDefinitionKey = appDef.getAppId() + "::" + appDef.getVersion();
                if (!appDefinitions.containsKey(appDefinitionKey)) {
                    appDefinitions.put(appDefinitionKey, appDef);
                }
            }
        }
    }

    private static void removeDefinedTables(Collection<AppDefinition> appDefinitionList, Set<String> tables) {
        if (appDefinitionList != null) {
            for (AppDefinition appDef : appDefinitionList) {
                if (tables.isEmpty()) {
                    break;
                }
                removeDefinedTables(appDef, tables);
            }
        }
    }

    private static void removeDefinedTables(AppDefinition appDef, Set<String> tables) {
        if (tables.isEmpty()) {
            return;
        }

        if (appDef.getFormDefinitionList() != null) {
            for (FormDefinition f : appDef.getFormDefinitionList()) {
                tables.remove(f.getTableName());
            }
        }

        //handle custom added form data table
        if (appDef.getBuilderDefinitionList() != null) {
            for (BuilderDefinition c : appDef.getBuilderDefinitionList()) {
                if (CustomFormDataTableUtil.TYPE.equals(c.getType())) {
                    tables.remove(c.getName());
                }
            }
        }
    }

    /**
     * Removes tables referenced by app definition JSON/plugin property fields
     * without exporting the full app definition XML.
     *
     * @param appDef app definition to scan
     * @param tables table names without the app_fd_ prefix
     */
    protected static void checkDefinitionUsages(AppDefinition appDef, Set<String> tables) {
        if (appDef == null || tables.isEmpty()) {
            return;
        }

        for (NamedDefinitionList list : DependenciesUtil.getJsonDefinitionLists(appDef)) {
            if (tables.isEmpty()) {
                break;
            }
            checkJsonUsages(list.getDefinitions(), tables);
        }
        checkPluginDefaultPropertiesUsages(appDef.getPluginDefaultPropertiesList(), tables);
        checkPackageDefinitionUsages(appDef.getPackageDefinitionList(), tables);
    }

    private static void checkJsonUsages(Collection<? extends AbstractAppVersionedObject> appObjectList, Set<String> tables) {
        if (tables.isEmpty()) {
            return;
        }
        
        if (appObjectList != null) {
            for (AbstractAppVersionedObject appObject : appObjectList) {
                if (tables.isEmpty()) {
                    break;
                }
                checkTextUsage(appObject.getJson(), tables);
            }
        }
    }

    private static void checkPluginDefaultPropertiesUsages(Collection<PluginDefaultProperties> pluginDefaultPropertiesList, Set<String> tables) {
        if (tables.isEmpty()) {
            return;
        }
        
        if (pluginDefaultPropertiesList != null) {
            for (PluginDefaultProperties pluginDefaultProperties : pluginDefaultPropertiesList) {
                if (tables.isEmpty()) {
                    break;
                }
                checkTextUsage(pluginDefaultProperties.getPluginProperties(), tables);
            }
        }
    }

    private static void checkPackageDefinitionUsages(Collection<PackageDefinition> packageDefinitionList, Set<String> tables) {
        if (tables.isEmpty()) {
            return;
        }
        
        if (packageDefinitionList != null) {
            for (PackageDefinition packageDef : packageDefinitionList) {
                if (tables.isEmpty()) {
                    break;
                }
                if (packageDef.getPackageActivityPluginMap() != null) {
                    for (PackageActivityPlugin activityPlugin : packageDef.getPackageActivityPluginMap().values()) {
                        if (tables.isEmpty()) {
                            break;
                        }
                        checkTextUsage(activityPlugin.getPluginProperties(), tables);
                    }
                }
                if (packageDef.getPackageParticipantMap() != null) {
                    for (PackageParticipant participant : packageDef.getPackageParticipantMap().values()) {
                        if (tables.isEmpty()) {
                            break;
                        }
                        checkTextUsage(participant.getPluginProperties(), tables);
                    }
                }
            }
        }
    }

    private static void checkTextUsage(String text, Set<String> tables) {
        if (StringUtils.isNotEmpty(text) && !tables.isEmpty()) {
            removeMatchedTables(text, tables);
        }
    }

    /**
     * Compares one usage-bearing XML node against all currently unresolved
     * tables and removes each table that has a matching reference.
     */
    private static boolean removeMatchedTables(String text, Set<String> tables) {
        for (Iterator<String> iterator = tables.iterator(); iterator.hasNext();) {
            String keyword = iterator.next();
            if (containsTableUsage(text, keyword)) {
                iterator.remove();
                if (tables.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Mirrors the previous XPath contains checks for JSON/plugin property table
     * references, including hash variables, BeanShell strings, and JDBC table
     * names with the app_fd_ prefix.
     * <p>
     * The {@code .keyword}/{@code .keyword?}/{@code .keyword.}/{@code .keyword[}/
     * {@code .keyword#} checks below are deliberately a looser literal
     * approximation of a hash variable reference rather than the stricter
     * {@code #...#}-anchored regex {@code DependenciesUtil} uses for the
     * dependency-usage viewer: this predicate only ever removes a table from
     * the orphan-candidate set, so over-matching just means being
     * conservative about flagging a table as orphaned, whereas the stricter
     * regex is required where a false positive would misreport an actual
     * dependency in the UI. Do not "fix" this by swapping in the regex
     * without confirming it can't cause tables to be reported as orphaned
     * that previously were not.
     */
    private static boolean containsTableUsage(String text, String keyword) {
        if (!text.contains(keyword)) {
            return false;
        }

        return DependenciesUtil.containsQuoted(text, keyword)
                || text.contains("." + keyword + "}")    // .keyword}
                || text.contains("." + keyword + "?")    // .keyword?
                || text.contains("." + keyword + ".")    // .keyword.
                || text.contains("." + keyword + "[")    // .keyword[
                || text.contains("." + keyword + "#")    // .keyword#
                || DependenciesUtil.containsBeanshellEscaped(text, keyword)
                || DependenciesUtil.containsAppFdTable(text, keyword)
                || DependenciesUtil.containsFormHashKeyword(text, keyword);
    }

    protected Set<String> getTables() {
        Set<String> tables = new HashSet<String>();

        DataSource ds = (DataSource)AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        ResultSet rs = null;
        try {
            con = ds.getConnection();
            DatabaseMetaData md = con.getMetaData();
            rs = md.getTables(con.getCatalog(), null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                String tablesName = rs.getString(3);
                if (tablesName.startsWith(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME)) {
                    tables.add(tablesName.substring(7));
                }
            }
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch(Exception e) {
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch(Exception e) {
            }
        }

        return tables;
    }

    @Override
    public List<GovHealthCheckAction> getActions() {
        if (actions == null) {
            actions = List.of(
                new GovHealthCheckAction("fixit", ResourceBundleUtil.getMessage("orphanedFormDataCheck.fixit"), "fa-solid fa-hammer", (GovHealthCheckAction action, String pluginClass, String detail, HttpServletRequest request, HttpServletResponse response) -> {
                    Set<String> tables = getOrphanedTables();
                    
                    boolean hasError = false;
                    
                    try {
                        FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
                        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
                        Connection con = null;
                        Statement statement = null;
                        try {
                            con = ds.getConnection();
                            statement = con.createStatement();

                            for (String t : tables) {
                                LogUtil.info(OrphanedFormDataCheck.class.getName(), "Remove app_fd_" + t);
                                
                                try {
                                    //delete tables
                                    String sql = "DROP TABLE app_fd_" + t;
                                    statement.execute(sql);

                                    //delete hibernate mapping file & cache
                                    formDataDao.clearFormTableCache(t);
                                } catch (Exception ex) {
                                    LogUtil.info(getClassName(), "Fail to drop table app_fd_" + t + " due to " + ex.getMessage());
                                    hasError = true;
                                }
                            }
                        } finally{
                            statement.close();
                            con.close();
                        }
                    } catch (Exception e) {
                        hasError = true;
                        LogUtil.error(getClassName(), e, "");
                    }
                    
                    if (hasError) {
                        return GovHealthCheckActionResult.fail(false, ResourceBundleUtil.getMessage("orphanedFormDataCheck.fixit.fail"));
                    } else {
                        return GovHealthCheckActionResult.success(true, ResourceBundleUtil.getMessage("orphanedFormDataCheck.fixit.success"));
                    }
                })
            );
        }
        return actions;
    }
}
