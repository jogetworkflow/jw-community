AppBuilder = {
    
    /*
     * Intialize the builder, called from CustomBuilder.initBuilder
     */
    initBuilder: function (callback) {
        var self = AppBuilder;
        
        $("#design-btn").attr("title", get_cbuilder_msg('abuilder.builders')).find("span").text(get_cbuilder_msg('abuilder.builders'));
        $("#design-btn").after('<a class="btn btn-light" title="'+self.msg('versions')+'" id="versions-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="versions" href="'+CustomBuilder.contextPath+'/web/console/app/'+CustomBuilder.appId+'/versioning" data-cbuilder-action="switchView" data-hide-tool=""><i class="la la-list-ol"></i> <span>'+self.msg('versions')+'</span></a>');
        
        $("#save-btn").parent().after('<div class="btn-group mr-1 float-right" style="margin-top:-16px;" role="group"><button class="btn btn-secondary btn-icon" title="'+self.msg('export')+'" id="export-btn" data-cbuilder-action="exportApp"><i class="las la-file-export"></i> <span>'+self.msg('export')+'</span></button></div>');
        
        $('#save-btn').hide();
        $('#save-btn').after(' <button class="btn btn-secondary btn-icon" style="display:none;" title="'+self.msg('unpublish')+'" id="unpublish-btn" data-cbuilder-action="unpublishApp"><i class="las la-cloud-download-alt"></i> <span>'+self.msg('unpublish')+'</span></button>\
            <button class="btn btn-primary btn-icon" style="display:none;" title="'+self.msg('publish')+'" id="publish-btn" data-cbuilder-action="publishApp"><i class="las la-cloud-upload-alt"></i> <span>'+self.msg('publish')+'</span></button>');
        
        var appInfo = self.msg('appInfo');
        if (appInfo !== "") {
            $("#builderToolbar").append('<span id="app-info">'+appInfo+'</div>');
        }
        
        $("#builder_canvas").off("click", " li.item a.item-link");
        $("#builder_canvas").on("click", " li.item a.item-link", function(){
            CustomBuilder.ajaxRenderBuilder($(this).attr("href"));
            return false;
        });
        $("#builder_canvas").off("click", " li.item a.delete");
        $("#builder_canvas").on("click", " li.item a.delete", function(){
            HelpGuide.hide();
            
            AppBuilder.deleteItem($(this).closest(".item"));
            return false;
        });
        $("#builder_canvas").off("click", " li.item a.launch");
        $("#builder_canvas").on("click", " li.item a.launch", function(){
            if (!$(this).hasClass("disabled")) {
                window.open(CustomBuilder.contextPath+'/web/userview/'+CustomBuilder.appId+'/'+$(this).closest(".item").attr("data-id"));
            }
            return false;
        });
        $("#builder_canvas").off("click", " li.item a.runprocess");
        $("#builder_canvas").on("click", " li.item a.runprocess", function(){
            HelpGuide.hide();
            
            if (!$(this).hasClass("disabled")) {
                var url = CustomBuilder.contextPath + '/web/client/app' + CustomBuilder.appPath + '/process/' + $(this).closest(".item").attr("data-id");
                JPopup.show("runProcessDialog", url, {}, "");
            }
            return false;
        });
        $("#builder_canvas").off("click", ".addnew");
        $("#builder_canvas").on("click", ".addnew", function(){
            HelpGuide.hide();
            
            var type = $(this).data("builder-type");
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
        
        AppBuilder.view = getUrlParam('view');
        
        CustomBuilder.cachedAjax({
            type: "POST",
            url: CustomBuilder.contextPath + '/web/json/console/app'+CustomBuilder.appPath+'/builders/overviewTools',
            dataType : "json",
            beforeSend: function (request) {
               request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            },
            success: function(response) {
                if (response !== undefined && response.length > 0) {
                    for (var i in response) {
                        $("#builderToolbar #hide-advanced-tools-btn").before('<button class="btn btn-light" title="'+response[i].label+'" id="'+response[i].className.replace(/\./g, '_')+'" type="button" data-cbuilder-view="overview" data-toggle="button" aria-pressed="false" data-overview="'+response[i].className+'">'+response[i].icon+'</button>');
                    }
                    $("#builderToolbar #hide-advanced-tools-btn").before('<button class="btn btn-light" title="'+get_cbuilder_msg('abuilder.overviewMap')+'" id="overviewmap-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="overviewMap" data-cbuilder-action="switchView" data-view-control><i class="las la-sitemap"></i> </button>');
        
                    $("#builderToolbar [data-overview]").off("click").on("click", function(){
                        CustomBuilder.switchView();
                        AppBuilder.showOverview($(this).data("overview"));
                        
                        $("[data-cbuilder-view]").removeClass("active-view active");
                        $(this).addClass("active-view");
                        
                        return false;
                    });
                }
        
                callback();
            }
        });
    },
    
    /*
     * Load and render data, called from CustomBuilder.loadJson
     */
    load: function (data) {
        if (CustomBuilder.appPublished !== "true") {
            $("#unpublish-btn").hide();
            $("#publish-btn").show();
        } else {
            $("#unpublish-btn").show();
            $("#publish-btn").hide();
        }
        
        $("#builder_canvas").css("opacity", "0.3");
        CustomBuilder.getBuilderItems(AppBuilder.renderBuilders);
        
        $(".canvas-header .error.missingplugin").remove();
        
        if (AppBuilder.view !== "") {
            setTimeout(function(){
                $("[data-cbuilder-view='"+AppBuilder.view+"']").trigger("click");
                AppBuilder.view = "";
            }, 1500);
        }
    },
    
    /*
     * Render builder and its items to canvas
     */
    renderBuilders: function(data) {
        var self = AppBuilder;
        
        $("#builder_canvas").html('<div><div class="canvas-header"><div class="search-container"><input class="form-control form-control-sm component-search" placeholder="'+get_cbuilder_msg('cbuilder.search')+'" type="text"><button class="clear-backspace"><i class="la la-close"></i></button></div> <a href="" id="showTags"><i class="las la-tags"></i> <span>'+self.msg('showTag')+'</span></a></div><div id="builders"><div id="builders-seperator"></div></div></div>');
        
        $("#builder_canvas").find('.search-container input').off("keyup change");
        $("#builder_canvas").find('.search-container input').on("keyup change", function(){
            var searchText = $(this).val().toLowerCase();
            if (searchText !== "") {
                var tags = "";
                if (searchText.indexOf("#") === 0) {
                    tags = searchText;
                    searchText = "";
                } else if (searchText.indexOf("#") > 0) {
                    tags = searchText.substring(searchText.indexOf("#"));
                    searchText = searchText.substring(0, searchText.indexOf("#") - 1);
                }
                
                searchText = searchText.trim();
                var tagsArr = [];
                if (tags !== "") {
                    var temp = tags.split("#");
                    for (var i in temp) {
                        var t = temp[i].trim();
                        if (t !== "") {
                            tagsArr.push(t);
                        }
                    }
                }
                
                $("#builder_canvas").find("li.item").each(function(){
                    var match = false;
                    if (searchText !== "") {
                        if ($("body").hasClass("overview_view")) { //overview tool search
                            $(this).find('.overview_data.active_data').each(function(){
                                if ($(this).text().toLowerCase().indexOf(searchText) > -1) {
                                    match = true;
                                    $(this).removeClass("search_hide").show();
                                } else {
                                    $(this).addClass("search_hide").hide();
                                }
                            });
                        } else {
                            $(this).find('span.item-label, span.item-id').each(function(){
                                if ($(this).text().toLowerCase().indexOf(searchText) > -1) {
                                    match = true;
                                }
                            });
                            $(this).find('span.item-sublabel').each(function(){
                                if ($(this).text().toLowerCase().indexOf(searchText) > -1) {
                                    match = true;
                                }
                            });
                        }
                    }
                    var hasTags = false;
                    if (tagsArr.length > 0) {
                        hasTags = true;
                        for (var i in tagsArr) {
                            var found = false;
                            $(this).find('.nv-tag').each(function(){
                                if ($(this).text().toLowerCase().indexOf(tagsArr[i]) > -1) {
                                    found = true;
                                }
                            });
                            if (!found) {
                                hasTags = false;
                                break;
                            }
                        }
                    }
                    
                    if (match || hasTags) {
                        $(this).removeClass("search_hide").show();
                    } else {
                        $(this).addClass("search_hide").hide();
                    }
                });
            } else {
                $("#builder_canvas").find("li.item").removeClass("search_hide").show();
                $("#builder_canvas").find("li.item .overview_data.active_data.search_hide").removeClass("search_hide").show();
            }
            if (this.value !== "") {
                $(this).next("button").show();
            } else {
                $(this).next("button").hide();
            }
            
            AppBuilder.resizeBuilders();
        });

        $("#builder_canvas").find('.search-container .clear-backspace').off("click");
        $("#builder_canvas").find('.search-container .clear-backspace').on("click", function(){
            $(this).hide();
            $(this).prev("input").val("");
            $("#builder_canvas").find("li.item").removeClass("search_hide").show();
            $("#builder_canvas").find("li.item .overview_data.active_data.search_hide").removeClass("search_hide").show();
            
            AppBuilder.resizeBuilders();
        });
        
        var container = $("#builder_canvas #builders");
        for (var i in data) {
            var builder = data[i];
            if (builder.value === "app") {
                continue;
            }
            var builderDiv = $('<div class="builder-type builder-'+builder.value+'" data-builder-type="'+builder.value+'"><div class="builder-title"><span class="icon" style="background: '+builder.color+'" ><i class="'+builder.icon+'"></i></span> '+builder.label+' <a class="addnew" data-builder-type="'+builder.value+'" title="'+get_cbuilder_msg("cbuilder.addnew")+'"><i class="las la-plus"></i></a></div><div class="ul-wrapper"><ul></ul></div></div>');
            if (builder.theme === 'light' || builder.theme === 'dark') {
                builderDiv = $('<div class="builder-type builder-' + builder.value + '" data-builder-type="' + builder.value + '"><div class="builder-title"><span class="icon" style="color: ' + builder.color + '" ><i class="' + builder.icon + '"></i></span> ' + builder.label + ' <a class="addnew" data-builder-type="' + builder.value + '" title="' + get_cbuilder_msg("cbuilder.addnew") + '"><i class="las la-plus"></i></a></div><div class="ul-wrapper"><ul></ul></div></div>');
            }
            if (builder.elements && builder.elements.length > 0) {
                for (var j in builder.elements) {
                    var action = "";
                    var actionClass= "";
                    var actionTitle= "";
                    var subLabel = "";
                    var itemClass = "";
                    if (CustomBuilder.appPublished !== "true") {
                        actionClass = "not_publish disabled";
                        actionTitle = ' ' +get_cbuilder_msg('abuilder.appNotPublished');
                    }
                    if (builder.value === "userview") {
                        action = '<a class="launch '+actionClass+'" title="'+self.msg('launch')+actionTitle+'"><i class="zmdi zmdi-play"></i></a>';
                    } else if (builder.value === "process") {
                        action = '<a class="runprocess '+actionClass+'" title="'+self.msg('runProcess')+actionTitle+'"><i class="zmdi zmdi-play"></i></a>';
                    }
                    if (builder.elements[j].subLabel !== undefined) {
                        itemClass = "has-sublabel";
                        subLabel = '<span class="item-sublabel">'+builder.elements[j].subLabel+'</span>';
                    }
                    $(builderDiv).find("ul").append('<li class="item '+itemClass+'" data-builder-type="'+builder.value+'" data-id="'+builder.elements[j].id+'"><a class="item-link" href="'+builder.elements[j].url+'" target="_self"><span class="item-label">'+builder.elements[j].label+'</span><span class="item-id">'+builder.elements[j].id+'</span>'+subLabel+'</a><div class="builder-actions">'+action+'<a class="delete" title="'+get_cbuilder_msg('cbuilder.remove')+'"><i class="las la-trash-alt"></i></a></div></li>');
                }
            } else {
                $(builderDiv).find("ul").append('<li class="message">'+self.msg('addNewMessage')+'</li>');
            }
            container.append(builderDiv);
            $("#builders-seperator").append("<span></span>");
        }
        $("#builder_canvas").css("opacity", "1");
        
        Nav.refresh();
        AppBuilder.resizeBuilders();
        
        $(window).off("resize.appbuilder");
        $(window).on("resize.appbuilder",  AppBuilder.resizeBuilders);
        
        setTimeout(function(){
            CustomBuilder.cachedAjax({
                type: "POST",
                url: CustomBuilder.contextPath + '/web/json/console/app' + CustomBuilder.appPath + '/builders/missingPlugins',
                dataType : "json",
                beforeSend: function (request) {
                   request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                },
                success: function(response) {
                    if (response !== undefined && response.result !== undefined && response.result.length > 0) {
                        $(".canvas-header").prepend('<div class="alert alert-warning error missingplugin" role="alert">'+response.error+'<ul></ul></div>');
                        for (var i in response.result) {
                            $(".canvas-header .missingplugin ul").append('<li>'+response.result[i]+'</li>');
                        }
                    }
                }
            });
        },1);
    },
    
    /*
     * Delete the selected item 
     */
    deleteItem : function(item) {
        if (confirm(get_cbuilder_msg("abuilder.deleteConfirmation"))) {
            var id = $(item).attr("data-id");
            var type = $(item).attr("data-builder-type");
            
            Usages.delete(id, type, {
                contextPath: CustomBuilder.contextPath,
                appId: CustomBuilder.appId,
                appVersion: CustomBuilder.appVersion,
                id: id,
                builder: type,
                confirmMessage: get_advtool_msg('dependency.usage.confirmDelete'),
                confirmLabel: get_advtool_msg('dependency.usage.confirmLabel'),
                cancelLabel: get_advtool_msg('dependency.usage.cencelLabel')
            }, function () {
                var callback = {
                    success: function () {
                        //delete from admin bar menu too
                        $(".menu-"+type + " ul li[data-id='"+id+"']").remove();
                        
                        $(item).remove();
                        
                        //delete tags
                        Nav.deleteItem(id, type);
                        
                        CustomBuilder.showMessage(get_cbuilder_msg('abuilder.deleted'), "success");
                    }
                }
                
                var urlType = type;
                if (type !== "userview" && type !== "form" && type !== "datalist") {
                    urlType = "cbuilder/" + type
                }
                ConnectionManager.post(CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/'+urlType+'/delete', callback, 'ids=' + id);
            });
        }
    },
    
    /*
     * Get the data for properties editing view
     */
    getBuilderProperties: function() {
        return CustomBuilder.data;
    },
    
    /*
     * Auto save the properties data to server
     */
    saveBuilderProperties: function(container, properties) {
        var builderProperties = CustomBuilder.getBuilderProperties();
        builderProperties = $.extend(builderProperties, properties);
        CustomBuilder.update();
        CustomBuilder.save();
    },
    
    /*
     * Update the app name if changed
     */
    builderSaved: function() {
        //update app name
        var name = CustomBuilder.data.name + " v" + CustomBuilder.appVersion;
        $("#builderElementName > .title > span").text(name);
    },
    
    /*
     * Action implementation of top panel to publish the app
     */
    publishApp: function() {
        if (confirm(AppBuilder.msg('publishConfirm'))) {
            var callback = {
                success : function() {
                    AppBuilder.updatePublishButton(CustomBuilder.appVersion, false);
                }
            };
            ConnectionManager.post(CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/publish', callback, '');
        }
    },
    
    /*
     * Action implementation of top panel to unpublish the app
     */
    unpublishApp: function() {
        if (confirm(AppBuilder.msg('unpublishConfirm'))) {
            var callback = {
                success : function() {
                    AppBuilder.updatePublishButton(CustomBuilder.appVersion, true);
                }
            };
            ConnectionManager.post(CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/unpublish', callback, '');
        }
    },
    
    updatePublishButton: function(version, isUnpublish) {
        if ((isUnpublish && CustomBuilder.appVersion === version) || // is current app version unpublish or
                (!isUnpublish && CustomBuilder.appVersion !== version)) { //other app version publish
            $("#publish-btn").show();
            $("#unpublish-btn").hide();
            CustomBuilder.appPublished = "false";
            $("#builderElementName .title .published").remove();
        } else if (!isUnpublish && CustomBuilder.appVersion === version) { //current app version publish
            $("#unpublish-btn").show();
            $("#publish-btn").hide();
            CustomBuilder.appPublished = "true";
            $("#builderElementName .title .published").remove();
            $("#builderElementName .title").append('<small class="published">('+AppBuilder.msg('published')+')</small>');
        }
        
        if (!isUnpublish && CustomBuilder.appVersion !== version) { //publish other version
            CustomBuilder.ajaxRenderBuilder(CustomBuilder.contextPath+'/web/console/app/'+CustomBuilder.appId+'/'+version+'/builders');
        } else {
            AppBuilder.reloadVersions();
            AppBuilder.renderBuilders(CustomBuilder.builderItems);
        }
    },
    
    /*
     * reload the versions view after publish/unpublish App
     */
    reloadVersions: function() {
        if ($("#versionsView").length > 0) {
            $("#versionsView iframe")[0].contentWindow.location.reload(true);
        }
    },
    
    /*
     * Action implementation of top panel to show export app dialog
     */
    exportApp: function() {
        JPopup.show("exportAppDialog", CustomBuilder.contextPath + "/web/console/app"+CustomBuilder.appPath+"/exportconfig?", {}, "");
    },
    
    /*
     * Convinient method to retrieve message
     */
    msg: function(key) {
        return CustomBuilder.config.msg[key];
    },
    
    resizeBuilders: function(){
        var builders = $('#builders')[0];
        var rowHeight = ($(window).height() - 270) / 2;
        if ($(window).width() <= 1290) {
            rowHeight = 200;
        }
        var rowGap = parseInt(window.getComputedStyle(builders).getPropertyValue('grid-row-gap'));
        
        $(builders).find("> .builder-type").each(function(){
            var item = this;
            item.style.removeProperty('gridRowEnd');
            var height = $(item).find('ul').outerHeight() + 80;
            var rowSpan = Math.ceil((height+rowGap)/(rowHeight+rowGap));
            item.style.gridRowEnd = "span "+(rowSpan);
        });
    },
    
    showOverview : function(tool, callback) {
        
        $("#undefinedView").html('<i class="dt-loading las la-spinner la-3x la-spin" style="opacity:0.3; margin:30px;"></i>');
        if ($(".item .overview_container").length === 0) {
            $.ajax({
                type: "POST",
                url: CustomBuilder.contextPath + '/web/json/console/app' + CustomBuilder.appPath + '/builders/overview',
                dataType : "json",
                beforeSend: function (request) {
                   request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                },
                success: function(data) {
                    if (data !== undefined && data !== null) {
                        var keys = Object.getOwnPropertyNames(data);
                        for (var i = 0; i < keys.length; i++) {
                            AppBuilder.renderOverview(keys[i], data[keys[i]].data);
                        }
                        
                        //render a toogle for show/hide no data record
                        $("#builder_canvas .canvas-header").append(' <a id="toogle_no_overview_data"><i class="las la-check-square"></i> '+get_cbuilder_msg('abuilder.hideNoDataItems')+'</a>');

                        $("#toogle_no_overview_data").off("click").on("click", function(){
                            $("#builders").toggleClass("show_overview_no_data");
                            $(this).find("i.las").toggleClass("la-check-square").toggleClass("la-stop");
                            
                            setTimeout(function(){
                                AppBuilder.resizeBuilders();
                            }, 10);
                            return false;
                        });
                        
                        //show details in popup dialog with edit link
                        var aceEditor;
                        var aceField;
                        var showDetail = function(detailLink) {
                            var frameBody = $($("iframe#overview_data_more_detail")[0].contentWindow.document).find("body");
                            
                            //set title with builder name & item name
                            var title = $(detailLink).closest('.builder-type').find("> .builder-title").text() + " : ";
                            title += $(detailLink).closest(".item").find("> .item-link > .item-label").text() + " ";
                            $(frameBody).find("#main-body-header").text(title);
                            
                            //edit link
                            var pathLink = $(detailLink).prev().attr("href");
                            $(frameBody).find("#main-body-header").append(' <a class="btn edit_link" style="margin:0px 15px;padding:5px 10px; font-size:70%; vertical-align: middle; line-height: normal;"><i class="fas fa-pencil-square-o" aria-hidden="true"></i> '+get_cbuilder_msg('ubuilder.edit')+'</a>');
                            $(frameBody).find("#main-body-header .edit_link").on("click", function(){
                                CustomBuilder.ajaxRenderBuilder(pathLink);
                                return false;
                            });
                            
                            //adjust width & height
                            var width = UI.getPopUpWidth("");
                            var height = UI.getPopUpHeight("");
                            $("iframe#overview_data_more_detail").css("width", width + "px");
                            $("iframe#overview_data_more_detail").css("height", height + "px");
                            $(frameBody).find("#main-body-content").css("height", (height - 53) + "px");
                            
                            //create code editor to show code
                            $(frameBody).find("#main-body-content").html('<pre id="code_detail" class="ace_editor" style="width:100%; height:100%"></pre>');
                            aceField = aceEditor.edit("code_detail");
                            aceField.getSession().setTabSize(4);
                            if (CustomBuilder.systemTheme === 'dark') { //support builder theme
                                aceField.setTheme("ace/theme/vibrant_ink");
                            } else {
                                aceField.setTheme("ace/theme/textmate");
                            }
                            aceField.setReadOnly(true);
                            aceField.setAutoScrollEditorIntoView(true);
                            
                            //update content
                            var content = $(detailLink).find(".more_detail_content").text();
                            aceField.setValue(content, -1);
                            aceField.resize();
                            aceField.renderer.updateFull();
                            
                            //show the popup
                            JPopup.dialogboxes["overview_data_more_detail"].show();
                            UI.adjustPopUpDialog(JPopup.dialogboxes["overview_data_more_detail"]);
                        };
                        
                        $("#builders")
                            .off("click.overviewDetail", ".overview_data .more_detail")
                            .on("click.overviewDetail", ".overview_data .more_detail", function(){
                                var detailLink = $(this);
                                if ($("iframe#overview_data_more_detail").length === 0) {
                                    JPopup.create("overview_data_more_detail", "", "", "");
                                    $("iframe#overview_data_more_detail")[0].src = CustomBuilder.contextPath+'/builder/popup.jsp';
                                    $("iframe#overview_data_more_detail").on("load", function(){
                                        var frameBody = $($("iframe#overview_data_more_detail")[0].contentWindow.document).find("body");
                                        $(frameBody).find("#main-body-content").css("padding", "0px");
                                        
                                        //wait for ace editor available
                                        while (!aceEditor) {
                                            aceEditor = $("iframe#overview_data_more_detail")[0].contentWindow.ace;
                                        }
                                        
                                        showDetail(detailLink);
                                    });
                                } else {
                                    showDetail(detailLink);
                                }
                            });
                        
                        if (callback) {
                            callback();
                        } else {
                            AppBuilder.afterRenderOverview(tool);
                        }
                    }
                }
            });
        } else {
            if (callback) {
                callback();
            } else {
                AppBuilder.afterRenderOverview(tool);
            }
        }
    },
    
    renderOverview : function(key, data) {
        var ids = key.split(":"); // bulderType:id
        var item = $('.item[data-builder-type="'+ids[0]+'"][data-id="'+ids[1]+'"]');
        
        $(item).find('.overview_container').remove();
        
        $(item).append('<ul class="overview_container"></ul>');
        var container = $(item).find(".overview_container");
        
        //clear search
        $(".clear-backspace").hide();
        $(".clear-backspace").prev("input").val("");
        $("#builder_canvas").find("li.item").show();
        $("#builder_canvas").find("li.item .overview_data").removeClass("active_data search_hide").show();
        
        var url = $(item).find('.item-link').attr("href");
        
        for (var i = 0; i < data.length; i++) {
            var li = $('<li class="overview_data" data-tool="'+data[i].tool+'"></li>');
            var label = data[i].label;
            if (label === undefined || label === null || label === "") {
                if (data[i].content.indexOf("\n") === -1) {
                    label = data[i].content;
                    data[i].content = "";
                } else {
                    var tempContent = data[i].content.trim();
                    var index = tempContent.indexOf("\n"); //show only first line
                    if (index !== -1) {
                        label = tempContent.substring(0, index);
                    }
                    if (label === "") {
                        label = tempContent.substring(0, 80);
                    } else if (label.length > 80) {
                        label = label.substring(0, 80);
                    }
                }
            }
            
            var pathUrl = url;
            if (pathUrl.indexOf("#") !== -1) {
                pathUrl = pathUrl.substring(0, pathUrl.indexOf("#")) + '?overview_path='+encodeURIComponent(data[i].path) + pathUrl.substring(pathUrl.indexOf("#"));
            } else {
                pathUrl += '?overview_path='+encodeURIComponent(data[i].path);
            }
            
            var badge = "";
            if (data[i].badge !== null && data[i].badge !== undefined && data[i].badge !== "") {
                var badgeColor = (data[i].badgeColor !== null && data[i].badgeColor !== undefined && data[i].badgeColor !== "")?data[i].badgeColor:"#17a2b8";
                
                badge = '<span class="badge" style="border:1px solid;font-size:60%;display:inline-block;font-weight:900;vertical-align:middle; color:'+badgeColor+';border-color:'+badgeColor+';">'+UI.escapeHTML(data[i].badge)+'</span> ';
            }
            
            li.append('<a class="path_link" href="'+pathUrl+'" target="_self">'+ badge + UI.escapeHTML(label)+'</a>');
            
            if (data[i].content !== undefined && data[i].content !== null && data[i].content !== "" && data[i].content !== label) {
                li.append('<a class="more_detail"><i class="las la-comment"></i><div class="more_detail_content" style="max-height:500px;">'+UI.escapeHTML(data[i].content)+'</div></a>');
            }
            if (data[i].isError) {
                li.addClass("error");
            }
            
            container.append(li);
        }
        
        container.append('<li class="overview_data no_record">'+get_cbuilder_msg('abuilder.noDataFound')+'</li>');
    },
    
    /**
     * Convert space, newline & tab char to HTML
     */
    prettyFormat : function (content) {
        content = content.replaceAll('\n', '<br/>');
        content = content.replaceAll(' ', '&nbsp;');
        content = content.replaceAll('\t', '&nbsp;&nbsp;&nbsp;&nbsp;');
        
        return content;
    },
    
    afterRenderOverview : function (tool) {
        $("#undefinedView").remove();
        
        $("body").addClass("overview_view");
        $('.item .overview_container .overview_data').hide();
        
        $('.item .overview_container').each(function(){
            if ($(this).find('.overview_data[data-tool="'+tool+'"]').length > 0) {
                $(this).find('.overview_data[data-tool="'+tool+'"]').addClass("active_data").show();
            } else {
                $(this).find('.overview_data.no_record').show();
                $(this).closest(".item").addClass("no_overview_data");
            }
        });
        
        $(".item .overview_container").show();
        
        AppBuilder.resizeBuilders();
    },
    
    overviewViewBeforeClosed : function() {
        $("#undefinedView").remove();
        $('.item').removeClass("no_overview_data");
        $(".item .overview_container").hide();
        $('.item .overview_container .overview_data').hide();
        $("body").removeClass("overview_view");
        
        //clear search
        $(".clear-backspace").hide();
        $(".clear-backspace").prev("input").val("");
        $("#builder_canvas").find("li.item").show();
        $("#builder_canvas").find("li.item .overview_data").removeClass("active_data search_hide").show();
        
        setTimeout(function(){
            AppBuilder.resizeBuilders();
        }, 10);
    },
    
    overviewMapViewInit : function(view) {
        var header = $(view).prev();
        $(header).html("");
        $(header).append('<i class="dt-loading las la-spinner la-3x la-spin" style="opacity:0.3; position:absolute; z-index:2000; margin:30px;"></i>');
        $(header).append('<div class="sticky-buttons" style="z-index:3;"><button id="mmCollapseAll" class="btn button btn-secondary">'+get_cbuilder_msg('cbuilder.collapseAll')+'</button> <button id="mmExpandAll" class="btn button btn-secondary">'+get_cbuilder_msg('cbuilder.expandAll')+'</button> <button id="mmScreenshot" class="btn button btn-secondary" style="display:none;">'+get_cbuilder_msg('cbuilder.screenshot')+'</button></div>');
        
        $(view).html("");
        $(view).attr("id", "jsmind_container");
        $(view).css("overflow", "auto");
        $(view).css("padding", "0px");
        
        loadCSS(CustomBuilder.contextPath + "/js/jsmind/jsmind.css");
        loadScript(CustomBuilder.contextPath + "/js/jsmind/dom-to-image.min.js");
        loadScript(CustomBuilder.contextPath + "/js/jsmind/jsmind.js", function(){
            loadScript(CustomBuilder.contextPath + "/js/jsmind/jsmind.screenshot.js", function(){
                //load overview data first before render the map
                AppBuilder.showOverview("", function(){
                    setTimeout(function(){
                        $('.item .overview_container .overview_data').hide();
                        var name = $("#builderElementName .title").text();

                        var mind = {
                            "meta":{
                                "name":CustomBuilder.appId,
                                "author":"joget.com",
                                "version":"0.2"
                            },
                            "format":"node_tree",
                            "data":{"id":CustomBuilder.appId,"topic":name,"children":[]}
                        };
                        var options = {                     
                            container:'jsmind_container',   
                            editable:true,                  
                            theme:'clouds',
                            view:{
                                hmargin:200,
                                vmargin:100,
                                line_width:1
                            },
                            layout:{
                                hspace:50
                            }
                        };
                        if (CustomBuilder.systemTheme === 'dark') { //support builder theme
                            options['theme'] = 'asphalt';
                        }
                        var jm = new jsMind(options);
                        jm.show(mind);

                        //buttons handling
                        $("#mmCollapseAll").off("click").on("click", function(){
                            jm.collapse_all();
                        });

                        $("#mmExpandAll").off("click").on("click", function(){
                            jm.expand_all();
                        });

                        //prepare for screenshot
                        //Note: screenshot is an experimental feature of jsmind
                        $("#mmScreenshot").off("click").on("click", function(){
                            jm.shoot();
                        });
                        $("#mmScreenshot").show();

                        //loop all builders
                        $("#builders .builder-type").each(function(){
                            if ($(this).find('.ul-wrapper ul li.item').length > 0) {
                                var id = $(this).data("builder-type");
                                var title = $(this).find('.builder-title').text();
                                var color = $(this).find('.builder-title .icon').css("background-color");
                                if (CustomBuilder.systemTheme === 'light' || CustomBuilder.systemTheme === 'dark') { //support builder theme
                                    color = $(this).find('.builder-title .icon').css("color");
                                }
                                var icon = $(this).find('.builder-title .icon').html().replace('<i', '<i style="color:'+color+';"');

                                jm.add_node(CustomBuilder.appId, id, icon + title, {}, "right");

                                //loop items
                                $(this).find('.ul-wrapper ul li.item').each(function(){
                                    var item = $(this);
                                    var itemId = id + "_" + $(this).data("id");
                                    var itemTitle = $(this).find('.item-label').text();
                                    var itemUrl = $(this).find('a.item-link').attr("href");

                                    jm.add_node(id, itemId, icon + ' <a href="'+itemUrl+'">' + itemTitle + '</a>', {}, "right");

                                    //loop overview plugins
                                    $("#builderToolbar .advanced-tools [data-overview]").each(function(){
                                        var overviewId = itemId + $(this).attr("id");
                                        var overviewClass = $(this).data("overview");
                                        var overviewTitle = $(this).html() + " " + $(this).attr("title");
                                        if ($(item).find('.overview_container .overview_data[data-tool="'+overviewClass+'"]').length > 0) {
                                            jm.add_node(itemId, overviewId, overviewTitle, {}, "right");

                                            //loop overview data
                                            var i = 0;
                                            $(item).find('.overview_container .overview_data[data-tool="'+overviewClass+'"]').each(function(){
                                                var dataLabel = $(this).find('a.path_link').html();
                                                var dataUrl = $(this).find('a.path_link').attr("href");
                                                
                                                jm.add_node(overviewId, overviewId+"_"+i++, '<a href="'+dataUrl+'">' + dataLabel + '</a>', {}, "right");
                                            });

                                            jm.collapse_node(overviewId);
                                        }
                                    });

                                    jm.collapse_node(itemId);
                                });

                                jm.collapse_node(id);
                            }
                        });

                        //disable further editing
                        jm.disable_edit();

                        //remove loading icon
                        $(header).find(".dt-loading").remove();
                    }, 200);    
                });
            });
        });
    },
    
    /*
     * remove dynamically added items    
     */            
    unloadBuilder : function() {
        $(".advanced-tools [data-overview], #overviewmap-btn").remove();
        $("#unpublish-btn, #publish-btn, #versions-btn, #app-info").remove();
        $("#design-btn").attr("title", get_cbuilder_msg("cbuilder.design")).find("span").text(get_cbuilder_msg("cbuilder.design"));
        $("#export-btn").parent().remove();
        $('#save-btn').show();
        $('.btn-group.tool').css('display', 'inline-block');
        $("#builder_canvas").css("opacity", "1");
        $(window).off("resize.appbuilder");
    }
};