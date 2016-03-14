<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header />

<div id="nav">
    <div id="nav-title">
        <p><i class="icon-tasks"></i> <fmt:message key="console.header.submenu.label.inbox"/></p>
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
        </ul>
    </div>
    <div id="main-body">

        <script>
            function closeDialog() {
                assignmentInbox.refresh();
            }
        </script>

        <div id="main-body-content">
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/workflow/assignment/list"
                          var="assignmentInbox"
                          divToUpdate="assignmentInbox"
                          jsonData="data"
                          rowsPerPage="10"
                          sort="dateCreated"
                          desc="true"
                          width="100%"
                          href="${pageContext.request.contextPath}/web/client/app/assignment/"
                          hrefParam="activityId"
                          hrefQuery="false"
                          hrefDialog="true"
                          hrefDialogWidth="600px"
                          hrefDialogHeight="400px"
                          hrefDialogTitle="Process Dialog"
                          fields="['activityId','processName','activityName','processVersion', 'dateCreated', 'processId', 'acceptedStatus', 'serviceLevelMonitor', 'due']"
                          column1="{key: 'processName', label: 'console.app.process.common.label', sortable: false, width: '120'}"
                          column2="{key: 'activityName', label: 'console.app.activity.common.label.name', sortable: false, width: '160'}"
                          column3="{key: 'processVersion', label: 'console.app.process.common.label.version', sortable: false, hide:true}"
                          column4="{key: 'dateCreated', label: 'console.app.assignment.common.label.dateCreated', sortable: true, width: '130'}"
                          column5="{key: 'processId', label: 'console.app.process.common.label.id', sortable: true, hide:true}"
                          column6="{key: 'serviceLevelMonitor', label: 'console.app.assignment.common.label.serviceLevelMonitor', sortable: true, relaxed: true, width: '100'}"
                          column7="{key: 'due', label: 'console.app.assignment.common.label.dueDate', sortable: true, width: '128'}"
                          />
            
            <script type="text/javascript">
                function toggleEmbedCode(){
                    var embedToggleCallback = function() {
                        $('#embed-code textarea').focus().select();
                    };
                    $("#embed-code").toggle("slow", embedToggleCallback );
                }
            </script>
                
            <div style="position:relative;margin-bottom:5px;">
                <span id="embed-icon"><a onclick="toggleEmbedCode()"><fmt:message key="general.method.label.embedCode"/></a></span>
                <c:if test="${!userSecurity.disableHashLogin}">
                <span id="rss-icon"><a target="_blank" href="${pageContext.request.contextPath}${rssLink}"><span><fmt:message key="general.method.label.rss"/></span></a></span>
                </c:if>
            </div>
                
            <div style="clear:both;"></div>
                
            <div id="embed-code" name="embed-code">
                <textarea>
<link rel="stylesheet" type="text/css" href="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/css/portlet.css">
<script type="text/javascript" src="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/js/jquery/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/js/jquery/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/js/json/util.js"></script>
<div id="inbox1"><center><img src="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/images/v3/portlet_loading.gif"/></center></div>
<script type="text/javascript" >$(document).ready(function(){ $.getScript('${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/web/js/client/inbox.js?id=1&rows=5&divId=inbox1',null); });</script>
                </textarea>
            </div>            
        </div>

    </div>
</div>

<script>
    Template.init("#menu-run", "#nav-run-inbox");
</script>

<commons:footer />
