<%@ tag import="org.joget.workflow.util.WorkflowUtil"%>
        <style>
            <%= WorkflowUtil.getSystemSetupValue("customCss") %>
        </style>
        <script type="text/javascript">
            HelpGuide.base = "${pageContext.request.contextPath}"
            HelpGuide.attachTo = "#main-body-header";
            HelpGuide.show();
        </script>
    </body>
</html>
