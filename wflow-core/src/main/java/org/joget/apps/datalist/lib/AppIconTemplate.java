package org.joget.apps.datalist.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.service.DataListService;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.joget.directory.model.UserMetaData;
import org.joget.directory.dao.UserMetaDataDao;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.json.JSONObject;

public class AppIconTemplate extends SimpleCardTemplate {
    private String userSettings = null;
    private DataListCollection selectedApps = null;

    @Override
    public String getName() {
        return "AppIconTemplate";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }
    
    @Override
    public String getLabel() {
        return "Card - App Icon";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/appIconTemplate.json", null, true, null);
    }
    
    @Override
    public String getTemplate() {
        return getTemplate(null, "/templates/appIconTemplate.ftl", null);
    }
    
    @Override
    public String fillRows(DataListCollection rows, String childtemplate) {
        String mode = getDatalist().getDataListParamString("samode");
        if ("true".equalsIgnoreCase(getPropertyString("enableSuperApp")) && (mode == null || mode.isEmpty())) {
            DataListCollection apps = getSelectedApps(rows);
            getDatalist().setTotal(apps.size());
            return super.fillRows(apps, childtemplate);
        } else {
            String splitColumn = getPropertyString("splitListByColumn");
            if (splitColumn.isEmpty()) {
                return super.fillRows(rows, childtemplate);
            } else {
                String value = "";
                Map<String, DataListCollection> splittedList= new TreeMap<String, DataListCollection>();

                for (Object o : rows) {
                    String s = (String) DataListService.evaluateColumnValueFromRow(o, splitColumn);
                    if (s == null) {
                        s = "";
                    }
                    DataListCollection splittedRows = splittedList.get(s);
                    if (splittedRows == null) {
                        splittedRows = new DataListCollection();
                        splittedList.put(s, splittedRows);
                    }
                    splittedRows.add(o);
                }

                for (String key : splittedList.keySet()) {
                    if (!key.isEmpty()) {
                        String style = "style=\"margin-top:25px\"";
                        if (value.isEmpty()) {
                            style = "";
                        }
                        value += "<h4 class=\"col-12\" "+style+">" + key + "</h4>";
                    }
                    value += super.fillRows(splittedList.get(key), childtemplate);
                }

                return value;
            }
        }
    }
    
    @Override
    public String render() {
        String result = "";
        
        if ("true".equalsIgnoreCase(getPropertyString("enableSuperApp"))) {
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            
            String mode = "";
            if (request != null) {
                mode = getDatalist().getDataListParamString("samode");
                if (mode == null || !mode.equals("edit")) {
                    mode = "";
                }
                
                //save user config
                if (mode.isEmpty() && request.getMethod().equalsIgnoreCase("POST")) {
                    String selectedApps = request.getParameter("selectedApps");
                    if (selectedApps != null) {
                        selectedApps = StringUtil.stripAllHtmlTag(selectedApps);
                        if (!selectedApps.isEmpty()) {
                            UserMetaDataDao dao = (UserMetaDataDao) AppUtil.getApplicationContext().getBean("userMetaDataDao");
                            WorkflowUserManager wum = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
                            AppDefinition appDef = AppUtil.getCurrentAppDefinition();

                            UserMetaData data = dao.getUserMetaData(wum.getCurrentUsername(), "superapp_"+appDef.getAppId()+"_"+getDatalist().getId());
                            if (data == null) {
                                data = new UserMetaData();
                                data.setUsername(wum.getCurrentUsername());
                                data.setKey("superapp_"+appDef.getAppId()+"_"+getDatalist().getId());
                                data.setValue(selectedApps);
                                
                                dao.addUserMetaData(data);
                            } else {
                                data.setValue(selectedApps);
                                dao.updateUserMetaData(data);
                            }
                            userSettings = selectedApps;
                        }
                    }
                }

                //add js & css 
                result += "<script src=\"" + request.getContextPath() + "/plugin/org.joget.apps.datalist.lib.AppIconTemplate/js/superAppListing.js\" ></script>\n";
                result += "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getContextPath() + "/plugin/org.joget.apps.datalist.lib.AppIconTemplate/css/superAppListing.css\" />\n";

                //add button
                result += "<div class=\"super-app-header\" style=\"text-align:right;\">";
                if ("edit".equals(mode)) {
                    result += "<a class=\"btn btn-primary saveChanges\" data-ajax-content-placeholder=\"dashboard\">"+ResourceBundleUtil.getMessage("datalist.appIconTemplate.save")+"</a> <a class=\"btn btn-secondary cancelChanges\" data-ajax-content-placeholder=\"dashboard\">"+ResourceBundleUtil.getMessage("datalist.appIconTemplate.cancel")+"</a>"; 
                    
                    //enable selector to populate id
                    getDatalist().setCheckboxPosition(DataList.CHECKBOX_POSITION_LEFT);
                } else {
                    result += "<a class=\"btn btn-secondary manageApps\" data-ajax-content-placeholder=\"dashboard\">"+ResourceBundleUtil.getMessage("datalist.appIconTemplate.manageApps")+"</a>";
                }
                result += "</div>";
            }
            
            //add wrapper & content
            result += "<div class=\"super-app-body\" data-super_app=\""+getDatalist().getId()+"\" data-super_app_mode=\""+mode+"\" data-super_app_settings=\""+StringUtil.escapeString(getSuperAppSettings(), StringUtil.TYPE_HTML, null)+"\">" + super.render() + "</div>";
        } else {
            result = super.render();
        }
        
        return result;
    }
    
    /**
     * If there is user selected app, return it.
     * If not, check if there is default app,
     * else, return the full rows
     * @param rows
     * @return 
     */
    protected DataListCollection getSelectedApps(DataListCollection rows) {
        if (selectedApps == null) {
            if (rows != null && !rows.isEmpty()) {
                String selection = getUserSettings();
                Map<String, Object> selectedAppIds = new LinkedHashMap<String, Object>();
                if (selection != null && !selection.isEmpty()) {
                    String[] temp = selection.split(";");
                    for (String t : temp) {
                        selectedAppIds.put(t, null);
                    }
                } else {
                    String mode = getPropertyString("defaultAppMode"); 
                    if ("ids".equals(mode)) {
                        String tempString = getPropertyString("defaultAppIds");
                        if (!tempString.isEmpty()) {
                            String[] temp = tempString.split(";"); 
                            for (String t : temp) {
                                selectedAppIds.put(t, null);
                            }
                        }
                    } else {
                        String defaultAppCol = getPropertyString("defaultAppByColumn");
                        if (!defaultAppCol.isEmpty()) {
                            DataListCollection newRows = new DataListCollection();
                            for (Object o : rows) {
                                String s = (String) DataListService.evaluateColumnValueFromRow(o, defaultAppCol);
                                if (s == null) {
                                    s = "false";
                                }
                                if (Boolean.parseBoolean(s)) {
                                    newRows.add(o);
                                }
                            }
                            if (!newRows.isEmpty()) {
                                selectedApps = newRows;
                                return selectedApps;
                            }
                        }
                    }
                }

                if (!selectedAppIds.isEmpty()) {
                    String idCol = getDatalist().getBinder().getPrimaryKeyColumnName();
                    Set<String> nonExistIds = new HashSet<String>();
                    nonExistIds.addAll(selectedAppIds.keySet());
                    for (Object r : rows) {
                        String id = (String) DataListService.evaluateColumnValueFromRow(r, idCol);
                        if (id != null && selectedAppIds.containsKey(id)) {
                            selectedAppIds.put(id, r);
                            nonExistIds.remove(id);
                        }
                    }
                    for (String s : nonExistIds) {
                        selectedAppIds.remove(s);
                    }

                    DataListCollection newRows = new DataListCollection();
                    newRows.addAll(selectedAppIds.values());

                    selectedApps = newRows;
                    return selectedApps;
                }
            }
            selectedApps = rows;
            return selectedApps;
        }
        
        return selectedApps;
    }
    
    /**
     * Return super app listing settings in json string
     * @return 
     */
    protected String getSuperAppSettings() {
        try {
            JSONObject settings = new JSONObject();
            
            String idCol = getDatalist().getBinder().getPrimaryKeyColumnName();
            DataListCollection rows = getDatalist().getRows();
            List<String> selectedAppIds = new ArrayList<String>();
            DataListCollection selected = getSelectedApps(rows);
            if (selected != null) {
                for (Object r : selected) {
                    String id = (String) DataListService.evaluateColumnValueFromRow(r, idCol);
                    selectedAppIds.add(id);
                }
            }
            settings.put("selected", selectedAppIds);
            
            String mode = getPropertyString("nonRemovableAppMode"); 
            List<String> nonRemovableAppIds = new ArrayList<String>();
            if ("ids".equals(mode)) {
                String tempString = getPropertyString("nonRemovableAppIds");
                if (!tempString.isEmpty()) {
                    String[] temp = tempString.split(";"); 
                    nonRemovableAppIds.addAll(Arrays.asList(temp));
                }
            } else {
                String nonRemovableCol = getPropertyString("nonRemovableAppByColumn");
                if (!nonRemovableCol.isEmpty()) {
                    DataListCollection newRows = new DataListCollection();
                    for (Object r : rows) {
                        String s = (String) DataListService.evaluateColumnValueFromRow(r, nonRemovableCol);
                        if (s == null) {
                            s = "false";
                        }
                        if (Boolean.parseBoolean(s)) {
                            String id = (String) DataListService.evaluateColumnValueFromRow(r, idCol);
                            nonRemovableAppIds.add(id);
                        }
                    }
                }
            }
            settings.put("nonRemovable", nonRemovableAppIds);
            settings.put("param", getDatalist().getDataListEncodedParamName("samode"));
            
            Map<String, String> msg = new HashMap<String, String>();
            msg.put("selectedApps", ResourceBundleUtil.getMessage("datalist.appIconTemplate.selected"));
            msg.put("availableApps", ResourceBundleUtil.getMessage("datalist.appIconTemplate.available"));
            msg.put("removeApp", ResourceBundleUtil.getMessage("datalist.appIconTemplate.remove"));
            msg.put("selectApp",ResourceBundleUtil.getMessage("datalist.appIconTemplate.select"));
            
            settings.put("msg", msg);
            
            return settings.toString();
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "");
        }
        return "";
    }
    
    /**
     * Get the user selected app from user meta data
     * @return 
     */
    protected String getUserSettings() {
        if (userSettings == null) {
            UserMetaDataDao dao = (UserMetaDataDao) AppUtil.getApplicationContext().getBean("userMetaDataDao");
            WorkflowUserManager wum = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();

            UserMetaData data = dao.getUserMetaData(wum.getCurrentUsername(), "superapp_"+appDef.getAppId()+"_"+getDatalist().getId());
            if (data != null) {
                userSettings = data.getValue();
            } else {
                userSettings = "";
            }
        }
        return userSettings;
    }
}