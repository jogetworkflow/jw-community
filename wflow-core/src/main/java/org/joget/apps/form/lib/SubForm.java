package org.joget.apps.form.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.beans.BeansException;

public class SubForm extends Element implements FormBuilderPaletteElement, PluginWebSupport {

    public static final String PROPERTY_PARENT_SUBFORM_ID = "parentSubFormId";
    public static final String PROPERTY_SUBFORM_PARENT_ID = "subFormParentId";

    @Override
    public String getName() {
        return "Sub Form";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "Sub Form Element";
    }

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

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        
        // set subform html
        String elementMetaData = ((Boolean) dataModel.get("includeMetaData")) ? FormUtil.generateElementMetaData(this) : "";
        Collection<Element> childElements = getChildren();
        Form subForm = (childElements.size() > 0) ? (Form) getChildren().iterator().next() : null;
        String label = getPropertyString("label");
        String cellClass = ((Boolean) dataModel.get("includeMetaData")) ? "form-cell" : "subform-cell";
        String html = "<div class='" + cellClass + "' " + elementMetaData + "><div class='subform-container' style='border: 5px solid #dfdfdf; padding: 3px;margin-top:5px;'>";
        html += "<span class='subform-title' style='background: #efefef;position:relative;top:-12px;'>" + label + "</span>";
        if (subForm != null) {
            String subFormHtml = subForm.render(formData, false);
            subFormHtml = subFormHtml.replaceAll("\"form-section", "\"subform-section");
            subFormHtml = subFormHtml.replaceAll("\"form-column", "\"subform-column");
            subFormHtml = subFormHtml.replaceAll("\"form-cell", "\"subform-cell");
            html += subFormHtml;
        } else {
            html += "SubForm could not be loaded";
        }
        html += "<div style='clear:both;'></div></div></div>";
        return html;
    }

    /**
     * Loads the actual subform
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
                json = AppUtil.processHashVariable(json, wfAssignment, null, null);
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
                Logger.getLogger(SubForm.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        if (subForm != null) {
            // set parent
            subForm.setParent(this);

            // replace binder(s) if necessary
            FormLoadBinder loadBinder = getLoadBinder();
            if (loadBinder != null) {
                subForm.setLoadBinder(loadBinder);
            } else {
                setLoadBinder(subForm.getLoadBinder());
            }
            FormStoreBinder storeBinder = getStoreBinder();
            if (storeBinder != null) {
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

    protected void populateParentWithSubFormKey(FormData formData) {
        String parentSubFormId = getPropertyString(PROPERTY_PARENT_SUBFORM_ID);
        if (parentSubFormId != null && !parentSubFormId.trim().isEmpty()) {
            Form subForm = null;
            Collection<Element> children = getChildren();
            if (!children.isEmpty()) {
                // get actual subform
                subForm = (Form) children.iterator().next();

                // get subform's primary key value
                String subFormPrimaryKeyValue = null;
                Element subFormPrimaryElement = FormUtil.findElement(FormUtil.PROPERTY_ID, subForm, formData);
                if (subFormPrimaryElement != null) {
                    subFormPrimaryElement.formatData(formData);
                    subFormPrimaryKeyValue = FormUtil.getElementPropertyValue(subFormPrimaryElement, formData);
                }
                
                // generate new ID if empty
                if (subFormPrimaryKeyValue == null || subFormPrimaryKeyValue.trim().isEmpty()) {
                    // generate new ID
                    subFormPrimaryKeyValue = UuidGenerator.getInstance().getUuid();

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
                if (subFormPrimaryKeyValue != null && !subFormPrimaryKeyValue.isEmpty()) {
                    Form rootForm = FormUtil.findRootForm(this);
                    FormStoreBinder rootStoreBinder = rootForm.getStoreBinder();
                    if (rootStoreBinder != null) {
                        FormRowSet rootFormRowSet = formData.getStoreBinderData(rootStoreBinder);
                        if (!rootFormRowSet.isEmpty()) {
                            FormRow row = rootFormRowSet.get(0);
                            row.setProperty(parentSubFormId, subFormPrimaryKeyValue);
                        }
                    }
                }
            }
        }
    }

    protected void populateSubFormWithParentKey(FormData formData) {
        String subFormParentId = getPropertyString(PROPERTY_SUBFORM_PARENT_ID);
        if (subFormParentId != null && !subFormParentId.trim().isEmpty()) {
            Form subForm = null;
            Collection<Element> children = getChildren();
            if (!children.isEmpty()) {
                // get actual subform
                subForm = (Form) children.iterator().next();

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
                    }
                }

            }
        }
    }

    protected void updateElementParameterNames(Element element, String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        if (element instanceof Form || element instanceof SubForm) {
            String formId = element.getPropertyString(FormUtil.PROPERTY_ID);
            if (formId == null) {
                formId = "";
            }
            prefix += "_" + formId;
        }

        // prefix parent ID set custom parameter name and readonly property for each child recursively
        boolean readonly = Boolean.valueOf(getPropertyString(FormUtil.PROPERTY_READONLY)).booleanValue();
        Collection<Element> children = element.getChildren();
        for (Element child : children) {
            String paramName = prefix + "_" + child.getPropertyString(FormUtil.PROPERTY_ID);
            child.setCustomParameterName(paramName);
            if (readonly) {
                child.setProperty(FormUtil.PROPERTY_READONLY, "true");
            }
            updateElementParameterNames(child, prefix);
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

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<div class='subform-container' style='border: 5px solid #dfdfdf; padding: 3px;margin-top:5px;'><span class='subform-title' style='background: #efefef;position:relative;top:-12px;'>SubForm</span></div>";
    }

    @Override
    public String getLabel() {
        return "Sub Form";
    }

    @Override
    public String getPropertyOptions() {
        String formDefField = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            String formJsonUrl = "[CONTEXT_PATH]/web/json/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/forms/options";
            formDefField = "{name:'formDefId',label:'@@form.subform.formId@@',type:'selectbox',options_ajax:'" + formJsonUrl + "',required:'true'}";
        } else {
            formDefField = "{name:'formDefId',label:'@@form.subform.formId@@',type:'textfield',required:'true'}";
        }
        Object[] arguments = new Object[]{formDefField};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/form/subForm.json", arguments, true, "message/form/SubForm");
        return json;
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public int getFormBuilderPosition() {
        return 900;
    }

    @Override
    public String getFormBuilderIcon() {
        return "/plugin/org.joget.apps.form.lib.SubForm/images/subForm_icon.gif";
    }

    /**
     * Return JSON of available forms that can be embedded as this subform.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("getOptions".equals(action)) {
            Collection<FormDefinition> formDefList = new ArrayList<FormDefinition>();
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            if (appDef != null) {
                // load forms from current thread app version
                formDefList = appDef.getFormDefinitionList();
            }
            String output = "[{\"value\":\"\",\"label\":\"\"}";
            for (FormDefinition formDef : formDefList) {
                output += ",{\"value\":\"" + formDef.getId() + "\",\"label\":\"" + formDef.getName() + "\"}";
            }
            output += "]";
            response.getWriter().write(output);
        }
    }

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
}
