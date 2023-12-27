package org.joget.apps.app.lib;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joget.apps.app.model.AppOverviewData;
import org.joget.apps.app.model.AppOverviewToolAbstract;
import org.joget.apps.app.model.HashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.json.JSONObject;

public class HashVariableOverviewTool extends AppOverviewToolAbstract {
    protected Set<String> hashPrefix;
    
    @Override
    public String getName() {
        return "HashVariableOverviewTool";
    }

    @Override
    public String getVersion() {
        return "9.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getLabel() {
        return "Hash Variable";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public void scan(String key, String path, String pluginClassName, JSONObject properties, JSONObject parentObject, AppOverviewData data) {
        if (AppUtil.containsHashVariable(properties.toString())) {
            String pathPrefix = path;
            if (!pathPrefix.isEmpty()) {
                pathPrefix += ".";
            }
            
            //if it is plugin properties
            if (pluginClassName != null && !pluginClassName.isEmpty()) {
                pathPrefix += "propertise.";
            }
            
            Pattern pattern = Pattern.compile("\\#([^#\" ])*\\.([^#\"])*\\#");
            
            Iterator keys = properties.keys();
            while (keys.hasNext()) {
                String pkey = (String) keys.next();
                if (!properties.isNull(pkey)) {
                    String valueStr = properties.get(pkey).toString();
                    if (!valueStr.contains("\"className\"") && AppUtil.containsHashVariable(valueStr)) { //skip plugin
                        Matcher matcher = pattern.matcher(valueStr);
                        while (matcher.find()) {
                            String hash = matcher.group();
                            String prefix = hash.substring(1, hash.indexOf("."));
                            if (getHashPrefix().contains(prefix)) { //check is hash variable
                                data.addItemData(key, this, pathPrefix + pkey, hash, valueStr);
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected Set<String> getHashPrefix() {
        if (hashPrefix == null) {
            hashPrefix = new HashSet<String>();
                    
            // get osgi plugins
            PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
            Collection<Plugin> pluginList = pluginManager.list(HashVariablePlugin.class);
            
            for (Plugin plugin: pluginList) {
                hashPrefix.add(((HashVariablePlugin) plugin).getPrefix());
            }
        }
        return hashPrefix;
    }

    @Override
    public String getIcon() {
        return "<i class=\"las la-hashtag\" aria-hidden=\"true\"></i>";
    }
}