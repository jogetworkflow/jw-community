<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<style>
    /* Light theme (default) */
    #logviewer {background: #f8f9fa; padding: 15px 0 200px; font-family: monospace; color: #212529; max-height: 480px; overflow-y: auto;}
    #logviewer .line {padding: 2px 15px;}
    #logviewer .line:nth-child(even) {background: #ffffff;}
    #logviewer .line.error {color: #dc3545;}
    #logviewer .line.warn {color: #fd7e14;}
    #logviewer .line.info {color: #0d6efd;}
    #logviewer .line.debug {color: #6c757d;}
    .followbuttondiv {position: fixed; right: 80px; margin-top: 10px;}
    .rtl .followbuttondiv {left: 80px; right: unset;}
    .linenumber {display: inline-block; width: 45px; text-align: left; color: #6c757d; font-size: 10px; vertical-align: top; padding-top: 2px;}
    .text {display: inline-block; width: calc(100% - 55px);}
    body.no-header.builder-popup div#main-body {margin-top: 0;}

    /* Dark theme */
    body[system-theme="dark"] #logviewer {background: #212529; color: #e9ecef;}
    body[system-theme="dark"] #logviewer .line:nth-child(even) {background: #2c3034;}
    body[system-theme="dark"] #logviewer .line.error {color: #ff6b6b;}
    body[system-theme="dark"] #logviewer .line.warn {color: #ffc107;}
    body[system-theme="dark"] #logviewer .line.info {color: #0dcaf0;}
    body[system-theme="dark"] #logviewer .line.debug {color: #adb5bd;}
    body[system-theme="dark"] .linenumber {color: #adb5bd;}

    .tree-container {
        max-width: 1200px;
        margin: 0 auto;
        border-radius: 8px;
        padding: 20px;
    }

    .tree-item {
        margin: 2px 0;
        padding-left: 10px;
    }

    .tree-item:hover {
        background: rgba(0, 0, 0, 0.05);
        border-radius: 4px;
    }

    .tree-node {
        position: relative;
        cursor: pointer;
        padding: 8px 0;
        line-height: 1.4;
    }

    .tree-node.no-children::before {
        position: absolute;
        left: -18px;
        color: #6c757d;
        font-size: 16px;
    }

    .line-number {
        color: #0d6efd;
        font-weight: bold;
        margin-right: 10px;
        min-width: 60px;
        display: inline-block;
    }

    .log-text {
        color: #212529;
        word-wrap: break-word;
    }

    .log-text.error {
        color: #dc3545;
    }

    .log-text.warn {
        color: #fd7e14;
    }

    .log-text.info {
        color: #0d6efd;
    }

    .log-text.debug {
        color: #6c757d;
    }

    .controls {
        margin-bottom: 20px;
        padding: 15px;
        background: #f8f9fa;
        border-radius: 6px;
        border: 1px solid #dee2e6;
    }

    .btn {
        background: #0d6efd;
        color: white;
        border: none;
        padding: 8px 16px;
        margin-right: 10px;
        border-radius: 4px;
        cursor: pointer;
        font-size: 12px;
        transition: background 0.2s;
    }

    .btn:hover {
        background: #0b5ed7;
    }

    .stats {
        color: #6c757d;
        font-size: 12px;
    }

    .search-filters {
        margin-bottom: 15px;
        padding: 15px;
        background: #f8f9fa;
        border-radius: 6px;
        border: 1px solid #dee2e6;
    }

    .search-filter-controls {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        align-items: center;
        margin-bottom: 10px;
    }

    .search-filter-group {
        display: flex;
        flex-direction: column;
        min-width: 150px;
        flex: 1;
    }

    .search-filter-group label {
        font-weight: bold;
        color: #212529;
        margin-bottom: 4px;
        font-size: 12px;
    }

    .search-filter-group select,
    .search-filter-group input {
        padding: 6px 8px;
        border: 1px solid #ced4da;
        border-radius: 4px;
        background: #ffffff;
        color: #212529;
        font-size: 12px;
    }

    .search-filter-buttons {
        display: flex;
        gap: 8px;
        align-items: flex-end;
        padding-top: 16px;
    }

    .search-filter-buttons .btn {
        margin: 0;
        white-space: nowrap;
        font-size: 12px;
        padding: 6px 12px;
    }

    .children {
        margin-left: 20px;
        margin-top: 8px;
    }

    .child-item {
        margin: 4px 0;
        padding: 6px 0;
        border-left: 2px solid #ced4da;
        padding-left: 10px;
    }

    .child-item:hover {
        background: rgba(0, 0, 0, 0.03);
        border-radius: 4px;
    }

    /* Dark theme overrides */
    body[system-theme="dark"] .tree-item:hover {
        background: rgba(255, 255, 255, 0.05);
    }

    body[system-theme="dark"] .tree-node.no-children::before {
        color: #adb5bd;
    }

    body[system-theme="dark"] .line-number {
        color: #6a9bd1;
    }

    body[system-theme="dark"] .log-text {
        color: #e9ecef;
    }

    body[system-theme="dark"] .log-text.error {
        color: #ff6b6b;
    }

    body[system-theme="dark"] .log-text.warn {
        color: #ffc107;
    }

    body[system-theme="dark"] .log-text.info {
        color: #0dcaf0;
    }

    body[system-theme="dark"] .log-text.debug {
        color: #adb5bd;
    }

    body[system-theme="dark"] .controls {
        background: #333;
        border: 1px solid #555;
    }

    body[system-theme="dark"] .btn {
        background: #4a90e2;
    }

    body[system-theme="dark"] .btn:hover {
        background: #357abd;
    }

    body[system-theme="dark"] .stats {
        color: #888;
    }

    body[system-theme="dark"] .search-filters {
        background: #333;
        border: 1px solid #555;
    }

    body[system-theme="dark"] .search-filter-group label {
        color: #e0e0e0;
    }

    body[system-theme="dark"] .search-filter-group select,
    body[system-theme="dark"] .search-filter-group input {
        border: 1px solid #555;
        background: #2d2d2d;
        color: #e0e0e0;
    }

    body[system-theme="dark"] .child-item {
        border-left: 2px solid #666;
    }

    body[system-theme="dark"] .child-item:hover {
        background: rgba(255, 255, 255, 0.03);
    }
</style>    
        
<div id="main">
    <div id="main-title"><fmt:message key="console.log.mtitle"/></div>
    <div id="main-action"></div>
    <c:if test="${supportMultipleNode}">        
        <div class="form-row">
            <label for="nodes">Cluster Nodes</label>
            <span class="form-input">
                <select id="nodes" name="nodes">
                    <c:forEach items="${nodes}" var="t">
                        <option value="${t.key}" ${selected}>${t.value}</option>
                    </c:forEach>
                </select>
            </span>
        </div>
    </c:if>
    <div id="main-body">
        <div id="main-body-content">
            <div class="followbuttondiv" style="display:none;"><a class="searchbtn btn console-primary"><i class="fas fa-magnifying-glass"></i></a> <a class="downloadbtn btn console-primary"><fmt:message key="general.method.label.download"/></a> <a class="followbtn btn"><fmt:message key="console.log.unfollow"/></a></div>
            <div id="logviewer">
                <div id="logs">
                </div> 
            </div>
        </div>
    </div>
</div>
            
<script>
$(document).ready(function() {
    var websocket;
    var status = "";
    var navHeight = $("#main-body-content").outerHeight(true);
    var textFile = null;
    var textChange = false;

    $(".followbtn").on("click", function(){
        if ($(this).hasClass("unfollow")) {
            $(this).removeClass("unfollow");
            $(this).text('<ui:msgEscJS key="console.log.follow"/>');
        } else {
            $(this).addClass("unfollow");
            $(this).text('<ui:msgEscJS key="console.log.unfollow"/>');
        }
    });

    $(".downloadbtn").on("click", function(){
        var text = "";
        $("#logviewer #logs .text").each(function(){
            text += $(this).text() + "\n";
        });
        var data = new Blob([text], {type: 'text/plain'});

        if (textFile !== null) {
            window.URL.revokeObjectURL(textFile);
            $(".hiddenLink").remove();
        }

        textFile = window.URL.createObjectURL(data);
        textChange = false;

        var hiddenLink = $('<a class="hiddenLink" style="display:none" target="_blank" download="log_${appId}.txt">text</a>');
        $(hiddenLink).attr("href", textFile);
        $(".downloadbtn").after(hiddenLink);
        $(hiddenLink)[0].click();
    });

    $(".searchbtn").on("click", function(){
        openSearchPanel();
    });

    var scrollSmoothToBottom = function() {
        if ($(".followbtn").hasClass("unfollow")) {
            var height = $("#logs").height();
            if (height > navHeight) {
                $('#logviewer').stop().animate({scrollTop:height}, 50);
            }
        }
    };

    setTimeout(function(){
        $(".followbtn").addClass("unfollow");
        $(".followbuttondiv").show();
        scrollSmoothToBottom();
    }, 1000);

    if ($('#nodes').length) {
        $("#nodes").val('${currentNode}');
        var node = $("#nodes").val();
        initLog(node);
    } else {
        initLog('${currentNode}');
    }

    $("#nodes").on('change', function() {
        websocket.close();
        var selectedNode = $("#nodes").val();
        $(".line").remove();
        initLog(selectedNode);
    });

    function initLog(node) {
        if (node !== undefined && node !== "") {
            node = "?node=" + encodeURIComponent(node);
        } else {
            node = "";
        }
        var i = 1;
        websocket = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "${pageContext.request.contextPath}/web/applog/${appId}" + node);
        let messages = [];
        let timer;
        websocket.onmessage = function(event) {
            textChange = true;
            var text = event.data;
            if (text.startsWith("INFO")) {
                status = "info";
            } else if (text.startsWith("ERROR")) {
                status = "error";
            } else if (text.startsWith("WARN")) {
                status = "warn";
            } else if (text.startsWith("DEBUG")) {
                status = "debug";
            }
            
            if(text.trim() !== ""){
                let line = '<div class="line '+status+'"><div class="linenumber">'+(i++)+'</div><div class="text">'+ UI.escapeHTML(text).replace(/\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;') + '</div></div>';
                messages.push(line);
                clearTimeout(timer);
                timer = setTimeout(function() {
                    let logs = $("#logviewer #logs");
                    logs[0].innerHTML = logs[0].innerHTML + messages.join("\n");
                    messages = [];
                    scrollSmoothToBottom();
                }, 1000);
            }
        };
    };

    //Change button color
    $("a.followbtn").addClass('console-secondary');

    $("div#main").css({visibility: 'visible'});

    function openSearchPanel() {
        var searchContent = `
            <div class="tree-container">
                <div class="search-filters">
                    <div class="search-filter-controls">
                        <div class="search-filter-group">
                            <label for="logLevelSelect">Log Level:</label>
                            <select id="logLevelSelect">
                                <option value="error">ERROR</option>
                                <option value="warn">WARN</option>
                                <option value="info">INFO</option>
                                <option value="debug">DEBUG</option>
                            </select>
                        </div>
                        <div class="search-filter-group">
                            <label for="searchInput">Search Text:</label>
                            <input type="text" id="searchInput" placeholder="Enter search term...">
                        </div>
                        <div class="search-filter-buttons">
                            <button id="applyFilter" class="btn">Apply Filter</button>
                            <button id="clearFilter" class="btn">Clear Filter</button>
                        </div>
                    </div>
                </div>
                <div class="controls">
                    <div class="stats" id="stats"></div>
                </div>
                <div id="tree-root"></div>
            </div>
        `;

        var searchPanel = new SlideOutPanel(null, '', {
            content: searchContent
        });

        searchPanel.init();

        setTimeout(function() {
            setupSearchFunctionality();
        }, 500);
    }

    function setupSearchFunctionality() {
        var currentFilter = {
            logLevel: 'error',
            searchText: ''
        };

        function performSearch() {
            var logLevel = $('#logLevelSelect').val();
            var searchText = $('#searchInput').val().toLowerCase();
            
            currentFilter.logLevel = logLevel;
            currentFilter.searchText = searchText;

            var overview = generateResult(logLevel, searchText);
            renderTree(overview);
        }

        function generateResult(logLevel, searchText) {
            var overview = {};
            var prevErrorLine = '';
            var selector = '.line.' + logLevel;
            
            $(selector).each(function(i, elm) {
                var line = $(elm).find('.linenumber').text().trim();
                var content = $(elm).find('.text').text().trim();
                
                // Debug: Log the content being processed
                console.log('Processing line:', line, 'Content:', content);
                
                // Check if this is a main error line (with timestamp pattern)
                if (content.match(/ERROR[\s]+[0-9]{2}\s[a-zA-Z]{3}\s[0-9]{4}/) || 
                    content.match(/WARN[\s]+[0-9]{2}\s[a-zA-Z]{3}\s[0-9]{4}/) ||
                    content.match(/INFO[\s]+[0-9]{2}\s[a-zA-Z]{3}\s[0-9]{4}/) ||
                    content.match(/DEBUG[\s]+[0-9]{2}\s[a-zA-Z]{3}\s[0-9]{4}/)) {
                    
                    // Always create the main entry, will filter later if needed
                    overview[line] = {
                        line: line,
                        text: content,
                        children: []
                    }
                    prevErrorLine = line;
                    console.log('Added main entry:', line, content);
                } else if (content.match(/Caused\sby:\s/)) {
                    // Add "Caused by:" lines as children
                    if (prevErrorLine !== '') {
                        var errorObj = overview[prevErrorLine];
                        if (errorObj !== undefined && errorObj !== null) {
                            errorObj.children.push({
                                line: line,
                                text: content
                            });
                            console.log('Added child:', line, content);
                        }
                    }
                }
            });

            // Apply search filter after collecting all data
            if (searchText !== '') {
                var filteredOverview = {};
                
                Object.keys(overview).forEach(function(lineNumber) {
                    var entry = overview[lineNumber];
                    var mainEntryMatches = entry.text.toLowerCase().includes(searchText);
                    var childMatches = false;
                    
                    // Check if any child matches the search
                    entry.children.forEach(function(child) {
                        if (child.text.toLowerCase().includes(searchText)) {
                            childMatches = true;
                        }
                    });
                    
                    // Include entry if main entry or any child matches
                    if (mainEntryMatches || childMatches) {
                        filteredOverview[lineNumber] = entry;
                    }
                });
                
                console.log('Filtered overview:', filteredOverview);
                return filteredOverview;
            }

            console.log('Final overview:', overview);
            return overview;
        }

        function getLogLevel(text) {
            if (text.includes('ERROR')) return 'error';
            if (text.includes('WARN')) return 'warn';
            if (text.includes('INFO')) return 'info';
            if (text.includes('DEBUG')) return 'debug';
            if (text.includes('Caused by:')) return 'caused-by';
            return '';
        }

        function createTreeNode(item, key) {
            var hasChildren = item.children && item.children.length > 0;
            var logLevel = getLogLevel(item.text);

            // Capture values explicitly to prevent any timing issues
            var lineNumber = item.line || '';
            var textContent = item.text || '';
            
            
            var nodeDiv = $('<div class="tree-item"></div>');
            nodeDiv.attr('data-line-number', lineNumber);

            var nodeContent = $('<div class="tree-node"></div>');
            nodeContent.addClass(hasChildren ? 'has-children' : 'no-children');

            // Use text() method instead of html() to avoid any potential escaping issues
            var lineSpan = $('<span class="line-number"></span>').text('Line ' + lineNumber + ':');
            var textSpan = $('<span class="log-text ' + logLevel + '"></span>').text(textContent);
            
            nodeContent.append(lineSpan);
            nodeContent.append(textSpan);
            
            nodeDiv.append(nodeContent);

            if (hasChildren) {
                var childrenDiv = $('<div class="children"></div>');

                item.children.forEach(function(child) {
                    // Capture child values explicitly
                    var childLineNumber = child.line || '';
                    var childTextContent = child.text || '';
                    var childLogLevel = getLogLevel(childTextContent);
                    
                    var childDiv = $('<div class="child-item"></div>');
                    childDiv.attr('data-line-number', childLineNumber);
                    
                    var childLineSpan = $('<span class="line-number"></span>').text('Line ' + childLineNumber + ':');
                    var childTextSpan = $('<span class="log-text ' + childLogLevel + '"></span>').text(childTextContent);
                    
                    childDiv.append(childLineSpan);
                    childDiv.append(childTextSpan);
                    
                    childrenDiv.append(childDiv);
                });

                nodeDiv.append(childrenDiv);
            }

            return nodeDiv;
        }

        function renderTree(data) {
            var root = $('#tree-root');
            root.empty();

            var sortedKeys = Object.keys(data).sort(function(a, b) {
                return parseInt(a) - parseInt(b);
            });

            sortedKeys.forEach(function(key) {
                var treeNode = createTreeNode(data[key], key);
                root.append(treeNode);
            });

            updateStats(data);
        }

        function updateStats(data) {
            var totalEntries = Object.keys(data).length;
            $('#stats').text('Total entries: ' + totalEntries);
        }

        // Add click handler for navigation
        $('#tree-root').on('click', '[data-line-number]', function(e) {
            e.stopPropagation();
            var lineNumber = $(this).data('line-number');
            if (lineNumber !== undefined && lineNumber !== null) {
                var targetLine = $('.line .linenumber:contains("' + lineNumber + '")').parent();
                if (targetLine.length > 0) {
                    $('#logviewer').animate({
                        scrollTop: targetLine.offset().top - $('#logviewer').offset().top + $('#logviewer').scrollTop()
                    }, 500);
                    
                    $('.line').css('background', '');
                    targetLine.css('background', '#fff3cd');
                    setTimeout(function() {
                        targetLine.css('background', '');
                    }, 2000);
                }
            }
        });

        $('#applyFilter').on('click', performSearch);
        $('#clearFilter').on('click', function() {
            $('#logLevelSelect').val('error');
            $('#searchInput').val('');
            performSearch();
        });

        $('#searchInput').on('keypress', function(e) {
            if (e.which === 13) {
                performSearch();
            }
        });

        performSearch();
    }
});
</script>  