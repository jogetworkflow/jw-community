<script type="text/javascript" src="${contextPath}/plugin/org.joget.apps.datalist.lib.TextFieldDataListFilterType/js/jquery.placeholder.min.js"></script>
<input id="${name!?replace(".", "_")}" name="${name!}" type="text" size="10" value="${value!?html}" placeholder="${label!?html}"/>
<script type="text/javascript">
    $(document).ready(function(){
        $('#${name!?replace(".", "_")}').placeholder();
    });
</script>