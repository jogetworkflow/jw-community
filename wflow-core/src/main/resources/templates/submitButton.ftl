<div class="form-cell" ${elementMetaData!}>
    <#if element.properties.readonly! != 'true'><input id="${elementParamName!}" name="${elementParamName!}" class="form-button" type="submit" value="${element.properties.label!?html}" <#if element.properties.disabled! == 'true'>disabled</#if> /></#if>
</div>
