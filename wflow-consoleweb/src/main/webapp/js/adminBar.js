var AdminBar = {
    cookiePath: "/",
    currentPageTitle: "",
    webConsole: false,
    builderMode: false,
    isDefaultUserview: false,
    setCookiePath: function(path) {
        AdminBar.cookiePath = path;
    },
    showQuickOverlay: function(url) {
        if (AdminBar.builderMode && typeof AdvancedTools !== "undefined") {
            AdvancedTools.hideQuickOverlay();
        }
        
        url = UrlUtil.updateUrlParam(url, "_ov", (new Date().getTime()));
        
        var $quickOverlayFrame = $(parent.document).find("#quickOverlayFrame");
        if ($quickOverlayFrame.length === 0) {
            var overlayContainer = 
                '<div id="quickOverlayContainer" class="quickOverlayContainer"><div id="quickOverlay" class="quickOverlay"></div>\
                <div id="quickOverlayButton" class="quickOverlayButton"><a href="#" onclick="AdminBar.hideQuickOverlay()"><i class="icon-remove"></i></a></div>\
                <iframe id="quickOverlayFrame" name="quickOverlayFrame" src="about:blank"></iframe></div>';
            $(document.body).append(overlayContainer);
            $(document.body).addClass("stop-scrolling");
            $quickOverlayFrame = $(document.body).find("#quickOverlayFrame");
        }
        $quickOverlayFrame.attr("src", "about:blank");
        $quickOverlayFrame.attr("src", url);
        $("#overlay, #quickOverlayButton, #quickOverlayFrame").fadeIn();
        $quickOverlayFrame.on("load", function() {
            AdminBar.currentPageTitle = document.title;
            document.title = $quickOverlayFrame[0].contentDocument.title;
        });
        return false;
    },
    hideQuickOverlay: function() {
        $("#overlay, #quickOverlayButton, #quickOverlayFrame").fadeOut();
        $("#quickOverlayContainer").remove();
        $(document.body).removeClass("stop-scrolling");
        document.title = AdminBar.currentPageTitle;
    },
    enableQuickEditMode: function() {
        if (AdminBar.isDefaultUserview) {
            return;
        }
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
        var quickEditModeActive =  $.cookie("quickEditModeActive");
        return quickEditModeActive === "true";
    },
    showQuickEdit: function() {
        $(".adminBarButton").fadeIn();
        $(".analyzer-page").css("display", "inline-block");
        if (AdminBar.isDefaultUserview) {
            return;
        }
        $("#quickEditModeOn").attr("checked", "checked");
        $("#quickEditModeOff").removeAttr("checked");
        $(".quickEdit").fadeIn();
        $(".analyzer-label").css("display", "inline-block");
        $(".analyzer-disabled").addClass("analyzer").removeClass("analyzer-disabled");
        $("#page").addClass("quickEditModeActive");
        $("#quickEditModeOption").buttonset("refresh");
    },
    hideQuickEdit: function() {
        $("#quickEditModeOff").attr("checked", "checked");
        $("#quickEditModeOn").removeAttr("checked");
        $(".quickEdit, .adminBarButton").css("display", "none");
        $(".analyzer-label, .analyzer-page").css("display", "none");
        $(".analyzer").addClass("analyzer-disabled").removeClass("analyzer");
        $("#page").removeClass("quickEditModeActive");
        $("#quickEditModeOption").buttonset("refresh");
    },
    initQuickEditMode: function() {
        $("#quickEditModeOption").buttonset();
        $("#adminBar #quickEditModeOption label").css("display", "block");
        var quickEditModeActive = AdminBar.isQuickEditMode();
        if (quickEditModeActive) {
            AdminBar.showQuickEdit();
            AdminBar.showAdminBar();
        } else {
            AdminBar.hideQuickEdit();
        }
    },
    initAdminBar: function() {
        $("#quickEditModeOn").on('click', AdminBar.enableQuickEditMode);
        $("#quickEditModeOff").on('click', AdminBar.disableQuickEditMode);
        $("#adminBar label.ui-button").on('click', function() {
            var input = $('#' + $(this).attr('for'));
            if(input) {
                input.trigger('click'); 
            }
            return true;
        });
        $("#adminBar #quickEditModeOption label").show();
        if (!AdminBar.webConsole || AdminBar.builderMode) {
            AdminBar.hideAdminBar();
        }
        if (AdminBar.webConsole) {
            $("#quickEditModeOption").hide();
            $(".adminBarButton").show();
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
                if ($("#quickEditModeOffLabel").hasClass("ui-state-active")) {
                    $("#quickEditModeOn").trigger('click');
                    AdminBar.showAdminBar();
                } else {
                    $("#quickEditModeOff").trigger('click');
                    AdminBar.hideAdminBar();
                    AdminBar.hideQuickOverlay();
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
        });        
        $("#adminControl").on('click', function() {
            if (AdminBar.isAdminBarOpen()) {
                AdminBar.disableQuickEditMode();
                AdminBar.hideAdminBar();
            } else {
                AdminBar.enableQuickEditMode();
                AdminBar.showAdminBar();
            }
        });
        if (window === parent) {
            $("#adminControl").fadeIn();
        }
    },
    showAdminBar: function() {
        $("#adminBar").removeClass("adminBarInactive");
        $("#adminBar").addClass("adminBarActive");
        $("#adminControl").addClass("active");
        $("#adminControl").find("i").attr("class", "icon-double-angle-right");
        AdminBar.showQuickEdit();
    },
    hideAdminBar: function() {
        $("#adminBar").removeClass("adminBarActive");
        $("#adminBar").addClass("adminBarInactive");
        $("#adminControl").removeClass("active");
        $("#adminControl").find("i").attr("class", "icon-pencil");
    },
    isAdminBarOpen: function() {
        return ($("#adminBar").hasClass("adminBarActive"));
    }
};
$(function () {
    AdminBar.initAdminBar();
    AdminBar.initQuickEditMode();
});

