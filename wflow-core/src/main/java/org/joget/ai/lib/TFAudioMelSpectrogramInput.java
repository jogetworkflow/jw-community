package org.joget.ai.lib;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.util.PitchConverter;
import be.tarsos.dsp.util.fft.FFT;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import org.joget.ai.TensorFlowInput;
import org.joget.ai.TensorFlowUtil;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppResourceUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FileUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.tensorflow.Tensor;

public class TFAudioMelSpectrogramInput implements TensorFlowInput {
    
    protected int bufferSize = 1024 * 4;
    protected int overlap = 768 * 4 ;
    protected int outputFrameWidth = 640*4;
    protected int outputFrameHeight = 480*4;
    protected double minFrequency = 50; // Hz
    protected double maxFrequency = 11000; // Hz

    protected BufferedImage bufferedImage = null;
    int position = 0;
    
    protected int frequencyToBin(final double frequency) {
        int bin = 0;
        final boolean logaritmic = true;
        if (frequency != 0 && frequency > minFrequency && frequency < maxFrequency) {
            double binEstimate = 0;
            if (logaritmic) {
                final double minCent = PitchConverter.hertzToAbsoluteCent(minFrequency);
                final double maxCent = PitchConverter.hertzToAbsoluteCent(maxFrequency);
                final double absCent = PitchConverter.hertzToAbsoluteCent(frequency * 2);
                binEstimate = (absCent - minCent) / maxCent * outputFrameHeight;
            } else {
                binEstimate = (frequency - minFrequency) / maxFrequency * outputFrameHeight;
            }
            bin = outputFrameHeight - 1 - (int) binEstimate;
        }
        return bin;
    }

    protected void drawFFT(float[] amplitudes, FFT fft, BufferedImage bufferedImage) {
        if(position >= outputFrameWidth){
            return;
        }
        
        Graphics2D bufferedGraphics = bufferedImage.createGraphics();

        double maxAmplitude=0;
        //for every pixel calculate an amplitude
        float[] pixelAmplitudes = new float[outputFrameHeight];
        //iterate the large array and map to pixels
        for (int i = amplitudes.length/800; i < amplitudes.length; i++) {
            int pixelY = frequencyToBin(i * 44100 / (amplitudes.length * 8));
            pixelAmplitudes[pixelY] += amplitudes[i];
            maxAmplitude = Math.max(pixelAmplitudes[pixelY], maxAmplitude);
        }

        //draw the pixels
        for (int i = 0; i < pixelAmplitudes.length; i++) {
            Color color = Color.black;
            if (maxAmplitude != 0) {

                final int greyValue = (int) (Math.log1p(pixelAmplitudes[i] / maxAmplitude) / Math.log1p(1.0000001) * 255);
                color = new Color(greyValue, greyValue, greyValue);
            }
            bufferedGraphics.setColor(color);
            bufferedGraphics.fillRect(position, i, 3, 1);
        }

        position+=3;
        position = position % outputFrameWidth;
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
        if (!form.isEmpty()) {
            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
            String recordId = appService.getOriginProcessId(processId);
            
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

            AudioDispatcher dispatcher = null;

            if (form != null && !form.isEmpty()) {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                File file = FileUtil.getFile(filename, appService.getFormTableName(appDef, form), processId);
                dispatcher = AudioDispatcherFactory.fromFile(file, bufferSize, overlap);
            } else if (TensorFlowUtil.isValidURL(filename)) {
                dispatcher = AudioDispatcherFactory.fromURL(new URL(filename), bufferSize, overlap);
            } else {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                File file = AppResourceUtil.getFile(appDef.getAppId(), appDef.getVersion().toString(), filename);
                dispatcher = AudioDispatcherFactory.fromFile(file, bufferSize, overlap);
            }
            
            outputFrameWidth = Integer.parseInt(AppPluginUtil.getVariable(params.get("width").toString(), variables));
            outputFrameHeight = Integer.parseInt(AppPluginUtil.getVariable(params.get("height").toString(), variables));
            
            bufferedImage = new BufferedImage(outputFrameWidth,outputFrameHeight, BufferedImage.TYPE_INT_RGB);
        
            dispatcher.addAudioProcessor(new AudioProcessor(){
                FFT fft = new FFT(bufferSize);
                float[] amplitudes = new float[bufferSize];
                
                @Override
                public void processingFinished() {
                    // TODO Auto-generated method stub
                }
                
                @Override
                public boolean process(AudioEvent audioEvent) {
                    float[] audioFloatBuffer = audioEvent.getFloatBuffer();
                    float[] transformBuffer = new float[bufferSize * 2];
                    System.arraycopy(audioFloatBuffer, 0, transformBuffer, 0, audioFloatBuffer.length);
                    fft.forwardTransform(transformBuffer);
                    fft.modulus(transformBuffer, amplitudes);
                    drawFFT(amplitudes,fft, bufferedImage);
                    return true;
                }
            });
            
            position = 0;
            
            dispatcher.run();
            
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