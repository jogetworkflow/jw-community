<%@ page import="org.joget.apps.app.service.MobileUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="mobileView" value="<%= MobileUtil.isMobileView() || MobileUtil.isMobileUserAgent(request) %>"/>
<c:if test="${mobileView}">
    <c:set scope="request" var="appDef" value="${appDef}"/>
    <c:set scope="request" var="assignment" value="${assignment}"/>
    <c:set scope="request" var="activityForm" value="${activityForm}"/>
    <c:set scope="request" var="form" value="${form}"/>
    <c:set scope="request" var="formHtml" value="${formHtml}"/>
    <c:set scope="request" var="stay" value="${stay}"/>
    <c:set scope="request" var="submitted" value="${submitted}"/>
    <c:set scope="request" var="errorCount" value="${errorCount}"/>
    <jsp:forward page="/WEB-INF/jsp/mobile/mAssignmentView.jsp"/>
</c:if>

<commons:popupHeader />

<div id="main-body-header"><c:out value="${assignment.processName}" escapeXml="true"/> - <c:out value="${assignment.activityName}" escapeXml="true"/></div>

<div id="main-body-content">
    <jsp:include page="formView.jsp" flush="true" />
</div>
    
<commons:popupFooter />
