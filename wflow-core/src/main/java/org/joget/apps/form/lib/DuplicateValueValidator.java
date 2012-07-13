package org.joget.apps.form.lib;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.PluginManager;

public class DuplicateValueValidator extends FormValidator {

    @Override
    public String getName() {
        return "Duplicate Value Validator";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "Duplicate Value Validator";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "Duplicate Value Validator";
    }

    @Override
    public String getPropertyOptions() {
        String formDefField = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            String formJsonUrl = "[CONTEXT_PATH]/web/json/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/forms/options";
            formDefField = "{name:'formDefId',label:'Form ID',type:'selectbox',required:'True',options_ajax:'" + formJsonUrl + "'}";
        } else {
            formDefField = "{name:'formDefId',label:'Form ID',type:'textfield',required:'True'}";
        }
        Object[] arguments = new Object[]{formDefField};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/form/duplicateValueValidator.json", arguments, true, "message/form/DuplicateValueValidator");
        return json;
    }

    @Override
    public String getElementDecoration() {
        String decoration = "";
        String mandatory = (String) getProperty("mandatory");
        if ("true".equals(mandatory)) {
            decoration += " * ";
        }
        if (decoration.trim().length() > 0) {
            decoration = decoration.trim();
        }
        return decoration;
    }

    @Override
    public boolean validate(Element element, FormData data, String[] values) {
        boolean result = true;
        String id = FormUtil.getElementParameterName(element);
        String label = element.getPropertyString("label");
        String formDefId = (String) getProperty("formDefId");
        String fieldId = (String) getProperty("fieldId");
        String mandatory = (String) getProperty("mandatory");
        String regex = (String) getProperty("regex");
        String errorMsg = (String) getProperty("errorMsg");
        
        PluginManager pm = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        //Check is empty or not
        if (isEmptyValues(values)) {
            if ("true".equals(mandatory)) {
                result = false;
                data.addFormError(id, pm.getMessage("form.duplicatevaluevalidator.e.missingValue", this.getClassName(), null));
            }
        } else {
            //check value format with regex
            if (!isFormatCorrect(regex, values)) {
                result = false;
                if (errorMsg != null && errorMsg.trim().length() == 0) {
                    errorMsg = pm.getMessage("form.duplicatevaluevalidator.e.formatInvalid", this.getClassName(), null); 
                }
                data.addFormError(id, errorMsg);
            } else {
                //check for duplicate value
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                String tableName = null;
                AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");

                if (formDefId != null) {
                    tableName = appService.getFormTableName(appDef, formDefId);
                }
                if (isDuplicate(formDefId, tableName, element, data, fieldId, values)) {
                    result = false;
                    data.addFormError(id, pm.getMessage("form.duplicatevaluevalidator.e.valueAlreadyExist", this.getClassName(), null));
                }
            }
        }

        return result;
    }

    protected boolean isEmptyValues(String[] values) {
        boolean result = false;
        if (values == null || values.length == 0) {
            result = true;
        } else {
            for (String val : values) {
                if (val == null || val.trim().length() == 0) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    protected boolean isFormatCorrect(String regex, String[] values) {
        if (regex == null || (regex != null && regex.trim().length() == 0)) {
            return true;
        }

        boolean result = true;
        if (values != null && values.length > 0) {
            for (String val : values) {
                if (val != null && !val.matches("^" + regex + "$")) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    protected boolean isDuplicate(String formDefId, String tableName, Element element, FormData formData, String fieldId, String[] values) {
        boolean result = false;
        FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");

        if (values != null && values.length > 0 && FormUtil.isElementPropertyValuesChanges(element, formData, values)) {
            for (String val : values) {
                String key = null;

                if (!FormUtil.PROPERTY_ID.equals(fieldId)) {
                    try {
                        key = formDataDao.findPrimaryKey(formDefId, tableName, fieldId, val);
                    } catch (Exception e) {
                        key = null;
                    }
                } else {
                    if (formDataDao.load(formDefId, tableName, val) != null) {
                        key = val;
                    }
                }
                if (key != null && key.trim().length() > 0) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
