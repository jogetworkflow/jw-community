I18nEditor = {
    languages : undefined,
    retrieveLabels : function (labels, jsonObj, keywords) {
        for (var key in jsonObj) {
            if ($.inArray(key, keywords) !== -1) {
                if ($.inArray(jsonObj[key], labels) === -1) {
                        labels.push(jsonObj[key]);
                    }
            } else if ($.type(jsonObj[key]) === "object" || $.type(jsonObj[key]) === "array") {
                I18nEditor.retrieveLabels(labels, jsonObj[key], keywords);
            }
        }
    },
    retrieveTooltipLabels : function(labels, jsonObj) {
        if (jsonObj.className !== "" &&
                jsonObj.className !== "org.joget.apps.form.model.Form" &&
                jsonObj.className !== "org.joget.apps.form.model.Section" && 
                jsonObj.className !== "org.joget.apps.form.model.Column") {
            if (jsonObj.properties.id !== "" && jsonObj.properties.id !== undefined 
                    && jsonObj.properties.label !== "" && jsonObj.properties.label !== undefined) {
                labels.push({
                    id : jsonObj.properties.id,
                    label : jsonObj.properties.label
                });
            }
        }
        if (jsonObj.elements !== undefined) {
            for (var i in jsonObj.elements) {
                I18nEditor.retrieveTooltipLabels(labels, jsonObj.elements[i]);
            }
        }
    },
    refresh : function (container) {
        $(container).find("select").trigger("chosen:updated");
        var height = $(container).height();
        $(container).find(".sticky_container").height(height - 46);
    },
    retrieveI18nHash : function (labels, found) {
        for (var i in found) {
            if (found[i].indexOf("#i18n.") !== -1 || found[i].indexOf("{i18n.") === 0) {
                labels.push(found[i].substring(6, found[i].length - 1));
            } else if (found[i].indexOf("{", 1) !== -1) {
                var regex = new RegExp("\\{([^\\{^\\}])*\\}","gi");
                var nestedfound = found[i].match(regex);
                I18nEditor.retrieveI18nHash(labels, nestedfound);
            }
        }
    },
    init : function (container, json, options) {
        var jsonObj = JSON.parse(json);
        
        var labels = [];
        if (AdvancedTools.options.builder === "custom") {
            I18nEditor.retrieveLabels(labels, jsonObj, CustomBuilder.config.advanced_tools.i18n.keywords);
        } else {
            I18nEditor.retrieveLabels(labels, jsonObj, ['label']);
        }
        
        var regex = new RegExp("\\#([^#^\"^ ])*\\.([^#^\"])*\\#", "gi");
        var found = json.match(regex);
        I18nEditor.retrieveI18nHash(labels, found);
        
        I18nEditor.renderTable(container, labels.sort(), options);
    },
    initTooltip : function (container, json, options) {
        var jsonObj = JSON.parse(json);
        
        var labels = [];
        I18nEditor.retrieveTooltipLabels(labels, jsonObj);
        
        options.isTooltip = true;
        options.formDefId = (jsonObj.properties !== undefined)?jsonObj.properties.id:"";
        
        I18nEditor.renderTable(container, labels, options);
    },
    initProcess : function (container, options) {
        var labels = [];
        if (ProcessBuilder !== undefined) {
            for (var id in ProcessBuilder.Designer.model.processes) {
                var process = ProcessBuilder.Designer.model.processes[id];
                labels.push({
                    label : process.name + " ("+id+")",
                    key : "plabel." + id
                });
                
                for (var act in process.activities) {
                    var activity = process.activities[act];
                    if (activity.type === "activity") {
                        labels.push({
                            label : activity.name + " ("+id+":"+act+")",
                            key : "plabel." + id + "." + act
                        });
                    }
                }
            }
        }
        options.isKeyValue = true;
        I18nEditor.renderTable(container, labels, options);
    },
    renderTable : function (container, labels, options) {
        if (labels.length > 0) {
            $(container).append('<div class="sticky_header"><div class="sticky_container"><table class="i18n_table"><thead><tr><th>&nbsp;</th><th class="lang1"><div></div></th><th class="lang2"><div></div></th></tr></thead><tbody></tbody></table></div></div>');
            var $table = $(container).find(".i18n_table");
            
            var i = 0;
            for (var l in labels) {
                var key = "";
                var label = "";
                var css = "odd";
                if (options.isTooltip) {
                    label = labels[l].label + " (" + labels[l].id + ")";
                    key = "tooltip." + options.formDefId + "." + labels[l].id;
                } else if (options.isKeyValue) {
                    label = labels[l].label;
                    key = labels[l].key;
                } else {
                    if (UI.escapeHTML(labels[l]) === "") {
                        continue;
                    }
                    label = labels[l];
                    key = labels[l];
                }
                if (i % 2 === 0) {
                    css = "even";
                }
                $table.find("tbody").append('<tr class="'+css+'"><td class="label"><span>'+UI.escapeHTML(label)+'</span><textarea name="i18n_key_'+l+'" style="display:none">'+key+'</textarea></td><td class="lang1"></td><td class="lang2"></td></tr>');
                i++;
            }
            
            if (I18nEditor.languages === undefined) {
                $.ajax({
                    url: options.contextPath + '/web/json/console/locales',
                    dataType : "json",
                    success: function(response) {
                        I18nEditor.languages = response.data;
                        I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang1 div"), "lang1", options);
                        I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang2 div"), "lang2", options);
                        
                        if (options.isTooltip || options.isKeyValue) {
                            $(".i18n_table #lang1").val("en_US").trigger("chosen:updated").trigger("change");
                        }
                    }
                });
            } else {
                I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang1 div"), "lang1",  options);
                I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang2 div"), "lang2",  options);
                
                if (options.isTooltip || options.isKeyValue) {
                    $(".i18n_table #lang1").val("en_US").trigger("chosen:updated").trigger("change");
                }
            }
        } else {
            $(container).append('<h3>'+get_advtool_msg('i18n.editor.no.label')+'</h3>');
        }
    },
    renderLocaleSelector : function(container, header, id, options) {
        $(header).append('<select id="'+id+'" data-placeholder="'+get_advtool_msg('i18n.editor.chooseLocale')+'"><option></option></select>');
        var selector = $(header).find("select");
        for (var i in I18nEditor.languages) {
            $(selector).append('<option>'+I18nEditor.languages[i]+'</option>');
        }
        if (UI.rtl) {
            $(selector).addClass("chosen-rtl");
        }
        $(selector).chosen({width: "60%"}).change(function(){
            var locale = $(this).val();
            $(header).find("a.button").remove();
            if (locale !== "") {
                $(header).append('<a class="button">'+get_advtool_msg('i18n.editor.save')+'</a>');
            }
            I18nEditor.loadLocale(container, locale, id, options);
        });
        
        $(header).on("click", "a.button", function(){
            I18nEditor.saveLocale(container, this, $(selector).val(), id, options);
        });
    },
    loadLocale : function (container, locale, id, options) {
        if (locale === "") {
            $(container).find("td."+id).html("");
        } else {
            $(container).find("tbody tr").each(function(i, tr){
                var key = $(tr).find("td.label textarea").val() + "_" + locale;
                $(tr).find("td."+id).html('<textarea></textarea>');
                $(tr).find("td."+id+" textarea").attr("rel", key.toLowerCase());
            });
            $.ajax({
                url: options.contextPath + '/web/json/console/app/'+options.appId+'/'+options.appVersion+'/message/list',
                data : {
                    locale : locale
                },
                dataType : "json",
                success: function(response) {
                    if (response.total > 0) {
                        for (var i in response.data) {
                            var message = response.data[i];
                            var mid = message.id.replace(new RegExp('"', 'g'), "\\\"").toLowerCase();
                            var field = $(container).find('td.'+id+' textarea[rel="'+mid+'"]');
                            if ($(field).attr("rel") === message.id.toLowerCase()) {
                                $(field).val(message.message);
                            }
                        }
                    }
                }
            });
        }
    },
    saveLocale : function(container, button, locale, id, options) {
        $(button).hide();
        $(button).after('<i class="fa-spinner fa-spin" aria-hidden="true"></i>');
        var data = [];
        $(container).find('td.'+id+' textarea').each(function(){
            var id = $(this).attr("rel");
            var key = $(this).closest("tr").find("td.label textarea").val();
            var val = $(this).val();
            data.push({
                id : id,
                key : key,
                value : val
            });
        });
        $.ajax({
            type: "POST",
            url: options.contextPath + '/web/json/console/app/'+options.appId+'/'+options.appVersion+'/message/submit',
            data : {
                locale : locale,
                data : JSON.encode(data)
            },
            dataType : "json"
        }).done(function() {
            $(button).next().remove();
            $(button).after('<span> '+get_advtool_msg('i18n.editor.saved')+'</span>');
        }).fail(function() {
            $(button).next().remove();
            $(button).after('<span> '+get_advtool_msg('i18n.editor.error')+'</span>');
        }).always(function() {
            setTimeout(function(){
                $(button).next().remove();
                $(button).show();
            }, 5000);
        });
    }
};