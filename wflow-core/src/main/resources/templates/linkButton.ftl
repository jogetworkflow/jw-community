<div class="form-cell" ${elementMetaData!}>
    <#if element.properties.confirmation?? >
        <button id="${elementParamName!}" name="${elementParamName!}" class="form-button" onclick="if(confirm('${element.properties.confirmation}')){${element.properties.target!}.location='${element.properties.url!}';}return false">${element.properties.label!?html}</button>
    <#else>
        <button id="${elementParamName!}" name="${elementParamName!}" class="form-button" onclick="${element.properties.target!}.location='${element.properties.url!}';return false">${element.properties.label!?html}</button>
    </#if>
</div>
