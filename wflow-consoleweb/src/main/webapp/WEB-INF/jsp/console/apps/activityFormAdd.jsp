<%@page import="org.joget.apps.app.model.PackageActivityForm"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />
<style>
    #externalFormIFrameStyle{
        width: 360px;
        height: 150px;
    }
</style>

    <div id="main-body-header">
        <fmt:message key="console.process.config.label.mapActivities"/> - <ui:stripTag html="${param.activityName}"/> <c:out value="(${activity.id})" escapeXml="true" />
    </div>

    <div id="main-body-content" style="text-align: left">
        <div id="formTabView">
            <ul>
                <li class="selected"><a href="#formList"><span><fmt:message key="console.process.config.label.mapActivities.form"/></span></a></li>
                <li><a href="#externalForm"><span><fmt:message key="console.process.config.label.mapActivities.formExternal"/></span></a></li>
            </ul>
            <div>
                <div id="formList">
                    <br/>
                    <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/forms?${pageContext.request.queryString}"
                                   var="JsonDataTable"
                                   divToUpdate="formList2"
                                   jsonData="data"
                                   rowsPerPage="10"
                                   width="100%"
                                   sort="name"
                                   desc="false"
                                   href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/${activity.id}/form/submit"
                                   hrefParam="id"
                                   hrefQuery="true"
                                   hrefDialog="false"
                                   hrefDialogWidth="600px"
                                   hrefDialogHeight="400px"
                                   hrefDialogTitle="Form Dialog"
                                   hrefPost="true"
                                   checkboxButton1="console.form.create.label"
                                   checkboxCallback1="createForm"
                                   checkboxOptional1="true"
                                   searchItems="name|Form Name"
                                   fields="['id','name','dateCreated','dateModified']"
                                   column1="{key: 'name', label: 'console.form.common.label.name', sortable: true}"
                                   column2="{key: 'tableName', label: 'console.form.common.label.tableName', sortable: false}"
                                   column3="{key: 'dateCreated', label: 'console.form.common.label.dateCreated', sortable: false}"
                                   column4="{key: 'dateModified', label: 'console.form.common.label.dateModified', sortable: false}"
                                   />
                </div>

                <div id="externalForm">
                    <br>
                    <form method="post" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/processes/${process.encodedId}/activity/${activity.id}/form/submit" class="form">
                        <input type="hidden" name="type" value="<%= PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL %>"/>
                        <input type="hidden" name="activityDefId" value="${activity.id}"/>
                        <input type="hidden" name="processDefId" value="${process.id}"/>
                        
                        <div class="form-row">
                            <label for="externalFormUrl"><fmt:message key="console.process.config.label.mapActivities.formExternal.url"/></label>
                            <span class="form-input">
                                <input id="externalFormUrl" type="text" name="externalFormUrl" size="60" value="<c:out value="${externalFormUrl}"/>"/>
                            </span>
                        </div>
                        <div class="form-row">
                            <label for="externalFormIFrameStyle"><fmt:message key="console.process.config.label.mapActivities.formExternal.iframeStyle"/></label>
                            <span class="form-input">
                                <textarea id="externalFormIFrameStyle" name="externalFormIFrameStyle"><c:out value="${externalFormIFrameStyle}"/></textarea>
                            </span>
                        </div>
                        <div class="form-buttons">
                            <input class="form-button" type="submit" value="<fmt:message key="general.method.label.submit"/>" />
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script>
        $(document).ready(function(){
            $('#JsonDataTable_searchTerm').hide();
        });
    
        var tabView = new TabView('formTabView', 'top');
        tabView.init();

        <c:if test="${param.activityId == 'runProcess'}">
            tabView.disable(1);
        </c:if>

        if("${externalFormUrl}" != ""){
            tabView.select(1);
        }
        
        function filter(jsonTable, url, value){
            var newUrl = url + value;
            jsonTable.load(jsonTable.url + newUrl);
        }

        function createForm(){
            document.location = "${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/form/create?activityDefId=${activity.id}&processDefId=${process.encodedId}&redirect=false";
        }

        function sel(formId){
            var callback = {
                success : function() {
                    document.location.reload(true);
                }
            }
            var params = "formId=" + formId + "&activityDefId=${activity.id}&processDefId=${process.encodedId}&version=${process.version}";
            ConnectionManager.post('${pageContext.request.contextPath}/web/admin/process/activity/form/add', callback, params); 
        }
    </script>
<commons:popupFooter />
