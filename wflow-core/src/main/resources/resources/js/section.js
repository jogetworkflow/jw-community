var VisibilityMonitor = function(targetEl, data) {
    this.target = targetEl;
    this.rules = data['rules'];
};

VisibilityMonitor.prototype.rules = null; 

VisibilityMonitor.prototype.init = function() {
    var thisObject = this;
    
    var targetEl = $(this.target);
    var id = $(this.target).attr("id");
    
    var changeEvent = function(field) {
        thisObject.handleChange(targetEl, thisObject.rules);
    };
    
    for (var i in thisObject.rules) {
        var field = thisObject.rules[i].field;
        if (field !== "(" && field !== ")") {
            var controlEl = $("[name=" + field + "]:not(form)");
            var parent = $(controlEl).closest(".subform-section");
            if (parent.length === 0) {
                parent = $(controlEl).closest(".form-section");
            }
            if (!$(parent).is(targetEl)) { // prevent it keep trigger itself
                FormUtil.setControlField(field);
            }
            $('body').off("change."+id+"_"+field);
            $('body').on("change."+id+"_"+field, "[name=" + field + "]:not(form)", changeEvent);
        }
    }
    
    thisObject.handleChange(targetEl, this.rules);
};

VisibilityMonitor.prototype.handleChange = function(targetEl, rules) {
    var thisObject = this;
    var match = false;
    
    try {
        var rule = "";
        for (var i in rules) {
            var field = rules[i].field;
            var join = rules[i].join;
            var value = rules[i].value;
            var regex = rules[i].regex;
            var reverse = rules[i].reverse;
            
            if (rule !== "" && rule.substr(rule.length - 1) !== "(" && field !== ")") {
                if (join === "or") {
                    rule += " || ";
                } else {
                    rule += " && ";
                }
            }
            if (field !== ")") {
                rule += " ";
            }
            if (reverse !== "" && field !== ")") {
                rule += "!";
            }
            if (field === "(" || field === ")" ) {
                rule += field;
            } else {
                var controlEl = $("[name=" + field + "]:not(form)");
                rule += thisObject.checkValue(thisObject, controlEl, value, regex);
            }
        }
        match = eval(rule);
    } catch (err) {}
    
    if (match && (targetEl.hasClass("section-visibility-hidden") || !targetEl.is(":visible"))) {
        targetEl.css("display", "");
        targetEl.removeClass("section-visibility-hidden");
        thisObject.enableInputField(targetEl);
    } else if (!match && (!targetEl.hasClass("section-visibility-hidden") || targetEl.is(":visible"))) {
        targetEl.css("display", "none");
        targetEl.addClass("section-visibility-hidden");
        thisObject.disableInputField(targetEl);
    }
};

VisibilityMonitor.prototype.checkValue = function(thisObject, controlEl, controlValue, operator) {
    //get enabled input field oni
    controlEl = $(controlEl).filter("input[type=hidden]:not([disabled=true]), :enabled, :disabled, [disabled=false]");
    controlEl = $(controlEl).filter(":not(.section-visibility-disabled)"); //must put in newline to avoid conflict with above condition
    
    var match = false;
    if ($(controlEl).length > 0) {
        if ($(controlEl).attr("type") === "checkbox" || $(controlEl).attr("type") === "radio") {
            controlEl = $(controlEl).filter(":checked");
        } else if ($(controlEl).is("select")) {
            controlEl = $(controlEl).find("option:selected");
        } else if ($(controlEl).attr("type") === "hidden") {
            controlEl = $(controlEl).filter("input[type=hidden]");
        }
        
        if ($(controlEl).length > 0) {
            $(controlEl).each(function() {
                match = thisObject.isMatch($(this).val(), controlValue, operator);
                if (match) {
                    return false;
                }
            });
        } else {
            match = thisObject.isMatch("", controlValue, operator);
        }
    }
    return match;
};
VisibilityMonitor.prototype.isMatch = function(fieldValue, controlValue, operator) {
    var result = false;
    if (fieldValue !== null) {
        var fieldValueNumber = null;
        var controlValueNumber = null;
        var isNumeric = false;
        if (controlValue !== null && controlValue !== "" && !isNaN(fieldValue) && !isNaN(controlValue)) {
            try {
                fieldValueNumber = parseFloat(fieldValue);
                controlValueNumber = parseFloat(controlValue);
                isNumeric = true;
            } catch (err) {}
        }
        if (isNumeric) {
            if (operator === "") {
                result = fieldValueNumber == controlValueNumber;
            } else if (">" === operator) {
                result = fieldValueNumber > controlValueNumber;
            } else if (">=" === operator) {
                result = fieldValueNumber >= controlValueNumber;
            } else if ("<" === operator) {
                result = fieldValueNumber < controlValueNumber;
            } else if ("<=" === operator) {
                result = fieldValueNumber <= controlValueNumber;
            }
        } else {
            if (operator === "") {
                result = fieldValue === controlValue;
            } else if (">" === operator) {
                result = fieldValue > controlValue;
            } else if (">=" === operator) {
                result = fieldValue >= controlValue;
            } else if ("<" === operator) {
                result = fieldValue < controlValue;
            } else if ("<=" === operator) {
                result = fieldValue <= controlValue;
            } else if ("isTrue" === operator) {
                result = fieldValue.toLowerCase() === "true" || fieldValue === "1";
            } else if ("isFalse" === operator) {
                result = fieldValue.toLowerCase() === "false" || fieldValue === "0";
            } else if ("contains" === operator) {
                result = fieldValue.indexOf(controlValue) >= 0;
            } else if ("listContains" === operator) {
                var list = fieldValue.split(";");
                result = $.inArray(controlValue, list) >= 0;
            } else if ("in" === operator) {
                var list = controlValue.replaceAll("__", ";").split(";");
                result = $.inArray(fieldValue, list) >= 0;
            } else if ("true" === operator) {
                try {
                    var regex = new RegExp(controlValue);
                    var regexResult = regex.exec(fieldValue);
                    result = regexResult.length > 0 && regexResult[0] === fieldValue;
                } catch (err) {}
            }
        }
    }
    return result;
};
VisibilityMonitor.prototype.disableInputField = function(targetEl) {
    var thisObject = this;
    
    var names = new Array();
    var radios = new Array();
    $(targetEl).find('input:not([type=submit]), select, textarea, .form-element').each(function(){
        if($(this).is("input[type=hidden]:not([disabled=true]), :enabled, [disabled=false]")){
            $(this).addClass("section-visibility-disabled").attr("disabled", true);
            
            var mobileSelector = ".ui-input-text, .ui-checkbox, .ui-radio, .ui-select";
            
            //mobile
            if ($(this).is(mobileSelector) 
                    || $(this).parent().is(mobileSelector) 
                    || $(this).parent().parent().is(mobileSelector)) {
                if ($(this).is("[type='checkbox'], [type='radio']")) {
                    $(this).checkboxradio("disable");
                } else if ($(this).is("select")) {
                    $(this).selectmenu("disable");
                } else {
                    $(this).textinput("disable");
                }
            }
        } 
        if ($(this).is("[name]") && FormUtil.isControlField($(this).attr("name"), $(this))) {
            var n = $(this).attr("name");
            if ($.inArray(n, names) < 0 && n !== "") {
                names.push(n);
            }
        }
        if ($(this).is("[type='radio']") && $.inArray($(this).attr("name"), radios) < 0) {
            radios.push($(this).attr("name"));
        }
        $(this).trigger("jsection:hide");
    });
    thisObject.handleRadio(targetEl, radios);
    thisObject.triggerChange(targetEl, names);
};
VisibilityMonitor.prototype.enableInputField = function(targetEl) {
    var thisObject = this;
    
    var names = new Array();
    var radios = new Array();
    $(targetEl).find('input:not([type=submit]), select, textarea, .form-element').each(function(){
        if($(this).is(".section-visibility-disabled")){
            $(this).removeClass("section-visibility-disabled").removeAttr("disabled");
            
            var mobileSelector = ".ui-input-text, .ui-checkbox, .ui-radio, .ui-select";
            
            //mobile
            if ($(this).is(mobileSelector) 
                    || $(this).parent().is(mobileSelector) 
                    || $(this).parent().parent().is(mobileSelector)) {
                if ($(this).is("[type='checkbox'], [type='radio']")) {
                    $(this).checkboxradio("enable");
                } else if ($(this).is("select")) {
                    $(this).selectmenu("enable");
                } else {
                    $(this).textinput("enable");
                }
            }
        } 
        if ($(this).is("[name]") && FormUtil.isControlField($(this).attr("name"), $(this))) {
            var n = $(this).attr("name");
            if ($.inArray(n, names) < 0 && n !== "") {
                names.push(n);
            }
        }
        if ($(this).is("[type='radio']") && $.inArray($(this).attr("name"), radios) < 0) {
            radios.push($(this).attr("name"));
        }
        $(this).trigger("jsection:show");
    });
    
    thisObject.handleRadio(targetEl, radios);
    thisObject.triggerChange(targetEl, names);
    $(window).trigger("resize");
};
VisibilityMonitor.prototype.triggerChange = function(targetEl, names) {
    $.each(names, function(i){
        var temp = false;
        var newObject = $("[name=" + names[i] + "]:not(form)");
        if (newObject.length === 0) {
            newObject = $('<input name="'+names[i]+'" style="display:none;" />');
            $(targetEl).append(newObject);
            temp = true;
        }
        $("[name=" + names[i] + "]:not(form)").trigger("change");
        if (temp) {
            $(newObject).remove();
        }
    });
};
VisibilityMonitor.prototype.handleRadio = function(targetEl, names) {
    $.each(names, function(i) {
        $("[name=" + names[i] + "][checked]").removeProp("checked").removeAttr("checked").attr("data-checked", "checked");
        $("[name=" + names[i] + "][data-checked]").each(function(){
            //check if not in visibility disabled section
            if ($(this).closest(".form-section.section-visibility-hidden, .subform-section.section-visibility-hidden").length === 0) {
                if ($(this).is("[data-checked]")) {
                    $(this).removeProp("data-checked").prop("checked", "checked");
                }
            }
        });
    });
};