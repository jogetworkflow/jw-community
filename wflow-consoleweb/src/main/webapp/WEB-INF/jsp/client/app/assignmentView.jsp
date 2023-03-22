<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader bodyCssClass=" builder-popup"/>

<div id="main-body-header"><c:out value="${assignment.processName}" escapeXml="true"/> - <c:out value="${assignment.activityName}" escapeXml="true"/></div>

<div id="main-body-content">
    <jsp:include page="formView.jsp" flush="true" />
</div>
    
<commons:popupFooter />
