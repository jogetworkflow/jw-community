<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<style>
    #logviewer {background: #000; padding: 15px 0 200px; font-family: monospace; color: #fff; max-height: 480px; overflow-y: auto;}
    #logviewer .line {padding: 2px 15px;}
    #logviewer .line:nth-child(even) {background:#131313;}
    #logviewer .line.error {color:red;}
    #logviewer .line.warn {color:yellow;}
    #logviewer .line.debug {color:lightskyblue;}
    .followbuttondiv {position: fixed; right: 80px; margin-top: 10px;}
    .rtl .followbuttondiv {left: 80px; right: unset;}
    .linenumber {display: inline-block; width: 45px; text-align: left; color: lightslategrey; font-size: 10px; vertical-align: top; padding-top: 2px;}
    .text {display: inline-block; width: calc(100% - 55px);}
</style>    
        
<div id="main">
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
            <div class="followbuttondiv" style="display:none;"><a class="downloadbtn btn"><fmt:message key="general.method.label.download"/></a> <a class="followbtn btn"><fmt:message key="console.log.unfollow"/></a></div>
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
});
</script>  