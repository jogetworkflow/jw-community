UserviewBuilder = {
    //Configuration
    saveUrl : '',
    previewUrl : '',
    contextPath : '/jw',
    appId: '',
    appVersion: '',
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
    isAltKeyPressed : false,
    saveChecker : 0,

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
        d.properties['footerMessage'] = "Powered by Kecak",
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
    initMenuType : function(category, className, label, icon, propertyOptions, defaultPropertiesValues, pwaValidation){
        this.menuTypes[className] = new Object();
        this.menuTypes[className]['label'] = label;
        
        if (propertyOptions !== null && propertyOptions !== undefined 
                && propertyOptions.length > 0 && propertyOptions[0].properties !== undefined) {
            for (var i = 0; i < propertyOptions[0].properties.length; i++) {
                var name = propertyOptions[0].properties[i].name;
                if (name === "id") {
                    propertyOptions[0].properties[i].type = "label";
                    break;
                }
            }
        }
        
        this.menuTypes[className]['propertyOptions'] = propertyOptions;
        this.menuTypes[className]['properties'] = defaultPropertiesValues;
        this.menuTypes[className]['pwaValidation'] = pwaValidation;

        var iconObj = null;
        if (icon !== undefined && icon !== null && icon !== "") {
            try {   
                iconObj = $(icon);
            } catch (err) {
                iconObj =  $('<span class="image" style="background-image:url(\'' + UserviewBuilder.contextPath + icon + '\');" />');
            }
        } else {
            iconObj = $('<i class="fas fa-desktop"></i>');
        }

        //get category
        var categoryId = category.replace(/\s/g , "-");
        if($('ul#'+categoryId).length == 0){
            $('#builder-palette-body').append('<h3>'+category+'</h3><ul id="'+categoryId+'"></ul>');
        }

        var li = $('<li><div id="'+className+'" element="'+className+'" class="builder-palette-element"> <label class="label">'+label+'</label></div></li>');
        $(li).find('.builder-palette-element').prepend(iconObj);
        $('ul#'+categoryId).append(li);
    },

    //Initial Builder feature
    initBuilder : function(){
        //Popup dialog
        PropertyEditor.Popup.createDialog("menu-wizard-container");
        
        //Shortcut key
        $(document).keyup(function (e) {
            if(e.which == 17){
                UserviewBuilder.isCtrlKeyPressed=false;
            } else if(e.which === 18){
                UserviewBuilder.isAltKeyPressed = false;
            }
        }).keydown(function (e) {
            if(e.which == 17){
                UserviewBuilder.isCtrlKeyPressed=true;
            } else if(e.which === 18){
                UserviewBuilder.isAltKeyPressed = true;
            }
            if ($(".property-editor-container:visible").length === 0) {
                if(e.which == 83 && UserviewBuilder.isCtrlKeyPressed == true && !UserviewBuilder.isAltKeyPressed) { //CTRL+S - save
                    UserviewBuilder.mergeAndSave();
                    return false;
                }
                if(e.which == 90 && UserviewBuilder.isCtrlKeyPressed == true && !UserviewBuilder.isAltKeyPressed) { //CTRL+Z - undo
                    UserviewBuilder.undo();
                    return false;
                }
                if(e.which == 89 && UserviewBuilder.isCtrlKeyPressed == true && !UserviewBuilder.isAltKeyPressed) { //CTRL+Y - redo
                    UserviewBuilder.redo();
                    return false;
                }
                if(e.which == 80 && UserviewBuilder.isCtrlKeyPressed == true && !UserviewBuilder.isAltKeyPressed) { //CTRL+P - preview
                    UserviewBuilder.preview();
                    return false;
                }
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
                $("body").removeClass("stop-scrolling");
            }
            return false;
        });

        //setting property page
        if(UserviewBuilder.data.setting == undefined){
            UserviewBuilder.data.setting = new Object();
            UserviewBuilder.data.setting['className'] = "org.joget.apps.userview.model.UserviewSetting";
            UserviewBuilder.data.setting['properties'] = new Array();
        }
        
        $("#step-setting").on("click", function(){
            $("#step-setting-container").html("");
            var options = {
                appPath: "/" + UserviewBuilder.appId + "/" + UserviewBuilder.appVersion,
                contextPath: UserviewBuilder.contextPath,
                propertiesDefinition : UserviewBuilder.settingPropertyOptions,
                propertyValues : UserviewBuilder.data.setting.properties,
                showCancelButton:false,
                closeAfterSaved : false,
                changeCheckIgnoreUndefined: true,
                autoSave: true,
                saveCallback: UserviewBuilder.saveSettingProperties
            };
            $('#step-setting-container').propertyEditor(options);
            $("body").addClass("stop-scrolling");
            return true;
        });

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
            handle: '.category-label, .category-comment',
            tolerance: 'intersect',
            stop: function(event, ui){
                UserviewBuilder.addToUndo();
                var id= $(ui.item[0]).attr('id');
                var category = UserviewBuilder.data.categories[UserviewBuilder.categoriesPointer[id]];
                delete UserviewBuilder.data.categories[UserviewBuilder.categoriesPointer[id]];
                
                $(".category-bottom").remove();
        
                if($(ui.item[0]).next().length === 0){
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
        
        //update paste icons
        $("#userview-sidebar, .category").on("mouseenter", function() {
            UserviewBuilder.updatePasteIcon();
        });
        
        //add control
        $("#builder-steps").after("<div class='controls'></div>");
        $(".controls").append("<a class='action-undo disabled' title='"+get_ubuilder_msg('ubuilder.undo.disabled.tip')+"'><i class='fas fa-undo'></i> "+get_ubuilder_msg('ubuilder.undo')+"</a>&nbsp;|&nbsp;");
        $(".controls").append("<a class='action-redo disabled' title='"+get_ubuilder_msg('ubuilder.redo.disabled.tip')+"'><i class='fas fa-redo'></i> "+get_ubuilder_msg('ubuilder.redo')+"</a>");
        
        $(".action-undo").click(function(){
            UserviewBuilder.undo();
            return false;
        });
        
        $(".action-redo").click(function(){
            UserviewBuilder.redo();
            return false;
        });
    },
    
    ShowPopupUserviewSetting : function () {
        var options = {
            appPath: "/" + UserviewBuilder.appId + "/" + UserviewBuilder.appVersion,
            contextPath: UserviewBuilder.contextPath,
            propertiesDefinition : UserviewBuilder.settingPropertyOptions,
            propertyValues : UserviewBuilder.data.setting.properties,
            changeCheckIgnoreUndefined: true,
            showCancelButton:true,
            cancelCallback: function() {
            },
            saveCallback: function(container, properties) {
                UserviewBuilder.saveSettingProperties(container, properties);
            }
        };
        
        PropertyEditor.Popup.showDialog("menu-wizard-container", options);
    },

    //Generate userview builder element based on json
    loadUserview : function(id, json){
        if(json == null){
            this.initDefaultUserviewDataModel(id);
        }else{
            this.data = json;
            this.data.properties.id = id;
            
            //handle for v5 app on property migration
            if (this.data.setting.properties['loginPageTop'] !== undefined) {
                this.data.setting.properties['theme']['properties']['loginPageTop'] = this.data.setting.properties['loginPageTop'];
                this.data.setting.properties['theme']['properties']['loginPageBottom'] = this.data.setting.properties['loginPageBottom'];
                delete this.data.setting.properties['loginPageTop'];
                delete this.data.setting.properties['loginPageBottom'];
            }
            if (this.data.setting.properties['mobileViewDisabled'] !== undefined) {
                this.data.setting.properties['theme']['properties']['mobileViewDisabled'] = this.data.setting.properties['mobileViewDisabled'];
                this.data.setting.properties['theme']['properties']['mobileCacheEnabled'] = this.data.setting.properties['mobileCacheEnabled'];
                this.data.setting.properties['theme']['properties']['mobileLoginRequired'] = this.data.setting.properties['mobileLoginRequired'];
                this.data.setting.properties['theme']['properties']['mobileViewBackgroundUrl'] = this.data.setting.properties['mobileViewBackgroundUrl'];
                this.data.setting.properties['theme']['properties']['mobileViewBackgroundColor'] = this.data.setting.properties['mobileViewBackgroundColor'];
                this.data.setting.properties['theme']['properties']['mobileViewBackgroundStyle'] = this.data.setting.properties['mobileViewBackgroundStyle'];
                this.data.setting.properties['theme']['properties']['mobileViewTranslucent'] = this.data.setting.properties['mobileViewTranslucent'];
                this.data.setting.properties['theme']['properties']['mobileViewLogoUrl'] = this.data.setting.properties['mobileViewLogoUrl'];
                this.data.setting.properties['theme']['properties']['mobileViewLogoWidth'] = this.data.setting.properties['mobileViewLogoWidth'];
                this.data.setting.properties['theme']['properties']['mobileViewLogoHeight'] = this.data.setting.properties['mobileViewLogoHeight'];
                this.data.setting.properties['theme']['properties']['mobileViewLogoAlign'] = this.data.setting.properties['mobileViewLogoAlign'];
                this.data.setting.properties['theme']['properties']['mobileViewCustomCss'] = this.data.setting.properties['mobileViewCustomCss'];
                delete this.data.setting.properties['mobileViewDisabled'];
                delete this.data.setting.properties['mobileCacheEnabled'];
                delete this.data.setting.properties['mobileLoginRequired'];
                delete this.data.setting.properties['mobileViewBackgroundUrl'];
                delete this.data.setting.properties['mobileViewBackgroundColor'];
                delete this.data.setting.properties['mobileViewBackgroundStyle'];
                delete this.data.setting.properties['mobileViewTranslucent'];
                delete this.data.setting.properties['mobileViewLogoUrl'];
                delete this.data.setting.properties['mobileViewLogoWidth'];
                delete this.data.setting.properties['mobileViewLogoHeight'];
                delete this.data.setting.properties['mobileViewLogoAlign'];
                delete this.data.setting.properties['mobileViewCustomCss'];
            }
            
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
            $(categoryObject).find('.category-label span').html(category.properties.label);
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
                $(menuObject).find('.menu-label span').html(menu.properties.label);
                this.decorateElementOptions(menuObject);
            }
            this.decorateElementOptions(categoryObject);
        }
        this.decorateElementOptions($('#userview-sidebar'));
        this.decorateCategoryBottom();
        $("#loading").remove();
    },

    addCategory : function(element, copiedCategory){
        UserviewBuilder.addToUndo();
        var category = new Object();
        if (copiedCategory !== undefined && copiedCategory !== null) {
            category = copiedCategory;
            category['properties']['id'] = 'category-'+UserviewBuilder.uuid();
        } else {
            category['className'] = "org.joget.apps.userview.model.UserviewCategory";
            category['properties'] = new Object();
            category['properties']['id'] = 'category-'+UserviewBuilder.uuid();
            category['properties']['label'] = get_ubuilder_msg('ubuilder.newCategory');
            category['menus'] = new Array();
        }
        
        var categoryHtml = $('<div id="'+category.properties.id+'" class="category"></div>');
        
        
        if ($(element).hasClass("category-bottom")) {
            if($(element).next(".category").length === 0) {
                UserviewBuilder.data.categories.push(category);
                $('#userview-sidebar').append(categoryHtml);
            } else {
                var nextCategoryId = $(element).next(".category").attr('id');
                var newPosition = UserviewBuilder.categoriesPointer[nextCategoryId];
                UserviewBuilder.data.categories.splice(newPosition,0, category);
                $(element).after(categoryHtml);
            }
        } else {
            UserviewBuilder.data.categories.splice(0,0, category);
            $('#userview-sidebar .sidebar-title').after(categoryHtml);
        }
    
        var categoryObject = $('#userview-sidebar').find('#'+category.properties.id);
        $(categoryObject).html(UserviewBuilder.getCategoryModel());
        $(categoryObject).find('.category-label span').html(category.properties.label);
        UserviewBuilder.decorateCategory($(categoryObject));
        
        if (category.menus !== undefined && category.menus !== null && category.menus.length > 0) {
            for(m in category.menus){
                var menu = category.menus[m];
                menu.properties.id=UserviewBuilder.uuid();
                UserviewBuilder.menusPointer[menu.properties.id] = new Object();
                UserviewBuilder.menusPointer[menu.properties.id].categoryId = category.properties.id;
                UserviewBuilder.menusPointer[menu.properties.id].position = m;
                
                $(categoryObject).find('.menu-container').append('<div id="'+menu.properties.id+'" class="menu"></div>');
                var menuObject =  $(categoryObject).find('.menu-container #'+menu.properties.id);
                $(menuObject).html(UserviewBuilder.getMenuModel());
                $(menuObject).find('.menu-label span').html(UI.escapeHTML(menu.properties.label));
                UserviewBuilder.decorateElementOptions(menuObject);
            }
        }
        UserviewBuilder.attachCategoryLabelEditableEvent($(categoryObject).find('.category-label .category-label-editable'));
        UserviewBuilder.attachCategoryMenuSortableEvent($(categoryObject).find('.menu-container'));
        UserviewBuilder.updateCategoriesAndMenusPointer();
        UserviewBuilder.decorateElementOptions($(categoryObject));
        UserviewBuilder.adjustJson();
    },

    deleteCategory : function(id){
        UserviewBuilder.addToUndo();
        $('#'+id).remove();
        this.data.categories.splice(this.categoriesPointer[id],1);
        delete this.categoriesPointer[id];
        this.updateCategoriesAndMenusPointer();
    },

    addMenu : function(obj, copied){
        UserviewBuilder.addToUndo();
        
        var id = UserviewBuilder.uuid();
        var categoryId = $(obj).parent().parent().attr('id');

        var menu = new Object();
        if (copied !== undefined && copied !== null) {
            menu = copied;
            menu.properties.id=id;
        } else {
            var type = $(obj).attr('element');
            menu.className = type;
            menu.properties = JSON.parse(UserviewBuilder.menuTypes[type].properties);
            menu.properties.id=id;
            menu.properties.label = UserviewBuilder.menuTypes[type].label;
        }
    
        var category = UserviewBuilder.data.categories[UserviewBuilder.categoriesPointer[categoryId]];

        if($(obj).next().length === 0){
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
        var menu = null;
        
        if (this.menusPointer[id] === undefined) {
            //find the menu id using custom id
            var found = false;
            for (var i in this.data.categories) {
                for (var j in this.data.categories[i].menus) {
                    if (this.data.categories[i].menus[j].properties.customId === id) {
                        id = this.data.categories[i].menus[j].properties.id;
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }
        
        menu = this.data.categories[this.categoriesPointer[this.menusPointer[id].categoryId]].menus[this.menusPointer[id].position];
        
        if (menu === null) {
            return;
        }

        var thisObject = this;
        var options = {
            appPath: "/" + UserviewBuilder.appId + "/" + UserviewBuilder.appVersion,
            contextPath: UserviewBuilder.contextPath,
            propertiesDefinition : thisObject.menuTypes[menu.className].propertyOptions,
            propertyValues : menu.properties,
            showCancelButton:true,
            changeCheckIgnoreUndefined: true,
            saveCallback: thisObject.saveMenu,
            customSaveValidation: thisObject.saveMenuValidation,
            validationFailedCallback: thisObject.saveMenuFailed,
            cancelCallback: thisObject.cancelEditMenu
        }
        PropertyEditor.Popup.showDialog("menu-wizard-container", options, {id:id});
    },

    saveMenu : function(container, properties){
        UserviewBuilder.addToUndo();
        var thisObject = UserviewBuilder;

        var id = $(container).attr('data-id');
        var menu = thisObject.data.categories[thisObject.categoriesPointer[thisObject.menusPointer[id].categoryId]].menus[thisObject.menusPointer[id].position];

        if (menu.properties.permission_rules !== null && menu.properties.permission_rules !== undefined) {
            properties.permission_rules = menu.properties.permission_rules;
        }
        if (menu.properties.hide !== null && menu.properties.hide !== undefined) {
            properties.hide = menu.properties.hide;
        }
        if (menu.properties.permissionDeny !== null && menu.properties.permissionDeny !== undefined) {
            properties.permissionDeny = menu.properties.permissionDeny;
        }
        if (menu.properties.permissionHidden !== null && menu.properties.permissionHidden !== undefined) {
            properties.permissionHidden = menu.properties.permissionHidden;
        }

        menu.properties = properties;
        var label = properties.label;
        $('#'+id+' .menu-label span').html(label);
        UserviewBuilder.adjustJson();
    },
    
    saveMenuValidation : function(container, properties, saveCallback){
        if(properties['enableOffline'] === "true") {
            var thisObject = UserviewBuilder;

            var id = $(container).attr('data-id');
            var menu = thisObject.data.categories[thisObject.categoriesPointer[thisObject.menusPointer[id].categoryId]].menus[thisObject.menusPointer[id].position];
            
            var pwaOffline = thisObject.menuTypes[menu.className].pwaValidation;
            if (pwaOffline === "checking") {
                $(container).find(".ajaxLoader").show();
                $.ajax({
                    url: UserviewBuilder.contextPath + '/web/property/json/'+ UserviewBuilder.appId + "/" + UserviewBuilder.appVersion+"/pwaValidation",
                    type: "POST",
                    data : {
                        className : menu.className,
                        properties : JSON.stringify(properties)
                    },
                    beforeSend: function (request) {
                       if (ConnectionManager.tokenName !== undefined) { 
                           request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                       }
                    },
                    dataType : "json",
                    success: function(response) {
                        if ($.isArray(response) && response.length > 0) {
                            var warning = get_ubuilder_msg("ubuilder.pwa.warning") + "\n";
                            for (var r in response) {
                                for (var i in response[r].messages) {
                                    warning += "- " + response[r].messages[i] + '\n';
                                }
                            }
                            warning += get_ubuilder_msg("ubuilder.pwa.confirm");

                            if (confirm(warning)) {
                                $(container).find(".ajaxLoader").hide();
                                saveCallback();
                            } else {
                                $(container).find(".property-editor-page-step-indicator:visible .step:last").trigger("click");
                                $(container).find(".ajaxLoader").hide();
                            }
                        } else {
                            $(container).find(".ajaxLoader").hide();
                            saveCallback();
                        }
                    }
                });
            } else {
                saveCallback();
            }
        } else {
            saveCallback();
        }
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
        
    },

    setPermission : function(id){
        var category = this.data.categories[this.categoriesPointer[id]];

        var thisObject = this;
        var options = {
            appPath: "/" + UserviewBuilder.appId + "/" + UserviewBuilder.appVersion,
            contextPath: UserviewBuilder.contextPath,
            propertiesDefinition : UserviewBuilder.categoryPropertyOptions,
            propertyValues : category.properties,
            showCancelButton:true,
            changeCheckIgnoreUndefined: true,
            saveCallback: thisObject.savePermission,
            validationFailedCallback: thisObject.saveMenuFailed,
            cancelCallback: thisObject.cancelEditMenu
        };
        PropertyEditor.Popup.showDialog("menu-wizard-container", options, {id: id});
    },

    savePermission : function(container, properties){
        UserviewBuilder.addToUndo();
        var thisObject = UserviewBuilder;

        var id = $(container).attr('data-id');
        var category = thisObject.data.categories[thisObject.categoriesPointer[id]];

        if (properties['hide'] === "true") {
            properties['permissionDeny'] = "";
        }
        category.properties = $.extend(category.properties, properties);
        
        $("#"+id).find('.category-label span').html(UI.escapeHTML(category.properties.label));
        if (category.properties.comment !== undefined && category.properties.comment !== null && category.properties.comment !== "") {
            $("#"+id).find(".category-comment").remove();
            $("#"+id).find(".category-label").before('<div class="category-comment"><strong>'+get_ubuilder_msg("ubuilder.comment")+':</strong> <div class="editable">'+UI.escapeHTML(category.properties.comment).replace(/(?:\r\n|\r|\n)/g, '<br />')+'</div></div>');
            UserviewBuilder.initEditableComment($("#"+id));
        }
        UserviewBuilder.adjustJson();
    },

    //Save setting properties return from property editor
    saveSettingProperties : function(container, properties){
        UserviewBuilder.data.setting.properties = $.extend(UserviewBuilder.data.setting.properties, properties);
        UserviewBuilder.updateSaveStatus("+");
        UserviewBuilder.adjustJson();
    },

    //Submit userview json to server for saving
    save : function(){
        UserviewBuilder.showMessage(get_ubuilder_msg('ubuilder.saving'));
        var self = this;
        var json = this.getJson();
        $.post(this.saveUrl + this.data.properties.id, {json : json} , function(data) {
            var d = JSON.decode(data);
            if(d.success == true){
                UserviewBuilder.updateSaveStatus("0");
                UserviewBuilder.screenCapture(self.appId, self.data.properties.id, self.data.properties.name, self.userviewUrl, "#builder-screenshot", 300, function() {
                    $('#userview-json-original').val(json);
                    UserviewBuilder.showMessage("");
                });
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
        var json = $('#userview-json').val();
        if (UserviewBuilder.getJson() !== json) {
            UserviewBuilder.addToUndo();
        }
        UserviewBuilder.loadUserview(UserviewBuilder.data.properties.id, UserviewBuilder.getData(json));
        
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
                return value;
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
        UserviewBuilder.decorateCategoryBottom();
        
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
    
    //put add category & paste button on the bottom of category
    decorateCategoryBottom : function() {
        $(".category-bottom").remove();
        $(".category").each(function(){
            var bottom = $("<div class='category-bottom'></div>");
            UserviewBuilder.decorateElementOptions(bottom);
            $(this).after(bottom);
        });
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
            optionHtml += "<button class='element-edit-label' title='"+get_ubuilder_msg('ubuilder.edit')+"'><i class='far fa-edit'></i><span>"+get_ubuilder_msg('ubuilder.edit')+"</span></button>";
        }else if ($(obj).hasClass("sidebar") || $(obj).hasClass("category-bottom")) {
            // add buttons for section
            optionHtml += "<button class='element-add-category' title='"+get_ubuilder_msg('ubuilder.addCategory')+"'><i class='fas fa-plus'></i><span>"+get_ubuilder_msg('ubuilder.addCategory')+"</span></button>";
            optionHtml += "<button class='element-paste paste-category disabled' title='"+get_ubuilder_msg('ubuilder.pasteCategory')+"'><i class='fas fa-paste'></i><span>"+get_ubuilder_msg('ubuilder.pasteCategory')+"</span></button>";
        }else if ($(obj).hasClass("category")) {
            // add buttons for section
            optionHtml += "<button class='element-edit-category' title='"+get_ubuilder_msg('ubuilder.editLabel')+"'><i class='far fa-edit'></i><span>"+get_ubuilder_msg('ubuilder.editLabel')+"</span></button>";
            optionHtml += "<button class='element-permission' title='"+get_ubuilder_msg('ubuilder.permission')+"'><i class='far fa-eye'></i><span>"+get_ubuilder_msg('ubuilder.permission')+"</span></button>";
            optionHtml += "<button class='element-copy' title='"+get_ubuilder_msg('ubuilder.copy')+"'><i class='far fa-copy'></i><span>"+get_ubuilder_msg('ubuilder.copy')+"</span></button>";
            optionHtml += "<button class='element-paste paste-menu disabled' title='"+get_ubuilder_msg('ubuilder.pasteMenu')+"'><i class='fas fa-paste'></i><span>"+get_ubuilder_msg('ubuilder.pasteMenu')+"</span></button>";
            optionHtml += "<button class='element-comment' title='"+get_ubuilder_msg('ubuilder.comment')+"'><i class='far fa-comment'></i><span>"+get_ubuilder_msg('ubuilder.comment')+"</span></button>";
            optionHtml += "<button class='element-delete-category element-delete' title='"+get_ubuilder_msg('ubuilder.deleteCategory')+"'><i class='fas fa-times'></i><span>"+get_ubuilder_msg('ubuilder.deleteCategory')+"</span></button>";
        
            //comment
            $(obj).find(".section-comment").remove();
            var comment = UserviewBuilder.data.categories[UserviewBuilder.categoriesPointer[$(obj).attr("id")]].properties.comment;
            if (comment !== undefined && comment !== null && comment !== "") {
                $(obj).find(".category-label").before('<div class="category-comment"><strong>'+get_ubuilder_msg("ubuilder.comment")+':</strong> <div class="editable">'+UI.escapeHTML(comment).replace(/(?:\r\n|\r|\n)/g, '<br />')+'</div></div>');
                UserviewBuilder.initEditableComment(obj);
            }
        }else if ($(obj).hasClass("menu")) {
            // add buttons for section
            optionHtml += "<button class='element-menu-properties' title='"+get_ubuilder_msg('ubuilder.properties')+"'><i class='far fa-edit'></i><span>"+get_ubuilder_msg('ubuilder.properties')+"</span></button>";
            optionHtml += "<button class='element-copy' title='"+get_ubuilder_msg('ubuilder.copy')+"'><i class='far fa-copy'></i><span>"+get_ubuilder_msg('ubuilder.copy')+"</span></button>";
            optionHtml += "<button class='element-delete-menu element-delete' title='"+get_ubuilder_msg('ubuilder.deleteMenu')+"'><i class='fas fa-times'></i><span>"+get_ubuilder_msg('ubuilder.deleteMenu')+"</span></button>";
        }


        optionHtml += "</span><div class='element-clear'></div>";
        var optionDiv = $(optionHtml);

        //event
        // handle editable
        $(optionDiv).children(".element-edit-label").click(function() {
            $(this).parent().parent().find('.editable').click();
        });

        // handle edit category permission
        $(optionDiv).children(".element-permission").click(function() {
            var id = $(this).parent().parent().attr('id');
            UserviewBuilder.setPermission(id);
        });

        // handle edit category label
        $(optionDiv).children(".element-edit-category").click(function() {
            $(this).parent().parent().find('.category-label-editable').click();
        });

        // handle add category
        $(optionDiv).children(".element-add-category").click(function() {
            UserviewBuilder.addCategory($(this).parent().parent());
        });

        // handle delete category
        $(optionDiv).children(".element-delete-category").click(function() {
            var id = $(this).parent().parent().attr('id');
            UserviewBuilder.deleteCategory(id);
        });

        // handle delete menu
        $(optionDiv).children(".element-delete-menu").click(function() {
            var id = $(this).parent().parent().attr('id');
            UserviewBuilder.deleteMenu(id);
        });

        // handle edit menu properties
        $(optionDiv).children(".element-menu-properties").click(function() {
            var id = $(this).parent().parent().attr('id');
            UserviewBuilder.editMenu(id);
        });
        
        // handle copy 
        $(optionDiv).children(".element-copy").click(function() {
            var element = $(this).parent().parent();
            UserviewBuilder.copy(element);
        });
        
        // handle paste
        $(optionDiv).children(".element-paste").click(function() {
            if ($(this).hasClass("disabled")) {
                alert(get_ubuilder_msg("ubuilder.noCopiedItem"));
                return false;
            }
            var element = $(this).parent().parent();
            UserviewBuilder.paste(element);
        });
        
        // handle comment
        $(optionDiv).children(".element-comment").click(function() {
            var element = $(this).parent().parent();
            
            if ($(element).find(".category-comment").length === 0) {
                $(element).find(".category-label").before('<div class="category-comment"><strong>'+get_ubuilder_msg("ubuilder.comment")+':</strong> <div class="editable"></div></div>');
                UserviewBuilder.initEditableComment(obj);
            }
            $(element).find('.category-comment .editable').click();
            
            return false;
        });

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
        $(obj).on("click", function() {
            if ($(optionDiv).css("display") === "block") {
                $(optionDiv).css("display", "none");
                $(optionDiv).css("visibility", "hidden");
            } else {
                if ($(obj).children().length > 0) {
                    var $family = $(obj).find("*");
                    $(".element-options").not($family).css("display", "none");
                    $(".element-options").not($family).css("visibility", "hidden");
                }
                $(optionDiv).css("display", "block");
                $(optionDiv).css("visibility", "visible");
            }
        });
    },
    
    initEditableComment: function(obj) {
        var thisObject = this;
        
        $(obj).find(".category-comment .editable").editable(function(value, settings){
            UserviewBuilder.addToUndo();
            var id = $(obj).attr('id');
            
            thisObject.data.categories[thisObject.categoriesPointer[id]].properties.comment = value;
            UserviewBuilder.adjustJson();
            if(value === ""){
                $(obj).find(".category-comment").remove();
            } else {
                value = UI.escapeHTML(value).replace(/(?:\r\n|\r|\n)/g, '<br />');
            }
            return value;
        },{
            type      : 'textarea',
            tooltip   : '' ,
            select    : true ,
            style     : 'inherit',
            cssclass  : 'LabelEditableField',
            onblur    : 'submit',
            rows      : 4,
            width     : '100%',
            minwidth  : 80,
            submit  : get_ubuilder_msg("ubuilder.save"),
            data: function(value, settings) {
                if (value !== "") {
                    return value.replace(/<br\s*[\/]?>/gi, "\n");
                } else {
                    return value;
                }
            }
        });
        $(obj).find(".category-comment").on("click", function(){
            if ($(obj).find('.category-comment .editable form').length === 0) {
                $(obj).find('.category-comment .editable').click();
            }
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

            //load the last data from undo stack
            var loading = $('<div id="loading"><i class="fas fa-spinner fa-spin fa-2x"></i> ' + get_ubuilder_msg("ubuilder.label.undoing") + '</div>');
            $("body").append(loading);
            this.loadUserview(this.data.properties.id, this.getData(this.undoStack.pop()));

            //enable redo button if it is disabled previously
            if(this.redoStack.length === 1){
                $('.action-redo').removeClass('disabled');
                $('.action-redo').attr('title', get_ubuilder_msg('ubuilder.redo.tip'));
            }

            //if undo stack is empty, disabled undo button
            if(this.undoStack.length === 0){
                $('.action-undo').addClass('disabled');
                $('.action-undo').attr('title', get_ubuilder_msg('ubuilder.undo.disabled.tip'));
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

            //load the last data from redo stack
            var loading = $('<div id="loading"><i class="fas fa-spinner fa-spin fa-2x"></i> ' + get_ubuilder_msg("ubuilder.label.redoing") + '</div>');
            $("body").append(loading);
            this.loadUserview(this.data.properties.id, this.getData(this.redoStack.pop()));

            //enable undo button if it is disabled previously
            if(this.undoStack.length == 1){
                $('.action-undo').removeClass('disabled');
                $('.action-undo').attr('title', get_ubuilder_msg('ubuilder.undo.tip'));
            }

            //if redo stack is empty, disabled redo button
            if(this.redoStack.length == 0){
                $('.action-redo').addClass('disabled');
                $('.action-redo').attr('title', get_ubuilder_msg('ubuilder.redo.disabled.tip'));
            }

            this.updateSaveStatus("+");
        }
    },

    //Add changes info to stack
    addToUndo : function(json){
        //if undo stack is full, delete first
        if(this.undoStack.length >= this.undoRedoMax){
            this.undoStack.splice(0,1);
        }
        
        if (json === null || json === undefined) {
            json = this.getJson();
        }

        //save current json data to undo stack
        this.undoStack.push(json);

        //enable undo button if it is disabled previously
        if(this.undoStack.length == 1){
            $('.action-undo').removeClass('disabled');
            $('.action-undo').attr('title', get_ubuilder_msg('ubuilder.undo.tip'));
        }

        this.updateSaveStatus("+");
    },

    adjustJson: function() {
        // update JSON
        $('#userview-json').val(this.getJson()).trigger("change");
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
            
    screenCapture: function(appId, userviewId, title, path, container, width, callback) {
        container = container || document.body;
        width = width || 300;
        UserviewBuilder.showMessage(get_ubuilder_msg('ubuilder.generatingScreenshot'));
        var appcontainer = $("<div><i class='fas fa-spinner fa-spin fa-2x'></i><div style='opacity:0'></div></div>");
        $(container).append(appcontainer);
        var thisObject = this;
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
                    $(iframe).contents().find(".page-loader").remove();
                    var ibody = $(iframe).contents().find('body');
                    html2canvas(ibody, {
                        onrendered: function(canvas) {
                            $(appcontainer).remove();
                            $(iframe).remove();
                            var imgData = canvas.toDataURL();
                            var img = $("<span class='screenshot'><a target='_blank' href='" + path + "'><div class='screenshot_label'>" + title + "</div><img src='" + imgData + "' width='" + width + "'></a></span>");
                            $(container).append(img);

                            var saveUrl = UserviewBuilder.contextPath + '/web/console/app/' + appId + '/_/userview/' + userviewId + '/screenshot/submit';
                            $.ajax({ 
                                type: "POST", 
                                url: saveUrl,
                                dataType: 'text',
                                beforeSend: function (request) {
                                   request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                                },
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
                setTimeout(function(){ 
                    UserviewBuilder.showMessage(get_ubuilder_msg('ubuilder.saved'));
                    setTimeout(function(){ UserviewBuilder.showMessage(""); }, 2000);
                    if (callback) {
                        callback.apply(thisObject);
                    }
                }, 500);
            }
        })
    },
    
    getCopiedElement : function() {
        var time = $.localStorage.getItem("userviewBuilder.copyTime");
        //10 mins
        if (time !== undefined && time !== null && ((new Date()) - (new Date(time))) > 3000000) {
            $.localStorage.removeItem('userviewBuilder.copyTime');
            $.localStorage.removeItem('userviewBuilder.copy');
            return null;
        }
        var copied = $.localStorage.getItem("userviewBuilder.copy");
        if (copied !== undefined && copied !== null) {
            return JSON.decode(copied);
        }
        return null;
    },
    
    copy : function(element) {
        var id = $(element).attr("id");
        var copy = new Object();
        if ($(element).hasClass("menu")) {
            copy['type'] = "menu";
            var menu = this.data.categories[this.categoriesPointer[this.menusPointer[id].categoryId]].menus[this.menusPointer[id].position];
            copy['object'] = menu;
        } else if ($(element).hasClass("category")) {
            copy['type'] = "category";
            var category = UserviewBuilder.data.categories[UserviewBuilder.categoriesPointer[id]];
            copy['object'] = category;
        }
        
        $.localStorage.setItem("userviewBuilder.copy", JSON.encode(copy));
        $.localStorage.setItem("userviewBuilder.copyTime", new Date());
        UserviewBuilder.updatePasteIcon();
        UserviewBuilder.showMessage(get_ubuilder_msg('ubuilder.copied'));
        setTimeout(function(){ UserviewBuilder.showMessage(""); }, 2000);
    },
    
    paste : function(element) {
        var copied = UserviewBuilder.getCopiedElement();
        
        if ($(element).hasClass("sidebar") || $(element).hasClass("category-bottom")) {
            UserviewBuilder.addCategory(element, copied['object']);
        } else {
            var dummy = $("<div></div>");
            $(element).find(".menu-container").append(dummy);
            UserviewBuilder.addMenu(dummy, copied['object']);
        }
    },
    
    updatePasteIcon : function() {
        $(".element-paste").addClass("disabled");
        var copied = UserviewBuilder.getCopiedElement();
        if (copied !== undefined && copied !== null) {
            if (copied['type'] === "menu") {
                $(".element-paste.paste-menu").removeClass("disabled");
            } else {
                $(".element-paste.paste-category").removeClass("disabled");
            }
        }
    },
    
    showDiff : function (callback, output) {
        var jsonUrl = UserviewBuilder.contextPath + '/web/json/console/app/' + UserviewBuilder.appId + '/' + UserviewBuilder.appVersion + '/userview/' + this.data.properties.id + '/json';
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
                $('#userview-json-current').val(currentString);
                var original = JSON.decode($('#userview-json-original').val());
                var latest = JSON.decode($('#userview-json').val());
                merged = DiffMerge.merge(original, current, latest, output);
            },
            error: function() {
                currentSaved = $('#userview-json-current').val();
                merged = $('#userview-json').val();
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
        UserviewBuilder.showMessage(get_ubuilder_msg('ubuilder.merging'));
        var thisObject = this;
        
        UserviewBuilder.showDiff(function (currentSaved, merged) {
            if (currentSaved !== undefined && currentSaved !== "") {
                $('#userview-json-original').val(currentSaved);
            }
            if (merged !== undefined && merged !== "") {
                $('#userview-json').val(merged);
            }
            UserviewBuilder.updateFromJson();
            UserviewBuilder.showMessage("");
            
            if (callback) {
                callback.call(thisObject, merged);
            }
        });
    },
    
    mergeAndSave: function() {
        UserviewBuilder.merge(UserviewBuilder.save);
    }  
}
