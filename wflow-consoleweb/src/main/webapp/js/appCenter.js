jQuery.expr[':'].Contains = function(a,i,m){ 
    return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase())>=0; 
};
var AppCenter = {
    searchFilter: function(header, list) { 
        var timer;
        var form = $("<form>").attr({"class":"filterform","action":"#","onsubmit":"return false"}), 
        input = $("<input>").attr({"class":"filterinput","type":"text"}); 
        $(form).append(input).append($("<span class='filterlabel'><i class='icon-search'></i></span>")).appendTo(header);  
        $(form).submit(function() { return false });
        $(input).change(function () { 
            var filter = $(this).val(); 
            if(filter) { 
                $(list).find(".app-name:not(:Contains(" + filter + "))").closest("li").fadeOut(); 
                $(list).find(".app-name:Contains(" + filter + ")").closest("li").show(); 
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
        $(input).focus();
    },
    designApp: function(event,appId, appVersion, userviewId) {
        AdminBar.showQuickOverlay(UI.base + "/web/console/app/" + appId + "/" + appVersion + "/forms");
        event.preventDefault();
        event.stopPropagation();
    }, 
    loadPublishedApps: function(container, customUrl) {
        container = container || "#apps";
        var isMarketplace = (container === '#marketplace-apps');
        var jsonUrl = (customUrl) ? customUrl : UI.base + "/web/json/apps/published/userviews?appCenter=true";

        // show loading icon
        $(container).empty();
        var loading = $('<div id="apps-loading"><i class="icon-spinner icon-spin icon-2x"></i></div>');
        $(container).append(loading);

        // load JSON
        $.ajax({ 
            url : jsonUrl,
            dataType:'jsonp',
            success:function(data) {
                var content = "";
                var apps = data.apps;
                for( var i=0; i<apps.length; i++) {
                    // add app
                    var app = apps[i];
                    // add userviews
                    var userviews = app.userviews;
                    for( var j=0; j<userviews.length; j++){
                        var uv = userviews[j];
                        content += '<li>';
                        var userviewUrl = uv.url;
                        var imageUrl = uv.imageUrl;
                        if (!imageUrl) {
                            imageUrl = UI.base + '/web/userview/screenshot/' + app.id + '/' + uv.id;
                        }
                        content += '<a class="app-link" target="_blank" href="' + userviewUrl + '">\
                                            <span class="app-icon"><img src="' + imageUrl + '" width="240" border="0"></span>\
                                            <div class="app-name">' + uv.name + '</div>\
                                            <div class="app-description">' + app.name + '</div>\
                                            <div class="uv-description" style="display:none">' + uv.description + '</div>';
                        if ($("#adminControl").length > 0) {
                            content += "<span class='app-design-button' onclick='designApp(event,\"" + app.id + "\",\"" + app.version + "\",\"" + uv.id + "\");return false'><i class='icon-pencil'></i></span>";
                        }
                        content += '</a>\
                                    </li>';
                    }
                }
                // show apps, hide loading icon
                $(loading).remove();
                $(container).append($(content));

                // sort by userview name
                var appsLi = $(".published-apps li");
                appsLi.sort(function(a, b) {
                    var aName = $(a).find(".app-name").text();
                    var bName = $(b).find(".app-name").text();
                    if (aName.indexOf("<") === 0 || aName.indexOf("#") === 0 || aName.toUpperCase() >= bName.toUpperCase()) {
                        return 1;
                    } else {
                        return -1;
                    }
                });
                appsLi.detach().appendTo($(container));

                $("span.app-icon").hover(
                    function() {
                        var desc = $(this).siblings("div.app-description").text();
                        $(this).prepend("<div class='app-summary'>" + desc + "</div>");
                        $(this).find(".app-summary").show("fast");
                    },
                    function() {
                        $(this).find(".app-summary").hide("fast").remove();
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
            }
        });
    }
}
//AppCenter.searchFilter($("#search"), $("#apps")); 
//AppCenter.loadPublishedApps("#apps");
