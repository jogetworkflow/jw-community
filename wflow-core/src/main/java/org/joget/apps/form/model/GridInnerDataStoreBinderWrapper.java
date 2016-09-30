package org.joget.apps.form.model;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.UuidGenerator;

public class GridInnerDataStoreBinderWrapper extends FormBinder implements FormStoreBinder {
    private final GridInnerDataRetriever dataRetriever;
    private final FormStoreBinder storeBinder;
    
    public String getName() {
        return "GridInnerDataStoreBinderWrapper";
    }

    public String getVersion() {
        return "6.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Grid Inner Data Store Binder Wrapper";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
    
    public GridInnerDataStoreBinderWrapper (GridInnerDataRetriever dataRetriever, FormStoreBinder storeBinder) {
        this.dataRetriever = dataRetriever;
        this.storeBinder = storeBinder;
    }

    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {
        if (rows != null && !rows.isEmpty()) {
            
            //store inner form/grid data
            storeInnerData(rows);

            storeBinder.store(element, rows, formData);
        }
        
        return rows;
    } 
    
    public void storeInnerData(FormRowSet rows) {
        Form innerForm = dataRetriever.getInnerForm();
        FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
        
        for (FormRow r : rows) {
            FormData rowFormData = dataRetriever.getFormData(r);
            if (rowFormData != null) {
                if (rowFormData.getPrimaryKeyValue() == null || rowFormData.getPrimaryKeyValue().isEmpty()) {
                    if (r.getId() == null || r.getId().isEmpty()) {
                        r.setId(UuidGenerator.getInstance().getUuid());
                    }
                    rowFormData.setPrimaryKeyValue(r.getId());
                }
                
                //format data
                FormUtil.executeElementFormatData(innerForm, rowFormData);
                
                if (rowFormData.getStoreBinders().size() > 1) {
                    //skip the form element and start with its child
                    for (Element e : innerForm.getChildren()) {
                        formService.recursiveExecuteFormStoreBinders(innerForm, e, rowFormData);
                    }
                }
            }
        }
    }
}
