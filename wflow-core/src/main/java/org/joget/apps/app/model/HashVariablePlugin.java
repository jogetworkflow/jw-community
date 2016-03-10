package org.joget.apps.app.model;

import java.util.Collection;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * Interface used to develop Hash Variable Plugin
 * 
 */
public interface HashVariablePlugin extends PropertyEditable {

    /**
     * Prefix of a Hash Variable use as identifier 
     * 
     * @return 
     */
    public abstract String getPrefix();

    /**
     * Processing the Hash Variable and return a value to replace the Hash Variable.
     * 
     * @return Null to skip the replacement of Hash Variable
     */
    public abstract String processHashVariable(String variableKey);
    
    /**
     * Escape special character in the value
     * 
     * @return 
     */
    public String escapeHashVariable(String variable);
    
    /**
     * List the possible syntax combination to populate in Hash Variable Assistants
     * in Property Editor
     * 
     * @return 
     */
    public abstract Collection<String> availableSyntax();
}
