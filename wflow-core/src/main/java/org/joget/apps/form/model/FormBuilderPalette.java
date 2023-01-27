package org.joget.apps.form.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Represents the form builder palette, storing elements in sorted order
 */
@Component
public class FormBuilderPalette {

    /**
     * Hidden category - not shown in Form Builder palette
     */
    public static final String CATEGORY_NONE = "None";
    /**
     * General category for common elements
     */
    public static final String CATEGORY_GENERAL = "Basic";
    /**
     * Category for miscellaneous or custom elements
     */
    public static final String CATEGORY_CUSTOM = "Custom";
    @Autowired
    PluginManager pluginManager;

    public FormBuilderPalette() {
    }

    /**
     * Retrieves the elements available in the Form Builder palette grouped by category.
     * @return
     */
    public Map<String, List<FormBuilderPaletteElement>> getElementCategoryMap() {
        Map<String, List<FormBuilderPaletteElement>> categoryMap = new TreeMap<String, List<FormBuilderPaletteElement>>();

        // get available elements from the plugin manager
        Collection<Plugin> elementList = pluginManager.list(FormBuilderPaletteElement.class);

        // add elements to palette
        for (Plugin element : elementList) {
            if (element instanceof FormBuilderPaletteElement) {
                addElement(categoryMap, (FormBuilderPaletteElement) element);
            }
        }

        return categoryMap;
    }

    protected void addElement(Map<String, List<FormBuilderPaletteElement>> categoryMap, FormBuilderPaletteElement elementMetaData) {
        // get element list for the the category
        String category = elementMetaData.getFormBuilderCategory();
        List<FormBuilderPaletteElement> elementList = categoryMap.get(category);
        if (elementList == null) {
            elementList = new ArrayList<FormBuilderPaletteElement>();
            categoryMap.put(category, elementList);
        }

        // add element to the list
        elementList.add(elementMetaData);

        // sort by position
        Collections.sort(elementList, new Comparator<FormBuilderPaletteElement>() {

            public int compare(FormBuilderPaletteElement o1, FormBuilderPaletteElement o2) {
                return o1.getFormBuilderPosition() - o2.getFormBuilderPosition();
            }
        });
    }

    /**
     * Retrieves the elements available for property editing in the Form Builder.
     * @return
     */
    public Collection<Plugin> getEditableElementList() {
        // get available elements from the plugin manager
        Collection<Plugin> elementList = pluginManager.list(FormBuilderEditable.class);
        return elementList;
    }
}
