<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<style>
    #logviewer {background: #000; padding: 15px 0 200px; font-family: monospace; color: #fff;}
    #logviewer .line {padding: 2px 15px;}
    #logviewer .line:nth-child(even) {background:#131313;}
    #logviewer .line.error {color:red;}
    #logviewer .line.warn {color:yellow;}
    #logviewer .line.debug {color:lightskyblue;}
    .followbuttondiv {position: fixed; right: 60px; margin-top: 8px;}
    .linenumber {display: inline-block; width: 45px; text-align: left; color: lightslategrey; font-size: 10px; vertical-align: top; padding-top: 2px;}
    .text {display: inline-block; width: calc(100% - 55px);}
</style>    
        
<div id="main">
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
        var status = "";
        var navHeight = $("#nav").outerHeight(true);
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
                var height = $("#logviewer").outerHeight(true);
                if (height > navHeight) {
                    $('html, body').stop().animate({scrollTop:height - navHeight}, 50);
                }
            }
        };
        
        setTimeout(function(){
            $(".followbtn").addClass("unfollow");
            $(".followbuttondiv").show();
            scrollSmoothToBottom();
        }, 1000);
        
        var i = 1;
        var websocket = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "${pageContext.request.contextPath}/web/applog/${appId}");
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
    });
    
    Template.init("#menu-apps", "#nav-app-logs");
</script>  