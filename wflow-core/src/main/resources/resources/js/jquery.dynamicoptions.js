(function($){
    $.fn.extend({
        dynamicOptions : function(o){
            var target = this;
            if($(target)){
                if ($(target).attr("name") === o.controlField) {
                    return;
                }
                
                FormUtil.setControlField(o.controlField);
                
                var showHideChange = function() {
                    if ($(target).hasClass("section-visibility-disabled") || $(target).find("input.section-visibility-disabled").length > 0) {
                        return;
                    }
                    showHideOption(target, o);
                };
                
                var ajaxChange = function() {
                    if ($(target).hasClass("section-visibility-disabled") || $(target).find("input.section-visibility-disabled").length > 0) {
                        return;
                    }
                    ajaxOptions(target, o);
                };
                
                var parentSection = $(this).closest(".subform-section");
                if ($(parentSection).length === 0) {
                    parentSection = $(this).closest(".form-section");
                }
                
                if (o.nonce === '') {
                    $('body').off("change", '[name='+o.controlField+']:not(form)', showHideChange);
                    $('body').on("change", '[name='+o.controlField+']:not(form)', showHideChange);
                    $(parentSection).on("jsection:show", showHideChange);
                    showHideOption(target, o);
                } else {
                    $('body').off("change", '[name='+o.controlField+']:not(form)', ajaxChange);
                    $('body').on("change", '[name='+o.controlField+']:not(form)', ajaxChange);
                    $(parentSection).on("jsection:show", ajaxChange);
                    ajaxOptions(target, o);
                }
            }
            return target;
        }
    });
    
    function getValues(name) {
        //get enabled input field oni
        var el = $('[name=' + name + ']').filter("input[type=hidden]:not([disabled=true]), :enabled, [disabled=false]");
        var values = new Array();
        
        if ($(el).is("select")) {
            el = $(el).find("option:selected");
        } else if ($(el).is("input[type=checkbox], input[type=radio]")) {
            el = $(el).filter(":checked");
        } 
        
        $(el).each(function() {
            values.push($(this).val());
        });
        
        return values;
    }
    
    function ajaxOptions(target, o){
        var controlValues = getValues(o.controlField);
        var values = getValues(o.paramName);
        
        var cv = controlValues.join(";");
        $.ajax({
            dataType: "json",
            url: o.contextPath + "/web/json/app/"+o.appId+"/"+o.appVersion+"/form/options",
            method: "POST",
            data: {
                _dv: cv, 
                _n: o.nonce,
                _bd: o.binderData
            },
            success: function(data){
                if (o.type === "selectbox") {
                    var defaultValues = [];
                    var hasValue = false;
                    $(target).find("option:not(.label)").remove();
                    for (var i=0, len=data.length; i < len; i++) {
                        var selected = "";
                        if ($.inArray(data[i].value, values) !== -1) {
                            selected = "selected=\"selected\"";
                            hasValue = true;
                        }
                        if (data[i].selected !== undefined && data[i].selected.toLowerCase() === "true") {
                            defaultValues.push(data[i].value);
                        }
                        var option = $("<option "+selected+" >"+UI.escapeHTML(data[i].label)+"</option>");
                        option.attr("value", data[i].value);
                        $(target).append(option);
                    }
                    
                    if (defaultValues.length > 0 && !hasValue) {
                        for (var dv in defaultValues) {
                            $(target).find("option[value='"+defaultValues[dv]+"']").prop("selected", "selected");
                        }
                    }
                } else {
                    var defaultValues = [];
                    var hasValue = false;
                    $(target).html("");
                    for (var i=0, len=data.length; i < len; i++) {
                        var checked = "";
                        if ($.inArray(data[i].value, values) !== -1) {
                            checked = "checked=\"checked\"";
                            hasValue = true;
                        }
                        if (data[i].selected !== undefined && data[i].selected.toLowerCase() === "true") {
                            defaultValues.push(data[i].value);
                        }
                        
                        var option = $("<label tabindex=\"0\" ><input "+checked+" id=\""+o.paramName+"\" name=\""+o.paramName+"\" type=\""+o.type+"\" /><i></i>"+UI.escapeHTML(data[i].label)+"</label>");
                        option.find("input").attr("value", data[i].value);
                        $(target).append(option);
                    }
                    if (defaultValues.length > 0 && !hasValue) {
                        for (var dv in defaultValues) {
                            $(target).find("input[value='"+defaultValues[dv]+"']").prop("checked", true);
                        }
                    }
                    
                    if (o.readonly === "true") {
                        $(target).find("input").click(function(){
                            return false;
                        });         
                    }
                }
                if (!$(target).is(".section-visibility-disabled")) {
                    $('[name='+o.paramName+']:not(form):not(.section-visibility-disabled)').trigger("change");
                }
            }
        });
    }
    
    function showHideOption(target, o){
        var controlValues = getValues(o.controlField);
        var values = getValues(o.paramName);
        
        if ($(target).is("select")) {
            if ($(target).closest(".form-cell, .subform-cell, .filter-cell").find('select.dynamic_option_container').length == 0) {
                $(target).after('<div class="ui-screen-hidden"><select class="dynamic_option_container" style="display:none;">'+$(target).html()+'</select></div>');
            }
            
            $(target).html($(target).closest(".form-cell, .subform-cell, .filter-cell").find('select.dynamic_option_container').html());
            
            $(target).find("option:not(.label)").each(function(){
                var option = $(this);
                if ($(option).attr("grouping") != "" && $.inArray($(option).attr("grouping"), controlValues) == -1) {
                    $(option).remove();
                }
            });
            for (var i in values) {
                if (values[i] !== "") {
                    $(target).find("option[value='"+values[i]+"']").prop("selected", "selected");
                }
            }
        } else { 
            
            $(target).find("input").each(function(){
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
        if (!$(target).is(".section-visibility-disabled")) {
            $('[name='+o.paramName+']:not(form):not(.section-visibility-disabled)').trigger("change");
        }
    }
})(jQuery);