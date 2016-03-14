package org.joget.plugin.base;

public class PluginProperty {

    public static final String TYPE_TEXTFIELD = "textfield";
    public static final String TYPE_TEXTAREA = "textarea";
    public static final String TYPE_CHECKBOX = "checkbox";
    public static final String TYPE_RADIO = "radio";
    public static final String TYPE_SELECTBOX = "selectbox";
    public static final String TYPE_PASSWORD = "password";
    private String name;
    private String label;
    private String type;
    private String[] options;
    private String value;

    public PluginProperty() {
    }

    public PluginProperty(String name, String label, String type, String[] options, String value) {
        setName(name);
        setLabel(label);
        setType(type);
        setOptions(options);
        setValue(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
