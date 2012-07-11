<div class="form-cell" ${elementMetaData!}>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
        <div class="form-cell-value">
            <#list options as option>
                <#if values?? && values?seq_contains(option.value!)>
                    <label class="readonly_label">
                        <span>${option.label!}</span>
                    </label>
                </#if>
            </#list>
        </div>
        <div style="clear:both;"></div>
    <#else>
        <select <#if element.properties.readonly! != 'true'>id="${elementParamName!}${element.properties.elementUniqueKey!}"</#if> name="${elementParamName!}" <#if element.properties.size?? && element.properties.size != ''> size="${element.properties.size!}"</#if> <#if element.properties.multiple! == 'true'>multiple</#if> <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'> disabled </#if>>
            <#list options as option>
                <option value="${option.value!}" grouping="${option.grouping!}" <#if values?? && values?seq_contains(option.value!)>selected</#if> <#if element.properties.readonly! == 'true'>disabled</#if>>${option.label!}</option>
            </#list>
        </select>
    </#if>
    <#if element.properties.readonly! == 'true'>    
        <#list values as value>
            <input type="hidden" id="${elementParamName!}" name="${elementParamName!}" value="${value?html}" />
        </#list>
    </#if>

    <#if (element.properties.controlField?? && element.properties.controlField! != "" && !(element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true')) >
        <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.SelectBox/js/jquery.dynamicoptions.js"></script>
        <script type="text/javascript">
            $("#${elementParamName!}${element.properties.elementUniqueKey!}").dynamicOptions({
                controlField : "${element.properties.controlField!}"
            });
        </script>
    </#if>
</div>
