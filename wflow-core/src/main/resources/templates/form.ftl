<#if isAuthorize>
    <#if quickEditEnabled>
    <div class="quickEdit">
         <a href="${request.contextPath}/web/console/app/${appId}/${appVersion}/form/builder/${element.properties.id!}" target="_blank"><i class="icon-edit"></i> @@adminBar.label.form@@: ${element.properties.name!?html}</a>
    </div>
    </#if>
    
    <#if isRecordExist >
        <#if !element.parent??>
        <form id="${element.properties.id!}" name="${element.properties.id!}" class="form-container <#if element.properties.readonly! == 'true'>readonly</#if>" ${elementMetaData!} <#if element.properties.url??>action="${element.properties.url}"</#if> method="POST" enctype="multipart/form-data">
        </#if>
            <#assign keys = element.formMetas?keys>
            <#list keys as key>
                <#assign metaValues = element.formMetas[key]>
                <#list metaValues as v>
                    <input type="hidden" value="${v!?html}" name="${key}" />
                </#list>
            </#list>
            <#if error??><div class="form-error-message">${error}</div></#if>
            <#list element.children as e>
                ${e.render(formData, includeMetaData!false)}
            </#list>
        <#if !element.parent??>
        </form>
        <script>
            $(function(){
                $("#section-actions button, #section-actions input").click(function(){
                    $.blockUI({ css: { 
                        border: 'none', 
                        padding: '15px', 
                        backgroundColor: '#000', 
                        '-webkit-border-radius': '10px', 
                        '-moz-border-radius': '10px', 
                        opacity: .3, 
                        color: '#fff' 
                    }, message : "<h1>@@form.form.message.wait@@</h1>" }); 
                    return true;
                });
            });
        </script>
        </#if>
    <#else>
        <h3>@@form.form.message.recordNotFound@@</h3>
    </#if>    
<#else>
    <#if element.properties.noPermissionMessage?? && element.properties.noPermissionMessage! != "">
        <h3>${element.properties.noPermissionMessage!}</h3>
    <#else>
        <h3>@@form.form.message.noPermission@@</h3>
    </#if>
</#if>
