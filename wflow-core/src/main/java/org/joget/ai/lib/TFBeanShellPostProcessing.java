package org.joget.ai.lib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.joget.ai.TensorFlowPostProcessing;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.commons.util.ResourceBundleUtil;

public class TFBeanShellPostProcessing implements TensorFlowPostProcessing {

    @Override
    public void runPostProcessing(Map params, Map<String, Object> tfVariables, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        String script = params.get("script").toString();
        String name = params.get("name").toString();
        if (!script.isEmpty()) {
            Map properties = new HashMap();
            properties.put("name", name);
            properties.put("tfVariables", tfVariables);
            properties.put("variables", variables);
            properties.put("tempDataHolder", tempDataHolder);

            AppPluginUtil.executeScript(script, properties);
        }
    }

    @Override
    public String getName() {
        return "beanshell";
    }

    @Override
    public String getLabel() {
        return ResourceBundleUtil.getMessage("app.simpletfai.beanshell");
    }

    @Override
    public String getDescription() {
        return ResourceBundleUtil.getMessage("app.simpletfai.beanshell.post");
    }

    @Override
    public String getUI() {
        String html = "<div><textarea name=\"script\" class=\"input_script\" style=\"display:none;\"></textarea><pre class=\"ace_editor\"></pre></div>";
        return html;
    }

    @Override
    public String getInitScript() {
        String script = "var aceField = ace.edit($(row).find(\".ace_editor\")[0]);";
        script += "var textarea = $(row).find(\".input_script\");";
        script += "setTimeout(function(){aceField.getSession().setValue(textarea.val());}, 2000);";
        script += "aceField.getSession().setTabSize(4);";
        script += "aceField.setTheme(\"ace/theme/textmate\");";
        script += "aceField.getSession().setMode(\"ace/mode/java\");";
        script += "aceField.setAutoScrollEditorIntoView(true);";
        script += "aceField.setOption(\"maxLines\", 1000000);";
        script += "aceField.setOption(\"minLines\", 5);";
        script += "aceField.resize();";
        script += "aceField.getSession().on('change', function(){";
        script += "    textarea.val(aceField.getSession().getValue());";
        script += "});";
        
        return script;
    }
    
}