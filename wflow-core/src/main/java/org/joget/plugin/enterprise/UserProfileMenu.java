package org.joget.plugin.enterprise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.directory.dao.UserDao;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.directory.model.service.UserSecurity;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;

public class UserProfileMenu extends UserviewMenu {

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "User Profile";
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-user-edit\"></i>";
    }

    public String getName() {
        return "User Profile Menu";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/userProfileMenu.json", null, true, "message/userview/userProfileMenu");
    }

    @Override
    public String getDecoratedMenu() {
        if ("true".equals(getRequestParameter("isPreview")) || "Yes".equals(getPropertyString("showInPopupDialog"))) {
            // sanitize label
            String label = getPropertyString("label");
            if (label != null) {
                label = StringUtil.stripHtmlRelaxed(label);
            }

            String menu = "<a onclick=\"menu_" + getPropertyString("id") + "_showDialog();return false;\" class=\"menu-link\"><span>" + label + "</span></a>";
            menu += "<script>\n";

            if ("Yes".equals(getPropertyString("showInPopupDialog"))) {
                String url = getUrl() + "?embed=true";

                menu += "var menu_" + getPropertyString("id") + "Dialog = new PopupDialog(\"" + url + "\",\"\");\n";
            }
            menu += "function menu_" + getPropertyString("id") + "_showDialog(){\n";
            if ("true".equals(getRequestParameter("isPreview"))) {
                menu += "alert('Feature disabled in Preview Mode.');\n";
            } else {
                menu += "menu_" + getPropertyString("id") + "Dialog.init();\n";
            }
            menu += "}\n</script>";
            return menu;
        }
        return null;
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
    }

    @Override
    public String getRenderPage() {
        if ("true".equals(getRequestParameterString("isPreview"))) {
            setProperty("isPreview", "true");
        } else {
            if ("submit".equals(getRequestParameterString("action"))) {
                // only allow POST
                HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                if (request != null && !"POST".equalsIgnoreCase(request.getMethod())) {
                    PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
                    String content = pluginManager.getPluginFreeMarkerTemplate(new HashMap(), getClass().getName(), "/templates/unauthorized.ftl", null);
                    return content;
                }

                submitForm();
            } else {
                viewForm(null);
            }
        }
        Map model = new HashMap();
        model.put("request", getRequestParameters());
        model.put("element", this);
        
        PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
        String content = pluginManager.getPluginFreeMarkerTemplate(model, getClass().getName(), "/templates/userProfile.ftl", null);
        return content;
    }
    
    private void viewForm(User submittedData) {
        setProperty("headerTitle", getPropertyString("label"));
        setProperty("view", "formView");

        ApplicationContext ac = AppUtil.getApplicationContext();
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) ac.getBean("workflowUserManager");
        UserDao userDao = (UserDao) ac.getBean("userDao");
        User user = submittedData;
        if (user == null) {
            user = userDao.getUser(workflowUserManager.getCurrentUsername());
        }
        if (user != null && user.getReadonly()) {
            return;
        }
        setProperty("user", user);
        setProperty("timezones", TimeZoneUtil.getList());
        
        SetupManager setupManager = (SetupManager) ac.getBean("setupManager");
        String enableUserLocale = setupManager.getSettingValue("enableUserLocale");
        Map<String, String> localeStringList = new TreeMap<String, String>();
        if(enableUserLocale != null && enableUserLocale.equalsIgnoreCase("true")) {
            String userLocale = setupManager.getSettingValue("userLocale");
            Collection<String> locales = new HashSet();
            locales.addAll(Arrays.asList(userLocale.split(",")));
            
            Locale[] localeList = Locale.getAvailableLocales();
            for (int x = 0; x < localeList.length; x++) {
                String code = localeList[x].toString();
                if (locales.contains(code)) {
                    localeStringList.put(code, code + " - " +localeList[x].getDisplayName());
                }
            }
        }
        setProperty("enableUserLocale", enableUserLocale);
        setProperty("localeStringList", localeStringList);
        
        UserSecurity us = DirectoryUtil.getUserSecurity();
        if (us != null) {
            setProperty("policies", us.passwordPolicies());
            setProperty("userProfileFooter", us.getUserProfileFooter(user));
        }
        
        String url = getUrl() + "?action=submit";
        setProperty("actionUrl", url);
    }

    private void submitForm() {
        ApplicationContext ac = AppUtil.getApplicationContext();
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) ac.getBean("workflowUserManager");
        UserDao userDao = (UserDao) ac.getBean("userDao");
        User userObject = userDao.getUser(workflowUserManager.getCurrentUsername());
        User currentUser = null;
        if (userObject != null) {
            currentUser = new User();
            BeanUtils.copyProperties(userObject, currentUser);
        }
        ExtDirectoryManager directoryManager = (ExtDirectoryManager) AppUtil.getApplicationContext().getBean("directoryManager");
           
        Collection<String> errors = new ArrayList<String>();
        Collection<String> passwordErrors = new ArrayList<String>();
        
        boolean authenticated = false;
        if (currentUser != null) {
            if (!currentUser.getUsername().equals(getRequestParameterString("username"))) {
                HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                if (request != null) {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.invalidate();
                        authenticated = false;
                    }
                }
            } else {
                try {
                    if (directoryManager.authenticate(currentUser.getUsername(), getRequestParameterString("oldPassword"))) {
                        authenticated = true;
                    }
                } catch (Exception e) { }
            }
        }
        UserSecurity us = DirectoryUtil.getUserSecurity();
        
        if ("".equals(getPropertyString("f_firstName")) && !StringUtil.stripAllHtmlTag(getRequestParameterString("firstName")).isEmpty()) {
            currentUser.setFirstName(StringUtil.stripAllHtmlTag(getRequestParameterString("firstName")));
        }

        if ("".equals(getPropertyString("f_lastName"))) {
            currentUser.setLastName(StringUtil.stripAllHtmlTag(getRequestParameterString("lastName")));
        }

        if ("".equals(getPropertyString("f_email"))) {
            currentUser.setEmail(getRequestParameterString("email"));
        }

        if ("".equals(getPropertyString("f_timeZone"))) {
            currentUser.setTimeZone(getRequestParameterString("timeZone"));
        }

        if ("".equals(getPropertyString("f_locale"))) {
            currentUser.setLocale(getRequestParameterString("locale"));
        }

        if (!authenticated) {
            if (errors == null) {
                errors = new ArrayList<String>();
            }
            errors.add(ResourceBundleUtil.getMessage("console.directory.user.error.label.authenticationFailed"));
        } else {
            if (us != null) {
                errors = us.validateUserOnProfileUpdate(currentUser);
            }

            if (getRequestParameterString("password") != null && !getRequestParameterString("password").isEmpty() && us != null) {
                passwordErrors = us.validatePassword(getRequestParameterString("username"),  getRequestParameterString("oldPassword"), getRequestParameterString("password"), getRequestParameterString("confirmPassword")); 
            }
        }

        setProperty("errors", errors);
        if (passwordErrors != null && !passwordErrors.isEmpty()) {
            setProperty("passwordErrors", passwordErrors);
        }
        
        if (authenticated && (passwordErrors != null && passwordErrors.isEmpty()) && (errors != null && errors.isEmpty())) {
            if ("".equals(getPropertyString("f_password"))) {
                if (getRequestParameterString("password") != null && getRequestParameterString("confirmPassword") != null && getRequestParameterString("password").length() > 0 && getRequestParameterString("password").equals(getRequestParameterString("confirmPassword"))) {
                    if (us != null) {
                        currentUser.setPassword(us.encryptPassword(getRequestParameterString("username"), getRequestParameterString("password")));
                    } else {
                        currentUser.setPassword(StringUtil.md5Base16(getRequestParameterString("password")));
                    }
                    currentUser.setConfirmPassword(getRequestParameterString("password"));
                }
            }

            if (currentUser.getUsername().equals(getRequestParameterString("username"))) {
                userDao.updateUser(currentUser);
                if (us != null) {
                    us.updateUserProfilePostProcessing(currentUser);
                }
                
                setAlertMessage(getPropertyString("message"));
                setProperty("headerTitle", getPropertyString("label"));
                if (getPropertyString("redirectURL") != null && !getPropertyString("redirectURL").isEmpty()) {
                    setProperty("view", "redirect");
                    boolean redirectToParent = "Yes".equals(getPropertyString("showInPopupDialog"));
                    setRedirectUrl(getPropertyString("redirectURL"), redirectToParent);
                } else {
                    setProperty("saved", "true");
                    viewForm(null);
                }
            }
        } else {
            viewForm(currentUser);
        }
    }

    @Override
    public String getCategory() {
        return UserviewBuilderPalette.CATEGORY_GENERAL;
    }
}
