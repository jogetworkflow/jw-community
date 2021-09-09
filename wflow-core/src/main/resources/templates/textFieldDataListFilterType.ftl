<script type="text/javascript" src="${contextPath}/plugin/org.joget.apps.datalist.lib.TextFieldDataListFilterType/js/jquery.placeholder.min.js"></script>
<#if (filterSelectionNumeric! == 'true' || filterSelectionString! == 'true')>
    <select id="${name!?replace(".", "_")}_searchType" name="${name!}_searchType" style="display:none">
        <option <#if searchType! == 'any'>selected</#if> value="any">@@datalist.tfdlft.filter.any@@</option>
        
        <#if filterSelectionNumeric! == 'true'>
        <option <#if searchType! == 'largerThan'>selected</#if> value="largerThan">@@datalist.tfdlft.filter.largerThan@@</option>
        <option <#if searchType! == 'smallerThan'>selected</#if> value="smallerThan">@@datalist.tfdlft.filter.smallerThan@@</option>
        </#if>
        
        <#if filterSelectionString! == 'true'>
        <option <#if searchType! == 'startsWith'>selected</#if> value="startsWith">@@datalist.tfdlft.filter.startsWith@@</option>
        <option <#if searchType! == 'endsWith'>selected</#if> value="endsWith">@@datalist.tfdlft.filter.endsWith@@</option>
        <option <#if searchType! == 'exact'>selected</#if> value="exact">@@datalist.tfdlft.filter.exact@@</option>
        </#if>
    </select>
</#if>
<input id="${name!?replace(".", "_")}" name="${name!}" type="text" size="10" value="${value!?html}" placeholder="${label!?html}"/>
<script type="text/javascript">
    $(document).ready(function(){
        var searchTypeTimeout${jsTimeoutName!};
        
        $('#${name!?replace(".", "_")}').placeholder();

        if ($('#${name!?replace(".", "_")}_searchType').length > 0) {
            if ($("#${name!?replace(".", "_")}_searchType").val() !== "any") {
                $('#${name!?replace(".", "_")}_searchType').show();
            }
            $('#${name!?replace(".", "_")}, #${name!?replace(".", "_")}_searchType').focus(function(e){
                clearTimeout(searchTypeTimeout${jsTimeoutName!});
                $('#${name!?replace(".", "_")}_searchType').show();
            });
            $('#${name!?replace(".", "_")}, #${name!?replace(".", "_")}_searchType').blur(function(e){
                searchTypeTimeout${jsTimeoutName!} = setTimeout( function(){
                    if ($("#${name!?replace(".", "_")}_searchType").val() === "any") {
                        $("#${name!?replace(".", "_")}_searchType").hide();
                    }
                }, 1000);
            });
        }
    });
</script>