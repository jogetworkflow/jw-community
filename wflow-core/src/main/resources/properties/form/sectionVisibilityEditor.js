{
    addOnValidation: function(data, errors, checkEncryption) {
        var wrapper = $('#' + this.id + '_input');
        
        var hasError = false;
        $(wrapper).find(".error").removeClass("error");
        $(wrapper).find(".required").each(function(){
            if ($(this).val() === "") {
                $(this).addClass("error");
                hasError = true;
            }
        });
        if (hasError) {
            var obj = new Object();
            obj.field = this.properties.name;
            obj.fieldName = this.properties.label;
            obj.message = this.options.mandatoryMessage;
            errors.push(obj);
            $(wrapper).append('<div class="property-input-error">' + obj.message + '</div>');
        }
    },
    getData: function(useDefault) {
        var field = this;
        var data = new Object();

        if (this.isDataReady) {
            var joins = [];
            var reverses = [];
            var visibilityControls = [];
            var visibilityValues = [];
            var regexs = [];
            
            var recursiveGetData = function(container, isGroup) {
                if (isGroup) { //open parentheses
                    joins.push($(container).find('> .buttons > .andOr').val());
                    reverses.push(($(container).find('> .buttons > .revert').hasClass("checked"))?"true":"");
                    visibilityControls.push("(");
                    visibilityValues.push("");
                    regexs.push("");
                }
                
                $(container).find("> .conditions-container > .perow").each(function(i, rowContainer){
                    if ($(rowContainer).hasClass("group")) {
                        recursiveGetData(rowContainer, true);
                    } else {
                        joins.push($(rowContainer).find('.andOr').val());
                        reverses.push(($(rowContainer).find('.revert').hasClass("checked"))?"true":"");
                        visibilityControls.push($(rowContainer).find('input.condition').val());
                        visibilityValues.push($(rowContainer).find('input.visibility_value').val().replaceAll(";", "__"));
                        regexs.push($(rowContainer).find('select.operation').val());
                    }
                });
                
                if (isGroup) { //close parentheses
                    joins.push("");
                    reverses.push("");
                    visibilityControls.push(")");
                    visibilityValues.push("");
                    regexs.push("");
                }
            };
            
            recursiveGetData($("#" + field.id + "_input .visibilitywrapper"), false);
            
            data['join'] = joins.join(";");
            data['reverse'] = reverses.join(";");
            data['visibilityControl'] = visibilityControls.join(";");
            data['visibilityValue'] = visibilityValues.join(";");
            data['regex'] = regexs.join(";");
        }
        return data;
    },
    renderField : function() {
        var thisObj = this;
        thisObj.isDataReady = false;
        
        var css = '.visibilitywrapper .pewrapper > .buttons > .andOr {float: left; margin-left: 5px; margin-top: 0 !important; width:auto !important;}';
        css += '.visibilitywrapper > .conditions-container {padding-bottom:15px;}';
        css += '.visibilitywrapper .conditions-container {padding-left:15px; padding-top:5px; overflow:hidden; min-height:20px;}';
        css += '.visibilitywrapper .buttons:after{content:""; display:block; clear:both;}';
        css += '.visibilitywrapper .buttons .sort{cursor:move;}';
        css += '.visibilitywrapper table{width:100%;}';
        css += '.visibilitywrapper .rulerow.pewrapper{background:#edf2f5f2; border-color:#9aafbb; padding:2px 2px 2px 5px;}';
        css += '.visibilitywrapper .revert{width: 15px; color:#ccc; cursor:pointer;}';
        css += '.visibilitywrapper .revert.checked{width: 15px; color:red;}';
        css += '.visibilitywrapper a.revert{position: absolute; left: -3px; top: 11px;}';
        css += '.visibilitywrapper .col_input.small {flex-grow: inherit; min-width: 70px; width: 70px !important; max-width: inherit;}';
        css += '.visibilitywrapper .col_input span.label {transform: none; bottom: 33px; font-size:80%;}';
        css += '.visibilitywrapper .col_inputs{display: flex;flex-wrap: wrap;}';
        css += '.visibilitywrapper .col_input{flex-basis: 0;flex-grow: 1;max-width: 100%;position: relative;width: 100%;min-width:100px;}';
        css += '.visibilitywrapper .col_input input, .visibilitywrapper .col_input select{width: 98%;}';
        css += '.visibilitywrapper .conditions-container > .rulerow:first-child > .buttons > .andOr, .visibilitywrapper .conditions-container > .rulerow:first-child > table .andOr {opacity:0.3; pointer-events:none;}';
        css += '.visibilitywrapper .alignright{width: 27px; text-align:left;}';
        css += '.visibilitywrapper .perow.condition .buttons{position:absolute; top:50%; right:5px; transform: translateY(-50%); z-index:5;}';
        
        var html = '<div name="'+thisObj.id+'" class="visibilitywrapper pewrapper"><div class="buttons"><a class="addcondition"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addCondition@@</a>&nbsp;&nbsp;<a class="addgroup"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addGroup@@</a></div><div class="conditions-container"></div></div>';
        
        return '<style>'+ css + '</style>' + html;
    },
    initScripting : function() {
        var thisObj = this;
        
        thisObj.loadValues();
        
        $("#" + thisObj.id + "_input").on("click", ".addcondition", function(){
            thisObj.addCondition($(this).closest('.pewrapper'));
        });
        
        $("#" + thisObj.id + "_input").on("click", ".addgroup", function(){
            thisObj.addGroup($(this).closest('.pewrapper'));
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deletecondition", function(){
            thisObj.deleteCondition(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deletegroup", function(){
            thisObj.deleteGroup(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".revert", function(){
            $(this).toggleClass("checked");
        });
        
        thisObj.sortable($("#" + thisObj.id + "_input .visibilitywrapper > .conditions-container"));
    },
    loadValues : function() {
        var thisObj = this;
        
        var values = new Array();
        if (thisObj.options.propertyValues !== undefined && thisObj.options.propertyValues !== null && thisObj.options.propertyValues["visibilityControl"] !== undefined) {
            var joins = thisObj.options.propertyValues["join"].split(";");
            var reverses = thisObj.options.propertyValues["reverse"].split(";");
            var visibilityControls = thisObj.options.propertyValues["visibilityControl"].split(";");
            var visibilityValues = thisObj.options.propertyValues["visibilityValue"].split(";");
            var regexs = thisObj.options.propertyValues["regex"].split(";");
            
            for (var i in visibilityControls) {
                values.push({
                    join : (joins.length > i?joins[i]:""),
                    reverse : (reverses.length > i?reverses[i]:""),
                    visibilityControl : visibilityControls[i],
                    visibilityValue : (visibilityValues.length > i?visibilityValues[i]:""),
                    regex : (regexs.length > i?regexs[i]:"")
                });
            }
        }
        
        //render value
        if (values.length > 0) {
            var container = $("#" + thisObj.id + "_input > .pewrapper");
            $.each(values, function(i, row) {
                if (row.visibilityControl === "(") {
                    //add group
                    container = thisObj.loadGroup(container, row);
                } else if (row.visibilityControl === ")") {
                    //back to parent group
                    container = $(container).parent().closest(".pewrapper");
                } else {
                    //add condition
                    thisObj.loadCondition(container, row);
                }
            });
        }
        
        thisObj.isDataReady = true;
    },
    loadCondition : function(container, data) {
        var thisObj = this;
        var rowContainer = thisObj.addCondition(container);
        
        if (data["reverse"] === "true") {
            $(rowContainer).find(".revert").addClass("checked");
        }
        if (data["join"] === "or") {
            $(rowContainer).find(".andOr").val(data["join"]);
        }
        $(rowContainer).find("input.condition").val(data["visibilityControl"]);
        $(rowContainer).find("select.operation").val(data["regex"]);
        $(rowContainer).find("input.visibility_value").val(data["visibilityValue"].replaceAll("__", ";"));
        
        return rowContainer;
    },
    addCondition : function(container) {
        var thisObj = this;
        var container = $(container).find("> .conditions-container");
        var rowContainer = $('<div class="rulerow perow condition"><div class="buttons"><a class="sort" title="@@app.rulesdecision.sort@@"><i class="fas fa-ellipsis-v"></i><i class="fas fa-ellipsis-v"></i></a></div><table><tr><td class="revert"><i class="fas fa-exclamation" title="@@app.rulesdecision.revert@@"></i></td><td><div class="col_inputs"><div class="col_input small"><select class="andOr"><option value="">@@app.rulesdecision.and@@</option><option value="or">@@app.rulesdecision.or@@</option></select></div><div class="col_input"><input class="condition autocomplete required" placeholder="@@form.section.fieldIdControl@@"/><span class="label">@@form.section.fieldIdControl@@</span></div><div class="col_input"><select class="operation"><option value="">@@pbuilder.label.equalTo@@</option><option value=">">@@pbuilder.label.greaterThan@@</option><option value=">=">@@pbuilder.label.greaterThanOrEqualTo@@</option><option value="<">@@pbuilder.label.lessThan@@</option><option value="<=">@@pbuilder.label.lessThanOrEqualTo@@</option><option value="isTrue">@@pbuilder.label.isTrue@@</option><option value="isFalse">@@pbuilder.label.isFalse@@</option><option value="contains">@@app.rulesdecision.contains@@</option><option value="listContains">@@app.rulesdecision.listContains@@</option><option value="in">@@app.rulesdecision.in@@</option><option value="true">@@app.rulesdecision.regex@@</option></select><span class="label">@@app.rulesdecision.operation@@</span></div><div class="col_input"><input class="visibility_value" placeholder="@@form.section.fieldValueControl@@"/><span class="label">@@form.section.fieldValueControl@@</span></div></div></td><td class="alignright"><a class="deletecondition" title="@@app.rulesdecision.deleteCondition@@"><i class="fas fa-trash-alt"></i></a></td></tr></table></div>');
        container.append(rowContainer);
        
        $(rowContainer).find(".autocomplete").autocomplete({
            source:function (request, response) {
                var sources = [];
                var options = window['FormBuilder']['getFieldOptions']();
                for (var i in options) {
                    sources.push(options[i].value);
                }
                response(sources);
            },
            minLength: 0,
            open: function() {
                $(this).autocomplete('widget').css('z-index', 99999);
                return false;
            }
        });
        return rowContainer;
    },
    deleteCondition : function(button) {
        var thisObj = this;
        var container = $(button).closest(".rulerow").remove();
    },
    loadGroup : function(container, data) {
        var thisObj = this;
        var groupContainer = thisObj.addGroup(container);
        
        if (data["reverse"] === "true") {
            $(groupContainer).find("> .buttons > .revert").addClass("checked");
        }
        if (data["join"] === "or") {
            $(container).find("> .buttons > .andOr").val("or");
        }
        $(groupContainer).find("input.condition").val(data["visibilityControl"]);
        
        return groupContainer;
    },
    addGroup : function(container) {
        var thisObj = this;
        var container = $(container).find("> .conditions-container");
        var group = $('<div class="rulerow perow rulewrapper pewrapper group"><div class="buttons"><a class="revert"><i class="fas fa-exclamation" title="@@app.rulesdecision.revert@@"></i></a>&nbsp;&nbsp;<select class="andOr"><option value="">@@app.rulesdecision.and@@</option><option value="or">@@app.rulesdecision.or@@</option></select> <a class="addcondition"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addCondition@@</a>&nbsp;&nbsp;<a class="addgroup"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addGroup@@</a>&nbsp;&nbsp;<a class="deletegroup" title="@@app.rulesdecision.deleteGroup@@"><i class="fas fa-trash-alt"></i></a>&nbsp;&nbsp;<a class="sort" title="@@app.rulesdecision.sort@@"><i class="fas fa-ellipsis-v"></i><i class="fas fa-ellipsis-v"></i></a></div><div class="conditions-container"></div></div>');
        $(container).append(group);
        
        thisObj.sortable($(group).find("> .conditions-container"));
        
        return group;
    },
    deleteGroup : function(button) {
        var thisObj = this;
        var container = $(button).closest(".rulewrapper.group").remove();
    },
    sortable : function(sortableContainer) {
        $(sortableContainer).sortable({
            opacity: 0.8,
            items: '> .perow',
            axis: 'y',
            handle: '.sort',
            tolerance: 'intersect',
            connectWith: '.conditions-container'
        });
    }
}
