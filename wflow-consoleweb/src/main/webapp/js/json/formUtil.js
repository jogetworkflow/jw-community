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
        return values;
    },
    
    getField : function(fieldId){
        var field = $("[name="+fieldId+"]");
        if ($(field).length == 0) {
            field = $("[name$=_"+fieldId+"]");
        }
        
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
        
        field = $(field).filter("input[type=hidden]:not([disabled=true]), :enabled, [disabled=false]");
        
        return field;
    },
    
    getGridCells : function(cellFieldId){
        var fieldId = cellFieldId.split(".")[0];
        
        var field = $("[name="+fieldId+"]");
        if ($(field).length === 0) {
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
        
        cellFieldId = cellFieldId.replace(/\./g, '_');
        var cells = $(field).find("[name=" + cellFieldId + "], [name$=_" + cellFieldId + "]");
        
        return cells;
    }
}

//filter parents
jQuery.expr[':'].parents = function(a,i,m){
    return jQuery(a).parents(m[3]).length < 1;
};