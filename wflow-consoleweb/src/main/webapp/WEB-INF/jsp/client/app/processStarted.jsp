<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

<h3>processStarted</h3>
<script type="text/javascript">
    if (parent && parent.PopupDialog && parent.PopupDialog.closeDialog) {
        parent.PopupDialog.closeDialog();
    }
</script>

<commons:popupFooter />