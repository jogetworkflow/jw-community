<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.apps.app.controller.ConsolePagePluginController"%>

<% 
    // Retrieve the menuLocation from the request scope
    String location = (String) request.getAttribute("menuLocation");
%>

<c:set var="menus" value="<%= ConsolePagePluginController.getConsolePageMenus(location) %>"/>
<c:forEach var="item" items="${menus}">
    <li id="${item.id}" data-menu-order="${item.order}">
        <a class="nav-link" href="${pageContext.request.contextPath}${item.url}">
            <span class="nav-steps">${item.icon}</span>${item.label}
        </a>
    </li>
</c:forEach>

<div id="adminWelcome">
    <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />
</div>

<div id="spinner-container" style="position:fixed;top:50%;left:50%;transform:translate(-50%, -50%);color:#009265;font-size:50px;"><i class="fas fa-spinner fa-spin"></i></div>