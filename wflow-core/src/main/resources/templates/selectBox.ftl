<div class="form-cell" ${elementMetaData!}>
    <script type="text/javascript" src="${request.contextPath}/node_modules/select2/dist/js/select2.full.min.js"></script>
    <link rel="stylesheet" href="${request.contextPath}/node_modules/select2/dist/css/select2.min.css">

    <label class="label" for="${elementParamName!}${element.properties.elementUniqueKey!}" field-tooltip="${elementParamName!}">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <#if (element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true') >
        <div class="form-cell-value">
            <#list options as option>
                <#if values?? && values?seq_contains(option.value!)>
                    <label class="readonly_label">
                        <span>${option.label!?html}</span>
                    </label>
                </#if>
            </#list>
        </div>
        <div style="clear:both;"></div>
    <#else>
<<<<<<< HEAD
        <style>
            .select2-container {
                margin-bottom:18px
            }
        </style>
        <select class="js-select2" <#if element.properties.readonly! != 'true'>id="${elementParamName!}${element.properties.elementUniqueKey!}"</#if> name="${elementParamName!}" <#if element.properties.size?? && element.properties.size != ''> style="width:${element.properties.size!}%"</#if> <#if element.properties.multiple! == 'true'>multiple="multiple" data-role="none" data-native-menu="true"</#if> <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'> disabled </#if>>
            <#list options as option>
=======
        <select <#if element.properties.readonly! != 'true'>id="${elementParamName!}${element.properties.elementUniqueKey!}"</#if> name="${elementParamName!}" <#if element.properties.size?? && element.properties.size != ''> size="${element.properties.size!}"</#if> <#if element.properties.multiple! == 'true'>multiple="multiple" data-role="none" data-native-menu="true"</#if> <#if error??>class="form-error-cell"</#if> <#if element.properties.readonly! == 'true'> disabled </#if>>
            <#list options as option>:
>>>>>>> kecak-workflow-development-ui-normalize
                <option value="${option.value!?html}" grouping="${option.grouping!?html}" <#if values?? && values?seq_contains(option.value!)>selected</#if> <#if element.properties.readonly! == 'true'>disabled</#if>>${option.label!?html}</option>
            </#list>
        </select>
    </#if>
    <#if element.properties.readonly! == 'true'>    
        <#list values as value>
            <input type="hidden" id="${elementParamName!}" name="${elementParamName!}" value="${value?html}" />
        </#list>
    </#if>

    <#if (element.properties.controlField?? && element.properties.controlField! != "" && !(element.properties.readonly! == 'true' && element.properties.readonlyLabel! == 'true')) >
        <script type="text/javascript">
            $(document).ready(function(){
                $("#${elementParamName!}${element.properties.elementUniqueKey!}").dynamicOptions({
                    controlField : "${element.properties.controlFieldParamName!}",
                    paramName : "${elementParamName!}",
                    type : "selectbox",
                    readonly : "${element.properties.readonly!}",
                    nonce : "${element.properties.nonce!}",
                    binderData : "${element.properties.binderData!}",
                    appId : "${element.properties.appId!}",
                    appVersion : "${element.properties.appVersion!}",
                    contextPath : "${request.contextPath}"
                });
            });
        </script>
    </#if>

    <#-- Select2 Implementation -->
    <script type="text/javascript">
        $(document).ready(function(){
            $('select#${elementParamName!}${element.properties.elementUniqueKey!}.js-select2').select2({
                dropdownAutoWidth : true,
                width : '${width!'resolve'}',
                theme : 'classic',
                language : {
                   errorLoading: () => '${element.properties.messageErrorLoading!'@@form.selectbox.messageErrorLoading.value@@'}',
                   loadingMore: () => '${element.properties.messageLoadingMore!'@@form.selectbox.messageLoadingMore.value@@'}',
                   noResults: () => '${element.properties.messageNoResults!'@@form.selectbox.messageNoResults.value@@'}',
                   searching: () => '${element.properties.messageSearching!'@@form.selectbox.messageSearching.value@@'}'
                }

                <#if element.properties.lazyLoading! == 'true' >
                    ,ajax: {
                        url: '${request.contextPath}/web/json/app/${appId!}/${appVersion!}/plugin/${className}/service',
                        delay : 500,
                        dataType: 'json',
                        data : function(params) {
                            return {
                                search: params.term,
                                formDefId : '${formDefId!}',
                                fieldId : '${element.properties.id!}',
                                nonce : '${nonce!}',
                                <#if element.properties.controlField! != '' >
                                    grouping : FormUtil.getValue('${element.properties.controlField!}'),
                                </#if>
                                page : params.page || 1
                            };
                        }
                    }
                </#if>
            });
        });
    </script>
</div>
