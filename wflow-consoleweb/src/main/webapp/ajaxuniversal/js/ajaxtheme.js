AjaxUniversalTheme = {
    init : function(target) {
        AjaxUniversalTheme.overrideLinkEvent(target);
        AjaxUniversalTheme.initContent(target);
        
        window.onpopstate = function(event) {
            if (event.state) {
                var url = event.state.url;
                if (AjaxUniversalTheme.isCurrentUserviewUrl(url)) {
                    AjaxUniversalTheme.call(url, "GET", null);
                }
            }
        }
    },
    
    initContent : function(target) {
        AjaxUniversalTheme.overrideButtonEvent(target);
        AjaxUniversalTheme.overrideDatalistButtonEvent(target);
        AjaxUniversalTheme.overrideFormEvent(target);
    },
    
    overrideLinkEvent : function(target) {
        $(target).on("click", "a[href]", function(e){
            var a = $(this);
            var href = $(a).attr("href");
            if (AjaxUniversalTheme.isCurrentUserviewUrl(href) && !AjaxUniversalTheme.isDatalistExportLink(a)) {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                AjaxUniversalTheme.call(href, "GET", null);
                return false;
            }
            return true;
        });
    },
    
    overrideDatalistButtonEvent : function(target) {
        $(target).find(".dataList button[data-href]").each(function(){
            var btn = $(this);
            var url = $(btn).data("href");
            var target = $(btn).data("target");
            if (AjaxUniversalTheme.isCurrentUserviewUrl(url) 
                    && (target === null || target === undefined || target === "" || target === "_top" || target === "_parent" || target === "_self")) {
                var confirmMsg = $(btn).data("confirmation");
                $(btn).off("click");
                $(btn).removeAttr("onclick");
                $(btn).on("click", function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    if (confirmMsg === "" || confirmMsg === null || confirmMsg === undefined || confirm(confirmMsg)) {
                        AjaxUniversalTheme.call(url, "GET", null);
                    }
                    return false;
                });
            }
        });
        
        //remove pagination if only 1 page
        if ($(target).find(".dataList .pagelinks a").length === 0) {
            $(target).find(".dataList .pagelinks").css("visibility", "hidden");
        }
    },
    
    overrideButtonEvent : function(target) {
        $(target).find("button[onclick], input[type=button][onclick]").each(function(){
            var btn = $(this);
            var onclick = $(btn).attr("onclick");
            if (onclick.indexOf("window.location") !== -1 || onclick.indexOf("top.location") !== -1 || onclick.indexOf("parent.location") !== -1
                    || onclick.indexOf("window.document.location") !== -1 || onclick.indexOf("top.document.location") !== -1 || onclick.indexOf("parent.document.location") !== -1) {
                var url = "";
                var confirmMsg = "";
                if (onclick.indexOf("confirm(") > 0) {
                    var part = AjaxUniversalTheme.getMsgAndRedirectUrl(onclick);
                    confirmMsg = part[0];
                    url = part[1];
                } else {
                    url = onclick.match(/(['"])((?:\\\1|(?:(?!\1).))+)\1/g)[0];
                    url = url.substring(1, url.length - 1);
                }
                if (url !== "" && AjaxUniversalTheme.isCurrentUserviewUrl(url)) {
                    $(btn).off("click");
                    $(btn).removeAttr("onclick");
                    $(btn).on("click", function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
                        if (confirmMsg === "" || confirmMsg === null || confirmMsg === undefined || confirm(confirmMsg)) {
                            AjaxUniversalTheme.call(url, "GET", null);
                        }
                        return false;
                    });
                }
            }
            return true;
        });
    },
    
    overrideFormEvent : function(target) {
        $(target).find("form").off("submit");
        $(target).find("form").on("submit", function(e){
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            
            var form = $(this);
            //datalist filter form
            if ($(form).hasClass("filter_form") && $(form).closest(".dataList").length > 0) {
                 var params = $(form).serialize();
                 var queryStr = window.location.search;
                 params = params.replace(/\+/g, " ");
                 var newUrl = window.location.pathname + "?" + UrlUtil.mergeRequestQueryString(queryStr, params);
                 AjaxUniversalTheme.call(newUrl, "GET", null);
            } else {
                 var formData = new FormData($(form)[0]);
                 var btn = $(this).find("input[type=submit][name]:focus, input[type=button][name]:focus, button[name]:focus" );
                 if ($(btn).length > 0) {
                     $(btn).each(function(){
                         formData.append($(this).attr("name"), $(this).val());
                     });
                 }
                 var url = $(form).attr("action");
                 if (url === "") {
                     url = window.location.href;
                 }
                 $.unblockUI();
                 AjaxUniversalTheme.call(url, "POST", formData);
            }
            
            return false;
        });
    },
    
    isCurrentUserviewUrl : function(url) {
        if (url !== null && url !== undefined && !url.startsWith("javascript") && url.indexOf("/web/ulogin/") < 0) {
            if (!(url.startsWith("http") || url.startsWith("/"))) {
                return true;
            } else {
                var currentPath = window.location.pathname;
                var currentOrigin = window.location.origin;
                
                if (url.startsWith("http")) {
                    if (!url.startsWith(currentOrigin)) {
                        return false;
                    } else {
                        url = url.replace(currentOrigin, "");
                    }
                }
                
                //remove userview key and menu id to compare
                currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
                currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
                url = url.substring(0, url.lastIndexOf("/"));
                url = url.substring(0, url.lastIndexOf("/"));
                
                if (currentPath === url) {
                    return true;
                }
            }
        }
        
        return false;
    },
    
    isDatalistExportLink : function(a) {
        return $(a).closest("div.exportlinks").length > 0;
    },
    
    call : function(url, method, formData) {
        if (method === "POST") {
            url = UrlUtil.updateUrlParam(url, ConnectionManager.tokenName, ConnectionManager.tokenValue);
        }
        
        $("#content").addClass("ajaxloading");
        
        const headers = new Headers();
        headers.append(ConnectionManager.tokenName, ConnectionManager.tokenValue);
        headers.append("__ajax_theme_loading", "true");
        var args = {
            method : method,
            headers: headers
        };
        
        if (formData !== undefined && formData !== null) {
            formData.append(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            args["body"] = formData;
        }
        
        fetch(url, args)
        .then(function (response) {
            if (method === "GET" || response.redirected) {
                history.pushState({url: response.url}, "", response.url); //handled redirected URL
            }
            return response.text();
        })
        .then(data => {
            if (data.indexOf("ajaxtheme_loading_container") !== -1) {
                var html = $(data);
                var menus = $(html).find("#ajaxtheme_loading_menus");
                var content = $(html).find("#ajaxtheme_loading_content");

                AjaxUniversalTheme.renderAjaxContent(menus, content);
            } else if (data.indexOf("<html>") !== -1 && data.indexOf("</html>") !== -1) {
                //handle userview redirection with alert
                if (data.indexOf("<div>") === -1) {
                    var part = AjaxUniversalTheme.getMsgAndRedirectUrl(data.substring(data.indexOf("alert")));
                    alert(part[0]);
                    AjaxUniversalTheme.call(part[1], "GET", null);
                } else {
                    //something wrong and caused the full html loaded
                    var html = $(data);
                    var menus = $(html).find("#category-container").wrap("<div>");
                    var content = $(html).find("#content main");
                    
                    AjaxUniversalTheme.renderAjaxContent(menus, content);
                }
            }
        })
        .catch(error => {
            alert(error.message);
            $("#content").removeClass("ajaxloading");
        });
    },
    
    renderAjaxContent : function(menus, content) {
        AjaxUniversalTheme.updateMenus(menus);
        $("#content main").html($(content).html());

        AjaxUniversalTheme.overrideFormEvent($("#category-container"));
        AjaxUniversalTheme.initContent($("#content main"));

        $("#content").removeClass("ajaxloading");

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
    },
    
    getMsgAndRedirectUrl : function(content) {
        var part = content.indexOf(".location") > 0?content.split(".location"):content.split("location.");
        var msg = part[0].match(/(['"])((?:\\\1|(?:(?!\1).))+)\1/g)[0];
        msg = msg.substring(1, msg.length - 1);
        var url = part[1].match(/(['"])((?:\\\1|(?:(?!\1).))+)\1/g)[0];
        url = url.substring(1, url.length - 1);
        
        return [msg, url];
    }
};

$(function(){
    AjaxUniversalTheme.init($("body"));
});

