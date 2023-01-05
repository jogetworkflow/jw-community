<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.commons.util.SetupManager"%>
<%@ page import="org.joget.apps.app.service.AppDevUtil"%>

<c:set var="isSecureMode" value="<%= SetupManager.isSecureMode() %>"/>

<commons:popupHeader bodyCssClass="builder-popup" />
    <div id="main-body-header">
        <fmt:message key="console.app.export.label.title"/>
    </div>
    <div id="main-body-content">

        <form id="exportform" method="get" target="_blank" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/export" class="form blockui" enctype="multipart/form-data">
            
            <fieldset>  
                <c:set var="pluginList" value="<%= AppDevUtil.getPluginJarList(null) %>"/>
                <c:if test="${!empty(pluginList)}"> 
                    <div class="alert alert-warning">
                        <fmt:message key="console.app.export.label.exportpluginsDisabled"/>
                        <ul>
                            <c:forEach items="${pluginList}" var="plugin">
                                <li>${plugin}</li>
                            </c:forEach>
                        </ul>  
                    </div>    
                </c:if>  
                <c:if test="${!isSecureMode}">   
                    <div class="form-row">                
                        <label for="exportplugins"><fmt:message key="console.app.export.label.exportplugins"/></label>
                        <div class="form-input">
                            <input id="exportplugins" type="checkbox" name="exportplugins" value="true"/>
                        </div>
                    </div>
                </c:if>
                <div class="form-row">
                    <label for="formdatas"><fmt:message key="console.app.export.label.formdatas"/></label>
                    <div class="form-input">
                        <label>
                            <input type="checkbox" class="toggleAll"/> <strong><fmt:message key="console.app.export.label.checkAll"/></strong>
                        </label>
                        <c:forEach items="${tableNameList}" var="tablename">
                            <label>
                                <input type="checkbox" name="tablenames" value="${tablename}"/> ${tablename}
                            </label>
                        </c:forEach>
                    </div>
                </div>
                <c:if test="${!empty(userGroups)}">       
                    <div class="form-row">
                        <label for="userGroups"><fmt:message key="console.app.export.label.userGroups"/></label>
                        <div class="form-input">
                            <label>
                                <input type="checkbox" class="toggleAll"/> <strong><fmt:message key="console.app.export.label.checkAll"/></strong>
                            </label>
                            <c:forEach items="${userGroups}" var="group">
                                <label>
                                    <input type="checkbox" name="usergroups" value="${group.id}"/> ${group.name}
                                </label>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>    
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<ui:msgEscHTML key="console.app.export.label"/>" />
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
                    if (parent && parent.CustomBuilder) {
                        parent.JPopup.hide("exportAppDialog");
                    }
                }, 100);
                return true;
            });
            
            $("#exportform .toggleAll").off("change");
            $("#exportform .toggleAll").on("change", function(){
                var checked = $(this).is(':checked');
                $(this).closest(".form-input").find("input[name]").each(function(){
                    if (checked) {
                        $(this).prop("checked", true);
                    } else {
                        $(this).prop("checked", false);
                    }
                });
            });
        });
    </script>
                    
<commons:popupFooter />