package org.joget.apps.userview.lib.component;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.SimplePageComponent;
import org.joget.apps.userview.model.UserviewBuilderPalette;

public class ButtonComponent extends SimplePageComponent {

    @Override
    public String getName() {
        return "ButtonComponent";
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
        return "/builder/icons/button.svg";
    }

    @Override
    public String render(String id, String cssClass, String style, String attr, boolean isBuilder) {
        return "<a "+attr+" id=\""+id+"\" class=\""+cssClass+"\">" + renderChildren() + style + "</a>";
    }

    @Override
    public String getBuilderJavaScriptTemplate() {
        return "{'html' : '<a class=\"\" data-cbuilder-textContent>"+getI18nLabel()+"</a>'}";
    }
    
}
