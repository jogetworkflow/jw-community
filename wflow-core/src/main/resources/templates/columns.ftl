<#if element.properties.gutter?? && element.properties.gutter != "">
<style>
    #${element.properties.id!} > .row {
        margin-left: -${element.properties.gutter!};
        margin-right: -${element.properties.gutter!};
    }
    #${element.properties.id!} > .row > .col {
        padding-left: ${element.properties.gutter!};
        padding-right: ${element.properties.gutter!};
    }
</style>
</#if>
<style>
    #${element.properties.id!} .col > .form-cell {
        width: 100% !important;
        max-width: 100% !important;
    }
    
    .container-fluid.form-element-columns {
        margin-top: 12px;
        padding: 0 !important;
    }

    .container-fluid[data-cbuilder-classname="org.joget.apps.form.lib.Columns"] {
        margin-top: 12px;
        margin-bottom: 5px;
        padding: 0 !important;
    }
</style>

<div id="${element.properties.id!}" class="container-fluid form-element-columns" ${elementMetaData!}>
    <div class="row" <#if includeMetaData!false>data-cbuilder-columns</#if>>
        <#list element.children as e>
            ${e.render(formData, includeMetaData!false)}
        </#list>
    </div>
</div>
