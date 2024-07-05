<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.HostManager"%>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>

<commons:header />
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/bootstrap4/css/bootstrap.min.css">
<script src="${pageContext.request.contextPath}/js/bootstrap4/js/bootstrap.bundle.min.js" type="text/javascript"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/console.v9.css"/>
<div id="nav">
    <div id="nav-title">
        <p><i class="fas fa-cogs"></i> <fmt:message key='console.header.top.label.settings'/></p>
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
        <section id="connector-setup" class="section">
            <div class="section-header">
                <h2 class="section-title"><fmt:message key="console.setting.directory.label.directoryManagerImpl"/></h2>
            </div>
            <div class="section-list-item">
                <span class="section-list-item-title">
                <c:choose>
                    <c:when test="${!empty overriddenDmClassName}">
                        ${directoryManagerName}
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="console.setting.directory.label.defaultPlugin"/>
                    </c:otherwise>
                </c:choose>
                </span>
                <div class="section-list-item-actions">
                    <c:if test="${isDmConfigurable}">
                        <button type="button" id="dm-config" class="btn button-action-main" onclick="configureDirectoryManagerImpl()"><fmt:message key="general.method.label.configPlugin"/></button>
                    </c:if>
                    <button type="button" id="dm-change" class="btn button-action-main" onclick="selectDirectoryManagerImpl()"><fmt:message key="console.setting.directory.label.changePlugin"/></button>
                </div>
            </div>
        </section>
        <c:if test="${isEnterprise}">
            <jsp:include page="idpMfa.jsp" flush="true"/>
        </c:if>
<%--        <div id="connectorSetup">--%>
<%--            <div class="main-body-row">--%>
<%--                <div class="row-content">--%>
<%--                        <fmt:message key="console.setting.directory.label.directoryManagerImpl"/>--%>
<%--                        <dl>--%>
<%--                            <dt><fmt:message key="console.setting.directory.label.currentPluginClassName"/></dt>--%>
<%--                            <c:choose>--%>
<%--                                <c:when test="${!empty overriddenDmClassName}">--%>
<%--                                    <dd><c:out value="${overriddenDmClassName}"/>&nbsp;</dd>--%>
<%--                                    <dt><fmt:message key="console.setting.directory.label.currentPluginName"/></dt>--%>
<%--                                    <dd><c:out value="${directoryManagerName}"/>&nbsp;</dd>--%>
<%--                                </c:when>--%>
<%--                                <c:otherwise>--%>
<%--                                    <dd><fmt:message key="console.setting.directory.label.defaultPlugin"/>&nbsp;</dd>--%>
<%--                                    <dt><fmt:message key="console.setting.directory.label.currentPluginName"/></dt>--%>
<%--                                    <dd>&nbsp;</dd>--%>
<%--                                </c:otherwise>--%>
<%--                            </c:choose>--%>
<%--                            <dt>&nbsp;</dt>--%>
<%--                            <dd>--%>
<%--                                <c:if test="${!empty overriddenDmClassName}">--%>
<%--                                    <c:if test="${!isOverridden}">--%>
<%--                                        <button type="button" class="smallbutton" onclick="removeDirectoryManagerImpl()"><fmt:message key="console.setting.directory.label.removePlugin"/></button>--%>
<%--                                    </c:if>--%>
<%--                                    <button type="button" class="smallbutton" onclick="configDirectoryManagerImpl('${overriddenDmClassName}')"><fmt:message key="general.method.label.configPlugin"/></button>--%>
<%--                                </c:if>--%>
<%--                            </dd>--%>
<%--                        </dl>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--            <div class="main-body-row">--%>
<%--                <div class="row-content">--%>
<%--                    <dl>--%>
<%--                        <dt><fmt:message key="console.setting.directory.label.selectPlugin"/></dt>--%>
<%--                        <dd>--%>
<%--                            <c:if test="${!empty directoryManagerPluginList}">--%>
<%--                                <select name="directoryManagerImpl" id="directoryManagerImpl">--%>
<%--                                    <c:forEach items="${directoryManagerPluginList}" var="plugin">--%>
<%--                                        <c:set var="pluginName" value="<%= ClassUtils.getUserClass(pageContext.findAttribute(\"plugin\")).getName() %>"/>--%>
<%--                                        <c:if test="${pluginName ne overriddenDmClassName}">--%>
<%--                                            <option value="<c:out value="${pluginName}"/>"><c:out value="${plugin.i18nLabel}"/> - <c:out value="${plugin.version}"/></option>--%>
<%--                                        </c:if>--%>
<%--                                    </c:forEach>--%>
<%--                                </select>--%>
<%--                                <div>--%>
<%--                                    <button type="button" class="smallbutton" onclick="selectDirectoryManagerImpl()"><fmt:message key="general.method.label.select"/></button>--%>
<%--                                </div>--%>
<%--                            </c:if>--%>
<%--                            <c:if test="${empty directoryManagerPluginList}">--%>
<%--                                <fmt:message key="console.setting.directory.label.noPlugin"/>--%>
<%--                            </c:if>--%>
<%--                        </dd>--%>
<%--                    </dl>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </div>--%>
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
        popupDialog.src = "${pageContext.request.contextPath}/web/console/setting/directoryManagerImpl/select";
        popupDialog.init();
    }

    <c:if test="${isDmConfigurable}">
    function configureDirectoryManagerImpl(){
        popupDialog.src = "${pageContext.request.contextPath}/web/console/setting/directoryManagerImpl/config?directoryManagerImpl=${overriddenDmClassName}";
        popupDialog.init();
    }
    </c:if>
</script>

<script>
    Template.init("", "#nav-setting-directory");
</script>

<commons:footer />
