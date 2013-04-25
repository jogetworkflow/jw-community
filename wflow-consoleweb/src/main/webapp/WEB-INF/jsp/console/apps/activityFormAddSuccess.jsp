<script type="text/javascript">
    //parent.location.reload(true);
    var parentUrlQueryString = parent.location.search;
    if(parentUrlQueryString == '')
        parent.location.href = parent.location.href + "?tab=activityList&activityDefId=${activityDefId}";
    else{
        if(parentUrlQueryString.indexOf('tab') == -1)
            parent.location.href = parent.location.href + "&tab=activityList&activityDefId=${activityDefId}";
        else{

            parent.location.href = parent.location.href.replace(parentUrlQueryString, '') + "?tab=activityList&activityDefId=${activityDefId}";
        }
    }
</script>