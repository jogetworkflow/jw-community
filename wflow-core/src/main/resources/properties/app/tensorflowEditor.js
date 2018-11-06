{
    getForms : [%s],
    fieldOptions : {},
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
    getInput : function(container) {
        var input = {
            "type" : $(container).data("type"),
            "name" : $(container).find(".input_name").val(),
        };
        
        if (input["type"] === "image" || input["type"] === "numbers" || input["type"] === "text") {
            input["datatype"] = $(container).find(".input_datatype").val();
        }
        
        if (input["type"] === "image") {
            input["form"] = $(container).find(".input_form").val();
            input["image"] = $(container).find(".input_image").val();
            input["height"] = $(container).find(".input_height").val();
            input["width"] = $(container).find(".input_width").val();
            input["mean"] = $(container).find(".input_mean").val();
            input["scale"] = $(container).find(".input_scale").val();
        } else if (input["type"] === "text") {
            input["maxlength"] = $(container).find(".input_maxlength").val();
            input["text"] = $(container).find(".input_text").val();
            input["dict"] = $(container).find(".input_dict").val();
            input["fillback"] = $(container).find(".input_fillback").is(":checked");
        } else if (input["type"] === "numbers") {
            input["numbers"] = $(container).find(".input_numbers").val();
        } else if (input["type"] === "boolean") {
            input["boolean"] = $(container).find(".input_boolean").val();
        }
        
        return input;
    },
    getOutput : function(container) {
        var output = {
            "name" : $(container).find(".output_name").val(),
            "variable" : $(container).find(".output_variable").val()
        };
        return output;
    },
    getPost : function(container) {
        var post = {
            "type" : $(container).data("type"),
            "name" : $(container).find(".post_name").val()
        };
        
        if (post["type"] === "labels") {
            post["threshold"] = $(container).find(".post_threshold").val();
            post["labels"] = $(container).find(".post_labels").val();
            post["variable"] = $(container).find(".post_variable").val();
            post["toplabel"] = $(container).find(".post_toplabel").is(":checked");
        } else if (post["type"] === "valuelabel") {
            post["labels"] = $(container).find(".post_labels").val();
            post["unique"] = $(container).find(".post_unique").is(":checked");
            post["variable"] = $(container).find(".post_variable").val();
            post["variable2"] = $(container).find(".post_variable2").val();
        } else if (post["type"] === "euclideanDistance") {
            post["variable"] = $(container).find(".post_variable").val();
            post["variable2"] = $(container).find(".post_variable2").val();
        }
        
        return post;
    },
    renderField : function() {
        var thisObj = this;
        
        var css = '.tfio-ioset {margin-bottom: 5px;}';
        css += '.tfiowrapper a.deleteinput, .tfiowrapper a.deletepost {position: absolute; right: 5px; top: 5px;}';
        css += '.tfio-row .input-fields > div {margin-top:5px;}';
        css += '.tfio-row .input-fields label {display:inline;}';
        
        var html = '<div name="'+thisObj.id+'">';
        html += '<div class="tfio-container"></div><a class="pebutton tfbutton addset"><i class="fas fa-plus-circle"></i> @@app.simpletfai.addSession@@</a>';
        html += '<div class="tfio-post tfiowrapper pewrapper"><div class="tfio_header peheader">@@app.simpletfai.postprocessin@@</div><div class="posts-container">';
        html += '<div class="buttons"><a class="addpost"><i class="fas fa-plus-circle"></i> @@app.simpletfai.addPostProcessing@@<ul>';
        html += '<li data-type="labels">@@app.simpletfai.labels@@</li>';
        html += '<li data-type="valuelabel">@@app.simpletfai.valuelabel@@</li>';
        html += '<li data-type="euclideanDistance">@@app.simpletfai.euclideanDistance@@</li>';
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
        
        $("#" + thisObj.id + "_input").find(".post_variable, .post_variable2, .input_variable").each(function(){
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
    loadInput : function(input, container) {
        var thisObj = this;
        thisObj.addInput($(container).find(".addinput"), input["type"]);
        var row = $(container).find(".inputrows-container .tfio-row:last");
        
        for(var propt in input){
            if ($(row).find(".input_" + propt).length > 0) {
                if ($(row).find(".input_" + propt).is("[type='checkbox']")) {
                    if (input[propt]) {
                        $(row).find(".input_" + propt).attr("checked", "checked");
                    }
                } else {
                    $(row).find(".input_" + propt).val(input[propt]);
                }
            }
        }
    },
    loadOutput : function(output, container) {
        var thisObj = this;
        thisObj.addOutput($(container).find(".addoutput"));
        var row = $(container).find(".outputrows-container .tfio-row:last");
        
        for(var propt in output){
            if ($(row).find(".output_" + propt).length > 0) {
                $(row).find(".output_" + propt).val(output[propt]);
            }
        }
    },
    loadPost : function(post, container) {
        var thisObj = this;
        thisObj.addPost($(container).find(".addpost"), post["type"]);
        var row = $(container).find(".postrows-container .tfio-row:last");
        for(var propt in post){
            if ($(row).find(".post_" + propt).length > 0) {
                if ($(row).find(".post_" + propt).is("[type='checkbox']")) {
                    if (post[propt]) {
                        $(row).find(".post_" + propt).attr("checked", "checked");
                    }
                } else {
                    $(row).find(".post_" + propt).val(post[propt]);
                }
            }
        }
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
        html += '<li data-type="image">@@app.simpletfai.image@@</li>';
        html += '<li data-type="text">@@app.simpletfai.text@@</li>';
        html += '<li data-type="numbers">@@app.simpletfai.numbers@@</li>';
        html += '<li data-type="boolean">@@app.simpletfai.boolean@@</li>';
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
        
        $(row).find(".input-fields").append('<input class="input_name half required" placeholder="@@app.simpletfai.inputname@@"/>');
        
        if (type === "image" || type === "numbers" || type === "text") {
            $(row).find(".input-fields").append('<select class="input_datatype"><option value="Float">@@app.simpletfai.float@@</option><option value="Double">@@app.simpletfai.double@@</option><option value="Integer">@@app.simpletfai.integer@@</option><option value="UInt8">@@app.simpletfai.uint8@@</option><option value="Long">@@app.simpletfai.long@@</option></select>');
        }
        
        if (type === "image") {
            $(row).find(".row-title").text("@@app.simpletfai.image@@");
            $(row).find(".input-fields").append('<div><select class="input_form"></select><input class="input_image half required" placeholder="@@app.simpletfai.urlorfieldid@@"/></div>');
            $.each(thisObj.getForms, function(i, v){
                $(row).find(".input_form").append('<option value="'+v.value+'">'+v.label+'</option>');
            });
            $(row).find(".input-fields").append('<div><input class="input_height small required" placeholder="@@app.simpletfai.height@@"/><input class="input_width small required" placeholder="@@app.simpletfai.width@@"/><input class="input_mean small required" placeholder="@@app.simpletfai.mean@@"/><input class="input_scale small required" placeholder="@@app.simpletfai.scale@@"/></div>');
            
            $(row).find(".input_image").autocomplete({
                source: [],
                minLength: 0,
                open: function() {
                    $(this).autocomplete('widget').css('z-index', 99999);
                    return false;
                }
            });
            
            $(row).find(".input_form").on("change", function(){
                var value = $(this).val();
                if (value === "" || thisObj.fieldOptions[value] !== undefined) {
                    thisObj.updateSource(value, $(row));
                } else {
                    $.ajax({
                        url: thisObj.options.contextPath + '/web/json/console/app' + thisObj.options.appPath + '/form/columns/options?formDefId=' + escape(value),
                        dataType: "text",
                        method: "GET",
                        success: function(data) {
                            if (data !== undefined && data !== null) {
                                var options = $.parseJSON(data);
                                thisObj.fieldOptions[value] = options;
                                thisObj.updateSource(value, $(row));
                            }
                        }
                    });
                }
            });
        } else if (type === "text") {
            $(row).find(".row-title").text("@@app.simpletfai.text@@");
            $(row).find(".input-fields").append('<div><input class="input_text full required" placeholder="@@app.simpletfai.textvalue@@"/></div>');
            $(row).find(".input-fields").append('<div><input class="input_dict half required" placeholder="@@app.simpletfai.dictionary@@"/> <a class="choosefile btn button small">@@peditor.chooseFile@@</a> <a class="clearfile btn button small">@@peditor.clear@@</a></div>');
            $(row).find(".input-fields").append('<div><input class="input_maxlength half required" placeholder="@@app.simpletfai.maxlength@@"/><label><input class="input_fillback" type="checkbox" value="true"/> @@app.simpletfai.fillBack@@<label></div>');
        } else if (type === "numbers") {
            $(row).find(".row-title").text("@@app.simpletfai.numbers@@");
            $(row).find(".input-fields").append('<div><input class="input_numbers full required" placeholder="@@app.simpletfai.numbervalues@@"/></div>');
        } else if (type === "boolean") {
            $(row).find(".row-title").text("@@app.simpletfai.boolean@@");
            $(row).find(".input-fields").append('<select class="input_boolean"><option value="true">@@app.simpletfai.true@@</option><option value="false">@@app.simpletfai.false@@</option></select>');
        }
        $(container).append(row);
    },
    updateSource : function(value, row) {
        var thisObj = this;
        var source = [];
        if (thisObj.fieldOptions[value] !== undefined) {
            $.each(thisObj.fieldOptions[value], function(i, option) {
                if (option['value'] !== "" && $.inArray(option['value'], source) === -1) {
                    source.push(option['value']);
                }
            });
        }
        source.sort();
        $(row).find(".input_image").autocomplete("option", "source", source);
        
        if ($(row).find(".input_image").val() !== "" && $.inArray($(row).find(".input_image").val(), source) === -1) {
            $(row).find(".input_image").val("");
        } 
    },
    addOutput : function(button) {
        var thisObj = this;
        var container = $(button).parent().parent().find("> .outputrows-container");
        var row = $('<div class="perow tfio-row output"><table><tr><td><input class="output_name required" placeholder="@@app.simpletfai.outputname@@"/></td><td><input class="output_variable required" placeholder="@@app.simpletfai.variableName@@"/></td><td class="alignright"><a class="deleteoutput" title="@@app.simpletfai.deleteOutput@@"><i class="fas fa-trash-alt"></i></a></td></tr></table></div>');
        
        $(container).append(row);
    },
    addPost : function(button, type) {
        var thisObj = this;
        var container = $(button).parent().parent().find("> .postrows-container");
        var row = $('<div class="perow tfio-row post" data-type="'+type+'"><label class="row-title"></label><a class="deletepost" title="@@app.simpletfai.deletepost@@"><i class="fas fa-trash-alt"></i></a><div class="input-fields"></div></div>');
        
        $(row).find(".input-fields").append('<input class="post_name half required" placeholder="@@app.simpletfai.postname@@"/>');
        
        if (type === "labels") {
            $(row).find(".row-title").text("@@app.simpletfai.labels@@");
            $(row).find(".input-fields").append('<input class="post_threshold small required" placeholder="@@app.simpletfai.threshold@@"/>');
            $(row).find(".input-fields").append('<label><input class="post_toplabel" type="checkbox" value="true"/> @@app.simpletfai.toplabel@@<label>');
            $(row).find(".input-fields").append('<div><input class="post_labels half required" placeholder="@@app.simpletfai.labels_file@@"/> <a class="choosefile btn button small">@@peditor.chooseFile@@</a> <a class="clearfile btn button small">@@peditor.clear@@</a></div>');
            $(row).find(".input-fields").append('<div><select class="post_variable half required"><option value="">@@app.simpletfai.variableName@@</option></select></div>');
        } else if (type === "valuelabel") {
            $(row).find(".row-title").text("@@app.simpletfai.valuelabel@@");
            $(row).find(".input-fields").append('<label><input class="post_unique" type="checkbox" value="true"/> @@app.simpletfai.unique@@<label>');
            $(row).find(".input-fields").append('<div><input class="post_labels half required" placeholder="@@app.simpletfai.labels_file@@"/> <a class="choosefile btn button small">@@peditor.chooseFile@@</a> <a class="clearfile btn button small">@@peditor.clear@@</a></div>');
            $(row).find(".input-fields").append('<div><select class="post_variable half required"><option value="">@@app.simpletfai.variableName@@</option></select><select class="post_variable2 half"><option value="">@@app.simpletfai.numberOfValues@@</option></select></div>');
        } else if (type === "euclideanDistance") {
            $(row).find(".row-title").text("@@app.simpletfai.euclideanDistance@@");
            $(row).find(".input-fields").append('<div><select class="post_variable half required"><option value="">@@app.simpletfai.variableName@@</option></select><select class="post_variable2 half required"><option value="">@@app.simpletfai.variableName@@</option></select></div>');
        }
        
        $(container).append(row);
        thisObj.updatePostVariables();
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

