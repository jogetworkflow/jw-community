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
    
    thisObject.handleChange(targetEl, controlEl, controlVal, isRegex);
    
    controlEl.live("change", function() {
        thisObject.handleChange(targetEl, controlEl, controlVal, isRegex);
    });
}

VisibilityMonitor.prototype.handleChange = function(targetEl, controlEl, controlVal, isRegex) {
    var thisObject = this;
    
    var match  = thisObject.checkValue(thisObject, controlEl, controlVal, isRegex);
    if (match) {
        targetEl.css("display", "block");
        thisObject.enableInputField(targetEl);
    } else {
        targetEl.css("display", "none");
        thisObject.disableInputField(targetEl);
    }
}

VisibilityMonitor.prototype.checkValue = function(thisObject, controlEl, controlValue, isRegex) {
    //get enabled input field oni
    controlEl = $(controlEl).filter(":enabled, [disabled=false]");
    
    var match = false;
    if ($(controlEl).length > 0) {
        if ($(controlEl).attr("type") == "checkbox" || $(controlEl).attr("type") == "radio") {
            controlEl = $(controlEl).filter(":checked");
        } else if ($(controlEl).is("select")) {
            controlEl = $(controlEl).find("option:selected");
        }
        
        $(controlEl).each(function() {
            match = thisObject.isMatch($(this).val(), controlValue, isRegex);
            if (match) {
                return false;
            }
        });
    }
    return match;
}
VisibilityMonitor.prototype.isMatch = function(value, controlValue, isRegex) {
    if (isRegex != undefined && "true" == isRegex) {
        var regex = new RegExp(controlValue);
        return regex.exec(value) == value;
    } else {
        return value == controlValue;
    }
}
VisibilityMonitor.prototype.disableInputField = function(targetEl) {
    var names = new Array();
    $(targetEl).find('input, select, textarea, .form-element').each(function(){
        if($(this).is(":enabled, [disabled=false]")){
            $(this).addClass("section-visibility-disabled").attr("disabled", true);
            var n = $(this).attr("name");
            if ($.inArray(n, names) < 0) {
                names.push(n);
            }
        } 
    });
    $.each(names, function(i){
        var newObject = $('<input name="'+names[i]+'" class="display:none;" />');
        $(targetEl).append(newObject);
        $("[name=" + names[i] + "]").trigger("change");
        $(newObject).remove();
    });
}
VisibilityMonitor.prototype.enableInputField = function(targetEl) {
    var names = new Array();
    $(targetEl).find('.section-visibility-disabled').removeClass(".section-visibility-disabled").removeAttr("disabled").each(function(){
        var n = $(this).attr("name");
        if ($.inArray(n, names) < 0) {
            names.push(n);
        }
    });
    $.each(names, function(i){
        $("[name=" + names[i] + "]").trigger("change");
    });
}
