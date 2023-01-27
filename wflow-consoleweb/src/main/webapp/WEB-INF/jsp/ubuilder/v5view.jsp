<%@page import="org.springframework.util.StopWatch"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page contentType="text/html" pageEncoding="utf-8"%>

<%
    StopWatch sw = new StopWatch(request.getRequestURI());
    sw.start("userview");
%>

<c:if test="${!empty processer}">
    <c:set var="html" value="${processer.html}"/>
    
    <c:if test="${!empty processer.redirectUrl}">
        <c:redirect url="${processer.redirectUrl}"/>
    </c:if> 
    
    ${html}  
</c:if>

<%
    sw.stop();
    long duration = sw.getTotalTimeMillis();
    pageContext.setAttribute("duration", duration);
%>    
<!--[${duration}ms]-->
