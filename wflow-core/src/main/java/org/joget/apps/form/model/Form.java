package org.joget.apps.form.model;

import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.json.JSONObject;

public class Form extends Element implements FormBuilderEditable, FormContainer {

    private Map<String, String[]> formMetas = new HashMap<String, String[]>();

    @Override
    public String getName() {
        return "Form";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "Form Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "form.ftl";
        
        //for preview
        if (formData.getFormResult(FormService.PREVIEW_MODE) != null) {
            setFormMeta("json", new String[]{formData.getRequestParameter("json")});
        }

        // get current app
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            dataModel.put("appId", appDef.getAppId());
            dataModel.put("appVersion", appDef.getVersion());
        }
        
        // check whether in form builder
        boolean formBuilderActive = FormUtil.isFormBuilderActive();
       
        // check for quick edit mode
        boolean isQuickEditEnabled = (!formBuilderActive && AppUtil.isQuickEditEnabled()) || (formBuilderActive && getParent() != null);
        dataModel.put("quickEditEnabled", isQuickEditEnabled);
        if (((Boolean) dataModel.get("includeMetaData") == true) || isAuthorize(formData)) {
            dataModel.put("isAuthorize", true);
            
            String paramName = FormUtil.getElementParameterName(this);
            setFormMeta(paramName+"_SUBMITTED", new String[]{"true"});
            String primaryKey = this.getPrimaryKeyValue(formData);

            if (getParent() == null) {
                if (formData.getRequestParameter("_FORM_META_ORIGINAL_ID") != null) {
                    setFormMeta("_FORM_META_ORIGINAL_ID", new String[]{formData.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID)});
                } else if (primaryKey != null) {
                    setFormMeta("_FORM_META_ORIGINAL_ID", new String[]{primaryKey});
                } else {
                    setFormMeta("_FORM_META_ORIGINAL_ID", new String[]{""});
                }
                
                //store form erros
                Map<String, String> errors = formData.getFormErrors();
                if (errors != null && !errors.isEmpty()) {
                    try {
                        JSONObject errorJson = new JSONObject();
                        errorJson.put("errors", errors);
                        setFormMeta(FormUtil.FORM_ERRORS_PARAM, new String[]{errorJson.toString()});
                    } catch (Exception e) {}
                }
            } else {
                String uniqueId = getCustomParameterName();
                if (formData.getRequestParameter(uniqueId + "_FORM_META_ORIGINAL_ID") != null) {
                    setFormMeta(uniqueId + "_FORM_META_ORIGINAL_ID", new String[]{formData.getRequestParameter(uniqueId + FormUtil.FORM_META_ORIGINAL_ID)});
                } else if (primaryKey != null) {
                    setFormMeta(uniqueId + "_FORM_META_ORIGINAL_ID", new String[]{primaryKey});
                } else {
                    setFormMeta(uniqueId + "_FORM_META_ORIGINAL_ID", new String[]{""});
                }
            }

            dataModel.put("formMeta", formMetas);
        } else {
            dataModel.put("isAuthorize", false);
        }

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/form.json", null, true, "message/form/Form");
    }

    @Override
    public String getFormBuilderTemplate() {
        return "";
    }

    public void setFormMeta(String name, String[] values) {
        formMetas.put(name, values);
    }

    public String[] getFormMeta(String name) {
        return formMetas.get(name);
    }

    public Map getFormMetas() {
        return formMetas;
    }
    
    @Override
    public FormRowSet formatData(FormData formData) {
        return null;
    }
}