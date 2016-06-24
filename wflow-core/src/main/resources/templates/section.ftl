<div id="${elementParamName!}" class="form-section <#if !(element.properties.label?? && element.properties.label != "") >no_label</#if> section_${element.properties.elementUniqueKey!}" ${elementMetaData!} <#if visible == false && includeMetaData == false>style="display: none"</#if>>
    <div class="form-section-title"><#if element.properties.label?? && element.properties.label != ""><span>${element.properties.label!}</span></#if></div>
    <#list element.children as e>
        ${e.render(formData, includeMetaData!false)}
    </#list>
    
<#if visibilityControlParam?? && includeMetaData == false>
<script type="text/javascript">
    $(document).ready(function() {
        new VisibilityMonitor($('.section_${element.properties.elementUniqueKey!}'), "${visibilityControlParam!}", "${element.properties.visibilityValue!}", "${element.properties.regex!}").init();
    });
</script>
</#if>
<#if includeMetaData == false>
<div style="clear:both"></div>
</#if>
</div>
