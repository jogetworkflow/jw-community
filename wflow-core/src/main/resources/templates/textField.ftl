<div class="form-cell" ${elementMetaData!}>
    <#if !(includeMetaData!) && element.properties.style! != "" >
        <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.TextField/js/jquery.numberFormatting.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                $('.textfield_${element.properties.elementUniqueKey!}').numberFormatting({
                    format : '${element.properties.style!}',
                    numOfDecimal : '${element.properties.numOfDecimal!}',
                    useThousandSeparator : '${element.properties.useThousandSeparator!}',
                    prefix : '${element.properties.prefix!}',
                    postfix : '${element.properties.postfix!}'
                });
            });
        </script>
    </#if>
    <label field-tooltip="${elementParamName!}" class="label" for="${elementParamName!}">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
        <div class="form-cell-value"><span>${valueLabel!?html}</span></div>
        <input id="${elementParamName!}" name="${elementParamName!}" class="textfield_${element.properties.elementUniqueKey!}" type="hidden" value="${value!?html}" />
    <#else>
        <#if ((validator?? && validator == "numeric") || element.properties.style! != "") && element.properties.readonly! != 'true' >
            <input id="${elementParamName!}" name="${elementParamName!}" class="textfield_${element.properties.elementUniqueKey!}" type="text" inputmode="numeric" oninput="this.value = this.value.replace(/[^0-9.,-]/g, '');" placeholder="${element.properties.placeholder!?html}" <#if element.properties.size?has_content>size="${element.properties.size!}"</#if> value="${value!?html}" <#if element.properties.maxlength?has_content>maxlength="${element.properties.maxlength!}"</#if> <#if error??>class="form-error-cell"</#if> />
            <div class="numeric_field_controls">
                <a class="numeric_field_control numeric_field_control_incr">
                    <img src="${request.contextPath}/plugin/org.joget.apps.form.lib.TextField/images/chevron-up-solid.svg">
                </a>
                <a class="numeric_field_control numeric_field_control_decr">
                    <img src="${request.contextPath}/plugin/org.joget.apps.form.lib.TextField/images/chevron-down-solid.svg">
                </a>
            </div>
            <#if !(includeMetaData!)>
                <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.TextField/js/jquery.numberField.js"></script>
                <script type="text/javascript">
                    $(document).ready(function(){
                        $('.textfield_${element.properties.elementUniqueKey!}').numberField({
                            format : '${element.properties.style!}',
                            numOfDecimal : '${element.properties.numOfDecimal!}',
                            useThousandSeparator : '${element.properties.useThousandSeparator!}',
                            prefix : '${element.properties.prefix!}',
                            postfix : '${element.properties.postfix!}',
                            incrementValue : '${element.properties.incrementValue!}',
                            decrementValue : '${element.properties.decrementValue!}'
                        });
                    });
                </script>
            </#if>   
        <#else>
            <input id="${elementParamName!}" name="${elementParamName!}" class="textfield_${element.properties.elementUniqueKey!}" type="text" placeholder="${element.properties.placeholder!?html}" <#if element.properties.size?has_content>size="${element.properties.size!}"</#if> value="${value!?html}" <#if element.properties.maxlength?has_content>maxlength="${element.properties.maxlength!}"</#if> <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'>readonly</#if> />
        </#if>
    </#if>
</div>