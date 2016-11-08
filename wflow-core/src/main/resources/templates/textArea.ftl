<div class="form-cell" ${elementMetaData!}>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
        <div class="form-cell-value">
            <div>${value!?html?replace("\n", "<br/>")}</div>
            <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value!?html}" />
        </div>
        <div style="clear:both;"></div>
    <#else>
        <textarea id="${elementParamName!}" name="${elementParamName!}" cols="${element.properties.cols!}"  rows="${element.properties.rows!}" <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'>readonly</#if>>${value!?html}</textarea>
    </#if>
</div>
