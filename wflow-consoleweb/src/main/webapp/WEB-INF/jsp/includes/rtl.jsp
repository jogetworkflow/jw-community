<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<c:if test="${rightToLeft == 'true'}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/rightToLeft.css">
    <script type="text/javascript">
        $(document).ready(function(){
            $(".row-content").append("<div style=\"clear:both\"></div>");
        });
    </script>
</c:if>
