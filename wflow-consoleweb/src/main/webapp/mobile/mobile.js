var Mobile = {

    contextPath: "/jw",
    
    viewFullSite: function(path, href) {
        $.cookie("desktopSite", "true", {
            path: path
        });
        location.href=href;
        return false;
    },
    
    viewMobileSite: function(path, href) {
        $.cookie("desktopSite", null, {
            path: path,
            expires: -10
        });
        location.href=href;
        return false;
    },
    
    isMobileUserAgent: function() {
        var mobileUserAgent = false;
        (function(a){if(/android|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(ad|hone|od)|iris|kindle|lge |maemo|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|e\-|e\/|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|xda(\-|2|g)|yas\-|your|zeto|zte\-/i.test(a.substr(0,4)))mobileUserAgent=true;})(navigator.userAgent||navigator.vendor||window.opera);
        return mobileUserAgent;
    },
    
    directToMobileSite: function(url) {
        var mobileUserAgent = Mobile.isMobileUserAgent();
        if (mobileUserAgent && $.cookie("desktopSite") !== "true") {
            location.href = url;
        }
    },

    init: function() {
        // add extra space on top for ios7 status bar
        var isCordova = $.cookie("cordova") === "true";
        var updateStatusBar = isCordova && navigator.userAgent.match(/iphone|ipad|ipod/i) && parseInt(navigator.appVersion.match(/OS (\d)/)[1], 10) >= 7;
        if (updateStatusBar) {
            document.body.style.webkitTransform = 'translate3d(0, 20px, 0)';
        }
    },

    initPage: function() {
        Mobile.init();
        
        // change form action URL
        $("form.form-container").each(function(index, el) {
            var action = $(el).attr("action");
            action = action.replace("/userview/", "/mobile/");
            $(el).attr("action", action);
        });
        // change form buttons
        var newButtons = $("<ul></ul>");
        $("form.form-container #section-actions .form-cell").each(function(index, el) {
            $(newButtons).append($("<li></li>").append(el));
        });
        if (newButtons) {
            newButtons = $("<div id='navbar' data-role='navbar' data-position='fixed'></div>").append(newButtons);
            $("#form-canvas form").append(newButtons);
            $("form.form-container #section-actions").remove();
        }
        // change cancel button changes
        $("#cancel").each(function(index, el) {
            var s = new String($(el).attr("onclick"));
            var m = s.match(/'(?:[^'\\]|\\.)*'/);
            if (m !== null) {
                m = new String(m).replace(/'/g, "");
                $(el).removeAttr("onclick");
                var switchPage = function(e) {
                    $.mobile.changePage( m, {
                        transition: "slide", 
                        reverse:true
                    } );
                    e.preventDefault();
                    return false;
                };
                $(el).click(switchPage);
            }
        });
        // change list link URLs
        $("a[target]").each(function(index, el) {
            var target = $(el).attr("target");
            if (target === "_self") {
                $(el).removeAttr("target");
            }
        });
        var thisWindow = window;
        
        // disable ajax for forms with file uploads
        var uploadFields = $("input[type='file']");
        if (uploadFields.length > 0) {
            // disable ajax form submission
            var parentForm =  uploadFields.parents("form");
            parentForm.attr("data-ajax", "false");
            parentForm.attr("action", parentForm.attr("action") + "&" + ConnectionManager.tokenName + "=" + ConnectionManager.tokenValue);
            parentForm.prepend('<input name="'+ConnectionManager.tokenName+'" value="'+ConnectionManager.tokenValue+'" type="hidden"/>');
        }
        // disable ajax for file upload links
        $(".form-fileupload-value a").attr("data-ajax", "false");
        $(".form-fileupload-value a").attr("target", "_blank");
    },
    
    initDataList: function() {
        $("span.rowCount").each(function(index, el) {
            $(el).attr("class", "ui-li-count");
            var count = $(el).text();
            var newContent = count.substr(1, count.length - 2);
            $(el).text(newContent);
        });
        $("a.menu-link").each(function(index, el) {
            var href = $(el).attr("href");
            href = new String(href).replace("/userview/", "/mobile/");
            $(el).attr("href", href);
        });      
    },

    updateCache: function() {
        if (typeof applicationCache != "undefined") {
            // Check if a new cache is available on page load.
            window.addEventListener('load', function(e) {

                window.applicationCache.addEventListener('updateready', function(e) {
                    $.mobile.loading('hide');
                    if (window.applicationCache.status === window.applicationCache.UPDATEREADY) {
                        // Browser downloaded a new app cache, swap it in and reload the page to get the new hotness.
                        window.applicationCache.swapCache();
                        window.location.reload();
                    } else {
                        // Manifest didn't change. Nothing new to server.
                    }
                }, false);

                window.applicationCache.addEventListener('checking', function(e) {
                    $.mobile.loading('show');
                }, false);            

                window.applicationCache.addEventListener('downloading', function(e) {
                    $.mobile.loading('show');
                }, false);            

                window.applicationCache.addEventListener('cached', function(e) {
                    $.mobile.loading('hide');
                }, false);            

                window.applicationCache.addEventListener('noupdate', function(e) {
                    $.mobile.loading('hide');
                }, false);            

                window.applicationCache.addEventListener('error', function(e) {
                    $.mobile.loading('hide');
                }, false);            

                window.applicationCache.addEventListener('obsolete', function(e) {
                    $.mobile.loading('hide');
                }, false);            

            }, false);            
        }
    },
    
    checkNetworkStatus: function() {
        var pollDelay = 30000;
        if (navigator.onLine) {
            $.ajax({
                async: true,
                cache: false,
                error: function (req, status, ex) {
                    Mobile.showNetworkStatus(false);
                    setTimeout(Mobile.checkNetworkStatus, pollDelay);
                },
                success: function (data, status, req) {
                    Mobile.showNetworkStatus(true);
                    setTimeout(Mobile.checkNetworkStatus, pollDelay);
                },
                timeout: 5000,
                type: "GET",
                url: Mobile.contextPath + "/images/v3/clear.gif"
            });
        } else {
            Mobile.showNetworkStatus(false);
            setTimeout(Mobile.checkNetworkStatus, pollDelay);
        }
    },
    
    showNetworkStatus: function(online) {
        if (online) {
            $("#online-status").html("");
            $("#online-status").css("display", "block");
            $("#online-status").css("background", "#00bb00");
        }
        else {
            $("#online-status").html("");
            $("#online-status").css("display", "block");
            $("#online-status").css("background", "red");
        }
    },
    
    logout: function() {
        if ($.cookie("all-apps") == "true") {
            $.ajax({
                type: 'POST',
                url: Mobile.contextPath + "/j_spring_security_logout",
                success: function(data) {
                    window.location.href = Mobile.contextPath + "/web/mobile/apps";
                }
            });
        } else {
            $.ajax({
                type: 'POST',
                url: Mobile.contextPath + "/j_spring_security_logout",
                success: function(data) {
                    window.location.href = Mobile.contextPath + "/web/mlogin";
                }
            });
        }
    }
};

$(document).bind("mobileinit", function(){
    $.mobile.defaultPageTransition = "slide";
    $.mobile.ajaxEnabled = false;
    $.mobile.autoInitializePage = false;
    $.mobile.touchOverflowEnabled = false;
});
$(document).on("pageshow", "div[data-role=page]", function() {
    Mobile.initPage();
    Mobile.checkNetworkStatus();
});
$(document).on("pagehide", "div[data-role=page]", function(event){
    $(event.target).remove();
});

$.ajaxPrefilter(function(options, originalOptions, jqXHR) {
    if (typeof applicationCache != "undefined" &&
            applicationCache.status !== applicationCache.UNCACHED &&
            applicationCache.status !== applicationCache.OBSOLETE
        ) {
        options.isLocal = true;
    }
});

$(function() {
    Mobile.init();    
})
