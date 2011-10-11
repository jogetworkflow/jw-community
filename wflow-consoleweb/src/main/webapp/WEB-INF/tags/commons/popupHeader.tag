<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ tag import="org.springframework.security.context.SecurityContextHolder"%>
<%@ tag import="org.joget.workflow.util.WorkflowUtil"%>
<%@ attribute name="title" %>

<c:if test="${empty title}"><c:set var="title"><fmt:message key="console.header.browser.title"/></c:set></c:if>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=8" />
        <title>${title}</title>

        <jsp:include page="/WEB-INF/jsp/includes/css.jsp" />
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <jsp:include page="/WEB-INF/jsp/includes/rtl.jsp" />
        
    </head>
    <body class="popupBody">
