$(document).ready(function() {
    if ((typeof _enableResponsiveTable) !== "undefined" && _enableResponsiveTable) {
        $(".dataList table").each(function(){
            responsiveTable($(this));
        });
    }
});
/* ---------- Responsive Table -------------- */
function responsiveTable(table) {
    var respButtons = table.closest(".dataList").find(".footable-buttons");
    if ($(respButtons).data("disableresponsive") === true) {
        return;
    }
    if ($(respButtons).data("searchpopup") === true) {
        $(respButtons).find(".search_trigger i").remove();
    }
    
    var responsiveSetting = null;
    if ($(respButtons).data("responsivejson") !== "" && $(respButtons).data("responsivejson") !== undefined) {
        try {
            responsiveSetting = eval($(respButtons).data("responsivejson"));
        } catch (err) {}
    }
    var phoneCols = 1;
    var phoneVisibleCols = [];
    var tabletCols = 4;
    var tabletVisibleCols = [];
    
    if (responsiveSetting !== null) {
        if (responsiveSetting[0].columns !== "") {
            if (!isNaN(responsiveSetting[0].columns)) {
                try {
                    phoneCols = parseInt(responsiveSetting[0].columns);
                }catch (err){
                }
            } else {
                var temp = responsiveSetting[0].columns.split(";");
                for (var i in temp) {
                    if (table.find("th.column_"+temp[i]).length > 0) {
                        phoneVisibleCols.push(temp[i]);
                    }
                }
            }
        }
        if (responsiveSetting[1].columns !== "") {
            if (!isNaN(responsiveSetting[1].columns)) {
                try {
                    tabletCols = parseInt(responsiveSetting[1].columns);
                }catch (err){
                }
            } else {
                var temp = responsiveSetting[1].columns.split(";");
                for (var i in temp) {
                    if (table.find("th.column_"+temp[i]).length > 0) {
                        tabletVisibleCols.push(temp[i]);
                    }
                }
            }
        }
    }
    
    var cols = table.find("th").filter(":not('.select_radio, .select_checkbox, .row_action, .column-hidden')");
    var hiddenCols = table.find("th.column-hidden");
    var select = table.find("th.select_radio, th.select_checkbox");
    var rowAction = table.find("th.row_action");
    //hide all columns by default to phone & tablet
    cols.data("hide", "phone,tablet");

    //show 4 column if it is tablet
    if (tabletVisibleCols.length === 0) {
        cols.filter(":lt("+tabletCols+")").data("hide", "phone");
    } else {
        for (var i in tabletVisibleCols) {
            cols.filter(".column_"+tabletVisibleCols[i]).data("hide", "phone");
        }
    }

    //show 1 column if it is phone
    if (phoneVisibleCols.length === 0) {
        cols.filter(":lt("+phoneCols+")").data("hide", "");
    } else {
        for (var i in phoneVisibleCols) {
            cols.filter(".column_"+phoneVisibleCols[i]).data("hide", "");
        }
    }

    //checkbox & radio
    select.data("hide", "phone,tablet");
    select.data("ignore", true);
    select.filter(":eq(0)").data("hide", "");
    select.css("width", "40px");

    //hide all row action from phone and tablet and add it back by event
    rowAction.data("hide", "phone,tablet");
    rowAction.data("value", "jq_dlist_row_action");

    hiddenCols.data("hide", "all");
    hiddenCols.data("ignore", true);

    //wrap all row action in data in extra .class so that it can be differentia and remove later
    table.find("td.row_action").wrapInner('<span class="row_action_inner"></span>');

    //add row action into detail view with better layout
    table.on("footable_row_detail_updated", function (event) {
        //remove row action from detail
        event.detail.find(".row_action_inner").each(function(){
            $(this).parent().remove();
        });

        var actions = $('<div class="footable-row-detail-cell-actions"></div>');
        event.row.find(".row_action").each(function(){
            var html = $(this).html();
            actions.append(html);
        });
        actions.find("a").addClass("form-button button btn btn-xs");
        event.detail.find(".footable-row-detail-cell").append(actions);
    });
    
    var buttons = table.closest(".dataList").find(".footable-buttons");
    var filters = table.closest(".dataList").find(".filters");
    
    table.on("footable_breakpoint", function (event) {
        if($(this).hasClass("breakpoint")) {
            buttons.show();
            filters.hide();
        } else {
            buttons.hide();
            filters.show();
        }
    });
    
    buttons.find(".expandAll").click(function(){
        table.find("tr:not(.footable-detail-show) .footable-toggle").trigger("click");
        return false;
    });
    
    buttons.find(".collapseAll").click(function(){
        table.find("tr.footable-detail-show .footable-toggle").trigger("click");
        return false;
    });
    
    if ($(respButtons).data("searchpopup") === true) {
        var tableId = table.attr("id");
        var searchPopup = new Boxy(
            '<div id="'+tableId+'_filterpopup" class="search_filter_popup"></div>',
            {
                title: '',
                closeable: true,
                draggable: true,
                show: false,
                fixed: true,
                modal: true,
                afterShow : function() {
                    $('.boxy-modal-blackout').off('click');
                    $('.boxy-modal-blackout').on('click',function(){
                        searchPopup.hide();
                        $('.boxy-modal-blackout').off('click');
                    });
                },
                afterHide : function() {
                    $("#"+tableId+"_filterpopup").find("form .filters").hide();
                    $("#"+tableId).closest(".dataList").find("form").before($("#"+tableId+"_filterpopup").find("form"));
                }
            }
        );
        
        buttons.find(".search_trigger").click(function(){
            $("#"+tableId+"_filterpopup").append($("#"+tableId).closest(".dataList").find(".filter_form"));
            $("#"+tableId+"_filterpopup").find("form .filters").show();
            searchPopup.show();
            searchPopup.center('x');
            searchPopup.center('y');
        });
    } else {
        buttons.find(".search_trigger").click(function(){
            if ($(this).hasClass("filter_show")) {
                filters.hide();
                $(this).removeClass("filter_show");
            } else {
                filters.show();
                $(this).addClass("filter_show");
            }
            return false;
        });
    }

    //toggle
    cols.filter(":eq(0)").data("toggle", "true");
    
    var _fooTableArgs = {
        breakpoints: { // The different screen resolution breakpoints
            phone: 480,
            tablet: 600
        }
    };
    
    if ((typeof _customFooTableArgs) !== "undefined") {
        _fooTableArgs = _customFooTableArgs;
    }
    
    if (responsiveSetting !== null) {
        if (responsiveSetting[0].breakpoint !== "") {
            try {
                var phone = parseInt(responsiveSetting[0].breakpoint);
                _fooTableArgs['breakpoints']['phone'] = phone;
            }catch (err){}
        }
        if (responsiveSetting[1].breakpoint !== "") {
            try {
                var tablet = parseInt(responsiveSetting[1].breakpoint);
                _fooTableArgs['breakpoints']['tablet'] = tablet;
            }catch (err){}
        }
    }

    table.footable(_fooTableArgs);
}