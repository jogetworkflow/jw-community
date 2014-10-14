UserviewBuilder = {
    //Configuration
    tinymceUrl : '',
    saveUrl : '',
    previewUrl : '',
    contextPath : '/jw',
    appId: '',
    userviewUrl: '',
    
    //undo & redo feature
    undoStack : new Array(),
    redoStack : new Array(),
    undoRedoMax : 50,

    //Data storing
    data : new Object(),
    categoriesPointer : new Array(),
    menusPointer : new Array(),

    //Property Options
    settingPropertyOptions : new Array(),
    categoryPropertyOptions : new Array(),
    menuTypes : new Array(),

    //Tracker
    isCtrlKeyPressed : false,
    saveChecker : 0,

    //Popup dialog object for property editor
    editorDialog: null,

    //Initial data storing model of userview
    initDefaultUserviewDataModel : function(id){
        var d = new Object();
        d['className'] = "org.joget.apps.userview.model.Userview";
        d['properties'] = new Object();
        d.properties['id'] = id;
        d.properties['name'] = "Userview Name";
        d.properties['description'] = "Userview Description";
        d.properties['welcomeMessage'] = "Welcome Message";
        d.properties['logoutText'] = "Logout";
        d.properties['footerMessage'] = "Powered by Joget",
        d['setting'] = new Object();
        d.setting['className'] = "org.joget.apps.userview.model.UserviewSetting";
        d.setting['properties'] = new Array();
        d['categories'] = new Array();

        this.data = d;
    },

    //Initial Setting avaiabled property options
    initSettingPropertyOptions : function(propertyOptions){
        this.settingPropertyOptions = propertyOptions;
    },

    //Initial Category avaiabled property options
    initCategoryPropertyOptions : function(propertyOptions){
        this.categoryPropertyOptions = propertyOptions;
    },

    //Initial Menu Type property options
    initMenuType : function(category, className, label, icon, propertyOptions){
        this.menuTypes[className] = new Object();
        this.menuTypes[className]['label'] = label;
        this.menuTypes[className]['propertyOptions'] = propertyOptions;

        var iconPath = "/images/v3/builder/sidebar_element.gif";
        if(icon != ""){
            iconPath = icon;
        }
        //get category
        var categoryId = category.replace(/\s/g , "-");
        if($('ul#'+categoryId).length == 0){
            $('#builder-palette-body').append('<h3>'+category+'</h3><ul id="'+categoryId+'"></ul>');
        }

        $('ul#'+categoryId).append('<li><div id="'+className+'" element="'+className+'" class="builder-palette-element"><img src="' + this.contextPath + iconPath + '" border="0" align="left" /><label class="label">'+label+'</label></div></li>');
    },

    //Initial Builder feature
    initBuilder : function(){
        //Popup dialog
        this.editorDialog = new Boxy('<div class="menu-wizard-container"></div>', {closeable: false,draggable:false,show:false,fixed: false});

        //Shortcut key
        $(document).keyup(function (e) {
            if(e.which == 17){
                UserviewBuilder.isCtrlKeyPressed=false;
            }
        }).keydown(function (e) {
            if(e.which == 17){
                UserviewBuilder.isCtrlKeyPressed=true;
            }
            if(e.which == 83 && UserviewBuilder.isCtrlKeyPressed == true) { //CTRL+S - save
		UserviewBuilder.save();
		return false;
            }
            if(e.which == 90 && UserviewBuilder.isCtrlKeyPressed == true) { //CTRL+Z - undo
		UserviewBuilder.undo();
		return false;
            }
            if(e.which == 89 && UserviewBuilder.isCtrlKeyPressed == true) { //CTRL+Y - redo
		UserviewBuilder.redo();
		return false;
            }
            if(e.which == 80 && UserviewBuilder.isCtrlKeyPressed == true) { //CTRL+P - preview
		UserviewBuilder.preview();
		return false;
            }
        });

        //Steps tab
        $('#builder-steps li').click( function(){
            var div = $(this).find('a').attr('href');
            if($(div).size() > 0){
                $('#builder-steps li').removeClass("active");
                $('#builder-steps li').removeClass("next");
                $(this).addClass("active");
                $(this).prev().addClass("next");
                $('#builder-content').children().hide();
                $(div).show();
            }
            return false;
        });

        //setting property page
        if(UserviewBuilder.data.setting == undefined){
            UserviewBuilder.data.setting = new Object();
            UserviewBuilder.data.setting['className'] = "org.joget.apps.userview.model.UserviewSetting";
            UserviewBuilder.data.setting['properties'] = new Array();
        }

        $("#step-setting-container").html("");
        var options = {
            contextPath: UserviewBuilder.contextPath,
            tinyMceScript: UserviewBuilder.tinymceUrl,
            propertiesDefinition : UserviewBuilder.settingPropertyOptions,
            propertyValues : UserviewBuilder.data.setting.properties,
            showCancelButton:false,
            closeAfterSaved : false,
            saveCallback: UserviewBuilder.saveSettingProperties
        }
        $('#step-setting-container').propertyEditor(options);
        $('#step-setting-container').hide();

        // make palette sections draggable
        $(".builder-palette-element").draggable({
            connectToSortable: ".menu-container",
            helper: "clone",
            zIndex: 200,
            revert: "invalid",
            cursor: "move"
        }).disableSelection();
        
        //Sortable Menu Categories
        $('#userview-sidebar').sortable({
            opacity: 0.8,
            axis: 'y',
            handle: '.category-label',
            tolerance: 'intersect',
            stop: function(event, ui){
                UserviewBuilder.addToUndo();
                var id= $(ui.item[0]).attr('id');
                var category = UserviewBuilder.data.categories[UserviewBuilder.categoriesPointer[id]];
                delete UserviewBuilder.data.categories[UserviewBuilder.categoriesPointer[id]];

                if($(ui.item[0]).next().length == 0){
                    UserviewBuilder.data.categories.push(category);
                }else{
                    var nextCategoryId = $(ui.item[0]).next().attr('id');
                    var newPosition = UserviewBuilder.categoriesPointer[nextCategoryId];
                    UserviewBuilder.data.categories.splice(newPosition,0, category);
                }

                UserviewBuilder.updateCategoriesAndMenusPointer();
            }
        });

        //Editable Label properties
        $('.editable').editable(function(value, settings){
            var id = $(this).attr('id');
            UserviewBuilder.addToUndo();
            UserviewBuilder.data.properties[id] = value;
            if(value==""){
                value = get_ubuilder_msg('ubuilder.editable.noValue');
            }
            UserviewBuilder.adjustJson();
            value = UI.escapeHTML(value);
            return value;
        },{
            type      : 'text',
            tooltip   : get_ubuilder_msg('ubuilder.editable.tooltip') ,
            select    : true ,
            style     : 'inherit',
            cssclass  : 'LabelEditableField',
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
    },

    //Generate userview builder element based on json
    loadUserview : function(id, json){
        if(json == null){
            this.initDefaultUserviewDataModel(id);
        }else{
            this.data = json;
            this.data.properties.id = id;
            UserviewBuilder.adjustJson();
        }

        $('#header-name span').html(UI.escapeHTML(this.data.properties.name));
        this.decorateElementOptions($('#header-name'));
        $('#header-description span').html(UI.escapeHTML(this.data.properties.description));
        this.decorateElementOptions($('#header-description'));
        $('#header-welcome-message span').html(UI.escapeHTML(this.data.properties.welcomeMessage));
        this.decorateElementOptions($('#header-welcome-message'));
        $('#header-logout-text span').html(UI.escapeHTML(this.data.properties.logoutText));
        this.decorateElementOptions($('#header-logout-text'));
        $('#footer-message span').html(UI.escapeHTML(this.data.properties.footerMessage));
        this.decorateElementOptions($('#footer-message'));

        //empty sidebar and put label on it
        this.decorateSidebar();

        //generate category & menu based on data
        for(c in this.data.categories){
            var category = this.data.categories[c];
            this.categoriesPointer[category.properties.id] = c;
            $('#userview-sidebar').append('<div id="'+category.properties.id+'" class="category"></div>');
            var categoryObject = $('#userview-sidebar').find('#'+category.properties.id);
            $(categoryObject).html(UserviewBuilder.getCategoryModel());
            $(categoryObject).find('.category-label span').html(UI.escapeHTML(category.properties.label));
            this.attachCategoryLabelEditableEvent($(categoryObject).find('.category-label .category-label-editable'));
            this.attachCategoryMenuSortableEvent($(categoryObject).find('.menu-container'));

            this.decorateCategory(categoryObject);
            for(m in category.menus){
                var menu = category.menus[m];
                this.menusPointer[menu.properties.id] = new Object();
                this.menusPointer[menu.properties.id].categoryId = category.properties.id;
                this.menusPointer[menu.properties.id].position = m;

                $(categoryObject).find('.menu-container').append('<div id="'+menu.properties.id+'" class="menu"></div>');
                var menuObject =  $(categoryObject).find('.menu-container #'+menu.properties.id);
                $(menuObject).html(UserviewBuilder.getMenuModel());
                $(menuObject).find('.menu-label span').html(UI.escapeHTML(menu.properties.label));
                this.decorateElementOptions(menuObject);
            }
            this.decorateElementOptions(categoryObject);
        }
        this.decorateElementOptions($('#userview-sidebar'));
    },

    addCategory : function(){
        UserviewBuilder.addToUndo();
        var category = new Object();
        category['className'] = "org.joget.apps.userview.model.UserviewCategory";
        category['properties'] = new Object();
        category['properties']['id'] = 'category-'+this.uuid();
        category['properties']['label'] = get_ubuilder_msg('ubuilder.newCategory');
        category['menus'] = new Array();

        var c = this.data.categories.length;
        this.data.categories[c] = category;

        this.categoriesPointer[category.properties.id] = c;
        $('#userview-sidebar').append('<div id="'+category.properties.id+'" class="category"></div>');
        var categoryObject = $('#userview-sidebar').find('#'+category.properties.id);
        $(categoryObject).html(UserviewBuilder.getCategoryModel());
        $(categoryObject).find('.category-label span').html(UI.escapeHTML(category.properties.label));
        this.attachCategoryLabelEditableEvent($(categoryObject).find('.category-label .category-label-editable'));
        this.attachCategoryMenuSortableEvent($(categoryObject).find('.menu-container'));
        this.decorateCategory($(categoryObject));
        this.decorateElementOptions($(categoryObject));
        UserviewBuilder.adjustJson();
    },

    deleteCategory : function(id){
        UserviewBuilder.addToUndo();
        $('#'+id).remove();
        this.data.categories.splice(this.categoriesPointer[id],1);
        delete this.categoriesPointer[id];
        this.updateCategoriesAndMenusPointer();
    },

    addMenu : function(obj){
        var id = this.uuid();
        var type = $(obj).attr('element');

        UserviewBuilder.addToUndo();
        var categoryId = $(obj).parent().parent().attr('id');

        var menu = new Object();
        menu.className = type;
        menu.properties = new Object();
        menu.properties.id=id;
        menu.properties.label = this.menuTypes[type].label;

        var category = UserviewBuilder.data.categories[UserviewBuilder.categoriesPointer[categoryId]];

        if($(obj).next().length == 0){
            category.menus.push(menu);
        }else{
            var nextMenuId = $(obj).next().attr('id');
            var nextMenu = UserviewBuilder.menusPointer[nextMenuId];
            var newPosition = (nextMenu) ? UserviewBuilder.menusPointer[nextMenuId].position : 0;
            category.menus.splice(newPosition,0, menu);
        }

        $(obj).after('<div id="'+menu.properties.id+'" class="menu"></div>');
        var menuObject = $('#'+id);
        $(menuObject).html(UserviewBuilder.getMenuModel());
        $(menuObject).find('.menu-label span').html(menu.properties.label);

        $(obj).remove();

        UserviewBuilder.decorateElementOptions(menuObject);
        UserviewBuilder.menusPointer[menu.properties.id] = new Object();
        UserviewBuilder.updateCategoriesAndMenusPointer();
    },

    editMenu : function(id){
        var menu = this.data.categories[this.categoriesPointer[this.menusPointer[id].categoryId]].menus[this.menusPointer[id].position];

        var thisObject = this;
        var options = {
            contextPath: UserviewBuilder.contextPath,
            tinyMceScript: thisObject.tinymceUrl,
            propertiesDefinition : thisObject.menuTypes[menu.className].propertyOptions,
            propertyValues : menu.properties,
            showCancelButton:true,
            saveCallback: thisObject.saveMenu,
            validationFailedCallback: thisObject.saveMenuFailed,
            cancelCallback: thisObject.cancelEditMenu
        }
        $('.menu-wizard-container').html("");
        $('.menu-wizard-container').attr('id', id);
        thisObject.editorDialog.show();
        $('.menu-wizard-container').propertyEditor(options);
        thisObject.editorDialog.center('x');
        thisObject.editorDialog.center('y');
    },

    saveMenu : function(container, properties){
        UserviewBuilder.addToUndo();
        var thisObject = UserviewBuilder;

        var id = $(container).attr('id');
        var menu = thisObject.data.categories[thisObject.categoriesPointer[thisObject.menusPointer[id].categoryId]].menus[thisObject.menusPointer[id].position];

        menu.properties = properties;
        var label = UI.escapeHTML(properties.label);
        $('#'+id+' .menu-label span').html(label);
        thisObject.editorDialog.hide();
        UserviewBuilder.adjustJson();
    },

    saveMenuFailed : function(container, returnedErrors){
        var errorMsg = get_ubuilder_msg('ubuilder.errors') + ':\n';
        for(key in returnedErrors){
            errorMsg += returnedErrors[key].fieldName + ' : ' + returnedErrors[key].message + '\n';
        }
        alert(errorMsg);
    },

    deleteMenu : function(id){
        UserviewBuilder.addToUndo();
        var thisObject = UserviewBuilder;

        $('#'+id).remove();
        thisObject.data.categories[thisObject.categoriesPointer[thisObject.menusPointer[id].categoryId]].menus.splice(thisObject.menusPointer[id].position, 1);

        thisObject.updateCategoriesAndMenusPointer();
    },

    cancelEditMenu : function(container){
        UserviewBuilder.editorDialog.hide();
    },

    setPermission : function(id){
        var category = this.data.categories[this.categoriesPointer[id]];

        var thisObject = this;
        var options = {
            contextPath: UserviewBuilder.contextPath,
            tinyMceScript: thisObject.tinymceUrl,
            propertiesDefinition : UserviewBuilder.categoryPropertyOptions,
            propertyValues : category.properties,
            showCancelButton:true,
            saveCallback: thisObject.savePermission,
            validationFailedCallback: thisObject.saveMenuFailed,
            cancelCallback: thisObject.cancelEditMenu
        }
        $('.menu-wizard-container').html("");
        $('.menu-wizard-container').attr('id', id);
        thisObject.editorDialog.show();
        $('.menu-wizard-container').propertyEditor(options);
        thisObject.editorDialog.center('x');
        thisObject.editorDialog.center('y');
    },

    savePermission : function(container, properties){
        UserviewBuilder.addToUndo();
        var thisObject = UserviewBuilder;

        var id = $(container).attr('id');
        var category = thisObject.data.categories[thisObject.categoriesPointer[id]];

        category.properties = properties;
        thisObject.editorDialog.hide();
        UserviewBuilder.adjustJson();
    },

    //Save setting properties return from property editor
    saveSettingProperties : function(container, properties){
        UserviewBuilder.data.setting.properties = properties;
        UserviewBuilder.updateSaveStatus("+");
        UserviewBuilder.adjustJson();

        $('#step-design').click();
    },

    //Submit userview json to server for saving
    save : function(){
        UserviewBuilder.showMessage(get_ubuilder_msg('ubuilder.saving'));
        var self = this;
        $.post(this.saveUrl + this.data.properties.id, {json : this.getJson()} , function(data) {
            var d = JSON.decode(data);
            if(d.success == true){
                UserviewBuilder.updateSaveStatus("0");
                UserviewBuilder.screenCapture(self.appId, self.data.properties.id, self.data.properties.name, self.userviewUrl, "#builder-screenshot");
            }else{
                alert(get_ubuilder_msg('ubuilder.saveFailed'));
            }
            UserviewBuilder.showMessage("");
        }, "text");
    },

    //Post userview json for preview it in new windows
    preview : function() {
        $('#userview-json').val(this.getJson());
        $('#userview-preview').attr("action", this.previewUrl + this.data.properties.id);
        $('#userview-preview').submit();
        return false;
    },

    updateFromJson: function() {
        var form = $('#userview-preview');
        form.attr("action", "?");
        form.attr("target", "");
        $('#userview-preview').submit();
        return false;
    },

    //Generate Json string based on data
    getJson : function(){
        return JSON.encode(UserviewBuilder.data);
    },

    //Convert JSON to data object
    getData : function(data){
        return JSON.decode(data);
    },

    //Get base model of category
    getCategoryModel : function(){
       return '<div class="category-label"><span class="category-label-editable"></span></div><div class="clear"></div><div class="menu-container"></div>';
    },

    //Get base model of menu
    getMenuModel : function(){
       return '<div class="menu-label"><span></span></div>';
    },

    //Make category label ediatble
    attachCategoryLabelEditableEvent : function(object){
        var thisObject = UserviewBuilder;
        $(object).editable(function(value, settings){
            var id = $(object).parent().parent().attr('id');
            if(value==""){
                alert(get_ubuilder_msg('ubuilder.emptyLabel'));
                value = thisObject.data.categories[thisObject.categoriesPointer[id]].properties.label;
            }else{
                UserviewBuilder.addToUndo();
                thisObject.data.categories[thisObject.categoriesPointer[id]].properties.label = value;
                UserviewBuilder.adjustJson();
            }
            value = UI.escapeHTML(value);
            return value;
        },{
            type      : 'text',
            tooltip   : get_ubuilder_msg('ubuilder.editable.tooltip') ,
            select    : true ,
            style     : 'inherit',
            cssclass  : 'categoryLabelEditableField',
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
    },

    //Make menu in container sortable
    attachCategoryMenuSortableEvent : function(object){
        $(object).sortable({
            connectWith: '.menu-container',
            opacity: 0.8,
            axis: 'y',
            handle: '.menu-label',
            dropOnEmpty: true,
            activeClass: "menu-container-highlight",
            stop: function(event, ui){
                if($(ui.item[0]).hasClass("menu")){
                    UserviewBuilder.addToUndo();
                    var newCategoryId = $(ui.item[0]).parent().parent().attr('id');
                    var id= $(ui.item[0]).attr('id');

                    var newCategory = UserviewBuilder.data.categories[UserviewBuilder.categoriesPointer[newCategoryId]];
                    var originalCategory = UserviewBuilder.data.categories[UserviewBuilder.categoriesPointer[UserviewBuilder.menusPointer[id].categoryId]];

                    var menu = originalCategory.menus[UserviewBuilder.menusPointer[id].position];
                    delete originalCategory.menus[UserviewBuilder.menusPointer[id].position];

                    if($(ui.item[0]).next().length == 0){
                        newCategory.menus.push(menu);
                    }else{
                         var nextMenuId = $(ui.item[0]).next().attr('id');
                         var newPosition = 0;
                         if (UserviewBuilder.menusPointer[nextMenuId] != undefined) {
                            newPosition = UserviewBuilder.menusPointer[nextMenuId].position;
                         }
                         newCategory.menus.splice(newPosition,0, menu);
                    }

                    UserviewBuilder.updateCategoriesAndMenusPointer();
                }
                if($(ui.item[0]).hasClass("builder-palette-element")){
                    UserviewBuilder.addMenu(ui.item[0]);
                }
            }
        });
    },

    //Update data pointer for categories & menu 
    updateCategoriesAndMenusPointer : function(){
        for(c in this.data.categories){
            this.categoriesPointer[this.data.categories[c].properties.id] = c;
            for(m in this.data.categories[c].menus){
                this.menusPointer[this.data.categories[c].menus[m].properties.id].categoryId = this.data.categories[c].properties.id;
                this.menusPointer[this.data.categories[c].menus[m].properties.id].position = m;
            }
        }
        UserviewBuilder.adjustJson();
    },

    //Put extra element on sidebar
    decorateSidebar : function(){
        $('#userview-sidebar').html('<div class="sidebar-title">'+get_ubuilder_msg('ubuilder.menu')+'<div>');
    },

    //Put extra element on category
    decorateCategory : function(obj){
        $(obj).find('.menu-container').html('<div class="tips">'+get_ubuilder_msg('ubuilder.dropMenu')+'<div>');
    },
    
    //Decorate element with button and its events
    decorateElementOptions : function(obj){
        if ($(obj).children(".element-options").length > 0) {
            // remove if already exists
            $(obj).children(".element-options").remove();
            $(obj).children(".element-clear").remove();
        }

        var optionHtml = "<span class='element-options'>";

        if ($(obj).hasClass("editable-info")) {
            // add buttons for editable
            optionHtml += "<button class='element-edit-label' title='"+get_ubuilder_msg('ubuilder.edit')+"'>"+get_ubuilder_msg('ubuilder.edit')+"</button>";
        }else if ($(obj).hasClass("sidebar")) {
            // add buttons for section
            optionHtml += "<button class='element-add-category' title='"+get_ubuilder_msg('ubuilder.addCategory')+"'>"+get_ubuilder_msg('ubuilder.addCategory')+"</button>";
        }else if ($(obj).hasClass("category")) {
            // add buttons for section
            optionHtml += "<button class='element-edit-category' title='"+get_ubuilder_msg('ubuilder.editLabel')+"'>"+get_ubuilder_msg('ubuilder.editLabel')+"</button>";
            optionHtml += "<button class='element-permission' title='"+get_ubuilder_msg('ubuilder.permission')+"'>"+get_ubuilder_msg('ubuilder.permission')+"</button>";
            optionHtml += "<button class='element-delete-category' title='"+get_ubuilder_msg('ubuilder.deleteCategory')+"'>"+get_ubuilder_msg('ubuilder.deleteCategory')+"</button>";
        }else if ($(obj).hasClass("menu")) {
            // add buttons for section
            optionHtml += "<button class='element-menu-roperties' title='"+get_ubuilder_msg('ubuilder.properties')+"'>"+get_ubuilder_msg('ubuilder.properties')+"</button>";
            optionHtml += "<button class='element-delete-menu' title='"+get_ubuilder_msg('ubuilder.deleteMenu')+"'>"+get_ubuilder_msg('ubuilder.deleteMenu')+"</button>";
        }


        optionHtml += "</span><div class='element-clear'></div>";
        var optionDiv = $(optionHtml);

        //event
        // handle editable
        $(optionDiv).children(".element-edit-label").click(function() {
            $(this).parent().parent().find('.editable').click();
        })

        // handle edit category permission
        $(optionDiv).children(".element-permission").click(function() {
            var id = $(this).parent().parent().attr('id');
            UserviewBuilder.setPermission(id);
        })

        // handle edit category label
        $(optionDiv).children(".element-edit-category").click(function() {
            $(this).parent().parent().find('.category-label-editable').click();
        })

        // handle add category
        $(optionDiv).children(".element-add-category").click(function() {
            UserviewBuilder.addCategory();
        })

        // handle deletecategory
        $(optionDiv).children(".element-delete-category").click(function() {
            if (confirm(get_ubuilder_msg('ubuilder.delete.confirm'))) {
                var id = $(this).parent().parent().attr('id');
                UserviewBuilder.deleteCategory(id);
            }
        })

        // handle delete menu
        $(optionDiv).children(".element-delete-menu").click(function() {
            if (confirm(get_ubuilder_msg('ubuilder.delete.confirm'))) {
                var id = $(this).parent().parent().attr('id');
                UserviewBuilder.deleteMenu(id);
            }
        })

        // handle edit menu properties
        $(optionDiv).children(".element-menu-roperties").click(function() {
            var id = $(this).parent().parent().attr('id');
            UserviewBuilder.editMenu(id);
        })

        // add option bar
        $(obj).prepend(optionDiv);
        $(obj).mouseover(function() {
            $(optionDiv).css("display", "block");
            $(optionDiv).css("visibility", "visible");
        });
        $(obj).mouseout(function() {
            $(optionDiv).css("display", "none");
            $(optionDiv).css("visibility", "hidden");
        });
    },

    //Undo the changes from stack
    undo : function(){
        if(this.undoStack.length > 0){
            //if redo stack is full, delete first
            if(this.redoStack.length >= this.undoRedoMax){
                this.redoStack.splice(0,1);
            }

            //save current json data to redo stack
            this.redoStack.push(this.getJson());

            //exclude setting propertise from undo/redo
            var setting = this.data.setting;

            //load the last data from undo stack
            this.loadUserview(null, this.getData(this.undoStack.pop()));

            this.data.setting = setting;

            //enable redo button if it is disabled previously
            if(this.redoStack.length == 1){
                $('#tool-redo a').removeClass('redo-disabled');
                $('#tool-redo a').addClass('redo-enabled');
                $('#tool-redo a').attr('title', get_ubuilder_msg('ubuilder.redo.tip'));
                $('#tool-redo a span').html(get_ubuilder_msg('ubuilder.redo'));
            }

            //if undo stack is empty, disabled undo button
            if(this.undoStack.length == 0){
                $('#tool-undo a').addClass('undo-disabled');
                $('#tool-undo a').removeClass('undo-enabled');
                $('#tool-undo a').attr('title', get_ubuilder_msg('ubuilder.undo.disabled.tip'));
                $('#tool-undo a span').html(get_ubuilder_msg('ubuilder.undo.disabled'));
            }

            this.updateSaveStatus("-");
        }
    },

    //Redo the changes from stack
    redo : function(){
        if(this.redoStack.length > 0){
            //if undo stack is full, delete first
            if(this.undoStack.length >= this.undoRedoMax){
                this.undoStack.splice(0,1);
            }

            //save current json data to undo stack
            this.undoStack.push(this.getJson());

            //exclude setting propertise from undo/redo
            var setting = this.data.setting;

            //load the last data from redo stack
            this.loadUserview(null, this.getData(this.redoStack.pop()));

            this.data.setting = setting;

            //enable undo button if it is disabled previously
            if(this.undoStack.length == 1){
                $('#tool-undo a').removeClass('undo-disabled');
                $('#tool-undo a').addClass('undo-enabled');
                $('#tool-undo a').attr('title', get_ubuilder_msg('ubuilder.undo.tip'));
                $('#tool-undo a span').html(get_ubuilder_msg('ubuilder.undo'));
            }

            //if redo stack is empty, disabled redo button
            if(this.redoStack.length == 0){
                $('#tool-redo a').addClass('redo-disabled');
                $('#tool-redo a').removeClass('redo-enabled');
                $('#tool-redo a').attr('title', get_ubuilder_msg('ubuilder.redo.disabled.tip'));
                $('#tool-redo a span').html(get_ubuilder_msg('ubuilder.redo.disabled'));
            }

            this.updateSaveStatus("+");
        }
    },

    //Add changes info to stack
    addToUndo : function(){
        //if undo stack is full, delete first
        if(this.undoStack.length >= this.undoRedoMax){
            this.undoStack.splice(0,1);
        }

        //save current json data to undo stack
        this.undoStack.push(this.getJson());

        //enable undo button if it is disabled previously
        if(this.undoStack.length == 1){
            $('#tool-undo a').removeClass('undo-disabled');
            $('#tool-undo a').addClass('undo-enabled');
            $('#tool-undo a').attr('title', get_ubuilder_msg('ubuilder.undo.tip'));
            $('#tool-undo a span').html(get_ubuilder_msg('ubuilder.undo'));
        }

        this.updateSaveStatus("+");
    },

    adjustJson: function() {
        // update JSON
        $('#userview-json').val(this.getJson());
    },

    //track the save status
    updateSaveStatus : function(mode){
        if(mode == "+"){
            this.saveChecker++;
        }else if(mode == "-"){
            this.saveChecker--;
        }else if(mode == "0"){
            this.saveChecker = 0;
        }

        if(this.saveChecker == 0){
            $('#step-save a').addClass('save-disabled');
            $('#step-save a').attr('title', get_ubuilder_msg('ubuilder.save.disabled.tip'));
            $('#step-save a').removeClass('save-enabled');
        }else if(this.saveChecker == 1 || this.saveChecker == -1){
            $('#step-save a').removeClass('save-disabled');
            $('#step-save a').attr('title', get_ubuilder_msg('ubuilder.save.tip'));
            $('#step-save a').addClass('save-enabled');
        }
    },

    uuid : function(){
        return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function(c) {  //xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        }).toUpperCase();
    },
           
    showMessage: function(message) {
        if (message && message != "") {
            $("#builder-message").html(message);
            $("#builder-message").fadeIn();
        } else {
            $("#builder-message").fadeOut();
        }
    },
            
    screenCapture: function(appId, userviewId, title, path, container, width) {
        container = container || document.body;
        width = width || 300;
        UserviewBuilder.showMessage(get_ubuilder_msg('ubuilder.generatingScreenshot'));
        var appcontainer = $("<div><i class='icon-spinner icon-spin icon-2x'></i><div style='opacity:0'></div></div>");
        $(container).append(appcontainer);
        $.ajax({
            url: path + "?_isScreenCapture=true",
            success: function(data) {
                var iframe = document.createElement('iframe');
                var iwidth = 1024; //$(window).width()
                var iheight = 768; //$(window).height()
                $(iframe).css({
                    'visibility':'hidden'
                }).width(iwidth).height(iheight);
                $(document.body).append(iframe);
                var d = iframe.contentWindow.document;
                d.open();
                $(iframe.contentWindow).load(function() {
                    var ibody = $(iframe).contents().find('body');
                    html2canvas(ibody, {
                        onrendered: function(canvas) {
                            $(appcontainer).remove();
                            $(iframe).remove();
                            var imgData = canvas.toDataURL();
                            var img = $("<span class='screenshot'><a target='_blank' href='" + path + "'><div class='screenshot_label'>" + title + "</div><img src='" + imgData + "' width='" + width + "'></a></span>");
                            $(container).append(img);

                            var saveUrl = UserviewBuilder.contextPath + '/web/console/app/' + appId + '//userview/' + userviewId + '/screenshot/submit';
                            $.ajax({ 
                                type: "POST", 
                                url: saveUrl,
                                dataType: 'text',
                                data: {
                                    base64data : imgData
                                }
                            })
                        }
                    });
                });    
                try {
                    if (true) { // disable scripts
                        data = data.replace(/\<script/gi,"<!--<script");
                        data = data.replace(/\<\/script\>/gi,"<\/script>-->");
                    }
                    d.write(data);
                } catch(e) {}
                d.close();
            },
            complete: function() {
                UserviewBuilder.showMessage("");
                alert(get_ubuilder_msg('ubuilder.saved'));
            }
        })
    }    
}