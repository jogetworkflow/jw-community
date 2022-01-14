var Usages = {
    dialog: undefined,
    getUsages : function (id, type, options, callback, otherApp) {
        var url = "";
        if (otherApp) {
            url = "Other";
        }
        $.ajax({
            url: options.contextPath + '/web/json/dependency/app/'+options.appId+'/'+options.appVersion+'/check'+url,
            data: {
                keyword : id,
                type: type
            },
            dataType : "json",
            success: callback
        });
    },
    renderUsage : function(container, response, id, type, options, otherApp) {
        if (response.size > 0) {
            if (otherApp) {
                for (var i in response.usages) {
                    var appUsages = response.usages[i];
                    var ac = $('<li class="usage_app"><h3>'+i+'</h3><ul class="app_items"></ul></li>');
                    container.append(ac);
                    Usages.renderResult($(ac).find(".app_items"), appUsages);
                }
            } else {
                Usages.renderResult(container, response.usages);
            }

            Usages.highlight(container, id);
            $(container).find("a.overlay").on("click", function() {
                AdminBar.showQuickOverlay($(this).attr("href"));
                return false;
            });
            $(container).find(".found_toggle").on("click", function() {
                var toggle = $(this);
                var container = $(this).next(".usage_found");
                if (container.is(":hidden")) {
                    container.show();
                    toggle.find("i").removeClass("icon-angle-down").removeClass("fa-angle-down").addClass("icon-angle-up").addClass("fa-angle-up");
                } else {
                    container.hide();
                    toggle.find("i").removeClass("icon-angle-up").removeClass("fa-angle-up").addClass("icon-angle-down").addClass("fa-angle-down");
                }
            });
        } else {
            var c = $('<li class="no_usage"><h3>'+response.usages+'</h3></li>');
            container.append(c);
        }
    },
    renderResult : function (container, usages) {
        var cat = "";
        var catContainer;
        for (var i in usages) {
            var item = usages[i];
            if (cat !== item.category) {
                cat = item.category;
                var c = $('<li class="usage_category"><h3>'+cat+'</h3><ul class="items"></ul></li>');
                container.append(c);

                catContainer = $(c).find(".items");
            }

            var el = $('<li class="item" data-where="'+item.where+'"></li>');
            catContainer.append(el);
            if (item.link !== undefined) {
                $(el).append('<a class="item_link" target="_blank" href="'+item.link+'">'+UI.escapeHTML(item.label)+'</a>');
            } else {
                $(el).append('<a>'+UI.escapeHTML(item.label)+'</a>');
            }

            if (item.found !== undefined && item.found.length > 0) {
                el.append('<a class="found_toggle"><i class="icon-angle-down fas fa-angle-down"></i></a>');
                var foundContainer = $('<ul class="usage_found" style="display:none"></ul>');
                el.append(foundContainer);

                for (var j in item.found) {
                    foundContainer.append("<li><pre>"+UI.escapeHTML(item.found[j])+"</pre></li>");
                }
            }
        }
    },
    delete: function (id, type, options, deleteCallback) {
        Usages.getUsages(id, type, options, function(response) {
            if (response.size > 0) {
                if (Usages.dialog === undefined) {
                    Usages.dialog = new Boxy('<div id="delete_usage_check"></div>', {
                        title:"",
                        closeable:true,
                        draggable:false,
                        show:false,
                        fixed: true, 
                        modal:true
                    });
                } else {
                    $("#delete_usage_check").html("");
                }
                
                $("#delete_usage_check").append('<h4>' + options.confirmMessage + ' <span><a class="btn btn-primary btn-sm confirm">' + options.confirmLabel + '</a> <a class="btn btn-secondary btn-sm closeBtn">' + options.cancelLabel + '</a></span></h4><div id="usages"><ul class="item_usages_container"></ul></div></div>');
                $("#delete_usage_check a.confirm").on("click", function() {
                    Usages.dialog.hide();
                    deleteCallback();
                });
                $("#delete_usage_check a.closeBtn").on("click", function() {
                    Usages.dialog.hide();
                });
                
                Usages.renderUsage($("#delete_usage_check .item_usages_container"), response, id, type, options);
                Usages.dialog.show();
                UI.adjustPopUpDialog(Usages.dialog);
                
            } else {
                deleteCallback();
            }
        });
    },
    render: function (element, id, type, options) {
        $(element).append('<i class="las la-spinner la-3x la-spin" style="opacity:0.3"></i>');
        $(element).append('<ul class="item_usages_container"></ul>');
        var container = $(element).find(".item_usages_container");
        
        Usages.getUsages(id, type, options, function(response) {
            Usages.renderUsage(container, response, id, type, options);
            $(element).find('i.la-spinner').remove();
        });
    },
    renderOtherApp: function (element, id, type, options) {
        $(element).append('<i class="las la-spinner la-3x la-spin" style="opacity:0.3"></i>');
        $(element).append('<ul class="item_usages_container"></ul>');
        var container = $(element).find(".item_usages_container");
        
        Usages.getUsages(id, type, options, function(response) {
            Usages.renderUsage(container, response, id, type, options, true);
            $(element).find('i.la-spinner').remove();
        }, true);
    },
    highlight : function (element, str) {
        var regex = new RegExp(str, "gi");

        $(element).find(".usage_found").each(function () {
            this.innerHTML = this.innerHTML.replace(regex, function(matched) {
                return "<span class=\"keyword_highlight\">" + matched + "</span>";
            });
        });
    }
};