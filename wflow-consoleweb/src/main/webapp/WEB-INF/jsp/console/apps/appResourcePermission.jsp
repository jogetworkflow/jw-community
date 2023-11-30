<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<jsp:useBean id="PropertyUtil" class="org.joget.plugin.property.service.PropertyUtil" scope="page"/>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<c:if test="${not empty theme and theme ne 'classic'}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
</c:if>

<commons:popupHeader bodyCssClass=" builder-popup" builderTheme="${theme}"/>

<jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />

<div id="main-body-header">
    <fmt:message key="console.app.resources.edit.label"/>
</div>

<div id="main-body-content" style="text-align: left;">
<c:if test="${upload}">
    <div class="form-message form-success"><fmt:message key="console.app.resources.added" /></div>
</c:if>
<c:if test="${!empty errors}">
    <span class="form-errors" style="display:block">
        <c:forEach items="${errors}" var="error">
            <fmt:message key="${error}"/>
        </c:forEach>
    </span>
</c:if>

    <div id="propertyEditor" class="pluginConfig menu-wizard-container">

    </div>
    <form id="propertiesForm" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/resource/permission/submit" class="form blockui" method="POST" style="display:none">
        <input id="permissionProperties" name="permissionProperties" type="hidden" value=""/>
        <input id="id" name="id" type="hidden" value="${appResource.id}"/>
    </form>
    <script>
        function saveProperties(container, properties){
            delete properties['filename'];
            delete properties['url'];
            delete properties['filesize'];
            $("#permissionProperties").val(JSON.encode(properties));
            $("#propertiesForm").submit();
        }

        function savePropertiesFailed(container, returnedErrors){
            var errorMsg = '<ui:msgEscJS key="console.plugin.label.youHaveFollowingErrors"/>:\n';
            for(key in returnedErrors){
                if (returnedErrors[key].fieldName === undefined || returnedErrors[key].fieldName === "") {
                    errorMsg += returnedErrors[key].message + '\n';
                } else {
                    errorMsg += returnedErrors[key].fieldName + ' : ' + returnedErrors[key].message + '\n';
                }
            }
            alert(errorMsg);
        }

        function cancel(container){
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }

        $(document).ready(function(){
            var prop = ${properties};
            prop['filename'] = "${appResource.id}";
            prop['url'] = "${pageContext.request.contextPath}/web/app/${appId}/resources/${appResource.id}";
            prop['hashvariable'] = "#appResource.${appResource.id}#";
            prop['filesize'] = "${appResource.filesizeString}";
            
            var options = {
                contextPath: '${pageContext.request.contextPath}',
                propertiesDefinition : [{
                    title : '<ui:msgEscJS key="console.app.resources.detail.label"/>',
                    properties : [{
                        name : 'filename',
                        label : '<ui:msgEscJS key="console.app.resource.common.label.id"/>',
                        type : 'label',
                        url : '${pageContext.request.contextPath}/web/app/${appId}/resources/${appResource.id}'
                    },
                    {
                        name : 'url',
                        label : '<ui:msgEscJS key="console.app.resource.common.label.url"/>',
                        type : 'readonly'
                    },
                    {
                        name : 'hashvariable',
                        label : '<ui:msgEscJS key="setting.plugin.hashVariable"/>',
                        type : 'readonly'
                    },
                    {
                        name : 'filesize',
                        label : '<ui:msgEscJS key="console.app.resource.common.label.filesize"/>',
                        type : 'label'
                    },
                    { 
                        name : 'permission',
                        label : '<ui:msgEscJS key="console.app.resource.common.label.permission"/>',
                        type : 'elementselect',
                        options_ajax : '[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.userview.model.UserviewPermission',
                        url : '[CONTEXT_PATH]/web/property/json/${appId}/${appVersion}/getPropertyOptions'
                    }]
                }],
                propertyValues : prop,
                cancelCallback: cancel,
                showCancelButton: true,          
                saveCallback: saveProperties,
                saveButtonLabel: '<c:choose><c:when test="${!empty submitLabel}"><ui:msgEscJS key="${submitLabel}"/></c:when><c:otherwise><ui:msgEscJS key="general.method.label.submit"/></c:otherwise></c:choose>',
                cancelButtonLabel: '<ui:msgEscJS key="general.method.label.cancel"/>',
                closeAfterSaved: false,
                validationFailedCallback: savePropertiesFailed
            }
            $('.menu-wizard-container').propertyEditor(options);
        });
    </script>

</div>
<commons:popupFooter />