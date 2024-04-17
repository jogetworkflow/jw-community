package org.joget.apps.form.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.Permission;
import org.joget.commons.util.SecurityUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONObject;

public class Form extends Element implements FormBuilderEditable, FormContainer {

    private Map<String, String[]> formMetas = new HashMap<String, String[]>();
    private Collection<FormAction> actions = new ArrayList<FormAction>();

    @Override
    public String getName() {
        return "Form";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
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
        boolean isQuickEditEnabled = (!"true".equals(getPropertyString("removeQuickEdit")) && !formBuilderActive && AppUtil.isQuickEditEnabled()) || (formBuilderActive && getParent() != null);
        dataModel.put("quickEditEnabled", isQuickEditEnabled);
        if (((Boolean) dataModel.get("includeMetaData") == true) || isAuthorize(formData)) {
            dataModel.put("isAuthorize", true);
            dataModel.put("isRecordExist", true);
            
            String paramName = FormUtil.getElementParameterName(this);
            setFormMeta(paramName+"_SUBMITTED", new String[]{"true"});
            String primaryKey = this.getPrimaryKeyValue(formData);

            if (getParent() == null) {
                boolean isRuntimeLoad = (formData.getFormResult(FormService.PREVIEW_MODE) == null || !dataModel.containsKey("elementMetaData"))
                        && !FormUtil.isFormSubmitted(this, formData);
                boolean urlHasId = (formData.getRequestParameter("id") != null || formData.getRequestParameter("fk_id") != null
                            || formData.getRequestParameter("fke_id") != null || formData.getRequestParameter("recordId") != null);
                
                if (isRuntimeLoad && urlHasId) {
                    //check for record exist
                    FormRowSet data = formData.getLoadBinderData(this);
                    if (data == null || data.isEmpty()) {
                        dataModel.put("isRecordExist", false);
                    }
                }
                
                if (formData.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID) != null &&
                        !formData.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID).isEmpty()) {
                    setFormMeta(FormUtil.FORM_META_ORIGINAL_ID, new String[]{formData.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID)});
                } else if (primaryKey != null) {
                    setFormMeta(FormUtil.FORM_META_ORIGINAL_ID, new String[]{primaryKey});
                } else {
                    setFormMeta(FormUtil.FORM_META_ORIGINAL_ID, new String[]{""});
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
                if (formData.getRequestParameter(uniqueId + FormUtil.FORM_META_ORIGINAL_ID) != null &&
                        !formData.getRequestParameter(uniqueId + FormUtil.FORM_META_ORIGINAL_ID).isEmpty()) {
                    setFormMeta(uniqueId + FormUtil.FORM_META_ORIGINAL_ID, new String[]{formData.getRequestParameter(uniqueId + FormUtil.FORM_META_ORIGINAL_ID)});
                } else if (primaryKey != null) {
                    setFormMeta(uniqueId + FormUtil.FORM_META_ORIGINAL_ID, new String[]{primaryKey});
                } else {
                    setFormMeta(uniqueId + FormUtil.FORM_META_ORIGINAL_ID, new String[]{""});
                }
            }
            
            //to remove unuse nonces after submission
            if (getParent() == null) {
                HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                if (request != null) {
                    setFormMeta("_NONCE_TOKEN_REQUEST_HASH", new String[]{Integer.toString(request.hashCode())});
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
        if (FormUtil.isFormSubmitted(this, formData) && formData.getRequestParameter("_NONCE_TOKEN_REQUEST_HASH") != null) {
            try {
                int requestHash = Integer.parseInt(formData.getRequestParameter("_NONCE_TOKEN_REQUEST_HASH"));
                SecurityUtil.clearNonces(requestHash);
            } catch (Exception e) {}
        }
        return null;
    }

    public Collection<FormAction> getActions() {
        return actions;
    }

    public void setActions(Collection<FormAction> actions) {
        this.actions = actions;
    }
    
    public void addAction(FormAction action) {
        if (this.actions == null) {
            this.actions = new ArrayList<FormAction>();
        }
        this.actions.add(action);
    }
    
    public String getTooltips() {
        String tips = "{}";
        Map<String, String> messages = AppUtil.getAppMessageFromStore();
        try {
            JSONObject obj = new JSONObject();
            for (String key : messages.keySet()) {
                if (key.startsWith("tooltip." + getPropertyString(FormUtil.PROPERTY_ID) + ".")) {
                    obj.put(key, messages.get(key));
                }
            }
            
            tips = obj.toString();
        } catch (Exception e) {}
        
        return tips;
    }
    
    @Override
    public Boolean isAuthorize(FormData formData) {
        if (formData.getFormResult(FormService.PREVIEW_MODE) != null) {
            return true;
        }
        
        Boolean isAuthorize = isAuthorizeSet.get(formData);
        if (isAuthorize == null) {
            if (Permission.DEFAULT.equals(getPermissionKey(formData))) {
                Map permissionMap = (Map) getProperty("permission");
                if (permissionMap != null) {
                    isAuthorize = FormUtil.getPermissionResult(permissionMap, formData);
                } else {
                    isAuthorize = true;
                }
            } else {
                isAuthorize = true;
            }
            isAuthorizeSet.put(formData, isAuthorize);
        }
        
        return isAuthorize;
    }
    
    @Override
    public String getPermissionKey(FormData formData) {
        if (!permissionKeys.containsKey(formData)) {
            permissionKeys.put(formData, Permission.DEFAULT);
            Object[] rules = (Object[]) getProperty("permission_rules");
            if (rules != null && rules.length > 0) {
                for (Object rule : rules) {
                    Map ruleMap = (Map) rule;
                    String key = ruleMap.get("permission_key").toString();
                    boolean isAuthorize = FormUtil.getPermissionResult((Map) ruleMap.get("permission"), formData);
                    if (isAuthorize) {
                        permissionKeys.put(formData, key);
                        break;
                    }
                }
            }
        }
        return permissionKeys.get(formData);
    }
    
    @Override
    public String decorateWithBuilderProperties(String html, FormData formData) {
        if (getParent() != null) {
            return html; //skip for subform
        } else {
            return super.decorateWithBuilderProperties(html, formData);
        }
    }
    
    @Override
    public Map<String, String> getElementStyles(String styleClass, Map<String, String> attrs) {
        Map<String, String> styles = super.getElementStyles(styleClass, attrs);
        
        //form & section default styles
        if (getParent() == null) {
            String[] keys = new String[]{"", "section-", "section-header-", "section-fieldLabel-", "section-fieldInput-"};
            String[] cssClass = new String[] {
                "." + styleClass,
                "." + styleClass + " .form-section, ." + styleClass + " .subform-section",
                "." + styleClass + " .form-section .form-section-title, ." + styleClass + " .subform-section .subform-section-title",
                "." + styleClass + " .form-cell > label.label, ." + styleClass + " .subform-cell > label.label",
                "." + styleClass + " .form-cell > label.label + *:not(.ui-screen-hidden):not(div.form-clear), ." + styleClass + " .subform-cell > label.label + *:not(.ui-screen-hidden):not(div.form-clear), "+
                    "." + styleClass + " .form-cell > label.label + .ui-screen-hidden + *, ." + styleClass + " .subform-cell > label.label + .ui-screen-hidden + *, "+
                    "." + styleClass + " .form-cell > label.label + div.form-clear + *, ." + styleClass + " .subform-cell > label.label + div.form-clear + * "
            };
            String[] cssHoverClass = new String[] {
                "." + styleClass + ":hover",
                "." + styleClass + " .form-section:hover, .{{styleClass}} .subform-section:hover",
                "." + styleClass + " .form-section:hover .form-section-title, ." + styleClass + " .subform-section:hover .subform-section-title",
                "." + styleClass + " .form-cell:hover > label.label, ." + styleClass + " .subform-cell:hover > label.label",
                "." + styleClass + " .form-cell:hover > label.label + *:not(.ui-screen-hidden):not(div.form-clear), ." + styleClass + " .subform-cell:hover > label.label + *:not(.ui-screen-hidden):not(div.form-clear), "+
                    "." + styleClass + " .form-cell:hover > label.label + .ui-screen-hidden + *, ." + styleClass + " .subform-cell:hover > label.label + .ui-screen-hidden + *, "+
                    "." + styleClass + " .form-cell:hover > label.label + div.form-clear + *, ." + styleClass + " .subform-cell:hover > label.label + div.form-clear + * "
            };
            
            for (int i=0; i < keys.length; i++) {
                Map<String, String> tempAttrs = AppPluginUtil.generateAttrAndStyles(getProperties(), keys[i]);
                if (!tempAttrs.get("desktopStyle").isEmpty()) {
                    styles.put("DESKTOP", styles.get("DESKTOP") + " " + cssClass[i] +" {" + tempAttrs.get("desktopStyle") + "} ");
                }
                if (!tempAttrs.get("tabletStyle").isEmpty()) {
                    styles.put("TABLET", styles.get("TABLET") + " " + cssClass[i] +" {" + tempAttrs.get("tabletStyle") + "} ");
                }
                if (!tempAttrs.get("mobileStyle").isEmpty()) {
                    styles.put("MOBILE", styles.get("MOBILE") + " " + cssClass[i] +" {" + tempAttrs.get("mobileStyle") + "} ");
                }
                if (!tempAttrs.get("hoverDesktopStyle").isEmpty()) {
                    styles.put("DESKTOP", styles.get("DESKTOP") + " " + cssHoverClass[i] +" {" + tempAttrs.get("hoverDesktopStyle") + "} ");
                }
                if (!tempAttrs.get("hoverTabletStyle").isEmpty()) {
                    styles.put("TABLET", styles.get("TABLET") + " " + cssHoverClass[i] +" {" + tempAttrs.get("hoverTabletStyle") + "} ");
                }
                if (!tempAttrs.get("hoverMobileStyle").isEmpty()) {
                    styles.put("MOBILE", styles.get("MOBILE") + " " + cssHoverClass[i] +" {" + tempAttrs.get("hoverMobileStyle") + "} ");
                }
            } 
            
            if ("label-top".equals(getPropertyString("css-label-position"))) {
                styles.put("DESKTOP", styles.get("DESKTOP") + " ." + styleClass +" .form-cell:not(.label-left) > label.label, ." + styleClass + " .subform-cell:not(.label-left) > label.label, " + 
                        " ." + styleClass +" .form-cell:not(.label-left) > label.label + *:not(.ui-screen-hidden), ." + styleClass + " .subform-cell:not(.label-left) > label.label + *:not(.ui-screen-hidden), " +
                        " ." + styleClass +" .form-cell:not(.label-left) > label.label + .ui-screen-hidden + *, ." + styleClass + " .subform-cell:not(.label-left) > label.label + .ui-screen-hidden + * " +
                        " {width: 100%; float: none;} ");
            }
            if ("tablet-label-top".equals(getPropertyString("css-tablet-label-position"))) {
                styles.put("TABLET", styles.get("TABLET") + " ." + styleClass +" .form-cell:not(.tablet-label-top) > label.label, ." + styleClass + " .subform-cell:not(.tablet-label-top) > label.label, " + 
                        " ." + styleClass +" .form-cell:not(.tablet-label-top) > label.label + *:not(.ui-screen-hidden), ." + styleClass + " .subform-cell:not(.tablet-label-top) > label.label + *:not(.ui-screen-hidden), " +
                        " ." + styleClass +" .form-cell:not(.tablet-label-top) > label.label + .ui-screen-hidden + *, ." + styleClass + " .subform-cell:not(.tablet-label-top) > label.label + .ui-screen-hidden + * " +
                        " {width: 100%; float: none;} ");
            }
        }
        
        return styles;
    }
}