<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>
    
<c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/rtl.css?build=<fmt:message key="build.number"/>">
    <script type="text/javascript">
        UI.rtl = true;
        $(document).ready(function(){
            $("body").addClass("rtl");
            $(".row-content").append("<div style=\"clear:both\"></div>");
        });
    </script>
</c:if>
