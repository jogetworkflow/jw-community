<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="jsonUiInRequest" scope="request" value="false"/>

<commons:header />

<div id="nav">
    <div id="nav-title">
        <p>${pluginIcon} ${pluginLabel}</p>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="subMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    ${content}
</div>

<script>
    Template.init("", "#${pluginName}");
</script>

<commons:footer />
