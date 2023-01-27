package org.joget.apps.userview.model;

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

@Component
public class UserviewBuilderPalette {

    /**
     * General category for common menus
     */
    public static final String CATEGORY_GENERAL = "Basic";
    /**
     * Category for miscellaneous or custom menus
     */
    public static final String CATEGORY_CUSTOM = "Custom";
    @Autowired
    PluginManager pluginManager;

    /**
     * Retrieves the elements available for userview builder palette
     * @return
     */
    public Map<String, List<UserviewMenu>> getUserviewMenuCategoryMap(Map basicRequestParams) {
        Map<String, List<UserviewMenu>> categoryMap = new TreeMap<String, List<UserviewMenu>>();

        // get available elements from the plugin manager
        Collection<Plugin> elementList = pluginManager.list(UserviewMenu.class);

        // add elements to palette
        for (Plugin element : elementList) {
            if (element instanceof UserviewMenu) {
                UserviewMenu um = new CachedUserviewMenu((UserviewMenu)element);
                um.setRequestParameters(basicRequestParams);
                addElement(categoryMap, um);
            }
        }

        return categoryMap;
    }

    protected void addElement(Map<String, List<UserviewMenu>> categoryMap, UserviewMenu elementMetaData) {
        // get element list for the the category
        String category = elementMetaData.getCategory();
        List<UserviewMenu> elementList = categoryMap.get(category);
        if (elementList == null) {
            elementList = new ArrayList<UserviewMenu>();
            categoryMap.put(category, elementList);
        }

        // add element to the list
        elementList.add(elementMetaData);

        // sort by label
        Collections.sort(elementList, new Comparator<UserviewMenu>() {

            public int compare(UserviewMenu o1, UserviewMenu o2) {
                return o1.getI18nLabel().compareTo(o2.getI18nLabel());
            }
        });
    }
}
