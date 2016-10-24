DependencyTree = {};
DependencyTree.Matchers = {};

DependencyTree.Util = {
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
            labelNode.value += ' (' + jsonObj['properties']['label'] + ')';
        } else if (jsonObj['properties']['name'] !== undefined && jsonObj['properties']['name'] !== "") {
            labelNode.value += ' (' + jsonObj['properties']['name'] + ')';
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
                        var cnode = new DependencyTree.Node();
                        node.addChild(cnode);
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
        $(".boxy-wrapper").css("z-index", "9999999");
    },
    createEditIndicator: function (viewer, node, callback) {
        var ind = new DependencyTree.Indicator();
        ind.icon = 'fa fa-pencil';
        ind.color = "green";
        ind.label = get_advtool_msg("dependency.tree.Edit.Properties");
        ind.callback = function() {
            DependencyTree.Util.hideAndCall(viewer, callback);
        };
        
        node.addIndicator(ind);
    }
};

DependencyTree.Matchers['pluginOrElementSelect'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (jsonObj['className'] !== undefined) {
            if (refObj !== undefined && refObj['label'] !== undefined && refObj['label'] !== "") {
                node.label = refObj['label'] + ' :';
            }
            
            if (refObj === undefined || (refObj !== undefined && refObj['url'] !== undefined && refObj['url'].indexOf("getPropertyOptions") !== -1)) {
                node.data['isPlugin'] = true;
                if (jsonObj['className'] !== "") {
                    var pluginClassName = jsonObj['className'];

                    node.data['className'] = pluginClassName;
                    if (viewer.pluginList[pluginClassName] !== undefined) {
                        node.value = viewer.pluginList[pluginClassName];
                        return true;
                    } else {
                        node.value = pluginClassName;

                        var plugin = new DependencyTree.Indicator();
                        plugin.icon = 'fa fa-plug';
                        plugin.color = "red";
                        plugin.label = get_advtool_msg("dependency.tree.Missing.Plugin");
                        node.addIndicator(plugin);
                    }
                } else {
                    node.label = "<span class=\"gray\">" + node.label + "</span>";
                }
            } else if (refObj !== undefined && refObj['url'] !== undefined && refObj['url'].indexOf("getPropertyOptions") === -1) {
                node.data['isElementSelect'] = true;
                var value = jsonObj['className'];
                
                //process label by reusing option matchers
                DependencyTree.Matchers['string']['matchers']['options'].match(viewer, deferreds, node, value, refObj);
                
                if (node.value === undefined || node.value === "") {
                    node.label = "<span class=\"gray\">" + node.label + "</span>";
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
                    if (node.data['className'] === "org.joget.apps.form.model.Form" 
                            || node.data['className'] === "org.joget.apps.form.model.Section" 
                            || node.data['className'] === "org.joget.apps.form.model.Column") {
                        pnode = new DependencyTree.Node();
                        pnode.label = '<i>' + get_advtool_msg("dependency.tree.Properties") + '</i>';
                        pnode.type = 'properties';
                        node.addChild(pnode);
                    }
                    
                    pnode.data['properties'] = jsonObj['properties'];
                    
                    var url;
                    if (refObj !== undefined && refObj['url'] !== undefined && refObj['url'].indexOf("getPropertyOptions") === -1) {
                        url = refObj['url'];
                    }
            
                    viewer.getPluginProperties(jsonObj['className'], function(properties) {
                        DependencyTree.Util.pluginPropertiesWalker(viewer, pnode, node, jsonObj, getProperties, properties);
                    }, url);
                }
                return false;
            }
        }
    }
};
DependencyTree.Matchers['formContainer'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (jsonObj['elements'] !== undefined && jsonObj['elements'].length > 0) {
            node.data['isFormContainer'] = true;
            //clear indicators added previously
            node.indicators = [];
            
            var parentElement;
            if (node.parent === undefined) {
                parentElement = $("form.form-container");
                node.data["element"] = $("form.form-container");
            } else {
                parentElement = node.data["element"];
            }
            
            var open = false;
            for (var j in jsonObj['elements']) {
                open = true;
                var c = jsonObj['elements'][j];
                var cnode = new DependencyTree.Node();
                cnode.data['isFormElement'] = true;
                cnode.data["element"] = $(parentElement).find("> [element-class]:eq("+j+")");
                node.addChild(cnode);
                DependencyTree.Util.runMatchers(viewer, deferreds, cnode, c);
            }
            
            node.opened = open;
            
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
                        FormBuilder.editElementProperties(node.data["element"]);
                    });
                }   
                
                return false;
            }
        },
        'hasCustomLoadBinder' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties']['loadBinder'] !== undefined && jsonObj['properties']['loadBinder']['className'] !== "" && jsonObj['properties']['loadBinder']['className'] !== "org.joget.apps.form.lib.WorkflowFormBinder") {
                    var ind = new DependencyTree.Indicator();
                    ind.icon = 'fa fa-upload';
                    ind.label = get_advtool_msg("dependency.tree.Load.Binder") + ' (' + viewer.pluginList[jsonObj['properties']['loadBinder']['className']] + ')';
                    node.addIndicator(ind);
                }
                return false;
            }
        },
        'hasCustomStoreBinder' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties']['storeBinder'] !== undefined && jsonObj['properties']['storeBinder']['className'] !== "" && jsonObj['properties']['storeBinder']['className'] !== "org.joget.apps.form.lib.WorkflowFormBinder") {
                    var ind = new DependencyTree.Indicator();
                    ind.icon = 'fa fa-download';
                    ind.label = get_advtool_msg("dependency.tree.Store.Binder") + ' (' + viewer.pluginList[jsonObj['properties']['storeBinder']['className']] + ')';
                    node.addIndicator(ind);
                }
                return false;
            }
        },
        'hasOptionsBinder' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties']['optionsBinder'] !== undefined && jsonObj['properties']['optionsBinder']['className'] !== "") {
                    var ind = new DependencyTree.Indicator();
                    ind.icon = 'fa fa-upload';
                    ind.label = get_advtool_msg("dependency.tree.Options.Binder") + ' (' + viewer.pluginList[jsonObj['properties']['optionsBinder']['className']] + ')';
                    node.addIndicator(ind);
                }
                return false;
            }
        },
        'hasPostProcessor' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties']['postProcessor'] !== undefined && jsonObj['properties']['postProcessor']['className'] !== "" && jsonObj['properties']['postProcessor']['className'] !== "org.joget.apps.form.lib.WorkflowFormBinder") {
                    var ind = new DependencyTree.Indicator();
                    ind.icon = 'fa fa-share';
                    ind.label = get_advtool_msg("dependency.tree.Post.Processor") + ' (' + viewer.pluginList[jsonObj['properties']['postProcessor']['className']] + ')';
                    node.addIndicator(ind);
                }
                return false;
            }
        },
        'hasSectionVisibilityControl' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (node.data['isFormContainer'] && jsonObj['properties']['visibilityControl'] !== undefined && jsonObj['properties']['visibilityControl'] !== "") {
                    var ind = new DependencyTree.Indicator();
                    ind.icon = 'fa fa-eye';
                    ind.label = get_advtool_msg("dependency.tree.Visibility.Control") + ' (' + jsonObj['properties']['visibilityControl'] + ' : ' + jsonObj['properties']['visibilityValue'] + ')';
                    node.addIndicator(ind);
                }
            }
        },
        'hasValidator' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties']['validator'] !== undefined && jsonObj['properties']['validator']['className'] !== "") {
                    var ind = new DependencyTree.Indicator();
                    ind.icon = 'fa fa-asterisk';
                    ind.label = get_advtool_msg("dependency.tree.Validator") + ' (' + viewer.pluginList[jsonObj['properties']['validator']['className']] + ')';
                    node.addIndicator(ind);
                }
                return false;
            }
        }
    }
};
DependencyTree.Matchers['userview'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (jsonObj['className'] !== undefined && jsonObj['className'] === "org.joget.apps.userview.model.Userview") {
            node.value = get_advtool_msg("dependency.tree.Userview");
            node.indicators = [];
            
            //properties
            var getProperties = $.Deferred();
            deferreds.push(getProperties);

            var pnode = new DependencyTree.Node();
            pnode.label = '<i>' + get_advtool_msg("dependency.tree.Properties") + '</i>';
            pnode.data['properties'] = jsonObj['properties'];
            pnode.opened = false;
            pnode.type = 'properties';
            node.addChild(pnode);
            
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
                var cnode = new DependencyTree.Node();
                cnode.data['isUserviewCategory'] = true;
                node.addChild(cnode);
                DependencyTree.Util.runMatchers(viewer, deferreds, cnode, c);
            }
            node.opened = open;
            
            return true;
        }
        return false;
    }
};
DependencyTree.Matchers['userviewCategory'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (jsonObj['className'] !== undefined && jsonObj['className'] === "org.joget.apps.userview.model.UserviewCategory") {
            node.value = get_advtool_msg("dependency.tree.Category");
            node.indicators = [];
            
            //properties
            var getProperties = $.Deferred();
            deferreds.push(getProperties);

            var pnode = new DependencyTree.Node();
            pnode.label = '<i>' + get_advtool_msg("dependency.tree.Properties") + '</i>';
            pnode.data['properties'] = jsonObj['properties'];
            pnode.opened = false;
            pnode.type = 'properties';
            node.addChild(pnode);
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
                var cnode = new DependencyTree.Node();
                cnode.data['isUserviewMenu'] = true;
                node.addChild(cnode);
                DependencyTree.Util.runMatchers(viewer, deferreds, cnode, c);
                
                editMenu(viewer, cnode, c['properties']["id"]);
            }
            node.opened = open;
            
            return true;
        }
        return false;
    },
    matchers : {
        'hidden' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj['properties'] !== undefined && jsonObj['properties']['hide'] === "yes") {
                    var ind = new DependencyTree.Indicator();
                    ind.icon = 'fa fa-eye-slash';
                    ind.label = get_advtool_msg("dependency.tree.Hidden");
                    node.addIndicator(ind);
                }
                return false;
            }
        }
    }
};
DependencyTree.Matchers['datalist'] = {
    match : function (viewer, deferreds, node, jsonObj, refObj) {
        if (jsonObj['columns'] !== undefined && jsonObj['binder'] !== undefined) {
            node.value = get_advtool_msg("dependency.tree.Datalist");
            node.indicators = [];
            node.opened = true;
            
            //properties
            var getProperties = $.Deferred();
            deferreds.push(getProperties);

            var pnode = new DependencyTree.Node();
            pnode.label = '<i>' + get_advtool_msg("dependency.tree.Properties") + '</i>';
            pnode.data['properties'] = jsonObj['properties'];
            pnode.opened = false;
            pnode.type = 'properties';
            node.addChild(pnode);
            DependencyTree.Util.createEditIndicator(viewer, node, function() {
                DatalistBuilder.showPopUpDatalistProperties();
            });
            
            var properties = DatalistBuilder.getDatalistPropertiesDefinition();

            DependencyTree.Util.pluginPropertiesWalker(viewer, pnode, node, {"properties" : jsonObj}, getProperties, properties);
            
            //Binder
            var bnode = new DependencyTree.Node();
            bnode.label = get_advtool_msg("dependency.tree.Binder");
            node.addChild(bnode);
            DependencyTree.Util.runMatchers(viewer, deferreds, bnode, jsonObj['binder']);
            
            DependencyTree.Util.createEditIndicator(viewer, bnode, function() {
                DatalistBuilder.showPopUpDatalistBinderProperties();
            });
            
            //Columns
            if (jsonObj['columns'] !== undefined && jsonObj['columns'].length > 0) {
                var pnode = new DependencyTree.Node();
                pnode.label = '<i>' + get_advtool_msg("dependency.tree.Columns") + '</i>';
                pnode.opened = true;
                pnode.type = 'childs';
                node.addChild(pnode);
                
                var showColumn = function(viewer, node, id) {
                    DependencyTree.Util.createEditIndicator(viewer, node, function() {
                        DatalistBuilder.showColumnProperties(id);
                    });
                };

                for (var j in jsonObj['columns']) {
                    var c = jsonObj['columns'][j];
                    var cnode = new DependencyTree.Node();
                    cnode.data['isDatalistElement'] = true;
                    cnode.data['isDatalistColumn'] = true;
                    cnode.value = get_advtool_msg("dependency.tree.Column");
                    pnode.addChild(cnode);
                    
                    if (c["action"] !== undefined && c["action"]["className"] !== undefined && c["action"]["className"] !== "") {
                        var ind = new DependencyTree.Indicator();
                        ind.icon = 'fa fa-link';
                        ind.label = get_advtool_msg("dependency.tree.Action") + ' (' + viewer.pluginList[c["action"]["className"]] + ')';
                        cnode.addIndicator(ind);
                    }
                    
                    if (c["format"] !== undefined && c["format"]["className"] !== undefined && c["format"]["className"] !== "") {
                        var ind = new DependencyTree.Indicator();
                        ind.icon = 'fa fa-text-height';
                        ind.label = get_advtool_msg("dependency.tree.Format") + ' (' + viewer.pluginList[c["format"]["className"]] + ')';
                        cnode.addIndicator(ind);
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
                var pnode = new DependencyTree.Node();
                pnode.label = '<i>' + get_advtool_msg("dependency.tree.Filters") + '</i>';
                pnode.opened = true;
                pnode.type = 'childs';
                node.addChild(pnode);
                
                var showFilter = function(viewer, node, id) {
                    DependencyTree.Util.createEditIndicator(viewer, node, function() {
                        DatalistBuilder.showFilterProperties(id);
                    });
                };

                for (var j in jsonObj['filters']) {
                    var c = jsonObj['filters'][j];
                    var cnode = new DependencyTree.Node();
                    cnode.data['isDatalistElement'] = true;
                    cnode.data['isDatalistFilter'] = true;
                    cnode.value = get_advtool_msg("dependency.tree.Filter");
                    pnode.addChild(cnode);

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
                var pnode = new DependencyTree.Node();
                pnode.label = '<i>' + get_advtool_msg("dependency.tree.Row.Actions") + '</i>';
                pnode.opened = true;
                pnode.type = 'childs';
                node.addChild(pnode);
                
                var showRowAction = function(viewer, node, id) {
                    DependencyTree.Util.createEditIndicator(viewer, node, function() {
                        DatalistBuilder.showRowActionProperties(id, DatalistBuilder.chosenRowActions);
                    });
                };

                for (var j in jsonObj['rowActions']) {
                    var c = jsonObj['rowActions'][j];
                    var cnode = new DependencyTree.Node();
                    cnode.data['isDatalistElement'] = true;
                    cnode.data['isDatalistFilter'] = true;
                    pnode.addChild(cnode);
                    
                    var pluginClassName = c['className'];
                    if (viewer.pluginList[pluginClassName] !== undefined) {
                        cnode.value = viewer.pluginList[pluginClassName];
                    } else {
                        cnode.value = pluginClassName;

                        var plugin = new DependencyTree.Indicator();
                        plugin.icon = 'fa fa-plug';
                        plugin.color = "red";
                        plugin.label = get_advtool_msg("dependency.tree.Missing.Plugin");
                        cnode.addIndicator(plugin);
                    }
                    
                    if (c["properties"] !== undefined && c["properties"]["rules"] !== undefined && c["properties"]["rules"].length > 0) {
                        var ind = new DependencyTree.Indicator();
                        ind.icon = 'fa fa-eye';
                        ind.label = get_advtool_msg("dependency.tree.Visibility.Control");
                        cnode.addIndicator(ind);
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
                var pnode = new DependencyTree.Node();
                pnode.label = '<i>' + get_advtool_msg("dependency.tree.Actions") + '</i>';
                pnode.opened = true;
                pnode.type = 'childs';
                node.addChild(pnode);

                var showAction = function(viewer, node, id) {
                    DependencyTree.Util.createEditIndicator(viewer, node, function() {
                        DatalistBuilder.showActionProperties(id, DatalistBuilder.chosenActions);
                    });
                };
                
                for (var j in jsonObj['actions']) {
                    var c = jsonObj['actions'][j];
                    var cnode = new DependencyTree.Node();
                    cnode.data['isDatalistElement'] = true;
                    cnode.data['isDatalistAction'] = true;
                    pnode.addChild(cnode);
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
            var ind = new DependencyTree.Indicator();
            ind.icon = 'fa fa-lock';
            ind.label = get_advtool_msg("dependency.tree.Permission") + ' (' + viewer.pluginList[jsonObj['properties']['permission']['className']] + ')';
            node.addIndicator(ind);
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
                var rnode = new DependencyTree.Node();
                rnode.label = '<i>' + get_advtool_msg("dependency.tree.Row") + ' ' + i + '</i>';
                rnode.opened = false;
                rnode.type = 'row';
                node.addChild(rnode);
                
                for (var j in jsonObj[i]) {
                    var cnode = new DependencyTree.Node();
                    rnode.addChild(cnode);
                    
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
                    node.label = refObj['label'] + ":" + ' <i>'+get_advtool_msg("dependency.tree.Grid.Data")+'</i>';
                } else {
                    node.label = "<span class=\"gray\">" + refObj['label'] + ":" + "</span>";
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
                
                var temp = node.parent.data["properties"][column.key];
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
                    var rnode = new DependencyTree.Node();
                    rnode.label = '<i>' + get_advtool_msg("dependency.tree.Row") + ' ' + i + '</i>';
                    rnode.opened = false;
                    rnode.type = 'row';
                    node.addChild(rnode);
                
                    $.each(refObj['columns'], function(i, column) {
                        var cnode = new DependencyTree.Node();
                        rnode.addChild(cnode);
                        
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
        if (node.value === undefined && $.type(jsonObj) === "string") {
            if (refObj !== undefined && refObj['label'] !== undefined && refObj['label'] !== "") {
                node.label = refObj['label'] + ":";
            }
            node.value = jsonObj;
            return true;
        }
        return false;
    },
    matchers : {
        'longText' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (node.value.length > 50) {
                    node.value = node.value.substring(0, 50) + "...";
                }
                return false;
            }
        },
        'removeHtml' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                node.value = UI.escapeHTML(node.value);
                return false;
            }
        },
        'secret' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (refObj !== undefined && refObj['type'] !== undefined && refObj['type'].toLowerCase() === "password") {
                    node.value = "********";
                }
                return false;
            }
        },
        'gribcombine' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (refObj !== undefined && refObj['type'] !== undefined && refObj['type'].toLowerCase() === "gridcombine") {
                    if (refObj !== undefined && refObj['label'] !== undefined && refObj['label'] !== "") {
                        if (node.childs.length > 0) {
                            node.label = refObj['label'] + ":" + ' <i>'+get_advtool_msg("dependency.tree.Grid.Data")+'</i>';
                        } else {
                            node.label = "<span class=\"gray\">" + refObj['label'] + ":" + "</span>";
                        }
                    }
                }
            }
        },
        'isForm' : {
            match : function (viewer, deferreds, node, jsonObj, refObj) {
                if (jsonObj !== "" && refObj !== undefined && (
                        (refObj['name'] !== undefined && refObj['name'].toLowerCase().indexOf("formid") !== -1) ||
                        (refObj['options_ajax'] !== undefined && refObj['options_ajax'].toLowerCase().indexOf("/forms/options") !== -1 ))) {
                    
                    if (viewer.formList[jsonObj] !== undefined) {
                        node.value = viewer.formList[jsonObj];
                                
                        var ind = new DependencyTree.Indicator();
                        ind.icon = 'fa fa-file-text-o';
                        ind.label = get_advtool_msg("dependency.tree.Form") + ' (' + viewer.formList[jsonObj] + ')';
                        ind.link = viewer.options.contextPath + '/web/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/form/builder/' + jsonObj;
                        node.addIndicator(ind);
                    } else {
                        var ind = new DependencyTree.Indicator();
                        ind.icon = 'fa fa-file-text-o';
                        ind.color = "red";
                        ind.label = get_advtool_msg("dependency.tree.Missing.Form");
                        node.addIndicator(ind);
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
                        node.value = viewer.datalistList[jsonObj];
                                
                        var ind = new DependencyTree.Indicator();
                        ind.icon = 'fa fa-table';
                        ind.label = get_advtool_msg("dependency.tree.Datalist") + ' (' + viewer.datalistList[jsonObj] + ')';
                        ind.link = viewer.options.contextPath + '/web/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/datalist/builder/' + jsonObj;
                        node.addIndicator(ind);
                    } else {
                        var ind = new DependencyTree.Indicator();
                        ind.icon = 'fa fa-table';
                        ind.color = "red";
                        ind.label = get_advtool_msg("dependency.tree.Missing.Datalist");
                        node.addIndicator(ind);
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
                        node.value = viewer.userviewList[jsonObj];
                                
                        var ind = new DependencyTree.Indicator();
                        ind.icon = 'fa fa-tv';
                        ind.label = get_advtool_msg("dependency.tree.Userview") + ' (' + viewer.userviewList[jsonObj] + ')';
                        ind.link = viewer.options.contextPath + '/web/console/app/'+viewer.options.appId+'/'+viewer.options.appVersion+'/userview/builder/' + jsonObj;
                        node.addIndicator(ind);
                    } else {
                        var ind = new DependencyTree.Indicator();
                        ind.icon = 'fa fa-tv';
                        ind.color = "red";
                        ind.label = get_advtool_msg("dependency.tree.Missing.Userview");
                        node.addIndicator(ind);
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
                            && node.indicators.length === 0 
                            && (refObj['url'] === undefined || refObj['url'].indexOf("getPropertyOptions") === -1)) {
                        
                    if (refObj['options'] !== undefined && $.type(refObj['options']) === "array") {  
                        for (var i in refObj['options']) {
                            if (refObj['options'][i].value === jsonObj) {
                                if (refObj['options'].length === 1 && refObj['options'][i].label === "" && refObj['type'].toLowerCase() === "checkbox") {
                                    node.value = get_advtool_msg("dependency.tree.True");
                                } else {
                                    node.value = refObj['options'][i].label;
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
                                
                                var parent = node.parent;
                                var properties = parent.data["properties"];
                                
                                while (properties === undefined && parent.parent !== undefined) {
                                    parent = parent.parent;
                                    properties = parent.data["properties"];
                                }
                                
                                var value = "";
                                if (properties !== undefined && properties[fieldId] !== undefined) {
                                    value = properties[fieldId];
                                }
                                
                                if (childField !== "" && value !== "") { //is grid
                                    var values = [];
                                    for (var j in value) {
                                        values.push(value[j][childField]);
                                    }
                                    value = values.join(";");
                                }
                                data[param] = value;
                            }
                        }
                        
                        var o = $.Deferred();
                        deferreds.push(o);
                        $.ajax({
                            url: DependencyTree.Util.processUrl(viewer, refObj['options_ajax']),
                            dataType : "text",
                            data : data,
                            success: function(response) {
                                var json = eval(response);
                                for (var i in json) {
                                    if (json[i].value === jsonObj) {
                                        node.value = json[i].label;
                                    }
                                }
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
                if (node.value === undefined || node.value === "") {
                    node.label = "<span class=\"gray\">" + node.label + "</span>";
                }
                return false;
            }
        },
    }
};

DependencyTree.Node = function() {
    this.label;
    this.value;
    this.opened = false;
    this.type = 'default';
    this.indicators = [];
    this.parent;
    this.childs = [];
    this.data = {};
};
DependencyTree.Node.prototype = {
    render : function(viewer, container) {
        var node = this;
        var li = $('<li><label></label></li>');
        if (node.label  !== undefined && node.label !== "") {
            $(li).find("label").append("<span>"+node.label+" </span>");
        }
        if (node.value  !== undefined && node.value !== "") {
            $(li).find("label").append("<span><strong>"+node.value+"</strong></span>");
        }
        
        $(li).data("jstree", {
           opened : node.opened,
           type : node.type,
           li_attr : {
               'data-type' : node.type
           }
        });
        $(container).append(li);
        
        if (node.indicators.length > 0) {
            var indicators = $('<span class="indicators">');
            $(li).find("label").after(indicators);
            
            for (var i in node.indicators) {
                node.indicators[i].render(viewer, indicators);
            }
        }
        
        if (node.childs.length > 0) {
            var childs = $('<ul>');
            $(li).append(childs);
            
            for (var i in node.childs) {
                node.childs[i].render(viewer, childs);
            }
        }
    },
    addChild : function (node) {
        node.parent = this;
        this.childs.push(node);
    },
    addIndicator : function (indicator) {
        indicator.node = this;
        this.indicators.push(indicator);
    }
};

DependencyTree.Indicator = function() {
    this.node;
    this.icon;
    this.color;
    this.label;
    this.link;
    this.callback;
};
DependencyTree.Indicator.prototype = {
    render : function(viewer, container) {
        var a = $('<a class="indicator">');
        
        if (this.link !== undefined && this.link !== "") {
            $(a).attr("href", this.link);
            $(a).attr("target", "_blank");
        }
        
        if (this.callback !== undefined && $.type(this.callback) === "function") {
            var callbacks = viewer.callbacks;
            callbacks.push(this.callback);
            $(a).attr("data-callback", callbacks.length - 1);
        }
        
        if (this.color !== undefined && this.color !== "") {
            $(a).addClass("indicator-"+this.color);
        }
        
        $(a).append('<span class="fa-stack"><i class="fa fa-circle fa-stack-2x"></i><i class="'+this.icon+' fa-stack-1x fa-inverse"></i></span>');
        if (this.label !== undefined && this.label !== "") {
            $(a).attr("title", this.label);
            $(a).attr('aria-label', this.label);
        }
        
        $(container).append(a);
    }
};

DependencyTree.Viewer = function(element, dataSelector, options) {
    this.element = element;
    this.dataSelector = dataSelector;
    this.options = options;
    this.formList;
    this.datalistList;
    this.userviewList;
    this.processList; //ignore now
    this.pluginList;
    this.container;
    this.pluginProperties = {};
    this.callbacks = [];
    this.isInit = false;
};
DependencyTree.Viewer.prototype = {
    init : function() {
        var viewer = this;
        var deferreds = [];
        if (!this.isInit) {
            var pl = $.Deferred();
            deferreds.push(pl);
            $.ajax({
                url: viewer.options.contextPath + '/web/property/json/getElements?classname=org.joget.plugin.property.model.PropertyEditable',
                dataType : "json",
                success: function(response) {
                    viewer.pluginList = {};
                    for (var i in response) {
                        if (response[i].value !== "") {
                            viewer.pluginList[response[i].value] = response[i].label;
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
                $(this).prepend('<i class="fa fa-spinner fa-spin"></i>');
                
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
            $("#dependencyTreeViewer").html("");
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
    render: function() {
        var viewer = this;
        
        if ($(viewer.element).find("#dependencyTreeViewer ul").length === 1) {
            return;
        }
        
        if ($(viewer.element).find(".dt-loading").length === 0) {
            $(viewer.element).prepend('<i class="dt-loading fa fa-5x fa-spinner fa-spin"></i>');
        }
        
        if (!viewer.isInit) {
            //wait until it is initialized
            setTimeout(function(){
                viewer.render();
            }, 200);
        }
        
        var data = $(viewer.dataSelector).val();
        var jsonObj = JSON.parse(data);
        
        var deferreds = [];
        var tree = new DependencyTree.Node();
        DependencyTree.Util.runMatchers(this, deferreds, tree, jsonObj);
        
        $.when.apply($, deferreds).then(function(){
            $("#dependencyTreeViewer").jstree('destroy');
            $("#dependencyTreeViewer").html("");
            $("#dependencyTreeViewer").append('<ul>');
            tree.render(viewer, $("#dependencyTreeViewer ul"));
            
            $('#dependencyTreeViewer').jstree({
                "types" : {
                    "default" : {
                        "icon" : "fa fa-cube"
                    },
                    "childs" : {
                        "icon" : "fa fa-cubes"
                    },
                    "row" : {
                        "icon" : "fa fa-bars"
                    },
                    "properties" : {
                        "icon" : "fa fa-newspaper-o"
                    }
                },
                "plugins": ["types"]
            });
            
            //move indicator outside of a
            $("#dependencyTreeViewer .indicators").each(function(){
                var parent = $(this).closest("a");
                $(parent).after(this);
            });
            
            $('#dependencyTreeViewer').on('after_open.jstree', function (e, data) {
                $(".jstree-anchor > .indicators").each(function(){
                    var parent = $(this).closest("a");
                    $(parent).after(this);
                });
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
    }
};