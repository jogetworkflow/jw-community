{   
    contextPath : "%s",
    appId : "%s",
    appVersion : "%s",
    template : "<style>div#mega-menu-container-{id} > .row > [class^=\"col\"]:last-child {padding-right: 0px;} div#mega-menu-container-{id} > .row > [class^=\"col\"] {padding-right: {col-gutter} !important; } div#mega-menu-container-{id} > .row + .row {margin-top: {row-gutter}}</style><div id='mega-menu-container-{id}' class='mega-menu-container container-fluid' style='background-color:{background-color};padding-top: {padding-top}; padding-right: {padding-right}; padding-bottom: {padding-bottom}; padding-left: {padding-left}; min-height: {min-height}'><div class='{row}'></div></div>",
    repeatIndex : 0,
    overriddenMenu : [],
    menuObject : [],
    debounce : function(func, wait) {
        let timeout;
        return function (...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    },
    defaultPadding: "20px",
    defaultRowGutter: "10px",
    defaultColGutter: "10px",
    sampleMenuTemplate : function(){
        var thisObj = this;
        return `<a href="" class="text-body">
                    <div class="row mb-4 border-bottom pb-2">
                        <div class="col-3">
                        <img src="` + thisObj.contextPath + `/images/loginTemplate/building.jpg"
                            class="img-fluid shadow-1-strong rounded"/>
                        </div>
                        <div class="col-9">
                        <p class="mb-2">
                            <strong>Lorem ipsum dolor sitamet</strong>
                        </p>
                        <p><u>15.07.2020</u></p>
                        </div>
                    </div>
                </a>`
    },
    getData: function(useDefault) {
        var data = new Object();

        var value = thisObj.megaMenuProperties;
        
        data[this.properties.name] = value;

        PropertyEditor.Util.retrieveHashFieldValue(this, data);

        return data;
    },
    renderField : function() {
        var thisObj = this;
        var html = "<a href='#' id='";
        html += thisObj.id;
        html += "'> @@userview.userviewcategory.configureMegaMenu@@ </a>";
        return html;
    }
    ,
    initScripting: function() {
        var thisObj = this;
        thisObj.createModal(this.id);

        $(".megaMenuModal#" + this.id).parent(".megaMenuWrapper").hide();

        $("#"+this.id).on("click", function(){
            $(".megaMenuModal#"+this.id).parent(".megaMenuWrapper").show();

            if ($(".megaMenuModal#" + this.id).find(".property-editor-container").length === 0) {
                //Generate Menu
                thisObj.generateMenu($("body .megaMenuModal#" + this.id + " div#menuBar"));
                //Set up options
                var options = {
                    showCancelButton : false,
                    autoSave: true,
                    closeAfterSaved: false,
                    contextPath: thisObj.contextPath,
                    saveCallback: function() {
                        var key = Object.keys(this.propertyValues)[0];
                        const value = this.propertyValues;

                        // Ensure megaMenuProperties is an object
                        thisObj.megaMenuProperties = thisObj.megaMenuProperties || thisObj.value || {};

                        if (key === "useGlobalValues") {
                            key = "general-layout";
                        }
                        
                        if (!thisObj.megaMenuProperties[key]) {
                            thisObj.megaMenuProperties[key] = {};
                        }
                        // Update or add the key in megaMenuProperties
                        Object.entries(value).forEach(([k, val]) => {
                            thisObj.megaMenuProperties[key][k] = val;
                        });

                        if (key  === 'general-layout' && thisObj.megaMenuProperties[key]["useGlobalValues"] && thisObj.megaMenuProperties[key]["useGlobalValues"] === "") {
                            thisObj.megaMenuProperties[key][template] = "";
                        }
                        
                        CustomBuilder.update();
                        if (key  !== 'general-layout') {
                            thisObj.updateSample($(".megaMenuModal#" + thisObj.id).find("#layoutPreview"), thisObj.megaMenuProperties);
                        }
                    }
                };

                $(".megaMenuModal#"+this.id).find("div#main-content").prepend("<div class=\"mega-menu header\"><span>@@userview.userviewcategory.megaMenu@@</span></div>")

                //Generate Setting
                thisObj.generateGeneralLayout($("body .megaMenuModal#" + this.id + " div#main-content"), options);
                thisObj.generateMenuEditor($("body .megaMenuModal#" + this.id + " div#main-content"), options);
                
                $(".megaMenuModal#"+this.id).find("#menuBar > ul li:first-child").addClass("selected");

                $(".megaMenuModal#" + this.id).siblings(".close-btn").off("mouseenter").on("mouseover", function(){
                    $(this).css({color : 'red'});
                }).on("mouseleave", function(){
                    $(this).css({color : ''})
                })
                $(".megaMenuModal#" + this.id).siblings(".close-btn").appendTo($(".megaMenuModal#"+this.id).find("div#main-content div.mega-menu.header"));
            }

            //Restore preview
            if (thisObj.value != "" && $(".megaMenuModal#" + thisObj.id).find("#layoutPreview").html() === "") {
                thisObj.updateSample($(".megaMenuModal#" + thisObj.id).find("#layoutPreview"), thisObj.value);
            }
        })
    },
    createModal: function(id) {
        var thisObj = this;

        var width = 0.75 * $(window).width();
        var height = 0.75 * $(window).height();

        $("<div class='megaMenuWrapper'>" +
            "<div id='" + id + "' class='megaMenuModal row'> <div id='menuBar' class='col-3'></div><div id='main-content' class='col-9'></div></div>" +
            "<button class='close-btn'>&times;</button>" +
        "</div>").appendTo($("body"));

        // Style the modal itself
        $(".megaMenuModal#" + id).css({
            'width': width,
            'height': height
        });

        // Close button click event to hide the modal
        $(".megaMenuModal#" + id).siblings(".close-btn").on("click", function () {
            $(".megaMenuModal#" + id).parent(".megaMenuWrapper").hide();
        });
    },
    generateMenu : function(container) {
        var thisObj = this;
        var categoryId = this.properties.propertyEditorObject.editorObject.initialValues.id;
        
        if ($(container).html().trim() === "") {
            $(container).append("<ul></ul>");

            var element = CustomBuilder.Builder.frameBody;

            $(container).find("ul").append("<li id='general-layout'>@@userview.userviewcategory.generalSettings.title@@</li>");

            $(element).contents().find("li#" + categoryId + " ul.menu-container > li.menu").each(function(index, menu) {
                var span = $(menu).find("span:not('.badge')").text().trim();
                var id = $(menu).attr('id');

                thisObj.overriddenMenu.push(span);
                $(container).find("ul").append("<li id='" + id  + "' class='mega-menu' data-attribute='" + span + "'>Menu: " + span + "</li>");
            });

            $(container).find("ul li").off("click").on("click", function(){
                if($(this).attr('id') === 'general-layout') {
                    thisObj.megaMenuProperties = thisObj.megaMenuProperties || thisObj.value || {};

                    if (!Object.keys(thisObj.megaMenuProperties).length === 0) {
                        thisObj.updateSample($(".megaMenuModal#" + thisObj.id).find("#layoutPreview"), thisObj.megaMenuProperties);
                    }                    
                }

                var currentSelectedId = $(container).find("ul li.selected").attr('id');
                $(container).find("ul li.selected").removeClass("selected");
                $("body .megaMenuModal#" + thisObj.id + " div#main-content").find(".property-editor-container."+currentSelectedId).removeClass("current");

                $(this).addClass("selected");
                $("body .megaMenuModal#" + thisObj.id + " div#main-content").find(".property-editor-container." + $(this).attr('id')).addClass("current");
            })
        }
    },
    generateGeneralLayout : function(container, options) {
        var thisObj = this;
        options["editorPanelMode"] = true;
        options.propertiesDefinition = [
            {
                title: "@@userview.userviewcategory.globalSettings.title@@",
                properties: []
            },
            {
                title: "@@userview.userviewcategory.configureGeneralLayout@@",
                properties: [
                    {
                        label: "general-layout",
                        name: "general-layout",
                        type: "hidden"
                    }
                ]
            }
        ];

        //Generate Column Select box
        var col = {
            label: "@@userview.customLogin.tag.columns@@",
            name: "col",
            required: "True", 
            type: "selectbox",
            options: [],
            value: thisObj.value?.["general-layout"]?.col || "3"
        };
        for (var i = 0; i < 12; i++) {
            var obj = {
                value: ""+ (i + 1),
                label: ""+ (i + 1)
            };
            col.options.push(obj);
        }   
        options.propertiesDefinition[1].properties.push(col);
        
        var propertyPage = 1;
        thisObj.generalLayoutProperties().forEach((prop) => {
                if (prop.name === "useGlobalValues") {
                    propertyPage = 0;   
                }

                options.propertiesDefinition[propertyPage].properties.push(prop)
            }
        );

        $(container).propertyEditor(options);
        $(container).find("div[property-name='general-layout']").closest(".property-editor-container").addClass("general-layout current");
        
        //Add descrption
        $(container).find(".property-editor-property.property-type-checkbox[property-name='useGlobalValues'] span.hidden_label").append("<span title='@@userview.userviewcategory.useGlobalValues.description@@'> <i class='fas fa-info-circle'></i></span>")
        $(container).find(".property-editor-property.property-type-checkbox[property-name='menuPicker'] span.hidden_label").append("<span title='@@userview.userviewcategory.menuPicker.description@@'> <i class='fas fa-info-circle'></i></span>")

        const observer = new MutationObserver((mutations, obs) => {
            const editorElement = $(container).find("div.editor pre")[0];
            if (editorElement) {
                obs.disconnect(); 
                
                const debouncedChange = thisObj.debounce(function () {
                    const globalTemplate = editor.getValue();
                    
                    if (thisObj.megaMenuProperties?.["general-layout"]?.useGlobalValues === "true" && 
                        globalTemplate !== "") {
                        var changeAllMenuTemplate = (thisObj.megaMenuProperties?.["general-layout"]?.menuPicker === 'true');
                        thisObj.overriddenMenu.forEach(function(menuItem, index){
                            var menuElement = $("body .megaMenuModal#" + thisObj.id + " div#menuBar ul li.mega-menu[data-attribute='" + menuItem + "']");
                            var id = $(menuElement).attr('id')

                            var editor = ace.edit($("body .megaMenuModal#" + thisObj.id + " div#main-content").find(".property-editor-container." + id).find("div.template_editor_container div.editor pre")[0]);
                            
                            if (editor.getValue() !== "" && !changeAllMenuTemplate) {
                                return;
                            }

                            var globalTemplateTemp = globalTemplate;
                            var menuTitle = $(CustomBuilder.Builder.frameBody).contents().find("li#"+id + " a span").eq(0).text().trim();
                            var icon = $(CustomBuilder.Builder.frameBody)
                            .contents()
                            .find("li#" + id + " a span")
                            .eq(0)
                            .find("i")
                            .prop("outerHTML") || "<i class='zmdi zmdi-menu'></i> ";
                            
                            if (globalTemplate.includes("{{icon||")) {
                                globalTemplateTemp = globalTemplateTemp.replace(/\{\{icon\|\|([^}]*)\}\}/g, "{{icon||" + icon + "}}");
                                globalTemplateTemp = globalTemplateTemp.replace(/\{\{title\|\|([^}]*)\}\}/g, "{{title||" + menuTitle + "}}");
                            } else {
                                var globalTemplateTemp = globalTemplateTemp.replace(/\{\{title\|\|([^}]*)\}\}/g, "{{title||" + icon + " " + menuTitle + "}}");
                            }
                            
                            thisObj.value = thisObj.value || {};
                            thisObj.value["mega-menu-" + id] = thisObj.value["mega-menu-" + id] || {};

                            if (globalTemplateTemp.includes("{{repeat||[")) {
                                globalTemplateTemp = globalTemplateTemp.replace(/{{repeat\|\|(\[[\s\S]*?\])}}/, function(match) {
                                    const newRepeatPart = JSON.stringify(thisObj.value["mega-menu-" + id]["repeat"]);
                                    if (newRepeatPart === undefined) {
                                        return match;
                                    }
                                    return `{{repeat||${newRepeatPart}}}`;
                                });
                            }
                            
                            globalTemplateTemp = globalTemplateTemp.replace(/\{\{([^}|]+)(?:\|\|([^}]*)?)?\}\}/g, (match, key, defaultValue) => {
                                if (match === "{{repeat}}" || match.includes("repeat.")) {
                                    return match;
                                }else if (defaultValue === undefined) {
                                    return "{{" + key + "||}}";
                                }
                    
                                return (thisObj.value["mega-menu-" + id]?.hasOwnProperty(key) && thisObj.value["mega-menu-" + id]?.[key] !== "")
                                    ? "{{" + key + "||" + thisObj.value["mega-menu-" + id][key] + "}}"
                                    : match
                            })

                            editor.setValue(globalTemplateTemp);

                            thisObj.value["mega-menu-" + id] = thisObj.value["mega-menu-" + id] || {};
                            thisObj.value["mega-menu-" + id].template = globalTemplateTemp;
                            thisObj.menuObject[index].save();
                        })
                        CustomBuilder.update();

                        thisObj.updateSample($(".megaMenuModal#" + thisObj.id).find("#layoutPreview"), thisObj.megaMenuProperties);
                    }
                }, 300);

                var editor = ace.edit(editorElement);
     
                editor.getSession().on("change", debouncedChange);
            }
        });
        
        observer.observe($(container)[0], { childList: true, subtree: true });

        var html = $("<div id='layoutPreview'></div>");
        
        $(container).find("div.property-editor-container.general-layout .property-editor-property-container").css({'height' : '' })
        $(container).find("div.property-editor-container.general-layout").find(".property-editor-pages > .property-editor-page-buffer").before(html);
        
        function handleChange(event) {
            const key = $(this).closest("div[property-parentid]").attr('property-name');
            var value = $(this).val();

            if ($(this).attr("type") === "number" && event.target.tagName === "INPUT") {
                value += $(this).siblings("select").val();
            } else if (
                event.target.tagName === "SELECT" &&
                $(this).siblings("input").attr("type") === "number" &&
                $(this).siblings("input").css("display") === "none"
            ) {
                value = "auto";
            } else if ($(this).attr("type") === "checkbox") {
                value = "" + $(this).is(':checked');
            }
            // Ensure megaMenuProperties is an object
            thisObj.megaMenuProperties = thisObj.megaMenuProperties || thisObj.value || {};
            
            //Ensure general-layout exists
            thisObj.megaMenuProperties['general-layout'] = thisObj.megaMenuProperties['general-layout'] || {};

            // Update or add the key in megaMenuProperties
            thisObj.megaMenuProperties['general-layout'][key] = value;

            thisObj.updateSample($(".megaMenuModal#" + thisObj.id).find("#layoutPreview"), thisObj.megaMenuProperties);
        }

        $(container).find("div[property-parentid] > .property-input > select")
        .not(function() {
            return $(this).closest("div[property-name='repeat']").length > 0;
        })
        .on("change", handleChange);
        $(container).find("div[property-parentid] > .property-input > input").not(function() {
            return $(this).closest("div[property-name='repeat']").length > 0;
        })
        .on("change focusout", handleChange);

        $(container).find("div[property-name] input[type='checkbox']").on("change", handleChange);

        $(container).on("blur", "div[property-name='repeat'] input", function() {
            var repeaterRowIndex = $(this).closest(".repeater-row").index();

            // Ensure megaMenuProperties exists and initialize default values
            thisObj.megaMenuProperties = thisObj.megaMenuProperties || thisObj.value || {};
            thisObj.megaMenuProperties['general-layout'] = thisObj.megaMenuProperties['general-layout'] || {};
            thisObj.megaMenuProperties['general-layout']['repeat'] = thisObj.megaMenuProperties['general-layout']['repeat'] || [{}];
        
            while (thisObj.megaMenuProperties['general-layout']['repeat'].length <= repeaterRowIndex) {
                thisObj.megaMenuProperties['general-layout']['repeat'].push({});
            }

            // Set the value for the corresponding repeat entry based on closest element's property-name
            var propertyName = $(this).closest("div[property-parentid]").attr('property-name');
            if (propertyName) {
                thisObj.megaMenuProperties['general-layout']['repeat'][repeaterRowIndex][propertyName] = $(this).val();
            }
           
            // Call updateSample with the updated properties
            thisObj.updateSample($(".megaMenuModal#" + thisObj.id).find("#layoutPreview"), thisObj.megaMenuProperties);
        });
        
    },
    updateSample : function(container, data) {
        var thisObj = this;
        var template = thisObj.template;
        var megaMenuContainer = $(".megaMenuModal#" + this.id);
        var css_style = ""
        if (data?.["general-layout"]) {
            for (const [key, value] of Object.entries(data["general-layout"])) {
                template = template.replace("{" + key + "}", value);
            }

            ["left", "right", "top", "bottom"].forEach(side => {
                const paddingValue = data["general-layout"]?.[`padding-${side}`];
                if (!paddingValue) {
                    template = template.replaceAll(`{padding-${side}}`, thisObj.defaultPadding);
                }
            });            

            if (data["general-layout"]?.["display-full-height"] === "true") {
                template = template.replace("{min-height}", $(megaMenuContainer).find("#layoutPreview").height() + 'px');
            } else if (data["general-layout"]?.["display-full-height"] === "" && data["general-layout"]?.["containerHeight"] !== "") {
                template = template.replace("{min-height}", data["general-layout"]?.["containerHeight"]);
            } else {
                template = template.replace("{min-height}", "fit-content");
            }

            if (data["general-layout"]?.["col-gutter"] && data["general-layout"]?.["col-gutter"] !== "") {
                template = template.replaceAll("{col-gutter}", data["general-layout"]?.["col-gutter"]);
            } else {
                template = template.replaceAll("{col-gutter}", thisObj.defaultColGutter);
            }

            if (data["general-layout"]?.["row-gutter"] !== "") {
                template = template.replaceAll("{row-gutter}", data["general-layout"]?.["row-gutter"]);
            } else {
                template = template.replaceAll("{row-gutter}", "");
            }
        }

        template = template.replace("{css-style}", css_style);
        
        // Replace ID Placeholder
        template = template.replaceAll("{id}", thisObj.id);

        // Replace the row placeholder
        template = template.replace("{row}", "row");

        // Convert the template string to a DOM structure
        var templateDom = $("<div>").html(template); 
        var preview = templateDom.find("#mega-menu-container-"+thisObj.id);

        // Clear existing content in the layout preview
        $(preview).empty();
        var colLimit = parseInt(data?.["general-layout"]?.["col"]) ? parseInt(data?.["general-layout"]?.["col"]) : 3;
        
        var rowLimit = colLimit !== 1 ? Math.ceil(($(megaMenuContainer).find("#menuBar li").length - 1) / colLimit) : ($(megaMenuContainer).find("#menuBar li").length - 1);
        for (var i = 0; i < rowLimit; i++) {
            var row_template = $("<div class='row" + "' id='rowNo" + i + "'></div>");
            $(preview).append(row_template);
        }

        // Add columns dynamically based on the number of columns in data["col"]
        var col = 0;
        var useRepeat = false;
        var repeatIndex = 0;
        var autoCol = false;
        for (var r = 0; r < rowLimit; r++) {
            if (colLimit === 'auto') {
                colLimit = $(megaMenuContainer).find("#menuBar li").length - 1;
                autoCol = true;
            } 
            for (var c = 0; c < colLimit; c++) {
                var colId = col
                if ($(megaMenuContainer).find("#menuBar li").eq(col + 1).length > 0) {
                    colId = $(megaMenuContainer).find("#menuBar li").eq(col + 1).attr('id');
                } else {
                    break;
                };
                
                var menuTemplate = "";
                if (data && data["general-layout"]?.["useGlobalValues"] === "true") {
                    var currentMenu = $(CustomBuilder.Builder.frameBody).contents().find("li#"+colId);
                    if ($(currentMenu).length > 0) {
                        ({ menuTemplate, useRepeat } = thisObj.applyData(menuTemplate, data, colId, repeatIndex, useRepeat, currentMenu))

                        // Increment repeatIndex if useRepeat is true
                        if (useRepeat === true) {
                            repeatIndex++;
                            useRepeat = false
                        }
                        menuTemplate = thisObj.trimKeys(menuTemplate);
                    }
                    
                } else if (data && data["mega-menu-" + colId]?.template) {
                    menuTemplate = thisObj.trimKeys(data["mega-menu-" + colId].template);
                }
                var col_template;
                if (autoCol){
                    var rowGutter = data["general-layout"]?.["row-gutter"] || thisObj.defaultRowGutter;
                    col_template = $("<div class='col-auto' style='margin-bottom:" + rowGutter + "'id='colNo-" + colId + "'>" + menuTemplate + "</div>");
                }else{
                    col_template = $("<div class='col' id='colNo-" + colId + "'>" + menuTemplate + "</div>");
                }
                $(preview).find("div#rowNo"+r).append(col_template);
                col += 1;
            }
        }

        if (data && data["general-layout"]?.["useCustomFooter"] === "true") {
            var customFooter = data["general-layout"]?.["footerText"] || "";
            var customFooterLink = data["general-layout"]?.["footerTextLink"] || "";
            var customFooterColor = data["general-layout"]?.["footerTextColor"] || "";
            
            var footerTemplate = $("<div class='megaMenuCustomFooter row justify-content-center mt-5'><a href='" + customFooterLink + "' style='color:" + customFooterColor + ";width:fit-content;'>" + customFooter + "</a></div>");

            templateDom.find('div.mega-menu-container').append(footerTemplate);
        }        

        container.html(templateDom.html());
    },
    applyData : function(menuTemplate, data, colId, repeatIndex, useRepeat, currentMenu) {
        menuTemplate = data["mega-menu-" + colId]?.template ? data["mega-menu-" + colId]?.template : (ace.edit($("div.property-editor-container.general-layout div[property-name='template'] pre")[0]).getValue() || "")
        
        menuTemplate = menuTemplate.replace(/\{\{([^}|]+)\|\|([^}]*)\}\}/g, (match, key, defaultValue) => {
            return data["mega-menu-" + colId]?.hasOwnProperty(key) && data["mega-menu-" + colId]?.[key] !== ""
                ? (key !== "image" ? data["mega-menu-" + colId][key] : data["mega-menu-" + colId][key] !== defaultValue ? data["mega-menu-" + colId][key] : match)
                : match
        })

        // Replace with Global Values
        menuTemplate = menuTemplate.replace(/\{\{([^}|]+)\|\|([^}]*)\}\}/g, (match, key, defaultValue) => {
            if (data["general-layout"]?.hasOwnProperty(key) && (!data["mega-menu-" + colId]?.hasOwnProperty(key) || (data["mega-menu-" + colId]?.hasOwnProperty(key) && data["mega-menu-" + colId]?.[key] === ""))) {
                return data["general-layout"][key]
            }
            // Check if "repeat" exists and is an array`
            if (Array.isArray(data["general-layout"]?.repeat) && !data["mega-menu-" + colId]?.hasOwnProperty(key)) {
                if (repeatIndex < data["general-layout"].repeat.length) {
                    if (data["general-layout"].repeat[repeatIndex]?.hasOwnProperty(key)) {
                        useRepeat = true
                        if (key === "image") {
                            return "{{" + key + "||" + data["general-layout"].repeat[repeatIndex][key] + "}}"
                        }
                        return data["general-layout"].repeat[repeatIndex][key]
                    }
                }
            }
    
            return match
        })
    
        var menuTitle = $(currentMenu).find("a span").eq(0).text().trim()
        var icon = $(currentMenu)
            .find("a span")
            .eq(0)
            .find("i")
            .prop("outerHTML") || "<i class='zmdi zmdi-menu'></i> ";

        icon = icon ? icon + " " : "";


        if (menuTemplate.includes("{{icon||")){
            menuTemplate = menuTemplate.replace(/{{icon\|\|.*?}}/g, icon);
            menuTemplate = menuTemplate.replace(/{{title\|\|.*?}}/g, menuTitle);
        }else {
            menuTemplate = menuTemplate.replace(/{{title\|\|.*?}}/g, icon + menuTitle);
        }
    
        if (!data["mega-menu-" + colId]?.hasOwnProperty("header")) {
            menuTemplate = menuTemplate.replace(/{{header\|\|.*?}}/g, $(currentMenu).find("span:not('.badge')").text())
        }

        return { menuTemplate, useRepeat }
    },
    trimKeys : function(template) {
        var thisObj = this;
        
        let repeatRegex = /{{repeat\|\|(\[.*?\])}}([\s\S]*?){{repeat}}/g;

        template = template.replace(repeatRegex, (fullMatch, arrayContent, repeatContent) => {
            let repeatArray = JSON.parse(arrayContent); 
            let repeatHtml = '';
            repeatArray.forEach(item => {
                let currentHtml = repeatContent.replace(/{{repeat\.(\w+)}}/g, (match, key) => {
                    return item[key] || '';
                });
                repeatHtml += currentHtml;
            });

            return repeatHtml;
        });

        template = template.replace(
            /\{\{image\|\|#appResource\.([^#]+)#\}\}/g,
            CustomBuilder.contextPath + "/web/app" + CustomBuilder.appPath+ "/resources/" + "$1"
        );

        template = template.replace(/#request\.contextPath#/g, CustomBuilder.contextPath);
        
        template = template.replace(/\{\{[^{}|]+\|\|([^{}]+)\}\}/g, "$1")

        return template;
    },
    generateMenuEditor : function(container, options) {
        var thisObj = this;

        $("body .megaMenuModal#" + thisObj.id + " div#menuBar ul li.mega-menu").each(function(){
            var id = $(this).attr('id');

            const keyToFind = "mega-menu-"+id;
            const value = thisObj.value?.[keyToFind]?.template || "";

            var props = [{
                            label: "mega-menu-" + id,
                            name: "mega-menu-" + id,
                            type: "hidden"
                        },
                        {
                            name: "template",
                            enableAutoReload: "true",
                            label: "@@userview.customLogin.chooseLoginTemplate@@",
                            type: "custom",
                            script_url: thisObj.contextPath + "/web/console/app/" + thisObj.appId + thisObj.appVersion + "/userview/megaMenuTemplateEditor?version=@@build.number@@",
                            mode: "html",
                            value: value,
                            "tags" : [
                                        {"value" : "text", "label" : "@@userview.userviewcategory.megaMenu.tags.text@@"},
                                        {"value" : "icon", "label": "@@userview.userviewcategory.megaMenu.tags.icon@@"},
                                        {"value" : "image", "label": "@@userview.userviewcategory.megaMenu.tags.image@@"},
                                        {"value" : "header", "label": "@@userview.userviewcategory.megaMenu.tags.header@@"},
                                        {"value" : "list", "label": "@@userview.userviewcategory.megaMenu.tags.list@@"}
                                    ]
                        }]
        
            thisObj.templateProperties(id).forEach((prop) => props.push(prop));

            options.propertiesDefinition = [{
                title: "Configure Menu: <b>" + $(this).text().replace(/^Menu:\s*/, '') + "</b>",
                properties: props
            }];
            options["editorPanelMode"] = false;
            var menuObj = $(container).propertyEditor(options);

            thisObj.menuObject.push(menuObj.data('editor'));

            const observer = new MutationObserver((mutations, obs) => {
                const editorElement = $(container).find("div[property-name='mega-menu-" + id  + "']").closest(".property-editor-container").find("div.editor pre")[0];
                if (editorElement) {
                    obs.disconnect(); 
                    let isUpdating = false;

                    const debouncedChange = thisObj.debounce(function () {
                        
                        var template = editor.getValue();
                        
                        if(template === "" || isUpdating){
                            isUpdating = false;
                            return;
                        }

                        var menuTitle = $(CustomBuilder.Builder.frameBody).contents().find("li#"+id + " a span").eq(0).text().trim();
                        var icon = $(CustomBuilder.Builder.frameBody)
                        .contents()
                        .find("li#" + id + " a span")
                        .eq(0)
                        .find("i")
                        .prop("outerHTML") || "<i class='zmdi zmdi-menu'></i> ";
                        
                        if (template.includes("{{icon||")) {
                            template = template.replace(/\{\{icon\|\|([^}]*)\}\}/g, "{{icon||" + icon + "}}");
                            template = template.replace(/\{\{title\|\|([^}]*)\}\}/g, "{{title||" + menuTitle + "}}");
                        } else {
                            template = template.replace(/\{\{title\|\|([^}]*)\}\}/g, "{{title||" + icon + " " + menuTitle + "}}");
                        }

                        editor.setValue(template, {silent: true});
                        isUpdating = true;

                        thisObj.value = thisObj.value || {};
                        thisObj.value["mega-menu-" + id] = thisObj.value["mega-menu-" + id] || {};
                        thisObj.value["mega-menu-" + id].template = template;

                        CustomBuilder.update();    
                    }, 300);

                    var editor = ace.edit(editorElement);
                    editor.getSession().on("change", debouncedChange);
                };
            });
            observer.observe($(container).find("div[property-name='mega-menu-" + id  + "']").closest(".property-editor-container")[0], { childList: true, subtree: true });

            $(container).find("div[property-name='mega-menu-" + id  + "']").closest(".property-editor-container").addClass("mega-menu " + id);
        })
    },
    generalLayoutProperties : function() {
        var thisObj = this;

        var props = [
                    {
                        label: "@@userview.customLogin.bgColor@@",
                        name: "background-color", 
                        type: "color",
                        value: thisObj.value?.["general-layout"]?.["background-color"] || ""
                    },
                    {
                        label: "@@userview.userviewcategory.displayInFullHeight@@",
                        name: "display-full-height", 
                        type : 'checkbox',
                        options : [{
                            value : 'true',
                            label : ''
                        }],
                        value : thisObj.value?.["general-layout"]?.["display-full-height"] || ""
                    },
                    {
                        label: "@@userview.userviewcategory.containerHeight@@",
                        name: "containerHeight", 
                        type : "number",
                        mode : "css_unit",
                        control_field : 'display-full-height',
                        control_value : '',
                        value: thisObj.value?.["general-layout"]?.["containerHeight"] || ""
                    },
                    {
                        label: "@@userview.userviewcategory.paddingTop@@",
                        name: "padding-top", 
                        type : "number",
                        mode : "css_unit",
                        value: thisObj.value?.["general-layout"]?.["padding-top"] || thisObj.defaultPadding
                    },
                    {
                        label: "@@userview.userviewcategory.paddingBottom@@",
                        name: "padding-bottom", 
                        type : "number",
                        mode : "css_unit",
                        value: thisObj.value?.["general-layout"]?.["padding-bottom"] || thisObj.defaultPadding
                    },
                    {
                        label: "@@userview.userviewcategory.paddingLeft@@",
                        name: "padding-left", 
                        type : "number",
                        mode : "css_unit",
                        value: thisObj.value?.["general-layout"]?.["padding-left"] || thisObj.defaultPadding
                    },
                    {
                        label: "@@userview.userviewcategory.paddingRight@@",
                        name: "padding-right", 
                        type : "number",
                        mode : "css_unit",
                        value: thisObj.value?.["general-layout"]?.["padding-right"] || thisObj.defaultPadding
                    },
                    {
                        label: "@@userview.userviewcategory.rowGutter@@",
                        name: "row-gutter", 
                        type : "number",
                        mode : "css_unit",
                        value: thisObj.value?.["general-layout"]?.["row-gutter"] || thisObj.defaultRowGutter
                    },
                    {
                        label: "@@userview.userviewcategory.colGutter@@",
                        name: "col-gutter", 
                        type : "number",
                        mode : "css_unit",
                        value: thisObj.value?.["general-layout"]?.["col-gutter"] || thisObj.defaultColGutter
                    },
                    {
                        name : 'useCustomFooter', 
                        label : '@@userview.userviewcategory.useCustomFooter@@', 
                        type : 'checkbox',
                        options : [{
                            value : 'true',
                            label : ''
                        }],
                        value : thisObj.value?.["general-layout"]?.["useCustomFooter"] || ""
                    },
                    {
                        name : 'openNewTab', 
                        label : '@@userview.userviewcategory.openNewTab@@', 
                        type : 'checkbox',
                        options : [{
                            value : 'true',
                            label : ''
                        }],
                        control_field : 'useCustomFooter',
                        control_value : 'true',
                        value : thisObj.value?.["general-layout"]?.["openNewTab"] || ""
                    },
                    {
                        "name" : "footerText",
                        "label" : "@@userviewmenu.corporatiTheme.customFooter@@",
                        "type" : "textfield",
                        "control_field" : 'useCustomFooter',
                        "control_value" : 'true',
                        "required" : "true",
                        "value" : thisObj.value?.["general-layout"]?.["footerText"] || ""
                    },
                    {
                        "name" : "footerTextLink",
                        "label" : "@@userview.userviewcategory.customFooterLink@@",
                        "type" : "textfield",
                        "control_field" : 'useCustomFooter',
                        "control_value" : 'true',
                        "required" : "true",
                        "value" : thisObj.value?.["general-layout"]?.["footerTextLink"] || ""
                    },
                    {
                        "name" : "footerTextColor",
                        "label" : "@@userview.userviewcategory.customFooterLinkColor@@",
                        "type" : "color",
                        "control_field" : 'useCustomFooter',
                        "control_value" : 'true',
                        "required" : "true",
                        "value" : thisObj.value?.["general-layout"]?.["footerTextColor"] || ""
                    },
                    {
                        name : 'useGlobalValues', 
                        label : '@@userview.userviewcategory.useGlobalValues@@', 
                        type : 'checkbox',
                        options : [{
                            value : 'true',
                            label : ''
                        }],
                        value : thisObj.value?.["general-layout"]?.["useGlobalValues"] || ""
                    },
                    {
                        name : 'menuPicker',
                        label : '@@userview.userviewcategory.menuPicker@@',
                        type : 'checkbox',
                        options : [{
                            value : 'true',
                            label : ''
                        }],
                        value : thisObj.value?.["general-layout"]?.["menuPicker"] || "",
                        control_field : 'useGlobalValues',
                        control_value : 'true'
                    },
                    {
                        name: "template",
                        enableAutoReload: "true",
                        label: "@@userview.customLogin.chooseLoginTemplate@@",
                        type: "custom",
                        control_field : 'useGlobalValues',
                        control_value : 'true',
                        required : "true",
                        script_url: thisObj.contextPath + "/web/console/app/" + thisObj.appId + thisObj.appVersion + "/userview/megaMenuTemplateEditor?version=@@build.number@@",
                        mode: "html",
                        value: thisObj.value?.["general-layout"]?.["template"] || "",
                        "tags" : [
                                    {"value" : "text", "label" : "@@userview.userviewcategory.megaMenu.tags.text@@"},
                                    {"value" : "icon", "label": "@@userview.userviewcategory.megaMenu.tags.icon@@"},
                                    {"value" : "image", "label": "@@userview.userviewcategory.megaMenu.tags.image@@"},
                                    {"value" : "header", "label": "@@userview.userviewcategory.megaMenu.tags.header@@"},
                                    {"value" : "list", "label": "@@userview.userviewcategory.megaMenu.tags.list@@"}
                                ]
                    },
                    {
                        "name" : "headerColor",
                        "label" : "@@theme.xadmin.headerColor@@",
                        "type" : "color",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{headerColor[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "required" : "true",
                        "value" : thisObj.value?.["general-layout"]?.headerColor || ""
                    },
                    {
                        "name" : "titleColor",
                        "label" : "@@userview.customLogin.titleColor@@",
                        "type" : "color",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{titleColor[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "required" : "true",
                        "value" : thisObj.value?.["general-layout"]?.titleColor || ""
                    },
                    {
                        "name" : "subtitleColor",
                        "label" : "@@userview.customLogin.subtitleColor@@",
                        "type" : "color",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{subtitleColor[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.["general-layout"]?.subtitleColor || ""
                    },
                    {
                        "name" : "descriptionColor",
                        "label" : "@@userview.userviewcategory.descriptionColor@@",
                        "type" : "color",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{descriptionColor[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.["general-layout"]?.descriptionColor || ""
                    },
                    {
                        "name" : "iconColor",
                        "label" : "@@userview.userviewcategory.iconColor@@",
                        "type" : "color",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{iconColor[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.["general-layout"]?.iconColor || ""
                    }
                    ];

        return props;
    },
    templateProperties : function(id) {
        var thisObj = this;

        const keyToFind = "mega-menu-"+id;
        
        var props = [
                    {
                        "name" : "height",
                        "label" : "@@userview.page.image.height@@",
                        "type" : "number",
                        "mode" : "css_unit",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{height[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.[keyToFind]?.height || ""
                    },
                    {
                        "name" : "width",
                        "label" : "@@userview.page.image.width@@",
                        "type" : "number",
                        "mode" : "css_unit",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{width[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.[keyToFind]?.width || ""
                    },
                    {
                        "name" : "header",
                        "label" : "@@userview.infotile.header@@",
                        "type" : "textfield",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{header[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.[keyToFind]?.header || ""
                    },
                    {
                        "name" : "subtitle",
                        "label" : "@@userview.infotile.subtitle@@",
                        "type" : "textfield",
                        "control_field" : "template",
                        "control_value" : "^[\\s\\S]*\\{\\{subtitle[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex" : "true",
                        "value" : thisObj.value?.[keyToFind]?.subtitle || ""
                    },
                    {
                        "name" : "description",
                        "label" : "@@userview.infotile.description@@",
                        "type" : "textfield",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{description[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.[keyToFind]?.description || ""
                    },
                    {
                        "name" : "image",
                        "label" : "@@userview.infotile.image@@",
                        "type": "image",
                        "appPath": "/" + thisObj.appId + "/" + thisObj.appVersion,
                        "allowInput" : "true",
                        "isPublic" : "true",
                        "imageSize" : "width:80px; height:80px; background-size: contain; background-repeat: no-repeat;",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{image[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "required": "true",
                        "value" : thisObj.value?.[keyToFind]?.image || "#request.contextPath#/images/loginTemplate/italy.jpg"
                    },
                    {
                        "name" : "link",
                        "label" : "@@org.joget.apps.userview.lib.Link.pluginLabel@@",
                        "type" : "textfield",
                        "control_field" : "template",
                        "control_value" : "^[\\s\\S]*\\{\\{link[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex" : "true",
                        "value" : thisObj.value?.[keyToFind]?.link || ""
                    },
                    {
                        "name" : "headerColor",
                        "label" : "@@theme.xadmin.headerColor@@",
                        "type" : "color",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{headerColor[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.[keyToFind]?.headerColor || ""
                    },
                    {
                        "name" : "titleColor",
                        "label" : "@@userview.customLogin.titleColor@@",
                        "type" : "color",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{titleColor[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.[keyToFind]?.titleColor || ""
                    },
                    {
                        "name" : "subtitleColor",
                        "label" : "@@userview.customLogin.subtitleColor@@",
                        "type" : "color",
                        "control_field" : "template",
                        "control_value" : "^[\\s\\S]*\\{\\{subtitleColor[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex" : "true",
                        "value" : thisObj.value?.[keyToFind]?.subtitleColor || ""
                    },
                    {
                        "name" : "descriptionColor",
                        "label" : "@@userview.userviewcategory.descriptionColor@@",
                        "type" : "color",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{descriptionColor[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.[keyToFind]?.descriptionColor || ""
                    },
                    {
                        "name" : "iconColor",
                        "label" : "@@userview.userviewcategory.iconColor@@",
                        "type" : "color",
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{iconColor[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.[keyToFind]?.iconColor || ""
                    },
                    {
                        "name" : "repeat",
                        "label" : "@@userview.infotile.repeat@@",
                        "type" : "repeater",
                        "fields" : [
                            {
                                "name" : "header",
                                "label" : "@@userview.infotile.header@@",
                                "type" : "textfield",
                                "control_field": "template",
                                "control_value": "^[\\s\\S]*\\{\\{repeat\\.header[\\||\\}]{2}[\\s\\S]*$",
                                "control_use_regex": "true",
                                "required" : "true"
                            },
                            {
                                "name" : "title",
                                "label" : "@@userview.infotile.title@@",
                                "type" : "textfield",
                                "control_field": "template",
                                "control_value": "^[\\s\\S]*\\{\\{repeat\\.title[\\||\\}]{2}[\\s\\S]*$",
                                "control_use_regex": "true",
                                "required" : "true"
                            },
                            {
                                "name" : "subtitle",
                                "label" : "@@userview.infotile.subtitle@@",
                                "type" : "textfield",
                                "control_field" : "template",
                                "control_value" : "^[\\s\\S]*\\{\\{repeat\\.subtitle[\\||\\}]{2}[\\s\\S]*$",
                                "control_use_regex" : "true"
                            },
                            {
                                "name" : "description",
                                "label" : "@@userview.infotile.description@@",
                                "type" : "textfield",
                                "control_field": "template",
                                "control_value": "^[\\s\\S]*\\{\\{repeat\\.description[\\||\\}]{2}[\\s\\S]*$",
                                "control_use_regex": "true"
                            },
                            {
                                "name" : "image",
                                "label" : "@@userview.infotile.image@@",
                                "type": "image",
                                "appPath": "/" + thisObj.appId + "/" + thisObj.appVersion,
                                "allowInput" : "true",
                                "isPublic" : "true",
                                "imageSize" : "width:80px; height:80px; background-size: contain; background-repeat: no-repeat;",
                                "control_field": "template",
                                "control_value": "^[\\s\\S]*\\{\\{repeat\\.image[\\||\\}]{2}[\\s\\S]*$",
                                "control_use_regex": "true",
                                "required" : "true",
                                "value" : "#request.contextPath#/images/loginTemplate/italy.jpg"
                            },
                            {
                                "name" : "link",
                                "label" : "@@org.joget.apps.userview.lib.Link.pluginLabel@@",
                                "type" : "textfield",
                                "control_field" : "template",
                                "control_value" : "^[\\s\\S]*\\{\\{repeat\\.link[\\||\\}]{2}[\\s\\S]*$",
                                "control_use_regex" : "true"                            },
                            {
                                "name" : "headerColor",
                                "label" : "@@theme.xadmin.headerColor@@",
                                "type" : "color",
                                "control_field": "template",
                                "control_value": "^[\\s\\S]*\\{\\{repeat\\.headerColor[\\||\\}]{2}[\\s\\S]*$",
                                "control_use_regex": "true",
                                "required" : "true"
                            },
                            {
                                "name" : "titleColor",
                                "label" : "@@userview.customLogin.titleColor@@",
                                "type" : "color",
                                "control_field": "template",
                                "control_value": "^[\\s\\S]*\\{\\{repeat\\.titleColor[\\||\\}]{2}[\\s\\S]*$",
                                "control_use_regex": "true",
                                "required" : "true"
                            },
                            {
                                "name" : "subtitleColor",
                                "label" : "@@userview.customLogin.subtitleColor@@",
                                "type" : "color",
                                "control_field" : "template",
                                "control_value" : "^[\\s\\S]*\\{\\{repeat\\.subtitleColor[\\||\\}]{2}[\\s\\S]*$",
                                "control_use_regex" : "true"
                            },
                            {
                                "name" : "descriptionColor",
                                "label" : "@@userview.userviewcategory.descriptionColor@@",
                                "type" : "color",
                                "control_field": "template",
                                "control_value": "^[\\s\\S]*\\{\\{repeat\\.descriptionColor[\\||\\}]{2}[\\s\\S]*$",
                                "control_use_regex": "true"
                            }
                        ],
                        "control_field": "template",
                        "control_value": "^[\\s\\S]*\\{\\{repeat[\\||\\}]{2}[\\s\\S]*$",
                        "control_use_regex": "true",
                        "value" : thisObj.value?.[keyToFind]?.repeat || ""
                    }];

        return props;
    }
}