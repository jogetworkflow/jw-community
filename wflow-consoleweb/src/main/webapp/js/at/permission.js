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
        } else {
            return null; 
        }
    },
    getRootElement : function() {
        if (PermissionManager.options.builder === "form") {
            return $(".form-container-div form");
        } else if (PermissionManager.options.builder === "userview") {
            return UserviewBuilder.data;
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
        
        var root = PermissionManager.getRootElement();
        PermissionManager.renderElement(root, tbody);
    },
    renderElementsHeader : function(row) {
        $(row).append('<th class="authorized" width="30%">'+get_advtool_msg('adv.permission.authorized')+'</th>');
        if (PermissionManager.options.builder === "form") {
            $(row).append('<th class="unauthorized" width="20%">'+get_advtool_msg('adv.permission.unauthorized')+'</th>');
        }
    },
    renderElement : function(elementObj, tbody, parentElement) {
        var element = PermissionManager.getElement(elementObj);
        
        if (element !== null && element !== undefined && !PermissionManager.isIgnoreRendering(element)) {
            var row = $('<tr><td class="element-meta"><div class="element-class"></div></td></tr>');
            $(row).data("element", element);
            
            if (parentElement !== null && parentElement !== undefined) {
                $(row).data("parent-element", parentElement);
            }
            
            var pluginLabel = PermissionManager.getElementPluginLabel(element);

            if (pluginLabel !== null) {
                $(row).find(".element-class").text(pluginLabel);

                if (element["properties"]["label"] !== undefined) {
                    $(row).find(".element-meta").append('<span class="element-label">' + UI.escapeHTML(element["properties"]["label"]) + '</span>');
                }
                if (element["properties"]["id"] !== undefined) {
                    var eid = element["properties"]["id"];
                    if (PermissionManager.options.builder === "userview") {
                        if (element["properties"]["customId"] !== undefined) {
                            eid = element["properties"]["customId"];
                        } else {
                            eid = "";
                        }
                    }
                    $(row).find(".element-meta").append('<span class="element-id">' + eid + '</span>');
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
    getElement : function(element) {
        if (PermissionManager.options.builder === "form") {
            return $(element)[0].dom;
        } else if (PermissionManager.options.builder === "userview") {
            return element;
        } else {
            return null;
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
        } else {
            return null;
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
        } else {
            return true;
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
                    },
                    {
                        name : 'noPermissionMessage',
                        type : 'textarea',
                        label : get_advtool_msg('adv.permission.noPermissionMessage')
                    }]
                }
            ];
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
            ruleObj.properties = $.extend(ruleObj.properties, properties);
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
        $(PermissionManager.container).find(".permission-table tbody tr").each(function(){
            var element = $(this).data("element");
            if (element["properties"]["permission_rules"] !== undefined && element["properties"]["permission_rules"][key] !== undefined) {
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
        $(PermissionManager.container).find(".permission-table tbody tr").each(function(){
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
        if (key === "default") {
            permissionObj = element["properties"];
        } else if (element["properties"]["permission_rules"] !== undefined) {
            permissionObj = element["properties"]["permission_rules"][key];
        }
        
        if (PermissionManager.options.builder === "form") {
            PermissionManager.showElementPermissionFormBuilder(key, row, parentRow, permissionObj);
        } else {
            PermissionManager.showElementPermissionUserviewBuilder(key, row, parentRow, permissionObj);
        }
    },
    showElementPermissionFormBuilder : function(key, row, parentRow, permissionObj) {
        if ($(row).hasClass("support-plugin")) {
            $(row).find(".element-meta").append('<div class="plugin"><div class="plugin-name"><a class="edit-plugin btn"><label>'+get_advtool_msg('adv.tool.permission')+':</label> <span></span> <i class="far fa-edit"></i></a></div><div class="comment"></div></div>');
            
            if (permissionObj !== null && permissionObj !== undefined) {
                if (permissionObj["readonly"] === "true") {
                    $(row).find(".authorized .readonly").addClass("selected");
                } else if (permissionObj["readonly"] === "hidden") {
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
                    } else if (permissionObj["readonly"] === "hidden") {
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
                } else if (permissionObj["hide"] === "deny") {
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
                    if (permissionObj["permissionHidden"] === "yes") {
                        $(row).find(".authorized .hidden").addClass("selected");
                    } else if (permissionObj["permissionHidden"] === "deny") {
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
    updateElementOption : function(action, row, update) {
        var options = $(action).closest("td");
        $(options).find("a").removeClass("selected");
        $(action).addClass("selected");
        
        var element = $(row).data("element");
        var key = $(PermissionManager.container).find(".permission_rules .permission_rule.active").data("key");
        
        var permissionObj = null;
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
        
        if (PermissionManager.options.builder === "form") {
            PermissionManager.updateElementOptionFormBuilder(options, row, permissionObj);
        } else if (PermissionManager.options.builder === "userview") {
            PermissionManager.updateElementOptionUserviewBuilder(options, row, permissionObj);
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
            } else if ($(options).find(".hidden").hasClass("selected")) {
                permissionObj["readonly"] = "hidden";
            } else {
                permissionObj["readonly"] = "";
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
            } else if ($(options).find(".hidden").hasClass("selected")) {
                permissionObj["hide"] = "yes";
            } else {
                permissionObj["hide"] = "deny";
            }
        } else {
            if ($(options).find(".accessible").hasClass("selected")) {
                permissionObj["permissionHidden"] = "";
            } else if ($(options).find(".hidden").hasClass("selected")) {
                permissionObj["permissionHidden"] = "yes";
            } else {
                permissionObj["permissionHidden"] = "deny";
            }
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
    updateJson : function() {
        if (!PermissionManager.addToUndo) {
            if (PermissionManager.options.builder === "form") {
                FormBuilder.addToUndo(PermissionManager.json);
            } else if (PermissionManager.options.builder === "userview") {
                UserviewBuilder.addToUndo(PermissionManager.json);
            }
            PermissionManager.addToUndo = true;
        }
        
        var json;
        if (PermissionManager.options.builder === "form") {
            json = FormBuilder.generateJSON(true);
        } else if (PermissionManager.options.builder === "userview") {
            json = UserviewBuilder.getJson();
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
