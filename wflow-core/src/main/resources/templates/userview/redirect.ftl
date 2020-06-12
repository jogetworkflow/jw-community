<!DOCTYPE html>
<html>
    <head>
        <script type="text/javascript">
            <#if alertMessage != "" >
                alert("${alertMessage?js_string}");
            </#if>
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
        </script>
    </head>
    <body>
    </body>
</html>
