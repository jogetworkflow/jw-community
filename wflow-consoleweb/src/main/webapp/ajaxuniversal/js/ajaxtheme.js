AjaxUniversalTheme = {
    init : function(target) {
        AjaxComponent.overrideLinkEvent(target);
        AjaxComponent.initContent(target);
        
        AjaxUniversalTheme.initSidebar();
            
        window.onpopstate = function(event) {
            if (event.state) {
                var url = event.state.url;
                if (AjaxComponent.isCurrentUserviewUrl(url)) {
                    AjaxComponent.call($("#content"), url, "GET", null);
                }
            }
        };
    },
    
    scrollBar : function(selector, mousewheelaxis) {
        $(selector).mCustomScrollbar({
            theme: "minimal-dark",
            scrollInertia: 100,
            axis: "mousewheelaxis",
            mouseWheel: {
                enable: !0,
                axis: mousewheelaxis,
                preventDefault: !0
            }
        });
        
        
    },
    
    initSidebar : function() {
        if ($("#sidebar").length > 0) {
            var sidebar = function(){
                if ($("#sidebar").css("display") === "inline-block" || $("#sidebar").width() > 280) {
                    if ($("#sidebar #navigation").hasClass("mCustomScrollbar")) {
                        $("#sidebar #navigation").mCustomScrollbar("destroy");
                    }
                } else {
                    AjaxUniversalTheme.scrollBar("#sidebar #navigation", "y");
                }
            };
            sidebar();
            $(window).resize(function() {
                sidebar();
            });
        }
    },
    
    callback : function(data) {
        if (data.indexOf("ajaxtheme_loading_container") !== -1) {
            var html = $(data);
            var title = $(html).find("#ajaxtheme_loading_title");
            var menus = $(html).find("#ajaxtheme_loading_menus");
            var content = $(html).find("#ajaxtheme_loading_content");

            AjaxUniversalTheme.renderAjaxContent(menus, content, title);
        } else if (data.indexOf("<html") !== -1 && data.indexOf("</html>") !== -1) {
            //something wrong and caused the full html loaded
            var html = $(data);
            var title = $(html).find("title");
            var menus = $(html).find("#category-container").wrap("<div>");
            var content = $(html).find("#content main");
            
            AjaxUniversalTheme.renderAjaxContent(menus, content, title);
        }
    },
    
    errorCallback : function(error) {
        alert(error.message);
    },
    
    renderAjaxContent : function(menus, content, title) {
        //update body id according to url
        var currentPath = window.location.pathname;
        var menuId = currentPath.substring(currentPath.lastIndexOf("/") + 1);
        $("body").attr("id", menuId);
        
        $("title").html($(title).html());
        
        AjaxUniversalTheme.updateMenus(menus);
        $("#content main").html($(content).html());

        AjaxComponent.overrideFormEvent($("#category-container"));
        AjaxComponent.initContent($("#content main"));
        
        if ($("#content main").find(".c-overflow").length > 0) {
            AjaxUniversalTheme.scrollBar(".c-overflow", "y");
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
                $("#category-container #"+cid).replaceWith($(this));
            } else {
                $(this).find(".menu-container > li").each(function(){
                    var mid = $(this).attr("id");
                    $("#category-container #"+mid).replaceWith($(this));
                });
            }
            
        });
    }
};

$(function(){
    AjaxUniversalTheme.init($("body"));
});

