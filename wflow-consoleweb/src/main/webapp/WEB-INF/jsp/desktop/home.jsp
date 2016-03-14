<%@page import="org.joget.apps.app.service.MobileUtil"%>
<%@page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.directory.model.service.DirectoryUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<c:set var="isAdmin" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>
<c:set var="isAnonymous" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
<c:set var="marketplaceUrl"><fmt:message key="appCenter.link.marketplace.url"/></c:set>
<c:set var="marketplaceTarget"><fmt:message key="appCenter.link.marketplace.target"/></c:set>

<commons:header id="appcenter" />

<script src="${pageContext.request.contextPath}/mobile/jqm/jquery.cookie.js"></script>
<script src="${pageContext.request.contextPath}/mobile/mobile.js"></script>

        <div id="main-container">
            <div id="main-content">
                <h1><i class="icon-desktop"></i> <fmt:message key="adminBar.label.appCenter"/></h1>
                <div id="search"></div>
                <c:if test="${isAdmin && !empty marketplaceUrl && marketplaceUrl != '???appCenter.link.marketplace.url???'}">
                    <div id="categories" class="menu-link-admin">
                        <a href="#" onclick="return loadPublishedApps()" id="category-published-apps" class="category"><fmt:message key="appCenter.label.publishedApps"/></a>
                        <c:if test="${isAdmin && !empty marketplaceUrl && marketplaceUrl != '???appCenter.link.marketplace.url???'}">
                            <c:choose>
                                <c:when test="${empty marketplaceTarget || marketplaceTarget == '???appCenter.link.marketplace.target???'}">
                                    <a href="#" onclick="return loadMarketplaceApps()" id="category-marketplace-apps" class="category"><fmt:message key="appCenter.label.marketplace"/></a>
                                </c:when>
                                <c:when test="${marketplaceTarget == '_popup'}">
                                    <a href="#" onclick="return loadMarketplace()" id="category-marketplace-apps" class="category"><fmt:message key="appCenter.label.marketplace"/></a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${marketplaceUrl}" target="${marketplaceTarget}" id="category-marketplace-apps" class="category"><fmt:message key="appCenter.label.marketplace"/></a>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${isAdmin}">
                <div class="menu-link-admin-top">
                    <a href="#" onclick="appCreate();return false" class="app-edit smallbutton"><i class="icon-edit"></i> <fmt:message key='console.app.create.label'/></a>   
                    <a href="#" onclick="appImport();return false" class="app-edit smallbutton"><i class="icon-edit"></i> <fmt:message key='console.app.import.label'/></a>
                </div>
                <ul id="marketplace-apps" class="published-apps"></ul>
                </c:if>
                <ul id="published-apps" class="published-apps">
                </ul>
                <c:if test="${isAdmin}">
                <div id="admin-content">
                    <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />
                </div>
                </c:if>
            </div>
        </div>

<%= AppUtil.getSystemAlert() %>

    <script>    
        <c:if test="${!isAnonymous}">
            HelpGuide.key = "help.web.desktop.user";
        </c:if>
        <c:if test="${isAdmin}">
            HelpGuide.key = "help.web.desktop.admin";
        </c:if>
    </script>

<commons:footer/>

<c:set var="mobileDisabled" value="<%= MobileUtil.isMobileDisabled() %>"/>
<script>
    <c:if test="${!mobileDisabled}">
    var mobileLinkTitle = "<fmt:message key="appCenter.label.mobileEdition"/>";
    var mobileLink = "<a href='${pageContext.request.contextPath}/web/mobile' id='header-mobile' onclick='return Mobile.viewMobileSite(\"${pageContext.request.contextPath}/home/\", \"${pageContext.request.contextPath}/web/mobile\")'><i class='icon-mobile-phone'>&nbsp;</i>" + mobileLinkTitle + "</a>";
    $("#header-links").prepend(mobileLink + " ");
    var url = "${pageContext.request.contextPath}/web/mobile";
    Mobile.directToMobileSite(url);
    </c:if>

    var marketplacePopup = function(marketplaceUrl) {
        var marketplaceAppPopupDialog = new PopupDialog("${pageContext.request.contextPath}/web/console/home", " ");
        marketplaceAppPopupDialog.src = marketplaceUrl;
        marketplaceAppPopupDialog.show();        
        return false;
    };

    var loadMarketplace = function() {
        var marketplaceAppUrl = "${pageContext.request.contextPath}/web/desktop/marketplace/app?url=" + encodeURIComponent("<c:url value="${marketplaceUrl}"/>");
        marketplacePopup(marketplaceAppUrl);
    };

    var loadApps = function(container, customUrl) {
        container = container || "#published-apps";
        var isMarketplace = (container === '#marketplace-apps');
        var jsonUrl = (customUrl) ? customUrl : "${pageContext.request.contextPath}/web/json/apps/published/userviews";

        // show loading icon
        $(container).empty();
        var loading = $('<div id="apps-loading"><i class="icon-spinner icon-spin icon-2x"></i> <fmt:message key="appCenter.label.loadingApps"/></div>');
        $(container).append(loading);

        // load JSON
        $.ajax({ 
            url : jsonUrl,
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
                        content += '<li>';
                        <c:if test="${isAdmin}">
                            if (!isMarketplace) {
                                content += '<a href="${pageContext.request.contextPath}/web/console/app/' + app.id + '//forms" onclick="return AdminBar.showQuickOverlay(\'${pageContext.request.contextPath}/web/console/app/' + app.id + '//forms\')" class="app-edit smallbutton"><i class="icon-edit"></i> <fmt:message key='adminBar.label.designApp'/></a>';
                            }
                        </c:if>
                        var userviewUrl = uv.url;
                        var imageUrl = uv.imageUrl;
                        if (!imageUrl) {
                            imageUrl = '${pageContext.request.contextPath}/web/userview/screenshot/' + app.id + '/' + uv.id;
                        }
                        content += '<a class="app-link" target="_blank" href="' + userviewUrl + '">\
                                            <span class="app-icon"><img src="' + imageUrl + '" width="240" border="0"></span>\
                                            <div class="app-name">' + uv.name + '</div>\
                                            <div class="app-description">' + app.name + '</div>\
                                            <div class="uv-description" style="display:none">' + uv.description + '</div>\
                                        </a>\
                                    </li>';
                    }
                }
                if (apps.length === 0) {
                    <c:if test="${!isAdmin}">
                    content += '<div class="apps-notice"><fmt:message key="mobile.apps.allApps"/>: <fmt:message key="console.run.apps.none"/></div>';
                    </c:if>
                    <c:if test="${isAnonymous}">
                        location.href = "${pageContext.request.contextPath}/web/login";
                    </c:if>
                }
                // show apps, hide loading icon
                $(loading).remove();
                $(container).append($(content));

                // sort by userview name
                var appsLi = $(".published-apps li");
                appsLi.sort(function(a, b) {
                    var aName = $(a).find(".app-name").text();
                    var bName = $(b).find(".app-name").text();
                    if (aName.indexOf("<") === 0 || aName.indexOf("#") === 0 || aName.toUpperCase() >= bName.toUpperCase()) {
                        return 1;
                    } else {
                        return -1;
                    }
                });
                appsLi.detach().appendTo($(container));

                <c:if test="${isAdmin}">
                    if (!isMarketplace) {
                        var extraContent = '<li class="menu-link-admin">';
                        extraContent += '<a href="#" onclick="appImport();return false" class="app-import app-edit smallbutton"><i class="icon-edit"></i> <fmt:message key='console.app.import.label'/></div>';
                        extraContent += '<a href="#" onclick="appCreate();return false" class="app-edit smallbutton"><i class="icon-edit"></i> <fmt:message key='console.app.create.label'/></div>';
                        extraContent += '<a class="app-link app-new" href="#" onclick="appCreate();return false">\
                                        <span class="app-icon"><i class="icon-plus"></i></span>\
                                        <div class="app-name"></div>\
                                    </a>\
                                </li>';
                        $(container).append($(extraContent));
                    } else {
                        $("#marketplace-apps a.app-link").click(function(e) {
                            var appName = $(this).find(".app-name").text();
                            var appUrl = $(this).attr("href");
                            var appId = appUrl.substring(appUrl.indexOf("/web/userview/") + "/web/userview/".length);
                            var marketplaceAppUrl = "${pageContext.request.contextPath}/web/desktop/marketplace/app?url=" + encodeURIComponent(appUrl) + "&appId=" + encodeURIComponent(appId) + "&name=" + encodeURIComponent(appName);
                            marketplacePopup(marketplaceAppUrl);
                            e.preventDefault();
                        });
                    }
                    var adminContent = $("#admin-content");
                    if (adminContent.length > 0) {
                        adminContent.detach();
                        $(container).append(adminContent);
                        adminContent.show();
                    }
                </c:if>
            }
        });
    };            
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
                $(list).find(".app-name:not(:Contains(" + filter + "))").closest("li").fadeOut(); 
                $(list).find(".app-name:Contains(" + filter + ")").closest("li").show(); 
            } else { 
                $(list).find("li").show(); 
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
    };  
    var loadPublishedApps = function() {
        var adminContent = $("#admin-content");
        if (adminContent.length > 0) {
            adminContent.detach();
            $("#main-content").append(adminContent);
            adminContent.hide();
        }        
        $(".published-apps").empty();
        $("#categories a").removeClass("category-selected");
        $("#category-published-apps").addClass("category-selected");
        $("#marketplace-apps").hide();
        $("#published-apps").show();
        $("#search form").remove();
        searchFilter($("#search"), $("#published-apps")); 
        loadApps("#published-apps");
    };
    var loadMarketplaceApps = function() {
        var adminContent = $("#admin-content");
        if (adminContent.length > 0) {
            adminContent.detach();
            $("#main-content").append(adminContent);
            adminContent.hide();
        }        
        $(".published-apps").empty();
        $("#categories a").removeClass("category-selected");
        $("#category-marketplace-apps").addClass("category-selected");
        $("#marketplace-apps").show();
        $("#published-apps").hide();
        $("#search form").remove();
        searchFilter($("#search"), $("#marketplace-apps")); 
        loadApps("#marketplace-apps", "${marketplaceUrl}");
    }
    $(function () { 
        loadPublishedApps();
    }); 
</script>        
