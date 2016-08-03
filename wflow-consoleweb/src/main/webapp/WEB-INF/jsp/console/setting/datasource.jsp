<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.HostManager"%>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>

<commons:header />
<style>
    .row-content{
        display: block;
        float: none;
    }

    .form-input{
        width: 50%
    }

    .form-input input, .form-input textarea{
        width: 100%
    }

    .row-title{
        font-weight: bold;
    }
</style>
<div id="nav">
    <div id="nav-title">
        <p><i class="icon-cogs"></i> <fmt:message key='console.header.top.label.settings'/></p>
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
    </div>
    <div id="main-body">
        <c:if test="${!isVirtualHostEnabled}">
        <div class="main-body-row">
            <span class="row-content">
                <div class="form-row">
                    <label for="profileList"><fmt:message key="console.setting.datasource.label.selectProfile"/></label>
                    <span class="form-input">
                        <select id="profileList">
                            <c:forEach items="${profileList}" var="profile">
                                <c:set var="selected"><c:if test="${profile == currentProfile}"> selected</c:if></c:set>
                                <option ${selected}><c:out value="${profile}"/></option>
                            </c:forEach>
                        </select>
                        <button type="button" onclick="changeProfile()"><fmt:message key="console.setting.datasource.label.switchProfile"/></button>
                        <button type="button" onclick="deleteProfile()"><fmt:message key="console.setting.datasource.label.deleteProfile"/></button>
                    </span>
                </div>
            </span>
        </div>
        <div id="datasourceSetup">
            <form id="datasourceForm" method="post" action="${pageContext.request.contextPath}/web/console/setting/datasource/submit">
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="workflowDriver"><fmt:message key="console.setting.datasource.label.driverName"/></label>
                        <span class="form-input">
                            <input id="workflowDriver" type="text" name="workflowDriver" value="<c:out value="${settingMap['workflowDriver']}"/>"/>
                        </span>
                    </div>
                    <div class="form-row">
                        <label for="workflowUrl"><fmt:message key="console.setting.datasource.label.url"/></label>
                        <span class="form-input">
                            <input id="workflowUrl" type="text" name="workflowUrl" value="<c:out value="${settingMap['workflowUrl']}"/>"/>
                        </span>
                    </div>
                    <div class="form-row">
                        <label for="workflowUser"><fmt:message key="console.setting.datasource.label.user"/></label>
                        <span class="form-input">
                            <input id="workflowUser" type="text" name="workflowUser" value="<c:out value="${settingMap['workflowUser']}"/>"/>
                        </span>
                    </div>
                    <div class="form-row">
                        <label for="workflowPassword"><fmt:message key="console.setting.datasource.label.password"/></label>
                        <span class="form-input">
                            <input id="workflowPassword" type="password" name="workflowPassword" value="<c:out value="${settingMap['workflowPassword']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row" id="testConnection" style="display: none">
                <b><fmt:message key="console.setting.datasource.label.testingConnection"/></b>
                <div id="workflowTestConnection"><fmt:message key="console.setting.datasource.label.testing"/> <fmt:message key="console.setting.datasource.label.datasource"/>...<span class="connectionStatus"></span></div>
            </div>
            <div class="form-buttons">
                <input class="form-button" id="saveDatasource" type="button" value="<fmt:message key="general.method.label.save"/>" onclick="submitDatasource()" />
                <input class="form-button" id="saveDatasourceAsNew" type="button" value="<fmt:message key="console.setting.datasource.label.saveAsNewProfile"/>" onclick="submitDatasource(true)" />
                <fmt:message key="console.setting.datasource.label.newProfileName"/>
                <input id="newProfileName" type="text" name="profileName" />
            </div>
            </form>
        </div>
        </c:if>
    </div>
</div>

<script>
    var profileList= [];
    <c:forEach items="${profileList}" var="profile">
        profileList.push('<c:out value="${profile}"/>');
    </c:forEach>

    var datasources = ['workflow'];
    function submitDatasource(asNewProfile){
        var connectionCount = 0;

        $('#saveDatasource').attr('disabled', 'disabled');
        $('#saveDatasourceAsNew').attr('disabled', 'disabled');

        var success = new Array();
        for(i in datasources)
            success[datasources[i]] = false;

        $('#testConnection').show();

        for(i in datasources){
            var testUrl = "${pageContext.request.contextPath}/web/json/workflow/testConnection";
            var testCallback = {
                success : function(o){
                    connectionCount++;

                    var obj = eval('(' + o + ')');
                    if(obj.success == true){
                        $('#testConnection #' + obj.datasource + 'TestConnection .connectionStatus').html('<span class="connection-ok"><fmt:message key="console.setting.datasource.label.connectionOk"/></span>');
                        success[obj.datasource] = true;

                        //check if all success
                        var allSuccess = true;
                        for(key in success){
                            if(success[key] == false){
                                allSuccess = false;
                                break;
                            }
                        }

                        if(allSuccess && connectionCount == datasources.length){
                            $('#saveDatasource').removeAttr('disabled');
                            $('#saveDatasourceAsNew').removeAttr('disabled');

                            if(asNewProfile && asNewProfile == true)
                                saveAsNewProfile();
                            else
                                $('#datasourceForm').submit();
                        }
                    }else{
                        $('#testConnection #' + obj.datasource + 'TestConnection .connectionStatus').html('<span class="connection-fail"><fmt:message key="console.setting.datasource.label.connectionFail"/></span>');
                        $('#saveDatasource').removeAttr('disabled');
                        $('#saveDatasourceAsNew').removeAttr('disabled');
                    }
                }
            };
            var img = '<img src="${pageContext.request.contextPath}/images/v3/loading.gif">';
            $('#testConnection #' + datasources[i] + 'TestConnection .connectionStatus').html(img);

            var driver   = $('#' + datasources[i] + 'Driver').val();
            var url      = $('#' + datasources[i] + 'Url').val();
            var user     = $('#' + datasources[i] + 'User').val();
            var password = $('#' + datasources[i] + 'Password').val();
            var testParam = "datasource=" + datasources[i] + "&driver=" + encodeURIComponent(driver) + "&url=" + encodeURIComponent(url) + "&user=" + encodeURIComponent(user) + "&password=" + encodeURIComponent(password);

            ConnectionManager.post(testUrl, testCallback, testParam);
        }
    }

    var callback = {
        success: function(){
            document.location.href = document.location.href;
        }
    }

    function arrayToObject(array){
        var obj = {};
        for(var i=0; i<array.length; i++){
            obj[array[i]]='';
        }
        return obj;
    }

    function changeProfile(){
        if(confirm("<fmt:message key="console.setting.datasource.label.switchProfileConfirm"/>")) {
            var param = "profileName=" + $('#profileList').val();
            ConnectionManager.post("${pageContext.request.contextPath}/web/console/setting/profile/change", callback, param);
        }
    }

    function deleteProfile(){
        if(confirm("<fmt:message key="console.setting.datasource.label.deleteProfileConfirm"/>")) {
            var currentProfile = '<c:out value="${currentProfile}"/>';
            if($('#profileList').val() == currentProfile)
                alert("<fmt:message key="console.setting.datasource.label.deleteProfileInvalid"/>")
            else{
                var param = "profileName=" + $('#profileList').val();
                ConnectionManager.post("${pageContext.request.contextPath}/web/console/setting/profile/delete", callback, param);
            }
        }
    }

    function saveAsNewProfile(){
        if(confirm("<fmt:message key="console.setting.datasource.label.saveAsProfileConfirm"/>")) {
            var newProfileName = $('#newProfileName').val();
            if(!/^[a-zA-Z0-9]+[a-zA-Z0-9 ]*$/.test(newProfileName)){
                alert('<fmt:message key="console.setting.datasource.label.saveAsProfileInvalid"/>');
                $('#newProfileName').focus();
            }else if(newProfileName in arrayToObject(profileList)){
                alert('<fmt:message key="console.setting.datasource.label.saveAsProfileExist"/>');
                $('#newProfileName').focus();
            }else{
                var param = $('#datasourceForm').serialize();
                ConnectionManager.post("${pageContext.request.contextPath}/web/console/setting/profile/create", callback, param);
            }
        }
    }
</script>

<script>
    Template.init("", "#nav-setting-datasource");
</script>

<commons:footer />
