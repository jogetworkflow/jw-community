<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<html>
    <body>
        <p>
        <img src="${pageContext.request.contextPath}/images/v3/loading.gif" />
        </p>

        <%
            String designerwebBaseUrl = "http://" + pageContext.getRequest().getServerName() + ":" + pageContext.getRequest().getServerPort();
            if(WorkflowUtil.getSystemSetupValue("designerwebBaseUrl") != null && WorkflowUtil.getSystemSetupValue("designerwebBaseUrl").length() > 0)
                designerwebBaseUrl = WorkflowUtil.getSystemSetupValue("designerwebBaseUrl");

            if(designerwebBaseUrl.endsWith("/"))
                designerwebBaseUrl = designerwebBaseUrl.substring(0, designerwebBaseUrl.length()-1);
        %>

        <c:set var="viewerUrl"><%= designerwebBaseUrl %>/wflow-wfdesignerweb/viewer/viewer.jsp?processId=${wfProcess.encodedId}</c:set>
        <form id="viewerForm" name="viewerForm" method="POST" action="${viewerUrl}">
            <dl>
                <dd><textarea style="visibility:hidden" name="xpdl" rows="20" cols="80"><c:out value="${xpdl}" escapeXml="true"/></textarea></dd>
                <dd><input type="hidden" name="packageId" value="${wfProcess.packageId}" /></dd>
                <dd><input type="hidden" name="processId" value="${wfProcess.id}" /></dd>
                <c:forEach items="${runningActivityIds}" var="activityId">
                    <dd><input type="hidden" name="activityId" value="${activityId}" /></dd>
                </c:forEach>
            </dl>
        </form>

        <script>
            document.forms['viewerForm'].submit();
        </script>
    </body>
</html>
