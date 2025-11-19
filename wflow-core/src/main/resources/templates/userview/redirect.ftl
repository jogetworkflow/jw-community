<!DOCTYPE html>
<html>
    <head>
        <script type="text/javascript" src="${request.contextPath}/wro/common.preload.js"></script>
        <script type="text/javascript" src="${request.contextPath}/wro/common.js"></script>
        <script type="text/javascript" src="${request.contextPath}/js/sweetAlert2/resources/sweetalert2.min.js" ></script>
        <script>loadCSS("${request.contextPath}/wro/common.css")</script>
        ${css!}
        ${js_css_lib!}
    </head>
    <body>
        <script type="text/javascript">
            $(document).ready(function(){
                const act = function() {
                    <#if redirectUrl == "SCRIPT_RELOAD_PARENT" >
                        if (parent.PopupDialog) {
                            parent.PopupDialog.closeDialog();
                        }
                        <#if redirectParent == "top" >top.<#else>parent.</#if>window.location.href = <#if redirectParent == "top" >top.<#else>parent.</#if>window.location.href;
                    <#elseif redirectUrl == "SCRIPT_CLOSE_POPUP" >
                        parent.PopupDialog.closeDialog();
                    <#else>
                        <#if redirectParent == "true" >parent.<#elseif redirectParent == "top" >top.</#if>location.href = "${redirectUrl}";
                    </#if>
                };

                //separator for ajax-component.js to parse alert message. DO NOT REMOVE
                <#if alertMessage != "" >
                    UI.alertBlock("${alertMessage?js_string}", act);
                <#else>    
                    act();
                </#if>
            });
        </script>
    </body>
</html>
