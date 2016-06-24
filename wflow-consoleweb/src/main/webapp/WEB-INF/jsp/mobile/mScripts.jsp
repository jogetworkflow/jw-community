<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.SecurityUtil"%>
<meta name="viewport" content="width=device-width,initial-scale=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/wro/mobile_common.css?build=<fmt:message key="build.number"/>" />
<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon_uv.ico"/>
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
<script type="text/javascript" src="${pageContext.request.contextPath}/wro/mobile_common.js?build=<fmt:message key="build.number"/>"></script>
<script>
    $(document).ready(function(){
        UI.base = "${pageContext.request.contextPath}";
        Mobile.contextPath = "${pageContext.request.contextPath}";
        
        $(document).on('pageinit', function() {
            $.unblockUI();
        });
        
        $('div:jqmData(role="page")').live('pageshow',function(){
            $( document ).trigger( "enhance.tablesaw" );
        });
    });
</script>    
<script>
    ConnectionManager.tokenName = "<%= SecurityUtil.getCsrfTokenName() %>";
    ConnectionManager.tokenValue = "<%= SecurityUtil.getCsrfTokenValue(request) %>";
    JPopup.tokenName = "<%= SecurityUtil.getCsrfTokenName() %>";
    JPopup.tokenValue = "<%= SecurityUtil.getCsrfTokenValue(request) %>";
</script>