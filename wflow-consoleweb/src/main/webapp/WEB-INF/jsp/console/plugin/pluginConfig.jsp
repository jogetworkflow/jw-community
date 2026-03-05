<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<jsp:useBean id="PropertyUtil" class="org.joget.plugin.property.service.PropertyUtil" scope="page"/>

<c:choose>
    <c:when test="${!empty param.__a_ and !empty param.__u_}">
        <commons:popupHeader bodyCssClass="pluginConfigPopup" builderTheme="${theme}" includeUserviewThemeCSS="${includeUserviewThemeCSS}"/>
    </c:when>
    <c:otherwise>
        <commons:popupHeader bodyCssClass="pluginConfigPopup" builderTheme="true"  includeUserviewThemeCSS="${includeUserviewThemeCSS}"/>
    </c:otherwise>    
</c:choose>    

<c:if test="${!empty propertyEditable}">
    <jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />
</c:if>

<div id="main-body-header">
    <c:choose>
        <c:when test="${!empty title}">
            <c:out value=" ${title}" escapeXml="true" />
        </c:when>
        <c:otherwise>
            <fmt:message key="console.plugin.label.pluginConfiguration"/> <ui:stripTag html=" ${param.title}"/>
        </c:otherwise>    
    </c:choose>
</div>

<div id="main-body-content" style="text-align: left;">
<c:if test="${!empty errors}">
    <span class="form-errors" style="display:block">
        <c:forEach items="${errors}" var="error">
            <fmt:message key="${error}"/>
        </c:forEach>
    </span>
</c:if>
<c:choose>
    <c:when test="${empty plugin}">
        <span class="form-errors" style="display:block"><fmt:message key="console.plugin.label.pluginNoProperties"/></span>
    </c:when>
    <c:otherwise>
        <div id="propertyEditor" class="pluginConfig menu-wizard-container">

        </div>
        <form id="propertiesForm" action="${actionUrl}" class="form blockui" method="POST" style="display:none">
            <input id="pluginProperties" name="pluginProperties" type="hidden" value=""/>
        </form>
        <script>
            function savePlugin(container, properties){
                $("#pluginProperties").val(JSON.encode(properties));
                $("#propertiesForm").submit();
            }

            function savePluginFailed(container, returnedErrors){
                var errorMsg = '<ui:msgEscJS key="console.plugin.label.youHaveFollowingErrors"/>';
                let errorMsgList = ''; 
                for(key in returnedErrors){
                    if (returnedErrors[key].fieldName === undefined || returnedErrors[key].fieldName === "") {
                        const errors = returnedErrors[key].message ;
                        errorMsgList += '<li>' + errors + '</li>';
                    } else {
                        const errors = returnedErrors[key].fieldName + ' : ' + returnedErrors[key].message;
                        errorMsgList += '<li>' + errors + '</li>';
                    }
                }
                UI.alert(errorMsgList, {
                    isHtml : true,
                    title: errorMsg,
                    icon: 'error'
                })           
            }
            
            function cancel(container){
                document.location = "${cancelUrl}";
            }

            $(document).ready(function(){
                var options = {
                    contextPath: '${pageContext.request.contextPath}',
                    <c:if test="${!empty appDef}">
                        appPath: '/${appDef.appId}/${appDef.version}',
                    </c:if>
                    <c:choose>
                        <c:when test="${!empty propertiesDefinition}">
                            propertiesDefinition : ${propertiesDefinition},
                        </c:when>
                        <c:when test="${!empty propertyEditable.propertyOptions}">
                            propertiesDefinition : ${PropertyUtil.injectHelpLink(plugin.helpLink, propertyEditable.propertyOptions)},
                        </c:when>
                    </c:choose>
                    <c:choose>
                        <c:when test="${!empty properties && fn:substring(properties, 0, 1) eq '{'}">
                            propertyValues : ${properties},
                        </c:when>
                    </c:choose>
                    <c:if test="${!empty defaultProperties && fn:substring(defaultProperties, 0, 1) eq '{'}">
                        defaultPropertyValues : ${defaultProperties},
                    </c:if>
                    <c:if test="${!empty skipValidation && skipValidation}">
                        skipValidation : ${skipValidation},
                    </c:if>
                    <c:choose>
                        <c:when test="${!empty cancelUrl}">
                            cancelCallback: cancel,
                            showCancelButton: true,        
                        </c:when>
                        <c:otherwise>
                            showCancelButton: false,
                        </c:otherwise>    
                    </c:choose>
                    helplink: '${plugin.helpLink}',    
                    saveCallback: savePlugin,
                    saveButtonLabel: '<c:choose><c:when test="${!empty submitLabel}"><ui:msgEscJS key="${submitLabel}"/></c:when><c:otherwise><ui:msgEscJS key="general.method.label.submit"/></c:otherwise></c:choose>',
                    cancelButtonLabel: '<c:choose><c:when test="${!empty cancelLabel}"><ui:msgEscJS key="${cancelLabel}"/></c:when><c:otherwise><ui:msgEscJS key="general.method.label.cancel"/></c:otherwise></c:choose>',
                    closeAfterSaved: false,
                    validationFailedCallback: savePluginFailed
                }
                $('.menu-wizard-container').propertyEditor(options);
            });
        </script>
    </c:otherwise>
</c:choose>

</div>

<commons:popupFooter />