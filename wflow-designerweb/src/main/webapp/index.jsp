<%--@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="serverPath" value="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}" />

<html>
    <head>
        <style>
            li a { 
                font-size: 18px;
                font-family: Trebuchet, Arial;
                line-height: 30px;
                text-decoration: none;
            }
            li a span {
                display: block;
                font-size: 12px;
                text-decoration: none;
                padding-left: 20px;
            }
            em {
                font-weight: bold;
                font-style: normal;
            }
        </style>
    </head>
    <body>
        <ul>
            <li><a href="webstart/launch.jnlp">Web Start wfdesigner <span class="link">(webstart/launch.jnlp)</span></a></li>
            <li><a href="test/webstart.jsp?file=workflow_patterns.xpdl">Web Start wfdesigner with XPDL filename <span class="link">(test/webstart.jsp?file=workflow_patterns.xpdl)</span></a></li>
            <li><a href="<c:url value="test/webstart.jsp"><c:param name="url" value="${serverPath}/test/workflow_patterns.xpdl" /></c:url>">Web Start wfdesigner with URL <span class="link">(<c:url value="test/webstart.jsp"><c:param name="url" value="${serverPath}/test/workflow_patterns.xpdl" /></c:url>)</span></a></li>
            <li><a href="<c:url value="test/viewerTest.jsp" />">JPEG Viewer for a running process</em> <span class="link">(<c:url value="test/viewerTest.jsp" />)</span></a></li>
        </ul>
    </body>

</html>--%>