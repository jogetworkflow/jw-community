(function($){
    $.fn.extend({
        cdatepicker : function(o){
            this.each(function(){
                var element = $(this);
                var elementParent = element.parent();
                var uid = $(element).attr("id");
                var value = $(element).val();

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
                
                var a = $("<a>").attr("href","#");
                if(!$(element).next("img.ui-datepicker-trigger").length){
                    $(element).insertBefore($(element).prev('img.ui-datepicker-trigger'));
                }
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
                    showDatepicker($(element));
                });
                $(elementParent).find("input , a.trigger").off("keydown").on("keydown", function(evt){
                    if (evt.keyCode === 13) {
                        evt.preventDefault();
                        evt.stopPropagation();
                        showDatepicker($(element));
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
                    var endDate = FormUtil.getField(o.endDateFieldId);
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
                        $(element).next(".trigger").remove();
                        $(element).datepicker("option", o.currentDateAs, new Date());
                        $(element).next("img.ui-datepicker-trigger").wrap("<a class=\"trigger\" href=\"#\"></a>");
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
    });
    
    //check the browser is support the input type
    function isSupport(type) {
        if (/Mobi/.test(navigator.userAgent)) { //only do it for mobile
            var input = document.createElement('input');
            input.setAttribute('type',type);

            var notADateValue = 'not-a-date';
            input.setAttribute('value', notADateValue); 

            return (input.value !== notADateValue);
        }
        return false;
    };
    
    //create a hidden date input for the picker
    function createNativeField(element, type, o) {
        if (isSupport(type)) {
            var attr = "";
            
            var cssClass = "";
            if (/iPhone/.test(navigator.userAgent) || /iPad/.test(navigator.userAgent)) {
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
            if (!$(element).prev(".ui-screen-hidden").hasClass("ios")) {
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
    function setDate(element, date, o) {
        if (o.datePickerType === "dateTime") {
            $(element).datetimepicker("setDate", new Date(date) );
        } else if (o.datePickerType === "timeOnly") {
            $(element).timepicker("setTime", date);
        } else {
            $(element).datepicker("setDate", new Date(date) );
        }
        $(element).focus().trigger("change");
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
})(jQuery);