package org.joget.ai.lib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.joget.ai.TensorFlowInput;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.tensorflow.Tensor;
import org.tensorflow.types.TBool;

public class TFBeanShellInput implements TensorFlowInput {

    @Override
    public Tensor getInputs(Map params, String processId, Map<String, String> variables, Map<String, Object> tempDataHolder) throws IOException {
        String script = params.get("script").toString();
        String name = params.get("name").toString();
        if (!script.isEmpty()) {
            Map properties = new HashMap();
            properties.put("name", name);
            properties.put("processId", processId);
            properties.put("variables", variables);
            properties.put("tempDataHolder", tempDataHolder);

            return (Tensor) AppPluginUtil.executeScript(script, properties);
        }
        return TBool.scalarOf(false);
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
        return ResourceBundleUtil.getMessage("app.simpletfai.beanshell.input");
    }

    @Override
    public String getUI() {
        String html = "<div><textarea name=\"script\" class=\"input_script\" style=\"display:none;\"></textarea><pre class=\"code-editor\"></pre></div>";
        return html;
    }

    @Override
    public String getInitScript() {
        String script = "var codeeditor = CodeMirror($(row).find(\".code-editor\")[0], {\n" +
                        "            lineNumbers: true,\n" +
                        "            mode: \"text\",\n" +
                        "            matchBrackets: true,\n" +
                        "            theme: \"default\",\n" +
                        "            autoRefresh:true,\n" +
                        "            gutters: [\"CodeMirror-lint-markers\", \"CodeMirror-linenumbers\", \"CodeMirror-foldgutter\"],\n" +
                        "            lint: true,\n" +
                        "            autoCloseTags: true,\n" +
                        "            autoCloseBrackets: true,\n" +
                        "            foldGutter: true,\n" +
                        "            lint: true,\n" +
                        "            lineWrapping: true,\n" +
                        "            highlightSelectionMatches: {annotateScrollbar: true, minChars: 1},\n" +
                        "            extraKeys: {\n" +
                        "                \"Ctrl-F\": function(cm) {\n" +
                        "                  cm.execCommand(\"replace\")\n" +
                        "                  $(row).find(\".CodeMirror-advanced-dialog\").css({display: 'block'})\n" +
                        "                  if (codeeditor.getOption(\"fullScreen\")){\n" +
                        "                    $(row).find(\".CodeMirror-advanced-dialog\").css({position:\"fixed\", zIndex:\"1001\", top: \"0px\", right: \"0px\", width: \"320px\"})\n" +
                        "                    $(\".property-editor-container\").css({\"overflow\":\"visible\"})\n" +
                        "                    $(\".property-editor-pages\").css({\"overflow-y\":\"visible\"})\n" +
                        "                  }\n" +
                        "                  else{\n" +
                        "                    $(row).find(\".CodeMirror-advanced-dialog\").css({position:\"fixed\", top:\"0\", right:\"0\", width:\"320px\", zIndex:\"999\", marginTop:\"150px\"});" +
                        "                    $(row).closest(\".property-type-codeeditor\").css({overflow: \"visible\"})\n" +
                        "                  }\n" +
                        "                },\n" +
                        "                \"Ctrl-=\": function(cm) {\n" +
                        "                  modifyFontSize(true);\n" +
                        "                },\n" +
                        "                \"Ctrl--\": function(cm) {\n" +
                        "                  modifyFontSize(false);\n" +
                        "                }\n" +
                        "              }\n" +
                        "          });";
        script += "codeeditor.execCommand(\"replace\");";
        script += "$(row).find(\".code-editor\").css({overflow: \"visible\"});";
        script += "$(row).find(\".code-editor .CodeMirror-advanced-dialog\").css({display: 'none'});";
        script += "codeeditor.setOption(\"mode\", \"text/x-java\");";
        script += "if ($('body').attr('builder-theme') === \"dark\") {\n codeeditor.setOption(\"theme\", \"ayu-mirage\");\n}";
        script += "function modifyFontSize(increase){\n" +
                    "var wrapper = codeeditor.getWrapperElement();\n" +
                    "var currentSize = parseFloat(window.getComputedStyle(wrapper, null).getPropertyValue('font-size'));\n" +
                    "var newSize = increase ? currentSize + 2 : currentSize - 2;\n" +
                    "\n" +
                    "wrapper.style.fontSize = newSize + 'px';\n" +
                    "\n" +
                    "codeeditor.refresh()\n" +
                    "}";
        script += "var panels = {};\r\n" + //
                  "var panelId = \"\";";
        script += "function makePanel(where) {\r\n" + //
                        "            var node = document.createElement(\"div\");\r\n" + //
                        "            var label, div, msg;\r\n" + //
                        "\r\n" + //
                        "            node.id = \"panel-\";\r\n" + //
                        "            node.className = \"panel \" + where;\r\n" + //
                        "            \r\n" + //
                        "            var wrapper = codeeditor.getWrapperElement();\r\n" + //
                        "\r\n" + //
                        "            div = $(\"<div>\")\r\n" + //
                        "            msg = get_peditor_msg('peditor.codemirror.helpMessage')\r\n" + //
                        "            msg.split(\" | \").forEach(el =>{\r\n" + //
                        "                div.append($(\"<span>\").text(el))\r\n" + //
                        "            })\r\n" + //
                        "\r\n" + //
                        "            label = div.appendTo(node);\r\n" + //
                        "\r\n" + //
                        "            label.css({\r\n" + //
                        "                \"color\": \"black\",\r\n" + //
                        "                \"font-size\": \"12px\",\r\n" + //
                        "                \"padding\": \"5px 10px\",\r\n" + //
                        "                \"font-weight\":\"bold\",\r\n" + //
                        "                \"display\": \"flex\",\r\n" + //
                        "                \"flex-direction\": \"column\",\r\n" + //
                        "                \"white-space\": \"normal\"\r\n" + //
                        "            })\r\n" + //
                        "\r\n" + //
                        "            $(node).css({\r\n" + //
                        "                \"background-color\":\"rgb(255, 250, 143)\"\r\n" + //
                        "            })\r\n" + //
                        "\r\n" + //
                        "            return node;\r\n" + //
                        "        };";
        script += "$(row).find(\".code-editor\").on('keydown', function(event) {\n" +
                    "            if (event.key === \"F1\") {\n" +
                    "                event.preventDefault();\n" +
                    "                if (panels[panelId]) {\n" +
                    "                    //Resets height\n" +
                    "                    codeeditor.setSize(null, $(row).find(\".CodeMirror\").height()-1)\n" +
                    "                    panels[panelId].clear();\n" +
                    "                    delete panels[panelId];\n" +
                    "                    resetHeight();\n" +
                    "                } else {\n" +
                    "                    addPanel(\"top\");\n" +
                    "                    resetHeight();\n" +
                    "                }\n" +
                    "            }else if (event.key === 'F12' || (event.key === 'Escape' && codeeditor.getOption(\"fullScreen\"))){\n" +
                    "                event.preventDefault();\n" +
                    "                if (codeeditor.getOption(\"fullScreen\")) {\n" +
                    "                    $(row).closest('#right-panel').css('z-index', 20);\n" +
                    "                    codeeditor.setOption(\"fullScreen\", false);\n" +
                    "                    $(\".property-editor-container\").css({\"overflow\":\"hidden\"})\n" +
                    "                    $(\".property-editor-pages\").css({\"overflow-y\":\"scroll\"})\n" +
                    "                    $(row).find(\".CodeMirror-advanced-dialog .row.find button:last\").click();\n" +
                    "                    resetHeight();\n" +
                    "                    $(row).find(\".CodeMirror-advanced-dialog\").css({position:\"sticky\", top:\"0px\", width:\"320px\", zIndex:\"10\"});\n" +
                    "                    event.stopPropagation();\n" +
                    "                }else{\n" +
                    "                    $(row).closest('#right-panel').css('z-index', 8999);\n" +
                    "                    codeeditor.setOption(\"fullScreen\", true);\n" +
                    "                    $(row).find(\".CodeMirror-advanced-dialog\").css({position:\"fixed\", zIndex:\"1001\", top: \"0px\", right: \"0px\", marginTop:\"0px\"})\n" +
                    "                    $(\".property-editor-container\").css({\"overflow\":\"visible\"})\n" +
                    "                    $(\".property-editor-pages\").css({\"overflow-y\":\"visible\"})\n" +
                    "                }\n" +
                    "            }\n" +
                    "        });";
        script += "function resetHeight(){\n" +
        "            //Make CodeMirror unscrollable, and height follows the code written \n" +
        "            $(row).find(\".CodeMirror\").css({\"height\":\"auto\", \"minHeight\":\"300px\"});\n" +
        "            $(row).find(\".CodeMirror-scroll\").css({\"maxHeight\":\"auto\", \"minHeight\":\"300px\"});\n" +
        "        }";
        script += "function addPanel(where) {\r\n" + //
                    "   var node = makePanel(where);\r\n" + //
                    "   panelId = \"panel\";\r\n" + //
                    "   panels[panelId] = codeeditor.addPanel(node, {position: where, stable: true});\r\n" + //
                    "}";
        script += "var tooltip = $(\"<span>\").attr('title', get_peditor_msg('peditor.codemirror.tooltipTitle')).append(\" <i class=\\\"zmdi zmdi-info-outline\\\"></i>\");\r\n";
        script += "$(row).find(\".row-title\").append(tooltip);";
        
        script += "var textarea = $(row).find(\".input_script\");";
        script += "setTimeout(function(){codeeditor.setValue(textarea.val());}, 2000);";
        script += "codeeditor.on('change', function(){";
        script += "textarea.val(codeeditor.getValue());";
        script += "});";
        script += "resetHeight();";
        
        return script;
    }
    
}
