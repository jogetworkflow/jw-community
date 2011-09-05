package org.joget.apps.userview.model;

import java.util.Collection;
import org.joget.commons.util.ResourceBundleUtil;

public class UserviewCategory extends Element {

    private UserviewPermission permission;
    private Collection<UserviewMenu> menus;

    public UserviewPermission getPermission() {
        return permission;
    }

    public void setPermission(UserviewPermission permission) {
        this.permission = permission;
    }

    public Collection<UserviewMenu> getMenus() {
        return menus;
    }

    public void setMenus(Collection<UserviewMenu> menus) {
        this.menus = menus;
    }

    public String getLabel() {
        return "Category";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return "[{title:'" + ResourceBundleUtil.getMessage("userview.userviewcategory.setPermission") + "', properties:[{name:'id',label:'ID',type:'hidden'},{name:'label',label:'Label',type:'hidden'},{name:'hide',label:'" + ResourceBundleUtil.getMessage("userview.userviewcategory.hideFromMenu") + "', type:'checkbox', options:[{value:'yes', label:''}]},{name:'permission',label:'" + ResourceBundleUtil.getMessage("userview.userviewcategory.permission") + "',type:'elementselect',options_ajax:'[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.userview.model.UserviewPermission',url:'[CONTEXT_PATH]/web/property/json/getPropertyOptions'}]}]";
    }

    public String getDefaultPropertyValues() {
        return "";
    }
}
