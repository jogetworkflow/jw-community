$(document).ready(function() {
    $('body').off('page_loaded.appcenter');
    $('body').on('page_loaded.appcenter', function(){
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
