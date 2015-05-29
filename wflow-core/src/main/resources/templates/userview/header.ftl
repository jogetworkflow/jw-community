${header_before!}
<header class="${header_classes!}">
    ${header_inner_before!}
    ${header_info_before!}
    <div id="header-info" class="${header_info_classes!}">
        ${header_info_inner_before!}
        ${header_name_before!}
        <div id="header-name" class="${header_name_classes!}">
            ${header_name_inner_before!}
            <a href="${home_page_link!}" id="header-link" class="${header_link_classes!}">
                <span>${userview.properties.name!}</span>
            </a>
            ${header_name_inner_after!}
        </div>
        ${header_name_after!}
        ${header_description_before!}
        <div id="header-description" class="${header_description_classes!}">
            ${header_description_inner_before!}
            <span id="description" class="${header_description_span_classes!}">${userview.properties.description!}</span>
            ${header_description_inner_after!}
        </div>
        ${header_description_after!}
        <div class="clearfix"></div>
        ${header_info_inner_after!}
    </div>
    ${header_info_after!}
    ${header_message_before!}
    <div id="header-message" class="${header_message_classes!}">
        ${header_message_inner_before!}
        <div id="header-welcome-message" class="${header_welcome_classes!}">
            <span id="welcomeMessage">${userview.properties.welcomeMessage!}</span>
        </div>
        <div id="header-logout-text" class="${header_logout_classes!}">
            <#if is_logged_in>
                <a href="${logout_link!}">
                    <span id="logoutText">${userview.properties.logoutText!}</span>
                </a>
            <#else>
                <a href="${login_link!}">
                    <span id="loginText">@@ubuilder.login@@</span>
                </a>
            </#if>
        </div>
        <div class="clearfix"></div>
        ${header_message_inner_after!}
    </div>
    ${header_message_after!}
    ${header_inner_after!}
</header>
${header_after!}