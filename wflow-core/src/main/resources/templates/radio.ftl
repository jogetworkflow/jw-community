<div class="form-cell" ${elementMetaData!}>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <div class="form-cell-value" id="${elementParamName!}${element.properties.elementUniqueKey!}">
    <#list options as option>
        <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
            <#if value?? && value == option.value!>
                <label class="readonly_label">
                    <span>${option.label!}</span>
                </label>
            </#if>
        <#else>
            <label>
                <input grouping="${option.grouping!}" <#if element.properties.readonly! != 'true'>id="${elementParamName!}"</#if> name="${elementParamName!}" type="radio" value="${option.value!}" <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'> disabled</#if> <#if value?? && value == option.value!>checked</#if> />
                ${option.label!}
            </label>
        </#if>
    </#list>
        <#if element.properties.readonly! == 'true'><input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value!}" /></#if>
    </div>
    <div style="clear:both;"></div>

    <#if (element.properties.controlField?? && element.properties.controlField! != "" && !(element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true')) >
        <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.SelectBox/js/jquery.dynamicoptions.js"></script>
        <script type="text/javascript">
            $("#${elementParamName!}${element.properties.elementUniqueKey!} input").dynamicOptions({
                controlField : "${element.properties.controlField!}"
            });
        </script>
    </#if>
</div>