//  global variables
var navItems = [];
var navItemWidth = [];
var navItemVisible = [];
var moreWidth = 0;
var winWidth = 0;   

$(document).ready(function () {
    if ($("body").hasClass("horizontal_menu")){
        if ($("body").hasClass("inline_menu") && $(window).outerWidth() >= 768){
            resizeMenuWidth();
        }
        winWidth = $(window).width();
        navItems = $('#category-container > li');
        // get width of each item, and list each as visible
        navItems.each(function () {
            var itemWidth = $(this).outerWidth();
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
        setTimeout(formatNav, 250);
        // format navigation on page resize
        var id;
        $(window).resize(function() {
            var menuItemId = $(navItems[0]).prop('id');
            if ($(window).outerWidth() >= 768){
                var firstItemLength = $("#" + menuItemId).outerWidth();
            }
            //checks if the first menu item width is same as the one captured in navItemWIdth array 
            //if no, removes old content, remeasures and adds the lengths back in
            if (firstItemLength != navItemWidth[0] && $(window).outerWidth() >= 768 ){;
                navItemWidth.length = 0;
                $('#category-container > li').each(function () {
                    var itemWidth = $(this).outerWidth();
                    navItemWidth.push(itemWidth);
                }); 
            }
            // checks if id is undefined, if so creates a new id
            if(id == undefined){
                id = setTimeout(onResize, 500);
            }
            clearTimeout(id);
            id = setTimeout(onResize, 500);
        });
        function onResize () {
            if($(window).outerWidth() >= 768 && $("body").hasClass("inline_menu")){
                resizeMenuWidth();
            }else{
                //to prevent the sidebar menu from being affected when less than 768px
                $("#sidebar").width("");
            }
            if(winWidth != $(window).width()){
                moreWidth = $('#menu-more').outerWidth();
                // hide all submenus
                $('.menu-item-has-children').removeClass('visible');
                winWidth = $(window).width();
                setTimeout(formatNav, 150);
            }
        }
        function resizeMenuWidth (){
            var headerlink = 0;
            var headernav = 0;
            var totalheaderlengths = 0;
            
            headerlink = $('#header-link').outerWidth(true);
            headernav = $(".header-nav").width();
            //measured in px
            totalheaderlengths = headerlink + headernav + 40;
            totalheaderlengthsstring = totalheaderlengths.toString();
            line = "calc( 95vw - " + totalheaderlengthsstring + "px)";
            $("#sidebar").width(line);
        }
        function formatNav () {
            // initial variables
            var room = true;
            var count = 0;
            var tempWidth = 0;
            var totalWidth = 0; 
            var containerWidth = $('#navigation').innerWidth();
            var navPadding = 5; // for spacing around items
            var numItems = navItems.length - 1;

            // for each menu item
            navItems.each(function () {
                // get width of menu with that item
                tempWidth = totalWidth + navItemWidth[count] + navPadding;
                // show all menu items if window is less than 768px
                if ($(window).outerWidth() < 768){
                    $('#menu-more').before($('#moreSubMenu').children().first());
                    $('#menu-more').hide();
                }
                // if the menu item will fit
                else if (((tempWidth < (containerWidth - moreWidth - navPadding)) || ((tempWidth < (containerWidth)) && (count == numItems))) && (room == true)) {
                    // update current menu width
                    totalWidth = tempWidth;
                    // show menu item
                    if (navItemVisible[count] != true) {
                        // move back to main menu
                        $('#menu-more').before($('#moreSubMenu').children().first());
                        navItemVisible[count] = true;
                        // if all are visible, hide More
                        if (count == numItems) {
                            $('#menu-more').hide();
                        }
                    }
                }
                // if the menu item will not fit
                else {
                    // if there is now no room, show more dropdown
                    if (room == true) {
                        room = false;
                        // change text to "Menu" if no links are showing
                        if (count == 0) {
                            ;
                        }
                        else {
                            $('nav').removeClass('all-hidden');
                        }
                        $('#menu-more').show();
                    }
                    // move menu item to More dropdown
                    var menuItemId = $(this).prop('id');
                    $("#" + menuItemId).appendTo($('#moreSubMenu'));
                    navItemVisible[count] = false;
                }
                // update count
                count += 1;
            });
        }
    }
});
