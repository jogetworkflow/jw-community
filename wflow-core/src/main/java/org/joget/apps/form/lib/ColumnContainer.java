package org.joget.apps.form.lib;

import java.util.Collection;
import java.util.Map;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderEditable;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

/**
 * Represents a single column in the Columns layout.
 * Acts as a structural container inside Form Builder.
 */
public class ColumnContainer extends Element implements FormBuilderEditable, FormContainer {

    @Override
    public String getName() {
        return "ColumnsChild";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<div class='col' data-cbuilder-subelement data-cbuilder-elements></div>";
    }

    @Override
    public String getDescription() {
        return "Columns Child Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {

        String readonlyValue = getPropertyString(FormUtil.PROPERTY_READONLY);
        boolean columnReadonly = "readonly".equalsIgnoreCase(readonlyValue) || "true".equalsIgnoreCase(readonlyValue);
        int version = "readonly".equalsIgnoreCase(readonlyValue) ? 2 : 1;

        StringBuilder style = new StringBuilder();
        String flex = getPropertyString("flex");
        if (flex.isEmpty()) {
            flex = getPropertyString("style-flex");
        }
        if (!flex.isEmpty()) {
            style.append("flex:").append(flex).append(";");
        }

        String width = getPropertyString("width");
        if (width.isEmpty()) {
            width = getPropertyString("style-max-width");
        }
        if (!width.isEmpty()) {
            style.append("max-width:").append(width).append(";");
        }

        Boolean includeMetaData = (Boolean) dataModel.get("includeMetaData");
        String meta = "";
        if (includeMetaData != null && includeMetaData) {
            meta = (String) dataModel.get("elementMetaData");
            meta += " data-cbuilder-subelement data-cbuilder-elements";
        }

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"col\" style=\"").append(style).append("\" ").append(meta).append(">");

        Collection<Element> children = getChildren();
        if (children != null) {
            int childIndex = 0;
            for (Element child : children) {
                if (columnReadonly) {
                    FormUtil.setReadOnlyProperty(child, true, false, version);
                }
                html.append(child.render(formData, includeMetaData));
                childIndex++;
            }
        }

        html.append("</div>");
        return html.toString();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "Column";
    }

    @Override
    public String getPropertyOptions() {
        return "[]";
    }
}
