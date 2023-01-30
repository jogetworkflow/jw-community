<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />

        <link href="${pageContext.request.contextPath}/wro/common.css" rel="stylesheet" />
        <link href="${pageContext.request.contextPath}/js/bootstrap4/css/bootstrap.min.css" rel="stylesheet" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/form.css" rel="stylesheet" />
        <script src="${pageContext.request.contextPath}/wro/common.preload.js"></script>
        <script src="${pageContext.request.contextPath}/wro/common.js"></script>
        <script src="${pageContext.request.contextPath}/js/bootstrap4/js/popper.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/bootstrap4/js/bootstrap.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/formUtil.js" ></script>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/datalist8.css?build=<fmt:message key="build.number"/>" />
        <style>
            html, body
            {
                width:100%;
                height:100%;
                height: auto !important;
                margin: 0 !important;
                padding: 0px;
                border: 0 !important;
            }
            body {
                padding: 25px !important;
            }
        </style>
        <script>
            var _enableResponsiveTable = true;
        </script>    
    </head>

    <body>
        <div id="preview">
            <c:if test="${!empty error}">
                <h3><fmt:message key="dbuilder.errorGenerating"/></h3>
                <div id="error">${error}</div>
            </c:if>

            <c:set scope="request" var="dataListId" value="${dataList.id}"/>

            <jsp:include page="/WEB-INF/jsp/dbuilder/dataListView.jsp" flush="true" />     

        </div>
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
