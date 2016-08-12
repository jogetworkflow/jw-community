<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="title"><fmt:message key="adminBar.label.app"/>: ${appDefinition.name}</c:set>
<commons:header title="${title}" />

<script>
    function convert(process){
        process.id = process.id.replace(/#/g, ':');
    }

    function convertProcessId(jsonObject){
        if(jsonObject.data != undefined && jsonObject.data.length == undefined){
            convert(jsonObject.data);
        }else{
            for(var i in jsonObject.data){
                convert(jsonObject.data[i]);
            }
        }

        return jsonObject;
    }
</script>

<div id="nav">
    <div id="nav-title">
        <jsp:include page="appTitle.jsp" flush="true" />
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="appSubMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
            <li><button id="launchDesigner" onclick="launchDesigner()"><fmt:message key="pbuilder.label.designProcesses"/></button></li>
            <li><button onclick="uploadPackage()"><fmt:message key="console.process.config.label.updateProcess"/></button></li>
        </ul>
    </div>
    <div id="main-body">

        <c:if test="${empty processList[0]}">
            <fmt:message key="console.process.config.label.noProcess"/>
        </c:if>

        <ul id="main-body-list">
            <c:forEach items="${processList}" var="process">
            <li>
                <div class="list-thumbnail" id="${process.encodedId}"><a href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.idWithoutVersion}">
                        <div id="thumbnail">
                            <img src="${pageContext.request.contextPath}/images/v3/loading.gif">
                            <fmt:message key="console.process.config.label.xpdlThumbnailLoading"/>
                        </div></a>
                </div>
                <div class="list-details">
                    <div class="list-name"><a href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.idWithoutVersion}"><c:out value="${process.name}"/></a>
                    </div>
                    <div class="list-description">
<!--                        <ul id="main-body-sublist">
                            <li>7 activities</li>
                            <li>2 tools</li>
                            <li>3 participants</li>
                        </ul>-->
                    </div>
                </div>
                <div class="clear"></div>
            </li>
            </c:forEach>
        </ul>
    </div>

    <div style="display:none">
        <div id="updateInformation" >
            <div style="height: 100px; width: 500px; margin:0" class="dialog">
                <p>
                    <fmt:message key="console.process.config.label.update.message"/>
                </p>
                <div style="text-align:center">
                    <button onclick="window.location='${pageContext.request.contextPath}/web/console/app/${appId}//processes';return false;""><fmt:message key="general.method.label.ok"/></button>
                    <button id="closeInfo"><fmt:message key="general.method.label.cancel"/></button>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    <ui:popupdialog var="popupDialog" src="/"/>

    function launchDesigner(){
        $("#updateInformation").dialog({modal:true, height:150, width:550, resizable:false, show: 'slide',overlay: {opacity: 0.5, background: "black"},zIndex: 15001});
        $("#closeInfo").click(function(){$("#updateInformation").dialog("close")});
        window.open("${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/process/builder");
    }

    function uploadPackage(){
        popupDialog.src = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/package/upload";
        popupDialog.init();
    }
    
    Thumbnail = {
        retry: 0,
        load: function(el, callback) {
            var image = new Image();
            image.src = "${pageContext.request.contextPath}/web/console/images/xpdl/thumbnail/" + el.attr("id") + "?rnd=" + new Date().valueOf().toString();
            $(image).load(function(){
                $(el).children("a").append(image);
                $(image).each(function() {
                    var maxWidth = 170; // Max width for the image
                    var maxHeight = 100;    // Max height for the image
                    var ratio = 0;  // Used for aspect ratio
                    var width = $(this).width();    // Current image width
                    var height = $(this).height();  // Current image height

                    // Check if the current width is larger than the max
                    if(width > maxWidth){
                        ratio = maxWidth / width;   // get ratio for scaling image
                        $(this).css("width", maxWidth); // Set new width
                        $(this).css("height", height * ratio);  // Scale height based on ratio
                        height = height * ratio;    // Reset height to match scaled image
                        width = width * ratio;    // Reset width to match scaled image
                    }

                    // Check if current height is larger than max
                    if(height > maxHeight){
                        ratio = maxHeight / height; // get ratio for scaling image
                        $(this).css("height", maxHeight);   // Set new height
                        $(this).css("width", width * ratio);    // Scale width based on ratio
                        width = width * ratio;    // Reset width to match scaled image
                    }
                });
                $(el).find(" #thumbnail").hide();
                $(el).addClass("loaded");
                Thumbnail.retry = 0;
                callback();
            });
            $(image).error(function(){
                var processDefId = el.attr("id");
                Thumbnail.render(processDefId);
                if (Thumbnail.retry <= 3) {
                    setTimeout(function() { Thumbnail.load(el, callback);}, 5000);
                } 
            });
        },
        remove_generator : function() {
            $("#xpdl_images_generator").remove();
        },
        render: function(processDefId) {
            if ($("#xpdl_images_generator").length === 0) {
                Thumbnail.retry++;
                // create invisible iframe for canvas
                var iframe = document.createElement('iframe');
                var iwidth = 1024;
                var iheight = 0;
                $(iframe).attr("id", "xpdl_images_generator");
                $(iframe).attr("src", "${pageContext.request.contextPath}/web/console/app/${appDefinition.id}/process/screenshot/" + processDefId + "?callback=Thumbnail.remove_generator");
                $(iframe).css({
                    'visibility':'hidden'
                }).width(iwidth).height(iheight);
                $(document.body).append(iframe);
            }
        },
        init: function() {
            if ($(".list-thumbnail:not(.loaded)").length > 0) {
                Thumbnail.load($(".list-thumbnail:not(.loaded):eq(0)"), function() {
                    Thumbnail.init();
                });
            }
        }
    }
    Thumbnail.init();
    Template.init("#menu-apps", "#nav-app-processes");
    HelpGuide.key = "help.web.console.app.processes.list";
</script>

<commons:footer />
