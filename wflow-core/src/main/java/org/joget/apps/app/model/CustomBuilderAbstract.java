package org.joget.apps.app.model;

import org.joget.apps.app.service.CustomBuilderUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;

public abstract class CustomBuilderAbstract extends ExtDefaultPlugin implements CustomBuilder, PropertyEditable {
    
    @Override
    public String createNewJSON(String id, String name, String description, BuilderDefinition copyDef) {
        return CustomBuilderUtil.createNewJSON(this, id, name, description, copyDef);
    }

    @Override
    public String getNameFromJSON(String json) {
        return CustomBuilderUtil.getNameFromJSON(json);
    }

    @Override
    public String getDescriptionFromJSON(String json) {
        return CustomBuilderUtil.getDescriptionFromJSON(json);
    }
    
    @Override
    public String getCreateNewPageHtml() {
        return "";
    }
}
