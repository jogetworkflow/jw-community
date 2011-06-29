<#if !element.parent??>
<form id="${element.properties.id!}" name="${element.properties.id!}" class="form-container" ${elementMetaData!} <#if element.properties.url??>action="${element.properties.url}"</#if> method="POST" enctype="multipart/form-data">
</#if>
    <#if error??><div class="form-error-message">${error}</div></#if>
    <#list element.children as e>
        ${e.render(formData, includeMetaData!false)}
    </#list>
<#if !element.parent??>
</form>
</#if>
