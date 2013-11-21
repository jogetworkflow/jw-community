package org.joget.apps.form.service;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.AbstractSubForm;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.commons.util.FileLimitException;
import org.joget.commons.util.FileManager;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.UuidGenerator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("formService")
public class FormService {

    public static final String PREFIX_FOREIGN_KEY = "fk_";
    public static final String PREFIX_FOREIGN_KEY_EDITABLE = "fke_";

    /**
     * Use case to generate HTML from a JSON element definition.
     * @param json
     * @return
     */
    public String previewElement(String json) {
        return previewElement(json, true);
    }

    /**
     * Use case to generate HTML from a JSON element definition.
     * @param json
     * @param includeMetaData true to include metadata required for use in the form builder.
     * @return
     */
    public String previewElement(String json, boolean includeMetaData) {
        Element element = createElementFromJson(StringUtil.decryptContent(json), !includeMetaData);
        FormData formData = new FormData();
        String html = "";
        try {
            formData = executeFormOptionsBinders(element, formData);
        } catch (Exception ex) {
            Logger.getLogger(FormService.class.getName()).log(Level.SEVERE, "Error executing form option binders", ex);
        }
        try {
            html = generateElementDesignerHtml(element, formData, includeMetaData);
        } catch (Exception ex) {
            Logger.getLogger(FormService.class.getName()).log(Level.SEVERE, "Error generating element html", ex);
        }
        return html;
    }

    /**
     * Creates an element object from a JSON definition
     * @param formJson
     * @return
     */
    public Element createElementFromJson(String elementJson) {
        return createElementFromJson(elementJson, true);
    }

    /**
     * Creates an element object from a JSON definition
     * @param formJson
     * @param processHashVariable
     * @return
     */
    public Element createElementFromJson(String elementJson, boolean processHashVariable) {
        try {
            String processedJson = elementJson;
            // process hash variable
            if (processHashVariable) {
                processedJson = AppUtil.processHashVariable(elementJson, null, StringUtil.TYPE_JSON, null);
            }
            
            processedJson = processedJson.replaceAll("\\\"\\{\\}\\\"", "{}");

            // instantiate element
            Element element = FormUtil.parseElementFromJson(processedJson);
            return element;
        } catch (Exception ex) {
            Logger.getLogger(FormService.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Generates HTML for the form element
     * @param element
     * @param formData
     * @return
     */
    public String generateElementHtml(Element element, FormData formData) {
        String html = element.render(formData, false);
        return html;
    }

    /**
     * Generates error HTML for the form element
     * @param element
     * @param formData
     * @return
     */
    public String generateElementErrorHtml(Element element, FormData formData) {
        Map dataModel = FormUtil.generateDefaultTemplateDataModel(element, formData);
        String html = element.renderErrorTemplate(formData, dataModel);
        return html;
    }

    /**
     * Generates HTML for the form element to be used in the Form Builder
     * @param element
     * @param formData
     * @return
     */
    public String generateElementDesignerHtml(Element element, FormData formData, boolean includeMetaData) {
        String html = element.render(formData, includeMetaData);
        return html;
    }

    /**
     * Generates the JSON definition for the specified form element
     * @param element
     * @return
     */
    public String generateElementJson(Element element) {
        String json = null;
        try {
            json = FormUtil.generateElementJson(element);
        } catch (Exception ex) {
            Logger.getLogger(FormService.class.getName()).log(Level.SEVERE, "Error generating JSON for element", ex);
        }
        return json;
    }

    /**
     * Use case to load and view a form, with data loaded
     * @param formDefId
     * @param primaryKeyValue
     * @return
     */
    public String viewForm(Form form, String primaryKeyValue) {
        FormData formData = new FormData();
        formData.setPrimaryKeyValue(primaryKeyValue);
        String html = generateElementHtml(form, formData);
        return html;
    }

    /**
     * Use case to view a form from its JSON definition, with data loaded
     * @param formJson
     * @param primaryKeyValue
     * @return
     */
    public String viewFormFromJson(String formJson, String primaryKeyValue) {
        FormData formData = new FormData();
        formData.setPrimaryKeyValue(primaryKeyValue);
        Form form = loadFormFromJson(formJson, formData);
        String html = generateElementHtml(form, formData);
        return html;
    }

    /**
     * Load a form from its JSON definition, with data loaded.
     * @param formJson
     * @param formData
     * @return
     */
    public Form loadFormFromJson(String formJson, FormData formData) {
        Form form = (Form) createElementFromJson(formJson);
        form = loadFormData(form, formData);
        return form;
    }

    /**
     * Main method to load a form with data loaded.
     * @param formJson
     * @param formData
     * @return
     */
    public Form loadFormData(Form form, FormData formData) {
        // set foreign key values
        Set<String> readOnlyForeignKeySet = new HashSet<String>();
        if (formData != null) {
            Map requestParams = new HashMap(formData.getRequestParams());
            for (Iterator i = requestParams.keySet().iterator(); i.hasNext();) {
                String paramName = (String) i.next();
                if (paramName.startsWith(PREFIX_FOREIGN_KEY) || paramName.startsWith(PREFIX_FOREIGN_KEY_EDITABLE)) {
                    String foreignKey = paramName.startsWith(PREFIX_FOREIGN_KEY) ? paramName.substring(PREFIX_FOREIGN_KEY.length()) : paramName.substring(PREFIX_FOREIGN_KEY_EDITABLE.length());
                    boolean editable = paramName.startsWith(PREFIX_FOREIGN_KEY_EDITABLE);
                    String[] values = (String[]) requestParams.get(paramName);

                    if (values != null) {
                        formData.addRequestParameterValues(foreignKey, values);
                        if (!editable) {
                            readOnlyForeignKeySet.add(foreignKey);
                            form.setFormMeta(paramName, values);
                        }
                    }
                }
            }
        }

        formData = executeFormOptionsBinders(form, formData);
        formData = executeFormLoadBinders(form, formData);

        // make foreign keys read-only
        for (String foreignKey : readOnlyForeignKeySet) {
            Element el = FormUtil.findElement(foreignKey, form, formData);
            if (el != null) {
                FormUtil.setReadOnlyProperty(el);
            }
        }
        return form;
    }

    /**
     * Process form submission
     * @param form
     * @param formData
     * @param ignoreValidation
     * @return
     */
    public FormData submitForm(Form form, FormData formData, boolean ignoreValidation) {
        FormData updatedFormData = formData;
        updatedFormData = FormUtil.executeElementFormatDataForValidation(form, formData);
        if (!ignoreValidation) {
            updatedFormData = validateFormData(form, formData);
        }
        Map<String, String> errors = updatedFormData.getFormErrors();
        if (!updatedFormData.getStay() && errors == null || errors.isEmpty()) {
            // generate primary key if necessary
            String primaryKeyValue = form.getPrimaryKeyValue(updatedFormData);
            if (primaryKeyValue == null || primaryKeyValue.trim().length() == 0) {
                // no primary key value specified, generate new primary key value
                primaryKeyValue = UuidGenerator.getInstance().getUuid();
                updatedFormData.setPrimaryKeyValue(primaryKeyValue);
            }
            // no errors, save form data
            updatedFormData = executeFormStoreBinders(form, updatedFormData);
        }
        return updatedFormData;
    }

    /**
     * Validates form data submitted for a specific form
     * @param form
     * @param formData
     * @return
     */
    public FormData validateFormData(Form form, FormData formData) {
        FormUtil.executeValidators(form, formData);
        return formData;
    }

    /**
     * Retrieves form data submitted via a HTTP servlet request
     * @param request
     * @return
     */
    public FormData retrieveFormDataFromRequest(FormData formData, HttpServletRequest request) {
        if (formData == null) {
            formData = new FormData();
        }
        // handle standard parameters
        Enumeration<String> e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = e.nextElement();
            String[] values = request.getParameterValues(paramName);
            formData.addRequestParameterValues(paramName, values);
        }
        
        handleFiles(formData);
        
        return formData;
    }

    public FormData retrieveFormDataFromRequestMap(FormData formData, Map requestMap) {
        if (formData == null) {
            formData = new FormData();
        }
        // handle standard parameters
        for (String key : (Set<String>) requestMap.keySet()) {
            Object paramValue = requestMap.get(key);
            if (paramValue != null) {
                Class type = paramValue.getClass();
                if (type.isArray()) {
                    formData.addRequestParameterValues(key, (String[]) paramValue);
                } else {
                    formData.addRequestParameterValues(key, new String[]{paramValue.toString()});
                }
            } else {
                formData.addRequestParameterValues(key, new String[]{""});
            }
        }
        
        handleFiles(formData);
        
        return formData;
    }
    
    private void handleFiles (FormData formData) {
        try {
            // handle multipart files
            Map<String, MultipartFile> fileMap = FileStore.getFileMap();
            if (fileMap != null) {
                for (String paramName : fileMap.keySet()) {
                    try {
                        MultipartFile file = FileStore.getFile(paramName);
                        if (file != null && file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
                            String path = FileManager.storeFile(file);
                            formData.addRequestParameterValues(paramName, new String[]{path});
                        }
                    } catch (FileLimitException ex) {
                        formData.addFormError(paramName, ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
                    }
                }
            }
            
            Collection<String> errorList = FileStore.getFileErrorList();
            if (errorList != null && !errorList.isEmpty()) {
                for (String paramName : errorList) {
                    formData.addFormError(paramName, ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
                }
            }
        } finally {
            FileStore.clear();
        }
    }

    /**
     * Invokes actions (e.g. buttons) in the form
     * @param form
     * @param formData
     * @return
     */
    public FormData executeFormActions(Form form, FormData formData) {
        FormData updatedFormData = formData;
        updatedFormData = FormUtil.executeActions(form, form, formData);
        return updatedFormData;
    }

    /**
     * Preloads data for an element, e.g. field options, etc. by calling all option binders in the element.
     * @param element
     * @param formData
     * @return
     */
    public FormData executeFormOptionsBinders(Element element, FormData formData) {
        // create new form data if necessary
        if (formData == null) {
            formData = new FormData();
        }

        // find and call all option binders in the form
        formData = FormUtil.executeOptionBinders(element, formData);
        return formData;
    }

    /**
     * Loads data for a specific row into an element by calling all load binders in the element.
     * @param element
     * @param formData
     * @return
     */
    public FormData executeFormLoadBinders(Element element, FormData formData) {
        // create new form data if necessary
        if (formData == null) {
            formData = new FormData();
        }

        // find and call all option binders in the form
        formData = FormUtil.executeLoadBinders(element, formData);
        return formData;
    }

    /**
     * Executes store binders for a form
     * @param form
     * @param formData
     * @return
     */
    public FormData executeFormStoreBinders(Form form, FormData formData) {

        // get formatted data from all elements
        formData = FormUtil.executeElementFormatData(form, formData);

        //recursively execute FormStoreBinders
        formData = recursiveExecuteFormStoreBinders(form, form, formData);

        return formData;
    }
    
    public FormData recursiveExecuteFormStoreBinders(Form form, Element element, FormData formData) {
        if (!Boolean.parseBoolean(element.getPropertyString(FormUtil.PROPERTY_READONLY)) && element.isAuthorize(formData)) {
        
            //load child element store binder to store before the main form
            Collection<Element> children = element.getChildren(formData);
            if (children != null) {
                for (Element child : children) {
                    formData = recursiveExecuteFormStoreBinders(form, child, formData);
                }
            }

            //if store binder exist && element is not readonly, run it
            FormStoreBinder binder = element.getStoreBinder();
            if (!(element instanceof AbstractSubForm) && binder != null) {
                FormRowSet rowSet = formData.getStoreBinderData(element.getStoreBinder());

                // execute binder
                try {
                    FormRowSet binderResult = binder.store(element, rowSet, formData);
                    formData.setStoreBinderData(binder, binderResult);
                } catch (Exception e) {
                    String formId = FormUtil.getElementParameterName(form);
                    formData.addFormError(formId, "Error storing data: " + e.getMessage());
                    Logger.getLogger(FormService.class.getName()).log(Level.SEVERE, "Error executing store binder", e);
                }
            }
        }
        
        return formData;
    }

    public String retrieveFormHtml(Form form, FormData formData) {
        String formHtml = null;
        if (form != null) {
            if (formData == null) {
                formData = new FormData();
            }
            formHtml = generateElementHtml(form, formData);
        }
        return formHtml;
    }

    public String retrieveFormErrorHtml(Form form, FormData formData) {
        String formHtml = null;
        if (form != null) {
            if (formData == null) {
                formData = new FormData();
            }
            formHtml = generateElementErrorHtml(form, formData);
        }
        return formHtml;
    }
}
