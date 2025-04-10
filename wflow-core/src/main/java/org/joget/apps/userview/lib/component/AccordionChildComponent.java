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
        String show = "show in";
        String builderAttr = "";
        if (isBuilder) {
            attr += " data-cbuilder-subelement";
            builderAttr += " data-cbuilder-elements";
        } else {
            if (!"true".equals(getPropertyString("expanded"))) {
                show = "";
            }
        }
        
        String parentId = "";
        if (getParent() != null) {
            parentId = getParent().getPropertyString("customId");
            if (parentId.isEmpty()) {
                parentId = getParent().getPropertyString("id");
            }
        }
        
        String collapse = "";
        if(show.isEmpty()){
            collapse = "collapsed";
        }
        
        String html = "<div id=\""+id+"\" class=\"panel accordion-item\" "+attr+">";
        html += "<h2 class=\"accordion-header\"><button class=\"panel-heading accordion-heading accordion-button accordion-toggle " + collapse+ "\" id=\"heading-"+id+"\" tabindex=\"0\" data-bs-toggle=\"collapse\" data-bs-target=\"#body-"+id+"\" data-parent=\"#pc-"+parentId+"\" role=\"button\" aria-expanded=\""+(!show.isEmpty())+"\" aria-controls=\"body-"+id+"\">"+getPropertyString("label")+"</button></h2>";
        html += "<div id=\"body-"+id+"\" tabindex=\"0\" class=\"card-body accordion-collapse collapse "+show+"\" data-parent=\"#pc-"+parentId+"\" "+builderAttr+">" + renderChildren() + "</div> "+ style + "</div>";
        
        return html;
    }

    @Override
    public String getBuilderJavaScriptTemplate() {
        return "{'parentContainerAttr' : 'accordionChilds'}";
    }
}

