package org.joget.apps.form.lib;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;

public class CustomHTML extends Element implements FormBuilderPaletteElement {

    @Override
    public String getName() {
        return "Custom HTML";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "Custom HTML Element";
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        String customHTML = (String) getProperty("value");

        Pattern pattern = Pattern.compile("name=\\\"([^\\\"]*)\\\"");
        Matcher matcher = pattern.matcher(customHTML);

        while (matcher.find()) {
            String name = matcher.group(1);

            //create dummy element object
            Element element = new TextField();
            element.setProperty("id", name);

            //get value from the formData
            String[] values = FormUtil.getElementPropertyValues(element, formData);
            if (values != null && values.length > 0) {
                // check for empty submission via parameter
                String[] paramValues = FormUtil.getRequestParameterValues(element, formData);
                if (paramValues == null || paramValues.length == 0) {
                    values = new String[]{""};
                }

                // formulate values
                String delimitedValue = FormUtil.generateElementPropertyValues(values);

                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                result.setProperty(name, delimitedValue);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
        }
        return rowSet;
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "customHTML.ftl";

        //get the list of names inside tag input and textarea
        String customHTML = (String) getProperty("value");

        Pattern pattern = Pattern.compile("<input [^>]* name=\\\"([^\\\"]*)\\\"");
        Matcher matcher = pattern.matcher(customHTML);

        while (matcher.find()) {
            //get the name and create an dummy element with ID set
            String name = matcher.group(1);
            Element element = new TextField();
            element.setProperty("id", name);

            //get the value
            String value = FormUtil.getElementPropertyValue(element, formData);

            if (value != null) {
                //remove value field created by user
                Pattern pattern2 = Pattern.compile("name=\\\"" + name + "\\\"[.]* value=\\\"([^\\\"]*)\\\"");
                Matcher matcher2 = pattern2.matcher(customHTML);
                customHTML = matcher2.replaceFirst("name=\"" + name + "\"");

                pattern2 = Pattern.compile("value=\\\"([^\\\"]*)\\\"[.]* name=\\\"" + name + "\\\"");
                matcher2 = pattern2.matcher(customHTML);
                customHTML = matcher2.replaceFirst("name=\"" + name + "\"");

                //inject value into customHTML
                pattern2 = Pattern.compile("name=\\\"" + name + "\\\"");
                matcher2 = pattern2.matcher(customHTML);
                customHTML = matcher2.replaceFirst("name=\"" + name + "\" value=\"" + value + "\"");
            }
        }

        dataModel.put("value", customHTML);

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>Custom HTML</label><span class='form-floating-label'>CUSTOM HTML</span>";
    }

    @Override
    public String getLabel() {
        return "Custom HTML";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/customHtml.json", null, true, "message/form/CustomHTML");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_CUSTOM;
    }

    @Override
    public int getFormBuilderPosition() {
        return 1200;
    }

    @Override
    public String getFormBuilderIcon() {
        return "/plugin/org.joget.apps.form.lib.TextArea/images/textArea_icon.gif";
    }
}
