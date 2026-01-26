<div class="form-cell" ${elementMetaData!}>
<#if (element.properties.readonly! != 'true' && element.properties.readonly! != 'readonly')>
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
        $("#${elementParamName!}_${element.properties.elementUniqueKey!}").wrap("<div class='datepicker-wrapper'></div>");

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

        $("#${elementParamName!}_${element.properties.elementUniqueKey!}").off('focus').on('focus', function(){
            $(this).siblings('.trigger').trigger('click');
        });

        $(window).on("resize orientationchange", function(){
            $("#${elementParamName!}_${element.properties.elementUniqueKey!}").datepicker("hide");
        });
    });
</script>
<style>
    .adgrid .datepicker-wrapper {
        min-width: 100%;    
    }

    .adgrid .datepicker-wrapper *:is(.hasDatepicker+a, .hasDatepicker+a+a) {
        display: block;
    }

    .datepicker-wrapper {
        position: relative;
        display: inline-block;
        width: 100%;
        min-width: 70%;
        max-width: 70%;
    }

    .input-group > .datepicker-wrapper {
        flex: 1 1 100%;
        width: 100%;
        max-width: unset;
    }

    .datepicker-wrapper > input.hasDatepicker {
        min-width: 100%;
        margin-bottom: 0px;
    }

    .input-group:has(input.hasDatepicker.popup-picker) .input-group-text  {
        border-color: #2B83FF;
        border-inline-end: none;
    }

    .input-group:has(input.hasDatepicker.popup-picker) input.hasDatepicker.popup-picker   {
        border-color: #2B83FF;
        border-inline-start: none;
    }

    input.hasDatepicker.popup-picker {
        border: 1px solid #2B83FF;
    }

    .datepicker-wrapper .hasDatepicker+a,
    .datepicker-wrapper .hasDatepicker+a+a {
        top: 50%;
        transform: translateY(-50%);
        bottom: unset;
    }

    .label-top .datepicker-wrapper .hasDatepicker+a,
    .label-top .datepicker-wrapper .hasDatepicker+a+a {
        bottom: unset;
    }

    .datepicker-wrapper .hasDatepicker+a {
        display: none;
    }

    img.ui-datepicker-trigger {
        display: none;
    }

    .datepicker-wrapper .hasDatepicker+a+a {
        right: 10px;
        color: rgba(119, 119, 119, 1);
        width: fit-content;
        height: fit-content;
        font-size: 12px;
    }

    .datepicker-wrapper .hasDatepicker+a+a:hover {
        color: #e46772;
    }

    body.rtl .datepicker-wrapper .hasDatepicker+a+a {
        left: 10px;
        right: unset;
    }

    .datepicker-wrapper .hasDatepicker+a+a:before {
        content: "\f00d";
        font-family: "Font Awesome 5 Free";
        font-weight: 900;
    }
</style>
</#if>
    <label field-tooltip="${elementParamName!}" class="label${classIdentifier!}" for="${elementParamName!}_${element.properties.elementUniqueKey!}">${label} <span class="form-cell-validator">${decoration}</span><#if element.properties.showUserTimeZone! == 'true' && userTimeZone?? && !(element.properties.readonly! == 'true' && element.properties.readonly! == 'readonly' && element.properties.readonlyLabel! == 'true') ><br/><span>(${userTimeZone!?html})</span></#if><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if ((iconValue?? && iconValue != ""))>
        <div class="input-group px-0" style="<#if element.properties.size?has_content>width:${element.properties.size!};</#if>">
            <span class="input-group-text">${iconValue}</span>
    </#if>
    <#if ((element.properties.readonly! == 'true' || element.properties.readonly! == 'readonly') && element.properties.readonlyLabel! == 'true') >
        <div class="form-cell-value"><span>${value!?html} <#if element.properties.showUserTimeZone! == 'true' && userTimeZone??>(${userTimeZone!?html})</#if></span></div>
        <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value!?html}" />
    <#else>
        <input id="${elementParamName!}_${element.properties.elementUniqueKey!}" style="<#if (element.properties.readonly! == 'readonly')>cursor:not-allowed;background:var(--jgt-input-colorBgContainerDisabled, #F5F5F5)<#else>background: initial;</#if>" class="datepicker-input" name="${elementParamName!}" type="text" <#if iconValue?? && iconValue == '' && element.properties.size?has_content> size="${element.properties.size!}"</#if> value="${value!?html}" class="${elementParamName!}<#if (element.properties.allowManual! != 'true' && element.properties.readonly! != 'true')> no-manual-input</#if> <#if error??>form-error-cell</#if>" <#if (element.properties.allowManual! != 'true' || element.properties.readonly! == 'true')>readonly</#if> <#if element.properties.readonly! == 'readonly'>readonly readonlyv2</#if> placeholder="<#if (element.properties.placeholder! != '')>${element.properties.placeholder!?html}<#else>${displayFormat!?html}</#if>" />
    </#if>
    <#if ((iconValue?? && iconValue != ""))>
        </div>
    </#if>
</div>
