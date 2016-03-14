<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header id="runApps" />

<script>
    var loadApps = function(container, baseUrl) {
        container = container || "#published-apps";
        baseUrl = baseUrl || "";

        // show loading icon
        $(container).empty();
        var loading = $('<div id="apps-loading"><i class="icon-spinner icon-spin icon-2x"></i> <fmt:message key="appCenter.label.loadingApps"/></div>');
        $(container).append(loading);

        // load JSON
        $.ajax({ 
            url : baseUrl + "${pageContext.request.contextPath}/web/json/apps/published/userviews",
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
                        content +=  '<div class="column">\
                                        <div class="appdiv">\
                                        <a class="screenshot" target="_blank" href="' + baseUrl + uv.url + '">\
                                            <div class="screenshot_label">' + UI.escapeHTML(uv.name) + '</div>\
                                            <img src="${pageContext.request.contextPath}/web/userview/screenshot/' + app.id + '/' + uv.id + '" width="150" border="0" />\
                                        </a><h3>' + UI.escapeHTML(app.name) + '</h3></div>\
                                    </div>';
                    }
                }

                // show apps, hide loading icon
                $(loading).remove();
                $(container).append($(content));
            }
        });
    }            
    jQuery.expr[':'].Contains = function(a,i,m){ 
        return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase())>=0; 
    };  
    var searchFilter = function(header, list) { 
        var timer;
        var form = $("<form>").attr({"class":"filterform","action":"#","onsubmit":"return false"}), 
        input = $("<input>").attr({"class":"filterinput","type":"text"}); 
        $(form).append(input).append($("<span class='filterlabel'><i class='icon-search'></i></span>")).appendTo(header);  
        $(form).submit(function() { return false });
        $(input).change(function () { 
            var filter = $(this).val(); 
            if(filter) { 
                $(list).find("a:not(:Contains(" + filter + ")),h3:not(:Contains(" + filter + "))").parent().parent().fadeOut(); 
                $(list).find("a:Contains(" + filter + "),h3:Contains(" + filter + ")").parent().parent().show(); 
            } else { 
                $(list).find("div").show(); 
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
    }  
    var loadPublishedApps = function() {
        $(".published-apps").empty();
        $("#categories a").removeClass("category-selected");
        $("#category-published-apps").addClass("category-selected");
        $("#published-apps").show();
        $("#title form").remove();
        searchFilter($("#title"), $("#published-apps")); 
        loadApps("#published-apps");
    }
    $(function () { 
        loadPublishedApps();
    }); 
</script>

<div id="nav">
    <div id="nav-title">

    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="subMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
        </ul>
    </div>
    <div id="main-body">

            <div id="title">
                <fmt:message key="appCenter.label.publishedApps"/>
            </div>
        
            <div id="published-apps" class="published-apps"></div>
            <div class="clear"></div>
        
    </div>
</div>

<script>
    Template.init("#menu-run", "#nav-run-apps");
</script>

<commons:footer />
