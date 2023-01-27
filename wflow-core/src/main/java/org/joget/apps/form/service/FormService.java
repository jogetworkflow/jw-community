package org.joget.apps.form.service;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;

/**
 * Service methods used to creates form from json and performs form feature actions 
 * 
 */
public interface FormService {
    String PREFIX_FOREIGN_KEY = "fk_";
    String PREFIX_FOREIGN_KEY_EDITABLE = "fke_";
    String PREVIEW_MODE = "_PREVIEW_MODE";
    String INCLUDE_META_DATA = "_INCLUDE_META_DATA";

    /**
     * Creates an element object from a JSON definition
     * @param elementJson
     * @return
     */
    Element createElementFromJson(String elementJson);

    /**
     * Creates an element object from a JSON definition
     * @param elementJson
     * @param processHashVariable
     * @return
     */
    Element createElementFromJson(String elementJson, boolean processHashVariable);

    /**
     * Invokes actions (e.g. buttons) in the form
     * @param form
     * @param formData
     * @return
     */
    FormData executeFormActions(Form form, FormData formData);

    /**
     * Loads data for a specific row into an element by calling all load binders in the element.
     * @param element
     * @param formData
     * @return
     */
    FormData executeFormLoadBinders(Element element, FormData formData);

    /**
     * Preloads data for an element, e.g. field options, etc. by calling all option binders in the element.
     * @param element
     * @param formData
     * @return
     */
    FormData executeFormOptionsBinders(Element element, FormData formData);

    /**
     * Executes store binders for a form
     * @param form
     * @param formData
     * @return
     */
    FormData executeFormStoreBinders(Form form, FormData formData);

    /**
     * Generates HTML for the form element to be used in the Form Builder
     * @param element
     * @param formData
     * @return
     */
    String generateElementDesignerHtml(Element element, FormData formData, boolean includeMetaData);

    /**
     * Generates error HTML for the form element
     * @param element
     * @param formData
     * @return
     */
    String generateElementErrorHtml(Element element, FormData formData);

    /**
     * Generates HTML for the form element
     * @param element
     * @param formData
     * @return
     */
    String generateElementHtml(Element element, FormData formData);

    /**
     * Generates the JSON definition for the specified form element
     * @param element
     * @return
     */
    String generateElementJson(Element element);

    /**
     * Main method to load a form with data loaded.
     * @param form
     * @param formData
     * @return
     */
    Form loadFormData(Form form, FormData formData);

    /**
     * Load a form from its JSON definition, with data loaded.
     * @param formJson
     * @param formData
     * @return
     */
    Form loadFormFromJson(String formJson, FormData formData);

    /**
     * Use case to generate HTML from a JSON element definition.
     * @param json
     * @return
     */
    String previewElement(String json);

    /**
     * Use case to generate HTML from a JSON element definition.
     * @param json
     * @param includeMetaData true to include metadata required for use in the form builder.
     * @return
     */
    String previewElement(String json, boolean includeMetaData);

    /**
     * Recursively executes all the store binders in a form
     * @param form
     * @param element
     * @param formData
     * @return 
     */
    FormData recursiveExecuteFormStoreBinders(Form form, Element element, FormData formData);

    /**
     * Retrieves form data submitted via a HTTP servlet request
     * @param formData
     * @param request
     * @return
     */
    FormData retrieveFormDataFromRequest(FormData formData, HttpServletRequest request);

    /**
     * Retrieves form data submitted via a HTTP servlet request parameters map
     * @param formData
     * @param requestMap
     * @return 
     */
    FormData retrieveFormDataFromRequestMap(FormData formData, Map requestMap);

    /**
     * Used to retrieves the Form HTML when there is errors in form
     * @param form
     * @param formData
     * @return 
     */
    String retrieveFormErrorHtml(Form form, FormData formData);

    /**
     * Used to retrieves the Form HTML 
     * @param form
     * @param formData
     * @return 
     */
    String retrieveFormHtml(Form form, FormData formData);
    
    /**
     * Store the data of a form field element
     * @param form
     * @param element
     * @param formData
     * @return 
     */
    FormData storeElementData(Form form, Element element, FormData formData);

    /**
     * Process form submission
     * @param form
     * @param formData
     * @param ignoreValidation
     * @return
     */
    FormData submitForm(Form form, FormData formData, boolean ignoreValidation);

    /**
     * Validates form data submitted for a specific form
     * @param form
     * @param formData
     * @return
     */
    FormData validateFormData(Form form, FormData formData);

    /**
     * Use case to load and view a form, with data loaded
     * @param form
     * @param primaryKeyValue
     * @return
     */
    String viewForm(Form form, String primaryKeyValue);

    /**
     * Use case to view a form from its JSON definition, with data loaded
     * @param formJson
     * @param primaryKeyValue
     * @return
     */
    String viewFormFromJson(String formJson, String primaryKeyValue);
    
}