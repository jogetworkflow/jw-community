package org.joget.apps.form.model;

import java.util.Map;

public class MissingElement extends Element {
    private String className = "";

    public MissingElement() {
    }
            
    public MissingElement(String className) {
        this.className = className;
    }
            
    public String getName() {
        return "Missing Element";
    }

    public String getVersion() {
        return "5.0.3";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "";
    }

    public String getClassName() {
        return this.className;
    }

    public String getPropertyOptions() {
        return "";
    }
    
    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        if (dataModel.get("elementMetaData") != null && !dataModel.get("elementMetaData").toString().isEmpty()) {
            return "<div class=\"form-cell\" "+dataModel.get("elementMetaData")+" style=\"display:none\"></div>";
        } else {
            return "";
        }
    }
}
