<div class="form-cell" ${elementMetaData!}>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <div class="form-cell-value">
    <#list options?keys as key>
        <label>
        <input <#if element.properties.readonly! != 'true'>id="${elementParamName!}"</#if> name="${elementParamName!}" type="radio" value="${key}" <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'> disabled</#if> <#if value?? && value == key>checked</#if> />
        ${options[key]}
        </label>
    </#list>
        <#if element.properties.readonly! == 'true'><input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value!}" /></#if>
    </div>
    <div style="clear:both;"></div>
</div>