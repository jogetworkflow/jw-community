$(document).ready(function(){
    function activateScrollbar(container, destroy) {

        if (destroy) {
            $(container).mCustomScrollbar('destroy');
            return;
        }
        $(container).mCustomScrollbar({
            autoHideScrollbar : true,
            scrollInertia: 300,
            scrollbarPosition: "inside",
            theme: "minimal-dark",
            callbacks:{
                onCreate: function(){
                    const contentHeight = $(container).find(".mCSB_container").outerHeight();
                    const scrollHeight = $(container).find(".mCSB_scrollTools").height();

                    const threshold = 1;
                    
                    if (Math.abs(contentHeight - scrollHeight) <= threshold) {
                        setTimeout(function () {
                            $(container).find(".mCSB_scrollTools").hide();
                        }, 100);
                    }
                }
            }
        });
        $(container).mCustomScrollbar("update");
    }

    //Updates the notification bell
    const targetSpan = $("body").find(".header-nav a.btn .badge")[0]; 

    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === "characterData" || mutation.type === "childList") {
                if (targetSpan.textContent.trim() === "0") {
                    $(targetSpan).addClass("empty");
                }else {
                    $(targetSpan).removeClass("empty");
                }
            }
        });
    });

    observer.observe(targetSpan, { characterData: true, childList: true, subtree: true });

    // Inbox
    $(window).off("load.inbox").on("load.inbox", function() {
        setTimeout(function() {
            loadInbox();
            
            $('img[data-lazysrc]').each(function () {
                $(this).attr('src', $(this).attr('data-lazysrc'));
            });
        }, 0);
    });

    $("body").off("page_loaded.inbox").on("page_loaded.inbox", function(){
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
                        var html = "<li class=\"task\"><span class=\"header\">" + d.activityName + "<a href=\"" + link + "?_mode=assignment&activityId=" + d.activityId + "\"><i class='zmdi zmdi-arrow-right-top'></i></a></span>";
                        html += "<div class='more-info'><span class=\"time\">" + d.dateCreated + "</span>";
                        html += "<span class=\"message\">" + d.processName + "</span></div>";
                        html += "</li>";

                        var $html = $(html);
                        $html.find("span.header").off("click").on("click", function(e){
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
            }
        );
    }

    // Horizontal
    $(window).on("resize", function(){
        if($("body").hasClass("horizontal_menu")) {
            let totalMenuWidth = 0;
            let gap = 0;
            let prevMenu = null;
            setTimeout(function(){
                $("body").find("ul#category-container > li.category").each(function(){
                    if (!$(this).hasClass("first") && prevMenu !== null) {
                        if ($("body").hasClass("rtl")){
                            gap = ($(prevMenu).offset().left + $(prevMenu).outerWidth()) - $(this).offset().left;
                        }else {
                            gap = $(this).offset().left - ($(prevMenu).offset().left + $(prevMenu).outerWidth());
                        }
                    }
                    totalMenuWidth += $(this).outerWidth();
                    prevMenu = $(this);
                    totalMenuWidth += gap;
                })

                var breakpoint = $(window).width();
                if ($("body").hasClass("inline_menu")) {
                    breakpoint -= ($("header.navbar .container-fluid").outerWidth(true) - $("header.navbar .container-fluid").innerWidth());
                    breakpoint -= $("a#header-link").outerWidth(true);
                    breakpoint -= $("header.navbar .header-nav").outerWidth(true);
                    breakpoint -= 20;
                }

                if (totalMenuWidth >= breakpoint || ($("body").hasClass("horizontal_menu") && !$("body").hasClass("inline_menu") && $(window).width() > 768)){
                    $("body").addClass("navigationEnabled");

                    if ($("body").hasClass("rtl")) {
                        $("body").addClass("leftNavEnabled");
                    } else {
                        $("body").addClass("rightNavEnabled");
                    }

                    $("body").find("nav button#leftNav").remove();
                    $("body").find("nav button#rightNav").remove();      

                    $("body").find("ul#category-container > li.category").each(function () {
                        $(this).off("mouseover").on("mouseover", function () {
                            if ($("body").hasClass("navigationEnabled")) {
                                let $menuContainer = $(this).find("ul.menu-container");
                        
                                // Prevent re-adding
                                if (!$menuContainer.hasClass("appended")) {
                                    $menuContainer.css({"opacity": 0, "pointer-events": "none"})
                                    $menuContainer.appendTo("body").addClass("appended");
                                }
                        
                                // Update position
                                $menuContainer.css({
                                    top: ($(this).offset().top + $(this).height()) + 'px',
                                    ...( ($(this).nextAll("li.category").length === 0 && !$("body").hasClass("inline_menu") && ($(this).offset().left + 200 > $(window).width()))
                                        ? { right: '0px' }  
                                        : { left: $(this).offset().left + 'px' } 
                                    )
                                });
                        
                                // Show after reflow
                                setTimeout(() => {
                                    $menuContainer.css({"opacity": 1, "pointer-events": "auto", "transition": "opacity 0.15s ease-in-out"});
                                }, 10);
                        
                                $menuContainer.addClass("mouseover").data("parent-id", $(this).attr("id"));
                        
                                // Handle menu mouseleave
                                $menuContainer.off("mouseleave").on("mouseleave", function (e) {
                                    if ($(e.relatedTarget).closest("li.category").length > 0) return;
                                    const id = $(this).data("parent-id");
                        
                                    $(this).css({ top: "", left: "", visibility: "", right: "" }).removeClass("mouseover appended");
                                    $("body").find("ul#category-container > li.category#" + id).append($(this));
                                });
                            }
                        }).off("mouseleave").on("mouseleave", function (e) {
                            if ($(e.relatedTarget).closest("ul.menu-container").length > 0) return;
                            
                            const id = $("body").find("ul.menu-container.mouseover").data("parent-id");
                            const $menu = $("body").find("ul.menu-container.mouseover");
                    
                            if ($menu.length) {
                                $menu.css({ top: "", left: "", visibility: "", right: "" }).removeClass("mouseover appended");
                                $("body").find("ul#category-container > li.category#" + id).append($menu);
                            }
                        });
                    });                  

                    // Left and Right Button
                    var $leftButton = $("<button id='leftNav' class='horizontalNavButton'></button>");
                    var $rightButton = $("<button id='rightNav' class='horizontalNavButton'></button>");

                    function updateButtons() {
                        let $container = $("#category-container");
                        let maxScroll = $container[0].scrollWidth - $container.outerWidth();
                        let scrollLeft = $("body").hasClass("rtl") ? Math.abs($container.scrollLeft()) : $container.scrollLeft;
                
                        if (scrollLeft <= 0) {
                            $("body:not(.rtl)").addClass("rightNavEnabled").removeClass("leftNavEnabled");
                            $("body.rtl").removeClass("rightNavEnabled").addClass("leftNavEnabled");
                        } else if (scrollLeft >= maxScroll) {
                            $("body:not(.rtl)").removeClass("rightNavEnabled").addClass("leftNavEnabled");
                            $("body.rtl").addClass("rightNavEnabled").removeClass("leftNavEnabled");
                        } else {
                            $("body").addClass("rightNavEnabled").addClass("leftNavEnabled");
                        }
                    }

                    $leftButton.on("click", function (e) {
                        e.stopPropagation();
                    
                        let $category = $("#category-container");
                        let category = $category[0];
                    
                        let currentValue = getComputedStyle(category).getPropertyValue('--translate-move-x').trim();
                        if (!currentValue) {
                            currentValue = "0px";
                        }
                    
                        let newValue = Math.max(0, parseInt(currentValue) - 100) + "px";
                        category.style.setProperty('--translate-move-x', newValue);
                    
                        $category.animate({ scrollLeft: "-=100" }, 300, updateButtons);
                    });
                    
                    $rightButton.on("click", function (e) {
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
                    
                        let newValue = Math.min(maxTranslate, parseInt(currentValue) + 100) + "px"; // Prevent exceeding max scroll
                        category.style.setProperty('--translate-move-x', newValue);
                    
                        $category.animate({ scrollLeft: "+=100" }, 300, updateButtons);
                    });                                        

                    $("body").find("ul#category-container").before($leftButton);
                    $("body").find("ul#category-container").after($rightButton);
                } else {
                    $("body").removeClass("navigationEnabled");
                }
            }, 500);
        }
    })   

    $(window).on("page_loaded", function(){
        setTimeout(function(){
            var $hoveredCategory = $("ul#category-container > li.category:hover");
            if ($hoveredCategory.length > 0) {
                if ($("body").hasClass("navigationEnabled")) {
                    let $menuContainer = $hoveredCategory.find("ul.menu-container");
                    if (!$menuContainer.hasClass("appended")) {
                        $menuContainer.css({
                            "opacity": 0,
                            "pointer-events": "none"
                        })
                        $menuContainer.appendTo("body").addClass("appended");
                    }
                    $menuContainer.css({
                        top: ($hoveredCategory.offset().top + $hoveredCategory.height()) + 'px',
                        ...(($hoveredCategory.nextAll("li.category").length === 0 && !$("body").hasClass("inline_menu") && ($hoveredCategory.offset().left + 200 > $(window).width())) ? {
                            right: '0px'
                        } : {
                            left: $hoveredCategory.offset().left + 'px'
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
                    $menuContainer.addClass("mouseover").data("parent-id", $hoveredCategory.attr("id"));
                    $menuContainer.off("mouseleave").on("mouseleave", function(e) {
                        if ($(e.relatedTarget).closest("li.category").length > 0)
                            return;
                        const id = $hoveredCategory.data("parent-id");
                        $hoveredCategory.css({
                            top: "",
                            left: "",
                            visibility: "",
                            right: ""
                        }).removeClass("mouseover appended");
                        $("body").find("ul#category-container > li.category#" + id).append($hoveredCategory);
                    });
                }
            }
        }, 150);

        if ($("a.print-button").length > 0) {
            $("a.print-button").each(function(){
                $(this).appendTo($(this).closest("div.Form_Menu").find("div.viewForm-body-content div#section-actions"));
            })
        }

        $(".dataList .filters select").on("change", function(){
            $(this).closest("div.filters").find("input.form-button[type='submit'][value='Show']").click();
        })

        var $navigationButtons = $('<div class="navigation-arrows-container"><div class="left-navigation-arrow"></div><div class="right-navigation-arrow"></div></div>');

        $(".form-element.multiPagedForm > .page-nav-panel.top > ul").after($navigationButtons);

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
        
        // Initial check on page load
        $(".form-element.multiPagedForm > .page-nav-panel.top > ul").each(function() {
            checkScrollPosition($(this));
        }); 

        if ($("div.main-body-content").length > 1) {
            $("body").addClass("multiple_body_content");
            $("body.multiple_body_content div.main-body-content").each(function(index){
                $(this).append('<div class="dropdown-main-body-content-container"></div>');
    
                if (index !== 0) {
                    $(this).addClass("closed");
                } else {
                    activateScrollbar($(this), false);
                }
    
                $(this).find("div.dropdown-main-body-content-container").off("click").on("click", function(){
                    const $container = $(this).closest("div.main-body-content");
                    if ($container.hasClass("closed")) {
                        $container.removeClass("closed");

                        activateScrollbar($container, false);
                    } else {
                        $container.addClass("closed");
                        activateScrollbar($container, true);
                    }

                    $("#content.page_content").mCustomScrollbar("update");
                })
            })
        } else {
            $(".form-container > .form-section:not(#section-actions)").length <= 1 ? $("body").removeClass("multiple_body_content") : $("body").addClass("multiple_body_content");
            $(".form-container > .form-section:not(#section-actions)").each(function(index){
                $(this).append('<div class="dropdown-main-body-content-container"></div>');

                $(this).find("div.dropdown-main-body-content-container").off("click").on("click", function() {
                    const $container = $(this).closest(".form-section")
                    if ($container.hasClass("closed")) {
                        $container.removeClass("closed");
                        activateScrollbar($container, false);
                    } else {
                        $container.addClass("closed");
                        activateScrollbar($container, true);
                    }
                })

                if (index !== 0) {
                    $(this).addClass("closed");
                } else {
                    activateScrollbar($(this), false);
                }
            })
        }
        activateScrollbar($("#content.page_content"), false);
    })

    $(window).resize(function() {
        if ( ($("body").hasClass("horizontal_menu") && $(window).width() < 768) || !$("body").hasClass("horizontal_menu")) {
            $("#sidebar nav").mCustomScrollbar({
                autoHideScrollbar : true,
                scrollInertia: 300,
                scrollbarPosition: "inside",
                theme: "minimal-dark"});
        } else {
            $("#sidebar nav").mCustomScrollbar("destroy");
        }
    });
})

// Override AjaxTheme's initSidebar
AjaxUniversalTheme.initSidebar = function() {
    if ($("#sidebar").length > 0) {
        var sidebar = function(){
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
};