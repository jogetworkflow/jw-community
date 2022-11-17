package org.joget.ai.lib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.joget.ai.TensorFlowInput;
import org.joget.ai.TensorFlowUtil;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.tensorflow.Tensor;

public class TFImageInput implements TensorFlowInput {

    @Override
    public Tensor getInputs(Map params, String processId, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        String filename = AppPluginUtil.getVariable(params.get("image").toString(), variables);
        String form = AppPluginUtil.getVariable(params.get("form").toString(), variables);
        String recordId = null;
        if (!form.isEmpty()) {
            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
            recordId = appService.getOriginProcessId(processId);
            
            Map<String, FormRowSet> formDatas = null;
            if (tempDataHolder.containsKey("FORM_DATAS_CACHE")) {
                formDatas = (Map<String, FormRowSet>) tempDataHolder.get("FORM_DATAS_CACHE");
            } else {
                formDatas = new HashMap<String, FormRowSet>();
                tempDataHolder.put("FORM_DATAS_CACHE", formDatas);
            }
            FormRowSet rows = null;
            if (!formDatas.containsKey(form+"::"+recordId)) {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                rows = appService.loadFormData(appDef.getAppId(), appDef.getVersion().toString(), form, recordId);
                if (rows != null) {
                    formDatas.put(form+"::"+recordId, rows);
                }
            }
            rows = formDatas.get(form+"::"+recordId);
            if (rows != null && !rows.isEmpty()) {
                filename = rows.get(0).getProperty(filename);
            }
        }
        String type = FilenameUtils.getExtension(filename);
        
        Integer height = null;
        Integer width = null;
        Float mean = null;
        Float scale = null;
        
        try {
            height = Integer.parseInt(AppPluginUtil.getVariable(params.get("height").toString(), variables));
            width = Integer.parseInt(AppPluginUtil.getVariable(params.get("width").toString(), variables));
        } catch (Exception e) {}
        
        try {
            mean = Float.parseFloat(AppPluginUtil.getVariable(params.get("mean").toString(), variables));
        } catch (Exception e) {}
        try {
            scale = Float.parseFloat(AppPluginUtil.getVariable(params.get("scale").toString(), variables));
        } catch (Exception e) {}
        
        return TensorFlowUtil.imageInput(TensorFlowUtil.getInputStream(filename, form, recordId), 
                type, height, width, mean, scale, 
                params.get("datatype").toString());
    }

    @Override
    public String getName() {
        return "image";
    }
    
    @Override
    public String getLabel() {
        return ResourceBundleUtil.getMessage("app.simpletfai.image");
    }
    
    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUI() {
        String label = ResourceBundleUtil.getMessage("app.simpletfai.urlorfieldid");
        String heightLabel = ResourceBundleUtil.getMessage("app.simpletfai.height");
        String widthLabel = ResourceBundleUtil.getMessage("app.simpletfai.width");
        String meanLabel = ResourceBundleUtil.getMessage("app.simpletfai.mean");
        String scaleLabel = ResourceBundleUtil.getMessage("app.simpletfai.scale");
        String emptyLabel = ResourceBundleUtil.getMessage("app.simpletfai.retrieveFromUrl");
        String sourceLabel = ResourceBundleUtil.getMessage("app.simpletfai.source");
        String typeLabel = ResourceBundleUtil.getMessage("app.simpletfai.type");
        
        String html = "<select name=\"datatype\" class=\"input_datatype small\"></select><span class=\"label\">"+typeLabel+"</span>";
        html += "<div><select name=\"form\" class=\"input_form quarter\"><option value=\"\">"+emptyLabel+"</option></select><span class=\"label\">"+sourceLabel+"</span><input name=\"image\" class=\"input_image half required\" placeholder=\""+label+"\"/><span class=\"label\">"+label+"</span></div>";
        html += "<div><input name=\"width\" class=\"input_width small\" placeholder=\""+widthLabel+"\"/><span class=\"label\">"+widthLabel+"</span>";
        html += "<input name=\"height\" class=\"input_height small\" placeholder=\""+heightLabel+"\"/><span class=\"label\">"+heightLabel+"</span>";
        html += "<input name=\"mean\" class=\"input_mean small\" placeholder=\""+meanLabel+"\"/><span class=\"label\">"+meanLabel+"</span>";
        html += "<input name=\"scale\" class=\"input_scale small\" placeholder=\""+scaleLabel+"\"/><span class=\"label\">"+scaleLabel+"</span></div>";
        
        return html;
    }

    @Override
    public String getInitScript() {
        String script = "$.each(editor.forms, function(i, v){ $(row).find(\".input_form\").append('<option value=\"'+v.value+'\">'+v.label+'</option>'); });\n";
        script += "$(row).find(\".input_image\").autocomplete({source: [],minLength: 0,open: function() {$(this).autocomplete('widget').css('z-index', 99999);return false;}});\n";
        script += "if (editor.updateSource === undefined) {";
        script += "    editor.fieldOptions = {};";
        script += "    editor.updateSource = function(value, row) {";
        script += "        var thisObj = this;var source = [];\n";
        script += "        if (thisObj.fieldOptions[value] !== undefined) {";
        script += "             $.each(thisObj.fieldOptions[value], function(i, option) {";
        script += "                 if (option['value'] !== \"\" && $.inArray(option['value'], source) === -1) {";
        script += "                     source.push(option['value']);";
        script += "                 }";
        script += "             });";
        script += "        }\n source.sort(); $(row).find(\".input_image\").autocomplete(\"option\", \"source\", source);";
        script += "        if (!(value === \"\" && $(row).find(\".input_image\").val().indexOf(\"http\") === 0) && $(row).find(\".input_image\").val() !== \"\" && $.inArray($(row).find(\".input_image\").val(), source) === -1) {";
        script += "            $(row).find(\".input_image\").val(\"\");";
        script += "        }\n";
        script += "    }\n";
        script += "}\n";
        script += "$(row).find(\".input_form\").on(\"change\", function(){";
        script += "    var value = $(this).val();";
        script += "    if (value === \"\" || editor.fieldOptions[value] !== undefined) {";
        script += "        editor.updateSource(value, $(row));";
        script += "    } else {";
        script += "        $.ajax({";
        script += "            url: editor.options.contextPath + '/web/json/console/app' + editor.options.appPath + '/form/columns/options?formDefId=' + escape(value),";
        script += "            dataType: \"text\",";
        script += "            method: \"GET\",";
        script += "            success: function(data) {";
        script += "                if (data !== undefined && data !== null) {";
        script += "                    var options = $.parseJSON(data);";
        script += "                    editor.fieldOptions[value] = options;";
        script += "                    editor.updateSource(value, $(row));";
        script += "                }";
        script += "            }";
        script += "        });";
        script += "    }});";
        script += "setTimeout(function(){$(row).find(\".input_form\").trigger(\"change\");}, 1000);";
        
        return script;
    }
    
}
