$(document).ready(function() {
    $("#sidebar-trigger").find("img#light-thumbnail").attr('src', UI.base + '/images/dx9AppCenter/thumbnail_bar.svg');

    setTimeout(function(){
        if($(".systemAlertMessage.passwordExpired").length === 1 && $(".systemAlertMessage").length === 1) {
            $("body").addClass("systemAlertPresent");
        }

        if ($(".systemAlertMessage.passwordExpired").length === 1) {
            $("header.navbar .container-fluid > #help-container").after(`
                <a class='renew-banner' target='_blank' href='https://www.jogetcloud.com/subscribe-on-demand.html'>
                    <img class='desktop-view' src='`  + UI.base + `/images/dx9AppCenter/renewBanner.svg'>
                    <div class='mobile-view'>
                        <i class='fas fa-play'></i> Renew Now
                    </div>
                </a>
            `);
        }
    }, 300)

    $("div#main #header-link").prepend("<img src='" + UI.base + "/images/dx9AppCenter/headerLogo.svg'>");

    setTimeout(function(){
        var $taskCounter = $("#taskCounter");
        var $notifications = $("#page > header > div > div > div.nav-no-collapse.header-nav > ul > li.inbox-notification.dropdown > ul.notifications");
        let inboxIndex = 0;
        setInterval(function(){
            if ($taskCounter.find("#tasksMessage > span.header").length > 0 && $notifications.find("li.task").length > 1){
                $taskCounter.find("#tasksMessage").animate({
                    opacity: 0.25
                }, 500, 'linear', function() {
                    if ($notifications.find("li.task").eq(inboxIndex + 1).length > 0) {
                        inboxIndex += 1;
                        $taskCounter.find("#tasksMessage").remove();
                        $taskCounter.find("a#viewTasks").before("<div id='tasksMessage'> " + $notifications.find("li.task").eq(inboxIndex).html() + "</div>");
                    } else {
                        inboxIndex = 0;
                        $taskCounter.find("#tasksMessage").remove();
                        $taskCounter.find("a#viewTasks").before("<div id='tasksMessage'> " + $notifications.find("li.task").eq(inboxIndex).html() + "</div>");
                    }

                });
            }
        }, 3000);
    }, 1500)
    
    $("body").off("page_loaded").on("page_loaded", function(){
        $('.carousel-item', '.show-neighbors').each(function(){
            var next = $(this).next();
            if (! next.length) {
                next = $(this).siblings(':first');
            }
            next.children(':first-child').clone().appendTo($(this));
            }).each(function(){
            var prev = $(this).prev();
            if (! prev.length) {
                prev = $(this).siblings(':last');
            }
            prev.children(':nth-last-child(2)').clone().prependTo($(this));
        });

        $('.carousel').carousel('cycle');
    })

    $(window).resize(function(){
        if ($(window).outerWidth() < 1280) {
            if ($("div#page > ul#category-container").length === 0) {
                var $cloneSidebar = $("ul#category-container").clone();
                
                $("footer").after($cloneSidebar);

                $("div#page > ul#category-container > li.category").on("click", function() {
                    $(this).siblings("li.active").removeClass("active");
                    $(this).addClass("active");
                })
            } 

            if ($("header.navbar .container-fluid > #header-link").length === 0) {
                $("header.navbar .container-fluid").prepend($("#header-link").clone().prepend("<img src='" + UI.base + "/images/dx9AppCenter/headerLogo.svg'>"));
            }

            if ($(window).outerWidth() < 768 && $("header.navbar .container-fluid > li.user-link").length === 0) {
                $("header.navbar .container-fluid").prepend($("header.navbar .header-nav > ul > li.user-link").clone());
            } else if ($(window).outerWidth() >= 768){
                $("header.navbar .container-fluid > li.user-link").remove();
            }
        } else {
            if ($("div#page > ul#category-container").length > 0) {
                $("div#page > ul#category-container").remove();
            } 

            if ($("header.navbar .container-fluid > #header-link").length > 0) {
                $("header.navbar .container-fluid > #header-link").remove();
            }

            if($("header.navbar .container-fluid > li.user-link").length > 0) {
                $("header.navbar .container-fluid > li.user-link").remove();
            }
        }
    })

    $('body').off('page_loaded.appcenter');
    $('body').on('page_loaded.appcenter', function(){
        $(window).on("resize", function(){
            function fadeOut($el, callback) {
                $el.css('opacity', 0);

                $el.one('transitionend', function (e) {
                    if (e.originalEvent.propertyName === 'opacity') {
                        $el.addClass('hidden').css('opacity', '');
                        if (callback) callback();
                    }
                });
            }

            function fadeIn($el, callback) {
                $el.removeClass('hidden').css('opacity', 0);

                $el.css('opacity', 1);

                $el.one('transitionend', function (e) {
                    if (e.originalEvent.propertyName === 'opacity') {
                    if (callback) callback();
                    }
                });
            }
            const $tabs = $("ul#mobileTab > li");
            const $leftCol = $("#searchColumnContainer > .row > .col:first-child");
            const $rightCol = $("#searchColumnContainer > .row > .col:last-child");

            if ($(window).outerWidth() < 768) {
                if ($("ul#mobileTab > li.active").length === 0) {
                    $tabs.eq(0).addClass("active");
                    $leftCol.removeClass('hidden').css('opacity', 1);
                    $rightCol.addClass('hidden').css('opacity', 0);
                } else {
                    var activeTab = $("ul#mobileTab > li.active").attr('id');
                    if (activeTab === "taskTab") {
                        $rightCol.addClass('hidden').css('opacity', 0);
                    }else {
                        $leftCol.addClass('hidden').css('opacity', 0);
                    }
                }

                $tabs.off("click").on("click", function () {
                    const $clicked = $(this);
                    if ($clicked.hasClass("active")) return;

                    $tabs.removeClass("active");
                    $clicked.addClass("active");

                    const isTaskTab = $clicked.attr("id") === "taskTab";
                    const isMiniTab = $clicked.attr("id") === "miniBannerTab";

                    if (isMiniTab) {
                        fadeOut($leftCol, () => {
                            fadeIn($rightCol);
                        });
                    } else if (isTaskTab) {
                        fadeOut($rightCol, () => {
                            fadeIn($leftCol);
                        });
                    }
                });
            } else {
                $leftCol.removeClass('hidden').css('opacity', 1);
                $rightCol.removeClass('hidden').css('opacity', 1);
            }
        })

        if ($('#dataList_list_appList').length > 0) {
            $('#dataList_list_appList .column_img').children('div').each(function () {
                var style = $(this).attr('style');
                $(this).parents(".data-row").attr('style', style);
            }); 
        }

        if ($("body#design_app").length === 1) {
            const $arrowContainer = $("body#design_app .design-app-arrows");

            $("ul#nav > li").eq(0).addClass("active");
            $("body#design_app").find("div#" + $("ul#nav > li.active").attr("data-target")).addClass("active");

            $("ul#nav > li").off("click").on("click", function() {
                $("body#design_app").find("div#" + $(this).parent().find("li.active").attr("data-target")).removeClass("active");
                $(this).parent().find("li.active").removeClass("active");
                
                $(this).addClass("active");
                $("body#design_app").find("div#" + $(this).attr("data-target")).addClass("active");

                resetArrows($arrowContainer);
            })

            // Initialize the page View
            $(".design-view-options > a#per-view").addClass("active");
            $(".design-view-options > a:not('#view-templates')").off("click").on("click", function(){
                $(".design-view-options > a.active").toggleClass("active");
                $(this).toggleClass("active");
            }) 

            $("body#design_app #perPageView > .row > .col:first-child").addClass("show");

            function resetArrows(arrowContainer) {
                const $activeContainer = $(".ContainerComponent.active");

                if ($activeContainer.length > 0) {
                    const $pages = $activeContainer.children(':not(style)');
                    const totalPages = $pages.length;
                    const currentIndex = $pages.index($pages.filter(':not(.hide)').first());

                    if (totalPages === 1 || $activeContainer.css('display') !== 'grid') {
                        $(arrowContainer).find(".fas").addClass("disabled");
                    } else {
                        $(arrowContainer).find(".fas").removeClass("disabled");

                        if (currentIndex === 0) {
                            $(arrowContainer).find(".fas#left-arrow").addClass("disabled");
                        }

                        if (currentIndex === totalPages - 1) {
                            $(arrowContainer).find(".fas#right-arrow").addClass("disabled");
                        }
                    }
                }
            }

            const $leftArr = $arrowContainer.find("#left-arrow");
            const $rightArr = $arrowContainer.find("#right-arrow");

            $leftArr.off("click").on("click", function(){
                const $activeContainer = $(".ContainerComponent.active");
                const $pages = $activeContainer.children(':not(style)');
                const currentIndex = $pages.index($pages.filter(':not(.hide)').first());

                if (currentIndex > 0  && $pages.length !== 1) {
                    $pages.eq(currentIndex).addClass('hide');
                    $pages.eq(currentIndex - 1).removeClass('hide slide-right slide-left');
                }

                resetArrows($arrowContainer);
            });

            $rightArr.off("click").on("click", function(){

                const $activeContainer = $(".ContainerComponent.active");
                const $pages = $activeContainer.children(':not(style)');
                const currentIndex = $pages.index($pages.filter(':not(.hide)').first());

                if (currentIndex < $pages.length - 1 && $pages.length !== 1) {
                    $pages.eq(currentIndex).addClass('hide');
                    $pages.eq(currentIndex + 1).removeClass('hide slide-right slide-left');
                }

                resetArrows($arrowContainer);
            });

            resetArrows($arrowContainer);

            // Nav Logic
            $(window).resize(function(){
                if ($(window).outerWidth() >= 991) {
                    $(".ContainerComponent").each(function(){
                        if ($(this).css('display') === 'grid' && !$(".design-app-arrows > #right-arrow").hasClass("disabled")) {
                            $(this).children(':not(style)').not(':first-child').addClass("hide");
                        }
                    });
                } else {
                    $(".ContainerComponent.active > .ColumnsComponent").removeClass("hide");
                }

                resetArrows($arrowContainer);
            });
        }

        if ($("body#admin").length === 1) {
            $("ul#admin-nav > li").eq(0).addClass("active");
            $("body#admin").find("div#" + $("ul#admin-nav > li.active").attr("data-target")).addClass("active");

            $("ul#admin-nav > li").off("click").on("click", function() {
                $("body#admin").find("div#" + $(this).parent().find("li.active").attr("data-target")).removeClass("active");
                $(this).parent().find("li.active").removeClass("active");

                $(this).addClass("active");
                $("body#admin").find("div#" + $(this).attr("data-target")).addClass("active");
            })

            $("body#admin #users_section .filters > span.filter-cell:last-of-type input").off("click.filter").on("click.filter", function() {
                localStorage.setItem("last-admin-tab", $(this).closest("#users_section").find("#user-tab-nav > li.active").attr('id'))
            })
            
            $("ul#user-tab-nav > li").off("click").on("click", function() {
                $(this).parent().find("li.active").removeClass("active");
                $(this).addClass("active");
            })

            if(localStorage.getItem("last-admin-tab") !== null) {
                const navId = localStorage.getItem("last-admin-tab");

                $("ul#user-tab-nav > li#" + navId).click();
                $("ul#admin-nav > li:last-of-type").click();

                setTimeout(function(){
                    localStorage.removeItem("last-admin-tab");
                }, 500);
            } else {
                $("ul#user-tab-nav > li:first-of-type").click();
            }

            $(window).off("resize.filter").on("resize.filter", function(){
                if ($(window).outerWidth() < 767) {
                    const $createBtn = $("#users_section .filters a.btn");
                    $createBtn.each(function(index, item){
                        $(item).appendTo($(item).closest(".filter_form"))
                    })
                } else {
                    const $createBtn = $("#users_section .filter_form > a.btn");
                    $createBtn.each(function(index, item){
                        $(item).appendTo($(item).closest(".filter_form").find(".filters"))
                    })
                }
            })
        }

        if ($('body#home').length === 1) {
            if ($('#dataList_applist .card-icon').length <= 0) {
                if ($('.login_link').length === 1) {
                    window.location.replace(UI.base + "/web/ulogin/appcenter/home/_/home");
                } else if ($('#NoAppAvailable').length === 0) {
                    $("<div id=\"NoAppAvailable\"></h1><i class=\"zmdi zmdi-alert-circle-o\"></i><h2></h2></div>").appendTo("div#home");
                    UI.loadMsg(['ubuilder.noAppAvailable'], function(msgs){
                        $('#NoAppAvailable').html("</h1><i class=\"zmdi zmdi-alert-circle-o\"></i><h2>"+msgs['ubuilder.noAppAvailable']+"</h2>");
                    });
                }
            }
            
            //Group the app badges together
            $("body#home #home_column div#dataList_applist .row.cards > .card-icon").each(function () {
                const $card = $(this);

                // Extract the elements
                const $col1 = $card.find(".appIcon > .column_1").detach();
                const $col6 = $card.find(".appIcon > .column_6").detach();

                // Create the flex wrapper and append both columns
                const $badgeGroup = $("<div class='version-badge-group d-flex gap-1 align-items-center justify-content-center'></div>")
                    .append($col1)
                    .append($col6);
                
                //Insert it
                $card.find(".appIcon h5").after($badgeGroup);
            });
        }
        if ($('body#design_app #tutorial_section').length === 1) {
            UI.loadMsg(['appcenter.video1','appcenter.video2','appcenter.video3','appcenter.video4','appcenter.video5'], function(msgs){
                $('body#design_app #tutorial_section iframe').each(function(){
                    var id = $(this).attr("id");
                    var url = msgs['appcenter.'+id];
                    if (url !== undefined && url !== "" && $(this).attr("src") !== url) {
                        $(this).attr("src", url);
                    }
                });
                $('body#design_app #tutorial_section').show();
            });
        }
    });
    if (window["ajaxContentPlaceholder"] !== undefined) {
        window["ajaxContentPlaceholder"][UI.base + '/web/userview/appcenter/home/_/home'] = "dashboard";
        window["ajaxContentPlaceholder"][UI.base + '/web/userview/appcenter/home/_/templates'] = "dashboard";
        window["ajaxContentPlaceholder"][UI.base + '/web/userview/appcenter/home/_/admin'] = "dashboard";
    }
    $('#loginForm > table > tbody > tr:nth-child(3) > td:nth-child(2) > input').val('Sign In');
});

// custom search
$(document).ready(function () {
    const renderCustomSearch = function () {
        // prevent double render in case already rendered
        if ($('div#customSearchSection').length === 0) {
            $('body#home #home.main-body-content #filters_applist').hide();
            const customSearchHtml =
                '<div id="customSearchSection" class="closed">' +
                '  <button id="customSearchButton" class="btn btn-secondary btn-sm button">Search</button>' +
                '  <input id="customSearchInput" type="text" placeholder="Filter apps" />' +
                '</div>';
            $("body#home #home.main-body-content").before(customSearchHtml);
            $("#customSearchInput").off("input").on("input", function () {
                const input = this.value.trim().toLowerCase();
                const apps = $("#dataList_applist .table-wrapper .cards.row").children();
                if (input === "") {
                    apps.show();
                    return;
                }
                apps.each(function () {
                    const $this = $(this);
                    const name = $this.find(".card-title").text().trim().toLowerCase();
                    const toggleCondition = name.includes(input) || name.replace(/[^\p{L}\p{N}]/gu, '').includes(input);
                    $this.toggle(toggleCondition);
                });
            });

            // set events and custom logic to handle animations
            const customSearchSection = $('#customSearchSection');

            $('#customSearchSection > #customSearchButton').on('click', function () {
                customSearchSection.removeClass('closed').addClass('open');
                customSearchSection.find('#customSearchInput').focus();
            });

            $('#customSearchSection > #customSearchInput').on('blur', function () {
                if ($(this).val() === '') {
                    customSearchSection.removeClass('open').addClass('closed');
                } else {
                    customSearchSection.removeClass('closed').addClass('open');
                }
            });
        }
    };

    // attempt to render custom search on ajax navigation
    $(document).on('page_loaded', renderCustomSearch);
});

$(function() {
    $(window).on("scroll", function() {
        if($(window).scrollTop() > 50) {
            $(".navbar").addClass("active");
        } else {
            //remove the background property so it comes transparent again
           $(".navbar").removeClass("active");
        }
    });
}); 