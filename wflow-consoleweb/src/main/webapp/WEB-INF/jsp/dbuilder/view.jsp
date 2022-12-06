<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-3.5.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-migrate-3.0.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui.custom.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/util.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/loadCSS/loadCSS.js"></script>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/themes/ui-lightness/jquery-ui.custom.css">
    
    <link rel="stylesheet" href="<c:url value="/css/datalistBuilderView.css"/>?build=<fmt:message key="build.number"/>" />
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

        <!--[if IE]><div id="preview-label" class="ie"><fmt:message key="fbuilder.preview"/></div><![endif]-->
        <!--[if !IE]><!--><div id="preview-label"><fmt:message key="fbuilder.preview"/></div><!--<![endif]-->        

    <script>
        $(function() {
            $("#preview input[type=submit], #preview .actions button").attr("disabled", "disabled");
            $("#preview tbody a").attr("onclick", "return false");
            $("#preview .actions button, #preview tbody a").click(function(e) {
                e.preventDefault();
                e.stopPropagation();
                return false;
            });
        });
    </script>
</body>
</html>
