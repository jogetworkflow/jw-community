<#if includeMetaData>
    <div class="form-cell" ${elementMetaData!}>
        <label class="label">${element.properties.id}</label>
        <span class="form-floating-label">@@form.hiddenfield.hiddenField@@</span>
        <input id="${elementParamName!}" name="${elementParamName!}" type="text" value="${value}" readonly />
    </div>
<#else>
    <#if element.properties.readonly! != 'true'><input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value}"/></#if>
</#if>
