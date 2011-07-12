<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>
<html>
    <head>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.4.4.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.8.6.min.js"></script>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form.css" />
        
        <c:if test="${rightToLeft == 'true'}">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form_rtl.css" />
        </c:if>
            
        <script type="text/javascript">
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
                    <textarea id="form-json" name="json" cols="80" rows="10" style="font-size: smaller"><c:out value="${elementJson}" escapeXml="false" /></textarea>
                </form>
                <button onclick="reloadForm()">Reload</button>
            </div-->
        </fieldset>

    </body>
</html>
