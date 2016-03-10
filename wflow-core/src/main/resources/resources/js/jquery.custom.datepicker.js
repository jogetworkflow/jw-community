(function($){
    $.fn.extend({
        cdatepicker : function(o){
            this.each(function(){
                var element = $(this);
                
                $(element).datepicker(o);
                
                if (o.startDateFieldId  !== undefined && o.startDateFieldId !== "") {
                    var startDate = FormUtil.getField(o.startDateFieldId);
                    
                    startDate.live("change", function(){
                        setDateRange(startDate, "minDate", element);
                    });
                    setDateRange(startDate, "minDate", element);
                }
                
                if (o.endDateFieldId  !== undefined && o.endDateFieldId !== "") {
                    var endDate = FormUtil.getField(o.endDateFieldId);
                    
                    endDate.live("change", function(){
                        setDateRange(endDate, "maxDate", element);
                    });
                    setDateRange(endDate, "maxDate", element);
                }
                
                if (o.currentDateAs !== undefined && o.currentDateAs !== "") {
                    var option = $(element).datepicker( "option", o.currentDateAs);
                    if (option === undefined || option === null) {
                        $(element).datepicker( "option", o.currentDateAs, new Date());
                    }
                }
            });
        }
    });
    
    function setDateRange(element, type, target) {
        var value = $(element).val();
        if (value !== "") {
            $(target).datepicker( "option", type, value);
        }
    }
})(jQuery);