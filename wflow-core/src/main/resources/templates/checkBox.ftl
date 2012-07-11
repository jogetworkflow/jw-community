<div class="form-cell" ${elementMetaData!}>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <div class="form-cell-value" id="${elementParamName!}${element.properties.elementUniqueKey!}">
    <#list options as option>
        <label>
        <input grouping="${option.grouping!}" id="${elementParamName!}" name="${elementParamName!}" type="checkbox" value="${option.value!}" <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'> onclick="this.checked=!this.checked;"</#if> <#if values?? && values?seq_contains(option.value!)>checked</#if> />
        ${option.label!}
        </label>
    </#list>
    </div>
    <div style="clear:both;"></div>

    <#if (element.properties.controlField?? && element.properties.controlField! != "") >
        <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.SelectBox/js/jquery.dynamicoptions.js"></script>
        <script type="text/javascript">
            $("#${elementParamName!}${element.properties.elementUniqueKey!} input").dynamicOptions({
                controlField : "${element.properties.controlField!}"
            });
        </script>
    </#if>
</div>