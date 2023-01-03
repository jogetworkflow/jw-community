jQuery.expr[':'].Contains = function(a,i,m){ 
    return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase())>=0; 
};
var AppCenter = {
    searchFilter: function(header, list) { 
        var timer;
        var form = $("<form>").attr({"class":"filterform","action":"#","onsubmit":"return false"}), 
        input = $("<input>").attr({"class":"filterinput","type":"text"}); 
        $(form).append(input).append($("<span class='filterlabel'><i class='icon-search fa fas fa-search'></i></span>")).appendTo(header);  
        $(form).submit(function() { return false });
        $(input).change(function () { 
            var filter = $(this).val(); 
            if(filter) { 
                $(list).find(".userview-name:not(:Contains(" + filter + "))").closest("li").fadeOut(); 
                $(list).find(".userview-name:Contains(" + filter + ")").closest("li").show(); 
            } else { 
                $(list).find("li").show(); 
            } 
            return false; 
        }).keyup(function () {
            if (timer) clearTimeout(timer);
            var $this = $(this);
            timer = setTimeout(function() {
                $this.change(); 
            }, 50);
        }); 
        if(!UI.isMobileUserAgent()) {
            $(input).focus();
        }
    },
    designApp: function(event,appId, appVersion, userviewId) {
        window.open(UI.base + "/web/console/app/" + appId + "/" + appVersion + "/builders", "app-board-window");
        event.preventDefault();
        event.stopPropagation();
    }, 
    loadPublishedApps: function(container, customUrl, excludes) {
        container = container || "#apps";
        var isMarketplace = (container === '#marketplace-apps');
        var jsonUrl = (customUrl) ? customUrl : UI.base + "/web/json/apps/published/userviews?appCenter=true";

        // show loading icon
        $(container).empty();
        var loading = $('<div id="apps-loading"><i class="icon-spinner icon-spin icon-2x fa fa-spinner fa-spin fa-2x"></i></div>');
        $(container).append(loading);

        // load JSON
        $.ajax({ 
            url : jsonUrl,
            dataType:'json',
            success:function(data) {
                var content = "";
                var urlParams = UrlUtil.getUrlParams(location.href);
                var isCordova = urlParams['_cordova'] && urlParams['_cordova'][0] === "true";
                var target = (isCordova) ? '' : ' target="_blank"';
                var apps = data.apps;
                for( var i=0; i<apps.length; i++) {
                    // add app
                    var app = apps[i];
                    // add userviews
                    var userviews = app.userviews;
                    for( var j=0; j<userviews.length; j++){
                        var uv = userviews[j];
                        
                        if (excludes !== undefined && excludes !== null && excludes.indexOf(app.id+":"+uv.id) !== -1) {
                            continue;
                        }
                        
                        content += '<li>';
                        var userviewUrl = uv.url;
                        var userviewDescription = (typeof uv.description !== "undefined") ? uv.description : "";
                        var imageUrl = uv.imageUrl;
                        if (!imageUrl) {
                            imageUrl = UI.base + '/web/userview/screenshot/' + app.id + '/' + uv.id;
                        }
                        content += '<a class="app-link"' + target + ' href="' + userviewUrl + '">\
                                            <span class="userview-icon" style="background-image:url(\''+imageUrl+'\')"></span>\
                                            <div class="userview-name">' + uv.name + '</div>\
                                            <div class="app-name">' + app.name + '</div>\
                                            <div class="userview-description">' + userviewDescription + '</div>';
                        if ($("#adminControl").length > 0) {
                            content += "<span class='app-design-button' onclick='AppCenter.designApp(event,\"" + app.id + "\",\"" + app.version + "\",\"" + uv.id + "\");return false'><i class='icon-pencil fas fa-pencil-alt'></i></span>";
                        }
                        content += '</a>\
                                    </li>';
                    }
                }
                
                // show apps, hide loading icon
                $(loading).remove();
                $(container).append($(content));

                // sort by userview name
                var appsLi = $(container).find("li:not(.grid-dummy-fix)");
                appsLi.sort(function(a, b) {
                    var aName = $(a).find(".userview-name").text();
                    var bName = $(b).find(".userview-name").text();
                    if (aName.indexOf("<") === 0 || aName.indexOf("#") === 0 || aName.toUpperCase() >= bName.toUpperCase()) {
                        return 1;
                    } else {
                        return -1;
                    }
                });
                appsLi.detach().appendTo($(container));
                
                for (var i=0; i < 10; i++) {
                    $(container).append('<li class="grid-dummy-fix"></li>');
                }

                $(function() {                    
                    HelpGuide.reposition();     
                })
                
                // check if within IFRAME
                if (window.self === window.top) {
                    $("#apps li").hover(
                        function() {
                            $(this).find(".app-design-button").show();
                        },
                        function() {
                            $(this).find(".app-design-button").hide();
                        }
                    );
                } else {
                    // open link within IFRAME
                    $("a.app-link").attr("target", "_self");
                    $("a.app-link").on("click", function() {
                        $(".page-loader").fadeIn();
                    });
                }
            }
        });
        
        // hide admin buttons if within IFRAME
        if (window.self !== window.top) {
            $("#appcenter_admin").hide();
        }        
    },
    showHints: function() {
        if ($("#main-action-help").length > 0) {
            if ($("#adminControl:visible").length > 0) {
                HelpGuide.key="help.web.adminBar";
            } else {
                HelpGuide.key="help.web.appcenter";
            }
            HelpGuide.insertButton();
            HelpGuide.show();
            HelpGuide.reposition();
        }
    },
    updateClock: function(clock){
        var date = new Date();
        var ampm = date.getHours() < 12 ? 'AM' : 'PM';
        var hours = date.getHours() == 0
                  ? 12
                  : date.getHours() > 12
                    ? date.getHours() - 12
                    : date.getHours();
        var minutes = date.getMinutes() < 10 
                    ? '0' + date.getMinutes() 
                    : date.getMinutes();
        $(clock).text(hours + ":" + minutes + " " + ampm);
    },
    updateNotifications: function(banner) {
        $("li a.refresh").click();
        setTimeout(function() {
            var messageSpan = $("li.dropdown-menu-title span");
            if ($(messageSpan).length > 0) {
                var message = $(messageSpan).text();
                var html = $(messageSpan).html();
                if (message  === "You have 1 assignments.") {
                    html = html.replace("assignments", "assignment");
                    $(messageSpan).html(html);
                }
                var newHtml = '<a href="_ja_inbox">' + html + '</a>';
                $(banner).html(newHtml);
            }
        }, 3000);
    },
    responsiveGetBannerMaxWidth: function() {
        var max_banner_width = $("body").width() - 550;
        return max_banner_width;
    },
    responsiveMoveHint: function() {
        var totalWidth = $("body").width();
        var appWidth = $('#main, #page > header').width();
        var bannerWidth = (totalWidth - appWidth - 2);
        $("body#home div#main-action-help").css("left",bannerWidth - 40);
        $("body#home div#main-action-help").css("right","unset");
    },
    responsiveResizeAppEvent: function(event, ui) {
        var width = $(".home_banner").width();
        AppCenter.responsiveResizeApp(width);
    },
    responsiveResizeApp: function(width) {
        var total_width = $("body").width();
        var contentWidth = null;
        
        if (width === undefined) {
            contentWidth = localStorage.getItem("appCenterContentWidth");
            if (contentWidth === null || contentWidth === undefined) {
                width = $(".home_banner").width();
                contentWidth = total_width - width;
            } else {
                width = total_width - contentWidth;
                $(".home_banner").width(width);
            }
        } else {
            contentWidth = total_width - width;
            localStorage.setItem("appCenterContentWidth", contentWidth);
        }
        
        if (contentWidth < 550) {
            contentWidth = 550;
            width = total_width - contentWidth;
            $(".home_banner").width(width);
        } else if (width < 630) {
            width = 630;
            contentWidth = total_width - width;
            $(".home_banner").width(width);
        }
        
        $('#main, #page > header').css('width', (contentWidth));
        
        //fix mac os scrollbar issue
        if ($('#page > header').offset().left - width > 5) {
            width = $('#page > header').offset().left;
            $(".home_banner").width(width);
        }
        
        if(width < 630){
            $("body #page div#clock").hide();
        }else{
            $("body #page div#clock").show();
        }

        if(width < 300){
            $("#banner h1").hide();
        }else{
            $("#banner h1").show();
        }

        if(width < 150){
            $("body#home div#brand_logo img").hide();
        }else{
            $("body#home div#brand_logo img").show();
        }
        
        AppCenter.responsiveMoveHint();
    },
    responsiveResizeBanner: function(event, ui) {
        var singleColumnMaxWidth = 1025;
        var totalWidth = $("body").width();

        if(totalWidth <= singleColumnMaxWidth){
            //single column mode
            if ($(".home_banner").resizable( "instance" ) != undefined) {
                $(".home_banner").resizable("destroy");
            }
            $("#main, #page > header, .home_banner").css('width', "");
            $("body#home div#main-action-help").css("left", "");
            $("body#home div#main-action-help").css("right","");

        }else if(totalWidth > singleColumnMaxWidth){
            //wide enough for 2 columns mode
            if($(".home_banner").resizable( "instance" ) == undefined){
                $(".home_banner").resizable({handles: "e", minWidth: 630, maxWidth: AppCenter.responsiveGetBannerMaxWidth()}).bind("resize", AppCenter.responsiveResizeAppEvent);
            }
            
            AppCenter.responsiveResizeApp();
        }
    },
    rotateBackgroundStart : function(backgrounds, interval){
        intervalInSeconds = interval * 1000;
        currentBackground = $.cookie("appCenterBackground");
        //show the last background saved in user's browser
        if(currentBackground != null && backgrounds.indexOf(currentBackground) != -1){
            $("#banner").css("background", 'url("' + currentBackground + '")');
            $("#banner").css("background-size", "cover");
        }
        setTimeout(function(){
            AppCenter.rotateBackground(backgrounds, intervalInSeconds);
        }, intervalInSeconds);
    },
    rotateBackground : function(backgrounds, interval){
        max = backgrounds.length;
        next = Math.floor(Math.random() * max);
        
        currentBackground = $.cookie("appCenterBackground");
        if(backgrounds[next] == currentBackground){
            AppCenter.rotateBackground(backgrounds, interval);
        }else{
            $("#banner").css("background", 'url("' + backgrounds[next] + '")');
            $("#banner").css("background-size", "cover");
            $.cookie("appCenterBackground", backgrounds[next]);
            
            setTimeout(function(){
                AppCenter.rotateBackground(backgrounds, interval);
            }, interval);
        }
    },
    showNotifications: true
}
$(function() {
    if (window.self === window.top) {
        // show hints if not within IFRAME
        setTimeout(function(){
            AppCenter.showHints();
        }, 500);
    } else {
        $("#main-action-help").hide();
    }
    var clock = $("#clock");
    if (clock.length > 0) {
        AppCenter.updateClock(clock);
        window.setInterval(function() {
            AppCenter.updateClock(clock);
        }, 10000);
    }
    var banner = $("#banner h1");
    if (AppCenter.showNotifications && banner.length > 0) {
        AppCenter.updateNotifications(banner);
        UI.visibilityChangeSetInterval("notifications", function() {
            AppCenter.updateNotifications(banner);
        }, 30000);
    }
    
    if ($("#clock").length > 0 && $("body#home div#brand_logo").length > 0) {
        AppCenter.responsiveResizeBanner();

        $(window).bind("resize", function(event){
            if(this == event.target){
                if($(".home_banner").resizable( "instance" ) != undefined){
                  if($(this).width() <= 1400){
                        $(".home_banner").resizable("disable");
                    }else{
                        $(".home_banner").resizable("enable");
                        $(".home_banner").resizable("option", "maxWidth", AppCenter.responsiveGetBannerMaxWidth());    
                    }
                }
                AppCenter.responsiveResizeBanner();
            }
        });
    }
});
//AppCenter.searchFilter($("#search"), $("#apps")); 
//AppCenter.loadPublishedApps("#apps");