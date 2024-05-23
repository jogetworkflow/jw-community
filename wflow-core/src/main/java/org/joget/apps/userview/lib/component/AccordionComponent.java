package org.joget.apps.userview.lib.component;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.SimplePageComponent;
import org.joget.apps.userview.model.UserviewBuilderPalette;

public class AccordionComponent extends SimplePageComponent {
    @Override
    public String getName() {
        return "AccordionComponent";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/"+getName()+".json", null, true, null);
    }
    
    @Override
    public String getCategory() {
        return UserviewBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-align-left\"></i>";
    }

    @Override
    public String render(String id, String cssClass, String style, String attr, boolean isBuilder) {
        String rowAttr = "";
        if (isBuilder) {
            rowAttr += " data-cbuilder-accordionChilds";
        }
        return "<div id=\""+id+"\" "+attr+" class=\"accordion panel-group"+cssClass+"\" "+rowAttr+">" + renderChildren() + style + "</div>";
    }

    @Override
    public String getBuilderJavaScriptTemplate() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/"+getName()+"_template.json", null, true, null);
    }
    
    public String getAdditionalStyle(String styleClass) {
        String builderStyles = "";
        
        builderStyles += getAdditionalStyle(styleClass, ".card", "card-");
        builderStyles += getAdditionalStyle(styleClass, ".card-header", "header-");
        builderStyles += getAdditionalStyle(styleClass, ".card-body", "body-");
        
        return builderStyles;
    }
}
