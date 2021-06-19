<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader bodyCssClass=" builder-popup"/>

<div id="main-body-header"><fmt:message key="client.app.run.process.label.start.success" /></div>
<script type="text/javascript">
    if (parent && parent.CustomBuilder) {
        parent.JPopup.hide("runProcessDialog");
    } else if (parent && parent.PopupDialog && parent.PopupDialog.closeDialog) {
        parent.PopupDialog.closeDialog();
    }
</script>

<commons:popupFooter />