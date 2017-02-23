<div class="form-cell" ${elementMetaData!}>
    <#if element.properties.confirmation?? >
        <button id="${elementParamName!}" name="${elementParamName!}" class="form-button btn button ${element.properties.cssClass!?html}" onclick="if(confirm('${element.properties.confirmation}')){${element.properties.target!}.location='${element.properties.url!}';}return false">${element.properties.label!?html}</button>
    <#else>
        <button id="${elementParamName!}" name="${elementParamName!}" class="form-button btn button ${element.properties.cssClass!?html}" onclick="${element.properties.target!}.location='${element.properties.url!}';return false">${element.properties.label!?html}</button>
    </#if>
</div>
