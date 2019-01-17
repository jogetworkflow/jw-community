<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<commons:popupHeader/>
    <div id="main-body-header">
        <fmt:message key="console.app.export.label.title"/>
    </div>
    <div id="main-body-content">

        <form id="exportform" method="get" target="_blank" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/export" class="form blockui" enctype="multipart/form-data">
            
            <fieldset>
                <div class="form-row">                
                    <label for="exportplugins"><fmt:message key="console.app.export.label.exportplugins"/></label>
                    <div class="form-input">
                        <input id="exportplugins" type="checkbox" name="exportplugins" value="true"/>
                    </div>
                </div>
                <div class="form-row">
                    <label for="formdatas"><fmt:message key="console.app.export.label.formdatas"/></label>
                    <div class="form-input">
                        <c:forEach items="${tableNameList}" var="tablename">
                            <label>
                                <input type="checkbox" name="tablenames" value="${tablename}"/> ${tablename}
                            </label>
                        </c:forEach>
                    </div>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<fmt:message key="console.app.export.label"/>" />
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
        
        $(function(){
            $("#exportform").on("submit", function(){
                setTimeout(function(){
                    if (parent && parent.closeDialog) {
                        parent.closeDialog();
                    }
                }, 100);
                return true;
            });
        });
    </script>
                    
<commons:popupFooter />