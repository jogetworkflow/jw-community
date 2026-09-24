package org.joget.apps.app.service;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.model.AbstractAppVersionedObject;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.CustomBuilder;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DependenciesUtil {

    /**
     * NamedDefinitionList nodeType for the separately persisted page
     * component content of a userview page ({@code BuilderDefinition}
     * rows of type {@link UserviewService#TYPE}). These share storage with
     * real {@code CustomBuilder} plugin definitions but aren't one, so they
     * are split out from the {@code "cbuilder"} list rather than routed
     * through {@link CustomBuilderUtil#getBuilder(String)}.
     */
    private static final String NODE_TYPE_USERVIEW_PAGE = "userviewpage";

    /**
     * A named {@code AppDefinition} object list whose JSON body may reference
     * a dependency keyword, paired with the usage "type" (form/userview/
     * datalist/cbuilder) it represents.
     */
    public static final class NamedDefinitionList {
        private final String nodeType;
        private final Collection<? extends AbstractAppVersionedObject> definitions;

        public NamedDefinitionList(String nodeType, Collection<? extends AbstractAppVersionedObject> definitions) {
            this.nodeType = nodeType;
            this.definitions = definitions;
        }

        public String getNodeType() {
            return nodeType;
        }

        public Collection<? extends AbstractAppVersionedObject> getDefinitions() {
            return definitions;
        }
    }

    /**
     * Single source of truth for the {@code AppDefinition} object lists whose
     * JSON body may reference a dependency keyword (forms, userviews,
     * datalists, and custom builders). Shared with
     * {@link org.joget.governance.lib.OrphanedFormDataCheck} so a newly added
     * definition list only needs to be registered here once.
     */
    public static List<NamedDefinitionList> getJsonDefinitionLists(AppDefinition appDef) {
        List<NamedDefinitionList> lists = new ArrayList<NamedDefinitionList>();
        lists.add(new NamedDefinitionList("form", appDef.getFormDefinitionList()));
        lists.add(new NamedDefinitionList("userview", appDef.getUserviewDefinitionList()));
        lists.add(new NamedDefinitionList("datalist", appDef.getDatalistDefinitionList()));
        lists.add(new NamedDefinitionList("cbuilder", filterBuilderDefinitions(appDef.getBuilderDefinitionList(), false)));
        lists.add(new NamedDefinitionList(NODE_TYPE_USERVIEW_PAGE, filterBuilderDefinitions(appDef.getBuilderDefinitionList(), true)));
        return lists;
    }

    /**
     * Splits {@code BuilderDefinition} rows into real {@code CustomBuilder}
     * plugin definitions and internal userview page content, so the two
     * shapes aren't scanned through the same {@code "cbuilder"} branch.
     */
    private static Collection<BuilderDefinition> filterBuilderDefinitions(Collection<BuilderDefinition> builderDefinitionList, boolean userviewPageOnly) {
        if (builderDefinitionList == null) {
            return null;
        }

        Collection<BuilderDefinition> filtered = new ArrayList<>();
        for (BuilderDefinition def : builderDefinitionList) {
            if (UserviewService.TYPE.equals(def.getType()) == userviewPageOnly) {
                filtered.add(def);
            }
        }
        return filtered;
    }

    /**
     * Locates the given menu within its owning userview's JSON and returns
     * the {@code categories[c].menus[m]} dot-path leading to it (recursing
     * into nested {@code menus} for sub-menus), for use as the prefix of a
     * userview builder {@code overview_path} link. Returns {@code null} if
     * the userview or menu can't be found, so callers can fall back to a
     * plain menu-select link instead.
     */
    private static String findMenuOverviewPathPrefix(AppDefinition appDef, String userviewId, String menuId) {
        if (appDef.getUserviewDefinitionList() == null) {
            return null;
        }

        for (AbstractAppVersionedObject userviewDef : appDef.getUserviewDefinitionList()) {
            if (!userviewId.equals(userviewDef.getId())) {
                continue;
            }

            try {
                JSONArray categories = new JSONObject(userviewDef.getJson()).getJSONArray("categories");
                for (int c = 0; c < categories.length(); c++) {
                    String path = findMenuPathInMenus(categories.getJSONObject(c).getJSONArray("menus"), menuId, "categories[" + c + "].menus");
                    if (path != null) {
                        return path;
                    }
                }
            } catch (JSONException e) {
                LogUtil.error(DependenciesUtil.class.getName(), e, "");
            }
            break;
        }
        return null;
    }

    private static String findMenuPathInMenus(JSONArray menus, String menuId, String prefix) throws JSONException {
        for (int m = 0; m < menus.length(); m++) {
            JSONObject menuObj = menus.getJSONObject(m);
            String path = prefix + "[" + m + "]";

            JSONObject properties = menuObj.optJSONObject("properties");
            if (properties != null && menuId.equals(properties.optString("id"))) {
                return path;
            }
            if (menuObj.has("menus")) {
                String nested = findMenuPathInMenus(menuObj.getJSONArray("menus"), menuId, path + ".menus");
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    /**
     * Finds the deepest addressable page element (in document order) whose
     * subtree references the dependency keyword, and returns its
     * {@code elements[..].elements[..]} dot-path for use as the suffix of a
     * userview builder {@code overview_path} link. Returns {@code null} if
     * the page JSON can't be parsed or no addressable element accounts for
     * the match, so callers can fall back to a generic {@code elements[0]}.
     */
    private static String findElementOverviewPathSuffix(String pageJson, String type, String keyword, Pattern hashVariablePattern) {
        try {
            return findElementOverviewPathSuffix(new JSONObject(pageJson), "elements", type, keyword, hashVariablePattern);
        } catch (JSONException e) {
            LogUtil.error(DependenciesUtil.class.getName(), e, "");
            return null;
        }
    }

    private static String findElementOverviewPathSuffix(JSONObject node, String prefix, String type, String keyword, Pattern hashVariablePattern) throws JSONException {
        if (!node.has("elements")) {
            return null;
        }

        JSONArray elements = node.getJSONArray("elements");
        for (int i = 0; i < elements.length(); i++) {
            JSONObject child = elements.getJSONObject(i);

            // Sub-elements without their own id (e.g. ColumnComponent, whose
            // JSON properties are always {}) render without a [data-cbuilder-id]
            // attribute. The userview builder's path-to-selector logic falls
            // back to a positional [data-cbuilder-classname=...]:eq(n) match for
            // those, which is a jQuery/Sizzle footgun mid-selector and reliably
            // fails to match once there's more than one such hop in the chain
            // (confirmed live: selector built, 0 DOM matches). So this walk
            // never targets or descends through an id-less element - the
            // closest addressable ancestor is reported instead.
            if (!hasSelectableId(child)) {
                continue;
            }

            String childPath = prefix + "[" + i + "]";
            String nested = findElementOverviewPathSuffix(child, childPath + ".elements", type, keyword, hashVariablePattern);
            if (nested != null) {
                return nested;
            }

            if (containsDependencyUsage(child.toString(), type, keyword, hashVariablePattern)) {
                return childPath;
            }
        }
        return null;
    }

    private static boolean hasSelectableId(JSONObject element) {
        JSONObject properties = element.optJSONObject("properties");
        return properties != null && !properties.optString("id").isEmpty();
    }

    public static boolean containsQuoted(String text, String keyword) {
        return text.contains("\"" + keyword + "\"");
    }

    public static boolean containsBeanshellEscaped(String text, String keyword) {
        return text.contains("\"" + keyword + "\\\"");
    }

    public static boolean containsAppFdTable(String text, String keyword) {
        return text.contains("app_fd_" + keyword);
    }

    public static boolean containsFormHashKeyword(String text, String keyword) {
        return text.contains("form." + keyword + ".");
    }

    /**
     * Builds the pattern that matches hash variables such as
     * {@code #form.table.field#} and escaped or parameterized variants such
     * as {@code #form.table?sql#}. Compiled once per {@code getDependencies}
     * call (the keyword doesn't change across the scanned objects) and passed
     * down instead of being recompiled per object, or cached indefinitely
     * keyed by an unbounded, request-supplied keyword.
     */
    private static Pattern buildHashVariablePattern(String keyword) {
        String regex = ".*#[^#]+\\." + StringUtil.escapeRegex(keyword) + "([\\}?\\.\\[]+[^#]*)*#.*";
        return Pattern.compile(regex);
    }

    private static void findKeywords(JSONArray keywords, String text, String keyword) {
        int found = text.indexOf(keyword);

        while (found > 0) {
            int start = (found - 40 < 0) ? 0 : found - 40;
            int end = ((found + keyword.length() + 40) >= text.length()) ? text.length() : (found + keyword.length() + 40);

            String words = text.substring(start, end);
            if (start != 0) {
                words = "..." + words;
            }
            if (end != text.length()) {
                words = words + "...";
            }
            keywords.put(words);

            text = text.substring(found + keyword.length());
            found = text.indexOf(keyword);
        }
    }

    private static void findHashVariableKeywords(JSONArray keywords, String text, String keyword, Pattern pattern) {
        int found = text.indexOf(keyword);

        while (found > 0) {
            int start = (found - 40 < 0) ? 0 : found - 40;
            int end = ((found + keyword.length() + 40) >= text.length()) ? text.length() : (found + keyword.length() + 40);

            String words = text.substring(start, end);
            if (pattern.matcher(words).matches()) {
                if (start != 0) {
                    words = "..." + words;
                }
                if (end != text.length()) {
                    words = words + "...";
                }
                keywords.put(words);
            }

            text = text.substring(found + keyword.length());
            found = text.indexOf(keyword);
        }
    }

    public static JSONArray getDependencies(String appId, String version, String type, String keyword, HttpServletRequest request) {
        keyword = SecurityUtil.validateStringInput(keyword);

        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");

        Long appVersion;
        if (version == null || version.isEmpty()) {
            appVersion = appService.getPublishedVersion(appId);
        } else {
            appVersion = Long.parseLong(version);
        }
        String appVersionString = appVersion.toString();
        AppDefinition appDef = appService.getAppDefinition(appId, appVersionString);
        return getDependencies(appDef, appId, appVersionString, type, keyword, request);
    }

    /**
     * Finds app definition usages without exporting the full app XML document.
     */
    static JSONArray getDependencies(AppDefinition appDef, String appId, String version, String type, String keyword, HttpServletRequest request) {
        JSONArray usages = new JSONArray();
        if (appDef == null) {
            return usages;
        }

        Pattern hashVariablePattern = buildHashVariablePattern(keyword);

        for (NamedDefinitionList list : getJsonDefinitionLists(appDef)) {
            addJsonUsages(usages, list.getDefinitions(), list.getNodeType(), appDef, appId, version, type, keyword, hashVariablePattern, request);
        }
        addPluginDefaultPropertiesUsages(usages, appDef.getPluginDefaultPropertiesList(), appId, version, type, keyword, hashVariablePattern, request);
        addPackageDefinitionUsages(usages, appDef.getPackageDefinitionList(), appId, version, type, keyword, hashVariablePattern, request);
        return usages;
    }

    private static void addJsonUsages(JSONArray usages, Collection<? extends AbstractAppVersionedObject> appObjectList, String nodeType, AppDefinition appDef, String appId, String version, String type, String keyword, Pattern hashVariablePattern, HttpServletRequest request) {
        if (appObjectList == null) {
            return;
        }

        for (AbstractAppVersionedObject appObject : appObjectList) {
            String text = appObject.getJson();
            if (!containsDependencyUsage(text, type, keyword, hashVariablePattern)) {
                continue;
            }

            String id = appObject.getId();
            if (nodeType.equals(type) && keyword.equals(id)) {
                continue;
            }

            JSONObject obj = new JSONObject();
            obj.put("where", id);
            obj.put("label", appObject.getName());

            if ("cbuilder".equals(nodeType)) {
                String builderType = ((BuilderDefinition) appObject).getType();
                CustomBuilder cbuilder = CustomBuilderUtil.getBuilder(builderType);
                if (cbuilder == null || (builderType.equals(type) && keyword.equals(id))) {
                    continue;
                }
                obj.put("type", builderType);
                obj.put("category", cbuilder.getObjectLabel());
                obj.put("link", request.getContextPath() + "/web/console/app/" + appId + "/" + version + "/cbuilder/" + builderType + "/design/" + id);
            } else if (NODE_TYPE_USERVIEW_PAGE.equals(nodeType)) {
                String userviewId = ((BuilderDefinition) appObject).getDescription();
                String menuId = id.substring("up-".length());
                obj.put("type", "userview");
                obj.put("category", ResourceBundleUtil.getMessage("dependency.usage.userview.pageComponent"));

                // menuId only selects the menu node in the outer builder tree; overview_path
                // (when resolvable) additionally opens straight into that menu's page-component
                // canvas and selects the referencing element, so keep menuId as a fallback if
                // the menu or element can't be located.
                String link = request.getContextPath() + "/web/console/app/" + appId + "/" + version + "/userview/builder/" + userviewId + "?menuId=" + menuId;
                String menuPath = findMenuOverviewPathPrefix(appDef, userviewId, menuId);
                if (menuPath != null) {
                    String elementPath = findElementOverviewPathSuffix(text, type, keyword, hashVariablePattern);
                    String overviewPath = menuPath + ".referencePage." + (elementPath != null ? elementPath : "elements[0]");
                    // [ and ] are rejected outright by strict HTTP request-target parsing
                    // (Tomcat's RFC 7230/3986 check), so the query value must be encoded.
                    link += "&overview_path=" + URLEncoder.encode(overviewPath, StandardCharsets.UTF_8);
                }
                obj.put("link", link);
            } else {
                obj.put("type", nodeType);
                obj.put("category", ResourceBundleUtil.getMessage("dependency.usage." + nodeType));
                obj.put("link", request.getContextPath() + "/web/console/app/" + appId + "/" + version + "/" + nodeType + "/builder/" + id);
            }

            obj.put("found", getFoundKeywords(text, type, keyword, hashVariablePattern));
            usages.put(obj);
        }
    }

    private static void addPluginDefaultPropertiesUsages(JSONArray usages, Collection<PluginDefaultProperties> pluginDefaultPropertiesList, String appId, String version, String type, String keyword, Pattern hashVariablePattern, HttpServletRequest request) {
        if (pluginDefaultPropertiesList == null) {
            return;
        }

        for (PluginDefaultProperties pluginDefaultProperties : pluginDefaultPropertiesList) {
            String text = pluginDefaultProperties.getPluginProperties();
            if (!containsDependencyUsage(text, type, keyword, hashVariablePattern)) {
                continue;
            }

            String className = pluginDefaultProperties.getId();
            JSONObject obj = new JSONObject();
            obj.put("where", className);
            obj.put("label", pluginDefaultProperties.getPluginName());
            obj.put("type", "plugin_default_properties");
            obj.put("category", ResourceBundleUtil.getMessage("dependency.usage.pluginDefault"));
            obj.put("link", request.getContextPath() + "/web/console/app/" + appId + "/" + version + "/builders?view=pluginDefaultProperties&plugin=" + className);
            obj.put("found", getFoundKeywords(text, type, keyword, hashVariablePattern));
            usages.put(obj);
        }
    }

    private static void addPackageDefinitionUsages(JSONArray usages, Collection<PackageDefinition> packageDefinitionList, String appId, String version, String type, String keyword, Pattern hashVariablePattern, HttpServletRequest request) {
        if (packageDefinitionList == null) {
            return;
        }

        for (PackageDefinition packageDef : packageDefinitionList) {
            if ("form".equals(type) && packageDef.getPackageActivityFormMap() != null) {
                for (Map.Entry<String, PackageActivityForm> entry : packageDef.getPackageActivityFormMap().entrySet()) {
                    addPackageActivityFormUsage(usages, entry.getKey(), entry.getValue(), appId, version, keyword, request);
                }
            }
            if (packageDef.getPackageActivityPluginMap() != null) {
                for (Map.Entry<String, PackageActivityPlugin> entry : packageDef.getPackageActivityPluginMap().entrySet()) {
                    addPackagePluginPropertiesUsage(usages, entry.getKey(), entry.getValue().getPluginProperties(), "process_tool", ResourceBundleUtil.getMessage("dependency.usage.tools"), appId, version, type, keyword, hashVariablePattern, request);
                }
            }
            if (packageDef.getPackageParticipantMap() != null) {
                for (Map.Entry<String, PackageParticipant> entry : packageDef.getPackageParticipantMap().entrySet()) {
                    addPackagePluginPropertiesUsage(usages, entry.getKey(), entry.getValue().getPluginProperties(), "process_participant", ResourceBundleUtil.getMessage("dependency.usage.participants"), appId, version, type, keyword, hashVariablePattern, request);
                }
            }
        }
    }

    private static void addPackageActivityFormUsage(JSONArray usages, String where, PackageActivityForm activityForm, String appId, String version, String keyword, HttpServletRequest request) {
        if (activityForm == null || !keyword.equals(StringUtils.trim(activityForm.getFormId()))) {
            return;
        }

        String[] ids = where.split("::");
        if (ids.length < 2) {
            return;
        }

        JSONObject obj = new JSONObject();
        obj.put("where", where);
        obj.put("label", where);
        obj.put("type", "process_activity");
        obj.put("category", ResourceBundleUtil.getMessage("dependency.usage.activities"));
        obj.put("link", request.getContextPath() + "/web/console/app/" + appId + "/" + version + "/process/builder#" + ids[0] + "?view=listViewer&id=" + ids[1]);
        usages.put(obj);
    }

    private static void addPackagePluginPropertiesUsage(JSONArray usages, String where, String text, String usageType, String category, String appId, String version, String type, String keyword, Pattern hashVariablePattern, HttpServletRequest request) {
        if (!containsDependencyUsage(text, type, keyword, hashVariablePattern)) {
            return;
        }

        String[] ids = where.split("::");
        if (ids.length < 2) {
            return;
        }

        JSONObject obj = new JSONObject();
        obj.put("where", where);
        obj.put("label", where);
        obj.put("type", usageType);
        obj.put("category", category);
        obj.put("link", request.getContextPath() + "/web/console/app/" + appId + "/" + version + "/processes/" + ids[0] + "?view=listViewer&id=" + ids[1]);
        obj.put("found", getFoundKeywords(text, type, keyword, hashVariablePattern));
        usages.put(obj);
    }

    /**
     * Mirrors the previous XPath predicates used to select {@code json} and
     * {@code pluginProperties} nodes that may reference a dependency.
     */
    private static boolean containsDependencyUsage(String text, String type, String keyword, Pattern hashVariablePattern) {
        if (StringUtils.isEmpty(text) || !text.contains(keyword)) {
            return false;
        }

        return containsQuoted(text, keyword)
                || hashVariablePattern.matcher(text).matches()
                || (("form".equals(type) || "table".equals(type)) && containsBeanshellEscaped(text, keyword))
                || ("table".equals(type) && containsAppFdTable(text, keyword))
                || ("table".equals(type) && containsFormHashKeyword(text, keyword));
    }

    /**
     * Builds the text snippets shown in the dependency usage result.
     */
    private static JSONArray getFoundKeywords(String text, String type, String keyword, Pattern hashVariablePattern) {
        text = text.replaceAll("    ", "");

        JSONArray foundArr = new JSONArray();
        findKeywords(foundArr, text, "\"" + keyword + "\"");
        findHashVariableKeywords(foundArr, text, keyword, hashVariablePattern);

        if ("form".equals(type)) {
            findKeywords(foundArr, text, "\\\"" + keyword + "\\\"");
        }

        if ("table".equals(type)) {
            findKeywords(foundArr, text, "\\\"" + keyword + "\\\"");
            findKeywords(foundArr, text, "form." + keyword + ".");
            findKeywords(foundArr, text, "app_fd_" + keyword + "");
        }

        return foundArr;
    }

}
