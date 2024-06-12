(function($){
    $.fn.extend({
        cdatepicker : function(o){
            if (o === "setDateRange") {
                setDateRange.apply(this, Array.prototype.slice.call( arguments, 1 ));
            } else {
                $(this).each(function(){
                    var element = $(this);
                    var elementParent = element.parent();
                    var uid = $(element).attr("id");
                    var value = $(element).val();
                    
                    $(element).data("options", o);

                    if (!isIOS()) { 
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
                        createNativeField($(element), "datetime-local", o);
                    } else if (o.datePickerType === "timeOnly") {
                        $(element).timepicker(o);
                        createNativeField($(element), "time", o);
                    } else {
                        $(element).datepicker(o);
                        createNativeField($(element), "date", o);
                    }

                    if($.placeholder) {
                        $(element).placeholder();
                    }

                    //modify the date trigger
                    setTimeout(function(){
                        //for Arabic
                        if($(element).next("img.ui-datepicker-trigger").length === 0 && $(element).prev("img.ui-datepicker-trigger").length !== 0){
                            $(element).after($(element).prev('img.ui-datepicker-trigger'));
                        }
                        if($(element).next("img.ui-datepicker-trigger").length !== 0){
                            $(element).next("img.ui-datepicker-trigger").wrap("<a class=\"trigger\"></a>");
                        }

                        var clearBtn = $("<a class=\"close-icon\"></a>");
                        $(element).next("a.trigger").after(clearBtn);

                        $(element).off("change.clearBtn");
                        $(element).on("change.clearBtn", function() {
                            if ( $(element).val() !== "" ) {
                                $(clearBtn).show();
                            } else {
                                $(clearBtn).hide();
                            }
                        });

                        //Always check first if value already exists
                        $(element).change();

                        $(clearBtn).off("click");
                        $(clearBtn).on("click", function(){
                            $(element).val("").change();
                        });

                        $(elementParent).off("click.dp", "input[readonly] , a.trigger");
                        $(elementParent).on("click.dp", "input[readonly] , a.trigger", function(evt){
                            evt.preventDefault();
                            evt.stopImmediatePropagation();
                            showDatepicker($(element));
                        });
                        $(elementParent)
                        .off("keydown.dp", "input , a.trigger")
                        .off("focus.dp", "input , a.trigger")
                        .off("focusout.dp", "input , a.trigger")
                        .on("keydown", "input , a.trigger", function(evt){
                            if (evt.keyCode === 13) {
                                evt.preventDefault();
                                evt.stopPropagation();
                                showDatepicker($(element));
                            }
                        }).on("focus.dp", "input , a.trigger", function() {
                            $(element).addClass("focus");
                        }).on("focusout.dp", "input , a.trigger", function(){
                            if (!$(element).hasClass("popup-picker")) {
                                $(element).removeClass("focus");
                            }
                        });

                        $(elementParent).find("input[readonly]").off("keydown.dp");
                        $(elementParent).find("input[readonly]").on("keydown.dp", function(evt){
                            if (evt.keyCode === 8) {
                                $(element).val("").change();
                                $(element).datepicker("hide");
                                return false;
                            }
                        });
                    }, 1);  
                    
                    var parent;
                    if ($(element).closest(".subform-container").length > 0) {
                        parent = $(element).closest(".subform-container");
                    }
                
                    if (o.startDateFieldId  !== undefined && o.startDateFieldId !== "") {
                        // try get from the same subform first
                        var startDate = FormUtil.getField(o.startDateFieldId, parent);
                        if (parent && $(startDate).length === 0) {
                            //find from the entire form
                            startDate = FormUtil.getField(o.startDateFieldId);
                        }
                    
                        var startDateOrg = null;
                        startDate.off("change.startdate"+uid);
                        startDate.on("change.startdate"+uid, function() {
                            if (startDateOrg !== $(startDate).val()) {
                                startDateOrg = $(startDate).val();
                                setDateRange(startDate, "minDate", element, o);
                            }
                        });
                        setDateRange(startDate, "minDate", element, o);
                    }

                    if (o.endDateFieldId  !== undefined && o.endDateFieldId !== "") {
                        // try get from the same subform first
                        var endDate = FormUtil.getField(o.endDateFieldId, parent);
                        if (parent && $(endDate).length === 0) {
                            //find from the entire form
                            endDate = FormUtil.getField(o.endDateFieldId);
                        }
                        
                        var endDateOrg = null;
                        endDate.off("change.endDate"+uid);
                        endDate.on("change.endDate"+uid, function() {
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
                            var date = new Date();
                            if (o.isBE !== undefined && o.isBE) {
                                date = convertToBe(date);
                            }
                            $(element).datepicker("option", o.currentDateAs, date);
                        }
                    }

                    if ((o.datePickerType !== "dateTime" && o.datePickerType !== "timeOnly") //only apply for datepicker
                            && o.dateFormat.indexOf("d") === -1  //when the format is without a day syntax
                            && value !== null && value !== undefined && value !== "") { //there is a default value
                        //use setTimeout to make sure if there is set other field as min/max, the field are ready before setting this value
                        setTimeout(function(){
                            //set the value again with a first day of the month, 
                            //just simple add the day syntax and 01 value and it seem to work with all formats.
                            //it is ok to set a date over the min/max date limit, the field will auto adjust to the min/max date value if it is invalid/over the limit
                            $(element).datepicker("setDate", $.datepicker.parseDate('dd/'+ o.dateFormat, '01/'+value));
                        }, 1);
                    }
                });
            }
        }
    });
    
    //check is ipad device, ipad pro userAgent no more contains mobile keyword
    function isIpadPro() {
        return /Macintosh/.test(navigator.userAgent) && navigator.maxTouchPoints && navigator.maxTouchPoints > 2;
    };
    
    //check is IOS device
    function isIOS() {
        return /iPhone|iPod|iPad/.test(navigator.userAgent) || isIpadPro();
    }
    
    //check the browser is support the input type
    function isSupport(type) {
        if (/Mobi/.test(navigator.userAgent) || isIpadPro()) { //only do it for mobile
            var input = document.createElement('input');
            input.setAttribute('type',type);

            var notADateValue = 'not-a-date';
            input.setAttribute('value', notADateValue); 

            return (input.value !== notADateValue) && //if date field is supported
                    (isIOS() || (!isIOS() && input['showPicker'] !== undefined)); //is ios or showPicker is supported
        }
        return false;
    }
    
    //create a hidden date input for the picker
    function createNativeField(element, type, o) {
        if (isSupport(type)) {
            var attr = "";
            
            var cssClass = "";
            if (isIOS()) {
                cssClass += "ios";
            }
            if (!$(element).is("[readonly]")) {
                cssClass += " manual";
            }
            
            $(element).before('<div class="ui-screen-hidden datapicker-native '+cssClass+'"><input class="native-picker" type="'+type+'" '+attr+'/></div>');
            $(element).addClass('use-native');
            
            var nativeField = $(element).prev(".ui-screen-hidden").find('.native-picker');
                    
            var nativeChange = function(){
                if (!$(element).is("[readonly]")) {
                    $(element).off("change.manual");
                }
                setDate($(element), $(this).val(), o);
                if (!$(element).is("[readonly]")) {
                    $(element).on("change.manual", manualChange);
                }
            };
            
            var manualChange = function() {
                $(nativeField).off("change.native");
                setNativeDate($(element), $(nativeField), o);
                $(nativeField).on("change.native", nativeChange);
            };
            
            setNativeDate($(element), $(nativeField), o);
            $(nativeField).on("change.native", nativeChange);
            
            if (!$(element).is("[readonly]")) {
                $(element).on("change.manual", manualChange);
            }
        }
    }
    
    //show the native picker or the jquery picker
    function showDatepicker(element) {
        if ($(element).hasClass("use-native")) {
            var nativeField = $(element).prev(".ui-screen-hidden").find('.native-picker')[0];
            if (!$(element).prev(".ui-screen-hidden").hasClass("ios")) { //only do it for non ios device, ios device is using css to place above field
                try {
                    nativeField.showPicker(); 
                } catch (e) {
                    $(element).datepicker("show");
                    $(element).removeClass("use-native");
                }    
            }
        } else {
            $(element).datepicker("show");
        }
    };

    //update input field when native field changed
    function setDate(element, date, o, focus) {
        if (o.datePickerType === "dateTime") {
            $(element).datetimepicker("setDate", new Date(date) );
        } else if (o.datePickerType === "timeOnly") {
            $(element).timepicker("setTime", date);
        } else {
            $(element).datepicker("setDate", new Date(date) );
        }
        
        //should not focus when native field is used.
        if (!$(element).hasClass("use-native")) {
            $(element).focus().trigger("change");
        } else {
            $(element).trigger("change");
        }
    };
    
    //update native field when input field changed
    function setNativeDate(element, nativeField, o) {
        var date = $(element).datepicker("getDate");
        if (date !== null) {
            date = toDateString(date);
            if (o.datePickerType === "dateTime") {
                $(nativeField).val(date.substr(0, 16));
            } else if (o.datePickerType === "timeOnly") {
                $(nativeField).val(date.substr(11, 16));
            } else {
                $(nativeField).val(date.substr(0, 10));
            }
        }
    };
    
    //format the date value to string
    function toDateString(date) {
        var newdate = new Date(date.getTime() - (date.getTimezoneOffset() * 60000));
        return newdate.toISOString();
    }
                
    function setDateRange(element, type, target, o) {
        var value = $(element).val();
        if (value === "" && $(target).datetimepicker("option", type) === null) {
            return;
        }
        if ($.datepicker._getInst($(element)[0]) !== undefined) {
            //use to make sure the value use as min/max are always a valid date if the element itself is a datapicker field
            value = $(element).datepicker("getDate"); 
            if (o.isBE !== undefined && o.isBE) {
                value = convertToBe(value);
            }
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
        
        //modify the date trigger
        setTimeout(function(){
            //for Arabic
            if($(element).next("img.ui-datepicker-trigger").length === 0 && $(element).prev("img.ui-datepicker-trigger").length !== 0){
                $(element).after($(element).prev('img.ui-datepicker-trigger'));
            }
            if($(target).next("img.ui-datepicker-trigger").length === 0 && $(target).prev("img.ui-datepicker-trigger").length !== 0){
                $(target).after($(target).prev('img.ui-datepicker-trigger'));
            }
            if($(element).next("img.ui-datepicker-trigger").length !== 0){
                $(element).next("img.ui-datepicker-trigger").wrap("<a class=\"trigger\"></a>");
            }
            if($(target).next("img.ui-datepicker-trigger").length !== 0){
                $(target).next("img.ui-datepicker-trigger").wrap("<a class=\"trigger\"></a>");
            }
        },1);

        
        if ($(element).hasClass("use-native")) {
            var date = $(element).datepicker("getDate");
            var nativePicker = $(target).prev(".ui-screen-hidden").find('.native-picker');
            
            if (date !== null) {
                date = toDateString(date);
                var v = "";
                if (o.datePickerType === "dateTime") {
                    v = date.substr(0, 16);
                } else if (o.datePickerType === "timeOnly") {
                    v = date.substr(11, 16);
                } else {
                    v = date.substr(0, 10);
                }
                if (type === "maxDate") {
                    $(nativePicker).attr("max", v);
                } else {
                    $(nativePicker).attr("min", v);
                }
            }
        }
    }
    
    function convertToBe(date) {
        var year = date.getFullYear();
        if ((parseInt(year) - 543) < 1900) {
            year = parseInt(year) + 543;
            date.setFullYear(year);
        }
        return date;
    }
})(jQuery);