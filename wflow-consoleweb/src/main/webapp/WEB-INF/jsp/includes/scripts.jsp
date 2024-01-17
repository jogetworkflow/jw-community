<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.directory.model.service.DirectoryUtil"%>
<%@ page import="org.joget.commons.util.SecurityUtil"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<c:if test="${!jsonUiInRequest}">
    
    <c:set var="userSecurity" scope="request" value='<%= DirectoryUtil.getUserSecurity() %>'/>

    <script type="text/javascript" src="${pageContext.request.contextPath}/wro/common.preload.js?build=<fmt:message key="build.number"/>"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/wro/common.js?build=<fmt:message key="build.number"/>"></script>
    <script>loadCSS("${pageContext.request.contextPath}/wro/common.css")</script>

    <c:set var="jsonUiInRequest" scope="request" value="true"/>
    
    <c:if test="${empty userSecurity || (!empty userSecurity && !userSecurity.allowSessionTimeout)}">
    <script type="text/javascript">
        $(document).ready(function() {
            window.setInterval("keepMeAlive('image_alive')", 200000);
        });
        function keepMeAlive(imgName) {  
             myImg = document.getElementById(imgName);   
             if (myImg) {
                 myImg.src = myImg.src.replace(/\?.*$/, '?' + Math.random());   
             } else {
                $('body').append('<img id="image_alive" width="1" height="1" src="${pageContext.request.contextPath}/images/v3/cj.gif?" alt="">');
             }
        }
    </script>
    </c:if>
    
    <!-- disabled using backspace key to navigate back in IE-->
    <script type="text/javascript">
        if ($.browser.msie) {
            $(document).on("keydown", function (e) {
                if (e.which === 8 && !$(e.target).is("input:not([readonly]), textarea:not([readonly])")) {
                    e.preventDefault();
                }
            });
        }
    </script>   
    
    <script>
        ConnectionManager.tokenName = "<%= SecurityUtil.getCsrfTokenName() %>";
        ConnectionManager.tokenValue = "<%= SecurityUtil.getCsrfTokenValue(request) %>";
        JPopup.tokenName = "<%= SecurityUtil.getCsrfTokenName() %>";
        JPopup.tokenValue = "<%= SecurityUtil.getCsrfTokenValue(request) %>";
        UI.locale = "<c:out value="${currentLocale}"/>";
        UI.theme = "<c:out value="${theme}"/>";
        
        if (window.self !== window.parent && window.parent.UI !== undefined
                && window.parent.UI.locale !== undefined && window.parent.UI.locale !== ""
                && window.parent.UI.locale !== UI.locale) {
            if (confirm("<ui:msgEscJS key="general.label.languageSwitching"/>")) {
                window.top.location.reload(true);
            }
        }
        
        if(window.parent.UI.theme === ""){
            window.parent.UI.theme = "<c:out value="${theme}"/>";
        }

        if (window.self !== window.parent && window.parent.UI !== undefined
                && window.parent.UI.theme !== undefined && window.parent.UI.theme !== ""
                && window.parent.UI.theme !== UI.theme) {
            if (confirm("<ui:msgEscJS key="general.label.themeSwitching"/>")) {
                window.parent.UI.theme = "<c:out value="${theme}"/>";
                window.top.location.reload(true);
            }
        }
    </script>
</c:if>
