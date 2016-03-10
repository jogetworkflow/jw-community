<%@page contentType="application/x-java-jnlp-file" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="serverPath" value="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}" />

<%
response.setContentType("application/x-java-jnlp-file");
response.setHeader("Expires", "0");
response.setHeader("Content-disposition", "filename=webstart.jnlp");
response.addDateHeader("Date", java.util.Calendar.getInstance().getTime().getTime());
response.addDateHeader("Last-Modified", java.util.Calendar.getInstance().getTime().getTime());
%>
<?xml version="1.0" encoding="utf-8"?>
<jnlp codebase="${serverPath}/webstart">
    <information>
        <title>jwdesigner-${project.version}</title>
        <vendor>joget.org</vendor>
        <homepage href="http://www.joget.org/"/>
        <description>joget.org wfdesigner</description>
        <offline-allowed/>
        <shortcut online="true">
            <desktop/>
        </shortcut>
    </information>
    <security>
        <all-permissions/>
    </security>
    <update check="background" policy="prompt-update"/>
    <resources>
        <j2se version="1.5+"/>
        <jar href="wflow-designer-${project.version}.jar" main="true"/>
        <jar href="jped-2.0.jar"/>
        <jar href="jped-highlight-2.0.jar"/>
        <jar href="jgraph-5.10.2.0.jar"/>
        <jar href="xercesImpl-2.8.1.jar"/>
        <jar href="xml-apis-1.3.04.jar"/>
        <jar href="itext-2.0.6.jar"/>
        <jar href="jedit-syntax-2.2.2.jar"/>
        <jar href="commons-codec-1.2.jar"/>
        <jar href="commons-discovery-0.2.jar"/>
        <jar href="commons-logging-1.0.4.jar"/>
        <jar href="officelnfs-2.7.jar"/>
        <jar href="log4j-1.2.8.jar"/>
        <jar href="httpclient-4.5.1.jar"/>
        <jar href="httpcore-4.4.3.jar"/>
        <jar href="httpmime-4.5.1.jar"/>
        <jar href="nimrod-laf-1.2.jar"/>
    </resources>
    <application-desc main-class="org.joget.designer.Designer">
        <c:if test="${!empty param.file}">
            <argument>${serverPath}/test/<c:out value="${param.file}" escapeXml="true"/></argument>
        </c:if>
        <c:if test="${!empty param.url}">
            <argument><c:out value="${param.url}"/></argument>
        </c:if>
            <fmt:setBundle basename="custom" var="custom"/>
            <argument>title:<fmt:message key="Title" bundle="${custom}"/></argument>
        <c:if test="${!empty param.path}">
            <argument>path:<c:out value="${param.path}"/></argument>
        </c:if>
        <c:if test="${!empty param.appId}">
            <argument>appId:<c:out value="${param.appId}" escapeXml="true"/></argument>
        </c:if>
        <c:if test="${!empty param.appVersion}">
            <argument>appVersion:<c:out value="${param.appVersion}" escapeXml="true"/></argument>
        </c:if>
        <c:if test="${!empty param.locale}">
            <argument>locale:<c:out value="${param.locale}" escapeXml="true"/></argument>
        </c:if>
        <c:if test="${!empty param.username}">
            <argument>username:<c:out value="${param.username}" escapeXml="true"/></argument>
        </c:if>
        <c:if test="${!empty param.hash}">
            <argument>hash:<c:out value="${param.hash}" escapeXml="true"/></argument>
        </c:if>
        <c:if test="${!empty param.domain}">
            <argument>domain:<c:out value="${param.domain}" escapeXml="true"/></argument>
        </c:if>
        <c:if test="${!empty param.port}">
            <argument>port:<c:out value="${param.port}" escapeXml="true"/></argument>
        </c:if>
        <c:if test="${!empty param.context}">
            <argument>context:<c:out value="${param.context}" escapeXml="true"/></argument>
        </c:if>
        <c:if test="${!empty param.session}">
            <argument>session:<c:out value="${param.session}" escapeXml="true"/></argument>
        </c:if>
        <c:if test="${!empty param.tokenName}">
            <argument>tokenName:<c:out value="${param.tokenName}" escapeXml="true"/></argument>
        </c:if>
        <c:if test="${!empty param.tokenValue}">
            <argument>tokenValue:<c:out value="${param.tokenValue}" escapeXml="true"/></argument>
        </c:if>
    </application-desc>
</jnlp>
