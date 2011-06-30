<%@page contentType="application/x-java-jnlp-file" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
        <title>jwdesigner-3.0-BETA</title>
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
        <jar href="wflow-designer.jar" version="3.0-BETA" main="true"/>
        <jar href="jped.jar" version="2.0"/>
        <jar href="jped-highlight.jar" version="2.0"/>
        <jar href="jgraph.jar" version="5.10.2.0"/>
        <jar href="xercesImpl.jar" version="2.8.1"/>
        <jar href="xml-apis.jar" version="1.3.04"/>
        <jar href="itext.jar" version="2.0.6"/>
        <jar href="jedit-syntax.jar" version="2.2.2"/>
        <jar href="commons-codec.jar" version="1.2"/>
        <jar href="commons-discovery.jar" version="0.2"/>
        <jar href="commons-logging.jar" version="1.0.4"/>
        <jar href="officelnfs.jar" version="2.7"/>
        <jar href="xml-apis.jar" version="1.3.04"/>
        <jar href="log4j.jar" version="1.2.15"/>
        <jar href="commons-httpclient.jar" version="3.1"/>
        <jar href="nimrod-laf.jar" version="1.2"/>
    </resources>
    <application-desc main-class="org.joget.designer.Designer">
        <c:if test="${!empty param.file}">
            <argument>${serverPath}/test/${param.file}</argument>
        </c:if>
        <c:if test="${!empty param.url}">
            <argument>${param.url}</argument>
        </c:if>
        <c:if test="${!empty param.path}">
            <argument>path:${param.path}</argument>
        </c:if>
        <c:if test="${!empty param.appId}">
            <argument>appId:${param.appId}</argument>
        </c:if>
        <c:if test="${!empty param.appVersion}">
            <argument>appVersion:${param.appVersion}</argument>
        </c:if>
        <c:if test="${!empty param.locale}">
            <argument>locale:${param.locale}</argument>
        </c:if>
        <c:if test="${!empty param.username && !empty param.hash}">
            <argument>username:${param.username}</argument>
            <argument>hash:${param.hash}</argument>
        </c:if>
    </application-desc>
</jnlp>
