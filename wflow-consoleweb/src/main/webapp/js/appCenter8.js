$(document).ready(function() {
    $('body').off('DOMSubtreeModified');
    $('body').on('DOMSubtreeModified', function(){
        if ($('#dataList_list_appList').length > 0) {
            $('#dataList_list_appList .column_img').children('div').each(function () {
                var style = $(this).attr('style');
                $(this).parents(".data-row").attr('style', style);
            }); 
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
        }
    });
    if (window["ajaxContentPlaceholder"] !== undefined) {
        window["ajaxContentPlaceholder"][UI.base + '/web/userview/appcenter/home/_/home'] = "dashboard";
        window["ajaxContentPlaceholder"][UI.base + '/web/userview/appcenter/home/_/templates'] = "dashboard";
        window["ajaxContentPlaceholder"][UI.base + '/web/userview/appcenter/home/_/admin'] = "dashboard";
    }
    $('#loginForm > table > tbody > tr:nth-child(3) > td:nth-child(2) > input').val('Sign In');
});


