package org.joget.apps.userview.lib.component;

import org.joget.apps.userview.model.SimplePageComponent;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.plugin.base.HiddenPlugin;

public class AccordionChildComponent extends SimplePageComponent implements HiddenPlugin {

    @Override
    public String getName() {
        return "AccordionChildComponent";
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
        return "[]";
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
        String show = "";
        String builderAttr = "";
        if (isBuilder) {
            attr += " data-cbuilder-subelement";
            builderAttr += " data-cbuilder-elements";
            show = "show";
        } else {
            show = ("true".equals(getPropertyString("expanded")))?"show":"";
        }
        
        String parentId = "";
        if (getParent() != null) {
            parentId = getParent().getPropertyString("customId");
            if (parentId.isEmpty()) {
                parentId = getParent().getPropertyString("id");
            }
        }
        
        String html = "<div id=\""+id+"\" class=\"card\" "+attr+">";
        html += "<a class=\"card-header\" id=\"heading-"+id+"\" data-toggle=\"collapse\" data-target=\"#body-"+id+"\" aria-expanded=\""+("show".equals(show))+"\" aria-controls=\"body-"+id+"\">"+getPropertyString("label")+"</a>";
        html += "<div id=\"body-"+id+"\" class=\"card-body collapse "+show+"\" aria-labelledby=\"heading-"+id+"\" data-parent=\"#pc-"+parentId+"\" "+builderAttr+">" + renderChildren() + "</div> "+ style + "</div>";
        
        return html;
    }

    @Override
    public String getBuilderJavaScriptTemplate() {
        return "{'parentContainerAttr' : 'accordionChilds'}";
    }
}

