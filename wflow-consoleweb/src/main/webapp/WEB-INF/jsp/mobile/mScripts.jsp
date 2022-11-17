<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.commons.util.SecurityUtil"%>
<meta name="viewport" content="width=device-width,initial-scale=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/wro/mobile_common.css?build=<fmt:message key="build.number"/>" />
<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon_uv.ico"/>
<style>
    .ui-content {
        clear: both;
        overflow-x: auto;
        <c:if test="${userview.setting.getPropertyString('mobileViewTranslucent') == 'true'}">
        opacity: 0.9;
        </c:if>
    }
    <c:if test="${userview.setting.getPropertyString('mobileViewTranslucent') == 'true'}">
    .ui-html, #loginForm {
        padding: 5px 10px;
        background: white;
        opacity: 0.9;
        border-radius: 10px;
    }
    </c:if>
    #userview {
        background: url(${userview.setting.getPropertyString('mobileViewBackgroundUrl')}) <c:if test="${userview.setting.getPropertyString('mobileViewBackgroundStyle') != 'width'}">${userview.setting.getPropertyString('mobileViewBackgroundStyle')}</c:if> ${userview.setting.getPropertyString('mobileViewBackgroundColor')};
        <c:if test="${userview.setting.getPropertyString('mobileViewBackgroundStyle') == 'width'}">
            background-size: 100%;
        </c:if>        
    }
    #userview #logo {
        width: ${userview.setting.getPropertyString('mobileViewLogoWidth')};
        height: ${userview.setting.getPropertyString('mobileViewLogoHeight')};
        background: url(${userview.setting.getPropertyString('mobileViewLogoUrl')}) no-repeat;
        margin-top: 6px;
        <c:choose>
            <c:when test="${userview.setting.getPropertyString('mobileViewLogoAlign') == 'right'}">
                float: right;
            </c:when>
            <c:when test="${userview.setting.getPropertyString('mobileViewLogoAlign') == 'center'}">
                margin-left: auto;
                margin-right: auto;
            </c:when>
        </c:choose>
    }   
    ${userview.setting.getPropertyString('mobileViewCustomCss')}
</style>
<script type="text/javascript" src="${pageContext.request.contextPath}/wro/mobile_common.js?build=<fmt:message key="build.number"/>"></script>
<script>
    $(document).ready(function(){
        UI.base = "${pageContext.request.contextPath}";
        Mobile.contextPath = "${pageContext.request.contextPath}";
        
        $(document).on('pageinit', function() {
            $.unblockUI();
        });
        
        $('div:jqmData(role="page")').on('pageshow',function(){
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