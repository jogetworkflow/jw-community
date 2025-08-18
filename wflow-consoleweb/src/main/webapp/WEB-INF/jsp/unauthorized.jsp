<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%
    String uri = (String)request.getAttribute("jakarta.servlet.forward.request_uri");
    String contextPath = request.getContextPath();
    if (uri.startsWith(contextPath + "/web/json")) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }
%>

<commons:header />

<style>
    #main {
        width: 99%;
        margin: auto;
        float: none;
        visibility: visible;
        padding-top: 10px;
    }
    div#content-container {
        padding-right: 0px;
    }
</style>

<div id="main">
    <div id="main-body">

        <h3><fmt:message key="general.header.unauthorized"/></h3>
        
        <fmt:message key="general.content.unauthorized" />

    </div>
</div>

<script>
    Template.init("#menu-home", "#nav-home-unauthorized");
</script>

<commons:footer />
