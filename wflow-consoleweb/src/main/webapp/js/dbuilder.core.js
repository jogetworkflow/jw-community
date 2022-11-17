DatalistBuilder = {

    UPDATE : 'Update',

    //Configuration
    appId: '',
    appVersion: '',
    saveUrl : '',
    previewUrl : '',
    contextPath : '',
    appPath : '',
    originalJson : '',
    filterParam : '',
    datalistId : '',
    
    //undo & redo feature
    tempJson : '',
    isCtrlKeyPressed : false,
    isAltKeyPressed : false,
    undoStack : new Array(),
    redoStack : new Array(),
    undoRedoMax : 50,

    columnPrefix : "column_",
    rowActionPrefix : "rowAction_",
    actionPrefix : "action_",
    filterPrefix : "filter_",
    availableColumns : {},      //available columns
    availableColumnNames : [],      //available column name
    availableActions : [],      //available actions
    availableFormatter : [],      //available formatter
    chosenColumns : [],         //columns chosen in the designer
    chosenActions : [],         //actions chosen in the designer
    chosenRowActions : [],      //row actions chosen in the designer
    chosenFilters : [],         //filters chosen in the designer
    columnIndexCounter : 0,     //column index counter
    rowActionIndexCounter : 0,  //row action index counter
    filterIndexCounter : 0,     //filter index counter
    actionIndexCounter : 0,     //action index counter
    binderProperties : {
        className : '',
        properties : {}
    },                          //binder's properties
    datalistProperties : {
        id : '',
        name : '',
        pageSize : '0',
        order : '',
        orderBy : '',
        showPageSizeSelector : 'true',
        pageSizeSelectorOptions : '10,20,30,40,50,100',
        buttonPosition : 'bottomLeft',
        checkboxPosition : 'left',
        useSession : 'false',
        considerFilterWhenGetTotal : ''
    },                          //datalist's properties

    elementPropertyDefinitions: new Object(),   // map of property dialog definitions for each element class
    
    init : function(callback){
        var deferreds = [];
        
        DatalistBuilder.initActionList(deferreds);
        DatalistBuilder.initFormatterList(deferreds);
        DatalistBuilder.initUndoRedo();
        
        // show popup dialog
        if (!PropertyEditor.Popup.hasDialog("list-property-editor")) {
            PropertyEditor.Popup.createDialog("list-property-editor");
        }
        
        //init paste icon
        $("#databuilderContentColumns, #databuilderContentFilters, #databuilderContentActions, #databuilderContentRowActions").each(function(){
            //attach relevant events to the column
            var obj = this;
            var type = "";
            
            if ($(obj).attr("id") === "databuilderContentColumns") {
                type = "column";
            } else if ($(obj).attr("id") === "databuilderContentFilters") {
                type = "filter";
            } else if ($(obj).attr("id") === "databuilderContentActions") {
                type = "action";
            } else if ($(obj).attr("id") === "databuilderContentRowActions") {
                type = "rowAction";
            }
            
            var parent = $(obj).parent();
            if ($(parent).children(".form-palette-options").length > 0) {
                // remove if already exists
                $(parent).children(".form-palette-options").remove();
            }

            var optionHtml = "<div class='form-palette-options'>";
            optionHtml += "<button class='element-paste paste-"+type+" disabled' title='"+get_dbuilder_msg('dbuilder.paste.'+type)+"'><i class='fas fa-paste'></i><span>"+get_dbuilder_msg('dbuilder.paste.'+type)+"</span></button>";
            optionHtml += "</div>";
            var optionDiv = $(optionHtml);

            // handle paste
            $(optionDiv).children(".element-paste").click(function() {
                if ($(this).hasClass("disabled")) {
                    alert(get_dbuilder_msg("dbuilder.noCopiedItem"));
                    return false;
                }
                DatalistBuilder.paste();
            });

            // add option bar
            $(parent).prepend(optionDiv);
            $(parent).mouseover(function() {
                DatalistBuilder.updatePasteIcon();
                $(optionDiv).css("visibility", "visible");
            });
            $(parent).mouseout(function() {
                $(optionDiv).css("visibility", "hidden");
            });
            $(parent).on("click", function() {
                if ($(optionDiv).css("visibility") === "visible") {
                    $(optionDiv).css("visibility", "hidden");
                } else {
                    if ($(obj).children().length > 0) {
                        var $family = $(obj).find("*");
                        $(".form-palette-options").not($family).css("visibility", "hidden");
                    }
                    $(optionDiv).css("visibility", "visible");
                }
            });
        });
        
        $.when.apply($, deferreds).then(function() {
            if (callback) {
                callback();
            }
        });
    },
    
    initUndoRedo : function() {
        //Shortcut key
        $(document).keyup(function (e) {
            if(e.which == 17){
                DatalistBuilder.isCtrlKeyPressed=false;
            } else if(e.which === 18){
                DatalistBuilder.isAltKeyPressed = false;
            }
        }).keydown(function (e) {
            if(e.which == 17){
                DatalistBuilder.isCtrlKeyPressed=true;
            } else if(e.which === 18){
                DatalistBuilder.isAltKeyPressed = true;
            }
            if ($(".property-editor-container:visible").length === 0) {
                if(e.which == 90 && DatalistBuilder.isCtrlKeyPressed == true && !DatalistBuilder.isAltKeyPressed) { //CTRL+Z - undo
                    DatalistBuilder.undo();
                    return false;
                }
                if(e.which == 89 && DatalistBuilder.isCtrlKeyPressed == true && !DatalistBuilder.isAltKeyPressed) { //CTRL+Y - redo
                    DatalistBuilder.redo();
                    return false;
                }
            }
        });
        
        //add control
        $("#builder-steps").after("<div class='controls'></div>");
        $(".controls").append("<a class='action-undo disabled' title='"+get_dbuilder_msg('dbuilder.undo.disabled.tip')+"'><i class='fas fa-undo'></i> "+get_dbuilder_msg('dbuilder.undo')+"</a>&nbsp;|&nbsp;");
        $(".controls").append("<a class='action-redo disabled' title='"+get_dbuilder_msg('dbuilder.redo.disabled.tip')+"'><i class='fas fa-redo'></i> "+get_dbuilder_msg('dbuilder.redo')+"</a>");
        
        $(".action-undo").click(function(){
            DatalistBuilder.undo();
            return false;
        });
        
        $(".action-redo").click(function(){
            DatalistBuilder.redo();
            return false;
        });
    },

    getBinderPropertiesDefinition : function() {
        return [
            {title: get_dbuilder_msg('dbuilder.selectBinder'),
                helplink: get_dbuilder_msg('dbuilder.selectBinder.helplink'),
                properties : [{
                    name : 'binder',
                    label : get_dbuilder_msg('dbuilder.selectDataSource'),
                    type : 'elementselect',
                    options_ajax : '[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.datalist.model.DataListBinder',
                    url : '[CONTEXT_PATH]/web/property/json' + DatalistBuilder.appPath + '/getPropertyOptions'
                }]
            }
        ];
    },

    initBinderList : function(){
        var propertiesDefinition = DatalistBuilder.getBinderPropertiesDefinition();

        var propertyValues = new Array();
        propertyValues['binder'] = DatalistBuilder.binderProperties;
        
        var options = {
            appPath: DatalistBuilder.appPath,
            contextPath: DatalistBuilder.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : propertyValues,
            showCancelButton: false,
            closeAfterSaved: false,
            saveCallback: function(container, properties) {
                var binderChanged = DatalistBuilder.binderProperties.className != properties.binder.className;
                DatalistBuilder.binderProperties = properties.binder;
                if (binderChanged) {
                    DatalistBuilder.updateBinderProperties(DatalistBuilder.UPDATE);
                } else {
                    DatalistBuilder.updateBinderProperties();
                }
            }
        };

        $('#source').html("");
        $('#source').propertyEditor(options);
    },

    updateBinderProperties : function(mode){
        var temp = {};
        temp['binderJson'] = JSON.encode(DatalistBuilder.binderProperties.properties);
        temp['id'] = DatalistBuilder.datalistProperties.id;
        temp['binderId'] = DatalistBuilder.binderProperties.className;
        
        if (temp['binderId'] != "") {
            $.post(
                DatalistBuilder.contextPath + '/web/json/console/app' + DatalistBuilder.appPath + '/builder/binder/columns',
                temp,
                DatalistBuilder.updateBinderPropertiesCallBack,
                "json"
            ).error(
                function () {
                    if (DatalistBuilder.binderProperties.className != undefined && DatalistBuilder.binderProperties.className != "") {
                        alert(get_dbuilder_msg('dbuilder.errorRetrieveColumns'));
                    }
                }
            );
        }
        
        if(mode == DatalistBuilder.UPDATE){
            //reset all fields
            DatalistBuilder.chosenColumns = new Array();
            DatalistBuilder.chosenActions = new Array();
            DatalistBuilder.chosenRowActions = new Array();
            DatalistBuilder.chosenFilters = new Array();
            DatalistBuilder.columnIndexCounter = 0;
            DatalistBuilder.rowActionIndexCounter = 0;
            DatalistBuilder.filterIndexCounter = 0;
            DatalistBuilder.actionIndexCounter = 0;
            
            DatalistBuilder.datalistProperties['orderBy'] = "";
            DatalistBuilder.datalistProperties['order'] = "";

            $('.databuilderFilter').remove();
            $('.databuilderItem').remove();
            $('.databuilderAction').remove();
            $('.databuilderRowAction').remove();
        }
        DatalistBuilder.adjustCanvas();
    },

    updateBinderPropertiesCallBack : function(data){
        //populate columns at the side bar in designer
        //loop thru the list of columns returned
        var columns = eval( data.columns );
        var fields = new Object();
        
        for(e in columns){
            column = columns[e];
            temp = {
                "id"         : column.name.toString(),
                "label"      : column.label.toString(),
                "displayLabel" : column.displayLabel.toString(),
                "name"       : column.name.toString(),
                "filterable" : column.filterable
            }
            fields[temp.id] = temp;
        }
        DatalistBuilder.availableColumns = fields;
        
        //call the decorator to populate fields
        DatalistBuilder.initFields();

        //attach events to the columns
        DatalistBuilder.initEvents();
        
        //refreash column and filter label
        $("#databuilderContentColumns").children("li").each( function(){
            var e = $(this).attr("id");
            var column = DatalistBuilder.chosenColumns[e];
            
            var c = DatalistBuilder.availableColumns[column.name];
            if (c !== undefined && c.label === column.label && c.label !== c.displayLabel) {
                DatalistBuilder.renderColumn(e);
            } else if (c === undefined) {
                $(this).remove();
            }
        });

        //filters
        $("#databuilderContentFilters").children("li").each( function(){
            var e = $(this).attr("id");
            var filter = DatalistBuilder.chosenFilters[e];
            
            var c = DatalistBuilder.availableColumns[filter.name];
            if (c !== undefined && filter.label === c.label && c.label !== c.displayLabel) {
                DatalistBuilder.renderFilter(e);
            } else if (c === undefined) {
                $(this).remove();
            }
        });
        
        var orderBy = DatalistBuilder.datalistProperties['orderBy'];
        if (orderBy !== undefined && orderBy !== "") {
            var c = DatalistBuilder.availableColumns[orderBy];
            if (c === undefined) {
                DatalistBuilder.datalistProperties['orderBy'] = "";
            }
        }
        DatalistBuilder.adjustCanvas();
        
        //change to designer's tab
        $("#builder-steps-designer").trigger("click");
    },

    initFields : function(){
        //populate column palette
        var fields = DatalistBuilder.availableColumns;
        $('#builder-palettle-items').html('');
        for(var e in fields){
            var field = fields[e];
            var cssClass = "";
            var filterable = "filterable";
            DatalistBuilder.availableColumnNames.push(field.id);
            if (field.id == field.label) {
                cssClass = " key";
            }
            if (!field.filterable) {
                filterable = "";
            }
            var element = '<li><div class="builder-palette-column builder-palette-element ' + filterable + '" id="' + field.id + '" data-id="' + field.id + '"><i class="far fa-hdd"></i> <label class="label' + cssClass + '">' + UI.escapeHTML(field.displayLabel) + '</label></div></li>';
            $('#builder-palettle-items').append(element);
        }
        
        //remove invalid columns & filters from canvas
        $("#databuilderContentColumns").children("li").each( function(){
            var e = $(this).attr("id");
            var column = DatalistBuilder.chosenColumns[e];
            if ($.inArray(column.name, DatalistBuilder.availableColumnNames) === -1) {
                delete DatalistBuilder.chosenColumns[e];
                $(this).remove();
            }
        });
        $("#databuilderContentFilters").children("li").each( function(){
            var e = $(this).attr("id");
            var column = DatalistBuilder.chosenFilters[e];
            if ($.inArray(column.name, DatalistBuilder.availableColumnNames)  === -1) {
                delete DatalistBuilder.chosenFilters[e];
                $(this).remove();
            }
        });
        
        DatalistBuilder.adjustCanvas();
    },

    initActionList : function(deferreds){
        var wait = $.Deferred();
        deferreds.push(wait);
        
        $.getJSON(
            DatalistBuilder.contextPath + '/web/json/console/app' + DatalistBuilder.appPath + '/builder/actions',
            function(returnedData){
                //put data into DatalistBuilder.availableActions
                availableActionsReturned = returnedData.actions;
                availableActions = [];
                for(e in availableActionsReturned){
                    availableActions[availableActionsReturned[e]['className']] = availableActionsReturned[e];
                }

                DatalistBuilder.availableActions = availableActions;
                DatalistBuilder.initActions();
                wait.resolve();
            }
        );

    },
    
    initFormatterList : function(deferreds){
        var wait = $.Deferred();
        deferreds.push(wait);
        
        $.getJSON(
            DatalistBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.apps.datalist.model.DataListColumnFormat',
            function(returnedData){
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        DatalistBuilder.availableFormatter[returnedData[e].value] = returnedData[e].label;
                    }
                }
                wait.resolve();
            }
        );
    },

    initActions : function(){
        //populate action palette
        var actions = DatalistBuilder.availableActions;
        $('#builder-palettle-actions').html('');
        for(var e in actions){
            var action = actions[e];
            
            var iconObj = null;
            if (action.icon !== undefined && action.icon !== null && action.icon !== "") {
                try {   
                    iconObj = $(action.icon);
                } catch (err) {
                    iconObj =  $('<span class="image" style="background-image:url(\'' + DatalistBuilder.contextPath + action.icon + '\');" />');
                }
            } else {
                iconObj = $('<span class=\"fa-stack\"><i><span style="border: 1.4px solid #4c4c4c; height: 10px; width: 15px; display: block; position: absolute; border-radius: 3px; left: 3px; top: 4px;"></span></i><i class=\"fas fa-hand-point-up fa-stack-xs\" style=\"padding-top: 2px;\"></i></span>');
            }
            
            var element = $('<li><div class="builder-palette-action builder-palette-element" id="' + action.className + '" data-id="' + action.className + '"> <label class="label">' + UI.escapeHTML(action.label) + '</label></div></li>');
            $(element).find('.builder-palette-element').prepend(iconObj);
            $('#builder-palettle-actions').append(element);
        }
        DatalistBuilder.initEvents();
        
        // decorate row actions
        $('#databuilderContentRowActions li').each(function(idx, obj) {
            var id = $(obj).attr("id");
            if (id && id != "") {
                DatalistBuilder.decorateRowAction(id);
            }
        });

        // decorate actions
        $('#databuilderContentActions li').each(function(idx, obj) {
            var id = $(obj).attr("id");
            if (id && id != "") {
                DatalistBuilder.decorateAction(id);
            }
        });

    },

    initEvents : function(){
        //attach draggable and related events to columns and actions

        //columns and filters drag
        $('.builder-palette-column:not(.filterable)').draggable({
            connectToSortable: "#databuilderContentColumns",
            opacity: 0.7,
            helper: "clone",
            zIndex: 200,
            revert: "invalid",
            cursor: "move"
        }).disableSelection();
        
        $('.builder-palette-column.filterable').draggable({
            connectToSortable: "#databuilderContentColumns, #databuilderContentFilters",
            opacity: 0.7,
            helper: "clone",
            zIndex: 200,
            revert: "invalid",
            cursor: "move"
        }).disableSelection();
        
        //row actions and actions drag
        $('.builder-palette-action').draggable({
            connectToSortable: "#databuilderContentRowActions, #databuilderContentActions",
            opacity: 0.7,
            helper: "clone",
            zIndex: 200,
            revert: "invalid",
            cursor: "move"
        }).disableSelection();

        $("#databuilderContentColumns").sortable({
            opacity: 0.8,
            dropOnEmpty: true,
            activeClass: "form-cell-highlight",
            axis:'x',
            over: function( event, ui ) {
                DatalistBuilder.tempJson = DatalistBuilder.getJson();
                DatalistBuilder.adjustCanvas(event, ui);
            },
            stop: function(event, ui){
                DatalistBuilder.addToUndo(DatalistBuilder.tempJson);
                if($(ui.item[0]).hasClass("builder-palette-column")){
                    var id = $(ui.item[0]).data('id');
                    var columnId = DatalistBuilder.getId("column");
                    DatalistBuilder.chosenColumns[columnId] = DatalistBuilder.cloneObject(DatalistBuilder.availableColumns[id]);
                    DatalistBuilder.chosenColumns[columnId].id = columnId;
                    DatalistBuilder.chosenColumns[columnId].name = id;
                    DatalistBuilder.chosenColumns[columnId].hidden = "false";
                    DatalistBuilder.chosenColumns[columnId].sortable = "false";
                    delete DatalistBuilder.chosenColumns[columnId].displayLabel;

                    //call decorator to render column in the designer's canvas
                    $(ui.item[0]).attr("id", columnId);
                    DatalistBuilder.renderColumn(columnId);
                }
                
                DatalistBuilder.adjustCanvas(event, ui);
            },
            sort: function (event, ui) {
                var that = $(this),
                    w = ui.helper.outerWidth();
                that.children().each(function () {
                    if ($(this).hasClass('ui-sortable-helper') || $(this).hasClass('ui-sortable-placeholder')) 
                        return true;
                    // If overlap is more than half of the dragged item
                    var dist = Math.abs($(ui.helper).offset().left - $(this).offset().left),
                        before = $(ui.helper).offset().left > $(this).offset().left; 
                    if ((w - dist) > (w / 2) && (dist < w)) {
                        if (before)
                            $('.ui-sortable-placeholder', that).insertBefore($(this));
                        else
                            $('.ui-sortable-placeholder', that).insertAfter($(this));
                        return false;
                    }
                });
            }
        });
        
        $("#databuilderContentFilters").sortable({
            opacity: 0.8,
            dropOnEmpty: true,
            activeClass: "form-cell-highlight",
            axis:'x',
            over: function( event, ui ) {
                DatalistBuilder.tempJson = DatalistBuilder.getJson();
                DatalistBuilder.adjustCanvas(event, ui);
            },
            stop: function(event, ui){
                DatalistBuilder.addToUndo(DatalistBuilder.tempJson);
                if($(ui.item[0]).hasClass("builder-palette-column")){
                    var id = $(ui.item[0]).data('id');
                    var columnId = DatalistBuilder.getId("filter");
                    temp = DatalistBuilder.cloneObject(DatalistBuilder.availableColumns[id]);
                    DatalistBuilder.chosenFilters[columnId] = {};
                    DatalistBuilder.chosenFilters[columnId].id = columnId;
                    DatalistBuilder.chosenFilters[columnId].name = id;
                    DatalistBuilder.chosenFilters[columnId].label = temp.label;
                    DatalistBuilder.chosenFilters[columnId].type = {
                        "className" : "org.joget.apps.datalist.lib.TextFieldDataListFilterType",
                        "properties" : {}
                    };

                    //call decorator to render column in the designer's canvas
                    $(ui.item[0]).attr("id", columnId);
                    DatalistBuilder.renderFilter(columnId);
                }
                
                DatalistBuilder.adjustCanvas(event, ui);
            },
            sort: function (event, ui) {
                var that = $(this),
                    w = ui.helper.outerWidth();
                that.children().each(function () {
                    if ($(this).hasClass('ui-sortable-helper') || $(this).hasClass('ui-sortable-placeholder')) 
                        return true;
                    // If overlap is more than half of the dragged item
                    var dist = Math.abs($(this).offset().left - $(ui.helper).offset().left),
                        before = $(ui.helper).offset().left < $(this).offset().left;
                    if ((w - dist) > (w / 2) && (dist < w)) {
                        if (before)
                            $('.ui-sortable-placeholder', that).insertBefore($(this));
                        else
                            $('.ui-sortable-placeholder', that).insertAfter($(this));
                        return false;
                    }
                });
            }
        });
        
        $("#databuilderContentRowActions").sortable({
            opacity: 0.8,
            dropOnEmpty: true,
            activeClass: "form-cell-highlight",
            axis:'x',
            over: function( event, ui ) {
                DatalistBuilder.tempJson = DatalistBuilder.getJson();
                DatalistBuilder.adjustCanvas(event, ui);
            },
            stop: function(event, ui){
                if($(ui.item[0]).hasClass("builder-palette-action")){
                    var id = $(ui.item[0]).data('id');
                    var action = DatalistBuilder.availableActions[id];
                    
                    if (action.supportRow) {    
                        DatalistBuilder.addToUndo(DatalistBuilder.tempJson);
                
                        var columnId = DatalistBuilder.getId("rowAction");
                        DatalistBuilder.chosenRowActions[columnId] = DatalistBuilder.cloneObject(action);
                        DatalistBuilder.chosenRowActions[columnId].id = columnId;
                        if (DatalistBuilder.chosenRowActions[columnId].defaultPropertyValues !== undefined &&
                                DatalistBuilder.chosenRowActions[columnId].defaultPropertyValues !== "") {
                            try {
                                DatalistBuilder.chosenRowActions[columnId].properties = eval("["+DatalistBuilder.chosenRowActions[columnId].defaultPropertyValues+"]")[0];
                            } catch (err) {
                                DatalistBuilder.chosenRowActions[columnId].properties = new Object();
                            }
                        } else {
                            DatalistBuilder.chosenRowActions[columnId].properties = new Object();
                        }
                        DatalistBuilder.chosenRowActions[columnId].properties.label = UI.escapeHTML(action.label);
                        delete DatalistBuilder.chosenRowActions[columnId].propertyOptions;
                        delete DatalistBuilder.chosenRowActions[columnId].supportColumn;
                        delete DatalistBuilder.chosenRowActions[columnId].supportRow;
                        delete DatalistBuilder.chosenRowActions[columnId].supportList;
                        delete DatalistBuilder.chosenRowActions[columnId].defaultPropertyValues;

                        //call decorator to render column in the designer's canvas
                        $(ui.item[0]).attr("id", columnId);
                        DatalistBuilder.renderRowAction(columnId);
                        
                        DatalistBuilder.adjustCanvas(event, ui);
                    } else {
                        $(ui.item[0]).remove();
                        alert(get_dbuilder_msg("dbuilder.notSupportRow"));
                    }
                } else {
                    DatalistBuilder.addToUndo(DatalistBuilder.tempJson);
                }
                DatalistBuilder.adjustCanvas(event, ui);
            },
            sort: function (event, ui) {
                var that = $(this),
                    w = ui.helper.outerWidth();
                that.children().each(function () {
                    if ($(this).hasClass('ui-sortable-helper') || $(this).hasClass('ui-sortable-placeholder')) 
                        return true;
                    // If overlap is more than half of the dragged item
                    var dist = Math.abs($(ui.helper).offset().left - $(this).offset().left),
                        before = $(ui.helper).offset().left > $(this).offset().left; 
                    if ((w - dist) > (w / 2) && (dist < w)) {
                        if (before)
                            $('.ui-sortable-placeholder', that).insertBefore($(this));
                        else
                            $('.ui-sortable-placeholder', that).insertAfter($(this));
                        return false;
                    }
                });
            }
        });
        
        $("#databuilderContentActions").sortable({
            opacity: 0.8,
            dropOnEmpty: true,
            activeClass: "form-cell-highlight",
            axis:'x',
            over: function( event, ui ) {
                DatalistBuilder.tempJson = DatalistBuilder.getJson();
                DatalistBuilder.adjustCanvas(event, ui);
            },
            stop: function(event, ui){
                if($(ui.item[0]).hasClass("builder-palette-action")){
                    var id = $(ui.item[0]).data('id');
                    var action = DatalistBuilder.availableActions[id];
                    
                    if (action.supportList) {
                        DatalistBuilder.addToUndo(DatalistBuilder.tempJson);
                        var columnId = DatalistBuilder.getId("action");
                        DatalistBuilder.chosenActions[columnId] = DatalistBuilder.cloneObject(action);
                        DatalistBuilder.chosenActions[columnId].id = columnId;
                        if (DatalistBuilder.chosenActions[columnId].defaultPropertyValues !== undefined &&
                                DatalistBuilder.chosenActions[columnId].defaultPropertyValues !== "") {
                            try {
                                DatalistBuilder.chosenActions[columnId].properties = eval("["+DatalistBuilder.chosenActions[columnId].defaultPropertyValues+"]")[0];
                            } catch (err) {
                                DatalistBuilder.chosenActions[columnId].properties = new Object();
                            }
                        } else {
                            DatalistBuilder.chosenActions[columnId].properties = new Object();
                        }
                        DatalistBuilder.chosenActions[columnId].properties.label = UI.escapeHTML(action.label);
                        delete DatalistBuilder.chosenActions[columnId].propertyOptions;
                        delete DatalistBuilder.chosenActions[columnId].supportColumn;
                        delete DatalistBuilder.chosenActions[columnId].supportRow;
                        delete DatalistBuilder.chosenActions[columnId].supportList;
                        delete DatalistBuilder.chosenActions[columnId].defaultPropertyValues;

                        //call decorator to render column in the designer's canvas
                        $(ui.item[0]).attr("id", columnId);
                        DatalistBuilder.renderAction(columnId);
                    } else {
                        $(ui.item[0]).remove();
                        alert(get_dbuilder_msg("dbuilder.notSupportList"));
                    }
                } else {
                    DatalistBuilder.addToUndo(DatalistBuilder.tempJson);
                }
                
                DatalistBuilder.adjustCanvas(event, ui);
            },
            sort: function (event, ui) {
                var that = $(this),
                    w = ui.helper.outerWidth();
                that.children().each(function () {
                    if ($(this).hasClass('ui-sortable-helper') || $(this).hasClass('ui-sortable-placeholder')) 
                        return true;
                    // If overlap is more than half of the dragged item
                    var dist = Math.abs($(ui.helper).offset().left - $(this).offset().left),
                        before = $(ui.helper).offset().left > $(this).offset().left; 
                    if ((w - dist) > (w / 2) && (dist < w)) {
                        if (before)
                            $('.ui-sortable-placeholder', that).insertBefore($(this));
                        else
                            $('.ui-sortable-placeholder', that).insertAfter($(this));
                        return false;
                    }
                });
            }
        });
    },

    renderFilter : function(columnId){
        var filter = $.extend({}, DatalistBuilder.chosenFilters[columnId]);
        
        if (DatalistBuilder.availableColumns[filter.name] !== undefined && filter.label === DatalistBuilder.availableColumns[filter.name].label) {
            filter.label = DatalistBuilder.availableColumns[filter.name].displayLabel;
        }
        
        var elementHtml = DatalistBuilder.retrieveFilterHTML(columnId, filter);
        
        if( $('#databuilderContentFilters #' + columnId).size() != 0 ){
            //replace existing
            var current =  $('#databuilderContentFilters #' + columnId);
            $(current).replaceWith(elementHtml);
        }else{
            //append if new
            $('#databuilderContentFilters').append(elementHtml);
        }
        DatalistBuilder.decorateFilter(columnId);
        DatalistBuilder.adjustCanvas();
    },
    
    retrieveFilterHTML: function(id, property) {
        var jsonStr = JSON.encode(property);
        $.ajax({
            type: "POST",
            data: {"json": jsonStr },
            url: DatalistBuilder.contextPath + '/web/dbuilder/getFilterTemplate',
            dataType : "text",
            beforeSend: function (request) {
                request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            },
            success: function(response) {
                var newElement = $('<li class="databuilderFilter column" id="' + id + '"><div class="content">' + response + '</div></li>');

                var current =  $('#databuilderContentFilters #' + id);
                $(current).replaceWith(newElement);
                DatalistBuilder.decorateFilter(id);
                DatalistBuilder.adjustCanvas();
            }
        });

        return '<li class="databuilderFilter column" id="' + id + '">' + get_dbuilder_msg("dbuilder.loading") + '</li>';
    },

    renderAction : function(columnId){
        var action = DatalistBuilder.chosenActions[columnId];

        var label = action.label;
        if(action.properties.label != undefined){
            label = action.properties.label;
        }
        label = $("<span></span>").text(UI.escapeHTML(label)).html();

        var string = '<li class="databuilderAction column" id="' + columnId + '"><div class="databuilderItemTitle">' + label + '</div>';
        string += '<div class="databuilderItemContent">';
        string += '<input value="' + label + '" type="button">';
        string += '</div>';

        if( $('#databuilderContentActions #' + columnId).size() != 0 ){
            //replace existing
            var current = $('#databuilderContentActions #' + columnId);
            $(current).replaceWith(string);
        }else{
            //append if new
            $('#databuilderContentActions').append(string);
        }
        DatalistBuilder.decorateAction(columnId);
        DatalistBuilder.adjustCanvas();
    },

    renderRowAction : function(columnId){
        var rowAction = DatalistBuilder.chosenRowActions[columnId];
        var label = rowAction.label;
        if(rowAction.properties.label != undefined){
            label = rowAction.properties.label;
        }
        label = $("<span></span>").text(label).html();

        var string = '<li class="databuilderRowAction column" id="' + columnId + '"><div class="databuilderItemTitle">' + label + '</div>';
        string += '<div class="databuilderItemContent">';
        string += '<ul>';

        var count = 0;
        while(count < 7){
            var className = '';
            if(count % 2 == 1){
                className = 'even';
            }
            string += '<li class="' + className + '">' + label + '</li>';
            count++;
        }
        string += '</ul></div>';
        string += '</li>';

        if( $('#databuilderContentRowActions #' + columnId).size() != 0 ){
            //replace existing
            var current = $('#databuilderContentRowActions #' + columnId);
            $(current).replaceWith(string);
        }else{
            //append if new
            $('#databuilderContentRowActions').append(string);
        }
        DatalistBuilder.decorateRowAction(columnId);
        DatalistBuilder.adjustCanvas();
    },
    
    getActionLabel : function(classname) {
        return DatalistBuilder.availableActions[classname].label;
    },
    
    getFormatterLabel : function(classname) {
        return DatalistBuilder.availableFormatter[classname];
    },

    renderColumn : function(columnId){
        var column = DatalistBuilder.chosenColumns[columnId];
        var sortable = '';
        if(column.name == DatalistBuilder.datalistProperties['orderBy']){
            if(DatalistBuilder.datalistProperties['order'] == '2'){
                sortable = '(A->Z)';
            }else{
                sortable = '(Z->A)';
            }
        }
        var label = column.label;
        if (DatalistBuilder.availableColumns[column.name] !== undefined && label === DatalistBuilder.availableColumns[column.name].label) {
            label = DatalistBuilder.availableColumns[column.name].displayLabel;
        }

        var string = '<li class="databuilderItem column" id="' + columnId + '"><div class="databuilderItemTitle">' + UI.escapeHTML(label) + '&nbsp;' + sortable + '</div>';
        string += '<div class="databuilderItemContent">';
        string += '<ul>';

        string += '<li class=""><i class="fas fa-database"></i> ' + column.name + '</li>';
        string += '<li class="even"><i class="fas fa-sort"></i> ' + ((column.sortable === "true")?get_dbuilder_msg('dbuilder.sortable'):get_dbuilder_msg('dbuilder.unsortable')) + '</li>';
        if (column.hidden === "true") {
            string += '<li class=""><i class="fas fa-eye-slash"></i> ' + get_dbuilder_msg('dbuilder.hidden') + ' </li>';
        } else {
            string += '<li class=""><i class="fas fa-eye"></i> ' + get_dbuilder_msg('dbuilder.visible') + ' </li>';
        }
        var exportable = true;
        if ((column.hidden === "true" && column.include_export !== "true") ||
                (column.hidden !== "true" && column.exclude_export === "true")) {
            exportable = false;
        }
        
        string += '<li class="even"><i class="fas fa-file-export"></i> ' + ((exportable)?get_dbuilder_msg('dbuilder.exportable'):get_dbuilder_msg('dbuilder.unexportable')) + '</li>';
        string += '<li class=""><i class="fas fa-ruler-horizontal"></i> ' + ((column.width !== "" && column.width !== undefined)?column.width:get_dbuilder_msg('dbuilder.default')) + '</li>';
        
        var action = '-';
        if (column.action !== undefined && column.action.className !== '' && column.action.className !== undefined) {
            action = DatalistBuilder.getActionLabel(column.action.className);
        }
        var formatter = '-';
        if (column.format !== undefined && column.format.className !== '' && column.format.className !== undefined) {
            formatter = DatalistBuilder.getFormatterLabel(column.format.className);
        }
        
        string += '<li class="even"><i class="fas fa-link"></i> ' + action + '</li>';
        string += '<li class=""><i class="fas fa-paint-brush"></i> ' + formatter + '</li>';

        string += '</ul></div>';
        string += '</li>';

        if( $('#databuilderContentColumns #' + columnId).size() != 0 ){
            //replace existing
            var current = $('#databuilderContentColumns #' + columnId);
            $(current).replaceWith(string);
        }else{
            //append if new
            $('#databuilderContentColumns').append(string);
        }
        DatalistBuilder.decorateColumn(columnId);
        DatalistBuilder.adjustCanvas();
    },

    decorateFilter : function(columnId){
        //attach relevant events to the column
        var obj = $("#" + columnId);
        if ($(obj).children(".form-palette-options").length > 0) {
            // remove if already exists
            $(obj).children(".form-palette-options").remove();
            $(obj).children(".element-clear").remove();
        }

        var optionHtml = "<div class='form-palette-options'>";
        optionHtml += "<button class='element-edit-properties' title='"+get_dbuilder_msg('dbuilder.edit')+"'><i class='far fa-edit'></i><span>"+get_dbuilder_msg('dbuilder.edit')+"</span></button>";
        optionHtml += "<button class='element-copy' title='"+get_dbuilder_msg('dbuilder.copy')+"'><i class='far fa-copy'></i><span>"+get_dbuilder_msg('dbuilder.copy')+"</span></button>";
        optionHtml += "<button class='element-delete' title='"+get_dbuilder_msg('dbuilder.delete')+"'><i class='fas fa-times'></i><span>"+get_dbuilder_msg('dbuilder.delete')+"</span></button>";
        optionHtml += "</div><div class='element-clear'></div>";
        var optionDiv = $(optionHtml);

        //events handling
        //=====================
        // handle edit properties label
        $(optionDiv).children(".element-edit-properties").click(function() {
            DatalistBuilder.showFilterProperties(columnId);
        });

        // handle copy
        $(optionDiv).children(".element-copy").click(function() {
            DatalistBuilder.copy($(this).parent().parent());
        });
        
        // handle delete
        $(optionDiv).children(".element-delete").click(function() {
            DatalistBuilder.deleteFilter(columnId);
        });

        // add option bar
        $(obj).prepend(optionDiv);
        $(obj).mouseover(function() {
            $(optionDiv).css("visibility", "visible");
        });
        $(obj).mouseout(function() {
            $(optionDiv).css("visibility", "hidden");
        });
        $(obj).on("click", function() {
            if ($(optionDiv).css("visibility") === "visible") {
                $(optionDiv).css("visibility", "hidden");
            } else {
                if ($(obj).children().length > 0) {
                    var $family = $(obj).find("*");
                    $(".form-palette-options").not($family).css("visibility", "hidden");
                }
                $(optionDiv).css("visibility", "visible");
            }
        });
    },

    decorateAction : function(columnId){
        //attach relevant events to the column
        var obj = $("#" + columnId);
        if ($(obj).children(".form-palette-options").length > 0) {
            // remove if already exists
            $(obj).children(".form-palette-options").remove();
            $(obj).children(".element-clear").remove();
        }

        var optionHtml = "<div class='form-palette-options'>";

        // add buttons for action
        var action = DatalistBuilder.chosenActions[columnId];
        var propertyValues = DatalistBuilder.availableActions[action.className];
        if (propertyValues && propertyValues.propertyOptions) {
            optionHtml += "<button class='element-edit-properties' title='"+get_dbuilder_msg('dbuilder.edit')+"'><i class='far fa-edit'></i><span>"+get_dbuilder_msg('dbuilder.edit')+"</span></button>";
        }            
        optionHtml += "<button class='element-copy' title='"+get_dbuilder_msg('dbuilder.copy')+"'><i class='far fa-copy'></i><span>"+get_dbuilder_msg('dbuilder.copy')+"</span></button>";
        optionHtml += "<button class='element-delete' title='"+get_dbuilder_msg('dbuilder.delete')+"'><i class='fas fa-times'></i><span>"+get_dbuilder_msg('dbuilder.delete')+"</span></button>";

        optionHtml += "</div><div class='element-clear'></div>";
        var optionDiv = $(optionHtml);

        //events handling
        //=====================
        // handle edit properties label
        $(optionDiv).children(".element-edit-properties").click(function() {
            DatalistBuilder.showActionProperties(columnId, DatalistBuilder.chosenActions);
        });

        // handle copy
        $(optionDiv).children(".element-copy").click(function() {
            DatalistBuilder.copy($(this).parent().parent());
        });
        
        // handle delete
        $(optionDiv).children(".element-delete").click(function() {
            DatalistBuilder.deleteAction(columnId);
        });

        // add option bar
        $(obj).prepend(optionDiv);
        $(obj).mouseover(function() {
            $(optionDiv).css("visibility", "visible");
        });
        $(obj).mouseout(function() {
            $(optionDiv).css("visibility", "hidden");
        });
        $(obj).on("click", function() {
            if ($(optionDiv).css("visibility") === "visible") {
                $(optionDiv).css("visibility", "hidden");
            } else {
                if ($(obj).children().length > 0) {
                    var $family = $(obj).find("*");
                    $(".form-palette-options").not($family).css("visibility", "hidden");
                }
                $(optionDiv).css("visibility", "visible");
            }
        });
    },

    decorateRowAction : function(columnId){
        //attach relevant events to the column
        var obj = $("#" + columnId);
        if ($(obj).children(".form-palette-options").length > 0) {
            // remove if already exists
            $(obj).children(".form-palette-options").remove();
            $(obj).children(".element-clear").remove();
        }

        var optionHtml = "<div class='form-palette-options'>";

        // add buttons for action
        var action = DatalistBuilder.chosenRowActions[columnId];
        var propertyValues = DatalistBuilder.availableActions[action.className];
        if (propertyValues && propertyValues.propertyOptions) {
            optionHtml += "<button class='element-edit-properties' title='"+get_dbuilder_msg('dbuilder.edit')+"'><i class='far fa-edit'></i><span>"+get_dbuilder_msg('dbuilder.edit')+"</span></button>";
        }            
        optionHtml += "<button class='element-copy' title='"+get_dbuilder_msg('dbuilder.copy')+"'><i class='far fa-copy'></i><span>"+get_dbuilder_msg('dbuilder.copy')+"</span></button>";
        optionHtml += "<button class='element-delete' title='"+get_dbuilder_msg('dbuilder.delete')+"'><i class='fas fa-times'></i><span>"+get_dbuilder_msg('dbuilder.delete')+"</span></button>";

        optionHtml += "</div><div class='element-clear'></div>";
        var optionDiv = $(optionHtml);

        //events handling
        //=====================
        // handle edit properties label
        $(optionDiv).children(".element-edit-properties").click(function() {
            DatalistBuilder.showRowActionProperties(columnId, DatalistBuilder.chosenRowActions);
        });

        // handle copy
        $(optionDiv).children(".element-copy").click(function() {
            DatalistBuilder.copy($(this).parent().parent());
        });
        
        // handle delete
        $(optionDiv).children(".element-delete").click(function() {
            DatalistBuilder.deleteRowAction(columnId);
        });

        // add option bar
        $(obj).prepend(optionDiv);
        $(obj).mouseover(function() {
            $(optionDiv).css("visibility", "visible");
        });
        $(obj).mouseout(function() {
            $(optionDiv).css("visibility", "hidden");
        });
        $(obj).on("click", function() {
            if ($(optionDiv).css("visibility") === "visible") {
                $(optionDiv).css("visibility", "hidden");
            } else {
                if ($(obj).children().length > 0) {
                    var $family = $(obj).find("*");
                    $(".form-palette-options").not($family).css("visibility", "hidden");
                }
                $(optionDiv).css("visibility", "visible");
            }
        });
    },

    decorateColumn : function(columnId){
        //attach relevant events to the column
        var obj = $("#" + columnId);
        if ($(obj).children(".form-palette-options").length > 0) {
            // remove if already exists
            $(obj).children(".form-palette-options").remove();
            $(obj).children(".element-clear").remove();
        }

        var optionHtml = "<div class='form-palette-options'>";
        optionHtml += "<button class='element-edit-properties' title='"+get_dbuilder_msg('dbuilder.properties')+"'><i class='far fa-edit'></i><span>"+get_dbuilder_msg('dbuilder.properties')+"</span></button>";
        optionHtml += "<button class='element-copy' title='"+get_dbuilder_msg('dbuilder.copy')+"'><i class='far fa-copy'></i><span>"+get_dbuilder_msg('dbuilder.copy')+"</span></button>";
        optionHtml += "<button class='element-delete' title='"+get_dbuilder_msg('dbuilder.delete')+"'><i class='fas fa-times'></i><span>"+get_dbuilder_msg('dbuilder.delete')+"</span></button>";
        optionHtml += "</div><div class='element-clear'></div>";
        var optionDiv = $(optionHtml);

        //events handling
        //=====================
        // handle edit properties label
        $(optionDiv).children(".element-edit-properties").click(function() {
            DatalistBuilder.showColumnProperties(columnId);
        });

        // handle copy
        $(optionDiv).children(".element-copy").click(function() {
            DatalistBuilder.copy($(this).parent().parent());
        });
        
        // handle delete
        $(optionDiv).children(".element-delete").click(function() {
            DatalistBuilder.deleteColumn(columnId);
        });

        // add option bar
        $(obj).prepend(optionDiv);
        $(obj).mouseover(function() {
            $(optionDiv).css("visibility", "visible");
        });
        $(obj).mouseout(function() {
            $(optionDiv).css("visibility", "hidden");
        });
        $(obj).on("click", function() {
            if ($(optionDiv).css("visibility") === "visible") {
                $(optionDiv).css("visibility", "hidden");
            } else {
                if ($(obj).children().length > 0) {
                    var $family = $(obj).find("*");
                    $(".form-palette-options").not($family).css("visibility", "hidden");
                }
                $(optionDiv).css("visibility", "visible");
            }
        });
    },

    getColumnPropertiesDefinition : function() {
        return [{
            title : get_dbuilder_msg('dbuilder.general'),
            helplink : get_dbuilder_msg('dbuilder.column.helplink'),
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
                label : get_dbuilder_msg('dbuilder.name'),
                name  : 'name',
                type : 'label'
            },
            {
                label : get_dbuilder_msg('dbuilder.label'),
                name  : 'label',
                required : 'true',
                type : 'textfield'
            },
            {
                label : get_dbuilder_msg('dbuilder.sortable'),
                name  : 'sortable',
                required : 'true',
                type : 'selectbox',
                options : [{
                    label : get_dbuilder_msg('dbuilder.sortable.no'),
                    value : 'false'
                },
                {
                    label : get_dbuilder_msg('dbuilder.sortable.yes'),
                    value : 'true'
                }]
            },
            {
                label : get_dbuilder_msg('dbuilder.export.renderHtml'),
                name  : 'renderHtml',
                type : 'selectbox',
                options : [{
                    label : get_dbuilder_msg('dbuilder.pageSize.default'),
                    value : ''
                },{
                    label : get_dbuilder_msg('dbuilder.hidden.no'),
                    value : 'false'
                },
                {
                    label : get_dbuilder_msg('dbuilder.hidden.yes'),
                    value : 'true'
                }]
            },
            {
                label : get_dbuilder_msg('dbuilder.hidden'),
                name  : 'hidden',
                required : 'true',
                type : 'selectbox',
                options : [{
                    label : get_dbuilder_msg('dbuilder.hidden.no'),
                    value : 'false'
                },
                {
                    label : get_dbuilder_msg('dbuilder.hidden.yes'),
                    value : 'true'
                }]
            },
            {
                label : get_dbuilder_msg('dbuilder.export.include'),
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
                label : get_dbuilder_msg('dbuilder.export.exclude'),
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
                label : get_dbuilder_msg('dbuilder.width'),
                name  : 'width',
                type : 'textfield'
            },
            {
                label : get_dbuilder_msg('dbuilder.style'),
                name  : 'style',
                type : 'textfield'
            },
            {
                label : get_dbuilder_msg('dbuilder.alignment'),
                name  : 'alignment',
                type : 'selectbox',
                value : '',
                options : [{
                    label : get_dbuilder_msg('dbuilder.pageSize.default'),
                    value : ''
                },{
                    label : get_dbuilder_msg('dbuilder.align.center'),
                    value : 'dataListAlignCenter'
                },{
                    label : get_dbuilder_msg('dbuilder.align.left'),
                    value : 'dataListAlignLeft'
                },
                {
                    label : get_dbuilder_msg('dbuilder.align.right'),
                    value : 'dataListAlignRight'
                }]
            },
            {
                label : get_dbuilder_msg('dbuilder.headerAlignment'),
                name  : 'headerAlignment',
                type : 'selectbox',
                value : '',
                options : [{
                    label : get_dbuilder_msg('dbuilder.pageSize.default'),
                    value : ''
                },{
                    label : get_dbuilder_msg('dbuilder.align.center'),
                    value : 'dataListAlignCenter'
                },{
                    label : get_dbuilder_msg('dbuilder.align.left'),
                    value : 'dataListAlignLeft'
                },
                {
                    label : get_dbuilder_msg('dbuilder.align.right'),
                    value : 'dataListAlignRight'
                }]
            }]
        },{
            title : get_dbuilder_msg('dbuilder.actionMapping'),
            properties : [
            {
                name : 'action',
                label : get_dbuilder_msg('dbuilder.action'),
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
                url : '[CONTEXT_PATH]/web/property/json' + DatalistBuilder.appPath + '/getPropertyOptions'
            }]
        },{
            title : get_dbuilder_msg('dbuilder.formatter'),
            properties :[
            {
                name : 'format',
                label : get_dbuilder_msg('dbuilder.formatter'),
                type : 'elementselect',
                options_ajax : '[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.datalist.model.DataListColumnFormat',
                url : '[CONTEXT_PATH]/web/property/json' + DatalistBuilder.appPath + '/getPropertyOptions'
            }]
        }];
    },

    showColumnProperties : function(columnId){
        var propertiesDefinition = DatalistBuilder.getColumnPropertiesDefinition();

        var propertyValues = DatalistBuilder.chosenColumns[columnId];
        if (propertyValues !== null && propertyValues !== undefined) {
            propertyValues['datalist_type'] = 'column';
        }

        var options = {
            appPath: DatalistBuilder.appPath,
            contextPath: DatalistBuilder.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : propertyValues,
            showCancelButton : true,
            changeCheckIgnoreUndefined: true,
            cancelCallback: function() {
            },
            saveCallback: function(container, properties) {
                // update element properties
                DatalistBuilder.updateColumnPropertiesCallback(columnId, properties);
            }
        }
        
        PropertyEditor.Popup.showDialog("list-property-editor", options);
    },

    updateColumnPropertiesCallback : function (columnId, properties){
        DatalistBuilder.addToUndo();
        
        if (properties["renderHtml"] === "") {
            delete properties["renderHtml"];
        }
        
        DatalistBuilder.chosenColumns[columnId] = $.extend(DatalistBuilder.chosenColumns[columnId], properties);
        DatalistBuilder.renderColumn(columnId);
    },
    
    getFilterPropertiesDefinition : function () {
        return [{
            title : get_dbuilder_msg('dbuilder.general'),
            helplink : get_dbuilder_msg('dbuilder.filter.helplink'),
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
                label : get_dbuilder_msg('dbuilder.name'),
                name  : 'name',
                type : 'label'
            },
            {
                label : get_dbuilder_msg('dbuilder.filterParam'),
                name  : 'filterParamName',
                type : 'label'
            },
            {
                label : get_dbuilder_msg('dbuilder.label'),
                name  : 'label',
                required : 'true',
                type : 'textfield'
            },
            {
                name : 'type',
                label : get_dbuilder_msg('dbuilder.type'),
                type : 'elementselect',
                value : 'org.joget.apps.datalist.lib.TextFieldDataListFilterType',
                options_ajax : '[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.datalist.model.DataListFilterType',
                url : '[CONTEXT_PATH]/web/property/json' + DatalistBuilder.appPath + '/getPropertyOptions'
            },
            {
                label : get_dbuilder_msg('dbuilder.hideFilter'),
                name  : 'hidden',
                type : 'checkbox',
                options : [{label : '', value : 'true'}]
            }]
        }];
    },

    showFilterProperties : function(columnId){
        var propertiesDefinition = DatalistBuilder.getFilterPropertiesDefinition();

        var propertyValues = DatalistBuilder.chosenFilters[columnId];
        propertyValues['filterParamName'] = DatalistBuilder.filterParam + propertyValues['name'];
        propertyValues['datalist_type'] = 'filter';
        
        var options = {
            appPath: DatalistBuilder.appPath,
            contextPath: DatalistBuilder.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : propertyValues,
            showCancelButton : true,
            changeCheckIgnoreUndefined: true,
            cancelCallback: function() {
            },
            saveCallback: function(container, properties) {
                // update element properties
                DatalistBuilder.updateFilterPropertiesCallback(columnId, properties);
            }
        }
        
        PropertyEditor.Popup.showDialog("list-property-editor", options);
    },

    updateFilterPropertiesCallback : function (columnId, properties){
        DatalistBuilder.addToUndo();
        DatalistBuilder.chosenFilters[columnId] = $.extend(DatalistBuilder.chosenFilters[columnId], properties);
        DatalistBuilder.renderFilter(columnId);
    },

    updateActionCallback : function (columnId, actions, properties) {
        DatalistBuilder.addToUndo();
        var action = actions[columnId];
        
        if (action.properties.permission_rules !== null && action.properties.permission_rules !== undefined) {
            properties.permission_rules = action.properties.permission_rules;
        }
        if (action.properties.hidden !== null && action.properties.hidden !== undefined) {
            properties.hidden = action.properties.hidden;
        }
        
        action.properties = properties;
        if( columnId.indexOf('rowAction') != -1 ){
            DatalistBuilder.renderRowAction(columnId);
        }else{
            DatalistBuilder.renderAction(columnId);
        }
    },

    deleteFilter : function(columnId){
        DatalistBuilder.addToUndo();
        delete DatalistBuilder.chosenFilters[columnId];
        $('.databuilderFilter#' + columnId).remove();
        DatalistBuilder.adjustCanvas();
    },

    deleteColumn : function(columnId){
        DatalistBuilder.addToUndo();
        delete DatalistBuilder.chosenColumns[columnId];
        $('.databuilderItem#' + columnId).remove();
        DatalistBuilder.adjustCanvas();
    },

    deleteAction : function(columnId){
        DatalistBuilder.addToUndo();
        delete DatalistBuilder.chosenActions[columnId];
        $('.databuilderAction#' + columnId).remove();
        DatalistBuilder.adjustCanvas();
    },

    deleteRowAction : function(columnId){
        DatalistBuilder.addToUndo();
        delete DatalistBuilder.chosenRowActions[columnId];
        $('.databuilderRowAction#' + columnId).remove();
        DatalistBuilder.adjustCanvas();
    },

    getDatalistPropertiesDefinition : function() {
        return [
            {title: get_dbuilder_msg('dbuilder.basicProperties'),
              helplink: get_dbuilder_msg('dbuilder.basicProperties.helplink'),
              properties : [
                {label : get_dbuilder_msg('dbuilder.datalistId'),
                  name  : 'id',
                  required : 'true',
                  type : 'readonly'},
                {label : get_dbuilder_msg('dbuilder.datalistName'),
                  name  : 'name',
                  required : 'true',
                  type : 'textfield'},
                {label : get_dbuilder_msg('dbuilder.hidePageSizeSelector'),
                  name  : 'hidePageSize',
                  type : 'checkbox',
                  options : [{label : '', value : 'true'}]},
                {label : get_dbuilder_msg('dbuilder.pageSizeSelectorOptions'),
                  name  : 'pageSizeSelectorOptions',
                  required : 'true',
                  value : '10,20,30,40,50,100',
                  type : 'textfield',
                  control_field: 'hidePageSize',
                  control_value: '',
                  control_use_regex: 'false'},
                {label : get_dbuilder_msg('dbuilder.pageSize'),
                  name  : 'pageSize',
                  required : 'true',
                  type : 'selectbox',
                  value : '0',
                  options_callback : function(props, values) {
                      var options = [{label : get_dbuilder_msg('dbuilder.pageSize.default'), value : '0'}];
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
                  options_callback_on_change : "pageSizeSelectorOptions"},
                {label : get_dbuilder_msg('dbuilder.order'),
                  name  : 'order',
                  required : 'false',
                  type : 'selectbox',
                  options : [{label : '', value : ''},
                             {label : get_dbuilder_msg('dbuilder.order.asc'), value : '2'},
                             {label : get_dbuilder_msg('dbuilder.order.desc'), value : '1'}]},
                {label : get_dbuilder_msg('dbuilder.orderBy'),
                  name  : 'orderBy',
                  required : 'false',
                  type : 'selectbox',
                  options_callback : 'DatalistBuilder.getColumnOptions'},
                {label : get_dbuilder_msg('dbuilder.description'),
                  name  : 'description',
                  required : 'false',
                  type : 'textarea'},
                {label : get_dbuilder_msg('dbuilder.useSession'),
                  name  : 'useSession',
                  type : 'checkbox',
                  options : [{label : '', value : 'true'}]},
                {label : get_dbuilder_msg('dbuilder.showDataWhenFilterSet'),
                  name  : 'showDataWhenFilterSet',
                  type : 'checkbox',
                  options : [{label : '', value : 'true'}]},
                {label : get_dbuilder_msg('dbuilder.considerFilterWhenGetTotal'),
                  name  : 'considerFilterWhenGetTotal',
                  type : 'checkbox',
                  options : [{label : '', value : 'true'}]},
                {label: get_dbuilder_msg('dbuilder.responsive'),
                  name: 'responsive',
                  type: 'header',
                  description: get_dbuilder_msg('dbuilder.responsive.desc')},
                {label: get_dbuilder_msg('dbuilder.disableResponsive'),
                  name: 'disableResponsive',
                  type: 'checkbox',
                  options : [{value : 'true', label : ''}]},
                {label: get_dbuilder_msg('dbuilder.searchPopup'),
                  name: 'searchPopup',
                  type: 'checkbox',
                  options : [{value : 'true', label : ''}]},  
                {label: get_dbuilder_msg('dbuilder.responsiveView'),
                  name: 'responsiveView',
                  description : get_dbuilder_msg('dbuilder.responsiveView.desc'),
                  type: 'gridfixedrow',
                  columns : [
                      {key : 'view', label : get_dbuilder_msg('dbuilder.view')},
                      {key : 'breakpoint', label : get_dbuilder_msg('dbuilder.breakpoint'), type : 'number'},
                      {key : 'columns', label : get_dbuilder_msg('dbuilder.columns')}
                  ],
                  rows: [
                      {label : get_dbuilder_msg('dbuilder.mobile')},
                      {label : get_dbuilder_msg('dbuilder.tablet')}
                  ]}
              ]
            }
        ];
    },
        
    showDatalistProperties : function(){
        var propertiesDefinition = DatalistBuilder.getDatalistPropertiesDefinition();
        
        var propertyValues = DatalistBuilder.datalistProperties;

        var options = {
            appPath: DatalistBuilder.appPath,
            contextPath: DatalistBuilder.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : propertyValues,
            autoSave: true,
            changeCheckIgnoreUndefined: true,
            saveCallback : DatalistBuilder.updateDatalistProperties,
            closeAfterSaved : false
        };

        $('#properties').html("");
        $('#properties').propertyEditor(options);
    },
    
    showPopUpDatalistProperties : function(){
        var propertiesDefinition = DatalistBuilder.getDatalistPropertiesDefinition();
        
        var propertyValues = DatalistBuilder.datalistProperties;

        var options = {
            appPath: DatalistBuilder.appPath,
            contextPath: DatalistBuilder.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : propertyValues,
            showCancelButton : true,
            changeCheckIgnoreUndefined: true,
            cancelCallback: function() {
            },
            saveCallback: function(container, properties) {
                // update element properties
                DatalistBuilder.updateDatalistProperties(container, properties);
            }
        }
        
        PropertyEditor.Popup.showDialog("list-property-editor", options);
    },
    
    showPopUpDatalistBinderProperties : function(){
        var propertiesDefinition = DatalistBuilder.getBinderPropertiesDefinition();

        var propertyValues = new Array();
        propertyValues['binder'] = DatalistBuilder.binderProperties;
        
        var options = {
            appPath: DatalistBuilder.appPath,
            contextPath: DatalistBuilder.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : propertyValues,
            showCancelButton: false,
            changeCheckIgnoreUndefined: true,
            cancelCallback: function() {
            },
            saveCallback: function(container, properties) {
                var binderChanged = DatalistBuilder.binderProperties.className != properties.binder.className;
                DatalistBuilder.binderProperties = properties.binder;
                if (binderChanged) {
                    DatalistBuilder.updateBinderProperties(DatalistBuilder.UPDATE);
                } else {
                    DatalistBuilder.updateBinderProperties();
                }
                
                $('#source').html("");
                $('#source').propertyEditor(options);
            }
        };
        
        PropertyEditor.Popup.showDialog("list-property-editor", options);
    },

    updateDatalistProperties : function(container, properties){
        DatalistBuilder.addToUndo();
        DatalistBuilder.datalistProperties = $.extend(DatalistBuilder.datalistProperties, properties);
        var json = DatalistBuilder.getJson();
        $('#list-json').val(json);

        //refresh all columns
        for(e in DatalistBuilder.chosenColumns){
            DatalistBuilder.renderColumn(e);
        }
    },

    getActionPropertiesDefinition : function(className) {
        var propertiesDefinition;
        var availableAction = DatalistBuilder.availableActions[className];
        
        if (availableAction && availableAction.propertyOptions) {
            propertiesDefinition = eval("(" + availableAction.propertyOptions + ")");
            
            propertiesDefinition[0]['properties'].push(
                {
                    label : 'datalist_type',
                    name  : 'datalist_type',
                    type : 'hidden',
                    value : 'action'
                }
            );    
        } else {
            return [];
        }
        
        return propertiesDefinition;
    },

    showActionProperties : function(columnId, actions) {
        
        var action = actions[columnId];
        var propertiesDefinition = DatalistBuilder.getActionPropertiesDefinition(action.className);
        
        var propertyValues = action.properties;
        if (propertyValues !== null && propertyValues !== undefined) {
            propertyValues['datalist_type'] = 'action';
        }

        var options = {
            appPath: DatalistBuilder.appPath,
            contextPath: DatalistBuilder.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : propertyValues,
            changeCheckIgnoreUndefined: true,
            showCancelButton : true,
            cancelCallback: function() {
            },
            saveCallback: function(container, properties) {
                // update element properties
                DatalistBuilder.updateActionCallback(columnId, actions, properties);
            }
        }
        
        PropertyEditor.Popup.showDialog("list-property-editor", options);
    },
    
    getRowActionPropertiesDefinition : function(className) {
        var propertiesDefinition;
        var availableAction = DatalistBuilder.availableActions[className];
        
        if (availableAction && availableAction.propertyOptions) {
            propertiesDefinition = eval("(" + availableAction.propertyOptions + ")");
        } else {
            return [];
        }
        
        propertiesDefinition.push({
            title : get_dbuilder_msg('dbuilder.rowAction.visibility'),
            properties : [
                {
                    label : 'datalist_type',
                    name  : 'datalist_type',
                    type : 'hidden',
                    value : 'row_action'
                },
                {
                name : 'rules',
                label : get_dbuilder_msg('dbuilder.rowAction.rules'),
                type : 'grid',
                columns : [{
                    key : 'join',
                    label : get_dbuilder_msg('dbuilder.rowAction.join'),
                    options : [{
                        value : 'AND',
                        label : get_dbuilder_msg('dbuilder.rowAction.join.and')
                    },
                    {
                        value : 'OR',
                        label : get_dbuilder_msg('dbuilder.rowAction.join.or')
                    }]
                },
                {
                    key : 'field',
                    label : get_dbuilder_msg('dbuilder.rowAction.field'),
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
                    label : get_dbuilder_msg('dbuilder.rowAction.operator'),
                    options : [{
                        value : '=',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.equal')
                    },
                    {
                        value : '<>',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.notEqual')
                    },
                    {
                        value : '>',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.greaterThan')
                    },
                    {
                        value : '>=',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.greaterThanOrEqual')
                    },
                    {
                        value : '<',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.lessThan')
                    },
                    {
                        value : '<=',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.lessThanOrEqual')
                    },
                    {
                        value : 'LIKE',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.like')
                    },
                    {
                        value : 'NOT LIKE',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.notLike')
                    },
                    {
                        value : 'IN',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.in')
                    },
                    {
                        value : 'NOT IN',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.notIn')
                    },
                    {
                        value : 'IS TRUE',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.isTrue')
                    },
                    {
                        value : 'IS FALSE',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.isFalse')
                    },
                    {
                        value : 'IS EMPTY',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.isEmpty')
                    },
                    {
                        value : 'IS NOT EMPTY',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.isNotEmpty')
                    },
                    {
                        value : 'REGEX',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.regex')
                    },
                    {
                        value : 'NOT REGEX',
                        label : get_dbuilder_msg('dbuilder.rowAction.operator.notRegex')
                    }]
                },
                {
                    key : 'value',
                    label : get_dbuilder_msg('dbuilder.rowAction.value')
                }]
            }]
        });
        
        return propertiesDefinition;
    },
    
    showRowActionProperties : function(columnId, actions) {
        var action = actions[columnId];
        var propertiesDefinition = DatalistBuilder.getRowActionPropertiesDefinition(action.className);
        
        var propertyValues = action.properties;
        if (propertyValues !== null && propertyValues !== undefined) {
            propertyValues['datalist_type'] = 'row_action';
        }

        var options = {
            appPath: DatalistBuilder.appPath,
            contextPath: DatalistBuilder.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : propertyValues,
            showCancelButton : true,
            changeCheckIgnoreUndefined: true,
            cancelCallback: function() {
                PropertyEditor.Popup.hideDialog("list-property-editor");
            },
            saveCallback: function(container, properties) {
                // hide dialog
                PropertyEditor.Popup.hideDialog("list-property-editor");
                // update element properties
                DatalistBuilder.updateActionCallback(columnId, actions, properties);
            }
        }
        
        PropertyEditor.Popup.showDialog("list-property-editor", options);
    },

    getJson : function(){
        var json = new Object();
        var columns = new Array();
        var rowActions = new Array();
        var actions = new Array();
        var filters = new Array();
        var formats = new Array();

        //datalist properties
        for(e in DatalistBuilder.datalistProperties){
            json[e] = DatalistBuilder.datalistProperties[e];
        }
        json.binder = DatalistBuilder.binderProperties;

        //columns
        $("#databuilderContentColumns").children("li").each( function(){
            var e = $(this).attr("id");
            var column = DatalistBuilder.chosenColumns[e];
            columns.push(column);
        });
        json.columns = columns;

        //row actions
        $("#databuilderContentRowActions").children("li").each( function(){
            var e = $(this).attr("id");
            var rowAction = DatalistBuilder.chosenRowActions[e];
            rowActions.push(rowAction);
        });
        json.rowActions = rowActions;

        //actions
        $("#databuilderContentActions").children("li").each( function(){
            var e = $(this).attr("id");
            var action = DatalistBuilder.chosenActions[e];
            actions.push(action);
        });
        json.actions = actions;

        //filters
        $("#databuilderContentFilters").children("li").each( function(){
            var e = $(this).attr("id");
            var filter = DatalistBuilder.chosenFilters[e];
            filters.push(filter);
        });
        filters.reverse();
        json.filters = filters;

        return JSON.encode(json);
    },

    setJson : function(json, id){
        var obj = json;

        // set id
        if (id) {
            DatalistBuilder.datalistId = id;
        }
        
        DatalistBuilder.loadJson(json);

        DatalistBuilder.originalJson = DatalistBuilder.getJson();
    },
    
    loadJson : function (json) {
        var obj = json;
        
        DatalistBuilder.datalistProperties.id = DatalistBuilder.datalistId;
        obj.id = DatalistBuilder.datalistId;
        
        DatalistBuilder.binderProperties = obj.binder;
        DatalistBuilder.updateBinderProperties(DatalistBuilder.UPDATE);

        DatalistBuilder.initBinderList();
        
        //main properties
        for(e in obj){
            if(e != 'binder' && e != 'columns'){
                DatalistBuilder.datalistProperties[e] = obj[e];
            }
        }

        //load columns
        for(e in obj.columns){
            DatalistBuilder.chosenColumns[obj.columns[e].id] = obj.columns[e];
            DatalistBuilder.renderColumn(obj.columns[e].id);
        }

        //load row actions
        for(e in obj.rowActions){
            DatalistBuilder.chosenRowActions[obj.rowActions[e].id] = obj.rowActions[e];
            DatalistBuilder.renderRowAction(obj.rowActions[e].id);
        }

        //load actions
        for(e in obj.actions){
            DatalistBuilder.chosenActions[obj.actions[e].id] = obj.actions[e];
            DatalistBuilder.renderAction(obj.actions[e].id);
        }

        //load filters & reverse filter for correct order
        for(e in obj.filters.reverse()){
            DatalistBuilder.chosenFilters[obj.filters[e].id] = obj.filters[e];
            DatalistBuilder.renderFilter(obj.filters[e].id);
        }
        
        DatalistBuilder.adjustCanvas();
        
        $("#loading").remove();
    },

    preview : function(){
        $('#list-preview').attr("action", DatalistBuilder.previewUrl + DatalistBuilder.datalistProperties.id);
        $('#list-preview').attr("target", "_blank");
        $('#list-preview').submit();
    },

    save : function(){
        var json = DatalistBuilder.getJson();
        $.post(DatalistBuilder.saveUrl + DatalistBuilder.datalistProperties.id, { json :  json} , function(data) {
            var d = JSON.decode(data);
            if(d.success == true){
                DatalistBuilder.originalJson = json;
                $('#list-json-original').val(json);
                DatalistBuilder.showMessage(get_dbuilder_msg('dbuilder.saved'));
                setTimeout(function(){ 
                    DatalistBuilder.showMessage(""); 
                }, 2000);
            }else{
                alert(get_dbuilder_msg('dbuilder.errorSaving'));
            }
        }, "text");
    },

    cloneObject : function(obj){
        if(obj == null || typeof(obj) != 'object'){
            return obj;
        }
        var temp = obj.constructor();
        for(var key in obj){
            temp[key] = this.cloneObject(obj[key]);
        }
        return temp;
    },

    adjustCanvas : function(){
        var columnMinWidth = 525;
        var rowActionMinWidth = 140;
        //minimum width of the builder canvas
        minWidth = 665;

        //compute the width needed for each section
        filterWidth = 0;
        widthSet = true;
        $("#databuilderContentFilters").children("li, .ui-draggable").each( function(){
            if($(this).width() == 0) widthSet = false;
            filterWidth += $(this).width() + 20;
        });
        columnWidth = 0;
        $("#databuilderContentColumns").children("li, .ui-draggable").each( function(){
            if($(this).width() == 0) widthSet = false;
            columnWidth += $(this).width() + 5;
        });
        if(columnWidth < columnMinWidth) columnWidth = columnMinWidth;
        rowActionWidth = 0;
        $("#databuilderContentRowActions").children("li, .ui-draggable").each( function(){
            if($(this).width() == 0) widthSet = false;
            rowActionWidth += $(this).width() + 5;
        });
        if(rowActionWidth < rowActionMinWidth) rowActionWidth = rowActionMinWidth;
        actionWidth = 0;
        $("#databuilderContentActions").children("li, .ui-draggable").each( function(){
            if($(this).width() == 0) widthSet = false;
            actionWidth += $(this).width() + 5;
        });
        
        if(!widthSet){
            setTimeout('DatalistBuilder.adjustCanvas()',1000);
        }
        
        //set the outer div's width
        if(minWidth < filterWidth) minWidth = filterWidth;
        if(minWidth < (columnWidth + rowActionWidth)) minWidth = columnWidth + rowActionWidth;
        if(minWidth < actionWidth) minWidth = actionWidth;

        //set the width of each sections
        $("#databuilderContentFilters").css("width", minWidth + 20);
        $("#databuilderContentColumns").css("width", columnWidth);
        $("#databuilderContentRowActions").css("width", rowActionWidth);
        $("#databuilderContentActions").css("width", minWidth + 20);
        $("#builder-canvas").css("width", minWidth + 10);
        
        // update JSON definition
        var jsonString = DatalistBuilder.getJson();
        $('#list-json').val(jsonString).trigger("change");
    },
    
    isSaved : function(){
        if(DatalistBuilder.originalJson == DatalistBuilder.getJson()){
            return true;
        }else{
            return false;
        }
    },
    
    getCopiedElement : function() {
        var time = $.localStorage.getItem("datalistBuilder.copyTime");
        //10 mins
        if (time !== undefined && time !== null && ((new Date()) - (new Date(time))) > 3000000) {
            $.localStorage.removeItem('datalistBuilder.copyTime');
            $.localStorage.removeItem('datalistBuilder.copy');
            return null;
        }
        var copied = $.localStorage.getItem("datalistBuilder.copy");
        if (copied !== undefined && copied !== null) {
            return JSON.decode(copied);
        }
        return null;
    },
    
    copy : function(element) {
        var id = $(element).attr("id");
        var copy = new Object();
        if ($(element).hasClass("databuilderItem")) {
            copy['type'] = "column";
        } else if ($(element).hasClass("databuilderFilter")) {
            copy['type'] = "filter";
        } else if ($(element).hasClass("databuilderRowAction")) {
            copy['type'] = "rowAction";
        } else if ($(element).hasClass("databuilderAction")) {
            copy['type'] = "action";
        }
        var t = copy['type'];
        copy['object'] = DatalistBuilder['chosen' + t.charAt(0).toUpperCase() + t.slice(1) + 's'][id];
        
        $.localStorage.setItem("datalistBuilder.copy", JSON.encode(copy));
        $.localStorage.setItem("datalistBuilder.copyTime", new Date());
        DatalistBuilder.updatePasteIcon();
        DatalistBuilder.showMessage(get_dbuilder_msg('dbuilder.copied'));
        setTimeout(function(){ DatalistBuilder.showMessage(""); }, 2000);
    },
    
    paste : function() {
        var copied = DatalistBuilder.getCopiedElement();
        if (copied !== undefined && copied !== null) {
            DatalistBuilder.addToUndo();
            var t = copied['type'];
            var id = DatalistBuilder.getId(t);
            DatalistBuilder['chosen' + t.charAt(0).toUpperCase() + t.slice(1) + 's'][id] = copied['object'];
            DatalistBuilder['chosen' + t.charAt(0).toUpperCase() + t.slice(1) + 's'][id].id = id;
            
            DatalistBuilder['render' + t.charAt(0).toUpperCase() + t.slice(1)](id);
        }
    },
    
    updatePasteIcon : function() {
        $(".element-paste").addClass("disabled");
        var copied = DatalistBuilder.getCopiedElement();
        if (copied !== undefined && copied !== null) {
            if (copied['type'] === "column" && copied['type'] === "filter") {
                if (!$.inArray(copy['object'].name, DatalistBuilder.availableColumnNames)) {
                    return;
                }
            }
            $(".element-paste.paste-"+copied['type']).removeClass("disabled");
        }
    },
    
    getId : function (t) {
        var id = DatalistBuilder[t + 'Prefix'] + DatalistBuilder[t + 'IndexCounter'];
        while(!(DatalistBuilder['chosen' + t.charAt(0).toUpperCase() + t.slice(1) + 's'][id] === null || DatalistBuilder['chosen' + t.charAt(0).toUpperCase() + t.slice(1) + 's'][id] === undefined)){
            id = DatalistBuilder[t + 'Prefix'] + DatalistBuilder[t + 'IndexCounter'];
            DatalistBuilder[t + 'IndexCounter']++;
        }
        return id;
    },
    
    //Undo the changes from stack
    undo : function(){
        if(DatalistBuilder.undoStack.length > 0){
            //if redo stack is full, delete first
            if(DatalistBuilder.redoStack.length >= DatalistBuilder.undoRedoMax){
                DatalistBuilder.redoStack.splice(0,1);
            }

            //save current json data to redo stack
            DatalistBuilder.redoStack.push(DatalistBuilder.getJson());

            //undo-ing
            var loading = $('<div id="loading"><i class="fas fa-spinner fa-spin fa-2x"></i> ' + get_dbuilder_msg("dbuilder.label.undoing") + '</div>');
            $("body").append(loading);
            DatalistBuilder.loadJson(JSON.decode(DatalistBuilder.undoStack.pop()));
            
            //enable redo button if it is disabled previously
            if(DatalistBuilder.redoStack.length === 1){
                $('.action-redo').removeClass('disabled');
                $('.action-redo').attr('title', get_dbuilder_msg('dbuilder.redo.tip'));
            }

            //if undo stack is empty, disabled undo button
            if(DatalistBuilder.undoStack.length === 0){
                $('.action-undo').addClass('disabled');
                $('.action-undo').attr('title', get_dbuilder_msg('dbuilder.undo.disabled.tip'));
            }
        }
    },

    //Redo the changes from stack
    redo : function(){
        if(DatalistBuilder.redoStack.length > 0){
            //if undo stack is full, delete first
            if(DatalistBuilder.undoStack.length >= DatalistBuilder.undoRedoMax){
                DatalistBuilder.undoStack.splice(0,1);
            }

            //save current json data to undo stack
            DatalistBuilder.undoStack.push(DatalistBuilder.getJson());

            //redo-ing
            var loading = $('<div id="loading"><i class="fas fa-spinner fa-spin fa-2x"></i> ' + get_dbuilder_msg("dbuilder.label.redoing") + '</div>');
            $("body").append(loading);
            DatalistBuilder.loadJson(JSON.decode(DatalistBuilder.redoStack.pop()));

            //enable undo button if it is disabled previously
            if(DatalistBuilder.undoStack.length == 1){
                $('.action-undo').removeClass('disabled');
                $('.action-undo').attr('title', get_dbuilder_msg('dbuilder.undo.tip'));
            }

            //if redo stack is empty, disabled redo button
            if(DatalistBuilder.redoStack.length == 0){
                $('.action-redo').addClass('disabled');
                $('.action-redo').attr('title', get_dbuilder_msg('dbuilder.redo.disabled.tip'));
            }
        }
    },

    //Add changes info to stack
    addToUndo : function(json){
        //if undo stack is full, delete first
        if(DatalistBuilder.undoStack.length >= DatalistBuilder.undoRedoMax){
            DatalistBuilder.undoStack.splice(0,1);
        }
        
        if (json === undefined || json === null) {
            json = DatalistBuilder.getJson();
        }

        //save current json data to undo stack
        DatalistBuilder.undoStack.push(json);

        //enable undo button if it is disabled previously
        if(DatalistBuilder.undoStack.length === 1){
            $('.action-undo').removeClass('disabled');
            $('.action-undo').attr('title', get_dbuilder_msg('dbuilder.undo.tip'));
        }
    },
    
    getColumnOptions : function(properties) {
        //populate list items
        var tempArray = [{'label':'','value':''}];
        for(ee in DatalistBuilder.availableColumns){
            var temp = {'label' : UI.escapeHTML(DatalistBuilder.availableColumns[ee].label),
                         'value' : DatalistBuilder.availableColumns[ee].id};
            tempArray.push(temp);
        }
        return tempArray;
    },
    
    showMessage: function(message) {
        if (message && message != "") {
            $("#builder-message").html(message);
            $("#builder-message").fadeIn();
        } else {
            $("#builder-message").fadeOut();
        }
    },
    
    updateList: function () {
        var json = $('#list-json').val();
        if (DatalistBuilder.getJson() !== json) {
            DatalistBuilder.addToUndo();
        }
        DatalistBuilder.loadJson(JSON.decode(json));

        return false;
    },

    showDiff : function (callback, output) {
        var jsonUrl = DatalistBuilder.contextPath + '/web/json/console/app/' + DatalistBuilder.appId + '/' + DatalistBuilder.appVersion + '/datalist/' + DatalistBuilder.datalistProperties.id + '/json';
        var thisObject = this;
        var merged;
        var currentSaved;
        $.ajax({
            type: "GET",
            url: jsonUrl,
            dataType: 'json',
            success: function (data) {
                var current = data;
                var currentString = JSON.stringify(data);
                currentSaved = currentString;
                $('#list-json-current').val(currentString);
                var original = JSON.decode($('#list-json-original').val());
                var latest = JSON.decode($('#list-json').val());
                merged = DiffMerge.merge(original, current, latest, output);
            },
            error: function() {
                currentSaved = $('#list-json-current').val();
                merged = $('#list-json').val();
            },
            complete: function() {
                if (callback) {
                    callback.call(thisObject, currentSaved, merged);
                }
            }
        });
    },
            
    merge: function (callback) {
        // get current remote definition
        DatalistBuilder.showMessage(get_dbuilder_msg('dbuilder.merging'));
        var thisObject = this;
        
        DatalistBuilder.showDiff(function (currentSaved, merged) {
            if (currentSaved !== undefined && currentSaved !== "") {
                $('#list-json-original').val(currentSaved);
            }
            if (merged !== undefined && merged !== "") {
                $('#list-json').val(merged);
            }
            DatalistBuilder.updateList();
            DatalistBuilder.showMessage("");
            
            if (callback) {
                callback.call(thisObject, merged);
            }
        });
    },
    
    mergeAndSave: function() {
        DatalistBuilder.merge(DatalistBuilder.save);
    }    
}
