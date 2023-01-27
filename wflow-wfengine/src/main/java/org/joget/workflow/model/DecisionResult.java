package org.joget.workflow.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DecisionResult {

    protected Map<String, String> variables = new HashMap<String, String>();
    protected Collection<String> transitions = null;
    protected Boolean isAndSplit = null;
    protected String auditData;

    public Boolean getIsAndSplit() {
        return isAndSplit;
    }

    public void setIsAndSplit(Boolean isAndSplit) {
        this.isAndSplit = isAndSplit;
    }

    public String getAuditData() {
        return auditData;
    }

    public void setAuditData(String auditData) {
        this.auditData = auditData;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariable(String name, String value) {
        this.variables.put(name, value);
    }

    public Collection<String> getTransitions() {
        return transitions;
    }

    public void addTransition(String idOrName) {
        if (this.transitions == null) {
            this.transitions = new ArrayList<String>();
        }
        this.transitions.add(idOrName);
    }
}
