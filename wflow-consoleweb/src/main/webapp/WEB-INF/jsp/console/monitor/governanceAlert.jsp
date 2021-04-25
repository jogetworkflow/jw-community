<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<jsp:useBean id="PropertyUtil" class="org.joget.plugin.property.service.PropertyUtil" scope="page"/>

<commons:popupHeader/>

<jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />

<div id="main-body-header">
    <fmt:message key="console.governance.manageAlert"/>
</div>

<div id="main-body-content" style="text-align: left;">
    <div id="propertyEditor" class="pluginConfig menu-wizard-container">

    </div>
    <form id="propertiesForm" action="${pageContext.request.contextPath}/web/console/monitor/governance/alert/submit" class="form blockui" method="POST" style="display:none">
        <input id="properties" name="properties" type="hidden" value=""/>
    </form>
    <script>
        function saveProperties(container, properties){
            $("#properties").val(JSON.encode(properties));
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
            var prop = ${alertProp};
            if (prop.subject === undefined) {
                prop.subject = '<ui:msgEscJS key="console.governance.alertSubject"/>';
            }
            
            var options = {
                contextPath: '${pageContext.request.contextPath}',
                propertiesDefinition : [{
                    title : '<ui:msgEscJS key="console.governance.alertDetails"/>',
                    properties : [
                        {
                            name : 'toSpecific',
                            label : '<ui:msgEscJS key="app.emailtool.toEmail"/>',
                            type : 'textfield'
                        },
                        {
                            name : 'cc',
                            label : '<ui:msgEscJS key="app.emailtool.cc"/>',
                            type : 'textfield'
                        },
                        {
                            name : 'bcc',
                            label : '<ui:msgEscJS key="app.emailtool.bcc"/>',
                            type : 'textfield'
                        },
                        {
                            name : 'subject',
                            label : '<ui:msgEscJS key="app.emailtool.subject"/>',
                            type : 'textfield'
                        }
                    ]
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