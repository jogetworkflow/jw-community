<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<jsp:useBean id="PropertyUtil" class="org.joget.plugin.property.service.PropertyUtil" scope="page"/>

<c:set var="appDef" scope="request" value="${appDefinition}"/>
<c:set var="builderLabel" scope="request"><fmt:message key="ubuilder.title"/></c:set>
<c:set var="builderI18N" scope="request" value=""/>
<c:set var="builderJS" scope="request">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/ubuilder.core.js?build=<fmt:message key="build.number"/>"></script>
    <script>
        $(function () {
            <c:set var="propertyOptions" value="${category.propertyOptions}"/>
            <c:if test="${empty propertyOptions}">
                <c:set var="propertyOptions" value="''"/>
            </c:if>
            try {
                <c:set var="initScript"> 
                    CustomBuilder.initPaletteElement('', '${category.className}', '<c:out value='${fn:replace(category.label, "\'", "\\\\\'")}' escapeXml='false'/>', '<i class="far fa-folder"></i>', ${propertyOptions}, '', false); 
                </c:set>
                <c:set var="initScript"><ui:escape value="${initScript}" format="javascript"/></c:set>
                eval("${initScript}");    
            } catch (err) {
                if (console && console.log) {
                    console.log("Error initializing ${element.className} : " + err);
                }
            }
        
        <c:forEach items="${simplePageComponent}" var="element">
            <c:set var="propertyOptions" value="${element.propertyOptions}"/>
            <c:set var="template" value="${element.builderJavaScriptTemplate}"/>
            <c:if test="${empty propertyOptions}">
                <c:set var="propertyOptions" value="''"/>
            </c:if>
            try {
                <c:set var="initScript">
                    CustomBuilder.initPaletteElement('Basic', '${element.className}', '<c:out value='${fn:replace(element.i18nLabel, "\'", "\\\\\'")}' escapeXml='false'/>', '<ui:escape value='${element.icon}' format='javascript'/>', ${propertyOptions}, '<ui:escape value='${element.defaultPropertyValues}' format='javascript'/>', !${element.isHiddenPlugin()}, "", {'list_css' : 'component', 'developer_mode' : '<ui:escape value='${element.developerMode}' format='javascript'/>', 'type' : 'component', 'builderTemplate' : ${template}}); 
                </c:set>
                <c:set var="initScript"><ui:escape value="${initScript}" format="javascript"/></c:set>
                eval("${initScript}");    
            } catch (err) {
                if (console && console.log) {
                    console.log("Error initializing ${element.className} : " + err);
                }
            }
        </c:forEach>
        
        <c:forEach items="${menuTypeCategories}" var="categoryRow">
            <c:set var="category" value="${categoryRow.key}"/>
            <c:set var="elementList" value="${categoryRow.value}"/>
            <c:forEach items="${elementList}" var="element">
                <c:set var="propertyOptions" value="${element.propertyOptions}"/>
                <c:set var="template" value="${element.builderJavaScriptTemplate}"/>
                <c:if test="${empty propertyOptions}">
                    <c:set var="propertyOptions" value="''"/>
                </c:if>
                try {
                    <c:set var="initScript"> 
                        CustomBuilder.initPaletteElement('<c:out value='${fn:replace(category, "\'", "\\\\\'")}' escapeXml='false'/>', '${element.className}', '<c:out value='${fn:replace(element.i18nLabel, "\'", "\\\\\'")}' escapeXml='false'/>', '<ui:escape value='${element.icon}' format='javascript'/>', ${propertyOptions}, '<ui:escape value='${element.defaultPropertyValues}' format='javascript'/>', true, "", {'pwaValidation' : '${element.pwaValidationType}', 'developer_mode' : '<ui:escape value='${element.developerMode}' format='javascript'/>', 'type' : 'menu', 'builderTemplate' : ${template}}); 
                    </c:set>
                    <c:set var="initScript"><ui:escape value="${initScript}" format="javascript"/></c:set>
                    eval("${initScript}");    
                } catch (err) {
                    if (console && console.log) {
                        console.log("Error initializing ${element.className} : " + err);
                    }
                }
            </c:forEach>
        </c:forEach>
        });
    </script>    
</c:set>
<c:set var="builderCSS" scope="request" value=""/>
<c:set var="builderCode" scope="request" value="userview"/>
<c:set var="builderColor" scope="request" value="#f3b328"/>
<c:set var="builderIcon" scope="request" value="fas fa-desktop"/>
<c:set var="builderDef" scope="request" value="${userview}"/>
<c:set var="builderDefJson" scope="request" value="${json}"/>
<c:set var="builderCanvas" scope="request" value=""/>
<c:set var="builderConfig" scope="request">
    {
        "builder" : {
            "options" : {
                "getDefinitionUrl" : "${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/userview/builder/${userview.id}/json",
                "rightPropertyPanel" : true,
                "defaultBuilder" : true
            },
            "callbacks" : {
                "initBuilder" : "UserviewBuilder.initBuilder",
                "unloadBuilder" : "UserviewBuilder.unloadBuilder",
                "load" : "UserviewBuilder.load",
                "saveEditProperties" : "UserviewBuilder.saveEditProperties",
                "getBuilderProperties" : "UserviewBuilder.getBuilderProperties",
                "saveBuilderProperties" : "UserviewBuilder.saveBuilderProperties",
                "getRuleObject" : "UserviewBuilder.getRuleObject"
            }
        },
        "advanced_tools" : {
            "xray" : {
                "disabled" : false,
            },
            "screenshot" : {
                "disabled" : false,
            },
            "permission" : {
                "element_support_plugin" : ["org.joget.apps.userview.model.UserviewCategory"],
                "render_elements_callback" : "UserviewBuilder.renderPermission",
                "permission_plugin" : "org.joget.apps.userview.model.UserviewAccessPermission",
                "childs_properties" : ["elements", "categories", "menus"],
                "supportNoPermisisonMessage" : "true"
            }
        }  
    }
</c:set>
<c:set var="builderProps" scope="request">
    ${setting.propertyOptions}
</c:set>
<c:set var="saveUrl" scope="request" value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/userview/builderSave/${userview.id}"/>
<c:set var="previewUrl" scope="request" value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/userview/builderPreview/${userview.id}"/>

<jsp:include page="../cbuilder/base.jsp" flush="true" />