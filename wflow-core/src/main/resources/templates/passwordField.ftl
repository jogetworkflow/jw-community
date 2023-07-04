<div class="form-cell" ${elementMetaData!}>
    <#if !(request.getAttribute("org.joget.apps.form.lib.PasswordField")??)  >
        <script type="text/javascript" src="${request.contextPath}/js/zxcvbn/zxcvbn.js"></script>
        <script src="${request.contextPath}/plugin/org.joget.apps.form.lib.PasswordField/js/jquery.passwordfield.js"></script>
        <style>
            .password-container { display: flex; flex-direction: column; position: relative; }
            .password-wrapper { position: relative; }
            .password-meter { position: absolute; bottom: 0; width: 100%; }
            .password-meter-bar { width: 0%; height: 5px; }
            .password-strength-text { min-height: 24px; padding-top: 5px; }
        </style>
    </#if>
    <label field-tooltip="${elementParamName!}" class="label" for="${elementParamName!}">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
        <span>*************</span>
        <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value!?html}" />
    <#else>
        <#if (element.properties.strengthChecker! == 'true') >
            <div class="password-container">
                <div class="password-wrapper">
                    <input id="${elementParamName!}_${element.properties.elementUniqueKey!}" name="${elementParamName!}" type="password" <#if element.properties.size?has_content>size="${element.properties.size!}"</#if> value="${value!?html}" <#if element.properties.maxlength?has_content>maxlength="${element.properties.maxlength!}"</#if> <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'>readonly</#if> />
                    <div class="password-meter" id="password-meter_${elementParamName!}_${element.properties.elementUniqueKey!}">
                        <div class="password-meter-bar" id="password-meter-bar_${elementParamName!}_${element.properties.elementUniqueKey!}"></div>
                    </div>
                </div>
                <div class="password-strength-text" id="strength-text_${elementParamName!}_${element.properties.elementUniqueKey!}"></div>
            </div>
            <script>
                $(document).ready(function(){
                    var messages = {
                        "form.passwordfield.strengthChecker.veryWeak": "@@form.passwordfield.strengthChecker.veryWeak@@",
                        "form.passwordfield.strengthChecker.weak": "@@form.passwordfield.strengthChecker.weak@@",
                        "form.passwordfield.strengthChecker.medium": "@@form.passwordfield.strengthChecker.medium@@",
                        "form.passwordfield.strengthChecker.strong": "@@form.passwordfield.strengthChecker.strong@@",
                        "form.passwordfield.strengthChecker.veryStrong": "@@form.passwordfield.strengthChecker.veryStrong@@"
                    }
                    
                    $('${elementParamName!}_${element.properties.elementUniqueKey!}').passwordField({
                        paramName : "${elementParamName!}",
                        elementUniqueKey : "${element.properties.elementUniqueKey!}",
                        messages : messages
                    });
                });
            </script>
        <#else>
            <input id="${elementParamName!}" name="${elementParamName!}" type="password" <#if element.properties.size?has_content>size="${element.properties.size!}"</#if> value="${value!?html}" <#if element.properties.maxlength?has_content>maxlength="${element.properties.maxlength!}"</#if> <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'>readonly</#if> />
        </#if>
    </#if>
</div>