package org.joget.apps.app.lib;

import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.model.AppOverviewData;
import org.joget.apps.app.model.AppOverviewToolAbstract;
import org.json.JSONObject;

public class BeanShellOverviewTool extends AppOverviewToolAbstract {
    
    @Override
    public String getName() {
        return "BeanShellOverviewTool";
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
        return "BeanShell Script";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public void scan(String key, String path, String pluginClassName, JSONObject properties, JSONObject parentObject, AppOverviewData data) {
        boolean isBeanshellPlugin = false;
        boolean hasBeanshellHash = false;
        
        if (pluginClassName != null && pluginClassName.toLowerCase().contains("beanshell")) {
            isBeanshellPlugin = true;
        } else {
            hasBeanshellHash = properties.toString().contains("beanshell.");
        }
        
        //find beanshell script or hash variable
        if (isBeanshellPlugin || hasBeanshellHash) {
            String pathPrefix = path;
            if (!pathPrefix.isEmpty()) {
                pathPrefix += ".";
            }
            
            //if it is plugin properties
            if (pluginClassName != null && !pluginClassName.isEmpty()) {
                pathPrefix += "propertise.";
            }
            
            Iterator keys = properties.keys();
            while (keys.hasNext()) {
                String pkey = (String) keys.next();
                if (!properties.isNull(pkey)) {
                    Object value = properties.get(pkey);
                    if (pkey.toLowerCase().endsWith("script") && !value.toString().isEmpty()) {
                        data.addItemData(key, this, pathPrefix + pkey, "", value.toString());
                    } else {
                        String valueStr = value.toString();
                        if (valueStr.contains("beanshell.")) {
                            //find all beanshell hash variables
                            int index = valueStr.indexOf("beanshell.");
                            while (index > 1) {
                                char startChar = valueStr.charAt(index-1);
                                String hashVariable = "";
                                
                                if (startChar == '{') {
                                    //if it is nested hash variable, find closing
                                    hashVariable = valueStr.substring(index-1, valueStr.indexOf("}", index + 10) + 1);
                                    int count = StringUtils.countMatches(hashVariable, "{");
                                    if (count > 1) {
                                        int nextCharIndex = index + hashVariable.length();
                                        while (count > 1 && nextCharIndex < valueStr.length()) {
                                            char nextChar = valueStr.charAt(nextCharIndex);
                                            if (nextChar == '}') {
                                                count--;
                                            } else if (nextChar == '{') {
                                                count++;
                                            }
                                            hashVariable += nextChar;
                                            nextCharIndex++;
                                        }
                                    }
                                } else {
                                    hashVariable = valueStr.substring(index-1, valueStr.indexOf("#", index + 10) + 1);
                                }
                                
                                if (!hashVariable.isEmpty()) {
                                    data.addItemData(key, this, pathPrefix + pkey, "", hashVariable);
                                    
                                    //find next hash variable
                                    index = valueStr.indexOf("beanshell.", index + hashVariable.length() - 1);
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getIcon() {
        return "<i class=\"las la-code\" aria-hidden=\"true\"></i>";
    }
}
