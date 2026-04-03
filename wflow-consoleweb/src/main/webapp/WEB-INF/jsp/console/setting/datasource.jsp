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

</style>
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
    <div id="main-title"><fmt:message key="console.header.submenu.label.setting.datasource"/></div>
    <div id="main-action">
    </div>
    <div id="main-body">
        <c:if test="${!isVirtualHostEnabled}">
        <div class="main-body-row">
            <p><fmt:message key="console.setting.datasource.label.readonlyNotice"/></p>
        </div>
        <div id="datasourceSetup">
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="currentProfile"><fmt:message key="console.setting.datasource.label.profileName"/></label>
                        <span class="form-input">
                            <input id="currentProfile" type="text" readonly value="<c:out value="${currentProfile}"/>"/>
                        </span>
                    </div>
                    <div class="form-row">
                        <label for="workflowDriver"><fmt:message key="console.setting.datasource.label.driverName"/></label>
                        <span class="form-input">
                            <input id="workflowDriver" type="text" name="workflowDriver" readonly value="<c:out value="${settingMap['workflowDriver']}"/>"/>
                        </span>
                    </div>
                    <div class="form-row">
                        <label for="workflowUrl"><fmt:message key="console.setting.datasource.label.url"/></label>
                        <span class="form-input">
                            <input id="workflowUrl" type="text" name="workflowUrl" readonly value="<c:out value="${settingMap['workflowUrl']}"/>"/>
                        </span>
                    </div>
                    <div class="form-row">
                        <label for="workflowUser"><fmt:message key="console.setting.datasource.label.user"/></label>
                        <span class="form-input">
                            <input id="workflowUser" type="text" name="workflowUser" readonly value="<c:out value="${settingMap['workflowUser']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
        </div>
        </c:if>
    </div>
</div>

<script>
    Template.init("", "#nav-setting-datasource");
</script>

<commons:footer />
