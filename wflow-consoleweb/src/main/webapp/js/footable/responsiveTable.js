$(document).ready(function() {
    $(".dataList table").each(function(){
        responsiveTable($(this));
        $(this).closest(".dataList").show();
    });
});
/* ---------- Responsive Table -------------- */
function responsiveTable(table) {
    var respButtons = table.closest(".dataList").find(".footable-buttons");
    if ($(respButtons).data("disableresponsive") === true) {
        return;
    }
    if ($(respButtons).data("searchpopup") === true) {
        $(respButtons).find(".search_trigger i").remove();
        var filters = table.closest(".dataList").find(".filters");
        $(filters).hide();
        
        var tableId = $(table).attr("id");
        if ($("#"+tableId+"_filterpopup").length > 0) {
            $("#"+tableId+"_filterpopup").closest("table.boxy-wrapper").remove();
        }
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
        $(respButtons).find(".search_trigger").off("click");
        $(respButtons).find(".search_trigger").on("click", function(){
            $("#"+tableId+"_filterpopup").append($("#"+tableId).closest(".dataList").find(".filter_form"));
            $("#"+tableId+"_filterpopup").find("form .filters").show();
            searchPopup.show();
            searchPopup.center('x');
            searchPopup.center('y');
        });
    } else {
        var filters = $(table).closest(".dataList").find(".filters");
        $(filters).hide();
        
        $(respButtons).find(".search_trigger").off("click");
        $(respButtons).find(".search_trigger").on("click", function(){
            if ($(this).hasClass("filter_show")) {
                $(filters).hide();
                $(this).removeClass("filter_show");
            } else {
                $(filters).show();
                $(this).addClass("filter_show");
            }
            return false;
        });
    }
    
    if ($(respButtons).data("responsiveclass") !== undefined && $(respButtons).data("responsiveclass").trim() !== "") {
        table.closest(".dataList").addClass($(respButtons).data("responsiveclass"));
        
        if (table.closest(".dataList").hasClass("card-label")) {
            initCardLabelLayout(table);
        }
        if (table.hasClass('lg-card') || table.hasClass('md-card') || table.hasClass('sm-card')) {
            $(table).find("tbody tr.space").remove();
            for(var i = 0; i < 10; i++) {
                $(table).find("tbody").append('<tr class="space"></tr>');
            }
        }
        
        var resize = function (event) {
            setTimeout(function(){
                $(".dataList table").each(function(){
                    var width = $(window).width();
                    var table = $(this);
                    if (table.closest(".dataList").parent().closest(".dataList").length > 0) {
                        width = table.closest(".dataList").parent().width();
                    }

                    table.closest(".dataList").removeClass("card-layout-active");
                    if ((width < 768 && table.closest(".dataList").hasClass("sm-card")) 
                            || (width < 992 && table.closest(".dataList").hasClass("md-card"))
                            || (table.closest(".dataList").hasClass("lg-card"))) {
                        table.closest(".dataList").addClass("card-layout-active");
                    }
                    if (width < 992) {
                        $(respButtons).find(".search_trigger").removeClass("filter_show");
                        $(filters).hide();
                        $(respButtons).show();
                        $(respButtons).find(".footable-button").hide();
                    } else {
                        $(respButtons).hide();
                        $(filters).show();
                    }
                });
            }, 10);
        };
        
        $(window).off("resize.responsive-table");
        $(window).on("resize.responsive-table", resize);
        resize();
        
        return;
    }
    var responsiveSetting = null;
    if ($(respButtons).data("responsivejson") !== "" && $(respButtons).data("responsivejson") !== undefined) {
        try {
            responsiveSetting = eval($(respButtons).data("responsivejson"));
        } catch (err) {}
    }
    initFooTable(table, respButtons, responsiveSetting);
}

function initCardLabelLayout(table) {
    var headers = $(table).find("thead th");
    $(table).find("tbody tr").each(function(){
        var row = $(this);
        var i = 0;
        $(row).find("td").each(function(){
            if (!$(this).is(".select_checkbox, .select_radio, .gap, .row_action")) {
                var cell = $('<td class="card_layout_body_cell"></td>');
                
                if ($(headers[i]).attr("data-cbuilder-classname") !== undefined) {
                    cell.removeAttr("data-cbuilder-classname");
                    cell.removeAttr("data-cbuilder-id");
                }
                
                var label = $('<div></div>')
                                .html($(headers[i]).html())
                                .attr("class", $(headers[i]).attr("class"))
                                .attr("style", $(headers[i]).attr("style"))
                                .removeClass("sortable");
                
                if (label.find("a").length > 0) {
                    label.append(label.find("a").text());
                    label.find("a").remove();
                }
                
                cell.append(label);
                cell.prepend(label.find(".overlay"));
                
                var value = $('<div></div>')
                                .html($(this).html())
                                .attr("class", $(this).attr("class"))
                                .attr("style", $(this).attr("style"));
                value.css("display", "");
                cell.append(value);   
                
                if ($(this).attr("data-cbuilder-select") !== undefined) {
                    cell.attr("data-cbuilder-select", $(this).attr("data-cbuilder-select"));
                }
                
                //for builder
                if ($(headers[i]).attr("data-cbuilder-mobile-invisible") !== undefined) {
                    label.attr("data-cbuilder-mobile-invisible", "");
                }
                if ($(headers[i]).attr("data-cbuilder-tablet-invisible") !== undefined) {
                    label.attr("data-cbuilder-tablet-invisible", "");
                }
                if ($(headers[i]).attr("data-cbuilder-desktop-invisible") !== undefined) {
                    label.attr("data-cbuilder-desktop-invisible", "");
                }
                if ($(this).attr("data-cbuilder-mobile-invisible") !== undefined) {
                    value.attr("data-cbuilder-mobile-invisible", "");
                }
                if ($(this).attr("data-cbuilder-tablet-invisible") !== undefined) {
                    value.attr("data-cbuilder-tablet-invisible", "");
                }
                if ($(this).attr("data-cbuilder-desktop-invisible") !== undefined) {
                    value.attr("data-cbuilder-desktop-invisible", "");
                }
                
                $(this).before(cell);
            }
            i++;
        });
    });
}

function initFooTable(table, respButtons, responsiveSetting) {
    var phoneCols = 1;
    var phoneVisibleCols = [];
    var tabletCols = 4;
    var tabletVisibleCols = [];
    
    if (responsiveSetting !== undefined && responsiveSetting !== null) {
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
    
    var cols = $(table).find("th").filter(":not('.select_radio, .select_checkbox, .row_action, .column-hidden')");
    var hiddenCols = $(table).find("th.column-hidden");
    var select = $(table).find("th.select_radio, th.select_checkbox");
    var rowAction = $(table).find("th.row_action");
    //hide all columns by default to phone & tablet
    $(cols).data("hide", "phone,tablet");
    
    $(cols).each(function () {
        $(this).data("hide", "phone,tablet");
        if ($(this).find("script, style").length > 0) {
            var temp = $('<div>'+$(this).html()+'</div>');
            temp.find("script, style").remove();
            
            $(this).data("name", temp.text());
        }
    });
    
    //show 4 column if it is tablet
    if (tabletVisibleCols.length === 0) {
        $(cols).filter(":lt("+tabletCols+")").each(function () {
            $(this).data("hide", "phone");
        });
    } else {
        for (var i in tabletVisibleCols) {
            $(cols).filter(".column_"+tabletVisibleCols[i]).each(function () {
                $(this).data("hide", "phone");
            });
        }
    }

    //show 1 column if it is phone
    if (phoneVisibleCols.length === 0) {
        $(cols).filter(":lt("+phoneCols+")").each(function () {
            $(this).data("hide", "");
        });
    } else {
        for (var i in phoneVisibleCols) {
            $(cols).filter(".column_"+phoneVisibleCols[i]).each(function () {
                $(this).data("hide", "");
            });
        }
    }

    //checkbox & radio
    $(select).data("hide", "phone,tablet");
    $(select).data("ignore", true);
    $(select).filter(":eq(0)").each(function () {
        $(this).data("hide", "");
    });
    $(select).css("width", "40px");

    //hide all row action from phone and tablet and add it back by event
    $(rowAction).each(function () {
        $(this).data("hide", "phone,tablet");
        $(this).data("value", "jq_dlist_row_action");
    });
    
    $(hiddenCols).each(function () {
        $(this).data("hide", "all");
        $(this).data("ignore", true);
    });

    //wrap all row action in data in extra .class so that it can be differentia and remove later
    $(table).find("td.row_action:not(.row_action_container)").wrapInner('<span class="row_action_inner"></span>');

    //add row action into detail view with better layout
    $(table).off("footable_row_detail_updated");
    $(table).on("footable_row_detail_updated", function (event) {
        //remove row action from detail
        $(event.detail).find(".row_action_inner").each(function(){
            $(this).parent().parent().remove();
        });
        
        //copy css classes
        var rowCols = $(event.row).find("td:not(.footable-visible)");
        var rowHeaders = $(event.row).closest("table").find("th:not(.footable-visible)");
        
        var i = 0;
        $(event.detail).find(".footable-row-detail-row").each(function(){
            if ($(rowCols[i]).attr("data-cbuilder-select") !== undefined) {
                $(this).attr("data-cbuilder-select", $(rowCols[i]).attr("data-cbuilder-select"));
            }
            if ($(rowHeaders[i]).find('[data-cbuilder-element-invisible]').length > 0) {
                $(this).find(".footable-row-detail-name").prepend("<span class=\"anchor\"><span data-cbuilder-element-invisible></span></span>");
                $(this).find('[data-cbuilder-element-invisible]').css({
                    "width" : $(this).width() + "px",
                    "height" : $(this).height() + "px",
                    "position" : "absolute",
                    "min-height" : "auto"
                });
            }
            $(this).find(".footable-row-detail-name").addClass($(rowHeaders[i]).attr("class"));
            $(this).find(".footable-row-detail-value").addClass($(rowCols[i]).attr("class"));
            
            i++;
        });

        var actions = $('<div class="footable-row-detail-cell-actions"></div>');
        $(event.row).find(".row_action").each(function(){
            var html = $(this).html();
            $(actions).append(html);
            $(actions).find(".row_action_inner > div").attr("style", "");
        });
        $(actions).find("a").addClass("form-button button btn btn-xs");
        $(event.detail).find(".footable-row-detail-cell").append(actions);
    });
    
    var buttons = table.closest(".dataList").find(".footable-buttons");
    var filters = table.closest(".dataList").find(".filters");
    
    $(table).off("footable_breakpoint");
    $(table).on("footable_breakpoint", function (event) {
        if($(this).hasClass("breakpoint")) {
            $(buttons).find(".search_trigger").removeClass("filter_show");
            buttons.show();
            filters.hide();
            
            if ($(table).hasClass("expandfirst")) {
                $(table).find("tbody tr:eq(0):not(.footable-detail-show) .footable-toggle").attr("data-cbuilder-unselectable", "").trigger("click");
            }
        } else {
            buttons.hide();
            filters.show();
        }
    });
    $(table).trigger("footable_breakpoint");
    
    $(buttons).find(".expandAll").off("click");
    $(buttons).find(".expandAll").on("click", function(){
        $(table).find("tr:not(.footable-detail-show) .footable-toggle").trigger("click");
        return false;
    });
    
    $(buttons).find(".collapseAll").off("click");
    $(buttons).find(".collapseAll").on("click", function(){
        $(table).find("tr.footable-detail-show .footable-toggle").trigger("click");
        return false;
    });

    //toggle
    $(cols).filter(":eq(0)").data("toggle", "true");
    
    var _fooTableArgs = {
        breakpoints: { // The different screen resolution breakpoints
            phone: 480,
            tablet: 600
        }
    };
    
    if ((typeof _customFooTableArgs) !== "undefined") {
        _fooTableArgs = _customFooTableArgs;
    }
    
    if (responsiveSetting !== undefined && responsiveSetting !== null) {
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
    $(table).footable(_fooTableArgs);
}