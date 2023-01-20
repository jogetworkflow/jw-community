package org.joget.apps.form.lib;

import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.PwaOfflineValidation;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kecak.apps.exception.ApiException;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SelectBox extends Element implements FormBuilderPaletteElement, FormAjaxOptionsElement, PwaOfflineValidation, PluginWebSupport {

    private final static long PAGE_SIZE = 10;
    private Element controlElement;

    private final Map<String, Form> formCache = new HashMap<>();

    @Override
    public String getName() {
        return "Select Box";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Select Box Element";
    }

    /**
     * Returns the option key=value pairs for this select box.
     *
     * @param formData
     * @return
     */
    public Collection<Map> getOptionMap(FormData formData) {
        Collection<Map> optionMap = FormUtil.getElementPropertyOptionsMap(this, formData);
        return optionMap;
    }

    @Override
    public FormData formatDataForValidation(FormData formData) {
        String[] paramValues = FormUtil.getRequestParameterValues(this, formData);
        String paramName = FormUtil.getElementParameterName(this);

        if ((paramValues == null || paramValues.length == 0) && FormUtil.isFormSubmitted(this, formData)) {
            formData.addRequestParameterValues(paramName, new String[]{""});
        } else if (paramValues != null && FormUtil.isFormSubmitted(this, formData)) {
            //check & remove invalid data from values
            Collection<String> newValues = new ArrayList<String>();
            Set<String> allValues = new HashSet<String>();
            if (FormUtil.isAjaxOptionsSupported(this, formData)) {
                FormAjaxOptionsBinder ab = (FormAjaxOptionsBinder) getOptionsBinder();
                String[] controlValues = FormUtil.getRequestParameterValues(getControlElement(formData), formData);
                if (controlValues.length == 1 && controlValues[0].contains(";")) {
                    controlValues = controlValues[0].split(";"); //to consistent the behaviour with FormUtil.getAjaxOptionsBinderData line 2013
                }
                FormRowSet rowSet = ab.loadAjaxOptions(controlValues);
                if (rowSet != null) {
                    formData.setOptionsBinderData(getOptionsBinder(), rowSet);
                    for (FormRow r : rowSet) {
                        allValues.add(r.getProperty(FormUtil.PROPERTY_VALUE));
                    }
                }
            } else {
                Collection<Map> optionMap = FormUtil.getElementPropertyOptionsMap(this, formData);

                //for other child implementation which does not using options binder & option grid, do nothing
                if (optionMap == null || optionMap.isEmpty()) {
                    return formData;
                }
                for (Map option : optionMap) {
                    if (option.containsKey(FormUtil.PROPERTY_VALUE)) {
                        allValues.add(option.get(FormUtil.PROPERTY_VALUE).toString());
                    }
                }
            }
            for (String pv : paramValues) {
                if (allValues.contains(pv)) {
                    newValues.add(pv);
                }
            }

            if (newValues.isEmpty()) {
                newValues.add("");
            }

            formData.addRequestParameterValues(paramName, newValues.toArray(new String[0]));
        }
        return formData;
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String[] values = FormUtil.getElementPropertyValues(this, formData);
            if (values != null && values.length > 0) {
                // check for empty submission via parameter
                String[] paramValues = FormUtil.getRequestParameterValues(this, formData);
                if ((paramValues == null || paramValues.length == 0) && FormUtil.isFormSubmitted(this, formData)) {
                    values = new String[]{""};
                }

                // formulate values
                String delimitedValue = FormUtil.generateElementPropertyValues(values);

                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                result.setProperty(id, delimitedValue);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
        }

        return rowSet;
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "selectBox.ftl";

        dynamicOptions(formData);

        // set value
        String[] valueArray = FormUtil.getElementPropertyValues(this, formData);
        List<String> values = Arrays.asList(valueArray);
        dataModel.put("values", values);

        // set options
        Collection<Map> optionMap = getOptionMap(formData);
        dataModel.put("options", optionMap);
        dataModel.put("className", getClassName());

        final String formDefId;
        final Form form = FormUtil.findRootForm(this);
        if(form != null) {
            formDefId = form.getPropertyString(FormUtil.PROPERTY_ID);
        } else {
            formDefId = "";
        }
        dataModel.put("formDefId", formDefId);

        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        if (appDefinition != null) {
            final String appId = appDefinition.getAppId();;
            final String appVersion = appDefinition.getVersion().toString();

            dataModel.put("appId", appDefinition.getAppId());
            dataModel.put("appVersion", appDefinition.getVersion());

            final String fieldId = getPropertyString(FormUtil.PROPERTY_ID);
            final String username = WorkflowUtil.getCurrentUsername();
            final String nonce = SecurityUtil.generateNonce(new String[]{getName(), appId, appVersion, formDefId, fieldId, username}, 1);
            dataModel.put("nonce", nonce);
        }

        dataModel.put("width", "resolve");

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>" + ResourceBundleUtil.getMessage("org.joget.apps.form.lib.SelectBox.pluginLabel") + "</label><select><option>" + ResourceBundleUtil.getMessage("form.checkbox.template.options") + "</option></select>";
    }

    @Override
    public String getLabel() {
        return "Select Box";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/selectBox.json", null, true, "message/form/SelectBox");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public int getFormBuilderPosition() {
        return 300;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"fas fa-caret-square-down\"></i>";
    }

    protected void dynamicOptions(FormData formData) {
        if (getControlElement(formData) != null) {
            setProperty("controlFieldParamName", FormUtil.getElementParameterName(getControlElement(formData)));

            FormUtil.setAjaxOptionsElementProperties(this, formData);
        }
    }

    public Element getControlElement(FormData formData) {
        if (controlElement == null) {
            if (getPropertyString("controlField") != null && !getPropertyString("controlField").isEmpty()) {
                Form form = FormUtil.findRootForm(this);
                controlElement = FormUtil.findElement(getPropertyString("controlField"), form, formData);
            }
        }
        return controlElement;
    }

    @Override
    public Map<WARNING_TYPE, String[]> validation() {
        Object binderData = getProperty(FormBinder.FORM_OPTIONS_BINDER);
        if (binderData != null && binderData instanceof Map) {
            Map bdMap = (Map) binderData;
            if (bdMap != null && bdMap.containsKey("className") && !bdMap.get("className").toString().isEmpty()) {
                PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
                FormLoadBinder binder = (FormLoadBinder) pluginManager.getPlugin(bdMap.get("className").toString());

                if (binder != null) {
                    Map bdProps = (Map) bdMap.get("properties");
                    ((PropertyEditable) binder).setProperties(bdProps);

                    if (binder instanceof FormAjaxOptionsBinder && ((FormAjaxOptionsBinder) binder).useAjax()) {
                        Map<WARNING_TYPE, String[]> warning = new HashMap<WARNING_TYPE, String[]>();
                        warning.put(WARNING_TYPE.NOT_SUPPORTED, new String[]{ResourceBundleUtil.getMessage("pwa.AjaxOptionsNotSupported")});
                        return warning;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if ("GET".equals(request.getMethod())) {
                handleGet(request, response);
            } else {
                throw new ApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method [" + request.getMethod() + "] is not supported");
            }
        } catch (ApiException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            response.sendError(e.getErrorCode(), e.getMessage());
        }
    }

    /**
     * Handle method GET
     *
     * @param request
     * @param response
     * @throws ApiException
     */
    protected void handleGet(HttpServletRequest request, HttpServletResponse response) throws ApiException {
        final String formDefId = getOptionalParameter(request, "formDefId", "");
        final String fieldId = getRequiredParameter(request, "fieldId");
        final String search = getOptionalParameter(request, "search", "");
        final Pattern searchPattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
        final long page = Long.parseLong(getOptionalParameter(request, "page", "1"));
        final String grouping = getOptionalParameter(request, "grouping", "");
        final String[] values = getOptionalParameterValues(request, "value", new String[0]);
        final String nonce = getRequiredParameter(request, "nonce");

        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        final String appId = appDefinition.getAppId();
        final String appVersion = appDefinition.getVersion().toString();
        final String username = WorkflowUtil.getCurrentUsername();

        if(!SecurityUtil.verifyNonce(nonce, new String[]{getName(), appId, appVersion, formDefId, fieldId, username})) {
            throw new ApiException(HttpServletResponse.SC_UNAUTHORIZED, "Invalid nonce token");
        }

        final FormData formData = new FormData();
        final Form form = generateForm(appDefinition, formDefId);

        final JSONArray jsonResults = new JSONArray();
        final Element element = FormUtil.findElement(fieldId, form, formData);

        if (element == null)
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Element [" + fieldId + "] is not found in form [" + formDefId + "]");

        FormUtil.executeOptionBinders(element, formData);
        List<FormRow> optionsRowSet = FormUtil.getElementPropertyOptionsMap(element, formData);
        if (values.length > 0) {
            for (FormRow row : optionsRowSet) {
                boolean found = false;
                for (String value : values) {
                    if (found = value.equals(row.getProperty(FormUtil.PROPERTY_VALUE))) {
                        break;
                    }
                }

                if (found) {
                    try {
                        JSONObject jsonRow = new JSONObject();
                        jsonRow.put("id", row.getProperty(FormUtil.PROPERTY_VALUE));
                        jsonRow.put("text", row.getProperty(FormUtil.PROPERTY_LABEL));
                        jsonResults.put(jsonRow);
                    } catch (JSONException ignored) {
                    }
                }
            }
        } else {
            int skip = (int) ((page - 1) * PAGE_SIZE);
            int pageSize = (int) PAGE_SIZE;
            for (int i = 0, size = optionsRowSet.size(); i < size && pageSize > 0; i++) {
                FormRow formRow = optionsRowSet.get(i);
                if (searchPattern.matcher(formRow.getProperty(FormUtil.PROPERTY_LABEL)).find() && (
                        grouping.isEmpty()
                                || grouping.equalsIgnoreCase(formRow.getProperty(FormUtil.PROPERTY_GROUPING)))) {

                    if (skip > 0) {
                        skip--;
                    } else {
                        try {
                            JSONObject jsonRow = new JSONObject();
                            jsonRow.put("id", formRow.getProperty(FormUtil.PROPERTY_VALUE));
                            jsonRow.put("text", formRow.getProperty(FormUtil.PROPERTY_LABEL));
                            jsonResults.put(jsonRow);
                            pageSize--;
                        } catch (JSONException ignored) {
                        }
                    }
                }
            }

            try {
                JSONObject jsonPagination = new JSONObject();
                jsonPagination.put("more", jsonResults.length() >= PAGE_SIZE);

                JSONObject jsonData = new JSONObject();
                jsonData.put("results", jsonResults);
                jsonData.put("pagination", jsonPagination);

                response.setContentType("application/json");
                response.getWriter().write(jsonData.toString());
            } catch (JSONException | IOException e) {
                throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, e);
            }
        }
    }

    protected String getRequiredParameter(HttpServletRequest request, String parameterName) throws ApiException {
        String value = request.getParameter(parameterName);
        if (value == null || value.isEmpty()) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Parameter [" + parameterName + "] is not supplied");
        }
        return value;
    }

    protected String[] getRequiredParameters(HttpServletRequest request, String parameterName) throws ApiException {
        String[] values = request.getParameterValues(parameterName);
        if (values == null || values.length == 0) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Parameter [" + parameterName + "] is not supplied");
        }
        return values;
    }

    protected String getOptionalParameter(HttpServletRequest request, String parameterName, String defaultValue) {
        String value = request.getParameter(parameterName);
        return value == null || value.isEmpty() ? defaultValue : value;
    }

    protected String[] getOptionalParameterValues(HttpServletRequest request, String parameterName, String[] defaultValues) {
        return Optional.of(parameterName)
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return request.getParameter(s);
                    }
                })
                .map(new Function<String, String[]>() {
                    @Override
                    public String[] apply(String s) {
                        return s.split(";");
                    }
                })
                .map(new Function<String[], Stream<String>>() {
                    @Override
                    public Stream<String> apply(String[] array) {
                        return Arrays.stream(array);
                    }
                })
                .orElseGet(new Supplier<Stream<String>>() {
                    @Override
                    public Stream<String> get() {
                        return Stream.empty();
                    }
                })
                .toArray(new IntFunction<String[]>() {
                    @Override
                    public String[] apply(int value) {
                        return new String[value];
                    }
                });
    }

    protected Form generateForm(AppDefinition appDef, String formDefId) {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        FormService formService = (FormService) appContext.getBean("formService");
        FormDefinitionDao formDefinitionDao = (FormDefinitionDao) appContext.getBean("formDefinitionDao");

        // check in cache
        if (formCache.containsKey(formDefId))
            return formCache.get(formDefId);

        // proceed without cache
        if (appDef != null && formDefId != null && !formDefId.isEmpty()) {
            FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
            if (formDef != null) {
                String json = formDef.getJson();
                Form form = (Form) formService.createElementFromJson(json);

                formCache.put(formDefId, form);

                return form;
            }
        }
        return null;
    }
}

