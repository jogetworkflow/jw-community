PermissionManager = {
    render: function(container) {
        if (CustomBuilder.paletteElements['permission-rule'] === undefined) {
            PermissionManager.initPermissionComponent();
        }
        
        PermissionManager.container = $(container);
        
        $(PermissionManager.container).html('<div class="permission_view"><div class="permission_rules"><div class="buttons"><a class="add_permission button"><i class="fas fa-plus"></i> ' + get_advtool_msg('adv.permission.addPermission') + '</a></div><div class="sortable"></div></div><div class="elements_container"></div></div>');
        PermissionManager.renderRules();
        PermissionManager.renderElementsHeader();
        PermissionManager.attachEvents();
    },
    /*
     * Create a dummy component for permission rules and plugins
     */
    initPermissionComponent : function() {
        CustomBuilder.initPaletteElement("", "permission-rule", "", "",[], "", false, "", {builderTemplate: {
            customPropertyOptions : function(elementOptions, element, elementObj, paletteElement){
                var propertiesDefinition;
                if (elementObj.properties.permission_key === undefined) {
                    propertiesDefinition = [
                        {
                            title : get_advtool_msg('adv.tool.permission') + " (" + get_advtool_msg('adv.permission.default') + ")",
                            properties : [{
                                name: 'permission',
                                label: get_advtool_msg('adv.tool.permission'),
                                type: 'elementselect',
                                options_callback: "CustomBuilder.getPermissionOptions",
                                url: CustomBuilder.contextPath + '/web/property/json'+CustomBuilder.appPath+'/getPropertyOptions'
                            }]
                        }
                    ];
                    if (CustomBuilder.config.advanced_tools.permission.supportNoPermisisonMessage === "true") {
                        propertiesDefinition[0]["properties"].push({
                            name : 'noPermissionMessage',
                            type : 'textarea',
                            label : get_advtool_msg('adv.permission.noPermissionMessage')
                        });  
                    }
                } else {
                    propertiesDefinition = [
                        {
                            title : get_advtool_msg('adv.tool.permission') + " (" + $(element).data("data").properties.permission_name + ")",
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
                                options_callback: "CustomBuilder.getPermissionOptions",
                                url: CustomBuilder.contextPath + '/web/property/json'+CustomBuilder.appPath+'/getPropertyOptions'
                            }]
                        }
                    ];
                }
                return propertiesDefinition;
            },
            'render' : function(element, elementObj, component, callback) {
                var name = get_advtool_msg('adv.permission.default');
                var pluginName = get_advtool_msg('adv.permission.noPlugin');

                if (elementObj.properties['permission_key'] !== undefined) {
                    name = elementObj.properties['permission_name'];
                }
                if (elementObj.properties['permission'] !== undefined && elementObj.properties['permission']["className"] !== undefined) {
                    var className = elementObj.properties['permission']["className"];
                    if (className !== "") {
                        pluginName = CustomBuilder.availablePermission[className];

                        if (pluginName === undefined) {
                            pluginName = className + "(" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")";
                        }
                    }
                }
                $(element).find(".name").text(name);
                $(element).find(".plugin_name").text(pluginName);
                $("body").addClass("no-right-panel");
            }
        }});
        
        CustomBuilder.initPaletteElement("", "permission-plugin", "", "", [
            {
                title : get_advtool_msg('adv.tool.permission'),
                properties : [{
                    name: 'permission',
                    label: get_advtool_msg('adv.tool.permission'),
                    type: 'elementselect',
                    options_callback: "CustomBuilder.getPermissionOptions",
                    url: CustomBuilder.contextPath + '/web/property/json'+CustomBuilder.appPath+'/getPropertyOptions'
                },
                {
                    name : 'permissionComment',
                    type : 'textarea',
                    label : get_advtool_msg('adv.permission.permissionComment')
                }]
            }
        ], "", false, "", {builderTemplate: {
            'render' : function(element, elementObj, component, callback) {
                PermissionManager.setActiveRule($(".permission_rules .permission_rule.active"));
                $("body").addClass("no-right-panel");
            }        
        }});
    },
    attachEvents: function() {
        $(PermissionManager.container).find(".sortable").sortable({
            opacity: 0.8,
            axis: 'y',
            handle: '.sort',
            tolerance: 'intersect',
            stop: function(event, ui) {
                PermissionManager.updateRulesOrder();
            }
        });
        $(PermissionManager.container).off("click", ".permission_rule:not(.active)");
        $(PermissionManager.container).on("click", ".permission_rule:not(.active)", function() {
            PermissionManager.setActiveRule($(this));
        });
        $(PermissionManager.container).off("click", ".permission_rule .edit_rule");
        $(PermissionManager.container).on("click", ".permission_rule .edit_rule", function(event) {
            PermissionManager.editRule($(this).closest(".permission_rule"));
            event.stopImmediatePropagation();
        });
        $(PermissionManager.container).off("click", ".permission_rule .delete_rule");
        $(PermissionManager.container).on("click", ".permission_rule .delete_rule", function(event) {
            PermissionManager.removeRule($(this).closest(".permission_rule"));
            event.stopImmediatePropagation();
        });
        $(PermissionManager.container).off("click", "tr .toggle-btn i");
        $(PermissionManager.container).on("click", "tr .toggle-btn i", function(event) {
            var tr = $(this).closest("tr");
            $(tr).toggleClass("collapsed");
            var level = $(tr).data("level");
            var cssClass = "";
            for (var i = level; i > 0; i--) {
                if (cssClass !== "") {
                    cssClass += ", ";
                }
                cssClass += "tr.level-" + i;
            }
            if ($(tr).hasClass("collapsed")) {
                $(tr).nextUntil(cssClass).hide();
            } else {
                $(tr).nextUntil(cssClass).removeClass("collapsed").show();
            }
        });
        if ($(PermissionManager.container).find(".sortable .permission_rule").length > 0) {
            PermissionManager.setActiveRule($(PermissionManager.container).find(".sortable .permission_rule:eq(0)"));
        } else {
            PermissionManager.setActiveRule($(PermissionManager.container).find(".permission_rules > .permission_rule"));
        }
        $(PermissionManager.container).find(".add_permission").off("click");
        $(PermissionManager.container).find(".add_permission").on("click", function() {
            PermissionManager.addRule();
        });
    },
    getRuleElement: function() {
        var ruleObject = CustomBuilder.data.properties;
        if (CustomBuilder.config.builder.callbacks["getRuleObject"] !== undefined && CustomBuilder.config.builder.callbacks["getRuleObject"] !== "") {
            ruleObject = CustomBuilder.callback(CustomBuilder.config.builder.callbacks["getRuleObject"], []);
        }
        return ruleObject;
    },
    renderRules: function() {
        var ruleObj = PermissionManager.getRuleElement();
        if (ruleObj["permission_rules"] !== undefined) {
            for (var i in ruleObj["permission_rules"]) {
                PermissionManager.renderRule(ruleObj["permission_rules"][i]);
            }
        }
        PermissionManager.renderRule(ruleObj);
    },
    renderRule: function(obj, prepend) {
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
                pluginName = CustomBuilder.availablePermission[className];
                if (pluginName === undefined) {
                    pluginName = className + "(" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")";
                }
            }
        }
        var rule = $('<div class="permission_rule"><div class="sort"></div><div class="name"></div><div class="plugin"><span class="plugin_name"></span></div><div class="rule-buttons"><a class="edit_rule btn"><i class="las la-edit"></i></a><a class="delete_rule btn"><i class="la la-trash"></i></a></div></div>');
        $(rule).data("key", key);
        $(rule).attr("id", "permission-rule-" + key);
        $(rule).data("data", {className: "permission-rule", properties : obj});
        $(rule).find(".name").text(name);
        $(rule).find(".plugin_name").text(pluginName);
        if (!isDefault) {
            $(rule).find(".name").addClass("visible");
            if (prepend) {
                $(PermissionManager.container).find(".permission_rules .sortable").prepend(rule);
            } else {
                $(PermissionManager.container).find(".permission_rules .sortable").append(rule);
            }
            $(rule).find(".name").editable(function(value, settings) {
                if (value === "") {
                    value = get_advtool_msg('adv.permission.unnamed');
                }
                obj['permission_name'] = value;
                CustomBuilder.update();
                return value;
            }, {
                type: 'text',
                tooltip: '',
                select: true,
                style: 'inherit',
                cssclass: 'labelEditableField',
                onblur: 'submit',
                rows: 1,
                width: '80%',
                minwidth: 80,
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
    renderElementsHeader: function() {
        var container = $(PermissionManager.container).find('.elements_container');
        $(container).append('<table class="permission-table"><thead><tr><th width="50%"></th></tr></thead><tbody></tbody></table>');
        var row = $(container).find("thead tr");
        
        var authorizedOptions = CustomBuilder.config.advanced_tools.permission.authorized;
        if (authorizedOptions !== undefined && authorizedOptions.property !== undefined && authorizedOptions.property !== "") {
            var label = get_advtool_msg('adv.permission.authorized');
            if (authorizedOptions.label !== undefined && authorizedOptions.label !== null) {
                label = authorizedOptions.label;
            }
            $(row).append('<th class="authorized" width="30%">' + label + '</th>');
        }
        
        var unauthorizedOptions = CustomBuilder.config.advanced_tools.permission.unauthorized;
        if (unauthorizedOptions !== undefined && unauthorizedOptions.property !== undefined && unauthorizedOptions.property !== "") {
            var label = get_advtool_msg('adv.permission.unauthorized');
            if (unauthorizedOptions.label !== undefined && unauthorizedOptions.label !== null) {
                label = unauthorizedOptions.label;
            }
            $(row).append('<th class="unauthorized" width="30%">' + label + '</th>');
        }
    },
    addRule: function() {
        var rule = {
            permission_key: PermissionManager.guid(),
            permission_name: get_advtool_msg('adv.permission.unnamed'),
            permission: {
                className: "",
                properties: []
            }
        };
        var ruleObj = PermissionManager.getRuleElement();
        if (ruleObj["permission_rules"] === undefined) {
            ruleObj["permission_rules"] = [];
        }
        ruleObj["permission_rules"].unshift(rule);
        var ruleElm = PermissionManager.renderRule(rule, true);
        PermissionManager.setActiveRule($(ruleElm));
        CustomBuilder.update();
    },
    removeRule: function(rule) {
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
            CustomBuilder.update();
        }
    },
    editRule: function(rule) {
        $("body").addClass("no-right-panel");

        var self = CustomBuilder.Builder;

        var data = $(rule).data("data");
        var props = data.properties;
        CustomBuilder.Builder.selectedEl = $(rule);
        CustomBuilder.editProperties("permission-rule", props, data, $(rule));

        $("#style-properties-tab-link").hide();
        $("#right-panel #style-properties-tab").find(".property-editor-container").remove();
        $("body").removeClass("no-right-panel");
    },
    updateRulesOrder: function() {
        var ruleObj = PermissionManager.getRuleElement();
        var newRules = [];
        $(PermissionManager.container).find(".permission_rules .sortable .permission_rule").each(function() {
            newRules.push($(this).data("data")["properties"]);
        });
        ruleObj["permission_rules"] = newRules;
        CustomBuilder.update();
    },
    removeElementsPermission: function(key) {
        $(PermissionManager.container).find(".permission-table tbody tr").each(function() {
            var self = CustomBuilder.Builder;
            var data = $(this).data("element");
            if (data !== null && data !== undefined) {
                var props = self.parseElementProps(data);

                delete props["permission_rules"][key];
            }
        });
    },
    setActiveRule: function(rule) {
        $(PermissionManager.container).find(".permission_rule").removeClass("active");
        $(rule).addClass("active");
        var key = $(rule).data("key");
        
        var container = $(PermissionManager.container).find('.elements_container table tbody');
        $(container).html("");
        var customRender = CustomBuilder.config.advanced_tools.permission.render_elements_callback;
        if (customRender !== undefined && customRender !== "") {
            CustomBuilder.callback(CustomBuilder.config.advanced_tools.permission.render_elements_callback, [container, key]);
        } else {
            PermissionManager.renderElement(CustomBuilder.data, container, key);
        }
        
        for (var i = 1; i < 9; i++) {
            $(container).find(".level-"+i).each(function(){
                var last = $(this).nextUntil(".level-"+i, ".level-"+(i+1)).last().addClass("last-"+i);
                $(last).nextUntil(".level-"+i).addClass("last-"+i);
            });
        }
    },
    renderElement: function(data, container, key, level) {
        var self = CustomBuilder.Builder;
        
        if (key === undefined) {
            key = $(".permission_rules .permission_rule.active").data("key");
        }
        if (level === undefined) {
            if ($(container).find("tr.header").length > 0) {
                level = 1;
                
                if ($(container).find("tr.header:not(.level-1)").length > 0) {
                    var header = $(container).find("tr.header:not(.level-1)");
                    $(header).addClass("level-1").data('level', 1);
                    $(header).find("td:eq(0)").addClass("element-meta");
                    var label = $(header).find("td:eq(0)").text();
                    $(header).find("td:eq(0)").html('<div><div class="toggle"><span class="toggle-btn"><i class="las la-plus-square"></i><i class="las la-minus-square"></i></span></div><span class="header-label">'+label+'</span></div>');
                }
            } else {
                level = 0;
            }
        }
        
        if (data !== null && data !== undefined && !PermissionManager.isIgnoreRendering(data)) {
            level = level+1;
            var row = $('<tr class="level-'+level+'" data-level="'+level+'"><td class="element-meta"><div><div class="toggle"><span class="toggle-btn"><i class="las la-plus-square"></i><i class="las la-minus-square"></i></span></div></div></td></tr>');
            $(row).data("element", data);
            $(container).append(row);
            
            if (level > 1) {
                for (var i = 1; i < level; i++) {
                    $(row).find('.toggle').prepend('<span class="bar"></span>');
                }
            }
            
            var prev = $(row).prev();
            if (prev.length > 0 && $(prev).hasClass("level-" + (level - 1))) {
                $(prev).addClass("has-child");
            }
            
            PermissionManager.renderElementMeta($(row).find(".element-meta > div"), data);
            
            var props = self.parseElementProps(data);
        
            var permissionObj = props;
            if (key !== "default") {
                if (props["permission_rules"] === undefined) {
                    props["permission_rules"] = {};
                }
                if (props["permission_rules"][key] === undefined) {
                    props["permission_rules"][key] = {};
                }
                permissionObj = props["permission_rules"][key];
            }
            
            if (PermissionManager.isElementSupportPlugin(data)) {
                $(row).addClass("support-plugin");
                $(row).find(".element-meta > div").append('<div class="permission-plugin"><a class="edit-permission-plugin-btn"><span class="name"></span> <i class="las la-edit"></i></a></div>');
                var pluginName = get_advtool_msg('adv.permission.noPlugin');
                var className = "";
                if (permissionObj["permission"] !== undefined 
                    && permissionObj["permission"]["className"] !== undefined  
                    && permissionObj["permission"]["className"] !== "") {

                    className = permissionObj["permission"]["className"];
                    var pluginName = CustomBuilder.availablePermission[className];
                    if (pluginName === undefined) {
                        pluginName = className + "(" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")";
                    }
                }
                $(row).find(".permission-plugin .name").text(pluginName);

                if (permissionObj["permissionComment"] !== undefined && permissionObj["permissionComment"] !== "") {
                    $(row).find(".permission-plugin").append('<div class="permission-comment"><i class="lar la-comment" title="Comment"></i> '+permissionObj["permissionComment"]+'</div>');
                }

                $(row).find(".edit-permission-plugin-btn").on("click", function() {
                    PermissionManager.editPermissionPlugin( $(row), permissionObj);
                });
            }
            
            var renderDefault = true;
            if ($("body").hasClass("default-builder")) {
                var self = CustomBuilder.Builder;
                var component = self.parseDataToComponent(data);
                if (component !== null && component.builderTemplate.renderPermission !== undefined) {
                    component.builderTemplate.renderPermission($(row), data, permissionObj, key, level);
                    renderDefault = false;
                }
            }
            if (renderDefault) {
                PermissionManager.renderElementDefault(data, $(row), permissionObj, key, level);
            }
        }
        
        var childsProps = CustomBuilder.config.advanced_tools.permission.childs_properties;
        if (childsProps !== null && childsProps !== undefined) {
            for (var i in childsProps) {
                if (data[childsProps[i]] !== undefined && data[childsProps[i]] !== null && data[childsProps[i]].length > 0) {
                    $.each(data[childsProps[i]], function(i, child){
                        PermissionManager.renderElement(child, container, key, level);
                    });
                }
            }
        }
    },
    renderElementDefault: function(data, row, permissionObj, key, level) {
        var authorizedOptions = CustomBuilder.config.advanced_tools.permission.authorized;
        if (authorizedOptions !== undefined && authorizedOptions.property !== undefined && authorizedOptions.property !== "") {
            $(row).append('<td class="authorized" width="30%"><div class="authorized-btns btn-group"></div></td>');
            var value = permissionObj[authorizedOptions.property];
            if (value === undefined || value === null) {
                value = authorizedOptions.default_value;
            }
            for (var i in authorizedOptions.options) {
                var active = "";
                if (authorizedOptions.options[i]['value'] === value) {
                    active = "active";
                }
                var disableChild = "";
                if (authorizedOptions.options[i]['disableChild'] === true) {
                    disableChild = "data-disable-child";
                }
                $(row).find(".authorized-btns").append('<button type="button" class="' + active + ' btn btn-outline-success btn-sm ' + authorizedOptions.options[i]['key'] + '-btn" data-value="' + authorizedOptions.options[i]['value'] + '" ' + disableChild + '>' + authorizedOptions.options[i]['label'] + '</button>');
            }
            var parentRow = $(row).prevAll(".level-" + (level - 1)).first();
            if ($(parentRow).length > 0 && !$(parentRow).hasClass("header") && ($(parentRow).find(".authorized-btns [data-disable-child]").hasClass("active") || $(parentRow).find(".authorized-btns [data-disable-child]").attr("disabled") === "disabled")) {
                $(row).find(".authorized-btns .btn").attr("disabled", "disabled");
            }
            $(row).on("click", ".authorized-btns .btn", function(event) {
                if ($(this).hasClass("active")) {
                    return false;
                }
                var group = $(this).closest(".btn-group");
                group.find(".active").removeClass("active");
                $(this).addClass("active");
                if ($(this).attr("data-disable-child") !== undefined) {
                    $(row).nextUntil(".level-" + level).each(function() {
                        $(this).find(".authorized-btns .btn").attr("disabled", "disabled");
                    });
                } else {
                    $(row).nextUntil(".level-" + level).each(function() {
                        $(this).find(".authorized-btns .btn").removeAttr("disabled");
                    });
                }
                var selectedValue = $(this).data("value");
                permissionObj[authorizedOptions.property] = "" + selectedValue;
                CustomBuilder.update();
                event.preventDefault();
                return false;
            });
        }
        var unauthorizedOptions = CustomBuilder.config.advanced_tools.permission.unauthorized;
        if (unauthorizedOptions !== undefined && unauthorizedOptions.property !== undefined && unauthorizedOptions.property !== "") {
            $(row).append('<td class="unauthorized" width="30%"><div class="unauthorized-btns btn-group"></div></td>');
            var value = permissionObj[unauthorizedOptions.property];
            if (value === undefined || value === null) {
                value = unauthorizedOptions.default_value;
            }
            for (var i in unauthorizedOptions.options) {
                var active = "";
                if (unauthorizedOptions.options[i]['value'] === value) {
                    active = "active";
                }
                var disableChild = "";
                if (unauthorizedOptions.options[i]['disableChild'] === true) {
                    disableChild = "data-disable-child";
                }
                $(row).find(".unauthorized-btns").append('<button type="button" class="' + active + ' btn btn-outline-danger btn-sm ' + unauthorizedOptions.options[i]['key'] + '-btn" data-value="' + unauthorizedOptions.options[i]['value'] + '" ' + disableChild + '>' + unauthorizedOptions.options[i]['label'] + '</button>');
            }
            var parentRow = $(row).prevAll(".level-" + (level - 1)).first();
            if ($(parentRow).length > 0 && !$(parentRow).hasClass("header") && ($(parentRow).find(".unauthorized-btns [data-disable-child]").hasClass("active") || $(parentRow).find(".unauthorized-btns [data-disable-child]").attr("disabled") === "disabled")) {
                $(row).find(".unauthorized-btns .btn").attr("disabled", "disabled");
            }
            $(row).on("click", ".unauthorized-btns .btn", function(event) {
                if ($(this).hasClass("active")) {
                    return false;
                }
                var group = $(this).closest(".btn-group");
                group.find(".active").removeClass("active");
                $(this).addClass("active");
                if ($(this).attr("data-disable-child") !== undefined) {
                    $(row).nextUntil(".level-" + level).each(function() {
                        $(this).find(".unauthorized-btns .btn").attr("disabled", "disabled");
                    });
                } else {
                    $(row).nextUntil(".level-" + level).each(function() {
                        $(this).find(".unauthorized-btns .btn").removeAttr("disabled");
                    });
                }
                var selectedValue = $(this).data("value");
                permissionObj[unauthorizedOptions.property] = "" + selectedValue;
                CustomBuilder.update();
                event.preventDefault();
                return false;
            });
        }
    },
    isIgnoreRendering: function(element) {
        var customRender = CustomBuilder.config.advanced_tools.permission.check_ignore_rendering_callback;
        if (customRender !== undefined && customRender !== "") {
            return CustomBuilder.callback(CustomBuilder.config.advanced_tools.permission.check_ignore_rendering_callback, [element]);
        } else {
            var ignore_classes = CustomBuilder.config.advanced_tools.permission.ignore_classes;
            if (element["className"] === null || element["className"] === undefined || $.inArray(element["className"], ignore_classes) !== -1) {
                return true;
            } else {
                return false;
            }
        }
    },
    isElementSupportPlugin : function(data) {
        var classes = CustomBuilder.config.advanced_tools.permission.element_support_plugin;
        if (data["className"] !== undefined && $.inArray(data["className"], classes) !== -1) {
            return true;
        } else {
            return false;
        }
    },
    renderElementMeta : function(container, data) {
        var self = CustomBuilder.Builder;
        var component = self.parseDataToComponent(data);
        var props = self.parseElementProps(data);

        if (component.builderTemplate !== undefined && component.builderTemplate.customPropertiesData) {
            props = component.builderTemplate.customPropertiesData(props, data, component);
        }

        var label = component.label;
        if (component.builderTemplate !== undefined && component.builderTemplate.getLabel) {
            label = component.builderTemplate.getLabel(data, component);
        } else if (CustomBuilder.config.advanced_tools.permission.element_label_callback !== "") {
            var tempLabel = CustomBuilder.callback(CustomBuilder.config.advanced_tools.permission.element_label_callback, [data]);
            if (tempLabel !== null && tempLabel !== undefined) {
                label = tempLabel;
            }
        }
        
        if (props.label !== undefined && props.label !== "") {
            label = props.label;
        } else if (props.textContent !== undefined && props.textContent !== "") {
            label = UI.escapeHTML(props.textContent);
            if (label.length > 30) {
                label += label.substring(0, 27) + "...";
            }
        }
        
        $(container).append('<span class="element-icon">' + component.icon + '</span>');
        $(container).append('<span class="element-label">' + label + '</span>');
        
        if (CustomBuilder.config.advanced_tools.permission.display_element_id) {
            var fieldId = "id";
            if (CustomBuilder.config.advanced_tools.permission.element_id_field) {
                fieldId = CustomBuilder.config.advanced_tools.permission.element_id_field;
            }
            if (props[fieldId] !== undefined && props[fieldId] !== "" && props[fieldId].length < 32) {
                $(container).append('<span class="element-id">' + props[fieldId] + '</span>');
            }
        }
    },
    editPermissionPlugin : function(element, permissionObj) {
        var self = CustomBuilder.Builder;
        
        CustomBuilder.editProperties("permission-plugin", permissionObj, {className: "permission-plugin", properties : permissionObj}, element);
        
        $("#style-properties-tab-link").hide();
        $("#right-panel #style-properties-tab").find(".property-editor-container").remove();
        $("body").removeClass("no-right-panel");
    },
    guid: function() {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
    }
};