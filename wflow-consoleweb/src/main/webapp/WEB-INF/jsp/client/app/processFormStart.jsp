<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

<div id="main-body-header"><c:out value="${process.name}" escapeXml="true"/></div>

<div id="main-body-content">
    <jsp:include page="formView.jsp" flush="true" />
</div>

<commons:popupFooter />
