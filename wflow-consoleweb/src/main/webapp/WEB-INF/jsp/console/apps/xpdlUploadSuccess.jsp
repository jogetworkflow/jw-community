<script type="text/javascript">
    if (parent && parent.AdminBar.showQuickOverlay) {
        parent.PopupDialog.closeDialog();
        parent.AdminBar.showQuickOverlay('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes')
    } else {
        parent.location = '${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes';
    }
</script>