<div class="form-cell" ${elementMetaData!}>
    <#if element.properties.confirmation?? >
        <button id="${elementParamName!}" name="${elementParamName!}" class="form-button" onclick="if(confirm('${element.properties.confirmation}')){top.location='${element.properties.url!}';}return false">${element.properties.label!}</button>
    <#else>
        <button id="${elementParamName!}" name="${elementParamName!}" class="form-button" onclick="top.location='${element.properties.url!}';return false">${element.properties.label!}</button>
    </#if>
</div>
