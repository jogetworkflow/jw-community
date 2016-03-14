FormBuilder = {

    propertyDialog: null, // property dialog popup object
    elementPropertyDefinitions: new Object(), // map of property dialog definitions for each element class
    elementTemplates: new Object(), // map of HTML templates for each element class
    contextPath: "/jw",
    elementPreviewUrl: "/web/fbuilder/element/preview",
    formPreviewUrl: "/web/fbuilder/form/preview/",
    tinymceUrl: "/js/tiny_mce/tiny_mce.js",
    originalJson: "",

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
        FormBuilder.initCanvas(id);

        var formJson = FormBuilder.generateJSON();
        
        FormBuilder.originalJson = formJson;
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
            helper: function() {
                var element = FormBuilder.updateElementDOM($(this));
                element = FormBuilder.initElement($(this));
                return element;
            },
            zIndex: 200,
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
                FormBuilder.generateJSON();
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

    initElementDefinition: function(elementClass, properties, template) {
        FormBuilder.elementPropertyDefinitions[elementClass] = properties;
        FormBuilder.elementTemplates[elementClass] = template;
    },

    initFormProperties: function(id) {
        var form = $(".form-container")[0];
        FormBuilder.updateElementDOM(form);
        if (id) {
            form.dom.properties.id = id;
        }
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
                FormBuilder.addColumn(section);
            }
        });

        // make columns sortable
        $(".form-section").sortable({
            connectWith: ".form-section",
            items: ".form-column",
            stop: function() {
                FormBuilder.generateJSON();
            },
            tolerance: "pointer",
            revertDuration: 100,
            revert: "invalid"
        }).disableSelection();

        // make cells sortable
        $(".form-column").sortable({
            connectWith: ".form-column",
            items: ".form-cell",
            stop: function() {
                FormBuilder.generateJSON();
            },
            tolerance: "pointer",
            revertDuration: 100,
            revert: "invalid"
        }).disableSelection();

        // make section dropabble
        $(".form-section").droppable({
            drop: function(event, ui) {
                var obj = $(ui.draggable);
                if (obj.hasClass("form-palette-column")) {
                    FormBuilder.decorateColumn(obj);
                } else if (obj.hasClass("form-column")) {
                    setTimeout(function() {
                        FormBuilder.initColumnSizes();
                        FormBuilder.initSectionsAndColumns();
                    }, 1000);
                }
            },
            activeClass: "form-cell-highlight",
            accept: ".form-palette-column, .form-column",
            greedy: true,
            tolerance: "touch"
        });

        // make column droppable
        $(".form-column").droppable({
            drop: function(event, ui) {
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

        FormBuilder.generateJSON();
    },

    initColumnSizes: function() {
        // recalculate column sizes
        $(".form-section").each(function() {
            // find number of columns
            var tempCount = $(this).children(".form-pallete-column").length; // ignore div from palette
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
            optionHtml += "<button class='form-palette-sec' title='"+get_fbuilder_msg("fbuilder.addSection")+"'>"+get_fbuilder_msg("fbuilder.addSection")+"</button>";
            optionHtml += "<button class='form-palette-col' title='"+get_fbuilder_msg("fbuilder.addColumn")+"'>"+get_fbuilder_msg("fbuilder.addColumn")+"</button>";
            optionHtml += "<button class='form-palette-edit' title='"+get_fbuilder_msg("fbuilder.editSection")+"'>"+get_fbuilder_msg("fbuilder.editSection")+"</button>";
            optionHtml += "<button class='form-palette-remove' title='"+get_fbuilder_msg("fbuilder.deleteSection")+"'>"+get_fbuilder_msg("fbuilder.deleteSection")+"</button>";
        } else if ($(obj).hasClass("form-column")) {
            // add buttons for column
            optionHtml += "<button class='form-palette-edit' title='"+get_fbuilder_msg("fbuilder.editColumn")+"'>"+get_fbuilder_msg("fbuilder.editColumn")+"</button>";
            if ($(obj).siblings(".form-column").length > 0) {
                optionHtml += "<button class='form-palette-remove' title='"+get_fbuilder_msg("fbuilder.deleteColumn")+"'>"+get_fbuilder_msg("fbuilder.deleteColumn")+"</button>";
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
            
        } else  {
            // add buttons for other elements
            optionHtml += "<button class='form-palette-edit' title='"+get_fbuilder_msg("fbuilder.edit")+"'>"+get_fbuilder_msg("fbuilder.edit")+"</button>";
            optionHtml += "<button class='form-palette-remove' title='"+get_fbuilder_msg("fbuilder.delete")+"'>"+get_fbuilder_msg("fbuilder.delete")+"</button>";
        }
        optionHtml += "</span><div class='form-clear'></div>";
        var optionDiv = $(optionHtml);

        // handle deletion
        $(optionDiv).children(".form-palette-remove").click(function() {
            if (confirm(get_fbuilder_msg("fbuilder.delete.confirm"))) {
                var element = $(this).parent().parent();
                FormBuilder.deleteElement(element);
            }
            return false;
        })

        // handle edit
        $(optionDiv).children(".form-palette-edit").click(function() {
            var element = $(this).parent().parent();
            // open properties dialog
            FormBuilder.editElementProperties(element);
            return false;
        });

        // handle add section
        $(optionDiv).children(".form-palette-sec").click(function() {
            var element = $(this).parent().parent();
            if (element.hasClass("form-section")) {
                if (true || confirm(get_fbuilder_msg("fbuilder.addSection.confirm"))) {
                    FormBuilder.addSection(element);
                }
            }
            return false;
        })

        // handle add column
        $(optionDiv).children(".form-palette-col").click(function() {
            var element = $(this).parent().parent();
            if (element.hasClass("form-section")) {
                if (true || confirm(get_fbuilder_msg("fbuilder.addColumn.confirm"))) {
                    FormBuilder.addColumn(element);
                }
            }
            return false;
        })

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
            tinyMceScript: FormBuilder.contextPath + FormBuilder.tinymceUrl,
            contextPath: FormBuilder.contextPath,
            propertiesDefinition : elementOptions,
            propertyValues : elementProperty,
            showCancelButton:true,
            cancelCallback: function() {
                FormBuilder.propertyDialog.hide();
                $("#form-property-editor").html("");
            },
            saveCallback: function(container, properties) {
                // hide dialog
                FormBuilder.propertyDialog.hide();

                // update element properties
                FormBuilder.updateElementProperties(element, properties);

                // refresh element UI
                FormBuilder.refreshElementTemplate(element);
                $("#form-property-editor").html("");
            }
        }

        // show popup dialog
        if (FormBuilder.propertyDialog == null) {
            FormBuilder.propertyDialog = new Boxy(
                '<div id="form-property-editor"></div>',
                {
                    title: 'Property Editor',
                    closeable: true,
                    draggable: false,
                    show: false,
                    fixed: true
                });
        }
        $("#form-property-editor").html("");
        FormBuilder.propertyDialog.show();
        $("#form-property-editor").propertyEditor(options);
        FormBuilder.propertyDialog.center('x');
        FormBuilder.propertyDialog.center('y');
    },

    updateElementProperties: function(element, properties) {
        // update element properties
        var dom = $(element)[0].dom;
        if (dom) {
            dom.properties = properties;
        }

        FormBuilder.generateJSON();
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

    retrieveElementHTML: function(element, dom, elementClass, elementProperty) {

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
                }
            });

            return get_fbuilder_msg("fbuilder.loading");
        }


        var html = $(element).html();
        return html;
    },

    deleteElement: function(element) {
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

    addSection: function(parent) {
        // add a new section
        var section = $("<div class='form-section' element-class='org.joget.apps.form.model.Section' element-property='{label:\""+get_fbuilder_msg("fbuilder.section")+"\"}'></div>");
        FormBuilder.decorateSection(section);
        if (parent) {
            // add after an existing section
            $(parent).after(section);
        } else {
            $(".form-container").append(section);
        }
        FormBuilder.initSectionsAndColumns();
        return section;
    },

    addColumn: function(section) {
        // add a new column
        var column = $("<div class='form-column' element-class='org.joget.apps.form.model.Column'></div>");
        $(section).append(column);
        FormBuilder.decorateColumn(column);
        FormBuilder.initSectionsAndColumns();
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

    generateJSON: function() {
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
        $("#form-json").text("");
        $("#form-json").text(json);

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
            tinyMceScript: FormBuilder.tinymceUrl,
            contextPath: FormBuilder.contextPath,
            propertiesDefinition: formOptions,
            propertyValues: formProperties,
            showCancelButton: false,
            closeAfterSaved: false,
            saveCallback: function(container, properties) {
                // update form properties
                FormBuilder.updateElementProperties(form, properties);

                // change to design tab
                $("#builder-step-design").trigger("click");
            }
        }
        $('#form-properties').propertyEditor(options);

        return false;
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
    }
}