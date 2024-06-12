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

    .form-input{
        width: 50%
    }

    .form-input input:not([type=checkbox]), .form-input textarea{
        width: 100%
    }

    .row-title{
        font-weight: bold;
    }
</style>
<div id="nav">
    <div id="nav-title">
        <p><i class="fas fa-cogs"></i> <fmt:message key='console.header.top.label.settings'/></p>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <jsp:include page="subMenu.jsp" flush="true" />
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
    </div>
    <div id="main-body">
        <div id="generalSetup">
            <form method="post" class="blockui" action="${pageContext.request.contextPath}/web/console/setting/general/submit">
            <jsp:include page="/web/json/plugin/org.joget.apps.ext.ConsoleWebPlugin/service?spot=settings" />
            <div class="main-body-content-subheader">
                <span><fmt:message key="console.setting.general.header.uiSetting"/></span>
            </div>
            <div class="main-body-row">
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
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="systemTheme"><fmt:message key="console.setting.general.label.system.theme"/></label>
                        <span class="form-input">
                            <select id="systemTheme" name="systemTheme">
                                <option value="classic" <c:if test="${settingMap['systemTheme'] eq 'classic'}">selected</c:if>><fmt:message key="console.setting.general.label.classic.theme"/></option>
                                <option value="light" <c:if test="${settingMap['systemTheme'] eq 'light'}">selected</c:if>><fmt:message key="console.setting.general.label.light.theme"/></option>
                                <option value="dark" <c:if test="${settingMap['systemTheme'] eq 'dark'}">selected</c:if>><fmt:message key="console.setting.general.label.dark.theme"/></option>
                            </select>
                        </span>
                    </div>
                </span>
            </div> 
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="userLocale"><fmt:message key="console.setting.general.label.userLocale"/></label>
                        <span class="form-input">
                            <input id="userLocale" type="text" name="userLocale" value="<c:out value="${settingMap['userLocale']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="customCss"><fmt:message key="console.setting.general.label.customCss"/></label>
                        <span class="form-input">
                            <textarea rows="15" id="customCss" type="text" name="customCss"><c:out value="${settingMap['customCss']}"/></textarea>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
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
            <div class="main-body-content-subheader">
                <span><fmt:message key="console.setting.general.header.timeSettings"/></span>
            </div>
            <div class="main-body-row">
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
                <div class="main-body-row">
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
            <div class="main-body-content-subheader">
                <span><fmt:message key="console.setting.general.header.saSettings"/></span>
            </div>
            <c:if test="${!userSecurity.disableHashLogin}">
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="masterLoginUsername"><fmt:message key="console.setting.general.label.masterLoginUsername"/></label>
                        <span class="form-input">
                            <input id="masterLoginUsername" type="text" name="masterLoginUsername" value="<c:out value="${settingMap['masterLoginUsername']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="jsonpWhitelist"><fmt:message key="console.setting.general.label.jsonpWhitelist"/></label>
                        <span class="form-input">
                            <input id="jsonpWhitelist" type="text" name="jsonpWhitelist" value="<c:out value="${settingMap['jsonpWhitelist']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
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
                <div class="main-body-row">
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
            <div class="main-body-row">
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
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="environmentName"><fmt:message key="console.setting.general.label.environmentName"/></label>
                        <span class="form-input">
                            <input id="environmentName" type="text" name="environmentName" value="<c:out value="${settingMap['environmentName']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>                
            <div class="main-body-content-subheader">
                <span><fmt:message key="console.setting.general.header.smtpSettings"/></span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="smtpHost"><fmt:message key="console.setting.general.label.smtpHost"/></label>
                        <span class="form-input">
                            <input id="smtpHost" type="text" name="smtpHost" value="<c:out value="${settingMap['smtpHost']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="smtpPort"><fmt:message key="console.setting.general.label.smtpPort"/></label>
                        <span class="form-input">
                            <input id="smtpPort" type="text" name="smtpPort" value="<c:out value="${settingMap['smtpPort']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
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
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="smtpUsername"><fmt:message key="console.setting.general.label.smtpUsername"/></label>
                        <span class="form-input">
                            <input id="smtpUsername" type="text" name="smtpUsername" value="<c:out value="${settingMap['smtpUsername']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="smtpPassword"><fmt:message key="console.setting.general.label.smtpPassword"/></label>
                        <span class="form-input">
                            <input id="smtpPassword" type="password" name="smtpPassword" value="<c:out value="${settingMap['smtpPassword']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="smtpEmail"><fmt:message key="console.setting.general.label.smtpEmail"/></label>
                        <span class="form-input">
                            <input id="smtpEmail" type="text" name="smtpEmail" value="<c:out value="${settingMap['smtpEmail']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
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
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="smtpStorepass"><fmt:message key="app.emailtool.digitalSignature"/> <fmt:message key="app.emailtool.storepass"/></label>
                        <span class="form-input">
                            <input id="smtpStorepass" type="password" name="smtpStorepass" value="<c:out value="${settingMap['smtpStorepass']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="smtpIssuerAlias"><fmt:message key="app.emailtool.digitalSignature"/> <fmt:message key="app.emailtool.issuerAlias"/></label>
                        <span class="form-input">
                            <input id="smtpIssuerAlias" type="text" name="smtpIssuerAlias" value="<c:out value="${settingMap['smtpIssuerAlias']}"/>"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<ui:msgEscHTML key="general.method.label.submit"/>" />
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
</script>

<script>
    Template.init("", "#nav-setting-general");
</script>

<commons:footer />
