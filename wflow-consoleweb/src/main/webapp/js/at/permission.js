PermissionManager = {
    permissionPlugins : null,
    permissionOptions : null,
    json : null,
    addToUndo : false,
    container : null,
    options : null,
    init : function (container, json, options) {
        PermissionManager.addToUndo = false;
        PermissionManager.json = json;
        PermissionManager.container = container;
        PermissionManager.options = options;
        
        PropertyEditor.Popup.createDialog(PermissionManager.options.builder + "-permission-rule-container");
        PropertyEditor.Popup.createDialog(PermissionManager.options.builder + "-permission-element-container");
        
        if (PermissionManager.permissionPlugins === null) {
            var className = "";
            if (PermissionManager.options.builder === "form") {
                className = "org.joget.apps.form.model.FormPermission";
            } else if (PermissionManager.options.builder === "userview") {
                className = "org.joget.apps.userview.model.UserviewAccessPermission";
            } else if (PermissionManager.options.builder === "datalist") {
                className = "org.joget.apps.datalist.model.DatalistPermission";
            } else if (PermissionManager.options.builder === "custom") {
                className = CustomBuilder.config.advanced_tools.permission.permission_plugin;
            }
            
            $.ajax({
                url: PermissionManager.options.contextPath + '/web/property/json/getElements?classname='+className,
                dataType : "json",
                success: function(response) {
                    PermissionManager.permissionPlugins = {};
                    if (response !== null && response !== "") {
                        PermissionManager.permissionOptions = response;
                        for (var i in response) {
                            if (response[i].value !== "") {
                                PermissionManager.permissionPlugins[response[i].value] = response[i].label;
                            }
                        }
                    }
                    PermissionManager.render();
                }
            });
        } else {
            PermissionManager.render();
        }
    },
    render : function() {
        $(PermissionManager.container).html('<div class="permission_view"><div class="permission_rules"><div class="buttons"><a class="add_permission button"><i class="fas fa-plus"></i> '+get_advtool_msg('adv.permission.addPermission')+'</a></div><div class="sortable"></div></div><div class="elements_container"></div></div>');
        PermissionManager.renderRules();
        PermissionManager.renderElements();
        PermissionManager.attachEvents();
    },
    attachEvents : function() {
        
        $(PermissionManager.container).find(".sortable").sortable({
            opacity: 0.8,
            axis: 'y',
            handle: '.sort',
            tolerance: 'intersect',
            stop: function(event, ui){
                PermissionManager.updateRulesOrder();
            }
        });
        
        $(PermissionManager.container).on("click", ".permission_rule:not(.active)", function(){
            PermissionManager.setActiveRule($(this));
        });
        
        $(PermissionManager.container).on("click", ".permission_rule .edit_rule", function(event){
            PermissionManager.editRule($(this).closest(".permission_rule"));
            event.stopImmediatePropagation();
        });
        
        $(PermissionManager.container).on("click", ".permission_rule .delete_rule", function(event){
            PermissionManager.removeRule($(this).closest(".permission_rule"));
            event.stopImmediatePropagation();
        });
        
        $(PermissionManager.container).on("click", "tr .options a", function(event){
            PermissionManager.updateElementOption($(this), $(this).closest("tr"));
        });
        
        $(PermissionManager.container).on("click", "tr a.edit-plugin", function(event){
            PermissionManager.editElementPlugin($(this).closest("tr"));
        });
        
        if ($(PermissionManager.container).find(".sortable .permission_rule").length > 0) {
            PermissionManager.setActiveRule($(PermissionManager.container).find(".sortable .permission_rule:eq(0)"));
        } else {
            PermissionManager.setActiveRule($(PermissionManager.container).find(".permission_rules > .permission_rule"));
        }
        
        $(PermissionManager.container).find(".add_permission").on("click", function(){
            PermissionManager.addRule();
        });
    },
    getRuleElement : function() {
        if (PermissionManager.options.builder === "form") {
            return $(".form-container-div form")[0].dom.properties;
        } else if (PermissionManager.options.builder === "userview") {
            return UserviewBuilder.data.setting.properties;
        } else if (PermissionManager.options.builder === "datalist") {
            return DatalistBuilder.datalistProperties; 
        } else if (PermissionManager.options.builder === "custom") {
            return CustomBuilder.data.properties; 
        }
    },
    getRootElement : function() {
        if (PermissionManager.options.builder === "form") {
            return $(".form-container-div form");
        } else if (PermissionManager.options.builder === "userview") {
            return UserviewBuilder.data;
        } else if (PermissionManager.options.builder === "custom") {
            return CustomBuilder.data;
        } else {
            return null; //TODO : support other builder
        }
    },
    getChildElements : function(element) {
        if (PermissionManager.options.builder === "form") {
            return $(element).find("> [element-class]");
        } else if (PermissionManager.options.builder === "userview") {
            if (element["categories"] !== undefined) {
                return element["categories"];
            } else {
                return element["menus"];
            }
        } else if (PermissionManager.options.builder === "custom") {
            var childs = CustomBuilder.config.advanced_tools.permission.childs_properties;
            if (childs !== null && childs !== undefined) {
                for (var i in childs) {
                    if (element[childs[i]] !== undefined) {
                        return element[childs[i]];
                    }
                }
            }
            return null;
        } else {
            return null; //TODO : support other builder
        }
    },
    renderRules : function() {
        var ruleObj = PermissionManager.getRuleElement();
        
        if (ruleObj["permission_rules"] !== undefined) {
            for (var i in ruleObj["permission_rules"]) {
                PermissionManager.renderRule(ruleObj["permission_rules"][i]);
            }
        }
        
        //render default
        PermissionManager.renderRule(ruleObj);
    },
    renderRule : function(obj, prepend) {
        var isDefault = true;
        var key = "default";
        var name = get_advtool_msg('adv.permission.default');
        var pluginName = get_advtool_msg('adv.permission.noPlugin');
        
        if (obj['permission_key'] !== undefined) {
            isDefault = false;
            key = obj['permission_key'];
            name = obj['permission_name'];
        }
        if (obj['permission'] !== undefined && obj['permission']["className"] !== undefined) {
            var className = obj['permission']["className"];
            if (className !== "") {
                pluginName = PermissionManager.permissionPlugins[className];
                
                if (pluginName === undefined) {
                    pluginName = className + "(" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")";
                }
            }
        }
        var rule = $('<div class="permission_rule"><div class="sort"></div><div class="name"></div><div class="plugin"><span class="plugin_name"></span></div><div class="rule-buttons"><a class="edit_rule btn"><i class="far fa-edit"></i></a><a class="delete_rule btn"><i class="fas fa-times"></i></a></div></div>');
        
        $(rule).data("key", key);
        $(rule).attr("id", "permission-rule-"+key);
        $(rule).data("obj", obj);
        $(rule).find(".name").text(name);
        $(rule).find(".plugin_name").text(pluginName);
        
        if (!isDefault) {
            $(rule).find(".name").addClass("visible");
            
            if (prepend) {
                $(PermissionManager.container).find(".permission_rules .sortable").prepend(rule);
            } else {
                $(PermissionManager.container).find(".permission_rules .sortable").append(rule);
            }
            
            $(rule).find(".name").editable(function(value, settings){
                if (value === "") {
                    value = get_advtool_msg('adv.permission.unnamed');
                }
                obj['permission_name'] = value;
                PermissionManager.updateJson();
                return value;
            },{
                type      : 'text',
                tooltip   : '' ,
                select    : true ,
                style     : 'inherit',
                cssclass  : 'labelEditableField',
                onblur    : 'submit',
                rows      : 1,
                width     : '80%',
                minwidth  : 80,
                data: function(value, settings) {
                    if (value !== "") {
                        var div = document.createElement('div');
                        div.innerHTML = value;
                        var decoded = div.firstChild.nodeValue;
                        return decoded;
                    } else {
                        return value;
                    }
                }
            });
        } else {
            $(rule).addClass("default");
            $(rule).find(".delete_rule").remove();
            $(PermissionManager.container).find(".permission_rules").append(rule);
        }
        
        return $(rule);
    },
    renderElements : function() {
        var container = $(PermissionManager.container).find('.elements_container');
        $(container).append('<table class="permission-table"><thead><tr><th width="50%"></th></tr></thead><tbody></tbody></table>');
        PermissionManager.renderElementsHeader($(container).find("thead tr"));
        var tbody = $(container).find("tbody");
        
        if (PermissionManager.options.builder === "datalist") {
            PermissionManager.renderElementsDatalistBuilder(tbody);
        } else {
            if (PermissionManager.options.builder === "custom"
                    && CustomBuilder.config.advanced_tools.permission.render_elements_callback !== "") {
                CustomBuilder.callback(CustomBuilder.config.advanced_tools.permission.render_elements_callback, [tbody]);
            } else {
                var root = PermissionManager.getRootElement();
                PermissionManager.renderElement(root, tbody);
            }
        }
    },
    renderElementsHeader : function(row) {
        if (PermissionManager.options.builder === "datalist") {
            $(row).before('<tr><th></th><th class="authorized" width="50%" colspan="2">'+get_advtool_msg('adv.permission.authorized')+'</th></tr>');
            $(row).append('<th class="authorized web" width="25%">'+get_advtool_msg('adv.permission.web')+'</th>');
            $(row).append('<th class="authorized export" width="25%">'+get_advtool_msg('adv.permission.export')+'</th>');
        } else {
            $(row).append('<th class="authorized" width="30%">'+get_advtool_msg('adv.permission.authorized')+'</th>');
            if (PermissionManager.options.builder === "form") {
                $(row).append('<th class="unauthorized" width="20%">'+get_advtool_msg('adv.permission.unauthorized')+'</th>');
            } else if (PermissionManager.options.builder === "custom" 
                && CustomBuilder.config.advanced_tools.permission.unauthorized !== undefined 
                && CustomBuilder.config.advanced_tools.permission.unauthorized.property !== undefined
                && CustomBuilder.config.advanced_tools.permission.unauthorized.property !== "") {
                $(row).append('<th class="unauthorized" width="20%">'+get_advtool_msg('adv.permission.unauthorized')+'</th>');
            }
        }
    },
    renderElementsDatalistBuilder : function(tbody) {
        if ($("#databuilderContentColumns > li").length > 0) {
            $(tbody).append('<tr class="header"><td>'+get_advtool_msg('dependency.tree.Columns')+'</td><td class="authorized web"></td><td class="authorized export"></td></tr>');
            $("#databuilderContentColumns > li").each(function() {
                PermissionManager.renderElementDatalistBuilder($(this), tbody, "column");
            });
        }
        if ($("#databuilderContentFilters > li").length > 0) {
            $(tbody).append('<tr class="header"><td>'+get_advtool_msg('dependency.tree.Filters')+'</td><td class="authorized web"></td><td class="authorized export disabled"></td></tr>');
            $($("#databuilderContentFilters > li").get().reverse()).each(function() {
                PermissionManager.renderElementDatalistBuilder($(this), tbody, "filter");
            });
        }
        if ($("#databuilderContentRowActions > li").length > 0) {
            $(tbody).append('<tr class="header"><td>'+get_advtool_msg('dependency.tree.Row.Actions')+'</td><td class="authorized web"></td><td class="authorized export disabled"></td></tr>');
            $("#databuilderContentRowActions > li").each(function() {
                PermissionManager.renderElementDatalistBuilder($(this), tbody, "row_action");
            });
        }
        if ($("#databuilderContentActions > li").length > 0) {
            $(tbody).append('<tr class="header"><td>'+get_advtool_msg('dependency.tree.Actions')+'</td><td class="authorized web"></td><td class="authorized export disabled"></td></tr>');
            $("#databuilderContentActions > li").each(function() {
                PermissionManager.renderElementDatalistBuilder($(this), tbody, "action");
            });
        }
    },
    renderElementDatalistBuilder : function(element, tbody, type) {
        var eid = $(element).attr("id");
        var element;
        if (type === "column") {
            element = DatalistBuilder.chosenColumns[eid];
        } else if (type === "row_action") {
            element = DatalistBuilder.chosenRowActions[eid];
        } else if (type === "action") {
            element = DatalistBuilder.chosenActions[eid];
        } else if (type === "filter") {
            element = DatalistBuilder.chosenFilters[eid];
        }
        
        var row = $('<tr><td class="element-meta"></td><td class="authorized web"></td><td class="authorized export"></td></tr>');
        $(row).find('.authorized').append('<div class="options"><a class="visible">'+get_advtool_msg("adv.permission.visible")+'</a><a class="hidden">'+get_advtool_msg("adv.permission.hidden")+'</a></div>');
        
        $(row).data("element", element);
        $(row).data("type", type);
        
        if (type === "column" || type === "filter") {
            if (element["label"] !== undefined) {
                $(row).find(".element-meta").append('<span class="element-label">' + UI.escapeHTML(element["label"]) + '</span>');
            }
        } else {
            if (element["properties"] !== undefined && element["properties"]["label"] !== undefined) {
                $(row).find(".element-meta").append('<span class="element-label">' + UI.escapeHTML(element["properties"]["label"]) + '</span>');
            } else if (element["label"] !== undefined) {
                $(row).find(".element-meta").append('<span class="element-label">' + UI.escapeHTML(element["label"]) + '</span>');
            }
        }
        
        if (type !== "column") {
            $(row).find(".authorized.export").html('');
        }
        
        if (type === "column" || type === "filter") {
            $(row).find(".element-meta").append('<span class="element-id">' + UI.escapeHTML(element["name"]) + '</span>');
        } else {
            var pluginLabel = PermissionManager.getElementPluginLabel(element);
            if (pluginLabel !== null) {
                $(row).find(".element-meta").append('<div class="element-class">'+pluginLabel+'</div>');
            }
        }
        
        $(tbody).append(row);
    },
    renderElement : function(elementObj, tbody, parentElement) {
        var element = PermissionManager.getElement(elementObj);
        
        if (element !== null && element !== undefined && !PermissionManager.isIgnoreRendering(element)) {
            var row = $('<tr><td class="element-meta"></td></tr>');
            $(row).data("element", element);
            
            if (parentElement !== null && parentElement !== undefined) {
                $(row).data("parent-element", parentElement);
            }
            
            var pluginLabel = PermissionManager.getElementPluginLabel(element);

            if (pluginLabel !== null) {
                $(row).find(".element-class").text(pluginLabel);

                var label = "";
                if (element["properties"]["label"] !== undefined) {
                    $(row).find(".element-meta").append('<div class="element-class">'+UI.escapeHTML(pluginLabel)+'</div>');
                    label = element["properties"]["label"];
                } else {
                    label = pluginLabel;
                }
                
                if (PermissionManager.options.builder === "custom" 
                        && CustomBuilder.config.advanced_tools.permission.element_label_callback !== "") {
                    var tempLabel = CustomBuilder.callback(CustomBuilder.config.advanced_tools.permission.element_label_callback, [element]);
                    if (tempLabel !== null && tempLabel !== undefined) {
                        label = tempLabel;
                    }
                }
                
                $(row).find(".element-meta").append('<span class="element-label">' + UI.escapeHTML(label) + '</span>');
                if (element["properties"]["id"] !== undefined) {
                    var eid = element["properties"]["id"];
                    if (PermissionManager.options.builder === "userview") {
                        if (element["properties"]["customId"] !== undefined) {
                            eid = element["properties"]["customId"];
                        } else {
                            eid = "";
                        }
                    }
                    if (PermissionManager.options.builder !== "custom" ||
                            (PermissionManager.options.builder === "custom" && CustomBuilder.config.advanced_tools.permission.display_element_id)) {
                        $(row).find(".element-meta").append('<span class="element-id">' + eid + '</span>');
                    }
                }

                if (PermissionManager.isElementSupportPlugin(element)) {
                    parentElement = element;
                    $(row).addClass("support-plugin");
                } else {
                    $(row).addClass("normal");
                }
                
                if (PermissionManager.options.builder === "form") {
                    PermissionManager.renderElementFormBuilder(row);
                } else if (PermissionManager.options.builder === "userview") {
                    PermissionManager.renderElementUserviewBuilder(row);
                } else if (PermissionManager.options.builder === "custom") {
                    PermissionManager.renderElementCustomBuilder(row);
                }
                
                $(tbody).append(row);
            }
        }
        var childs = PermissionManager.getChildElements(elementObj);
        if (childs !== null && childs !== undefined && childs.length > 0) {
            $.each(childs, function(i, child){
                PermissionManager.renderElement(child, tbody, parentElement);
            });
        }
    },
    renderElementFormBuilder : function(row) {
        $(row).append('<td class="authorized"></td><td class="unauthorized"></td>');
        $(row).find('.authorized').append('<div class="options"><a class="visible">'+get_advtool_msg("adv.permission.visible")+'</a><a class="readonly">'+get_advtool_msg("adv.permission.readonly")+'</a><a class="hidden">'+get_advtool_msg("adv.permission.hidden")+'</a></div>');
        $(row).find('.unauthorized').append('<div class="options"><a class="readonly">'+get_advtool_msg("adv.permission.readonly")+'</a><a class="hidden">'+get_advtool_msg("adv.permission.hidden")+'</a></div>');
    },
    renderElementUserviewBuilder : function(row) {
        $(row).append('<td class="authorized"></td>');
        $(row).find('.authorized').append('<div class="options"><a class="accessible">'+get_advtool_msg("adv.permission.accessible")+'</a><a class="hidden">'+get_advtool_msg("adv.permission.hidden")+'</a><a class="deny">'+get_advtool_msg("adv.permission.deny")+'</a></div>');
    },
    renderElementCustomBuilder : function(row) {
        if (CustomBuilder.config.advanced_tools.permission.authorized !== undefined 
                && CustomBuilder.config.advanced_tools.permission.authorized.property !== undefined
                && CustomBuilder.config.advanced_tools.permission.authorized.property !== "") {
            var property = CustomBuilder.config.advanced_tools.permission.authorized.property;
            var default_value = CustomBuilder.config.advanced_tools.permission.authorized.default_value;
            $(row).append('<td class="authorized" data-property="'+property+'" data-default="'+default_value+'" ><div class="options"></div></td>');
            var options = CustomBuilder.config.advanced_tools.permission.authorized.options;
            for (var i in options) {
                $(row).find('.authorized .options').append('<a class="'+options[i].key+'" data-value="'+options[i].value+'">'+options[i].label+'</a>');
            }
        }
        if (CustomBuilder.config.advanced_tools.permission.unauthorized !== undefined 
                && CustomBuilder.config.advanced_tools.permission.unauthorized.property !== undefined
                && CustomBuilder.config.advanced_tools.permission.unauthorized.property !== "") {
            var property = CustomBuilder.config.advanced_tools.permission.unauthorized.property;
            var default_value = CustomBuilder.config.advanced_tools.permission.unauthorized.default_value;
            $(row).append('<td class="unauthorized" data-property="'+property+'" data-default="'+default_value+'" ><div class="options"></div></td>');
            var options = CustomBuilder.config.advanced_tools.permission.unauthorized.options;
            for (var i in options) {
                $(row).find('.unauthorized .options').append('<a class="'+options[i].key+'" data-value="'+options[i].value+'">'+options[i].label+'</a>');
            }
        }
    },
    getElement : function(element) {
        if (PermissionManager.options.builder === "form") {
            return $(element)[0].dom;
        } else if (PermissionManager.options.builder === "userview") {
            return element;
        } else if (PermissionManager.options.builder === "datalist") {
            return null;
        } else if (PermissionManager.options.builder === "custom") {
            return element;
        }
    },
    getElementPluginLabel : function(element) {
        if (PermissionManager.options.builder === "form") {
            var classname = element["className"];
            if (classname === "org.joget.apps.form.model.Section") {
                return get_advtool_msg("adv.permission.section");
            } else if ($("#builder-palette .form-palette-element[element-class='"+classname+"'] label").length > 0) {
                return $("#builder-palette .form-palette-element[element-class='"+classname+"'] label").text();
            } else {
                return null;
            }
        } else if (PermissionManager.options.builder === "userview") {
            var classname = element["className"];
            if (classname === "org.joget.apps.userview.model.UserviewCategory") {
                return get_advtool_msg("dependency.tree.Category");
            } else if (UserviewBuilder.menuTypes[classname] !== undefined) {
                return UserviewBuilder.menuTypes[classname].label;
            } else {
                return null;
            }
        } else if (PermissionManager.options.builder === "datalist") {
            var classname = element["className"];
            if ($("#builder-palettle-actions div[data-id='"+classname+"'] label").length > 0) {
                return $("#builder-palettle-actions div[data-id='"+classname+"'] label").text();
            } else {
                return null;
            }
        } else if (PermissionManager.options.builder === "custom") {
            return CustomBuilder.getPermissionElementLabel(element);
        }
    },
    isIgnoreRendering : function(element) {
        if (PermissionManager.options.builder === "form") {
            if (element["className"] === "org.joget.apps.form.model.Form" 
                    || element["className"] === "org.joget.apps.form.model.Column") {
                return true;
            } else {
                return false;
            }
        } else if (PermissionManager.options.builder === "userview") {
            if (element["className"] === "org.joget.apps.userview.model.Userview") {
                return true;
            } else {
                return false;
            }
        } else if (PermissionManager.options.builder === "custom") {
            var ignore_classes = CustomBuilder.config.advanced_tools.permission.ignore_classes;
            if (element["className"] === null || element["className"] === undefined || $.inArray(element["className"], ignore_classes) !== -1) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    },
    isElementSupportPlugin : function(element) {
        if (PermissionManager.options.builder === "form") {
            if (element["className"] === "org.joget.apps.form.model.Section") {
                return true;
            } else {
                return false;
            }
        } else if (PermissionManager.options.builder === "userview") {
            if (element["className"] === "org.joget.apps.userview.model.UserviewCategory") {
                return true;
            } else {
                return false;
            }
        } else if (PermissionManager.options.builder === "custom") {
            var classes = CustomBuilder.config.advanced_tools.permission.element_support_plugin;
            if (element["className"] !== undefined && $.inArray(element["className"], classes) !== -1) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    },
    addRule : function() {
        var rule = {
            permission_key : PermissionManager.guid(),
            permission_name : get_advtool_msg('adv.permission.unnamed'),
            permission : {
                className : "",
                properties : []
            }
        };
        
        var ruleObj = PermissionManager.getRuleElement();
        if (ruleObj["permission_rules"] === undefined) {
            ruleObj["permission_rules"] = [];
        }
        ruleObj["permission_rules"].unshift(rule);
        
        var ruleElm = PermissionManager.renderRule(rule, true);
        PermissionManager.setActiveRule($(ruleElm));
        
        PermissionManager.updateJson();
    },
    removeRule : function(rule) {
        var ruleObj = PermissionManager.getRuleElement();
        var key = $(rule).data("key");
        var index = -1;
        for (var i = 0; i < ruleObj["permission_rules"].length; i++) {
            if (ruleObj["permission_rules"][i]["permission_key"] === key) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            ruleObj["permission_rules"].splice(index, 1);
            
            PermissionManager.removeElementsPermission(key);
            
            var isActive = $(rule).hasClass("active");
             $(rule).remove();
            
            if (isActive) {
                if ($(PermissionManager.container).find(".sortable .permission_rule").length > 0) {
                    PermissionManager.setActiveRule($(PermissionManager.container).find(".sortable .permission_rule:eq(0)"));
                } else {
                    PermissionManager.setActiveRule($(PermissionManager.container).find(".permission_rules > .permission_rule"));
                }
            }
            
            PermissionManager.updateJson();
        }
    },
    editRule : function(rule) {
        var appPath = "/" + PermissionManager.options.appId + "/" + PermissionManager.options.appVersion;
        var propertiesDefinition;
        
        if ($(rule).hasClass("default")) {
            propertiesDefinition = [
                {
                    title : get_advtool_msg('adv.tool.permission') + " (" + get_advtool_msg('adv.permission.default') + ")",
                    properties : [{
                        name: 'permission',
                        label: get_advtool_msg('adv.tool.permission'),
                        type: 'elementselect',
                        options: PermissionManager.permissionOptions,
                        url: PermissionManager.options.contextPath + '/web/property/json'+appPath+'/getPropertyOptions'
                    }]
                }
            ];
            if (PermissionManager.options.builder !== "userview") {
                propertiesDefinition[0]["properties"].push({
                    name : 'noPermissionMessage',
                    type : 'textarea',
                    label : get_advtool_msg('adv.permission.noPermissionMessage')
                });  
            }
        } else {
            propertiesDefinition = [
                {
                    title : get_advtool_msg('adv.tool.permission') + " (" + $(rule).data("obj").permission_name + ")",
                    properties : [{
                        name : 'permission_key',
                        type : 'hidden'
                    },
                    {
                        name : 'permission_name',
                        type : 'textfield',
                        label : get_advtool_msg('dependency.tree.Name'),
                        required : "true"
                    },
                    {
                        name: 'permission',
                        label: get_advtool_msg('adv.tool.permission'),
                        type: 'elementselect',
                        options: PermissionManager.permissionOptions,
                        url: PermissionManager.options.contextPath + '/web/property/json'+appPath+'/getPropertyOptions'
                    }]
                }
            ];
        }
        
        var options = {
            appPath: appPath,
            contextPath: PermissionManager.options.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : $(rule).data("obj"),
            changeCheckIgnoreUndefined: true,
            showCancelButton: true,
            saveCallback: PermissionManager.updateRule,
            cancelCallback : function() {}
        };
        PropertyEditor.Popup.showDialog( PermissionManager.options.builder + "-permission-rule-container", options, {id:$(rule).data("key")});
    },
    updateRule : function(container, properties) {
        var ruleObj = PermissionManager.getRuleElement();
        
        var rule;
        if (properties['permission_key'] !== undefined) {
            for (var i = 0; i < ruleObj["permission_rules"].length; i++) {
                if (ruleObj["permission_rules"][i]["permission_key"] === properties['permission_key']) {
                    ruleObj["permission_rules"][i] = properties;
                    break;
                }
            }
            rule = $("#permission-rule-"+properties['permission_key']);
            $(rule).data("obj", properties);
        } else {
            ruleObj = $.extend(ruleObj, properties);
            rule = $("#permission-rule-default");
        }
        
        if (properties['permission_name'] !== undefined) {
            $(rule).find(".name").text(properties['permission_name']);
        }
        var pluginName = get_advtool_msg('adv.permission.noPlugin');
        if (properties['permission'] !== undefined && properties['permission']["className"] !== undefined) {
            var className = properties['permission']["className"];
            if (className !== "") {
                pluginName = PermissionManager.permissionPlugins[className];
                
                if (pluginName === undefined) {
                    pluginName = className + "(" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")";
                }
            }
        }
        $(rule).find(".plugin_name").text(pluginName);
        
        PermissionManager.updateJson();
    },
    updateRulesOrder : function() {
        var ruleObj = PermissionManager.getRuleElement();
        var newRules = [];
        
        $(PermissionManager.container).find(".permission_rules .sortable .permission_rule").each(function(){
            newRules.push($(this).data("obj"));
        });
        
        ruleObj["permission_rules"] = newRules;
        
        PermissionManager.updateJson();
    },
    removeElementsPermission : function(key) {
        $(PermissionManager.container).find(".permission-table tbody tr:not(.header)").each(function(){
            var element = $(this).data("element");
            if (element["properties"] === undefined && element["permission_rules"] !== undefined && element["permission_rules"][key] !== undefined) {
                delete element["permission_rules"][key];
            } else if (element["properties"]["permission_rules"] !== undefined && element["properties"]["permission_rules"][key] !== undefined) {
                delete element["properties"]["permission_rules"][key];
            }
        });
    },
    setActiveRule : function(rule) {
        $(PermissionManager.container).find(".permission_rule").removeClass("active");
        $(rule).addClass("active");
        
        //update all elements to show setting for this rule
        var key = $(rule).data("key");
        
        var parentRow = null;
        $(PermissionManager.container).find(".permission-table tbody tr:not(.header)").each(function(){
            PermissionManager.showElementPermission(key, $(this), parentRow);
            
            if ($(this).hasClass("support-plugin")) {
                parentRow = $(this);
            }
        });
    },
    showElementPermission : function(key, row, parentRow) {
        $(row).find(".element-meta .plugin").remove();
        $(row).find("td").removeClass("disabled");
        $(row).find(".options a").removeClass("selected");
        
        var permissionObj = null;
        var element = $(row).data("element");
        
        if (PermissionManager.options.builder === "datalist") {
            if (key === "default") {
                permissionObj = element;
            } else {
                if (element["permission_rules"] === undefined) {
                    element["permission_rules"] = {};
                }
                if (element["permission_rules"][key] === undefined) {
                    element["permission_rules"][key] = {};
                }
                permissionObj = element["permission_rules"][key];
            }
        } else {
            if (key === "default") {
                permissionObj = element["properties"];
            } else {
                if (element["properties"]["permission_rules"] === undefined) {
                    element["properties"]["permission_rules"] = {};
                }
                if (element["properties"]["permission_rules"][key] === undefined) {
                    element["properties"]["permission_rules"][key] = {};
                }
                permissionObj = element["properties"]["permission_rules"][key];
            }
        }
        
        if (PermissionManager.options.builder === "form") {
            PermissionManager.showElementPermissionFormBuilder(key, row, parentRow, permissionObj);
        } else if (PermissionManager.options.builder === "userview") {
            PermissionManager.showElementPermissionUserviewBuilder(key, row, parentRow, permissionObj);
        } else if (PermissionManager.options.builder === "datalist") {
            PermissionManager.showElementPermissionDatalistBuilder(key, row, permissionObj);
        } else if (PermissionManager.options.builder === "custom") {
            PermissionManager.showElementPermissionCustomBuilder(key, row, parentRow, permissionObj);
        }
    },
    showElementPermissionFormBuilder : function(key, row, parentRow, permissionObj) {
        if ($(row).hasClass("support-plugin")) {
            $(row).find(".element-meta").append('<div class="plugin"><div class="plugin-name"><a class="edit-plugin btn"><label>'+get_advtool_msg('adv.tool.permission')+':</label> <span></span> <i class="far fa-edit"></i></a></div><div class="comment"></div></div>');
            
            if (permissionObj !== null && permissionObj !== undefined) {
                if (permissionObj["readonly"] === "true") {
                    $(row).find(".authorized .readonly").addClass("selected");
                } else if (permissionObj["permissionHidden"] === "true") {
                    $(row).find(".authorized .hidden").addClass("selected");
                } else {
                    $(row).find(".authorized .visible").addClass("selected");
                }

                PermissionManager.updateElementPlugin(row, permissionObj);
            } else {
                $(row).find(".authorized .visible").addClass("selected");
                $(row).find(".unauthorized").addClass("disabled");
                
                $(row).find('.plugin-name span').text(get_advtool_msg('adv.permission.noPlugin'));
            }
        } else {
            if ($(parentRow).find(".unauthorized").hasClass("disabled") || $(parentRow).find(".unauthorized .hidden").hasClass("selected")) {
                $(row).find(".unauthorized").addClass("disabled");
            } else {
                if (permissionObj !== null && permissionObj !== undefined) {
                    if (permissionObj["permissionReadonlyHidden"] === "true") {
                        $(row).find(".unauthorized .hidden").addClass("selected");
                    } else {
                        $(row).find(".unauthorized .readonly").addClass("selected");
                    }
                }
            }
            
            if ($(parentRow).find(".authorized .visible").hasClass("selected")) {
                if (permissionObj !== null && permissionObj !== undefined) {
                    if (permissionObj["readonly"] === "true") {
                        $(row).find(".authorized .readonly").addClass("selected");
                    } else if (permissionObj["permissionHidden"] === "true") {
                        $(row).find(".authorized .hidden").addClass("selected");
                    } else {
                        $(row).find(".authorized .visible").addClass("selected");
                    }
                } else {
                    $(row).find(".authorized .visible").addClass("selected");
                }
            } else {
                $(row).find(".authorized").addClass("disabled");
            }
        }
    },
    showElementPermissionUserviewBuilder : function(key, row, parentRow, permissionObj) {
        if ($(row).hasClass("support-plugin")) {
            $(row).find(".element-meta").append('<div class="plugin"><div class="plugin-name"><a class="edit-plugin btn"><label>'+get_advtool_msg('adv.tool.permission')+':</label> <span></span> <i class="far fa-edit"></i></a></div><div class="comment"></div></div>');
            
            if (permissionObj !== null && permissionObj !== undefined) {
                if (permissionObj["hide"] === "yes") {
                    $(row).find(".authorized .hidden").addClass("selected");
                } else if (permissionObj["permissionDeny"] === "true") {
                    $(row).find(".authorized .deny").addClass("selected");
                } else {
                    $(row).find(".authorized .accessible").addClass("selected");
                }

                PermissionManager.updateElementPlugin(row, permissionObj);
            } else {
                $(row).find(".authorized .accessible").addClass("selected");
                $(row).find('.plugin-name span').text(get_advtool_msg('adv.permission.noPlugin'));
            }
        } else {
            if ($(parentRow).find(".authorized .accessible").hasClass("selected")) {
                if (permissionObj !== null && permissionObj !== undefined) {
                    if (permissionObj["permissionHidden"] === "true") {
                        $(row).find(".authorized .hidden").addClass("selected");
                    } else if (permissionObj["permissionDeny"] === "true") {
                        $(row).find(".authorized .deny").addClass("selected");
                    } else {
                        $(row).find(".authorized .accessible").addClass("selected");
                    }
                } else {
                    $(row).find(".authorized .accessible").addClass("selected");
                }
            } else {
                $(row).find(".authorized").addClass("disabled");
            }
        }
    },
    showElementPermissionDatalistBuilder : function(key, row, permissionObj) {
        var type = $(row).data("type");
        if (type === "column") {
            if (permissionObj["hidden"] === "true") {
                $(row).find(".authorized.web .hidden").addClass("selected");
                
                if (permissionObj["include_export"] === "true") {
                    $(row).find(".authorized.export .visible").addClass("selected");
                } else {
                    $(row).find(".authorized.export .hidden").addClass("selected");
                }
            } else {
                $(row).find(".authorized.web .visible").addClass("selected");
                
                if (permissionObj["exclude_export"] === "true") {
                    $(row).find(".authorized.export .hidden").addClass("selected");
                } else {
                    $(row).find(".authorized.export .visible").addClass("selected");
                }
            }
        } else {
            $(row).find("td.authorized.export").addClass("disabled");
            if (permissionObj["hidden"] === "true") {
                $(row).find(".authorized.web .hidden").addClass("selected");
            } else {
                $(row).find(".authorized.web .visible").addClass("selected");
            }
        }
    },
    showElementPermissionCustomBuilder : function(key, row, parentRow, permissionObj) {
        if ($(row).hasClass("support-plugin")) {
            $(row).find(".element-meta").append('<div class="plugin"><div class="plugin-name"><a class="edit-plugin btn"><label>'+get_advtool_msg('adv.tool.permission')+':</label> <span></span> <i class="far fa-edit"></i></a></div><div class="comment"></div></div>');
            if (permissionObj !== null && permissionObj !== undefined) {
                var property = $(row).find(".authorized").data("property");
                var propertyValue = permissionObj[property];
                if (propertyValue === null || propertyValue === undefined) {
                    propertyValue = "";
                }
                $(row).find(".authorized a[data-value='"+propertyValue+"']").addClass("selected");

                PermissionManager.updateElementPlugin(row, permissionObj);
            } else {
                var default_value = $(row).find(".authorized").data("default");
                if (default_value === null || default_value === undefined) {
                    default_value = "";
                }
                $(row).find(".authorized a[data-value='"+default_value+"']").addClass("selected");
                $(row).find(".unauthorized").addClass("disabled");
                
                $(row).find('.plugin-name span').text(get_advtool_msg('adv.permission.noPlugin'));
            }
        } else {
            if ($(row).find(".unauthorized").length > 0) {
                var default_value = $(row).find(".unauthorized").data("default");
                if (default_value === null || default_value === undefined) {
                    default_value = "";
                }
                if (parentRow !== null && ($(parentRow).find(".unauthorized").hasClass("disabled") || !$(parentRow).find(".unauthorized a[data-value='"+default_value+"']").hasClass("selected"))) {
                    $(row).find(".unauthorized").addClass("disabled");
                } else {
                    if (permissionObj !== null && permissionObj !== undefined) {
                        var property = $(row).find(".unauthorized").data("property");
                        var propertyValue = permissionObj[property];
                        if (propertyValue === null || propertyValue === undefined) {
                            propertyValue = "";
                        }
                        $(row).find(".unauthorized a[data-value='"+propertyValue+"']").addClass("selected");
                    }
                }
            }
            
            var default_value = $(row).find(".authorized").data("default");
            if (default_value === null || default_value === undefined) {
                default_value = "";
            }
            if (parentRow === null || $(parentRow).find(".authorized a[data-value='"+default_value+"']").hasClass("selected")) {
                if (permissionObj !== null && permissionObj !== undefined) {
                    var property = $(row).find(".authorized").data("property");
                    var propertyValue = permissionObj[property];
                    if (propertyValue === null || propertyValue === undefined) {
                        propertyValue = "";
                    }
                    $(row).find(".authorized a[data-value='"+propertyValue+"']").addClass("selected");
                } else {
                    $(row).find(".authorized a[data-value='"+default_value+"']").addClass("selected");
                }
            } else {
                $(row).find(".authorized").addClass("disabled");
            }
        }
    },
    updateElementOption : function(action, row, update) {
        var options = $(action).closest("td");
        $(options).find("a").removeClass("selected");
        $(action).addClass("selected");
        
        var element = $(row).data("element");
        var key = $(PermissionManager.container).find(".permission_rules .permission_rule.active").data("key");
        
        var permissionObj = null;
        if (PermissionManager.options.builder === "datalist") {
            if (key === "default") {
                permissionObj = element;
            } else {
                if (element["permission_rules"] === undefined) {
                    element["permission_rules"] = {};
                }
                if (element["permission_rules"][key] === undefined) {
                    element["permission_rules"][key] = {};
                }    
                permissionObj = element["permission_rules"][key];
            }
        } else {
            if (key === "default") {
                permissionObj = element["properties"];
            } else {
                if (element["properties"]["permission_rules"] === undefined) {
                    element["properties"]["permission_rules"] = {};
                }
                if (element["properties"]["permission_rules"][key] === undefined) {
                    element["properties"]["permission_rules"][key] = {};
                }    
                permissionObj = element["properties"]["permission_rules"][key];
            }
        }
        
        if (PermissionManager.options.builder === "form") {
            PermissionManager.updateElementOptionFormBuilder(options, row, permissionObj);
        } else if (PermissionManager.options.builder === "userview") {
            PermissionManager.updateElementOptionUserviewBuilder(options, row, permissionObj);
        } else if (PermissionManager.options.builder === "datalist") {
            PermissionManager.updateElementOptionDatalistBuilder(options, row, permissionObj);
        } else if (PermissionManager.options.builder === "custom") {
            PermissionManager.updateElementOptionCustomBuilder(options, row, permissionObj);
        }
        
        if ($(row).hasClass("support-plugin")) {
            $(row).nextUntil(".support-plugin").each(function(){
                PermissionManager.showElementPermission(key, $(this), $(row));
            });
        }
        
        if (update === undefined || update === true) {
            PermissionManager.updateJson();
        }
    },
    updateElementOptionFormBuilder : function(options, row, permissionObj) {
        if ($(options).hasClass("authorized")) {
            if ($(options).find(".readonly").hasClass("selected")) {
                permissionObj["readonly"] = "true";
                permissionObj["permissionHidden"] = "";
            } else if ($(options).find(".hidden").hasClass("selected")) {
                permissionObj["readonly"] = "";
                permissionObj["permissionHidden"] = "true";
            } else {
                permissionObj["readonly"] = "";
                permissionObj["permissionHidden"] = "";
            }
        } else {
            if ($(row).hasClass("support-plugin")) {
                if ($(options).find(".readonly").hasClass("selected")) {
                    permissionObj["permissionReadonly"] = "true";
                } else if ($(options).find(".hidden").hasClass("selected")) {
                    permissionObj["permissionReadonly"] = "";
                }
            } else {
                if ($(options).find(".readonly").hasClass("selected")) {
                    permissionObj["permissionReadonlyHidden"] = "";
                } else if ($(options).find(".hidden").hasClass("selected")) {
                    permissionObj["permissionReadonlyHidden"] = "true";
                }
            }
        }
    },
    updateElementOptionUserviewBuilder : function(options, row, permissionObj) {
        if ($(row).hasClass("support-plugin")) {
            if ($(options).find(".accessible").hasClass("selected")) {
                permissionObj["hide"] = "";
                permissionObj["permissionDeny"] = "";
            } else if ($(options).find(".hidden").hasClass("selected")) {
                permissionObj["hide"] = "yes";
                permissionObj["permissionDeny"] = "";
            } else {
                permissionObj["hide"] = "";
                permissionObj["permissionDeny"] = "true";
            }
        } else {
            if ($(options).find(".accessible").hasClass("selected")) {
                permissionObj["permissionHidden"] = "";
                permissionObj["permissionDeny"] = "";
            } else if ($(options).find(".hidden").hasClass("selected")) {
                permissionObj["permissionHidden"] = "true";
                permissionObj["permissionDeny"] = "";
            } else {
                permissionObj["permissionHidden"] = "";
                permissionObj["permissionDeny"] = "true";
            }
        }
    },
    updateElementOptionDatalistBuilder : function(options, row, permissionObj) {
        var type = $(row).data("type");
        if (type === "column") {
            if ($(row).find(".authorized.web .hidden").hasClass("selected")) {
                permissionObj["hidden"] = "true";
                if ($(row).find(".authorized.export .hidden").hasClass("selected")) {
                    permissionObj["include_export"] = "";
                    permissionObj["exclude_export"] = "";
                } else {
                    permissionObj["include_export"] = "true";
                    permissionObj["exclude_export"] = "";
                }
            } else {
                permissionObj["hidden"] = "false";
                if ($(row).find(".authorized.export .hidden").hasClass("selected")) {
                    permissionObj["include_export"] = "";
                    permissionObj["exclude_export"] = "true";
                } else {
                    permissionObj["include_export"] = "";
                    permissionObj["exclude_export"] = "";
                }
            }
        } else {
            if ($(options).find(".hidden").hasClass("selected")) {
                permissionObj["hidden"] = "true";
            } else {
                permissionObj["hidden"] = "";
            }
        }
    },
    updateElementOptionCustomBuilder : function(options, row, permissionObj) {
        if ($(options).hasClass("authorized")) {
            var property = $(row).find(".authorized").data("property");
            permissionObj[property] = $(options).find(".selected").data("value");
        } else {
            var property = $(row).find(".unauthorized").data("property");
            permissionObj[property] = $(options).find(".selected").data("value");
        }
    },
    editElementPlugin : function(row) {
        var permissionObj = null;
        
        var key = $(PermissionManager.container).find(".permission_rules .permission_rule.active").data("key");
        var element = $(row).data("element");
        if (key === "default") {
            permissionObj = element["properties"];
        } else if (element["properties"]["permission_rules"] !== undefined) {
            permissionObj = element["properties"]["permission_rules"][key];
        }
        
        var appPath = "/" + PermissionManager.options.appId + "/" + PermissionManager.options.appVersion;
        var propertiesDefinition = [
            {
                title : get_advtool_msg('adv.tool.permission'),
                properties : [{
                    name: 'permission',
                    label: get_advtool_msg('adv.tool.permission'),
                    type: 'elementselect',
                    options: PermissionManager.permissionOptions,
                    url: PermissionManager.options.contextPath + '/web/property/json'+appPath+'/getPropertyOptions'
                },
                {
                    name : 'permissionComment',
                    type : 'textarea',
                    label : get_advtool_msg('adv.permission.permissionComment')
                }]
            }
        ];
        
        var options = {
            appPath: appPath,
            contextPath: PermissionManager.options.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : permissionObj,
            changeCheckIgnoreUndefined: true,
            showCancelButton: true,
            saveCallback: function(container, properties) {
                permissionObj = $.extend(permissionObj, properties);
                if (key === "default") {
                    element["properties"] = permissionObj;
                } else {
                    if (element["properties"]["permission_rules"] === undefined) {
                        element["properties"]["permission_rules"] = {};
                    }
                    element["properties"]["permission_rules"][key] = permissionObj;
                }
                
                PermissionManager.updateElementPlugin(row, permissionObj);
                
                $(row).nextUntil(".support-plugin").each(function(){
                    PermissionManager.showElementPermission(key, $(this), $(row));
                });
                
                PermissionManager.updateJson();
            },
            cancelCallback : function() {}
        };
        PropertyEditor.Popup.showDialog(PermissionManager.options.builder + "-permission-element-container", options, {});
    },
    updateElementPlugin : function(row, permissionObj) {
        if (permissionObj["permission"] !== undefined 
                && permissionObj["permission"]["className"] !== undefined  
                && permissionObj["permission"]["className"] !== "") {

            var className = permissionObj["permission"]["className"];
            var pluginName = PermissionManager.permissionPlugins[className];
            if (pluginName === undefined) {
                pluginName = className + "(" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")";
            }

            $(row).find('.plugin-name span').text(pluginName);
        } else {
            $(row).find('.plugin-name span').text(get_advtool_msg('adv.permission.noPlugin'));
        }
        
        if (PermissionManager.options.builder === "form") {
            PermissionManager.updateElementPluginFormBuilder(row, permissionObj);
        } else if (PermissionManager.options.builder === "custom") {
            PermissionManager.updateElementPluginCustomBuilder(row, permissionObj);
        }
        
        if (permissionObj["permissionComment"] !== undefined) {
            $(row).find('.plugin .comment').text(permissionObj["permissionComment"]);
        }
    },
    updateElementPluginFormBuilder : function(row, permissionObj) {
        if (permissionObj["permission"] !== undefined 
                && permissionObj["permission"]["className"] !== undefined  
                && permissionObj["permission"]["className"] !== "") {
            if (permissionObj["permissionReadonly"] === "true") {
                $(row).find(".unauthorized .readonly").addClass("selected");
            } else {
                $(row).find(".unauthorized .hidden").addClass("selected");
            }
            $(row).find(".unauthorized").removeClass("disabled");
        } else {
            $(row).find(".unauthorized").addClass("disabled");
        }
    },
    updateElementPluginCustomBuilder : function(row, permissionObj) {
        if ($(row).find(".unauthorized").length > 0) {
            if (permissionObj["permission"] !== undefined 
                    && permissionObj["permission"]["className"] !== undefined  
                    && permissionObj["permission"]["className"] !== "") {
                var property = $(row).find(".unauthorized").data("property");
                var propertyValue = permissionObj[property];
                if (propertyValue === null || propertyValue === undefined) {
                    propertyValue = "";
                }
                $(row).find(".unauthorized a[data-value='"+propertyValue+"']").addClass("selected");
                $(row).find(".unauthorized").removeClass("disabled");
            } else {
                $(row).find(".unauthorized").addClass("disabled");
            }
        }
    },
    updateJson : function() {
        if (!PermissionManager.addToUndo) {
            if (PermissionManager.options.builder === "form") {
                FormBuilder.addToUndo(PermissionManager.json);
            } else if (PermissionManager.options.builder === "userview") {
                UserviewBuilder.addToUndo(PermissionManager.json);
            } else if (PermissionManager.options.builder === "datalist") {
                DatalistBuilder.addToUndo(PermissionManager.json);
            }
            PermissionManager.addToUndo = true;
        }
        
        var json;
        if (PermissionManager.options.builder === "form") {
            json = FormBuilder.generateJSON(true);
        } else if (PermissionManager.options.builder === "userview") {
            json = UserviewBuilder.getJson();
        } else if (PermissionManager.options.builder === "datalist") {
            json = DatalistBuilder.getJson();
            $('#list-json').val(json).trigger("change");
        } else if (PermissionManager.options.builder === "custom") {
            CustomBuilder.update();
            json = CustomBuilder.getJson();
        }
        
        AdvancedTools.json = json;
    },
    guid : function () {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
    }
};
