$(document).ready(function() {
    if ((typeof _enableResponsiveTable) !== "undefined" && _enableResponsiveTable) {
        $(".dataList table").each(function(){
            responsiveTable($(this));
        });
    }
});
/* ---------- Responsive Table -------------- */
function responsiveTable(table) {
    var cols = table.find("th").filter(":not('.select_radio, .select_checkbox, .row_action, .column-hidden')");
    var hiddenCols = table.find("th.column-hidden");
    var select = table.find("th.select_radio, th.select_checkbox");
    var rowAction = table.find("th.row_action");
    //hide all columns by default to phone & tablet
    cols.data("hide", "phone,tablet");

    //show 4 column if it is tablet
    cols.filter(":lt(4)").data("hide", "phone");

    //show 1 column if it is phone
    cols.filter(":eq(0)").data("hide", "");

    //checkbox & radio
    select.data("hide", "all");
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
    
    var buttons = table.parent().find(".footable-buttons");
    var filters = table.parent().parent().find(".filters");
    
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

    table.footable(_fooTableArgs);
}