<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader bodyCssClass=" builder-popup no-header" builderTheme="true"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror6/codemirror6-bundle.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/wro/codeMirror6.min.css" />
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

    #description_editor > .cm-editor {
        min-height: 300px;
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

            var isDark = false;
            //Set dark theme if dark theme mode is activated
            if ($('body').attr('builder-theme') === "dark") {
                isDark = true;
            }

            codeeditor = window.initCM6Editor($("#description_editor")[0], "", "", isDark);

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
                    UI.alert("<ui:msgEscJS key="adv.tool.updated"/>", {icon: 'success'});
                }
            }
        });
    </script>
</div>  
<commons:popupFooter />
