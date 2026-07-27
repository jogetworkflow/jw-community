package org.joget.apps.app.service;

import jakarta.servlet.http.HttpServletRequest;
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
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class DependenciesUtil {

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
        lists.add(new NamedDefinitionList("cbuilder", appDef.getBuilderDefinitionList()));
        return lists;
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
            addJsonUsages(usages, list.getDefinitions(), list.getNodeType(), appId, version, type, keyword, hashVariablePattern, request);
        }
        addPluginDefaultPropertiesUsages(usages, appDef.getPluginDefaultPropertiesList(), appId, version, type, keyword, hashVariablePattern, request);
        addPackageDefinitionUsages(usages, appDef.getPackageDefinitionList(), appId, version, type, keyword, hashVariablePattern, request);
        return usages;
    }

    private static void addJsonUsages(JSONArray usages, Collection<? extends AbstractAppVersionedObject> appObjectList, String nodeType, String appId, String version, String type, String keyword, Pattern hashVariablePattern, HttpServletRequest request) {
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
