<%@ page isErrorPage="true" %>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <title>Joget Workflow</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/v3/joget.ico"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/home/style.css"/>
    </head>
    <body>
        <div id="container">
            <div id="logo">
                <a href="${pageContext.request.contextPath}/"><img src="${pageContext.request.contextPath}/home/logo.png" border="0" height="60" /></a>
            </div>
            <div id="title">
                Internal server error (Code 500)
            </div>
            <div id="content">
                <br><br>
                Sorry, an unintended problem has occurred.
                <br><br>
                If you would like to help report this incident, please copy the full error message and send it to your administrator.
                <p>&nbsp;</p>

                <div>
                    <script type="text/javascript">
                        function showStackTrace() {
                            document.getElementById("stack_trace").style.display = "block";
                        }
                    </script>
                    <a href="#" onclick="showStackTrace()">Show Error Message</a>
                    <p>&nbsp;</p>
                    <p>&nbsp;</p>
                </div>

                <div id="stack_trace" style="display:none; overflow:auto; font-size:10px; text-align: left">
                    <hr />
                    <c:set var="error">
                        <% exception.printStackTrace(new java.io.PrintWriter(out));%>
                    </c:set>
                    <pre><c:out value="${error}" escapeXml="true"/></pre>
                </div>
            </div>
            <div id="footer">
                <a href="http://www.joget.com">&copy; Joget Workflow - Open Dynamics Inc</a>
            </div>
        </div>
    </body>
</html>
