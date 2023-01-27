{
    forms : %s,
    inputs : %s,
    posts : %s,
    existingWorkflowVariable : null,
    addOnValidation: function(data, errors, checkEncryption) {
        var wrapper = $('#' + this.id + '_input');
        
        var value = data[this.properties.name];
        var defaultValue = null;
        
        if (this.defaultValue !== undefined && this.defaultValue !== null && this.defaultValue !== "") {
            defaultValue = this.defaultValue;
        }

        var hasValue = true;
        var hasError = false;
        
        if (value === null || value === undefined || value['sessions'] === undefined || 
                value['sessions'] === null || (value['sessions'] !== undefined && value["sessions"].length === 0)) {
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
            
            var fields = $(wrapper).find("input.error, select.error");
            if ($(fields).length > 0) {
                $(fields).first().focus();
            }
        }
    },
    getData: function(useDefault) {
        var field = this;
        var data = new Object();

        if (this.isDataReady) {
            var tf = {};
            if (!field.isHidden()) {
                var sessions = [];
                var postProcessing = [];
                
                $("#" + field.id + "_input .tfio-ioset").each(function(){
                    var session = field.getSet($(this));
                    sessions.push(session);
                });
                if (sessions.length > 0) {
                    tf["sessions"] = sessions;
                }
                
                $("#" + field.id + "_input .postrows-container .tfio-row").each(function(){
                    var post = field.getPost($(this));
                    postProcessing.push(post);
                });
                if (postProcessing.length > 0) {
                    tf["postProcessing"] = postProcessing;
                }
                
                data[this.properties.name] = tf;
            }
        } else {
            data[this.properties.name] = this.value;
        }
        return data;
    },
    getSet : function(container) {
        var thisObj = this;
        
        var session = {
            "model" : $(container).find(".input_model").val(),
            "inputs" : [],
            "outputs" : []
        };
        
        $(container).find(".inputrows-container .tfio-row").each(function(){
            session["inputs"].push(thisObj.getInput($(this)));
        });
        
        $(container).find(".outputrows-container .tfio-row").each(function(){
            session["outputs"].push(thisObj.getOutput($(this)));
        });
        
        return session;
    },
    getValues : function(container) {
        var thisObj = this;
        var values = {};
        
        $(container).find("[name]").each(function(){
            var name = $(this).attr("name");
            if ($(this).is("[type='checkbox']") || $(this).is("[type='radio']")) {
                if ($(this).hasClass("truefalse")) {
                    values[name] = $(this).is(":checked");
                } else {
                    if (values[name] === undefined) {
                        var temp = "";
                        $(container).find("[name='"+name+"']:checked").each(function(){
                            if (temp !== "") {
                                temp += ";";
                            }
                            temp += $(this).val();
                        });
                    }
                }
            } else {
                values[name] = $(this).val();
            }
        });
        return values;
    },
    getInput : function(container) {
        var thisObj = this;
        var input = thisObj.getValues(container);
        input["type"] = $(container).data("type");
        
        return input;
    },
    getOutput : function(container) {
        var thisObj = this;
        return thisObj.getValues(container);
    },
    getPost : function(container) {
        var thisObj = this;
        var post = thisObj.getValues(container);
        post["type"] = $(container).data("type");
        
        return post;
    },
    renderField : function() {
        var thisObj = this;
        
        var css = '.tfio-ioset {margin-bottom: 5px;}';
        css += '.tfiowrapper a.deleteinput, .tfiowrapper a.deletepost {position: absolute; right: 5px; top: 5px;}';
        css += '.tfio-row .input-fields > div {margin-top:5px;}';
        css += '.tfio-row .input-fields label {display:inline-block; padding: 7px 0;}';
        
        var html = '<div name="'+thisObj.id+'">';
        html += '<div class="tfio-container"></div><a class="pebutton tfbutton addset"><i class="fas fa-plus-circle"></i> @@app.simpletfai.addSession@@</a>';
        html += '<div class="tfio-post tfiowrapper pewrapper"><div class="tfio_header peheader">@@app.simpletfai.postprocessin@@</div><div class="posts-container">';
        html += '<div class="buttons"><a class="addpost"><i class="fas fa-plus-circle"></i> @@app.simpletfai.addPostProcessing@@<ul>';
        
        $.each(thisObj.posts, function(key, post){
            html += '<li data-type="'+key+'">'+post["label"]+'</li>';
        });
        
        html += '</ul></a></div><div class="postrows-container"></div></div></div></div>';
        
        return '<style>'+ css + '</style>' + html;
    },
    initScripting : function() {
        var thisObj = this;
        
        thisObj.properties.appPath = thisObj.options.appPath;
        
        thisObj.loadValues();
        
        if ($("#" + thisObj.id + "_input").find(".tfio-container .tfio-ioset").length === 0) {
            thisObj.addSet();
        }
        
        $("#" + thisObj.id + "_input").find(".addset").on("click", function(){
            thisObj.addSet();
        });
        
        $("#" + thisObj.id + "_input").on("click", ".addinput ul li", function(){
            thisObj.addInput($(this).closest(".addinput"), $(this).data("type"));
        });
        
        $("#" + thisObj.id + "_input").on("click", ".addoutput", function(){
            thisObj.addOutput(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".addpost ul li", function(){
            thisObj.addPost($(this).closest(".addpost"), $(this).data("type"));
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deleteset", function(){
            thisObj.deleteSet(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deleteinput, .deleteoutput, .deletepost", function(){
            thisObj.deleteRow(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".choosefile", function(){
            $("#" + thisObj.id + "_input .choosefile").removeClass("current");
            $(this).addClass("current");
            $(this).parent().find("input").trigger("focus");
            PropertyEditor.Util.showAppResourcesDialog(thisObj);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".clearfile", function(){
            $(this).parent().find("input").val("");
        });
        
        $("#" + thisObj.id + "_input").on("change", ".output_variable, .post_name", function(){
            if (thisObj.existingWorkflowVariable === null) {
                thisObj.existingWorkflowVariable = thisObj.editorObject.fields["rules"].getWorkflowVariables;
            }
            
            thisObj.updateRuleEditor();
        });
        $("#" + thisObj.id + "_input").find(".output_variable, .post_name").trigger("change");
        
        $("#" + thisObj.id + "_input").on("change", ".output_variable", function(){
            thisObj.updatePostVariables();
        });
    },
    updateRuleEditor: function() {
        var thisObj = this;
        var variables = $.merge([], thisObj.existingWorkflowVariable);
        
        $("#" + thisObj.id + "_input").find(".output_variable, .post_name").each(function(){
            if ($(this).val() !== "") {
                variables.push($(this).val());
            }
        });
        
        thisObj.editorObject.fields["rules"].getWorkflowVariables = variables;
    },
    updatePostVariables: function() {
        var thisObj = this;
        var html = '';
        
        $("#" + thisObj.id + "_input").find(".output_variable").each(function(){
            if ($(this).val() !== "") {
                html += '<option value="'+$(this).val()+'">'+$(this).val()+'</option>';
            }
        });
        
        $("#" + thisObj.id + "_input").find(".post_variable, .input_variable").each(function(){
            var value = $(this).val();
            $(this).find("option:not(':first')").remove();
            $(this).append(html);
            $(this).val(value);
        });
    },
    loadValues : function() {
        var thisObj = this;
        if (thisObj.value !== undefined && thisObj.value !== null) {
            if (thisObj.value["sessions"] !== undefined) {
                $.each(thisObj.value["sessions"], function(i, v){
                    thisObj.loadSet(v);
                });
            }
            
            if (thisObj.value["postProcessing"] !== undefined) {
                var container = $("#" + thisObj.id + "_input");
                $.each(thisObj.value["postProcessing"], function(i, v){
                    thisObj.loadPost(v, container);
                });
            }
        }
    },
    loadSet : function(session) {
        var thisObj = this;
        thisObj.addSet();
        var ioset = $("#" + thisObj.id + "_input .tfio-ioset:last");
        $(ioset).find(".input_model").val(session["model"]);
        
        $.each(session["inputs"], function(i, v){
            thisObj.loadInput(v, $(ioset));
        });
        
        $.each(session["outputs"], function(i, v){
            thisObj.loadOutput(v, $(ioset));
        });
    },
    loadFieldValues : function(values, container) {
        for(var propt in values){
            if ($(container).find("[name='"+propt+"']").length > 0) {
                if ($(container).find("[name='"+propt+"']").is("[type='checkbox']") || $(container).find("[name='"+propt+"']").is("[type='radio']")) {
                    if ($(container).find("[name='"+propt+"']").hasClass("truefalse")) {
                        if (values[propt]) {
                            $(container).find("[name='"+propt+"']").prop("checked", true);
                        }
                    } else {
                        
                    }
                } else {
                    $(container).find("[name='"+propt+"']").val(values[propt]);
                }
            }
        }
    },
    loadInput : function(input, container) {
        var thisObj = this;
        
        if (thisObj.inputs[input["type"]] === undefined) {
            return;
        }
        
        thisObj.addInput($(container).find(".addinput"), input["type"]);
        var row = $(container).find(".inputrows-container .tfio-row:last");
        
        thisObj.loadFieldValues(input, row);
    },
    loadOutput : function(output, container) {
        var thisObj = this;
        thisObj.addOutput($(container).find(".addoutput"));
        var row = $(container).find(".outputrows-container .tfio-row:last");
        
        thisObj.loadFieldValues(output, row);
    },
    loadPost : function(post, container) {
        var thisObj = this;
        
        if (thisObj.posts[post["type"]] === undefined) {
            return;
        }
        
        thisObj.addPost($(container).find(".addpost"), post["type"]);
        var row = $(container).find(".postrows-container .tfio-row:last");
        
        thisObj.loadFieldValues(post, row);
    },
    addSet : function() {
        var thisObj = this;
        
        var ioset = $('<div class="tfio-ioset tfiowrapper pewrapper"><div class="tfio_header peheader">@@app.simpletfai.model@@ <span class="buttons"><a class="deleteset" title="@@app.simpletfai.deleteSet@@"><i class="fas fa-trash-alt"></i></a></span></div><div class="model-container"></div><div class="tfio_header peheader">@@app.simpletfai.inputs@@</div><div class="inputs-container"></div><div class="tfio_header peheader">@@app.simpletfai.outputs@@</div><div class="outputs-container"></div></div>');
        thisObj.initModel($(ioset));
        thisObj.initInputs($(ioset));
        thisObj.initOutputs($(ioset));
        $("#" + thisObj.id + "_input").find("> div > .tfio-container").append(ioset);
    },
    initModel : function(ioset) {
        var thisObj = this;
        $(ioset).find(".model-container").append('<input class="input_model half required" placeholder="@@app.simpletfai.model@@"/> <a class="choosefile btn button small">@@peditor.chooseFile@@</a> <a class="clearfile btn button small">@@peditor.clear@@</a></div>');
    },
    initInputs : function(ioset) {
        var thisObj = this;
        var html = '<div class="buttons"><a class="addinput"><i class="fas fa-plus-circle"></i> @@app.simpletfai.addInput@@<ul>';
        
        $.each(thisObj.inputs, function(key, input){
            html += '<li data-type="'+key+'">'+input["label"]+'</li>';
        });
        
        html += '</ul></a></div><div class="inputrows-container"></div>';
        
        $(ioset).find(".inputs-container").append(html);
    },
    initOutputs : function(ioset) {
        var thisObj = this;
        $(ioset).find(".outputs-container").append('<div class="buttons"><a class="addoutput"><i class="fas fa-plus-circle"></i> @@app.simpletfai.addOutput@@</a></div><div class="outputrows-container"></div>');
    },
    addInput : function(button, type) {
        var thisObj = this;
        var container = $(button).parent().parent().find("> .inputrows-container");
        var row = $('<div class="perow tfio-row input" data-type="'+type+'"><label class="row-title"></label><a class="deleteinput" title="@@app.simpletfai.deleteinput@@"><i class="fas fa-trash-alt"></i></a><div class="input-fields"></div></div>');
        
        $(row).find(".row-title").text(thisObj.inputs[type].label);
        $(row).find(".input-fields").append('<input name="name" class="input_name half required" placeholder="@@app.simpletfai.inputname@@"/><span class="label">@@app.simpletfai.inputname@@</span>');
        $(row).find(".input-fields").append(thisObj.inputs[type].ui);
        
        if ($(row).find(".input_datatype").length > 0) {
            $(row).find(".input_datatype").html('<option value="Float">@@app.simpletfai.float@@</option><option value="Double">@@app.simpletfai.double@@</option><option value="Integer">@@app.simpletfai.integer@@</option><option value="UInt8">@@app.simpletfai.uint8@@</option><option value="Long">@@app.simpletfai.long@@</option>');
        }
        
        if (thisObj.inputs[type].initScript !== undefined && thisObj.inputs[type].initScript !== "") {
            thisObj.execScript(thisObj.inputs[type].initScript, $(row));
        }
        
        $(container).append(row);
    },
    addOutput : function(button) {
        var thisObj = this;
        var container = $(button).parent().parent().find("> .outputrows-container");
        var row = $('<div class="perow tfio-row output"><table><tr><td><input name="name" class="output_name required" placeholder="@@app.simpletfai.outputname@@"/><span class="label">@@app.simpletfai.outputname@@</span></td><td><input name="variable" class="output_variable required" placeholder="@@app.simpletfai.variableName@@"/><span class="label">@@app.simpletfai.variableName@@</span></td><td class="alignright"><a class="deleteoutput" title="@@app.simpletfai.deleteOutput@@"><i class="fas fa-trash-alt"></i></a></td></tr></table></div>');
        
        $(container).append(row);
    },
    addPost : function(button, type) {
        var thisObj = this;
        var container = $(button).parent().parent().find("> .postrows-container");
        var row = $('<div class="perow tfio-row post" data-type="'+type+'"><label class="row-title"></label><a class="deletepost" title="@@app.simpletfai.deletepost@@"><i class="fas fa-trash-alt"></i></a><div class="input-fields"></div></div>');
        
        
        $(row).find(".row-title").text(thisObj.posts[type].label);
        
        if (thisObj.posts[type].description !== undefined && thisObj.posts[type].description !== "") {
            var toolTipId = "tooltip-" + (new Date()).getTime();
            $(row).find(".row-title").append(' <i class="fas fa-question-circle" style="cursor:pointer;"></i>');
            
            $(row).find(".row-title .fa-question-circle").tooltipster({
                content : thisObj.posts[type].description,
                contentAsHTML: true,
                side : 'right',
                interactive : true
            });
        }
        
        $(row).find(".input-fields").append('<input name="name" class="post_name half required" placeholder="@@app.simpletfai.postname@@"/><span class="label">@@app.simpletfai.postname@@</span>');
        $(row).find(".input-fields").append(thisObj.posts[type].ui);
        
        if (thisObj.posts[type].initScript !== undefined && thisObj.posts[type].initScript !== "") {
            thisObj.execScript(thisObj.posts[type].initScript, $(row));
        }
        
        $(container).append(row);
        thisObj.updatePostVariables();
    },
    execScript : function(script, row) {
        var editor = this;
        try {
            eval(script);
        } catch (err) {}
    },
    deleteSet : function(button) {
        var thisObj = this;
        var container = $(button).closest(".tfiowrapper.tfio-ioset").remove();
    },
    deleteRow : function(button) {
        var thisObj = this;
        var container = $(button).closest(".tfio-row").remove();
    },
    selectResource: function(filename) {
        var thisObj = this;
        if (thisObj.properties.appResourcePrefix !== undefined && thisObj.properties.appResourcePrefix !== null && thisObj.properties.appResourcePrefix === "true") {
            filename = "#appResource." + filename + "#";
        }
        $("#" + thisObj.id + "_input .choosefile.current").parent().find("input").val(filename);
    }
}

