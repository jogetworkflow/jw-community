(function( $ ){

    var methods = {

        init: function() {
            return this.each(function(){
                methods.initCells.apply($(this));

                // add new row button
                if ($(this).find(".grid-row-add").length == 0) {
                    var link = $('<a class="grid-row-add" href="#">Add Row</a>');
                    link.click(function() {
                        var table = $(this).parent();
                        methods.addRow.apply(table, arguments);
                        return false;
                    });
                    $(this).append(link);
                }

                // add first row if grid is empty
                if ($(this).find(".grid-row").length == 0) {
                    methods.addRow.apply($(this), arguments);
                }
                
                $(this).find("th:last-child").after("<th style=\"border: 0 none;\"></th>");
            });
        },

        initCells: function() {
            // make cells editable
            $(this).find(".grid-row .grid-cell").editable(methods.updateCell, {
                 type: "text",
                 tooltip: "Click to edit",
                 cssclass: "grid-cell-input",
                 width: "none",
                 submit: "OK",
                 data : function (content, setting) {
                     return $(this).next("input").val();
                 }
            });
            
            // add delete link for each row
            $(this).find(".grid-row").each(function() {
                if ($(this).find(".grid-row-del").length == 0) {
                    var td = $('<td class="grid-cell-options"><a class="grid-row-del" href="#">X</a></td>');
                    $(this).append(td);
                }
                $(this).find(".grid-row-del").unbind();
                $(this).find(".grid-row-del").click(methods.deleteRow);
            });
        },

        updateCell: function(value, settings) {
            var input = $(this).siblings("input");
            input.attr("value", value);
            if (value == "") {
                value = "Click to edit";
            }
            // trigger change
            var el = $(this).parent().parent().parent().parent().parent();
            setTimeout(function() {
                $(el).trigger("change");
            }, 100);
            return value;
        },

        addRow: function() {
            return $(this).each(function(){
                // get table
                var table = $(this).find("table");

                // clone template
                var template = $(table).find(".grid-row-template");
                var newRow = $(template).clone();
                newRow.removeClass("grid-row-template");
                newRow.addClass("grid-row");
                newRow.removeAttr("style");

                // set input names and values
                var rowIndex = $(table).find("tr").length - 2;
                methods.updateInput(newRow, rowIndex);

                // append row
                table.append(newRow);

                methods.initCells.apply(this);

                // trigger change
                $(this).trigger("change");
            });
        },

        updateInput: function(row, rowIndex) {
            $(row).find(".grid-cell").each(function(index, column) {
                var input = $(column).siblings(".grid-input");
                var name = $(column).attr("id") + "_" + rowIndex;
                var value = $(column).text();
                input.attr("name", name);
                input.attr("value", value);
            });
        },

        deleteRow: function() {
            if (confirm("Delete row?")) {
                var row = $(this).parent().parent();
                var table = row.parent();
                row.remove();

                // reset input names
                table.find(".grid-row").each(function(rowIndex, row) {
                    methods.updateInput(row, rowIndex);
                });

                // trigger change
                var el = table.parent();
                $(el).trigger("change");
            }
            return false;
        }

    };

    $.fn.formgrid = function( method ) {

        if ( methods[method] ) {
            return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
        } else if ( typeof method === 'object' || ! method ) {
            return methods.init.apply( this, arguments );
        } else {
            $.error( 'Method ' +  method + ' does not exist on jQuery.formgrid' );
        }

    };

})( jQuery );

