<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.directory.model.service.DirectoryUtil"%>
<%@ page import="org.joget.commons.util.SecurityUtil"%>

<c:if test="${!jsonUiInRequest}">
    
    <c:set var="userSecurity" scope="request" value='<%= DirectoryUtil.getUserSecurity() %>'/>

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/themes/ui-lightness/jquery-ui-1.10.3.custom.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/flexigrid/css/flexigrid/flexigrid.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/guiders/guiders-1.1.1.css"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-migrate-1.2.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.10.3.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/flexigrid/flexigrid.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/ui.js?build=<fmt:message key="build.number"/>"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/ui_ext.js?build=<fmt:message key="build.number"/>"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/util.js?build=<fmt:message key="build.number"/>"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/md5/jquery.md5.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.cookie.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/guiders/guiders-1.1.1.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.blockUI.js"></script>

    <!-- jquery clue tip -->
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/jquerycluetip/css/jquery.cluetip.css">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquerycluetip/jquery.hoverIntent.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquerycluetip/jquery.cluetip.js"></script>

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
    </script>
</c:if>
