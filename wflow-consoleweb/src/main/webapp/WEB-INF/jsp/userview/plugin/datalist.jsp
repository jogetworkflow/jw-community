<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<c:if test="${requestParameters.isPreview eq 'true'}">
    <script>
        $(document).ready(function() {
            $(".dataList .actions button").attr("disabled", "disabled");
            $(".dataList form input, .dataList form select").attr("disabled", "disabled");
            $(".dataList a").attr("href", "#");
        });
    </script>
</c:if>

<script type="text/javascript" src="${requestParameters.contextPath}/js/jquery/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="${requestParameters.contextPath}/js/jquery/ui/jquery-ui-1.8.6.min.js"></script>

<link rel="stylesheet" href="<c:url value="/css/datalistBuilderView.css"/>" />
<div class="datalist-body-content">
    <c:if test="${!empty properties.customHeader}">
        ${properties.customHeader}
    </c:if>
    <c:if test="${!empty properties.error}">
        <h3>Error generating the data list</h3>
        <div id="error">${properties.error}</div>
    </c:if>

    <c:set scope="request" var="dataList" value="${properties.dataList}"/>

    <jsp:include page="/WEB-INF/jsp/dbuilder/dataListView.jsp" flush="true" />

    <c:if test="${!empty properties.customFooter}">
        ${properties.customFooter}
    </c:if>    
</div>
