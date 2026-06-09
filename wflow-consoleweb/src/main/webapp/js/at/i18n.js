I18nEditor = {
    languages : undefined,
    retrieveLabels : function (keys, labels, jsonObj, keywords, options) {
        for (var key in jsonObj) {
            if (!(options.skip && options.skip(key, jsonObj, keys, labels, options))) {
                const isAnyKeywordSubstringOfKey = keywords.some(substr => key.toLowerCase().includes(substr.toLowerCase()));
                if (isAnyKeywordSubstringOfKey) {
                    // tests whether key is only #i18n.xxx# hash
                    const keyContainsI18nHash = /^\s*#i18n\.[^#]+#\s*$/.test(jsonObj[key]);
                    if (!keyContainsI18nHash && $.inArray(jsonObj[key], keys) === -1) {
                        var lkey = jsonObj[key],
                            label = jsonObj[key];

                        if (options.key) {
                            lkey = options.key(lkey, jsonObj, options);
                        }
                        if (options.label) {
                            label = options.label(label, jsonObj, options);
                        }
                        labels.push({
                            key : lkey,
                            label : label
                        });
                        keys.push(lkey);
                    }
                } else if ($.type(jsonObj[key]) === "object" || $.type(jsonObj[key]) === "array") {
                    I18nEditor.retrieveLabels(keys, labels, jsonObj[key], keywords, options);
                }
            }
        }
    },
    refresh : function (container) {
        $(container).find("select").trigger("chosen:updated");
        var height = $(container).height();
        $(container).find(".sticky_container").height(height - 46);
    },
    retrieveI18nHash : function (keys, labels, found, options) {
        for (var i in found) {
            if ((typeof found[i]) === "string") {
                if (found[i].indexOf("#i18n.") !== -1 || found[i].indexOf("{i18n.") === 0) {
                    var label = found[i].substring(6, found[i].length - 1);
                    if ($.inArray(label, keys) === -1) {
                        labels.push({
                            key : label,
                            label : label
                        });
                        keys.push(label);
                    }
                } else if (found[i].indexOf("{", 1) !== -1) {
                    var regex = new RegExp("\\{([^\\{^\\}])*\\}","gi");
                    var nestedfound = found[i].match(regex);
                    I18nEditor.retrieveI18nHash(labels, nestedfound, options);
                }
            }
        }
    },
    init : function (container, json, options) {
        var jsonObj = JSON.parse(json);
        
        var keys = [];
        var labels = [];
        I18nEditor.retrieveLabels(keys, labels, jsonObj, CustomBuilder.config.advanced_tools.i18n.keywords, options);
        
        if (options.i18nHash) {
            var regex = new RegExp("\\#([^#^\"^ ])*\\.([^#^\"])*\\#", "gi");
            var found = json.match(regex);
            I18nEditor.retrieveI18nHash(keys, labels, found, options);
        }
        if (options.sort) {
            labels.sort(function(a, b) {
                return a.label.localeCompare(b.label);
            });
        }
        
        I18nEditor.renderTable(container, labels, options);
    },
    renderTable : function (container, labels, options) {
        if ($(container).attr("id") === undefined) {
            $(container).attr("id", "i18n_container" + (new Date).getTime());
        }

        $(container).append('<div class="sticky_header"><div class="sticky_container"><table class="i18n_table"><thead><tr><th><div class="search-container"><input class="form-control form-control-sm component-search" placeholder="'+get_cbuilder_msg('cbuilder.search')+'" type="text"><button class="clear-backspace"><i class="la la-close"></i></button></div></th><th class="lang1 lang" data-lang="lang1"><div></div></th><th class="lang2 lang" data-lang="lang2"><div></div></th></tr></thead><tbody></tbody></table></div></div>');
        var $table = $(container).find(".i18n_table");

        $table.data("options", options);

        $(container).find('.search-container input').off("keyup");
        $(container).find('.search-container input').on("keyup", function(){
            var searchText = $(this).val().toLowerCase();
            $(container).find("tbody tr").each(function(){
                var match = false;
                $(this).find('td.label > span').each(function(){
                    if ($(this).text().toLowerCase().indexOf(searchText) > -1) {
                        match = true;
                    }
                });
                $(this).find('textarea').each(function(){
                    if ($(this).val().toLowerCase().indexOf(searchText) > -1) {
                        match = true;
                    }
                });
                if (match) {
                    $(this).show();
                } else {
                    $(this).hide();
                }
            });
            if (this.value !== "") {
                $(this).next("button").show();
            } else {
                $(this).next("button").hide();
            }
        });

        $(container).find('.search-container .clear-backspace').off("click");
        $(container).find('.search-container .clear-backspace').on("click", function(){
            $(this).hide();
            $(this).prev("input").val("");
            $(container).find("tbody tr").show();
        });

        if (labels.length > 0) {    
            var i = 0;
            for (var l in labels) {
                var key = "";
                var label = "";
                var css = "odd";
                if (UI.escapeHTML(labels[l].key).trim() === "") {
                    continue;
                }
                label = labels[l].label;
                key = labels[l].key;
                if (i % 2 === 0) {
                    css = "even";
                }
                I18nEditor.appendI18nTableRow($table, key, label, css, l);
                i++;
            }
        } else {
            $table.find("tbody").append('<tr class="norecord"><td colspan="3" class="label"><h3>'+get_advtool_msg('i18n.editor.no.label')+'</h3></td></tr>');
        }
        
        if (I18nEditor.languages === undefined) {
            $.ajax({
                url: options.contextPath + '/web/json/console/locales',
                dataType : "json",
                success: function(response) {
                    I18nEditor.languages = response.data;
                    I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang1 div"), "lang1", options);
                    I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang2 div"), "lang2", options);

                    if (options.loadEnglish) {
                        $(".i18n_table #lang1").val("en_US").trigger("chosen:updated").trigger("change");
                    }
                }
            });
        } else {
            I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang1 div"), "lang1",  options);
            I18nEditor.renderLocaleSelector($(container), $(container).find("th.lang2 div"), "lang2",  options);

            if (options.loadEnglish) {
                $(".i18n_table #lang1").val("en_US").trigger("chosen:updated").trigger("change");
            }
        }
    },
    appendI18nTableRow: function ($table, key, label, css, index) {
        if (!index) {
            const $tr = $table.find('tbody tr');
            index = $tr.length - 1;
        }
        if (!css || css.trim() === '') {
            css = index % 2 === 0 ? 'even' : 'odd';
        }
        // escape html characters
        key = key.replace(/&/g, '&amp;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&apos;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
        $table.find("tbody").append('<tr class="' + css + '" i18n-key="' + key + '"><td class="label"><span>' + UI.escapeHTML(label) + '</span><textarea name="i18n_key_' + index + '" style="display:none">' + key + '</textarea></td><td class="lang1"></td><td class="lang2"></td></tr>');
    },
    renderLocaleSelector : function(container, header, id, options) {
        $(header).append('<select id="' + id + '" data-placeholder="' + get_advtool_msg('i18n.editor.chooseLocale') + '"><option></option></select>');
        var selector = $(header).find("select");
        for (var i in I18nEditor.languages) {
            $(selector).append('<option>' + I18nEditor.languages[i] + '</option>');
        }
        $(selector).chosen({
            width: "60%"
        }).change(function() {
            var locale = $(this).val();
            $(header).find("a.button, span.actions").remove();
            if (locale !== "") {
                $(header).append('<a class="saveBtn button btn btn-secondary btn-sm">' + get_advtool_msg('i18n.editor.save') + '</a> <span class="actions" style="position:absolute; right:0px; top:16px; display:block; font-size:22px"><a class="downloadPoBtn btn-link" title="' + get_advtool_msg('i18n.editor.generate.po') + '"><i class="las la-file-download"></i></a> <a class="importPoBtn btn-link" title="' + get_advtool_msg('i18n.editor.import.po') + '"><i class="las la-file-upload"></i></a></span>');
            }
            I18nEditor.loadLocale(container, locale, id, options);
        });
        if (UI.rtl) {
            $(selector).addClass("chosen-rtl");
        }
        $(header).off("click", "a.saveBtn");
        $(header).on("click", "a.saveBtn", function() {
            I18nEditor.saveLocale(container, this, $(selector).val(), id, options);
        });
        $(header).off("click", "a.downloadPoBtn");
        $(header).on("click", "a.downloadPoBtn", function() {
            I18nEditor.downloadPo(container, this, $(selector).val(), id, options);
        });
        $(header).off("click", "a.importPoBtn");
        $(header).on("click", "a.importPoBtn", function() {
            I18nEditor.importPo(container, this, $(selector).val(), id, options);
        });
    },
    loadLocale : function (container, locale, id, options) {
        if (options === undefined) {
            options = $(container).find(".i18n_table").data("options");
        }

        if ($(container).find("select#"+id).val() !== locale) {
            $(container).find("select#"+id).val(locale).trigger("change").trigger("chosen:updated");
            return;
        }
        
        if (locale === "") {
            $(container).find("td."+id).html("");
        } else {
            const $container = $(container);
            const $table = $container.find('table.i18n_table');
            const insertLocaleTextarea = function($tr, refreshColumnOnly) {
                const key = $tr.find("td.label textarea").val() + "_" + locale;
                const langs = refreshColumnOnly ? $table.find("thead th." + id) : $table.find("thead th.lang");
                langs.each(function(i, th) {
                    const $elem = $(th);
                    const lang = $elem.data("lang");
                    const shouldAddTextarea = $tr.find("td." + lang + " textarea").length === 0 && $elem.find('.chosen-container .chosen-default').length === 0;
                    if (refreshColumnOnly || shouldAddTextarea) {
                        $tr.find("td." + lang).html('<textarea></textarea>');
                        const $ta = $tr.find("td." + lang + " textarea");
                        $ta.attr("rel", key.toLowerCase());
                        $ta.data("original", "");
                    }
                });
            }
            const insertData = function(message) {
                const mKey = message.messageKey.replace(/"/g, '\\"');
                const hasRow = $table.find('tbody tr[i18n-key="' + mKey + '"]').length > 0;
                if (!hasRow) {
                    return false;
                }
                const mId = message.id.replace(/"/g, '\\"').toLowerCase();
                const $field = $container.find('td.' + id + ' textarea[rel="' + mId + '"]');
                if ($field.attr("rel") === message.id.toLowerCase()) {
                    const v = message.message || "";
                    $field.val(v);
                    $field.data("original", v);
                    return true;
                }
                return false;
            }
            // insert text areas for each row first
            $(container).find("tbody tr:not(.addnew)").each(function(i, tr){
                insertLocaleTextarea($(tr), true);
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
                            // try insert data, if cannot means row not exist -> create new row
                            if (!insertData(message)) {
                                const key = message.messageKey;
                                I18nEditor.appendI18nTableRow($table, key, key);
                                const $tr = $table.find('tr[i18n-key="' + key.replace(/"/g, '\\"') + '"]');
                                insertLocaleTextarea($tr);
                                insertData(message);
                            }
                        }
                    }
                }
            });
        }
    },
    saveLocale : function(container, button, locale, id, options) {
        $(button).hide();
        $(button).after('<i class="las la-spinner la-2x la-spin" style="color:#000;opacity:0.3"></i>');
        var data = [];
        $(container).find('td.'+id+' textarea').each(function(){
            var $ta = $(this);
            var id = $ta.attr("rel");
            var key = $ta.closest("tr").find("td.label textarea").val();
            var val = ($ta.val() || "");
            var orig = ($ta.data("original") || "");
            if (val !== orig) {
                data.push({ 
                    id : id, 
                    key : key, 
                    value : val 
                });
            }
        });
        $.ajax({
            type: "POST",
            url: options.contextPath + '/web/json/console/app/'+options.appId+'/'+options.appVersion+'/message/submit',
            data : {
                locale : locale,
                data : JSON.encode(data)
            },
            dataType : "text"
        }).done(function() {
            $(button).next().remove();
            $(button).after('<span style="color:green;"> '+get_advtool_msg('i18n.editor.saved')+'</span>');
            for (var i=0;i<data.length;i++){
                var rel = data[i].id.replace(/"/g, '\\"').toLowerCase();
                $(container).find('td.'+id+' textarea[rel="'+rel+'"]').each(function(){
                    $(this).data("original", $(this).val() || "");
                });
            }
        }).fail(function() {
            $(button).next().remove();
            $(button).after('<span style="color:red;"> '+get_advtool_msg('i18n.editor.error')+'</span>');
        }).always(function() {
            setTimeout(function(){
                $(button).next().remove();
                $(button).show();
            }, 3000);
        });
    },
    downloadPo: function(container, button, locale, id, options) {
        var text = '# This file was generated by Joget DX\r\n'+
'# http://www.joget.org\r\n'+
'msgid ""\r\n'+
'msgstr ""\r\n'+
'"Content-Type: text/plain; charset=utf-8\\n"\r\n'+
'"Project-Id-Version: '+options.appId+'\\n"\r\n'+
'"POT-Creation-Date: \\n"\r\n'+
'"PO-Revision-Date: \\n"\r\n'+
'"Last-Translator: \\n"\r\n'+
'"Language-Team: \\n"\r\n'+
'"Language: '+locale+'\\n"\r\n'+
'"MIME-Version: 1.0\\n"\r\n\r\n';
        
        $(container).find('td.' + id + ' textarea').each(function() {
            var id = $(this).attr("rel");
            var key = $(this).closest("tr").find("td.label textarea").val();
            var val = $(this).val();
            text += 'msgid "'+key+'"\r\nmsgstr "' + val + '"\r\n\r\n';
        });
        
        var blob = new Blob([text], { type: 'text/x-gettext-translation' });

        var a = document.createElement('a');
        a.download = options.appId + "_" + options.appVersion + "_" + locale + ".po";
        a.href = URL.createObjectURL(blob);
        a.dataset.downloadurl = ['text/x-gettext-translation', a.download, a.href].join(':');
        a.style.display = "none";
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        setTimeout(function() { URL.revokeObjectURL(a.href); }, 1500);
    },
    importPo: function(container, button, locale, id, options) {
        JPopup.show("importPoDialog", options.contextPath + "/web/console/app/"+options.appId+"/"+options.appVersion+"/message/importpo?containerId=" + $(container).attr("id") + "&columnId=" + id + "&lang=" + locale, {}, "");
    }
};