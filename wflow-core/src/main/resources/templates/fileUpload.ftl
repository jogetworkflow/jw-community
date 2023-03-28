<div class="form-cell" ${elementMetaData!}>
    <#if !(request.getAttribute("org.joget.apps.form.lib.FileUpload")?? || request.getAttribute("org.joget.plugin.enterprise.ImageUpload")??)  >
        <link rel="stylesheet" href="${request.contextPath}/js/dropzone/dropzone.css" />
        <script type="text/javascript" src="${request.contextPath}/js/dropzone/dropzone.js"></script>
        <script src="${request.contextPath}/plugin/org.joget.apps.form.lib.FileUpload/js/jquery.fileupload.js"></script>
        <script type="text/javascript">// Immediately after the js include
            Dropzone.autoDiscover = false;
        </script>
    </#if>

    <label class="label" field-tooltip="${elementParamName!}">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <div id="form-fileupload_${elementParamName!}_${element.properties.elementUniqueKey!}" tabindex="0" class="form-fileupload <#if error??>form-error-cell</#if> <#if element.properties.readonly! == 'true'>readonly<#else>dropzone</#if>">
    <#if element.properties.readonly! != 'true'>
        <div class="dz-message needsclick">
            @@form.fileupload.dropFile@@
        </div>
        <input style="display:none" id="${elementParamName!}" name="${elementParamName!}" type="file" size="${element.properties.size!}" <#if error??>class="form-error-cell"</#if> <#if element.properties.multiple! == 'true'>multiple</#if>/>
    </#if>
        <ul class="form-fileupload-value">
            <#if element.properties.readonly! != 'true'>
                <li class="template" style="display:none;">
                    <span class="name" data-dz-name></span> <a class="remove"style="display:none">@@form.fileupload.remove@@</a> 
                    <strong class="error text-danger" data-dz-errormessage></strong>
                    <div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0">
                        <div class="progress-bar progress-bar-success" style="width:0%;" data-dz-uploadprogress></div>
                    </div>
                    <input type="hidden" name="${elementParamName!}_path" value="" disabled/>
                </li>
            </#if>
            <#if tempFilePaths??>
                <#list tempFilePaths?keys as key>
                    <li>
                        <span class="name">${tempFilePaths[key]!?html}</span>
                            <#if element.properties.readonly! != 'true'>
                                <a class="remove">@@form.fileupload.remove@@</a>
                            </#if>
                        <input type="hidden" name="${elementParamName!}_path" value="${key!?html}"/>
                    </li>
                </#list>
            </#if>
            <#if filePaths??>
                <#list filePaths?keys as key>
                    <li>
                        <a href="${request.contextPath}${key!?html}" target="_blank" ><span class="name">${filePaths[key]!?html}</span></a>
                        <#if element.properties.readonly! != 'true'>
                            <a class="remove">@@form.fileupload.remove@@</a>
                        </#if>
                        <input type="hidden" name="${elementParamName!}_path" value="${filePaths[key]!?html}"/>
                    </li>
                </#list>
            </#if>
        </ul>
    </div>
    <#if element.properties.readonly! != 'true'>
        <script>
            $(document).ready(function(){
                $('#form-fileupload_${elementParamName!}_${element.properties.elementUniqueKey!}').fileUploadField({
                    url : "${element.serviceUrl!}",
                    paramName : "${elementParamName!}",
                    multiple : "${element.properties.multiple!}",
                    maxSize : "${element.properties.maxSize!}",
                    maxSizeMsg : "${element.properties.maxSizeMsg!}",
                    fileType : "${element.properties.fileType!}",
                    fileTypeMsg : "${element.properties.fileTypeMsg!}",
                    padding : "${element.properties.padding!}",
                    removeFile : "${element.properties.removeFile!}",
                    resizeWidth : "${element.properties.resizeWidth!}",
                    resizeHeight : "${element.properties.resizeHeight!}",
                    resizeQuality : "${element.properties.resizeQuality!}",
                    resizeMethod : "${element.properties.resizeMethod!}"
                });
            });
        </script>
    </#if>
</div>
