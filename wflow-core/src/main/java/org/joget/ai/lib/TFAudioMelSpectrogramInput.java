package org.joget.ai.lib;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.joget.ai.TensorFlowInput;
import org.joget.ai.TensorFlowUtil;
import org.joget.ai.audio.Audio;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppResourceUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FileUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SetupManager;
import org.tensorflow.Tensor;

public class TFAudioMelSpectrogramInput implements TensorFlowInput {
    
    protected int outputFrameWidth;
    protected int outputFrameHeight;
    
    protected BufferedImage bufferedImage = null;
    
    protected AudioInputStream getInputStream(String form, String filename, String recordId) {
        try {
            if (form != null && !form.isEmpty()) {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                File file = FileUtil.getFile(filename, appService.getFormTableName(appDef, form), recordId);
                return AudioSystem.getAudioInputStream(file);
            } else if (TensorFlowUtil.isValidURL(filename)) {
                return AudioSystem.getAudioInputStream(new URL(filename));
            } else {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                File file = AppResourceUtil.getFile(appDef.getAppId(), appDef.getVersion().toString(), filename);
                return AudioSystem.getAudioInputStream(file);
            }
        } catch (Exception e) {
            LogUtil.error(TFAudioMelSpectrogramInput.class.getName(), e, "");
        }
        return null;
    }
    
    protected Tensor getTensor() {
        final int channels = 1;
        int index = 0;
        FloatBuffer fb = FloatBuffer.allocate(outputFrameWidth * outputFrameHeight * channels);

        for (int row = 0; row < outputFrameHeight; row++) {
            for (int column = 0; column < outputFrameWidth; column++) {
                int pixel = bufferedImage.getRGB(column, row);

                float red = (pixel >> 16) & 0xff;
                fb.put(index++, red);
            }
        }

        return Tensor.create(new long[]{1, outputFrameHeight, outputFrameWidth, channels}, fb);
    }

    @Override
    public Tensor getInputs(Map params, String processId, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        String filename = AppPluginUtil.getVariable(params.get("audio").toString(), variables);
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
        
        try {
            outputFrameWidth = Integer.parseInt(AppPluginUtil.getVariable(params.get("width").toString(), variables));
            outputFrameHeight = Integer.parseInt(AppPluginUtil.getVariable(params.get("height").toString(), variables));
            int fftSize = Integer.parseInt(AppPluginUtil.getVariable(params.get("fftSize").toString(), variables));
            int overlapSize = Integer.parseInt(AppPluginUtil.getVariable(params.get("overlapSize").toString(), variables));
            int minFrequency = Integer.parseInt(AppPluginUtil.getVariable(params.get("minFrequency").toString(), variables));
            int maxFrequency = Integer.parseInt(AppPluginUtil.getVariable(params.get("maxFrequency").toString(), variables));
            
            AudioInputStream ais = getInputStream(form, filename, recordId);
            Audio audio = new Audio(ais);
            
            bufferedImage = audio.getSpectrogramImage(outputFrameWidth, outputFrameHeight, fftSize, overlapSize, minFrequency, maxFrequency);
            ImageIO.write(bufferedImage, "jpg", new File(SetupManager.getBaseDirectory() + "/image-" + filename + "-2.jpg"));

            return getTensor();
        } catch (Exception e) {
            LogUtil.error(TFAudioMelSpectrogramInput.class.getName(), e, "");
        }
        
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
        String label = ResourceBundleUtil.getMessage("app.simpletfai.urlorAudioFilefieldid");
        String heightLabel = ResourceBundleUtil.getMessage("app.simpletfai.height");
        String widthLabel = ResourceBundleUtil.getMessage("app.simpletfai.width");
        String fftSizeLabel = ResourceBundleUtil.getMessage("app.simpletfai.fftSize");
        String overlapSizeLabel = ResourceBundleUtil.getMessage("app.simpletfai.overlapSize");
        String minFrequencyLabel = ResourceBundleUtil.getMessage("app.simpletfai.minFrequency");
        String maxFrequencyLabel = ResourceBundleUtil.getMessage("app.simpletfai.maxFrequency");
        String emptyLabel = ResourceBundleUtil.getMessage("app.simpletfai.retrieveFromUrl");
        String sourceLabel = ResourceBundleUtil.getMessage("app.simpletfai.source");
        
        String html = "<div><select name=\"form\" class=\"input_form quarter\"><option value=\"\">"+emptyLabel+"</option></select><span class=\"label\">"+sourceLabel+"</span><input name=\"audio\" class=\"input_audio half required\" placeholder=\""+label+"\"/><span class=\"label\">"+label+"</span></div>";
        html += "<div><input name=\"width\" class=\"input_width small required \" placeholder=\""+widthLabel+"\"/><span class=\"label\">"+widthLabel+"</span>";
        html += "<input name=\"height\" class=\"input_height small required \" placeholder=\""+heightLabel+"\"/><span class=\"label\">"+heightLabel+"</span>";
        html += "<input name=\"fftSize\" class=\"input_fftSize small required \" placeholder=\""+fftSizeLabel+"\"/><span class=\"label\">"+fftSizeLabel+"</span>";
        html += "<input name=\"overlapSize\" class=\"input_overlapSize small required \" placeholder=\""+overlapSizeLabel+"\"/><span class=\"label\">"+overlapSizeLabel+"</span></div>";
        html += "<div><input name=\"minFrequency\" class=\"input_minFrequency small required \" placeholder=\""+minFrequencyLabel+"\"/><span class=\"label\">"+minFrequencyLabel+"</span>";
        html += "<input name=\"maxFrequency\" class=\"input_maxFrequency small required \" placeholder=\""+maxFrequencyLabel+"\"/><span class=\"label\">"+maxFrequencyLabel+"</span></div>";
        
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