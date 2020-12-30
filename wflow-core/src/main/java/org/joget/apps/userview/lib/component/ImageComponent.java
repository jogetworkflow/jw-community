package org.joget.apps.userview.lib.component;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.SimplePageComponent;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.workflow.util.WorkflowUtil;

public class ImageComponent extends SimplePageComponent {

    @Override
    public String getName() {
        return "ImageComponent";
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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/"+getName()+".json", new Object[]{WorkflowUtil.getHttpServletRequest().getContextPath()}, true, null);
    }
    
    @Override
    public String getCategory() {
        return UserviewBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public String getIcon() {
        return "<i class=\"lar la-image\"></i>";
    }

    @Override
    public String render(String id, String cssClass, String style, String attr, boolean isBuilder) {
        return "<img "+attr+" id=\""+id+"\" class=\""+cssClass+"\" style=\""+style+"\" />";
    }

    @Override
    public String getBuilderJavaScriptTemplate() {
        return "{'html' : '<img src=\""+WorkflowUtil.getHttpServletRequest().getContextPath()+"/builder/icons/image.svg\" width=\"128\" height=\"128\"/>'}";
    }
    
}
