(function ($) {
    var Nav = {
        target : null,
        options : null,
        definition : null,
        patch : [],
        init: function(target, options) {
            Nav.target = target;
            Nav.options = options;

            $(Nav.target).off("click", Nav.options.infoBtn);
            $(Nav.target).on("click", Nav.options.infoBtn, function(){    
                Nav.toggleInfo();
                return false;
            });
            
            $(Nav.target).off("click", ".nv-tags .nv-tag-plus");
            $(Nav.target).on("click", ".nv-tags .nv-tag-plus", function(){
                Nav.editTags($(this).closest("li"));
                event.preventDefault();
                event.stopPropagation();
                return false;
            });
            
            $(Nav.target).off("click", ".nv-tags .nv-tag");
            $(Nav.target).on("click", ".nv-tags .nv-tag", function(){
                Nav.searchTag($(this));
                event.preventDefault();
                event.stopPropagation();
                return false;
            });
            
            $.tooltipster.off('close.tags');
            $.tooltipster.on('close.tags', function(event){
                Nav.saveTags();
            });
        },
        refresh: function() {
            var showInfoActive = $.cookie("showInfoActive");
            if (showInfoActive === "true") {
                Nav.showInfo();
            }
        },
        toggleInfo: function() {
            if (!$(Nav.options.infoBtn).hasClass("show")) {
                Nav.showInfo();
                $.cookie("showInfoActive", "true", {
                    path: Nav.options.contextPath + "/"
                });
            } else {
                Nav.hideInfo();
                $.cookie("showInfoActive", "false", {
                    path: Nav.options.contextPath + "/"
                });
            }
        },
        showInfo: function() {
            $(Nav.options.infoBtn).addClass("show");
            Nav.renderTags();
            $(".nv-tags").show();
            $(Nav.options.infoBtn).find("i").attr("class", "fas fa-list-ul");
            $(Nav.options.infoBtn).find("span").text(Nav.options.message.hide);
        },
        hideInfo: function() {
            $(Nav.options.infoBtn).removeClass("show");
            $(".nv-tags").hide();
            $(Nav.options.infoBtn).find("i").attr("class", "fas fa-tags");
            $(Nav.options.infoBtn).find("span").text(Nav.options.message.show);
            if (AppBuilder) {
                AppBuilder.resizeBuilders();
            }
        },
        renderTags: function() {
            if (Nav.definition === null) {
                $.ajax({
                    type: "GET",
                    url: Nav.options.tagUrl,
                    dataType: 'json',
                    success: function (data) {
                        Nav.definition = data;
                        Nav.renderTags();
                    }
                });
                return;
            }
            
            $(".nv-tags .nv-tag").remove();
            $(Nav.target).find("div.builder-type").each(function(){
                var type = $(this).data("builder-type");
                if (type === "datalist") {
                    type = "list"; //for backward compatible
                }
                
                $(this).find("li.item").each(function(){
                    var liobj = $(this);
                    if ($(liobj).find(".nv-tags").length === 0) {
                        $(liobj).prepend('<div class="nv-tags"><label><i class="las la-tags"></i>: </label> <span class="nv-tag-plus" Title="Add Tags"><i class="las la-plus"></i></span></div>');
                    }
                    var id = $(liobj).data("id");
                    
                    var tags = [];
                    if (Nav.definition["datas"] !== undefined && Nav.definition["datas"][type] !== undefined && Nav.definition["datas"][type][id] !== undefined) {
                        tags = Nav.definition["datas"][type][id];
                    }
                    
                    for (var i in tags) {
                        var tinfo = Nav.definition["labels"][tags[i]];
                        if (tinfo !== undefined) {
                            var label = "<span style=\"display:none\">" + tinfo.color + " " + Nav.options.message[tinfo.color] + "</span>";
                            if (tinfo.label !== undefined && tinfo.label !== "") {
                                label = tinfo.label;
                                if (label.indexOf('<') !== -1) { //the label may contains html, need escape it
                                    label = UI.escapeHTML(label);
                                }
                            }
                            $(liobj).find(".nv-tag-plus").before('<span data-id="' + tags[i] + '" class="nv-tag tag-' + tinfo.color + '">' + label + '</span>');
                        }
                    }
                    
                });
            });
            if (AppBuilder) {
                AppBuilder.resizeBuilders();
            }
        },
        editTags: function(li) {
            if ($(".tooltipstered").length > 0) {
                try {
                    $(".tooltipstered").tooltipster("close");
                    $(".tooltipstered").tooltipster("destroy");
                } catch (err) {}
            }
            if ($(Nav.target).find("div#manageTagDiv").length === 0) {
                $(Nav.target).append('<div id="manageTagDiv" style="display:none"></div>');
            }
            
            var id = $(li).data("id");
            var type = $(li).closest("div.builder-type").data("builder-type");
            if (type === "datalist") {
                type = "list"; //for backward compatible
            }
                
            $(Nav.target).find("#manageTagDiv").html('<div id="tooltip-tags"></div>');
            
            var html = '<div class="chooseTags" data-id="'+id+'" data-type="'+type+'"><h4>'+Nav.options.message.tags+' <a class="close"><i class="fas fa-times"></i></a></h4>';
            html += '<div class="tag-options">';
            html += Nav.getTagOptions(id, type);
            html += '</div>';
            html += '<div class="tag-buttons"><a class="createNew btn">'+Nav.options.message.createNew+'</a></div></div>';
            
            $(Nav.target).find("#tooltip-tags").append(html);
            
            if (!$(li).find(".nv-tag-plus").hasClass("tooltipstered")) {
                $(li).find(".nv-tag-plus").attr("data-tooltip-content", "#tooltip-tags");
                $(li).find(".nv-tag-plus").tooltipster({
                    theme: 'tooltipster-shadow',
                    contentCloning: false,
                    interactive : true,
                    trigger : 'click',
                    side : [ 'right', 'left', 'top', 'bottom'],
                    contentAsHTML : true
                });
            }
            
            $(li).find(".nv-tag-plus").tooltipster("open");
            
            $("#tooltip-tags").on("click", "h4 a.close", function(){
                $(".tooltipstered").tooltipster("close");
                return false;
            });
            
            $("#tooltip-tags").on("click", "h4 a.back", function(){
                Nav.back();
                return false;
            });
            
            $("#tooltip-tags").on("click", ".tag-options .nv-tag", function(){
                Nav.toggleTag($(li), $(this));
                return false;
            });
            
            $("#tooltip-tags").on("click", ".tag-options > div > a", function(){
                Nav.editLabel($(this).data("id"));
                return false;
            });
            
            $("#tooltip-tags").on("click", ".tag-buttons .createNew", function(){
                Nav.addLabel();
                return false;
            });
            
            $("#tooltip-tags").on("click", ".tag-buttons .create", function(){
                Nav.saveAddLabel();
                return false;
            });
            
            $("#tooltip-tags").on("click", ".tag-buttons .save", function(){
                Nav.saveEditLabel();
                return false;
            });
            
            $("#tooltip-tags").on("click", ".tag-buttons .delete", function(){
                Nav.removeLabel();
                return false;
            });
            
            $("#tooltip-tags").on("click", ".colors > div", function(){
                $("#tooltip-tags").find(".colors > div").removeClass("checked");
                $(this).addClass("checked");
                return false;
            });
        },
        saveTags: function() {
            if (Nav.patch.length > 0) {
                $.ajax({
                    method: "POST",
                    url: Nav.options.tagUrl + "?_=" + jQuery.now(),
                    data: { patch: JSON.stringify(Nav.patch)},
                    success: function(data) {
                        Nav.definition = data;
                        Nav.renderTags();
                    }
                });
                Nav.patch = [];
            }
        },
        toggleTag : function(li, tag) {
            var id = $(li).data("id");
            var type = $(li).closest("div.builder-type").data("builder-type");
            if (type === "datalist") {
                type = "list"; //for backward compatible
            }
            var tagId = $(tag).data("id");
            var tagObj = Nav.definition["labels"][tagId];
                    
            if ($(tag).hasClass("checked")) {
                Nav.removeTag(li, id, type, tagId);
                $(tag).removeClass("checked");
            } else {
                Nav.addTag(li, tagObj, id, type, tagId);
                $(tag).addClass("checked");
            }
        },
        getTagOptions : function(id, type) {
            var html = "";
            var tags = [];
            if (Nav.definition["datas"] !== undefined && Nav.definition["datas"][type] !== undefined && Nav.definition["datas"][type][id] !== undefined) {
                tags = Nav.definition["datas"][type][id];
            }    
            var labels = Nav.definition["labels"];
            for (var i in labels) {
                var checked = "";
                if ($.inArray(i, tags) !== -1) {
                    checked = "checked";
                }
                var label = "&nbsp;";
                if (labels[i].label !== undefined && labels[i].label !== "") {
                    label = labels[i].label;
                    if (label.indexOf('<') !== -1) { //the label may contains html, need escape it
                        label = UI.escapeHTML(label);
                    }
                }
                html += '<div><div data-id="'+i+'" class="'+checked+' nv-tag tag-'+labels[i].color+'"><span>'+label+'</span><i class="check fas fa-check"></i></div><a data-id="'+i+'"><i class="zmdi zmdi-edit"></i></a></div>';
            }
            return html;
        },
        addTag : function(li, tag, id, type, tagId) {
            if (Nav.definition["datas"][type] === undefined) {
                Nav.definition["datas"][type] = {};
                Nav.patch.push({
                    op : 'add',
                    path : '/datas/'+type,
                    value : {}
                });
            }
            if (Nav.definition["datas"][type][id] === undefined) {
                Nav.definition["datas"][type][id] = [];
                Nav.patch.push({
                    op : 'add',
                    path : '/datas/'+type+'/'+id,
                    value : []
                });
            }
            Nav.definition["datas"][type][id].push(tagId);
            Nav.patch.push({
                op : 'add',
                path : '/datas/'+type+'/'+id+'/-',
                value : tagId
            });
            
            var label = "<span style=\"visibility:hidden\">"+tag.color+" "+Nav.options.message[tag.color]+"</span>";
            if (tag.label !== undefined && tag.label !== "") {
                label = tag.label;
                if (label.indexOf('<') !== -1) { //the label may contains html, need escape it
                    label = UI.escapeHTML(label);
                }
            }
            $(li).find(".nv-tag-plus").before('<span data-id="'+tagId+'" class="nv-tag tag-'+tag.color+'">'+label+'</span>');
        },
        removeTag : function(li, id, type, tagId) {
            var tags = [];
            if (Nav.definition["datas"] !== undefined && Nav.definition["datas"][type] !== undefined && Nav.definition["datas"][type][id] !== undefined) {
                tags = Nav.definition["datas"][type][id];
            }
                    
            var index = $.inArray(tagId, tags);
            if (index !== -1) {
                tags.splice(index, 1);
            }
            Nav.patch.push({
                op : 'remove',
                path : '/datas/'+type+'/'+id+'/'+index
            });
            
            $(li).find('.nv-tag').each(function(){
                if ($(this).data("id") === tagId) {
                    $(this).remove();
                }
            });
        },
        addLabel: function() {
            var html = '<div class="addLabel"><h4><a class="back"><i class="fas fa-chevron-left"></i></a>&nbsp;&nbsp;&nbsp;'+Nav.options.message.createNew+' <a class="close"><i class="fas fa-times"></i></a></h4>';
            html += '<div class="fields">';
            html += '<div class="field-row"><label>'+Nav.options.message.name+'</label><input name="label" type="text" value="" /></div>';
            html += '<div class="field-row"><label>'+Nav.options.message.color+'</label><div class="colors">';
            html += Nav.getColorOptions("red");
            html += '</div></div></div>';
            html += '<div class="tag-buttons"><a class="create btn">'+Nav.options.message.create+'</a></div></div>';
            $("#tooltip-tags").append(html);
            $("#tooltip-tags .chooseTags").hide();
        },
        editLabel: function(tagId) {
            var tag = Nav.definition["labels"][tagId];
            var label = "";
            if (tag.label !== undefined && tag.label !== "") {
                label = tag.label;
                if (label.indexOf('<') !== -1) { //the label may contains html, need escape it
                    label = UI.escapeHTML(label);
                }
            }
            
            var html = '<div class="editLabel" data-id="'+tagId+'"><h4><a class="back"><i class="fas fa-chevron-left"></i></a>&nbsp;&nbsp;&nbsp;'+Nav.options.message.edit+' <a class="close"><i class="fas fa-times"></i></a></h4>';
            html += '<div class="fields">';
            html += '<div class="field-row"><label>'+Nav.options.message.name+'</label><input name="label" type="text" value="'+label+'" /></div>';
            html += '<div class="field-row"><label>'+Nav.options.message.color+'</label><div class="colors">';
            html += Nav.getColorOptions(tag.color);
            html += '</div></div></div>';
            html += '<div class="tag-buttons"><a class="save btn">'+Nav.options.message.save+'</a> <a class="delete btn">'+Nav.options.message.delete+'</a></div></div>';
            $("#tooltip-tags").append(html);
            $("#tooltip-tags .chooseTags").hide();
        },
        getColorOptions : function(selection) {
            var html = '';
            var colors = ["red", "pink", "orange", "yellow", "green", "lime", "blue", "sky", "purple", "black"];
            
            for (var i in colors) {
                var checked = "";
                if (colors[i] === selection) {
                    checked = "checked";
                }
                html += '<div data-value="'+colors[i]+'" class="'+checked+' nv-tag tag-'+colors[i]+'"><i class="check fas fa-check"></i></div>';
            }
            
            return html;
        },
        saveAddLabel: function() {
            var tagId = "t" + jQuery.now();
                    
            var tag = {
                label : UI.escapeHTML($("#tooltip-tags .addLabel input[name=label]").val()),
                color : UI.escapeHTML($("#tooltip-tags .addLabel .nv-tag.checked").data("value"))
            };
            
            Nav.definition["labels"][tagId] = tag;
            
            Nav.patch.push({
                op : 'add',
                path : '/labels/'+tagId,
                value : tag
            });
            
            Nav.back();
        },
        saveEditLabel: function() {
            var tagId = $("#tooltip-tags .editLabel").data("id");
            var tag = {
                label : UI.escapeHTML($("#tooltip-tags .editLabel input[name=label]").val()),
                color : UI.escapeHTML($("#tooltip-tags .editLabel .nv-tag.checked").data("value"))
            };
            
            Nav.definition["labels"][tagId] = tag;
            
            Nav.patch.push({
                op : 'add',
                path : '/labels/'+tagId,
                value : tag
            });
            
            Nav.back();
        },
        removeLabel: function() {
            var tagId = $("#tooltip-tags .editLabel").data("id");
            $(Nav.target).find("div.nv-col").each(function(){
                var type = $(this).attr("id").replace("nv-", "");
                $(this).find("li").each(function(){
                    var liobj = $(this);
                    var id = $(liobj).data("id");
                    
                    var tags = [];
                    if (Nav.definition["datas"] !== undefined && Nav.definition["datas"][type] !== undefined && Nav.definition["datas"][type][id] !== undefined) {
                        tags = Nav.definition["datas"][type][id];
                    }
                    
                    if ($.inArray(tagId, tags) !== -1) {
                        Nav.removeTag(liobj, id, type, tagId);
                    }
                });
            });
            
            delete Nav.definition["labels"][tagId];
            Nav.patch.push({
                op : 'remove',
                path : '/labels/'+tagId
            });
            
            Nav.back();
        },
        back: function() {
            var id = $("#tooltip-tags .chooseTags").data("id");
            var type = $("#tooltip-tags .chooseTags").data("type");
            var html = Nav.getTagOptions(id, type);
            
            $("#tooltip-tags .chooseTags .tag-options").html(html);
            $("#tooltip-tags .chooseTags").show();
            $("#tooltip-tags .editLabel, #tooltip-tags .addLabel").remove();
        },
        deleteItem: function(id, type) {
            if (Nav.definition["datas"] !== undefined && Nav.definition["datas"][type] !== undefined && Nav.definition["datas"][type][id] !== undefined) {
                delete Nav.definition["datas"][type][id];
                Nav.patch.push({
                    op : 'remove',
                    path : '/datas/'+type+"/"+id
                });
                Nav.saveTags();
            }
        },
        searchTag: function(tag){
            var searchTxt = "";
            if ($(tag).find("span").length > 0) {
                searchTxt = $(tag).find("span").text();
                if (searchTxt.indexOf(" ") !== -1) {
                    searchTxt = searchTxt.substring(searchTxt.indexOf(" ") + 1);
                }
            } else {
                searchTxt = $(tag).text();
            }
            var filter = $(Nav.options.search).find("input").val();
            if (filter.indexOf("#"+searchTxt) === -1) {
                if (filter !== "") {
                    filter += " ";
                }
                $(Nav.options.search).find("input").val(filter + "#" + searchTxt);
                $(Nav.options.search).find("input").trigger("change");
            }
        }
    };
    window.Nav = Nav;
}(jQuery));