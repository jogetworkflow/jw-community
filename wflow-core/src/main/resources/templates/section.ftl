<div id="${elementParamName!}" class="form-section <#if !(element.properties.label?? && element.properties.label != "") && includeMetaData == false >no_label</#if> section_${element.properties.elementUniqueKey!}" ${elementMetaData!} <#if visible == false && includeMetaData == false>style="display: none"</#if>>
    <div class="form-section-title"><#if element.properties.label?? && element.properties.label != ""><span>${element.properties.label!}</span></#if></div>
    <#list element.children as e>
        ${e.render(formData, includeMetaData!false)}
    </#list>
    
<#if rules?? && includeMetaData == false>
<script type="text/javascript">
    $(document).ready(function() {
        new VisibilityMonitor($('.section_${element.properties.elementUniqueKey!}'), ${rules}).init();
    });
</script>
</#if>
<#if includeMetaData == false>
<div style="clear:both"></div>
</#if>
</div>
