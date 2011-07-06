<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<link href="${pageContext.request.contextPath}/css/builder_navigator.css" rel="stylesheet" type="text/css" />

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
                        <c:when test="${(fn:length(formDefinitionList) gt 0 && builder ne 'f') || (fn:length(formDefinitionList) gt 1 && builder eq 'f')}">
                            <c:forEach items="${formDefinitionList}" var="formDef">
                                <c:if test="${!(builder eq 'f' && id eq formDef.id)}">
                                    <li>
                                        <a href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/form/builder/${formDef.id}">${formDef.name}</a>
                                        <a title="<fmt:message key="general.method.label.openNewWindow"/>" class="builder_qn_open" target="_blank" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/form/builder/${formDef.id}"><span><fmt:message key="general.method.label.openNewWindow"/></span></a>
                                        <div class="clear"></div>
                                    </li>
                                </c:if>
                            </c:forEach>
                         </c:when>
                         <c:otherwise>
                             <li><fmt:message key="general.method.label.noItemAvailable"/></li>
                         </c:otherwise>           
                    </c:choose>                                    
                </ul>
            </div>
        </div>
        <div id="builder_qv_db" class="builder_qn_type">
            <a class="builder_qn_type_name"><span class="col-1"></span><span class="col-2"><fmt:message key="dbuilder.title"/></span><span class="col-3"></span></a>
            <div class="builder_qn_items">
                <ul>
                    <c:choose>
                        <c:when test="${(fn:length(datalistDefinitionList) gt 0 && builder ne 'd') || (fn:length(datalistDefinitionList) gt 1 && builder eq 'd')}">
                            <c:forEach items="${datalistDefinitionList}" var="listDef">
                                <c:if test="${!(builder eq 'd' && id eq listDef.id)}">
                                    <li>
                                        <a href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/datalist/builder/${listDef.id}">${listDef.name}</a>
                                        <a title="<fmt:message key="general.method.label.openNewWindow"/>" class="builder_qn_open" target="_blank" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/datalist/builder/${listDef.id}"><span><fmt:message key="general.method.label.openNewWindow"/></span></a>
                                        <div class="clear"></div>
                                    </li>
                                </c:if>
                            </c:forEach>
                         </c:when>
                         <c:otherwise>
                             <li><fmt:message key="general.method.label.noItemAvailable"/></li>
                         </c:otherwise>           
                    </c:choose>
                </ul>
            </div>
        </div>
        <div id="builder_qv_ub" class="builder_qn_type">
            <a class="builder_qn_type_name"><span class="col-1"></span><span class="col-2"><fmt:message key="ubuilder.title"/></span><span class="col-3"></span></a>
            <div class="builder_qn_items">
                <ul>
                    <c:choose>
                        <c:when test="${(fn:length(userviewDefinitionList) gt 0 && builder ne 'u') || (fn:length(userviewDefinitionList) gt 1 && builder eq 'u')}">
                            <c:forEach items="${userviewDefinitionList}" var="userviewDef">
                                <c:if test="${!(builder eq 'u' && id eq userviewDef.id)}">
                                    <li>
                                        <a href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/userview/builder/${userviewDef.id}">${userviewDef.name}</a>
                                        <a title="<fmt:message key="general.method.label.openNewWindow"/>" class="builder_qn_open" target="_blank" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/userview/builder/${userviewDef.id}"><span><fmt:message key="general.method.label.openNewWindow"/></span></a>
                                        <div class="clear"></div>
                                    </li>
                                </c:if>
                            </c:forEach>
                         </c:when>
                         <c:otherwise>
                             <li><fmt:message key="general.method.label.noItemAvailable"/></li>
                         </c:otherwise>           
                    </c:choose>
                </ul>
            </div>
        </div>
    </div>
</div>
