<ul id="${categories_container_id!}">
    <#list userview.categories as category>
        <#if category.properties.hide! != 'yes' && category.menus?size gt 0>
            <#list category.menus as menu>
                <#if menu.properties.rowCount! == 'true'>
                    <li id="${menu.properties.id!}">
                        ${menu.getMenu()}
                    </li>
                </#if>
            </#list>
        </#if>
    </#list>
</ul>
