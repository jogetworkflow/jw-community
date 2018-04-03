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
        I18nEditor.retrieveLabels(labels, jsonObj, ['label']);
        
        var regex = new RegExp("\\#([^#^\"^ ])*\\.([^#^\"])*\\#", "gi");
        var found = json.match(regex);
        I18nEditor.retrieveI18nHash(labels, found);
        
        I18nEditor.renderTable(container, labels.sort(), options);
    },
    renderTable : function (container, labels, options) {
        if (labels.length > 0) {
            $(container).append('<div class="sticky_header"><div class="sticky_container"><table class="i18n_table"><thead><tr><th>&nbsp;</th><th class="lang1"><div></div></th><th class="lang2"><div></div></th></tr></thead><tbody></tbody></table></div></div>');
            var $table = $(container).find(".i18n_table");
            
            for (var l in labels) {
                if (UI.escapeHTML(labels[l]) === "") {
                    continue;
                }
                var css = "odd";
                if (l % 2 === 0) {
                    css = "even";
                }
                $table.find("tbody").append('<tr class="'+css+'"><td class="label"><span>'+UI.escapeHTML(labels[l])+'</span><textarea name="i18n_key_'+l+'" style="display:none">'+labels[l]+'</textarea></td><td class="lang1"></td><td class="lang2"></td></tr>');
            }
            
            if (I18nEditor.languages === undefined) {
                $.ajax({
                    url: options.contextPath + '/web/json/console/locales',
                    dataType : "json",
                    success: function(response) {
                        I18nEditor.languages = response.data;
                        I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang1 div"), "lang1", options);
                        I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang2 div"), "lang2", options);
                    }
                });
            } else {
                I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang1 div"), "lang1",  options);
                I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang2 div"), "lang2",  options);
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
                $(tr).find("td."+id+" textarea").attr("rel", key);
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
                            var mid = message.id.replace(new RegExp('"', 'g'), "\\\"");
                            var field = $(container).find('td.'+id+' textarea[rel="'+mid+'"]');
                            if ($(field).attr("rel") === message.id) {
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