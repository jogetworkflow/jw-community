<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<commons:popupHeader/>
    <div id="main-body-header">
        <fmt:message key="console.app.import.label.title"/>
    </div>
    <div id="main-body-content">

        <form method="post" action="${pageContext.request.contextPath}/web/console/app/import/submit" class="form" enctype="multipart/form-data">
            
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
                <legend><fmt:message key="console.app.import.label.title"/></legend>
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
                                <input id="overrideEnvVariable" type="checkbox" name="overrideEnvVariable" value="true"/>
                                <fmt:message key="console.app.import.label.overrideEnvVariable"/>
                            </label>
                        </span>
                    </div>
                    <div class="form-row">                
                        <span class="form-input">
                            <label for="overridePluginDefault" class="upload">
                                <input id="overridePluginDefault" type="checkbox" name="overridePluginDefault" value="true"/>
                                <fmt:message key="console.app.import.label.overridePluginDefault"/>
                            </label>
                        </span>
                    </div>
                </div>
                <div class="form-buttons">
                    <input class="form-button" type="submit" value="<fmt:message key="general.method.label.upload"/>" />
                </div>
            </fieldset>
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