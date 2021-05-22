AjaxUniversalTheme = {
    init : function(target) {
        AjaxComponent.overrideLinkEvent(target);
        AjaxComponent.initContent(target);
            
        window.onpopstate = function(event) {
            if (event.state) {
                var url = event.state.url;
                if (AjaxComponent.isCurrentUserviewUrl(url)) {
                    AjaxComponent.call($("#content"), url, "GET", null);
                }
            }
        };
    },
    
    callback : function(data) {
        if (data.indexOf("ajaxtheme_loading_container") !== -1) {
            var html = $(data);
            var menus = $(html).find("#ajaxtheme_loading_menus");
            var content = $(html).find("#ajaxtheme_loading_content");

            AjaxUniversalTheme.renderAjaxContent(menus, content);
        } else if (data.indexOf("<html>") !== -1 && data.indexOf("</html>") !== -1) {
            //something wrong and caused the full html loaded
            var html = $(data);
            var menus = $(html).find("#category-container").wrap("<div>");
            var content = $(html).find("#content main");

            AjaxUniversalTheme.renderAjaxContent(menus, content);
        }
    },
    
    errorCallback : function(error) {
        alert(error.message);
    },
    
    renderAjaxContent : function(menus, content) {
        AjaxUniversalTheme.updateMenus(menus);
        $("#content main").html($(content).html());

        AjaxComponent.overrideFormEvent($("#category-container"));
        AjaxComponent.initContent($("#content main"));

        if ($(".ma-backdrop").is(":visible")) {
            $("body").trigger("click.sidebar-toggled");
        }

        setTimeout(function(){
            $(window).trigger('resize'); //inorder for datalist to render in correct viewport
        }, 5);
    },
    
    updateMenus : function(menus) {
        $(menus).find("#category-container > li").each(function(){
            var cid = $(this).attr("id");
            if (cid !== undefined && cid !== null) {
                if ($("#"+cid).hasClass("toggled")) {
                    $(this).addClass("toggled");
                }
                $("#"+cid).replaceWith($(this));
            } else {
                $(this).find(".menu-container > li").each(function(){
                    var mid = $(this).attr("id");
                    $("#"+mid).replaceWith($(this));
                });
            }
            
        });
    }
};

$(function(){
    AjaxUniversalTheme.init($("body"));
});

