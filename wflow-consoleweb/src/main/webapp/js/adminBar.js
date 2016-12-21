var AdminBar = {
    cookiePath: "/",
    currentPageTitle: "",
    webConsole: false,
    builderMode: false,
    setCookiePath: function(path) {
        AdminBar.cookiePath = path;
    },
    showQuickOverlay: function(url) {
        url = UrlUtil.updateUrlParam(url, "_ov", (new Date().getTime()));
        
        var $quickOverlayFrame = $(parent.document).find("#quickOverlayFrame");
        if ($quickOverlayFrame.length === 0) {
            var overlayContainer = 
                '<div id="quickOverlayContainer"><div id="quickOverlay"></div>\
                <div id="quickOverlayButton"><a href="#" onclick="AdminBar.hideQuickOverlay()"><i class="icon-remove"></i></a></div>\
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
        $("#quickEditModeOn").attr("checked", "checked");
        $("#quickEditModeOff").removeAttr("checked");
        $(".quickEdit, .adminBarButton").fadeIn();
        $(".analyzer-label, .analyzer-page").css("display", "inline-block");
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
        $("#adminBar").on("mouseover", AdminBar.showAdminBar);
        $("#adminBar").on("mouseout", AdminBar.hideAdminBar);
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
                } else {
                    $("#quickEditModeOff").trigger('click');
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
        });        
    },
    showAdminBar: function() {
        $("#adminBar").removeClass("adminBarInactive");
        $("#adminBar").addClass("adminBarActive");
    },
    hideAdminBar: function() {
        if (!AdminBar.webConsole || AdminBar.builderMode) {
            $("#adminBar").removeClass("adminBarActive");
            $("#adminBar").addClass("adminBarInactive");
        }
    },
    isAdminBarOpen: function() {
        return ($("#adminBar").hasClass("adminBarActive"));
    }
};
$(function () {
    AdminBar.initQuickEditMode();
    AdminBar.initAdminBar();
});

