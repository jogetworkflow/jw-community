APMEntries = function(container, args) {
    this.container = container;
    this.args = args;
    this.traceDurationNanos = 0;
    this.render = function() {
        var thisObj = this;
        $(thisObj.container).html("");
        $(thisObj.container).append('<div class="apmentries"></div>');
        
        if (thisObj.args.data.overwritten || thisObj.args.data.expired || thisObj.args.data.entries.length === 0) {
            $(thisObj.container).find(".apmentries").append('<p>'+get_apmviewer_msg('apm.nodata')+'</p>');
        } else {
            var last = thisObj.args.data.entries[thisObj.args.data.entries.length - 1];
            thisObj.traceDurationNanos = Math.max(thisObj.traceDurationNanos, last.startOffsetNanos + last.durationNanos);
                
            for (var i = 0; i < thisObj.args.data.entries.length; i++) {
                thisObj.renderNode($(thisObj.container).find(".apmentries"), thisObj.args.data.entries[i]);
            }
        }
        
        $(thisObj.container).find(".node-trigger").off("click");
        $(thisObj.container).find(".node-trigger").on("click", function() {
            if ($(this).find("i").hasClass("fa-minus-square")) {
                $(this).closest(".entry").find("> .childs").hide();
                $(this).find("i").removeClass("fa-minus-square").addClass("fa-plus-square");
            } else {
                $(this).closest(".entry").find("> .childs").show();
                $(this).find("i").removeClass("fa-plus-square").addClass("fa-minus-square");
            }
        });
    };
    this.renderNode = function(container, node) {
        var thisObj = this;
        var left = Math.floor(node.startOffsetNanos * 50 / thisObj.traceDurationNanos);
        var right = Math.floor((node.startOffsetNanos + node.durationNanos - 1) * 50 / thisObj.traceDurationNanos);
        var offset = 2 * Math.min(left, 50 - 1);
        var duration = 2 * Math.max(1, right - left + 1);
        
        var nodeDom = $('<div class="entry"><div class="info"></div><div class="childs"></div></div>');
        $(nodeDom).find("> .info").append('<div class="barwrapper"><div class="bar"><div class="bar-inner" style="margin-left:'+offset+'px;width:'+duration+'px;"></div></div></div>');
        $(nodeDom).find("> .info").append('<div class="text-wrapper"><a class="node-trigger"><i class="far fa-minus-square"></i></a><div class="text"></div></div>');
        
        if (node.message !== undefined) {
            $(nodeDom).find("> .info .text").append('<span>'+node.message+'</span>');
        } else {
            $(nodeDom).find("> .info .text").append('<span>'+node.queryMessage.prefix+'</span>');
            var textObj = thisObj.args.data.sharedQueryTexts[node.queryMessage.sharedQueryTextIndex];
            if (textObj.fullText !== undefined) {
                $(nodeDom).find("> .info .text").append(' <span>'+textObj.fullText+'</span>');
            } else {
                $(nodeDom).find("> .info .text").append(' <a class="loadtext" data-sha="'+textObj.fullTextSha1+'"><span>'+textObj.truncatedText+'</span> <span class="dots">...</span> <span>'+textObj.truncatedEndText+'</span></a>');
            }
            $(nodeDom).find("> .info .text").append(' <span>'+node.queryMessage.suffix+'</span>');
        }
        $(container).append(nodeDom);
        
        if (node.childEntries !== undefined) {
            $(nodeDom).find("> .info").addClass("hasChild");
            for (var i = 0; i < node.childEntries.length; i++) {
                thisObj.renderNode($(nodeDom).find("> .childs"), node.childEntries[i]);
            }
        }
    };
};
APMTree = function(container, args) {
    this.container = container;
    this.args = args;
    this.renderTree = function() {
        var thisObj = this;
        $(thisObj.container).html("");
        $(thisObj.container).append('<div class="apmtree"></div>');
        if (thisObj.args.data.rootNodes.length > 0) {
            for (var i = 0; i < thisObj.args.data.rootNodes.length; i++) {
                thisObj.renderNode($(thisObj.container).find(".apmtree"), thisObj.args.data.rootNodes[i]);
            }
        } else {
            $(thisObj.container).find(".apmtree").append('<p>'+get_apmviewer_msg('apm.nodata')+'</p>');
        }
        thisObj.initScript();
    };
    this.renderNode = function(container, node, parentNode) {
        var thisObj = this;
        var nextContainer = container;
        
        if (parentNode === undefined || node.sampleCount !== parentNode.sampleCount) {
            var nodeDom = $('<div class="node"><a class="node-trigger"><i class="far fa-minus-square"></i></a><div class="percentage"></div><div class="texts"></div><div class="childs"></div></div>');
            node.container = nodeDom;
            
            var percentage = (node.sampleCount / thisObj.args.data.unfilteredSampleCount) * 100;
            
            $(nodeDom).find(".texts").append('<span>'+node.stackTraceElement+'</span>');
            $(nodeDom).find(".percentage").text(APMViewer.numberFormat(percentage, 1) + "%");
            $(nodeDom).find(".percentage").data("value", percentage);
            
            if ($(container).find(".node").length > 0) {
                var added = false;
                $(container).find(".node").each(function() {
                    var currentPercentage = $(this).find(".percentage").data("value");
                    if (!added && percentage > currentPercentage) {
                        $(this).before(nodeDom);
                        added = true;
                    }
                });
                if (!added) {
                    $(container).append(nodeDom);
                }
            } else {
                $(container).append(nodeDom);
            }
            
            nextContainer = $(nodeDom).find(".childs");
        } else {
            $(parentNode.container).find(".texts").addClass("hasMore");
            $(parentNode.container).find(".texts").append('<span>'+node.stackTraceElement+'</span>');
            node.container = parentNode.container;
        }
        
        if (node.leafThreadState !== undefined) {
            $(nextContainer).after('<div class="nodeState">'+node.leafThreadState+'</div>');
        }
        
        if (node.childNodes !== undefined && node.childNodes.length > 0) {
            for (var i = 0; i < node.childNodes.length; i++) {
                thisObj.renderNode(nextContainer, node.childNodes[i], node);
            }
        }
        
        if ($(nextContainer).find(".node").length === 0) {
            $(nextContainer).parent().find(".node-trigger").css("visibility", "hidden");
        }
    };
    this.initScript = function() {
        var thisObj = this;
        $(thisObj.container).find(".node-trigger").off("click");
        $(thisObj.container).find(".node-trigger").on("click", function() {
            if ($(this).find("i").hasClass("fa-minus-square")) {
                $(this).parent().find("> .childs").hide();
                $(this).find("i").removeClass("fa-minus-square").addClass("fa-plus-square");
            } else {
                $(this).parent().find("> .childs").show();
                $(this).find("i").removeClass("fa-plus-square").addClass("fa-minus-square");
            }
        });
        
        $(thisObj.container).find(".texts.hasMore").off("click");
        $(thisObj.container).find(".texts.hasMore").on("click", function() {
            $(this).toggleClass("show");
        });
    };
};
APMTable = function(table, args) {
    this.table = table;
    this.args = args;
    this.level = 0;
    this.renderTable = function() {
        var thisObj = this;
        $(this.table).html("");
        $(this.table).append('<table class="apmtable"><thead><tr></tr></thead><tbody></tbody></table>');
        
        $.each(this.args.columns, function(i, c) {
            var style = "";
            var cssClass = "";
            if (c.width !== undefined) {
                style = "width:" + c.width + ";";
            }
            if (c.sortable !== undefined && c.sortable === true) {
                cssClass = " sortable";
            }
            
            $(thisObj.table).find('> table > thead > tr').append('<th data-name="'+c.name+'" class="'+cssClass+'" style="'+style+'">'+c.label+'</th>');
        });
        
        this.renderRows(this.args.data, 0);
        
        this.initScript();
    };
    this.renderRows = function(rows, level) {
        var thisObj = this;
        
        if (level > this.level) {
            this.level = level;
        }
        
        var i = 0;
        for (var key in rows) {
            var row = rows[key];
            if (thisObj.args.skipRow !== undefined && thisObj.args.skipRow(row)) {
                continue;
            }
            
            var css = "row";
            if (level === 0 && thisObj.args.fixedRow !== undefined && i < thisObj.args.fixedRow) {
                css += " fixedrow";
            }
            
            css += " rowlvl" + level;
            
            var htmlrow = $('<tr class="'+css+'"></tr>');
            $.each(this.args.columns, function(i, c) {
                var value = "";
                if (c.value !== undefined) {
                    value = c.value(row);
                } else {
                    if ($.isFunction(row[c.name])) {
                        value = row[c.name]();
                    } else {
                        value = row[c.name];
                    }
                }
                var formattedValue = value;
                if (c.format !== undefined) {
                    formattedValue = c.format(value, row);
                }
                
                var style = "";
                if (c.align !== undefined) {
                    style = ' style="text-align:'+c.align+'"';
                }
                
                var type = "";
                if (c.isString === true) {
                    type = 'data-type="text"';
                    if (!(c.sortable !== undefined && c.sortable === true)) {
                        value = "";
                    }
                    $(htmlrow).append('<td '+type+' data-value="'+value+'" '+style+'><div>'+formattedValue+'</div></td>');
                } else {
                    type = 'data-type="number"';
                    $(htmlrow).append('<td '+type+' data-value="'+value+'" '+style+'><div>'+formattedValue+'</div></td>');
                }
            });
            
            $(thisObj.table).find('> table > tbody').append(htmlrow);
            
            if (this.args.hasDetail !== undefined && this.args.hasDetail(row)) {
                $(htmlrow).find('td:eq(0)').append('<a class="info-trigger"><i class="fas fa-info-circle"></i> <i class="arrow fas fa-chevron-down"></i></a> ');
                $(htmlrow).data("details", row);
            }
            
            if (this.args.subgroup !== undefined && row[this.args.subgroup] !== undefined && (($.isArray(row[this.args.subgroup]) && row[this.args.subgroup].length > 0) || (!$.isArray(row[this.args.subgroup]) && Object.keys(row[this.args.subgroup]).length > 0))) {
                $(htmlrow).find('td:eq(0)').prepend('<a class="row-trigger"><i class="far fa-plus-square"></i></a> ');
                thisObj.renderRows(row[this.args.subgroup], level + 1);
            }
            
            i++;
        }
    };
    this.initScript = function() {
        var thisObj = this;
        
        $(this.table).find('> table > tbody > tr > td > .info-trigger').off("click");
        $(this.table).find('> table > tbody > tr > td > .info-trigger').on("click", function() {
            var row = $(this).closest("tr");
            
            if ($(this).find("i.arrow").hasClass("fa-chevron-down")) {
                if (!$(row).next().hasClass("row-info")) {
                    var newInfoRow = $('<tr class="row-info"><td colspan="'+thisObj.args.columns.length+'"><div class="row-info-content"></div></td></tr>');
                    $(newInfoRow).data("parentrow", row);
                    $(row).after(newInfoRow);
                    $(newInfoRow).show();
                    thisObj.args.showDetail($(row).data("details"), $(newInfoRow).find(".row-info-content"));
                } else {
                    $(row).next().show();
                }
                $(this).find("i.arrow").addClass("fa-chevron-up").removeClass("fa-chevron-down");
            } else {
                $(row).next().hide();
                $(this).find("i.arrow").addClass("fa-chevron-down").removeClass("fa-chevron-up");
            }
        });
        
        $(this.table).find('> table > tbody > tr > td > .row-trigger').off("click");
        $(this.table).find('> table > tbody > tr > td > .row-trigger').on("click", function() {
            var row = $(this).closest("tr");
            if ($(this).find("i").hasClass("fa-plus-square")) {
                var nextClass = "";
                var filterClass = "";
                
                for (var i = 0; i < thisObj.level + 1; i++) {
                    if ($(row).hasClass("rowlvl" + i)) {
                        nextClass = "." + "rowlvl" + i;
                        filterClass = "." + "rowlvl" + (i+1);
                        break;
                    }
                }
                $(row).nextUntil(nextClass, filterClass).show();
                
                $(this).find("i").removeClass("fa-plus-square").addClass("fa-minus-square");
                $(row).removeClass("row-collapsed").addClass("row-expand");
            } else {
                var nextClass = "";
                for (var i = 0; i < thisObj.level + 1; i++) {
                    if (nextClass !== "") {
                        nextClass += ", ";
                    }
                    nextClass += "." + "rowlvl" + i;
                        
                    if ($(row).hasClass("rowlvl" + i)) {
                        break;
                    }
                }
                $(row).nextUntil(nextClass).hide();
                $(row).nextUntil(nextClass).find("> td > .row-trigger i").removeClass("fa-minus-square").addClass("fa-plus-square");
                $(row).nextUntil(nextClass).find("> td > .info-trigger i.arrow").removeClass("fa-chevron-up").addClass("fa-chevron-down");
                
                $(this).find("i").removeClass("fa-minus-square").addClass("fa-plus-square");
                $(row).removeClass("row-expand").addClass("row-collapsed");
            }
        });
        
        $(this.table).find("> table > thead > tr > th.sortable").off("click");
        $(this.table).find("> table > thead > tr > th.sortable").on("click", function(){
            thisObj.sortTable($(thisObj.table).find("> table > thead > tr > th").index($(this)));
            thisObj.updateOddEven();
        });
        
        if (thisObj.args.defaultSort !== undefined) {
            var sortCol = $(thisObj.table).find("> table > thead > tr > th[data-name='"+thisObj.args.defaultSort+"']");
            if (this.args.defaultSortOrder !== undefined && thisObj.args.defaultSortOrder === "asc") {
                $(sortCol).addClass("sortDesc");
            }
            thisObj.sortTable($(thisObj.table).find("> table > thead > tr > th").index($(sortCol)));
        }
        
        if (thisObj.args.pagging !== undefined) {
            var size = $(thisObj.table).find("> table > tbody > tr").length;
            $(this.table).append('<div class="tablepagging"></div>');
            
            for (var i = 0; i < Math.ceil(size / thisObj.args.pagging); i++) {
                $(this.table).find("> .tablepagging").append('<a>'+(i+1)+'</a>');
            }
            
            $(this.table).find("> .tablepagging > a").off("click");
            $(this.table).find("> .tablepagging > a").on("click", function(){
                $(thisObj.table).find("> .tablepagging > a").removeClass("active");
                $(this).addClass("active");
                
                thisObj.showPage();
                thisObj.updateOddEven();
            });
            $(this.table).find("> .tablepagging > a:eq(0)").addClass("active");
            
            thisObj.showPage();
        }
        
        if ($(thisObj.table).find("> table > tbody > tr").length === 0) {
            $(thisObj.table).find("> table > tbody").append('<tr class="row rowlvl0"><td colspan="'+thisObj.args.columns.length+'">'+get_apmviewer_msg('apm.nodata')+'</td></tr>');
        }
        thisObj.updateOddEven();
    };
    this.showPage = function() {
        if ($(this.table).find("> .tablepagging").length === 0) {
            return;
        }
        
        var thisObj = this;
        
        var page = $(this.table).find("> .tablepagging > a").index($(this.table).find("> .tablepagging > a.active")) + 1;
        
        var pageSize = thisObj.args.pagging;
        
        var from = (page - 1) * pageSize;  
        var to = page * pageSize; 
        $(this.table).find("> table > tbody > tr").hide();
        $(this.table).find("> table > tbody > tr").each(function(i, row) {
            if (i >= from && i < to) {
                $(row).show();
            }
        });
    };
    this.sortTable = function(sortCol, primaryRow) {
        var order = "sortDesc";
        if ($(this.table).find("> table > thead > tr > th:eq("+sortCol+")").hasClass("sortDesc")) {
            order = "sortAsc";
        }
        $(this.table).find("> table > thead > tr > th").removeClass("sort sortAsc sortDesc");
        $(this.table).find("> table > thead > tr > th i").remove();
        $(this.table).find("> table > thead > tr > th:eq("+sortCol+")").addClass("sort " + order);
        if (order === "sortDesc") {
            $(this.table).find("> table > thead > tr > th:eq("+sortCol+")").append('<i class="fas fa-sort-down"></i>');
        } else {
            $(this.table).find("> table > thead > tr > th:eq("+sortCol+")").append('<i class="fas fa-sort-up"></i>');
        }
        this.sortTableRows(sortCol, order);
        this.showPage();
    };
    this.sortTableRows = function(sortCol, order, primaryRow){
        var thisObj = this;
        var rows = null;
        var sortingRows = [];
        if (primaryRow === undefined) {
            rows = $(this.table).find("> table > tbody > tr.rowlvl0:not(.fixedrow)");
        } else {
            var nextClass = "";
            var filterClass = "";
            for (var i = 0; i < this.level + 1; i++) {
                if (nextClass !== "") {
                    nextClass += ", ";
                }
                nextClass += ".rowlvl" + i;
                if ($(primaryRow).hasClass("rowlvl" + i)) {
                    filterClass = ".rowlvl" + (i+1);
                    break;
                }
            }
            rows = $(primaryRow).nextUntil(nextClass, filterClass);
        }
        
        if (rows == null || rows.length === 0) {
            return;
        }
        
        $.each(rows, function(i, row){
            if (thisObj.level > 1) {
                thisObj.sortTableRows(sortCol, order, row);
            }
            sortingRows.push({
                value : $(row).find("td:eq("+sortCol+")").data("value"),
                type : $(row).find("td:eq("+sortCol+")").data("type"),
                row : row
            });
        });
        
        if (primaryRow === undefined && $(thisObj.table).find("> table > tbody > tr.fixedrow").length > 0) {
            primaryRow = $(thisObj.table).find("> table > tbody > tr.fixedrow:last");
            if ($(primaryRow).next().hasClass("row-info")) {
                primaryRow = $(primaryRow).next();
            }
        }
        
        if (sortingRows.length > 1) {
            sortingRows.sort(function(a, b) {
                if (a.type === "text") {
                    var atext = a.value.toLowerCase();
                    var btext = b.value.toLowerCase(); 

                    if (order === "sortDesc") {
                        return ((atext < btext) ? -1 : ((atext > btext) ? 1 : 0));
                    } else {
                        return ((atext > btext) ? -1 : ((atext < btext) ? 1 : 0));
                    }
                } else {
                    var aval = a.value;
                    var bval = b.value; 

                    if (order === "sortDesc") {
                        return ((aval < bval) ? -1 : ((aval > bval) ? 1 : 0));
                    } else {
                        return ((aval > bval) ? -1 : ((aval < bval) ? 1 : 0));
                    }
                }
            });
            $.each(sortingRows, function(i, s){
                var childs = [];
                
                var nextClass = "";
                for (var i = 0; i < thisObj.level + 1; i++) {
                    if (nextClass !== "") {
                        nextClass += ", ";
                    }
                    nextClass += ".rowlvl" + i;
                    if ($(s.row).hasClass("rowlvl" + i)) {
                        break;
                    }
                }
                childs = $(s.row).nextUntil(nextClass);
                
                if (primaryRow !== undefined) {
                    $(primaryRow).after(s.row);
                } else {
                    $(thisObj.table).find("> table > tbody").prepend(s.row);
                }
                if (childs.length > 0) {
                    $(s.row).after(childs);
                }
            });
        }
    };
    this.updateOddEven = function(currentRow) {
        var thisObj = this;
        var rows = null;
        if (currentRow !== undefined) {
            var nextClass = "";
            var filterClass = "";
            for (var i = 0; i < this.level + 1; i++) {
                if (nextClass !== "") {
                    nextClass += ", ";
                }
                nextClass += ".rowlvl" + i;
                if ($(currentRow).hasClass("rowlvl" + i)) {
                    filterClass = ".rowlvl" + (i+1);
                    break;
                }
            }
            rows = $(currentRow).nextUntil(nextClass, filterClass);
        } else {
            $(thisObj.table).find("> table > tbody > tr").removeClass("odd").removeClass("even");
            rows = $(thisObj.table).find("> table > tbody > tr.rowlvl0:not(.fixedrow)");
        }
        
        if (rows !== null && rows.length > 0) {
            var i = 0;
            $.each(rows, function(i, r){
                var css = "odd";
                if (i % 2 === 1) {
                    css = "even";
                }
                
                $(r).addClass(css);
                thisObj.updateOddEven($(r));
                
                i++;
            });
            
            $(rows).last().addClass("last");
        }
    };
    this.updateData = function(data){
        this.args.data = data;
        this.renderTable();
    };
};
APMNode = function() {
    this.transaction = null;
    this.displayName = get_apmviewer_msg('apm.overall');
    this.name = get_apmviewer_msg('apm.overall');
    this.patterns = [];
    this.totalDurationNanos = 0;
    this.transactionCount = 0;
    this.slowTrace = 0;
    this.checkSlowTrace = true;
    this.errors = 0;
    this.isTransition = false;
    this.transactionParam = '';
    this.parent = null;
    this.transactions = {};
    this.detailrow = null;
    
    this.average = function() {
        return (this.totalDurationNanos / (1000000 * this.transactionCount));
    };
    this.percentage = function() {
        return (this.totalDurationNanos / APMViewer.apps['OVERALL'].totalDurationNanos) * 100;
    };
    this.throughput = function() {
        return (60 * 1000 * this.transactionCount) / APMViewer.getDuration();
    };
    this.initBase = function(name, displayName, patterns, parent, checkSlowTrace) {
        this.name = name;
        this.displayName = displayName;
        this.patterns = patterns;
        this.parent = parent;
        if (checkSlowTrace !== undefined) {
            this.checkSlowTrace = checkSlowTrace;
        } else if (parent !== undefined) {
            this.checkSlowTrace = parent.checkSlowTrace;
        }
        return this;
    };
    this.updateNode = function(transaction) {
        this.totalDurationNanos += transaction.totalDurationNanos;
        this.transactionCount += transaction.transactionCount;
        this.errors += transaction.errorCount;
        this.slowTrace += transaction.slowTrace;
    };
    this.updateNodeError = function(transaction) {
        this.errors += transaction.errorCount;
    };
    this.addTransaction = function(found, transaction) {
        this.updateNode(transaction);
        
        if (found.length > 2) {
            if (this.transactions["CHILD_OF_" + found[1]] === undefined) {
                this.transactions["CHILD_OF_" + found[1]] = new APMNode().initBase(found[0], found[1], [], this);
            }
            
            if (found[2] === "") {
                found[2] = "/";
            }
        
            this.transactions["CHILD_OF_" + found[1]].addTransaction([found[0], found[2]], transaction);
        } else {
            if (this.transactions[found[1]] === undefined) {
                this.transactions[found[1]] = new APMNode().initBase(found[0], found[1], [], this);
            }
        
            this.transactions[found[1]].updateNode(transaction);
            if (this.transactions[found[1]].parent !== null && this.transactions[found[1]].parent !== undefined) {
                this.transactions[found[1]].isTransition = true;
            }
        }
    };
    this.reset = function() {
        this.totalDurationNanos = 0;
        this.transactionCount = 0;
        this.slowTrace = 0;
        this.errors = 0;
        this.transactions = {};
        this.detailrow = null;
    };
    this.renderDetails = function(detailrow) {
        var thisObj = this;
        if (this.detailrow === null) {
            this.detailrow = detailrow;
            
            if (thisObj.name !== "") {
                this.transactionParam = '&transaction-name=' + encodeURIComponent(thisObj.name);
            }
            
            $(thisObj.detailrow).append('<ul class="tabs"></ul><div class="tab-contents"></div>');
            $(thisObj.detailrow).find(".tabs").append('<li data-tab="response" class="active">'+get_apmviewer_msg('apm.responseTime')+'</li>');
            $(thisObj.detailrow).find(".tabs").append('<li data-tab="slowtrace" class="'+(thisObj.slowTrace === 0?'disabled':'')+'">'+get_apmviewer_msg('apm.slowTrace')+' ('+thisObj.slowTrace+')</li>');
            $(thisObj.detailrow).find(".tabs").append('<li data-tab="errors" class="'+(thisObj.errors === 0?'disabled':'')+'">'+get_apmviewer_msg('apm.errors')+' ('+thisObj.errors+')</li>');
            $(thisObj.detailrow).find(".tabs").append('<li data-tab="queries" class="">'+get_apmviewer_msg('apm.queries')+'</li>');
            $(thisObj.detailrow).find(".tabs").append('<li data-tab="services" class="">'+get_apmviewer_msg('apm.secviceCalls')+'</li>');
            $(thisObj.detailrow).find(".tabs").append('<li data-tab="thread" class="">'+get_apmviewer_msg('apm.threadProfile')+'</li>');

            $(thisObj.detailrow).find(".tab-contents").append('<div class="response"></div>');
            $(thisObj.detailrow).find(".tab-contents .response").append('<div class="charts"><div class="average"></div><div class="percentiles"></div><div class="throughput"></div><div class="statsdata"><div class="chart-container"><div class="chart stats"></div></div></div></div>');
            
            $(thisObj.detailrow).find(".tabs li:not(.disabled)").on("click", function() {
                var tab = $(this).data("tab");
                $(thisObj.detailrow).find(".tab-contents > div").hide();
                $(thisObj.detailrow).find(".tabs > li").removeClass("active");
                $(this).addClass("active");
                if ($(thisObj.detailrow).find(".tab-contents > ."+ tab).length === 0) {
                    thisObj[tab + "Details"]();
                } else {
                    $(thisObj.detailrow).find(".tab-contents > ."+ tab).show();
                }
            });
                
            APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['average'] + this.transactionParam + APMViewer.getTimeRange(), function(data){
                $(thisObj.detailrow).find(".tab-contents .response .charts .average").append('<div class="chart-container"><div class="chart"></div></div>');
                
                var legend = [];
                for (var i = 0; i < data.dataSeries.length; i++ ) {
                    data.dataSeries[i].type = 'line';
                    if (data.dataSeries[i].name === undefined) {
                        data.dataSeries[i].name = "Others";
                    }
                    
                    legend.push(data.dataSeries[i].name);
                }
                
                var chart = echarts.init($(thisObj.detailrow).find(".tab-contents .response .charts .average .chart")[0]);
                var option = {
                    title: {
                        text: get_apmviewer_msg('apm.average'),
                        textStyle: {
                            fontSize : 14
                        }
                    },
                    dataZoom: [{
                        type: 'slider'
                    }],
                    grid: {
                        top: 65,
                        bottom: 80,
                        left: 40
                    },
                    legend: {
                        data: legend,
                        padding: [20, 5, 5, 5],
                        textStyle: {
                            fontSize : 8
                        }
                    },           
                    tooltip: {
                        trigger: 'axis',
                        formatter: function (params) {
                            var todate = new Date(params[0].axisValue);
                            var fromdate = new Date(params[0].axisValue - data.dataPointIntervalMillis);
                            var count = data.transactionCounts[params[0].axisValue];
                            
                            var html = "<span>"+ moment(fromdate).format("HH:mm") + " - " + moment(todate).format("HH:mm") + "</span><br/>";
                            if (count !== undefined) {
                                html += '<span>'+count+' '+get_apmviewer_msg('apm.transactions')+'</span>';
                            }
                            
                            html += "<table style=\"color:#fff;\">";
                            for (var i = 0; i < params.length; i ++) {
                                var value = APMViewer.numberFormat(params[i].data[1], 2) + "ms";
                                html += "<tr><td>"+params[i].marker+" <strong style=\"color:#fff\">"+data.dataSeries[i].name+"</strong></td><td style=\"text-align:right\">"+value+"</td></tr>";
                            }
                            html += "</table>";
                            return html;
                        },
                        axisPointer: {
                            animation: false
                        }
                    },
                    xAxis: {
                        type: 'time',
                        min: APMViewer.currenttime - APMViewer.getDuration(),
                        max: APMViewer.currenttime
                    },
                    yAxis: {
                        type: 'value',
                        name: get_apmviewer_msg('apm.milliseconds'),
                        nameRotate : 90,
                        nameLocation : 'center',
                        nameGap : 25
                    },
                    series: data.dataSeries
                };
                chart.setOption(option);
                $( window ).resize(function() {
                    chart.resize();
                });
                
                var stats = $(thisObj.detailrow).find(".tab-contents .response .stats");
                $(stats).append("<h4>"+get_apmviewer_msg('apm.jvmStats')+"</h4><table></table>");
                $(stats).find("table").append("<tr><td>"+get_apmviewer_msg('apm.cpuTime')+"</td><td>"+APMViewer.numberFormat(data.mergedAggregate.mainThreadStats.totalCpuNanos / (1000000 * data.mergedAggregate.transactionCount))+get_apmviewer_msg('apm.ms')+"</td></tr>");
                $(stats).find("table").append("<tr><td>"+get_apmviewer_msg('apm.blockedTime')+"</td><td>"+APMViewer.numberFormat(data.mergedAggregate.mainThreadStats.totalBlockedNanos / (1000000 * data.mergedAggregate.transactionCount))+get_apmviewer_msg('apm.ms')+"</td></tr>");
                $(stats).find("table").append("<tr><td>"+get_apmviewer_msg('apm.waitedTime')+"</td><td>"+APMViewer.numberFormat(data.mergedAggregate.mainThreadStats.totalWaitedNanos / (1000000 * data.mergedAggregate.transactionCount))+get_apmviewer_msg('apm.ms')+"</td></tr>");
                $(stats).find("table").append("<tr><td>"+get_apmviewer_msg('apm.allocatedMemory')+"</td><td>"+APMViewer.numberFormat(data.mergedAggregate.mainThreadStats.totalAllocatedBytes / (1000000 * data.mergedAggregate.transactionCount))+get_apmviewer_msg('apm.mb')+"</td></tr>");
            });
            
            APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['percentiles'] + this.transactionParam + APMViewer.getTimeRange(), function(data){
                $(thisObj.detailrow).find(".tab-contents .response .charts .percentiles").append('<div class="chart-container"><div class="chart"></div></div>');
                
                var legend = [];
                for (var i = 0; i < data.dataSeries.length; i++ ) {
                    data.dataSeries[i].type = 'line';
                    if (data.dataSeries[i].name === undefined) {
                        data.dataSeries[i].name = get_apmviewer_msg('apm.others');
                    }
                    
                    legend.push(data.dataSeries[i].name);
                }
                
                var chart = echarts.init($(thisObj.detailrow).find(".tab-contents .response .charts .percentiles .chart")[0]);
                var option = {
                    title: {
                        text: get_apmviewer_msg('apm.percentiles'),
                        textStyle: {
                            fontSize : 14
                        }
                    },
                    dataZoom: [{
                        type: 'slider'
                    }],
                    grid: {
                        top: 50,
                        bottom: 80,
                        left: 50
                    },
                    legend: {
                        data: legend,
                        padding: [20, 5, 5, 5],
                        textStyle: {
                            fontSize : 8
                        }
                    },           
                    tooltip: {
                        trigger: 'axis',
                        formatter: function (params) {
                            var todate = new Date(params[0].axisValue);
                            var fromdate = new Date(params[0].axisValue - data.dataPointIntervalMillis);
                            var count = data.transactionCounts[params[0].axisValue];
                            
                            var html = "<span>"+ moment(fromdate).format("HH:mm") + " - " + moment(todate).format("HH:mm") + "</span><br/>";
                            if (count !== undefined) {
                                html += '<span>'+count+' '+get_apmviewer_msg('apm.transactions')+'</span>';
                            }
                            html += "<table style=\"color:#fff;\">";
                            for (var i = 0; i < params.length; i ++) {
                                var value = APMViewer.numberFormat(params[i].data[1], 2) + "ms";
                                html += "<tr><td>"+params[i].marker+" <strong style=\"color:#fff\">"+data.dataSeries[i].name+"</strong></td><td style=\"text-align:right\">"+value+"</td></tr>";
                            }
                            html += "</table>";
                            return html;
                        },
                        axisPointer: {
                            animation: false
                        }
                    },
                    xAxis: {
                        type: 'time',
                        min: APMViewer.currenttime - APMViewer.getDuration(),
                        max: APMViewer.currenttime
                    },
                    yAxis: {
                        type: 'value',
                        name: get_apmviewer_msg('apm.milliseconds'),
                        nameRotate : 90,
                        nameLocation : 'center',
                        nameGap : 40
                    },
                    series: data.dataSeries,
                    color : ['#61a0a8', '#d48265','#c23531']
                };
                chart.setOption(option);
                $( window ).resize(function() {
                    chart.resize();
                });
            });
            
            APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['throughput'] + this.transactionParam + APMViewer.getTimeRange(), function(data){
                $(thisObj.detailrow).find(".tab-contents .response .charts .throughput").append('<div class="chart-container"><div class="chart"></div></div>');
                
                data.dataSeries[0].type = "line";
                var chart = echarts.init($(thisObj.detailrow).find(".tab-contents .response .charts .throughput .chart")[0]);
                var option = {
                    title: {
                        text: get_apmviewer_msg('apm.throughput'),
                        textStyle: {
                            fontSize : 14
                        }
                    },
                    dataZoom: [{
                        type: 'slider'
                    }],
                    grid: {
                        bottom: 80,
                        left: 50
                    },
                    tooltip: {
                        trigger: 'axis',
                        formatter: function (params) {
                            var todate = new Date(params[0].axisValue);
                            var fromdate = new Date(params[0].axisValue - data.dataPointIntervalMillis);

                            var html = "<span>"+ moment(fromdate).format("HH:mm") + " - " + moment(todate).format("HH:mm") + "</span>";
                            html += "<div>"+APMViewer.numberFormat(params[0].data[1], 2)+" "+get_apmviewer_msg('apm.transactionsPM')+"</div>";
                            return html;
                        },
                        axisPointer: {
                            animation: false
                        }
                    },
                    xAxis: {
                        type: 'time',
                        min: APMViewer.currenttime - APMViewer.getDuration(),
                        max: APMViewer.currenttime
                    },
                    yAxis: {
                        type: 'value',
                        name: get_apmviewer_msg('apm.TransactionsPM'),
                        nameRotate : 90,
                        nameLocation : 'center',
                        nameGap : 35
                    },
                    series: data.dataSeries
                };
                chart.setOption(option);
                $( window ).resize(function() {
                    chart.resize();
                });
            });
        }
    };
    this.slowtraceDetails = function() {
        var thisObj = this;
        $(thisObj.detailrow).find(".tab-contents").append('<div class="slowtrace"></div>');
        
        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['points'] + this.transactionParam + APMViewer.getTimeRange(), function(data){
            $(thisObj.detailrow).find(".tab-contents .slowtrace").append('<div class="chart-container"><div class="chart"></div></div>');

            var dataSeries = [{
                name : "normal",
                data : data.normalPoints,
                type : "scatter"
            },
            {
                name : "partial",
                data : data.partialPoints,
                type : "scatter"
            }];
            
            var chart = echarts.init($(thisObj.detailrow).find(".tab-contents .slowtrace .chart")[0]);
            var option = {
                title: {
                    text: ''
                },
                dataZoom: [{
                    type: 'slider'
                }],
                grid: {
                    bottom: 80,
                    left: 90
                },
                xAxis: {
                    type: 'time',
                    min: APMViewer.currenttime - APMViewer.getDuration(),
                    max: APMViewer.currenttime
                },
                yAxis: {
                    type: 'value',
                    name: get_apmviewer_msg('apm.milliseconds'),
                    nameRotate : 90,
                    nameLocation : 'center',
                    nameGap : 75
                },
                series: dataSeries,
                color : ['#61a0a8', '#d48265','#c23531']
            };
            chart.setOption(option);
            chart.on("click", function(param) {
                if (param.seriesType === "scatter") {
                    thisObj.showTraceView(param.data[3]);
                }
            });
            $( window ).resize(function() {
                chart.resize();
            });
        });
    };
    this.errorsDetails = function() {
        var thisObj = this;
        $(thisObj.detailrow).find(".tab-contents").append('<div class="errors"></div>');
        
        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['errpoints'] + thisObj.transactionParam + APMViewer.getTimeRange(), function(data){
            APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['messages'] + thisObj.transactionParam + APMViewer.getTimeRange(), function(data2){
                $(thisObj.detailrow).find(".tab-contents .errors").append('<div class="chart-container"><div class="chart"></div></div>');

                var dataSeries = [{
                    name : get_apmviewer_msg('apm.error'),
                    data : data.errorPoints,
                    type : "scatter"
                },
                {
                    name : get_apmviewer_msg('apm.errorRate'),
                    data : data2.dataSeries.data,
                    type : "line",
                    areaStyle: {},
                    yAxisIndex:1
                }];

                var chart = echarts.init($(thisObj.detailrow).find(".tab-contents .errors .chart")[0]);
                var option = {
                    title: {
                        text: ''
                    },
                    legend: {
                        data: [dataSeries[0].name, dataSeries[1].name]
                    },
                    dataZoom: [{
                        type: 'slider'
                    }],
                    tooltip: {
                        trigger: 'axis',
                        formatter: function (params) {
                            if (params[0].seriesType === "line") {
                                var todate = new Date(params[0].axisValue);
                                var fromdate = new Date(params[0].axisValue - data2.dataPointIntervalMillis);

                                var html = "<span>"+ moment(fromdate).format("HH:mm") + " - " + moment(todate).format("HH:mm") + "</span>";

                                html += "<div>"+get_apmviewer_msg('apm.errorRate')+": "+APMViewer.numberFormat(params[0].data[1], 2)+"%</div>";
                                if (data2.dataSeriesExtra[params[0].axisValue] !== undefined) {
                                    html += "<div>"+get_apmviewer_msg('apm.errorCount')+": "+data2.dataSeriesExtra[params[0].axisValue][0]+"</div>";
                                    html += "<div>"+get_apmviewer_msg('apm.transactionCount')+": "+data2.dataSeriesExtra[params[0].axisValue][1]+"</div>";
                                }
                                return html;
                            }
                            return null;
                        },
                        axisPointer: {
                            animation: false
                        }
                    },
                    grid: {
                        bottom: 80,
                        left: 90
                    },
                    xAxis: {
                        type: 'time',
                        min: APMViewer.currenttime - APMViewer.getDuration(),
                        max: APMViewer.currenttime
                    },
                    yAxis: [{
                        type: 'value',
                        name: get_apmviewer_msg('apm.milliseconds'),
                        nameRotate : 90,
                        nameLocation : 'center',
                        nameGap : 75
                    },
                    {
                        type: 'value',
                        name: get_apmviewer_msg('apm.errorRate')+' %',
                        nameRotate : 90,
                        nameLocation : 'center',
                        nameGap : 25
                    }],
                    series: dataSeries,
                    color : ['#c23531', '#d48265']
                };
                chart.setOption(option);
                chart.on("click", function(param) {
                    if (param.seriesType === "scatter") {
                        thisObj.showTraceView(param.data[3]);
                    }
                });
                $( window ).resize(function() {
                    chart.resize();
                });
                
                $(thisObj.detailrow).find(".tab-contents .errors").append('<div class="errors-container"></div>');
                var table = new APMTable($(thisObj.detailrow).find(".tab-contents .errors .errors-container"), {
                    columns : [
                        {
                            name : "message", 
                            label : get_apmviewer_msg('apm.message'), 
                            width : "80%", 
                            sortable : false,
                            isString : true
                        },
                        {
                            name : "count", 
                            label : get_apmviewer_msg('apm.count'), 
                            sortable : true
                        }
                    ],
                    data : data2.errorMessages,
                    pagging : 10
                });
                table.renderTable();
            });
        });
    };
    this.queriesDetails = function() {
        var thisObj = this;
        $(thisObj.detailrow).find(".tab-contents").append('<div class="queries"></div>');
        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['queries'] + this.transactionParam + APMViewer.getTimeRange(), function(data){
            var table = new APMTable($(thisObj.detailrow).find(".tab-contents .queries"), {
                columns : [
                    {
                        name : "truncatedQueryText", 
                        label : get_apmviewer_msg('apm.query'), 
                        width : "50%", 
                        sortable : false,
                        isString : true
                    },
                    {
                        name : "totalDurationNanos", 
                        label : get_apmviewer_msg('apm.totalTime'), 
                        sortable : true,
                        format : function(value) {
                            return APMViewer.numberFormat(value/1000000, 1) + get_apmviewer_msg('apm.ms');
                        }
                    },
                    {
                        name : "executionCount", 
                        label : get_apmviewer_msg('apm.totalCount'), 
                        sortable : true
                    },
                    {
                        name : "averageTime", 
                        label : get_apmviewer_msg('apm.averageTime'), 
                        sortable : true,
                        value : function(obj) {
                            return obj.totalDurationNanos / obj.executionCount;
                        },
                        format : function(value) {
                            return APMViewer.numberFormat(value/1000000, 1) + get_apmviewer_msg('apm.ms');
                        }
                    },
                    {
                        name : "totalRows", 
                        label : get_apmviewer_msg('apm.averageRows'), 
                        sortable : true,
                        format : function(value) {
                            return APMViewer.numberFormat(value, 1);
                        }
                    }
                ],
                data : data,
                pagging : 10
            });
            table.renderTable();
        });
    };
    this.servicesDetails = function() {
        var thisObj = this;
        $(thisObj.detailrow).find(".tab-contents").append('<div class="services"></div>');
        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['service'] + this.transactionParam + APMViewer.getTimeRange(), function(data){
            var table = new APMTable($(thisObj.detailrow).find(".tab-contents .services"), {
                columns : [
                    {
                        name : "text", 
                        label : "", 
                        width : "50%", 
                        sortable : false,
                        isString : true
                    },
                    {
                        name : "totalDurationNanos", 
                        label : get_apmviewer_msg('apm.totalTime'), 
                        sortable : true,
                        format : function(value) {
                            return APMViewer.numberFormat(value/1000000, 1) + get_apmviewer_msg('apm.ms');
                        }
                    },
                    {
                        name : "executionCount", 
                        label : get_apmviewer_msg('apm.totalCount'), 
                        sortable : true
                    },
                    {
                        name : "averageTime", 
                        label : get_apmviewer_msg('apm.averageTime'), 
                        sortable : true,
                        value : function(obj) {
                            return obj.totalDurationNanos / obj.executionCount;
                        },
                        format : function(value) {
                            return APMViewer.numberFormat(value/1000000, 1) + get_apmviewer_msg('apm.ms');
                        }
                    }
                ],
                data : data,
                pagging : 10
            });
            table.renderTable();
        });
    };
    this.threadDetails = function() {
        var thisObj = this;
        $(thisObj.detailrow).find(".tab-contents").append('<div class="thread"></div>');
        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['profile'] + this.transactionParam + APMViewer.getTimeRange(), function(data){
            $(thisObj.detailrow).find(".tab-contents .thread").append('<div class="threadtree"></div>');
            var tree = new APMTree($(thisObj.detailrow).find(".tab-contents .thread .threadtree"), {
                data : data.profile
            });
            tree.renderTree();
        });
    };
    this.showTraceView = function(traceId) {
        var thisObj = this;
        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['trace'] + traceId, function(data){
            $("#traceview").html("");
            
            var html = "<h3 class=\"boxy-content-header\">" + data.headline + "</h3><div class=\"boxy-content-body trace-info\">";
            html += '<div><label>Transaction type</label> : <span>'+data.transactionType+'</span></div>';
            html += '<div><label>Transaction name</label> : <span>'+data.transactionName+'</span></div>';
            html += '<div><label>Start</label> : <span>'+(new Date(data.startTime))+'</span></div>';
            html += '<div><label>Duration</label> : <span>'+APMViewer.numberFormat((data.durationNanos/1000000), 2)+get_apmviewer_msg('apm.ms')+'</span></div>';
            html += '<div><label>Request http method</label> : <span>'+data.detail["Request http method"]+'</span></div>';
            html += '<div><label>Request parameters</label> : <br/><ul>';
            for (var key in data.detail["Request parameters"]) {
                html += '<li><label>'+key+'</label> : <span>'+data.detail["Request parameters"][key]+'</span></li>';
            }
            html += '</ul></div>';
            html += '<div><label>Response code</label> : <span>'+data.detail["Response code"]+'</span></div>';
            
            html += '<div><label>'+get_apmviewer_msg('apm.breakdown')+'</label> : <br/><table>';
            html += '<thead><tr><th width="50%"></th><th>'+get_apmviewer_msg('apm.totalms')+'</th><th>'+get_apmviewer_msg('apm.count')+'</th></tr></thead><tbody>';
            
            var breakdown = thisObj.breakdownlist(data.mainThreadRootTimer);
            for (var i =0; i < breakdown.length; i++) {
                html+= '<tr><td>'+breakdown[i].name+'</td>';
                html+= '<td style="text-align:right">'+APMViewer.numberFormat(breakdown[i].total/1000000,2)+'</td>';
                html+= '<td style="text-align:right">'+breakdown[i].count+'</td></tr>';
            }
            
            html += '</tbody></table></div>';
            
            html += '<div><label>'+get_apmviewer_msg('apm.jvmStats')+'</label> : <br/><ul>';
            html += '<li><label>'+get_apmviewer_msg('apm.cpuTime')+'</label> : <span>'+APMViewer.numberFormat(data.mainThreadStats.totalCpuNanos/1000000,2)+get_apmviewer_msg('apm.ms')+'</span></li>';
            html += '<li><label>'+get_apmviewer_msg('apm.blockedTime')+'</label> : <span>'+APMViewer.numberFormat(data.mainThreadStats.totalBlockedNanos/1000000,2)+get_apmviewer_msg('apm.ms')+'</span></li>';
            html += '<li><label>'+get_apmviewer_msg('apm.waitedTime')+'</label> : <span>'+APMViewer.numberFormat(data.mainThreadStats.totalWaitedNanos/1000000,2)+get_apmviewer_msg('apm.ms')+'</span></li>';
            html += '<li><label>'+get_apmviewer_msg('apm.allocatedMemory')+'</label> : <span>'+APMViewer.numberFormat(data.mainThreadStats.totalAllocatedBytes/1000000,2)+get_apmviewer_msg('apm.mb')+'</span></li>';
            html += '</ul></div>';
            
            if (data.entryCount > 0) {
                html += '<div class="trace-entries additional"><a>Trace entries ('+data.entryCount+')</a></div>';
            }
            if (data.queryCount > 0) {
                html += '<div class="trace-query additional"><a>Query stats ('+data.queryCount+')</a></div>';
            }
            if (data.mainThreadProfileSampleCount > 0) {
                html += '<div class="trace-profile additional"><a>Profile ('+data.mainThreadProfileSampleCount+')</a></div>';
            }
            html += '</div>';
            
            $("#traceview").html(html);
            
            if (data.entryCount > 0) {
                $("#traceview").find('.trace-entries a').off("click");
                $("#traceview").find('.trace-entries a').on("click", function() {
                    if ($("#traceview").find('.trace-entries .content').length === 0) {
                        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['traceEntries'] + traceId, function(data){
                            $("#traceview").find('.trace-entries').append('<div class="content"></div>');
                            var entries = new APMEntries($("#traceview").find('.trace-entries .content'), {
                                data : data
                            });
                            entries.render();
                        });
                    } else {
                        if ($("#traceview").find('.trace-entries .content').is(":visible")) {
                            $("#traceview").find('.trace-entries .content').hide();
                        } else {
                            $("#traceview").find('.trace-entries .content').show();
                        }
                    }
                });
            }
            if (data.queryCount > 0) {
                $("#traceview").find('.trace-query a').off("click");
                $("#traceview").find('.trace-query a').on("click", function() {
                    if ($("#traceview").find('.trace-query .content').length === 0) {
                        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['traceQueries'] + traceId, function(data){
                            $("#traceview").find('.trace-query').append('<div class="content"></div>');
                            var table = new APMTable($("#traceview").find('.trace-query .content'), {
                                columns : [
                                    {
                                        name : "query", 
                                        label : get_apmviewer_msg('apm.query'), 
                                        width : "50%", 
                                        sortable : false,
                                        isString : true,
                                        value : function(obj) {
                                            var text = "";
                                            var textObj = data.sharedQueryTexts[obj.sharedQueryTextIndex];
                                            if (textObj !== undefined) {
                                                if (textObj.fullTextSha1 !== undefined) {
                                                    text = '<a class="loadtext" data-sha="'+textObj.fullTextSha1+'">';
                                                    text += '<span>'+textObj.truncatedText+'</span>';
                                                    text += ' <span class="dots">...</span> ';
                                                    text += '<span>'+textObj.truncatedEndText+'</span></a>';
                                                } else {
                                                    text = textObj.fullText;
                                                }
                                            }
                                            return text;
                                        }
                                    },
                                    {
                                        name : "totalDurationNanos", 
                                        label : get_apmviewer_msg('apm.totalTime'), 
                                        sortable : true,
                                        format : function(value) {
                                            return APMViewer.numberFormat(value/1000000, 1) + get_apmviewer_msg('apm.ms');
                                        }
                                    },
                                    {
                                        name : "executionCount", 
                                        label : get_apmviewer_msg('apm.totalCount'), 
                                        sortable : true
                                    },
                                    {
                                        name : "averageTime", 
                                        label : get_apmviewer_msg('apm.averageTime'), 
                                        sortable : true,
                                        value : function(obj) {
                                            return obj.totalDurationNanos / obj.executionCount;
                                        },
                                        format : function(value) {
                                            return APMViewer.numberFormat(value/1000000, 1) + get_apmviewer_msg('apm.ms');
                                        }
                                    },
                                    {
                                        name : "totalRows", 
                                        label : get_apmviewer_msg('apm.averageRows'), 
                                        sortable : true,
                                        format : function(value) {
                                            return APMViewer.numberFormat(value, 1);
                                        }
                                    }
                                ],
                                data : data.queries,
                                pagging : 10
                            });
                            table.renderTable();
                        });
                    } else {
                        if ($("#traceview").find('.trace-query .content').is(":visible")) {
                            $("#traceview").find('.trace-query .content').hide();
                        } else {
                            $("#traceview").find('.trace-query .content').show();
                        }
                    }
                });
            }
            if (data.mainThreadProfileSampleCount > 0) {
                $("#traceview").find('.trace-profile a').off("click");
                $("#traceview").find('.trace-profile a').on("click", function() {
                    if ($("#traceview").find('.trace-profile .content').length === 0) {
                        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['traceProfile'] + traceId, function(data){
                            $("#traceview").find('.trace-profile').append('<div class="content"></div>');
                            var tree = new APMTree($("#traceview").find('.trace-profile .content'), {
                                data : data
                            });
                            tree.renderTree();
                        });
                    } else {
                        if ($("#traceview").find('.trace-profile .content').is(":visible")) {
                            $("#traceview").find('.trace-profile .content').hide();
                        } else {
                            $("#traceview").find('.trace-profile .content').show();
                        }
                    }
                });
            }
            
            APMViewer.popup.show();
            APMViewer.popup.center('x');
            APMViewer.popup.center('y');
        });
    };
    this.breakdownlist = function(data, list) {
        var thisObj = this;
        var breakdown = {};
        
        if (list !== undefined) {
            breakdown = list;
        }
        
        if (breakdown[data.name] === undefined) {
            breakdown[data.name] = {
                name : data.name,
                total : data.totalNanos,
                count : data.count
            };
        } else {
            breakdown[data.name].total += data.totalNanos;
            breakdown[data.name].count += data.count;
        }
        
        if (data.childTimers !== undefined) {
            for (var i = 0; i < data.childTimers.length; i++) {
                breakdown = thisObj.breakdownlist(data.childTimers[i], breakdown);
            }
        }
        
        if (list === undefined) {
            var newList = [];
            for (var k in breakdown) {
                newList.push(breakdown[k]);
                
                newList.sort(function(a, b) {
                    var aval = a.total;
                    var bval = b.total; 

                    return ((aval > bval) ? -1 : ((aval < bval) ? 1 : 0));
                });
            }
            return newList;
        } else {
            return breakdown;
        }
    };
};
APMViewer = {
    urls : {
        'base' : '/web/json/console/monitor/apm/retrieve/',
        'summary' : 'summary?agent-rollup-id=',
        'average' : 'transaction/average?agent-rollup-id=&transaction-type=Web',
        'percentiles' : 'transaction/percentiles?agent-rollup-id=&transaction-type=Web&percentile=80&percentile=95&percentile=99',
        'throughput' : 'transaction/throughput?agent-rollup-id=&transaction-type=Web',
        'trackcount' : 'transaction/trace-count?agent-rollup-id=&transaction-type=Web',
        'queries' : 'transaction/queries?agent-rollup-id=&transaction-type=Web',
        'service' : 'transaction/service-calls?agent-rollup-id=&transaction-type=Web',
        'profile' : 'transaction/profile?agent-rollup-id=&transaction-type=Web&auxiliary=false&truncate-branch-percentage=0.1',
        'messages' : 'error/messages?agent-rollup-id=&transaction-type=Web&error-message-limit=50',
        'gauges' : 'jvm/gauges?agent-rollup-id=&gauge-name=java.lang%3Atype%3DMemory%3AHeapMemoryUsage.used&gauge-name=java.lang%3Atype%3DOperatingSystem%3AFreePhysicalMemorySize&gauge-name=java.lang%3Atype%3DOperatingSystem%3AProcessCpuLoad&gauge-name=java.lang%3Atype%3DOperatingSystem%3ASystemCpuLoad',
        'points' : 'transaction/points?agent-rollup-id=&transaction-type=Web&duration-millis-low=0&headline-comparator=begins&headline=&error-message-comparator=begins&error-message=&user-comparator=begins&user=&attribute-name=&attribute-value-comparator=begins&attribute-value=&limit=1000',
        'errpoints' : 'error/points?agent-rollup-id=&transaction-type=Web&duration-millis-low=0&headline-comparator=begins&headline=&error-message-comparator=begins&error-message=&user-comparator=begins&user=&attribute-name=&attribute-value-comparator=begins&attribute-value=&limit=1000',
        'trace' : 'trace/header?agent-id=&trace-id=',
        'traceEntries' : 'trace/entries?agent-id=&trace-id=',
        'traceQueries' : 'trace/queries?agent-id=&trace-id=',
        'traceProfile' : 'trace/main-thread-profile?agent-id=&trace-id=',
        'fullQueryText' : 'transaction/full-query-text?agent-rollup-id=&full-text-sha1=',
        'config' : 'config/alerts?agent-rollup-id=',
        'removeAlert' : 'config/alerts/remove?agent-rollup-id=',
        'updateAlert' : 'config/alerts/update?agent-rollup-id=',
        'addAlert' : 'config/alerts/add?agent-rollup-id=',
        'smtp' : 'admin/smtp',
        'sendTestEmail' : 'admin/send-test-email',
        'adminGeneral' : 'admin/general',
        'apps' : '/web/json/console/app/list'
    },
    deferreds : [],
    contextPath : '',
    totalMemory : 0,
    maxheap : 0,
    title : '',
    isVirtualHostEnabled : false,
    apps : {},
    currenttime : null,
    table : null,
    popup : null,
    alertPopup : null,
    alertTexts : {
       'java.lang / Memory / HeapMemoryUsage / used - average' : get_apmviewer_msg('apm.heapMemoryUsage'),
       'java.lang / OperatingSystem / FreePhysicalMemorySize - average' : get_apmviewer_msg('apm.freePhysicalMemory'),
       'java.lang / OperatingSystem / ProcessCpuLoad - average' : get_apmviewer_msg('apm.processCPULoad'),
       'java.lang / OperatingSystem / SystemCpuLoad - average' : get_apmviewer_msg('apm.systemCPULoad'),
       'error count' : get_apmviewer_msg('apm.errorCount'),
       'error rate' : get_apmviewer_msg('apm.errorRate')
    },
    init : function(contextPath, totalMemory, maxheap, title, isVirtualHostEnabled) {
        APMViewer.contextPath = contextPath;
        APMViewer.totalMemory = totalMemory;
        APMViewer.maxheap = maxheap;
        APMViewer.title = title;
        APMViewer.isVirtualHostEnabled = isVirtualHostEnabled;
        
        var tool = $('<div class="apmtool"></div>');
        $(tool).append('<a class="refresh"><i class="fas fa-sync-alt"></i></a>');
        $(tool).append(' <select class="durationSelector"></select>');
        $(tool).append(' <a class="alert"><i class="fas fa-bell"></i></a>');
        $(tool).find(".durationSelector").append('<option value="1800000">'+get_apmviewer_msg('apm.last30m')+'</option>');
        $(tool).find(".durationSelector").append('<option value="3600000">'+get_apmviewer_msg('apm.last60m')+'</option>');
        $(tool).find(".durationSelector").append('<option value="7200000">'+get_apmviewer_msg('apm.last2h')+'</option>');
        $(tool).find(".durationSelector").append('<option value="14400000" selected>'+get_apmviewer_msg('apm.last4h')+'</option>');
        $(tool).find(".durationSelector").append('<option value="28800000">'+get_apmviewer_msg('apm.last8h')+'</option>');
        $(tool).find(".durationSelector").append('<option value="86400000">'+get_apmviewer_msg('apm.last24h')+'</option>');
        $(tool).find(".durationSelector").append('<option value="172800000">'+get_apmviewer_msg('apm.last2d')+'</option>');
        $(tool).find(".durationSelector").append('<option value="604800000">'+get_apmviewer_msg('apm.last7d')+'</option>');
        $(tool).find(".durationSelector").append('<option value="2592000000">'+get_apmviewer_msg('apm.last30d')+'</option>');
        
        $(".apmviewer").parent().prepend(tool);
        
        $('.apmtool .refresh').on("click", function(){
            APMViewer.refresh();
        });
        
        $('.apmtool .alert').on("click", function(){
            APMViewer.showManageAlert();
        });
        
        $('.apmtool .durationSelector').on("change", function(){
            APMViewer.refresh();
        });
        
        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['apps'], function(data){
            APMViewer.loadAppsList(data);
            APMViewer.refresh();
        });
        
        APMViewer.popup = new Boxy(
            '<div id="traceview"></div>',
            {
                title: '&nbsp;',
                closeText : '<i class="fas fa-times"></i>',
                closeable: true,
                draggable: false,
                show: false,
                fixed: true,
                modal: true,
                afterShow : function() {
                    $('.boxy-modal-blackout').off('click');
                    $('.boxy-modal-blackout').on('click',function(){
                        APMViewer.popup.hide();
                        $('.boxy-modal-blackout').off('click');
                    });
                }
            });
            
        APMViewer.alertPopup = new Boxy(
            '<div id="alertpopup"></div>',
            {
                title: '&nbsp;',
                closeText : '<i class="fas fa-times"></i>',
                closeable: true,
                draggable: false,
                show: false,
                fixed: true,
                modal: true,
                afterShow : function() {
                    $('.boxy-modal-blackout').off('click');
                    $('.boxy-modal-blackout').on('click',function(){
                        APMViewer.alertPopup.hide();
                        $('.boxy-modal-blackout').off('click');
                    });
                }
            });    
            
        $("body").on("click", ".loadtext", function() {
            var thisObj = this;
            var sha = $(this).data("sha");
            APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['fullQueryText'] + sha, function(data){
                $(thisObj).replaceWith(data.fullText);
            });
        });    
    },
    showManageAlert : function() {
        var thisObj = this;
        
        if ($("#alertpopup").find("ul").length === 0) {
            $("#alertpopup").append('<h3 class=\"boxy-content-header\">' + get_apmviewer_msg('apm.performanceAlert') + '</h3><div class="boxy-content-body alert-info"><ul class="tabs"></ul><div class="tab-contents"></div></div>');
            $("#alertpopup").find(".tabs").append('<li data-tab="alert" class="active">'+get_apmviewer_msg('apm.manageAlert')+'</li>');
            $("#alertpopup").find(".tabs").append('<li data-tab="smtp" class="">'+get_apmviewer_msg('apm.smtp')+'</li>');

            $("#alertpopup").find(".tabs li").on("click", function() {
                var tab = $(this).data("tab");
                $("#alertpopup").find(".tab-contents > div").hide();
                $("#alertpopup").find(".tabs > li").removeClass("active");
                $(this).addClass("active");
                if ($("#alertpopup").find(".tab-contents > ."+ tab).length === 0) {
                    thisObj[tab + "View"]();
                } else {
                    $("#alertpopup").find(".tab-contents > ."+ tab).show();
                }
            });

            $("#alertpopup").find(".tab-contents").append('<div class="alert"></div>');
        } else {
            $("#alertpopup").find(".tab-contents > div").hide();
            $("#alertpopup").find(".tabs > li").removeClass("active");
            $("#alertpopup").find(".tabs > li:eq(0)").addClass("active");
            $("#alertpopup").find(".tab-contents > .alert").show();
        }
            
        APMViewer.loadAlertList(function(){
            APMViewer.alertPopup.show();
            APMViewer.alertPopup.center('x');
            APMViewer.alertPopup.center('y');
        });
    },
    loadAlertList : function(callback) {
        var thisObj = this;
        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['config'], function(data){
            $("#alertpopup .alert").html('<div class="apmtableview"></div>');
            
            var list = [];
            var reg = new RegExp('^([^-]+) - (.+) over the last (.+) minute[s]* is (.+) than or equal to (.+)$');
            $.each(data, function(i, d){
                var found = d.display.match(reg);
                if (APMViewer.alertTexts[found[2]] !== undefined) {
                    list.push({
                        id: d.version,
                        name : APMViewer.alertTexts[found[2]],
                        time : found[3],
                        operator : found[4],
                        threshold : found[5]
                    });
                }
            });
            
            var table = new APMTable($("#alertpopup .alert .apmtableview"), {
                columns : [
                    {
                        name : "name", 
                        label : get_apmviewer_msg('apm.name'), 
                        width : "50%", 
                        sortable : true,
                        isString : true
                    },
                    {
                        name : "time", 
                        label : get_apmviewer_msg('apm.timeperiod'), 
                        sortable : true
                    },
                    {
                        name : "threshold", 
                        label : get_apmviewer_msg('apm.threshold'), 
                        sortable : true,
                        isString : true,
                        format : function(value, obj) {
                            var text = '';
                            if (obj['operator'] === 'greater') {
                                text += ">= ";
                            } else {
                                text += "<= ";
                            }
                            
                            text += value.replace("percent", "%");
                            
                            return text;
                        }
                    },
                    {
                        name : "id", 
                        label : '', 
                        sortable : false,
                        isString : true,
                        align : 'center',
                        format : function(value, obj) {
                            return '<a class="rowaction edit" data-id="'+value+'" style="margin-right:10px;"><i class="fas fa-pencil-alt"></i></a> <a style="color:red;" class="rowaction delete" data-id="'+value+'"><i class="fas fa-trash"></i></a>';
                        }
                    }
                ],
                data : list
            });
            table.renderTable();
            
            $("#alertpopup .alert").append('<div class="buttons"><a class="addalert btn">'+get_apmviewer_msg('apm.addAlert')+'</a></div>');
            
            $("#alertpopup .alert").find("a.addalert").off("click");
            $("#alertpopup .alert").find("a.addalert").on("click", function(){
                APMViewer.loadAlertDetail();
            });
            
            $("#alertpopup .alert").find("table .rowaction.edit").off("click");
            $("#alertpopup .alert").find("table .rowaction.edit").on("click", function() {
                APMViewer.loadAlertDetail($(this).data("id"));
            });
            
            $("#alertpopup .alert").find("table .rowaction.delete").off("click");
            $("#alertpopup .alert").find("table .rowaction.delete").on("click", function() {
                var thisRowObj = $(this);
                var id = $(this).data("id");
                APMViewer.httpPost(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['removeAlert'], {
                    "version" : id
                },
                function(data){
                    $(thisRowObj).closest("tr").remove();
                });
            });
            
            if (callback !== undefined) {
                callback();
            }
        });
    },
    loadAlertDetail : function(id) {
        $("#alertpopup .alert").html('<div class="alertproperty"></div>');
        
        var propertyValues = {};
        
        var deferreds = [];
        var d = $.Deferred();
        deferreds.push(d);
        
        if (id === undefined) {
            d.resolve();
        } else {
            APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['config'] + "&version=" + id, function(data){
                propertyValues['metric'] = data.config.condition.metric;
                
                if (propertyValues['metric'] === "gauge:java.lang:type=Memory:HeapMemoryUsage.used" || propertyValues['metric'] === "gauge:java.lang:type=OperatingSystem:FreePhysicalMemorySize") {
                    propertyValues['threshold'] = data.config.condition.threshold / 1048576;
                } else {
                    propertyValues['threshold'] = data.config.condition.threshold;
                }
                
                propertyValues['timeperiod'] = data.config.condition.timePeriodSeconds / 60;
                
                if (data.config.condition.minTransactionCount !== undefined) {
                    propertyValues['minTransaction'] = data.config.condition.minTransactionCount;
                }
                
                if (data.config.condition.lowerBoundThreshold !== undefined && data.config.condition.lowerBoundThreshold) {
                    propertyValues['lowerThreshold'] = "true";
                }
                
                propertyValues['severity'] = data.config.severity;
                propertyValues['email'] = data.config.emailNotification.emailAddresses.join(',');
                
                d.resolve();
            });
        }
        
        $.when.apply($, deferreds).then(function() {
            var options = {
                propertiesDefinition : [{
                    title : (id === undefined)?get_apmviewer_msg('apm.addAlert'):get_apmviewer_msg('apm.editAlert'),
                    properties :[
                        {
                            label : get_apmviewer_msg('apm.metric'),
                            name  : 'metric',
                            type : 'selectbox',
                            options : [{
                                label : '',
                                value : ''
                            },
                            {
                                label : get_apmviewer_msg('apm.errorCount'),
                                value : 'error:count'
                            },
                            {
                                label : get_apmviewer_msg('apm.errorRate') + " (%)",
                                value : 'error:rate'
                            },
                            {
                                label : get_apmviewer_msg('apm.heapMemoryUsage') + " (MB)",
                                value : 'gauge:java.lang:type=Memory:HeapMemoryUsage.used'
                            },
                            {
                                label : get_apmviewer_msg('apm.freePhysicalMemory') + " (MB)",
                                value : 'gauge:java.lang:type=OperatingSystem:FreePhysicalMemorySize'
                            },
                            {
                                label : get_apmviewer_msg('apm.processCPULoad'),
                                value : 'gauge:java.lang:type=OperatingSystem:ProcessCpuLoad'
                            },
                            {
                                label : get_apmviewer_msg('apm.systemCPULoad'),
                                value : 'gauge:java.lang:type=OperatingSystem:SystemCpuLoad'
                            }],
                            description : get_apmviewer_msg('apm.help.metric'),
                            required : 'true'
                        },
                        {
                            label : get_apmviewer_msg('apm.threshold'),
                            name  : 'threshold',
                            required : 'true',
                            description : get_apmviewer_msg('apm.help.threshold'),
                            type : 'textfield'
                        },
                        {
                            label : get_apmviewer_msg('apm.lowerThreshold'),
                            name  : 'lowerThreshold',
                            description : get_apmviewer_msg('apm.help.lowerThreshold'),
                            type : 'checkbox',
                            options : [{
                                label : "",
                                value : 'true'
                            }]
                        },
                        {
                            label : get_apmviewer_msg('apm.timeperiod'),
                            name  : 'timeperiod',
                            required : 'true',
                            description : get_apmviewer_msg('apm.help.timeperiod'),
                            type : 'textfield'
                        },
                        {
                            label : get_apmviewer_msg('apm.minTransaction'),
                            name  : 'minTransaction',
                            required : 'true',
                            description : get_apmviewer_msg('apm.help.minTransaction'),
                            type : 'textfield',
                            control_field: 'metric',
                            control_value: 'error:rate',
                            control_use_regex: 'false'
                        },
                        {
                            label : get_apmviewer_msg('apm.severity'),
                            name  : 'severity',
                            type : 'selectbox',
                            options : [{
                                label : '',
                                value : ''
                            },
                            {
                                label : get_apmviewer_msg('apm.critical'),
                                value : 'critical'
                            },
                            {
                                label : get_apmviewer_msg('apm.high'),
                                value : 'high'
                            },
                            {
                                label : get_apmviewer_msg('apm.medium'),
                                value : 'medium'
                            },
                            {
                                label : get_apmviewer_msg('apm.low'),
                                value : 'low'
                            }],
                            required : 'true'
                        },
                        {
                            label : get_apmviewer_msg('apm.email'),
                            name  : 'email',
                            required : 'true',
                            description : get_apmviewer_msg('apm.help.email'),
                            type : 'textfield'
                        }
                    ]
                }],
                propertyValues : propertyValues,
                showCancelButton : true,
                saveButtonLabel : get_apmviewer_msg('apm.save'),
                cancelCallback: function() {
                    APMViewer.loadAlertList();
                },
                saveCallback: function(container, properties) {
                    var data = {
                        condition: {
                            conditionType: "metric",
                            metric: properties['metric'],
                            timePeriodSeconds: parseFloat(properties['timeperiod']) * 60
                        },
                        emailNotification: {
                            emailAddresses: properties['email'].split(',')
                        },
                        severity: properties['severity']
                    };
                    
                    if (properties['metric'] === "gauge:java.lang:type=Memory:HeapMemoryUsage.used" || properties['metric'] === "gauge:java.lang:type=OperatingSystem:FreePhysicalMemorySize") {
                        data.condition.threshold = parseFloat(properties['threshold']) * 1048576;
                    } else {
                        data.condition.threshold = parseInt(properties['threshold']);
                    }
                    
                    if (properties['minTransaction'] !== undefined && properties['minTransaction'] !==  "") {
                        data.condition.minTransactionCount = parseInt(properties['minTransaction']);
                    }

                    if (properties['lowerThreshold'] !== undefined && properties['lowerThreshold'] === "true") {
                        data.condition.lowerBoundThreshold = true;
                    }
                    
                    if (properties['metric'] === "error:count" || properties['metric'] === "error:rate") {
                        data.condition.transactionName = "";
                        data.condition.transactionType = "Web";
                    }
                    
                    var action = APMViewer.urls['addAlert'];
                    if (id !== undefined) {
                        data.version = id;
                        action = APMViewer.urls['updateAlert'];
                    }
                    
                    APMViewer.httpPost(APMViewer.contextPath + APMViewer.urls['base'] + action, data,function(data){
                        APMViewer.loadAlertList();
                    });
                }
            };
            $("#alertpopup .alert .alertproperty").propertyEditor(options);
        });  
    },
    smtpView : function() {
        if ($("#alertpopup .smtp").length === 0) {
            $("#alertpopup").find(".tab-contents").append('<div class="smtp"></div>');
        }
        $("#alertpopup .smtp").html('<div class="smtpproperty"></div>');
        
        var propertyValues = {};
        
        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['smtp'], function(data){
            var propertyValues = {
                "host": data.config.host,
                "port": data.config.port,
                "connectionSecurity": data.config.connectionSecurity,
                "username": data.config.username,
                "newPassword": "===SECURE PASSWORD ===",
                "fromEmailAddress": data.config.fromEmailAddress,
                "fromDisplayName": data.config.fromDisplayName,
                "version": data.config.version
            };
            
            var options = {
                propertiesDefinition : [{
                    title : get_apmviewer_msg('apm.configSmtp'),
                    properties :[
                        {
                            label : get_apmviewer_msg('apm.host'),
                            name  : 'host',
                            required : 'true',
                            type : 'textfield'
                        },
                        {
                            label : get_apmviewer_msg('apm.port'),
                            name  : 'port',
                            required : 'true',
                            type : 'textfield'
                        },
                        {
                            label : get_apmviewer_msg('apm.connectionSecurity'),
                            name  : 'connectionSecurity',
                            type : 'selectbox',
                            options : [{
                                label : '',
                                value : ''
                            },
                            {
                                label : get_apmviewer_msg('apm.ssl'),
                                value : 'ssl-tls'
                            },
                            {
                                label : get_apmviewer_msg('apm.tls'),
                                value : 'starttls'
                            }]
                        },
                        {
                            label : get_apmviewer_msg('apm.username'),
                            name  : 'username',
                            required : 'true',
                            type : 'textfield'
                        },
                        {
                            label : get_apmviewer_msg('apm.password'),
                            name  : 'newPassword',
                            required : 'true',
                            type : 'password'
                        },
                        {
                            label : get_apmviewer_msg('apm.fromEmailAddress'),
                            name  : 'fromEmailAddress',
                            required : 'true',
                            type : 'textfield'
                        },
                        {
                            label : get_apmviewer_msg('apm.fromDisplayName'),
                            name  : 'fromDisplayName',
                            value : get_apmviewer_msg('apm.fromDisplayName.default'),
                            type : 'textfield'
                        },
                        {
                            name  : 'version',
                            type : 'hidden'
                        }
                    ],
                    buttons : [{
                        name : 'testmail',    
                        label : get_apmviewer_msg('apm.sendTestMail'),
                        callback : "APMViewer.sendTestMail",
                        fields : ['fromEmailAddress', 'fromDisplayName', 'host', 'port', 'connectionSecurity', 'username', 'newPassword', 'version'],
                        addition_fields : [
                            {
                                name : 'testEmailRecipient',
                                label : get_apmviewer_msg('apm.testEmail'),
                                type : 'textfield',
                                required : 'True'
                            }
                        ]
                    }]
                }],
                propertyValues : propertyValues,
                showCancelButton : false,
                saveButtonLabel : get_apmviewer_msg('apm.save'),
                closeAfterSaved: false,
                saveCallback: function(container, properties) {
                    $(container).find(".page-button-save").parent().find(".msg").remove();
                    var newdata = data.config;
                    
                    newdata.host = properties["host"];
                    newdata.port = properties["port"];
                    newdata.connectionSecurity = properties["connectionSecurity"];
                    newdata.username = properties["username"];
                    newdata.fromEmailAddress = properties["fromEmailAddress"];
                    newdata.fromDisplayName = properties["fromDisplayName"];
                    
                    if (properties["newPassword"] !== "%%%%===SECURE PASSWORD ===%%%%") {
                        newdata.newPassword = properties["newPassword"].substring(4, properties["newPassword"].length - 4);
                        newdata.passwordExists = true;
                    }
                    
                    APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['adminGeneral'], function(data){
                        if (data.config.agentDisplayName !== APMViewer.title) {
                            data.config.agentDisplayName = APMViewer.title;
                            APMViewer.httpPost(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['adminGeneral'], data.config, function(data){
                                //ignore
                            });
                        }
                    });
                    
                    
                    APMViewer.httpPost(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['smtp'], newdata, function(data){
                        $(container).find(".page-button-save").before('<span class="msg">'+get_apmviewer_msg('apm.saved')+'</span>');
                        
                        setTimeout(function(){
                            $(container).find(".page-button-save").parent().find(".msg").remove();
                        }, 5000);
                    });
                }
            };
            $("#alertpopup .smtp .smtpproperty").propertyEditor(options);
        });
    },
    sendTestMail : function(properties) {
        if (properties["newPassword"] !== "%%%%===SECURE PASSWORD ===%%%%") {
            properties["newPassword"] = properties["newPassword"].substring(4, properties["newPassword"].length - 4);
            properties["passwordExists"] = true;
        } else {
            properties["newPassword"] = "";
            properties["passwordExists"] = true;
        }
        
        APMViewer.httpPost(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['sendTestEmail'], properties, function(data){
            if (data.error) {
                alert(get_apmviewer_msg("apm.testEmailFail") + " (" + data.message + ")");
            } else {
                alert(get_apmviewer_msg("apm.testEmailSuccess"));
            }
        });
    },
    loadAppsList : function(data) {
        APMViewer.apps['OVERALL'] = new APMNode().initBase("", get_apmviewer_msg('apm.overall'), []);
        APMViewer.apps['OVERALL'].isTransition = true;
        $.each(data.data, function(i, v){
            APMViewer.apps[v.id] = new APMNode().initBase(v.name, get_apmviewer_msg('apm.app') + " : " + v.name, ['^.+'+APMViewer.contextPath+'/web/userview/'+ v.id + '(/[^/]+)(.*)$', '^.+'+APMViewer.contextPath+'/web/embed/userview/'+ v.id + '(/[^/]+)(.*)$', '^.+'+APMViewer.contextPath+'/web(/ulogin)/'+ v.id + '(/.*)$']);
        });
        APMViewer.apps['PLUGINS'] = new APMNode().initBase("Plugins", get_apmviewer_msg('apm.pluginWebService'), ['^.+'+APMViewer.contextPath+'/web([\.a-zA-Z0-9]+/service)$']);
        APMViewer.apps['RESOURCES'] = new APMNode().initBase("Resources", get_apmviewer_msg('apm.resources'), ['^.+'+APMViewer.contextPath+'(/images/.+)$', '^.+'+APMViewer.contextPath+'(/css/.+)$', '^.+'+APMViewer.contextPath+'(/wro/.+)$', '^.+'+APMViewer.contextPath+'(/.+\.(?:js|css|jpg|ico|png|gif|eot|svg|ttf|woff|woff2|map))$'], undefined, false);
        APMViewer.apps['APM'] = new APMNode().initBase("Performance", get_apmviewer_msg('apm.performance'), ['^.+'+APMViewer.contextPath+'/web(/console/monitor/apm)$', '^.+'+APMViewer.contextPath+'/web/json/console/monitor/apm/retrieve(/[^/]+)$', '^.+'+APMViewer.contextPath+'/web/json/console/monitor/apm/retrieve(/[^/]+)(.*)$']);
        APMViewer.apps['WEBCONSOLE'] = new APMNode().initBase("Web Console", get_apmviewer_msg('apm.webConsole'), ['^.+'+APMViewer.contextPath+'(/)$', '^.+'+APMViewer.contextPath+'(/web)$', '^.+'+APMViewer.contextPath+'(/home.*)$', '^.+'+APMViewer.contextPath+'(/web/console/.*)$', '^.+'+APMViewer.contextPath+'(/web.*)$']);
        APMViewer.apps['OTHERS'] = new APMNode().initBase("Others", get_apmviewer_msg('apm.others'), ['^.+'+APMViewer.contextPath+'/(.*)$']);
    },
    reset : function() {
        $(".apmviewer").html("");
        
        for (var key in APMViewer.apps) {
            if (APMViewer.apps.hasOwnProperty(key)) {
                APMViewer.apps[key].reset();
            }
        }
    },
    refresh : function() {
        APMViewer.showLoading();
        var d = new Date();
        APMViewer.currenttime = d.getTime();
        APMViewer.reset();
        if (!APMViewer.isVirtualHostEnabled) {
            APMViewer.loadGaugesChart();
        }
        APMViewer.loadSummary();
    },
    loadGaugesChart : function() {
        $(".apmviewer").append('<div class="main-body-content-subheader"><span>'+get_apmviewer_msg('apm.jvmPerformance')+'</span></div><div class="gaugeschart chart-container"><div id="gchart" class="chart"></div></div>');
        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['gauges'] + APMViewer.getTimeRange(), function(data){
            for (var i = 0; i < data.dataSeries.length; i++) {
                data.dataSeries[i].type = "line";
                data.dataSeries[i].smooth = true;
                
                for (var j = 0; j < data.dataSeries[i].data.length; j++) {
                    if (data.dataSeries[i].data[j] !== null) {
                        if (i === 0 ) {
                            data.dataSeries[i].data[j][1] = data.dataSeries[i].data[j][1] / APMViewer.maxheap * 100;
                        } else if (i === 1 ) {
                            data.dataSeries[i].data[j][1] = data.dataSeries[i].data[j][1] / APMViewer.totalMemory * 100;
                        } else {
                            data.dataSeries[i].tunit = 100;
                            data.dataSeries[i].data[j][1] = data.dataSeries[i].data[j][1] * 100;
                        }
                    }
                }
            }
            data.dataSeries[0].tlabel = get_apmviewer_msg('apm.heapMemoryUsage');
            data.dataSeries[1].tlabel = get_apmviewer_msg('apm.freePhysicalMemory');
            data.dataSeries[2].tlabel = get_apmviewer_msg('apm.processCPULoad');
            data.dataSeries[3].tlabel = get_apmviewer_msg('apm.systemCPULoad');
            data.dataSeries[0].name = data.dataSeries[0].tlabel;
            data.dataSeries[1].name = data.dataSeries[1].tlabel;
            data.dataSeries[2].name = data.dataSeries[2].tlabel;
            data.dataSeries[3].name = data.dataSeries[3].tlabel;
            
            var myChart = echarts.init(document.getElementById('gchart'));
            var option = {
                title: {
                    text: ''
                },
                legend: {
                    data:[data.dataSeries[0].name, data.dataSeries[1].name, data.dataSeries[2].name, data.dataSeries[3].name]
                },
                dataZoom: [{
                    type: 'slider'
                }],
                grid: {
                    bottom: 90
                },
                tooltip: {
                    trigger: 'axis',
                    formatter: function (params) {
                        var todate = new Date(params[0].axisValue);
                        var fromdate = new Date(params[0].axisValue - data.dataPointIntervalMillis);
                        
                        var html = "<span>"+ moment(fromdate).format("HH:mm") + " - " + moment(todate).format("HH:mm") + "</span>";
                        html += "<table style=\"color:#fff;\">";
                        for (var i = 0; i < params.length; i ++) {
                            var value = params[i].data[1].toFixed(2) + "%";
                            html += "<tr><td>"+params[i].marker+" <strong style=\"color:#fff\">"+data.dataSeries[i].tlabel+"</strong></td><td style=\"text-align:right\">"+value+"</td></tr>";
                        }
                        html += "</table>";
                        return html;
                    },
                    axisPointer: {
                        animation: false
                    }
                },
                xAxis: {
                    type: 'time',
                    min: APMViewer.currenttime - APMViewer.getDuration(),
                    max: APMViewer.currenttime
                },
                yAxis: {
                    type: 'value',
                    min: 0,
                    max:100
                },
                series: data.dataSeries
            };
            myChart.setOption(option);
            $( window ).resize(function() {
                myChart.resize();
            });
        });
    },
    loadSummary : function() {
        var d = $.Deferred();
        APMViewer.deferreds.push(d);
        APMViewer.httpGet(APMViewer.contextPath + APMViewer.urls['base'] + APMViewer.urls['summary'] + APMViewer.getTimeRange(), function(data){
            //seperate transaction
            $.each(data, function(i, t){
                APMViewer.apps['OVERALL'].updateNode(t);
                
                var found = false;
                for (var key in APMViewer.apps) {
                    if (APMViewer.apps.hasOwnProperty(key)) {
                        var app = APMViewer.apps[key];
                        if (app.patterns !== undefined && app.patterns.length > 0) {
                            for (var j=0; j < app.patterns.length; j++) {
                                var found = t.transactionName.match(new RegExp(app.patterns[j]));
                                
                                if (found !== null) {
                                    app.addTransaction(found, t);
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (found) {
                            break;
                        }
                    }
                }
            }); 
            
            d.resolve();
        });
        
        APMViewer.loadSummaryComplete(APMViewer.deferreds);
    },
    loadSummaryComplete : function(deferreds) {
        var length = deferreds.length;
        $.when.apply($, deferreds).then(function() {
            if (length !== APMViewer.deferreds.length) {
                APMViewer.loadSummaryComplete(APMViewer.deferreds);
            } else {
                APMViewer.populateViewTable();
                APMViewer.deferreds = [];
                APMViewer.hideLoading();
            }
        });    
    },
    showLoading : function() {
        if ($(".apmviewer").find("#loading")) {
            var loading = $('<div id="loading"><i class="fas fa-spinner fa-spin fa-2x"></i> '+get_apmviewer_msg('apm.loading')+'</div>');
            $(".apmviewer").prepend(loading);
        }
    },
    hideLoading : function() {
        $(".apmviewer #loading").remove();
    },
    getTimeRange : function() {
        return "&from=" + (APMViewer.currenttime - APMViewer.getDuration()) + "&to=" + APMViewer.currenttime;
    },
    getDuration : function() {
        return $(".apmtool .durationSelector").val();
    },
    httpGet : function(url, callback) {
        var connCallback = {
            success: function(response) {
                var data = {};
                try {
                    data = eval("["+response+"]")[0];
                } catch(err) {}
                callback(data)
            }
        };
        // make request
        ConnectionManager.get(url, connCallback);
    },
    httpPost : function(url, data, callback) {
        var connCallback = {
            success: function(response) {
                var data = {};
                try {
                    data = eval("["+response+"]")[0];
                } catch(err) {}
                callback(data)
            }
        };
        
        var thisWindow = window;
        $.support.cors = true;
        $.ajax({
           type: 'POST',
           url: url,
           data: JSON.stringify(data),
           dataType : "text",
           contentType: "application/json",
           beforeSend: function (request) {
              if (ConnectionManager.tokenName !== undefined) { 
                  request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
              }
           },
           xhrFields: {
               withCredentials: true
           },
           success: function(data) {
               connCallback.success.call(thisWindow, data);
           },
           error: function(data) {
               try {
                   // do nothing for now
                   if (callback.error) {
                       callback.error.call(thisWindow, data);
                   }
               }
               catch (e) {}
           }
         });
    },
    populateViewTable : function() {
        if ($(".apmviewer .apmtableview").length === 0) {
            $(".apmviewer").append('<div class="main-body-content-subheader"><span>'+get_apmviewer_msg('apm.webRequestPerformance')+'</span></div><div class="apmtableview"></div>');
            APMViewer.table = new APMTable($(".apmviewer .apmtableview"), {
                columns : [
                    {
                        name : "displayName", 
                        label : get_apmviewer_msg('apm.name'), 
                        width : "50%", 
                        sortable : true,
                        isString : true
                    },
                    {
                        name : "transactionCount", 
                        label : get_apmviewer_msg('apm.transactionCount'), 
                        sortable : true
                    },
                    {
                        name : "average", 
                        label : get_apmviewer_msg('apm.averageTime'), 
                        sortable : true,
                        format : function(value) {
                            return APMViewer.numberFormat(value) +get_apmviewer_msg('apm.ms');
                        }
                    },
                    {
                        name : "percentage", 
                        label : get_apmviewer_msg('apm.percentOfTotalTime'), 
                        sortable : true,
                        format : function(value) {
                            return APMViewer.numberFormat(value) +'%';
                        }
                    },
                    {
                        name : "throughput", 
                        label : get_apmviewer_msg('apm.throughputPM'), 
                        sortable : true,
                        format : function(value) {
                            return value.toFixed(2);
                        }
                    },
                    {
                        name : "slowTrace", 
                        label : get_apmviewer_msg('apm.slowTrace'), 
                        sortable : true
                    },
                    {
                        name : "errors", 
                        label : get_apmviewer_msg('apm.errors'), 
                        sortable : true
                    }
                ],
                data : APMViewer.apps,
                fixedRow : 1,
                subgroup : "transactions",
                defaultSort : "average",
                defaultSortOrder : "desc",
                skipRow : function (row) {
                    return row.transactionCount === 0;
                },
                hasDetail : function (row) {
                    return row.isTransition;
                },
                showDetail : function (row, detailContainer) {
                    row.renderDetails(detailContainer);
                }
            });
            APMViewer.table.renderTable();
        } else {
            APMViewer.table.updateData(APMViewer.apps);
        }
    },
    numberFormat : function(value, decimal) {
        if (decimal === undefined) {
            decimal = 2;
        }
        return value.toFixed(decimal).replace(/\d(?=(\d{3})+(\.|$))/g, '$&,');
    }
};