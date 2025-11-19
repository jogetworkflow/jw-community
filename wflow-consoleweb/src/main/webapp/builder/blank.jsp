<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.SecurityUtil"%>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <meta name="description" content="">
        <meta name="author" content="">
        <title></title>
        
        <link href="${pageContext.request.contextPath}/wro/common.css" rel="stylesheet" />
        <link href="${pageContext.request.contextPath}/wro/jds.min.css" rel="stylesheet" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/line-awesome-1.3.0/css/line-awesome.min.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/universal/lib/material-design-iconic-font/css/material-design-iconic-font.min.css" />
        <script src="${pageContext.request.contextPath}/wro/common.preload.js"></script>
        <script src="${pageContext.request.contextPath}/wro/common.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/sweetAlert2/resources/sweetalert2.min.js" ></script>
        <script src="${pageContext.request.contextPath}/wro/jds.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/tiny_mce/js/tinymce/tinymce.min.js"></script>
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
            .mce-tinymce-inline {margin-left: 230px !important;}
        </style>
        <script>
            UI.base = "${pageContext.request.contextPath}";
            ConnectionManager.tokenName = "<%= SecurityUtil.getCsrfTokenName() %>";
            ConnectionManager.tokenValue = "<%= SecurityUtil.getCsrfTokenValue(request) %>";
            JPopup.tokenName = "<%= SecurityUtil.getCsrfTokenName() %>";
            JPopup.tokenValue = "<%= SecurityUtil.getCsrfTokenValue(request) %>";
            UI.msg = {
                "ok" : "<ui:msgEscJS key="general.method.label.ok"/>",
                "cancel" : "<ui:msgEscJS key="general.method.label.cancel"/>"
            };
        </script>
    </head>
    <body>
    </body>
</html>
