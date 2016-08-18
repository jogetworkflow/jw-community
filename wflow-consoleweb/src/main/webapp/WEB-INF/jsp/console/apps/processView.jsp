<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="title"><fmt:message key="adminBar.label.app"/>: ${appDefinition.name}</c:set>
<commons:header title="${title}" />

<jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />

<div id="nav">
    <div id="nav-title">
        <jsp:include page="appTitle.jsp" flush="true" />
    </div>

    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="appSubMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
            <li><button id="launchDesigner" onclick="launchDesigner()"><fmt:message key="pbuilder.label.designProcess"/></button></li>
            <li><button id="uploadPackage" onclick="uploadPackage()"><fmt:message key="console.process.config.label.updateProcess"/></button></li>
            <li><button id="runProcess" onclick="runProcess()"><fmt:message key="console.process.config.label.startProcess"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <p><font class="ftl_label"><fmt:message key="console.app.process.common.label.name"/>:</font> &nbsp;&nbsp; <strong><c:out value="${process.name}"/>&nbsp;</strong></p>

        <div id="main-body-content">

            <div id="xpdlThumbnailLoading">
                <img src="${pageContext.request.contextPath}/images/v3/loading.gif">
                <fmt:message key="console.process.config.label.xpdlThumbnailLoading"/>
            </div>
            <div id="thumbnailDiv" style="display:block; overflow:hidden; height:250px">
                <a id="xpdlThumbnail" href="${pageContext.request.contextPath}/web/console/images/xpdl/${process.encodedId}" target="_blank"></a>
            </div>
            <div id="advancedView" style="display: none">
                <dl>
                    <dt><fmt:message key="console.app.package.common.label.id"/></dt>
                    <dd><c:out value="${process.packageId}"/>&nbsp;</dd>
                    <dt><fmt:message key="console.app.process.common.label.definitionId"/></dt>
                    <dd><c:out value="${process.id}"/>&nbsp;</dd>
                    <dt><fmt:message key="console.app.process.common.label.description"/></dt>
                    <dd><c:out value="${process.description}"/>&nbsp;</dd>
                    <dt><fmt:message key="console.process.config.label.linkToRunProcess"/></dt>
                    <dd>${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/web/client/app/<c:out value="${appId}"/>/<c:out value="${appVersion}"/>/process/<c:out value="${processIdWithoutVersion}"/>?start=true</dd>
                </dl>
            </div>
            <div class="form-buttons">
                <a href="#" id="showAdvancedInfo" onclick="showAdvancedInfo();return false"><fmt:message key="general.method.label.showAdditionalInfo"/></a>
                <a href="#" style="display: none" id="hideAdvancedInfo" onclick="hideAdvancedInfo();return false"><fmt:message key="general.method.label.hideAdditionalInfo"/></a>
            </div>

            <div id="processTabView" style="min-height: 200px">
                <ul>
                    <li class="selected"><a href="#participantList"><span><fmt:message key="console.process.config.label.mapParticipants"/></span></a></li>
                    <li><a href="#activityList"><span><fmt:message key="console.process.config.label.mapActivities"/></span></a></li>
                    <li><a href="#toolList"><span><fmt:message key="console.process.config.label.mapTools"/></span></a></li>
                    <li><a href="#variableList"><span><fmt:message key="console.process.config.label.variableList"/></span></a></li>
                </ul>
                <br><br>
                <div>

                    <div id="participantList">
                        <div class="tabSummary"><fmt:message key="console.process.config.label.mapParticipants.description"/></div>
                        <c:forEach var="participant" items="${participantList}" varStatus="rowCounter">
                            <c:set var="escapeXml" value="${true}"/>
                            <c:set var="participantUid" value="${processIdWithoutVersion}::${participant.id}"/>
                            <c:choose>
                                <c:when test="${rowCounter.last}">
                                    <c:set var="rowStyle" scope="page" value="special"/>
                                </c:when>
                                <c:when test="${rowCounter.count % 2 == 0}">
                                    <c:set var="rowStyle" scope="page" value="even"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="rowStyle" scope="page" value="odd"/>
                                </c:otherwise>
                            </c:choose>
                            <div class="main-body-row ${rowStyle}">
                                <span class="row-content" helpTitle="|||<fmt:message key="console.process.config.label.id"/>: <c:out value="${participant.id}"/>">
                                    <c:out value="${participant.name}"/>
                                </span>
                                <span class="row-content id">
                                    <font class="ftl_label"><fmt:message key="console.process.config.label.id"/> :</font> <c:out value="${participant.id}"/>
                                </span>
                                <span class="row-button">
                                    <input type="button" value="<fmt:message key="console.process.config.label.mapParticipants.addEditMapping"/>" onclick="addEditParticipant('<ui:escape value="${participant.id}" format="html;javascript"/>', '<ui:escape value="${participant.name}" format="html;javascript"/>')"/>
                                </span>
                                <div style="clear: both; padding-left: 1em; padding-top: 0.5em;">
                                    <div id="participant_${participant.id}" style="padding-left: 1em; padding-top: 0.5em;">
                                        <c:if test="${!empty participantMap[participantUid]}">
                                            <dl>
                                                <dt><fmt:message key="console.process.config.label.mapParticipants.type"/></dt>
                                                <dd><fmt:message key="console.process.config.label.mapParticipants.type.${participantMap[participantUid].type}"/></dd>
                                                <dt><fmt:message key="console.process.config.label.mapParticipants.value"/></dt>
                                                <dd>
                                                    <c:choose>
                                                        <c:when test="${participantMap[participantUid].type eq 'user' || participantMap[participantUid].type eq 'group'}">
                                                            <c:set var="participantValues" value="${fn:replace(participantMap[participantUid].value, ';', ',')}"/>
                                                            <c:forEach var="participantValue" items="${fn:split(participantValues, ',')}" varStatus="status">
                                                                <span class="participant-remove">
                                                                    <c:set var="participantDisplayName" value=""/>
                                                                    <c:if test="${!(status.last && status.index eq 0)}">
                                                                        <a onClick="participantRemoveMappingSingle(this, '${participantMap[participantUid].type}','<ui:escape value="${participant.id}" format="html;javascript"/>','${participantValue}')"> <img src="${pageContext.request.contextPath}/images/v3/cross-circle.png"/></a>
                                                                        </c:if>
                                                                        <c:choose>
                                                                            <c:when test="${participantMap[participantUid].type eq 'user'}">
                                                                                <c:set var="participantDisplayName" value="${usersMap[participantValue].username}"/>
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                <c:if test="${!empty groupsMap[participantValue]}">
                                                                                    <c:choose>
                                                                                        <c:when test="${!empty groupsMap[participantValue].name}">
                                                                                            <c:set var="participantDisplayName" value="${groupsMap[participantValue].name}"/>
                                                                                        </c:when>
                                                                                        <c:otherwise>
                                                                                            <c:set var="participantDisplayName" value="${participantValue}"/>
                                                                                        </c:otherwise>
                                                                                    </c:choose>
                                                                                </c:if>
                                                                            </c:otherwise>
                                                                        </c:choose>
                                                                        <c:choose>
                                                                            <c:when test="${!isExtDirectoryManager || empty participantDisplayName}">
                                                                                <c:if test="${empty participantDisplayName}">
                                                                                    <c:set var="participantDisplayName">
                                                                                    <span style="color:gray;">${participantValue} <fmt:message key="console.process.config.label.mapParticipants.unavailable"/></span>
                                                                                    </c:set>
                                                                                    <c:set var="escapeXml" value="${false}"/>
                                                                                </c:if>  
                                                                                <c:out value="${participantDisplayName}" escapeXml="${escapeXml}"/>
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                <a href="${pageContext.request.contextPath}/web/console/directory/${participantMap[participantUid].type}/view/${participantValue}."><c:out value="${participantDisplayName}"/></a>
                                                                            </c:otherwise>
                                                                    </c:choose>
                                                                </span>
                                                            </c:forEach>
                                                        </c:when>
                                                        <c:when test="${participantMap[participantUid].type eq 'hod' || participantMap[participantUid].type eq 'department'}">
                                                            <c:set var="participantDisplayName" value=""/>
                                                            <c:if test="${!empty departmentsMap[participantMap[participantUid].value]}">
                                                                <c:choose>
                                                                    <c:when test="${!empty departmentsMap[participantMap[participantUid].value].name}">
                                                                        <c:set var="participantDisplayName" value="${departmentsMap[participantMap[participantUid].value].name}"/>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <c:set var="participantDisplayName" value="${participantMap[participantUid].value}"/>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </c:if>
                                                            <c:choose>
                                                                <c:when test="${!isExtDirectoryManager || empty participantDisplayName}">
                                                                    <c:if test="${empty participantDisplayName}">
                                                                        <c:set var="participantDisplayName">
                                                                            <span style="color:gray;">${participantMap[participantUid].value} <fmt:message key="console.process.config.label.mapParticipants.unavailable"/></span>
                                                                        </c:set>
                                                                        <c:set var="escapeXml" value="${false}"/>
                                                                    </c:if>  
                                                                        <c:out value="${participantDisplayName}" escapeXml="${escapeXml}"/>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <a href="${pageContext.request.contextPath}/web/console/directory/dept/view/${participantMap[participantUid].value}."><c:out value="${participantDisplayName}"/></a>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </c:when>
                                                        <c:when test="${participantMap[participantUid].type eq 'workflowVariable'}">
                                                            <font class="ftl_label"><fmt:message key="console.app.process.common.label.variableId"/> :</font> <c:out value="${fn:substring(participantMap[participantUid].value, 0, fn:indexOf(participantMap[participantUid].value, ','))}"/><br/>
                                                            <fmt:message key="console.process.config.label.mapParticipants.variable.${fn:substring(participantMap[participantUid].value, fn:indexOf(participantMap[participantUid].value, ',')+1, -1)}"/>
                                                        </c:when>
                                                        <c:when test="${participantMap[participantUid].type eq 'requester' || participantMap[participantUid].type eq 'requesterHod' || participantMap[participantUid].type eq 'requesterSubordinates' || participantMap[participantUid].type eq 'requesterDepartment' || participantMap[participantUid].type eq 'requesterHodIgnoreReportTo'}">
                                                            <c:choose>
                                                                <c:when test="${participantMap[participantUid].value ne ''}">
                                                                    <font class="ftl_label"><fmt:message key="console.app.activity.common.label.definitionId"/> :</font>
                                                                    <a href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}?tab=activityList&activityDefId=<c:out value="${participantMap[participantUid].value}"/>"><c:out value="${participantMap[participantUid].value}"/></a>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <fmt:message key="console.process.config.label.mapParticipants.previousActivity"/>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </c:when>
                                                        <c:when test="${participantMap[participantUid].type eq 'plugin'}">
                                                            ${participantPluginMap[participantMap[participantUid].value].i18nLabel} (<fmt:message key="console.plugin.label.version"/> ${participantPluginMap[participantMap[participantUid].value].version})
                                                        </c:when>
                                                        <c:when test="${participantMap[participantUid].type eq 'role'}">
                                                            <fmt:message key="console.process.config.label.mapParticipants.role.${participantMap[participantUid].value}"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            ${participantMap[participantUid].value}
                                                        </c:otherwise>
                                                    </c:choose>
                                                </dd>
                                                <dt>&nbsp;</dt>
                                                <dd>
                                                    <div>
                                                        <input type="button" class="smallbutton" value="<fmt:message key="console.process.config.label.mapParticipants.removeMapping"/>" onclick="participantRemoveMapping('<ui:escape value="${participant.id}" format="html;javascript"/>')"/>
                                                        <c:if test="${participantMap[participantUid].type eq 'plugin'}">
                                                            <input type="button" class="smallbutton" value="<fmt:message key="general.method.label.configPlugin"/>" onclick="participantConfigurePlugin('<ui:escape value="${participant.id}" format="html;javascript"/>', '<ui:escape value="${participant.name}" format="html;javascript"/>')"/>
                                                        </c:if>
                                                    </div>
                                                </dd>
                                            </dl>
                                        </c:if>
                                        <c:if test="${empty participantMap[participantUid] && participant.id eq 'processStartWhiteList'}">
                                            <dl>
                                                <dt><fmt:message key="console.process.config.label.mapParticipants.type"/></dt>
                                                <dd><fmt:message key="console.process.config.label.mapParticipants.type.role"/></dd>
                                                <dt><fmt:message key="console.process.config.label.mapParticipants.value"/></dt>
                                                <dd><fmt:message key="console.process.config.label.mapParticipants.role.everyone"/></dd>
                                            </dl>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <div id="activityList" style="height_: 300px; overflow-y_: scroll">
                        <div class="tabSummary"><fmt:message key="console.process.config.label.mapActivities.description"/></div>
                        <c:forEach var="activity" items="${activityList}" varStatus="rowCounter">
                            <c:set var="activityUid" value="${processIdWithoutVersion}::${activity.id}"/>
                            <c:if test="${activity.type == 'normal'}">
                                <c:choose>
                                    <c:when test="${rowCounter.last}">
                                        <c:set var="rowStyle" scope="page" value="special"/>
                                    </c:when>
                                    <c:when test="${rowCounter.count % 2 == 0}">
                                        <c:set var="rowStyle" scope="page" value="even"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="rowStyle" scope="page" value="odd"/>
                                    </c:otherwise>
                                </c:choose>

                                <div class="main-body-row ${rowStyle}" style="min-height: 50px">
                                    <span class="row-content" helpTitle="|||<fmt:message key="console.process.config.label.id"/>: <c:out value="${activity.id}" escapeXml="true"/>">
                                        <c:set var="activityDisplayName" value="${activity.name}"/>
                                        <c:if test="${empty activity.name}">
                                            <c:set var="activityDisplayName" value="${activity.id}"/>
                                        </c:if>
                                        <c:out value="${activityDisplayName}"/> <c:if test="${activity.type ne 'normal'}">(${activity.type})</c:if>
                                        </span>
                                        <span class="row-content id">
                                            <font class="ftl_label"><fmt:message key="console.process.config.label.id"/> :</font> <c:out value="${activity.id}"/>
                                    </span>
                                    <span class="row-button">
                                        <input type="button" value="<fmt:message key="console.process.config.label.mapActivities.addEditFormMapping"/>" onclick="addEditForm('<ui:escape value="${activity.id}" format="html;javascript"/>', '<ui:escape value="${activityDisplayName}" format="html;javascript"/>')"/>
                                    </span>
                                    <div style="clear: both; padding-left: 1em; padding-top: 0.5em;">
                                        <div id="activityForm_${activity.id}" style="padding-left: 1em; padding-top: 0.5em;">
                                            <c:set var="activityForm" value="${activityFormMap[activityUid]}"/>
                                            <c:set var="form" value="${formMap[activityUid]}"/>
                                            <c:if test="${!empty activityForm && form != null}">
                                                <dl>
                                                    <dt><fmt:message key="console.form.common.label.name"/></dt>
                                                    <dd><a href="#" onclick="launchFormBuilder('${form.id}');return false;"><c:out value="${form.name}"/></a></dd>
                                                    <dt>&nbsp;</dt>
                                                    <dd><div><input type="button" class="smallbutton" value="<fmt:message key="console.process.config.label.mapActivities.removeMapping"/>" onclick="activityRemoveForm('<ui:escape value="${activity.id}" format="html;javascript"/>')"/></div></dd>
                                                </dl>
                                                <c:if test="${activity.id ne 'runProcess'}">
                                                    <dl>
                                                        <dt>&nbsp;</dt>
                                                        <c:set var="disableSaveAsDraft" value=""/>
                                                        <c:if test="${activityForm.disableSaveAsDraft}">
                                                            <c:set var="disableSaveAsDraft" value="checked"/>
                                                        </c:if>
                                                        <dd><input type="checkbox" name="disableSaveAsDraft" ${disableSaveAsDraft} onchange="toggleDisableSaveAsDraft('<c:out value="${processIdWithoutVersion}"/>','<ui:escape value="${activity.id}" format="html;javascript"/>', this)"> <fmt:message key="console.process.config.label.mapActivities.disableSaveAsDraft"/></dd>
                                                    </dl>
                                                </c:if>
                                            </c:if>
                                            <c:if test="${!empty activityForm && activityForm.type == 'EXTERNAL'}">
                                                <dl>
                                                    <dt><fmt:message key="console.process.config.label.mapActivities.formExternal"/></dt>
                                                    <dd><a target="_blank" href="${activityForm.formUrl}">${activityForm.formUrl}</a></dd>
                                                    <dt>&nbsp;</dt>
                                                    <dd><div><input type="button" class="smallbutton" value="<fmt:message key="console.process.config.label.mapActivities.removeMapping"/>" onclick="activityRemoveForm('<ui:escape value="${activity.id}" format="html;javascript"/>')"/></div></dd>
                                                </dl>
                                            </c:if>
                                        </div>
                                        <div style="padding-left: 1em;">
                                            <dl>
                                                <dt>&nbsp;</dt>
                                                <c:set var="showNext" value=""/>
                                                <c:if test="${!empty activityForm && activityForm.autoContinue}">
                                                    <c:set var="showNext" value="checked"/>
                                                </c:if>
                                                <dd><input type="checkbox" name="showNextAssigment" ${showNext} onchange="toggleContinueNextAssignment('<c:out value="${processIdWithoutVersion}"/>','<ui:escape value="${activity.id}" format="html;javascript"/>', this)"> <fmt:message key="console.process.config.label.mapActivities.showContinueAssignment"/></dd>
                                            </dl>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>

                    <div id="toolList" style="height_: 300px; overflow-y_: scroll">
                        <div class="tabSummary"><fmt:message key="console.process.config.label.mapTools.description"/></div>
                        <c:forEach var="activity" items="${activityList}" varStatus="rowCounter">
                            <c:set var="activityUid" value="${processIdWithoutVersion}::${activity.id}"/>
                            <c:if test="${activity.type == 'tool'}">
                                <c:choose>
                                    <c:when test="${rowCounter.count % 2 == 0}">
                                        <c:set var="rowStyle" scope="page" value="even"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="rowStyle" scope="page" value="odd"/>
                                    </c:otherwise>
                                </c:choose>

                                <div class="main-body-row ${rowStyle}" style="min-height: 50px">
                                    <span class="row-content" helpTitle="|||<fmt:message key="console.process.config.label.id"/>: <c:out value="${activity.id}"/>">
                                        <c:set var="activityDisplayName" value="${activity.name}"/>
                                        <c:if test="${empty activity.name}">
                                            <c:set var="activityDisplayName" value="${activity.id}"/>
                                        </c:if>
                                        <c:out value="${activityDisplayName}"/>
                                    </span>
                                    <span class="row-content id">
                                        <font class="ftl_label"><fmt:message key="console.process.config.label.id"/> :</font> <c:out value="${activity.id}"/>
                                    </span>
                                    <span class="row-button">
                                        <input type="button" value="<fmt:message key="console.process.config.label.mapTools.addEditMapping"/>" onclick="addEditPlugin('<ui:escape value="${activity.id}" format="html;javascript"/>', '<ui:escape value="${activityDisplayName}" format="html;javascript"/>')"/>
                                    </span>
                                    <div style="clear: both; padding-left: 1em; padding-top: 0.5em;">
                                        <div id="activityForm_${activity.id}" style="padding-left: 1em; padding-top: 0.5em;">
                                            <c:set var="plugin" value="${pluginMap[activityUid]}"/>
                                            <c:if test="${plugin ne null}">
                                                <dl>
                                                    <dt><fmt:message key="console.plugin.label.name"/></dt>
                                                    <dd>${plugin.i18nLabel}&nbsp;</dd>
                                                    <dt><fmt:message key="console.plugin.label.version"/></dt>
                                                    <dd>${plugin.version}&nbsp;</dd>
                                                    <dt>&nbsp;</dt>
                                                    <dd>
                                                        <div>
                                                            <input type="button" class="smallbutton" value="<fmt:message key="console.process.config.label.mapTools.removePlugin"/>" onclick="activityRemovePlugin('<ui:escape value="${activity.id}" format="html;javascript"/>')"/>
                                                            <input type="button" class="smallbutton" value="<fmt:message key="general.method.label.configPlugin"/>" onclick="activityConfigurePlugin('<ui:escape value="${activity.id}" format="html;javascript"/>', '<ui:escape value="${activityDisplayName}" format="html;javascript"/>')"/>
                                                        </div>
                                                    </dd>
                                                </dl>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>

                    <div id="variableList">
                        <div class="tabSummary"><fmt:message key="console.process.config.label.variableList.description"/></div>
                        <c:forEach var="variable" items="${variableList}" varStatus="rowCounter">
                            <c:choose>
                                <c:when test="${rowCounter.count % 2 == 0}">
                                    <c:set var="rowStyle" scope="page" value="even"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="rowStyle" scope="page" value="odd"/>
                                </c:otherwise>
                            </c:choose>

                            <div class="main-body-row ${rowStyle}" style="min-height: 50px">
                                <span class="row-content">
                                    ${variable.id}<br>
                                </span>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>

        <div style="display:none">
            <div id="updateInformation" >
                <div style="height: 100px; width: 500px; margin:0" class="dialog">
                    <p>
                        <fmt:message key="console.process.config.label.update.message"/>
                    </p>
                    <div style="text-align:center">
                        <button onclick="window.location='${pageContext.request.contextPath}/web/console/app/${appId}//processes';return false;"><fmt:message key="general.method.label.ok"/></button>
                        <button id="closeInfo"><fmt:message key="general.method.label.cancel"/></button>
                    </div>
                </div>
            </div>
        </div>

        <script>
            var retry = 0;
            
            function remove_generator() {
                $("#xpdl_images_generator").remove();
            }
            
            function renderProcess() {
                if ($("#xpdl_images_generator").length === 0) {
                    retry++;
                    // create invisible iframe for canvas
                    var iframe = document.createElement('iframe');
                    var iwidth = 1024;
                    var iheight = 0;
                    $(iframe).attr("id", "xpdl_images_generator");
                    $(iframe).attr("src", "${pageContext.request.contextPath}/web/console/app/${appDefinition.id}/process/screenshot/${process.encodedId}?callback=remove_generator");
                    $(iframe).css({
                        'visibility':'hidden'
                    }).width(iwidth).height(iheight);
                    $(document.body).append(iframe);
                }
            }
            
            function loadImage() {
                var image = new Image();
                image.src = "${pageContext.request.contextPath}/web/console/images/xpdl/${process.encodedId}?rnd=" + new Date().valueOf().toString();
                $(image).load(function(){
                    $('#xpdlThumbnail').append(image);
                    $(image).each(function() {
                        var maxWidth = 600; // Max width for the image
                        var maxHeight = 250;    // Max height for the image
                        var ratio = 0;  // Used for aspect ratio
                        var width = $(this).width();    // Current image width
                        var height = $(this).height();  // Current image height

                        // Check if the current width is larger than the max
                        if(width > maxWidth){
                            ratio = maxWidth / width;   // get ratio for scaling image
                            $(this).css("width", maxWidth); // Set new width
                            $(this).css("height", height * ratio);  // Scale height based on ratio
                            height = height * ratio;    // Reset height to match scaled image
                            width = width * ratio;    // Reset width to match scaled image
                        }

                        // Check if current height is larger than max
                        if(height > maxHeight){
                            ratio = maxHeight / height; // get ratio for scaling image
                            $(this).css("height", maxHeight);   // Set new height
                            $(this).css("width", width * ratio);    // Scale width based on ratio
                            width = width * ratio;    // Reset width to match scaled image
                        }
                    });
                    $('#xpdlThumbnailLoading').hide();
                });
            
                $(image).error(function(){
                    renderProcess();
                    if (retry <= 3) {
                        setTimeout(function() { loadImage(); }, 5000);
                    }
                });
            }

            $(document).ready(function() {
                loadImage();
                /*$('span.row-content[@helpTitle]').cluetip({
                    splitTitle: '|||',
                    showTitle: false,
                    arrows: true,
                    positionBy: 'mouse',
                    dropShadow: false,
                    hoverIntent: false,
                    sticky: true,
                    mouseOutClose: true,
                    closePosition: 'title'}
            );*/
            <c:if test="${!empty param.activityDefId}">
                    setTimeout(function() {
                        var topy = $("#activityForm_<ui:escape value="${param.activityDefId}" format="html;javascript"/>").offset().top - 100;
                        topy = parseInt(topy);
                        window.scrollTo(0, topy);
                    }, 100);
            </c:if>
            <c:if test="${!empty param.participantId}">
                    setTimeout(function() {
                        var topy = $("#participant_<ui:escape value="${param.participantId}" format="html;javascript"/>").offset().top - 100;
                        topy = parseInt(topy);
                        window.scrollTo(0, topy);
                    }, 100);
            </c:if>
                });

                var tabView = new TabView('processTabView', 'top');
                tabView.init();
            <c:if test="${!empty param.tab}">
                tabView.select('#<ui:escape value="${param.tab}" format="html;javascript"/>');
            </c:if>
            <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/form/edit/${form.id}"/>

                var reloadCallback = {
                    success: function(data){
                        if (data && data.length > 0) {
                            data = data.trim();
                            $("#" + data).css("display", "none");
                            return;
                        }

                        //get current selected tab
                        var selectedTabId = $('#processTabView .ui-tabs-selected a').attr('href');
                        selectedTabId = selectedTabId.replace('#', '');

                        var urlQueryString = document.location.search;
                        if(urlQueryString == ''){
                            document.location.href = document.location.href + "?tab=" + selectedTabId;
                        }else{
                            if(urlQueryString.indexOf('tab') == -1){
                                document.location.href = document.location.href + "&tab=" + selectedTabId;
                            }else{
                                document.location.href = document.location.href.replace(urlQueryString, '') + "?tab=" + selectedTabId;
                            }
                        }
                    }
                }

                function closeDialog() {
                    popupDialog.close();
                }

                function uploadPackage(){
                    popupDialog.src = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/package/upload";
                    popupDialog.init();
                }

                function launchDesigner(){
                    $("#updateInformation").dialog({modal:true, height:150, width:550, resizable:false, show: 'slide',overlay: {opacity: 0.5, background: "black"},zIndex: 15001});
                    $("#closeInfo").click(function(){$("#updateInformation").dialog("close")});
                    window.open("${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/process/builder?processId=<ui:escape value="${process.idWithoutVersion}" format="u"/>");
                }

                function launchFormBuilder(formId) {
                    window.open("${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/form/builder/" + formId);
                }

                function runProcess() {
                    var url = "${pageContext.request.contextPath}/web/client/app/${appId}/${appVersion}/process/<c:out value="${processIdWithoutVersion}"/>";
                    popupDialog.src = url;
                    popupDialog.init();
                    //                window.open(url, "_blank", "");
                }

                function addEditForm(activityId, activityName){
                    popupDialog.src = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/" + escape(activityId) + "/form?activityName=" + encodeURIComponent(activityName) ;
                    popupDialog.init();
                }

                function addEditPlugin(activityId, activityName){
                    popupDialog.src = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/" + escape(activityId) + "/plugin?activityName=" + encodeURIComponent(activityName);
                    popupDialog.init();
                }

                function addEditParticipant(participantId, participantName){
                    popupDialog.src = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/participant/" + participantId + "?participantName=" + encodeURIComponent(participantName);
                    popupDialog.init();
                }

                function activityRemoveForm(activityId){
                    if (confirm("<fmt:message key="console.process.config.label.mapActivities.removeMapping.confirm"/>")) {
                        var url = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/" + escape(activityId) + "/form/remove";
                        ConnectionManager.post(url, reloadCallback);
                    }
                }

                function activityRemovePlugin(activityId){
                    if (confirm("<fmt:message key="console.process.config.label.mapTools.removePlugin.confirm"/>")) {
                        var url = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/" + escape(activityId) + "/plugin/remove";
                        ConnectionManager.post(url, reloadCallback);
                    }
                }

                function activityConfigurePlugin(activityId, activityName){
                    var title = " - " + activityName + " (" + activityId + ")";
                    popupDialog.src = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/" + escape(activityId) + "/plugin/configure?title=" + encodeURIComponent(title);
                    popupDialog.init();
                }

                function participantRemoveMapping(participantId){
                    var removeItem = {
                        success : function(response) {
                            if (participantId === 'processStartWhiteList') {
                                document.location.href = '${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/<c:out value="${processIdWithoutVersion}"/>?tab=participantList&participantId=processStartWhiteList';
                            } else {
                                $('#participant_'+participantId).html('');
                            }
                        }
                    }
                    
                    if (confirm("<fmt:message key="console.process.config.label.mapParticipants.removeMapping.confirm"/>")) {
                        ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/participant/' + participantId + '/remove', removeItem, '');
                    }
                }

                function participantRemoveMappingSingle(obj, type, participantId, value){
                    var removeItem = {
                        success : function(response) {
                            $(obj).parent().hide('slow');
                            if($(obj).parent().parent().find('.participant-remove:visible').length == 2){
                                $(obj).parent().parent().find('.participant-remove:visible').find('img').remove();
                            }
                        }
                    }
                    
                    if (confirm("<fmt:message key="console.process.config.label.mapParticipants.removeMapping.confirm"/>")) {
                        ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/participant/' + participantId + '/remove', removeItem, 'type='+encodeURIComponent(type)+'&value='+encodeURIComponent(value));
                    }
                }

                function participantConfigurePlugin(participantId, participantName){
                    var title = " - " + participantName + " (" + participantId + ")";
                    popupDialog.src = '${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/participant/'+participantId+'/plugin/configure?title=' + encodeURIComponent(title);
                    popupDialog.init();
                }

                function showAdvancedInfo(){
                    $('#advancedView').slideToggle('slow');
                    $('#showAdvancedInfo').hide();
                    $('#hideAdvancedInfo').show();
                }

                function hideAdvancedInfo(){
                    $('#advancedView').slideToggle('slow');
                    $('#showAdvancedInfo').show();
                    $('#hideAdvancedInfo').hide();
                }

                function showXpdlImage(){
                    popupDialog.src = "${pageContext.request.contextPath}/web/console/images/xpdl/${process.encodedId}";
                    popupDialog.init();

                }

                var autoCallback = {
                    success: function() {
                        // do nothing
                    }
                }

                function toggleContinueNextAssignment(processDefId, activityDefId, checkbox){
                    var params = "auto="+$(checkbox).is(':checked');
                    ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/' + processDefId + '/activity/' + activityDefId + '/continue', autoCallback, params);
                }
                
                function toggleDisableSaveAsDraft(processDefId, activityDefId, checkbox){
                    var params = "disable="+$(checkbox).is(':checked');
                    ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/' + processDefId + '/activity/' + activityDefId + '/draft', autoCallback, params);
                }
        </script>

    </div>
</div>
    
<script>
    Template.init("#menu-apps", "#nav-app-processes");
    <c:choose>
        <c:when test="${param.tab == 'toolList'}">
            HelpGuide.key = "help.web.console.app.processes.view.run";
        </c:when>
        <c:when test="${param.tab == 'activityList'}">
            HelpGuide.key = "help.web.console.app.processes.view.tools";
        </c:when>
        <c:when test="${param.tab == 'participantList'}">
            HelpGuide.key = "help.web.console.app.processes.view.forms";
        </c:when>
        <c:otherwise>
            HelpGuide.key = "help.web.console.app.processes.view";
        </c:otherwise>
    </c:choose>
</script>
    
<commons:footer />
