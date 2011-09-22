<#if !element.parent??>
<form id="${element.properties.id!}" name="${element.properties.id!}" class="form-container" ${elementMetaData!} <#if element.properties.url??>action="${element.properties.url}"</#if> method="POST" enctype="multipart/form-data">
<input type="hidden" value="true" name="_SUBMITTED">
<input type="hidden" value="${element.properties.form_meta_original_id!}" name="_FORM_META_ORIGINAL_ID">
</#if>
    <#if error??><div class="form-error-message">${error}</div></#if>
    <#list element.children as e>
        ${e.render(formData, includeMetaData!false)}
    </#list>
<#if !element.parent??>
</form>
</#if>
