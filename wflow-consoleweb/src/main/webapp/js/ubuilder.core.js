UserviewBuilder = {
    mode: "userview",
    selectedMenu: null,
    availableThemeConfigPlugin : {},
    screenshotFrame : null,
    screenshots : {},
    
    /*
     * Intialize the builder, called from CustomBuilder.initBuilder
     */
    initBuilder: function (callback) {
        
        UserviewBuilder.initUserviewElements();
        
        CustomBuilder.Builder.init({
            callbacks : {
                "initComponent" : "UserviewBuilder.initComponent",
                "renderElement" : "UserviewBuilder.renderElement",
                "selectElement" : "UserviewBuilder.selectElement",
                "updateElementId" : "UserviewBuilder.updateElementId",
                "unselectElement" : "UserviewBuilder.unselectElement",
                "renderXray" : "UserviewBuilder.renderXray",
                "renderPermission" : "UserviewBuilder.renderPermission"
            }
        }, function() {
            CustomBuilder.Builder.setHead('<link data-datalist-style href="' + CustomBuilder.contextPath + '/css/datalist8.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<link data-form-style href="' + CustomBuilder.contextPath + '/css/form8.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<link data-userview-style href="' + CustomBuilder.contextPath + '/css/userview8.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<link data-ubuilder-style href="' + CustomBuilder.contextPath + '/css/ubuilder.css" rel="stylesheet" />');
            
            //create another iframe for page layout screenshot
            $("#builder_canvas").prepend('<div id="screnshot_iframe_wrapper" style="width: 100%;height: 100%;border: none;overflow: hidden;position: absolute;top:0;left:0;display: block;opacity: 0;padding: 0;z-index:-1;"><iframe src="about:none" id="iframe_screenshot" style="width:100%;height:100%;opacity: 1;"></iframe></div>');
            UserviewBuilder.screenshotFrame = $("#iframe_screenshot").get(0);
            $("#iframe_screenshot").on("load", function(){
                var frameHead = $(UserviewBuilder.screenshotFrame.contentWindow.document).find("head");
                frameHead.append('<link data-datalist-style href="' + CustomBuilder.contextPath + '/css/datalist8.css" rel="stylesheet" />');
                frameHead.append('<link data-form-style href="' + CustomBuilder.contextPath + '/css/form8.css" rel="stylesheet" />');
                frameHead.append('<link data-userview-style href="' + CustomBuilder.contextPath + '/css/userview8.css" rel="stylesheet" />');
                frameHead.append('<link data-ubuilder-style href="' + CustomBuilder.contextPath + '/css/ubuilder.css" rel="stylesheet" />');
                
                var frameBody = $(UserviewBuilder.screenshotFrame.contentWindow.document).find("body");
                frameBody.append('<div id="content" style="padding:0"><main style="padding:0"></main></div>');
            });
            UserviewBuilder.screenshotFrame.src = CustomBuilder.contextPath+'/builder/blank.jsp';
            
            $(CustomBuilder.Builder.iframe).off("change.builder");
            $(CustomBuilder.Builder.iframe).on("change.builder", function() {
                if (CustomBuilder.Builder.selectedEl === null) {
                    UserviewBuilder.removeMenuSnapshot();
                }
            });
            
            callback();
            
            UserviewBuilder.initThemeConfigPluginList();
            CustomBuilder.initPermissionList("org.joget.apps.userview.model.UserviewPermission");
        });
    },
    
    /*
     * retrieve available theme plugin
     */
    initThemeConfigPluginList: function(deferreds) {
        $.getJSON(
            CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.apps.userview.model.SupportBuilderColorConfig',
            function(returnedData){
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        UserviewBuilder.availableThemeConfigPlugin[returnedData[e].value] = returnedData[e].label;
                    }
                }
            }
        );
    },
    
    /*
     * Initialize builder components for userview 
     */
    initUserviewElements: function() {
        //userview
        CustomBuilder.initPaletteElement("", "org.joget.apps.userview.model.Userview", "", "", "", "", false, "", {builderTemplate: {
            getChildsDataHolder : function(elementObj, component) {
                return "categories";
            },
            getChildsContainerAttr: function(elementObj, component) {
                return "categories";
            }
        }});
    
        CustomBuilder.initPaletteElement("", "org.joget.apps.userview.model.UserviewPage", "", "", "", "", false, "", {builderTemplate: {
        }});
    
        CustomBuilder.initPaletteElement("", "userview-header", get_cbuilder_msg('ubuilder.header'), "<i class=\"fas fa-heading\"></i>",  
            UserviewBuilder.getHeaderProperties()
        , "", false, "", {builderTemplate: {
            'draggable' : false,
            'movable' : false,
            'deletable' : false,
            'copyable' : false,
            'navigable' : false,
            'stylePrefix' : 'header',
            'render' : function(element, elementObj, component, callback) {
                UserviewBuilder.updateThemeStyle();
                callback(element);
            },
            'isSupportProperties' : function(elementObj, component) {
                return UserviewBuilder.availableThemeConfigPlugin[CustomBuilder.data.setting.properties.theme.className] !== undefined;
            }
        }});
        CustomBuilder.initPaletteElement("", "userview-welcome-message", get_cbuilder_msg('ubuilder.welcomeMessage'), "<i class=\"fas fa-heading\"></i>",  
            UserviewBuilder.getWelcomeMessageProperties()
        , "", false, "", {builderTemplate: {
            'draggable' : false,
            'movable' : false,
            'deletable' : false,
            'copyable' : false,
            'navigable' : false,
            'stylePrefix' : 'welcome-message',
            'render' : function(element, elementObj, component, callback) {
                element.find("#welcomeMessage").html(elementObj.properties.welcomeMessage);
                UserviewBuilder.updateThemeStyle();
                callback(element);
            }
        }});
        CustomBuilder.initPaletteElement("", "userview-sidebar", get_cbuilder_msg('ubuilder.sidebar'), "<i class=\"fas fa-columns\"></i>", 
            UserviewBuilder.getSidebarProperties()
        , "", false, "", {builderTemplate: {
            'draggable' : false,
            'movable' : false,
            'deletable' : false,
            'copyable' : false,
            'navigable' : false,
            'supportProperties' : false,
            'stylePrefix' : 'sidebar',
            'render' : function(element, elementObj, component, callback) {
                UserviewBuilder.updateThemeStyle();
                callback(element);
            },
            'isSupportProperties' : function(elementObj, component) {
                return UserviewBuilder.availableThemeConfigPlugin[CustomBuilder.data.setting.properties.theme.className] !== undefined;
            }
        }});
        CustomBuilder.initPaletteElement("", "userview-brand-logo", get_cbuilder_msg('ubuilder.brandLogo'), "<i class=\"far fa-image\"></i>",  
            UserviewBuilder.getBrandLogoProperties()
        , "", false, "", {builderTemplate: {
            'draggable' : false,
            'movable' : false,
            'deletable' : false,
            'copyable' : false,
            'stylePrefix' : 'brand-logo',
            'render' : function(element, elementObj, component, callback) {
                var logoUrl = elementObj.properties.logo;
                var img = "";
                if (logoUrl !== undefined && logoUrl !== "") {
                    if (logoUrl.indexOf("#appResource.") === 0) {
                        logoUrl = logoUrl.replace('#appResource.', CustomBuilder.contextPath + '/web/app/' + CustomBuilder.appId + '/resources/');
                        logoUrl = logoUrl.replace('#', '');
                    }
                    img += '<img src="'+logoUrl+'"/>';
                }
                element.html(img);
                UserviewBuilder.updateThemeStyle();
                callback(element);
            }
        }});
        CustomBuilder.initPaletteElement("", "userview-name", get_cbuilder_msg('ubuilder.userviewName'), "<i class=\"fas fa-heading\"></i>",  
            UserviewBuilder.getBrandNameProperties()
        , "", false, "", {builderTemplate: {
            'draggable' : false,
            'movable' : false,
            'deletable' : false,
            'copyable' : false,
            'stylePrefix' : 'brand-name',
            'render' : function(element, elementObj, component, callback) {
                element.find("> span").html(elementObj.properties.name);
                UserviewBuilder.updateThemeStyle();
                callback(element);
            }
        }});
        CustomBuilder.initPaletteElement("", "userview-categories", get_cbuilder_msg('ubuilder.categories'), "<i class=\"fas fa-folder\"></i>", "", "", false, "", {builderTemplate: {
            'draggable' : false,
            'movable' : false,
            'deletable' : false,
            'copyable' : false,
            'supportProperties' : false,
            'stylePrefix' : 'categories',
            'getChildsDataHolder' : function(elementObj, component) {
                return "categories";
            },
            'getChildsContainerAttr' : function(elementObj, component) {
                return "categories";
            },
            'render' : function(element, elementObj, component, callback) {
                UserviewBuilder.updateThemeStyle();
                callback(element);
            }
        }});
        CustomBuilder.initPaletteElement("", "userview-breadcrumb", get_cbuilder_msg('ubuilder.breadcrumb'), "<i class=\"fas fa-font\"></i>", "", "", false, "", {builderTemplate: {
            'draggable' : false,
            'movable' : false,
            'deletable' : false,
            'copyable' : false,
            'navigable' : false,
            'supportProperties' : false,
            'stylePrefix' : 'breadcrumb',
            'render' : function(element, elementObj, component, callback) {
                UserviewBuilder.updateThemeStyle();
                callback(element);
            }
        }});
        CustomBuilder.initPaletteElement("", "userview-footer", get_cbuilder_msg('ubuilder.footer'), "<i class=\"fas fa-font\"></i>",  
            UserviewBuilder.getFooterProperties()
        , "", false, "", {builderTemplate: {
            'draggable' : false,
            'movable' : false,
            'deletable' : false,
            'copyable' : false,
            'navigable' : false,
            'stylePrefix' : 'footer',
            'render' : function(element, elementObj, component, callback) {
                element.find("#footerMessage").html(elementObj.properties.footerMessage);
                UserviewBuilder.updateThemeStyle();
                callback(element);
            },
            'customPropertyOptions' : function(elementOptions, element, elementObj, component) {
                if (UserviewBuilder.availableThemeConfigPlugin[CustomBuilder.data.setting.properties.theme.className] !== undefined) {
                    return UserviewBuilder.getFooterColorProperties()
                } else {
                    return elementOptions;
                }
            }
        }});
        CustomBuilder.initPaletteElement("", "menu-component", get_cbuilder_msg('ubuilder.menuComponent'), "", "", "", false, "", {builderTemplate: {
            'deletable' : false,
            'copyable' : false,
            'getLabel' : function(elementObj, component) {
                var classname = UserviewBuilder.selectedMenu.className;
                var self = CustomBuilder.Builder;
                var actualComponent = self.getComponent(classname);
                
                return CustomBuilder.Builder._getElementType(UserviewBuilder.selectedMenu, actualComponent);
            },
            'customPropertyOptions' : function(elementOptions, element, elementObj, component) {
                var classname = UserviewBuilder.selectedMenu.className;
                var self = CustomBuilder.Builder;
                var actualComponent = self.getComponent(classname);
                
                var elementOptions = actualComponent.propertyOptions;
                if (actualComponent.builderTemplate !== undefined && actualComponent.builderTemplate.customPropertyOptions !== undefined) {
                    elementOptions = actualComponent.builderTemplate.customPropertyOptions(elementOptions, element, UserviewBuilder.selectedMenu, actualComponent);
                }
                return elementOptions;
            },
            'customPropertiesData' : function(properties, elementObj, component) {
                return UserviewBuilder.selectedMenu.properties;
            }
        }});
    },
    
    getHeaderProperties : function() {
        return [
            {
                title: get_cbuilder_msg('ubuilder.themeColor'),
                properties : [
                    {
                        name : 'dx8headerColor',
                        label : get_cbuilder_msg('ubuilder.headerColor'),
                        type : 'color'
                    },
                    {
                        name : 'dx8headerFontColor',
                        label : get_cbuilder_msg('ubuilder.headerFontColor'),
                        type : 'color'
                    }
                ]
            }
        ];
    },
    
    getSidebarProperties : function() {
        return [
            {
                title: get_cbuilder_msg('ubuilder.themeColor'),
                properties : [
                    {
                        name : 'dx8navBackground',
                        label : get_cbuilder_msg('ubuilder.navBackground'),
                        type : 'color'
                    },
                    {
                        name : 'dx8navLinkBackground',
                        label : get_cbuilder_msg('ubuilder.navLinkBackground'),
                        type : 'color'
                    },
                    {
                        name : 'dx8navLinkColor',
                        label : get_cbuilder_msg('ubuilder.navLinkColor'),
                        type : 'color'
                    },
                    {
                        name : 'dx8navLinkIcon',
                        label : get_cbuilder_msg('ubuilder.navLinkIcon'),
                        type : 'color'
                    },
                    {
                        name : 'dx8navActiveLinkBackground',
                        label : get_cbuilder_msg('ubuilder.navActiveLinkBackground'),
                        type : 'color'
                    },
                    {
                        name : 'dx8navActiveLinkColor',
                        label : get_cbuilder_msg('ubuilder.navActiveLinkColor'),
                        type : 'color'
                    },
                    {
                        name : 'dx8navActiveIconColor',
                        label : get_cbuilder_msg('ubuilder.navActiveIconColor'),
                        type : 'color'
                    },
                    {
                        name : 'dx8navBadge',
                        label : get_cbuilder_msg('ubuilder.navBadge'),
                        type : 'color'
                    },
                    {
                        name : 'dx8navBadgeText',
                        label : get_cbuilder_msg('ubuilder.navBadgeText'),
                        type : 'color'
                    }
                ]
            }
        ];
    },
    
    getBrandLogoProperties : function () {
        return [
            {
                title: get_cbuilder_msg('ubuilder.editBrandLogo'),
                properties : [
                    {
                        name : 'logo',
                        label : get_cbuilder_msg('ubuilder.logo'),
                        type: 'image',
                        appPath: CustomBuilder.appPath,
                        allowInput : 'true',
                        isPublic : 'true',
                        imageSize : 'width:80px; height:35px; background-size: contain; background-repeat: no-repeat;'
                    }
                ]
            }
        ];
    },
    
    getBrandNameProperties : function () {
        return [
            {
                title: get_cbuilder_msg('ubuilder.editBrandName'),
                properties : [
                    {
                        name : 'name',
                        label : get_cbuilder_msg('ubuilder.brandName'),
                        type : 'textarea'
                    }
                ]
            }
        ];
    },
    
    getWelcomeMessageProperties : function () {
        return [
            {
                title: get_cbuilder_msg('ubuilder.editWelcomeMessage'),
                properties : [
                    {
                        name : 'welcomeMessage',
                        label : get_cbuilder_msg('ubuilder.welcomeMessage'),
                        type : 'textarea'
                    }
                ]
            }
        ];
    },
    
    getFooterProperties : function () {
        return [
            {
                title: get_cbuilder_msg('ubuilder.editFooter'),
                properties : [
                    {
                        name : 'footerMessage',
                        label : get_cbuilder_msg('ubuilder.footerMessage'),
                        type : 'textarea'
                    }
                ]
            }
        ];
    },
    
    getFooterColorProperties : function () {
        return [
            {
                title: get_cbuilder_msg('ubuilder.editFooter'),
                properties : [
                    {
                        name : 'footerMessage',
                        label : get_cbuilder_msg('ubuilder.footerMessage'),
                        type : 'textarea'
                    },
                    {
                        name : 'dx8footerBackground',
                        label : get_cbuilder_msg('ubuilder.footerBackground'),
                        type : 'color'
                    },
                    {
                        name : 'dx8footerColor',
                        label : get_cbuilder_msg('ubuilder.footerColor'),
                        type : 'color'
                    }
                ]
            }
        ];
    },
    
    /*
     * Get the properties for Propertise View
     */
    getBuilderProperties : function() {
        return CustomBuilder.data.setting.properties;
    },
    
    /*
     * Save properties from properties view
     */
    saveBuilderProperties : function(container, properties) {
        $.extend(CustomBuilder.data.setting.properties, properties);
        CustomBuilder.update();
        
        var combinedProperties = $.extend(true, {}, CustomBuilder.data.properties, CustomBuilder.data.setting.properties.theme.properties);
        var userviewElement = CustomBuilder.Builder.frameBody;
        userviewElement.find('[data-cbuilder-classname="userview-header"]').data("data", {className: "userview-header", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-welcome-message"]').data("data", {className: "userview-welcome-message", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-sidebar"]').data("data", {className: "userview-sidebar", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-brand-logo"]').data("data", {className: "userview-brand-logo", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-name"]').data("data", {className: "userview-name", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-categories"]').data("data", {className: "userview-categories", properties: combinedProperties, categories: CustomBuilder.data.categories});
        userviewElement.find('[data-cbuilder-classname="userview-breadcrumb"]').data("data", {className: "userview-breadcrumb", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-footer"]').data("data", {className: "userview-footer", properties: combinedProperties});
        
        UserviewBuilder.updateThemeStyle();
    },
    
    /*
     * Create default userview data model when it is empty
     */
    initDefaultUserviewDataModel : function() {
        var d = new Object();
        d['className'] = "org.joget.apps.userview.model.Userview";
        d['properties'] = new Object();
        d.properties['id'] = CustomBuilder.id;
        d.properties['name'] = get_cbuilder_msg('ubuilder.userviewName');
        d.properties['description'] = get_cbuilder_msg('ubuilder.userviewDescription');
        d.properties['welcomeMessage'] = get_cbuilder_msg('ubuilder.welcomeMessage');
        d.properties['logoutText'] = get_cbuilder_msg('ubuilder.logout');
        d.properties['footerMessage'] = get_cbuilder_msg('ubuilder.poweredBy'),
        d['setting'] = new Object();
        d.setting['className'] = "org.joget.apps.userview.model.UserviewSetting";
        d.setting['properties'] = new Array();
        d['categories'] = new Array();

        CustomBuilder.data = d;
        CustomBuilder.update(false);
        
        return CustomBuilder.data;
    },
    
    /*
     * Use to migrate some v5 properties to v6
     */
    v5PropertiesMigration : function(data) {
        //handle for v5 app on property migration
        if (data.setting.properties['loginPageTop'] !== undefined) {
            data.setting.properties['theme']['properties']['loginPageTop'] = data.setting.properties['loginPageTop'];
            data.setting.properties['theme']['properties']['loginPageBottom'] = data.setting.properties['loginPageBottom'];
            delete data.setting.properties['loginPageTop'];
            delete data.setting.properties['loginPageBottom'];
            
            CustomBuilder.update(false);
        }
        if (data.setting.properties['mobileViewDisabled'] !== undefined) {
            data.setting.properties['theme']['properties']['mobileViewDisabled'] = data.setting.properties['mobileViewDisabled'];
            data.setting.properties['theme']['properties']['mobileCacheEnabled'] = data.setting.properties['mobileCacheEnabled'];
            data.setting.properties['theme']['properties']['mobileLoginRequired'] = data.setting.properties['mobileLoginRequired'];
            data.setting.properties['theme']['properties']['mobileViewBackgroundUrl'] = data.setting.properties['mobileViewBackgroundUrl'];
            data.setting.properties['theme']['properties']['mobileViewBackgroundColor'] = data.setting.properties['mobileViewBackgroundColor'];
            data.setting.properties['theme']['properties']['mobileViewBackgroundStyle'] = data.setting.properties['mobileViewBackgroundStyle'];
            data.setting.properties['theme']['properties']['mobileViewTranslucent'] = data.setting.properties['mobileViewTranslucent'];
            data.setting.properties['theme']['properties']['mobileViewLogoUrl'] = data.setting.properties['mobileViewLogoUrl'];
            data.setting.properties['theme']['properties']['mobileViewLogoWidth'] = data.setting.properties['mobileViewLogoWidth'];
            data.setting.properties['theme']['properties']['mobileViewLogoHeight'] = data.setting.properties['mobileViewLogoHeight'];
            data.setting.properties['theme']['properties']['mobileViewLogoAlign'] = data.setting.properties['mobileViewLogoAlign'];
            data.setting.properties['theme']['properties']['mobileViewCustomCss'] = data.setting.properties['mobileViewCustomCss'];
            delete data.setting.properties['mobileViewDisabled'];
            delete data.setting.properties['mobileCacheEnabled'];
            delete data.setting.properties['mobileLoginRequired'];
            delete data.setting.properties['mobileViewBackgroundUrl'];
            delete data.setting.properties['mobileViewBackgroundColor'];
            delete data.setting.properties['mobileViewBackgroundStyle'];
            delete data.setting.properties['mobileViewTranslucent'];
            delete data.setting.properties['mobileViewLogoUrl'];
            delete data.setting.properties['mobileViewLogoWidth'];
            delete data.setting.properties['mobileViewLogoHeight'];
            delete data.setting.properties['mobileViewLogoAlign'];
            delete data.setting.properties['mobileViewCustomCss'];
            
            CustomBuilder.update(false);
        }
    },
    
    /*
     * Load and render data, called from CustomBuilder.loadJson
     */
    load: function (data) {
        if (UserviewBuilder.mode === "page") {
            UserviewBuilder.loadContentPage();
        } else {
            $(".drag-elements-sidepane li.component").hide();
            
            if(data.properties === null || data.properties === undefined){
                data = UserviewBuilder.initDefaultUserviewDataModel();
            } else {
                UserviewBuilder.v5PropertiesMigration(data);
            }
        
            CustomBuilder.Builder.load(data, function(){
                if (UserviewBuilder.selectedMenu !== undefined && UserviewBuilder.selectedMenu !== null) {
                    var self = CustomBuilder.Builder;
                    
                    var id = UserviewBuilder.selectedMenu.properties.id;
                    self.selectNode(self.frameBody.find('[data-cbuilder-id="'+id+'"]'));
                }
            });
        }
    },
    
    /*
     * Load and render content page, called from UserviewBuilder.load
     */
    loadContentPage : function() {
        $(".drag-elements-sidepane li.component").show();
        
        var menu = UserviewBuilder.selectedMenu;
        if (menu.referencePage === undefined) {
            menu.referencePage = {
                className : "org.joget.apps.userview.model.UserviewPage",
                properties : {
                    id : "up-"+ menu.properties.id
                },
                elements : [
                    {
                        className : "menu-component"
                    }
                ]
            };
        }
        UserviewBuilder.mode = "page";
        CustomBuilder.Builder.load(menu.referencePage);
    },
    
    /*
     * Initialize the builder component behaviour for each elements.
     * Called from CustomBuilder.Builder.getComponent
     */
    initComponent : function(component) {
        if (component.className === "org.joget.apps.userview.model.UserviewCategory") {
            component.builderTemplate.parentContainerAttr = "categories";
            component.builderTemplate.childsContainerAttr = "menus";
            component.builderTemplate.getChildsDataHolder = function(elementObj, component) {
                return "menus";
            };
            component.builderTemplate.getParentDataHolder = function(elementObj, component) {
                return "categories";
            };
            component.builderTemplate.supportStyle = false;
        } else if (component.className.indexOf("userview-") === 0) {
            
        } else if (component.type === "menu") {
            component.builderTemplate.getParentContainerAttr = function(elementObj, component) {
                if (UserviewBuilder.mode === "userview") {
                    return "menus";
                } else {
                    return "elements";
                }
            };
            component.builderTemplate.getParentDataHolder = function(elementObj, component) {
                if (UserviewBuilder.mode === "userview") {
                    return "menus";
                } else {
                    return "elements";
                }
            };
            component.builderTemplate.getDragHtml = function(component) {
                if (UserviewBuilder.mode === "userview") {
                    return '<li><a href="" class="menu-link default"><span>'+component.label+'</span></a></li>';
                } else {
                    return this.dragHtml;
                }
            };
            component.builderTemplate.supportStyle = false;
            
            //change label to icon text field
            var found = false;
            for (var i in component.propertyOptions) {
                for (var r in component.propertyOptions[i].properties) {
                    if (component.propertyOptions[i].properties[r].name === "label") {
                        component.propertyOptions[i].properties[r].type = "icon-textfield";
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }
    },
    
    /*
     * A callback method called from CustomBuilder.applyElementProperties when properties saved
     */
    saveEditProperties : function(container, elementProperty, element) {
        if (element.className.indexOf("userview-") === 0) {
            CustomBuilder.data.properties.description = elementProperty.description;
            CustomBuilder.data.properties.footerMessage = elementProperty.footerMessage;
            CustomBuilder.data.properties.logoutText = elementProperty.logoutText;
            CustomBuilder.data.properties.name = elementProperty.name;
            CustomBuilder.data.properties.welcomeMessage = elementProperty.welcomeMessage;
            
            var temp = $.extend({}, elementProperty);
            delete temp.description;
            delete temp.footerMessage;
            delete temp.logoutText;
            delete temp.name;
            delete temp.welcomeMessage;
            delete temp.id;
            
            //remove all styling before merge new changes
            var orgProperty = CustomBuilder.data.setting.properties.theme.properties;
            for (var property in orgProperty) {
                if (orgProperty.hasOwnProperty(property)) {
                    if ((property.indexOf('attr-') === 0 || property.indexOf('css-') === 0 || property.indexOf('style-') === 0
                        || property.indexOf('-attr-') > 0 || property.indexOf('-css-') > 0 || property.indexOf('-style-') > 0)) {
                        delete orgProperty[property];
                    }
                }
            }
            
            $.extend(orgProperty, temp);
        }
    },
    
    /*
     * A callback method called from the default component.builderTemplate.render method
     */
    renderElement : function(element, elementObj, component, callback) {
        if (component.builderTemplate.getHtml() === undefined) {
            if (UserviewBuilder.mode === "userview") {
                if (elementObj.className === "org.joget.apps.userview.model.Userview") {
                    UserviewBuilder.renderUserview(element, elementObj, component, callback);
                } else if (elementObj.className === "org.joget.apps.userview.model.UserviewCategory") {
                    UserviewBuilder.renderCategory(element, elementObj, component, callback);
                } else {
                    UserviewBuilder.renderElementAjax(element, elementObj, component, callback, "menu");
                }
            } else {
                if (elementObj.className === "org.joget.apps.userview.model.UserviewPage") {
                    UserviewBuilder.renderUserviewPage(element, elementObj, component, callback);
                } else if (elementObj.className === "menu-component") {
                    UserviewBuilder.renderMenuComponent(element, elementObj, component, callback);
                } else {
                    UserviewBuilder.renderElementAjax(element, elementObj, component, callback, "component");
                }
            }
        } else {
            callback(element);
        }
    },
    
    /*
     * Retrieve element template using ajax and render element. Called from UserviewBuilder.renderElement
     */
    renderElementAjax : function(element, elementObj, component, callback, type) {
        var jsonStr = JSON.encode(elementObj);
        $.ajax({
            type: "POST",
            data: {"json": jsonStr },
            url: CustomBuilder.contextPath + '/web/ubuilder/app/' + CustomBuilder.appId + '/' + CustomBuilder.appVersion + '/'+ CustomBuilder.data.properties.id +'/'+type+'/template',
            dataType : "text",
            beforeSend: function (request) {
               request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            },
            success: function(response) {
                //remove onclick inline event
                response = response.replace("onclick=", "xonclick=");
                
                var newElement = null;
                if (type === "menu") {
                    newElement = $('<li id="'+elementObj.properties.id+'" class="menu">' + response + '</li>');
                } else {
                    newElement = $(response);
                }
                $(element).replaceWith(newElement);
                callback(newElement);
            }
        });
    },
    
    /*
     * used to render userview. Called from UserviewBuilder.renderElement
     */
    renderUserview : function(element, elementObj, component, callback) {
        var html = '<div id="page">';
        html += '<header class="navbar" data-cbuilder-classname="userview-header"><div class="navbar-inner"><div class="container-fluid"><div class="hi-trigger ma-trigger" id="sidebar-trigger"><div class="line-wrap"><div class="line top"></div><div class="line center"></div><div class="line bottom"></div></div></div>';
        
        //header message
        html += '<div id="header-message" class=""><div id="header-welcome-message" class="" data-cbuilder-classname="userview-welcome-message"><span id="welcomeMessage">'+elementObj.properties.welcomeMessage+'</span></div><div class="clearfix"></div></div>';
        
        //home icon
        html += '<div class="nav-no-collapse header-nav"><ul class="nav pull-right"><li class=""><a class="btn" href="" title="Home"><i class="fa fa-home"></i></a></li>';
        
        //inbox
        html += '<li class="inbox-notification dropdown"><a class="btn dropdown-toggle"><i class="fa fa-tasks white"></i><span class="badge red">1</span></a>';
        
        //shortcut
        html += '<li class="shortcut-link dropdown"><a class="btn dropdown-toggle"><i class="fa fa-th-list white"></i></a></ul></div></div></div></header>';
        
        //sidebar
        html += '<div id="main" class="container-fluid-full"><div class="row-fluid"><div id="sidebar" class="span2" data-cbuilder-classname="userview-sidebar">';
        
        //logo
        html += '<div class="sidebar_brand"><div class="logo_container" data-cbuilder-classname="userview-brand-logo">';
        if (elementObj.setting.properties.theme !== undefined) {
            var logoUrl = elementObj.setting.properties.theme.properties.logo;
            if (logoUrl !== undefined && logoUrl !== "") {
                if (logoUrl.indexOf("#appResource.") === 0) {
                    logoUrl = logoUrl.replace('#appResource.', CustomBuilder.contextPath + '/web/app/' + CustomBuilder.appId + '/resources/');
                    logoUrl = logoUrl.replace('#', '');
                }
                html += '<img src="'+logoUrl+'"/>';
            }
        }
        html += '</div>';
        
        //userview name
        html += '<a href="*" id="header-link" class="" data-cbuilder-classname="userview-name"><span>'+elementObj.properties.name+'</span></a></div>';
        
        html += '<nav id="navigation" class="nav-collapse sidebar-nav">';
        
        //category container
        html += '<ul id="category-container" class="nav nav-tabs nav-stacked main-menu" data-cbuilder-classname="userview-categories" data-cbuilder-categories></ul></nav></div>';
        
        html += '<div id="content" class="span10"><main>';
        
        //breadcrumb
        html += '<ul class="breadcrumb" data-cbuilder-classname="userview-breadcrumb"><li><i class="fa fa-home"></i> <a href="*">'+get_cbuilder_msg('ubuilder.home')+'</a> <i class="fa fa-angle-right"></i></li><li><a>'+get_cbuilder_msg('ubuilder.page')+'</a></li></ul>';
        
        html += '<div class="userview-body-content"><div class="center"><p>'+get_cbuilder_msg('ubuilder.content')+'</p><p id="btn_container" style="display:none"><button id="edit-content-btn" class="btn btn-primary">'+get_cbuilder_msg('ubuilder.editContentLayout')+'</button></p></div></div>';
        html += '</main></div></div></div><div class="clearfix"></div>';
        
        //footer
        html += '<footer class="" data-cbuilder-classname="userview-footer"><div id="footer-message"><p><span id="footerMessage">'+elementObj.properties.footerMessage+'</span></p></div></footer>';
        html += '</div>';
        
        var userviewElement = $(html);
        
        var combinedProperties = $.extend(true, {}, elementObj.properties, elementObj.setting.properties.theme.properties);
        
        userviewElement.find('[data-cbuilder-classname="userview-header"]').data("data", {className: "userview-header", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-welcome-message"]').data("data", {className: "userview-welcome-message", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-sidebar"]').data("data", {className: "userview-sidebar", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-brand-logo"]').data("data", {className: "userview-brand-logo", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-name"]').data("data", {className: "userview-name", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-categories"]').data("data", {className: "userview-categories", properties: combinedProperties, categories: elementObj.categories});
        userviewElement.find('[data-cbuilder-classname="userview-breadcrumb"]').data("data", {className: "userview-breadcrumb", properties: combinedProperties});
        userviewElement.find('[data-cbuilder-classname="userview-footer"]').data("data", {className: "userview-footer", properties: combinedProperties});
        
        userviewElement.attr("data-cbuilder-uneditable", "");
        userviewElement.attr("data-cbuilder-unselectable", "");
        
        userviewElement.find("#navigation").off("scroll resize");
        userviewElement.find("#navigation").on("scroll resize", function(){
            CustomBuilder.Builder._updateBoxes();
        });
        
        userviewElement.find("#edit-content-btn").off("click");
        userviewElement.find("#edit-content-btn").on("click", function() {
            UserviewBuilder.loadContentPage();
        });
        
        $(element).replaceWith(userviewElement);
        
        UserviewBuilder.updateThemeStyle();
        callback(userviewElement);
    },
    
    /*
     * Apply the theme style for header, welcome messge, sidebar, brand logo, brand name categories, breadcrumb and footer
     */
    updateThemeStyle : function() {
        if (UserviewBuilder.mode === "userview") {
            var builder = CustomBuilder.Builder;
            var element = builder.frameBody.find("#page");
            var props = CustomBuilder.data.setting.properties.theme.properties;
            
            builder.handleStylingProperties(element, props, "header", "header.navbar");
            builder.handleStylingProperties(element, props, "welcome-message", "#welcomeMessage");
            builder.handleStylingProperties(element, props, "sidebar", "#sidebar");
            builder.handleStylingProperties(element, props, "categories", "#category-container");
            builder.handleStylingProperties(element, props, "footer", "footer");
            builder.handleStylingProperties(element, props, "breadcrumb", ".breadcrumb");
            builder.handleStylingProperties(element, props, "brand-name", ".sidebar_brand #header-link");
            builder.handleStylingProperties(element, props, "brand-logo", ".sidebar_brand .logo_container img");
            
            element.find('> style[data-cbuilder-style="calculatedThemeStyle"]').remove();
            if (props.dx8background !== undefined) {
                var css = "<style data-cbuilder-style='calculatedThemeStyle'>body{";
                if (props.dx8background !== undefined  && props.dx8background !== "") {
                    css += "--theme-background:"+props.dx8background+";";
                }
                if (props.dx8contentbackground !== undefined  && props.dx8contentbackground !== "") {
                    css += "--theme-content-background:"+props.dx8contentbackground+";";
                }
                if (props.dx8headerColor !== undefined  && props.dx8headerColor !== "") {
                    css += "--theme-header:"+props.dx8headerColor+";";
                }
                if (props.dx8headerFontColor !== undefined  && props.dx8headerFontColor !== "") {
                    css += "--theme-header-font:"+props.dx8headerFontColor+";";
                }
                if (props.dx8navBackground !== undefined  && props.dx8navBackground !== "") {
                    css += "--theme-sidebar:"+props.dx8navBackground+";";
                }
                if (props.dx8navLinkBackground !== undefined  && props.dx8navLinkBackground !== "") {
                    css += "--theme-sidebar-link-bg:"+props.dx8navLinkBackground+";";
                }
                if (props.dx8navLinkColor !== undefined  && props.dx8navLinkColor !== "") {
                    css += "--theme-sidebar-link:"+props.dx8navLinkColor+";";
                }
                if (props.dx8navLinkIcon !== undefined  && props.dx8navLinkIcon !== "") {
                    css += "--theme-sidebar-icon:"+props.dx8navLinkIcon+";";
                }
                if (props.dx8navBadge !== undefined  && props.dx8navBadge !== "") {
                    css += "--theme-sidebar-badge:"+props.dx8navBadge+";";
                }
                if (props.dx8navBadgeText !== undefined  && props.dx8navBadgeText !== "") {
                    css += "--theme-sidebar-badge-text:"+props.dx8navBadgeText+";";
                }
                if (props.dx8navLinkIcon !== undefined  && props.dx8navLinkIcon !== "") {
                    css += "--theme-sidebar-icon:"+props.dx8navLinkIcon+";";
                }
                if (props.dx8navActiveLinkBackground !== undefined  && props.dx8navActiveLinkBackground !== "") {
                    css += "--theme-sidebar-active-link-bg:"+props.dx8navActiveLinkBackground+";";
                }
                if (props.dx8navActiveLinkColor !== undefined  && props.dx8navActiveLinkColor !== "") {
                    css += "--theme-sidebar-active-link:"+props.dx8navActiveLinkColor+";";
                }
                if (props.dx8navActiveIconColor !== undefined  && props.dx8navActiveIconColor !== "") {
                    css += "--theme-sidebar-active-icon:"+props.dx8navActiveIconColor+";";
                }
                if (props.dx8buttonBackground !== undefined  && props.dx8buttonBackground !== "") {
                    css += "--theme-button-bg:"+props.dx8buttonBackground+";";
                }
                if (props.dx8buttonColor !== undefined  && props.dx8buttonColor !== "") {
                    css += "--theme-button:"+props.dx8buttonColor+";";
                }
                if (props.dx8primaryColor !== undefined  && props.dx8primaryColor !== "") {
                    css += "--theme-primary:"+props.dx8primaryColor+";";
                }
                if (props.dx8fontColor !== undefined  && props.dx8fontColor !== "") {
                    css += "--theme-font-color:"+props.dx8fontColor+";";
                }
                if (props.dx8footerBackground !== undefined  && props.dx8footerBackground !== "") {
                    css += "--theme-footer-bg:"+props.dx8footerBackground+";";
                }
                if (props.dx8footerColor !== undefined  && props.dx8footerColor !== "") {
                    css += "--theme-footer:"+props.dx8footerColor+";";
                }
                if (props.dx8linkColor !== undefined  && props.dx8linkColor !== "") {
                    css += "--theme-link:"+props.dx8linkColor+";";
                }
                if (props.dx8linkActiveColor !== undefined  && props.dx8linkActiveColor !== "") {
                    css += "--theme-link-active:"+props.dx8linkActiveColor+";";
                }
                css += "}</style>";
                element.append(css);
            }
        }
    },
    
    /*
     * used to render category. Called from UserviewBuilder.renderElement
     */
    renderCategory : function(element, elementObj, component, callback) {
        var html = '<li id="'+elementObj.properties.id+'" class="category toggled"><a class="dropdown"><span>'+elementObj.properties.label+'</span></a><ul class="menu-container" data-cbuilder-menus></ul></li>';
        var category = $(html);
        $(element).replaceWith(category);
        callback(category);
    },
    
    /*
     * used to render userview oage. Called from UserviewBuilder.renderElement
     */
    renderUserviewPage : function(element, elementObj, component, callback) {
        var html = '<div id="page-content-wrapper" data-cbuilder-uneditable data-cbuilder-unselectable>';
        html += '<div class="bar-var"><button id="save-content-btn" class="btn btn-primary"><i class="las la-undo"></i> '+get_cbuilder_msg('ubuilder.doneEditContentLayout')+'</button></div>';
        html += '<div id="content"><main data-cbuilder-elements></main></div>';
        html += '</div>';
        
        var userviewElement = $(html);
        
        userviewElement.find("#save-content-btn").off("click");
        userviewElement.find("#save-content-btn").on("click", function() {
            UserviewBuilder.mode = "userview";
            UserviewBuilder.load(CustomBuilder.data);
        });
        
        $(element).replaceWith(userviewElement);
        callback(userviewElement);
    },
    
    /*
     * used to render menu. Called from UserviewBuilder.renderElement
     */
    renderMenuComponent : function(element, elementObj, component, callback) {
        var self = CustomBuilder.Builder;
        
        var classname = UserviewBuilder.selectedMenu.className;
        var actualComponent = self.getComponent(classname);
        
        UserviewBuilder.renderElementAjax(element, UserviewBuilder.selectedMenu, actualComponent, callback, "component");
    },
    
    /*
     * A callback method called from the default component.builderTemplate.selectNode method.
     * It used to listen to navigator scroll event
     */
    selectElement : function(element, elementObj, component) {
        var self = CustomBuilder.Builder;
        
        if (UserviewBuilder.mode === "userview") {
            UserviewBuilder.selectedMenu = null;
            UserviewBuilder.removeMenuSnapshot();
        }
        
        if (elementObj.className === "userview-categories") {
            $("#element-select-box #element-options").append('<a id="category-btn" href="" title="'+get_cbuilder_msg('ubuilder.addCategory')+'"><i class="las la-plus"></i></a>');
            
            $("#category-btn").off("click");
            $("#category-btn").on("click", function(event) {
                $("#element-select-box").hide();
                
                UserviewBuilder.addCategory();
                
                event.preventDefault();
                return false;
            });
        } else if (element.closest(".menu-container").length > 0) {
            //is a menu
            UserviewBuilder.selectedMenu = elementObj;
            UserviewBuilder.showMenuSnapshot();
        }
        
        if (element.closest("#navigation").length > 0) {
            var elementOffset = element.offset();
            var navigation = self.frameBody.find("#navigation");
            var topOffset = $(navigation).find("> *:eq(0)").offset().top;
            if (elementOffset.top > navigation.offset().top + navigation.height() - 30) {
                $(navigation).animate({
                    scrollTop: elementOffset.top - topOffset
                }, 1000, function(){
                    self._updateBoxes();
                });
            }
        }
    },
    
    showMenuSnapshot : function() {
        var self = CustomBuilder.Builder;
        var json = JSON.encode(UserviewBuilder.selectedMenu);
        var screenshotKey = UserviewBuilder.selectedMenu.properties.id + "_" + CustomBuilder.hashCode(json);
        
        var renderScreenshot = function(screenshot) {
            self.frameBody.find(".userview-body-content").prepend('<img class="screenshot" src="'+screenshot+'"/>');
        };
        
        self.frameBody.find("#btn_container").show();
        
        if (UserviewBuilder.screenshots[screenshotKey] === undefined) {
            UserviewBuilder.generateMenuSnapshot(json, screenshotKey, renderScreenshot);
        } else {
            renderScreenshot(UserviewBuilder.screenshots[screenshotKey]);
        }
    },
    
    removeMenuSnapshot : function() {
        var self = CustomBuilder.Builder;
        self.frameBody.find("#btn_container").hide();
        self.frameBody.find(".userview-body-content img.screenshot").remove();
    },
    
    generateMenuSnapshot : function(json, screenshotKey, callback) {
         var frameBody = $(UserviewBuilder.screenshotFrame.contentWindow.document).find("body");
         var main = frameBody.find("#content > main");
         
         $.ajax({
            type: "POST",
            data: {"json": json },
            url: CustomBuilder.contextPath + '/web/ubuilder/app/' + CustomBuilder.appId + '/' + CustomBuilder.appVersion + '/'+ CustomBuilder.data.properties.id +'/page/template',
            dataType : "text",
            beforeSend: function (request) {
               request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            },
            success: function(response) {
                $(main).html(response);
                
                html2canvas(main, {
                    logging: true,
                    onrendered: function (canvas) {
                        var image = canvas.toDataURL("image/png");
                        UserviewBuilder.screenshots[screenshotKey] = image;
                        callback(image);
                    }
                });
            }
        });
    },
    
    /*
     * Add category when add action button pressed on navigator
     */
    addCategory : function() {
        var self = CustomBuilder.Builder;
        
        self.component = self.getComponent("org.joget.apps.userview.model.UserviewCategory");
        var classname = self.component.className;
        var elementObj = {
            className: classname,
            properties: {
                label : get_cbuilder_msg('ubuilder.newCategory')
            },
            menus : []
        };
        self.updateElementId(elementObj);
        
        var parent = self.selectedEl;
        var parentDataArray = CustomBuilder.data.categories;
        parentDataArray.push(elementObj);
        
        var temp = $('<div></div>');
        self.selectedEl.append(temp);
        
        self.renderElement(elementObj, temp, self.component, true);
        
        CustomBuilder.update();
    },
    
    /*
     * A callback method called from the CustomBuilder.Builder.renderNodeAdditional
     * It used to render the info of an element
     */
    renderXray : function(detailsDiv, element, elementObj, component , callback) {
        
        
        callback();
    },
    
    /*
     * A callback method called from the CustomBuilder.Builder.renderNodeAdditional
     * It used to render the permission option of an element
     */
    renderPermission : function (detailsDiv, element, elementObj, component , callback) {
        
        callback();
    },
    
    /*
     * A callback method from CustomBuilder.Builder.updateElementId to update id to an unqiue value
     */
    updateElementId : function(elementObj) {
        // set ID if empty or it is copied section
        var elementClass = elementObj.className;
        if (elementClass === "org.joget.apps.userview.model.UserviewCategory") {
            elementObj.properties.id = 'category-'+CustomBuilder.uuid();
        } else {
            elementObj.properties.id = CustomBuilder.uuid();
        }
    }
}