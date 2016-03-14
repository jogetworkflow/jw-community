<script type="text/javascript">
    if (parent && parent.showQuickOverlay) {
        parent.PopupDialog.closeDialog();
        parent.showQuickOverlay('${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes')
    } else {
        parent.location = '${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes';
    }
</script>