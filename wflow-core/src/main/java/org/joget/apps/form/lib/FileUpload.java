package org.joget.apps.form.lib;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FileDownloadSecurity;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormPermission;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.UserviewPermission;
import org.joget.commons.util.FileManager;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;

public class FileUpload extends Element implements FormBuilderPaletteElement, FileDownloadSecurity {

    @Override
    public String getName() {
        return "File Upload";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "FileUpload Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "fileUpload.ftl";

        // set value
        String[] values = FormUtil.getElementPropertyValues(this, formData);
        
        //check is there a stored value
        String storedValue = formData.getStoreBinderDataProperty(this);
        if (storedValue != null) {
            values = storedValue.split(";");
        }
        
        
        Map<String, String> tempFilePaths = new HashMap<String, String>();
        Map<String, String> filePaths = new HashMap<String, String>();
        
        String primaryKeyValue = getPrimaryKeyValue(formData);
        String formDefId = "";
        Form form = FormUtil.findRootForm(this);
        if (form != null) {
            formDefId = form.getPropertyString(FormUtil.PROPERTY_ID);
        }
        String appId = "";
        String appVersion = "";

        AppDefinition appDef = AppUtil.getCurrentAppDefinition();

        if (appDef != null) {
            appId = appDef.getId();
            appVersion = appDef.getVersion().toString();
        }
                
        for (String value : values) {
            // check if the file is in temp file
            File file = FileManager.getFileByPath(value);
            
            if (file != null) {
                tempFilePaths.put(value, file.getName());
            } else if (value != null && !value.isEmpty()) {
                // determine actual path for the file uploads
                String fileName = value;
                String encodedFileName = fileName;
                if (fileName != null) {
                    try {
                        encodedFileName = URLEncoder.encode(fileName, "UTF8").replaceAll("\\+", "%20");
                    } catch (UnsupportedEncodingException ex) {
                        // ignore
                    }
                }
                
                String filePath = "/web/client/app/" + appId + "/" + appVersion + "/form/download/" + formDefId + "/" + primaryKeyValue + "/" + encodedFileName + ".";
                if (Boolean.valueOf(getPropertyString("attachment")).booleanValue()) {
                    filePath += "?attachment=true";
                }
                filePaths.put(filePath, value);
            }
        }
        
        if (!tempFilePaths.isEmpty()) {
            dataModel.put("tempFilePaths", tempFilePaths);
        }
        if (!filePaths.isEmpty()) {
            dataModel.put("filePaths", filePaths);
        }
        
        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public FormData formatDataForValidation(FormData formData) {
        // check for file removal
        String postfix = "_remove";
        String filePathPostfix = "_path";
        String id = FormUtil.getElementParameterName(this);
        if (id != null) {
            String[] tempFilenames = formData.getRequestParameterValues(id);
            String[] tempExisting = formData.getRequestParameterValues(id + filePathPostfix);
            
            if ((tempFilenames != null && tempFilenames.length > 0) || (tempExisting != null && tempExisting.length > 0)) {
                List<String> filenames = new ArrayList<String>();
                if (tempFilenames != null && tempFilenames.length > 0) {
                    filenames.addAll(Arrays.asList(tempFilenames));
                }
                List<String> removalFlag = new ArrayList<String>();
                String[] tempRemove = formData.getRequestParameterValues(id + postfix);
                if (tempRemove != null && tempRemove.length > 0) {
                    removalFlag.addAll(Arrays.asList(tempRemove));
                }
                
                List<String> existingFilePath = new ArrayList<String>();
                if (tempExisting != null && tempExisting.length > 0) {
                    existingFilePath.addAll(Arrays.asList(tempExisting));
                }

                for (String filename : existingFilePath) {
                    if (!removalFlag.contains(filename)) {
                        filenames.add(filename);
                    }
                }

                if (filenames.isEmpty()) {
                    formData.addRequestParameterValues(id, new String[]{""});
                } else if (!"true".equals(getPropertyString("multiple"))) {
                    formData.addRequestParameterValues(id, new String[]{filenames.get(0)});
                } else {
                    formData.addRequestParameterValues(id, filenames.toArray(new String[]{}));
                }
            }
        }
        return formData;
    }
    
    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String[] values = FormUtil.getElementPropertyValues(this, formData);
            if (values != null && values.length > 0) {
                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                List<String> resultedValue = new ArrayList<String>();
                List<String> filePaths = new ArrayList<String>();
                
                for (String value : values) {
                    // check if the file is in temp file
                    File file = FileManager.getFileByPath(value);
                    if (file != null) {
                        filePaths.add(value);
                        resultedValue.add(file.getName());

                    } else {
                        resultedValue.add(value);
                    }
                }
                
                if (!filePaths.isEmpty()) {
                    result.putTempFilePath(id, filePaths.toArray(new String[]{}));
                }
                
                // formulate values
                String delimitedValue = FormUtil.generateElementPropertyValues(resultedValue.toArray(new String[]{}));
                String paramName = FormUtil.getElementParameterName(this);
                formData.addRequestParameterValues(paramName, resultedValue.toArray(new String[]{}));
                        
                // set value into Properties and FormRowSet object
                result.setProperty(id, delimitedValue);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
        }

        return rowSet;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>FileUpload</label><input type='file' />";
    }

    @Override
    public String getLabel() {
        return "File Upload";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/fileUpload.json", null, true, "message/form/FileUpload");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public int getFormBuilderPosition() {
        return 900;
    }

    @Override
    public String getFormBuilderIcon() {
        return null;
    }
    
    @Override
    public Boolean selfValidate(FormData formData) {
        String id = FormUtil.getElementParameterName(this);
        Boolean valid = true;
        String error = "";
        try {
            String value = FormUtil.getElementPropertyValue(this, formData);

            File file = FileManager.getFileByPath(value);
            if (file != null) {
                if(getPropertyString("maxSize") != null && !getPropertyString("maxSize").isEmpty()) {
                    long maxSize = Long.parseLong(getPropertyString("maxSize")) * 1024;
                    
                    if (file.length() > maxSize) {
                        valid = false;
                        error += getPropertyString("maxSizeMsg") + " ";
                        
                    }
                }
                if(getPropertyString("fileType") != null && !getPropertyString("fileType").isEmpty()) {
                    String[] fileType = getPropertyString("fileType").split(";");
                    String filename = file.getName().toUpperCase();
                    Boolean found = false;
                    for (String type : fileType) {
                        if (filename.endsWith(type.toUpperCase())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        valid = false;
                        error += getPropertyString("fileTypeMsg");
                    }
                }
            }
            
            if (!valid) {
                formData.addFormError(id, error);
            }
        } catch (Exception e) {}
        
        return valid;
    }
    
    public boolean isDownloadAllowed(Map requestParameters) {
        String permissionType = getPropertyString("permissionType");
        if (permissionType.equals("public")) {
            return true;
        } else if (permissionType.equals("custom")) {
            Object permissionElement = getProperty("permissionPlugin");
            if (permissionElement != null && permissionElement instanceof Map) {
                Map elementMap = (Map) permissionElement;
                String className = (String) elementMap.get("className");
                Map<String, Object> properties = (Map<String, Object>) elementMap.get("properties");

                //convert it to plugin
                PluginManager pm = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
                UserviewPermission plugin = (UserviewPermission) pm.getPlugin(className);
                if (plugin != null && plugin instanceof FormPermission) {
                    WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
                    ExtDirectoryManager dm = (ExtDirectoryManager) AppUtil.getApplicationContext().getBean("directoryManager");
                    String username = workflowUserManager.getCurrentUsername();
                    User user = dm.getUserByUsername(username);

                    plugin.setProperties(properties);
                    plugin.setCurrentUser(user);
                    plugin.setRequestParameters(requestParameters);

                    return plugin.isAuthorize();
                }
            }
            return false;
        } else {
            return !WorkflowUtil.isCurrentUserAnonymous();
        }
    }
}
