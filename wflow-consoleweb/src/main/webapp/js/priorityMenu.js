$(document).ready(function () {
    let navItems = [];
    let navItemWidth = [];
    let navItemVisible = [];
    let moreWidth = 0;
    let winWidth = 0;
    const menuBreakpoint = 768;
    let initialLoad = true;

    if ($("body").hasClass("horizontal_menu")) {
        winWidth = $(window).width();
        navItems = $('#category-container > li');
        // get width of each item, and list each as visible
        navItems.each(function () {
            let itemWidth = $(this).outerWidth();
            navItemWidth.push(itemWidth);
            navItemVisible.push(true);
        });
        // add more link
        $('#category-container').append('<li id="menu-more" class="menu-item menu-item-has-children" style="display: none;"><i class="fas fa-ellipsis-h" id="menuMoreLink" href="#"></i><ul id="moreSubMenu" class="sub-menu"></ul></li>');
        moreWidth = $('#menu-more').outerWidth();
        // toggle sub-menu
        $('#menuMoreLink').click(function(event) {
            event.preventDefault();
            $('.menu-item-has-children:not(#menu-more)').removeClass('visible');
            $(this).parent('.menu-item-has-children').toggleClass('visible');
        });
        // collapse all sub-menus when user clicks off
        $('body').click(function(event) {
            if (!$(event.target).closest('.menu-item').length) {
                $('.menu-item-has-children').removeClass('visible');
            }
        });
        $('.menu-item-has-children a').click(function(e) { e.stopPropagation(); });
        $('.menu-item-has-children ul').click(function(e) { e.stopPropagation(); });
        $('.menu-item-has-children li').click(function(e) { e.stopPropagation(); });
        // toggle all sub-menus
        $('.menu-item-has-children').click(function(event) {
            if (!$(this).hasClass('visible')) {
                $(this).siblings('.menu-item-has-children').removeClass('visible');
                $(this).addClass('visible');
            }
            else {
                $(this).removeClass('visible');
            }
        });
        // format navigation on page load
        // delay is added so that menu loads in the items that fit properly
        if ($("body").hasClass("inline_menu") && $(window).outerWidth() >= menuBreakpoint) {
            resizeMenuWidth(formatNav);
        } else {
            setTimeout(formatNav, 250);
        }
        // format navigation on page resize
        let id;
        $(window).resize(function() {
            let firstItemLength;
            let menuItemId = $(navItems[0]).prop('id');
            if ($(window).outerWidth() >= menuBreakpoint){
                firstItemLength = $("#" + menuItemId).outerWidth();
            }
            //checks if the first menu item width is same as the one captured in navItemWidth array
            //if no, removes old content, remeasures and adds the lengths back in
            if (firstItemLength !== navItemWidth[0] && $(window).outerWidth() >= menuBreakpoint ){
                navItemWidth.length = 0;
                $('#category-container > li').each(function () {
                    let itemWidth = $(this).outerWidth();
                    navItemWidth.push(itemWidth);
                });
            }
            // checks if id is undefined, if so creates a new id
            if(id === undefined){
                id = setTimeout(onResize, 500);
            }
            clearTimeout(id);
            id = setTimeout(onResize, 500);
        });
        function onResize () {
            if($(window).outerWidth() >= menuBreakpoint && $("body").hasClass("inline_menu")){
                resizeMenuWidth(formatNav, true);
            }else{
                //to prevent the sidebar menu from being affected when within menu breakpoint (less than 768px)
                $("#sidebar").width("");
            }
            if(winWidth !== $(window).width()){
                moreWidth = $('#menu-more').outerWidth();
                // hide all submenus
                $('.menu-item-has-children').removeClass('visible');
                winWidth = $(window).width();
                setTimeout(formatNav, 150);
            }
        }
        function resizeMenuWidth (callback, checkWidth){
            // header title and header nav button container width
            const headerLinkWidth = $('#header-link').outerWidth(true);
            const headerNavWidth = $(".header-nav").outerWidth(true);
            const totalHeaderWidth = headerLinkWidth + headerNavWidth;

            // width of the containerFluid
            const containerFluidElem = $('.container-fluid');
            const containerFluidWidth = containerFluidElem.width();

            // get width of the sidebar's outer width (padding, margin, borders)
            const sidebarElem = $('#sidebar');
            const sidebarOuterWidth = sidebarElem.outerWidth(true);
            const sidebarWidth = sidebarElem.width();
            const sidebarExtraWidth = sidebarOuterWidth - sidebarWidth;

            // Calculate the available width for the sidebar, with an additional 20 pixels for safety distance from the header-nav button.
            // navbar width - all the extra width and header length
            let availableWidth = containerFluidWidth - totalHeaderWidth - sidebarExtraWidth - 20;
            $("#sidebar").width(availableWidth);

            // Call the callback function if provided
            if (typeof callback === 'function') {
                if (checkWidth) {
                    //for initial load skip width check
                    if (winWidth !== $(window).width() || initialLoad) {
                        initialLoad = false;
                        moreWidth = $('#menu-more').outerWidth();
                        // hide all submenus
                        $('.menu-item-has-children').removeClass('visible');
                        winWidth = $(window).width();

                        callback(availableWidth);
                    }
                } else {
                    callback(availableWidth);
                }
            }
        }
        function formatNav(containerWidth) {
            // initial variables
            let room = true;
            let count = 0;
            let tempWidth = 0;
            let totalWidth = 0;
            let navPadding = $('#category-container').outerWidth() - $('#category-container').width(); // for spacing around items
            let numItems = navItems.length - 1;
            if (containerWidth === undefined) {
                containerWidth = $('#navigation').innerWidth();
            }
            // for each menu item
            navItems.each(function () {
                // get width of menu with that item
                tempWidth = totalWidth + navItemWidth[count] + navPadding;
                // show all menu items if window is within menu breakpoint (less than 768px)
                if ($(window).outerWidth() < menuBreakpoint){
                    $('#menu-more').before($('#moreSubMenu').children().first());
                    $('#menu-more').hide();
                }
                // if the menu item will fit
                else if (((tempWidth < (containerWidth - moreWidth - navPadding)) || ((tempWidth < (containerWidth)) && (count === numItems))) && (room === true)) {
                    // update current menu width
                    totalWidth = tempWidth;
                    // show menu item
                    if (navItemVisible[count] !== true) {
                        // move back to main menu
                        $('#menu-more').before($('#moreSubMenu').children().first());
                        navItemVisible[count] = true;
                        // if all are visible, hide More
                        if (count === numItems) {
                            $('#menu-more').hide();
                        }
                    }
                }
                // if the menu item will not fit
                else {
                    // if there is now no room, show more dropdown
                    if (room === true) {
                        room = false;
                        // change text to "Menu" if no links are showing
                        if (count !== 0) {
                            $('nav').removeClass('all-hidden');
                        }
                        $('#menu-more').show();
                    }
                    // move menu item to More dropdown
                    let menuItemId = $(this).prop('id');
                    $("li#" + menuItemId).appendTo($('#moreSubMenu'));
                    navItemVisible[count] = false;
                }
                // update count
                count += 1;
            });
        }
    }
});
