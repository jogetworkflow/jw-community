<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:header id="allApps" />

<div id="nav">
    <div id="nav-title">
        <p><i class="fas fa-wrench"></i> <fmt:message key="console.header.menu.label.apps"/></p>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <li id="nav-apps"><a class="nav-link" href="${pageContext.request.contextPath}/web/desktop/apps"><span class="nav-steps">&nbsp;</span> <fmt:message key="console.header.submenu.label.allApps"/></a></li>
        </ul>
        
        <div id="adminWelcome">
            <jsp:include page="/WEB-INF/jsp/console/welcome.jsp" flush="true" />
        </div>
    </div>
</div>

<div id="main" class="nv-apps">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
            <li><button onclick="appCreate()"><fmt:message key="console.app.create.label"/></button></li>
            <li><button onclick="appImport()"><fmt:message key="console.app.import.label"/></button></li>
        </ul>
    </div>
    <div id="main-body">
        <script> 
            (function ($) { 
                jQuery.expr[':'].Contains = function(a,i,m){ 
                    return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase())>=0; 
                };  

                function listFilter(header, list) { 
                    var form = $("<form>").attr({"class":"filterform","action":"#","onsubmit":"return false"}), 
                    input = $("<input>").attr({"class":"filterinput","type":"text"}); 
                    $(form).append($("<span class='filterlabel'><i class='fas fa-search'></i></span>")).append(input).appendTo(header);  
                    $(input) .change( function () { 
                        var filter = $(this).val(); 
                        if(filter) { 
                            $(list).find("a:not(:Contains(" + filter + "))").parent().slideUp(); 
                            $(list).find("a:Contains(" + filter + ")").parent().slideDown(); 
                        } else { 
                            $(list).find("li").slideDown(); 
                        } 
                        return false; 
                    }) .keyup( function () { 
                        $(this).change(); 
                    }); 
                }  

                $(function () { 
                    listFilter($("#nv-unpublished h4"), $("#nv-unpublished ul")); 
                    listFilter($("#nv-published h4"), $("#nv-published ul")); 
                    $("#nv-published h4 input").focus();
                    
                    $(".nv-list li a").on("click", function(){
                        if (window.parent && window.parent['CustomBuilder']) {
                            window.parent['CustomBuilder'].ajaxRenderBuilder($(this).attr("href"));
                            window.parent['AdminBar'].hideQuickOverlay();
                            return false;
                        } else {
                            return true;
                        }
                    });
                }); 
            }(jQuery)); 
        </script>

        <div id="nv-apps">
            <div id="nv-published" class="nv-col  nv-border">
                <h4><fmt:message key="appCenter.label.publishedApps"/></h4>
                <ul class="nv-list">
                    <c:forEach items="${appPublishedList}" var="appDef">
                        <li>
                            <a class="nv-link" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/builders" target="_blank"><span class="nv-link-name"><i class="fas fa-file"></i> ${appDef.name}</span> <span class="nv-version"><fmt:message key="console.app.common.label.version"/> ${appDef.version}</span></a>
                        </li>
                    </c:forEach>
                </ul>
            </div>
            <div id="nv-unpublished" class="nv-col">
                <h4><fmt:message key="appCenter.label.unpublishedApps"/></h4>
                <ul class="nv-list">
                    <c:forEach items="${appUnpublishedList}" var="appDef">
                        <li>
                            <a class="nv-link" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/builders" target="_blank"><span class="nv-link-name"><i class="far fa-file"></i> ${appDef.name}</span> <span class="nv-version"><fmt:message key="console.app.common.label.version"/> ${appDef.version}</span></a>
                        </li>
                    </c:forEach>
                </ul>
            </div>
            <div id="nv-clear"></div>
        </div>
    </div>
</div>

<script>
    Template.init("#menu-apps", "#nav-apps");
</script>

<commons:footer />
