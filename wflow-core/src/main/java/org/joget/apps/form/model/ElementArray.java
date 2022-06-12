package org.joget.apps.form.model;

import java.util.Map;
import org.joget.plugin.base.HiddenPlugin;

/**
 * A wrapper used to return a array of elements for multi control field AJAX cascading options
 */
public class ElementArray extends Element implements HiddenPlugin {

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        return null;
    }

    @Override
    public String getName() {
        return "ElementArray";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getLabel() {
        return "ElementArray";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return null;
    }
}
