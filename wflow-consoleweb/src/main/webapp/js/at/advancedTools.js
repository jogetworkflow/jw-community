var AdvancedTools = {
    jsonForm: null,
    options : null,
    json: null,
    isChange:false,
    editor: null,
    treeViewer: null,
    init: function (jsonForm, options) {
        AdvancedTools.jsonForm = jsonForm;
        AdvancedTools.options = options;
        
        var overlayContainer = 
            '<div id="advancedToolsOverlayContainer" class="quickOverlayContainer" style="display:none"><div id="advancedToolsOverlay" class="quickOverlay"></div>\
            <div id="advancedToolsOverlayButton" class="quickOverlayButton"><a href="#" onclick="AdvancedTools.hideQuickOverlay()"><i class="icon-remove"></i></a></div>\
            <div id="advancedToolsFrame" ><h1>'+get_advtool_msg('adv.tool.Advanced.Tools')+'</h1><ul class="builder_tool_tabs"></ul></div></div>';
        $(document.body).append(overlayContainer);
        
        AdvancedTools.initDefinitionTab();
        AdvancedTools.initTreeViewer();
        AdvancedTools.initDiffChecker();
        AdvancedTools.initUsage();
        if (window['FormBuilder'] !== undefined) {
            AdvancedTools.initTable();
        }
        AdvancedTools.initI18n();
        
        $("#builder-bar .controls").append('&nbsp;&nbsp;&nbsp;<a id="advanced_tool" ><i class="fa fa-wrench"></i> '+get_advtool_msg('adv.tool.Advanced.Tools')+'</a>');
        $("#advanced_tool").click(function(){
            $(".builder_tool_tabs li:first-child input").attr("checked", "checked");
            AdvancedTools.showQuickOverlay();
        });
        
    },
    initDefinitionTab: function () {
        var tab = '<li><input type="radio" name="tabs" id="definition" />\
            <label for="definition"><i class="fa fa-code"></i><span>'+get_advtool_msg('adv.tool.JSON.Definition')+'</span></label>\
            <div id="tab-definition" class="tab-content"></div>\
            </li>';
        
        $(".builder_tool_tabs").append(tab);
        $("#tab-definition").append($(AdvancedTools.jsonForm));
        $(AdvancedTools.jsonForm).show();
        $(AdvancedTools.jsonForm).prepend('<pre id="json_definition"></pre>');
        
        AdvancedTools.editor = ace.edit("json_definition");
        AdvancedTools.editor.$blockScrolling = Infinity;
        AdvancedTools.editor.setTheme("ace/theme/textmate");
        AdvancedTools.editor.getSession().setTabSize(4);
        AdvancedTools.editor.getSession().setMode("ace/mode/json");
        AdvancedTools.editor.setAutoScrollEditorIntoView(true);
        AdvancedTools.editor.setOption("maxLines", 1000000); //unlimited, to fix the height issue
        AdvancedTools.editor.setOption("minLines", 10);
        AdvancedTools.editor.resize();
        var textarea = $(AdvancedTools.jsonForm).find('textarea[name="json"]').hide();
        $(textarea).on("change", function() {
            if (!AdvancedTools.silentChange) {
                AdvancedTools.silentChange = true;
                var jsonObj = JSON.decode($(this).val());
                AdvancedTools.editor.getSession().setValue(JSON.stringify(jsonObj, null, 4));
                AdvancedTools.editor.resize(true);
                AdvancedTools.silentChange = false;
            }
        });
        $(textarea).trigger("change");
        AdvancedTools.editor.getSession().on('change', function(){
            if (!AdvancedTools.silentChange) {
                AdvancedTools.silentChange = true;
                var jsonObj = JSON.decode(AdvancedTools.editor.getSession().getValue());
                textarea.val(JSON.encode(jsonObj)).trigger("change");
                AdvancedTools.silentChange = false;
            }
        });
        $(AdvancedTools.jsonForm).find("button").addClass("button").wrap('<div class="sticky-buttons">');
        $(AdvancedTools.jsonForm).find("button").on("click", function() {
            AdvancedTools.isChange = true;
        });
        
    },
    initTreeViewer: function () {
        var tab = '<li><input type="radio" name="tabs" id="treeViewer" />\
            <label for="treeViewer"><i class="fa fa-sitemap"></i><span>'+get_advtool_msg('adv.tool.Tree.Viewer')+'</span></label>\
            <div id="tab-treeViewer" class="tab-content"></div>\
            </li>';
        
        $(".builder_tool_tabs").append(tab);
        
        AdvancedTools.treeViewer = new DependencyTree.Viewer($("#tab-treeViewer"), $(AdvancedTools.jsonForm).find('textarea[name="json"]'), AdvancedTools.options);
        AdvancedTools.treeViewer.init();
        
        $("label[for=treeViewer]").on("click", function() {
            AdvancedTools.treeViewer.render();
        });
    },
    initDiffChecker: function () {
        var tab = '<li><input type="radio" name="tabs" id="diffChecker" />\
            <label for="diffChecker"><i class="fa fa-random"></i><span>'+get_advtool_msg('adv.tool.Diff.Checker')+'</span></label>\
            <div id="tab-diffChecker" class="tab-content"></div>\
            </li>';
        
        $(".builder_tool_tabs").append(tab);
        
        $("label[for=diffChecker]").on("click", function() {
            $("#tab-diffChecker").html("");
            
            var builder;
            if (AdvancedTools.options.builder === "form") {
                builder = FormBuilder;
            } else if (AdvancedTools.options.builder === "datalist") {
                builder = DatalistBuilder;
            } else if (AdvancedTools.options.builder === "userview") {
                builder = UserviewBuilder;
            }
            
            builder.showDiff(function (merge) {
                if ($("#tab-diffChecker").find("#diff1").length > 0) {
                    $("#tab-diffChecker").find("#diff1").before('<h3>'+get_advtool_msg('diff.checker.newChanges')+'</h3>');
                    $("#tab-diffChecker").find("#diff2").before('<h3>'+get_advtool_msg('diff.checker.mergedChanges')+'</h3>');
                    $("#tab-diffChecker").append('<div class="sticky-buttons"><a class="update button">'+get_advtool_msg('diff.checker.merge.update')+'</a></div>')
                } else if ($("#tab-diffChecker").find("#diff2").length > 0) {
                    $("#tab-diffChecker").find("#diff2").before('<h3>'+get_advtool_msg('diff.checker.changes')+'</h3>');
                } else {
                    $("#tab-diffChecker").append('<h3>'+get_advtool_msg('diff.checker.noChanges')+'</h3>');
                }
            }, $("#tab-diffChecker"));
            
            $("#tab-diffChecker").on("click", "a.update", function(){
                builder.merge();
                AdvancedTools.hideQuickOverlay();
            });
        });
    },
    initUsage: function () {
        var tab = '<li><input type="radio" name="tabs" id="checkUsage" />\
            <label for="checkUsage"><i class="fa fa-binoculars"></i><span>'+get_advtool_msg('adv.tool.Usages')+'</span></label>\
            <div id="tab-checkUsage" class="tab-content"></div>\
            </li>';
        
        $(".builder_tool_tabs").append(tab);
        
        $("label[for=checkUsage]").on("click", function() {
            if ($("#tab-checkUsage .item_usages_container").length === 0) {
                $("#tab-checkUsage").prepend('<i class="dt-loading fa fa-5x fa-spinner fa-spin"></i>');
                Usages.render($("#tab-checkUsage"), AdvancedTools.options.id, AdvancedTools.options.builder, AdvancedTools.options);
                $("#tab-checkUsage .dt-loading").remove();
            }
        });
    },
    initTable: function () {
        var tab = '<li><input type="radio" name="tabs" id="formDataTable" />\
            <label for="formDataTable"><i class="fa fa-table"></i><span>'+get_advtool_msg('adv.tool.Table')+'</span></label>\
            <div id="tab-formDataTable" class="tab-content"><div class="table_usage"><h2>'+get_advtool_msg('adv.tool.Table.Usage')+'</h2></div><div class="table_columns"><h2>'+get_advtool_msg('adv.tool.Table.Columns')+'</h2></div></div>\
            </li>';
        
        $(".builder_tool_tabs").append(tab);
        
        $("label[for=formDataTable]").on("click", function() {
            if ($("#tab-formDataTable .table_usage .item_usages_container").length === 0) {
                $("#tab-formDataTable .table_usage").append('<i class="dt-loading fa fa-5x fa-spinner fa-spin"></i>');
                var jsonObj = JSON.decode($(AdvancedTools.jsonForm).find('textarea[name="json"]').val());
                var tableName = jsonObj['properties']['tableName'];
                
                $("#tab-formDataTable .table_usage").data("tableName", tableName);
                Usages.render($("#tab-formDataTable .table_usage"), tableName, "table", AdvancedTools.options);
                $("#tab-formDataTable .table_usage .dt-loading").remove();
            }
            
            if ($("#tab-formDataTable .table_columns .existing_column").length === 0) {
                $("#tab-formDataTable .table_columns").append('<div class="existing_column"><i class="dt-loading fa fa-5x fa-spinner fa-spin"></i></div>');
                
                $.ajax({
                    method: "POST",
                    url: AdvancedTools.options.contextPath + '/web/json/console/app/'+AdvancedTools.options.appId+'/'+AdvancedTools.options.appVersion+'/builder/binder/columns',
                    data : {
                        binderJson: '{"formDefId":"'+AdvancedTools.options.id+'"}',
                        id: 'getColumns',
                        binderId:'org.joget.apps.datalist.lib.FormRowDataListBinder'
                    },
                    dataType : "json",
                    success: function(resp) {
                        if (resp.columns.length > 0) {
                            var ul = $('<ul class="table_column_items">');
                            var fields = [];
                            $("#tab-formDataTable .table_columns .existing_column").append(ul);
                            for (var i in resp.columns) {
                                fields.push(resp.columns[i]['name']);
                            }
                            fields.sort();
                            for (var i in fields) {
                                $(ul).append('<li>'+fields[i]+'</li>');
                            }
                        } else {
                            $("#tab-formDataTable .table_columns .existing_column").append('<ul><li class="no_usage"><h3>'+get_advtool_msg('table.column.noExistingColumns')+'</h3></li></ul>');
                        }
                    },
                    complete: function() {
                        $("#tab-formDataTable .table_columns .existing_column .dt-loading").remove();
                    }
                });
            }
//            
//            if ($("#tab-formDataTable .table_columns .new_column").length === 0) {
//                $("#tab-formDataTable .table_columns").append('<div class="new_column"><i class="dt-loading fa fa-5x fa-spinner fa-spin"></i></div>');
//                
//                $("#tab-formDataTable .table_columns .new_column .dt-loading").remove();
//            }
        });
        
        $(AdvancedTools.jsonForm).find('textarea[name="json"]').on("change", function() {
            var jsonObj = JSON.decode($(this).val());
            var tableName = jsonObj['properties']['tableName'];
            
            if ($("#tab-formDataTable .table_usage").data("tableName") !== tableName) {
                $("#tab-formDataTable .table_usage .item_usages_container").remove();
            }
        });
    },
    initI18n: function () {
        var tab = '<li><input type="radio" name="tabs" id="i18n" />\
            <label for="i18n"><i class="fa fa-language"></i><span>'+get_advtool_msg('adv.tool.i18n')+'</span></label>\
            <div id="tab-i18n" class="tab-content"></div>\
            </li>';
        
        $(".builder_tool_tabs").append(tab);
        
        $("label[for=i18n]").on("click", function() {
            if ($("#tab-i18n .i18n_table").length === 0) {
                $("#tab-i18n").prepend('<i class="dt-loading fa fa-5x fa-spinner fa-spin"></i>');
                I18nEditor.init($("#tab-i18n"), $(AdvancedTools.jsonForm).find('textarea[name="json"]').val(), AdvancedTools.options);
                $("#tab-i18n .dt-loading").remove();
            }
            I18nEditor.refresh($("#tab-i18n"));
        });
        
        $(AdvancedTools.jsonForm).find('textarea[name="json"]').on("change", function() {
            $("#tab-i18n").html("");
        });
    },
    showQuickOverlay: function() {
        $("#advancedToolsOverlayContainer").show();
        $(document.body).addClass("stop-scrolling");
        
        var height = $(window).height() - 200;
        $(".builder_tool_tabs .tab-content").height(height);
        
        $(".quickOverlayButton, #advancedToolsFrame").fadeIn();
        AdvancedTools.json = $(AdvancedTools.jsonForm).find('textarea[name="json"]').val();
        AdvancedTools.isChange = false;
        AdvancedTools.editor.resize(true);
        return false;
    },
    hideQuickOverlay: function() {
        if ($("#advancedToolsOverlayContainer").is(":visible")) {
            //reset the json textarea if does not submit to update
            if (!AdvancedTools.isChange) {
                $(AdvancedTools.jsonForm).find('textarea[name="json"]').val(AdvancedTools.json).trigger("change");
            }

            //calling tree viewer hide to clean extra elements created during generate image
            AdvancedTools.treeViewer.hide();

            $(".quickOverlayButton, #advancedToolsFrame").fadeOut();
            $("#advancedToolsOverlayContainer").hide();
            $(document.body).removeClass("stop-scrolling");
        }
    }
};

