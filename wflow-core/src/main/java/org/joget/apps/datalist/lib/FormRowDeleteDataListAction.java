package org.joget.apps.datalist.lib;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListActionDefault;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormDataDeletableBinder;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormLoadMultiRowElementBinder;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.orm.hibernate4.HibernateObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test implementation for an action
 */
public class FormRowDeleteDataListAction extends DataListActionDefault {

    public String getName() {
        return "Form Row Delete Action";
    }

    public String getVersion() {
        return "3.0.0";
    }

    public String getDescription() {
        return "Form Row Delete Action";
    }
    
    public String getLabel() {
        return "Form Row Delete Action";
    }

    public String getLinkLabel() {
        String label = getPropertyString("label");
        if (label == null || label.isEmpty()) {
            label = "Delete";
        }
        return label;
    }

    public String getHref() {
        return getPropertyString("href");
    }

    public String getTarget() {
        return "post";
    }

    public String getHrefParam() {
        return getPropertyString("hrefParam");
    }

    public String getHrefColumn() {
        return getPropertyString("hrefColumn");
    }

    public String getConfirmation() {
        String confirm = getPropertyString("confirmation");
        if (confirm == null || confirm.isEmpty()) {
            confirm = "Please Confirm";
        }
        return confirm;
    }

    @Transactional
    public DataListActionResult executeAction(DataList dataList, String[] rowKeys) {
        DataListActionResult result = new DataListActionResult();
        result.setType(DataListActionResult.TYPE_REDIRECT);
        result.setUrl("REFERER");
        
        // only allow POST
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null && !"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }
            
        if (rowKeys != null && rowKeys.length > 0) {
            String formDefId = getPropertyString("formDefId");
            
            if ("true".equalsIgnoreCase(getPropertyString("deleteSubformData")) || "true".equalsIgnoreCase(getPropertyString("deleteGridData"))) {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                Form form = getForm(appDef, formDefId);
                
                if (form != null) {
                    try {
                        FormDataDao formDataDao = (FormDataDao) FormUtil.getApplicationContext().getBean("formDataDao");
                        for (String id : rowKeys) {
                            deleteData(formDataDao, form, id);
                        }
                    } catch (Exception e) {
                        result.setMessage(ResourceBundleUtil.getMessage("datalist.formrowdeletedatalistaction.error.delete"));
                    }
                } else {
                    result.setMessage(ResourceBundleUtil.getMessage("datalist.formrowdeletedatalistaction.noform"));
                }
            } else {
                String tableName = getSelectedFormTableName(formDefId);
                if (tableName != null) {
                    FormDataDao formDataDao = (FormDataDao) FormUtil.getApplicationContext().getBean("formDataDao");
                    formDataDao.delete(formDefId, tableName, rowKeys);
                }
            }
        }

        return result;
    }

    public String getPropertyOptions() {
        String formDefField = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            String formJsonUrl = "[CONTEXT_PATH]/web/json/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/forms/options";
            formDefField = "{name:'formDefId',label:'@@datalist.formrowdeletedatalistaction.formId@@',type:'selectbox',options_ajax:'" + formJsonUrl + "',required:'True'}";
        } else {
            formDefField = "{name:'formDefId',label:'@@datalist.formrowdeletedatalistaction.formId@@',type:'textfield',required:'True'}";
        }
        Object[] arguments = new Object[]{formDefField};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/formRowDeleteDataListAction.json", arguments, true, "message/datalist/formRowDeleteDataListAction");
        return json;
    }

    protected String getSelectedFormTableName(String formDefId) {
        String tableName = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        if (formDefId != null) {
            tableName = appService.getFormTableName(appDef, formDefId);
        }
        return tableName;
    }
    
    protected Form getForm(AppDefinition appDef, String formDefId) {
        FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
        FormDefinitionDao formDefinitionDao = (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");
        
        Form form = null;
        FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
        
        if (formDef != null && formDef.getJson() != null) {
            String formJson = formDef.getJson();
            formJson = AppUtil.processHashVariable(formJson, null, StringUtil.TYPE_JSON, null);
            form = (Form) formService.createElementFromJson(formJson);
        }
        
        return form;
    } 
    
    protected void deleteData(FormDataDao formDataDao, Form form, String primaryKey) {
        FormData formData = new FormData();
        formData.setPrimaryKeyValue(primaryKey);
        formData.addFormResult(FormUtil.FORM_RESULT_LOAD_ALL_DATA, FormUtil.FORM_RESULT_LOAD_ALL_DATA);
        formData = FormUtil.executeLoadBinders(form, formData);
        
        Map<FormLoadBinder, FormRowSet> binders = formData.getLoadBinderMap();
        
        for (FormLoadBinder binder : binders.keySet()) {
            if (binder == form.getLoadBinder()) {
                try {
                    formDataDao.delete(form.getPropertyString(FormUtil.PROPERTY_ID), form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME), new String[]{primaryKey});
                } catch (HibernateObjectRetrievalFailureException e) {
                    //ignore
                }
            } else if (binder instanceof FormDataDeletableBinder) {
                FormDataDeletableBinder b = (FormDataDeletableBinder) binder;
                if ((binder instanceof FormLoadMultiRowElementBinder 
                        && "true".equalsIgnoreCase(getPropertyString("deleteGridData")))
                        || (!(binder instanceof FormLoadMultiRowElementBinder)
                        && "true".equalsIgnoreCase(getPropertyString("deleteSubformData")))) {
                    formDataDao.delete(b.getFormId(), b.getTableName(), binders.get(binder));
                }
            }
        }
    }

    public String getClassName() {
        return this.getClass().getName();
    }
}