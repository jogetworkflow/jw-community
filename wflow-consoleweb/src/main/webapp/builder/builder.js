/**
 * Customised from https://github.com/givanz/VvvebJs
 */

 _CustomBuilder = {
    isAjaxReady : false,
    saveUrl : '',
    previewUrl : '',
    contextPath : '/jw',
    appId: '',
    appVersion: '',
    appPath: '',
    builderType: '',
    builderLabel: '',
    id: '',
    isViewerWithPE: false, //is current viewer using properties editor for other purpose
    config : {},
    defaultConfig: {
        builder : {
            options : {
                getDefinitionUrl : "",
                rightPropertyPanel : false,
                defaultBuilder : false,
                submitDiff : false //use for saving, prepare diff and post together with json definition
            },
            callbacks : {
                initBuilder : "",
                load : "",
                saveEditProperties : "",
                cancelEditProperties : "",
                getBuilderItemName : "",
                getBuilderProperties : "",
                saveBuilderProperties : ""
            }
        },
        advanced_tools : {
            tree_viewer : {
                disabled : false,
                childs_properties : ["elements"],
                matchers : {
                    'editable' : {
                        match : function (viewer, deferreds, node, jsonObj, refObj) {
                            if (node.data['parent'] === undefined) {
                                DependencyTree.Util.createEditIndicator(viewer, node, function(){
                                    CustomBuilder.showPopUpBuilderProperties();
                                });
                            } else if (jsonObj['className'] !== undefined) {
                                DependencyTree.Util.createEditIndicator(viewer, node, function() {
                                    CustomBuilder.editProperties(jsonObj["className"], jsonObj["properties"]);
                                });
                            }   

                            return false;
                        }
                    }
                }
            },
            xray : {
                 disabled : true
            },
            usage : {
                disabled : false
            },
            i18n : {
                disabled : false,
                keywords : [
                    "label"
                ],
                options : {
                    sort : true,
                    i18nHash : true
                }
            },
            permission : {
                disabled : false,
                permission_plugin : "org.joget.apps.form.model.FormPermission",
                authorized : {
                    property : "hidden",
                    default_value : "",
                    options : [
                        {
                            key : "visible",
                            value : "",
                            label : get_cbuilder_msg("ubuilder.visible")
                        },
                        {
                            key : "hidden",
                            value : "true",
                            label : get_cbuilder_msg("ubuilder.hidden"),
                            disableChild : true
                        }
                    ]
                },
                unauthorized : {
                    property : "",
                    default_value : "",
                    options : [
                        {
                            key : "visible",
                            value : "",
                            label : get_cbuilder_msg("ubuilder.visible")
                        },
                        {
                            key : "hidden",
                            value : "true",
                            label : get_cbuilder_msg("ubuilder.hidden"),
                            disableChild : true
                        }
                    ]
                },
                element_support_plugin : [],
                childs_properties : ["elements"],
                ignore_classes : [],
                render_elements_callback : ""
            },
            screenshot : {
                disabled : true
            },
            definition : {
                disabled : false
            },
            diffChecker : {
                disabled : false
            },
            customTabsCallback : ""
        }
    },
    propertiesOptions: null,
    
    //undo & redo feature
    undoStack : new Array(),
    redoStack : new Array(),
    undoRedoMax : 50,
    
    json : null,
    data : {},
    paletteElements : {},
    availablePermission : {},
    permissionOptions: null,
    
    //Tracker
    isCtrlKeyPressed : false,
    isAltKeyPressed : false,
    saveChecker : 0,
    
    navCreateNewDialog : null,
    
    cachedAjaxCalls: {},
    
    builderItems : null,
    builderItemsLoading : [],
    
    builderShortcutActionHandlers : {},
    
    /*
     * Utility method to call a function by name
     */
    callback : function(name, args) {
        if (name !== "" && name !== undefined && name !== null) {
            var func = PropertyEditor.Util.getFunction(name);
            if (func !== null && func !== undefined) {
                return func.apply(null, args);
            }
        }
    },
    
    /*
     * Used in cbuilder/base.jsp to setup the configuration of the builder 
     */
    initConfig : function (config) {
        CustomBuilder.config = $.extend(true, {}, CustomBuilder.defaultConfig);
        CustomBuilder.config = $.extend(true, CustomBuilder.config, config);
    },
    
    /*
     * Used in cbuilder/base.jsp to setup the properties page of the builder 
     */
    initPropertiesOptions : function(options) {
        CustomBuilder.propertiesOptions = options;
        if (options !== undefined && options !== null && options !== "") {
            $("#properties-btn").show();
        } else {
            $("#properties-btn").hide();
        }
    },
    
    /* 
     * to show an overlay and error message when app is not found
     */
    renderAppNotExist: function(appId) {
        $("body").removeClass("initializing");
        appId = UI.escapeHTML(appId);
        
        //add a overlay with message, add it to quick nav so that it get removed together when navigate to other app
        $("#builder-quick-nav #builder-menu ul #appNotExist").remove();
        $("#builder-quick-nav #builder-menu ul").append('<li id="appNotExist"><div class="error">'+get_cbuilder_msg('abuilder.appNotExist', [appId])+'</div></li>');
        
        var url = CustomBuilder.contextPath+'/web/console/app/'+appId+'/0/builders';
        //change the browser URL, so the next import can redirect correctly.
        history.pushState({url: url}, "", url);
    },
    
    ajaxRenderBuilder: function(url) {
        HelpGuide.hide();
        
        $("#builder-quick-nav #builder-menu ul #appNotExist").hide();
        
        var rtl;
        if($('body').hasClass('rtl')){
           rtl=true;
        }
        //check url is same
        var temp = url;
        var hash = ""
        if (url.indexOf("#") > 0) {
            temp = url.substring(0, url.indexOf("#"));
            hash = url.substring(url.indexOf("#"));
        }
        if (window.location.pathname === temp) {
            if (window.location.hash !== hash) {
                window.location.hash = hash.replace("#", "");
            }
            return;
        }
        
        //check if there is unsave changes in current builder
        if (!CustomBuilder.isSaved()) {
            if (!confirm(get_cbuilder_msg('ubuilder.saveBeforeClose'))) {
                return;
            }
        }
        
        if (temp.indexOf("/builders") !== -1 || temp.indexOf("/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=appLicense") !== -1) {
            $("#builder_loader").css("color", "#6e9f4b");
            $("#builder_loader i.fa-stack-1x").attr("class", "far fa-edit fa-stack-1x");
        } else {
            var builderLi = $("#builder-menu a[href='"+url+"']").closest(".builder-icon");
            $("#builder_loader").css("color", $(builderLi).find('> span').css("color"));
            $("#builder_loader i.fa-stack-1x").attr("class", $(builderLi).find('> span > i').attr("class") + " fa-stack-1x");
        }
        
        $(".builder-view").remove();
        $(".boxy-content:visible").each(function(){
            var id = $(this).attr("id");
            JPopup.hide(id);
        });
        
        $("#quick-nav-bar").removeClass("active");
        if (typeof $('body').attr("builder-theme") !== 'undefined' && $('body').attr("builder-theme") !== false){
            $("#loadingMessage").text("");
        }
        $("body").addClass("initializing");
        
        var headers = new Headers();
        headers.append(ConnectionManager.tokenName, ConnectionManager.tokenValue);
        headers.append("_ajax-rendering", "true");
        
        var args = {
            method : "GET",
            headers: headers,
            redirect : "follow"
        };
        
        PresenceUtil.message("leave");
        var redirect = false;
        
        fetch(url, args)
        .then(function (response) {
            if (response.url.indexOf("/web/login") !== -1) {
                document.location.href = url;
                redirect = true;
                return false;
            } else if (response.url.indexOf("/web/console/home") !== -1) {
                var appId = "";
                if (url.indexOf("/web/console/app/") !== -1) {
                    appId = url.substring(url.indexOf("/web/console/app/") + 17);
                    appId = appId.substring(0, appId.indexOf("/"));
                }
                //app not exist
                CustomBuilder.renderAppNotExist(appId);
                redirect = true;
                return false;
            } else {
                history.pushState({url: response.url+hash}, "", response.url+hash); //handled redirected URL
                return response.text();
            }
        })
        .then(function (data) {
            if (redirect) {
                return;
            }
            
            $("#design-btn").trigger("click");
    
            CustomBuilder.updatePresenceIndicator();
            
            data = eval("[" + data.trim() + "]")[0];
            
            //to standardize formatting
            var jsonData = JSON.decode(data.builderDefJson);
            $("#cbuilder-json, #cbuilder-json-original, #cbuilder-json-current").val(JSON.encode(jsonData));
            
            $("head title").text(data.title);
            $("#builderElementName .title").html(data.name);
            
            $("#builder_loader").css("color", data.builderColor);
            $("#builder_loader i.fa-stack-1x").attr("class", data.builderIcon + " fa-stack-1x");
            $("#save-btn").removeClass("unsaved");
            
            if (CustomBuilder.builderType === data.builderType && CustomBuilder.systemTheme === data.systemTheme && CustomBuilder.builderType !== "app") {
                CustomBuilder.id = data.id;
                CustomBuilder.appId = data.appId;
                CustomBuilder.appVersion = data.appVersion;
                CustomBuilder.appPath = data.appPath;
                CustomBuilder.appPublished = data.appPublished;
                CustomBuilder.saveUrl = data.saveUrl;
                CustomBuilder.previewUrl = data.previewUrl;
                CustomBuilder.undoStack = new Array();
                CustomBuilder.redoStack = new Array();
                CustomBuilder.saveChecker = 0;
                CustomBuilder.systemTheme = data.systemTheme;
            
                $("body").removeClass("initializing");
                if ($("body").hasClass("default-builder")) {
                    CustomBuilder.Builder.selectedEl = null;
                    CustomBuilder.Builder.highlightEl = null;
                }
                CustomBuilder.initConfig(eval("[" + data.builderConfig + "]")[0]);
                CustomBuilder.loadJson($("#cbuilder-json").val());
                CustomBuilder.intBuilderMenu();
            } else {
                CustomBuilder.unbindBuilderShortcutActionHandlers();
                
                CustomBuilder.callback(CustomBuilder.config.builder.callbacks["unloadBuilder"], []);
                
                CustomBuilder.removeVisibilityChangeEvent("paste");
                
                $("body").attr("class", "no-right-panel initializing max-property-editor");
                $("#builder_canvas").attr("class", "");
                
                CustomBuilder = $.extend(true, {}, _CustomBuilder); //reset everything
                
                CustomBuilder.id = data.id;
                CustomBuilder.appId = data.appId;
                CustomBuilder.appVersion = data.appVersion;
                CustomBuilder.appPath = data.appPath;
                CustomBuilder.appPublished = data.appPublished;
                CustomBuilder.saveUrl = data.saveUrl;
                CustomBuilder.previewUrl = data.previewUrl;
                CustomBuilder.undoStack = new Array();
                CustomBuilder.redoStack = new Array();
                CustomBuilder.saveChecker = 0;
                CustomBuilder.paletteElements = {};
                CustomBuilder.availablePermission = {};
                CustomBuilder.permissionOptions = null;
                CustomBuilder.builderType = data.builderType;
                CustomBuilder.builderLabel = data.builderLabel;
                CustomBuilder.builderColor = data.builderColor;
                CustomBuilder.builderIcon = data.builderIcon;
                CustomBuilder.systemTheme = data.systemTheme;
                
                //reset builder 
                $("#builderIcon").css("background-color", data.builderColor);
                $("#builderIcon i").attr("class", "fa-2x " + data.builderIcon);
                $("#builderTitle span").text(data.builderLabel);
                $("#builderElementName").css("color", data.builderColor);
                $("#builder_canvas").html(data.builderCanvas);
                $("#left-panel .drag-elements-sidepane .components-list").html("");
                $("#elements-tabs").hide().html('<li class="nav-item component-tab"><a class="nav-link active" id="components-tab" data-toggle="tab" href="#components" role="tab" aria-controls="components" aria-selected="true" title="' + get_cbuilder_msg('cbuilder.elements') + '"/>"><div><small>' + get_cbuilder_msg('cbuilder.elements') + '</small></div></a></li>');
                $("#elements-tabs").next().find(" > :not(#components)").remove();
                $("#top-panel .responsive-buttons").hide();
                $("#builderToolbar .copypaste").hide();
                $("#style-properties-tab-link").hide();
                $("#style-properties-tab-link a").html('<i class="las la-palette"></i> <span>'+get_cbuilder_msg("cbuilder.styles") + '</span>');
                
                $("#left-panel .drag-elements-sidepane").off("mousedown touchstart mouseup touchend");
                
                if ($("body").hasClass("default-builder")) {
                    CustomBuilder.Builder.reset();
                }
                
                //load css and js
                $("style[data-cbuilder-style], link[data-cbuilder-style],script[data-cbuilder-script]").remove();
                $("head").append('<script data-cbuilder-script type="text/javascript" src="'+CustomBuilder.contextPath+'/web/console/i18n/cbuilder?type='+CustomBuilder.builderType+'&build='+CustomBuilder.buildNumber+'"></script>');
                if (data.builderCSS !== "") {
                    $("head").append(data.builderCSS);
                }
                if (data.builderJS !== "") {
                    $("head").append(data.builderJS);
                }
            }
            if(rtl === true) {
                $('body').addClass("rtl");
            }
            //update admin bar
            var acBtn = $("#adminBarButtons .adminBarButton").eq(0);
            $(acBtn).attr("href", CustomBuilder.contextPath + '/web/console/app'+ CustomBuilder.appPath +'/builders');
            $(acBtn).attr("onclick", "return AdminBar.openAppComposer('" + CustomBuilder.contextPath + '/web/console/app'+ CustomBuilder.appPath + "/builders');");
        })
        .catch(function (error) {
            console.log(error);
            document.location.href = url;
        });
    },
    
    /*
     * Used in cbuilder/base.jsp to initialize the builder
     */
    initBuilder: function (callback) {
        if (!CustomBuilder.isAjaxReady) {
            window.onpopstate = function(event) {
                if (event.state) {
                    var url = event.state.url;
                    CustomBuilder.ajaxRenderBuilder(url);
                }
            };
            window.onbeforeunload = function() {
                if(!CustomBuilder.isSaved()){
                    return get_cbuilder_msg("ubuilder.saveBeforeClose");
                }
            };
            
            CustomBuilder.isAjaxReady = true;
        }
        
        CustomBuilder.advancedToolsOptions = {
            contextPath : CustomBuilder.contextPath,
            appId : CustomBuilder.appId,
            appVersion : CustomBuilder.appVersion,
            id : CustomBuilder.id,
            builder : (CustomBuilder.builderType !== "form" && CustomBuilder.builderType !== "userview" && CustomBuilder.builderType !== "datalist")?"custom":CustomBuilder.builderType
        };
        
        if (!CustomBuilder.supportTreeViewer()) {
            $(".advanced-tools #treeviewer-btn").hide();
        } else {
            $(".advanced-tools #treeviewer-btn").show();
        }
        if (!CustomBuilder.supportXray()) {
            $(".advanced-tools #xray-btn").hide();
        } else {
            $(".advanced-tools #xray-btn").show();
        }
        if (!CustomBuilder.supportI18n()) {
            $(".advanced-tools #i18n-btn").hide();
        } else {
            $(".advanced-tools #i18n-btn").show();
        }
        if (!CustomBuilder.supportUsage()) {
            $(".advanced-tools #usages-btn").hide();
        } else {
            $(".advanced-tools #usages-btn").show();
        }
        if (!CustomBuilder.supportPermission()) {
            $(".advanced-tools #permission-btn").hide();
        } else {
            $(".advanced-tools #permission-btn").show();
        }
        if (!CustomBuilder.supportScreenshot()) {
            $(".advanced-tools #screenshot-btn").hide();
        } else {
            $(".advanced-tools #screenshot-btn").show();
        }
        if (!CustomBuilder.supportDiffChecker()) {
            $(".advanced-tools #diff-checker-btn").hide();
        } else {
            $(".advanced-tools #diff-checker-btn").show();
        }
        if (!CustomBuilder.supportDefinition()) {
            $(".advanced-tools #json-def-btn").hide();
        } else {
            $(".advanced-tools #json-def-btn").show();
        }
        
        if (CustomBuilder.previewUrl !== "") {
            $("#preview-btn").show();
        } else {
            $("#preview-btn").hide();
        }
        
        $("body").addClass("property-editor-right-panel");
        
        //use for old builder implementation like api builder & report builder
        if (CustomBuilder.config.builder.options['rightPropertyPanel'] !== true) { 
            $("body").addClass("property-editor-migrated");
        } else {
            $("body").removeClass("property-editor-migrated");
        }
        
        if (CustomBuilder.getBuilderSetting("autoApplyChanges") === true) {
            $("#toggleAutoApplyChange").addClass("toggle-enabled").removeClass("toggle-disabled");
            $("#toggleAutoApplyChange").attr("title", get_cbuilder_msg("cbuilder.disableAutoApplyChanges"));
        } else {
            $("#toggleAutoApplyChange").addClass("toggle-disabled").removeClass("toggle-enabled");
            $("#toggleAutoApplyChange").attr("title", get_cbuilder_msg("cbuilder.enableAutoApplyChanges"));
        }
        
        if (CustomBuilder.getBuilderSetting("expandProps") === true) {
            $("#expand-all-props-btn").hide();
            $("#collapse-all-props-btn").show();
        } else {
            $("#expand-all-props-btn").show();
            $("#collapse-all-props-btn").hide();
        }
        
        if (CustomBuilder.getBuilderSetting("right-panel-mode") === "window" 
                || (CustomBuilder.getBuilderSetting("right-panel-mode") === undefined && CustomBuilder.config.builder.options['rightPropertyPanel'] !== true)) {
            $("body").addClass("right-panel-mode-window");
        } else {
            $("body").removeClass("right-panel-mode-window");
        }
        CustomBuilder.adjustPropertyPanelSize();
        
//        var builderMode = $.localStorage.getItem("builderMode");
//        if (builderMode === undefined || builderMode === null || builderMode === "" || !(builderMode === "mode-basic" || builderMode === "mode-advanced")) {
//            builderMode = "mode-basic";
//        }
//        $("body").removeClass("mode-basic mode-advanced").addClass(builderMode);
//        if ($("#adminBar").find("#builderModeOption").length === 0) {
//            $("#adminBar").append('<div id="builderModeOption"><div><a><i class="las la-star"></i><span>'+get_cbuilder_msg('cbuilder.mode')+' : </span><span class="mode-basic">'+get_cbuilder_msg('cbuilder.mode.basic')+'</span><span class="mode-advanced">'+get_cbuilder_msg('cbuilder.mode.advanced')+'</span></a></div></div>');
//            $("#builderModeOption span[class]").on("click", function(){
//                var builderMode = $(this).attr("class");
//                $.localStorage.setItem("builderMode", builderMode);
//                $("body").removeClass("mode-basic mode-advanced").addClass(builderMode);
//                $(".initChosen").trigger("chosen:updated");
//            });
//        }

        $(".components-list").off("click", " ol > li > i.la-star");
        $(".components-list").on("click", " ol > li > i.la-star", function() {
            CustomBuilder.togglePaletteFav($(this).parent());
        });
        CustomBuilder.updatePaletteFav();
        
        var builderCallback = function(){
            var jsonData = JSON.decode($("#cbuilder-json").val());
            $("#cbuilder-json, #cbuilder-json-original, #cbuilder-json-current").val(JSON.encode(jsonData));
            
            if (callback) {
                callback();
            }
            
            if (CustomBuilder.config.advanced_tools.permission.permission_plugin !== undefined && CustomBuilder.config.advanced_tools.permission.permission_plugin !== "") {
                CustomBuilder.initPermissionList(CustomBuilder.config.advanced_tools.permission.permission_plugin);
            }
            
            CustomBuilder.initBuilderActions();
            
            CustomBuilder.customAdvancedToolTabs();
            
            $(document).uitooltip({
                position: { my: "left top+5", at: "left bottom", collision: "flipfit" },
                open: function (event, ui) {
                    $(".ui-tooltip").each(function(){
                       if (!($(this).is($(ui.tooltip)))) {
                           $(this).remove(); //remove the other tooltips that fail to hide itself.
                       } 
                    });
                    if ($(event.originalEvent.target).is("iframe")) {
                        $(ui.tooltip).hide();
                        return false;
                    }
                    var el = $(event.originalEvent.target).closest('[title]');
                    var position = el.attr('tooltip-position');
                    if (position === "right") {
                        var offset = el.offset();
                        $(ui.tooltip).css("left", (offset.left + el.width() + 5) + "px");
                        $(ui.tooltip).css("top", (offset.top + 5) + "px");
                    }
                    
                    if (CustomBuilder.tooltipTimeout !== undefined && CustomBuilder.tooltipTimeout !== null) {
                        clearTimeout(CustomBuilder.tooltipTimeout);
                    }
                    CustomBuilder.tooltipTimeout = setTimeout(function() {
                        $(".ui-tooltip").remove();
                        CustomBuilder.tooltipTimeout = null;
                    }, 2000); //force tooltip to remove after 2s
                },
                close: function (event, ui) {
                    $(".ui-helper-hidden-accessible").remove();
                    if (CustomBuilder.tooltipTimeout !== undefined && CustomBuilder.tooltipTimeout !== null) {
                        clearTimeout(CustomBuilder.tooltipTimeout);
                    }
                } 
            });
            
            CustomBuilder.builderFavIcon();
            CustomBuilder.updateBuilderBasedOnSettings();
            
            $("body").removeClass("initializing");
            CustomBuilder.intBuilderMenu();
                 
            HelpGuide.clear();
            HelpGuide.key = "help.web.console.app.builder."+CustomBuilder.builderType;
            HelpGuide.base = CustomBuilder.contextPath;
            HelpGuide.attachTo = "#help-guide-container";
            HelpGuide.show(); 
        };
        
        CustomBuilder.callback(CustomBuilder.config.builder.callbacks["initBuilder"], [builderCallback]);
    },
    
    
    initBuilderActions : function(container) {
        if (container === undefined) {
            container = "body";
        }
        
        jQuery.hotkeys.options.filterInputAcceptingElements = false;
        jQuery.hotkeys.options.filterTextInputs = false;
        
        $(container).find("[data-cbuilder-action]").each(function () {
            var on = "click";
            var target = $(this);
            if (this.dataset.cbuilderOn)
                on = this.dataset.cbuilderOn;

            var action = CustomBuilder[this.dataset.cbuilderAction];
            if (CustomBuilder.config.builder.callbacks[this.dataset.cbuilderAction] !== undefined && CustomBuilder.config.builder.callbacks[this.dataset.cbuilderAction] !== "") {
                var func = PropertyEditor.Util.getFunction(CustomBuilder.config.builder.callbacks[this.dataset.cbuilderAction]);
                if (func !== null && func !== undefined) {
                    action = func;
                }
            }

            var buttonAction = function(event) {
                if ($("#quick-nav-bar").hasClass("active")) {
                   $("#closeQuickNav").trigger("click");
                }
                
                var isShortcut = event.type === "keydown";
                if ($(target).is(":visible") && !$(target).hasClass("disabled")) {
                    var result = action.call(this, event);
                    if (result) {
                        return result;
                    }
                } else if (isShortcut) {
                    return; //let default shortcut key handler to proceed
                }
                return false;
            };
            $(this).off(on);
            $(this).on(on, buttonAction);
            if (this.dataset.cbuilderShortcut)
            {
                if (CustomBuilder.builderShortcutActionHandlers[this.dataset.cbuilderShortcut] !== undefined) {
                    $(document).unbind('keydown.shortcut', CustomBuilder.builderShortcutActionHandlers[this.dataset.cbuilderShortcut]);
                    if (window.FrameDocument) {
                        $(window.FrameDocument).unbind('keydown.shortcut', CustomBuilder.builderShortcutActionHandlers[this.dataset.cbuilderShortcut]);
                    }
                    delete CustomBuilder.builderShortcutActionHandlers[this.dataset.cbuilderShortcut];
                }
                
                CustomBuilder.builderShortcutActionHandlers[this.dataset.cbuilderShortcut] = buttonAction;
                $(document).on('keydown.shortcut', null, this.dataset.cbuilderShortcut, buttonAction);
                if (window.FrameDocument) {
                    $(window.FrameDocument).on('keydown.shortcut', null, this.dataset.cbuilderShortcut, buttonAction);
                }
            }
        });
    },
    
    unbindBuilderShortcutActionHandlers : function() {
        for (var i in CustomBuilder.builderShortcutActionHandlers) {
            if (CustomBuilder.builderShortcutActionHandlers.hasOwnProperty(i)) {
                $(document).unbind('keydown.shortcut', CustomBuilder.builderShortcutActionHandlers[i]);
                if (window.FrameDocument) {
                    $(window.FrameDocument).unbind('keydown.shortcut', CustomBuilder.builderShortcutActionHandlers[i]);
                }
            }
        }
        
        CustomBuilder.builderShortcutActionHandlers = {};
    },
    
    /*
     * Change the builder fav icon to the builder color
     */
    builderFavIcon : function() {
        if(!(navigator.userAgent.indexOf('MSIE')!==-1 || navigator.appVersion.indexOf('Trident/') > -1)){
            setTimeout(function(){
                var faviconSize = 32;
                var canvas = document.createElement('canvas');
                canvas.width = faviconSize;
                canvas.height = faviconSize;

                var context = canvas.getContext('2d');
                context.fillStyle = CustomBuilder.builderColor;
                context.fillRect(0, 0, faviconSize, faviconSize);

                var img = new Image();
                img.onload = function() {
                    context.drawImage(img, 4, 4, 24, 24);
                    $("#favicon").attr("href",canvas.toDataURL('image/png'));
                };
                img.src = CustomBuilder.contextPath + "/builder/favicon.svg";
            }, 1);
        }
    },
    
    /*
     * Based on cache, setting up the advanced tools during builder initializing
     */
    updateBuilderBasedOnSettings : function() {
        var builderSetting = null;
        var builderSettingJson = $.localStorage.getItem(CustomBuilder.builderType+"-settings");
        if (builderSettingJson !== null && builderSettingJson !== undefined) {
            builderSetting = JSON.decode(builderSettingJson);
        } else {
            builderSetting = {
                advanceTools : false
            };
            $.localStorage.setItem(CustomBuilder.builderType+"-settings", JSON.encode(builderSetting));
        }
        
        if (builderSetting.advanceTools !== undefined && builderSetting.advanceTools === true) {
            $("body").addClass("advanced-tools-supported");
        } else {
            $("body").removeClass("advanced-tools-supported");
        }
    },
    
    /*
     * Update builder setting in cache
     */
    setBuilderSetting : function(key, value) {
        var builderSetting = null;
        var builderSettingJson = $.localStorage.getItem(CustomBuilder.builderType+"-settings");
        if (builderSettingJson !== null && builderSettingJson !== undefined) {
            builderSetting = JSON.decode(builderSettingJson);
        } else {
            builderSetting = {
                rightPanel : true,
                advanceTools : false
            };
        }
        builderSetting[key] = value;
        $.localStorage.setItem(CustomBuilder.builderType+"-settings", JSON.encode(builderSetting));
    },
    
    /*
     * Get builder setting in cache
     */
    getBuilderSetting : function(key) {
        var builderSetting = null;
        var builderSettingJson = $.localStorage.getItem(CustomBuilder.builderType+"-settings");
        if (builderSettingJson !== null && builderSettingJson !== undefined) {
            builderSetting = JSON.decode(builderSettingJson);
        } else {
            builderSetting = {
                rightPanel : true,
                advanceTools : false,
                autoApplyChanges : true
            };
            $.localStorage.setItem(CustomBuilder.builderType+"-settings", JSON.encode(builderSetting));
        }
        return builderSetting[key];
    },
    
    showPopUpBuilderProperties : function() {
        
    },
    
    /*
     * Retrieve the properties value for Properties page
     */
    getBuilderProperties : function() {
        if (CustomBuilder.config.builder.callbacks["getBuilderProperties"] !== "") {
            return CustomBuilder.callback(CustomBuilder.config.builder.callbacks["getBuilderProperties"], []);
        } else {
            return CustomBuilder.data.properties;
        }
    },
    
    /*
     * Retrieve the builder item name
     */
    getBuilderItemName : function() {
        if (CustomBuilder.config.builder.callbacks["getBuilderItemName"] !== "") {
            return CustomBuilder.callback(CustomBuilder.config.builder.callbacks["getBuilderItemName"], []);
        } else {
            var props = CustomBuilder.getBuilderProperties();
            if (props) {
                return props['name'];
            } else {
                return null;
            }
        }
    },
    
    /*
     * Save the properties value in Properties page
     */
    saveBuilderProperties : function(container, properties) {
        if (CustomBuilder.config.builder.callbacks["saveBuilderProperties"] !== "") {
            CustomBuilder.callback(CustomBuilder.config.builder.callbacks["saveBuilderProperties"], [container, properties]);
        } else {
            var builderProperties = CustomBuilder.getBuilderProperties();
            builderProperties = $.extend(builderProperties, properties);
            CustomBuilder.update();
        }
    },
    
    /*
     * Used to create additional palette tabs
     */
    initPaletteElmentTabs : function(defaultTab, tabs) {
        $("#elements-tabs").show();
        $("#components-tab").attr("title", defaultTab.label);
        $("#components-tab small").text(defaultTab.label);
        if (defaultTab.image !== undefined && defaultTab.image !== "") {
            $("#components-tab").prepend('<img src="'+defaultTab.image+'" />');
        }
        
        if (tabs !== undefined && tabs.length > 0) {
            for (var i in tabs) {
                var li = $('<li class="nav-item component-tab"><a class="nav-link" id="'+tabs[i].name+'-tab" data-toggle="tab" href="#'+tabs[i].name+'" role="tab" aria-controls="'+tabs[i].name+'" aria-selected="false" title="'+tabs[i].label+'"><div><small>'+tabs[i].label+'</small></div></a></li>');
                if (tabs[i].image !== undefined && tabs[i].image !== "") {
                    $(li).find("a").prepend('<img src="'+tabs[i].image+'" />');
                }
                $("#elements-tabs").append(li);
                
                var tabPane = $('<div class="tab-pane fade" id="'+tabs[i].name+'" role="tabpanel" aria-labelledby="'+tabs[i].name+'-tab"> \
                        <div class="search"> \
                            <input class="form-control form-control-sm component-search" placeholder="'+get_cbuilder_msg("cbuilder.searchPalette")+'" type="text" data-cbuilder-action="tabSearch" data-cbuilder-on="keyup"> \
                            <button class="clear-backspace"  data-cbuilder-action="clearTabSearch"> \
                                <i class="la la-close"></i> \
                            </button> \
                        </div> \
                        <div class="drag-elements-sidepane sidepane"> \
                            <div> \
                                <ul class="components-list clearfix" data-type="leftpanel"> \
                                </ul>\
                            </div> \
                        </div> \
                    </div>');
                
                $("#elements-tabs").next().append(tabPane);
            }
        }
    },
    
    /*
     * Add element to palette
     */
    initPaletteElement : function(categories, className, label, icon, propertyOptions, defaultPropertiesValues, render, css, metaData, tab){
        if (this.paletteElements[className] !== undefined) {
            return;
        }
        if (tab === undefined || tab === "") {
            tab = "components";
        }
        if ((typeof propertyOptions) === "string") {
            try {
                propertyOptions = eval(propertyOptions);
            } catch (err) {
                if (console.log) {
                    console.log("error retrieving properties options of " + label + " : " + err);
                }
                return;
            }
        }
        if ((typeof defaultPropertiesValues) === "string") {
            try {
                defaultPropertiesValues = eval("["+defaultPropertiesValues+"]")[0];
            } catch (err) {
                if (console.log) {
                    console.log("error retrieving default property values of " + label + " : " + err);
                }
                return;
            }
        }
        
        if (css === undefined || css === null) {
            css = "";
        }
        
        var licss = "";
        if (metaData !== undefined && metaData.list_css !== undefined) {
            licss = metaData.list_css;
        }
        
        if (metaData !== undefined && metaData.developer_mode !== undefined && metaData.developer_mode !== "") {
            var temp = metaData.developer_mode.split(";");
            for (var j in temp) {
                licss += " "+temp[j]+"-mode-only";
            }
        }
        
        this.paletteElements[className] = new Object();
        this.paletteElements[className]['className'] = className;
        this.paletteElements[className]['label'] = label;
        this.paletteElements[className]['propertyOptions'] = propertyOptions;
        this.paletteElements[className]['properties'] = defaultPropertiesValues;
        
        var iconObj = null;
        var iconStr = "";
        if (icon !== undefined && icon !== null && icon !== "") {
            try {   
                iconObj = $(icon);
                iconStr = icon;
            } catch (err) {
                iconObj =  $('<span class="image" style="background-image:url(\'' + CustomBuilder.contextPath + icon + '\');"></span>');
                iconStr = '<span class="image" style="background-image:url(\'' + CustomBuilder.contextPath + icon + '\');" ></span>';
            }
        } else {
            iconObj = $('<i class="fas fa-th-large"></i>');
            iconStr = '<i class="fas fa-th-large"></i>';
        }
        this.paletteElements[className]['icon'] = iconStr;
        
        if (metaData !== undefined && metaData !== null) {
            this.paletteElements[className] = $.extend(this.paletteElements[className], metaData);
        }

        if (render === undefined || render !== false) {
            var categoriesArr = categories.split(";");
            for (var i in categoriesArr) {
                var category = categoriesArr[i];
                var categoryId = CustomBuilder.createPaletteCategory(category, tab);
                var container = $('#'+ tab + '_comphead_' + categoryId + '_list');
                var eid = categoryId+"_"+className.replace(/\./g, "_");
                var li = $('<li class="'+licss+'"><div id="'+eid+'" element-class="'+className+'" class="builder-palette-element '+css+'"> <a>'+UI.escapeHTML(label)+'</a></div><i class="lar la-star"></i></li>');
                $(li).find('.builder-palette-element').prepend($(iconObj).clone());
                $(container).append(li);
            }
        }
        CustomBuilder.updatePaletteFav();
    },
    
    /*
     * Check a palette element is in fav list and flag it
     */
    updatePaletteFav : function() {
        if (CustomBuilder.updatePaletteFavTimeout !== undefined && CustomBuilder.updatePaletteFavTimeout !== null) {
            clearTimeout(CustomBuilder.updatePaletteFavTimeout);
        }
        
        CustomBuilder.updatePaletteFavTimeout = setTimeout(function() {
            var list = CustomBuilder.getBuilderSetting("paletteFavList");
            if (list !== undefined && list !== null) {
                for (var i in list) {
                    var div = $("li div#"+list[i]);
                    if ($(div).length > 0) {
                        $(div).parent().addClass("fav");
                    }
                }
            }
            CustomBuilder.updatePaletteFavTimeout = null;
        }, 300);    
    },
    
    /*
     * Toogle a palette element into fav list
     */
    togglePaletteFav : function(li) {
        var list = CustomBuilder.getBuilderSetting("paletteFavList");
        if (list === undefined || list === null) {
            list = [];
        }
        var id = $(li).find("[element-class]").attr("id");
        
        if ($(li).hasClass("fav")) {
            $(li).removeClass("fav");
            if ($.inArray(id, list) !== -1) {
                list.splice( $.inArray(id, list), 1);
            }
        } else {
            $(li).addClass("fav");
            if ($.inArray(id, list) === -1) {
                list.push(id);
            }
        }
        CustomBuilder.setBuilderSetting("paletteFavList", list);
    },
    
    /*
     * Add palette category to tab
     */
    createPaletteCategory : function(category, tab, cssClass) {
        if (tab === undefined || tab === "") {
            tab = "components";
        }
        
        if (cssClass === undefined || cssClass === null) {
            cssClass = "";
        }
        
        var categoryId = "default";
        if (category === undefined || category === null || category === "") {
            category = "&nbsp;";
        } else {
            categoryId = category.replace(/\s/g , "-");
            if (!/^[A-Za-z][-A-Za-z0-9_:.]*$/.test(categoryId)) {
                categoryId = "palette-" + CustomBuilder.hashCode(category);
            }
        }
        var list = $("#"+tab + " .components-list");
        if ($('#'+ tab + '_comphead_' + categoryId + '_list').length === 0) {
            list.append('<li class="header clearfix '+cssClass+'" data-section="' + tab + '-' + categoryId + '"  data-search=""><label class="header" for="' + tab + '_comphead_' + categoryId + '">' + category + '  <div class="la la-angle-down header-arrow"></div>\
                            </label><input class="header_check" type="checkbox" checked="true" id="' + tab + '_comphead_' + categoryId + '">  <ol id="' + tab + '_comphead_' + categoryId + '_list"></ol></li>');
        }
        return categoryId;
    },
    
    /*
     * Remove a category from palette
     */
    clearPaletteCategory : function(category, tab) {
        if (tab === undefined || tab === "") {
            tab = "components";
        }
        
        var categoryId = "default";
        if (category === undefined || category === null || category === "") {
            category = "&nbsp;";
        } else {
            categoryId = category.replace(/\s/g , "-");
            if (!/^[A-Za-z][-A-Za-z0-9_:.]*$/.test(categoryId)) {
                categoryId = "palette-" + CustomBuilder.hashCode(category);
            }
        }
        var list = $("#"+tab + " .components-list");
        var container = $('#'+ tab + '_comphead_' + categoryId + '_list');
        
        $(container).find("[element-class]").each(function() {
            var className = $(this).attr("element-class");
            delete CustomBuilder.paletteElements[className];
        });
        
        container.html("");
    },
    
    /*
     * Retrieve permission plugin available for the builder
     */
    initPermissionList : function(classname){
        $.getJSON(
            CustomBuilder.contextPath + '/web/property/json/getElements?classname=' + classname,
            function(returnedData){
                if (returnedData !== null && returnedData !== undefined) {
                    CustomBuilder.permissionOptions = returnedData;
                    for (e in returnedData) {
                        if (returnedData[e].value !== "") {
                            CustomBuilder.availablePermission[returnedData[e].value] = returnedData[e].label;
                        }
                    }
                }
            }
        );
    },
    
    /*
     * Load and render the JSON data to canvas
     */
    loadJson : function(json, addToUndo) {
        try {
            CustomBuilder.data = JSON.decode(json);
        } catch (e) {}
        
        //when addToUndo is set to true, the new json need to update and keep a record in undo
        if (addToUndo === true) {
            //make sure the builder id is not change
            var props = CustomBuilder.getBuilderProperties();
            if (props !== undefined && props !== null) {
                //if props not having id and it can be found in CustomBuilder.data.properties. Note: this is to handle UI builder
                if (props.id === undefined && CustomBuilder.data.properties !== undefined && CustomBuilder.data.properties.id !== undefined) {
                    props = CustomBuilder.data.properties;
                }
                //check if id is changed
                if (props.id !== CustomBuilder.id) {
                    props.id = CustomBuilder.id; //reset it
                }
            }
        
            CustomBuilder.update(addToUndo);
        } else {
            CustomBuilder.json = json;
        }
        
        //callback to render json
        CustomBuilder.callback(CustomBuilder.config.builder.callbacks["load"], [CustomBuilder.data]);
    },
    
    /*
     * Update JSON data base on CustomBuilder.data
     */
    update : function(addToUndo) {
        CustomBuilder.callback(CustomBuilder.config.builder.callbacks["beforeUpdate"], [CustomBuilder.data]);
        
        var json = JSON.encode(CustomBuilder.data);
        CustomBuilder.updateJson(json, addToUndo);
        CustomBuilder.updatePasteIcons();
        
        CustomBuilder.callback(CustomBuilder.config.builder.callbacks["afterUpdate"], [CustomBuilder.data]);
    },
    
    /*
     * Update JSON data
     */
    updateJson : function (json, addToUndo) {
        if (CustomBuilder.json !== null && addToUndo !== false && CustomBuilder.json !== json) {
            CustomBuilder.addToUndo();
        }
        
        CustomBuilder.json = json;
        CustomBuilder.adjustJson();
        
        //update save button
        $("#save-btn").removeClass("unsaved");
        if ($('body').attr("builder-theme") !== 'undefined' && $('body').attr("builder-theme") !== false) {
            $("#save-btn > span").text(get_cbuilder_msg('ubuilder.save'));
            $("#save-btn > i").removeClass("zmdi zmdi-check");
            $("#save-btn > i").addClass("las la-cloud-upload-alt");
        }
        if (!CustomBuilder.isSaved()) {
            $("#save-btn").addClass("unsaved");
        }
    },
    
    /*
     * Get the generated JSON
     */
    getJson : function () {
        return CustomBuilder.json;
    },
    
    /*
     * Save JSON 
     */
    save : function(){
        if (typeof $('body').attr("builder-theme") !== 'undefined' && $('body').attr("builder-theme") !== false) {
            $("body").addClass("initializing");
        }
        var proceedSave = true;
        
        //the tooltip can't hide itself after click save, manually delete it
        $(".ui-tooltip").remove();
        
        if (CustomBuilder.config.builder.callbacks["builderBeforeSave"] !== undefined &&
                CustomBuilder.config.builder.callbacks["builderBeforeSave"] !== "") {
            proceedSave = CustomBuilder.callback(CustomBuilder.config.builder.callbacks["builderBeforeSave"]);
        }
        
        if (proceedSave) {
            CustomBuilder.showMessage(get_cbuilder_msg('cbuilder.saving'));
            var self = CustomBuilder;
            var json = CustomBuilder.getJson();
            
            var jsonFile = new Blob([json], {type : 'text/plain'});
            var params = new FormData();
            params.append("jsonFile", jsonFile);
            
            if (CustomBuilder.config.builder.options["submitDiff"]) {
                //prepare diff file
                // Parse the original JSON strings into JavaScript objects
                const oldData = JSON.decode($('#cbuilder-json-original').val());

                // Get the difference patch
                let diff = jsondiffpatch.diff(oldData, CustomBuilder.data);
                
                if (diff === null || diff === undefined) {
                    diff = {};
                }

                // Convert the difference patch to a JSON string
                const diffString = JSON.stringify(diff);
                
                //submit it
                var diffFile = new Blob([diffString], {type : 'text/plain'});
                params.append("diffFile", diffFile);
            }
        
            $.ajax({ 
                type: "POST", 
                url: CustomBuilder.saveUrl,
                data: params,
                cache: false,
                processData: false,
                contentType: false,
                beforeSend: function (request) {
                   request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                },
                success:function(data) {
                    var d = JSON.decode(data);
                    if(d.success == true){
                        $("#save-btn").removeClass("unsaved");
                        CustomBuilder.savedJson = json;
                        $('#cbuilder-json-original').val(d.data);
                        CustomBuilder.updateSaveStatus("0");
                        CustomBuilder.showMessage(get_cbuilder_msg('ubuilder.saved'), "success");

                        if (d.properties !== undefined && d.properties !== null) {
                            CustomBuilder.config.builder.properties = $.extend(true, CustomBuilder.config.builder.properties, d.properties);
                        }

                        CustomBuilder.callback(CustomBuilder.config.builder.callbacks["builderSaved"]);
                    }else{
                        CustomBuilder.showMessage(get_cbuilder_msg('ubuilder.saveFailed') + ((d.error && d.error !== "")?(" : " + d.error):""), "danger");

                        CustomBuilder.callback(CustomBuilder.config.builder.callbacks["builderSaveFailed"]);
                    }

                    //check builder name change
                    var name = CustomBuilder.getBuilderItemName();
                    if ((name !== null && $("#builderElementName .title span.item_name").text() !== name) || (name === null && CustomBuilder.builderType === "process")) {
                        if (name !== null) {
                            $("#builderElementName .title span.item_name").text(name);

                            $("head title").text(CustomBuilder.builderLabel + " : " + name);
                        }

                        //reload nav
                        CustomBuilder.reloadBuilderMenu();
                    }

                    setTimeout(function(){
                        $("#save-btn").removeAttr("disabled");
                        if (typeof $('body').attr("builder-theme") !== 'undefined' && $('body').attr("builder-theme") !== false) {
                            $("#save-btn > span").text(get_cbuilder_msg('cbuilder.saved'));
                            $("#save-btn > i").removeClass("las la-cloud-upload-alt");
                            $("#save-btn > i").addClass("zmdi zmdi-check");
                            $("body").removeClass("initializing");
                            $("#loadingMessage").text("");
                        }
                    }, 3000);
                }
            });
        } else {
            setTimeout(function(){
                $("#save-btn").removeAttr("disabled");
                if (typeof $('body').attr("builder-theme") !== 'undefined' && $('body').attr("builder-theme") !== false) {
                    $("body").removeClass("initializing");
                    $("#loadingMessage").text("");
                }
            }, 1000);
        }
    },

    /*
     * Preview generated JSON result
     */
    preview : function() {
        $('#cbuilder-json').val(CustomBuilder.getJson());
        $('#cbuilder-preview').attr("action", CustomBuilder.previewUrl);
        $('#cbuilder-preview').submit();
        return false;
    },
    
    /*
     * Used by advanced tool definition tab to update JSON
     */
    updateFromJson: function() {
        var json = $('#cbuilder-json').val();
        if (CustomBuilder.getJson() !== json) {
            CustomBuilder.loadJson(json, true); //need to save a copy in undo
        }
        return false;
    },
    
    /*
     * Undo the changes from stack
     */
    undo : function(){
        if(CustomBuilder.undoStack.length > 0){
            //if redo stack is full, delete first
            if(CustomBuilder.redoStack.length >= CustomBuilder.undoRedoMax){
                CustomBuilder.redoStack.splice(0,1);
            }

            //save current json data to redo stack
            CustomBuilder.redoStack.push(CustomBuilder.getJson());

            CustomBuilder.loadJson(CustomBuilder.undoStack.pop(), false);
            CustomBuilder.adjustJson();
            //enable redo button if it is disabled previously
            if(CustomBuilder.redoStack.length === 1){
                $('#redo-btn').removeClass('disabled');
            }

            //if undo stack is empty, disabled undo button
            if(CustomBuilder.undoStack.length === 0){
                $('#undo-btn').addClass('disabled');
            }

            CustomBuilder.updateSaveStatus("-");
        }
    },

    /*
     * Redo the changes from stack
     */
    redo : function(){
        if(CustomBuilder.redoStack.length > 0){
            //if undo stack is full, delete first
            if(CustomBuilder.undoStack.length >= CustomBuilder.undoRedoMax){
                CustomBuilder.undoStack.splice(0,1);
            }

            //save current json data to undo stack
            CustomBuilder.undoStack.push(CustomBuilder.getJson());

            CustomBuilder.loadJson(CustomBuilder.redoStack.pop(), false);
            CustomBuilder.adjustJson();
            //enable undo button if it is disabled previously
            if(CustomBuilder.undoStack.length === 1){
                $('#undo-btn').removeClass('disabled');
            }

            //if redo stack is empty, disabled redo button
            if(CustomBuilder.redoStack.length === 0){
                $('#redo-btn').addClass('disabled');
            }

            CustomBuilder.updateSaveStatus("+");
        }
    },
    
    /*
     * Add changes JSON to stack
     */
    addToUndo : function(json){
        //if undo stack is full, delete first
        if(CustomBuilder.undoStack.length >= CustomBuilder.undoRedoMax){
            CustomBuilder.undoStack.splice(0,1);
        }
        
        if (json === null || json === undefined) {
            json = CustomBuilder.getJson();
        }

        //save current json data to undo stack
        CustomBuilder.undoStack.push(json);

        //enable undo button if it is disabled previously
        if(CustomBuilder.undoStack.length === 1){
            $('#undo-btn').removeClass('disabled');
        }

        CustomBuilder.updateSaveStatus("+");
    },
    
    /*
     * Update the JSON for preview and advanced tools definition tab, then trigger 
     * a change event
     */
    adjustJson: function() {
        // update JSON
        $('#cbuilder-json').val(CustomBuilder.getJson()).trigger("change");
    },
    
    /*
     * Track the save status
     */
    updateSaveStatus : function(mode){
        if(mode === "+"){
            CustomBuilder.saveChecker++;
        }else if(mode === "-"){
            CustomBuilder.saveChecker--;
        }else if(mode === "0"){
            CustomBuilder.saveChecker = 0;
        }
    },
    
    /*
     * Show notifcation message
     */
    showMessage: function(message, type, center) {
        if (message && message !== "") {
            if (typeof $('body').attr("builder-theme") !== 'undefined' && $('body').attr("builder-theme") !== false) {
                $("#loadingMessage").text(message);
            }
            var id = "toast-" + (new Date()).getTime();
            var delay = 3000;
            if (type === undefined) {
                type = "secondary";
                delay = 1500;
            }
            var toast = $('<div id="'+id+'" role="alert" aria-live="assertive" aria-atomic="true" class="toast alert-dismissible toast-'+type+'" data-autohide="true">\
                '+message+'\
                <button type="button" class="close" data-dismiss="toast" aria-label="'+get_cbuilder_msg("cbuilder.close")+'">\
                    <span aria-hidden="true">&times;</span>\
                </button>\
              </div>');
            
            $("#builder-message").removeClass('center');
            $("#builder-message").append(toast);
            if (center) {
                $("#builder-message").addClass('center');
            }
            $('#'+id).toast({delay : delay});
            $('#'+id).toast("show");
            $('#'+id).on('hidden.bs.toast', function () {
                $('#'+id).remove();
            });
        }
    },
    
    /*
     * Remove copied element and clear clipboard
     */
    clearCopiedElement : function() {
        var data = CustomBuilder.getCopiedElement();
        if (data) {
            $.localStorage.removeItem("customBuilder_"+CustomBuilder.builderType+".copyTime");
            $.localStorage.removeItem("customBuilder_"+CustomBuilder.builderType+".copy");
            $("#paste-element-btn").addClass("disabled");
        }
    },
    
    /*
     * Retrieve copied element in cache
     */
    getCopiedElement : function() {
        var time = $.localStorage.getItem("customBuilder_"+CustomBuilder.builderType+".copyTime");
        //10 mins
        if (time !== undefined && time !== null && ((new Date()) - (new Date(time))) > 3000000) {
            $.localStorage.removeItem("customBuilder_"+CustomBuilder.builderType+".copyTime");
            $.localStorage.removeItem("customBuilder_"+CustomBuilder.builderType+".copy");
            return null;
        }
        var copied = $.localStorage.getItem("customBuilder_"+CustomBuilder.builderType+".copy");
        if (copied !== undefined && copied !== null) {
            return JSON.decode(copied);
        }
        return null;
    },
    
    /*
     * Copy an element
     */
    copy : function(element, type) {
        var copy = new Object();
        copy['type'] = type;
        copy['object'] = element;
        
        $.localStorage.setItem("customBuilder_"+CustomBuilder.builderType+".copy", JSON.encode(copy));
        $.localStorage.setItem("customBuilder_"+CustomBuilder.builderType+".copyTime", new Date());
        CustomBuilder.copyTextToClipboard(" ", false); //to clear the clipboard
        $.localStorage.removeItem("customBuilder.copiedText");
        
        CustomBuilder.showMessage(get_cbuilder_msg('ubuilder.copied'), "info");
    },
    
    /*
     * Update paste icon based on copied element 
     * Not used in DX8, it is for backward compatible
     */
    updatePasteIcon : function(type) {
        $(".element-paste").addClass("disabled");
        $(".element-paste."+type).removeClass("disabled");
    },
    
    /*
     * Update paste icon based on copied element 
     * Not used in DX8, it is for backward compatible
     */
    updatePasteIcons : function() {
        var type = "dummyclass";
        var copied = CustomBuilder.getCopiedElement();
        if (copied !== null) {
            type = copied['type'];
        }
        CustomBuilder.updatePasteIcon(type);
    },
    
    /*
     * Copy text to clipboard, option to clear the element clipboard.
     */
    copyTextToClipboard : function(text, clearElementClipboard) {
        var $temp = $("<input style='height:1px;opacity:0'>");
        $("body").append($temp);
        $temp.val(text).select();
        document.execCommand("copy");
        $temp.remove(); 
        
        $.localStorage.setItem("customBuilder.copiedText", text);
        
        if (clearElementClipboard) {
            CustomBuilder.clearCopiedElement();
        }
    },
    
    /*
     * Retrieve copied text in cache
     */
    getCopiedText : function() {
        return $.localStorage.getItem("customBuilder.copiedText");
    },
    
    /*
     * Check the diff before save and also use for advanced tool check diff tab
     */
    showDiff : function (callback, output) {
        var jsonUrl = "";
        if (CustomBuilder.config.builder.options["getDefinitionUrl"] !== "") {
            jsonUrl = CustomBuilder.config.builder.options["getDefinitionUrl"];
        } else {
            jsonUrl = CustomBuilder.contextPath + '/web/json/console/app/' + CustomBuilder.appId + '/' + CustomBuilder.appVersion + '/cbuilder/'+CustomBuilder.builderType+'/json/' + CustomBuilder.data.properties.id;
        }
        
        var thisObject = CustomBuilder;
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
                $('#cbuilder-json-current').val(currentString);
                var original = JSON.decode($('#cbuilder-json-original').val());
                var latest = JSON.decode($('#cbuilder-json').val());
                merged = DiffMerge.merge(original, current, latest, output);
            },
            error: function() {
                currentSaved = $('#cbuilder-json-current').val();
                merged = $('#cbuilder-json').val();
            },
            complete: function() {
                if (callback) {
                    callback.call(thisObject, currentSaved, merged);
                }    
            }
        });
    },
    
    /*
     * Merge the diff between local and remote
     */
    merge: function (callback) {
        var deferreds = [];
        var wait = $.Deferred();
        deferreds.push(wait);
        
        if (CustomBuilder.config.builder.callbacks["builderBeforeMerge"] !== undefined &&
                CustomBuilder.config.builder.callbacks["builderBeforeMerge"] !== "") {
            CustomBuilder.callback(CustomBuilder.config.builder.callbacks["builderBeforeMerge"], [deferreds]);
        }
        
        wait.resolve();
        
        $.when.apply($, deferreds).then(function() {
            // get current remote definition
            CustomBuilder.showMessage(get_cbuilder_msg('ubuilder.merging'));
            var thisObject = CustomBuilder;

            CustomBuilder.showDiff(function (currentSaved, merged) {
                if (currentSaved !== undefined && currentSaved !== "") {
                    $('#cbuilder-json-original').val(currentSaved);
                }
                if (merged !== undefined && merged !== "") {
                    $('#cbuilder-json').val(merged);
                }
                CustomBuilder.updateFromJson();

                if (callback) {
                    callback.call(thisObject, merged);
                }
            });
        });
    },
    
    /*
     * Merge remote change and save
     */
    mergeAndSave: function(event) {
        if ($("body").hasClass("properties-builder-view")) {
            var editor = $("#propertiesView .builder-view-body").data("editor");
            if (editor !== undefined && editor.isChange()) {
                if (editor.options.orgSaveCallback === undefined || editor.options.orgSaveCallback === null) {
                    editor.options.orgSaveCallback = editor.options.saveCallback;
                    editor.options.saveCallback = function(container, properties) {
                        editor.options.orgSaveCallback(container, properties);
                        $("#save-btn").attr("disabled", "disabled");
                        CustomBuilder.merge(CustomBuilder.save);
                    };
                }
                editor.save();
                editor.options.saveCallback = editor.options.orgSaveCallback;
                editor.options.orgSaveCallback = null;
            } else {
                $("#save-btn").attr("disabled", "disabled");
                CustomBuilder.merge(CustomBuilder.save);
            }
        } else if ($("body").hasClass("property-editor-right-panel") && !$("body").hasClass("no-right-panel")) {
            CustomBuilder.checkChangeBeforeCloseElementProperties(function(){
                $("#save-btn").attr("disabled", "disabled");
                $("body").addClass("no-right-panel");
                CustomBuilder.merge(CustomBuilder.save);
            });
        } else {
            $("#save-btn").attr("disabled", "disabled");
            CustomBuilder.merge(CustomBuilder.save);
        }
        
        if (event) {
            //to stop browser save dialog
            event.preventDefault();
            return false;
        }
    }, 
    
    /*
     * Builder support tree viewer in advanced tool based on config
     */
    supportTreeViewer: function() {
        return CustomBuilder.config.builder.options['rightPropertyPanel'] === true && !CustomBuilder.config.advanced_tools.tree_viewer.disabled;
    },
    
    /*
     * Builder support xray viewer in advanced tool based on config
     */
    supportXray: function() {
        return !CustomBuilder.config.advanced_tools.xray.disabled;
    },
    
    /*
     * Builder support i18n editor in advanced tool based on config
     */
    supportI18n: function() {
        return !CustomBuilder.config.advanced_tools.i18n.disabled;
    },
    
    /*
     * Builder support check usage in advanced tool based on config
     */
    supportUsage: function() {
        return !CustomBuilder.config.advanced_tools.usage.disabled;
    },
    
    /*
     * Builder support permission editor in advanced tool based on config
     */
    supportPermission: function() {
        return !CustomBuilder.config.advanced_tools.permission.disabled;
    },
    
    /*
     * Builder support screenshot in advanced tool based on config
     */
    supportScreenshot: function() {
        return !CustomBuilder.config.advanced_tools.screenshot.disabled;
    },
    
    /*
     * Builder support diff checker in advanced tool based on config
     */
    supportDiffChecker: function() {
        return !CustomBuilder.config.advanced_tools.diffChecker.disabled;
    },
    
    /*
     * Builder support definition in advanced tool based on config
     */
    supportDefinition: function() {
        return !CustomBuilder.config.advanced_tools.definition.disabled;
    },
    
    /*
     * Used to initialize additional advanced tool tabs in toolbar
     */
    customAdvancedToolTabs: function() {
        CustomBuilder.callback(CustomBuilder.config.advanced_tools["customTabsCallback"]);
    },
    
    /*
     * Edit an element properties in right panel or popup dialog
     */
    editProperties: function(elementClass, elementProperty, elementObj, element) {
        if ($("body").hasClass("property-editor-migrated")) {
            if (!$("body").hasClass("no-right-panel")) {
                CustomBuilder.checkChangeBeforeCloseElementProperties(function(hasChange) {
                    $("body").addClass("no-right-panel");
                    CustomBuilder.editProperties(elementClass, elementProperty, elementObj, element);
                });   
                return;
            } else {
                $("body").removeClass("no-right-panel");
                $("#right-panel .property-editor-container").remove();
            }
        }
        
        $(".element-properties .nav-tabs .nav-link").removeClass("has-properties-errors");
        
        var paletteElement = CustomBuilder.paletteElements[elementClass];
        
        if (paletteElement === undefined) {
            return;
        }
        var elementOptions = paletteElement.propertyOptions;
        if (paletteElement.builderTemplate !== undefined && paletteElement.builderTemplate.customPropertyOptions !== undefined) {
            elementOptions = paletteElement.builderTemplate.customPropertyOptions(elementOptions, element, elementObj, paletteElement);
        }
        
        // show property dialog
        var options = {
            appPath: "/" + CustomBuilder.appId + "/" + CustomBuilder.appVersion,
            contextPath: CustomBuilder.contextPath,
            propertiesDefinition : elementOptions,
            propertyValues : elementProperty,
            showCancelButton:true,
            changeCheckIgnoreUndefined: true,
            cancelCallback: function() {
                CustomBuilder.callback(CustomBuilder.config.builder.callbacks["cancelEditProperties"], [elementObj, element]);
            },
            saveCallback: function(container, properties) {
                elementProperty = $.extend(elementProperty, properties);
                
                CustomBuilder.callback(CustomBuilder.config.builder.callbacks["saveEditProperties"], [container, elementProperty, elementObj, element]);
                CustomBuilder.update();
            }
        };
        
        if ($("body").hasClass("property-editor-right-panel")) {
            CustomBuilder.clearPropertySearch();
            $("#right-panel #element-properties-tab").find(".property-editor-container").remove();
            
            options['editorPanelMode'] = true;
            options['showCancelButton'] = false;
            options['closeAfterSaved'] = false;
            options['saveCallback'] = function(container, properties) {
                var d = $(container).find(".property-editor-container").data("deferred");
                var currentElement = element;
                
                if (currentElement && $("body").hasClass("default-builder") && !$(currentElement).is(CustomBuilder.Builder.selectedEl)) {
                    currentElement = CustomBuilder.Builder.selectedEl;
                }
                
                d.resolve({
                    container :container, 
                    prevProperties : elementProperty, 
                    properties : properties, 
                    elementObj : elementObj,
                    element : currentElement
                });
            };
            options['validationFailedCallback'] = function(container, errors) {
                var d = $(container).find(".property-editor-container").data("deferred");
                var currentElement = element;
                
                if (currentElement && $("body").hasClass("default-builder") && !$(currentElement).is(CustomBuilder.Builder.selectedEl)) {
                    currentElement = CustomBuilder.Builder.selectedEl;
                }
                
                d.resolve({
                    container :container,  
                    prevProperties : elementProperty, 
                    errors : errors, 
                    elementObj : elementObj,
                    element : currentElement
                });
            };
            
            $("#right-panel #element-properties-tab").propertyEditor(options);
            if ($("body").hasClass("max-property-editor") || $("body").hasClass("right-panel-mode-window")) {
                CustomBuilder.adjustPropertyPanelSize();
            }
            
            if (CustomBuilder.getBuilderSetting("expandProps") === true) {
                $("#right-panel .property-editor-container > .property-editor-pages > .property-editor-page ").removeClass("collapsed");
            }
            
            $("#element-properties-tab-link").show();
        } else {
            // show popup dialog
            if (!PropertyEditor.Popup.hasDialog(CustomBuilder.builderType+"-property-editor")) {
                PropertyEditor.Popup.createDialog(CustomBuilder.builderType+"-property-editor");
            }
            PropertyEditor.Popup.showDialog(CustomBuilder.builderType+"-property-editor", options);
        }
    },
    
    /*
     * Save element properties/styles changes when apply button (tick icon) in right panel is pressed
     */
    applyElementProperties : function(callback) {
        var button = $(this);
        button.attr("disabled", "");
        $(".element-properties .nav-tabs .nav-link").removeClass("has-properties-errors");
        
        var deferreds = [];
                        
        $(".element-properties .tab-pane > .property-editor-container").each(function() {
            var d = $.Deferred();
            deferreds.push(d);
            $(this).data("deferred", d);
            $(this).find(".page-button-save").last().trigger("click");
        });
        
        $.when.apply($, deferreds).then(function() {
            var container = $(arguments[0].container);
            var prevProperties = arguments[0].prevProperties;
            var element = $(arguments[0].element);
            var elementObj = arguments[0].elementObj;
            var hasError = false;
            
            for (var i in arguments) {
                if (arguments[i].errors !== undefined) {
                    hasError = true;
                    var id = $(arguments[i].container).attr("id");
                    $(".element-properties .nav-tabs .nav-link[href='#"+id+"']").addClass("has-properties-errors");
                }
            }
            
            if (!hasError) {
                var elementProperty = prevProperties;
                var oldPropertiesJson = JSON.encode(elementProperty);
                
                for (var i in arguments) {
                    $.extend(elementProperty, arguments[i].properties);
                }
                
                //clean unuse styling 
                for (var property in elementProperty) {
                    if (elementProperty.hasOwnProperty(property)) {
                        if ((property.indexOf('attr-') === 0 || property.indexOf('css-') === 0 || property.indexOf('style-') === 0
                            || property.indexOf('-attr-') > 0 || property.indexOf('-css-') > 0 || property.indexOf('-style-') > 0) 
                            && elementProperty[property] === "") {
                            delete elementProperty[property];
                        }
                    }
                }
                
                var newPropertiesJson = JSON.encode(elementProperty);
                if (oldPropertiesJson !== newPropertiesJson) {
                    if ($(element).is('[data-cbuilder-style-id]')) {
                        var style = $(element).next('[data-cbuilder-style]');
                        $(element).append(style);
                        $(element).removeAttr("data-cbuilder-style-id");
                    }
                    
                    if (CustomBuilder.isViewerWithPE === undefined || CustomBuilder.isViewerWithPE === false) {
                        CustomBuilder.callback(CustomBuilder.config.builder.callbacks["saveEditProperties"], [container, elementProperty, elementObj, element]);
                    }
                    
                    if ($("body").hasClass("default-builder") || CustomBuilder.isViewerWithPE) {
                        var updateDeferreds = [];
                        var dummy = $.Deferred();
                        updateDeferreds.push(dummy);
                        CustomBuilder.Builder.updateElement(elementObj, element, updateDeferreds);
                        dummy.resolve();
                        $.when.apply($, updateDeferreds).then(function() {
                            button.removeAttr("disabled");
                            if (callback && $.isFunction(callback)) {
                                callback();
                            }
                        });
                    } else {
                        button.removeAttr("disabled");
                        if (callback && $.isFunction(callback)) {
                            callback();
                        }
                    }
                    CustomBuilder.update();
                    CustomBuilder.showMessage(get_cbuilder_msg("cbuilder.newChangesApplied"), "success");
                } else {
                    if (callback && $.isFunction(callback)) {
                        callback();
                    }
                }
                
                //close the properties editor if it is window mode
                if ($("body").hasClass("right-panel-mode-window")) {
                    $("body").addClass("no-right-panel");
                    $("#right-panel .property-editor-container").remove();
                }
                
            } else {
                CustomBuilder.showMessage(get_cbuilder_msg("cbuilder.pleaseCorrectErrors"), "danger");
            }
            button.removeAttr("disabled");
        });
    },
    
    /*
     * Check change before close the properties panel
     */
    checkChangeBeforeCloseElementProperties : function(callback) {
        var hasChange = false;
        var isContinue = false;
        
        if (!$("body").hasClass("no-right-panel")) {
            $(".element-properties .property-editor-container").each(function() {
                var editor = $(this).parent().data("editor");
                if (editor !== undefined && !editor.saved && editor.isChange()) {
                    hasChange = true;
                }
            });
        } else {
            isContinue = true;
        }
        
        if (hasChange) {
            if (CustomBuilder.getBuilderSetting("autoApplyChanges")) {
                CustomBuilder.applyElementProperties(function(){
                    if (callback){
                        callback(hasChange);
                    }
                });
            } else {
                isContinue = confirm(get_cbuilder_msg("cbuilder.discardChanges"));
            }
        } else {
            isContinue = true;
        }
        
        if (isContinue && callback) {
            callback(hasChange);
        }
    },
    
    /*
     * Utility method to generate an uuid
     */
    uuid : function(){
        return 'xxxxxxxxxxxx4xxxxxxxxxxxxxxxxxxx'.replace(/[xy]/g, function(c) {  //xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        }).toUpperCase();
    },
    
    /*
     * Generate an unique hash code for a string
     */
    hashCode : function(s) {
        var h = 0, l = s.length, i = 0;
        if ( l > 0 )
          while (i < l)
            h = (h << 5) - h + s.charCodeAt(i++) | 0;
        return h;
    },
    
    /*
     * Show the advanced tools
     */
    enableEnhancedTools : function() {
        $("body").addClass("advanced-tools-supported");
        CustomBuilder.setBuilderSetting("advanceTools", true);
    },
    
    /*
     * Hide the advanced tools
     */
    disableEnhancedTools : function() {
        $("body").removeClass("advanced-tools-supported");
        CustomBuilder.setBuilderSetting("advanceTools", false);
        $("#design-btn").trigger("click");
    },
    
    /*
     * Show/hide builder left panel
     */
    toogleLeftPanel : function() {
        if ($(this).find("i").hasClass("la-angle-left")) {
            $("body").addClass("left-panel-closed");
            $(this).find("i").removeClass("la-angle-left").addClass("la-angle-right");
        } else {
            $("body").removeClass("left-panel-closed");
            $(this).find("i").removeClass("la-angle-right").addClass("la-angle-left");
        }
    },
    
    /*
     * Show/hide builder right panel
     */
    toogleRightPanel : function() {
        if ($(this).find("i").hasClass("la-angle-right")) {
            $("body").addClass("right-panel-closed");
            $(this).find("i").removeClass("la-angle-right").addClass("la-angle-left");
        } else {
            $("body").removeClass("right-panel-closed");
            $(this).find("i").removeClass("la-angle-left").addClass("la-angle-right");
        }
    },
    
    /*
     * Resize builder right panel
     */
    resizeRightPanel : function(event) {
        var button = $(this);
        var panel = $("#right-panel");
        $(panel).addClass("resizing");
        $("body").addClass("right-panel-resizing");
        
        var stopResize = function() {
            $("body").off("mousemove.rpresize touchmove.rpresize");
            $("body").off("mouseup.rpresize touchend.rpresize");
            
            if ($("body").hasClass("default-builder")) {
                CustomBuilder.Builder.frameHtml.off("mousemove.rpresize touchmove.rpresize");
                CustomBuilder.Builder.frameHtml.off("mouseup.rpresize touchend.rpresize");
            }
            $(panel).removeClass("resizing");
            $("body").removeClass("right-panel-resizing");
        };
        
        var resize = function(e) {
            var x = e.clientX;
            if (e.originalEvent) {
                x = e.originalEvent.clientX;
            }
            if (e.type === "touchmove") {
                x = e.touches[0].clientX;
                if (e.touches[0].originalEvent) {
                    x= e.touches[0].originalEvent.clientX;
                }
            }
            if (!$(e.currentTarget).is("#cbuilder")) {
                x += $(CustomBuilder.Builder.iframe).offset().left;
            }
            if (x < 60) {
                x = 60;
            }
            var newWidth = $(panel).offset().left - x + $(panel).outerWidth();
            $(panel).css("width", newWidth + "px");
            CustomBuilder.setBuilderSetting("right-panel-width", newWidth);
            
            CustomBuilder.adjustPropertyPanelSize();
        };
        
        if ($("body").hasClass("default-builder")) {
            CustomBuilder.Builder.frameHtml.off("mousemove.rpresize touchmove.rpresize");
            CustomBuilder.Builder.frameHtml.off("mouseup.rpresize touchend.rpresize");
            
            CustomBuilder.Builder.frameHtml.on("mousemove.rpresize touchmove.rpresize", resize);
            CustomBuilder.Builder.frameHtml.on("mouseup.rpresize touchend.rpresize", stopResize);
        }
        
        $("body").off("mousemove.rpresize touchmove.rpresize");
        $("body").off("mouseup.rpresize touchend.rpresize");
        
        $("body").on("mousemove.rpresize touchmove.rpresize", resize);
        $("body").on("mouseup.rpresize touchend.rpresize", stopResize);
    },
    
    /*
     * Resize builder right panel window
     */
    resizeRightPanelWindow : function(event) {
        var button = $(this);
        var panel = $("#right-panel");
        $(panel).addClass("resizing");
        $("body").addClass("right-panel-resizing");
        
        var stopResize = function() {
            $("body").off("mousemove.rpwresize touchmove.rpwresize");
            $("body").off("mouseup.rpwresize touchend.rpwresize");
            
            if ($("body").hasClass("default-builder")) {
                CustomBuilder.Builder.frameHtml.off("mousemove.rpwresize touchmove.rpwresize");
                CustomBuilder.Builder.frameHtml.off("mouseup.rpwresize touchend.rpwresize");
            }
            $(panel).removeClass("resizing");
            $("body").removeClass("right-panel-resizing");
        };
        
        var resize = function(e) {
            var x = e.clientX;
            if (e.originalEvent) {
                x = e.originalEvent.clientX;
            }
            if (e.type === "touchmove") {
                x = e.touches[0].clientX;
                if (e.touches[0].originalEvent) {
                    x= e.touches[0].originalEvent.clientX;
                }
            }
            if (!$(e.currentTarget).is("#cbuilder")) {
                x += $(CustomBuilder.Builder.iframe).offset().left;
            }
            
            var y = e.clientY;
            if (e.originalEvent) {
                y = e.originalEvent.clientY;
            }
            if (e.type === "touchmove") {
                y = e.touches[0].clientY;
                if (e.touches[0].originalEvent) {
                    y= e.touches[0].originalEvent.clientY;
                }
            }
            if (!$(e.currentTarget).is("#cbuilder")) {
                y += $(CustomBuilder.Builder.iframe).offset().top;
            }
            
            var top = $(panel).offset().top;
            var left = $(panel).offset().left;
            
            var newWidth = x - left + 5;
            var newHeight = y - top + 5;
            
            if (newWidth < 150) {
                newWidth = 150;
            }
            if (newHeight < 300) {
                newHeight = 300;
            }
            
            CustomBuilder.setBuilderSetting("right-panel-window-width", newWidth);
            CustomBuilder.setBuilderSetting("right-panel-window-height", newHeight);
            
            CustomBuilder.adjustPropertyPanelSize();
        };
        
        if ($("body").hasClass("default-builder")) {
            CustomBuilder.Builder.frameHtml.off("mousemove.rpwresize touchmove.rpwresize");
            CustomBuilder.Builder.frameHtml.off("mouseup.rpwresize touchend.rpwresize");
            
            CustomBuilder.Builder.frameHtml.on("mousemove.rpwresize touchmove.rpwresize", resize);
            CustomBuilder.Builder.frameHtml.on("mouseup.rpwresize touchend.rpwresize", stopResize);
        }
        
        $("body").off("mousemove.rpwresize touchmove.rpwresize");
        $("body").off("mouseup.rpwresize touchend.rpwresize");
        
        $("body").on("mousemove.rpwresize touchmove.rpwresize", resize);
        $("body").on("mouseup.rpwresize touchend.rpwresize", stopResize);
    },
    
    /*
     * Move builder right panel window
     */
    moveRightPanelWindow : function(event) {
        var button = $(this);
        var panel = $("#right-panel");
        $(panel).addClass("resizing");
        $("body").addClass("right-panel-resizing");
        
        var stopMove = function() {
            $("body").off("mousemove.rpwmove touchmove.rpwmove");
            $("body").off("mouseup.rpwmove touchend.rpwmove");
            
            if ($("body").hasClass("default-builder")) {
                CustomBuilder.Builder.frameHtml.off("mousemove.rpwmove touchmove.rpwmove");
                CustomBuilder.Builder.frameHtml.off("mouseup.rpwmove touchend.rpwmove");
            }
            $(panel).removeClass("resizing");
            $("body").removeClass("right-panel-resizing");
        };
        
        var move = function(e) {
            var x = e.clientX;
            if (e.originalEvent) {
                x = e.originalEvent.clientX;
            }
            if (e.type === "touchmove") {
                x = e.touches[0].clientX;
                if (e.touches[0].originalEvent) {
                    x= e.touches[0].originalEvent.clientX;
                }
            }
            if (!$(e.currentTarget).is("#cbuilder")) {
                x += $(CustomBuilder.Builder.iframe).offset().left;
            }
            if (x < 60) {
                x = 60;
            }
            
            var y = e.clientY;
            if (e.originalEvent) {
                y = e.originalEvent.clientY;
            }
            if (e.type === "touchmove") {
                y = e.touches[0].clientY;
                if (e.touches[0].originalEvent) {
                    y= e.touches[0].originalEvent.clientY;
                }
            }
            if (!$(e.currentTarget).is("#cbuilder")) {
                y += $(CustomBuilder.Builder.iframe).offset().top;
            }
            if (y < 60) {
                y = 60;
            }
            
            var newTop = y - 20;
            var newLeft = x - 20;
            
            CustomBuilder.setBuilderSetting("right-panel-window-top", newTop);
            CustomBuilder.setBuilderSetting("right-panel-window-left", newLeft);
     
            CustomBuilder.adjustPropertyPanelSize();
        };
        
        if ($("body").hasClass("default-builder")) {
            CustomBuilder.Builder.frameHtml.off("mousemove.rpwmove touchmove.rpwmove");
            CustomBuilder.Builder.frameHtml.off("mouseup.rpwmove touchend.rpwmove");
            
            CustomBuilder.Builder.frameHtml.on("mousemove.rpwmove touchmove.rpwmove", move);
            CustomBuilder.Builder.frameHtml.on("mouseup.rpwmove touchend.rpwmove", stopMove);
        }
        
        $("body").off("mousemove.rpwmove touchmove.rpwmove");
        $("body").off("mouseup.rpwmove touchend.rpwmove");
        
        $("body").on("mousemove.rpwmove touchmove.rpwmove", move);
        $("body").on("mouseup.rpwmove touchend.rpwmove", stopMove);
    },
    
    /*
     * Animate the builder left panel to reopen it
     */
    animateLeftPanel : function() {
        $("body").removeClass("left-panel-closed");
        $("#left-panel #left-panel-toogle").find("i").removeClass("la-angle-right").addClass("la-angle-left");
            
        $("#left-panel").off('animationend webkitAnimationEnd oAnimationEnd');
        $("#left-panel").on('animationend webkitAnimationEnd oAnimationEnd', function(){
            setTimeout(function(){$("#left-panel").removeClass("switchingLeft");}, 5);
        });
        $("#left-panel").addClass("switchingLeft");
    },
    
    /*
     * Switch builder view based on toolbar icon
     */
    switchView : function() {
        var $this = $(this);
        var view = $this.data("cbuilder-view");
        
        if ($this.is(".active-view")) {
            view = "design";
        }
        
        var currentView = $("[data-cbuilder-view].active-view").data("cbuilder-view");
        if (CustomBuilder.config.builder.callbacks[currentView+"ViewBeforeClosed"] !== undefined) {
            CustomBuilder.callback(CustomBuilder.config.builder.callbacks[currentView+"ViewBeforeClosed"], [$("#"+currentView+"View.builder-view .builder-view-body")]);
        } else if (CustomBuilder[currentView+"ViewBeforeClosed"] !== undefined) {
            CustomBuilder[currentView+"ViewBeforeClosed"]($("#"+currentView+"View.builder-view .builder-view-body"));
        }
        $("body").removeClass(currentView+"-builder-view");
        $("body").removeClass("hide-tool");
        $("body").removeClass("view-control");
        $("[data-cbuilder-view]").removeClass("active-view active");
        $(".builder-view").hide();
        
        $("[data-cbuilder-view='"+view+"']").addClass("active-view");
        
        if (view !== "design") {
            var viewDiv = $("#"+view+"View.builder-view");
            if (viewDiv.length === 0) {
                viewDiv = $('<div id="'+view+'View" class="builder-view" style="display:none"><div class="builder-view-header"></div><div class="builder-view-body"></div></div>');
                $("body").append(viewDiv);
            }  
            if (CustomBuilder.config.builder.callbacks[view+"ViewInit"] !== undefined) {
                CustomBuilder.callback(CustomBuilder.config.builder.callbacks[view+"ViewInit"], [$(viewDiv).find('.builder-view-body')]);
            } else if (CustomBuilder[view+"ViewInit"] !== undefined) {
                CustomBuilder[view+"ViewInit"]($(viewDiv).find('.builder-view-body'));
            } else if ($this.attr("href") !== undefined) {
                if ($(viewDiv).find('.builder-view-body iframe').length === 0) {
                    $(viewDiv).find('.builder-view-body').html('<i class="dt-loading las la-spinner la-3x la-spin" style="opacity:0.3; position:absolute; z-index:2000;"></i>');
                    var iframe = document.createElement('iframe');
                    iframe.onload = function() {
                        $(viewDiv).find('.builder-view-body .dt-loading').remove();
                        $(iframe).css('opacity', "1");
                    }; 
                    iframe.src = $this.attr("href"); 
                    $(iframe).css('opacity', "0");
                    $(viewDiv).find('.builder-view-body').append(iframe);
                }
            }
    
            if ($this.data("hide-tool") !== undefined) {
                $("body").addClass("hide-tool");
            }

            if ($this.data("view-control") !== undefined) {
                $("body").addClass("view-control");
            }
            
            $("#"+view+"View.builder-view").show();
            $(viewDiv).find('.builder-view-body').trigger("builder-view-show");
            $("body").addClass(view+"-builder-view");
        }
    },
    
    /*
     * Show the builder properties view, called by switchView method
     */
    propertiesViewInit : function(view) {
        $(view).html("");
        
        var props = CustomBuilder.getBuilderProperties();
        
        var options = {
            appPath: CustomBuilder.appPath,
            contextPath: CustomBuilder.contextPath,
            propertiesDefinition : CustomBuilder.propertiesOptions,
            propertyValues : props,
            showCancelButton:false,
            closeAfterSaved : false,
            changeCheckIgnoreUndefined: true,
            autoSave: true,
            saveCallback: CustomBuilder.saveBuilderProperties
        };
        $("body").addClass("stop-scrolling");
        
        $(view).off("builder-view-show");
        $(view).on("builder-view-show", function(){
            $(view).propertyEditor(options);
        });
    },
    
    /*
     * Show the builder preview view, called by switchView method
     */
    previewViewInit : function(view) {
        $(view).html('<div id="preview-iframe-wrapper"><i class="dt-loading las la-spinner la-3x la-spin" style="opacity:0.3; position:absolute; z-index:2000;"></i><iframe id="preview-iframe" name="preview-iframe" style="opacity:0;" src="about:none"></iframe></div>');
        
        var iframe = $(view).find("#preview-iframe")[0];
        iframe.onload = function() {
            $(view).find('.dt-loading').remove();
            $(iframe).css('opacity', "1");
            
            iframe.contentWindow["UI"].base = CustomBuilder.contextPath;
            iframe.contentWindow["UI"].userview_app_id = CustomBuilder.appId;
        }; 
        
        var viewport = $(".responsive-buttons button.active").data("view");
	$(view).closest(".builder-view").addClass(viewport);
        
        $('#cbuilder-preview [name=OWASP_CSRFTOKEN]').val(ConnectionManager.tokenValue);
        $('#cbuilder-preview').attr("action", CustomBuilder.previewUrl);
        $('#cbuilder-preview').attr("target", "preview-iframe");
        $('#cbuilder-preview').submit();
        return false;
    },
    
    /*
     * Show the tree viewer on left panel, called by switchView method
     */
    treeViewerViewInit : function(view) {
        if ($("body").hasClass("default-builder")) {
            $(view).closest(".builder-view").addClass("treeRightPanel");
            $(view).closest(".builder-view").prependTo($("#left-panel")).css("top", "0px");
            CustomBuilder.animateLeftPanel();
            view.addClass("panel-section tree");
            view.html("");
            view.append('<div class="panel-header"><span class="text-secondary">'+get_advtool_msg("adv.tool.Tree.Viewer")+'</span></div><div class="tree-container scrollable tree"></div>');
            
            CustomBuilder.Builder.renderTreeMenu(view.find(".tree-container"));
            
            $(CustomBuilder.Builder.iframe).off("change.builder");
            $(CustomBuilder.Builder.iframe).on("change.builder", function() {
                if ($(view).is(":visible")) {
                    view.find(".tree-container").html("");
                    CustomBuilder.Builder.renderTreeMenu(view.find(".tree-container"));
                }
            });
        }
    },
    
    /*
     * Run before tree viewer view dismiss, called by switchView method
     */
    treeViewerViewBeforeClosed : function(view) {
        if ($("body").hasClass("default-builder")) {
            CustomBuilder.animateLeftPanel();
        }
    },
    
    /*
     * Show the xray view, called by switchView method
     */
    xrayViewInit : function(view) {
        CustomBuilder.treeViewerViewInit(view);
        if ($("body").hasClass("default-builder")) {
            CustomBuilder.Builder.renderNodeAdditional('Xray');
        }
    },
    
    /*
     * Run before xray view dismiss, called by switchView method
     */
    xrayViewBeforeClosed : function(view) {
        if ($("body").hasClass("default-builder")) {
            CustomBuilder.Builder.removeNodeAdditional();
            CustomBuilder.animateLeftPanel();
        }
    },
    
    /*
     * Show the permission editor view, called by switchView method
     */
    permissionViewInit : function(view) {
        CustomBuilder.isViewerWithPE = true;
        CustomBuilder.Builder.selectedElBeforePermission = CustomBuilder.Builder.selectedEl;
        
        $("body").addClass("no-right-panel");
        view.html("");
        $(view).prepend('<i class="dt-loading fas fa-5x fa-spinner fa-spin"></i>');
        PermissionManager.render($(view));
        $(view).find(".dt-loading").remove();

        $("#cbuilder-json").off("change.permissionViewInit");
        $("#cbuilder-json").on("change.permissionViewInit", function () {
            if (!$("body").hasClass("permission-builder-view")) {
                view.html("");
                $(view).prepend('<i class="dt-loading fas fa-5x fa-spinner fa-spin"></i>');
                PermissionManager.render($(view));
                $(view).find(".dt-loading").remove();
            }
        });
    },
    
    /*
     * Run before permission editor dismiss, called by switchView method
     */
    permissionViewBeforeClosed : function(view) {
        CustomBuilder.isViewerWithPE = false;
        CustomBuilder.Builder.selectedEl = null;
        
        if (CustomBuilder.Builder.selectedElBeforePermission !== null && CustomBuilder.Builder.selectedElBeforePermission !== undefined) {
            CustomBuilder.Builder.selectNode(CustomBuilder.Builder.selectedElBeforePermission);
        }
        
        $("#cbuilder-json").off("change.permissionViewInit");
    },
    
    /*
     * Show the find usage view, called by switchView method
     */
    findUsagesViewInit : function(view) {
        $(view).html("");
        Usages.render($(view), CustomBuilder.id, CustomBuilder.advancedToolsOptions.builder, CustomBuilder.advancedToolsOptions);
    },
    
    /*
     * Show the i18n editor view, called by switchView method
     */
    i18nViewInit : function(view) {
        if ($(view).find(".i18n_table").length === 0) {
            $(view).html("");
            $(view).prepend('<i class="dt-loading las la-spinner la-3x la-spin" style="opacity:0.3"></i>');
            
            var config = $.extend(true, CustomBuilder.config.advanced_tools.i18n.options, CustomBuilder.advancedToolsOptions);
            I18nEditor.init($(view), $("#cbuilder-info").find('textarea[name="json"]').val(), config);
            
            $(view).find(".dt-loading").remove();
            
            $("#cbuilder-info").find('textarea[name="json"]').off("change.i18n");
            $("#cbuilder-info").find('textarea[name="json"]').on("change.i18n", function() {
                $(view).html("");
            });
        }
        setTimeout(function(){
            I18nEditor.refresh($(view));
        }, 5);
    },
    
    /*
     * Show the diff checker view, called by switchView method
     */
    diffCheckerViewInit : function(view) {
        $(view).html('<div id="diffoutput"> </div>');
        
        CustomBuilder.merge(function(){
            var original = $('#cbuilder-json-original').val();
            var current = $('#cbuilder-json').val();
            
            original = difflib.stringAsLines(JSON.stringify(JSON.decode(original), null, 4));
            current = difflib.stringAsLines(JSON.stringify(JSON.decode(current), null, 4));
            
            var sm = new difflib.SequenceMatcher(original, current),
            opcodes = sm.get_opcodes();
            
            if (opcodes.length === 1 && opcodes[0][0] === "equal") {
                $(view).append('<p>'+get_advtool_msg('diff.checker.noChanges')+'</p>');
            } else {
                $("#diffoutput").append(diffview.buildView({
                    baseTextLines: original,
                    newTextLines: current,
                    opcodes: opcodes,
                    baseTextName: "",
                    newTextName: "",
                    contextSize: null,
                    viewType: "1"
                }));
                
                //generate indicator
                var height = $("#diffoutput table").outerHeight() + 75;
                $("#diffoutput").append('<div id="diff-indicator" style="visibility:hidden;" ><canvas id="diff-indicator-canvas" width="10" height="'+(height + 3)+'"></div>');
                var c = document.getElementById("diff-indicator-canvas");
                var ctx = c.getContext("2d");
                $("#diffoutput table tbody tr > td").each(function(index, td){
                    if ($(td).is(".replace, .delete, .insert")) {
                        var cssClass = $(td).attr("class");
                        var y = $(td).offset().top - 65;
                        
                        var color = "#ff3349";
                        if (cssClass === "insert") {
                            color = "#49f772";
                        } else if (cssClass === "replace") {
                            color = "#fdd735";
                        }
                        
                        ctx.beginPath();
                        ctx.rect(0, y, 10, $(td).outerHeight());
                        ctx.fillStyle = color;
                        ctx.fill();
                    }
                });
                var image = c.toDataURL("image/png");
                $("#diff-indicator canvas").remove();
                $("#diff-indicator").css({
                    "visibility" : "",
                    "background-image" : "url("+image+")",
                    "background-repeat" : "repeat-x",
                    "background-size" : "contain"
                });
            }
        });
    },
    
    /*
     * Show the JSON definition view, called by switchView method
     */
    jsonDefViewInit : function(view) {
        if ($(view).find($("#json_definition")).length === 0) {
            $(view).addClass("ace_fullpage");
            $(view).append($("#cbuilder-info").find("button").addClass("button").clone());
            $(view).find("button.button").wrap('<div class="sticky-buttons">');
            $(view).prepend('<pre id="json_definition" style="height:100%"></pre>');

            var editor = ace.edit("json_definition");
            editor.$blockScrolling = Infinity;
            if ($('body').attr('builder-theme') === "dark") {
                editor.setTheme("ace/theme/vibrant_ink");
            } else {
                editor.setTheme("ace/theme/textmate");
            }
            editor.getSession().setTabSize(4);
            editor.getSession().setMode("ace/mode/json");
            editor.setAutoScrollEditorIntoView(true);
            editor.resize();
            var textarea = $("#cbuilder-info").find('textarea[name="json"]').hide();
            $(textarea).on("change", function() {
                if (!CustomBuilder.editorSilentChange) {
                    CustomBuilder.editorSilentChange = true;
                    var jsonObj = JSON.decode($(this).val());
                    editor.getSession().setValue(JSON.stringify(jsonObj, null, 4));
                    editor.resize(true);
                    CustomBuilder.editorSilentChange = false;
                }
            });
            $(textarea).trigger("change");
            editor.getSession().on('change', function(){
                if (!CustomBuilder.editorSilentChange) {
                    CustomBuilder.editorSilentChange = true;
                    var value = editor.getSession().getValue();
                    if (value.length > 0) {
                        var jsonObj = JSON.decode(value);
                        textarea.val(JSON.encode(jsonObj)).trigger("change");
                    }
                    CustomBuilder.editorSilentChange = false;
                }
            });
            $(view).find("button.button").on("click", function() {
                CustomBuilder.editorIsChange = true;
                var text = $(this).text();
                $(this).text(get_advtool_msg('adv.tool.updated'));
                $(this).attr("disabled", true);
                setTimeout(function(){
                    $(view).find("button.button").text(text);
                    $(view).find("button.button").removeAttr("disabled");
                }, 1000);
            });
            $(view).data("editor", editor);
        } else {
            var editor = $(view).data("editor");
            CustomBuilder.editorIsChange = false;
            editor.resize(true);
        }
    },
    
    /*
     * Run before JSON definition view dismiss, called by switchView method
     */
    jsonDefViewBeforeClosed : function(view) {
        if (!CustomBuilder.editorIsChange) {
            CustomBuilder.editorSilentChange = true;
            $("#cbuilder-info").find('textarea[name="json"]').val(CustomBuilder.getJson());
            CustomBuilder.editorSilentChange = false;
        }
    },
    
    /*
     * Prepare and render the screeshot view, called by switchView method
     */
    screenshotViewInit: function(view) {
        $("body").addClass("no-left-panel");
        $(view).html('<div id="screenshotViewImage"></div><div class="sticky-buttons"></div>');
        
        if ($("body").hasClass("default-builder")) {
            $(CustomBuilder.Builder.iframe).off("change.builder", CustomBuilder.Builder.renderScreenshot);
            $(CustomBuilder.Builder.iframe).on("change.builder", CustomBuilder.Builder.renderScreenshot);

            CustomBuilder.Builder.renderScreenshot();
        }
    },
    
    /*
     * Reset the builder back to design view, called by switchView method
     */
    screenshotViewBeforeClosed: function(view) {
        $("body").removeClass("no-left-panel");
    },
    
    logViewerViewBeforeClosed: function(view) {
        view.html("");
    },
    
    /*
     * Custom implementation for app message editor view
     */
    appMessageViewInit : function(view) {
        if ($(view).find(".i18n_table").length === 0) {
            $(view).html("");
            $(view).prepend('<i class="dt-loading las la-spinner la-3x la-spin" style="opacity:0.3"></i>');
            
            var config = $.extend(true, {loadEnglish : true}, CustomBuilder.advancedToolsOptions);
            
            CustomBuilder.cachedAjax({
                type: "POST",
                url: CustomBuilder.contextPath + '/web/json/console/app' + CustomBuilder.appPath + '/message/keys',
                dataType : "json",
                beforeSend: function (request) {
                   request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                },
                success: function(response) {
                    var labels = [];
                    try {
                        for (var i in response.data) {
                            labels.push({key: response.data[i], label: response.data[i]});
                        }
                    } catch(err) {}
                    
                    I18nEditor.renderTable($(view), labels, config);
                    I18nEditor.refresh($(view));
                    
                    $(view).find(".i18n_table tbody").prepend('<tr class="even addnew"><td class="label"><a class="addNewKey btn btn-primary btn-sm"><i class="las la-plus"></i> '+get_cbuilder_msg('abuilder.addNewKey')+'</a></td><td class="lang1"></td><td class="lang2"></td></tr>');
                    $(view).find(".i18n_table .addNewKey").on("click", function(){
                        CustomBuilder.appMessageAddNewKey($(this));
                    });
                    
                    $(view).find(".dt-loading").remove();
                }
            });
        }
        setTimeout(function(){
            I18nEditor.refresh($(view));
        }, 5);
    },
    
    /*
     * Add new key to i18n table
     */
    appMessageAddNewKey : function(button) {
        $(button).hide();
        $(button).after('<div class="newKeyContainer"><label><strong>'+get_cbuilder_msg('abuilder.addNewKey')+'</strong></label> <textarea></textarea> <a class="addNewKeySubmit btn btn-primary btn-sm">'+get_cbuilder_msg("cbuilder.ok")+'</a> <a class="addNewKeyCancel btn btn-secondary btn-sm">'+get_cbuilder_msg("cbuilder.cancel")+'</a></div>');
        var container = $(button).next();
        $(container).find(".addNewKeySubmit").on("click", function(){
            var key = $(container).find("textarea").val();
            
            //check duplicate
            var keysInput = $(button).closest("tbody").find("tr:not(.addnew) td.label textarea");
            var found = false;
            $(keysInput).each(function(){
               var v = $(this).val();
               if (v === key) {
                   found = true;
               }
            });
            if (!found) {
                var cssClass = "odd";
                if ($(button).closest("tr").next().hasClass("odd")) {
                    cssClass = "even";
                }
                var newRow = $('<tr class="'+cssClass+'"><td class="label"><span>'+UI.escapeHTML(key)+'</span><textarea name="i18n_key_'+(keysInput.length +1)+'" style="display:none">'+key+'</textarea></td><td class="lang1"></td><td class="lang2"></td></tr>');
                var lang1 = $(button).closest(".i18n_table").find("select#lang1").val();
                var lang2 = $(button).closest(".i18n_table").find("select#lang2").val();
                if (lang1 !== "") {
                    var relkey = key + "_" + lang1;
                    newRow.find('td.lang1').html('<textarea></textarea>');
                    newRow.find('td.lang1 textarea').attr("rel", relkey.toLowerCase());
                }
                if (lang2 !== "") {
                    var relkey = key + "_" + lang2;
                    newRow.find('td.lang2').html('<textarea></textarea>');
                    newRow.find('td.lang2 textarea').attr("rel", relkey.toLowerCase());
                }
                
                $(button).closest("tr").after(newRow);
                $(button).closest("table").find("tr.norecord").remove();
                
                $(container).remove();
                $(button).show();
            } else {
                $(container).find("textarea").css("border-color", "red");
                $(container).find("textarea").before('<br><span style="color:red;">'+get_cbuilder_msg('abuilder.addNewKey.error')+'</span><br>');
            }
        });
        $(container).find(".addNewKeyCancel").on("click", function(){
            $(container).remove();
            $(button).show();
        });
    },
    
    /*
     * Search element in left panel when left panel search field keyup
     */
    tabSearch : function() {
        var searchText = this.value.toLowerCase();
	var tab = $(this).closest(".tab-pane");
	$(tab).find(".components-list li ol li").each(function () {
            var element = $(this);
            element.hide();
            if ($(element).find("a").text().toLowerCase().indexOf(searchText) > -1) { 
                element.show();
            }
	});
        if (this.value !== "") {
            $(this).next("button").show();
        } else {
            $(this).next("button").hide();
        }
    },
    
    /*
     * Clear the search on left panel when clear icon clicked 
     */
    clearTabSearch : function() {
        var tab = $(this).closest(".tab-pane");
        $(tab).find(".component-search").val("");
        $(tab).find(".components-list li ol li").show();
        $(this).hide();
    },
    
    /*
     * Toggle fullscreen mode
     */
    fullscreen : function() {
        if (document.documentElement.requestFullScreen) {

            if (document.FullScreenElement)
                document.exitFullScreen();
            else
                document.documentElement.requestFullScreen();
        //mozilla		
        } else if (document.documentElement.mozRequestFullScreen) {

            if (document.mozFullScreenElement)
                document.mozCancelFullScreen();
            else
                document.documentElement.mozRequestFullScreen();
        //webkit	  
        } else if (document.documentElement.webkitRequestFullScreen) {

            if (document.webkitFullscreenElement)
                document.webkitExitFullscreen();
            else
                document.documentElement.webkitRequestFullScreen();
        //ie	  
        } else if (document.documentElement.msRequestFullscreen) {

            if (document.msFullScreenElement)
                document.msExitFullscreen();
            else
                document.documentElement.msRequestFullscreen();
        }
    },
    
    /*
     * Cheange viewport based on viewport icon pressed
     */
    viewport : function (view) {
        if (typeof view !== "string") {
            view = $(view.target).closest("button").data("view");
        }
        $(".responsive-buttons button").removeClass("active");
        $(".responsive-buttons button#"+view+"-view").addClass("active");
	$("body, #builder_canvas, #previewView").removeClass("mobile tablet desktop noviewport").addClass(view);
        
        //for builder
        $("#element-highlight-box, #element-select-box").hide();
        $("body").addClass("no-right-panel");
        
        $("body").trigger($.Event("viewport.change", {"view": view}));
    },
    
    /*
     * adjust the right panel size
     */
    adjustPropertyPanelSize : function () {
        $("body").addClass("max-property-editor");
        
        var width = CustomBuilder.getBuilderSetting("right-panel-width");
        if ($("body").hasClass("right-panel-mode-window")) {
            width = CustomBuilder.getBuilderSetting("right-panel-window-width");
            if (isNaN(width)) { //if width is not defined, set it to 80% of window width
                width = $(window).width() * 0.8;
            }
            if (width < 150) {
                width = 150;
            } else if (width > ($(window).width() * 0.95)) {
                width = $(window).width() * 0.95;
            }
            
            //set top, left & height too
            var top = CustomBuilder.getBuilderSetting("right-panel-window-top");
            if (isNaN(top)) { //if top is not defined, set it to 10% of window height
                top = $(window).height() * 0.1;
            }
            
            var left = CustomBuilder.getBuilderSetting("right-panel-window-left");
            if (isNaN(left)) { //if top is not defined, set it to 10% of window height
                left = $(window).width() * 0.1;
            }
            
            var height = CustomBuilder.getBuilderSetting("right-panel-window-height");
            if (isNaN(height)) { //if top is not defined, set it to 80% of window height
                height = $(window).height() * 0.8;
            }
            if (height < ($(window).height() * 0.4)) {
                height = $(window).height() * 0.4;
            } else if (height > ($(window).height() * 0.90)) {
                height = $(window).height() * 0.90;
            }
            
            $("#right-panel").css("top", top+"px");
            $("#right-panel").css("left", left+"px");
            $("#right-panel").css("height", height+"px");
        } else {
            //remove top, left, height, width
            $("#right-panel").css("top", "");
            $("#right-panel").css("left", "");
            $("#right-panel").css("height", "");
            $("#right-panel").css("width", "");
        }
        
        if (!isNaN(width)) {
            var winWidth = $("body").width() - 60;
            if (width > winWidth) {
                width = winWidth;
            }
            $("#right-panel").css("width", width + 'px');
        } else {
            try {
                width = parseInt($("#right-panel").css("min-width").replace("px", ""));
            } catch (e){}
        }
        
        if (width > 680) {
            $("#right-panel, #right-panel .property-editor-container").addClass("wider");
        } else {
            $("#right-panel, #right-panel .property-editor-container").removeClass("wider");
        }
    },
    
    /*
     * Close the right panel
     */
    closePropertiesWindow : function() {
        CustomBuilder.checkChangeBeforeCloseElementProperties(function(){
            $("body").addClass("no-right-panel");
        });
    },
    
    /*
     * Show the right panel as window
     */
    maxPropertiesWindow : function() {
        $("body").addClass("right-panel-mode-window");
        CustomBuilder.setBuilderSetting("right-panel-mode", "window");
        
        CustomBuilder.adjustPropertyPanelSize();
    },
    
    /*
     * Restore the right panel from window
     */
    dockPropertiesWindow : function() {
        $("body").removeClass("right-panel-mode-window");
        CustomBuilder.setBuilderSetting("right-panel-mode", "");
        
        CustomBuilder.adjustPropertyPanelSize();
    },
    
    /*
     * Expand all sections in properties panel
     */
    expandAllProperties : function() {
        $("#right-panel .property-editor-container > .property-editor-pages > .property-editor-page ").removeClass("collapsed");
        $("#expand-all-props-btn").hide();
        $("#collapse-all-props-btn").show();
        CustomBuilder.setBuilderSetting("expandProps", true);
    },
    
    /*
     * Collapse all sections in properties panel
     */
    collapseAllProperties : function() {
        $("#right-panel .property-editor-container > .property-editor-pages > .property-editor-page ").addClass("collapsed");
        $("#expand-all-props-btn").show();
        $("#collapse-all-props-btn").hide();
        CustomBuilder.setBuilderSetting("expandProps", false);
    },
    
    /*
     * Search property on right panel when search field keyup event
     */
    propertySearch : function() {
        var searchText = this.value.toLowerCase();
	var tab = $(this).closest(".element-properties");
        $(tab).find(".property-page-show").each(function() {
            var page = $(this);
            if ($(page).find(".property-editor-page-title > span").text().toLowerCase().indexOf(searchText) > -1) { 
                $(page).find(".property-editor-property").removeClass("property-search-hide");
            } else {
                $(page).find(".property-editor-property").each(function () {
                    var element = $(this);
                    element.addClass("property-search-hide");
                    
                    var show = false;
                    if (CustomBuilder.isSearchMatch($(element).find(".property-label").text(), searchText)) { 
                       show = true;
                    }
                    if (!show) {
                        $(element).find("input[name], textarea[name], select[name], .ace_editor, .tinymce").each(function(){
                            if ($(this).is('.ace_editor')) {
                                var id = $(this).attr('id');
                                var codeeditor = ace.edit(id);
                                var value = codeeditor.getValue();
                                if (CustomBuilder.isSearchMatch(value, searchText)) {
                                    show = true;
                                    return false;
                                }
                            } else if ($(this).is('.tinymce')) {
                                var value = tinymce.get($(this).attr('id')).getContent();
                                if (CustomBuilder.isSearchMatch(value, searchText)) {
                                    show = true;
                                    return false;
                                }
                            } else if ($(this).is('[type="checkbox"]') || $(this).is('[type="radio"]')) {
                                if ($(this).is(":checked")) {
                                    var label = ""; 
                                    if ($(this).parent().find("span").length > 0) {
                                        label = $(this).parent().find("span").text();
                                    }
                                    
                                    if (CustomBuilder.isSearchMatch($(this).val(), searchText)) {
                                        show = true;
                                        return false;
                                    } else if (CustomBuilder.isSearchMatch(label, searchText)) {
                                        show = true;
                                        return false;
                                    }
                                }
                            } else if ($(this).is('select')) {
                                var label = ""; 
                                if ($(this).find("option:selected").length > 0) {
                                    label = $(this).find("option:selected").text();
                                }
                                if (CustomBuilder.isSearchMatch(label, searchText)) {
                                    show = true;
                                    return false;
                                }
                            } else if (CustomBuilder.isSearchMatch($(this).val(), searchText)) {
                                show = true;
                                return false;
                            }
                        });
                    }
                    if (show) {
                        element.removeClass("property-search-hide");
                    }
                });
            }
            
            if ($(page).find(".property-editor-property:not(.property-search-hide)").length > 0) {
                $(page).removeClass("property-search-hide");
                if (searchText !== "") {
                    $(page).removeClass("collapsed");
                }
            } else {
                $(page).addClass("property-search-hide");
            }
        });
        if (this.value !== "") {
            $(this).next("button").show();
        } else {
            $(this).next("button").hide();
        }
    },
    
    isSearchMatch : function(str, searchText) {
        return str !== null && str !== undefined && str.toLowerCase().indexOf(searchText) > -1;
    },
    
    /*
     * Clear the search on right panel
     */
    clearPropertySearch : function() {
        var tab = $(this).closest(".element-properties");
        $(tab).find(".component-search").val("");
        $(tab).find(".property-search-hide").removeClass("property-search-hide");
        $(this).hide();
    },
    
    /*
     * Toggle the auto apply changes value
     */
    toogleAutoApplyChanges : function() {
        if (CustomBuilder.getBuilderSetting("autoApplyChanges") === true) {
            CustomBuilder.setBuilderSetting("autoApplyChanges", false);
            $("#toggleAutoApplyChange").addClass("toggle-disabled").removeClass("toggle-enabled");
            $("#toggleAutoApplyChange").attr("title", get_cbuilder_msg("cbuilder.enableAutoApplyChanges"));
        } else {
            CustomBuilder.setBuilderSetting("autoApplyChanges", true);
            $("#toggleAutoApplyChange").addClass("toggle-enabled").removeClass("toggle-disabled");
            $("#toggleAutoApplyChange").attr("title", get_cbuilder_msg("cbuilder.disableAutoApplyChanges"));
        }
    },
    
    /*
     * Utility method to get property for an object
     */
    getPropString : function(value) {
        return (value !== undefined && value !== null) ? value : "";
    },
    
    /*
     * Method used for toolbar to copy an element
     */
    copyElement : function(event) {
        if (event && ((/textarea|input|select/i.test(event.target.nodeName) && event.target.selectionStart !== event.target.selectionEnd) || 
                (window.getSelection && window.getSelection() !== undefined && window.getSelection() !== null && 
                window.getSelection().anchorNode !== undefined && window.getSelection().anchorNode !== null &&
                window.getSelection().anchorNode.nodeName === "#text" && window.getSelection().toString().length > 0))) {
            //clear element clipboard
            CustomBuilder.clearCopiedElement();
            return true; //to continue to the default handler to copy text
        }
        if (CustomBuilder.Builder.selectedEl !== null) {
            CustomBuilder.Builder.copyNode();
        }
    },
    
    /*
     * Method used for toolbar to paste an element
     */
    pasteElement : function(event) {
        if (event && /textarea|input|select/i.test(event.target.nodeName)) {
            if (CustomBuilder.getCopiedElement() === null) {
                return true; //to continue to the default handler to paste text
            } else {
                try {
                    navigator.clipboard.readText().then(clipText => {
                        if (clipText === undefined || clipText === null ||
                                clipText === "" || clipText.trim().length === 0 || 
                                clipText === CustomBuilder.getCopiedText()) {
                            CustomBuilder.Builder.pasteNode();
                        } else {
                            if ($(event.target).hasClass("ace_text-input") || $(event.target).hasClass("ace_editor")) {
                                var id = $(event.target).closest(".ace_editor").attr("id");
                                var codeeditor = ace.edit(id);
                                codeeditor.session.insert(codeeditor.getCursorPosition(), clipText);
                            } else {
                                var caret = PropertyAssistant.doGetCaretPosition(event.target);
                                var text = $(event.target).val();
                                var selectedText = (window.getSelection())?window.getSelection().toString():"";
                                if (selectedText.length > 0) { //remove the selected text
                                    text = [text.slice(0, caret), text.slice(selectedText.length)].join('');
                                }
                                var output = [text.slice(0, caret), clipText, text.slice(caret)].join('');
                                $(event.target).val(output);
                            }
                        }
                    });
                } catch (err) {
                    CustomBuilder.Builder.pasteNode();
                }
            }
        } else {
            CustomBuilder.Builder.pasteNode();
        }
    },
    
    /*
     * Retrieve permision plugin option for permission editor
     */
    getPermissionOptions: function(){
        return CustomBuilder.permissionOptions;
    },
    
    /*
     * Utility method to cache ajax call, this is for better performance when frquently undo/redo or update element
     */
    cachedAjax: function(ajaxObj) {
        var json = "";
        if (ajaxObj.data !== null && ajaxObj.data !== undefined) {
            json = JSON.encode(ajaxObj.data);
        }
        var key = (ajaxObj.type?ajaxObj.type:"") + "::" + ajaxObj.url + "::" + CustomBuilder.hashCode(json);
        
        if (CustomBuilder.cachedAjaxCalls[key] !== undefined) {
            //cache for 60sec
            if (((new Date().getTime()) - CustomBuilder.cachedAjaxCalls[key].time) < 60000) {
                if (ajaxObj.success) {
                    ajaxObj.success(CustomBuilder.cachedAjaxCalls[key].data);
                }
                if (ajaxObj.complete) {
                    ajaxObj.complete();
                }
                return;
            } else {
                delete CustomBuilder.cachedAjaxCalls[key];
            }
        }
        
        var orgSuccess = ajaxObj.success;
        ajaxObj.success = function(response) {
            CustomBuilder.cachedAjaxCalls[key] = {
                time : (new Date().getTime()),
                data : response
            };

            if (orgSuccess) {
                orgSuccess(response);
            }
        };
        
        var retried = false;
        var orgError = ajaxObj.error;
        ajaxObj.error = function(request, status, error) {
            if (request.status === 403 && !retried) {
                CustomBuilder.refreshSession(function(){
                    $.ajax(ajaxObj);
                });
                retried = true;
            } else if (orgError) {
                orgError(request, status, error);
            }
        };
        
        $.ajax(ajaxObj);
    },
    
    /*
     * Get screenshot of an element
     */
    getScreenshot: function(target, callback, errorCallback) {
        var hasSvg = false;
        if ($(target).find("svg[xmlns]").length > 0) {
            hasSvg = true;
            $(target).find("svg[xmlns]").each(function(i, svg){
                $(svg).addClass("hided");
                var newsvg = $(svg).clone().wrap('<p>').parent().html();
                var $tempCanvas = $('<canvas class="screenshotSvg"></canvas>');
                $tempCanvas.attr("style", $(svg).attr("style"));
                $tempCanvas.attr("width", $(svg).width());
                $tempCanvas.attr("height", $(svg).height());
                $(svg).after($tempCanvas);
                // fix duplicate xmlns
                newsvg = newsvg.replace('xmlns="http://www.w3.org/1999/xhtml"', '');
                // render
                canvg($tempCanvas[0], newsvg);
            });
        }
        target = $(target)[0];
        html2canvas(target, {logging : false}).then(function(canvas) {
            if (hasSvg === true) {
                $(target).find('canvas.screenshotSvg').remove();
            }
            var image = canvas.toDataURL("image/png");
            if (callback) {
                callback(image);
            }
        },
        function(error) {
            if (hasSvg === true) {
                $(target).find('canvas.screenshotSvg').remove();
            }
            if (errorCallback) {
                callback(error);
            }
        });
    },
    
    /*
     * Utility method to check is there an unsaved changes
     */
    isSaved : function(){
        var hasChange = false;
        
        if ($("body").hasClass("property-editor-right-panel") && !$("body").hasClass("no-right-panel")) {
            $(".element-properties .property-editor-container").each(function() {
                var editor = $(this).parent().data("editor");
                if (editor !== undefined && !editor.saved && editor.isChange()) {
                    hasChange = true;
                }
            });
        }
        
        if(((CustomBuilder.savedJson !== undefined && CustomBuilder.savedJson === $('#cbuilder-json').val()) ||
            ($('#cbuilder-json-original').val() === $('#cbuilder-json').val())) && !hasChange){
            return true;
        }else{
            return false;
        }
    },
    
    /*
     * Used to retrieve all the builder items in current app
     */
    getBuilderItems : function(callback) {
        if (callback) {
            CustomBuilder.builderItemsLoading.push(callback);
        }
        
        if (CustomBuilder.builderItemsLoading.length === 1) {
            $.ajax({
                type: "GET",
                url: CustomBuilder.contextPath + "/web/json/console/app/"+CustomBuilder.appId+"/"+CustomBuilder.appVersion+"/adminbar/builder/menu",
                dataType: 'json',
                success: function (data) {
                    CustomBuilder.builderItems = data;
                    // update appPublished after app generation
                    CustomBuilder.appPublished = data[2].appPublished;
                    if (CustomBuilder.appPublished === "true") {
                        $("#builderElementName .title .published").remove();
                        $("#builderElementName .title").append(data[2].published);
                    }
                    var callbacks = $.extend([], CustomBuilder.builderItemsLoading);
                    CustomBuilder.builderItemsLoading = [];
                    
                    for (var i in callbacks) {
                        callbacks[i](data);
                    }
                }
            });
        }
    },
    
    /*
     * Render additional menus to adding bar
     */
    intBuilderMenu : function() {
        if (CustomBuilder.systemTheme === undefined) {
            CustomBuilder.systemTheme = $('body').attr("builder-theme");
        }
        UI.userview_app_id = CustomBuilder.appId;
        
        if ($("#quick-nav-bar").find("#builder-quick-nav").length === 0) {
            $("#quick-nav-bar").append('<div id="closeQuickNav"></div>');
            $("#quick-nav-bar").append('<div id="builder-quick-nav">\
                <div id="builder-menu" style="display: none;">\
                    <div id="builder-menu-search">\
                        <input type="text" placeholder="'+get_cbuilder_msg("cbuilder.search") + '" value="" />\
                        <button class="clear-backspace" style="display:none;"><i class="la la-close"></i></button>\
                    </div><ul></ul>\
                </div></div>');

            $("#builder-menu-search input").off("keyup");
            $("#builder-menu-search input").on("keyup", function(){
                var searchText = $(this).val().toLowerCase();
                $("#builder-menu ul li ul li.item").each(function(){
                    var element = $(this);
                    element.hide();
                    if ($(element).find("a").text().toLowerCase().indexOf(searchText) > -1) { 
                        element.show();
                    }
                });
                if (searchText !== "") {
                    $("#builder-menu-search .clear-backspace").show();
                } else {
                    $("#builder-menu-search .clear-backspace").hide();
                }
            });
            
            $("#closeQuickNav").off("click");
            $("#closeQuickNav").on("click", function(){
                $("#quick-nav-bar").removeClass("active");
            });
            
            $(document).off("keydown.quicknav");
            $(document).on("keydown.quicknav", function(e) {
                // ESCAPE key pressed
                if (e.keyCode === 27) {
                    if ($("#quick-nav-bar").hasClass("active")) {
                        $("#quick-nav-bar").removeClass("active");
                    } else if ($("body").hasClass('property-editor-right-panel') && !$("body").hasClass('no-right-panel')) {
                        CustomBuilder.closePropertiesWindow();
                    }
                }
            });

            $("#builder-menu-search .clear-backspace").off("click");
            $("#builder-menu-search .clear-backspace").on("click", function(){
                $("#builder-menu ul li ul li").show();
                $("#builder-menu-search input").val("");
                $("#builder-menu-search .clear-backspace").hide();
            });
            
            $("#builder-quick-nav").off("click", "li.builder-icon");
            $("#builder-quick-nav").on("click", "li.builder-icon", function(){
                HelpGuide.hide();
                $("#quick-nav-bar").addClass("active");
                return false;
            });

            $("#builder-quick-nav").off("click", "a.launch");
            $("#builder-quick-nav").on("click", "a.launch", function(){
                window.open($(this).attr('href'));
                return false;
            });
            
            $("#builder-quick-nav").off("click", "a.builder-link");
            $("#builder-quick-nav").on("click", "a.builder-link", function(){
                CustomBuilder.ajaxRenderBuilder($(this).attr("href"));
                return false;
            });

            $("#builder-menu ul").off("click", ".addnew a");
            $("#builder-menu ul").on("click", ".addnew a", function(){
                var type = $(this).data("type");
                if (type === "process") {
                    CustomBuilder.ajaxRenderBuilder(CustomBuilder.contextPath + '/web/console/app' + CustomBuilder.appPath + '/process/builder');
                } else {
                    var url = CustomBuilder.contextPath + '/web/console/app' + CustomBuilder.appPath + '/';
                    if (type === "form" || type === "datalist" || type === "userview") {
                        url += type + '/create?builderMode=true';
                    } else {
                        url += "cbuilder/" + type + "/create?builderMode=false";
                    }
                    JPopup.show("navCreateNewDialog", url, {}, "");
                }
                return false;
            });
        }
        $("#quick-nav-bar").removeClass("active");
        
        if (CustomBuilder.systemTheme === 'light' || CustomBuilder.systemTheme === 'dark') {
            $("#save-btn > span").text(get_cbuilder_msg('ubuilder.save'));
            $("#save-btn > i").removeClass("zmdi zmdi-check");
            $("#save-btn > i").addClass("las la-cloud-upload-alt");
            $('body').attr("builder-theme", CustomBuilder.systemTheme);
            var iframes = $('iframe');
            if (iframes.length > 0) {
                var iframeBody = iframes.contents().find('body');
                iframeBody.attr("builder-theme", CustomBuilder.systemTheme);
                if (CustomBuilder.systemTheme === 'dark' && CustomBuilder.builderType !== "userview") {
                    iframeBody.addClass("dark-mode");
                }
            }
        }
        
        setTimeout(function(){
            CustomBuilder.reloadBuilderMenu();
        }, 100); //delay the loading to prevent it block the builder ajax call
    },
    
    reloadBuilderMenu : function() {
        CustomBuilder.getBuilderItems(CustomBuilder.renderBuilderMenu);
    },
    
    renderBuilderMenu : function(data) {
        $("#builder-menu > ul.app_tools, #builder-menu > .seperator").remove();
        $("#builder-menu > ul").html("");
        
        var container = $("#builder-menu > ul");
        
        $("#builder-quick-nav .backToApp").remove();
        $("#builder-quick-nav").prepend('<div class="backToApp"><a class="builder-link" href="'+CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/builders"  target="_self" title="'+get_cbuilder_msg("abuilder.title")+'"><i class="far fa-edit"></i></a></div>');
        
        CustomBuilder.builderTypes = [];
        for (var i in data) {
            var builder = data[i];
            var li = $('<li class="builder-icon menu-'+builder.value+'"><span tooltip-position="right" title="'+builder.label+'" style="background: '+builder.color+';color: '+builder.color+'"><i class="'+builder.icon+'"></i></span><ul></ul></li>');
            $(li).find("ul").append('<li class="header"><span class="header-label">'+builder.label+'</span> <span class="addnew"><a data-type="'+builder.value+'"><i class="las la-plus"></i> '+get_cbuilder_msg("cbuilder.addnew")+'</a></span></li>');
            CustomBuilder.builderTypes.push(builder.value);
            if (builder.elements) {
                for (var j in builder.elements) {
                    var extra = '';
                    if (builder.value === "userview" && CustomBuilder.appPublished === "true") {
                        extra = '<a class="launch" title="'+get_cbuilder_msg('ubuilder.launch')+'" href="'+CustomBuilder.contextPath+'/web/userview/'+CustomBuilder.appId+'/'+builder.elements[j].id+'" target="_blank"><i class="zmdi zmdi-play"></i></a>';
                    }
                    $(li).find("ul").append('<li class="item" data-id="'+builder.elements[j].id+'" ><a class="builder-link" href="'+builder.elements[j].url+'" target="_self">'+builder.elements[j].label+'</a>'+extra+'</li>');
                }
            }
            container.append(li);
        }
        
        container.after('<span class="seperator"></span><ul class="app_tools"></ul>');
        
        var appTools = $("#builder-menu > ul.app_tools");
        appTools.append('<li><a title="'+get_cbuilder_msg('abuilder.notes')+'" id="appDesc-btn" data-cbuilder-view="appDesc" href="'+CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/note" data-cbuilder-action="switchView" data-hide-tool=""><i class="las la-sticky-note"></i></a></li>');
        appTools.append('<li><a title="'+get_cbuilder_msg('abuilder.envVariable')+'" id="variables-btn" data-cbuilder-view="envVariables" href="'+CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/envVariable" data-cbuilder-action="switchView" data-hide-tool=""><i class="word-icon" style="font-size: 75%; font-weight: 350; line-height: 20px; vertical-align: top; display:inline-block; letter-spacing: 0.6px;">{x}</i></a></li>');
        appTools.append('<li><a title="'+get_cbuilder_msg('abuilder.appMessage')+'" id="appMessage-btn" data-cbuilder-view="appMessage" data-cbuilder-action="switchView" data-hide-tool=""><i class="la la-language"</i></a></li>');
        appTools.append('<li><a title="'+get_cbuilder_msg('abuilder.resources')+'" id="resources-btn" data-cbuilder-view="resources" href="'+CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/resources" data-cbuilder-action="switchView" data-hide-tool=""><i class="lar la-file-image"></i> </a></li>');
        appTools.append('<li><a title="'+get_cbuilder_msg('abuilder.pluginDefault')+'" id="plugin-default-btn" data-cbuilder-view="pluginDefaultProperties" href="'+CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/properties" data-cbuilder-action="switchView" data-hide-tool=""><i class="las la-plug"></i> </a></li>');
        if (CustomBuilder.isGlowrootAvailable === "true") {
            appTools.append('<li><a title="'+get_cbuilder_msg('abuilder.performance')+'" id="performance-btn" data-cbuilder-view="performance" href="'+CustomBuilder.contextPath+'/web/console/app/'+CustomBuilder.appId+'/performance" data-cbuilder-action="switchView" data-hide-tool=""><i class="las la-tachometer-alt"></i> </a></li>');
        }
        appTools.append('<li><a title="'+get_cbuilder_msg('abuilder.logs')+'" id="logs-btn" data-cbuilder-view="logViewer" href="'+CustomBuilder.contextPath+'/web/console/app/'+CustomBuilder.appId+'/logs" data-cbuilder-action="switchView" data-hide-tool=""><i class="las la-scroll"></i> </a></li>');
        
        CustomBuilder.initBuilderActions(appTools);

        $("#builder-menu > ul > li.menu-" + CustomBuilder.builderType).addClass("active");

        $("#builder-menu > ul > li.builder-icon").off("click touch");
        $("#builder-menu > ul > li.builder-icon").on("click touch", function(){
            $("#builder-menu > ul > li.builder-icon").removeClass("active");
            $(this).addClass("active");
        });
        
        $("body").addClass("quick-nav-shown");
    },
    
    /*
     * stop previous Presence Indicator and start with new url
     */
    updatePresenceIndicator : function() {
        if (PresenceUtil.source !== null) {
            PresenceUtil.source.close();
            PresenceUtil.source = null;
        }
        PresenceUtil.createEventSource();
    },
    
    /*
     * Block the page and add listener to check session
     */
    sessionTimeout : function() {
        //blocking the page and show alert message
        if ($("body").find(".session_timeout").length === 0) {
            $("body").append('<div class="session_timeout" style="position:fixed;top:0;left:0;right:0;bottom:0;background:#727272bf;z-index:100000;"><div></div></div>');
            var sessionDiv = $("body").find('.session_timeout > div');
            $(sessionDiv).css({
                "display" : "block",
                "width": "450px",
                "max-width": "90%",
                "text-align": "center",
                "background": "#fff",
                "padding": "20px",
                "border-radius": "10px",
                "top": "50%",
                "left": "50%",
                "position": "absolute",
                "transform": "translate(-50%, -50%)"
            });
            $(sessionDiv).append('<p>'+get_cbuilder_msg('cbuilder.sessionTimeout')+'</p>');
            $(sessionDiv).append('<a href="'+CustomBuilder.contextPath+'/web/presence" target="_blank" class="btn btn-primary">'+get_cbuilder_msg('ubuilder.login')+'</a>');
            $(sessionDiv).append('<p>'+get_cbuilder_msg('cbuilder.doNotClose')+'</p>');
            $(sessionDiv).append('<p><i class="fas fa-spin fa-spinner" style="font-size: 40px; color: #ccc;"></i></p>');
            
            //adding listener
            CustomBuilder.addVisibilityChangeEvent("session", function(event, hidden) {
                if (!document[hidden]) {
                    CustomBuilder.refreshSession(CustomBuilder.sessionCheck);
                }
            });
        }
    },
    
    /*
     * Check a user is logged in and having permission to continue edit
     */
    sessionCheck : function() {
        $.ajax({
            type: 'POST',
            url: UI.base + "/web/json/console/app"+CustomBuilder.appPath+"/check",
            dataType: "json",
            success: function (response) {
                if (response !== null && response !== undefined){
                    if (response.status) {
                        $("body").find(".session_timeout").remove();
                        CustomBuilder.removeVisibilityChangeEvent("session");
                    }
                }
            }
        });
    },

    /*
     * Update all csrf token
     */
    refreshSession : function(callback) {
        //refresh ConnectionManager token
        $.ajax({
            type: 'POST',
            url: UI.base + "/csrf",
            headers: {
                "FETCH-CSRF-TOKEN-PARAM":"true",
                "FETCH-CSRF-TOKEN":"true"
            },
            success: function (response) {
                var temp = response.split(":");
                ConnectionManager.tokenValue = temp[1];
                JPopup.tokenValue = temp[1];

                $("iframe").each(function() {
                    try {
                        if (this.contentWindow.ConnectionManager !== undefined) {
                            this.contentWindow.ConnectionManager.tokenValue = temp[1];
                        }
                        if (this.contentWindow.JPopup !== undefined) {
                            this.contentWindow.JPopup.tokenValue = temp[1];
                        }
                    } catch(err) {}
                });

                if (callback) {
                    callback();
                }
            }
        });
    },
    
    /*
     * Add listener for browser tab visibility changes
     */
    addVisibilityChangeEvent : function(key, listener) {
        if (!(typeof document.addEventListener === "undefined")) {
            var hidden, visibilityChange;
            if (typeof document.hidden !== "undefined") { // Opera 12.10 and Firefox 18 and later support 
                hidden = "hidden";
                visibilityChange = "visibilitychange";
            } else if (typeof document.msHidden !== "undefined") {
                hidden = "msHidden";
                visibilityChange = "msvisibilitychange";
            } else if (typeof document.webkitHidden !== "undefined") {
                hidden = "webkitHidden";
                visibilityChange = "webkitvisibilitychange";
            }
            if (CustomBuilder.visibilityChangeEvents === undefined) {
                CustomBuilder.visibilityChangeEvents = {};
            }

            CustomBuilder.visibilityChangeEvents[key] = function(event) {
                listener(event, hidden);
            };
            document.addEventListener(visibilityChange, CustomBuilder.visibilityChangeEvents[key], false);
        }
    },
    
    /*
     * Remove listener for browser tab visibility changes
     */
    removeVisibilityChangeEvent : function(key) {
        if (CustomBuilder.visibilityChangeEvents !== undefined && CustomBuilder.visibilityChangeEvents[key] !== undefined) {
            if (!(typeof document.removeEventListener === "undefined")) {
                var visibilityChange;
                if (typeof document.hidden !== "undefined") { // Opera 12.10 and Firefox 18 and later support 
                    visibilityChange = "visibilitychange";
                } else if (typeof document.msHidden !== "undefined") {
                    visibilityChange = "msvisibilitychange";
                } else if (typeof document.webkitHidden !== "undefined") {
                    visibilityChange = "webkitvisibilitychange";
                }
                document.removeEventListener(visibilityChange, CustomBuilder.visibilityChangeEvents[key], false);
                delete CustomBuilder.visibilityChangeEvents[key];
            }
        }
    },
    
    /*
     *  remove all the custom styling from all elements 
     */
    clearCustomStyling : function(data, checker) {
        var props = Object.getOwnPropertyNames(data);
        for (var i = 0; i < props.length; i++) {
            var name = props[i];
            if (typeof data[name] === "object") {
                CustomBuilder.clearCustomStyling(data[name], checker);
            } else if (Array.isArray(data[name])) {
                for (var j = 0; j < data[name].length; j++) {
                    CustomBuilder.clearCustomStyling(data[name][j], checker);
                }
            } else if (name.indexOf("style-") === 0 || (checker !== undefined && checker(name))) {
                delete data[name];
            }
        }
    }
};

/*
 * Default builder to manage the palette and canvas
 */
_CustomBuilder.Builder = {
    zoom : 1,
    dragMoveMutation : false,
    mousedown : false,
    defaultOptions : {
        "enableViewport" : true,
        "enableCopyPaste" : true,
        callbacks : {
            "initComponent" : "",
            "renderElement" : "",
            "selectElement" : "",
            "updateElementId" : "",
            "unloadElement" : "",
            "copyElement" : ""
        }
    },
    options : {},
    
    /*
     * A stating point to use the default builder
     */
    init : function(options, callback) {
        CustomBuilder.Builder.options = $.extend(true, {}, CustomBuilder.Builder.defaultOptions);
        CustomBuilder.Builder.options = $.extend(true, CustomBuilder.Builder.options, options);
        
        if (CustomBuilder.Builder.options.enableViewport) {
            $("#top-panel .responsive-buttons").show();
            $("body").addClass("viewport-enabled");
            CustomBuilder.viewport("desktop");
        } else {
            $("#top-panel .responsive-buttons").hide();
        }
        
        if (CustomBuilder.Builder.options.enableCopyPaste) {
            $("#builderToolbar .copypaste").show();
            
            CustomBuilder.addVisibilityChangeEvent("paste", CustomBuilder.Builder.updatePasteStatus);
        } else {
            $("#builderToolbar .copypaste").hide();
        }
        
        var self = this;
        self.selectedEl = null;
	self.highlightEl = null;
        self.zoom = 1;
                
        $("#builder_canvas").append('<div id="iframe-wrapper"> \
                <div id="iframe-layer"> \
                    <div id="element-parent-box"> \
                        <div id="element-parent-name"></div> \
                    </div> \
                    <div id="element-highlight-box"> \
                        <div id="element-highlight-name"><div class="element-name"></div> \
                            <div class="element-actions">   \
                                <a class="parent-btn" title="'+get_cbuilder_msg("cbuilder.selectParent")+'"><i class="las la-level-up-alt"></i></a> \
                                <a class="up-btn" title="'+get_cbuilder_msg("cbuilder.moveUp")+'"><i class="la la-arrow-up"></i></a> \
                                <a class="down-btn" title="'+get_cbuilder_msg("cbuilder.moveDown")+'"><i class="la la-arrow-down"></i></a> \
                                <a class="left-btn" title="'+get_cbuilder_msg("cbuilder.moveLeft")+'"><i class="la la-arrow-left"></i></a> \
                                <a class="right-btn" title="'+get_cbuilder_msg("cbuilder.moveRight")+'"><i class="la la-arrow-right"></i></a> \
                                <span class="element-options"> \
                                    \
                                </span>   \
                                <a class="delete-btn" href="" title="'+get_cbuilder_msg("cbuilder.remove")+'"><i class="la la-trash"></i></a> \
                            </div> \
                        </div> \
                        <div class="element-bottom-actions">   \
                        </div> \
                    </div> \
                    <div id="element-select-box"> \
                        <div id="element-select-name"><div class="element-name"></div> \
                            <div class="element-actions">   \
                                <a class="parent-btn" title="'+get_cbuilder_msg("cbuilder.selectParent")+'"><i class="las la-level-up-alt"></i></a> \
                                <a class="up-btn" title="'+get_cbuilder_msg("cbuilder.moveUp")+'"><i class="la la-arrow-up"></i></a> \
                                <a class="down-btn" title="'+get_cbuilder_msg("cbuilder.moveDown")+'"><i class="la la-arrow-down"></i></a> \
                                <a class="left-btn" title="'+get_cbuilder_msg("cbuilder.moveLeft")+'"><i class="la la-arrow-left"></i></a> \
                                <a class="right-btn" title="'+get_cbuilder_msg("cbuilder.moveRight")+'"><i class="la la-arrow-right"></i></a> \
                                <span class="element-options"> \
                                    \
                                </span>   \
                                <a class="delete-btn" href="" title="'+get_cbuilder_msg("cbuilder.remove")+'"><i class="la la-trash"></i></a> \
                            </div> \
                        </div> \
                        <div class="element-bottom-actions">   \
                        </div> \
                    </div> \
                </div> \
                <iframe src="about:none" id="iframe1"></iframe> \
            </div>');
        
        $("#style-properties-tab-link").show();
        
        self.documentFrame = $("#iframe-wrapper > iframe");
        self.canvas = $("#builder_canvas");
        
        $("body").addClass("default-builder");
        $("body").addClass(CustomBuilder.builderType);
        
        self._loadIframe(CustomBuilder.contextPath+'/builder/blank.jsp', callback);
        
        self._initDragdrop();
        self._initBox();
        
        self.dragElement = null;
    },
    
    /*
     * Reset to initial state
     */
    reset: function() {
        var self = CustomBuilder.Builder;
        self.unbindEvent("change.builder");
        self.unbindEvent("nodeAdditionalSelected nodeAdditionalAdded nodeAdditionalRemoved nodeAdditionalModeChanged");
        $("body").removeClass("default-builder");
        $("body").removeClass("viewport-enabled");
    },
    
    /*
     * Render the json to canvas
     */
    load: function (data, callback) {
        if (CustomBuilder.systemTheme === undefined) {
            CustomBuilder.systemTheme = $('body').attr("builder-theme");
        }
        if (CustomBuilder.systemTheme === 'light' || CustomBuilder.systemTheme === 'dark') {
            if (CustomBuilder.systemTheme === "dark" && CustomBuilder.builderType !== "userview") {
                CustomBuilder.Builder.frameBody.addClass("dark-mode");
            } else {
                CustomBuilder.Builder.frameBody.removeClass("dark-mode");
            }
        }
        CustomBuilder.Builder.frameBody.addClass("initializing");
        
        var self = CustomBuilder.Builder;
        
        var selectedELSelector = "";
        var selectedElIndex = 0;
        
        //to handle change of id
        var selectedELAltSelector = "";
        var selectedElAltIndex = 0;
        
        if (self.selectedEl) {
            if ($(self.selectedEl).is("[data-cbuilder-id]")) {
                selectedELSelector = '[data-cbuilder-id="'+ $(self.selectedEl).data("cbuilder-id") +'"]';
            } else {
                selectedELSelector = '[data-cbuilder-id="'+ $(self.selectedEl).closest('[data-cbuilder-id]').data("cbuilder-id") +'"]';
                selectedELSelector += ' [data-cbuilder-classname="' + $(self.selectedEl).data("cbuilder-classname") + '"]';
            }
            selectedElIndex = self.frameBody.find(selectedELSelector).index(self.selectedEl);
            
            //to handle change of id
            selectedELAltSelector = '[data-cbuilder-id="'+ $(self.selectedEl).parent().closest('[data-cbuilder-id]').data("cbuilder-id") +'"]';
            selectedELAltSelector += ' [data-cbuilder-classname="' + $(self.selectedEl).data("cbuilder-classname") + '"]';
            selectedElAltIndex = self.frameBody.find(selectedELAltSelector).index(self.selectedEl);
        } 
        
        self.frameBody.html("");
        self.selectNode(false);
        $("#element-parent-box, #element-highlight-box").hide();
        
        var component = self.parseDataToComponent(data);
        var temp = $('<div></div>');
        self.frameBody.append(temp);
        self.renderElement(data, temp, component, false, null, function(){
            if (self.nodeAdditionalType !== undefined && self.nodeAdditionalType !== "") {
                CustomBuilder.Builder.renderNodeAdditional(self.nodeAdditionalType);
            }
            
            //reselect previous selected element
            if (selectedELSelector !== "") {
                var element = self.frameBody.find(selectedELSelector);
                
                //to handle change of id
                if (element.length === 0) {
                    element = self.frameBody.find(selectedELAltSelector);
                    selectedElIndex = selectedElAltIndex;
                }
                
                if (element.length > 1) {
                    var elements = element;
                    do {
                        element = elements[selectedElIndex];
                    } while (element === undefined && selectedElIndex-- > 0);
                }
                if ($(element).length > 0) {
                    self.selectNode(element);
                }
            } else {
                CustomBuilder.Builder.updatePasteStatus();
            }
            
            if (callback) {
                callback();
            }
            
            setTimeout(function(){
                CustomBuilder.Builder.frameBody.removeClass("initializing");
            }, 1);
        });
        
        if (component !== null && component.builderTemplate.isPastable(data, component)) {
            $("#paste-element-btn").removeClass("disabled");
        }
        
        $("#iframe-wrapper").show();
    },
    
    /*
     * Used to decide what builder component use for the element data
     */
    parseDataToComponent : function(data) {
        var self = CustomBuilder.Builder;
        
        var component = null;
        if (data !== undefined && data.className !== undefined) {
            component = self.getComponent(data.className);
        } else if (self.options.callbacks !== undefined && self.options.callbacks['parseDataToComponent'] !== undefined && self.options.callbacks['parseDataToComponent'] !== "") {
            component = CustomBuilder.callback(self.options.callbacks['parseDataToComponent'], [data]);
        }
        
        return component;
    },
    
    /*
     * Find the child elements of the element data
     */
    parseDataChildElements : function(data, component) {
        var self = CustomBuilder.Builder;
        
        if (data[component.builderTemplate.getChildsDataHolder(data, component)] !== undefined) {
            return data[component.builderTemplate.getChildsDataHolder(data, component)];
        } else if (self.options.callbacks !== undefined && self.options.callbacks['parseDataChildElements'] !== undefined && self.options.callbacks['parseDataChildElements'] !== "") {
            return CustomBuilder.callback(self.options.callbacks['parseDataChildElements'], [data]);
        }
        return null;
    },
    
    /*
     * Find the properties of the element data
     */
    parseElementProps : function(data) {
        if (data.properties !== undefined) {
            return data.properties;
        } else {
            return data;
        }
    },
    
    /*
     * Load/update/re-rendering child elements of the element data
     */
    loadAndUpdateChildElements: function(element, elementObj, component, deferreds) {
        var self = CustomBuilder.Builder;
        var elements = self.parseDataChildElements(elementObj, component);
        
        if (elements !== null && elements.length > 0) {
            var container = $(element);
            if (!$(element).is('[data-cbuilder-'+component.builderTemplate.getChildsContainerAttr(elementObj, component)+']')) {
                container = $(element).find('[data-cbuilder-'+component.builderTemplate.getChildsContainerAttr(elementObj, component)+']:eq(0)');
            }
            
            if ($(container).find("[data-cbuilder-classname]").length === 0) {  //empty container, just load it
                for (var i in elements) {
                    var childComponent = self.parseDataToComponent(elements[i]);
                    if (childComponent !== undefined && childComponent !== null) {
                        var temp = $('<div></div>');
                        $(container).append(temp);

                        var select = false;

                        self.renderElement(elements[i], temp, childComponent, select, deferreds);
                    }
                }
            } else { //compare and update
                var i = 0;
                $(container).find("> [data-cbuilder-classname]").each(function() {
                    var data = $(this).data("data");
                    var classname = $(this).data("cbuilder-classname");
                    var childComponent = self.parseDataToComponent(elements[i]);
                    var props = self.parseElementProps(elements[i]);
                    if ((data === undefined || data === null) && childComponent !== null && childComponent !== undefined && classname === childComponent.className) {
                        $(this).data("data", elements[i]);
                        
                        var id = props.id;
                        if (id === undefined && elements[i].id !== undefined) {
                            id = elements[i].id;
                        }
                        
                        $(this).attr("data-cbuilder-id", id);
                        
                        self.loadAndUpdateChildElements($(this), elements[i], childComponent, deferreds);
                    } else {
                        //TODO: if differrent, need add it?
                    }
                    
                    if ($(this).find("> .clear-float").length === 0) { //add clear float to check hight in case the childs are all floated
                        $(this).append('<div class="clear-float"></div>');
                    }
                    if ($(this).outerHeight(false) === 0) {
                        $(this).attr("data-cbuilder-invisible", "");
                    }
                    i++;
                });
            }
        }
        
        var childsContainerAttr = component.builderTemplate.getChildsContainerAttr(elementObj, component);
        if (childsContainerAttr !== undefined && childsContainerAttr !== null && childsContainerAttr !== "" && 
            ($(element).is('[data-cbuilder-'+childsContainerAttr+']') || 
            $(element).find('[data-cbuilder-'+childsContainerAttr+']').length > 0)) {
            $(element).attr('data-cbuilder-iscontainer', '');
        } else {
            $(element).attr('data-cbuilder-noncontainer', '');
        }
    },
    
    /*
     * Prepare the iframe for canvas
     */
    _loadIframe: function (url, callback) {
        var self = this;
        self.iframe = this.documentFrame.get(0);
        self.iframe.src = url;

        return this.documentFrame.on("load", function ()
        {
            window.FrameWindow = self.iframe.contentWindow;
            window.FrameDocument = self.iframe.contentWindow.document;
            $("#element-parent-box, #element-highlight-box").hide();

            $(window.FrameWindow).off("scroll resize");
            $(window.FrameWindow).on("scroll resize", function (event) {
                self._updateBoxes();
            });
            
            window.FrameWindow["UI"].userview_app_id = CustomBuilder.appId;

            return self._frameLoaded(callback);
        });
    },
    
    /*
     * Update the position of the highlight and select box
     */
    _updateBoxes : function() {
        var self = this;
        if (self.selectedEl)
        {
            var node = self.selectedEl;
            if (!self.selectedEl.is(":visible") || self.selectedEl.is("[data-cbuilder-uneditable]")) {
                var id = $(node).data('cbuilder-id');
                if (self.frameBody.find('[data-cbuilder-select="'+id+'"]:visible').length > 0) {
                    node = self.frameBody.find('[data-cbuilder-select="'+id+'"]:visible').first();
                }
            }
            var box = self.getBox(node);

            $("#element-select-box").css(
                {"top": box.top - self.frameDoc.scrollTop(),
                    "left": box.left - self.frameDoc.scrollLeft(),
                    "width": box.width,
                    "height": box.height,
                    "display" : "block"
                });
        }

        if (self.highlightEl)
        {
            var node = self.highlightEl;
            if (!self.highlightEl.is(":visible") || self.highlightEl.is("[data-cbuilder-uneditable]")) {
                var id = $(node).data('cbuilder-id');
                if (self.frameBody.find('[data-cbuilder-select="'+id+'"]:visible').length > 0) {
                    node = self.frameBody.find('[data-cbuilder-select="'+id+'"]:visible').first();
                }
            }
            var box = self.getBox(node);

            $("#element-highlight-box").css(
                {"top": box.top - self.frameDoc.scrollTop(),
                    "left": box.left - self.frameDoc.scrollLeft(),
                    "width": box.width,
                    "height": box.height,
                    "display" : "block"
                });
        }
    },
    
    /*
     * Used to initialize the canvas iframe once it is  loaded
     */
    _frameLoaded : function(callback) {
		
        var self = CustomBuilder.Builder;

        self.frameDoc = $(window.FrameDocument);
        self.frameHtml = $(window.FrameDocument).find("html");
        self.frameBody = $(window.FrameDocument).find("body");
        self.frameHead = $(window.FrameDocument).find("head");

        //insert editor helpers like non editable areas
        self.frameHead.append('<link data-cbuilder-helpers href="' + CustomBuilder.contextPath + '/builder/editor-helpers.css" rel="stylesheet">');

        self._initHighlight();

        $(window).triggerHandler("cbuilder.iframe.loaded", self.frameDoc);
        
        if (callback)
            callback();
    },	
    
    /*
     * Used to get the element label of the highlight and select box
     */
    _getElementType: function (data, component) {
        var label = null;
        
        if (component.builderTemplate.getLabel) {
            label = component.builderTemplate.getLabel(data, component);
        }
        
        if (label === null || label === undefined || label === "") {
            label = component.label;
        }
        
        return label;
    },
    
    /*
     * Move the selected element up
     */
    moveNodeUp: function (node) {
        var self = CustomBuilder.Builder;
        
        if (!node) {
            node = self.selectedEl;
        }
        
        var elementObj = $(node).data("data");
        var component = self.parseDataToComponent(elementObj);
        
        var prev = $(node).prev("[data-cbuilder-classname]");
        if (prev.length > 0) {
            var parent = self.selectedEl.parent().closest("[data-cbuilder-classname]");
            if (parent.length === 0) {
                parent = self.selectedEl.closest("body");
            }
            var parentDataArray = $(parent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)];
            if (parentDataArray === undefined) {
                parentDataArray = [];
                $(parent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)] = parentDataArray;
            }
            var oldIndex = $.inArray($(self.selectedEl).data("data"), parentDataArray);
            if (oldIndex !== -1) {
                parentDataArray.splice(oldIndex, 1);
            }
            
            $(prev).before(node);
            var newIndex = $.inArray($(prev).data("data"), parentDataArray);
            parentDataArray.splice(newIndex, 0, $(self.selectedEl).data("data"));
        } else {
            var parentArr = self.frameHtml.find('[data-cbuilder-'+component.builderTemplate.getParentContainerAttr(elementObj, component)+']');
            var index = parentArr.index($(node).closest('[data-cbuilder-'+component.builderTemplate.getParentContainerAttr(elementObj, component)+']'));
            if (index > 0) {
                var parent = self.selectedEl.parent().closest("[data-cbuilder-classname]");
                if (parent.length === 0) {
                    parent = self.selectedEl.closest("body");
                }
                var parentDataArray = $(parent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)];
                if (parentDataArray === undefined) {
                    parentDataArray = [];
                    $(parent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)] = parentDataArray;
                }
                var oldIndex = $.inArray($(self.selectedEl).data("data"), parentDataArray);
                if (oldIndex !== -1) {
                    parentDataArray.splice(oldIndex, 1);
                }
                
                var newParent = $(parentArr[index - 1]).closest("[data-cbuilder-classname]");
                $(parentArr[index - 1]).append(node);
                parentDataArray = $(newParent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)];
                if (parentDataArray === undefined) {
                    parentDataArray = [];
                    $(newParent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)] = parentDataArray;
                }
                parentDataArray.push($(self.selectedEl).data("data"));
                
                self.checkVisible(parent);
                self.checkVisible(newParent);
            }
        }
        if (component.builderTemplate.afterMoved)
            component.builderTemplate.afterMoved(self.selectedEl, elementObj, component);
        
        if (self.subSelectedEl) {
            self.selectNodeAndShowProperties(self.subSelectedEl, false, (!$("body").hasClass("no-right-panel")));
        } else {
            self.selectNodeAndShowProperties(self.selectedEl, false, (!$("body").hasClass("no-right-panel")));
        }

        CustomBuilder.update();
        self.triggerChange();
    },
    
    /*
     * Move the selected element down
     */
    moveNodeDown: function (node) {
        var self = CustomBuilder.Builder;
        
        if (!node) {
            node = self.selectedEl;
        }
        
        var elementObj = $(node).data("data");
        var component = self.parseDataToComponent(elementObj);
        
        var next = $(node).next("[data-cbuilder-classname]");
        if (next.length > 0) {
            var parent = self.selectedEl.parent().closest("[data-cbuilder-classname]");
            if (parent.length === 0) {
                parent = self.selectedEl.closest("body");
            }
            var parentDataArray = $(parent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)];
            if (parentDataArray === undefined) {
                parentDataArray = [];
                $(parent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)] = parentDataArray;
            }
            var oldIndex = $.inArray($(self.selectedEl).data("data"), parentDataArray);
            if (oldIndex !== -1) {
                parentDataArray.splice(oldIndex, 1);
            }
            
            $(next).after(node);
            var newIndex = $.inArray($(next).data("data"), parentDataArray) + 1;
            parentDataArray.splice(newIndex, 0, $(self.selectedEl).data("data"));
        } else {
            var parentArr = self.frameHtml.find('[data-cbuilder-'+component.builderTemplate.getParentContainerAttr(elementObj, component)+']');
            var index = parentArr.index($(node).closest('[data-cbuilder-'+component.builderTemplate.getParentContainerAttr(elementObj, component)+']'));
            if (index < parentArr.length - 1) {
                var parent = self.selectedEl.parent().closest("[data-cbuilder-classname]");
                if (parent.length === 0) {
                    parent = self.selectedEl.closest("body");
                }
                var parentDataArray = $(parent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)];
                if (parentDataArray === undefined) {
                    parentDataArray = [];
                    $(parent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)] = parentDataArray;
                }
                var oldIndex = $.inArray($(self.selectedEl).data("data"), parentDataArray);
                if (oldIndex !== -1) {
                    parentDataArray.splice(oldIndex, 1);
                }
                
                var newParent = $(parentArr[index + 1]).closest("[data-cbuilder-classname]");
                $(parentArr[index + 1]).find('[data-cbuilder-classname]:eq(0)').before(node);
                parentDataArray = $(newParent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)];
                if (parentDataArray === undefined) {
                    parentDataArray = [];
                    $(newParent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)] = parentDataArray;
                }
                parentDataArray.splice(0, 0, $(self.selectedEl).data("data"));
                
                self.checkVisible(parent);
                self.checkVisible(newParent);
            }
        }
        if (component.builderTemplate.afterMoved)
            component.builderTemplate.afterMoved(self.selectedEl, elementObj, component);
        
        if (self.subSelectedEl) {
            self.selectNodeAndShowProperties(self.subSelectedEl, false, (!$("body").hasClass("no-right-panel")));
        } else {
            self.selectNodeAndShowProperties(self.selectedEl, false, (!$("body").hasClass("no-right-panel")));
        }

        CustomBuilder.update();
        self.triggerChange();
    },
    
    /*
     * Copy the selected element and paste a clone element after it
     */
    cloneNode:  function(node) {
        var self = CustomBuilder.Builder;
        
        if (!node) {
            node = self.selectedEl;
        }
        
        var elementObj = $.extend(true, {}, $(node).data("data"));
        self.component = self.parseDataToComponent(elementObj);
        
        self.updateElementId(elementObj);
        
        var parent = $(node).parent().closest("[data-cbuilder-classname]");
        if ($(parent).length === 0) {
            parent = $(node).closest("body");
        }
        var parentDataArray = $(parent).data("data")[self.component.builderTemplate.getParentDataHolder(elementObj, self.component)];
        if (parentDataArray === undefined) {
            parentDataArray = [];
            $(parent).data("data")[self.component.builderTemplate.getParentDataHolder(elementObj, self.component)] = parentDataArray;
        }
        var newIndex = $.inArray($(node).data("data"), parentDataArray) + 1;
        parentDataArray.splice(newIndex, 0, elementObj);
        
        var temp = $('<div></div>');
        $(node).after(temp);
        
        self.renderElement(elementObj, temp, self.component, true);
        
        CustomBuilder.update();
    },
    
    /*
     * Copy the selected element and save in cache
     */
    copyNode: function(node) {
        var self = CustomBuilder.Builder;
        
        if (!node) {
            node = self.selectedEl;
        }
        
        var data = $(node).data("data");
        var component = self.parseDataToComponent(data);
        var type = component.builderTemplate.getParentContainerAttr(data, component);
        
        CustomBuilder.copy(data, type);
        
        if (CustomBuilder.Builder.options.callbacks["copyElement"] !== undefined && CustomBuilder.Builder.options.callbacks["copyElement"] !== "") {
            CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["copyElement"], [data, type]);
        }
        
        self.selectNode(self.selectedEl);
        
        if (component.builderTemplate.isPastable(data, component)) {
            $("#paste-element-btn").removeClass("disabled");
        }
    },
    
    /*
     * Paste the copied element in cache. 
     * First check the copied element can place as children of the selected element,
     * else check can the copied element can place as sibling of the selected element
     */
    pasteNode: function(node) {
        var self = CustomBuilder.Builder;
        
        if (!node) {
            node = self.selectedEl;
            if (!node) {
                node = self.frameBody.find('[data-cbuilder-classname]:eq(0)');
            }
        }
        
        self.component = self.parseDataToComponent($(node).data("data"));
        
        var data = CustomBuilder.getCopiedElement();
        var copiedObj = $.extend(true, {}, data.object);
        var copiedComponent = self.parseDataToComponent(copiedObj);
        
        self.updateElementId(copiedObj);
        
        if (copiedComponent.builderTemplate.isAbsolutePosition(copiedObj, copiedComponent)) {
            copiedObj.x_offset = parseInt(copiedObj.x_offset) + 5;
            copiedObj.y_offset = parseInt(copiedObj.y_offset) + 5;
        }
        
        if (copiedComponent.builderTemplate.customPasteData) {
            copiedComponent.builderTemplate.customPasteData(copiedObj, copiedComponent);
        }

        if (CustomBuilder.Builder.options.callbacks["pasteElement"] !== undefined && CustomBuilder.Builder.options.callbacks["pasteElement"] !== "") {
            CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["pasteElement"], [node, $(node).data("data"), self.component, copiedObj, copiedComponent]);
        } else {
            self._pasteNode(node, copiedObj, copiedComponent);
        }
        
        CustomBuilder.update();
    },
    
    /*
     * Internal method to handle paste element
     */
    _pasteNode: function(element, copiedObj, copiedComponent) {
        var self = CustomBuilder.Builder;   
        
        var elementObj = $(element).data("data");
        var component = self.parseDataToComponent(elementObj);
        
        var temp = $(copiedComponent.builderTemplate.getPasteTemporaryNode(copiedObj, copiedComponent));
        
        var copiedParentContainerAttr = copiedComponent.builderTemplate.getParentContainerAttr(copiedObj, copiedComponent);
        if (component.builderTemplate.getParentDataHolder(elementObj, component) === copiedComponent.builderTemplate.getParentDataHolder(copiedObj, copiedComponent)
                && component.builderTemplate.getParentContainerAttr(elementObj, component) === copiedParentContainerAttr
                && !($(element).is("[data-cbuilder-"+copiedParentContainerAttr+"]") || $(element).find("[data-cbuilder-"+copiedParentContainerAttr+"]:eq(0)").length > 0)) {
            //paste as sibling
            var parent = $(element).parent().closest("[data-cbuilder-classname]");
            if ($(parent).length === 0) {
                parent = $(element).closest("body");
            }
            var parentDataArray = $(parent).data("data")[copiedComponent.builderTemplate.getParentDataHolder(copiedObj, copiedComponent)];
            if (parentDataArray === undefined) {
                parentDataArray = [];
                $(parent).data("data")[copiedComponent.builderTemplate.getParentDataHolder(copiedObj, copiedComponent)] = parentDataArray;
            }
            var newIndex = $.inArray(elementObj, parentDataArray) + 1;
            parentDataArray.splice(newIndex, 0, copiedObj);

            $(element).after(temp);
        } else {
            //paste as child
            var parentDataArray = $(element).data("data")[copiedComponent.builderTemplate.getParentDataHolder(copiedObj, copiedComponent)];
            if (parentDataArray === undefined) {
                parentDataArray = [];
                $(element).data("data")[copiedComponent.builderTemplate.getParentDataHolder(copiedObj, copiedComponent)] = parentDataArray;
            }
            parentDataArray.push(copiedObj);
            
            var container = null;
            if ($(element).is("[data-cbuilder-"+copiedParentContainerAttr+"]")) {
                container = $(element);
            } else {
                container = $(element).find("[data-cbuilder-"+copiedParentContainerAttr+"]:eq(0)");
            }
            
            if (container.length > 0) {
                $(container).append(temp);
            }
        }
        
        self.component = copiedComponent;
        self.renderElement(copiedObj, temp, copiedComponent, true, null, function(element){
            if ($(element).find("[data-cbuilder-classname]").length > 0) {
                self.recursiveCheckVisible(element);
            }
        });
    },
    
    /*
     * Delete a selected node
     */
    deleteNode: function(node) {
        var self = CustomBuilder.Builder;
        
        if (node === undefined) {
            node = $(self.selectedEl);
        }
    
        $("#element-select-box").hide();
        var elementObj = $(node).data("data");
        var component = self.parseDataToComponent(elementObj);

        var parent = $(node).parent().closest("[data-cbuilder-classname]");
        if (parent.length === 0) {
            parent = $(node).closest("body");
        }
        
        var parentDataHolder = component.builderTemplate.getParentDataHolder(elementObj, component);
        
        if (parentDataHolder === null) { //if it is missing plugin, this will return null. getting it from parent element
            var parentObj = $(parent).data("data");
            if (parentObj) {
                var parentComponent = self.parseDataToComponent(parentObj);
                parentDataHolder = parentComponent.builderTemplate.getChildsDataHolder(parentObj, parentComponent);
            } else {
                parentDataHolder = component.builderTemplate.parentDataHolder; //default back to "elements"
            }
        }
        
        var parentDataArray = $(parent).data("data")[parentDataHolder];
        if (parentDataArray === undefined) {
            parentDataArray = [];
            $(parent).data("data")[parentDataHolder] = parentDataArray;
        }
        var index = $.inArray($(node).data("data"), parentDataArray);
        if (index !== -1) {
            parentDataArray.splice(index, 1);
        }

        if (component.builderTemplate.unload)
            component.builderTemplate.unload($(node), elementObj, component);

        $(node).remove();
        self.selectNode(false);

        if (component.builderTemplate.afterRemoved)
            component.builderTemplate.afterRemoved(parent, elementObj, component);

        self.checkVisible(parent);

        CustomBuilder.update();
        self.triggerChange();
    },
    
    /*
     * Select first element in canvas
     */
    selectFirst:  function() {
        var self = CustomBuilder.Builder;
        var first = self.frameBody.find('[data-cbuilder-classname]:not([data-cbuilder-uneditable])').first();
        if (first) {
            self.selectNode(first, false);
        }
    },
    
    /*
     * Select an element in canvas
     */
    selectNode:  function(node, dragging) {
        CustomBuilder.Builder.highlightEl = node;
        CustomBuilder.Builder.selectNodeAndShowProperties(node, dragging, true);
    },
    
    selectNodeAndShowProperties: function(node, dragging, show) {
        var self = CustomBuilder.Builder;
        
        self.frameBody.find('[data-cbuilder-inlineedit]').each(function(){
            try {
                self.iframe.contentWindow.tinymce.get($(this).attr("id")).destroy();
                $(this).removeAttr('data-cbuilder-inlineedit');
                if ($(this).is('[data-cbuilder-style-id]')) {
                    var style = $(this).next('[data-cbuilder-style]');
                    $(this).append(style);
                    $(this).removeAttr("data-cbuilder-style-id");
                }
                $(this).off("change.inlineedit");
            }catch(err){}
        });
        
        if (show === undefined) {
            show = true;
        }
        if (!node || $(node).is('[data-cbuilder-uneditable]'))
        {
            self.selectedEl = null;
            self.subSelectedEl = null;
            self.selectedElData = null;
            $("#element-select-box").hide();
            
            if ($("body").hasClass("property-editor-right-panel")) {
                $("body").addClass("no-right-panel");
                $("#right-panel .property-editor-container").remove();
                $("#copy-element-btn").addClass("disabled");
            }

            if (!node) {
                node = self.frameBody.find('[data-cbuilder-uneditable]').first();
            }

            if ($(node).length > 0) {
                $("#paste-element-btn").addClass("disabled");
                var data = node.data("data");
                var component = self.parseDataToComponent(data);
                if (component !== null && component.builderTemplate.isPastable(data, component)) {
                    $("#paste-element-btn").removeClass("disabled");
                }
            }
                
            return;
        }

        var target = $(node);
        var isSubSelect = false;
        if ($(node).is('[data-cbuilder-subelement]')) {
            target = $(node).parent().closest("[data-cbuilder-classname]");
            node = $(target);
        }
        if ($(node).is('[data-cbuilder-select]')) {
            var id = $(node).data('cbuilder-select');
            target = self.frameBody.find('[data-cbuilder-id="'+id+'"]');
            self.subSelectedEl = $(node);
            isSubSelect = true;
        } else {
            self.subSelectedEl = null;
        }
        if (!target.is(":visible")) {
            var id = $(node).data('cbuilder-id');
            if (self.frameBody.find('[data-cbuilder-select="'+id+'"]:visible').length > 0) {
                node = self.frameBody.find('[data-cbuilder-select="'+id+'"]:visible');
            }
        }
        if (target)
        {
            if (self.frameBody.hasClass("show-node-details-single")) {
                self.frameBody.find(".cbuilder-node-details.current").removeClass("current");
                $(target).find("> .cbuilder-node-details").addClass("current");
                self.adjustNodeAdditional(target);
                self.triggerEvent("nodeAdditionalSelected");
            }
            
            self.selectedEl = target;
            var data = target.data("data");
            self.selectedElData = data;
            var component = self.parseDataToComponent(data);
            self.component = component;
            
            if (dragging) {
                return;
            }
            if (component === null || component.builderTemplate === undefined) {
                self.component = undefined;
                return;
            }
            
            try {
                var box = self.getBox(node);
                $("#element-parent-box").hide();
                
                $("#element-select-box").removeClass("missing_component");
                if ($(node).is("[data-cbuilder-missing-plugin]")) {
                    $("#element-select-box").addClass("missing_component");
                } 
                
                $("#element-select-box").css(
                    {
                        "top": box.top - self.frameDoc.scrollTop(),
                        "left": box.left - self.frameDoc.scrollLeft(),
                        "width": box.width,
                        "height": box.height,
                        "display": "block"
                    });
                
                $("#element-select-box #element-select-name").attr("style", "");
                $("#element-select-name .element-name").html(this._getElementType(data, component));
                
                var offset = $("#element-select-box #element-select-name").offset();
                if (offset.top <= 55) {
                    $("#element-select-box #element-select-name").css("top", "0px");
                } else {
                    $("#element-select-box #element-select-name").css("top", "");
                }
                
                if (!isSubSelect || (isSubSelect && component.builderTemplate.isSubSelectAllowActions(data, component))) {
                    self.decorateBoxActions(node, data, component, $("#element-select-box"), box);
                } else {
                    $("#element-select-box .element-actions").hide();
                }
                
                if (show) {
                    self._showPropertiesPanel(target, data, component);
                    
                    if (component.builderTemplate.getInlineEditor() !== undefined && component.builderTemplate.getInlineEditor() !== null) {
                        var inlineEditor = component.builderTemplate.getInlineEditor();
                        var inilineEditEl = $(target);
                        
                        var inlineSelector = inlineEditor.selector;
                        if (inlineSelector !== undefined && inlineSelector !== null && inlineSelector !== "") {
                            if ($(target).find(inlineSelector).length > 0) {
                                inilineEditEl = $(target).find(inlineSelector);
                            } else {
                                inilineEditEl = null;
                            }
                        }
                        
                        if (inilineEditEl !== null) {
                            $(target).attr('data-cbuilder-inlineedit-element', '');
                            var inlineid = $(inilineEditEl).attr("id");
                            if (inlineid === undefined || inlineid === null || inlineid === "") {
                                inlineid = "inline_" + CustomBuilder.uuid();
                                $(inilineEditEl).attr("id", inlineid);
                            }
                            $(inilineEditEl).attr('data-cbuilder-inlineedit', '');
                            $(inilineEditEl).removeAttr('data-cbuilder-invisible');
                            $(inilineEditEl).removeAttr('data-cbuilder-element-invisible');
                            $(inilineEditEl).removeAttr('data-cbuilder-desktop-invisible');
                            $(inilineEditEl).removeAttr('data-cbuilder-tablet-invisible');
                            $(inilineEditEl).removeAttr('data-cbuilder-mobile-invisible');
                            
                            if ($(inilineEditEl).find("style[data-cbuilder-style]").length > 0) {
                                $(inilineEditEl).attr('data-cbuilder-style-id', $(inilineEditEl).find("style[data-cbuilder-style]").attr("data-cbuilder-style"));
                                $(inilineEditEl).after($(inilineEditEl).find("style[data-cbuilder-style]"));
                            }
                            
                            try {
                                var toolbar = 'styles | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image | removeformat';
                                if (inlineEditor.mode === "full") {
                                    toolbar = 'styles | fontfamily fontsize forecolor backcolor | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image | removeformat';
                                } else if (inlineEditor.mode === "simple") {
                                    toolbar = 'bold italic | alignleft aligncenter alignright alignjustify | removeformat';
                                }
                                setTimeout(function(){
                                    //find propety field
                                    var pfield = $("#element-properties-tab [property-name='"+inlineEditor.property+"']");
                                    self.iframe.contentWindow.tinymce.init({
                                        selector: '#' + inlineid,
                                        inline: true,
                                        image_advtab: true,
                                        relative_urls: false,
                                        convert_urls: false,
                                        extended_valid_elements:"style,link[href|rel]",
                                        custom_elements:"style,link,~link",
                                        valid_elements: '*[*]',
                                        plugins: 'advlist autolink lists link image charmap preview anchor pagebreak searchreplace wordcount visualblocks visualchars code fullscreen insertdatetime media nonbreaking table directionality emoticons codesample',
                                        toolbar: toolbar,
                                        menubar: false,
                                        promotion: false,
                                        init_instance_callback: function(editor) {
                                            editor.focus();

                                            //copy the value from property editor
                                            var value = "";
                                            if ($(pfield).hasClass('property-type-codeeditor')) {
                                                value = ace.edit($(pfield).find('pre.ace_editor').attr('id')).getValue();
                                            } else if ($(pfield).hasClass('property-type-htmleditor')) {
                                                value = tinymce.get($(pfield).find('textarea').attr('id')).getContent();
                                            } else {
                                                value = $(pfield).find('textarea, input').val();
                                            }
                                            editor.setContent(value);

                                            $(inilineEditEl).off('change.inlineedit keyup.inlineedit mousemouseup.inlineedit touchend.inlineedit');
                                            $(inilineEditEl).on('change.inlineedit keyup.inlineedit mousemouseup.inlineedit touchend.inlineedit', function(e) {
                                                self._inlineEditChnaged(target, pfield, editor);
                                            });

                                            editor.on('ExecCommand', function(e) {
                                                self._inlineEditChnaged(target, pfield, editor);
                                            });

                                            $(pfield).find('textarea[name], pre[name]').off("change.inlineedit keyup.inlineedit");
                                            $(pfield).find('textarea[name], pre[name]').on("change.inlineedit keyup.inlineedit", function(){
                                                if (!$(pfield).hasClass("syncPropValue")) {
                                                    $(pfield).addClass("syncInlineValue");
                                                    var value = "";
                                                    if ($(pfield).hasClass('property-type-codeeditor')) {
                                                        value = ace.edit($(pfield).find('pre.ace_editor').attr('id')).getValue();
                                                    } else if ($(pfield).hasClass('property-type-htmleditor')) {
                                                        value = tinymce.get($(pfield).find('textarea').attr('id')).getContent();
                                                    } else {
                                                        value = $(pfield).find('textarea, input').val();
                                                    }
                                                    editor.setContent(value);
                                                    $(pfield).removeClass("syncInlineValue");
                                                    self._updateBoxes();
                                                }
                                            });

                                            if ($(pfield).hasClass('property-type-htmleditor')) {
                                                tinymce.get($(pfield).find('textarea').attr('id')).on('ExecCommand', function(e) {
                                                    if (!$(pfield).hasClass("syncPropValue")) {
                                                        $(pfield).addClass("syncInlineValue");
                                                        var value = tinymce.get($(pfield).find('textarea').attr('id')).getContent();
                                                        editor.setContent(value);
                                                        $(pfield).removeClass("syncInlineValue");
                                                        self._updateBoxes();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }, 200);
                            } catch (err) {}
                        }
                    }
                }
                
                if (component.builderTemplate.selectNode)
                    component.builderTemplate.selectNode(target, data, component);
                
                $("#element-highlight-box").hide();
                self.highlightEl = null;
                
                self.selectedEl.trigger("builder.selected");
            } catch (err) {
                console.log(err);
                return false;
            }
        }
    },
    
    _inlineEditChnaged : function(target, field, editor) {
        var self = CustomBuilder.Builder;
        if (!$(field).hasClass("syncInlineValue")) {
            var content = editor.getContent();
            $(field).addClass("syncPropValue");

            if ($(field).hasClass('property-type-codeeditor')) {
                ace.edit($(field).find('pre.ace_editor').attr('id')).setValue(content);
            } else if ($(field).hasClass('property-type-htmleditor')) {
                tinymce.get($(field).find('textarea').attr('id')).setContent(content);
            } else {
                $(field).find('textarea, input').val(content);
            }

            $(field).removeClass("syncPropValue");
            self._updateBoxes();
        }
    },
    
    /*
     * show the Properties Panel of a node
     */
    _showPropertiesPanel : function(target, data, component) {
        var self = CustomBuilder.Builder;
        
        if ($("body").hasClass("property-editor-right-panel") && !$("body").hasClass("disable-select-edit")) {
            var supportProps = component.builderTemplate.isSupportProperties(data, component);
            var supportStyle = component.builderTemplate.isSupportStyle(data, component);

            if (supportProps || supportStyle) {
                $("body").removeClass("no-right-panel");

                var elementPropertiesHidden = false;

                if (supportProps) {
                    var props = self.parseElementProps(data);
                    var className = data.className;
                    if (className === undefined) {
                        className = self.selectedEl.data("cbuilder-classname");
                    }
                    if (component.builderTemplate.customPropertiesData) {
                        props = component.builderTemplate.customPropertiesData(props, data, component);
                    }
                    CustomBuilder.editProperties(className, props, data, target);
                } else {
                    elementPropertiesHidden = true;
                    $("#element-properties-tab-link").hide();
                    $("#right-panel #element-properties-tab").find(".property-editor-container").remove();
                }

                if (supportStyle) {
                    var props = self.parseElementProps(data);
                    var className = data.className;
                    if (className === undefined) {
                        className = self.selectedEl.data("cbuilder-classname");
                    }
                    if (component.builderTemplate.customPropertiesData) {
                        props = component.builderTemplate.customPropertiesData(props, data, component);
                    }

                    self.editStyles(props, target, data, component);
                } else {
                    $("#style-properties-tab-link").hide();
                    $("#right-panel #style-properties-tab").find(".property-editor-container").remove();
                }

                if (elementPropertiesHidden) {
                    $("#style-properties-tab-link a").trigger("click");
                } else if (!supportStyle) {
                    $("#element-properties-tab-link a").trigger("click");
                }
            } else {
                $("body").addClass("no-right-panel");
            }
        }
    },
    
    /*
     * Init the canvas highlight event
     * It handle the drag and drop moving as well
     */
    _initHighlight: function () {

        var self = CustomBuilder.Builder;

        self.frameHtml.off("mousemove.builder touchmove.builder");
        self.frameHtml.on("mousemove.builder touchmove.builder", function (event, parentEvent) {
            var x = 0;
            var y = 0;
            
            var eventTarget = $(event.target);
            
            if (self.isDragging && self.dragElement) {
                self.frameBody.find('[data-cbuilder-inlineedit]').each(function(){
                    try {
                        self.iframe.contentWindow.tinymce.get($(this).attr("id")).destroy();
                        $(this).removeAttr('data-cbuilder-inlineedit');
                        $(this).off("change.inlineedit");
                    }catch(err){}
                });
            }
                
            if (event.type === "touchmove") {
                if (event.touches === undefined) {
                    var frameOffset = $(self.iframe).offset();
                    x = parentEvent.touches[0].clientX;
                    y = parentEvent.touches[0].clientY;
                    if (parentEvent.touches[0].originalEvent) {
                        x = parentEvent.touches[0].originalEvent.clientX;
                        y = parentEvent.touches[0].originalEvent.clientY;
                    }
                    x = x - frameOffset.left;
                    y = y - frameOffset.top;
                } else {
                    x = event.touches[0].clientX;
                    y = event.touches[0].clientY;
                    if (event.touches[0].originalEvent) {
                        x = event.touches[0].originalEvent.clientX;
                        y = event.touches[0].originalEvent.clientY;
                    }
                }
                
                eventTarget = self.iframe.contentWindow.document.elementFromPoint(x, y);
            } else {
                x = event.clientX;
                y = event.clientY;
                if (event.originalEvent) {
                    x = event.originalEvent.clientX;
                    y = event.originalEvent.clientY;
                }
            }
            var target = $(eventTarget);
            if (CustomBuilder.Builder.isInlineEditing(target)) {
                return true;
            }
            
            if ($(target).closest(".ui-draggable-handle").length > 0) {
                return;
            }
            
            var isAlternativeDrop = false;
            if ($(target).closest("[data-cbuilder-alternative-drop]").length > 0) {
                isAlternativeDrop = true;
                if (!$(target).is("[data-cbuilder-select]")) {
                    target = $(eventTarget).closest("[data-cbuilder-select]");
                }
            } else {
                if (!$(target).is("[data-cbuilder-classname]")) {
                    target = $(eventTarget).closest("[data-cbuilder-classname]");
                }
                if ($(target).length === 0 && self.component !== undefined) {
                    target = $(eventTarget).closest("body[data-cbuilder-"+self.component.builderTemplate.getParentContainerAttr(self.data, self.component)+"]");
                }
            }
            if ($(target).length > 0)
            {
                if (self.isDragging && self.dragElement)
                {
                    if (self.elementPosX !== x && self.elementPosY !== y) {
                        self.isMoved = true;
                    }
                    self.elementPosX = x;
                    self.elementPosY = y;
                    
                    self.dragElement.attr("data-cbuilder-dragelement", "true");
                    self.frameBody.addClass("is-dragging");
                    
                    try {
                        $("#element-highlight-box").hide();
                        var parentContainerAttr = self.component.builderTemplate.getParentContainerAttr(self.data, self.component);

                        if (self.component.builderTemplate.isAbsolutePosition(self.data, self.component)) {
                            var selement = self.getElementsOnPosition(x, y, "[data-cbuilder-"+parentContainerAttr+"]");
                            if (selement !== null && selement.length > 0) {
                                var elementsContainer = $(selement);
                                if ($(selement).is('[data-cbuilder-alternative-drop]')) {
                                    target = $(selement).closest("[data-cbuilder-select]");
                                } else {
                                    target = $(selement).closest("[data-cbuilder-classname]");
                                }
                                self.highlightParent($(elementsContainer), event);
                                if (elementsContainer.find(self.dragElement).length === 0) {
                                    elementsContainer.append(self.dragElement);
                                }
                                var cursorPos = self.dragElement.data("cursorPosition");
                                if (!cursorPos) {
                                    cursorPos = {x: 10, y : 10};
                                }

                                var containerOffset = elementsContainer.offset();
                                var x_offset = ((x - containerOffset.left) / self.zoom) - cursorPos.x;
                                var y_offset = ((y - containerOffset.top) /self.zoom) - cursorPos.y;

                                self.dragElement.css({
                                   "top" : y_offset + "px",
                                   "left" : x_offset + "px",
                                   "position" : "absolute"
                                });
                            } else {
                                return;
                            }
                        } else {
                            var elementsContainer = $(eventTarget).closest("[data-cbuilder-"+parentContainerAttr+"]");
                            
                            if ($(eventTarget).is("[data-cbuilder-ignore-dragging]").length > 0 || $(eventTarget).closest("[data-cbuilder-ignore-dragging]").length > 0) {
                                var selement = self.getElementsOnPosition(x, y, "[data-cbuilder-"+parentContainerAttr+"]");
                                if (selement.length > 0) {
                                    elementsContainer = $(selement);
                                    if ($(selement).is('[data-cbuilder-alternative-drop]')) {
                                        target = $(selement).closest("[data-cbuilder-select]");
                                    } else {
                                        target = $(selement).closest("[data-cbuilder-classname]");
                                    }
                                } else {
                                    return;
                                }
                            }
                            if ($(elementsContainer).closest('[data-cbuilder-classname]').is(self.dragElement) || $(self.dragElement).find($(elementsContainer)).length > 0) {
                                return;
                            }
                            if ($(target).parent().length > 0 && $(target).parent().is(elementsContainer)) {
                                self.highlightParent($(elementsContainer), event);
                                    
                                //not container
                                var offset = $(target).offset();
                                var top = offset.top - $(self.frameDoc).scrollTop();
                                var dY = (($(target).outerHeight() * self.zoom) / 4);
                                var left = offset.left - $(self.frameDoc).scrollLeft();
                                var dX = (($(target).outerWidth() * self.zoom) / 4);

                                if ($(target).parent().is("[data-cbuilder-sort-horizontal]")) {
                                    if (x < (left + dX*2)) {
                                        $(target).before(self.dragElement);
                                    } else { 
                                        $(target).after(self.dragElement);
                                    }
                                } else {
                                    if (y < (top + dY) || (y < (top + dY * 2) && x < (left + dX*3)) || (y < (top + dY * 3) && x < (left + dX))) {
                                        $(target).before(self.dragElement);
                                    } else { 
                                        $(target).after(self.dragElement);
                                    }
                                }
                            } else {
                                self.highlightParent($(elementsContainer), event);
                                
                                //is container
                                var childs = elementsContainer.find('> [data-cbuilder-classname]');
                                if (isAlternativeDrop) {
                                    childs = elementsContainer.find('> [data-cbuilder-select]:visible');
                                }
                                if (childs.length === 0 && $(elementsContainer).find('> [data-cbuilder-sample]').length === 1) {
                                    elementsContainer.prepend(self.dragElement);
                                } else if (childs.length > 0) {
                                    //when has childs, find child at x,y
                                    var child = null;
                                    var offset = null;
                                    var top = null;
                                    var left =  null;

                                    childs.each(function(){
                                        if (child === null) {
                                            offset = $(this).offset();
                                            top = offset.top - $(self.frameDoc).scrollTop();
                                            left = offset.left  - $(self.frameDoc).scrollLeft();

                                            if (y < top + ($(this).outerHeight() * self.zoom) && x < left + ($(this).outerWidth() * self.zoom)) {
                                                child = $(this);
                                            }
                                        }
                                    });
                                    
                                    if (child === null && elementsContainer.is("[data-cbuilder-sort-horizontal]")) {
                                        var lastChild = childs[childs.length -1];
                                        offset = $(lastChild).offset();
                                        left = offset.left  - $(self.frameDoc).scrollLeft();
                                        if (x > left + ($(lastChild).outerWidth() * self.zoom)) {
                                            child = $(lastChild);
                                        }
                                    }

                                    if (child !== null) {
                                        var dY = ((child.outerHeight() * self.zoom) / 4);
                                        var dX = ((child.outerWidth() * self.zoom) / 4);

                                        if (elementsContainer.is("[data-cbuilder-sort-horizontal]")) {
                                            if (x < (left + dX*2)) {
                                                child.before(self.dragElement);
                                            } else { 
                                                child.after(self.dragElement);
                                            }
                                        } else {
                                            if (y < (top + dY) || (y < (top + dY * 2) && x < (left + dX*3)) || (y < (top + dY * 3) && x < (left + dX))) {
                                                child.before(self.dragElement);
                                            } else { 
                                                child.after(self.dragElement);
                                            }
                                        }
                                    } else {
                                        if (elementsContainer.is('[data-cbuilder-prepend]') || elementsContainer.is('[data-cbuilder-single]')) {
                                            elementsContainer.prepend(self.dragElement);
                                        } else {
                                            elementsContainer.append(self.dragElement);
                                        }
                                    }
                                } else {
                                    //when empty
                                    if (elementsContainer.is('[data-cbuilder-prepend]') || elementsContainer.is('[data-cbuilder-single]')) {
                                        elementsContainer.prepend(self.dragElement);
                                    } else {
                                        elementsContainer.append(self.dragElement);
                                    }
                                }
                            }
                        }
                        if (self.component.builderTemplate.dragging) {
                            self.dragElement = self.component.builderTemplate.dragging(self.dragElement, self.component);
                            self.dragElement.attr("data-cbuilder-dragelement", "true");
                        }
                        
                        if (self.dragElement.closest('[data-cbuilder-replicate-origin]').length > 0) {
                            var replicate = self.dragElement.closest('[data-cbuilder-replicate-origin]');
                            var replicateHtml = $(replicate).html();
                            var replicateKey = $(replicate).attr("data-cbuilder-replicate-origin");
                            $(replicate).parent().find('[data-cbuilder-replicate="'+replicateKey+'"]').html(replicateHtml);
                            $(replicate).parent().find('[data-cbuilder-replicate="'+replicateKey+'"] [data-cbuilder-classname]').removeAttr('data-cbuilder-classname');
                        }
                        
                        var dragOffset = $(self.dragElement).offset();
                        var frameX = x + $(self.frameDoc).scrollLeft();
                        var frameY = y + $(self.frameDoc).scrollTop();
                        var scrollHMin = $(self.frameDoc).scrollLeft();
                        var scrollHMax = scrollHMin + $(self.iframe).width();
                        var dragLeft = dragOffset.left;
                        var dragRight = dragOffset.left + $(self.dragElement).width();
                        var scrollVMin = $(self.frameDoc).scrollTop();
                        var scrollVMax = scrollVMin + $(self.iframe).height();
                        var dragTop = dragOffset.top;
                        var dragBottom = dragOffset.top + $(self.dragElement).height();
                        
                        var followCursor = false;
                        if (frameY < dragTop && frameY > scrollVMin && frameY < scrollVMin + 50) {
                            //check if the cursor on the top edge of the screen and the drag element can't follow
                            $(self.frameDoc).scrollTop(frameY - 30);
                            followCursor = true;
                        }
                        if (frameY > dragBottom && frameY < scrollVMax && frameY > scrollVMax - 50) {
                            //check if the cursor on the bottom edge of the screen and the drag element can't follow
                            $(self.frameDoc).scrollTop(frameY + 30);
                            followCursor = true;
                        }
                        if (frameX < dragLeft && frameX > scrollHMin && frameX < scrollHMin + 50) {
                            //check if the cursor on the left edge of the screen and the drag element can't follow
                            $(self.frameDoc).scrollLeft(frameX - 30);
                            followCursor = true;
                        }
                        if (frameX > dragRight && frameX < scrollHMax && frameX > scrollHMax - 50) {
                            //check if the cursor on the right edge of the screen and the drag element can't follow
                            $(self.frameDoc).scrollLeft(frameX + 30);
                            followCursor = true;
                        }
                        
                        if (!followCursor) {
                            //check if the drag element is visible in the canvas
                            if (dragLeft < scrollHMin) {
                                $(self.frameDoc).scrollLeft(dragLeft);
                            }
                            if (dragRight > scrollHMax) {
                                $(self.frameDoc).scrollLeft(scrollHMin + (dragRight - scrollHMax) + $(self.dragElement).width());
                            }
                            if (dragTop < scrollVMin) {
                                $(self.frameDoc).scrollTop(dragTop);
                            }
                            if (dragBottom > scrollVMax) {
                                $(self.frameDoc).scrollTop(scrollVMin + (dragBottom - scrollHMax) + $(self.dragElement).height());
                            }
                        }
                      
                        if (self.iconDrag)
                            self.iconDrag.css({'left': x + 238, 'top': y + 20});
                    } catch (err) {
                        console.log(err);
                        return false;
                    }
                } else if (!$(target).is(self.frameBody))
                {
                    $("#element-parent-box").hide();
                    self.highlight($(target), event);
                } else {
                    $("#element-parent-box, #element-highlight-box").hide();
                }
            }
        });

        self.frameHtml.off("mouseup.builder touchend.builder");
        self.frameHtml.on("mouseup.builder touchend.builder", function (event) {
            self.mousedown = false;
            var target = $(event.target);
            if (CustomBuilder.Builder.isInlineEditing(target)) {
                return true;
            }
            if (self.isDragging)
            {
                self.isDragging = false;
                self.frameBody.removeClass("is-dragging");
                self.frameBody.find("[data-cbuilder-droparea]").removeAttr("data-cbuilder-droparea");
                
                if (self.iconDrag) {
                    self.iconDrag.remove();
                    self.iconDrag = null;
                }
                self.handleDropEnd();
            }
        });
        
        self.frameHtml.off("mousedown.builder touchstart.builder");
        self.frameHtml.on("mousedown.builder touchstart.builder", function (event) {
            self.mousedown = true;
            var target = $(event.target);
            if (CustomBuilder.Builder.isInlineEditing(target)) {
                self.mousedown = false;
                return true;
            }
            if (!$(target).is("[data-cbuilder-classname]")) {
                target = $(event.target).closest("[data-cbuilder-classname]");
            }
            if ($(event.target).closest('[data-cbuilder-classname], [data-cbuilder-select]').is('[data-cbuilder-select]')) {
                target = $(event.target).closest("[data-cbuilder-select]");
            }
            if ($(event.target).is("[data-cbuilder-subelement]")) {
                target = $(event.target).parent().closest("[data-cbuilder-classname]");
            }
            if ($(target).is("[data-cbuilder-unselectable]")) {
                CustomBuilder.checkChangeBeforeCloseElementProperties(function(hasChange) {
                    self.selectNode(false);
                });
                self.mousedown = false;
                return false;
            }
            if ($(target).length > 0)
            {
                try {
                    CustomBuilder.checkChangeBeforeCloseElementProperties(function(hasChange) {
                        if (hasChange) {
                            self.mousedown = false;
                        }
                        if (self.mousedown) {
                            self.selectNode(target, true);
                            if (self.selectedElData && self.component.builderTemplate.isDraggable(self.selectedElData, self.component)) {
                                $("#element-select-box").hide();
                                if (self.subSelectedEl){
                                    self.dragElement = self.subSelectedEl;
                                } else {
                                    self.dragElement = self.selectedEl;
                                }
                                self.isDragging = true;
                                self.isMoved = false;
                                self.currentParent = self.selectedEl.parent().closest("[data-cbuilder-classname]");
                                self.currentParentDataHolder = self.component.builderTemplate.getParentDataHolder(self.selectedElData, self.component);
                                self.data = self.selectedElData;
                                
                                var x = 0;
                                var y = 0;
                                if (event.type === "touchstart") {
                                    x = event.touches[0].clientX;
                                    y = event.touches[0].clientY;
                                    if (event.touches[0].originalEvent) {
                                        x = event.touches[0].originalEvent.clientX;
                                        y = event.touches[0].originalEvent.clientY;
                                    }
                                } else {
                                    x = event.clientX;
                                    y = event.clientY;
                                    if (event.originalEvent) {
                                        x = event.originalEvent.clientX;
                                        y = event.originalEvent.clientY;
                                    }
                                }
                                self.elementPosX = x;
                                self.elementPosY = y;

                                if (self.component.builderTemplate.dragStart)
                                    self.dragElement = self.component.builderTemplate.dragStart(self.dragElement, self.component);

                                if (self.component.builderTemplate.isAbsolutePosition(self.data, self.component)) {
                                    var elementOffset = self.dragElement.offset();
                                    var xDiff = x - elementOffset.left;
                                    var yDiff = y - elementOffset.top;
                                    self.dragElement.data("cursorPosition", {"x" : xDiff, "y" : yDiff});
                                }    

                                self.frameBody.find("[data-cbuilder-"+self.component.builderTemplate.getParentContainerAttr(self.data, self.component)+"]").attr("data-cbuilder-droparea", "");
                            } else {
                                self.selectNode(target);
                            }
                        } else {
                            var data = $(target).data("data");
                            if (data !== undefined) {
                                self.selectNode(target);
                            } else if(self.selectedEl) { //the clicked element is child element of previous editing element
                                target = $(self.selectedEl).find('[data-cbuilder-id="'+$(target).attr("data-cbuilder-id")+'"]');
                                self.selectNode(target);
                            }
                        }
                    });
                }catch (err){}
            } else {
                CustomBuilder.checkChangeBeforeCloseElementProperties(function(hasChange) {
                    self.selectNode(false);
                });
            }
            return false;
        });
        
        self.frameHtml.off("click.builder");
        self.frameHtml.on("click.builder", function (event) {
            var target = $(event.target);
            if (CustomBuilder.Builder.isInlineEditing(target)) {
                return true;
            }
            if (!$(target).is("[data-cbuilder-classname]")) {
                target = $(event.target).closest("[data-cbuilder-classname]");
            }
            if ($(target).is("[data-cbuilder-unselectable]")) {
                return false;
            }
            if ($(event.target).closest('[data-cbuilder-classname], [data-cbuilder-select]').is('[data-cbuilder-select]')) {
                target = $(event.target).closest("[data-cbuilder-select]");
            }
            if ($(event.target).is("[data-cbuilder-subelement]")) {
                target = $(event.target).parent().closest("[data-cbuilder-classname]");
            }
            if ($(target).length > 0)
            {
                event.stopPropagation();
                event.stopImmediatePropagation();
            }
            event.preventDefault();
            return false;    
        });
    },
    
    /*
     * Used to check the current event target is from the inline editor
     */
    isInlineEditing : function(target) {
        return $(target).find("> .mce-edit-focus").length > 0  //inline editing is focused
                || $(target).closest('.mce-content-body[contenteditable]').length > 0 //the event target is within the inline editor
                || $(target).closest('.tox-tinymce').length > 0 //the event target is toolbar
                || $(target).closest('.tox-tiered-menu').length > 0 // the event target is toolbar menu
                || $(target).closest('.tox-dialog').length > 0 // the event target is form dialog box for html editor
                || $(target).closest('.tox-tbtn').length > 0; // the event target is additional toolbar menu for rich text
    },
    
    /*
     * Highlight an element parent in canvas
     */
    highlightParent : function(target, event) {
        var self = CustomBuilder.Builder;
        
        if ($(event.target).closest('[data-cbuilder-classname], [data-cbuilder-select]').is('[data-cbuilder-select]')) {
            target = $(event.target).closest('[data-cbuilder-select]');
        }
        if (!$(target).is('[data-cbuilder-classname]')) {
            target = $(target).closest('[data-cbuilder-classname]');
        }
        if ($(target).length > 0 && !$(target).is(self.frameBody) && !$(target).is('[data-cbuilder-uneditable]')) {
            var box = self.getBox(target);
            
            $("#element-parent-box").css(
                    {"top": box.top - self.frameDoc.scrollTop(),
                        "left": box.left - self.frameDoc.scrollLeft(),
                        "width": box.width,
                        "height": box.height,
                        "display": event.target.hasAttribute('contenteditable') ? "none" : "block"
                    });

            var nameOffset = $("#element-parent-box").offset();
            if (nameOffset.top <= 76) {
                $("#element-parent-name").css("top", "0px");
            } else {
                $("#element-parent-name").css("top", "");
            }
            
            var data = target.data("data");
            if (data === undefined && $(target).is('[data-cbuilder-select]')) {
                var id = $(target).attr('data-cbuilder-select');
                data = self.frameBody.find('[data-cbuilder-id="'+id+'"]').data("data");
            }
            if (data !== undefined) {
                var component = self.parseDataToComponent(data);
                $("#element-parent-name").html(self._getElementType(data, component));
            } else {
                $("#element-parent-box").hide();
            }
        } else {
            $("#element-parent-box").hide();
        }
    },
    
    /*
     * Highlight an element in canvas
     */
    highlight : function(target, event) {
        var self = CustomBuilder.Builder;
        
        if ($(event.target).closest('[data-cbuilder-classname], [data-cbuilder-select]').is('[data-cbuilder-select]')) {
            target = $(event.target).closest('[data-cbuilder-select]');
        }
        if ($(event.target).closest("[data-cbuilder-classname]").is("[data-cbuilder-subelement]")) {
            target = $(event.target).closest("[data-cbuilder-classname]").parent().closest("[data-cbuilder-classname]");
        }
        
        if ($(target).length > 0 && !$(target).is(self.frameBody) && !$(target).is('[data-cbuilder-uneditable]')) {
            var data = target.data("data");
            if (data === undefined && $(target).is('[data-cbuilder-select]')) {
                var id = $(target).attr('data-cbuilder-select');
                data = self.frameBody.find('[data-cbuilder-id="'+id+'"]').data("data");
            }
            if (data !== undefined) {
                self.highlightEl = target;
                
                var box = self.getBox(target);
            
                $("#element-highlight-box").removeClass("missing_component");

                $("#element-highlight-box").css(
                        {"top": box.top - self.frameDoc.scrollTop(),
                            "left": box.left - self.frameDoc.scrollLeft(),
                            "width": box.width,
                            "height": box.height,
                            "display": event.target.hasAttribute('contenteditable') ? "none" : "block",
                            "border": self.isDragging ? "1px dashed aqua" : "", //when dragging highlight parent with green
                        });

                if ($(target).is("[data-cbuilder-missing-plugin]")) {
                    $("#element-highlight-box").addClass("missing_component");
                }
                
                var component = self.parseDataToComponent(data);
                
                $("#element-highlight-box .element-name").html(self._getElementType(data, component));
                
                var nameOffset = $("#element-highlight-box").offset();
                if (nameOffset.top <= 76) {
                    $("#element-highlight-name").css("top", "0px");
                } else {
                    $("#element-highlight-name").css("top", "");
                }
                
                self.decorateBoxActions(target, data, component, $("#element-highlight-box"), box);
            } else {
                $("#element-highlight-box").hide();
                self.highlightEl = null;
            }
        } else {
            $("#element-highlight-box").hide();
            self.highlightEl = null;
        }
    },
    
    /* 
     * decorate the highlight or select box of an element
     */
    decorateBoxActions : function(element, data, component, box, boxOffset) {
        var boxElement = $(box).data("element");
        if (boxElement === element) { //it is still same element, do need to do it again
            return;
        }
        
        var isSubSelect = $(element).is('[data-cbuilder-select]');
        
        //only enable/disable it for selected element, should not do it during hightlight/hover
        if (box.is("#element-select-box")) {
            $("#paste-element-btn").addClass("disabled");
            if (component.builderTemplate.isPastable(data, component)) {
                $("#paste-element-btn").removeClass("disabled");
            }
        
            $("#copy-element-btn").addClass("disabled");
            if (!isSubSelect && component.builderTemplate.isCopyable(data, component)) {
                $("#copy-element-btn").removeClass("disabled");
            }
        }

        $(box).find(".up-btn, .down-btn, .left-btn, .right-btn").hide();
        $(box).find(".element-name").removeClass("moveable");
        if (!isSubSelect && component.builderTemplate.isMovable(data, component)) {
            if ($(element).closest('[data-cbuilder-sort-horizontal]').length > 0) {
                $(box).find(".left-btn, .right-btn").show();
            } else {
                $(box).find(".up-btn, .down-btn").show();
            }
        }
        if (!isSubSelect && component.builderTemplate.isDraggable(data, component)) {
            $(box).find(".element-name").addClass("moveable");
        }

        $(box).find(".delete-btn").hide();
        if (!isSubSelect && component.builderTemplate.isDeletable(data, component)) {
            $(box).find(".delete-btn").show();
        }

        $(box).find(".parent-btn").hide();
        if (!isSubSelect && component.builderTemplate.isNavigable(data, component)) {
            $(box).find(".parent-btn").show();
        }
        $(box).find(".element-actions").show();

        $(box).find(".element-options").html("");
        $(box).find(".element-bottom-actions").html("");

        if (!isSubSelect && component.builderTemplate.decorateBoxActions)
            component.builderTemplate.decorateBoxActions(element, data, component, box);
        
        var nameWrapper = $(box).find("#element-highlight-name");
        if ($(box).is("#element-select-box")) {
            nameWrapper = $(box).find("#element-select-name");
        }
        
        $(nameWrapper).css("left", "unset");
        $(nameWrapper).css("right", "unset");

        var offset = $(nameWrapper).offset();
        var right = offset.left + $(nameWrapper).width();
        var frameRight = $("#iframe-wrapper").offset().left + $("#iframe-wrapper").width();
        if (right > frameRight) {
            if ((CustomBuilder.systemTheme === 'light' || CustomBuilder.systemTheme === 'dark')
                    && (CustomBuilder.builderType === 'datalist' || CustomBuilder.builderType === 'process') && nameWrapper[0].id === 'element-select-name') {
                // add 2 cause in light and dark mode theme the select border is 2px
                $(nameWrapper).css("right", ($(nameWrapper).width() - boxOffset.width + 2) + "px");
            } else {
                $(nameWrapper).css("right", ($(nameWrapper).width() - boxOffset.width + 1) + "px");
            }
            $(nameWrapper).css("left", "unset");
        } else {
            $(nameWrapper).css("right", "unset");
            if ((CustomBuilder.systemTheme === 'light' || CustomBuilder.systemTheme === 'dark')
                    && (CustomBuilder.builderType === 'datalist' || CustomBuilder.builderType === 'process') && nameWrapper[0].id === 'element-select-name') {
                // add 2 cause in light and dark mode theme the select border is 2px
                $(nameWrapper).css("left", "-2px");
            } else {
                $(nameWrapper).css("left", "-1px");
            }
        }
        
        $(box).data("element", element);
    },
    
    /*
     * set the selected element when highlight box action is clicked.
     */
    boxActionSetElement : function(event) {
        var self = CustomBuilder.Builder;
        
        $("#element-highlight-box, #element-select-box").hide();
        if ($(event.target).closest("#element-highlight-box").length > 0) {
            self.selectNodeAndShowProperties(self.highlightEl, false, false);
        }
    },
    
    /*
     * Initialize the select box buttons action
     */
    _initBox: function () {
        var self = this;
        
        $("body").off("mousedown.buildermove touchstart.buildermove", "#element-highlight-name .element-name.moveable, #element-select-name .element-name.moveable");
        $("body").on("mousedown.buildermove touchstart.buildermove", "#element-highlight-name .element-name.moveable, #element-select-name .element-name.moveable", function (event) {
            self.mousedown = true;
            try {
                CustomBuilder.checkChangeBeforeCloseElementProperties(function(hasChange) {
                    if (hasChange) {
                        self.mousedown = false;
                    }
                    if (self.mousedown) {
                        self.boxActionSetElement(event);
                        
                        if (self.component.builderTemplate.isDraggable(self.selectedElData, self.component)) {
                            $("#element-select-box").hide();
                            if (self.subSelectedEl){
                                self.dragElement = self.subSelectedEl;
                            } else {
                                self.dragElement = self.selectedEl;
                            }
                            self.isDragging = true;
                            self.isMoved = false;
                            self.currentParent = self.selectedEl.parent().closest("[data-cbuilder-classname]");
                            self.currentParentDataHolder = self.component.builderTemplate.getParentDataHolder(self.selectedElData, self.component);
                            self.data = self.selectedElData;

                            var x = 0;
                            var y = 0;
                            if (event.type === "touchstart") {
                                x = event.touches[0].clientX;
                                y = event.touches[0].clientY;
                                if (event.touches[0].originalEvent) {
                                    x = event.touches[0].originalEvent.clientX;
                                    y = event.touches[0].originalEvent.clientY;
                                }
                            } else {
                                x = event.clientX;
                                y = event.clientY;
                                if (event.originalEvent) {
                                    x = event.originalEvent.clientX;
                                    y = event.originalEvent.clientY;
                                }
                            }
                            self.elementPosX = x;
                            self.elementPosY = y;

                            if (self.component.builderTemplate.dragStart)
                                self.dragElement = self.component.builderTemplate.dragStart(self.dragElement, self.component); 

                            self.frameBody.find("[data-cbuilder-"+self.component.builderTemplate.getParentContainerAttr(self.data, self.component)+"]").attr("data-cbuilder-droparea", "");
                        }
                    }
                });
            }catch (err){}
                
            event.preventDefault();
            return false;
        });

        $(".element-actions .down-btn, .element-actions .right-btn").off("click");
        $(".element-actions .down-btn, .element-actions .right-btn").on("click", function (event) {
            self.boxActionSetElement(event);
            
            self.moveNodeDown();
            event.preventDefault();
            return false;
        });

        $(".element-actions .up-btn, .element-actions .left-btn").off("click");
        $(".element-actions .up-btn, .element-actions .left-btn").on("click", function (event) {
            self.boxActionSetElement(event);
            
            self.moveNodeUp();
            event.preventDefault();
            return false;
        });

        $(".element-actions .copy-btn").off("click");
        $(".element-actions .copy-btn").on("click", function (event) {
            self.boxActionSetElement(event);
            
            self.copyNode();
            event.preventDefault();
            return false;
        });

        $(".element-actions .paste-btn").off("click");
        $(".element-actions .paste-btn").on("click", function (event) {
            self.boxActionSetElement(event);
            
            self.pasteNode();
            event.preventDefault();
            return false;
        });

        $(".element-actions .parent-btn").off("click");
        $(".element-actions .parent-btn").on("click", function (event) {
            self.boxActionSetElement(event);
            
            var node = self.selectedEl.parent().closest("[data-cbuilder-classname]");
            self.selectNode(node);
            
            event.preventDefault();
            return false;
        });

        $(".element-actions .delete-btn").off("click");
        $(".element-actions .delete-btn").on("click", function (event) {
            self.boxActionSetElement(event);
            
            self.deleteNode();
            event.preventDefault();
            return false;
        });
    },

    /*
     * Create a dummy component for permission rules and plugins
     */
    _initPermissionComponent : function() {
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
                            title : get_advtool_msg('adv.tool.permission') + " (" + $(element).data("data").permission_name + ")",
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
        ], "", false, "", {builderTemplate: {}});
    },

    /*
     * Get builder component based on classname
     * Builder component are use to decide the bahavior of q component in canvas
     */
    getComponent : function(className) {
        var component = CustomBuilder.paletteElements[className];
        
        if (component === undefined) {
            component = CustomBuilder.Builder.missingComponent(className);
        }
        
        if (component.builderTemplate === undefined || component.builderTemplate.builderReady === undefined) {
            if (component.builderTemplate === undefined) {
                component.builderTemplate = {};
            }
            component.builderTemplate = $.extend(true, {
                'render' : function(element, elementObj, component, callback) {
                    if (CustomBuilder.Builder.options.callbacks["renderElement"] !== undefined && CustomBuilder.Builder.options.callbacks["renderElement"] !== "") {
                        CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["renderElement"], [element, elementObj, component, callback]);
                    } else if (callback) {
                        callback(element);
                    }
                },
                'unload' : function(element, elementObj, component) {
                    if (CustomBuilder.Builder.options.callbacks["unloadElement"] !== undefined && CustomBuilder.Builder.options.callbacks["unloadElement"] !== "") {
                        CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["unloadElement"], [element, elementObj, component]);
                    }
                },
                'selectNode' : function(element, elementObj, component) {
                    if (CustomBuilder.Builder.options.callbacks["selectElement"] !== undefined && CustomBuilder.Builder.options.callbacks["selectElement"] !== "") {
                        CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["selectElement"], [element, elementObj, component]);
                    }
                },
                'decorateBoxActions' : function(element, elementObj, component, box) {
                    if (CustomBuilder.Builder.options.callbacks["decorateBoxActions"] !== undefined && CustomBuilder.Builder.options.callbacks["decorateBoxActions"] !== "") {
                        CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["decorateBoxActions"], [element, elementObj, component, box]);
                    }
                },
                'getDragHtml' : function(elementObj, component) {
                    return this.dragHtml;
                },
                'getHtml' : function(elementObj, component) {
                    return this.html;
                },
                'getParentContainerAttr' : function(elementObj, component) {
                    return this.parentContainerAttr;
                },
                'getChildsContainerAttr' : function(elementObj, component) {
                    return this.childsContainerAttr;
                },
                'getParentDataHolder' : function(elementObj, component) {
                    return this.parentDataHolder;
                },
                'getChildsDataHolder' : function(elementObj, component) {
                    return this.childsDataHolder;
                },
                'getPasteTemporaryNode' : function(elementObj, component) {
                    return '<div></div>';
                },
                'getInlineEditor' : function(elementObj, component) {
                    return this.inlineEditor;
                },
                'isRenderNodeAdditional' : function(elementObj, component, type) {
                    return this.renderNodeAdditional;
                },
                'isSupportProperties' : function(elementObj, component) {
                    return this.supportProperties;
                },
                'isSupportStyle' : function(elementObj, component) {
                    return this.supportStyle;
                },
                'isDraggable' : function(elementObj, component) {
                    return this.draggable;
                },
                'isMovable' : function(elementObj, component) {
                    return this.movable;
                },
                'isDeletable' : function(elementObj, component) {
                    return this.deletable;
                },
                'isCopyable' : function(elementObj, component) {
                    return this.copyable;
                },
                'isNavigable' : function(elementObj, component) {
                    return this.navigable;
                },
                'isSubSelectAllowActions' : function(elementObj, component) {
                    return false;
                },
                'isPastable' : function(elementObj, component) {
                    var copied = CustomBuilder.getCopiedElement();
                    if (copied !== null && copied !== undefined) {
                        var copiedComponent = CustomBuilder.Builder.parseDataToComponent(copied.object);
                        if (copiedComponent !== null && copiedComponent !== undefined && component.builderTemplate.getChildsContainerAttr(elementObj, component) === copiedComponent.builderTemplate.getParentContainerAttr(copied.object, copiedComponent)) {
                            return true;
                        } else if (copiedComponent !== null && copiedComponent !== undefined && component.builderTemplate.getParentContainerAttr(elementObj, component) === copiedComponent.builderTemplate.getParentContainerAttr(copied.object, copiedComponent)) {
                            return true; //sibling
                        }
                    }
                    return false;
                },
                'isAbsolutePosition' : function(elementObj, component) {
                    return this.absolutePosition;
                },
                'isUneditable' : function(elementObj, component) {
                    return this.uneditable;
                },
                'getStylePropertiesDefinition' : function(elementObj, component) {
                    return this.stylePropertiesDefinition;
                },
                'updateProperties' : function(element, elementObj, component) {
                    
                },
                'parentContainerAttr' : 'elements',  //the html attr to locate the container of its parent
                'childsContainerAttr' : 'elements', //the html attr to locate the container of its childs
                'parentDataHolder' : 'elements', //the data attr of its parent element to store the current element
                'childsDataHolder' : 'elements', //the data attr of childs element
                'stylePropertiesDefinition' : CustomBuilder.Builder.stylePropertiesDefinition(component.builderTemplate.stylePrefix),
                'supportProperties' : true,
                'supportStyle' : true,
                'draggable' : true,
                'movable' : true,
                'deletable' : true,
                'copyable' : true,
                'navigable' : true,
                'absolutePosition' : false,
                'uneditable' : false,
                'renderNodeAdditional' : true,
                'builderReady' : true
            }, component.builderTemplate);
            
            if (CustomBuilder.Builder.options.callbacks !== undefined && CustomBuilder.Builder.options.callbacks["initComponent"] !== undefined && CustomBuilder.Builder.options.callbacks["initComponent"] !== "") {
                CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["initComponent"], [component]);
            }
        }
        
        return component;
    },
    
    missingComponent: function (className) {
        CustomBuilder.Builder.frameBody.find("[data-cbuilder-classname='"+className+"']").attr("data-cbuilder-missing-plugin", "");
        
        CustomBuilder.initPaletteElement("", className, get_advtool_msg('dependency.tree.Missing.Plugin') + " ("+className+")", "", "", "", false, "", {builderTemplate: {
            'render' : function(element, elementObj, component, callback) {
                var newcallback = function(element) {
                    $(element).attr("data-cbuilder-missing-plugin", "");
                    callback(element);
                };
                if (CustomBuilder.Builder.options.callbacks["renderElement"] !== undefined && CustomBuilder.Builder.options.callbacks["renderElement"] !== "") {
                    CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["renderElement"], [element, elementObj, component, newcallback]);
                } else if (callback) {
                    newcallback(element);
                }
            },
            'getParentDataHolder' : function(elementObj, component) {
                return null;
            },
            'supportProperties' : false,
            'supportStyle' : false,
            'draggable' : false,
            'movable' : false,
            'deletable' : true,
            'copyable' : false,
            'navigable' : true
        }});
        return CustomBuilder.paletteElements[className];
    },

    /*
     * Initialize the drap and drop of elements in pallete
     */
    _initDragdrop: function () {

        var self = CustomBuilder.Builder;
        self.isDragging = false;

        $('.drag-elements-sidepane').off("mousedown.builder touchstart.builder", "ul > li > ol > li > [element-class]");
        $('.drag-elements-sidepane').on("mousedown.builder touchstart.builder", "ul > li > ol > li > [element-class]", function (event) {
            $this = $(this);
            if (self.iconDrag) {
                self.iconDrag.remove();
                self.iconDrag = null;
            }
            $("#element-parent-box, #element-highlight-box").hide();
            self.selectNode(false);
            
            self.currentParent = null;
            self.currentParentDataHolder = null;
            self.component = self.getComponent($this.attr("element-class"));
            self.data = null;
            
            var html = null;
            if (self.component.builderTemplate.getDragHtml){
                html = self.component.builderTemplate.getDragHtml(self.component);
            }
            if (html === undefined || html === null) {
                html = self.component.builderTemplate.getHtml(self.data, self.component);
            }
            
            self.dragElement = $(html);
            
            if ($(self.dragElement).find("> .clear-float").length === 0) { //add clear float to check hight in case the childs are all floated
                $(self.dragElement).append('<div class="clear-float"></div>');
            }

            if (self.component.builderTemplate.dragStart)
                self.dragElement = self.component.builderTemplate.dragStart(self.dragElement, self.component);

            self.dragElement.attr("data-cbuilder-dragelement", "true");
            
            self.isDragging = true;
            self.isMoved = false;
            self.frameBody.addClass("is-dragging");
            self.frameBody.find("[data-cbuilder-"+self.component.builderTemplate.getParentContainerAttr(self.data, self.component)+"]").attr("data-cbuilder-droparea", "");
            
            self.iconDrag = $($($this.parent().html())[0]).attr("id", "dragElement-clone").css('position', 'absolute');
            self.iconDrag.find("> a").remove();

            $('body').append(self.iconDrag);
            
            var x = 0;
            var y = 0;
            if (event.type === "touchstart") {
                x = event.touches[0].clientX;
                y = event.touches[0].clientY;
                if (event.touches[0].originalEvent) {
                    x = event.touches[0].originalEvent.clientX;
                    y = event.touches[0].originalEvent.clientY;
                }
            } else {
                x = event.clientX;
                y = event.clientY;
                if (event.originalEvent) {
                    x = event.originalEvent.clientX;
                    y = event.originalEvent.clientY;
                }
            }
            self.iconDrag.css({'left': x - 50, 'top': y - 45});

            event.preventDefault();
            return false;
        });
        
        $('.drag-elements-sidepane').off("mouseup.builder", "ul > li > ol > li");
        $('.drag-elements-sidepane').on("mouseup.builder", "ul > li > ol > li", function (event) {
            if (self.isDragging) {
                self.isDragging = false;
                self.frameBody.removeClass("is-dragging");
                self.frameBody.find("[data-cbuilder-droparea]").removeAttr("data-cbuilder-droparea");

                if (self.iconDrag) {
                    self.iconDrag.remove();
                    self.iconDrag = null;
                }
                if (self.dragElement) {
                    var replicate = self.dragElement.closest('[data-cbuilder-replicate-origin]');
                    self.dragElement.remove();
                    self.frameBody.find("[data-cbuilder-dragSubElement]").remove();
                    if (replicate.length > 0) {
                        var replicateHtml = $(replicate).html();
                        var replicateKey = $(replicate).attr("data-cbuilder-replicate-origin");
                        $(replicate).parent().find('[data-cbuilder-replicate="'+replicateKey+'"]').html(replicateHtml);
                        $(replicate).parent().find('[data-cbuilder-replicate="'+replicateKey+'"] [data-cbuilder-classname]').removeAttr('data-cbuilder-classname');
                    }
                    $("#element-parent-box").hide();
                    self.dragElement = null;
                }
            }
        });

        $('body').off('mouseup.builder touchend.builder');
        $('body').on('mouseup.builder touchend.builder', function (event) {
            if (self.iconDrag && self.isDragging == true)
            {
                self.isDragging = false;
                self.frameBody.removeClass("is-dragging");
                self.frameBody.find("[data-cbuilder-droparea]").removeAttr("data-cbuilder-droparea");
                
                if (self.iconDrag) {
                    self.iconDrag.remove();
                    self.iconDrag = null;
                }
                
                var x = 0;
                var y = 0;
                if (event.type === "touchend") {
                    x = event.changedTouches[0].clientX;
                    y = event.changedTouches[0].clientY;
                    if (event.changedTouches[0].originalEvent) {
                        x = event.changedTouches[0].originalEvent.clientX;
                        y = event.changedTouches[0].originalEvent.clientY;
                    }
                } else {
                    x = event.clientX;
                    y = event.clientY;
                    if (event.originalEvent) {
                        x = event.originalEvent.clientX;
                        y = event.originalEvent.clientY;
                    }
                }
                
                var elementMouseIsOver = document.elementFromPoint(x, y);
                
                if (self.dragElement && elementMouseIsOver && elementMouseIsOver.tagName !== 'IFRAME') {
                    var replicate = self.dragElement.closest('[data-cbuilder-replicate-origin]');
                    self.dragElement.remove();
                    self.frameBody.find("[data-cbuilder-dragSubElement]").remove();
                    if (replicate.length > 0) {
                        var replicateHtml = $(replicate).html();
                        var replicateKey = $(replicate).attr("data-cbuilder-replicate-origin");
                        $(replicate).parent().find('[data-cbuilder-replicate="'+replicateKey+'"]').html(replicateHtml);
                        $(replicate).parent().find('[data-cbuilder-replicate="'+replicateKey+'"] [data-cbuilder-classname]').removeAttr('data-cbuilder-classname');
                    }
                    $("#element-parent-box").hide();
                    self.dragElement = null;
                } else {
                    self.handleDropEnd();
                }
            }
        });

        $('body').off('mousemove.builder touchmove.builder');
        $('body').on('mousemove.builder touchmove.builder', function (event) {
            if (self.iconDrag && self.isDragging == true)
            {
                event.stopPropagation();
                event.stopImmediatePropagation();
                
                var x = 0;
                var y = 0;
                if (event.type === "touchmove") {
                    x = event.touches[0].clientX;
                    y = event.touches[0].clientY;
                    if (event.touches[0].originalEvent) {
                        x = event.touches[0].originalEvent.clientX;
                        y = event.touches[0].originalEvent.clientY;
                    }
                } else {
                    x = event.clientX;
                    y = event.clientY;
                    if (event.originalEvent) {
                       x = event.originalEvent.clientX;
                       y = event.originalEvent.clientY;
                    }
                }
                self.iconDrag.css({'left': x - 50, 'top': y - 45});
                
                var elementMouseIsOver = document.elementFromPoint(x, y);

                //if drag elements hovers over iframe switch to iframe mouseover handler	
                if (elementMouseIsOver && elementMouseIsOver.tagName == 'IFRAME')
                {
                    if (event.type === "touchmove") {
                        self.frameBody.trigger("touchmove", event);
                    } else {
                        self.frameBody.trigger("mousemove", event);
                    }
                    event.stopPropagation();
                    self.selectNode(false);
                }
            }
        });
    },
    
    /*
     * Called when a drop event end to decide it is a move or add new
     */
    handleDropEnd : function() {
        var self = CustomBuilder.Builder;
        
        if (self.component.builderTemplate.dropEnd)
            self.dragElement = self.component.builderTemplate.dropEnd(self.dragElement);
        
        self.dragElement.removeAttr("data-cbuilder-dragelement");
        
        if (self.isMoved) {
            if (self.dragElement.data("cbuilder-classname") === undefined && self.dragElement.data("cbuilder-select") === undefined) {
                self.addElement();
            } else {
                self.moveElement();
            }

            CustomBuilder.update();
        } else {
            if (self.subSelectedEl) {
                self.selectNode(self.subSelectedEl);
            } else {
                self.selectNode(self.selectedEl);
            }
        }
    },
    
    /*
     * Add/render element to canvas when new element drop from pallete.
     * Also update the JSON definition
     */
    addElement : function(callback) {
        var self = CustomBuilder.Builder;
        if (CustomBuilder.Builder.options.callbacks["addElement"] !== undefined && CustomBuilder.Builder.options.callbacks["addElement"] !== "") {
            CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["addElement"], [self.component, self.dragElement, function(elementObj){
                if (self.component.builderTemplate.isAbsolutePosition(self.data, self.component)) {
                    var position = $(self.dragElement).position();
                    elementObj.x_offset = position.left;
                    elementObj.y_offset = position.top;
                }   
                    
                CustomBuilder.Builder.renderElement(elementObj, self.dragElement, self.component, true, null, callback);
            }]);
        } else { 
            var classname = self.component.className;
            var properties = {};
            
            if (self.component.properties !== undefined) {
                properties = $.extend(true, properties, self.component.properties);
            }
            if (self.component.builderTemplate.properties !== undefined) {
                properties = $.extend(true, properties, self.component.builderTemplate.properties);
            }
            
            var elementObj = {
                className: classname,
                properties: properties
            };
            
            self.updateElementId(elementObj);
            
            if (self.component.builderTemplate.isAbsolutePosition(elementObj, self.component)) {
                var position = $(self.dragElement).position();
                elementObj.x_offset = position.left;
                elementObj.y_offset = position.top;
            }
     
            var childsDataHolder = self.component.builderTemplate.getChildsDataHolder(elementObj, self.component);
            var elements = [];
            if (self.component.builderTemplate[childsDataHolder] !== undefined) {
                elements = $.extend(true, elements, self.component.builderTemplate[childsDataHolder]);
                elementObj[childsDataHolder] = elements;
            }
            
            var parent = $(self.dragElement).closest("[data-cbuilder-classname]");
            if ($(parent).length === 0) {
                parent = $(self.dragElement).closest("body");
            }
            var data = parent.data("data");
            
            var index = 0;
            var container = $(self.dragElement).parent().closest("[data-cbuilder-"+self.component.builderTemplate.getParentContainerAttr(elementObj, self.component)+"]");
            index = $(container).find("> *").index(self.dragElement);
            
            var parentDataArray = data[self.component.builderTemplate.getParentDataHolder(elementObj, self.component)];
            if (parentDataArray === undefined) {
                parentDataArray = [];
                data[self.component.builderTemplate.getParentDataHolder(elementObj, self.component)] = parentDataArray;
            }
            if ($(container).is('[data-cbuilder-single]')) {
                parentDataArray.splice(0, parentDataArray.length, elementObj);
                $(container).find("> [data-cbuilder-classname]").remove();
            } else {
                parentDataArray.splice(index, 0, elementObj);
            }
            
            if (self.component.builderTemplate.afterAddElement !== undefined) {
                self.component.builderTemplate.afterAddElement(elementObj, self.component);
            }

            self.renderElement(elementObj, self.dragElement, self.component, true, null, callback);
        }
    },
    
    /*
     * Update element id to an unique value
     */
    updateElementId : function(elementObj) {
        var self = CustomBuilder.Builder;
        if (CustomBuilder.Builder.options.callbacks["updateElementId"] !== undefined && CustomBuilder.Builder.options.callbacks["updateElementId"] !== "") {
            CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["updateElementId"], [elementObj]);
        } else {
            var props = self.parseElementProps(elementObj);
            props["id"] = CustomBuilder.uuid();
        }
        
        var component = self.parseDataToComponent(elementObj);
        var elements = elementObj[component.builderTemplate.getChildsDataHolder(elementObj, component)];
        
        if (elements !== undefined && elements.length > 0) {
            for (var i in elements) {
                self.updateElementId(elements[i]);
            }
        }
    },
    
    /*
     * update the JSON definition when an element is moved
     */
    moveElement : function() {
        var self = CustomBuilder.Builder;
        
        if (self.component.builderTemplate.isAbsolutePosition(self.data, self.component)) {
            var elementObj = $(self.dragElement).data("data");
            var position = $(self.dragElement).position();
            elementObj.x_offset = position.left;
            elementObj.y_offset = position.top;
        }
        
        if (CustomBuilder.Builder.options.callbacks["moveElement"] !== undefined && CustomBuilder.Builder.options.callbacks["moveElement"] !== "") {
            CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["moveElement"], [self.component, self.dragElement]);
        } else {
            self.dragElement.removeAttr("data-cbuilder-dragelement");
        
            var elementObj = $(self.selectedEl).data("data");
            
            var component = self.parseDataToComponent(elementObj);

            var parent = self.currentParent;
            var parentDataHolder = self.currentParentDataHolder;
            if (parent.length === 0) {
                parent = self.selectedEl.closest("body");
            }
            var parentDataArray = $(parent).data("data")[parentDataHolder];
            if (parentDataArray === undefined) {
                parentDataArray = [];
                $(parent).data("data")[parentDataHolder] = parentDataArray;
            }
            var oldIndex = $.inArray(elementObj, parentDataArray);
            if (oldIndex !== -1) {
                parentDataArray.splice(oldIndex, 1);
            }

            var newParent = $(self.dragElement).parent().closest("[data-cbuilder-classname]");
            var newParentDataArray = $(newParent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)];
            if (newParentDataArray === undefined) {
                newParentDataArray = [];
                $(newParent).data("data")[component.builderTemplate.getParentDataHolder(elementObj, component)] = newParentDataArray;
            }
            if ($(self.dragElement).parent().is('[data-cbuilder-single]')) {
                newParentDataArray.splice(0, newParentDataArray.length, elementObj);
                $(self.dragElement).parent().find("> [data-cbuilder-classname]").not(self.dragElement).remove();
            } else {
                var prev = $(self.dragElement).prev("[data-cbuilder-classname]");
                var newIndex = 0;
                if ($(prev).length > 0) {
                    newIndex = $.inArray($(prev).data("data"), newParentDataArray) + 1;
                }
                newParentDataArray.splice(newIndex, 0, elementObj);
            }

            self.checkVisible(parent);
            self.checkVisible(self.selectedEl);
            self.checkVisible(newParent);
            
            if (self.subSelectedEl) {
                self.selectNodeAndShowProperties(self.subSelectedEl, false, (!$("body").hasClass("no-right-panel")));
            } else {
                self.selectNodeAndShowProperties(self.selectedEl, false, (!$("body").hasClass("no-right-panel")));
            }
            
            self.triggerChange();
        }
    },
    
    /*
     * Re-render an element when an element is updated
     */
    updateElement : function(elementObj, element, deferreds) {
        var self = CustomBuilder.Builder;
        
        var component = self.parseDataToComponent(elementObj);
        if (component.builderTemplate !== undefined && component.builderTemplate.updateProperties !== undefined) {
            component.builderTemplate.updateProperties(element, elementObj, component);
        }
        
        self.renderElement(elementObj, element, component, true, deferreds, function(newElement){
            if (self.nodeAdditionalType !== undefined && self.nodeAdditionalType !== "") {
                var level = $(element).data("cbuilder-node-level");
                CustomBuilder.Builder.renderNodeAdditional(self.nodeAdditionalType, newElement, level);
                setTimeout(function(){
                    self._updateBoxes();
                    self.self.triggerChange();
                }, 2);
            } else {
                setTimeout(function(){
                    self._updateBoxes();
                    self.triggerChange();
                }, 2);
            }
        });
    },
    
    /*
     * Rendering an element
     */
    renderElement : function(elementObj, element, component, selectNode, deferreds, callback) {
        var self = CustomBuilder.Builder;
        var oldElement = element;
        $("#element-select-box").hide();
        $("#element-parent-box, #element-highlight-box").hide();
        
        if (deferreds === null || deferreds === undefined || deferreds.length === 0) {
            deferreds = [deferreds];
        }
        
        element.css("border", "");
        
        var dummy = $.Deferred();
        deferreds.push(dummy);
        
        var d = $.Deferred();
        deferreds.push(d);
        
        var html = component.builderTemplate.getHtml(elementObj, component);
        var temp;
        if (html !== undefined) {
            temp = $(html);
            
            //if properties has 
            var props = self.parseElementProps(elementObj);
            if (props.tagName !== undefined && props.tagName !== "") {
                var newTemp = document.createElement(props.tagName);
                attributes = temp[0].attributes;
                for (i = 0, len = attributes.length; i < len; i++) {
                    newTemp.setAttribute(attributes[i].nodeName, attributes[i].nodeValue);
                }
                temp = $(newTemp);
            }
            
            //loop properties for css class, style & attribute 
            self.handleStylingProperties(temp, props);
        }
        var regex = new RegExp('#([^#^\"^ ])*\\.([^#^\"])*\\#');
        if (temp !== undefined && !regex.test($("<div></div>").append($(temp).clone()).html())) {
            element.replaceWith(temp);
            element = temp;
            
            var id = props.id;
            if (id === undefined && elementObj.id !== undefined) {
                id = elementObj.id;
            }
            
            $(element).attr("data-cbuilder-classname", component.className);
            $(element).attr("data-cbuilder-id", id);
            $(element).attr("data-cbuilder-label", self._getElementType(elementObj, component));
            $(element).data("data", elementObj);
            
            if (component.builderTemplate.isAbsolutePosition(elementObj, component)) {
                $(element).attr("data-cbuilder-absolute-position", "");
                $(element).css({
                    "left" : elementObj.x_offset + "px",
                    "top" : elementObj.y_offset + "px",
                    "position" : "absolute"
                });
            }
            
            if (component.builderTemplate.isUneditable(elementObj, component)) {
                $(element).attr("data-cbuilder-uneditable", "");
            }

            self.loadAndUpdateChildElements(element, elementObj, component, deferreds);
            
            d.resolve();
        } else {
            component.builderTemplate.render(element, elementObj, component, function(newElement){
                if (newElement !== null) {
                    var props = self.parseElementProps(elementObj);
                    var id = props.id;
                    if (id === undefined && elementObj.id !== undefined) {
                        id = elementObj.id;
                    }

                    if (newElement === undefined) {
                        newElement = element;
                    }
                    $(newElement).attr("data-cbuilder-classname", component.className);
                    $(newElement).attr("data-cbuilder-id", id);
                    $(newElement).attr("data-cbuilder-label", self._getElementType(elementObj, component));
                    $(newElement).data("data", elementObj);

                    if (component.builderTemplate.isAbsolutePosition(elementObj, component)) {
                        $(newElement).attr("data-cbuilder-absolute-position", "");
                        $(newElement).css({
                            "left" : elementObj.x_offset + "px",
                            "top" : elementObj.y_offset + "px",
                            "position" : "absolute"
                        });
                    }

                    if (component.builderTemplate.isUneditable(elementObj, component)) {
                        $(element).attr("data-cbuilder-uneditable", "");
                    }

                    self.loadAndUpdateChildElements(newElement, elementObj, component, deferreds);
                    element = newElement;
                }
                
                d.resolve();
            });
        }
        
        dummy.resolve();
        
        $.when.apply($, deferreds).then(function() {
            self.checkVisible(element);
            self.checkVisible(element.parent().closest("[data-cbuilder-classname]"));
            
            if (component.builderTemplate.afterRender !== undefined) {
                component.builderTemplate.afterRender(element, elementObj, component)
            }

            if (selectNode) {
                if ($(oldElement).is($(self.selectedEl))) {
                    self.selectedEl = element;
                } else {
                    self.selectNodeAndShowProperties(element, false, (!$("body").hasClass("no-right-panel")));
                }
            }

            if (callback) {
                callback(element);
            }

            self.triggerChange();
        });
    },
    
    /*
     * Used to apply element styling based on properties
     */
    handleStylingProperties: function(element, properties, prefix, cssStyleClass, disableImportant) {
        element.removeAttr("data-cbuilder-mobile-invisible");
        element.removeAttr("data-cbuilder-tablet-invisible");
        element.removeAttr("data-cbuilder-desktop-invisible");
        if (cssStyleClass !== undefined && cssStyleClass !== null && cssStyleClass !== "") {
            element.find('> style[data-cbuilder-style="'+cssStyleClass+'"]').remove();
        }
        
        if (prefix === undefined) {
            prefix = "";
        } else {
            prefix += "-";
        }
        
        var desktopStyle = "";
        var tabletStyle = "";
        var mobileStyle = "";
        var hoverDesktopStyle = "";
        var hoverTabletStyle = "";
        var hoverMobileStyle = "";
        
        var getStyle = function(value, key, prefix) {
            if (key === (prefix + "custom")) {
                var values = value.split(";");
                var temp = "";
                for (var v in values) {
                    if (values[v] !== "") {
                        if (values[v].indexOf("!important") === -1 && (disableImportant === undefined || disableImportant === true)) {
                            values[v] += " !important";
                        }
                        temp += values[v] + ";";
                    }
                }
                return temp;
            } else {
                return key.replace(prefix, "") + ":" + value + ((disableImportant === undefined || disableImportant === true)?" !important":"")+";";
            }
        };
        
        for (var property in properties) {
            if (properties.hasOwnProperty(property)) {
                if (property.indexOf(prefix+'attr-') === 0) {
                    var key = property.replace(prefix+'attr-', '');
                    element.attr(key, properties[property]);
                } else if (property.indexOf(prefix+'css-') === 0) {
                    if (properties[property] !== "") {
                        element.addClass(properties[property]);
                    }
                } else if (property.indexOf(prefix+'style-') === 0) {
                    var value = properties[property];
                    if (property.indexOf('-background-image') > 0) {
                        if (value.indexOf("#appResource.") === 0) {
                            value = value.replace("#appResource.", CustomBuilder.contextPath + '/web/app/' + CustomBuilder.appId + '/resources/');
                            value = value.substring(0, value.length -1);
                        }
                        value = "url('" + value + "')";
                    }
                    
                    if (property.indexOf(prefix+'style-hover-mobile-') === 0) {
                        hoverMobileStyle += getStyle(value, property, prefix+'style-hover-mobile-');
                    } else if (property.indexOf(prefix+'style-hover-tablet-') === 0) {
                        hoverTabletStyle += getStyle(value, property, prefix+'style-hover-tablet-');
                    } else if (property.indexOf(prefix+'style-hover-') === 0) {
                        hoverDesktopStyle += getStyle(value, property, prefix+'style-hover-');
                    } else if (property.indexOf(prefix+'style-mobile-') === 0) {
                        var key = property.replace(prefix+'style-mobile-', '');
                        if (key === "display" && value === "none") {
                            element.attr("data-cbuilder-mobile-invisible", "");
                        } else {
                            mobileStyle += getStyle(value, property, prefix+'style-mobile-');
                        }
                    } else if (property.indexOf(prefix+'style-tablet-') === 0) {
                        var key = property.replace(prefix+'style-tablet-', '');
                        if (key === "display" && value === "none") {
                            element.attr("data-cbuilder-tablet-invisible", "");
                        } else {
                            tabletStyle += getStyle(value, property, prefix+'style-tablet-');
                        }
                    } else {
                        var key = property.replace(prefix+'style-', '');
                        if (key === "display" && value === "none") {
                            element.attr("data-cbuilder-desktop-invisible", "");
                        } else {
                            desktopStyle += getStyle(value, property, prefix+'style-');
                        }
                    }
                }
            }
        }
        
        //if has text content
        if (properties[prefix+"textContent"] !== undefined) {
            if (element.is('[data-cbuilder-textContent]')) {
                element.html(properties[prefix+"textContent"]);
            } else {
                element.find('[data-cbuilder-textContent]').html(properties[prefix+"textContent"]);
            }
        }
        
        var builderStyles = "";
        if (desktopStyle !== "" || tabletStyle !== "" || mobileStyle !== "") {
           var styleClass = cssStyleClass;
           if (styleClass === undefined || styleClass === null || styleClass === "") {
                styleClass = "builder-style-"+CustomBuilder.uuid();
                element.addClass(styleClass);
                styleClass = "." + styleClass;
           }
           
           builderStyles = "<style data-cbuilder-style='"+styleClass+"'>";
           if (desktopStyle !== "") {
               builderStyles += styleClass + "{" + desktopStyle + "} ";
           }
           if (tabletStyle !== "") {
               builderStyles += "@media (max-width: 991px) {" + styleClass + "{" + tabletStyle + "}} ";
           }
           if (mobileStyle !== "") {
               builderStyles += "@media (max-width: 767px) {" + styleClass + "{" + mobileStyle + "}} ";
           }
           if (hoverDesktopStyle !== "") {
               builderStyles += styleClass + ":hover{" + hoverDesktopStyle + "} ";
           }
           if (hoverTabletStyle !== "") {
               builderStyles += "@media (max-width: 991px) {" + styleClass + ":hover{" + hoverTabletStyle + "}} ";
           }
           if (hoverMobileStyle !== "") {
               builderStyles += "@media (max-width: 767px) {" + styleClass + ":hover{" + hoverMobileStyle + "}} ";
           }
           builderStyles += "</style>";
           element.append(builderStyles);
        }
    },

    /*
     * Set html to canvas
     */
    setHtml: function (html)
    {
        window.FrameDocument.body.innerHTML = html;
    },

    /*
     * Add html to canvas head
     */
    setHead: function (head)
    {
        CustomBuilder.Builder.frameHead.append(head);
    },
    
    /*
     * Check visible from the most inner node
     */
    recursiveCheckVisible : function(node) {
        $(node).find("> [data-cbuilder-classname]").each(function(){
            CustomBuilder.Builder.recursiveCheckVisible($(this));
        });
        CustomBuilder.Builder.checkVisible($(node));
    },
    
    /*
     * Check an element is visible or not, if not show an invisible flag
     */
    checkVisible : function(node) {
        $(node).removeAttr("data-cbuilder-invisible");
        if (!$(node).is('[data-cbuilder-uneditable]') && !$(node).is('[data-cbuilder-visible]')) { //use "data-cbuilder-visible" to skip visiblity check
            var temp = $('<div>'+$(node).html()+'</div>');
            $(temp).find('style, script').remove();
            if ($(node).is('div, p') && $(temp).text().trim() === "" 
                    && $(node).find("[data-cbuilder-invisible]").length === 0
                    && $(node).find("a, hr, img, svg, video, iframe, picture, canvas").length === 0) { //handle non-text elements
                $(node).attr("data-cbuilder-invisible", "");
            } else {
                if ($(node).find("> .clear-float").length === 0) { //add clear float to check hight in case the childs are all floated
                    $(node).append('<div class="clear-float"></div>');
                }
                var height = $(node).outerHeight(false);
                if ($(node).find("> .cbuilder-node-details").length > 0) {
                    height = height - $(node).find("> .cbuilder-node-details").outerHeight();
                }
                if (height === 0) {
                    $(node).attr("data-cbuilder-invisible", "");
                }
            }
        }
    },
    
    /*
     * Render the tree menu for element navigation. Used by tree viewer
     */
    renderTreeMenu: function(container, node) {
        var self = CustomBuilder.Builder;
        
        if (container.find("> ol").length === 0) {
            container.append('<ol></ol>');
        }
        
        var target = node;
        if (node === undefined) {
            target = self.frameBody;
        }
        
        $(target).find("> *").each(function() {
            if ($(this).is("[data-cbuilder-classname]") && !$(this).is("[data-cbuilder-uneditable]")) {
                var rid = "r" + (new Date().getTime());
                var data = $(this).data("data");
                var component = self.parseDataToComponent(data);
                var props = self.parseElementProps(data);
                
                if (component.builderTemplate.customPropertiesData) {
                    props = component.builderTemplate.customPropertiesData(props, data, component);
                }
                
                var label = component.label;
                if (component.builderTemplate.getLabel) {
                    label = component.builderTemplate.getLabel(data, component);
                } else if (component.builderTemplate.isSupportProperties(data, component)) {
                    if (props.label !== undefined && props.label !== "") {
                        label = props.label;
                    } else if (props.textContent !== undefined && props.textContent !== "") {
                        label = props.textContent;
                    } else if (props.id !== undefined && props.id !== "" && props.id.length < 32) {
                        label = props.id;
                    }
                }
                
                label = UI.stripHtmlTags(label);
                if (label.length > 30) {
                    label += label.substring(0, 27) + "...";
                }
                
                var li = $('<li class="tree-viewer-item"><label>'+component.icon+' <a>'+label+'</a></label><input type="checkbox" id="'+rid+'" checked/></li>');
                $(li).data("node", $(this));
                $(li).attr("data-cbuilder-node-id", props.id);
                
                //create a refer from element back to the tree node
                $(this).data("tree-node", $(li));
                
                $(this).off("builder.selected");
                $(this).on("builder.selected", function(event) {
                    $(".tree-viewer-item").removeClass("active");
                    $(li).addClass("active");
                    
                    event.stopPropagation();
                });
                
                if (self.selectedEl && self.selectedEl.is($(this))) {
                    $(li).addClass("active");
                }
                container.find("> ol").append(li);
                
                if (component.builderTemplate.customTreeMenu) {
                    component.builderTemplate.customTreeMenu(li, data, component);
                }
                
                self.renderTreeMenu(li, $(this));
            } else {
                if (CustomBuilder.Builder.options.callbacks["renderTreeMenuAdditionalNode"] !== undefined && CustomBuilder.Builder.options.callbacks["renderTreeMenuAdditionalNode"] !== "") {
                    container = CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["renderTreeMenuAdditionalNode"], [container, $(this)]);
                }
                self.renderTreeMenu(container, $(this));
            }
        });
        
        //cleaning & attach event
        if (node === undefined) {
            container.find("li").each(function(){
                if ($(this).find("> ol > li").length === 0) {
                    $(this).find("> ol").remove();
                    $(this).find("> input").remove();
                }
            });
            
            $(container).off("click", "li.tree-viewer-item > label");
            $(container).on("click", "li.tree-viewer-item > label", function (e) {
                node = $(this).parent().data("node");
                if (node !== undefined) {
                    self.frameHtml.animate({
                        scrollTop: $(node).offset().top - 25
                    }, 1000);

                    node.trigger("mousedown").trigger("mouseup");
                }
            });
        }
    },
    
    /*
     * Used to traverling through all the nodes and render additional html to the node.
     * Used by xray viwer
     */
    renderNodeAdditional : function(type, node, level) {
        var self = CustomBuilder.Builder;
        
        self.nodeAdditionalType = type;
        
        var target = $(node);
        if (node === undefined) {
            target = self.frameBody;
            
            $("#node-details-toggle").find("label").removeClass("active");
            $("#node-details-toggle").find("#details-toggle-all").attr("checked", "");
            $("#node-details-toggle").find("#details-toggle-all").parent().addClass("active");
            $("#node-details-toggle").find("#details-toggle-single").removeAttr("checked");
            $("#node-details-toggle").show();
            
            $("#node-details-toggle").find("input").off("click");
            $("#node-details-toggle").find("input").on("click", function(){
                if ($("#details-toggle-single").is(":checked")) {
                    self.frameBody.addClass("show-node-details-single");
                } else {
                    self.frameBody.removeClass("show-node-details-single");
                }
                self._updateBoxes();
                self.triggerEvent("nodeAdditionalModeChanged");
            });
            
            self.frameBody.addClass("show-node-details");
            level = 0;
            self.colorCount = 0;
        }
        
        var clevel = level;
        if (target.is("[data-cbuilder-classname]") && !target.is("[data-cbuilder-uneditable]")) {
            clevel++;
        }
        
        $(target).find("> *:not(.cbuilder-node-details)").each(function() {
            self.renderNodeAdditional(type, $(this), clevel);
        });
        
        if (target.is("[data-cbuilder-classname]") || target.is("[data-cbuilder-select]")) {
            var element = target;
            if (target.is("[data-cbuilder-select]")) {
                var id = $(target).data('cbuilder-select');
                element = self.frameBody.find('[data-cbuilder-id="'+id+'"]');
                if ($(element).find("> .cbuilder-node-details").length > 0) {
                    return;
                }
            } else if (!$(element).is(":visible")) {
                return;
            }
            
            if ($(element).is("[data-cbuilder-uneditable]")) {
                return;
            }
            
            var data = $(element).data("data");
            var component = self.parseDataToComponent(data);
            
            if(!component.builderTemplate.isRenderNodeAdditional(data, component, type)) {
                return;
            }
            
            var detailsDiv = $("<div class='cbuilder-node-details cbuilder-details-"+type+"' style='visibility:hidden'></div>");
            if (component.builderTemplate.addNodeDetailContainer) {
                component.builderTemplate.addNodeDetailContainer(target, detailsDiv, data, component, type);
            } else {
                $(target).prepend(detailsDiv);
            }
            $(target).attr("data-cbuilder-node-level", level);
            
            var color = level;
            if (component.builderTemplate.nodeDetailContainerColorNumber) {
                color = component.builderTemplate.nodeDetailContainerColorNumber(target, data, component, type);
            }
            
            $(detailsDiv).addClass("cbuilder-node-details-color"+color);
            $(detailsDiv).prepend("<dl class=\"cbuilder-node-details-list\"></dl>");
            
            var dl = detailsDiv.find('dl');
            var label = component.label;
            if (component.builderTemplate.getLabel) {
                label = component.builderTemplate.getLabel(data, component);
            }
            if ($(element).is("[data-cbuilder-missing-plugin]")) {
                label = '<span class="missing-plugin">' + label + "</span>";
            }
            dl.append('<dt><i class="las la-cube" title="'+get_cbuilder_msg('cbuilder.type')+'"></i></dt><dd>'+label+'</dd>');

            var props = self.parseElementProps(data);
            
            if (component.builderTemplate.customPropertiesData) {
                props = component.builderTemplate.customPropertiesData(props, data, component);
            }
                
            var id = props.id;
            if (id === undefined && data.id !== undefined) {
                id = data.id;
            }
            if (props.customId !== undefined && props.customId !== "") {
                id = props.customId;
            }
            if (id !== undefined) {
                dl.append('<dt><i class="las la-id-badge" title="'+get_cbuilder_msg('cbuilder.id')+'"></i></dt><dd>'+id+'</dd>');
            }
            
            var callback = function() {
                self.adjustNodeAdditional(target);
                
                if (level === 0) {
                    CustomBuilder.Builder.triggerEvent("nodeAdditionalAdded");
                }
            };
            
            var method = component.builderTemplate["render" + type];
            if (method !== undefined) {
                component.builderTemplate["render" + type](detailsDiv, element, data, component, callback);
            } else if (CustomBuilder.Builder.options.callbacks["render" + type] !== undefined && CustomBuilder.Builder.options.callbacks["render" + type] !== "") {
                CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["render" + type], [detailsDiv, element, data, component, callback]);
            } else if (CustomBuilder.Builder["render" + type] !== undefined) {
                CustomBuilder.Builder["render" + type](detailsDiv, element, data, component, callback);
            } else {
                callback();
            }
        }
    },
    
    adjustNodeAdditional : function(target) {
        var self = CustomBuilder.Builder;
        
        //check if negative margin top
        if ($(target).css("margin-top").indexOf("-") !== -1) {
            $(target).addClass("cbuilder-node-details-reset-margin-top");
        }
        
        var detailsDiv = $(target).find("> .cbuilder-node-details");
        
        //reset to empty first
        $(detailsDiv).find(".cbuilder-node-details-list").css({
            "top" : "",
            "left" : "",
            "right" : ""
        });
        $(detailsDiv).css("padding-top", "");
        $(detailsDiv).css("visibility", "hidden");

        var box = self.getBox(target);
        var dBox = self.getBox(detailsDiv, 3);
        var targetOffset = target.offset();

        var offset = 0;
        if (targetOffset.top !== box.top) {
            offset = targetOffset.top - box.top;
        }

        $(detailsDiv).find(".cbuilder-node-details-list").css({
            "top" : (box.top - dBox.top + offset) + "px",
            "left" : (box.left - dBox.left) + "px",
            "right" : ((dBox.left + dBox.width) - (box.left + box.width)) + "px"
        });

        var height = $(detailsDiv).find(".cbuilder-node-details-list").outerHeight();
        var padding = height + (box.top - dBox.top + offset) + offset;

        setTimeout(function(){
            $(detailsDiv).css("padding-top", padding + "px");
            $(detailsDiv).css("visibility", "visible");
            self._updateBoxes();
        }, 1);

        $(detailsDiv).uitooltip({
            position: { my: "left+15 center", at: "right center" }
        });
        
        if ($(detailsDiv).find("span.missing-plugin").length > 0) {
            var treeNode = $(target).data("tree-node");
            if ($(treeNode).length > 0) {
                $(treeNode).find("> label > a").css("color", "#fb8e8e");
            }
        }
    },
    
    /*
     * Used to traverling through all the nodes and remove additional html added to the node.
     * Used by xray viwer and permission editor
     */
    removeNodeAdditional : function(node) {
        var self = CustomBuilder.Builder;
        
        self.nodeAdditionalType = "";
        
        var target = $(node);
        if (node === undefined) {
            target = self.frameBody;
            $("#node-details-toggle").hide();
            self.frameBody.removeClass("show-node-details show-node-details-single");
            self.frameBody.find(".cbuilder-node-details").remove();
            self.frameBody.find(".cbuilder-node-details-reset-margin-top").removeClass("cbuilder-node-details-reset-margin-top");
        }
        
        $(target).find(".cbuilder-node-details-wrap").each(function() {
            $(this).find("> [data-cbuilder-classname]").unwrap();
        });
        
        CustomBuilder.Builder.triggerEvent("nodeAdditionalRemoved");
    },
    
    /*
     * Calculate the box position and size of a node
     */
    getBox: function(node, level) {
        var self = CustomBuilder.Builder;
        
        var offset = $(node).offset();
        var top = offset.top;
        var left = offset.left;
        var right = left + ($(node).outerWidth() * self.zoom);
        var bottom = top + ($(node).outerHeight() * self.zoom);
        
        if (level === undefined) {
            level = 0;
            
            var id = $(node).data("cbuilder-id");
            if (id !== undefined) {
                self.frameBody.find('[data-cbuilder-group="'+id+'"]').each(function(){
                    var cbox = self.getBox($(this), 2);
                    
                    if (cbox.top > 0 && cbox.top < top) {
                        top = cbox.top;
                    }
                    if (cbox.left > 0 && cbox.left < left) {
                        left = cbox.left;
                    }
                    if (cbox.right > 0 && cbox.right > right) {
                        right = cbox.right;
                    }
                    if (cbox.bottom > 0 && cbox.bottom > bottom) {
                        bottom = cbox.bottom;
                    }
                });
            }
        }
        
        if (level < 3) {
            $(node).find("> *:visible:not(.cbuilder-node-details)").each(function(){
                var cbox = self.getBox($(this), ++level);

                if (cbox.top > 0 && cbox.top < top) {
                    top = cbox.top;
                }
                if (cbox.left > 0 && cbox.left < left) {
                    left = cbox.left;
                }
                if (cbox.right > 0 && cbox.width > right) {
                    right = cbox.right;
                }
                if (cbox.bottom > 0 && cbox.height > bottom) {
                    bottom = cbox.bottom;
                }
            });
        }
        
        var box = {
            top : top,
            left : left,
            right : right,
            bottom : bottom,
            width : right - left,
            height : bottom - top
        };
        
        return box;
    },
    
    /*
     * Edit the element style on right panel
     */
    editStyles : function (elementProperties, element, elementObj, component) {
        // show property dialog
        var options = {
            appPath: "/" + CustomBuilder.appId + "/" + CustomBuilder.appVersion,
            contextPath: CustomBuilder.contextPath,
            propertiesDefinition : component.builderTemplate.getStylePropertiesDefinition(elementObj, component),
            propertyValues : elementProperties,
            showCancelButton:false,
            changeCheckIgnoreUndefined: true,
            editorPanelMode: true,
            closeAfterSaved: false,
            saveCallback: function(container, properties) {
                var d = $(container).find(".property-editor-container").data("deferred");
                d.resolve({
                    container :container, 
                    prevProperties : elementProperties,
                    properties : properties, 
                    elementObj : elementObj,
                    element : element
                });
            },
            validationFailedCallback: function(container, errors) {
                var d = $(container).find(".property-editor-container").data("deferred");
                d.resolve({
                    container :container, 
                    prevProperties : elementProperties,
                    errors : errors, 
                    elementObj : elementObj,
                    element : element
                });
            }
        };
        
        $("#style-properties-tab-link").show();
        $("#right-panel #style-properties-tab").find(".property-editor-container").remove();
        $("#right-panel #style-properties-tab").propertyEditor(options);
        
        var supportViewport = false;
        
        for (var i in options.propertiesDefinition) {
            for (var j in options.propertiesDefinition[i].properties) {
                if (options.propertiesDefinition[i].properties[j].viewport !== undefined) {
                    supportViewport = true;
                    break;
                }
            }
            if (supportViewport) {
                break;
            }
        }
        
        if (supportViewport) {
            var controls = $('<div id="style-properties-viewport-control" class="btn-group btn-group-sm btn-group-fullwidth clearfix" role="group"></div>');
            $(controls).append('<button data-viewport="desktop" class="btn btn-outline-dark active"><i class="la la la-laptop"></i> '+get_cbuilder_msg('cbuilder.desktop')+'</button>');
            $(controls).append('<button data-viewport="tablet" class="btn btn-outline-dark"><i class="la la-tablet"></i> '+get_cbuilder_msg('cbuilder.tablet')+'</button>');
            $(controls).append('<button data-viewport="mobile" class="btn btn-outline-dark"><i class="la la-mobile-phone"></i> '+get_cbuilder_msg('cbuilder.mobile')+'</button>');

            $(controls).find("button").off("click").on("click", function(){
                $(controls).find("button").removeClass("active");
                $(this).closest(".property-editor-container").attr("data-viewport", $(this).data("viewport"));
                $(this).addClass("active");
                return false;
            });
            $("#right-panel #style-properties-tab").find(".property-editor-container").attr("data-viewport", "desktop");
            $("#right-panel #style-properties-tab").find(".property-editor-container > .property-editor-pages").prepend(controls);
        }
        
        if ($("body").hasClass("max-property-editor")) {
            CustomBuilder.adjustPropertyPanelSize();
        }
        
        if (CustomBuilder.getBuilderSetting("expandProps") === true) {
            $("#right-panel .property-editor-container > .property-editor-pages > .property-editor-page ").removeClass("collapsed");
        }
    },
    
    /*
     * Used to prepare the base element styling properties
     */
    stylePropertiesDefinition : function(prefix, title) {
        var self = CustomBuilder.Builder;
        
        if (prefix === undefined || prefix === null) {
            prefix = "";
        } else if (prefix.indexOf("-", prefix.length - 1) === -1) {
            prefix = prefix + "-";
        }
        if (title === undefined || title === null) {
            title = get_cbuilder_msg('style.styling');
        }
        
        return [
                {
                    title: title,
                    properties:[
                        {
                            name : prefix+'style',
                            label : get_cbuilder_msg('style.normalstyle'),
                            type : 'cssstyle',
                            viewport : 'desktop'
                        },
                        {
                            name : prefix+'style-tablet',
                            label : get_cbuilder_msg('style.normalstyle'),
                            type : 'cssstyle',
                            viewport : 'tablet'
                        },
                        {
                            name : prefix+'style-mobile',
                            label : get_cbuilder_msg('style.normalstyle'),
                            type : 'cssstyle',
                            viewport : 'mobile'
                        },
                        {
                            name : prefix+'style-hover',
                            label : get_cbuilder_msg('style.hoverstyle'),
                            type : 'cssstyle',
                            viewport : 'desktop'
                        },
                        {
                            name : prefix+'style-hover-tablet',
                            label : get_cbuilder_msg('style.hoverstyle'),
                            type : 'cssstyle',
                            viewport : 'tablet'
                        },
                        {
                            name : prefix+'style-hover-mobile',
                            label : get_cbuilder_msg('style.hoverstyle'),
                            type : 'cssstyle',
                            viewport : 'mobile'
                        }
                    ]
                }
            ];
    },
    
    /*
     * Used to prepare the style definition
     */
    generateStylePropertiesDefinition : function(prefix, configs) {
        var self = CustomBuilder.Builder;
        
        var orig = self.stylePropertiesDefinition();
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
    
    /*
     * Trigger a change when there is a canvas change happen
     */
    triggerChange : function() {
        var self = CustomBuilder.Builder;
        
        self.frameBody.find('[data-cbuilder-replicate-origin]').each(function(){
            var replicate = $(this);
            var replicateHtml = $(replicate).html();
            var replicateKey = $(replicate).attr("data-cbuilder-replicate-origin");
            $(replicate).parent().find('[data-cbuilder-replicate="'+replicateKey+'"]').html(replicateHtml);
            $(replicate).parent().find('[data-cbuilder-replicate="'+replicateKey+'"] [data-cbuilder-classname]').removeAttr('data-cbuilder-classname');
        });
        
        CustomBuilder.Builder.triggerEvent("change.builder");
    },
    
    /*
     * Trigger an event from canvas
     */
    triggerEvent : function(eventName) {
        setTimeout(function() {
            $(CustomBuilder.Builder.iframe).trigger(eventName);
        }, 0);
    },
    
    /*
     * A convenient method to bind to a canvas event 
     */
    bindEvent : function(eventName, callback) {
        $(CustomBuilder.Builder.iframe).off(eventName, callback);
        $(CustomBuilder.Builder.iframe).on(eventName, callback);
    },
    
    /*
     * A convenient method to unbind to a canvas event 
     */
    unbindEvent : function(eventName, callback) {
        if (callback) {
            $(CustomBuilder.Builder.iframe).off(eventName, callback);
        } else {
            $(CustomBuilder.Builder.iframe).off(eventName);
        }
    },
    
    /*
     * Used for _initHighLight to get element behind an ignored element
     */
    getElementsOnPosition : function(x, y, selector) {
        var self = CustomBuilder.Builder;
        
        //when has elements, find element at x,y
        var element = null;
        var offset = null;
        var top = null;
        var left =  null;

        self.frameBody.find(selector).each(function(){
            if (element === null) {
                offset = $(this).offset();
                top = offset.top - $(self.frameDoc).scrollTop();
                left = offset.left  - $(self.frameDoc).scrollLeft();

                if (y < top + ($(this).outerHeight() * self.zoom) && x < left + ($(this).outerWidth() * self.zoom)) {
                    element = $(this);
                }
            }
        });

        return element;
    },
    
    /*
     * Set zoom ratio for the canvas
     */
    setZoom : function(action) {
        var self = CustomBuilder.Builder;
        if (action === "-") {
            if (self.zoom > 0.5) {
                self.zoom = self.zoom - 0.1;
            }
        } else if (action === "+") {
            if (self.zoom < 1.5) {
                self.zoom = self.zoom + 0.1;
            }
        } else {
            self.zoom = 1;
        }
        self.frameBody.css({
            "transform" : "scale("+self.zoom+")",
            "transform-origin" : "0 0"
        });
        self.triggerChange();
        self._updateBoxes();
    },
    
    /*
     * Render a screesnhot for download in screenshot view
     */
    renderScreenshot : function() {
        $("#screenshotViewImage").html('<i class="las la-spinner la-3x la-spin" style="opacity:0.3"></i>');
        $("#screenshotView .sticky-buttons").html("");
        
        var self = CustomBuilder.Builder;
        
        self.frameBody.addClass("screenshot-in-progress");
        var target = self.frameBody;
        var id = CustomBuilder.id;
        
        if (self.frameBody.find('> *:eq(0)').length > 0) {
            var tempId = self.frameBody.find('> *:eq(0)').attr("data-cbuilder-id");
            if (tempId !== undefined && tempId !== "") {
                id = tempId;
            }
        }
        
        if (CustomBuilder.screenshotTimeout !== undefined && CustomBuilder.screenshotTimeout !== null) {
            clearTimeout(CustomBuilder.screenshotTimeout);
        }
        
        CustomBuilder.screenshotTimeout = setTimeout(function() {
            CustomBuilder.getScreenshot(target, function(image){
                $("#screenshotViewImage").html('<img style="max-width:100%; border:1px solid #ddd;" src="'+image+'"/>');
                
                var link = document.createElement('a');
                link.download = CustomBuilder.appId + '-' + CustomBuilder.builderType + '-' + id+'.png';
                link.href = image;
                $(link).addClass("btn button btn-secondary");  
                $(link).html(get_cbuilder_msg('cbuilder.download'));
                $("#screenshotView .sticky-buttons").append(link);
                
                self.frameBody.removeClass("screenshot-in-progress");
                CustomBuilder.screenshotTimeout = null;
            }, function(error) {
                self.frameBody.removeClass("screenshot-in-progress");
                CustomBuilder.screenshotTimeout = null;
            });
        }, 300);
    },
    
    /*
     * Used to update the toolbar paste icon when browser tab active. 
     * Called by 
     */
    updatePasteStatus : function(event, hidden) {
        if (!document[hidden]) {
            $("#paste-element-btn").addClass("disabled");
            var element = CustomBuilder.Builder.selectedEl;
            var data = CustomBuilder.data;
            if (element !== null) {
                data = $(element).data("data");
            }
            var component = CustomBuilder.Builder.parseDataToComponent(data);
            if (component !== null && component.builderTemplate.isPastable(data, component)) {
                $("#paste-element-btn").removeClass("disabled");
            }
        }
    }
}

CustomBuilder = $.extend(true, {}, _CustomBuilder);

var isIE11 = !!window.MSInputMethodContext && !!document.documentMode;
