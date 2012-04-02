(function($){
    $.fn.extend({
        dynamicOptions : function(o){
            var target = this;
            if($(target)){
                $('[name$='+o.controlField+']').live("change", function(){
                    showHideOption(target, o);
                });
                showHideOption(target, o);
            }
            return target;
        }
    });
    
    function showHideOption(target, o){
        var controlValue = $('[name$='+o.controlField+']').filter(":enabled, [disabled=false]").val();
        
        if ($(target).is("select")) {
            $(target).find("option").each(function(){
                var option = $(this);
                if ($(option).attr("grouping") == controlValue) {
                    $(option).show();
                } else {
                    if ($(option).is(":selected")) {
                        $(option).removeAttr("selected");
                    }
                    $(option).hide();
                }
            });
        } else {
            $(target).each(function(){
                var option = $(this);
                var label = $(option).parent();
                if ($(option).attr("grouping") == controlValue) {
                    $(label).show();
                } else {
                    if ($(option).is(":checked")) {
                        $(option).removeAttr("checked");
                    }
                    $(label).hide();
                }
            });
        }
    }
})(jQuery);