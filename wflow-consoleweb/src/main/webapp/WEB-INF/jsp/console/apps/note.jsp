<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<commons:popupHeader bodyCssClass=" builder-popup no-header" builderTheme="${theme}"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/ace/ace.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/wro/codeMirror.min.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/wro/codeMirror.min.css" />
<c:if test="${not empty theme and theme ne 'classic'}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
</c:if>
<style>
    .sticky-buttons {
        position: fixed;
        right: 20px;
        top: 15px;
    }
    .btn.btn-secondary {
        display: inline-block;
        font-weight: 400;
        text-align: center;
        vertical-align: middle;
        -webkit-user-select: none;
        -moz-user-select: none;
        -ms-user-select: none;
        user-select: none;
        border: 1px solid transparent;
        padding: 0.375rem 0.75rem;
        font-size: 1rem;
        line-height: 1.5;
        border-radius: 0.25rem;
        transition: color .15s ease-in-out,background-color .15s ease-in-out,border-color .15s ease-in-out,box-shadow .15s ease-in-out;
        color: #fff;
        background-color: #6c757d;
        border-color: #6c757d;
    }
    .btn.btn-secondary:hover {
        color: #fff;
        background-color: #5a6268;
        border-color: #545b62;
    }
    #description_editor{
        margin: 0;
    }
</style>    
<div id="main-body-content">
    <div id="appDesc">
        <form method="post" action="${pageContext.request.contextPath}/web/console/app/<c:out value="${appDefinition.id}"/>/${appDefinition.version}/note/submit">
            <textarea id="description" name="description" style="display:none"><c:out value="${appDefinition.description}" escapeXml="true"/></textarea>
            <pre id="description_editor" name="description_editor" class="ace_editor"></pre>
            <br />
            <div class="sticky-buttons"><input type="submit" value="<ui:msgEscHTML key="general.method.label.submit"/>" class="btn btn-secondary"/></div>
        </form>
    </div> 
    <script>
        $(document).ready(function(){
            codeeditor = CodeMirror(document.getElementById("description_editor"), {
                lineNumbers: true,
                mode: "text",
                autoRefresh:true,
                matchBrackets: true,
                theme: "default",
                historyEventDelay: 100,
                gutters: ["CodeMirror-lint-markers", "CodeMirror-linenumbers", "CodeMirror-foldgutter"],
                lint: true,
                autoCloseTags: true,
                autoCloseBrackets: true,
                foldGutter: true,
                lint: true,
                lineWrapping: true,
                highlightSelectionMatches: {annotateScrollbar: true, minChars: 1},
                extraKeys: {
                    "Ctrl-F": function(cm) {
                        cm.execCommand("replace")
                        $('#description_editor').find(".CodeMirror-advanced-dialog").css({display: 'block'})
                        if (codeeditor.getOption("fullScreen")){
                            $('#description_editor').find(".CodeMirror-advanced-dialog").css({position:"fixed", zIndex:"2147483647", top: "0px", left:"calc(100% - 320px)"})
                         }
                        else{
                            $('#description_editor').find(".CodeMirror-advanced-dialog").css({position:"fixed", top:"0px", zIndex:"10", left:"calc(100% - 320px)"});
                        }
                        $('#description_editor').find(".CodeMirror-advanced-dialog").draggable()
                    },
                    "Ctrl-=": function(cm) {
                      cm.increaseFontSize();
                    },
                    "Ctrl--": function(cm) {
                      cm.decreaseFontSize();
                    },
                    "Ctrl-/": function(cm) {
                      cm.toggleComment()
                    }
                }
                });
            
            //Make the replace appear
            codeeditor.execCommand("replace");
            
            //Set height
            $('#description_editor').find(".CodeMirror-advanced-dialog").css({display: 'none'})
            $('#description_editor').find(".CodeMirror").css({"height":"auto", "minHeight": "300px"})
            $('#description_editor').find(".CodeMirror-scroll").css({"maxHeight":"100%", "minHeight":"300px"});

            //Set dark theme if dark theme mode is activated
            if ($('body').attr('builder-theme') === "dark") {
                codeeditor.setOption("theme", "ayu-mirage");
            }

            //Detect keydown for specific actions, such as f12 to toggle full screen mode, escape
            //to exit full scree mode, and F1 to toggle help panel
            $('#description_editor').on('keydown', function(event) {
                if (event.key === 'F12' || (event.key === 'Escape' && codeeditor.getOption("fullScreen")) ){
                    event.preventDefault();
                    if (codeeditor.getOption("fullScreen")) {
                        codeeditor.setOption("fullScreen", false);
                        $('#description_editor').find(".CodeMirror-advanced-dialog").css({display: 'none'})
                        $('#description_editor').find(".CodeMirror").css({"height":"auto", "minHeight": "300px"})
                        $('#description_editor').find(".CodeMirror-scroll").css({"maxHeight":"100%", "minHeight":"300px"});
                        $(this).find(".CodeMirror-advanced-dialog .row.find button:last").click();
                        $(this).find(".CodeMirror-advanced-dialog").css({position:"sticky", top:"0px", zIndex:"10"});
                        event.stopPropagation();
                    }else{
                        codeeditor.setOption("fullScreen", true);
                        $('#description_editor').find(".CodeMirror-advanced-dialog").css({position:"fixed", zIndex:"2147483647", top: "0px", left:"calc(80% - 320px)"})
                        $('#' + thisObj.id).find(".CodeMirror-advanced-dialog").draggable()
                    }
                }
            });
            var textarea = $('textarea[name="description"]');
            codeeditor.setValue(textarea.val())

            codeeditor.on('change', function(){
                textarea.val(codeeditor.getValue());
            });
            
            if ("${saved}" === "true") {
                if (window.top.CustomBuilder !== undefined) {
                    window.top.CustomBuilder.showMessage("<ui:msgEscJS key="adv.tool.updated"/>" ,"success", true);
                    if ($("#versionsView", window.top.document).length > 0) {
                        $("#versionsView", window.top.document).remove();
                    }
                } else {
                    alert("<ui:msgEscJS key="adv.tool.updated"/>");
                }
            }
        });
    </script>
</div>  
<commons:popupFooter />
