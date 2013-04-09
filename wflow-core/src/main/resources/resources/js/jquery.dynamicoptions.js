(function($){
    $.fn.extend({
        dynamicOptions : function(o){
            var target = this;
            if($(target)){
                $('[name='+o.controlField+']').live("change", function(){
                    showHideOption(target, o);
                });
                showHideOption(target, o);
            }
            return target;
        }
    });
    
    function showHideOption(target, o){
        //get enabled input field oni
        var controlEl = $('[name='+o.controlField+']').filter("input[type=hidden]:not([disabled=true]), :enabled, [disabled=false]");
        var controlValues = new Array();
        
        if ($(controlEl).is("select")) {
            controlEl = $(controlEl).find("option:selected");
        } else if ($(controlEl).is("input[type=checkbox], input[type=radio]")) {
            controlEl = $(controlEl).filter(":checked");
        } 
        
        $(controlEl).each(function() {
            controlValues.push($(this).val());
        });
        
        if ($(target).is("select")) {
            if ($(target).next('.dynamic_option_container').length == 0) {
                $(target).after('<select class="dynamic_option_container" style="display:none;">'+$(target).html()+'</select>');
            }
            
            $(target).html($(target).next('.dynamic_option_container').html());
            
            $(target).find("option").each(function(){
                var option = $(this);
                if ($(option).attr("grouping") != "" && $.inArray($(option).attr("grouping"), controlValues) == -1) {
                    $(option).remove();
                }
            });
        } else {
              
            $(target).each(function(){
                var option = $(this);
                var label = $(option).parent();
                if ($(option).attr("grouping") == "" || $.inArray($(option).attr("grouping"), controlValues) > -1) {
                    $(label).show();
                } else {
                    if ($(option).is(":checked")) {
                        $(option).removeAttr("checked");
                    }
                    $(label).hide();
                }
            });
        }
        $('[name='+$(target).attr("name")+']').trigger("change");
    }
})(jQuery);