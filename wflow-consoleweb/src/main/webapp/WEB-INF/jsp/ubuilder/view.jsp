<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@page contentType="text/html" pageEncoding="utf-8"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<c:set var="isAnonymous" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
<c:if test="${!empty userview.setting.permission && !userview.setting.permission.authorize && isAnonymous}">
    <c:redirect url="/web/ulogin/${appId}/${userview.properties.id}/${key}"/>
</c:if>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <title>
            ${userview.properties.name} &nbsp;&gt;&nbsp;
            <c:if test="${!empty userview.current}">
                ${userview.current.properties.label}
            </c:if>
        </title>

        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />

        <script type="text/javascript">
            function userviewPrint(){
                $('head').append('<link id="userview_print_css" rel="stylesheet" href="${pageContext.request.contextPath}/css/userview_print.css" type="text/css" media="print"/>');
                setTimeout("do_print()", 1000); 
            }

            function do_print(){
                window.print();
                $('#userview_print_css').remove();
            }
            
            ${userview.setting.theme.javascript}
        </script>

        <link href="${pageContext.request.contextPath}/css/userview.css" rel="stylesheet" type="text/css" />

        <style type="text/css">
            ${userview.setting.theme.css}
        </style>
    </head>

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

    <body id="${bodyId}" class="<c:if test="${param.embed}">embeded</c:if><c:if test="${rightToLeft == 'true'}"> rtl</c:if>">
        <div id="page">
            <div id="header">

                <c:choose>
                    <c:when test="${!empty userview.setting.theme.header}">
                        <div id="header-inner">${userview.setting.theme.header}</div>
                    </c:when>
                    <c:otherwise>
                        <div id="header-info">
                            <div id="header-name">
                                <a href="${pageContext.request.contextPath}/web/userview/${appId}/${userview.properties.id}/${key}" id="header-link"><span id="name">${userview.properties.name}</span></a>
                            </div>
                            <div id="header-description">
                                <span id="description">${userview.properties.description}</span>
                            </div>
                            <div class="clear"></div>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div id="header-message">
                    <div id="header-welcome-message">
                        <span id="welcomeMessage">${userview.properties.welcomeMessage}</span>
                    </div>
                    <div id="header-logout-text">
                        <c:choose>
                            <c:when test="${isAnonymous}">
                                <a href="${pageContext.request.contextPath}/web/ulogin/${appId}/${userview.properties.id}/${key}"><span id="loginText"><fmt:message key="ubuilder.login"/></span></a>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/j_spring_security_logout"><span id="logoutText">${userview.properties.logoutText}</span></a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="clear"></div>
                </div>
            </div>
            <div id="main">
                <c:choose>
                    <c:when test="${!empty userview.setting.permission && !userview.setting.permission.authorize}">
                        <div id="content" class="unauthorize">
                            <h3><fmt:message key="ubuilder.noAuthorize"/></h3>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${!empty userview.setting.theme.pageTop}">
                            ${userview.setting.theme.pageTop}
                        </c:if>
                        <div id="navigation">
                            <div id="category-container">
                                <c:forEach items="${userview.categories}" var="category" varStatus="cStatus">
                                    <c:if test="${category.properties.hide ne 'yes'}">
                                        <c:set var="c_class" value=""/>

                                        <c:if test="${cStatus.first}">
                                            <c:set var="c_class" value="${c_class} first"/>
                                        </c:if>
                                        <c:if test="${cStatus.last}">
                                            <c:set var="c_class" value="${c_class} last"/>
                                        </c:if>
                                        <c:if test="${!empty userview.currentCategory && category.properties.id eq userview.currentCategory.properties.id}">
                                            <c:set var="c_class" value="${c_class} current-category"/>
                                        </c:if>
                                        
                                        <div id="${category.properties.id}" class="category ${c_class}">
                                            <div class="category-label">
                                                <c:set var="firstMenuItem" value="${category.menus[0]}"/>
                                                <c:choose>
                                                    <c:when test="${!empty firstMenuItem && firstMenuItem.homePageSupported}">
                                                        <c:set var="menuItemId" value="${firstMenuItem.properties.customId}"/>
                                                        <c:if test="${empty menuItemId}">
                                                            <c:set var="menuItemId" value="${firstMenuItem.properties.id}"/>
                                                        </c:if>
                                                        <a href="${pageContext.request.contextPath}/web/userview/${appId}/${userview.properties.id}/${key}/${menuItemId}"><span>${category.properties.label}</span></a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span>${category.properties.label}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="clear"></div>
                                            <div class="menu-container">
                                                <c:forEach items="${category.menus}" var="menu" varStatus="mStatus">
                                                    <c:set var="m_class" value=""/>

                                                    <c:if test="${mStatus.first}">
                                                        <c:set var="m_class" value="${m_class} first"/>
                                                    </c:if>
                                                    <c:if test="${mStatus.last}">
                                                        <c:set var="m_class" value="${m_class} last"/>
                                                    </c:if>
                                                    <c:if test="${!empty userview.current && menu.properties.id eq userview.current.properties.id}">
                                                        <c:set var="m_class" value="${m_class} current"/>
                                                    </c:if>

                                                    <div id="${menu.properties.id}" class="menu ${m_class}">
                                                        ${menu.menu}
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </div>
                        <c:if test="${!empty userview.setting.theme.beforeContent}">
                            ${userview.setting.theme.beforeContent}
                        </c:if>
                        <div id="content">
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
                                        <a href="${pageContext.request.contextPath}/web/userview/${appId}/${userview.properties.id}/${key}"><fmt:message key="ubuilder.pageNotFound.backToMain"/></a>
                                    </p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <c:if test="${!empty userview.setting.theme.pageBottom}">
                            ${userview.setting.theme.pageBottom}
                        </c:if>
                    </c:otherwise>
                </c:choose>
                <div class="clear"></div>
            </div>
            <div id="footer">

                <c:choose>
                    <c:when test="${!empty userview.setting.theme.footer}">
                        <div id="footer-inner">${userview.setting.theme.footer}</div>
                    </c:when>
                    <c:otherwise>
                        <div id="footer-message">
                            <span id="footerMessage">${userview.properties.footerMessage}</span>
                        </div>
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
        <script type="text/javascript">
            HelpGuide.base = "${pageContext.request.contextPath}"
            HelpGuide.attachTo = "#header";
            HelpGuide.key = "help.web.userview.${appId}.${userview.properties.id}.${bodyId}";
            HelpGuide.show();
        </script>
    </body>
</html>
