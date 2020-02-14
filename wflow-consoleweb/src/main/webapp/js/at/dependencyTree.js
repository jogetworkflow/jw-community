$.jstree.plugins.joget = function (options, parent) {
    this.redraw_node = function(obj, deep, callback, force_draw) {
        obj = parent.redraw_node.call(this, obj, deep, callback, force_draw);
        
        if (obj) {
            var node = this.get_node(jQuery(obj).attr('id'));
            if (node && node.data && ("value" in node.data) && (node.data["value"] !== "")) {
                $(obj).find("> .jstree-anchor").append("<strong>"+node.data["value"]+"</strong>");
            }
            if (node && node.data && ("indicators" in node.data) && (node.data["indicators"].length > 0)) {
                var inds = $('<span class="indicators"></span>');
                
                for (var i in node.data["indicators"]) {
                    var ind = node.data["indicators"][i];
                    var a = $('<a class="indicator">');
        
                    if (ind.callback !== undefined && ind.callback !== null && $.type(ind.callback) === "string") {
                        $(a).attr("href", ind.callback);
                        $(a).attr("target", "_blank");
                    } else if (ind.callback !== undefined && ind.callback !== null  && $.type(ind.callback) === "number") {
                        $(a).attr("data-callback", ind.callback);
                    }
                    if (ind.color !== undefined && ind.color !== "") {
                        $(a).addClass("indicator-"+ind.color);
                    }
                    var slash = "";
                    if (ind.slash) {
                        slash = '<i class="fas fa-slash fa-stack-1x fa-inverse"></i>';
                    }
                    $(a).append('<span class="fa-stack"><i class="fas fa-circle fa-stack-2x"></i>'+slash+'<i class="'+ind.icon+' fa-stack-1x fa-inverse"></i></span>');
                    if (ind.label !== undefined && ind.label !== "") {
                        $(a).attr("title", ind.label);
                        $(a).attr('aria-label', ind.label);
                    }
                    $(inds).append(a);
                }
                
                $(obj).find("> .jstree-anchor").after(inds);
            }
        }
        return obj;
    };
};

$.jstree.defaults.joget = {};

DependencyTree = {
    elements : []
};
DependencyTree.Matchers = {};

DependencyTree.Node = {
    text : "",
    type : "default",
    children : [],
    data : {
        indicators : []
    },
    li_attr : {},
    a_attr : {
        class : ''
    }
};

DependencyTree.Util = {
    cleanData: function(node) {
        node.data["parent"] = null;
        node.data["properties"] = null;
        node.data["propertiesDef"] = null;
        for (var i in node.children) {
            DependencyTree.Util.cleanData(node.children[i]);
        }
    },
    runMatchers : function (viewer, deferreds, node, jsonObj, refObj, matchers) {
        if (matchers === undefined) {
            matchers = DependencyTree.Matchers;
        }
        
        for (var i in matchers) {
            if (matchers[i].match(viewer, deferreds, node, jsonObj, refObj) && matchers[i].matchers !== undefined) {
                DependencyTree.Util.runMatchers(viewer, deferreds, node, jsonObj, refObj, matchers[i].matchers);
            }
        }
    },
    pluginPropertiesWalker : function (viewer, node, labelNode, jsonObj, deffer, properties) {
        if (jsonObj['properties']['label'] !== undefined && jsonObj['properties']['label'] !== "") {
            labelNode.data['value'] += ' (' + UI.escapeHTML(jsonObj['properties']['label']) + ')';
        } else if (jsonObj['properties']['name'] !== undefined && jsonObj['properties']['name'] !== "") {
            labelNode.data['value'] += ' (' + UI.escapeHTML(jsonObj['properties']['name']) + ')';
        }
        
        node.data['propertiesDef'] = properties;
        var newDeferreds = [];

        for (var i in properties) {
            var page = properties[i];
            if (page['properties'] !== undefined) {
                for (var j in page['properties']) {
                    var prop = page['properties'][j];
                    var name = prop['name'];
                    var cjsonObj = jsonObj['properties'][name];
                    if (cjsonObj === undefined) {
                        cjsonObj = "";
                    }
                    if (name !== undefined) {
                        var cnode = $.extend(true, {}, DependencyTree.Node);
                        cnode.data['parent'] = node;
                        node.children.push(cnode);
                        DependencyTree.Util.runMatchers(viewer, newDeferreds, cnode, cjsonObj, prop);
                    }
                }
            }
        }
        
        $.when.apply($, newDeferreds).then(function(){
            deffer.resolve();
        });
    },
    processUrl: function (viewer, url){
        if(url === null || url === undefined){
            return url;
        }
        var regX = /\[CONTEXT_PATH\]/g;
        url = url.replace(regX, viewer.options.contextPath);
        
        regX = /\[APP_PATH\]/g;
        url = url.replace(regX, "/"+viewer.options.appId+'/'+viewer.options.appVersion);
        
        return url;
    },
    hideAndCall: function (viewer, callback) {
        callback(viewer);
    },
    addIndicator: function (viewer, node, icon, label, color, callback, slash) {
        if (color === undefined) {
            color = "";
        }
        if (slash === undefined) {
            slash = false;
        }
        if (callback === undefined) {
            callback = null;
        }
        
        if ($.type(callback) === "function") {
            var callbacks = viewer.callbacks;
            callbacks.push(callback);
            callback = callbacks.length - 1;
        }
        
        var ind = {
            icon : icon,
            color : color,
            label : label,
            slash : slash,
            callback : callback
        };
            
        node.data["indicators"].push(ind);
        
        if (icon.indexOf("pwaoffline") !== -1) {
            if (viewer['warning']['pwaoffline'] === undefined) {
                viewer['warning']['pwaoffline'] = [];
            }
            viewer['warning']['pwaoffline'].push(ind);
        } else if (icon.indexOf("missingplugin") !== -1) {
            if (!(node.data["className"] === "org.joget.apps.userview.model.Userview" 
                    || node.data["className"] === "org.joget.apps.userview.model.UserviewCategory")) {
                if (viewer['warning']['missingplugin'] === undefined) {
                    viewer['warning']['missingplugin'] = [];
                }
                viewer['warning']['missingplugin'].push(ind);
            }
        } else if (icon.indexOf("missingelement") !== -1) {
            if (viewer['warning']['missingelement'] === undefined) {
                viewer['warning']['missingelement'] = [];
            }
            viewer['warning']['missingelement'].push(ind);
        }
    },
    createEditIndicator: function (viewer, node, callback) {
        DependencyTree.Util.addIndicator(viewer, node, 'fas fa-pencil-alt', get_advtool_msg("dependency.tree.Edit.Properties"), "green", function() {
            DependencyTree.Util.hideAndCall(viewer, callback);
        });
    },
    getValueFromObject: function (obj, name) {
        try {
            var parts = name.split(".");
            var value = null;
            if (parts[0] !== undefined && parts[0] !== "") {
                value = obj[parts[0]];
            }
            if (parts.length > 1) {
                for (var i = 1; i < parts.length; i ++) {
                    value = value[parts[i]];
                }
            }
            
            return value;
        } catch (err) {};
        return null;
    },
    getValue: function(response, refObj, jsonObj) {
        if(response !== undefined && response !== null){
            var options = $.parseJSON(response);
            if (refObj['options_ajax_mapping'] !== undefined) {
                var mapping = refObj['options_ajax_mapping'];
                if (mapping.arrayObj !== undefined) {
                    options = DependencyTree.Util.getValueFromObject(options, mapping.arrayObj);
                }
                                
                for (var o in options) {
                    var v = "";
                    if (mapping.value !== undefined) {
                        if (DependencyTree.Util.getValueFromObject(options[o], mapping.value) === jsonObj) {
                            return DependencyTree.Util.getValueFromObject(options[o], mapping.label);
                        }
                    } else {
                        if (options[o].value === jsonObj) {
                            return options[o].label;
                        }
                    }
                }
            }
        }
        return jsonObj;
    }
};

DependencyTree.Matchers['pluginOrElementSelect'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (jsonObj['className'] !== undefined) {
            if (refObj !== undefined && refObj['label'] !== undefined && refObj['label'] !== "") {
                node.text = refObj['label'] + ' : ';
            }
            
            if (refObj === undefined || (refObj !== undefined && refObj['url'] !== undefined && refObj['url'].indexOf("getPropertyOptions") !== -1)) {
                node.data['isPlugin'] = true;
                if (jsonObj['className'] !== "") {
                    var pluginClassName = jsonObj['className'];

                    node.data['className'] = pluginClassName;
                    if (viewer.pluginList[pluginClassName] !== undefined) {
                        node.data['value'] = viewer.pluginList[pluginClassName].label;
                        return true;
                    } else {
                        node.data['value'] = pluginClassName;

                        if (node.data["indicators"] === undefined) {
                            node.data["indicators"] = [];
                        }
                        
                        DependencyTree.Util.addIndicator(viewer, node, 'missingplugin fas fa-plug', get_advtool_msg("dependency.tree.Missing.Plugin"), "red", null, true);
                    }
                } else {
                    node.a_attr['class'] += " gray";
                }
            } else if (refObj !== undefined && refObj['url'] !== undefined && refObj['url'].indexOf("getPropertyOptions") === -1) {
                node.data['isElementSelect'] = true;
                var value = jsonObj['className'];
                
                //process label by reusing option matchers
                DependencyTree.Matchers['string']['matchers']['options'].match(viewer, deferreds, node, value, refObj);
                
                if (node.data['value'] === undefined || node.data['value'] === "") {
                    node.a_attr['class'] += " gray";
                }
                
                return true;
            }
        }
        return false;
    },
    matchers : {
        'properties' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties'] !== undefined && !$.isEmptyObject(jsonObj['properties'])) {
                    var getProperties = $.Deferred();
                    deferreds.push(getProperties);
                    
                    var pnode = node;
                    var customBuilderHasChilds = false;
                    if (AdvancedTools.options.builder === "custom") {
                        var childsproperties = CustomBuilder.config.advanced_tools.tree_viewer.childs_properties;
                        for (var i in childsproperties) {
                            if (jsonObj[childsproperties[i]] !== undefined) {
                                customBuilderHasChilds = true;
                                break;
                            }
                        }
                    }
                    if (node.data['className'] === "org.joget.apps.form.model.Form" 
                            || node.data['className'] === "org.joget.apps.form.model.Section" 
                            || node.data['className'] === "org.joget.apps.form.model.Column"
                            || customBuilderHasChilds) {
                        pnode = $.extend(true, {}, DependencyTree.Node);
                        pnode.text = get_advtool_msg("dependency.tree.Properties");
                        pnode.a_attr['class'] += " properties";
                        pnode.type = 'properties';
                        pnode.data['parent'] = node;
                        node.children.push(pnode);
                    }
                    
                    pnode.data['properties'] = jsonObj['properties'];
                    
                    var url;
                    if (refObj !== undefined && refObj['url'] !== undefined && refObj['url'].indexOf("getPropertyOptions") === -1) {
                        url = refObj['url'];
                    }
            
                    viewer.getPluginProperties(jsonObj['className'], function(properties) {
                        DependencyTree.Util.pluginPropertiesWalker(viewer, pnode, node, jsonObj, getProperties, properties);
                        
                        if ((typeof viewer.pluginList[node.data['className']]) !== "undefined") {
                            var checkpwa = true;
                            if(pnode.data.isUserviewMenu){
                                if(jsonObj['properties']['enableOffline'] !== "true") {
                                    checkpwa = false;
                                }
                            }

                            if (checkpwa) {
                                var pwaOffline = viewer.pluginList[node.data['className']].pwaValidation;
                                if (pwaOffline === "notSupported") {
                                    DependencyTree.Util.addIndicator(viewer, node, 'pwaoffline fas fa-wifi', get_advtool_msg("pwa.notSupported"), "red", null, true);
                                } else if (pwaOffline === "readonly") {
                                    DependencyTree.Util.addIndicator(viewer, node, 'pwaoffline fas fa-wifi', get_advtool_msg("pwa.readonly"), "yellow", null, true);
                                } else if (pwaOffline === "checking") {
                                    var checkPwaDeferred = $.Deferred();
                                    deferreds.push(checkPwaDeferred);
                                    
                                    $.ajax({
                                        url: viewer.options.contextPath + '/web/property/json/'+viewer.options.appId+'/'+viewer.options.appVersion+"/pwaValidation",
                                        type: "POST",
                                        data : {
                                            className : node.data['className'],
                                            properties : JSON.stringify(jsonObj['properties'])
                                        },
                                        beforeSend: function (request) {
                                           if (ConnectionManager.tokenName !== undefined) { 
                                               request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                                           }
                                        },
                                        dataType : "json",
                                        success: function(response) {
                                            if ($.isArray(response)) {
                                                for (var r in response) {
                                                    var color = "red";
                                                    if (response[r].type === "READONLY") {
                                                        color = "yellow";
                                                    }
                                                    for (var i in response[r].messages) {
                                                        DependencyTree.Util.addIndicator(viewer, node, 'pwaoffline fas fa-wifi', response[r].messages[i], color, null, true);
                                                    }
                                                }
                                            }
                                            checkPwaDeferred.resolve();
                                        },
                                        error: function() {
                                            checkPwaDeferred.resolve();
                                        }
                                    });
                                }
                            }
                        }
                    }, url);
                }
                return false;
            }
        }
    }
};
DependencyTree.Matchers['customBuilder'] = {
    match: function(viewer, deferreds, node, jsonObj, refObj) {
        if (AdvancedTools.options.builder === "custom") {
            if (Object.keys(CustomBuilder.config.advanced_tools.tree_viewer.matchers).length + 1 !== Object.keys(DependencyTree.Matchers['customBuilder'].matchers).length) {
                DependencyTree.Matchers['customBuilder'].matchers = $.extend(true, DependencyTree.Matchers['customBuilder'].matchers, CustomBuilder.config.advanced_tools.tree_viewer.matchers);
            }
            
            return true;
        }
        return false;
    },
    matchers: {
        rootElement : {
            match: function (viewer, deferreds, node, jsonObj, refObj) {
                var open = false;
                var hasChildProperty = false;
                var childsproperties = CustomBuilder.config.advanced_tools.tree_viewer.childs_properties;
                for (var i in childsproperties) {
                    if (jsonObj[childsproperties[i]] !== undefined) {
                        hasChildProperty = true;
                        break;
                    }
                }
                var label = "";
                var properties;
                if (jsonObj['className'] !== undefined) {
                    if (CustomBuilder.paletteElements[jsonObj['className']] !== undefined) {
                        label = CustomBuilder.paletteElements[jsonObj['className']].label;
                        properties = CustomBuilder.paletteElements[jsonObj['className']].propertyOptions;
                    } else {
                        return false;
                    }
                } else if (hasChildProperty) {
                    properties = CustomBuilder.propertiesOptions;
                    node.data['value'] = label;
                    node.data['indicators'] = [];
                    var getProperties = $.Deferred();
                    deferreds.push(getProperties);
                    var pnode = $.extend(true, {}, DependencyTree.Node);
                    pnode.text = get_advtool_msg("dependency.tree.Properties");
                    pnode.data['properties'] = jsonObj['properties'];
                    pnode.a_attr['class'] += " properties";
                    pnode.state = {
                        opened: false
                    };
                    pnode.type = 'properties';
                    pnode.data['parent'] = node;
                    node.children.push(pnode);
                    DependencyTree.Util.pluginPropertiesWalker(viewer, pnode, node, jsonObj, getProperties, properties);
                } else {
                    return false;
                }
            
                for (var i in childsproperties) {
                    if (jsonObj[childsproperties[i]] !== undefined) {
                        for (var j in jsonObj[childsproperties[i]]) {
                            open = true;
                            var c = jsonObj[childsproperties[i]][j];
                            var cnode = $.extend(true, {}, DependencyTree.Node);
                            cnode.data['isCustomBuilder'] = true;
                            cnode.data['parent'] = node;
                            node.children.push(cnode);
                            DependencyTree.Util.runMatchers(viewer, deferreds, cnode, c);
                        }
                    }
                }
                node.state = {
                    opened: open
                };
                return false;
            }
        }
    }
};
DependencyTree.Matchers['formContainer'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (AdvancedTools.options.builder === "form" && jsonObj['elements'] !== undefined && jsonObj['elements'].length > 0) {
            node.data['isFormContainer'] = true;
            //clear indicators added previously
            node.data['indicators'] = [];
            
            var parentElement;
            if (node.data['parent'] === undefined) {
                parentElement = $("form.form-container");
                node.data["element"] = DependencyTree.elements.length;
                DependencyTree.elements.push($("form.form-container"));
            } else {
                parentElement = DependencyTree.elements[node.data["element"]];
            }
            
            var open = false;
            for (var j in jsonObj['elements']) {
                open = true;
                var c = jsonObj['elements'][j];
                var cnode = $.extend(true, {}, DependencyTree.Node);
                cnode.data['isFormElement'] = true;
                cnode.data["element"] = DependencyTree.elements.length;
                DependencyTree.elements.push($(parentElement).find("> [element-class]:eq("+j+")"));
                cnode.data['parent'] = node;
                node.children.push(cnode);
                DependencyTree.Util.runMatchers(viewer, deferreds, cnode, c);
            }
            
            node.state = {opened : open};
            
            return true;
        }
        return false;
    }
};
DependencyTree.Matchers['formElement'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (node.data['isFormContainer'] || node.data['isFormElement']) {
            return true;
        }
        return false;
    },
    matchers : {
        'editable' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['className'] === "org.joget.apps.form.model.Form") {
                    DependencyTree.Util.createEditIndicator(viewer, node, function(){
                        FormBuilder.showPopUpFormProperties();
                    });
                } else {
                    DependencyTree.Util.createEditIndicator(viewer, node, function() {
                        FormBuilder.editElementProperties(DependencyTree.elements[node.data["element"]]);
                    });
                }   
                
                return false;
            }
        },
        'hasCustomLoadBinder' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties']['loadBinder'] !== undefined && jsonObj['properties']['loadBinder']['className'] !== "" && jsonObj['properties']['loadBinder']['className'] !== "org.joget.apps.form.lib.WorkflowFormBinder") {
                    if ((typeof viewer.pluginList[jsonObj['properties']['loadBinder']['className']]) !== "undefined") {
                        DependencyTree.Util.addIndicator(viewer, node, 'fas fa-upload', get_advtool_msg("dependency.tree.Load.Binder") + ' (' + viewer.pluginList[jsonObj['properties']['loadBinder']['className']].label + ')');
                    } else {
                        DependencyTree.Util.addIndicator(viewer, node, 'missingplugin fas fa-upload', get_advtool_msg("dependency.tree.Load.Binder") + ' (' + jsonObj['properties']['loadBinder']['className'] + ')', "red");
                    }
                }
                return false;
            }
        },
        'hasCustomStoreBinder' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties']['storeBinder'] !== undefined && jsonObj['properties']['storeBinder']['className'] !== "" && jsonObj['properties']['storeBinder']['className'] !== "org.joget.apps.form.lib.WorkflowFormBinder") {
                    if ((typeof viewer.pluginList[jsonObj['properties']['storeBinder']['className']]) !== "undefined") {
                        DependencyTree.Util.addIndicator(viewer, node, 'fas fa-download', get_advtool_msg("dependency.tree.Store.Binder") + ' (' + viewer.pluginList[jsonObj['properties']['storeBinder']['className']].label + ')');
                    } else {
                        DependencyTree.Util.addIndicator(viewer, node, 'missingplugin fas fa-download', get_advtool_msg("dependency.tree.Store.Binder") + ' (' + jsonObj['properties']['storeBinder']['className'] + ')', "red");
                    }
                }
                return false;
            }
        },
        'hasOptionsBinder' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties']['optionsBinder'] !== undefined && jsonObj['properties']['optionsBinder']['className'] !== "") {
                    if ((typeof viewer.pluginList[jsonObj['properties']['optionsBinder']['className']]) !== "undefined") {
                        DependencyTree.Util.addIndicator(viewer, node, 'fas fa-upload', get_advtool_msg("dependency.tree.Options.Binder") + ' (' + viewer.pluginList[jsonObj['properties']['optionsBinder']['className']].label + ')');
                    } else {
                        DependencyTree.Util.addIndicator(viewer, node, 'missingplugin fas fa-upload', get_advtool_msg("dependency.tree.Options.Binder") + ' (' + jsonObj['properties']['optionsBinder']['className'] + ')', "red");
                    }
                }
                return false;
            }
        },
        'hasPostProcessor' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties']['postProcessor'] !== undefined && jsonObj['properties']['postProcessor']['className'] !== "" && jsonObj['properties']['postProcessor']['className'] !== "org.joget.apps.form.lib.WorkflowFormBinder") {
                    if ((typeof viewer.pluginList[jsonObj['properties']['postProcessor']['className']]) !== "undefined") {
                        DependencyTree.Util.addIndicator(viewer, node, 'fas fa-share', get_advtool_msg("dependency.tree.Post.Processor") + ' (' + viewer.pluginList[jsonObj['properties']['postProcessor']['className']].label + ')');
                    } else {
                        DependencyTree.Util.addIndicator(viewer, node, 'missingplugin fas fa-share', get_advtool_msg("dependency.tree.Post.Processor") + ' (' + jsonObj['properties']['postProcessor']['className'] + ')', "red");
                    }
                }
                return false;
            }
        },
        'hasSectionVisibilityControl' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (node.data['isFormContainer'] && jsonObj['properties']['visibilityControl'] !== undefined && jsonObj['properties']['visibilityControl'] !== "") {
                    DependencyTree.Util.addIndicator(viewer, node, 'fas fa-eye', get_advtool_msg("dependency.tree.Visibility.Control") + ' (' + jsonObj['properties']['visibilityControl'] + ' : ' + jsonObj['properties']['visibilityValue'] + ')');
                }
            }
        },
        'hasValidator' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties']['validator'] !== undefined && jsonObj['properties']['validator']['className'] !== "") {
                    if ((typeof viewer.pluginList[jsonObj['properties']['validator']['className']]) !== "undefined") {
                        DependencyTree.Util.addIndicator(viewer, node, 'fas fa-asterisk', get_advtool_msg("dependency.tree.Validator") + ' (' + viewer.pluginList[jsonObj['properties']['validator']['className']].label + ')');
                    } else {
                        DependencyTree.Util.addIndicator(viewer, node, 'missingplugin fas fa-asterisk', get_advtool_msg("dependency.tree.Validator") + ' (' + jsonObj['properties']['validator']['className'] + ')', "red");
                    }
                }
                return false;
            }
        }
    }
};
DependencyTree.Matchers['userview'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (jsonObj['className'] !== undefined && jsonObj['className'] === "org.joget.apps.userview.model.Userview") {
            node.data['value'] = get_advtool_msg("dependency.tree.Userview");
            node.data['indicators'] = [];
            
            //properties
            var getProperties = $.Deferred();
            deferreds.push(getProperties);

            var pnode = $.extend(true, {}, DependencyTree.Node);
            pnode.text = get_advtool_msg("dependency.tree.Properties");
            pnode.data['properties'] = jsonObj['properties'];
            pnode.a_attr['class'] += " properties";
            pnode.state = {opened : false};
            pnode.type = 'properties';
            pnode.data['parent'] = node;
            node.children.push(pnode);
            
            var properties = [{
                properties : [
                    {name: "id", label: get_advtool_msg("dependency.tree.Id")},
                    {name: "name", label: get_advtool_msg("dependency.tree.Name")},
                    {name: "description", label: get_advtool_msg("dependency.tree.Description")},
                    {name: "welcomeMessage", label: get_advtool_msg("dependency.tree.Welcome.Message")},
                    {name: "logoutText", label: get_advtool_msg("dependency.tree.Logout.Message")},
                    {name: "footerMessage", label: get_advtool_msg("dependency.tree.Footer.Message")}
                ]
            }];

            DependencyTree.Util.pluginPropertiesWalker(viewer, pnode, node, jsonObj, getProperties, properties);
            
            //setting
            var getSetting = $.Deferred();
            deferreds.push(getSetting);

            pnode.data['properties'] = jsonObj['setting']['properties'];
            var settingProperties = UserviewBuilder.settingPropertyOptions;

            DependencyTree.Util.pluginPropertiesWalker(viewer, pnode, node, jsonObj['setting'], getSetting, settingProperties);
            
            DependencyTree.Util.createEditIndicator(viewer, node, function(){
                UserviewBuilder.ShowPopupUserviewSetting();
            });
            
            //categories
            var open = false;
            for (var j in jsonObj['categories']) {
                open = true;
                var c = jsonObj['categories'][j];
                var cnode = $.extend(true, {}, DependencyTree.Node);
                cnode.data['isUserviewCategory'] = true;
                cnode.data['parent'] = node;
                node.children.push(cnode);
                DependencyTree.Util.runMatchers(viewer, deferreds, cnode, c);
            }
            node.state = {opened : open};
            
            return true;
        }
        return false;
    }
};
DependencyTree.Matchers['userviewCategory'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (jsonObj['className'] !== undefined && jsonObj['className'] === "org.joget.apps.userview.model.UserviewCategory") {
            node.data['value'] = get_advtool_msg("dependency.tree.Category");
            node.data['indicators'] = [];
            
            //properties
            var getProperties = $.Deferred();
            deferreds.push(getProperties);

            var pnode = $.extend(true, {}, DependencyTree.Node);
            pnode.text = get_advtool_msg("dependency.tree.Properties");
            pnode.data['properties'] = jsonObj['properties'];
            pnode.a_attr['class'] += " properties";
            pnode.state = {opened : false};
            pnode.type = 'properties';
            pnode.data['parent'] = node;
            node.children.push(pnode);
            DependencyTree.Util.createEditIndicator(viewer, node, function(){
                UserviewBuilder.setPermission(jsonObj['properties']['id']);
            });
            
            var properties = UserviewBuilder.categoryPropertyOptions;

            DependencyTree.Util.pluginPropertiesWalker(viewer, pnode, node, jsonObj, getProperties, properties);
            
            //Menu
            var editMenu = function(viewer, node, id) {
                DependencyTree.Util.createEditIndicator(viewer, node,  function(){
                    UserviewBuilder.editMenu(id);
                });
            };
            var open = false;
            for (var j in jsonObj['menus']) {
                open = true;
                var c = jsonObj['menus'][j];
                var cnode = $.extend(true, {}, DependencyTree.Node);
                cnode.data['isUserviewMenu'] = true;
                cnode.data['parent'] = node;
                node.children.push(cnode);
                DependencyTree.Util.runMatchers(viewer, deferreds, cnode, c);
                
                editMenu(viewer, cnode, c['properties']["id"]);
            }
            node.state = {opened : open};
            
            return true;
        }
        return false;
    },
    matchers : {
        'hidden' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties'] !== undefined && jsonObj['properties']['hide'] === "yes") {
                    DependencyTree.Util.addIndicator(viewer, node, 'fas fa-eye-slash', get_advtool_msg("dependency.tree.Hidden"));
                }
                return false;
            }
        }
    }
};
DependencyTree.Matchers['datalist'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (jsonObj['columns'] !== undefined && jsonObj['binder'] !== undefined) {
            node.data['value'] = get_advtool_msg("dependency.tree.Datalist");
            node.data['indicators'] = [];
            node.state = {opened : true};
            
            //properties
            var getProperties = $.Deferred();
            deferreds.push(getProperties);

            var pnode = $.extend(true, {}, DependencyTree.Node);
            pnode.text = get_advtool_msg("dependency.tree.Properties");
            pnode.data['properties'] = jsonObj['properties'];
            pnode.state = {opened : false};
            pnode.type = 'properties';
            pnode.a_attr['class'] += " properties";
            pnode.data['parent'] = node;
            node.children.push(pnode);
            DependencyTree.Util.createEditIndicator(viewer, node, function() {
                DatalistBuilder.showPopUpDatalistProperties();
            });
            
            var properties = DatalistBuilder.getDatalistPropertiesDefinition();

            DependencyTree.Util.pluginPropertiesWalker(viewer, pnode, node, {"properties" : jsonObj}, getProperties, properties);
            
            //Binder
            var bnode = $.extend(true, {}, DependencyTree.Node);
            bnode.text = get_advtool_msg("dependency.tree.Binder");
            bnode.data['parent'] = node;
            node.children.push(bnode);
            DependencyTree.Util.runMatchers(viewer, deferreds, bnode, jsonObj['binder']);
            
            DependencyTree.Util.createEditIndicator(viewer, bnode, function() {
                DatalistBuilder.showPopUpDatalistBinderProperties();
            });
            
            //Columns
            if (jsonObj['columns'] !== undefined && jsonObj['columns'].length > 0) {
                var pnode = $.extend(true, {}, DependencyTree.Node);
                pnode.text = get_advtool_msg("dependency.tree.Columns");
                pnode.state = {opened : true};
                pnode.type = 'childs';
                pnode.a_attr['class'] += " properties";
                pnode.data['parent'] = node;
                node.children.push(pnode);
                
                var showColumn = function(viewer, node, id) {
                    DependencyTree.Util.createEditIndicator(viewer, node, function() {
                        DatalistBuilder.showColumnProperties(id);
                    });
                };

                for (var j in jsonObj['columns']) {
                    var c = jsonObj['columns'][j];
                    var cnode = $.extend(true, {}, DependencyTree.Node);
                    cnode.data['isDatalistElement'] = true;
                    cnode.data['isDatalistColumn'] = true;
                    cnode.data['value'] = get_advtool_msg("dependency.tree.Column");
                    cnode.data['parent'] = pnode;
                    pnode.children.push(cnode);
                    
                    if (c["action"] !== undefined && c["action"]["className"] !== undefined && c["action"]["className"] !== "") {
                        if ((typeof viewer.pluginList[c["action"]["className"]]) !== "undefined") {
                            DependencyTree.Util.addIndicator(viewer, cnode, 'fas fa-link', get_advtool_msg("dependency.tree.Action") + ' (' + viewer.pluginList[c["action"]["className"]].label + ')');
                        } else {
                            DependencyTree.Util.addIndicator(viewer, cnode, 'missingplugin fas fa-link', get_advtool_msg("dependency.tree.Action") + ' (' + c["action"]["className"] + ')', "red");
                        }
                    }
                    
                    if (c["format"] !== undefined && c["format"]["className"] !== undefined && c["format"]["className"] !== "") {
                        if ((typeof viewer.pluginList[c["format"]["className"]]) !== "undefined") {
                            DependencyTree.Util.addIndicator(viewer, cnode, 'fas fa-text-height', get_advtool_msg("dependency.tree.Format") + ' (' + viewer.pluginList[c["format"]["className"]].label + ')');
                        } else {
                            DependencyTree.Util.addIndicator(viewer, cnode, 'missingplugin fas fa-text-height', get_advtool_msg("dependency.tree.Format") + ' (' + c["format"]["className"] + ')', "red");
                        }
                    }

                    //properties
                    var getProperties = $.Deferred();
                    deferreds.push(getProperties);

                    cnode.data['properties'] = c;
                    showColumn(viewer, cnode, c['id']);

                    var properties = DatalistBuilder.getColumnPropertiesDefinition();

                    DependencyTree.Util.pluginPropertiesWalker(viewer, cnode, cnode, {"properties":c}, getProperties, properties);
                }
            }
            
            //Filters
            if (jsonObj['filters'] !== undefined && jsonObj['filters'].length > 0) {
                var pnode = $.extend(true, {}, DependencyTree.Node);
                pnode.text = get_advtool_msg("dependency.tree.Filters");
                pnode.state = {opened : true};
                pnode.type = 'childs';
                pnode.a_attr['class'] += " properties";
                pnode.data['parent'] = node;
                node.children.push(pnode);
                
                var showFilter = function(viewer, node, id) {
                    DependencyTree.Util.createEditIndicator(viewer, node, function() {
                        DatalistBuilder.showFilterProperties(id);
                    });
                };

                for (var j in jsonObj['filters']) {
                    var c = jsonObj['filters'][j];
                    var cnode = $.extend(true, {}, DependencyTree.Node);
                    cnode.data['isDatalistElement'] = true;
                    cnode.data['isDatalistFilter'] = true;
                    cnode.data['value'] = get_advtool_msg("dependency.tree.Filter");
                    cnode.data['parent'] = pnode;
                    pnode.children.push(cnode);

                    //properties
                    var getProperties = $.Deferred();
                    deferreds.push(getProperties);

                    cnode.data['properties'] = c;
                    showFilter(viewer, cnode, c['id']);

                    var properties = DatalistBuilder.getFilterPropertiesDefinition();

                    DependencyTree.Util.pluginPropertiesWalker(viewer, cnode, cnode, {"properties":c}, getProperties, properties);
                }
            }
            
            //Row Actions
            if (jsonObj['rowActions'] !== undefined && jsonObj['rowActions'].length > 0) {
                var pnode = $.extend(true, {}, DependencyTree.Node);
                pnode.text = get_advtool_msg("dependency.tree.Row.Actions");
                pnode.state = {opened : true};
                pnode.type = 'childs';
                pnode.a_attr['class'] += " properties";
                pnode.data['parent'] = node;
                node.children.push(pnode);
                
                var showRowAction = function(viewer, node, id) {
                    DependencyTree.Util.createEditIndicator(viewer, node, function() {
                        DatalistBuilder.showRowActionProperties(id, DatalistBuilder.chosenRowActions);
                    });
                };

                for (var j in jsonObj['rowActions']) {
                    var c = jsonObj['rowActions'][j];
                    var cnode = $.extend(true, {}, DependencyTree.Node);
                    cnode.data['isDatalistElement'] = true;
                    cnode.data['isDatalistFilter'] = true;
                    cnode.data['parent'] = pnode;
                    pnode.children.push(cnode);
                    
                    var pluginClassName = c['className'];
                    if (viewer.pluginList[pluginClassName] !== undefined) {
                        cnode.data['value'] = viewer.pluginList[pluginClassName].label;
                    } else {
                        cnode.data['value'] = pluginClassName;
                        DependencyTree.Util.addIndicator(viewer, cnode, 'missingplugin fas fa-plug', get_advtool_msg("dependency.tree.Missing.Plugin"), "red", null, true);
                    }
                    
                    if (c["properties"] !== undefined && c["properties"]["rules"] !== undefined && c["properties"]["rules"].length > 0) {
                        DependencyTree.Util.addIndicator(viewer, cnode, 'fas fa-eye', get_advtool_msg("dependency.tree.Visibility.Control"));
                    }

                    //properties
                    var getProperties = $.Deferred();
                    deferreds.push(getProperties);

                    cnode.data['properties'] = c["properties"];
                    showRowAction(viewer, cnode, c['id']);

                    var properties = DatalistBuilder.getRowActionPropertiesDefinition(pluginClassName);

                    DependencyTree.Util.pluginPropertiesWalker(viewer, cnode, cnode, c, getProperties, properties);
                }
            }
            
            //Actions
            if (jsonObj['actions'] !== undefined && jsonObj['actions'].length > 0) {
                var pnode = $.extend(true, {}, DependencyTree.Node);
                pnode.text = get_advtool_msg("dependency.tree.Actions");
                pnode.state = {opened : true};
                pnode.type = 'childs';
                pnode.a_attr['class'] += " properties";
                pnode.data['parent'] = node;
                node.children.push(pnode);

                var showAction = function(viewer, node, id) {
                    DependencyTree.Util.createEditIndicator(viewer, node, function() {
                        DatalistBuilder.showActionProperties(id, DatalistBuilder.chosenActions);
                    });
                };
                
                for (var j in jsonObj['actions']) {
                    var c = jsonObj['actions'][j];
                    var cnode = $.extend(true, {}, DependencyTree.Node);
                    cnode.data['isDatalistElement'] = true;
                    cnode.data['isDatalistAction'] = true;
                    cnode.data['parent'] = pnode;
                    pnode.children.push(cnode);
                    DependencyTree.Util.runMatchers(viewer, deferreds, cnode, c);
                    
                    showAction(viewer, cnode, c["id"]);
                }
            }
            
            return true;
        }
        return false;
    }
};
DependencyTree.Matchers['hasPermission'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (jsonObj['properties'] !== undefined && jsonObj['properties']['permission'] !== undefined && jsonObj['properties']['permission']['className'] !== "") {
            if ((typeof viewer.pluginList[jsonObj['properties']['permission']['className']]) !== "undefined") {
                DependencyTree.Util.addIndicator(viewer, node, 'fas fa-lock', get_advtool_msg("dependency.tree.Permission") + ' (' + viewer.pluginList[jsonObj['properties']['permission']['className']].label + ')');
            } else {
                DependencyTree.Util.addIndicator(viewer, node, 'fas fa-lock', get_advtool_msg("dependency.tree.Permission") + ' (' + jsonObj['properties']['permission']['className'] + ')', "red");
            }
        }
        return false;
    }
};
DependencyTree.Matchers['userviewCache'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (jsonObj['properties'] !== undefined && jsonObj['properties']['userviewCacheScope'] !== undefined && jsonObj['properties']['userviewCacheScope'] !== "") {
            DependencyTree.Util.addIndicator(viewer, node, 'fas fa-bolt', get_advtool_msg("dependency.tree.UserviewCache"));
        }
        return false;
    }
};
DependencyTree.Matchers['grid'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (refObj !== undefined && refObj['type'] !== undefined && (refObj['type'].toLowerCase() === "grid" || refObj['type'].toLowerCase() === "gridfixedrow")) {
            var hasRow = false;
            for (var i in jsonObj) {
                hasRow = true;
                var rnode = $.extend(true, {}, DependencyTree.Node);
                rnode.text = get_advtool_msg("dependency.tree.Row") + ' ' + i ;
                rnode.state = {opened : false};
                rnode.type = 'row';
                rnode.a_attr['class'] += " properties";
                rnode.data['parent'] = node;
                node.children.push(rnode);
                
                for (var j in jsonObj[i]) {
                    var cnode = $.extend(true, {}, DependencyTree.Node);
                    cnode.data['parent'] = rnode;
                    rnode.children.push(cnode);
                    
                    var ref;
                    for (var k in refObj['columns']) {
                        if (refObj['columns'][k]['key'] === j) {
                            ref = refObj['columns'][k];
                        }
                    }
                    
                    DependencyTree.Util.runMatchers(viewer, deferreds, cnode, jsonObj[i][j], ref);
                }
            }
            
            if (refObj !== undefined && refObj['label'] !== undefined && refObj['label'] !== "") {
                if (hasRow) {
                    node.text = refObj['label'] + " : " + ' <i>'+get_advtool_msg("dependency.tree.Grid.Data")+'</i>';
                } else {
                    node.text = refObj['label'];
                    node.a_attr['class'] += " gray";
                }
            }
            
            return true;
        }
        return false;
    }
};

DependencyTree.Matchers['gribcombine'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (refObj !== undefined && refObj['type'] !== undefined && refObj['type'].toLowerCase() === "gridcombine") {
            var values = new Array();
            $.each(refObj['columns'], function(i, column) {
                
                var temp = node.data['parent'].data["properties"][column.key];
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
            
            //render value
            if (values.length > 0) {
                $.each(values, function(i, row) {
                    var rnode = $.extend(true, {}, DependencyTree.Node);
                    rnode.text = get_advtool_msg("dependency.tree.Row") + ' ' + i;
                    rnode.state = {opened : false};
                    rnode.type = 'row';
                    rnode.a_attr['class'] += " properties";
                    rnode.data['parent'] = node;
                    node.children.push(rnode);
                
                    $.each(refObj['columns'], function(i, column) {
                        var cnode = $.extend(true, {}, DependencyTree.Node);
                        cnode.data['parent'] = rnode;
                        rnode.children.push(cnode);
                        
                        var columnValue = "";
                        if (row[column.key] !== undefined) {
                            columnValue = row[column.key];
                        }
                        DependencyTree.Util.runMatchers(viewer, deferreds, cnode, columnValue, column);
                        
                    });
                });
            }
        }
        return false;
    }
};
DependencyTree.Matchers['string'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (node.data['value'] === undefined && $.type(jsonObj) === "string") {
            if (refObj !== undefined && refObj['label'] !== undefined && refObj['label'] !== "") {
                node.text = refObj['label'] + " : ";
            }
            node.data['value'] = jsonObj;
            return true;
        }
        return false;
    },
    matchers : {
        'longText' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (node.data['value'].length > 50) {
                    node.data['value'] = node.data['value'].substring(0, 50) + "...";
                }
                return false;
            }
        },
        'removeHtml' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                node.data['value'] = UI.escapeHTML(node.data['value']);
                return false;
            }
        },
        'secret' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (refObj !== undefined && refObj['type'] !== undefined && refObj['type'].toLowerCase() === "password") {
                    node.data['value'] = "********";
                }
                return false;
            }
        },
        'gribcombine' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (refObj !== undefined && refObj['type'] !== undefined && refObj['type'].toLowerCase() === "gridcombine") {
                    if (refObj !== undefined && refObj['label'] !== undefined && refObj['label'] !== "") {
                        if (node.children.length > 0) {
                            node.text = refObj['label'] + ":" + ' <i>'+get_advtool_msg("dependency.tree.Grid.Data")+'</i>';
                        } else {
                            node.text = refObj['label'] + ":";
                            node.a_attr['class'] += " gray";
                        }
                    }
                }
            }
        },
        'isForm' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj !== "" && refObj !== undefined && (
                        (refObj['name'] !== undefined && refObj['name'].toLowerCase().indexOf("formid") !== -1 && refObj['name'].toLowerCase().indexOf("parentsubformid") === -1 ) ||
                        (refObj['options_ajax'] !== undefined && refObj['options_ajax'].toLowerCase().indexOf("/forms/options") !== -1 ))) {
                    
                    if (viewer.formList[jsonObj] !== undefined) {
                        node.data['value'] = viewer.formList[jsonObj];
                        DependencyTree.Util.addIndicator(viewer, node, 'fas fa-file-alt', get_advtool_msg("dependency.tree.Form") + ' (' + viewer.formList[jsonObj] + ')', "", viewer.options.contextPath + '/web/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/form/builder/' + jsonObj);
                    } else {
                        DependencyTree.Util.addIndicator(viewer, node, 'missingelement fas fa-file-alt', get_advtool_msg("dependency.tree.Missing.Form"), "red", null, true);
                    }
                }
                return false;
            }
        },
        'isList' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj !== "" && refObj !== undefined && (
                        (refObj['name'] !== undefined && refObj['name'].toLowerCase().indexOf("listid") !== -1) ||
                        (refObj['options_ajax'] !== undefined && refObj['options_ajax'].toLowerCase().indexOf("/datalist/options") !== -1 ))) {
                    
                    if (viewer.datalistList[jsonObj] !== undefined) {
                        node.data['value'] = viewer.datalistList[jsonObj];
                        DependencyTree.Util.addIndicator(viewer, node, 'fas fa-table', get_advtool_msg("dependency.tree.Datalist") + ' (' + viewer.datalistList[jsonObj] + ')', "", viewer.options.contextPath + '/web/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/datalist/builder/' + jsonObj);
                    } else {
                        DependencyTree.Util.addIndicator(viewer, node, 'missingelement fas fa-table', get_advtool_msg("dependency.tree.Missing.Datalist"), "red", null, true);
                    }
                }
                return false;
            }
        },
        'isUserview' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj !== "" && refObj !== undefined && (
                        (refObj['name'] !== undefined && refObj['name'].toLowerCase().indexOf("userviewid") !== -1) ||
                        (refObj['options_ajax'] !== undefined && refObj['options_ajax'].toLowerCase().indexOf("/userview/options") !== -1 ))) {
                    
                    if (viewer.userviewList[jsonObj] !== undefined) {
                        node.data['value'] = viewer.userviewList[jsonObj];
                        DependencyTree.Util.addIndicator(viewer, node, 'fas fa-desktop', get_advtool_msg("dependency.tree.Userview") + ' (' + viewer.userviewList[jsonObj] + ')', "", viewer.options.contextPath + '/web/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/userview/builder/' + jsonObj);
                    } else {
                        DependencyTree.Util.addIndicator(viewer, node, 'missingelement fas fa-desktop', get_advtool_msg("dependency.tree.Missing.Userview"), "red", null, true);
                    }
                }
                return false;
            }
        },
        'isCustomBuilder' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (viewer.builderType !== null && viewer.builderType !== undefined) {
                    for (var key in viewer.builderType) {
                        if (viewer.builderType.hasOwnProperty(key)) {
                            var builder = viewer.builderType[key];
                            if (jsonObj !== "" && refObj !== undefined && (
                                    (refObj['name'] !== undefined && refObj['name'].toLowerCase().indexOf(builder.value+"id") !== -1) ||
                                    (refObj['options_ajax'] !== undefined && refObj['options_ajax'].toLowerCase().indexOf("/cbuilder/"+builder.value+"/options") !== -1 ))) {

                                if (viewer.builderList[jsonObj] !== undefined) {
                                    node.data['value'] = viewer.builderList[jsonObj];
                                    DependencyTree.Util.addIndicator(viewer, node, builder.icon, builder.label + ' (' + viewer.builderList[jsonObj] + ')', "", viewer.options.contextPath + '/web/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/cbuilder/'+builder.value+'/design/' + jsonObj);
                                } else {
                                    DependencyTree.Util.addIndicator(viewer, node, 'missingelement ' + builder.icon, builder.label, "red", null, true);
                                }
                            }
                        }
                    }
                }
                return false;
            }
        },
        'options' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (refObj !== undefined 
                        && ((refObj['options_ajax'] !== undefined && refObj['options_ajax'] !== "") 
                        || (refObj['options'] !== undefined && $.type(refObj['options']) === "array"))
                            && node.data['indicators'].length === 0 
                            && (refObj['url'] === undefined || refObj['url'].indexOf("getPropertyOptions") === -1)) {
                        
                    if (refObj['options'] !== undefined && $.type(refObj['options']) === "array") {  
                        for (var i in refObj['options']) {
                            if (refObj['options'][i].value === jsonObj) {
                                if (refObj['options'].length === 1 && refObj['options'][i].label === "" && refObj['type'].toLowerCase() === "checkbox") {
                                    node.data['value'] = get_advtool_msg("dependency.tree.True");
                                } else {
                                    node.data['value'] = refObj['options'][i].label;
                                }
                            }
                        }
                    } else {
                        var data = {};
                        
                        if (refObj['options_ajax_on_change'] !== undefined && refObj['options_ajax_on_change'] !== "") {
                            var onChanges = refObj['options_ajax_on_change'].split(";");
                            
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
                                
                                var parent = node.data['parent'];
                                var properties = parent.data["properties"];
                                
                                while (properties === undefined && parent.data['parent'] !== undefined) {
                                    parent = parent.data['parent'];
                                    properties = parent.data["properties"];
                                }
                                
                                var value = "";
                                if (properties !== undefined && properties[fieldId] !== undefined) {
                                    value = properties[fieldId];
                                }
                                
                                if (childField !== "" && value !== "") {
                                    if ($.isArray(value)) { //is grid
                                        var values = [];
                                        for (var j in value) {
                                            values.push(value[j][childField]);
                                        }
                                        value = values.join(";");
                                    } else if (value[childField] !== undefined) {
                                        if (value[childField] === null) {
                                            value = "";
                                        } else if ($.type(value[childField]) === "string") {
                                            value = value[childField];
                                        } else {
                                            value = JSON.encode(value[childField]);
                                        }
                                    }
                                }
                                data[param] = value;
                            }
                        }
                        
                        var ajaxUrl = DependencyTree.Util.processUrl(viewer, refObj['options_ajax']);
                        var method = "GET";
                        if (refObj['options_ajax_method'] !== undefined) {
                            method = refObj['options_ajax_method'].toUpperCase();
                        }
                            
                        var o = $.Deferred();
                        deferreds.push(o);
                        $.ajax({
                            url: ajaxUrl,
                            dataType : "text",
                            method : method,
                            data : data,
                            success: function(response) {
                                node.data['value'] = DependencyTree.Util.getValue(response, refObj, jsonObj);
                                o.resolve();
                            },
                            error: function() {
                                o.resolve();
                            }
                        });
                    }
                }
                return false;
            }
        },
        'empty' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (node.data['value'] === undefined || node.data['value'] === "") {
                    node.a_attr['class'] += " gray";
                }
                return false;
            }
        }
    }
};
DependencyTree.Viewer = function(element, dataSelector, options) {
    this.element = element;
    this.dataSelector = dataSelector;
    this.options = options;
    this.formList;
    this.datalistList;
    this.userviewList;
    this.builderList;
    this.processList; //ignore now
    this.pluginList;
    this.builderType;
    this.container;
    this.pluginProperties = {};
    this.callbacks = [];
    this.isInit = false;
    this.warning = {};
};
DependencyTree.Viewer.prototype = {
    init : function() {
        var viewer = this;
        var deferreds = [];
        if (!this.isInit) {
            var pl = $.Deferred();
            deferreds.push(pl);
            $.ajax({
                url: viewer.options.contextPath + '/web/property/json/getElements?classname=org.joget.plugin.property.model.PropertyEditable&includeHidden=true&pwaValidation=true',
                dataType : "json",
                success: function(response) {
                    viewer.pluginList = {};
                    for (var i in response) {
                        if (response[i].value !== "") {
                            viewer.pluginList[response[i].value] = response[i];
                        }
                    }
                    pl.resolve();
                },
                error: function() {
                    pl.resolve();
                }
            });
            
            var fl = $.Deferred();
            deferreds.push(fl);
            $.ajax({
                url: viewer.options.contextPath + '/web/json/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/forms/options',
                dataType : "json",
                success: function(response) {
                    viewer.formList = {};
                    for (var i in response) {
                        if (response[i].value !== "") {
                            viewer.formList[response[i].value] = response[i].label;
                        }
                    }
                    fl.resolve();
                },
                error: function() {
                    fl.resolve();
                }
            });
            
            var dl = $.Deferred();
            deferreds.push(dl);
            $.ajax({
                url: viewer.options.contextPath + '/web/json/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/datalist/options',
                dataType : "json",
                success: function(response) {
                    viewer.datalistList = {};
                    for (var i in response) {
                        if (response[i].value !== "") {
                            viewer.datalistList[response[i].value] = response[i].label;
                        }
                    }
                    dl.resolve();
                },
                error: function() {
                    dl.resolve();
                }
            });
            
            var ul = $.Deferred();
            deferreds.push(ul);
            $.ajax({
                url: viewer.options.contextPath + '/web/json/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/userview/options',
                dataType : "json",
                success: function(response) {
                    viewer.userviewList = {};
                    for (var i in response) {
                        if (response[i].value !== "") {
                            viewer.userviewList[response[i].value] = response[i].label;
                        }
                    }
                    ul.resolve();
                },
                error: function() {
                    ul.resolve();
                }
            });
            
            var cb = $.Deferred();
            deferreds.push(cb);
            $.ajax({
                url: viewer.options.contextPath + '/web/json/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/cbuilders',
                dataType : "json",
                success: function(response) {
                    viewer.builderType = {};
                    for (var i in response) {
                        if (response[i].value !== "") {
                            viewer.builderType[response[i].value] = response[i];
                        }
                    }
                    cb.resolve();
                },
                error: function() {
                    cb.resolve();
                }
            });
            
            var cbl = $.Deferred();
            deferreds.push(cbl);
            $.ajax({
                url: viewer.options.contextPath + '/web/json/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/cbuilderAllOptions',
                dataType : "json",
                success: function(response) {
                    viewer.builderList = {};
                    for (var i in response) {
                        if (response[i].value !== "") {
                            viewer.builderList[response[i].value] = response[i].label;
                        }
                    }
                    cbl.resolve();
                },
                error: function() {
                    cbl.resolve();
                }
            });
            
        } else {
            var dummy = $.Deferred();
            deferreds.push(dummy);
            dummy.resolve();
        }
        
        if (viewer.container === undefined) {
            viewer.container = $('<div id="dependencyTreeContainer"><div class="dt-buttons sticky-buttons"><a id="dt-collapse" class="button">'+get_advtool_msg("dependency.tree.Collapse.All")+'</a> <a id="dt-expand" class="button">'+get_advtool_msg("dependency.tree.Expand.All")+'</a><a id="dt-save" class="button">'+get_advtool_msg("dependency.tree.Generate.Image")+'</a></div><div id="dependencyTreeViewer"></div></div>');
            viewer.element.append(viewer.container);
    
            $(".dt-buttons #dt-collapse").click(function(){
                $('#dependencyTreeViewer').jstree('close_all');
                return false;
            });
            
            $(".dt-buttons #dt-expand").click(function(){
                $('#dependencyTreeViewer').jstree('open_all');
                return false;
            });
            
            $(".dt-buttons #dt-save").click(function(){
                $(this).prepend('<i class="fas fa-spinner fa-spin"></i>');
                
                // create invisible div for canvas
                var div = $('<div id="dependencyTreeExport">');
                var iwidth = $('#dependencyTreeViewer').width() + 100;
                var iheight = $('#dependencyTreeViewer').height() + 100; 
                
                var part = -1;
                var size = 8000;
                if (iheight > 8000) {
                    var tempH = iheight;
                    var count = 2;
                    do {
                        tempH = iheight/count;
                        tempH = Math.ceil(tempH/100)*100;
                        count++;
                    } while (tempH > 8000)
                    iheight = tempH;
                    size = tempH;
                        
                    part = 1;
                }
                
                $(div).css("padding", "50px").css("background", "#fff").css("overflow", "hidden").width(iwidth).height(iheight);
                $(div).append($('#dependencyTreeContainer').html());
                $(div).find(".dt-buttons").remove();
                $("body").append(div);
                
                viewer.generateImage(div, 0, size, $('#dependencyTreeViewer').height() + 100, part);
                return false;
            });
        }
        
        $(viewer.dataSelector).on("change", function() {
            var activeTab = $('.builder_tool_tabs li.ui-tabs-active a').attr("id");
            if (activeTab !== "treeViewer") {
                $("#dependencyTreeViewer").html("");
            }
        });
            
        $.when.apply($, deferreds).then(function(){
            viewer.isInit = true;
        });
    },
    hide : function() {
        $("#dependencyTreeExport, #downloadTreeForm, #downloadTreeFrame").remove();
    },
    generateImage : function (div, offset, size, height, part) {
        var viewer = this;
        if (offset < height) {
            var postfix = "";
            if (part !== -1) {
                postfix = "_" + part;
                part++;
            }
            
            $(div).find(" > div ").css("margin-top", "-" + offset + "px");
            
            if (offset + size > height) {
                $(div).height(height - offset);
            }
        
            html2canvas(div, {
                logging: true,
                onrendered: function (canvas) {

                    if ($("#downloadTreeFrame").length === 0) {
                        $("body").append('<iframe id="downloadTreeFrame" name="downloadTreeFrame" style="display:none">');
                    }

                    var imgageData = canvas.toDataURL("image/png");
                    var form = $('<form id="downloadTreeForm" target="downloadTreeFrame" action="'+viewer.options.contextPath +'/web/dependency/tree/image/'+viewer.options.id+'" method="POST" >');
                    $("body").append(form);
                    $(form).append('<input type="hidden" name="data">');
                    $(form).find("[name=data]").val(imgageData);
                    $(form).append('<input type="hidden" name="postfix" value="'+postfix+'" />');
                    $(form).append('<input type="hidden" name="'+ConnectionManager.tokenName+'" value="'+ConnectionManager.tokenValue+'" />');
                    $(form).submit();
                    $(form).remove();
                    
                    viewer.generateImage(div, offset+size, size, height, part);
                }
            });
        } else {
            $(div).remove();
            $(".dt-buttons #dt-save i").remove();
        }
    },
    redraw: function() {
        var viewer = this;
        
        if ($(viewer.element).find(".dt-loading").length === 0) {
            $(viewer.element).prepend('<i class="dt-loading fas fa-5x fa-spinner fa-spin"></i>');
        }
        viewer.callbacks = [];
        
        var data = $(viewer.dataSelector).val();
        var jsonObj = JSON.parse(data);
        
        var deferreds = [];
        var tree = $.extend(true, {}, DependencyTree.Node);
        DependencyTree.Util.runMatchers(this, deferreds, tree, jsonObj);
        $.when.apply($, deferreds).then(function(){
            DependencyTree.Util.cleanData(tree);
            $('#dependencyTreeViewer').jstree(true).settings.core.data = tree;
            $('#dependencyTreeViewer').jstree(true).refresh();
            
            $(viewer.element).find(".dt-loading").remove();
        });
    },
    render: function() {
        var viewer = this;
        $(".treeviewer-alert.warning.alert").remove();
        viewer['warning'] = {};
        
        if ($(viewer.element).find("#dependencyTreeViewer ul").length === 1) {
            return;
        }
        
        if ($(viewer.element).find(".dt-loading").length === 0) {
            $(viewer.element).prepend('<i class="dt-loading fas fa-5x fa-spinner fa-spin"></i>');
        }
        
        if (!viewer.isInit) {
            //wait until it is initialized
            setTimeout(function(){
                viewer.render();
            }, 200);
            return;
        }
        var jsonObj;
        
        if (AdvancedTools.options.builder === "custom") {
            jsonObj = CustomBuilder.data;
        } else {
            var data = $(viewer.dataSelector).val();
            jsonObj = JSON.parse(data);
        }   
        
        var deferreds = [];
        DependencyTree.elements = []; //clear previous data
        var tree = $.extend(true, {}, DependencyTree.Node);
        DependencyTree.Util.runMatchers(this, deferreds, tree, jsonObj);
        
        $.when.apply($, deferreds).then(function(){
            DependencyTree.Util.cleanData(tree);
            $("#dependencyTreeViewer").jstree('destroy');
            $("#dependencyTreeViewer").html("");
            
            $('#dependencyTreeViewer').on("refresh.jstree ready.jstree", function(){
                $(".treeviewer-alert.warning.alert").remove();
                
                if (viewer['warning']['pwaoffline'] !== undefined && viewer['warning']['pwaoffline'].length > 0) {
                    $(viewer.element).prepend('<div class="treeviewer-alert warning alert">' + get_advtool_msg("pwa.warning") + ' <span class="indicators">' + viewer.buildIndicator(viewer['warning']['pwaoffline']) + '</span></div>');
                }
                if (viewer['warning']['missingplugin'] !== undefined && viewer['warning']['missingplugin'].length > 0) {
                    $(viewer.element).prepend('<div class="treeviewer-alert warning alert">' + get_advtool_msg("dependency.tree.warning.MissingPlugin") + ' <span class="indicators">' + viewer.buildIndicator(viewer['warning']['missingplugin']) + '</span></div>');
                }
                if (viewer['warning']['missingelement'] !== undefined && viewer['warning']['missingelement'].length > 0) {
                    $(viewer.element).prepend('<div class="treeviewer-alert warning alert">' + get_advtool_msg("dependency.tree.warning.MissingElement") + ' <span class="indicators">' + viewer.buildIndicator(viewer['warning']['missingelement']) + '</span></div>');
                }
            }).jstree({
                "types" : {
                    "default" : {
                        "icon" : "fas fa-cube"
                    },
                    "childs" : {
                        "icon" : "fas fa-cubes"
                    },
                    "row" : {
                        "icon" : "fas fa-bars"
                    },
                    "properties" : {
                        "icon" : "fas fa-newspaper"
                    }
                },
                "plugins": ["types", "joget"],
                'core' : {
                    'data' : tree
                }
            });
            
            $("#dependencyTreeContainer").on("click", ".indicator", function(){
                var i = $(this).data("callback");
                if (viewer.callbacks[i] !== undefined && $.type(viewer.callbacks[i]) === "function") {
                    viewer.callbacks[i]();
                }
            });
            
            $(viewer.element).find(".dt-loading").remove();
        });
    },
    getPluginProperties : function (name, callback, url) {
        var viewer = this;
        
        if (name === undefined) {
            name = "";
        }
        
        var key = name;
        
        if (url === undefined) {
            url = viewer.options.contextPath + '/web/property/json/'+viewer.options.appId+'/'+viewer.options.appVersion+'/getPropertyOptions';
        } else {
            url = DependencyTree.Util.processUrl(viewer, url);
            key = url + "?value=" + name;
        }
        
        var deferreds = [];
        if (viewer.pluginProperties[key] === undefined) {
            var p = $.Deferred();
            deferreds.push(p);
            $.ajax({
                url: url,
                data : "value="+escape(name),
                dataType : "text",
                success: function(response) {
                    viewer.pluginProperties[key] = eval(response);
                    p.resolve();
                },
                error: function() {
                    p.resolve();
                }
            });
        } else {
            var dummy = $.Deferred();
            deferreds.push(dummy);
            dummy.resolve();
        }
        
        $.when.apply($, deferreds).then(function(){
            callback(viewer.pluginProperties[key]);
        });
    },
    buildIndicator : function(inds) {
        var exist = [];
        var indicators = "";
        
        for (var i in inds) {
            var ind = inds[i];
            if (exist.indexOf(ind.color + ':' + ind.icon) === -1) {
                var color = "";
                if (ind.color !== undefined && ind.color !== "") {
                    color = "indicator-" + ind.color;
                }
                var slash = "";
                if (ind.slash) {
                    slash = '<i class="fas fa-slash fa-stack-1x fa-inverse"></i>';
                }

                var html = '<a class="indicator '+color+'" >';
                html += '<span class="fa-stack"><i class="fas fa-circle fa-stack-2x"></i>' + slash + '<i class="' + ind.icon + ' fa-stack-1x fa-inverse"></i></span>';
                html += '</a>';
                indicators += html;
                exist.push(ind.color + ':' + ind.icon);
            }
        }
        return indicators;
    }
};