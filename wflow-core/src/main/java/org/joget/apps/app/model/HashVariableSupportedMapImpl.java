package org.joget.apps.app.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.HashVariableSupportedMap;
import org.joget.workflow.model.WorkflowAssignment;

public class HashVariableSupportedMapImpl<K,V> extends HashVariableSupportedMap<K,V> {
    protected AppDefinition appDef;
    protected WorkflowAssignment assignment;
    
    public HashVariableSupportedMapImpl(Map<K,V> initialValues) {
        super(initialValues);
        
        if (initialValues instanceof HashVariableSupportedMapImpl) {
            this.assignment = ((HashVariableSupportedMapImpl) initialValues).assignment;
            this.appDef = ((HashVariableSupportedMapImpl) initialValues).appDef;
        } else {
            this.assignment = AppUtil.getCurrentAssignment();
            this.appDef = AppUtil.getCurrentAppDefinition();
        }
        AppUtil.resetCurrentAssignment(); //reset the current assignment after use it
    }
    
    protected HashVariableSupportedMapImpl(AppDefinition appDef, WorkflowAssignment assignment, Map<K,V> initialValues) {
        super(initialValues);
        this.appDef = appDef;
        this.assignment = assignment;
    }
    
    @Override
    public Object clone() {
        this.isInternal = true;
        HashVariableSupportedMapImpl<K,V> clone = new HashVariableSupportedMapImpl<>(appDef, assignment, (HashMap<K,V>) ((HashMap) this.initialMap).clone());
        clone.putAll(this);
        clone.isInternal = this.isInternal;
        return clone;
    }

    @Override
    protected Object getProcessedValue(V value) {
        Object newValue = null;
        if (value instanceof Map && !(value instanceof HashVariableSupportedMapImpl)) {
            newValue = new HashVariableSupportedMapImpl(appDef, assignment, (Map) value);
        } else if (value instanceof Object[]) {
            Object[] objArr = (Object[]) value;
            if (objArr.length > 0 && objArr[0] instanceof Map && !(objArr[0] instanceof HashVariableSupportedMapImpl)) {
                Collection arr = new ArrayList();
                for (Object v : objArr) {
                    arr.add(new HashVariableSupportedMapImpl(appDef, assignment, (Map) v));
                }
                newValue = arr.toArray(new HashVariableSupportedMapImpl[0]);
            } else {
                newValue = value;
            }
        } else if (value instanceof String) {
            newValue = AppUtil.processHashVariable((String) value, assignment, null, null, appDef, true);
        } else {
            newValue = value;
        }
        return newValue;
    }
}
