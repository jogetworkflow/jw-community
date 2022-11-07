$(document).ready(function() {
    $('body').on('DOMSubtreeModified', function(){
        if ($('#dataList_list_appList').length > 0) {
            $('#dataList_list_appList .column_img').children('div').each(function () {
                var style = $(this).attr('style');
                $(this).parents(".data-row").attr('style', style);
            });
        }
    });
    if (window["ajaxContentPlaceholder"] !== undefined) {
        window["ajaxContentPlaceholder"][UI.base + '/web/userview/appcenter/home/_/home'] = "dashboard";
        window["ajaxContentPlaceholder"][UI.base + '/web/userview/appcenter/home/_/templates'] = "dashboard";
        window["ajaxContentPlaceholder"][UI.base + '/web/userview/appcenter/home/_/admin'] = "dashboard";
    }
});


