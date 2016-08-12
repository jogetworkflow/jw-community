<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="icon-dashboard"></i> <fmt:message key='console.header.menu.label.monitor'/></p>
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
            <dt><fmt:message key="console.app.process.common.label.instance"/></dt>
            <dd><a href="${pageContext.request.contextPath}/web/console/monitor/<c:out value="${processStatus}"/>/process/view/${activity.processId}"><c:out value="${activity.processId}"/></a>&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.id"/></dt>
            <dd><c:out value="${activity.id}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.definitionId"/></dt>
            <dd><c:out value="${activity.activityDefId}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.name"/></dt>
            <dd><c:out value="${activity.name}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.state"/></dt>
            <dd><c:out value="${activity.state}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.serviceLevelMonitor"/></dt>
            <dd><c:out value="${serviceLevelMonitor}" escapeXml="false"/>&nbsp;</dd>

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
                                <c:out value="${assignmentUser}"/>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                &nbsp;</dd>
            </c:if>

            <c:choose>
                <c:when test="${trackWflowActivity.type == 'subflow'}">
                    <dt><fmt:message key="console.app.activity.common.label.subflowId"/></dt>
                    <dd><a href="${pageContext.request.contextPath}/web/console/monitor/<c:out value="${processStatus}"/>/process/view/${trackWflowActivity.nameOfAcceptedUser}"><c:out value="${trackWflowActivity.nameOfAcceptedUser}"/></a>&nbsp;</dd>
                </c:when>
                <c:when test="${trackWflowActivity.status != 'Pending'}">
                    <dt><fmt:message key="console.app.activity.common.label.acceptedUser"/></dt>
                    <dd><c:out value="${trackWflowActivity.nameOfAcceptedUser}"/>&nbsp;</dd>
                </c:when>
            </c:choose>

            <dt><fmt:message key="console.app.activity.common.label.priority"/></dt>
            <dd><c:out value="${trackWflowActivity.priority}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.createdTime"/></dt>
            <dd><ui:dateToString date="${trackWflowActivity.createdTime}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.dateLimit"/></dt>
            <dd><c:out value="${trackWflowActivity.limit}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.dueDate"/></dt>
            <dd><ui:dateToString date="${trackWflowActivity.due}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.delay"/></dt>
            <dd><c:out value="${trackWflowActivity.delay}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.finishTime"/></dt>
            <dd><ui:dateToString date="${trackWflowActivity.finishTime}"/>&nbsp;</dd>
            <dt><fmt:message key="console.app.activity.common.label.timeConsumingFromDateCreated"/></dt>
            <dd><c:out value="${trackWflowActivity.timeConsumingFromDateCreated}"/>&nbsp;</dd>
        </dl>
        <div id="reevaluateForUser" style="display: none">
            <div class="popupBody">
                <div id="main-body-header"><fmt:message key="console.monitoring.running.label.reevaluateForUser"/></div>
                <div id="main-body-content">
                    <div class="form">
                        <fieldset>
                            <div class="form-row">
                                <label><fmt:message key="console.monitoring.running.label.reevaluateForUser.select"/></label>
                                <span class="form-input">
                                    <select id="reevaluateUser">
                                        <c:forEach var="assignmentUser" items="${trackWflowActivity.assignmentUsers}">
                                            <option><c:out value="${assignmentUser}"/></option>
                                        </c:forEach>
                                    </select>
                                </span>
                            </div>
                        </fieldset>
                        <div class="form-buttons">
                            <input id="reevaluateForUserSubmit" class="form-button" type="button" value="<fmt:message key="console.monitoring.running.label.reevaluateForUser.submit"/>" onclick="reevaluateForUser()"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div id="main-body-content-subheader">
            <fmt:message key="console.app.activity.common.label.variableList"/>
        </div>
        <c:forEach var="variable" items="${variableList}" varStatus="rowCounter">
            <div class="form-row">
                <label for="${variable.name}"><c:out value="${variable.name}"/></label>
                <span class="input">
                    <c:choose>
                        <c:when test="${activity.state != 'closed.completed' && activity.state != 'closed.terminated' && activity.state != 'closed.aborted'}">
                            <input name="<c:out value="${variable.name}"/>" type="text" id="<c:out value="${variable.name}"/>" value="<c:out value="${variable.val}"/>"/>
                            <input type="button" value="<fmt:message key="general.method.label.set"/>" onclick="setVariable('<c:out value="${variable.name}"/>')"/>
                        </c:when>
                        <c:otherwise>
                            <input name="<c:out value="${variable.name}"/>" type="text" id="<c:out value="${variable.name}"/>" value="<c:out value="${variable.val}"/>" disabled="true"/>
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
        ConnectionManager.post("${pageContext.request.contextPath}/web/json/monitoring/running/activity/complete", callback, "state=${activity.state}&processDefId=${activity.processDefId}&activityId=${activity.id}&processId=${activity.processId}");
    }

    function showReevaluateForUser(){
        var openDialog = function() {
            $(".ui-dialog.ui-widget").css("position", "fixed");
        }
      
        $('#reevaluateForUser').dialog({
            modal: true,
            width: 860,
            height: 520,
            position: 'center',
            autoOpen: true,
            draggable: false,
            resizable: false,
            open: openDialog,
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

    Template.init("#menu-monitor", "#nav-monitor-<c:out value="${processStatus}"/>");
</script>

<commons:footer />
