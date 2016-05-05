var VisibilityMonitor = function(targetEl, controlEl, controlValue, isRegex) {
    this.target = targetEl;
    this.control = controlEl;
    this.controlValue = controlValue;
    this.isRegex = isRegex;
}

VisibilityMonitor.prototype.target = null; // the target element (to be shown or hidden)

VisibilityMonitor.prototype.control = null; // the control element (where value changes the target)

VisibilityMonitor.prototype.controlValue = null; // the value in the control element which will trigger the change

VisibilityMonitor.prototype.isRegex = null; // the flag decide control value is a regex or not

VisibilityMonitor.prototype.init = function() {
    var thisObject = this;
    
    var targetEl = $(this.target);
    var controlEl = $("[name=" + this.control + "]");
    var controlVal = this.controlValue;
    var isRegex = this.isRegex;
    
    $(controlEl).addClass("control-field");
    
    thisObject.handleChange(targetEl, controlEl, controlVal, isRegex);
    
    var changeEvent = function() {
        thisObject.handleChange(targetEl, controlEl, controlVal, isRegex);
    };
    
    $('body').off("change", "[name=" + this.control + "]", changeEvent);
    $('body').on("change", "[name=" + this.control + "]", changeEvent);
}

VisibilityMonitor.prototype.handleChange = function(targetEl, controlEl, controlVal, isRegex) {
    var thisObject = this;
    
    var match  = thisObject.checkValue(thisObject, controlEl, controlVal, isRegex);
    if (match) {
        targetEl.css("display", "block");
        targetEl.removeClass("section-visibility-hidden");
        thisObject.enableInputField(targetEl);
    } else {
        targetEl.css("display", "none");
        targetEl.addClass("section-visibility-hidden");
        thisObject.disableInputField(targetEl);
    }
}

VisibilityMonitor.prototype.checkValue = function(thisObject, controlEl, controlValue, isRegex) {
    //get enabled input field oni
    controlEl = $(controlEl).filter("input[type=hidden]:not([disabled=true]), :enabled, [disabled=false]");
    controlEl = $(controlEl).filter(":not(.section-visibility-disabled)"); //must put in newline to avoid conflict with above condition
    
    var match = false;
    if ($(controlEl).length > 0) {
        if ($(controlEl).attr("type") == "checkbox" || $(controlEl).attr("type") == "radio") {
            controlEl = $(controlEl).filter(":checked");
        } else if ($(controlEl).is("select")) {
            controlEl = $(controlEl).find("option:selected");
        }
        
        if ($(controlEl).length > 0) {
            $(controlEl).each(function() {
                match = thisObject.isMatch($(this).val(), controlValue, isRegex);
                if (match) {
                    return false;
                }
            });
        } else {
            match = thisObject.isMatch("", controlValue, isRegex);
        }
    }
    return match;
}
VisibilityMonitor.prototype.isMatch = function(value, controlValue, isRegex) {
    if (isRegex != undefined && "true" == isRegex) {
        try {
            var regex = new RegExp(controlValue);
            return regex.exec(value) == value;
        } catch (err) {
            return false;
        }
    } else {
        return value == controlValue;
    }
}
VisibilityMonitor.prototype.disableInputField = function(targetEl) {
    var thisObject = this;
    
    var names = new Array();
    $(targetEl).find('input, select, textarea, .form-element').each(function(){
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
        if ($(this).is("[name].control-field")) {
            var n = $(this).attr("name");
            if ($.inArray(n, names) < 0 && n != "") {
                names.push(n);
            }
        }
    });
    thisObject.triggerChange(targetEl, names);
}
VisibilityMonitor.prototype.enableInputField = function(targetEl) {
    var thisObject = this;
    
    var names = new Array();
    $(targetEl).find('input, select, textarea, .form-element').each(function(){
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
        if ($(this).is("[name].control-field")) {
            var n = $(this).attr("name");
            if ($.inArray(n, names) < 0 && n != "") {
                names.push(n);
            }
        }
    });
    
    thisObject.triggerChange(targetEl, names);
    $(window).trigger("resize");
}
VisibilityMonitor.prototype.triggerChange = function(targetEl, names) {
    $.each(names, function(i){
        var temp = false;
        var newObject = $("[name=" + names[i] + "]");
        if (newObject.length === 0) {
            newObject = $('<input name="'+names[i]+'" class="control-field" style="display:none;" />');
            $(targetEl).append(newObject);
            temp = true;
        }
        $("[name=" + names[i] + "]").trigger("change");
        if (temp) {
            $(newObject).remove();
        }
    });
}