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
        html += '<div class="actions"><a class="choosetemplate btn button small" style="margin-left:0px;margin-top:5px">@@userview.infotile.chooseTemplate@@</a> <a class="edittemplate btn button small" style="margin-top:5px">@@userview.infotile.editTemplate@@</a> <a style="display:none;margin-top:5px;" class="hideedit btn button small">@@userview.infotile.hideTemplateEditor@@</a>'
        if(thisObj.properties.control_field === "customLogin" || thisObj.properties.enableAutoReload === 'true'){
            html += '<a class="reloadtemplate btn button small" style="margin-top:5px">@@userview.infotile.reloadTemplate@@</a> <div class="reloadMessage toast hide" style="position:fixed;z-index:300;top: 0px;right:50px;margin-top:150px;background-color:green;" role="alert" aria-live="assertive" aria-atomic="true" data-delay="2000"> <div class="toast-header"> <strong class="mr-auto">@@userview.infotile.reloadMessage@@</strong> <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close"> <span aria-hidden="true">&times;</span> </button> </div> </div>';
        }
        html += "</div>";
        html += '<div class="editor" style="margin-top:10px; display:none;"><pre id="' + this.id + '" name="' + this.id + '"></pre></div>';
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
            if(thisObj.properties.control_field === "customLogin" || thisObj.properties.enableAutoReload === 'true'){
                const container = $("#" + thisObj.id).closest('.template_editor_container');

                function handleChange(event){
                    var scroll = $(container).closest(".property-editor-property-container").scrollTop();

                    //Doesnt have scrollbar
                    if (!($(container).closest(".property-editor-property-container").get(0).scrollHeight > $(container).closest(".property-editor-property-container").get(0).clientHeight)){
                        scroll = $(container).closest(".property-editor-pages").scrollTop();
                    }

                    if (event.type === "focusin"){
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
                
                function onChooseFileClick(e) {
                    const $btn = $(e.currentTarget);
                    const $imgChooser = $btn.closest(".property-input").find("input");
                    const currentImage = $imgChooser.val();

                    setTimeout(function () {
                        $("body").find("ul.app_resources > li").off("click.imgChoose").on("click.imgChoose", function () {
                            setTimeout(function () {
                                if (currentImage === undefined || currentImage !== $imgChooser.val()) {
                                    $(container).find('.reloadtemplate').click();
                                }
                                $imgChooser.attr("data-value", $imgChooser.val());
                            }, 100);
                        });
                    }, 100);
                }

                standardSiblings.find("a.choosefile").off("click.loginImagePicker").on("click.loginImagePicker", onChooseFileClick);
                repeater.off("click.loginImagePicker", "[property-name='image'] a.choosefile")
                .on("click.loginImagePicker", "[property-name='image'] a.choosefile", onChooseFileClick);

                standardSiblings.not('[property-name="image"]').find(".property-input input").off("focusin focusout", handleChange).on("focusin focusout", handleChange)

                icon.off("click.handleChange", ".la.la-check").on("click.handleChange", ".la.la-check", function(event){
                    $(this).data('colorValue', $(this).siblings('.color_value').css('display'));
                    setTimeout(function() {
                        handleChange.call(this, event);
                    }.bind(this), 1000);
                })
            }
        });

        if(thisObj.properties.control_field === "customLogin" || thisObj.properties.enableAutoReload === 'true'){
            $(container).find(".reloadtemplate").off("click");
            $(container).find(".reloadtemplate").on("click", function() {
                $(container).find(".sample_preview").html("");
                var parent = $(container).parent().parent();
            
                var dataControlField = $("#"+thisObj.id).closest(".property-input").parent().attr("property-name"); 
                
                var template = thisObj.codeeditor.getValue();
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
                        }else {
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
                    }else if ($(this).attr('type') === 'number' && $(this).siblings("select").length > 0 && $(this).siblings("select").val() === 'auto'){
                        dict[propertyName] = "auto";
                    }
                    else if ($(this).attr('data-value') !== undefined) {
                        var value = $(this).val();

                        if ($(this).attr('type') === 'number' && $(this).siblings("select").length > 0) {
                            var selectValue = $(this).siblings("select").val();
                            
                            value += selectValue; 
                        }  

                        dict[propertyName] = value;
                    }
                })

                template = thisObj.fillStandardVariables(template, dict, "");

                thisObj.codeeditor.setValue(template);
                $(container).find(".reloadMessage").toast();  
                $(container).find(".reloadMessage").toast('show');  
            });
        }
        
        $(container).find(".edittemplate").off("click");
        $(container).find(".edittemplate").on("click", function() {
            $(container).find(".edittemplate").hide();
            $(container).find(".editor").show();
            $(container).find(".hideedit").show();
            thisObj.codeeditor.refresh();
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
        
        this.codeeditor = CodeMirror(document.getElementById(this.id), {
            lineNumbers: true,
            mode: "text",
            matchBrackets: true,
            theme: "default",
            autoRefresh:true,
            gutters: ["CodeMirror-lint-markers", "CodeMirror-linenumbers", "CodeMirror-foldgutter"],
            lint: true,
            historyEventDelay: 100,
            autoCloseTags: true,
            autoCloseBrackets: true,
            foldGutter: true,
            lint: true,
            lineWrapping: true,
            highlightSelectionMatches: {annotateScrollbar: true, minChars: 1},
            extraKeys: {
                "Ctrl-F": function(cm) {
                  cm.execCommand("replace")
                  $('#' + thisObj.id).find(".CodeMirror-advanced-dialog").css({display: 'block'})
                  var offsetTop = "0px"
                  if ($("body #top-panel").length > 0){
                    offsetTop = $("body #top-panel").outerHeight() + "px"
                  }
                  if (thisObj.codeeditor.getOption("fullScreen")){
                    $('#' + thisObj.id).find(".CodeMirror-advanced-dialog").css({position:"fixed", zIndex:"2147483647", top: offsetTop, left:"calc(100%% - 320px)"});
                  }
                  else{
                    $('#' + thisObj.id).find(".CodeMirror-advanced-dialog").css({position:"fixed", top:"0", left:"calc(100%% - 320px)", zIndex:"999", marginTop:"150px"});
                  }
                  $('#' + thisObj.id).find(".CodeMirror-advanced-dialog").draggable()
                },
                "Ctrl-=": function(cm) {
                  cm.increaseFontSize();
                },
                "Ctrl--": function(cm) {
                  cm.decreaseFontSize();
                },
                "Ctrl-/": function(cm) {
                  cm.toggleComment()
                }
            }
        });

        thisObj.codeeditor.execCommand("replace");
        $('#' + thisObj.id).find(".CodeMirror-advanced-dialog").css({display: 'none'});

        this.codeeditor.setValue(this.value);
        this.codeeditor.setOption("mode", "htmlmixed");

        //Set dark theme if dark theme mode is activated
        if ($('body').attr('builder-theme') === "dark") {
            this.codeeditor.setOption("theme", "ayu-mirage");
        }

        //Detect keydown for specific actions, such as f12 to toggle full screen mode, escape
        //to exit full screen mode, and F1 to toggle help panel
        //and Undo and Redo
        $('#' + this.id).on('keydown', function(event) {
            if (event.keyCode == 90 && event.ctrlKey){
                thisObj.codeeditor.execCommand("undo")
                event.preventDefault();
                event.stopPropagation();
            }
            else if (event.keyCode == 89 && event.ctrlKey){
                thisObj.codeeditor.execCommand("redo")
                event.preventDefault();
                event.stopPropagation();
            }
            else if (event.key === "F1" && !thisObj.codeeditor.getOption("fullScreen")) {
                if (panels[panelId]) {
                    //Resets height
                    thisObj.codeeditor.setSize(null, $("#" + thisObj.id).find(".CodeMirror").height()-1)
                    panels[panelId].clear();
                    delete panels[panelId];
                    resetHeight();
                } else {
                    addPanel("top");
                    resetHeight();
                }
                event.preventDefault();
            }else if (event.key === "F1" && thisObj.codeeditor.getOption("fullScreen")) {
                event.preventDefault();
            }else if (event.key === 'F12' || (event.key === 'Escape' && thisObj.codeeditor.getOption("fullScreen"))){
                if (thisObj.codeeditor.getOption("fullScreen")) {
                    thisObj.codeeditor.setOption("fullScreen", false);
                    $('#' + thisObj.id).find(".CodeMirror-advanced-dialog .row.find button:last").click();
                    resetHeight();
                    $('#' + thisObj.id).find(".CodeMirror-advanced-dialog").css({position:"sticky", top:"0px", zIndex:"10"});
                    $('#'+thisObj.id).find(".CodeMirror").css({left: "", top: ""})
                    event.stopPropagation();
                }else{
                    var offsetTop = "0px"
                    var offsetLeft = "0px"
                    if ($("body #top-panel").length > 0){
                        offsetTop = $("body #top-panel").outerHeight() + "px"
                    }
                    if ($("body #quick-nav-bar").length > 0){
                        offsetLeft = $("body #quick-nav-bar").outerWidth()+"px"
                    }
                    thisObj.codeeditor.setOption("fullScreen", true);
                    $('#' + thisObj.id).find(".CodeMirror-advanced-dialog").css({position:"fixed", zIndex:"2147483647", top: offsetTop, left:"calc(100%% - 320px)", marginTop:"0px"})
                    $('#'+thisObj.id).find(".CodeMirror").css({left: offsetLeft, top: offsetTop})
                    $('#' + thisObj.id).find(".CodeMirror-advanced-dialog").draggable()
                }
                event.preventDefault();
            }
        });

        var panels = {};
        var panelId = "";

        function makePanel(where) {
            var node = document.createElement("div");
            var label, div, msg;

            node.id = "panel-" + thisObj.id;
            node.className = "panel " + where;
            
            div = $("<div>")
            msg = get_peditor_msg('peditor.codemirror.helpMessage')
            msg.split(" | ").forEach(el =>{
                div.append($("<span>").text(el))
            })

            label = div.appendTo(node);

            label.css({
                "color": "black",
                "font-size": "12px",
                "padding": "5px 10px",
                "font-weight":"bold",
                "display": "flex",
                "flex-direction": "column"
            })

            $(node).css({
                "background-color":"rgb(255, 250, 143)"
            })

            return node;
        };

        function resetHeight(){
            //Make CodeMirror unscrollable, and height follows the code written 
            $("#" + thisObj.id).find(".CodeMirror").css({"height":"auto", "minHeight":"300px"});
            $("#" + thisObj.id).find(".CodeMirror-scroll").css({"maxHeight":"auto", "minHeight":"300px"});
        }

        function addPanel(where) {
            var node = makePanel(where);
            panelId = "panel-" + thisObj.id;
            panels[panelId] = thisObj.codeeditor.addPanel(node, {position: where, stable: true});
        }
        
        this.codeeditor.setValue(this.value);

        var tooltip = $("<span>").attr('title', get_peditor_msg('peditor.codemirror.tooltipTitle')).append(" <i class=\"zmdi zmdi-info-outline\"></i>");
        
        $("#"+thisObj.id).parent().parent().find(".property-label").append(tooltip);

        resetHeight();

        $("#" + thisObj.id).closest(".property-editor-property-container").siblings(".property-editor-page-title").on("click", function(){
            setTimeout(function(){
                thisObj.codeeditor.refresh();
            }, 1)
        })
        
        let debounceTimer;

        this.codeeditor.on("change", function(){
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(function() {
                thisObj.updateSample();
            }, 300); //Debounce to improve performance
        });

        thisObj.updateSample();
    },
    pageShown: function() {
        this.codeeditor.refresh();
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
            if (thisObj.properties.control_field === "customLogin" || $(thisObj.editor).closest(".megaMenuWrapper").length > 0){
                //Added border for better distinguish between different tempates
                $(tile).css({"border": "1px solid black"})
                if (thisObj.properties.control_field === "customLogin"){
                    $(tile).css("aspect-ratio", "16/9");
                }
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
                if (thisObj.properties.control_field === "customLogin" || $(thisObj.editor).closest(".megaMenuWrapper").length > 0){
                    //Added border for better distinguish between different tempates
                    $(tile).css({"border": "1px solid black"})
                    if (thisObj.properties.control_field === "customLogin") {
                        $(tile).css("aspect-ratio", "16/9");
                    }
                    $(tile).css("padding", "5px");
                }

                $(tile).css("cursor", "pointer");
                
                t_container.append(tile);
            }
            t_container = $(object).find(".templates");

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
        
        var tile = $('<div class="tile" style="padding-left:0;padding-right:0;position:relative;margin:5px;display:inline-block;'+width+'"></div>');
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