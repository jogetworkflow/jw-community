<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <title>
            <c:set var="html">
                ${userview.properties.name} &nbsp;&gt;&nbsp;
                <c:if test="${!empty userview.current}">
                    ${userview.current.properties.label}
                </c:if>
            </c:set>
            <ui:stripTag html="${html}"/>
        </title>

        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />

        <script type="text/javascript">
            $(document).ready(function () {

                $('.menu-link.default, .category-label a').click(function(){
                    var action = $(this).attr('href');
                    $('#preview').attr('action', action);
                    $('#preview').submit();

                    return false;
                });

                $('#header-link').click(function(){
                    var action = $(this).attr('href');
                    $('#preview').attr('action', action);
                    $('#preview').submit();

                    return false;
                });
            });

            ${userview.setting.theme.javascript}
            UI.base = "${pageContext.request.contextPath}";
            UI.userview_app_id = '${appId}';
            UI.userview_id = '${userview.properties.id}';
        </script>

        <link href="${pageContext.request.contextPath}/wro/userview.min.css?build=<fmt:message key="build.number"/>" rel="stylesheet" type="text/css" />
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon_uv.ico"/>
        <style type="text/css">
            <c:if test="${param.embed}">
                #header, #footer, #sidebar{display:none;}
                #content{float:left;}
            </c:if>
            ${userview.setting.theme.css}
        </style>
    </head>

    <c:set var="bodyId" scope="request" value=""/>
    <c:choose>
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

    <body id="${bodyId}" class="<c:if test="${param.embed}">embeded</c:if><c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}"> rtl</c:if>">
        <div id="page">
            <div id="header">

                <c:choose>
                    <c:when test="${!empty userview.setting.theme.header}">
                        <div id="header-inner">${userview.setting.theme.header}</div>
                    </c:when>
                    <c:otherwise>
                        <div id="header-info">
                            <div id="header-name">
                                <a href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/userview/builderPreview/${userview.properties.id}" id="header-link"><span id="name">${userview.properties.name}</span></a>
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
                        <span id="logoutText">${userview.properties.logoutText}</span>
                    </div>
                    <div class="clear"></div>
                </div>
            </div>
            <div id="main">
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
                                                <a href="${firstMenuItem.url}"><span>${category.properties.label}</span></a>
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
                <div id="content">
                    <c:if test="${!empty userview.setting.theme.beforeContent}">
                        ${userview.setting.theme.beforeContent}
                    </c:if>
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
                </div>
                <c:if test="${!empty userview.setting.theme.pageBottom}">
                    ${userview.setting.theme.pageBottom}
                </c:if>
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
        <!--[if IE]><div id="preview-label" class="ie"><fmt:message key="fbuilder.preview"/></div><![endif]-->
        <!--[if !IE]><!--><div id="preview-label"><fmt:message key="fbuilder.preview"/></div><!--<![endif]-->        
        <div style="display:none" id="preview-form">
            <form id="preview" action="" method="post">
                <input type="hidden" name="json" value="<c:out value="${json}"/>"/>
            </form>
        </div>
        <jsp:include page="/WEB-INF/jsp/includes/csrf.jsp" flush="true" />    
    </body>
</html>
