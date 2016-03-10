<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<div class="dropdown">
    <div class="top"></div>
    <ul class="dropdown-list">
        <div class="middle">
        <c:forEach items="${appDefinitionList}" var="appDefinition">
            <li>
                <a href="${pageContext.request.contextPath}/web/console/app/${appDefinition.id}/forms">
                    <span class="substeps">&nbsp;</span>
                    <span class="subtitle"><c:out value="${appDefinition.name}"/></span>
                    <span class="subsubtitle"><fmt:message key="console.app.common.label.version"/> ${appDefinition.version}</span></a>
            </li>
        </c:forEach>
        </div>
        <li>
            <div class="dropdown-btn">
                <input value="<fmt:message key="console.app.create.label"/>" type="button" onclick="appCreate()"/>
                <script>
                    <ui:popupdialog var="appCreateDialog" src="${pageContext.request.contextPath}/web/console/app/create"/>
                        function appCreate(){
                            appCreateDialog.init();
                        }
                </script>
                <input value="<fmt:message key="console.app.import.label"/>" type="button" onclick="appImport()"/>
                <script>
                    <ui:popupdialog var="appCreateDialog2" src="${pageContext.request.contextPath}/web/console/app/import"/>
                        function appImport(){
                            appCreateDialog2.init();
                        }
                </script>
            </div>
        </li>
    </ul>
    <div class="bottom"></div>
</div>