<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="java.io.File,org.joget.commons.util.SetupManager"%>

<commons:header />
<style>
    .row-content{
        display: block;
        float: none;
    }

    .form-input{
        width: 50%
    }

    .form-input input, .form-input textarea{
        width: 100%
    }

    .row-title{
        font-weight: bold;
    }
</style>
<div id="nav">
    <div id="nav-title">

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
            <form method="post" action="${pageContext.request.contextPath}/web/console/setting/general/submit">
            <div class="main-body-content-subheader">
                <span><fmt:message key="console.setting.general.header.uiSetting"/></span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="css"><fmt:message key="console.setting.general.label.css"/></label>
                        <span class="form-input">
                            <input id="css" type="text" name="css" value="${settingMap['css']}"/>
                            <i><span><fmt:message key="console.setting.general.label.default"/></span> ${pageContext.request.contextPath}/css/new.css</i>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="customCss"><fmt:message key="console.setting.general.label.customCss"/></label>
                        <span class="form-input">
                            <textarea rows="15" id="customCss" type="text" name="customCss">${settingMap['customCss']}</textarea>
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
                                <option></option>
                                <c:forEach var="locale" items="${localeList}">
                                    <c:set var="selected"><c:if test="${locale == settingMap['systemLocale']}"> selected</c:if></c:set>
                                    <option ${selected}>${locale}</option>
                                </c:forEach>
                            </select>
                            <br>
                            <i><span><fmt:message key="console.setting.general.label.default"/></span> en_US</i>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="rightToLeft"><fmt:message key="console.setting.general.label.rightToLeft"/></label>
                        <c:set var="checked"></c:set>
                        <c:if test="${settingMap['rightToLeft'] == 'true'}">
                            <c:set var="checked">checked</c:set>
                        </c:if>
                        <input type="checkbox" id="rightToLeft" name="rightToLeft" ${checked} />
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
                            <input id="deadlineCheckerInterval" type="text" name="deadlineCheckerInterval" value="${settingMap['deadlineCheckerInterval']}"/>
                            <i><span><fmt:message key="console.setting.general.label.default"/></span> 0</i>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="welcomeMessageDelay"><fmt:message key="console.setting.general.label.welcomeMessageDelay"/></label>
                        <span class="form-input">
                            <input id="welcomeMessageDelay" type="text" name="welcomeMessageDelay" value="${settingMap['welcomeMessageDelay']}"/>
                            <i><span><fmt:message key="console.setting.general.label.default"/></span> 3</i>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-content-subheader">
                <span><fmt:message key="console.setting.general.header.saSettings"/></span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="masterLoginUsername"><fmt:message key="console.setting.general.label.masterLoginUsername"/></label>
                        <span class="form-input">
                            <input id="masterLoginUsername" type="text" name="masterLoginUsername" value="${settingMap['masterLoginUsername']}"/>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="masterLoginPassword"><fmt:message key="console.setting.general.label.masterLoginPassword"/></label>
                        <span class="form-input">
                            <input id="masterLoginPassword" type="password" name="masterLoginPassword" value="${settingMap['masterLoginPassword']}"/>
                            <i><fmt:message key="console.setting.general.label.masterLoginHash"/><span id="masterLoginHash">-</span></i>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="dataFileBasePath"><fmt:message key="console.setting.general.label.dataFileBasePath"/></label>
                        <span class="form-input">
                            <input id="dataFileBasePath" type="text" name="dataFileBasePath" value="${settingMap['dataFileBasePath']}"/>
                            <i><span><fmt:message key="console.setting.general.label.default"/></span> <%= new java.io.File(SetupManager.getBaseDirectory()).getAbsolutePath() %></i>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="designerwebBaseUrl"><fmt:message key="console.setting.general.label.designerwebBaseUrl"/></label>
                        <span class="form-input">
                            <input id="designerwebBaseUrl" type="text" name="designerwebBaseUrl" value="${settingMap['designerwebBaseUrl']}"/>
                            <i><span><fmt:message key="console.setting.general.label.default"/></span> http://${pageContext.request.serverName}:${pageContext.request.serverPort}</i>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="deleteProcessOnCompletion"><fmt:message key="console.setting.general.label.deleteProcessOnCompletion"/></label>
                        <c:set var="checked"></c:set>
                        <c:if test="${settingMap['deleteProcessOnCompletion'] == 'true'}">
                            <c:set var="checked">checked</c:set>
                        </c:if>
                        <input type="checkbox" id="deleteProcessOnCompletion" name="deleteProcessOnCompletion" ${checked} />
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="mediumWarningLevel"><fmt:message key="console.setting.general.label.mediumWarningLevel"/></label>
                        <span class="form-input">
                            <input id="mediumWarningLevel" type="text" name="mediumWarningLevel" value="${settingMap['mediumWarningLevel']}"/>
                            <i><span><fmt:message key="console.setting.general.label.default"/></span> 20</i>
                        </span>
                    </div>
                </span>
            </div>
            <div class="main-body-row">
                <span class="row-content">
                    <div class="form-row">
                        <label for="criticalWarningLevel"><fmt:message key="console.setting.general.label.criticalWarningLevel"/></label>
                        <span class="form-input">
                            <input id="criticalWarningLevel" type="text" name="criticalWarningLevel" value="${settingMap['criticalWarningLevel']}"/>
                            <i><span><fmt:message key="console.setting.general.label.default"/></span> 50</i>
                        </span>
                    </div>
                </span>
            </div>
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<fmt:message key="general.method.label.submit"/>" />
            </div>
            </form>
        </div>
    </div>
</div>

<script>
    //masterLoginHash
    var loginHashDeliminator = '<%= org.joget.directory.model.User.LOGIN_HASH_DELIMINATOR %>';
    if($('#masterLoginPassword').val() != '' && $('#masterLoginUsername').val() != ''){
        $('#masterLoginHash').text($.md5($('#masterLoginUsername').val() + loginHashDeliminator + $('#masterLoginPassword').val()));
    }
    $('#masterLoginUsername, #masterLoginPassword').keyup(function(){
        if($('#masterLoginPassword').val() != '' && $('#masterLoginUsername').val() != ''){
            $('#masterLoginHash').text($.md5($('#masterLoginUsername').val() + loginHashDeliminator + $('#masterLoginPassword').val()));
        }else{
            $('#masterLoginHash').text("-");
        }
    });
</script>

<script>
    Template.init("", "#nav-setting-general");
</script>

<commons:footer />
