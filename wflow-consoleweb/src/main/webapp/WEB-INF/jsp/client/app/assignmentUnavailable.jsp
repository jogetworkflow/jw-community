<%@ page import="org.joget.apps.app.service.MobileUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="mobileView" value="<%= MobileUtil.isMobileView() || MobileUtil.isMobileUserAgent(request) %>"/>
<c:if test="${mobileView}">
    <jsp:forward page="/WEB-INF/jsp/mobile/mAssignmentUnavailable.jsp"/>
</c:if>

<commons:popupHeader />

<div id="main-body-header"><fmt:message key="client.app.run.process.label.assignment.unavailable" /></div>
<div id="main-body-content">
    <div id="assignment-body">
        <div id="assignment-body-content">
            <fmt:message key="client.app.run.process.label.assignment.unavailable.explanation" />
        </div>
    </div>
</div>
    
<commons:popupFooter />
