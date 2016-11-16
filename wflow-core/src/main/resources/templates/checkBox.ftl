<div class="form-cell" ${elementMetaData!}>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <div class="form-cell-value" id="${elementParamName!}${element.properties.elementUniqueKey!}">
    <#list options as option>
        <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
            <#if values?? && values?seq_contains(option.value!)>
                <label class="readonly_label">
                    <span>${option.label!?html}</span>
                    <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${option.value!?html}" />
                </label>
            </#if>
        <#else>
            <label>
                <#if element.properties.readonly! == 'true' && values?? && values?seq_contains(option.value!)>
                    <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${option.value!?html}" />
                </#if>
                <input grouping="${option.grouping!?html}" <#if element.properties.readonly! != 'true'>id="${elementParamName!}" name="${elementParamName!}"</#if> type="checkbox" value="${option.value!?html}" <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'> disabled</#if> <#if values?? && values?seq_contains(option.value!)>checked</#if> />
                ${option.label!?html}
            </label>
        </#if>
    </#list>
    </div>
    <div style="clear:both;"></div>

    <#if (element.properties.controlField?? && element.properties.controlField! != "" && !(element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true')) >
        <script type="text/javascript">
            $(document).ready(function(){
                $("#${elementParamName!}${element.properties.elementUniqueKey!}").dynamicOptions({
                    controlField : "${element.properties.controlFieldParamName!}",
                    paramName : "${elementParamName!}",
                    type : "checkbox",
                    readonly : "${element.properties.readonly!}",
                    nonce : "${element.properties.nonce!}",
                    binderData : "${element.properties.binderData!}",
                    appId : "${element.properties.appId!}",
                    appVersion : "${element.properties.appVersion!}",
                    contextPath : "${request.contextPath}"
                });
            });
        </script>
    </#if>
</div>