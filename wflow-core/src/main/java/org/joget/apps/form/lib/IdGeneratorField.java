package org.joget.apps.form.lib;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;

public class IdGeneratorField extends Element implements FormBuilderPaletteElement {

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "idGeneratorField.ftl";

        String value = FormUtil.getElementPropertyValue(this, formData);
        dataModel.put("value", value);

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String value = FormUtil.getElementPropertyValue(this, formData);
            if ((value == null || value.trim().isEmpty()) && !FormUtil.isReadonly(this, formData)) {
                // generate new value
                value = getGeneratedValue(formData);
                
                String paramName = FormUtil.getElementParameterName(this);
                formData.addRequestParameterValues(paramName, new String[] {value});
            }
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

    protected String getGeneratedValue(FormData formData) {
        String value = "";
        if (formData != null) {
            try {
                value = FormUtil.getElementPropertyValue(this, formData);
                if (!(value != null && value.trim().length() > 0)) {
                    String envVariable = getPropertyString("envVariable");

                    AppDefinition appDef;
                    if(isUsingPublishedAppVersion()) {
                        appDef = getPublishedAppDefinition();
                    }else if (isUsingLatestAppVersion()) {
                        appDef = getLatestAppDefinition();
                    } else {
                        appDef = AppUtil.getCurrentAppDefinition();
                    }

                    EnvironmentVariableDao environmentVariableDao = (EnvironmentVariableDao) AppUtil.getApplicationContext().getBean("environmentVariableDao");
                    
                    Integer count = environmentVariableDao.getIncreasedCounter(envVariable, "Used for plugin: " + getName(), appDef);

                    String format = getPropertyString("format");
                    value = format;
                    Matcher m = Pattern.compile("(\\?+)").matcher(format);
                    if (m.find()) {
                        String pattern = m.group(1);
                        String formater = pattern.replaceAll("\\?", "0");
                        pattern = pattern.replaceAll("\\?", "\\\\?");

                        DecimalFormat myFormatter = new DecimalFormat(formater);
                        String runningNumber = myFormatter.format(count);
                        value = value.replaceAll(pattern, StringUtil.escapeRegex(runningNumber));
                    }
                }
            } catch (Exception e) {
                LogUtil.error(IdGeneratorField.class.getName(), e, "");
            }
        }
        return value;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getName() {
        return "Id Generator Field";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "ID Generator Element";
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_CUSTOM;
    }

    @Override
    public int getFormBuilderPosition() {
        return 3000;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i><span>ID</span></i>";
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>" + ResourceBundleUtil.getMessage("org.joget.apps.form.lib.IdGeneratorField.pluginLabel") + "</label><span></span>";
    }

    @Override
    public String getLabel() {
        return "ID Generator Field";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/idGeneratorField.json", null, true, "message/form/IdGeneratorField");
    }


    /**
     *
     * @return
     */
    protected boolean isUsingPublishedAppVersion() {
        return "published".equalsIgnoreCase(getPropertyString("environmentVariableScope"));
    }

    protected boolean isUsingLatestAppVersion() {
        return "latest".equalsIgnoreCase(getPropertyString("environmentVariableScope"));
    }

    /**
     * Get published application definition
     *
     * @return
     */
    protected AppDefinition getPublishedAppDefinition() {
        AppDefinition currentAppDefinition = AppUtil.getCurrentAppDefinition();
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        String appId = currentAppDefinition.getAppId();
        AppDefinition appDefinitions = appDefinitionDao.getPublishedAppDefinition(appId);
        if(appDefinitions == null)
            return currentAppDefinition;

        return appDefinitions;
    }

    protected AppDefinition getLatestAppDefinition() {
        AppDefinition currentAppDefinition = AppUtil.getCurrentAppDefinition();
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        String appId = currentAppDefinition.getAppId();
        long version = appDefinitionDao.getLatestVersion(appId);

        Collection<AppDefinition> appDefinitions = appDefinitionDao.findByVersion(appId, appId, version, null, null, null, null, 1);
        if(appDefinitions == null || appDefinitions.isEmpty())
            return currentAppDefinition;

        return appDefinitions.stream()
                .findFirst()
                .orElse(currentAppDefinition);
    }
}
