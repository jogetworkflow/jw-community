<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header id="desktop" />

    <link rel="stylesheet" href="${pageContext.request.contextPath}/home/style.css"/>
    <script src="${pageContext.request.contextPath}/mobile/jqm/jquery.cookie.js"></script>
    <script src="${pageContext.request.contextPath}/mobile/mobile.js"></script>

    <span id="main-action-help" style="display:none"></span>
    
    <div id="title">
        <div id="title-label"><fmt:message key="adminBar.label.appCenter"/></div>
        <div id="search">
        </div>
        <div id="categories">
            <a href="#" onclick="return loadPublishedApps()" id="category-published-apps" class="category"><fmt:message key="appCenter.label.publishedApps"/></a>
            <a href="#" onclick="return loadMarketplaceApps()" id="category-marketplace-apps" class="category"><fmt:message key="appCenter.label.marketplace"/></a>
        </div>
    </div>
    <div class="clear"></div>
    <div id="published-apps" class="published-apps"></div>
    <div id="marketplace-apps" class="published-apps"></div>
    <div class="clear"></div>

    <c:set var="isAdmin" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>
    <c:if test="${!isAdmin}">
        <style>
            #getting-started {
                height: 0px;
                width: 0px;
                overflow: hidden;
                z-index: -100;
                position: absolute;
                margin-top: 100%;
            }
        </style>
        <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />          
    </c:if>
    <c:if test="${isAdmin}">
        <div id="title" class="administration">
            <fmt:message key="appCenter.label.administration"/>
        </div>
        <div id="content" class="administration">
            <div class="column border">
                <c:set var="helpWebConsole"><fmt:message key="appCenter.help.webConsole"/></c:set>
                <c:choose>
                <c:when test="${!empty helpWebConsole && helpWebConsole != '???appCenter.help.webConsole???'}"><fmt:message key="appCenter.help.webConsole"/></c:when>
                <c:otherwise>
                <a target="_self" href="${pageContext.request.contextPath}/web/console/home">
                    <h3>Access the v3 Web Console</h3>
                    <img src="${pageContext.request.contextPath}/home/webconsole.png" width="150" border="0" />
                    <br />
                    <h4>For Administrators and App Designers to:</h4>
                    <ul>
                        <li>Setup Users</li>
                        <li>Design Apps</li>
                        <li>Run Apps</li>
                        <li>Monitor Apps</li>
                    </ul>
                </a>
                </c:otherwise>
                </c:choose>
            </div>
            <div class="column border">
                <c:set var="helpSupport"><fmt:message key="appCenter.help.helpSupport"/></c:set>
                <c:choose>
                <c:when test="${!empty helpSupport && helpSupport != '???appCenter.help.helpSupport???'}"><fmt:message key="appCenter.help.helpSupport"/></c:when>
                <c:otherwise>
                    <h3>Get Help and Support</h3>
                    <a target="_blank" href="http://www.joget.com/services?src=jwcv4"><img src="${pageContext.request.contextPath}/home/website.png" width="150" border="0" /></a>
                    <br />
                    <h4>Ways to get Help and Support:</h4>
                    <ul>
                        <li><a target="_blank" href="http://www.joget.com/services/support/?src=jwcv4">Enterprise Support</a></li>
                        <li><a target="_blank" href="http://community.joget.org/?src=jwcv4">Enterprise Knowledge Base</a></li>
                        <li><a target="_blank" href="http://www.joget.com/services/training/?src=jwcv4">Training and Consultancy Services</a></li>
                    </ul>
                </c:otherwise>
                </c:choose>
            </div>
            <div class="column-wide">
                <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />
            </div>
            <div class="column-narrow">
                <c:set var="helpResources"><fmt:message key="appCenter.help.helpResources"/></c:set>
                <c:choose>
                <c:when test="${!empty helpResources && helpResources != '???appCenter.help.helpResources???'}"><fmt:message key="appCenter.help.helpResources"/></c:when>
                <c:otherwise>
                <a target="_blank" href="http://www.joget.org/videos/tutorial/?src=jwcv4">
                    <h4>Learn with the following resources:</h4>
                    <ul>
                        <li>Tutorial Videos</li>
                        <li>Knowledge Base</li>
                    </ul>
                </a>
                </c:otherwise>
                </c:choose>
            </div>
        </div>
    </c:if>

    <script>    
        <c:set var="isAnonymous" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
        <c:if test="${!isAnonymous}">
            HelpGuide.key = "help.web.desktop.user";
        </c:if>
        <c:if test="${isAdmin}">
            HelpGuide.key = "help.web.desktop.admin";
        </c:if>
    </script>
    
<jsp:include page="/WEB-INF/jsp/console/apps/adminBar.jsp" flush="true">
    <jsp:param name="desktop" value="true"/>
    <jsp:param name="appControls" value="true"/>
</jsp:include>

<commons:footer/>

<script>
    var mobileLinkTitle = "<fmt:message key="appCenter.label.mobileEdition"/>";
    var mobileLink = "<a href='${pageContext.request.contextPath}/web/mobile' onclick='return Mobile.viewMobileSite(\"${pageContext.request.contextPath}/home/\", \"${pageContext.request.contextPath}/web/mobile\")' title='User Agent: " + navigator.userAgent + "'>" + mobileLinkTitle + "</a>";
    $("#header #account").prepend(mobileLink + " | ");
    var url = "${pageContext.request.contextPath}/mobile";
    Mobile.directToMobileSite(url);

    var loadApps = function(container, baseUrl, contextPath) {
        container = container || "#published-apps";
        baseUrl = baseUrl || "";
        contextPath = contextPath || "${pageContext.request.contextPath}";

        // show loading icon
        $(container).empty();
        var loading = $('<div id="apps-loading"><i class="icon-spinner icon-spin icon-2x"></i> <fmt:message key="appCenter.label.loadingApps"/></div>');
        $(container).append(loading);

        // load JSON
        $.ajax({ 
            url : baseUrl + contextPath + "/web/json/apps/published/userviews",
            dataType:'jsonp',
            success:function(data) {
                var content = "";
                var apps = data.apps;
                for( var i=0; i<apps.length; i++) {
                    // add app
                    var app = apps[i];
                    // add userviews
                    var userviews = app.userviews;
                    for( var j=0; j<userviews.length; j++){
                        var uv = userviews[j];
                        content +=  '<div class="column">\
                                        <div class="appdiv">\
                                        <a class="screenshot" target="_blank" href="' + baseUrl + uv.url + '">\
                                            <div class="screenshot_label">' + UI.escapeHTML(uv.name) + '</div>\
                                            <img src="' + baseUrl + contextPath + '/web/userview/screenshot/' + app.id + '/' + uv.id + '" width="150" border="0" />\
                                        </a><h3>' + UI.escapeHTML(app.name) + '</h3></div>\
                                    </div>';
                    }
                }

                // show apps, hide loading icon
                $(loading).remove();
                $(container).append($(content));
            }
        });
    }            
    jQuery.expr[':'].Contains = function(a,i,m){ 
        return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase())>=0; 
    };  
    var searchFilter = function(header, list) { 
        var timer;
        var form = $("<form>").attr({"class":"filterform","action":"#","onsubmit":"return false"}), 
        input = $("<input>").attr({"class":"filterinput","type":"text"}); 
        $(form).append(input).append($("<span class='filterlabel'><i class='icon-search'></i></span>")).appendTo(header);  
        $(form).submit(function() { return false });
        $(input).change(function () { 
            var filter = $(this).val(); 
            if(filter) { 
                $(list).find("a:not(:Contains(" + filter + ")),h3:not(:Contains(" + filter + "))").parent().parent().fadeOut(); 
                $(list).find("a:Contains(" + filter + "),h3:Contains(" + filter + ")").parent().parent().show(); 
            } else { 
                $(list).find("div").show(); 
            } 
            return false; 
        }).keyup(function () {
            if (timer) clearTimeout(timer);
            var $this = $(this);
            timer = setTimeout(function() {
                $this.change(); 
            }, 50);
        }); 
        $(input).focus();
    }  
    var loadPublishedApps = function() {
        $(".published-apps").empty();
        $("#categories a").removeClass("category-selected");
        $("#category-published-apps").addClass("category-selected");
        $("#marketplace-apps").hide();
        $("#published-apps").show();
        $("#search form").remove();
        searchFilter($("#search"), $("#published-apps")); 
        loadApps("#published-apps");
    }
    var loadMarketplaceApps = function() {
        $(".published-apps").empty();
        $("#categories a").removeClass("category-selected");
        $("#category-marketplace-apps").addClass("category-selected");
        $("#marketplace-apps").show();
        $("#published-apps").hide();
        $("#search form").remove();
        searchFilter($("#search"), $("#marketplace-apps")); 
        loadApps("#marketplace-apps", "http://marketplace.cloud.joget.com", "/jw4");
    }
    $(function () { 
        loadPublishedApps();
    }); 
</script>        
