package org.joget.apps.app.model;

import java.util.ArrayList;
import java.util.Collection;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.ExtDefaultPlugin;

public abstract class DefaultHashVariablePlugin extends ExtDefaultPlugin implements HashVariablePlugin {
    public String escapeHashVariable(String variable) {
        return StringUtil.escapeString(variable, StringUtil.TYPE_REGEX, null);
    }
    
    public Collection<String> availableSyntax() {
        Collection <String> list = new ArrayList<String>();
        
        list.add(getPrefix() + ".KEY");
        
        return list;
    }
}
