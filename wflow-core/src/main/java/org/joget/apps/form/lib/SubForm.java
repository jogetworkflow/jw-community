package org.joget.apps.form.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.AbstractSubForm;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.PluginWebSupport;

public class SubForm extends AbstractSubForm implements FormBuilderPaletteElement, PluginWebSupport {

    @Override
    public String getName() {
        return "Sub Form";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "Sub Form Element";
    }
    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        
        // set subform html
        String elementMetaData = ((Boolean) dataModel.get("includeMetaData")) ? FormUtil.generateElementMetaData(this) : "";
        Collection<Element> childElements = getChildren();
        Form subForm = (childElements.size() > 0) ? (Form) getChildren().iterator().next() : null;
        String label = getPropertyString("label");
        String cellClass = ((Boolean) dataModel.get("includeMetaData")) ? "form-cell" : "subform-cell";
        String noFrame = ("true".equalsIgnoreCase(getPropertyString("noframe"))) ? " no-frame" : "";
        String html = "<div class='" + cellClass + "' " + elementMetaData + "><div class='subform-container"+noFrame+"'>";
        html += "<span class='subform-title'>" + label + "</span>";
        if (subForm != null) {
            String subFormHtml = subForm.render(formData, false);
            subFormHtml = subFormHtml.replaceAll("\"form-section", "\"subform-section");
            subFormHtml = subFormHtml.replaceAll("\"form-column", "\"subform-column");
            subFormHtml = subFormHtml.replaceAll("\"form-cell", "\"subform-cell");
            html += subFormHtml;
        } else {
            html += "SubForm could not be loaded";
        }
        html += "<div style='clear:both;'></div></div></div>";
        return html;
    }
    
    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<div class='subform-container'><span class='subform-title'>SubForm</span></div>";
    }

    @Override
    public String getLabel() {
        return "Sub Form";
    }

    @Override
    public String getPropertyOptions() {
        String formDefField = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            String formJsonUrl = "[CONTEXT_PATH]/web/json/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/forms/options";
            formDefField = "{name:'formDefId',label:'@@form.subform.formId@@',type:'selectbox',options_ajax:'" + formJsonUrl + "',required:'true'}";
        } else {
            formDefField = "{name:'formDefId',label:'@@form.subform.formId@@',type:'textfield',required:'true'}";
        }
        Object[] arguments = new Object[]{formDefField};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/form/subForm.json", arguments, true, "message/form/SubForm");
        return json;
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
        return "/plugin/org.joget.apps.form.lib.SubForm/images/subForm_icon.gif";
    }

    /**
     * Return JSON of available forms that can be embedded as this subform.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("getOptions".equals(action)) {
            Collection<FormDefinition> formDefList = new ArrayList<FormDefinition>();
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            if (appDef != null) {
                // load forms from current thread app version
                formDefList = appDef.getFormDefinitionList();
            }
            String output = "[{\"value\":\"\",\"label\":\"\"}";
            for (FormDefinition formDef : formDefList) {
                output += ",{\"value\":\"" + formDef.getId() + "\",\"label\":\"" + formDef.getName() + "\"}";
            }
            output += "]";
            response.getWriter().write(output);
        }
    }
}
