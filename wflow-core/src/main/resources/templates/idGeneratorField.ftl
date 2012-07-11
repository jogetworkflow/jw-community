<div class="form-cell" ${elementMetaData!}>
    <label class="label">${element.properties.label}</label>
    <span>${value!?html}<#if !value?? || value == ''>@@form.idgeneratorfield.auto@@</#if></span>
    <#if value?? ><input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value}" /></#if>
</div>
