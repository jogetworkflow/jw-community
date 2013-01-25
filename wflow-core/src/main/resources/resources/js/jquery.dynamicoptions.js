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
        var controlValue = $('[name$='+o.controlField+']').filter("input[type=hidden]:not([disabled=true]), :enabled, [disabled=false]").val();
        if ($(target).is("select")) {
            if ($(target).next('.dynamic_option_container').length == 0) {
                $(target).after('<select class="dynamic_option_container" style="display:none;">'+$(target).html()+'</select>');
            }
            
            $(target).html($(target).next('.dynamic_option_container').html());
            
            $(target).find("option").each(function(){
                var option = $(this);
                if ($(option).attr("grouping") != "" && $(option).attr("grouping") != controlValue) {
                    $(option).remove();
                }
            });
        } else {
            $(target).each(function(){
                var option = $(this);
                var label = $(option).parent();
                if ($(option).attr("grouping") == "" || $(option).attr("grouping") == controlValue) {
                    $(label).show();
                } else {
                    if ($(option).is(":checked")) {
                        $(option).removeAttr("checked");
                    }
                    $(label).hide();
                }
            });
        }
        $(target).trigger("change");
    }
})(jQuery);