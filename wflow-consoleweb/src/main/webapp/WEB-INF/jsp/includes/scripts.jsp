<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:if test="${!jsonUiInRequest}">

    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/themes/themeroller/jquery-ui-themeroller.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/flexigrid/css/flexigrid/flexigrid.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/autocomplete/jquery.autocomplete.css"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/guiders/guiders-1.1.1.css"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.5.2.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.8.6.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/flexigrid/flexigrid.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/ui.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/util.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/autocomplete/lib/jquery.bgiframe.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/autocomplete/jquery.autocomplete.pack.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/md5/jquery.md5.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.cookie.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/guiders/guiders-1.1.1.js"></script>

    <!-- jquery clue tip -->
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/jquerycluetip/css/jquery.cluetip.css">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquerycluetip/jquery.hoverIntent.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquerycluetip/jquery.cluetip.js"></script>

    <c:set var="jsonUiInRequest" scope="request" value="true"/>
    
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
