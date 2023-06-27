$(document).ready(function() {
    $(".dataList").each(function(){
        if ($(this).find("> form > .table-wrapper > table.responsivetable").length > 0) {
            responsiveTable($(this));
        }
        if ($(this).find("> form > .table-wrapper > table.draggabletable").length > 0) {
            draggableTable($(this));
        }
        if ($(this).find("> form > .table-wrapper > table.showhidecolumns").length > 0) {
            showHideColumns($(this));
        }
        popupFilter($(this));
        $(this).show();
    });
    responsiveTemplate();
});
function popupFilter(datalist) {
    var filterForm = $(datalist).find('.filter_form');
    if ($(filterForm).data("searchpopup") === true) {
        var filters = $(filterForm).find(".filters");
        $(filters).hide();
        
        var trigger = $(filters).prev('a');
        $(trigger).parent().addClass("popup");
        
        var tableId = $(datalist).attr("id");
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

        //delay to override the original event
        setTimeout(function(){
            $(trigger).off("click");
            $(trigger).on("click", function(){
                $("#"+tableId+"_filterpopup").append($("#"+tableId).closest(".dataList").find(".filter_form"));
                $("#"+tableId+"_filterpopup").find("form .filters").show();
                searchPopup.show();
                searchPopup.center('x');
                searchPopup.center('y');
            });
        }, 10);
    }
}
function responsiveTemplate() {
    var resize = function (event) {
        setTimeout(function(){
            $(".dataList").each(function(){
                var dl = this;
                var mode = $(this).data("responsivemode");
                
                if (mode === "parent") {
                    var width = $(this).parent().width();
                    $(this).find('*[class]').each(function(){
                        updateClasses(this, dl, width);
                    });
                    updateClasses(this, dl, width);

                    $(this).removeClass("size_xl size_lg size_md size_sm");
                    if (width >= 960) {
                        $(this).addClass("size_xl");
                    } else if (width >= 720) {
                        $(this).addClass("size_lg");
                    } else if (width >= 540) {
                        $(this).addClass("size_md");
                    } else {
                        $(this).addClass("size_sm");
                    }
                } else {
                    var width = $(window).width();
                    $(this).removeClass("size_xl size_lg size_md size_sm");
                    if (width >= 992) {
                        $(this).addClass("size_xl");
                    } else if (width >= 768) {
                        $(this).addClass("size_lg");
                    } else if (width >= 576) {
                        $(this).addClass("size_md");
                    } else {
                        $(this).addClass("size_sm");
                    }
                }
            });
        }, 10);
    };

    $(window).off("resize.responsive-datalist");
    $(window).on("resize.responsive-datalist", resize);
    resize();
}
function updateClasses(el, datalist, width) {
    if (!$(el).closest(".dataList").is($(datalist))) {
        return;
    }
    
    var classes = $(el).attr('class');
                    
    //reset all classes
    classes = classes.replaceAll(/-xlxx/g, '-xl');
    classes = classes.replaceAll(/-lgxx/g, '-lg');
    classes = classes.replaceAll(/-mdxx/g, '-md');

    if (width < 960) {
        classes = classes.replaceAll(/-xl/g, '-xlxx');
    }
    if (width < 720) {
        classes = classes.replaceAll(/-lg/g, '-lgxx');
    }
    if (width < 540) {
        classes = classes.replaceAll(/-md/g, '-mdxx');
    }
    $(el).attr('class', classes);
}
function responsiveTable(datalist) {
    if ($(datalist).find("> form > .table-wrapper, > .table-wrapper").data("disableresponsive") === true) {
        $(datalist).find("> form > .table-wrapper > table.responsivetable, > .table-wrapper > table.responsivetable").removeClass("responsivetable");
        return;
    }
    
    var id = $(datalist).attr("id");
    
    var resize = function () {
        setTimeout(function(){
            $(".dataList#"+id).each(function(){
                var mode = $(this).data("responsivemode");
                var isMobile = false;
                if (mode === "parent") {
                    var width = $(datalist).parent().width();
                    isMobile = (width < 540);
                } else {
                    var width = $(window).width();
                    isMobile = (width < 576);
                }
                
                var table = $(datalist).find("> form > .table-wrapper > table.responsivetable, > .table-wrapper > table.responsivetable");
                
                if (isMobile) {
                    $(table).addClass("cardLayout");
                    $(table).find('> tbody > tr > td.column_body').each(function(){
                        var td = $(this);
                        if ($(td).find('> .cell-label').length === 0) {
                            //wrapping the column body content to cell-value
                            $(td).wrapInner('<div class="cell-value"></div>');
                            
                            //copy the column header text to cell label
                            $(td).prepend('<label class="cell-label"></label>');
                            var index = $(td).parent().find("td").index(td);
                            var th = $(table).find('> thead > tr > th:eq('+index+')');
                            if ($(th).find('style').length > 0) { //if there is style tag in the header
                                $(td).find('> .cell-label').html($(th).html());
                                $(td).find('> .cell-label').find('style').remove();
                                $(td).find('> .cell-label').text($(td).find('> .cell-label').text());
                            } else {
                                $(td).find('> .cell-label').text($(th).text());
                            }
                            
                            //move the column header class to the cell-label to make custom styling working
                            $(td).find('> .cell-label').addClass($(th).attr('class'));
                            
                            //move the column body class to the cell-value to make custom styling working
                            $(td).find('> .cell-value').addClass($(this).attr('class'));
                            $(td).attr("xclass", $(this).attr('class'));
                            $(td).attr("class", "");
                            
                            //for the user hided column
                            if ($(td).attr("xclass").indexOf("control_hide") !== -1) {
                                $(td).addClass("control_hide");
                            }
                            
                            //is list builder
                            if ($(th).is('[data-cbuilder-id]')) { 
                                $(td).attr("data-cbuilder-select", $(th).attr("data-cbuilder-id"));
                                $(td).attr("data-cbuilder-ignore-dragging", "");
                            }
                        }
                    });
                    
                    //is collasible setting is enabled
                    if ($(table).hasClass("cardCollapsible")) {
                        $(table).find('> tbody > tr').each(function(){
                            
                            //add toggle to the first row
                            var firstCell = $(this).find('> td[xclass]:eq(0)');
                            $(firstCell).addClass("collapsible_toggle_column");
                            if ($(firstCell).find('> span.collapsible_toggle').length === 0) {
                                $(firstCell).append('<span class="collapsible_toggle"><i class="fas fa-plus-square"></i><i class="fas fa-minus-square"></i></span>');
                            }
                        });
                        
                        //if collapsed by default, add collapsed class to all rows
                        if ($(table).hasClass("cardCollapseByDefault")) {
                            $(table).find('> tbody > tr').addClass("collapsed");
                        }
                        
                        //event handling for collapse/expand toggle
                        $(table).off("click.cardCollapsible_"+id, "span.collapsible_toggle");
                        $(table).on("click.cardCollapsible_"+id, "span.collapsible_toggle", function(){
                            $(this).closest("tr").toggleClass("collapsed");
                        });
                        
                        //show collapse/expand all buttons
                        $(".dataList#"+id).find('.collapsibleBtns').show();
                        $(".dataList#"+id).find(".collapsibleBtns .expandAll").off("click.cardCollapsible_"+id);
                        $(".dataList#"+id).find(".collapsibleBtns .collapseAll").off("click.cardCollapsible_"+id);
                        $(".dataList#"+id).find(".collapsibleBtns .expandAll").on("click.cardCollapsible_"+id, function(){
                            $(table).find('> tbody > tr').removeClass("collapsed");
                            return false;
                        });
                        $(".dataList#"+id).find(".collapsibleBtns .collapseAll").on("click.cardCollapsible_"+id, function(){
                            $(table).find('> tbody > tr').addClass("collapsed");
                            return false;
                        });
                    }
                } else {
                    //restore add remove all elements added for mobile
                    $(table).removeClass("cardLayout");
                    $(table).find('> tbody > tr').removeClass("collapsed");
                    $(table).find('> tbody > tr > td[xclass]:eq(0)').removeClass("collapsible_toggle_column");
                    $(table).find('> tbody > tr > td > span.collapsible_toggle').remove();
                    $(table).find('> tbody > tr > td[xclass]').each(function(){
                        var td = $(this);
                        $(td).attr("class", $(td).attr('xclass'));
                        $(td).removeAttr("xclass");
                        $(td).find('> .cell-label').remove();
                        $(td).find('> .cell-value').contents().unwrap();
                    });
                    
                    $(table).off("click.cardCollapsible_"+id, "span.collapsible_toggle");
                    
                    //hide collapse/expand all buttons
                    $(".dataList#"+id).find('.collapsibleBtns').hide();
                    $(".dataList#"+id).find(".collapsibleBtns .expandAll").off("click.cardCollapsible_"+id);
                    $(".dataList#"+id).find(".collapsibleBtns .collapseAll").off("click.cardCollapsible_"+id);
                }
            });
        }, 2);
    };

    $(window).off("resize."+id);
    $(window).on("resize."+id, resize);
    resize();
}
/* init the table to allow draggable columns */
function draggableTable(datalist) {
    
    var id = $(datalist).attr("id");
    
    //rearrange columns
    var key = window.location.pathname + $(datalist).closest(".main-body-content").attr('id') + "_order";
    var cache = localStorage.getItem(key);
    if (cache !== undefined && cache !== null) {
        rearrangeColumns(datalist, cache);
    }
    
    setTimeout(function(){
        var table = $(datalist).find("> form > .table-wrapper > table, > .table-wrapper > table");
        var headers = $(table).find('> thead > tr > th.column_header');
        
        $(headers).attr('draggable', true);
        
        $(headers).off("dragover.draggableTable");
        $(headers).on("dragover.draggableTable", function(event){
            $(table).find('.drop_left, .drop_right').removeClass("drop_left drop_right");
            if (!$(this).hasClass("current_dragging")) {
                var index = $(this).closest("tr").find(' > th').index($(this));
                
                event = event || window.event;
                var dragX = event.pageX;
                
                var pos = "drop_left";
                var offset = getOffset(this);
                if (dragX > (offset.left + ($(this).width()/3*2))) {
                    pos = "drop_right";
                }
                
                $(table).find('> thead > tr > th:eq('+index+')').addClass(pos);
                $(table).find('> tbody > tr').each(function(){
                    $(this).find('> td:eq('+index+')').addClass(pos);
                });
            }
            event.preventDefault();
        });
        
        $(headers).off("dragleave.draggableTable");
        $(headers).on("dragleave.draggableTable", function(event){
            $(table).find('.drop_left, .drop_right').removeClass("drop_left drop_right");
            event.preventDefault();
        });
        
        $(headers).off("drop.draggableTable");
        $(headers).on("drop.draggableTable", function(event){
            if ($(this).hasClass("drop_left") || $(this).hasClass("drop_right")) {
                if ($(this).hasClass("drop_left")) {
                    $(this).before($(table).find('> thead > tr > th.current_dragging'));
                    $(table).find('> tbody > tr').each(function(){
                        $(this).find('> td.drop_left').before($(this).find('> td.current_dragging'));
                    });
                } else {
                    $(this).after($(table).find('> thead > tr > th.current_dragging'));
                    $(table).find('> tbody > tr').each(function(){
                        $(this).find('> td.drop_right').after($(this).find('> td.current_dragging'));
                    });
                }
                
            }
            
            $(table).find('.drop_left, .drop_right').removeClass("drop_left drop_right");
            $(table).find('.current_dragging').removeClass("current_dragging");
            
            var columns = [];
            $(table).find('> thead > tr > th.column_header').each(function(){
                columns.push(getKey($(this)));
            });
            localStorage.setItem(key, JSON.stringify(columns));
        });
        
        $(headers).off("dragend.draggableTable");
        $(headers).on("dragend.draggableTable", function(event){
            $(table).find('.drop_left, .drop_right').removeClass("drop_left drop_right");
            $(table).find('.current_dragging').removeClass("current_dragging");
        });
        
        $(headers).off("dragstart.draggableTable");
        $(headers).on("dragstart.draggableTable", function(event){
            var index = $(this).closest("tr").find(' > th').index($(this));
            $(table).find('> thead > tr > th:eq('+index+')').addClass('current_dragging');
            $(table).find('> tbody > tr').each(function(){
                $(this).find('> td:eq('+index+')').addClass('current_dragging');
            });
        });
    }, 2);
}
/* rearrange the column based on cache order */
function rearrangeColumns(datalist, cache) {
    var table = $(datalist).find("> form > .table-wrapper > table, > .table-wrapper > table");
        
    var columns = JSON.parse(cache);
    for (var i = 0; i < columns.length - 1; i++) {
        var current = "."+columns[i];
        var next = "."+columns[i+1];
        
        if (current.indexOf(".header_") !== 0) {
            continue;
        }
        
        var currentheader = $(table).find(current);
        var nextHeader = $(table).find(next);
        
        var headers = $(table).find('> thead > tr > th');
        var currentIndex = $(headers).index(currentheader);
        var nextIndex = $(headers).index(nextHeader);
        
        $(currentheader).after(nextHeader);
        
        $(table).find('> tbody > tr').each(function(){
            var currentCell = $(this).find('> td:eq('+currentIndex+')');
            var nextCell = $(this).find('> td:eq('+nextIndex+')');
            $(currentCell).after(nextCell);
        });
    }
}
/* get position of a header */
function getOffset(el) {
    const rect = el.getBoundingClientRect();
    return {
        left: rect.left + window.scrollX,
        top: rect.top + window.scrollY
    };
}
/* init the table to allow show/hide columns */
function showHideColumns(datalist) {
    var id = $(datalist).attr("id");
    
    var table = $(datalist).find("> form > .table-wrapper > table, > .table-wrapper > table");
    
    var hide = function(key) {
        var header = "."+key;
        var body = header.replace(".header_", ".body_");
        if ($(table).find(body).first().parent().is("[xclass]")) {
            $(table).find(body).each(function(){
                $(this).parent().addClass("control_hide");
                $(this).parent().attr("xclass", $(this).parent().attr("xclass") + " control_hide");
            });
            $(table).find('th'+header).addClass("control_hide");
        } else {
            $(table).find(header + ', ' + body).addClass("control_hide");
        }
    };
    
    var show = function(key) {
        var header = "."+key;
        var body = header.replace(".header_", ".body_");
        if ($(table).find(body).first().parent().is("[xclass]")) {
            $(table).find(body).each(function(){
                $(this).parent().removeClass("control_hide");
                $(this).parent().attr("xclass", $(this).parent().attr("xclass").replace(' control_hide', ''));
            });
            $(table).find('th'+header).removeClass("control_hide");
        } else {
            $(table).find(header + ', ' + body).removeClass("control_hide");
        }
    };
    
    //rearrange columns
    var key = window.location.pathname + $(datalist).closest(".main-body-content").attr('id') + "_hide";
    var cache = localStorage.getItem(key);
    if (cache !== undefined && cache !== null) {
        var columns = JSON.parse(cache);
        for (var i = 0; i < columns.length; i++) {
            hide(columns[i]);
        }
    }
    
    setTimeout(function(){
        var headers = $(table).find('> thead > tr > th.column_header');

        var dropdown = $('<div class="show_hide_control"><span class="toggle"></span><ul></ul></div>');
        $(headers).each(function(){
            if (!$(this).is(".column-hidden")) {
                $(dropdown).find('ul').append('<li><label><input type="checkbox" value="'+(getKey($(this)))+ '" ' + (!$(this).hasClass('control_hide')?'checked':'') +'/> '+$(this).text()+'</label></li>');
            }
        });
        
        $(dropdown).find('span.toggle').off('click');
        $(dropdown).find('span.toggle').on('click', function(){
            $(dropdown).toggleClass("show_dropdown");
        });

        $(dropdown).find('input').off('click');
        $(dropdown).find('input').on('click', function(){
            if ($(this).is(':checked')) {
                show($(this).attr('value'));
            } else {
                hide($(this).attr('value'));
            }

            var columns = [];
            $(table).find('> thead > tr > th.column_header.control_hide').each(function(){
                columns.push(getKey($(this)));
            });
            localStorage.setItem(key, JSON.stringify(columns));
        });

        $(table).before(dropdown);
    }, 2);
}

function getKey(header) {
    var cssclasses = $(header).attr("class").split(" ");
    for (var i = 2; i < cssclasses.length; i++) {
        if (cssclasses[i].indexOf("header_") === 0) { //find the header class with id. 
            return cssclasses[i];
        }
    }
    return "";
}