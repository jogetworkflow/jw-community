package org.joget.apps.userview.lib.component;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.SimplePageComponent;
import org.joget.apps.userview.model.UserviewBuilderPalette;

public class LabelComponent extends SimplePageComponent {

    @Override
    public String getName() {
        return "LabelComponent";
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
        return "<i class=\"las la-font\"></i>";
    }

    @Override
    public String render(String id, String cssClass, String style, String attr, boolean isBuilder) {
        return "<label "+attr+" id=\""+id+"\" class=\""+cssClass+"\" style=\""+style+"\">" + renderChildren() + "</label>";
    }

    @Override
    public String getBuilderJavaScriptTemplate() {
        return "{'html' : '<label data-cbuilder-textContent>"+getI18nLabel()+"</label>'}";
    }
    
}