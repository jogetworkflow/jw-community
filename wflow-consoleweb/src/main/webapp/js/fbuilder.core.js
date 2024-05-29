FormBuilder = {

    existingFields: [],
    availableBinder : {},
    availableValidator : {},
    
    /*
     * Intialize the builder, called from CustomBuilder.initBuilder
     */
    initBuilder: function (callback) {
        
        //add button to edit form style
        $("#noviewport-view").after('<button class="btn btn-light" id="formstyle-btn" type="button" title="'+get_cbuilder_msg('style.defaultStyles')+'"><i class="las la-palette"></i></button>');
        $("#formstyle-btn").off("click");
        $("#formstyle-btn").on("click", function(event){
            if (!($("#design-btn").is(".active-view") || $("#treeviewer-btn").is(".active-view") || $("#xray-btn").is(".active-view"))) {
                $("#design-btn").trigger("click");
            }
            FormBuilder.editDefaultStyle(event);
            return false;
        });
        
        $("#i18n-btn").after('<button class="btn btn-light" title="'+get_advtool_msg('adv.tool.tooltip')+'" id="tooltip-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="tooltip" data-cbuilder-action="switchView" data-view-control><i class="lar la-question-circle"></i> </button>');
        $("#usages-btn").after('<button class="btn btn-light" title="'+get_advtool_msg('adv.tool.Table')+'" id="table-usage-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="tableUsage" data-cbuilder-action="switchView" data-view-control><i class="las la-table"></i> </button>');
        
        CustomBuilder.Builder.init({
            callbacks : {
                "initComponent" : "FormBuilder.initComponent",
                "renderElement" : "FormBuilder.renderElement",
                "updateElementId" : "FormBuilder.updateElementId",
                "unloadElement" : "FormBuilder.unloadElement",
                "decorateBoxActions" : "FormBuilder.decorateBoxActions",
                "renderXray" : "FormBuilder.renderXray",
                "copyElement" : "FormBuilder.copyElement",
                "pasteElement" : "FormBuilder.pasteElement"
            }
        }, function() {
            CustomBuilder.Builder.setHead('<link data-fbuilder-style href="' + CustomBuilder.contextPath + '/css/form8.css" rel="stylesheet" />');
            if (CustomBuilder.systemTheme === undefined) {
                CustomBuilder.systemTheme = $('body').attr("builder-theme");
            }
            if (CustomBuilder.systemTheme === 'dark') {
                CustomBuilder.Builder.setHead('<link data-userview-style href="' + CustomBuilder.contextPath + '/css/darkTheme.css" rel="stylesheet" />');
            }
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
        CustomBuilder.Builder.load(data, function(){
           FormBuilder.afterUpdate(); 
           FormBuilder.retrieveExistingFieldIds();
        });
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
        var found = 0;
        var idPos = 0;
        var labelPos = 0;
        if (propertyOptions !== null && propertyOptions !== undefined) {
            for (var i in propertyOptions) {
                if (propertyOptions[i].properties !== null && propertyOptions[i].properties !== undefined) {
                    for (var j in propertyOptions[i].properties) {
                        if (propertyOptions[i].properties[j].name === "id" && propertyOptions[i].properties[j].js_validation === undefined) {
                            propertyOptions[i].properties[j].js_validation = 'FormBuilder.validateFieldId';
                            propertyOptions[i].properties[j].id_suggestion = "label";
                            found++;
                            idPos = j;
                        }
                        if (propertyOptions[i].properties[j].name === "label") {
                            found++;
                            labelPos = j;
                        }
                        if (found === 2) {
                            break;
                        }
                    }
                }
                if (found === 2) {
                    //swape position of id & label if id come before label
                    if (labelPos > idPos) {
                        propertyOptions[i].properties[idPos] = propertyOptions[i].properties.splice(labelPos, 1, propertyOptions[i].properties[idPos])[0];
                    }
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
            component.builderTemplate.getStylePropertiesDefinition = function(elementObj, component) {
                return component.builderTemplate.sectionStylePropertiesDefinition;
            };
            component.builderTemplate.sectionStylePropertiesDefinition = $.extend(true, [], 
                self.generateStylePropertiesDefinition("", [
                    {'prefix' : '', 'label' : get_cbuilder_msg('fbuilder.form')},
                    {'prefix' : 'section', 'label' : get_cbuilder_msg('fbuilder.section')}, 
                    {'prefix' : 'section-header', 'label' : get_cbuilder_msg('fbuilder.sectionHeader')},
                    {'prefix' : 'section-fieldLabel', 'label' : get_cbuilder_msg('fbuilder.fieldLabel')},
                    {'prefix' : 'section-fieldInput', 'label' : get_cbuilder_msg('fbuilder.fieldInput')}
                ]));
                
            component.builderTemplate.sectionStylePropertiesDefinition.push({
                title:'Others',
                properties:[
                    {
                        name : 'css-label-position',
                        label : get_cbuilder_msg('fbuilder.fieldLabelPosition'),
                        type : 'selectbox',
                        options : [
                            {value : '', label : 'Default'},
                            {value : 'label-left', label : 'Left'},
                            {value : 'label-top', label : 'Top'}
                        ],
                        viewport : 'desktop'
                    },
                    {
                        name : 'css-tablet-label-position',
                        label : get_cbuilder_msg('fbuilder.fieldLabelPosition'),
                        type : 'selectbox',
                        options : [
                            {value : '', label : 'Default'},
                            {value : 'tablet-label-left', label : 'Left'},
                            {value : 'tablet-label-top', label : 'Top'}
                        ],
                        viewport : 'tablet'
                    }
                ]
            }); 
                        
        } else if (component.className === "org.joget.apps.form.model.Section") {
            component.builderTemplate.parentContainerAttr = "sections";
            component.builderTemplate.childsContainerAttr = "columns";
            component.icon = '<i class="las la-credit-card"></i>';
            component.builderTemplate.isPastable = function(elementObj, component) {
                var copied = CustomBuilder.getCopiedElement();
                if (copied !== null && copied !== undefined) {
                    var copiedComponent = CustomBuilder.Builder.getComponent(copied.object.className);
                    if (copiedComponent.builderTemplate.getParentContainerAttr() === "sections" || copiedComponent.builderTemplate.getParentContainerAttr() === "columns" || copiedComponent.builderTemplate.getParentContainerAttr() === "elements") {
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
            component.builderTemplate.renderPermission = FormBuilder.renderPermission;
            component.builderTemplate.stylePropertiesDefinition = $.extend(true, [], self.generateStylePropertiesDefinition("", [{}, {'prefix' : 'header', 'label' : get_cbuilder_msg('ubuilder.header')}]));
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
                FormBuilder.recalculateColumnWidth($(parent));
            };
        } else {
            component.builderTemplate.getStylePropertiesDefinition = FormBuilder.getStylePropertiesDefinition;
            component.builderTemplate.renderPermission = FormBuilder.renderPermission;            
            
            component.builderTemplate.stylePropertiesDefinition.push({
                title: get_cbuilder_msg('fbuilder.others'),
                properties:[
                    {
                        name : 'css-classes',
                        label : get_cbuilder_msg('fbuilder.cssClasses'),
                        type : 'textfield'
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
        } else if (elementObj.className === "org.joget.apps.form.model.Form") {
            FormBuilder.retrieveExistingFieldIds();
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
                        $(newElement).find("> *:not(.form-section):not(style)").remove();
                        
                        $(element).replaceWith(wrapper);
                        CustomBuilder.Builder.recursiveCheckVisible(newElement);
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
     * A callback method called from the default component.builderTemplate.decorateBoxActions method.
     * It used to add column and add section action button when a section is selected
     */
    decorateBoxActions : function(element, elementObj, component, box) {
        var builder = CustomBuilder.Builder;
        
        if (elementObj.className === "org.joget.apps.form.model.Section") {
            $(box).find(".element-options").append('<a class="columns-btn" title="'+get_cbuilder_msg("fbuilder.addColumn")+'"><i class="las la-columns"></i></a><a class="default-style-btn" title="'+get_cbuilder_msg('style.defaultStyles')+'" style=""><i class="las la-palette"></i></a>');
            
            $(box).find(".element-bottom-actions").append('<a class="add-section-btn" href="" title="'+get_cbuilder_msg("fbuilder.addSection")+'"><i class="las la-plus"></i></a>');
            
            $(box).find(".columns-btn").off("click");
            $(box).find(".columns-btn").on("click", function(event) {
                builder.boxActionSetElement(event);
                
                FormBuilder.addColumn();
                
                event.preventDefault();
                return false;
            });
            
            $(box).find(".add-section-btn").off("click");
            $(box).find(".add-section-btn").on("click", function(event) {
                builder.boxActionSetElement(event);
                
                FormBuilder.addSection();
                
                event.preventDefault();
                return false;
            });
            
            $(box).find(".default-style-btn").off("click");
            $(box).find(".default-style-btn").on("click", function(event){
                FormBuilder.editDefaultStyle(event);
                return false;
            });
        }
    },
    
    /*
     * Show the property panel to edit default form style
     */
    editDefaultStyle: function(event) {
        var builder = CustomBuilder.Builder;
        
        builder.boxActionSetElement(event);
                
        $("body").removeClass("no-right-panel");
        $("#element-properties-tab-link").hide();
        $("#right-panel #element-properties-tab").find(".property-editor-container").remove();

        builder.editStyles(CustomBuilder.data.properties, builder.frameBody.find("form"), CustomBuilder.data, builder.parseDataToComponent(CustomBuilder.data));
        $("#style-properties-tab-link a").trigger("click");
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
                label = '<span class="missing-plugin">' + label + '</span>';
            }
            dl.append('<dt><i class="las la-upload" title="'+get_advtool_msg('dependency.tree.Load.Binder')+'"></i></dt><dd>'+label+'</dd>');
        }
        if (elementObj['properties']['storeBinder'] !== undefined && elementObj['properties']['storeBinder']['className'] !== "" && elementObj['properties']['storeBinder']['className'] !== "org.joget.apps.form.lib.WorkflowFormBinder") {
            var label = elementObj['properties']['storeBinder']['className'];
            if (FormBuilder.availableBinder[label]  !== undefined) {
                label = FormBuilder.availableBinder[label];
            } else {
                label += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
                label = '<span class="missing-plugin">' + label + '</span>';
            }
            dl.append('<dt><i class="las la-download" title="'+get_advtool_msg('dependency.tree.Store.Binder')+'"></i></dt><dd>'+label+'</dd>');
        }
        if (elementObj['properties']['optionsBinder'] !== undefined && elementObj['properties']['optionsBinder']['className'] !== "") {
            var label = elementObj['properties']['optionsBinder']['className'];
            if (FormBuilder.availableBinder[label]  !== undefined) {
                label = FormBuilder.availableBinder[label];
            } else {
                label += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
                label = '<span class="missing-plugin">' + label + '</span>';
            }
            dl.append('<dt><i class="las la-upload" title="'+get_advtool_msg('dependency.tree.Options.Binder')+'"></i></dt><dd>'+label+'</dd>');
        }
        if (elementObj['properties']['validator'] !== undefined && elementObj['properties']['validator']['className'] !== "") {
            var label = elementObj['properties']['validator']['className'];
            if (FormBuilder.availableValidator[label]  !== undefined) {
                label = FormBuilder.availableValidator[label];
            } else {
                label += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
                label = '<span class="missing-plugin">' + label + '</span>';
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
                label = '<span class="missing-plugin">' + label + '</span>';
            }
            permissionLabel.push(label);
        }
        if (elementObj['properties']['permission_rules'] !== undefined) {
            var keys = Object.keys(elementObj['properties']['permission_rules']);
            for (var i in keys) {
                var rule = elementObj['properties']['permission_rules'][keys[i]];
                if (rule !== undefined && rule['permission'] !== undefined && rule['permission']['className'] !== "") {
                    var label = rule['permission']['className'];
                    if (FormBuilder.availablePermission[label]  !== undefined) {
                        label = FormBuilder.availablePermission[label];
                    } else {
                        label += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
                        label = '<span class="missing-plugin">' + label + '</span>';
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
     * A method called from the component template
     * It used to render the permission option of an element
     */
    renderPermission : function (row, elementObj, permissionObj, key, level) {
        $(row).append('<td class="authorized" width="30%"><div class="authorized-btns btn-group"></div></td>');
        $(row).append('<td class="unauthorized" width="30%"><div class="unauthorized-btns btn-group"></div></td>');
        
        if (elementObj.className === "org.joget.apps.form.model.Section") {
            var className = "";
            if (permissionObj["permission"] !== undefined 
                && permissionObj["permission"]["className"] !== undefined  
                && permissionObj["permission"]["className"] !== "") {

                className = permissionObj["permission"]["className"];
            }

            $(row).find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm visible-btn">'+get_advtool_msg("adv.permission.visible")+'</button>');
            $(row).find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm readonly-btn">'+get_advtool_msg("adv.permission.readonly")+'</button>');
            $(row).find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm hidden-btn">'+get_advtool_msg("adv.permission.hidden")+'</button>');

            if (permissionObj["readonly"] === "true") {
                $(row).find(".authorized-btns .readonly-btn").addClass("active");
            } else if (permissionObj["permissionHidden"] === "true") {
                $(row).find(".authorized-btns .hidden-btn").addClass("active");
            } else {
                $(row).find(".authorized-btns .visible-btn").addClass("active");
            }

            $(row).find(".unauthorized-btns").append('<button type="button" class="btn btn-outline-danger btn-sm readonly-btn">'+get_advtool_msg("adv.permission.readonly")+'</button>');
            $(row).find(".unauthorized-btns").append('<button type="button" class="btn btn-outline-danger btn-sm hidden-btn">'+get_advtool_msg("adv.permission.hidden")+'</button>');

            if (className !== "") {
                if (permissionObj["permissionReadonly"] === "true") {
                    $(row).find(".unauthorized-btns .readonly-btn").addClass("active");
                } else {
                    $(row).find(".unauthorized-btns .hidden-btn").addClass("active");
                }
            } else {
                $(row).find(".unauthorized-btns .btn").attr("disabled", "disabled");
            }

        } else {
            $(row).find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm visible-btn">'+get_advtool_msg("adv.permission.visible")+'</button>');
            $(row).find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm readonly-btn">'+get_advtool_msg("adv.permission.readonly")+'</button>');
            $(row).find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm hidden-btn">'+get_advtool_msg("adv.permission.hidden")+'</button>');

            if (permissionObj["readonly"] === "true") {
                $(row).find(".authorized-btns .readonly-btn").addClass("active");
            } else if (permissionObj["permissionHidden"] === "true") {
                $(row).find(".authorized-btns .hidden-btn").addClass("active");
            } else {
                $(row).find(".authorized-btns .visible-btn").addClass("active");
            }

            $(row).find(".unauthorized-btns").append('<button type="button" class="btn btn-outline-danger btn-sm readonly-btn">'+get_advtool_msg("adv.permission.readonly")+'</button>');
            $(row).find(".unauthorized-btns").append('<button type="button" class="btn btn-outline-danger btn-sm hidden-btn">'+get_advtool_msg("adv.permission.hidden")+'</button>');

            if (permissionObj["permissionReadonlyHidden"] === "true") {
                $(row).find(".unauthorized-btns .hidden-btn").addClass("active");
            } else {
                $(row).find(".unauthorized-btns .readonly-btn").addClass("active");
            }
            
            //find section row
            var sectionRow = $(row).prevAll(".level-1").first();
            if (!$(sectionRow).find(".authorized-btns .visible-btn").hasClass("active")) {
                $(row).find(".authorized-btns .btn").attr("disabled", "disabled");
            }
            if ($(sectionRow).find(".unauthorized-btns .readonly-btn").attr("disabled") === "disabled" || !$(sectionRow).find(".unauthorized-btns .readonly-btn").hasClass("active")) {
                $(row).find(".unauthorized-btns .btn").attr("disabled", "disabled");
            }
        }

        $(row).on("click", ".btn", function(event) {
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
                        $(row).nextUntil(".level-1").each(function(){
                            $(this).find(".unauthorized-btns .btn").removeAttr("disabled");
                        });
                    } else {
                        permissionObj["permissionReadonly"] = "";
                        $(row).nextUntil(".level-1").each(function(){
                            $(this).find(".unauthorized-btns .btn").attr("disabled", "disabled");
                        });
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
                        $(row).nextUntil(".level-1").each(function(){
                            $(this).find(".authorized-btns .btn").removeAttr("disabled");
                        });
                    } else {
                        $(row).nextUntil(".level-1").each(function(){
                            $(this).find(".authorized-btns .btn").attr("disabled", "disabled");
                        });
                    }
                }
            }
            CustomBuilder.update();

            event.preventDefault();
            return false;
        });
    },
    
    /*
     * Return custom styling definition to support styling for label and input field
     */
    getStylePropertiesDefinition: function(elementObj, component) {
        var self = CustomBuilder.Builder;
        
        //if having label
        if ($(self.selectedEl).find('> label.label').length > 0) {
            var style;
            if (component.builderTemplate.valueStylePropertiesDefinition === undefined || component.builderTemplate.labelStylePropertiesDefinition === undefined) {
                style = $.extend(true, [] , component.builderTemplate.stylePropertiesDefinition);
                
                //tempory remove the last and adding label position for it
                var other = style.pop();
                other.properties.push({
                        name : 'css-label-position',
                        label : get_cbuilder_msg('fbuilder.fieldLabelPosition'),
                        type : 'selectbox',
                        options : [
                            {value : '', label : 'Default'},
                            {value : 'label-left', label : 'Left'},
                            {value : 'label-top', label : 'Top'}
                        ],
                        viewport : 'desktop'
                    });
                other.properties.push({
                        name : 'css-tablet-label-position',
                        label : get_cbuilder_msg('fbuilder.fieldLabelPosition'),
                        type : 'selectbox',
                        options : [
                            {value : '', label : 'Default'},
                            {value : 'tablet-label-left', label : 'Left'},
                            {value : 'tablet-label-top', label : 'Top'}
                        ],
                        viewport : 'tablet'
                    });     
                    
                component.builderTemplate.labelStylePropertiesDefinition = $.merge([], style);
                component.builderTemplate.labelStylePropertiesDefinition = $.merge(component.builderTemplate.labelStylePropertiesDefinition, self.generateStylePropertiesDefinition("", [
                        {'prefix' : 'fieldLabel', 'label' : get_cbuilder_msg('fbuilder.fieldLabel')}
                    ]));
                component.builderTemplate.labelStylePropertiesDefinition = $.merge(component.builderTemplate.labelStylePropertiesDefinition, [other]);    
                
                
                component.builderTemplate.valueStylePropertiesDefinition = $.merge([], style);
                component.builderTemplate.valueStylePropertiesDefinition = $.merge(component.builderTemplate.valueStylePropertiesDefinition, self.generateStylePropertiesDefinition("", [
                        {'prefix' : 'fieldLabel', 'label' : get_cbuilder_msg('fbuilder.fieldLabel')},
                        {'prefix' : 'fieldInput', 'label' : get_cbuilder_msg('fbuilder.fieldInput')}
                    ]));
                component.builderTemplate.valueStylePropertiesDefinition = $.merge(component.builderTemplate.valueStylePropertiesDefinition, [other]); 
            }
            
            //if having input field
            if ($(self.selectedEl).find('> label.label + *:not(.ui-screen-hidden):not(div.form-clear), > label.label + .ui-screen-hidden + *, > label.label + div.form-clear + *').length > 0) {
                return component.builderTemplate.valueStylePropertiesDefinition;
            } else {
                return component.builderTemplate.labelStylePropertiesDefinition;
            }
        } else {
            return component.builderTemplate.stylePropertiesDefinition;
        }
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
        if (self.selectedEl.find(".clear-float").length > 0) {
            self.selectedEl.find(".clear-float").before(temp);
        } else {
            self.selectedEl.append(temp);
        }
        
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
            var newIndex = elements.length;
            if (elementClass === "form-section") {
                newIndex++; //only add for section, because for form field there is 1 dragged from pallate 
            }
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
            $(view).prepend('<i class="dt-loading las la-spinner la-3x la-spin" style="opacity:0.3"></i>');
            
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
        $(view).html('<div class="tabs"><ul class="nav nav-tabs nav-fill" id="form-erd-tabs" role="tablist"></ul><div class="tab-content"></div></div>');
        
        $(view).find('.tabs > ul').append('<li id="diagram-tab-link" class="nav-item content-tab"><a class="nav-link show active" data-toggle="tab" href="#diagram-tab" role="tab" aria-controls="diagram-tab" aria-selected="true"><i class="las la-project-diagram"></i> <span>'+get_cbuilder_msg('fbuilder.erd')+'</span></a></li>');
        $(view).find('.tabs > ul').append('<li id="desc-tab-link" class="nav-item content-tab"><a class="nav-link" data-toggle="tab" href="#desc-tab" role="tab" aria-controls="desc-tab"><i class="las la-list"></i> <span>'+get_cbuilder_msg('fbuilder.relationship.desc')+'</span></a></li>');
        $(view).find('.tabs > ul').append('<li id="columns-tab-link" class="nav-item content-tab"><a class="nav-link" data-toggle="tab" href="#columns-tab" role="tab" aria-controls="columns-tab"><i class="las la-th"></i> <span>'+get_advtool_msg('adv.tool.Table.Columns')+'</span></a></li>');
        $(view).find('.tabs > ul').append('<li id="usage-tab-link" class="nav-item content-tab"><a class="nav-link" data-toggle="tab" href="#usage-tab" role="tab" aria-controls="usage-tab"><i class="la la-binoculars"></i> <span>'+get_advtool_msg('adv.tool.Table.Usage')+'</span></a></li>');
        
        $(view).find('.tabs > .tab-content').append('<div id="diagram-tab" class="tab-pane fade active show"><div class="usage_content"><i class="las la-spinner la-3x la-spin" style="opacity:0.3"></i></div></div>');
        $(view).find('.tabs > .tab-content').append('<div id="desc-tab" class="tab-pane fade"><div class="usage_content"><i class="las la-spinner la-3x la-spin" style="opacity:0.3"></i></div></div>');
        $(view).find('.tabs > .tab-content').append('<div id="columns-tab" class="tab-pane fade"><div class="column_names"><div class="usage_content"><i class="las la-spinner la-3x la-spin" style="opacity:0.3"></i></div></div></div>');
        $(view).find('.tabs > .tab-content').append('<div id="usage-tab" class="tab-pane fade"><div class="sameapp_usage"><h3>'+get_advtool_msg('adv.tool.Table.Usage')+'</h3><div class="usage_content"><i class="las la-spinner la-3x la-spin" style="opacity:0.3"></i></div></div><div class="diffapp_usage"><h3>'+get_advtool_msg('adv.tool.Table.Usage.otherApp')+'</h3><div class="usage_content"><i class="las la-spinner la-3x la-spin" style="opacity:0.3"></i></div></div></div>');
        
        CustomBuilder.cachedAjax({
            method: "POST",
            url: CustomBuilder.contextPath + '/web/json/app'+CustomBuilder.appPath+'/form/erd',
            dataType : "json",
            success: function(data) {
                if (data.entities !== undefined) {
                    $('#diagram-tab').append('<div class="diagram-actions"><a class="btn btn-secondary btn-sm expandAll"><i class="las la-expand-arrows-alt"></i> '+get_cbuilder_msg('fbuilder.expandAll')+'</a> <a class="btn btn-secondary btn-sm collapseAll"><i class="las la-compress-arrows-alt"></i> '+get_cbuilder_msg('fbuilder.collapseAll')+'</a></div>');
                    $('#diagram-tab').append('<div id="diagram-grid" class="row"><div class="col"><div class="row"></div><div class="row"></div><div class="row"></div></div><div class="col"><div class="row"></div><div class="row"></div><div class="row"></div></div><div class="col"><div class="row"></div><div class="row"></div><div class="row"></div></div></div>');
                    
                    var entities = Object.keys(data.entities)
                        .map(function(key) {
                            return data.entities[key];
                        });
    
                    function compare( a, b ) {
                        if (Object.keys(a.hasMany).length < Object.keys(b.hasMany).length){
                            return 1;
                        }
                        if (Object.keys(a.hasMany).length > Object.keys(b.hasMany).length){
                            return -1;
                        }
                        return 0;
                    }

                    entities.sort(compare);
                    
                    //prepare grid
                    for (var i = 3; (i * i) < entities.length ;i++) {
                        $("#diagram-grid").append($("#diagram-grid .col:eq(0)").clone());
                        $("#diagram-grid .col").append('<div class="row"></div>');
                    }
                    
                    function findEmptyCell(x, y) {
                        if ($('#diagram-grid .col:eq('+x+') .row:eq('+y+') .entity-container').length === 0) {
                            return [x, y];
                        } else {
                            var limit = $('#diagram-grid .col').length;
                            for (var i=y-1; i<=y+1; i++) {
                                for (var j=x-1; j<=x+1; j++) {
                                    if (j >= 0 && i >= 0 && j < limit && i < limit 
                                            && !((j === x && (i === y-1 || i === y+1))) //not putting on direct top & bottom
                                            && !((j === y && (i === x-1 || i === x+1))) //not putting on direct left & right
                                            && $('#diagram-grid .col:eq('+j+') .row:eq('+i+') .entity-container').length === 0) {
                                        return [j, i];
                                    }
                                }
                            }
                            if (x+1 >= limit && y+1 >= limit) {
                                //start from first cell again
                                x = 0;
                                y = 0;
                            } else if (x+1 >= limit) {
                                y += 1; //find from next row
                            } else if (y+1 >= limit) {
                                x += 1; //find from next col
                            } else {
                                x += 1;
                                y += 1;
                            }
                            return findEmptyCell(x, y);
                        }
                    }

                    function systemField(entity, fieldId, entityContainer) {
                        var field = entity.fields[fieldId];
                        if (field === undefined) {
                            field = {
                                id : fieldId,
                                pluginClassName : "",
                                pluginLabel : get_cbuilder_msg('fbuilder.systemField')
                            };
                        }
                        renderField(entity, field, entityContainer, false);
                    }

                    function renderField(entity, field, entityContainer, checkSystemField) {
                        var isIndex = false;
                        
                        if (field.id === "id" || (entity.indexes !== undefined && entity.indexes.indexOf(field.id) !== -1)) {
                            isIndex = true;
                        }
                        
                        var systemFields = ["id", "dateCreated", "dateModified", "createdBy", "createdByName", "modifiedBy", "modifiedByName"];
                        if (checkSystemField === false || (checkSystemField === undefined && systemFields.indexOf(field.id) === -1)) {
                            var label = field.id;
                            entityContainer.find(".fields").append('<div id="'+entity.tableName+'_field_'+field.id+'" class="field" data-field="'+field.id+'"><span class="label"><a class="markindex '+(isIndex?'indexed':'')+'" title="'+(isIndex?get_cbuilder_msg('fbuilder.indexedField'):get_cbuilder_msg('fbuilder.markAsIndexField'))+'"><i class="las la-key"></i></a> '+label+'</span><span class="type">'+field.pluginLabel+'</span></div>');
                        }
                    }
                    
                    jsPlumb.unbind();
                    jsPlumb.detachEveryConnection();
                    jsPlumb.deleteEveryEndpoint();
                    jsPlumb.reset();
                    jsPlumb.importDefaults({
                        Container: "diagram-grid",
                        Anchor: "Continuous",
                        Endpoint: ["Dot", {radius: 4}],
                        PaintStyle: {strokeStyle: "#0047ad", lineWidth: 2, outlineWidth: 15, outlineColor: 'transparent'},
                        ConnectionOverlays: [
                            ["Arrow", {
                                location: -16,
                                id: "end",
                                length: 15,
                                width: 20,
                                foldback: 0.1,
                                direction: -1
                            }],
                            ["Arrow", {
                                location: 15,
                                id : "start",
                                length: 0.5,
                                width: 15,
                                foldback: 1
                            }]
                        ],
                        ConnectionsDetachable: false
                    });
                    
                    var unindexed = {};
                    
                    function getRandomRGBColor() {
                        return [Math.floor(Math.random() * 256), Math.floor(Math.random() * 256), Math.floor(Math.random() * 256)];
                    }
                    
                    function getRGBLightness(color) {
                        return ((color[0]*299)+(color[1]*587)+(color[2]*114))/1000;
                    }
                    
                    function rgbToHex(color) {
                        var hexR = color[0].toString(16).padStart(2, "0");
                        var hexG = color[1].toString(16).padStart(2, "0");
                        var hexB = color[2].toString(16).padStart(2, "0");
                        return "#" + hexR + hexG + hexB;
                    }
                    
                    function getRandomDarkColor() {
                        var color;
                        do {
                            color = getRandomRGBColor();
                        } while (getRGBLightness(color) > 70);

                        // return the color in hexadecimal format
                        return rgbToHex(color);
                    }
                    
                    function checkIndexField(entity, field) {
                        if (!$("#" + entity + "_field_" + field + " .label .markindex").hasClass("indexed") && !$("#"+entity+"_container").hasClass("external")) {
                            if (unindexed[entity] === undefined) {
                                unindexed[entity] = [field];
                            } else if ($.inArray(field, unindexed[entity]) === -1) {
                                unindexed[entity].push(field);
                            }
                        }
                    }
                    
                    var connected = [];
                    function drawConnection(entity1, entityField1, entity2, entityField2) {
                        if (connected.indexOf(entity1 + ":"+ entityField1 + " >> " + entity2 + ":" + entityField2) === -1) {
                            connected.push(entity1 + ":"+ entityField1 + " >> " + entity2 + ":" + entityField2);
                            
                            if ($("#" + entity2 + "_field_" + entityField2).length === 0 && $("#" + entity2 + "_container").length > 0) {
                                systemField(data.entities[entity2], entityField2, $("#" + entity2 + "_container"));
                            }

                            $("#" + entity1 + "_field_" + entityField1).addClass("connection_endpoint");
                            $("#" + entity2 + "_field_" + entityField2).addClass("connection_endpoint");
                            
                            checkIndexField(entity1, entityField1);
                            checkIndexField(entity2, entityField2);

                            try {
                                jsPlumb.connect({
                                    source: $("#" + entity1 + "_field_" + entityField1),
                                    target: $("#" + entity2 + "_field_" + entityField2),
                                    connector: ["Flowchart", {cornerRadius: 5, stub : (Math.random() * 40 + 15)}],
                                    anchors: ['ContinuousLeft', 'ContinuousRight'],
                                    paintStyle: {strokeStyle: getRandomDarkColor(), lineWidth: 2, outlineWidth: 15, outlineColor: 'transparent'}
                                });
                            } catch (err) {}
                        }
                    }
                    
                    function placeEntity(entity, x, y) {
                        if (x === undefined) {
                            x = Math.round($("#diagram-grid .col").length/2) - 1;
                            y = x;
                        }
                        if ($('#diagram-grid #'+entity.tableName+'_container').length === 0) {
                            var cellPos = findEmptyCell(x, y);
                            var cell = $('#diagram-grid .col:eq('+cellPos[0]+') .row:eq('+cellPos[1]+')');

                            var entityContainer = $('<div id="'+entity.tableName+'_container" data-tablename="'+entity.tableName+'" class="entity-container '+((entity.external === true)?'external':'')+'"><h5>'+entity.label + ((entity.external === true && entity.tableName.indexOf("app_fd_") === 0)?'':' <span class="tableName">('+entity.tableName+')</span>') + '</h5><div class="fields"></div><div class="forms"><label>'+get_cbuilder_msg('fbuilder.forms')+':</label> <ul></ul></div></div>');
                            if (entity.external === true) {
                                $(entityContainer).find('h5').append('<i class="las la-exclamation-circle" title="'+get_cbuilder_msg('fbuilder.externalEntity')+'"></i>');
                            }
                            $(cell).append(entityContainer);
                            $(entityContainer).data("entity", entity);
                           
                            //render fields
                            if (entity.external !== true) {
                                systemField(entity, "id", entityContainer, true);
                            }
                            for (const f in entity.fields) {
                                renderField(entity, entity.fields[f], entityContainer);
                            }
                            if (entity.external !== true) {
                                systemField(entity, "dateCreated", entityContainer);
                                systemField(entity, "dateModified", entityContainer);
                                systemField(entity, "createdBy", entityContainer);
                                systemField(entity, "createdByName", entityContainer);
                                systemField(entity, "modifiedBy", entityContainer);
                                systemField(entity, "modifiedByName", entityContainer);
                            }
                            
                            for (const f in entity.forms) {
                                if (f === CustomBuilder.data.properties.id) {
                                    $(entityContainer).addClass("current");
                                }
                                entityContainer.find(".forms ul").append('<li><a href="'+CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/form/builder/'+f+'">'+entity.forms[f]+'</a></li>');
                            }

                            for (const h in entity.hasMany) {
                                placeEntity(data.entities[entity.hasMany[h].entity], cellPos[0], cellPos[1]);

                                drawConnection(entity.tableName, entity.hasMany[h].fieldId, entity.hasMany[h].entity, entity.hasMany[h].entityFieldId);
                            }
                            for (const h in entity.ownBy) {
                                placeEntity(data.entities[entity.ownBy[h].entity], cellPos[0], cellPos[1]);

                                drawConnection(entity.ownBy[h].entity, entity.ownBy[h].entityFieldId, entity.tableName, entity.ownBy[h].fieldId);
                            }
                        }
                    }
                    
                    function markIndexes(indexes) {
                        $.blockUI({ css: { 
                            border: 'none', 
                            padding: '15px', 
                            backgroundColor: '#000', 
                            '-webkit-border-radius': '10px', 
                            '-moz-border-radius': '10px', 
                            opacity: .3, 
                            color: '#fff' 
                        }, message : '<i class="las la-spinner la-3x la-spin" style="opacity:0.3"></i>' }); 
                        $.ajax({
                            type: "POST",
                            data: {
                                "indexes": JSON.encode(indexes)
                            },
                            url: CustomBuilder.contextPath + '/web/fbuilder/app'+CustomBuilder.appPath+'/form/erd/indexes',
                            dataType : "text",
                            beforeSend: function (request) {
                                request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                            },
                            success: function(res) {
                                var keys = Object.keys(indexes);
                                for (var k in keys) {
                                    var temp = indexes[keys[k]];
                                    for (var i in temp) {
                                        $("#" + keys[k] + "_field_" + temp[i] + " .label .markindex").addClass("indexed");
                                        $("#" + keys[k] + "_field_" + temp[i] + " .label .markindex").attr("title", get_cbuilder_msg('fbuilder.indexedField'));
                                    }
                                }
                            },
                            error: function() {
                                alert(get_cbuilder_msg('fbuilder.indexFail'));
                            },
                            complete: function() {
                                $.unblockUI();
                            }
                        });
                    }
                    
                    for (const i in entities) {
                        var entity = entities[i];
                        var desc = $('<div id="'+entity.tableName+'_desc"><h5><i class="las la-table"></i> '+entity.label+' <span class="tableName">('+entity.tableName+')</span></h5><ul class="relations"></ul></div>');

                        for (const h in entity.hasMany) {
                            $(desc).find('.relations').append('<li>'+get_cbuilder_msg('fbuilder.relation', [entity.label, data.entities[entity.hasMany[h].entity].label])+'</li>');
                        }

                        $("#desc-tab").append(desc);

                        placeEntity(entity);
                    }
                    
                    if (Object.keys(unindexed).length > 0) {
                        var alert = $('<div class="alert alert-info" role="alert" style="margin:20px 0;"><h5>'+get_cbuilder_msg('fbuilder.suggestion')+'</h5>'+get_cbuilder_msg('fbuilder.reletionUnindexField')+'<ul></ul><button class="btn btn-info">'+get_cbuilder_msg('fbuilder.proceed')+'</button></div>');
                        var keys = Object.keys(unindexed);
                        for (var k in keys) {
                            var label = $("#diagram-grid #"+keys[k]+"_container").data('entity').label;
                            for (var i in unindexed[keys[k]]) {
                                $(alert).find("ul").append('<li>'+unindexed[keys[k]][i]+' ('+label+')</li>');
                            }
                        }
                        $("#diagram-grid").before(alert);
                        
                        $(alert).find("button").on("click", function(){
                            $(alert).remove();
                            markIndexes(unindexed);
                        });
                    }
                    
                    setTimeout(function(){
                        jsPlumb.repaintEverything();
                    }, 5);
                    
                    $(".entity-container h5").off("click")
                    $(".entity-container h5").on("click", function(){
                        $(this).parent().toggleClass("showDetails");
                        jsPlumb.repaintEverything();
                    });
                    
                    $(".entity-container .forms a").off("click");
                    $(".entity-container .forms a").on("click", function(){
                        CustomBuilder.ajaxRenderBuilder($(this).attr("href"));
                        return false;
                    });
                    
                    $(".entity-container .fields").off("click", "a.markindex:not(.indexed)");
                    $(".entity-container .fields").on("click", "a.markindex:not(.indexed)", function(){
                        var tableName = $(this).closest(".entity-container").data("tablename");
                        var field = $(this).closest(".field").data("field");
                        
                        if (confirm(get_cbuilder_msg('fbuilder.indexFieldConfirm', [field]))) {
                            var indexes = {}
                            indexes[tableName] = [field];
                            markIndexes(indexes);
                        }
                        
                        return false;
                    });
                    
                    $('#diagram-tab a.expandAll').off("click");
                    $('#diagram-tab a.expandAll').on("click", function(){
                        $('#diagram-grid .entity-container').addClass("showDetails");
                        jsPlumb.repaintEverything();
                    });
                    $('#diagram-tab a.collapseAll').off("click");
                    $('#diagram-tab a.collapseAll').on("click", function(){
                        $('#diagram-grid .entity-container').removeClass("showDetails");
                        jsPlumb.repaintEverything();
                    });
                } else {
                    $(view).find("#diagram-tab .usage_content, #desc-tab .usage_content").html('<p>'+get_cbuilder_msg('fbuilder.noData')+'</p>');
                }
            },
            complete: function() {
                $(view).find("#diagram-tab .usage_content, #desc-tab .usage_content").remove();
            }
        });
        
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
    
    tableUsageViewBeforeClosed: function(view) {
        jsPlumb.unbind();
        jsPlumb.detachEveryConnection();
        jsPlumb.deleteEveryEndpoint();
        jsPlumb.reset();
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
        if (CustomBuilder.data.properties['loadBinder'] !== undefined && CustomBuilder.data.properties['loadBinder']["className"] !== "") {
            CustomBuilder.cachedAjax({
                url: CustomBuilder.contextPath + '/web/json/console/app/'+CustomBuilder.appId+'/'+CustomBuilder.appVersion+'/form/binder/columns/options',
                dataType: "text",
                data : {
                    "binderJson" : JSON.encode(CustomBuilder.data.properties['loadBinder'])
                },
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
        } else {
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
        }
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
     * copy element to copy form hash variable clipboard
     */
    copyElement: function(data, type) {
        if (type === "elements") {
            CustomBuilder.copyTextToClipboard("#form." + CustomBuilder.data.properties.tableName + "." + data.properties.id +"#", false);
        }
    },

    /*
     * special handling to paste field in a section 
     */
    pasteElement: function(element, elementData, component, copiedObj, copiedComponent) {
        //if element is section & copied element is not column
        if ($(element).is('.form-section') && copiedComponent.builderTemplate.getParentContainerAttr() === "elements") {
            //find last column in section
            var lastColumn = $(element).find('> .form-column').last();
            CustomBuilder.Builder._pasteNode(lastColumn, copiedObj, copiedComponent);
        } else {
            CustomBuilder.Builder._pasteNode(element, copiedObj, copiedComponent);
        }
    },
    
    /*
     * Used for update generate app button 
     */
    afterUpdate: function() {
        if ($("#generator-btn").length > 0) {
            if (!FormBuilder.isSaved()) {
                $("#generator-btn").addClass("disabled");
                $("#generator-btn").attr("title",  $("#generator-btn").attr("title-unsave"));
            } else {
                $("#generator-btn").removeClass("disabled");
                $("#generator-btn").attr("title",  $("#generator-btn").attr("title-default"));
            }
        }
    },
      
    /*
     * remove dynamically added items    
     */            
    unloadBuilder : function() {
        $("#tooltip-btn, #table-usage-btn, #formstyle-btn").remove();
        $("#generator-btn").parent().remove();
        
        jsPlumb.unbind();
        jsPlumb.detachEveryConnection();
        jsPlumb.deleteEveryEndpoint();
        jsPlumb.reset();
    }
}