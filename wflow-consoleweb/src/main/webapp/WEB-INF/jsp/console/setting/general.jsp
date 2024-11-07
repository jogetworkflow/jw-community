<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.commons.util.SecurityUtil"%>
<%@ page import="java.io.File,org.joget.commons.util.SetupManager"%>
<%@ page import="org.joget.commons.util.HostManager"%>

<c:set var="isVirtualHostEnabled" value="<%= HostManager.isVirtualHostEnabled() %>"/>
<c:set var="isNonceSupported" value="<%= SecurityUtil.getNonceGenerator() != null %>"/>
<c:set var="isEnterprise" value="<%= AppUtil.isEnterprise() %>"/>

<commons:header />
<style>
    .row-content{
        display: block;
        float: none;
    }

    .row-title{
        font-weight: bold;
    }

    .main-body-row {
        display: none;
    }

    div#content {
        min-height: 100vh;
    }


    .main-body-content-subheader {
        font-weight: 600;
        background-color: transparent;
        margin-bottom: 0px;
        cursor: pointer;
        padding-right: 0;
        padding-left: 0;
        margin-right: 1.6rem;
        width: fit-content;
        padding-bottom:20px !important;
    }

    body.rtl .main-body-content-subheader:first-child {
        margin-right: 0;
    }

    .main-body-content-subheader:hover {
        color: #009265;
    }

    .main-body-content-subheader.selected span{
        color: #009265;
        padding: 0 0 5px 0;
    }

    #header-container {
        display: flex;
        margin-bottom: 20px;
        border-bottom: 1px solid #f0f0f0;
    }

    #main-title{
        display: inline-flex;
        justify-content: space-between;
        width: 100%;
        align-items: center;
        margin-top:0px;
     }

    #search-bar {
        width: min-content;
        display: flex;
        align-items: center;
        position:relative;
        border: 1px solid rgb(206, 212, 218);
        height: 20px;
        padding:5px 5px 5px 10px;
        border-radius: 5px;
    }

    div.main-body-row > span > div.form-row.highlight {
        outline: 3px solid var(--console-button-primary-bg);
        border-radius: 3px;
        outline-offset: 0.5rem;    
    }

    #search-bar:has(input:focus-visible) {
        outline: 2px solid var(--console-button-primary-bg-hover);
        border: 1px solid transparent;
    }

    #search-bar #search-icon, #search-bar #search-icon:hover {
        font-size: 0.8rem;
        color: initial;
        cursor: initial;
    }

    body[system-theme='dark'] #search-bar #search-icon, 
    body[system-theme='dark'] #search-bar #search-icon:hover,
    body[system-theme='dark'] #search-bar input,
    body[system-theme='dark'] #search-results {
        color: #e2e2e2;
    }

    #search-bar input{
        width:180px;
        padding: 0px;
        border:none;
        background: transparent;
        border-bottom: none;
        box-shadow: none;
        transition: width 0.5s ease, padding 0.3s ease;
        font-weight: normal;
        outline: 0;
        height: 20px;
        margin-left:5px;
        font-size: 0.8rem;
    }

    body.rtl #search-bar input{
        margin-left: 0px;
        margin-right: 5px;
    }

    #search-results {
        position: absolute;
        top: 100%; 
        left: 0; 
        right: 0;
        background-color: white; 
        border: 1px solid #ccc; 
        z-index: 10; 
        max-height: 200px;
        overflow-y: auto; 
        display: none; 
        width:100%;
        font-weight: normal;
        font-size: 0.8rem;
    }

    body[system-theme='dark'] #search-results {
        background-color: var(--console-main-background-color); 
        border: 1px solid var(--console-border-color);
    }

    #search-results .result-item {
        padding: 5px;
        cursor: pointer;
    }

    .highlight {
        color: #009265;
        font-weight:bold;
    }

    .form-buttons {
        position: sticky;
        bottom: 0px;
        width: 100%;
        display: flex;
        justify-content: flex-start;
        background-color: #fff;
        margin-bottom: 0;
        padding-bottom: 20px;
        padding-top: 20px;
        padding-left: 15px;
        border: 0;
        box-shadow: 0 -4px 4px -2px rgba(0, 0, 0, 0.1);
    }

    body[system-theme='dark'] .form-buttons {
        background-color: #121212;
        box-shadow: 0 -4px 4px -2px rgba(255, 255, 255, 0.1);
    }

    body.rtl .form-buttons {
        padding-left: initial;
        padding-right: 15px;
    }

    body.rtl {
        left:25px;
        right: initial;
    }

    a#licenseLink, a#sysinfoLink {
        background: var(--console-button-secondary-bg)
    }

    a#licenseLink:hover, a#sysinfoLink:hover {
        background: var(--console-button-secondary-bg-hover)
    }
</style>
<div id="nav">
    <div id="nav-title">
        <p><i class="fas fa-cogs" id="search-icon"></i> <fmt:message key='console.header.top.label.settings'/></p>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="subMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title">
        <fmt:message key="console.header.submenu.label.setting.general"/> 
        <div id="search-bar">
            <i class="fas fa-search" id="search-icon"></i>
            <input>
            <div id="search-results" class="results-container"></div>
        </div>
    </div>
    <div id="main-action">
    </div>
    <div id="main-body">
        <div id="generalSetup">
            <form method="post" id="generalSettings" class="blockui" action="${pageContext.request.contextPath}/web/console/setting/general/submit">
            <jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=settings" />
            <div id="header-container">
                <div class="main-body-content-subheader" id="uiSetting">
                    <span><fmt:message key="console.setting.general.header.uiSetting"/></span>
                </div>
                <div class="main-body-content-subheader" id="timeSettings">
                    <span><fmt:message key="console.setting.general.header.timeSettings"/></span>
                </div>
                <div class="main-body-content-subheader" id="saSettings">
                    <span><fmt:message key="console.setting.general.header.saSettings"/></span>
                </div>
                <div class="main-body-content-subheader" id="smtpSettings">
                    <span><fmt:message key="console.setting.general.header.smtpSettings"/></span>
                </div>
            </div>
            <div id="content">
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="defaultUserview"><fmt:message key="console.setting.general.label.defaultUserview"/></label>
                            <span class="form-input">
                                <select id="defaultUserview" name="defaultUserview">
                                    <c:set var="prevAppName" value="" />
                                    <c:forEach var="userviewDef" items="${userviewDefinitionList}">
                                        <c:set var="userviewPath" value="${userviewDef.appId}/${userviewDef.id}" />
                                        <c:set var="appName" value="${userviewDef.appDefinition.name}" />
                                        <c:if test="${appName != prevAppName}">
                                            <c:if test="${prevAppName ne ''}">
                                            </optgroup>
                                            </c:if>
                                            <c:set var="prevAppName" value="${appName}" />
                                            <optgroup label="<ui:stripTag html="${prevAppName}"/>">
                                        </c:if>
                                        <c:set var="selected"><c:if test="${userviewPath == settingMap['defaultUserview']}"> selected</c:if></c:set>
                                        <option value="${userviewPath}" ${selected}><ui:stripTag html="${userviewDef.name}"/></option>
                                    </c:forEach>
                                    </optgroup>
                                </select>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="systemTheme"><fmt:message key="console.setting.general.label.system.theme"/></label>
                            <span class="form-input">
                                <select id="systemTheme" name="systemTheme">
                                    <option value="light" <c:if test="${settingMap['systemTheme'] eq 'light'}">selected</c:if>><fmt:message key="console.setting.general.label.light.theme"/></option>
                                    <option value="dark" <c:if test="${settingMap['systemTheme'] eq 'dark'}">selected</c:if>><fmt:message key="console.setting.general.label.dark.theme"/></option>
                                </select>
                            </span>
                        </div>
                    </span>
                </div> 
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="landingPage"><fmt:message key="console.setting.general.label.landingPage"/></label>
                            <span class="form-input">
                                <input id="defaultLandingPage" type="text" name="landingPage" value="<c:out value="${settingMap['landingPage']}"/>"/>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span><span> /home</span></i>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="systemTimeZone"><fmt:message key="console.setting.general.label.systemTimeZone"/></label>
                            <span class="form-input">
                                <select id="systemTimeZone" name="systemTimeZone">
                                    <c:forEach var="timezone" items="${timezones}">
                                        <c:set var="selected"><c:if test="${timezone.key == settingMap['systemTimeZone']}"> selected</c:if></c:set>
                                        <option value="${timezone.key}" ${selected}>${timezone.value}</option>
                                    </c:forEach>
                                </select>
                                <br>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> ${timezones[serverTZ]}</i>
                            </span>
                        </div>
                    </span>
                </div>            
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="systemLocale"><fmt:message key="console.setting.general.label.systemLocale"/></label>
                            <span class="form-input">
                                <select id="systemLocale" name="systemLocale">
                                    <c:forEach var="locale" items="${localeList}">
                                        <c:set var="selected"><c:if test="${locale.key == settingMap['systemLocale']}"> selected</c:if></c:set>
                                        <option value="${locale.key}" ${selected}>${locale.value}</option>
                                    </c:forEach>
                                </select>
                                <br>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> en_US</i>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="systemDateFormat"><fmt:message key="console.setting.general.label.systemDateFormat"/></label>
                            <span class="form-input">
                                <input id="systemDateFormat" type="text" name="systemDateFormat" value="<c:out value="${settingMap['systemDateFormat']}"/>"/>
                                <br/>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> <fmt:message key="console.setting.general.default.systemDateFormat"/></i>
                            </span>
                        </div>
                    </span>
                </div>            
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="dateFormatFollowLocale"><fmt:message key="console.setting.general.label.dateFormatFollowLocale"/></label>
                            <span class="form-input">
                            <c:set var="checked"></c:set>
                            <c:if test="${settingMap['dateFormatFollowLocale'] == 'true'}">
                                <c:set var="checked">checked</c:set>
                            </c:if>
                            <input type="checkbox" id="dateFormatFollowLocale" name="dateFormatFollowLocale" ${checked} />
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="datepickerFollowLocale"><fmt:message key="console.setting.general.label.datepickerFollowLocale"/></label>
                            <span class="form-input">
                            <c:set var="checked"></c:set>
                            <c:if test="${settingMap['datepickerFollowLocale'] == 'true'}">
                                <c:set var="checked">checked</c:set>
                            </c:if>
                            <input type="checkbox" id="datepickerFollowLocale" name="datepickerFollowLocale" ${checked} />
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="enableUserLocale"><fmt:message key="console.setting.general.label.enableUserLocale"/></label>
                            <span class="form-input">
                            <c:set var="checked"></c:set>
                            <c:if test="${settingMap['enableUserLocale'] == 'true'}">
                                <c:set var="checked">checked</c:set>
                            </c:if>
                            <input type="checkbox" id="enableUserLocale" name="enableUserLocale" ${checked} />
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="userLocale"><fmt:message key="console.setting.general.label.userLocale"/></label>
                            <span class="form-input">
                                <input id="userLocale" type="text" name="userLocale" value="<c:out value="${settingMap['userLocale']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="rightToLeft"><fmt:message key="console.setting.general.label.rightToLeft"/></label>
                            <span class="form-input">
                            <c:set var="checked"></c:set>
                            <c:if test="${settingMap['rightToLeft'] == 'true'}">
                                <c:set var="checked">checked</c:set>
                            </c:if>
                            <input type="checkbox" id="rightToLeft" name="rightToLeft" ${checked} />
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="displayNameFormat"><fmt:message key="console.setting.general.label.displayNameFormat"/></label>
                            <span class="form-input">
                                <select id="displayNameFormat" name="displayNameFormat">
                                    <option value="">{<fmt:message key="console.directory.user.common.label.firstName"/>} {<fmt:message key="console.directory.user.common.label.lastName"/>}</option>
                                    <option value="{lastName} {firstName}" <c:if test="${settingMap['displayNameFormat'] eq '{lastName} {firstName}'}">selected</c:if>>{<fmt:message key="console.directory.user.common.label.lastName"/>} {<fmt:message key="console.directory.user.common.label.firstName"/>}</option>
                                    <option value="{firstName}{lastName}" <c:if test="${settingMap['displayNameFormat'] eq '{firstName}{lastName}'}">selected</c:if>>{<fmt:message key="console.directory.user.common.label.firstName"/>}{<fmt:message key="console.directory.user.common.label.lastName"/>}</option>
                                    <option value="{lastName}{firstName}" <c:if test="${settingMap['displayNameFormat'] eq '{lastName}{firstName}'}">selected</c:if>>{<fmt:message key="console.directory.user.common.label.lastName"/>}{<fmt:message key="console.directory.user.common.label.firstName"/>}</option>
                                </select>
                                <br>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> {<fmt:message key="console.directory.user.common.label.firstName"/>} {<fmt:message key="console.directory.user.common.label.lastName"/>}</i>
                            </span>
                        </div>
                    </span>
                </div> 
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="css"><fmt:message key="console.setting.general.label.css"/></label>
                            <span class="form-input">
                                <input id="css" type="text" name="css" value="<c:out value="${settingMap['css']}"/>"/>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> ${pageContext.request.contextPath}/css/new.css</i>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="customCss"><fmt:message key="console.setting.general.label.customCss"/></label>
                            <span class="form-input">
                                <textarea rows="15" id="customCss" type="text" name="customCss"><c:out value="${settingMap['customCss']}"/></textarea>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="uiSetting">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="disableListRenderHtml"><fmt:message key="console.setting.general.label.disableListRenderHtml"/></label>
                            <span class="form-input">
                            <c:set var="checked"></c:set>
                            <c:if test="${settingMap['disableListRenderHtml'] == 'true'}">
                                <c:set var="checked">checked</c:set>
                            </c:if>
                            <input type="checkbox" id="disableListRenderHtml" name="disableListRenderHtml" ${checked} />
                            </span>
                        </div>
                    </span>
                </div>            
                <div class="main-body-row" data-header="timeSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="deadlineCheckerInterval"><fmt:message key="console.setting.general.label.deadlineCheckerInterval"/></label>
                            <span class="form-input">
                                <input id="deadlineCheckerInterval" type="text" name="deadlineCheckerInterval" value="<c:out value="${settingMap['deadlineCheckerInterval']}"/>"/>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> 0</i>
                            </span>
                        </div>
                    </span>
                </div>
                <c:if test="${isNonceSupported}">
                    <div class="main-body-row" data-header="timeSettings">
                        <span class="row-content">
                            <div class="form-row">
                                <label for="extendNonceCacheTime"><fmt:message key="console.setting.general.label.extendNonceCacheTime"/></label>
                                <span class="form-input">
                                    <input id="extendNonceCacheTime" type="number" min="0" max="12" name="extendNonceCacheTime" value="<c:out value="${settingMap['extendNonceCacheTime']}"/>"/>
                                </span>
                            </div>
                        </span>
                    </div>
                </c:if>              
                <c:if test="${!userSecurity.disableHashLogin}">
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="masterLoginUsername"><fmt:message key="console.setting.general.label.masterLoginUsername"/></label>
                            <span class="form-input">
                                <input id="masterLoginUsername" type="text" name="masterLoginUsername" value="<c:out value="${settingMap['masterLoginUsername']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="masterLoginPassword"><fmt:message key="console.setting.general.label.masterLoginPassword"/></label>
                            <span class="form-input">
                                <input id="masterLoginPassword" type="password" name="masterLoginPassword" value="<c:out value="${settingMap['masterLoginPassword']}"/>"/>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.masterLoginHash"/></span><span id="masterLoginHash">-</span></i>
                            </span>
                        </div>
                    </span>
                </div>
                </c:if>
                <c:if test="${!isVirtualHostEnabled}">
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="dataFileBasePath"><fmt:message key="console.setting.general.label.dataFileBasePath"/></label>
                            <span class="form-input">
                                <input id="dataFileBasePath" type="text" name="dataFileBasePath" value="<c:out value="${settingMap['dataFileBasePath']}"/>"/>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> <%= new java.io.File(SetupManager.getBaseDirectory()).getAbsolutePath() %></i>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="designerwebBaseUrl"><fmt:message key="console.setting.general.label.designerwebBaseUrl"/></label>
                            <span class="form-input">
                                <input id="designerwebBaseUrl" type="text" name="designerwebBaseUrl" value="<c:out value="${settingMap['designerwebBaseUrl']}"/>"/>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> <%= AppUtil.getDesignerContextPath() %></i>
                            </span>
                        </div>
                    </span>
                </div>
                </c:if>
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="deleteProcessOnCompletion"><fmt:message key="console.setting.general.label.processCompletionDataHandling"/></label>
                            <span class="form-input">
                                <select id="deleteProcessOnCompletion" name="deleteProcessOnCompletion">
                                    <option value=""><fmt:message key="console.setting.general.label.retainProcessOnCompletion"/></option>
                                    <option value="true" <c:if test="${'true' eq settingMap['deleteProcessOnCompletion']}"> selected</c:if>><fmt:message key="console.setting.general.label.deleteProcessOnCompletion"/></option>
                                    <option value="archive" <c:if test="${'archive' eq settingMap['deleteProcessOnCompletion']}"> selected</c:if>><fmt:message key="console.setting.general.label.archiveProcessOnCompletion"/></option>
                                </select>
                                <jsp:include page="../monitor/archiveStatus.jsp" flush="true"/>
                                <div class="alert alert-warning" style="margin-top:10px;">
                                    <fmt:message key="console.monitoring.archiveProcessData.warn"/>
                                </div>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="mediumWarningLevel"><fmt:message key="console.setting.general.label.mediumWarningLevel"/></label>
                            <span class="form-input">
                                <input id="mediumWarningLevel" type="text" name="mediumWarningLevel" value="<c:out value="${settingMap['mediumWarningLevel']}"/>"/>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> 20</i>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="criticalWarningLevel"><fmt:message key="console.setting.general.label.criticalWarningLevel"/></label>
                            <span class="form-input">
                                <input id="criticalWarningLevel" type="text" name="criticalWarningLevel" value="<c:out value="${settingMap['criticalWarningLevel']}"/>"/>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> 50</i>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="fileSizeLimit"><fmt:message key="console.setting.general.label.fileSizeLimit"/></label>
                            <span class="form-input">
                                <input id="fileSizeLimit" type="text" name="fileSizeLimit" value="<c:out value="${settingMap['fileSizeLimit']}"/>"/>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> <fmt:message key="console.setting.general.label.noLimit"/></i>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="disablePerformanceAnalyzer"><fmt:message key="console.setting.general.label.disablePerformanceAnalyzer"/></label>
                            <span class="form-input">
                            <c:set var="checked"></c:set>
                            <c:if test="${settingMap['disablePerformanceAnalyzer'] == 'true'}">
                                <c:set var="checked">checked</c:set>
                            </c:if>
                            <input type="checkbox" id="disablePerformanceAnalyzer" name="disablePerformanceAnalyzer" ${checked} />
                            </span>
                        </div>
                    </span>
                </div>            
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="performanceAnalyzerThreshold"><fmt:message key="console.setting.general.label.performanceAnalyzerThreshold"/></label>
                            <span class="form-input">
                                <input id="performanceAnalyzerThreshold" type="text" name="performanceAnalyzerThreshold" value="<c:out value="${settingMap['performanceAnalyzerThreshold']}"/>"/>
                                <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span><span> 100</span></i>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="jsonpWhitelist"><fmt:message key="console.setting.general.label.jsonpWhitelist"/></label>
                            <span class="form-input">
                                <input id="jsonpWhitelist" type="text" name="jsonpWhitelist" value="<c:out value="${settingMap['jsonpWhitelist']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="jsonpIPWhitelist"><fmt:message key="console.setting.general.label.jsonpIPWhitelist"/></label>
                            <span class="form-input">
                                <input id="jsonpIPWhitelist" type="text" name="jsonpIPWhitelist" value="<c:out value="${settingMap['jsonpIPWhitelist']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>
                <c:if test="${isEnterprise}">
                    <div class="main-body-row" data-header="saSettings"> 
                        <span class="row-content">
                            <div class="form-row">
                                <label for="glowrootUrl"><fmt:message key="apm.glowrootUrl"/></label>
                                <span class="form-input">
                                    <input id="glowrootUrl" type="text" name="glowrootUrl" value="<c:out value="${settingMap['glowrootUrl']}"/>"/>
                                    <i><span class="ftl_label"><fmt:message key="console.setting.general.label.default"/></span> http://localhost:4000</i>
                                </span>
                            </div>
                        </span>
                    </div>
                </c:if>   
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="startProcessId"><fmt:message key="console.setting.general.label.startProcessId"/></label>
                            <span class="form-input">
                                <select id="startProcessId" name="startProcessId">
                                    <option value=""><fmt:message key="console.setting.general.label.startProcessId.UUID"/></option>
                                    <option value="processId" <c:if test="${'processId' == settingMap['startProcessId']}"> selected</c:if>><fmt:message key="console.setting.general.label.startProcessId.processId"/></option>
                                </select>
                            </span>
                        </div>
                    </span>
                </div> 
                <div class="main-body-row" data-header="saSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="environmentName"><fmt:message key="console.setting.general.label.environmentName"/></label>
                            <span class="form-input">
                                <input id="environmentName" type="text" name="environmentName" value="<c:out value="${settingMap['environmentName']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>                
                <div class="main-body-row" data-header="smtpSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="smtpHost"><fmt:message key="console.setting.general.label.smtpHost"/></label>
                            <span class="form-input">
                                <input id="smtpHost" type="text" name="smtpHost" value="<c:out value="${settingMap['smtpHost']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="smtpSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="smtpPort"><fmt:message key="console.setting.general.label.smtpPort"/></label>
                            <span class="form-input">
                                <input id="smtpPort" type="text" name="smtpPort" value="<c:out value="${settingMap['smtpPort']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="smtpSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="smtpSecurity"><fmt:message key="console.setting.general.label.smtpSecurity"/></label>
                            <span class="form-input">
                                <select id="smtpSecurity" name="smtpSecurity">
                                    <option value=""></option>
                                    <option value="TLS" <c:if test="${'TLS' == settingMap['smtpSecurity']}"> selected</c:if>><fmt:message key="console.setting.general.label.smtpSecurity.TLS"/></option>
                                    <option value="SSL" <c:if test="${'SSL' == settingMap['smtpSecurity']}"> selected</c:if>><fmt:message key="console.setting.general.label.smtpSecurity.SSL"/></option>
                                </select>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="smtpSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="smtpUsername"><fmt:message key="console.setting.general.label.smtpUsername"/></label>
                            <span class="form-input">
                                <input id="smtpUsername" type="text" name="smtpUsername" value="<c:out value="${settingMap['smtpUsername']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="smtpSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="smtpPassword"><fmt:message key="console.setting.general.label.smtpPassword"/></label>
                            <span class="form-input">
                                <input id="smtpPassword" type="password" name="smtpPassword" value="<c:out value="${settingMap['smtpPassword']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="smtpSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="smtpEmail"><fmt:message key="console.setting.general.label.smtpEmail"/></label>
                            <span class="form-input">
                                <input id="smtpEmail" type="text" name="smtpEmail" value="<c:out value="${settingMap['smtpEmail']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="smtpSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="smtpP12"><fmt:message key="app.emailtool.digitalSignature"/> <fmt:message key="app.emailtool.p12path"/></label>
                            <span class="form-input">
                                <input id="smtpP12" type="text" name="smtpP12" value="<c:out value="${settingMap['smtpP12']}"/>"/>
                                <i><span class="ftl_label"><fmt:message key="app.emailtool.p12path.desc"/></span></i>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="smtpSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="smtpStorepass"><fmt:message key="app.emailtool.digitalSignature"/> <fmt:message key="app.emailtool.storepass"/></label>
                            <span class="form-input">
                                <input id="smtpStorepass" type="password" name="smtpStorepass" value="<c:out value="${settingMap['smtpStorepass']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>
                <div class="main-body-row" data-header="smtpSettings">
                    <span class="row-content">
                        <div class="form-row">
                            <label for="smtpIssuerAlias"><fmt:message key="app.emailtool.digitalSignature"/> <fmt:message key="app.emailtool.issuerAlias"/></label>
                            <span class="form-input">
                                <input id="smtpIssuerAlias" type="text" name="smtpIssuerAlias" value="<c:out value="${settingMap['smtpIssuerAlias']}"/>"/>
                            </span>
                        </div>
                    </span>
                </div>
            </div>
            <div class="form-buttons">

                <button class="form-button console-primary" type="submit"><ui:msgEscHTML key="general.method.label.submit"/></button>
            </div>
            </form>
        </div>
    </div>
</div>

<script>
    //masterLoginHash
    var loginHashDeliminator = '<%= org.joget.directory.model.User.LOGIN_HASH_DELIMINATOR %>';
    if($('#masterLoginPassword').val() != '' && $('#masterLoginUsername').val() != ''){
	    getLoginHash($('#masterLoginUsername').val(), $('#masterLoginPassword').val());
    }
    $('#masterLoginUsername, #masterLoginPassword').keyup(function(){
        if($('#masterLoginPassword').val() != '' && $('#masterLoginUsername').val() != ''){
            getLoginHash($('#masterLoginUsername').val(), $('#masterLoginPassword').val());
        }else{
            $('#masterLoginHash').text("-");
        }
    });
    function getLoginHash(username, password) {
        var callback = {
            success : function(o) {
	            var o = eval("(" + o + ")");
                $('#masterLoginHash').text(o.hash);
            }
        }
        var params = "username=" + username + "&password=" + password;
        ConnectionManager.post('${pageContext.request.contextPath}/web/console/setting/general/loginHash', callback, params);
    }
    $(document).ready(function() {
        const savedMessage = localStorage.getItem('formSavedMessage');
        if (savedMessage) {
            $('<div class="toast"><i id="toast-icon" class="fas fa-check-circle"></i><div class="toast-body">'+ savedMessage +'</div><i id="close" class="fas fa-close"></i></div>').appendTo('div#main');
            $('.toast').css({
                'opacity': '0', 
            });
            $('.toast #toast-icon').css({
                "color":"var(--console-button-primary-bg)",
                "margin-right": ".5em", 
                "padding-right": ".5em",
                "border-right": "1px solid #ced4da"
            })
            $('.toast #close').css({"position": "absolute", "right": "0", "margin": "0 1em"});
            $('.toast #close').mouseenter(function(){
                $(this).css({'color': 'var(--console-button-primary-bg-hover)', 'cursor': 'pointer'});
            })
            $('.toast #close').mouseleave(function(){
                $(this).css({'color': 'initial', 'cursor': 'initial'});
            })
            $('.toast #close').click(function(){
                $(this).parent().remove();
            })
            let mainWidth = $('#main').outerWidth();
            let mainOffset = $('#main').offset();
            $('.toast').css({'position': 'absolute', 
            'top': mainOffset.top + 30 + 'px', 
            'left': (mainOffset.left + mainWidth / 2) + 'px', 
            'transform': 'translateX(-50%)',
            'height': 'calc-size(fit-content, 20px)',
            'padding': '10px 10px',
            'display': 'flex',
            'width': ( 0.5 * $("#main").width() ) +'px',
            'align-items': 'center',
            'background': '#e6f5e8',
            'border-radius': '10px',
            'color': '#000',
            'box-shadow': '0 4px 6px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0, 0, 0, 0.04)',
            'transition': 'opacity 0.5s ease-in-out'});

            if ($('body').hasClass('rtl')) {
                $('.toast #close').css({"left": "0", "right": "initial"});
                
                $('.toast #toast-icon').css({
                    "margin-left": ".5em", 
                    "padding-left": ".5em",
                    "border-left": "1px solid #ced4da",
                    "margin-right": "initial", 
                    "padding-right": "initial",
                    "border-right": "none"
                })
            }
            setTimeout(function() {
                $('.toast').css({
                    'opacity': '1', 
                });

                setTimeout(function() {
                    $('.toast').css({
                        'opacity': '0', // Fade out
                    });
                    setTimeout(function() {
                        $('.toast').remove();
                    }, 500);
                }, 2000);

            }, 100); 
            localStorage.removeItem('formSavedMessage');
        }

        // Override the form submission to validate the SMTP email
        $("#generalSettings").submit(function(event) {
            event.preventDefault(); // Prevent default form submission
            validateSMTPEmail();
            localStorage.setItem('formSavedMessage', '<fmt:message key="general.label.savedMessage"/>');
        });

        let elementsAfterFirstHeader = $('.main-body-content-subheader').eq(0).nextUntil('#header-container');
        //Insert the elements after the header-container
        elementsAfterFirstHeader.attr('data-header', 'system');
        elementsAfterFirstHeader.prependTo('div#content');
        //Insert the settings header to the header-container
        $('.main-body-content-subheader').eq(0).attr('id', 'system');
        $('.main-body-content-subheader').eq(0).prependTo('#header-container');

        //Header select logic
        $("div.main-body-content-subheader").off('click').on('click', function(){
            if ($(this).hasClass('selected')){
                return;
            }
            var headerId;
            //Remove current selected if exists
            const currentSelected = $('body').find("div.main-body-content-subheader.selected");
            if (currentSelected.length > 0){
                $(currentSelected).removeClass('selected');
                headerId = $(currentSelected).attr('id');
                $("div.main-body-row[data-header='"+ headerId +"']").hide();
            }

            headerId = $(this).attr('id');
            $(this).addClass('selected');
            $("div.main-body-row[data-header='"+ headerId +"']").show();
            $("div.main-body-row[data-header='"+ headerId +"']").last().css({'border': 'none'});
        })
        //Initialize the first subsection
        $('.main-body-content-subheader').eq(0).click();

        $("body").off('input', '#search-bar input').on('input', '#search-bar input', function(){
                var thisObj = this;
                const filteredResults = $(".main-body-row").filter(function(){
                    return $(this).find('label').text().toLowerCase().includes($(thisObj).val().toLowerCase());
                })
                if(filteredResults && $(thisObj).val() !== ''){
                    $("#search-results").empty();
                    filteredResults.each(function(){
                        let resultText = $(this).find('label').text();
                        let regex = new RegExp($(thisObj).val(), 'gi'); 

                        resultText = resultText.replace(regex, function(match) {
                            return `<span class="highlight">`+match+`</span>`;
                        });

                        $("#search-results").append(`<div class="result-item" data-header="`+ $(this).attr('data-header') +`"">`+ resultText +`</div>`);
                    })
                    $("#search-results").show();
                }else if ($(thisObj).val() === '') {
                    $("#search-results").hide();
                }
        })
        $("body").off('click', '#search-bar .result-item').on('click', '#search-bar .result-item', function(){
            const headerId = $(this).attr('data-header'); 
            const labelText = $(this).text(); 

            $("body").find('.main-body-content-subheader[id=' + headerId + ']').click();
            setTimeout(function() {
                const position = $("body").find('label:contains("' + labelText + '")').offset();
                
                if (position) {
                    const offsetMiddle = position.top - ($(window).height() / 2) + ($("body").find('label:contains("' + labelText + '")').outerHeight() / 2);

                    $('html, body').animate({
                        scrollTop: offsetMiddle
                    }, 500);
                }

                let element = $("body").find('label:contains("' + labelText + '")').parent();

                element.addClass("highlight");

                setTimeout(function() {
                    element.removeClass("highlight")
                }, 3000);

            }, 100);
        })
        $(this).on('click', function(event){
            if (event.target !== $("#search-bar") && $("#search-bar").has(event.target).length === 0){
                $("#search-bar").removeClass('active');
                $("#search-bar").find('#search-results').hide();
                $("#search-bar").find('input').val('');
            }
        })
    });

    function validateSMTPEmail() {
        var valid = true;
        var originalAppId = UI.userview_app_id;
        
        // Call the validateEmail function from ui.js
        UI.validateEmail('#smtpEmail', false, function(isValid) {
            if (!isValid) {
                try {
                    if (UI.userview_app_id === '') {
                        UI.userview_app_id = 'appcenter';
                    }
                    
                    UI.loadMsg(['app.edm.message.invalidEmailFormat'], function(messages) {
                        alert(messages['app.edm.message.invalidEmailFormat']);
                    });
                    valid = false;
                    UI.unblockUI();
                    
                } finally {
                    UI.userview_app_id = originalAppId;
                }
            }

            // Submit the form if everything is valid
            if (valid) {
                $("#generalSettings").off('submit').submit(); // Re-enable form submission and submit
            }
        });
    }
</script>

<script>
    Template.init("", "#nav-setting-general");
</script>

<commons:footer />
