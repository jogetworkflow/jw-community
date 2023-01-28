<%@ tag import="org.joget.workflow.util.WorkflowUtil"%>
<%@ tag import="org.joget.commons.util.HostManager"%>
<%@ tag import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

    </div>
</div>

<div id="footer">
    <jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=footer" />
    <%--
    <c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>
    <c:if test="${false && isVirtualHostEnabled}">
        <div id="footer-profile">
            <fmt:message key="console.header.top.label.profile"/>: <%= HostManager.getCurrentProfile() %>
        </div>
    </c:if>
    --%>
</div>

<jsp:include page="/WEB-INF/jsp/console/apps/adminBar.jsp" flush="true">
    <jsp:param name="webConsole" value="true"/>
    <jsp:param name="appControls" value="true"/>
</jsp:include>

<style>
<%= WorkflowUtil.getSystemSetupValue("customCss") %>
</style>

<script>
    if (window.parent !== self && window.parent.name !== "quickOverlayFrame") {
        $("body").addClass("quickOverlayFrame");
    } else {
        $("body").addClass("webconsole");
    }
    $(function(){
        $("#nav-body").prepend('<a class="menu-trigger" style="display:none;"><i class="fas fa-bars"></i></a>');
        $("#nav-body .menu-trigger").on("click", function(){
           $("#nav-body").toggleClass("show");
        });
        
        if ($("#main-action-buttons li").length > 1) {
            $("#main-action-buttons").parent().addClass("buttongroupdiv");
            $("#main-action-buttons").addClass("buttongroup");
            $("#main-action-buttons").prepend('<li class="moreaction"><a class="btn"><ui:msgEscJS key="general.method.label.actions"/> <i class="fas fa-chevron-down"></i></a></li>');
            
            $(".buttongroup").on("click", ".moreaction", function(){
                var bgroup = $(this).closest(".buttongroup");
                if (!$(bgroup).hasClass("focus")) {
                    $(bgroup).addClass("focus");
                    $(bgroup).find("a").off("click.bgroupfocus");
                    $(bgroup).find("a").on("click.bgroupfocus", function(){
                        setTimeout(function(){
                            $(bgroup).removeClass("focus");
                            $(bgroup).find("a").off("click.bgroupfocus"); 
                            $("body").off("click.bodybgroup");
                        }, 100);
                    });
                    $("body").off("click.bodytabs");
                    $("body").on("click.bodytabs", function(e){
                        if (!$(bgroup).is(e.target) && $(bgroup).has(e.target).length === 0) {
                            $(bgroup).removeClass("focus");
                            $(bgroup).find("a").off("click.bgroupfocus"); 
                            $("body").off("click.bodybgroup");
                        }
                    });
                } else {
                    $(bgroup).removeClass("focus");
                    $(bgroup).find("a").off("click.bgroupfocus"); 
                    $("body").off("click.bodybgroup");
                }
            });
        }
        
        $("body").on("click", ".ui-tabs-nav li.ui-tabs-active", function(){
            var tabs = $(this).closest(".ui-tabs-nav");
            if (!$(tabs).hasClass("focus")) {
                $(tabs).addClass("focus");
                $(tabs).find("a").off("click.tabsfocus");
                $(tabs).find("a").on("click.tabsfocus", function(){
                    setTimeout(function(){
                        $(tabs).removeClass("focus");
                        $(tabs).find("a").off("click.tabsfocus"); 
                        $("body").off("click.bodytabs");
                    }, 100);
                });
                $("body").off("click.bodytabs");
                $("body").on("click.bodytabs", function(e){
                    if (!$(tabs).is(e.target) && $(tabs).has(e.target).length === 0) {
                        $(tabs).removeClass("focus");
                        $(tabs).find("a").off("click.tabsfocus"); 
                        $("body").off("click.bodytabs");
                    }
                });
            } else {
                $(tabs).removeClass("focus");
                $(tabs).find("a").off("click.tabsfocus"); 
                $("body").off("click.bodytabs");
            }
        });
        $("body").on("submit", "form.blockui", function(){
            UI.blockUI();
            return true;
        });
        
        if ($("form.blockui select, #main-body-content-filter select").length > 0) {
            $("form.blockui select, #main-body-content-filter select").each(function(){
                if (!$(this).parent().is("td")) {
                    if ($("body").hasClass("rtl")) {
                        $(this).addClass("chosen-rtl");
                    }
                    $(this).off("change.chosenupdate");
                    $(this).chosen({width: "50%", placeholder_text: " "});
                    $(this).on("change.chosenupdate", function(){
                        $(this).trigger("chosen:updated");
                    });
                }
            });
        }
    });
</script>

<script type="text/javascript">
    HelpGuide.base = "${pageContext.request.contextPath}"
    HelpGuide.attachTo = "body.webconsole #header-links, body:not(.webconsole) #nav-title";
    HelpGuide.show();
</script>

<%= AppUtil.getSystemAlert() %>

</body>
</html>
