<div id="${element.properties.id!}" class="form-column<#if element.properties.horizontal! == 'true'> form-column-horizontal</#if>" style="width: ${element.properties.width!}" ${elementMetaData!}>
    <#if element.properties.label??>
        <h3 class="form-column-label">
            ${element.properties.label!}
        </h3>
    </#if>
    <#list element.children as e>
        ${e.render(formData, includeMetaData!false)}
    </#list>
</div>
