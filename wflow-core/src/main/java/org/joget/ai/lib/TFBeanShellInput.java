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
                        "                   var offsetTop = \"0px\"\r\n" + //
                        "                  if ($(\"body #top-panel\").length > 0){\r\n" + //
                        "                    offsetTop = $(\"body #top-panel\").outerHeight() + \"px\"\r\n" + //
                        "                  }\n"+
                        "                 if (codeeditor.getOption(\"fullScreen\")){\r\n" + //
                        "                    $(row).find(\".CodeMirror-advanced-dialog\").css({position:\"fixed\", zIndex:\"2147483647\", top: offsetTop, left:\"calc(100% - 320px)\"});\r\n" + //
                        "                  }\r\n" + //
                        "                  else{\r\n" + //
                        "                    $(row).find(\".CodeMirror-advanced-dialog\").css({position:\"fixed\", top:\"0\", left:\"calc(100% - 320px)\", zIndex:\"999\", marginTop:\"150px\"});\r\n" + //
                        "                  }\r\n" + //
                        "                  $(row).find(\".CodeMirror-advanced-dialog\").draggable()"+
                        "               },\r\n" + //
                        "                \"Ctrl-=\": function(cm) {\r\n" + //
                        "                  cm.increaseFontSize();\r\n" + //
                        "                },\r\n" + //
                        "                \"Ctrl--\": function(cm) {\r\n" + //
                        "                  cm.decreaseFontSize();\r\n" + //
                        "                },\r\n" + //
                        "                \"Ctrl-/\": function(cm) {\r\n" + //
                        "                  cm.toggleComment()\r\n" + //
                        "                }"+
                        "              }\n" +
                        "          });";
        script += "codeeditor.execCommand(\"replace\");";
        script += "$(row).find(\".code-editor\").css({overflow: \"visible\"});";
        script += "$(row).find(\".code-editor .CodeMirror-advanced-dialog\").css({display: 'none'});";
        script += "codeeditor.setOption(\"mode\", \"text/x-java\");";
        script += "if ($('body').attr('builder-theme') === \"dark\") {\n codeeditor.setOption(\"theme\", \"ayu-mirage\");\n}";
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
                    "            if (event.key === \"F1\" && codeeditor.getOption(\"fullScreen\")) {\n" +
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
                    "            }\n"+
                    "            else if (event.key === \"F1\" && codeeditor.getOption(\"fullScreen\")) {\r\n" + //
                    "               event.preventDefault();"+
                    "            }else if (event.key === 'F12' || (event.key === 'Escape' && codeeditor.getOption(\"fullScreen\"))){\n" +
                    "                event.preventDefault();\n" +
                    "                if (codeeditor.getOption(\"fullScreen\")) {\n" +
                    "                    codeeditor.setOption(\"fullScreen\", false);\n" +
                    "                    $(\".property-editor-container\").css({\"overflow\":\"hidden\"})\n" +
                    "                    $(\".property-editor-pages\").css({\"overflow-y\":\"scroll\"})\n" +
                    "                    $(row).find(\".CodeMirror-advanced-dialog .row.find button:last\").click();\n" +
                    "                    resetHeight();\n" +
                    "                    $(row).find(\".CodeMirror-advanced-dialog\").css({position:\"sticky\", top:\"0px\", zIndex:\"10\"});\n" +
                    "                    $(row).find(\".CodeMirror\").css({left: \"\", top: \"\"})\n"+
                    "                    event.stopPropagation();\n" +
                    "                }else{\n" +
                    "                    var offsetTop = \"0px\"\r\n" + //
                    "                    var offsetLeft = \"0px\"\r\n" + //
                    "                    if ($(\"body #top-panel\").length > 0){\r\n" + //
                    "                        offsetTop = $(\"body #top-panel\").outerHeight() + \"px\"\r\n" + //
                    "                    }\r\n" + //
                    "                    if ($(\"body #quick-nav-bar\").length > 0){\r\n" + //
                    "                        offsetLeft = $(\"body #quick-nav-bar\").outerWidth()+\"px\"\r\n" + //
                    "                    }\n"+
                    "                    codeeditor.setOption(\"fullScreen\", true);\n" +
                    "                    $(row).find(\".CodeMirror-advanced-dialog\").css({position:\"fixed\", zIndex:\"2147483647\", top: offsetTop, left:\"calc(100% - 320px)\", marginTop:\"0px\"})\r\n" + //
                    "                    $(row).find(\".CodeMirror\").css({left: offsetLeft, top: offsetTop})\r\n" + //
                    "                    $(row).find(\".CodeMirror-advanced-dialog\").draggable()" +
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
