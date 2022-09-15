<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<commons:popupHeader/>
    <div id="main-body-header">
        <fmt:message key="console.app.import.label.title"/>
    </div>
    <div id="main-body-content">

        <form method="post" action="${pageContext.request.contextPath}/web/console/app/import/submit" class="form blockui" enctype="multipart/form-data">
            
            <c:if test="${errorList != null || error}">
                <div class="form-errors" style="display:block">
                    <c:if test="${error}">
                        <div><fmt:message key="console.app.import.error.pleaseCheckAndTryAgain"/></div>
                    </c:if>
                    <c:forEach var="error" items="${errorList}">
                        <div>${error}</div>
                    </c:forEach>
                </div>
            </c:if>
            
            <fieldset>
                <div class="form-row">
                    <label for="appZip" class="upload"><fmt:message key="console.app.import.label.selectFile"/></label>
                    <span class="form-input">
                        <input id="appZip" type="file" name="appZip"/>
                    </span>
                </div>
                <div class="form-row">
                    <a href="#" id="showAdvancedInfo" onclick="showAdvancedInfo();return false"><fmt:message key="console.app.import.label.showAdvancedOptions"/></a>
                    <a href="#" style="display: none" id="hideAdvancedInfo" onclick="hideAdvancedInfo();return false"><fmt:message key="console.app.import.label.hideAdvancedOptions"/></a>
                </div>
                <div id="advancedView" style="display:none">
                    <div class="form-row">                
                        <span class="form-input">
                            <label for="overrideEnvVariable">
                                <input id="overrideEnvVariable" type="checkbox" name="overrideEnvVariable" value="true"/><i></i>
                                <fmt:message key="console.app.import.label.overrideEnvVariable"/>
                            </label>
                        </span>
                    </div>
                    <div class="form-row">                
                        <span class="form-input">
                            <label for="overridePluginDefault" class="upload">
                                <input id="overridePluginDefault" type="checkbox" name="overridePluginDefault" value="true"/><i></i>
                                <fmt:message key="console.app.import.label.overridePluginDefault"/>
                            </label>
                        </span>
                    </div>
                    <div class="form-row">                
                        <span class="form-input">
                            <label for="doNotImportParticipant" class="upload">
                                <input id="doNotImportParticipant" type="checkbox" name="doNotImportParticipant" value="true"/><i></i>
                                <fmt:message key="console.app.import.label.doNotImportParticipant"/>
                            </label>
                        </span>
                    </div>
                    <div class="form-row">                
                        <span class="form-input">
                            <label for="doNotImportTool" class="upload">
                                <input id="doNotImportTool" type="checkbox" name="doNotImportTool" value="true"/><i></i>
                                <fmt:message key="console.app.import.label.doNotImportTool"/>
                            </label>
                        </span>
                    </div>
                    <div class="form-row">                
                        <span class="form-input">
                            <label for="doNotImportPlugins" class="upload">
                                <input id="doNotImportPlugins" type="checkbox" name="doNotImportPlugins" value="true"/><i></i>
                                <fmt:message key="console.app.import.label.doNotImportPlugins"/>
                            </label>
                        </span>
                    </div>
                    <div class="form-row">                
                        <span class="form-input">
                            <label for="doNotImportFormDatas" class="upload">
                                <input id="doNotImportFormDatas" type="checkbox" name="doNotImportFormDatas" value="true"/><i></i>
                                <fmt:message key="console.app.import.label.doNotImportFormDatas"/>
                            </label>
                        </span>
                    </div>
                    <div class="form-row">                
                        <span class="form-input">
                            <label for="doNotImportUserGroups" class="upload">
                                <input id="doNotImportUserGroups" type="checkbox" name="doNotImportUserGroups" value="true"/><i></i>
                                <fmt:message key="console.app.import.label.doNotImportUserGroups"/>
                            </label>
                        </span>
                    </div>        
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button btn btn-primary" type="submit" value="<ui:msgEscHTML key="general.method.label.upload"/>" />
            </div>
        </form>
    </div>

    <script>
        function showAdvancedInfo(){
            $('#advancedView').slideToggle('slow');
            $('#showAdvancedInfo').hide();
            $('#hideAdvancedInfo').show();
        }

        function hideAdvancedInfo(){
            $('#advancedView').slideToggle('slow');
            $('#showAdvancedInfo').show();
            $('#hideAdvancedInfo').hide();
        }
    </script>
                    
<commons:popupFooter />