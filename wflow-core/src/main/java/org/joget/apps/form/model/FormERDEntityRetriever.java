package org.joget.apps.form.model;

import org.joget.apps.form.service.FormERD;

public interface FormERDEntityRetriever {
    
    public String getEntityTable();
    
    public String getForeignKey();
    
    public FormERD.Entity getEntity();
}
