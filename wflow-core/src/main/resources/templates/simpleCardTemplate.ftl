<style>
    body.rtl #dataList_{{list.id}} .table-wrapper .ph_columns {
        display: flex;
    }
    body.rtl #dataList_{{list.id}} .table-wrapper .card-body,
    body.rtl #dataList_{{list.id}} .table-wrapper .ph_columns * {
        text-align: right;
    }
    body.rtl #dataList_{{list.id}} .table-wrapper .card-body .label {
        padding-left: 15px;
        padding-right: 0;
    }
</style>
<div class="cards row">
    {{rows data-cbuilder-highlight="@@datalist.simpleCardTemplate.card@@" data-cbuilder-style="[{'prefix' : 'card', 'class' : '.card', 'label' : '@@datalist.simpleCardTemplate.card@@'}]"}}
        <div class="{{columns_mobile}} {{columns_tablet}} {{columns_desktop}} p-2">
            {{selector}}
                <input type="{{type}}" name="{{name}}" id="{{id}}" value="{{value}}" style="display:none"/>
            {{selector}}
            <div class="card data-row" >
                {{selector}}
                    <label for="{{id}}" class="stretched-link"></label>
                {{selector}}
                <#if element.properties.image! == 'true'>
                    {{column_image data-cbuilder-droparea-msg="@@datalist.simpleCardTemplate.image@@" attr-class="card-img-top"||<svg class="bd-placeholder-img" width="100%" height="180" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMidYMid slice" focusable="false" role="img" aria-label="Placeholder: Image cap"><title>Placeholder</title><rect width="100%" height="100%" fill="#868e96"></rect><text x="50%" y="50%" fill="#dee2e6" dy=".3em">@@datalist.simpleCardTemplate.image@@</text></svg>}}
                </#if>
                <div class="card-body">
                    {{column_title data-cbuilder-droparea-msg="@@datalist.simpleCardTemplate.title@@"}}
                        <h5 class="card-title">{{body||@@datalist.simpleCardTemplate.title@@}}</h5>
                    {{column_title}}
                    {{columns<#if element.properties.inlineLabel! == 'true'> data-cbuilder-style="[{}, {'prefix' : 'header', 'class' : '.card-text > .label', 'label' : '@@datalist.simpleCardTemplate.label@@'}, {'prefix' : 'column-value', 'class' : '.card-text > .column-value', 'label' : '@@form.grid.value@@'}]"</#if>}}
                         {{column}}
                            <div class="card-text mb-2 <#if element.properties.inlineLabel! == 'true'>has-inline-label</#if>">
                                <#if element.properties.inlineLabel! == 'true'>
                                    <div class="label">{{label||@@datalist.simpleCardTemplate.label@@}}</div>
                                    <div class="column-value">{{body||@@datalist.simpleCardTemplate.textContent@@}}</div>
                                <#else>
                                    {{body||@@datalist.simpleCardTemplate.textContent@@}}
                                </#if>
                            </div>
                         {{column}}
                    {{columns}}
                    {{rowActions data-cbuilder-sort-horizontal}}
                        <div class="card-actions">
                            {{rowAction}}
                        </div>
                    {{rowActions}}
                    <#if element.properties.cardAction! == 'true'>
                    {{rowAction_card data-cbuilder-droparea-msg="@@datalist.simpleCardTemplate.cardAction@@" attr-class="stretched-link"}}
                    </#if>
                </div>
                <#if element.properties.footerMsg! == 'true'>
                {{column_footer data-cbuilder-droparea-msg="@@datalist.simpleCardTemplate.footer@@"}}
                    <div class="card-footer text-muted">
                        {{body||dd-MM-YYYY}}
                    </div>
                {{column_footer}}
                </#if>
            </div>
        </div>
    {{rows}}
</div>
