<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.datalist.create.label.title"/>
    </div>
    <div id="main-body-content">
        <p>&nbsp;</p>
        <div id="main-body-message">
            <fmt:message key="console.datalist.create.label.saved"/>
            <p id="main-body-submessage"><fmt:message key="console.datalist.create.label.popupWarning"/></p>
            <button onclick="launchUserviewBuilder()" class="form-button-large"><fmt:message key="console.datalist.create.label.launch"/></button>
        </div>
    </div>

    <script type="text/javascript">
        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }

        function launchUserviewBuilder() {
            var datalistBuilder = window.open("${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/builder/${datalistDefinition.id}");
            if (datalistBuilder) {
                closeDialog();
            }
        }

        if (parent && parent.JsonDataTable) {
            parent.JsonDataTable.refresh();
        }

        launchUserviewBuilder();
    </script>

<commons:popupFooter />



