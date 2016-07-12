<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<script src="${pageContext.request.contextPath}/web/console/i18n/advtool?build=<fmt:message key="build.number"/>"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/wro/advancedTool.js?build=<fmt:message key="build.number"/>"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/wro/advancedTool.css?build=<fmt:message key="build.number"/>">
<script type="text/javascript">
    $(document).ready(function(){
        AdvancedTools.init("${param.jsonForm}", {
            contextPath : '${pageContext.request.contextPath}',
            appId : '${appId}',
            appVersion : '${appDefinition.version}',
            id : '${param.elementId}',
            builder : '${param.builder}'
        });
    });
</script>   