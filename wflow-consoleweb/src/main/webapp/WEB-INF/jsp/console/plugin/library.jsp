<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/JSON.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/JSONError.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/storage/jquery.html5storage.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/wro/quill.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/ace/ace.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/chosen/chosen.jquery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dropzone/dropzone.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/tinyColorPicker/jqColorPicker.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/tooltipster/js/tooltipster.bundle.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/peditor?build=<fmt:message key="build.number"/>"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/scrollTo/jquery.scrollTo.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.propertyeditor.js?build=<fmt:message key="build.number"/>"></script>
<script type="text/javascript">// Immediately after the js include
    Dropzone.autoDiscover = false;
</script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/chosen/chosen.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dropzone/dropzone.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/wro/quill.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/tooltipster/css/tooltipster.bundle.min.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.propertyeditor.css?build=<fmt:message key="build.number"/>" />
        
<c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.propertyeditor_rtl.css?build=<fmt:message key="build.number"/>">
    <script type="text/javascript">
        UI.rtl = true;
        $(document).ready(function(){
            $("body").addClass("rtl");
        });
    </script>    
</c:if>