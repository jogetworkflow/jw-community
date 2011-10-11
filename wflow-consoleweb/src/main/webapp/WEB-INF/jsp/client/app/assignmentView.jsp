<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

<h3>${assignment.processName} - ${assignment.activityName}</h3>

<jsp:include page="formView.jsp" flush="true" />

<commons:popupFooter />
