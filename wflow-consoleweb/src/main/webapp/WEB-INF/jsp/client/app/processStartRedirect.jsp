<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <script>
            $(document).ready(function(){
               $("#postStart").submit(); 
            });
        </script>
    </head>
    <body>
        <form id="postStart" method="POST" action="${pageContext.request.contextPath}${formUrl}">
        </form>
    </body>
</html>
