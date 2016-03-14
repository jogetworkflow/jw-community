package org.joget.apps.form.lib;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.FileManager;

public class FileUpload extends Element implements FormBuilderPaletteElement {

    @Override
    public String getName() {
        return "File Upload";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "FileUpload Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "fileUpload.ftl";

        // set value
        String value = FormUtil.getElementPropertyValue(this, formData);
        
        // check if the file is in temp file
        File file = FileManager.getFileByPath(value);
        if (file != null) {
            dataModel.put("tempFilePath", value);
            dataModel.put("value", file.getName());
        } else if (value != null && !value.isEmpty()) {
            dataModel.put("value", value);
            
            // determine actual path for the file uploads
            String primaryKeyValue = getPrimaryKeyValue(formData);
            String fileName = value;
            String formDefId = "";
            Form form = FormUtil.findRootForm(this);
            if (form != null) {
                formDefId = form.getPropertyString(FormUtil.PROPERTY_ID);
            }
            String encodedFileName = fileName;
            if (fileName != null) {
                try {
                    encodedFileName = URLEncoder.encode(fileName, "UTF8").replaceAll("\\+", "%20");
                } catch (UnsupportedEncodingException ex) {
                    // ignore
                }
            }

            String appId = "";
            String appVersion = "";

            AppDefinition appDef = AppUtil.getCurrentAppDefinition();

            if (appDef != null) {
                appId = appDef.getId();
                appVersion = appDef.getVersion().toString();
            }

            String filePath = "/web/client/app/" + appId + "/" + appVersion + "/form/download/" + formDefId + "/" + primaryKeyValue + "/" + encodedFileName + ".";
            if (Boolean.valueOf(getPropertyString("attachment")).booleanValue()) {
                filePath += "?attachment=true";
            }
            dataModel.put("filePath", filePath);
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
            String removalId = id + postfix;
            String filename = formData.getRequestParameter(id);
            String removalFlag = formData.getRequestParameter(removalId);
            String existingFilePath = formData.getRequestParameter(id + filePathPostfix);
            if (filename == null && "on".equals(removalFlag)) {
                // don't remove file, reset value
                formData.addRequestParameterValues(id, new String[]{""});
            } else if (filename == null && existingFilePath != null && !existingFilePath.isEmpty()) {
                formData.addRequestParameterValues(id, new String[]{existingFilePath});
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
            String value = FormUtil.getElementPropertyValue(this, formData);
            if (value != null) {
                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                
                // check if the file is in temp file
                File file = FileManager.getFileByPath(value);
                if (file != null) {
                    result.putTempFilePath(id, value);
                    result.setProperty(id, file.getName());
                    
                    String paramName = FormUtil.getElementParameterName(this);
                    formData.addRequestParameterValues(paramName, new String[]{file.getName()});
                } else {
                    result.setProperty(id, value);
                }
                
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
}
