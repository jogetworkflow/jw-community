<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.monitor.scheduler.view.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="viewScheduler" commandName="schedulerLog" cssClass="form">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.monitor.scheduler.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.monitor.scheduler.common.label.jobName"/></label>
                    <span class="form-input">
                    	<c:out value="${schedulerLog.jobName}"/>
                    </span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.monitor.scheduler.common.label.jobClassName"/></label>
                    <span class="form-input">
                    	<c:out value="${schedulerLog.jobClassName}"/>
                    </span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.monitor.scheduler.common.label.finishTime"/></label>
                    <span class="form-input">
                    	<c:out value="${schedulerLog.finishTime}"/>
                    </span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.monitor.scheduler.common.label.jobStatus"/></label>
                    <span class="form-input">
                    	<c:out value="${schedulerLog.jobStatus}"/>
                    </span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.monitor.scheduler.common.label.message"/></label>
                    <span class="form-input">
                    	<c:out value="${schedulerLog.message}"/>
                    </span>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="button" value="<fmt:message key="general.method.label.back"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
    </script>
<commons:popupFooter />
