FormBuilder = {

    elementPropertyDefinitions: new Object(), // map of property dialog definitions for each element class
    elementTemplates: new Object(), // map of HTML templates for each element class
    contextPath: "/jw",
    elementPreviewUrl: "/web/fbuilder/element/preview",
    formPreviewUrl: "/web/fbuilder/form/preview/",
    originalJson: "",
    appId: "",
    appVersion: "",
    formId: "",
    existingFields: [],
    
    //undo & redo feature
    tempJson : '',
    isCtrlKeyPressed : false,
    isAltKeyPressed : false,
    undoStack : new Array(),
    redoStack : new Array(),
    undoRedoMax : 50,

    init: function(id) {

        // workaround for IE draggable bug #4333 http://bugs.jqueryui.com/ticket/4333
        $.extend($.ui.sortable.prototype, (function (orig) {
            return {
                _mouseCapture: function (event) {
                    var result = orig.call(this, event);
                    if (result && $.browser.msie) event.stopPropagation();
                    return result;
                }
            };
        })($.ui.sortable.prototype["_mouseCapture"]));

        // initialize palette
        FormBuilder.initPalette();

        // initialize canvas
        FormBuilder.formId = id;
        FormBuilder.initCanvas(id);
        
        FormBuilder.initUndoRedo();

        var formJson = FormBuilder.generateJSON(true);
        
        FormBuilder.originalJson = formJson;
        $("#form-json-original").val(FormBuilder.originalJson);
    },

    initPalette: function() {
        // make palette sections draggable
        $(".form-palette-section").draggable({
            connectToSortable: ".form-container",
            helper: "clone",
            zIndex: 200,
            revert: "invalid",
            cursor: "move"
        }).disableSelection();

        // make palette columns draggable
        $(".form-palette-column").draggable({
            connectToSortable: ".form-section",
            helper: "clone",
            zIndex: 200,
            revert: "invalid",
            cursor: "move"
        }).disableSelection();

        // make elements draggable
        $(".form-palette-element").draggable({
            connectToSortable: ".form-column",
            helper: "clone",
            opacity: 0.7,
            revert: "invalid",
            cursor: "move"
        }).disableSelection();
    },

    initCanvas: function(id) {
        // make sections sortable
        $(".form-container").sortable({
            connectWith: ".form-container",
            items: ".form-section",
            stop: function() {
                FormBuilder.generateJSON(true);
            },
            tolerance: "pointer",
            revertDuration: 100,
            revert: "invalid"
        }).disableSelection();

        // capture drop events
        $(".form-container").droppable({
            drop: function(event, ui) {
                var obj = $(ui.draggable);
                if (obj.hasClass("form-palette-section")) {
                    FormBuilder.decorateSection(obj);
                }
            },
            activeClass: "form-cell-highlight",
            accept: ".form-palette-section",
            greedy: true,
            tolerance: "touch"
        });
        // decorate all elements
        $(".form-section").each(function(index, obj) {
            FormBuilder.decorateSection(obj);
        });
        $(".form-column").each(function(index, obj) {
            FormBuilder.decorateColumn(obj);
        });
        $(".form-cell").each(function(index, obj) {
            FormBuilder.decorateElement(obj);
        });

        // initialize form properties
        FormBuilder.initFormProperties(id);

        // initialize new sections and columns
        FormBuilder.initSectionsAndColumns();
    },
    
    initUndoRedo : function() {
        //Shortcut key
        $(document).keyup(function (e) {
            if(e.which == 17){
                FormBuilder.isCtrlKeyPressed=false;
            } else if(e.which === 18){
                FormBuilder.isAltKeyPressed = false;
            }
        }).keydown(function (e) {
            if(e.which == 17){
                FormBuilder.isCtrlKeyPressed=true;
            } else if(e.which === 18){
                FormBuilder.isAltKeyPressed = true;
            }
            if ($(".property-editor-container:visible").length === 0) {
                if(e.which == 90 && FormBuilder.isCtrlKeyPressed == true && !FormBuilder.isAltKeyPressed) { //CTRL+Z - undo
                    FormBuilder.undo();
                    return false;
                }
                if(e.which == 89 && FormBuilder.isCtrlKeyPressed == true && !FormBuilder.isAltKeyPressed) { //CTRL+Y - redo
                    FormBuilder.redo();
                    return false;
                }
            }
        });
        
        //add control
        $("#builder-steps").after("<div class='controls'></div>");
        $(".controls").append("<a class='action-undo disabled' title='"+get_fbuilder_msg('fbuilder.undo.disabled.tip')+"'><i class='fas fa-undo'></i> "+get_fbuilder_msg('fbuilder.undo')+"</a>&nbsp;|&nbsp;");
        $(".controls").append("<a class='action-redo disabled' title='"+get_fbuilder_msg('fbuilder.redo.disabled.tip')+"'><i class='fas fa-redo'></i> "+get_fbuilder_msg('fbuilder.redo')+"</a>");
        
        $(".action-undo").click(function(){
            FormBuilder.undo();
            return false;
        });
        
        $(".action-redo").click(function(){
            FormBuilder.redo();
            return false;
        });
    },

    populatePaletteIcons: function() {
        $("#builder-palette .form-palette-element").each(function() {
            var icon = $(this).data("icon");
            var iconObj = null;
            if (icon !== undefined && icon !== null && icon !== "") {
                try {   
                    iconObj = $(icon);
                } catch (err) {
                    iconObj =  $('<span class="image" style="background-image:url(\'' + FormBuilder.contextPath + icon + '\');" />');
                }
            } else {
                iconObj = $('<i class="far fa-edit"></i>');
            }
            $(this).prepend(iconObj);
        });
    },

    initElementDefinition: function(elementClass, properties, template) {
        //add in field id validation
        var found = false;
        if (properties !== null && properties !== undefined) {
            for (var i in properties) {
                if (properties[i].properties != null && properties[i].properties !== undefined) {
                    for (var j in properties[i].properties) {
                        if (properties[i].properties[j].name === "id" && properties[i].properties[j].js_validation === undefined) {
                            properties[i].properties[j].js_validation = 'FormBuilder.validateFieldId';
                            found = true;
                            break;
                        }
                    }
                }
                if (found) {
                    break;
                }
            }
        }
        FormBuilder.elementPropertyDefinitions[elementClass] = properties;
        FormBuilder.elementTemplates[elementClass] = template;
    },

    initFormProperties: function(id) {
        var form = $(".form-container")[0];
        FormBuilder.updateElementDOM(form);
        if (id) {
            form.dom.properties.id = id;
        }
        
        //retrieve existing field ids
        FormBuilder.retrieveExistingFieldIds();
    },

    initSectionsAndColumns: function() {
        // make sure there is at least 1 section
        if ($(".form-section").length == 0) {
            FormBuilder.addSection();
        }

        // make sure all sections have at least 1 column
        $(".form-section").each(function(index, section) {
            var columnCount = $(section).children(".form-column").length;
            if (columnCount == 0) {
                FormBuilder.addColumn(section, false);
            }
        });

        $(".form-section").on("mouseenter", function() {
            FormBuilder.updatePasteIcon();
        });

        // make columns sortable
        $(".form-section").sortable({
            connectWith: ".form-section",
            items: ".form-column",
            start: function( event, ui ) {
                FormBuilder.tempJson = FormBuilder.generateJSON();
            },
            stop: function() {
                $(".form-section > .form-clear.bottom").remove();
                
                FormBuilder.initColumnSizes();
                FormBuilder.initSectionsAndColumns();
                    
                FormBuilder.addToUndo(FormBuilder.tempJson);
                FormBuilder.generateJSON(true);
                
                $(".form-section").append("<div class='form-clear bottom'></div>");
            },
            tolerance: "pointer",
            revertDuration: 100,
            revert: "invalid"
        }).disableSelection();

        // make cells sortable
        $(".form-column").sortable({
            connectWith: ".form-column",
            items: ".form-cell",
            start: function( event, ui ) {
                FormBuilder.tempJson = FormBuilder.generateJSON();
            },
            stop: function() {
                FormBuilder.addToUndo(FormBuilder.tempJson);
                FormBuilder.generateJSON(true);
            },
            out: function (event, ui) {
                // workaround for bug ​http://bugs.jqueryui.com/ticket/6259
                if (ui.helper) {
                    ui.helper.data('overSortable', true);
                }
            },
            tolerance: "pointer",
            revertDuration: 100,
            revert: "invalid"
        }).disableSelection();

        // make section dropabble
        $(".form-section").droppable({
            over: function( event, ui ) {
                FormBuilder.tempJson = FormBuilder.generateJSON();
            },
            drop: function(event, ui) {
                FormBuilder.addToUndo(FormBuilder.tempJson);
                $(".form-section > .form-clear.bottom").remove();
                var obj = $(ui.draggable);
                if (obj.hasClass("form-palette-column")) {
                    FormBuilder.decorateColumn(obj);
                } else if (obj.hasClass("form-column")) {
                    setTimeout(function() {
                        FormBuilder.initColumnSizes();
                        FormBuilder.initSectionsAndColumns();
                    }, 1000);
                }
                $(".form-section").append("<div class='form-clear bottom'></div>");
            },
            activeClass: "form-section-highlight",
            accept: ".form-palette-column, .form-column",
            greedy: true,
            tolerance: "touch"
        });

        // make column droppable
        $(".form-column").droppable({
            over: function( event, ui ) {
                // workaround for bug ​http://bugs.jqueryui.com/ticket/6259
                ui.helper.removeData('overSortable');
                FormBuilder.tempJson = FormBuilder.generateJSON();
            },
            drop: function(event, ui) {
                // workaround for bug ​http://bugs.jqueryui.com/ticket/6259
                if (ui.helper.data('overSortable') != null) {
                    ui.helper.removeData('overSortable');
                    return;
                }
                FormBuilder.addToUndo(FormBuilder.tempJson);
                var column = $(this);
                // check for drop within the column
                if (column.hasClass("form-column") && (column.parent().find(".ui-sortable-placeholder").length > 0)) {
                    var obj = $(ui.draggable);
                    FormBuilder.addElement(obj);
                }
            },
            activeClass: "form-cell-highlight",
            accept: ".form-palette-element",
            greedy: true,
            tolerance: "touch"
        });

        // set column widths
        FormBuilder.initColumnSizes();

        // reset column options and hints
        $(".form-column").each(function(index, obj) {
            // set hints
            if ($(obj).children(".hint").length == 0) {
                $(obj).prepend("<span class='hint'></span>");
            }
            if ($(obj).children(".form-cell").length == 0) {
                $(obj).children(".hint").text(get_fbuilder_msg("fbuilder.dropFieldsHere"));
            } else {
                $(obj).children(".hint").text(get_fbuilder_msg("fbuilder.dragThisColumn"));
            }
            
            // set options
            FormBuilder.decorateElementOptions(obj);
        });

        FormBuilder.generateJSON(true);
    },

    initColumnSizes: function() {
        // recalculate column sizes
        $(".form-section").each(function() {
            
            // find number of columns
            var tempCount = $(this).children(".form-palette-column").length; // ignore div from palette
            if (tempCount == 0) {
                tempCount =  $(this).children(".ui-sortable-helper").length; // ignore sortable placeholder
            }
            var columnCount = $(this).children(".form-column").length - tempCount;
            var autoWidth = true;
            $(this).children(".form-column").each(function(index, column) {
                var prop = column.dom.properties;
                var customWidth = prop.customWidth;
                if (customWidth && customWidth != "") {
                    autoWidth = false;
                    return false;
                }
            });            
            var columnWidth = (columnCount != 1) ? (Math.floor(100 / columnCount)-1) : 100;
            var columnWidthStr = columnWidth + "%";

            // set column width property
            $(this).children(".form-column").each(function(index, column) {
                var width = (autoWidth) ? columnWidthStr : "";
                var prop = column.dom.properties;
                var customWidth = prop.customWidth;
                if (customWidth && customWidth != "") {
                    width = customWidth;
                }
                $(column).css("width", width);
                prop.width = width;
                FormBuilder.updateElementProperties(column, prop);
            });
        })
    },

    initElement: function(element) {
        // initialize an element based on its 'element-template' property
        var template = $(element).attr("element-template");
        if (typeof template == "undefined") {
            var dom = $(element)[0].dom;
            if (dom) {
                // lookup from element templates map
                var elementClass = dom.className;
                template = FormBuilder.elementTemplates[elementClass];
            }
        }
        if (template) {
            template = "<div class='form-palette-element'>" + template + "</div>";
            element = $(template);
        }
        return element;
    },

    decorateSection: function(obj) {
        // change css class from form-palette-section to form-section
        if ($(obj).hasClass("form-palette-section")) {
            $(obj).removeClass("form-palette-section").addClass("form-section");
        }
        
        if ($(obj).find(".form-section-title").length == 0) {
            $(obj).prepend("<div class='form-section-title'><span>"+get_fbuilder_msg("fbuilder.section")+"</span><div>");
        }

        // set definition and properties
        FormBuilder.updateElementDOM(obj);
        FormBuilder.generateElementId(obj, "form-section", "section");

        // add options
        FormBuilder.decorateElementOptions(obj);

    },

    decorateColumn: function(obj) {
        // change css class from form-palette-column to form-column
        if ($(obj).hasClass("form-palette-column")) {
            $(obj).removeClass("form-palette-column").addClass("form-column");
        }

        // set definition and properties
        FormBuilder.updateElementDOM(obj);

        // add options
        FormBuilder.decorateElementOptions(obj);

    },

    decorateElement: function(obj) {
        // set definition and properties
        FormBuilder.updateElementDOM(obj);
        FormBuilder.generateElementId(obj, "form-cell", "field");

        if ($(obj).hasClass("form-palette-element")) {
            // set inner html
            var element = FormBuilder.initElement(obj);
            $(obj).html($(element).html());

            // change css class from form-palette-element to form-cell
            $(obj).removeClass("form-palette-element").addClass("form-cell");
        }

        // add options
        if ($(obj).is("form")) {
            return;
        }
        FormBuilder.decorateElementOptions(obj);

    },

    decorateElementOptions: function(obj) {
        if ($(obj).children(".form-palette-options").length > 0) {
            // remove if already exists
            $(obj).children(".form-palette-options").remove();
            $(obj).children(".form-clear").remove();
        }

        var optionHtml = "<span class='form-palette-options'>";
        if ($(obj).hasClass("form-section")) {
            // add buttons for section
            optionHtml += "<button class='form-palette-edit' title='"+get_fbuilder_msg("fbuilder.editSection")+"'><i class='far fa-edit'></i><span>"+get_fbuilder_msg("fbuilder.editSection")+"</span></button>";
            optionHtml += "<button class='form-palette-copy' title='"+get_fbuilder_msg("fbuilder.copy")+"'><i class='far fa-copy'></i><span>"+get_fbuilder_msg("fbuilder.copy")+"</span></button>";
            optionHtml += "<button class='form-palette-col' title='"+get_fbuilder_msg("fbuilder.addColumn")+"'><i class='fas fa-columns'></i><span>"+get_fbuilder_msg("fbuilder.addColumn")+"</span></button>";
            optionHtml += "<button class='form-palette-comment' title='"+get_fbuilder_msg("fbuilder.comment")+"'><i class='far fa-comment'></i><span>"+get_fbuilder_msg("fbuilder.comment")+"</span></button>";
            optionHtml += "<button class='form-palette-remove' title='"+get_fbuilder_msg("fbuilder.deleteSection")+"'><i class='fas fa-times'></i><span>"+get_fbuilder_msg("fbuilder.deleteSection")+"</span></button>";
            $(obj).append("<div class='form-clear bottom'></div>");
            
            //comment
            $(obj).find(".section-comment").remove();
            var dom = $(obj)[0].dom;
            var comment = dom.properties["comment"];
            if (comment !== undefined && comment !== null && comment !== "") {
                $(obj).find(".form-section-title").before('<div class="section-comment"><strong>'+get_fbuilder_msg("fbuilder.comment")+':</strong> <div class="editable">'+UI.escapeHTML(comment).replace(/(?:\r\n|\r|\n)/g, '<br />')+'</div></div>');
                FormBuilder.initEditableComment(obj);
            }
        } else if ($(obj).hasClass("form-column")) {
            // add buttons for column
            optionHtml += "<button class='form-palette-edit' title='"+get_fbuilder_msg("fbuilder.editColumn")+"'><i class='far fa-edit'></i><span>"+get_fbuilder_msg("fbuilder.editColumn")+"</span></button>";
            optionHtml += "<button class='form-palette-paste column disabled' title='"+get_fbuilder_msg("fbuilder.pasteElement")+"'><i class='fas fa-paste'></i><span>"+get_fbuilder_msg("fbuilder.pasteElement")+"</span></button>";
            if ($(obj).siblings(".form-column").length > 0) {
                optionHtml += "<button class='form-palette-remove' title='"+get_fbuilder_msg("fbuilder.deleteColumn")+"'><i class='fas fa-times'></i><span>"+get_fbuilder_msg("fbuilder.deleteColumn")+"</span></button>";
            }
            
            //Update column label
            var label = $(obj)[0].dom.properties.label;
            if (label != undefined && label != "") {
                $(obj).find(".form-column-label").remove();
                var hint = $(obj).find(".hint");
                $(hint).after("<h3 class=\"form-column-label\">" + UI.escapeHTML(label) + "</h3>");
            }else{
                $(obj).find(".form-column-label").remove();
            }
        } else if (!$(obj).hasClass("form-container ")) {
            // add buttons for other elements
            optionHtml += "<button class='form-palette-edit' title='"+get_fbuilder_msg("fbuilder.edit")+"'><i class='far fa-edit'></i><span>"+get_fbuilder_msg("fbuilder.edit")+"</span></button>";
            optionHtml += "<button class='form-palette-copy' title='"+get_fbuilder_msg("fbuilder.copy")+"'><i class='far fa-copy'></i><span>"+get_fbuilder_msg("fbuilder.copy")+"</span></button>";
            optionHtml += "<button class='form-palette-remove' title='"+get_fbuilder_msg("fbuilder.delete")+"'><i class='fas fa-times'></i><span>"+get_fbuilder_msg("fbuilder.delete")+"</span></button>";
        }
        optionHtml += "</span><div class='form-clear'></div>";
        
        if ($(obj).hasClass("form-section")) {
            optionHtml += "<span class='form-palette-options bottom'>";
            optionHtml += "<button class='form-palette-sec' title='"+get_fbuilder_msg("fbuilder.addSection")+"'><i class='fas fa-plus'></i><span>"+get_fbuilder_msg("fbuilder.addSection")+"</span></button>";
            optionHtml += "<button class='form-palette-paste section disabled' title='"+get_fbuilder_msg("fbuilder.pasteSection")+"'><i class='fas fa-paste'></i><span>"+get_fbuilder_msg("fbuilder.pasteSection")+"</span></button>";
            optionHtml += "</span><div class='form-clear'></div>";
        }    
        
        var optionDiv = $(optionHtml);

        // handle deletion
        $(optionDiv).children(".form-palette-remove").click(function() {
            var element = $(this).parent().parent();
            FormBuilder.deleteElement(element);
            return false;
        });

        // handle edit
        $(optionDiv).children(".form-palette-edit").click(function() {
            var element = $(this).parent().parent();
            // open properties dialog
            FormBuilder.editElementProperties(element);
            return false;
        });

        // handle add section
        $(optionDiv).children(".form-palette-sec").click(function() {
            var position = ($(this).parent()).hasClass("top")?"before":"after";
            var element = $(this).parent().parent();
            if (element.hasClass("form-section")) {
                FormBuilder.addToUndo();
                FormBuilder.addSection(element, position);
            }
            return false;
        });

        // handle add column
        $(optionDiv).children(".form-palette-col").click(function() {
            var element = $(this).parent().parent();
            if (element.hasClass("form-section")) {
                FormBuilder.addToUndo();
                FormBuilder.addColumn(element);
            }
            return false;
        });

        // handle copy
        $(optionDiv).children(".form-palette-copy").click(function() {
            var element = $(this).parent().parent();
            FormBuilder.copy(element);
            return false;
        });
        
        // handle paste
        $(optionDiv).children(".form-palette-paste").click(function() {
            if ($(this).hasClass("disabled")) {
                alert(get_fbuilder_msg("fbuilder.noCopiedItem"));
                return false;
            }
            var position = ($(this).parent()).hasClass("top")?"before":"after";
            var element = $(this).parent().parent();
            if ($(element).hasClass("form-column")) {
                position = "inner";
            }
            FormBuilder.paste(element, position);
            return false;
        });
        
        // handle comment
        $(optionDiv).children(".form-palette-comment").click(function() {
            var element = $(this).parent().parent();
            
            if ($(element).find(".section-comment").length === 0) {
                $(obj).find(".form-section-title").before('<div class="section-comment"><strong>'+get_fbuilder_msg("fbuilder.comment")+':</strong> <div class="editable"></div></div>');
                FormBuilder.initEditableComment(obj);
            }
            $(obj).find('.section-comment .editable').click();
            
            return false;
        });

        // add option bar
        $(obj).prepend(optionDiv);
        $(obj).mouseover(function() {
            $(optionDiv).css("display", "block");
            $(optionDiv).css("visibility", "visible");
        });
        $(obj).mouseout(function() {
            $(optionDiv).css("display", "none");
            $(optionDiv).css("visibility", "hidden");
        });
        $(obj).on("click", function() {
            if ($(optionDiv).css("display") === "block") {
                $(optionDiv).css("display", "none");
                $(optionDiv).css("visibility", "hidden");
            } else {
                if ($(obj).children().length > 0) {
                    var $family = $(obj).find("*");
                    $(".form-palette-options").not($family).css("display", "none");
                    $(".form-palette-options").not($family).css("visibility", "hidden");
                }
                $(optionDiv).css("display", "block");
                $(optionDiv).css("visibility", "visible");
            }
        });

    },
    
    initEditableComment: function(obj) {
        $(obj).find(".section-comment .editable").editable(function(value, settings){
            FormBuilder.addToUndo();
            
            $(obj)[0].dom.properties["comment"] = value;
            if(value === ""){
                $(obj).find(".section-comment").remove();
            } else {
                value = UI.escapeHTML(value).replace(/(?:\r\n|\r|\n)/g, '<br />');
            }
            FormBuilder.generateJSON(true);
            return value;
        },{
            type      : 'textarea',
            tooltip   : '' ,
            select    : true ,
            style     : 'inherit',
            cssclass  : 'LabelEditableField',
            onblur    : 'submit',
            rows      : 4,
            width     : '90%',
            minwidth  : 80,
            submit  : get_fbuilder_msg("fbuilder.save"),
            data: function(value, settings) {
                if (value !== "") {
                    return value.replace(/<br\s*[\/]?>/gi, "\n");
                } else {
                    return value;
                }
            }
        });
        $(obj).find(".section-comment").on("click", function(){
            if ($(obj).find('.section-comment .editable form').length === 0) {
                $(obj).find('.section-comment .editable').click();
            }
        });
    }, 

    updateElementDOM: function(element) {
        // set class name
        var dom = new Object();
        dom.className = $(element).attr("element-class");

        // set properties
        var propertyJson = $(element).attr("element-property");
        var property = eval("(" + propertyJson + ")");
        if (typeof property == "undefined") {
            property = new Object();
        }
        dom.properties = property;
        $(element)[0].dom = dom;
        $(element).attr("element-id", property.id);
    },
    
    generateElementId: function(element, elementClass, prefix) {
        var dom = $(element)[0].dom;
        if (dom != null) {
            // set ID if empty
            var elementId = dom.properties.id;
            if (typeof elementId == "undefined" || elementId == "") {
                // determine element class
                if (typeof elementClass == "undefined" || elementClass == "") {
                    elementClass = "form-cell";
                } 

                // determine prefix
                if (typeof prefix == "undefined" || prefix == "") {
                    prefix = "element";
                } 

                // generate ID
                var elements = $(".form-container").find("." + elementClass);
                var newIndex = elements.length + 1;
                var newId = prefix + newIndex;

                // set ID
                dom.properties.id = newId;
                $(element).attr("element-id", newId);
                $(element).attr("element-property", JSON.encode(dom.properties));
            }
        }
    },

    editElementProperties: function(element) {
        // get current properties
        var dom = $(element)[0].dom;
        var elementClass = dom.className;
        var elementProperty = dom.properties;

        // get available property options from the attribute 'element-options'
        var elementOptions = new Object();
        var elementOptionStr = $(element).attr("element-options");
        if (elementOptionStr && elementOptionStr.length > 0) {
            // custom options available, use that
            elementOptions = eval("(" + elementOptionStr + ")");
        } else {
            // lookup from available definitions
            elementOptions = FormBuilder.elementPropertyDefinitions[elementClass];
            if (elementOptions == null) {
                // default element properties
                elementOptions = [{
                        title: get_fbuilder_msg("fbuilder.editField"),
                        properties: [{
                                name:'label',
                                label:get_fbuilder_msg("fbuilder.label"),
                                type:'textfield'}]
                    }];
            }
        }

        // show property dialog
        var options = {
            appPath: "/" + FormBuilder.appId + "/" + FormBuilder.appVersion,
            contextPath: FormBuilder.contextPath,
            propertiesDefinition : elementOptions,
            propertyValues : elementProperty,
            showCancelButton:true,
            changeCheckIgnoreUndefined: true,
            cancelCallback: function() {
            },
            saveCallback: function(container, properties) {
                FormBuilder.addToUndo();

                // update element properties
                FormBuilder.updateElementProperties(element, properties);

                // refresh element UI
                FormBuilder.refreshElementTemplate(element);
            }
        }

        // show popup dialog
        if (!PropertyEditor.Popup.hasDialog("form-property-editor")) {
            PropertyEditor.Popup.createDialog("form-property-editor");
        }
        PropertyEditor.Popup.showDialog("form-property-editor", options);
    },

    updateElementProperties: function(element, properties) {
        if ($(element).is("form")) {
            element = $(".form-container");
        }
        // update element properties
        var dom = $(element)[0].dom;
        $(element).attr("element-id", properties.id);
        if (dom) {
            if (dom.properties.permission_rules !== null && dom.properties.permission_rules !== undefined) {
                properties.permission_rules = dom.properties.permission_rules;
            }
            if (dom.properties.permissionHidden !== null && dom.properties.permissionHidden !== undefined) {
                properties.permissionHidden = dom.properties.permissionHidden;
            }
            if (dom.properties.permissionReadonlyHidden !== null && dom.properties.permissionReadonlyHidden !== undefined) {
                properties.permissionReadonlyHidden = dom.properties.permissionReadonlyHidden;
            }
            if ((properties['readonly'] === null || properties['readonly'] === undefined) && dom.properties['readonly'] !== undefined) { //permission readonly is always reset when edit hidden field due to no having readonly in property options
                properties['readonly'] = dom.properties['readonly'];
            }
            if (properties['readonly'] === "true") {
                properties['permissionHidden'] = "";
            }
            dom.properties = properties;
        }

        FormBuilder.generateJSON(true);
    },

    refreshElementTemplate: function(element) {
        var dom = $(element)[0].dom;

        // make server-side call to get updated template
        var elementClass = dom.className;
        var elementProperty = dom.properties;
        var elementHtml = FormBuilder.retrieveElementHTML(element, dom, elementClass, elementProperty);

        // set updated element HTML (non-section and column only)
        if (elementClass != 'org.joget.apps.form.model.Section' && elementClass != 'org.joget.apps.form.model.Column') {
            FormBuilder.unloadElement(element);
            $(element).html(elementHtml);
        } else if (elementClass == 'org.joget.apps.form.model.Column') {
            // hard-code refresh column sizes
            FormBuilder.initColumnSizes();
        }

        // reset options
        FormBuilder.decorateElementOptions(element);
    },

    retrieveElementHTML: function(element, dom, elementClass, elementProperty, callback) {

        // temp hard-coded to handle sections
        if (elementClass == 'org.joget.apps.form.model.Section') {
            var title = elementProperty['label'];
            if (title && title.length > 0) {
                title = "<span>" + UI.escapeHTML(title) + "</span>";
            } else {
                title = "";
            }
            element.find(".form-section-title").html(title);
        } else if (elementClass == 'org.joget.apps.form.model.Column') {
            // temp hard-coded to handle columns
            var horiz = elementProperty['horizontal'];
            if (horiz && horiz == "true") {
                element.addClass("form-column-horizontal");
            } else {
                element.removeClass("form-column-horizontal");
            }
        } else {
            // make AJAX JSON call to get updated template
            var jsonStr = JSON.encode(dom);
            $.ajax({
                type: "POST",
                data: {"json": jsonStr },
                url: FormBuilder.contextPath + FormBuilder.elementPreviewUrl,
                dataType : "text",
                beforeSend: function (request) {
                   request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                },
                success: function(response) {
                    var newElement = $(response);

                    //locate main element
                    if( newElement.length > 1 ){
                        i = 0;
                        while(i<newElement.length){
                            if($(newElement[i]).attr("element-class") != undefined){
                                FormBuilder.decorateElement($(newElement[i]));
                                break;
                            }
                            i++;
                        }
                    }else{
                        FormBuilder.decorateElement(newElement);
                    }
                    $(element).replaceWith(newElement);
                    
                    if ($(element).is("form")) {
                        FormBuilder.initCanvas($(element).attr("id"));
                        $("#loading").remove();
                    }
                    
                    if (callback !== undefined && $.type(callback) === "function") {
                        callback();
                    }
                }
            });

            return get_fbuilder_msg("fbuilder.loading");
        }


        var html = $(element).html();
        return html;
    },

    deleteElement: function(element) {
        FormBuilder.addToUndo();
        FormBuilder.unloadElement(element);
        
        // delete element
        $(element).remove();

        FormBuilder.initSectionsAndColumns();
    },
    
    unloadElement: function(element) {
        var dom = $(element)[0].dom;
        var elementClass = dom.className;
        
        var unloadMethod = elementClass.replace(/\./g, "_") + "_formBuilderElementUnLoad";
        
        if($.isFunction(window[unloadMethod])){
            window[unloadMethod](element);
        }
    },

    addSection: function(parent, position) {
        // add a new section
        var section = $("<div class='form-section' element-class='org.joget.apps.form.model.Section' element-property='{label:\""+get_fbuilder_msg("fbuilder.section")+"\"}'></div>");
        FormBuilder.decorateSection(section);
        if (parent) {
            if (position !== undefined && position === "before") {
            // add after an existing section
                $(parent).before(section);
            } else {
                // add after an existing section
            $(parent).after(section);
            }
        } else {
            $(".form-container").append(section);
        }
        FormBuilder.initSectionsAndColumns();
        return section;
    },

    addColumn: function(section, initSectionsAndColumns) {
        // add a new column
        var column = $("<div class='form-column' element-class='org.joget.apps.form.model.Column'></div>");
        $(section).find(".form-clear.bottom").before(column);
        FormBuilder.decorateColumn(column);
        
        if (initSectionsAndColumns === undefined || initSectionsAndColumns) {
            FormBuilder.initSectionsAndColumns();
        }
        return column;
    },

    addElement: function(element, column) {
        if (typeof column != "undefined" && $(column).hasClass("form-column")) {
            // add to column
            $(column).append(element);
        }
        FormBuilder.decorateElement(element);
        setTimeout(function() {
            FormBuilder.initSectionsAndColumns();
            element.css("width", "");
            element.css("height", "");
            element.css("top", "");
            element.css("left", "");
        }, 10);
    },

    clear: function() {
        // empty the form
        $(".form-container").empty();
    },

    previewForm: function(){
        $('#form-preview').attr("action", FormBuilder.contextPath + FormBuilder.formPreviewUrl);
        $('#form-preview').submit();
        return false;
    },

    generateJSON: function(triggerChange) {
        var form = new Object();
        form.className = "org.joget.apps.form.model.Form";

        // set form properties
        var formDom = $(".form-container")[0].dom;
        if (formDom) {
            form.properties = formDom.properties;
        }

        // add sections
        form.elements = new Array();
        $(".form-section").each(function(index, sectionElement){
            var section = new Object();
            section.elements = new Array();
            var sectionDom = $(sectionElement)[0].dom;
            if (sectionDom) {
                section.className = sectionDom.className;
                section.properties = sectionDom.properties;
                form.elements.push(section);

                // add columns
                $(sectionElement).children(".form-column").each(function(index2, columnElement) {
                    var column = new Object();
                    column.elements = new Array();
                    var columnDom = $(columnElement)[0].dom;
                    if (columnDom) {
                        column.className = columnDom.className;
                        column.properties = columnDom.properties;
                        section.elements.push(column);

                        // add elements
                        $(columnElement).children(".form-cell").each(function(index3, fieldElement) {
                            var element = new Object();
                            var elementDom = $(fieldElement)[0].dom;
                            if (elementDom) {
                                element.className = elementDom.className;
                                element.properties = elementDom.properties;
                                column.elements.push(element);
                            }
                        });
                    }

                });
            }
        });

        // convert object to JSON
        var json = JSON.encode(form);

        // set output
        $("#form-json").val("");
        $("#form-json").val(json);
        
        if (triggerChange) {
            $("#form-json").trigger("change");
        }

        return json;
    },

    showBuilder: function() {
        // update menu
        $("#builder-step-properties").removeClass("active");
        $("#builder-step-properties").addClass("inactive");
        $("#builder-step-design").removeClass("first-inactive");
        $("#builder-step-design").removeClass("next");
        $("#builder-step-design").addClass("first-active");
        $("#builder-step-design").addClass("active");

        // hide form properties div
        $("#form-properties").css("display", "none");

        // show builder div
        $("#builder-content").css("display", "block");
        $("body").removeClass("stop-scrolling");
        return false;
    },

    showFormProperties: function() {
        // update menu
        $("#builder-step-design").removeClass("active");
        $("#builder-step-design").removeClass("first-active");
        $("#builder-step-design").addClass("first-inactive");
        $("#builder-step-design").addClass("next");
        $("#builder-step-properties").addClass("active");

        // hide builder div
        $("#builder-content").css("display", "none");

        // show form properties div
        var dialog = $("#form-properties");
        if (dialog.length == 0) {
            dialog = $("<div id='form-properties'></div>");
            $("#builder-body").append(dialog);
        }
        dialog.css("display", "block");

        // get form property options
        var form = $(".form-container")[0];
//        var formOptions = new Object();
//        var formOptionStr = $(form).attr("element-options");
//        if (formOptionStr && formOptionStr.length > 0) {
//            formOptions = eval("(" + formOptionStr + ")");
//        }
        var formOptions = FormBuilder.elementPropertyDefinitions["org.joget.apps.form.model.Form"];

        // show form property editor
        $("#form-properties").html("");
        var formProperties = form.dom.properties;
        var options = {
            appPath: "/" + FormBuilder.appId + "/" + FormBuilder.appVersion,
            contextPath: FormBuilder.contextPath,
            propertiesDefinition: formOptions,
            propertyValues: formProperties,
            changeCheckIgnoreUndefined: true,
            showCancelButton: false,
            closeAfterSaved: false,
            autoSave: true,
            saveCallback: function(container, properties) {
                FormBuilder.addToUndo();
                // update form properties
                FormBuilder.updateElementProperties(form, properties);
                
                //retrieve existing fields
                FormBuilder.retrieveExistingFieldIds();
            }
        };
        $('#form-properties').propertyEditor(options);
        $("body").addClass("stop-scrolling");
        return false;
    },
    
    showPopUpFormProperties : function () {
        // get form property options
        var form = $(".form-container")[0];
        
        var formOptions = FormBuilder.elementPropertyDefinitions["org.joget.apps.form.model.Form"];
        var formProperties = form.dom.properties;
        
        // show property dialog
        var options = {
            appPath: "/" + FormBuilder.appId + "/" + FormBuilder.appVersion,
            contextPath: FormBuilder.contextPath,
            propertiesDefinition: formOptions,
            propertyValues: formProperties,
            showCancelButton:true,
            cancelCallback: function() {
            },
            saveCallback: function(container, properties) {
                FormBuilder.addToUndo();

                // update element properties
                FormBuilder.updateElementProperties(form, properties);
                
                //retrieve existing fields
                FormBuilder.retrieveExistingFieldIds();
                
                // change to design tab
                $("#builder-step-design").trigger("click");
            }
        };

        // show popup dialog
        if (!PropertyEditor.Popup.hasDialog("form-property-editor")) {
            PropertyEditor.Popup.createDialog("form-property-editor");
        }
        PropertyEditor.Popup.showDialog("form-property-editor", options);
    },
    
    isEmpty : function() {
        return ($(".form-container-div .form-cell").length === 0);
    },
    
    isSaved : function(){
        if(FormBuilder.originalJson === FormBuilder.generateJSON()){
            return true;
        }else{
            return false;
        }
    },
    
    getCopiedElement : function() {
        var time = $.localStorage.getItem("formBuilder.copyTime");
        //10 mins
        if (time !== undefined && time !== null && ((new Date()) - (new Date(time))) > 3000000) {
            $.localStorage.removeItem('formBuilder.copyTime');
            $.localStorage.removeItem('formBuilder.copy');
            $.localStorage.removeItem('formBuilder.copyProperty');
            return null;
        }
        var copied = $.localStorage.getItem("formBuilder.copy");
        if (copied !== undefined && copied !== null) {
            copied = $(copied).attr("element-property", $.localStorage.getItem("formBuilder.copyProperty"));
        }
        return copied;
    },
    
    copy : function(element) {
        var copy = $(element).clone().wrap('<p/>').parent();
        $(copy).find(".form-palette-options, .form-clear").remove();
        $.localStorage.setItem("formBuilder.copy", $(copy).html());
        var dom = $(element)[0].dom;
        var elementProperty = $(element).attr("element-property");
        if (dom && dom.properties) {
            elementProperty = JSON.encode(dom.properties);
        }
        $.localStorage.setItem("formBuilder.copyProperty", elementProperty);
        $.localStorage.setItem("formBuilder.copyTime", new Date());
        FormBuilder.updatePasteIcon();
        FormBuilder.showMessage(get_fbuilder_msg('fbuilder.copied'));
        setTimeout(function(){ FormBuilder.showMessage(""); }, 2000);
    },
    
    paste : function(element, position) {
        var copied = FormBuilder.getCopiedElement();
        if (copied !== undefined && copied !== null) {
            FormBuilder.addToUndo();
            var copiedElement = $(copied);
            
            if ($(copiedElement).hasClass("form-section")) {
                $(copiedElement).find(".form-cell").each(function(index, obj) {
                    $(obj).html("");
                });
            } else {
                $(copiedElement).html("");
            }
            
            if ($(copiedElement).hasClass("form-section")) {
                FormBuilder.updateCopiedId(copiedElement);
            }
            
            if (position === "before") {
                $(element).before(copiedElement);
            } else if (position === "after") {
                $(element).after(copiedElement);
            } else {
                $(element).append(copiedElement);
            }
            
            // decorate all elements
            if ($(copiedElement).hasClass("form-section")) {
                FormBuilder.decorateSection(copiedElement);
            
                $(copiedElement).find(".form-column").each(function(index, obj) {
                    FormBuilder.decorateColumn(obj);
                });

                $(copiedElement).find(".form-cell").each(function(index, obj) {
                    FormBuilder.updateElementDOM(obj);
                    FormBuilder.refreshElementTemplate(obj);
                    FormBuilder.decorateElement(obj);
                });
            } else {
                FormBuilder.updateElementDOM(copiedElement);
                FormBuilder.refreshElementTemplate(copiedElement);
                FormBuilder.decorateElement(copiedElement);
            }
            
            // initialize new sections and columns
            FormBuilder.initSectionsAndColumns();
        }
    },
    
    updateCopiedId : function (element) {
        var propertyJson = $(element).attr("element-property");
        var property = eval("(" + propertyJson + ")");
        if (property !== undefined && property !== null) {
            var id = property.id;
            var count = 0;
            var newId = id;
            while ($("[element-id="+newId+"]").length > 0) {
                count++;
                newId = id + "_" + count;
            }
            
            property.id = newId;
            $(element).attr("element-property", JSON.encode(property));
        }
    },
    
    updatePasteIcon : function() {
        $(".form-palette-paste").addClass("disabled");
        var copied = FormBuilder.getCopiedElement();
        if (copied !== undefined && copied !== null) {
            if ($(copied).hasClass("form-section")) {
                $(".form-palette-paste.section").removeClass("disabled");
            } else {
                $(".form-palette-paste.column").removeClass("disabled");
            }
        }
    },
    
    //Undo the changes from stack
    undo : function(){
        if(FormBuilder.undoStack.length > 0){
            //if redo stack is full, delete first
            if(FormBuilder.redoStack.length >= FormBuilder.undoRedoMax){
                FormBuilder.redoStack.splice(0,1);
            }

            //save current json data to redo stack
            FormBuilder.redoStack.push(FormBuilder.generateJSON());

            //undo-ing
            var loading = $('<div id="loading"><i class="fas fa-spinner fa-spin fa-2x"></i> ' + get_fbuilder_msg("fbuilder.label.undoing") + '</div>');
            $("body").append(loading);
            FormBuilder.loadJson(JSON.decode(FormBuilder.undoStack.pop()));
            
            //enable redo button if it is disabled previously
            if(FormBuilder.redoStack.length === 1){
                $('.action-redo').removeClass('disabled');
                $('.action-redo').attr('title', get_fbuilder_msg('fbuilder.redo.tip'));
            }

            //if undo stack is empty, disabled undo button
            if(FormBuilder.undoStack.length === 0){
                $('.action-undo').addClass('disabled');
                $('.action-undo').attr('title', get_fbuilder_msg('fbuilder.undo.disabled.tip'));
            }
        }
    },

    //Redo the changes from stack
    redo : function(){
        if(FormBuilder.redoStack.length > 0){
            //if undo stack is full, delete first
            if(FormBuilder.undoStack.length >= FormBuilder.undoRedoMax){
                FormBuilder.undoStack.splice(0,1);
            }

            //save current json data to undo stack
            FormBuilder.undoStack.push(FormBuilder.generateJSON());

            //redo-ing
            var loading = $('<div id="loading"><i class="fas fa-spinner fa-spin fa-2x"></i> ' + get_fbuilder_msg("fbuilder.label.redoing") + '</div>');
            $("body").append(loading);
            FormBuilder.loadJson(JSON.decode(FormBuilder.redoStack.pop()));

            //enable undo button if it is disabled previously
            if(FormBuilder.undoStack.length == 1){
                $('.action-undo').removeClass('disabled');
                $('.action-undo').attr('title', get_fbuilder_msg('fbuilder.undo.tip'));
            }

            //if redo stack is empty, disabled redo button
            if(FormBuilder.redoStack.length == 0){
                $('.action-redo').addClass('disabled');
                $('.action-redo').attr('title', get_fbuilder_msg('fbuilder.redo.disabled.tip'));
            }
        }
    },

    //Add changes info to stack
    addToUndo : function(json){
        //if undo stack is full, delete first
        if(FormBuilder.undoStack.length >= FormBuilder.undoRedoMax){
            FormBuilder.undoStack.splice(0,1);
        }
        
        if (json === undefined || json === null) {
            json = FormBuilder.generateJSON();
        }
        
        //save current json data to undo stack
        FormBuilder.undoStack.push(json);

        //enable undo button if it is disabled previously
        if(FormBuilder.undoStack.length === 1){
            $('.action-undo').removeClass('disabled');
            $('.action-undo').attr('title', get_fbuilder_msg('fbuilder.undo.tip'));
        }
    },
    
    loadJson : function(json){
        if (json !== null && json !== undefined && json.properties !== null && json.properties !== undefined) {
            json.properties.id = FormBuilder.formId;
        }
        
        $(".form-container-div").html("<form></form>");
        FormBuilder.retrieveElementHTML($(".form-container-div form"), json, null, null, function() {
           FormBuilder.generateJSON(true); 
        });
    },
    
    showMessage: function(message) {
        if (message && message != "") {
            $("#builder-message").html(message);
            $("#builder-message").fadeIn();
        } else {
            $("#builder-message").fadeOut();
        }
    },
    
    updateForm: function() {
        var json = $('#form-json').val();
        if (FormBuilder.generateJSON() !== json) {
            FormBuilder.addToUndo();
        }
        FormBuilder.loadJson(JSON.decode(json));
        
        return false;
    },
    
    saveForm: function (data) {
        var json = (data) ? data : FormBuilder.generateJSON();
        var saveUrl = FormBuilder.contextPath + "/web/console/app/" + FormBuilder.appId + "/" + FormBuilder.appVersion + "/form/" + FormBuilder.formId + "/update";
        $.ajax({
            type: "POST",
            data: {"json": json},
            url: saveUrl,
            dataType: "text",
            beforeSend: function (request) {
                request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            },
            success: function (response) {
                FormBuilder.showMessage(get_fbuilder_msg('fbuilder.saved'));
                var updateOriginalJson = function() {
                    if ($(".form-container").length > 0 && $(".form-container")[0].dom !== undefined) {
                        FormBuilder.originalJson = FormBuilder.generateJSON(true);
                        $('#form-json-original').val(json);
                        FormBuilder.showMessage("");
                    } else {
                        setTimeout(function () {
                            updateOriginalJson();
                        }, 1000);
                    }
                };
                
                updateOriginalJson();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(get_fbuilder_msg('fbuilder.errorSaving') + " (" + textStatus + "): " + errorThrown);
            }
        });
    },
    
    showDiff : function (callback, output) {
        var jsonUrl = FormBuilder.contextPath + '/web/json/console/app/' + FormBuilder.appId + '/' + FormBuilder.appVersion + '/form/' + FormBuilder.formId + '/json';
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
                $('#form-json-current').val(currentString);
                var original = JSON.decode($('#form-json-original').val());
                var latest = JSON.decode($('#form-json').val());
                merged = DiffMerge.merge(original, current, latest, output);
            },
            error: function() {
                currentSaved = $('#form-json-current').val();
                merged = $('#form-json').val();
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
        FormBuilder.showMessage(get_fbuilder_msg('fbuilder.merging'));
        var thisObject = this;
        
        FormBuilder.showDiff(function (currentSaved, merged) {
            if (currentSaved !== undefined && currentSaved !== "") {
                $('#form-json-original').val(currentSaved);
            }
            if (merged !== undefined && merged !== "") {
                $('#form-json').val(merged);
            }
            FormBuilder.updateForm();
            FormBuilder.showMessage("");
            
            if (callback) {
                callback.call(thisObject, merged);
            }
        });
    },
    
    mergeAndSave: function() {
        FormBuilder.merge(FormBuilder.saveForm);
    },
    
    retrieveExistingFieldIds : function() {
        var form = $(".form-container")[0];
        var tableName = form.dom.properties['tableName'];
        
        $.ajax({
            url: FormBuilder.contextPath + '/web/json/console/app/'+FormBuilder.appId+'/'+FormBuilder.appVersion+'/form/columns/options?tableName='+tableName,
            dataType: "text",
            success: function(data) {
                if(data !== undefined && data !== null){
                    var options = $.parseJSON(data);
                    FormBuilder.existingFields = [];
                    for (var o in options) {
                        FormBuilder.existingFields.push(options[o]['value']);
                    }
                }
            }
        });
    },
    
    getFieldIds : function(includeGridColumn) {
        var ids = [];
        $(".form-column .form-cell").each(function(){
            var className = $(this).attr("element-class");
            var propertyJson = $(this).attr("element-property");
            var property = eval("(" + propertyJson + ")");
            
            if (includeGridColumn && className.toLowerCase().indexOf("grid") !== -1 && property['options'] !== undefined) {
                var fieldId = property['id'];
                for (var i in property['options']) {
                    var c = property['options'][i]['value'];
                    if (c !== undefined && c !== "" && ids[fieldId + "." + c] === undefined) {
                        ids.push(fieldId + "." + c);
                    }
                }
            } else if(property['id'] !== undefined && property['id'] !== "" && $.inArray(property['id'], ids) === -1) {
                ids.push(property['id']);
            }
        });
        return ids;
    },
    
    getAllFieldOptions: function(properties) {
        //populate list items
        var tempArray = [{'label':'','value':''}];
        var ids = FormBuilder.getFieldIds(false);
        
        for (var i in FormBuilder.existingFields) {
            if($.inArray(FormBuilder.existingFields[i], ids) === -1) {
                ids.push(FormBuilder.existingFields[i]);
            }
        }
        
        for(var i in ids){
            var temp = {'label' : ids[i],
                         'value' : ids[i]};
            tempArray.push(temp);
        }
        return tempArray;
    },
    
    getFieldOptions: function(properties) {
        //populate list items
        var tempArray = [{'label':'','value':''}];
        var ids = FormBuilder.getFieldIds(false);
        for(var i in ids){
            var temp = {'label' : ids[i],
                         'value' : ids[i]};
            tempArray.push(temp);
        }
        return tempArray;
    },
    
    getFieldAndGridColumnOptions: function(properties) {
        //populate list items
        var tempArray = [{'label':'','value':''}];
        var ids = FormBuilder.getFieldIds(true);
        for(var i in ids){
            var temp = {'label' : ids[i],
                         'value' : ids[i]};
            tempArray.push(temp);
        }
        return tempArray;
    },
    
    refreshAppDesign: function() {
        if (window.opener && window.opener.refreshNavigator) {
            window.opener.location.reload(true);
        }
    },
    
    validateFieldId: function(name, value) {
        if ($.inArray(value, ["appId","appVersion","version","userviewId","menuId","key","embed"]) >= 0) {
            return get_fbuilder_msg("fbuilder.reserveIds");
        }
        return null;    
    }
}