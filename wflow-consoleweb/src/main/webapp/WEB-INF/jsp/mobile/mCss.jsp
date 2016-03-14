<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<style>
    iframe {
        display: none;
    }
    #online-status {
        font-size: 11px;
        position: fixed;
        top: 2px;
        left: 2px;
        z-index: 10000;
        opacity: 0.8;
        color: white;
        background: #00bb00;
        padding: 2px;
        display: none;
        border-radius: 21px;
        height: 3px;
        width: 3px;
        line-height: 1px;
        border: solid 1px #535353;
    }
    .ui-content {
        clear: both;
        overflow-x: auto;
        <c:if test="${userview.setting.properties.mobileViewTranslucent == 'true'}">
        opacity: 0.9;
        </c:if>
    }
    .ui-body-c .ui-link {
        margin: 10px 5px 5px 5px;
        display: block;
        text-decoration: underline;
        font-size: 1.1em;
    }
    .ui-mobile .navbar, .ui-mobile-viewport .ui-title .container {
        display: none;
    }
    .ui-mobile .ui-checkbox input, .ui-mobile .ui-radio input {
        display: none;
    }
    .ui-mobile .form-section-title span, .ui-mobile .subform-section-title span {
        display: block;
        border: 1px solid #bbb;
        background: #bbb;
        color: #333;
        font-weight: bold;
        text-shadow: 0 1px 0 #eee;
        padding: 10px;
        border-radius: 2px;
        margin: 5px 0px;
    }
    .ui-mobile .form-cell-value, .ui-mobile .subform-cell-value {
        padding: 7px;
        border: solid 1px silver;
        border-radius: 10px;
        margin: 7px;
        box-shadow: -1px -1px 1px silver;
        background: #f6f6f6;
        color: #696969;
    }
    .ui-mobile .form-cell-full {
        border: none;
        box-shadow: none;
        background: none;
    }
    .ui-mobile .form-cell label, .ui-mobile .subform-cell label {
        padding: 12px 0px;
        display: inline-block;
    }
    .ui-mobile .form-errors {
        text-align: center;
        padding: 5px 5px 20px 5px;    
    }    
    .ui-mobile .form-error-message {
        color: red;
    }
    .ui-mobile ul {
        list-style: none;
    }
    <c:if test="${userview.setting.properties.mobileViewTranslucent == 'true'}">
    .ui-html, #loginForm {
        padding: 5px 10px;
        background: white;
        opacity: 0.9;
        border-radius: 10px;
    }
    </c:if>
    .ui-html-footer {
        clear: both;
    }
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
    .quickEdit, #form-canvas .quickEdit {
        display: none;
    }
    .ui-mobile .form-cell, .ui-mobile .subform-cell {
        clear: both;
    }
    .ui-mobile .print-button, .ui-mobile .ui-body-c .print-button {
        display: none;
    }
    .boxy-inner {
        overflow: auto;
    }
    iframe.boxy-content {
        padding: 0px;
    }
    .ui-mobile .form-fileupload-value .ui-checkbox {
        margin-top: 15px;
    }
    .ui-mobile .form-fileupload-value .ui-checkbox input {
        display: block;
    }
    .ui-loader-default {
        background: none;
        filter: Alpha(Opacity=80);
        opacity: .8;
        width: 46px;
        height: 46px;
        margin-left: -23px;
        margin-top: -23px;
    }
    .ui-loader .ui-icon {
        background-color: transparent;
        display: block;
        margin: 0;
        width: 44px;
        height: 44px;
        padding: 1px;
        -webkit-border-radius: 36px;
        border-radius: 36px;
    }
    .ui-icon-loading, .ui-loader .ui-icon-loading {
        background-color: transparent;
	background-size: 46px 46px;
    }
    .ui-listview > .ui-li-divider {
        background: #bbb;
    }
    ${userview.setting.properties.mobileViewCustomCss}
</style>
