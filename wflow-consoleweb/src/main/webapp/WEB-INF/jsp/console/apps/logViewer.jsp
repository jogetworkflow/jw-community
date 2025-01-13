<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader bodyCssClass=" builder-popup no-header" builderTheme="true"/>
<style>
    #main {margin: 0 !important; padding: 0!important; width: 100%;}
    .form-row {padding-top: 25px; padding-left: 20px;}
    #main-title, #main-action {display: none;}
    .text {white-space: normal;}
    div#logviewer {padding: 0;}
</style>    
<jsp:include page="../log/log.jsp" flush="true" />
<commons:popupFooter />
