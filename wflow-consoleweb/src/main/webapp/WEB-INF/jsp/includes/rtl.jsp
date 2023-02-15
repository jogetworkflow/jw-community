<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<c:choose>
    <c:when test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
        <link id="rtlcss" rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/rtl.css?build=<fmt:message key="build.number"/>">
        <script type="text/javascript">
            UI.rtl = true;
            $(document).ready(function(){
                $("body").addClass("rtl");
                $(".row-content").append("<div style=\"clear:both\"></div>");

                if( window.self !== window.parent && window.parent.UI !== undefined) {
                    window.parent.UI.rtl = true;
                    $(window.parent.document).find('body').addClass("rtl");
                }
            });
        </script>
    </c:when>
    <c:otherwise>
        <script type="text/javascript">
            $(document).ready(function(){
                if( window.self !== window.parent && window.parent.UI !== undefined) {
                    window.parent.UI.rtl = false;
                    $(window.parent.document).find('body').removeClass("rtl");
                }
            });
        </script>
    </c:otherwise>
</c:choose>