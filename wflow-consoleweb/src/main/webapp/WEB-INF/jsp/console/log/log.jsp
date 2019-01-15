<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<style>
    #logviewer {background: #000; padding: 15px 0 200px; font-family: monospace; color: #fff;}
    #logviewer .line {padding: 2px 15px;}
    #logviewer .line:nth-child(even) {background:#131313;}
    #logviewer .line.error {color:red;}
    #logviewer .line.warn {color:yellow;}
    #logviewer .line.debug {color:lightskyblue;}
    .followbuttondiv {position: fixed; right: 30px; margin-top: 8px;}
    .linenumber {display: inline-block; width: 45px; text-align: left; color: lightslategrey; font-size: 10px; vertical-align: top; padding-top: 2px;}
    .text {display: inline-block; width: calc(100% - 55px);}
</style>    
        
<div id="main">
    <div id="main-body">
        <div id="main-body-content">
            <div class="followbuttondiv" style="display:none;"><a class="followbtn btn"><fmt:message key="console.log.unfollow"/></a></div>
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
        
        $(".followbtn").on("click", function(){
            if ($(this).hasClass("unfollow")) {
                $(this).removeClass("unfollow");
                $(this).text("<fmt:message key="console.log.follow"/>");
            } else {
                $(this).addClass("unfollow");
                $(this).text("<fmt:message key="console.log.unfollow"/>");
            }
            
            
        });
        
        var scrollSmoothToBottom = function() {
            if ($(".followbtn").hasClass("unfollow")) {
                var height = $("#logviewer").outerHeight(true);
                if (height > navHeight) {
                    $('html, body').stop().animate({scrollTop:height - navHeight}, "200");
                }
            }
        };
        
        setTimeout(function(){
            $(".followbtn").addClass("unfollow");
            $(".followbuttondiv").show();
            scrollSmoothToBottom();
        }, 1000);
        
        var i = 1;
        var websocket = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "${pageContext.request.contextPath}/applog/${appId}");
        websocket.onmessage = function(event) {
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
                $("#logviewer #logs").append('<div class="line '+status+'"><div class="linenumber">'+(i++)+'</div><div class="text">'+ text.replace(/\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;') + '</div></div>');
                scrollSmoothToBottom();
            }
        };
    });
    
    Template.init("#menu-apps", "#nav-app-logs");
</script>  