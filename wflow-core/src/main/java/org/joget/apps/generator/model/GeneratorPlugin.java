package org.joget.apps.generator.model;

import org.joget.apps.app.model.AppDefinition;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;

public abstract class GeneratorPlugin extends ExtDefaultPlugin implements PropertyEditable {
    protected String formId;
    protected AppDefinition appDef;

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public AppDefinition getAppDefinition() {
        return appDef;
    }

    public void setAppDefinition(AppDefinition appDef) {
        this.appDef = appDef;
    }
    
    public boolean isDisabled() {
        return false;
    }
    
    public String getDefaultPropertyValues(){
        return PropertyUtil.getDefaultPropertyValues(getPropertyOptions());
    }

    public abstract String getExplanation();

    public abstract GeneratorResult generate();

}
