$(document).ready(function() {
    const targetSpan = $("body").find(".header-nav a.btn .badge")[0];
    const observer = new MutationObserver( (mutations) => {
        mutations.forEach( (mutation) => {
            if (mutation.type === "characterData" || mutation.type === "childList") {
                if (targetSpan.textContent.trim() === "0") {
                    $(targetSpan).addClass("empty");
                } else {
                    $(targetSpan).removeClass("empty");
                }
            }
        }
        );
    }
    );
    observer.observe(targetSpan, {
        characterData: true,
        childList: true,
        subtree: true
    });
    $(window).off("load.inbox").on("load.inbox", function() {
        setTimeout(function() {
            loadInbox();
            $('img[data-lazysrc]').each(function() {
                $(this).attr('src', $(this).attr('data-lazysrc'));
            });
        }, 0);
    });
    $("body").off("page_loaded.inbox").on("page_loaded.inbox", function() {
        setTimeout(function() {
            loadInbox();
        }, 1);
    });
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
    loadInboxData = function() {
        $(".inbox-notification .loading").show();
        var url = $(".inbox-notification").data("url");
        $.getJSON(url + "&_t=" + (new Date()), {}, function(data) {
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
                    var html = "<li class=\"task\"><span class=\"header\">" + d.activityName + "<a href=\"" + link + "?_mode=assignment&activityId=" + d.activityId + "\"><i class='zmdi zmdi-arrow-right-top'></i></a></span>";
                    html += "<div class='more-info'><span class=\"time\">" + d.dateCreated + "</span>";
                    html += "<span class=\"message\">" + d.processName + "</span></div>";
                    html += "</li>";
                    var $html = $(html);
                    $html.find("span.header").off("click").on("click", function(e) {
                        $html.toggleClass("showMoreInfo");
                        if (!$(e.target).is("a, a *")) {
                            e.stopPropagation();
                        }
                    })
                    footer.before($html);
                });
            }
            $(".inbox-notification .loading").hide();
            $(".inbox-notification").off("inbox_notification_updated").on("inbox_notification_updated", function(e) {
                loadInboxData();
            });
        });
    }
    $(window).on("resize", function() {
        if ($("body").hasClass("horizontal_menu")) {
            let totalMenuWidth = 0;
            let gap = 0;
            let prevMenu = null;
            setTimeout(function() {
                $("body").find("ul#category-container > li.category").each(function() {
                    if (!$(this).hasClass("first") && prevMenu !== null) {
                        if ($("body").hasClass("rtl")) {
                            gap = $(prevMenu).offset().left - $(this).offset().left + $(this).outerWidth();
                        } else {
                            gap = $(this).offset().left - ($(prevMenu).offset().left + $(prevMenu).outerWidth());
                        }
                    }
                    totalMenuWidth += $(this).outerWidth();
                    prevMenu = $(this);
                    totalMenuWidth += gap;
                })
                var breakpoint = $(window).outerWidth();
                if ($("body").hasClass("inline_menu")) {
                    breakpoint -= ($("header.navbar .container-fluid").outerWidth(true) - $("header.navbar .container-fluid").innerWidth());
                    breakpoint -= $("a#header-link").outerWidth(true);
                    breakpoint -= $("header.navbar .header-nav").outerWidth(true);
                    breakpoint -= 20;
                } else {
                    breakpoint -= 32; //Cater for margin
                }

                $("body").find("nav button#leftNav").remove();
                $("body").find("nav button#rightNav").remove();
                $("body").removeClass("leftNavEnabled rightNavEnabled");

                if (($("body").hasClass("horizontal_menu") && $(window).outerWidth() >= 768)) {
                    $("body").addClass("navigationEnabled");
                    $("body").find("ul#category-container > li.category").each(function() {
                        $(this).off("mouseover").on("mouseover", function() {
                            var $activeMenuContainer = $("body ul.menu-container.appended")
                            if ($activeMenuContainer.length > 0 && $activeMenuContainer.data("parent-id") !== $(this).attr('id')) {
                                $activeMenuContainer.css({
                                    top: "",
                                    left: "",
                                    visibility: "",
                                    right: ""
                                }).removeClass("mouseover appended");
                                $("body").find("ul#category-container > li.category#" + $activeMenuContainer.data("parent-id")).append($activeMenuContainer);
                            }
                            let $menuContainer = $(this).find("ul.menu-container");
                            if (!$menuContainer.hasClass("appended")) {
                                $menuContainer.css({
                                    "opacity": 0,
                                    "pointer-events": "none"
                                })
                                $menuContainer.appendTo("body").addClass("appended");
                            }
                            $menuContainer.css({
                                top: ($(this).offset().top + $(this).height()) + 'px',
                                ...(($(this).nextAll("li.category").length === 0 && !$("body").hasClass("inline_menu") && ($(this).offset().left + 200 > $(window).outerWidth())) ? {
                                    right: '0px'
                                } : {
                                    left: $(this).offset().left + 'px'
                                })
                            });
                            setTimeout( () => {
                                $menuContainer.css({
                                    "opacity": 1,
                                    "pointer-events": "auto",
                                    "transition": "opacity 0.15s ease-in-out"
                                });
                            }
                            , 10);
                            $menuContainer.addClass("mouseover").data("parent-id", $(this).attr("id"));
                            $menuContainer.off("mouseleave").on("mouseleave", function(e) {
                                if ($(e.relatedTarget).closest("li.category").length > 0)
                                    return;
                                const id = $(this).data("parent-id");
                                $(this).css({
                                    top: "",
                                    left: "",
                                    visibility: "",
                                    right: ""
                                }).removeClass("mouseover appended");
                                $("body").find("ul#category-container > li.category#" + id).append($(this));
                            });
                        }).off("mouseleave").on("mouseleave", function(e) {
                            if ($(e.relatedTarget).closest("ul.menu-container").length > 0)
                                return;
                            const id = $("body").find("ul.menu-container.mouseover").data("parent-id");
                            const $menu = $("body").find("ul.menu-container.mouseover");
                            if ($menu.length) {
                                $menu.css({
                                    top: "",
                                    left: "",
                                    visibility: "",
                                    right: ""
                                }).removeClass("mouseover appended");
                                $("body").find("ul#category-container > li.category#" + id).append($menu);
                            }
                        });
                    });
                    var $hoveredCategory = $("ul#category-container > li.category:hover");
                    if ($hoveredCategory.length > 0) {
                        $hoveredCategory.trigger("mouseover")
                    }
                }else {
                    $("body").removeClass("navigationEnabled");
                    $("body").find("ul#category-container > li.category").each(function() {
                        $(this).off('mouseover');
                        $(this).find("ul.menu-container").off('mouseover');
                    })
                }
                if (totalMenuWidth >= breakpoint && ($("body").hasClass("horizontal_menu") && $(window).outerWidth() >= 768)) {
                    if ($("body").hasClass("rtl")) {
                        $("body").addClass("leftNavEnabled");
                    } else {
                        $("body").addClass("rightNavEnabled");
                    }
                    var $leftButton = $("<button id='leftNav' class='horizontalNavButton'></button>");
                    var $rightButton = $("<button id='rightNav' class='horizontalNavButton'></button>");
                    function updateButtons() {
                        let $container = $("#category-container");
                        let maxScroll = $container[0].scrollWidth - $container.outerWidth();
                        let scrollLeft = $("body").hasClass("rtl") ? Math.abs($container.scrollLeft()) : $container.scrollLeft();
                        const EPSILON = 1; 

                        if (scrollLeft <= EPSILON) {
                            $("body:not(.rtl)").addClass("rightNavEnabled").removeClass("leftNavEnabled");
                            $("body.rtl").removeClass("rightNavEnabled").addClass("leftNavEnabled");
                        } else if (Math.abs(scrollLeft - maxScroll) <= EPSILON) {
                            $("body:not(.rtl)").removeClass("rightNavEnabled").addClass("leftNavEnabled");
                            $("body.rtl").addClass("rightNavEnabled").removeClass("leftNavEnabled");
                        } else {
                            $("body").addClass("rightNavEnabled").addClass("leftNavEnabled");
                        }
                    }
                    $leftButton.on("click", function(e) {
                        e.stopPropagation();
                        let $category = $("#category-container");
                        let category = $category[0];
                        let currentValue = getComputedStyle(category).getPropertyValue('--translate-move-x').trim();
                        if (!currentValue) {
                            currentValue = "0px";
                        }
                        let newValue = Math.max(0, parseInt(currentValue) - 100) + "px";
                        category.style.setProperty('--translate-move-x', newValue);
                        $category.animate({
                            scrollLeft: "-=100"
                        }, 300, updateButtons);
                    });
                    $rightButton.on("click", function(e) {
                        e.stopPropagation();
                        let $category = $("#category-container");
                        let category = $category[0];
                        let scrollWidth = category.scrollWidth;
                        let containerWidth = category.offsetWidth;
                        let maxTranslate = scrollWidth - containerWidth;
                        let currentValue = getComputedStyle(category).getPropertyValue('--translate-move-x').trim();
                        if (!currentValue) {
                            currentValue = "0px";
                        }
                        let newValue = Math.min(maxTranslate, parseInt(currentValue) + 100) + "px";
                        category.style.setProperty('--translate-move-x', newValue);
                        $category.animate({
                            scrollLeft: "+=100"
                        }, 300, updateButtons);
                    });
                    $("body").find("ul#category-container").before($leftButton);
                    $("body").find("ul#category-container").after($rightButton);
                    updateButtons();
                }else {
                    //Resets translate
                    $("#category-container")[0].style.setProperty('--translate-move-x', "");
                }
            }, 150);
        }

        var $navigationButtons = $('<div class="navigation-arrows-container"><div class="left-navigation-arrow"></div><div class="right-navigation-arrow"></div></div>');
        if ($(".form-element.multiPagedForm > .page-nav-panel.top .navigation-arrows-container").length === 0) {
            $(".form-element.multiPagedForm > .page-nav-panel.top > ul").after($navigationButtons);
        } 


        function checkScrollPosition($ul) {
            let scrollLeft = $ul.scrollLeft();
            let maxScrollLeft = $ul[0].scrollWidth - $ul[0].clientWidth;
            if (scrollLeft <= 0) {
                $ul.parent().find(".navigation-arrows-container > .left-navigation-arrow").addClass("disabled");
            } else {
                $ul.parent().find(".navigation-arrows-container > .left-navigation-arrow").removeClass("disabled");
            }
            if (Math.round(scrollLeft) >= Math.round(maxScrollLeft)) {
                $ul.parent().find(".navigation-arrows-container > .right-navigation-arrow").addClass("disabled");
            } else {
                $ul.parent().find(".navigation-arrows-container > .right-navigation-arrow").removeClass("disabled");
            }
        }
        $navigationButtons.find("div.left-navigation-arrow").on("click", function() {
            let $ul = $(this).parent().prev("ul");
            $ul.scrollLeft($ul.scrollLeft() - 100);
            setTimeout(function() {
                checkScrollPosition($ul);
            }, 300);
        });
        $navigationButtons.find("div.right-navigation-arrow").on("click", function() {
            let $ul = $(this).parent().prev("ul");
            $ul.scrollLeft($ul.scrollLeft() + 100);
            setTimeout(function() {
                checkScrollPosition($ul);
            }, 300);
        });
        $(".form-element.multiPagedForm > .page-nav-panel.top > ul").each(function() {
            checkScrollPosition($(this));

            var totalWidth = 0;
            $(this).find("li").each(function(){
                totalWidth += $(this).width();
            })

            if (totalWidth <= $(this).parent().width()) {
                $(this).addClass("hideNavArrows")
            } else {
                $(this).removeClass("hideNavArrows")
            }
        });
    })
    $(window).on("page_loaded", function() {
        if ($("a.print-button").length > 0) {
            $("a.print-button").each(function() {
                $(this).appendTo($(this).closest("div.Form_Menu").find("div.viewForm-body-content div#section-actions"));
            })
        }
        $(".dataList .filters select").on("change", function() {
            $(this).closest("div.filters").find("input.form-button[type='submit'][value='Show']").click();
        })
    })
    $(window).resize(function() {
        if (($("body").hasClass("horizontal_menu") && $(window).outerWidth() < 768) || !$("body").hasClass("horizontal_menu")) {
            $("#sidebar nav").mCustomScrollbar({
                autoHideScrollbar: true,
                scrollInertia: 300,
                scrollbarPosition: "inside",
                theme: "minimal-dark"
            });
        } else {
            $("#sidebar nav").mCustomScrollbar("destroy");
        }
    });
})
AjaxUniversalTheme.initSidebar = function() {
    if ($("#sidebar").length > 0) {
        var sidebar = function() {
            if ($("#sidebar").css("display") === "inline-block") {
                if ($("#sidebar #navigation").hasClass("mCustomScrollbar")) {
                    $("#sidebar #navigation").mCustomScrollbar("destroy");
                }
            } else {
                AjaxUniversalTheme.scrollBar("#sidebar #navigation", "y");
            }
        };
        sidebar();
        $(window).resize(function() {
            sidebar();
        });
    }
}
;
