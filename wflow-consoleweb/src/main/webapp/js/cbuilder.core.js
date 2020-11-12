CustomBuilder = {
    saveUrl : '',
    previewUrl : '',
    contextPath : '/jw',
    appId: '',
    appVersion: '',
    builderType: '',
    builderLabel: '',
    config: {
        builder : {
            callbacks : {
                initBuilder : "",
                load : "",
                saveEditProperties : "",
                cancelEditProperties : ""
            }
        },
        advanced_tools : {
            tree_viewer : {
                disabled : false,
                childs_properties : ["elements"],
                matchers : {
                    'editable' : {
                        match : function (viewer, deferreds, node, jsonObj, refObj) {
                            if (node.data['parent'] === undefined) {
                                DependencyTree.Util.createEditIndicator(viewer, node, function(){
                                    CustomBuilder.showPopUpBuilderProperties();
                                });
                            } else if (jsonObj['className'] !== undefined) {
                                DependencyTree.Util.createEditIndicator(viewer, node, function() {
                                    CustomBuilder.editProperties(jsonObj["className"], jsonObj["properties"]);
                                });
                            }   

                            return false;
                        }
                    }
                }
            },
            usage : {
                disabled : false
            },
            i18n : {
                disabled : false,
                keywords : [
                    "label"
                ]
            },
            permission : {
                disabled : false,
                permission_plugin : "org.joget.apps.form.model.FormPermission",
                authorized : {
                    property : "hidden",
                    default_value : "",
                    options : [
                        {
                            key : "vissible",
                            value : "",
                            label : get_cbuilder_msg("ubuilder.visible")
                        },
                        {
                            key : "hidden",
                            value : "true",
                            label : get_cbuilder_msg("ubuilder.hidden")
                        }
                    ]
                },
                unauthorized : {
                    property : "",
                    default_value : "",
                    options : [
                        {
                            key : "vissible",
                            value : "",
                            label : get_cbuilder_msg("ubuilder.visible")
                        },
                        {
                            key : "hidden",
                            value : "true",
                            label : get_cbuilder_msg("ubuilder.hidden")
                        }
                    ]
                },
                element_label_callback : "",
                element_support_plugin : [],
                display_element_id : false,
                childs_properties : ["elements"],
                ignore_classes : [],
                render_elements_callback : "" 
            },
            customTabsCallback : ""
        }
    },
    propertiesOptions: null,
    
    //undo & redo feature
    undoStack : new Array(),
    redoStack : new Array(),
    undoRedoMax : 50,
    
    json : null,
    data : {},
    paletteElements : {},
    
    //Tracker
    isCtrlKeyPressed : false,
    isAltKeyPressed : false,
    saveChecker : 0,
    
    callback : function(name, args) {
        if (name !== "" && name !== undefined && name !== null) {
            var func = PropertyEditor.Util.getFunction(name);
            if (func !== null && func !== undefined) {
                return func.apply(null, args);
            }
        }
    },
    
    initConfig : function (config) {
        CustomBuilder.config = $.extend(true, CustomBuilder.config, config);
    },
    
    initPropertiesOptions : function(options) {
        CustomBuilder.propertiesOptions = options;
    },
    
    initBuilder : function (callback) {
        //add control
        $("#builder-steps").after("<div class='controls'></div>");
        $(".controls").append("<a class='action-undo disabled' title='"+get_cbuilder_msg('ubuilder.undo.disabled.tip')+"'><i class='fas fa-undo'></i> "+get_cbuilder_msg('ubuilder.undo')+"</a>&nbsp;|&nbsp;");
        $(".controls").append("<a class='action-redo disabled' title='"+get_cbuilder_msg('ubuilder.redo.disabled.tip')+"'><i class='fas fa-redo'></i> "+get_cbuilder_msg('ubuilder.redo')+"</a>");
        
        $(".action-undo").click(function(){
            CustomBuilder.undo();
            return false;
        });
        
        $(".action-redo").click(function(){
            CustomBuilder.redo();
            return false;
        });
        
        //Shortcut key
        $(document).keyup(function (e) {
            if(e.which == 17){
                CustomBuilder.isCtrlKeyPressed=false;
            } else if(e.which === 18){
                CustomBuilder.isAltKeyPressed = false;
            }
        }).keydown(function (e) {
            if(e.which == 17){
                CustomBuilder.isCtrlKeyPressed=true;
            } else if(e.which === 18){
                CustomBuilder.isAltKeyPressed = true;
            }
            if ($(".property-editor-container:visible").length === 0) {
                if(e.which === 83 && CustomBuilder.isCtrlKeyPressed === true && !CustomBuilder.isAltKeyPressed) { //CTRL+S - save
                    CustomBuilder.mergeAndSave();
                    return false;
                }
                if(e.which === 90 && CustomBuilder.isCtrlKeyPressed === true && !CustomBuilder.isAltKeyPressed) { //CTRL+Z - undo
                    CustomBuilder.undo();
                    return false;
                }
                if(e.which === 89 && CustomBuilder.isCtrlKeyPressed === true && !CustomBuilder.isAltKeyPressed) { //CTRL+Y - redo
                    CustomBuilder.redo();
                    return false;
                }
                if(e.which === 80 && CustomBuilder.isCtrlKeyPressed === true && !CustomBuilder.isAltKeyPressed) { //CTRL+P - preview
                    CustomBuilder.preview();
                    return false;
                }
            }
        });
        
        //Steps tab
        $('#builder-steps li').click( function(){
            var div = $(this).find('a').attr('href');
            if($(div).size() > 0){
                $('#builder-steps li').removeClass("active");
                $('#builder-steps li').removeClass("next");
                $(this).addClass("active");
                $(this).prev().addClass("next");
                $('#builder-content').children().hide();
                $(div).show();
            }
            $("body").removeClass("stop-scrolling");
            return false;
        });
        
        $("#step-properties").on("click", function(){
            $("#step-properties-container").html("");
            var options = {
                appPath: CustomBuilder.appPath,
                contextPath: CustomBuilder.contextPath,
                propertiesDefinition : CustomBuilder.propertiesOptions,
                propertyValues : CustomBuilder.data.properties,
                showCancelButton:false,
                closeAfterSaved : false,
                changeCheckIgnoreUndefined: true,
                autoSave: true,
                saveCallback: CustomBuilder.saveBuilderProperties
            };
            $('#step-properties-container').propertyEditor(options);
            $("body").addClass("stop-scrolling");
            return true;
        });
        
        CustomBuilder.callback(CustomBuilder.config.builder.callbacks["initBuilder"], [callback]);
    },
    
    showPopUpBuilderProperties : function() {
        var options = {
            appPath: CustomBuilder.appPath,
            contextPath: CustomBuilder.contextPath,
            propertiesDefinition : CustomBuilder.propertiesOptions,
            propertyValues : CustomBuilder.data.properties,
            showCancelButton:false,
            closeAfterSaved : false,
            changeCheckIgnoreUndefined: true,
            autoSave: true,
            saveCallback: CustomBuilder.saveBuilderProperties
        };
        
        // show popup dialog
        if (!PropertyEditor.Popup.hasDialog(CustomBuilder.builderType + "-property-editor")) {
            PropertyEditor.Popup.createDialog(CustomBuilder.builderType + "-property-editor");
        }
        PropertyEditor.Popup.showDialog(CustomBuilder.builderType + "-property-editor", options);
    },
    
    saveBuilderProperties : function(container, properties) {
        CustomBuilder.data.properties = properties;
        CustomBuilder.update();
    },
    
    initPaletteElement : function(category, className, label, icon, propertyOptions, defaultPropertiesValues, render, css, metaData){
        if ((typeof propertyOptions) === "string") {
            try {
                propertyOptions = eval(propertyOptions);
            } catch (err) {
                if (console.log) {
                    console.log("error retrieving properties options of " + label + " : " + err);
                }
                return;
            }
        }
        if ((typeof defaultPropertiesValues) === "string") {
            try {
                defaultPropertiesValues = eval("["+defaultPropertiesValues+"]")[0];
            } catch (err) {
                if (console.log) {
                    console.log("error retrieving default property values of " + label + " : " + err);
                }
                return;
            }
        }
        
        if (css === undefined || css === null) {
            css = "";
        }
        
        this.paletteElements[className] = new Object();
        this.paletteElements[className]['label'] = label;
        this.paletteElements[className]['propertyOptions'] = propertyOptions;
        this.paletteElements[className]['properties'] = defaultPropertiesValues;
        
        if (metaData !== undefined && metaData !== null) {
            this.paletteElements[className] = $.extend(this.paletteElements[className], metaData);
        }

        if (render === undefined || render !== false) {
            var iconObj = null;
            if (icon !== undefined && icon !== null && icon !== "") {
                try {   
                    iconObj = $(icon);
                } catch (err) {
                    iconObj =  $('<span class="image" style="background-image:url(\'' + CustomBuilder.contextPath + icon + '\');" />');
                }
            } else {
                iconObj = $('<i class="fas fa-th-large"></i>');
            }

            var container;
            //get category
            if (category !== undefined && category !== null && category !== "") {
                var categoryId = category.replace(/\s/g , "-");
                if($('ul#'+categoryId).length == 0){
                    $('#builder-palette-body').append('<h3>'+category+'</h3><ul id="'+categoryId+'"></ul>');
                }
                container = $('ul#'+categoryId);
            } else {
                if ( $('#builder-palette-body > ul').length === 0) {
                    $('#builder-palette-body').append('<ul></ul>');
                }
                container = $('#builder-palette-body > ul');
            }

            var li = $('<li><div id="'+className.replace(".", "_")+'" element-class="'+className+'" class="builder-palette-element '+css+'"> <label class="label">'+label+'</label></div></li>');
            $(li).find('.builder-palette-element').prepend(iconObj);
            $(container).append(li);
        }
    },
    
    loadJson : function(json, addToUndo) {
        CustomBuilder.data = JSON.decode(json);
        
        //callback to render json
        CustomBuilder.callback(CustomBuilder.config.builder.callbacks["load"], [CustomBuilder.data]);
        
        $("#loading").remove();
    },
    
    update : function(addToUndo) {
        var json = JSON.encode(CustomBuilder.data);
        CustomBuilder.updateJson(json, addToUndo);
        CustomBuilder.updatePasteIcons();
    },
    
    updateJson : function (json, addToUndo) {
        if (CustomBuilder.json !== null && addToUndo !== false) {
            CustomBuilder.addToUndo();
        }
        
        CustomBuilder.json = json;
        CustomBuilder.adjustJson();
    },
    
    getJson : function () {
        return CustomBuilder.json;
    },
    
    save : function(){
        CustomBuilder.showMessage(get_cbuilder_msg('cbuilder.saving'));
        var self = this;
        var json = this.getJson();
        $.post(this.saveUrl, {json : json} , function(data) {
            var d = JSON.decode(data);
            if(d.success == true){
                $('#cbuilder-json-original').val(json);
                CustomBuilder.updateSaveStatus("0");
                CustomBuilder.showMessage("");
            }else{
                alert(get_cbuilder_msg('ubuilder.saveFailed'));
            }
            CustomBuilder.showMessage("");
        }, "text");
    },

    preview : function() {
        $('#cbuilder-json').val(this.getJson());
        $('#cbuilder-preview').attr("action", this.previewUrl);
        $('#cbuilder-preview').submit();
        return false;
    },
    
    updateFromJson: function() {
        var json = $('#cbuilder-json').val();
        if (CustomBuilder.getJson() !== json) {
            CustomBuilder.loadJson(json);
        }
        return false;
    },
    
    //Undo the changes from stack
    undo : function(){
        if(this.undoStack.length > 0){
            //if redo stack is full, delete first
            if(this.redoStack.length >= this.undoRedoMax){
                this.redoStack.splice(0,1);
            }

            //save current json data to redo stack
            this.redoStack.push(this.getJson());

            //load the last data from undo stack
            var loading = $('<div id="loading"><i class="fas fa-spinner fa-spin fa-2x"></i> ' + get_cbuilder_msg("ubuilder.label.undoing") + '</div>');
            $("body").append(loading);
            this.loadJson(this.undoStack.pop(), false);

            //enable redo button if it is disabled previously
            if(this.redoStack.length === 1){
                $('.action-redo').removeClass('disabled');
                $('.action-redo').attr('title', get_cbuilder_msg('ubuilder.redo.tip'));
            }

            //if undo stack is empty, disabled undo button
            if(this.undoStack.length === 0){
                $('.action-undo').addClass('disabled');
                $('.action-undo').attr('title', get_cbuilder_msg('ubuilder.undo.disabled.tip'));
            }

            this.updateSaveStatus("-");
        }
    },

    //Redo the changes from stack
    redo : function(){
        if(this.redoStack.length > 0){
            //if undo stack is full, delete first
            if(this.undoStack.length >= this.undoRedoMax){
                this.undoStack.splice(0,1);
            }

            //save current json data to undo stack
            this.undoStack.push(this.getJson());

            //load the last data from redo stack
            var loading = $('<div id="loading"><i class="fas fa-spinner fa-spin fa-2x"></i> ' + get_cbuilder_msg("ubuilder.label.redoing") + '</div>');
            $("body").append(loading);
            this.loadJson(this.redoStack.pop(), false);

            //enable undo button if it is disabled previously
            if(this.undoStack.length === 1){
                $('.action-undo').removeClass('disabled');
                $('.action-undo').attr('title', get_cbuilder_msg('ubuilder.undo.tip'));
            }

            //if redo stack is empty, disabled redo button
            if(this.redoStack.length === 0){
                $('.action-redo').addClass('disabled');
                $('.action-redo').attr('title', get_cbuilder_msg('ubuilder.redo.disabled.tip'));
            }

            this.updateSaveStatus("+");
        }
    },
    
    //Add changes info to stack
    addToUndo : function(json){
        //if undo stack is full, delete first
        if(this.undoStack.length >= this.undoRedoMax){
            this.undoStack.splice(0,1);
        }
        
        if (json === null || json === undefined) {
            json = this.getJson();
        }

        //save current json data to undo stack
        this.undoStack.push(json);

        //enable undo button if it is disabled previously
        if(this.undoStack.length === 1){
            $('.action-undo').removeClass('disabled');
            $('.action-undo').attr('title', get_cbuilder_msg('ubuilder.undo.tip'));
        }

        this.updateSaveStatus("+");
    },
    
    adjustJson: function() {
        // update JSON
        $('#cbuilder-json').val(this.getJson()).trigger("change");
    },
    
    //track the save status
    updateSaveStatus : function(mode){
        if(mode === "+"){
            this.saveChecker++;
        }else if(mode === "-"){
            this.saveChecker--;
        }else if(mode === "0"){
            this.saveChecker = 0;
        }
    },
    
    showMessage: function(message) {
        if (message && message !== "") {
            $("#builder-message").html(message);
            $("#builder-message").fadeIn();
        } else {
            $("#builder-message").fadeOut();
        }
    },
    
    getCopiedElement : function() {
        var time = $.localStorage.getItem("customBuilder_"+CustomBuilder.builderType+".copyTime");
        //10 mins
        if (time !== undefined && time !== null && ((new Date()) - (new Date(time))) > 3000000) {
            $.localStorage.removeItem("customBuilder_"+CustomBuilder.builderType+".copyTime");
            $.localStorage.removeItem("customBuilder_"+CustomBuilder.builderType+".copy");
            return null;
        }
        var copied = $.localStorage.getItem("customBuilder_"+CustomBuilder.builderType+".copy");
        if (copied !== undefined && copied !== null) {
            return JSON.decode(copied);
        }
        return null;
    },
    
    copy : function(element, type) {
        var copy = new Object();
        copy['type'] = type;
        copy['object'] = element;
        
        $.localStorage.setItem("customBuilder_"+CustomBuilder.builderType+".copy", JSON.encode(copy));
        $.localStorage.setItem("customBuilder_"+CustomBuilder.builderType+".copyTime", new Date());
        CustomBuilder.updatePasteIcon(type);
        CustomBuilder.showMessage(get_cbuilder_msg('ubuilder.copied'));
        setTimeout(function(){ CustomBuilder.showMessage(""); }, 2000);
    },
    
    updatePasteIcon : function(type) {
        $(".element-paste").addClass("disabled");
        $(".element-paste."+type).removeClass("disabled");
    },
    
    updatePasteIcons : function() {
        var type = "dummyclass";
        var copied = CustomBuilder.getCopiedElement();
        if (copied !== null) {
            type = copied['type'];
        }
        CustomBuilder.updatePasteIcon(type);
    },
    
    showDiff : function (callback, output) {
        var jsonUrl = CustomBuilder.contextPath + '/web/json/console/app/' + CustomBuilder.appId + '/' + CustomBuilder.appVersion + '/cbuilder/'+CustomBuilder.builderType+'/json/' + this.data.properties.id;
        var thisObject = this;
        var merged;
        var currentSaved;
        $.ajax({
            type: "GET",
            url: jsonUrl,
            dataType: 'json',
            success: function (data) {
                var current = data;
                var currentString = JSON.stringify(data);
                currentSaved = currentString;
                $('#cbuilder-json-current').val(currentString);
                var original = JSON.decode($('#cbuilder-json-original').val());
                var latest = JSON.decode($('#cbuilder-json').val());
                merged = DiffMerge.merge(original, current, latest, output);
            },
            error: function() {
                currentSaved = $('#cbuilder-json-current').val();
                merged = $('#cbuilder-json').val();
            },
            complete: function() {
                if (callback) {
                    callback.call(thisObject, currentSaved, merged);
                }    
            }
        });
    },
            
    merge: function (callback) {
        // get current remote definition
        CustomBuilder.showMessage(get_cbuilder_msg('ubuilder.merging'));
        var thisObject = this;
        
        CustomBuilder.showDiff(function (currentSaved, merged) {
            if (currentSaved !== undefined && currentSaved !== "") {
                $('#cbuilder-json-original').val(currentSaved);
            }
            if (merged !== undefined && merged !== "") {
                $('#cbuilder-json').val(merged);
            }
            CustomBuilder.updateFromJson();
            CustomBuilder.showMessage("");
            
            if (callback) {
                callback.call(thisObject, merged);
            }
        });
    },
    
    mergeAndSave: function() {
        CustomBuilder.merge(CustomBuilder.save);
    }, 
    
    supportTreeViewer: function() {
        return !CustomBuilder.config.advanced_tools.tree_viewer.disabled;
    },
    
    supportI18n: function() {
        return !CustomBuilder.config.advanced_tools.i18n.disabled;
    },
    
    supportUsage: function() {
        return !CustomBuilder.config.advanced_tools.usage.disabled;
    },
    
    supportPermission: function() {
        return !CustomBuilder.config.advanced_tools.permission.disabled;
    },
    
    customAdvancedToolTabs: function() {
        CustomBuilder.callback(CustomBuilder.config.advanced_tools["customTabsCallback"]);
    },
    
    getPermissionElementLabel: function(element) {
        if (element["className"] !== undefined && element["className"] !== "") {
            var plugin = CustomBuilder.paletteElements[element["className"]];
            if (plugin !== null && plugin !== undefined) {
                return plugin.label;
            }
        }
        return "";
    },
    
    saveBuilderPropertiesfunction(container, properties){
        CustomBuilder.data.properties = $.extend(CustomBuilder.data.properties, properties);
        CustomBuilder.update();

        $('#step-design').click();
    },
    
    editProperties: function(elementClass, elementProperty) {
        if (CustomBuilder.paletteElements[elementClass] === undefined) {
            return;
        }
        var elementOptions = CustomBuilder.paletteElements[elementClass].propertyOptions;

        // show property dialog
        var options = {
            appPath: "/" + CustomBuilder.appId + "/" + CustomBuilder.appVersion,
            contextPath: CustomBuilder.contextPath,
            propertiesDefinition : elementOptions,
            propertyValues : elementProperty,
            showCancelButton:true,
            changeCheckIgnoreUndefined: true,
            cancelCallback: function() {
                CustomBuilder.callback(CustomBuilder.config.builder.callbacks["cancelEditProperties"], []);
            },
            saveCallback: function(container, properties) {
                elementProperty = $.extend(elementProperty, properties);
                
                CustomBuilder.callback(CustomBuilder.config.builder.callbacks["saveEditProperties"], [container, elementProperty]);
                CustomBuilder.update();
            }
        };

        // show popup dialog
        if (!PropertyEditor.Popup.hasDialog(CustomBuilder.builderType+"-property-editor")) {
            PropertyEditor.Popup.createDialog(CustomBuilder.builderType+"-property-editor");
        }
        PropertyEditor.Popup.showDialog(CustomBuilder.builderType+"-property-editor", options);
    },
    
    uuid : function(){
        return 'xxxxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {  //xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        }).toUpperCase();
    }
};
