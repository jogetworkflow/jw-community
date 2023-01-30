        <script type="text/javascript">
            $(function(){
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

            HelpGuide.base = "${pageContext.request.contextPath}"
            HelpGuide.attachTo = "#main-body-header";
            HelpGuide.show();
        </script>
        <jsp:include page="/WEB-INF/jsp/includes/csrf.jsp" flush="true" />
    </body>
</html>
