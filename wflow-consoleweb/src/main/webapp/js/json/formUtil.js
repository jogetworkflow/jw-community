FormUtil = {
    getValue : function(fieldId){
        var value = "";
        var field = FormUtil.getField(fieldId);
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
    
    getValues : function(fieldId){
        var values = new Array();
        
        if (fieldId.indexOf(".") > 0) { // grid cell values
            values = FormUtil.getGridCellValues(fieldId);
        } else {
            var field = FormUtil.getField(fieldId);
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
    
    getField : function(fieldId){
        var field = $("[name="+fieldId+"]");
        if ($(field).length == 0) {
            field = $("[name$=_"+fieldId+"]");
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
                field = $("[name="+fieldname+"]");
            });
        }
        
        return field;
    },
    
    getGridCells : function(cellFieldId){
        var fieldId = cellFieldId.split(".")[0];
        
        var field = FormUtil.getField(fieldId);
        
        cellFieldId = cellFieldId.replace(/\./g, '_');
        var cells = $(field).find("[name=" + cellFieldId + "], [name$=_" + cellFieldId + "]");
        //filter those in template 
        cells = $(cells).filter(':parents(.grid-row-template)');
        return cells;
    },
    
    getGridCellValues : function (cellFieldId) {
        var fieldId = cellFieldId.split(".")[0];
        
        var field = FormUtil.getField(fieldId);
        
        var values = new Array();
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
                var cell = $(field).find("[name=" + cellId + "], [name$=_" + cellId + "]");
                if (cell.length > 1) {
                    values.push(cell.text());
                }
            }
        });
        
        return values;
    },
    
    getFieldsAsUrlQueryString : function(fields) {
        var queryString = "";
        
        if (fields !== undefined) {
            $.each(fields, function(i, v){
                var values = [];
                
                if (v['field'] !== "") {
                    values = FormUtil.getValues(v['field']).join(";");
                }
            
                if (values.length === 0 && v['defaultValue'] !== "") {
                    values = v['defaultValue'];
                }
                
                queryString += encodeURIComponent(v['param']) + "=" + encodeURIComponent(values) + "&";
            });
            
            if (queryString !== "") {
                queryString = queryString.substring(0, queryString.length-1);
            }
        }
        
        return queryString;
    }
}

//filter parents
jQuery.expr[':'].parents = function(a,i,m){
    return jQuery(a).parents(m[3]).length < 1;
};