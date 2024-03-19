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
        
        callback();
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
                        $(this).show();
                    } else {
                        $(this).hide();
                    }
                });
            } else {
                $("#builder_canvas").find("li.item").show();
            }
            if (this.value !== "") {
                $(this).next("button").show();
            } else {
                $(this).next("button").hide();
            }
        });

        $("#builder_canvas").find('.search-container .clear-backspace').off("click");
        $("#builder_canvas").find('.search-container .clear-backspace').on("click", function(){
            $(this).hide();
            $(this).prev("input").val("");
            $("#builder_canvas").find("li.item").show();
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
    
    /*
     * remove dynamically added items    
     */            
    unloadBuilder : function() {
        $("#unpublish-btn, #publish-btn, #versions-btn, #app-info").remove();
        $("#design-btn").attr("title", get_cbuilder_msg("cbuilder.design")).find("span").text(get_cbuilder_msg("cbuilder.design"));
        $("#export-btn").parent().remove();
        $('#save-btn').show();
        $('.btn-group.tool').css('display', 'inline-block');
        $("#builder_canvas").css("opacity", "1");
        $(window).off("resize.appbuilder");
    }
};