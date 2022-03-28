package org.joget.governance.lib;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDaoImpl;
import org.joget.apps.form.model.AbstractSubForm;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.governance.model.GovAppHealthCheck;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckResult;
import org.springframework.context.ApplicationContext;

public class NamingConventionCheck extends GovHealthCheckAbstract implements GovAppHealthCheck {

    @Override
    public String getName() {
        return "NamingConventionCheck";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "Naming Convention";
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
        return ResourceBundleUtil.getMessage("governance.qualityAssurance");
    }

    @Override
    public String getSortPriority() {
        return "3";
    }

    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        result.setStatus(GovHealthCheckResult.Status.PASS);
        
        Set<String> checked = new HashSet<String>();
        Map<String, Map<String, String>> tables = new HashMap<String, Map<String, String>>();
        
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
        
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        Collection<AppDefinition> latestAppDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", Boolean.FALSE, null, null);
        
        for (AppDefinition appDef : appDefinitionList) {
            checkFormFieldIds(appDef, tables, result, formService);
            checked.add(appDef.getAppId() + "::" + appDef.getVersion());
        }
        
        for (AppDefinition appDef : latestAppDefinitionList) {
            if (!checked.contains(appDef.getAppId() + "::" + appDef.getVersion())) {
                checkFormFieldIds(appDef, tables, result, formService);
                checked.add(appDef.getAppId() + "::" + appDef.getVersion());
            }
        }
        
        return result;
    }
    
    private void checkFormFieldIds(AppDefinition appDef, Map<String, Map<String, String>> tables, GovHealthCheckResult result, FormService formService) {
        if (appDef.getFormDefinitionList() != null && !appDef.getFormDefinitionList().isEmpty()) {
            for (FormDefinition formDef : appDef.getFormDefinitionList()) {
                Map<String, String> checkDuplicateMap = tables.get(formDef.getTableName());
                if (checkDuplicateMap == null) {
                    checkDuplicateMap = new HashMap<String, String>();
                    tables.put(formDef.getTableName(), checkDuplicateMap);
                }
                
                // get JSON
                String json = formDef.getJson();
                if (json != null) {
                    try {
                        Form form = (Form) formService.createElementFromJson(json, false);
                        Collection<String> tempColumnList = new HashSet<String>();
                        findAllElementIds(form, tempColumnList);
                        for (String c : tempColumnList) {
                            if (!c.isEmpty()) {
                                String exist = checkDuplicateMap.get(c.toLowerCase());
                                if (exist != null && !exist.equals(c)) {
                                    result.setStatus(GovHealthCheckResult.Status.WARN);
                                    result.addDetailWithAppId(ResourceBundleUtil.getMessage("namingConventionCheck.msg", new String[]{appDef.getName(), formDef.getName(), exist}), "/web/console/app/"+appDef.getAppId()+"/"+appDef.getVersion().toString()+"/form/builder/"+formDef.getId(), null, appDef.getName());
                                } else {
                                    checkDuplicateMap.put(c.toLowerCase(), c);
                                }
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.debug(FormDataDaoImpl.class.getName(), "JSON definition of form["+formDef.getAppId()+":"+formDef.getAppVersion()+":"+formDef.getId()+"] is either protected or corrupted.");
                    }
                }
            }
        }
    }
    
    protected void findAllElementIds(Element element, Collection<String> columnList) {
        Collection<String> fieldNames = element.getDynamicFieldNames();
        if (fieldNames != null && !fieldNames.isEmpty()) {
            columnList.addAll(fieldNames);
        }
        if (!(element instanceof FormContainer) && element.getProperties() != null) {
            String id = element.getPropertyString(FormUtil.PROPERTY_ID);
            if (id != null && !id.isEmpty()) {
                columnList.add(id);
            }
        }
        if (!(element instanceof AbstractSubForm)) { // do not recurse into subforms
            Collection<org.joget.apps.form.model.Element> children = element.getChildren();
            if (children != null) {
                for (org.joget.apps.form.model.Element child : children) {
                    findAllElementIds(child, columnList);
                }
            }
        }
    }

    @Override
    public GovHealthCheckResult performAppCheck(String appId, String version) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        result.setStatus(GovHealthCheckResult.Status.PASS);

        Map<String, Map<String, String>> tables = new HashMap<String, Map<String, String>>();
        
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");

        checkFormFieldIds(appDef, tables, result, formService);
        return result;
    }
}
