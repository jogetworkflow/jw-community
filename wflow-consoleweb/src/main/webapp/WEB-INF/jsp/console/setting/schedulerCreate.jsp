<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.setting.scheduler.create.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="createScheduler" action="${pageContext.request.contextPath}/web/console/setting/scheduler/submit/create" method="POST" commandName="schedulerDetails" cssClass="form">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.setting.scheduler.common.label.details"/></legend>
                <div class="form-row">
                    <label for="jobName"><fmt:message key="console.setting.scheduler.common.label.jobName"/></label>
                    <span class="form-input"><form:input path="jobName" cssErrorClass="form-input-error" size="30" /> *</span>
                </div>
                <div class="form-row">
                    <label for="groupJobName"><fmt:message key="console.setting.scheduler.common.label.groupJobName"/></label>
                    <span class="form-input"><form:input path="groupJobName" cssErrorClass="form-input-error"  size="30"/> *</span>
                </div>
                <div class="form-row">
                    <label for="triggerName"><fmt:message key="console.setting.scheduler.common.label.triggerName"/></label>
                    <span class="form-input"><form:input path="triggerName" cssErrorClass="form-input-error" size="30"/> *</span>
                </div>
                <div class="form-row">
                    <label for="groupTriggerName"><fmt:message key="console.setting.scheduler.common.label.groupTriggerName"/></label>
                    <span class="form-input"><form:input path="groupTriggerName" cssErrorClass="form-input-error"  size="30"/> *</span>
                </div>
                <div class="form-row">
                    <label for="jobClassName"><fmt:message key="console.setting.scheduler.common.label.jobClassName"/></label>
                    <span class="form-input"><form:input path="jobClassName" cssErrorClass="form-input-error"  size="30" /> *</span>
                </div>
                <div class="form-row">
                    <label for="cronExpression"><fmt:message key="console.setting.scheduler.common.label.cronExpression"/></label>
                    <span class="form-input"><form:input path="cronExpression" cssErrorClass="form-input-error"  size="30"/> *</span>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="button" value="<fmt:message key="general.method.label.save"/>"  onclick="validateField()"/>
                <input class="form-button" type="button" value="<fmt:message key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
        function validateField(){
        	var jobName = /^[0-9a-zA-Z_-]+$/.test($("#jobName").attr("value"));
        	var groupJobName = /^[0-9a-zA-Z_-]+$/.test($("#groupJobName").attr("value"));
        	var triggerName = /^[0-9a-zA-Z_-]+$/.test($("#triggerName").attr("value"));
        	var groupTriggerName = /^[0-9a-zA-Z_-]+$/.test($("#groupTriggerName").attr("value"));
        	var jobClassName = /^[0-9a-zA-Z_-]+$/.test($("#jobClassName").attr("value"));
        	var cronExpression = /^[0-9a-zA-Z_-]+$/.test($("#cronExpression").attr("value"));
        	
            if(!jobName){
                var alertString = '<fmt:message key="console.setting.scheduler.error.label.jobNameInvalid"/>';
                $("#jobName").focus();
                alert(alertString);
            } else if (!groupJobName) {
            	var alertString = '<fmt:message key="console.setting.scheduler.error.label.groupJobNameInvalid"/>';
                $("#groupJobName").focus();
                alert(alertString);
            } else if (!triggerName) {
            	var alertString = '<fmt:message key="console.setting.scheduler.error.label.triggerNameInvalid"/>';
                $("#triggerName").focus();
                alert(alertString);
            } else if (!groupTriggerName) {
            	var alertString = '<fmt:message key="console.setting.scheduler.error.label.groupTriggerNameInvalid"/>';
                $("#groupTriggerName").focus();
                alert(alertString);
            } else{
                $("#createScheduler").submit();
            }
        }

        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
    </script>
<commons:popupFooter />
