<div class="form-cell" ${elementMetaData!}>
<link rel="stylesheet" href="${request.contextPath}/css/jquery-ui-1.8.6.custom.css" />

<#if element.properties.readonly! != 'true'>
<script type="text/javascript">
    $(document).ready(function() {
        $("#${elementParamName!}_${element.properties.elementUniqueKey!}").datepicker({
                        showOn: "button",
                        buttonImage: "${request.contextPath}/css/images/calendar.png",
                        buttonImageOnly: true,
                        changeMonth: true,
                        changeYear: true
                        <#if element.properties.format! != ''>
                        ,dateFormat: "${element.properties.format}"
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
        <input id="${elementParamName!}_${element.properties.elementUniqueKey!}" name="${elementParamName!}" type="text" size="${element.properties.size!}" value="${value!?html}" class="${elementParamName!} <#if error??>form-error-cell</#if>" readonly />
    </#if>
</div>
