package org.joget.apps.form.model;

import java.util.ArrayList;
import java.util.Collection;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.HiddenField;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.beans.BeansException;

/**
 * An abstract class to develop a Form Field element which embed a Form as its child.
 * 
 */
public abstract class AbstractSubForm extends Element implements FormContainer {
    public static final String PROPERTY_PARENT_SUBFORM_ID = "parentSubFormId";
    public static final String PROPERTY_SUBFORM_PARENT_ID = "subFormParentId";
    
    @Override
    public Collection<Element> getChildren(FormData formData) {
        Collection<Element> children = super.getChildren();
        if (children == null || children.isEmpty()) {
            // override getChildren to return the subform
            if (checkForRecursiveForm(this, getPropertyString("formDefId"))) {
                Form subForm = loadSubForm(formData);

                if (subForm != null) {
                    children = new ArrayList<Element>();
                    children.add(subForm);
                    setChildren(children);
                }
            }
        }
        return children;
    }
    
    /**
     * Retrieve a Form object as subform. This method will use either value from 
     * property key "formDefId" or "json" to construct the Form object
     * 
     * @return
     * @throws BeansException
     */
    protected Form loadSubForm(FormData parentFormData) throws BeansException {
        FormData formData = new FormData();
        Form subForm = null;
        FormService formService = (FormService) FormUtil.getApplicationContext().getBean("formService");

        String json = getPropertyString("json");
        String formDefId = getPropertyString("formDefId");
        if (formDefId != null && !formDefId.isEmpty()) {
            // load subform
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            FormDefinitionDao formDefinitionDao = (FormDefinitionDao) FormUtil.getApplicationContext().getBean("formDefinitionDao");
            FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
            if (formDef != null) {
                json = formDef.getJson();
            }
        }
        if (json != null && json.trim().length() > 0) {
            if (parentFormData != null && parentFormData.getProcessId() != null && !parentFormData.getProcessId().isEmpty()) {
                formData.setProcessId(parentFormData.getProcessId());
                WorkflowManager wm = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
                WorkflowAssignment wfAssignment = wm.getAssignmentByProcess(parentFormData.getProcessId());
                json = AppUtil.processHashVariable(json, wfAssignment, StringUtil.TYPE_JSON, null);
            }
            
            // use the json definition to create the subform
            try {
                subForm = (Form) formService.createElementFromJson(json);
                
                //if id field not exist, automatically add an id hidden field
                Element idElement = FormUtil.findElement(FormUtil.PROPERTY_ID, subForm, formData);
                if (idElement == null) {
                    Collection<Element> subFormElements = subForm.getChildren();
                    idElement = new HiddenField();
                    idElement.setProperty(FormUtil.PROPERTY_ID, FormUtil.PROPERTY_ID);
                    idElement.setParent(subForm);
                    subFormElements.add(idElement);
                }
                
            } catch (Exception e) {
                LogUtil.error(AbstractSubForm.class.getName(), e, null);
            }
        }

        if (subForm != null) {
            // set parent
            subForm.setParent(this);

            // replace binder(s) if necessary
            FormLoadBinder loadBinder = getLoadBinder();
            if (loadBinder != null) {
                ((FormBinder) loadBinder).setElement(subForm);
                subForm.setLoadBinder(loadBinder);
            } else {
                setLoadBinder(subForm.getLoadBinder());
            }
            FormStoreBinder storeBinder = getStoreBinder();
            if (storeBinder != null) {
                ((FormBinder) storeBinder).setElement(subForm);
                subForm.setStoreBinder(storeBinder);
            } else {
                setStoreBinder(subForm.getStoreBinder());
            }
            // recursively update parameter names for child elements
            String parentId = getCustomParameterName();
            if (parentId == null || parentId.isEmpty()) {
                parentId = getPropertyString(FormUtil.PROPERTY_ID);
            }
            updateElementParameterNames(subForm, parentId);
        }

        return subForm;
    }
    
    /**
     * Update all the parameter name of field elements in subform with a prefix 
     * 
     * @param element
     * @param prefix 
     */
    protected void updateElementParameterNames(Element element, String prefix) {
        if (prefix == null) {
            prefix = "";
        } else {
            String paramName = prefix + "_" + element.getPropertyString(FormUtil.PROPERTY_ID);
            element.setCustomParameterName(paramName);
        }
            
        if (element instanceof Form || element instanceof AbstractSubForm) {
            String formId = element.getPropertyString(FormUtil.PROPERTY_ID);
            if (formId == null) {
                formId = "";
            }
            prefix += "_" + formId;
        }

        // prefix parent ID set custom parameter name and readonly property for each child recursively
        boolean readonly = Boolean.valueOf(getPropertyString(FormUtil.PROPERTY_READONLY)).booleanValue();
        boolean readonlyLabel = Boolean.valueOf(getPropertyString(FormUtil.PROPERTY_READONLY_LABEL)).booleanValue();
        Collection<Element> children = element.getChildren();
        for (Element child : children) {
            if (readonly) {
                child.setProperty(FormUtil.PROPERTY_READONLY, "true");
            }
            if (readonlyLabel) {
                child.setProperty(FormUtil.PROPERTY_READONLY_LABEL, "true");
            }
            updateElementParameterNames(child, prefix);
        }
    }
    
    /**
     * Update parent form field value with primary key of subform based on 
     * property key of this constant PROPERTY_PARENT_SUBFORM_ID.
     * 
     * @param formData 
     */
    protected void populateParentWithSubFormKey(FormData formData) {
        String parentSubFormId = getPropertyString(PROPERTY_PARENT_SUBFORM_ID);
        if (parentSubFormId != null && !parentSubFormId.trim().isEmpty()) {
            Form subForm = getSubForm(formData);
            
            if (subForm != null) {

                // get subform's primary key value
                String subFormPrimaryKeyValue = null;
                Element subFormPrimaryElement = FormUtil.findElement(FormUtil.PROPERTY_ID, subForm, formData);
                if (subFormPrimaryElement != null) {
                    subFormPrimaryElement.formatData(formData);
                    subFormPrimaryKeyValue = FormUtil.getElementPropertyValue(subFormPrimaryElement, formData);
                }
                
                //try get value from parent field
                Form rootForm = FormUtil.findRootForm(this);
                String parentSubFormIdElementValue = "";
                Element parentSubFormIdElement = null;
                        
                if (FormUtil.PROPERTY_ID.equals(parentSubFormId) && rootForm.getParent() == null && formData.getPrimaryKeyValue() != null && !formData.getPrimaryKeyValue().isEmpty()) {
                    parentSubFormIdElementValue = formData.getPrimaryKeyValue();
                } else {
                    parentSubFormIdElement = FormUtil.findElement(parentSubFormId, rootForm, formData);
                    
                    if (parentSubFormIdElement != null) {
                        parentSubFormIdElementValue = FormUtil.getElementPropertyValue(parentSubFormIdElement, formData);
                    }
                }
                
                // generate new ID if empty
                if (subFormPrimaryKeyValue == null || subFormPrimaryKeyValue.trim().isEmpty()) {
                    // generate new ID
                    if (parentSubFormIdElementValue != null && !parentSubFormIdElementValue.isEmpty()) {
                        subFormPrimaryKeyValue = parentSubFormIdElementValue;
                    } else {
                        subFormPrimaryKeyValue = UuidGenerator.getInstance().getUuid();
                    }
                    
                    if (subFormPrimaryElement != null) {
                        // add into form data
                        String paramName = FormUtil.getElementParameterName(subFormPrimaryElement);
                        formData.addRequestParameterValues(paramName, new String[]{subFormPrimaryKeyValue});
                    } else {
                        // set value into subform's data
                        FormStoreBinder storeBinder = subForm.getStoreBinder();
                        if (storeBinder != null) {
                            FormRow row = null;
                            FormRowSet subFormRowSet = formData.getStoreBinderData(storeBinder);
                            if (subFormRowSet == null) {
                                subFormRowSet = new FormRowSet();
                            }
                            if (!subFormRowSet.isEmpty()) {
                                row = subFormRowSet.get(0);
                            } else {
                                row = new FormRow();
                                subFormRowSet.add(row);
                            }
                            row.setProperty(FormUtil.PROPERTY_ID, subFormPrimaryKeyValue);
                        }
                    }
                }

                // set value into root form's data
                if (subFormPrimaryKeyValue != null && !subFormPrimaryKeyValue.isEmpty() && !parentSubFormIdElementValue.equals(subFormPrimaryKeyValue)) {
                    
                    FormStoreBinder rootStoreBinder = rootForm.getStoreBinder();
                    if (rootStoreBinder != null) {
                        FormRowSet rootFormRowSet = formData.getStoreBinderData(rootStoreBinder);
                        if (!rootFormRowSet.isEmpty()) {
                            FormRow row = rootFormRowSet.get(0);
                            row.setProperty(parentSubFormId, subFormPrimaryKeyValue);
                        }
                    }
                    //add to request param to prevent overwrite
                    if (parentSubFormIdElement != null) {
                        String paramName = FormUtil.getElementParameterName(parentSubFormIdElement);
                        formData.addRequestParameterValues(paramName, new String[]{subFormPrimaryKeyValue});
                    }
                    
                    //set to form data primary key if the parent field is id field and the parent form is root form
                    if (FormUtil.PROPERTY_ID.equals(parentSubFormId) && rootForm.getParent() == null) {
                        formData.setPrimaryKeyValue(subFormPrimaryKeyValue);
                    }
                    
                    //if readonly hidden field
                    if (parentSubFormIdElement instanceof HiddenField && FormUtil.isReadonly(parentSubFormIdElement, formData)) {
                        parentSubFormIdElement.setProperty("value", subFormPrimaryKeyValue);
                    }
                }
            }
        }
    }

    /**
     * Update subform field value with primary key of parent form based on 
     * property key of this constant PROPERTY_SUBFORM_PARENT_ID.
     * 
     * @param formData 
     */
    protected void populateSubFormWithParentKey(FormData formData) {
        String subFormParentId = getPropertyString(PROPERTY_SUBFORM_PARENT_ID);
        if (subFormParentId != null && !subFormParentId.trim().isEmpty()) {
            Form subForm = getSubForm(formData);
            
            if (subForm != null) {

                // get root form's primary key value
                String rootFormPrimaryKeyValue = null;
                Form rootForm = FormUtil.findRootForm(this);
                Element rootFormPrimaryElement = FormUtil.findElement(FormUtil.PROPERTY_ID, rootForm, formData);
                if (rootFormPrimaryElement != null) {
                    rootFormPrimaryKeyValue = FormUtil.getElementPropertyValue(rootFormPrimaryElement, formData);
                } else {
                    // look in the root form rowset
                    FormStoreBinder rootStoreBinder = rootForm.getStoreBinder();
                    if (rootStoreBinder != null) {
                        FormRowSet rootFormRowSet = formData.getStoreBinderData(rootStoreBinder);
                        if (!rootFormRowSet.isEmpty()) {
                            FormRow row = rootFormRowSet.get(0);
                            rootFormPrimaryKeyValue = row.getId();
                        }
                    }
                }
                if (rootFormPrimaryKeyValue == null || rootFormPrimaryKeyValue.isEmpty()) {
                    rootFormPrimaryKeyValue = formData.getPrimaryKeyValue();
                }
                // set value into subform's data
                if (rootFormPrimaryKeyValue != null && !rootFormPrimaryKeyValue.isEmpty()) {
                    Element subFormForeignKeyElement = FormUtil.findElement(subFormParentId, subForm, formData);
                    if (subFormForeignKeyElement != null) {
                        String paramName = FormUtil.getElementParameterName(subFormForeignKeyElement);
                        formData.addRequestParameterValues(paramName, new String[]{rootFormPrimaryKeyValue});
                        
                        //if readonly hidden field
                        if (subFormForeignKeyElement instanceof HiddenField && FormUtil.isReadonly(subFormForeignKeyElement, formData)) {
                            subFormForeignKeyElement.setProperty("value", rootFormPrimaryKeyValue);
                        }
                    }
                }

            }
        }
    }
    
    @Override
    public String getPrimaryKeyValue(FormData formData) {
        String primaryKeyValue = null;
        if (formData != null && getLoadBinder() != null) {
            // custom load binder is used, custom primary key value
            Form rootForm = FormUtil.findRootForm(this);
            String parentSubFormId = getPropertyString(PROPERTY_PARENT_SUBFORM_ID);
            // get value from parent field
            Element foreignKeyElement = FormUtil.findElement(parentSubFormId, rootForm, formData);
            if (foreignKeyElement != null) {
                primaryKeyValue = FormUtil.getElementPropertyValue(foreignKeyElement, formData);
            }
            if (primaryKeyValue == null || primaryKeyValue.trim().isEmpty()) {
                primaryKeyValue = super.getPrimaryKeyValue(formData);
            }
        } else {
            primaryKeyValue = super.getPrimaryKeyValue(formData);
        }
        return primaryKeyValue;
    }
    
    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // set parent field with subform's primary key value
        populateParentWithSubFormKey(formData);

        // set subform field with parent's primary key value
        populateSubFormWithParentKey(formData);

        return rowSet;
    }
    
    /**
     * Check the subform is not exist in the parent elements tree.
     * 
     * @param e
     * @param id
     * @return 
     */
    protected boolean checkForRecursiveForm(Element e, String id) {
        //Recursive find parent and compare
        Form form = FormUtil.findRootForm(e);
        if (form != null && form != e) {
            String formId = form.getPropertyString(FormUtil.PROPERTY_ID);
            if (id.equals(formId)) {
                return false;
            } else {
                return checkForRecursiveForm(form, id);
            }
        }
        return true;
    }
    
    /**
     * Get From object from its children.
     * 
     * @param formData
     * @return 
     */
    protected Form getSubForm(FormData formData) {
        Collection<Element> children = getChildren(formData);
        if (children != null && !children.isEmpty()) {
            return (Form) getChildren().iterator().next();
        }
        return null;
    }
    
    @Override
    public boolean continueValidation(FormData formData) {
        if ("true".equalsIgnoreCase(getPropertyString(FormUtil.PROPERTY_READONLY))) {
            return false;
        }
        
        return true;
    }
}
