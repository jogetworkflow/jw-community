AjaxMenusCount = {
    /*
     * Intialize the content to find menu with ajax count
     */
    init : function() {
        if ($("[data-ajaxmenucount]").length > 0) {
            var headers = new Headers();
            headers.append(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            headers.append("__ajax_menu_count", "true");
            
            var args = {
                method : "GET",
                headers: headers
            };
            
            var url = window.location.href;
            
            //handle for UI builder preview
            if (url.indexOf("/web/console/app/") !== -1 && url.indexOf("/userview/builderPreview/") !== -1) {
                $("[data-ajaxmenucount]").text("0");
                return;
            }
            
            //add a param to prevent it override cache
            if (url.indexOf("?") === -1) {
                url += "?__ajax_menu_count=1";
            } else {
                url += "&__ajax_menu_count=1";
            }
        
            fetch(url, args)
            .then(function (response) {
                return response.text();
            })
            .then(function (data){
                if (data !== null) {
                    AjaxMenusCount.update(data);
                }
            });
        }
    },
    
    update : function(data) {
        var menus = $(data);
        $("[data-ajaxmenucount]").each(function(){
            var id = $(this).data("ajaxmenucount");
            if ($(menus).find("#"+id).length > 0) {
                var count = $(menus).find("#"+id+" .rowCount").text();
                $(this).text(count);
            }
        });
        if (window['Analyzer'] !== undefined && $(menus).find("#ajaxAnalyzerJson").length > 0) {
            var analyzerJson = $(html).find("#ajaxAnalyzerJson").val();
            var analyzer = JSON.parse(analyzerJson);
            Analyzer.initAnalyzer(analyzer);
        }
    }
};

$(function(){
    AjaxMenusCount.init();
});
