var NavMenu = {
    cookiePath: "/",
    setCookiePath: function(path) {
        NavMenu.cookiePath = path;
    },
    showQuickOverlay: function (url, overlay) {
        if (overlay !== 'true') {
            return true;
        }
        if ($("#quickOverlayContainer").length === 0) {
            var overlayContainer =
                    '<div id="quickOverlayContainer"><div id="quickOverlay"></div>\
                        <div id="quickOverlayButton"><a href="#" onclick="NavMenu.hideQuickOverlay()"><i class="icon-remove"></i></a></div>\
                        <iframe id="quickOverlayFrame" name="quickOverlayFrame" src="about:blank"></iframe></div>';
            $(document.body).append(overlayContainer);
            $(document.body).addClass("stop-scrolling");
        }
        $("#quickOverlayFrame").attr("src", "about:blank");
        $("#quickOverlayFrame").attr("src", url);
        $("#overlay, #quickOverlayButton, #quickOverlayFrame").fadeIn();
        NavMenu.hidePopupMenu();
        return false;
    },
    hideQuickOverlay: function () {
        $("#overlay, #quickOverlayButton, #quickOverlayFrame").fadeOut();
        $("#quickOverlayContainer").remove();
        $(document.body).removeClass("stop-scrolling");
    },
    enableQuickEditMode: function () {
        var path = NavMenu.cookiePath;
        $.cookie("quickEditModeActive", "true", {
            path: path
        });
        NavMenu.initQuickEditMode();
    },
    disableQuickEditMode: function () {
        var path = NavMenu.cookiePath;
        $.cookie("quickEditModeActive", "false", {
            path: path
        });
        NavMenu.initQuickEditMode();
    },
    showQuickEdit: function () {
        $("#quickEditModeOn").attr("checked", "checked");
        $("#quickEditModeOff").removeAttr("checked");
        $(".quickEdit, .menu-link-admin").fadeIn();
        $("#page").addClass("quickEditModeActive");
        $("#quickEditModeOption").buttonset("refresh");
    },
    hideQuickEdit: function () {
        $("#quickEditModeOff").attr("checked", "checked");
        $("#quickEditModeOn").removeAttr("checked");
        $(".quickEdit, .menu-link-admin").css("display", "none");
        $("#page").removeClass("quickEditModeActive");
        $("#quickEditModeOption").buttonset("refresh");
    },
    initQuickEditMode: function () {
        $("#quickEditModeOption").buttonset();
        $("#menu-popup #quickEditModeOption label").css("display", "block");
        var quickEditModeActive = $.cookie("quickEditModeActive");
        if (quickEditModeActive === "true") {
            NavMenu.showQuickEdit();
        } else {
            NavMenu.hideQuickEdit();
        }
    },
    initPopupMenu: function() {
        $("#quickEditModeOn").on('click', NavMenu.enableQuickEditMode);
        $("#quickEditModeOff").on('click', NavMenu.disableQuickEditMode);
        $("#menu-popup label.ui-button").on('click', function() {
            var input = $('#' + $(this).attr('for'));
            if (input) {
                input.trigger('click');
            }
            return true;
        });
        $("#menu-button").on('click', function() {
            if (NavMenu.isPopupMenuOpen()) {
                NavMenu.hidePopupMenu();
            } else {
                NavMenu.showPopupMenu();
            }
        });
        $("#main-menu #menu-popup").mouseleave(function() {
            NavMenu.hidePopupMenu();
        });
    },
    showPopupMenu: function() {
        $("#menu-popup").addClass("menu-display");
    },
    hidePopupMenu: function() {
        $("#menu-popup").removeClass("menu-display");
    },
    isPopupMenuOpen: function() {
        return ($("#menu-popup").hasClass("menu-display"));
    }
};
$(function () {
    NavMenu.initQuickEditMode();
    NavMenu.initPopupMenu();
});
