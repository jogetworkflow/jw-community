<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        
        <link href="${pageContext.request.contextPath}/wro/common.css" rel="stylesheet" />
        <link href="${pageContext.request.contextPath}/js/bootstrap4/css/bootstrap.min.css" rel="stylesheet" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/form8.css" rel="stylesheet" />
        <script src="${pageContext.request.contextPath}/wro/common.preload.js"></script>
        <script src="${pageContext.request.contextPath}/wro/common.js"></script>
        <script src="${pageContext.request.contextPath}/js/bootstrap4/js/bootstrap.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/formUtil.js" ></script>
        
        <c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form_rtl.css?build=<fmt:message key="build.number"/>" />
        </c:if>
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
        <script type="text/javascript">
            UI.base = "${pageContext.request.contextPath}";
            
            var reloadForm = function() {
                $('#form-preview').submit();
                return false;
            };
            $(document).ready(function() {
                // add toggle json link
                $("#form-json-link").click(function() {
                    if ($("#form-info").css("display") != "block") {
                        $("#form-info").css("display", "block");
                    } else {
                        $("#form-info").css("display", "none");
                    }
                });
                // disable form buttons
                $("#form-view .form-button").attr("disabled", "disabled");
            });
        </script>
    </head>
    <body>

        <fieldset id="form-canvas">

            <c:out value="${elementTemplate}" escapeXml="false" />

            <p>&nbsp;</p>
            <!--a href="#" id="form-json-link" style="font-size: smaller" onclick="return false">DEBUG: Show JSON</a>
            <div id="form-info" style="display: none">
                <form id="form-preview" method="post">
                    <textarea id="form-json" name="json" cols="80" rows="10" style="font-size: smaller"><c:out value="${elementJson}" /></textarea>
                </form>
                <button onclick="reloadForm()">Reload</button>
            </div-->
        </fieldset>
                
        <jsp:include page="/WEB-INF/jsp/includes/csrf.jsp" flush="true" />    
    </body>
</html>
