AppBuilder = {
    
    /*
     * Intialize the builder, called from CustomBuilder.initBuilder
     */
    initBuilder: function (callback) {
        var self = AppBuilder;
        
        $(".btn-group.tool").hide();
        $("#i18n-btn").before('<a class="btn btn-light" title="'+self.msg('envVariable')+'" id="variables-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="envVariables" href="'+CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/envVariable" data-cbuilder-action="switchView" data-hide-tool=""><i class="word-icon" style="font-size: 75%; font-weight: 350; line-height: 20px; vertical-align: top; display:inline-block; letter-spacing: 0.6px;">{x}</i></a>');
        $("#i18n-btn").after('<a class="btn btn-light" title="'+get_cbuilder_msg('abuilder.resources')+'" id="resources-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="resources" href="'+CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/resources" data-cbuilder-action="switchView"><i class="lar la-file-image"></i> </a>\
            <a class="btn btn-light" title="'+self.msg('pluginDefault')+'" id="plugin-default-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="pluginDefaultProperties" href="'+CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/properties" data-cbuilder-action="switchView"><i class="las la-plug"></i> </a>\
            <a class="btn btn-light" title="'+self.msg('performance')+'" id="performance-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="performance" href="'+CustomBuilder.contextPath+'/web/console/app/'+CustomBuilder.appId+'/performance" data-cbuilder-action="switchView"><i class="las la-tachometer-alt"></i> </a>\
            <a class="btn btn-light" title="'+self.msg('logs')+'" id="logs-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="logViewer" href="'+CustomBuilder.contextPath+'/web/console/app/'+CustomBuilder.appId+'/logs" data-cbuilder-action="switchView"><i class="las la-scroll"></i> </a>');
        
        $("#design-btn").attr("title", get_cbuilder_msg('abuilder.builders')).find("span").text(get_cbuilder_msg('abuilder.builders'));
        $("#design-btn").after('<a class="btn btn-light" title="'+self.msg('versions')+'" id="versions-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="versions" href="'+CustomBuilder.contextPath+'/web/console/app/'+CustomBuilder.appId+'/versioning" data-cbuilder-action="switchView" data-hide-tool=""><i class="la la-list-ol"></i> <span>'+self.msg('versions')+'</span></a>');
        
        $("#save-btn").parent().after('<div class="btn-group mr-1 float-right" style="margin-top:-16px;" role="group"><button class="btn btn-secondary btn-icon" title="'+self.msg('export')+'" id="export-btn" data-cbuilder-action="exportApp"><i class="las la-file-export"></i> <span>'+self.msg('export')+'</span></button></div>');
        
        $('#save-btn').hide();
        $('#save-btn').after(' <button class="btn btn-secondary btn-icon" style="display:none;" title="'+self.msg('unpublish')+'" id="unpublish-btn" data-cbuilder-action="unpublishApp"><i class="las la-cloud-download-alt"></i> <span>'+self.msg('unpublish')+'</span></button>\
            <button class="btn btn-primary btn-icon" style="display:none;" title="'+self.msg('publish')+'" id="publish-btn" data-cbuilder-action="publishApp"><i class="las la-cloud-upload-alt"></i> <span>'+self.msg('publish')+'</span></button>');
        
        $("#builder_canvas").off("click", " li.item a.item-link");
        $("#builder_canvas").on("click", " li.item a.item-link", function(){
            CustomBuilder.ajaxRenderBuilder($(this).attr("href"));
            return false;
        });
        $("#builder_canvas").off("click", " li.item a.delete");
        $("#builder_canvas").on("click", " li.item a.delete", function(){
            AppBuilder.deleteItem($(this).closest(".item"));
            return false;
        });
        $("#builder_canvas").off("click", " li.item a.launch");
        $("#builder_canvas").on("click", " li.item a.launch", function(){
            window.open(CustomBuilder.contextPath+'/web/userview/'+CustomBuilder.appId+'/'+$(this).closest(".item").attr("data-id"));
            return false;
        });
        $("#builder_canvas").off("click", " li.item a.runprocess");
        $("#builder_canvas").on("click", " li.item a.runprocess", function(){
            var url = CustomBuilder.contextPath + '/web/client/app' + CustomBuilder.appPath + '/process/' + $(this).closest(".item").attr("data-id");
            JPopup.show("runProcessDialog", url, {}, "");
            return false;
        });
        $("#builder_canvas").off("click", ".addnew");
        $("#builder_canvas").on("click", ".addnew", function(){
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
    },
    
    /*
     * Render builder and its items to canvas
     */
    renderBuilders: function(data) {
        var self = AppBuilder;
        
        $("#builder_canvas").html('<div class="canvas-header"><div class="search-container"><input class="form-control form-control-sm component-search" placeholder="'+get_cbuilder_msg('cbuilder.search')+'" type="text"><button class="clear-backspace"><i class="la la-close"></i></button></div> <a href="" id="showTags"><i class="las la-tags"></i> <span>'+self.msg('showTag')+'</span></a></div><div id="builders"></div>');
        
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
                        $(this).find('span.item-label').each(function(){
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
            var builderDiv = $('<div class="builder-type builder-'+builder.value+'" data-builder-type="'+builder.value+'"><div style="background: '+builder.color+'" class="builder-title"><i class="'+builder.icon+'"></i> '+builder.label+' <a class="addnew" data-builder-type="'+builder.value+'" title="'+get_cbuilder_msg("cbuilder.addnew")+'"><i class="las la-plus-circle"></i></a></div><div class="ul-wrapper"><ul></ul></div></div>');
            if (builder.elements && builder.elements.length > 0) {
                for (var j in builder.elements) {
                    var action = "";
                    if (CustomBuilder.appPublished === "true" && (builder.value === "userview" || builder.value === "process")) {
                        if (builder.value === "userview") {
                            action = '<a class="launch" title="'+self.msg('launch')+'"><i class="las la-play"></i></a>';
                        } else {
                            action = '<a class="runprocess" title="'+self.msg('runProcess')+'"><i class="las la-play"></i></a>';
                        }
                    }
                    $(builderDiv).find("ul").append('<li class="item" data-builder-type="'+builder.value+'" data-id="'+builder.elements[j].id+'"><a class="item-link" href="'+builder.elements[j].url+'" target="_self"><span class="item-label">'+builder.elements[j].label+'</span></a><div class="builder-actions">'+action+'<a class="delete" title="'+get_cbuilder_msg('cbuilder.remove')+'"><i class="las la-trash-alt"></i></a></div></li>');
                }
            } else {
                $(builderDiv).find("ul").append('<li class="message">'+self.msg('addNewMessage')+'</li>');
            }
            container.append(builderDiv);
        }
        $("#builder_canvas").css("opacity", "1");
        
        Nav.refresh();
        AppBuilder.resizeBuilders();
        
        $(window).off("resize.appbuilder");
        $(window).on("resize.appbuilder",  AppBuilder.resizeBuilders);
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
     * Action implementation of top panel to publish the app
     */
    publishApp: function() {
        if (confirm(AppBuilder.msg('publishConfirm'))) {
            var callback = {
                success : function() {
                    $("#unpublish-btn").show();
                    $("#publish-btn").hide();
                    CustomBuilder.appPublished = "true";
                    AppBuilder.reloadVersions();
                    AppBuilder.renderBuilders(CustomBuilder.builderItems);
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
                    $("#publish-btn").show();
                    $("#unpublish-btn").hide();
                    CustomBuilder.appPublished = "false";
                    AppBuilder.reloadVersions();
                    AppBuilder.renderBuilders(CustomBuilder.builderItems);
                }
            };
            ConnectionManager.post(CustomBuilder.contextPath+'/web/console/app'+CustomBuilder.appPath+'/unpublish', callback, '');
        }
    },
    
    /*
     * reload the versions view after publish/unpublish App
     */
    reloadVersions: function() {
        if ($("#versionsView").length > 0) {
            $("#versionsView iframe")[0].contentWindow.reloadTable();
        }
    },
    
    /*
     * Action implementation of top panel to show export app dialog
     */
    exportApp: function() {
        JPopup.show("exportAppDialog", CustomBuilder.contextPath + "/web/console/app"+CustomBuilder.appPath+"/exportconfig?", {}, "");
    },
    
    logViewerViewBeforeClosed: function(view) {
        view.html("");
    },
    
    /*
     * Custom implementation for i18n editor view
     */
    i18nViewInit : function(view) {
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
                    
                    $(view).find(".i18n_table tbody").prepend('<tr class="even addnew"><td class="label"><a class="addNewKey btn btn-primary btn-sm"><i class="las la-plus-circle"></i> '+get_cbuilder_msg('abuilder.addNewKey')+'</a></td><td class="lang1"></td><td class="lang2"></td></tr>');
                    $(view).find(".i18n_table .addNewKey").on("click", function(){
                        AppBuilder.i18nAddNewKey($(this));
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
    i18nAddNewKey : function(button) {
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
     * Convinient method to retrieve message
     */
    msg: function(key) {
        return CustomBuilder.config.msg[key];
    },
    
    resizeBuilders: function(){
        var builders = $('#builders')[0];
        var rowHeight = parseInt(window.getComputedStyle(builders).getPropertyValue('grid-auto-rows'));
        var rowGap = parseInt(window.getComputedStyle(builders).getPropertyValue('grid-row-gap'));
        var maxHeight = $(window).height() - 270;
        
        $(builders).find("> .builder-type").each(function(){
            var item = this;
            var height = $(item).find('ul').outerHeight() + 54;
            if (height > maxHeight) {
                height = maxHeight;
            }
            var rowSpan = Math.ceil((height+rowGap)/(rowHeight+rowGap));
            item.style.gridRowEnd = "span "+rowSpan;
        });
    },
    
    /*
     * remove dynamically added items    
     */            
    unloadBuilder : function() {
        $("#variables-btn, #resources-btn, #plugin-default-btn, #performance-btn, #logs-btn, #unpublish-btn, #publish-btn, #versions-btn").remove();
        $("#design-btn").attr("title", get_cbuilder_msg("cbuilder.design")).find("span").text(get_cbuilder_msg("cbuilder.design"));
        $("#export-btn").parent().remove();
        $('#save-btn, .btn-group.tool').show();
        $("#builder_canvas").css("opacity", "1");
        $(window).off("resize.appbuilder");
    }
};