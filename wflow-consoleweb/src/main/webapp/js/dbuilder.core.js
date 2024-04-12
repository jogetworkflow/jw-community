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
    
    chosenColumns : new Array(),
    chosenActions : new Array(),
    chosenRowActions : new Array(),
    chosenFilters : new Array(),

    availableDisplayColumns : {},
    availableActions : {},
    availableFilters : {},
    availableFormatters : {},
    availableColumns : null,
    template : "",
    defaultTemplate : '<style>body{overflow: visible;min-width:fit-content;}.dataList{min-width: max-content;}</style>\
                                        <table class="xrounded_shadowed responsivetable defaulttemplate expandfirst">\
                                            <thead>\
                                                {{columns data-cbuilder-sort-horizontal data-cbuilder-prepend data-cbuilder-style="[{\'class\' : \'td\', \'label\' : \'Body\'}, {\'prefix\' : \'header\', \'class\' : \'th\', \'label\' : \'Header\'}]"}}\
                                                    <tr>\
                                                        {{column}}\
                                                            <th>{{label||Sample Label}}<span class="overlay"></span></th>\
                                                        {{column}}\
                                                        <th class="gap"></th>\
                                                        {{rowActions data-cbuilder-sort-horizontal data-cbuilder-style="[{\'class\' : \'.rowAction_body\', \'label\' : \'Body\'}, {\'prefix\' : \'header\', \'class\' : \'.rowAction_header\', \'label\' : \'Header\'}, {\'prefix\' : \'link\', \'class\' : \'.rowAction_body > a\', \'label\' : \'Link\'}]"}}\
                                                            <th>\
                                                                {{rowAction}}\
                                                                    <div class="rowAction rowAction_header" data-cbuilder-visible>{{header_label|| }}<span class="overlay"></span></div>\
                                                                {{rowAction}}\
                                                            </th>\
                                                        {{rowActions}}\
                                                    </tr>\
                                                {{columns}}\
                                            </thead>\
                                            <tbody>\
                                                {{rows data-cbuilder-sync}}\
                                                    {{columns data-cbuilder-sync}}\
                                                        <tr>\
                                                            {{column}}\
                                                                <td>{{body||Sample Value}}</td>\
                                                            {{column}}\
                                                            <td class="gap"></td>\
                                                            {{rowActions data-cbuilder-sync}}\
                                                                <td>\
                                                                    {{rowAction}}<div class="rowAction rowAction_body">{{body}}</div>{{rowAction}}\
                                                                </td>\
                                                            {{rowActions}}\
                                                        </tr>\
                                                    {{columns}}\
                                                {{rows}}\
                                            </tbody>\
                                        </table>',

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
                "decorateBoxActions" : "DatalistBuilder.decorateBoxActions",
                "pasteElement" : "DatalistBuilder.pasteElement",
                "renderXray" : "DatalistBuilder.renderXray",
                "parseDataToComponent" : "DatalistBuilder.parseDataToComponent",
                "renderTreeMenuAdditionalNode" : "DatalistBuilder.renderTreeMenuAdditionalNode"
            }
        }, function() {
            CustomBuilder.Builder.setHead('<link data-datalist-style href="' + CustomBuilder.contextPath + '/css/datalist8.css" rel="stylesheet" />');
            if (CustomBuilder.systemTheme === undefined) {
                CustomBuilder.systemTheme = $('body').attr("builder-theme");
            }
            if (CustomBuilder.systemTheme === 'dark') {
                CustomBuilder.Builder.setHead('<link data-userview-style href="' + CustomBuilder.contextPath + '/css/darkTheme.css" rel="stylesheet" />');
            }
            CustomBuilder.Builder.setHead('<link data-userview-style href="' + CustomBuilder.contextPath + '/css/userview8.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<link data-dbuilder-style href="' + CustomBuilder.contextPath + '/css/dbuilder.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<script data-responsive-script src="' + CustomBuilder.contextPath + '/js/footable/responsiveTable.js"/>');
            
            var deferreds = [];
        
            CustomBuilder.createPaletteCategory(get_cbuilder_msg('dbuilder.columnsFilters'));
            DatalistBuilder.initDatalistComponent();
            DatalistBuilder.initActionList(deferreds);
            DatalistBuilder.initFilterList(deferreds);
            DatalistBuilder.initFormatterList(deferreds);
            
            $(CustomBuilder.Builder.iframe).off("change.builder, resize.builder");
            $(CustomBuilder.Builder.iframe).on("change.builder, resize.builder", function(){
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
        
        if(mode === DatalistBuilder.UPDATE){
            if (CustomBuilder.data['pageSizeSelectorOptions'] === undefined) {
                CustomBuilder.data['pageSizeSelectorOptions'] = "10,20,30,40,50,100";
            }
        }
        
        wait.resolve();
        
        $.when.apply($, deferreds).then(function() {
            CustomBuilder.update();
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
     * check to ignore rendering an element for permission view
     */
    isPermissionIgnoreRendering : function(data) {
        return false;
    },
    
    /*
     * render headers for permission
     */
    renderPermissionElements: function (tbody, key) {
        //set header for web & export
        var thead = $(tbody).closest("table").find("thead tr");
        
        var isTemplate = CustomBuilder.Builder.frameBody.find("table.defaulttemplate").length > 0;
        
        if ($(thead).find('.export').length === 0) {
            $(thead).append('<th class="export" width="30%">'+get_advtool_msg('adv.permission.export')+'</th>');
            $(thead).find(".authorized").text(get_advtool_msg('adv.permission.web'));
        }
        
        if (isTemplate) {
            if (CustomBuilder.data.columns !== undefined && CustomBuilder.data.columns.length > 0) {
                $(tbody).append('<tr class="header"><td>'+get_cbuilder_msg("dbuilder.type.columns")+'</td><td class="authorized"></td><td class="export"></td></tr>');

                var childs = CustomBuilder.data.columns;
                if (childs !== null && childs !== undefined && childs.length > 0) {
                    $.each(childs, function(i, child){
                        PermissionManager.renderElement(child, tbody, key);
                    });
                }
            }
            if (CustomBuilder.data.rowActions !== undefined && CustomBuilder.data.rowActions.length > 0) {
                $(tbody).append('<tr class="header"><td>'+get_cbuilder_msg("dbuilder.type.rowActions")+'</td><td class="authorized"></td><td style="background:#fff;"></td></tr>');

                var childs = CustomBuilder.data.rowActions;
                if (childs !== null && childs !== undefined && childs.length > 0) {
                    $.each(childs, function(i, child){
                        PermissionManager.renderElement(child, tbody, key);
                    });
                }
            }
        } else {
            CustomBuilder.Builder.frameBody.find(".dataList [data-placeholder-key]:not([data-cbuilder-sync])").each(function(){
                if ($(this).closest("[data-cbuilder-replicate]").length === 0) {
                    var pkey = $(this).attr("data-placeholder-key");
                    var childs = CustomBuilder.data[pkey];
                    if (childs !== null && childs !== undefined && childs.length > 0) {
                        var label = "";
                        if (pkey === "columns" || pkey === "rowActions") {
                            label = get_cbuilder_msg("dbuilder.type."+pkey);
                        } else {
                            label = $(this).attr("data-cbuilder-droparea-msg");
                        }
                        
                        if (pkey.indexOf("column") === 0) {
                            $(tbody).append('<tr class="header"><td>'+label+'</td><td class="authorized"></td><td class="export"></td></tr>');
                        } else {
                            $(tbody).append('<tr class="header"><td>'+label+'</td><td class="authorized"></td><td style="background:#fff;"></td></tr>');
                        }
                        
                        
                        $.each(childs, function(i, child){
                            PermissionManager.renderElement(child, tbody, key);
                        });
                    }
                }
            });
        }
        if (CustomBuilder.data.filters !== undefined && CustomBuilder.data.filters.length > 0) {
            $(tbody).append('<tr class="header"><td>'+get_cbuilder_msg("dbuilder.type.filters")+'</td><td class="authorized"></td><td style="background:#fff;"></td></tr>');
            
            var childs = CustomBuilder.data.filters;
            if (childs !== null && childs !== undefined && childs.length > 0) {
                $.each(childs, function(i, child){
                    PermissionManager.renderElement(child, tbody, key);
                });
            }
        }
        if (CustomBuilder.data.actions !== undefined && CustomBuilder.data.actions.length > 0) {
            $(tbody).append('<tr class="header"><td>'+get_cbuilder_msg("dbuilder.type.actions")+'</td><td class="authorized"></td><td style="background:#fff;"></td></tr>');
            
            var childs = CustomBuilder.data.actions;
            if (childs !== null && childs !== undefined && childs.length > 0) {
                $.each(childs, function(i, child){
                    PermissionManager.renderElement(child, tbody, key);
                });
            }
        }
        
    },
    
    /*
     * Load and render data, called from CustomBuilder.loadJson
     */
    load: function (data) {
        $("body").addClass("frame_loading");
        
        if (data.binder === undefined || data.binder.className === undefined || data.binder.className === "") {
            setTimeout(function(){
                $("#binder-btn").trigger("click");
            }, 1);
            return;
        }
        var deferreds = [];
        
        DatalistBuilder.retrieveColumns(deferreds);  
        DatalistBuilder.retrieveTemplate(CustomBuilder.data, deferreds);  
        
        var self = CustomBuilder.Builder;
            
        var selectedELSelector = "";
        var selectedElIndex = 0;
        
        if (CustomBuilder.overviewPath !== null && CustomBuilder.overviewPath !== undefined && CustomBuilder.overviewPath !== "") {
            //handle overview tool navigation path 
            [selectedELSelector, CustomBuilder.overviewPropertiesPath] = DatalistBuilder.getOverviewPathElementSelector(data, CustomBuilder.overviewPath);
            CustomBuilder.overviewPath = null;
        } else if (self.subSelectedEl) {
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
            
            var cdeferreds = [];
            var d = $.Deferred();
            cdeferreds.push(d);
            
            var html = '<div class="dataList" style="display:block !important;" data-cbuilder-uneditable data-cbuilder-classname="org.joget.apps.datalist.model.DataList" >\
                            <form class="filter_form"><div class="filters" data-cbuilder-columns_filters data-cbuilder-filters data-cbuilder-sort-horizontal data-cbuilder-droparea-msg="'+get_cbuilder_msg('dbuilder.dragFiltersHere')+'"></div></form>\
                            <form><div class="table-wrapper">'+DatalistBuilder.convertTemplate(DatalistBuilder.template, CustomBuilder.data)+'</div>\
                                <div class="actions bottom left" data-cbuilder-all_actions data-cbuilder-actions  data-cbuilder-sort-horizontal data-cbuilder-droparea-msg="'+get_cbuilder_msg('dbuilder.dragActionsHere')+'"></div>\
                            </form>\
                        </div>';
            
            self.frameBody.append(html);
            self.frameBody.find(".dataList").data("data", CustomBuilder.data);
            self.frameBody.find(".dataList").attr("data-cbuilder-id", CustomBuilder.data.id);
            self.frameBody.find(".dataList").attr("id", CustomBuilder.data.id);
            
            self.frameBody.find(".dataList [data-placeholder-key]:not([data-cbuilder-sync])").each(function(){
                if ($(this).closest("[data-cbuilder-replicate]").length === 0) {
                    var key = $(this).attr("data-placeholder-key");
                    var objs = CustomBuilder.data[key];
                    if (objs !== null && objs !== undefined) {
                        for (var i in objs) {
                            var data = objs[i];
                            var component = self.parseDataToComponent(data);
                            
                            var temp = $('<span></span>');
                            if ($(this).is("tr")) {
                                temp = $('<td></td>');
                            } else if ($(this).is("ul") || $(this).is("ol")) {
                                temp = $('<li></li>');
                            }
                            
                            if ($(this).find('> [data-cbuilder-sample]').length > 0) {
                                $(this).find('> [data-cbuilder-sample]').before(temp);
                            } else {
                                $(this).append(temp);
                            }
                            
                            var syncElements = [];
                            var se = $(temp).clone();
                            self.frameBody.find(".dataList [data-placeholder-key=\""+key+"\"][data-cbuilder-sync]").each(function(){
                                var seTemp = $(se).clone();
                                syncElements.push(seTemp);
                                if ($(this).find('> [data-cbuilder-sample]').length > 0) {
                                    $(this).find('> [data-cbuilder-sample]').before(seTemp);
                                } else {
                                    $(this).append(seTemp);
                                }
                            });
                            if (syncElements.length > 0) {
                                $(temp).data("syncElements", syncElements);
                            }
                            
                            self.renderElement(data, temp, component, null, cdeferreds);
                        }
                    }
                }
            });
            
            for (var i in CustomBuilder.data.filters) {
                var data = CustomBuilder.data.filters[i];
                
                var component = self.parseDataToComponent(data);
                var temp = $('<span></span>');
                self.frameBody.find(".filters").append(temp);
                self.renderElement(data, temp, component, null, cdeferreds);
            }
            for (var i in CustomBuilder.data.actions) {
                var data = CustomBuilder.data.actions[i];
                
                var component = self.parseDataToComponent(data);
                var temp = $('<span></span>');
                self.frameBody.find("div.actions").append(temp);
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
                        //set highlighted El for the element like list row or card which only exist for styling
                        CustomBuilder.Builder.highlightEl = element;
                        
                        self.selectNode(element);
                    }
                }
                $("#iframe-wrapper").show();
                
                DatalistBuilder.afterUpdate(CustomBuilder.data);
                
                $("body").removeClass("frame_loading");
            });
        });
    },
    
    convertTemplate : function(html, data) {
        html = html.replace(/\{\{contextPath\}\}/g, CustomBuilder.contextPath);
        html = html.replace(/~\{~\{/g, "{{");
        html = html.replace(/~\}~\}/g, "}}");
        html = DatalistBuilder.fillTemplateProps(html, data);
        html = DatalistBuilder.fillPlaceholders(html, data);
        return html;
    },
    
    fillTemplateProps : function(html, data) {
        var newHtml = html;
        if (data.template !== undefined && data.template !== null) {
            var regexp = (/\{\{([a-zA-Z0-9-_]+)\}\}/gm);
            while ((match = regexp.exec(html)) !== null) {
                var replace = match[0];
                if (replace !== "" && replace !== undefined) {
                    var key = match[1];
                    var value = null;
                    if (data.template !== undefined && data.template !== null && data.template.properties[key] !== undefined && data.template.properties[key] !== null) {
                        value = data.template.properties[key];
                    }
                    if (value !== null && value !== undefined) {
                        newHtml = newHtml.replace(replace, value);
                    }
                } else {
                    break;
                }
            }
        }
        return newHtml;
    },
    
    fillPlaceholders : function(html, data) {
        var regexp = (/\{\{([a-zA-Z0-9-_]+)(| .+?|\|\|.+?)\}\}([\s\S]+?)\{\{\1\}\}/gm);
        var newHtml = html;
        while ((match = regexp.exec(html)) !== null) {
            var replace = match[0];
            if (replace !== "" && replace !== undefined) {
                var key = match[1];
                var props = match[2];
                var childtemplate = match[3];
                
                var result = DatalistBuilder.fillPlaceholder(childtemplate, data, key, props);
                if (result !== null) {
                    newHtml = newHtml.replace(replace, result);
                }
            } else {
                break;
            }
        }
        
        regexp = (/\{\{([a-zA-Z0-9-_]+)(| .+?|\|\|.+?)\}\}/gm);
        html = newHtml;
        while ((match = regexp.exec(html)) !== null) {
            var replace = match[0];
            if (replace !== "" && replace !== undefined) {
                var key = match[1];
                var props = match[2];
                
                var result = DatalistBuilder.fillPlaceholder("", data, key, props);
                if (result !== null) {
                    newHtml = newHtml.replace(replace, result);
                }
            } else {
                break;
            }
        }
        return newHtml;
    },
    
    fillPlaceholder: function(html, data, key, props) {
        var sample = null;
        if (props.indexOf("||") !== -1) {
            var temp = props.split("||");
            props = temp[0];
            sample = temp[1];
        }
        var result = null;
        if (key === "columns" || key.indexOf("column_") === 0 || key === "rowActions" || key.indexOf("rowAction_") === 0) {
            if (key === "rowActions" && props.indexOf("data-cbuilder-sync") === -1) {
                props += " data-cbuilder-all_actions data-cbuilder-rowActions data-cbuilder-droparea-msg=\""+get_cbuilder_msg('dbuilder.dragRowActionsHere')+"\"";
            } else if (key.indexOf("rowAction_") === 0 && props.indexOf("data-cbuilder-sync") === -1) {
                props += " data-cbuilder-all_actions data-cbuilder-rowActions";
                if (props.indexOf("data-cbuilder-multiple") === -1) {
                    props += " data-cbuilder-single";
                }
            } else if (key === "columns" && props.indexOf("data-cbuilder-sync") === -1) {
                props += " data-cbuilder-columns_filters data-cbuilder-columns data-cbuilder-droparea-msg=\""+get_cbuilder_msg('dbuilder.dragColumnsHere')+"\"";
            } else if (key.indexOf("column_") === 0 && props.indexOf("data-cbuilder-sync") === -1) {
                props += " data-cbuilder-columns_filters data-cbuilder-columns";
                if (props.indexOf("data-cbuilder-multiple") === -1) {
                    props += " data-cbuilder-single";
                }
            }
            
            //check has child key
            var innerkey = key.substring(0, key.length -1); 
            if (html.indexOf("{{"+innerkey+"}}") !== -1 || html.indexOf("{{"+innerkey+" ") !== -1 || html.indexOf("{{"+innerkey+"||") !== -1) {
                var childTemplate = null;
                var childProps = null;
                var replace = null;
                var regexp = (/\{\{([a-zA-Z0-9-_]+)(| .+?|\|\|.+?)\}\}([\s\S]+?)\{\{\1\}\}/gm);
                while ((match = regexp.exec(html)) !== null) {
                    replace = match[0];
                    if (replace !== "" && replace !== undefined) {
                        if (match[1] === innerkey) { 
                            childProps = match[2];
                            childTemplate = match[3];
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (childTemplate === null) {
                    regexp = (/\{\{([a-zA-Z0-9-_]+)(| .+?|\|\|.+?)\}\}/gm);
                    while ((match = regexp.exec(html)) !== null) {
                        replace = match[0];
                        if (replace !== "" && replace !== undefined) {
                            if (match[1] === innerkey) {
                                childProps = match[2];
                                childTemplate = "{{body}}";
                            }
                        } else {
                            break;
                        }
                    }
                }
                var newHtml = html.trim();
                
                var tags = ["span", "th", "td", "li"];
                for (var t in tags) {
                    var temp = newHtml.replace(replace, '<'+tags[t]+' class="childplaceholder"></'+tags[t]+'>');
                    if ($(temp).is(".childplaceholder") || $(temp).find(".childplaceholder").length > 0) {
                        newHtml = temp;
                        break;
                    }
                }
                newHtml = $(DatalistBuilder.fillPlaceholders(newHtml));
                
                var childTemp = $(newHtml);
                var temp = $('<div></div>');
                if ($(childTemp).is("tr")) {
                    temp = $('<table></table>');
                } else if ($(childTemp).is("td") || $(childTemp).is("th")) {
                    temp = $('<tr></tr>');
                } else if ($(childTemp).is("li")) {
                    temp = $('<ul></ul>');
                }
                
                var placeholder;
                if ($(childTemp).hasClass("childplaceholder")) {
                    $(temp).append('<div class="'+key+'" data-placeholder-key="'+key+'" '+props+'></div>');
                    placeholder = $(temp).find("> ."+key);
                    $(placeholder).append($(childTemp));
                } else {
                    $(temp).append(childTemp);
                    placeholder = $(temp).find(".childplaceholder").parent();
                    $(placeholder).addClass(key);
                    $(placeholder).attr("data-placeholder-key", key);
                     
                    var regexp = (/(([a-zA-Z0-9-_]+)="(.+?)"|[a-zA-Z0-9-_]+)/gm);
                    while ((match = regexp.exec(props)) !== null) {
                        var attr = match[0];
                        if (attr !== "" && attr !== undefined) {
                            if (attr.indexOf("=") !== -1) { 
                                $(placeholder).attr(match[2], match[3]);
                            } else {
                                $(placeholder).attr(attr, "");
                            }
                        }
                    }
                }
                
                //render sample
                $(placeholder).attr("data-placeholder-template", DatalistBuilder.escapePlaceholder(childTemplate));
                if (childTemplate !== "{{body}}") {
                    var sample = $(DatalistBuilder.fillPlaceholders(childTemplate));
                    $(sample).attr("data-cbuilder-sample", "");
                    $(temp).find('.childplaceholder').before($(sample));
                }
                $(temp).find('.childplaceholder').remove();
                
                result = $(temp).html();
            } else if (html.trim().length > 0) {
                var childTemplate = html;
                var temp = $('<div><div class="'+key+'" data-placeholder-key="'+key+'" '+props+'></div></div>');
                var placeholder = $(temp).find("> ."+key);
                $(placeholder).attr("data-placeholder-template", DatalistBuilder.escapePlaceholder(childTemplate));
                var sample = $(DatalistBuilder.fillPlaceholders(childTemplate));
                $(sample).attr("data-cbuilder-sample", "");
                $(placeholder).append($(sample));
                
                result = $(temp).html();
            } else {
                if (sample !== null) {
                    var temp = $('<div>'+sample+'</div>');
                    $(temp).find("> *").attr("data-cbuilder-sample", "");
                    sample = $(temp).html();
                } else {
                    sample = "";
                }
                result = '<div class="'+key+'" data-placeholder-key="'+key+'" '+props+' data-placeholder-template="'+DatalistBuilder.escapePlaceholder('{{body}}')+'">'+sample+'</div>';
            }
        } else if (key === "rows") {
            var html = DatalistBuilder.fillPlaceholders(html);
            
            var childTemp = $(html);
            var clone = $('<div></div>');
            if ($(childTemp).is("tr")) {
                clone = $('<table></table>');
            } else if ($(childTemp).is("td") || $(childTemp).is("th")) {
                clone = $('<tr></tr>');
            } else if ($(childTemp).is("li")) {
                clone = $('<ul></ul>');
            }
            $(clone).append(childTemp);
            if (props.indexOf('data-cbuilder-sync') === -1) {
                $(childTemp).attr("data-cbuilder-replicate-origin", "rows");
            }
            if (props.indexOf('data-cbuilder-style') !== -1) {
                $(childTemp).attr("data-cbuilder-select", CustomBuilder.data.id);
            }
            var regexp = (/(([a-zA-Z0-9-_]+)="(.+?)"|[a-zA-Z0-9-_]+)/gm);
            while ((match = regexp.exec(props)) !== null) {
                var attr = match[0];
                if (attr !== "" && attr !== undefined) {
                    if (attr.indexOf("=") !== -1) { 
                        $(childTemp).attr(match[2], match[3]);
                    } else {
                        $(childTemp).attr(attr, "");
                    }
                }
            }
            result = $(clone).html();
            var copyTemp = $(html);
            $(clone).html('');
            $(clone).append(copyTemp);
            for (var i = 0; i < 4; i++) {
                if (props.indexOf('data-cbuilder-sync') === -1) {
                    $(copyTemp).attr("data-cbuilder-replicate", "rows");
                }
                result += $(clone).html();
            }
        } else if (key === "selector"){
            result = "";
        } else {
            if (sample !== "") {
                result = sample;
            }
            if (data !== undefined && data[key] !== undefined && data[key] !== null) {
                result = data[key];
            }
            if (result === null) {
                result = "";
            }
        }
        
        return result;
    },
    
    escapePlaceholder : function(html) {
        html = html.replace(/\{\{/g, "~{~{");
        html = html.replace(/\}\}/g, "~}~}");
        return html;
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
     * Retrive template syntax based on the template property
     */
    retrieveTemplate : function(data, deferreds) {
        if (data.template !== undefined && data.template !== null && data.template.className !== undefined && data.template.className !== "") {
            var wait = $.Deferred();
            deferreds.push(wait);
            
            CustomBuilder.cachedAjax({
                type: "POST",
                data: {
                    "json": JSON.encode(data.template),
                    "listId" : CustomBuilder.data.id
                },
                url: CustomBuilder.contextPath + '/web/dbuilder/getRenderingTemplate',
                dataType : "text",
                beforeSend: function (request) {
                    request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                },
                success: function(response) {
                    DatalistBuilder.template = response;
                },
                error: function() {
                    //ignore
                },
                complete: function() {
                    wait.resolve();
                }
            });
        } else {
            data.template = {
                className : '',
                properties : {}
            };
            DatalistBuilder.template = DatalistBuilder.defaultTemplate;
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
                        CustomBuilder.showMessage(get_cbuilder_msg('dbuilder.errorRetrieveColumns'), "danger", true);
                    }
                    DatalistBuilder.retrieveColumnsCallback({
                        sample : DatalistBuilder.sampleData,
                        columns : []
                    }, wait);
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
        
        //clear missing columns
        for (var i in CustomBuilder.paletteElements) {
            if (CustomBuilder.paletteElements[i].isMissingColumn !== undefined) {
                delete CustomBuilder.paletteElements[i];
            }
        }
        
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
            
            DatalistBuilder.initColumn(column, false);
        }
        DatalistBuilder.availableColumns = fields;
        
        var change = false;
        
        //handle non exist columns and filters
        var missingColumns = [];
        var i = CustomBuilder.data.filters.length;
        while (i--) {
            var filter = CustomBuilder.data.filters[i];
            if (DatalistBuilder.availableColumns[filter.name] === undefined) { 
                if ($.inArray(filter.name, missingColumns) === -1) {
                    missingColumns.push(filter.name);
                }
            } 
        }
        var i = CustomBuilder.data.columns.length;
        while (i--) {
            var column = CustomBuilder.data.columns[i];
            if (column.name !== undefined && DatalistBuilder.availableColumns[column.name] === undefined) { 
                if ($.inArray(column.name, missingColumns) === -1) {
                    missingColumns.push(column.name);
                }
            } 
        }
        
        if (missingColumns.length > 0) {
            for(var e in missingColumns){
                var column = {
                    id : missingColumns[e],
                    name : missingColumns[e],
                    label : get_cbuilder_msg('cbuilder.missing') + " ("+missingColumns[e]+")",
                    displayLabel : get_cbuilder_msg('cbuilder.missing') + " ("+missingColumns[e]+")",
                    filterable : false
                };

                DatalistBuilder.initColumn(column, true);
            }
        }
        
        //handle order by
        if (CustomBuilder.data.orderBy !== undefined && CustomBuilder.data.orderBy !== "") {
            if (DatalistBuilder.availableColumns[CustomBuilder.data.orderBy] === undefined) { 
                CustomBuilder.data.orderBy = "";
                change = true;
            }
        }
        
        if (change) {
            CustomBuilder.update(false);
        }
        
        deferrer.resolve();
    },
    
    initColumn: function(column, isMissing) {
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
                    var self = CustomBuilder.Builder;
                    var parent = null;
                    if (self.dragElement) {
                        parent = self.dragElement.parent();
                    } else if (self.selectedEl) {
                        parent = self.selectedEl.parent();
                    }
                    if (parent !== null && $(parent).is("[data-placeholder-key]") && $(parent).attr('data-placeholder-key').indexOf('column_') === 0) {
                        return $(parent).attr('data-placeholder-key');
                    } else {
                        if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) !== -1) {
                            return "filters";
                        } else {
                            return "columns";
                        }
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
                    if ($(dragElement).parent().is("[data-placeholder-key]")) {
                        dragElement = DatalistBuilder.draggingElement(dragElement, component);
                    } else if (!$(dragElement).is("span.filter-cell")) { //is filter
                        var replace = $('<span class="filter-cell "><input type="text" size="10" placeholder="'+UI.escapeHTML(component.label)+'"/></span></div>');
                        dragElement.replaceWith(replace);
                        dragElement = replace;

                        CustomBuilder.Builder.frameBody.find("[data-cbuilder-dragSubElement]").remove();
                    }
                    return dragElement;
                },
                'afterMoved' : function(element, elementObj, component) {
                    var syncElements = $(element).data("syncElements");
                    if ($(element).parent().is("[data-placeholder-key]") 
                            && syncElements !== undefined && syncElements !== null && syncElements.length > 0) {
                        DatalistBuilder.syncElements(element, elementObj, component, syncElements);
                    }
                },
                'customPropertiesData' : function(props, elementObj, component) {
                    if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) === 0) {
                        props.datalist_type = 'filter';
                    } else {
                        props.datalist_type = 'column';
                    }
                    return props;
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
                        return DatalistBuilder.getElementStylePropertiesDefinition(elementObj, component);
                    }
                },
                'isPastable' : function(elementObj, component){
                    var copied = CustomBuilder.getCopiedElement();
                    if (copied !== null && copied !== undefined) {
                        return true;
                    }
                    return false;
                },
                'renderPermission' : function(row, elementObj, permissionObj, key, level) {
                    if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) !== -1) {
                        PermissionManager.renderElementDefault(elementObj, row, permissionObj, key, level);
                    } else {
                        DatalistBuilder.renderColumnPermission(elementObj, row, permissionObj, key, level);
                    }
                },
                'navigable' : false,
                'dragHtml' : '<span></span>'
            }
        };

        if (isMissing) {
            CustomBuilder.Builder.frameBody.find("[data-cbuilder-classname='"+column.name+"']").attr("data-cbuilder-missing-plugin", "");
            
            meta['builderTemplate']['render'] = function(element, elementObj, component, callback) {
                var newcallback = function(element) {
                    $(element).attr("data-cbuilder-missing-plugin", "");
                    callback(element);
                };
                if (CustomBuilder.Builder.options.callbacks["renderElement"] !== undefined && CustomBuilder.Builder.options.callbacks["renderElement"] !== "") {
                    CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["renderElement"], [element, elementObj, component, newcallback]);
                } else if (callback) {
                    newcallback(element);
                }
            };
            meta['builderTemplate']['isPastable'] = function(elementObj, component){
                return false;
            };
            meta['builderTemplate']['getLabel'] = function(elementObj, component) {
                return component.label;
            };
            meta['builderTemplate']['draggable'] = false;
            meta['builderTemplate']['movable'] = false;
            meta['builderTemplate']['deletable'] = true;
            meta['builderTemplate']['copyable'] = false;
            meta['builderTemplate']['navigable'] = false;
            meta["isMissingColumn"] = true;

            CustomBuilder.initPaletteElement("", column.name, column.displayLabel, "", "", "", false, "", meta);
        } else {
            //populate palette
            CustomBuilder.initPaletteElement(get_cbuilder_msg('dbuilder.columnsFilters'), column.name, column.displayLabel, '<i class="far fa-hdd"></i>', {}, {}, true, cssClass, meta);
        }
    },
    
    /*
     * Initialize builder component for datalist
     */
    initDatalistComponent: function() {
        var meta = {
            builderTemplate : {
                'getStylePropertiesDefinition' : function(elementObj, component) {
                    var selectedEl = CustomBuilder.Builder.selectedEl;
                    if ($(selectedEl).is(".filter-cell")) {
                        return component.builderTemplate.filterStylePropertiesDefinition;
                    } else if ($(selectedEl).is(".btn")) {
                        return component.builderTemplate.actionStylePropertiesDefinition;
                    } else if ($(CustomBuilder.Builder.highlightEl).is("[data-cbuilder-style]") || $(selectedEl).parent().is("[data-placeholder-key]")) {
                        var styleConfig = null;
                        if ($(selectedEl).parent().is("[data-cbuilder-style]")) {
                            try {
                                styleConfig = eval($(selectedEl).parent().attr('data-cbuilder-style'));
                            } catch (err){}
                        } else if ($(CustomBuilder.Builder.highlightEl).is("[data-cbuilder-style]")) {
                            try {
                                styleConfig = eval($(CustomBuilder.Builder.highlightEl).attr('data-cbuilder-style'));
                            } catch (err){}
                        }
                        var key = "";
                        var prefix = "";
                        if (!$(selectedEl).parent().is("[data-placeholder-key]")) {
                            for (var i in styleConfig) {
                                if (styleConfig[i].prefix !== undefined && styleConfig[i].prefix !== null && styleConfig[i].prefix !== "") {
                                    key += styleConfig[i].prefix;
                                }
                            }
                        } else {
                            key = $(selectedEl).parent().attr("data-placeholder-key");
                            prefix = key;
                        }
                        if (CustomBuilder.data.template && CustomBuilder.data.template.className) {
                            currentTemplate = CustomBuilder.data.template.className;
                        }
                        const builderTemplateKey = key + "StylePropertiesDefinition" + currentTemplate;
                        if (component.builderTemplate[builderTemplateKey] === undefined) {
                            component.builderTemplate[builderTemplateKey] = $.extend(true, [], DatalistBuilder.generateStylePropertiesDefinition(prefix, styleConfig));
                        }
                        return component.builderTemplate[builderTemplateKey];
                    }
                },
                'getLabel' : function(elementObj, component) {
                    if (CustomBuilder.Builder.highlightEl !== null && $(CustomBuilder.Builder.highlightEl).is("[data-cbuilder-highlight]")) {
                        return $(CustomBuilder.Builder.highlightEl).attr('data-cbuilder-highlight');
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
                'filterStylePropertiesDefinition' : $.extend(true, [], DatalistBuilder.generateStylePropertiesDefinition("filter")),
                'actionStylePropertiesDefinition' : $.extend(true, [], DatalistBuilder.generateStylePropertiesDefinition("action")),
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
                
                if (returnedData.displayColumns) {
                    for(var e in returnedData.displayColumns){
                        var column = returnedData.displayColumns[e];
                        
                        DatalistBuilder.availableDisplayColumns[column.className] = column;
                        var icon = column.icon;
                        if (icon === undefined || icon === null && icon === "") {
                            icon = '<i class="fas fa-info"></i>';
                        }
                        
                        var meta = {
                            isDisplayColumn : true,
                            builderTemplate : {
                                'customPropertyOptions' : function(elementOptions, element, elementObj, paletteElement) {
                                    return DatalistBuilder.getDisplayColumnPropertiesDefinition(elementOptions);
                                },
                                'dragging' : function(dragElement, component) {
                                    dragElement = DatalistBuilder.draggingElement(dragElement, component);
                                    return dragElement;
                                },
                                'afterMoved' : function(element, elementObj, component) {
                                    var syncElements = $(element).data("syncElements");
                                    if ($(element).parent().is("[data-placeholder-key]") 
                                            && syncElements !== undefined && syncElements !== null && syncElements.length > 0) {
                                        DatalistBuilder.syncElements(element, elementObj, component, syncElements);
                                    }
                                },
                                'getStylePropertiesDefinition' : function(elementObj, component) {
                                    return DatalistBuilder.getElementStylePropertiesDefinition(elementObj, component);
                                },
                                'renderPermission' : function(row, elementObj, permissionObj, key, level) {
                                    DatalistBuilder.renderColumnPermission(elementObj, row, permissionObj, key, level);
                                },
                                'parentContainerAttr' : 'columns',
                                'parentDataHolder' : 'columns',
                                'navigable' : false,
                                'dragHtml' : '<span></span>'
                            }
                        };
                        
                        if (column.jscomponent !== undefined && column.jscomponent !== "" && column.jscomponent !== null) {
                            try {
                                var temp = eval('[' + column.jscomponent + ']')[0];  
                                meta.builderTemplate = $.extend(true, meta.builderTemplate, temp);
                            } catch (err){
                                //ignore
                            }
                        }

                        CustomBuilder.initPaletteElement(get_cbuilder_msg('dbuilder.displayColumns'), column.className, column.label, icon, column.propertyOptions, column.defaultPropertyValues, true, "", meta);
                    }
                }
                
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
                        developer_mode : action.developerMode,
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
                                var self = CustomBuilder.Builder;
                                var parent = null;
                                if (self.dragElement) {
                                    parent = self.dragElement.parent();
                                } else if (self.selectedEl) {
                                    parent = self.selectedEl.parent();
                                }
                                if (parent !== null && $(parent).is("[data-placeholder-key]") && $(parent).attr('data-placeholder-key').indexOf('rowAction_') === 0) {
                                    return $(parent).attr('data-placeholder-key');
                                } else {
                                    if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) !== -1) {
                                        return "rowActions";
                                    } else {
                                        return "actions";
                                    }
                                }
                            },
                            'dragging' : function(dragElement, component) {
                                if ($(dragElement).parent().is("[data-placeholder-key]")) {
                                    dragElement = DatalistBuilder.draggingElement(dragElement, component);
                                } else if (!$(dragElement).is("button")) { //is action
                                    var replace = $('<button class="form-button btn button">'+UI.escapeHTML(component.label)+'</button>');
                                    dragElement.replaceWith(replace);
                                    dragElement = replace;

                                    CustomBuilder.Builder.frameBody.find("[data-cbuilder-dragSubElement]").remove();
                                }
                                return dragElement;
                            },
                            'afterMoved' : function(element, elementObj, component) {
                                var syncElements = $(element).data("syncElements");
                                if ($(element).parent().is("[data-placeholder-key]") 
                                        && syncElements !== undefined && syncElements !== null && syncElements.length > 0) {
                                    DatalistBuilder.syncElements(element, elementObj, component, syncElements);
                                }
                            },
                            'customPropertiesData' : function(props, elementObj, component) {
                                if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) === 0) {
                                    props.datalist_type = 'row_action';
                                } else {
                                    props.datalist_type = 'action';
                                }
                                return props;
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
                                    return DatalistBuilder.getElementStylePropertiesDefinition(elementObj, component);
                                } else {
                                    return component.builderTemplate.stylePropertiesDefinition;
                                }
                            },
                            'isSubSelectAllowActions' : function(elementObj, component) {
                                return false;
                            },
                            'isPastable' : function(elementObj, component){
                                var copied = CustomBuilder.getCopiedElement();
                                if (copied !== null && copied !== undefined) {
                                    return true;
                                }
                                return false;
                            },
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
        if ($(dragElement).parent().is('[data-placeholder-key]')) {
            var key = $(dragElement).parent().attr('data-placeholder-key');
            if (CustomBuilder.data[key] === undefined) {
                CustomBuilder.data[key] = [];
            }
            if (key === "rowActions" || key.indexOf("rowAction_") === 0) {
                type = "rowAction";
            } else {
                type = "column";
            }
            parentArray = CustomBuilder.data[key];
        } else if ($(dragElement).parent().is("[data-cbuilder-filters]")) {
            parentArray = CustomBuilder.data.filters;
            type = "filter";
        } else if ($(dragElement).parent().is("[data-cbuilder-actions]")) {
            parentArray = CustomBuilder.data.actions;
            type = "action";
        }
        
        var elementObj = {
            id : DatalistBuilder.getId(type)
        };
        
        if (component.isDisplayColumn) {
            elementObj.className = component.className;
            elementObj.properties = $.extend(true, {}, component.properties);
            elementObj.properties.label = component.label;
            elementObj.properties.id = elementObj.id;
        } else if (type === "filter" || type === "column") {
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
            elementObj.properties = $.extend(true, {}, component.properties);
            elementObj.properties.label = component.label;
            
            if (type === "rowAction") {
                if (component.className === 'org.joget.apps.datalist.lib.FormRowDeleteDataListAction') {
                    elementObj.properties['link-css-display-type'] = 'btn btn-sm btn-danger';
                } else {
                    elementObj.properties['link-css-display-type'] = 'btn btn-sm btn-primary';
                }
            }
        }

        var index = 0;
        var container = $(dragElement).parent().closest("[data-cbuilder-"+component.builderTemplate.getParentContainerAttr(elementObj, component)+"]");
        if ($(container).is('[data-cbuilder-single]')) {
            parentArray.splice(0,parentArray.length, elementObj);
            $(container).find("> [data-cbuilder-classname]").remove();
        } else {
            index = $(container).find("> *").index(dragElement);
            parentArray.splice(index, 0, elementObj);
        }

        callback(elementObj);
    },
    
    draggingElement : function(dragElement, component) {
        var key = $(dragElement).parent().attr('data-placeholder-key');
        if (key === undefined) {
            return dragElement;
        }
        
        var data = component.label;
        var rowData = DatalistBuilder.sampleData;
        if (rowData !== undefined && rowData !== null 
                && rowData[component.className] !== undefined && rowData[component.className] !== null && rowData[component.className] !== "") {
            data = rowData[component.className];
        }

        var id = $(dragElement).attr("data-cbuilder-id");
        var elementObj = $(dragElement).data("data");
        var value = $(dragElement).attr("data-cbuilder-value");
        if (value !== undefined) {
            data = value;
        } else {  //data already having the a/span tag if getting from data-cbuilder-value
            if (key.indexOf("rowAction") === 0) {
                data = '<a href="#">' + data + '</a>';
            } else if (key.indexOf("column") === 0) {
                data = '<span>' + data + '</span>';
            }
        }
        
        var obj;
        if (elementObj !== null && elementObj !== undefined) {
            if (elementObj.propertie !== null && elementObj.propertie !== undefined) {
                obj = $.extend(true, {}, elementObj.propertie);
            } else {
                obj = $.extend(true, {}, elementObj);
            }
            obj['body'] = data;
        } else {
            obj = {
                label : component.label,
                body : data
            };
        }
        
        var html = DatalistBuilder.convertTemplate($(dragElement).parent().attr('data-placeholder-template'), obj);
        var replace = $(html);

        var syncElements = $(dragElement).data("syncElements");
        
        $(dragElement).replaceWith(replace);
        dragElement = replace;
        
        DatalistBuilder.updateStyle(dragElement, obj, component, $(dragElement).parent());
        
        if (CustomBuilder.Builder.frameBody.find(".dataList [data-placeholder-key=\""+key+"\"][data-cbuilder-sync]").length > 0) {
            var index = $(dragElement).parent().find("> *").index($(dragElement));
            if (syncElements === undefined || syncElements === null || syncElements.length === 0) {
                syncElements = [];
                CustomBuilder.Builder.frameBody.find(".dataList [data-placeholder-key=\""+key+"\"][data-cbuilder-sync]").each(function(){
                    var dragHtml = DatalistBuilder.convertTemplate($(this).attr('data-placeholder-template'), obj);
                    var subDragElement = $(dragHtml);
                    $(subDragElement).attr('data-cbuilder-dragSubElement', '');
                    syncElements.push(subDragElement);
                    if (index === 0) {
                        $(this).prepend(subDragElement);
                    } else {
                        $(this).find('> *:eq('+(index-1)+')').after(subDragElement);
                    }
                    
                    if (key.indexOf("rowAction") === 0) {
                        var width = $(dragElement).width();
                        var synceWidth = $(subDragElement).width();
                        if (synceWidth > width) {
                            $(dragElement).width(synceWidth);
                        } else {
                            $(subDragElement).width(width);
                        }
                    }
                    
                    DatalistBuilder.updateStyle(subDragElement, obj, component, $(dragElement).parent());
                });
            } else {
                //move to the index
                for (var i in syncElements) {
                    var cIndex = $(syncElements[i]).parent().find('> *').index($(syncElements[i]));
                    if (index === 0) {
                        $(syncElements[i]).parent().prepend(syncElements[i]);
                    } else if (cIndex < index) {
                        $(syncElements[i]).parent().find('> *:eq('+(index)+')').after(syncElements[i]);
                    } else {
                        $(syncElements[i]).parent().find('> *:eq('+(index-1)+')').after(syncElements[i]);
                    }
                    
                    if (key.indexOf("rowAction") === 0) {
                        var width = $(dragElement).width();
                        var synceWidth = $(syncElements[i]).width();
                        if (synceWidth > width) {
                            $(dragElement).width(synceWidth);
                        } else {
                            $(syncElements[i]).width(width);
                        }
                    }
                    DatalistBuilder.updateStyle(syncElements[i], obj, component, $(dragElement).parent());
                }
            }
        }
        
        $(dragElement).attr("data-cbuilder-id", id);
        $(dragElement).attr("data-cbuilder-label", component.label);
        if (syncElements !== undefined && syncElements !== null && syncElements.length > 0) {
            $(dragElement).data("syncElements", syncElements);
        }
        if (value !== undefined){
            $(dragElement).attr("data-cbuilder-value", value);
        }
        if (elementObj !== undefined) {
            $(dragElement).attr("data-cbuilder-classname", component.className);
            $(dragElement).data("data", elementObj);
            CustomBuilder.Builder.selectedEl = dragElement;
        }
        
        DatalistBuilder.adjustTableOverlaySize($(dragElement).closest(".dataList"));
        
        return dragElement;
    },
    
    /**
     * Sync the table body cells after moving column using left right button
     */
    syncElements : function(element, elementObj, component, syncElements) {
        var key = $(element).parent().attr('data-placeholder-key');
        if (CustomBuilder.Builder.frameBody.find(".dataList [data-placeholder-key=\""+key+"\"][data-cbuilder-sync]").length > 0) {
            var index = $(element).parent().find("> *").index($(element));
            
            //move to the index
            for (var i in syncElements) {
                var cIndex = $(syncElements[i]).parent().find('> *').index($(syncElements[i]));
                if (index === 0) {
                    $(syncElements[i]).parent().prepend(syncElements[i]);
                } else if (cIndex < index) {
                    $(syncElements[i]).parent().find('> *:eq('+(index)+')').after(syncElements[i]);
                } else {
                    $(syncElements[i]).parent().find('> *:eq('+(index-1)+')').after(syncElements[i]);
                }

                if (key.indexOf("rowAction") === 0) {
                    var width = $(element).width();
                    var synceWidth = $(element[i]).width();
                    if (synceWidth > width) {
                        $(element).width(synceWidth);
                    } else {
                        $(syncElements[i]).width(width);
                    }
                }
                DatalistBuilder.updateStyle(syncElements[i], elementObj, component, $(element).parent());
            }
            
            DatalistBuilder.adjustTableOverlaySize($(element).closest(".dataList"));
        }
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
        var deferrer = $.Deferred();
        
        if (elementObj.id.indexOf(DatalistBuilder.columnPrefix) === 0) {
            DatalistBuilder.renderColumn(element, elementObj, component, deferrer);
        } else if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) === 0) {
            DatalistBuilder.renderFilter(element, elementObj, component, deferrer);
        } else if (elementObj.id.indexOf(DatalistBuilder.actionPrefix) === 0) {
            DatalistBuilder.renderAction(element, elementObj, component, deferrer);
        } else if (elementObj.id.indexOf(DatalistBuilder.rowActionPrefix) === 0) {
            DatalistBuilder.renderRowActions(element, elementObj, component, deferrer);
        } else if (component.isDisplayColumn) {
            DatalistBuilder.renderColumn(element, elementObj, component, deferrer);
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
        
        builder.handleStylingProperties(element, elementObj, "filter", ".dataList .filter-cell");
        builder.handleStylingProperties(element, elementObj, "action", ".dataList .actions .btn");
        
        var proceededKey = [];
        $(element).find('[data-placeholder-key]:not([data-cbuilder-sync]), [data-cbuilder-style]').each(function(){
            var key = $(this).attr('data-placeholder-key');
            var checkingKey = key;
            if (checkingKey === undefined && $(this).is('[data-cbuilder-style]')) {
                try {
                    checkingKey = "";
                    var styleConfig = eval($(this).attr('data-cbuilder-style'));
                    for (var i in styleConfig) {
                        if (styleConfig[i].prefix !== undefined) {
                            checkingKey += styleConfig[i].prefix;
                        }
                    }
                } catch (err){}
            }
            if ($.inArray(checkingKey, proceededKey) === -1) {
                var styleConfig = null;
                if ($(this).is("[data-cbuilder-style]")) {
                    try {
                        styleConfig = eval($(this).attr('data-cbuilder-style'));
                    } catch (err){}
                }
                if (styleConfig === null) {
                    styleConfig = [{}];
                }
                for (var i in styleConfig) {
                    var cssStyle = "";
                    var prefix = "";
                    
                    if (key !== undefined) {     
                        prefix = key;
                        cssStyle = ".ph_" + key;
                        if (styleConfig[i].class !== undefined) {
                            if (styleConfig[i].class.indexOf(" ") !== -1) {
                                cssStyle = styleConfig[i].class.replace(" ", cssStyle + " ");
                            } else {
                                cssStyle = styleConfig[i].class + cssStyle;
                            }
                        }
                        if (styleConfig[i].prefix !== undefined) {
                            prefix = key + "-" + styleConfig[i].prefix;
                        }
                    } else {
                        cssStyle = styleConfig[i].class;
                        prefix = styleConfig[i].prefix;
                    }
                    
                    builder.handleStylingProperties(element, elementObj, prefix, ".dataList " + cssStyle, false);
                }
                proceededKey.push(checkingKey);
            }
        });
        
        if (deferrer !== undefined) {
            deferrer.resolve({element : element});
        }
    },
    
    /*
     * Rendering column, call form DatalistBuilder.renderElement
     */
    renderColumn : function(element, elementObj, component, deferrer) {
        var self = CustomBuilder.Builder;
        
        var value = elementObj.label;
        if (component.isDisplayColumn) {
            value = component.label;
        }
        var rowData = DatalistBuilder.sampleData;
        if (rowData !== undefined && rowData !== null) {
            if (rowData[elementObj.name] !== undefined && rowData[elementObj.name] !== null && rowData[elementObj.name] !== "") {
                value = rowData[elementObj.name];
            }
        } else {
            rowData = {};
        }
        
        var formatDeferrer = $.Deferred();
        if ((DatalistBuilder.availableColumns[elementObj.name] !== undefined && elementObj.format !== undefined && elementObj.format.className !== undefined && elementObj.format.className !== "") || component.isDisplayColumn) {
            var colStr = JSON.encode(elementObj);
            var rowStr = JSON.encode(rowData);

            CustomBuilder.cachedAjax({
                type: "POST",
                data: {
                    "column": colStr,
                    "value" : value,
                    "row" : rowStr,
                    "appId" : CustomBuilder.appId,
                    "listId" : CustomBuilder.data.id,
                    "binderJson" : JSON.encode(CustomBuilder.data.binder.properties),
                    "binderId" : CustomBuilder.data.binder.className
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
                        value = formatted.html();
                    }
                },
                error: function() {
                    //ignore
                },
                complete: function() {
                    formatDeferrer.resolve();
                }
            });
        } else {
            formatDeferrer.resolve();
        }

        $.when.apply($, [formatDeferrer]).then(function() {
            var obj = $.extend(true, {}, elementObj);
            if (component.isDisplayColumn) {
                obj = $.extend(true, {}, elementObj.properties);
            } else {
                obj = $.extend(true, {}, elementObj);
            }
            obj.body = '<span>' + value + '</span>';
            var html = DatalistBuilder.convertTemplate($(element).parent().attr('data-placeholder-template'), obj);
            var replace = $(html);
            
            var syncElements = $(element).data("syncElements");
            element.replaceWith(replace);
            element = replace;
            
            DatalistBuilder.updateStyle(element, elementObj, component, $(element).parent());
            
            $(element).attr("data-cbuilder-value", value);
            if (syncElements !== null && syncElements !== undefined) {
                //update sync element
                for (var i in syncElements) {
                    var syncHtml = DatalistBuilder.convertTemplate($(syncElements[i]).parent().attr('data-placeholder-template'), obj);
                    var syncReplace = $(syncHtml);
                    syncElements[i].replaceWith(syncReplace);
                    syncElements[i] = syncReplace;
                    
                    DatalistBuilder.updateStyle(syncElements[i], elementObj, component, $(element).parent());
                }
                $(element).data("syncElements", syncElements);
            }

            //check all attribute of parent
            $.each($(element).parent()[0].attributes, function() {
                if(this.specified && this.name.indexOf("attr-") === 0) {
                    var n = this.name.substring(5);
                    var orgAttr = $(element).attr(n);
                    if (orgAttr !== undefined) {
                        orgAttr += " " + this.value;
                    } else {
                        orgAttr = this.value;
                    }
                    $(element).attr(n, orgAttr);
                }
            });
            
            DatalistBuilder.adjustTableOverlaySize($(element).closest(".dataList"));
            
            deferrer.resolve({element : element});  
        });
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
        var self = CustomBuilder.Builder;
        
        var obj;
        if (elementObj.properties !== null && elementObj.properties !== undefined) {
            obj = $.extend(true, {}, elementObj.properties);
        } else {
            obj = $.extend(true, {}, elementObj);
        }
        var value = '<a href="#">' + obj.label + '</a>';
        
        obj.body = value;
        
        var html = DatalistBuilder.convertTemplate($(element).parent().attr('data-placeholder-template'), obj);
        var replace = $(html);

        var syncElements = $(element).data("syncElements");
        element.replaceWith(replace);
        element = replace;
        
        DatalistBuilder.updateStyle(element, elementObj, component, $(element).parent());
        
        var width = $(element).width();

        $(element).attr("data-cbuilder-value", value);
        if (syncElements !== null && syncElements !== undefined) {
            //update sync element
            for (var i in syncElements) {
                var syncHtml = DatalistBuilder.convertTemplate($(syncElements[i]).parent().attr('data-placeholder-template'), obj);
                var syncReplace = $(syncHtml);
                syncElements[i].replaceWith(syncReplace);
                syncElements[i] = syncReplace;
                
                DatalistBuilder.updateStyle(syncElements[i], elementObj, component, $(element).parent());
                
                var synceWidth = $(syncReplace).width();
                if (synceWidth > width) {
                    $(element).width(synceWidth);
                } else {
                    $(syncReplace).width(width);
                }
            }
            $(element).data("syncElements", syncElements);
        }

        //check all attribute of parent
        $.each($(element).parent()[0].attributes, function() {
            if(this.specified && this.name.indexOf("attr-") === 0) {
                var n = this.name.substring(5);
                var orgAttr = $(element).attr(n);
                if (orgAttr !== undefined) {
                    orgAttr += " " + this.value;
                } else {
                    orgAttr = this.value;
                }
                $(element).attr(n, orgAttr);
            }
        });

        DatalistBuilder.adjustTableOverlaySize($(element).closest(".dataList"));

        deferrer.resolve({element : element});
    },
    
    updateStyle : function(element, elementObj, component, parent) {
        var self = CustomBuilder.Builder;
        if (elementObj !== undefined && elementObj.id !== undefined) {
            if ($(parent).is('[data-placeholder-key]')) {
                $(element).addClass("ph_" + $(parent).attr('data-placeholder-key'));
            }
            
            var styleProperties = elementObj;
            if (elementObj.id.indexOf("column") === 0) {
                var style = "";
                if (elementObj.style !== undefined && elementObj.style !== "") {
                    var style = elementObj.style;
                    if (style.substr(style.length - 1) !== ";") {
                        style += ";";
                    }
                }
                
                if (element.is("th")) {
                    element.addClass("column_header column_" + elementObj.name);
                    
                    if (elementObj.sortable === "true") {
                        element.addClass("sortable");
                        var overlay = $(element).find(".overlay");
                        element.html('<a href="#">'+elementObj.label+'</a>');
                        if (overlay.length > 0) {
                            element.append(overlay);
                        }
                    }
                    if (elementObj.headerAlignment !== undefined && elementObj.headerAlignment !== "") {
                        element.addClass(elementObj.headerAlignment);
                    }
                    if (elementObj.width !== undefined && elementObj.width !== "") {
                        element.css("width", elementObj.width);
                    }
                    if (elementObj.hidden !== undefined && elementObj.hidden === "true") {
                        if (element.find(".overlay").length > 0) {
                            element.find(".overlay").attr("data-cbuilder-element-invisible", "");
                        } else {
                            element.attr("data-cbuilder-element-invisible", "");
                        }
                    }
                } else {
                    element.addClass("column_body column_" + elementObj.name);
                    if (elementObj.alignment !== undefined && elementObj.alignment !== "") {
                        element.addClass(elementObj.alignment);
                    }
                    if (elementObj.width !== undefined && elementObj.width !== "") {
                        element.css("width", elementObj.width);
                    }
                    if (style !== "") {
                        var orgStyle = element.attr("style");
                        if (orgStyle === undefined) {
                            orgStyle = "";
                        }
                        element.attr("style", style + orgStyle);
                    }
                    if (elementObj.hidden !== undefined && elementObj.hidden === "true") {
                        element.attr("data-cbuilder-element-invisible", "");
                    }
                }
            } else {
                styleProperties = elementObj.properties;
            }
            
            var styleConfig = null;
            if ($(parent).is("[data-cbuilder-style]")) {
                try {
                    styleConfig = eval($(parent).attr('data-cbuilder-style'));
                } catch (err){}
            }
            if (styleConfig === null) {
                styleConfig = [{}];
                if (elementObj.id.indexOf("rowAction") === 0) {
                    styleConfig.push({
                        class : 'a',
                        prefix : 'link'
                    });
                }
            }
            for (var i in styleConfig) {
                var styleElement = null;
                if (styleConfig[i].class !== undefined) {
                    if ($(element).is(styleConfig[i].class)) {
                        styleElement = $(element);
                    } else if (styleConfig[i].class.indexOf(" ") !== -1) {
                        if ($(element).find(styleConfig[i].class.substring(styleConfig[i].class.indexOf(" ") + 1)).length > 0) {
                            styleElement = $(element).find(styleConfig[i].class.substring(styleConfig[i].class.indexOf(" ") + 1));
                        } else if ($(element).find(styleConfig[i].class).length > 0) {
                            styleElement = $(element).find(styleConfig[i].class);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else {
                    styleElement = $(element);
                }
                self.handleStylingProperties(styleElement, styleProperties, styleConfig[i].prefix);
            }
        }
    },
    
    /*
     * Used to render the invibility flag overlay
     */
    adjustTableOverlaySize : function(datalist) {
        var table = $(datalist).find("table");
        if ($(table).length > 0) {
            var height = $(table).height();
            var thHeight = $(table).find("thead").height();
            var bottom = height - thHeight;
            $(table).find("thead th > .overlay").each(function(){
                $(this).css("bottom", "-" + bottom + "px");
            });
            $(table).find("thead th.rowActions > div > .overlay").each(function(){
                $(this).css("top", "-10px");
                $(this).css("bottom", "-" + (bottom + 13) + "px");
            });
        }
    },
    
    /*
     * A callback method called from the default component.builderTemplate.unload method
     */
    unloadElement : function(element, elementObj, component) {
        var syncElements = $(element).data("syncElements");
        if (syncElements !== null && syncElements !== undefined) {
            for (var i in syncElements) {
                $(syncElements[i]).remove();
            }
        }
    },
    
    /*
     * A callback method called from the default component.builderTemplate.decorateBoxActions method.
     * It used to add the syling options for all elements
     */
    decorateBoxActions : function(element, elementObj, component, box) {
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
        
        if ($(element).is("[data-cbuilder-missing-plugin]")) {
            return;
        }
        
        if (type !== "") {
            $(box).find(".element-options").append('<a class="default-style-btn" title="'+get_cbuilder_msg('style.defaultStyles')+'" style=""><i class="las la-palette"></i></a>');
            $(box).find(".default-style-btn").off("click");
            $(box).find(".default-style-btn").on("click", function(event){
                builder.boxActionSetElement(event);
                
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
        
        if (copiedObj.id.indexOf(DatalistBuilder.columnPrefix) === 0 || copiedObj.id.indexOf(DatalistBuilder.rowActionPrefix) === 0) {
            var parent = builder.frameBody.find(".dataList");
            var parentDataHolder = component.builderTemplate.getParentDataHolder(elementObj, component)
            if (component.builderTemplate.getParentContainerAttr(elementObj, component) !== copiedComponent.builderTemplate.getParentContainerAttr(copiedObj, copiedComponent)) {
                parentDataHolder = copiedComponent.builderTemplate.getParentDataHolder(copiedObj, copiedComponent);
            }
            
            var parentDataArray = $(parent).data("data")[parentDataHolder];
            var container = builder.frameBody.find(".dataList").find('[data-placeholder-key="'+parentDataHolder+'"]:not([data-cbuilder-sync])');
            if ($(container).length > 1) {
                container = $(container[0]);
            }
            var temp = $('<div></div>');
            if ($(container).is('tr')) {
                temp = $('<td></td>');
            } else if ($(container).is('ul') || $(container).is('ol')) {
                temp = $('<li></li>');
            }
            if ($(container).is('[data-cbuilder-single]')) {
                parentDataArray.splice(0, parentDataArray.length, copiedObj);
                $(container).find("> [data-cbuilder-classname]").remove();
                $(container).prepend(temp);
            } else {
                var index = $(container).find("> [data-cbuilder-classname]").index(element);
                if (index === -1) {
                    parentDataArray.push(copiedObj);
                    if ($(container).find("> [data-cbuilder-sample]").length > 0) {
                        $(container).find("> [data-cbuilder-sample]").before(temp);
                    }
                } else {
                    parentDataArray.splice(index+1, 0, copiedObj);
                    $(element).after(temp);
                }
            }
            
            var syncContainers = builder.frameBody.find(".dataList [data-placeholder-key=\""+parentDataHolder+"\"][data-cbuilder-sync]");
            if (syncContainers.length > 0) {
                var syncElements = [];
                var syncIndex = $(container).find("> *").index(temp);
                var se = $(temp).clone();
                syncContainers.each(function(){
                    var seTemp = $(se).clone();
                    syncElements.push(seTemp);
                    if (syncIndex === 0) {
                        $(this).prepend(seTemp);
                    } else {
                        $(this).find("> *:eq("+(syncIndex-1)+")").after(seTemp);
                    }
                });
                $(temp).data("syncElements", syncElements);
            }
            
            builder.component = copiedComponent;
            builder.renderElement(copiedObj, temp, copiedComponent, true);
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
        if ((elementObj.id.indexOf(DatalistBuilder.columnPrefix) === 0 || elementObj.id.indexOf(DatalistBuilder.filterPrefix) === 0) && elementObj.name !== undefined) {
            dl.append('<dt><i class="las la-user" title="' + get_cbuilder_msg('cbuilder.name') + '"></i></dt><dd>' + elementObj.name + '</dd>');
        } 
        if (component.isDisplayColumn) {
            dl.append('<dt><i class="las la-user" title="' + get_cbuilder_msg('cbuilder.name') + '"></i></dt><dd>' + elementObj.properties.label + '</dd>');
            
            //to make it consistent hight
            dl.append('<dt>&nbsp;</dt><dd>&nbsp;</dd>');
            dl.append('<dt>&nbsp;</dt><dd>&nbsp;</dd>');
        } else if (elementObj.id.indexOf(DatalistBuilder.columnPrefix) === 0) {
            dl.find('> *:eq(1)').text(elementObj.datalist_type);
            var action = "-";
            if (elementObj.action !== undefined && elementObj.action.className !== undefined && elementObj.action.className !== "") {
                action = elementObj.action.className;
                if (DatalistBuilder.availableActions[action] !== undefined) {
                    action = DatalistBuilder.availableActions[action].label;
                } else {
                    action += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
                    action = '<span class="missing-plugin">' + action + '</span>';
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
                    format = '<span class="missing-plugin">' + format + '</span>';
                }
            }
            dl.append('<dt><i class="las la-paint-brush" title="'+get_cbuilder_msg('dbuilder.formatter')+'"></i></dt><dd>'+format+'</dd>');
        } else if (elementObj.id.indexOf(DatalistBuilder.filterPrefix) === 0) {
            var type = "-";
            if (elementObj.type !== undefined && elementObj.type.className !== undefined && elementObj.type.className !== "") {
                type = elementObj.type.className;
                if (DatalistBuilder.availableFilters[type] !== undefined) {
                    type = DatalistBuilder.availableFilters[type];
                } else {
                    type += " ("+get_advtool_msg('dependency.tree.Missing.Plugin')+")";
                    type = '<span class="missing-plugin">' + type + '</span>';
                }
            }
            dl.find('> *:eq(1)').text(type);
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
            dl.append('<dt><i class="las la-eye" title="'+get_cbuilder_msg('dbuilder.rowAction.visibility')+'"></i></dt><dd>'+fields.join(', ')+'&nbsp;</dd>');
            
            //to make it consistent hight
            dl.append('<dt>&nbsp;</dt><dd>&nbsp;</dd>');
            dl.append('<dt>&nbsp;</dt><dd>&nbsp;</dd>');
        }
        
        callback();
    },
    
    /*
     * A callback method called from the CustomBuilder.Builder.renderNodeAdditional
     * It used to render the permission option of a column
     */
    renderColumnPermission : function (elementObj, row, permissionObj, key, level) {
        $(row).append('<td class="authorized" width="30%"><div class="authorized-btns btn-group"></div></td>');
        $(row).find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm visible-btn" >'+get_cbuilder_msg("ubuilder.visible")+'</button>');
        $(row).find(".authorized-btns").append('<button type="button" class="btn btn-outline-success btn-sm hidden-btn" >'+get_cbuilder_msg("ubuilder.hidden")+'</button>');
        
        $(row).append('<td class="export" width="30%"><div class="authorized-export-btns btn-group"></div></td>');
        $(row).find(".authorized-export-btns").append('<button type="button" class="btn btn-outline-info btn-sm visible-btn" >'+get_cbuilder_msg("ubuilder.visible")+'</button>');
        $(row).find(".authorized-export-btns").append('<button type="button" class="btn btn-outline-info btn-sm hidden-btn" >'+get_cbuilder_msg("ubuilder.hidden")+'</button>');
        
        if (permissionObj["hidden"] === "true") {
            $(row).find(".authorized-btns .hidden-btn").addClass("active");
                
            if (permissionObj["include_export"] === "true") {
                $(row).find(".authorized-export-btns .visible-btn").addClass("active");
            } else {
                $(row).find(".authorized-export-btns .hidden-btn").addClass("active");
            }
        } else {
            $(row).find(".authorized-btns .visible-btn").addClass("active");

            if (permissionObj["exclude_export"] === "true") {
                $(row).find(".authorized-export-btns .hidden-btn").addClass("active");
            } else {
                $(row).find(".authorized-export-btns .visible-btn").addClass("active");
            }
        }
        
        $(row).on("click", ".btn", function(event) {
            if ($(this).hasClass("active")) {
                return false;
            }

            var group = $(this).closest(".btn-group");
            group.find(".active").removeClass("active");
            $(this).addClass("active");

            if ($(row).find(".authorized-btns .hidden-btn").hasClass("active")) {
                permissionObj["hidden"] = "true";
                if ($(row).find(".authorized-export-btns .hidden-btn").hasClass("active")) {
                    permissionObj["include_export"] = "";
                    permissionObj["exclude_export"] = "";
                } else {
                    permissionObj["include_export"] = "true";
                    permissionObj["exclude_export"] = "";
                }
            } else {
                permissionObj["hidden"] = "false";
                if ($(row).find(".authorized-export-btns .hidden-btn").hasClass("active")) {
                    permissionObj["include_export"] = "";
                    permissionObj["exclude_export"] = "true";
                } else {
                    permissionObj["include_export"] = "";
                    permissionObj["exclude_export"] = "";
                }
            }

            CustomBuilder.update();

            event.preventDefault();
            return false;
        });
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
        var templateJson = "";
        const deferreds = [];

        // Parses the template from retrieveTemplateHtml, then toggles the template styles on/off
        const toggleStyleProperties = function (template, styleModifier) {
            // parses template to find style prefixes (data-cbuilder-style), null if not found
            const rowActionMatches = /{{rowActions.*data-cbuilder-style="(\[\{.*}])"}}/.exec(template);

            // check if it has style prefix and JSON-ify, else null
            const templateRowActionStyle = rowActionMatches
                ? JSON.parse(rowActionMatches[1].replaceAll("'", '"'))
                    .filter(function (item) { return item.prefix; })
                : null;

            // run for each item in CustomBuilder.data.rowActions[n].properties
            CustomBuilder.data.rowActions.forEach(function (rowAction) {
                Object.entries(rowAction.properties).forEach(function (prop) {
                    const key = prop[0];
                    const value = prop[1];

                    // check if property begins with style- or prefix-style (regardless if commented)
                    const modifyStyles = function(customRegex) {
                        const customCheck = customRegex
                            ? customRegex.test(key) && key.indexOf("-style-") !== -1
                            : false;
                        const isStyle = /^[-_]?style-/.test(key) || customCheck;
                        if (isStyle) {
                            // put in new key and delete old key
                            const newKey = styleModifier(key);
                            if (newKey) {
                                rowAction.properties[newKey] = value;
                                delete rowAction.properties[key];
                            }
                        }
                    };

                    // conditionally execute depending on templateRowActionStyle
                    if (templateRowActionStyle) {
                        templateRowActionStyle.forEach(function (style) {
                            // check if it has style prefix and test key for prefix
                            const prefixRegex = new RegExp("^[-_]?" + style.prefix);
                            modifyStyles(prefixRegex);
                        });
                    } else {
                        modifyStyles();
                    }
                });
            });
        };

        // Fetches given rendering template, and toggles the styles using toggleStyleProperties
        const retrieveTemplateHtml = function(data, toggleStyle, wait) {
            // special case, if template is default (Table - Classic)
            if (data.template.className === "") {
                return toggleStyleProperties(DatalistBuilder.defaultTemplate, toggleStyle);
            }
            deferreds.push(wait);
            CustomBuilder.cachedAjax({
                type: "POST",
                data: {
                    "json": JSON.encode(data.template),
                    "listId" : data.id
                },
                url: CustomBuilder.contextPath + '/web/dbuilder/getRenderingTemplate',
                dataType : "text",
                beforeSend: function (request) {
                    request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                },
                success: function(response) {
                    toggleStyleProperties(response, toggleStyle);
                },
                error: function() {
                    window.alert("Template loading failed. Please try again.");
                },
                complete: function() {
                    wait.resolve();
                }
            });
        };

        const toggleOnStyle = function(key) { if (key.indexOf("_") === 0) return key.substring(1); };
        const toggleOffStyle = function(key) { if (key[0] !== "_") return "_" + key; };

        // Toggle off old style, then toggle on new style
        const toggleRowActionStyleProperties = function(oldProperties, newProperties) {
            retrieveTemplateHtml(oldProperties, toggleOffStyle, $.Deferred());
            retrieveTemplateHtml(newProperties, toggleOnStyle, $.Deferred());
        };

        const updateBuilder = function() {
            CustomBuilder.data = $.extend(CustomBuilder.data, properties);
            CustomBuilder.update();
            if (templateJson !== JSON.encode(CustomBuilder.data.template)) {
                CustomBuilder.loadJson(CustomBuilder.data, false);
            } else {
                DatalistBuilder.refreshTableLayout();
            }
        };

        if (CustomBuilder.data.template !== undefined && CustomBuilder.data.template !== null) {
            templateJson = JSON.encode(CustomBuilder.data.template);
            
            if (CustomBuilder.data.template.className !== properties.template.className) {
                //change of template, prompt to check for remove custom style
                if (confirm("Detected changing template. Do you want to remove previous custom styling?")) {
                    CustomBuilder.clearCustomStyling(CustomBuilder.data, function(name){
                        return (name.indexOf("-style-") !== -1 && (
                                    name.indexOf("action") === 0 ||
                                    name.indexOf("rowAction") === 0 ||
                                    name.indexOf("column") === 0 ||
                                    name.indexOf("filter") === 0 ||
                                    name.indexOf("card") === 0 ||
                                    name.indexOf("link") === 0 ||
                                    name.indexOf("header") === 0 ||
                                    name.indexOf("list") === 0
                                ));
                    });
                } else {
                    toggleRowActionStyleProperties(CustomBuilder.data, properties);
                }
            }
        }

        // update builder depending if deferreds exist
        if (deferreds.length > 0) {
            $.when.apply($, deferreds).then(function() {
                updateBuilder();
            });
        } else {
            updateBuilder();
        }
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
                        label: get_cbuilder_msg('dbuilder.template'),
                        name: 'template',
                        type: 'elementselect',
                        options_ajax : '[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.datalist.model.DataListTemplate',
                        options_extra : [
                            {value : '', label : get_cbuilder_msg('dbuilder.classicTable')}
                        ],
                        url : '[CONTEXT_PATH]/web/property/json' + CustomBuilder.appPath + '/getPropertyOptions'
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
                        control_use_regex: 'false',
                        developer_mode : 'advanced'
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
                        options_callback_on_change : "pageSizeSelectorOptions",
                        developer_mode : 'advanced'
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
                        ],
                        developer_mode : 'advanced'
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.considerFilterWhenGetTotal'),
                        name  : 'considerFilterWhenGetTotal',
                        type : 'checkbox',
                        options : [
                            {label : '', value : 'true'}
                        ],
                        developer_mode : 'advanced'
                    },
                    {
                        label: get_cbuilder_msg('dbuilder.responsive'),
                        name: 'responsive',
                        type: 'header',
                        description: get_cbuilder_msg('dbuilder.responsive.desc')
                    }, 
                    {
                        label: get_cbuilder_msg('dbuilder.responsiveMode'),
                        name: 'responsiveMode',
                        type: 'selectbox',
                        options : [
                            {value : '', label : get_cbuilder_msg('dbuilder.responsiveFollowWindowWidth')},
                            {value : 'parent', label : get_cbuilder_msg('dbuilder.responsiveFollowParentWidth')}
                        ]
                    }
                ]
            },
            {
                title: get_cbuilder_msg('dbuilder.template') + ' (' + get_cbuilder_msg('dbuilder.classicTable') + ')',
                properties : [
                    {
                        label : get_cbuilder_msg('dbuilder.rowActionsMode'),
                        name  : 'rowActionsMode',
                        type : 'selectbox',
                        options : [
                            {label : get_cbuilder_msg('dbuilder.default'), value : ''},
                            {label : get_cbuilder_msg('dbuilder.rowActionsMode.singlecolumn'), value : 'true'},
                            {label : get_cbuilder_msg('dbuilder.rowActionsMode.dropdown'), value : 'dropdown'}
                        ]
                    },
                    {
                        label : get_cbuilder_msg('dbuilder.rowActionsDropdownLabel'),
                        name  : 'rowActionsDropdownLabel',
                        type : 'textfield',
                        control_field: 'rowActionsMode',
                        control_value: 'dropdown',
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
                        label: get_cbuilder_msg('dbuilder.allowUserRearrangeColumns'),
                        name: 'draggabletable',
                        type: 'checkbox',
                        options : [
                            {value : 'true', label : ''}
                        ]
                    },
                    {
                        label: get_cbuilder_msg('dbuilder.allowUserShowHideColumns'),
                        name: 'showhidecolumns',
                        type: 'checkbox',
                        options : [
                            {value : 'true', label : ''}
                        ]
                    },
                    {
                        label: get_cbuilder_msg('dbuilder.mobileCardLayout'),
                        name: 'mobileCardLayout',
                        type: 'header',
                        control_field: 'disableResponsive',
                        control_value: '',
                    },
                    {
                        label: get_cbuilder_msg('dbuilder.cardCollapsible'),
                        name: 'cardCollapsible',
                        type: 'checkbox',
                        options : [
                            {value : 'true', label : ''}
                        ],
                        control_field: 'disableResponsive',
                        control_value: '',
                    },
                    {
                        label: get_cbuilder_msg('dbuilder.cardCollapseByDefault'),
                        name: 'cardCollapseByDefault',
                        type: 'checkbox',
                        options : [
                            {value : 'true', label : ''}
                        ],
                        control_field: 'cardExpanable',
                        control_value: 'true',
                    }
                ],
                control_field: 'template',
                control_value: '',
                control_use_regex: 'false'
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
                type : 'textfield',
                developer_mode : 'advanced'
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
                }],
                developer_mode : 'advanced'
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
                            label : get_cbuilder_msg('dbuilder.default'),
                            value : ''
                        },
                        {
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
     * Get properties definition for display column
     */
    getDisplayColumnPropertiesDefinition : function(elementOptions) {
        if (elementOptions !== null && elementOptions !== undefined && elementOptions.length > 0) {
            //add label & id field if does exist
            var hasId = false;
            var hasLabel = false;
            var labelIndex = 0;
            for (var p in elementOptions[0].properties) {
                if (elementOptions[0].properties[p].name === "id") {
                    hasId = true;
                } else if (elementOptions[0].properties[p].name === "label") {
                    hasLabel = true;
                    labelIndex = p;
                }
            }
            if (!hasId) {
                elementOptions[0].properties.splice(labelIndex, 0 , {
                    label : 'ID',
                    name  : 'id',
                    required : 'true',
                    type : 'textfield',
                    js_validation: "DatalistBuilder.validateDuplicateId",
                    regex_validation: '^[a-zA-Z0-9_]+$',
                    validation_message: get_cbuilder_msg("cbuilder.invalidId"),
                    id_suggestion: 'label'
                });
            }
            if (!hasLabel) {
                elementOptions[0].properties.unshift({
                    label : get_cbuilder_msg('dbuilder.label'),
                    name  : 'label',
                    required : 'true',
                    type : 'textfield'
                });
            }
            return elementOptions;
        } else {
            return [{
                title : get_cbuilder_msg('dbuilder.general'),
                properties :[
                {
                    label : get_cbuilder_msg('dbuilder.label'),
                    name  : 'label',
                    required : 'true',
                    type : 'textfield'
                },
                {
                    label : 'ID',
                    name  : 'id',
                    required : 'true',
                    type : 'textfield',
                    js_validation: "DatalistBuilder.validateDuplicateId",
                    regex_validation: '^[a-zA-Z0-9_]+$',
                    validation_message: get_cbuilder_msg("cbuilder.invalidId"),
                    id_suggestion: 'label'
                }]
            }];
        }
    },
    
    /*
     * used to render tree viewer
     */
    renderTreeMenuAdditionalNode : function(container, target) {
        var type = "";
        if (target.is("[data-cbuilder-filters]") & target.find("[data-cbuilder-classname]").length > 0) {
            type = "filters";
        } else if (target.is("[data-cbuilder-actions]") & target.find("[data-cbuilder-classname]").length > 0) {
            type = "actions";
        } else if (target.is("[data-placeholder-key]") & target.find("[data-cbuilder-classname]").length > 0) {
            type = target.attr("data-placeholder-key");
        }
        
        if (type !== "") {
            var rid = "r" + (new Date().getTime());
            var label = "";
            if (type === "filters" || type === "actions" || type === "columns" || type === "rowActions") {
                label = get_cbuilder_msg("dbuilder.type." + type);
            } else {
                label = target.attr("data-cbuilder-droparea-msg");
            }
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
     * Used to prepare the style definition
     */
    generateStylePropertiesDefinition : function(prefix, configs) {
        var self = DatalistBuilder;
        
        var orig = CustomBuilder.Builder.stylePropertiesDefinition();
        var properties = [];
        
        if (configs === null || configs === undefined) {
            configs = [{}];
        }
        
        for (var i in orig) {
            for (var j in configs) {
                var p = $.extend(true, {}, orig[i]);
                if (configs[j].label !== undefined) {
                    p.title = p.title + " ("+configs[j].label+")";
                }
                var newPrefix = prefix;
                if (newPrefix === undefined || newPrefix === null) {
                    newPrefix = "";
                }
                if (configs[j].prefix !== undefined) {
                    if (newPrefix !== "") {
                        newPrefix += "-";
                    }
                    newPrefix += configs[j].prefix;
                }
                if (newPrefix !== "") {
                    for (var k in p.properties) {
                        if (p.properties[k].name) {
                            p.properties[k].name = p.properties[k].name.replace('style', newPrefix+'-style');
                        }
                    }
                }
                properties.push(p);
            }
        }
        return properties;
    },
    
    getElementStylePropertiesDefinition(elementObj, component) {
        var styleConfig = null;
        var selectedEl = CustomBuilder.Builder.selectedEl;
        if ($(selectedEl).parent().is("[data-cbuilder-style]")) {
            try {
                styleConfig = eval($(selectedEl).parent().attr('data-cbuilder-style'));
            } catch (err){}
        }
        var key = $(selectedEl).parent().attr("data-placeholder-key");
        let currentTemplate = "";
        if (CustomBuilder.data.template && CustomBuilder.data.template.className) {
            currentTemplate = CustomBuilder.data.template.className;
        }
        const builderTemplateKey = key + "StylePropertiesDefinition" + currentTemplate;
        if (component.builderTemplate[builderTemplateKey] === undefined) {
            component.builderTemplate[builderTemplateKey] = $.extend(true, [], DatalistBuilder.generateStylePropertiesDefinition("", styleConfig));
        }
        return component.builderTemplate[builderTemplateKey];
    },
    
    /*
     * Refresh table layout based on Properties
     */
    refreshTableLayout : function() {
        var self = CustomBuilder.Builder;
        var datalist = self.frameBody.find(".dataList");
        var table = self.frameBody.find(".dataList table.responsivetable");
        if (table.length > 0) {
            self.iframe.contentWindow.responsiveTable($(datalist));
        } else {
            $(window).off("resize."+$(datalist).attr("id"));
        }
        self.iframe.contentWindow.responsiveTemplate();
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
     * Prepare the selector based on overview path parameter
     */
    getOverviewPathElementSelector : function(data, path) {
        //check is data page
        var binderIndex = path.indexOf("binder.");
        if (binderIndex === 0) {
            setTimeout(function(){
                $("#binder-btn").trigger("click");
            }, 1);
            
            return ["", path];
        } else if (path.indexOf(".") === -1) { //it is not a properties of inner object
            if (path.indexOf("-style-") !== -1) {
                //find the element like list row or card which only exist for styling
                var prefix = path.substring(0, path.indexOf("-style-"));
                return ['[data-cbuilder-style*="\''+prefix+'\'"]', path];
            } else {
                //it is properties page
                setTimeout(function(){
                    $("#properties-btn").trigger("click");
                }, 1);

                return ["", path];
            }
        }
        
        return DatalistBuilder.buildSelectorByPath(data, path);
    },
    
    /**
     * Utility method to build selector based on overview path
     */
    buildSelectorByPath: function(obj, path) {
        var selector = "";
        var propertiesPath = "";
        if (obj !== null && obj !== undefined 
                && path !== null && path !== undefined && path !== "") {
            propertiesPath = path;
            var splitpath = path.split(".");
            var currentObj = obj;
            
            //remove processed path from propertiesPath
            propertiesPath = propertiesPath.substring(splitpath[0].length + 1);
            
            try {
                var index = null;
                var property = splitpath[0];
                if (property.indexOf('[') !== -1) {
                    index = parseInt(property.substring(property.indexOf('[') + 1, property.indexOf(']')));
                    property = property.substring(0, property.indexOf('['));
                }

                currentObj = CustomBuilder.Builder.getObjectByProperty(currentObj, property, index);

                if (currentObj !== null) {
                    selector = '[data-cbuilder-id="'+currentObj.id+'"]';
                }
            } catch (err) {
                if (console && console.error) {
                    console.error(err);
                }
            }
        }
        
        if (propertiesPath.indexOf('style-') !== -1) {
            //show styling tab
            setTimeout(function(){
                $("#style-properties-tab-link a").trigger("click");
            }, 1);
        } else {
            //show properties tab
            setTimeout(function(){
                $("#element-properties-tab-link a").trigger("click");
            }, 1);
        }
        
        return [selector, propertiesPath];
    },
      
    /*
     * remove dynamically added items    
     */            
    unloadBuilder : function() {
        $("#binder-btn").remove();
    },
    
    /*
     * Check and remove orphaned columns before save
     */ 
    beforeMerge : function(deferreds) {
        var wait = $.Deferred();
        deferreds.push(wait);
        
        DatalistBuilder.removeOrphanedColumns(wait);
    },
    
    /*
     * Check and remove orphaned columns 
     */
    removeOrphanedColumns : function(deferred) {
        if ($("body").hasClass("frame_loading")) {
            //wait for the template ready
            setTimeout(function(){
                DatalistBuilder.removeOrphanedColumns(deferred);
            }, 10);
        } else {
            var self = CustomBuilder.Builder;
        
            var change = false;
        
            var ids = [];
            for(var ee in DatalistBuilder.availableColumns){
                ids.push(DatalistBuilder.availableColumns[ee].id);
            }

            //find all placeholder key
            var placeholder = [];
            self.frameBody.find(".dataList [data-placeholder-key]").each(function(){
                if ($.inArray($(this).data("placeholder-key"), placeholder) === -1) {
                    placeholder.push($(this).data("placeholder-key"));
                }
            });

            //remove unused placeholder in data
            for (var prop in CustomBuilder.data) {
                if (Object.prototype.hasOwnProperty.call(CustomBuilder.data, prop) && (prop.indexOf("column") === 0 || prop.indexOf("rowAction") === 0 || prop === "filters")) {
                    if ($.inArray(prop, placeholder) === -1 && prop !== "filters") {
                        if (prop === "columns"  || prop === "rowActions") {
                            CustomBuilder.data[prop] = [];
                        } else if (prop.indexOf("-style-") === -1 && prop !== "rowActionsMode" && prop !== "rowActionsDropdownLabel") {
                            delete CustomBuilder.data[prop];
                        }
                        change = true;
                    } else if (prop.indexOf("column") === 0 || prop === "filters") {
                        var nonExistIndex = [];

                        for (var i in CustomBuilder.data[prop]) {
                            var name = CustomBuilder.data[prop][i].name;
                            if (name !== undefined && $.inArray(name, ids) === -1) {
                                nonExistIndex.push(i);

                                //find and remove from canvas
                                var id = CustomBuilder.data[prop][i].id;
                                var element;

                                if (prop === "filters") {
                                    element = self.frameBody.find(".dataList [data-cbuilder-filters] [data-cbuilder-id='"+id+"']");
                                } else {
                                    element = self.frameBody.find(".dataList [data-placeholder-key='"+prop+"'] [data-cbuilder-id='"+id+"']");
                                }

                                var syncElements = $(element).data("syncElements");
                                if (syncElements !== null && syncElements !== undefined) {
                                    for (var i in syncElements) {
                                        $(syncElements[i]).remove();
                                    }
                                }

                                $(element).remove();
                            }
                        }

                        //remove non exist column 
                        if (nonExistIndex.length > 0) {
                            nonExistIndex.reverse();
                            for (var i in nonExistIndex) {
                                CustomBuilder.data[prop].splice(nonExistIndex[i], 1);
                                change = true;
                            }
                        }
                    }
                }
            }

            if (change) {
                $("#element-parent-box, #element-highlight-box").hide();
                CustomBuilder.Builder.selectNode(false);
                CustomBuilder.update(false);
            }
            
            if (deferred) {
                deferred.resolve();
            }
        }
    },
    
    /*
     * update the variables used by other plugins for backward compatible
     */
    afterUpdate : function(data) {
        DatalistBuilder.chosenColumns = [];
        DatalistBuilder.chosenActions = [];
        DatalistBuilder.chosenRowActions = [];
        DatalistBuilder.chosenFilters = [];
        
        for (var prop in CustomBuilder.data) {
            if (Object.prototype.hasOwnProperty.call(CustomBuilder.data, prop) 
                    && (prop.indexOf("column") === 0  || prop.indexOf("rowAction") === 0 
                    || prop === "filters" || prop === "actions")) {
                for (var i in CustomBuilder.data[prop]) {
                    var id =  CustomBuilder.data[prop][i].id;
                       
                    if (prop === "columns") {
                        DatalistBuilder.chosenColumns[id] = CustomBuilder.data[prop][i];
                    } else if (prop === "rowActions" || prop.indexOf("rowAction") === 0) {
                        DatalistBuilder.chosenRowActions[id] = CustomBuilder.data[prop][i];
                    } else if (prop === "filters") {
                        DatalistBuilder.chosenFilters[id] = CustomBuilder.data[prop][i];
                    } else if (prop === "actions") {
                        DatalistBuilder.chosenActions[id] = CustomBuilder.data[prop][i];
                    }
                }
            }
        }
    },
            
    /*
     * Validation for duplicate id of columns
     */
    validateDuplicateId : function (name, value) {
        var self = CustomBuilder.Builder;
        var found = $(self.selectedEl).parent().find('[data-cbuilder-id="'+value+'"]');
        if (found.length > 0 && !(found.length === 1 && found.is(self.selectedEl))) {
            return get_cbuilder_msg("cbuilder.duplicateId");
        }
        return null;
    }
}