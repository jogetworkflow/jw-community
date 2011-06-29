<div class="form-cell" ${elementMetaData!}>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <select id="${elementParamName!}" name="${elementParamName!}" <#if element.properties.size?? && element.properties.size != ''> size="${element.properties.size!}"</#if> <#if element.properties.multiple! == 'true'>multiple</#if> <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'> disabled </#if>>
        <#list options?keys as key>
            <option value="${key}" <#if values?? && values?seq_contains(key)>selected</#if> <#if element.properties.readonly! == 'true'>disabled</#if>>${options[key]}</option>
        </#list>
    </select>
    <#if element.properties.readonly! == 'true'>    
        <#list values as value>
            <input type="hidden" name="${elementParamName!}" value="${value?html}" />
        </#list>
    </#if>
</div>
