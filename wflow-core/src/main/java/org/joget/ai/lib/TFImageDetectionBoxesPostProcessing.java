package org.joget.ai.lib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.joget.ai.TensorFlowPostProcessing;
import org.joget.ai.TensorFlowUtil;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FileUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;

public class TFImageDetectionBoxesPostProcessing implements TensorFlowPostProcessing {

    @Override
    public void runPostProcessing(Map params, Map<String, Object> tfVariables, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        
        String name = params.get("name").toString();
        String variable = params.get("variable").toString();
        String processId = params.get("processId").toString();
        
        float[] values = (float[]) tfVariables.get(variable);
        String variable2 = params.get("variable2").toString();
        Integer number = null;
        
        if (!variable2.isEmpty()) {
            float[] values2 = (float[]) tfVariables.get(variable2);
            number = (int) values2[0];
        }
        
        Float threshold = null;
        try {
            threshold = Float.parseFloat(AppPluginUtil.getVariable(params.get("threshold").toString(), variables));
        } catch (Exception e) {}
        
        float[] scores = null;
        String variable3 = params.get("variable3").toString();
        if (!variable3.isEmpty()) {
            scores = (float[]) tfVariables.get(variable3);
        }
        
        String variable4 = params.get("variable4").toString();
        float[] boxes = (float[]) tfVariables.get(variable4);
        
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
            
        String filename = AppPluginUtil.getVariable(params.get("image").toString(), variables);
        String form = AppPluginUtil.getVariable(params.get("form").toString(), variables);
        String recordId = null;
        if (!form.isEmpty()) {
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
        String boxType = params.get("boxType").toString();
        if (boxType.isEmpty()) {
            boxType = "xywh";
        }
        
        Float fontSize = 20f;
        try {
            fontSize = Float.parseFloat(AppPluginUtil.getVariable(params.get("fontsize").toString(), variables));
        } catch (Exception e) {}
        
        BufferedImage img = null;
        InputStream labelInputStream = TensorFlowUtil.getInputStream(AppPluginUtil.getVariable(params.get("labels").toString(), variables), null, null);
        InputStream colorInputStream = TensorFlowUtil.getInputStream(AppPluginUtil.getVariable(params.get("colors").toString(), variables), null, null);
        InputStream inputStream = TensorFlowUtil.getInputStream(filename, form, recordId);
        
        String outputFileName = filename.replaceAll(StringUtil.escapeRegex("."+type), StringUtil.escapeRegex("-processed."+type));
        if(params.containsKey("output_name") && !params.get("output_name").toString().isEmpty()){
            outputFileName = params.get("output_name").toString();
        }
        String output_form = AppPluginUtil.getVariable(params.get("output_form").toString(), variables);
        
        try {
            img = ImageIO.read(inputStream);
            Graphics2D g2d = img.createGraphics();
            
            BasicStroke bs = new BasicStroke(2);
            g2d.setStroke(bs);
            
            Font currentFont = g2d.getFont();
            Font newFont = currentFont.deriveFont(fontSize);
            g2d.setFont(newFont);
            
            List<String> labels = IOUtils.readLines(labelInputStream);
            if (number == null) {
                number = values.length;
            }
            List<String> colors = null;
            if (colorInputStream != null) {
                colors = IOUtils.readLines(colorInputStream);
            }
            if (colors == null || colors.size() == 0) {
                colors = new ArrayList<String>();
                colors.add("#FF0000");
            }
            
            for (int i=0; i < number; i++) {
                boolean pass = true;
                if (threshold != null && scores != null) {
                    pass = scores[i] > threshold;
                }
                if (pass) {
                    String label = labels.get((int)(values[i] - 1));
                    if (scores != null) {
                        label += " (" + Math.round(scores[i] * 100) + "%)";
                    }
                    int x = 0;
                    int y = 0;
                    int w = 0;
                    int h = 0;
                    
                    if ("xywh".equals(boxType) || "yxwh".equals(boxType)) {
                        if ("xywh".equals(boxType)) {
                            x = Math.round(boxes[(i * 4)]);
                            y = Math.round(boxes[(i * 4)+1]);
                        } else {
                            y = Math.round(boxes[(i * 4)]);
                            x = Math.round(boxes[(i * 4)+1]);
                        }
                        w = Math.round(boxes[(i * 4)+2]);
                        h = Math.round(boxes[(i * 4)+3]);
                    } else if ("x1y1x2y2".equals(boxType)) {
                        x = Math.round(boxes[(i * 4)]);
                        y = Math.round(boxes[(i * 4)+1]);
                        w = Math.round(boxes[(i * 4)+2]) - x;
                        h = Math.round(boxes[(i * 4)+3]) - y; 
                    } else if("y1x1y2x2".equals(boxType)) {
                        y = Math.round(boxes[(i * 4)]);
                        x = Math.round(boxes[(i * 4)+1]);
                        w = Math.round(boxes[(i * 4)+3]) - x;
                        h = Math.round(boxes[(i * 4)+2]) - y; 
                    }
                    
                    g2d.setColor(Color.decode(colors.get(((int)(values[i] - 1)) % colors.size())));
                    g2d.drawRect(x, y, w, h);
                    
                    g2d.drawString(label, Math.round(x + fontSize), Math.round(y + fontSize));
                }
            }
            g2d.dispose();
            File file = FileUtil.getFile(outputFileName, appService.getFormTableName(appDef, output_form), recordId);
            ImageIO.write(img, type, file);
        } catch (IOException e) {
            LogUtil.error(TFImageDetectionBoxesPostProcessing.class.getName(), e, "");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (labelInputStream != null) {
                labelInputStream.close();
            }
            if (colorInputStream != null) {
                colorInputStream.close();
            }
        }
        
        String outputFileField = AppPluginUtil.getVariable(params.get("output_image").toString(), variables);
        if (!output_form.isEmpty()) {
            FormRowSet rowSet = new FormRowSet();
            FormRow row = new FormRow();
            row.setId(recordId);
            row.put(outputFileField, outputFileName);
            rowSet.add(row);
            appService.storeFormData(appDef.getId(), appDef.getVersion().toString(), output_form, rowSet, null);
        }
        
        tfVariables.put(name, outputFileName);
    }

    @Override
    public String getName() {
        return "imageDetection";
    }

    @Override
    public String getLabel() {
        return ResourceBundleUtil.getMessage("app.simpletfai.imageDetection");
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUI() {
        String emptyLabel = ResourceBundleUtil.getMessage("app.simpletfai.retrieveFromUrl");
        String sourceLabel = ResourceBundleUtil.getMessage("app.simpletfai.source");
        String sourceUploadFieldLabel = ResourceBundleUtil.getMessage("app.simpletfai.urlorfieldid");
        String outputLabel = ResourceBundleUtil.getMessage("app.simpletfai.selectOutputForm");
        String soutputUploadFieldLabel = ResourceBundleUtil.getMessage("app.simpletfai.outputUploadField");
        String labelsFileLabel = ResourceBundleUtil.getMessage("app.simpletfai.labels_file");
        String chooseFileLabel = ResourceBundleUtil.getMessage("peditor.chooseFile");
        String clearFileLabel = ResourceBundleUtil.getMessage("peditor.clear");
        String variableNameLabel = ResourceBundleUtil.getMessage("app.simpletfai.detectedClasses");
        String numberLabel = ResourceBundleUtil.getMessage("app.simpletfai.numberOfValues");
        String scoreLabel = ResourceBundleUtil.getMessage("app.simpletfai.score");
        String thresholdLabel = ResourceBundleUtil.getMessage("app.simpletfai.scoreThreshold");
        String boxesLabel = ResourceBundleUtil.getMessage("app.simpletfai.boxes");
        String boxesTypeLabel = ResourceBundleUtil.getMessage("app.simpletfai.boxesType");
        String fontSizeLabel = ResourceBundleUtil.getMessage("app.simpletfai.fontSize");
        String colorsFileLabel = ResourceBundleUtil.getMessage("app.simpletfai.colors_file");
        String outputFileNameLabel = ResourceBundleUtil.getMessage("app.simpletfai.outputFileNameLabel");
        
        String html = "<input name=\"fontsize\" class=\"post_fontsize small required\" value=\"20\" placeholder=\""+fontSizeLabel+"\"/><span class=\"label\">"+fontSizeLabel+"</span>";
        html += "<div><select name=\"form\" class=\"post_form quarter\"><option value=\"\">"+emptyLabel+"</option></select><span class=\"label\">"+sourceLabel+"</span><input name=\"image\" class=\"post_image half required\" placeholder=\""+sourceUploadFieldLabel+"\"/><span class=\"label\">"+sourceUploadFieldLabel+"</span></div>";
        html += "<div><select name=\"output_form\" class=\"post_output_form quarter required\"><option value=\"\"></option></select><span class=\"label\">"+outputLabel+"</span><input name=\"output_image\" class=\"post_output_image half required\" placeholder=\""+soutputUploadFieldLabel+"\"/><span class=\"label\">"+soutputUploadFieldLabel+"</span></div>";
        html += "<div><input name=\"output_name\" class=\"post_output_name full\" placeholder=\""+outputFileNameLabel+"\"/><span class=\"label\">"+outputFileNameLabel+"</span></div>";
        html += "<div><input name=\"labels\" class=\"post_labels half required\" placeholder=\""+labelsFileLabel+"\"/><span class=\"label\">"+labelsFileLabel+"</span> <a class=\"choosefile btn button small\">"+chooseFileLabel+"</a> <a class=\"clearfile btn button small\">"+clearFileLabel+"</a></div>";
        html += "<div><input name=\"colors\" class=\"post_colors half\" placeholder=\""+colorsFileLabel+"\"/><span class=\"label\">"+colorsFileLabel+"</span> <a class=\"choosefile btn button small\">"+chooseFileLabel+"</a> <a class=\"clearfile btn button small\">"+clearFileLabel+"</a></div>";
        html += "<div><select name=\"variable\" class=\"post_variable half required\"><option value=\"\">"+variableNameLabel+"</option></select><span class=\"label\">"+variableNameLabel+"</span><select name=\"variable2\" class=\"post_variable half\"><option value=\"\">"+numberLabel+"</option></select><span class=\"label\">"+numberLabel+"</span></div>";
        html += "<div><select name=\"variable3\" class=\"post_variable half\"><option value=\"\">"+scoreLabel+"</option></select><span class=\"label\">"+scoreLabel+"</span><input name=\"threshold\" class=\"post_threshold half\" placeholder=\""+thresholdLabel+"\"/><span class=\"label\">"+thresholdLabel+"</span></div>";
        
        String options = "<option value=\"\">"+boxesTypeLabel+"</option>";
        options += "<option value=\"xywh\" selected>"+ResourceBundleUtil.getMessage("app.simpletfai.boxesType.xywh")+"</option>";
        options += "<option value=\"yxwh\">"+ResourceBundleUtil.getMessage("app.simpletfai.boxesType.yxwh")+"</option>";
        options += "<option value=\"x1y1x2y2\">"+ResourceBundleUtil.getMessage("app.simpletfai.boxesType.x1y1x2y2")+"</option>";
        options += "<option value=\"y1x1y2x2\">"+ResourceBundleUtil.getMessage("app.simpletfai.boxesType.y1x1y2x2")+"</option>";
        
        html += "<div><select name=\"variable4\" class=\"post_variable half required\"><option value=\"\">"+boxesLabel+"</option></select><span class=\"label\">"+boxesLabel+"</span><select name=\"boxType\" class=\"post_boxtype half required\">"+options+"</select><span class=\"label\">"+boxesTypeLabel+"</span></div>";
        return html;
    }

    @Override
    public String getInitScript() {
        String script = "$.each(editor.forms, function(i, v){ $(row).find(\".post_form, .post_output_form\").append('<option value=\"'+v.value+'\">'+v.label+'</option>'); });\n";
        script += "$(row).find(\".post_image, .post_output_image\").autocomplete({source: [],minLength: 0,open: function() {$(this).autocomplete('widget').css('z-index', 99999);return false;}});\n";
        script += "if (editor.updatePostSource === undefined) {";
        script += "    editor.fieldOptions = {};";
        script += "    editor.updatePostSource = function(value, row, changedField) {";
        script += "        var thisObj = this;var source = [];\n";
        script += "        var name = \".post_image\";\n";
        script += "        if ($(changedField).hasClass(\"post_output_form\")) {\n";
        script += "            name = \".post_output_image\";\n";
        script += "        }\n";
        script += "        if (thisObj.fieldOptions[value] !== undefined) {";
        script += "             $.each(thisObj.fieldOptions[value], function(i, option) {";
        script += "                 if (option['value'] !== \"\" && $.inArray(option['value'], source) === -1) {";
        script += "                     source.push(option['value']);";
        script += "                 }";
        script += "             });";
        script += "        }\n source.sort(); $(row).find(name).autocomplete(\"option\", \"source\", source);";
        script += "        if (!(value === \"\" && $(row).find(name).val() !== undefined && $(row).find(name).val().indexOf(\"http\") === 0) && $(row).find(name).val() !== \"\" && $.inArray($(row).find(name).val(), source) === -1) {";
        script += "            $(row).find(name).val(\"\");";
        script += "        }\n";
        script += "    }\n";
        script += "}\n";
        script += "$(row).find(\".post_form, .post_output_form\").on(\"change\", function(){";
        script += "    var thisObj = $(this);";
        script += "    var value = $(this).val();";
        script += "    if (value === \"\" || editor.fieldOptions[value] !== undefined) {";
        script += "        editor.updatePostSource(value, $(row), thisObj);";
        script += "    } else {";
        script += "        $.ajax({";
        script += "            url: editor.options.contextPath + '/web/json/console/app' + editor.options.appPath + '/form/columns/options?formDefId=' + escape(value),";
        script += "            dataType: \"text\",";
        script += "            method: \"GET\",";
        script += "            success: function(data) {";
        script += "                if (data !== undefined && data !== null) {";
        script += "                    var options = $.parseJSON(data);";
        script += "                    editor.fieldOptions[value] = options;";
        script += "                    editor.updatePostSource(value, $(row), thisObj);";
        script += "                }";
        script += "            }";
        script += "        });";
        script += "    }});";
        script += "setTimeout(function(){$(row).find(\".post_form, .post_output_form\").trigger(\"change\");}, 1000);";
        
        return script;
    }
}