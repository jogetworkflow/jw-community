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
        
        return $(field).filter("input[type=hidden]:not([disabled=true]), :enabled, [disabled=false]");
    }
}