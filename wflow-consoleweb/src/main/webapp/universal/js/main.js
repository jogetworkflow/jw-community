if ((typeof _customFooTableArgs) === "undefined") {
    _customFooTableArgs = {
        breakpoints: { // The different screen resolution breakpoints
            phone: 480,
            tablet: 767
        }
    };
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
        //fix tinymce position
        if ($(".tinymce").length > 0) {
            function overrideTinymce() {
                if (window["tinymce"] !== undefined) {
                    tinymce.Env.container = document.getElementById("form-canvas").offsetParent;
                } else {
                    setTimeout(function(){
                        overrideTinymce();
                    }, 100);
                }
            }
            overrideTinymce();
        }
        
        $(".rowCount").each(function() {
            var count = $(this).text().replace("(", "").replace(")", "");
            $(this).text(count);
            $(this).addClass("pull-right badge");
        });

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

        var toogleMenu = function(menu) {
            if ($(menu).parent().css("display") !== "none") {
                $(menu).next().slideToggle(200);
                $(menu).parent().toggleClass("toggled");
            }
        };
        var initMenu = function() {
            $("#sidebar a.dropdown").each(function() {
                if ($(this).parent().hasClass("active")) {
                    toogleMenu(this);
                }
            });

            $("#sidebar a.dropdown").on("click", function(e) {
                toogleMenu(this);
                e.preventDefault();
                e.stopPropagation();
            });
        };
        initMenu();

        //open menu
        var originalHash = '';
        $("#sidebar-trigger").on("click", function() {
            originalHash = location.hash.replace('#', '');

            //close menu on back
            $(window).bind('hashchange.menu', function(event) {
                var hash = location.hash.replace('#', '');

                if (hash !== 'menu' && $(".ma-backdrop").is(":visible") && $("body").hasClass("sidebar-toggled")) {
                    $(".ma-backdrop").trigger("click");
                }
            });

            //close menu on touch any place other than menu
            $("body").on("click.sidebar-toggled", function(e) {
                var container = $("#sidebar");

                if ($(".ma-backdrop").is(":visible") && $("body").hasClass("sidebar-toggled") &&
                    !$("#sidebar-trigger").is(e.target) && $("#sidebar-trigger").has(e.target).length === 0 &&
                    !container.is(e.target) && container.has(e.target).length === 0) {
                    $("body").removeClass("sidebar-toggled");
                    $(".ma-backdrop").remove();
                    $("#sidebar, #sidebar-trigger").removeClass("toggled");
                    $("body").off(".sidebar-toggled");
                    $(window).unbind('hashchange.menu');
                    location.hash = originalHash;
                    return false;
                }
            });

            var backdrop = '<div class="ma-backdrop" />';
            $("body").addClass("sidebar-toggled");
            $("header.navbar").append(backdrop);
            $(this).addClass("toggled");
            $("#sidebar").addClass("toggled");
            location.hash = 'menu';
        });

        if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
            $("html").addClass("ismobile");
        }

        Waves.init();
        attachButtonEffect();

        var scrollBar = function(selector, theme, mousewheelaxis) {
            $(selector).mCustomScrollbar({
                theme: theme,
                scrollInertia: 100,
                axis: "mousewheelaxis",
                mouseWheel: {
                    enable: !0,
                    axis: mousewheelaxis,
                    preventDefault: !0
                }
            });
        };
        if ($("#sidebar").length > 0) {
            scrollBar("#sidebar", "minimal-dark", "y");
        }
        if ($(".c-overflow").length > 0) {
            scrollBar(".c-overflow", "minimal-dark", "y");
        }

        //add button effect to responsive table
        $(".dataList table").on("footable_row_detail_updated", function(event) {
            attachButtonEffect();
        });

        //remove pagination if only 1 page
        if ($(".dataList .pagelinks a").length === 0) {
            $(".dataList .pagelinks").hide();
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

        initTextareaAutoHeight();

        // hide responsive switch if within IFRAME
        if (window.self !== window.top) {
            $("#responsiveSwitch").hide();
        }
    });

    $(window).on("load", function() {
        setTimeout(function() {
            loadInbox();
        }, 0);
    });

    function onFormChange() {
        $(document).on("change", "form *", function() {
            attachButtonEffect();
            initTextareaAutoHeight();
        });
    }

    function initTextareaAutoHeight() {
        $("textarea").each(function() {
            textareaAutoHeight(this);
        });
        $(document).off("input keydown keyup", "textarea");
        $(document).on("input keydown keyup", "textarea", function() {
            textareaAutoHeight(this);
        });
    }

    function textareaAutoHeight(e) {
        var scrollLeft = window.pageXOffset ||
            (document.documentElement || document.body.parentNode || document.body).scrollLeft;

        var scrollTop = window.pageYOffset ||
            (document.documentElement || document.body.parentNode || document.body).scrollTop;

        var rows = $(e).attr("rows");
        var rowHeight = 22;
        $(e).attr("rows", 1).css({ 'height': 'auto', 'overflow-y': 'hidden' });
        var minTextAreaHeight = (rows * rowHeight);
        var newHeight = e.scrollHeight + (e.scrollHeight > 30 ? 6 : 0);
        if (newHeight < minTextAreaHeight) {
            newHeight = minTextAreaHeight;
        }
        $(e).attr("rows", rows).css({ 'height': 'auto', 'overflow-y': 'hidden' }).height(newHeight);

        window.scrollTo(scrollLeft, scrollTop);
    }

    function attachButtonEffect() {
        setTimeout(function() {
            Waves.attach('.btn:not(.waves-button), .form-button:not(.waves-button), button:not(.waves-button), input[type=button]:not(.waves-button), input[type=reset]:not(.waves-button), input[type=submit]:not(.waves-button)', ['btn', 'waves-button', 'waves-float']);
        }, 0);
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
        }
    }

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
                }

                $(".inbox-notification .loading").hide();
            }
        );
    }

}(window.jQuery);