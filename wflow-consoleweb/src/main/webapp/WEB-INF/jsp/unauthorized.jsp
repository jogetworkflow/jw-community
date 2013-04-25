<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header />

<style>
    #main {
        width: 90%;
        margin: auto;
        float: none;
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
