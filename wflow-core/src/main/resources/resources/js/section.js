var VisibilityMonitor = function(targetEl, controlEl, controlValue) {
    this.target = targetEl;
    this.control = controlEl;
    this.controlValue = controlValue;
}

VisibilityMonitor.prototype.target = null; // the target element (to be shown or hidden)

VisibilityMonitor.prototype.control = null; // the control element (where value changes the target)

VisibilityMonitor.prototype.controlValue = null; // the value in the control element which will trigger the change

VisibilityMonitor.prototype.init = function() {
    var targetEl = $("#" + this.target);
    var controlEl = $("#" + this.control + " ");
    var controlVal = this.controlValue;
    var match = this.checkValue(controlEl, controlVal);
    if (!match) {
        targetEl.css("display", "none");
    } else {
        targetEl.css("display", "block");
    }
    var thisObject = this;
    controlEl.change(function() {
        var controlEl = $("#" + thisObject.control + " ");
        var controlVal = thisObject.controlValue;
        var match  = thisObject.checkValue(controlEl, controlVal);
        if (match) {
            targetEl.css("display", "block");
        } else {
            targetEl.css("display", "none");
        }
    });
}
VisibilityMonitor.prototype.checkValue = function(controlEl, controlValue) {
    var match = false;
    if (controlEl.attr("type") == "checkbox" || controlEl.attr("type") == "radio") {
        controlEl.filter(":checked").each(function() {
            match = $(this).val() == controlValue;
            if (match) {
                return false;
            }
        });
    } else {
        match = controlEl.val() == controlValue;
    }
    return match;
}
