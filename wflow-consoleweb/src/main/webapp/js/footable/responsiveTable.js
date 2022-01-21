$(document).ready(function() {
    $(".dataList").each(function(){
        if ($(this).find("> form > .table-wrapper > table.responsivetable").length > 0) {
            responsiveTable($(this));
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
                var width = $(this).parent().width();
                
//                $(this).find('*[class]').each(function(){
//                    updateClasses(this, dl, width);
//                });
//                updateClasses(this, dl, width);
                
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
                var width = $(datalist).parent().width();
                var table = $(datalist).find("> form > .table-wrapper > table.responsivetable, > .table-wrapper > table.responsivetable");
                if (width < 540) {
                    $(table).find('> tbody > tr > td.column_body').each(function(){
                        if ($(this).find('> .cell-label').length === 0) {
                            $(this).prepend('<label class="cell-label"></label>');
                            var index = $(this).parent().find("td").index(this);
                            var th = $(table).find('> thead > tr > th:eq('+index+')');
                            $(this).find('> .cell-label').text($(th).text());
                            $(this).find('> .cell-label').addClass($(th).attr('class'));
                        }
                    });
                } else {
                    $(table).find('> tbody > tr > td > .cell-label').remove();
                }
            });
        }, 2);
    };

    $(window).off("resize."+id);
    $(window).on("resize."+id, resize);
    resize();
}
