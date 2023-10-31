AjaxUniversalTheme = {
    init : function(target) {
        AjaxComponent.overrideLinkEvent(target);
        AjaxComponent.initContent(target);
        
        AjaxUniversalTheme.initSidebar();
            
        window.onpopstate = function(event) {
            if (event.state) {
                var url = event.state.url;
                if (AjaxComponent.isCurrentUserviewUrl(url)) {
                    AjaxComponent.call($("#content.page_content"), url, "GET", null);
                }
            }
        };
        
        $("body").append('<div id="ajaxtheme_dynamic_elements_after_this" style="display:none;"></div>');
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
        AjaxComponent.unbindEvents(); //remove all binded evernts
        AjaxUniversalTheme.clearDynamicElement();
        AjaxUniversalTheme.updateLoginUrl();
        
        if (data.indexOf("ajaxtheme_loading_container") !== -1) {
            var html = $(data);
            var title = $(html).find("#ajaxtheme_loading_title");
            var menus = $(html).find("#ajaxtheme_loading_menus");
            var content = $(html).find("#ajaxtheme_loading_content");
            var homeBanner = $(html).find("#ajaxtheme_homebanner_content");

            AjaxUniversalTheme.renderAjaxContent(menus, content, title, homeBanner);
            
            if (window['Analyzer'] !== undefined && $(html).find("#ajaxAnalyzerJson").length > 0) {
                Analyzer.clearAnalyzer();
                var analyzerJson = $(html).find("#ajaxAnalyzerJson").val();
                var analyzer = JSON.parse(analyzerJson);
                Analyzer.initAnalyzer(analyzer);
            }
        } else if (data.indexOf("<html") !== -1 && data.indexOf("</html>") !== -1) {
            //something wrong and caused the full html loaded
            var html = $(data);
            var title = $(html).find("title");
            var menus = $(html).find("#category-container").wrap("<div>");
            var content = $(html).find("#content main");
            
            AjaxUniversalTheme.renderAjaxContent(menus, content, title);
            
            if (window['Analyzer'] !== undefined && $(html).find("#analyzerJson").length > 0) {
                Analyzer.clearAnalyzer();
                var analyzerJson = $(html).find("#analyzerJson").val();
                var analyzer = JSON.parse(analyzerJson);
                Analyzer.initAnalyzer(analyzer);
            }
        }
        
        themePageInit();
    },
    
    errorCallback : function(error) {
        alert(error.message);
    },
    
    renderAjaxContent : function(menus, content, title, homeBanner) {
        //update body id according to url
        var currentPath = window.location.pathname;
        var menuId = currentPath.substring(currentPath.lastIndexOf("/") + 1);
        $("body").attr("id", menuId);
        
        $("title").html($(title).html());
        
        AjaxUniversalTheme.updateMenus(menus);
        $("#content main").html($(content).html());
        $("#content main").attr("aria-live", "polite");
        
        $(".home_banner").remove();
        $("body").removeClass("has_home_banner");
        if ($(homeBanner).find(".home_banner").length > 0) {
            $("#page #main").before($(homeBanner).find(".home_banner"));
            $("body").addClass("has_home_banner");
        }
        
        AjaxComponent.overrideFormEvent($("#category-container"));
        AjaxComponent.initContent($("#content main"));
        AjaxMenusCount.init();
        
        if ($("#content main").find(".c-overflow").length > 0) {
            AjaxUniversalTheme.scrollBar(".c-overflow", "y");
        }
        
        $("html, body").animate({
            scrollTop: 0
        }, 0);

        setTimeout(function(){
            $(window).trigger('resize'); //inorder for datalist to render in correct viewport
        }, 5);
    },
    
    updateMenus : function(menus) {
        if ($(menus).find("[data-ajaxmenucount]").length > 0) {
            //temporary replace count with current page value
            $(menus).find("[data-ajaxmenucount]").each(function(){
                var id = $(this).data("ajaxmenucount");
                if ($("#category-container #"+id).length > 0) {
                    var count = $("#category-container #"+id+" .rowCount").text();
                    $(this).text(count);
                }
            });
        }
        
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
    },
    
    /* remove all dynamic added elemetns of the page */
    clearDynamicElement : function() {
        $("#ajaxtheme_dynamic_elements_after_this").nextAll().remove();
        $("#ajaxtheme_dynamic_elements_after_this").nextAll().remove(); //the previous 1 only able to clear the wrapper of jquery ui dialog, need to run another time to remove it.
    },
    
    /* login url should follow current page url */
    updateLoginUrl : function() {
        if ($("a[href*='/web/ulogin/'], a[href*='/web/embed/ulogin/']").length > 0) {
            $("a[href*='/web/ulogin/'], a[href*='/web/embed/ulogin/']").each(function(){
                var href = $(this).attr("href");
                var currentUrl = window.location.href;
                
                //if not same url, no need update the url if it is same menu
                if (currentUrl.indexOf(href) === -1) {
                    //remove menu id
                    href = href.substring(0, href.lastIndexOf('/') + 1);
                    
                    var compareUrl = href.replace("/ulogin/", "/userview/");

                    //if same UI view
                    var index = currentUrl.indexOf(compareUrl);
                    if (index !== -1) {
                        href = href + currentUrl.substring(index + compareUrl.length);
                        
                        //remove query string
                        if (href.indexOf("?") !== -1) {
                            href = href.substring(0, href.indexOf("?")-1);
                        }
                        
                        $(this).attr("href", href);
                    }
                }
            });
        }
    }
};

$(function(){
    AjaxUniversalTheme.init($("body"));
});

