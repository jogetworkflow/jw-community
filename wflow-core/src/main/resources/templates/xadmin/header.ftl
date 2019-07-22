${header_before!}
<div class="container ${header_classes!}">
    ${header_inner_before!}
    ${header_info_before!}
    <div class="logo">
        <a href="${home_page_link!}">
            ${header_name_before!}
            <div id="header-name" class="${header_name_classes!}">
                ${header_name_inner_before!}
                <span>${userview.properties.name!}</span>
                ${header_name_inner_after!}
            </div>
            ${header_name_after!}
        </a>
    </div>
    <div class="left_open">
        <a><i title="@@xadmin.expandMenu@@" class="iconfont">&#xe699;</i></a>
    </div>
    ${header_info_after!}
    ${header_message_before!}
    ${header_menus!}
    ${header_message_after!}
    ${header_inner_after!}
</div>
${header_after!}