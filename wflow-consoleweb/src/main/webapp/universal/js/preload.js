!function ($) {

    var hideTimeout = setTimeout(function() {
        $(".page-loader").hide();
    }, 5000);
    $(document).ready(function() {
        clearTimeout(hideTimeout);
        $(".page-loader").hide();
    });

    // show page loader on leaving page
    $(window).on('beforeunload', function() {
        if ($("html.ismobile").length > 0) {
            $(".page-loader").show();
        }
    });    
    
}(window.jQuery);
