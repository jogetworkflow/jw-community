package org.joget.ai.lib;

import java.io.IOException;
import java.util.Map;
import org.joget.ai.TensorFlowInput;
import org.joget.commons.util.ResourceBundleUtil;
import org.tensorflow.Tensor;

public class TFAudioMelSpectrogramInput /*implements TensorFlowInput */{
    
    @Override
    public Tensor getInputs(Map params, String processId, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        
        return null;
    }

    @Override
    public String getName() {
        return "audioMelSpectrogram";
    }
    
    @Override
    public String getLabel() {
        return ResourceBundleUtil.getMessage("app.simpletfai.audioMelSpectrogram");
    }
    
    @Override
    public String getDescription() {
        return ResourceBundleUtil.getMessage("app.simpletfai.audioMelSpectrogram.desc");
    }

    @Override
    public String getUI() {
        String label = ResourceBundleUtil.getMessage("app.simpletfai.urlorfieldid");
        String heightLabel = ResourceBundleUtil.getMessage("app.simpletfai.height");
        String widthLabel = ResourceBundleUtil.getMessage("app.simpletfai.width");
        String emptyLabel = ResourceBundleUtil.getMessage("app.simpletfai.retrieveFromUrl");
        
        String html = "<div><select name=\"form\" class=\"input_form\"><option value=\"\">"+emptyLabel+"</option></select><input name=\"audio\" class=\"input_audio half required\" placeholder=\""+label+"\"/></div>";
        html += "<div><input name=\"height\" class=\"input_height small required\" placeholder=\""+heightLabel+"\"/>";
        html += "<input name=\"width\" class=\"input_width small required\" placeholder=\""+widthLabel+"\"/>";
        
        return html;
    }

    @Override
    public String getInitScript() {
        String script = "$.each(editor.forms, function(i, v){ $(row).find(\".input_form\").append('<option value=\"'+v.value+'\">'+v.label+'</option>'); });\n";
        script += "$(row).find(\".input_audio\").autocomplete({source: [],minLength: 0,open: function() {$(this).autocomplete('widget').css('z-index', 99999);return false;}});\n";
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
        script += "        }\n source.sort(); $(row).find(\".input_audio\").autocomplete(\"option\", \"source\", source);";
        script += "        if (!(value === \"\" && $(row).find(\".input_audio\").val().indexOf(\"http\") === 0) && $(row).find(\".input_audio\").val() !== \"\" && $.inArray($(row).find(\".input_audio\").val(), source) === -1) {";
        script += "            $(row).find(\".input_audio\").val(\"\");";
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