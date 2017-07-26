<style>
.close-icon:after {
    content: "x";
    width: 16px;
    height: 16px;
    position: absolute;
    text-align: center;
    line-height: 15px;
    margin-left: 10px;
    top: 10px;
    background-color: #FA9595;
    border-radius: 50%;
    color: white;
    font-size: 15px;
    box-shadow: 0 0 2px #E50F0F;
    cursor: pointer;
}
</style>

<link rel="stylesheet" href="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/css/jquery-ui-timepicker-addon.css" />

<div class="form-cell" ${elementMetaData!}>
<#if element.properties.readonly! != 'true'>
    <#if !(request.getAttribute("org.joget.apps.form.lib.DatePicker_EDITABLE")??) >
        <#if request.getAttribute("currentLocale")!?starts_with("zh") >
            <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/js/jquery.ui.datepicker-zh-CN.js"></script>
        </#if>
        <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/js/jquery.placeholder.min.js"></script>
    </#if>
<script type="text/javascript">
    $(document).ready(function() {
        $("#${elementParamName!}_${element.properties.elementUniqueKey!}").cdatepicker({
                        showOn: "button",
                        buttonImage: "${request.contextPath}/css/images/calendar.png",
                        buttonImageOnly: true,
                        changeMonth: true,
                        changeYear: true,
                        timeInput: true
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
                        <#if element.properties.startTime! != ''>
                        ,hourMin: ${element.properties.startTime}    
                        </#if>
                        <#if element.properties.endTime! != '' && element.properties.startTime?has_content && element.properties.endTime?number gte element.properties.startTime?number>
                        ,hourMax: ${element.properties.endTime}    
                        </#if>
                        <#if element.properties.datePickerType! != ''>
                        ,datePickerType: "${element.properties.datePickerType}"
                        </#if>
        });
    });
</script>
<script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.DatePicker/js/jquery-ui-timepicker-addon.js"></script>
</#if>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
        <span>${value!?html}</span>
        <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value!?html}" />
    <#else>
        <input id="${elementParamName!}_${element.properties.elementUniqueKey!}" name="${elementParamName!}" type="text" size="${element.properties.size!}" value="${value!?html}" class="${elementParamName!} <#if error??>form-error-cell</#if>" <#if (element.properties.allowManual! != 'true' || element.properties.readonly! == 'true')>readonly</#if> placeholder="<#if (element.properties.placeholder! != '')>${element.properties.placeholder}<#else>${displayFormat!?html}</#if>" />
    </#if>
</div>
