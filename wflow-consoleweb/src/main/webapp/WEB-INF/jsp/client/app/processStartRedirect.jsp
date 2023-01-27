<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <script>
            $(document).ready(function(){
               $("#postStart").append('<input name="'+ConnectionManager.tokenName+'" value="'+ConnectionManager.tokenValue+'" type="hidden" />');
               $("#postStart").submit(); 
            });
        </script>
    </head>
    <body>
        <form id="postStart" method="POST" action="${pageContext.request.contextPath}${formUrl}">
        </form>
    </body>
</html>
