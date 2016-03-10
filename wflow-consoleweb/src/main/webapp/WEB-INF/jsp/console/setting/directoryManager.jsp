<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.HostManager"%>
<%@ page import="org.joget.directory.model.service.DirectoryUtil"%>
<%@ page import="org.springframework.util.ClassUtils"%>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>
<c:set var="isOverridden" value="<%= DirectoryUtil.isOverridden() %>"/>
<c:set var="overriddenDmClassName" value="<%= DirectoryUtil.getOverriddenDirectoryManagerClassName() %>"/>

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
        <div id="connectorSetup">
            <div class="main-body-row">
                <div class="row-content">
                        <fmt:message key="console.setting.directory.label.directoryManagerImpl"/>

                        <c:choose>
                            <c:when test="${isOverridden}">
                                <c:set var="selected" value="${overriddenDmClassName}"/>
                            </c:when>
                            <c:when test="${!empty settingMap['directoryManagerImpl']}">
                                <c:set var="selected" value="${settingMap['directoryManagerImpl']}"/>
                            </c:when>
                        </c:choose>
                        

                        <dl>
                            <dt><fmt:message key="console.setting.directory.label.currentPluginClassName"/></dt>
                            <c:choose>
                                <c:when test="${!empty selected}">
                                    <dd><c:out value="${selected}"/>&nbsp;</dd>
                                    <dt><fmt:message key="console.setting.directory.label.currentPluginName"/></dt>
                                    <dd><c:out value="${directoryManagerName}"/>&nbsp;</dd>
                                </c:when>
                                <c:otherwise>
                                    <dd><fmt:message key="console.setting.directory.label.defaultPlugin"/>&nbsp;</dd>
                                    <dt><fmt:message key="console.setting.directory.label.currentPluginName"/></dt>
                                    <dd>&nbsp;</dd>
                                </c:otherwise>
                            </c:choose>
                            <dt>&nbsp;</dt>
                            <dd>
                                <c:if test="${!empty selected}">
                                    <c:if test="${!isOverridden}">
                                        <button type="button" class="smallbutton" onclick="removeDirectoryManagerImpl()"><fmt:message key="console.setting.directory.label.removePlugin"/></button>
                                    </c:if>
                                    <button type="button" class="smallbutton" onclick="configDirectoryManagerImpl('${selected}')"><fmt:message key="general.method.label.configPlugin"/></button>
                                </c:if>
                            </dd>
                        </dl>
                </div>
            </div>             
            <div class="main-body-row">
                <div class="row-content">
                    <dl>
                        <dt><fmt:message key="console.setting.directory.label.selectPlugin"/></dt>
                        <dd>
                            <c:if test="${!empty directoryManagerPluginList}">
                                <select name="directoryManagerImpl" id="directoryManagerImpl">
                                    <c:forEach items="${directoryManagerPluginList}" var="plugin">
                                        <c:set var="pluginName" value="<%= ClassUtils.getUserClass(pageContext.findAttribute(\"plugin\")).getName() %>"/>
                                        <c:if test="${pluginName ne overriddenDmClassName}">
                                            <option value="<c:out value="${pluginName}"/>"><c:out value="${plugin.i18nLabel}"/> - <c:out value="${plugin.version}"/></option>
                                        </c:if>
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
                </div>
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
    
    function removeDirectoryManagerImpl(){
        if(confirm("<fmt:message key="console.setting.directory.label.removePluginConfirm"/>")) {
            ConnectionManager.post("${pageContext.request.contextPath}/web/console/setting/directoryManagerImpl/remove", callback, null);
        }
    }
    
    function configDirectoryManagerImpl(pluginName){
        popupDialog.src = "${pageContext.request.contextPath}/web/console/setting/directoryManagerImpl/config?directoryManagerImpl=" + pluginName;
        popupDialog.init();
    }
</script>

<script>
    Template.init("", "#nav-setting-directory");
</script>

<commons:footer />
