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
        <div id="connectorSetup">
            <div class="main-body-row">
                <span class="row-content">
                        <fmt:message key="console.setting.directory.label.directoryManagerImpl"/>

                        <c:set var="selected">
                            <fmt:message key="console.setting.directory.label.defaultPlugin"/>
                        </c:set>
                        <c:if test="${!empty settingMap['directoryManagerImpl']}">
                            <c:set var="selected" value="${settingMap['directoryManagerImpl']}"/>
                        </c:if>

                        <dl>
                            <dt><fmt:message key="console.setting.directory.label.currentPluginClassName"/></dt>
                            <dd>${selected}&nbsp;</dd>
                            <c:if test="${!empty settingMap['directoryManagerImpl']}">
                                <dt><fmt:message key="console.setting.directory.label.currentPluginName"/></dt>
                                <dd>${directoryManagerName}&nbsp;</dd>
                                <c:if test="${directoryManagerConfigError}">
                                    <dt>&nbsp;</dt>
                                    <dd><fmt:message key="console.setting.directory.label.currentPluginConfigError"/></dd>
                                </c:if>
                            </c:if>
                            <dt>&nbsp;</dt>
                            <dd>
                                <c:if test="${!empty settingMap['directoryManagerImpl']}">
                                    <button type="button" class="smallbutton" onclick="removeDirectoryManagerImpl()"><fmt:message key="console.setting.directory.label.removePlugin"/></button>
                                    <button type="button" class="smallbutton" onclick="configDirectoryManagerImpl('${settingMap['directoryManagerImpl']}')"><fmt:message key="general.method.label.configPlugin"/></button>
                                </c:if>
                            </dd>
                        </dl>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                        <dl>
                            <dt><fmt:message key="console.setting.directory.label.selectPlugin"/></dt>
                            <dd>
                                <c:if test="${!empty directoryManagerPluginList}">
                                    <select name="directoryManagerImpl" id="directoryManagerImpl">
                                        <c:forEach items="${directoryManagerPluginList}" var="plugin">
                                            <option value="${plugin['class'].name}">${plugin.name} - ${plugin.version}</option>
                                        </c:forEach>
                                    </select>
                                    <div>
                                        <button type="button" class="smallbutton" onclick="selectDirectoryManagerImpl()"><fmt:message key="general.method.label.select"/></button>
                                    </div>
                                </c:if>
                                <c:if test="${empty directoryManagerPluginList}">
                                    <fmt:message key="console.setting.directory.label.noPlugin"/>
                                </c:if>
                            </dd>
                        </dl>
                </span>
            </div>
        </div>
    </div>
</div>

<script>
    var callback = {
        success: function(){
            document.location.href = document.location.href;
        }
    }
    
    <ui:popupdialog var="popupDialog" src="${pageContext.request.contextPath}/web/console/setting/directoryManagerImpl/config"/>

    function selectDirectoryManagerImpl(){
        popupDialog.src = "${pageContext.request.contextPath}/web/console/setting/directoryManagerImpl/config?directoryManagerImpl=" + $('#directoryManagerImpl').val();
        popupDialog.init();
    }

    function configDirectoryManagerImpl(pluginName){
        popupDialog.src = "${pageContext.request.contextPath}/web/console/setting/directoryManagerImpl/config?directoryManagerImpl=" + pluginName;
        popupDialog.init();
    }

    function removeDirectoryManagerImpl(){
        if(confirm("<fmt:message key="console.setting.directory.label.removePluginConfirm"/>")) {
            ConnectionManager.post("${pageContext.request.contextPath}/web/console/setting/directoryManagerImpl/remove", callback, null);
        }
    }
</script>

<script>
    Template.init("", "#nav-setting-directory");
</script>

<commons:footer />
