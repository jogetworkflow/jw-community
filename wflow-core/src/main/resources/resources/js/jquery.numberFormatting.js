(function($){
    $.fn.extend({
        numberFormatting : function(o){
            var formatValue = function(element, o) {
                var value = $(element).val();
                if (value !== "") {
                    var formatted = FormUtil.numberFormat(value, o);
                    $(element).val(formatted);
                    
                    if ($(element).parent().find(".form-cell-value, .subform-cell-value").length > 0) {
                        $(element).parent().find(".form-cell-value > span, .subform-cell-value > span").text(formatted);
                        $(element).closest(".grid").trigger("change");
                    } else {
                        $(element).trigger("change");
                    }
                }
            };
            
            this.each(function(){
                var element = $(this);
                
                formatValue(element, o);
                $(element).on("focusout.numberFormatting", function() {
                    formatValue(element, o);
                });
            });
        }
    });
})(jQuery);