DatalistBuilder = {

    UPDATE : 'Update',

    //Configuration
    tinymceUrl : '',
    saveUrl : '',
    previewUrl : '',
    contextPath : '',
    appPath : '',
    originalJson : '',

    columnPrefix : "column_",
    rowActionPrefix : "rowAction_",
    actionPrefix : "action_",
    filterPrefix : "filter_",
    availableColumns : [],      //available columns
    availableActions : [],      //available actions
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
        pageSize : 10,
        order : '',
        orderBy : '',
        actions : [],
        rowActions : [],
        filters : []
    },                          //datalist's properties

    propertyDialog: null,                       // property dialog popup object
    elementPropertyDefinitions: new Object(),   // map of property dialog definitions for each element class
    
    init : function(){
        DatalistBuilder.initBinderList();
        DatalistBuilder.initActionList();

        //bind click event
        //$('#builder-steps-properties').click( function(){
            //DatalistBuilder.showMainProperties();
        //});

        //refresh main properties
        //DatalistBuilder.showDatalistProperties();
    },

    initBinderList : function(){
        //get the list of binders available
        $.getJSON(
            DatalistBuilder.contextPath + '/web/json/console/app' + DatalistBuilder.appPath + '/builder/binders',
            function(returnedData) {
                binderList = returnedData.binders;
                binderListArray = [];
                for(i in binderList){
                    temp = {
                        'label' : binderList[i].name,
                        'value' : binderList[i].className
                    };
                    binderListArray.push(temp);
                }

                propertiesDefinition = [
                    {title: get_dbuilder_msg('dbuilder.selectBinder'),
                        properties : [{
                            name : 'binder',
                            label : get_dbuilder_msg('dbuilder.selectDataSource'),
                            type : 'elementselect',
                            options : binderListArray,
                            url : '[CONTEXT_PATH]/web/json/console/app' + DatalistBuilder.appPath + '/builder/binder/options?id=' + DatalistBuilder.datalistProperties.id 
                        }]
                    }
                ];

                propertyValues = {
                    'binder' : {
                        'className' : DatalistBuilder.binderProperties.className,
                        'properties' : DatalistBuilder.binderProperties.properties
                    }
                };

                var options = {
                    tinyMceScript: DatalistBuilder.tinymceUrl,
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
        });
    },

    updateBinderProperties : function(mode){
        var temp = {};
        for(e in DatalistBuilder.binderProperties.properties){
            temp['binder_' + e] = DatalistBuilder.binderProperties.properties[e];
        }
        $.getJSON(
            DatalistBuilder.contextPath + '/web/json/console/app' + DatalistBuilder.appPath + '/builder/binder/columns?' + $.param(temp),
            {   id: DatalistBuilder.datalistProperties.id,
                binderId : DatalistBuilder.binderProperties.className
            },
            DatalistBuilder.updateBinderPropertiesCallBack
        );
        
        if(mode == DatalistBuilder.UPDATE){
            //reset all fields
            DatalistBuilder.chosenColumns = [];
            DatalistBuilder.chosenActions = [];
            DatalistBuilder.chosenRowActions = [];
            DatalistBuilder.chosenFilters = [];
            DatalistBuilder.columnIndexCounter = 0;
            DatalistBuilder.rowActionIndexCounter = 0;
            DatalistBuilder.filterIndexCounter = 0;
            DatalistBuilder.actionIndexCounter = 0;

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
        var fields = new Array();
        
        for(e in columns){
            column = columns[e];
            temp = {
                "action"     : '',
                "href"       : '',
                "hrefParam"  : '',
                "filterable" : column.filterable.toString(),
                "id"         : column.name.toString(),
                "label"      : column.label.toString(),
                "name"       : column.name.toString(),
                "sortable"   : column.sortable.toString()
            }
            fields[temp.id] = temp;
        }
        DatalistBuilder.availableColumns = fields;

        //call the decorator to populate fields
        DatalistBuilder.initFields();

        //attach events to the columns
        DatalistBuilder.initEvents();

        //change to designer's tab
        $("#builder-steps-designer").trigger("click");
    },

    initFields : function(){
        //populate column palette
        fields = DatalistBuilder.availableColumns;
        $('#builder-palettle-items').html('');
        for(var e in fields){
            var field = fields[e];
            var element = '<li><div class="builder-palette-column" id="' + field.id + '"><label class="label">' + field.label + '</label></div></li>';
            $('#builder-palettle-items').append(element);
        }
    },

    initActionList : function(){
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
            }
        );
    },

    initActions : function(){
        //populate action palette
        actions = DatalistBuilder.availableActions;
        $('#builder-palettle-actions').html('');
        for(var e in actions){
            var action = actions[e];
            var element = '<li><div class="builder-palette-action" id="' + action.className + '"><label class="label">' + action.label + '</label></div></li>';
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
        $('.builder-palette-column').draggable({
            opacity: 0.7,
            helper: "clone",
            zIndex: 100
        });

        //columns
        $('#databuilderContentColumns').droppable({
            accept: '.builder-palette-column',
            activeClass: 'form-cell-highlight',
            greedy: true,
            drop: function(event, ui){
                //get the field id
                var id = $(ui.draggable).attr('id');
                var columnId = DatalistBuilder.columnPrefix + DatalistBuilder.columnIndexCounter;
                while(DatalistBuilder.chosenColumns[columnId] != null){
                    columnId = DatalistBuilder.columnPrefix + DatalistBuilder.columnIndexCounter;
                    DatalistBuilder.columnIndexCounter++;
                }
                DatalistBuilder.chosenColumns[columnId] = DatalistBuilder.cloneObject(DatalistBuilder.availableColumns[id]);
                DatalistBuilder.chosenColumns[columnId].id = columnId;
                DatalistBuilder.chosenColumns[columnId].name = id;

                //call decorator to render column in the designer's canvas
                DatalistBuilder.renderColumn(columnId);
            }
        });

        //filters
        $('#databuilderContentFilters').droppable({
            accept: '.builder-palette-column',
            activeClass: 'form-cell-highlight',
            greedy: true,
            drop: function(event, ui){
                //get the field id
                var id = $(ui.draggable).attr('id');
                var columnId = DatalistBuilder.filterPrefix + DatalistBuilder.filterIndexCounter;
                while(DatalistBuilder.chosenFilters[columnId] != null){
                    columnId = DatalistBuilder.filterPrefix + DatalistBuilder.filterIndexCounter;
                    DatalistBuilder.filterIndexCounter++;
                }
                temp = DatalistBuilder.cloneObject(DatalistBuilder.availableColumns[id]);
                DatalistBuilder.chosenFilters[columnId] = {};
                DatalistBuilder.chosenFilters[columnId].id = columnId;
                DatalistBuilder.chosenFilters[columnId].name = id;
                DatalistBuilder.chosenFilters[columnId].label = temp.label;

                //call decorator to render column in the designer's canvas
                DatalistBuilder.renderFilter(columnId);
            }
        });

        //row actions and actions drag
        $('.builder-palette-action').draggable({
            opacity: 0.7,
            helper: "clone",
            zIndex: 20000
        });

        //row actions
        $('#databuilderContentRowActions').droppable({
            accept: '.builder-palette-action',
            activeClass: 'form-cell-highlight',
            greedy: true,
            drop: function(event, ui){
                //get the field id
                var id = $(ui.draggable).attr('id');
                var columnId = DatalistBuilder.rowActionPrefix + DatalistBuilder.rowActionIndexCounter;
                while(DatalistBuilder.chosenRowActions[columnId] != null){
                    columnId = DatalistBuilder.rowActionPrefix + DatalistBuilder.rowActionIndexCounter;
                    DatalistBuilder.rowActionIndexCounter++;
                }
                DatalistBuilder.chosenRowActions[columnId] = DatalistBuilder.cloneObject(DatalistBuilder.availableActions[id]);
                DatalistBuilder.chosenRowActions[columnId].id = columnId;
                if (!DatalistBuilder.chosenRowActions[columnId].properties) {
                    DatalistBuilder.chosenRowActions[columnId].properties = new Object();
                }
                DatalistBuilder.chosenRowActions[columnId].properties.id = columnId;
                delete DatalistBuilder.chosenRowActions[columnId].propertyOptions;
                
                //call decorator to render column in the designer's canvas
                DatalistBuilder.renderRowAction(columnId);
            }
        });
        
        // actions
        $('#databuilderContentActions').droppable({
            accept: '.builder-palette-action',
            activeClass: 'form-cell-highlight',
            greedy: true,
            drop: function(event, ui){
                //get the field id
                var id = $(ui.draggable).attr('id');
                var columnId = DatalistBuilder.actionPrefix + DatalistBuilder.actionIndexCounter;
                while(DatalistBuilder.chosenActions[columnId] != null){
                    columnId = DatalistBuilder.actionPrefix + DatalistBuilder.actionIndexCounter;
                    DatalistBuilder.actionIndexCounter++;
                }
                DatalistBuilder.chosenActions[columnId] = DatalistBuilder.cloneObject(DatalistBuilder.availableActions[id]);
                DatalistBuilder.chosenActions[columnId].id = columnId;
                if (typeof DatalistBuilder.chosenActions[columnId].properties == "undefined") {
                    DatalistBuilder.chosenActions[columnId].properties = new Object();
                }
                DatalistBuilder.chosenActions[columnId].properties.id = columnId;
                delete DatalistBuilder.chosenActions[columnId].propertyOptions;

                //call decorator to render column in the designer's canvas
                DatalistBuilder.renderAction(columnId);
            }
        });

        $("#databuilderContentColumns").sortable();
        $("#databuilderContentActions").sortable();
        $("#databuilderContentRowActions").sortable();
        $("#databuilderContentFilters").sortable();
    },

    renderFilter : function(columnId){
        filter = DatalistBuilder.chosenFilters[columnId];
        count = 0;
        var string = '<li class="databuilderFilter column" id="' + columnId + '"><div class="databuilderItemTitle">' + filter.label + '</div>';
        string += '<div class="databuilderItemContent">';
        string += '</div>';

        if( $('#databuilderContentFilters #' + columnId).size() != 0 ){
            //replace existing
            $('#databuilderContentFilters #' + columnId).html(string);
        }else{
            //append if new
            $('#databuilderContentFilters').append(string);
        }
        DatalistBuilder.decorateFilter(columnId);
        DatalistBuilder.adjustCanvas();
    },

    renderAction : function(columnId){
        action = DatalistBuilder.chosenActions[columnId];
        count = 0;

        label = action.label;
        if(action.properties.label != undefined){
            label = action.properties.label;
        }

        var string = '<li class="databuilderAction column" id="' + columnId + '"><div class="databuilderItemTitle">' + label + '</div>';
        string += '<div class="databuilderItemContent">';
        string += '<input value="' + label + '" type="button">';
        string += '</div>';

        if( $('#databuilderContentActions #' + columnId).size() != 0 ){
            //replace existing
            $('#databuilderContentActions #' + columnId).html(string);
        }else{
            //append if new
            $('#databuilderContentActions').append(string);
        }
        DatalistBuilder.decorateAction(columnId);
        DatalistBuilder.adjustCanvas();
    },

    renderRowAction : function(columnId){
        rowAction = DatalistBuilder.chosenRowActions[columnId];
        label = rowAction.label;
        if(rowAction.properties.label != undefined){
            label = rowAction.properties.label;
        }

        var string = '<li class="databuilderRowAction column" id="' + columnId + '"><div class="databuilderItemTitle">' + label + '</div>';
        string += '<div class="databuilderItemContent">';
        string += '<ul>';

        count = 0;
        while(count < 6){
            var className = '';
            if(count % 2 == 0){
                className = 'even';
            }
            string += '<li class="' + className + '">' + label + '</li>';
            count++;
        }
        string += '</ul></div>';
        string += '</li>';

        if( $('#databuilderContentRowActions #' + columnId).size() != 0 ){
            //replace existing
            $('#databuilderContentRowActions #' + columnId).html(string);
        }else{
            //append if new
            $('#databuilderContentRowActions').append(string);
        }
        DatalistBuilder.decorateRowAction(columnId);
        DatalistBuilder.adjustCanvas();
    },

    renderColumn : function(columnId){
        column = DatalistBuilder.chosenColumns[columnId];
        sampleDataArray = ['Bird', 'Apple', 'Fish', 'Cat', 'Dog', 'Elephant' ];
        var sortable = '';
        if(column.name == DatalistBuilder.datalistProperties['orderBy']){
            if(DatalistBuilder.datalistProperties['order'] == '2'){
                sampleDataArray = sampleDataArray.sort();
                sortable = '(A->Z)';
            }else{
                sampleDataArray = sampleDataArray.sort().reverse();
                sortable = '(Z->A)';
            }
        }

        var string = '<li class="databuilderItem column" id="' + columnId + '"><div class="databuilderItemTitle">' + column.label + '&nbsp;' + sortable + '</div>';
        string += '<div class="databuilderItemContent">';
        string += '<ul>';

        count = 0;
        for(e in sampleDataArray){
            var className = '';
            if(count % 2 == 0){
                className = 'even';
            }
            string += '<li class="' + className + '">' + sampleDataArray[e] + '</li>';
            count++;
        }
        string += '</ul></div>';
        string += '</li>';

        if( $('#databuilderContentColumns #' + columnId).size() != 0 ){
            //replace existing
            $('#databuilderContentColumns #' + columnId).html(string);
        }else{
            //append if new
            $('#databuilderContentColumns').append(string);
        }
        DatalistBuilder.decorateColumn(columnId);
        DatalistBuilder.adjustCanvas();
    },

    decorateFilter : function(columnId){
        //attach relevant events to the column
        obj = $("#" + columnId);
        if ($(obj).children(".form-palette-options").length > 0) {
            // remove if already exists
            $(obj).children(".element-options").remove();
            $(obj).children(".element-clear").remove();
        }

        var optionHtml = "<div class='form-palette-options'>";

        if ($(obj).hasClass("column")) {
            // add buttons for section
            optionHtml += "<button class='element-delete' title='"+get_dbuilder_msg('dbuilder.delete')+"'>"+get_dbuilder_msg('dbuilder.delete')+"</button>";
        }

        optionHtml += "</div><div class='element-clear'></div>";
        var optionDiv = $(optionHtml);

        //events handling
        //=====================
        // handle delete
        $(optionDiv).children(".element-delete").click(function() {
            if (confirm(get_dbuilder_msg('dbuilder.delete.confirm'))) {
                DatalistBuilder.deleteFilter(columnId);
            }
        })

        // add option bar
        $(obj).prepend(optionDiv);
        $(obj).mouseover(function() {
            $(optionDiv).css("visibility", "visible");
        });
        $(obj).mouseout(function() {
            $(optionDiv).css("visibility", "hidden");
        });
    },

    decorateAction : function(columnId){
        //attach relevant events to the column
        obj = $("#" + columnId);
        if ($(obj).children(".form-palette-options").length > 0) {
            // remove if already exists
            $(obj).children(".form-palette-options").remove();
            $(obj).children(".element-clear").remove();
        }

        var optionHtml = "<div class='form-palette-options'>";

        if ($(obj).hasClass("column")) {
            // add buttons for action
            var action = DatalistBuilder.chosenActions[columnId];
            var propertyValues = DatalistBuilder.availableActions[action.className];
            if (propertyValues && propertyValues.propertyOptions) {
                optionHtml += "<button class='element-edit-properties' title='"+get_dbuilder_msg('dbuilder.edit')+"'>"+get_dbuilder_msg('dbuilder.edit')+"</button>";
            }            
            optionHtml += "<button class='element-delete' title='"+get_dbuilder_msg('dbuilder.delete')+"'>"+get_dbuilder_msg('dbuilder.delete')+"</button>";
        }

        optionHtml += "</div><div class='element-clear'></div>";
        var optionDiv = $(optionHtml);

        //events handling
        //=====================
        // handle edit properties label
        $(optionDiv).children(".element-edit-properties").click(function() {
            DatalistBuilder.showActionProperties(columnId, DatalistBuilder.chosenActions);
        })
        
        // handle delete
        $(optionDiv).children(".element-delete").click(function() {
            if (confirm(get_dbuilder_msg('dbuilder.delete.confirm'))) {
                DatalistBuilder.deleteAction(columnId);
            }
        })

        // add option bar
        $(obj).prepend(optionDiv);
        $(obj).mouseover(function() {
            $(optionDiv).css("visibility", "visible");
        });
        $(obj).mouseout(function() {
            $(optionDiv).css("visibility", "hidden");
        });
    },

    decorateRowAction : function(columnId){
        //attach relevant events to the column
        obj = $("#" + columnId);
        if ($(obj).children(".form-palette-options").length > 0) {
            // remove if already exists
            $(obj).children(".form-palette-options").remove();
            $(obj).children(".element-clear").remove();
        }

        var optionHtml = "<div class='form-palette-options'>";

        if ($(obj).hasClass("column")) {
            // add buttons for action
            var action = DatalistBuilder.chosenRowActions[columnId];
            var propertyValues = DatalistBuilder.availableActions[action.className];
            if (propertyValues && propertyValues.propertyOptions) {
                optionHtml += "<button class='element-edit-properties' title='"+get_dbuilder_msg('dbuilder.edit')+"'>"+get_dbuilder_msg('dbuilder.edit')+"</button>";
            }            
            optionHtml += "<button class='element-delete' title='"+get_dbuilder_msg('dbuilder.delete')+"'>"+get_dbuilder_msg('dbuilder.delete')+"</button>";
        }

        optionHtml += "</div><div class='element-clear'></div>";
        var optionDiv = $(optionHtml);

        //events handling
        //=====================
        // handle edit properties label
        $(optionDiv).children(".element-edit-properties").click(function() {
            DatalistBuilder.showActionProperties(columnId, DatalistBuilder.chosenRowActions);
        })
        
        // handle delete
        $(optionDiv).children(".element-delete").click(function() {
            if (confirm(get_dbuilder_msg('dbuilder.delete.confirm'))) {
                DatalistBuilder.deleteRowAction(columnId);
            }
        })

        // add option bar
        $(obj).prepend(optionDiv);
        $(obj).mouseover(function() {
            $(optionDiv).css("visibility", "visible");
        });
        $(obj).mouseout(function() {
            $(optionDiv).css("visibility", "hidden");
        });
    },

    decorateColumn : function(columnId){
        //attach relevant events to the column
        obj = $("#" + columnId);
        if ($(obj).children(".form-palette-options").length > 0) {
            // remove if already exists
            $(obj).children(".element-options").remove();
            $(obj).children(".element-clear").remove();
        }

        var optionHtml = "<div class='form-palette-options'>";

        if ($(obj).hasClass("editable-info")) {
            // add buttons for editable
            optionHtml += "<button class='element-edit-label' title='"+get_dbuilder_msg('dbuilder.edit')+"'>"+get_dbuilder_msg('dbuilder.edit')+"</button>";
        }else if ($(obj).hasClass("column")) {
            // add buttons for section
            optionHtml += "<button class='element-edit-properties' title='"+get_dbuilder_msg('dbuilder.properties')+"'>"+get_dbuilder_msg('dbuilder.properties')+"</button>";
            optionHtml += "<button class='element-delete' title='"+get_dbuilder_msg('dbuilder.delete')+"'>"+get_dbuilder_msg('dbuilder.delete')+"</button>";
        }

        optionHtml += "</div><div class='element-clear'></div>";
        var optionDiv = $(optionHtml);

        //events handling
        //=====================
        // handle edit properties label
        $(optionDiv).children(".element-edit-properties").click(function() {
            DatalistBuilder.showColumnProperties(columnId);
        })

        // handle delete
        $(optionDiv).children(".element-delete").click(function() {
            if (confirm(get_dbuilder_msg('dbuilder.delete.confirm'))) {
                DatalistBuilder.deleteColumn(columnId);
            }
        })

        // add option bar
        $(obj).prepend(optionDiv);
        $(obj).mouseover(function() {
            $(optionDiv).css("visibility", "visible");
        });
        $(obj).mouseout(function() {
            $(optionDiv).css("visibility", "hidden");
        });
    },

    showColumnProperties : function(columnId){

        //populate formatters available
        formatters = [];
        for( e in DatalistBuilder.availableFormats){
            temp = {  value : DatalistBuilder.availableFormats[e].className,
                      label : DatalistBuilder.availableFormats[e].name };
            formatters.push(temp);
        }

        propertiesDefinition = [{
            title : get_dbuilder_msg('dbuilder.general'),
            properties :[
            {
                label : 'ID',
                name  : 'id',
                required : true,
                type : 'hidden'
            },
            {
                label : 'Name',
                name  : 'name',
                required : true,
                type : 'hidden'
            },
            {
                label : get_dbuilder_msg('dbuilder.label'),
                name  : 'label',
                required : true,
                type : 'textfield'
            },
            {
                label : get_dbuilder_msg('dbuilder.sortable'),
                name  : 'sortable',
                required : true,
                type : 'selectbox',
                options : [{
                    label : get_dbuilder_msg('dbuilder.sortable.no'),
                    value : 'false'
                },
                {
                    label : get_dbuilder_msg('dbuilder.sortable.yes'),
                    value : 'true'
                }]
            }]
        },{
            title : get_dbuilder_msg('dbuilder.actionMapping'),
            properties : [
            {
                name : 'action',
                label : get_dbuilder_msg('dbuilder.action'),
                type : 'elementselect',
                options_ajax : '[CONTEXT_PATH]/web/json/console/app' + DatalistBuilder.appPath + '/builder/column/actions',
                url : '[CONTEXT_PATH]/web/json/console/app' + DatalistBuilder.appPath + '/builder/column/action/properties'
            }]
        },{
            title : get_dbuilder_msg('dbuilder.formatter'),
            properties :[
            {
                name : 'format',
                label : get_dbuilder_msg('dbuilder.formatter'),
                type : 'elementselect',
                options_ajax : '[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.datalist.model.DataListColumnFormat',
                url : '[CONTEXT_PATH]/web/property/json/getPropertyOptions'
            }]
        }];

        propertyValues = DatalistBuilder.chosenColumns[columnId];

        var options = {
            tinyMceScript: DatalistBuilder.tinymceUrl,
            contextPath: DatalistBuilder.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : propertyValues,
            closeAfterSaved : true,
            showCancelButton : true,
            cancelCallback: function() {
                DatalistBuilder.propertyDialog.hide()
            },
            saveCallback: function(container, properties) {
                // hide dialog
                DatalistBuilder.propertyDialog.hide()
                // update element properties
                DatalistBuilder.updateColumnPropertiesCallback(columnId, properties);
            }
        }

        // show popup dialog
        if (DatalistBuilder.propertyDialog == null) {
            DatalistBuilder.propertyDialog = new Boxy(
                '<div id="form-property-editor"></div>',
                {
                    title: 'Property Editor',
                    closeable: true,
                    draggable: false,
                    show: false,
                    fixed: false
                });
        }
        
        $("#form-property-editor").html("");
        DatalistBuilder.propertyDialog.show();
        $("#form-property-editor").propertyEditor(options);
        DatalistBuilder.propertyDialog.center('x');
        DatalistBuilder.propertyDialog.center('y');
    },

    updateColumnPropertiesCallback : function (columnId, properties){
        DatalistBuilder.chosenColumns[columnId] = properties;
        DatalistBuilder.renderColumn(columnId);
    },

    updateActionCallback : function (columnId, actions, properties) {
        var action = actions[columnId];
        action.properties = properties;
        if( columnId.indexOf('rowAction') != -1 ){
            DatalistBuilder.renderRowAction(columnId);
        }else{
            DatalistBuilder.renderAction(columnId);
        }
    },

    deleteFilter : function(columnId){
        delete DatalistBuilder.chosenFilters[columnId];
        $('.databuilderFilter#' + columnId).remove();
        DatalistBuilder.adjustCanvas();
    },

    deleteColumn : function(columnId){
        delete DatalistBuilder.chosenColumns[columnId];
        $('.databuilderItem#' + columnId).remove();
        DatalistBuilder.adjustCanvas();
    },

    deleteAction : function(columnId){
        delete DatalistBuilder.chosenActions[columnId];
        $('.databuilderAction#' + columnId).remove();
        DatalistBuilder.adjustCanvas();
    },

    deleteRowAction : function(columnId){
        delete DatalistBuilder.chosenRowActions[columnId];
        $('.databuilderRowAction#' + columnId).remove();
        DatalistBuilder.adjustCanvas();
    },

    showDatalistProperties : function(){
        propertiesDefinition = [
            {title: get_dbuilder_msg('dbuilder.basicProperties'),
              properties : [
                {label : get_dbuilder_msg('dbuilder.datalistId'),
                  name  : 'id',
                  required : true,
                  type : 'readonly'},
                {label : get_dbuilder_msg('dbuilder.datalistName'),
                  name  : 'name',
                  required : true,
                  type : 'textfield'},
                {label : get_dbuilder_msg('dbuilder.pageSize'),
                  name  : 'pageSize',
                  required : true,
                  type : 'textfield'},
                {label : get_dbuilder_msg('dbuilder.order'),
                  name  : 'order',
                  required : true,
                  type : 'selectbox',
                  options : [{label : get_dbuilder_msg('dbuilder.order.asc'), value : '2'},
                              {label : get_dbuilder_msg('dbuilder.order.desc'), value : '1'}]},
                {label : get_dbuilder_msg('dbuilder.orderBy'),
                  name  : 'orderBy',
                  required : true,
                  type : 'selectbox'}
              ]
            }
        ];

        //populate the propertiesDefinition with values
        for(e in propertiesDefinition[0].properties){
            var propertyName = propertiesDefinition[0].properties[e].name;
            propertiesDefinition[0].properties[e].value = DatalistBuilder.datalistProperties[propertyName];

            if(propertyName == 'orderBy'){
                //populate list items
                var tempArray = [];
                for(ee in DatalistBuilder.availableColumns){
                    var temp = {'label' : DatalistBuilder.availableColumns[ee].label,
                                 'value' : DatalistBuilder.availableColumns[ee].id};
                    tempArray.push(temp);
                }
                propertiesDefinition[0].properties[e].options = tempArray;
            }
        }

        var options = {
            propertiesDefinition : propertiesDefinition,
            showCancelButton : true,
            cancelCallback : DatalistBuilder.showDatalistProperties,
            saveCallback : DatalistBuilder.updateDatalistProperties,
            closeAfterSaved : false
        }

        $('#properties').html("");
        $('#properties').propertyEditor(options);
    },

    updateDatalistProperties : function(container, properties){
        DatalistBuilder.datalistProperties = properties;

        //refresh all columns
        for(e in DatalistBuilder.chosenColumns){
            DatalistBuilder.renderColumn(e);
        }
        //change to designer's tab
        $("#builder-steps-designer").trigger("click");
    },

    showActionProperties : function(columnId, actions) {
        var propertiesDefinition;
        var action = actions[columnId];
        var availableAction = DatalistBuilder.availableActions[action.className];
        if (availableAction && availableAction.propertyOptions) {
            propertiesDefinition = eval("(" + availableAction.propertyOptions + ")");
        } else {
            return;
        }
        var propertyValues = action.properties;

        var options = {
            tinyMceScript: DatalistBuilder.tinymceUrl,
            contextPath: DatalistBuilder.contextPath,
            propertiesDefinition : propertiesDefinition,
            propertyValues : propertyValues,
            closeAfterSaved : true,
            showCancelButton : true,
            cancelCallback: function() {
                DatalistBuilder.propertyDialog.hide()
            },
            saveCallback: function(container, properties) {
                // hide dialog
                DatalistBuilder.propertyDialog.hide()
                // update element properties
                DatalistBuilder.updateActionCallback(columnId, actions, properties);
            }
        }

        // show popup dialog
        if (DatalistBuilder.propertyDialog == null) {
            DatalistBuilder.propertyDialog = new Boxy(
                '<div id="form-property-editor"></div>',
                {
                    title: 'Property Editor',
                    closeable: true,
                    draggable: false,
                    show: false,
                    fixed: false
                });
        }
        $("#form-property-editor").html("");
        DatalistBuilder.propertyDialog.show();
        $("#form-property-editor").propertyEditor(options);
        DatalistBuilder.propertyDialog.center('x');
        DatalistBuilder.propertyDialog.center('y');
    },

    getJson : function(){
        json = {};
        columns = [];
        rowActions = [];
        actions = [];
        filters = [];
        formats = [];

        //datalist properties
        for(e in DatalistBuilder.datalistProperties){
            json[e] = DatalistBuilder.datalistProperties[e];
        }
        json.binder = {
            'name'          : '',
            'className'     : DatalistBuilder.binderProperties.className,
            'properties'    : DatalistBuilder.binderProperties.properties
        }


        //columns
        $("#databuilderContentColumns").children().each( function(){
            e = $(this).attr("id");
            temp = DatalistBuilder.chosenColumns[e];

            //re-structure the column object to comply with datalist interpreter
            column = {};
            for(ee in temp){
                if(ee == 'action' || ee == 'format'){
                    if (temp[ee] != null) {
                        column[ee] = temp[ee];
                    }
                }else{
                    column[ee] = temp[ee];
                }
            }
            columns.push(column);
        });
        json.columns = columns;

        //row actions
        $("#databuilderContentRowActions").children().each( function(){
            e = $(this).attr("id");
            temp = DatalistBuilder.chosenRowActions[e];
            rowActions.push(temp);
        });
        json.rowActions = rowActions;

        //actions
        $("#databuilderContentActions").children().each( function(){
            e = $(this).attr("id");
            temp = DatalistBuilder.chosenActions[e];
            actions.push(temp);
        });
        json.actions = actions;

        //filters
        //for(e in DatalistBuilder.chosenFilters){
        $("#databuilderContentFilters").children().each( function(){
            e = $(this).attr("id");
            temp = DatalistBuilder.chosenFilters[e];
            filters.unshift(temp);
        });
        json.filters = filters;

        return JSON.encode(json);
    },

    setJson : function(json, id){
        var obj = json;

        DatalistBuilder.binderProperties = obj.binder;
        DatalistBuilder.init();
        DatalistBuilder.updateBinderProperties("");

        //main properties
        for(e in obj){
            if(e != 'binder' && e != 'columns'){
                DatalistBuilder.datalistProperties[e] = obj[e];
            }
        }
        
        // set id
        if (id) {
            DatalistBuilder.datalistProperties.id = id;
        }

        //load columns
        for(e in obj.columns){
            formats = "";
            for(ee in obj.columns[e].formats){
                formats = obj.columns[e].formats[ee].className + ";";
            }
            formats.substr(0, formats.length-1);
            DatalistBuilder.chosenColumns[obj.columns[e].id] = { id     : obj.columns[e].id,
                                                                 name   : obj.columns[e].name,
                                                                 label  : obj.columns[e].label,
                                                                 sortable   : obj.columns[e].sortable,
                                                                 filterable : obj.columns[e].filterable,
                                                                 type   : obj.columns[e].type,
                                                                 action  : obj.columns[e].action,
                                                                 formats : formats };
            DatalistBuilder.renderColumn(obj.columns[e].id);
        }

        //load row actions
        for(e in obj.rowActions){
            DatalistBuilder.chosenRowActions[obj.rowActions[e].id] = {id            : obj.rowActions[e].id,
                                                                      className     : obj.rowActions[e].className,
                                                                      type          : obj.rowActions[e].type,
                                                                      properties    : obj.rowActions[e].properties,
                                                                      name          : obj.rowActions[e].name,
                                                                      label         : obj.rowActions[e].label };
            DatalistBuilder.renderRowAction(obj.rowActions[e].id);
        }

        //load actions
        for(e in obj.actions){
            DatalistBuilder.chosenActions[obj.actions[e].id] = {id          : obj.actions[e].id,
                                                                className   : obj.actions[e].className,
                                                                type        : obj.actions[e].type,
                                                                properties  : obj.actions[e].properties,
                                                                name        : obj.actions[e].name,
                                                                label       : obj.actions[e].label };
            DatalistBuilder.renderAction(obj.actions[e].id);
        }

        //load filters
        for(e in obj.filters){
            DatalistBuilder.chosenFilters[obj.filters[e].id] = {id          : obj.filters[e].id,
                                                                label       : obj.filters[e].label,
                                                                name        : obj.filters[e].name};
            DatalistBuilder.renderFilter(obj.filters[e].id);
        }

        DatalistBuilder.originalJson = DatalistBuilder.getJson();
    },

    preview : function(){
        $('#list-preview').attr("action", DatalistBuilder.previewUrl + DatalistBuilder.datalistProperties.id);
        $('#list-preview').attr("target", "_blank");
        $('#list-preview').submit();
    },

    save : function(){
        $.post(DatalistBuilder.saveUrl + DatalistBuilder.datalistProperties.id, { json : DatalistBuilder.getJson() } , function(data) {
            var d = JSON.decode(data);
            if(d.success == true){
                DatalistBuilder.originalJson = DatalistBuilder.getJson();
                alert(get_dbuilder_msg('dbuilder.saved'));
            }else{
                alert(get_dbuilder_msg('dbuilder.errorSaving'));
            }
        });
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
        //define size of each div's child
        var filterSize = 95;
        var columnSize = 135;
        var rowActionSize = 105;
        var actionSize = 105;

        var columnMinWidth = 525;
        var rowActionMinWidth = 140;

        //get the number of items in each div
        filterCount = $("#databuilderContentFilters").children().size();
        columnCount = $("#databuilderContentColumns").children().size();
        rowActionCount = $("#databuilderContentRowActions").children().size();
        actionCount = $("#databuilderContentActions").children().size();

        //minimum width of the builder canvas
        minWidth = 665;

        //compute the width needed for each section
        filterWidth 	= filterCount * filterSize;
        columnWidth 	= columnCount * columnSize;
        if(columnWidth < columnMinWidth) columnWidth = columnMinWidth;
        rowActionWidth 	= rowActionCount * rowActionSize;
        if(rowActionWidth < rowActionMinWidth) rowActionWidth = rowActionMinWidth;
        actionWidth 	= actionCount * actionSize;

        //set the outer div's width
        if(minWidth < filterWidth) minWidth = filterWidth;
        if(minWidth < (columnWidth + rowActionWidth)) minWidth = columnWidth + rowActionWidth;
        if(minWidth < actionWidth) minWidth = actionWidth;

        //set the width of each sections
        $("#databuilderContentFilters").css("width", minWidth + 13);
        $("#databuilderContentColumns").css("width", columnWidth);
        $("#databuilderContentRowActions").css("width", rowActionWidth);
        $("#databuilderContentActions").css("width", minWidth + 13);
        $("#builder-canvas").css("width", minWidth + 10);
        
        // update JSON definition
        var jsonString = '(' + DatalistBuilder.getJson() + ')';
        $('#list-json').val(jsonString);
    },
    
    isSaved : function(){
        if(DatalistBuilder.originalJson == DatalistBuilder.getJson()){
            return true;
        }else{
            return false;
        }
    }
}
