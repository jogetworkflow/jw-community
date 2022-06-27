<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header />

<script type="text/javascript" src="${pageContext.request.contextPath}/js/JSON.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/JSONError.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/governance.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/governance.css?build=<fmt:message key="build.number"/>">

<div id="nav">
    <div id="nav-title">
        <p><i class="fas fa-tachometer-alt"></i> <fmt:message key='console.header.menu.label.monitor'/></p>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="subMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-action">
        <ul id="main-action-buttons">
            <li><button class="alertBtn btn btn-primary"><fmt:message key="console.governance.manageAlert"/></button></li>
            <li><button class="deleteDataBtn btn btn-primary"><fmt:message key="console.governance.deleteData"/></button></li>
            <li><button class="checkNowBtn btn btn-primary"><fmt:message key="console.governance.checkInterval.checkNow"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <div class="governance">
            <div class="governance_header">
                <c:if test="${serverName ne ''}">
                    <div class="serverName">
                        <h3><fmt:message key="console.governance.serverName"/> : ${serverName}</h3>
                    </div>
                </c:if>    
                <div class="check_interval_container">
                    <select id="status_selector">
                        <option value="open" selected><fmt:message key="console.governance.showOpenOnly"/></option>
                        <option value="all"><fmt:message key="console.governance.showAll"/></option>
                    </select> 
                    
                    <label><fmt:message key="console.governance.checkInterval"/></label>
                    <select id="check_interval">
                        <option value="0"><fmt:message key="console.governance.checkInterval.disabled"/></option>
                        <option value="300"><fmt:message key="console.governance.checkInterval.5mins"/></option>
                        <option value="900"><fmt:message key="console.governance.checkInterval.15mins"/></option>
                        <option value="1800"><fmt:message key="console.governance.checkInterval.30mins"/></option>
                        <option value="3600"><fmt:message key="console.governance.checkInterval.1hr"/></option>
                        <option value="10800"><fmt:message key="console.governance.checkInterval.3hr"/></option>
                        <option value="21600"><fmt:message key="console.governance.checkInterval.6hr"/></option>
                        <option value="43200"><fmt:message key="console.governance.checkInterval.12hr"/></option>
                        <option value="86400"><fmt:message key="console.governance.checkInterval.24hr"/></option>
                        <option value="172800"><fmt:message key="console.governance.checkInterval.2days"/></option>
                        <option value="604800"><fmt:message key="console.governance.checkInterval.1week"/></option>
                    </select> 
                    <button class="updateBtn btn btn-secondary"><fmt:message key="console.governance.checkInterval.update"/></button>
                </div>
                <div style="clear:both;"></div>
            </div>
            <div class="governance_report">
                <table>
                    <thead>
                        <tr>
                            <th class="name" width="25%"></th>
                            <th class="status" width="10%"><fmt:message key="console.governance.status"/></th>
                            <th class="timestamp" width="15%"><fmt:message key="console.governance.lastChecked"/></th>
                            <th class="details" width="50%"><fmt:message key="console.governance.details"/></th>
                        </tr>    
                    </thead>
                    <tbody>
                        <c:forEach items="${checker}" var="categoryRow" varStatus="cloop">
                            <tr id="cat-${cloop.index}" class="category">
                                <th colspan="4"><fmt:message key="console.governance.category"/><span class="category_label">${categoryRow.key}</span></th>
                            </tr>    
                            <c:set var="elementList" value="${categoryRow.value}"/>
                            <c:forEach items="${elementList}" var="element" varStatus="loop">
                                <tr class="<c:if test="${loop.index % 2 eq 1}">even</c:if>" plugin-class="${element.className}">
                                    <td class="name">
                                        <span class="plugin_label">${element.i18nLabel}</span>
                                        <span class="actions"><a class="deactivateBtn <c:if test="${element.properties.deactivated eq 'true'}">deactivated</c:if>"><span class="activate"><fmt:message key="console.governance.activate"/></span><span class="deactivate"><fmt:message key="console.governance.deactivate"/></span></a><c:if test="${element.configurable}"><a class="configBtn"><fmt:message key="console.governance.configure"/></a></c:if><c:if test="${!empty element.infoLink}"><a href="${element.infoLink}" target="_blank" rel="noopener"><i class="fas fa-question-circle"></i></a></c:if></span>
                                    </td>
                                    <td class="status"></td>
                                    <td class="timestamp"></td>
                                    <td class="details"></td>
                                </tr>
                            </c:forEach>
                        </c:forEach>
                    </tbody>    
                </table>    
            </div>
        </div>
    </div>
</div>

<script>
    Template.init("#menu-monitor", "#nav-governance");
    
    $(function(){
        GovernanceUtil.init('${interval}', {
            pass : '<ui:msgEscJS key="console.governance.pass"/>',
            fail : '<ui:msgEscJS key="console.governance.fail"/>',
            warn : '<ui:msgEscJS key="console.governance.warn"/>',
            info : '<ui:msgEscJS key="console.governance.info"/>',
            more : '<ui:msgEscJS key="console.governance.moreInfo"/>',
            intervalUpdated : '<ui:msgEscJS key="console.governance.intervalUpdated"/>',
            seconds : '<ui:msgEscJS key="console.governance.seconds"/>',
            minutes : '<ui:msgEscJS key="console.governance.minutes"/>',
            hours : '<ui:msgEscJS key="console.governance.hours"/>',
            days : '<ui:msgEscJS key="console.governance.days"/>',
            months : '<ui:msgEscJS key="console.governance.months"/>',
            years : '<ui:msgEscJS key="console.governance.years"/>',
            activateConfirm : '<ui:msgEscJS key="console.governance.activate.confirm"/>',
            deactivateConfirm : '<ui:msgEscJS key="console.governance.deactivate.confirm"/>',
            deleteConfirm : '<ui:msgEscJS key="console.governance.deleteData.confirm"/>',
            dataDeleted : '<ui:msgEscJS key="console.governance.dataDeleted"/>',
            view : '<ui:msgEscJS key="console.governance.view"/>',
            suppress : '<ui:msgEscJS key="console.governance.suppress"/>',
            suppressed : '<ui:msgEscJS key="console.governance.suppressed"/>',
            suppressConfirm : '<ui:msgEscJS key="console.governance.suppress.confirm"/>'
        });
        GovernanceUtil.updateResult(${lastResult});
    });
</script>

<commons:footer />
