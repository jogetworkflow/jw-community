<div class="form-cell customHtml ${element.properties.id!}" ${elementMetaData!}>
    <#if element.properties.label != "">
        <label class="label">${element.properties.label!} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    </#if>
    <#if elementMetaData != ""><span class="form-floating-label">@@form.customhtml.customHtml@@</span></#if>
    <div class="form-cell-value <#if element.properties.label == ''>form-cell-full</#if>">
    ${value!}
    </div>
    <div style="clear:both;"></div>
</div>
