<div class="form-cell" ${elementMetaData!}>

<#if !(request.getAttribute("org.joget.apps.form.lib.Grid")??) >
    <script type="text/javascript" src="${request.contextPath}/js/jquery/jquery.jeditable.js"></script>
    <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.apps.form.lib.Grid/js/jquery.formgrid.js"></script>
    <style type="text/css">
        .grid table {
            width: 100%;
        }
        .grid th, .grid td {
            border: solid 1px silver;
            margin: 0px;
        }
        .grid-cell-options {
            width: 10px;
        }
        .grid-row-template {
            display: none;
        }
        .grid-cell input:focus {
            background: #efefef;
            border: 1px solid #a1a1a1;
        }
        .grid-cell {
            display: block;
        }
        .grid-cell form {
            display: block;
        }
    </style>
</#if>
<#if element.properties.readonly! != 'true'>
    <script type="text/javascript">
        $(document).ready(function() {
            $(".grid_${element.properties.elementUniqueKey!}").formgrid();
        });
    </script>
</#if>

    <label class="label">${element.properties.label!} <span class="form-cell-validator">${decoration}${customDecorator}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
    <div class="form-clear"></div>
    <div class="grid grid_${element.properties.elementUniqueKey!} form-element" id="${elementParamName!}" name="${elementParamName!}">
        <table cellspacing="0" class="tablesaw tablesaw-stack" data-tablesaw-mode="stack">
            <thead>
                <tr>
                <#list headers?keys as header>
                    <th id="${elementParamName!}_${header}">${headers[header]?html}</th>
                </#list>
                </tr>
            </thead>
            <tbody>
                <tr class="grid-row-template" style="display:none;">
                <#list headers?keys as header>
                    <td><span id="${elementParamName!}_${header?html}" name="${elementParamName!}_${header?html}" class="grid-cell"></span> <input class="grid-input" type="hidden" value="" /></td>
                </#list>
                </tr>
                <#list rows as row>
                    <tr class="grid-row">
                    <#list headers?keys as header>
                        <td><span id="${elementParamName!}_${header?html}" name="${elementParamName!}_${header?html}" class="grid-cell">${row[header]?html}</span> <input name="${elementParamName!}_${header?html}_${row_index}" class="grid-input" type="hidden" value="${row[header]!?html}" /></td>
                    </#list>
                    </tr>
                </#list>
            </tbody>    
        </table>
    </div>
</div>
