<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<style>
    .runProcess-body-content{
        padding:10px;
    }
    .runProcess-body-header{
        font-size:16px;
        font-weight:bold;
        padding:10px 0px;
    }
    .runProcess-body-message{
        font-size:20px;
        line-height:15px;
        text-align:center;
        text-shadow:1px 1px 1px #767676;
    }
    .runProcess-body-submessage {
        font-size:16px;
        line-height:15px;
        text-align:center;
        text-shadow:none;
    }
    .runProcess-body-message button {
        -moz-border-radius: 4px;
        -webkit-border-radius: 4px;
        padding: 10px 20px;
        margin: 20px;
        font-size: 20px;
        cursor: pointer;
        border-radius: 4px; }
</style>
<c:if test="${requestParameters.isPreview eq 'true'}">
    <script>
        $(document).ready(function() {
            $(".form-button, .form-button-large").attr("disabled", "disabled");
        });
    </script>
</c:if>

<div class="runProcess-body-content">
    <div class="runProcess-body-header">
        <c:out value="${properties.headerTitle}"/>
    </div>
<c:choose>
    <c:when test="${properties.view eq 'unauthorized' or properties.view eq 'featureDisabled'}">
        <p>
            <fmt:message key="general.content.unauthorized" />
        </p>
    </c:when>
    <c:when test="${properties.view eq 'processDetail'}">
        <p>&nbsp;</p>
        <form id="processForm" name="processForm" method="POST" action="${properties.startUrl}">
            <div class="runProcess-body-message">
                <c:out value="${properties.processName}"/>
                <p class="runProcess-body-submessage"><c:out value="${properties.process.packageName}"/></p>
                <button onclick="return startProcess()" class="form-button"><fmt:message key="client.app.run.process.label.start"/></button>
            </div>
        </form>
        <script>
            function startProcess(){
                if(confirm('<ui:msgEscJS key="client.app.run.process.label.start.confirm"/>')){
                    return true;
                }
                else {
                    return false;
                }
            }
        </script>
    </c:when>
    <c:when test="${properties.view eq 'processFormPost'}">
        <p>&nbsp;</p>
        <form id="processForm" name="processForm" method="POST" action="${properties.startUrl}&${properties.csrfToken}"></form>
        <script>$("#processForm").submit();</script>
    </c:when>
    <c:when test="${properties.view eq 'formView'}">
        <c:set var="appDef" scope="request" value="${properties.appDef}"/>
        <c:set var="assignment" scope="request" value="${properties.assignment}"/>
        <c:set var="activityForm" scope="request" value="${properties.activityForm}"/>
        <c:set var="formHtml" scope="request" value="${properties.formHtml}"/>
        <c:set var="stay" scope="request" value="${properties.stay}"/>
        <c:set var="errorCount" scope="request" value="${properties.errorCount}"/>
        <c:set var="submitted" scope="request" value="${properties.submitted}"/>
        <c:set var="closeDialog" scope="request" value="${properties.closeDialog}"/>
        <jsp:include page="../../client/app/formView.jsp" flush="true" />
    </c:when>
    <c:when test="${properties.view eq 'processStarted'}">
        <p>
            <c:choose>
                <c:when test="${!empty properties.messageShowAfterComplete}">
                    ${properties.messageShowAfterComplete}
                </c:when>
                <c:otherwise>
                    <fmt:message key="client.app.run.process.label.start.success" />
                </c:otherwise>
            </c:choose>
        </p>
    </c:when>
    <c:when test="${properties.view eq 'assignmentUpdated'}">
        <p>
            <c:choose>
                <c:when test="${!empty properties.messageShowAfterComplete}">
                    ${properties.messageShowAfterComplete}
                </c:when>
                <c:otherwise>
                    <fmt:message key="client.app.run.process.label.assignment.updated" />
                </c:otherwise>
            </c:choose>
        </p>
    </c:when>
    <c:when test="${properties.view eq 'assignmentUnavailable'}">
        <p>
            <fmt:message key="client.app.run.process.label.assignment.unavailable" />
        </p>
    </c:when>
    <c:when test="${properties.view eq 'noProcess'}">
        <p>
            <fmt:message key="client.app.run.process.label.process.unavailable" />
        </p>
    </c:when>    
</c:choose>
</div>
<div style="clear:both;"></div>




