<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@page import="org.joget.apps.userview.model.UserviewMenu"%>
<%@page import="org.joget.workflow.util.WorkflowUtil"%>
<%@page contentType="text/html" pageEncoding="utf-8"%>

<c:set var="bodyId" scope="request" value=""/>
<c:choose>
    <c:when test="${!empty userview.setting.permission && !userview.setting.permission.authorize}">
        <c:set var="bodyId" scope="request" value="unauthorize"/>
    </c:when>
    <c:when test="${!empty userview.current}">
        <c:choose>
            <c:when test="${!empty userview.current.properties.customId}">
                <c:set var="bodyId" scope="request" value="${userview.current.properties.customId}"/>
            </c:when>
            <c:otherwise>
                <c:set var="bodyId" scope="request" value="${userview.current.properties.id}"/>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <c:set var="bodyId" scope="request" value="pageNotFound"/>
    </c:otherwise>
</c:choose>
            
<c:catch var="bodyError">
<c:set var="bodyContent">
    <c:choose>
        <c:when test="${!empty userview.current}">
            <c:set var="properties" scope="request" value="${userview.current.properties}"/>
            <c:set var="requestParameters" scope="request" value="${userview.current.requestParameters}"/>
            <c:set var="readyJspPage" value="${userview.current.readyJspPage}"/>
            <c:choose>
                <c:when test="${!empty readyJspPage}">
                    <jsp:include page="../${readyJspPage}" flush="true"/>
                </c:when>
                <c:otherwise>
                    ${userview.current.readyRenderPage}
                </c:otherwise>
            </c:choose>
        </c:when>
        <c:otherwise>
            <h3><fmt:message key="ubuilder.pageNotFound"/></h3>

            <fmt:message key="ubuilder.pageNotFound.message"/>
            <br><br>
            <fmt:message key="ubuilder.pageNotFound.explanation"/>
            <p>&nbsp;</p>
            <p>&nbsp;</p>
            <p>
                <a href="${pageContext.request.contextPath}/web/userview/${appId}/${userview.properties.id}/<c:out value="${key}"/>"><fmt:message key="ubuilder.pageNotFound.backToMain"/></a>
            </p>
        </c:otherwise>
    </c:choose>
</c:set>
            
<c:set var="alertMessageProperty" value="<%= UserviewMenu.ALERT_MESSAGE_PROPERTY %>"/>
<c:set var="alertMessageValue" value="${userview.current.properties[alertMessageProperty]}"/>
<c:set var="redirectUrlProperty" value="<%= UserviewMenu.REDIRECT_URL_PROPERTY %>"/>
<c:set var="redirectUrlValue" value="${userview.current.properties[redirectUrlProperty]}"/>

</c:catch>            

<c:set var="html">
    <c:if test="${!empty userview.current}">
        ${userview.current.properties.label}
    </c:if>
</c:set>
    
<jg-value name="title">
    <ui:stripTag html="${html}"/>
</jg-value>

<jg-value name="menus">
    <header class="bar bar-header bar-stable">
        <h5 class="appName"><strong>${userview.properties.name}</strong></h5>
    </header>
    <ion-content class="has-header">
        <ion-list>
            <a class="item item-icon-left nav-clear menu-close " ng-show="hasList" ng-click="viewList()">
                <i class="icon fa fa-th"></i> <strong>All Apps</strong>
            </a>
            <c:forEach items="${userview.categories}" var="category" varStatus="cStatus">
                <c:if test="${category.properties.hide ne 'yes'}"> 
                    <c:set var="c_class" value=""/>
                    <c:if test="${!empty userview.currentCategory && category.properties.id eq userview.currentCategory.properties.id}">
                        <c:set var="c_class" value="${c_class} current-category"/>
                    </c:if>
                    <div class="item item-divider menu-category ${c_class}">
                        <ui:stripTag html="${category.properties.label}" relaxed="true"/>
                    </div>
                    <c:forEach items="${category.menus}" var="menu" varStatus="mStatus">
                        <c:set var="m_class" value=""/>
                        <c:if test="${!empty userview.current && menu.properties.id eq userview.current.properties.id}">
                            <c:set var="m_class" value="${m_class} current"/>
                        </c:if>
                                                    
                        <a class="item item-icon-right nav-clear menu-close menu-item ${m_class}" href="${menu.url}">
                            <ui:stripTag html="${menu.properties.label}" relaxed="true"/>
                            <i class="icon ion-chevron-right"></i>
                        </a>
                    </c:forEach>
                </c:if>
            </c:forEach>
        </ion-list>
    </ion-content>
</jg-value>

<c:choose>
    <c:when test="${!empty redirectUrlValue}">
        <jg-result alert="${alertMessageValue}" redirect="loadPage('${redirectUrlValue}')"></jg-result>
    </c:when>
    <c:otherwise>
        <jg-result alert="${alertMessageValue}"></jg-result>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/ui.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/util.js"></script>

        <div class="uview_body">
            ${bodyContent}  
            <c:if test="${!empty bodyError}">
                ${bodyError}
                <pre>
                <%
                    Exception e = (Exception)pageContext.findAttribute("bodyError");
                    e.printStackTrace(new java.io.PrintWriter(out));
                %>
                </pre>
            </c:if>
            <div class="clearfix"></div>  
        </div>
    </c:otherwise>
</c:choose>
