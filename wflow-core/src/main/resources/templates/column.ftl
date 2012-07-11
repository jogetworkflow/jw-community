<div id="${element.properties.id!}" class="form-column<#if element.properties.horizontal! == 'true'> form-column-horizontal</#if>" style="width: ${element.properties.width!}" ${elementMetaData!}>
    <#list element.children as e>
        ${e.render(formData, includeMetaData!false)}
    </#list>
</div>
