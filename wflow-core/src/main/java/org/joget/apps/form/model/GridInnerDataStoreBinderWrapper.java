package org.joget.apps.form.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.service.FileUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.UuidGenerator;

public class GridInnerDataStoreBinderWrapper extends FormBinder implements FormStoreBinder, FormDeleteBinder {
    protected final GridInnerDataRetriever dataRetriever;
    protected final FormStoreBinder storeBinder;
    protected boolean deleteGridData = false;
    protected boolean deleteSubformData = false;
    protected boolean abortProcess = false;
    protected boolean deleteFiles = false;
    protected FormData formData = null;;
    
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

    public boolean isDeleteGridData() {
        return deleteGridData;
    }

    public void setDeleteGridData(boolean deleteGridData) {
        this.deleteGridData = deleteGridData;
    }

    public boolean isDeleteSubformData() {
        return deleteSubformData;
    }

    public void setDeleteSubformData(boolean deleteSubformData) {
        this.deleteSubformData = deleteSubformData;
    }

    public boolean isAbortProcess() {
        return abortProcess;
    }

    public void setAbortProcess(boolean abortProcess) {
        this.abortProcess = abortProcess;
    }
    
    public boolean isDeleteFiles() {
        return deleteFiles;
    }

    public void setDeleteFiles(boolean deleteFiles) {
        this.deleteFiles = deleteFiles;
    }
    
    public GridInnerDataStoreBinderWrapper (GridInnerDataRetriever dataRetriever, FormStoreBinder storeBinder) {
        this.dataRetriever = dataRetriever;
        this.storeBinder = storeBinder;
    }
    
    

    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {
        this.formData = formData;
        
        //handle deleted row inner form/grid data
        handleDeletedRows(element, rows, formData, isDeleteGridData(), isDeleteSubformData(), isAbortProcess(), isDeleteFiles());
        
        if (rows != null && !rows.isEmpty()) {
            //store inner form/grid data
            storeInnerData(rows);
        }    

        storeBinder.store(element, rows, formData);
        
        return rows;
    } 
    
    public void delete(Element element, FormRowSet rows, FormData formData, boolean deleteGrid, boolean deleteSubform, boolean abortProcess, boolean deleteFiles) {
        if (deleteGrid || deleteSubform || abortProcess || deleteFiles) {
            handleDeletedRows(element, null, formData, deleteGrid, deleteSubform, abortProcess, deleteFiles);
        }
        
        if (storeBinder instanceof FormDeleteBinder) {
            ((FormDeleteBinder) storeBinder).delete(element, rows, formData, deleteGrid, deleteSubform, abortProcess, deleteFiles);
        } else if (element.getLoadBinder() != null && element.getLoadBinder() instanceof FormDataDeletableBinder) {
            FormDataDeletableBinder binder = (FormDataDeletableBinder) element.getLoadBinder();
            FormDataDao formDataDao = (FormDataDao) FormUtil.getApplicationContext().getBean("formDataDao");
            formDataDao.delete(binder.getFormId(), binder.getTableName(), rows);
        }
    }
    
    public void handleDeletedRows(Element element, FormRowSet rows, FormData formData, boolean deleteGrid, boolean deleteSubform, boolean abortProcess, boolean deleteFiles) {
        //get load binder data of this element
        FormRowSet loadedRows = formData.getLoadBinderData(element);
        Set<String> ids = new HashSet<String>();
        if (loadedRows != null && !loadedRows.isEmpty()) {
            for (FormRow r : loadedRows) {
                if (r.getId() != null && !r.getId().isEmpty()) {
                    ids.add(r.getId());
                }
            }
        }
        
        if (!ids.isEmpty() && rows != null && !rows.isEmpty()) {
            for (FormRow r : rows) {
                if (r.getId() != null && !r.getId().isEmpty() && ids.contains(r.getId())) {
                    ids.remove(r.getId());
                }
            }
        }
        
        if (!ids.isEmpty()) {
            Form innerForm = dataRetriever.getInnerForm();
            if (innerForm != null) {
                for (String id : ids) {
                    //abort process
                    if (abortProcess) {
                        FormUtil.abortRunningProcessForRecord(id);
                    }

                    //delete files
                    if (deleteFiles) {
                        FileUtil.deleteFiles(innerForm, id);
                    }
                    
                    //remove inner data
                    if (deleteGrid || deleteSubform) {
                        FormUtil.recursiveDeleteChildFormData(innerForm, id, deleteGrid, deleteSubform, abortProcess, deleteFiles);
                    }
                }
            }
        }
    }
    
    public void storeInnerData(FormRowSet rows) {
        Form innerForm = dataRetriever.getInnerForm();
        if (innerForm == null) {
            return;
        }
        
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
            } else {
                rowFormData = new FormData();
                rowFormData.setPrimaryKeyValue(r.getId());
                rowFormData.addRequestParameterValues(FormUtil.FORM_META_ORIGINAL_ID, new String[]{r.getId()});
            }
            
            if (this.formData != null && this.formData.getRequestParameter("saveAsDraft") != null) {
                rowFormData.addRequestParameterValues("saveAsDraft", this.formData.getRequestParameterValues("saveAsDraft"));
            }

            FormUtil.executePostFormSubmissionProccessor(innerForm, rowFormData);
        }
    }
    
    @Override
    public Map<String, Object> getProperties(){
        return ((FormBinder)storeBinder).getProperties();
    }
    
    @Override
    public void setProperties(Map<String, Object> properties){
        ((FormBinder)storeBinder).setProperties(properties);
    }
    
    @Override
    public Object getProperty(String property){
        return ((FormBinder)storeBinder).getProperty(property);
    }
    
    @Override
    public String getPropertyString(String property){
        return ((FormBinder)storeBinder).getPropertyString(property);
    }
    
    @Override
    public void setProperty(String property, Object value){
        ((FormBinder)storeBinder).setProperty(property, value);
    }
    
    @Override
    public Element getElement() {
        return ((FormBinder)storeBinder).getElement();
    }

    @Override
    public void setElement(Element element) {
        ((FormBinder)storeBinder).setElement(element);
    }
}
