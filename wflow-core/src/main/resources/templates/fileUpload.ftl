<div class="form-cell" ${elementMetaData!}>
    <label class="label">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <div class="form-fileupload">
    <#if element.properties.readonly! != 'true'>
        <input id="${elementParamName!}" name="${elementParamName!}" type="file" size="${element.properties.size!}" <#if error??>class="form-error-cell"</#if> <#if element.properties.multiple! == 'true'>multiple</#if>/>
    </#if>
    <#if tempFilePaths?? || filePaths??>
        <style>
            ul.form-fileupload-value li{display:block;}
        </style>
        <ul class="form-fileupload-value">
            <#if tempFilePaths??>
                <#list tempFilePaths?keys as key>
                    <li>
                        ${tempFilePaths[key]!?html}
                        <input type="hidden" name="${elementParamName!}_path" value="${key!?html}"/>
                        <#if element.properties.readonly! != 'true'>
                            <input type="checkbox" name="${elementParamName!}_remove" value="${key!?html}" /> <span style="font-size:smaller">@@form.fileupload.remove@@</span>
                        </#if>
                    </li>
                </#list>
            </#if>
            <#if filePaths??>
                <#list filePaths?keys as key>
                    <li>
                        <a href="${request.contextPath}${key!?html}" target="_blank" >${filePaths[key]!?html}</a>
                        <input type="hidden" name="${elementParamName!}_path" value="${filePaths[key]!?html}"/>
                        <#if element.properties.readonly! != 'true'>
                            <input type="checkbox" name="${elementParamName!}_remove" value="${filePaths[key]!?html}" /> <span style="font-size:smaller">@@form.fileupload.remove@@</span>
                        </#if>
                    </li>
                </#list>
            </#if>
        </ul>
    </#if>
    </div>
</div>
