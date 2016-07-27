<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="title"><fmt:message key="adminBar.label.app"/>: ${appDefinition.name}</c:set>
<commons:header title="${title}" />

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
        </ul>
    </div>
    <div id="main-body">

        <div id='nv-refresh'>
            <a href="#" id="toggleInfo" onclick="toggleInfo();return false"><i class='icon-th-list'></i></a>&nbsp;&nbsp;
            <a href='#' onclick='return refreshNavigator()'><i class='icon-refresh'></i> <fmt:message key="general.method.label.refresh"/></a>
        </div>
        <div id="nv-container">
        <jsp:include page="/web/console/app/${appId}/${appVersion}/navigator" flush="true"/>
        </div>
        <script>
            function refreshNavigator() {
                if ($("#nv-refresh").css("visibility") != "hidden") {
                    var loading = $("<img id='nv-loading' src='${pageContext.request.contextPath}/images/v3/loading.gif'>");
                    $("#nv-refresh a").css("visibility", "hidden");
                    $("#nv-refresh").append(loading);
                }
                $.ajax({
                    url: "${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/navigator?hidden=true&_=" + jQuery.now(),
                    success: function(data) {
                        $("#nv-container").html(data);
                    },
                    complete: function() {
                        $("#nv-refresh a").css("visibility", "visible");
                        $(loading).remove();
                    }
                });
                return false;
            }
            function closeDialog() {
                refreshNavigator();
            }            
        </script>
    </div>
</div>

<script>
    $(document).ready(function(){
        <c:if test="${param.formCreate == 'true'}">
            formCreate();
        </c:if>
    });
    
    <ui:popupdialog var="formCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/form/create"/>
    function formCreate(){
        formCreateDialog.init();
    }
    Template.init("#menu-apps", "#nav-app-forms");
    HelpGuide.key = "help.web.console.app.navigator";
</script>

<commons:footer />
