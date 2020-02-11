PropertyEditor = {};
PropertyEditor.Model = {};
PropertyEditor.Type = {};
PropertyEditor.Validator = {};

PropertyEditor.SimpleMode = {
    render : function(container, options) {
        options["simpleMode"] = true;
        options["closeAfterSaved"] = false;
        $(container).propertyEditor(options);
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
                $("#"+id + " .property-editor-container").data("disable-hide", (hide !== true));
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
                if ($("#"+id + " .property-editor-container").data("disable-hide") !== true) {
                    PropertyEditor.Popup.hideDialog(id);
                } else {
                    PropertyEditor.Popup.cleanDialog(id);
                }
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
            return PropertyEditor.Popup.checkChangeAndHide(id, true, true);
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
        PropertyEditor.Popup.adjustSize(id, width, height);
        
        if (popupProp !== null && popupProp !== undefined && popupProp.x !== undefined&& popupProp.y !== undefined) {
            $("#"+id + " .property-editor-container").closest(".boxy-wrapper").css("left", popupProp.x + "px");
            $("#"+id + " .property-editor-container").closest(".boxy-wrapper").css("top", popupProp.y + "px");
        } else {
            PropertyEditor.Popup.propertyDialog[id].center('x');
            PropertyEditor.Popup.propertyDialog[id].center('y');
        }
    }
};

/* Utility Functions */
PropertyEditor.Util = {
    resources: {},
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
                } else if (o1[propName] === "" && o2[propName] === null) {
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
                            var targetValue = targetField.value;
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
    replaceContextPath: function(string, contextPath) {
        if (string === null) {
            return string;
        }
        var regX = /\[CONTEXT_PATH\]/g;
        return string.replace(regX, contextPath);
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
    handleDynamicOptionsField: function(page) {
        if (page !== null && page !== undefined) {
            var pageContainer = $(page.editor).find("#" + page.id);
            if ($(pageContainer).is("[data-control_field][data-control_value]")) {
                PropertyEditor.Util.bindDynamicOptionsEvent($(pageContainer), page);
            }
            $(pageContainer).find("[data-control_field][data-control_value]").each(function() {
                PropertyEditor.Util.bindDynamicOptionsEvent($(this), page);
            });
            $(pageContainer).find("[data-required_control_field][data-required_control_value]").each(function() {
                PropertyEditor.Util.bindDynamicRequiredEvent($(this), page);
            });
        }
    },
    bindDynamicOptionsEvent: function(element, page) {
        var control_field = element.data("control_field");
        var controlVal = String(element.data("control_value"));
        var isRegex = element.data("control_use_regex");
        
        var field = null;
        if (page.editorObject !== undefined) {
            var fields = page.editorObject.fields;
            if (page.parentId !== "" && page.parentId !== undefined) {
                var parentId = page.parentId.substring(1);
                if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                    fields = fields[parentId].fields;
                }
            }
            field = fields[control_field];
        } else if (page[control_field] !== undefined) {
            field = page[control_field];
        }
        if (field !== null && field !== undefined) {
            $(field.editor).on("change", "[name=\"" + field.id + "\"]", function() {
                var match = PropertyEditor.Util.dynamicOptionsCheckValue(field, controlVal, isRegex);
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
            $(field.editor).find("[name=\"" + field.id + "\"]").trigger("change");
        }
    },
    bindDynamicRequiredEvent: function(element, page) {
        var control_field = element.data("required_control_field");
        var controlVal = String(element.data("required_control_value"));
        var isRegex = element.data("required_control_use_regex");
        
        var field = null;
        if (page.editorObject !== undefined) {
            var fields = page.editorObject.fields;
            if (page.parentId !== "" && page.parentId !== undefined) {
                var parentId = page.parentId.substring(1);
                if (fields[parentId] !== undefined && fields[parentId].fields !== undefined) {
                    fields = fields[parentId].fields;
                }
            }
            field = fields[control_field];
        } else if (page[control_field] !== undefined) {
            field = page[control_field];
        }
        if (field !== null && field !== undefined) {
            $(field.editor).on("change", "[name=\"" + field.id + "\"]", function() {
                var match = PropertyEditor.Util.dynamicOptionsCheckValue(field, controlVal, isRegex);
                if (match) {
                    element.find(".property-required").show();
                } else {
                    element.find(".property-required").hide();
                }
            });
            $(field.editor).find("[name=\"" + field.id + "\"]").trigger("change");
        }
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
                $(field.editor).on("change", selector, function() {
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

                if (childField !== "") {
                    if ($.isArray(targetValue)) { //is grid
                        var values = [];
                        for (var j in targetValue) {
                            values.push(targetValue[j][childField]);
                        }
                        targetValue = values.join(";");
                    } else {
                        if (targetValue === null || targetValue === undefined || targetValue[childField] === null || targetValue[childField] === undefined) {
                            targetValue = "";
                        } else if ($.type(targetValue[childField]) === "string") {
                            targetValue = targetValue[childField];
                        } else {
                            targetValue = JSON.encode(targetValue[childField]);
                        }
                    }
                } else if (targetValue === null || targetValue === undefined) {
                    targetValue = "";
                }

                ajaxUrl += param + "=" + encodeURIComponent(targetValue);
            }
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
                    if (data !== undefined && data !== null) {
                        var options = $.parseJSON(data);
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
                }
            });
        }
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
            $(field.editor).on("change", selector, function() {
                PropertyEditor.Util.callLoadOptionsAjax(field, reference, ajax_url, on_change, mapping, method, extra);
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
    internalDynamicOptionsCheckValue: function(data, name, controlVal, isRegex) {
        var values = new Array();
        var value = data[name];

        if (value !== undefined && value !== null && value["className"] !== undefined) {
            values = [value["className"]];
        }else if (value !== undefined && value !== null) {
            values = value.split(";");
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
                        return true;
                    }
                } else {
                    if (result === values[i]) {
                        return true;
                    }
                }
            } else {
                if (values[i] === controlVal) {
                    return true;
                }
            }
        }

        return false;
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
            var width = $(field.editor).width() * 0.80;

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
                close: function(event, ui) {
                    $(object).dialog("destroy");
                    $(object).remove();
                    $(field).focus();
                }
            });
            $(object).dialog("open");

            $(object).on("click", ".app_resources li", function() {
                field.selectResource($(this).find(".name").text());
                $(object).dialog("close");
            });

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
        var errors = new Array();
        var data = this.getData();
        var deferreds = [];

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

        $.when.apply($, deferreds).then(function() {
            if (errors.length > 0) {
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
            $(this.editor).on("change", function() {
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
                    $thisObject.nextPage(false);
                } else if (currentOffset > pageLine) {
                    $thisObject.prevPage(false);
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
            if (single === "true") {
                this.toggleSinglePageDisplay(true);
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
        $.localStorage.setItem("propertyEditor.singlePageDisplay", single + "");
    },
    isSinglePageDisplay: function() {
        return $(this.editor).hasClass("single-page");
    },
    nextPage: function(scroll) {
        if ($(this.editor).find('.property-page-show.current').length > 0) {
            var current = $(this.editor).find('.property-page-show.current');
            var next = $(current).next();
            while (!$(next).hasClass("property-page-show") && $(next).hasClass("property-editor-page")) {
                next = $(next).next();
            }
            if ($(next).hasClass("property-editor-page")) {
                this.changePage($(current).attr('id'), $(next).attr('id'), scroll);
            }
        }
    },
    prevPage: function(scroll) {
        if ($(this.editor).find('.property-page-show.current').length > 0) {
            var current = $(this.editor).find('.property-page-show.current');
            var prev = $(current).prev();
            while (!$(prev).hasClass("property-page-show") && $(prev).hasClass("property-editor-page")) {
                prev = $(prev).prev();
            }
            if ($(prev).hasClass("property-editor-page")) {
                this.changePage($(current).attr('id'), $(prev).attr('id'), scroll);
            }
        }
    },
    changePage: function(currentPageId, pageId, scroll) {
        var thisObject = this;
        if (!this.isSinglePageDisplay() && currentPageId !== null && currentPageId !== undefined) {
            this.pages[currentPageId].validation(function(data) {
                thisObject.changePageCallback(pageId, scroll);
            }, thisObject.alertValidationErrors);
        } else {
            this.changePageCallback(pageId, scroll);
        }
    },
    changePageCallback: function(pageId, scroll) {
        $(this.editor).find('.property-page-hide, .property-type-hidden, .property-page-show').hide();
        $(this.editor).find('.property-page-show').removeClass("current");
        this.pages[pageId].show(scroll);
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
        return !PropertyEditor.Util.deepEquals(this, this.getData(), this.options.propertyValues);
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

        $.when.apply($, deferreds).then(function() {
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

        var showHide = "";
        if (this.properties.control_field !== undefined && this.properties.control_field !== null &&
            this.properties.control_value !== undefined && this.properties.control_value !== null) {
            showHide = 'data-control_field="' + this.properties.control_field + '" data-control_value="' + this.properties.control_value + '"';

            if (this.properties.control_use_regex !== undefined && this.properties.control_use_regex.toLowerCase() === "true") {
                showHide += ' data-control_use_regex="true"';
            } else {
                showHide += ' data-control_use_regex="false"';
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

        this.buttonPanel.initScripting();
        this.attachDescriptionEvent();
        this.attachHashVariableAssistant();
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
    },
    attachHashVariableAssistant: function() {
        $(this.editor).find("#" + this.id).hashVariableAssitant(this.options.contextPath);
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
        var page = this.page;
        var html = '<div class="property-editor-page-button-panel">';
        html += '<div class="page-button-navigation">';
        html += '<input type="button" class="page-button-prev" value="' + this.options.previousPageButtonLabel + '"/>';
        html += '<input type="button" class="page-button-next" value="' + this.options.nextPageButtonLabel + '"/>';
        html += '</div><div class="page-button-action">';
        if (page.properties.buttons !== undefined && page.properties.buttons !== null) {
            $.each(page.properties.buttons, function(i, button) {
                var showHide = "";

                if (button.control_field !== undefined && button.control_field !== null && button.control_value !== undefined && button.control_value !== null) {
                    showHide = 'data-control_field="' + button.control_field + '" data-control_value="' + button.control_value + '"';

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
                
                html += '<input id="' + page.id + '_' + button.name + '" type="button" class="page-button-custom" value="' + button.label + '" ' + buttonAttrs +' data-action="' + button.name + '" ' + showHide + ' />';
                if (button.addition_fields !== undefined && button.addition_fields !== null) {
                    html += '<div id="' + page.id + '_' + button.name + '_form" class="button_form" style="display:none;">';
                    html += '<div id="main-body-header" style="margin-bottom:15px;">' + button.label + '</div>';
                    $.each(button.addition_fields, function(i, property) {
                        html += page.renderProperty(i, button.name, property);
                    });
                    html += '</div>';
                }
            });
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
                
                $.when.apply($, deferreds).then(function() {
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
                                    text: $(button).val(),
                                    click: function() {
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
                                text: $(button).val(),
                                click: function() {
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
            showHide = 'data-control_field="' + this.properties.control_field + '" data-control_value="' + this.properties.control_value + '"';

            if (this.properties.control_use_regex !== undefined && this.properties.control_use_regex.toLowerCase() === "true") {
                showHide += ' data-control_use_regex="true"';
            } else {
                showHide += ' data-control_use_regex="false"';
            }
        }
        if (this.properties.required_validation_control_field !== undefined && this.properties.required_validation_control_field !== null &&
            this.properties.required_validation_control_value !== undefined && this.properties.required_validation_control_value !== null) {
            showHide += ' data-required_control_field="' + this.properties.required_validation_control_field + '" data-required_control_value="' + this.properties.required_validation_control_value + '"';

            if (this.properties.required_validation_control_use_regex !== undefined && this.properties.required_validation_control_use_regex.toLowerCase() === "true") {
                showHide += ' data-required_control_use_regex="true"';
            } else {
                showHide += ' data-required_control_use_regex="false"';
            }
        }
        
        var parentId = this.parentId;
        if (parentId !== "" && parentId !== undefined) {
            parentId = parentId.substring(1);
        }

        var html = '<div id="property_' + this.number + '" property-parentid="'+parentId+'" property-name="'+this.properties.name+'" class="property_container_' + this.id + ' property-editor-property property-type-' + this.properties.type.toLowerCase() + '" ' + showHide + '>';

        html += this.renderLabel();
        html += this.renderFieldWrapper();

        html += '<div style="clear:both;"></div></div>';

        return html;
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
                toolTipId = this.properties.name + (new Date()).getTime();
                toolTip = ' <i class="property-label-description fas fa-question-circle" data-tooltip-content="#'+toolTipId+'"></i>';
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
    remove: function() {}
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
        return '<input type="text" id="' + this.id + '" name="' + this.id + '"' + size + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '" disabled />';
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

PropertyEditor.Type.Number = function() {};
PropertyEditor.Type.Number.prototype = {
    shortname: "number",
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

        return '<input type="number" id="' + this.id + '" name="' + this.id + '"' + size + maxlength + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '"/>';
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
        return '<input class="jscolor" type="text" id="' + this.id + '" name="' + this.id + '"' + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value.toUpperCase()) + '"/>';
    },
    initScripting: function() {
        $("#" + this.id).data('colorMode', 'HEX').colorPicker({
            opacity: false, // disables opacity slider
            renderCallback: function($elm, toggled) {
                if ($elm.val() !== "" && $elm.val() !== undefined) {
                    $elm.val('#' + this.color.colors.HEX);
                }
            }
        }).off("focusin.tcp");
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
                $.each(thisObj.value.split(";"), function(i, v) {
                    if (v === option.value) {
                        checked = " checked";
                    }
                });
                html += '<span class="multiple_option"><label><input type="checkbox" id="' + thisObj.id + '" name="' + thisObj.id + '" value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + checked + '/>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</label></span>';
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

        if (this.properties.options !== undefined && this.properties.options !== null) {
            $.each(this.properties.options, function(i, option) {
                var checked = "";
                if (thisObj.value === option.value) {
                    checked = " checked";
                }
                html += '<span class="multiple_option"><label><input type="radio" id="' + thisObj.id + '" name="' + thisObj.id + '" value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + checked + '/><img style="max-width:100%;" title="' + PropertyEditor.Util.escapeHtmlTag(option.label) + '" src="' + PropertyEditor.Util.escapeHtmlTag(PropertyEditor.Util.replaceContextPath(option.image, thisObj.options.contextPath)) + '"/></label></span>';
            });
        }
        return html;
    },
    initScripting: function() {
        PropertyEditor.Util.supportHashField(this);
    },
    renderDefault: PropertyEditor.Type.CheckBox.prototype.renderDefault
};
PropertyEditor.Type.ImageRadio = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.ImageRadio.prototype);

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
        } else if (typeof AdvancedTools !== "undefined" && AdvancedTools !== null 
                && AdvancedTools.treeViewer !== undefined && AdvancedTools.treeViewer !== null 
                && AdvancedTools.treeViewer.builderType !== undefined) {
            for (var key in AdvancedTools.treeViewer.builderType) {
                if (lname.indexOf(key+"id") !== -1 || lname.indexOf(key+"defid") !== -1) {
                    builder = "cbuilder/" + key;
                }
            }
        }
        
        if (builder !== "") {
            html += " <a href=\"\" target=\"_blank\" class=\"openbuilder\" data-type=\""+builder+"\" style=\"display:none;\"><i class=\"fas fa-external-link-alt\"></i></i></a>";
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
                            html = html.replace(regex, "<span style=\"background:$1;width:10px;height:10px;display:inline-block;margin:0 2px;\"></span>");
                        }
                    }
                    $(this).html(html);
                });
            }
            $("#" + this.id).on("chosen:showing_dropdown", function(evt, chosen) {
                updateLabel(chosen.chosen);
            });
            $("#" + this.id).on("chosen:hiding_dropdown", function(evt, chosen) {
                updateLabel(chosen.chosen);
            });
            $("#" + this.id).on("chosen:ready", function(evt) {
                updateLabel($("#" + field.id).data("chosen"));
            });
            $("#" + this.id).on("chosen:updated", function(evt) {
                updateLabel($("#" + field.id).data("chosen"));
            });
            $("#" + this.id).on("change", function() {
                updateLabel($("#" + field.id).data("chosen"));
            });
            setTimeout(function() {
                $($("#" + field.id).data("chosen").container).find(".chosen-search input").on("keydown", function() {
                    setTimeout(function() { updateLabel($("#" + field.id).data("chosen")); }, 5);
                });
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
                        url += "?processId=" + value;
                    } else {
                        url += "/" + value;
                    }
                    
                    $("#" + field.id + "_input a.openbuilder").attr("href", url);
                    $("#" + field.id + "_input a.openbuilder").show();
                } else {
                    $("#" + field.id + "_input a.openbuilder").hide();
                }
            };
            $("#" + field.id).on("change", function() {
                updateLink();
            });
            updateLink();
        }

        PropertyEditor.Util.supportHashField(this);
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
        var html = '<table id="' + this.id + '"><tr class="grid_header">';
        //render header
        $.each(this.properties.columns, function(i, column) {
            var required = "";
            if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
            }
            html += '<th><span>' + column.label + '</span>' + required + '</th>';
        });
        html += '<th class="property-type-grid-action-column"></th></tr>';

        //render model
        html += '<tr class="grid_model" style="display:none">';
        $.each(this.properties.columns, function(i, column) {
            html += '<td><span>';

            PropertyEditor.Util.retrieveOptionsFromCallback(thisObj, column, column.key);

            if (column.type === "truefalse") {
                column.true_value = (column.true_value !== undefined) ? column.true_value : 'true';
                html += '<input name="' + column.key + '" type="checkbox" value="' + column.true_value + '"/>';
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
                $.each(thisObj.properties.columns, function(i, column) {
                    var columnValue = "";
                    if (row[column.key] !== undefined) {
                        columnValue = row[column.key];
                    }

                    html += '<td><span>';
                    if (column.type === "truefalse") {
                        var checked = "";
                        if (columnValue === column.true_value) {
                            checked = "checked";
                        }
                        html += '<input name="' + column.key + '" type="checkbox" ' + checked + ' value="' + column.true_value + '"/>';
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

        //add
        $(table).next('a.property-type-grid-action-add').click(function() {
            grid.gridActionAdd(this);
            return false;
        });

        //delete
        $(table).find('a.property-type-grid-action-delete').click(function() {
            grid.gridActionDelete(this);
            table.trigger("change");
            return false;
        });

        //move up
        $(table).find('a.property-type-grid-action-moveup').click(function() {
            grid.gridActionMoveUp(this);
            table.trigger("change");
            return false;
        });

        //move down
        $(table).find('a.property-type-grid-action-movedown').click(function() {
            grid.gridActionMoveDown(this);
            table.trigger("change");
            return false;
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
        var html = '<table id="' + this.id + '"><tr class="grid_header">';
        //render header
        $.each(this.properties.columns, function(i, column) {
            var required = "";
            if (column.required !== undefined && column.required.toLowerCase() === 'true') {
                required = ' <span class="property-required">' + get_peditor_msg('peditor.mandatory.symbol') + '</span>';
            }
            html += '<th><span>' + column.label + '</span>' + required + '</th>';
        });
        html += '<th class="property-type-grid-action-column"></th></tr>';

        //render model
        html += '<tr class="grid_model" style="display:none">';
        $.each(this.properties.columns, function(i, column) {
            html += '<td><span>';

            PropertyEditor.Util.retrieveOptionsFromCallback(thisObj, column, column.key);

            if (column.type === "truefalse") {
                column.true_value = (column.true_value !== undefined) ? column.true_value : 'true';
                html += '<input name="' + column.key + '" type="checkbox" value="' + column.true_value + '"/>';
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
                $.each(thisObj.properties.columns, function(i, column) {
                    var columnValue = "";
                    if (row[column.key] !== undefined) {
                        columnValue = row[column.key];
                    }

                    html += '<td><span>';
                    if (column.type === "truefalse") {
                        var checked = "";
                        if ((columnValue === column.true_value)) {
                            checked = "checked";
                        }
                        html += '<input name="' + column.key + '" type="checkbox" ' + checked + ' value="' + column.true_value + '"/>';
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
        return html;
    },
    renderDefault: function() {
        var thisObj = this;
        var defaultValueText = '';

        var defaultValues = new Array();
        if (thisObj.options.defaultPropertyValues !== null && thisObj.options.defaultPropertyValues !== undefined) {
            $.each(thisObj.properties.columns, function(i, column) {
                var temp = thisObj.options.defaultPropertyValues[column.key];
                if (temp !== undefined) {
                    var temp_arr = temp.split(";");

                    $.each(temp_arr, function(i, row) {
                        if (defaultValues[i] === null) {
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
    initScripting: PropertyEditor.Type.Grid.prototype.initScripting,
    gridActionAdd: PropertyEditor.Type.Grid.prototype.gridActionAdd,
    gridActionDelete: PropertyEditor.Type.Grid.prototype.gridActionDelete,
    gridActionMoveUp: PropertyEditor.Type.Grid.prototype.gridActionMoveUp,
    gridActionMoveDown: PropertyEditor.Type.Grid.prototype.gridActionMoveDown,
    gridDisabledMoveAction: PropertyEditor.Type.Grid.prototype.gridDisabledMoveAction,
    pageShown: PropertyEditor.Type.Grid.prototype.pageShown,
    handleAjaxOptions: PropertyEditor.Type.Grid.prototype.handleAjaxOptions,
    updateSource: PropertyEditor.Type.Grid.prototype.updateSource
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
        var html = '<table id="' + this.id + '"><tr class="grid_header">';
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

                        html += '<td><span>';
                        if (column.type === "truefalse") {
                            var checked = "";
                            column.true_value = (column.true_value !== undefined) ? column.true_value : 'true';
                            if (columnValue === column.true_value) {
                                checked = "checked";
                            }
                            html += '<input name="' + column.key + '" type="checkbox" ' + checked + ' value="' + column.true_value + '"/>';
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

        $.each(grid.properties.columns, function(i, column) {
            if ((column.options_ajax !== undefined && column.options_ajax !== null) || (column.options_callback_on_change !== undefined && column.options_callback_on_change !== null)) {
                PropertyEditor.Util.handleOptionsField(grid, column.key, column.options_ajax, column.options_ajax_on_change, column.options_ajax_mapping, column.options_ajax_method, column.options_extra);
            }
        });
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

            $("#" + thisObj.id + "_input .repeater-row").each(function(i){
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
                $("#" + field.id + "_input .repeater-row").each(function(){
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
        
        $("#" + thisObj.id + "_input").on("click", ".addrow", function(){
            thisObj.addRow(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deleterow", function(){
            thisObj.deleteRow(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", "a.expand", function(){
            thisObj.expandRow(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", "a.compress", function(){
            thisObj.compressRow(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", "a.expandAll", function(){
            $("#" + thisObj.id + "_input .repeater-row a.expand").each(function() {
                thisObj.expandRow(this);
            });
        });
        
        $("#" + thisObj.id + "_input").on("click", "a.collapseAll", function(){
            $("#" + thisObj.id + "_input .repeater-row a.compress").each(function() {
                thisObj.compressRow(this);
            });
        });
        
        $("#" + thisObj.id + "_input .repeater-rows-container").sortable({
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
        var cId = thisObj.id + "-" + ((new Date()).getTime());
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
            $("#" + thisObj.id + "_input").find(".repeater-rows-container").append(row);
        }
        
        if (fields !== null && fields !== undefined) {
            $.each(fields, function(i, property) {
                var type = property.propertyEditorObject;
                type.initScripting();
                type.initDefaultScripting();
            });
        }
        
        $(row.editor).find(".property-label-description").each(function(){
            if (!$(this).hasClass("tooltipstered")) {
                $(this).tooltipster({
                    contentCloning: false,
                    side : 'right',
                    interactive : true
                });
            }
        });
        
        $(row).find("[data-control_field][data-control_value]").each(function() {
            PropertyEditor.Util.bindDynamicOptionsEvent($(this), fieldsHolder);
        });
        $(row).find("[data-required_control_field][data-required_control_value]").each(function() {
            PropertyEditor.Util.bindDynamicRequiredEvent($(this), fieldsHolder);
        });
        
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
        $(button).closest(".repeater-row").remove();
        thisObj.updateBtn();
    },
    updateBtn : function() {
        var thisObj = this;
        
        if ($("#" + thisObj.id + "_input").find(".repeater-row.expand").length > 0) {
            $("#" + thisObj.id + "_input a.collapseAll").show();
            $("#" + thisObj.id + "_input a.expandAll").hide();
        } else {
            $("#" + thisObj.id + "_input a.collapseAll").hide();
            $("#" + thisObj.id + "_input a.expandAll").show();
        }
    }
};
PropertyEditor.Type.Repeater = PropertyEditor.Util.inherit(PropertyEditor.Model.Type, PropertyEditor.Type.Repeater.prototype);

PropertyEditor.Type.HtmlEditor = function() {};
PropertyEditor.Type.HtmlEditor.prototype = {
    shortname: "htmleditor",
    getData: function(useDefault) {
        var data = new Object();
        var value = "";
        if ($('[name=' + this.id + ']:not(.hidden)').length > 0) {
            value = tinyMCE.editors[$('[name=' + this.id + ']:not(.hidden)').attr('id')].getContent();
        }
        if (value === undefined || value === null || value === "") {
            if (useDefault !== undefined && useDefault &&
                this.defaultValue !== undefined && this.defaultValue !== null) {
                value = this.defaultValue;
            }
        }
        data[this.properties.name] = value;
        return data;
    },
    renderField: function() {
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

        tinymce.init({
            selector: '#' + this.id,
            height: height,
            plugins: [
                'advlist autolink lists link image charmap print preview hr anchor pagebreak',
                'searchreplace wordcount visualblocks visualchars code fullscreen',
                'insertdatetime media nonbreaking table contextmenu directionality',
                'emoticons paste textcolor colorpicker textpattern imagetools codesample toc'
            ],
            toolbar1: 'undo redo | insert | styleselect fontsizeselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image media table codesample | forecolor backcolor emoticons | print preview',
            menubar: 'edit insert view format table tools',
            image_advtab: true,
            relative_urls: false,
            convert_urls: false,
            valid_elements: '*[*]',
            setup: function(editor) {
                editor.on('focus', function(e) {
                    $(thisObj.editor).find(".property-description").hide();
                    var property = $("#" + e.target.id).parentsUntil(".property-editor-property-container", ".property-editor-property");
                    $(property).find(".property-description").show();
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
        if (this.value === null) {
            this.value = "";
        }
        this.codeeditor = ace.edit(this.id);
        this.codeeditor.setValue(this.value);
        this.codeeditor.getSession().setTabSize(4);
        if (this.properties.theme !== undefined || this.properties.theme !== "") {
            this.properties.theme = "textmate";
        }
        this.codeeditor.setTheme("ace/theme/" + this.properties.theme);
        if (this.properties.mode !== undefined || this.properties.mode !== "") {
            this.codeeditor.getSession().setMode("ace/mode/" + this.properties.mode);
        }
        this.codeeditor.setAutoScrollEditorIntoView(true);
        this.codeeditor.setOption("maxLines", 1000000); //unlimited, to fix the height issue
        this.codeeditor.setOption("minLines", 10);
        this.codeeditor.resize();
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

        if (this.isDataReady) {
            var element = new Object();
            element['className'] = $('[name=' + this.id + ']:not(.hidden)').val();
            element['properties'] = new Object();

            if (this.pageOptions.propertiesDefinition !== undefined && this.pageOptions.propertiesDefinition !== null) {
                $.each(this.pageOptions.propertiesDefinition, function(i, page) {
                    var p = page.propertyEditorObject;
                    element['properties'] = $.extend(element['properties'], p.getData());
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
        var thisObj = this;
        var field = $("#" + this.id);
        var currentPage = $(this.editor).find("#" + this.page.id);
        while ($(currentPage).next().data("page") === this.page.id) {
            currentPage = $(currentPage).next();
        }
        $(currentPage).after("<div class=\"anchor property-editor-page\" data-page=\""+this.page.id+"\" anchorField=\""+this.id+"\" style=\"display:none\"></div>");

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
    renderPages: function() {
        var thisObj = this;
        var field = $("#" + this.id);
        var value = $(field).filter(":not(.hidden)").val();
        var currentPage = $(this.editor).find("#" + this.page.id);
        var anchor = $(this.editor).find(".anchor[anchorField=\"" + this.id + "\"]");

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
        if ($(this.editor).find('.property-editor-page[elementid=' + this.id + ']:first').attr('elementvalue') !== value) {
            this.removePages();
            thisObj.editorObject.refresh();
        }

        //if properties page not found, render it now
        if ($(this.editor).find('.property-editor-page[elementid=' + this.id + ']').length === 0 && value !== null) {
            var deferreds = [];

            PropertyEditor.Util.prevAjaxCalls = {};
            PropertyEditor.Util.showAjaxLoading(thisObj.editor, thisObj, "CONTAINER");

            deferreds.push(this.getElementProperties(value));
            deferreds.push(this.getElementDefaultProperties(value));
            $.when.apply($, deferreds).then(function() {
                if (thisObj.pageOptions.propertiesDefinition !== undefined && thisObj.pageOptions.propertiesDefinition !== null) {
                    var parentId = thisObj.properties.name;
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
                }
                thisObj.editorObject.refresh();
                PropertyEditor.Util.removeAjaxLoading(thisObj.editor, thisObj, "CONTAINER");
            });
        }
    },
    getElementProperties: function(value) {
        var thisObj = this;
        var d = $.Deferred();

        $.ajax({
            url: PropertyEditor.Util.replaceContextPath(this.properties.url, this.options.contextPath),
            data: "value=" + encodeURIComponent(value),
            dataType: "text",
            success: function(response) {
                if (response !== null && response !== undefined && response !== "") {
                    var data = eval(response);
                    thisObj.pageOptions.propertiesDefinition = data;
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
            $.ajax({
                url: PropertyEditor.Util.replaceContextPath(this.properties.default_property_values_url, this.options.contextPath),
                data: "value=" + encodeURIComponent(value),
                dataType: "text",
                success: function(response) {
                    if (response !== null && response !== undefined && response !== "") {
                        var data = $.parseJSON(response);
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
        if (this.pageOptions.propertiesDefinition !== undefined && this.pageOptions.propertiesDefinition !== null) {
            $.each(this.pageOptions.propertiesDefinition, function(i, page) {
                var p = page.propertyEditorObject;
                p.remove();
            });
        }
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

            $("#" + thisObj.id + "_input .repeater-row").each(function(i){
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
            $("#" + this.id + "_input .repeater-row").each(function(){
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
        var propertiesDefinition = $(row).data("propertiesDefinition");

        if (this.isDataReady) {
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
        
        var html = '<div name="'+thisObj.id+'"><div class="repeater-rows-container elementmultiselect"></div><div style="text-align:right; margin-bottom: 10px;"><a class="pebutton addrow"><i class="fas fa-plus-circle"></i> '+get_peditor_msg('peditor.addRow')+'</a></div></div>';
        
        return html;
    },
    initScripting : function() {
        var thisObj = this;
        thisObj.properties.propertiesDefinition = {};
        thisObj.properties.defaultPropertyValues = {};
        var currentPage = $(thisObj.editor).find("#" + thisObj.page.id);
        while ($(currentPage).next().data("page") === this.page.id) {
            currentPage = $(currentPage).next();
        }
        $(currentPage).after("<div class=\"anchor property-editor-page\" data-page=\""+thisObj.page.id+"\" anchorField=\""+thisObj.id+"\" style=\"display:none\"></div>");
        
        thisObj.loadValues();
        
        $("#" + thisObj.id + "_input").on("click", ".addrow", function(){
            thisObj.addRow(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deleterow", function(){
            thisObj.deleteRow(this);
        });
        
        $("#" + thisObj.id + "_input .repeater-rows-container").sortable({
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
    loadValues : function() {
        var thisObj = this;
        
        if (!((typeof thisObj.value) === "undefined") && thisObj.value !== null && thisObj.value.length > 0) {
            $.each(thisObj.value, function(i, v) {
                thisObj.addRow(null, v);
            });
        } else {
            thisObj.addRow(null);
        }
        if (this.properties.options !== null && !((typeof this.properties.options) === "undefined")) {
            thisObj.handleAjaxOptions(this.properties.options);
        }
    },
    addRow : function(before, value) {
        var thisObj = this;
        
        var row = $('<div class="repeater-row property-editor-property" style="margin-bottom:0px;"><div class="actions expand-compress property-label-container"><div class="property-label" style="display:none"></div><div class="num"></div></div><div class="actions sort"><i class="fas fa-arrows-alt"></i></div><div class="inputs"><div class="inputs-container"></div></div><div class="actions rowbuttons"><a class="addrow"><i class="fas fa-plus-circle"></i></a><a class="deleterow"><i class="fas fa-trash"></i></a></div></div>');
        
        var cId = thisObj.id + "-" + ((new Date()).getTime());
        
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
        $(row).data("element", value);
        
        if (before !== null && !((typeof before) === "undefined") && !$(before).hasClass("pebutton")) {
            $(before).closest(".repeater-row").before(row);
            
            var bid = $(before).find("select").attr("id");
            var beforeAnchor = $(thisObj.editor).find(".anchor[anchorField=\""+bid+"\"]");
            $(beforeAnchor).before("<div class=\"anchor property-editor-page\" data-page=\""+thisObj.page.id+"\" anchorField=\""+cId+"\" style=\"display:none\"></div>");
        } else {
            $("#" + thisObj.id + "_input").find(".repeater-rows-container").append(row);
            
            var beforeAnchor = $(thisObj.editor).find(".anchor[anchorField=\""+thisObj.id+"\"]");
            $(beforeAnchor).before("<div class=\"anchor property-editor-page\" data-page=\""+thisObj.page.id+"\" anchorField=\""+cId+"\" style=\"display:none\"></div>");
        }
        
        var field = $(row).find("#"+cId);
        
        if (UI.rtl) {
            $(field).addClass("chosen-rtl");
        }
        $(field).chosen({ width: "54%", placeholder_text: " " });

        if (!$(field).hasClass("hidden") && !((typeof $(field).val()) === "undefined") && $(field).val() !== null) {
            thisObj.renderPages($(field));
        }

        $(field).change(function() {
            thisObj.renderPages($(field));
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
        var movePages = $(".anchor[anchorfield=\""+fieldId+"\"], .property-editor-page[elementid=\""+fieldId+"\"]");
        $(".anchor[anchorfield=\""+nextRowId+"\"]").before(movePages);
        
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
                    if (value === option.value) {
                        selected = " selected";
                    }
                    html += '<option value="' + PropertyEditor.Util.escapeHtmlTag(option.value) + '"' + selected + '>' + PropertyEditor.Util.escapeHtmlTag(option.label) + '</option>';
                });
                $(this).html(html);
                $(this).trigger("change");
                $(this).trigger("chosen:updated");
            });
        }
    },
    renderPages: function(field) {
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
        if ($(this.editor).find('.property-editor-page[elementid=' + id + ']:first').attr('elementvalue') !== value) {
            this.removePages(field);
            thisObj.editorObject.refresh();
        }

        //if properties page not found, render it now
        if ($(this.editor).find('.property-editor-page[elementid=' + id + ']').length === 0  && value !== null) {
            var deferreds = [];

            PropertyEditor.Util.prevAjaxCalls = {};
            PropertyEditor.Util.showAjaxLoading(thisObj.editor, thisObj, "CONTAINER");

            deferreds.push(thisObj.getElementProperties(row, value));
            deferreds.push(thisObj.getElementDefaultProperties(value));
            $.when.apply($, deferreds).then(function() {
                if (!((typeof $(row).data("propertiesDefinition")) === "undefined") && $(row).data("propertiesDefinition") !== null) {
                    var parentId = thisObj.properties.name;
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
                }
                thisObj.editorObject.refresh();
                PropertyEditor.Util.removeAjaxLoading(thisObj.editor, thisObj, "CONTAINER");
            });
        }
    },
    getElementProperties: function(row, value) {
        var thisObj = this;
        var d = $.Deferred();

        $.ajax({
            url: PropertyEditor.Util.replaceContextPath(this.properties.url, this.options.contextPath),
            data: "value=" + encodeURIComponent(value),
            dataType: "text",
            success: function(response) {
                if (response !== null && !((typeof response) === "undefined") && response !== "") {
                    var data = eval(response);
                    $(row).data("propertiesDefinition", data);
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
            if (!((typeof thisObj.properties.defaultPropertyValues[value]) === "undefined")) {
                $.ajax({
                    url: PropertyEditor.Util.replaceContextPath(this.properties.default_property_values_url, this.options.contextPath),
                    data: "value=" + encodeURIComponent(value),
                    dataType: "text",
                    success: function(response) {
                        if (response !== null && !((typeof response) === "undefined") && response !== "") {
                            var data = $.parseJSON(response);
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
        var propertiesDefinition = $(row).data("propertiesDefinition")
        if (!((typeof propertiesDefinition) === "undefined") && propertiesDefinition !== null) {
            $.each(propertiesDefinition, function(i, page) {
                var p = page.propertyEditorObject;
                p.remove();
            });
        }
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
        var i = 1;
        $("#" + this.id + "_input .property-label").each(function(){
            $(this).text(thisObj.properties.label + " " + i);
            $(this).next().text(i);
            i++;
        });
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
        $("#" + this.id).autocomplete({
            source: thisObj.source,
            minLength: 0,
            open: function() {
                $(this).autocomplete('widget').css('z-index', 99999);
                return false;
            }
        });
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

        return '<input type="text" class="image" id="' + this.id + '" name="' + this.id + '"' + size + maxlength + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '"/> <a class="choosefile btn button small">' + get_peditor_msg('peditor.chooseFile') + '</a> <a class="clearfile btn button small">' + get_peditor_msg('peditor.clear') + '</a>';
    },
    initScripting: function() {
        var thisObj = this;
        $("#" + this.id).parent().find(".clearfile").on("click", function() {
            $("#" + thisObj.id).val("").trigger("focus").trigger("change");
        });

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

        return '<input type="text" class="image" id="' + this.id + '" name="' + this.id + '"' + size + maxlength + ' value="' + PropertyEditor.Util.escapeHtmlTag(this.value) + '"/><a class="choosefile btn button small">' + get_peditor_msg('peditor.chooseImage') + '</a><div class="image-placeholder" style="' + style + '"><a class="image-remove"><i class="fas fa-times"></i></a></div>';
    },
    initScripting: function() {
        var thisObj = this;
        $("#" + this.id).on("change", function() {
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

        $("#" + this.id).parent().find(".image-remove").on("click", function() {
            $("#" + thisObj.id).val("").trigger("focus").trigger("change");
        });

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
                editor.render();
                $(element).data("editor", editor);
                return false;
            });
        },
        hashVariableAssitant: function(contextPath) {
            var container = this;

            var showHashVariableAssit = function(field, caret, syntax) {
                var html = "<div class=\"property_editor_hashassit\">";
                html += "<input type=\"text\" id=\"property_editor_hashassit_input\" class=\"hashassit_input\" style=\"width:90%\"/>";
                html += "</div>";

                var object = $(html);
                $(object).dialog({
                    autoOpen: false,
                    modal: true,
                    height: 85,
                    closeText: '',
                    close: function(event, ui) {
                        $(object).dialog("destroy");
                        $(object).remove();
                        $(field).focus();
                    }
                });

                $.ajax({
                    url: contextPath + '/web/json/hash/options',
                    dataType: "text",
                    success: function(data) {
                        if (data !== undefined && data !== null) {
                            var options = $.parseJSON(data);
                            $(object).find(".hashassit_input").autocomplete({
                                source: options,
                                minLength: 0,
                                open: function() {
                                    $(this).autocomplete('widget').css('z-index', 99999);
                                    return false;
                                }
                            }).focus(function() {
                                $(this).data("uiAutocomplete").search($(this).val());
                            }).keydown(function(e) {
                                var autocomplete = $(this).autocomplete("widget");
                                if (e.which === 13 && $(autocomplete).is(":hidden")) {
                                    var text = $(this).val();
                                    if (text.length > 0) {
                                        if (syntax === "#") {
                                            text = "#" + text + "#";
                                        } else {
                                            text = "{" + text + "}";
                                        }
                                        if ($(field).hasClass("ace_text-input")) {
                                            var id = $(field).closest(".ace_editor").attr("id");
                                            var codeeditor = ace.edit(id);
                                            codeeditor.insert(text);
                                        } else {
                                            var org = $(field).val();
                                            var output = [org.slice(0, caret), text, org.slice(caret)].join('');
                                            $(field).val(output);
                                        }
                                    }
                                    $(object).dialog("close");
                                }
                            });
                            $(object).dialog("open");
                            $(object).find(".hashassit_input").val("").focus();
                        } else {
                            $(object).dialog("destroy");
                            $(object).remove();
                            $(field).focus();
                        }
                    }
                });
            };

            var doGetCaretPosition = function(oField) {
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
            };

            var keys = {};
            $(container).keydown(function(e) {
                if (!(e.ctrlKey && e.altKey)) {
                    keys[e.which] = true;
                    if ((keys[17] === true && keys[16] === true && keys[18] !== true) && (keys[51] === true || keys[219] === true)) {
                        var element = $(container).find(":focus");
                        showHashVariableAssit(element, doGetCaretPosition(element[0]), (keys[51] === true) ? "#" : "{");
                        keys = {};
                    }
                }
            }).keyup(function(e) {
                delete keys[e.which];
            });
        }
    });
})(jQuery);
