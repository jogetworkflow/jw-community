DatalistBuilder = {
    UPDATE : 'Update',
    
    columnPrefix : "column_",
    rowActionPrefix : "rowAction_",
    actionPrefix : "action_",
    filterPrefix : "filter_",
    columnIndexCounter : 0,
    rowActionIndexCounter : 0,
    filterIndexCounter : 0,
    actionIndexCounter : 0,
    
    availableActions : {},
    availableFilters : {},
    availableFormatters : {},
    availableColumns : null,

    /*
     * Intialize the builder, called from CustomBuilder.initBuilder
     */
    initBuilder: function (callback) {
        
        $("#design-btn").before('<button class="btn btn-light" id="binder-btn" type="button" data-toggle="button" aria-pressed="true" data-cbuilder-view="dataBinder" data-cbuilder-action="switchView" title="'+get_cbuilder_msg('dbuilder.data')+'"><i class="las la-database"></i> <span>'+get_cbuilder_msg('dbuilder.data')+'</span> </button>');
        
        CustomBuilder.Builder.init({
            callbacks : {
                "addElement" : "DatalistBuilder.addElement",
                "renderElement" : "DatalistBuilder.renderElement",
                "updateElementId" : "DatalistBuilder.updateElementId",
                "unloadElement" : "DatalistBuilder.unloadElement",
                "selectElement" : "DatalistBuilder.selectElement",
                "pasteElement" : "DatalistBuilder.pasteElement",
                "renderXray" : "DatalistBuilder.renderXray",
                "parseDataToComponent" : "DatalistBuilder.parseDataToComponent",
                "renderTreeMenuAdditionalNode" : "DatalistBuilder.renderTreeMenuAdditionalNode"
            }
        }, function() {
            CustomBuilder.Builder.setHead('<link data-footable-style href="' + CustomBuilder.contextPath + '/js/footable/footable.core.min.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<link data-datalist-style href="' + CustomBuilder.contextPath + '/css/datalist8.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<link data-userview-style href="' + CustomBuilder.contextPath + '/css/userview8.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<link data-dbuilder-style href="' + CustomBuilder.contextPath + '/css/dbuilder.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<script data-footable-script src="' + CustomBuilder.contextPath + '/js/footable/footable.js"/>');
            CustomBuilder.Builder.setHead('<script data-responsive-script src="' + CustomBuilder.contextPath + '/js/footable/responsiveTable.js"/>');
            
            var deferreds = [];
        
            CustomBuilder.createPaletteCategory(get_cbuilder_msg('dbuilder.columnsFilters'));
            DatalistBuilder.initDatalistComponent();
            DatalistBuilder.initActionList(deferreds);
            DatalistBuilder.initFilterList(deferreds);
            DatalistBuilder.initFormatterList(deferreds);
            
            $(CustomBuilder.Builder.iframe).on("change.builder", function(){
                DatalistBuilder.refreshTableLayout();
            });
            
            $.when.apply($, deferreds).then(function() {
                if (callback) {
                    callback();
                }
            });
        });
    },
    
    /*
     * A callback method from CustomBuilder.switchView to render data binder properties editor
     */
    dataBinderViewInit: function(view) {
        $(view).html("");
        
        var options = {
            appPath: CustomBuilder.appPath,
            contextPath: CustomBuilder.contextPath,
            propertiesDefinition : DatalistBuilder.getBinderPropertiesDefinition(),
            propertyValues : CustomBuilder.data,
            showCancelButton:false,
            closeAfterSaved : false,
            changeCheckIgnoreUndefined: true,
            autoSave: true,
            saveCallback: function(container, properties) {
                var binderChanged = CustomBuilder.data.binder.className !== properties.binder.className;
                CustomBuilder.data = $.extend(CustomBuilder.data, properties);
                
                if (binderChanged) {
                    DatalistBuilder.updateBinderProperties(DatalistBuilder.UPDATE);
                } else {
                    DatalistBuilder.updateBinderProperties();
                }
            }
        };
        $("body").addClass("stop-scrolling");
        
        $(view).off("builder-view-show");
        $(view).on("builder-view-show", function(){
            $(view).propertyEditor(options);
        });
    },
    
    /*
     * Get binder properties definition for data view
     */
    getBinderPropertiesDefinition : function() {
        return [
            {title: get_cbuilder_msg('dbuilder.selectBinder'),
                helplink: get_cbuilder_msg('dbuilder.selectBinder.helplink'),
                properties : [{
                    name : 'binder',
                    label : get_cbuilder_msg('dbuilder.selectDataSource'),
                    type : 'elementselect',
                    options_ajax : '[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.datalist.model.DataListBinder',
                    url : '[CONTEXT_PATH]/web/property/json' + CustomBuilder.appPath + '/getPropertyOptions'
                }]
            }
        ];
    },
    
    /*
     * update binder properties from data view
     */
    updateBinderProperties : function(mode){
        var deferreds = [];
        var wait = $.Deferred();
        deferreds.push(wait);
        
        DatalistBuilder.retrieveColumns(deferreds);
        
        if(mode === DatalistBuilder.UPDATE){
            //reset all fields
            CustomBuilder.data.filters = new Array();
            CustomBuilder.data.columns = new Array();
            CustomBuilder.data.actions = new Array();
            CustomBuilder.data.rowActions = new Array();
            DatalistBuilder.columnIndexCounter = 0;
            DatalistBuilder.rowActionIndexCounter = 0;
            DatalistBuilder.filterIndexCounter = 0;
            DatalistBuilder.actionIndexCounter = 0;
            
            CustomBuilder.data['orderBy'] = "";
            CustomBuilder.data['order'] = "";
        }
        
        wait.resolve();
        
        $.when.apply($, deferreds).then(function() {
            if (DatalistBuilder.availableColumns !== null) {
                CustomBuilder.update();
            }
            DatalistBuilder.load(CustomBuilder.data);
        });
    },
    
    /*
     * To retrieve the permission rule property
     */
    getRuleObject : function() {
        return CustomBuilder.data;
    },
    
    /*
     * Load and render data, called from CustomBuilder.loadJson
     */
    load: function (data) {
        if (data.binder === undefined || data.binder.className === undefined || data.binder.className === "") {
            setTimeout(function(){
                $("#binder-btn").trigger("click");
            }, 1);
            return;
        }
        var deferreds = [];
        
        DatalistBuilder.retrieveColumns(deferreds);   
        
        var self = CustomBuilder.Builder;
            
        var selectedELSelector = "";
        var selectedElIndex = 0;
        if (self.subSelectedEl) {
            selectedELSelector = '[data-cbuilder-select="'+ $(self.subSelectedEl).data("cbuilder-select") +'"]';
            selectedElIndex = self.frameBody.find(selectedELSelector).index(self.subSelectedEl);
        } else if (self.selectedEl) {
            if ($(self.selectedEl).is("[data-cbuilder-id]")) {
                selectedELSelector = '[data-cbuilder-id="'+ $(self.selectedEl).data("cbuilder-id") +'"]';
            } else {
                selectedELSelector = '[data-cbuilder-id="'+ $(self.selectedEl).closest('[data-cbuilder-id]').data("cbuilder-id") +'"]';
                selectedELSelector += ' [data-cbuilder-classname="' + $(self.selectedEl).data("cbuilder-classname") + '"]';
            }
            selectedElIndex = self.frameBody.find(selectedELSelector).index(self.selectedEl);
        } 
        
        
        $.when.apply($, deferreds).then(function() {
            self.frameBody.html("");
            self.selectNode(false);
            $("#element-highlight-box").hide();

            var html = '<div class="dataList" style="display:block !important;" data-cbuilder-uneditable data-cbuilder-classname="org.joget.apps.datalist.model.DataList" >\
                            <form class="filter_form"><div class="filters" data-cbuilder-columns_filters data-cbuilder-filters data-cbuilder-sort-horizontal></div></form>\
                            <div class="footable-buttons" style="display:none">\
                                <button class="expandAll footable-button"><i></i> Expand All</button>\
                                <button class="collapseAll footable-button"><i></i> Collapse All</button>\
                                <span class="search_trigger">Search <i></i></span>\
                            </div>\
                            <form>\
                                <table class="xrounded_shadowed expandfirst">\
                                    <thead>\
                                        <tr data-cbuilder-columns_filters data-cbuilder-columns data-cbuilder-prepend  data-cbuilder-sort-horizontal>\
                                            <th class="gap">&nbsp;</th><th class="row_action_container row_action" data-cbuilder-all_actions data-cbuilder-rowActions  data-cbuilder-sort-horizontal></th>\
                                        </tr>\
                                    </thead>\
                                    <tbody>\
                                        <tr class="odd"><td class="gap">&nbsp;</td><td class="row_action_container row_action"></td></tr>\
                                        <tr class="even"><td class="gap">&nbsp;</td><td class="row_action_container row_action"></td></tr>\
                                        <tr class="odd"><td class="gap">&nbsp;</td><td class="row_action_container row_action"></td></tr>\
                                        <tr class="even"><td class="gap">&nbsp;</td><td class="row_action_container row_action"></td></tr>\
                                        <tr class="odd"><td class="gap">&nbsp;</td><td class="row_action_container row_action"></td></tr>\
                                    </tbody>\
                                </table>\
                                <div class="actions bottom left" data-cbuilder-all_actions data-cbuilder-actions  data-cbuilder-sort-horizontal></div>\
                            </form>\
                        </div>';

            self.frameBody.append(html);
            self.frameBody.find(".dataList").data("data", CustomBuilder.data);
            self.frameBody.find(".dataList").attr("data-cbuilder-id", CustomBuilder.data.id);

            $("#iframe-wrapper").show();
            
            var cdeferreds = [];
            var d = $.Deferred();
            cdeferreds.push(d);
            
            for (var i in CustomBuilder.data.filters) {
                var data = CustomBuilder.data.filters[i];
                
                var component = self.parseDataToComponent(data);
                var temp = $('<span></span>');
                self.frameBody.find(".filters").append(temp);
                self.renderElement(data, temp, component, null, cdeferreds);
            }
            
            for (var i in CustomBuilder.data.columns) {
                var data = CustomBuilder.data.columns[i];
                
                var component = self.parseDataToComponent(data);
                var temp = $('<th></th>');
                self.frameBody.find("table thead tr .gap").before(temp);
                self.renderElement(data, temp, component, null, cdeferreds);
            }
            
            for (var i in CustomBuilder.data.actions) {
                var data = CustomBuilder.data.actions[i];
                
                var component = self.parseDataToComponent(data);
                var temp = $('<span></span>');
                self.frameBody.find("div.actions").append(temp);
                self.renderElement(data, temp, component, null, cdeferreds);
            }
            
            for (var i in CustomBuilder.data.rowActions) {
                var data = CustomBuilder.data.rowActions[i];
                
                var component = self.parseDataToComponent(data);
                var temp = $('<div></div>');
                self.frameBody.find("th.row_action_container").append(temp);
                self.renderElement(data, temp, component, null, cdeferreds);
            }
            
            d.resolve();
            
            $.when.apply($, cdeferreds).then(function() {
                DatalistBuilder.refreshTableLayout();

                var dlComponent = self.parseDataToComponent(CustomBuilder.data);
                DatalistBuilder.updateDatalistStyle(self.frameBody.find(".dataList"), CustomBuilder.data, dlComponent);

                if (dlComponent.builderTemplate.isPastable(CustomBuilder.data, dlComponent)) {
                    $("#paste-element-btn").removeClass("disabled");
                }

                if (self.nodeAdditionalType !== undefined && self.nodeAdditionalType !== "") {
                    CustomBuilder.Builder.renderNodeAdditional(self.nodeAdditionalType);
                }

                //reselect previous selected element
                if (selectedELSelector !== "") {
                    var element = self.frameBody.find(selectedELSelector);
                    if (element.length > 1) {
                        do {
                            element = element[selectedElIndex];
                        } while (element === undefined && selectedElIndex-- > 0);
                    }
                    
                    if ($(element).length > 0) {
                        self.selectNode(element);
                    }
                }
            });
        });
    },
    
    /*
     * A callback method from CustomBuilder.Builder.parseDataToComponent
     */
    parseDataToComponent : function(data) {
        var self = CustomBuilder.Builder;
        
        if (data.className !== undefined) {
            return self.getComponent(data.className);
        } else if (data.columns !== undefined && data.binder !== undefined) {
            return self.getComponent("org.joget.apps.datalist.model.DataList");
        } else {
            return self.getComponent(data.name);
        }
    },
    
    /*
     * Retrive columns based on data binder
     */
    retrieveColumns : function(deferreds) {
        DatalistBuilder.availableColumns = null;
        
        var wait = $.Deferred();
        deferreds.push(wait);
        
        if (CustomBuilder.data.binder !== undefined && CustomBuilder.data.binder.className !== undefined && CustomBuilder.data.binder.className !== "") {
            var temp = {};
            temp['binderJson'] = JSON.encode(CustomBuilder.data.binder.properties);
            temp['id'] = CustomBuilder.data.id;
            temp['binderId'] = CustomBuilder.data.binder.className;
            temp['retrieveSample'] = true;
        
            $.post(
                CustomBuilder.contextPath + '/web/json/console/app' + CustomBuilder.appPath + '/builder/binder/columns',
                temp,
                function(data) {
                    DatalistBuilder.retrieveColumnsCallback(data, wait);
                },
                "json"
            ).error(
                function () {
                    if (CustomBuilder.data.binder.className !== undefined && CustomBuilder.data.binder.className !== "") {
                        alert(get_cbuilder_msg('dbuilder.errorRetrieveColumns'));
                    }
                    wait.resolve();
                }
            );
        } else {
            wait.resolve();
        }
    },
    
    /*
     * Initialize builder component for column and filter
     */
    retrieveColumnsCallback : function(data, deferrer) {
        CustomBuilder.clearPaletteCategory(get_cbuilder_msg('dbuilder.columnsFilters'));
        
        DatalistBuilder.sampleData = data.sample;
        
        var columns = data.columns;
        var fields = new Object();
        
        for(var e in columns){
            var column = columns[e];
            var temp = {
                "id"         : column.name.toString(),
                "label"      : column.label.toString(),
                "displayLabel" : column.displayLabel.toString(),
                "name"       : column.name.toString(),
                "filterable" : column.filterable
            }
            fields[temp.id] = temp;
            
            var cssClass = "";
            if (column.name === column.label) {
                cssClass = " key";
            }
            var meta = {
                filterable : column.filterable,
                builderTemplate : {
                    'getParentContainerAttr' : function(elementObj, component) {
                        if (elementObj === undefined || elementObj === null) {
                            if (component.filterable) {
                                return "columns_filters";
                            } else {
                                return "columns";
                            }
                        } else if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) !== -1) {
                            return "filters";
                        } else {
                            return "columns";
                        }
                    },
                    'getParentDataHolder' : function(elementObj, component) {
                        if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) !== -1) {
                            return "filters";
                        } else {
                            return "columns";
                        }
                    },
                    'getLabel' : function(elementObj, component) {
                        if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) !== -1) {
                            var className = "org.joget.apps.datalist.lib.TextFieldDataListFilterType";
                            if (elementObj.type !== undefined && elementObj.type.className !== undefined && elementObj.type.className !== "") {
                                className = elementObj.type.className;
                            }
                            return DatalistBuilder.availableFilters[className];
                        } else {
                            return elementObj.name;
                        }
                    },
                    'dragging' : function(dragElement, component) {
                        if ($(dragElement).parent().is("tr")) {
                            //is column
                            var tr = $(dragElement).parent();
                            var table = tr.closest("table");
                            
                            if (table.closest(".dataList").hasClass("card-layout-active")) {
                                //card layout
                                if (!$(dragElement).is("td")) {
                                    var replace = $('<td data-cbuilder-id="new-dragging">'+UI.escapeHTML(component.label)+'</td>');
                                    dragElement.replaceWith(replace);
                                    dragElement = replace;
                                }
                            } else {
                                //table layout
                                if (!$(dragElement).is("th")) {
                                    var replace = $('<th data-cbuilder-id="new-dragging">'+UI.escapeHTML(component.label)+'</th>');
                                    dragElement.replaceWith(replace);
                                    dragElement = replace;

                                    //add tbody td
                                    var index = tr.find("th").index(dragElement);
                                    table.find("tbody tr").each(function(){
                                        var ctr = $(this);
                                        var td = $('<td class="dragging" data-cbuilder-select="new-dragging">'+UI.escapeHTML(component.label)+'</td>');
                                        if (index === 0) {
                                            ctr.prepend(td);
                                        } else {
                                            ctr.find("td:eq("+(index - 1)+")").after(td);
                                        }
                                    });
                                } else {
                                    var id = $(dragElement).attr("data-cbuilder-id");

                                    //move tbody td together
                                    var index = tr.find("th").index(dragElement);
                                    var cindex = table.find("tbody tr:eq(0) td").index(table.find("tbody tr:eq(0) td[data-cbuilder-select='"+id+"']"));
                                    if (cindex < index) {
                                        index++;
                                    }

                                    table.find("tbody tr").each(function(){
                                        var ctr = $(this);
                                        var td = ctr.find("[data-cbuilder-select='"+id+"']");

                                        if (index === 0) {
                                            ctr.prepend(td);
                                        } else {
                                            ctr.find("td:eq("+(index - 1)+")").after(td);
                                        }
                                    });
                                }
                            }
                        } else {
                            //is filter
                            if (!$(dragElement).is("span.filter-cell")) {
                                var replace = $('<span class="filter-cell "><input type="text" size="10" placeholder="'+UI.escapeHTML(component.label)+'"/></span></div>');
                                dragElement.replaceWith(replace);
                                dragElement = replace;
                                
                                //remove tbody temporary td if exist
                                $(dragElement).closest(".dataList").find("td.dragging").remove();
                            }
                        }
                        return dragElement;
                    },
                    'afterMoved' : function(element, elementObj, component) {
                        if ($(element).parent().is("tr")) {
                            var tr = $(element).parent();
                            var table = tr.closest("table");
                            
                            //move tbody td together
                            var index = tr.find("th").index(element);
                            var cindex = table.find("tbody tr:eq(0) td.column_body").index(table.find("tbody tr:eq(0) td.column_body[data-cbuilder-select='"+elementObj.id+"']"));
                            if (cindex < index) {
                                index++;
                            }

                            table.find("tbody tr").each(function(){
                                var ctr = $(this);
                                var td = ctr.find("td.column_body[data-cbuilder-select='"+elementObj.id+"']");

                                if (index === 0) {
                                    ctr.prepend(td);
                                } else {
                                    ctr.find("td.column_body:eq("+(index - 1)+")").after(td);
                                }
                            });
                        } 
                    },
                    'getPasteTemporaryNode' : function(elementObj, component) {
                        if (elementObj.id.indexOf(DatalistBuilder.columnPrefix) === 0) {
                            return '<th></th>';
                        }
                        return '<div></div>';
                    },
                    'customPropertyOptions' : function(elementOptions, element, elementObj, paletteElement) {
                        if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) === 0) {
                            return DatalistBuilder.getFilterPropertiesDefinition();
                        } else {
                            return DatalistBuilder.getColumnPropertiesDefinition();
                        }
                    },
                    'getStylePropertiesDefinition' : function(elementObj, component) {
                        if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) === 0) {
                            return component.builderTemplate.stylePropertiesDefinition;
                        } else {
                            return component.builderTemplate.tableStylePropertiesDefinition;
                        }
                    },
                    'isSubSelectAllowActions' : function(elementObj, component) {
                        if (CustomBuilder.data.responsive_layout === "card-layout") {
                            return true;
                        }
                        return false;
                    },
                    'isDraggable' : function(elementObj, component) {
                        if (elementObj.id.indexOf(DatalistBuilder.columnPrefix) === 0 
                                && CustomBuilder.Builder.selectedEl.closest(".dataList").hasClass("card-layout-active")) { //if row action and card layout
                            return false;
                        }
                        return true;
                    },
                    'isPastable' : function(elementObj, component){
                        var copied = CustomBuilder.getCopiedElement();
                        if (copied !== null && copied !== undefined) {
                            return true;
                        }
                        return false;
                    },
                    'renderPermission' : function(detailsDiv, element, elementObj, component, callback) {
                        var self = CustomBuilder.Builder;
                        
                        var props = self.parseElementProps(elementObj);
        
                        var permissionObj = props;
                        var key = CustomBuilder.Builder.permissionRuleKey;
                        if (key !== "default") {
                            if (props["permission_rules"] === undefined) {
                                props["permission_rules"] = {};
                            }
                            if (props["permission_rules"][key] === undefined) {
                                props["permission_rules"][key] = {};
                            }
                            permissionObj = props["permission_rules"][key];
                        }
                        
                        if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) !== -1) {
                            self._internalRenderPermission(detailsDiv, element, elementObj, component, permissionObj, callback);
                        } else {
                            DatalistBuilder.renderColumnPermission(detailsDiv, element, elementObj, component, permissionObj, callback);
                        }
                    },
                    'tableStylePropertiesDefinition' : $.extend(true, [], DatalistBuilder.tableStylePropertiesDefinition()),
                    'navigable' : false,
                    'dragHtml' : '<span></span>'
                }
            };
        
            //populate palette
            CustomBuilder.initPaletteElement(get_cbuilder_msg('dbuilder.columnsFilters'), temp.name, temp.displayLabel, '<i class="far fa-hdd"></i>', {}, {}, true, cssClass, meta);
        }
        DatalistBuilder.availableColumns = fields;
        
        //remove not exist columns and filters
        var i = CustomBuilder.data.filters.length;
        while (i--) {
            var filter = CustomBuilder.data.filters[i];
            if (DatalistBuilder.availableColumns[filter.name] === undefined) { 
                CustomBuilder.data.filters.splice(i, 1);
            } 
        }
        var i = CustomBuilder.data.columns.length;
        while (i--) {
            var column = CustomBuilder.data.columns[i];
            if (DatalistBuilder.availableColumns[column.name] === undefined) { 
                CustomBuilder.data.columns.splice(i, 1);
            } 
        }
        
        //handle order by
        if (CustomBuilder.data.orderBy !== undefined && CustomBuilder.data.orderBy !== "") {
            if (DatalistBuilder.availableColumns[CustomBuilder.data.orderBy] === undefined) { 
                CustomBuilder.data.orderBy = "";
            }
        }
        
        deferrer.resolve();
    },
    
    /*
     * Initialize builder component for datalist
     */
    initDatalistComponent: function() {
        var meta = {
            builderTemplate : {
                'getStylePropertiesDefinition' : function(elementObj, component) {
                    var selectedEl = CustomBuilder.Builder.selectedEl;
                    if ($(selectedEl).is(".column_header")) {
                        return component.builderTemplate.columnStylePropertiesDefinition;
                    } else if ($(selectedEl).is(".rowaction_header")) {
                        return component.builderTemplate.rowActionStylePropertiesDefinition;
                    } else if ($(selectedEl).is(".filter-cell")) {
                        return component.builderTemplate.filterStylePropertiesDefinition;
                    } else if ($(selectedEl).is(".btn")) {
                        return component.builderTemplate.actionStylePropertiesDefinition;
                    } else {
                        return component.builderTemplate.cardStylePropertiesDefinition;
                    }
                },
                'isSubSelectAllowActions' : function(elementObj, component) {
                    return true;
                },
                'getLabel' : function(elementObj, component) {
                    if (CustomBuilder.Builder.highlightEl !== null && $(CustomBuilder.Builder.highlightEl).is("tr")) {
                        return get_cbuilder_msg('dbuilder.card');
                    } else {
                        return "";
                    }
                },
                'isPastable' : function(elementObj, component){
                    var copied = CustomBuilder.getCopiedElement();
                    if (copied !== null && copied !== undefined) {
                        return true;
                    }
                    return false;
                },
                'cardStylePropertiesDefinition' : $.extend(true, [], DatalistBuilder.datalistStylePropertiesDefinition("card")),
                'columnStylePropertiesDefinition' : $.extend(true, [], DatalistBuilder.datalistStylePropertiesDefinition("column")),
                'rowActionStylePropertiesDefinition' : $.extend(true, [], DatalistBuilder.datalistStylePropertiesDefinition("rowaction")),
                'filterStylePropertiesDefinition' : $.extend(true, [], DatalistBuilder.datalistStylePropertiesDefinition("filter")),
                'actionStylePropertiesDefinition' : $.extend(true, [], DatalistBuilder.datalistStylePropertiesDefinition("action")),
                'draggable' : false,
                'movable' : false,
                'deletable' : false,
                'copyable' : false,
                'navigable' : false,
                'supportProperties' : false
            }
        };
        CustomBuilder.initPaletteElement("", "org.joget.apps.datalist.model.DataList", "", null, null, null, false, "", meta);
    },
    
    /*
     * Initialize builder component for action and row action
     */
    initActionList: function(deferreds) {
        var wait = $.Deferred();
        deferreds.push(wait);
        
        $.getJSON(
            CustomBuilder.contextPath + '/web/json/console/app/' + CustomBuilder.appId + '/' +  CustomBuilder.appVersion + '/builder/actions',
            function(returnedData){
                for(e in returnedData.actions){
                    var action = returnedData.actions[e];
                    
                    DatalistBuilder.availableActions[action.className] = action;
                    var icon = action.icon;
                    if (icon === undefined || icon === null && icon === "") {
                        icon = '<span class="fa-stack"><i><span style="border: 1.4px solid #4c4c4c; height: 10px; width: 15px; display: block; position: absolute; border-radius: 3px; left: 3px; top: 4px;"></span></i><i class="fas fa-hand-point-up fa-stack-xs" style="padding-top: 2px;"></i></span>';
                    }
                    
                    var meta = {
                        supportColumn : action.supportColumn,
                        supportRow : action.supportRow,
                        supportList : action.supportList,
                        builderTemplate : {
                            'getParentContainerAttr' : function(elementObj, component) {
                                if (elementObj === undefined || elementObj === null) {
                                    if (!component.supportRow) {
                                        return "actions";
                                    } else if (!component.supportList) {
                                        return "rowActions";
                                    } else {
                                        return "all_actions";
                                    }
                                } else if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) !== -1) {
                                    return "rowActions";
                                } else {
                                    return "actions";
                                }
                            },
                            'getParentDataHolder' : function(elementObj, component) {
                                if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) !== -1) {
                                    return "rowActions";
                                } else {
                                    return "actions";
                                }
                            },
                            'dragging' : function(dragElement, component) {
                                if ($(dragElement).parent().is(".row_action_container")) {
                                    var th = $(dragElement).parent();
                                    var table = th.closest("table");

                                    //is row action
                                    if (table.closest(".dataList").hasClass("card-layout-active")) {
                                        //card layout
                                        if (!$(dragElement).is("div")) {
                                            var replace = $('<div data-cbuilder-id="new-dragging"><a href="#" class="btn btn-sm btn-primary">'+UI.escapeHTML(component.label)+'</a></div>');
                                            dragElement.replaceWith(replace);
                                            dragElement = replace;
                                        }
                                    } else {
                                        //table layout
                                        if (!$(dragElement).is("div")) {
                                            var replace = $('<div data-cbuilder-id="new-dragging"><a href="#" class="btn btn-sm btn-primary">'+UI.escapeHTML(component.label)+'</a></div>');
                                            dragElement.replaceWith(replace);
                                            dragElement = replace;

                                            //add tbody td
                                            var index = th.find("> div").index(dragElement);
                                            table.find("tbody tr").each(function(){
                                                var ctr = $(this).find("td.row_action_container");
                                                var ra = $('<div class="dragging" data-cbuilder-select="new-dragging"><a href="#" class="btn btn-sm btn-primary">'+UI.escapeHTML(component.label)+'</a></div>');
                                                if (index === 0) {
                                                    ctr.prepend(ra);
                                                } else {
                                                    ctr.find("> div:eq("+(index - 1)+")").after(ra);
                                                }
                                            });
                                        } else {
                                            var id = $(dragElement).attr("data-cbuilder-id");

                                            //move tbody td together
                                            var index = th.find("> div").index(dragElement);
                                            var cindex = table.find("tbody tr:eq(0) td.row_action_container > div").index(table.find("tbody tr:eq(0) td.row_action_container > div[data-cbuilder-select='"+id+"']"));
                                            if (cindex < index) {
                                                index++;
                                            }

                                            table.find("tbody tr").each(function(){
                                                var ctr = $(this).find("td.row_action_container");
                                                var ra = ctr.find("[data-cbuilder-select='"+id+"']");

                                                if (index === 0) {
                                                    ctr.prepend(ra);
                                                } else {
                                                    ctr.find("> div:eq("+(index - 1)+")").after(ra);
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    //is action
                                    if (!$(dragElement).is("button")) {
                                        var replace = $('<button class="form-button btn button">'+UI.escapeHTML(component.label)+'</button>');
                                        dragElement.replaceWith(replace);
                                        dragElement = replace;

                                        //remove tbody temporary row action if exist
                                        $(dragElement).closest(".dataList").find("td.row_action_container .dragging").remove();
                                    }
                                }
                                return dragElement;
                            },
                            'afterMoved' : function(element, elementObj, component) {
                                if ($(element).parent().is("th")) {
                                    var th = $(element).parent();
                                    var table = th.closest("table");

                                    //move tbody row action together
                                    var index = th.find("> div").index(element);
                                    var cindex = table.find("tbody tr:eq(0) td.row_action_container > div").index(table.find("tbody tr:eq(0) td.row_action_container > div[data-cbuilder-select='"+elementObj.id+"']"));
                                    if (cindex < index) {
                                        index++;
                                    }

                                    table.find("tbody tr").each(function(){
                                        var ctr = $(this).find("td.row_action_container");
                                        var ra = ctr.find("[data-cbuilder-select='"+elementObj.id+"']");

                                        if (index === 0) {
                                            ctr.prepend(ra);
                                        } else {
                                            ctr.find("> div:eq("+(index - 1)+")").after(ra);
                                        }
                                    });
                                }
                            },
                            'customPropertyOptions' : function(elementOptions, element, elementObj, component) {
                                if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) === 0) {
                                    return DatalistBuilder.getRowActionPropertiesDefinition(elementOptions, component);
                                } else {
                                    return DatalistBuilder.getActionPropertiesDefinition(elementOptions, component);
                                }
                            },
                            'getStylePropertiesDefinition' : function(elementObj, component) {
                                if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) === 0) {
                                    return component.builderTemplate.tableStylePropertiesDefinition;
                                } else {
                                    return component.builderTemplate.stylePropertiesDefinition;
                                }
                            },
                            'isSubSelectAllowActions' : function(elementObj, component) {
                                if (CustomBuilder.data.responsive_layout === "card-layout") {
                                    return true;
                                }
                                return false;
                            },
                            'isDraggable' : function(elementObj, component) {
                                if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) === 0 
                                        && CustomBuilder.Builder.selectedEl.closest(".dataList").hasClass("card-layout-active")) { //if row action and card layout
                                    return false;
                                }
                                return true;
                            },
                            'isPastable' : function(elementObj, component){
                                var copied = CustomBuilder.getCopiedElement();
                                if (copied !== null && copied !== undefined) {
                                    return true;
                                }
                                return false;
                            },
                            'tableStylePropertiesDefinition' : $.extend(true, [], DatalistBuilder.tableStylePropertiesDefinition()),
                            'navigable' : false,
                            'dragHtml' : '<span></span>'
                        }
                    };
                    
                    CustomBuilder.initPaletteElement(get_cbuilder_msg('dbuilder.actions'), action.className, action.label, icon, action.propertyOptions, action.defaultPropertyValues, true, "", meta);
                }
                wait.resolve();
            }
        );
    },
    
    /*
     * Retrieve available filter plugin for xray mode
     */
    initFilterList : function(deferreds){
        var wait = $.Deferred();
        deferreds.push(wait);
        
        $.getJSON(
            CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.apps.datalist.model.DataListFilterType',
            function(returnedData){
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        DatalistBuilder.availableFilters[returnedData[e].value] = returnedData[e].label;
                    }
                }
                wait.resolve();
            }
        );
    },
    
    /*
     * Retrieve available formatter plugin for xray mode
     */
    initFormatterList : function(deferreds){
        var wait = $.Deferred();
        deferreds.push(wait);
        
        $.getJSON(
            CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.apps.datalist.model.DataListColumnFormat',
            function(returnedData){
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        DatalistBuilder.availableFormatters[returnedData[e].value] = returnedData[e].label;
                    }
                }
                wait.resolve();
            }
        );
    },
    
    /*
     * A callback method from CustomBuilder.Builder.addElement
     */
    addElement : function(component, dragElement, callback) {
        var parentArray;
        var type = "";
        if ($(dragElement).parent().is("[data-cbuilder-filters]")) {
            parentArray = CustomBuilder.data.filters;
            type = "filter";
        } else if ($(dragElement).parent().is("[data-cbuilder-columns]")) {
            parentArray = CustomBuilder.data.columns;
            type = "column";
            
            //remove tbody temporary td
            $(dragElement).closest("table").find("tbody td.dragging").remove();
        } else if ($(dragElement).parent().is("[data-cbuilder-rowActions]")) {
            parentArray = CustomBuilder.data.rowActions;
            type = "rowAction";
            
            //remove tbody temporary row actions
            $(dragElement).closest("table").find("tbody td.row_action_container .dragging").remove();
            
        } else if ($(dragElement).parent().is("[data-cbuilder-actions]")) {
            parentArray = CustomBuilder.data.actions;
            type = "action";
        }
        
        var elementObj = {
            id : DatalistBuilder.getId(type)
        };
        
        if (type === "filter" || type === "column") {
            elementObj.name = component.className;
            elementObj.label = component.label;
            
            if (type === "filter") {
                elementObj.filterParamName = CustomBuilder.config.builder.options.filterParam + elementObj.name;
                elementObj.type = {
                    "className": "org.joget.apps.datalist.lib.TextFieldDataListFilterType",
                    "properties": {}
                };
            } else {
                elementObj.filterable = component.filterable;
                elementObj.hidden = "false";
                elementObj.sortable = "false";
            }
        } else {
            elementObj.className = component.className;
            elementObj.label = component.label;
            elementObj.properties = $.extend(true, {}, component.properties);
            
            if (type === "rowAction") {
                elementObj.properties['link-css-display-type'] = 'btn btn-sm btn-primary';
            }
        }

        var index = 0;
        var container = $(dragElement).parent().closest("[data-cbuilder-"+component.builderTemplate.getParentContainerAttr(elementObj, component)+"]");
        index = $(container).find("> *").index(dragElement);
        parentArray.splice(index, 0, elementObj);

        callback(elementObj);
    },
    
    /*
     * Utility method to retrieve unique id based on type
     */
    getId : function (t) {
        var id = DatalistBuilder[t + 'Prefix'] + DatalistBuilder[t + 'IndexCounter'];
        while(CustomBuilder.Builder.frameBody.find('[data-cbuilder-id="'+id+'"]').length > 0){
            id = DatalistBuilder[t + 'Prefix'] + DatalistBuilder[t + 'IndexCounter'];
            DatalistBuilder[t + 'IndexCounter']++;
        }
        return id;
    },
    
    /*
     * A callback method called from the default component.builderTemplate.render method
     */
    renderElement : function(element, elementObj, component, callback) {
        DatalistBuilder.destroyFootable($(element).closest("table"));
        
        var deferrer = $.Deferred();
        
        if (elementObj.id.indexOf(DatalistBuilder.columnPrefix) === 0) {
            DatalistBuilder.renderColumn(element, elementObj, component, deferrer);
        } else if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) === 0) {
            DatalistBuilder.renderFilter(element, elementObj, component, deferrer);
        } else if (elementObj.id.indexOf(DatalistBuilder.actionPrefix) === 0) {
            DatalistBuilder.renderAction(element, elementObj, component, deferrer);
        } else if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) === 0) {
            DatalistBuilder.renderRowActions(element, elementObj, component, deferrer);
        } else if (elementObj.id === CustomBuilder.data.id) {
            DatalistBuilder.updateDatalistStyle(element, elementObj, component, deferrer);
        }
        
        $.when.apply($, [deferrer]).then(function() {
            var newElement = arguments[0].element;
            callback(newElement);
        });
    },
    
    /*
     * Update datalist style for default style of card, column. filter, row action and action
     */
    updateDatalistStyle : function(element, elementObj, component, deferrer) {
        var builder = CustomBuilder.Builder;
        
        builder.handleStylingProperties(element, elementObj, "card", ".dataList.card-layout-active tbody tr");
        builder.handleStylingProperties(element, elementObj, "filter", ".dataList .filter-cell");
        builder.handleStylingProperties(element, elementObj, "column-header", ".dataList table .column_header");
        builder.handleStylingProperties(element, elementObj, "column", ".dataList table .column_body");
        builder.handleStylingProperties(element, elementObj, "rowaction-header", ".dataList table .rowaction_header");
        builder.handleStylingProperties(element, elementObj, "rowaction", ".dataList table .rowaction_body");
        builder.handleStylingProperties(element, elementObj, "action", ".dataList .actions .btn");
        
        if (deferrer !== undefined) {
            deferrer.resolve({element : element});
        }
    },
    
    /*
     * Rendering column, call form DatalistBuilder.renderElement
     */
    renderColumn : function(element, elementObj, component, deferrer) {
        var builder = CustomBuilder.Builder;
        
        var table = $(element).closest("table");
        
        if (element.is("td")) {
            //is card layout, add th and remove this
            var newTh = $('<th data-cbuilder-id="new-dragging">'+UI.escapeHTML(component.label)+'</th>');
            var prev = element.prev("td[data-cbuilder-select]");
            if (prev.length > 0) {
                var prevId = prev.data('cbuilder-select');
                table.find("thead tr th[data-cbuilder-id='"+prevId+"']").after(newTh);
            } else {
                table.find("thead tr").prepend(newTh);
            }
            element.remove();
            element = newTh;
        }
        
        var th = element;
        
        th.addClass("column_header column_" + elementObj.name);
        
        if (elementObj.sortable === "true") {
            th.addClass("sortable");
            th.html('<a href="#">'+elementObj.label+'</a><span class="overlay"></span>');
        } else {
            th.html(elementObj.label+'<span class="overlay"></span>');
        }
        th.data("name", elementObj.label);
        if (elementObj.headerAlignment !== undefined && elementObj.headerAlignment !== "") {
            th.addClass(elementObj.headerAlignment);
        }
        if (elementObj.width !== undefined && elementObj.width !== "") {
            th.css("width", elementObj.width);
        }
        if (elementObj.hidden !== undefined && elementObj.hidden === "true") {
            th.find(".overlay").attr("data-cbuilder-element-invisible", "");
        }
        
        builder.handleStylingProperties(th, elementObj, "header");
        
        var style = "";
        if (elementObj.style !== undefined && elementObj.style !== "") {
            var style = elementObj.style;
            if (style.substr(style.length - 1) !== ";") {
                style += ";";
            }
        }
        
        var value = elementObj.label;
        var rowData = DatalistBuilder.sampleData;
        if (rowData !== undefined && rowData !== null) {
            if (rowData[elementObj.name] !== undefined && rowData[elementObj.name] !== null && rowData[elementObj.name] !== "") {
                value = rowData[elementObj.name];
            }
        } else {
            rowData = {};
        }
        
        var index = th.parent().find("th").index(th);
        
        table.find('tbody tr [data-cbuilder-select="'+elementObj.id+'"], .card_layout_body_cell').remove();
        table.find("tbody tr").each(function(){
            var tr = $(this);
            var label = "<span class=\"value\">" + value + "</span>";
            if (elementObj.action !== undefined && elementObj.action.className !== undefined && elementObj.action.className !== "") {
                label = '<a href="#">'+ label + '</a>';
            }

            var td = $('<td data-cbuilder-select="'+elementObj.id+'" >'+label+'</td>');
            td.addClass("column_body column_" + elementObj.name);

            if (elementObj.alignment !== undefined && elementObj.alignment !== "") {
                td.addClass(elementObj.alignment);
            }
            if (elementObj.width !== undefined && elementObj.width !== "") {
                td.css("width", elementObj.width);
            }
            if (style !== "") {
                var orgStyle = td.attr("style");
                if (orgStyle === undefined) {
                    orgStyle = "";
                }
                td.attr("style", style + orgStyle);
            }
            if (elementObj.hidden !== undefined && elementObj.hidden === "true") {
                td.attr("data-cbuilder-element-invisible", "");
            }

            builder.handleStylingProperties(td, elementObj);
            if (index === 0) {
                tr.prepend(td);
            } else {
                tr.find("td:eq("+(index-1)+")").after(td);
            }
        });

        DatalistBuilder.adjustTableOverlaySize(table);
        deferrer.resolve({element : element});
        
        
        
        if (elementObj.format !== undefined && elementObj.format.className !== undefined && elementObj.format.className !== "") {
            var colStr = JSON.encode(elementObj);
            var rowStr = JSON.encode(rowData);
            
            CustomBuilder.cachedAjax({
                type: "POST",
                data: {
                    "column": colStr,
                    "value" : value,
                    "row" : rowStr,
                    "appId" : CustomBuilder.appId,
                    "listId" : CustomBuilder.data.id
                },
                url: CustomBuilder.contextPath + '/web/dbuilder/getFormatterTemplate',
                dataType : "json",
                beforeSend: function (request) {
                    request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                },
                success: function(response) {
                    if (response.formatted !== undefined && response.formatted !== "") {
                        var formatted = $('<div>' + response.formatted + '</div>');
                        formatted.find("link, script").remove();
                        table.find('tbody tr [data-cbuilder-select="'+elementObj.id+'"] span.value').html(formatted.html());
                    } else {
                        table.find('tbody tr [data-cbuilder-select="'+elementObj.id+'"] span.value').html(elementObj.label);
                    }
                },
                error: function() {
                    //ignore
                }
            });
        }
    },
    
    /*
     * Rendering filter, call form DatalistBuilder.renderElement
     */
    renderFilter : function(element, elementObj, component, deferrer) {
        if (elementObj.filterParamName === undefined) {
            elementObj.filterParamName = CustomBuilder.config.builder.options.filterParam + elementObj.name;
        }
        
        var jsonStr = JSON.encode(elementObj);
        CustomBuilder.cachedAjax({
            type: "POST",
            data: {"json": jsonStr },
            url: CustomBuilder.contextPath + '/web/dbuilder/getFilterTemplate',
            dataType : "text",
            beforeSend: function (request) {
                request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            },
            success: function(response) {
                var newElement = $('<span class="filter-cell ">' + response + '</span>');
                
                if (elementObj.hidden === "true") {
                    newElement.attr("data-cbuilder-element-invisible", "");
                }
                
                CustomBuilder.Builder.handleStylingProperties(newElement, elementObj);
                
                $(element).replaceWith(newElement);
                deferrer.resolve({element : newElement});
            }
        });
    },
    
    /*
     * Rendering action, call form DatalistBuilder.renderElement
     */
    renderAction : function(element, elementObj, component, deferrer) {
        
        var label = elementObj.label;
        if (elementObj.properties.label !== undefined && elementObj.properties.label !== "") {
            label = elementObj.properties.label;
        }
        
        var replace = $('<button class="form-button btn button">'+label+'</button>');
        element.replaceWith(replace);
        element = replace;
        
        CustomBuilder.Builder.handleStylingProperties(element, elementObj.properties);
                                        
        deferrer.resolve({element : element});
    },
    
    /*
     * Rendering row action, call form DatalistBuilder.renderElement
     */
    renderRowActions : function(element, elementObj, component, deferrer) {
        var builder = CustomBuilder.Builder;
        
        var table = $(element).closest("table");
        
        if (element.closest("td").length > 0) {
            //is card layout, add to th and remove this
            var prev = element.prev("[data-cbuilder-select]");
            if (prev.length > 0) {
                var prevId = prev.data('cbuilder-select');
                table.find("thead tr th.row_action_container div[data-cbuilder-id='"+prevId+"']").after(element);
            } else {
                table.find("thead tr th.row_action_container").prepend(element);
            }
        }
        
        var ra = element;
        ra.css("width", "auto");
        ra.addClass("rowaction_header header_" + elementObj.id);
        
        var label = elementObj.label;
        if (elementObj.properties.label !== undefined && elementObj.properties.label !== "") {
            label = elementObj.properties.label;
        }
        
        var headerLabel = elementObj.properties.header_label;
        if (headerLabel === undefined || headerLabel === null || headerLabel === "") {
            headerLabel = "&nbsp;";
        }
        ra.html(headerLabel+'<span class="overlay"></span>');
        
        builder.handleStylingProperties(ra, elementObj.properties, "header");
        
        var width = ra.width();
        
        var index = ra.parent().find("> div").index(ra);
        
        table.find('tbody tr td.row_action_container [data-cbuilder-select="'+elementObj.id+'"]').remove();
        table.find("tbody tr").each(function(){
            var tr = $(this);
            var td = tr.find("td.row_action_container");
            var nra = $('<div data-cbuilder-select="'+elementObj.id+'"><a href="#">'+label+'</a></div>');
            nra.addClass("rowaction_body body_" + elementObj.id);
            
            builder.handleStylingProperties(nra, elementObj.properties);
            builder.handleStylingProperties(nra.find("a"), elementObj.properties, "link");
            
            if (index === 0) {
                td.prepend(nra);
            } else {
                td.find("> div:eq("+(index-1)+")").after(nra);
            }
        });
        
        var bodyWidth = table.find('tbody tr:eq(0) td.row_action_container [data-cbuilder-select="'+elementObj.id+'"]').width();
        if (bodyWidth > width) {
            width = bodyWidth;
        }
        ra.width(width);
        table.find('tbody tr td.row_action_container [data-cbuilder-select="'+elementObj.id+'"]').width(width);
        
        DatalistBuilder.adjustTableOverlaySize(table);
        
        deferrer.resolve({element : element});
    },
    
    /*
     * Used to render the invibility flag overlay
     */
    adjustTableOverlaySize : function(table) {
        var height = $(table).height();
        var thHeight = $(table).find("thead").height();
        var bottom = height - thHeight;
        $(table).find("thead .overlay").each(function(){
            $(this).css("bottom", "-" + bottom + "px");
        });
    },
    
    /*
     * A callback method called from the default component.builderTemplate.unload method
     */
    unloadElement : function(element, elementObj, component) {
        if (elementObj.id.indexOf(DatalistBuilder.columnPrefix) === 0) {
            var table = $(element).closest("table");
            table.find("tbody tr").each(function(){
                var tr = $(this);
                tr.find("td[data-cbuilder-select='"+elementObj.id+"']").remove();
            });
        } else if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) === 0) {
            var table = $(element).closest("table");
            table.find("tbody tr").each(function(){
                var tr = $(this);
                tr.find("td.row_action_container [data-cbuilder-select='"+elementObj.id+"']").remove();
            });
        }
    },
    
    /*
     * A callback method called from the default component.builderTemplate.selectNode method.
     * It used to add column and add section action button when a section is selected
     */
    selectElement : function(element, elementObj, component) {
        var builder = CustomBuilder.Builder;
        
        var type = "";
        if (elementObj.id.indexOf(DatalistBuilder.columnPrefix) === 0) {
            type = "column";
        } else if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) === 0) {
            type = "filter";
        } else if (elementObj.id.indexOf(DatalistBuilder.actionPrefix) === 0) {
            type = "action";
        } else if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) === 0) {
            type = "rowAction";
        }
        
        if (type !== "") {
            $("#element-options").append('<a id="default-style-btn" href="" title="'+get_cbuilder_msg('style.defaultStyles')+'" style=""><i class="las la-palette"></i></a>');
            $("#default-style-btn").off("click");
            $("#default-style-btn").on("click", function(){
                $("body").removeClass("no-right-panel");
                $("#element-properties-tab-link").hide();
                $("#right-panel #element-properties-tab").find(".property-editor-container").remove();
                
                builder.editStyles(CustomBuilder.data, builder.frameBody.find(".dataList"), CustomBuilder.data, builder.parseDataToComponent(CustomBuilder.data));
                $("#style-properties-tab-link a").trigger("click");
                
                return false;
            });
        }
    },
    
    /*
     * A callback method called from the default CustomBuilder.Builder.pasteNode method.
     * This method is to handle pasting column before row actions
     */
    pasteElement : function(element, elementObj, component, copiedObj, copiedComponent) {
        var builder = CustomBuilder.Builder;
        
        if (component.builderTemplate.getParentDataHolder(elementObj, component) !== copiedComponent.builderTemplate.getParentDataHolder(copiedObj, copiedComponent)) {
            var parent = builder.frameBody.find(".dataList");
            if (copiedObj.id.indexOf(DatalistBuilder.columnPrefix) === 0) {
                var parentDataArray = $(parent).data("data")[copiedComponent.builderTemplate.getParentDataHolder(copiedObj, copiedComponent)];
                parentDataArray.push(copiedObj);

                var temp = $('<th></th>');
                var container = null;
                if ($(parent).is("[data-cbuilder-"+copiedComponent.builderTemplate.getParentContainerAttr(copiedObj, copiedComponent)+"]")) {
                    container = $(parent);
                } else {
                    container = $(parent).find("[data-cbuilder-"+copiedComponent.builderTemplate.getParentContainerAttr(copiedObj, copiedComponent)+"]:eq(0)");
                }

                if (container.length > 0) {
                    container.find(".gap").before(temp);
                }
            
                builder.component = copiedComponent;
                builder.renderElement(copiedObj, temp, copiedComponent, true);
            } else {
                builder._pasteNode(parent, copiedObj, copiedComponent);
            }
        } else {
            builder._pasteNode(element, copiedObj, copiedComponent);
        }
    },
    
    saveEditProperties : function(container, elementProperty, element) {
        
    },
    
    /*
     * A callback method from CustomBuilder.Builder.updateElementId to update id to an unqiue value
     */
    updateElementId : function(elementObj) {
        var type = "";
        
        if (elementObj.id.indexOf(DatalistBuilder.columnPrefix) === 0) {
            type = "column";
        } else if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) === 0) {
            type = "filter";
        } else if (elementObj.id.indexOf(DatalistBuilder.actionPrefix) === 0) {
            type = "action";
        } else if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) === 0) {
            type = "rowAction";
        }
        
        elementObj.id = DatalistBuilder.getId(type);
    },
    
    /*
     * A callback method called from the CustomBuilder.Builder.renderNodeAdditional
     * It used to render the info of an element
     */
    renderXray : function(detailsDiv, element, elementObj, component , callback) {
        var dl = detailsDiv.find('dl');
        
        if (elementObj.id.indexOf(DatalistBuilder.columnPrefix) === 0) {
            dl.find('> *:eq(1)').text(elementObj.name);
            if ($(element).closest(".card-layout-active").length === 0) {
                var sortable = get_cbuilder_msg('dbuilder.unsortable');
                if (elementObj.sortable === "true") {
                    sortable = get_cbuilder_msg('dbuilder.sortable');
                }
                dl.append('<dt><i class="las la-sort" title="'+get_cbuilder_msg('dbuilder.sortable')+'"></i></dt><dd>'+sortable+'</dd>');
                var exportable = get_cbuilder_msg('dbuilder.exportable');
                if ((elementObj.hidden === "true" && elementObj.include_export !== "true") ||
                    (elementObj.hidden !== "true" && elementObj.exclude_export === "true")) {
                    exportable = get_cbuilder_msg('dbuilder.unexportable');
                }
                dl.append('<dt><i class="las la-file-export" title="'+get_cbuilder_msg('dbuilder.exportable')+'"></i></dt><dd>'+exportable+'</dd>');
                var width = get_cbuilder_msg("dbuilder.default");
                if (elementObj.width !== undefined && elementObj.width !== "") {
                    width = elementObj.width;
                }
                dl.append('<dt><i class="las la-ruler-horizontal" title="'+get_cbuilder_msg('dbuilder.width')+'"></i></dt><dd>'+width+'</dd>');
            }
            var action = "-";
            if (elementObj.action !== undefined && elementObj.action.className !== undefined && elementObj.action.className !== "") {
                action = elementObj.action.className;
                if (DatalistBuilder.availableActions[action] !== undefined) {
                    action = DatalistBuilder.availableActions[action];
                } else {
                    action += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
                }
            }
            dl.append('<dt><i class="las la-link" title="'+get_cbuilder_msg('dbuilder.action')+'"></i></dt><dd>'+action+'</dd>');
            var format = "-";
            if (elementObj.format !== undefined && elementObj.format.className !== undefined && elementObj.format.className !== "") {
                format = elementObj.format.className;
                if (DatalistBuilder.availableFormatters[format] !== undefined) {
                    format = DatalistBuilder.availableFormatters[format];
                } else {
                    format += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
                }
            }
            dl.append('<dt><i class="las la-paint-brush" title="'+get_cbuilder_msg('dbuilder.formatter')+'"></i></dt><dd>'+format+'</dd>');
        } else if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) === 0) {
            dl.find('> *:eq(1)').text(elementObj.name);
            var type = "-";
            if (elementObj.type !== undefined && elementObj.type.className !== undefined && elementObj.type.className !== "") {
                type = elementObj.type.className;
                if (DatalistBuilder.availableFilters[type] !== undefined) {
                    type = DatalistBuilder.availableFilters[type];
                } else {
                    type += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
                }
            }
            dl.find("> *:eq(1)").after('<dt><i class="las la-cube" title="'+get_cbuilder_msg('dbuilder.filter')+'"></i></dt><dd>'+type+'</dd>');
        } else if (elementObj.id.indexOf(DatalistBuilder.actionPrefix) === 0) {
            //nothing
        } else if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) === 0) {
            var fields = [];
            if (elementObj['properties'] !== undefined && elementObj['properties']['rules'] !== undefined) {
                for (var i in elementObj['properties']['rules']) {
                    var name = elementObj['properties']['rules'][i].field;
                    if ($.inArray(name, fields) === -1) {
                        fields.push(name);
                    }
                }
            }
            dl.append('<dt><i class="las la-eye" title="'+get_cbuilder_msg('dbuilder.rowAction.visibility')+'"></i></dt><dd>'+fields.join(', ')+'</dd>');
        }
        
        callback();
    },
    
    /*
     * A callback method called from the CustomBuilder.Builder.renderNodeAdditional
     * It used to render the permission option of a column
     */
    renderColumnPermission : function (detailsDiv, element, elementObj, component, permissionObj, callback) {
        var self = CustomBuilder.Builder;
        var dl = detailsDiv.find('dl');
        
        dl.append('<dt class="authorized-web-row" ><i class="lab la-html5" title="'+get_advtool_msg('adv.permission.web')+'"></i></dt><dd class="authorized-web-row" ><div class="authorized-web-btns btn-group"></div></dd>');
        dl.find(".authorized-web-btns").append('<button type="button" class="btn btn-outline-success btn-sm visible-btn" >'+get_cbuilder_msg("ubuilder.visible")+'</button>');
        dl.find(".authorized-web-btns").append('<button type="button" class="btn btn-outline-success btn-sm hidden-btn" >'+get_cbuilder_msg("ubuilder.hidden")+'</button>');
        
        dl.append('<dt class="authorized-export-row" ><i class="las la-file-download" title="'+get_advtool_msg('adv.permission.export')+'"></i></dt><dd class="authorized-export-row" ><div class="authorized-export-btns btn-group"></div></dd>');
        dl.find(".authorized-export-btns").append('<button type="button" class="btn btn-outline-info btn-sm visible-btn" >'+get_cbuilder_msg("ubuilder.visible")+'</button>');
        dl.find(".authorized-export-btns").append('<button type="button" class="btn btn-outline-info btn-sm hidden-btn" >'+get_cbuilder_msg("ubuilder.hidden")+'</button>');
        
        if (permissionObj["hidden"] === "true") {
            $(dl).find(".authorized-web-btns .hidden-btn").addClass("active");
                
            if (permissionObj["include_export"] === "true") {
                $(dl).find(".authorized-export-btns .visible-btn").addClass("active");
            } else {
                $(dl).find(".authorized-export-btns .hidden-btn").addClass("active");
            }
        } else {
            $(dl).find(".authorized-web-btns .visible-btn").addClass("active");

            if (permissionObj["exclude_export"] === "true") {
                $(dl).find(".authorized-export-btns .hidden-btn").addClass("active");
            } else {
                $(dl).find(".authorized-export-btns .visible-btn").addClass("active");
            }
        }
        
        dl.on("click", ".btn", function(event) {
            if ($(this).hasClass("active")) {
                return false;
            }

            var group = $(this).closest(".btn-group");
            group.find(".active").removeClass("active");
            $(this).addClass("active");

            if ($(dl).find(".authorized-web-btns .hidden-btn").hasClass("active")) {
                permissionObj["hidden"] = "true";
                if ($(dl).find(".authorized-export-btns .hidden-btn").hasClass("active")) {
                    permissionObj["include_export"] = "";
                    permissionObj["exclude_export"] = "";
                } else {
                    permissionObj["include_export"] = "true";
                    permissionObj["exclude_export"] = "";
                }
            } else {
                permissionObj["hidden"] = "false";
                if ($(dl).find(".authorized-export-btns .hidden-btn").hasClass("active")) {
                    permissionObj["include_export"] = "";
                    permissionObj["exclude_export"] = "true";
                } else {
                    permissionObj["include_export"] = "";
                    permissionObj["exclude_export"] = "";
                }
            }
            
            var target = $(this).closest(".cbuilder-node-details").parent();
            if ($(target).is("[data-cbuilder-select]")) {
                //copy to others
                $(self.frameBody).find("[data-cbuilder-select='"+$(target).data("cbuilder-select")+"']").each(function(){
                    if (!$(this).is(target)) {
                        $(this).find("> .cbuilder-node-details .authorized-web-btns > .active").removeClass("active");
                        $(this).find("> .cbuilder-node-details .authorized-export-btns > .active").removeClass("active");
                        
                        if ($(dl).find(".authorized-web-btns .hidden-btn").hasClass("active")) {
                            $(this).find("> .cbuilder-node-details .authorized-web-btns .hidden-btn").addClass("active");
                        } else {
                            $(this).find("> .cbuilder-node-details .authorized-web-btns .visible-btn").addClass("active");
                        }
                        if ($(dl).find(".authorized-export-btns .hidden-btn").hasClass("active")) {
                            $(this).find("> .cbuilder-node-details .authorized-export-btns .hidden-btn").addClass("active");
                        } else {
                            $(this).find("> .cbuilder-node-details .authorized-export-btns .visible-btn").addClass("active");
                        }
                    }
                });
            }

            CustomBuilder.update();

            event.preventDefault();
            return false;
        });
        
        callback();
    },
    
    /*
     * Get properties for properties view
     */
    getBuilderProperties : function() {
        return CustomBuilder.data;
    },
    
    /*
     * Save properties from properties view
     */
    saveBuilderProperties : function(container, properties) {
        CustomBuilder.data = $.extend(CustomBuilder.data, properties);
        CustomBuilder.update();
        
        DatalistBuilder.refreshTableLayout();
    },
    
    /*
     * Get properties definition for properties view
     */
    getDatalistPropertiesDefinition : function() {
        return [
            {
                title: get_cbuilder_msg('dbuilder.basicProperties'),
                helplink: get_cbuilder_msg('dbuilder.basicProperties.helplink'),
                properties : [
                    {
                        label : get_cbuilder_msg('dbuilder.datalistId'),
                        name  : 'id',
                        required : 'true',
                        type : 'readonly'
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.datalistName'),
                        name  : 'name',
                        required : 'true',
                        type : 'textfield'
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.hidePageSizeSelector'),
                        name  : 'hidePageSize',
                        type : 'checkbox',
                        options : [
                            {label : '', value : 'true'}
                        ]
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.pageSizeSelectorOptions'),
                        name  : 'pageSizeSelectorOptions',
                        required : 'true',
                        value : '10,20,30,40,50,100',
                        type : 'textfield',
                        control_field: 'hidePageSize',
                        control_value: '',
                        control_use_regex: 'false'
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.pageSize'),
                        name  : 'pageSize',
                        required : 'true',
                        type : 'selectbox',
                        value : '0',
                        options_callback : function(props, values) {
                            var options = [{label : get_cbuilder_msg('dbuilder.pageSize.default'), value : '0'}];
                            if (values['pageSizeSelectorOptions'] === null || values['pageSizeSelectorOptions'] === undefined || values['pageSizeSelectorOptions'] === "") {
                                options.push({label : '10', value : '10'},
                                   {label : '20', value : '20'},
                                   {label : '30', value : '30'},
                                   {label : '40', value : '40'},
                                   {label : '50', value : '50'},
                                   {label : '100', value : '100'});
                            } else {
                                var op = values['pageSizeSelectorOptions'].split(",");
                                for (var i in op) {
                                    if (!isNaN(op[i])) {
                                        options.push({label : op[i]+"", value : op[i]+""});
                                    }
                                }
                            }
                            return options;
                        },
                        options_callback_on_change : "pageSizeSelectorOptions"
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.order'),
                        name  : 'order',
                        required : 'false',
                        type : 'selectbox',
                        options : [
                            {label : '', value : ''},
                            {label : get_cbuilder_msg('dbuilder.order.asc'), value : '2'},
                            {label : get_cbuilder_msg('dbuilder.order.desc'), value : '1'}
                        ]
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.orderBy'),
                        name  : 'orderBy',
                        required : 'false',
                        type : 'selectbox',
                        options_callback : 'DatalistBuilder.getColumnOptions'
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.description'),
                        name  : 'description',
                        required : 'false',
                        type : 'textarea'
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.useSession'),
                        name  : 'useSession',
                        type : 'checkbox',
                        options : [
                            {label : '', value : 'true'}
                        ]
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.showDataWhenFilterSet'),
                        name  : 'showDataWhenFilterSet',
                        type : 'checkbox',
                        options : [
                            {label : '', value : 'true'}
                        ]
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.considerFilterWhenGetTotal'),
                        name  : 'considerFilterWhenGetTotal',
                        type : 'checkbox',
                        options : [
                            {label : '', value : 'true'}
                        ]
                    },
                    {
                        label: get_cbuilder_msg('dbuilder.responsive'),
                        name: 'responsive',
                        type: 'header',
                        description: get_cbuilder_msg('dbuilder.responsive.desc')
                    },
                    {
                        label: get_cbuilder_msg('dbuilder.searchPopup'),
                        name: 'searchPopup',
                        type: 'checkbox',
                        options : [
                            {value : 'true', label : ''}
                        ]
                    },
                    {
                        label: get_cbuilder_msg('dbuilder.disableResponsive'),
                        name: 'disableResponsive',
                        type: 'checkbox',
                        options : [
                            {value : 'true', label : ''}
                        ]
                    }, 
                    {
                        label: get_cbuilder_msg('dbuilder.responsiveLayout'),
                        name: 'responsive_layout',
                        type: 'selectbox',
                        options : [
                            {value : '', label : get_cbuilder_msg('dbuilder.classicDropdown')},
                            {value : 'card-layout', label : get_cbuilder_msg('dbuilder.card')}
                        ],
                        value: 'card-layout',
                        control_field: 'disableResponsive',
                        control_value: '',
                        control_use_regex: 'false'
                    },
                    {
                        label: get_cbuilder_msg('dbuilder.responsiveView'),
                        name: 'responsiveView',
                        description : get_cbuilder_msg('dbuilder.responsiveView.desc'),
                        type: 'gridfixedrow',
                        columns : [
                            {key : 'view', label : get_cbuilder_msg('dbuilder.view')},
                            {key : 'breakpoint', label : get_cbuilder_msg('dbuilder.breakpoint'), type : 'number'},
                            {key : 'columns', label : get_cbuilder_msg('dbuilder.columns')}
                        ],
                        rows: [
                            {label : get_cbuilder_msg('dbuilder.mobile')},
                            {label : get_cbuilder_msg('dbuilder.tablet')}
                        ],
                        control_field: 'responsive_layout',
                        control_value: '',
                        control_use_regex: 'false'
                    }, 
                    {
                        label: get_cbuilder_msg('dbuilder.displayAsCard'),
                        name: 'card_layout_display',
                        type: 'checkbox',
                        options : [
                            {value : 'lg-card', label : get_cbuilder_msg('cbuilder.desktop')},
                            {value : 'md-card', label : get_cbuilder_msg('cbuilder.tablet')},
                            {value : 'sm-card', label : get_cbuilder_msg('cbuilder.mobile')}
                        ],
                        value: 'sm-card',
                        control_field: 'responsive_layout',
                        control_value: 'card-layout',
                        control_use_regex: 'false'
                    }, 
                    {
                        label: get_cbuilder_msg('dbuilder.showHeaderAsLabel'),
                        name: 'card_layout_label',
                        type: 'checkbox',
                        options : [
                            {value : 'card-label', label : ''}
                        ],
                        control_field: 'responsive_layout',
                        control_value: 'card-layout',
                        control_use_regex: 'false'
                    }
                ]
            }
        ];
    },
    
    /*
     * Get properties definition for filter
     */
    getFilterPropertiesDefinition : function () {
        return [{
            title : get_cbuilder_msg('dbuilder.general'),
            helplink : get_cbuilder_msg('dbuilder.filter.helplink'),
            properties :[
            {
                label : 'ID',
                name  : 'id',
                required : 'true',
                type : 'hidden'
            },
            {
                label : 'datalist_type',
                name  : 'datalist_type',
                type : 'hidden',
                value : 'filter'
            },
            {
                label : get_cbuilder_msg('dbuilder.name'),
                name  : 'name',
                type : 'label'
            },
            {
                label : get_cbuilder_msg('dbuilder.filterParam'),
                name  : 'filterParamName',
                type : 'label'
            },
            {
                label : get_cbuilder_msg('dbuilder.label'),
                name  : 'label',
                required : 'true',
                type : 'textfield'
            },
            {
                name : 'type',
                label : get_cbuilder_msg('dbuilder.type'),
                type : 'elementselect',
                value : 'org.joget.apps.datalist.lib.TextFieldDataListFilterType',
                options_ajax : '[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.datalist.model.DataListFilterType',
                url : '[CONTEXT_PATH]/web/property/json' + CustomBuilder.appPath + '/getPropertyOptions'
            },
            {
                label : get_cbuilder_msg('dbuilder.hideFilter'),
                name  : 'hidden',
                type : 'checkbox',
                options : [{label : '', value : 'true'}]
            }]
        }];
    },
    
    /*
     * Get properties definition for column
     */
    getColumnPropertiesDefinition : function() {
        return [{
            title : get_cbuilder_msg('dbuilder.general'),
            helplink : get_cbuilder_msg('dbuilder.column.helplink'),
            properties :[
            {
                label : 'ID',
                name  : 'id',
                required : 'true',
                type : 'hidden'
            },
            {
                label : 'datalist_type',
                name  : 'datalist_type',
                type : 'hidden',
                value : 'column'
            },
            {
                label : get_cbuilder_msg('dbuilder.name'),
                name  : 'name',
                type : 'label'
            },
            {
                label : get_cbuilder_msg('dbuilder.label'),
                name  : 'label',
                required : 'true',
                type : 'textfield'
            },
            {
                label : get_cbuilder_msg('dbuilder.sortable'),
                name  : 'sortable',
                required : 'true',
                type : 'selectbox',
                options : [{
                    label : get_cbuilder_msg('dbuilder.sortable.no'),
                    value : 'false'
                },
                {
                    label : get_cbuilder_msg('dbuilder.sortable.yes'),
                    value : 'true'
                }]
            },
            {
                label : get_cbuilder_msg('dbuilder.export.renderHtml'),
                name  : 'renderHtml',
                type : 'selectbox',
                options : [{
                    label : get_cbuilder_msg('dbuilder.pageSize.default'),
                    value : ''
                },{
                    label : get_cbuilder_msg('dbuilder.hidden.no'),
                    value : 'false'
                },
                {
                    label : get_cbuilder_msg('dbuilder.hidden.yes'),
                    value : 'true'
                }]
            },
            {
                label : get_cbuilder_msg('dbuilder.hidden'),
                name  : 'hidden',
                required : 'true',
                type : 'selectbox',
                options : [{
                    label : get_cbuilder_msg('dbuilder.hidden.no'),
                    value : 'false'
                },
                {
                    label : get_cbuilder_msg('dbuilder.hidden.yes'),
                    value : 'true'
                }]
            },
            {
                label : get_cbuilder_msg('dbuilder.export.include'),
                name  : 'include_export',
                type : 'checkbox',
                control_field: 'hidden',
                control_value: 'true',
                control_use_regex: 'false',
                options : [{
                    label : '',
                    value : 'true'
                }]
            },
            {
                label : get_cbuilder_msg('dbuilder.export.exclude'),
                name  : 'exclude_export',
                type : 'checkbox',
                control_field: 'hidden',
                control_value: 'false',
                control_use_regex: 'false',
                options : [{
                    label : '',
                    value : 'true'
                }]
            },
            {
                label : get_cbuilder_msg('dbuilder.width'),
                name  : 'width',
                type : 'textfield'
            },
            {
                label : get_cbuilder_msg('dbuilder.style'),
                name  : 'style',
                type : 'textfield'
            },
            {
                label : get_cbuilder_msg('dbuilder.alignment'),
                name  : 'alignment',
                type : 'selectbox',
                value : '',
                options : [{
                    label : get_cbuilder_msg('dbuilder.pageSize.default'),
                    value : ''
                },{
                    label : get_cbuilder_msg('dbuilder.align.center'),
                    value : 'dataListAlignCenter'
                },{
                    label : get_cbuilder_msg('dbuilder.align.left'),
                    value : 'dataListAlignLeft'
                },
                {
                    label : get_cbuilder_msg('dbuilder.align.right'),
                    value : 'dataListAlignRight'
                }]
            },
            {
                label : get_cbuilder_msg('dbuilder.headerAlignment'),
                name  : 'headerAlignment',
                type : 'selectbox',
                value : '',
                options : [{
                    label : get_cbuilder_msg('dbuilder.pageSize.default'),
                    value : ''
                },{
                    label : get_cbuilder_msg('dbuilder.align.center'),
                    value : 'dataListAlignCenter'
                },{
                    label : get_cbuilder_msg('dbuilder.align.left'),
                    value : 'dataListAlignLeft'
                },
                {
                    label : get_cbuilder_msg('dbuilder.align.right'),
                    value : 'dataListAlignRight'
                }]
            }]
        },{
            title : get_cbuilder_msg('dbuilder.actionMapping'),
            properties : [
            {
                name : 'action',
                label : get_cbuilder_msg('dbuilder.action'),
                type : 'elementselect',
                options_callback : function(props, values) {
                    var options = [{label : '', value : ''}];
                    var actions = DatalistBuilder.availableActions;
                    for(var e in actions){
                        var action = actions[e];
                        if (action.supportColumn) {
                            options.push({label : UI.escapeHTML(action.label), value : action.className});
                        }
                    }
        
                    return options;
                },
                url : '[CONTEXT_PATH]/web/property/json' + CustomBuilder.appPath + '/getPropertyOptions'
            }]
        },{
            title : get_cbuilder_msg('dbuilder.formatter'),
            properties :[
            {
                name : 'format',
                label : get_cbuilder_msg('dbuilder.formatter'),
                type : 'elementselect',
                options_ajax : '[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.datalist.model.DataListColumnFormat',
                url : '[CONTEXT_PATH]/web/property/json' + CustomBuilder.appPath + '/getPropertyOptions'
            }]
        }];
    },

    /*
     * Get properties definition for action
     */
    getActionPropertiesDefinition : function(propertyOptions, component) {
        if (component.actionPropertyOptions === undefined) {
            component.actionPropertyOptions = $.extend(true, [], propertyOptions);
            
            //change label to icon text field
            var found = false;
            for (var i in component.actionPropertyOptions) {
                for (var r in component.actionPropertyOptions[i].properties) {
                    if (component.actionPropertyOptions[i].properties[r].name === "label") {
                        component.actionPropertyOptions[i].properties[r].type = "icon-textfield";
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            
            component.actionPropertyOptions[0]['properties'].push(
                {
                    label : 'datalist_type',
                    name  : 'datalist_type',
                    type : 'hidden',
                    value : 'action'
                }
            ); 
        }
        
        return component.actionPropertyOptions;
    },
    
    /*
     * Get properties definition for row action
     */
    getRowActionPropertiesDefinition : function(propertyOptions, component) {
        if (component.rowActionPropertyOptions === undefined) {
            component.rowActionPropertyOptions = $.extend(true, [], propertyOptions);
            
            //change label to icon text field
            var found = false;
            for (var i in component.rowActionPropertyOptions) {
                for (var r in component.rowActionPropertyOptions[i].properties) {
                    if (component.rowActionPropertyOptions[i].properties[r].name === "label") {
                        component.rowActionPropertyOptions[i].properties[r].type = "icon-textfield";
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            
            component.rowActionPropertyOptions.push({
                title : get_cbuilder_msg('dbuilder.rowAction.visibility'),
                properties : [
                    {
                        label : 'datalist_type',
                        name  : 'datalist_type',
                        type : 'hidden',
                        value : 'row_action'
                    },
                    {
                    name : 'rules',
                    label : get_cbuilder_msg('dbuilder.rowAction.rules'),
                    type : 'grid',
                    columns : [{
                        key : 'join',
                        label : get_cbuilder_msg('dbuilder.rowAction.join'),
                        options : [{
                            value : 'AND',
                            label : get_cbuilder_msg('dbuilder.rowAction.join.and')
                        },
                        {
                            value : 'OR',
                            label : get_cbuilder_msg('dbuilder.rowAction.join.or')
                        }]
                    },
                    {
                        key : 'field',
                        label : get_cbuilder_msg('dbuilder.rowAction.field'),
                        options_extra : [
                            {value : '', label : ''},
                            {value : '(', label : '('},
                            {value : ')', label : ')'}
                        ],
                        type : 'autocomplete',
                        options_callback : 'DatalistBuilder.getColumnOptions',
                        required: 'true'
                    },
                    {
                        key : 'operator',
                        label : get_cbuilder_msg('dbuilder.rowAction.operator'),
                        options : [{
                            value : '=',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.equal')
                        },
                        {
                            value : '<>',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.notEqual')
                        },
                        {
                            value : '>',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.greaterThan')
                        },
                        {
                            value : '>=',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.greaterThanOrEqual')
                        },
                        {
                            value : '<',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.lessThan')
                        },
                        {
                            value : '<=',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.lessThanOrEqual')
                        },
                        {
                            value : 'LIKE',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.like')
                        },
                        {
                            value : 'NOT LIKE',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.notLike')
                        },
                        {
                            value : 'IN',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.in')
                        },
                        {
                            value : 'NOT IN',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.notIn')
                        },
                        {
                            value : 'IS TRUE',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.isTrue')
                        },
                        {
                            value : 'IS FALSE',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.isFalse')
                        },
                        {
                            value : 'IS EMPTY',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.isEmpty')
                        },
                        {
                            value : 'IS NOT EMPTY',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.isNotEmpty')
                        },
                        {
                            value : 'REGEX',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.regex')
                        },
                        {
                            value : 'NOT REGEX',
                            label : get_cbuilder_msg('dbuilder.rowAction.operator.notRegex')
                        }]
                    },
                    {
                        key : 'value',
                        label : get_cbuilder_msg('dbuilder.rowAction.value')
                    }]
                }]
            }, {
                title : get_cbuilder_msg('dbuilder.others'),
                properties : [
                    {
                        label : get_cbuilder_msg('dbuilder.headerLabel'),
                        name  : 'header_label',
                        type : 'textfield'
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.displayType'),
                        name  : 'link-css-display-type',
                        type : 'selectbox',
                        value : 'btn btn-sm btn-primary',
                        options : [{
                            label : get_cbuilder_msg('dbuilder.btn.link'),
                            value : 'btn btn-link'
                        },
                        {
                            label : get_cbuilder_msg('dbuilder.btn.primary'),
                            value : 'btn btn-sm btn-primary'
                        },
                        {
                            label : get_cbuilder_msg('dbuilder.btn.secondary'),
                            value : 'btn btn-sm btn-secondary'
                        },
                        {
                            label : get_cbuilder_msg('dbuilder.btn.success'),
                            value : 'btn btn-sm btn-success'
                        },
                        {
                            label : get_cbuilder_msg('dbuilder.btn.danger'),
                            value : 'btn btn-sm btn-danger'
                        },
                        {
                            label : get_cbuilder_msg('dbuilder.btn.warning'),
                            value : 'btn btn-sm btn-warning'
                        },
                        {
                            label : get_cbuilder_msg('dbuilder.btn.info'),
                            value : 'btn btn-sm btn-info'
                        },
                        {
                            label : get_cbuilder_msg('dbuilder.btn.light'),
                            value : 'btn btn-sm btn-light'
                        },
                        {
                            label : get_cbuilder_msg('dbuilder.btn.dark'),
                            value : 'btn btn-sm btn-dark'
                        }]
                    }
                ]
            });
        }
        
        return component.rowActionPropertyOptions;
    },
    
    /*
     * used to render tree viewer
     */
    renderTreeMenuAdditionalNode : function(container, target) {
        var type = "";
        if (target.is("[data-cbuilder-filters]") & target.find("[data-cbuilder-classname]").length > 0) {
            type = "filters";
        } else if (target.is("[data-cbuilder-columns]") & target.find("[data-cbuilder-classname]").length > 0) {
            type = "columns";
        } else if (target.is("[data-cbuilder-rowActions]") & target.find("[data-cbuilder-classname]").length > 0) {
            type = "rowActions";
        } else if (target.is("[data-cbuilder-actions]") & target.find("[data-cbuilder-classname]").length > 0) {
            type = "actions";
        }
        
        if (type !== "") {
            var rid = "r" + (new Date().getTime());
            var label = get_cbuilder_msg("dbuilder.type." + type);
            var li = $('<li class="tree-viewer-node"><label>'+label +'</label><input type="checkbox" id="'+rid+'" checked/></li>');
            
            if (target.is("[data-cbuilder-rowActions]")) {
                $(".tree-container > ol").append(li);
            } else {
                container.find("> ol").append(li);
            }
            container = li;
        }
        
        return container;
    },
    
    /*
     * used to prepare properties definition for action
     */
    datalistStylePropertiesDefinition : function(type) {
        var self = DatalistBuilder;
        
        if (self[type + 'StylePropertiesDefinitionObj'] === undefined) {
            self[type + 'StylePropertiesDefinitionObj'] = [];
            
            var orig = CustomBuilder.Builder.stylePropertiesDefinition();
            
            for (var i in orig) {
                if (type === "column" || type === "rowaction") {
                    var header = $.extend(true, {}, orig[i]);
                    header.title = header.title + " ("+get_cbuilder_msg('dbuilder.header')+")";
                    for (var j in header.properties) {
                        if (header.properties[j].name) {
                            header.properties[j].name = header.properties[j].name.replace('style', type+'-header-style');
                        }
                    }
                    self[type + 'StylePropertiesDefinitionObj'].push(header);

                    var tablebody = $.extend(true, {}, orig[i]);
                    tablebody.title = tablebody.title + " ("+get_cbuilder_msg('dbuilder.body')+")";
                    for (var j in tablebody.properties) {
                        if (tablebody.properties[j].name) {
                            tablebody.properties[j].name = tablebody.properties[j].name.replace('style', type+'-style');
                        }
                    }
                    self[type + 'StylePropertiesDefinitionObj'].push(tablebody);
                } else {
                    var header = $.extend(true, {}, orig[i]);
                    for (var j in header.properties) {
                        if (header.properties[j].name) {
                            header.properties[j].name = header.properties[j].name.replace('style', type+'-style');
                        }
                    }

                    self[type + 'StylePropertiesDefinitionObj'].push(header);
                }
            }
        }
        
        return self[type + 'StylePropertiesDefinitionObj'];
    },
    
    /*
     * used to prepare properties definition for table header/body
     */
    tableStylePropertiesDefinition : function() {
        var self = DatalistBuilder;
        
        if (self.tableStylePropertiesDefinitionObj === undefined) {
            self.tableStylePropertiesDefinitionObj = [];
            
            var orig = CustomBuilder.Builder.stylePropertiesDefinition();
            
            for (var i in orig) {
                var header = $.extend(true, {}, orig[i]);
                header.title = header.title + " (Header)";
                for (var j in header.properties) {
                    if (header.properties[j].name) {
                        header.properties[j].name = header.properties[j].name.replace('style', 'header-style');
                    }
                }

                self.tableStylePropertiesDefinitionObj.push(header);
                
                var tablebody = $.extend(true, {}, orig[i]);
                tablebody.title = tablebody.title + " (Body)";
                self.tableStylePropertiesDefinitionObj.push(tablebody);
            }
        }
        
        return self.tableStylePropertiesDefinitionObj;
    },
    
    /*
     * Update card layout based on properties
     */
    cardLayoutResponsive : function(table) {
        var self = CustomBuilder.Builder;
        var width = $(self.iframe.contentWindow).width();
        table.closest(".dataList").removeClass("card-layout-active");
        table.find("thead [data-cbuilder-invisible]").removeAttr("data-cbuilder-invisible");
        table.find("tbody tr").removeAttr("data-cbuilder-columns_filters");
        table.find("tbody tr").removeAttr("data-cbuilder-columns");
        table.find("tbody tr").removeAttr("data-cbuilder-prepend");
        table.find("tbody tr").removeAttr("data-cbuilder-alternative-drop");
        table.find("tbody tr").removeAttr("data-cbuilder-select");
        table.find("tbody tr .row_action_container").removeAttr("data-cbuilder-all_actions");
        table.find("tbody tr .row_action_container").removeAttr("data-cbuilder-rowactions");
        table.find("tbody tr .row_action_container").removeAttr("data-cbuilder-alternative-drop");
        table.find("tbody tr .row_action_container").removeAttr("data-cbuilder-sort-horizontal");
        table.find("tbody tr .row_action_container div").each(function(){
           $(this).css("width", $(this).data("builder-width")); 
           $(this).data("builder-width", "");
        });
        
        if ((width < 768 && table.closest(".dataList").hasClass("sm-card")) 
                || (width < 992 && table.closest(".dataList").hasClass("md-card"))
                || (table.closest(".dataList").hasClass("lg-card"))) {
            table.closest(".dataList").addClass("card-layout-active");
            
            table.find("tbody tr").attr("data-cbuilder-columns_filters", "");
            table.find("tbody tr").attr("data-cbuilder-columns", "");
            table.find("tbody tr").attr("data-cbuilder-prepend", "");
            table.find("tbody tr").attr("data-cbuilder-alternative-drop", "");
            table.find("tbody tr").attr("data-cbuilder-select", CustomBuilder.data.id);
            table.find("tbody tr .row_action_container").attr("data-cbuilder-all_actions", "");
            table.find("tbody tr .row_action_container").attr("data-cbuilder-rowactions", "");
            table.find("tbody tr .row_action_container").attr("data-cbuilder-alternative-drop", "");
            table.find("tbody tr .row_action_container").attr("data-cbuilder-sort-horizontal", "");
            table.find("tbody tr .row_action_container div").each(function(){
                var cssWidth = $(this).css("width");
                if (cssWidth !== "") {
                    $(this).data("builder-width", cssWidth); 
                }
                $(this).css("width", ""); 
            });
        }
    },
    
    /*
     * Remove footable attributes/styles/classes
     */
    destroyFootable : function(table) {
        if ($(table).hasClass("footable")) {
            $(table).off("footable_breakpoint");
            $(table).parent().find(".footable-buttons").hide();
            $(table).parent().parent().find(".filters").show();
            $(table).removeClass("footable-loaded footable phone tablet breakpoint default");
            $(table).find(".footable-row-detail, .footable-toggle").remove();
            $(table).find("tr").removeClass("footable-detail-show");
            $(table).find('th,td').removeClass("footable-visible footable-first-column footable-last-column")
                    .data("hide", "")
                    .show();
            $(table).find("tr").removeData("detail_created");
            $(table).unbind("footable_initialize");
            $(table).unbind("footable_resize");
            $(table).unbind("footable_redraw");
            $(table).unbind("footable_toggle_row");
            $(table).unbind("footable_expand_first_row");
            $(table).unbind("footable_expand_all");
            $(table).unbind("footable_collapse_all");
        }
    },
    
    /*
     * Refresh table layout based on Properties
     */
    refreshTableLayout : function() {
        var self = CustomBuilder.Builder;
        var table = self.frameBody.find(".dataList table");
        if (self.iframe.contentWindow.footable) {
            if (table.length > 0) {
                DatalistBuilder.destroyFootable(table);
                table.closest(".dataList").removeClass("card-layout lg-card md-card sm-card card-label card-layout-active");
                table.find(".card_layout_body_cell").remove();
                $(self.iframe.contentWindow).off("resize.card-responsive");
                
                if (CustomBuilder.data.disableResponsive !== "true") {
                    if (CustomBuilder.data.responsive_layout !== undefined && CustomBuilder.data.responsive_layout !== "") {
                        var css = CustomBuilder.getPropString(CustomBuilder.data.responsive_layout);
                        
                        var viewport = CustomBuilder.getPropString(CustomBuilder.data.card_layout_display).split(";");
                        css += " " + viewport.join(" ");
                        table.closest(".dataList").addClass(css);
                        
                        if (CustomBuilder.data.card_layout_label === "card-label") {
                            table.closest(".dataList").addClass("card-label");
                            self.iframe.contentWindow.initCardLabelLayout(table);
                        }
                        
                        $(self.iframe.contentWindow).on("resize.card-responsive", function(){
                            DatalistBuilder.cardLayoutResponsive(table);
                        });
                        DatalistBuilder.cardLayoutResponsive(table);
                    } else {
                        self.iframe.contentWindow.initFooTable(table, self.frameBody.find(".dataList .footable-buttons"), CustomBuilder.data.responsiveView, true);
                    }
                }
                $(self.iframe.contentWindow).trigger("resize");
            }
        } else {
            setTimeout(function(){
                DatalistBuilder.refreshTableLayout();
            }, 100);
        }
    },
    
    /*
     * Utility method to get all column id and label to populate field selection
     */
    getColumnOptions : function() {
        //populate list items
        var tempArray = [{'label':'','value':''}];
        for(var ee in DatalistBuilder.availableColumns){
            var temp = {'label' : UI.escapeHTML(DatalistBuilder.availableColumns[ee].label),
                         'value' : DatalistBuilder.availableColumns[ee].id};
            tempArray.push(temp);
        }
        return tempArray;
    },
      
    /*
     * remove dynamically added items    
     */            
    unloadBuilder : function() {
        $("#binder-btn").remove();
    } 
}