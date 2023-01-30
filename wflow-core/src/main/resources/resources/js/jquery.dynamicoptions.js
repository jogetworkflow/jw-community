(function($){
    $.fn.extend({
        dynamicOptions : function(o){
            var target = this;
            if($(target)){
                var cf = o.controlField.split(";");
                
                for (var i in cf) {
                    if ($(target).attr("name") === cf[i]) {
                        return;
                    }
                }
                
                for (var i in cf) {
                    FormUtil.setControlField(cf[i]);
                }
                
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
                    for (var i in cf) {
                        $('body').off("change", '[name='+cf[i]+']:not(form)', showHideChange);
                        $('body').on("change", '[name='+cf[i]+']:not(form)', showHideChange);
                    }
                    
                    $(parentSection).off("jsection:show." + o.paramName);
                    $(parentSection).on("jsection:show." + o.paramName, showHideChange);
                    showHideOption(target, o);
                } else {
                    for (var i in cf) {
                        $('body').off("change", '[name='+cf[i]+']:not(form)', ajaxChange);
                        $('body').on("change", '[name='+cf[i]+']:not(form)', ajaxChange);
                    }
                    
                    $(parentSection).off("jsection:show." + o.paramName);
                    $(parentSection).on("jsection:show." + o.paramName, ajaxChange);
                    ajaxOptions(target, o);
                }
            }
            return target;
        }
    });
    
    function getValues(name) {
        var values = new Array();
        var cf = name.split(";");
        
        for (var i in cf) {
            //get enabled input field oni
            var el = $('[name=' + cf[i] + ']').filter("input[type=hidden]:not([disabled=true]), :enabled, [disabled=false]");

            if ($(el).is("select")) {
                el = $(el).find("option:selected");
            } else if ($(el).is("input[type=checkbox], input[type=radio]")) {
                el = $(el).filter(":checked");
            } 

            $(el).each(function() {
                values.push($(this).val());
            });
        }
        
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
    
    function intersectArray(a, b) {
        return a.filter(function(value) {
            return b.indexOf(value) !== -1;
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
                if ($(option).attr("value") !== "" && $(option).attr("grouping") !== undefined && $(option).attr("grouping") !== null) {
                    var groups = $(option).attr("grouping").split(";");
                    var intersect = intersectArray(groups, controlValues);
                    if (intersect.length === 0) {
                        $(option).remove();
                    }
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
                if ($(option).attr("grouping") !== undefined && $(option).attr("grouping") !== null) {
                    var groups = $(option).attr("grouping").split(";");
                    var intersect = intersectArray(groups, controlValues);
                    if (intersect.length > 0) {
                       $(label).show();
                    } else {
                        if ($(option).is(":checked")) {
                            $(option).removeAttr("checked");
                        }
                        $(label).hide();
                    }
                }
            });
        }
        if (!$(target).is(".section-visibility-disabled")) {
            $('[name='+o.paramName+']:not(form):not(.section-visibility-disabled)').trigger("change");
        }
    }
})(jQuery);