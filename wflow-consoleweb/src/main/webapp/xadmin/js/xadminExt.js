;!function (win) {
    "use strict";
    function htmlDecode(input){
        var e = document.createElement('textarea');
        e.innerHTML = input;
        // handle case of empty input
        return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
    }

    win.xadmin.internalEnd = win.xadmin.end;
    win.xadmin.end = function() {
        this.internalEnd();
        $("body").trigger("Xadmin.end");
    };
    win.xadmin.internalAddtab = win.xadmin.add_tab;
    win.xadmin.add_tab = function (title,url,is_refresh) {
        if ($("#preview-form form#preview").length > 0) {
            var id = md5(url);
            element.tabAdd('xbs_tab', {
                title: title,
                content: '<iframe tab-id="'+id+'" id="'+id+'" name="'+id+'" frameborder="0" src="'+UI.base+'/images/v3/cj.gif" scrolling="yes" class="x-iframe"></iframe>',
                id: id
            })
            element.tabChange('xbs_tab', id);

            var newform = $('<form method="POST" data-ajax="false" style="display:none;" target="'+id+'" action="'+url+'"></form>'); 
            $("#preview-form form#preview").after(newform); 
            $(newform).html($("#preview-form form#preview").html());
            setTimeout(function() {
                $(newform).submit();
                $(newform).remove();
            }, 120);
        } else {
            this.internalAddtab(title,url,is_refresh);
        }
    };
    win.xadmin.openPopup = function(title, url, width, height, callback) {
        layui.use('layer', function(){
            var index = layer.open({
                type: 2,
                area: [width+'px', height +'px'],
                fix: false, //not fix
                maxmin: true,
                shadeClose: true,
                shade:0.4,
                title: title,
                content: url,
                cancel: function(){ 
                    //check is in iframe
                    if (parent && parent.layer && parent.xadmin && $(window.frameElement)) {
                        var pindex = parent.layer.getFrameIndex(window.name);
                        if (pindex) {
                            parent.layer.restore(pindex);
                        }
                    }
                    return true;
                }
            });
            
            layer.iframeAuto(index);
            if (callback) {
                callback(index);
            }
        });
    };
    win.xadmin.closePopup = function(index) {
        layer.close(index);
    };
    win.xadmin.redirect = function(title,url) {
        var currentURL = window.location.href;
        if (url.indexOf(currentURL.substring(currentURL.indexOf("/web/userview"), currentURL.lastIndexOf("/") + 1)) !== -1) { // if same userview page
            xadmin.add_tab(title, url, true);
        } else {
            window.location = url;
        }
    };
    win.xadmin.tabTitle = function(tabId, title) {
        title = htmlDecode(title);
        if (title.trim().length > 0) {
            $(".layui-tab li[lay-id='"+tabId+"']").each(function(){
                $(this).contents().first()[0].textContent = title;
            });
        }
    };
    win.xadmin.updateTabTitle = function(title) {
        if (parent && parent.layer && parent.xadmin && $(window.frameElement)) {
            var tabId = $(window.frameElement).attr("tab-id");
            parent.xadmin.tabTitle(tabId, title);
        }
    };
    win.xadmin.updateMenu = function() {
        if (parent && parent.AjaxMenusCount) {
            parent.AjaxMenusCount.init();
        }
    };
    win.xadmin.validateUrl = function(url) {
        var currentURL = window.location.href;
        // if same userview page, only same userview page can open in xadmin tab.
        // please refer to XadminTheme.java line 676 & xadmin.redirect
        if (url.indexOf(currentURL.substring(currentURL.indexOf("/web/userview"), currentURL.lastIndexOf("/") + 1)) !== -1) { 
            return true;
        }
        return false;
    };

    PopupDialog.prototype.show = function() {
        var newSrc = this.src;
        if (newSrc.indexOf("?") < 0) {
            newSrc += "?";
        }
        newSrc += "&_=" + new Date().valueOf().toString();
        newSrc += UI.userviewThemeParams();
        
        if (this.title === undefined || this.title === null || this.title.length === 0) {
            this.title = " ";
        }

        PopupDialogCache.popupDialog = this;

        var temWidth = $(window).width();
        var temHeight = $(window).height();
        if (temWidth >= 768) {
            this.width = temWidth * 0.8;
            this.height = temHeight * 0.9;
        } else {
            this.width = temWidth - 20;
            this.height = temHeight - 20;
        }
        var height = this.height;
        xadmin.openPopup(this.title, newSrc, this.width, this.height, function(index){
            PopupDialogCache.popupDialog.windowName = index;
            $("#layui-layer-iframe" + index).closest(".layui-layer-iframe").css("height", (parseInt(height) + 51) + "px");
        });
    };
    PopupDialog.prototype.close = function() {
        PopupDialogCache.popupDialog = null;
        xadmin.closePopup(this.windowName);
        return true;
    };
    
    JPopup.create = function (id, title, width, height) {
        //ignore
    };
    JPopup.show = function (id, url, params, title, width, height, action) {
        if (title === null || title === undefined || title === "") {
            title = " ";
        }
        
        //check is in iframe
        if (parent && parent.layer && parent.xadmin && $(window.frameElement)) {
            var pindex = parent.layer.getFrameIndex(window.name);
            if (pindex) {
                parent.layer.full(pindex);
            }
        }
        
        width = UI.getPopUpWidth(width);
        height = UI.getPopUpHeight(height);
        if (action !== undefined && action.toLowerCase() === "get") {
            $.each(params, function (key, data) {
                url += "&" + key + "=" + encodeURIComponent(data);
            });
            url += "&" + JPopup.tokenName + "="+ JPopup.tokenValue;
            
            xadmin.openPopup(title, url, width, height, function(index){
                JPopup.dialogboxes[id] = index;
                $("#layui-layer-iframe" + index).closest(".layui-layer-iframe").css("height", (parseInt(height) + 51) + "px");
            });
        } else {
            url += "&" + JPopup.tokenName + "="+ JPopup.tokenValue;
            
            xadmin.openPopup(title, UI.base+"/images/v3/cj.gif", width, height, function(index){
                JPopup.dialogboxes[id] = index;
                $("#layui-layer-iframe" + index).closest(".layui-layer-iframe").css("height", (parseInt(height) + 51) + "px");
                $("#layui-layer-iframe"+ index).replaceWith('<iframe scrolling="auto" allowtransparency="true" id="'+id+'" name="'+id+'" ></iframe>');
                
                var form = $('<form method="post" data-ajax="false" style="display:none;" target="' + id + '" action="' + url + '"></form>');
                $(document.body).append(form); 
                $.each(params, function (key, data) {
                    $(form).append("<input id=\""+key+"\" name=\""+key+"\">");
                    $(form).find('#'+key).val(data);
                });
                setTimeout(function() {
                    $(form).submit();
                    $(form).remove();
                }, 120);
            });
        }
    };
    JPopup.hide = function (id) {
        //check is in iframe
        if (parent && parent.layer && parent.xadmin && $(window.frameElement)) {
            var pindex = parent.layer.getFrameIndex(window.name);
            if (pindex) {
                parent.layer.restore(pindex);
            }
        }
        
        xadmin.closePopup(JPopup.dialogboxes[id]);
    };
    
    function uuid() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }
    
    function reloadImages() {
        $('img[data-lazysrc]').each(function () {
            $(this).attr('src', $(this).attr('data-lazysrc'));
        });
    }

    $(window).load(function() {
        reloadImages();
    });
    
    $(document).ready(function () {
        checkCookie();
        $('#smallFont').click(function () {
            setFontSize("13");
            setCookie("13");
        });

        $('#mediumFont').click(function () {
            setFontSize("17");
            setCookie("17");
        });

        $('#bigFont').click(function () {
            setFontSize("20");
            setCookie("20");
        });
        
        $(document).off("click.dropdown", "[data-toggle=\"dropdown\"]");
        $(document).on("click.dropdown", "[data-toggle=\"dropdown\"]", function(){
            var dropdown = $(this);
            $(document).off("click.dropdownclose");
            $(dropdown).toggleClass("focus");
            
            $(document).find("[data-toggle=\"dropdown\"]").each(function(){
               if (!$(this).is(dropdown)) {
                   $(this).removeClass("focus");
               }
            });
            
            if ($(this).hasClass("focus")) {
                $(document).on("click.dropdownclose", function(e){
                    if (!$(dropdown).is(e.target) && $(dropdown).has(e.target).length === 0) {
                        $(dropdown).removeClass("focus");
                        $(document).off("click.dropdownclose");
                    }
                });
            }
        });
    });
    
    function setCookie(cvalue) {
        $.cookie("fontsize", cvalue);
    }

    function checkCookie() {
        if ($('.adjustfontSize').length > 0) {
            var fontSize = $.cookie("fontsize");
            if (fontSize !== "" && fontSize !== null) {
                setFontSize(fontSize);
            } else {
                setFontSize('13');
            }
        }
    }

    function setFontSize(size) {
        var fontClass = "";
        $(".buttonFontSize").removeClass("activeFont");
        if (size === '13') {
            $('#smallFont').addClass("activeFont");
        } else if (size === '17') {
            fontClass = "mediumFontSize";
            $('#mediumFont').addClass("activeFont");
        } else {
            fontClass = "largeFontSize";
            $('#bigFont').addClass("activeFont");
        }
        
        $('body').removeClass("largeLabel");
        if (size !== '13') {
            $('body').addClass("largeLabel");
        }

        $('body').removeClass("smallFontSize mediumFontSize largeFontSize");
        $('body').addClass(fontClass);
    }
    
    $(function(){
        $(".form-cell-validator, .subform-cell-validator").each(function(){
            $(this).parent().prepend($(this));
            $(this).show();
        });
        $("#form-canvas .grid").prev(".form-clear").remove();
        $("fieldset").each(function(){
            if ($(this).find("> legend").length > 0 && $(this).find("> legend").text().length > 0) {
                $(this).addClass("has-legend");
            }
        });
        //fix section padding
        $(".form-section.no_label .form-column:eq(0) > div:eq(0) > .subform-container > .subform-section:eq(0):not(.no_label)").each(function(){
            $(this).closest(".form-section").addClass("fix-padding");
        });
        if (parent && parent.layer && ($("body.popupBody #main-body-header").length > 0 || $("[class*=-body-header]").length > 0)) {
            var index = parent.layer.getFrameIndex(window.name);
            var title = "";
            if ($("body.popupBody #main-body-header").length > 0) {
                title = $("body.popupBody #main-body-header").text();
            } else if ($("[class*=-body-header]").length > 0) {
                title = $("[class*=-body-header]").text();
            }
            parent.layer.title(title, index);
        }
        if ($("#side-nav form").length > 0) {
            layui.use(['layer','element'],function() {
                $("#side-nav form").each(function() {
                    var form = $(this);
                    $(form).submit(function(){
                        var id = uuid();
                        var action = $(form).attr("action");
                        var method = $(form).attr("method");
                        var title = $(form).parent().find("> a").text();

                        element.tabAdd('xbs_tab', {
                            title: title,
                            content: '<iframe tab-id="'+id+'" id="'+id+'" name="'+id+'" frameborder="0" src="'+UI.base+'/images/v3/cj.gif" scrolling="yes" class="x-iframe"></iframe>',
                            id: id
                        })
                        element.tabChange('xbs_tab', id);
                        
                        var newform = $('<form method="'+method+'" data-ajax="false" style="display:none;" target="'+id+'" action="'+action+'"></form>'); 
                        $(form).after(newform); 
                        $(newform).html($(form).html());
                        setTimeout(function() {
                            $(newform).submit();
                            $(newform).remove();
                        }, 120);
                        
                        return false;
                    });
                });
            });    
        }
        if (parent && parent.layer && !$("body").hasClass("index-window")) {
            //prevent redirect away from index page
            $("a[onclick], button[onclick]").each(function(){
                var onclick = $(this).attr("onclick");
                if (onclick.indexOf("top.location=") !== -1) {
                    if ($("body", window.top.document).hasClass("index-window")) {
                        if (onclick.indexOf("window.top") !== -1) {
                            onclick = onclick.replace("top.location=", "parent.location=");
                        } else {
                            onclick = onclick.replace("top.location=", "window.parent.location=");
                        }
                        $(this).attr("onclick", onclick);
                    }
                }
                if (onclick.indexOf("window.parent.location=") !== -1
                        || onclick.indexOf("parent.document.location.href=") !== -1) {
                    if ($("body", window.parent.document).hasClass("index-window")) {
                        onclick = onclick.replace("window.parent.location=", "document.location=");
                        onclick = onclick.replace("parent.document.location.href=", "document.location=");
                        $(this).attr("onclick", onclick);
                    }
                }
            });
            $("a[target='_top'], a[target='_parent']").each(function(){
                var target = $(this).attr("target");
                if (target === "_top") {
                    if ($("body", window.top.document).hasClass("index-window")) {
                        target = "_parent";
                        $(this).attr("target", target);
                    }
                    if ($("body", window.parent.document).hasClass("index-window")) {
                        target = "_self";
                        $(this).attr("target", target);
                    }
                }
            });
        }
        
        /* ---------- Inbox ------------------------- */
        function loadInbox() {
            if ($(".inbox-notification").length === 1) {
                loadInboxData();
                $(".inbox-notification .refresh").on("click", function(e) {
                    e.preventDefault();
                    loadInboxData();
                    return false;
                });
                
                setTimeout(function(){
                    loadInboxData();
                }, 300000);
            }
        }
        function loadInboxData() {
            $(".inbox-notification .loading").show();
            var url = $(".inbox-notification").data("url");
            $.getJSON(url + "&_t=" + (new Date()).getTime(), {},
                function(data) {
                    var count = 0;
                    if (data.count !== undefined) {
                        count = data.count;
                    }
                    $(".inbox-notification > a > .badge").text(count);
                    $(".inbox-notification .inbox-title .count").text(count);
                    $(".inbox-notification > dl > dd.task").remove();
                    if (data.data) {
                        var footer = $(".inbox-notification dd .dropdown-menu-sub-footer").parent();
                        var link = $(".inbox-notification dd .dropdown-menu-sub-footer").attr("data-href");
                        $.each(data.data, function(i, d) {
                            var html = "<dd class=\"task\"><a onclick=\"xadmin.add_tab('"+d.activityName+"', '" + link + "?_mode=assignment&activityId=" + d.activityId + "', true)\">";
                            html += "<span class=\"header\">" + d.activityName + "</span>";
                            html += "<span class=\"message\">" + d.processName + "</span><span class=\"time\">" + d.dateCreated + "</span>";
                            html += "</a></dd>";
                            footer.before($(html));
                        });
                    }

                    $(".inbox-notification .loading").hide();
                }
            );
        }
        loadInbox();
    });
}(window);
