package org.kecak.apps.userview.model;

import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterType;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.userview.model.UserviewMenu;

import java.util.Map;

/**
 * @author aristo
 *
 * Bootstrap Web UI Framework for Userview Theme
 */
public interface BootstrapUserviewTheme extends BootstrapUserview {
    /**
     * WARNING!!!!! these methods have to be implemented in Kecak Core
     * JSP File in Plugins cannot be recognized by Kecak Core
     * @return
     */

    String getUserviewJsp();

    String getPreviewJsp();

    String getDataListJsp();

    String getFormJsp();

    String getRunProcessJsp();

    String getLoginJsp();

    default String getUnauthorizedJsp() {
        return "userview/plugin/unauthorized.jsp";
    }

    /**
     * Override this method to handle custom own form element's template
     *
     * @param element
     * @param formData
     * @param dataModel
     * @return
     */
    String renderBootstrapFormElementTemplate(Element element, FormData formData, Map dataModel);

    /**
     * Override this method to handle custom own userview menu's jsp
     * @param menu
     * @return
     */
    String getBootstrapJspPage(UserviewMenu menu);

    /**
     * Override this method to handle custom render page
     * @param menu
     * @return
     */
    String getBootstrapRenderPage(UserviewMenu menu);

    /**
     * Override this method to handle custom menu decoration
     * @param menu
     * @return
     */
    String getBootstrapDecoratedMenu(UserviewMenu menu);

    /**
     * Get navigation bar header
     * @return
     */
    String getNavigationBarHeader();

    String renderBootstrapDataListFilterTemplate(DataList dataList, DataListFilterType filterType, String name, String label);
}
