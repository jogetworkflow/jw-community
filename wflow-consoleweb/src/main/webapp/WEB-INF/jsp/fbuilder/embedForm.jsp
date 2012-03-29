<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@page contentType="text/html" pageEncoding="utf-8"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.7.2.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.8.6.min.js"></script>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form.css" />
        
        <c:if test="${rightToLeft == 'true'}">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form_rtl.css" />
        </c:if>
    </head>
    <body>
        <c:set var="formHtml" scope="request" value="${formHtml}"/>
        <c:set var="errorCount" scope="request" value="${errorCount}"/>
        <c:set var="submitted" scope="request" value="${submitted}"/>
        <jsp:include page="../client/app/formView.jsp" flush="true" />
        
        <c:if test="${submitted && errorCount == 0}">
            <script type="text/javascript">
                var setting = ${setting};
                setting['result'] = '${jsonResult}';
                if (window.parent && window.parent.${callback}){
                    window.parent.${callback}(setting);
                }else if (window.opener && window.opener.${callback}){
                    window.opener.${callback}(setting);
                }else if(${callback}){
                    ${callback}(setting);
                }
                window.close();
            </script>
        </c:if>
    </body>
</html>
