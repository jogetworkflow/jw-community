<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.HostManager"%>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>

<commons:header />
<link rel="stylesheet" href="${pageContext.request.contextPath}/wro/jds.min.css">
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
    <div id="main-title"><fmt:message key="console.header.submenu.label.setting.directory"/></div>
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
                        <button type="button" id="dm-config" class="btn button-action-main console-primary" onclick="configureDirectoryManagerImpl()"><i class="fas fa-wrench"></i> <fmt:message key="general.method.label.configPlugin"/></button>
                    </c:if>
                    <button type="button" id="dm-change" class="btn button-action-main console-tertiary" onclick="selectDirectoryManagerImpl()"><i class="fas fa-exchange-alt"></i> <fmt:message key="console.setting.directory.label.changePlugin"/></button>
                </div>
            </div>
        </section>
        <c:if test="${hasIdpMfaPage && IdentityProviderManager != null && MfaManager != null}">
            <jsp:include page="idpMfa.jsp" flush="true"/>
        </c:if>
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
    
