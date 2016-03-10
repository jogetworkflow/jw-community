package org.joget.apps.app.model;

import java.util.ArrayList;
import java.util.Collection;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.ExtDefaultPlugin;

/**
 * A base abstract class to develop a Hash Variable plugin. 
 * 
 */
public abstract class DefaultHashVariablePlugin extends ExtDefaultPlugin implements HashVariablePlugin {
    
    /**
     * Escape special character in the value.
     * 
     * Default to escape Regex in the value
     * 
     * @return 
     */
    public String escapeHashVariable(String variable) {
        return StringUtil.escapeString(variable, StringUtil.TYPE_REGEX, null);
    }
    
    /**
     * List the possible syntax combination to populate in Hash Variable Assistants
     * in Property Editor
     * 
     * Default to "<i>prefix</i>.KEY"
     * 
     * @return 
     */
    public Collection<String> availableSyntax() {
        Collection <String> list = new ArrayList<String>();
        
        list.add(getPrefix() + ".KEY");
        
        return list;
    }
}
