<div class="form-cell" ${elementMetaData!}>
<#if element.properties.readonly! != 'true'>
    <#if !(request.getAttribute("org.joget.apps.form.lib.DatePicker_EDITABLE")??)>
        <#if locale! != ''>
            <script type="text/javascript" src="${request.contextPath}/js/jquery/ui/i18n/jquery.ui.datepicker-${locale}.js"></script>
        </#if>
        <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/js/jquery.placeholder.min.js"></script>
        <link rel="stylesheet" href="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/css/jquery-ui-timepicker-addon.css" />
        <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/js/jquery-ui-timepicker-addon.js"></script>
        <#if isBE!>
            <script type="text/javascript" src="${request.contextPath}/js/jquery/ui/i18n/jquery.ui.datepicker.ext.be.js"></script>
        </#if>
    </#if>
<script type="text/javascript">
    $(document).ready(function() {
        $("#${elementParamName!}_${element.properties.elementUniqueKey!}").cdatepicker({
                        showOn: "button",
                        buttonImage: "${request.contextPath}/css/images/calendar.png",
                        buttonImageOnly: true,
                        changeMonth: true,
                        changeYear: true,
                        timeInput: true,
                        buttonText : ''
                        <#if element.properties.format24hr! == ''>
                        ,timeFormat: "hh:mm tt"
                        </#if>
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
                        <#if element.properties.datePickerType! != ''>
                        ,datePickerType: "${element.properties.datePickerType}"
                        </#if>
                        <#if element.properties.firstday! != ''>
                        ,firstDay: "${element.properties.firstday}"
                        </#if>
                        <#if isBE! && element.properties.datePickerType! != 'timeOnly'>
                        ,isBE: true
                        ,autoConversionField: false
                        </#if>
                        <#if isRTL!>
                            ,isRTL: true
                        </#if>
                        <#if (element.properties.disableWeekends! == 'true')>
                        ,beforeShowDay: $.datepicker.noWeekends
                        </#if>
                        ,timeOnlyTitle : "@@form.datepicker.chooseTime@@"
                        ,timeText : "@@form.datepicker.time@@"
                        ,hourText : "@@form.datepicker.hour@@"
                        ,minuteText : "@@form.datepicker.minute@@"
                        ,currentText : "@@form.datepicker.now@@"
                        ,closeText : "@@form.datepicker.done@@"
        });

        $(window).on("resize orientationchange", function(){
            $("#${elementParamName!}_${element.properties.elementUniqueKey!}").datepicker("hide");
        });
    });
</script>
</#if>
    <label field-tooltip="${elementParamName!}" class="label" for="${elementParamName!}_${element.properties.elementUniqueKey!}">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if element.properties.showUserTimeZone! == 'true' && !(element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') ><br/><span>(${userTimeZone!?html})</span></#if><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
        <span>${value!?html} <#if element.properties.showUserTimeZone! == 'true' >(${userTimeZone!?html})</#if></span>
        <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value!?html}" />
    <#else>
        <input id="${elementParamName!}_${element.properties.elementUniqueKey!}" name="${elementParamName!}" type="text" <#if element.properties.size?has_content>size="${element.properties.size!}"</#if> value="${value!?html}" class="${elementParamName!}<#if (element.properties.allowManual! != 'true' && element.properties.readonly! != 'true')> no-manual-input</#if> <#if error??>form-error-cell</#if>" <#if (element.properties.allowManual! != 'true' || element.properties.readonly! == 'true')>readonly</#if> placeholder="<#if (element.properties.placeholder! != '')>${element.properties.placeholder!?html}<#else>${displayFormat!?html}</#if>" />
    </#if>
</div>
