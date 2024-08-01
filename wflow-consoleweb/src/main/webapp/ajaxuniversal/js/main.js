if ((typeof _customFooTableArgs) === "undefined") {
    _customFooTableArgs = {
        breakpoints: { // The different screen resolution breakpoints
            phone: 480,
            tablet: 767
        }
    };
}

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
    $('body').removeClass("smallFontSize mediumFontSize largeFontSize");
    $('body').addClass(fontClass);
}

/*
 * Call when page loaded or page switched in AJAX theme
 */
function themePageInit() {
    checkCookie();
    $('#smallFont').click(function () {
        setCookie("13");
        setFontSize("13");
    });

    $('#mediumFont').click(function () {
        setFontSize("17");
        setCookie("17");
    });

    $('#bigFont').click(function () {
        setFontSize("20");
        setCookie("20");
    });

    $(".rowCount").each(function() {
        var count = $(this).text().replace("(", "").replace(")", "");
        $(this).text(count);
        $(this).addClass("pull-right badge");
    });
    
    //remove pagination if only 1 page
    if ($(".dataList .pagelinks a").length === 0) {
        $(".dataList .pagelinks").css("visibility", "hidden");
    }

    $(".filter-cell select").on("change", function() {
        if ($(this).val() === "") {
            $(this).addClass("emptyValue");
        } else {
            $(this).removeClass("emptyValue");
        }
    });
    $(".filter-cell select").each(function() {
        if ($(this).val() === "") {
            $(this).addClass("emptyValue");
        } else {
            $(this).removeClass("emptyValue");
        }
    });
}

! function($) {
    var bsButton = $.fn.button.noConflict(); // reverts $.fn.button to jqueryui btn
    $.fn.bsButton = bsButton;

    //override event to use new method name
    $(document).off('click.button.data-api', '[data-toggle^=button]');
    $(document).on('click.button.data-api', '[data-toggle^=button]', function(e) {
        var $btn = $(e.target)
        if (!$btn.hasClass('btn')) $btn = $btn.closest('.btn')
        $btn.bsButton('toggle')
    })

    $(document).ready(function() {
        themePageInit();

        $("body").swipe({
            swipeRight: function(event, direction, distance, duration, fingerCount, fingerData) {
                if (!$("body").hasClass("rtl")) {
                    var posx = fingerData[0]['start']['x'];
                    if ($(".hi-trigger").is(":visible") && !$("body").hasClass("sidebar-toggled") && posx < 20) {
                        $(".hi-trigger").trigger("click");
                    }
                } else {
                    if ($(".ma-backdrop").is(":visible") && $("body").hasClass("sidebar-toggled")) {
                        $(".ma-backdrop").trigger("click");
                    }
                }
            },
            swipeLeft: function(event, direction, distance, duration, fingerCount, fingerData) {
                if (!$("body").hasClass("rtl")) {
                    if ($(".ma-backdrop").is(":visible") && $("body").hasClass("sidebar-toggled")) {
                        $(".ma-backdrop").trigger("click");
                    }
                } else {
                    var posx = fingerData[0]['start']['x'];
                    if ($(".hi-trigger").is(":visible") && !$("body").hasClass("sidebar-toggled") && posx > $(window).width() - 20) {
                        $(".hi-trigger").trigger("click");
                    }
                }
            },
            preventDefaultEvents: false,
            fallbackToMouseEvents: false,
        });
        
        //to keep track is keypress focus, to prevent click and focus trigger together
        var isTabPress = false;
        $(document).on("keydown", function(e){
            var charCode = e.which || e.keyCode;
            if (charCode === 9) {
                isTabPress = true;
            }
        }).on("keyup", function(e){
            var charCode = e.which || e.keyCode;
            if (charCode === 9) {
                isTabPress = false;
            }
        });

        //for bootstrap dropdown
        $("body").on("focus", "a.dropdown-toggle", function(e){
            if (isTabPress && !$(this).parent().hasClass("show")) {
                $(".dropdown.open").removeClass("show");
                $(this).trigger("click");
            }
        });

        var toogleMenu = function(menu, preventPageJumping) {
            if ($(menu).parent().css("display") !== "none") {
                //to prevent page jumpling
                if (preventPageJumping) {
                    var top = $("#mCSB_1_container").offset().top;
                    if (top < 0) {
                        $("#mCSB_1_container").css("top", top + "px");
                    }
                    $(menu).next().slideToggle(200, function(){
                        if (top < 0) {
                            setTimeout(function(){
                                $("#mCSB_1_container").css("top", top + "px");
                            }, 100);
                        }
                    });
                } else {
                    $(menu).next().slideToggle(200);
                }
                $(menu).parent().toggleClass("toggled");
            }
        };

        var initMenu = function() {
            $("#sidebar a.dropdown").each(function() {
                if ($(this).parent().hasClass("active")) {
                    toogleMenu(this);
                }
            });

            $("#sidebar").on("click", "a.dropdown", function(e) {
                toogleMenu(this);
                e.preventDefault();
                e.stopPropagation();
            });
            $("#sidebar").on("focus", "a.dropdown", function(e) {
                $(".focusVisible").removeClass("focusVisible");
                if (isTabPress) {
                    var el = this;
                    if($(el).closest("#sidebar").length > 0) {
                        if (!$("body").hasClass("horizontal_menu") && $("body").hasClass("sidebar-minimized")) {
                            $("#sidebar-trigger").trigger("click");
                        }
                        if ($("body").hasClass("horizontal_menu")) {
                            if ($(el).next().is(":hidden")) {
                                $(el).next().addClass("focusVisible");
                            }
                        } else {
                            if (!$(el).parent().hasClass("toggled")) {
                                toogleMenu(el, true);
                            }
                        }
                    }
                }
            });
        };
        initMenu();
        
        //open menu
        $("#sidebar-trigger").on("click", function() {
            if (window.innerWidth >= 1280) {
                $("body").toggleClass("sidebar-minimized");
            } else {
                if (!$("body").hasClass("sidebar-toggled")) {
                    if ($(".ma-backdrop").length === 0){
                        var backdrop = '<div class="ma-backdrop" />';
                        $("body").prepend(backdrop);
                    }
                    $("body").addClass("sidebar-toggled");
                    $(this).addClass("toggled");
                    $("#sidebar").addClass("toggled");
                } else {
                    $("body").removeClass("sidebar-toggled");
                    $(".ma-backdrop").remove();
                    $("#sidebar, #sidebar-trigger").removeClass("toggled");
                }
            }
            return false;
        });
        
        $("body").off("click.sidebar-toggled");
        $("body").on("click.sidebar-toggled", ".ma-backdrop", function(e) {
            var container = $("#sidebar");
            if (($(".ma-backdrop").is(":visible") && $("body").hasClass("sidebar-toggled") && !$("#sidebar-trigger").is(e.target) && $("#sidebar-trigger").has(e.target).length === 0 && !container.is(e.target) && container.has(e.target).length === 0)) {
                $("body").removeClass("sidebar-toggled");
                $(".ma-backdrop").remove();
                $("#sidebar, #sidebar-trigger").removeClass("toggled");
            }
            return false;
        });

        if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
            $("html").addClass("ismobile");
        }

        // hide responsive switch if within IFRAME
        if (window.self !== window.top) {
            $("#responsiveSwitch").hide();
        }
    });

    $(window).on("load", function() {
        setTimeout(function() {
            loadInbox();
            
            $('img[data-lazysrc]').each(function () {
                $(this).attr('src', $(this).attr('data-lazysrc'));
            });
        }, 0);
    });
    $("body").on("page_loaded.inbox", function(){
        setTimeout(function() {
            loadInbox();
        }, 1);
    });

    /* ---------- Inbox ------------------------- */
    function loadInbox() {
        if ($(".inbox-notification").length === 1) {
            loadInboxData();
            $(".inbox-notification .refresh").on("click", function(e) {
                e.preventDefault();
                loadInboxData();
                return false;
            });
        }
    }
    
    let inboxReloadTimeout = null;
    function loadInboxData() {
        $(".inbox-notification .loading").show();
        var url = $(".inbox-notification").data("url");
        $.getJSON(url + "&_t=" + (new Date()), {},
            function(data) {
                var count = 0;
                if (data.count !== undefined) {
                    count = data.count;
                }
                $(".inbox-notification > a > .badge").text(count);
                $(".inbox-notification .dropdown-menu-title .count").text(count);

                $(".inbox-notification > ul > li.task").remove();
                if (data.data) {
                    var footer = $(".inbox-notification > ul .dropdown-menu-sub-footer").parent();
                    var link = $(".inbox-notification > ul .dropdown-menu-sub-footer").attr("href");
                    $.each(data.data, function(i, d) {
                        var html = "<li class=\"task\"><a href=\"" + link + "?_mode=assignment&activityId=" + d.activityId + "\">";
                        html += "<span class=\"header\">" + d.activityName + "</span>";
                        html += "<span class=\"message\">" + d.processName + "</span><span class=\"time\">" + d.dateCreated + "</span>";
                        html += "</a></li>";
                        footer.before($(html));
                    });
                    $(".inbox-notification").trigger("inbox_notification_updated");
                }

                $(".inbox-notification .loading").hide();
                
                //reload every 5mins
                clearTimeout(inboxReloadTimeout);
                inboxReloadTimeout = setTimeout(function(){
                    loadInboxData();
                }, 300000);
            }
        );
    }

}(window.jQuery);