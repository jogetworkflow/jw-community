<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
        <link href="${pageContext.request.contextPath}/js/bootstrap4/css/bootstrap.min.css" rel="stylesheet" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/line-awesome-1.3.0/css/line-awesome.min.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/universal/lib/material-design-iconic-font/css/material-design-iconic-font.min.css" />
        <script src="${pageContext.request.contextPath}/wro/common.preload.js"></script>
        <script src="${pageContext.request.contextPath}/wro/common.js"></script>
        <script src="${pageContext.request.contextPath}/js/bootstrap4/js/bootstrap.min.js"></script>
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
        </script>
    </head>
    <body>
    </body>
</html>
