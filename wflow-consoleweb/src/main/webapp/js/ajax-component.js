AjaxComponent = {
    currentUrlEventListening : [],
    
    /*
     * Intialize the content to found ajax supported component and event listening component
     */
    initAjax : function(element) {
        AjaxComponent.unbindEvents();
        $(element).find("[data-ajax-component], [data-ajax-component][data-events-triggering], [data-ajax-component][data-events-listening]").each(function() {
            AjaxComponent.initContent($(this));
        });
        
        setTimeout(function(){
            AjaxComponent.triggerPageLoadedEvent();
            AjaxComponent.triggerEvents($("#content.page_content"), window.location.href, "get");
        }, 2);
        
        setTimeout(function(){
            $(window).trigger('resize'); //inorder for datalist to render in correct viewport
        }, 5);
    },
    
    /*
     * Override the behaviour of an AJAX supported component
     */
    initContent : function(element) {
        AjaxComponent.overrideLinkEvent(element);
        setTimeout(function(){
            AjaxComponent.overrideButtonEvent(element);
            AjaxComponent.overrideDatalistButtonEvent(element);
            AjaxComponent.overrideFormEvent(element);
            AjaxComponent.initEventsListening(element);

            if (window["AdminBar"] !== undefined) {
                AdminBar.initQuickEditMode();
            }
        },1);
    },
    
    /*
     * Override the link behaviour
     */
    overrideLinkEvent : function(element) {
        setTimeout(function(){
            $(element).on("click", "a[href]:not(.off_ajax)", function(e){
                var a = $(this);
                var href = $(a).attr("href");
                var target = $(a).attr("target");
                var onclick = $(a).attr("onclick");           
                if ((typeof PwaUtil === 'undefined' || PwaUtil.isOnline !== false) && onclick === undefined && AjaxComponent.isCurrentUserviewUrl(href) && !AjaxComponent.isDatalistExportLink(a)
                        && (target === null || target === undefined || target === "" || target === "_top" || target === "_parent" || target === "_self")) {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    AjaxComponent.call($(a), href, "GET", null);
                    return false;
                }
                return true;
            });
        },1);
    },
    
    /*
     * Override the datalist button behaviour
     */
    overrideDatalistButtonEvent : function(element) {  
        $(element).find(".dataList button[data-href]:not(.off_ajax)").each(function(){
            var btn = $(this);
            var url = $(btn).data("href");
            var target = $(btn).data("target");
            var hrefParam = $(btn).data("hrefparam");
            if (AjaxComponent.isCurrentUserviewUrl(url) 
                    && (target === null || target === undefined || target === "" || target === "_top" || target === "_parent" || target === "_self")
                    && (hrefParam === undefined || hrefParam === "")) {
                var confirmMsg = $(btn).data("confirmation");
                $(btn).off("click");
                $(btn).removeAttr("onclick");
                $(btn).on("click", function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    if (confirmMsg === "" || confirmMsg === null || confirmMsg === undefined || confirm(confirmMsg)) {
                        if (typeof PwaUtil === 'undefined' || PwaUtil.isOnline !== false) {
                            AjaxComponent.call($(btn), url, "GET", null);
                        } else {
                            if (target === "" || target.toLowerCase() === "_self" || target.toLowerCase() === "_top") {
                                window.top.location = url;
                            } else if (target.toLowerCase() === "_parent") {
                                if (window.parent) {
                                    window.parent.location = url;
                                } else {
                                    document.location = url; 
                                }
                            }
                        }
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
        $(element).find("button[onclick]:not(.off_ajax), input[type=button][onclick]:not(.off_ajax)").each(function(){
            if (typeof PwaUtil === 'undefined' || PwaUtil.isOnline !== false) {
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
            }
            return true;
        });
    },
    
    /*
     * Override the form submission behaviour
     */
    overrideFormEvent : function(element) {
        $(element).find("form:not(.off_ajax)").off("submit");
        $(element).find("form:not(.off_ajax)").on("submit", function(e){
            if (typeof PwaUtil === 'undefined' || PwaUtil.isOnline !== false) {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();

                var form = $(this);
                //datalist filter form
                if ($(form).hasClass("filter_form") && $(form).closest(".dataList").length > 0) {
                     var params = UrlUtil.serializeForm($(form));
                     var queryStr = window.location.search;
                     params = params.replace(/\+/g, " ");
                     var newUrl = window.location.pathname + "?" + UrlUtil.mergeRequestQueryString(queryStr, params);
                     AjaxComponent.call($(form), newUrl, "GET", null);
                } else {
                    var formData = new FormData($(form)[0]);
                    var btn;
                    if (e.originalEvent !== undefined && e.originalEvent.submitter !== undefined) {
                       btn = $(e.originalEvent.submitter);
                    } else {
                       btn = $(this).find(document.activeElement);
                    }
                    if (($(btn).length === 0 || !$(btn).is('input[type=submit], input[type=button], button, a')) && $(this).find("input[type=submit]:focus, input[type=button]:focus, button:focus").length === 0) {
                        btn = $(this).find("input[type='submit'][name], input[type='button'][name], button[name]").eq(0);
                    }
                    if ($(btn).length > 0) {
                        $(btn).each(function(){
                            $(this).attr('clicked', 'true');
                            formData.append($(this).attr("name"), $(this).val());
                        });
                    }
                    if (typeof PwaUtil !== 'undefined'){
                        PwaUtil.submitForm(form);
                    }
                    var url = $(form).attr("action");
                    if (url === "") {
                        url = window.location.href;
                    }
                    $.unblockUI();
                    AjaxComponent.call($(form), url, "POST", formData);
                }
                
                return false;
            } else {
                var btn = $(this).find("input[type=submit][name]:focus, input[type=button][name]:focus, button[name]:focus");
                if ($(btn).length === 0 && $(this).find("input[type=submit]:focus, input[type=button]:focus, button:focus").length === 0) {
                    btn = $(this).find("input[type=submit][name], input[type=button][name], button[name]").eq(0);
                }
                if ($(btn).length > 0) {
                    $(btn).each(function () {
                        $(this).attr('clicked', 'true');
                    });
                }
                PwaUtil.submitForm(this);
                return true;
            }
        });
    },
 
    /*
     * Ajax call to retrieve the component html
     */
    call : function(element, url, method, formData, customCallback, customErrorCallback, isTriggerByEvent) {
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
            $(".ma-backdrop").trigger("click.sidebar-toggled");
        }
        
        if (!AjaxComponent.isCurrentUserviewUrl(url) || (typeof PwaUtil !== 'undefined' && PwaUtil.isOnline === false ) || AjaxComponent.isLanguageSwitching(url)) { 
            window.top.location.href = url; 
            return;
        }
        
        var isAjaxComponent = false;
        if (method === "POST") {
            url = UrlUtil.updateUrlParam(url, ConnectionManager.tokenName, ConnectionManager.tokenValue);
        }
        
        var headers = new Headers();
        headers.append(ConnectionManager.tokenName, ConnectionManager.tokenValue);
        headers.append("__ajax_theme_loading", "true");
        
        var contentConatiner = $("#content.page_content");
        
        if (AjaxComponent.isCurrentUserviewPage(url)) {
            if ($(element).closest("[data-ajax-component]").length > 0) {
                isAjaxComponent = true;
                contentConatiner = $(element).closest("[data-ajax-component]");

                headers.append("__ajax_component", $(contentConatiner).attr("id"));
                
                if(isTriggerByEvent) {
                    $(contentConatiner).data("event-url", url);
                } else {
                    //merge parameter with the url trigger by event
                    if ($(contentConatiner).data("event-url") !== undefined) {
                        var qs1 = "";
                        var qs2 = "";
                        if ($(contentConatiner).data("event-url").indexOf("?") !== -1) {
                            qs1 = $(contentConatiner).data("event-url").substring($(contentConatiner).data("event-url").indexOf("?") + 1);
                        }
                        if (url.indexOf("?") !== -1) {
                            qs2 = url.substring(url.indexOf("?") + 1);
                        }
                        
                        url = window.location.pathname + "?" + UrlUtil.mergeRequestQueryString(qs1, qs2);
                    }
                }
            }
            //check it is a link clicked event, trigger event and do nothing else
            if ($(element).closest("[data-ajax-component][data-events-triggering]").length > 0 && method === "GET" && AjaxComponent.isLinkClickedEvent($(element).closest("[data-ajax-component][data-events-triggering]"), url)) {
                return;
            }
        } else {
            if (window['AjaxUniversalTheme'] === undefined) { 
                window.top.location.href = url;
                return;
            }
            
            AjaxComponent.unbindEvents();
        }
        
        $(contentConatiner).addClass("ajaxloading");
        
        var contentPlaceholder = $(element).data("ajax-content-placeholder");
        if (contentPlaceholder === undefined || contentPlaceholder === null || contentPlaceholder === "") {
            contentPlaceholder = AjaxComponent.getContentPlaceholder(url);
            
            if (contentPlaceholder === undefined || contentPlaceholder === null || contentPlaceholder === "") {
                contentPlaceholder = $(contentConatiner).data("ajax-content-placeholder");
            }
        }
        $(contentConatiner).attr("data-content-placeholder", contentPlaceholder);
        $(contentConatiner).removeAttr("aria-live");
        
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
            if (response.status === 403) {
                if (AjaxComponent.retry !== true) {
                    AjaxComponent.retry = true;
                    //could be csrf token expired, retrieve new token and retry once
                    $.ajax({
                        type: 'POST',
                        url: UI.base + "/csrf",
                        headers: {
                            "FETCH-CSRF-TOKEN-PARAM":"true",
                            "FETCH-CSRF-TOKEN":"true"
                        },
                        success: function (response) {
                            var temp = response.split(":");
                            ConnectionManager.tokenValue = temp[1];
                            JPopup.tokenValue = temp[1];

                            $("iframe").each(function() {
                                try {
                                    if (this.contentWindow.ConnectionManager !== undefined) {
                                        this.contentWindow.ConnectionManager.tokenValue = temp[1];
                                    }
                                    if (this.contentWindow.JPopup !== undefined) {
                                        this.contentWindow.JPopup.tokenValue = temp[1];
                                    }
                                } catch(err) {}
                            });

                            AjaxComponent.call(element, url, method, formData, customCallback, customErrorCallback, isTriggerByEvent);
                        },
                        completed: function() {
                            AjaxComponent.retry = false;
                        }
                    });
                } else {
                    document.location.href = url;
                }
            }
            
            const disposition = response.headers.get('Content-Disposition');
            if (disposition !== null && disposition.indexOf("attachment;") === 0) {
                var filename = "download";
                var i = disposition.toLowerCase().indexOf("utf-8''");
                if (i !== -1) {
                    filename = decodeURIComponent(disposition.substring(i+7));
                } else {
                    filename = disposition.split(/;(.+)/)[1].split(/=(.+)/)[1];
                }
        
                //it is file
                var a = document.createElement("a");
                a.setAttribute("download", filename);
                //check environment
                if (navigator.userAgent.toLowerCase().includes('android')) {
                    // If the environment is Android, directly retrieve the URL from the response
                    a.href = response.url;
                    a.click();
                } else {
                    response.blob().then((b) => {
                        a.href = URL.createObjectURL(b);
                        a.click();
                    });
                }
                $(contentConatiner).removeClass("ajaxloading");
                return null;
            } else {
                if (response.url.indexOf("/web/login") !== -1) {
                    document.location.href = url;
                    return null;
                } else if (AjaxComponent.isLanguageSwitching(response.url)) {    
                    document.location.href = response.url;
                    return null;
                } else if ((method === "GET" || response.redirected) && response.status === 200) {
                    //only change url if is page change or main component
                    if (!isAjaxComponent || $(contentConatiner).hasClass("main-component")) {
                        var resUrl = response.url;
                        history.pushState({url: resUrl}, "", resUrl); //handled redirected URL
                    }
                }
                return response.text();
            }
        })
        .then(function (data){
            if (data !== null) {
                if (data.indexOf("<html>") !== -1 && data.indexOf("</html>") !== -1) {
                    //handle userview redirection with alert
                    if (data.indexOf("<div") === -1) {
                        var part = AjaxComponent.getMsgAndRedirectUrl(data);
                        if (part[0] !== "") {
                            alert(part[0]);
                        }
                        if (part[1] !== null && part[1] !== undefined) { //if there is URL
                            if (part[2] === null) { //if no target window, use current window
                                if (part[1] === "") { // it is a reload when url is empty
                                    part[1] = document.location.href;
                                }
                                //if redirect url is not same with current userview page
                                if (!AjaxComponent.isCurrentUserviewPage(part[1])) {
                                    AjaxComponent.call($("#content.page_content"), part[1], "GET", null);
                                } else {
                                    AjaxComponent.triggerEvents(contentConatiner, url, method);
                                    AjaxComponent.call(contentConatiner, part[1], "GET", null);
                                }
                            } else { //if target is top or parent window
                                var win = part[2];
                                if (part[1] === "") { // it is a reload when url is empty
                                    part[1] = win.location.href;
                                }
                                if(win["AjaxComponent"]){ //use ajax component to reload/redirect if exist
                                    if (part[1].indexOf("embed=false") !== -1) { //remove embed false url
                                        part[1] = part[1].replace("embed=false", "");
                                    }
                                    win["AjaxComponent"].call($("#content.page_content", win["document"]), part[1], "GET", null);
                                } else {
                                    win.location.href = part[1];
                                }
                                part[3] = true; //if the target is parent or top, always close popup if exist
                            }
                        }
                        if (part[3] === true && parent.PopupDialog) { 
                            parent.PopupDialog.closeDialog();
                        }
                        return;
                    }
                }

                if (!isAjaxComponent && window['AjaxUniversalTheme'] !== undefined) {
                    window['AjaxUniversalTheme'].callback(data);
                } else {
                    AjaxComponent.callback(contentConatiner, data, url);
                }
                if (customCallback){
                    customCallback();
                }
                
                setTimeout(function(){
                    if (!isAjaxComponent) {
                        AjaxComponent.triggerPageLoadedEvent();
                    }
                    AjaxComponent.triggerEvents(contentConatiner, url, method, formData);
                }, 2);
                
                $(contentConatiner).removeClass("ajaxloading");
                $(contentConatiner).removeAttr("data-content-placeholder");
            }
        })
        .catch(function (error) {
            if (!isAjaxComponent && window['AjaxUniversalTheme'] !== undefined) {
                window['AjaxUniversalTheme'].errorCallback(error);
            } else {
                AjaxComponent.errorCallback(element, error);
            }
            if (customErrorCallback){
                customErrorCallback();
            }
            $(contentConatiner).removeClass("ajaxloading");
            $(contentConatiner).removeAttr("data-content-placeholder");
        });
    },
    
    /*
     * Handle the ajax callback
     */
    callback : function(element, data, url) {
        var newTarget = $(data);
        var eventUrl = $(element).data("event-url");
        $(element).replaceWith(newTarget);
        $(newTarget).data("ajax-url", url);
        if (eventUrl) {
            $(newTarget).data("event-url", eventUrl);
        }
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
    
    isLinkClickedEvent : function(element, url) {
        return AjaxComponent.triggerEvents(element, url, "linkClicked"); 
    },
    
    /*
     * trigger a default page loaded event
     */
    triggerPageLoadedEvent : function() {
        var urlParams = {};
        var url = window.location.href;
        if (url.indexOf("?") !== -1) {
            urlParams = UrlUtil.getUrlParams(url.substring(url.indexOf("?") + 1));
        }
            
        AjaxComponent.triggerEvent("page_loaded", urlParams);
    },
    
    /*
     * Check the event triggering rules and trigger event
     */
    triggerEvents : function(element, url, method, formData) {
        var triggered = false;
        
        if (method === undefined) {
            method = "GET";
        }
        
        if (!$(element).is("[data-ajax-component]")) {
            element = $(element).find(".main-component");
        }
        
        if ($(element).is("[data-ajax-component][data-events-triggering]")) {
            var events = $(element).data("events-triggering");
            var urlParams = {};
            if (url.indexOf("?") !== -1) {
                urlParams = UrlUtil.getUrlParams(url.substring(url.indexOf("?") + 1));
            }
            for (var i in events) {
                var matched = true;
                
                if (events[i].ajaxMethod !== undefined && events[i].ajaxMethod.toLowerCase() !== method.toLowerCase()) {
                    matched = false;
                }
                
                var rules = events[i].parametersRules;
                if (matched && rules !== undefined && rules.length > 0) {
                    for (var r in rules) {
                        var rname = rules[r].name;
                        var op = rules[r].operator;
                        var compareValue = rules[r].value;
                        
                        var values = urlParams[rname];
                        if (values === undefined && formData !== undefined && formData.has(rname)) {
                            values = formData.get(rname);
                            if (!Array.isArray(values)) {
                                values = [values];
                            }
                        }
                        
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
                    triggered = true;
                    var eventName = events[i].name;
                    if ($(element).attr("data-pc-id") !== undefined && !$(element).is(".main-component")) {
                        eventName = eventName + "_" + $(element).attr("data-pc-id");
                    }
                    
                    AjaxComponent.triggerEvent(eventName, urlParams);
                } else if (events[i].notMatchName !== undefined && events[i].notMatchName !== "") {
                    var eventName = events[i].notMatchName;
                    if ($(element).attr("data-pc-id") !== undefined && !$(element).is(".main-component")) {
                        eventName = eventName + "_" + $(element).attr("data-pc-id");
                    }
                    
                    AjaxComponent.triggerEvent(eventName, urlParams);
                }
            }
        }
        return triggered;
    },
    
    /*
     * Trigger an event with url parameters
     */
    triggerEvent : function(name, urlParams) {
        var e = $.Event(name, urlParams);
        if (console && console.log) {
            console.log("Event `" + name + "` triggered.");
        }
        $("body").trigger(e);
    },
    
    /*
     * Based on the event listening config, listen to the event and do the action based on event
     */
    initEventsListening : function(element) {
        var listen = function(component) {
            if ($(component).is("[data-ajax-component][data-events-listening]") && !$(component).is("[data-events-listening-initialled]")) {
                var events = $(component).data("events-listening");
                var id = $(component).attr("id");
                for (var i in events) {
                    var eventName = events[i].name;
                    var eventObject = events[i].eventObject;
                    if (eventObject !== "") {
                        eventObject = "_" + eventObject;
                    }
                    if (eventName.indexOf(" ") !== -1) {
                        var temp = eventName.split(" ");
                        eventName = "";
                        for (var t in temp) {
                            if (temp[t].trim() !== "") {
                                eventName += temp[t] + eventObject + "." + id + "-" + i + " ";
                            }
                        }
                    } else {
                        eventName = eventName + eventObject + "." + id + "-" + i;
                    }

                    $("body").off(eventName);
                    $("body").on(eventName, "", {element: component, eventObj : events[i]}, function(event){
                        if (console && console.log) {
                            console.log("Event `" + event.type + "." + id + "-" + i + "` received.");
                        }
                        AjaxComponent.handleEventAction(event.data.element, event.data.eventObj, event);
                    });
                    AjaxComponent.currentUrlEventListening.push(eventName);
                }
                $(component).attr("data-events-listening-initialled", "");
            }
        };
        
        $(element).find("[data-ajax-component][data-events-listening]").each(function() {
            listen($(this));
        });
        if ($(element).is("[data-ajax-component][data-events-listening]")) {
            listen($(element));
        }
    },

    /*
     * Handle the event action when the listened event triggered
     */
    handleEventAction : function(element, eventObj, urlParams) {
        var action = eventObj.action;
        if (action === "hide") {
            $(element).hide();
            AjaxComponent.scrollIntoViewport($(".main-component"));
        } else if (action === "show") {
            $(element).show();
            if (eventObj.disabledScrolling === undefined || eventObj.disabledScrolling === "") {
                AjaxComponent.scrollIntoViewport($(element));
            }
            $(element).attr("aria-live", "polite");
        } else if (action === "reload") {
            var currentAjaxUrl = $(element).closest("[data-ajax-component]").data("ajax-url");
            if (currentAjaxUrl === undefined) {
                currentAjaxUrl = window.location.href;
            }
            AjaxComponent.call(element, currentAjaxUrl, "GET", null);
            $(element).show();
            if (eventObj.disabledScrolling === undefined || eventObj.disabledScrolling === "") {
                AjaxComponent.scrollIntoViewport($(element));
            }
            $(element).attr("aria-live", "polite");
        } else if (action === "parameters") {
            var url = $(element).closest("[data-ajax-component]").data("ajax-url");
            if (url === undefined || url === null) {
                url = window.location.href;
            }
            var newUrl = AjaxComponent.updateUrlParams(url, eventObj.parameters, urlParams);
            AjaxComponent.call(element, newUrl, "GET", null, null, null, true);
            $(element).show();
            if (eventObj.disabledScrolling === undefined || eventObj.disabledScrolling === "") {
                AjaxComponent.scrollIntoViewport($(element));
            }
            $(element).attr("aria-live", "polite");
        } else if (action === "reloadPage") {
            if (window['AjaxUniversalTheme'] !== undefined) {
                AjaxComponent.call($("#content.page_content"), window.location.href, "GET", null);
            } else {
                window.location.reload(true);
            }
        } else if (action === "redirectPage") {
            var url = AjaxComponent.getEventRedirectURL(eventObj.redirectUrl, urlParams);
            if (window['AjaxUniversalTheme'] !== undefined) {
               AjaxComponent.call($("#content.page_content"), url, "GET", null);
            } else {
                window.location.href = url;
            }
        } else if (action === "redirectComponent") {
            var url = AjaxComponent.getEventRedirectURL(eventObj.redirectUrl, urlParams);
            AjaxComponent.call($(element), url, "GET", null, null, null, true);
            $(element).show();
            if (eventObj.disabledScrolling === undefined || eventObj.disabledScrolling === "") {
                AjaxComponent.scrollIntoViewport($(element));
            }
            $(element).attr("aria-live", "polite");
        }
    },
    
    /*
     * Used to unbind all listener in current page
     */
    unbindEvents : function () {
        for (var i in AjaxComponent.currentUrlEventListening) {
            $("body").off(AjaxComponent.currentUrlEventListening[i]);
        }
        AjaxComponent.currentUrlEventListening = [];
    },
    
    /*
     * Update url parameters value based on event parameters
     */
    updateUrlParams : function(url, parameters, urlParams) {      
        var params = "";
        for (var i in parameters) {
            if (parameters[i].value !== "") {
                if (params !== "") {
                    params += "&";
                }
                params += parameters[i].name + "=" + parameters[i].value;
            }
        }
        params = AjaxComponent.getEventRedirectURL(params, urlParams);
        var currentParam = "";
        if (url.indexOf("?") !== -1) {
            currentParam = url.substring(url.indexOf("?") + 1);
        }
        
        var newUrl = window.location.pathname + "?" + UrlUtil.mergeRequestQueryString(currentParam, params);
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
        var regex = /(\{([a-zA-Z0-9_-]+)\})/g;
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
        if (url !== null && url !== undefined && url.indexOf("javascript") !== 0 && url.indexOf("/web/ulogin/") < 0) {
            if (!(url.indexOf("http") === 0 || url.indexOf("/") === 0)) {
                return true;
            } else {
                var currentPath = window.location.pathname;
                var currentOrigin = window.location.origin;
                
                if (url.indexOf("http") === 0) {
                    if (!url.indexOf(currentOrigin) === 0) {
                        return false;
                    } else {
                        url = url.replace(currentOrigin, "");
                    }
                }
                
                //remove userview key and menu id to compare
                currentPath = currentPath.replace("/web/embed", "/web");
                currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
                currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
                url = url.replace("/web/embed", "/web");
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
     * Check is language switching using parameter
     */
    isLanguageSwitching : function(url) {
        if (url !== null && url !== undefined && url.indexOf("_lang=") !== -1) {
            var params = UrlUtil.getUrlParams(url);
            var lang = params['_lang'][0];
            
            if (UI.locale !== lang) {
                return true;
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
        if (url.indexOf("/") === 0) {
            return window.location.pathname.indexOf(url) !== -1;
        } else if (url.indexOf("http") === 0) {
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
     * Extract out the alert message, redirect URL, target window & close popup flag from HTML
     */
    getMsgAndRedirectUrl: function(content) {
        //get script
        var index = content.indexOf("<script");
        if (index !== -1) {
            content = content.substring(content.indexOf(">", index) + 1, content.indexOf("</script>", index));
        }
        
        //split the content to get alert message and redirect url
        var part = content.indexOf(".location") > 0 ? content.split(".location") : content.split("location.");
        var regex = new RegExp(/(['"])((?:\\\1|(?:(?!\1).))+)\1/g); //regex to extract string between " or ' char
        var msg = "";
        var url = "";
        var target = null;
        var closePopup = false;
        if (content.indexOf("alert") !== -1) {
            if (regex.test(part[0])) {
                msg = part[0].match(regex)[0];
                msg = msg.substring(1, msg.length - 1);
            }
        }
        if (content.indexOf(".location") === -1) { //if there is no redirection
            url = null;
        }
        if (regex.test(part[1])) {
            url = part[1].match(regex)[0];
            url = url.substring(1, url.length - 1);
        }
        
        //check redirection target
        if (content.indexOf("top.window.location") !== -1 || content.indexOf("top.location") !== -1) {
            target = top;
        } else if (content.indexOf("parent.window.location") !== -1 || content.indexOf("parent.location") !== -1) {
            target = parent;
        }
        
        //check is there a script to close popup dialog
        if (content.indexOf("parent.PopupDialog.closeDialog();") !== -1) {
            closePopup = true;
        }
        return [msg, url, target, closePopup];
    },
    
    /*
     * Use to find the matching content placholder
     */
    getContentPlaceholder : function(url) {
        if (window["ajaxContentPlaceholder"] !== undefined) {
            var urlObj = new URL(url, window.location.origin);
            var rule = window["ajaxContentPlaceholder"][urlObj.pathname];
            if (rule !== undefined) {
                if (typeof(rule) === "string") {
                    return rule;
                } else {
                    for (var key in rule) {
                        if (rule.hasOwnProperty(key)) {
                            if (key !== "") {
                                var patt = new RegExp(key);
                                if (patt.test(urlObj.search)) {
                                    return rule[key];
                                }
                            }
                        }
                    }
                    if (rule[""] !== undefined) {
                        return rule[""];
                    }
                }
            }
        }
        return "";
    },
    
    scrollIntoViewport : function (element) {
        let elementTop = $(element).offset().top;
        let elementBottom = elementTop + $(element).outerHeight();

        let viewportTop = $(element).scrollTop();
        let viewportBottom = viewportTop + $(element).height();

        if (!(elementBottom > viewportTop && elementTop < viewportBottom)){
            $("html, body").animate({
                scrollTop: elementTop - 50
            }, 1000);
        }
    }  
};

$(function(){
    AjaxComponent.initAjax($("body"));
});
