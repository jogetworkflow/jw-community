<%@tag description="UI tree" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<%@ attribute name="var" %>
<%@ attribute name="url" %>
<%@ attribute name="baseUrl" %>
<%@ attribute name="title" %>
<%@ attribute name="jsonData" %>
<%@ attribute name="divToUpdate" required="true" %>
<%@ attribute name="nodeLabel" %>
<%@ attribute name="nodeKey" %>
<%@ attribute name="nodeCount" %>
<%@ attribute name="nodeDescription" %>
<%@ attribute name="href"  %>
<%@ attribute name="hrefParam"  %>
<%@ attribute name="hrefQuery"  %>
<%@ attribute name="hrefDialog"  %>
<%@ attribute name="hrefDialogWidth"  %>
<%@ attribute name="hrefDialogHeight"  %>
<%@ attribute name="hrefDialogTitle"  %>
<%@ attribute name="showRefresh" %>
<%@ attribute name="xss" %>

<c:if test="${empty var}"><c:set var="var" value="JsonTree"/></c:if>
<c:if test="${empty title}"><c:set var="title" value=""/></c:if>
<c:if test="${empty nodeLabel}"><c:set var="nodeLabel" value="name"/></c:if>
<c:if test="${empty nodeKey}"><c:set var="nodeKey" value="id"/></c:if>
<c:if test="${empty jsonData}"><c:set var="jsonData" value="data"/></c:if>
<c:if test="${empty hrefQuery}"><c:set var="hrefQuery" value="false"/></c:if>
<c:if test="${empty hrefDialog}"><c:set var="hrefDialog" value="false"/></c:if>
<c:if test="${empty hrefDialogWidth}"><c:set var="hrefDialogWidth" value="450px"/></c:if>
<c:if test="${empty hrefDialogHeight}"><c:set var="hrefDialogHeight" value="400px"/></c:if>
<c:if test="${empty xss}"><c:set var="xss" value="false"/></c:if>

<jsp2:include page="/WEB-INF/jsp/includes/scripts.jsp" />

<c:if test="${showRefresh}">
<a href="#" onclick="${var}.refresh()">refresh</a>
</c:if>
<div id="${divToUpdate}"></div>

<script>
UI.base = "${pageContext.request.contextPath}";

var ${var} = new JsonTree("${divToUpdate}", "${url}");
${var}.baseUrl = "${baseUrl}";
${var}.title = "${title}";
${var}.jsonData = "${jsonData}";
${var}.nodeKey = "${nodeKey}";
${var}.nodeLabel = "${nodeLabel}";
${var}.nodeDescription = "${nodeDescription}";
${var}.nodeCount = "${nodeCount}";
${var}.xss = ${xss};

${var}.link = new Link("${href}", "${hrefParam}", ${hrefQuery}, new PopupDialog("${href}"<c:if test="${xss}">, '<fmt:message key="wflowClient.assignment.view.label.title"/>', '_blank'</c:if>));
${var}.init();
</script>


