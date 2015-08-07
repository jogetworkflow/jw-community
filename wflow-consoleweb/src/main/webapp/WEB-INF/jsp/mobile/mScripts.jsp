<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<meta name="viewport" content="width=device-width,initial-scale=1">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/themes/ui-lightness/jquery-ui-1.10.3.custom.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/mobile/jqm/jquery.mobile-1.4.5.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/mobile/mobile.css">
<style>
    .ui-content {
        clear: both;
        overflow-x: auto;
        <c:if test="${userview.setting.properties.mobileViewTranslucent == 'true'}">
        opacity: 0.9;
        </c:if>
    }
    <c:if test="${userview.setting.properties.mobileViewTranslucent == 'true'}">
    .ui-html, #loginForm {
        padding: 5px 10px;
        background: white;
        opacity: 0.9;
        border-radius: 10px;
    }
    </c:if>
    #userview {
        background: url(${userview.setting.properties.mobileViewBackgroundUrl}) <c:if test="${userview.setting.properties.mobileViewBackgroundStyle != 'width'}">${userview.setting.properties.mobileViewBackgroundStyle}</c:if> ${userview.setting.properties.mobileViewBackgroundColor};
        <c:if test="${userview.setting.properties.mobileViewBackgroundStyle == 'width'}">
            background-size: 100%;
        </c:if>        
    }
    #userview #logo {
        width: ${userview.setting.properties.mobileViewLogoWidth};
        height: ${userview.setting.properties.mobileViewLogoHeight};
        background: url(${userview.setting.properties.mobileViewLogoUrl}) no-repeat;
        margin-top: 6px;
        <c:choose>
            <c:when test="${userview.setting.properties.mobileViewLogoAlign == 'right'}">
                float: right;
            </c:when>
            <c:when test="${userview.setting.properties.mobileViewLogoAlign == 'center'}">
                margin-left: auto;
                margin-right: auto;
            </c:when>
        </c:choose>
    }   
    ${userview.setting.properties.mobileViewCustomCss}
</style>
<script src="${pageContext.request.contextPath}/js/jquery/jquery-1.9.1.min.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery/jquery-migrate-1.2.1.min.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.10.3.min.js"></script>
<script src="${pageContext.request.contextPath}/mobile/jqm/jquery.cookie.js"></script>
<script src="${pageContext.request.contextPath}/mobile/jqm/jquery.mobile-1.4.5.min.js"></script>
<script src="${pageContext.request.contextPath}/js/json/ui.js"></script>
<script src="${pageContext.request.contextPath}/js/json/ui_ext.js"></script>
<script src="${pageContext.request.contextPath}/js/json/util.js"></script>
<script src="${pageContext.request.contextPath}/mobile/mobile.js"></script>
<script src="${pageContext.request.contextPath}/mobile/mobile_util.js"></script>
<script>
    $(document).ready(function(){
        UI.base = "${pageContext.request.contextPath}";
        Mobile.contextPath = "${pageContext.request.contextPath}";
    });
</script>    