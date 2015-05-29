${nav_before!}
<nav id="${nav_id!}" class="${nav_classes!}">
    ${nav_inner_before!}
    ${categories_container_before!}
    <ul id="${categories_container_id!}" class="${categories_container_classes!}">
        ${categories_container_inner_before!}
        <#list userview.categories as category>
            <#if category.properties.hide! != 'yes' && category.menus?size gt 0>
                ${category_before!}
                <#assign cClass = category_classes!>
                <#if category_index == 0>
                    <#assign cClass = cClass + " " + first_category_classes!>
                </#if>
                <#if (category_index + 1) == userview.categories?size>
                    <#assign cClass = cClass + " " + last_category_classes!>
                </#if>
                <#if userview.currentCategory?? && category.properties.id == userview.currentCategory.properties.id>
                    <#assign cClass = cClass + " " + current_category_classes!>
                </#if>
                <#assign firstMenu = category.menus[0]>
                <#if combine_single_menu_category! && category.menus?size == 1>
                    <li id="${firstMenu.properties.id}" class="${cClass}">
                        ${category_inner_before!}
                        ${firstMenu.menu}
                        ${category_inner_after!}
                    </li>
                <#else>
                    <li class="${cClass}">
                        ${category_inner_before!}
                        <a class="${category_label_classes!} dropdown" href="#"><span>${theme.decorateCategoryLabel(category)}</span></a>
                        ${menu_container_before!}
                        <ul class="${menus_container_classes!}" >
                            ${menu_container_inner_before!}
                            <#list category.menus as menu>
                                <#assign mClass = menu_classes!>
                                <#if menu_index == 0>
                                    <#assign mClass = mClass + " " + first_menu_classes!>
                                </#if>
                                <#if (menu_index + 1) == category.menus?size>
                                    <#assign mClass = mClass + " " + last_menu_classes!>
                                </#if>
                                <#if userview.current?? && menu.properties.id == userview.current.properties.id>
                                    <#assign mClass = mClass + " " + current_menu_classes!>
                                </#if>
                                ${menu_before!}
                                <li id="${menu.properties.id!}" class="${mClass}">
                                    ${menu_inner_before!}
                                    ${menu.menu}
                                    ${menu_inner_after!}
                                </li>
                                ${menu_after!}
                            </#list>
                            ${menu_container_inner_after!}
                        </ul>
                        ${menu_container_after!}
                        ${category_inner_after!}
                    </li>
                </#if>
                ${category_after!}
            </#if>
        </#list>
        ${categories_container_inner_after!}
    </ul>
    ${categories_container_after!}
    ${nav_inner_after!}
</nav>
${nav_after!}