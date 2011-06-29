<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:if test="${!jsonUiInRequest}">

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/themes/themeroller/jquery-ui-themeroller.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/flexigrid/css/flexigrid/flexigrid.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/autocomplete/jquery.autocomplete.css"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.4.4.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.8.6.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/flexigrid/flexigrid.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/ui.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/util.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/autocomplete/lib/jquery.bgiframe.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/autocomplete/jquery.autocomplete.pack.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/md5/jquery.md5.js"></script>

    <!-- jquery clue tip -->
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/jquerycluetip/css/jquery.cluetip.css">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquerycluetip/jquery.hoverIntent.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquerycluetip/jquery.cluetip.js"></script>

    <c:set var="jsonUiInRequest" scope="request" value="true"/>
</c:if>
