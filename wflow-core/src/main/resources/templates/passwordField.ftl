<div class="form-cell" ${elementMetaData!}>
    <#if !(request.getAttribute("org.joget.apps.form.lib.PasswordField")??)>
        <script type="text/javascript" src="${request.contextPath}/js/zxcvbn/zxcvbn.js"></script>
        <script src="${request.contextPath}/plugin/org.joget.apps.form.lib.PasswordField/js/jquery.passwordfield.js"></script>
        <style>
            .password-container { display: flex; flex-direction: column; position: relative; max-width: 70%;}
            .label-top.form-cell .password-container, .label-top.subform-cell .password-container { max-width: 100%;}
            .password-container div.input-group { max-width: unset; width: 100%;}
            .password-wrapper { position: relative; }
            .password-meter { position: absolute; bottom: 0; width: 100%; }
            .password-meter-bar { width: 0%; height: 5px; }
            .password-strength-text { min-height: 24px; font-size: 12px }
            .password-input-wrapper {
                display: flex;
                position: relative;
            }
            .password-input-wrapper input[type=text],
            .password-input-wrapper input[type=password] {
                padding-inline-end: 2.2em !important;
                max-width: 100%;
                min-width: 100%;
            }
            .password-wrapper:has(.password-meter) .password-input-wrapper input {
                 margin-bottom: 0;
            }
            .password-input-wrapper button.password-toggle {
                cursor: pointer;
                border: none;
                background: transparent;
                margin: 0;
                padding: 0;
                position: absolute;
                right: 9px;
                top: 50%;
                transform: translateY(-50%);
            }
            .rtl .password-input-wrapper button.password-toggle {
                left: 9px;
                right: unset;
            }
            .password-input-wrapper button.password-toggle,
            .password-input-wrapper button.password-toggle:hover,
            .password-input-wrapper button.password-toggle:focus,
            .password-input-wrapper button.password-toggle:active {
                color: unset;
                background: none;
                box-shadow: none;
                outline: none;
            }
            .password-input-wrapper button.password-toggle:focus-visible {
                outline: 5px auto -webkit-focus-ring-color !important;
            }
        </style>
    </#if>
    <label field-tooltip="${elementParamName!}" class="label${classIdentifier!}" for="${elementParamName!}_${element.properties.elementUniqueKey!}">${label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if ((element.properties.readonly! == 'true' || element.properties.readonly! == 'readonly') && element.properties.readonlyLabel! == 'true')>
        <span>*************</span>
        <#if ((iconValue?? && iconValue != ""))>
            <div class="input-group px-0"  style="<#if element.properties.size?has_content>width:${element.properties.size!};</#if>">
                <span class="input-group-text">${iconValue}</span>
        </#if>
        <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value!?html}" />
        <#if ((iconValue?? && iconValue != ""))>
            </div>
        </#if>
    <#else>
        <div class="password-container px-0 ${elementParamName!}_${element.properties.elementUniqueKey!}" <#if element.properties.size?has_content>style="width:${element.properties.size!};"</#if>>
            <div class="password-wrapper">
                <#if ((iconValue?? && iconValue != ""))>
                    <div class="input-group px-0">
                        <span class="input-group-text">${iconValue}</span>
                </#if>
                <div class="password-input-wrapper">
                    <input id="${elementParamName!}_${element.properties.elementUniqueKey!}" name="${elementParamName!}" type="password" value="${value!?html}" <#if element.properties.maxlength?has_content>maxlength="${element.properties.maxlength!}"</#if> <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'>readonly</#if> <#if element.properties.readonly! == 'readonly'>readonly persistablereadonly</#if>/>
                    <#if (element.properties.enableVisibility! == 'true')>
                        <button type="button" class="password-toggle" title="Toggle password visibility">
                            <i class="fas fa-fw fa-eye"></i>
                        </button>
                        <script>
                            $(document).ready(function(){
                                $('div.password-container.${elementParamName!}_${element.properties.elementUniqueKey!}').passwordField.togglePasswordVisibility({
                                    paramName: '${elementParamName!}',
                                    elementUniqueKey: '${element.properties.elementUniqueKey!}',
                                    messages: {
                                        "form.passwordfield.showPassword": "@@form.passwordfield.showPassword@@",
                                        "form.passwordfield.hidePassword": "@@form.passwordfield.hidePassword@@"
                                    }
                                });
                            });
                        </script>
                    </#if>
                </div>
                <#if ((iconValue?? && iconValue != ""))>
                    </div>
                </#if>
                <#if (element.properties.strengthChecker! == 'true')>
                    <div class="password-meter" id="password-meter_${elementParamName!}_${element.properties.elementUniqueKey!}">
                        <div class="password-meter-bar" id="password-meter-bar_${elementParamName!}_${element.properties.elementUniqueKey!}"></div>
                    </div>
                </#if>
            </div>
            <#if (element.properties.strengthChecker! == 'true')>
                <div class="password-strength-text" id="strength-text_${elementParamName!}_${element.properties.elementUniqueKey!}"></div>
                <script>
                    $(document).ready(function(){
                        var messages = {
                            "form.passwordfield.strengthChecker.veryWeak": "@@form.passwordfield.strengthChecker.veryWeak@@",
                            "form.passwordfield.strengthChecker.weak": "@@form.passwordfield.strengthChecker.weak@@",
                            "form.passwordfield.strengthChecker.medium": "@@form.passwordfield.strengthChecker.medium@@",
                            "form.passwordfield.strengthChecker.strong": "@@form.passwordfield.strengthChecker.strong@@",
                            "form.passwordfield.strengthChecker.veryStrong": "@@form.passwordfield.strengthChecker.veryStrong@@"
                        }

                        $('${elementParamName!}_${element.properties.elementUniqueKey!}').passwordField.strengthChecker({
                            paramName: "${elementParamName!}",
                            elementUniqueKey: "${element.properties.elementUniqueKey!}",
                            messages: messages
                        });
                    });
                </script>
            </#if>
        </div>
    </#if>
</div>