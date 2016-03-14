<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<style>
    .viewForm-body-content{
        padding:10px;
    }
    .viewForm-body-header{
        font-size:16px;
        font-weight:bold;
        padding:10px 0px;
    }
    .viewForm-body-message{
        font-size:20px;
        line-height:15px;
        text-align:center;
        text-shadow:1px 1px 1px #767676;
    }
    .viewForm-body-submessage {
        color:gray;
        font-size:16px;
        line-height:15px;
        text-align:center;
        text-shadow:none;
    }
    .viewForm-body-message button{
        padding:4px 10px;
        color:#565656;
        cursor:pointer;
        border:1px solid #799837;
        background:url("${requestParameters.contextPath}/plugin/org.joget.apps.userview.lib.viewForm/images/dropdown_btn_bg.gif") repeat-x scroll 0 0 #E7FAB6;
    }
    .viewForm-body-message button {
        border: 1px solid #799837;
        -moz-border-radius: 4px;
        -webkit-border-radius: 4px;
        padding: 4px 10px;
        background: #e7fab6 url("${requestParameters.contextPath}/plugin/org.joget.apps.userview.lib.viewForm/images/dropdown_btn_bg_hover.gif") 0 0 repeat-x;
        font-size: 12px;
        color: #565656;
        cursor: pointer;
        border-radius: 4px; }
    .viewForm-body-message button:hover {
        background: #e7fab6 url("${requestParameters.contextPath}/plugin/org.joget.apps.userview.lib.viewForm/images/dropdown_btn_bg.gif") 0 0 repeat-x;
    }
</style>
<c:if test="${requestParameters.isPreview eq 'true'}">
    <script>
        $(document).ready(function() {
            $(".form-button").attr("disabled", "disabled");
        });
    </script>
</c:if>
<div class="viewForm-body-content">
    <c:choose>
        <c:when test="${empty properties.customHeader && !empty properties.headerTitle}">
            <div class="viewForm-body-header">
                <c:out value="${properties.headerTitle}"/>
            </div>
        </c:when>
        <c:otherwise>
            ${properties.customHeader}
        </c:otherwise>
    </c:choose>
    <c:choose>
        <c:when test="${properties.view eq 'unauthorized'}">
            <p>
                <fmt:message key="general.body.unauthorized" />
            </p>
        </c:when>
        <c:when test="${properties.view eq 'formView'}">
            <c:set var="appDef" scope="request" value="${properties.appDef}"/>
            <c:set var="assignment" scope="request" value="${properties.assignment}"/>
            <c:set var="activityForm" scope="request" value="${properties.activityForm}"/>
            <c:set var="formHtml" scope="request" value="${properties.formHtml}"/>
            <c:set var="formJson" scope="request" value="${properties.formJson}"/>
            <c:set var="errorCount" scope="request" value="${properties.errorCount}"/>
            <c:set var="submitted" scope="request" value="${properties.submitted}"/>
            <c:set var="stay" scope="request" value="${properties.stay}"/>
            <c:set var="closeDialog" scope="request" value="${properties.closeDialog}"/>
            <jsp:include page="../../client/app/formView.jsp" flush="true" />
        </c:when>
        <c:when test="${properties.view eq 'formUnavailable'}">
            <p>
                <fmt:message key="client.app.run.process.label.form.unavailable" />
            </p>
        </c:when>
        <c:when test="${properties.view eq 'assignmentUpdated'}">
            <p>
                <fmt:message key="client.app.run.process.label.assignment.updated" />
            </p>
        </c:when>
    </c:choose>
    <c:if test="${!empty properties.showRecordTraveling && properties.showRecordTraveling}">
        <div id="recordTraveling" style="padding:10px 0">
            <c:if test="${!empty properties.firstRecordUrl}">
                <a id="firstRecord" href="${properties.firstRecordUrl}"><span>${properties.firstRecordLabel}</span></a>&nbsp;&nbsp;
            </c:if>
            <c:if test="${!empty properties.previousRecordUrl}">
                <a id="prevRecord" href="${properties.previousRecordUrl}"><span>${properties.previousRecordLabel}</span></a>&nbsp;&nbsp;
            </c:if>
            <span>${properties.recordPosition} / ${properties.totalRecord}</span>&nbsp;&nbsp;
            <c:if test="${!empty properties.nextRecordUrl}">
                <a id="nextRecord" href="${properties.nextRecordUrl}"><span>${properties.nextRecordLabel}</span></a>&nbsp;&nbsp;
            </c:if>
            <c:if test="${!empty properties.lastRecordUrl}">
                <a id="lastRecord" href="${properties.lastRecordUrl}"><span>${properties.lastRecordLabel}</span></a>&nbsp;&nbsp;
            </c:if>
        </div>
    </c:if>
    <c:if test="${!empty properties.customFooter}">
        ${properties.customFooter}
    </c:if>
</div>

<div style="clear:both;"></div>

<a class="print-button" href="#" onclick="userviewPrint();return false;"><fmt:message key="general.method.label.print" /></a>  
