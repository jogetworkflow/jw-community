<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<html>
<head>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.5.2.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.8.6.min.js"></script>

    <link rel="stylesheet" href="<c:url value="/css/datalistBuilderView.css"/>" />
    <style>
        * { font-family:'PT Sans',Arial; font-size:13px; line-height:16px }
        body { width:900px }
    </style>
</head>

<body>
<div id="preview">
    <c:if test="${!empty error}">
        <h3><fmt:message key="dbuilder.errorGenerating"/></h3>
        <div id="error">${error}</div>
    </c:if>

    <c:set scope="request" var="dataListId" value="${dataList.id}"/>
    
    <jsp:include page="/WEB-INF/jsp/dbuilder/dataListView.jsp" flush="true" />

</body>
</html>
