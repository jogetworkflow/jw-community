<div id="ajaxtheme_loading_container">
    <div id="ajaxtheme_loading_title">
        ${title!}
    </div>
    <div id="ajaxtheme_loading_menus">
        ${menus!}
    </div>
    <div id="ajaxtheme_homebanner_content">
        ${main_container_before!}
    </div>
    <div id="ajaxtheme_loading_content">
        ${userview_menu_alert!}
        ${content_inner_before!}
        ${content!}
        ${content_inner_after!}
    </div>
    <#if analyzer??>
        <textarea id="ajaxAnalyzerJson" rows="1" cols="1" style="display:none;">${analyzer!?html}</textarea>
    </#if>
</div>