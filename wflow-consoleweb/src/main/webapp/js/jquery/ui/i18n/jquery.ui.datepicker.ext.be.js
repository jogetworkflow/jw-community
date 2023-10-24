/*
 * Buddhist Era ( BE. ) Plugins for jQuery UI Datepicker
 * Version 0.2 Alpha
 * Copyright 2011, Sorajate Maiprasert  ( Sorajate @ gmail.com )
 * Licensed under GPL Version 2 license.
 * Tested On Datepicker V 1.8.13
 * Depends:
 *	jquery.ui.datepicker.js
 * Usage:
 * - To Enable Buddhist Era  set option iSBE to true
 * - To Enable Auto Conversion hidden field set autoConversionField to true ( it is true by default if isBE is true )
 *
 * Changes:
 * - Tested on jQuery 3.5.1, Datepicker 1.13.1, Datetimepicker 1.6.3
 * - _toBE function added by owen convert date to BE 
 * - _restrictMinMax function added by owen to fix range limit to working for BE
 * - _daylightSavingAdjust function added by owen to fix today & selected date is not highlighted
 * - _showDatepicker function modified by cchunzhe to support "showOn: button" option
 * - _showDatepicker's extend _updateDateTime function modified by cchunzhe to fix datetimepicker year reverting to
 *      Gregorian year
 * - Removed _generateMonthYearHeader as it was redundant and interfering with the year select box
 * - Added new method override for _setDateFromField to fix datepicker displaying incorrect month after picking a date
 */
(function ($) {
    //Proxy Pattern
    var d_showDatepicker = $.datepicker._showDatepicker;
    var d_selectDay = $.datepicker._selectDay;
    var d_selectDate = $.datepicker._selectDate;
    var d_attachDatepicker = $.datepicker._attachDatepicker;
    var d_adjustInstDate = $.datepicker._adjustInstDate;
    var d_inlineDatepicker = $.datepicker._inlineDatepicker;
    var d_setDate = $.datepicker._setDate;
    var d_updateDatepicker = $.datepicker._updateDatepicker;
    var d_generateHTML = $.datepicker._generateHTML;
    var d_setDateFromField = $.datepicker._setDateFromField;
    var d_restrictMinMax = $.datepicker._restrictMinMax;
    var d_daylightSavingAdjust = $.datepicker._daylightSavingAdjust;

    //Insert default parameter to datepicker
    $.extend($.datepicker._defaults, {
        isBE: false,
        autoConversionField: true
    });

    $.extend($.datepicker, {

        /* Parse existing date and initialise date picker. */
        _setDateFromField: function(inst, noDefault) {
            if (inst.input.val() == inst.lastVal) {
                return;
            }
            var dateFormat = this._get(inst, 'dateFormat');
            var dates = inst.lastVal = inst.input ? inst.input.val() : null;
            var isBE = $.datepicker._isBE(inst);
            var hasAutoConvert = $.datepicker._hasAutoConvert(inst);
            var date, defaultDate, dateYear;
            date = defaultDate = this._getDefaultDate(inst);
            var settings = this._getFormatConfig(inst);
            try {
                date = this.parseDate(dateFormat, dates, settings) || defaultDate;
            } catch (event) {
                if (console.log) {
                    console.log(event);
                }
                dates = (noDefault ? '' : dates);
            }

            // to convert date for displaying correct calendar month
            // if "is thai year" and "is not UTC" (autoconvert changes thai year to gregorian year)
            if (isBE && !hasAutoConvert) {
                dateYear = date.getFullYear();
                date.setFullYear( $.datepicker._convertToCe(dateYear) );
            }

            inst.selectedDay = date.getDate();
            inst.drawMonth = inst.selectedMonth = date.getMonth();
            inst.drawYear = inst.selectedYear = date.getFullYear();
            inst.currentDay = (dates ? date.getDate() : 0);
            inst.currentMonth = (dates ? date.getMonth() : 0);
            inst.currentYear = (dates ? date.getFullYear() : 0);

            // revert date back
            if (isBE && !hasAutoConvert) {
                date.setFullYear( $.datepicker._convertToBe(dateYear) );
            }
            this._adjustInstDate(inst);
        },

        _attachBE: function () {
            $.extend($.datepicker._defaults, {
                beforeShow: function () {
                }
            });

        },

        _selectDate: function (id, dateStr) {
            d_selectDate.apply(this, arguments);
        },

        /*Override methods*/
        _attachDatepicker: function (target, settings) {
            d_attachDatepicker.apply(this, arguments);
            var autoConvert = (typeof (settings.autoConversionField) !== 'undefined' && settings.autoConversionField != null) ? settings.autoConversionField : true;
            var _isBE = (typeof (settings.isBE) !== 'undefined' && settings.isBE != null) ? settings.isBE : false;
            var _isInput = $(target).is("input")
            if (autoConvert == true && _isBE == true && _isInput == true) {  //Only for input for now
                $.datepicker._attachConversion(target);
            }
        },

        _attachConversion: function (target) {
            var _name = $(target).attr('name');
            var _id = $(target).attr('id');
            var convertId = _id + "_convert";
            $(target).parent().append('<input type="hidden" rel="' + _id + '" id="' + convertId + '" name="' + _name + '" />');
            //change original name
            $(target).attr('name', convertId);
        },

        _adjustInstDate: function (inst, offset, period) {
            var _isBE = (typeof (inst.settings.isBE) !== 'undefined' && inst.settings.isBE != null) ? inst.settings.isBE : false;
            if (_isBE == true) {
                //Change drawYear back to CE after it got parse
                inst.drawYear = $.datepicker._convertToCe(inst.drawYear);
            }
            d_adjustInstDate.apply(this, arguments);
        },

        _selectDay: function (id, month, year, td) {
            //need the instance
            var target = $(id);
            if ($(td).hasClass(this._unselectableClass) || this._isDisabledDatepicker(target[0])) {
                return;
            }
            var _inst = this._getInst(target[0]);
            var _isBE = (typeof (_inst.settings.isBE) !== 'undefined' && _inst.settings.isBE != null) ? _inst.settings.isBE : false;
            var autoConvert = (typeof (_inst.settings.autoConversionField) !== 'undefined' && _inst.settings.autoConversionField != null) ? _inst.settings.autoConversionField : true;
            var d_year = $.datepicker._convertToCe(year);

            if (_isBE == true) { //Fire only when isBE
                year = $.datepicker._convertToBe(year);
            }

            d_selectDay.apply(this, arguments);
            //Restore value
            //_inst.selectedYear = _inst.currentYear = _inst.drawYear =  d_year;
            _inst.currentYear = $.datepicker._convertToCe(_inst.currentYear);
            _inst.drawYear = $.datepicker._convertToCe(_inst.drawYear);
            _inst.selectedYear = $.datepicker._convertToCe(_inst.selectedYear);
            if (autoConvert == true && _isBE == true) {
                //Parse value to child element
                var childName = target.attr('name');
                var childId = '#' + childName;
                var childValue = this._formatDate(_inst, _inst.currentDay, _inst.currentMonth, d_year);
                $(childId).val(childValue);
            }
        },

        _showDatepicker: function (input) {
            // copied from original JQuery UI datepicker
            // for compatibility with "showOn: button" option
            // == START MODIFIED SECTION ==
            input = input.target || input;
            if (input.nodeName.toLowerCase() !== "input") { // find from button/image trigger
                input = $("input", input.parentNode)[0];
            }

            if ($.datepicker._isDisabledDatepicker(input) || $.datepicker._lastInput === input) { // already here
                return;
            }
            var _inst = $.datepicker._getInst(input);
            // == END MODIFIED SECTION ==
            var _isBE = $.datepicker._isBE(_inst);

            //Datetimepicker Compatible
            if (jQuery.timepicker && _isBE == true) {
                var tp_inst = $.datepicker._get(_inst, 'timepicker');
                if (tp_inst) {
                    var tp_updateDateTime = tp_inst._updateDateTime;
                    $.extend(_inst.settings.timepicker, {
                        _updateDateTime: function (dp_inst) {
                            dp_inst = _inst || dp_inst
                            dp_inst.selectedYear = $.datepicker._convertToBe(dp_inst.selectedYear);
                            dp_inst.currentYear = $.datepicker._convertToBe(dp_inst.currentYear);

                            tp_updateDateTime.apply(this, arguments);

                            //Restore after finish use
                            dp_inst.selectedYear = $.datepicker._convertToCe(dp_inst.selectedYear);
                            dp_inst.currentYear = $.datepicker._convertToCe(dp_inst.currentYear);
                        }
                    });
                }
            }

            d_showDatepicker.apply(this, arguments);
        },

        _inlineDatepicker: function (target, inst) {
            d_inlineDatepicker.apply(this, arguments);
        },

        _updateDatepicker: function (inst) {
            var _inst = inst;
            var _isBE = (typeof (_inst.settings.isBE) !== 'undefined' && _inst.settings.isBE != null) ? _inst.settings.isBE : false;
            d_updateDatepicker.apply(this, arguments);

            if (_isBE == true) { //Fire only when isBE
                $.datepicker._SetBEDisplay(_inst);
            }

        },

        _setDate: function (inst, date, noChange) {
            d_setDate.apply(this, arguments);
        },

        _generateHTML: function (inst) {
            var _isBE = (typeof (inst.settings.isBE) !== 'undefined' && inst.settings.isBE != null) ? inst.settings.isBE : false;
            if (_isBE) {
                //This will restore the selected
                if (inst.currentYear != 0) {
                    inst.currentYear = $.datepicker._convertToCe(inst.currentYear);
                }
            }
            var result = d_generateHTML.apply(this, arguments);
            //Restore value
            if (_isBE) {
                inst.currentYear = $.datepicker._convertToCe(inst.currentYear);
                inst.drawYear = $.datepicker._convertToCe(inst.drawYear);
                inst.selectedYear = $.datepicker._convertToCe(inst.selectedYear);
            }
            return result;
        },
        
        _restrictMinMax: function( inst, date ) {
            date = this._toBE(date);
            var minDate = this._getMinMaxDate( inst, "min" ),
                    maxDate = this._getMinMaxDate( inst, "max" ),
                    newDate = ( minDate && date < minDate ? minDate : date );
            return ( maxDate && newDate > maxDate ? maxDate : newDate );
	},
        
        _daylightSavingAdjust: function( date ) {
            if ( !date ) {
                    return null;
            }
            date.setHours( date.getHours() > 12 ? date.getHours() + 2 : 0 );
            
            if (date.getFullYear() === (new Date()).getFullYear()) {
                date = this._toBE(date);
            }
            return date;
	},

        _SetBEDisplay: function (inst) {
            var _inst = inst;
            var element = _inst.inline == true ? _inst.dpDiv : _inst.dpDiv[0];
            var drawYear = _inst.drawYear;
            if (drawYear == 0) {
                drawYear = _inst.currentYear;
            }
            drawYear = $.datepicker._convertToCe(drawYear);
            var selectedYear = _inst.selectedYear = _inst.currentYear = _inst.drawYear = $.datepicker._convertToCe(inst.selectedYear);

            if (selectedYear == 0) {
                selectedYear = new Date().getFullYear();
            }
            var yF = $(element).find('.ui-datepicker-year');

            var domEl = $(yF).get(0);

            if (!domEl) {
                return false;
            }

            if (typeof (domEl.tagName) !== 'undefined' && domEl.tagName != null) {
                switch (domEl.tagName) {
                    case "SELECT" :  //Change all value
                        $(domEl).children('option').each(function () {
                            $(this).text($.datepicker._convertToBe($(this).val()));
                            var _year = $(this).val();
                            if (_year == drawYear) {
                                $(this).attr('selected', 'selected');
                            }

                        });
                        break;
                    case "SPAN" :
                        //Use generate month header instead
                        break;
                    default :
                        //console.log(domEl.tagName);
                        break;
                }
            }

        },

        _isBE: function (inst) {
            var _isBE = (typeof (inst.settings.isBE) !== 'undefined' && inst.settings.isBE != null) ? inst.settings.isBE : false;
            return _isBE;
        },

        _hasAutoConvert: function (inst) {
            var autoConvert = (typeof (inst.settings.autoConversionField) !== 'undefined' && inst.settings.autoConversionField != null) ? inst.settings.autoConversionField : true;
            return autoConvert;
        },

        _convertToBe: function (year) {
            if ((parseInt(year) - 543) < 1900) {
                return parseInt(year) + 543;
            } else {
                return year;
            }
        },

        _convertToCe: function (year) {
            if ((parseInt(year) - 543) >= 1900) {
                return parseInt(year) - 543;
            } else {
                return year;
            }
        },
        
        _toBE: function (date) {
            var year = date.getFullYear();
            if ((parseInt(year) - 543) < 1900) {
                year = parseInt(year) + 543;
                date.setFullYear(year);
            }
            return date;
        }
    });

})(jQuery);
