AppBuilder = {
    
    /*
     * Intialize the builder, called from CustomBuilder.initBuilder
     */
    initBuilder: function (callback) {
        $(".btn-group.tool").hide();
        $("#i18n-btn").before('<button class="btn btn-light" title="Enviroment Variables" id="variables-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="envVariables" data-cbuilder-action="switchView" data-hide-tool=""><i class="word-icon" style="font-size: 75%; font-weight: 350; margin-top: -2px; vertical-align: top; display:inline-block; letter-spacing: 0.6px;">{x}</i></button>');
        $("#i18n-btn").after('<button class="btn btn-light" title="Resources" id="resources-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="resources" data-cbuilder-action="switchView"><i class="lar la-file-image"></i> </button>\
            <button class="btn btn-light" title="Plugin Default Properties" id="plugin-default-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="pluginDefaultProperties" data-cbuilder-action="switchView"><i class="las la-plug"></i> </button>\
            <button class="btn btn-light" title="Performance" id="performance-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="performance" data-cbuilder-action="switchView"><i class="las la-tachometer-alt"></i> </button>\
            <button class="btn btn-light" title="Logs" id="logs-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="logViewer" data-cbuilder-action="switchView"><i class="las la-scroll"></i> </button>');
        
        $("#design-btn").attr("title", "Items").find("span").text("Items");
        $("#design-btn").after('<button class="btn btn-light" title="Version" id="versions-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="versions" data-cbuilder-action="switchView" data-hide-tool=""><i class="la la-list-ol"></i> <span>Versions</span></button>');
        
        $("#save-btn").parent().after('<div class="btn-group mr-1 float-right" style="margin-top:-16px;" role="group"><button class="btn btn-secondary btn-icon" title="Export" id="export-btn" data-cbuilder-action="exportApp"><i class="las la-file-export"></i> <span>Export</span></button></div>');
        $("#export-btn").on("click", function(){
            
            return false;
        });
        
        $('#save-btn').hide();
        $('#save-btn').after(' <button class="btn btn-secondary btn-icon" style="display:none;" title="Unpublish" id="unpublish-btn" data-cbuilder-action="unpublishApp"><i class="las la-cloud-download-alt"></i> <span>Unpublish</span></button>\
            <button class="btn btn-primary btn-icon" style="display:none;" title="Publish" id="publish-btn" data-cbuilder-action="publishApp"><i class="las la-cloud-upload-alt"></i> <span>Publish</span></button>');
        
        
        $("#builder_canvas").on("click", " li.item a.item-link", function(){
            CustomBuilder.ajaxRenderBuilder($(this).attr("href"));
            return false;
        });
        $("#builder_canvas").on("click", " li.item a.delete", function(){
            AppBuilder.deleteItem($(this).closest(".item"));
            return false;
        });
        $("#builder_canvas").on("click", " li.item a.launch", function(){
            window.open(CustomBuilder.contextPath+'/web/userview/'+CustomBuilder.appId+'/'+$(this).closest(".item").attr("data-id"));
            return false;
        });
        $("#builder_canvas").on("click", " li.item a.runprocess", function(){
            JPopup.show("runProcessDialog", url, {}, "");
            return false;
        });
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
        $("#builder_canvas").html('<div class="canvas-header"><div class="search-container"><input class="form-control form-control-sm component-search" placeholder="'+get_cbuilder_msg('cbuilder.search')+'" type="text"><button class="clear-backspace"><i class="la la-close"></i></button></div> <a href="" id="showTags"><i class="las la-tags"></i> <span>Show Tags</span></a></div><div id="builders"></div>');
        
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
                    var hasTags = true;
                    if (tagsArr.length > 0) {
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
            var builderDiv = $('<div class="builder-type builder-'+builder.value+'" data-builder-type="'+builder.value+'"><div style="background: '+builder.color+'" class="builder-title"><i class="'+builder.icon+'"></i> '+builder.label+' <a class="addnew" data-builder-type="'+builder.value+'" title="'+get_cbuilder_msg("cbuilder.addnew")+'"><i class="las la-plus-circle"></i></a></div><ul></ul></div>');
            if (builder.elements) {
                for (var j in builder.elements) {
                    var action = "";
                    if (CustomBuilder.appPublished === "true" && (builder.value === "userview" || builder.value === "process")) {
                        if (builder.value === "userview") {
                            action = '<a class="launch" title="Launch Userview"><i class="las la-play"></i></a>';
                        } else {
                            action = '<a class="runprocess" title="Run Process"><i class="las la-play"></i></a>';
                        }
                    }
                    $(builderDiv).find("ul").append('<li class="item" data-builder-type="'+builder.value+'" data-id="'+builder.elements[j].id+'"><a class="item-link" href="'+builder.elements[j].url+'" target="_self"><span class="item-label">'+builder.elements[j].label+'</span></a><div class="builder-actions">'+action+'<a class="delete" title="Delete"><i class="las la-trash-alt"></i></a></div></li>');
                }
            }
            container.append(builderDiv);
        }
        $("#builder_canvas").css("opacity", "1");
        
        CustomBuilder.renderBuilderMenu(data);
        
        Nav.refresh();
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
     * Get a property defintion to edit in properties view
     */
    getPropertiesDefinition: function() {
        return [];
    },
    
    getBuilderProperties: function() {
        
    },
    
    saveBuilderProperties: function() {
        
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
    }
};