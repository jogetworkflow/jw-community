<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="appName" value="${param.name}" />
<c:set var="appId" value="${param.appId}" />

<commons:popupHeader bodyCssClass=" builder-popup no-header" builderTheme="true" />
<style>
    html, body, .bgi-cover{
        height: 100%;
        overflow: hidden;
    }
    .d-flex {
        height: 100%;
        display: flex !important;
        -ms-flex-direction: row !important;
        flex-direction: row !important;
    }
    .bgi-cover {
        background-image: url('${pageContext.request.contextPath}/images/marketplace.png');
        background-size:cover;
    }
    .d-flex > * {
        width: 50%;
        box-sizing: border-box;
    }
    .login-form-wrapper {
        height: 100%;
        overflow:auto;
        padding:50px;
        display: flex !important;
    }
    #loginForm{
        margin:auto;
        width: 320px;
        padding: 40px;
        display: block;
        border-radius: 10px;
        box-shadow: 6px 6px 15px -6px rgba(0,0,0,0.75);
        align-items:center;
    }
    #loginForm h1, #loginForm p {
        text-align: center;
    }
    #loginForm input {
        margin-bottom: 10px; 
        width: 100%;
    }
    .singup, .continueAsGuest {
        display: block;
        text-align: center;
        color: var(--theme-link-color-1, #007bff);
        font-weight: normal;
    }
    .continueAsGuest {
        color: var(--theme-link-color-2, #AAAAAA);
    }
    @media (max-width: 767px) {
        .d-flex {
            display: block !important;
            overflow: scroll;
        }
        .d-flex > * {
            width: 100%;
        }
        .bgi-cover {
            display: none;
        }
    }
</style>    

<div class="content d-flex">
    <div class="login-form-wrapper">
        <form id="loginForm" name="loginForm" action="" method="POST">
            <input type="hidden" name="type" value="<c:out value="${type}" />" />
            <input type="hidden" name="pluginType" value="<c:out value="${pluginType}" />" />
            <h1><ui:msgEscHTML key="appCenter.link.marketplace.login"/></h1>
            <p><ui:msgEscHTML key="appCenter.link.marketplace.message"/></p>
            <c:if test="${!empty error}">
                <div class="form-errors">
                    <ui:msgEscHTML key="AbstractUserDetailsAuthenticationProvider.badCredentials"/>
                </div>    
            </c:if>    
            <table align="center">
                <tbody>
                    <tr><td><label><ui:msgEscHTML key="ubuilder.login.username"/> </label></td><td><input type="text" id="username" name="username" placeholder="<ui:msgEscHTML key="ubuilder.login.username"/>"></td></tr>
                    <tr><td><label><ui:msgEscHTML key="ubuilder.login.password"/> </label></td><td><input type="password" id="password" name="password" placeholder="<ui:msgEscHTML key="ubuilder.login.password"/>"></td></tr>
                    <tr><td>&nbsp;</td><td><input name="submit" class="form-button" type="submit" value="<ui:msgEscHTML key="ubuilder.login" />"></td></tr>
                    <tr><td colspan="2">
                            <a class="singup" href="<ui:msgEscHTML key="appCenter.link.marketplace.url" />/jw/web/userview/mp/mpp/_/signup" target="_blank"><ui:msgEscHTML key="appCenter.link.marketplace.createAccount" /></a><br/>
                            <a class="continueAsGuest"><ui:msgEscHTML key="appCenter.link.marketplace.guest" /></a> 
                    </td></tr>
                </tbody>
            </table>
        </form>
    </div>
    <div class="bgi-cover">
    </div>
</div>
<script>
    $(function(){
        $(".continueAsGuest").off("click").on("click", function(){
            $("#username").css("color", "transparent").val("_ContinueAsGuest");
            $("[name='submit']").trigger("click");
        });
    });
</script>    
<commons:popupFooter />