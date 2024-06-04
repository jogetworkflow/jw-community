PropertyEditor = {};
PropertyEditor.Model = {};
PropertyEditor.Type = {};
PropertyEditor.Validator = {};

PropertyEditor.SimpleMode = {
    render : function(container, options) {
        setTimeout(function(){
            options["simpleMode"] = true;
            options["closeAfterSaved"] = false;
            $(container).propertyEditor(options);
        }, 10);
    }
};

PropertyEditor.Popup = {
    propertyDialog : {},
    hasDialog : function(id) {
        return PropertyEditor.Popup.propertyDialog[id] !== undefined;
    },
    createDialog : function(id) {
        if (PropertyEditor.Popup.hasDialog(id)) {
            return;
        }
        PropertyEditor.Popup.propertyDialog[id] = new Boxy(
            '<div id="'+id+'"></div>',
            {
                title: '<i class="fas fa-arrows-alt"></i><label class="autosave">'+get_peditor_msg('peditor.autoSaveWhenClose')+' <input type="checkbox"/></label>',
                closeable: true,
                draggable: true,
                show: false,
                fixed: true,
                beforeHide : function() {
                    PropertyEditor.Popup.cleanDialog(id);
                },
                afterHide : function() {
                    $("#"+id).html("");
                }
            }
        );
    },
    cleanDialog : function(id) {
        if ($("#"+id + " .property-editor-container").length === 0) {
            return;
        }
        var popupProp = {
            width : $("#"+id + " .property-editor-container").width(),
            height : $("#"+id + " .property-editor-container").height(),
            x : $("#"+id).closest(".boxy-wrapper").position().left,
            y : $("#"+id).closest(".boxy-wrapper").position().top,
            autoSave : $("#"+id).closest(".boxy-wrapper").find(".autosave input").is(":checked")
        };
        $.localStorage.setItem(id+".boxy", JSON.encode(popupProp));
        if ($("#"+id).hasClass("ui-resizable")) {
            $("#"+id).resizable( "destroy" );
        }
    },
    checkChangeAndHide : function(id, checkSave, hide) {
        var editor = $("#"+id).data("editor");
        if (!editor.saved && editor.isChange()) {
            if (checkSave === true && $("#"+id).closest(".boxy-wrapper").find(".autosave input").is(":checked")) {
                $("#"+id).data("disable-hide", !hide);
                editor.save();
            } else if (!confirm(get_peditor_msg('peditor.confirmClose'))) {
                return false;
            }
        }
        PropertyEditor.Popup.cleanDialog(id);
        if (hide !== undefined && hide === true) {
            PropertyEditor.Popup.hideDialog(id);
        }
        return true;
    },
    showDialog : function(id, options, args) {
        if (!PropertyEditor.Popup.hasDialog(id)) {
            return;
        }
        
        if ($("#"+id).find(".property-editor-container").length > 0) {
            if (!PropertyEditor.Popup.checkChangeAndHide(id, true, false)) {
                return;
            }
        }
        $("#"+id).html("");
        
        if (args !== null && args !== undefined && args.id !== null && args.id !== undefined) {
            $("#"+id).attr("data-id", args.id);
        }
        
        PropertyEditor.Popup.propertyDialog[id].show();
        
        if (options.cancelCallback !== undefined) {
            var orgCancelCallback = options.cancelCallback;
            options.cancelCallback = function() {
                orgCancelCallback();
                PropertyEditor.Popup.checkChangeAndHide(id, false, true);
            };
        }
        if (options.saveCallback !== undefined) {
            var orgSaveCallback = options.saveCallback;
            options.saveCallback = function(container, properties) {
                orgSaveCallback(container, properties);
                if ($("#"+id).data("disable-hide") !== true) {
                    PropertyEditor.Popup.hideDialog(id);
                } else {
                    PropertyEditor.Popup.cleanDialog(id);
                }
                $("#"+id).data("disable-hide", false);
            };
        }
        
        $("#"+id).closest(".boxy-wrapper").off("keydown.popup");
        $("#"+id).closest(".boxy-wrapper").on("keydown.popup", function(e) {
            if (e.which === 27 && $(".property_editor_hashassit").length === 0) {
                PropertyEditor.Popup.checkChangeAndHide(id, true, true);
            }
        });
        
        options.isPopupDialog = true;
         
        $("#"+id).propertyEditor(options);
        
        $("#"+id).closest(".boxy-wrapper").addClass("property-boxy-wrapper");
        
        $("#"+id).closest(".boxy-wrapper").find(".title-bar .close").off("click");
        $("#"+id).closest(".boxy-wrapper").find(".title-bar .close").on("click", function(e){
            e.stopImmediatePropagation();
            PropertyEditor.Popup.checkChangeAndHide(id, true, true);
            return false;
        });
        
        PropertyEditor.Popup.positionDialog(id, args);
    },
    hideDialog : function(id) {
        if (!PropertyEditor.Popup.hasDialog(id)) {
            return;
        }
        PropertyEditor.Popup.propertyDialog[id].hide();
    },
    adjustSize : function(id, width, height) {
        $("#"+id + " .property-editor-container").css("width", width + "px");
        $("#"+id + " .property-editor-container").css("height", height + "px");
        $("#"+id + " .property-editor-container").find(".property-editor-property-container").css("height", (height - 114) + "px");
        $("#"+id + " .property-editor-container").closest(".boxy-content").css("width", "auto");
        $("#"+id + " .property-editor-container").closest(".boxy-content").css("height", "auto");
        
        if (width <= 680) {
            $("#"+id + " .property-editor-container").addClass("narrow");
        } else {
            $("#"+id + " .property-editor-container").removeClass("narrow");
        }
    },
    positionDialog : function(id, args) {
        if (!PropertyEditor.Popup.hasDialog(id)) {
            return;
        }
        
        if ($("#"+id).hasClass("ui-resizable")) {
            $("#"+id).resizable( "destroy" );
        }
        $("#"+id).resizable({
            minHeight: 300,
            minWidth: 300,
            resize : function ( event, ui ) {
                PropertyEditor.Popup.adjustSize(id, ui.size.width, ui.size.height);
            }
        });
        
        var popupProp = null;
        var popupPropJson = $.localStorage.getItem(id+".boxy");
        if (popupPropJson !== null && popupPropJson !== undefined) {
            popupProp = JSON.decode(popupPropJson);
        }
        
        if (popupProp !== null && popupProp.autoSave === true) {
            $("#"+id).closest(".boxy-wrapper").find(".autosave input").prop("checked", true);
        } 
        
        //adjust width & height
        var width = $(window).width() * 0.8;
        var height = $(window).height() * 0.85 - 25;
        if (popupProp !== null && popupProp !== undefined) {
            if (popupProp.width !== undefined) {
                width = popupProp.width;
            }
            if (popupProp.height !== undefined) {
                height = popupProp.height;
            }
            
            //if width & height is larger than window
            if ((width + popupProp.x + 30 > $(window).width()) || (height + popupProp.y + 55 > $(window).height())) {
                popupProp = null;
                width = $(window).width() * 0.8;
                height = $(window).height() * 0.85 - 25;
            }
        } else if (args !== undefined) {
            if (args.defaultWidth !== undefined && width > args.defaultWidth) {
                width = args.defaultWidth;
            }
            if (args.defaultHeight !== undefined && height > args.defaultHeight) {
                height = args.defaultHeight;
            }
        }
        if ($("body").hasClass("property-editor-right-panel")) {
            width = $("#right-panel").outerWidth(true) - 32;
            height = $("#right-panel").outerHeight(true) + 10 ;
        }
        PropertyEditor.Popup.adjustSize(id, width, height);
        
        if ($("body").hasClass("property-editor-right-panel")) {
            $("#"+id + " .property-editor-container").closest(".boxy-wrapper").css("left", "auto");
            $("#"+id + " .property-editor-container").closest(".boxy-wrapper").css("top", "65px");
            $("#"+id + " .property-editor-container").closest(".boxy-wrapper").css("right", "0px");
        } else {
            if (popupProp !== null && popupProp !== undefined && popupProp.x !== undefined&& popupProp.y !== undefined) {
                $("#"+id + " .property-editor-container").closest(".boxy-wrapper").css("left", popupProp.x + "px");
                $("#"+id + " .property-editor-container").closest(".boxy-wrapper").css("top", popupProp.y + "px");
            } else {
                PropertyEditor.Popup.propertyDialog[id].center('x');
                PropertyEditor.Popup.propertyDialog[id].center('y');
            }
        }
    }
};

/* Utility Functions */
PropertyEditor.Util = {
    resources: {},
    cachedAjaxCalls: {},
    timeCachedAjaxCalls: {},
    ajaxCalls: {},
    prevAjaxCalls: {},
    types: {},
    validators: {},
    escapeHtmlTag: function(string) {
        string = String(string);

        var regX = /&/g;
        var replaceString = '&amp;';
        string = string.replace(regX, replaceString);

        var regX = /</g;
        var replaceString = '&lt;';
        string = string.replace(regX, replaceString);

        regX = />/g;
        replaceString = '&gt;';
        string = string.replace(regX, replaceString);

        regX = /"/g;
        replaceString = '&quot;';
        return string.replace(regX, replaceString);
    },
    deepEquals: function(editor, o1, o2, parentId) {
        if (o1 === o2) {
            return true;
        }
        if (o1 === undefined || o1 === null || o2 === undefined || o2 === null ) {
            return false;
        }
        
        var aProps = Object.getOwnPropertyNames(o1);
        var bProps = Object.getOwnPropertyNames(o2);

        var temp = [];
        for (var i = 0; i < aProps.length; i++) {
            if ($.inArray(aProps[i], temp) === -1) {
                temp.push(aProps[i]);
            }
        }
        if (editor.options.changeCheckIgnoreUndefined === undefined || !editor.options.changeCheckIgnoreUndefined) {
            for (var i = 0; i < bProps.length; i++) {
                if ($.inArray(bProps[i], temp) === -1) {
                    temp.push(bProps[i]);
                }
            }
        }
        

        for (var i = 0; i < temp.length; i++) {
            var propName = temp[i];
            if ((typeof o1[propName] === "object" || typeof o2[propName] === "object")) {
                var returnFalse = true;
                if ((o1[propName]["className"] !== undefined || o1[propName]["className"] !== undefined) &&
                        ((o1[propName] === undefined && o2[propName]["className"] === "") ||
                        (o2[propName] === undefined && o1[propName]["className"] === ""))) {
                    //to handle empty element select
                    returnFalse = false;
                } else if ((Array.isArray(o1[propName]) || Array.isArray(o2[propName])) && 
                        ((o2[propName] === undefined && o1[propName].length === 0) || 
                        (o1[propName] === undefined && o2[propName].length === 0))) {
                    //to handle empty grid
                    returnFalse = false;
                } else if ((o1[propName] === "" || o1[propName]["className"] === "") && o2[propName] === null) {
                    //to handle null original value
                    returnFalse = false;
                } else if (o1[propName] !== undefined && o2[propName] !== undefined && PropertyEditor.Util.deepEquals(editor, o1[propName], o2[propName], propName)) {
                    returnFalse = false;
                }
                if (returnFalse) {
                    return false;
                }
            } else if ((o1[propName] !== undefined && o2[propName] !== undefined && o1[propName] !== o2[propName]) ||
                (o1[propName] === undefined && o2[propName] !== "") ||
                (o2[propName] === undefined && o1[propName] !== "")) {
                var returnFalse = true;
                
                var fields = editor.fields;
                if (parentId !== "" && parentId !== undefined && fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                    fields = fields[parentId].fields;
                }
                
                if (fields[propName] !== undefined) {
                    if (fields[propName].properties['type'].toLowerCase() === "checkbox" && o2[propName] === fields[propName].properties['value'] && (o1[propName] === undefined || o1[propName] === "")) {
                        //to handle invalid false default value is set for checkbox
                        returnFalse = false;
                    } else if (fields[propName].properties['type'].toLowerCase() === "password" && o1[propName] === "%%%%%%%%" && (o2[propName] === undefined || o2[propName] === "")) {
                        //handle for password field empty value
                        returnFalse = false;
                    } else if (fields[propName].properties['type'].toLowerCase() === "hidden") {
                        //handle for hidden field
                        returnFalse = false;
                    }
                }
                
                if (returnFalse) {
                    return false;
                }
            }
        }
        return true;
    },
    inherit: function(base, methods) {
        var sub = function() {
            base.apply(this, arguments); // Call base class constructor
            // Call sub class initialize method that will act like a constructor
            this.initialize.apply(this);
        };

        sub.prototype = Object.create(base.prototype);
        $.extend(sub.prototype, methods);

        //register types and validators
        if (base === PropertyEditor.Model.Type) {
            PropertyEditor.Util.types[methods.shortname.toLowerCase()] = sub;
        } else if (base === PropertyEditor.Model.Validator) {
            PropertyEditor.Util.validators[methods.shortname.toLowerCase()] = sub;
        }

        return sub;
    },
    nl2br: function(string) {
        string = PropertyEditor.Util.escapeHtmlTag(string);
        var regX = /\n/g;
        var replaceString = '<br/>';
        return string.replace(regX, replaceString);
    },
    getFunction: function(name) {
        try {
            if ($.isFunction(name)) {
                return name;
            }
            var parts = name.split(".");
            var func = null;
            if (parts[0] !== undefined && parts[0] !== "") {
                func = window[parts[0]];
            }
            if (parts.length > 1) {
                for (var i = 1; i < parts.length; i++) {
                    func = func[parts[i]];
                }
            }

            return func;
        } catch (err) {};
        return null;
    },
    retrieveOptionsFromCallback: function(field, properties, reference) {
        try {
            if (properties.options_callback !== undefined && properties.options_callback !== null && properties.options_callback !== "") {
                var fields = field.editorObject.fields;
                if (field.parentId !== "" && field.parentId !== undefined) {
                    var parentId = field.parentId.substring(1);
                    if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                        fields = fields[parentId].fields;
                    }
                } 
                
                if (field.repeaterFields) {
                    fields = $.extend({}, fields, field.repeaterFields);
                }

                var on_change = null;
                if (properties.options_callback_on_change !== undefined && properties.options_callback_on_change !== null && properties.options_callback_on_change !== "") {
                    on_change = properties.options_callback_on_change;
                }

                var func = PropertyEditor.Util.getFunction(properties.options_callback);
                if ($.isFunction(func)) {
                    var onChangeValues = {};

                    if (on_change !== undefined && on_change !== null) {
                        var onChanges = on_change.split(";");
                        for (var i in onChanges) {
                            var fieldId = onChanges[i];
                            var param = fieldId;
                            var childField = "";
                            if (fieldId.indexOf(":") !== -1) {
                                param = fieldId.substring(0, fieldId.indexOf(":"));
                                fieldId = fieldId.substring(fieldId.indexOf(":") + 1);
                            }
                            if (fieldId.indexOf(".") !== -1) {
                                childField = fieldId.substring(fieldId.indexOf(".") + 1);
                                fieldId = fieldId.substring(0, fieldId.indexOf("."));
                            }
                            
                            var targetField = fields[fieldId];
                            var targetValue = "";
                            if (!targetField.isHidden() || field.isHidden()) { //if the field not render yet, get the value too
                                targetValue = targetField.value;
                            }
                            if (targetField.editor.find("#" + targetField.id).length > 0) {
                                var data = targetField.getData(true);
                                targetValue = data[fieldId];
                            }
                            if (childField !== "") {
                                if ($.isArray(targetValue)) { //is grid
                                    var values = [];
                                    for (var j in targetValue) {
                                        values.push(targetValue[j][childField]);
                                    }
                                    targetValue = values;
                                } else {
                                    if (targetValue === null || targetValue === undefined || targetValue[childField] === null || targetValue[childField] === undefined) {
                                        targetValue = "";
                                    } else {
                                        targetValue = targetValue[childField];
                                    }
                                }
                            } else if (targetValue === null || targetValue === undefined) {
                                targetValue = "";
                            }

                            onChangeValues[param] = targetValue;
                        }
                    }

                    var options = func(properties, onChangeValues);
                    if (options !== null) {
                        properties.options = options;
                    }
                }
            } else if (properties.options_script !== undefined && properties.options_script !== null && properties.options_script !== "") {
                try {
                    var options = eval(properties.options_script);
                    if (options !== null) {
                        properties.options = options;
                    }
                } catch (e) {}
            }
            if (properties.options_ajax === undefined && (properties.options_extra !== undefined && properties.options_extra !== null)) {
                var options = properties.options_extra;
                if (properties.options !== undefined && properties.options.length > 0) {
                    if (properties.options[0].value === "") {
                        var empty = properties.options[0];
                        properties.options.shift();
                        properties.options = options.concat(properties.options);
                        properties.options.unshift(empty);
                    } else {
                        properties.options = options.concat(properties.options);
                    }
                } else {
                    properties.options = options;
                }
            }
        } catch (err) {};
    },
    setUrlVariables : function(variables) {
        PropertyEditor.Util.UrlVariables = variables;
    },
    replaceContextPath: function(string, contextPath) {
        if (string === null) {
            return string;
        }
        var regX = /\[CONTEXT_PATH\]/g;
        string = string.replace(regX, contextPath);
        
        //replace url variable
        if (PropertyEditor.Util.UrlVariables !== undefined) {
            for (var property in PropertyEditor.Util.UrlVariables) {
                string = string.replace(new RegExp('\\\['+property+'\\\]', 'g'), PropertyEditor.Util.UrlVariables[property]);
                string = string.replace(new RegExp('([?&]'+property+'=)([&]+.*)*$', 'g'), '$1' + PropertyEditor.Util.UrlVariables[property] + '$2');
            }
        }
        
        return string;
    },
    getTypeObject: function(page, number, prefix, properties, value, defaultValue) {
        var type = properties.type.toLowerCase();
        var object = new PropertyEditor.Util.types[type](page, number, prefix, properties, value, defaultValue);
        return object;
    },
    getValidatorObject: function(page, properties) {
        var type = properties.type.toLowerCase();
        var object = new PropertyEditor.Util.validators[type](page, properties);
        return object;
    },
    uuid: function() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0,
                v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        }).toUpperCase();
    },
    unhandleDynamicOptionsField: function(page) {
        if (page !== null && page !== undefined) {
            var pageContainer = $(page.editor).find("#" + page.id);
            if ($(pageContainer).is("[data-control_field][data-control_value]")) {
                PropertyEditor.Util.unbindDynamicOptionsEvent($(pageContainer), page);
            }
            $(pageContainer).find("[data-control_field][data-control_value]").each(function() {
                PropertyEditor.Util.unbindDynamicOptionsEvent($(this), page);
            });
            $(pageContainer).find("[data-required_control_field][data-required_control_value]").each(function() {
                PropertyEditor.Util.unbindDynamicRequiredEvent($(this), page);
            });
        }
    },
    handleDynamicOptionsField: function(page) {
        if (page !== null && page !== undefined) {
            var triggerChangeFields = [];
            
            var pageContainer = $(page.editor).find("#" + page.id);
            if ($(pageContainer).is("[data-control_field][data-control_value]")) {
                PropertyEditor.Util.bindDynamicOptionsEvent($(pageContainer), page, triggerChangeFields);
            }
            $(pageContainer).find("[data-control_field][data-control_value]").each(function() {
                PropertyEditor.Util.bindDynamicOptionsEvent($(this), page, triggerChangeFields);
            });
            $(pageContainer).find("[data-required_control_field][data-required_control_value]").each(function() {
                PropertyEditor.Util.bindDynamicRequiredEvent($(this), page, triggerChangeFields);
            });
            
            for (var i in triggerChangeFields) {
                $(page.editor).find("[name=\"" + triggerChangeFields[i] + "\"]").trigger("change");
            }
        }
    },
    unbindDynamicOptionsEvent: function(element, page) {
        var control_id = element.data("control_id");
        var control_fields = element.data("control_field").split(";");
        
        for (var i in control_fields) {
            var control_field = control_fields[i];
            var field = null;
            if (page.fields !== undefined && page.fields[control_field] !== undefined) {
                field = page.fields[control_field];
            } else if (page.editorObject !== undefined) {
                var fields = page.editorObject.fields;
                if (page.parentId !== "" && page.parentId !== undefined) {
                    var parentId = page.parentId.substring(1);
                    if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                        fields = fields[parentId].fields;
                    }
                }
                field = fields[control_field];
            }
            if (field !== null && field !== undefined) {
                $(field.editor).off("change."+control_id+"_"+field.id);
            }
        }
    },
    bindDynamicOptionsEvent: function(element, page, triggerChangeFields) {
        var control_id = element.data("control_id");
        var control_fields = element.data("control_field").split(";");
        var controlVals = String(element.data("control_value")).split(";");
        var isRegex = element.data("control_use_regex");
        
        for (var i in control_fields) {
            var control_field = control_fields[i];
            var controlVal = controlVals[i];
        
            var field = null;
            if (page.fields !== undefined && page.fields[control_field] !== undefined) {
                field = page.fields[control_field];
            } else if (page.editorObject !== undefined) {
                var fields = page.editorObject.fields;
                if (page.parentId !== "" && page.parentId !== undefined) {
                    var parentId = page.parentId.substring(1);
                    if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                        fields = jQuery.extend({}, fields, fields[parentId].fields);
                    }
                }
                field = fields[control_field];
            }
            if (field !== null && field !== undefined) {
                $(field.editor).off("change."+control_id+"_"+field.id, "[name=\"" + field.id + "\"]");
                $(field.editor).on("change."+control_id+"_"+field.id, "[name=\"" + field.id + "\"]", function() {
                    var match;
                    if (control_fields.length > 1) {
                        match = PropertyEditor.Util.dynamicOptionsCheckValueMultiFields(element, page, element.data("control_field"), String(element.data("control_value")), isRegex);
                    } else {        
                        match = PropertyEditor.Util.dynamicOptionsCheckValue(field, controlVal, isRegex);
                    }
                    if (match) {
                        element.show();
                        element.removeClass("hidden");
                        if (element.hasClass("property-editor-page")) {
                            element.removeClass("property-page-hide");
                            element.addClass("property-page-show");

                            element.find(".property-editor-property:not(.hidden)").each(function() {
                                $(this).find("input, select, textarea, table").removeClass("hidden");
                            });
                        } else {
                            element.find("input, select, textarea, table").removeClass("hidden");
                        }
                    } else {
                        element.hide();
                        element.addClass("hidden");
                        element.find("input, select, textarea, table").addClass("hidden");
                        if (element.hasClass("property-editor-page")) {
                            element.addClass("property-page-hide");
                            element.removeClass("property-page-show");
                        }
                    }
                    element.find("input, select, textarea, table").trigger("change");
                    if (page.properties !== undefined && page.properties.properties !== undefined) {
                        $.each(page.properties.properties, function(i, property) {
                            var type = property.propertyEditorObject;
                            if (element.find("[name='" + type.id + "']").length > 0) {
                                type.pageShown();
                            }
                        });
                    } else {
                        $.each(page, function(i, property) {
                            var type = property;
                            if (element.find("[name='" + type.id + "']").length > 0) {
                                type.pageShown();
                            }
                        });
                    }

                    if (element.hasClass("property-editor-page")) {
                        var current = $(page.editor).find('.property-page-show.current');
                        if ($(current).length > 0) {
                            var pageId = $(current).attr("id");
                            page.editorObject.pages[pageId].refreshStepsIndicator();
                            page.editorObject.pages[pageId].buttonPanel.refresh();
                        }
                    }
                    if (element.hasClass("page-button-custom") && page.editorObject.isSinglePageDisplay()) {
                        $(field.editor).find('.property-editor-buttons').html('');
                        var buttonPanel = $(field.editor).find('.property-page-show.current .property-editor-page-button-panel').clone(true);
                        $(buttonPanel).find(".button_form").remove();
                        $(field.editor).find('.property-editor-buttons').append(buttonPanel);
                    }
                });
                
                if ($.inArray(field.id, triggerChangeFields) === -1) {
                    triggerChangeFields.push(field.id);
                }
            }
        }
    },
    unbindDynamicRequiredEvent: function(element, page) {
        var control_id = element.data("required_control_id");
        var control_fields = element.data("required_control_field").split(";");
        
        for (var i in control_fields) {
            var control_field = control_fields[i];
            var field = null;
            if (page.fields !== undefined && page.fields[control_field] !== undefined) {
                field = page.fields[control_field];
            } else if (page.editorObject !== undefined) {
                var fields = page.editorObject.fields;
                if (page.parentId !== "" && page.parentId !== undefined) {
                    var parentId = page.parentId.substring(1);
                    if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                        fields = fields[parentId].fields;
                    }
                }

                if (element.repeaterFields) {
                    fields = $.extend({}, fields, element.repeaterFields);
                }
                field = fields[control_field];
            }
            if (field !== null && field !== undefined) {
                $(field.editor).off("change."+control_id+"_"+field.id);
            }
        }
    },
    bindDynamicRequiredEvent: function(element, page, triggerChangeFields) {
        var control_id = element.data("required_control_id");
        var control_fields = element.data("required_control_field").split(";");
        var controlVals = String(element.data("required_control_value")).split(";");
        var isRegex = element.data("required_control_use_regex");
        
        for (var i in control_fields) {
            var control_field = control_fields[i];
            var controlVal = controlVals[i];
            
            var field = null;
            if (page.fields !== undefined && page.fields[control_field] !== undefined) {
                field = page.fields[control_field];
            } else if (page.editorObject !== undefined) {
                var fields = page.editorObject.fields;
                if (page.parentId !== "" && page.parentId !== undefined) {
                    var parentId = page.parentId.substring(1);
                    if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                        fields = fields[parentId].fields;
                    }
                }

                if (element.repeaterFields) {
                    fields = $.extend({}, fields, element.repeaterFields);
                }
                field = fields[control_field];
            }
            if (field !== null && field !== undefined) {
                $(field.editor).off("change."+control_id+"_"+field.id, "[name=\"" + field.id + "\"]");
                $(field.editor).on("change."+control_id+"_"+field.id, "[name=\"" + field.id + "\"]", function() {
                    var match;
                    if (control_fields.length > 1) {
                        match = PropertyEditor.Util.dynamicOptionsCheckValueMultiFields(element, page, element.data("required_control_field"), String(element.data("required_control_value")), isRegex);
                    } else {        
                        match = PropertyEditor.Util.dynamicOptionsCheckValue(field, controlVal, isRegex);
                    }
                    if (match) {
                        element.find(".property-required").show();
                    } else {
                        element.find(".property-required").hide();
                    }
                });
                
                if ($.inArray(field.id, triggerChangeFields) === -1) {
                    triggerChangeFields.push(field.id);
                }
            }
        }
    },
    handleSuggestionField: function(field) {
        if (field.properties.id_suggestion !== undefined && field.properties.id_suggestion !== null && field.properties.id_suggestion !== "") {
            //find the listening field
            var fieldId = field.properties.id_suggestion;
            var fields = field.editorObject.fields;
            if (field.parentId !== "" && field.parentId !== undefined) {
                var parentId = field.parentId.substring(1);
                if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                    fields = fields[parentId].fields;
                }
            }
            if (field.repeaterFields) {
                fields = $.extend({}, fields, field.repeaterFields);
            }
            
            if (fields[fieldId] !== undefined) {
                var  selector = "[name=\"" + fields[fieldId].id + "\"]";
                
                //listen to field changes and display a suggestion based on changed value
                $(field.editor).off("change.suggestion_"+field.id, selector);
                $(field.editor).on("change.suggestion_"+field.id, selector, function() {
                    var targetField = fields[fieldId];
                    var data = targetField.getData(true);
                    var targetValue = data[fieldId];
                    PropertyEditor.Util.displaySuggestion(field, targetValue);
                });
            }
        }  
    },
    displaySuggestion: function(field, value) {
        var input = $("#" + field.id);
        $(input).parent().find('.suggestions').remove();
        
        var text = $('<p>'+value+'</p>').text().trim(); //remove all html tag
        if (window['Pinyin'] !== undefined && window['Pinyin'].isSupported()) { // for Chinese
            var pinyin = '';
            var tokens = window['Pinyin'].parse(text);
            var lastToken;
            for(var i=0; i < tokens.length; i++){
                var v = tokens[i];
                if (v.type === 2) {
                    pinyin += pinyin && !/\n|\s/.test(lastToken.target) ? ' ' + v.target : v.target;
                } else {
                    pinyin += (lastToken && lastToken.type === 2 ? ' ' : '') + v.target;
                }
                lastToken = v;
            }
            if (pinyin !== "") {
                text = pinyin;
            }
        } else if (window['wanakana'] !== undefined && window['wanakana'].isJapanese(text)) { //for Japanese
            text = window['wanakana'].toRomaji(text);
        } else if (window['Aromanize'] !== undefined) { //for Korean
            text = window['Aromanize'].romanize(text);
        }
        if (getSlug !== undefined) {
            var lang = UI.locale.substring(0,2); 
            text = getSlug(text, { separator: "_",  truncate: 30, lang:lang});
        }
        
        var data = field.getData(true);
        var currentValue = data[field.id];
        
        if(text !== "" && text !== currentValue) {
            //render suggestion under field
            var suggestion = $('<em class="suggestions"><i>'+get_peditor_msg('peditor.suggestion')+'</i>: <span>'+text+'</span> <a class="accept">'+get_peditor_msg('peditor.accept')+'</a><em>');
            
            $(suggestion).find('a.accept').on('click', function(){
                $(input).val(text);
                $(suggestion).remove();
            });
            
            $(input).after(suggestion);
        }
    },
    unhandleOptionsField: function(field) {
        $(field.editor).off("change."+field.id);
    },
    handleOptionsField: function(field, reference, ajax_url, on_change, mapping, method, extra) {
        if (field.properties.options_callback !== undefined && field.properties.options_callback !== null && field.properties.options_callback !== "" &&
            field.properties.options_callback_on_change !== undefined && field.properties.options_callback_on_change !== null && field.properties.options_callback_on_change !== "") {
            var onChanges = field.properties.options_callback_on_change.split(";");
            var fieldIds = [];
            var fields = field.editorObject.fields;
            if (field.parentId !== "" && field.parentId !== undefined) {
                var parentId = field.parentId.substring(1);
                if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                    fields = fields[parentId].fields;
                }
            }
            
            if (field.repeaterFields) {
                fields = $.extend({}, fields, field.repeaterFields);
            }
            
            for (var i in onChanges) {
                var fieldId = onChanges[i];
                if (fieldId.indexOf(":") !== -1) {
                    fieldId = fieldId.substring(fieldId.indexOf(":") + 1);
                }
                if ($.inArray(fieldId, fieldIds) === -1) {
                    fieldIds.push(fieldId);
                }
            }
            for (var i in fieldIds) {
                var selector = "";
                var fieldId = fieldIds[i];
                if (fieldId.indexOf(".") !== -1) {
                    if (fieldId.indexOf(".properties") !== -1) {
                        selector = ".property-editor-page[elementid=\"" + fields[fieldId.substring(0, fieldId.indexOf("."))].id + "\"] .property-editor-property:not(.hidden) [name]";
                    } else {
                        selector = "#" + fields[fieldId.substring(0, fieldId.indexOf("."))].id + " [name=\"" + fieldId.substring(fieldId.indexOf(".") + 1) + "\"]";
                        if ($(field.editor).find(selector).length === 0) {
                            selector = "[name=\"" + fields[fieldId.substring(0, fieldId.indexOf("."))].id + "\"]";
                        }
                    }
                } else {
                    selector = "[name=\"" + fields[fieldId].id + "\"]";
                }
                $(field.editor).off("change."+field.id, selector);
                $(field.editor).on("change."+field.id, selector, function() {
                    PropertyEditor.Util.retrieveOptionsFromCallback(field, field.properties, reference);
                    field.handleAjaxOptions(field.properties.options, reference);
                });
            }
            return;
        }
        if (field !== null && field !== undefined && (ajax_url === undefined || ajax_url === null)) {
            ajax_url = field.properties.options_ajax;
        }
        if (field !== null && field !== undefined && (on_change === undefined || on_change === null)) {
            on_change = field.properties.options_ajax_on_change;
        }
        if (field !== null && field !== undefined && (mapping === undefined || mapping === null)) {
            mapping = field.properties.options_ajax_mapping;
        }
        if (field !== null && field !== undefined && (method === undefined || method === null)) {
            method = field.properties.options_ajax_method;
        }
        if (field !== null && field !== undefined && (extra === undefined || extra === null)) {
            extra = field.properties.options_extra;
        }
        if (field !== null && field !== undefined && ajax_url !== undefined && ajax_url !== null) {
            field.isDataReady = false;
            PropertyEditor.Util.callLoadOptionsAjax(field, reference, ajax_url, on_change, mapping, method, extra);
            if (on_change !== undefined && on_change !== null) {
                PropertyEditor.Util.fieldOnChange(field, reference, ajax_url, on_change, mapping, method, extra);
            }
        }
    },
    callLoadOptionsAjax: function(field, reference, ajax_url, on_change, mapping, method, extra) {
        var ajaxUrl = PropertyEditor.Util.getAjaxOptionsUrl(field, ajax_url, on_change);
        
        if (ajaxUrl === null || ajaxUrl === undefined) {
            //there is dependent field does not initialise yet, or validation error or getting dependent field value error
            return;
        }
        
        var prevAjaxUrl = PropertyEditor.Util.prevAjaxCalls[field.id + "::" + reference];
        if (prevAjaxUrl !== null && prevAjaxUrl !== undefined && prevAjaxUrl === ajaxUrl) {
            return;
        }

        if (PropertyEditor.Util.ajaxCalls[ajaxUrl] === undefined || PropertyEditor.Util.ajaxCalls[ajaxUrl] === null) {
            PropertyEditor.Util.ajaxCalls[ajaxUrl] = [];
        }
        
        PropertyEditor.Util.ajaxCalls[ajaxUrl].push({
            field: field,
            mapping: mapping,
            reference: reference
        });
        PropertyEditor.Util.prevAjaxCalls[field.id + "::" + reference] = ajaxUrl;
        
        if (PropertyEditor.Util.cachedAjaxCalls[ajaxUrl] !== undefined) {
            PropertyEditor.Util.handleAjaxOptions(PropertyEditor.Util.cachedAjaxCalls[ajaxUrl], ajaxUrl, extra);
            return;
        } else if (PropertyEditor.Util.timeCachedAjaxCalls[ajaxUrl] !== undefined) {
            //cache for 30sec
            if (((new Date().getTime()) - PropertyEditor.Util.timeCachedAjaxCalls[ajaxUrl].time) < 30000) {
                PropertyEditor.Util.handleAjaxOptions(PropertyEditor.Util.timeCachedAjaxCalls[ajaxUrl].data, ajaxUrl, extra);
                return;
            } else {
                delete PropertyEditor.Util.timeCachedAjaxCalls[ajaxUrl];
            }
        }

        if (PropertyEditor.Util.ajaxCalls[ajaxUrl].length === 1) {
            if (method === undefined || method.toUpperCase() !== "POST") {
                method = "GET";
            }

            PropertyEditor.Util.showAjaxLoading(field.editor, field, reference);
            $.ajax({
                url: ajaxUrl,
                dataType: "text",
                method: method.toUpperCase(),
                success: function(data) {
                    //cache the data for plugins options
                    if (ajaxUrl.indexOf("/property/json/getElements") !== -1
                            || ajaxUrl.indexOf("/workflowVariable/options") !== -1) {
                        PropertyEditor.Util.cachedAjaxCalls[ajaxUrl] = data;
                    } else {
                        PropertyEditor.Util.timeCachedAjaxCalls[ajaxUrl] = {
                            time : (new Date().getTime()),
                            data : data
                        };
                    }
                    
                    PropertyEditor.Util.handleAjaxOptions(data, ajaxUrl, extra);
                },
                error: function(xhr,status,error){
                    var calls = PropertyEditor.Util.ajaxCalls[ajaxUrl];
                    for (var i in calls) {
                        calls[i].field.handleAjaxOptions([], calls[i].reference);
                        PropertyEditor.Util.removeAjaxLoading(calls[i].field.editor, calls[i].field, calls[i].reference);
                    }
                    delete PropertyEditor.Util.ajaxCalls[ajaxUrl];
                }
            });
        }
    },
    handleAjaxOptions : function(data, ajaxUrl, extra) {
        if (data !== undefined && data !== null) {
            var options = [];
            if (data !== "") {
                options = $.parseJSON(data);
            }
            var calls = PropertyEditor.Util.ajaxCalls[ajaxUrl];
            for (var i in calls) {
                var tempOptions = options;

                if (calls[i].mapping !== undefined) {
                    if (calls[i].mapping.arrayObj !== undefined) {
                        tempOptions = PropertyEditor.Util.getValueFromObject(tempOptions, calls[i].mapping.arrayObj);
                    }

                    var newOptions = [];
                    calls[i].mapping.addEmpty = true;
                    if (calls[i].mapping.addEmpty !== undefined && calls[i].mapping.addEmpty) {
                        newOptions.push({ value: '', label: '' });
                    }

                    for (var o in tempOptions) {
                        if (calls[i].mapping.value !== undefined && calls[i].mapping.label !== undefined) {
                            newOptions.push({
                                value: PropertyEditor.Util.getValueFromObject(tempOptions[o], calls[i].mapping.value),
                                label: PropertyEditor.Util.getValueFromObject(tempOptions[o], calls[i].mapping.label)
                            });
                        } else {
                            newOptions.push(tempOptions[o]);
                        }
                    }
                    tempOptions = newOptions;
                }

                if (extra !== undefined && extra !== null) {
                    if (tempOptions !== undefined && tempOptions.length > 0) {
                        if (tempOptions[0].value === "") {
                            var empty = tempOptions[0];
                            tempOptions.shift();
                            tempOptions = extra.concat(tempOptions);
                            tempOptions.unshift(empty);
                        } else {
                            tempOptions = extra.concat(tempOptions);
                        }
                    } else {
                        tempOptions = extra;
                    }
                }

                calls[i].field.handleAjaxOptions(tempOptions, calls[i].reference);
                PropertyEditor.Util.removeAjaxLoading(calls[i].field.editor, calls[i].field, calls[i].reference);
                calls[i].field.isDataReady = true;
            }
            delete PropertyEditor.Util.ajaxCalls[ajaxUrl];
        }
    },
    //clear the cached ajax call results for ajax options field
    clearAjaxOptionsCache: function(field) {
        var ajax_url = field.properties.options_ajax;
        var on_change = field.properties.options_ajax_on_change;
        
        var ajaxUrl = PropertyEditor.Util.getAjaxOptionsUrl(field, ajax_url, on_change);
        
        if (ajaxUrl === null || ajaxUrl === undefined) {
            //there is dependent field does not initialise yet, or validation error or getting dependent field value error
            return;
        }
        
        delete PropertyEditor.Util.cachedAjaxCalls[ajaxUrl];
        delete PropertyEditor.Util.timeCachedAjaxCalls[ajaxUrl];
    },
    //construct the ajax call url based on the on change dependencies fields
    getAjaxOptionsUrl: function(field, ajax_url, on_change) {
        var ajaxUrl = PropertyEditor.Util.replaceContextPath(ajax_url, field.options.contextPath);
        if (on_change !== undefined && on_change !== null) {
            var onChanges = on_change.split(";");
            var fields = field.editorObject.fields;
            if (field.parentId !== "" && field.parentId !== undefined) {
                var parentId = field.parentId.substring(1);
                if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                    fields = fields[parentId].fields;
                }
            }
            
            if (field.repeaterFields) {
                fields = $.extend({}, fields, field.repeaterFields);
            }
            for (var i in onChanges) {
                var fieldId = onChanges[i];
                var param = fieldId;
                var childField = "";
                if (fieldId.indexOf(":") !== -1) {
                    param = fieldId.substring(0, fieldId.indexOf(":"));
                    fieldId = fieldId.substring(fieldId.indexOf(":") + 1);
                }
                if (fieldId.indexOf(".") !== -1) {
                    childField = fieldId.substring(fieldId.indexOf(".") + 1);
                    fieldId = fieldId.substring(0, fieldId.indexOf("."));
                }

                if (ajaxUrl.indexOf('?') !== -1) {
                    ajaxUrl += "&";
                } else {
                    ajaxUrl += "?";
                }

                var targetField = fields[fieldId];
                var data = targetField.getData(true);
                var targetValue = data[fieldId];
                
                if (targetField.properties.type === "password") {
                    var wrapper = $('#' + targetField.id + '_input');
                    if (targetValue.indexOf("%%%%****SECURE_VALUE****-") === 0 && $(wrapper).find(".property-input-error.encrypted").length === 0) {
                        $(wrapper).append('<div class="property-input-error encrypted">'+get_peditor_msg("peditor.password")+'</div>');
                    } else {
                        $(wrapper).find(".property-input-error.encrypted").remove();
                    }
                }

                if (childField !== "") {
                    if ($.isArray(targetValue)) { //is grid
                        var values = [];
                        for (var j in targetValue) {
                            values.push(targetValue[j][childField]);
                        }
                        targetValue = values.join(";");
                    } else {
                        if (!targetField.isHidden()) {
                            //it is element select, simply validate the properties fields before make ajax call to prevent unnecessary call
                            if (childField === "className" && (targetValue === undefined || targetValue === null || targetValue[childField] === undefined || targetValue[childField] === null || targetValue[childField] === "")) {
                                return;
                            } else if (childField === "properties") {
                                try {
                                    if (targetField.pageOptions.propertiesDefinition !== undefined && targetField.pageOptions.propertiesDefinition !== null) {
                                        if (!$(targetField.editor).find(".anchor[anchorField=\"" + targetField.id + "\"]").hasClass("partialLoad")) {
                                            var errors = [];
                                            $.each(targetField.pageOptions.propertiesDefinition, function(i, page) {
                                                var p = page.propertyEditorObject;
                                                p.validate(targetValue[childField], errors, true);
                                            });
                                            if (errors.length > 0) {
                                                //there is required field leave empty, don't make the call until all field are filled.
                                                return;
                                            }
                                        }
                                    } else {
                                        //the element select field not ready yet, this call will trigger again later when it is ready.
                                        return;
                                    }
                                } catch (err) {
                                    //if error then don't make the ajax call
                                    return;
                                }
                            }

                            if (targetValue === null || targetValue === undefined || targetValue[childField] === null || targetValue[childField] === undefined) {
                                targetValue = "";
                            } else if ($.type(targetValue[childField]) === "string") {
                                targetValue = targetValue[childField];
                            } else {
                                targetValue = JSON.encode(targetValue[childField]);
                            }
                        } else {
                            targetValue = "";
                        }
                    }
                } else if (targetValue === null || targetValue === undefined) {
                    targetValue = "";
                }

                ajaxUrl += param + "=" + encodeURIComponent(targetValue);
            }
        }
        return ajaxUrl;
    },
    fieldOnChange: function(field, reference, ajax_url, on_change, mapping, method, extra) {
        var onChanges = on_change.split(";");
        var fieldIds = [];
        var fields = field.editorObject.fields;
        if (field.parentId !== "" && field.parentId !== undefined) {
            var parentId = field.parentId.substring(1);
            if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                fields = fields[parentId].fields;
            }
        }
        if (field.repeaterFields) {
            fields = $.extend({}, fields, field.repeaterFields);
        }
        for (var i in onChanges) {
            var fieldId = onChanges[i];
            if (fieldId.indexOf(":") !== -1) {
                fieldId = fieldId.substring(fieldId.indexOf(":") + 1);
            }
            if ($.inArray(fieldId, fieldIds) === -1) {
                fieldIds.push(fieldId);
            }
        }
        var referenceKey = "";
        if (reference !== undefined && reference !== null && reference !== "") {
            referenceKey = "__" + reference;
        }
        for (var i in fieldIds) {
            var selector = "";
            var fieldId = fieldIds[i];
            if (fieldId.indexOf(".") !== -1) {
                if (fieldId.indexOf(".properties") !== -1) {
                    selector = ".property-editor-page[elementid=\"" + fields[fieldId.substring(0, fieldId.indexOf("."))].id + "\"] .property-editor-property:not(.hidden) [name]";
                } else {
                    selector = "#" + fields[fieldId.substring(0, fieldId.indexOf("."))].id + " [name=\"" + fieldId.substring(fieldId.indexOf(".") + 1) + "\"]";
                    if ($(field.editor).find(selector).length === 0) {
                        selector = "[name=\"" + fields[fieldId.substring(0, fieldId.indexOf("."))].id + "\"]";
                    }
                }
            } else {
                selector = "[name=\"" + fields[fieldId].id + "\"]";
            }
            $(field.editor).off("change."+field.id+referenceKey, selector);
            $(field.editor).on("change."+field.id+referenceKey, selector, function() {
                //delay to make sure show/hide dynamic field is complete before make an ajax call when there is a change event 
                // (in case, the ajax call and the show/hide dynamic field is listen on same field)
                setTimeout(function(){
                    PropertyEditor.Util.callLoadOptionsAjax(field, reference, ajax_url, on_change, mapping, method, extra);
                }, 1);
            });
        }
    },
    getValueFromObject: function(obj, name) {
        if ($.type(obj) === "string") {
            return obj;
        }

        try {
            var parts = name.split(".");
            var value = null;
            if (parts[0] !== undefined && parts[0] !== "") {
                value = obj[parts[0]];
            }
            if (parts.length > 1) {
                for (var i = 1; i < parts.length; i++) {
                    value = value[parts[i]];
                }
            }

            return value;
        } catch (err) {};
        return null;
    },
    dynamicOptionsCheckValue: function(control, controlVal, isRegex) {
        if (control.isHidden()) {
            return false;
        }

        var data = control.getData(true);
        return PropertyEditor.Util.internalDynamicOptionsCheckValue(data, control.properties.name, controlVal, isRegex);
    },
    dynamicOptionsCheckValueMultiFields: function(element, page, controlFieldStr, controlValsStr, isRegex) {
        var control_fields = controlFieldStr.split(";");
        var controlVals = controlValsStr.split(";");
        
        var data = {};
        for (var i in control_fields) {
            var control_field = control_fields[i];
            var controlVal = controlVals[i];
            
            var field = null;
            if (page.editorObject !== undefined) {
                var fields = page.editorObject.fields;
                if (page.parentId !== "" && page.parentId !== undefined) {
                    var parentId = page.parentId.substring(1);
                    if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                        fields = fields[parentId].fields;
                    }
                }

                if (element.repeaterFields) {
                    fields = $.extend({}, fields, element.repeaterFields);
                }
                field = fields[control_field];
            } else if (page[control_field] !== undefined) {
                field = page[control_field];
            }
            if (field !== null && field !== undefined) {
                if (!field.isHidden()) {
                    data = $.extend(data, field.getData(true));
                }
            }
        }
        return PropertyEditor.Util.internalDynamicOptionsCheckValue(data, controlFieldStr, controlValsStr, isRegex);
    },
    internalDynamicOptionsCheckValue: function(data, nameStr, controlValStr, isRegex) {
        var names = nameStr.split(";");
        var controlVals = controlValStr.split(";");
        
        var checkResult = true;
        for (var j in names) {
            var r = false;
            var name = names[j];
            var controlVal = controlVals[j];
            
            var values = new Array();
            var value = data[name];

            if (value !== undefined && value !== null) {
                if (value["className"] !== undefined) {
                    values = [value["className"]];
                } else if (value.indexOf(";") !== -1 && $('<div>'+value+'</div>').find("*").length === 0) { //check is not html
                    values = value.split(";");
                } else {
                    values = [value];
                }
            }

            if (values.length === 0) {
                values.push("");
            }
        
            for (var i = 0; i < values.length; i++) {
                if (isRegex !== undefined && isRegex) {
                    var regex = new RegExp(controlVal);
                    var result = regex.exec(values[i]);
                    if ($.isArray(result)) {
                        if (result.indexOf(values[i]) !== -1) {
                            r = true;
                            break;
                        }
                    } else {
                        if (result === values[i]) {
                            r = true;
                            break;
                        }
                    }
                } else {
                    if (values[i] === controlVal) {
                        r = true;
                        break;
                    }
                }
            }
            checkResult = checkResult && r;
            if (!checkResult) {
                break;
            }
        }

        return checkResult;
    },
    supportHashField: function(field) {
        if (field.properties.supportHash !== undefined && field.properties.supportHash.toLowerCase() === "true") {
            var propertyInput = $("#" + field.id + "_input");
            propertyInput.append('<div class="hashField"><input type="text" id="' + field.id + '_hash" name="' + field.id + '_hash" size="50" value="' + PropertyEditor.Util.escapeHtmlTag(field.value) + '"/></div>');
            propertyInput.append("<a class=\"hashFieldAction\"><i class=\"fas fa-chevron-left\"></i><span>#</span><i class=\"fas fa-chevron-right\"></i></a>");

            if ($(propertyInput).find("div.default").length > 0) {
                propertyInput.append($(propertyInput).find("div.default"));
            }

            var toogleHashField = function() {
                if ($(propertyInput).hasClass("hash")) {
                    $(propertyInput).removeClass("hash");
                    $(propertyInput).find(".hashFieldAction").html("<i class=\"fas fa-chevron-left\"></i><span>#</span><i class=\"fas fa-chevron-right\"></i>");
                } else {
                    $(propertyInput).addClass("hash");
                    $(propertyInput).find(".hashFieldAction").html("<i class=\"fas fa-share\"></i>");
                }
            };

            if (field.options.propertyValues !== undefined && field.options.propertyValues !== null) {
                var hashFields = field.options.propertyValues['PROPERTIES_EDITOR_METAS_HASH_FIELD'];
                if (hashFields !== undefined && hashFields !== "") {
                    var hfs = hashFields.split(";");
                    for (var i in hfs) {
                        if (field.properties.name === hfs[i]) {
                            toogleHashField();
                            break;
                        }
                    }
                }

            }

            $(propertyInput).find(".hashFieldAction").off("click");
            $(propertyInput).find(".hashFieldAction").on("click", toogleHashField);
        }
    },
    retrieveHashFieldValue: function(field, data) {
        if (field.properties.supportHash !== undefined && field.properties.supportHash.toLowerCase() === "true") {
            var propertyInput = $("#" + field.id + "_input");
            if ($(propertyInput).hasClass("hash")) {
                var value = $('[name="' + field.id + '_hash"]:not(.hidden)').val();
                if (value === undefined || value === null || value === "") {
                    value = "";
                }
                value = value.trim();
                data[field.properties.name] = value;
                data['HASH_FIELD'] = field.properties.name;
            }
        }
    },
    showAjaxLoading: function(editor, field, reference) {
        var container = $("#" + field.id + "_input");
        if (reference == "CONTAINER") {
            container = $(container).parent();
        } else if (typeof(reference) !== "undefined") {
            container = $(container).find(".grid_model [name='" + reference + "']");
        }
        $(container).addClass("ajaxLoading");
        $(editor).find(".ajaxLoader").show();
    },
    removeAjaxLoading: function(editor, field, reference) {
        var container = $("#" + field.id + "_input");
        if (reference === "CONTAINER") {
            container = $(container).parent();
        } else if (typeof(reference) !== "undefined") {
            container = $(container).find(".grid_model [name='" + reference + "']");
        }
        $(container).removeClass("ajaxLoading");
        if ($(editor).find(".ajaxLoading").length === 0) {
            $(editor).find(".ajaxLoader").hide();
        }
    },
    getAppResources: function(field, callback) {
        if (PropertyEditor.Util.resources[field.properties.appPath] !== null && PropertyEditor.Util.resources[field.properties.appPath] !== undefined) {
            callback(PropertyEditor.Util.resources[field.properties.appPath]);
        } else {
            $.ajax({
                url: field.options.contextPath + "/web/property/json" + field.properties.appPath + "/getAppResources",
                dataType: "json",
                method: "GET",
                success: function(data) {
                    PropertyEditor.Util.resources[field.properties.appPath] = data;
                    callback(data);
                }
            });
        }
    },
    showAppResourcesDialog: function(field) {
        PropertyEditor.Util.getAppResources(field, function(resources) {
            var height = $(field.editor).height() * 0.95;
            var width = $(window).width() * 0.80;
            if (width > 800) {
                width = 800;
            }

            var html = "<div class=\"property_editor_app_resources\"><div id=\"app_resource_dropzone\" class=\"dropzone\"><div class=\"dz-message needsclick\">" + get_peditor_msg('peditor.dropfile') + "</div><div class=\"uploading\"></div></div><div class=\"search_field\"><i class=\"fas fa-search\"></i><input type=\"text\"/></div><ul class=\"app_resources\"></ul></div>";
            var object = $(html);

            var isPublic = "";
            if (field.properties.isPublic !== undefined && field.properties.isPublic !== null && field.properties.isPublic.toLowerCase() === "true") {
                isPublic = "?isPublic=true";
            }

            var options = {
                url: field.options.contextPath + "/web/property/json" + field.properties.appPath + "/appResourceUpload" + isPublic,
                paramName: 'app_resource',
                previewsContainer: ".property_editor_app_resources .uploading",
                previewTemplate: '<div><span class="name" data-dz-name></span><strong class="error text-danger" data-dz-errormessage></strong><div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="progress-bar progress-bar-success" style="width:0%;" data-dz-uploadprogress></div></div></div>',
                dictInvalidFileType: get_peditor_msg('peditor.invalidFileType'),
                dictFileTooBig: get_peditor_msg('peditor.fileTooBig')
            };

            var types = [];
            if (field.properties.allowType !== undefined && field.properties.allowType !== null && field.properties.allowType !== "") {
                options.acceptedFiles = field.properties.allowType.replace(/;/g, ',');
                types = field.properties.allowType.split(";");
            }

            if (field.properties.maxSize !== undefined && field.properties.maxSize !== null && field.properties.maxSize !== "") {
                try {
                    options.maxFilesize = parseInt(field.properties.maxSize) / 1024;
                } catch (err) {}
            }

            for (var r in resources) {
                var ar_container = $(object).find(".app_resources");

                var fileType = resources[r].value.substring(resources[r].value.indexOf("."));
                //check valid file type
                if (types.length > 0) {
                    var valid = false;
                    for (var t in types) {
                        if (fileType === types[t]) {
                            valid = true;
                            break;
                        }
                    }
                    if (!valid) {
                        continue;
                    }
                }

                if (!resources[r].value.match(/.(jpg|jpeg|png|gif)$/i)) {
                    ar_container.append("<li><div class=\"image\"><div class=\"ext\"><span>" + fileType.substring(1) + "</span></div></div><span class=\"name\">" + resources[r].value + "</span></li>");
                } else {
                    ar_container.append("<li><div class=\"image\" style=\"background-image:url('" + resources[r].url + "');\"></div><span class=\"name\">" + resources[r].value + "</span></li>");
                }
            }

            $(object).dialog({
                autoOpen: false,
                modal: true,
                height: height,
                width: width,
                closeText: '',
                close: function(event, ui) {
                    $(object).dialog("destroy");
                    $(object).remove();
                    $(field).focus();
                }
            });
            $(object).dialog("open");

            $(object).off("click", ".app_resources li");
            $(object).on("click", ".app_resources li", function() {
                field.selectResource($(this).find(".name").text());
                $(object).dialog("close");
            });

            $(object).find(".search_field input").off("keyup");
            $(object).find(".search_field input").on("keyup", function() {
                var text = $(this).val();
                if (text.length > 3) {
                    $(object).find(".app_resources li").each(function() {
                        if ($(this).find(".name").text().indexOf(text) === -1) {
                            $(this).hide();
                        } else {
                            $(this).show();
                        }
                    });
                } else if (text.length === 0) {
                    $(object).find(".app_resources li").show();
                }
            });
            
            options.timeout = 0;

            var myDropzone = new Dropzone("#app_resource_dropzone", options);
            myDropzone.on("success", function(file, resp) {
                resp = $.parseJSON(resp);
                var ar_container = $(object).find(".app_resources");

                //check existing and remove it
                for (var i in PropertyEditor.Util.resources[field.properties.appPath]) {
                    if (PropertyEditor.Util.resources[field.properties.appPath][i].value === resp.value) {
                        PropertyEditor.Util.resources[field.properties.appPath].splice(i, 1);

                        ar_container.find("li").each(function() {
                            if ($(this).find(".name").text() === resp.value) {
                                $(this).remove();
                            }
                        });
                    }
                }

                PropertyEditor.Util.resources[field.properties.appPath].unshift(resp);
                $(file.previewElement).remove();

                var fileType = resp.value.substring(resp.value.indexOf("."));
                if (!resp.value.match(/.(jpg|jpeg|png|gif)$/i)) {
                    ar_container.prepend("<li><div class=\"image\"><div class=\"ext\"><span>" + fileType.substring(1) + "</span></div></div><span class=\"name\">" + resp.value + "</span></li>");
                } else {
                    ar_container.prepend("<li><div class=\"image\" style=\"background-image:url('" + resp.url + "');\"></div><span class=\"name\">" + resp.value + "</span></li>");
                }
            });
            myDropzone.on("error", function(file, error) {
                setTimeout(function() {
                    $(object).find(".app_resources .error").remove();
                }, 8000);
            });

        });
    },
    /* used to replace jquery `$.when.apply($, deferreds).then` due to performance slowness. */
    deferredHandler : function(deferreds, callback) {
        var count = deferreds.length;
        if (count > 0) {
            for (var i in deferreds) {
                deferreds[i].always(function() {
                    count--;

                    if (count === 0) {
                        callback();
                    }
                });
            }
        } else {
            callback();
        }
    }
};

PropertyEditor.Model.Editor = function(element, options) {
    this.element = element;
    this.options = options;
    this.pages = {};
    this.fields = {};
    this.editorId = 'property_' + PropertyEditor.Util.uuid();
    this.saved = false;
    var simplecss = (options.simpleMode)?" simple":"";
    $(this.element).append('<div id="' + this.editorId + '" class="property-editor-container '+simplecss+'" style="position:relative;"><div class="ajaxLoader"><div class="loaderIcon"><i class="fas fa-spinner fa-spin fa-4x"></i></div></div><div class="property-editor-display" ><a class="compress" title="' + get_peditor_msg('peditor.compress') + '"><i class="fas fa-compress" aria-hidden="true"></i></a><a class="expand" title="' + get_peditor_msg('peditor.expand') + '"><i class="fas fa-expand" aria-hidden="true"></i></a></div><div class="property-editor-nav"></div><div class="property-editor-pages"></div><div class="property-editor-buttons"></div><div>');
    this.editor = $(this.element).find('div#' + this.editorId);
};
PropertyEditor.Model.Editor.prototype = {
    getData: function() {
        var properties = new Object();
        if (this.options.propertiesDefinition !== undefined && this.options.propertiesDefinition !== null) {
            $.each(this.options.propertiesDefinition, function(i, page) {
                var p = page.propertyEditorObject;
                properties = $.extend(properties, p.getData());
            });
        }
        return properties;
    },
    validation: function(successCallaback, failureCallback) {
        var thisObj = this;
        var errors = new Array();
        var data = this.getData();
        var deferreds = [];
        
        $(thisObj.editor).find(".property-page-has-errors").removeClass("property-page-has-errors");

        if (this.options.propertiesDefinition !== undefined && this.options.propertiesDefinition !== null) {
            $.each(this.options.propertiesDefinition, function(i, page) {
                var p = page.propertyEditorObject;
                var deffers = p.validate(data, errors, true);
                if (deffers !== null && deffers !== undefined && deffers.length > 0) {
                    deferreds = $.merge(deferreds, deffers);
                }
            });
        } else {
            var dummy = $.Deferred();
            deferreds.push(dummy);
            dummy.resolve();
        }

        PropertyEditor.Util.deferredHandler(deferreds, function() {
            if (errors.length > 0) {
                $(thisObj.editor).find(".property-input-error").closest(".property-editor-page").addClass("property-page-has-errors");
               
                failureCallback(errors);
            } else {
                successCallaback(data);
            }
        });
    },
    render: function() {
        var html = '';
        if (this.options.propertiesDefinition === undefined 
                || this.options.propertiesDefinition === null
                || this.options.propertiesDefinition.length === 0) {
            html += this.renderNoPropertyPage();
        } else {
            var editorObject = this;
            $.each(this.options.propertiesDefinition, function(i, page) {
                var p = page.propertyEditorObject;
                if (p === undefined) {
                    p = new PropertyEditor.Model.Page(editorObject, i, page);
                    page.propertyEditorObject = p;
                    editorObject.pages[p.id] = p;
                }
                html += p.render();
            });
        }
        html += '<div class="property-editor-page-buffer"></div>';
        $(this.editor).find(".property-editor-pages").append(html);
        
        this.initScripting();
    },
    renderNoPropertyPage: function() {
        var p = new PropertyEditor.Model.Page(this, 'no_property', { title: get_peditor_msg('peditor.noProperties') });
        this.pages[p.id] = p;

        this.options.propertiesDefinition = new Array();
        this.options.propertiesDefinition.push({
            'propertyEditorObject': p
        });

        return p.render();
    },
    initScripting: function() {
        var thisObject = this;

        if (this.options.propertiesDefinition !== undefined && this.options.propertiesDefinition !== null) {
            $.each(this.options.propertiesDefinition, function(i, page) {
                var p = page.propertyEditorObject;
                p.initScripting();
            });
        }

        this.adjustSize();
        this.initPage();

        if (this.options.showCancelButton && !this.options.isPopupDialog) {
            $(this.editor).keydown(function(e) {
                if (e.which === 27 && $(".property_editor_hashassit").length === 0) {
                    if (thisObject.isChange()) {
                        if (confirm(get_peditor_msg('peditor.confirmClose'))) {
                            thisObject.cancel();
                        }
                    } else {
                        thisObject.cancel();
                    }
                }
            });
        }
        
        if (this.options.simpleMode) {
            $(this.editor).off("change.simplemode");
            $(this.editor).on("change.simplemode", function() {
                if (thisObject.isChange()) {
                    thisObject.save();
                }
            });
        } else if (this.options.autoSave) {
            $(thisObject.editor).addClass("peautosave");
            $(thisObject.editor).css("z-index", "102");
            if ($(thisObject.editor).parent().find(".peautosaveblock").length === 0) {
                $(thisObject.editor).parent().prepend('<div class="peautosaveblock" style="position:fixed;top:0;bottom:0;left:0;right:0;z-index: 101;display:none;"></div>');
            }
            $(thisObject.editor).off("mouseenter mouseleave");
            $(thisObject.editor).on( "mouseenter", function() {
                $(thisObject.editor).addClass("pediting");
                $(thisObject.editor).parent().find(".peautosaveblock").show();
            }).on("mouseleave", function(event) {
                //check cursor position still within editor
                var e = event || window.event;
                e = jQuery.event.fix(e);
                
                var pageX = e.pageX;
                var pageY = e.pageY;
                
                var offset = $(thisObject.editor).offset();
                if (!(pageY < offset.top || pageY > (offset.top + $(thisObject.editor).height())
                        || pageX < offset.left || pageX > (offset.left + $(thisObject.editor).width()))) {
                    return;
                }
                
                if ($(thisObject.editor).hasClass("pediting") && thisObject.isChange()) {
                    thisObject.save();
                } else {
                    $(thisObject.editor).parent().find(".peautosaveblock").hide();
                }
                $(thisObject.editor).removeClass("pediting");
            });
        } else if ($(this.editor).hasClass("editor-panel-mode")) {
            $(this.editor).off("change.panelmode");
            $(this.editor).on("change.panelmode", "*", function() {
                //dynamic styling class
                setTimeout(function() {
                     thisObject.updateStylingClass();
                }, 50);
            });
            thisObject.updateStylingClass();
        }
        
        try {
            this.initialValues = this.getData();
        } catch (err) {
            this.initialValues = this.options.propertyValues;
        }
    },
    updateStylingClass: function() {
        if ($(this.editor).hasClass('editor-panel-mode')) {
            $(this.editor).find('.property-editor-property').removeClass("property-last");
            $(this.editor).find('.property-type-header, .property-plugin-selection, .property-type-elementmultiselect').each(function(){
                $(this).prevUntil('.property-type-header, .property-plugin-selection, .property-type-elementmultiselect', ":not(.hidden):first").addClass("property-last");
            });
            $(this.editor).find('.property-editor-property-container').each(function(){
                $(this).find('> .property-editor-property:not(.hidden):not(.property-type-hidden):first').addClass("property-first");
                $(this).find('> .property-editor-property:not(.hidden):not(.property-type-hidden):last').addClass("property-last");
            });
        }
    },
    adjustSize: function() {
        if (this.options.isPopupDialog) {
            var tempHeight = $(this.editor).height();
            $(this.editor).find(".property-editor-property-container").css("height", (tempHeight - 114) + "px");
        } else {
            //adjust height & width
            var tempHeight = $(window).height();
            if ($(this.element).hasClass("boxy-content")) {
                $(this.editor).css("width", "auto");
                tempHeight = tempHeight * 0.85;
            } else if ($(this.element).parent().attr('id') === "main-body-content") {
                $(this.editor).css("width", "auto");
                tempHeight = tempHeight - $(this.element).offset().top;
            } else if ($(this.element).hasClass("fixed-height")) {
                $(this.editor).css("width", "auto");
                tempHeight = $(this.element).height();
            } else {
                $(this.editor).css("width", "auto");
                tempHeight = tempHeight * 0.9 - $(this.element).offset().top;
            }
            if (this.options.adjustSize !== undefined) {
                tempHeight = this.options.adjustSize(tempHeight);
            }
            $(this.editor).css("height", (tempHeight - 25) + "px");
            $(this.editor).find(".property-editor-property-container").css("height", (tempHeight - 139) + "px");
        }
    },
    initPage: function() {
        var $thisObject = this;

        var pageContainer = $(this.editor).find('.property-editor-pages');
        $(pageContainer).scroll(function() {
            if ($thisObject.isSinglePageDisplay()) {
                var pageLine = $(pageContainer).offset().top + ($(pageContainer).height() * 0.3);
                var currentOffset = $(pageContainer).find('.current').offset().top;
                var nextOffset = currentOffset + $(pageContainer).find('.current').height();
                if (nextOffset < pageLine) {
                    $thisObject.nextPage(false, false);
                } else if (currentOffset > pageLine) {
                    $thisObject.prevPage(false, false);
                }
            }
        });

        this.initDisplayMode();

        this.changePage(null, $(this.editor).find('.property-page-show:first').attr("id"));
    },
    initDisplayMode: function() {
        if (!$(this.editor).hasClass("simple")) {
            var $thisObject = this;

            //init display mode based on cookies value
            var single = $.localStorage.getItem("propertyEditor.singlePageDisplay");
            if (single === "true" || ($thisObject.options.editorPanelMode !== undefined && $thisObject.options.editorPanelMode === true)) {
                this.toggleSinglePageDisplay(true);
                $(this.editor).find(".property-page-show").each(function(i){
                    if (i > 0) {
                        $(this).addClass("collapsed");
                    }
                });
            }

            $(this.editor).find('.property-editor-display a').click(function() {
                $thisObject.toggleSinglePageDisplay();
            });
        }
    },
    toggleSinglePageDisplay: function(single) {
        if ((single || !this.isSinglePageDisplay()) && !$(this.editor).hasClass("simple")) {
            $(this.editor).addClass("single-page");
            single = true;
            if ($(this.editor).find('.property-page-show.current').length > 0) {
                this.changePageCallback($(this.editor).find('.property-page-show.current').attr("id"), false);
            }
        } else {
            $(this.editor).removeClass("single-page");
            single = false;
        }

        //store display mode to cookies
        if (!(this.options.editorPanelMode !== undefined && this.options.editorPanelMode === true)) {
            $.localStorage.setItem("propertyEditor.singlePageDisplay", single + "");
        } else {
            $(this.editor).addClass("editor-panel-mode narrow");
        }
    },
    isSinglePageDisplay: function() {
        return $(this.editor).hasClass("single-page");
    },
    nextPage: function(scroll, changeFocus) {
        if ($(this.editor).find('.property-page-show.current').length > 0) {
            var current = $(this.editor).find('.property-page-show.current');
            var next = $(current).next();
            while (!$(next).hasClass("property-page-show") && $(next).hasClass("property-editor-page")) {
                next = $(next).next();
            }
            if ($(next).hasClass("property-editor-page")) {
                this.changePage($(current).attr('id'), $(next).attr('id'), scroll, changeFocus);
            }
        }
    },
    prevPage: function(scroll, changeFocus) {
        if ($(this.editor).find('.property-page-show.current').length > 0) {
            var current = $(this.editor).find('.property-page-show.current');
            var prev = $(current).prev();
            while (!$(prev).hasClass("property-page-show") && $(prev).hasClass("property-editor-page")) {
                prev = $(prev).prev();
            }
            if ($(prev).hasClass("property-editor-page")) {
                this.changePage($(current).attr('id'), $(prev).attr('id'), scroll, changeFocus);
            }
        }
    },
    changePage: function(currentPageId, pageId, scroll, changeFocus) {
        var thisObject = this;
        if (!this.isSinglePageDisplay() && currentPageId !== null && currentPageId !== undefined) {
            this.pages[currentPageId].validation(function(data) {
                thisObject.changePageCallback(pageId, scroll, changeFocus);
            }, thisObject.alertValidationErrors);
        } else {
            this.changePageCallback(pageId, scroll, changeFocus);
        }
    },
    changePageCallback: function(pageId, scroll, changeFocus) {
        $(this.editor).find('.property-page-hide, .property-type-hidden, .property-page-show').hide();
        $(this.editor).find('.property-page-show').removeClass("current");
        this.pages[pageId].show(scroll, changeFocus);
    },
    refresh: function() {
        $(this.editor).find('.property-page-hide, .property-type-hidden, .property-page-show:not(.current)').hide();
        if ($(this.editor).find('.property-page-show.current').length > 0) {
            var current = $(this.editor).find('.property-page-show.current');
            var pageId = $(current).attr('id');
            this.pages[pageId].show(false, false);
        }
        this.adjustSize();
    },
    alertValidationErrors: function(errors) {
        var errorMsg = '';
        for (key in errors) {
            if (errors[key].fieldName !== '' && errors[key].fieldName !== null) {
                errorMsg += errors[key].fieldName + ' : ';
            }
            errorMsg += errors[key].message + '\n';
        }
        alert(errorMsg);
    },
    isChange: function() {
        try {
            var test =  !PropertyEditor.Util.deepEquals(this, this.getData(), this.initialValues);
            return test;
        } catch (err) {
            //error caused by editor not loaded fully
            return false;
        }
    },
    save: function() {
        var thisObj = this;
        if (this.options.skipValidation || (this.options.propertiesDefinition === undefined || this.options.propertiesDefinition === null)) {
            this.saveCallback(this.getData());
        } else {
            var thisObj = this;
            this.validation(function(data) {
                if ($.isFunction(thisObj.options.customSaveValidation)) {
                    thisObj.options.customSaveValidation(thisObj.element, data, function(){
                        thisObj.saveCallback(data);
                    });
                } else {
                    thisObj.saveCallback(data);
                }
            }, function(errors) {
                thisObj.saveFailureCallback(errors);
            });
        }
    },
    saveCallback: function(data) {
        this.saved = true;
        if (this.options.closeAfterSaved && !this.options.isPopupDialog) {
            $(this.editor).remove();
        }
        
        if (this.options.autoSave) { 
            this.options.propertyValues = data;
            $(this.editor).parent().find(".peautosaveblock").hide();
        }

        if ($.isFunction(this.options.saveCallback)) {
            this.options.saveCallback(this.element, data);
        }

        if (this.options.closeAfterSaved && !this.options.isPopupDialog) {
            this.clear();
        }
        this.initialValues = data;
    },
    saveFailureCallback: function(errors) {
        var thisObj = this;
        $(this.editor).find('.property-page-show').each(function() {
            if ($(this).find('.property-input-error').length > 0) {
                var errorPage = $(this);
                thisObj.changePage(null, $(errorPage).attr("id"));
                var errorField = $(errorPage).find('.property-input-error:first').parent();
                if ($(errorField).find("td.error").length > 0) {
                    $(errorField).find("td.error:first input, td.error:first select").focus();
                } else {
                    $(errorField).find('input, select, textarea').focus();
                }
                return false;
            }
        });

        if ($.isFunction(this.options.validationFailedCallback)) {
            this.options.validationFailedCallback(this.element, errors);
        }
    },
    cancel: function() {
        if (!this.options.isPopupDialog) {
            $(this.editor).remove();
        }
        if ($.isFunction(this.options.cancelCallback)) {
            this.options.cancelCallback(this.element);
        }
        if (!this.options.isPopupDialog) {
            this.clear();
        }
    },
    clear: function() {
        this.element = null;
        this.options = null;
        this.editorId = null;
        this.editor = null;
        this.pages = null;
    }
};

PropertyEditor.Model.Page = function(editorObject, number, properties, elementData, parentId) {
    this.editor = editorObject.editor;
    this.editorId = editorObject.editorId;
    this.options = editorObject.options;
    this.editorObject = editorObject;
    this.number = number;
    this.properties = properties;
    this.elementData = typeof elementData !== 'undefined' ? elementData : "";
    this.parentId = typeof parentId !== 'undefined' ? ("_" + parentId) : "";
    this.id = this.editorId + this.parentId + '_' + 'page_' + this.number;
    this.buttonPanel = new PropertyEditor.Model.ButtonPanel(this);
};
PropertyEditor.Model.Page.prototype = {
    isHidden: function() {
        return $(this.editor).find("#" + this.id).hasClass("hidden");
    },
    getData: function(pageProperties) {
        if (this.isHidden()) {
            return pageProperties;
        }
        var useDefault = false;
        if (pageProperties === undefined || pageProperties === null) {
            pageProperties = this.properties.properties;
        } else {
            useDefault = true;
        }

        var properties = new Object();
        if (pageProperties !== undefined) {
            $.each(pageProperties, function(i, property) {
                var type = property.propertyEditorObject;

                if (!type.isHidden()) {
                    var data = type.getData(useDefault);

                    //handle Hash Field
                    if (data !== null && data['HASH_FIELD'] !== null && data['HASH_FIELD'] !== undefined) {
                        if (properties['PROPERTIES_EDITOR_METAS_HASH_FIELD'] === undefined) {
                            properties['PROPERTIES_EDITOR_METAS_HASH_FIELD'] = data['HASH_FIELD'];
                        } else {
                            properties['PROPERTIES_EDITOR_METAS_HASH_FIELD'] += ";" + data['HASH_FIELD'];
                        }
                        delete data['HASH_FIELD'];
                    }

                    if (data !== null) {
                        properties = $.extend(properties, data);
                    }
                }
            });
        }
        return properties;
    },
    validate: function(data, errors, depthValidation, pageProperties) {
        var thisObj = this;
        var deferreds = [];
        var checkEncryption = false;

        //remove previous error message
        $("#" + this.id + " .property-input-error").remove();
        $("#" + this.id + " .property-editor-page-errors").remove();

        if (!this.isHidden()) {
            if (pageProperties === undefined || pageProperties === null) {
                pageProperties = this.properties.properties;

                if (this.properties.validators !== null && this.properties.validators !== undefined) {
                    $.each(this.properties.validators, function(i, property) {
                        var validator = property.propertyEditorObject;
                        if (validator === undefined) {
                            validator = PropertyEditor.Util.getValidatorObject(thisObj, property);
                            property.propertyEditorObject = validator;
                        }
                        var deffers = validator.validate(data, errors);
                        if (deffers !== null && deffers !== undefined && deffers.length > 0) {
                            deferreds = $.merge(deferreds, deffers);
                        }
                    });
                }
            } else {
                checkEncryption = true;
            }

            if (depthValidation && pageProperties !== undefined && pageProperties !== null) {
                $.each(pageProperties, function(i, property) {
                    var type = property.propertyEditorObject;
                    if (!type.isHidden()) {
                        var deffers = type.validate(data, errors, checkEncryption);
                        if (deffers !== null && deffers !== undefined && deffers.length > 0) {
                            deferreds = $.merge(deferreds, deffers);
                        }
                    }
                });
            }
        }

        return deferreds;
    },
    validation: function(successCallback, failureCallback, depthValidation, pageProperties) {
        var errors = [];
        var data = this.getData(pageProperties);
        var deferreds = [];

        deferreds = $.merge(deferreds, this.validate(data, errors, depthValidation, pageProperties));

        if (deferreds.length === 0) {
            var dummy = $.Deferred();
            deferreds.push(dummy);
            dummy.resolve();
        }

        PropertyEditor.Util.deferredHandler(deferreds, function() {
            if (errors.length > 0) {
                failureCallback(errors);
            } else if (successCallback !== undefined && successCallback !== null) {
                successCallback(data);
            }
        });
    },
    render: function() {
        var hiddenClass = " property-page-show";
        var pageTitle = '';

        if (this.properties.hidden !== undefined && this.properties.hidden.toLowerCase() === "true") {
            hiddenClass = " property-page-hide";
        }
        
        if (this.properties.viewport !== undefined) {
            hiddenClass += " " + this.properties.viewport;
        }

        var showHide = "";
        if (this.properties.control_field !== undefined && this.properties.control_field !== null &&
            this.properties.control_value !== undefined && this.properties.control_value !== null) {
            showHide = 'data-control_id="'+this.id+'" data-control_field="' + this.properties.control_field + '" data-control_value="' + this.properties.control_value + '"';

            if (this.properties.control_use_regex !== undefined && this.properties.control_use_regex.toLowerCase() === "true") {
                showHide += ' data-control_use_regex="true"';
            } else {
                showHide += ' data-control_use_regex="false"';
            }
        }
        if (this.properties.developer_mode !== undefined && this.properties.developer_mode !== null) {
            var modes = this.properties.developer_mode.split(";");
            for (var i in modes) {
                if (modes[i] !== "") {
                    hiddenClass += " "+modes[i]+"-mode-only";
                }
            }
        }

        if (this.properties.title !== undefined && this.properties.title !== null) {
            pageTitle = this.properties.title;
        }

        if (this.properties.properties === undefined) {
            hiddenClass += " no-property-page";
            pageTitle = get_peditor_msg('peditor.noProperties');
        }

        var helplink = "";
        if (this.properties.helplink !== undefined && this.properties.helplink !== "") {
            helplink = ' <a class="helplink" target="_blank" href="' + this.properties.helplink + '"><i class="fas fa-question-circle"></i></a>'
        }

        var html = '<div id="' + this.id + '" ' + this.elementData + 'class="property-editor-page' + hiddenClass + '" ' + showHide + '>';
        html += '<div class="property-editor-page-title"><span>' + pageTitle + '</span>' + helplink + '</div><div class="property-editor-page-step-indicator"></div><div class="property-editor-property-container">';

        html += this.renderProperties();

        html += '<div class="property-editor-page-buffer"></div></div>' + this.buttonPanel.render() + '</div>';

        return html;
    },
    renderProperties: function() {
        var html = "";
        if (this.properties.properties !== undefined) {
            var page = this;
            $.each(this.properties.properties, function(i, property) {
                html += page.renderProperty(i, "", property);
            });
        }
        return html;
    },
    renderProperty: function(i, prefix, property) {
        var type = property.propertyEditorObject;

        if (type === undefined) {
            var value = null;
            if (this.options.propertyValues !== null && this.options.propertyValues !== undefined && this.options.propertyValues[property.name] !== undefined) {
                value = this.options.propertyValues[property.name];
            } else if ((this.options.propertyValues === null || this.options.propertyValues === undefined) && property.value !== undefined && property.value !== null) {
                value = property.value;
            }

            var defaultValue = null;

            if (this.options.defaultPropertyValues !== null && this.options.defaultPropertyValues !== undefined && this.options.defaultPropertyValues[property.name] !== undefined &&
                this.options.defaultPropertyValues[property.name] !== "") {
                defaultValue = this.options.defaultPropertyValues[property.name];
            }

            type = PropertyEditor.Util.getTypeObject(this, i, prefix, property, value, defaultValue);
            property.propertyEditorObject = type;

            if (prefix === "" || prefix === null || prefix === undefined) {
                var fields = this.editorObject.fields;
                if (this.parentId !== "" && this.parentId !== undefined) {
                    var parentId = this.parentId.substring(1);
                    if (fields[parentId] === undefined) {
                        fields[parentId] = {};
                    }
                    if (fields[parentId].fields === undefined) {
                        fields[parentId].fields = [];
                    }
                    fields = fields[parentId].fields;
                }
                fields[property.name] = type;
            }
        }

        if (type !== null) {
            return type.render();
        }
        return "";
    },
    initScripting: function() {
        if (this.properties.properties !== undefined) {
            $.each(this.properties.properties, function(i, property) {
                var type = property.propertyEditorObject;
                type.initScripting();
                type.initDefaultScripting();
            });
        }
        PropertyEditor.Util.handleDynamicOptionsField(this);
        
        $(this.editor).find("#" + this.id + " .property-editor-page-title").off("click.collapsible");
        $(this.editor).find("#" + this.id + " .property-editor-page-title").on("click.collapsible", function(){
            $(this).parent().toggleClass("collapsed");
        });

        this.buttonPanel.initScripting();
        this.attachDescriptionEvent();
    },
    show: function(scroll, changeFocus) {
        var page = $(this.editor).find("#" + this.id);
        $(page).show();
        if (this.editorObject.isSinglePageDisplay()) {
            if (scroll === undefined || scroll) {
                var pages = $(this.editor).find('.property-editor-pages');
                var pos = $(page).offset().top - $(pages).offset().top - 50 - ($(pages).find(' > div:eq(0)').offset().top - $(pages).offset().top - 50);
                $(this.editor).find('.property-editor-pages').scrollTo(pos, 200);
            }
        }

        $(page).addClass("current");
        this.refreshStepsIndicator();
        this.buttonPanel.refresh();

        if (this.properties.properties !== undefined) {
            $.each(this.properties.properties, function(i, property) {
                var type = property.propertyEditorObject;
                type.pageShown();
            });
        }
        if ((changeFocus === undefined || changeFocus) && !UI.isMobileUserAgent()) {
            var fields = $(page).find('.property-editor-property-container .property-editor-property .property-input').find('input:not(:hidden), select, textarea');
            if (fields.length > 0) {
                fields[0].focus();
            }
        }
    },
    remove: function() {
        var page = $(this.editor).find("#" + this.id);
        PropertyEditor.Util.unhandleDynamicOptionsField(this);

        if (this.properties.properties !== undefined) {
            $.each(this.properties.properties, function(i, property) {
                var type = property.propertyEditorObject;
                type.remove();
            });
        }

        $(page).remove();
    },
    refreshStepsIndicator: function() {
        if ((this.editorObject.isSinglePageDisplay() && $(this.editor).find('.property-page-show').length > 0) ||
            (!this.editorObject.isSinglePageDisplay() && ($(this.editor).find('.property-page-show').length > 1 || $(this.editor).find('.property-editor-page-step-indicator .step').length > 1))) {
            var thisObject = this;
            var editor = this.editor;
            var currentPage = $(editor).find(".property-page-show.current");
            var currentPageParentElementId = $(currentPage).attr("elementid");
            if ($(currentPage).attr("parentelementid") !== undefined && $(currentPage).attr("parentelementid") !== "") {
                currentPageParentElementId = $(currentPage).attr("parentelementid");
            }
            var prev = null;
            var html = '';

            $(this.editor).find('.property-page-show').each(function(i) {
                var pageId = $(this).attr("id");
                var parentElementId = $(this).attr("elementid");
                if ($(this).attr("parentelementid") !== undefined && $(this).attr("parentelementid") !== "") {
                    parentElementId = $(this).attr("parentelementid");
                }

                if (prev !== null && prev !== parentElementId && currentPageParentElementId !== prev) {
                    html += ' <span class="seperator">' + get_peditor_msg('peditor.stepSeperator') + '</span> ';
                }

                if (parentElementId === undefined || currentPageParentElementId === parentElementId) {
                    prev = null;
                    var childPageClass = "";

                    if (parentElementId !== undefined && currentPageParentElementId === parentElementId) {
                        childPageClass = " childPage";
                    }

                    if ($(this).hasClass("current")) {
                        html += '<span class="step active' + childPageClass + '">';
                    } else {
                        html += '<span class="step clickable' + childPageClass + '" rel="' + pageId + '" style="cursor:pointer">';
                    }
                    html += $(this).find('.property-editor-page-title span').html() + '</span>';

                    if (i < $(editor).find('.property-page-show').length - 1) {
                        html += ' <span class="seperator">' + get_peditor_msg('peditor.stepSeperator') + '</span> ';
                    }
                } else {
                    var value = $("#" + parentElementId).val();
                    var valueLabel = $("#" + parentElementId).find('option[value="' + value + '"]').text();
                    var label = $("#" + parentElementId).closest(".property-editor-property").find(".property-label-container .property-label")
                        .clone().children().remove().end().text();

                    if (prev !== parentElementId) {
                        if ($(this).hasClass("current")) {
                            html += '<span class="step active">';
                        } else {
                            html += '<span class="step clickable" rel="' + pageId + '" style="cursor:pointer">';
                        }
                        html += label + " (" + valueLabel + ')</span>';
                    }
                    prev = parentElementId;
                }
            });
            html += '<div style="clear:both;"></div>';

            $(this.editor).find('#' + this.id + ' .property-editor-page-step-indicator').html(html);
            $(this.editor).find('#' + this.id + ' .property-editor-page-step-indicator .clickable').click(function() {
                thisObject.editorObject.changePage($(currentPage).attr("id"), $(this).attr("rel"));
            });

            if (this.editorObject.isSinglePageDisplay()) {
                $(this.editor).find('.property-editor-nav').html('');
                $(this.editor).find('.property-editor-nav').append($(this.editor).find('#' + this.id + ' .property-editor-page-step-indicator').clone(true));
            }
        }
    },
    attachDescriptionEvent: function() {
        var thisObj = this;
        
        $(this.editor).find(".property-label-description").each(function(){
            if (!$(this).hasClass("tooltipstered")) {
                $(this).tooltipster({
                    contentCloning: false,
                    side : 'right',
                    interactive : true
                });
            }
        });
    }
};

PropertyEditor.Model.ButtonPanel = function(page) {
    this.page = page;
    this.pageId = page.id;
    this.options = page.options;
    this.editor = page.editor;
};
PropertyEditor.Model.ButtonPanel.prototype = {
    render: function() {
        var html = "";
        var customButtons = this.renderPageButtons('btn btn-secondary btn-sm');
        if ($(this.editor).hasClass("editor-panel-mode") || this.options.editorPanelMode) { //for builder properties panel
            if (customButtons !== "") {
                customButtons = '<div class="property-editor-page-buttons">' + customButtons +'<div style="clear:both"></div></div>';
                html += customButtons;
            }
        } 
        
        html += '<div class="property-editor-page-button-panel">';
        html += '<div class="page-button-navigation">';
        html += '<input type="button" class="page-button-prev" value="' + this.options.previousPageButtonLabel + '"/>';
        html += '<input type="button" class="page-button-next" value="' + this.options.nextPageButtonLabel + '"/>';
        html += '</div><div class="page-button-action">';
        
        if (!($(this.editor).hasClass("editor-panel-mode") || this.options.editorPanelMode)) {
            html += customButtons;
        }

        if (!this.options.autoSave) {
            html += '<input type="button" class="page-button-save" value="' + this.options.saveButtonLabel + '"/>';
        }
        if (this.options.showCancelButton) {
            html += '<input type="button" class="page-button-cancel" value="' + this.options.cancelButtonLabel + '"/>';
        }
        html += '</div><div style="clear:both"></div></div>';
        
        return html;
    },
    //render the additional buttons & popup form for buttons
    renderPageButtons: function(css) {
        var page = this.page;
        var thisObj = this;
        var html = "";
        if (css === undefined || css === null) {
            css = "";
        }
        if (page.properties.buttons !== undefined && page.properties.buttons !== null) {
            $.each(page.properties.buttons, function(i, button) {
                var showHide = "";

                if (button.control_field !== undefined && button.control_field !== null && button.control_value !== undefined && button.control_value !== null) {
                    showHide = 'data-control_id="'+page.id + '_' + button.name+'" data-control_field="' + button.control_field + '" data-control_value="' + button.control_value + '"';

                    if (button.control_use_regex !== undefined && button.control_use_regex.toLowerCase() === "true") {
                        showHide += ' data-control_use_regex="true"';
                    } else {
                        showHide += ' data-control_use_regex="false"';
                    }
                }

                var buttonAttrs = "";
                if (button.ajax_url !== undefined) {
                     if (button.ajax_method === undefined) {
                        button.ajax_method = "GET";
                    }

                    buttonAttrs = 'data-ajax_url="' + button.ajax_url + '" data-ajax_method="' + button.ajax_method + '"';
                } else if (button.callback !== undefined) {
                    buttonAttrs = 'data-callback="' + button.callback + '"';
                }

                html += '<input id="' + page.id + '_' + button.name + '" type="button" class="page-button-custom '+css+'" value="' + button.label + '" ' + buttonAttrs +' data-action="' + button.name + '" ' + showHide + ' />';
                if (button.addition_fields !== undefined && button.addition_fields !== null) {
                    html += '<div id="' + page.id + '_' + button.name + '_form" class="button_form '+(($(thisObj.editor).hasClass("editor-panel-mode") || thisObj.options.editorPanelMode)?'single-page property-editor-container editor-panel-mode':'')+'" style="display:none;">';
                    html += '<div id="main-body-header" style="margin-bottom:15px;">' + button.label + '</div>';
                    $.each(button.addition_fields, function(i, property) {
                        html += page.renderProperty(i, button.name, property);
                    });
                    html += '</div>';
                }
            });
        }
        return html;
    },
    initScripting: function() {
        var currentPage = $(this.editor).find("#" + this.pageId);
        var page = this.page;
        var panel = this;
        $(currentPage).find('input.page-button-next').click(function() {
            page.editorObject.nextPage();
        });

        //previous page event
        $(currentPage).find('input.page-button-prev').click(function() {
            page.editorObject.prevPage();
        });

        //save event
        $(currentPage).find('input.page-button-save').click(function() {
            page.editorObject.save();
        });

        //cancel event
        $(currentPage).find('input.page-button-cancel').click(function() {
            page.editorObject.cancel();
        });

        //custom page button
        $(currentPage).find('.page-button-custom').click(function() {
            var button = $(this);
            var id = $(button).attr("id");

            //get properties
            var buttonProperties;
            $.each(page.properties.buttons, function(i, buttonProp) {
                if (buttonProp.name === $(button).data("action")) {
                    buttonProperties = buttonProp;
                }
            });
            
            if (buttonProperties.allFields !== undefined && buttonProperties.allFields === "true") {
                var deferreds = [];
                var errors = [];
                var data = {};
                
                var elementId = $(currentPage).attr("elementid");
                if (elementId !== null && elementId !== undefined) {
                    var propertyName = $(panel.editor).find("#" + elementId).closest(".property-editor-property").attr("property-name");
                    var parentId = $(panel.editor).find("#" + elementId).closest(".property-editor-property").attr("property-parentid");
                    
                    var fields = page.editorObject.fields;
                    if (parentId !== "" && parentId !== undefined && fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                        fields = fields[parentId].fields;
                    }
                    
                    var elementSelect = fields[propertyName];
                    
                    var eldata = elementSelect.getData(true);
                    data = eldata[propertyName]["properties"];
                    deferreds = elementSelect.validate(eldata, errors, true);
                } else {
                    data = page.editorObject.getData();
                    $.each(panel.options.propertiesDefinition, function(i, page) {
                        var p = page.propertyEditorObject;
                        var deffers = p.validate(data, errors, true);
                        if (deffers !== null && deffers !== undefined && deffers.length > 0) {
                            deferreds = $.merge(deferreds, deffers);
                        }
                    });
                }
                
                PropertyEditor.Util.deferredHandler(deferreds, function() {
                    if (errors.length > 0) {
                        page.editorObject.alertValidationErrors(errors);
                    } else {
                        //popup form for extra input
                        if ($("#" + id + "_form").length === 1) {
                            var object = $("#" + id + "_form");
                            $(object).dialog({
                                modal: true,
                                width: "70%",
                                closeText: '',
                                buttons: [{
                                    "text" : $(button).val(),
                                    "class" : "btn btn-primary",
                                    "click" : function() {
                                        page.validation(function(addition_data) {
                                            data = $.extend(data, addition_data);
                                            panel.executeButtonEvent(data, $(button));
                                            $(object).dialog("close");
                                        }, function(errors) {}, true, buttonProperties.addition_fields);
                                    }
                                }],
                                close: function(event, ui) {
                                    $(object).dialog("destroy");
                                }
                            });
                        } else {
                            panel.executeButtonEvent(data, $(button));
                        }
                    }
                });
            } else {
                var pageProperties = page.properties.properties;
                pageProperties = $.grep(pageProperties, function(property) {
                    if (buttonProperties.fields !== undefined && buttonProperties.fields.indexOf(property.name) !== -1) {
                        if (buttonProperties.require_fields !== undefined && buttonProperties.require_fields.indexOf(property.name) !== -1) {
                            property.required = "true";
                        }
                        return true;
                    }
                    return false;
                });

                page.validation(function(data) {
                    //popup form for extra input
                    if ($("#" + id + "_form").length === 1) {
                        var object = $("#" + id + "_form");
                        $(object).dialog({
                            modal: true,
                            width: "70%",
                            closeText: '',
                            buttons: [{
                                "text" : $(button).val(),
                                "class" : "btn btn-primary",
                                "click" : function() {
                                    page.validation(function(addition_data) {
                                        data = $.extend(data, addition_data);
                                        panel.executeButtonEvent(data, $(button));
                                        $(object).dialog("close");
                                    }, function(errors) {}, true, buttonProperties.addition_fields);
                                }
                            }],
                            close: function(event, ui) {
                                $(object).dialog("destroy");
                            }
                        });
                    } else {
                        panel.executeButtonEvent(data, $(button));
                    }
                }, function(errors) {}, true, pageProperties);
            }
            return false;
        });
    },
    executeButtonEvent: function(data, button) {
        var url = $(button).data("ajax_url");
        var callback = $(button).data("callback");
        if (url !== null && url !== undefined && url !== "") {
            var method = $(button).data("ajax_method");
            $.each(data, function(i, d) {
                if (d.indexOf("%%%%") !== -1 && d.substring(0, 4) === "%%%%" && d.substring(d.length - 4) === "%%%%") {
                    data[i] = d.replace(/%%%%/g, "");
                }
            });

            $.ajax({
                method: method,
                url: PropertyEditor.Util.replaceContextPath(url, this.options.contextPath),
                data: $.param(data),
                dataType: "text",
                success: function(response) {
                    var r = $.parseJSON(response);

                    if (r.message !== undefined && r.message !== null) {
                        alert(r.message);
                    }
                }
            });
        } else if (callback !== null && callback !== undefined && callback !== "") {
            var callbackFunc = PropertyEditor.Util.getFunction(callback);
            if (callbackFunc !== null) {
                var message = callbackFunc(data);
                if (message !== undefined && message !== null) {
                    alert(message);
                }
            }
        }
    },
    refresh: function() {
        if ($(this.editor).find('.property-page-show').length === 1) {
            $(this.editor).find('.property-page-show .property-editor-page-button-panel .page-button-navigation').hide();
        } else {
            $(this.editor).find('.property-page-show .property-editor-page-button-panel .page-button-navigation').show();
            $(this.editor).find('.property-page-show .property-editor-page-button-panel .page-button-navigation input[type=button]').removeAttr("disabled");
            $(this.editor).find('.property-page-show:first .property-editor-page-button-panel .page-button-navigation .page-button-prev').attr("disabled", "disabled");
            $(this.editor).find('.property-page-show:last .property-editor-page-button-panel .page-button-navigation .page-button-next').attr("disabled", "disabled");
        }

        if (this.page.editorObject.isSinglePageDisplay()) {
            $(this.editor).find('.property-editor-buttons').html('');
            var buttonPanel = $(this.editor).find('.property-page-show.current .property-editor-page-button-panel').clone(true);
            $(buttonPanel).find(".button_form").remove();
            $(this.editor).find('.property-editor-buttons').append(buttonPanel);
        }
    }
};

PropertyEditor.Model.Validator = function(page, properties) {
    this.page = page;
    this.editorObject = this.page.editorObject;
    this.editor = this.page.editor;
    this.properties = properties;
    this.options = this.page.options;
};
PropertyEditor.Model.Validator.prototype = {
    initialize: function() {},
    validate: function(data, errors) {}
};

PropertyEditor.Validator.Ajax = function() {};
PropertyEditor.Validator.Ajax.prototype = {
    shortname: "ajax",
    validate: function(data, errors) {
        var thisObject = this;
        var deffers = [];
        var d = $.Deferred();
        deffers.push(d);

        var temp = $.extend({}, data);
        if (thisObject.options.defaultPropertyValues !== null && thisObject.options.defaultPropertyValues !== undefined) {
            for (var t in temp) {
                if (temp[t] === "" && thisObject.options.defaultPropertyValues[t] !== undefined) {
                    temp[t] = thisObject.options.defaultPropertyValues[t];
                }
            }
        }

        var method = "GET";
        if (thisObject.properties.method !== undefined && thisObject.properties.method !== "") {
            method = thisObject.properties.method;
        }

        $.ajax({
            url: PropertyEditor.Util.replaceContextPath(this.properties.url, thisObject.options.contextPath),
            data: $.param(temp),
            dataType: "text",
            method: method.toUpperCase(),
            success: function(response) {
                var r = $.parseJSON(response);
                var errorsHtml = "";
                if (r.status.toLowerCase() === "fail") {
                    if (r.message.length === 0) {
                        var obj = new Object();
                        obj.fieldName = '';
                        obj.message = thisObject.properties.default_error_message;

                        errors.push(obj);
                        errorsHtml += '<div class="property-input-error">' + obj.message + '</div>';
                    } else {
                        for (i in r.message) {
                            var obj2 = new Object();
                            obj2.fieldName = '';
                            obj2.message = r.message[i];

                            errors.push(obj2);
                            errorsHtml += '<div class="property-input-error">' + obj2.message + '</div>';
                        }
                    }
                }

                if (errorsHtml !== "") {
                    var page = $(thisObject.editor).find('#' + thisObject.page.id);
                    var errorContainer;
                    if ($(page).find(".property-editor-page-errors").length === 0) {
                        $(page).find('.property-editor-property-container').prepend('<div class="property-editor-page-errors"></div>');
                    }
                    var errorContainer = $(page).find(".property-editor-page-errors");
                    $(errorContainer).append(errorsHtml);
                    $(page).addClass("property-page-has-errors");
                }
                d.resolve();
            },
            error: function() {
                var obj = new Object();
                obj.fieldName = '';
                obj.message = get_peditor_msg('peditor.validationFailed');
                errors.push(obj);
                d.resolve();
            }
        });

        return deffers;
    }
};
PropertyEditor.Validator.Ajax = PropertyEditor.Util.inherit(PropertyEditor.Model.Validator, PropertyEditor.Validator.Ajax.prototype);

PropertyEditor.Model.Type = function(page, number, prefix, properties, value, defaultValue) {
    this.page = page;
    this.number = number;
    this.prefix = prefix;
    if (this.prefix !== undefined && this.prefix !== null && this.prefix !== "") {
        this.prefix = "_" + this.prefix;
    } else {
        this.prefix = "";
    }
    this.editorObject = this.page.editorObject;
    this.editor = this.page.editor;
    this.editorId = this.page.editorId;
    this.parentId = this.page.parentId;
    if (properties.name === undefined) {
        properties.name = PropertyEditor.Util.uuid();
    }
    this.id = this.editorId + this.parentId + this.prefix + '_' + properties.name;
    this.properties = properties;
    this.value = value;
    this.defaultValue = defaultValue;
    this.options = this.page.options;
    this.isDataReady = true;
};
PropertyEditor.Model.Type.prototype = {
    supportPrefix : false,
    initialize: function() {},
    validate: function(data, errors, checkEncryption) {
        var wrapper = $('#' + this.id + '_input');

        var value = data[this.properties.name];
        var defaultValue = null;

        if (this.defaultValue !== undefined && this.defaultValue !== null && this.defaultValue !== "") {
            defaultValue = this.defaultValue;
        }

        var hasValue = true;
        if (value === '' || value === undefined || value === null || value === '%%%%%%%%' ||
            ($.isArray(value) && value.length === 0)) {
            hasValue = false;
        }
        
        var checkRequired = false;
        if (this.properties.required_validation_control_field !== undefined && this.properties.required_validation_control_field !== null &&
            this.properties.required_validation_control_value !== undefined && this.properties.required_validation_control_value !== null) {
            var cf_name = this.properties.required_validation_control_field;
            var cf_value = this.properties.required_validation_control_value;
            var cf_isRegex = (this.properties.required_validation_control_use_regex.toLowerCase() === "true");
            
            checkRequired = PropertyEditor.Util.internalDynamicOptionsCheckValue(data, cf_name, cf_value, cf_isRegex);
        }
        if (this.properties.required !== undefined && this.properties.required.toLowerCase() === "true") {
            checkRequired = true;
        }

        if (checkRequired &&
            defaultValue === null && !hasValue) {
            var obj = new Object();
            obj.field = this.properties.name;
            obj.fieldName = this.properties.label;
            obj.message = this.options.mandatoryMessage;
            errors.push(obj);
            $(wrapper).append('<div class="property-input-error">' + obj.message + '</div>');
        }

        if (hasValue &&
            this.properties.regex_validation !== undefined &&
            this.properties.regex_validation !== '' &&
            (typeof value) === "string") {
            var regex = new RegExp(this.properties.regex_validation);
            if (!regex.exec(value)) {
                var obj2 = new Object();
                obj2.fieldName = this.properties.label;
                if (this.properties.validation_message !== undefined && this.properties.validation_message !== '') {
                    obj2.message = this.properties.validation_message;
                } else {
                    obj2.message = get_peditor_msg('peditor.validationFailed');
                }
                errors.push(obj2);
                $(wrapper).append('<div class="property-input-error">' + obj2.message + '</div>');
            }
        }

        if (this.properties.js_validation !== undefined && this.properties.js_validation !== '') {
            var func = PropertyEditor.Util.getFunction(this.properties.js_validation);
            if ($.isFunction(func)) {
                var errorMsg = func(this.properties.name, value);

                if (errorMsg !== null && errorMsg !== "") {
                    var obj2 = new Object();
                    obj2.fieldName = this.properties.label;
                    obj2.message = errorMsg;
                    errors.push(obj2);
                    $(wrapper).append('<div class="property-input-error">' + obj2.message + '</div>');
                }
            }
        }

        if ((checkEncryption !== undefined && checkEncryption) && hasValue && (typeof value) === "string") {
            if ((value.substring(0, 25) === "%%%%****SECURE_VALUE****-")) {
                var obj2 = new Object();
                obj2.fieldName = this.properties.label;
                obj2.message = get_peditor_msg('peditor.dataIsEncypted');
                errors.push(obj2);
                $(wrapper).append('<div class="property-input-error">' + obj2.message + '</div>');
            }
        }

        var deffers = this.addOnValidation(data, errors, checkEncryption);
        if (deffers !== null && deffers !== undefined && deffers.length > 0) {
            return deffers;
        }
    },
    addOnValidation: function(data, errors, checkEncryption) {
        //nothing will happen
    },
    getData: function(useDefault) {
        var data = new Object();
        var value = this.value;

        if (this.isDataReady) {
            value = $('[name=' + this.id + ']:not(.hidden)').val();
            if (value === undefined || value === null || value === "") {
                if (useDefault !== undefined && useDefault &&
                    this.defaultValue !== undefined && this.defaultValue !== null) {
                    value = this.defaultValue;
                } else {
                    value = "";
                }
            }
            value = value.trim();
        }
        data[this.properties.name] = value;
        PropertyEditor.Util.retrieveHashFieldValue(this, data);
        return data;
    },
    render: function() {
        var showHide = "";

        if (this.properties.control_field !== undefined && this.properties.control_field !== null &&
            this.properties.control_value !== undefined && this.properties.control_value !== null) {
            showHide = 'data-control_id="'+this.id+'" data-control_field="' + this.properties.control_field + '" data-control_value="' + this.properties.control_value + '"';

            if (this.properties.control_use_regex !== undefined && this.properties.control_use_regex.toLowerCase() === "true") {
                showHide += ' data-control_use_regex="true"';
            } else {
                showHide += ' data-control_use_regex="false"';
            }
        }
        if (this.properties.required_validation_control_field !== undefined && this.properties.required_validation_control_field !== null &&
            this.properties.required_validation_control_value !== undefined && this.properties.required_validation_control_value !== null) {
            showHide += 'data-required_control_id="'+this.id+'" data-required_control_field="' + this.properties.required_validation_control_field + '" data-required_control_value="' + this.properties.required_validation_control_value + '"';

            if (this.properties.required_validation_control_use_regex !== undefined && this.properties.required_validation_control_use_regex.toLowerCase() === "true") {
                showHide += ' data-required_control_use_regex="true"';
            } else {
                showHide += ' data-required_control_use_regex="false"';
            }
        }
        var cssClass = "";
        if (this.properties.developer_mode !== undefined && this.properties.developer_mode !== null) {
            var modes = this.properties.developer_mode.split(";");
            for (var i in modes) {
                if (modes[i] !== "") {
                    cssClass += " "+modes[i]+"-mode-only";
                }
            }
        }
        
        if (this.properties.viewport !== undefined) {
            cssClass += " viewport-" + this.properties.viewport;
        }
        
        var parentId = this.parentId;
        if (parentId !== "" && parentId !== undefined) {
            parentId = parentId.substring(1);
        }

        var html = '<div id="property_' + this.number + '" property-parentid="'+parentId+'" property-name="'+this.properties.name+'" class="'+this.getContainerClass()+' property_container_' + this.id + ' property-editor-property property-type-' + this.properties.type.toLowerCase() + cssClass + '" ' + showHide + '>';

        html += this.renderLabel();
        html += this.renderFieldWrapper();

        html += '<div style="clear:both;"></div></div>';

        return html;
    },
    getContainerClass: function() {
        return "";
    },
    renderLabel: function() {
        var html = "";
        if (this.properties.label !== undefined && this.properties.label !== null) {
            var required = '';
            if ((this.properties.required !== undefined && this.properties.required.toLowerCase() === 'true') 
                    || (this.properties.required_validation_control_field !== undefined && this.properties.required_validation_control_field !== null &&
                        this.properties.required_validation_control_value !== undefined && this.properties.required_validation_control_value !== null)) {
                required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
            }

            var description = '';
            if (this.properties.description !== undefined && this.properties.description !== null) {
                description = this.properties.description;
            }

            var toolTip = '';
            var toolTipId = '';
            if (description !== "" && this.properties.type !== "header") {
                toolTipId = this.properties.name + (new Date()).getTime() + (Math.floor(Math.random() * 10000));;
                toolTip = ' <i class="property-label-description fas fa-info-circle" data-tooltip-content="#'+toolTipId+'"></i>';
            }

            html += '<div class="property-label-container">';
            html += '<div class="property-label">' + this.properties.label + required + toolTip + '</div>';
            html += '<div id="'+toolTipId+'" class="property-description">' + description + '</div>';
            html += '</div>';
        }
        return html;
    },
    renderFieldWrapper: function() {
        var html = '<div id="' + this.id + '_input" class="property-input">';
        
        if (this.supportPrefix && this.properties.prefix !== undefined && this.properties.prefix !== null) {
            html += '<span class="withPrefix"><span class="prefix">'+this.properties.prefix+'</span>';
        }
        
        html += this.renderField();
        
        if (this.supportPrefix && this.properties.prefix !== undefined && this.properties.prefix !== null) {
            html += '</span>';
        }
        
        html += this.renderDefault();
        html += '</div>';
        return html;
    },
    renderField: function() {
        return "";
    },
    renderDefault: function() {
        var html = '';
        if (this.defaultValue !== null) {
            html = '<div class="default"><span class="label">' + get_peditor_msg('peditor.default') + '</span><span class="value">' + PropertyEditor.Util.escapeHtmlTag(this.defaultValue) + '</span><div class="clear"></div></div>';
        }
        return html;
    },
    initDefaultScripting: function() {
        PropertyEditor.Util.handleOptionsField(this);
        if (this.supportPrefix && this.properties.prefix !== undefined && this.properties.prefix !== null) {
            var container = $("#" + this.id).closest(".property-input");
            var prefixWidth = $(container).find(".withPrefix .prefix").outerWidth(true);
            $(container).find(".withPrefix input").css("padding-left", (prefixWidth + 5) + "px");
        }
        
        PropertyEditor.Util.handleSuggestionField(this);
    },
    initScripting: function() {},
    handleAjaxOptions: function(options, reference) {
        if (options !== null && options !== undefined) {
            this.properties.options = options;

            var wrapper = $('#' + this.id + '_input');
            var html = this.renderField() + this.renderDefault();
            $(wrapper).html(html);

            $('#' + this.id).trigger("change");
        }
    },
    isHidden: function() {
        return $(".property_container_" + this.id + ":not(.hidden)").length === 0;
    },
    pageShown: function() {},
    remove: function() {
        PropertyEditor.Util.unhandleOptionsField(this);
    }
};

PropertyEditor.Type.Header = function() {};
PropertyEditor.Type.Header.prototype = {
    shortname: "header",
    getData: function(useDefault) {
        return null;
    },
    validate: function(data, errors, checkEncryption) {}
};
PropertyEditor.Type.Header = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Header.prototype);

PropertyEditor.Type.Hidden = function() {};
PropertyEditor.Type.Hidden.prototype = {
    shortname: "hidden",
    renderField: function() {
        if (this.value === null) {
            this.value = "";
        }
        return '<input type="hidden" id="' + this.id + '" name="' + this.id + '" value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '" />';
    },
    renderDefault: function() {
        return "";
    },
    validate: function(data, errors, checkEncryption) {}
};
PropertyEditor.Type.Hidden = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Hidden.prototype);

PropertyEditor.Type.Label = function() {};
PropertyEditor.Type.Label.prototype = {
    shortname: "label",
    renderField: function() {
        if (this.value === null) {
            this.value = "";
        }
        var label = PropertyEditor.Util.escapeHtmlTag(this.value);
        if (this.properties.url !== undefined && this.properties.url !== null && this.properties.url !== "") {
            label = '<a href="'+this.properties.url+'" target="_blank">' + label + '</a>';
        }
        return '<input type="hidden" id="' + this.id + '" name="' + this.id + '" value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '" /><label>' + label + '</label>';
    },
    renderDefault: function() {
        return "";
    }
};
PropertyEditor.Type.Label = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Label.prototype);

PropertyEditor.Type.Readonly = function() {};
PropertyEditor.Type.Readonly.prototype = {
    shortname: "readonly",
    supportPrefix: true,
    renderField: function() {
        if (this.value === null) {
            this.value = "";
        }
        var size = '';
        if (this.properties.size !== undefined && this.properties.size !== null) {
            size = ' size="' + this.properties + '"';
        } else {
            size = ' size="50"';
        }
        return '<input type="text" id="' + this.id + '" name="' + this.id + '"' + size + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '" readonly />';
    },
    renderDefault: function() {
        return "";
    }
};
PropertyEditor.Type.Readonly = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Readonly.prototype);

PropertyEditor.Type.TextField = function() {};
PropertyEditor.Type.TextField.prototype = {
    shortname: "textfield",
    supportPrefix: true,
    renderField: function() {
        var size = '';
        if (this.value === null) {
            this.value = "";
        }
        if (this.properties.size !== undefined && this.properties.size !== null) {
            size = ' size="' + this.properties.size + '"';
        } else {
            size = ' size="50"';
        }
        var maxlength = '';
        if (this.properties.maxlength !== undefined && this.properties.maxlength !== null) {
            maxlength = ' maxlength="' + this.properties.maxlength + '"';
        }

        return '<input type="text" id="' + this.id + '" name="' + this.id + '"' + size + maxlength + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '"/>';
    }
};
PropertyEditor.Type.TextField = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.TextField.prototype);

PropertyEditor.Type.IconTextField = function() {};
PropertyEditor.Type.IconTextField.prototype = {
    shortname: "icon-textfield",
    icons : {
        "Font Awesome 5" : {
            'fab fa-500px' : '',
            'fab fa-accessible-icon' : 'accessibility handicap person wheelchair wheelchair-alt',
            'fab fa-accusoft' : '',
            'fab fa-acquisitions-incorporated' : 'Dungeons & Dragons d&d dnd fantasy game gaming tabletop',
            'fas fa-ad' : '',
            'fas fa-address-book' : '',
            'far fa-address-book' : '',
            'fas fa-address-card' : '',
            'far fa-address-card' : '',
            'fas fa-adjust' : 'contrast',
            'fab fa-adn' : '',
            'fab fa-adversal' : '',
            'fab fa-affiliatetheme' : '',
            'fas fa-air-freshener' : '',
            'fab fa-algolia' : '',
            'fas fa-align-center' : 'middle text',
            'fas fa-align-justify' : 'text',
            'fas fa-align-left' : 'text',
            'fas fa-align-right' : 'text',
            'fab fa-alipay' : '',
            'fas fa-allergies' : 'freckles hand intolerances pox spots',
            'fab fa-amazon' : '',
            'fab fa-amazon-pay' : '',
            'fas fa-ambulance' : 'help machine support vehicle',
            'fas fa-american-sign-language-interpreting' : '',
            'fab fa-amilia' : '',
            'fas fa-anchor' : 'link',
            'fab fa-android' : 'robot',
            'fab fa-angellist' : '',
            'fas fa-angle-double-down' : 'arrows',
            'fas fa-angle-double-left' : 'arrows back laquo previous quote',
            'fas fa-angle-double-right' : 'arrows forward next quote raquo',
            'fas fa-angle-double-up' : 'arrows',
            'fas fa-angle-down' : 'arrow',
            'fas fa-angle-left' : 'arrow back previous',
            'fas fa-angle-right' : 'arrow forward next',
            'fas fa-angle-up' : 'arrow',
            'fas fa-angry' : 'disapprove emoticon face mad upset',
            'far fa-angry' : 'disapprove emoticon face mad upset',
            'fab fa-angrycreative' : '',
            'fab fa-angular' : '',
            'fas fa-ankh' : 'amulet copper coptic christianity copts crux ansata egyptian venus',
            'fab fa-app-store' : '',
            'fab fa-app-store-ios' : '',
            'fab fa-apper' : '',
            'fab fa-apple' : 'food fruit mac osx',
            'fas fa-apple-alt' : 'fall food fruit fuji macintosh seasonal',
            'fab fa-apple-pay' : '',
            'fas fa-archive' : 'box package storage',
            'fas fa-archway' : 'arc monument road street',
            'fas fa-arrow-alt-circle-down' : 'arrow-circle-o-down download',
            'far fa-arrow-alt-circle-down' : 'arrow-circle-o-down download',
            'fas fa-arrow-alt-circle-left' : 'arrow-circle-o-left back previous',
            'far fa-arrow-alt-circle-left' : 'arrow-circle-o-left back previous',
            'fas fa-arrow-alt-circle-right' : 'arrow-circle-o-right forward next',
            'far fa-arrow-alt-circle-right' : 'arrow-circle-o-right forward next',
            'fas fa-arrow-alt-circle-up' : 'arrow-circle-o-up',
            'far fa-arrow-alt-circle-up' : 'arrow-circle-o-up',
            'fas fa-arrow-circle-down' : 'download',
            'fas fa-arrow-circle-left' : 'back previous',
            'fas fa-arrow-circle-right' : 'forward next',
            'fas fa-arrow-circle-up' : '',
            'fas fa-arrow-down' : 'download',
            'fas fa-arrow-left' : 'back previous',
            'fas fa-arrow-right' : 'forward next',
            'fas fa-arrow-up' : '',
            'fas fa-arrows-alt' : 'arrow arrows bigger enlarge expand fullscreen move position reorder resize',
            'fas fa-arrows-alt-h' : 'arrows-h resize',
            'fas fa-arrows-alt-v' : 'arrows-v resize',
            'fas fa-assistive-listening-systems' : '',
            'fas fa-asterisk' : 'details',
            'fab fa-asymmetrik' : '',
            'fas fa-at' : 'e-mail email',
            'fas fa-atlas' : 'book directions geography map wayfinding',
            'fas fa-atom' : 'atheism chemistry science',
            'fab fa-audible' : '',
            'fas fa-audio-description' : '',
            'fab fa-autoprefixer' : '',
            'fab fa-avianex' : '',
            'fab fa-aviato' : '',
            'fas fa-award' : 'honor praise prize recognition ribbon',
            'fab fa-aws' : '',
            'fas fa-backspace' : 'command delete keyboard undo',
            'fas fa-backward' : 'previous rewind',
            'fas fa-balance-scale' : 'balanced justice legal measure weight',
            'fas fa-ban' : 'abort ban block cancel delete hide prohibit remove stop trash',
            'fas fa-band-aid' : 'bandage boo boo ouch',
            'fab fa-bandcamp' : '',
            'fas fa-barcode' : 'scan',
            'fas fa-bars' : 'checklist drag hamburger list menu nav navigation ol reorder settings todo ul',
            'fas fa-baseball-ball' : '',
            'fas fa-basketball-ball' : '',
            'fas fa-bath' : '',
            'fas fa-battery-empty' : 'power status',
            'fas fa-battery-full' : 'power status',
            'fas fa-battery-half' : 'power status',
            'fas fa-battery-quarter' : 'power status',
            'fas fa-battery-three-quarters' : 'power status',
            'fas fa-bed' : 'lodging sleep travel',
            'fas fa-beer' : 'alcohol bar beverage drink liquor mug stein',
            'fab fa-behance' : '',
            'fab fa-behance-square' : '',
            'fas fa-bell' : 'alert notification reminder',
            'far fa-bell' : 'alert notification reminder',
            'fas fa-bell-slash' : '',
            'far fa-bell-slash' : '',
            'fas fa-bezier-curve' : 'curves illustrator lines path vector',
            'fas fa-bible' : 'book catholicism christianity',
            'fas fa-bicycle' : 'bike gears transportation vehicle',
            'fab fa-bimobject' : '',
            'fas fa-binoculars' : '',
            'fas fa-birthday-cake' : '',
            'fab fa-bitbucket' : 'bitbucket-square git',
            'fab fa-bitcoin' : '',
            'fab fa-bity' : '',
            'fab fa-black-tie' : '',
            'fab fa-blackberry' : '',
            'fas fa-blender' : '',
            'fas fa-blender-phone' : 'appliance fantasy silly',
            'fas fa-blind' : '',
            'fab fa-blogger' : '',
            'fab fa-blogger-b' : '',
            'fab fa-bluetooth' : '',
            'fab fa-bluetooth-b' : '',
            'fas fa-bold' : '',
            'fas fa-bolt' : 'electricity lightning weather zap',
            'fas fa-bomb' : '',
            'fas fa-bone' : '',
            'fas fa-bong' : 'aparatus cannabis marijuana pipe smoke smoking',
            'fas fa-book' : 'documentation read',
            'fas fa-book-dead' : 'Dungeons & Dragons crossbones d&d dark arts death dnd documentation evil fantasy halloween holiday read skull spell',
            'fas fa-book-open' : 'flyer notebook open book pamphlet reading',
            'fas fa-book-reader' : 'library',
            'fas fa-bookmark' : 'save',
            'far fa-bookmark' : 'save',
            'fas fa-bowling-ball' : '',
            'fas fa-box' : 'package',
            'fas fa-box-open' : '',
            'fas fa-boxes' : '',
            'fas fa-braille' : '',
            'fas fa-brain' : 'cerebellum gray matter intellect medulla oblongata mind noodle wit',
            'fas fa-briefcase' : 'bag business luggage office work',
            'fas fa-briefcase-medical' : 'health briefcase',
            'fas fa-broadcast-tower' : 'airwaves radio waves',
            'fas fa-broom' : 'clean firebolt fly halloween holiday nimbus 2000 quidditch sweep witch',
            'fas fa-brush' : 'bristles color handle painting',
            'fab fa-btc' : '',
            'fas fa-bug' : 'insect report',
            'fas fa-building' : 'apartment business company office work',
            'far fa-building' : 'apartment business company office work',
            'fas fa-bullhorn' : 'announcement broadcast louder megaphone share',
            'fas fa-bullseye' : 'target',
            'fas fa-burn' : 'energy',
            'fab fa-buromobelexperte' : '',
            'fas fa-bus' : 'machine public transportation transportation vehicle',
            'fas fa-bus-alt' : 'machine public transportation transportation vehicle',
            'fas fa-business-time' : 'briefcase business socks clock flight of the conchords wednesday',
            'fab fa-buysellads' : '',
            'fas fa-calculator' : '',
            'fas fa-calendar' : 'calendar-o date event schedule time when',
            'far fa-calendar' : 'calendar-o date event schedule time when',
            'fas fa-calendar-alt' : 'calendar date event schedule time when',
            'far fa-calendar-alt' : 'calendar date event schedule time when',
            'fas fa-calendar-check' : 'accept agree appointment confirm correct done ok select success todo',
            'far fa-calendar-check' : 'accept agree appointment confirm correct done ok select success todo',
            'fas fa-calendar-minus' : 'delete negative remove',
            'far fa-calendar-minus' : 'delete negative remove',
            'fas fa-calendar-plus' : 'add create new positive',
            'far fa-calendar-plus' : 'add create new positive',
            'fas fa-calendar-times' : 'archive delete remove x',
            'far fa-calendar-times' : 'archive delete remove x',
            'fas fa-camera' : 'photo picture record',
            'fas fa-camera-retro' : 'photo picture record',
            'fas fa-campground' : 'camping fall outdoors seasonal tent',
            'fas fa-cannabis' : 'bud chronic drugs endica endo ganja marijuana mary jane pot reefer sativa spliff weed whacky-tabacky',
            'fas fa-capsules' : 'drugs medicine',
            'fas fa-car' : 'machine transportation vehicle',
            'fas fa-car-alt' : '',
            'fas fa-car-battery' : '',
            'fas fa-car-crash' : '',
            'fas fa-car-side' : '',
            'fas fa-caret-down' : 'arrow dropdown menu more triangle down',
            'fas fa-caret-left' : 'arrow back previous triangle left',
            'fas fa-caret-right' : 'arrow forward next triangle right',
            'fas fa-caret-square-down' : 'caret-square-o-down dropdown menu more',
            'far fa-caret-square-down' : 'caret-square-o-down dropdown menu more',
            'fas fa-caret-square-left' : 'back caret-square-o-left previous',
            'far fa-caret-square-left' : 'back caret-square-o-left previous',
            'fas fa-caret-square-right' : 'caret-square-o-right forward next',
            'far fa-caret-square-right' : 'caret-square-o-right forward next',
            'fas fa-caret-square-up' : 'caret-square-o-up',
            'far fa-caret-square-up' : 'caret-square-o-up',
            'fas fa-caret-up' : 'arrow triangle up',
            'fas fa-cart-arrow-down' : 'shopping',
            'fas fa-cart-plus' : 'add create new positive shopping',
            'fas fa-cat' : 'feline halloween holiday kitten kitty meow pet',
            'fab fa-cc-amazon-pay' : '',
            'fab fa-cc-amex' : 'amex',
            'fab fa-cc-apple-pay' : '',
            'fab fa-cc-diners-club' : '',
            'fab fa-cc-discover' : '',
            'fab fa-cc-jcb' : '',
            'fab fa-cc-mastercard' : '',
            'fab fa-cc-paypal' : '',
            'fab fa-cc-stripe' : '',
            'fab fa-cc-visa' : '',
            'fab fa-centercode' : '',
            'fas fa-certificate' : 'badge star',
            'fas fa-chair' : 'furniture seat',
            'fas fa-chalkboard' : 'blackboard learning school teaching whiteboard writing',
            'fas fa-chalkboard-teacher' : 'blackboard instructor learning professor school whiteboard writing',
            'fas fa-charging-station' : '',
            'fas fa-chart-area' : 'analytics area-chart graph',
            'fas fa-chart-bar' : 'analytics bar-chart graph',
            'far fa-chart-bar' : 'analytics bar-chart graph',
            'fas fa-chart-line' : 'activity analytics dashboard graph line-chart',
            'fas fa-chart-pie' : 'analytics graph pie-chart',
            'fas fa-check' : 'accept agree checkmark confirm correct done notice notification notify ok select success tick todo yes',
            'fas fa-check-circle' : 'accept agree confirm correct done ok select success todo yes',
            'far fa-check-circle' : 'accept agree confirm correct done ok select success todo yes',
            'fas fa-check-double' : 'accept agree checkmark confirm correct done notice notification notify ok select success tick todo',
            'fas fa-check-square' : 'accept agree checkmark confirm correct done ok select success todo yes',
            'far fa-check-square' : 'accept agree checkmark confirm correct done ok select success todo yes',
            'fas fa-chess' : '',
            'fas fa-chess-bishop' : '',
            'fas fa-chess-board' : '',
            'fas fa-chess-king' : '',
            'fas fa-chess-knight' : '',
            'fas fa-chess-pawn' : '',
            'fas fa-chess-queen' : '',
            'fas fa-chess-rook' : '',
            'fas fa-chevron-circle-down' : 'arrow dropdown menu more',
            'fas fa-chevron-circle-left' : 'arrow back previous',
            'fas fa-chevron-circle-right' : 'arrow forward next',
            'fas fa-chevron-circle-up' : 'arrow',
            'fas fa-chevron-down' : '',
            'fas fa-chevron-left' : 'back bracket previous',
            'fas fa-chevron-right' : 'bracket forward next',
            'fas fa-chevron-up' : '',
            'fas fa-child' : '',
            'fab fa-chrome' : 'browser',
            'fas fa-church' : 'building community religion',
            'fas fa-circle' : 'circle-thin dot notification',
            'far fa-circle' : 'circle-thin dot notification',
            'fas fa-circle-notch' : 'circle-o-notch',
            'fas fa-city' : 'buildings busy skyscrapers urban windows',
            'fas fa-clipboard' : 'paste',
            'far fa-clipboard' : 'paste',
            'fas fa-clipboard-check' : 'accept agree confirm done ok select success todo yes',
            'fas fa-clipboard-list' : 'checklist completed done finished intinerary ol schedule todo ul',
            'fas fa-clock' : 'date late schedule timer timestamp watch',
            'far fa-clock' : 'date late schedule timer timestamp watch',
            'fas fa-clone' : 'copy duplicate',
            'far fa-clone' : 'copy duplicate',
            'fas fa-closed-captioning' : 'cc',
            'far fa-closed-captioning' : 'cc',
            'fas fa-cloud' : 'save',
            'fas fa-cloud-download-alt' : 'import',
            'fas fa-cloud-meatball' : '',
            'fas fa-cloud-moon' : 'crescent evening halloween holiday lunar night sky',
            'fas fa-cloud-moon-rain' : '',
            'fas fa-cloud-rain' : 'precipitation',
            'fas fa-cloud-showers-heavy' : 'precipitation rain storm',
            'fas fa-cloud-sun' : 'day daytime fall outdoors seasonal',
            'fas fa-cloud-sun-rain' : '',
            'fas fa-cloud-upload-alt' : 'cloud-upload',
            'fab fa-cloudscale' : '',
            'fab fa-cloudsmith' : '',
            'fab fa-cloudversify' : '',
            'fas fa-cocktail' : 'alcohol beverage drink',
            'fas fa-code' : 'brackets html',
            'fas fa-code-branch' : 'branch code-fork fork git github rebase svn vcs version',
            'fab fa-codepen' : '',
            'fab fa-codiepie' : '',
            'fas fa-coffee' : 'beverage breakfast cafe drink fall morning mug seasonal tea',
            'fas fa-cog' : 'settings',
            'fas fa-cogs' : 'gears settings',
            'fas fa-coins' : '',
            'fas fa-columns' : 'dashboard panes split',
            'fas fa-comment' : 'bubble chat conversation feedback message note notification sms speech texting',
            'far fa-comment' : 'bubble chat conversation feedback message note notification sms speech texting',
            'fas fa-comment-alt' : 'bubble chat commenting conversation feedback message note notification sms speech texting',
            'far fa-comment-alt' : 'bubble chat commenting conversation feedback message note notification sms speech texting',
            'fas fa-comment-dollar' : '',
            'fas fa-comment-dots' : '',
            'far fa-comment-dots' : '',
            'fas fa-comment-slash' : '',
            'fas fa-comments' : 'bubble chat conversation feedback message note notification sms speech texting',
            'far fa-comments' : 'bubble chat conversation feedback message note notification sms speech texting',
            'fas fa-comments-dollar' : '',
            'fas fa-compact-disc' : 'bluray cd disc media',
            'fas fa-compass' : 'directory location menu safari',
            'far fa-compass' : 'directory location menu safari',
            'fas fa-compress' : 'collapse combine contract merge smaller',
            'fas fa-concierge-bell' : 'attention hotel service support',
            'fab fa-connectdevelop' : '',
            'fab fa-contao' : '',
            'fas fa-cookie' : 'baked good chips food snack sweet treat',
            'fas fa-cookie-bite' : 'baked good bitten chips eating food snack sweet treat',
            'fas fa-copy' : 'clone duplicate file files-o',
            'far fa-copy' : 'clone duplicate file files-o',
            'fas fa-copyright' : '',
            'far fa-copyright' : '',
            'fas fa-couch' : 'furniture sofa',
            'fab fa-cpanel' : '',
            'fab fa-creative-commons' : '',
            'fab fa-creative-commons-by' : '',
            'fab fa-creative-commons-nc' : '',
            'fab fa-creative-commons-nc-eu' : '',
            'fab fa-creative-commons-nc-jp' : '',
            'fab fa-creative-commons-nd' : '',
            'fab fa-creative-commons-pd' : '',
            'fab fa-creative-commons-pd-alt' : '',
            'fab fa-creative-commons-remix' : '',
            'fab fa-creative-commons-sa' : '',
            'fab fa-creative-commons-sampling' : '',
            'fab fa-creative-commons-sampling-plus' : '',
            'fab fa-creative-commons-share' : '',
            'fab fa-creative-commons-zero' : '',
            'fas fa-credit-card' : 'buy checkout credit-card-alt debit money payment purchase',
            'far fa-credit-card' : 'buy checkout credit-card-alt debit money payment purchase',
            'fab fa-critical-role' : 'Dungeons & Dragons d&d dnd fantasy game gaming tabletop',
            'fas fa-crop' : 'design',
            'fas fa-crop-alt' : '',
            'fas fa-cross' : 'catholicism christianity',
            'fas fa-crosshairs' : 'gpd picker position',
            'fas fa-crow' : 'bird bullfrog fauna halloween holiday toad',
            'fas fa-crown' : '',
            'fab fa-css3' : 'code',
            'fab fa-css3-alt' : '',
            'fas fa-cube' : 'package',
            'fas fa-cubes' : 'packages',
            'fas fa-cut' : 'scissors',
            'fab fa-cuttlefish' : '',
            'fab fa-d-and-d' : '',
            'fab fa-d-and-d-beyond' : 'Dungeons & Dragons d&d dnd fantasy gaming tabletop',
            'fab fa-dashcube' : '',
            'fas fa-database' : '',
            'fas fa-deaf' : '',
            'fab fa-delicious' : '',
            'fas fa-democrat' : 'american democratic party donkey election left left-wing liberal politics usa',
            'fab fa-deploydog' : '',
            'fab fa-deskpro' : '',
            'fas fa-desktop' : 'computer cpu demo desktop device machine monitor pc screen',
            'fab fa-dev' : '',
            'fab fa-deviantart' : '',
            'fas fa-dharmachakra' : 'buddhism buddhist wheel of dharma',
            'fas fa-diagnoses' : '',
            'fas fa-dice' : 'chance gambling game roll',
            'fas fa-dice-d20' : 'Dungeons & Dragons chance d&d dnd fantasy gambling game roll',
            'fas fa-dice-d6' : 'Dungeons & Dragons chance d&d dnd fantasy gambling game roll',
            'fas fa-dice-five' : 'chance gambling game roll',
            'fas fa-dice-four' : 'chance gambling game roll',
            'fas fa-dice-one' : 'chance gambling game roll',
            'fas fa-dice-six' : 'chance gambling game roll',
            'fas fa-dice-three' : 'chance gambling game roll',
            'fas fa-dice-two' : 'chance gambling game roll',
            'fab fa-digg' : '',
            'fab fa-digital-ocean' : '',
            'fas fa-digital-tachograph' : '',
            'fas fa-directions' : '',
            'fab fa-discord' : '',
            'fab fa-discourse' : '',
            'fas fa-divide' : '',
            'fas fa-dizzy' : 'dazed disapprove emoticon face',
            'far fa-dizzy' : 'dazed disapprove emoticon face',
            'fas fa-dna' : 'double helix helix',
            'fab fa-dochub' : '',
            'fab fa-docker' : '',
            'fas fa-dog' : 'canine fauna mammmal pet pooch puppy woof',
            'fas fa-dollar-sign' : '$ dollar-sign money price usd',
            'fas fa-dolly' : '',
            'fas fa-dolly-flatbed' : '',
            'fas fa-donate' : 'generosity give',
            'fas fa-door-closed' : '',
            'fas fa-door-open' : '',
            'fas fa-dot-circle' : 'bullseye notification target',
            'far fa-dot-circle' : 'bullseye notification target',
            'fas fa-dove' : 'bird fauna flying peace',
            'fas fa-download' : 'import',
            'fab fa-draft2digital' : '',
            'fas fa-drafting-compass' : 'mechanical drawing plot plotting',
            'fas fa-dragon' : 'Dungeons & Dragons d&d dnd fantasy',
            'fas fa-draw-polygon' : '',
            'fab fa-dribbble' : '',
            'fab fa-dribbble-square' : '',
            'fab fa-dropbox' : '',
            'fas fa-drum' : 'instrument music percussion snare sound',
            'fas fa-drum-steelpan' : 'calypso instrument music percussion reggae snare sound steel tropical',
            'fas fa-drumstick-bite' : '',
            'fab fa-drupal' : '',
            'fas fa-dumbbell' : 'exercise gym strength weight weight-lifting',
            'fas fa-dungeon' : 'Dungeons & Dragons d&d dnd door entrance fantasy gate',
            'fab fa-dyalog' : '',
            'fab fa-earlybirds' : '',
            'fab fa-ebay' : '',
            'fab fa-edge' : 'browser ie',
            'fas fa-edit' : 'edit pen pencil update write',
            'far fa-edit' : 'edit pen pencil update write',
            'fas fa-eject' : '',
            'fab fa-elementor' : '',
            'fas fa-ellipsis-h' : 'dots drag kebab list menu nav navigation ol reorder settings ul',
            'fas fa-ellipsis-v' : 'dots drag kebab list menu nav navigation ol reorder settings ul',
            'fab fa-ello' : '',
            'fab fa-ember' : '',
            'fab fa-empire' : '',
            'fas fa-envelope' : 'e-mail email letter mail message notification support',
            'far fa-envelope' : 'e-mail email letter mail message notification support',
            'fas fa-envelope-open' : 'e-mail email letter mail message notification support',
            'far fa-envelope-open' : 'e-mail email letter mail message notification support',
            'fas fa-envelope-open-text' : '',
            'fas fa-envelope-square' : 'e-mail email letter mail message notification support',
            'fab fa-envira' : 'leaf',
            'fas fa-equals' : '',
            'fas fa-eraser' : 'delete remove',
            'fab fa-erlang' : '',
            'fab fa-ethereum' : '',
            'fab fa-etsy' : '',
            'fas fa-euro-sign' : 'eur',
            'fas fa-exchange-alt' : 'arrow arrows exchange reciprocate return swap transfer',
            'fas fa-exclamation' : 'alert danger error important notice notification notify problem warning',
            'fas fa-exclamation-circle' : 'alert danger error important notice notification notify problem warning',
            'fas fa-exclamation-triangle' : 'alert danger error important notice notification notify problem warning',
            'fas fa-expand' : 'bigger enlarge resize',
            'fas fa-expand-arrows-alt' : 'arrows-alt bigger enlarge move resize',
            'fab fa-expeditedssl' : '',
            'fas fa-external-link-alt' : 'external-link new open',
            'fas fa-external-link-square-alt' : 'external-link-square new open',
            'fas fa-eye' : 'optic see seen show sight views visible',
            'far fa-eye' : 'optic see seen show sight views visible',
            'fas fa-eye-dropper' : 'eyedropper',
            'fas fa-eye-slash' : 'blind hide show toggle unseen views visible visiblity',
            'far fa-eye-slash' : 'blind hide show toggle unseen views visible visiblity',
            'fab fa-facebook' : 'facebook-official social network',
            'fab fa-facebook-f' : 'facebook',
            'fab fa-facebook-messenger' : '',
            'fab fa-facebook-square' : 'social network',
            'fab fa-fantasy-flight-games' : 'Dungeons & Dragons d&d dnd fantasy game gaming tabletop',
            'fas fa-fast-backward' : 'beginning first previous rewind start',
            'fas fa-fast-forward' : 'end last next',
            'fas fa-fax' : '',
            'fas fa-feather' : 'bird light plucked quill',
            'fas fa-feather-alt' : 'bird light plucked quill',
            'fas fa-female' : 'human person profile user woman',
            'fas fa-fighter-jet' : 'airplane fast fly goose maverick plane quick top gun transportation travel',
            'fas fa-file' : 'document new page pdf resume',
            'far fa-file' : 'document new page pdf resume',
            'fas fa-file-alt' : 'document file-text invoice new page pdf',
            'far fa-file-alt' : 'document file-text invoice new page pdf',
            'fas fa-file-archive' : '.zip bundle compress compression download zip',
            'far fa-file-archive' : '.zip bundle compress compression download zip',
            'fas fa-file-audio' : '',
            'far fa-file-audio' : '',
            'fas fa-file-code' : '',
            'far fa-file-code' : '',
            'fas fa-file-contract' : 'agreement binding document legal signature',
            'fas fa-file-csv' : 'spreadsheets',
            'fas fa-file-download' : '',
            'fas fa-file-excel' : '',
            'far fa-file-excel' : '',
            'fas fa-file-export' : '',
            'fas fa-file-image' : '',
            'far fa-file-image' : '',
            'fas fa-file-import' : '',
            'fas fa-file-invoice' : 'bill document receipt',
            'fas fa-file-invoice-dollar' : '$ bill document dollar-sign money receipt usd',
            'fas fa-file-medical' : '',
            'fas fa-file-medical-alt' : '',
            'fas fa-file-pdf' : '',
            'far fa-file-pdf' : '',
            'fas fa-file-powerpoint' : '',
            'far fa-file-powerpoint' : '',
            'fas fa-file-prescription' : 'drugs medical medicine rx',
            'fas fa-file-signature' : 'John Hancock contract document name',
            'fas fa-file-upload' : '',
            'fas fa-file-video' : '',
            'far fa-file-video' : '',
            'fas fa-file-word' : '',
            'far fa-file-word' : '',
            'fas fa-fill' : 'bucket color paint paint bucket',
            'fas fa-fill-drip' : 'bucket color drop paint paint bucket spill',
            'fas fa-film' : 'movie',
            'fas fa-filter' : 'funnel options',
            'fas fa-fingerprint' : 'human id identification lock smudge touch unique unlock',
            'fas fa-fire' : 'caliente flame heat hot popular',
            'fas fa-fire-extinguisher' : '',
            'fab fa-firefox' : 'browser',
            'fas fa-first-aid' : '',
            'fab fa-first-order' : '',
            'fab fa-first-order-alt' : '',
            'fab fa-firstdraft' : '',
            'fas fa-fish' : 'fauna gold swimming',
            'fas fa-fist-raised' : 'Dungeons & Dragons d&d dnd fantasy hand ki monk resist strength unarmed combat',
            'fas fa-flag' : 'country notice notification notify pole report symbol',
            'far fa-flag' : 'country notice notification notify pole report symbol',
            'fas fa-flag-checkered' : 'notice notification notify pole racing report symbol',
            'fas fa-flag-usa' : 'betsy ross country old glory stars stripes symbol',
            'fas fa-flask' : 'beaker experimental labs science',
            'fab fa-flickr' : '',
            'fab fa-flipboard' : '',
            'fas fa-flushed' : 'embarrassed emoticon face',
            'far fa-flushed' : 'embarrassed emoticon face',
            'fab fa-fly' : '',
            'fas fa-folder' : '',
            'far fa-folder' : '',
            'fas fa-folder-minus' : 'archive delete negative remove',
            'fas fa-folder-open' : '',
            'far fa-folder-open' : '',
            'fas fa-folder-plus' : 'add create new positive',
            'fas fa-font' : 'text',
            'fab fa-font-awesome' : 'meanpath',
            'fab fa-font-awesome-alt' : '',
            'fab fa-font-awesome-flag' : '',
            'far fa-font-awesome-logo-full' : '',
            'fas fa-font-awesome-logo-full' : '',
            'fab fa-font-awesome-logo-full' : '',
            'fab fa-fonticons' : '',
            'fab fa-fonticons-fi' : '',
            'fas fa-football-ball' : 'fall pigskin seasonal',
            'fab fa-fort-awesome' : 'castle',
            'fab fa-fort-awesome-alt' : 'castle',
            'fab fa-forumbee' : '',
            'fas fa-forward' : 'forward next',
            'fab fa-foursquare' : '',
            'fab fa-free-code-camp' : '',
            'fab fa-freebsd' : '',
            'fas fa-frog' : 'amphibian bullfrog fauna hop kermit kiss prince ribbit toad wart',
            'fas fa-frown' : 'disapprove emoticon face rating sad',
            'far fa-frown' : 'disapprove emoticon face rating sad',
            'fas fa-frown-open' : 'disapprove emoticon face rating sad',
            'far fa-frown-open' : 'disapprove emoticon face rating sad',
            'fab fa-fulcrum' : '',
            'fas fa-funnel-dollar' : '',
            'fas fa-futbol' : 'ball football soccer',
            'far fa-futbol' : 'ball football soccer',
            'fab fa-galactic-republic' : 'politics star wars',
            'fab fa-galactic-senate' : 'star wars',
            'fas fa-gamepad' : 'controller',
            'fas fa-gas-pump' : '',
            'fas fa-gavel' : 'hammer judge lawyer opinion',
            'fas fa-gem' : 'diamond',
            'far fa-gem' : 'diamond',
            'fas fa-genderless' : '',
            'fab fa-get-pocket' : '',
            'fab fa-gg' : '',
            'fab fa-gg-circle' : '',
            'fas fa-ghost' : 'apparition blinky clyde floating halloween holiday inky pinky spirit',
            'fas fa-gift' : 'generosity giving party present wrapped',
            'fab fa-git' : '',
            'fab fa-git-square' : '',
            'fab fa-github' : 'octocat',
            'fab fa-github-alt' : 'octocat',
            'fab fa-github-square' : 'octocat',
            'fab fa-gitkraken' : '',
            'fab fa-gitlab' : 'Axosoft',
            'fab fa-gitter' : '',
            'fas fa-glass-martini' : 'alcohol bar beverage drink glass liquor martini',
            'fas fa-glass-martini-alt' : '',
            'fas fa-glasses' : 'foureyes hipster nerd reading sight spectacles',
            'fab fa-glide' : '',
            'fab fa-glide-g' : '',
            'fas fa-globe' : 'all coordinates country earth global gps language localize location map online place planet translate travel world',
            'fas fa-globe-africa' : 'all country earth global gps language localize location map online place planet translate travel world',
            'fas fa-globe-americas' : 'all country earth global gps language localize location map online place planet translate travel world',
            'fas fa-globe-asia' : 'all country earth global gps language localize location map online place planet translate travel world',
            'fab fa-gofore' : '',
            'fas fa-golf-ball' : '',
            'fab fa-goodreads' : '',
            'fab fa-goodreads-g' : '',
            'fab fa-google' : '',
            'fab fa-google-drive' : '',
            'fab fa-google-play' : '',
            'fab fa-google-plus' : 'google-plus-circle google-plus-official',
            'fab fa-google-plus-g' : 'google-plus social network',
            'fab fa-google-plus-square' : 'social network',
            'fab fa-google-wallet' : '',
            'fas fa-gopuram' : 'building entrance hinduism temple tower',
            'fas fa-graduation-cap' : 'learning school student',
            'fab fa-gratipay' : 'favorite heart like love',
            'fab fa-grav' : '',
            'fas fa-greater-than' : '',
            'fas fa-greater-than-equal' : '',
            'fas fa-grimace' : 'cringe emoticon face',
            'far fa-grimace' : 'cringe emoticon face',
            'fas fa-grin' : 'emoticon face laugh smile',
            'far fa-grin' : 'emoticon face laugh smile',
            'fas fa-grin-alt' : 'emoticon face laugh smile',
            'far fa-grin-alt' : 'emoticon face laugh smile',
            'fas fa-grin-beam' : 'emoticon face laugh smile',
            'far fa-grin-beam' : 'emoticon face laugh smile',
            'fas fa-grin-beam-sweat' : 'emoticon face smile',
            'far fa-grin-beam-sweat' : 'emoticon face smile',
            'fas fa-grin-hearts' : 'emoticon face love smile',
            'far fa-grin-hearts' : 'emoticon face love smile',
            'fas fa-grin-squint' : 'emoticon face laugh smile',
            'far fa-grin-squint' : 'emoticon face laugh smile',
            'fas fa-grin-squint-tears' : 'emoticon face happy smile',
            'far fa-grin-squint-tears' : 'emoticon face happy smile',
            'fas fa-grin-stars' : 'emoticon face star-struck',
            'far fa-grin-stars' : 'emoticon face star-struck',
            'fas fa-grin-tears' : 'LOL emoticon face',
            'far fa-grin-tears' : 'LOL emoticon face',
            'fas fa-grin-tongue' : 'LOL emoticon face',
            'far fa-grin-tongue' : 'LOL emoticon face',
            'fas fa-grin-tongue-squint' : 'LOL emoticon face',
            'far fa-grin-tongue-squint' : 'LOL emoticon face',
            'fas fa-grin-tongue-wink' : 'LOL emoticon face',
            'far fa-grin-tongue-wink' : 'LOL emoticon face',
            'fas fa-grin-wink' : 'emoticon face flirt laugh smile',
            'far fa-grin-wink' : 'emoticon face flirt laugh smile',
            'fas fa-grip-horizontal' : 'affordance drag drop grab handle',
            'fas fa-grip-vertical' : 'affordance drag drop grab handle',
            'fab fa-gripfire' : '',
            'fab fa-grunt' : '',
            'fab fa-gulp' : '',
            'fas fa-h-square' : 'hospital hotel',
            'fab fa-hacker-news' : '',
            'fab fa-hacker-news-square' : '',
            'fab fa-hackerrank' : '',
            'fas fa-hammer' : 'admin fix repair settings tool',
            'fas fa-hamsa' : 'amulet christianity islam jewish judaism muslim protection',
            'fas fa-hand-holding' : '',
            'fas fa-hand-holding-heart' : '',
            'fas fa-hand-holding-usd' : '$ dollar sign donation giving money price',
            'fas fa-hand-lizard' : '',
            'far fa-hand-lizard' : '',
            'fas fa-hand-paper' : 'stop',
            'far fa-hand-paper' : 'stop',
            'fas fa-hand-peace' : '',
            'far fa-hand-peace' : '',
            'fas fa-hand-point-down' : 'finger hand-o-down point',
            'far fa-hand-point-down' : 'finger hand-o-down point',
            'fas fa-hand-point-left' : 'back finger hand-o-left left point previous',
            'far fa-hand-point-left' : 'back finger hand-o-left left point previous',
            'fas fa-hand-point-right' : 'finger forward hand-o-right next point right',
            'far fa-hand-point-right' : 'finger forward hand-o-right next point right',
            'fas fa-hand-point-up' : 'finger hand-o-up point',
            'far fa-hand-point-up' : 'finger hand-o-up point',
            'fas fa-hand-pointer' : 'select',
            'far fa-hand-pointer' : 'select',
            'fas fa-hand-rock' : '',
            'far fa-hand-rock' : '',
            'fas fa-hand-scissors' : '',
            'far fa-hand-scissors' : '',
            'fas fa-hand-spock' : '',
            'far fa-hand-spock' : '',
            'fas fa-hands' : '',
            'fas fa-hands-helping' : 'aid assistance partnership volunteering',
            'fas fa-handshake' : 'greeting partnership',
            'far fa-handshake' : 'greeting partnership',
            'fas fa-hanukiah' : 'candle hanukkah jewish judaism light',
            'fas fa-hashtag' : '',
            'fas fa-hat-wizard' : 'Dungeons & Dragons buckle cloth clothing d&d dnd fantasy halloween holiday mage magic pointy witch',
            'fas fa-haykal' : 'bahai bahá\'í star',
            'fas fa-hdd' : 'cpu hard drive harddrive machine save storage',
            'far fa-hdd' : 'cpu hard drive harddrive machine save storage',
            'fas fa-heading' : 'header',
            'fas fa-headphones' : 'audio listen music sound speaker',
            'fas fa-headphones-alt' : 'audio listen music sound speaker',
            'fas fa-headset' : 'audio gamer gaming listen live chat microphone shot caller sound support telemarketer',
            'fas fa-heart' : 'favorite like love',
            'far fa-heart' : 'favorite like love',
            'fas fa-heartbeat' : 'ekg lifeline vital signs',
            'fas fa-helicopter' : 'airwolf apache chopper flight fly',
            'fas fa-highlighter' : 'edit marker sharpie update write',
            'fas fa-hiking' : 'activity backpack fall fitness outdoors seasonal walking',
            'fas fa-hippo' : 'fauna hungry mammmal',
            'fab fa-hips' : '',
            'fab fa-hire-a-helper' : '',
            'fas fa-history' : '',
            'fas fa-hockey-puck' : '',
            'fas fa-home' : 'house main',
            'fab fa-hooli' : '',
            'fab fa-hornbill' : '',
            'fas fa-horse' : 'equus fauna mammmal neigh',
            'fas fa-hospital' : 'building emergency room medical center',
            'far fa-hospital' : 'building emergency room medical center',
            'fas fa-hospital-alt' : 'building emergency room medical center',
            'fas fa-hospital-symbol' : '',
            'fas fa-hot-tub' : '',
            'fas fa-hotel' : 'building lodging',
            'fab fa-hotjar' : '',
            'fas fa-hourglass' : '',
            'far fa-hourglass' : '',
            'fas fa-hourglass-end' : '',
            'fas fa-hourglass-half' : '',
            'fas fa-hourglass-start' : '',
            'fas fa-house-damage' : 'devastation home',
            'fab fa-houzz' : '',
            'fas fa-hryvnia' : 'money',
            'fab fa-html5' : '',
            'fab fa-hubspot' : '',
            'fas fa-i-cursor' : '',
            'fas fa-id-badge' : '',
            'far fa-id-badge' : '',
            'fas fa-id-card' : 'document identification issued',
            'far fa-id-card' : 'document identification issued',
            'fas fa-id-card-alt' : 'demographics',
            'fas fa-image' : 'album photo picture',
            'far fa-image' : 'album photo picture',
            'fas fa-images' : 'album photo picture',
            'far fa-images' : 'album photo picture',
            'fab fa-imdb' : '',
            'fas fa-inbox' : '',
            'fas fa-indent' : '',
            'fas fa-industry' : 'factory manufacturing',
            'fas fa-infinity' : '',
            'fas fa-info' : 'details help information more',
            'fas fa-info-circle' : 'details help information more',
            'fab fa-instagram' : '',
            'fab fa-internet-explorer' : 'browser ie',
            'fab fa-ioxhost' : '',
            'fas fa-italic' : 'italics',
            'fab fa-itunes' : '',
            'fab fa-itunes-note' : '',
            'fab fa-java' : '',
            'fas fa-jedi' : 'star wars',
            'fab fa-jedi-order' : 'star wars',
            'fab fa-jenkins' : '',
            'fab fa-joget' : '',
            'fas fa-joint' : 'blunt cannabis doobie drugs marijuana roach smoke smoking spliff',
            'fab fa-joomla' : '',
            'fas fa-journal-whills' : 'book jedi star wars the force',
            'fab fa-js' : '',
            'fab fa-js-square' : '',
            'fab fa-jsfiddle' : '',
            'fas fa-kaaba' : 'building cube islam muslim',
            'fab fa-kaggle' : '',
            'fas fa-key' : 'password unlock',
            'fab fa-keybase' : '',
            'fas fa-keyboard' : 'input type',
            'far fa-keyboard' : 'input type',
            'fab fa-keycdn' : '',
            'fas fa-khanda' : 'chakkar sikh sikhism sword',
            'fab fa-kickstarter' : '',
            'fab fa-kickstarter-k' : '',
            'fas fa-kiss' : 'beso emoticon face love smooch',
            'far fa-kiss' : 'beso emoticon face love smooch',
            'fas fa-kiss-beam' : 'beso emoticon face love smooch',
            'far fa-kiss-beam' : 'beso emoticon face love smooch',
            'fas fa-kiss-wink-heart' : 'beso emoticon face love smooch',
            'far fa-kiss-wink-heart' : 'beso emoticon face love smooch',
            'fas fa-kiwi-bird' : 'bird fauna',
            'fab fa-korvue' : '',
            'fas fa-landmark' : 'building historic memoroable politics',
            'fas fa-language' : 'dialect idiom localize speech translate vernacular',
            'fas fa-laptop' : 'computer cpu dell demo device dude you\'re getting mac macbook machine pc',
            'fas fa-laptop-code' : '',
            'fab fa-laravel' : '',
            'fab fa-lastfm' : '',
            'fab fa-lastfm-square' : '',
            'fas fa-laugh' : 'LOL emoticon face laugh',
            'far fa-laugh' : 'LOL emoticon face laugh',
            'fas fa-laugh-beam' : 'LOL emoticon face',
            'far fa-laugh-beam' : 'LOL emoticon face',
            'fas fa-laugh-squint' : 'LOL emoticon face',
            'far fa-laugh-squint' : 'LOL emoticon face',
            'fas fa-laugh-wink' : 'LOL emoticon face',
            'far fa-laugh-wink' : 'LOL emoticon face',
            'fas fa-layer-group' : 'layers',
            'fas fa-leaf' : 'eco flora nature plant',
            'fab fa-leanpub' : '',
            'fas fa-lemon' : 'food',
            'far fa-lemon' : 'food',
            'fab fa-less' : '',
            'fas fa-less-than' : '',
            'fas fa-less-than-equal' : '',
            'fas fa-level-down-alt' : 'level-down',
            'fas fa-level-up-alt' : 'level-up',
            'fas fa-life-ring' : 'support',
            'far fa-life-ring' : 'support',
            'fas fa-lightbulb' : 'idea inspiration',
            'far fa-lightbulb' : 'idea inspiration',
            'fab fa-line' : '',
            'fas fa-link' : 'chain',
            'fab fa-linkedin' : 'linkedin-square',
            'fab fa-linkedin-in' : 'linkedin',
            'fab fa-linode' : '',
            'fab fa-linux' : 'tux',
            'fas fa-lira-sign' : 'try turkish',
            'fas fa-list' : 'checklist completed done finished ol todo ul',
            'fas fa-list-alt' : 'checklist completed done finished ol todo ul',
            'far fa-list-alt' : 'checklist completed done finished ol todo ul',
            'fas fa-list-ol' : 'checklist list numbers ol todo ul',
            'fas fa-list-ul' : 'checklist list ol todo ul',
            'fas fa-location-arrow' : 'address coordinates gps location map place where',
            'fas fa-lock' : 'admin protect security',
            'fas fa-lock-open' : 'admin lock open password protect',
            'fas fa-long-arrow-alt-down' : 'long-arrow-down',
            'fas fa-long-arrow-alt-left' : 'back long-arrow-left previous',
            'fas fa-long-arrow-alt-right' : 'long-arrow-right',
            'fas fa-long-arrow-alt-up' : 'long-arrow-up',
            'fas fa-low-vision' : '',
            'fas fa-luggage-cart' : '',
            'fab fa-lyft' : '',
            'fab fa-magento' : '',
            'fas fa-magic' : 'autocomplete automatic mage magic spell witch wizard',
            'fas fa-magnet' : '',
            'fas fa-mail-bulk' : '',
            'fab fa-mailchimp' : '',
            'fas fa-male' : 'human man person profile user',
            'fab fa-mandalorian' : '',
            'fas fa-map' : 'coordinates location paper place travel',
            'far fa-map' : 'coordinates location paper place travel',
            'fas fa-map-marked' : 'address coordinates destination gps localize location map paper pin place point of interest position route travel where',
            'fas fa-map-marked-alt' : 'address coordinates destination gps localize location map paper pin place point of interest position route travel where',
            'fas fa-map-marker' : 'address coordinates gps localize location map pin place position travel where',
            'fas fa-map-marker-alt' : 'address coordinates gps localize location map pin place position travel where',
            'fas fa-map-pin' : 'address coordinates gps localize location map marker place position travel where',
            'fas fa-map-signs' : '',
            'fab fa-markdown' : '',
            'fas fa-marker' : 'edit sharpie update write',
            'fas fa-mars' : 'male',
            'fas fa-mars-double' : '',
            'fas fa-mars-stroke' : '',
            'fas fa-mars-stroke-h' : '',
            'fas fa-mars-stroke-v' : '',
            'fas fa-mask' : 'costume disguise halloween holiday secret super hero',
            'fab fa-mastodon' : '',
            'fab fa-maxcdn' : '',
            'fas fa-medal' : '',
            'fab fa-medapps' : '',
            'fab fa-medium' : '',
            'fab fa-medium-m' : '',
            'fas fa-medkit' : 'first aid firstaid health help support',
            'fab fa-medrt' : '',
            'fab fa-meetup' : '',
            'fab fa-megaport' : '',
            'fas fa-meh' : 'emoticon face neutral rating',
            'far fa-meh' : 'emoticon face neutral rating',
            'fas fa-meh-blank' : 'emoticon face neutral rating',
            'far fa-meh-blank' : 'emoticon face neutral rating',
            'fas fa-meh-rolling-eyes' : 'emoticon face neutral rating',
            'far fa-meh-rolling-eyes' : 'emoticon face neutral rating',
            'fas fa-memory' : 'DIMM RAM',
            'fas fa-menorah' : 'candle hanukkah jewish judaism light',
            'fas fa-mercury' : 'transgender',
            'fas fa-meteor' : '',
            'fas fa-microchip' : 'cpu processor',
            'fas fa-microphone' : 'record sound voice',
            'fas fa-microphone-alt' : 'record sound voice',
            'fas fa-microphone-alt-slash' : 'disable mute record sound voice',
            'fas fa-microphone-slash' : 'disable mute record sound voice',
            'fas fa-microscope' : '',
            'fab fa-microsoft' : '',
            'fas fa-minus' : 'collapse delete hide minify negative remove trash',
            'fas fa-minus-circle' : 'delete hide negative remove trash',
            'fas fa-minus-square' : 'collapse delete hide minify negative remove trash',
            'far fa-minus-square' : 'collapse delete hide minify negative remove trash',
            'fab fa-mix' : '',
            'fab fa-mixcloud' : '',
            'fab fa-mizuni' : '',
            'fas fa-mobile' : 'apple call cell phone cellphone device iphone number screen telephone text',
            'fas fa-mobile-alt' : 'apple call cell phone cellphone device iphone number screen telephone text',
            'fab fa-modx' : '',
            'fab fa-monero' : '',
            'fas fa-money-bill' : 'buy cash checkout money payment price purchase',
            'fas fa-money-bill-alt' : 'buy cash checkout money payment price purchase',
            'far fa-money-bill-alt' : 'buy cash checkout money payment price purchase',
            'fas fa-money-bill-wave' : '',
            'fas fa-money-bill-wave-alt' : '',
            'fas fa-money-check' : 'bank check cheque',
            'fas fa-money-check-alt' : 'bank check cheque',
            'fas fa-monument' : 'building historic memoroable',
            'fas fa-moon' : 'contrast crescent darker lunar night',
            'far fa-moon' : 'contrast crescent darker lunar night',
            'fas fa-mortar-pestle' : 'crush culinary grind medical mix spices',
            'fas fa-mosque' : 'building islam muslim',
            'fas fa-motorcycle' : 'bike machine transportation vehicle',
            'fas fa-mountain' : '',
            'fas fa-mouse-pointer' : 'select',
            'fas fa-music' : 'note sound',
            'fab fa-napster' : '',
            'fab fa-neos' : '',
            'fas fa-network-wired' : '',
            'fas fa-neuter' : '',
            'fas fa-newspaper' : 'article press',
            'far fa-newspaper' : 'article press',
            'fab fa-nimblr' : '',
            'fab fa-nintendo-switch' : '',
            'fab fa-node' : '',
            'fab fa-node-js' : '',
            'fas fa-not-equal' : '',
            'fas fa-notes-medical' : '',
            'fab fa-npm' : '',
            'fab fa-ns8' : '',
            'fab fa-nutritionix' : '',
            'fas fa-object-group' : 'design',
            'far fa-object-group' : 'design',
            'fas fa-object-ungroup' : 'design',
            'far fa-object-ungroup' : 'design',
            'fab fa-odnoklassniki' : '',
            'fab fa-odnoklassniki-square' : '',
            'fas fa-oil-can' : '',
            'fab fa-old-republic' : 'politics star wars',
            'fas fa-om' : 'buddhism hinduism jainism mantra',
            'fab fa-opencart' : '',
            'fab fa-openid' : '',
            'fab fa-opera' : '',
            'fab fa-optin-monster' : '',
            'fab fa-osi' : '',
            'fas fa-otter' : 'fauna mammmal',
            'fas fa-outdent' : '',
            'fab fa-page4' : '',
            'fab fa-pagelines' : 'eco flora leaf leaves nature plant tree',
            'fas fa-paint-brush' : '',
            'fas fa-paint-roller' : 'brush painting tool',
            'fas fa-palette' : 'colors painting',
            'fab fa-palfed' : '',
            'fas fa-pallet' : '',
            'fas fa-paper-plane' : '',
            'far fa-paper-plane' : '',
            'fas fa-paperclip' : 'attachment',
            'fas fa-parachute-box' : 'aid assistance rescue supplies',
            'fas fa-paragraph' : '',
            'fas fa-parking' : '',
            'fas fa-passport' : 'document identification issued',
            'fas fa-pastafarianism' : 'agnosticism atheism flying spaghetti monster fsm',
            'fas fa-paste' : 'clipboard copy',
            'fab fa-patreon' : '',
            'fas fa-pause' : 'wait',
            'fas fa-pause-circle' : '',
            'far fa-pause-circle' : '',
            'fas fa-paw' : 'animal pet',
            'fab fa-paypal' : '',
            'fas fa-peace' : '',
            'fas fa-pen' : 'design edit update write',
            'fas fa-pen-alt' : 'design edit update write',
            'fas fa-pen-fancy' : 'design edit fountain pen update write',
            'fas fa-pen-nib' : 'design edit fountain pen update write',
            'fas fa-pen-square' : 'edit pencil-square update write',
            'fas fa-pencil-alt' : 'design edit pencil update write',
            'fas fa-pencil-ruler' : '',
            'fab fa-penny-arcade' : 'Dungeons & Dragons d&d dnd fantasy game gaming pax tabletop',
            'fas fa-people-carry' : 'movers',
            'fas fa-percent' : '',
            'fas fa-percentage' : '',
            'fab fa-periscope' : '',
            'fas fa-person-booth' : 'changing changing room election human person vote voting',
            'fab fa-phabricator' : '',
            'fab fa-phoenix-framework' : '',
            'fab fa-phoenix-squadron' : '',
            'fas fa-phone' : 'call earphone number support telephone voice',
            'fas fa-phone-slash' : '',
            'fas fa-phone-square' : 'call number support telephone voice',
            'fas fa-phone-volume' : 'telephone volume-control-phone',
            'fab fa-php' : '',
            'fab fa-pied-piper' : '',
            'fab fa-pied-piper-alt' : '',
            'fab fa-pied-piper-hat' : 'clothing',
            'fab fa-pied-piper-pp' : '',
            'fas fa-piggy-bank' : 'save savings',
            'fas fa-pills' : 'drugs medicine',
            'fab fa-pinterest' : '',
            'fab fa-pinterest-p' : '',
            'fab fa-pinterest-square' : '',
            'fas fa-place-of-worship' : '',
            'fas fa-plane' : 'airplane destination fly location mode travel trip',
            'fas fa-plane-arrival' : 'airplane arriving destination fly land landing location mode travel trip',
            'fas fa-plane-departure' : 'airplane departing destination fly location mode take off taking off travel trip',
            'fas fa-play' : 'music playing sound start',
            'fas fa-play-circle' : 'playing start',
            'far fa-play-circle' : 'playing start',
            'fab fa-playstation' : '',
            'fas fa-plug' : 'connect online power',
            'fas fa-plus' : 'add create expand new positive',
            'fas fa-plus-circle' : 'add create expand new positive',
            'fas fa-plus-square' : 'add create expand new positive',
            'far fa-plus-square' : 'add create expand new positive',
            'fas fa-podcast' : '',
            'fas fa-poll' : 'results survey vote voting',
            'fas fa-poll-h' : 'results survey vote voting',
            'fas fa-poo' : '',
            'fas fa-poo-storm' : 'mess poop shit',
            'fas fa-poop' : '',
            'fas fa-portrait' : '',
            'fas fa-pound-sign' : 'gbp',
            'fas fa-power-off' : 'on reboot restart',
            'fas fa-pray' : '',
            'fas fa-praying-hands' : '',
            'fas fa-prescription' : 'drugs medical medicine rx',
            'fas fa-prescription-bottle' : 'drugs medical medicine rx',
            'fas fa-prescription-bottle-alt' : 'drugs medical medicine rx',
            'fas fa-print' : '',
            'fas fa-procedures' : '',
            'fab fa-product-hunt' : '',
            'fas fa-project-diagram' : '',
            'fab fa-pushed' : '',
            'fas fa-puzzle-piece' : 'add-on addon section',
            'fab fa-python' : '',
            'fab fa-qq' : '',
            'fas fa-qrcode' : 'scan',
            'fas fa-question' : 'help information support unknown',
            'fas fa-question-circle' : 'help information support unknown',
            'far fa-question-circle' : 'help information support unknown',
            'fas fa-quidditch' : '',
            'fab fa-quinscape' : '',
            'fab fa-quora' : '',
            'fas fa-quote-left' : '',
            'fas fa-quote-right' : '',
            'fas fa-quran' : 'book islam muslim',
            'fab fa-r-project' : '',
            'fas fa-rainbow' : '',
            'fas fa-random' : 'shuffle sort',
            'fab fa-ravelry' : '',
            'fab fa-react' : '',
            'fab fa-reacteurope' : '',
            'fab fa-readme' : '',
            'fab fa-rebel' : '',
            'fas fa-receipt' : 'check invoice table',
            'fas fa-recycle' : '',
            'fab fa-red-river' : '',
            'fab fa-reddit' : '',
            'fab fa-reddit-alien' : '',
            'fab fa-reddit-square' : '',
            'fas fa-redo' : 'forward refresh reload repeat',
            'fas fa-redo-alt' : 'forward refresh reload repeat',
            'fas fa-registered' : '',
            'far fa-registered' : '',
            'fab fa-renren' : '',
            'fas fa-reply' : '',
            'fas fa-reply-all' : '',
            'fab fa-replyd' : '',
            'fas fa-republican' : 'american conservative election elephant politics republican party right right-wing usa',
            'fab fa-researchgate' : '',
            'fab fa-resolving' : '',
            'fas fa-retweet' : 'refresh reload share swap',
            'fab fa-rev' : '',
            'fas fa-ribbon' : 'badge cause lapel pin',
            'fas fa-ring' : 'Dungeons & Dragons Gollum band binding d&d dnd fantasy jewelry precious',
            'fas fa-road' : 'street',
            'fas fa-robot' : '',
            'fas fa-rocket' : 'app',
            'fab fa-rocketchat' : '',
            'fab fa-rockrms' : '',
            'fas fa-route' : '',
            'fas fa-rss' : 'blog',
            'fas fa-rss-square' : 'blog feed',
            'fas fa-ruble-sign' : 'rub',
            'fas fa-ruler' : '',
            'fas fa-ruler-combined' : '',
            'fas fa-ruler-horizontal' : '',
            'fas fa-ruler-vertical' : '',
            'fas fa-running' : 'jog sprint',
            'fas fa-rupee-sign' : 'indian inr',
            'fas fa-sad-cry' : 'emoticon face tear tears',
            'far fa-sad-cry' : 'emoticon face tear tears',
            'fas fa-sad-tear' : 'emoticon face tear tears',
            'far fa-sad-tear' : 'emoticon face tear tears',
            'fab fa-safari' : 'browser',
            'fab fa-sass' : '',
            'fas fa-save' : 'floppy floppy-o',
            'far fa-save' : 'floppy floppy-o',
            'fab fa-schlix' : '',
            'fas fa-school' : '',
            'fas fa-screwdriver' : 'admin fix repair settings tool',
            'fab fa-scribd' : '',
            'fas fa-scroll' : 'Dungeons & Dragons announcement d&d dnd fantasy paper',
            'fas fa-search' : 'bigger enlarge magnify preview zoom',
            'fas fa-search-dollar' : '',
            'fas fa-search-location' : '',
            'fas fa-search-minus' : 'minify negative smaller zoom zoom out',
            'fas fa-search-plus' : 'bigger enlarge magnify positive zoom zoom in',
            'fab fa-searchengin' : '',
            'fas fa-seedling' : '',
            'fab fa-sellcast' : 'eercast',
            'fab fa-sellsy' : '',
            'fas fa-server' : 'cpu',
            'fab fa-servicestack' : '',
            'fas fa-shapes' : 'circle square triangle',
            'fas fa-share' : '',
            'fas fa-share-alt' : '',
            'fas fa-share-alt-square' : '',
            'fas fa-share-square' : 'send social',
            'far fa-share-square' : 'send social',
            'fas fa-shekel-sign' : 'ils',
            'fas fa-shield-alt' : 'achievement award block defend security winner',
            'fas fa-ship' : 'boat sea',
            'fas fa-shipping-fast' : '',
            'fab fa-shirtsinbulk' : '',
            'fas fa-shoe-prints' : 'feet footprints steps',
            'fas fa-shopping-bag' : '',
            'fas fa-shopping-basket' : '',
            'fas fa-shopping-cart' : 'buy checkout payment purchase',
            'fab fa-shopware' : '',
            'fas fa-shower' : '',
            'fas fa-shuttle-van' : 'machine public-transportation transportation vehicle',
            'fas fa-sign' : '',
            'fas fa-sign-in-alt' : 'arrow enter join log in login sign in sign up sign-in signin signup',
            'fas fa-sign-language' : '',
            'fas fa-sign-out-alt' : 'arrow exit leave log out logout sign-out',
            'fas fa-signal' : 'bars graph online status',
            'fas fa-signature' : 'John Hancock cursive name writing',
            'fab fa-simplybuilt' : '',
            'fab fa-sistrix' : '',
            'fas fa-sitemap' : 'directory hierarchy ia information architecture organization',
            'fab fa-sith' : '',
            'fas fa-skull' : 'bones skeleton yorick',
            'fas fa-skull-crossbones' : 'Dungeons & Dragons alert bones d&d danger dead deadly death dnd fantasy halloween holiday jolly-roger pirate poison skeleton warning',
            'fab fa-skyatlas' : '',
            'fab fa-skype' : '',
            'fab fa-slack' : 'anchor hash hashtag',
            'fab fa-slack-hash' : 'anchor hash hashtag',
            'fas fa-slash' : '',
            'fas fa-sliders-h' : 'settings sliders',
            'fab fa-slideshare' : '',
            'fas fa-smile' : 'approve emoticon face happy rating satisfied',
            'far fa-smile' : 'approve emoticon face happy rating satisfied',
            'fas fa-smile-beam' : 'emoticon face happy positive',
            'far fa-smile-beam' : 'emoticon face happy positive',
            'fas fa-smile-wink' : 'emoticon face happy',
            'far fa-smile-wink' : 'emoticon face happy',
            'fas fa-smog' : 'dragon',
            'fas fa-smoking' : 'cigarette nicotine smoking status',
            'fas fa-smoking-ban' : 'no smoking non-smoking',
            'fab fa-snapchat' : '',
            'fab fa-snapchat-ghost' : '',
            'fab fa-snapchat-square' : '',
            'fas fa-snowflake' : 'precipitation seasonal winter',
            'far fa-snowflake' : 'precipitation seasonal winter',
            'fas fa-socks' : 'business socks business time flight of the conchords wednesday',
            'fas fa-solar-panel' : 'clean eco-friendly energy green sun',
            'fas fa-sort' : 'order',
            'fas fa-sort-alpha-down' : 'sort-alpha-asc',
            'fas fa-sort-alpha-up' : 'sort-alpha-desc',
            'fas fa-sort-amount-down' : 'sort-amount-asc',
            'fas fa-sort-amount-up' : 'sort-amount-desc',
            'fas fa-sort-down' : 'arrow descending sort-desc',
            'fas fa-sort-numeric-down' : 'numbers sort-numeric-asc',
            'fas fa-sort-numeric-up' : 'numbers sort-numeric-desc',
            'fas fa-sort-up' : 'arrow ascending sort-asc',
            'fab fa-soundcloud' : '',
            'fas fa-spa' : 'flora mindfullness plant wellness',
            'fas fa-space-shuttle' : 'astronaut machine nasa rocket transportation',
            'fab fa-speakap' : '',
            'fas fa-spider' : 'arachnid bug charlotte crawl eight halloween holiday',
            'fas fa-spinner' : 'loading progress',
            'fas fa-splotch' : '',
            'fab fa-spotify' : '',
            'fas fa-spray-can' : '',
            'fas fa-square' : 'block box',
            'far fa-square' : 'block box',
            'fas fa-square-full' : '',
            'fas fa-square-root-alt' : '',
            'fab fa-squarespace' : '',
            'fab fa-stack-exchange' : '',
            'fab fa-stack-overflow' : '',
            'fas fa-stamp' : '',
            'fas fa-star' : 'achievement award favorite important night rating score',
            'far fa-star' : 'achievement award favorite important night rating score',
            'fas fa-star-and-crescent' : 'islam muslim',
            'fas fa-star-half' : 'achievement award rating score star-half-empty star-half-full',
            'far fa-star-half' : 'achievement award rating score star-half-empty star-half-full',
            'fas fa-star-half-alt' : 'achievement award rating score star-half-empty star-half-full',
            'fas fa-star-of-david' : 'jewish judaism',
            'fas fa-star-of-life' : '',
            'fab fa-staylinked' : '',
            'fab fa-steam' : '',
            'fab fa-steam-square' : '',
            'fab fa-steam-symbol' : '',
            'fas fa-step-backward' : 'beginning first previous rewind start',
            'fas fa-step-forward' : 'end last next',
            'fas fa-stethoscope' : '',
            'fab fa-sticker-mule' : '',
            'fas fa-sticky-note' : '',
            'far fa-sticky-note' : '',
            'fas fa-stop' : 'block box square',
            'fas fa-stop-circle' : '',
            'far fa-stop-circle' : '',
            'fas fa-stopwatch' : 'time',
            'fas fa-store' : '',
            'fas fa-store-alt' : '',
            'fab fa-strava' : '',
            'fas fa-stream' : '',
            'fas fa-street-view' : 'map',
            'fas fa-strikethrough' : '',
            'fab fa-stripe' : '',
            'fab fa-stripe-s' : '',
            'fas fa-stroopwafel' : 'dessert food sweets waffle',
            'fab fa-studiovinari' : '',
            'fab fa-stumbleupon' : '',
            'fab fa-stumbleupon-circle' : '',
            'fas fa-subscript' : '',
            'fas fa-subway' : 'machine railway train transportation vehicle',
            'fas fa-suitcase' : 'baggage luggage move suitcase travel trip',
            'fas fa-suitcase-rolling' : '',
            'fas fa-sun' : 'brighten contrast day lighter sol solar star weather',
            'far fa-sun' : 'brighten contrast day lighter sol solar star weather',
            'fab fa-superpowers' : '',
            'fas fa-superscript' : 'exponential',
            'fab fa-supple' : '',
            'fas fa-surprise' : 'emoticon face shocked',
            'far fa-surprise' : 'emoticon face shocked',
            'fas fa-swatchbook' : '',
            'fas fa-swimmer' : 'athlete head man person water',
            'fas fa-swimming-pool' : 'ladder recreation water',
            'fas fa-synagogue' : 'building jewish judaism star of david temple',
            'fas fa-sync' : 'exchange refresh reload rotate swap',
            'fas fa-sync-alt' : 'refresh reload rotate',
            'fas fa-syringe' : 'immunizations needle',
            'fas fa-table' : 'data excel spreadsheet',
            'fas fa-table-tennis' : '',
            'fas fa-tablet' : 'apple device ipad kindle screen',
            'fas fa-tablet-alt' : 'apple device ipad kindle screen',
            'fas fa-tablets' : 'drugs medicine',
            'fas fa-tachometer-alt' : 'dashboard tachometer',
            'fas fa-tag' : 'label',
            'fas fa-tags' : 'labels',
            'fas fa-tape' : '',
            'fas fa-tasks' : 'downloading downloads loading progress settings',
            'fas fa-taxi' : 'cab cabbie car car service lyft machine transportation uber vehicle',
            'fab fa-teamspeak' : '',
            'fas fa-teeth' : '',
            'fas fa-teeth-open' : '',
            'fab fa-telegram' : '',
            'fab fa-telegram-plane' : '',
            'fas fa-temperature-high' : 'mercury thermometer warm',
            'fas fa-temperature-low' : 'cool mercury thermometer',
            'fab fa-tencent-weibo' : '',
            'fas fa-terminal' : 'code command console prompt',
            'fas fa-text-height' : '',
            'fas fa-text-width' : '',
            'fas fa-th' : 'blocks boxes grid squares',
            'fas fa-th-large' : 'blocks boxes grid squares',
            'fas fa-th-list' : 'checklist completed done finished ol todo ul',
            'fab fa-the-red-yeti' : '',
            'fas fa-theater-masks' : '',
            'fab fa-themeco' : '',
            'fab fa-themeisle' : '',
            'fas fa-thermometer' : 'mercury status temperature',
            'fas fa-thermometer-empty' : 'mercury status temperature',
            'fas fa-thermometer-full' : 'fever mercury status temperature',
            'fas fa-thermometer-half' : 'mercury status temperature',
            'fas fa-thermometer-quarter' : 'mercury status temperature',
            'fas fa-thermometer-three-quarters' : 'mercury status temperature',
            'fab fa-think-peaks' : '',
            'fas fa-thumbs-down' : 'disagree disapprove dislike hand thumbs-o-down',
            'far fa-thumbs-down' : 'disagree disapprove dislike hand thumbs-o-down',
            'fas fa-thumbs-up' : 'agree approve favorite hand like ok okay success thumbs-o-up yes you got it dude',
            'far fa-thumbs-up' : 'agree approve favorite hand like ok okay success thumbs-o-up yes you got it dude',
            'fas fa-thumbtack' : 'coordinates location marker pin thumb-tack',
            'fas fa-ticket-alt' : 'ticket',
            'fas fa-times' : 'close cross error exit incorrect notice notification notify problem wrong x',
            'fas fa-times-circle' : 'close cross exit incorrect notice notification notify problem wrong x',
            'far fa-times-circle' : 'close cross exit incorrect notice notification notify problem wrong x',
            'fas fa-tint' : 'drop droplet raindrop waterdrop',
            'fas fa-tint-slash' : '',
            'fas fa-tired' : 'emoticon face grumpy',
            'far fa-tired' : 'emoticon face grumpy',
            'fas fa-toggle-off' : 'switch',
            'fas fa-toggle-on' : 'switch',
            'fas fa-toilet-paper' : 'bathroom halloween holiday lavatory prank restroom roll',
            'fas fa-toolbox' : 'admin container fix repair settings tools',
            'fas fa-tooth' : 'bicuspid dental molar mouth teeth',
            'fas fa-torah' : 'book jewish judaism',
            'fas fa-torii-gate' : 'building shintoism',
            'fas fa-tractor' : '',
            'fab fa-trade-federation' : '',
            'fas fa-trademark' : '',
            'fas fa-traffic-light' : '',
            'fas fa-train' : 'bullet locomotive railway',
            'fas fa-transgender' : 'intersex',
            'fas fa-transgender-alt' : '',
            'fas fa-trash' : 'delete garbage hide remove',
            'fas fa-trash-alt' : 'delete garbage hide remove trash trash-o',
            'far fa-trash-alt' : 'delete garbage hide remove trash trash-o',
            'fas fa-tree' : 'bark fall flora forest nature plant seasonal',
            'fab fa-trello' : '',
            'fab fa-tripadvisor' : '',
            'fas fa-trophy' : 'achievement award cup game winner',
            'fas fa-truck' : 'delivery shipping',
            'fas fa-truck-loading' : '',
            'fas fa-truck-monster' : '',
            'fas fa-truck-moving' : '',
            'fas fa-truck-pickup' : '',
            'fas fa-tshirt' : 'cloth clothing',
            'fas fa-tty' : '',
            'fab fa-tumblr' : '',
            'fab fa-tumblr-square' : '',
            'fas fa-tv' : 'computer display monitor television',
            'fab fa-twitch' : '',
            'fab fa-twitter' : 'social network tweet',
            'fab fa-twitter-square' : 'social network tweet',
            'fab fa-typo3' : '',
            'fab fa-uber' : '',
            'fab fa-uikit' : '',
            'fas fa-umbrella' : 'protection rain',
            'fas fa-umbrella-beach' : 'protection recreation sun',
            'fas fa-underline' : '',
            'fas fa-undo' : 'back control z exchange oops return rotate swap',
            'fas fa-undo-alt' : 'back control z exchange oops return swap',
            'fab fa-uniregistry' : '',
            'fas fa-universal-access' : '',
            'fas fa-university' : 'bank institution',
            'fas fa-unlink' : 'chain chain-broken remove',
            'fas fa-unlock' : 'admin lock password protect',
            'fas fa-unlock-alt' : 'admin lock password protect',
            'fab fa-untappd' : '',
            'fas fa-upload' : 'export publish',
            'fab fa-usb' : '',
            'fas fa-user' : 'account avatar head human man person profile',
            'far fa-user' : 'account avatar head human man person profile',
            'fas fa-user-alt' : 'account avatar head human man person profile',
            'fas fa-user-alt-slash' : '',
            'fas fa-user-astronaut' : 'avatar clothing cosmonaut space suit',
            'fas fa-user-check' : '',
            'fas fa-user-circle' : 'account avatar head human man person profile',
            'far fa-user-circle' : 'account avatar head human man person profile',
            'fas fa-user-clock' : '',
            'fas fa-user-cog' : '',
            'fas fa-user-edit' : '',
            'fas fa-user-friends' : '',
            'fas fa-user-graduate' : 'cap clothing commencement gown graduation student',
            'fas fa-user-injured' : 'cast ouch sling',
            'fas fa-user-lock' : '',
            'fas fa-user-md' : 'doctor job medical nurse occupation profile',
            'fas fa-user-minus' : 'delete negative remove',
            'fas fa-user-ninja' : 'assassin avatar dangerous deadly sneaky',
            'fas fa-user-plus' : 'positive sign up signup',
            'fas fa-user-secret' : 'clothing coat hat incognito privacy spy whisper',
            'fas fa-user-shield' : '',
            'fas fa-user-slash' : 'ban remove',
            'fas fa-user-tag' : '',
            'fas fa-user-tie' : 'avatar business clothing formal',
            'fas fa-user-times' : 'archive delete remove x',
            'fas fa-users' : 'people persons profiles',
            'fas fa-users-cog' : '',
            'fab fa-ussunnah' : '',
            'fas fa-utensil-spoon' : 'spoon',
            'fas fa-utensils' : 'cutlery dinner eat food knife restaurant spoon',
            'fab fa-vaadin' : '',
            'fas fa-vector-square' : 'anchors lines object',
            'fas fa-venus' : 'female',
            'fas fa-venus-double' : '',
            'fas fa-venus-mars' : '',
            'fab fa-viacoin' : '',
            'fab fa-viadeo' : '',
            'fab fa-viadeo-square' : '',
            'fas fa-vial' : 'test tube',
            'fas fa-vials' : 'lab results test tubes',
            'fab fa-viber' : '',
            'fas fa-video' : 'camera film movie record video-camera',
            'fas fa-video-slash' : '',
            'fas fa-vihara' : 'buddhism buddhist building monastery',
            'fab fa-vimeo' : '',
            'fab fa-vimeo-square' : '',
            'fab fa-vimeo-v' : 'vimeo',
            'fab fa-vine' : '',
            'fab fa-vk' : '',
            'fab fa-vnv' : '',
            'fas fa-volleyball-ball' : '',
            'fas fa-volume-down' : 'audio lower music quieter sound speaker',
            'fas fa-volume-mute' : '',
            'fas fa-volume-off' : 'audio music mute sound',
            'fas fa-volume-up' : 'audio higher louder music sound speaker',
            'fas fa-vote-yea' : 'accept cast election politics positive yes',
            'fas fa-vr-cardboard' : 'google reality virtual',
            'fab fa-vuejs' : '',
            'fas fa-walking' : '',
            'fas fa-wallet' : '',
            'fas fa-warehouse' : '',
            'fas fa-water' : '',
            'fab fa-weebly' : '',
            'fab fa-weibo' : '',
            'fas fa-weight' : 'measurement scale weight',
            'fas fa-weight-hanging' : 'anvil heavy measurement',
            'fab fa-weixin' : '',
            'fab fa-whatsapp' : '',
            'fab fa-whatsapp-square' : '',
            'fas fa-wheelchair' : 'handicap person',
            'fab fa-whmcs' : '',
            'fas fa-wifi' : '',
            'fab fa-wikipedia-w' : '',
            'fas fa-wind' : 'air blow breeze fall seasonal',
            'fas fa-window-close' : '',
            'far fa-window-close' : '',
            'fas fa-window-maximize' : '',
            'far fa-window-maximize' : '',
            'fas fa-window-minimize' : '',
            'far fa-window-minimize' : '',
            'fas fa-window-restore' : '',
            'far fa-window-restore' : '',
            'fab fa-windows' : 'microsoft',
            'fas fa-wine-bottle' : 'alcohol beverage drink glass grapes',
            'fas fa-wine-glass' : 'alcohol beverage drink grapes',
            'fas fa-wine-glass-alt' : 'alcohol beverage drink grapes',
            'fab fa-wix' : '',
            'fab fa-wizards-of-the-coast' : 'Dungeons & Dragons d&d dnd fantasy game gaming tabletop',
            'fab fa-wolf-pack-battalion' : '',
            'fas fa-won-sign' : 'krw',
            'fab fa-wordpress' : '',
            'fab fa-wordpress-simple' : '',
            'fab fa-wpbeginner' : '',
            'fab fa-wpexplorer' : '',
            'fab fa-wpforms' : '',
            'fab fa-wpressr' : 'rendact',
            'fas fa-wrench' : 'fix settings spanner tool update',
            'fas fa-x-ray' : 'radiological images radiology',
            'fab fa-xbox' : '',
            'fab fa-xing' : '',
            'fab fa-xing-square' : '',
            'fab fa-y-combinator' : '',
            'fab fa-yahoo' : '',
            'fab fa-yandex' : '',
            'fab fa-yandex-international' : '',
            'fab fa-yelp' : '',
            'fas fa-yen-sign' : 'jpy money',
            'fas fa-yin-yang' : 'daoism opposites taoism',
            'fab fa-yoast' : '',
            'fab fa-youtube' : 'film video youtube-play youtube-square',
            'fab fa-youtube-square' : '',
            'fab fa-zhihu' : ''
        },
        "Material Design Iconic" : {
            'zmdi zmdi-3d-rotation' : '',
            'zmdi zmdi-airplane-off' : '',
            'zmdi zmdi-airplane' : '',
            'zmdi zmdi-album' : '',
            'zmdi zmdi-archive' : '',
            'zmdi zmdi-assignment-account' : '',
            'zmdi zmdi-assignment-alert' : '',
            'zmdi zmdi-assignment-check' : '',
            'zmdi zmdi-assignment-o' : '',
            'zmdi zmdi-assignment-return' : '',
            'zmdi zmdi-assignment-returned' : '',
            'zmdi zmdi-assignment' : '',
            'zmdi zmdi-attachment-alt' : '',
            'zmdi zmdi-attachment' : '',
            'zmdi zmdi-audio' : '',
            'zmdi zmdi-badge-check' : '',
            'zmdi zmdi-balance-wallet' : '',
            'zmdi zmdi-balance' : '',
            'zmdi zmdi-battery-alert' : '',
            'zmdi zmdi-battery-flash' : '',
            'zmdi zmdi-battery-unknown' : '',
            'zmdi zmdi-battery' : '',
            'zmdi zmdi-bike' : '',
            'zmdi zmdi-block-alt' : '',
            'zmdi zmdi-block' : '',
            'zmdi zmdi-boat' : '',
            'zmdi zmdi-book-image' : '',
            'zmdi zmdi-book' : '',
            'zmdi zmdi-bookmark-outline' : '',
            'zmdi zmdi-bookmark' : '',
            'zmdi zmdi-brush' : '',
            'zmdi zmdi-bug' : '',
            'zmdi zmdi-bus' : '',
            'zmdi zmdi-cake' : '',
            'zmdi zmdi-car-taxi' : '',
            'zmdi zmdi-car-wash' : '',
            'zmdi zmdi-car' : '',
            'zmdi zmdi-card-giftcard' : '',
            'zmdi zmdi-card-membership' : '',
            'zmdi zmdi-card-travel' : '',
            'zmdi zmdi-card' : '',
            'zmdi zmdi-case-check' : '',
            'zmdi zmdi-case-download' : '',
            'zmdi zmdi-case-play' : '',
            'zmdi zmdi-case' : '',
            'zmdi zmdi-cast-connected' : '',
            'zmdi zmdi-cast' : '',
            'zmdi zmdi-chart-donut' : '',
            'zmdi zmdi-chart' : '',
            'zmdi zmdi-city-alt' : '',
            'zmdi zmdi-city' : '',
            'zmdi zmdi-close-circle-o' : '',
            'zmdi zmdi-close-circle' : '',
            'zmdi zmdi-close' : '',
            'zmdi zmdi-cocktail' : '',
            'zmdi zmdi-code-setting' : '',
            'zmdi zmdi-code-smartphone' : '',
            'zmdi zmdi-code' : '',
            'zmdi zmdi-coffee' : '',
            'zmdi zmdi-collection-bookmark' : '',
            'zmdi zmdi-collection-case-play' : '',
            'zmdi zmdi-collection-folder-image' : '',
            'zmdi zmdi-collection-image-o' : '',
            'zmdi zmdi-collection-image' : '',
            'zmdi zmdi-collection-item-1' : '',
            'zmdi zmdi-collection-item-2' : '',
            'zmdi zmdi-collection-item-3' : '',
            'zmdi zmdi-collection-item-4' : '',
            'zmdi zmdi-collection-item-5' : '',
            'zmdi zmdi-collection-item-6' : '',
            'zmdi zmdi-collection-item-7' : '',
            'zmdi zmdi-collection-item-8' : '',
            'zmdi zmdi-collection-item-9-plus' : '',
            'zmdi zmdi-collection-item-9' : '',
            'zmdi zmdi-collection-item' : '',
            'zmdi zmdi-collection-music' : '',
            'zmdi zmdi-collection-pdf' : '',
            'zmdi zmdi-collection-plus' : '',
            'zmdi zmdi-collection-speaker' : '',
            'zmdi zmdi-collection-text' : '',
            'zmdi zmdi-collection-video' : '',
            'zmdi zmdi-compass' : '',
            'zmdi zmdi-cutlery' : '',
            'zmdi zmdi-delete' : '',
            'zmdi zmdi-dialpad' : '',
            'zmdi zmdi-dns' : '',
            'zmdi zmdi-drink' : '',
            'zmdi zmdi-edit' : '',
            'zmdi zmdi-email-open' : '',
            'zmdi zmdi-email' : '',
            'zmdi zmdi-eye-off' : '',
            'zmdi zmdi-eye' : '',
            'zmdi zmdi-eyedropper' : '',
            'zmdi zmdi-favorite-outline' : '',
            'zmdi zmdi-favorite' : '',
            'zmdi zmdi-filter-list' : '',
            'zmdi zmdi-fire' : '',
            'zmdi zmdi-flag' : '',
            'zmdi zmdi-flare' : '',
            'zmdi zmdi-flash-auto' : '',
            'zmdi zmdi-flash-off' : '',
            'zmdi zmdi-flash' : '',
            'zmdi zmdi-flip' : '',
            'zmdi zmdi-flower-alt' : '',
            'zmdi zmdi-flower' : '',
            'zmdi zmdi-font' : '',
            'zmdi zmdi-fullscreen-alt' : '',
            'zmdi zmdi-fullscreen-exit' : '',
            'zmdi zmdi-fullscreen' : '',
            'zmdi zmdi-functions' : '',
            'zmdi zmdi-gas-station' : '',
            'zmdi zmdi-gesture' : '',
            'zmdi zmdi-globe-alt' : '',
            'zmdi zmdi-globe-lock' : '',
            'zmdi zmdi-globe' : '',
            'zmdi zmdi-graduation-cap' : '',
            'zmdi zmdi-home' : '',
            'zmdi zmdi-hospital-alt' : '',
            'zmdi zmdi-hospital' : '',
            'zmdi zmdi-hotel' : '',
            'zmdi zmdi-hourglass-alt' : '',
            'zmdi zmdi-hourglass-outline' : '',
            'zmdi zmdi-hourglass' : '',
            'zmdi zmdi-http' : '',
            'zmdi zmdi-image-alt' : '',
            'zmdi zmdi-image-o' : '',
            'zmdi zmdi-image' : '',
            'zmdi zmdi-inbox' : '',
            'zmdi zmdi-invert-colors-off' : '',
            'zmdi zmdi-invert-colors' : '',
            'zmdi zmdi-key' : '',
            'zmdi zmdi-label-alt-outline' : '',
            'zmdi zmdi-label-alt' : '',
            'zmdi zmdi-label-heart' : '',
            'zmdi zmdi-label' : '',
            'zmdi zmdi-labels' : '',
            'zmdi zmdi-lamp' : '',
            'zmdi zmdi-landscape' : '',
            'zmdi zmdi-layers-off' : '',
            'zmdi zmdi-layers' : '',
            'zmdi zmdi-library' : '',
            'zmdi zmdi-link' : '',
            'zmdi zmdi-lock-open' : '',
            'zmdi zmdi-lock-outline' : '',
            'zmdi zmdi-lock' : '',
            'zmdi zmdi-mail-reply-all' : '',
            'zmdi zmdi-mail-reply' : '',
            'zmdi zmdi-mail-send' : '',
            'zmdi zmdi-mall' : '',
            'zmdi zmdi-map' : '',
            'zmdi zmdi-menu' : '',
            'zmdi zmdi-money-box' : '',
            'zmdi zmdi-money-off' : '',
            'zmdi zmdi-money' : '',
            'zmdi zmdi-more-vert' : '',
            'zmdi zmdi-more' : '',
            'zmdi zmdi-movie-alt' : '',
            'zmdi zmdi-movie' : '',
            'zmdi zmdi-nature-people' : '',
            'zmdi zmdi-nature' : '',
            'zmdi zmdi-navigation' : '',
            'zmdi zmdi-open-in-browser' : '',
            'zmdi zmdi-open-in-new' : '',
            'zmdi zmdi-palette' : '',
            'zmdi zmdi-parking' : '',
            'zmdi zmdi-pin-account' : '',
            'zmdi zmdi-pin-assistant' : '',
            'zmdi zmdi-pin-drop' : '',
            'zmdi zmdi-pin-help' : '',
            'zmdi zmdi-pin-off' : '',
            'zmdi zmdi-pin' : '',
            'zmdi zmdi-pizza' : '',
            'zmdi zmdi-plaster' : '',
            'zmdi zmdi-power-setting' : '',
            'zmdi zmdi-power' : '',
            'zmdi zmdi-print' : '',
            'zmdi zmdi-puzzle-piece' : '',
            'zmdi zmdi-quote' : '',
            'zmdi zmdi-railway' : '',
            'zmdi zmdi-receipt' : '',
            'zmdi zmdi-refresh-alt' : '',
            'zmdi zmdi-refresh-sync-alert' : '',
            'zmdi zmdi-refresh-sync-off' : '',
            'zmdi zmdi-refresh-sync' : '',
            'zmdi zmdi-refresh' : '',
            'zmdi zmdi-roller' : '',
            'zmdi zmdi-ruler' : '',
            'zmdi zmdi-scissors' : '',
            'zmdi zmdi-screen-rotation-lock' : '',
            'zmdi zmdi-screen-rotation' : '',
            'zmdi zmdi-search-for' : '',
            'zmdi zmdi-search-in-file' : '',
            'zmdi zmdi-search-in-page' : '',
            'zmdi zmdi-search-replace' : '',
            'zmdi zmdi-search' : '',
            'zmdi zmdi-seat' : '',
            'zmdi zmdi-settings-square' : '',
            'zmdi zmdi-settings' : '',
            'zmdi zmdi-shield-check' : '',
            'zmdi zmdi-shield-security' : '',
            'zmdi zmdi-shopping-basket' : '',
            'zmdi zmdi-shopping-cart-plus' : '',
            'zmdi zmdi-shopping-cart' : '',
            'zmdi zmdi-sign-in' : '',
            'zmdi zmdi-sort-amount-asc' : '',
            'zmdi zmdi-sort-amount-desc' : '',
            'zmdi zmdi-sort-asc' : '',
            'zmdi zmdi-sort-desc' : '',
            'zmdi zmdi-spellcheck' : '',
            'zmdi zmdi-storage' : '',
            'zmdi zmdi-store-24' : '',
            'zmdi zmdi-store' : '',
            'zmdi zmdi-subway' : '',
            'zmdi zmdi-sun' : '',
            'zmdi zmdi-tab-unselected' : '',
            'zmdi zmdi-tab' : '',
            'zmdi zmdi-tag-close' : '',
            'zmdi zmdi-tag-more' : '',
            'zmdi zmdi-tag' : '',
            'zmdi zmdi-thumb-down' : '',
            'zmdi zmdi-thumb-up-down' : '',
            'zmdi zmdi-thumb-up' : '',
            'zmdi zmdi-ticket-star' : '',
            'zmdi zmdi-toll' : '',
            'zmdi zmdi-toys' : '',
            'zmdi zmdi-traffic' : '',
            'zmdi zmdi-translate' : '',
            'zmdi zmdi-triangle-down' : '',
            'zmdi zmdi-triangle-up' : '',
            'zmdi zmdi-truck' : '',
            'zmdi zmdi-turning-sign' : '',
            'zmdi zmdi-wallpaper' : '',
            'zmdi zmdi-washing-machine' : '',
            'zmdi zmdi-window-maximize' : '',
            'zmdi zmdi-window-minimize' : '',
            'zmdi zmdi-window-restore' : '',
            'zmdi zmdi-wrench' : '',
            'zmdi zmdi-zoom-in' : '',
            'zmdi zmdi-zoom-out' : '',
            'zmdi zmdi-alert-circle-o' : '',
            'zmdi zmdi-alert-circle' : '',
            'zmdi zmdi-alert-octagon' : '',
            'zmdi zmdi-alert-polygon' : '',
            'zmdi zmdi-alert-triangle' : '',
            'zmdi zmdi-help-outline' : '',
            'zmdi zmdi-help' : '',
            'zmdi zmdi-info-outline' : '',
            'zmdi zmdi-info' : '',
            'zmdi zmdi-notifications-active' : '',
            'zmdi zmdi-notifications-add' : '',
            'zmdi zmdi-notifications-none' : '',
            'zmdi zmdi-notifications-off' : '',
            'zmdi zmdi-notifications-paused' : '',
            'zmdi zmdi-notifications' : '',
            'zmdi zmdi-account-add' : '',
            'zmdi zmdi-account-box-mail' : '',
            'zmdi zmdi-account-box-o' : '',
            'zmdi zmdi-account-box-phone' : '',
            'zmdi zmdi-account-box' : '',
            'zmdi zmdi-account-calendar' : '',
            'zmdi zmdi-account-circle' : '',
            'zmdi zmdi-account-o' : '',
            'zmdi zmdi-account' : '',
            'zmdi zmdi-accounts-add' : '',
            'zmdi zmdi-accounts-alt' : '',
            'zmdi zmdi-accounts-list-alt' : '',
            'zmdi zmdi-accounts-list' : '',
            'zmdi zmdi-accounts-outline' : '',
            'zmdi zmdi-accounts' : '',
            'zmdi zmdi-face' : '',
            'zmdi zmdi-female' : '',
            'zmdi zmdi-male-alt' : '',
            'zmdi zmdi-male-female' : '',
            'zmdi zmdi-male' : '',
            'zmdi zmdi-mood-bad' : '',
            'zmdi zmdi-mood' : '',
            'zmdi zmdi-run' : '',
            'zmdi zmdi-walk' : '',
            'zmdi zmdi-cloud-box' : '',
            'zmdi zmdi-cloud-circle' : '',
            'zmdi zmdi-cloud-done' : '',
            'zmdi zmdi-cloud-download' : '',
            'zmdi zmdi-cloud-off' : '',
            'zmdi zmdi-cloud-outline-alt' : '',
            'zmdi zmdi-cloud-outline' : '',
            'zmdi zmdi-cloud-upload' : '',
            'zmdi zmdi-cloud' : '',
            'zmdi zmdi-download' : '',
            'zmdi zmdi-file-plus' : '',
            'zmdi zmdi-file-text' : '',
            'zmdi zmdi-file' : '',
            'zmdi zmdi-folder-outline' : '',
            'zmdi zmdi-folder-person' : '',
            'zmdi zmdi-folder-star-alt' : '',
            'zmdi zmdi-folder-star' : '',
            'zmdi zmdi-folder' : '',
            'zmdi zmdi-gif' : '',
            'zmdi zmdi-upload' : '',
            'zmdi zmdi-border-all' : '',
            'zmdi zmdi-border-bottom' : '',
            'zmdi zmdi-border-clear' : '',
            'zmdi zmdi-border-color' : '',
            'zmdi zmdi-border-horizontal' : '',
            'zmdi zmdi-border-inner' : '',
            'zmdi zmdi-border-left' : '',
            'zmdi zmdi-border-outer' : '',
            'zmdi zmdi-border-right' : '',
            'zmdi zmdi-border-style' : '',
            'zmdi zmdi-border-top' : '',
            'zmdi zmdi-border-vertical' : '',
            'zmdi zmdi-copy' : '',
            'zmdi zmdi-crop' : '',
            'zmdi zmdi-format-align-center' : '',
            'zmdi zmdi-format-align-justify' : '',
            'zmdi zmdi-format-align-left' : '',
            'zmdi zmdi-format-align-right' : '',
            'zmdi zmdi-format-bold' : '',
            'zmdi zmdi-format-clear-all' : '',
            'zmdi zmdi-format-clear' : '',
            'zmdi zmdi-format-color-fill' : '',
            'zmdi zmdi-format-color-reset' : '',
            'zmdi zmdi-format-color-text' : '',
            'zmdi zmdi-format-indent-decrease' : '',
            'zmdi zmdi-format-indent-increase' : '',
            'zmdi zmdi-format-italic' : '',
            'zmdi zmdi-format-line-spacing' : '',
            'zmdi zmdi-format-list-bulleted' : '',
            'zmdi zmdi-format-list-numbered' : '',
            'zmdi zmdi-format-ltr' : '',
            'zmdi zmdi-format-rtl' : '',
            'zmdi zmdi-format-size' : '',
            'zmdi zmdi-format-strikethrough-s' : '',
            'zmdi zmdi-format-strikethrough' : '',
            'zmdi zmdi-format-subject' : '',
            'zmdi zmdi-format-underlined' : '',
            'zmdi zmdi-format-valign-bottom' : '',
            'zmdi zmdi-format-valign-center' : '',
            'zmdi zmdi-format-valign-top' : '',
            'zmdi zmdi-redo' : '',
            'zmdi zmdi-select-all' : '',
            'zmdi zmdi-space-bar' : '',
            'zmdi zmdi-text-format' : '',
            'zmdi zmdi-transform' : '',
            'zmdi zmdi-undo' : '',
            'zmdi zmdi-wrap-text' : '',
            'zmdi zmdi-comment-alert' : '',
            'zmdi zmdi-comment-alt-text' : '',
            'zmdi zmdi-comment-alt' : '',
            'zmdi zmdi-comment-edit' : '',
            'zmdi zmdi-comment-image' : '',
            'zmdi zmdi-comment-list' : '',
            'zmdi zmdi-comment-more' : '',
            'zmdi zmdi-comment-outline' : '',
            'zmdi zmdi-comment-text-alt' : '',
            'zmdi zmdi-comment-text' : '',
            'zmdi zmdi-comment-video' : '',
            'zmdi zmdi-comment' : '',
            'zmdi zmdi-comments' : '',
            'zmdi zmdi-check-all' : '',
            'zmdi zmdi-check-circle-u' : '',
            'zmdi zmdi-check-circle' : '',
            'zmdi zmdi-check-square' : '',
            'zmdi zmdi-check' : '',
            'zmdi zmdi-circle-o' : '',
            'zmdi zmdi-circle' : '',
            'zmdi zmdi-dot-circle-alt' : '',
            'zmdi zmdi-dot-circle' : '',
            'zmdi zmdi-minus-circle-outline' : '',
            'zmdi zmdi-minus-circle' : '',
            'zmdi zmdi-minus-square' : '',
            'zmdi zmdi-minus' : '',
            'zmdi zmdi-plus-circle-o-duplicate' : '',
            'zmdi zmdi-plus-circle-o' : '',
            'zmdi zmdi-plus-circle' : '',
            'zmdi zmdi-plus-square' : '',
            'zmdi zmdi-plus' : '',
            'zmdi zmdi-square-o' : '',
            'zmdi zmdi-star-circle' : '',
            'zmdi zmdi-star-half' : '',
            'zmdi zmdi-star-outline' : '',
            'zmdi zmdi-star' : '',
            'zmdi zmdi-bluetooth-connected' : '',
            'zmdi zmdi-bluetooth-off' : '',
            'zmdi zmdi-bluetooth-search' : '',
            'zmdi zmdi-bluetooth-setting' : '',
            'zmdi zmdi-bluetooth' : '',
            'zmdi zmdi-camera-add' : '',
            'zmdi zmdi-camera-alt' : '',
            'zmdi zmdi-camera-bw' : '',
            'zmdi zmdi-camera-front' : '',
            'zmdi zmdi-camera-mic' : '',
            'zmdi zmdi-camera-party-mode' : '',
            'zmdi zmdi-camera-rear' : '',
            'zmdi zmdi-camera-roll' : '',
            'zmdi zmdi-camera-switch' : '',
            'zmdi zmdi-camera' : '',
            'zmdi zmdi-card-alert' : '',
            'zmdi zmdi-card-off' : '',
            'zmdi zmdi-card-sd' : '',
            'zmdi zmdi-card-sim' : '',
            'zmdi zmdi-desktop-mac' : '',
            'zmdi zmdi-desktop-windows' : '',
            'zmdi zmdi-device-hub' : '',
            'zmdi zmdi-devices-off' : '',
            'zmdi zmdi-devices' : '',
            'zmdi zmdi-dock' : '',
            'zmdi zmdi-floppy' : '',
            'zmdi zmdi-gamepad' : '',
            'zmdi zmdi-gps-dot' : '',
            'zmdi zmdi-gps-off' : '',
            'zmdi zmdi-gps' : '',
            'zmdi zmdi-headset-mic' : '',
            'zmdi zmdi-headset' : '',
            'zmdi zmdi-input-antenna' : '',
            'zmdi zmdi-input-composite' : '',
            'zmdi zmdi-input-hdmi' : '',
            'zmdi zmdi-input-power' : '',
            'zmdi zmdi-input-svideo' : '',
            'zmdi zmdi-keyboard-hide' : '',
            'zmdi zmdi-keyboard' : '',
            'zmdi zmdi-laptop-chromebook' : '',
            'zmdi zmdi-laptop-mac' : '',
            'zmdi zmdi-laptop' : '',
            'zmdi zmdi-mic-off' : '',
            'zmdi zmdi-mic-outline' : '',
            'zmdi zmdi-mic-setting' : '',
            'zmdi zmdi-mic' : '',
            'zmdi zmdi-mouse' : '',
            'zmdi zmdi-network-alert' : '',
            'zmdi zmdi-network-locked' : '',
            'zmdi zmdi-network-off' : '',
            'zmdi zmdi-network-outline' : '',
            'zmdi zmdi-network-setting' : '',
            'zmdi zmdi-network' : '',
            'zmdi zmdi-phone-bluetooth' : '',
            'zmdi zmdi-phone-end' : '',
            'zmdi zmdi-phone-forwarded' : '',
            'zmdi zmdi-phone-in-talk' : '',
            'zmdi zmdi-phone-locked' : '',
            'zmdi zmdi-phone-missed' : '',
            'zmdi zmdi-phone-msg' : '',
            'zmdi zmdi-phone-paused' : '',
            'zmdi zmdi-phone-ring' : '',
            'zmdi zmdi-phone-setting' : '',
            'zmdi zmdi-phone-sip' : '',
            'zmdi zmdi-phone' : '',
            'zmdi zmdi-portable-wifi-changes' : '',
            'zmdi zmdi-portable-wifi-off' : '',
            'zmdi zmdi-portable-wifi' : '',
            'zmdi zmdi-radio' : '',
            'zmdi zmdi-reader' : '',
            'zmdi zmdi-remote-control-alt' : '',
            'zmdi zmdi-remote-control' : '',
            'zmdi zmdi-router' : '',
            'zmdi zmdi-scanner' : '',
            'zmdi zmdi-smartphone-android' : '',
            'zmdi zmdi-smartphone-download' : '',
            'zmdi zmdi-smartphone-erase' : '',
            'zmdi zmdi-smartphone-info' : '',
            'zmdi zmdi-smartphone-iphone' : '',
            'zmdi zmdi-smartphone-landscape-lock' : '',
            'zmdi zmdi-smartphone-landscape' : '',
            'zmdi zmdi-smartphone-lock' : '',
            'zmdi zmdi-smartphone-portrait-lock' : '',
            'zmdi zmdi-smartphone-ring' : '',
            'zmdi zmdi-smartphone-setting' : '',
            'zmdi zmdi-smartphone-setup' : '',
            'zmdi zmdi-smartphone' : '',
            'zmdi zmdi-speaker' : '',
            'zmdi zmdi-tablet-android' : '',
            'zmdi zmdi-tablet-mac' : '',
            'zmdi zmdi-tablet' : '',
            'zmdi zmdi-tv-alt-play' : '',
            'zmdi zmdi-tv-list' : '',
            'zmdi zmdi-tv-play' : '',
            'zmdi zmdi-tv' : '',
            'zmdi zmdi-usb' : '',
            'zmdi zmdi-videocam-off' : '',
            'zmdi zmdi-videocam-switch' : '',
            'zmdi zmdi-videocam' : '',
            'zmdi zmdi-watch' : '',
            'zmdi zmdi-wifi-alt-2' : '',
            'zmdi zmdi-wifi-alt' : '',
            'zmdi zmdi-wifi-info' : '',
            'zmdi zmdi-wifi-lock' : '',
            'zmdi zmdi-wifi-off' : '',
            'zmdi zmdi-wifi-outline' : '',
            'zmdi zmdi-wifi' : '',
            'zmdi zmdi-arrow-left-bottom' : '',
            'zmdi zmdi-arrow-left' : '',
            'zmdi zmdi-arrow-merge' : '',
            'zmdi zmdi-arrow-missed' : '',
            'zmdi zmdi-arrow-right-top' : '',
            'zmdi zmdi-arrow-right' : '',
            'zmdi zmdi-arrow-split' : '',
            'zmdi zmdi-arrows' : '',
            'zmdi zmdi-caret-down-circle' : '',
            'zmdi zmdi-caret-down' : '',
            'zmdi zmdi-caret-left-circle' : '',
            'zmdi zmdi-caret-left' : '',
            'zmdi zmdi-caret-right-circle' : '',
            'zmdi zmdi-caret-right' : '',
            'zmdi zmdi-caret-up-circle' : '',
            'zmdi zmdi-caret-up' : '',
            'zmdi zmdi-chevron-down' : '',
            'zmdi zmdi-chevron-left' : '',
            'zmdi zmdi-chevron-right' : '',
            'zmdi zmdi-chevron-up' : '',
            'zmdi zmdi-forward' : '',
            'zmdi zmdi-long-arrow-down' : '',
            'zmdi zmdi-long-arrow-left' : '',
            'zmdi zmdi-long-arrow-return' : '',
            'zmdi zmdi-long-arrow-right' : '',
            'zmdi zmdi-long-arrow-tab' : '',
            'zmdi zmdi-long-arrow-up' : '',
            'zmdi zmdi-rotate-ccw' : '',
            'zmdi zmdi-rotate-cw' : '',
            'zmdi zmdi-rotate-left' : '',
            'zmdi zmdi-rotate-right' : '',
            'zmdi zmdi-square-down' : '',
            'zmdi zmdi-square-right' : '',
            'zmdi zmdi-swap-alt' : '',
            'zmdi zmdi-swap-vertical-circle' : '',
            'zmdi zmdi-swap-vertical' : '',
            'zmdi zmdi-swap' : '',
            'zmdi zmdi-trending-down' : '',
            'zmdi zmdi-trending-flat' : '',
            'zmdi zmdi-trending-up' : '',
            'zmdi zmdi-unfold-less' : '',
            'zmdi zmdi-unfold-more' : '',
            'zmdi zmdi-apps' : '',
            'zmdi zmdi-grid-off' : '',
            'zmdi zmdi-grid' : '',
            'zmdi zmdi-view-agenda' : '',
            'zmdi zmdi-view-array' : '',
            'zmdi zmdi-view-carousel' : '',
            'zmdi zmdi-view-column' : '',
            'zmdi zmdi-view-comfy' : '',
            'zmdi zmdi-view-compact' : '',
            'zmdi zmdi-view-dashboard' : '',
            'zmdi zmdi-view-day' : '',
            'zmdi zmdi-view-headline' : '',
            'zmdi zmdi-view-list-alt' : '',
            'zmdi zmdi-view-list' : '',
            'zmdi zmdi-view-module' : '',
            'zmdi zmdi-view-quilt' : '',
            'zmdi zmdi-view-stream' : '',
            'zmdi zmdi-view-subtitles' : '',
            'zmdi zmdi-view-toc' : '',
            'zmdi zmdi-view-web' : '',
            'zmdi zmdi-view-week' : '',
            'zmdi zmdi-widgets' : '',
            'zmdi zmdi-alarm-check' : '',
            'zmdi zmdi-alarm-off' : '',
            'zmdi zmdi-alarm-plus' : '',
            'zmdi zmdi-alarm-snooze' : '',
            'zmdi zmdi-alarm' : '',
            'zmdi zmdi-calendar-alt' : '',
            'zmdi zmdi-calendar-check' : '',
            'zmdi zmdi-calendar-close' : '',
            'zmdi zmdi-calendar-note' : '',
            'zmdi zmdi-calendar' : '',
            'zmdi zmdi-time-countdown' : '',
            'zmdi zmdi-time-interval' : '',
            'zmdi zmdi-time-restore-setting' : '',
            'zmdi zmdi-time-restore' : '',
            'zmdi zmdi-time' : '',
            'zmdi zmdi-timer-off' : '',
            'zmdi zmdi-timer' : '',
            'zmdi zmdi-android-alt' : '',
            'zmdi zmdi-android' : '',
            'zmdi zmdi-apple' : '',
            'zmdi zmdi-behance' : '',
            'zmdi zmdi-codepen' : '',
            'zmdi zmdi-dribbble' : '',
            'zmdi zmdi-dropbox' : '',
            'zmdi zmdi-evernote' : '',
            'zmdi zmdi-facebook-box' : '',
            'zmdi zmdi-facebook' : '',
            'zmdi zmdi-github-box' : '',
            'zmdi zmdi-github' : '',
            'zmdi zmdi-google-drive' : '',
            'zmdi zmdi-google-earth' : '',
            'zmdi zmdi-google-glass' : '',
            'zmdi zmdi-google-maps' : '',
            'zmdi zmdi-google-pages' : '',
            'zmdi zmdi-google-play' : '',
            'zmdi zmdi-google-plus-box' : '',
            'zmdi zmdi-google-plus' : '',
            'zmdi zmdi-google' : '',
            'zmdi zmdi-instagram' : '',
            'zmdi zmdi-language-css3' : '',
            'zmdi zmdi-language-html5' : '',
            'zmdi zmdi-language-javascript' : '',
            'zmdi zmdi-language-python-alt' : '',
            'zmdi zmdi-language-python' : '',
            'zmdi zmdi-lastfm' : '',
            'zmdi zmdi-linkedin-box' : '',
            'zmdi zmdi-paypal' : '',
            'zmdi zmdi-pinterest-box' : '',
            'zmdi zmdi-pocket' : '',
            'zmdi zmdi-polymer' : '',
            'zmdi zmdi-share' : '',
            'zmdi zmdi-stackoverflow' : '',
            'zmdi zmdi-steam-square' : '',
            'zmdi zmdi-steam' : '',
            'zmdi zmdi-twitter-box' : '',
            'zmdi zmdi-twitter' : '',
            'zmdi zmdi-vk' : '',
            'zmdi zmdi-wikipedia' : '',
            'zmdi zmdi-windows' : '',
            'zmdi zmdi-aspect-ratio-alt' : '',
            'zmdi zmdi-aspect-ratio' : '',
            'zmdi zmdi-blur-circular' : '',
            'zmdi zmdi-blur-linear' : '',
            'zmdi zmdi-blur-off' : '',
            'zmdi zmdi-blur' : '',
            'zmdi zmdi-brightness-2' : '',
            'zmdi zmdi-brightness-3' : '',
            'zmdi zmdi-brightness-4' : '',
            'zmdi zmdi-brightness-5' : '',
            'zmdi zmdi-brightness-6' : '',
            'zmdi zmdi-brightness-7' : '',
            'zmdi zmdi-brightness-auto' : '',
            'zmdi zmdi-brightness-setting' : '',
            'zmdi zmdi-broken-image' : '',
            'zmdi zmdi-center-focus-strong' : '',
            'zmdi zmdi-center-focus-weak' : '',
            'zmdi zmdi-compare' : '',
            'zmdi zmdi-crop-16-9' : '',
            'zmdi zmdi-crop-3-2' : '',
            'zmdi zmdi-crop-5-4' : '',
            'zmdi zmdi-crop-7-5' : '',
            'zmdi zmdi-crop-din' : '',
            'zmdi zmdi-crop-free' : '',
            'zmdi zmdi-crop-landscape' : '',
            'zmdi zmdi-crop-portrait' : '',
            'zmdi zmdi-crop-square' : '',
            'zmdi zmdi-exposure-alt' : '',
            'zmdi zmdi-exposure' : '',
            'zmdi zmdi-filter-b-and-w' : '',
            'zmdi zmdi-filter-center-focus' : '',
            'zmdi zmdi-filter-frames' : '',
            'zmdi zmdi-filter-tilt-shift' : '',
            'zmdi zmdi-gradient' : '',
            'zmdi zmdi-grain' : '',
            'zmdi zmdi-graphic-eq' : '',
            'zmdi zmdi-hdr-off' : '',
            'zmdi zmdi-hdr-strong' : '',
            'zmdi zmdi-hdr-weak' : '',
            'zmdi zmdi-hdr' : '',
            'zmdi zmdi-iridescent' : '',
            'zmdi zmdi-leak-off' : '',
            'zmdi zmdi-leak' : '',
            'zmdi zmdi-looks' : '',
            'zmdi zmdi-loupe' : '',
            'zmdi zmdi-panorama-horizontal' : '',
            'zmdi zmdi-panorama-vertical' : '',
            'zmdi zmdi-panorama-wide-angle' : '',
            'zmdi zmdi-photo-size-select-large' : '',
            'zmdi zmdi-photo-size-select-small' : '',
            'zmdi zmdi-picture-in-picture' : '',
            'zmdi zmdi-slideshow' : '',
            'zmdi zmdi-texture' : '',
            'zmdi zmdi-tonality' : '',
            'zmdi zmdi-vignette' : '',
            'zmdi zmdi-wb-auto' : '',
            'zmdi zmdi-eject-alt' : '',
            'zmdi zmdi-eject' : '',
            'zmdi zmdi-equalizer' : '',
            'zmdi zmdi-fast-forward' : '',
            'zmdi zmdi-fast-rewind' : '',
            'zmdi zmdi-forward-10' : '',
            'zmdi zmdi-forward-30' : '',
            'zmdi zmdi-forward-5' : '',
            'zmdi zmdi-hearing' : '',
            'zmdi zmdi-pause-circle-outline' : '',
            'zmdi zmdi-pause-circle' : '',
            'zmdi zmdi-pause' : '',
            'zmdi zmdi-play-circle-outline' : '',
            'zmdi zmdi-play-circle' : '',
            'zmdi zmdi-play' : '',
            'zmdi zmdi-playlist-audio' : '',
            'zmdi zmdi-playlist-plus' : '',
            'zmdi zmdi-repeat-one' : '',
            'zmdi zmdi-repeat' : '',
            'zmdi zmdi-replay-10' : '',
            'zmdi zmdi-replay-30' : '',
            'zmdi zmdi-replay-5' : '',
            'zmdi zmdi-replay' : '',
            'zmdi zmdi-shuffle' : '',
            'zmdi zmdi-skip-next' : '',
            'zmdi zmdi-skip-previous' : '',
            'zmdi zmdi-stop' : '',
            'zmdi zmdi-surround-sound' : '',
            'zmdi zmdi-tune' : '',
            'zmdi zmdi-volume-down' : '',
            'zmdi zmdi-volume-mute' : '',
            'zmdi zmdi-volume-off' : '',
            'zmdi zmdi-volume-up' : '',
            'zmdi zmdi-n-1-square' : '',
            'zmdi zmdi-n-2-square' : '',
            'zmdi zmdi-n-3-square' : '',
            'zmdi zmdi-n-4-square' : '',
            'zmdi zmdi-n-5-square' : '',
            'zmdi zmdi-n-6-square' : '',
            'zmdi zmdi-neg-1' : '',
            'zmdi zmdi-neg-2' : '',
            'zmdi zmdi-plus-1' : '',
            'zmdi zmdi-plus-2' : '',
            'zmdi zmdi-sec-10' : '',
            'zmdi zmdi-sec-3' : '',
            'zmdi zmdi-zero' : '',
            'zmdi zmdi-airline-seat-flat-angled' : '',
            'zmdi zmdi-airline-seat-flat' : '',
            'zmdi zmdi-airline-seat-individual-suite' : '',
            'zmdi zmdi-airline-seat-legroom-extra' : '',
            'zmdi zmdi-airline-seat-legroom-normal' : '',
            'zmdi zmdi-airline-seat-legroom-reduced' : '',
            'zmdi zmdi-airline-seat-recline-extra' : '',
            'zmdi zmdi-airline-seat-recline-normal' : '',
            'zmdi zmdi-airplay' : '',
            'zmdi zmdi-closed-caption' : '',
            'zmdi zmdi-confirmation-number' : '',
            'zmdi zmdi-developer-board' : '',
            'zmdi zmdi-disc-full' : '',
            'zmdi zmdi-explicit' : '',
            'zmdi zmdi-flight-land' : '',
            'zmdi zmdi-flight-takeoff' : '',
            'zmdi zmdi-flip-to-back' : '',
            'zmdi zmdi-flip-to-front' : '',
            'zmdi zmdi-group-work' : '',
            'zmdi zmdi-hd' : '',
            'zmdi zmdi-hq' : '',
            'zmdi zmdi-markunread-mailbox' : '',
            'zmdi zmdi-memory' : '',
            'zmdi zmdi-nfc' : '',
            'zmdi zmdi-play-for-work' : '',
            'zmdi zmdi-power-input' : '',
            'zmdi zmdi-present-to-all' : '',
            'zmdi zmdi-satellite' : '',
            'zmdi zmdi-tap-and-play' : '',
            'zmdi zmdi-vibration' : '',
            'zmdi zmdi-voicemail' : '',
            'zmdi zmdi-group' : '',
            'zmdi zmdi-rss' : '',
            'zmdi zmdi-shape' : '',
            'zmdi zmdi-spinner' : '',
            'zmdi zmdi-ungroup' : '',
            'zmdi zmdi-500px' : '',
            'zmdi zmdi-8tracks' : '',
            'zmdi zmdi-amazon' : '',
            'zmdi zmdi-blogger' : '',
            'zmdi zmdi-delicious' : '',
            'zmdi zmdi-disqus' : '',
            'zmdi zmdi-flattr' : '',
            'zmdi zmdi-flickr' : '',
            'zmdi zmdi-github-alt' : '',
            'zmdi zmdi-google-old' : '',
            'zmdi zmdi-linkedin' : '',
            'zmdi zmdi-odnoklassniki' : '',
            'zmdi zmdi-outlook' : '',
            'zmdi zmdi-paypal-alt' : '',
            'zmdi zmdi-pinterest' : '',
            'zmdi zmdi-playstation' : '',
            'zmdi zmdi-reddit' : '',
            'zmdi zmdi-skype' : '',
            'zmdi zmdi-slideshare' : '',
            'zmdi zmdi-soundcloud' : '',
            'zmdi zmdi-tumblr' : '',
            'zmdi zmdi-twitch' : '',
            'zmdi zmdi-vimeo' : '',
            'zmdi zmdi-whatsapp' : '',
            'zmdi zmdi-xbox' : '',
            'zmdi zmdi-yahoo' : '',
            'zmdi zmdi-youtube-play' : '',
            'zmdi zmdi-youtube' : '',
            'zmdi zmdi-3d-rotation' : '',
            'zmdi zmdi-airplane-off' : '',
            'zmdi zmdi-airplane' : '',
            'zmdi zmdi-album' : '',
            'zmdi zmdi-archive' : '',
            'zmdi zmdi-assignment-account' : '',
            'zmdi zmdi-assignment-alert' : '',
            'zmdi zmdi-assignment-check' : '',
            'zmdi zmdi-assignment-o' : '',
            'zmdi zmdi-assignment-return' : '',
            'zmdi zmdi-assignment-returned' : '',
            'zmdi zmdi-assignment' : '',
            'zmdi zmdi-attachment-alt' : '',
            'zmdi zmdi-attachment' : '',
            'zmdi zmdi-audio' : '',
            'zmdi zmdi-badge-check' : '',
            'zmdi zmdi-balance-wallet' : '',
            'zmdi zmdi-balance' : '',
            'zmdi zmdi-battery-alert' : '',
            'zmdi zmdi-battery-flash' : '',
            'zmdi zmdi-battery-unknown' : '',
            'zmdi zmdi-battery' : '',
            'zmdi zmdi-bike' : '',
            'zmdi zmdi-block-alt' : '',
            'zmdi zmdi-block' : '',
            'zmdi zmdi-boat' : '',
            'zmdi zmdi-book-image' : '',
            'zmdi zmdi-book' : '',
            'zmdi zmdi-bookmark-outline' : '',
            'zmdi zmdi-bookmark' : '',
            'zmdi zmdi-brush' : '',
            'zmdi zmdi-bug' : '',
            'zmdi zmdi-bus' : '',
            'zmdi zmdi-cake' : '',
            'zmdi zmdi-car-taxi' : '',
            'zmdi zmdi-car-wash' : '',
            'zmdi zmdi-car' : '',
            'zmdi zmdi-card-giftcard' : '',
            'zmdi zmdi-card-membership' : '',
            'zmdi zmdi-card-travel' : '',
            'zmdi zmdi-card' : '',
            'zmdi zmdi-case-check' : '',
            'zmdi zmdi-case-download' : '',
            'zmdi zmdi-case-play' : '',
            'zmdi zmdi-case' : '',
            'zmdi zmdi-cast-connected' : '',
            'zmdi zmdi-cast' : '',
            'zmdi zmdi-chart-donut' : '',
            'zmdi zmdi-chart' : '',
            'zmdi zmdi-city-alt' : '',
            'zmdi zmdi-city' : '',
            'zmdi zmdi-close-circle-o' : '',
            'zmdi zmdi-close-circle' : '',
            'zmdi zmdi-close' : '',
            'zmdi zmdi-cocktail' : '',
            'zmdi zmdi-code-setting' : '',
            'zmdi zmdi-code-smartphone' : '',
            'zmdi zmdi-code' : '',
            'zmdi zmdi-coffee' : '',
            'zmdi zmdi-collection-bookmark' : '',
            'zmdi zmdi-collection-case-play' : '',
            'zmdi zmdi-collection-folder-image' : '',
            'zmdi zmdi-collection-image-o' : '',
            'zmdi zmdi-collection-image' : '',
            'zmdi zmdi-collection-item-1' : '',
            'zmdi zmdi-collection-item-2' : '',
            'zmdi zmdi-collection-item-3' : '',
            'zmdi zmdi-collection-item-4' : '',
            'zmdi zmdi-collection-item-5' : '',
            'zmdi zmdi-collection-item-6' : '',
            'zmdi zmdi-collection-item-7' : '',
            'zmdi zmdi-collection-item-8' : '',
            'zmdi zmdi-collection-item-9-plus' : '',
            'zmdi zmdi-collection-item-9' : '',
            'zmdi zmdi-collection-item' : '',
            'zmdi zmdi-collection-music' : '',
            'zmdi zmdi-collection-pdf' : '',
            'zmdi zmdi-collection-plus' : '',
            'zmdi zmdi-collection-speaker' : '',
            'zmdi zmdi-collection-text' : '',
            'zmdi zmdi-collection-video' : '',
            'zmdi zmdi-compass' : '',
            'zmdi zmdi-cutlery' : '',
            'zmdi zmdi-delete' : '',
            'zmdi zmdi-dialpad' : '',
            'zmdi zmdi-dns' : '',
            'zmdi zmdi-drink' : '',
            'zmdi zmdi-edit' : '',
            'zmdi zmdi-email-open' : '',
            'zmdi zmdi-email' : '',
            'zmdi zmdi-eye-off' : '',
            'zmdi zmdi-eye' : '',
            'zmdi zmdi-eyedropper' : '',
            'zmdi zmdi-favorite-outline' : '',
            'zmdi zmdi-favorite' : '',
            'zmdi zmdi-filter-list' : '',
            'zmdi zmdi-fire' : '',
            'zmdi zmdi-flag' : '',
            'zmdi zmdi-flare' : '',
            'zmdi zmdi-flash-auto' : '',
            'zmdi zmdi-flash-off' : '',
            'zmdi zmdi-flash' : '',
            'zmdi zmdi-flip' : '',
            'zmdi zmdi-flower-alt' : '',
            'zmdi zmdi-flower' : '',
            'zmdi zmdi-font' : '',
            'zmdi zmdi-fullscreen-alt' : '',
            'zmdi zmdi-fullscreen-exit' : '',
            'zmdi zmdi-fullscreen' : '',
            'zmdi zmdi-functions' : '',
            'zmdi zmdi-gas-station' : '',
            'zmdi zmdi-gesture' : '',
            'zmdi zmdi-globe-alt' : '',
            'zmdi zmdi-globe-lock' : '',
            'zmdi zmdi-globe' : '',
            'zmdi zmdi-graduation-cap' : '',
            'zmdi zmdi-home' : '',
            'zmdi zmdi-hospital-alt' : '',
            'zmdi zmdi-hospital' : '',
            'zmdi zmdi-hotel' : '',
            'zmdi zmdi-hourglass-alt' : '',
            'zmdi zmdi-hourglass-outline' : '',
            'zmdi zmdi-hourglass' : '',
            'zmdi zmdi-http' : '',
            'zmdi zmdi-image-alt' : '',
            'zmdi zmdi-image-o' : '',
            'zmdi zmdi-image' : '',
            'zmdi zmdi-inbox' : '',
            'zmdi zmdi-invert-colors-off' : '',
            'zmdi zmdi-invert-colors' : '',
            'zmdi zmdi-key' : '',
            'zmdi zmdi-label-alt-outline' : '',
            'zmdi zmdi-label-alt' : '',
            'zmdi zmdi-label-heart' : '',
            'zmdi zmdi-label' : '',
            'zmdi zmdi-labels' : '',
            'zmdi zmdi-lamp' : '',
            'zmdi zmdi-landscape' : '',
            'zmdi zmdi-layers-off' : '',
            'zmdi zmdi-layers' : '',
            'zmdi zmdi-library' : '',
            'zmdi zmdi-link' : '',
            'zmdi zmdi-lock-open' : '',
            'zmdi zmdi-lock-outline' : '',
            'zmdi zmdi-lock' : '',
            'zmdi zmdi-mail-reply-all' : '',
            'zmdi zmdi-mail-reply' : '',
            'zmdi zmdi-mail-send' : '',
            'zmdi zmdi-mall' : '',
            'zmdi zmdi-map' : '',
            'zmdi zmdi-menu' : '',
            'zmdi zmdi-money-box' : '',
            'zmdi zmdi-money-off' : '',
            'zmdi zmdi-money' : '',
            'zmdi zmdi-more-vert' : '',
            'zmdi zmdi-more' : '',
            'zmdi zmdi-movie-alt' : '',
            'zmdi zmdi-movie' : '',
            'zmdi zmdi-nature-people' : '',
            'zmdi zmdi-nature' : '',
            'zmdi zmdi-navigation' : '',
            'zmdi zmdi-open-in-browser' : '',
            'zmdi zmdi-open-in-new' : '',
            'zmdi zmdi-palette' : '',
            'zmdi zmdi-parking' : '',
            'zmdi zmdi-pin-account' : '',
            'zmdi zmdi-pin-assistant' : '',
            'zmdi zmdi-pin-drop' : '',
            'zmdi zmdi-pin-help' : '',
            'zmdi zmdi-pin-off' : '',
            'zmdi zmdi-pin' : '',
            'zmdi zmdi-pizza' : '',
            'zmdi zmdi-plaster' : '',
            'zmdi zmdi-power-setting' : '',
            'zmdi zmdi-power' : '',
            'zmdi zmdi-print' : '',
            'zmdi zmdi-puzzle-piece' : '',
            'zmdi zmdi-quote' : '',
            'zmdi zmdi-railway' : '',
            'zmdi zmdi-receipt' : '',
            'zmdi zmdi-refresh-alt' : '',
            'zmdi zmdi-refresh-sync-alert' : '',
            'zmdi zmdi-refresh-sync-off' : '',
            'zmdi zmdi-refresh-sync' : '',
            'zmdi zmdi-refresh' : '',
            'zmdi zmdi-roller' : '',
            'zmdi zmdi-ruler' : '',
            'zmdi zmdi-scissors' : '',
            'zmdi zmdi-screen-rotation-lock' : '',
            'zmdi zmdi-screen-rotation' : '',
            'zmdi zmdi-search-for' : '',
            'zmdi zmdi-search-in-file' : '',
            'zmdi zmdi-search-in-page' : '',
            'zmdi zmdi-search-replace' : '',
            'zmdi zmdi-search' : '',
            'zmdi zmdi-seat' : '',
            'zmdi zmdi-settings-square' : '',
            'zmdi zmdi-settings' : '',
            'zmdi zmdi-shield-check' : '',
            'zmdi zmdi-shield-security' : '',
            'zmdi zmdi-shopping-basket' : '',
            'zmdi zmdi-shopping-cart-plus' : '',
            'zmdi zmdi-shopping-cart' : '',
            'zmdi zmdi-sign-in' : '',
            'zmdi zmdi-sort-amount-asc' : '',
            'zmdi zmdi-sort-amount-desc' : '',
            'zmdi zmdi-sort-asc' : '',
            'zmdi zmdi-sort-desc' : '',
            'zmdi zmdi-spellcheck' : '',
            'zmdi zmdi-storage' : '',
            'zmdi zmdi-store-24' : '',
            'zmdi zmdi-store' : '',
            'zmdi zmdi-subway' : '',
            'zmdi zmdi-sun' : '',
            'zmdi zmdi-tab-unselected' : '',
            'zmdi zmdi-tab' : '',
            'zmdi zmdi-tag-close' : '',
            'zmdi zmdi-tag-more' : '',
            'zmdi zmdi-tag' : '',
            'zmdi zmdi-thumb-down' : '',
            'zmdi zmdi-thumb-up-down' : '',
            'zmdi zmdi-thumb-up' : '',
            'zmdi zmdi-ticket-star' : '',
            'zmdi zmdi-toll' : '',
            'zmdi zmdi-toys' : '',
            'zmdi zmdi-traffic' : '',
            'zmdi zmdi-translate' : '',
            'zmdi zmdi-triangle-down' : '',
            'zmdi zmdi-triangle-up' : '',
            'zmdi zmdi-truck' : '',
            'zmdi zmdi-turning-sign' : '',
            'zmdi zmdi-wallpaper' : '',
            'zmdi zmdi-washing-machine' : '',
            'zmdi zmdi-window-maximize' : '',
            'zmdi zmdi-window-minimize' : '',
            'zmdi zmdi-window-restore' : '',
            'zmdi zmdi-wrench' : '',
            'zmdi zmdi-zoom-in' : '',
            'zmdi zmdi-zoom-out' : '',
            'zmdi zmdi-alert-circle-o' : '',
            'zmdi zmdi-alert-circle' : '',
            'zmdi zmdi-alert-octagon' : '',
            'zmdi zmdi-alert-polygon' : '',
            'zmdi zmdi-alert-triangle' : '',
            'zmdi zmdi-help-outline' : '',
            'zmdi zmdi-help' : '',
            'zmdi zmdi-info-outline' : '',
            'zmdi zmdi-info' : '',
            'zmdi zmdi-notifications-active' : '',
            'zmdi zmdi-notifications-add' : '',
            'zmdi zmdi-notifications-none' : '',
            'zmdi zmdi-notifications-off' : '',
            'zmdi zmdi-notifications-paused' : '',
            'zmdi zmdi-notifications' : '',
            'zmdi zmdi-account-add' : '',
            'zmdi zmdi-account-box-mail' : '',
            'zmdi zmdi-account-box-o' : '',
            'zmdi zmdi-account-box-phone' : '',
            'zmdi zmdi-account-box' : '',
            'zmdi zmdi-account-calendar' : '',
            'zmdi zmdi-account-circle' : '',
            'zmdi zmdi-account-o' : '',
            'zmdi zmdi-account' : '',
            'zmdi zmdi-accounts-add' : '',
            'zmdi zmdi-accounts-alt' : '',
            'zmdi zmdi-accounts-list-alt' : '',
            'zmdi zmdi-accounts-list' : '',
            'zmdi zmdi-accounts-outline' : '',
            'zmdi zmdi-accounts' : '',
            'zmdi zmdi-face' : '',
            'zmdi zmdi-female' : '',
            'zmdi zmdi-male-alt' : '',
            'zmdi zmdi-male-female' : '',
            'zmdi zmdi-male' : '',
            'zmdi zmdi-mood-bad' : '',
            'zmdi zmdi-mood' : '',
            'zmdi zmdi-run' : '',
            'zmdi zmdi-walk' : '',
            'zmdi zmdi-cloud-box' : '',
            'zmdi zmdi-cloud-circle' : '',
            'zmdi zmdi-cloud-done' : '',
            'zmdi zmdi-cloud-download' : '',
            'zmdi zmdi-cloud-off' : '',
            'zmdi zmdi-cloud-outline-alt' : '',
            'zmdi zmdi-cloud-outline' : '',
            'zmdi zmdi-cloud-upload' : '',
            'zmdi zmdi-cloud' : '',
            'zmdi zmdi-download' : '',
            'zmdi zmdi-file-plus' : '',
            'zmdi zmdi-file-text' : '',
            'zmdi zmdi-file' : '',
            'zmdi zmdi-folder-outline' : '',
            'zmdi zmdi-folder-person' : '',
            'zmdi zmdi-folder-star-alt' : '',
            'zmdi zmdi-folder-star' : '',
            'zmdi zmdi-folder' : '',
            'zmdi zmdi-gif' : '',
            'zmdi zmdi-upload' : '',
            'zmdi zmdi-border-all' : '',
            'zmdi zmdi-border-bottom' : '',
            'zmdi zmdi-border-clear' : '',
            'zmdi zmdi-border-color' : '',
            'zmdi zmdi-border-horizontal' : '',
            'zmdi zmdi-border-inner' : '',
            'zmdi zmdi-border-left' : '',
            'zmdi zmdi-border-outer' : '',
            'zmdi zmdi-border-right' : '',
            'zmdi zmdi-border-style' : '',
            'zmdi zmdi-border-top' : '',
            'zmdi zmdi-border-vertical' : '',
            'zmdi zmdi-copy' : '',
            'zmdi zmdi-crop' : '',
            'zmdi zmdi-format-align-center' : '',
            'zmdi zmdi-format-align-justify' : '',
            'zmdi zmdi-format-align-left' : '',
            'zmdi zmdi-format-align-right' : '',
            'zmdi zmdi-format-bold' : '',
            'zmdi zmdi-format-clear-all' : '',
            'zmdi zmdi-format-clear' : '',
            'zmdi zmdi-format-color-fill' : '',
            'zmdi zmdi-format-color-reset' : '',
            'zmdi zmdi-format-color-text' : '',
            'zmdi zmdi-format-indent-decrease' : '',
            'zmdi zmdi-format-indent-increase' : '',
            'zmdi zmdi-format-italic' : '',
            'zmdi zmdi-format-line-spacing' : '',
            'zmdi zmdi-format-list-bulleted' : '',
            'zmdi zmdi-format-list-numbered' : '',
            'zmdi zmdi-format-ltr' : '',
            'zmdi zmdi-format-rtl' : '',
            'zmdi zmdi-format-size' : '',
            'zmdi zmdi-format-strikethrough-s' : '',
            'zmdi zmdi-format-strikethrough' : '',
            'zmdi zmdi-format-subject' : '',
            'zmdi zmdi-format-underlined' : '',
            'zmdi zmdi-format-valign-bottom' : '',
            'zmdi zmdi-format-valign-center' : '',
            'zmdi zmdi-format-valign-top' : '',
            'zmdi zmdi-redo' : '',
            'zmdi zmdi-select-all' : '',
            'zmdi zmdi-space-bar' : '',
            'zmdi zmdi-text-format' : '',
            'zmdi zmdi-transform' : '',
            'zmdi zmdi-undo' : '',
            'zmdi zmdi-wrap-text' : '',
            'zmdi zmdi-comment-alert' : '',
            'zmdi zmdi-comment-alt-text' : '',
            'zmdi zmdi-comment-alt' : '',
            'zmdi zmdi-comment-edit' : '',
            'zmdi zmdi-comment-image' : '',
            'zmdi zmdi-comment-list' : '',
            'zmdi zmdi-comment-more' : '',
            'zmdi zmdi-comment-outline' : '',
            'zmdi zmdi-comment-text-alt' : '',
            'zmdi zmdi-comment-text' : '',
            'zmdi zmdi-comment-video' : '',
            'zmdi zmdi-comment' : '',
            'zmdi zmdi-comments' : '',
            'zmdi zmdi-check-all' : '',
            'zmdi zmdi-check-circle-u' : '',
            'zmdi zmdi-check-circle' : '',
            'zmdi zmdi-check-square' : '',
            'zmdi zmdi-check' : '',
            'zmdi zmdi-circle-o' : '',
            'zmdi zmdi-circle' : '',
            'zmdi zmdi-dot-circle-alt' : '',
            'zmdi zmdi-dot-circle' : '',
            'zmdi zmdi-minus-circle-outline' : '',
            'zmdi zmdi-minus-circle' : '',
            'zmdi zmdi-minus-square' : '',
            'zmdi zmdi-minus' : '',
            'zmdi zmdi-plus-circle-o-duplicate' : '',
            'zmdi zmdi-plus-circle-o' : '',
            'zmdi zmdi-plus-circle' : '',
            'zmdi zmdi-plus-square' : '',
            'zmdi zmdi-plus' : '',
            'zmdi zmdi-square-o' : '',
            'zmdi zmdi-star-circle' : '',
            'zmdi zmdi-star-half' : '',
            'zmdi zmdi-star-outline' : '',
            'zmdi zmdi-star' : '',
            'zmdi zmdi-bluetooth-connected' : '',
            'zmdi zmdi-bluetooth-off' : '',
            'zmdi zmdi-bluetooth-search' : '',
            'zmdi zmdi-bluetooth-setting' : '',
            'zmdi zmdi-bluetooth' : '',
            'zmdi zmdi-camera-add' : '',
            'zmdi zmdi-camera-alt' : '',
            'zmdi zmdi-camera-bw' : '',
            'zmdi zmdi-camera-front' : '',
            'zmdi zmdi-camera-mic' : '',
            'zmdi zmdi-camera-party-mode' : '',
            'zmdi zmdi-camera-rear' : '',
            'zmdi zmdi-camera-roll' : '',
            'zmdi zmdi-camera-switch' : '',
            'zmdi zmdi-camera' : '',
            'zmdi zmdi-card-alert' : '',
            'zmdi zmdi-card-off' : '',
            'zmdi zmdi-card-sd' : '',
            'zmdi zmdi-card-sim' : '',
            'zmdi zmdi-desktop-mac' : '',
            'zmdi zmdi-desktop-windows' : '',
            'zmdi zmdi-device-hub' : '',
            'zmdi zmdi-devices-off' : '',
            'zmdi zmdi-devices' : '',
            'zmdi zmdi-dock' : '',
            'zmdi zmdi-floppy' : '',
            'zmdi zmdi-gamepad' : '',
            'zmdi zmdi-gps-dot' : '',
            'zmdi zmdi-gps-off' : '',
            'zmdi zmdi-gps' : '',
            'zmdi zmdi-headset-mic' : '',
            'zmdi zmdi-headset' : '',
            'zmdi zmdi-input-antenna' : '',
            'zmdi zmdi-input-composite' : '',
            'zmdi zmdi-input-hdmi' : '',
            'zmdi zmdi-input-power' : '',
            'zmdi zmdi-input-svideo' : '',
            'zmdi zmdi-keyboard-hide' : '',
            'zmdi zmdi-keyboard' : '',
            'zmdi zmdi-laptop-chromebook' : '',
            'zmdi zmdi-laptop-mac' : '',
            'zmdi zmdi-laptop' : '',
            'zmdi zmdi-mic-off' : '',
            'zmdi zmdi-mic-outline' : '',
            'zmdi zmdi-mic-setting' : '',
            'zmdi zmdi-mic' : '',
            'zmdi zmdi-mouse' : '',
            'zmdi zmdi-network-alert' : '',
            'zmdi zmdi-network-locked' : '',
            'zmdi zmdi-network-off' : '',
            'zmdi zmdi-network-outline' : '',
            'zmdi zmdi-network-setting' : '',
            'zmdi zmdi-network' : '',
            'zmdi zmdi-phone-bluetooth' : '',
            'zmdi zmdi-phone-end' : '',
            'zmdi zmdi-phone-forwarded' : '',
            'zmdi zmdi-phone-in-talk' : '',
            'zmdi zmdi-phone-locked' : '',
            'zmdi zmdi-phone-missed' : '',
            'zmdi zmdi-phone-msg' : '',
            'zmdi zmdi-phone-paused' : '',
            'zmdi zmdi-phone-ring' : '',
            'zmdi zmdi-phone-setting' : '',
            'zmdi zmdi-phone-sip' : '',
            'zmdi zmdi-phone' : '',
            'zmdi zmdi-portable-wifi-changes' : '',
            'zmdi zmdi-portable-wifi-off' : '',
            'zmdi zmdi-portable-wifi' : '',
            'zmdi zmdi-radio' : '',
            'zmdi zmdi-reader' : '',
            'zmdi zmdi-remote-control-alt' : '',
            'zmdi zmdi-remote-control' : '',
            'zmdi zmdi-router' : '',
            'zmdi zmdi-scanner' : '',
            'zmdi zmdi-smartphone-android' : '',
            'zmdi zmdi-smartphone-download' : '',
            'zmdi zmdi-smartphone-erase' : '',
            'zmdi zmdi-smartphone-info' : '',
            'zmdi zmdi-smartphone-iphone' : '',
            'zmdi zmdi-smartphone-landscape-lock' : '',
            'zmdi zmdi-smartphone-landscape' : '',
            'zmdi zmdi-smartphone-lock' : '',
            'zmdi zmdi-smartphone-portrait-lock' : '',
            'zmdi zmdi-smartphone-ring' : '',
            'zmdi zmdi-smartphone-setting' : '',
            'zmdi zmdi-smartphone-setup' : '',
            'zmdi zmdi-smartphone' : '',
            'zmdi zmdi-speaker' : '',
            'zmdi zmdi-tablet-android' : '',
            'zmdi zmdi-tablet-mac' : '',
            'zmdi zmdi-tablet' : '',
            'zmdi zmdi-tv-alt-play' : '',
            'zmdi zmdi-tv-list' : '',
            'zmdi zmdi-tv-play' : '',
            'zmdi zmdi-tv' : '',
            'zmdi zmdi-usb' : '',
            'zmdi zmdi-videocam-off' : '',
            'zmdi zmdi-videocam-switch' : '',
            'zmdi zmdi-videocam' : '',
            'zmdi zmdi-watch' : '',
            'zmdi zmdi-wifi-alt-2' : '',
            'zmdi zmdi-wifi-alt' : '',
            'zmdi zmdi-wifi-info' : '',
            'zmdi zmdi-wifi-lock' : '',
            'zmdi zmdi-wifi-off' : '',
            'zmdi zmdi-wifi-outline' : '',
            'zmdi zmdi-wifi' : '',
            'zmdi zmdi-arrow-left-bottom' : '',
            'zmdi zmdi-arrow-left' : '',
            'zmdi zmdi-arrow-merge' : '',
            'zmdi zmdi-arrow-missed' : '',
            'zmdi zmdi-arrow-right-top' : '',
            'zmdi zmdi-arrow-right' : '',
            'zmdi zmdi-arrow-split' : '',
            'zmdi zmdi-arrows' : '',
            'zmdi zmdi-caret-down-circle' : '',
            'zmdi zmdi-caret-down' : '',
            'zmdi zmdi-caret-left-circle' : '',
            'zmdi zmdi-caret-left' : '',
            'zmdi zmdi-caret-right-circle' : '',
            'zmdi zmdi-caret-right' : '',
            'zmdi zmdi-caret-up-circle' : '',
            'zmdi zmdi-caret-up' : '',
            'zmdi zmdi-chevron-down' : '',
            'zmdi zmdi-chevron-left' : '',
            'zmdi zmdi-chevron-right' : '',
            'zmdi zmdi-chevron-up' : '',
            'zmdi zmdi-forward' : '',
            'zmdi zmdi-long-arrow-down' : '',
            'zmdi zmdi-long-arrow-left' : '',
            'zmdi zmdi-long-arrow-return' : '',
            'zmdi zmdi-long-arrow-right' : '',
            'zmdi zmdi-long-arrow-tab' : '',
            'zmdi zmdi-long-arrow-up' : '',
            'zmdi zmdi-rotate-ccw' : '',
            'zmdi zmdi-rotate-cw' : '',
            'zmdi zmdi-rotate-left' : '',
            'zmdi zmdi-rotate-right' : '',
            'zmdi zmdi-square-down' : '',
            'zmdi zmdi-square-right' : '',
            'zmdi zmdi-swap-alt' : '',
            'zmdi zmdi-swap-vertical-circle' : '',
            'zmdi zmdi-swap-vertical' : '',
            'zmdi zmdi-swap' : '',
            'zmdi zmdi-trending-down' : '',
            'zmdi zmdi-trending-flat' : '',
            'zmdi zmdi-trending-up' : '',
            'zmdi zmdi-unfold-less' : '',
            'zmdi zmdi-unfold-more' : '',
            'zmdi zmdi-apps' : '',
            'zmdi zmdi-grid-off' : '',
            'zmdi zmdi-grid' : '',
            'zmdi zmdi-view-agenda' : '',
            'zmdi zmdi-view-array' : '',
            'zmdi zmdi-view-carousel' : '',
            'zmdi zmdi-view-column' : '',
            'zmdi zmdi-view-comfy' : '',
            'zmdi zmdi-view-compact' : '',
            'zmdi zmdi-view-dashboard' : '',
            'zmdi zmdi-view-day' : '',
            'zmdi zmdi-view-headline' : '',
            'zmdi zmdi-view-list-alt' : '',
            'zmdi zmdi-view-list' : '',
            'zmdi zmdi-view-module' : '',
            'zmdi zmdi-view-quilt' : '',
            'zmdi zmdi-view-stream' : '',
            'zmdi zmdi-view-subtitles' : '',
            'zmdi zmdi-view-toc' : '',
            'zmdi zmdi-view-web' : '',
            'zmdi zmdi-view-week' : '',
            'zmdi zmdi-widgets' : '',
            'zmdi zmdi-alarm-check' : '',
            'zmdi zmdi-alarm-off' : '',
            'zmdi zmdi-alarm-plus' : '',
            'zmdi zmdi-alarm-snooze' : '',
            'zmdi zmdi-alarm' : '',
            'zmdi zmdi-calendar-alt' : '',
            'zmdi zmdi-calendar-check' : '',
            'zmdi zmdi-calendar-close' : '',
            'zmdi zmdi-calendar-note' : '',
            'zmdi zmdi-calendar' : '',
            'zmdi zmdi-time-countdown' : '',
            'zmdi zmdi-time-interval' : '',
            'zmdi zmdi-time-restore-setting' : '',
            'zmdi zmdi-time-restore' : '',
            'zmdi zmdi-time' : '',
            'zmdi zmdi-timer-off' : '',
            'zmdi zmdi-timer' : '',
            'zmdi zmdi-android-alt' : '',
            'zmdi zmdi-android' : '',
            'zmdi zmdi-apple' : '',
            'zmdi zmdi-behance' : '',
            'zmdi zmdi-codepen' : '',
            'zmdi zmdi-dribbble' : '',
            'zmdi zmdi-dropbox' : '',
            'zmdi zmdi-evernote' : '',
            'zmdi zmdi-facebook-box' : '',
            'zmdi zmdi-facebook' : '',
            'zmdi zmdi-github-box' : '',
            'zmdi zmdi-github' : '',
            'zmdi zmdi-google-drive' : '',
            'zmdi zmdi-google-earth' : '',
            'zmdi zmdi-google-glass' : '',
            'zmdi zmdi-google-maps' : '',
            'zmdi zmdi-google-pages' : '',
            'zmdi zmdi-google-play' : '',
            'zmdi zmdi-google-plus-box' : '',
            'zmdi zmdi-google-plus' : '',
            'zmdi zmdi-google' : '',
            'zmdi zmdi-instagram' : '',
            'zmdi zmdi-language-css3' : '',
            'zmdi zmdi-language-html5' : '',
            'zmdi zmdi-language-javascript' : '',
            'zmdi zmdi-language-python-alt' : '',
            'zmdi zmdi-language-python' : '',
            'zmdi zmdi-lastfm' : '',
            'zmdi zmdi-linkedin-box' : '',
            'zmdi zmdi-paypal' : '',
            'zmdi zmdi-pinterest-box' : '',
            'zmdi zmdi-pocket' : '',
            'zmdi zmdi-polymer' : '',
            'zmdi zmdi-share' : '',
            'zmdi zmdi-stackoverflow' : '',
            'zmdi zmdi-steam-square' : '',
            'zmdi zmdi-steam' : '',
            'zmdi zmdi-twitter-box' : '',
            'zmdi zmdi-twitter' : '',
            'zmdi zmdi-vk' : '',
            'zmdi zmdi-wikipedia' : '',
            'zmdi zmdi-windows' : '',
            'zmdi zmdi-aspect-ratio-alt' : '',
            'zmdi zmdi-aspect-ratio' : '',
            'zmdi zmdi-blur-circular' : '',
            'zmdi zmdi-blur-linear' : '',
            'zmdi zmdi-blur-off' : '',
            'zmdi zmdi-blur' : '',
            'zmdi zmdi-brightness-2' : '',
            'zmdi zmdi-brightness-3' : '',
            'zmdi zmdi-brightness-4' : '',
            'zmdi zmdi-brightness-5' : '',
            'zmdi zmdi-brightness-6' : '',
            'zmdi zmdi-brightness-7' : '',
            'zmdi zmdi-brightness-auto' : '',
            'zmdi zmdi-brightness-setting' : '',
            'zmdi zmdi-broken-image' : '',
            'zmdi zmdi-center-focus-strong' : '',
            'zmdi zmdi-center-focus-weak' : '',
            'zmdi zmdi-compare' : '',
            'zmdi zmdi-crop-16-9' : '',
            'zmdi zmdi-crop-3-2' : '',
            'zmdi zmdi-crop-5-4' : '',
            'zmdi zmdi-crop-7-5' : '',
            'zmdi zmdi-crop-din' : '',
            'zmdi zmdi-crop-free' : '',
            'zmdi zmdi-crop-landscape' : '',
            'zmdi zmdi-crop-portrait' : '',
            'zmdi zmdi-crop-square' : '',
            'zmdi zmdi-exposure-alt' : '',
            'zmdi zmdi-exposure' : '',
            'zmdi zmdi-filter-b-and-w' : '',
            'zmdi zmdi-filter-center-focus' : '',
            'zmdi zmdi-filter-frames' : '',
            'zmdi zmdi-filter-tilt-shift' : '',
            'zmdi zmdi-gradient' : '',
            'zmdi zmdi-grain' : '',
            'zmdi zmdi-graphic-eq' : '',
            'zmdi zmdi-hdr-off' : '',
            'zmdi zmdi-hdr-strong' : '',
            'zmdi zmdi-hdr-weak' : '',
            'zmdi zmdi-hdr' : '',
            'zmdi zmdi-iridescent' : '',
            'zmdi zmdi-leak-off' : '',
            'zmdi zmdi-leak' : '',
            'zmdi zmdi-looks' : '',
            'zmdi zmdi-loupe' : '',
            'zmdi zmdi-panorama-horizontal' : '',
            'zmdi zmdi-panorama-vertical' : '',
            'zmdi zmdi-panorama-wide-angle' : '',
            'zmdi zmdi-photo-size-select-large' : '',
            'zmdi zmdi-photo-size-select-small' : '',
            'zmdi zmdi-picture-in-picture' : '',
            'zmdi zmdi-slideshow' : '',
            'zmdi zmdi-texture' : '',
            'zmdi zmdi-tonality' : '',
            'zmdi zmdi-vignette' : '',
            'zmdi zmdi-wb-auto' : '',
            'zmdi zmdi-eject-alt' : '',
            'zmdi zmdi-eject' : '',
            'zmdi zmdi-equalizer' : '',
            'zmdi zmdi-fast-forward' : '',
            'zmdi zmdi-fast-rewind' : '',
            'zmdi zmdi-forward-10' : '',
            'zmdi zmdi-forward-30' : '',
            'zmdi zmdi-forward-5' : '',
            'zmdi zmdi-hearing' : '',
            'zmdi zmdi-pause-circle-outline' : '',
            'zmdi zmdi-pause-circle' : '',
            'zmdi zmdi-pause' : '',
            'zmdi zmdi-play-circle-outline' : '',
            'zmdi zmdi-play-circle' : '',
            'zmdi zmdi-play' : '',
            'zmdi zmdi-playlist-audio' : '',
            'zmdi zmdi-playlist-plus' : '',
            'zmdi zmdi-repeat-one' : '',
            'zmdi zmdi-repeat' : '',
            'zmdi zmdi-replay-10' : '',
            'zmdi zmdi-replay-30' : '',
            'zmdi zmdi-replay-5' : '',
            'zmdi zmdi-replay' : '',
            'zmdi zmdi-shuffle' : '',
            'zmdi zmdi-skip-next' : '',
            'zmdi zmdi-skip-previous' : '',
            'zmdi zmdi-stop' : '',
            'zmdi zmdi-surround-sound' : '',
            'zmdi zmdi-tune' : '',
            'zmdi zmdi-volume-down' : '',
            'zmdi zmdi-volume-mute' : '',
            'zmdi zmdi-volume-off' : '',
            'zmdi zmdi-volume-up' : '',
            'zmdi zmdi-n-1-square' : '',
            'zmdi zmdi-n-2-square' : '',
            'zmdi zmdi-n-3-square' : '',
            'zmdi zmdi-n-4-square' : '',
            'zmdi zmdi-n-5-square' : '',
            'zmdi zmdi-n-6-square' : '',
            'zmdi zmdi-neg-1' : '',
            'zmdi zmdi-neg-2' : '',
            'zmdi zmdi-plus-1' : '',
            'zmdi zmdi-plus-2' : '',
            'zmdi zmdi-sec-10' : '',
            'zmdi zmdi-sec-3' : '',
            'zmdi zmdi-zero' : '',
            'zmdi zmdi-airline-seat-flat-angled' : '',
            'zmdi zmdi-airline-seat-flat' : '',
            'zmdi zmdi-airline-seat-individual-suite' : '',
            'zmdi zmdi-airline-seat-legroom-extra' : '',
            'zmdi zmdi-airline-seat-legroom-normal' : '',
            'zmdi zmdi-airline-seat-legroom-reduced' : '',
            'zmdi zmdi-airline-seat-recline-extra' : '',
            'zmdi zmdi-airline-seat-recline-normal' : '',
            'zmdi zmdi-airplay' : '',
            'zmdi zmdi-closed-caption' : '',
            'zmdi zmdi-confirmation-number' : '',
            'zmdi zmdi-developer-board' : '',
            'zmdi zmdi-disc-full' : '',
            'zmdi zmdi-explicit' : '',
            'zmdi zmdi-flight-land' : '',
            'zmdi zmdi-flight-takeoff' : '',
            'zmdi zmdi-flip-to-back' : '',
            'zmdi zmdi-flip-to-front' : '',
            'zmdi zmdi-group-work' : '',
            'zmdi zmdi-hd' : '',
            'zmdi zmdi-hq' : '',
            'zmdi zmdi-markunread-mailbox' : '',
            'zmdi zmdi-memory' : '',
            'zmdi zmdi-nfc' : '',
            'zmdi zmdi-play-for-work' : '',
            'zmdi zmdi-power-input' : '',
            'zmdi zmdi-present-to-all' : '',
            'zmdi zmdi-satellite' : '',
            'zmdi zmdi-tap-and-play' : '',
            'zmdi zmdi-vibration' : '',
            'zmdi zmdi-voicemail' : '',
            'zmdi zmdi-group' : '',
            'zmdi zmdi-rss' : '',
            'zmdi zmdi-shape' : '',
            'zmdi zmdi-spinner' : '',
            'zmdi zmdi-ungroup' : '',
            'zmdi zmdi-500px' : '',
            'zmdi zmdi-8tracks' : '',
            'zmdi zmdi-amazon' : '',
            'zmdi zmdi-blogger' : '',
            'zmdi zmdi-delicious' : '',
            'zmdi zmdi-disqus' : '',
            'zmdi zmdi-flattr' : '',
            'zmdi zmdi-flickr' : '',
            'zmdi zmdi-github-alt' : '',
            'zmdi zmdi-google-old' : '',
            'zmdi zmdi-linkedin' : '',
            'zmdi zmdi-odnoklassniki' : '',
            'zmdi zmdi-outlook' : '',
            'zmdi zmdi-paypal-alt' : '',
            'zmdi zmdi-pinterest' : '',
            'zmdi zmdi-playstation' : '',
            'zmdi zmdi-reddit' : '',
            'zmdi zmdi-skype' : '',
            'zmdi zmdi-slideshare' : '',
            'zmdi zmdi-soundcloud' : '',
            'zmdi zmdi-tumblr' : '',
            'zmdi zmdi-twitch' : '',
            'zmdi zmdi-vimeo' : '',
            'zmdi zmdi-whatsapp' : '',
            'zmdi zmdi-xbox' : '',
            'zmdi zmdi-yahoo' : '',
            'zmdi zmdi-youtube-play' : '',
            'zmdi zmdi-youtube' : '',
            'zmdi zmdi-import-export' : '',
            'zmdi zmdi-swap-vertical-' : '',
            'zmdi zmdi-airplanemode-inactive' : '',
            'zmdi zmdi-airplanemode-active' : '',
            'zmdi zmdi-rate-review' : '',
            'zmdi zmdi-comment-sign' : '',
            'zmdi zmdi-network-warning' : '',
            'zmdi zmdi-shopping-cart-add' : '',
            'zmdi zmdi-file-add' : '',
            'zmdi zmdi-network-wifi-scan' : '',
            'zmdi zmdi-collection-add' : '',
            'zmdi zmdi-format-playlist-add' : '',
            'zmdi zmdi-format-queue-music' : '',
            'zmdi zmdi-plus-box' : '',
            'zmdi zmdi-tag-backspace' : '',
            'zmdi zmdi-alarm-add' : '',
            'zmdi zmdi-battery-charging' : '',
            'zmdi zmdi-daydream-setting' : '',
            'zmdi zmdi-more-horiz' : '',
            'zmdi zmdi-book-photo' : '',
            'zmdi zmdi-incandescent' : '',
            'zmdi zmdi-wb-iridescent' : '',
            'zmdi zmdi-calendar-remove' : '',
            'zmdi zmdi-refresh-sync-disabled' : '',
            'zmdi zmdi-refresh-sync-problem' : '',
            'zmdi zmdi-crop-original' : '',
            'zmdi zmdi-power-off' : '',
            'zmdi zmdi-power-off-setting' : '',
            'zmdi zmdi-leak-remove' : '',
            'zmdi zmdi-star-border' : '',
            'zmdi zmdi-brightness-low' : '',
            'zmdi zmdi-brightness-medium' : '',
            'zmdi zmdi-brightness-high' : '',
            'zmdi zmdi-smartphone-portrait' : '',
            'zmdi zmdi-live-tv' : '',
            'zmdi zmdi-format-textdirection-l-to-r' : '',
            'zmdi zmdi-format-textdirection-r-to-l' : '',
            'zmdi zmdi-arrow-back' : '',
            'zmdi zmdi-arrow-forward' : '',
            'zmdi zmdi-arrow-in' : '',
            'zmdi zmdi-arrow-out' : '',
            'zmdi zmdi-rotate-90-degrees-ccw' : '',
            'zmdi zmdi-adb' : '',
            'zmdi zmdi-network-wifi' : '',
            'zmdi zmdi-network-wifi-alt' : '',
            'zmdi zmdi-network-wifi-lock' : '',
            'zmdi zmdi-network-wifi-off' : '',
            'zmdi zmdi-network-wifi-outline' : '',
            'zmdi zmdi-network-wifi-info' : '',
            'zmdi zmdi-layers-clear' : '',
            'zmdi zmdi-colorize' : '',
            'zmdi zmdi-format-paint' : '',
            'zmdi zmdi-format-quote' : '',
            'zmdi zmdi-camera-monochrome-photos' : '',
            'zmdi zmdi-sort-by-alpha' : '',
            'zmdi zmdi-folder-shared' : '',
            'zmdi zmdi-folder-special' : '',
            'zmdi zmdi-comment-dots' : '',
            'zmdi zmdi-reorder' : '',
            'zmdi zmdi-dehaze' : '',
            'zmdi zmdi-sort' : '',
            'zmdi zmdi-pages' : '',
            'zmdi zmdi-stack-overflow' : '',
            'zmdi zmdi-calendar-account' : '',
            'zmdi zmdi-paste' : '',
            'zmdi zmdi-cut' : '',
            'zmdi zmdi-save' : '',
            'zmdi zmdi-smartphone-code' : '',
            'zmdi zmdi-directions-bike' : '',
            'zmdi zmdi-directions-boat' : '',
            'zmdi zmdi-directions-bus' : '',
            'zmdi zmdi-directions-car' : '',
            'zmdi zmdi-directions-railway' : '',
            'zmdi zmdi-directions-run' : '',
            'zmdi zmdi-directions-subway' : '',
            'zmdi zmdi-directions-walk' : '',
            'zmdi zmdi-local-hotel' : '',
            'zmdi zmdi-local-activity' : '',
            'zmdi zmdi-local-play' : '',
            'zmdi zmdi-local-airport' : '',
            'zmdi zmdi-local-atm' : '',
            'zmdi zmdi-local-bar' : '',
            'zmdi zmdi-local-cafe' : '',
            'zmdi zmdi-local-car-wash' : '',
            'zmdi zmdi-local-convenience-store' : '',
            'zmdi zmdi-local-dining' : '',
            'zmdi zmdi-local-drink' : '',
            'zmdi zmdi-local-florist' : '',
            'zmdi zmdi-local-gas-station' : '',
            'zmdi zmdi-local-grocery-store' : '',
            'zmdi zmdi-local-hospital' : '',
            'zmdi zmdi-local-laundry-service' : '',
            'zmdi zmdi-local-library' : '',
            'zmdi zmdi-local-mall' : '',
            'zmdi zmdi-local-movies' : '',
            'zmdi zmdi-local-offer' : '',
            'zmdi zmdi-local-parking' : '',
            'zmdi zmdi-local-parking' : '',
            'zmdi zmdi-local-pharmacy' : '',
            'zmdi zmdi-local-phone' : '',
            'zmdi zmdi-local-pizza' : '',
            'zmdi zmdi-local-post-office' : '',
            'zmdi zmdi-local-printshop' : '',
            'zmdi zmdi-local-see' : '',
            'zmdi zmdi-local-shipping' : '',
            'zmdi zmdi-local-store' : '',
            'zmdi zmdi-local-taxi' : '',
            'zmdi zmdi-local-wc' : '',
            'zmdi zmdi-my-location' : '',
            'zmdi zmdi-directions' : ''    
        }
    },
    getData: function(useDefault) {
        var data = new Object();
        var value = $('[name=' + this.id + ']:not(.hidden)').val();
        var icon = $('[name=' + this.id + ']:not(.hidden)').prev("span.icon");
        if (icon.length > 0) {
            var iconValue = icon.find(".value").html();
            if (this.properties.iconOnly !== undefined && this.properties.iconOnly === "true") {
                value = iconValue;
            } else {
                if (iconValue !== "") {
                    value = iconValue + " " + value;
                }
            }
        }
        
        if (value === undefined || value === null || value === "") {
            if (useDefault !== undefined && useDefault &&
                this.defaultValue !== undefined && this.defaultValue !== null) {
                value = this.defaultValue;
            } else {
                value = "";
            }
        }
        data[this.properties.name] = value;
        return data;
    },
    renderField: function() {
        var size = '';
        if (this.value === null) {
            this.value = "";
        }
        if (this.properties.size !== undefined && this.properties.size !== null) {
            size = ' size="' + this.properties.size + '"';
        } else {
            size = ' size="50"';
        }
        var maxlength = '';
        if (this.properties.maxlength !== undefined && this.properties.maxlength !== null) {
            maxlength = ' maxlength="' + this.properties.maxlength + '"';
        }
        
        var valueWithoutIcon = this.value;
        var icon= "";
        var temp = $('<div>'+this.value+'</div>');
        if ($(temp).find("> *:eq(0)").is("i")) {
            var i = $(temp).find("> *:eq(0)");
            var iClass = $(i).attr("class");
            
            if (iClass !== "") {
                icon = $('<div></div>').append(i).html();
                valueWithoutIcon = $(temp).html().trim();
            }
        }
        
        var hideTextField = "";
        var iconOnly = "";
        if (this.properties.iconOnly !== undefined && this.properties.iconOnly === "true") {
            hideTextField = "visibility: hidden; !important;";
            iconOnly = "style=\"border-radius: 0.25rem;\"";
            valueWithoutIcon = "";
        }

        return '<span class="icon"><span '+iconOnly+'><span class="value">'+icon+'</span><span class="dropdown"><i class="fas fa-angle-down"></i></span></span></span><input style="padding-left:50px;'+hideTextField+'" type="text" id="' + this.id + '" name="' + this.id + '"' + size + maxlength + ' value="' + PropertyEditor.Util.escapeHtmlTag(valueWithoutIcon) + '"/>';
    },
    initScripting: function() {
        var field = this;
        var icon = $("#" + this.id).prev("span.icon");
        if (!$(icon).hasClass("attachedPicker")) {
            $(icon).find("> span").off("click");
            $(icon).find("> span").on("click", function(e) {
                e.stopPropagation();
                
                var i = $(icon);
                if ($(i).find('.property-icon-picker').length === 0) {
                    $(i).append('<div class="property-icon-picker"><div class="value_holder"><span class="color"></span><input class="color_value" placeholder="Color" style="display:none"/></input><input class="text_value" placeholder="Value"/><i class="la la-check"></i></div><div><input class="search" placeholder="Search"/><ul></ul></div></div>');
                    
                    for (var set in field.icons) {
                        $(i).find("ul").append('<li class="iconset">'+set+'</li>');
                        for (var property in field.icons[set]) {
                            $(i).find("ul").append('<li data-search-terms="'+field.icons[property]+'"><i class="'+property+'" data-icon-picker-options ></i></li>');
                        }
                    }
                    
                    $(i).find("input.text_value").val($(icon).find("span.value i").attr("class"));
                    if ($(icon).find("span.value i").length > 0) {
                        var color = $(icon).find("span.value i")[0].style.color;
                        if (color !== undefined) {
                            $(i).find("input.color_value").val(color);
                            $(i).find(".value_holder .color").css("background", color);
                        }
                    }
                    
                    $(i).find("input.search").off("keyup");
                    $(i).find("input.search").on("keyup", function() {
                        var searchText = this.value.toLowerCase();
                        $(this).parent().find("li[data-search-terms]").each(function () {
                            var selection = $(this);
                            selection.hide();
                            if ($(selection).find("i").attr("class").toLowerCase().indexOf(searchText) > -1 || $(selection).attr("data-search-terms").toLowerCase().indexOf(searchText) > -1) { 
                                selection.show();
                            }
                        });
                    });
                }
                
                $(i).addClass("open");
                
                $("body").off("click.icon-picker");
                $("body").on("click.icon-picker", function(e) {
                    var container = $(i).find(".property-icon-picker");
                    // if the target of the click isn't the container nor a descendant of the container
                    if (!container.is(e.target) && container.has(e.target).length === 0) 
                    {
                        $(i).removeClass("open");
                        $("body").off("click.icon-picker");
                    } else if ($(e.target).is("li[data-search-terms]")) {
                        $(i).find("input.text_value").val($(e.target).find("i").attr("class"));
                    } else if ($(e.target).is("i[data-icon-picker-options]")) {
                        $(i).find("input.text_value").val($(e.target).attr("class"));
                    } else if ($(e.target).is("span.color")) {
                        $(i).find("input.text_value").hide();
                        $(i).find("input.color_value").show();
                    } else if ($(e.target).is("i")) {
                        if ($(i).find("input.text_value").is(":visible")) {
                            if ($(i).find("span.value > i").length > 0) {
                                if ($(i).find("input.text_value").val() === "") {
                                    $(i).find("span.value > i").remove();
                                } else {
                                    $(i).find("span.value > i").attr("class", $(i).find("input.text_value").val());
                                }
                            } else {
                                if ($(i).find("input.text_value").val() !== "") {
                                    $(i).find("span.value").prepend('<i class="' + $(i).find("input.text_value").val() + '"></i>');
                                }
                            }

                            var color = $(i).find("input.color_value").val();
                            if (color !== "") {
                                $(i).find("span.value > i").attr("style", "color:"+color);
                            }
                            
                            $(i).removeClass("open");
                            $("body").off("click.icon-picker");
                            $("#" + this.id).trigger("change");
                        } else if ($(i).find("input.color_value").is(":visible")) {
                            $(i).find("input.text_value").show();
                            $(i).find("input.color_value").hide();
                            $(i).find(".color").css("background", $(i).find("input.color_value").val());
                        }
                    } else {
                        return false;
                    }
                });
            });
            $(icon).addClass("attachedPicker")
        }
    }
};
PropertyEditor.Type.IconTextField = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.IconTextField.prototype);

PropertyEditor.Type.Number = function() {};
PropertyEditor.Type.Number.prototype = {
    shortname: "number",
    supportPrefix: true,
    getData: function(useDefault) {
        var data = new Object();
        var value = $('[name=' + this.id + ']:not(.hidden)').val();
        
        if (this.properties.mode === "css_unit") {
            var selector = $('[name=' + this.id + ']:not(.hidden)').next("select");
            if (selector.length > 0) {
                var unitValue = selector.val();
                if (unitValue === "auto") {
                    value = unitValue;
                } else if (value !== "") {
                    value += unitValue;
                }
            }
        }
        
        if (value === undefined || value === null || value === "") {
            if (useDefault !== undefined && useDefault &&
                this.defaultValue !== undefined && this.defaultValue !== null) {
                value = this.defaultValue;
            } else {
                value = "";
            }
        }
        data[this.properties.name] = value;
        return data;
    },
    renderField: function() {
        var size = '';
        var value = this.value;
        if (value === null) {
            value = "";
        }
        if (this.properties.size !== undefined && this.properties.size !== null) {
            size = ' size="' + this.properties.size + '"';
        } else {
            size = ' size="50"';
        }
        var maxlength = '';
        if (this.properties.maxlength !== undefined && this.properties.maxlength !== null) {
            maxlength = ' maxlength="' + this.properties.maxlength + '"';
        }
        
        var unitSelector = "";
        var cssClass = "";
        if (this.properties.mode === "css_unit") {
            cssClass = "withUnitSlector";
            var unit = ['em', 'px', '%', 'rem', 'auto'];
            
            var unitValue = 'px';
            for (var i in unit) {
                if (value.indexOf(unit[i]) !== -1) {
                    unitValue = unit[i];
                    break;
                }
            }
            unitSelector = '<select>';
            for (var i in unit) {
                unitSelector += '<option value="'+unit[i]+'" '+(unit[i] === unitValue?'selected':'')+'>'+unit[i]+'</option>';
            }
            unitSelector += '</select>';
            
            value = value.replace(/[^\d.-]/g, '');
        }

        return '<input type="number" class="'+cssClass+'" id="' + this.id + '" name="' + this.id + '"' + size + maxlength + ' value="' + PropertyEditor.Util.escapeHtmlTag(value) + '"/>' + unitSelector;
    },
    initScripting: function() {
        var input = $("#" + this.id);
        var selector = $("#" + this.id).next("select");
        var updateLayoutOnAutoValue = function() {
            var value = selector.val();
            if (value === "auto") {
                input.addClass("autoUnit");
            } else {
                input.removeClass("autoUnit");
            }
        };
        
        selector.off("change.updatelayout");
        selector.on("change.updatelayout", updateLayoutOnAutoValue);
        updateLayoutOnAutoValue();
    }
};
PropertyEditor.Type.Number = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Number.prototype);

PropertyEditor.Type.Color = function() {};
PropertyEditor.Type.Color.prototype = {
    shortname: "color",
    renderField: function() {
        if (this.value === null) {
            this.value = "";
        }
        if (this.value.indexOf("#") === 0) {
            this.value = this.value.toUpperCase();
        }
        return '<input class="jscolor" type="text" id="' + this.id + '" name="' + this.id + '"' + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '"/>';
    },
    initScripting: function() {
        try {
            $("#" + this.id).colorPicker({
                renderCallback: function($elm, toggled) {
                    if ($elm.val() !== "" && $elm.val() !== undefined) {
                        if (this.color.colors.alpha === 1) {
                            $elm.val('#' + this.color.colors.HEX);
                        } else {
                            $elm.val(this.color.toString('RGB'));
                        }
                    }
                }
            }).off("focusin.tcp");
        } catch (err) {}
        PropertyEditor.Util.supportHashField(this);
    }
};
PropertyEditor.Type.Color = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Color.prototype);

PropertyEditor.Type.Password = function() {};
PropertyEditor.Type.Password.prototype = {
    shortname: "password",
    getData: function(useDefault) {
        var data = new Object();
        var value = $('[name=' + this.id + ']:not(.hidden)').val();
        if (value === undefined || value === null || value === "") {
            if (useDefault !== undefined && useDefault &&
                this.defaultValue !== undefined && this.defaultValue !== null) {
                value = this.defaultValue;
            } else {
                value = "";
            }
        }
        value = "%%%%" + value + "%%%%";
        data[this.properties.name] = value;
        return data;
    },
    renderField: function() {
        var size = '';
        if (this.value === null) {
            this.value = "";
        }
        if (this.properties.size !== undefined && this.properties.size !== null) {
            size = ' size="' + this.properties.size + '"';
        } else {
            size = ' size="50"';
        }
        var maxlength = '';
        if (this.properties.maxlength !== undefined && this.properties.maxlength !== null) {
            maxlength = ' maxlength="' + this.properties.maxlength + '"';
        }

        this.value = this.value.replace(/%%%%/g, '');

        return '<input type="password" autocomplete="new-password" id="' + this.id + '" name="' + this.id + '"' + size + maxlength + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '"/>';
    },
    renderDefault: function() {
        var html = '';
        if (this.defaultValue !== null) {
            defaultValue = this.defaultValue.replace(/./g, '*');
            html = '<div class="default"><span class="label">' + get_peditor_msg('peditor.default') + '</span><span class="value">' + PropertyEditor.Util.escapeHtmlTag(defaultValue) + '</span><div class="clear"></div></div>';
        }
        return html;
    }
};
PropertyEditor.Type.Password = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Password.prototype);

PropertyEditor.Type.TextArea = function() {};
PropertyEditor.Type.TextArea.prototype = {
    shortname: "textarea",
    renderField: function() {
        var rows = '';
        if (this.value === null) {
            this.value = "";
        }
        if (this.properties.rows !== undefined && this.properties.rows !== null) {
            rows = ' rows="' + this.properties.rows + '"';
        } else {
            rows = ' rows="5"';
        }
        var cols = '';
        if (this.properties.cols !== undefined && this.properties.cols !== null) {
            cols = ' cols="' + this.properties.cols + '"';
        } else {
            cols = ' cols="50"';
        }

        return '<textarea id="' + this.id + '" name="' + this.id + '"' + rows + cols + '>' + PropertyEditor.Util.escapeHtmlTag(this.value) + '</textarea>';
    },
    renderDefault: function() {
        var html = '';
        if (this.defaultValue !== null) {
            html = '<div class="default"><span class="label">' + get_peditor_msg('peditor.default') + '</span><span class="value">' + PropertyEditor.Util.nl2br(PropertyEditor.Util.escapeHtmlTag(this.defaultValue)) + '</span><div class="clear"></div></div>';
        }
        return html;
    }
};
PropertyEditor.Type.TextArea = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.TextArea.prototype);

PropertyEditor.Type.CheckBox = function() {};
PropertyEditor.Type.CheckBox.prototype = {
    shortname: "checkbox",
    getData: function(useDefault) {
        var data = new Object();
        var value = this.value;

        if (this.isDataReady) {
            value = "";
            $('[name=' + this.id + ']:not(.hidden):checkbox:checked').each(function(i) {
                value += $(this).val() + ';';
            });
            if (value !== '') {
                value = value.replace(/;$/i, '');
            } else if (useDefault !== undefined && useDefault &&
                this.defaultValue !== undefined && this.defaultValue !== null) {
                value = this.defaultValue;
            }
        }
        data[this.properties.name] = value;
        PropertyEditor.Util.retrieveHashFieldValue(this, data);
        return data;
    },
    getContainerClass: function() {
        PropertyEditor.Util.retrieveOptionsFromCallback(this, this.properties);
        if (this.properties.options !== undefined && this.properties.options !== null) {
            if (this.properties.options.length === 1 && this.properties.options[0].label === "") {
                this.isTrueFalseField = true;
                return "property-type-checkbox-truefalse";
            }
        }
        return "";
    },
    renderField: function() {
        var thisObj = this;
        var html = '';

        if (this.value === null) {
            this.value = "";
        }

        if (this.properties.options !== undefined && this.properties.options !== null) {
            $.each(this.properties.options, function(i, option) {
                var checked = "";
                $.each(thisObj.value.split(";"), function(i, v) {
                    if (v === option.value) {
                        checked = " checked";
                    }
                });
                var label = PropertyEditor.Util.escapeHtmlTag(option.label);
                if (thisObj.isTrueFalseField) {
                    label = '<span class="hidden_label" style="display:none">' + PropertyEditor.Util.escapeHtmlTag(thisObj.properties.label) + '</span>';
                }
                html += '<span class="multiple_option"><label><input type="checkbox" id="' + thisObj.id + '" name="' + thisObj.id + '" value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + checked + '/>' + label + '</label></span>';
            });
        }
        return html;
    },
    renderDefault: function() {
        var defaultValueText = '';

        if (this.defaultValue === null || this.defaultValue === undefined) {
            this.defaultValue = "";
        }

        var checkbox = this;
        if (this.properties.options !== undefined && this.properties.options !== null) {
            $.each(this.properties.options, function(i, option) {
                $.each(checkbox.defaultValue.split(";"), function(i, v) {
                    if (v !== "" && v === option.value) {
                        defaultValueText += PropertyEditor.Util.escapeHtmlTag(option.label) + ', ';
                    }
                });
            });
        }

        if (defaultValueText !== '') {
            defaultValueText = defaultValueText.substring(0, defaultValueText.length - 2);
            defaultValueText = '<div class="default"><span class="label">' + get_peditor_msg('peditor.default') + '</span><span class="value">' + PropertyEditor.Util.escapeHtmlTag(defaultValueText) + '</span><div class="clear"></div></div>';
        }

        return defaultValueText;
    },
    initScripting: function() {
        PropertyEditor.Util.supportHashField(this);
    }
};
PropertyEditor.Type.CheckBox = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.CheckBox.prototype);

PropertyEditor.Type.Radio = function() {};
PropertyEditor.Type.Radio.prototype = {
    shortname: "radio",
    getData: function(useDefault) {
        var data = new Object();
        var value = this.value;

        if (this.isDataReady) {
            value = $('[name=' + this.id + ']:not(.hidden):checked').val();
            if (value === undefined || value === null || value === "") {
                if (useDefault !== undefined && useDefault &&
                    this.defaultValue !== undefined && this.defaultValue !== null) {
                    value = this.defaultValue;
                } else {
                    value = "";
                }
            }
        }
        data[this.properties.name] = value;
        PropertyEditor.Util.retrieveHashFieldValue(this, data);
        return data;
    },
    renderField: function() {
        var thisObj = this;
        var html = '';

        if (this.value === null) {
            this.value = "";
        }

        PropertyEditor.Util.retrieveOptionsFromCallback(this, this.properties);

        if (this.properties.options !== undefined && this.properties.options !== null) {
            $.each(this.properties.options, function(i, option) {
                var checked = "";
                if (thisObj.value === option.value) {
                    checked = " checked";
                }
                html += '<span class="multiple_option"><label><input type="radio" id="' + thisObj.id + '" name="' + thisObj.id + '" value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + checked + '/>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</label></span>';
            });
        }
        return html;
    },
    initScripting: function() {
        PropertyEditor.Util.supportHashField(this);
    },
    renderDefault: PropertyEditor.Type.CheckBox.prototype.renderDefault
};
PropertyEditor.Type.Radio = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Radio.prototype);

PropertyEditor.Type.ImageRadio = function() {};
PropertyEditor.Type.ImageRadio.prototype = {
    shortname: "imageradio",
    getData: PropertyEditor.Type.Radio.prototype.getData,
    renderField: function() {
        var thisObj = this;
        var html = '';
       
        if (this.value === null) {
            this.value = "";
        }
        
        PropertyEditor.Util.retrieveOptionsFromCallback(this, this.properties);
        
        var modeClass = "";
        var size = "";
        if (this.properties.size !== undefined && this.properties.size !== null && this.properties.size !== "") {
            size = this.properties.size;
        }

        if (this.properties.mode === "inline") { //display the image options in columns
            modeClass = "inline-option";
            if(this.properties.cols !== undefined){
                modeClass += " inline-cols-"+this.properties.cols;
            }
        } else if (this.properties.mode ==="picker"){ //display the images options in a dropdown picker
            modeClass = "imagepicker";
            var imagevalue = "";
            html += '<div id="' + this.id + '_scheme_selector" class="selector"><div class="image_values">';
            $.each(this.properties.options, function(i, option){                
                if (thisObj.value === option.value){
                    imagevalue = PropertyEditor.Util.escapeHtmlTag(PropertyEditor.Util.replaceContextPath(option.image, thisObj.options.contextPath));
                    return false;                    
                }
            });
            html += '<div class="imageoption" style="background:url(\' '+ imagevalue +' \');background-size: 100% 100%;'+size+'">';
            html += '</div>';
            html += '<span class="trigger"><i class="fas fa-chevron-down"></i></span></div><div class="image-input" style="display:none;"><input type="text"/></div><ul style="display:none;">';
            $.each(this.properties.options, function(i, option) {
                var checked = "";
                if (thisObj.value === option.value) {
                    checked = "checked";
                }
                html += '<span class="multiple_option '+modeClass+'"><label><input type="radio" id="' + thisObj.id + '" name="' + thisObj.id + '" value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"'  + checked + '/>\n\
<li data-value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '" class="' + checked + '" name="' + thisObj.id + ' " '+ checked +'><div class="imageoption"  style="background:url(\''+ PropertyEditor.Util.escapeHtmlTag(PropertyEditor.Util.replaceContextPath(option.image, thisObj.options.contextPath)) +'\'); background-size: 100% 100%;'+size+'"></div></li></label></span>';
            });
            html += '</div>';
        }
        
        if (this.properties.options !== undefined && this.properties.options !== null && this.properties.mode !=="picker" ) {
            $.each(this.properties.options, function(i, option) {
                var checked = "";
                if (thisObj.value === option.value) {
                    checked = "checked";
                }
                html += '<span class="multiple_option '+modeClass+'"><label><input type="radio" id="' + thisObj.id + '" name="' + thisObj.id + '" value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + checked + '/><img style="max-width:100%;'+size+'" title="' + PropertyEditor.Util.escapeHtmlTag(option.label) + '" src="' + PropertyEditor.Util.escapeHtmlTag(PropertyEditor.Util.replaceContextPath(option.image, thisObj.options.contextPath)) + '"/></label></span>';
            });
        }
        return html;
    },
    initScripting: function() {
        PropertyEditor.Util.supportHashField(this);
        
        if (this.properties.mode ==="picker"){
            var thisObj = this;
            var selector = $("#" + this.id + "_scheme_selector");

            $(selector).find(".image_values span.trigger").off("click");
            $(selector).find(".image_values span.trigger").on("click", function(){
                $(selector).toggleClass("showPicker");
            });
            $(selector).find("li").off("click");
            $(selector).find("li").on("click", function(){
                $(selector).find("li").removeClass("checked");
                $(this).addClass("checked");
                thisObj.renderValue();
                $(selector).removeClass("showPicker");
            });
        }
        this.isDataReady = true;
    },
    renderValue : function() {
        var selector = $("#" + this.id + "_scheme_selector");
        if ($(selector).find("li.checked").length > 0) {
            $(selector).find(".image_values div.imageoption").remove();
            $(selector).find(".image_values").prepend($(selector).find("li.checked").html());
        }
    },
    renderDefault: PropertyEditor.Type.CheckBox.prototype.renderDefault
};
PropertyEditor.Type.ImageRadio = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.ImageRadio.prototype);

PropertyEditor.Type.IconButtons = function() {};
PropertyEditor.Type.IconButtons.prototype = {
    shortname: "iconbuttons",
    getData: PropertyEditor.Type.Radio.prototype.getData,
    renderField: function() {
        var thisObj = this;
        var html = '<div class="btn-group btn-group-sm btn-group-fullwidth clearfix" role="group">';

        if (this.value === null) {
            this.value = "";
        }

        PropertyEditor.Util.retrieveOptionsFromCallback(this, this.properties);

        if (this.properties.options !== undefined && this.properties.options !== null) {
            $.each(this.properties.options, function(i, option) {
                var checked = "";
                if (thisObj.value === option.value) {
                    checked = " checked";
                }
                html += '<input type="radio" autocomplete="off" class="btn-check" id="' + thisObj.id + '_'+option.value+'" name="' + thisObj.id + '" value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + checked + '/><label class="btn btn-outline-secondary " for="' + thisObj.id + '_'+option.value+'" title="' + PropertyEditor.Util.escapeHtmlTag(option.title) + '">'+option.label+'</label>';
            });
        }
        
        html += '</div>';
        return html;
    },
    initScripting: function() {
        PropertyEditor.Util.supportHashField(this);
    },
    renderDefault: PropertyEditor.Type.CheckBox.prototype.renderDefault
};
PropertyEditor.Type.IconButtons = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.IconButtons.prototype);

PropertyEditor.Type.SelectBox = function() {};
PropertyEditor.Type.SelectBox.prototype = {
    shortname: "selectbox",
    renderField: function() {
        var thisObj = this;
        var html = '<select id="' + this.id + '" name="' + this.id + '" class="initChosen">';

        if (this.value === null) {
            this.value = "";
        }
        
        PropertyEditor.Util.retrieveOptionsFromCallback(this, this.properties);

        if (this.properties.options !== undefined && this.properties.options !== null) {
            $.each(this.properties.options, function(i, option) {
                var selected = "";
                if (thisObj.value === option.value) {
                    selected = " selected";
                }
                html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
            });
        }
        html += '</select>';
        
        if (thisObj.options.appPath !== undefined && thisObj.options.appPath !== "") {
            html += thisObj.builderLink();
        }
        
        return html;
    },
    builderLink : function() {
        var thisObj = this;
        var html = "";
        var builder = "";
        
        var lname = thisObj.properties.name.toLowerCase();
        
        if (lname.indexOf("formid") !== -1 || lname.indexOf("formdefid") !== -1) {
            builder = "form";
        } else if (lname.indexOf("listid") !== -1 || lname.indexOf("listdefid") !== -1 ||
                   lname.indexOf("datalistid") !== -1 || lname.indexOf("datalistdefid") !== -1) {
            builder = "datalist";
        } else if (lname.indexOf("userviewid") !== -1 || lname.indexOf("userviewdefid") !== -1) {
            builder = "userview";
        } else if (lname.indexOf("processid") !== -1 || lname.indexOf("processdefid") !== -1) {
            builder = "process";
        } else if (typeof CustomBuilder !== "undefined" && CustomBuilder !== null 
                && CustomBuilder.builderTypes !== undefined) {
            for (var i in CustomBuilder.builderTypes) {
                var key = CustomBuilder.builderTypes[i];
                if (lname.indexOf(key+"id") !== -1 || lname.indexOf(key+"defid") !== -1) {
                    builder = "cbuilder/" + key;
                }
            }
        }
        
        if (builder !== "") {
            if (builder !== "process") {
                html += " <a class=\"builderAddNew\" data-type=\""+builder+"\" title=\""+get_peditor_msg('peditor.addNewElement')+"\"><i class=\"fas fa-plus-circle\"></i></i></a>";
            }
            html += " <a href=\"\" target=\"_blank\" class=\"openbuilder\" data-type=\""+builder+"\" style=\"display:none;\" title=\""+get_peditor_msg('peditor.openBuilder')+"\"><i class=\"fas fa-external-link-alt\"></i></i></a>";
        }
        
        return html;
    },
    renderDefault: PropertyEditor.Type.CheckBox.prototype.renderDefault,
    handleAjaxOptions: function(options, reference) {
        var thisObj = this;
        if (options !== null && options !== undefined) {
            this.properties.options = options;
            var html = "";

            var value = $("#" + this.id).val();
            if (value === "" || value === null) {
                value = thisObj.value;
            }
            $.each(this.properties.options, function(i, option) {
                var selected = "";
                if (value === option.value) {
                    selected = " selected";
                }
                html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
            });
            $("#" + this.id).html(html);
            $("#" + this.id).trigger("change");
            $("#" + this.id).trigger("chosen:updated");
        }
    },
    initScripting: function() {
        var field = this;
        if (UI.rtl) {
            $("#" + this.id).addClass("chosen-rtl");
        }
        $("#" + this.id).chosen({ width: "54%", placeholder_text: " " });
        
        if (this.properties.html === "true") {
            $("#" + this.id)
            .off('chosen:showing_dropdown.updatelabel chosen:hiding_dropdown.updatelabel chosen:ready.updatelabel chosen:updated.updatelabel change.updatelabel keyup.updatelabel')
            .on('chosen:showing_dropdown.updatelabel chosen:hiding_dropdown.updatelabel chosen:ready.updatelabel chosen:updated.updatelabel change.updatelabel keyup.updatelabel', function() {
                $("#" + field.id).next().find(".chosen-results li, .chosen-single > span, .search-choice > span").each(function() {
                    var html = $(this).text();
                    if (html.indexOf('<') !== -1) {
                        $(this).html(html);
                    }
                });
            });
            setTimeout(function() {
                $("#" + field.id).trigger("keyup");
            }, 100);
        }

        //support options_label_processor for selectobox & multiselect
        if (this.properties.options_label_processor !== undefined && this.properties.options_label_processor !== null) {
            var processors = this.properties.options_label_processor.split(";");
            var updateLabel = function(chosen) {
                $(chosen.container).find(".chosen-results li, .chosen-single > span, .search-choice > span").each(function() {
                    var html = $(this).html();
                    var regex = new RegExp("\\[(.*)<em>(.*)\\]", "g");
                    html = html.replace(regex, "[$1$2]");
                    regex = new RegExp("\\[(.*)</em>(.*)\\]", "g");
                    html = html.replace(regex, "[$1$2]");
                    for (var i in processors) {
                        if ("color" === processors[i]) {
                            var regex = new RegExp("\\[color\\](.+)\\[/color\\]", "g");
                            html = html.replace(regex, "<span style=\"background:$1;width:10px;height:10px;min-height:auto;min-width:auto;display:inline-block;margin:0 2px;\"></span>");
                        }
                    }
                    $(this).html(html);
                });
            }
            $("#" + this.id).off("chosen:showing_dropdown");
            $("#" + this.id).on("chosen:showing_dropdown", function(evt, chosen) {
                updateLabel(chosen.chosen);
            });
            $("#" + this.id).off("chosen:hiding_dropdown");
            $("#" + this.id).on("chosen:hiding_dropdown", function(evt, chosen) {
                updateLabel(chosen.chosen);
            });
            $("#" + this.id).off("chosen:ready");
            $("#" + this.id).on("chosen:ready", function(evt) {
                updateLabel($("#" + field.id).data("chosen"));
            });
            $("#" + this.id).off("chosen:updated");
            $("#" + this.id).on("chosen:updated", function(evt) {
                updateLabel($("#" + field.id).data("chosen"));
            });
            $("#" + this.id).off("change.updatelabel");
            $("#" + this.id).on("change.updatelabel", function() {
                updateLabel($("#" + field.id).data("chosen"));
            });
            setTimeout(function() {
                if ($("#" + field.id).length > 0) {
                    $($("#" + field.id).data("chosen").container).find(".chosen-search input").off("keydown");
                    $($("#" + field.id).data("chosen").container).find(".chosen-search input").on("keydown", function() {
                        setTimeout(function() { updateLabel($("#" + field.id).data("chosen")); }, 5);
                    });
                }
            }, 1000);
            updateLabel($("#" + field.id).data("chosen"));
        }
        
        if ($("#" + field.id + "_input a.openbuilder").length > 0) {
            var updateLink = function() {
                var value = $("#" + field.id).val();
                if (value !== "" && value !== undefined && value !== null) {
                    var builder = $("#" + field.id + "_input a.openbuilder").data("type");
                    var url = "";
                    
                    if (builder.indexOf("cbuilder") !== -1) {
                        url = field.options.contextPath + "/web/console/app" + field.options.appPath + "/" + builder + "/design";
                    } else {
                        url = field.options.contextPath + "/web/console/app" + field.options.appPath + "/" + builder + "/builder";
                    }
                    
                    if (builder === "process") {
                        url += "#" + value;
                    } else {
                        url += "/" + value;
                    }
                    
                    $("#" + field.id + "_input a.openbuilder").attr("href", url);
                    $("#" + field.id + "_input a.openbuilder").show();
                    $("#" + field.id + "_input").addClass("builder-link");
                } else {
                    $("#" + field.id + "_input a.openbuilder").hide();
                    $("#" + field.id + "_input").removeClass("builder-link");
                }
            };
            $("#" + field.id).off("change.builder");
            $("#" + field.id).on("change.builder", function() {
                updateLink();
            });
            updateLink();
        }
        
        if ($("#" + field.id + "_input a.builderAddNew").length > 0) {
            $("#" + field.id + "_input a.builderAddNew").off("click");
            $("#" + field.id + "_input a.builderAddNew").on("click", function(){
                var type = $(this).data("type");
                var url = CustomBuilder.contextPath + '/web/console/app' + CustomBuilder.appPath + '/' + type + '/create?builderMode=true';
                JPopup.show("navCreateNewDialog", url, {}, "");
                $('iframe#navCreateNewDialog').data('field', field);
            });
        }

        PropertyEditor.Util.supportHashField(this);
    },
    addNewOption: function(id, label) {
        var field = this;
        $("#" + field.id).append('<option value="'+PropertyEditor.Util.escapeHtmlTag(id)+'">'+PropertyEditor.Util.escapeHtmlTag(label)+'</option>');
        $("#" + field.id).val(PropertyEditor.Util.escapeHtmlTag(id));
        
        $("#" + field.id).trigger("change");
        $("#" + field.id).trigger("chosen:updated");
        
        if (field.properties.options_ajax !== null && field.properties.options_ajax !== undefined && field.properties.options_ajax !== "") {
            //clear cache
            PropertyEditor.Util.clearAjaxOptionsCache(this);
        } else if (field.properties.options_callback_addoption !== undefined && field.properties.options_callback_addoption !== null && field.properties.options_callback_addoption !== "") {
            var func = PropertyEditor.Util.getFunction(field.properties.options_callback_addoption);
            if ($.isFunction(func)) {
                func(field.properties, {"value" : id, "label" : label});
            }
        }
    },
    pageShown: function() {
        $("#" + this.id).trigger("chosen:updated");
    }
};
PropertyEditor.Type.SelectBox = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.SelectBox.prototype);

PropertyEditor.Type.MultiSelect = function() {};
PropertyEditor.Type.MultiSelect.prototype = {
    shortname: "multiselect",
    getData: function(useDefault) {
        var data = new Object();
        var value = this.value;

        if (this.isDataReady) {
            value = "";
            var values = $('[name=' + this.id + ']:not(.hidden)').val();
            for (num in values) {
                if (values[num] !== "") {
                    value += values[num] + ';';
                }
            }
            if (value !== '') {
                value = value.replace(/;$/i, '');
            } else if (useDefault !== undefined && useDefault &&
                this.defaultValue !== undefined && this.defaultValue !== null) {
                value = this.defaultValue;
            }
        }
        data[this.properties.name] = value;
        PropertyEditor.Util.retrieveHashFieldValue(this, data);
        return data;
    },
    renderField: function() {
        var thisObj = this;
        if (this.value === null) {
            this.value = "";
        }

        var size = '';
        if (this.properties.size !== undefined && this.properties.size !== null) {
            size = ' size="' + this.properties.size + '"';
        }

        var html = '<select id="' + this.id + '" name="' + this.id + '" multiple' + size + ' class="initChosen">';

        PropertyEditor.Util.retrieveOptionsFromCallback(this, this.properties);

        if (this.properties.options !== undefined && this.properties.options !== null) {
            $.each(this.properties.options, function(i, option) {
                var selected = "";
                $.each(thisObj.value.split(";"), function(i, v) {
                    if (v === option.value) {
                        selected = " selected";
                    }
                });
                html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
            });
        }
        html += '</select>';

        return html;
    },
    handleAjaxOptions: function(options, reference) {
        var thisObj = this;
        if (options !== null && options !== undefined) {
            this.properties.options = options;

            var values = $("#" + this.id).val();
            if (values !== null && !$.isArray(values)) {
                values = [values];
            }
            if (values === null || values.length === 0 || (values.length === 1 && values[0] === "")) {
                values = thisObj.value.split(";");
            }

            var html = "";
            $.each(this.properties.options, function(i, option) {
                var selected = "";
                $.each(values, function(i, v) {
                    if (v === option.value) {
                        selected = " selected";
                    }
                });
                html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
            });
            $("#" + this.id).html(html);
            $("#" + this.id).trigger("change");
            $("#" + this.id).trigger("chosen:updated");
        }
    },
    renderDefault: PropertyEditor.Type.CheckBox.prototype.renderDefault,
    initScripting: PropertyEditor.Type.SelectBox.prototype.initScripting,
    pageShown: PropertyEditor.Type.SelectBox.prototype.pageShown
};
PropertyEditor.Type.MultiSelect = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.MultiSelect.prototype);

PropertyEditor.Type.SortableSelect = function() {};
PropertyEditor.Type.SortableSelect.prototype = {
    shortname: "sortableselect",
    getData: function(useDefault) {
        var data = new Object();
        var value = this.value;

        if (this.isDataReady) {
            value = "";
            $('[name=' + this.id + ']:not(.hidden) option').each(function() {
                value += $(this).val() + ';';
            });
            if (value !== '') {
                value = value.replace(/;$/i, '');
            } else if (useDefault !== undefined && useDefault &&
                this.defaultValue !== undefined && this.defaultValue !== null) {
                value = this.defaultValue;
            }
        }
        data[this.properties.name] = value;
        PropertyEditor.Util.retrieveHashFieldValue(this, data);
        return data;
    },
    renderField: function() {
        var thisObj = this;
        if (this.value === null) {
            this.value = "";
        }

        var size = ' size="8"';

        var values = thisObj.value.split(";");

        PropertyEditor.Util.retrieveOptionsFromCallback(this, this.properties);

        var html = '<select id="' + this.id + '_options" class="options" name="' + this.id + '_options" multiple' + size + '>';
        if (this.properties.options !== undefined && this.properties.options !== null) {
            $.each(this.properties.options, function(i, option) {
                if (option.value !== "") {
                    var selected = "";
                    $.each(values, function(i, v) {
                        if (v === option.value) {
                            selected = " class=\"selected\"";
                        }
                    });
                    html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
                }
            });
        }
        html += '</select>';
        html += '<div class="sorted_select_control"><button class="selectAll btn"><i class="fas fa-angle-double-right" aria-hidden="true"></i></button><button class="select btn"><i class="fas fa-angle-right" aria-hidden="true"></i></button><button class="unselect btn"><i class="fas fa-angle-left" aria-hidden="true"></i></button><button class="unselectAll btn"><i class="fas fa-angle-double-left" aria-hidden="true"></i></button></div>';
        html += '<select id="' + this.id + '" name="' + this.id + '" multiple' + size + '>';
        if (this.properties.options !== undefined && this.properties.options !== null && values.length > 0) {
            $.each(values, function(i, v) {
                $.each(thisObj.properties.options, function(i, option) {
                    if (v === option.value) {
                        html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '">' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
                    }
                });
            });
        }
        html += '</select>';
        html += '<div class="sorted_select_control sort"><button class="moveup btn"><i class="fas fa-angle-up" aria-hidden="true"></i></button><button class="movedown btn"><i class="fas fa-angle-down" aria-hidden="true"></i></button></div>';

        return html;
    },
    handleAjaxOptions: function(options, reference) {
        var thisObj = this;
        if (options !== null && options !== undefined) {
            this.properties.options = options;
            var html = "";

            var isInit = true;
            var values = thisObj.value.split(";");
            if ($("#" + thisObj.id + "_options option").length > 0) {
                isInit = false;
                values = [];
                $("#" + thisObj.id + " option").each(function() {
                    values.push($(this).val());
                });
            }

            $.each(this.properties.options, function(i, option) {
                if (option.value !== "") {
                    var selected = "";
                    $.each(values, function(i, v) {
                        if (v === option.value) {
                            selected = " class=\"selected\"";
                        }
                    });
                    html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
                }
            });
            $("#" + thisObj.id + "_options").html(html);

            if (isInit) {
                $.each(values, function(i, v) {
                    var selected = $("#" + thisObj.id + "_options").find("option[value='" + v + "']");
                    if (selected.length > 0) {
                        var option = $(selected).clone();
                        $("#" + thisObj.id).append(option);
                        $(selected).addClass("selected");
                    }
                });
            } else {
                $("#" + thisObj.id + "option").each(function() {
                    var value = $(this).val();
                    if ($("#" + thisObj.id + "_options").find("option[value='" + value + "']").length === 0) {
                        $(this).remove();
                    }
                });
                $("#" + thisObj.id + " option").each(function() {
                    var value = $(this).val();
                    if ($("#" + thisObj.id + "_options").find("option[value='" + value + "']").length === 0) {
                        $(this).remove();
                    }
                });
                $("#" + thisObj.id + "_options option.selected").each(function() {
                    var value = $(this).val();
                    if ($("#" + thisObj.id).find("option[value='" + value + "']").length === 0) {
                        var option = $(this).clone();
                        $("#" + thisObj.id).append(option);
                    }
                });
            }
            $("#" + thisObj.id).trigger("change");
        }
    },
    renderDefault: PropertyEditor.Type.CheckBox.prototype.renderDefault,
    optionsSelectAll: function() {
        var thisObj = this;
        $("#" + thisObj.id + "_options option").each(function() {
            var value = $(this).val();
            if ($("#" + thisObj.id).find("option[value='" + value + "']").length === 0) {
                var option = $(this).clone();
                $("#" + thisObj.id).append(option);
            }
            $(this).addClass("selected");
        });
    },
    optionsSelect: function() {
        var thisObj = this;
        $("#" + thisObj.id + "_options option:selected").each(function() {
            var value = $(this).val();
            if ($("#" + thisObj.id).find("option[value='" + value + "']").length === 0) {
                var option = $(this).clone();
                $("#" + thisObj.id).append(option);
            }
            $(this).addClass("selected");
        });
    },
    optionsUnselect: function() {
        var thisObj = this;
        $("#" + thisObj.id + " option:selected").each(function() {
            var value = $(this).val();
            $("#" + thisObj.id + "_options").find("option[value='" + value + "']").removeClass("selected");
            $(this).remove();
        });
    },
    optionsUnselectAll: function() {
        var thisObj = this;
        $("#" + thisObj.id + " option").remove();
        $("#" + thisObj.id + "_options option").removeClass("selected");
    },
    optionsMoveUp: function() {
        var thisObj = this;
        $("#" + thisObj.id + " option:selected").each(function() {
            var prev = $(this).prev();
            if (prev !== undefined) {
                $(prev).before($(this));
            }
        });
    },
    optionsMoveDown: function() {
        var thisObj = this;
        $("#" + thisObj.id + " option:selected").each(function() {
            var next = $(this).next();
            if (next !== undefined) {
                $(next).after($(this));
            }
        });
    },
    initScripting: function() {
        var element = $("#" + this.id);
        var container = $(element).parent();
        var field = this;

        //selectAll
        $(container).find('button.selectAll').click(function() {
            field.optionsSelectAll();
            element.trigger("change");
            return false;
        });

        //select
        $(container).find('button.select').click(function() {
            field.optionsSelect();
            element.trigger("change");
            return false;
        });

        //unselect
        $(container).find('button.unselect').click(function() {
            field.optionsUnselect();
            element.trigger("change");
            return false;
        });

        //unslectAll
        $(container).find('button.unselectAll').click(function() {
            field.optionsUnselectAll();
            element.trigger("change");
            return false;
        });

        //moveup
        $(container).find('button.moveup').click(function() {
            field.optionsMoveUp();
            element.trigger("change");
            return false;
        });

        //movedown
        $(container).find('button.movedown').click(function() {
            field.optionsMoveDown();
            element.trigger("change");
            return false;
        });

        PropertyEditor.Util.supportHashField(this);
    },
    pageShown: function() {
        //do nothing
    }
};
PropertyEditor.Type.SortableSelect = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.SortableSelect.prototype);

PropertyEditor.Type.Grid = function() {};
PropertyEditor.Type.Grid.prototype = {
    shortname: "grid",
    options_sources: {},
    getData: function(useDefault) {
        var field = this;
        var data = new Object();

        if (this.isDataReady) {
            var gridValue = new Array();
            if (!field.isHidden()) {
                $('#' + this.id + ' tr').each(function(tr) {
                    var row = $(this);
                    if (!$(row).hasClass("grid_model") && !$(row).hasClass("grid_header")) {
                        var obj = new Object();

                        $.each(field.properties.columns, function(i, column) {
                            if (column.type !== "truefalse") {
                                obj[column.key] = $(row).find('input[name=' + column.key + '], select[name=' + column.key + ']').val();
                                if (obj[column.key] !== null && obj[column.key] !== undefined) {
                                    obj[column.key] = obj[column.key].trim();
                                }
                            } else {
                                if ($(row).find('input[name=' + column.key + ']').is(":checked")) {
                                    obj[column.key] = (column.true_value !== undefined) ? column.true_value : 'true';
                                } else {
                                    obj[column.key] = (column.false_value !== undefined) ? column.false_value : 'false';
                                }
                            }
                        });
                        gridValue.push(obj);
                    }
                });
                if (gridValue.length === 0 && useDefault !== undefined && useDefault &&
                    this.defaultValue !== null && this.defaultValue !== undefined) {
                    gridValue = this.defaultValue;
                }
                data[this.properties.name] = gridValue;
            }
        } else {
            data[this.properties.name] = this.value;
        }

        return data;
    },
    addOnValidation: function(data, errors, checkEncryption) {
        var thisObj = this;
        var wrapper = $('#' + this.id + '_input');
        var table = $("#" + this.id);
        $(table).find("td").removeClass("error");

        var value = data[this.properties.name];
        var hasError = false;
        if ($.isArray(value) && value.length > 0) {
            $.each(value, function(i, row) {
                $.each(thisObj.properties.columns, function(j, column) {
                    if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                        if (row[column.key] === undefined || row[column.key] === null || row[column.key] === "") {
                            var td = $(table).find("tr:eq(" + (i + 2) + ") td:eq(" + j + ")");
                            $(td).addClass("error");
                            hasError = true;
                        }
                    }
                });
            });
        }

        if (hasError) {
            var obj = new Object();
            obj.field = this.properties.name;
            obj.fieldName = this.properties.label;
            obj.message = this.options.mandatoryMessage;
            errors.push(obj);
            $(wrapper).append('<div class="property-input-error">' + obj.message + '</div>');
        }
    },
    renderField: function() {
        var thisObj = this;
        var html = '<table id="' + this.id + '" class="grid"><tr class="grid_header">';
        html += '<th class="property-type-grid-row-header">'+thisObj.properties.label+'</th>';
        //render header
        $.each(this.properties.columns, function(i, column) {
            var required = "";
            if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
            }
            html += '<th value="' + i + '"><span>' + column.label + '</span> <i class="fa fa-sort"></i>' + required + '</th>';
        });
        html += '<th class="property-type-grid-action-column"></th></tr>';

        //render model
        html += '<tr class="grid_model" style="display:none">';
        html += '<th class="property-type-grid-row-header">'+thisObj.properties.label+'</th>';
        $.each(this.properties.columns, function(i, column) {
            var required = "";
            if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
            }
            var tdclass = column.type;
            if (tdclass === undefined) {
                tdclass = "";
            }
            html += '<td class="'+tdclass+'"><span class="label"><span>'+column.label+'</span> '+required+'</span><span>';

            PropertyEditor.Util.retrieveOptionsFromCallback(thisObj, column, column.key);

            if (column.type === "truefalse") {
                column.true_value = (column.true_value !== undefined) ? column.true_value : 'true';
                html += '<label><input name="' + column.key + '" type="checkbox" value="' + column.true_value + '"/><span class="hidden_label">'+column.label+'</span></label>';
            } else if (column.options !== undefined || column.options_ajax !== undefined) {
                if (column.type === "autocomplete") {
                    thisObj.updateSource(column.key, column.options);
                    html += '<input name="' + column.key + '" class="autocomplete" size="10" value=""/>';
                } else {
                    html += '<select name="' + column.key + '" data-value="">';
                    if (column.options !== undefined) {
                        $.each(column.options, function(i, option) {
                            html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '">' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
                        });
                    }
                    html += '</select>';
                }
            } else if (column.type === "number") {
                html += '<input name="' + column.key + '" type="number" size="10" value=""/>';
            } else {
                html += '<input name="' + column.key + '" size="10" value=""/>';
            }
            html += '</span></td>';
        });
        html += '<td class="property-type-grid-action-column">';
        html += '<a href="#" class="property-type-grid-action-moveup"><i class="fas fa-chevron-circle-up"></i><span>' + get_peditor_msg('peditor.moveUp') + '</span></a>';
        html += ' <a href="#" class="property-type-grid-action-movedown"><i class="fas fa-chevron-circle-down"></i><span>' + get_peditor_msg('peditor.moveDown') + '</span></a>';
        html += ' <a href="#" class="property-type-grid-action-delete"><i class="fas fa-times-circle"></i><span>' + get_peditor_msg('peditor.delete') + '</span></a>';
        html += '</td></tr>';

        //render value
        if (this.value !== null) {
            $.each(this.value, function(i, row) {
                html += '<tr>';
                html += '<th class="property-type-grid-row-header">'+thisObj.properties.label+'</th>';
                $.each(thisObj.properties.columns, function(i, column) {
                    var columnValue = "";
                    if (row[column.key] !== undefined) {
                        columnValue = row[column.key];
                    }
                    
                    var required = "";
                    if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                        required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
                    }
                    var tdclass = column.type;
                    if (tdclass === undefined) {
                        tdclass = "";
                    }
                    
                    html += '<td class="'+tdclass+'"><span class="label"><span>'+column.label+'</span> '+required+'</span><span>';

                    if (column.type === "truefalse") {
                        var checked = "";
                        if (columnValue === column.true_value) {
                            checked = "checked";
                        }
                        html += '<label><input name="' + column.key + '" type="checkbox" ' + checked + ' value="' + column.true_value + '"/><span class="hidden_label">'+column.label+'</span></label>';
                    } else if (column.options !== undefined || column.options_ajax !== undefined) {
                        if (column.type === "autocomplete") {
                            html += '<input name="' + column.key + '" class="autocomplete" size="10" value="' + PropertyEditor.Util.escapeHtmlTag(columnValue) + '"/>';
                        } else {
                            html += '<select name="' + column.key + '" data-value="' + columnValue + '" class="initFullWidthChosen">';
                            if (column.options !== undefined) {
                                $.each(column.options, function(i, option) {
                                    var selected = "";
                                    if (columnValue === option.value) {
                                        selected = " selected";
                                    }
                                    html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
                                });
                            }
                            html += '</select>';
                        }
                    } else if (column.type === "number") {
                        html += '<input name="' + column.key + '" size="10" type="number" value="' + PropertyEditor.Util.escapeHtmlTag(columnValue) + '"/>';
                    } else {
                        html += '<input name="' + column.key + '" size="10" value="' + PropertyEditor.Util.escapeHtmlTag(columnValue) + '"/>';
                    }
                    html += '</span></td>';
                });

                html += '<td class="property-type-grid-action-column">';
                html += '<a href="#" class="property-type-grid-action-moveup"><i class="fas fa-chevron-circle-up"></i><span>' + get_peditor_msg('peditor.moveUp') + '</span></a>';
                html += ' <a href="#" class="property-type-grid-action-movedown"><i class="fas fa-chevron-circle-down"></i><span>' + get_peditor_msg('peditor.moveDown') + '</span></a>';
                html += ' <a href="#" class="property-type-grid-action-delete"><i class="fas fa-times-circle"></i><span>' + get_peditor_msg('peditor.delete') + '</span></a>';
                html += '</td></tr>';
            });
        }
        
        html += '</table><a href="#" class="property-type-grid-action-add"><i class="fas fa-plus-circle"></i><span>' + get_peditor_msg('peditor.add') + '</span></a>';                
        
        html += this.renderSortControl();
        
        return html;
    },
    renderSortControl: function() {
        //Sorting selectbox
        var html = '<div class="grid_sorting_container"><a class="grid_sorting_config"><i class="las la-sort-amount-down-alt"></i></a><div class="grid_sorting_field"><select id="SortingOption">';
        $.each(this.properties.columns, function(i, column) {
            html += ' <option value="' + i +'"> '+ column.label +' </option>';
        });
        html += '</select><select id="SortingOrderOption">';
        html += '<option value="asc">' + get_peditor_msg('peditor.sort.asc') + '</option><option value="desc">' + get_peditor_msg('peditor.sort.desc') + '</option>';
        html += '</select><button class="sortingTable">' + get_peditor_msg('peditor.sort') + '</button></div></div>';
        return html;
    },
    renderDefault: function() {
        var thisObj = this;
        var defaultValueText = '';
        if (this.defaultValue !== null) {
            $.each(thisObj.defaultValue, function(i, row) {
                $.each(thisObj.properties.columns, function(i, column) {
                    var columnValue = "";
                    if (row[column.key] !== undefined) {
                        columnValue = row[column.key];
                    }

                    if (column.options !== undefined) {
                        $.each(column.options, function(i, option) {
                            if (columnValue === option.value) {
                                defaultValueText += PropertyEditor.Util.escapeHtmlTag(option.label) + '; ';
                            }
                        });
                    } else {
                        defaultValueText += columnValue + '; ';
                    }
                });
                defaultValueText += '<br/>';
            });
        }
        if (defaultValueText !== '') {
            defaultValueText = '<div class="default"><span class="label">' + get_peditor_msg('peditor.default') + '</span><span class="value">' + defaultValueText + '</span><div class="clear"></div></div>';
        }
        return defaultValueText;
    },
    getContainerClass: function() {
        return "property-grid";
    },
    initScripting: function() {
        var table = $("#" + this.id);
        var grid = this;

        $(table).find("select.initFullWidthChosen").each(function() {
            if (UI.rtl) {
                $(this).addClass("chosen-rtl");
            }
            $(this).chosen({ width: "100%", placeholder_text: " " });
        });

        $(table).find("input.autocomplete").each(function() {
            var key = $(this).attr("name");
            $(this).autocomplete({
                source: grid.options_sources[grid.id + ":" + key],
                minLength: 0,
                open: function() {
                    $(this).autocomplete('widget').css('z-index', 99999);
                    return false;
                }
            });
        });
        
        //sorting (button)
        $("#" + this.id + "_input").find('.sortingTable').off("click");
        $("#" + this.id + "_input").find('.sortingTable').on("click", function() {
            let chosenValue = $(this).parent().find('#SortingOption').val();
            let chosenOrder = $(this).parent().find('#SortingOrderOption').val();
            grid.sortTable(chosenValue, chosenOrder);
        });
        
        //sorting (headers) 
        $(table).find('.grid_header th').off("click");
        $(table).find('.grid_header th').on("click", function() {
            let headerValue = $(this).attr('value');
            grid.sortTable(headerValue, !$(this).hasClass('sort_asc')?"asc":"desc");
        });

        //add
        $(table).next('a.property-type-grid-action-add').off("click");
        $(table).next('a.property-type-grid-action-add').on("click", function() {
            grid.gridActionAdd(this);
            return false;
        });

        //delete
        $(table).find('a.property-type-grid-action-delete').off("click");
        $(table).find('a.property-type-grid-action-delete').on("click", function() {
            grid.gridActionDelete(this);
            table.trigger("change");
            return false;
        });

        //move up
        $(table).find('a.property-type-grid-action-moveup').off("click");
        $(table).find('a.property-type-grid-action-moveup').on("click", function() {
            grid.gridActionMoveUp(this);
            table.trigger("change");
            return false;
        });

        //move down
        $(table).find('a.property-type-grid-action-movedown').off("click");
        $(table).find('a.property-type-grid-action-movedown').on("click", function() {
            grid.gridActionMoveDown(this);
            table.trigger("change");
            return false;
        });
        
        $(table).off("click.collapsible", ".property-type-grid-row-header");
        $(table).on("click.collapsible", ".property-type-grid-row-header", function(){
            $(this).toggleClass("collapsed");
        });

        grid.gridDisabledMoveAction(table);

        $.each(grid.properties.columns, function(i, column) {
            if ((column.options_ajax !== undefined && column.options_ajax !== null) || (column.options_callback_on_change !== undefined && column.options_callback_on_change !== null)) {
                PropertyEditor.Util.handleOptionsField(grid, column.key, column.options_ajax, column.options_ajax_on_change, column.options_ajax_mapping, column.options_ajax_method, column.options_extra);
            }
        });
    },
    handleAjaxOptions: function(options, reference) {
        var grid = this;
        if (options !== null && options !== undefined) {
            if (this.options_sources[grid.id + ":" + reference] !== undefined) {
                this.updateSource(reference, options);
            } else {
                var filter = null;
                $.each(grid.properties.columns, function(i, column) {
                    if (column.key === reference && column.options_ajax_row_regex_filter !== undefined && column.options_ajax_row_regex_filter !== "") {
                        filter = column.options_ajax_row_regex_filter;
                    }
                });

                var html = "";
                $.each(options, function(i, option) {
                    html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '">' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
                });
                var change = false;
                $("#" + grid.id + " [name='" + reference + "']").each(function() {
                    var val = $(this).val();
                    if (val === "" || val === null) {
                        val = $(this).data("value");
                    }
                    $(this).html(html);

                    if (filter !== null) {
                        var tempFilter = filter;
                        $(this).closest("tr").find("[name]").each(function() {
                            var name = $(this).attr("name");
                            var val = $(this).val();
                            if (val === null) {
                                val = $(this).data("value");
                            }
                            tempFilter = tempFilter.replace("${" + name + "}", val);
                        });
                        var regex = new RegExp(tempFilter);

                        $(this).find("option").each(function() {
                            var option_value = $(this).val();
                            if (option_value !== "") {
                                var result = regex.exec(option_value);
                                if (!(result !== null && result.length > 0 && result[0] === option_value)) {
                                    $(this).remove();
                                }
                            }
                        });
                    }

                    if ($(this).hasClass("initFullWidthChosen")) {
                        $(this).val(val);
                        $(this).trigger("chosen:updated");
                    }
                    if ($(this).val() !== val) {
                        change = true;
                    }
                });
                if (change) {
                    $("#" + grid.id).trigger("change");
                }
            }
        }
    },
    sortTable: function(n, dir){
        let grid = this;
        let table = $("#" + this.id)[0];
        
        if (dir === undefined) {
            //Sorting direction set to ascending:
            dir = "asc";
        }
        
        //method to retrieve cell value based on input type
        let getCellValue = function(cell) {
            let cellValue = "";
            if ($(cell).find('select').length > 0) {
                if ($(cell).find('select option:checked').length > 0) {
                    cellValue = $(cell).find('select option:checked').text();
                }
            } else if ($(cell).find('input[type="checkbox"]').length > 0) { //checks for checkbox
                if ($(cell).find('input[type="checkbox"]').is(":checked")) {
                    cellValue = "true";
                }
            } else {
                cellValue = $(cell).find('input').val();
            }
            return cellValue;
        };
        
        let rows, switching = true, i, x, y, xCell, yCell, shouldSwitch;
        
        /*Loop that will continue until
        no switching has been done:*/
        while (switching) {
            //start by saying: no switching is done:
            switching = false;
            rows = table.rows;
            /*Loop through all table rows (first row is skipped because its the header,
             * second row is skipped cus its the grid-model*/
            for (i = 2; i < (rows.length - 1); i++) {
                //start by saying there should be no switching:
                shouldSwitch = false;
                /*Get two elements to compare,
                one from current row and one from the next:*/
                
                x = getCellValue($(rows[i].getElementsByTagName("td")[n]));
                y = getCellValue($(rows[i+1].getElementsByTagName("td")[n]));

                //prevents infinite looping of the if(x.trim() == '') statement
                if (x.trim() === '' && y.trim() === '' ) {
                    continue;
                } else if (x.trim() === '') { //moves empty cells to the bottom of the row
                    shouldSwitch= true;
                    break;
                } else {
                     /* check if the two rows should switch place,
                        based on the direction, asc or desc:*/
                    if (dir === "asc") {
                        if ((x.toLowerCase().localeCompare(y.toLowerCase(), undefined,{numeric:true})) > 0 && !(y.trim() === '')) {
                            //if == 1, mark as a switch and break the loop:
                            shouldSwitch= true;
                            break;
                        } 
                    } else if (dir === "desc") {
                        if ((y.toLowerCase().localeCompare(x.toLowerCase(), undefined,{numeric:true})) > 0 && !(y.trim() === '')) {
                            //if == 1, mark as a switch and break the loop:
                            shouldSwitch= true;
                            break;
                        }
                    }
                }
            }
            if (shouldSwitch) {
                /*If a switch has been marked, make the switch
                and mark that a switch has been done:*/
                rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
                switching = true;
            }
        }     
        
        //changes all sort icon back to default
        $(table).find("th i.fa").attr('class','fa fa-sort');
        
        //changes icon depending if it is ascending or decending 
        if (dir === "asc"){
            $(table).find("th[value='"+n+"']").removeClass("sort_desc").addClass("sort_asc");
            $(table).find("th[value='"+n+"'] i.fa").attr('class','fa fa-sort-desc');
        }else if (dir === "desc") {
            $(table).find("th[value='"+n+"']").removeClass("sort_asc").addClass("sort_desc");
            $(table).find("th[value='"+n+"'] i.fa").attr('class','fa fa-sort-asc');
        }
        
        grid.gridDisabledMoveAction(table);
    },    
    gridActionAdd: function(object) {
        var grid = this;
        var table = $(object).prev('table');
        var model = $(table).find('.grid_model').html();
        var row = $('<tr>' + model + '</tr>');

        $(row).find("select").each(function() {
            $(this).addClass("initFullWidthChosen");
            if (UI.rtl) {
                $(this).addClass("chosen-rtl");
            }
            $(this).chosen({ width: "100%", placeholder_text: " " });
        });

        $(row).find("input.autocomplete").each(function() {
            var key = $(this).attr("name");
            $(this).autocomplete({
                source: grid.options_sources[grid.id + ":" + key],
                minLength: 0,
                open: function() {
                    $(this).autocomplete('widget').css('z-index', 99999);
                    return false;
                }
            });
        });

        $(table).append(row);
        $(row).find('a.property-type-grid-action-delete').click(function() {
            grid.gridActionDelete(this);
            return false;
        });
        $(row).find('a.property-type-grid-action-moveup').click(function() {
            grid.gridActionMoveUp(this);
            return false;
        });
        $(row).find('a.property-type-grid-action-movedown').click(function() {
            grid.gridActionMoveDown(this);
            return false;
        });

        grid.gridDisabledMoveAction(table);
    },
    gridActionDelete: function(object) {
        var grid = this;
        var currentRow = $(object).parent().parent();
        var table = $(currentRow).parent();
        $(currentRow).remove();
        grid.gridDisabledMoveAction(table);
    },
    gridActionMoveUp: function(object) {
        var grid = this;
        var currentRow = $(object).parent().parent();
        var prevRow = $(currentRow).prev();
        if (prevRow.attr("id") !== "model") {
            $(currentRow).after(prevRow);
            grid.gridDisabledMoveAction($(currentRow).parent());
        }
    },
    gridActionMoveDown: function(object) {
        var grid = this;
        var currentRow = $(object).parent().parent();
        var nextRow = $(currentRow).next();
        if (nextRow.length > 0) {
            $(nextRow).after(currentRow);
            grid.gridDisabledMoveAction($(currentRow).parent());
        }
    },
    gridDisabledMoveAction: function(table) {
        $(table).find('a.property-type-grid-action-moveup').removeClass("disabled");
        $(table).find('a.property-type-grid-action-moveup:eq(1)').addClass("disabled");

        $(table).find('a.property-type-grid-action-movedown').removeClass("disabled");
        $(table).find('a.property-type-grid-action-movedown:last').addClass("disabled");
    },
    updateSource: function(key, options) {
        var thisObj = this;
        var skey = thisObj.id + ":" + key;
        this.options_sources[skey] = [];
        if (options !== undefined) {
            $.each(options, function(i, option) {
                if (option['value'] !== "" && $.inArray(option['value'], thisObj.options_sources[skey]) === -1) {
                    thisObj.options_sources[skey].push(option['value']);
                }
            });
        }
        this.options_sources[skey].sort();

        var table = $("#" + this.id);

        var filter = null;
        $.each(thisObj.properties.columns, function(i, column) {
            if (column.key === key && column.options_ajax_row_regex_filter !== undefined && column.options_ajax_row_regex_filter !== "") {
                filter = column.options_ajax_row_regex_filter;
            }
        });

        $(table).find("input[name='" + key + "'].ui-autocomplete-input").each(function() {
            var source = thisObj.options_sources[skey];

            if (filter !== null) {
                var tempFilter = filter;
                $(this).closest("tr").find("[name]").each(function() {
                    var name = $(this).attr("name");
                    var val = $(this).val();
                    if (val === null) {
                        val = $(this).data("value");
                    }
                    tempFilter = tempFilter.replace("${" + name + "}", val);
                });
                var regex = new RegExp(tempFilter);

                var tempSource = [];
                for (var i in source) {
                    var option_value = source[i];
                    if (option_value !== "") {
                        var result = regex.exec(option_value);
                        if (result !== null && result.length > 0 && result[0] === option_value) {
                            tempSource.push(option_value);
                        }
                    } else {
                        tempSource.push(option_value);
                    }
                }
                source = tempSource;
            }

            $(this).autocomplete("option", "source", source);
        });
    },
    pageShown: function() {
        $("#" + this.id + " select.initFullWidthChosen").trigger("chosen:updated");
    }
};
PropertyEditor.Type.Grid = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Grid.prototype);

PropertyEditor.Type.GridCombine = function() {};
PropertyEditor.Type.GridCombine.prototype = {
    shortname: "gridcombine",
    options_sources: {},
    getData: function(useDefault) {
        var field = this;
        var data = new Object();

        if (this.isDataReady) {
            if (!field.isHidden()) {
                if ($('#' + this.id + ' tr').length > 2) {
                    $('#' + this.id + ' tr').each(function(tr) {
                        var row = $(this);
                        if (!$(row).hasClass("grid_model") && !$(row).hasClass("grid_header")) {
                            $.each(field.properties.columns, function(i, column) {
                                var value = data[column.key];

                                if (value === undefined) {
                                    value = "";
                                } else {
                                    value += ';';
                                }

                                var fieldValue = "";
                                if (column.type !== "truefalse") {
                                    fieldValue = $(row).find('input[name=' + column.key + '], select[name=' + column.key + ']').val();
                                    if (fieldValue === undefined || fieldValue === null) {
                                        fieldValue = "";
                                    }
                                } else {
                                    if ($(row).find('input[name=' + column.key + ']').is(":checked")) {
                                        fieldValue = (column.true_value !== undefined) ? column.true_value : 'true';
                                    } else {
                                        fieldValue = (column.false_value !== undefined) ? column.false_value : 'false';
                                    }
                                }

                                value += fieldValue.trim();
                                data[column.key] = value;
                            });
                        }
                    });
                } else if (useDefault !== undefined && useDefault) {
                    if (field.options.defaultPropertyValues !== null && field.options.defaultPropertyValues !== undefined) {
                        $.each(field.properties.columns, function(i, column) {
                            var temp = field.options.defaultPropertyValues[column.key];
                            if (temp !== undefined) {
                                data[column.key] = temp;
                            } else {
                                data[column.key] = "";
                            }
                        });
                    }
                } else {
                    $.each(field.properties.columns, function(i, column) {
                        data[column.key] = "";
                    });
                }
            }
        } else {
            if (field.options.propertyValues !== undefined && field.options.propertyValues !== null) {
                $.each(field.properties.columns, function(i, column) {
                    var temp = field.options.propertyValues[column.key];
                    data[column.key] = temp;
                });
            }
        }
        return data;
    },
    validate: function(data, errors, checkEncryption) {
        var wrapper = $('#' + this.id + '_input');

        var value = this.getData(false);
        var defaultValue = null;

        if (this.defaultValue !== undefined && this.defaultValue !== null && this.defaultValue !== "") {
            defaultValue = this.defaultValue;
        }

        var hasValue = true;
        if (Object.keys(value).length === 0) {
            hasValue = false;
        }

        if (this.properties.required !== undefined &&
            this.properties.required.toLowerCase() === "true" &&
            defaultValue === null && !hasValue) {
            var obj = new Object();
            obj.field = this.properties.name;
            obj.fieldName = this.properties.label;
            obj.message = this.options.mandatoryMessage;
            errors.push(obj);
            $(wrapper).append('<div class="property-input-error">' + obj.message + '</div>');
        }

        if (this.properties.js_validation !== undefined && this.properties.js_validation !== '') {
            var func = PropertyEditor.Util.getFunction(this.properties.js_validation);
            if ($.isFunction(func)) {
                var errorMsg = func(this.properties.name, value);

                if (errorMsg !== null && errorMsg !== "") {
                    var obj2 = new Object();
                    obj2.fieldName = this.properties.label;
                    obj2.message = errorMsg;
                    errors.push(obj2);
                    $(wrapper).append('<div class="property-input-error">' + obj2.message + '</div>');
                }
            }
        }

        this.addOnValidation(data, errors, checkEncryption);
    },
    addOnValidation: function(data, errors, checkEncryption) {
        var thisObj = this;
        var wrapper = $('#' + this.id + '_input');
        var table = $("#" + this.id);
        $(table).find("td").removeClass("error");

        var hasError = false;
        if (data !== undefined && data !== null && $('#' + this.id + ' tr').length > 2) {
            $.each(thisObj.properties.columns, function(j, column) {
                if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                    var temp = data[column.key];
                    if (temp !== undefined) {
                        var temp_arr = temp.split(";");

                        $.each(temp_arr, function(i, row) {
                            if (row === "") {
                                var td = $(table).find("tr:eq(" + (i + 2) + ") td:eq(" + j + ")");
                                $(td).addClass("error");
                                hasError = true;
                            }
                        });
                    }
                }
            });
        }

        if (hasError) {
            var obj = new Object();
            obj.field = this.properties.name;
            obj.fieldName = this.properties.label;
            obj.message = this.options.mandatoryMessage;
            errors.push(obj);
            $(wrapper).append('<div class="property-input-error">' + obj.message + '</div>');
        }
    },
    renderField: function() {
        var thisObj = this;
        var html = '<table id="' + this.id + '" class="grid"><tr class="grid_header">';
        html += '<th class="property-type-grid-row-header">'+thisObj.properties.label+'</th>';
        //render header
        $.each(this.properties.columns, function(i, column) {
            var required = "";
            if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
            }
            html += '<th value="' + i + '"><span>' + column.label + '</span>  <i class="fa fa-sort"></i>' + required + '</th>';
        });
        html += '<th class="property-type-grid-action-column"></th></tr>';

        //render model
        html += '<tr class="grid_model" style="display:none">';
        html += '<th class="property-type-grid-row-header">'+thisObj.properties.label+'</th>';
        $.each(this.properties.columns, function(i, column) {
            var required = "";
            if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
            }
            var tdclass = column.type;
            if (tdclass === undefined) {
                tdclass = "";
            }
            html += '<td class="'+tdclass+'"><span class="label"><span>'+column.label+'</span> '+required+'</span><span>';

            PropertyEditor.Util.retrieveOptionsFromCallback(thisObj, column, column.key);

            if (column.type === "truefalse") {
                column.true_value = (column.true_value !== undefined) ? column.true_value : 'true';
                html += '<label><input name="' + column.key + '" type="checkbox" value="' + column.true_value + '"/><span class="hidden_label">'+column.label+'</span></label>';
            } else if (column.options !== undefined || column.options_ajax !== undefined) {
                if (column.type === "autocomplete") {
                    thisObj.updateSource(column.key, column.options);
                    html += '<input name="' + column.key + '" class="autocomplete" size="10" value=""/>';
                } else {
                    html += '<select name="' + column.key + '" data-value="">';
                    if (column.options !== undefined) {
                        $.each(column.options, function(i, option) {
                            html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '">' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
                        });
                    }
                    html += '</select>';
                }
            } else if (column.type === "number") {
                html += '<input name="' + column.key + '" type="number" size="10" value=""/>';
            } else {
                html += '<input name="' + column.key + '" size="10" value=""/>';
            }
            html += '</span></td>';
        });
        html += '<td class="property-type-grid-action-column">';
        html += '<a href="#" class="property-type-grid-action-moveup"><i class="fas fa-chevron-circle-up"></i><span>' + get_peditor_msg('peditor.moveUp') + '</span></a>';
        html += ' <a href="#" class="property-type-grid-action-movedown"><i class="fas fa-chevron-circle-down"></i><span>' + get_peditor_msg('peditor.moveDown') + '</span></a>';
        html += ' <a href="#" class="property-type-grid-action-delete"><i class="fas fa-times-circle"></i><span>' + get_peditor_msg('peditor.delete') + '</span></a>';
        html += '</td></tr>';

        var values = new Array();
        if (thisObj.options.propertyValues !== undefined && thisObj.options.propertyValues !== null) {
            $.each(this.properties.columns, function(i, column) {
                var temp = thisObj.options.propertyValues[column.key];
                if (temp !== undefined) {
                    var temp_arr = temp.split(";");

                    $.each(temp_arr, function(i, row) {
                        if (values[i] === null || values[i] === undefined) {
                            values[i] = new Object();
                        }
                        values[i][column.key] = row;
                    });
                }
            });
        }

        //check for all empty columns if there is only one row
        if (values.length === 1) {
            var row = values[0];
            var empty = true;
            $.each(thisObj.properties.columns, function(i, column) {
                if (row[column.key] !== undefined && row[column.key] !== "") {
                    empty = false;
                }
            });
            if (empty) {
                values = [];
            }
        }

        //render value
        if (values.length > 0) {
            $.each(values, function(i, row) {
                html += '<tr>';
                html += '<th class="property-type-grid-row-header">'+thisObj.properties.label+'</th>';
                $.each(thisObj.properties.columns, function(i, column) {
                    var columnValue = "";
                    if (row[column.key] !== undefined) {
                        columnValue = row[column.key];
                    }
                    
                    var required = "";
                    if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                        required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
                    }
                    var tdclass = column.type;
                    if (tdclass === undefined) {
                        tdclass = "";
                    }
                    html += '<td class="'+tdclass+'"><span class="label"><span>'+column.label+'</span> '+required+'</span><span>';

                    if (column.type === "truefalse") {
                        var checked = "";
                        if ((columnValue === column.true_value)) {
                            checked = "checked";
                        }
                        html += '<label><input name="' + column.key + '" type="checkbox" ' + checked + ' value="' + column.true_value + '"/><span class="hidden_label">'+column.label+'</span></label>';
                    } else if (column.options !== undefined || column.options_ajax !== undefined) {
                        if (column.type === "autocomplete") {
                            html += '<input name="' + column.key + '" class="autocomplete" size="10" value="' + PropertyEditor.Util.escapeHtmlTag(columnValue) + '"/>';
                        } else {
                            html += '<select name="' + column.key + '" data-value="' + columnValue + '" class="initFullWidthChosen">';
                            if (column.options !== undefined) {
                                $.each(column.options, function(i, option) {
                                    var selected = "";
                                    if (columnValue === option.value) {
                                        selected = " selected";
                                    }
                                    html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
                                });
                            }
                            html += '</select>';
                        }
                    } else if (column.type === "number") {
                        html += '<input name="' + column.key + '" size="10" type="number" value="' + PropertyEditor.Util.escapeHtmlTag(columnValue) + '"/>';
                    } else {
                        html += '<input name="' + column.key + '" size="10" value="' + PropertyEditor.Util.escapeHtmlTag(columnValue) + '"/>';
                    }
                    html += '</span></td>';
                });

                html += '<td class="property-type-grid-action-column">';
                html += '<a href="#" class="property-type-grid-action-moveup"><i class="fas fa-chevron-circle-up"></i><span>' + get_peditor_msg('peditor.moveUp') + '</span></a>';
                html += ' <a href="#" class="property-type-grid-action-movedown"><i class="fas fa-chevron-circle-down"></i><span>' + get_peditor_msg('peditor.moveDown') + '</span></a>';
                html += ' <a href="#" class="property-type-grid-action-delete"><i class="fas fa-times-circle"></i><span>' + get_peditor_msg('peditor.delete') + '</span></a>';
                html += '</td></tr>';
            });
        }

        html += '</table><a href="#" class="property-type-grid-action-add"><i class="fas fa-plus-circle"></i><span>' + get_peditor_msg('peditor.add') + '</span></a>';
        
        html += this.renderSortControl();
        
        return html;
    },
    renderDefault: function() {
        var thisObj = this;
        var defaultValueText = '';

        var defaultValues = new Array();
        if (thisObj.options.defaultPropertyValues !== null && thisObj.options.defaultPropertyValues !== undefined) {
            $.each(thisObj.properties.columns, function(i, column) {
                var temp = thisObj.options.defaultPropertyValues[column.key];
                if (temp !== undefined && temp !== '') {
                    var temp_arr = temp.split(";");

                    $.each(temp_arr, function(i, row) {
                        if (defaultValues[i] === null || defaultValues[i] === undefined) {
                            defaultValues[i] = new Object();
                        }
                        defaultValues[i][column.key] = row;
                    });
                }
            });
        }

        if (defaultValues !== null) {
            $.each(defaultValues, function(i, row) {
                $.each(thisObj.properties.columns, function(i, column) {
                    var columnValue = "";
                    if (row[column.key] !== undefined) {
                        columnValue = row[column.key];
                    }

                    if (column.options !== undefined) {
                        $.each(column.options, function(i, option) {
                            if (columnValue === option.value) {
                                defaultValueText += PropertyEditor.Util.escapeHtmlTag(option.label) + '; ';
                            }
                        });
                    } else {
                        defaultValueText += columnValue + '; ';
                    }
                });
                defaultValueText += '<br/>';
            });
        }
        if (defaultValueText !== '') {
            defaultValueText = '<div class="default"><span class="label">' + get_peditor_msg('peditor.default') + '</span><span class="value">' + defaultValueText + '</span><div class="clear"></div></div>';
        }
        return defaultValueText;
    },
    getContainerClass: function() {
        return "property-grid";
    },
    initScripting: PropertyEditor.Type.Grid.prototype.initScripting,
    gridActionAdd: PropertyEditor.Type.Grid.prototype.gridActionAdd,
    gridActionDelete: PropertyEditor.Type.Grid.prototype.gridActionDelete,
    gridActionMoveUp: PropertyEditor.Type.Grid.prototype.gridActionMoveUp,
    gridActionMoveDown: PropertyEditor.Type.Grid.prototype.gridActionMoveDown,
    gridDisabledMoveAction: PropertyEditor.Type.Grid.prototype.gridDisabledMoveAction,
    pageShown: PropertyEditor.Type.Grid.prototype.pageShown,
    handleAjaxOptions: PropertyEditor.Type.Grid.prototype.handleAjaxOptions,
    updateSource: PropertyEditor.Type.Grid.prototype.updateSource,
    renderSortControl: PropertyEditor.Type.Grid.prototype.renderSortControl,
    sortTable: PropertyEditor.Type.Grid.prototype.sortTable
};
PropertyEditor.Type.GridCombine = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.GridCombine.prototype);

PropertyEditor.Type.GridFixedRow = function() {};
PropertyEditor.Type.GridFixedRow.prototype = {
    shortname: "gridfixedrow",
    options_sources: {},
    getData: PropertyEditor.Type.Grid.prototype.getData,
    addOnValidation: function(data, errors, checkEncryption) {
        var thisObj = this;
        var wrapper = $('#' + this.id + '_input');
        var table = $("#" + this.id);
        $(table).find("td").removeClass("error");

        var value = data[this.properties.name];

        var hasError = false;
        if (thisObj.properties.rows !== null) {
            $.each(thisObj.properties.rows, function(i, row) {
                if (row.required !== undefined && row.required.toLowerCase() === 'true') {
                    $.each(thisObj.properties.columns, function(j, column) {
                        if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                            if (value[i] === undefined || value[i] === null ||
                                value[i][column.key] === undefined || value[i][column.key] === null || value[i][column.key] === "") {
                                var td = $(table).find("tr:eq(" + (i + 1) + ") td:eq(" + j + ")");
                                $(td).addClass("error");
                                hasError = true;
                            }
                        }
                    });
                }
            });
        }

        if (hasError) {
            var obj = new Object();
            obj.field = this.properties.name;
            obj.fieldName = this.properties.label;
            obj.message = this.options.mandatoryMessage;
            errors.push(obj);
            $(wrapper).append('<div class="property-input-error">' + obj.message + '</div>');
        }
    },
    renderField: function() {
        var thisObj = this;
        var html = '<table id="' + this.id + '" class="grid"><tr class="grid_header">';
        html += '<th class="property-type-grid-row-header">'+thisObj.properties.label+'</th>';
        //render header
        $.each(this.properties.columns, function(i, column) {
            var required = "";
            if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
            }
            html += '<th><span>' + column.label + '</span>' + required + '</th>';
        });
        html += '<th class="property-type-grid-action-column"></th></tr>';

        //render value
        if (thisObj.properties.rows !== null) {
            $.each(thisObj.properties.rows, function(i, row) {
                html += '<tr>';
                html += '<th class="property-type-grid-row-header">'+thisObj.properties.label+'</th>';
                $.each(thisObj.properties.columns, function(j, column) {
                    if (j === 0) { //first column to display Row label
                        var required = "";
                        if (row.required !== undefined && row.required.toLowerCase() === 'true') {
                            required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
                        }

                        html += '<td><span>' + row.label + '</span>' + required;
                        html += '<input type="hidden" name="' + column.key + '" value="' + PropertyEditor.Util.escapeHtmlTag(row.label) + '"/></td>';
                    } else {
                        var columnValue = "";
                        if (thisObj.value !== undefined && thisObj.value !== null &&
                            thisObj.value[i] !== undefined && thisObj.value[i] !== null &&
                            thisObj.value[i][column.key] !== undefined) {
                            columnValue = thisObj.value[i][column.key];
                        }

                        PropertyEditor.Util.retrieveOptionsFromCallback(thisObj, column, column.key);

                        var required = "";
                        if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                            required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
                        }
                        var tdclass = column.type;
                        if (tdclass === undefined) {
                            tdclass = "";
                        }
                        html += '<td class="'+tdclass+'"><span class="label"><span>'+column.label+'</span> '+required+'</span><span>';

                        if (column.type === "truefalse") {
                            var checked = "";
                            column.true_value = (column.true_value !== undefined) ? column.true_value : 'true';
                            if (columnValue === column.true_value) {
                                checked = "checked";
                            }
                            html += '<label><input name="' + column.key + '" type="checkbox" ' + checked + ' value="' + column.true_value + '"/><span class="hidden_label">'+column.label+'</span></label>';
                        } else if (column.options !== undefined || column.options_ajax !== undefined) {
                            if (column.type === "autocomplete") {
                                if (i === 0) {
                                    thisObj.updateSource(column.key, column.options);
                                }
                                html += '<input name="' + column.key + '" class="autocomplete" size="10" value="' + PropertyEditor.Util.escapeHtmlTag(columnValue) + '"/>';
                            } else {
                                html += '<select name="' + column.key + '" data-value="' + columnValue + '" class="initFullWidthChosen">';
                                if (column.options !== undefined) {
                                    $.each(column.options, function(i, option) {
                                        var selected = "";
                                        if (columnValue === option.value) {
                                            selected = " selected";
                                        }
                                        html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
                                    });
                                }
                                html += '</select>';
                            }
                        } else if (column.type === "number") {
                            html += '<input name="' + column.key + '" type="number" size="10" value="' + PropertyEditor.Util.escapeHtmlTag(columnValue) + '"/>';
                        } else {
                            html += '<input name="' + column.key + '" size="10" value="' + PropertyEditor.Util.escapeHtmlTag(columnValue) + '"/>';
                        }
                        html += '</span></td>';
                    }
                });

                html += '</tr>';
            });
        }

        html += '</table>';
        return html;
    },
    renderDefault: PropertyEditor.Type.Grid.prototype.renderDefault,
    initScripting: function() {
        var table = $("#" + this.id);
        var grid = this;

        $(table).find("select.initFullWidthChosen").each(function() {
            if (UI.rtl) {
                $(this).addClass("chosen-rtl");
            }
            $(this).chosen({ width: "100%", placeholder_text: " " });
        });

        $(table).find("input.autocomplete").each(function() {
            var key = $(this).attr("name");
            $(this).autocomplete({
                source: grid.options_sources[grid.id + ":" + key],
                minLength: 0,
                open: function() {
                    $(this).autocomplete('widget').css('z-index', 99999);
                    return false;
                }
            });
        });
        
        $(table).off("click.collapsible", ".property-type-grid-row-header");
        $(table).on("click.collapsible", ".property-type-grid-row-header", function(){
            $(this).toggleClass("collapsed");
        });

        $.each(grid.properties.columns, function(i, column) {
            if ((column.options_ajax !== undefined && column.options_ajax !== null) || (column.options_callback_on_change !== undefined && column.options_callback_on_change !== null)) {
                PropertyEditor.Util.handleOptionsField(grid, column.key, column.options_ajax, column.options_ajax_on_change, column.options_ajax_mapping, column.options_ajax_method, column.options_extra);
            }
        });
    },
    getContainerClass: function() {
        return "property-grid";
    },
    pageShown: PropertyEditor.Type.Grid.prototype.pageShown,
    handleAjaxOptions: PropertyEditor.Type.Grid.prototype.handleAjaxOptions,
    updateSource: PropertyEditor.Type.Grid.prototype.updateSource
};
PropertyEditor.Type.GridFixedRow = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.GridFixedRow.prototype);

PropertyEditor.Type.Repeater = function() {};
PropertyEditor.Type.Repeater.prototype = {
    shortname: "repeater",
    addOnValidation: function(data, errors, checkEncryption) {
        var thisObj = this;
        var deferreds = [];
        
        var value = data[this.properties.name];
        if (value !== null && value !== undefined && value.length > 0) {
            //remove previous error message
            $("#" + thisObj.id + "_input .error").removeClass("error");
            $("#" + thisObj.id + "_input .property-input-error").remove();

            $("#" + thisObj.id + "_input > div > .repeater-rows-container > .repeater-row").each(function(i){
                var deffers = thisObj.validateRow($(this), value[i], errors, checkEncryption);
                if (deffers !== null && deffers !== undefined && deffers.length > 0) {
                    deferreds = $.merge(deferreds, deffers);
                }
            });
        }
        return deferreds;
    },
    validateRow: function(row, data, errors, checkEncryption) {
        var fields = $(row).data("fields");
        var deferreds = [];
        
        if (fields !== null && fields !== undefined) {
            $.each(fields, function(i, property) {
                var type = property.propertyEditorObject;
                if (!type.isHidden()) {
                    var deffers = type.validate(data, errors, checkEncryption);
                    if (deffers !== null && deffers !== undefined && deffers.length > 0) {
                        deferreds = $.merge(deferreds, deffers);
                    }
                }
            });
        }
        
        if ($(row).find(".property-input-error").length > 0) {
            $(row).addClass("error");
        }
        return deferreds;
    },
    getData: function(useDefault) {
        var field = this;
        var data = new Object();

        if (this.isDataReady) {
            var rows = [];
            if (!field.isHidden()) {
                $("#" + field.id + "_input > div > .repeater-rows-container > .repeater-row").each(function(){
                    rows.push(field.getRow($(this)));
                });
                
                data[this.properties.name] = rows;
            }
        } else {
            data[this.properties.name] = this.value;
        }
        return data;
    },
    getRow: function(row) {
        var fields = $(row).data("fields");
        
        var properties = new Object();
        if (fields !== undefined) {
            $.each(fields, function(i, property) {
                var type = property.propertyEditorObject;

                if (!type.isHidden()) {
                    var data = type.getData(false);

                    //handle Hash Field
                    if (data !== null && data['HASH_FIELD'] !== null && data['HASH_FIELD'] !== undefined) {
                        if (properties['PROPERTIES_EDITOR_METAS_HASH_FIELD'] === undefined) {
                            properties['PROPERTIES_EDITOR_METAS_HASH_FIELD'] = data['HASH_FIELD'];
                        } else {
                            properties['PROPERTIES_EDITOR_METAS_HASH_FIELD'] += ";" + data['HASH_FIELD'];
                        }
                        delete data['HASH_FIELD'];
                    }

                    if (data !== null) {
                        properties = $.extend(properties, data);
                    }
                }
            });
        }
        return properties;
    },
    renderField : function() {
        var thisObj = this;
        
        var html = '<div name="'+thisObj.id+'"><div class="repeater-rows-container"></div><div style="text-align:right; margin-bottom: 10px;"><a class="pebutton btn collapseAll"><i class="fas fa-compress"></i> '+get_peditor_msg('peditor.collapseAllRows')+'</a> <a class="pebutton btn expandAll"><i class="fas fa-expand"></i> '+get_peditor_msg('peditor.expandAllRows')+'</a> <a class="pebutton addrow"><i class="fas fa-plus-circle"></i> '+get_peditor_msg('peditor.addRow')+'</a></div></div>';
        
        return html;
    },
    initScripting : function() {
        var thisObj = this;
        
        thisObj.loadValues();
        
        $("#" + thisObj.id + "_input").off("click", "> div > div > .addrow, > div > .repeater-rows-container > .repeater-row > div > .addrow");
        $("#" + thisObj.id + "_input").on("click", "> div > div > .addrow, > div > .repeater-rows-container > .repeater-row > div > .addrow", function(){
            thisObj.addRow(this);
        });
        
        $("#" + thisObj.id + "_input").off("click", "> div > .repeater-rows-container > .repeater-row > div > .deleterow");
        $("#" + thisObj.id + "_input").on("click", "> div > .repeater-rows-container > .repeater-row > div > .deleterow", function(){
            thisObj.deleteRow(this);
        });
        
        $("#" + thisObj.id + "_input").off("click", "> div > .repeater-rows-container > .repeater-row > div > a.expand");
        $("#" + thisObj.id + "_input").on("click", "> div > .repeater-rows-container > .repeater-row > div > a.expand", function(){
            thisObj.expandRow(this);
        });
        
        $("#" + thisObj.id + "_input").off("click", "> div > .repeater-rows-container > .repeater-row > div > a.compress");
        $("#" + thisObj.id + "_input").on("click", "> div > .repeater-rows-container > .repeater-row > div > a.compress", function(){
            thisObj.compressRow(this);
        });
        
        $("#" + thisObj.id + "_input").off("click", "> div > div > a.expandAll");
        $("#" + thisObj.id + "_input").on("click", "> div > div > a.expandAll", function(){
            $("#" + thisObj.id + "_input > div > .repeater-rows-container > .repeater-row > div > a.expand").each(function() {
                thisObj.expandRow(this);
            });
        });
        
        $("#" + thisObj.id + "_input").off("click", "> div > div > a.collapseAll");
        $("#" + thisObj.id + "_input").on("click", "> div > div > a.collapseAll", function(){
            $("#" + thisObj.id + "_input > div > .repeater-rows-container > .repeater-row > div > a.compress").each(function() {
                thisObj.compressRow(this);
            });
        });
        
        $("#" + thisObj.id + "_input > div > .repeater-rows-container").sortable({
            opacity: 0.8,
            axis: 'y',
            handle: '.sort',
            tolerance: 'intersect'
        });
        thisObj.updateBtn();
    },
    loadValues : function() {
        var thisObj = this;
        
        if (thisObj.value !== undefined && thisObj.value !== null && thisObj.value.length > 0) {
            $.each(thisObj.value, function(i, v) {
                thisObj.addRow(null, v);
            });
        }
    },
    addRow : function(before, value) {
        var thisObj = this;
        
        var row = $('<div class="repeater-row compress"><div class="actions expand-compress"><a class="expand"><i class="fas fa-expand"></i></a></div><div class="actions sort"><i class="fas fa-arrows-alt"></i></div><div class="inputs"><div class="inputs-container"></div></div><div class="actions rowbuttons"><a class="addrow"><i class="fas fa-plus-circle"></i></a><a class="deleterow"><i class="fas fa-trash"></i></a></div></div>');
        
        var fields = $.extend(true, {}, thisObj.properties.fields);
        var fieldsHolder = {};
        
        var html = "";
        var cId = thisObj.properties.name + "-" + ((new Date()).getTime()) + (Math.floor(Math.random() * 10000));
        if (fields !== null && fields !== undefined) {
            $.each(fields, function(i, property) {
                html += thisObj.renderProperty(i, cId, property, value, fieldsHolder);
            });
        }
        $(row).find(".inputs .inputs-container").append(html);
        
        var defaultField = 2;
        if (thisObj.properties.defaultField !== undefined && !isNaN(thisObj.properties.defaultField)) {
            defaultField = thisObj.properties.defaultField;
        }
        var c = 0;
        $(row).find(".inputs .inputs-container .property-editor-property").each(function(i){
            if (c >= defaultField) {
                $(this).addClass("default-hidden");
            }
            c++;
        });
        
        $(row).data("fields", fields);
        
        if (before !== null && before !== undefined && !$(before).hasClass("pebutton")) {
            $(before).closest(".repeater-row").before(row);
        } else {
            $("#" + thisObj.id + "_input").find("> div > .repeater-rows-container").append(row);
        }
        
        if (fields !== null && fields !== undefined) {
            $.each(fields, function(i, property) {
                var type = property.propertyEditorObject;
                type.initScripting();
                type.initDefaultScripting();
            });
        }
        
        $(row).find(".property-label-description").each(function(){
            if (!$(this).hasClass("tooltipstered")) {
                $(this).tooltipster({
                    contentCloning: false,
                    side : 'right',
                    interactive : true
                });
            }
        });
        
        var triggerChangeFields = [];
        var page = {
            fields : fieldsHolder,
            editorObject : thisObj.editorObject,
            parentId : thisObj.parentId
        };
            
        $(row).find("[data-control_field][data-control_value]").each(function() {
            PropertyEditor.Util.bindDynamicOptionsEvent($(this), page, triggerChangeFields);
        });
        $(row).find("[data-required_control_field][data-required_control_value]").each(function() {
            PropertyEditor.Util.bindDynamicRequiredEvent($(this), page, triggerChangeFields);
        });
        
        for (var i in triggerChangeFields) {
            $(thisObj.editor).find("[name=\"" + triggerChangeFields[i] + "\"]").trigger("change");
        }
        
        thisObj.updateBtn();
    },
    renderProperty: function(i, prefix, property, values, fieldsHolder) {
        var type = property.propertyEditorObject;

        if (type === undefined) {
            var value = null;
            if (values !== null && values !== undefined && values[property.name] !== undefined) {
                value = values[property.name];
            } else if (property.value !== undefined && property.value !== null) {
                value = property.value;
            }

            type = PropertyEditor.Util.getTypeObject(this, i, prefix, property, value, null);
            type.repeaterFields = fieldsHolder;
            property.propertyEditorObject = type;

            fieldsHolder[property.name] = type;
        }

        if (type !== null) {
            return type.render();
        }
        return "";
    },
    expandRow : function(button) {
        var thisObj = this;
        $(button).addClass("compress");
        $(button).find("i").addClass("fa-compress");
        $(button).closest(".repeater-row").addClass("expand");
        $(button).removeClass("expand");
        $(button).find("i").removeClass("fa-expand");
        $(button).closest(".repeater-row").removeClass("compress");
        
        var row = $(button).closest(".repeater-row");
        var fields = $(row).data("fields");
        
        if (fields !== null && fields !== undefined) {
            $.each(fields, function(i, property) {
                var type = property.propertyEditorObject;
                type.pageShown();
            });
        }
        thisObj.updateBtn();
    },
    compressRow : function(button) {
        var thisObj = this;
        $(button).addClass("expand");
        $(button).find("i").addClass("fa-expand");
        $(button).closest(".repeater-row").addClass("compress");
        $(button).removeClass("compress");
        $(button).find("i").removeClass("fa-compress");
        $(button).closest(".repeater-row").removeClass("expand");
        thisObj.updateBtn();
    },
    deleteRow : function(button) {
        var thisObj = this;
        var row = $(button).closest(".repeater-row");
        
        var page = {
            fields : $(row).data("fields"),
            editorObject : thisObj.editorObject,
            parentId : thisObj.parentId
        };
        
        $(row).find("[data-control_field][data-control_value]").each(function() {
            PropertyEditor.Util.unbindDynamicOptionsEvent($(this), page);
        });
        $(row).find("[data-required_control_field][data-required_control_value]").each(function() {
            PropertyEditor.Util.unbindDynamicRequiredEvent($(this), page);
        });
        
        $(button).closest(".repeater-row").remove();
        thisObj.updateBtn();
    },
    updateBtn : function() {
        var thisObj = this;
        
        if ($("#" + thisObj.id + "_input > div > .repeater-rows-container").find("> .repeater-row.expand").length > 0) {
            $("#" + thisObj.id + "_input > div > div > a.collapseAll").show();
            $("#" + thisObj.id + "_input > div > div > a.expandAll").hide();
        } else {
            $("#" + thisObj.id + "_input > div > div > a.collapseAll").hide();
            $("#" + thisObj.id + "_input > div > div > a.expandAll").show();
        }
    }
};
PropertyEditor.Type.Repeater = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Repeater.prototype);

PropertyEditor.Type.HtmlEditor = function() {};
PropertyEditor.Type.HtmlEditor.prototype = {
    shortname: "htmleditor",
    getData: function(useDefault) {
        var data = new Object();
        
        if (this.isDataReady) {
            var value = "";
            if ($('[name=' + this.id + ']:not(.hidden)').length > 0) {
                value = tinymce.get($('[name=' + this.id + ']:not(.hidden)').attr('id')).getContent();
            }
            if (value === undefined || value === null || value === "") {
                if (useDefault !== undefined && useDefault &&
                    this.defaultValue !== undefined && this.defaultValue !== null) {
                    value = this.defaultValue;
                }
            }
            data[this.properties.name] = value;
        } else {
            data[this.properties.name] = this.value;
        }
        return data;
    },
    renderField: function() {
        this.isDataReady = false;
        var rows = ' rows="15"';
        if (this.properties.rows !== undefined && this.properties.rows !== null) {
            rows = ' rows="' + this.properties.rows + '"';
        }
        var cols = ' cols="60"';
        if (this.properties.cols !== undefined && this.properties.cols !== null) {
            cols = ' cols="' + this.properties.cols + '"';
        }

        if (this.value === null) {
            this.value = "";
        }
        return '<textarea id="' + this.id + '" name="' + this.id + '" class="tinymce"' + rows + cols + '>' + PropertyEditor.Util.escapeHtmlTag(this.value) + '</textarea>';
    },
    initScripting: function() {
        var thisObj = this;
        var height = 500;
        if (!(this.properties.height === undefined || this.properties.height === "")) {
            try {
                height = parseInt(this.properties.height);
            } catch (err) {}
        }
        
        var themeSkin = "oxide";
        var contentCss = "default";
        if ($('body').attr('builder-theme') === "dark") {
            themeSkin = "oxide-dark";
            contentCss = "dark";
        }

        tinymce.init({
            selector: '#' + this.id,
            height: height,
            plugins: 'advlist autolink lists link image charmap preview anchor pagebreak searchreplace wordcount visualblocks visualchars code fullscreen insertdatetime media nonbreaking table directionality emoticons codesample',
            toolbar1: 'undo redo | insert | styles fontsize | forecolor backcolor | bold italic underline | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image media table codesample emoticons | removeformat print preview | ltr rtl',
            menubar: 'edit insert view format table tools',
            image_advtab: true,
            relative_urls: false,
            convert_urls: false,
            extended_valid_elements:"style,link[href|rel]",
            custom_elements:"style,link,~link",
            valid_elements: '*[*]',
            skin: themeSkin,
            content_css: contentCss,
            promotion: false,
            setup: function(editor) {
                editor.off('focus.tinymce');
                editor.on('focus.tinymce', function(e) {
                    $(thisObj.editor).find(".property-description").hide();
                    var property = $("#" + e.target.id).parentsUntil(".property-editor-property-container", ".property-editor-property");
                    $(property).find(".property-description").show();
                });
                editor.off('SetContent');
                editor.on('SetContent', function(e){
                    thisObj.isDataReady = true;
                });
            }
        });
    }
};
PropertyEditor.Type.HtmlEditor = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.HtmlEditor.prototype);

PropertyEditor.Type.CodeEditor = function() {};
PropertyEditor.Type.CodeEditor.prototype = {
    codeeditor: null,
    shortname: "codeeditor",
    getData: function(useDefault) {
        var data = new Object();
        if (!this.isHidden()) {
            var value = this.codeeditor.getValue();
            if (value === undefined || value === null || value === "") {
                if (useDefault !== undefined && useDefault &&
                    this.defaultValue !== undefined && this.defaultValue !== null) {
                    value = this.defaultValue;
                }
            }
            data[this.properties.name] = value;
        }
        return data;
    },
    renderField: function() {
        return '<pre id="' + this.id + '" name="' + this.id + '" class="ace_editor"></pre>';
    },
    initScripting: function() {
        var thisObj = this;
        if (this.value === null) {
            this.value = "";
        }
        ace.config.set('loadWorkerFromBlob', false);
        this.codeeditor = ace.edit(this.id);
        this.codeeditor.setValue(this.value);
        this.codeeditor.getSession().setTabSize(4);
        if (this.properties.theme !== undefined || this.properties.theme !== "") {
            if ($('body').attr('builder-theme') === "dark") {
                this.properties.theme = "vibrant_ink";
            } else {
                this.properties.theme = "textmate";
            }
        }
        this.codeeditor.setTheme("ace/theme/" + this.properties.theme);
        if (this.properties.mode !== undefined && this.properties.mode !== "") {
            this.codeeditor.getSession().setMode("ace/mode/" + this.properties.mode);
        }
        if (this.properties.check_syntax !== undefined && this.properties.check_syntax.toLowerCase() === "false") {
            this.codeeditor.getSession().setUseWorker(false);
        }
        this.codeeditor.getSession().on('change', function() {
            $(thisObj.editor).find("#"+thisObj.id).trigger("change");
        });
        this.codeeditor.setAutoScrollEditorIntoView(true);
        this.codeeditor.setOption("maxLines", 1000000); //unlimited, to fix the height issue
        this.codeeditor.setOption("minLines", 10);
        this.codeeditor.resize();
        $(thisObj.editor).find("#"+thisObj.id).trigger("change");
    },
    pageShown: function() {
        this.codeeditor.resize();
        this.codeeditor.gotoLine(1);
    }
};
PropertyEditor.Type.CodeEditor = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.CodeEditor.prototype);

PropertyEditor.Type.ElementSelect = function() {};
PropertyEditor.Type.ElementSelect.prototype = {
    shortname: "elementselect",
    initialize: function() {
        this.pageOptions = {
            appPath : this.options.appPath,
            contextPath : this.options.contextPath,
            showDescriptionAsToolTip: this.options.showDescriptionAsToolTip,
            changeCheckIgnoreUndefined : this.options.changeCheckIgnoreUndefined,
            skipValidation : this.options.skipValidation,
            propertiesDefinition : null,
            defaultPropertyValues : null,
            propertyValues : null,
            mandatoryMessage : this.options.mandatoryMessage
        };
        
        if (this.value !== null) {
            this.pageOptions.propertyValues = this.value.properties;
        }
    },
    getData: function(useDefault) {
        var thisObj = this;
        var data = new Object();
        var anchor = $(this.editor).find(".anchor[anchorField=\"" + this.id + "\"]");

        if (this.isDataReady && !$(anchor).hasClass("partialLoad") && !$(anchor).hasClass("initial")) {
            var element = new Object();
            element['className'] = $('[name=' + this.id + ']:not(.hidden)').val();
            element['properties'] = new Object();

            if (this.pageOptions.propertiesDefinition !== undefined && this.pageOptions.propertiesDefinition !== null) {
                $.each(this.pageOptions.propertiesDefinition, function(i, page) {
                    var p = page.propertyEditorObject;
                    if (p !== undefined) {
                        element['properties'] = $.extend(element['properties'], p.getData());
                    }
                });
            }

            if (element['className'] === "" && useDefault !== undefined && useDefault &&
                this.defaultValue !== undefined && this.defaultValue !== null) {
                element = this.defaultValue;
            }

            data[this.properties.name] = element;
        } else {
            data[this.properties.name] = this.value;
        }
        return data;
    },
    validate: function(data, errors, checkEncryption) {
        var wrapper = $('#' + this.id + '_input');

        var value = data[this.properties.name];
        var deferreds = [];
        var defaultValue = null;
        
        if ($(this.editor).find(".anchor[anchorField=\"" + this.id + "\"]").hasClass("partialLoad")) {
            return deferreds;
        }
        
        if (this.defaultValue !== undefined && this.defaultValue !== null && this.defaultValue.className !== "") {
            defaultValue = this.defaultValue;
        }

        if (this.properties.required !== undefined && this.properties.required.toLowerCase() === "true" &&
            value.className === '' && defaultValue === null) {
            var obj = new Object();
            obj.field = this.properties.name;
            obj.fieldName = this.properties.label;
            obj.message = this.options.mandatoryMessage;
            errors.push(obj);
            $(wrapper).append('<div class="property-input-error">' + obj.message + '</div>');
        }

        if (this.pageOptions.propertiesDefinition !== undefined && this.pageOptions.propertiesDefinition !== null) {
            $.each(this.pageOptions.propertiesDefinition, function(i, page) {
                var p = page.propertyEditorObject;
                var deffers = p.validate(value['properties'], errors, true);
                if (deffers !== null && deffers !== undefined && deffers.length > 0) {
                    deferreds = $.merge(deferreds, deffers);
                }
            });
        }
        return deferreds;
    },
    renderField: function() {
        var html = '<select id="' + this.id + '" name="' + this.id + '" class="initChosen">';
        var valueString = "";
        if (this.value !== null && ((typeof this.value) === "string")) {
            var temp = this.value;
            this.value = {};
            this.value.className = temp;
        }
        if (this.value !== null) {
            valueString = this.value.className;
        }

        PropertyEditor.Util.retrieveOptionsFromCallback(this, this.properties);

        if (this.properties.options !== undefined && this.properties.options !== null) {
            $.each(this.properties.options, function(i, option) {
                var selected = "";
                if (valueString === option.value) {
                    selected = " selected";
                }
                html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
            });
        }
        html += '</select>';
        return html;
    },
    renderDefault: function() {
        var defaultValueText = '';
        var defaultValueString = '';
        if (this.defaultValue !== null && this.defaultValue !== undefined) {
            defaultValueString = this.defaultValue.classname;

            if (this.properties.options !== undefined && this.properties.options !== null) {
                $.each(this.properties.options, function(i, option) {
                    if (defaultValueString !== "" && defaultValueString === option.value) {
                        defaultValueText = PropertyEditor.Util.escapeHtmlTag(option.label);
                    }
                });
            }
        }
        if (defaultValueText !== '') {
            defaultValueText = '<div class="default"><span class="label">' + get_peditor_msg('peditor.default') + '</span><span class="value">' + defaultValueText + '</span><div class="clear"></div></div>';
        }
        return defaultValueText;
    },
    pageShown: PropertyEditor.Type.SelectBox.prototype.pageShown,
    handleAjaxOptions: function(options, reference) {
        var thisObj = this;
        if (options !== null && options !== undefined) {
            this.properties.options = options;
            var value = "";
            var html = "";

            value = $("#" + this.id).val();
            if ((value === "" || value === null) && thisObj.value !== undefined && thisObj.value !== null) {
                value = thisObj.value.className;
            }

            $.each(this.properties.options, function(i, option) {
                var selected = "";
                var cssClass = "";
                if (value === option.value) {
                    selected = " selected";
                }
                if (option.developer_mode !== undefined && option.developer_mode !== "") {
                    var temp = option.developer_mode.split(";");
                    for (var j in temp) {
                        cssClass += " "+temp[j]+"-mode-only";
                    }
                    cssClass = 'class="'+cssClass+'"';
                }
                html += '<option '+cssClass+' value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
            });
            $("#" + this.id).html(html);
            $("#" + this.id).trigger("change");
            $("#" + this.id).trigger("chosen:updated");
        }
    },
    initScripting: function() {
        var thisObj = this;
        var field = $("#" + this.id);
        if ($(this.editor).hasClass("editor-panel-mode") || thisObj.options.editorPanelMode) {
            var initial = "";
            if (thisObj.value !== undefined && thisObj.value !== null && thisObj.value.className !== undefined && thisObj.value.className !== "" && $(field).closest(".element-pages, .property-type-repeater").length !== 0) {
                initial = "initial";
            }
            field.closest(".property-editor-property").append("<div class=\"element-pages\" style=\"display:none;\"><div class=\"anchor property-editor-page "+initial+"\" data-page=\""+this.page.id+"\" anchorField=\""+this.id+"\" style=\"display:none\"></div></div>");
        } else {
            var currentPage = $(this.editor).find("#" + this.page.id);
            while ($(currentPage).next().data("page") === this.page.id) {
                currentPage = $(currentPage).next();
            }
            $(currentPage).after("<div class=\"anchor property-editor-page\" data-page=\""+this.page.id+"\" anchorField=\""+this.id+"\" style=\"display:none\"></div>");
        }
        if (UI.rtl) {
            $(field).addClass("chosen-rtl");
        }
        $(field).chosen({ width: "54%", placeholder_text: " " });

        if (!$(field).hasClass("hidden") && $(field).val() !== undefined &&
            $(field).val() !== null && this.properties.options !== undefined &&
            this.properties.options !== null && this.properties.options.length > 0) {
            this.renderPages();
        }

        $(field).change(function() {
            thisObj.renderPages();
        });
    },
    getContainerClass: function() {
        if (this.properties.url.indexOf('/getPropertyOptions')  !== -1) {
            return "property-plugin-selection";
        }
        return "";
    },
    renderPages: function() {
        var thisObj = this;
        var field = $("#" + this.id);
        var value = $(field).filter(":not(.hidden)").val();
        var currentPage = $(this.editor).find("#" + this.page.id);
        var anchor = $(this.editor).find(".anchor[anchorField=\"" + this.id + "\"]");
        
        if (value !== "") {
            $(field).closest(".property-type-elementselect").addClass("has_value");
        } else {
            $(field).closest(".property-type-elementselect").removeClass("has_value");
        }

        var data = null;
        if (this.properties.keep_value_on_change !== undefined && this.properties.keep_value_on_change.toLowerCase() === "true") {
            if (this.pageOptions.propertiesDefinition !== undefined && this.pageOptions.propertiesDefinition !== null) {
                data = this.getData();
                this.pageOptions.propertyValues = data[this.properties.name].properties;
            } else {
                this.pageOptions.propertyValues = (this.value) ? this.value.properties : null;
            }
        } else {
            this.pageOptions.propertyValues = (this.value && this.value.className === value) ? this.value.properties : null;
        }
        //check if value is different, remove all the related properties page
        var existing = $(this.editor).find('.property-editor-page[elementid=' + this.id + ']:first');
        if ($(existing).length > 0 && $(existing).attr('elementvalue') !== value) {
            this.removePages();
            thisObj.editorObject.refresh();
        }
        
        //use the default api to retrieve plugin properties if url is empty
        var defaultUrl = "[CONTEXT_PATH]/web/property/json"+thisObj.options.appPath+"/getPropertyOptions";
        if (thisObj.properties.url === "" || thisObj.properties.url === undefined) {
            thisObj.properties.url = defaultUrl;
        }
        var isDefault = true;
        if (thisObj.properties.url !== defaultUrl) {
            isDefault = false; // check this property type is used for plugin or not
        }
        
        //check the value is empty or not
        var isNotEmpty = (value !== "" && value !== undefined && value !== null);

        //if properties page not found, render it now. If this is used for plugin, don't render when value is empty
        if ($(this.editor).find('.property-editor-page[elementid=' + this.id + ']').length === 0 && ((isDefault && isNotEmpty) || !isDefault) && !$(anchor).hasClass("loading")) {
            $(anchor).addClass("loading");
            var deferreds = [];

            PropertyEditor.Util.prevAjaxCalls = {};
            PropertyEditor.Util.showAjaxLoading(thisObj.editor, thisObj, "CONTAINER");

            deferreds.push(this.getElementProperties(value));
            deferreds.push(this.getElementDefaultProperties(value));
            PropertyEditor.Util.deferredHandler(deferreds, function() {
                if (thisObj.pageOptions.propertiesDefinition !== undefined && thisObj.pageOptions.propertiesDefinition !== null) {
                    var parentId = thisObj.prefix + "_" + thisObj.properties.name;
                    var elementdata = ' elementid="' + thisObj.id + '" elementvalue="' + value + '"';

                    //check if the element has a parent element
                    if (currentPage.attr("elementid") !== undefined && currentPage.attr("elementid") !== "") {
                        parentId = currentPage.attr("elementid") + "_" + parentId;
                        if (currentPage.attr("parentelementid") !== undefined && currentPage.attr("parentelementid") !== "") {
                            elementdata += ' parentelementid="' + currentPage.attr("parentelementid") + '"';
                        } else {
                            elementdata += ' parentelementid="' + currentPage.attr("elementid") + '"';
                        }
                    }
                    
                    if ($(anchor).hasClass("initial") && thisObj.pageOptions.propertiesDefinition.length > 0) {
                        $(anchor).removeClass("initial").addClass("partialLoad");
                        var cloneDefinitionFirstPage = $.extend({}, thisObj.pageOptions.propertiesDefinition[0]);
                        cloneDefinitionFirstPage.properties = [{
                            name : "hidden",
                            type : "hidden"
                        }];
                        var html = "";
                        var p = new PropertyEditor.Model.Page(thisObj.editorObject, 0, cloneDefinitionFirstPage, elementdata, parentId);
                        p.options = thisObj.pageOptions;
                        html += p.render();
                        $(anchor).after(html);
                        
                        if ($(anchor).parent(".element-pages").length > 0 && $(anchor).parent(".element-pages").find(".property-page-show").length > 0) {
                            $(anchor).parent(".element-pages").show();
                        }

                        var temp = $(anchor).next();
                        $(temp).addClass("collapsed");
                        
                        $(temp).on("click", function() {
                            thisObj.renderPropertiesPages(anchor, elementdata, parentId, value);
                            $(temp).off("click");
                            $(temp).remove();
                            
                            $(anchor).next().removeClass("collapsed");
                        });
                        
                        //trigger a change event for fields depended on this element select field
                        $(temp).find('[name]').trigger("change");
                    } else {
                        thisObj.renderPropertiesPages(anchor, elementdata, parentId, value);
                    }
                }
                thisObj.editorObject.refresh();
                PropertyEditor.Util.removeAjaxLoading(thisObj.editor, thisObj, "CONTAINER");
                $(anchor).removeClass("loading");
            });
        }
    },
    renderPropertiesPages : function(anchor, elementdata, parentId, value) {
        var thisObj = this;
        
        //handle keep_value_on_change equal to true with new added property default value
        if (thisObj.pageOptions.propertyValues !== null && (typeof thisObj.pageOptions.propertyValues) !== "undefined" && !((typeof thisObj.properties.keep_value_on_change) === "undefined") && thisObj.properties.keep_value_on_change.toLowerCase() === "true") {
            $.each(thisObj.pageOptions.propertiesDefinition, function(i, page) {
                if (page.properties !== undefined) {
                    $.each(page.properties, function(i, property) {
                        //if there is default value and the property name is not exist in existing values
                        if (property.value !== undefined && thisObj.pageOptions.propertyValues[property.name] === undefined) {
                            thisObj.pageOptions.propertyValues[property.name] = property.value;
                        }
                    });
                }
            });
        }

        var html = "";
        $.each(thisObj.pageOptions.propertiesDefinition, function(i, page) {
            var p = page.propertyEditorObject;
            if (p === undefined) {
                p = new PropertyEditor.Model.Page(thisObj.editorObject, i, page, elementdata, parentId);
                p.options = thisObj.pageOptions;
                page.propertyEditorObject = p;
                thisObj.editorObject.pages[p.id] = p;
            }
            html += p.render();
        });
        $(anchor).after(html);

        $.each(thisObj.pageOptions.propertiesDefinition, function(i, page) {
            var p = page.propertyEditorObject;
            p.initScripting();
        });

        //add parent properties to plugin header
        var valueLabel = $("#" + thisObj.id).find('option[value="' + value + '"]').text();
        var parentTitle = '<h1>' + thisObj.properties.label + " (" + valueLabel + ')</h1>';
        var childFirstPage = $(thisObj.editor).find('.property-editor-page[elementid=' + thisObj.id + '].property-page-show:eq(0)');
        $(childFirstPage).find('.property-editor-page-title').prepend(parentTitle);

        if ($(anchor).parent(".element-pages").length > 0 && $(anchor).parent(".element-pages").find(".property-page-show").length > 0) {
            $(anchor).parent(".element-pages").show();
        }

        $(anchor).removeClass("partialLoad");
        
        thisObj.editorObject.refresh();
        PropertyEditor.Util.removeAjaxLoading(thisObj.editor, thisObj, "CONTAINER");
        $(anchor).removeClass("loading");
    },
    getElementProperties: function(value) {
        var thisObj = this;
        var d = $.Deferred();
        
        var ajaxUrl = PropertyEditor.Util.replaceContextPath(this.properties.url, this.options.contextPath) + "?" + "value=" + encodeURIComponent(value);
        if (PropertyEditor.Util.cachedAjaxCalls[ajaxUrl] !== undefined) {
            thisObj.pageOptions.propertiesDefinition = $.extend(true, [], PropertyEditor.Util.cachedAjaxCalls[ajaxUrl]);
            d.resolve();
            return d;
        }

        $.ajax({
            url: PropertyEditor.Util.replaceContextPath(this.properties.url, this.options.contextPath),
            data: "value=" + encodeURIComponent(value),
            dataType: "text",
            headers: {
                call_reference : thisObj.properties.options_ajax
            },
            success: function(response) {
                if (response !== null && response !== undefined && response !== "") {
                    try {
                        var data = eval(response);
                        if (ajaxUrl.indexOf("/getPropertyOptions?") !== -1) {
                            PropertyEditor.Util.cachedAjaxCalls[ajaxUrl] = $.extend(true, [], data);
                        }
                        
                        thisObj.pageOptions.propertiesDefinition = data;
                    } catch (err) {
                        if (console && console.log) {
                            console.log("error retrieving properties options of " + value + " : " + err);
                        }
                        thisObj.pageOptions.propertiesDefinition = null;
                    }
                } else {
                    thisObj.pageOptions.propertiesDefinition = null;
                }
                d.resolve();
            }
        });

        return d;
    },
    getElementDefaultProperties: function(value) {
        var thisObj = this;
        var d = $.Deferred();

        if (this.properties.default_property_values_url !== null && this.properties.default_property_values_url !== undefined &&
            this.properties.default_property_values_url !== "") {
        
            var ajaxUrl = PropertyEditor.Util.replaceContextPath(this.properties.default_property_values_url, this.options.contextPath) + "?" + "value=" + encodeURIComponent(value);
            if (PropertyEditor.Util.cachedAjaxCalls[ajaxUrl] !== undefined) {
                thisObj.pageOptions.defaultPropertyValues = $.extend(true, {}, PropertyEditor.Util.cachedAjaxCalls[ajaxUrl]);
                d.resolve();
                return d;
            }
            
            $.ajax({
                url: PropertyEditor.Util.replaceContextPath(this.properties.default_property_values_url, this.options.contextPath),
                data: "value=" + encodeURIComponent(value),
                dataType: "text",
                success: function(response) {
                    if (response !== null && response !== undefined && response !== "") {
                        var data = $.parseJSON(response);
                        if (ajaxUrl.indexOf("/getDefaultProperties?") !== -1) {
                            PropertyEditor.Util.cachedAjaxCalls[ajaxUrl] = $.extend(true, {}, data);
                        }
                        
                        thisObj.pageOptions.defaultPropertyValues = data;
                    } else {
                        thisObj.pageOptions.defaultPropertyValues = null;
                    }
                    d.resolve();
                }
            });
        } else {
            d.resolve();
        }

        return d;
    },
    removePages: function() {
        var anchor = $(this.editor).find(".anchor[anchorField=\"" + this.id + "\"]");
        if ($(anchor).hasClass("partialLoad")) {
            $(anchor).next().remove();
            $(anchor).removeClass("partialLoad");
        } else {
            if (this.pageOptions.propertiesDefinition !== undefined && this.pageOptions.propertiesDefinition !== null) {
                $.each(this.pageOptions.propertiesDefinition, function(i, page) {
                    var p = page.propertyEditorObject;
                    if (p !== undefined) {
                        p.remove();
                    }
                });
            }
        }
        this.pageOptions.propertiesDefinition = null;
        
        var field = $("#" + this.id);
        field.closest(".property-editor-property").find(".element-pages").hide();
    },
    remove: function() {
        this.removePages();
    }
};
PropertyEditor.Type.ElementSelect = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.ElementSelect.prototype);

PropertyEditor.Type.ElementMultiSelect = function() {};
PropertyEditor.Type.ElementMultiSelect.prototype = {
    shortname: "elementmultiselect",
    addOnValidation: function(data, errors, checkEncryption) {
        var thisObj = this;
        var deferreds = [];
        
        var value = data[this.properties.name];
        if (value !== null && value !== undefined && value.length > 0) {
            //remove previous error message
            $("#" + thisObj.id + "_input .error").removeClass("error");
            $("#" + thisObj.id + "_input .property-input-error").remove();

            $("#" + thisObj.id + "_input  > div > .repeater-rows-container > .repeater-row").each(function(i){
                var deffers = thisObj.validateRow($(this), value[i], errors, checkEncryption);
                if (deffers !== null && deffers !== undefined && deffers.length > 0) {
                    deferreds = $.merge(deferreds, deffers);
                }
            });
        }
        
        return deferreds;
    },
    validateRow: function(row, data, errors, checkEncryption) {
        var deferreds = [];
        
        var field = $(row).find("select");
        var id = $(field).attr("id");
        var anchor = $(this.editor).find(".anchor[anchorField=\"" + id + "\"]");
        
        if ($(anchor).hasClass("partialLoad")) {
            return deferreds;
        }
        
        var propertiesDefinition = $(row).data("propertiesDefinition");

        if (propertiesDefinition !== undefined && propertiesDefinition !== null) {
            $.each(propertiesDefinition, function(i, page) {
                var p = page.propertyEditorObject;
                var deffers = p.validate(data['properties'], errors, true);
                if (deffers !== null && deffers !== undefined && deffers.length > 0) {
                    deferreds = $.merge(deferreds, deffers);
                }
            });
        }
        return deferreds;
    },
    getData: function(useDefault) {
        var thisObj = this;
        var data = new Object();

        if (this.isDataReady) {
            var arr = [];
            $("#" + this.id + "_input  > div > .repeater-rows-container > .repeater-row").each(function(){
                var temp = thisObj.getRow($(this), useDefault);
                if (temp !== null) {
                    arr.push(thisObj.getRow($(this), useDefault));
                }
            });
            data[this.properties.name] = arr;
        } else {
            data[this.properties.name] = this.value;
        }
        return data;
    },
    getRow: function(row, useDefault) {
        var thisObj = this;
        var field = $(row).find("select");
        var id = $(field).attr("id");
        var anchor = $(this.editor).find(".anchor[anchorField=\"" + id + "\"]");
        
        var propertiesDefinition = $(row).data("propertiesDefinition");

        if (this.isDataReady && !$(anchor).hasClass("partialLoad") && !$(anchor).hasClass("initial")) {
            var element = new Object();
            element['className'] = $('[name=' + id + ']:not(.hidden)').val();
            element['properties'] = new Object();

            if (propertiesDefinition !== undefined && this.options.propertiesDefinition !== null) {
                $.each(propertiesDefinition, function(i, page) {
                    var p = page.propertyEditorObject;
                    element['properties'] = $.extend(element['properties'], p.getData());
                });
            }

            return element;
        } else {
            return $(row).data("element");
        }
        return null;
    },
    renderField : function() {
        var thisObj = this;
        
        PropertyEditor.Util.retrieveOptionsFromCallback(this, this.properties);
        
        var html = '<div name="'+thisObj.id+'"><div class="repeater-rows-container elementmultiselect"></div><div class="pebutton_container"><a class="pebutton addrow"><i class="fas fa-plus-circle"></i> '+get_peditor_msg('peditor.addRow')+'</a></div></div>';
        
        return html;
    },
    initScripting : function() {
        var thisObj = this;
        thisObj.properties.propertiesDefinition = {};
        thisObj.properties.defaultPropertyValues = {};
        if (!($(thisObj.editor).hasClass("editor-panel-mode") || thisObj.options.editorPanelMode)) {
            var currentPage = $(thisObj.editor).find("#" + thisObj.page.id);
            while ($(currentPage).next().data("page") === thisObj.page.id) {
                currentPage = $(currentPage).next();
            }
            $(currentPage).after("<div class=\"anchor property-editor-page\" data-page=\""+thisObj.page.id+"\" anchorField=\""+thisObj.id+"\" style=\"display:none\"></div>");
        }
        
        thisObj.loadValues(true);
        
        $("#" + thisObj.id + "_input").off("click", "> div > div > .addrow, > div > .repeater-rows-container > .repeater-row > .actions > .addrow");
        $("#" + thisObj.id + "_input").on("click", "> div > div > .addrow, > div > .repeater-rows-container > .repeater-row > .actions > .addrow", function(){
            thisObj.addRow(this);
        });
        
        $("#" + thisObj.id + "_input").off("click", "> div > .repeater-rows-container > .repeater-row > .actions > .deleterow");
        $("#" + thisObj.id + "_input").on("click", "> div > .repeater-rows-container > .repeater-row > .actions > .deleterow", function(){
            thisObj.deleteRow(this);
        });
        
        $("#" + thisObj.id + "_input  > div > .repeater-rows-container").sortable({
            opacity: 0.8,
            axis: 'y',
            handle: '.sort',
            tolerance: 'intersect',
            stop: function (event, ui) {
                var row = $(ui.item[0]);
                thisObj.movedRow(row);
            }
        });
    },
    loadValues : function(init) {
        var thisObj = this;
        
        if (!((typeof thisObj.value) === "undefined") && thisObj.value !== null && thisObj.value.length > 0) {
            var collapse = thisObj.value.length > 1;
            $.each(thisObj.value, function(i, v) {
                thisObj.addRow(null, v, init, collapse);
            });
        } else {
            thisObj.addRow(null);
        }
        if (this.properties.options !== null && !((typeof this.properties.options) === "undefined")) {
            thisObj.handleAjaxOptions(this.properties.options);
        }
    },
    addRow : function(before, value, init, collapse) {
        var thisObj = this;
        
        var row = $('<div class="repeater-row property-editor-property" style="margin-bottom:0px;"><div class="actions expand-compress property-label-container"><div class="property-label" style="display:none"></div><div class="num"></div></div><div class="actions sort"><i class="fas fa-arrows-alt"></i></div><div class="inputs"><div class="inputs-container"></div></div><div class="actions rowbuttons"><a class="addrow"><i class="fas fa-plus-circle"></i></a><a class="deleterow"><i class="fas fa-trash"></i></a></div></div>');
        
        var cId = thisObj.id + "-" + ((new Date()).getTime()) + (Math.floor(Math.random() * 10000));
        
        var valueString = "";
        if (value !== null && ((typeof value) === "string")) {
            var temp = value;
            value = {};
            value.className = temp;
        }
        if (!((typeof value) === "undefined") && value !== null) {
            valueString = value.className;
        }
        var html = '<select id="' + cId + '" name="' + cId + '" data-value="'+PropertyEditor.Util.escapeHtmlTag(valueString)+'" class="initChosen">';
        
        if (!((typeof thisObj.properties.options) === "undefined") && thisObj.properties.options !== null) {
            $.each(thisObj.properties.options, function(i, option) {
                var selected = "";
                if (valueString === option.value) {
                    selected = " selected";
                }
                html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
            });
        }
        html += '</select>';
        
        $(row).find(".inputs .inputs-container").append(html);
        
        if ($(this.editor).hasClass("editor-panel-mode") || thisObj.options.editorPanelMode) {
            var initial = "";
            if (init === true && $("#" + this.id).closest(".element-pages, .property-type-repeater").length !== 0 && valueString !== "") {
                initial = "initial";
            }
            $(row).find(".inputs").append("<div class=\"element-pages\" style=\"display:none;\"><div class=\"anchor property-editor-page "+initial+"\" data-page=\""+thisObj.page.id+"\" anchorField=\""+cId+"\" style=\"display:none\"></div></div>");
        }
        
        $(row).data("element", value);
        
        if (before !== null && !((typeof before) === "undefined") && !$(before).hasClass("pebutton")) {
            $(before).closest(".repeater-row").before(row);
            
            var bid = $(before).find("select").attr("id");
            var beforeAnchor = $(thisObj.editor).find(".anchor[anchorField=\""+bid+"\"]");
            $(beforeAnchor).before("<div class=\"anchor property-editor-page\" data-page=\""+thisObj.page.id+"\" anchorField=\""+cId+"\" style=\"display:none\"></div>");
        } else {
            $("#" + thisObj.id + "_input").find(" > div > .repeater-rows-container").append(row);
            
            var beforeAnchor = $(thisObj.editor).find(".anchor[anchorField=\""+thisObj.id+"\"]");
            $(beforeAnchor).before("<div class=\"anchor property-editor-page\" data-page=\""+thisObj.page.id+"\" anchorField=\""+cId+"\" style=\"display:none\"></div>");
        }
        
        var field = $(row).find("#"+cId);
        
        if (UI.rtl) {
            $(field).addClass("chosen-rtl");
        }
        $(field).chosen({ width: "54%", placeholder_text: " " });

        if (!$(field).hasClass("hidden") && !((typeof $(field).val()) === "undefined") && $(field).val() !== null) {
            thisObj.renderPages($(field), collapse);
        }

        $(row).data("collapse", collapse);
        $(field).change(function() {
            thisObj.renderPages($(field), $(row).data("collapse"));
            $(row).data("collapse", false);
        });
        this.updateRows();
    },
    deleteRow : function(button) {
        var thisObj = this;
        var row = $(button).closest(".repeater-row");
        var id = $(row).find("select").attr("id");
        $(this.editor).find(".anchor[anchorField=\"" + id + "\"]").remove();
        thisObj.removePages($(row).find("select"));
        $(row).remove();
        this.updateRows();
    },
    movedRow : function(row) {
        var thisObj = this;
        var fieldId = $(row).find("select").attr("id");
        var nextRowId = thisObj.id;
        if ($(row).next(".repeater-row").length > 0) {
            nextRowId = $(row).next(".repeater-row").find("select").attr("id");
        }
        if (!($(thisObj.editor).hasClass("editor-panel-mode") || thisObj.options.editorPanelMode)) {
            var movePages = $(".anchor[anchorfield=\""+fieldId+"\"], .property-editor-page[elementid=\""+fieldId+"\"]");
            $(".anchor[anchorfield=\""+nextRowId+"\"]").before(movePages);
        }
        
        this.updateRows();
    },
    pageShown: function() {
        $("#" + this.id + " select").trigger("chosen:updated");
    },
    handleAjaxOptions: function(options, reference) {
        var thisObj = this;
        if (options !== null && !((typeof options) === "undefined")) {
            this.properties.options = options;
            
            $("#" + thisObj.id + "_input select").each(function(){
                var value = "";
                var html = "";
                var objValue = $(this).data("value");
                
                value = $(this).val();
                if ((value === "" || value === null) && !((typeof objValue) === "undefined") && objValue !== null) {
                    value = objValue;
                }
                
                $.each(thisObj.properties.options, function(i, option) {
                    var selected = "";
                    var cssClass = "";
                    if (value === option.value) {
                        selected = " selected";
                    }
                    if (option.developer_mode !== undefined && option.developer_mode !== "") {
                        var temp = option.developer_mode.split(";");
                        for (var j in temp) {
                            cssClass += " "+temp[j]+"-mode-only";
                        }
                        cssClass = 'class="'+cssClass+'"';
                    }
                    html += '<option '+cssClass+' value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
                });
                $(this).html(html);
                $(this).trigger("change");
                $(this).trigger("chosen:updated");
            });
        }
    },
    renderPages: function(field, collapse) {
        var thisObj = this;
        var id = $(field).attr("id");
        var value = $(field).filter(":not(.hidden)").val();
        var currentPage = $(this.editor).find("#" + this.page.id);
        var row = $(field).closest(".repeater-row");
        var anchor = $(this.editor).find(".anchor[anchorField=\"" + id + "\"]");
        var elData = $(row).data("element");

        var data = null;
        var propertyValues = null;
        if (!((typeof this.properties.keep_value_on_change) === "undefined") && this.properties.keep_value_on_change.toLowerCase() === "true") {
            if (!((typeof thisObj.properties.propertiesDefinition[value]) === "undefined") && thisObj.properties.propertiesDefinition[value] !== null) {
                data = thisObj.getData(field);
                propertyValues = data.properties;
            } else {
                propertyValues = (elData && elData.properties) ? elData.properties : null;
            }
        } else {
            propertyValues = (elData && elData.properties) ? elData.properties : null;
        }
        
        //check if value is different, remove all the related properties page
        var existing = $(this.editor).find('.property-editor-page[elementid=' + id + ']:first');
        if ($(existing).length > 0 && $(existing).attr('elementvalue') !== value) {
            this.removePages(field);
            thisObj.editorObject.refresh();
        }
        
        //use the default api to retrieve plugin properties if url is empty
        var defaultUrl = "[CONTEXT_PATH]/web/property/json"+thisObj.options.appPath+"/getPropertyOptions";
        if (thisObj.properties.url === "" || thisObj.properties.url === undefined) {
            thisObj.properties.url = defaultUrl;
        }
        var isDefault = true;
        if (thisObj.properties.url !== defaultUrl) {
            isDefault = false; // check this property type is used for plugin or not
        }
        
        //check the value is empty or not
        var isNotEmpty = (value !== "" && value !== undefined && value !== null);

        //if properties page not found, render it now. If this is used for plugin, don't render when value is empty
        if ($(this.editor).find('.property-editor-page[elementid=' + id + ']').length === 0  && ((isDefault && isNotEmpty) || !isDefault) && !$(anchor).hasClass("loading")) {
            $(anchor).addClass("loading");
            var deferreds = [];

            PropertyEditor.Util.prevAjaxCalls = {};
            PropertyEditor.Util.showAjaxLoading(thisObj.editor, thisObj, "CONTAINER");

            deferreds.push(thisObj.getElementProperties(row, value));
            deferreds.push(thisObj.getElementDefaultProperties(value));
            PropertyEditor.Util.deferredHandler(deferreds, function() {
                if (!((typeof $(row).data("propertiesDefinition")) === "undefined") && $(row).data("propertiesDefinition") !== null) {
                    var parentId = thisObj.prefix + "_" + thisObj.properties.name;
                    var elementdata = ' elementid="' + id + '" elementvalue="' + value + '"';

                    //check if the element has a parent element
                    if (!((typeof currentPage.attr("elementid")) === "undefined") && currentPage.attr("elementid") !== "") {
                        parentId = currentPage.attr("elementid") + "_" + parentId;
                        if (!((typeof currentPage.attr("parentelementid")) === "undefined") && currentPage.attr("parentelementid") !== "") {
                            elementdata += ' parentelementid="' + currentPage.attr("parentelementid") + '"';
                        } else {
                            elementdata += ' parentelementid="' + currentPage.attr("elementid") + '"';
                        }
                    }
                    
                    var newOptions = {
                        appPath : thisObj.options.appPath,
                        contextPath : thisObj.options.contextPath,
                        showDescriptionAsToolTip: thisObj.options.showDescriptionAsToolTip,
                        changeCheckIgnoreUndefined : thisObj.options.changeCheckIgnoreUndefined,
                        skipValidation : thisObj.options.skipValidation,
                        propertiesDefinition : $(row).data("propertiesDefinition"),
                        defaultPropertyValues : thisObj.properties.defaultPropertyValues[value],
                        propertyValues : propertyValues,
                        mandatoryMessage : thisObj.options.mandatoryMessage
                    };

                    
                    if ($(anchor).hasClass("initial") && $(row).data("propertiesDefinition").length > 0) {
                        $(anchor).removeClass("initial").addClass("partialLoad");
                        var cloneDefinitionFirstPage = $.extend({}, newOptions.propertiesDefinition[0]);
                        cloneDefinitionFirstPage.properties = [{
                            name : "hidden",
                            type : "hidden"
                        }];
                        var html = "";
                        var p = new PropertyEditor.Model.Page(thisObj.editorObject, 0, cloneDefinitionFirstPage, elementdata, parentId);
                        p.options = newOptions;
                        html += p.render();
                        $(anchor).after(html);
                        
                        if ($(anchor).parent(".element-pages").length > 0 && $(anchor).parent(".element-pages").find(".property-page-show").length > 0) {
                            $(anchor).parent(".element-pages").show();
                        }

                        var temp = $(anchor).next();
                        $(temp).addClass("collapsed");
                        
                        $(temp).on("click", function() {
                            thisObj.renderPropertiesPages(id, row, anchor, newOptions, propertyValues, elementdata, value);
                            $(temp).off("click");
                            $(temp).remove();
                            
                            $(anchor).next().removeClass("collapsed");
                        });
                        
                        //trigger a change event for fields depended on this element select field
                        $(temp).find('[name]').trigger("change");
                    } else {
                        thisObj.renderPropertiesPages(id, row, anchor, newOptions, propertyValues, elementdata, value, collapse);
                    }
                }
                thisObj.editorObject.refresh();
                PropertyEditor.Util.removeAjaxLoading(thisObj.editor, thisObj, "CONTAINER");
                $(anchor).removeClass("loading");
            });
        }
    },
    renderPropertiesPages : function(id, row, anchor, newOptions, propertyValues, elementdata, value, collapse) {
        var thisObj = this;
        
        //handle keep_value_on_change equal to true with new added property default value
        if (propertyValues !== null && (typeof propertyValues) !== "undefined" && !((typeof thisObj.properties.keep_value_on_change) === "undefined") && thisObj.properties.keep_value_on_change.toLowerCase() === "true") {
            $.each($(row).data("propertiesDefinition"), function(i, page) {
                if (page.properties !== undefined) {
                    $.each(page.properties, function(i, property) {
                        //if there is default value and the property name is not exist in existing values
                        if (property.value !== undefined && propertyValues[property.name] === undefined) {
                            propertyValues[property.name] = property.value;
                        }
                    });
                }
            });
        }

        var html = "";
        $.each(newOptions.propertiesDefinition, function(i, page) {
            var p = page.propertyEditorObject;
            if (((typeof p) === "undefined")) {
                p = new PropertyEditor.Model.Page(thisObj.editorObject, i, page, elementdata, id);
                p.options = newOptions;
                page.propertyEditorObject = p;
                thisObj.editorObject.pages[p.id] = p;
            }
            html += p.render();
        });
        $(anchor).after(html);

        $.each(newOptions.propertiesDefinition, function(i, page) {
            var p = page.propertyEditorObject;
            p.initScripting();
        });

        //add parent properties to plugin header
        var valueLabel = $("#" + thisObj.id).find('option[value="' + value + '"]').text();
        var parentTitle = '<h1>' + thisObj.properties.label + " (" + valueLabel + ')</h1>';
        var childFirstPage = $(thisObj.editor).find('.property-editor-page[elementid=' + thisObj.id + '].property-page-show:eq(0)');
        $(childFirstPage).find('.property-editor-page-title').prepend(parentTitle);

        if ($(anchor).parent(".element-pages").length > 0 && $(anchor).parent(".element-pages").find(".property-page-show").length > 0) {
            $(anchor).parent(".element-pages").show();
        }

        if (collapse === true) {
            $(anchor).next().addClass("collapsed");
        }
        $(anchor).removeClass("partialLoad");
        
        thisObj.editorObject.refresh();
        PropertyEditor.Util.removeAjaxLoading(thisObj.editor, thisObj, "CONTAINER");
        $(anchor).removeClass("loading");
    },
    getElementProperties: function(row, value) {
        var thisObj = this;
        var d = $.Deferred();
        
        var ajaxUrl = PropertyEditor.Util.replaceContextPath(this.properties.url, this.options.contextPath) + "?" + "value=" + encodeURIComponent(value);
        if (PropertyEditor.Util.cachedAjaxCalls[ajaxUrl] !== undefined) {
            $(row).data("propertiesDefinition", $.extend(true, [], PropertyEditor.Util.cachedAjaxCalls[ajaxUrl]));
            d.resolve();
            return d;
        }
        
        $.ajax({
            url: PropertyEditor.Util.replaceContextPath(this.properties.url, this.options.contextPath),
            data: "value=" + encodeURIComponent(value),
            dataType: "text",
            headers: {
                call_reference : thisObj.properties.options_ajax
            },
            success: function(response) {
                if (response !== null && !((typeof response) === "undefined") && response !== "") {
                    try {
                        var data = eval(response);
                        if (ajaxUrl.indexOf("/getPropertyOptions?") !== -1) {
                            PropertyEditor.Util.cachedAjaxCalls[ajaxUrl] = $.extend(true, [], data);
                        }
                        
                        $(row).data("propertiesDefinition", data);
                    } catch (err) {
                        if (console && console.log) {
                            console.log("error retrieving properties options of " + value + " : " + err);
                        }
                        $(row).data("propertiesDefinition", null);
                    }
                } else {
                    $(row).data("propertiesDefinition", null);
                }
                d.resolve();
            }
        });

        return d;
    },
    getElementDefaultProperties: function(value) {
        var thisObj = this;
        var d = $.Deferred();

        if (this.properties.default_property_values_url !== null && !((typeof this.properties.default_property_values_url) === "undefined") &&
            this.properties.default_property_values_url !== "") {
            if (((typeof thisObj.properties.defaultPropertyValues[value]) === "undefined")) {
                
                var ajaxUrl = PropertyEditor.Util.replaceContextPath(this.properties.default_property_values_url, this.options.contextPath) + "?" + "value=" + encodeURIComponent(value);
                if (PropertyEditor.Util.cachedAjaxCalls[ajaxUrl] !== undefined) {
                    thisObj.properties.defaultPropertyValues[value] = $.extend(true, {}, PropertyEditor.Util.cachedAjaxCalls[ajaxUrl]);
                    d.resolve();
                    return d;
                }
            
                $.ajax({
                    url: PropertyEditor.Util.replaceContextPath(this.properties.default_property_values_url, this.options.contextPath),
                    data: "value=" + encodeURIComponent(value),
                    dataType: "text",
                    success: function(response) {
                        if (response !== null && !((typeof response) === "undefined") && response !== "") {
                            var data = $.parseJSON(response);
                            if (ajaxUrl.indexOf("/getDefaultProperties?") !== -1) {
                                PropertyEditor.Util.cachedAjaxCalls[ajaxUrl] = $.extend(true, {}, data);
                            }

                            thisObj.properties.defaultPropertyValues[value] = data;
                        } else {
                            thisObj.properties.defaultPropertyValues[value] = null;
                        }
                        d.resolve();
                    }
                });
            } else {
                d.resolve();
            }
        } else {
            d.resolve();
        }

        return d;
    },
    removePages: function(field) {
        var thisObj = this;
        var value = $(field).val();
        var row = $(field).closest(".repeater-row");
        var id = $(field).attr("id");
        var anchor = $(this.editor).find(".anchor[anchorField=\"" + id + "\"]");
        
        if ($(anchor).hasClass("partialLoad")) {
            $(anchor).next().remove();
            $(anchor).removeClass("partialLoad");
        } else {
            var propertiesDefinition = $(row).data("propertiesDefinition");
            if (!((typeof propertiesDefinition) === "undefined") && propertiesDefinition !== null) {
                $.each(propertiesDefinition, function(i, page) {
                    var p = page.propertyEditorObject;
                    if (p !== undefined) {
                        p.remove();
                    }
                });
            }
        }
        $(row).removeData("propertiesDefinition");
        thisObj.editorObject.refresh();
    },
    remove: function() {
        var thisObj = this;
        $("#" + this.id + "_input select").each(function(){
            thisObj.removePages($(this));
        });
    },
    updateRows: function() {
        var thisObj = this;
        thisObj.editorObject.refresh();
    }
};
PropertyEditor.Type.ElementMultiSelect = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.ElementMultiSelect.prototype);

PropertyEditor.Type.AutoComplete = function() {};
PropertyEditor.Type.AutoComplete.prototype = {
    shortname: "autocomplete",
    source: [],
    supportPrefix: true,
    renderField: function() {
        var size = '';
        if (this.value === null) {
            this.value = "";
        }
        if (this.properties.size !== undefined && this.properties.size !== null) {
            size = ' size="' + this.properties.size + '"';
        } else {
            size = ' size="50"';
        }
        var maxlength = '';
        if (this.properties.maxlength !== undefined && this.properties.maxlength !== null) {
            maxlength = ' maxlength="' + this.properties.maxlength + '"';
        }

        PropertyEditor.Util.retrieveOptionsFromCallback(this, this.properties);
        this.updateSource();

        return '<input type="text" class="autocomplete" id="' + this.id + '" name="' + this.id + '"' + size + maxlength + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '"/>';
    },
    handleAjaxOptions: function(options, reference) {
        this.properties.options = options;
        this.updateSource();
    },
    updateSource: function() {
        var thisObj = this;
        this.source = [];
        if (this.properties.options !== undefined) {
            $.each(this.properties.options, function(i, option) {
                if (option['value'] !== "" && $.inArray(option['value'], thisObj.source) === -1) {
                    thisObj.source.push(option['value']);
                }
            });
        }
        this.source.sort();
        $("#" + this.id).autocomplete("option", "source", this.source);
    },
    initScripting: function() {
        var thisObj = this;
        
        var args = {
            source: thisObj.source,
            minLength: 0,
            open: function() {
                $(this).autocomplete('widget').css('z-index', 99999);
                return false;
            }
        };
        
        if (this.properties.multivalues !== undefined && this.properties.multivalues.toLowerCase() === "true") {
            args['source'] = function( request, response ) {
                response($.ui.autocomplete.filter(thisObj.source, thisObj.extractLast(request.term)));
            };
            args['select'] = function( event, ui ) {
                var terms = thisObj.splitTerms(this.value);
                terms.pop();
                terms.push( ui.item.value );
                this.value = terms.join("; ");
                return false;
            };
        }
        
        $("#" + this.id).autocomplete(args);
    },
    splitTerms: function(val) {
        return val.split( /;\s*/ );
    },
    extractLast: function( terms ) {
        return this.splitTerms(terms).pop();
    }
};
PropertyEditor.Type.AutoComplete = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.AutoComplete.prototype);

PropertyEditor.Type.File = function() {};
PropertyEditor.Type.File.prototype = {
    shortname: "file",
    source: [],
    renderField: function() {
        var size = '';
        var imagesize = '';
        if (this.value === null) {
            this.value = "";
        }
        if (this.properties.size !== undefined && this.properties.size !== null) {
            size = ' size="' + this.properties.size + '"';
        } else {
            size = ' size="50"';
        }
        var maxlength = '';
        if (this.properties.maxlength !== undefined && this.properties.maxlength !== null) {
            maxlength = ' maxlength="' + this.properties.maxlength + '"';
        }

        if (this.properties.allowInput === undefined || this.properties.allowInput === null || this.properties.allowInput !== "true") {
            maxlength += " readonly";
        }

        return '<input type="text" class="image" id="' + this.id + '" name="' + this.id + '"' + size + maxlength + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '"/><div class="file-picker-actions"><a class="choosefile btn button small"><i class="fas fa-folder-open"></i></a><a class="clearfile"><i class="fas fa-undo"></i></a></div>';
    },
    initScripting: function() {
        var thisObj = this;
        $("#" + this.id).parent().find(".clearfile").off("click");
        $("#" + this.id).parent().find(".clearfile").on("click", function() {
            $("#" + thisObj.id).val("").trigger("focus").trigger("change");
        });

        $("#" + this.id).parent().find(".choosefile").off("click");
        $("#" + this.id).parent().find(".choosefile").on("click", function() {
            $("#" + thisObj.id).trigger("focus");
            PropertyEditor.Util.showAppResourcesDialog(thisObj);
        });
    },
    selectResource: function(filename) {
        if (this.properties.appResourcePrefix !== undefined && this.properties.appResourcePrefix !== null && this.properties.appResourcePrefix === "true") {
            filename = "#appResource." + filename + "#";
        }
        $("#" + this.id).val(filename).trigger("change");
    }
};
PropertyEditor.Type.File = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.File.prototype);

PropertyEditor.Type.Image = function() {};
PropertyEditor.Type.Image.prototype = {
    shortname: "image",
    source: [],
    renderField: function() {
        var size = '';
        var imagesize = '';
        if (this.value === null) {
            this.value = "";
        }
        if (this.properties.size !== undefined && this.properties.size !== null) {
            size = ' size="' + this.properties.size + '"';
        } else {
            size = ' size="50"';
        }
        if (this.properties.imageSize !== undefined && this.properties.imageSize !== null) {
            if (isNaN(this.properties.imageSize)) {
                imagesize = " " + this.properties.imageSize;
            } else {
                imagesize = ' width:' + this.properties.imageSize + 'px; height:' + this.properties.imageSize + 'px;';
            }
        } else {
            imagesize = ' width:80px; height:80px;';
        }
        var maxlength = '';
        if (this.properties.maxlength !== undefined && this.properties.maxlength !== null) {
            maxlength = ' maxlength="' + this.properties.maxlength + '"';
        }

        if (this.properties.allowInput === undefined || this.properties.allowInput === null || this.properties.allowInput !== "true") {
            maxlength += " readonly";
        }

        if (this.properties.allowType === undefined || this.properties.allowType === null || this.properties.allowType === "") {
            this.properties.allowType = ".jpeg;.jpg;.gif;.png";
        }

        var style = imagesize;
        if (this.value !== "") {
            var path = this.value;
            if (path.indexOf("#appResource.") !== -1) {
                path = path.substring(0, path.length - 1);
                path = path.replace("#appResource.", this.options.contextPath + "/web/app" + this.properties.appPath + "/resources/");
            }
            path = path.replace("[CONTEXT_PATH]", this.options.contextPath);
            path = path.replace("[APP_PATH]", this.properties.appPath);
            style += " background-image:url('" + PropertyEditor.Util.escapeHtmlTag(path) + "')";
        }

        return '<input type="text" class="image" id="' + this.id + '" name="' + this.id + '"' + size + maxlength + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '"/><div class="file-picker-actions"><a class="choosefile btn button small"><i class="fas fa-folder-open"></i></a></div><div class="image-placeholder" style="' + style + '"><a class="image-remove"><i class="fas fa-times"></i></a></div>';
    },
    initScripting: function() {
        var thisObj = this;
        $("#" + this.id).off("change.init");
        $("#" + this.id).on("change.init", function() {
            var value = $(this).val();
            var path = value;
            if (path.indexOf("#appResource.") !== -1) {
                path = path.substring(0, path.length - 1);
                path = path.replace("#appResource.", thisObj.options.contextPath + "/web/app" + thisObj.properties.appPath + "/resources/");
            }
            path = path.replace("[CONTEXT_PATH]", thisObj.options.contextPath);
            path = path.replace("[APP_PATH]", thisObj.properties.appPath);
            var imagePlaceholder = $(this).parent().find(".image-placeholder");
            $(imagePlaceholder).css("background-image", "url('" + PropertyEditor.Util.escapeHtmlTag(path) + "')");
        });

        $("#" + this.id).parent().find(".image-remove").off("click");
        $("#" + this.id).parent().find(".image-remove").on("click", function() {
            $("#" + thisObj.id).val("").trigger("focus").trigger("change");
        });

        $("#" + this.id).parent().find(".choosefile").off("click");
        $("#" + this.id).parent().find(".choosefile").on("click", function() {
            $("#" + thisObj.id).trigger("focus");
            PropertyEditor.Util.showAppResourcesDialog(thisObj);
        });

    },
    selectResource: function(filename) {
        $("#" + this.id).val("#appResource." + filename + "#").trigger("change");
    }
};
PropertyEditor.Type.Image = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Image.prototype);

PropertyEditor.Type.Custom = function() {};
PropertyEditor.Type.Custom.prototype = {
    shortname: "custom",
    initialize: function() {
        var thisObj = this;
        $.ajax({
            dataType: "text",
            url: PropertyEditor.Util.replaceContextPath(thisObj.properties.script_url, thisObj.options.contextPath),
            success: function(customObjScript) {
                if (customObjScript !== undefined && customObjScript !== "") {
                    try {
                        var customObj = eval("[" + customObjScript + "]" )[0];
                        $.extend(thisObj, customObj);
                        if (customObj['initialize'] !== undefined) {
                            thisObj.initialize.apply(thisObj);
                        }
                        $("#" + thisObj.id + "_input").replaceWith($(thisObj.renderFieldWrapper()));
                        
                        thisObj.initScripting();
                    } catch (err) {}
                }
            }
        });
    }
};
PropertyEditor.Type.Custom = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Custom.prototype);

PropertyEditor.Type.CssStyle = function() {};
PropertyEditor.Type.CssStyle.prototype = {
    shortname: "cssstyle",
    styleGroups : {
        "text" : {
            header : "<i class=\"las la-font\"></i> " + get_peditor_msg("style.text"),
            fields : {
                "font-size" : {"field" : "unit", "label" : get_peditor_msg("style.fontSize"), "class" : "input1"},
                "color" : {"field" : "color", "label" : get_peditor_msg("style.color"), "class" : "input1"},
                "font-family" : {"field" : "font-family", "label" : get_peditor_msg("style.fontFamily"), "class" : "input2"},
                "font-weight" : {"field" : "font-weight", "label" : get_peditor_msg("style.fontWeight"), "class" : "input1"},
                "line-height" : {"field" : "unit", "label" : get_peditor_msg("style.lineHeight"), "class" : "input1"},
                "text-align" : {"field" : "text-align", "label" : get_peditor_msg("style.textAlign"), "class" : "input1"},
                "font-style" : {"field" : "font-style", "label" : get_peditor_msg("style.fontStyle"), "class" : "input1"},
                "letter-spacing" : {"field" : "unit", "label" : get_peditor_msg("style.letterSpacing"), "class" : "input1"},
                "text-decoration-color" : {"field" : "color", "label" : get_peditor_msg("style.textDecorationColor"), "class" : "input1"},
                "text-decoration-line" : {"field" : "text-decoration-line", "label" : get_peditor_msg("style.textDecoration"), "class" : "input2"},
                "text-decoration-style" : {"field" : "text-decoration-style", "label" : get_peditor_msg("style.textDecorationStyle"), "class" : "input1"}
            }
        },
        "background" : {
            header : "<i class=\"las la-fill\"></i> " + get_peditor_msg("style.background"),
            fields : {
                "background-image" : {"field" : "image", "label" : get_peditor_msg("style.backgroundImage"), "class" : "input3"},
                "background-color" : {"field" : "color", "label" : get_peditor_msg("style.backgroundColor"), "class" : "input1"},
                "background-repeat" : {"field" : "background-repeat", "label" : get_peditor_msg("style.repeat"), "class" : "input1"},
                "background-size" : {"field" : "background-size", "label" : get_peditor_msg("style.backgroundSize"), "class" : "input1"},
                "background-position-x" : {"field" : "unit", "label" : get_peditor_msg("style.positionX"), "class" : "input1"},
                "background-position-y" : {"field" : "unit", "label" : get_peditor_msg("style.positionY"), "class" : "input1"}
            }
        },
        "margin" : {
            header : "<i class=\"las la-arrows-alt\"></i> " + get_peditor_msg("style.margin"),
            fields : {
                "margin-top" : {"field" : "unit", "label" : get_peditor_msg("style.marginTop"), "class" : "input1"},
                "margin-left" : {"field" : "unit", "label" : get_peditor_msg("style.marginLeft"), "class" : "input1"},
                "margin-right" : {"field" : "unit", "label" : get_peditor_msg("style.marginRight"), "class" : "input1"},
                "margin-bottom" : {"field" : "unit", "label" : get_peditor_msg("style.marginBottom"), "class" : "input1"}
            }
        },
        "padding" : {
            header : "<i class=\"las la-compress-arrows-alt\"></i> " + get_peditor_msg("style.padding"),
            fields : {
                "padding-top" : {"field" : "unit", "label" : get_peditor_msg("style.paddingTop"), "class" : "input1"},
                "padding-left" : {"field" : "unit", "label" : get_peditor_msg("style.paddingLeft"), "class" : "input1"},
                "padding-right" : {"field" : "unit", "label" : get_peditor_msg("style.paddingRight"), "class" : "input1"},
                "padding-bottom" : {"field" : "unit", "label" : get_peditor_msg("style.paddingBottom"), "class" : "input1"}
            }
        },
        "border" : {
            header : "<i class=\"las la-border-style\"></i> " + get_peditor_msg("style.border"),
            fields : {
                "border-style" : {"field" : "border-style", "label" : get_peditor_msg("style.style"), "class" : "input1"},
                "border-color" : {"field" : "color", "label" : get_peditor_msg("style.borderColor"), "class" : "input1"},
                "border-top-width" : {"field" : "unit", "label" : get_peditor_msg("style.borderTop"), "class" : "input1"},
                "border-left-width" : {"field" : "unit", "label" : get_peditor_msg("style.borderLeft"), "class" : "input1"},
                "border-right-width" : {"field" : "unit", "label" : get_peditor_msg("style.borderRight"), "class" : "input1"},
                "border-bottom-width" : {"field" : "unit", "label" : get_peditor_msg("style.borderBottom"), "class" : "input1"},
                "border-radius" : {"field" : "unit", "label" : get_peditor_msg("style.borderRadius"), "class" : "input1"}
            }
        },
        "display" : {
            header : "<i class=\"lar la-images\"></i> " + get_peditor_msg("style.display"),
            fields : {
                "display" : {"field" : "display", "label" : get_peditor_msg("style.display"), "class" : "input1"},
                "position" : {"field" : "position", "label" : get_peditor_msg("style.position"), "class" : "input1"},
                "top" : {"field" : "unit", "label" : get_peditor_msg("style.top"), "class" : "input1"},
                "left" : {"field" : "unit", "label" : get_peditor_msg("style.left"), "class" : "input1"},
                "right" : {"field" : "unit", "label" : get_peditor_msg("style.right"), "class" : "input1"},
                "bottom" : {"field" : "unit", "label" : get_peditor_msg("style.bottom"), "class" : "input1"},
                "float" : {"field" : "float", "label" : get_peditor_msg("style.float"), "class" : "input1"}
            }
        },
        "size" : {
            header : "<i class=\"las la-vector-square\"></i> " + get_peditor_msg("style.size"),
            fields : {
                "width" : {"field" : "unit", "label" : get_peditor_msg("style.width"), "class" : "input1"},
                "height" : {"field" : "unit", "label" : get_peditor_msg("style.height"), "class" : "input1"},
                "min-width" : {"field" : "unit", "label" : get_peditor_msg("style.minWidth"), "class" : "input1"},
                "min-height" : {"field" : "unit", "label" : get_peditor_msg("style.minHeight"), "class" : "input1"},
                "max-width" : {"field" : "unit", "label" : get_peditor_msg("style.maxWidth"), "class" : "input1"},
                "max-height" : {"field" : "unit", "label" : get_peditor_msg("style.maxHeight"), "class" : "input1"}
            }
        },
        "custom" : {
            header : "<i class=\"las la-code\"></i> " + get_peditor_msg("style.custom"),
            fields : {
                "custom" : {"field" : "custom", "label" : get_peditor_msg("style.custom"), "class" : "input3"}
            }
        }
    },
    styleFields : {
        'display' : {
            type : 'selectbox',
            options : [
                {value : '', label : get_peditor_msg("style.default")},
                {value : 'block', label : get_peditor_msg("style.block")},
                {value : 'inline', label : get_peditor_msg("style.inline")},
                {value : 'inline-block', label : get_peditor_msg("style.inlineBlock")},
                {value : 'none', label : get_peditor_msg("style.none")}
            ]
        },
        'position' : {
            type : 'selectbox',
            options : [
                {value : '', label : get_peditor_msg("style.default")},
                {value : 'static', label : get_peditor_msg("style.static")},
                {value : 'fixed', label : get_peditor_msg("style.fixed")},
                {value : 'relative', label : get_peditor_msg("style.relative")},
                {value : 'absolute', label : get_peditor_msg("style.absolute")}
            ]
        },
        'unit' : {
            type : 'number',
            mode : 'css_unit'
        },
        'float' : {
            type : 'iconbuttons',
            options : [
                {value : '', label : '<i class="la la-times"></i>', title : get_peditor_msg("style.none")},
                {value : 'left', label : '<i class="la la-align-left"></i>', title : get_peditor_msg("style.left")},
                {value : 'right', label : '<i class="la la-align-right"></i>', title : get_peditor_msg("style.right")}
            ]
        },
        'color' : {
            type : 'color'
        },
        'font-family' : {
            type : 'selectbox',
            options : [
                {value : '', label : 'Default'},
                {value : 'Arial, Helvetica, sans-serif', label : '&lt;span style="font-family:Arial;"&gt;Arial&lt;/span&gt;'},
                {value : '\'Lucida Sans Unicode\', \'Lucida Grande\', sans-serif', label : '&lt;span style="font-family:\'Lucida Sans Unicode\', \'Lucida Grande\', sans-serif;"&gt;Lucida Grande&lt;/span&gt;'},
                {value : '\'Palatino Linotype\', \'Book Antiqua\', Palatino, serif', label : '&lt;span style="font-family:\'Palatino Linotype\', \'Book Antiqua\', Palatino, serif;"&gt;Palatino Linotype&lt;/span&gt;'},
                {value : '\'Times New Roman\', Times, serif', label : '&lt;span style="font-family:\'Times New Roman\', Times, serif;"&gt;Times New Roman&lt;/span&gt;'},
                {value : 'Georgia, serif', label : '&lt;span style="font-family:Georgia, serif;"&gt;Georgia, serif&lt;/span&gt;'},
                {value : 'Tahoma, Geneva, sans-serif', label : '&lt;span style="font-family:Tahoma, Geneva, sans-serif;"&gt;Tahoma&lt;/span&gt;'},
                {value : '\'Comic Sans MS\', cursive, sans-serif', label : '&lt;span style="font-family:\'Comic Sans MS\', cursive, sans-serif;"&gt;Comic Sans&lt;/span&gt;'},
                {value : 'Verdana, Geneva, sans-serif', label : '&lt;span style="font-family:Verdana, Geneva, sans-serif;"&gt;Verdana&lt;/span&gt;'},
                {value : 'Impact, Charcoal, sans-serif', label : '&lt;span style="font-family:Impact, Charcoal, sans-serif;"&gt;Impact&lt;/span&gt;'},
                {value : '\'Arial Black\', Gadget, sans-serif', label : '&lt;span style="font-family:\'Arial Black\', Gadget, sans-serif;"&gt;Arial Black&lt;/span&gt;'},
                {value : '\'Trebuchet MS\', Helvetica, sans-serif', label : '&lt;span style="font-family:\'Trebuchet MS\', Helvetica, sans-serif;"&gt;Trebuchet&lt;/span&gt;'},
                {value : '\'Courier New\', Courier, monospace', label : '&lt;span style="font-family:\'Courier New\', Courier, monospace;"&gt;Courier New&lt;/span&gt;'},
                {value : '\'Brush Script MT\', sans-serif', label : '&lt;span style="font-family:\'Brush Script MT\', sans-serif;"&gt;Brush Script&lt;/span&gt;'}
            ],
            html : 'true'
        },
        'font-weight' : {
            type : 'selectbox',
            options : [
                {value : '', label : get_peditor_msg("style.default")},
                {value : '100', label : '&lt;span style="font-weight:100;"&gt;' + get_peditor_msg("style.thin") + '&lt;/span&gt;'},
                {value : '200', label : '&lt;span style="font-weight:200;"&gt;' + get_peditor_msg("style.extraLight") + '&lt;/span&gt;'},
                {value : '300', label : '&lt;span style="font-weight:300;"&gt;' + get_peditor_msg("style.light") + '&lt;/span&gt;'},
                {value : '400', label : '&lt;span style="font-weight:400;"&gt;' + get_peditor_msg("style.normal") + '&lt;/span&gt;'},
                {value : '500', label : '&lt;span style="font-weight:500;"&gt;' + get_peditor_msg("style.medium") + '&lt;/span&gt;'},
                {value : '600', label : '&lt;span style="font-weight:600;"&gt;' + get_peditor_msg("style.semiBold") + '&lt;/span&gt;'},
                {value : '700', label : '&lt;span style="font-weight:700;"&gt;' + get_peditor_msg("style.bold") + '&lt;/span&gt;'},
                {value : '800', label : '&lt;span style="font-weight:800;"&gt;' + get_peditor_msg("style.extraBold") + '&lt;/span&gt;'},
                {value : '900', label : '&lt;span style="font-weight:900;"&gt;' + get_peditor_msg("style.ultraBold") + '&lt;/span&gt;'}
            ],
            html : 'true'
        },
        'text-align' : {
            type : 'iconbuttons',
            options : [
                {value : '', label : '<i class="la la-times"></i>', title : get_peditor_msg("style.default")},
                {value : 'left', label : '<i class="la la-align-left"></i>', title : get_peditor_msg("style.left")},
                {value : 'center', label : '<i class="la la-align-center"></i>', title : get_peditor_msg("style.center")},
                {value : 'right', label : '<i class="la la-align-right"></i>', title : get_peditor_msg("style.right")},
                {value : 'justify', label : '<i class="la la-align-justify"></i>', title : get_peditor_msg("style.justify")}
            ]
        },
        'text-decoration-line' : {
            type : 'selectbox',
            options : [
                {value : '', label : get_peditor_msg("style.default")},
                {value : 'none', label : get_peditor_msg("style.none")},
                {value : 'underline', label : '&lt;span style="text-decoration-line:underline;"&gt;' + get_peditor_msg("style.underline") + '&lt;/span&gt;'},
                {value : 'overline', label : '&lt;span style="text-decoration-line:overline;"&gt;' + get_peditor_msg("style.overline") + '&lt;/span&gt;'},
                {value : 'line-through', label : '&lt;span style="text-decoration-line:line-through;"&gt;' + get_peditor_msg("style.lineThrough") + '&lt;/span&gt;'},
                {value : 'underline overline', label : '&lt;span style="text-decoration-line:underline overline;"&gt;' + get_peditor_msg("style.underlineOverline") + '&lt;/span&gt;'}
            ],
            html : 'true'
        },
        'text-decoration-style' : {
            type : 'selectbox',
            options : [
                {value : '', label : get_peditor_msg("style.default")},
                {value : 'solid', label : '&lt;span style="text-decoration-line:underline;text-decoration-style:solid;"&gt;' + get_peditor_msg("style.solid") + '&lt;/span&gt;'},
                {value : 'wavy', label : '&lt;span style="text-decoration-line:underline;text-decoration-style:wavy;"&gt;' + get_peditor_msg("style.wavy") + '&lt;/span&gt;'},
                {value : 'dotted', label : '&lt;span style="text-decoration-line:underline;text-decoration-style:dotted;"&gt;' + get_peditor_msg("style.dotted") + '&lt;/span&gt;'},
                {value : 'dashed', label : '&lt;span style="text-decoration-line:underline;text-decoration-style:dashed;"&gt;' + get_peditor_msg("style.dashed") + '&lt;/span&gt;'},
                {value : 'double', label : '&lt;span style="text-decoration-line:underline;text-decoration-style:double;"&gt;' + get_peditor_msg("style.double") + '&lt;/span&gt;'}
            ],
            html : 'true'
        },
        'font-style' : {
            type : 'selectbox',
            options : [
                {value : '', label : get_peditor_msg("style.default")},
                {value : 'italic', label : '&lt;span style="font-style: italic;"&gt;' + get_peditor_msg("style.italic") + '&lt;/span&gt;'},
                {value : 'oblique', label : '&lt;span style="font-style: oblique;"&gt;' + get_peditor_msg("style.oblique") + '&lt;/span&gt;'}
            ],
            html : 'true'
        },
        'border-style' : {
            type : 'selectbox',
            options : [
                {value : '', label : get_peditor_msg("style.default")},
                {value : 'solid', label : '&lt;span style="border:1px solid #ccc;"&gt;' + get_peditor_msg("style.solid") + '&lt;/span&gt;'},
                {value : 'dotted', label : '&lt;span style="border:1px dotted #ccc;"&gt;' + get_peditor_msg("style.dotted") + '&lt;/span&gt;'},
                {value : 'dashed', label : '&lt;span style="border:1px dashed #ccc;"&gt;' + get_peditor_msg("style.dashed") + '&lt;/span&gt;'},
                {value : 'double', label : '&lt;span style="border:1px double #ccc;"&gt;' + get_peditor_msg("style.double") + '&lt;/span&gt;'}
            ],
            html : 'true'
        },
        'image' : {
            type: 'image',
            appPath: '',
            allowInput : 'true',
            isPublic : 'true',
            imageSize : 'width:100px;height:100px;'
        },
        'background-repeat' : {
            type : 'selectbox',
            options : [
                {value : '', label : get_peditor_msg("style.default")},
                {value : 'repeat-x', label : get_peditor_msg("style.repeatX")},
                {value : 'repeat-y', label : get_peditor_msg("style.repeatY")},
                {value : 'no-repeat', label : get_peditor_msg("style.noRepeat")}
            ]
        },
        'background-size' : {
            type : 'selectbox',
            options : [
                {value : '', label : get_peditor_msg("style.default")},
                {value : 'contain', label : get_peditor_msg("style.contain")},
                {value : 'cover', label : get_peditor_msg("style.cover")}
            ]
        },
        'custom' : {
            type : 'codeeditor',
            "mode" : "text"
        }
    },
    getData: function(useDefault) {
        var field = this;
        var properties = new Object();
        var prefix = field.properties.name;
        if (this.isDataReady) {
            for (var g in field.styleGroups) {
                for (var p in field.styleGroups[g].fields) {
                    properties[prefix + "-" + p] = "";
                }
            }
            
            $("#" + field.id + " .css-styles-container .style-group").each(function(){
                var fields = $(this).data("fields");
                if (fields !== undefined) {
                    $.each(fields, function(i, property) {
                        var type = property.propertyEditorObject;
                        var data = type.getData(false);
                        if (data !== null) {
                            properties[property.name] = data[property.name];
                        }
                    });
                }
            });
        } else {
            if (field.options.propertyValues !== undefined && field.options.propertyValues !== null) {
                for (var g in field.styleGroups) {
                    for (var p in field.styleGroups[g].fields) {
                        if (field.options.propertyValues[prefix + "-" +p] !== undefined) {
                            properties[prefix + "-" + p] = field.options.propertyValues[prefix + "-" +p];
                        }
                    }
                }
            }
        }
        
        return properties;
    },
    renderField : function() {
        var thisObj = this;
        this.isDataReady = false;
        
        var html = '<div id="'+thisObj.id+'" name="'+thisObj.id+'"><div class="css-styles-container"></div>';
        
        var options = '<option value=""></option>';
        for (var g in thisObj.styleGroups) {
            options += '<option value="'+g+'" >'+UI.escapeHTML(thisObj.styleGroups[g].header)+'</option>';
        }
        
        html += '<div style="text-align:left; margin-top:5px; margin-bottom: 10px;"><select class="add_new_style initChosen">'+options+'</select></div></div>';
        
        return html;
    },
    initScripting : function() {
        var field = this;
        
        var updateLabel = function(chosen) {
            var options = chosen.results_data;
            for (var i=0; i<options.length; i++) {
                if (options[i].original === undefined) {
                    const element = document.createElement('div');
                    element.innerHTML = options[i].text;
                    options[i].original = element.textContent;
                    var temp = $('<div>'+options[i].original+'</div>');
                    options[i].textonly = $(temp).text();
                    options[i].icon = $(temp).find('i');
                }
                options[i].text = options[i].textonly;
            }
            
            $(chosen.container).find(".chosen-results li, .chosen-single > span, .search-choice > span").each(function() {
                var index = $(this).attr('data-option-array-index');
                if (chosen.results_data[index]) {
                    var icon = chosen.results_data[index].icon;
                    $(this).prepend(icon);
                }
            });
        };
        
        $("#" + this.id).find(".add_new_style").chosen({ width: "100%", placeholder_text: get_peditor_msg("style.addNew") })
        .off('chosen:showing_dropdown.updatelabel chosen:hiding_dropdown.updatelabel chosen:ready.updatelabel chosen:updated.updatelabel change.updatelabel keyup.updatelabel')
        .on('chosen:showing_dropdown.updatelabel chosen:hiding_dropdown.updatelabel chosen:ready.updatelabel chosen:updated.updatelabel change.updatelabel keyup.updatelabel', function() {
            updateLabel($("#" + field.id + " .add_new_style").data("chosen"));
        });
        setTimeout(function() {
            if ($("#" + field.id + " .add_new_style").length > 0) {
                $($("#" + field.id + " .add_new_style").data("chosen").container).find(".chosen-search input").off("keydown");
                $($("#" + field.id + " .add_new_style").data("chosen").container).find(".chosen-search input").on("keydown", function() {
                    setTimeout(function() { updateLabel($("#" + field.id + " .add_new_style").data("chosen")); }, 5);
                });
            }
        }, 1000);
        updateLabel($("#" + field.id + " .add_new_style").data("chosen"));
        
        for (var g in field.styleGroups) {
            field.renderGroup(g, field.options.propertyValues);
        }
        
        $("#" + field.id).find(".add_new_style").off("change.addGroup");
        $("#" + field.id).find(".add_new_style").on("change.addGroup", function() {
            var g = $("#" + field.id).find(".add_new_style").chosen().val();
            field.renderGroup(g);
        });
        
        this.isDataReady = true;
    },
    renderGroup : function(g, values) {
        var thisObj = this;
        var group = thisObj.styleGroups[g];
        
        if (values !== undefined && values !== null) {
            var isHasValues = false;
            for (var f in group.fields) {
                if (values[thisObj.properties.name + "-" +f] !== undefined) {
                    isHasValues = true;
                    break;
                }
            }
            if (!isHasValues) {
                return;
            }
        }
        
        var container = $("#" + this.id).find('.css-styles-container');
        var gHtml = "";
        var fields = [];
        for (var f in group.fields) {
            var field = $.extend(true, {}, thisObj.styleFields[group.fields[f].field]);
            field['name'] = thisObj.properties.name + "-" + f;
            field['label'] = group.fields[f].label;
            if (field['appPath'] !== undefined) {
                field['appPath'] = thisObj.options.appPath;
            }
            fields.push(field);

            var type = field.propertyEditorObject;
            if (type === undefined) {
                var value = null;
                if (values !== null && values !== undefined && values[field.name] !== undefined) {
                    value = values[field.name];
                } else if (field.value !== undefined && field.value !== null) {
                    value = field.value;
                }

                type = PropertyEditor.Util.getTypeObject(this, f, thisObj.id, field, value, null);
                field.propertyEditorObject = type;
            }

            if (type !== null) {
                gHtml += '<div class="'+group.fields[f].class+'">' + type.render() + '</div>';
            }
        }
        var group = $('<div class="style-group" data-style-group="'+g+'"><i class="delete_action fas fa-trash"></i><h6>'+group.header+'</h6><div class="style-group-input-container">' + gHtml + '</div></div>');
        $(container).append(group);
        $(group).data("fields", fields);
        
        $(group).find("> h6").off("click").on("click", function(){
            $(this).toggleClass("collapsed");
        });
        $(group).find("> .delete_action").off("click").on("click", function(){
            $(this).parent().remove();
            
            var options = '<option value=""></option>';
            for (var g in thisObj.styleGroups) {
                if ($("#" + thisObj.id).find(".css-styles-container .style-group[data-style-group='"+g+"']").length > 0) {
                    continue;
                }
                options += '<option value="'+g+'" >'+UI.escapeHTML(thisObj.styleGroups[g].header)+'</option>';
            }
            $("#" + thisObj.id).find(".add_new_style").html(options);
            $("#" + thisObj.id).find(".add_new_style").trigger("chosen:updated");
            
            if ($("#" + thisObj.id).find(".add_new_style option").length > 1) {
                $("#" + thisObj.id).find(".add_new_style").parent().show();
            }
        });
        
        $.each(fields, function(i, property) {
            var type = property.propertyEditorObject;
            type.initScripting();
            type.initDefaultScripting();
        });
        
        $("#" + thisObj.id).find(".add_new_style").find('option[value="'+g+'"]').remove();
        $("#" + thisObj.id).find(".add_new_style").trigger("chosen:updated");
        if ($("#" + thisObj.id).find(".add_new_style option").length === 1) {
            $("#" + thisObj.id).find(".add_new_style").parent().hide();
        }
        
        if (values === undefined) {
            $(group).closest(".property-editor-pages").animate({
                scrollTop: $(group).position().top
            }, 10);
        }
    }
};
PropertyEditor.Type.CssStyle = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.CssStyle.prototype);

PropertyEditor.Type.ColorScheme = function() {};
PropertyEditor.Type.ColorScheme.prototype = {
    shortname: "colorscheme",
    schemeOptions : [
        "#e9e9e9;#FFFFFF;#996C67;#291715;#c41c00;#ff5722",
        "#e9e9e9;#FFFFFF;#D3B8B9;#774B4E;#d32f2f;#9a0007",
        "#e9e9e9;#FFFFFF;#C1ADB8;#2a8ffb;#2a0814;#e72a6d",
        "#e9e9e9;#FFFFFF;#90AECF;#2a8ffb;#4a0072;#7b1fa2",
        "#e9e9e9;#FFFFFF;#7EB3C7;#334A52;#512da8;#140078",
        "#e9e9e9;#FFFFFF;#7AB5B7;#324B4C;#303f9f;#001970",
        "#e9e9e9;#FFFFFF;#AECAC7;#2C6562;#1976d2;#004ba0",
        "#e9e9e9;#FFFFFF;#AFA4DA;#312D4A;#304ffe;#0026ca",
        "#e9e9e9;#FFFFFF;#9debf9;#007252;#00838f;#005662",
        "#e9e9e9;#FFFFFF;#AABEB2;#00652D;#014048;#ff5722",
        "#e9e9e9;#FFFFFF;#BEBDAB;#565737;#2e7d32;#005005",
        "#e9e9e9;#FFFFFF;#D5C1B5;#755741;#827717;#524c00",
        "#e9e9e9;#FFFFFF;#D8BEBB;#AC2C2E;#8d6e63;#5f4339",
        "#e9e9e9;#FFFFFF;#7FD1AE;#757575;#0f2f4a;#0072d2",
        "#e9e9e9;#FFFFFF;#A4BEB8;#006651;#546e7a;#29434e"
    ],
    getData: function(useDefault) {
        var field = this;
        var data = new Object();

        if (this.isDataReady) {
            if (field.properties.editColor === undefined || field.properties.editColor.toLowerCase() !== "false") {
                var selector = $("#" + this.id + "_scheme_selector .color_values");
                if ($(selector).length > 0) {
                    var value = selector.find('colorgroup').css("background-color");
                    selector.find('colorgroup color').each(function(){
                        value += ";" + $(this).css("background-color");
                    });
                    data[this.properties.name] = value;
                } else {
                    data[this.properties.name] = "";
                }
            } else {
                var selector = $("#" + this.id + "_scheme_selector");
                if ($(selector).find("li.selected").length > 0) {
                    data[this.properties.name] = $(selector).find("li.selected").attr("data-value");
                } else {
                    data[this.properties.name] = "";
                }
            }
        } else {
            data[this.properties.name] = this.value;
        }
        return data;
    },
    renderField : function() {
        var thisObj = this;
        
        if (this.value === null) {
            this.value = "";
        }
        
        var noOfColors = 6;
        if (thisObj.properties.noOfColors !== undefined) {
            if (jQuery.type(thisObj.properties.noOfColors) === "number") {
                noOfColors = thisObj.properties.noOfColors;
            } else if (jQuery.type(thisObj.properties.noOfColors) === "string") {
                try {
                    noOfColors = parseInt(thisObj.properties.noOfColors);
                } catch (err) {}
            }
            if (noOfColors < 1) {
                noOfColors = 6;
            }
        }
        thisObj.properties.noOfColors = noOfColors;
        
        var html = '<div id="' + this.id + '_scheme_selector" class="selector"><div class="color_values">';
        
        var colors = this.value.split(";");
        if (colors.length > 1) {
            html += '<colorgroup style="background:'+colors[0]+';">';
            for (var i=1; i<noOfColors; i++) {
                if (colors[i] === undefined) {
                    colors[i] = "";
                }
                html += '<color style="background:'+colors[i]+';"></color>';
            }
            html += '</colorgroup>';
        } else {
            html += '<colorgroup>';
            for (var i=1; i<noOfColors; i++) {
                html += '<color></color>';
            }
            html += '</colorgroup>';
        }
        
        html += '<span class="trigger"><i class="fas fa-chevron-down"></i></span></div><div class="color-input" style="display:none;"><input type="text"/></div><ul style="display:none;">';

        var schemeOptions = [];
        if (thisObj.properties.schemeOptions !== undefined) {
            schemeOptions = thisObj.properties.schemeOptions;
        } else {
            schemeOptions = thisObj.schemeOptions;
        }
        $.each(schemeOptions, function(i, option) {
            var selected = "";
            if (thisObj.value === option) {
                selected = "selected";
            }
            var values = option.split(";");
            html += '<li data-value="' + PropertyEditor.Util.escapeHtmlTag(option) + '" class="' + selected + '">';
            html += '<colorgroup style="background:'+values[0]+';">';
            for (var i = 1; i < noOfColors; i++) {
                if (values[i] === undefined) {
                    values[i] = "";
                }
                html += '<color style=\"background:'+values[i]+';\"></color>';
            }
            html += '</colorgroup></li>';
        });
        
        html += '</div>';
        
        return html;
    },
    initScripting : function() {
        var thisObj = this;
        
        var selector = $("#" + this.id + "_scheme_selector");
        
        if (thisObj.properties.editColor === undefined || thisObj.properties.editColor.toLowerCase() !== "false") {
            $(selector).find(".color-input input").colorPicker({
                renderCallback: function($elm, toggled) {
                    if ($elm.val() !== "" && $elm.val() !== undefined) {
                        if (this.color.colors.alpha === 1) {
                            $elm.val('#' + this.color.colors.HEX);
                        } else {
                            $elm.val(this.color.toString('RGB'));
                        }
                    }
                }
            }).off("focusin.tcp");
            
            $(selector).find(".color_values").off("click", "colorgroup");
            $(selector).find(".color_values").on("click", "colorgroup", function(e){
                if (!$(selector).hasClass("showEditor")) {
                    $(selector).find(".color_values colorgroup, .color_values color").removeClass("editing");
                    $(e.target).addClass("editing");
                    var color = $(e.target).css("background-color");
                    if ($(e.target).attr("style") === undefined || $(e.target).attr("style") === "") {
                        color = "#000000"; // if there is no value set before, set it to black instead of transparent
                    }
                    $(selector).find(".color-input input").val("");
                    $(selector).find(".color-input").show();
                    $(selector).find(".color-input input").val(color).trigger("click");
                } else {
                    $(selector).find(".color_values .editing").css("background-color", $(selector).find(".color-input input").val());
                    $(selector).find(".color-input input").val("");
                    $(selector).find(".color-input").hide();
                    $(selector).find(".color_values colorgroup, .color_values color").removeClass("editing");
                }

                $(selector).toggleClass("showEditor");
            });
        }
        
        $(selector).find(".color_values span.trigger").off("click");
        $(selector).find(".color_values span.trigger").on("click", function(){
            $(selector).toggleClass("showPicker");
        });
        
        $(selector).find("li").off("click");
        $(selector).find("li").on("click", function(){
            $(selector).find("li").removeClass("selected");
            $(this).addClass("selected");
            thisObj.renderValue();
            $(selector).removeClass("showPicker");
        });
        this.isDataReady = true;
    },
    renderValue : function() {
        var selector = $("#" + this.id + "_scheme_selector");
        if ($(selector).find("li.selected").length > 0) {
            $(selector).find(".color_values colorgroup").remove();
            $(selector).find(".color_values").prepend($(selector).find("li.selected").html());
        }
    }
};
PropertyEditor.Type.ColorScheme = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.ColorScheme.prototype);

PropertyAssistant = {
    initialized : false,
    options : null,
    dialog : null,
    data : {},
    cachedAjaxCalls: {},
    
    /*
     * Attach property assistant event to the property editor and prepare a dialog for it
     */
    init : function(element, options) {
        PropertyAssistant.options = options;
        
        //not able to retrieve the option if there is no app apth
        if (PropertyAssistant.options.appPath === undefined || PropertyAssistant.options.appPath === "") {
            return;
        }
        
        if (!PropertyAssistant.initialized) {
            PropertyAssistant.initialized = true;
            PropertyAssistant.getDialog();
        }
        
        var keys = {};
        $(element).keydown(function(e) {
            if (!(e.ctrlKey && e.altKey)) {
                keys[e.which] = true;
                if (keys[17] === true && keys[16] === true && keys[18] !== true && keys[51] === true) {
                    var field = $(element).find(":focus");
                    if ($(field).length > 0) {
                        PropertyAssistant.currentField = field[0];
                        PropertyAssistant.currentCaretPosition = PropertyAssistant.doGetCaretPosition(field[0]);
                        PropertyAssistant.showDialog();
                    }
                    keys = {};
                }
            }
        }).keyup(function(e) {
            delete keys[e.which];
        });
        
        $(element).off("focus.assist", "input[name]:not([type=checkbox]):not([type=radio]):not([type=button]):not([type=number]), textarea, .ace_editor");
        $(element).on("focus.assist", "input[name]:not([type=checkbox]):not([type=radio]):not([type=button]):not([type=number]), textarea, .ace_editor", function() {
            var field = $(this);
            $(element).find(".assist_icon").remove();
            var container = $(field).parent();
            var position = container.css("position");
            if (position === "static") {
                container.css("position", "relative");
            }
            var display = container.css("display");
            if (display === "inline") {
                container.css("display", "block");
            }
            
            $(container).append('<i class="assist_icon la la-user-astronaut" title="'+get_peditor_msg('peditor.assit')+'"></i>');
            
            $(container).find(".assist_icon").off("click");
            $(container).find(".assist_icon").on("click", function(){
                PropertyAssistant.currentField = field[0];
                PropertyAssistant.currentCaretPosition = PropertyAssistant.doGetCaretPosition(field[0]);
                PropertyAssistant.showDialog();
            });
            
            $(field).off("focusout.assit");
            $(field).on("focusout.assit", function() {
                setTimeout(function(){
                    $(field).off("focusout.assit");
                    $(container).find(".assist_icon").remove();
                    
                    if (position === "static") {
                        container.css("position", position);
                    }
                    if (display === "inline") {
                        container.css("display", display);
                    }
                }, 500);
            });
        });
    },
    
    /*
     * Get the definition of the property assistant and create the dialog for editing
     */
    getDialog : function(callback) {
        if (PropertyAssistant.dialog === null) {
            PropertyAssistant.cachedAjax({
                type: "POST",
                url: PropertyAssistant.options.contextPath + '/web/property/json' + PropertyAssistant.options.appPath + '/getPropertyAssistants',
                dataType : "json",
                beforeSend: function (request) {
                   request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                },
                success: function(response) {
                    if (response) {
                        for (var i in response) {
                            var d = eval('['+ response[i].definition + ']')[0];
                            var t = response[i].type;
                            var newData = {};
                            newData[t] = d;
                            PropertyAssistant.data = $.extend(true, PropertyAssistant.data, newData);
                        }
                        
                        PropertyAssistant.data['HASH_VARIABLE'].optionField['hashEscapeType'] = {
                            "name" : "hashEscapeType",
                            "label" : get_peditor_msg('peditor.hashVariableEscapeType'),
                            "options" : [
                                {
                                    "value" : "?expression",
                                    "label" : get_peditor_msg('peditor.escape.expression'),
                                    "syntax" : [
                                        "expression"
                                    ]
                                },
                                {
                                    "value" : "?html",
                                    "label" : get_peditor_msg('peditor.escape.html'),
                                    "syntax" : [
                                        "html"
                                    ]
                                },
                                {
                                    "value" : "?java",
                                    "label" : get_peditor_msg('peditor.escape.java'),
                                    "syntax" : [
                                        "java"
                                    ]
                                },
                                {
                                    "value" : "?javascript",
                                    "label" : get_peditor_msg('peditor.escape.javascript'),
                                    "syntax" : [
                                        "javascript"
                                    ]
                                },
                                {
                                    "value" : "?json",
                                    "label" : get_peditor_msg('peditor.escape.json'),
                                    "syntax" : [
                                        "expression"
                                    ]
                                },
                                {
                                    "value" : "?img2base64",
                                    "label" : get_peditor_msg('peditor.escape.img2base64'),
                                    "syntax" : [
                                        "img2base64"
                                    ]
                                },
                                {
                                    "value" : "?noescape",
                                    "label" : get_peditor_msg('peditor.escape.noescape'),
                                    "syntax" : [
                                        "noescape"
                                    ]
                                },
                                {
                                    "value" : "?nl2br",
                                    "label" : get_peditor_msg('peditor.escape.nl2br'),
                                    "syntax" : [
                                        "nl2br"
                                    ]
                                },
                                {
                                    "value" : "?regex",
                                    "label" : get_peditor_msg('peditor.escape.regex'),
                                    "syntax" : [
                                        "regex"
                                    ]
                                },
                                {
                                    "value" : "?separator(SEPARATOR_CHARS)",
                                    "label" : get_peditor_msg('peditor.escape.separator'),
                                    "syntax" : [
                                        "separator(",
                                        {
                                            "placeholder" : "SEPARATOR_CHARS",
                                            "required" : true
                                        },
                                        ")"
                                    ]
                                },
                                {
                                    "value" : "?sql",
                                    "label" : get_peditor_msg('peditor.escape.sql'),
                                    "syntax" : [
                                        "sql"
                                    ]
                                },
                                {
                                    "value" : "?url",
                                    "label" : get_peditor_msg('peditor.escape.url'),
                                    "syntax" : [
                                        "url"
                                    ]
                                },
                                {
                                    "value" : "?xml",
                                    "label" : get_peditor_msg('peditor.escape.xml'),
                                    "syntax" : [
                                        "xml"
                                    ]
                                }
                            ],
                            "type" : "selectbox",
                            "showValues" : true
                        };
                        
                        PropertyAssistant.dialog = new Boxy(
                            '<div id="propertyAssistant"></div>',
                            {
                                title: '<i class="la la-user-astronaut"></i> '+get_peditor_msg('peditor.assit'),
                                closeable: true,
                                draggable: true,
                                show: false,
                                fixed: true,
                                modal: true,
                                afterHide: function() {
                                    $(PropertyAssistant.currentField).trigger("focus");
                                }
                            }
                        );
                
                        $("#propertyAssistant").html('<div class="typeSelectorWrapper"><select id="assistantTypeSelector"></select></div><div class="inputWrapper"><div class="inputResultWrapper"><quote class="inputResult" contenteditable="false" placeholder="'+get_peditor_msg('peditor.emptyValue')+'"></quote></div><div class="inputResultActions"><button class="insertBtn btn btn-primary btn-sm">'+get_peditor_msg('peditor.insert')+'</button> <button class="clearBtn btn btn-secondary btn-sm">'+get_peditor_msg('peditor.clear')+'</button></div></div><div class="inputOptionsWrapper"></div>');
                        
                        $("#propertyAssistant .inputResultWrapper").append('<div class="traveller"><i class="up las la-caret-up"></i><i class="down las la-caret-down"></i><a class="optional_btn show" title="'+get_peditor_msg('peditor.showOptionalField')+'"><i class="las la-toggle-off"></i></a><a class="optional_btn hide" title="'+get_peditor_msg('peditor.hideOptionalField')+'"><i class="las la-toggle-on"></i></a></div>');
                        
                        $("#propertyAssistant .traveller .up").off("click");
                        $("#propertyAssistant .traveller .up").on("click", function() {
                            PropertyAssistant.travelUp();
                        });
                        $("#propertyAssistant .traveller .down").off("click");
                        $("#propertyAssistant .traveller .down").on("click", function() {
                            PropertyAssistant.travelDown();
                        });
                        $("#propertyAssistant .optional_btn").off("click");
                        $("#propertyAssistant .optional_btn").on("click", function() {
                            PropertyAssistant.toogleOptionalField();
                        });
                        
                        var show = $.localStorage.getItem("propertyAssitant.showOptionalField");
                        if (show !== undefined && show === "true") {
                            $('#propertyAssistant .inputResultWrapper').addClass("showOptionalField");
                        }
                        
                        for (var key in PropertyAssistant.data) {
                            if (PropertyAssistant.data.hasOwnProperty(key)) {
                                $("#assistantTypeSelector").append('<option value="'+key+'">'+get_peditor_msg('peditor.'+key)+'</option>');
                            }
                        }
                        
                        $("#assistantTypeSelector").chosen({ width: "54%", placeholder_text: '' });
                        
                        $("#assistantTypeSelector").off("change");
                        $("#assistantTypeSelector").on("change", function(){
                            PropertyAssistant.changeType();
                        });
                        
                        $('#propertyAssistant .inputWrapper').off('click', '[contenteditable]');
                        $('#propertyAssistant .inputWrapper').on('click', '[contenteditable]',  function(e) {
                            $("#propertyAssistant .inputOptionsWrapper .subOptionField").hide();
                            $('#propertyAssistant .inputWrapper [contenteditable]').prop("contenteditable", false);
                            $('#propertyAssistant .inputResult [contenteditable]').removeClass("editing");
                            $(this).prop("contenteditable", true);
                            if ($(this).hasClass("chunk")) {
                                $(this).addClass("editing");
                            }
                            
                            $(this).on("focusout", function(){
                                $(this).prop("contenteditable", false);
                                $(this).off("focusout");
                            });
                            
                            $(this).trigger("focus");
                            
                            if (typeof window.getSelection != "undefined"
                                    && typeof document.createRange != "undefined") {
                                var range = document.createRange();
                                range.selectNodeContents($(this)[0]);
                                range.collapse(false);
                                if (window.getSelection) {
                                    var sel = window.getSelection();
                                    if (sel !== undefined && sel !== null) {
                                        sel.removeAllRanges();
                                        sel.addRange(range);
                                    }
                                }
                            } else if (typeof document.body.createTextRange != "undefined") {
                                var textRange = document.body.createTextRange();
                                textRange.moveToElementText($(this)[0]);
                                textRange.collapse(false);
                                textRange.select();
                            }
                            
                            PropertyAssistant.updateOptionField($(this));
                            
                            e.stopPropagation();
                            e.stopImmediatePropagation();
                        });
                        
                        $('#propertyAssistant .clearBtn').off("click");
                        $('#propertyAssistant .clearBtn').on("click", function() {
                            if ($('#propertyAssistant .inputResult').find(".editing").length > 0) {
                                if ($('#propertyAssistant .inputResult .editing').is("[placeholder]")) {
                                    $('#propertyAssistant .inputResult .editing').html("");
                                } else {
                                    $('#propertyAssistant .inputResult .editing').remove();
                                }
                            } else {
                                $('#propertyAssistant .inputResult').html("");
                            }
                            return false;
                        });
                        
                        $('#propertyAssistant .insertBtn').off("click");
                        $('#propertyAssistant .insertBtn').on("click", function() {
                            //check all require chunk are filled
                            if ($('#propertyAssistant [contenteditable][data-required]:empty').length > 0) {
                                return false;
                            }
                            PropertyAssistant.insertValue();
                            return false;
                        });
                    }
                }
            });
        } else {
            $("#assistantTypeSelector").val("HASH_VARIABLE");
            $("#assistantTypeSelector").trigger("change");
            $("#assistantTypeSelector").trigger("chosen:updated");
            if (callback) {
                callback(PropertyAssistant.dialog);
            }
        }
    },
    
    /*
     * Show the dialog
     */
    showDialog: function() {
        PropertyAssistant.getDialog(function(){
            var width = 800;
            var height = 700;
            
            width = UI.getPopUpWidth(width);
            height = UI.getPopUpHeight(height);
            
            $("#propertyAssistant").css("width", width + "px");
            $("#propertyAssistant").css("height", height + "px");
            $("#propertyAssistant").closest(".boxy-wrapper").attr("id", "propertyAssistantDialog");
            
            PropertyAssistant.dialog.show();
            PropertyAssistant.dialog.center('x');
            PropertyAssistant.dialog.center('y');
            
            $("#assistantTypeSelector").val("HASH_VARIABLE");
            $("#assistantTypeSelector").trigger("change");
            
            $(".boxy-modal-blackout").off("click.propertyAssistant");
            $(".boxy-modal-blackout").on("click.propertyAssistant", function(){
                PropertyAssistant.dialog.hide();
                $(".boxy-modal-blackout").off("click.propertyAssistant");
            });
        });
    },
    
    /*
     * Insert the value to field
     */
    insertValue: function() {
        var temp = $('#propertyAssistant .inputResult').clone();
        $(temp).find("[contenteditable]:empty").remove();
        $(temp).find("[contenteditable][data-prefix]").each(function(){
            $(this).prepend($(this).data("prefix"));
        });
        $(temp).find("[contenteditable][data-postfix]").each(function(){
            $(this).append($(this).data("postfix"));
        });
        
        var value = $(temp).text();
        if (value.trim() !== "") {
            if ($(PropertyAssistant.currentField).hasClass("ace_text-input") || $(PropertyAssistant.currentField).hasClass("ace_editor")) {
                var id = $(PropertyAssistant.currentField).closest(".ace_editor").attr("id");
                var codeeditor = ace.edit(id);
                var old = codeeditor.getValue();
                if (old !== "") {
                    value = " " + value;
                }
                codeeditor.session.insert(PropertyAssistant.currentCaretPosition, value);
            } else {
                var org = $(PropertyAssistant.currentField).val();
                var output = "";
                if (PropertyAssistant.currentCaretPosition !== null) {
                    output = [org.slice(0, PropertyAssistant.currentCaretPosition), value, org.slice(PropertyAssistant.currentCaretPosition)].join('');
                } else {
                    output = org;
                    if (output !== "") {
                        output += ' ';
                    }
                    output += value;
                }
                $(PropertyAssistant.currentField).val(output);
            }
        }
        
        PropertyAssistant.dialog.hide();
    },
    
    /*
     * Change the property value type
     */
    changeType: function() {
        var type = $("#assistantTypeSelector").val();
        $("#propertyAssistant .inputOptionsWrapper .inputOptionsFieldContainer").hide();
        PropertyAssistant.showOptionsField(type);
        
        $('#propertyAssistant .inputResult').html("");
    },
    
    /*
     * Check to show chunk option field 
     */
    updateOptionField: function(chunk){ 
        if ($(chunk).data("option-field")) {
            var fields = $(chunk).data("option-field").split(";");
            for (var i in fields) {
                PropertyAssistant.showOptionsField($("#assistantTypeSelector").val(), fields[i]);
            }
        }
    },
    
    /*
     * Show options field based on editing chunk
     */
    showOptionsField: function(type, name) {
        if (name === undefined) {
            if ($("#propertyAssistant .inputOptionsWrapper #"+type+"_input").length === 0) {
                PropertyAssistant.createOptionsField({
                    name : type,
                    label : get_peditor_msg('peditor.'+type),
                    options : PropertyAssistant.data[type].optionGroup,
                    showValues : true,
                    type : "selectbox"
                });
            } else {
                $("#propertyAssistant #"+type).val("");
                $("#propertyAssistant #"+type).trigger("chosen:updated");
                $("#propertyAssistant .inputOptionsWrapper #"+type+"_input").show();
            }
        } else {
            if ($("#propertyAssistant .inputOptionsWrapper #"+type+"_" + name + "_input").length === 0) {
                if (PropertyAssistant.data[type].optionField[name] !== undefined) {
                    PropertyAssistant.createOptionsField(PropertyAssistant.data[type].optionField[name], type);
                }
            } else {
                if (PropertyAssistant.data[type].optionField[name] !== undefined && PropertyAssistant.data[type].optionField[name].options_ajax) {
                    PropertyAssistant.renderAjaxOptions($("#propertyAssistant #"+type+"_" + name), PropertyAssistant.data[type].optionField[name]);
                }
                
                $("#propertyAssistant #"+type).val("");
                $("#propertyAssistant #"+type).trigger("chosen:updated");
                $("#propertyAssistant #"+type+"_" + name).val("");
                $("#propertyAssistant #"+type+"_" + name).trigger("chosen:updated");
                $("#propertyAssistant .inputOptionsWrapper #"+type+"_" + name + "_input").show();
            }
        }
    },
    
    /*
     * Create options field for chunk editing
     */
    createOptionsField : function(field, prefix) {
        var isSubOptionsField = "";
        if (prefix) {
            prefix = prefix + "_";
            isSubOptionsField = " subOptionField";
        } else {
            prefix = "";
        }
        
        var container = $('<div id="'+prefix+field.name+'_input" class="inputOptionsFieldContainer '+isSubOptionsField+'"><label>'+field.label+'</label><div class="inputOptionsField"></div></div>');
        
        if (field.type.toLowerCase() === "selectbox") {
            $(container).find('.inputOptionsField').append('<select id="'+prefix+field.name+'"><option><option></select>');
            
            if (field.options) {
                PropertyAssistant.renderOptions($(container).find('select'), field.options, field);
            } else if (field.options_ajax) {
                PropertyAssistant.renderAjaxOptions($(container).find('select'), field);
            }
            
            $(container).find('select').chosen({ width: "100%", placeholder_text: get_peditor_msg('peditor.selectOption') });
            
            if (field.showValues) {
                PropertyAssistant.showOptionValue($(container).find('select'));
            }
        } else if (field.type.toLowerCase() === "number") {
            $(container).find('.inputOptionsField').append('<input id="'+prefix+field.name+'" type="number" value=""/>');
        }
        
        $(container).find('#'+prefix+field.name).off('change');
        $(container).find('#'+prefix+field.name).on('change', function(){
            var value = $(container).find('#'+prefix+field.name).val();
            var option = null;
            
            if ($(container).find('#'+prefix+field.name).is("select")) {
                option = $(container).find('#'+prefix+field.name + ' option:selected');
            }
            
            if (value !== "") {
                PropertyAssistant.useValue(value, option);
            }
            
            $(container).find('#'+prefix+field.name).val("");
            $(container).find('#'+prefix+field.name).trigger("chosen:updated");
        });
        
        $("#propertyAssistant .inputOptionsWrapper").append(container);
    },
    
    /*
     * Render options for options field
     */
    renderOptions : function(select, options, field, level) {
        var prefix = "";
        if (level === undefined) {
            level = -1;
        }
        if (level > 0) {
            for (var i =0; i < level; i++) {
                prefix += "&nbsp;&nbsp;&nbsp;&nbsp;";
            }
        }
        if ($.isArray(options)) {
            for (var i in options) {
                var opt = options[i];
                var optionEl = $('<option></option>');
                optionEl.attr('value', opt.value);
                
                var label = opt.label;
                if (field.showValues) {
                    label = opt.value + ' ::: ' + label;
                }
                optionEl.text(prefix + label);
                
                if (opt.syntax) {
                    optionEl.data("syntax", opt.syntax);
                }
                
                $(select).append(optionEl);
            }
        } else {
            for (var key in options) {
                if (options.hasOwnProperty(key)) {
                    if (level === -1) {
                        var optGroup = $('<optgroup></optgroup>');
                        optGroup.attr("label", prefix + key);
                        $(select).append(optGroup);
                        var newLevel = level;
                        if (!$.isArray(options[key])) {
                            newLevel = 0;
                        }
                        PropertyAssistant.renderOptions(optGroup, options[key], field, newLevel+1);
                    } else { // use for render second level tree structure
                        $(select).append('<option disabled>'+prefix + key+'</option>');
                        PropertyAssistant.renderOptions(select, options[key], field, level+1);
                    }
                }
            }
        }
    },
    
    /*
     * Retrieve option by AJAX and render it
     */
    renderAjaxOptions : function(select, field) {
        var url = field.options_ajax;
        if (url.indexOf('[CONTEXT_PATH]') !== -1) {
            url = url.replace('[CONTEXT_PATH]', PropertyAssistant.options.contextPath);
        }
        if (url.indexOf('[APP_PATH]') !== -1) {
            url = url.replace('[APP_PATH]', PropertyAssistant.options.appPath);
        }
        if (field.options_ajax_on_change) {
            var temp = field.options_ajax_on_change.split(";");
            for (var s in temp) {
                var temp2 = temp[s].split(":");
                if (url.indexOf('?') !== -1) {
                    url += "&";
                } else {
                    url += "?";
                }

                if (temp2[1] !== undefined) {
                    url += temp2[1];
                } else {
                    url += temp2[0];
                }

                var targetValue = $('#propertyAssistant .inputResult .editing').parent().find('> [data-option-field="'+temp2[0]+'"]').text();
                url += "=" + encodeURIComponent(targetValue);
            }
        }
        if ($(select).data("url") && $(select).data("url") === url) {
            return;
        } else {
            $(select).html('<option></option>');
        }
        
        $(select).data("url", url);

        var method = "POST";
        if (field.options_ajax_method) {
            method = field.options_ajax_method;
        }
        
        //show loading when the option is not ready yet
        $(select).closest('.inputOptionsField').css({
            'pointer-event' : 'none',
            'opacity' : 0.3,
            'position' : 'relative'
        });
        $(select).closest('.inputOptionsField').append('<i class="fas fa-spinner fa-spin" style="color:#000; position:absolute; top:9px; left:15px;"></i>');

        PropertyAssistant.cachedAjax({
            type: method,
            url: url,
            dataType: "JSON",
            beforeSend: function (request) {
               request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            },
            success: function(options) {
                var tempOptions = options;
                if (field.options_ajax_mapping !== undefined) {
                    if (field.options_ajax_mapping.arrayObj !== undefined) {
                        tempOptions = PropertyEditor.Util.getValueFromObject(tempOptions, field.options_ajax_mapping.arrayObj);
                    }

                    var newOptions = [];
                    for (var o in tempOptions) {
                        if (field.options_ajax_mapping.value !== undefined && field.options_ajax_mapping.label !== undefined) {
                            newOptions.push({
                                value: PropertyEditor.Util.getValueFromObject(tempOptions[o], field.options_ajax_mapping.value),
                                label: PropertyEditor.Util.getValueFromObject(tempOptions[o], field.options_ajax_mapping.label)
                            });
                        } else {
                            newOptions.push(tempOptions[o]);
                        }
                    }
                    tempOptions = newOptions;
                }
                PropertyAssistant.renderOptions(select, tempOptions, field);
                
                //remove the loading icon
                $(select).closest('.inputOptionsField').css({
                    'pointer-event' : 'initial',
                    'opacity' : 1
                });
                $(select).closest('.inputOptionsField').find('.fa-spinner').remove();

                $(select).trigger("change");
                $(select).trigger("chosen:updated");
            }
        });
    },
    
    /*
     * Make the selectbox show the value and label together
     */
    showOptionValue : function(select) {
        $(select).off("chosen:showing_dropdown")
        $(select).on("chosen:showing_dropdown", function(evt, chosen) {
            PropertyAssistant.updateOptionLabel(chosen.chosen);
        });
        $(select).off("chosen:hiding_dropdown");
        $(select).on("chosen:hiding_dropdown", function(evt, chosen) {
            PropertyAssistant.updateOptionLabel(chosen.chosen);
        });
        $(select).off("chosen:ready");
        $(select).on("chosen:ready", function(evt) {
            PropertyAssistant.updateOptionLabel($(select).data("chosen"));
        });
        $(select).off("chosen:updated");
        $(select).on("chosen:updated", function(evt) {
            PropertyAssistant.updateOptionLabel($(select).data("chosen"));
        });
        $(select).off("change");
        $(select).on("change", function() {
            PropertyAssistant.updateOptionLabel($(select).data("chosen"));
        });
        setTimeout(function() {
            $($(select).data("chosen").container).find(".chosen-search input").off("keydown");
            $($(select).data("chosen").container).find(".chosen-search input").on("keydown", function() {
                setTimeout(function() { PropertyAssistant.updateOptionLabel($(select).data("chosen")); }, 5);
            });
        }, 1000);
        PropertyAssistant.updateOptionLabel($(select).data("chosen"));
    },
    
    /*
     * Use by showOptionValue to style & format the value & label
     */
    updateOptionLabel : function(chosen) {
        $(chosen.container).find(".chosen-results li, .chosen-single > span, .search-choice > span").each(function() {
            var html = $(this).html() + ' ';
            var regex = new RegExp("\\[(.*)<em>(.*)\\]", "g");
            html = html.replace(regex, "[$1$2]");
            regex = new RegExp("\\[(.*)</em>(.*)\\]", "g");
            html = html.replace(regex, "[$1$2]");
            
            if (/ ::: /.test(html)) {
                html = '<cite class="option_value">' + html.replace(' ::: ', '</cite> <span class="option_desc">') + '</span>';
            }
            $(this).html(html);
        });
    },
    
    /*
     * Use a value of an option field
     */
    useValue : function(value, option) {
        var result = $('#propertyAssistant .inputResult');
        
        if ($(result).find(".editing").length > 0) {
            result = $(result).find(".editing");
        }
        
        var nestedHash = false;
        if ($("#assistantTypeSelector").val() === "HASH_VARIABLE" && $(result).parent().hasClass("chunk")) {
            nestedHash = true;
        }
        
        var output = $('<span contenteditable="false" class="chunk"></span>');
        
        if (option && $(option).data("syntax")) {
            var syntax = $(option).data("syntax");
            for (var i in syntax) {
                if( Object.prototype.toString.call(syntax[i]) == '[object String]' ) {
                    $(output).append(syntax[i]);
                } else {
                    var editable = $('<span contenteditable="false" class="chunk" placeholder="'+syntax[i].placeholder+'" ></span>');
                    if (syntax[i].option) {
                        $(editable).attr("data-option-field", syntax[i].option);
                    }
                    if (syntax[i].required) {
                        $(editable).attr("data-required", "");
                    }
                    if (syntax[i].prefix) {
                        $(editable).attr("data-prefix", syntax[i].prefix);
                    }
                    if (syntax[i].postfix) {
                        $(editable).attr("data-postfix", syntax[i].postfix);
                        $(editable).attr("placeholder", syntax[i].placeholder+syntax[i].postfix);
                    }
                    if (syntax[i].multiple) {
                        $(editable).attr("data-multiple", syntax[i].multiple);
                    }
                    if (syntax[i].default) {
                        $(editable).text(syntax[i].default);
                    }
                    $(output).append(editable);
                }
            }
            
            var temp = $(output).html();
            if ($("#assistantTypeSelector").val() === "HASH_VARIABLE" && value.indexOf("#") === 0 && value.lastIndexOf("#") === value.length - 1) { //support escape syntax
                temp = temp.substring(0, temp.length - 1) + '<span contenteditable="false" class="chunk" data-prefix="?" placeholder="" data-option-field="hashEscapeType"></span>#';
                $(output).html(temp);
            }
            if (nestedHash && value.indexOf("#") === 0 && value.lastIndexOf("#") === value.length - 1) {
                temp = "{" + temp.substring(1, temp.length - 1) + "}";
                $(output).html(temp);
            }
        } else {
            value = UI.escapeHTML(value);
            if ($("#assistantTypeSelector").val() === "HASH_VARIABLE" && value.indexOf("#") === 0 && value.lastIndexOf("#") === value.length - 1) { //support escape syntax
                value = value.substring(0, value.length - 1) + '<span contenteditable="false" class="chunk" data-prefix="?" placeholder="" data-option-field="hashEscapeType"></span>#';
            }
            if (nestedHash && value.indexOf("#") === 0 && value.lastIndexOf("#") === value.length - 1) {
                value = "{" + value.substring(1, value.length - 1) + "}";
            }
            $(output).html(value);
        }
        
        if ($(result).hasClass("chunk")) {
            if ($(result).is('[data-multiple]')) {
                var newEditable = $('<span contenteditable="false" class="chunk"></span>');
                if ($(result).is('[data-option-field]')){
                    newEditable.attr('data-option-field', $(result).attr('data-option-field'));
                }
                $(newEditable).html(output.html());
                if ($(result).text() !== "") {
                    var prefix = $(result).attr("data-multiple");
                    $(newEditable).attr("data-prefix", prefix);
                }
                $(result).append(newEditable);
                
                $(result).removeClass("editing");
                $(newEditable).addClass("editing");
            } else {
                $(result).html(output.html());
            }
        } else {
            if ($(result).text() !== "") {
                $(result).append(" ");
            }
            $(result).append(output);
            $(output).addClass("editing");
        }
        PropertyAssistant.travelDown();
    },
    
    /*
     * Utility method to cache ajax call, this is for better performance when frequently retrieving ajax data
     */
    cachedAjax: function(ajaxObj) {
        var json = "";
        if (ajaxObj.data !== null && ajaxObj.data !== undefined) {
            json = JSON.encode(ajaxObj.data);
        }
        var key = (ajaxObj.type?ajaxObj.type:"") + "::" + ajaxObj.url + "::" + PropertyAssistant.hashCode(json);
        
        if (PropertyAssistant.cachedAjaxCalls[key] !== undefined) {
            //cache for 60sec
            if (((new Date().getTime()) - PropertyAssistant.cachedAjaxCalls[key].time) < 60000) {
                if (ajaxObj.success) {
                    ajaxObj.success(PropertyAssistant.cachedAjaxCalls[key].data);
                }
                return;
            } else {
                delete PropertyAssistant.cachedAjaxCalls[key];
            }
        }
        
        var orgSuccess = ajaxObj.success;
        ajaxObj.success = function(response) {
            PropertyAssistant.cachedAjaxCalls[key] = {
                time : (new Date().getTime()),
                data : response
            };

            if (orgSuccess) {
                orgSuccess(response);
            }
        };
        
        $.ajax(ajaxObj);
    },
    
    /*
     * Travelling to previous chunk
     */
    travelUp : function() {
        var result = $('#propertyAssistant .inputResult .editing');
        $("#propertyAssistant .inputOptionsWrapper .subOptionField").hide();
        
        if ($(result).length === 0) {
            PropertyAssistant.updateOptionField($('#propertyAssistant .inputResult'));
            return;
        }
        $(result).removeClass('editing');
        
        var prev = null;
        var current = $(result);
        
        //find previous visible chunk
        do {
            current = $(current).prev('.chunk');
            if ($(current).is(":visible")) {
                prev = current;
            }
        } while (prev === null && $(current).next('.chunk').length > 0);
        
        if (prev !== null && $(prev).length > 0) {
            //find inner visible chunk
            while ($(prev).find('> .chunk:visible').length > 0) {
                //set to last chunk
                prev = $(prev).find('> .chunk:visible').last();
            }
            
            $(prev).addClass("editing");
            PropertyAssistant.updateOptionField($(prev));
            return;
        }
        
        if ($(result).parent().closest('.chunk').length > 0) {
            $(result).parent().closest('.chunk').addClass("editing");
            PropertyAssistant.updateOptionField($(result).parent().closest('.chunk'));
        }
    },
    
    /*
     * Travelling to next chunk
     */
    travelDown : function() {
        var result = $('#propertyAssistant .inputResult .editing');
        $("#propertyAssistant .inputOptionsWrapper .subOptionField").hide();
        
        if ($(result).length === 0) {
            $('#propertyAssistant .inputResult > .chunk:first-child').addClass("editing");
            PropertyAssistant.updateOptionField($('#propertyAssistant .inputResult > .chunk:first-child'));
            return;
        }
        $(result).removeClass('editing');
        
        if ($(result).find('> .chunk:visible').length > 0) { //should not travel to optional hidden chunck
            $(result).find('> .chunk:visible').eq(0).addClass("editing");
            PropertyAssistant.updateOptionField($(result).find('> .chunk:visible').eq(0));
            return;
        }
        
        var next = null;
        
        var findNextVisibleChunk = function(current) {
            var temp = null;
            //find next visible chunk
            do {
                current = $(current).next('.chunk');
                if ($(current).is(":visible")) {
                    temp = current;
                }
            } while (temp === null && $(current).next('.chunk').length > 0);
            
            return temp;
        };
        next = findNextVisibleChunk($(result));
        
        if (next === null) { //find parent visible sibling
            var parent = $(result);
            do {
                parent = $(parent).parent('.chunk');
                next = findNextVisibleChunk($(parent));
            } while (next === null && $(parent).length > 0);
        }
        
        if (next !== null && $(next).length > 0) {
            //find inner visible chunk
            if ($(next).find('> .chunk:visible').length > 0) {
                //set to first chunk
                next = $(next).find('> .chunk:visible').first();
            }
            
            $(next).addClass("editing");
            PropertyAssistant.updateOptionField($(next));
            return;
        }
        
        $(result).addClass('editing');
        PropertyAssistant.updateOptionField($(result));
    },
    
    /*
     * Generate an unique hash code for a string
     */
    hashCode : function(s) {
        var h = 0, l = s.length, i = 0;
        if ( l > 0 )
          while (i < l)
            h = (h << 5) - h + s.charCodeAt(i++) | 0;
        return h;
    },
    
    /*
     * Get the caret position of a field
     */
    doGetCaretPosition : function(oField) {
        if ($(PropertyAssistant.currentField).hasClass("ace_text-input") || $(PropertyAssistant.currentField).hasClass("ace_editor")) {
            var id = $(PropertyAssistant.currentField).closest(".ace_editor").attr("id");
            var codeeditor = ace.edit(id);
            return codeeditor.getCursorPosition();
        } else {
            // Initialize
            var iCaretPos = 0;

            // IE Support
            if (document.selection) {
                // Set focus on the element
                oField.focus();
                // To get cursor position, get empty selection range
                var oSel = document.selection.createRange();
                // Move selection start to 0 position
                oSel.moveStart('character', -oField.value.length);
                // The caret position is selection length
                iCaretPos = oSel.text.length;
            } else if (oField.selectionStart || oField.selectionStart === '0') // Firefox support
                iCaretPos = oField.selectionStart;

            // Return results
            return (iCaretPos);
        }
    },
    
    /*
     * toogle to show/hide optional fields
     */
    toogleOptionalField : function() {
        var show = $.localStorage.getItem("propertyAssitant.showOptionalField");
        if (show !== undefined && show === "true") {
            show = false;
            $('#propertyAssistant .inputResultWrapper').removeClass("showOptionalField");
        } else {
            show = true;
            $('#propertyAssistant .inputResultWrapper').addClass("showOptionalField");
        }
        $.localStorage.setItem("propertyAssitant.showOptionalField", show);
    }
};

(function($) {
    $.fn.extend({
        propertyEditor: function(options) {
            var defaults = {
                appPath: '',
                contextPath: '',
                saveCallback: null,
                cancelCallback: null,
                validationFailedCallback: null,
                saveButtonLabel: get_peditor_msg('peditor.ok'),
                cancelButtonLabel: get_peditor_msg('peditor.cancel'),
                nextPageButtonLabel: get_peditor_msg('peditor.next'),
                previousPageButtonLabel: get_peditor_msg('peditor.prev'),
                showCancelButton: false,
                closeAfterSaved: true,
                showDescriptionAsToolTip: false,
                changeCheckIgnoreUndefined: false,
                mandatoryMessage: get_peditor_msg('peditor.mandatory'),
                skipValidation: false,
                isPopupDialog: false,
                autoSave: false,
                simpleMode: false,
                simpleModeOnChangeCallback: null
            };
            var o = $.extend(true, defaults, options);
            $.ajaxSetup({
                cache: false
            });

            var element = null;
            if (this.length > 1) {
                element = $(this[this.length - 1]);
            } else {
                element = $(this[0]);
            }
            
            return element.each(function() {
                var editor = new PropertyEditor.Model.Editor(this, o);
                
                PropertyAssistant.init($(element).find(".property-editor-container"), o);
                
                editor.render();
                $(element).data("editor", editor);
                
                //scroll to the field
                if (o.scrollToField !== null && o.scrollToField !== undefined && o.scrollToField !== "") {
                    var scrollAndExpandToField = function(fieldElement) {
                        if ($(element).find(fieldElement).length > 0) {
                            if ($(fieldElement).eq(0).closest(".property-page-show").hasClass("collapsed")) {
                                $(fieldElement).eq(0).closest(".property-page-show").removeClass("collapsed");
                            }

                            if ($(fieldElement).eq(0).closest('.property-editor-container').hasClass('single-page')) {
                                $(fieldElement).eq(0).closest('.property-editor-pages').animate({
                                    scrollTop: $(fieldElement).eq(0).offset().top + $(fieldElement).eq(0).closest('.property-editor-pages').scrollTop() - 200
                                }, 1);
                            } else {
                                var pageId = $(fieldElement).eq(0).closest(".property-page-show").attr("id");
                                editor.changePage(null, pageId);
                                
                                var scroll = $(fieldElement).eq(0).offset().top - 100 + $(fieldElement).eq(0).closest('.property-editor-property-container').scrollTop();
                                scroll = scroll - $(fieldElement).eq(0).closest('.property-editor-property-container').offset().top;
                                
                                $(fieldElement).eq(0).closest('.property-editor-property-container').animate({
                                    scrollTop: scroll 
                                }, 1);
                            }
                        }
                    };
                    
                    setTimeout(function(){
                        if (o.scrollToField.indexOf('.properties.') !== -1) { // it is element select field
                            var names = o.scrollToField.split('.');
                            var fieldSelector = "";
                            var altFieldSelector = "";
                            for (var i in names) {
                                if (names[i] !== "properties") { 
                                    if (names[i].indexOf('[') !== -1 && names[i].indexOf(']') !== -1 ) {
                                        //to handle multi element select
                                        var index = parseInt(names[i].substring(names[i].indexOf('[') + 1, names[i].indexOf(']')));
                                        var property = names[i].substring(0, names[i].indexOf('['));
                                        fieldSelector += '.property-page-show [property-name="'+property+'"] .repeater-rows-container .repeater-row:eq('+index+') ';
                                        altFieldSelector += "_" + property;
                                    } else {
                                        fieldSelector += '.property-page-show [property-name="'+names[i]+'"] ';
                                        altFieldSelector += "_" + names[i];
                                    }
                                    scrollAndExpandToField(fieldSelector + ', [name$="' + altFieldSelector + '"]');
                                }
                            }
                        } else {
                            scrollAndExpandToField('.property-page-show [property-name="'+o.scrollToField+'"]');
                        }
                    }, 1000);
                }
                
                return false;
            });
        }
    });
})(jQuery);
