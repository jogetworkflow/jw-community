FormUtil = {
    controlFields : [],
    getValue : function(fieldId, element){
        var value = "";
        var field = FormUtil.getField(fieldId, element);
        if ($(field).length > 0) {
            if ($(field).attr("type") == "checkbox" || $(field).attr("type") == "radio") {
                field = $(field).filter(":checked");
            } else if ($(field).is("select")) {
                field = $(field).find("option:selected");
            }
            
            value = $(field).val();
        }
        return value;
    },
    
    getValues : function(fieldId, element){
        var values = new Array();
        
        if (fieldId.indexOf(".") > 0) { // grid cell values
            values = FormUtil.getGridCellValues(fieldId, element);
        } else {
            var field = FormUtil.getField(fieldId, element);
            if ($(field).length > 0) {
                if ($(field).attr("type") == "checkbox" || $(field).attr("type") == "radio") {
                    field = $(field).filter(":checked");
                } else if ($(field).is("select")) {
                    field = $(field).find("option:selected");
                }

                $(field).each(function() {
                    values.push($(this).val());
                });
            }
        }
        return values;
    },
    
    getField : function(fieldId, element){
        if (element === undefined || $(element).length === 0) {
            element = $(document.body);
        }
        var field = $(element).find("[name="+fieldId+"]:not(form)");
        if ($(field).length == 0) {
            field = $(element).find("[name$=_"+fieldId+"]:not(form)");
        }
        
        //filter those in hidden section
        field = $(field).filter(':parents(.section-visibility-hidden)');
        
        //to prevent return field with similar name, get the field with shorter name (Field in the subform)
        if ($(field).length > 1) {
            var fieldname;
            $(field).each(function(){
                if (fieldname === undefined) {
                    fieldname = $(this).attr("name");
                }
                if ($(this).attr("name").length < fieldname.length) {
                    fieldname = $(this).attr("name");
                }
                field = $("[name="+fieldname+"]:not(form)");
            });
        }
        
        return field;
    },
    
    getGridCells : function(cellFieldId, element){
        var fieldId = cellFieldId.split(".")[0];
        var cells = null;
        
        var field = FormUtil.getField(fieldId, element);
        var gridDataObject = field.data("gridDataObject");
        if (gridDataObject !== null && gridDataObject !== undefined) {
            var cellId = cellFieldId.split(".")[1];
            
            //build dummy hidden fields for plugins using this method
            cells = new Array();
            for (var i in gridDataObject) {
                var value = gridDataObject[i][cellId];
                var temp = $("<input type='hidden'/>").val(value);
                cells.push(temp);
            }
        } else {
            cellFieldId = cellFieldId.replace(/\./g, '_');
            cells = $(field).find("[name=" + cellFieldId + "], [name$=_" + cellFieldId + "]");
            //filter those in template 
            cells = $(cells).filter(':parents(.grid-row-template)');
        }
        return cells;
    },
    
    getGridCellValues : function (cellFieldId, element) {
        var fieldId = cellFieldId.split(".")[0];
        var values = new Array();
        
        var field = FormUtil.getField(fieldId, element);
        
        var gridDataObject = field.data("gridDataObject");
        if (gridDataObject !== null && gridDataObject !== undefined) {
            var cellId = cellFieldId.split(".")[1];
            
            for (var i in gridDataObject) {
                var value = gridDataObject[i][cellId];
                values.push(value);
            }
        } else {
            field.find("tr.grid-row").each(function() {
                if ($(this).find("textarea[id$=_jsonrow]").length > 0) {
                    var cellId = cellFieldId.split(".")[1];

                    //get json data from hidden textarea
                    var data = $(this).find("textarea[id$=_jsonrow]").val();
                    var dataObj = $.parseJSON(data);

                    if (dataObj[cellId] !== undefined) {
                        values.push(dataObj[cellId]);
                    }
                } else {
                    var cellId = cellFieldId.replace(/\./g, '_');
                    var cell = $(this).find("[name=" + cellId + "], [name$=_" + cellId + "]");
                    if (cell.length > 0) {
                        values.push(cell.text());
                    }
                }
            });
        }
        
        return values;
    },
    
    setControlField : function(fieldId, selector) {
        if (selector !== undefined) {
            $(selector).addClass("control-field");
        } else {
            $("[name='"+fieldId+"']:not(form)").addClass("control-field");
        }
        if (FormUtil.controlFields.indexOf(fieldId) === -1) {
            FormUtil.controlFields.push(fieldId);
        }
    },
    
    isControlField : function(fieldId, field) {
        if (FormUtil.controlFields.indexOf(fieldId) !== -1 || $(field).hasClass("control-field")) {
            return true;
        } else {
            //handle for subform
            for (var i = 0; i < FormUtil.controlFields.length; i++) {
                if (FormUtil.controlFields[i].indexOf("_") === 0 && (fieldId.length >= FormUtil.controlFields[i].length && fieldId.substring(fieldId.length - FormUtil.controlFields[i].length) === FormUtil.controlFields[i])) {
                    return true;
                }
            }
            return false;
        }
    },
    
    getFieldsAsUrlQueryString : function(fields, element) {
        var queryString = "";
        
        if (fields !== undefined) {
            $.each(fields, function(i, v){
                var values = [];
                
                if (v['field'] !== "") {
                    values = FormUtil.getValues(v['field'], element);
                }
                
                if (values.length === 0 && v['defaultValue'] !== "") {
                    values = [v['defaultValue']];
                }
                
                if (values.length === 0) {
                    values = [""];
                }
                
                for (var j in values) {
                    queryString += encodeURIComponent(v['param']) + "=" + encodeURIComponent(values[j]) + "&";
                }
            });
            
            if (queryString !== "") {
                queryString = queryString.substring(0, queryString.length-1);
            }
        }
        
        return queryString;
    },
    
    numberFormat : function (value, options){
        var numOfDecimal = parseInt(options.numOfDecimal);
        var decimalSeperator = ".";
        var regexDecimalSeperator = "\\\.";
        var thousandSeparator = ",";
        var regexThousandSeparator = ",";
        if(options.format.toUpperCase() === "EURO"){
            decimalSeperator = ",";
            regexDecimalSeperator = ",";
            thousandSeparator = ".";
            regexThousandSeparator = "\\\.";
        }
        
        var number = value.replace(/\s/g, "");
        number = number.replace(new RegExp(regexThousandSeparator, 'g'), '');
        number = number.replace(new RegExp(regexDecimalSeperator, 'g'), '.');
        if(options.prefix !== ""){
            number = number.replace(options.prefix, "");
        }
        if(options.postfix !== ""){
            number = number.replace(options.postfix, "");
        }
                
        var exponent = "";
        if (!isFinite(number)) {
            number = 0;
        } else {
            number = Number(number);
            if (numOfDecimal !== null){
                number = number.toFixed(numOfDecimal);
            } else {
                number = number.toFixed(0);
            }
            
            var numberstr = number.toString();
            var eindex = numberstr.indexOf("e");
            if (eindex > -1){
                exponent = numberstr.substring(eindex);
                number = parseFloat(numberstr.substring(0, eindex));
            }

            if (numOfDecimal !== null){
                var temp = Math.pow(10, numOfDecimal);
                number = Math.round(number * temp) / temp;
            }
        }
        
        var sign = number < 0 ? "-" : "";
        
        var integer = (number > 0 ? Math.floor (number) : Math.abs (Math.ceil (number))).toString ();
        var fractional = number.toString ().substring (integer.length + sign.length);
        fractional = numOfDecimal !== null && numOfDecimal > 0 || fractional.length > 1 ? (decimalSeperator + fractional.substring (1)) : "";
        if(numOfDecimal !== null && numOfDecimal > 0){
            for (i = fractional.length - 1, z = numOfDecimal; i < z; ++i){
                fractional += "0";
            }
        }
        
        if(options.useThousandSeparator.toUpperCase() === "TRUE"){
            for (i = integer.length - 3; i > 0; i -= 3){
                integer = integer.substring (0 , i) + thousandSeparator + integer.substring (i);
            }
        }
        
        var resultString = "";
        if(sign !== ""){
            resultString += sign;
        }
        if(options.prefix !== ""){
            resultString += options.prefix + ' ';
        }
        resultString += integer + fractional;
        if(exponent !== ""){
            resultString += ' ' + exponent;
        }
        if(options.postfix !== ""){
            resultString += ' ' + options.postfix;
        }
        
        return  resultString;
    },
    
    populateTooltip : function(tooltips) {
        if (tooltips !== undefined && tooltips !== null) {
            $.each(tooltips, function(key, tooltip) {
                var label = null;
                var key = key.substring(8);
                var selector = key.replace(".", "_");
                var subselector = key.substring(key.indexOf(".") + 1);
                if ($("label[field-tooltip$=" + selector + "]").length > 0) {
                    label = $("label[field-tooltip$=" + selector + "]");
                } else if ($("form#"+ key.substring(0, key.indexOf(".")) +" .form-cell label[field-tooltip$=" + subselector + "]").length > 0) {
                    label = $("form#"+ key.substring(0, key.indexOf(".")) +" .form-cell label[field-tooltip$=" + subselector + "]");
                } else if ($("[name$=_" + selector + "]:not(form)").length > 0) {
                    label = $("[name$=_" + selector + "]:not(form)").closest(".subform-cell").find("label.label");
                } else {
                    label = $("[name=" + subselector + "]:not(form)").closest(".form-cell").find("label.label");
                }
                if (label !== null && label.find("i.tooltipstered").length === 0) {
                    $(label).append(" <i class=\"fieldtooltip fa fas fa-info-circle\"></i>");
                    $(label).find("i").tooltipster({
                        content: $("<div>" + tooltip + "</div>"),
                        side: 'right',
                        interactive: true
                    });
                }
            });
        }
    }
}

//filter parents
jQuery.expr[':'].parents = function(a,i,m){
    return jQuery(a).parents(m[3]).length < 1;
};