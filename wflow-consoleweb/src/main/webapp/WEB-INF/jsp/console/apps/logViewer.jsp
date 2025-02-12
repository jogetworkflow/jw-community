<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader bodyCssClass=" builder-popup no-header" builderTheme="true"/>
<style>
    #main {margin: 0 !important; padding: 0!important; width: 100%;}
    .form-row {padding-top: 25px; padding-left: 20px;}
    #main-title, #main-action {display: none;}
    .text {white-space: normal;}
    body.no-header.builder-popup div#main-body {margin-top: 0;}
    body.no-header.builder-popup div#main-body-content {padding-top: 0 !important;}
    body.rtl.no-header.builder-popup div.form-row {
        padding-right: 20px;
        padding-left: 0px;
        margin-right: 10px;
        margin-left: 0px;
    }
</style>    
<jsp:include page="../log/log.jsp" flush="true" />
<commons:popupFooter />
