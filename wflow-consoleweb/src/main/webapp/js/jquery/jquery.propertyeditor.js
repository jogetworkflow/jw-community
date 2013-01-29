(function($) {
    var loadOptionsStack = new Array();
    var pageValidationStack = new Array();
    var validationProgressStack = new Array();
    var optionsStack = new Array();
    var elementStack = new Array();
    var tinyMceInitialed = false;

    $.fn.extend({

        propertyEditor : function(options){
            var defaults = {
                contextPath : '',
                tinyMceScript : '',
                propertiesDefinition : null,
                propertyValues : null,
                defaultPropertyValues : null,
                saveCallback : null,
                cancelCallback : null,
                validationFailedCallback : null,
                saveButtonLabel : get_peditor_msg('peditor.ok'),
                cancelButtonLabel : get_peditor_msg('peditor.cancel'),
                nextPageButtonLabel : get_peditor_msg('peditor.next'),
                previousPageButtonLabel : get_peditor_msg('peditor.prev'),
                showCancelButton: false,
                closeAfterSaved: true,
                showDescriptionAsToolTip: false,
                mandatoryMessage: get_peditor_msg('peditor.mandatory'),
                skipValidation:false
            }

            var o =  $.extend(defaults, options);

            $.ajaxSetup ({
                cache: false
            }); 

            return this.each(function() {
                var editorId = 'property_' + uuid();
                var html = '<div id="' + editorId + '" class="property-editor-container" style="position:relative;">' ;
                optionsStack[editorId] = o;

                if(o.propertiesDefinition == undefined || o.propertiesDefinition == null){
                    html += renderNoPropertyPage(editorId, o);
                }else{
                    $.each(o.propertiesDefinition, function(i, page){
                        html += renderPage(editorId, i, page, o, '', '');
                    });
                }

                html += '</div>';

                $(this).append(html);

                var editor = $(this).find('div#'+editorId);
                loadOptions(editor);

                $(editor).find('.property-page-hide, .property-type-hidden').hide();
                $(editor).find('.property-page-show').hide();
                $(editor).find('.property-page-show:first').show();
                $(editor).find('.property-page-show:first').addClass("current");
                $(editor).find('.property-page-show:first .property-editor-page-button-panel .page-button-navigation .page-button-prev').attr("disabled","disabled");
                $(editor).find('.property-page-show:last .property-editor-page-button-panel .page-button-navigation .page-button-next').attr("disabled","disabled");

                if(o.tinyMceScript != ''){
                    $(editor).find('.tinymce').tinymce({
                        // Location of TinyMCE script
                        script_url : o.tinyMceScript,

                        // General options
                        convert_urls : false,
                        theme : "advanced",
                        plugins : "layer,table,save,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,contextmenu,paste,noneditable,xhtmlxtras,template,advlist",

                        // Theme options
                        theme_advanced_buttons1 : "cleanup,code,|,undo,redo,|,cut,copy,paste|,search,replace,|,bullist,numlist,|,outdent,indent",
                        theme_advanced_buttons2 : "bold,italic,underline,strikethrough,|,forecolor,backcolor,|,justifyleft,justifycenter,justifyright,justifyfull,|,sub,sup,|,insertdate,inserttime,charmap,iespell",
                        theme_advanced_buttons3 : "formatselect,fontselect,fontsizeselect,|,hr,removeformat,blockquote,|,link,unlink,image,media",
                        theme_advanced_buttons4 : "tablecontrols,|,visualaid,insertlayer,moveforward,movebackward,absolute",
                        theme_advanced_toolbar_location : "top",
                        theme_advanced_toolbar_align : "left",
                        theme_advanced_statusbar_location : "bottom",
                        
                        extended_valid_elements : "iframe[src|width|height|name|align|frameborder|scrolling|style]",

                        height : "300px",
                        width : "500px"
                    });
                    if($(editor).find('.tinymce').length > 0){
                       tinyMceInitialed = true;
                    }
                }

                //attach event
                //next page event
                $(editor).find('input.page-button-next').click(function(){
                    var currentPage = $(this).parent().parent().parent();
                    nextPage(currentPage);
                });

                //previous page event
                $(editor).find('input.page-button-prev').click(function(){
                    var currentPage = $(this).parent().parent().parent();
                    prevPage(currentPage);
                });

                //save event
                $(editor).find('input.page-button-save').click(function(){
                    var propertyEditor = $(this).parent().parent().parent().parent();

                    saveProperties(propertyEditor, o);
                });

                //cancel event
                $(editor).find('input.page-button-cancel').click(function(){
                    var propertyEditor = $(this).parent().parent().parent().parent();
                    var parent = $(propertyEditor).parent();

                    $(propertyEditor).remove();

                    if($.isFunction(o.cancelCallback)){
                        o.cancelCallback(parent);
                    }

                    cleanMemory(editorId);
                });

                //grid action
                attachGridAction(editor);
                
                attachDescriptionEvent(editor);

                //element select onchange event
                $(editor).find('.property-type-elementselect .property-input select').change(function(){
                    appendElementPropertiesPage(this);
                });

                //for element select that has value, append properties page;
                $(editor).find('.property-type-elementselect .property-input select').each(function(){
                    if($(this).val() != '' && $(this).val() != null){
                        appendElementPropertiesPage(this);
                    }
                });

                $(editor).find('.property-page-show:first .property-editor-property-container .property-editor-property:first .property-input').find('input, select, textarea').focus();

                //hide page navigation when page only has one, else show steps indicator
                if($(editor).find('.property-page-show').length <= 1){
                    $(editor).find('.property-page-show .property-editor-page-button-panel .page-button-navigation').hide();
                }else{
                    renderStepsIndicator($(editor).find('.property-page-show.current'));
                }
            });
        }
    });

    function uuid(){
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        }).toUpperCase();
    }

    function saveProperties(editor, options){
        $(editor).find('.property-input-error').remove();

        var editorId = $(editor).attr('id');
        var properties = new Object();
        var errors = new Array();
        var currentPageId = $(editor).find('.property-page-show.current').attr('id');

        validationProgressStack[editorId] = new Object();
        validationProgressStack[editorId].errors = new Array();
        validationProgressStack[editorId].count = 0;
        validationProgressStack[editorId].valid = true;

        if(pageValidationStack[currentPageId] != undefined){
            validationProgressStack[editorId].count += pageValidationStack[currentPageId]['validators'].length;
        }

        if(options.propertiesDefinition != undefined && options.propertiesDefinition != null){
            //get properties value
            $.each(options.propertiesDefinition, function(i, page){
                if(page.properties != undefined){
                    validationProgressStack[editorId].count += 1;
                    properties = $.extend(properties, getPageData(editorId, page.properties, ''));
                }
            });
        }

        validationProgressStack[editorId].properties = properties;

        if(options.skipValidation || (options.propertiesDefinition == undefined || options.propertiesDefinition == null)){
            validationProgressStack[editorId].count = 0;
            validationProgressStack[editorId].valid = true;
            saveAction(editorId);
        }

        //do normal validation check
        $.each(options.propertiesDefinition, function(i, page){
            if(page.properties != undefined){
                validatePage(editorId, null, page.properties, properties, options.defaultPropertyValues, '');
            }
        });

        //do current page validation check
        if(pageValidationStack[currentPageId] != undefined){
            for(key in pageValidationStack[currentPageId]['validators']){
                var validator = pageValidationStack[currentPageId]['validators'][key];

                if(validator.type.toLowerCase() == "ajax"){
                    validateAjax(editorId, currentPageId, pageValidationStack[currentPageId].properties, properties, validator, "editor");
                }
            }
        }
    }

    function saveAction(editorId){
        var editor = $('div#'+editorId);
        var parent = $(editor).parent();

        if(validationProgressStack[editorId].count == 0 && validationProgressStack[editorId].valid){
            if(optionsStack[editorId].closeAfterSaved){
                $(editor).remove();
            }

            if($.isFunction(optionsStack[editorId].saveCallback)){
                optionsStack[editorId].saveCallback(parent, validationProgressStack[editorId].properties);
            }

            if(optionsStack[editorId].closeAfterSaved){
                cleanMemory(editorId);
            }
        }else if(validationProgressStack[editorId].count == 0 && !validationProgressStack[editorId].valid){
            var find = false;
            //display 1st page that having error
            $(editor).find('.property-page-show').each(function(){
                if($(this).find('.property-input-error').length > 0 && !find){
                    //find current page
                    var currentPage = $(editor).find('.property-page-show.current');
                    var errorPage = $(this);

                    $(currentPage).hide();
                    $(currentPage).removeClass("current");

                    $(errorPage).show();
                    $(errorPage).addClass("current");
                    renderStepsIndicator(errorPage);
                    $(errorPage).find('.property-input-error').parent().find('input, select, textarea').focus();

                    find = true;
                }
            });


            if($.isFunction(optionsStack[editorId].validationFailedCallback)){
                optionsStack[editorId].validationFailedCallback(parent, validationProgressStack[editorId].errors);
            }
        }
    }

    function renderPage(id, i, page, options, element, parent){
        var hiddenClass = " property-page-show";
        var pageTitle = '';

        if(page.hidden != undefined && page.hidden=="True"){
            hiddenClass = " property-page-hide";
        }
        if(page.title != undefined && page.title != null){
            pageTitle = page.title;
        }
        if(element == undefined || element == null){
            element = '';
        }

        if(page.validators != undefined){
            pageValidationStack[id + '_' + 'page_' + i] = page;
        }

        var html = '<div id="' + id + '_' + 'page_' + i + '" '+ element +'class="property-editor-page' + hiddenClass + '">';
        html += '<div class="property-editor-page-title">'+pageTitle+'</div><div class="property-editor-page-step-indicator"></div><div class="property-editor-property-container">';

        if(page.properties != undefined){
            $.each(page.properties, function(i, property){
                html += renderProperty(id, i, property, options, parent);
            });
        }

        html += '</div>' + renderButtonPanel(options, true) + '</div>';

        return html;
    }

    function renderNoPropertyPage(id, options){
        var html = '<div id="' + id + '_' + 'page_no_property" class="property-editor-page no-property-page">';
        html += '<div class="property-editor-page-title">'+get_peditor_msg('peditor.noProperties')+'</div><div class="property-editor-page-step-indicator"></div><div class="property-editor-property-container">';
        html += '</div>' + renderButtonPanel(options, false) + '</div>';

        return html;
    }

    function renderStepsIndicator(currentPage){
        var editor = $(currentPage).parent();

        var html = '';

        $(editor).find('.property-page-show').each(function(i){
            var pageId = $(this).attr("id");
            
            if($(this).hasClass("current")){
                html += '<span class="step active">';
            }else{
                html += '<span class="step clickable" rel="'+pageId+'" style="cursor:pointer">';
            }
            html += $(this).find('.property-editor-page-title').html() + '</span>';
            if(i < $(editor).find('.property-page-show').length - 1){
                html += ' <span class="seperator">'+get_peditor_msg('peditor.stepSeperator')+'</span> ';
            }
        });
        html += '<div style="clear:both;"></div>';
        $(currentPage).find('.property-editor-page-step-indicator').html(html);
        
        $(currentPage).find('.property-editor-page-step-indicator .clickable').click(function(){
            changePage(currentPage, $(this).attr("rel"));
        });
    }

    function renderButtonPanel(options, showNavButton){
        var html = '<div class="property-editor-page-button-panel">';
        html += '<div class="page-button-navigation">';
        if(showNavButton){
            html += '<input type="button" class="page-button-prev" value="'+ options.previousPageButtonLabel +'"/>';
            html += '<input type="button" class="page-button-next" value="'+ options.nextPageButtonLabel +'"/>';
        }
        html += '</div><div class="page-button-action">'
        html += '<input type="button" class="page-button-save" value="'+ options.saveButtonLabel +'"/>';
        if(options.showCancelButton){
            html += '<input type="button" class="page-button-cancel" value="'+ options.cancelButtonLabel +'"/>';
        }
        html += '</div><div style="clear:both"></div></div>';
        return html;
    }

    function renderProperty(id, i, property, options, parent){

        var html = '<div id="property_'+ i +'" class="property-editor-property property-type-'+ property.type.toLowerCase() +'">';

        if(property.label != undefined && property.label != null){
            var required = '';
            if(property.required != undefined && property.required.toLowerCase() == 'true'){
                required = ' <span class="property-required">'+get_peditor_msg('peditor.mandatory.symbol')+'</span>';
            }

            var description = '';
            if(property.description != undefined && property.description != null){
               description = property.description;
            }

            var toolTip = '';
            if(options.showDescriptionAsToolTip){
                toolTip = ' title="'+ description +'"';
            }

            html += '<div class="property-label-container">'
            html += '<div class="property-label"'+ toolTip +'>'+ property.label + required + '</div>';

            if(!options.showDescriptionAsToolTip){
                html += '<div class="property-description">'+ description +'</div>';
            }
            html += '</div>';
        }
        
        id = id + parent;

        html += '<div id="'+ id +'_'+ property.name +'_input" class="property-input">';

        var value = null;

        if(options.propertyValues != undefined && options.propertyValues[property.name] != undefined){
            value = options.propertyValues[property.name];
        }else if(property.value != undefined && property.value != null){
            value = property.value;
        }

        var defaultValue = null;

        if(options.defaultPropertyValues != undefined && options.defaultPropertyValues[property.name] != undefined && options.defaultPropertyValues[property.name] != ""){
            defaultValue = options.defaultPropertyValues[property.name];
        }

        if(property.type.toLowerCase() == "hidden"){
            html += renderHidden(id, property, value);
        }else if(property.type.toLowerCase() == "label"){
            html += renderLabel(id, property, value);
        }else if(property.type.toLowerCase() == "readonly"){
            html += renderReadonly(id, property, value);
        }else if(property.type.toLowerCase() == "textfield"){
            html += renderTextfield(id, property, value, defaultValue);
        }else if(property.type.toLowerCase() == "password"){
            html += renderPassword(id, property, value, defaultValue);
        }else if(property.type.toLowerCase() == "textarea"){
            html += renderTextarea(id, property, value, defaultValue);
        }else if(property.type.toLowerCase() == "checkbox"){
            html += renderCheckbox(id, property, value, defaultValue);
        }else if(property.type.toLowerCase() == "radio"){
            html += renderRadio(id, property, value, defaultValue);
        }else if(property.type.toLowerCase() == "selectbox"){
            html += renderSelectbox(id, property, value, defaultValue);
        }else if(property.type.toLowerCase() == "multiselect"){
            html += renderMultiselect(id, property, value, defaultValue);
        }else if(property.type.toLowerCase() == "grid"){
            html += renderGrid(id, property, value, defaultValue);
        }else if(property.type.toLowerCase() == "htmleditor"){
            html += renderHtmleditor(id, property, value, defaultValue);
        }else if(property.type.toLowerCase() == "elementselect"){
            html += renderElementSelect(id, property, value, defaultValue);
        }

        html += '</div><div style="clear:both;"></div></div>'

        if(property.options_ajax != undefined && property.options_ajax != null){
            addToLoadOptionsStack(id +'_'+ property.name, property.type.toLowerCase(), value, defaultValue, property.options_ajax, property.options_ajax_on_change, id);
        }

        return html;
    }

    function renderHidden(id, property, value){
        if(value == null){
            value = "";
        }
        return '<input type="hidden" id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'" value="'+ escapeHtmlTag(value) +'" />';
    }
    
    function renderLabel(id, property, value){
        if(value == null){
            value = "";
        }
        return '<input type="hidden" id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'" value="'+ escapeHtmlTag(value) +'" /><label>'+escapeHtmlTag(value)+'</label>';
    }

    function renderReadonly(id, property, value){
        if(value == null){
            value = "";
        }
        var size = '';
        if(property.size != undefined && property.size != null){
            size = ' size="'+ size +'"';
        }

        return '<input type="text" id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'"'+ size +' value="'+ escapeHtmlTag(value) +'" disabled />';
    }

    function renderTextfield(id, property, value, defaultValue){
        var size = '';
        if(value == null){
            value = "";
        }
        if(property.size != undefined && property.size != null){
            size = ' size="'+ property.size +'"';
        }
        var maxlength = '';
        if(property.maxlength != undefined && property.maxlength != null){
            maxlength = ' maxlength="'+ property.maxlength +'"';
        }

        var defaultValueLabel = '';
        if(defaultValue != null){
            defaultValueLabel = '<div class="default"><span class="label">'+get_peditor_msg('peditor.default')+'</span><span class="value">'+escapeHtmlTag(defaultValue)+'</span><div class="clear"></div></div>';
        }

        return '<input type="text" id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'"'+ size + maxlength +' value="'+ escapeHtmlTag(value) +'"/>'+defaultValueLabel;
    }

    function renderPassword(id, property, value, defaultValue){
        var size = '';
        if(value == null){
            value = "";
        }
        if(property.size != undefined && property.size != null){
            size = ' size="'+ property.size +'"';
        }
        var maxlength = '';
        if(property.maxlength != undefined && property.maxlength != null){
            maxlength = ' maxlength="'+ property.maxlength +'"';
        }

        var defaultValueLabel = '';
        if(defaultValue != null){
            defaultValue = defaultValue.replace(/./g, '*');
            defaultValueLabel = '<div class="default"><span class="label">'+get_peditor_msg('peditor.default')+'</span><span class="value">'+escapeHtmlTag(defaultValue)+'</span><div class="clear"></div></div>';
        }

        return '<input type="password" id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'"'+ size + maxlength +' value="'+ escapeHtmlTag(value) +'"/>'+defaultValueLabel;
    }

    function renderTextarea(id, property, value, defaultValue){
        var rows = '';
        if(value == null){
            value = "";
        }
        if(property.rows != undefined && property.rows != null){
            rows = ' rows="'+ property.rows +'"';
        }
        var cols = '';
        if(property.cols != undefined && property.cols != null){
            cols = ' cols="'+ property.cols +'"';
        }

        var defaultValueLabel = '';
        if(defaultValue != null){
            defaultValueLabel = '<div class="default"><span class="label">'+get_peditor_msg('peditor.default')+'</span><span class="value">'+ nl2br(escapeHtmlTag(defaultValue)) +'</span><div class="clear"></div></div>';
        }

        return '<textarea id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'"'+ rows + cols +'>'+ escapeHtmlTag(value) +'</textarea>'+defaultValueLabel;
    }

    function renderCheckbox(id, property, value, defaultValue){
        var html = '';
        var defaultValueText = '';

        if(value == null){
            value = "";
        }
        if(defaultValue == null){
            defaultValue = "";
        }

        if(property.options != undefined && property.options != null){
            $.each(property.options, function(i, option){
                var checked = "";
                $.each(value.split(";"), function(i, v){
                    if(v == option.value){
                        checked = " checked";
                    }
                });
                $.each(defaultValue.split(";"), function(i, v){
                    if(v == option.value){
                        defaultValueText += option.label + ', ';
                    }
                });
                html += '<label><input type="checkbox" id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'" value="'+escapeHtmlTag(option.value)+'"'+checked+'/>'+option.label+'</label>';
            });
        }

        if(defaultValueText != ''){
            defaultValueText = defaultValueText.substring(0, defaultValueText.length - 2);
            defaultValueText = '<div class="default"><span class="label">'+get_peditor_msg('peditor.default')+'</span><span class="value">' + escapeHtmlTag(defaultValueText) + '</span><div class="clear"></div></div>';
        }

        return html + defaultValueText;
    }

    function renderRadio(id, property, value, defaultValue){
        var html = '';
        var defaultValueText = '';

        if(value == null){
            value = "";
        }
        if(defaultValue == null){
            defaultValue = "";
        }

        if(property.options != undefined && property.options != null){
            $.each(property.options, function(i, option){
                var checked = "";
                if(value == option.value){
                    checked = " checked";
                }
                if(defaultValue == option.value){
                    defaultValueText = option.label;
                }
                html += '<label><input type="radio" id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'" value="'+escapeHtmlTag(option.value)+'"'+checked+'/>'+option.label+'</label>';
            });
        }

        if(defaultValueText != ''){
            defaultValueText = '<div class="default"><span class="label">'+get_peditor_msg('peditor.default')+'</span><span class="value">' + escapeHtmlTag(defaultValueText) + '</span><div class="clear"></div></div>';
        }

        return html + defaultValueText;
    }

    function renderSelectbox(id, property, value, defaultValue){
        var html = '<select id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'">';
        var defaultValueText = '';

        if(value == null){
            value = "";
        }
        if(defaultValue == null){
            defaultValue = "";
        }

        if(property.options != undefined && property.options != null){
            $.each(property.options, function(i, option){
                var selected = "";
                if(value == option.value){
                    selected = " selected";
                }
                if(defaultValue != "" && defaultValue == option.value){
                    defaultValueText = option.label;
                }
                html += '<option value="'+escapeHtmlTag(option.value)+'"'+selected+'>'+option.label+'</option>';
            });
        }
        html += '</select>';
        if(defaultValueText != ''){
            defaultValueText = '<div class="default"><span class="label">'+get_peditor_msg('peditor.default')+'</span><span class="value">' + escapeHtmlTag(defaultValueText) + '</span><div class="clear"></div></div>';
        }
        return html + defaultValueText;
    }

    function renderMultiselect(id, property, value, defaultValue){
        if(value == null){
            value = "";
        }
        if(defaultValue == null){
            defaultValue = "";
        }

        var size = '';
        if(property.size != undefined && property.size != null){
            size = ' size="'+ property.size +'"';
        }

        var html = '<select id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'" multiple'+ size +'>';
        var defaultValueText = '';

        if(property.options != undefined && property.options != null){
            $.each(property.options, function(i, option){
                var selected = "";
                $.each(value.split(";"), function(i, v){
                    if(v == option.value){
                        selected = " selected";
                    }
                });
                $.each(defaultValue.split(";"), function(i, v){
                    if(v != "" && v == option.value){
                        defaultValueText += option.label + ', ';
                    }
                });
                html += '<option value="'+escapeHtmlTag(option.value)+'"'+selected+'>'+option.label+'</option>';
            });
        }
        html += '</select>';

        if(defaultValueText != ''){
            defaultValueText = defaultValueText.substring(0, defaultValueText.length - 2);
            defaultValueText = '<div class="default"><span class="label">'+get_peditor_msg('peditor.default')+'</span><span class="value">' + escapeHtmlTag(defaultValueText) + '</span><div class="clear"></div></div>';
        }

        return html + defaultValueText;
    }

    function renderGrid(id, property, value, defaultValue){
        var html = '<table id="'+ id +'_'+ property.name +'"><tr id="header">';
        //render header
        $.each(property.columns, function(i, column){
            html += '<th><span>'+column.label+'</span></th>';
        });
        html += '<th class="property-type-grid-action-column"></th></tr>';

        //render model
        html += '<tr id="model" style="display:none">'
        $.each(property.columns, function(i, column){
            html += '<td><span>';
            if(column.options != undefined){
                html += '<select name="'+ column.key +'" value="">';
                    $.each(column.options, function(i, option){
                        html += '<option value="'+escapeHtmlTag(option.value)+'">'+option.label+'</option>';
                });
                html += '</select>';
            }else{
                html += '<input name="'+ column.key +'" size="10" value=""/>';
            }
            html += '</span></td>';
        });
        html += '<td class="property-type-grid-action-column">';
        html += '<a href="#" class="property-type-grid-action-moveup"><span>'+get_peditor_msg('peditor.moveUp')+'</span></a>';
        html += ' <a href="#" class="property-type-grid-action-movedown"><span>'+get_peditor_msg('peditor.moveDown')+'</span></a>';
        html += ' <a href="#" class="property-type-grid-action-delete"><span>'+get_peditor_msg('peditor.delete')+'</span></a>';
        html += '</td></tr>';

        //render value
        if(value != null){
            $.each(value, function(i, row){
                html += '<tr>';
                $.each(property.columns, function(i, column){
                    var columnValue = "";
                    if(row[column.key] != undefined){
                        columnValue = row[column.key];
                    }

                    html += '<td><span>';
                    if(column.options != undefined){
                        html += '<select name="'+ column.key +'" value="">';
                        $.each(column.options, function(i, option){
                            var selected = "";
                            if(columnValue == option.value){
                                selected = " selected";
                            }
                            html += '<option value="'+escapeHtmlTag(option.value)+'"'+selected+'>'+option.label+'</option>';
                        });
                        html += '</select>';
                    }else{
                        html += '<input name="'+ column.key +'" size="10" value="'+escapeHtmlTag(columnValue)+'"/>';
                    }
                    html += '</span></td>';
                });

                html += '<td class="property-type-grid-action-column">';
                html += '<a href="#" class="property-type-grid-action-moveup"><span>'+get_peditor_msg('peditor.moveUp')+'</span></a>';
                html += ' <a href="#" class="property-type-grid-action-movedown"><span>'+get_peditor_msg('peditor.moveDown')+'</span></a>';
                html += ' <a href="#" class="property-type-grid-action-delete"><span>'+get_peditor_msg('peditor.delete')+'</span></a>';
                html += '</td></tr>';
            });
        }

        var defaultValueText = '';
        if(defaultValue != null){
            $.each(defaultValue, function(i, row){
                $.each(property.columns, function(i, column){
                    var columnValue = "";
                    if(row[column.key] != undefined){
                        columnValue = row[column.key];
                    }

                    if(column.options != undefined){
                        $.each(column.options, function(i, option){
                            if(columnValue == option.value){
                                defaultValueText +=  escapeHtmlTag(option.label) + '; ';
                            }
                        });
                    }else{
                        defaultValueText += columnValue + '; ';
                    }
                });
                defaultValueText += '<br/>';
            });
        }
        if(defaultValueText != ''){
            defaultValueText = '<div class="default"><span class="label">'+get_peditor_msg('peditor.default')+'</span><span class="value">'+ defaultValueText +'</span><div class="clear"></div></div>';
        }

        html += '</table><a href="#" class="property-type-grid-action-add"><span>'+get_peditor_msg('peditor.add')+'</span></a>'+defaultValueText;
        return html;
    }

    function renderHtmleditor(id, property, value, defaultValue){
        var rows = '15';
        if(property.rows != undefined && property.rows != null){
            rows = ' rows="'+ property.rows +'"';
        }
        var cols = '60';
        if(property.cols != undefined && property.cols != null){
            cols = ' cols="'+ property.cols +'"';
        }

        if(value == null){
            value = "";
        }
        var defaultValueLabel = '';
        if(defaultValue != null){
            defaultValueLabel = '<div class="default"><span class="label">'+get_peditor_msg('peditor.default')+'</span><span class="value">'+ escapeHtmlTag(defaultValue) +'</span><div class="clear"></div></div>';
        }
        return '<textarea id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'" class="tinymce"'+rows +cols+'>'+ escapeHtmlTag(value) +'</textarea>'+defaultValueLabel;
    }

    function renderElementSelect(id, property, value, defaultValue){
        var html = '<select id="'+ id +'_'+ property.name +'" name="'+ id +'_'+ property.name +'">';
        var defaultValueText = '';
        var valueString = "";
        var defaultValueString = "";

        elementStack[id +'_'+ property.name] = new Object();
        elementStack[id +'_'+ property.name]['name'] = property.name;
        elementStack[id +'_'+ property.name]['url'] = property.url;

        if(property.keep_value_on_change != undefined && property.keep_value_on_change.toLowerCase() == "true"){
            elementStack[id +'_'+ property.name]['keep_value_on_change'] = "true";
        }else{
            elementStack[id +'_'+ property.name]['keep_value_on_change'] = "false";
        }
        if(value != null){
            valueString = value.className;
            elementStack[id +'_'+ property.name]['value'] = value.className;
            elementStack[id +'_'+ property.name]['properties'] = value.properties;
        }
        if(defaultValue != null){
            defaultValueString = defaultValue.classname;
        }

        if(property.options != undefined && property.options != null){
            $.each(property.options, function(i, option){
                var selected = "";
                if(valueString == option.value){
                    selected = " selected";
                }
                if(defaultValueString != "" && defaultValueString == option.value){
                    defaultValueText = option.label;
                }
                html += '<option value="'+option.value+'"'+selected+'>'+option.label+'</option>';
            });
        }
        html += '</select>';
        if(defaultValueText != ''){
            defaultValueText = '<div class="default"><span class="label">'+get_peditor_msg('peditor.default')+'</span><span class="value">' + defaultValueText + '</span><div class="clear"></div></div>';
        }
        return html + defaultValueText;
    }

    function nl2br(string){
        string = escapeHtmlTag(string);
        var regX = /\n/g;
        var replaceString = '<br/>';
        return string.replace(regX, replaceString);
    }

    function escapeHtmlTag(string){
        string = String(string);
        
        var regX = /&/g;
        var replaceString = '&amp;';
        string = string.replace(regX, replaceString);
        
        var regX = /</g;
        var replaceString = '&lt;';
        string = string.replace(regX, replaceString);

        regX = />/g;
        replaceString = '&gt;';
        string = string.replace(regX, replaceString);

        regX = /"/g;
        replaceString = '&quot;';
        return string.replace(regX, replaceString);
    }

    function addToLoadOptionsStack(id, type, value, defaultValue, url, targetName, targetPrefixId){
        loadOptionsStack[id] = new Object();
        loadOptionsStack[id]['type'] = type;
        loadOptionsStack[id]['value'] = value;
        loadOptionsStack[id]['defaultValue'] = defaultValue;
        loadOptionsStack[id]['url'] = url;
        loadOptionsStack[id]['targetName'] = targetName;
        loadOptionsStack[id]['targetPrefixId'] = targetPrefixId;
    }

    function loadOptions(editor){
        for(key in loadOptionsStack){
            if($(editor).find('#'+key+'_input').length != 0 && $(editor).find('#'+key+'_input option').length == 0 && $(editor).find('#'+key+'_input label').length == 0){
                callLoadOptionsAjax($(editor).attr('id'), key);
                if(loadOptionsStack[key].targetName != undefined && loadOptionsStack[key].targetName != null){
                    fieldOnChange($(editor).attr('id'), key);
                }
            }
        }
    }

    function callLoadOptionsAjax(editorId, key){
        var ajaxUrl = replaceContextPath(loadOptionsStack[key].url, optionsStack[editorId].contextPath);
        if(loadOptionsStack[key].targetName != undefined && loadOptionsStack[key].targetName != null){
            if(ajaxUrl.indexOf('?') != -1){
                ajaxUrl += "&";
            }else{
                ajaxUrl += "?";
            }
            var targetName = loadOptionsStack[key].targetName;
            var targetValue = $("#"+loadOptionsStack[key].targetPrefixId+"_"+targetName).val();
            if(targetValue == null || targetValue == undefined){
                var options = optionsStack[editorId];
                if(options.propertyValues != undefined && options.propertyValues[targetName] != undefined && options.propertyValues[targetName] != ""){
                    targetValue = options.propertyValues[targetName];
                }
            }
            ajaxUrl += loadOptionsStack[key].targetName + "=" + escape(targetValue);
        }
        $.ajax({
            url: ajaxUrl,
            context: {
                id: key,
                type: loadOptionsStack[key].type,
                defaultValue : loadOptionsStack[key].defaultValue,
                value: loadOptionsStack[key].value
            },
            dataType: "text",
            success: function(data) {
                if(data != undefined && data != null){
                    var options = $.parseJSON(data);
                    var id = this.id;
                    var type = this.type;
                    var value = "";
                    var defaultValue = "";

                    if(this.value != null){
                        if(this.value.className != undefined){
                            value = this.value.className;
                            elementStack[id].properties = this.value.properties;
                        }else{
                            value = this.value;
                        }
                    }

                    if(this.defaultValue != null){
                        if(this.defaultValue.className != undefined){
                            defaultValue = this.defaultValue.className;
                        }else{
                            defaultValue = this.defaultValue;
                        }
                    }

                    var defaultValueText = '';

                    if(options != undefined && options != null){
                        if(type == 'checkbox'){
                            $('#'+id+'_input label').remove();
                        }else if(type == 'radio'){
                            $('#'+id+'_input label').remove();
                        }else{
                            $('#'+id + ' option').remove();
                        }

                        $.each(options, function(i, option){
                            var checked = "";
                            if (typeof value == "string") {
                                $.each(value.split(";"), function(i, v){
                                    if(v == option.value){
                                        if(type == 'checkbox' || type == 'radio'){
                                            checked = " checked";
                                        }else{
                                            checked = " selected";
                                        }
                                    }
                                });
                            }
                            $.each(defaultValue.split(";"), function(i, v){
                                if(v != "" && v == option.value){
                                    defaultValueText += option.label + ', ';
                                }
                            });

                            if(type == 'checkbox'){
                                $('#'+id+'_input').append('<label><input type="checkbox" id="'+ id + '" name="'+ id + '" value="'+escapeHtmlTag(option.value)+'"'+checked+'/>'+option.label+'</label>');
                            }else if(type == 'radio'){
                                $('#'+id+'_input').append('<label><input type="radio" id="'+ id +'" name="'+ id +'" value="'+escapeHtmlTag(option.value)+'"'+checked+'/>'+option.label+'</label>');
                            }else{
                                $('#'+id).append('<option value="'+option.value+'"'+checked+'>'+option.label+'</option>');
                            }
                        });

                        //for element select that has value, append properties page;
                        if($('#'+id).parent().parent().hasClass('property-type-elementselect') && $('#'+id).val() != '' && $('#'+id).val() != null){
                            appendElementPropertiesPage($('#'+id));
                        }
                    }

                    $('#'+id+'_input div.default').remove();
                    if(defaultValueText != ''){
                        defaultValueText = defaultValueText.substring(0, defaultValueText.length - 2);
                        defaultValueText = '<div class="default"><span class="label">'+get_peditor_msg('peditor.default')+'</span><span class="value">' + escapeHtmlTag(defaultValueText) + '</span><div class="clear"></div></div>';
                    }
                    $('#'+id+'_input').append(defaultValueText);
                }
            }
        });
    }

    function nextPage(currentPage){
        var next = $(currentPage).next();
        while(!$(next).hasClass("property-page-show")){
            next = $(next).next();
        }
        
        changePage(currentPage, $(next).attr('id'));
    }

    function prevPage(currentPage){
        var prev = $(currentPage).prev();
        while(!$(prev).hasClass("property-page-show")){
            prev = $(prev).prev();
        }
        
        changePage(currentPage, $(prev).attr('id'));
    }
    
    function changePage(currentPage, nextPageId){
        var pageId = $(currentPage).attr('id');
        var editorId = $(currentPage).parent().attr('id');
        validationProgressStack[pageId] = new Object();
        validationProgressStack[pageId].count = 0;
        validationProgressStack[pageId].valid = true;
        validationProgressStack[pageId].errors = new Array();

        if(pageValidationStack[pageId] != undefined){
            validationProgressStack[pageId].count = pageValidationStack[pageId]['validators'].length;
            for(key in pageValidationStack[pageId]['validators']){
                var validator = pageValidationStack[pageId]['validators'][key];

                if(validator.type.toLowerCase() == "ajax"){
                    validateAjax(editorId, pageId, pageValidationStack[pageId].properties, getPageData(editorId, pageValidationStack[pageId].properties, ''), validator, "page");
                }
            }
        }else{
            changePageAction(pageId, nextPageId);
        }
    }
    
    function changePageAction(pageId, nextPageId){
        var currentPage = $('#'+pageId);
        if(validationProgressStack[pageId].count == 0 && validationProgressStack[pageId].valid){
            $(currentPage).hide();
            $(currentPage).removeClass("current");

            var next = $('#'+nextPageId);
            $(next).show();
            $(next).addClass("current");
            renderStepsIndicator(next);
            $(next).find('.property-editor-property-container .property-editor-property:first .property-input').find('input, select, textarea').focus();

        }else if(validationProgressStack[pageId].count == 0 && !validationProgressStack[pageId].valid){
            alertValidationFail(validationProgressStack[pageId].errors);
        }
    }

    function appendElementPropertiesPage(object){
        var id = $(object).attr('id');
        var value = $(object).val();
        var currentPage = $(object).parent().parent().parent().parent();
        var editor = $(currentPage).parent();
        var editorId = $(editor).attr('id');
        
        var properties = null;
        if (elementStack[id].keep_value_on_change != undefined && elementStack[id].keep_value_on_change.toLowerCase() == "true") {
            properties = getElementPageData(editorId, id);
        }

        //check if value is different, remove all the related properties page
        if($(editor).find('.property-editor-page[elementId='+id+']:first').attr('elementValue') != value){
            removePropertiesPage(editor, id);
        }
        
        //if properties page not found, render it now
        if($(editor).find('.property-editor-page[elementId='+id+']').length == 0){
            $.ajax({
                url: replaceContextPath(elementStack[id].url, optionsStack[$(editor).attr('id')].contextPath),
                context: {
                    id : id,
                    value : value,
                    properties : properties
                },
                data : "value="+escape(value),
                dataType : "text",
                success: function(response) {
                    if(response != null && response != undefined && response != ""){
                        var d = eval(response);
                        var pagehtml = '';
                        var id = this.id;
                        var value = this.value;
                        var properties = this.properties;

                        elementStack[id]['propertiesDefinition'] = d;

                        var option = optionsStack[editorId];
                        option['defaultPropertyValues'] = null;
                        if(value == elementStack[this.id].value){
                            option['propertyValues'] = elementStack[id].properties;
                        }else{
                            option['propertyValues'] = properties;
                        }

                        $.each(d, function(i, page){
                            pagehtml += renderPage(editorId, elementStack[id].name + '_' + i, page, option, ' elementId="'+ id+'" elementValue="'+ value +'"', '_'+id);
                        });

                        addElementPropertiesPage(editorId, currentPage, pagehtml);
                    }else{
                        appendElementPropertiesPageCallback(editorId, currentPage);
                    }
                }
            });
        }
    }

    function removePropertiesPage(editor, id){    
        //search for child level properties page
        if(elementStack[id].propertiesDefinition != undefined && elementStack[id].propertiesDefinition.length > 0){
            $.each(elementStack[id].propertiesDefinition, function(i, page){
                if(page.properties != undefined && page.properties.length > 0){
                    $.each(page.properties, function(j, property){
                        if(property.type.toLowerCase() == "elementselect"){
                            removePropertiesPage(editor, $(editor).attr('id')+'_'+id+'_'+property.name);
                        }
                    });
                }
            });
            elementStack[id].propertiesDefinition = "";
        }
     
        $(editor).find('.property-editor-page[elementId='+id+']').remove();
    }

    function getPageData(editorId, pagePropertiesDefinition, parent){
        var editor = $('div#'+editorId);
        var properties = new Object();

        $.each(pagePropertiesDefinition, function(i, property){
            if(property.type.toLowerCase() == "elementselect"){
                var element = new Object();
                element['className'] = "";
                if($(editor).find('#'+editorId+'_'+parent+property.name).val() != null){
                    element['className'] = $(editor).find('#'+editorId+'_'+parent+property.name).val();
                }
                element['properties'] = getElementPageData(editorId, editorId+'_'+parent+property.name);

                properties[property.name] = element;
            }else if(property.type.toLowerCase() == "grid"){
                var gridValue = new Array();
                $(editor).find('#'+editorId+'_'+parent+property.name).find('tr').each(function(tr){
                    var row = $(this);
                    if($(row).attr('id') != "model" && $(row).attr('id') != "header"){
                        var obj = new Object();

                        $.each(property.columns, function(i, column){
                            obj[column.key] = $(row).find('input[name='+ column.key +'], select[name='+ column.key +']').val();
                        });
                        gridValue.push(obj);
                    }
                });

                properties[property.name] = gridValue;
            }else{
                var value = '';

                if(property.type.toLowerCase() == "checkbox"){
                    $(editor).find('#'+editorId+'_'+parent+property.name + ':checkbox:checked').each(function(i){
                        value += $(this).val() + ';';
                    });
                    if(value != ''){
                        value = value.replace(/;$/i, '');
                    }
                }else if(property.type.toLowerCase() == "multiselect"){
                    var values = $(editor).find('#'+editorId+'_'+parent+property.name).val();
                    for(num in values){
                        value += values[num] + ';';
                    }
                    if(value != ''){
                        value = value.replace(/;$/i, '');
                    }
                }else if(property.type.toLowerCase() == "htmleditor"){
                    value = $(editor).find('#'+editorId+'_'+parent+property.name).html();
                }else if(property.type.toLowerCase() == "radio"){
                    value = $(editor).find('#'+editorId+'_'+parent+property.name+':checked').val();
                }else{
                    value = $(editor).find('#'+editorId+'_'+parent+property.name).val();
                }

                properties[property.name] = value;
            }
        });
        return properties;
    }
    
    function getElementPageData(editorId, elementId){
        var properties = new Object();

        if(elementStack[elementId].propertiesDefinition != undefined){
            //get properties value
            $.each(elementStack[elementId].propertiesDefinition, function(i, page){
                if(page.properties != undefined){
                    if(validationProgressStack[editorId] != undefined){
                        validationProgressStack[editorId].count += 1;
                    }
                    properties = $.extend(properties, getPageData(editorId, page.properties, elementId+'_'));
                }
            });
        }
        return properties;
    }

    function validatePage(editorId, pageId, pagePropertiesDefinition, data, defaultValues, parent){
        var editor = $('div#'+editorId);
        var errors = new Array();
        if(pagePropertiesDefinition != undefined && pagePropertiesDefinition.length != 0){
            $.each(pagePropertiesDefinition, function(i, property){
                var value = data[property.name];
                var defaultValue = "";

                if(defaultValues != undefined && defaultValues[property.name] != undefined){
                    defaultValue = defaultValues[property.name];
                }
                if(property.required != undefined && property.required.toLowerCase() == "true" && (value == '' || value == undefined || (property.type.toLowerCase() == "elementselect" && value.className == '')) && defaultValue == ''){
                    var obj = new Object();
                    obj.fieldName = property.label;
                    obj.message = optionsStack[editorId].mandatoryMessage;
                    errors.push(obj);
                    if(property.type.toLowerCase() == "checkbox" || property.type.toLowerCase() == "radio"){
                        $(editor).find('#'+editorId+'_'+parent+property.name).parent().parent().append('<div class="property-input-error">'+ optionsStack[editorId].mandatoryMessage +'</div>');
                    }else{
                        $(editor).find('#'+editorId+'_'+parent+property.name).parent().append('<div class="property-input-error">'+ optionsStack[editorId].mandatoryMessage +'</div>');
                    }
                }

                if(!((value == '' || value == undefined) && defaultValue == '') && property.regex_validation != undefined && property.regex_validation != '' && (property.type.toLowerCase().toLowerCase() == "textfield" || property.type.toLowerCase() == "password" || property.type.toLowerCase() == "textarea" || property.type.toLowerCase() == "htmleditor")){
                    var regex = new RegExp(property.regex_validation);
                    if(!regex.exec(value)){
                        var obj2 = new Object();
                        obj2.fieldName = property.label;
                        if(property.validation_message != undefined && property.validation_message != '' ){
                            obj2.message = property.validation_message;
                        }else{
                            obj2.message = get_peditor_msg('peditor.validationFailed');
                        }
                        errors.push(obj2);
                        $(editor).find('#'+editorId+'_'+parent+property.name).parent().append('<div class="property-input-error">'+ obj2.message +'</div>');
                    }
                }

                if(property.type.toLowerCase() == "elementselect"){
                    if(elementStack[editorId+'_'+parent+property.name] != undefined && elementStack[editorId+'_'+parent+property.name].propertiesDefinition != undefined && elementStack[editorId+'_'+parent+property.name].propertiesDefinition.length > 0){
                        $.each(elementStack[editorId+'_'+parent+property.name].propertiesDefinition, function(i, page){
                            if(page.properties != undefined){
                                validatePage(editorId, pageId, page.properties, value.properties, null, editorId+'_'+parent+property.name+'_');
                            }
                        });
                    }else if(value != undefined && value.className != undefined && value.className != ""){
                       if(pageId != null){
                            validationProgressStack[pageId].count = validationProgressStack[pageId].count + 1;
                        }else{
                            validationProgressStack[editorId].count = validationProgressStack[editorId].count + 1;
                        }

                        $.ajax({
                            url: replaceContextPath(elementStack[editorId+'_'+parent+property.name].url, optionsStack[editorId].contextPath),
                            context: {
                                id : editorId+'_'+parent+property.name,
                                value : value
                            },
                            data : "value="+escape(value.className),
                            dataType : "text",
                            success: function(response) {
                                if(response != null && response != undefined && response != ""){
                                    var d = eval(response);
                                    var value = this.value;

                                    if(pageId != null){
                                        validationProgressStack[pageId].count = validationProgressStack[pageId].count + d.length;
                                    }else{
                                        validationProgressStack[editorId].count = validationProgressStack[editorId].count + d.length;
                                    }
                                    
                                    if(d.length > 0){
                                        $.each(d, function(i, page){
                                            validatePage(editorId, pageId, page.properties, value.properties, null, editorId+'_'+parent+property.name+'_');
                                        });
                                    }
                                }
                                if(pageId != null){
                                    validationProgressStack[pageId].count = validationProgressStack[pageId].count - 1;
                                    nextPageAction(pageId);
                                }else{
                                    validationProgressStack[editorId].count = validationProgressStack[editorId].count - 1;
                                    saveAction(editorId);
                                }
                            }
                        });
                    }
                }
            });
        }

        if(pageId != null){
            validationProgressStack[pageId].count = validationProgressStack[pageId].count - 1;
            if(errors.length != 0){
                validationProgressStack[pageId].valid = false;
                validationProgressStack[pageId].errors = validationProgressStack[pageId].errors.concat(errors);
            }
            nextPageAction(pageId);
        }else{
            validationProgressStack[editorId].count = validationProgressStack[editorId].count - 1;
            if(errors.length != 0){
                validationProgressStack[editorId].valid = false;
                validationProgressStack[editorId].errors = validationProgressStack[editorId].errors.concat(errors);
            }
            saveAction(editorId);
        }
    }

    function validateAjax(editorId, pageId, pagePropertiesDefinition, data, validator, mode){
        //remove previous error message
        $('#'+pageId + ' > .property-editor-property-container > .property-editor-page-errors').remove();
        
        $.ajax({
            url: replaceContextPath(validator.url, optionsStack[editorId].contextPath),
            data : $.param( data ),
            dataType : "text",
            success: function(response) {
                var errors = new Array();

                var r = $.parseJSON(response);

                if(r.status.toLowerCase() == "fail"){
                    if(r.message.length == 0){
                        var obj = new Object();
                        obj.fieldName = '';
                        obj.message = validator.default_error_message;

                        errors.push(obj);
                    }else{
                        for(i in r.message){
                            var obj2 = new Object();
                            obj2.fieldName = '';
                            obj2.message = r.message[i];

                            errors.push(obj2);
                        }
                    }
                }
                
                if(errors.length != 0){
                    var errorPage = $('#'+pageId);
                    var errorContainer = $('<div class="property-editor-page-errors"></div>');
                    for(e in errors){
                        $(errorContainer).append('<div class="property-input-error">'+errors[e].message+'</div>')
                    }
                    $(errorPage).find('.property-editor-property-container').prepend(errorContainer);
                }

                if(mode == "page"){
                    validationProgressStack[pageId].count = validationProgressStack[pageId].count - 1;
                    if(errors.length != 0){
                        validationProgressStack[pageId].valid = false;
                        validationProgressStack[pageId].errors = validationProgressStack[pageId].errors.concat(errors);
                    }
                    nextPageAction(pageId);
                }else{
                    validationProgressStack[editorId].count = validationProgressStack[editorId].count - 1;
                    if(errors.length != 0){
                        validationProgressStack[editorId].valid = false;
                        validationProgressStack[editorId].errors = validationProgressStack[editorId].errors.concat(errors);
                    }
                    saveAction(editorId);
                }
            }
        });
    }

    function alertValidationFail(errors){
        var errorMsg = '';
        for(key in errors){
            if(errors[key].fieldName != '' && errors[key].fieldName != null){
                errorMsg += errors[key].fieldName + ' : ';
            }
            errorMsg += errors[key].message + '\n';
        }
        alert(errorMsg);
    }

    function addElementPropertiesPage(editorId, currentPage, html){
        var content = $(html);

        //attach event
        //next page event
        $(content).find('input.page-button-next').click(function(){
            var currentPage = $(this).parent().parent().parent();
            nextPage(currentPage);
        });

        //previous page event
        $(content).find('input.page-button-prev').click(function(){
            var currentPage = $(this).parent().parent().parent();
            prevPage(currentPage);
        });

        //save event
        $(content).find('input.page-button-save').click(function(){
            var propertyEditor = $(this).parent().parent().parent().parent();

            saveProperties(propertyEditor, optionsStack[editorId]);
        });

        //cancel event
        $(content).find('input.page-button-cancel').click(function(){
            var propertyEditor = $(this).parent().parent().parent().parent();
            var parent = $(propertyEditor).parent();

            $(propertyEditor).remove();

            if($.isFunction(optionsStack[editorId].cancelCallback)){
                optionsStack[editorId].cancelCallback(parent);
            }
        });

        //grid action
        attachGridAction(content);
        
        attachDescriptionEvent(content);

        //element select onchange event
        $(content).find('.property-type-elementselect .property-input select').change(function(){
            appendElementPropertiesPage(this);
        });

        $(currentPage).after(content);

        //if tinymce ald exist, using command to init it
        if(tinyMceInitialed){
            $(content).find('.tinymce').each(function(){
                tinymce.execCommand('mceAddControl', false, $(this).attr('id'));
            });
        }else{
            $(content).find('.tinymce').tinymce({
                // Location of TinyMCE script
                script_url : optionsStack[editorId].tinyMceScript,

                // General options
                convert_urls : false,
                theme : "advanced",
                plugins : "layer,table,save,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,contextmenu,paste,noneditable,xhtmlxtras,template,advlist",

                // Theme options
                theme_advanced_buttons1 : "cleanup,code,|,undo,redo,|,cut,copy,paste|,search,replace,|,bullist,numlist,|,outdent,indent",
                theme_advanced_buttons2 : "bold,italic,underline,strikethrough,|,forecolor,backcolor,|,justifyleft,justifycenter,justifyright,justifyfull,|,sub,sup,|,insertdate,inserttime,charmap,iespell",
                theme_advanced_buttons3 : "formatselect,fontselect,fontsizeselect,|,hr,removeformat,blockquote,|,link,unlink,image,media",
                theme_advanced_buttons4 : "tablecontrols,|,visualaid,insertlayer,moveforward,movebackward,absolute",
                theme_advanced_toolbar_location : "top",
                theme_advanced_toolbar_align : "left",
                theme_advanced_statusbar_location : "bottom",
                
                extended_valid_elements : "iframe[src|width|height|name|align|frameborder|scrolling|style]",

                height : "300px",
                width : "500px"
            });
            if($(content).find('.tinymce').length > 0){
                tinyMceInitialed = true;
            }
        }

        loadOptions($('div#'+editorId));
        $('div#'+editorId).find('.property-editor-page').hide();
        $('div#'+editorId).find('.property-editor-page.current').show();

        appendElementPropertiesPageCallback(editorId, currentPage);
    }

    function appendElementPropertiesPageCallback(editorId, currentPage){
        var activePage = $('div#'+editorId).find('.property-editor-page.current');
        renderStepsIndicator(activePage);

        var editor = $('#'+editorId);

        //hide page navigation when page only has one, else show steps indicator
        if($(editor).find('.property-page-show').length <= 1){
            $(editor).find('.property-page-show .property-editor-page-button-panel .page-button-navigation').hide();
        }else{
            $(editor).find('.property-page-show .property-editor-page-button-panel .page-button-navigation').show();
        }
        
        $(editor).find('.property-page-show .property-editor-page-button-panel .page-button-navigation input[type=button]').removeAttr("disabled");
        $(editor).find('.property-page-show:first .property-editor-page-button-panel .page-button-navigation .page-button-prev').attr("disabled","disabled");
        $(editor).find('.property-page-show:last .property-editor-page-button-panel .page-button-navigation .page-button-next').attr("disabled","disabled");
    }

    function replaceContextPath(string, contextPath){
        if(string == null){
            return string;
        }
        var regX = /\[CONTEXT_PATH\]/g;
        return string.replace(regX, contextPath);
    }

    function cleanMemory(editorId){
        for(key in loadOptionsStack){
            if(key.match(editorId) != null){
                delete loadOptionsStack[key];
            }
        }
        for(key in pageValidationStack){
            if(key.match(editorId) != null){
                delete pageValidationStack[key];
            }
        }
        for(key in validationProgressStack){
            if(key.match(editorId) != null){
                delete validationProgressStack[key];
            }
        }
        for(key in optionsStack){
            if(key.match(editorId) != null){
                delete optionsStack[key];
            }
        }
        for(key in elementStack){
            if(key.match(editorId) != null){
                delete elementStack[key];
            }
        }
    }

    function fieldOnChange(editorId, key) {
        var targetEl = $("#" + loadOptionsStack[key].targetPrefixId + '_' + loadOptionsStack[key].targetName);
        targetEl.change(function() {
            callLoadOptionsAjax(editorId, key);
        });
    }

    function attachGridAction(object){
        //add
        $(object).find('a.property-type-grid-action-add').click(function(){
            var table = $(this).prev('table');
            var model = $(table).find('#model').html();
            var row = $('<tr>' + model + '</tr>');
            $(table).append(row);
            $(row).find('a.property-type-grid-action-delete').click(function(){
                gridActionDelete(this);
                return false;
            });
            $(row).find('a.property-type-grid-action-moveup').click(function(){
                gridActionMoveUp(this);
                return false;
            });
            $(row).find('a.property-type-grid-action-movedown').click(function(){
                gridActionMoveDown(this);
                return false;
            });
            attachDescriptionEvent(row);
            gridDisabledMoveAction(table);
            return false;
        });

        //delete
        $(object).find('a.property-type-grid-action-delete').click(function(){
            gridActionDelete(this);
            return false;
        });

        //move up
        $(object).find('a.property-type-grid-action-moveup').click(function(){
            gridActionMoveUp(this);
            return false;
        });

        //move down
        $(object).find('a.property-type-grid-action-movedown').click(function(){
            gridActionMoveDown(this);
            return false;
        });

        $(object).find('tr#model').each(function(){
            gridDisabledMoveAction($(this).parent());
        });
    }

    function gridActionDelete(object){
        $(object).parent().parent().remove();
    }

    function gridActionMoveUp(object){
        var currentRow = $(object).parent().parent();
        var prevRow = $(currentRow).prev();
        if(prevRow.attr("id") != "model"){
            $(currentRow).after(prevRow);
            gridDisabledMoveAction($(currentRow).parent());
        }
    }

    function gridActionMoveDown(object){
        var currentRow = $(object).parent().parent();
        var nextRow = $(currentRow).next();
        if(nextRow.length > 0){
            $(nextRow).after(currentRow);
            gridDisabledMoveAction($(currentRow).parent());
        }
    }

    function gridDisabledMoveAction(table){
        $(table).find('a.property-type-grid-action-moveup').removeClass("disabled");
        $(table).find('a.property-type-grid-action-moveup:eq(1)').addClass("disabled");

        $(table).find('a.property-type-grid-action-movedown').removeClass("disabled");
        $(table).find('a.property-type-grid-action-movedown:last').addClass("disabled");
    }
    
    function attachDescriptionEvent(container){
        $(container).find("input, select, textarea").focus(function(){
            var editor = $(this).parentsUntil(".property-editor-container", ".property-editor-page").parent();
            $(editor).find(".property-description").hide();
            var property = $(this).parentsUntil(".property-editor-property-container", ".property-editor-property");
            $(property).find(".property-description").show();
        });
    }

})(jQuery);