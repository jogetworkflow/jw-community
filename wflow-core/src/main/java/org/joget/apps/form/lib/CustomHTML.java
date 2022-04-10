package org.joget.apps.form.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.util.ReflectionUtils;

public class CustomHTML extends Element implements FormBuilderPaletteElement, FormContainer {
    protected static Whitelist whitelist = null;
    
    @Override
    public String getName() {
        return "Custom HTML";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Custom HTML Element";
    }
    
    @Override
    public Collection<String> getDynamicFieldNames() {
        Collection<String> fieldNames = new ArrayList<String>();
        
        String customHTML = (String) getProperty("value");

        if (customHTML != null && !customHTML.isEmpty()) {
            customHTML = stripNoneInputTag(customHTML);
            
            //input field
            Pattern pattern = Pattern.compile("<input[^>]*>");
            Matcher matcher = pattern.matcher(customHTML);
            while (matcher.find()) {
                String inputString = matcher.group(0);

                //get the name
                Pattern patternName = Pattern.compile("name=\"([^\\\"]*)\"");
                Matcher matcherName = patternName.matcher(inputString);
                if (matcherName.find()) {
                    String name = matcherName.group(1);
                    fieldNames.add(name);
                }
            }

            //textarea
            Pattern patternTextarea = Pattern.compile("<textarea[^>]*>.*?</textarea>", Pattern.DOTALL);
            Matcher matcherTextarea = patternTextarea.matcher(customHTML);
            while (matcherTextarea.find()) {
                String textareaString = matcherTextarea.group(0);

                //get the name
                Pattern patternName = Pattern.compile("name=\"([^\\\"]*)\"");
                Matcher matcherName = patternName.matcher(textareaString);
                if (matcherName.find()) {
                    String name = matcherName.group(1);
                    fieldNames.add(name);
                }
            }

            //Select Box
            Pattern patternSelect = Pattern.compile("<select[^>]*>.*?</select>", Pattern.DOTALL);
            Matcher matcherSelect = patternSelect.matcher(customHTML);
            while (matcherSelect.find()) {
                String selectString = matcherSelect.group(0);

                //get the name
                Pattern patternName = Pattern.compile("name=\"([^\\\"]*)\"");
                Matcher matcherName = patternName.matcher(selectString);
                if (matcherName.find()) {
                    String name = matcherName.group(1);
                    fieldNames.add(name);
                }
            }
        }
        return fieldNames;
    }
    
    /**
     * Removed all HTML tags not in the allowed map from the content
     * @param content
     * @param allowedTag
     * @return 
     */
    public String stripNoneInputTag(String content) {
        if (content != null && !content.isEmpty()) {
            if (whitelist == null) {
                whitelist = Whitelist.none().addAttributes(":all","name");
                whitelist.addTags("input");
                whitelist.addTags("textarea");
                whitelist.addTags("select");
                java.lang.reflect.Field field = ReflectionUtils.findField(whitelist.getClass(), "protocols");
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, whitelist, new HashMap());
            }
            content = Jsoup.clean(content, whitelist);
        }
        return content;
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;
        FormRow result = null;
        
        for (String name : getDynamicFieldNames()) {
            //create dummy element object
            Element element = new TextField();
            element.setProperty("id", name);

            //get value from the formData
            String[] values = FormUtil.getElementPropertyValues(element, formData);
            if (values != null && values.length > 0) {
                // check for empty submission via parameter
                String[] paramValues = FormUtil.getRequestParameterValues(element, formData);
                if (paramValues == null || paramValues.length == 0) {
                    values = new String[]{""};
                }

                // formulate values
                String delimitedValue = FormUtil.generateElementPropertyValues(values);

                if (rowSet == null) {
                    rowSet = new FormRowSet();
                    result = new FormRow();
                    rowSet.add(result);
                }

                // set value into Properties and FormRowSet object
                if (result != null) {
                    result.setProperty(name, delimitedValue);
                }
            }
        }
        return rowSet;
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "customHTML.ftl";

        //get the list of names inside tag input and textarea
        String customHTML = (String) getProperty("value");
        if (customHTML != null && !customHTML.isEmpty()) {
            if (getPropertyString("autoPopulate") != null && getPropertyString("autoPopulate").equalsIgnoreCase("true")) {
                //input field
                Pattern pattern = Pattern.compile("<input[^>]*>");
                Matcher matcher = pattern.matcher(customHTML);
                while (matcher.find()) {
                    String inputString = matcher.group(0);
                    String newInputString = inputString;

                    //get the name
                    Pattern patternName = Pattern.compile("name=\"([^\\\"]*)\"");
                    Matcher matcherName = patternName.matcher(inputString);
                    String name = "";
                    if (matcherName.find()) {
                        name = matcherName.group(1);
                    }

                    //get the type
                    Pattern patternType = Pattern.compile("type=\"([^\\\"]*)\"");
                    Matcher matcherType = patternType.matcher(inputString);
                    String type = "";
                    if (matcherType.find()) {
                        type = matcherType.group(1);
                    }

                    if (type.equalsIgnoreCase("checkbox") || type.equalsIgnoreCase("radio")) {
                        //get the value
                        Pattern patternValue = Pattern.compile("value=\"([^\\\"]*)\"");
                        Matcher matcherValue = patternValue.matcher(inputString);
                        String value = "";
                        if (matcherValue.find()) {
                            value = matcherValue.group(1);
                        }

                        Element element = new CheckBox();
                        element.setProperty("id", name);
                        element.setParent(this);
                        String[] values = FormUtil.getElementPropertyValues(element, formData);

                        Boolean checked = false;
                        if (values != null && values.length > 0) {
                            for (String v : values) {
                                if (value.equals(v)) {
                                    checked = true;
                                }
                            }
                        }

                        newInputString = newInputString.replaceFirst("checked", "");
                        newInputString = newInputString.replaceFirst("checked=\"true\"", "");
                        newInputString = newInputString.replaceFirst("checked=\"checked\"", "");

                        if (checked) {
                            newInputString = newInputString.replaceFirst("value=\"", "checked=\"checked\" value=\"");
                        }

                    } else if (type.equalsIgnoreCase("file") || type.equalsIgnoreCase("button") || type.equalsIgnoreCase("submit") || type.equalsIgnoreCase("reset") || type.equalsIgnoreCase("image")) {
                        //ignore
                    } else {
                        Element element = new TextField();
                        element.setProperty("id", name);
                        element.setParent(this);
                        String value = FormUtil.getElementPropertyValue(element, formData);
                        newInputString = newInputString.replaceFirst("value=\"[^\\\"]*\"", "value=\"" + StringUtil.escapeRegex(StringEscapeUtils.escapeHtml(value)) + "\"");
                    }

                    customHTML = customHTML.replaceFirst(StringUtil.escapeRegex(inputString), StringUtil.escapeRegex(newInputString));
                }

                //textarea
                Pattern patternTextarea = Pattern.compile("<textarea[^>]*>.*?</textarea>", Pattern.DOTALL);
                Matcher matcherTextarea = patternTextarea.matcher(customHTML);
                while (matcherTextarea.find()) {
                    String textareaString = matcherTextarea.group(0);
                    String newTextareaString = textareaString;

                    //get the name
                    Pattern patternName = Pattern.compile("name=\"([^\\\"]*)\"");
                    Matcher matcherName = patternName.matcher(textareaString);
                    String name = "";
                    if (matcherName.find()) {
                        name = matcherName.group(1);
                    }

                    Element element = new TextField();
                    element.setProperty("id", name);
                    element.setParent(this);
                    String value = FormUtil.getElementPropertyValue(element, formData);

                    newTextareaString = newTextareaString.replaceFirst(">.*?</", ">" + StringUtil.escapeRegex(StringEscapeUtils.escapeHtml(value)) + "</");

                    customHTML = customHTML.replaceFirst(StringUtil.escapeRegex(textareaString), StringUtil.escapeRegex(newTextareaString));
                }

                //Select Box
                Pattern patternSelect = Pattern.compile("<select[^>]*>.*?</select>", Pattern.DOTALL);
                Matcher matcherSelect = patternSelect.matcher(customHTML);
                while (matcherSelect.find()) {
                    String selectString = matcherSelect.group(0);
                    String newSelectString = selectString;

                    //get the name
                    Pattern patternName = Pattern.compile("name=\"([^\\\"]*)\"");
                    Matcher matcherName = patternName.matcher(selectString);
                    String name = "";
                    if (matcherName.find()) {
                        name = matcherName.group(1);
                    }

                    Element element = new SelectBox();
                    element.setProperty("id", name);
                    element.setParent(this);
                    String[] values = FormUtil.getElementPropertyValues(element, formData);

                    //get the option
                    Pattern patternOption = Pattern.compile("<option[^>]*>.*?</option>");
                    Matcher matcherOption = patternOption.matcher(selectString);
                    while (matcherOption.find()) {
                        String optionString = matcherOption.group(0);
                        String newOptionString = optionString;

                        //get the value
                        Pattern patternValue = Pattern.compile("value=\"([^\\\"]*)\"");
                        Matcher matcherValue = patternValue.matcher(optionString);
                        String value = "";
                        if (matcherValue.find()) {
                            value = matcherValue.group(1);
                        }

                        Boolean selected = false;
                        if (values != null && values.length > 0) {
                            for (String v : values) {
                                if (value.equals(v)) {
                                    selected = true;
                                }
                            }
                        }

                        newOptionString = newOptionString.replaceFirst("selected", "");
                        newOptionString = newOptionString.replaceFirst("selected=\"selected\"", "");
                        newOptionString = newOptionString.replaceFirst("selected=\"true\"", "");

                        if (selected) {
                            newOptionString = newOptionString.replaceFirst("value=\"", "selected=\"selected\" value=\"");
                        }
                        newSelectString = newSelectString.replaceFirst(StringUtil.escapeRegex(optionString), newOptionString);
                    }
                    customHTML = customHTML.replaceFirst(StringUtil.escapeRegex(selectString), StringUtil.escapeRegex(newSelectString));
                }
            }
            
            //remove scripting in builder move
            if (dataModel.containsKey("elementMetaData") && !dataModel.get("elementMetaData").toString().isEmpty()) {
                Pattern pattern = Pattern.compile("<script[^>]*>.*?</script>", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(customHTML);
                while (matcher.find()) {
                    String selectString = matcher.group(0);
                    customHTML = customHTML.replaceFirst(StringUtil.escapeRegex(selectString), "");
                }
            }
        }
        
        dataModel.put("value", customHTML);

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>" + ResourceBundleUtil.getMessage("org.joget.apps.form.lib.CustomHTML.pluginLabel") + "</label><span class='form-floating-label'>" + ResourceBundleUtil.getMessage("form.customhtml.customHtml") + "</span>";
    }

    @Override
    public String getLabel() {
        return "Custom HTML";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/customHtml.json", null, true, "message/form/CustomHTML");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_CUSTOM;
    }

    @Override
    public int getFormBuilderPosition() {
        return 1200;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"fas fa-code\"></i>";
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
}
