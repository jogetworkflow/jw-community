<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<commons:header />

<div id="nav">
    <div id="nav-title">

    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="subMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
            <c:if test="${activity.state == 'open.not_running.not_started'}">
                <li><button onclick="reevaluate()"><fmt:message key="console.monitoring.running.label.reevaluate"/></button></li>
                <li><button onclick="showReevaluateForUser()"><fmt:message key="console.monitoring.running.label.reevaluateForUser"/></button></li>
            </c:if>
            <c:if test="${activity.state != 'closed.completed' && activity.state != 'closed.terminated' && activity.state != 'closed.aborted'}">
                <li><button onclick="reassign()"><fmt:message key="console.monitoring.running.label.reassign"/></button></li>
                <li><button onclick="completeActivity()"><fmt:message key="console.monitoring.running.label.complete"/></button></li>
            </c:if>
            <c:if test="${!empty formId}">
                <li><button onclick="viewForm()"><fmt:message key="console.monitoring.running.label.viewForm"/></button></li>
            </c:if>
        </ul>
    </div>
    <div id="main-body">
        <dl>
            <dt><fmt:message key="console.app.activity.common.label.name"/></dt>
            <dd>${activity.name}&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.state"/></dt>
            <dd>${activity.state}&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.serviceLevelMonitor"/></dt>
            <dd>${serviceLevelMonitor}&nbsp;</dd>

            <c:if test="${trackWflowActivity.status == 'Pending'}">
                <dt><fmt:message key="console.app.activity.common.label.listOfPending"/></dt>
                <dd>
                    <c:choose>
                        <c:when test="${assignUserSize > 1}">
                            <c:forEach var="assignmentUser" items="${trackWflowActivity.assignmentUsers}" varStatus="index">
                                    <c:choose>
                                        <c:when test="${index.count < assignUserSize}">
                                            <span><c:out value="${assignmentUser}, "/></span>
                                        </c:when>
                                        <c:otherwise>
                                            <span><c:out value="${assignmentUser}"/></span>
                                        </c:otherwise>
                                    </c:choose>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="assignmentUser" items="${trackWflowActivity.assignmentUsers}">
                                ${assignmentUser}
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                &nbsp;</dd>
            </c:if>


            <c:if test="${trackWflowActivity.status != 'Pending'}">
                <dt><fmt:message key="console.app.activity.common.label.acceptedUser"/></dt>
                <dd>${trackWflowActivity.nameOfAcceptedUser}&nbsp;</dd>
            </c:if>

            <dt><fmt:message key="console.app.activity.common.label.priority"/></dt>
            <dd>${trackWflowActivity.priority}&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.createdTime"/></dt>
            <dd>${trackWflowActivity.createdTime}&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.startedTime"/></dt>
            <dd>${trackWflowActivity.startedTime}&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.dateLimit"/></dt>
            <dd>${trackWflowActivity.limit}&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.dueDate"/></dt>
            <dd>${trackWflowActivity.due}&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.delay"/></dt>
            <dd>${trackWflowActivity.delay}&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.finishTime"/></dt>
            <dd>${trackWflowActivity.finishTime}&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.timeConsumingFromDateCreated"/></dt>
            <dd>${trackWflowActivity.timeConsumingFromDateCreated}&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.timeConsumingFromDateStarted"/><dt>
            <dd>${trackWflowActivity.timeConsumingFromDateStarted}&nbsp;</dd>
        </dl>
        <div id="reevaluateForUser" style="display: none">
            <div style="width:650px;margin:20px auto;">
                <h3><fmt:message key="console.monitoring.running.label.reevaluateForUser"/></h3>
                <p><fmt:message key="console.monitoring.running.label.reevaluateForUser.select"/>
                    <select id="reevaluateUser">
                        <c:forEach var="assignmentUser" items="${trackWflowActivity.assignmentUsers}">
                            <option>${assignmentUser}</option>
                        </c:forEach>
                    </select>
                </p>
                <p><input id="reevaluateForUserSubmit" class="form-button" type="button" value="<fmt:message key="console.monitoring.running.label.reevaluateForUser.submit"/>" onclick="reevaluateForUser()"/></p>
            </div>
        </div>
        <div id="main-body-content-subheader">
            <fmt:message key="console.app.activity.common.label.variableList"/>
        </div>
        <c:forEach var="variable" items="${variableList}" varStatus="rowCounter">
            <div class="form-row">
                <label for="${variable.name}">${variable.name}</label>
                <span class="input">
                    <c:choose>
                        <c:when test="${activity.state != 'closed.completed' && activity.state != 'closed.terminated' && activity.state != 'closed.aborted'}">
                            <input name="${variable.name}" type="text" id="${variable.name}" value="${variable.val}"/>
                            <input type="button" value="<fmt:message key="general.method.label.set"/>" onclick="setVariable('${variable.name}')"/>
                        </c:when>
                        <c:otherwise>
                            <input name="${variable.name}" type="text" id="${variable.name}" value="${variable.val}" disabled="true"/>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
        </c:forEach>
    </div>
</div>

<script>
    <ui:popupdialog var="popupDialog" src=""/>

    var callback = {
        success: function(){
            window.location.reload(true);
        }
    }

    function reevaluate(){
        ConnectionManager.post("${pageContext.request.contextPath}/web/json/monitoring/activity/reevaluate", callback, "activityId=${activity.id}");
    }

    function setVariable(variable){
        var url = "${pageContext.request.contextPath}/web/json/monitoring/activity/variable/${activity.id}/" + escape(variable);
        var value = $('#' + variable).attr('value');

        ConnectionManager.post(url, callback, "value=" + escape(value));

    }

    function viewForm(){
        //var url = '${pageContext.request.contextPath}/web/formbuilder/view/${formId}?overlay=true&processId=${activity.processId}&activityId=${activity.id}';
        //popupDialog.src = url;
        //popupDialog.init();
    }


    function reassign(){
        popupDialog.src = "${pageContext.request.contextPath}/web/console/monitor/running/activity/reassign?state=${activity.state}&processDefId=${activity.encodedProcessDefId}&activityId=${activity.id}&processId=${activity.processId}";
        popupDialog.init();
    }

    function completeActivity(){
        ConnectionManager.post("${pageContext.request.contextPath}/web/monitoring/running/activity/complete", callback, "state=${activity.state}&processDefId=${activity.processDefId}&activityId=${activity.id}&processId=${activity.processId}");
    }

    function showReevaluateForUser(){
        $('#reevaluateForUser').dialog({
            modal: true,
            width: 860,
            height: 520,
            position: 'center',
            autoOpen: true,
            overlay: {
              opacity: 0.5,
              background: "black"
            },
            zIndex: 15001
        });
    }

    function reevaluateForUser(){
        $('#reevaluateForUserSubmit').attr('disabled', 'disabled');
        $('#reevaluateForUserSubmit').val('<fmt:message key="console.monitoring.running.label.reevaluateForUser.loading"/>')

        var url = "${pageContext.request.contextPath}/web/json/monitoring/user/reevaluate";
        var value = $('#reevaluateUser').val();

        ConnectionManager.post(url, callback, "username=" + escape(value));
    }

    Template.init("#menu-monitor", "#nav-monitor-${processStatus}");
</script>

<commons:footer />
