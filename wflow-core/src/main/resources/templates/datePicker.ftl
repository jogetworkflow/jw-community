<div class="form-cell" ${elementMetaData!}>
<#if element.properties.readonly! != 'true'>
    <#if !(request.getAttribute("org.joget.apps.form.lib.DatePicker_EDITABLE")??) >
        <#if request.getAttribute("currentLocale")!?starts_with("zh") >
            <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/js/jquery.ui.datepicker-zh-CN.js"></script>
        </#if>
    </#if>
<script type="text/javascript">
    $(document).ready(function() {
        $("#${elementParamName!}_${element.properties.elementUniqueKey!}").cdatepicker({
                        showOn: "button",
                        buttonImage: "${request.contextPath}/css/images/calendar.png",
                        buttonImageOnly: true,
                        changeMonth: true,
                        changeYear: true
                        <#if element.properties.format! != ''>
                        ,dateFormat: "${element.properties.format}"
                        </#if>
                        <#if element.properties.yearRange! != ''>
                        ,yearRange: "${element.properties.yearRange}"
                        </#if>
                        <#if element.properties.startDateFieldId! != ''>
                        ,startDateFieldId: "${element.properties.startDateFieldId}"
                        </#if>
                        <#if element.properties.endDateFieldId! != ''>
                        ,endDateFieldId: "${element.properties.endDateFieldId}"
                        </#if>
                        <#if element.properties.currentDateAs! != ''>
                        ,currentDateAs: "${element.properties.currentDateAs}"
                        </#if>
        });
    });
</script>
</#if>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
        <span>${value!?html}</span>
        <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value!?html}" />
    <#else>
        <#if (element.properties.allowManual! == 'true')>
            <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/js/jquery.placeholder.min.js"></script>
        </#if>
        <input id="${elementParamName!}_${element.properties.elementUniqueKey!}" name="${elementParamName!}" type="text" size="${element.properties.size!}" value="${value!?html}" class="${elementParamName!} <#if error??>form-error-cell</#if>" <#if (element.properties.allowManual! != 'true' || element.properties.readonly! == 'true')>readonly</#if> <#if (element.properties.allowManual! == 'true')>placeholder="${displayFormat!?html}"</#if> />
    </#if>
</div>
