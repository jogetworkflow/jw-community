package org.joget.apps.form.lib;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormButton;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

/**
 * Form button that simply links to a URL e.g. cancel button
 */
public class LinkButton extends FormButton {

    public static final String DEFAULT_ID = "link";

    @Override
    public String getName() {
        return "Link Button";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "Link Button";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        if (getPropertyString("target") == null || getPropertyString("target").isEmpty()) {
            setProperty("target", "top");
        }
        
        String template = "linkButton.ftl";
        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public FormData actionPerformed(Form form, FormData formData) {
        Logger.getLogger(LinkButton.class.getName()).log(Level.INFO, " -- LinkButton actionPerformed " + FormUtil.getElementParameterName(this));
        return formData;
    }

    /*
    @Override
    public String getFormBuilderTemplate() {
    return "<button>Link</button>";
    }

    @Override
    public String getPropertyOptions() {
    return "[{title:'Edit Button', properties:[{name:'id',label:'ID',type:'textfield',required:'True'},{name:'label',label:'Label',type:'textfield',required:'True'},{name:'url',label:'URL',type:'textfield',required:'True'}]}]";
    }

    @Override
    public String getDefaultPropertyValues() {
    return "{id:'" + DEFAULT_ID + "',label:'Link'}";
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
        return "Link Button";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
}
