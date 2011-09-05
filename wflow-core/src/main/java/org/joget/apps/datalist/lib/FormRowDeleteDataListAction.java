package org.joget.apps.datalist.lib;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListActionDefault;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.service.FormUtil;

/**
 * Test implementation for an action
 */
public class FormRowDeleteDataListAction extends DataListActionDefault {

    public String getName() {
        return "Form Row Delete Action";
    }

    public String getVersion() {
        return "1.0.0";
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
        return "";
    }

    public String getTarget() {
        return "";
    }

    public String getHrefParam() {
        return "";
    }

    public String getHrefColumn() {
        return "";
    }

    public String getConfirmation() {
        String confirm = getPropertyString("confirmation");
        if (confirm == null || confirm.isEmpty()) {
            confirm = "Please Confirm";
        }
        return confirm;
    }

    public DataListActionResult executeAction(DataList dataList, String[] rowKeys) {
        DataListActionResult result = null;

        Form form = getSelectedForm();
        if (form != null) {
            FormDataDao formDataDao = (FormDataDao) FormUtil.getApplicationContext().getBean("formDataDao");
            formDataDao.delete(form, rowKeys);

            result = new DataListActionResult();
            result.setType(DataListActionResult.TYPE_REDIRECT);
            result.setUrl("REFERER");
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

    protected Form getSelectedForm() {
        Form form = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        String formDefId = getPropertyString("formDefId");
        if (formDefId != null) {
            form = appService.viewDataForm(appDef.getId(), appDef.getVersion().toString(), formDefId, null, null, null, null, null, null);
        }
        return form;
    }

    public String getClassName() {
        return this.getClass().getName();
    }
}
