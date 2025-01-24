$(document).ready(function() {
    if ($("body").hasClass("horizontal_menu")) {
        $("body").off("mouseover", 'li.category.mega-menu-category').on("mouseover", 'li.category.mega-menu-category', function() {
            if ($(this).hasClass("category-hover"))
                return;
            var sidebar = $(this).closest("#sidebar");
            var megaMenuWrapper = $("body").find("div.mega-menu-container#mega-menu-" + $(this).attr('id'));
            var top = $("body header.navbar").length > 0 ? ($(this).offset().top + $(this).outerHeight(true)) + 'px' : 0;
            if ($(this).closest("li#menu-more").length) {
                top = $("body header.navbar").length > 0 ? $("body header.navbar").height() + 'px' : 0;
            }
            var left = ($(sidebar).offset().left + $(sidebar).outerWidth()) + 'px';
            var width = ($(window).outerWidth() - $(sidebar).outerWidth()) + 'px';
            if ($("body").hasClass("horizontal_menu")) {
                left = 0 + 'px';
                width = $(window).outerWidth() + 'px';
                if (!$("body").hasClass("inline_menu")) {
                    top = ($("body header.navbar").height() + ($(sidebar).height())) + 'px'
                }
            }
            $(megaMenuWrapper).css({
                'top': top,
                'left': left,
                'width': width,
                'max-height': 'calc(100% - ' + top + ')'
            })
            if ($(this).closest("li#menu-more").length) {
                $(megaMenuWrapper).appendTo('li#menu-more');
                setTimeout( () => {
                    $(this).closest("li#menu-more").find(".mega-menu-container.category-hover, li.category.category-hover").removeClass("category-hover");
                    $(megaMenuWrapper).addClass("category-hover");
                    $(this).addClass("category-hover");
                    $(this).closest("li#menu-more").find("ul#moreSubMenu").addClass("stack");
                    $(this).closest("li#menu-more").find("ul#moreSubMenu").removeClass("hidden")
                    $(megaMenuWrapper).off("mouseover").on("mouseover", function(e) {
                        $(this).closest("li#menu-more").find("ul#moreSubMenu").removeClass("stack");
                        $(this).closest("li#menu-more").find("ul#moreSubMenu").addClass("hidden");
                    })
                }
                , 200)
            } else {
                $(megaMenuWrapper).appendTo($(this));
                $(this).addClass("category-hover");
            }
        }).off("mouseleave", 'li.category.mega-menu-category').on("mouseleave", 'li.category.mega-menu-category', function(e) {
            var megaMenuWrapper = $("body").find("div.mega-menu-container#mega-menu-" + $(this).attr('id'));
            var categoryOver = this;

            if (!$(e.relatedTarget).closest('li#menu-more').length) {
                $("body").find("ul#moreSubMenu.hidden").removeClass("hidden");
                $(this).removeClass("category-hover");
                $(megaMenuWrapper).removeClass("category-hover");
            } else {
                $(e.relatedTarget).closest('li#menu-more').off("mouseleave").on("mouseleave", function(e) {
                    var current = $(this).find("li.category-hover");
                    if ($(categoryOver).closest("li#menu-more").length === 0) {
                        current = categoryOver;
                    }
                    if ($(current).length) {
                        $(current).removeClass("category-hover");
                        $(current).closest("ul#moreSubMenu").removeClass("stack");
                        $(megaMenuWrapper).removeClass("category-hover");
                        $("body").find("ul#moreSubMenu.hidden").removeClass("hidden");
                    }
                }).off("mouseover", "i#menuMoreLink").on("mouseover", "i#menuMoreLink", function() {
                    if ($(this).closest("li#menu-more").find("li.category-hover").length) {
                        $(this).closest("li#menu-more").find("ul#moreSubMenu").addClass("stack");
                        $(this).closest("li#menu-more").find("ul#moreSubMenu").removeClass("hidden");
                        $(megaMenuWrapper).off("mouseover").on("mouseover", function() {
                            if ($(this).closest("li#menu-more").find("ul#moreSubMenu").hasClass("stack")) {
                                $(this).closest("li#menu-more").find("ul#moreSubMenu").removeClass("stack");
                            }
                        })
                    }
                })
            }
        })
        $(document).on("touchstart", function(event) {
            if ($("body").find("li.mega-menu-category.category-hover, div.mega-menu-container.category-hover").length > 0 && $(event.target).closest("li.mega-menu-category.category-hover, div.mega-menu-container.category-hover").length === 0) {
                $("li.mega-menu-category.category-hover, div.mega-menu-container.category-hover").removeClass("category-hover");
            }
        });
        document.addEventListener("wheel", function (event) {
            let container = event.target.closest(".mega-menu-container");
        
            if (container) {
                let scrollTop = container.scrollTop;
                let scrollHeight = container.scrollHeight;
                let clientHeight = container.clientHeight;

                let buffer = 2;

                let atTop = scrollTop === 0;
                let atBottom = scrollTop + clientHeight >= scrollHeight - buffer;
        
                if ((event.deltaY < 0 && atTop) || (event.deltaY > 0 && atBottom)) {
                    event.preventDefault();
                }
            }
        }, { passive: false });
    }
});