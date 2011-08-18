<%@page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header />

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
            <li><button id="launchDesigner" onclick="launchDesigner()"><fmt:message key="console.process.config.label.launchDesigner"/></button></li>
            <li><button id="uploadPackage" onclick="uploadPackage()"><fmt:message key="console.process.config.label.updateProcess"/></button></li>
            <li><button id="runProcess" onclick="runProcess()"><fmt:message key="console.process.config.label.startProcess"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <p><font class="ftl_label"><fmt:message key="console.app.process.common.label.name"/>:</font> &nbsp;&nbsp; <strong>${process.name}&nbsp;</strong></p>

        <div id="main-body-content">

            <div id="xpdlThumbnailLoading">
                <img src="${pageContext.request.contextPath}/images/v3/loading.gif">
                <fmt:message key="console.process.config.label.xpdlThumbnailLoading"/>
            </div>
            <div id="thumbnailDiv" style="display:block; overflow:hidden">
                <a id="xpdlThumbnail" href="${pageContext.request.contextPath}/web/images/xpdl/${process.encodedId}" target="_blank"></a>
            </div>
            <div id="advancedView" style="display: none">
                <dl>
                    <dt><fmt:message key="console.app.package.common.label.id"/></dt>
                    <dd>${process.packageId}&nbsp;</dd>
                    <dt><fmt:message key="console.app.process.common.label.definitionId"/></dt>
                    <dd>${process.id}&nbsp;</dd>
                    <dt><fmt:message key="console.app.process.common.label.description"/></dt>
                    <dd>${process.description}&nbsp;</dd>
                    <dt><fmt:message key="console.process.config.label.linkToRunProcess"/></dt>
                    <dd>${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/web/client/app/${appId}/${appVersion}/process/${processIdWithoutVersion}/start</dd>
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
                                <span class="row-content" helpTitle="|||<fmt:message key="console.process.config.label.id"/>: ${participant.id}">
                                    ${participant.name}
                                </span>
                                <span class="row-content id">
                                    <font class="ftl_label"><fmt:message key="console.process.config.label.id"/> :</font> ${participant.id}
                                </span>
                                <span class="row-button">
                                    <input type="button" value="<fmt:message key="console.process.config.label.mapParticipants.addEditMapping"/>" onclick="addEditParticipant('${participant.id}')"/>
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
                                                            <c:forEach var="participantValue" items="${fn:split(participantMap[participantUid].value, ',')}" varStatus="status">
                                                                <span class="participant-remove">
                                                                    <c:if test="${!(status.last && status.index eq 0)}">
                                                                        <a onClick="participantRemoveMappingSingle(this, '${participantMap[participantUid].type}','${participant.id}','${participantValue}')"> <img src="${pageContext.request.contextPath}/images/v3/cross-circle.png"/></a>
                                                                    </c:if>
                                                                    <c:choose>
                                                                        <c:when test="${participantMap[participantUid].type eq 'user'}">
                                                                            <c:set var="participantDisplayName" value="${usersMap[participantValue].username}"/>
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <c:set var="participantDisplayName" value="${groupsMap[participantValue].name}"/>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                    <c:choose>
                                                                        <c:when test="${!isExtDirectoryManager}">
                                                                            ${participantDisplayName}
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <a href="${pageContext.request.contextPath}/web/console/directory/${participantMap[participantUid].type}/view/${participantValue}">${participantDisplayName}</a>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </span>
                                                            </c:forEach>
                                                        </c:when>
                                                        <c:when test="${participantMap[participantUid].type eq 'hod' || participantMap[participantUid].type eq 'department'}">
                                                            <c:set var="participantDisplayName" value="${departmentsMap[participantMap[participantUid].value].name}"/>
                                                            <a href="${pageContext.request.contextPath}/web/console/directory/dept/view/${participantMap[participantUid].value}">${participantDisplayName}</a>
                                                        </c:when>
                                                        <c:when test="${participantMap[participantUid].type eq 'workflowVariable'}">
                                                            <font class="ftl_label"><fmt:message key="console.app.process.common.label.variableId"/> :</font> <c:out value="${fn:substring(participantMap[participantUid].value, 0, fn:indexOf(participantMap[participantUid].value, ','))}"/><br/>
                                                            <fmt:message key="console.process.config.label.mapParticipants.variable.${fn:substring(participantMap[participantUid].value, fn:indexOf(participantMap[participantUid].value, ',')+1, -1)}"/>
                                                        </c:when>
                                                        <c:when test="${participantMap[participantUid].type eq 'requester' || participantMap[participantUid].type eq 'requesterHod' || participantMap[participantUid].type eq 'requesterSubordinates' || participantMap[participantUid].type eq 'requesterDepartment'}">
                                                            <c:choose>
                                                                <c:when test="${participantMap[participantUid].value ne ''}">
                                                                    <font class="ftl_label"><fmt:message key="console.app.activity.common.label.definitionId"/> :</font>
                                                                    <a href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}?tab=activityList&activityDefId=${participantMap[participantUid].value}">${participantMap[participantUid].value}</a>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <fmt:message key="console.process.config.label.mapParticipants.previousActivity"/>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </c:when>
                                                        <c:when test="${participantMap[participantUid].type eq 'plugin'}">
                                                            ${participantPluginMap[participantMap[participantUid].value].name} (<fmt:message key="console.plugin.label.version"/> ${participantPluginMap[participantMap[participantUid].value].version})
                                                        </c:when>
                                                        <c:otherwise>
                                                            ${participantMap[participantUid].value}
                                                        </c:otherwise>
                                                    </c:choose>
                                                </dd>
                                                <dt>&nbsp;</dt>
                                                <dd>
                                                    <div>
                                                        <input type="button" class="smallbutton" value="<fmt:message key="console.process.config.label.mapParticipants.removeMapping"/>" onclick="participantRemoveMapping('${participant.id}')"/>
                                                        <c:if test="${participantMap[participantUid].type eq 'plugin'}">
                                                            <input type="button" class="smallbutton" value="<fmt:message key="general.method.label.configPlugin"/>" onclick="participantConfigurePlugin('${participant.id}')"/>
                                                        </c:if>
                                                    </div>
                                                </dd>
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
                                    <span class="row-content" helpTitle="|||<fmt:message key="console.process.config.label.id"/>: ${activity.id}">
                                        <c:set var="activityDisplayName" value="${activity.name}"/>
                                        <c:if test="${empty activity.name}">
                                            <c:set var="activityDisplayName" value="${activity.id}"/>
                                        </c:if>
                                        ${activityDisplayName} <c:if test="${activity.type ne 'normal'}">(${activity.type})</c:if>
                                    </span>
                                    <span class="row-content id">
                                        <font class="ftl_label"><fmt:message key="console.process.config.label.id"/> :</font> ${activity.id}
                                    </span>
                                    <span class="row-button">
                                        <input type="button" value="<fmt:message key="console.process.config.label.mapActivities.addEditFormMapping"/>" onclick="addEditForm('${activity.id}')"/>
                                    </span>
                                    <div style="clear: both; padding-left: 1em; padding-top: 0.5em;">
                                        <div id="activityForm_${activity.id}" style="padding-left: 1em; padding-top: 0.5em;">
                                            <c:set var="activityForm" value="${activityFormMap[activityUid]}"/>
                                            <c:set var="form" value="${formMap[activityUid]}"/>
                                            <c:if test="${!empty activityForm && form != null}">
                                                <dl>
                                                    <dt><fmt:message key="console.form.common.label.name"/></dt>
                                                    <dd><a href="#" onclick="launchFormBuilder('${form.id}');return false;">${form.name}</a></dd>
                                                    <dt>&nbsp;</dt>
                                                    <dd><div><input type="button" class="smallbutton" value="<fmt:message key="console.process.config.label.mapActivities.removeMapping"/>" onclick="activityRemoveForm('${activity.id}')"/></div></dd>
                                                </dl>
                                            </c:if>
                                            <c:if test="${!empty activityForm && activityForm.type == 'EXTERNAL'}">
                                                <dl>
                                                    <dt><fmt:message key="console.process.config.label.mapActivities.formExternal"/></dt>
                                                    <dd><a target="_blank" href="${activityForm.formUrl}">${activityForm.formUrl}</a></dd>
                                                    <dt>&nbsp;</dt>
                                                    <dd><div><input type="button" class="smallbutton" value="<fmt:message key="console.process.config.label.mapActivities.removeMapping"/>" onclick="activityRemoveForm('${activity.id}')"/></div></dd>
                                                </dl>
                                            </c:if>
                                        </div>
                                        <dl>
                                            <dt>&nbsp;</dt>
                                            <c:set var="showNext" value=""/>
                                            <c:if test="${!empty activityForm && activityForm.autoContinue}">
                                                <c:set var="showNext" value="checked"/>
                                            </c:if>
                                            <dd><input type="checkbox" name="showNextAssigment" ${showNext} onchange="toggleContinueNextAssignment('${processIdWithoutVersion}','${activity.id}', this)"> <fmt:message key="console.process.config.label.mapActivities.showContinueAssignment"/></dd>
                                        </dl>
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
                                    <span class="row-content" helpTitle="|||<fmt:message key="console.process.config.label.id"/>: ${activity.id}">
                                        <c:set var="activityDisplayName" value="${activity.name}"/>
                                        <c:if test="${empty activity.name}">
                                            <c:set var="activityDisplayName" value="${activity.id}"/>
                                        </c:if>
                                        ${activityDisplayName}
                                    </span>
                                    <span class="row-content id">
                                        <font class="ftl_label"><fmt:message key="console.process.config.label.id"/> :</font> ${activity.id}
                                    </span>
                                    <span class="row-button">
                                        <input type="button" value="<fmt:message key="console.process.config.label.mapTools.addEditMapping"/>" onclick="addEditPlugin('${activity.id}')"/>
                                    </span>
                                    <div style="clear: both; padding-left: 1em; padding-top: 0.5em;">
                                        <div id="activityForm_${activity.id}" style="padding-left: 1em; padding-top: 0.5em;">
                                            <c:set var="plugin" value="${pluginMap[activityUid]}"/>
                                            <c:if test="${plugin ne null}">
                                                <dl>
                                                    <dt><fmt:message key="console.plugin.label.name"/></dt>
                                                    <dd>${plugin.name}&nbsp;</dd>
                                                    <dt><fmt:message key="console.plugin.label.version"/></dt>
                                                    <dd>${plugin.version}&nbsp;</dd>
                                                    <dt>&nbsp;</dt>
                                                    <dd>
                                                        <div>
                                                            <input type="button" class="smallbutton" value="<fmt:message key="console.process.config.label.mapTools.removePlugin"/>" onclick="activityRemovePlugin('${activity.id}')"/>
                                                            <input type="button" class="smallbutton" value="<fmt:message key="general.method.label.configPlugin"/>" onclick="activityConfigurePlugin('${activity.id}')"/>
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
                        <button onclick="window.location='${pageContext.request.contextPath}/web/console/app/${appId}//processes/${processIdWithoutVersion}';return false;"><fmt:message key="general.method.label.ok"/></button>
                        <button id="closeInfo"><fmt:message key="general.method.label.cancel"/></button>
                </div>
            </div>
        </div>
    </div>

    <script>
        var image = new Image();
        image.src = "${pageContext.request.contextPath}/web/images/xpdl/${process.encodedId}?rnd=" + new Date().valueOf().toString();
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

        $(document).ready(function() {
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
                var topy = $("#activityForm_${param.activityDefId}").offset().top - 100;
                topy = parseInt(topy);
                window.scrollTo(0, topy);
             }, 100);
        </c:if>
        <c:if test="${!empty param.participantId}">
            setTimeout(function() {
                var topy = $("#participant_${param.participantId}").offset().top - 100;
                topy = parseInt(topy);
                window.scrollTo(0, topy);
             }, 100);
        </c:if>
        });

        var tabView = new TabView('processTabView', 'top');
        tabView.init();
        <c:if test="${!empty param.tab}">
            tabView.select('#${param.tab}');
        </c:if>
        <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/form/edit/${form.id}"/>

            var reloadCallback = {
                success: function(data){
                    if (data && data.length > 0) {
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
                <%
                        String designerwebBaseUrl = "http://" + pageContext.getRequest().getServerName() + ":" + pageContext.getRequest().getServerPort();
                        if (WorkflowUtil.getSystemSetupValue("designerwebBaseUrl") != null && WorkflowUtil.getSystemSetupValue("designerwebBaseUrl").length() > 0) {
                            designerwebBaseUrl = WorkflowUtil.getSystemSetupValue("designerwebBaseUrl");
                        }

                        if (designerwebBaseUrl.endsWith("/")) {
                            designerwebBaseUrl = designerwebBaseUrl.substring(0, designerwebBaseUrl.length() - 1);
                        }

                        String locale = "en";
                        if (WorkflowUtil.getSystemSetupValue("systemLocale") != null && WorkflowUtil.getSystemSetupValue("systemLocale").length() > 0) {
                            locale = WorkflowUtil.getSystemSetupValue("systemLocale");
                        }
                %>
                var base = 'http://${pageContext.request.serverName}:${pageContext.request.serverPort}';
                var url = base + "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/package/xpdl";
                var path = base + '${pageContext.request.contextPath}';
                document.location = '<%= designerwebBaseUrl%>/jwdesigner/designer/webstart.jsp?url=' + encodeURIComponent(url) + '&path=' + encodeURIComponent(path) + '&appId=${appId}&appVersion=${appVersion}&locale=<%= locale%>&username=${username}&hash=${loginHash}';
            }

            function launchFormBuilder(formId) {
                window.open("${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/form/builder/" + formId);
            }

            function runProcess() {
                var url = "${pageContext.request.contextPath}/web/client/app/${appId}/${appVersion}/process/${processIdWithoutVersion}";
                popupDialog.src = url;
                popupDialog.init();
//                window.open(url, "_blank", "");
            }

            function addEditForm(activityId){
                popupDialog.src = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/" + escape(activityId) + "/form";
                popupDialog.init();
            }

            function addEditPlugin(activityId){
                popupDialog.src = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/" + escape(activityId) + "/plugin";
                popupDialog.init();
            }

            function addEditParticipant(participantId){
                popupDialog.src = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/participant/" + participantId;
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

            function activityConfigurePlugin(activityId){
                popupDialog.src = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/" + escape(activityId) + "/plugin/configure";
                popupDialog.init();
            }

            function participantRemoveMapping(participantId){
                var removeItem = {
                    success : function(response) {
                        $('#participant_'+participantId).html('');
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
                    ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/participant/' + participantId + '/remove', removeItem, 'type='+type+'&value='+value);
                }
            }

            function participantConfigurePlugin(participantId){
                popupDialog.src = '${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/participant/'+participantId+'/plugin/configure';
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
                popupDialog.src = "${pageContext.request.contextPath}/web/images/xpdl/${process.encodedId}";
                popupDialog.init();

            }

            var autoCallback = {
                success: function() {
                    // do nothing
                }
            }

            function toggleContinueNextAssignment(processDefId, activityDefId, checkbox){
                var params = "auto="+$(checkbox).attr('checked');
                ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/' + processDefId + '/activity/' + activityDefId + '/continue', autoCallback, params);
            }
    </script>

</div>

<script>
    Template.init("#menu-apps", "#nav-app-processes");
    HelpGuide.key = "help.web.console.app.processes.view";
</script>

<commons:footer />
