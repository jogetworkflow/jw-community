package org.joget.apps.form.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.form.lib.ColumnContainer;
import org.joget.apps.form.lib.Columns;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.form.service.VisibilityControlUtil;
import org.joget.apps.userview.model.Permission;
import org.joget.apps.util.DefaultPropertyValuesCache;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * A base abstract class to develop a Form Field Element plugin. 
 * All forms, containers and form fields must extend this class.
 * 
 */
public abstract class Element extends ExtDefaultPlugin implements PropertyEditable{

    protected Collection<Element> children = new ArrayList<Element>();
    private Element parent;
    private String customParameterName;
    private FormLoadBinder loadBinder;
    private FormLoadBinder optionsBinder;
    private FormStoreBinder storeBinder;
    private Validator validator;
    protected Map<FormData, Boolean> isAuthorizeSet = new HashMap<FormData, Boolean>();
    protected Map<FormData, Boolean> isReadonlySet = new HashMap<FormData, Boolean>();
    protected Map<FormData, Boolean> isPersistableReadonlySet = new HashMap<FormData, Boolean>();
    protected Map<FormData, Boolean> isHiddenSet = new HashMap<FormData, Boolean>();
    protected Map<FormData, String> permissionKeys = new HashMap<FormData, String>();
    protected Set<String> childsUniqueKeys = new HashSet<String>();

    // Visibility control fields
    protected Collection<Map<String, String>> visibilityRules = null;
    protected Map<String, Element> visibilityControlElements = new HashMap<String, Element>();
    protected Map<FormData, Boolean> continueValidations = new HashMap<FormData, Boolean>();

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
        
        //update element unique key
        if (customParameterName != null && !customParameterName.isEmpty()) {
            setUniqueKey(getProperty(FormUtil.PROPERTY_ELEMENT_UNIQUE_KEY) + Integer.toUnsignedString(customParameterName.hashCode()));
        }
        
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
        } else if (FormUtil.isHidden(this, formData)) {
            return "";
        }
        
        if (FormUtil.isReadonly(this, formData, 1)) {
            this.setProperty(FormUtil.PROPERTY_READONLY, "true");
        } else if (FormUtil.isReadonly(this, formData, 2)) {
            this.setProperty(FormUtil.PROPERTY_READONLY, "readonly");
        } else {
            this.setProperty(FormUtil.PROPERTY_READONLY, "");
        }

        String html = renderTemplate(formData, dataModel);
        html = decorateWithBuilderProperties(html, formData);

        if (!includeMetaData && !(this instanceof Section) && !(this instanceof Form) && !(this instanceof Column) && !(this instanceof Columns) && !(this instanceof ColumnContainer)) {
            html = decorateWithVisibilityControl(html, formData);
        }

        return html;
    }

    /**
     * Decorate the HTML output with visibility control script if visibility rules are defined.
     *
     * @param html The rendered HTML
     * @param formData The form data
     * @return HTML with visibility script injected
     */
    protected String decorateWithVisibilityControl(String html, FormData formData) {
        if (!getVisibilityRules(formData).isEmpty()) {
            String uniqueKey = getPropertyString("elementUniqueKey");
            String rulesJson = getVisibilityRulesJson(formData);

            if (rulesJson != null && !html.isEmpty()) {
                // Add a unique class to the form-cell for targeting
                String cellClass = "field_" + uniqueKey;
                boolean visible = isVisibilityMatch(formData);

                String hiddenClass = visible ? "" : " field-visibility-hidden";
                String hiddenStyle = visible ? "" : " style=\"display: none\"";

                //replace class attribute while preserving any existing classes
                html = html.replaceFirst(
                    "class=\"form-cell([^\"]*)\"",
                    "class=\"form-cell " + cellClass + hiddenClass + "$1\"" + hiddenStyle
                );

                // Inject visibility control script
                String script = "\n<script type=\"text/javascript\">\n" +
                        "$(document).ready(function() {\n" +
                        "    new VisibilityMonitor($('." + cellClass + "'), " + rulesJson + ", true).init();\n" +
                        "});\n" +
                        "</script>\n";

                // Insert script before the closing div
                int lastDivIndex = html.lastIndexOf("</div>");
                if (lastDivIndex > 0) {
                    html = html.substring(0, lastDivIndex) + script + html.substring(lastDivIndex);
                }
            }
        }
        return html;
    }
    
    public Map<String, String> getElementStyles(String styleClass, Map<String, String> attrs) {
        return getElementStyles(styleClass, attrs, true);
    }
    
    public Map<String, String> getElementStyles(String styleClass, Map<String, String> attrs, boolean applyToLabelAndField) {
        Map<String, String> styles = new HashMap<String, String>();
        styles.put("DESKTOP", "");
        styles.put("TABLET", "");
        styles.put("MOBILE", "");

        if (applyToLabelAndField) {
            if (hasCompiledGroupKeys()) {
                // New format: per-group compiled CSS strings exist — apply with per-group inherit control.
                generatePerGroupStyles(styleClass, styles);
            } else {
                // Legacy format: no compiled keys (element saved before per-group feature).
                // Fall back to original behaviour: all CSS applied to element + label/input selectors.
                String[] cssClass = getLabelAndInputSelector(styleClass, false);
                String[] cssHoverClass = getLabelAndInputSelector(styleClass, true);
                String labelInputSelector = ", " + cssClass[0] + ", " + cssClass[1];
                String labelInputHoverSelector = ", " + cssHoverClass[0] + ", " + cssHoverClass[1];
                if (!attrs.get("desktopStyle").isEmpty()) {
                    styles.put("DESKTOP", styles.get("DESKTOP") + " ." + styleClass + labelInputSelector + "{" + attrs.get("desktopStyle") + "} ");
                }
                if (!attrs.get("tabletStyle").isEmpty()) {
                    styles.put("TABLET", styles.get("TABLET") + " ." + styleClass + labelInputSelector + "{" + attrs.get("tabletStyle") + "} ");
                }
                if (!attrs.get("mobileStyle").isEmpty()) {
                    styles.put("MOBILE", styles.get("MOBILE") + " ." + styleClass + labelInputSelector + "{" + attrs.get("mobileStyle") + "} ");
                }
                if (!attrs.get("hoverDesktopStyle").isEmpty()) {
                    styles.put("DESKTOP", styles.get("DESKTOP") + " ." + styleClass + ":hover" + labelInputHoverSelector + "{" + attrs.get("hoverDesktopStyle") + "} ");
                }
                if (!attrs.get("hoverTabletStyle").isEmpty()) {
                    styles.put("TABLET", styles.get("TABLET") + " ." + styleClass + ":hover" + labelInputHoverSelector + "{" + attrs.get("hoverTabletStyle") + "} ");
                }
                if (!attrs.get("hoverMobileStyle").isEmpty()) {
                    styles.put("MOBILE", styles.get("MOBILE") + " ." + styleClass + ":hover" + labelInputHoverSelector + "{" + attrs.get("hoverMobileStyle") + "} ");
                }
            }
        } else {
            // Element-only mode: apply all compiled CSS to the element selector only.
            if (!attrs.get("desktopStyle").isEmpty()) {
                styles.put("DESKTOP", styles.get("DESKTOP") + " ." + styleClass + "{" + attrs.get("desktopStyle") + "} ");
            }
            if (!attrs.get("tabletStyle").isEmpty()) {
                styles.put("TABLET", styles.get("TABLET") + " ." + styleClass + "{" + attrs.get("tabletStyle") + "} ");
            }
            if (!attrs.get("mobileStyle").isEmpty()) {
                styles.put("MOBILE", styles.get("MOBILE") + " ." + styleClass + "{" + attrs.get("mobileStyle") + "} ");
            }
            if (!attrs.get("hoverDesktopStyle").isEmpty()) {
                styles.put("DESKTOP", styles.get("DESKTOP") + " ." + styleClass + ":hover{" + attrs.get("hoverDesktopStyle") + "} ");
            }
            if (!attrs.get("hoverTabletStyle").isEmpty()) {
                styles.put("TABLET", styles.get("TABLET") + " ." + styleClass + ":hover{" + attrs.get("hoverTabletStyle") + "} ");
            }
            if (!attrs.get("hoverMobileStyle").isEmpty()) {
                styles.put("MOBILE", styles.get("MOBILE") + " ." + styleClass + ":hover{" + attrs.get("hoverMobileStyle") + "} ");
            }
        }

        // Always apply explicit fieldLabel-* and fieldInput-* styles to their specific selectors.
        addingLabelAndInputStyle(styleClass, styles);

        return styles;
    }

    /** Returns true if this element has at least one JS-compiled per-group CSS key (new format). */
    private boolean hasCompiledGroupKeys() {
        for (String key : getProperties().keySet()) {
            if (extractCompiledGroupName(key) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts the style group name from a compiled CSS property key, or returns null when the
     * key is not a compiled key. Handles every viewport/state variant so that styling applied only
     * on hover/tablet/mobile (with no desktop-normal style) is still detected as the new format:
     *   style-{group}-compiled
     *   style-tablet-{group}-compiled / style-mobile-{group}-compiled
     *   style-hover-{group}-compiled
     *   style-hover-tablet-{group}-compiled / style-hover-mobile-{group}-compiled
     */
    private String extractCompiledGroupName(String key) {
        if (!key.startsWith("style-") || !key.endsWith("-compiled")) {
            return null;
        }
        String middle = key.substring("style-".length(), key.length() - "-compiled".length());
        // Strip the optional state (hover) prefix, then the optional viewport (tablet/mobile) prefix.
        if (middle.startsWith("hover-")) {
            middle = middle.substring("hover-".length());
        }
        if (middle.startsWith("tablet-")) {
            middle = middle.substring("tablet-".length());
        } else if (middle.startsWith("mobile-")) {
            middle = middle.substring("mobile-".length());
        }
        return middle.isEmpty() ? null : middle;
    }

    private void generatePerGroupStyles(String styleClass, Map<String, String> styles) {
        Map<String, Object> properties = getProperties();
        String[] labelSelectors = getLabelAndInputSelector(styleClass, false);
        String[] hoverLabelSelectors = getLabelAndInputSelector(styleClass, true);
        String labelInputSelector = ", " + labelSelectors[0] + ", " + labelSelectors[1];
        String labelInputHoverSelector = ", " + hoverLabelSelectors[0] + ", " + hoverLabelSelectors[1];

        // Discover group names from compiled keys across ALL viewport/state variants
        // (desktop, tablet, mobile, hover, hover-tablet, hover-mobile). A group styled only on
        // hover (e.g. style-hover-border-compiled with no desktop-normal style-border-compiled)
        // must still be discovered, otherwise its per-group inherit toggle would be ignored.
        Set<String> groupNames = new LinkedHashSet<>();
        for (String key : properties.keySet()) {
            String groupName = extractCompiledGroupName(key);
            if (groupName != null) {
                groupNames.add(groupName);
            }
        }

        for (String groupName : groupNames) {
            // Each viewport × state combination has its own inherit flag.
            // "false" means the user explicitly disabled inheritance; anything else (including absent) defaults to true.
            boolean inheritDesktop = isInheritEnabled(properties, "style-" + groupName + "-inherit");
            boolean inheritTablet = isInheritEnabled(properties, "style-tablet-" + groupName + "-inherit");
            boolean inheritMobile = isInheritEnabled(properties, "style-mobile-" + groupName + "-inherit");
            boolean inheritHoverDesktop = isInheritEnabled(properties, "style-hover-" + groupName + "-inherit");
            boolean inheritHoverTablet = isInheritEnabled(properties, "style-hover-tablet-" + groupName + "-inherit");
            boolean inheritHoverMobile = isInheritEnabled(properties, "style-hover-mobile-" + groupName + "-inherit");

            String base = "." + styleClass;
            String baseHover = "." + styleClass + ":hover";

            // Read the pre-compiled CSS strings for all six viewport × state combinations.
            String desktopCss = getCompiledGroupCss(properties, "style-" + groupName + "-compiled");
            String tabletCss = getCompiledGroupCss(properties, "style-tablet-" + groupName + "-compiled");
            String mobileCss = getCompiledGroupCss(properties, "style-mobile-" + groupName + "-compiled");
            String hoverDesktopCss = getCompiledGroupCss(properties, "style-hover-" + groupName + "-compiled");
            String hoverTabletCss = getCompiledGroupCss(properties, "style-hover-tablet-" + groupName + "-compiled");
            String hoverMobileCss = getCompiledGroupCss(properties, "style-hover-mobile-" + groupName + "-compiled");

            if (!desktopCss.isEmpty()) {
                styles.put("DESKTOP", styles.get("DESKTOP") + " " + (base + (inheritDesktop ? labelInputSelector : "")) + "{" + desktopCss + "} ");
            }
            if (!tabletCss.isEmpty()) {
                styles.put("TABLET", styles.get("TABLET") + " " + (base + (inheritTablet ? labelInputSelector : "")) + "{" + tabletCss + "} ");
            }
            if (!mobileCss.isEmpty()) {
                styles.put("MOBILE", styles.get("MOBILE") + " " + (base + (inheritMobile ? labelInputSelector : "")) + "{" + mobileCss + "} ");
            }
            if (!hoverDesktopCss.isEmpty()) {
                styles.put("DESKTOP", styles.get("DESKTOP") + " " + (baseHover + (inheritHoverDesktop ? labelInputHoverSelector : "")) + "{" + hoverDesktopCss + "} ");
            }
            if (!hoverTabletCss.isEmpty()) {
                styles.put("TABLET", styles.get("TABLET") + " " + (baseHover + (inheritHoverTablet ? labelInputHoverSelector : "")) + "{" + hoverTabletCss + "} ");
            }
            if (!hoverMobileCss.isEmpty()) {
                styles.put("MOBILE", styles.get("MOBILE") + " " + (baseHover + (inheritHoverMobile ? labelInputHoverSelector : "")) + "{" + hoverMobileCss + "} ");
            }
        }
    }

    /**
     * Returns the pre-compiled CSS string for the given property key, or an empty string if absent.
     */
    private String getCompiledGroupCss(Map<String, Object> properties, String key) {
        Object val = properties.get(key);
        return (val != null) ? val.toString() : "";
    }

    /**
     * Returns true unless the property value is explicitly "false".
     * Defaults to true (inherit ON) when the flag is absent.
     */
    private boolean isInheritEnabled(Map<String, Object> properties, String key) {
        Object val = properties.get(key);
        return !"false".equals(val != null ? val.toString() : "");
    }
    
    public String[] getLabelAndInputSelector(String styleClass, boolean isHover) {
        String[] selector = new String[]{
            "form.form-container ." + styleClass + " > label.label",
            "form.form-container ." + styleClass + " > label.label + *:not(.ui-screen-hidden):not(div.form-clear), "
            + "form.form-container ." + styleClass + " > label.label + .ui-screen-hidden + *, "
            + "form.form-container ." + styleClass + " > label.label + div.form-clear + *, "
            + "form.form-container ." + styleClass + " .form-cell-value > label, "
            + "form.form-container ." + styleClass + " .form-cell-value > label > i,"
            + "form.form-container ." + styleClass + " select option, "
            + "form.form-container ." + styleClass + " div.richtexteditor, "
            + "form.form-container ." + styleClass + " > label.label + div.input-group input, "
            + "form.form-container ." + styleClass + " > label.label + div.input-group select, "
            + "form.form-container ." + styleClass + " > label.label + div.input-group textarea"
        };
        if (isHover) {
            selector = new String[]{
                "form.form-container ." + styleClass + ":hover > label.label",
                "form.form-container ." + styleClass + ":hover > label.label + *:not(.ui-screen-hidden):not(div.form-clear), "
                + "form.form-container ." + styleClass + ":hover > label.label + .ui-screen-hidden + *, "
                + "form.form-container ." + styleClass + ":hover > label.label + div.form-clear + *, "
                + "form.form-container ." + styleClass + ":hover .form-cell-value > label, "
                + "form.form-container ." + styleClass + ":hover .form-cell-value > label > i, "
                + "form.form-container ." + styleClass + ":hover select option, "
                + "form.form-container ." + styleClass + ":hover div.richtexteditor, "
                + "form.form-container ." + styleClass + ":hover > label.label + div.input-group input, "
                + "form.form-container ." + styleClass + ":hover > label.label + div.input-group select, "
                + "form.form-container ." + styleClass + ":hover > label.label + div.input-group textarea"
            };
        }
        return selector;
    }
    
    public void addingLabelAndInputStyle(String styleClass, Map<String, String> styles) {
        String[] keys = new String[]{"fieldLabel-", "fieldInput-"};
        String[] cssClass = getLabelAndInputSelector(styleClass, false);
        String[] cssHoverClass = getLabelAndInputSelector(styleClass, true);

        for (int i=0; i < keys.length; i++) {
            Map<String, String> tempAttrs = AppPluginUtil.generateAttrAndStyles(getProperties(), keys[i]);
            if (!tempAttrs.get("desktopStyle").isEmpty()) {
                styles.put("DESKTOP", styles.get("DESKTOP") + " " + cssClass[i] +" {" + tempAttrs.get("desktopStyle") + "} ");
            }
            if (!tempAttrs.get("tabletStyle").isEmpty()) {
                styles.put("TABLET", styles.get("TABLET") + " " + cssClass[i] +" {" + tempAttrs.get("tabletStyle") + "} ");
            }
            if (!tempAttrs.get("mobileStyle").isEmpty()) {
                styles.put("MOBILE", styles.get("MOBILE") + " " + cssClass[i] +" {" + tempAttrs.get("mobileStyle") + "} ");
            }
            if (!tempAttrs.get("hoverDesktopStyle").isEmpty()) {
                styles.put("DESKTOP", styles.get("DESKTOP") + " " + cssHoverClass[i] +" {" + tempAttrs.get("hoverDesktopStyle") + "} ");
            }
            if (!tempAttrs.get("hoverTabletStyle").isEmpty()) {
                styles.put("TABLET", styles.get("TABLET") + " " + cssHoverClass[i] +" {" + tempAttrs.get("hoverTabletStyle") + "} ");
            }
            if (!tempAttrs.get("hoverMobileStyle").isEmpty()) {
                styles.put("MOBILE", styles.get("MOBILE") + " " + cssHoverClass[i] +" {" + tempAttrs.get("hoverMobileStyle") + "} ");
            }
        }
    }
    
    /**
     * Helper method to find the index of the first occurence of class attribute in the HTML string.
     * @param html The HTML string to search.
     * @return The index of the class attribute.
     */
    public int findClassAttrIndex(String html) {
        int index = html.indexOf("class=");
        if (this instanceof Form) {
            index = html.indexOf("class=\"form-container");
        }
        return index;
    }

    /**
     * Helper method to check if icon is included within the field
     * @param dataModel the dataModel
     */
    public void checkIfIconIsPresent(Map dataModel) {
        if (getPropertyString("iconIncluded") != null && getPropertyString("iconIncluded").equals("true")
                && !"label".equals(getPropertyString("iconPosition"))) {
            dataModel.put("classIdentifier", " withIcon");
        }

        String label = getPropertyString("label");
        if (label == null) {
            dataModel.put("label", "");
            dataModel.put("iconValue", "");
            return;
        }

        // When iconPosition is "label", keep the icon HTML inside the label element
        // instead of extracting it as an input-group prefix (iconValue).
        if ("label".equals(getPropertyString("iconPosition"))) {
            dataModel.put("label", label);
            dataModel.put("iconValue", "");
            return;
        }

        String[] parts = label.split("</i>", 2);

        if (parts.length == 2) {
            String icon = "<style>\r\n" + //
                                "            body .form-container span.input-group-text + [class*=\"form-cell-value\"] {\r\n" + //
                                "                padding: 4px 10px;\r\n" + //
                                "            }\r\n" + //
                                "           div.input-group { \r\n" +
                                "               flex-wrap: nowrap !important;\r\n" +
                                "               max-width: 70%;\r\n" + //
                                "           }\r\n" +
                                "           .label-top.form-cell div.input-group,\r\n" + //
                                "           .label-top.subform-cell div.input-group { \r\n" +
                                "               max-width: 100%;\r\n" + //
                                "           }\r\n" +
                                "           div.input-group input,  \r\n" +
                                "           div.input-group select, \r\n" +
                                "           div.input-group textarea { \r\n" +
                                "               border-inline-start: none; \r\n" +
                                "               border-start-start-radius: 0;\r\n" +
                                "               border-end-start-radius: 0;\r\n" +
                                "           }\r\n" +
                                "           div.input-group > .input-group-text { \r\n" +
                                "               border-start-end-radius: 0;\r\n" +
                                "               border-end-end-radius: 0;\r\n" +
                                "           }\r\n" +
                                "           div.input-group > .input-group-text + * { \r\n" +
                                "               width: 100%;\r\n" +
                                "           }\r\n" +
                                "        </style>" + 
                                parts[0] + 
                                "</i>";
            String labelOnly = parts[1];
            dataModel.put("label", labelOnly);
            dataModel.put("iconValue", icon);
        } else {
            dataModel.put("label", label);
            dataModel.put("iconValue", "");
        }
    }

    public String decorateWithBuilderProperties(String html, FormData formData) {
        Map<String, String> attrs = AppPluginUtil.generateAttrAndStyles(getProperties(), "");

        String builderStyles = "";
        String cssClass = attrs.get("cssClass");
        String styleClass = "builder-style-"+getPropertyString("elementUniqueKey");

        Map<String, String> styles = getElementStyles(styleClass, attrs);

        if (!styles.get("DESKTOP").isEmpty() || !styles.get("TABLET").isEmpty() || !styles.get("MOBILE").isEmpty()) {
            cssClass += " " + styleClass;
            if (!styles.get("DESKTOP").isEmpty()) {
                builderStyles += styles.get("DESKTOP");
            }
            if (!styles.get("TABLET").isEmpty()) {
                builderStyles += " @media (max-width: 991px) {" + styles.get("TABLET") + "} ";
            }
            if (!styles.get("MOBILE").isEmpty()) {
                builderStyles += " @media (max-width: 767px) {" + styles.get("MOBILE") + "} ";
            }
        }

        if (!cssClass.isEmpty() || !attrs.get("attr").isEmpty()) {
            if (this instanceof Form) {
                // SAFER: Preserve <form class="form-container"> for subforms
                int index = findClassAttrIndex(html);
                if (index != -1) {
                    String remaining = html.substring(index);

                    // Locate the opening quote of the class attribute (supports " or ')
                    int qStart = remaining.indexOf("\"");
                    if (qStart == -1) qStart = remaining.indexOf("'");
                    if (qStart != -1) {
                        char qChar = remaining.charAt(qStart);
                        int qEnd = remaining.indexOf(qChar, qStart + 1);

                        if (qEnd != -1) {
                            StringBuilder sb = new StringBuilder();

                            // Preserve everything before the class attribute
                            sb.append(html, 0, index);

                            // Insert additional attributes before the class
                            String extra = attrs.get("attr");
                            if (extra != null && !extra.trim().isEmpty()) {
                                sb.append(extra).append(" ");
                            }

                            // Rebuild the class attribute with existing + new classes
                            String existing = remaining.substring(qStart + 1, qEnd).trim();
                            sb.append("class=").append(qChar);
                            if (!existing.isEmpty()) sb.append(existing).append(" ");
                            if (!cssClass.trim().isEmpty()) sb.append(cssClass);
                            sb.append(qChar);

                            // Append the rest of the HTML unchanged
                            sb.append(remaining.substring(qEnd + 1));
                            html = sb.toString();
                        }
                    }
                }
            } else {
                // Regex only for NON-form elements
                html = html.replaceFirst("(class=(['\"]))(.*?)\\2",
                    attrs.get("attr") + " $1$3 " + cssClass + "$2");
            }
        }

        if (!builderStyles.isEmpty()) {
            if (this instanceof Form) {
               int index = html.lastIndexOf("</form>");

                // Unauthorized message or non-form rendering may not contain </form>
                if (index != -1) {
                    html = html.substring(0, index)
                         + "<style id=\"" + styleClass + "\">"
                         + builderStyles
                         + "</style></form>";
                }
            } else {
                int index = html.lastIndexOf("</div>");
                if (index != -1) {
                    html = html.substring(0, index)
                         + "<style id=\"" + styleClass + "\">"
                         + builderStyles
                         + "</style></div>";
                }
            }
        }

        return html;
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
        if (FormUtil.isHidden(this, formData)) {
            return "";
        }
        
        if (FormUtil.isReadonly(this, formData)) {
            this.setProperty(FormUtil.PROPERTY_READONLY, "true");
        } else {
            this.setProperty(FormUtil.PROPERTY_READONLY, "");
        }
        
        return renderTemplate(formData, dataModel);
    }

    /**
     * Read-only HTML template for front-end UI (Not used at the moment)
     * @param formData
     * @param dataModel Model containing values to be displayed in the template.
     * @return
     */
    public String renderReadOnlyTemplate(FormData formData, Map dataModel) {
        if (FormUtil.isHidden(this, formData)) {
            return "";
        }
        
        if (FormUtil.isReadonly(this, formData)) {
            this.setProperty(FormUtil.PROPERTY_READONLY, "true");
        } else {
            this.setProperty(FormUtil.PROPERTY_READONLY, "");
        }
        
        // set readonly flag
        dataModel.put(FormUtil.PROPERTY_READONLY, Boolean.TRUE);

        return renderTemplate(formData, dataModel);
    }

    /**
     * Get visibility rules for this element.
     * @param formData
     * @return Collection of visibility rules
     */
    public Collection<Map<String, String>> getVisibilityRules(FormData formData) {
        if (visibilityRules == null) {
            visibilityRules = VisibilityControlUtil.parseVisibilityRules(this, formData, visibilityControlElements);
        }
        return visibilityRules;
    }

    /**
     * Check if the visibility control rules match for this element.
     *
     * @param formData
     * @return true if element should be visible, false otherwise
     */
    public Boolean isVisibilityMatch(FormData formData) {
        return VisibilityControlUtil.evaluateVisibilityRules(
                getVisibilityRules(formData),
                visibilityControlElements,
                formData);
    }

    /**
     * Check if element has visibility control rules defined.
     *
     * @return true if visibility rules are defined
     */
    public boolean hasVisibilityRules() {
        return VisibilityControlUtil.hasVisibilityControl(this);
    }

    /**
     * Generate JSON representation of visibility rules for client-side JavaScript.
     *
     * @param formData
     * @return JSON string of visibility rules
     */
    public String getVisibilityRulesJson(FormData formData) {
        return VisibilityControlUtil.toJson(getVisibilityRules(formData));
    }

    /**
     * Flag to indicate whether or not continue validating descendent elements.
     * Checks for visibility control rules in addition to hidden status.
     *
     * @param formData
     * @return
     */
    public boolean continueValidation(FormData formData) {
        if (this instanceof Section) {
            return !isHidden(formData);
        }

        Boolean continueValidation = continueValidations.get(formData);
        if (continueValidation == null) {
            if (!isHidden(formData)) {
                // Check visibility control rules
                String visibilityControl = getPropertyString("visibilityControl");
                if (visibilityControl != null && !visibilityControl.isEmpty()) {
                    continueValidation = isVisibilityMatch(formData);
                } else {
                    continueValidation = true;
                }
            } else {
                continueValidation = false;
            }
            continueValidations.put(formData, continueValidation);
        }
        return continueValidation;
    }
    
    /**
     * Set default Plugin Properties Options value to a new added Field in Form Builder.
     * 
     * @return 
     */
    public String getDefaultPropertyValues(){
        return DefaultPropertyValuesCache.getDefaultPropertyValues(this);
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
        Boolean isAuthorize = isAuthorizeSet.get(formData);
        if (isAuthorize == null) {
            isAuthorize = true;
            if (Permission.DEFAULT.equals(getPermissionKey(formData))) {
                Map permission = (Map) getProperty("permission");
                if (permission != null) {
                    isAuthorize = FormUtil.getPermissionResult(permission, formData);
                } else if (getParent() != null) {
                    isAuthorize = getParent().isAuthorize(formData);
                }
            } else {
                if (this instanceof Section) {
                    Map rules = (Map) getProperty("permission_rules");
                    if (rules != null && rules.containsKey(getPermissionKey(formData))) {
                        Map rule = (Map) rules.get(getPermissionKey(formData));
                        isAuthorize = FormUtil.getPermissionResult((Map) rule.get("permission"), formData);
                    }
                } else if (getParent() != null) {
                    isAuthorize = getParent().isAuthorize(formData);
                }
            }
            isAuthorizeSet.put(formData, isAuthorize);
        }
        return isAuthorize;
    }
    
    @Override
    public void setProperty(String property, Object value) {
        if (FormUtil.PROPERTY_READONLY.equals(property)) {
            isReadonlySet.clear();
        }
        super.setProperty(property, value);
    }
    
    /**
     * Flag to indicate whether or not the current logged in user is able to edit this field in the form.
     * If the value is edited through DOM manipulation, the value WILL NOT be saved into database.
     *
     * @param formData
     * @return 
     */
    public Boolean isReadonly(FormData formData) {
        Boolean isReadonly = isReadonlySet.get(formData);
        if (isReadonly == null) {
            boolean isParentReadonly = false;
            boolean isParentPersistableReadonly = false;
            if (getParent() != null) {
                isParentReadonly = getParent().isReadonly(formData);
                isParentPersistableReadonly = getParent().isPersistableReadonly(formData);
            }
            if ((!isParentReadonly && !isParentPersistableReadonly) || !isAuthorize(formData)) {
                Map props = getProperties();
                if (!Permission.DEFAULT.equals(getPermissionKey(formData)) && !(this instanceof Form)) {
                    Map rules = (Map) getProperty("permission_rules");
                    if (rules != null && rules.containsKey(getPermissionKey(formData))) {
                        props = (Map)rules.get(getPermissionKey(formData));
                    }
                }

                if (props == null) {
                    props = new HashMap();
                }

                if (isAuthorize(formData)) {
                    String readonlyProp = "";
                    String hiddenProp = "";
                    if (props.containsKey(FormUtil.PROPERTY_READONLY)) {
                        readonlyProp = (String) props.get(FormUtil.PROPERTY_READONLY);
                    }
                    if (props.containsKey(FormUtil.PROPERTY_HIDDEN)) {
                        hiddenProp = (String) props.get(FormUtil.PROPERTY_HIDDEN);
                    }

                    isReadonly = "true".equalsIgnoreCase(readonlyProp) || "true".equalsIgnoreCase(hiddenProp);
                } else {
                    if (isParentPersistableReadonly) {
                        isReadonly = false;
                    } else if (props.containsKey("permissionReadonly")) {
                        isReadonly = "true".equalsIgnoreCase((String) props.get("permissionReadonly"));
                    } else if (props.containsKey("permissionReadonlyHidden")) {
                        isReadonly = "".equalsIgnoreCase((String) props.get("permissionReadonlyHidden"));
                    } else {
                        if (isParentPersistableReadonly) {
                            isReadonly = false;
                        } else {
                            isReadonly = true;
                        }
                    }
                }
            } else {
                if (isParentPersistableReadonly) {
                    isReadonly = false;
                } else {
                    isReadonly = true;
                }
            }
            isReadonlySet.put(formData, isReadonly);
        }
        return isReadonly;
    }

    /**
     * Flag to indicate whether or not the current logged in user is able to edit this field in the form.
     * If the value is edited through DOM manipulation, the value WILL be saved into database.
     *
     * @param formData
     * @return
     */
    public Boolean isPersistableReadonly (FormData formData) {
        Boolean isPersistableReadonly = isPersistableReadonlySet.get(formData);
        if (isPersistableReadonly == null) {
            boolean isParentReadonly = false;
            boolean isParentPersistableReadonly = false;
            if (getParent() != null) {
                isParentReadonly = getParent().isReadonly(formData);
                isParentPersistableReadonly = getParent().isPersistableReadonly(formData);
            }
            if ((!isParentReadonly && !isParentPersistableReadonly) || !isAuthorize(formData)) {
                Map props = getProperties();
                if (!Permission.DEFAULT.equals(getPermissionKey(formData)) && !(this instanceof Form)) {
                    Map rules = (Map) getProperty("permission_rules");
                    if (rules != null && rules.containsKey(getPermissionKey(formData))) {
                        props = (Map)rules.get(getPermissionKey(formData));
                    }
                }

                if (props == null) {
                    props = new HashMap();
                }

                if (isAuthorize(formData)) {
                    isPersistableReadonly = "readonly".equalsIgnoreCase((String) props.get(FormUtil.PROPERTY_READONLY));
                } else {
                    if (isParentPersistableReadonly) {
                        isPersistableReadonly = true;
                    } else if (props.containsKey("permissionReadonly")) {
                        isPersistableReadonly = "readonly".equalsIgnoreCase((String) props.get("permissionReadonly"));
                    } else if (props.containsKey("permissionReadonlyHidden")) {
                        isPersistableReadonly = "readonly".equalsIgnoreCase((String) props.get("permissionReadonlyHidden"));
                    } else {
                        if (isParentReadonly) {
                            isPersistableReadonly = false;
                        } else {
                            isPersistableReadonly = true;
                        }
                    }
                }
            } else {
                if (isParentReadonly) {
                    isPersistableReadonly = false;
                } else {
                    isPersistableReadonly = true;
                }
            }
            isPersistableReadonlySet.put(formData, isPersistableReadonly);
        }
        return isPersistableReadonly;
    }
    
    /**
     * Flag to indicate whether or not the current logged in user is able to view this field in the form.
     * 
     * @param formData
     * @return 
     */
    public Boolean isHidden(FormData formData) {
        Boolean isHidden = isHiddenSet.get(formData);
        if (isHidden == null) {
            if (this instanceof Form) {
                isHidden = false;
            } else {
                boolean isParentHidden = false;
                boolean isParentReadonly = false;
                boolean isParentPersistableReadonly = false;
                if (getParent() != null) {
                    isParentHidden = getParent().isHidden(formData);
                    isParentReadonly = getParent().isReadonly(formData);
                    isParentPersistableReadonly = getParent().isPersistableReadonly(formData);
                }
                if (!isParentHidden && (isParentReadonly || isParentPersistableReadonly) && !(this instanceof Section) && isAuthorize(formData)) {
                    //section in subform should check for permission plugin, so should not just follow parent permission.
                    //for authorized user, based on permission setting, if parent is readonly, all childs are readonly as well
                    isHidden = false;
                } else if (!isParentHidden) {
                    Map props = getProperties();
                    if (!Permission.DEFAULT.equals(getPermissionKey(formData))) {
                        Map rules = (Map) getProperty("permission_rules");
                        if (rules != null && rules.containsKey(getPermissionKey(formData))) {
                            props = (Map)rules.get(getPermissionKey(formData));
                        }
                    }

                    if (props == null) {
                        props = new HashMap();
                    }

                    if (isAuthorize(formData)) {
                        isHidden = "true".equalsIgnoreCase((String) props.get(FormUtil.PROPERTY_HIDDEN));
                    } else {
                        if (isParentPersistableReadonly) {
                            isHidden = false;
                        } else if (props.containsKey("permissionReadonly")) {
                            isHidden = !"true".equalsIgnoreCase((String) props.get("permissionReadonly")) && !"readonly".equalsIgnoreCase((String) props.get("permissionReadonly"));;
                        } else if (this instanceof Section) {
                            isHidden = true;
                        } else {
                            isHidden = "true".equalsIgnoreCase((String) props.get("permissionReadonlyHidden"));
                        }
                    }
                } else {
                    isHidden = true;
                }
            }
            isHiddenSet.put(formData, isHidden);
        }
        return isHidden;
    }
    
    public String getPermissionKey(FormData formData) {
        if (!permissionKeys.containsKey(formData)) {
            if (getParent() != null) {
                permissionKeys.put(formData, getParent().getPermissionKey(formData));
            } else {
                permissionKeys.put(formData, Permission.DEFAULT);
            }
        }
        return permissionKeys.get(formData);
    }
    
    @Override
    public String getPluginIcon() {
        if (this instanceof FormBuilderPaletteElement) {
            return ((FormBuilderPaletteElement) this).getFormBuilderIcon();
        }   
        return "";
    }
    
    public void setUniqueKey(String uniqueKey) {
        //remove `-` from unique key to prevent it is used as function name and causing js error
        uniqueKey = uniqueKey.replaceAll("-", "_");
        
        //check is there a same field id under same parent, make it unique by adding index
        if (getParent() != null) {
            if (getParent().childsUniqueKeys == null) { //Cloud security AOP may caused this not initiallized 
                getParent().childsUniqueKeys = new HashSet<String>();
            }
            
            if (getParent().childsUniqueKeys.contains(uniqueKey)) {
                uniqueKey += getParent().childsUniqueKeys.size();
            }
            getParent().childsUniqueKeys.add(uniqueKey);
        }
        
        setProperty(FormUtil.PROPERTY_ELEMENT_UNIQUE_KEY, uniqueKey);
    }
}
