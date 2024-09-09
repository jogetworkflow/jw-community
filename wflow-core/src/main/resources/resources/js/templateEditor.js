{
    templates : [%s],
    getData: function(useDefault) {
        var data = new Object();
        if (!this.isHidden()) {
            var value = this.codeeditor.getValue();
            if (value === undefined || value === null || value === "") {
                if (useDefault !== undefined && useDefault &&
                    this.defaultValue !== undefined && this.defaultValue !== null) {
                    value = this.defaultValue;
                }
            }
            data[this.properties.name] = value;
        }
        return data;
    },
    renderField : function() {
        
        var html = '<div class="template_editor_container" style="overflow:hidden;">';
        html += '<div class="actions"><a class="choosetemplate btn button small" style="margin-left:0px;margin-top:5px">@@userview.infotile.chooseTemplate@@</a> <a class="edittemplate btn button small" style="margin-top:5px">@@userview.infotile.editTemplate@@</a> <a style="display:none;margin-top:5px;" class="hideedit btn button small">@@userview.infotile.hideTemplateEditor@@</a><a class="reloadtemplate btn button small" style="margin-top:5px">@@userview.infotile.reloadTemplate@@</a> <div class="reloadMessage toast hide" style="position:fixed;z-index:300;top: 0px;right:50px;margin-top:150px;background-color:green;" role="alert" aria-live="assertive" aria-atomic="true" data-delay="2000"> <div class="toast-header"> <strong class="mr-auto">@@userview.infotile.reloadMessage@@</strong> <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close"> <span aria-hidden="true">&times;</span> </button> </div> </div></div>';
        html += '<div class="editor" style="margin-top:10px; display:none;"><pre id="' + this.id + '" name="' + this.id + '" class="ace_editor"></pre></div>';
        html += '<div class="sample_container" style="margin-top:10px; padding:10px; border:1px solid #ced4da; background:#fff; border-radius:5px; overflow: scroll;"><label>@@userview.infotile.sample@@</label><div class="sample_preview" style="position:relative;"></div></div>';
        html += '</div>';
        
        html += '<style>.property_editor_template_chooser div.tile:after, .sample_preview div.tile:after{content:""; position:absolute; top:0px; left:0px; right:0px; bottom:0px;z-index:100;}</style>';
        
        return html;
    },
    initScripting: function() {
        var thisObj = this;
        var container = $("#" + this.id).closest('.template_editor_container');

        $(container).find(".choosetemplate").off("click");
        $(container).find(".choosetemplate").on("click", function() {
            thisObj.showTemplateChooser();
        });

        $(document).ready(function(){
            const container = $("#" + thisObj.id).closest('.template_editor_container');

            function handleChange(event){
                var scroll = $(container).closest(".property-editor-property-container").scrollTop();

                //Doesnt have scrollbar
                if (!($(container).closest(".property-editor-property-container").get(0).scrollHeight > $(container).closest(".property-editor-property-container").get(0).clientHeight)){
                    scroll = $(container).closest(".property-editor-pages").scrollTop();
                }

                if (event.type === "focusin"){
                    //Check for image change
                    if ($(this).attr("class")==="image" && (($(this).attr("data-value") !== $(this).val() && $(this).attr("data-value") !==  undefined))) {
                        $(container).find('.reloadtemplate').click();
                        
                        if (!($(container).closest(".property-editor-property-container").get(0).scrollHeight > $(container).closest(".property-editor-property-container").get(0).clientHeight)){
                            $(container).closest(".property-editor-pages").scrollTop(scroll);
                        }else{
                            $(container).closest(".property-editor-property-container").scrollTop(scroll);
                        }
                    }

                    //Save initial value for checking
                    $(this).attr("data-value", $(this).val());
                }
                else {                  
                    //Only reload template once the value has been changed
                    if (($(this).attr("data-value") !== $(this).val() && !$(this).hasClass("la-check")) || ($(this).hasClass("la-check") && $(this).data('colorValue') === "none")){
                        $(container).find('.reloadtemplate').click();
                    }
                    
                    if (!($(container).closest(".property-editor-property-container").get(0).scrollHeight > $(container).closest(".property-editor-property-container").get(0).clientHeight)){
                        $(container).closest(".property-editor-pages").scrollTop(scroll);
                    }else{
                        $(container).closest(".property-editor-property-container").scrollTop(scroll);
                    }
                }
            }

            var parent = $(container).parent().parent();

            //Get the siblings
            var siblings = $(parent).siblings();
            //Take only the repeater
            var repeater = siblings.filter('[property-name="repeat"]').first();
            //Exclude repeater, and checkbox
            var standardSiblings = siblings.not('[property-name="repeat"]').not('[property-name="icon"]').filter('[data-control_field="template"]');
            //Icon
            var icon = siblings.filter('[property-name="icon"]');
            
            repeater.off("focusin focusout", ".property-input .repeater-rows-container .repeater-row .inputs .inputs-container div[class^=\"property_container\"]:not(.hidden)[property-name!=\"icon\"] input", handleChange).on("focusin focusout", ".property-input .repeater-rows-container .repeater-row .inputs .inputs-container div[class^=\"property_container\"]:not(.hidden)[property-name!=\"icon\"] input", handleChange);
            
            repeater.off("click.handleChange", ".property-input .repeater-rows-container .repeater-row .inputs .inputs-container div[class^=\"property_container\"]:not(.hidden)[property-name=\"icon\"] .la.la-check", handleChange).on("click.handleChange", ".property-input .repeater-rows-container .repeater-row .inputs .inputs-container div[class^=\"property_container\"]:not(.hidden)[property-name=\"icon\"] .la.la-check", function(event){
                $(this).data('colorValue', $(this).siblings('.color_value').css('display'));
                setTimeout(function() {
                    handleChange.call(this, event);
                }.bind(this), 1000);
            });

            standardSiblings.find(".property-input input").off("focusin focusout", handleChange).on("focusin focusout", handleChange)

            icon.off("click.handleChange", ".la.la-check").on("click.handleChange", ".la.la-check", function(event){
                $(this).data('colorValue', $(this).siblings('.color_value').css('display'));
                setTimeout(function() {
                    handleChange.call(this, event);
                }.bind(this), 1000);
            })
        });

        $(container).find(".reloadtemplate").off("click");
        $(container).find(".reloadtemplate").on("click", function() {
            $(container).find(".sample_preview").html("");
            var parent = $(container).parent().parent();
           
            var dataControlField = $("#"+thisObj.id).closest(".property-input").parent().attr("property-name"); 
            
            var template = thisObj.codeeditor.getSession().getValue();
            var dict = {};
            var arr = [];
            //Get the siblings
            var siblings = $(parent).siblings();
            //Take only the repeater
            var repeater = siblings.filter('[property-name="repeat"]').first();
            //Exclude repeater, and checkbox
            var standardSiblings = siblings.filter(':visible').not('[property-name="repeat"]').filter('[data-control_field="template"]');

            repeater.find(".property-input .repeater-rows-container .repeater-row").each(function(){
                $(this).find('.inputs .inputs-container div[class^=\"property_container\"]:not(.hidden)').each(function(){
                    var propertyName = $(this).attr('property-name');

                    if (propertyName === "icon"){
                        dict[propertyName] = $(this).find('.value i').prop("outerHTML");
                    }else if ($(this).find("input").val() !== ""){
                        dict[propertyName] = $(this).find("input").val();
                    }
                })
            
                arr.push(dict);
                dict = {};
            })

            template = thisObj.fillLoopVariables(template, arr, "");

            dict = {};
            standardSiblings.find('.property-input input').each(function(){
                var propertyName = $(this).closest('div[id^="property_"][data-control_field="' + dataControlField + '"]').attr('property-name');
                if (propertyName === "icon"){
                    dict[propertyName] = $(this).closest('div[id^="property_"][data-control_field="' + dataControlField + '"]').find('.value i').prop('outerHTML');
                }
                else if ($(this).val() !== ""){
                    dict[propertyName] = $(this).val();
                }
            })

            template = thisObj.fillStandardVariables(template, dict, "");

            thisObj.codeeditor.getSession().setValue(template);
            $(container).find(".reloadMessage").toast();  
            $(container).find(".reloadMessage").toast('show');  
        });
        
        $(container).find(".edittemplate").off("click");
        $(container).find(".edittemplate").on("click", function() {
            $(container).find(".edittemplate").hide();
            $(container).find(".editor").show();
            $(container).find(".hideedit").show();
            thisObj.codeeditor.resize();
        });
        
        $(container).find(".hideedit").off("click");
        $(container).find(".hideedit").on("click", function() {
            $(container).find(".editor").hide();
            
            var value = thisObj.codeeditor.getValue();
            if (value !== "") {
                $(container).find(".edittemplate").show();
            }
            $(container).find(".hideedit").hide();
        });
        
        if (this.value === null) {
            this.value = "";
        }
        
        ace.config.set('loadWorkerFromBlob', false);
        this.codeeditor = ace.edit(this.id);
        this.codeeditor.setValue(this.value);
        this.codeeditor.getSession().setTabSize(4);

        if (this.properties.theme !== undefined || this.properties.theme !== "") {
            this.properties.theme = "textmate";
        }
        this.codeeditor.setTheme("ace/theme/" + this.properties.theme);
        if (this.properties.mode !== undefined && this.properties.mode !== "") {
            this.codeeditor.getSession().setMode("ace/mode/" + this.properties.mode);
        }
        if (this.properties.check_syntax !== undefined && this.properties.check_syntax.toLowerCase() === "false") {
            this.codeeditor.getSession().setUseWorker(false);
        }
        this.codeeditor.getSession().on('change', function() {
            thisObj.updateSample();
        });
        this.codeeditor.setOption("maxLines", 1000000); //unlimited, to fix the height issue
        this.codeeditor.setOption("minLines", 10);
        this.codeeditor.resize();
        
        thisObj.updateSample();
    },
    pageShown: function() {
        this.codeeditor.resize();
        this.codeeditor.gotoLine(1);
    },
    updateSample : function() {
        var thisObj = this;
        
        var container = $(thisObj.editor).find("#"+thisObj.id).closest('.template_editor_container');
        $(thisObj.editor).find("#"+thisObj.id).trigger("change");
        
        $(container).find(".sample_preview").html("");
        var value = thisObj.codeeditor.getValue();
        if (value === "") {
            $(container).find(".edittemplate").hide();
        } else {
            if (!$(container).find(".editor").is(":visible")) {
                $(container).find(".edittemplate").show();
            }

            tile = thisObj.getTile(value)
            if (thisObj.properties.control_field === "customLogin"){
                $(tile).css("aspect-ratio", "16/9");
            }
            
            $(container).find(".sample_preview").append(tile);
        }
    },
    showTemplateChooser : function() {
        var thisObj = this;
        
        var height = $(thisObj.editor).height() * 0.95;
        var width = $(window).width() * 0.95;
        if (width > 800) {
            width = 800;
        }
        
        var html = "<div class=\"property_editor_template_chooser container\" style=\"overflow:scroll !important; padding:30px 20px !important; font-family: -apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,'Noto Sans',sans-serif,'Apple Color Emoji','Segoe UI Emoji','Segoe UI Symbol','Noto Color Emoji'; font-size:13px;\"><div class=\"search_tags\" style=\"margin-bottom:15px;\"></div><div class=\"templates row\" style=\"list-style:none;justify-content: flex-start;\"></div></div>";
        var object = $(html);
        
        var tags = $(object).find(".search_tags");
        
        $(tags).append('<button type="button" style="margin:2px;" class="btn btn-outline-primary clear active" data-tag="">@@userview.infotile.tag.all@@</button> ');
        
        //loop and render tags based on configuration
        for (var i in thisObj.properties.tags) {
            $(tags).append('<button type="button" style="margin:2px;" class="btn btn-outline-primary" data-tag="'+UI.escapeHTML(thisObj.properties.tags[i].value)+'">'+UI.escapeHTML(thisObj.properties.tags[i].label)+'</button> ');
        }
        
        //loop 20 first, the others load later
        var r = 0;
        for (; r < 1; r++) {
            var t_container = $(object).find(".templates");
            var tile = thisObj.getTile(thisObj.templates[r]);
            if (thisObj.properties.control_field === "customLogin"){
                //Added border for better distinguish between different tempates
                $(tile).css({"border": "1px solid black"})
                $(tile).css("aspect-ratio", "16/9");
                $(tile).css("padding", "5px");
            }

            $(tile).css("cursor", "pointer");
            t_container.append(tile);
        }
        t_container.append('<div class="dt-loading"><i class=" las la-spinner la-3x la-spin" style="opacity:0.5;"></i></div>');
        
        $(object).dialog({
            autoOpen: false,
            modal: true,
            height: height,
            width: width,
            closeText: '',
            close: function(event, ui) {
                $(object).dialog("destroy");
                $(object).remove();
            }
        });
        $(object).dialog("open");
        
        var search = function() {
            if ($(object).find(".search_tags button:not(.clear).active").length > 0) {
                var tags = [];
                $(object).find(".search_tags button:not(.clear).active").each(function(){
                    tags.push($(this).data('tag'));
                });
                
                $(object).find(".templates > div.tile").each(function() {
                    var tile = $(this);
                    var match = true;
                    for (var t in tags) {
                        if ($(tile).data("tags").indexOf(tags[t]) === -1) {
                            match = false;
                            break;
                        }
                    }
                    
                    if (!match) {
                        $(this).hide();
                    } else {
                        $(this).show();
                    }
                });
            } else {
                $(object).find(".templates > div.tile").show();
            }
        };
        
        $(object).find(".search_tags button").off("click");
        $(object).find(".search_tags button").on("click", function() {
            var current = $(this);
            $(this).toggleClass("active");
            $(object).find(".search_tags button").each(function(){
                if (!$(this).is(current)) {
                    $(this).removeClass("active");
                }
            });
            
            if ($(this).hasClass("clear") && $(this).hasClass("active")) {
                $(object).find(".search_tags button:not(.clear)").removeClass("active");
            } else {
                if ($(object).find(".search_tags button:not(.clear).active").length > 0) {
                    $(object).find(".search_tags button.clear").removeClass("active");
                } else {
                    $(object).find(".search_tags button.clear").addClass("active");
                }
            }
            search();
        });
        
        $(object).find(".search-container .clear-backspace").off("click");
        $(object).find(".search-container .clear-backspace").on("click", function() {
            $(object).find(".search-container input").val("");
            $(object).find(".search-container .clear-backspace").hide();
            $(object).find(".templates > div.tile").show();
        });
        
        $(object).off("click", ".templates > div.tile");
        $(object).on("click", ".templates > div.tile", function() {
            var template = $(this).data('template');
            thisObj.codeeditor.setValue(template);
            
            $(object).dialog("close");
        });
        
        //loop the remaining templates
        setTimeout(function(){
            for (; r < thisObj.templates.length; r++) {
                var t_container = $(object).find(".templates");
                var tile = thisObj.getTile(thisObj.templates[r]);
                if (thisObj.properties.control_field === "customLogin"){
                    //Added border for better distinguish between different tempates
                    $(tile).css({"border": "1px solid black"})
                    $(tile).css("aspect-ratio", "16/9");
                    $(tile).css("padding", "5px");
                }

                $(tile).css("cursor", "pointer");
                
                t_container.append(tile);
            }
            t_container.find(".dt-loading").remove();
            search();
        }, 800);
    },
    getTile : function(template) {
        var thisObj = this;
        var newTemplate = template;
        var width = "";
        var tags = "";
        
        var regexp = new RegExp('<!--(\{[^\}]+\})-->','g');
        var match;

        while ((match = regexp.exec(template)) !== null) {
            newTemplate = newTemplate.replace(match[0], "");
            try {
                var meta = eval("["+match[1]+"]")[0];
                width = "width:" + meta.sampleWidth;
                
                if (meta.tags !== undefined) {
                    tags = meta.tags;
                }
            } catch(err){}
        }
        
        var tile = $('<div class="tile" style="position:relative;margin:5px;display:inline-block;'+width+'"></div>');
        var id = CustomBuilder.uuid();
        newTemplate = newTemplate.replace(/\{\{id\}\}/g, id);
        newTemplate = newTemplate.replace(/\{\{contextPath\}\}/g, CustomBuilder.contextPath);
        newTemplate = newTemplate.replace(/#request\.contextPath#/g, CustomBuilder.contextPath);
        
        newTemplate = thisObj.fillVariables(newTemplate);
        
        $(tile).append(newTemplate);
        $(tile).data("template", template);
        $(tile).data("tags", tags);
        
        return tile;
    },
    fillVariables : function(template, objs, propertyName) {
        var thisObj = this;
        template = thisObj.fillLoopVariables(template, objs, propertyName);
        template = thisObj.fillStandardVariables(template, objs, propertyName);
        return template;
    },
    fillLoopVariables : function(template, objs, propertyName) {
        var thisObj = this;
        var newTemplate = template;
        var regexp = (/\{\{([^\|]+)\|\|(.+)\}\}([\s\S]+)\{\{\1\}\}/gm);
        while ((match = regexp.exec(template)) !== null) {
            var replace = match[0];
            if (replace !== "" && replace !== undefined) {
                var key = match[1];
                var sampleProps = match[2];
                var childtemplate = match[3];

                if (propertyName !== undefined && objs !== undefined){
                    newTemplate = newTemplate.replaceAll(sampleProps, JSON.stringify(objs))
                    
                    return newTemplate
                }

                var cregexp = new RegExp('\{\{'+key+'\.','gm');
                childtemplate = childtemplate.replace(cregexp, "\{\{");

                var value = "";
                try {
                    var arr = eval(sampleProps);
                    for (var i = 0; i < arr.length; i++) {
                        value += thisObj.fillVariables(childtemplate, arr[i], propertyName);
                    }
                } catch (err) {}
                
                newTemplate = newTemplate.replace(replace, value);
            } else {
                break;
            }
        }
        return newTemplate;
    },
    fillStandardVariables : function(template, objs, propertyName) {
        var newTemplate = template;
        var regexp = (/\{\{(.+?)\}\}/g);
        while ((match = regexp.exec(template)) !== null) {
            var key = match[1];
            var value = "";
            if (key.indexOf("||") !== -1) {
                value = match[1].substring(match[1].indexOf("||")+2);
                key = match[1].substring(0, match[1].indexOf("||"));
            }
            if (objs !== undefined && objs[key] !== undefined) {
                value = objs[key];
            }
            if (key === "reload") {
                value = '<a class="reload" style="color:inherit; opacity: 0.6; margin-left:5px;" href=""><i class="fas fa-redo"></i></a>';
            } else if (key === "actions") {
                value = '<div class="dropdown"><a class="text-muted"><i class="fa fa-ellipsis-h"></i></a></div>';
            }
            
            if(propertyName !== undefined && objs !== undefined){
                if (!match[0].includes("repeat")){
                    newTemplate = newTemplate.replace(match[0], "{{"+ key +"||"+value+"}}")
                }
            }else{
                if (value.includes("#appResource")){
                    value = value.replace("#appResource.", CustomBuilder.contextPath+"/web/app"+CustomBuilder.appPath+"/resources/");
                    value = value.slice(0, -1);
                }
                newTemplate = newTemplate.replace(match[0], value);
            }
        }
        return newTemplate;
    }
}
