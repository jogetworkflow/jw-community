<div class="form-cell" ${elementMetaData!}>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <div class="form-cell-value">
    <#list options?keys as key>
        <label>
        <input id="${elementParamName!}" name="${elementParamName!}" type="checkbox" value="${key}" <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'> onclick="this.checked=!this.checked;"</#if> <#if values?? && values?seq_contains(key)>checked</#if> />
        ${options[key]}
        </label>
    </#list>
    </div>
    <div style="clear:both;"></div>
</div>