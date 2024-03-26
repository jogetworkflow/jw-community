<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/JSON.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/JSONError.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/storage/jquery.html5storage.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/tiny_mce/js/tinymce/tinymce.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/ace/ace.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dropzone/dropzone.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/tinyColorPicker/jqColorPicker.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/tooltipster/js/tooltipster.bundle.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/speakingurl/speakingurl.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/peditor?build=<fmt:message key="build.number"/>"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/scrollTo/jquery.scrollTo.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.propertyeditor.js?build=<fmt:message key="build.number"/>"></script>

<!-- Required dependencies -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/lib/codemirror.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/display/panel.js"></script>

<!-- For the modes -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/mode/javascript/javascript.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/mode/xml/xml.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/mode/css/css.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/mode/htmlmixed/htmlmixed.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/mode/clike/clike.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/mode/sql/sql.js"></script>

<!-- Fullscreen -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/display/fullscreen.js"></script>

<!-- Lint related addons, it provides a way to display errors and warnings in your editor -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/lint/lint.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/lint/htmlmixed-lint.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/lint/javascript-lint.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/lint/css-lint.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/lint/json-lint.js"></script>

<!-- Enable code hint. Works together witht he lint addons -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/hint/jshint.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/hint/htmlhint.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/hint/csslint.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/hint/jsonlint.js"></script>

<!-- Match Brackets, close tags, close brackets, -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/edit/matchbrackets.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/edit/closetag.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/fold/xml-fold.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/edit/closebrackets.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/dialog/dialog.js"></script>

<!-- Search related addon, these are the dependencies that make search possible -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/search/searchcursor.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/search/search.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/search/jump-to-line.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/search/match-highlighter.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/scroll/annotatescrollbar.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/search/matchesonscrollbar.js"></script>

<!-- Fold addons, these give users the ability to fold the code -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/fold/foldcode.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/codemirror/addon/fold/foldgutter.js"></script>

<script type="text/javascript">// Immediately after the js include
    Dropzone.autoDiscover = false;
</script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dropzone/dropzone.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/tooltipster/css/tooltipster.bundle.min.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.propertyeditor.css?build=<fmt:message key="build.number"/>" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/universal/lib/material-design-iconic-font/css/material-design-iconic-font.min.css" />

<!-- CSS for Codemirror6 -->
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/codemirror/lib/codemirror.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/codemirror/theme/ayu-mirage.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/codemirror/addon/display/fullscreen.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/codemirror/addon/lint/lint.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/codemirror/addon/dialog/dialog.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/codemirror/addon/search/matchesonscrollbar.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/codemirror/addon/fold/foldgutter.css" />

<c:if test="${not empty theme and theme ne 'classic'}">
        <link href="${pageContext.request.contextPath}/css/builderTheme.css" rel="stylesheet" />
</c:if>
        
<c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.propertyeditor_rtl.css?build=<fmt:message key="build.number"/>">
    <script type="text/javascript">
        UI.rtl = true;
        $(document).ready(function(){
            $("body").addClass("rtl");
        });
    </script>    
</c:if>
<c:if test="${fn:startsWith(currentLocale, 'zh') == true}">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/tiny-pinyin/tiny-pinyin.js"></script>
</c:if>
<c:if test="${fn:startsWith(currentLocale, 'ja') == true}">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/wanakana/wanakana.min.js"></script>
</c:if>    
<c:if test="${fn:startsWith(currentLocale, 'ko') == true}">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/aromanize-js/aromanize.js"></script>
</c:if>   