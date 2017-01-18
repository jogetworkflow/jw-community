<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.directory.model.service.DirectoryUtil"%>
<%@ page import="org.joget.commons.util.SecurityUtil"%>

<c:if test="${!jsonUiInRequest}">
    
    <c:set var="userSecurity" scope="request" value='<%= DirectoryUtil.getUserSecurity() %>'/>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/wro/common.css?build=<fmt:message key="build.number"/>" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/wro/common.js?build=<fmt:message key="build.number"/>"></script>

    <c:set var="jsonUiInRequest" scope="request" value="true"/>
    
    <c:if test="${empty userSecurity || (!empty userSecurity && !userSecurity.allowSessionTimeout)}">
    <script type="text/javascript">
        $(document).ready(function(){
            $('body').append('<img id="image_alive" width="1" height="1" src="${pageContext.request.contextPath}/images/v3/clear.gif?" alt="">');
            window.setInterval("keepMeAlive('image_alive')", 200000);
        });
        function keepMeAlive(imgName)
        {  
             myImg = document.getElementById(imgName);   
             if (myImg)
                 myImg.src = myImg.src.replace(/\?.*$/, '?' + Math.random());   
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
    </script>
</c:if>
