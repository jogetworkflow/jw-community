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
    var targetEl = $(this.target);
    var controlEl = $("[name=" + this.control + "]");
    var controlVal = this.controlValue;
    var isRegex = this.isRegex;
    var match = this.checkValue(this, controlEl, controlVal, isRegex);
    if (!match) {
        targetEl.css("display", "none");
        this.disableInputField(targetEl);
    } else {
        targetEl.css("display", "block");
        this.enableInputField(targetEl);
    }
    var thisObject = this;
    controlEl.live("change", function() {
        var controlEl = $("[name=" + thisObject.control + "]");
        var controlVal = thisObject.controlValue;
        var isRegex = thisObject.isRegex;
        var match  = thisObject.checkValue(thisObject, controlEl, controlVal, isRegex);
        if (match) {
            targetEl.css("display", "block");
            thisObject.enableInputField(targetEl);
        } else {
            targetEl.css("display", "none");
            thisObject.disableInputField(targetEl);
        }
    });
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
        return regex.exec(value);
    } else {
        return value == controlValue;
    }
}
VisibilityMonitor.prototype.disableInputField = function(targetEl) {
    $(targetEl).find('input, select, textarea, .form-element').each(function(){
        if($(this).is(":enabled, [disabled=false]")){
            $(this).addClass("section-visibility-disabled").attr("disabled", true).trigger("change");
        } 
    });
}
VisibilityMonitor.prototype.enableInputField = function(targetEl) {
    $(targetEl).find('.section-visibility-disabled').removeClass(".section-visibility-disabled").removeAttr("disabled").trigger("change"); 
}