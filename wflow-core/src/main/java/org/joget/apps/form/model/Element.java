package org.joget.apps.form.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.UserviewPermission;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.service.WorkflowUserManager;

/**
 * A base abstract class to develop a Form Field Element plugin. 
 * All forms, containers and form fields must extend this class.
 * 
 */
public abstract class Element extends ExtDefaultPlugin implements PropertyEditable{

    private Collection<Element> children = new ArrayList<Element>();
    private Element parent;
    private String customParameterName;
    private FormLoadBinder loadBinder;
    private FormLoadBinder optionsBinder;
    private FormStoreBinder storeBinder;
    private Validator validator;

    /**
     * Get load binder
     * 
     * @return 
     */
    public FormLoadBinder getLoadBinder() {
        return loadBinder;
    }

    /**
     * Set load binder
     * @param loadBinder 
     */
    public void setLoadBinder(FormLoadBinder loadBinder) {
        this.loadBinder = loadBinder;
    }

    /**
     * Gets an Options Binder
     * @return 
     */
    public FormLoadBinder getOptionsBinder() {
        return optionsBinder;
    }

    /**
     * Sets an Options Binder
     * @param optionsBinder 
     */
    public void setOptionsBinder(FormLoadBinder optionsBinder) {
        this.optionsBinder = optionsBinder;
    }

    /**
     * Gets a Store Binder
     * @return 
     */
    public FormStoreBinder getStoreBinder() {
        return storeBinder;
    }

    /**
     * Sets a Store Binder
     * @param storeBinder 
     */
    public void setStoreBinder(FormStoreBinder storeBinder) {
        this.storeBinder = storeBinder;
    }

    /**
     * Gets a validator
     * @return 
     */
    public Validator getValidator() {
        return validator;
    }

    /**
     * Sets a validator
     * @param validator 
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * Retrieves all children form field element under this field
     * @param formData
     * @return 
     */
    public Collection<Element> getChildren(FormData formData) {
        return getChildren();
    }
    
    /**
     * Retrieves all children form field element under this field
     * @return 
     */
    public Collection<Element> getChildren() {
        return children;
    }

    /**
     * Sets form fields as children of this field
     * 
     * @param children 
     */
    public void setChildren(Collection<Element> children) {
        this.children = children;

        // reset parent for each child
        if (children != null) {
            for (Element child : children) {
                child.parent = this;
            }
        }
    }

    /**
     * Returns the immediate parent for this element
     * @return null if there is no parent.
     */
    public Element getParent() {
        return parent;
    }

    /**
     * Sets the immediate parent for this element.
     * @param parent
     */
    public void setParent(Element parent) {
        this.parent = parent;
    }

    /**
     * @return If non-null, this is to be used as the HTML input name for the element
     */
    public String getCustomParameterName() {
        if (customParameterName == null && this.getPropertyString("customParameterName") != null && !this.getPropertyString("customParameterName").isEmpty()) {
            customParameterName = this.getPropertyString("customParameterName");
        }
        return customParameterName;
    }

    /**
     * Sets a custom parameter name for the HTML input name of the element
     * 
     * @param customParameterName 
     */
    public void setCustomParameterName(String customParameterName) {
        setProperty("customParameterName", customParameterName);
        this.customParameterName = customParameterName;
    }

    /**
     * Method for override to perform format data in request parameter before execute validation
     * @param formData
     * @return the formatted data.
     */
    public FormData formatDataForValidation(FormData formData) {
        //do nothing
        return formData;
    }
    
    /**
     * Method for override to perform specify validation for this field.
     * 
     * Error message can display with following code:
     * <pre>
     * String id = FormUtil.getElementParameterName(this);
     * formData.addFormError(id, "Error!!");
     * </pre>
     * 
     * @param formData
     * @return 
     */
    public Boolean selfValidate(FormData formData) {
        //do nothing
        return true;
    }

    /**
     * Method that retrieves loaded or submitted form data, and formats it for a store binder.
     * The formatted data is to be stored and returned in a FormRowSet.
     * @param formData
     * @return the formatted data.
     */
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String value = FormUtil.getElementPropertyValue(this, formData);
            if (value != null) {
                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                result.setProperty(id, value);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
        }

        return rowSet;
    }
    
    /**
     * Returns the primary key value for the current element.
     * Defaults to the primary key value of the form.
     */
    public String getPrimaryKeyValue(FormData formData) {
        String primaryKeyValue = null;
        // get value from form's ID field
        Element primaryElement = FormUtil.findElement(FormUtil.PROPERTY_ID, this, formData);
        if (primaryElement != null) {
            primaryKeyValue = FormUtil.getElementPropertyValue(primaryElement, formData);
        }
        if ((primaryKeyValue == null || primaryKeyValue.trim().isEmpty()) && formData != null) {
            // ID field not available, use parent primary key
            Element parent = this.getParent();
            if (parent != null) {
                primaryKeyValue = parent.getPrimaryKeyValue(formData);
            }
        }
        if ((primaryKeyValue == null || primaryKeyValue.trim().isEmpty()) && formData != null) {
            // ID field not available, use default form primary key
            primaryKeyValue = formData.getPrimaryKeyValue();
        }
        return primaryKeyValue;
    }

    /**
     * Render HTML template for UI, with option for form builder design mode
     * @param formData
     * @param includeMetaData set true to render additional meta required for the Form Builder.
     * @return
     */
    public String render(FormData formData, Boolean includeMetaData) {
        Map dataModel = FormUtil.generateDefaultTemplateDataModel(this, formData);

        // set metadata for form builder
        dataModel.put("includeMetaData", includeMetaData);
        if (includeMetaData) {
            String elementMetaData = FormUtil.generateElementMetaData(this);
            dataModel.put("elementMetaData", elementMetaData);
        }

        return renderTemplate(formData, dataModel);
    }

    /**
     * HTML template for front-end UI
     * @param formData
     * @param dataModel Model containing values to be displayed in the template.
     * @return
     */
    public abstract String renderTemplate(FormData formData, Map dataModel);

    /**
     * HTML template with errors for front-end UI
     * @param formData
     * @param dataModel Model containing values to be displayed in the template.
     * @return
     */
    public String renderErrorTemplate(FormData formData, Map dataModel) {
        return renderTemplate(formData, dataModel);
    }

    /**
     * Read-only HTML template for front-end UI (Not used at the moment)
     * @param formData
     * @param dataModel Model containing values to be displayed in the template.
     * @return
     */
    public String renderReadOnlyTemplate(FormData formData, Map dataModel) {
        // set readonly flag
        dataModel.put(FormUtil.PROPERTY_READONLY, Boolean.TRUE);

        return renderTemplate(formData, dataModel);
    }

    /**
     * Flag to indicate whether or not continue validating descendent elements.
     * @param formData
     * @return
     */
    public boolean continueValidation(FormData formData) {
        return true;
    }
    
    /**
     * Set default Plugin Properties Options value to a new added Field in Form Builder.
     * 
     * @return 
     */
    public String getDefaultPropertyValues(){
        return PropertyUtil.getDefaultPropertyValues(getPropertyOptions());
    }
    
    /**
     * Used to create multiple form data column in database by returning extra column names.
     * 
     * @return 
     */
    public Collection<String> getDynamicFieldNames() {
        return null;
    }

    @Override
    public String toString() {
        return "Element {" + "className=" + getClassName() + ", properties=" + getProperties() + '}';
    }
    
    /**
     * Flag to indicate whether or not this field has fail the validation process
     * 
     * @param formData
     * @return 
     */
    public Boolean hasError(FormData formData) {
        String error = FormUtil.getElementError(this, formData);
        if (error != null && !error.isEmpty()) {
            return true;
        }
        
        Collection<Element> childs = getChildren(formData);
        if (childs != null && !childs.isEmpty()) {
            for (Element child : childs) {
                if (child.hasError(formData)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Flag to indicate whether or not the current logged in user is authorized to view this field in the form.
     * 
     * It used property key "permission" to retrieve Form Permission plugin.
     * 
     * @param formData
     * @return 
     */
    public Boolean isAuthorize(FormData formData) {
        if (formData.getFormResult(FormService.PREVIEW_MODE) != null) {
            return true;
        }
        
        Boolean isAuthorize = true;
        
        Map permissionMap = (Map) getProperty("permission");
        if (permissionMap != null && permissionMap.get("className") != null) {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            UserviewPermission permission = (UserviewPermission) pluginManager.getPlugin(permissionMap.get("className").toString());
            if (permission != null) {
                permission.setProperties((Map) permissionMap.get("properties"));
                permission.setRequestParameters(formData.getRequestParams());
                
                WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
                User user = workflowUserManager.getCurrentUser();
                permission.setCurrentUser(user);
                
                isAuthorize = permission.isAuthorize();
            }
        }
        
        return isAuthorize;
    }
}
