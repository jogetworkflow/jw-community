var AdminBar = {
    cookiePath: "/",
    currentPageTitle: "",
    webConsole: false,
    builderMode: false,
    setCookiePath: function(path) {
        AdminBar.cookiePath = path;
    },
    showQuickOverlay: function(url) {
        if (AdminBar.webConsole && !AdminBar.builderMode) {
            document.location = url;
            return false;
        }
        
        if (typeof HelpGuide !== "undefined") {
            HelpGuide.hide();
        }
        
        if (AdminBar.builderMode && typeof AdvancedTools !== "undefined") {
            AdvancedTools.hideQuickOverlay();
        }
        
        url = UrlUtil.updateUrlParam(url, "_ov", (new Date().getTime()));
        
        var $quickOverlayFrame = $(parent.document).find("#quickOverlayFrame");
        if ($quickOverlayFrame.length === 0) {
            var overlayContainer = 
                '<div id="quickOverlayContainer" class="quickOverlayContainer"><div id="quickOverlay" class="quickOverlay"></div>\
                <div id="quickOverlayButton" class="quickOverlayButton"><a class="max" href="#" onclick="AdminBar.maxQuickOverlay()"><i class="fas fa-window-maximize"></i></a><a class="pin disabled" href="#" onclick="AdminBar.togglePinQuickOverlay();return false;"><i class="fas fa-thumbtack"></i></a><a class="overlayClose" href="#" onclick="AdminBar.hideQuickOverlay();return false;"><i class="fas fa-times"></i></a></div>\
                <div id="quickOverlayFrameDiv"><iframe id="quickOverlayFrame" name="quickOverlayFrame" src="about:blank"></iframe></div></div>';
            $(document.body).append(overlayContainer);
            $(document.body).addClass("stop-scrolling");
            $quickOverlayFrame = $(document.body).find("#quickOverlayFrame");
            
            if (/iPhone|iPod|iPad/.test(navigator.userAgent)) {
                $("body").addClass("fixiosframe");
            }
        }
        $quickOverlayFrame.attr("src", "about:blank");
        $quickOverlayFrame.attr("src", url);
        $quickOverlayFrame.addClass("iframeloading");
        $("#overlay, #quickOverlayButton, #quickOverlayFrameDiv").fadeIn();
        $quickOverlayFrame.on("load", function() {
            AdminBar.currentPageTitle = document.title;
            var frameTitle = $quickOverlayFrame[0].contentDocument.title;
            if (frameTitle !== "") {
                document.title = frameTitle;
            }
            $("#quickOverlayContainer").removeClass("minimize");
        });
        $("#quickOverlayFrameDiv, #adminBar, #adminControl, #quickOverlayButton").off("mouseenter mouseleave");
        $("#quickOverlayFrameDiv, #adminBar, #adminControl, #quickOverlayButton").on( "mouseenter", function() {
            $("#quickOverlayContainer").removeClass("minimize");
        }).on("mouseleave", function(event) {
            if ((event.relatedTarget === null || event.relatedTarget === undefined) && event.pageY < 0) {
                $("#quickOverlayContainer").addClass("minimize");
            }
        });
        $("#quickOverlay").off("mouseenter");
        $("#quickOverlay").on( "mouseenter", function() {
            $("#quickOverlayContainer").addClass("minimize");
        });
        
        $("#quickOverlay").off("click");
        $("#quickOverlay").on("click", function() {
            AdminBar.hideQuickOverlay();
        });
        
        AdminBar.initPinMode();
        
        return false;
    },
    openAppComposer: function(url) {
        AdminBar.hideQuickOverlay();
        if (window['CustomBuilder'] !== undefined) {
            CustomBuilder.ajaxRenderBuilder(url);
            return false;
        } else {
            return true;
        }
    },
    togglePinQuickOverlay: function() {
        var pinActive =  $.cookie("pinModeActive");
        if (pinActive === undefined || pinActive === null || pinActive === "true") {
            pinActive = "false";
        } else {
            pinActive = "true";
        }
        $.cookie("pinModeActive", pinActive, {
            path: AdminBar.cookiePath
        });
        AdminBar.initPinMode();
        return false;
    },
    maxQuickOverlay: function() {
        document.location = $("#quickOverlayFrame")[0].contentWindow.location.href;
    },
    hideQuickOverlay: function() {
        $("#adminBarButtons a").removeClass("current");
        $("#overlay, #quickOverlayButton, #quickOverlayFrameDiv").fadeOut();
        $("#quickOverlayContainer").remove();
        $(document.body).removeClass("stop-scrolling");
        if (AdminBar.currentPageTitle !== "") {
            document.title = AdminBar.currentPageTitle;
        }
        return false;
    },
    enableQuickEditMode: function() {
        var path = AdminBar.cookiePath;
        $.cookie("quickEditModeActive", "true", {
            path: path
        });
        AdminBar.initQuickEditMode();
    },
    disableQuickEditMode: function() {
        var path = AdminBar.cookiePath;
        $.cookie("quickEditModeActive", "false", {
            path: path
        });
        AdminBar.initQuickEditMode();
    },
    isQuickEditMode: function() {
        var quickEditModeActive = $.cookie("quickEditModeActive");
        return quickEditModeActive === null || quickEditModeActive === "true";
    },
    isAdminBarHide: function() {
        var adminBarModeHide =  $.cookie("adminBarModeHide");
        return adminBarModeHide === null || adminBarModeHide === "true";
    },
    showQuickEdit: function() {
        if (!AdminBar.isAdminBarHide()) {
            $(".analyzer-page").css("display", "inline-block");
            $("#quickEditMode").removeClass("off");
            $(".quickEdit").fadeIn();
            $(".analyzer-label").css("display", "inline-block");
            $(".analyzer-disabled").addClass("analyzer").removeClass("analyzer-disabled");
            $("body").addClass("quickEditModeActive");

            $("iframe").each(function(){
                try {
                    $(this)[0].contentWindow.AdminBar.showQuickEdit();
                } catch (err) {}
            });
        }
    },
    hideQuickEdit: function() {
        $("#quickEditMode").addClass("off");
        $(".quickEdit").css("display", "none");
        $(".analyzer-label, .analyzer-page").css("display", "none");
        $(".analyzer").addClass("analyzer-disabled").removeClass("analyzer");
        $("body").removeClass("quickEditModeActive");
        
        $("iframe").each(function(){
            try {
                $(this)[0].contentWindow.AdminBar.hideQuickEdit();
            } catch (err) {}
        });
    },
    initQuickEditMode: function() {
        var quickEditModeActive = AdminBar.isQuickEditMode();
        if (quickEditModeActive) {
            AdminBar.showQuickEdit();
        } else {
            AdminBar.hideQuickEdit();
        }
    },
    initPinMode: function() {
        var pinActive =  $.cookie("pinModeActive");
        if (pinActive === undefined || pinActive === null || pinActive === "true") {
            $("#quickOverlayContainer").addClass("pinned");
            $("#quickOverlayButton a.pin").removeClass("disabled");
        } else {
            $("#quickOverlayContainer").removeClass("pinned");
            $("#quickOverlayButton a.pin").addClass("disabled");
        }
    },
    initAdminBar: function() {
        $("#quickEditMode").on('click', function() {
            if ($("#quickEditMode").hasClass("off")) {
                AdminBar.enableQuickEditMode();
            } else {
                AdminBar.disableQuickEditMode();
            }
            return false;
        });
        if ((AdminBar.webConsole && !AdminBar.builderMode)) {
            $("#quickEditModeOption").hide();
        }
        if (AdminBar.isAdminBarHide()) {
            AdminBar.hideAdminBar();
        } else {
            AdminBar.showAdminBar();
        }
        // shortcut keys
        $(document).keyup(function (e) {
            if(e.which === 16){
                AdminBar.isShiftKeyPressed = false;
            } else if(e.which === 17){
                AdminBar.isCtrlKeyPressed = false;
            } else if(e.which === 18){
                AdminBar.isAltKeyPressed = false;
            }
        }).keydown(function (e) {
            if(e.which === 16){
                AdminBar.isShiftKeyPressed = true;
            } else if(e.which === 17){
                AdminBar.isCtrlKeyPressed = true;
            } else if(e.which === 18){
                AdminBar.isAltKeyPressed = true;
            }
            if(e.which === 48 && AdminBar.isCtrlKeyPressed && !AdminBar.isShiftKeyPressed && !AdminBar.isAltKeyPressed) { // CTRL+0
                if (AdminBar.isAdminBarHide()) {
                    AdminBar.showAdminBar();
                    AdminBar.enableQuickEditMode();
                } else {
                    AdminBar.hideAdminBar();
                    AdminBar.disableQuickEditMode();
                }
                return false;
            }  
            if(e.which === 49 && AdminBar.isCtrlKeyPressed && !AdminBar.isShiftKeyPressed && !AdminBar.isAltKeyPressed) { // CTRL+1
                AdminBar.showAdminBar();
                $("#adminBarButtons a.adminBarButton:eq(0)").trigger("click");
		return false;
            }
            if(e.which === 50 && AdminBar.isCtrlKeyPressed && !AdminBar.isShiftKeyPressed && !AdminBar.isAltKeyPressed) { // CTRL+2
                AdminBar.showAdminBar();
                $("#adminBarButtons a.adminBarButton:eq(1)").trigger("click");
		return false;
            }
            if(e.which === 51 && AdminBar.isCtrlKeyPressed && !AdminBar.isShiftKeyPressed && !AdminBar.isAltKeyPressed) { // CTRL+3
                AdminBar.showAdminBar();
                $("#adminBarButtons a.adminBarButton:eq(2)").trigger("click");
		return false;
            }
            if(e.which === 52 && AdminBar.isCtrlKeyPressed && !AdminBar.isShiftKeyPressed && !AdminBar.isAltKeyPressed) { // CTRL+4
                AdminBar.showAdminBar();
                $("#adminBarButtons a.adminBarButton:eq(3)").trigger("click");
		return false;
            }
            if(e.which === 53 && AdminBar.isCtrlKeyPressed && !AdminBar.isShiftKeyPressed && !AdminBar.isAltKeyPressed) { // CTRL+5
                AdminBar.showAdminBar();
                $("#adminBarButtons a.adminBarButton:eq(4)").trigger("click");
		return false;
            }
            if(e.which === 54 && AdminBar.isCtrlKeyPressed && !AdminBar.isShiftKeyPressed && !AdminBar.isAltKeyPressed) { // CTRL+6
                AdminBar.showAdminBar();
                $("#adminBarButtons a.adminBarButton:eq(5)").trigger("click");
		return false;
            }
            if(e.which === 55 && AdminBar.isCtrlKeyPressed && !AdminBar.isShiftKeyPressed && !AdminBar.isAltKeyPressed) { // CTRL+7
                AdminBar.showAdminBar();
                $("#adminBarButtons a.adminBarButton:eq(6)").trigger("click");
		return false;
            }
        });        
        $("#adminControl").on('click', function() {
            if (AdminBar.isAdminBarOpen()) {
                AdminBar.hideAdminBar();
            } else {
                AdminBar.showAdminBar();
            }
            return false;
        });
        $("#adminBarButtons a").on('click', function() {
            $("#adminBarButtons a").removeClass("current");
            $(this).addClass("current");
        });
        if (window === parent) {
            $("#adminControl").css("display", "block");
        }
    },
    showAdminBar: function() {
        $("body, html").addClass("adminBarShown");
        $("#adminBar").removeClass("adminBarInactive");
        $("#adminBar").addClass("adminBarActive");
        $("#adminControl").addClass("active");
        $("#adminControl").find("i").attr("class", "fas fa-angle-double-right");
        var path = AdminBar.cookiePath;
        $.cookie("adminBarModeHide", "false", {
            path: path
        });
        AdminBar.initQuickEditMode();
    },
    hideAdminBar: function() {
        $("body, html").removeClass("adminBarShown");
        $("#adminBar").removeClass("adminBarActive");
        $("#adminBar").addClass("adminBarInactive");
        $("#adminControl").removeClass("active");
        $("#adminControl").find("i").attr("class", "fas fa-pencil-alt");
        var path = AdminBar.cookiePath;
        $.cookie("adminBarModeHide", "true", {
            path: path
        });
        AdminBar.hideQuickOverlay();
        AdminBar.hideQuickEdit();
    },
    isAdminBarOpen: function() {
        return ($("#adminBar").hasClass("adminBarActive"));
    }
};
$(window).on("load", function() {
    if (window === parent) {
        setTimeout(function () {
            AdminBar.initAdminBar();
        }, 0);
    } else {
        AdminBar.initQuickEditMode();
        
        var environmentName = "";
        if ($("#adminBar #environmentName").length > 0) {
            environmentName = $("#adminBar #environmentName").text();
        }
        var parentAdminBar = $("#adminBar", parent.document);
        if (environmentName !== "") {
            if ($(parentAdminBar).find("#environmentName").length === 0) {
                $(parentAdminBar).append('<span id="environmentName">'+environmentName+'</span>');
            } else {
                $(parentAdminBar).find("#environmentName").text(environmentName);
            }
        } else {
            $(parentAdminBar).find("#environmentName").remove();
        }
    }
});

