{
    getWorkflowVariables : ProcessBuilder.getWorkflowVariables(),
    getTransitions : ProcessBuilder.getCurrentActivityOutgoingTransition(),
    variablehtml : "",
    transitionshtml : "",
    addOnValidation: function(data, errors, checkEncryption) {
        var wrapper = $('#' + this.id + '_input');
        
        var value = data[this.properties.name];
        var defaultValue = null;
        
        if (this.defaultValue !== undefined && this.defaultValue !== null && this.defaultValue !== "") {
            defaultValue = this.defaultValue;
        }

        var hasValue = true;
        var hasError = false;
        
        if (value['ifrules'] === undefined && value['else'] !== undefined && value["else"].length === 0) {
            hasValue = false;
        }
        
        $(wrapper).find(".error").removeClass("error");
        $(wrapper).find(".required").each(function(){
            if ($(this).val() === "") {
                $(this).addClass("error");
                hasError = true;
            }
        });
        $(wrapper).find(".inputs-container, .outputs-container").each(function(){
            if ($(this).find(".tfio-row").length === 0) {
                $(this).addClass("error");
                hasError = true;
            }
        });

        if (hasError || (this.properties.required !== undefined &&
            this.properties.required.toLowerCase() === "true" &&
            defaultValue === null && !hasValue)) {
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
            var rules = {};
            if (!field.isHidden()) {
                var ifrules = [];
                $("#" + field.id + "_input .ifrules-container .if-rule").each(function(){
                    var rule = field.getGroup($(this));
                    rule["actions"] = field.getActions($(this));
                    ifrules.push(rule);
                });
                
                if (ifrules.length > 0) {
                    rules["ifrules"] = ifrules;
                }
                
                rules["else"] = field.getActions($("#" + field.id + "_input .else-rule"));
                
                if (rules.ifrules === undefined && useDefault !== undefined && useDefault &&
                    this.defaultValue !== null && this.defaultValue !== undefined) {
                    rules = this.defaultValue;
                }
                data[this.properties.name] = rules;
            }
        } else {
            data[this.properties.name] = this.value;
        }
        return data;
    },
    getCondition : function(container) {
        var condition = {
            "revert" : $(container).find(".revert").hasClass("checked"),
            "variable" : $(container).find("input.condition").val(),
            "operation" : $(container).find("select.operation").val(),
            "value" : $(container).find("input.condition_value").val()
        };
        return condition;
    },
    getGroup : function(container) {
        var thisObj = this;
        
        var group = {
            "revert" : $(container).find("> .rules-container > .buttons > .revert").hasClass("checked"),
            "andOr" : $(container).find("> .rules-container > .buttons > .andOr").val(),
            "conditions" : []
        };
        
        $(container).find("> .rules-container > .conditions-container > .rulerow").each(function(){
            if ($(this).hasClass("condition")) {
                group["conditions"].push(thisObj.getCondition($(this)));
            } else {
                group["conditions"].push(thisObj.getGroup($(this)));
            }
        });
        return group;
    },
    getActions : function(container) {
        var thisObj = this;
        var actions = [];
        
        $(container).find(".actions-container .rulerow").each(function(){
            actions.push(thisObj.getAction($(this)));
        });
        
        return actions;
    },
    getAction : function(container) {
        var action = {
            "type" : $(container).find("select.type").val(),
            "name" : (($(container).find("select.type").val() === "transition")?($(container).find("select.transition_name").val()):($(container).find("select.variable_name").val())),
            "value" : $(container).find("input.variable_value").val()
        };
        
        return action;
    },
    renderField : function() {
        var thisObj = this;
        
        var css = '.rulewrapper .rules-container{position:relative; padding-left: 8px;}';
        css += '.rulewrapper .rules-container .buttons {margin-bottom: 6px;}';
        css += '.rulewrapper .andOr {float: left; margin-left: 5px; margin-top: 0 !important; width:auto !important;}';
        css += '.rulewrapper .conditions-container {padding-left:15px; padding-top:5px; overflow:hidden;}';
        css += '.rulewrapper .buttons:after{content:""; display:block; clear:both;}';
        css += '.rulewrapper .buttons .sort{cursor:move;}';
        css += '.conditions-container .rulerow:after {content:""; border-top:2px solid #9aafbb; position:absolute; left: -6px; top: 50%; width: 5px; z-index:999;}';
        css += '.conditions-container .rulerow:last-child:before{content:""; border-left:2px solid #9aafbb; position:absolute; left: -6px; bottom:50%; height:1000px; z-index:999;}';
        css += '.rulerow.rulewrapper{background:#edf2f5f2; border-color:#9aafbb; padding:2px 2px 2px 5px;}';
        css += '.rulewrapper .revert{width: 15px; color:#ccc; cursor:pointer;}';
        css += '.rulewrapper .revert.checked{width: 15px; color:red;}';
        css += '.rulewrapper a.revert{position: absolute; left: -3px; top: 11px;}';
        css += '.if-rule > .rules-container {padding-left: 20px;}';
        css += '.if-rule > .rules-container > .buttons > a.revert {top: 12px; left: 9px;}';
        css += '.pewrapper .col_input span.label {transform: none; bottom: 33px; font-size:80%;}';
        css += '.col_inputs{display: flex;flex-wrap: wrap;}';
        css += '.col_input{flex-basis: 0;flex-grow: 1;max-width: 100%;position: relative;width: 100%;min-width:100px;}';
        css += '.col_input input, .col_input select{width: 98%;}';
        
        var html = '<div name="'+thisObj.id+'"><div class="ifrules-container"></div><a class="rbutton pebutton addif"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addRule@@</a><div class="else-rule rulewrapper pewrapper"><div class="rule_header peheader">@@app.rulesdecision.elsethen@@</div><div class="then-container"></div></div></div>';
        
        return '<style>'+ css + '</style>' + html;
    },
    initScripting : function() {
        var thisObj = this;
        
        if (thisObj.variablehtml === "") {
            $.each(thisObj.getWorkflowVariables, function(i, v){
                thisObj.variablehtml += '<option value="'+v+'">'+v+'</option>';
            });
        }
        if (thisObj.transitionshtml === "") {
            $.each(thisObj.getTransitions, function(i, v){
                thisObj.transitionshtml += '<option value="'+v.value+'">'+v.label+'</option>';
            });
        }
        
        thisObj.initThen($("#" + thisObj.id + "_input").find(".else-rule"));
        
        thisObj.loadValues();
        
        $("#" + thisObj.id + "_input").find(".addif").on("click", function(){
            thisObj.addRule();
        });
        
        $("#" + thisObj.id + "_input").on("click", ".addcondition", function(){
            thisObj.addCondition(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".addgroup", function(){
            thisObj.addGroup(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".addaction", function(){
            thisObj.addAction(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deleterule", function(){
            thisObj.deleteRule(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deletecondition", function(){
            thisObj.deleteCondition(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deleteaction", function(){
            thisObj.deleteAction(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deletegroup", function(){
            thisObj.deleteGroup(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".revert", function(){
            $(this).toggleClass("checked");
        });
        
        $("#" + thisObj.id + "_input").on("change", ".action .type", function(){
            var container = $(this).closest(".action");
            if ($(this).val() === "transition") {
                $(container).find(".transition_name").show();
                $(container).find(".variable_name").hide();
                $(container).find(".variable_value").css("visibility", "hidden");
            } else {
                $(container).find(".variable_name").show();
                $(container).find(".transition_name").hide();
                $(container).find(".variable_value").css("visibility", "visible");
            }
        });
        
        $("#" + thisObj.id + "_input .ifrules-container").sortable({
            opacity: 0.8,
            axis: 'y',
            handle: '.sort',
            tolerance: 'intersect'
        });
    },
    loadValues : function() {
        var thisObj = this;
        
        if (thisObj.value !== undefined && thisObj.value !== null) {
            if (thisObj.value.ifrules !== null && thisObj.value.ifrules !== undefined) {
                $.each(thisObj.value.ifrules, function(i, v){
                    thisObj.loadRule(v);
                });
            }
            thisObj.loadActions($("#" + thisObj.id + "_input .else-rule .actions-container"), thisObj.value.else);
        }
    },
    loadRule : function(rule) {
        var thisObj = this;
        thisObj.addRule();
        var ruleContainer = $("#" + thisObj.id + "_input > div > .ifrules-container").find(".if-rule.rulewrapper:last");
        thisObj.loadGroup(ruleContainer, rule);
        thisObj.loadActions($(ruleContainer).find(".actions-container"), rule.actions);
    },
    loadGroup : function(container, group) {
        var thisObj = this;
        if (group["revert"] === true) {
            $(container).find("> .rules-container > .buttons > .revert").addClass("checked");
        }
        if (group["andOr"] === "or") {
            $(container).find("> .rules-container > .buttons > .andOr").val("or");
        }
        $.each(group["conditions"], function(i, v){
            if (v["conditions"] !== undefined) {
                thisObj.addGroup($(container).find("> .rules-container > .buttons > .addgroup"));
                var group = $(container).find("> .rules-container > .conditions-container > .rulerow:last");
                thisObj.loadGroup(group, v);
            } else {
                thisObj.addCondition($(container).find("> .rules-container > .buttons > .addcondition"));
                var cond = $(container).find("> .rules-container > .conditions-container > .rulerow:last");
                thisObj.loadCondition(cond, v);
            }
        });
    },
    loadCondition : function(container, condition) {
        var thisObj = this;
        if (condition["revert"] === true) {
            $(container).find(".revert").addClass("checked");
        }
        $(container).find("input.condition").val(condition["variable"]);
        $(container).find("select.operation").val(condition["operation"]);
        $(container).find("input.condition_value").val(condition["value"]);
    },
    loadActions : function(container, actions) {
        if (actions !== null && actions !== undefined && actions.length > 0) {
            var thisObj = this;
            $.each(actions, function(i, v){
                thisObj.addAction($(container).parent().find("> .buttons > .addaction"));
                var action = $(container).find(".rulerow:last");
                thisObj.loadAction(action, v);
            });
        }
    },
    loadAction : function(container, action) {
        var thisObj = this;
        
        $(container).find("select.type").val(action["type"]);
        if (action["type"] === "transition") {
            $(container).find("select.transition_name").val(action["name"]);
        } else {
            $(container).find("select.variable_name").val(action["name"]);
            $(container).find(".variable_name").show();
            $(container).find(".transition_name").hide();
            $(container).find(".variable_value").css("visibility", "visible");
        }
        $(container).find("input.variable_value").val(action["value"]);
    },
    addRule : function() {
        var thisObj = this;
        
        var ifrule = $('<div class="if-rule rulewrapper pewrapper"><div class="rule_header peheader">@@app.rulesdecision.if@@ <span class="buttons"><a class="deleterule" title="@@app.rulesdecision.deleteRule@@"><i class="fas fa-trash-alt"></i></a> | <a class="sort" title="@@app.rulesdecision.sort@@"><i class="fas fa-ellipsis-v"></i><i class="fas fa-ellipsis-v"></i></a></span></div><div class="rules-container"></div><div class="rule_header peheader">@@app.rulesdecision.then@@</div><div class="then-container"></div></div>');
        thisObj.initConditions($(ifrule).find("> .rules-container"));
        thisObj.initThen(ifrule);
        $("#" + thisObj.id + "_input").find("> div > .ifrules-container").append(ifrule);
    },
    initThen : function(rule) {
        var thisObj = this;
        
        $(rule).find(".then-container").append('<div class="buttons"><a class="addaction"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addAction@@</a></div><div class="actions-container"></div>');
    },
    initConditions : function(container, isGroup) {
        var thisObj = this;
        var conditions = $('<div class="buttons"><a class="revert"><i class="fas fa-exclamation" title="@@app.rulesdecision.revert@@"></i>&nbsp;&nbsp;</a><select class="andOr"><option value="and">@@app.rulesdecision.and@@</option><option value="or">@@app.rulesdecision.or@@</option></select><a class="addcondition"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addCondition@@</a>&nbsp;&nbsp;<a class="addgroup"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addGroup@@</a></div><div class="conditions-container"></div>');
        $(container).append(conditions);
        if (isGroup !== undefined && isGroup === true) {
            $(container).find(".buttons").append('&nbsp;&nbsp;<a class="deletegroup" title="@@app.rulesdecision.deleteGroup@@"><i class="fas fa-trash-alt"></i></a>');
        }
    },
    addCondition : function(button) {
        var thisObj = this;
        var container = $(button).parent().parent().find("> .conditions-container");
        $(container).append('<div class="rulerow perow condition"><table><tr><td class="revert"><i class="fas fa-exclamation" title="@@app.rulesdecision.revert@@"></i></td><td><div class="col_inputs"><div class="col_input"><input class="condition autocomplete required" placeholder="@@app.rulesdecision.variable@@"/><span class="label">@@app.rulesdecision.variable@@</span></div><div class="col_input"><select class="operation"><option value="==">@@pbuilder.label.equalTo@@</option><option value=">">@@pbuilder.label.greaterThan@@</option><option value=">=">@@pbuilder.label.greaterThanOrEqualTo@@</option><option value="<">@@pbuilder.label.lessThan@@</option><option value="<=">@@pbuilder.label.lessThanOrEqualTo@@</option><option value="true">@@pbuilder.label.isTrue@@</option><option value="false">@@pbuilder.label.isFalse@@</option><option value="contains">@@app.rulesdecision.contains@@</option><option value="listContains">@@app.rulesdecision.listContains@@</option><option value="in">@@app.rulesdecision.in@@</option><option value="regex">@@app.rulesdecision.regex@@</option></select><span class="label">@@app.rulesdecision.operation@@</span></div><div class="col_input"><input class="condition_value" placeholder="@@app.simpletfai.value@@"/><span class="label">@@app.simpletfai.value@@</span></div></div></td><td class="alignright"><a class="deletecondition" title="@@app.rulesdecision.deleteCondition@@"><i class="fas fa-trash-alt"></i></a></td></tr></table></div>');
        
        $(container).find(".autocomplete").autocomplete({
            source:function (request, response) {
                var sources = [];
                $.each(thisObj.getWorkflowVariables, function(i, v){
                    sources.push(v);
                });
                response(sources);
            },
            minLength: 0,
            open: function() {
                $(this).autocomplete('widget').css('z-index', 99999);
                return false;
            }
        });
    },
    addGroup : function(button) {
        var thisObj = this;
        var container = $(button).parent().parent().find("> .conditions-container");
        var group = $('<div class="rulerow perow rulewrapper pewrapper group"><div class="rules-container"></div></div>');
        thisObj.initConditions($(group).find("> .rules-container"), true);
        $(container).append(group);
    },
    addAction : function(button) {
        var thisObj = this;
        var container = $(button).parent().parent().find("> .actions-container");
        var voptions = thisObj.variablehtml;
        var toptions = thisObj.transitionshtml;
        
        var row = $('<div class="rulerow perow action"><table><tr><td><select class="type"><option value="transition">@@app.rulesdecision.transition@@</option><option value="variable">@@app.rulesdecision.workflowVariable@@</option></select><span class="label">@@app.simpletfai.type@@</span></td><td><select class="transition_name">'+toptions+'</select><span class="label">@@app.rulesdecision.transition@@</span><select class="variable_name" style="display:none;">'+voptions+'</select><span class="label">@@app.rulesdecision.workflowVariable@@</span></td><td><input class="variable_value" placeholder="@@app.rulesdecision.value@@" style="visibility:hidden;"/><span class="label">@@app.rulesdecision.value@@</span></td><td class="alignright"><a class="deleteaction" title="@@app.rulesdecision.deleteAction@@"><i class="fas fa-trash-alt"></i></a></td></tr></table></div>');
        
        $(row).find(".variable_name").trigger("change");
        $(row).find(".transition_name").trigger("change");
        
        $(row).find(".variable_value").autocomplete({
            source:function (request, response) {
                var sources = [];
                $.each(thisObj.getWorkflowVariables, function(i, v){
                    sources.push(v);
                });
                response(sources);
            },
            minLength: 0,
            open: function() {
                $(this).autocomplete('widget').css('z-index', 99999);
                return false;
            }
        });
        
        $(container).append(row);
    },
    deleteRule : function(button) {
        var thisObj = this;
        var container = $(button).closest(".rulewrapper.if-rule").remove();
    },
    deleteCondition : function(button) {
        var thisObj = this;
        var container = $(button).closest(".rulerow").remove();
    },
    deleteGroup : function(button) {
        var thisObj = this;
        var container = $(button).closest(".rulewrapper.group").remove();
    },
    deleteAction : function(button) {
        var thisObj = this;
        var container = $(button).closest(".rulerow").remove();
    }
}
