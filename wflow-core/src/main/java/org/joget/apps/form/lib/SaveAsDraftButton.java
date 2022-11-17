package org.joget.apps.form.lib;

import org.joget.apps.app.service.AppService;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormButton;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

/**
 * Form button to save form data as draft without validation.
 */
public class SaveAsDraftButton extends FormButton {

    public static final String DEFAULT_ID = "draft";

    @Override
    public String getName() {
        return "Save As Draft Button";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Save As Draft Button";
    }

    @Override
    public FormData actionPerformed(Form form, FormData formData) {
        AppService appService = (AppService) FormUtil.getApplicationContext().getBean("appService");
        FormData updatedFormData = appService.submitForm(form, formData, true);
        return updatedFormData;
    }

    /*
    @Override
    public String getFormBuilderTemplate() {
    return "<input type='submit' value='Save As Draft' />";
    }

    @Override
    public String getPropertyOptions() {
    return "[{title:'Edit Button', properties:[{name:'id',label:'ID',type:'textfield',required:'True'},{name:'label',label:'Label',type:'textfield',required:'True'}]}]";
    }

    @Override
    public String getDefaultPropertyValues() {
    return "{id:'" + DEFAULT_ID + "',label:'Save As Draft'}";
    }

    @Override
    public String getFormBuilderCategory() {
    return FormBuilderPalette.CATEGORY_CUSTOM;
    }

    @Override
    public int getFormBuilderPosition() {
    return 2200;
    }

    @Override
    public String getFormBuilderIcon() {
    return null;
    }
     */

    public String getLabel() {
        return "Save As Draft Button";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
}
