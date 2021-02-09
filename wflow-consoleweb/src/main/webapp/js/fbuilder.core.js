FormBuilder = {

    existingFields: [],
    availableBinder : {},
    availableValidator : {},
    
    /*
     * Intialize the builder, called from CustomBuilder.initBuilder
     */
    initBuilder: function (callback) {
        
        $("#i18n-btn").after('<button class="btn btn-light" title="'+get_advtool_msg('adv.tool.tooltip')+'" id="tooltip-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="tooltip" data-cbuilder-action="switchView"><i class="lar la-question-circle"></i> </button>');
        $("#usages-btn").after('<button class="btn btn-light" title="'+get_advtool_msg('adv.tool.Table')+'" id="table-usage-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="tableUsage" data-cbuilder-action="switchView"><i class="las la-table"></i> </button>');
        
        CustomBuilder.Builder.init({
            callbacks : {
                "initComponent" : "FormBuilder.initComponent",
                "renderElement" : "FormBuilder.renderElement",
                "updateElementId" : "FormBuilder.updateElementId",
                "unloadElement" : "FormBuilder.unloadElement",
                "selectElement" : "FormBuilder.selectElement",
                "renderXray" : "FormBuilder.renderXray"
            }
        }, function() {
            CustomBuilder.Builder.setHead('<link data-fbuilder-style href="' + CustomBuilder.contextPath + '/css/form8.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<link data-userview-style href="' + CustomBuilder.contextPath + '/css/userview8.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<script data-fbuilder-script type="text/javascript" src="' + CustomBuilder.contextPath + '/js/json/formUtil.js" ></script>');
            callback();
            
            FormBuilder.initBinderList();
            FormBuilder.initValidatorList();
        });
    },
    
    /*
     * Load and render data, called from CustomBuilder.loadJson
     */
    load: function (data) {
        CustomBuilder.Builder.load(data);
    },
    
    /*
     * Retrieve the available binder plugins for render xray info
     */
    initBinderList : function(){
        $.getJSON(
            CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.apps.form.model.FormBinder',
            function(returnedData){
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        FormBuilder.availableBinder[returnedData[e].value] = returnedData[e].label;
                    }
                }
            }
        );
    },
    
    /*
     * Retrieve the available validator plugins for render xray info
     */
    initValidatorList : function(){
        $.getJSON(
            CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.apps.form.model.FormValidator',
            function(returnedData){
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        FormBuilder.availableValidator[returnedData[e].value] = returnedData[e].label;
                    }
                }
            }
        );
    },
    
    /*
     * Render form element plugin in palette, used in /fbuidler/formBuilder.jsp
     */
    initPaletteElement : function (category, className, label, icon, propertyOptions, defaultPropertiesValues, render, css, metaData, tab) {
        var found = false;
        if (propertyOptions !== null && propertyOptions !== undefined) {
            for (var i in propertyOptions) {
                if (propertyOptions[i].properties != null && propertyOptions[i].properties !== undefined) {
                    for (var j in propertyOptions[i].properties) {
                        if (propertyOptions[i].properties[j].name === "id" && propertyOptions[i].properties[j].js_validation === undefined) {
                            propertyOptions[i].properties[j].js_validation = 'FormBuilder.validateFieldId';
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
        
        CustomBuilder.initPaletteElement(category, className, label, icon, propertyOptions, defaultPropertiesValues, render, css, metaData, tab);
    },
    
    /*
     * Initialize the builder component behaviour for each form elements.
     * Called from CustomBuilder.Builder.getComponent
     */
    initComponent : function(component) {
        var self = CustomBuilder.Builder;
        
        if (component.className === "org.joget.apps.form.model.Form") {
            component.builderTemplate.parentContainerAttr = "";
            component.builderTemplate.childsContainerAttr = "sections";
        } else if (component.className === "org.joget.apps.form.model.Section") {
            component.builderTemplate.parentContainerAttr = "sections";
            component.builderTemplate.childsContainerAttr = "columns";
            component.icon = '<i class="las la-credit-card"></i>';
            component.builderTemplate.isPastable = function(elementObj, component) {
                var copied = CustomBuilder.getCopiedElement();
                if (copied !== null && copied !== undefined) {
                    var copiedComponent = CustomBuilder.Builder.getComponent(copied.object.className);
                    if (copiedComponent.builderTemplate.getParentContainerAttr() === "sections" || copiedComponent.builderTemplate.getParentContainerAttr() === "columns") {
                        return true;
                    }
                }
                return false;
            };
            component.builderTemplate.afterRemoved = function(parent, elementObj, component) {
                var parentDataArray = $(parent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)];
                if (parentDataArray.length === 0) {
                    self.selectedEl = parent;
                    FormBuilder.addSection(true, true);
                }
            };
        } else if (component.className === "org.joget.apps.form.model.Column") {
            component.builderTemplate.parentContainerAttr = "columns";
            component.builderTemplate.childsContainerAttr = "elements";
            component.icon = '<i class="fas fa-columns"></i>';
            component.builderTemplate.afterRemoved = function(parent, elementObj, component) {
                var parentDataArray = $(parent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)];
                if (parentDataArray.length === 0) {
                    self.selectedEl = parent;
                    FormBuilder.addColumn(true);
                }
            };
        } else {
            component.builderTemplate.stylePropertiesDefinition.push({
                title:'Others',
                properties:[
                    {
                        name : 'css-classes',
                        label : 'CSS Classes',
                        type : 'textfield'
                    },
                    {
                        name : 'css-label-position',
                        label : 'Label Position',
                        type : 'selectbox',
                        options : [
                            {value : '', label : 'Default'},
                            {value : 'label-left', label : 'Left'},
                            {value : 'label-top', label : 'Top'}
                        ]
                    }
                ]
            });
        }
    },
    
    /*
     * A callback method called from CustomBuilder.applyElementProperties when properties saved
     */
    saveEditProperties : function(container, elementProperty, elementObj, element) {
        if (elementProperty['readonly'] === "true") {
            elementProperty['permissionHidden'] = "";
        }
        
        if (elementObj.className === "org.joget.apps.form.model.Column") {
            //recalculate width
            FormBuilder.recalculateColumnWidth(element.parent().closest("[data-cbuilder-classname]"));
        }
    },
    
    /*
     * A callback method called from the default component.builderTemplate.render method,
     * It will render the form element by using ajax to retrieve the template
     */
    renderElement : function(element, elementObj, component, callback) {
        if (component.builderTemplate.getHtml() === undefined) {
            //load by ajax
            var jsonStr = JSON.encode(elementObj);
            CustomBuilder.cachedAjax({
                type: "POST",
                data: {"json": jsonStr },
                url: CustomBuilder.contextPath + '/web/fbuilder/app/' + CustomBuilder.appId + '/' + CustomBuilder.appVersion + '/form/'+ CustomBuilder.id + '/element/preview/',
                dataType : "text",
                beforeSend: function (request) {
                   request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                },
                success: function(response) {
                    var newElement = $(response);
                    
                    $(newElement).find(".form-section").attr("data-cbuilder-columns", "");
                    $(newElement).find(".form-column").attr("data-cbuilder-elements", "");
                    $(newElement).find("[data-cbuilder-classname='org.joget.apps.form.lib.HiddenField']").attr("data-cbuilder-element-invisible", "");
                    if ($(newElement).hasClass("form-section")) {
                        $(newElement).attr("data-cbuilder-columns", "");
                    }
                    if ($(newElement).hasClass("form-column")) {
                        $(newElement).attr("data-cbuilder-elements", "");
                    }
                    if (component.className === "org.joget.apps.form.lib.HiddenField") {
                        $(newElement).attr("data-cbuilder-element-invisible", "");
                    }
                    
                    if (component.className === "org.joget.apps.form.model.Form") {
                        var wrapper = $("<div id=\"form-canvas\"></div>");
                        wrapper.append(newElement);
                        wrapper.find("form").attr("data-cbuilder-uneditable", "").attr("data-cbuilder-sections", "");
                        newElement = wrapper.find("form");
                        $(wrapper).find("> *:not(form)").remove();
                        $(newElement).find("> *:not(.form-section)").remove();
                        
                        $(element).replaceWith(wrapper);
                    } else {
                        $(element).replaceWith(newElement);
                    }
                    callback(newElement);
                }
            });
        } else {
            callback(element);
        }
    },
    
    /*
     * A callback method called from the default component.builderTemplate.unload method
     */
    unloadElement : function(element, elementObj, component) {
        var unloadMethod = elementObj.className.replace(/\./g, "_") + "_formBuilderElementUnLoad";
        
        if($.isFunction(window[unloadMethod])){
            window[unloadMethod](element);
        }
    },
    
    /*
     * A callback method called from the default component.builderTemplate.selectNode method.
     * It used to add column and add section action button when a section is selected
     */
    selectElement : function(element, elementObj, component) {
        if (elementObj.className === "org.joget.apps.form.model.Section") {
            $("#element-select-box #element-options").append('<a id="columns-btn" href="" title="'+get_cbuilder_msg("fbuilder.addColumn")+'"><i class="las la-columns"></i></a>');
            
            $("#element-select-box #element-bottom-actions").append('<a id="add-section-btn" href="" title="'+get_cbuilder_msg("fbuilder.addSection")+'"><i class="las la-plus"></i></a>');
            
            $("#columns-btn").off("click");
            $("#columns-btn").on("click", function(event) {
                $("#element-select-box").hide();
                
                FormBuilder.addColumn();
                
                event.preventDefault();
                return false;
            });
            
            $("#add-section-btn").off("click");
            $("#add-section-btn").on("click", function(event) {
                $("#element-select-box").hide();
                
                FormBuilder.addSection();
                
                event.preventDefault();
                return false;
            });
        }
    },
    
    /*
     * A callback method called from the CustomBuilder.Builder.renderNodeAdditional
     * It used to render the info of an element
     */
    renderXray : function(detailsDiv, element, elementObj, component , callback) {
        var dl = detailsDiv.find('dl');
        if (elementObj['properties']['loadBinder'] !== undefined && elementObj['properties']['loadBinder']['className'] !== "" && elementObj['properties']['loadBinder']['className'] !== "org.joget.apps.form.lib.WorkflowFormBinder") {
            var label = elementObj['properties']['loadBinder']['className'];
            if (FormBuilder.availableBinder[label]  !== undefined) {
                label = FormBuilder.availableBinder[label];
            } else {
                label += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
            }
            dl.append('<dt><i class="las la-upload" title="'+get_advtool_msg('dependency.tree.Load.Binder')+'"></i></dt><dd>'+label+'</dd>');
        }
        if (elementObj['properties']['storeBinder'] !== undefined && elementObj['properties']['storeBinder']['className'] !== "" && elementObj['properties']['storeBinder']['className'] !== "org.joget.apps.form.lib.WorkflowFormBinder") {
            var label = elementObj['properties']['storeBinder']['className'];
            if (FormBuilder.availableBinder[label]  !== undefined) {
                label = FormBuilder.availableBinder[label];
            } else {
                label += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
            }
            dl.append('<dt><i class="las la-download" title="'+get_advtool_msg('dependency.tree.Store.Binder')+'"></i></dt><dd>'+label+'</dd>');
        }
        if (elementObj['properties']['optionsBinder'] !== undefined && elementObj['properties']['optionsBinder']['className'] !== "") {
            var label = elementObj['properties']['optionsBinder']['className'];
            if (FormBuilder.availableBinder[label]  !== undefined) {
                label = FormBuilder.availableBinder[label];
            } else {
                label += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
            }
            dl.append('<dt><i class="las la-upload" title="'+get_advtool_msg('dependency.tree.Options.Binder')+'"></i></dt><dd>'+label+'</dd>');
        }
        if (elementObj['properties']['validator'] !== undefined && elementObj['properties']['validator']['className'] !== "") {
            var label = elementObj['properties']['validator']['className'];
            if (FormBuilder.availableValidator[label]  !== undefined) {
                label = FormBuilder.availableValidator[label];
            } else {
                label += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
            }
            dl.append('<dt><i class="las la-asterisk" title="'+get_advtool_msg('dependency.tree.Validator')+'"></i></dt><dd>'+label+'</dd>');
        }
        var permissionLabel = [];
        if (elementObj['properties']['permission'] !== undefined && elementObj['properties']['permission']['className'] !== "") {
            var label = elementObj['properties']['permission']['className'];
            if (CustomBuilder.availablePermission[label]  !== undefined) {
                label = CustomBuilder.availablePermission[label];
            } else {
                label += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
            }
            permissionLabel.push(label);
        }
        if (elementObj['properties']['permission_rules'] !== undefined) {
            for (var key of Object.keys(elementObj['properties']['permission_rules'])) {
                var rule = elementObj['properties']['permission_rules'][key];
                if (rule['permission'] !== undefined && rule['permission']['className'] !== "") {
                    var label = rule['permission']['className'];
                    if (FormBuilder.availablePermission[label]  !== undefined) {
                        label = FormBuilder.availablePermission[label];
                    } else {
                        label += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
                    }
                    if ($.inArray(label, permissionLabel) === -1) {
                        permissionLabel.push(label);
                    }
                }
            }
        }
        if (permissionLabel.length > 0) {
            dl.append('<dt><i class="las la-lock" title="'+get_advtool_msg('dependency.tree.Permission')+'"></i></dt><dd>'+permissionLabel.join(', ')+'</dd>');
        }
        
        if (elementObj['properties']['comment'] !== undefined && elementObj['properties']['comment'] !== "") {
            dl.append('<dt><i class="lar la-comment" title="Comment"></i></dt><dd>'+elementObj['properties']['comment']+'</dd>');
        }
        
        callback();
    },
    
    /*
     * A callback method called from the CustomBuilder.Builder.renderPermission
     * It used to render the permission option of an element
     */
    renderPermission : function (detailsDiv, element, elementObj, component, permissionObj, callback) {
        var dl = detailsDiv.find('dl');
        
        if (elementObj.className === "org.joget.apps.form.model.Column") {
            //do nothing
        } else {
            if (elementObj.className === "org.joget.apps.form.model.Section") {
                var className = "";
                if (permissionObj["permission"] !== undefined 
                    && permissionObj["permission"]["className"] !== undefined  
                    && permissionObj["permission"]["className"] !== "") {

                    className = permissionObj["permission"]["className"];
                }
                
                dl.append('<dt class="authorized-row" ><i class="las la-lock-open" title="'+get_advtool_msg('adv.permission.authorized')+'"></i></dt><dd class="authorized-row" ><div class="authorized-btns btn-group"></div></dd>');
                dl.find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm visible-btn">'+get_advtool_msg("adv.permission.visible")+'</button>');
                dl.find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm readonly-btn">'+get_advtool_msg("adv.permission.readonly")+'</button>');
                dl.find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm hidden-btn">'+get_advtool_msg("adv.permission.hidden")+'</button>');
            
                if (permissionObj["readonly"] === "true") {
                    dl.find(".authorized-btns .readonly-btn").addClass("active");
                    $(element).find("[data-cbuilder-classname] .authorized-row .btn").attr("disabled", "");
                } else if (permissionObj["permissionHidden"] === "true") {
                    dl.find(".authorized-btns .hidden-btn").addClass("active");
                    $(element).find("[data-cbuilder-classname] .authorized-row .btn").attr("disabled", "");
                } else {
                    dl.find(".authorized-btns .visible-btn").addClass("active");
                }
            
                dl.append('<dt class="unauthorized-row" ><i class="las la-lock" title="'+get_advtool_msg('adv.permission.unauthorized')+'"></i></dt><dd class="unauthorized-row" ><div class="unauthorized-btns btn-group"></div></dd>');
                    
                dl.find(".unauthorized-btns").append('<button type="button" class="btn btn-outline-danger btn-sm readonly-btn">'+get_advtool_msg("adv.permission.readonly")+'</button>');
                dl.find(".unauthorized-btns").append('<button type="button" class="btn btn-outline-danger btn-sm hidden-btn">'+get_advtool_msg("adv.permission.hidden")+'</button>');
            
                if (className !== "") {
                    if (permissionObj["permissionReadonly"] === "true") {
                        dl.find(".unauthorized-btns .readonly-btn").addClass("active");
                    } else {
                        dl.find(".unauthorized-btns .hidden-btn").addClass("active");
                        $(element).find("[data-cbuilder-classname] .unauthorized-row .btn").attr("disabled", "");
                    }
                } else {
                    dl.find(".unauthorized-btns .btn").attr("disabled", "");
                    $(element).find("[data-cbuilder-classname] .unauthorized-row .btn").attr("disabled", "");
                }
                
            } else {
                dl.append('<dt class="authorized-row" ><i class="las la-lock-open" title="'+get_advtool_msg('adv.permission.authorized')+'"></i></dt><dd class="authorized-row" ><div class="authorized-btns btn-group"></div></dd>');
                dl.find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm visible-btn">'+get_advtool_msg("adv.permission.visible")+'</button>');
                dl.find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm readonly-btn">'+get_advtool_msg("adv.permission.readonly")+'</button>');
                dl.find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm hidden-btn">'+get_advtool_msg("adv.permission.hidden")+'</button>');
            
                if (permissionObj["readonly"] === "true") {
                    dl.find(".authorized-btns .readonly-btn").addClass("active");
                } else if (permissionObj["permissionHidden"] === "true") {
                    dl.find(".authorized-btns .hidden-btn").addClass("active");
                } else {
                    dl.find(".authorized-btns .visible-btn").addClass("active");
                }
                
                dl.append('<dt class="unauthorized-row" ><i class="las la-lock" title="'+get_advtool_msg('adv.permission.unauthorized')+'"></i></dt><dd class="unauthorized-row" ><div class="unauthorized-btns btn-group"></div></dd>');
                    
                dl.find(".unauthorized-btns").append('<button type="button" class="btn btn-outline-danger btn-sm readonly-btn">'+get_advtool_msg("adv.permission.readonly")+'</button>');
                dl.find(".unauthorized-btns").append('<button type="button" class="btn btn-outline-danger btn-sm hidden-btn">'+get_advtool_msg("adv.permission.hidden")+'</button>');
            
                if (permissionObj["permissionReadonlyHidden"] === "true") {
                    dl.find(".unauthorized-btns .hidden-btn").addClass("active");
                } else {
                    dl.find(".unauthorized-btns .readonly-btn").addClass("active");
                }
            }
            
            dl.on("click", ".btn", function(event) {
                if ($(this).hasClass("active")) {
                    return false;
                }
                
                var group = $(this).closest(".btn-group");
                group.find(".active").removeClass("active");
                $(this).addClass("active");
                
                if (group.hasClass("unauthorized-btns")) {
                    if (elementObj.className === "org.joget.apps.form.model.Section") {
                        if ($(this).hasClass("readonly-btn")) {
                            permissionObj["permissionReadonly"] = "true";
                            $(element).find("[data-cbuilder-classname] .unauthorized-row .btn").removeAttr("disabled");
                        } else {
                            permissionObj["permissionReadonly"] = "";
                            $(element).find("[data-cbuilder-classname] .unauthorized-row .btn").attr("disabled", "");
                        }
                    } else {
                        if ($(this).hasClass("readonly-btn")) {
                            permissionObj["permissionReadonlyHidden"] = "";
                        } else {
                            permissionObj["permissionReadonlyHidden"] = "true";
                        }
                    }
                } else {
                    if ($(this).hasClass("visible-btn")) {
                        permissionObj["readonly"] = "";
                        permissionObj["permissionHidden"] = "";
                    } else if ($(this).hasClass("readonly-btn")) {
                        permissionObj["readonly"] = "true";
                        permissionObj["permissionHidden"] = "";
                    } else {
                        permissionObj["readonly"] = "";
                        permissionObj["permissionHidden"] = "true";
                    }
                    
                    if (elementObj.className === "org.joget.apps.form.model.Section") {
                        if ($(this).hasClass("visible-btn")) {
                            $(element).find("[data-cbuilder-classname] .authorized-row .btn").removeAttr("disabled");
                        } else {
                            $(element).find("[data-cbuilder-classname] .authorized-row .btn").attr("disabled", "");
                        }
                    }
                }
                CustomBuilder.update();
                
                event.preventDefault();
                return false;
            });
        }
        
        callback();
    },
    
    /*
     * To add a column when an add column action button is clicked
     */
    addColumn: function(noUpdate) {
        var self = CustomBuilder.Builder;
        
        self.component = self.getComponent("org.joget.apps.form.model.Column");
        var classname = self.component.className;
        var properties = {width: "100%"};
        var elements = [];
        if (self.component.properties !== undefined) {
            properties = $.extend(true, properties, self.component.properties);
        }
        if (self.component.builderTemplate.getElements !== undefined) {
            elements = $.extend(true, elements, self.component.builderTemplate.getElements());
        }
        var elementObj = {
            className: classname,
            properties: properties,
            elements : elements
        };
        self.updateElementId(elementObj);
        
        var parent = self.selectedEl;
        var parentDataArray = $(parent).data("data").elements;
        parentDataArray.push(elementObj);
        
        //recalculate width
        FormBuilder.recalculateColumnWidth(parent);
        
        var temp = $('<div></div>');
        self.selectedEl.append(temp);
        
        self.renderElement(elementObj, temp, self.component, true);
        
        if (noUpdate === undefined || !noUpdate) {
            CustomBuilder.update();
        }
    },
    
    /*
     * recalculate all the column width in section and update it accordingly
     */
    recalculateColumnWidth : function(section) {
        var sectionDataArray = $(section).data("data").elements;
        
        var columnCount = sectionDataArray.length;
        var customWidthStr = "";
        for (var i in sectionDataArray) {
            var columnObj = sectionDataArray[i];
            var customWidth = columnObj.properties.customWidth;
            if (customWidth && customWidth !== "") {
               if (customWidthStr === "") {
                   customWidthStr = "(100% - ";
               } else {
                   customWidthStr += " - ";
               }
               customWidthStr += customWidth;
               columnCount--;
            }
        }
        if (customWidthStr !== "") {
            customWidthStr += ")";
            customWidthStr = "calc(" + customWidthStr + "/" + columnCount + ")";
        } else {
            var columnWidth = (columnCount !== 1) ? (Math.floor(100 / columnCount)) : 100;
            customWidthStr = columnWidth + "%";
        }

        $(section).find("> .form-column").each(function(){
            var column = $(this);
            var width = customWidthStr;
            var columnObj = $(column).data("data");
            var customWidth = columnObj.properties.customWidth;
            if (customWidth && customWidth != "") {
                width = customWidth;
            }
            $(column).css("width", width);
            columnObj.properties.width = width;
        });
        
        if ($(section).find("> .form-column").length !== sectionDataArray.length) {
            sectionDataArray[sectionDataArray.length - 1].properties.width = customWidthStr;
        }
    },
    
    /*
     * To add a section when an add section action button is clicked
     */
    addSection: function(noUpdate, isForm){
        var self = CustomBuilder.Builder;
        
        self.component = self.getComponent("org.joget.apps.form.model.Section");
        var classname = self.component.className;
        var properties = {};
        var elements = [];
        if (self.component.properties !== undefined) {
            properties = $.extend(true, properties, self.component.properties);
        }
        if (self.component.builderTemplate.getElements !== undefined) {
            elements = $.extend(true, elements, self.component.builderTemplate.getElements());
        }
        elements.push({
            className : 'org.joget.apps.form.model.Column',
            properties : { width : '100%'},
            elements : []
        });
        var elementObj = {
            className: classname,
            properties: properties,
            elements : elements
        };
        elementObj.properties.label = self.component.label;
        self.updateElementId(elementObj);
        
        var parent = self.selectedEl.parent().closest("[data-cbuilder-classname]");
        
        if (isForm) {
            parent = self.selectedEl;
        }
        
        var parentDataArray = $(parent).data("data").elements;
        var newIndex = $.inArray($(self.selectedEl).data("data"), parentDataArray) + 1;
        parentDataArray.splice(newIndex, 0, elementObj);
        
        var temp = $('<div></div>');
        if (isForm) {
            self.selectedEl.append(temp);
        } else {
            self.selectedEl.after(temp);
        }
        
        self.renderElement(elementObj, temp, self.component, true);
        
        if (noUpdate === undefined || !noUpdate) {
            CustomBuilder.update();
        }
    },
    
    /*
     * A callback method from CustomBuilder.Builder.updateElementId to update id to an unqiue value
     */
    updateElementId : function(elementObj) {
        // set ID if empty or it is copied section
        var elementId = elementObj.properties.id;
        var elementClass = elementObj.className;
        if (typeof elementId === "undefined" || elementId === "" || elementClass === "org.joget.apps.form.model.Section") {
            // determine element class
            if (elementClass === "org.joget.apps.form.model.Section") {
                elementClass = "form-section";
                prefix = "section";
            } else if (elementClass === "org.joget.apps.form.model.Column") {
                return;
            } else {
                elementClass = "form-cell";
                prefix = "field";
            } 

            // generate ID
            var elements = CustomBuilder.Builder.frameBody.find("." + elementClass);
            var newIndex = elements.length + 1;
            var newId = prefix + newIndex;
            
            while (CustomBuilder.Builder.frameBody.find("[data-cbuilder-id="+newId+"]").length > 0) {
                newIndex++;
                newId = prefix + newIndex;
            }

            // set ID
            elementObj.properties.id = newId;
        }
    },
    
    /*
     * A callback method from CustomBuilder.switchView to render tooltip editor
     */
    tooltipViewInit: function(view) {
        if ($(view).find(".i18n_table").length === 0) {
            $(view).html("");
            $(view).prepend('<i class="dt-loading fas fa-5x fa-spinner fa-spin"></i>');
            
            I18nEditor.init($(view), $("#cbuilder-info").find('textarea[name="json"]').val(), $.extend(true, {
                skip : function(key, obj) {
                    if (key === "properties" && (obj.className === "org.joget.apps.form.model.Form" || obj.className === "org.joget.apps.form.model.Section" || obj.className === "org.joget.apps.form.model.Column")) {
                        return true;
                    }
                    return false;
                },
                key : function(key, obj) {
                    return "tooltip." + CustomBuilder.id + "." + obj.id;
                },
                label : function(label, obj) {
                    return label + " (" + obj.id + ")";
                },
                sort : false,
                i18nHash : false,
                loadEnglish : true
            }, CustomBuilder.advancedToolsOptions));
            
            $(view).find(".dt-loading").remove();
            
            $("#cbuilder-info").find('textarea[name="json"]').off("change.i18n");
            $("#cbuilder-info").find('textarea[name="json"]').on("change.i18n", function() {
                $(view).html("");
            });
        }
        setTimeout(function(){
            I18nEditor.refresh($(view));
        }, 5);
    },
    
    /*
     * A callback method from CustomBuilder.switchView to render table usage view
     */
    tableUsageViewInit: function(view) {
        $(view).html('<div class="column_names"><h3>'+get_advtool_msg('adv.tool.Table.Columns')+'</h3>\
            <div class="usage_content"><i class="las la-spinner la-3x la-spin" style="opacity:0.3"></i></div></div>\
            <div class="sameapp_usage"><h3>'+get_advtool_msg('adv.tool.Table.Usage')+'</h3>\
            <div class="usage_content"></div></div>\
            <div class="diffapp_usage"><h3>'+get_advtool_msg('adv.tool.Table.Usage.otherApp')+'</h3>\
            <div class="usage_content"></div></div>');
        
        CustomBuilder.cachedAjax({
            method: "POST",
            url: CustomBuilder.contextPath + '/web/json/console/app'+CustomBuilder.appPath+'/builder/binder/columns',
            data : {
                binderJson: '{"formDefId":"'+CustomBuilder.data.properties.id+'"}',
                id: 'getColumns',
                binderId:'org.joget.apps.datalist.lib.FormRowDataListBinder'
            },
            dataType : "json",
            success: function(resp) {
                if (resp.columns.length > 0) {
                    var ul = $('<ul class="table_column_items">');
                    var fields = [];
                    $(view).find(".column_names .usage_content").append(ul);
                    for (var i in resp.columns) {
                        fields.push(resp.columns[i]['name']);
                    }
                    fields.sort();
                    for (var i in fields) {
                        $(ul).append('<li>'+fields[i]+'</li>');
                    }
                } else {
                    $(view).find(".column_names .usage_content").append('<ul><li class="no_usage"><h3>'+get_advtool_msg('table.column.noExistingColumns')+'</h3></li></ul>');
                }
            },
            complete: function() {
                $(view).find(".column_names .usage_content i.la-spinner").remove();
            }
        });
        
        Usages.render($(view).find('.sameapp_usage .usage_content'), CustomBuilder.data.properties.tableName, "table", CustomBuilder.advancedToolsOptions);
        Usages.renderOtherApp($(view).find('.diffapp_usage .usage_content'), CustomBuilder.data.properties.tableName, "table", CustomBuilder.advancedToolsOptions);
    },
    
    /*
     * Utility method to check there is no form element in the form
     */
    isEmpty : function() {
        return (CustomBuilder.Builder.frameBody.find(".form-column .form-cell").length === 0);
    },
    
    /*
     * Utility method to check is there an unsaved changes
     */
    isSaved : function(){
        return CustomBuilder.isSaved();
    },
    
    /*
     * Utility method to retrieve all fields id of the current form table
     */
    retrieveExistingFieldIds : function() {
        var tableName = CustomBuilder.data.properties['tableName'];
        
        CustomBuilder.cachedAjax({
            url: CustomBuilder.contextPath + '/web/json/console/app/'+CustomBuilder.appId+'/'+CustomBuilder.appVersion+'/form/columns/options?tableName='+tableName,
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
    
    /*
     * Utility method to retrieve all fields id in form of current canvas
     */
    getFieldIds : function(includeGridColumn) {
        var ids = [];
        CustomBuilder.Builder.frameBody.find(".form-column .form-cell").each(function(){
            var data = $(this).data("data");
            var className = data.className;
            var property = data.properties;
            
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
    
    /*
     * Utility method to get a list of all field id and label to populate field selection
     */
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
    
    /*
     * Utility method to get all field id and label in canvas to populate field selection
     */
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
    
    /*
     * Utility method to get all field (Including grid column) id and label in canvas to populate field selection
     */
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
    
    /*
     * Refresh the navigator of the design app
     */
    refreshAppDesign: function() {
        if (window.opener && window.opener.refreshNavigator) {
            window.opener.location.reload(true);
        }
    },
    
    /*
     * Utility method to validate the field id to prevent having the same with reserve keywords
     */
    validateFieldId: function(name, value) {
        if ($.inArray(value, ["appId","appVersion","version","userviewId","menuId","key","embed"]) >= 0) {
            return get_cbuilder_msg("fbuilder.reserveIds");
        }
        return null;    
    },
      
    /*
     * remove dynamically added items    
     */            
    unloadBuilder : function() {
        $("#tooltip-btn, #table-usage-btn").remove();
        $("#generator-btn").parent().remove();
    } 
}