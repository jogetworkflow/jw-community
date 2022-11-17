(function($){
    $.fn.extend({
        cdatepicker : function(o){
            this.each(function(){
                var element = $(this);
                var elementParent = element.parent();

                if (!/iPhone|iPod|iPad/.test(navigator.userAgent)) {
                    o.beforeShow = function(input, inst) {
                        $(element).addClass("popup-picker");
                        setTimeout(function(){
                            var tabbables = $("#ui-datepicker-div").find(':tabbable');
                            var first = tabbables.filter(':first');
                            var last  = tabbables.filter(':last');

                            $("#ui-datepicker-div").off("keydown", ":tabbable");
                            $("#ui-datepicker-div").on("keydown", ":tabbable", function(e) {
                                var keyCode = e.keyCode || e.which;
                                if (keyCode === 9) {
                                    var focusedElement = $(e.target);

                                    var isFirstInFocus = (first.get(0) === focusedElement.get(0));
                                    var isLastInFocus = (last.get(0) === focusedElement.get(0));

                                    var tabbingForward = !e.shiftKey;

                                    if (tabbingForward) {
                                        if (isLastInFocus) {
                                            first.focus();
                                            e.preventDefault();
                                        }
                                    } else {
                                        if (isFirstInFocus) {
                                            last.focus();
                                            e.preventDefault();
                                        }
                                    }
                                } else if (keyCode === 27) {
                                    $(element).datepicker("hide");
                                    $(element).next("a.trigger").focus();
                                }
                            });
                            first.focus();
                            
                            var orizindex = inst.dpDiv.css("z-index");
                            try {
                                orizindex = parseInt(orizindex);
                            } catch (err) {}
                            inst.dpDiv.css({"z-index":(orizindex + 200)});
                        }, 100);
                    };
                    o.onClose = function(selectedDate) {
                        $(element).removeClass("popup-picker");
                        $(element).focus();
                    };
                }
                
                if ($(element).val() === "" && o.yearRange !== undefined && o.datePickerType !== "timeOnly") {
                    var yearRange = o.yearRange;
                    if (yearRange.indexOf("-", 2) > 0) {
                        yearRange = yearRange.substring(yearRange.indexOf("-", 2));
                        o.defaultDate = yearRange + "y";
                    } else if (yearRange.indexOf(":c") < 0 
                            && yearRange.indexOf(":+") < 0 
                            && yearRange.indexOf(":-") < 0 
                            && yearRange.substring(yearRange.indexOf(":") + 1).length === 4) {
                        yearRange = yearRange.substring(yearRange.indexOf(":") + 1);
                        var d = new Date();
                        if (yearRange < d.getFullYear()) {
                            d.setFullYear(yearRange);
                            o.defaultDate = d;
                        }
                    }
                }
                
                if (o.datePickerType === "dateTime") {
                    $(element).datetimepicker(o);
                } else if (o.datePickerType === "timeOnly") {
                    $(element).timepicker(o);
                } else {
                    $(element).datepicker(o);
                }
                
                if($.placeholder) {
                    $(element).placeholder();
                }
                
                var a = $("<a>").attr("href","#");
                $(element).next("img.ui-datepicker-trigger").wrap("<a class=\"trigger\" href=\"#\"></a>");

                $(element).next("a.trigger").after("<a class=\"close-icon\" type=\"reset\"></a>");
                
                $(element).change(function() {
                    if ( $(element).val() !== "" ) {
                        $(elementParent).find("a.close-icon").show();
                    } else {
                        $(elementParent).find("a.close-icon").hide();
                    }
                });
                
                //Always check first if value already exists
                $(element).change();
                
                $(elementParent).find("a.close-icon").click(function(){
                    $(element).val("").change();
                });
                
                $(elementParent).find("input[readonly] , a.trigger").click(function(evt){
                    evt.preventDefault();
                    evt.stopImmediatePropagation();
                    $(element).datepicker("show");
                });
                $(elementParent).find("input , a.trigger").off("keydown").on("keydown", function(evt){
                    if (evt.keyCode === 13) {
                        evt.preventDefault();
                        evt.stopPropagation();
                        $(element).datepicker("show");
                    }
                }).on("focus", function() {
                    $(element).addClass("focus");
                }).on("focusout", function(){
                    if (!$(element).hasClass("popup-picker")) {
                        $(element).removeClass("focus");
                    }
                });
                $(elementParent).find("input[readonly]").on("keydown", function(evt){
                    if (evt.keyCode === 8) {
                        $(element).val("").change();
                        $(element).datepicker("hide");
                        return false;
                    }
                });
                
                if (o.startDateFieldId  !== undefined && o.startDateFieldId !== "") {
                    var startDate = FormUtil.getField(o.startDateFieldId);
                    var startDateOrg = null;
                    startDate.on("change", function() {
                        if (startDateOrg !== $(startDate).val()) {
                            startDateOrg = $(startDate).val();
                            setDateRange(startDate, "minDate", element, o);
                        }
                    });
                    setDateRange(startDate, "minDate", element, o);
                }
                
                if (o.endDateFieldId  !== undefined && o.endDateFieldId !== "") {
                    var endDate = FormUtil.getField(o.endDateFieldId);
                    var endDateOrg = null;
                    endDate.on("change", function() {
                        if (endDateOrg !== $(endDate).val()) {
                            endDateOrg = $(endDate).val();
                             setDateRange(endDate, "maxDate", element, o);
                        }
                    });
                    setDateRange(endDate, "maxDate", element, o);
                }
                
                if (o.currentDateAs !== undefined && o.currentDateAs !== "") {
                    var option = $(element).datepicker( "option", o.currentDateAs);
                    if (option === undefined || option === null) {
                        $(element).next(".trigger").remove();
                        $(element).datepicker("option", o.currentDateAs, new Date());
                        $(element).next("img.ui-datepicker-trigger").wrap("<a class=\"trigger\" href=\"#\"></a>");
                    }
                }
            });
        }
    });
    
    function setDateRange(element, type, target, o) {
        var value = $(element).val();
        if (value === "" && $(target).datetimepicker("option", type) === null) {
            return;
        }
        $(target).next(".trigger").remove();
        if (o.datePickerType === "dateTime") {
            $(target).datetimepicker("option", type, value);
        } else if (o.datePickerType === "timeOnly") {
            if (type === "maxDate") {
                type = "maxTime";
            } else {
                type = "minTime";
            }
            $(target).timepicker("option", type, value);
        } else {
            $(target).datepicker("option", type, value);
        }
        $(target).next("img.ui-datepicker-trigger").wrap("<a class=\"trigger\" href=\"#\"></a>");
    }
})(jQuery);