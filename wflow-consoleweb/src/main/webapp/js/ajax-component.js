AjaxComponent = {
    url : null,
    
    /*
     * Intialize the content to found ajax supported component and event listening component
     */
    initAjax : function(element) {
        $(element).find("[data-ajax-component]").each(function() {
            AjaxComponent.overrideLinkEvent($(this));
            AjaxComponent.initContent($(this));
        });
    },
    
    /*
     * Override the behaviour of an AJAX supported component
     */
    initContent : function(element) {
        AjaxComponent.overrideButtonEvent(element);
        AjaxComponent.overrideDatalistButtonEvent(element);
        AjaxComponent.overrideFormEvent(element);
        
        var url = window.location.href;
        if (url !== AjaxComponent.url) {
            $("[data-events-triggering]").each(function() {
                if (!$(this).is(element)) {
                    AjaxComponent.triggerEvents($(this), window.location.href);
                }
            });
            AjaxComponent.url = url;
        }
        
        if (window["AdminBar"] !== undefined) {
            AdminBar.initQuickEditMode();
        }
    },
    
    /*
     * Override the link behaviour
     */
    overrideLinkEvent : function(element) {
        $(element).on("click", "a[href]", function(e){
            var a = $(this);
            var href = $(a).attr("href");
            var target = $(a).attr("target");
            var onclick = $(a).attr("onclick");
            if (onclick === undefined && AjaxComponent.isCurrentUserviewUrl(href) && !AjaxComponent.isDatalistExportLink(a)
                    && (target === null || target === undefined || target === "" || target === "_top" || target === "_parent" || target === "_self")) {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                AjaxComponent.call($(a), href, "GET", null);
                return false;
            }
            return true;
        });
    },
    
    /*
     * Override the datalist button behaviour
     */
    overrideDatalistButtonEvent : function(element) {
        $(element).find(".dataList button[data-href]").each(function(){
            var btn = $(this);
            var url = $(btn).data("href");
            var target = $(btn).data("target");
            if (AjaxComponent.isCurrentUserviewUrl(url) 
                    && (target === null || target === undefined || target === "" || target === "_top" || target === "_parent" || target === "_self")) {
                var confirmMsg = $(btn).data("confirmation");
                $(btn).off("click");
                $(btn).removeAttr("onclick");
                $(btn).on("click", function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    if (confirmMsg === "" || confirmMsg === null || confirmMsg === undefined || confirm(confirmMsg)) {
                        AjaxComponent.call($(btn), url, "GET", null);
                    }
                    return false;
                });
            }
        });
        
        //remove pagination if only 1 page
        if ($(element).find(".dataList .pagelinks a").length === 0) {
            $(element).find(".dataList .pagelinks").css("visibility", "hidden");
        }
    },
    
    /*
     * Override the button behaviour
     */
    overrideButtonEvent : function(element) {
        $(element).find("button[onclick], input[type=button][onclick]").each(function(){
            var btn = $(this);
            var onclick = $(btn).attr("onclick");
            if (onclick.indexOf("window.location") !== -1 || onclick.indexOf("top.location") !== -1 || onclick.indexOf("parent.location") !== -1
                    || onclick.indexOf("window.document.location") !== -1 || onclick.indexOf("top.document.location") !== -1 || onclick.indexOf("parent.document.location") !== -1) {
                var url = "";
                var confirmMsg = "";
                if (onclick.indexOf("confirm(") > 0) {
                    var part = AjaxComponent.getMsgAndRedirectUrl(onclick);
                    confirmMsg = part[0];
                    url = part[1];
                } else {
                    url = onclick.match(/(['"])((?:\\\1|(?:(?!\1).))+)\1/g)[0];
                    url = url.substring(1, url.length - 1);
                }
                if (url !== "" && AjaxComponent.isCurrentUserviewUrl(url)) {
                    $(btn).off("click");
                    $(btn).removeAttr("onclick");
                    $(btn).on("click", function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
                        if (confirmMsg === "" || confirmMsg === null || confirmMsg === undefined || confirm(confirmMsg)) {
                            AjaxComponent.call($(btn), url, "GET", null);
                        }
                        return false;
                    });
                }
            }
            return true;
        });
    },
    
    /*
     * Override the form submission behaviour
     */
    overrideFormEvent : function(element) {
        $(element).find("form").off("submit");
        $(element).find("form").on("submit", function(e){
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
                 AjaxComponent.call($(form), newUrl, "GET", null);
            } else {
                 var formData = new FormData($(form)[0]);
                 var btn = $(this).find("input[type=submit][name]:focus, input[type=button][name]:focus, button[name]:focus" );
                 if ($(btn).length === 0) {
                     btn = $(this).find("input[type=submit][name], input[type=button][name], button[name]").eq(0);
                 }
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
                 AjaxComponent.call($(form), url, "POST", formData);
            }
            
            return false;
        });
    },
    
    /*
     * Ajax call to retrieve the component html
     */
    call : function(element, url, method, formData) {
        if (url.indexOf("?") === 0) {
            var currentUrl = window.location.href;
            if (currentUrl.indexOf("?") > 0) {
                currentUrl = currentUrl.substring(0, currentUrl.indexOf('?'));
            }
            url = currentUrl + url;
        } else if (url.indexOf("http") !== 0 && url.indexOf("/") !== 0) {
            var currentUrl = window.location.href;
            url = currentUrl.substring(0, currentUrl.lastIndexOf('/') + 1) + url;
        }
        
        if ($(".ma-backdrop").is(":visible")) {
            $("body").trigger("click.sidebar-toggled");
        }
        
        var isAjaxComponent = false;
        if (method === "POST") {
            url = UrlUtil.updateUrlParam(url, ConnectionManager.tokenName, ConnectionManager.tokenValue);
        }
        
        const headers = new Headers();
        headers.append(ConnectionManager.tokenName, ConnectionManager.tokenValue);
        headers.append("__ajax_theme_loading", "true");
        
        var contentConatiner = $("#content");
        
        if ($(element).closest("[data-ajax-component]").length > 0 && AjaxComponent.isCurrentUserviewPage(url)) {
            isAjaxComponent = true;
            contentConatiner = $(element).closest("[data-ajax-component]");
            
            var currentAjaxUrl = $(element).closest("[data-ajax-component]").data("ajax-url");
            if (url === currentAjaxUrl) {
                return;
            }
            
            headers.append("__ajax_component", $(contentConatiner).attr("id"));
        }
        
        $(contentConatiner).addClass("ajaxloading");
        
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
            if ((method === "GET" || response.redirected) && response.status === 200) {
                history.pushState({url: response.url}, "", response.url); //handled redirected URL
            }
            return response.text();
        })
        .then(data => {
            if (data.indexOf("<html>") !== -1 && data.indexOf("</html>") !== -1) {
                //handle userview redirection with alert
                if (data.indexOf("<div>") === -1) {
                    var part = AjaxComponent.getMsgAndRedirectUrl(data.substring(data.indexOf("alert")));
                    alert(part[0]);
                    
                    //if redirect url is not same with current userview page
                    if (!AjaxComponent.isCurrentUserviewPage(part[1])) {
                        AjaxComponent.call($("#content"), part[1], "GET", null, callback, errorCallback);
                    } else {
                        AjaxComponent.call(contentConatiner, part[1], "GET", null, callback, errorCallback);
                    }
                    return;
                }
            }
            
            if (!isAjaxComponent && AjaxUniversalTheme !== undefined) {
                AjaxUniversalTheme.callback(data);
            } else {
                AjaxComponent.callback(contentConatiner, data, url);
            }
            $(contentConatiner).removeClass("ajaxloading");
        })
        .catch(error => {
            if (!isAjaxComponent && AjaxUniversalTheme !== undefined) {
                AjaxUniversalTheme.errorCallback(error);
            } else {
                AjaxComponent.errorCallback(element, error);
            }
            $(contentConatiner).removeClass("ajaxloading");
        });
    },
    
    /*
     * Handle the ajax callback
     */
    callback : function(element, data, url) {
        var newTarget = $(data);
        $(element).replaceWith(newTarget);
        $(element).data("ajax-url", url);
        AjaxComponent.initContent($(newTarget));

        setTimeout(function(){
            $(window).trigger('resize'); //inorder for datalist to render in correct viewport
        }, 5);
    },
    
    /*
     * Handle the ajax error callback
     */
    errorCallback : function(element, data) {
        //ignore for now
    },
    
    /*
     * Check the event triggering rules and trigger event
     */
    triggerEvents : function(element, url) {
        if ($(element).is("[data-events-triggering]")) {
            var events = $(element).data("events-triggering");
            var urlParams = UrlUtil.getUrlParams(url);
            
            for (var i in events) {
                var matched = true;
                
                var rules = events[i].parametersRules;
                if (rules !== undefined && rules.length > 0) {
                    for (var r in rules) {
                        var rname = rules[r].name;
                        var op = rules[r].operator;
                        var compareValue = rules[r].value;
                        
                        var values = urlParams[rname];
                        
                        if (op === "==" || op === "!=" || op === ">" || op === ">=" || op === "<" || op === "<=" || op === "true" || op === "false" || op === "in") {
                            var value = ""
                            if (!(values === null || values === undefined || values.length === 0)) {
                                value = values[0];
                            }
                            if (op === "==" && !(compareValue == value)) {
                                matched = false;
                            } else if (op === "!=" && !(compareValue != value)) {
                                matched = false;
                            } else if (op === ">" && !(compareValue >= value)) {
                                matched = false;
                            } else if (op === ">=" && !(compareValue >= value)) {
                                matched = false;
                            } else if (op === "<" && !(compareValue < value)) {
                                matched = false;
                            } else if (op === "<=" && !(compareValue <= value)) {
                                matched = false;
                            } else if (op === "true" && !(value.toLowerCase() === "true")) {
                                matched = false;
                            } else if (op === "false" && !(value.toLowerCase() === "false")) {
                                matched = false;
                            } else if (op === "in") {
                                var compareValues = compareValue.split(";");
                                if (compareValues.indexOf(value) === -1) {
                                    matched = false;
                                }
                            }
                        } else if (op === "empty" || op === "notEmpty") {
                            var isEmpty = true;
                            if (values !== null && values !== undefined && values.length > 0) {
                                for (var v in values) {
                                    if (values[v].trim().length > 0) {
                                        isEmpty = false;
                                        break;
                                    }
                                }
                            }
                            if (op === "notEmpty") {
                                matched = !isEmpty;
                            } else {
                                matched = isEmpty;
                            }
                        } else {
                            var temp = false;
                            if (op === "contains") {
                                for (var v in values) {
                                    if (values[v] === compareValue) {
                                        temp = true;
                                        break;
                                    }
                                }
                            } else if (op === "regex") {
                                var regex = new RegExp(compareValue);
                                for (var v in values) {
                                    var result = regex.exec(values[v]);
                                    if (result.length > 0 && result[0] === value) {
                                        temp = true;
                                        break;
                                    }
                                }
                            }
                            if (!temp) {
                                matched = false;
                            }
                        }
                        
                        if (!matched) {
                            break;
                        }
                    }
                }
                
                if (matched) {
                    AjaxComponent.handleEventAction(element, events[i].action, events[i], urlParams);
                } else if (events[i].elseAction !== "") {
                    AjaxComponent.handleEventAction(element, events[i].elseAction, events[i], urlParams);
                }
            }
        }
    },
    
    /*
     * Handle the event action when the listened event triggered
     */
    handleEventAction : function(element, action, eventObj, urlParams) {
        if (action === "hide") {
            $(element).hide();
        } else if (action === "show") {
            if ($(element).closest("[data-ajax-component]").length > 0) {
                var currentAjaxUrl = $(element).closest("[data-ajax-component]").data("ajax-url");
                if (window.location.href !== currentAjaxUrl) {
                    AjaxComponent.call(element, window.location.href, "GET", null);
                }
            }
            $(element).show();
        } else if (action === "parameters") {
            var newUrl = AjaxComponent.updateUrlParams(eventObj.parameters);
            AjaxComponent.call(element, newUrl, "GET", null);
        } else if (action === "reloadPage") {
            if (AjaxUniversalTheme !== undefined) {
                AjaxComponent.call($("#content"), window.location.href, "GET", null);
            } else {
                window.location.reload(true);
            }
        } else if (action === "redirectPage") {
            var url = AjaxComponent.getEventRedirectURL(eventObj.redirectUrl, urlParams);
            if (AjaxUniversalTheme !== undefined) {
               AjaxComponent.call($("#content"), url, "GET", null);
            } else {
                window.location.href = url;
            }
        }
    },
    
    /*
     * Update url parameters value based on event parameters
     */
    updateUrlParams : function(parameters, urlParams) {      
        var params = "";
        for (var i in parameters) {
            if (parameters[i].value !== "") {
                if (params !== "") {
                    params += "&";
                }
                params += parameters[i].name + "=" + parameters[i].value;
            } else if (urlParams[parameters[i].name] !== undefined) {
                delete urlParams[parameters[i].name];
            }
        }
        params = AjaxComponent.getEventRedirectURL(params, urlParams);
        urlParams = $.extend(urlParams, UrlUtil.getUrlParams(params));
        
        var newUrl = window.location.pathname + "?" + UrlUtil.constructUrlQueryString(urlParams);
        return newUrl;
    },
    
    /*
     * Get config redirection url
     */
    getEventRedirectURL : function(url, urlParams) {
        if (url.indexOf("{") !== -1 && url.indexOf("}") !== -1) {
            url = AjaxComponent.replaceParams(url, urlParams);
        }
        return url;
    },
    
    /*
     * Replace variables in a value
     */
    replaceParams: function(value, params) {
        var regex = /(\{([a-zA-Z0-9_]+)\})/g;
        var matches = {};
        var match = regex.exec(value);
        while (match != null) {
            matches[match[2]] = match[1];
            match = regex.exec(value);
        }
        for (var key in matches) {
            if (matches.hasOwnProperty(key)) {
                var paramValue = params[key];
                if (Array.isArray(paramValue)) {
                    paramValue = paramValue.join(";");
                }
                value = value.replaceAll(matches[key], paramValue);
            }
        }
        return value;
    },
    
    /*
     * Check the URL is within the same userview
     */
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
    
    /*
     * Check the URL is same userview menu
     */
    isCurrentUserviewPage : function(url) {
        if (url.indexOf("?") !== -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.startsWith("/")) {
            return window.location.pathname.indexOf(url) !== -1;
        } else if (url.startsWith("http")) {
            var currentUrl = window.location.href;
            if (currentUrl.indexOf("?") !== -1) {
                currentUrl = currentUrl.substring(0, currentUrl.indexOf("?"));
            }
            return currentUrl === url;
        } else {
            var path = window.location.pathname;
            return path.substring(path.lastIndexOf("/")+1) === url;
        }
    },
    
    /*
     * Check a link is a datalist export
     */
    isDatalistExportLink : function(a) {
        return $(a).closest("div.exportlinks").length > 0;
    },
    
    /*
     * Extract out the alert message and redirect URL from HTML
     */
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
    AjaxComponent.initAjax($("body"));
});