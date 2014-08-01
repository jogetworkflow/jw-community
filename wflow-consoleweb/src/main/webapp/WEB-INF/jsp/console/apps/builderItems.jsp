<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<div id="builder_quick_navigator">
    <div class="builder_qn_label">
        <span>Quick Navigation : </span>
    </div>
    <div class="builder_qn_container">
        <div id="builder_qv_fb" class="builder_qn_type">
            <a class="builder_qn_type_name"><span class="col-1"></span><span class="col-2"><fmt:message key="fbuilder.title"/></span><span class="col-3"></span></a>
            <div class="builder_qn_items">
                <ul>
                    <c:choose>
                        <c:when test="${(fn:length(formDefinitionList) gt 0 && builder ne 'f') || (fn:length(formDefinitionList) gt 0 && builder eq 'f')}">
                            <c:forEach items="${formDefinitionList}" var="formDef">
                                <c:choose>
                                    <c:when test="${builder eq 'f' && id eq formDef.id}">
                                        <li class="active">
                                            <span><c:out value="${formDef.name}"/></span>
                                            <div class="clear"></div>
                                        </li>
                                    </c:when>
                                    <c:otherwise>
                                        <li>
                                            <a href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/form/builder/${formDef.id}"><c:out value="${formDef.name}"/></a>
                                            <a title="<fmt:message key="general.method.label.openNewWindow"/>" class="builder_qn_open" target="_blank" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/form/builder/${formDef.id}"><span><fmt:message key="general.method.label.openNewWindow"/></span></a>
                                            <div class="clear"></div>
                                        </li>
                                    </c:otherwise> 
                                </c:choose>
                            </c:forEach>
                         </c:when>
                         <c:otherwise>
                             <li><fmt:message key="general.method.label.noItemAvailable"/></li>
                         </c:otherwise>           
                    </c:choose>
                </ul>
                <ul>
                    <li class="buttons"><button onclick="navCreate('form')"><fmt:message key="console.form.create.label"/></button></li>
                </ul>
            </div>
        </div>
        <div id="builder_qv_db" class="builder_qn_type">
            <a class="builder_qn_type_name"><span class="col-1"></span><span class="col-2"><fmt:message key="dbuilder.title"/></span><span class="col-3"></span></a>
            <div class="builder_qn_items">
                <ul>
                    <c:choose>
                        <c:when test="${(fn:length(datalistDefinitionList) gt 0 && builder ne 'd') || (fn:length(datalistDefinitionList) gt 0 && builder eq 'd')}">
                            <c:forEach items="${datalistDefinitionList}" var="listDef">
                                <c:choose>
                                    <c:when test="${builder eq 'd' && id eq listDef.id}">
                                        <li class="active">
                                            <span><c:out value="${listDef.name}"/></span>
                                            <div class="clear"></div>
                                        </li>
                                    </c:when>
                                    <c:otherwise>
                                        <li>
                                            <a href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/datalist/builder/${listDef.id}"><c:out value="${listDef.name}"/></a>
                                            <a title="<fmt:message key="general.method.label.openNewWindow"/>" class="builder_qn_open" target="_blank" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/datalist/builder/${listDef.id}"><span><fmt:message key="general.method.label.openNewWindow"/></span></a>
                                            <div class="clear"></div>
                                        </li>
                                    </c:otherwise> 
                                </c:choose>
                            </c:forEach>
                         </c:when>
                         <c:otherwise>
                             <li><fmt:message key="general.method.label.noItemAvailable"/></li>
                         </c:otherwise>           
                    </c:choose>
                </ul>
                <ul>
                    <li class="buttons"><button onclick="navCreate('datalist')"><fmt:message key="console.datalist.create.label"/></button></li>
                </ul>
            </div>
        </div>
        <div id="builder_qv_ub" class="builder_qn_type">
            <a class="builder_qn_type_name"><span class="col-1"></span><span class="col-2"><fmt:message key="ubuilder.title"/></span><span class="col-3"></span></a>
            <div class="builder_qn_items">
                <ul>
                    <c:choose>
                        <c:when test="${(fn:length(userviewDefinitionList) gt 0 && builder ne 'u') || (fn:length(userviewDefinitionList) gt 0 && builder eq 'u')}">
                            <c:forEach items="${userviewDefinitionList}" var="userviewDef">
                                <c:choose>
                                    <c:when test="${builder eq 'u' && id eq userviewDef.id}">
                                        <li class="active">
                                            <span><c:out value="${userviewDef.name}"/></span>
                                            <div class="clear"></div>
                                        </li>
                                    </c:when>
                                    <c:otherwise>
                                        <li>
                                            <a href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/userview/builder/${userviewDef.id}"><c:out value="${userviewDef.name}"/></a>
                                            <a title="<fmt:message key="general.method.label.openNewWindow"/>" class="builder_qn_open" target="_blank" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/userview/builder/${userviewDef.id}"><span><fmt:message key="general.method.label.openNewWindow"/></span></a>
                                            <div class="clear"></div>
                                        </li>
                                    </c:otherwise> 
                                </c:choose>
                            </c:forEach>
                         </c:when>
                         <c:otherwise>
                             <li><fmt:message key="general.method.label.noItemAvailable"/></li>
                         </c:otherwise>           
                    </c:choose>
                </ul>
                <ul>
                    <li class="buttons"><button onclick="navCreate('userview')"><fmt:message key="console.userview.create.label"/></button></li>
                </ul>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        <ui:popupdialog var="builderwCreateDialog" src=""/>
        <c:if test="${!empty param.showCreate}">
            showCreateForm('<c:out value="${param.showCreate}" escapeXml="true"/>');
        </c:if>
        function navCreate(type){
            window.location.href = "?showCreate="+type;
        }
        function showCreateForm(type){
            builderwCreateDialog.src = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/" + type + "/create?builderMode=true";
            builderwCreateDialog.init();
        }
    </script>
</div>
