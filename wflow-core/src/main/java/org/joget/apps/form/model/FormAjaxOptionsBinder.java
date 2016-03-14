package org.joget.apps.form.model;

import java.io.Serializable;

public interface FormAjaxOptionsBinder extends Serializable {
    
    public boolean useAjax();
    
    public FormRowSet loadAjaxOptions(String[] dependencyValues);
}
