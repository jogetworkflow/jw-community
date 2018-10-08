jQuery.expr[':'].Contains = function(a,i,m){ 
    return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase())>=0; 
};
var AppCenter = {
    searchFilter: function(header, list) { 
        var timer;
        var form = $("<form>").attr({"class":"filterform","action":"#","onsubmit":"return false"}), 
        input = $("<input>").attr({"class":"filterinput","type":"text"}); 
        $(form).append(input).append($("<span class='filterlabel'><i class='icon-search fa fa-search'></i></span>")).appendTo(header);  
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
        AdminBar.showQuickOverlay(UI.base + "/web/console/app/" + appId + "/" + appVersion + "/forms");
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
            dataType:'jsonp',
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
                            content += "<span class='app-design-button' onclick='AppCenter.designApp(event,\"" + app.id + "\",\"" + app.version + "\",\"" + uv.id + "\");return false'><i class='fa fa-pencil icon-pencil'></i></span>";
                        }
                        content += '</a>\
                                    </li>';
                    }
                }
                // show apps, hide loading icon
                $(loading).remove();
                $(container).append($(content));

                // sort by userview name
                var appsLi = $(container).find("li");
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

                HelpGuide.reposition();     
                
                // check if within IFRAME
                if (window.self === window.top) {
                    $("span.userview-icon").hover(
                        function() {
                            var summary = $(this).siblings("div.app-name").text();
                            if (summary !== "") {
                                $(this).prepend("<div class='userview-summary'>" + summary + "</div>");
                                $(this).find(".userview-summary").show("fast");
                            }
                        },
                        function() {
                            $(this).find(".userview-summary").hide("fast").remove();
                        }
                    );
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
    }
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
});
//AppCenter.searchFilter($("#search"), $("#apps")); 
//AppCenter.loadPublishedApps("#apps");
