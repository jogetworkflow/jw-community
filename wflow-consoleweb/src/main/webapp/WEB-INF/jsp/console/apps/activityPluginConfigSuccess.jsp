<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<script type="text/javascript">
    var parentUrlQueryString = parent.location.search;
    if(parentUrlQueryString == '')
        parent.location.href = parent.location.href + "?tab=toolList&activityDefId=<c:out value="${activityDefId}"/>";
    else{
        if(parentUrlQueryString.indexOf('tab') == -1)
            parent.location.href = parent.location.href + "&tab=toolList&activityDefId=<c:out value="${activityDefId}"/>";
        else{

            parent.location.href = parent.location.href.replace(parentUrlQueryString, '') + "?tab=toolList&activityDefId=<c:out value="${activityDefId}"/>";
        }
    }
</script>