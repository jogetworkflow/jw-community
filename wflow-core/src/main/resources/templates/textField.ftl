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
    <label field-tooltip="${elementParamName!}" class="label${classIdentifier!}" for="${elementParamName!}">${label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if ((iconValue?? && iconValue != ""))>
        <div class="input-group px-0" style="<#if element.properties.size?has_content>width:${element.properties.size!};</#if>">
            <span class="input-group-text">${iconValue}</span>
    </#if>
    <#if ((element.properties.readonly! == 'true' || element.properties.readonly! == 'readonly') && element.properties.readonlyLabel! == 'true') >
        <div class="form-cell-value"><span>${valueLabel!?html}</span></div>
        <input id="${elementParamName!}" name="${elementParamName!}" class="textfield_${element.properties.elementUniqueKey!}" type="hidden" value="${value!?html}" />
    <#else>
        <#if ((validator?? && validator == "numeric") || element.properties.style! != "") && element.properties.readonly! != 'true' >
            <div class="numeric_field_wrapper" style="position: relative;<#if iconValue?? && iconValue == '' && element.properties.size?has_content> width:${element.properties.size!};</#if>">
                <input id="${elementParamName!}" name="${elementParamName!}" class="textfield_${element.properties.elementUniqueKey!}" type="text" inputmode="numeric" oninput="this.value = this.value.replace(/[^0-9.,-]/g, '');" placeholder="${element.properties.placeholder!?html}" value="${value!?html}" <#if element.properties.maxlength?has_content>maxlength="${element.properties.maxlength!}"</#if> <#if element.properties["fieldInput-style-text-align"]?has_content>style="padding-right: 30px;"</#if> <#if error??>class="form-error-cell"</#if> />
                    <div class="numeric_field_controls<#if (element.properties.disableIncrementDecrementArrow! == 'true')> disabled</#if>">
                        <a class="numeric_field_control numeric_field_control_incr">
                            <img src="${request.contextPath}/plugin/org.joget.apps.form.lib.TextField/images/chevron-up-solid.svg">
                        </a>
                        <a class="numeric_field_control numeric_field_control_decr">
                            <img src="${request.contextPath}/plugin/org.joget.apps.form.lib.TextField/images/chevron-down-solid.svg">
                        </a>
                    </div>
            </div>
            <style>
                .numeric_field_controls {
                    display: flex;
                    flex-direction: column;

                    position: absolute;
                    top: 50%;
                    transform: translateY(-50%);
                    right: 15.5px !important;
                    bottom: unset !important;
                }

                .numeric_field_wrapper > input {
                    max-width: 100% !important;
                }
                
                body.rtl .numeric_field_controls {
                    left: 15.5px !important;
                    right: unset !important;
                }

                .numeric_field_control {
                    line-height: 12px;
                    position: unset;
                } 

                .numeric_field_control img {
                    max-width: 10px; 
                    min-width: 10px; 
                    max-height: fit-content; 
                    min-height: fit-content;
                }

                .numeric_field_control.numeric_field_control_decr {
                    top: 15px;
                } 
            </style>
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

                        <#if element.properties["fieldInput-style-text-align"]?has_content>
                            if ($('body').hasClass('rtl')) {
                                $('.textfield_${element.properties.elementUniqueKey!}').css('padding-right', '').css('padding-left', '30px');
                            }
                        </#if>
                    });
                </script>
            </#if>   
        <#else>
            <input id="${elementParamName!}" name="${elementParamName!}" class="textfield_${element.properties.elementUniqueKey!}" type="text" placeholder="${element.properties.placeholder!?html}" <#if iconValue?? && iconValue == '' && element.properties.size?has_content> size="${element.properties.size!}"</#if> value="${value!?html}" <#if element.properties.maxlength?has_content>maxlength="${element.properties.maxlength!}"</#if> <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'>readonly</#if> <#if element.properties.readonly! == 'readonly'>readonly readonlyv2</#if>/>
        </#if>
    </#if>
    <#if ((iconValue?? && iconValue != ""))>
        </div>
    </#if>
</div>