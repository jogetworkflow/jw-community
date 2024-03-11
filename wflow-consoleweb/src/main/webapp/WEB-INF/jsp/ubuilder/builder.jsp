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
        <c:forEach items="${categories}" var="category">
            CustomBuilder.createPaletteCategory('<c:out value='${fn:replace(category, "\'", "\\\\\'")}' escapeXml='false'/>');
        </c:forEach>
        CustomBuilder.createPaletteCategory('<ui:msgEscJS key="ubuilder.pageComponents"/>', "", "page_components_palette");    
        <c:forEach items="${pageComponent}" var="element">
            <c:set var="category" value=""/>
            <c:set var="pwaValidation" value=""/>
            <c:set var="type" value="component"/>
            <c:set var="propertyOptions" value="${element.propertyOptions}"/>
            <c:set var="template" value="${element.builderJavaScriptTemplate}"/>
            <c:if test="${empty propertyOptions}">
                <c:set var="propertyOptions" value="''"/>
            </c:if>
            <c:if test="${element.isUiMenu()}">
                <c:set var="type" value="menu"/>   
                <c:set var="pwaValidation" value="'pwaValidation' : '${element.pwaValidationType}', "/>    
                <c:set var="category" value='${category};${fn:replace(element.category, "\'", "\\\\\'")}'/>
            </c:if>
            try {
                <c:set var="initScript"> 
                    CustomBuilder.initPaletteElement('<ui:msgEscJS key="ubuilder.pageComponents"/>${category}', '${element.className}', '<c:out value='${fn:replace(element.i18nLabel, "\'", "\\\\\'")}' escapeXml='false'/>', '<ui:escape value='${element.icon}' format='javascript'/>', ${propertyOptions}, '<ui:escape value='${element.defaultPropertyValues}' format='javascript'/>', !${element.isHiddenPlugin()}, "", {${pwaValidation} 'developer_mode' : '<ui:escape value='${element.developerMode}' format='javascript'/>', 'type' : '${type}', 'builderTemplate' : ${template}}); 
                </c:set>
                <c:set var="initScript"><ui:escape value="${initScript}" format="javascript"/></c:set>
                eval("${initScript}");    
            } catch (err) {
                if (console && console.log) {
                    console.log("Error initializing ${element.className} : " + err);
                }
            }
        </c:forEach>
        });
    </script>    
</c:set>
<c:set var="builderCSS" scope="request">
    <style>
        body.page-component-editor {
            --builder-header-top-height: 55px;
        }
        body #save-content-btn {
            display: none;
        }
        body.page-component-editor #save-content-btn{
            display: inline-block !important;
            margin-right: 30px;
            animation: glowing 1600ms infinite;
        }
        body.page-component-editor #top-panel{
            padding: 5px 0;
        }
        body.page-component-editor #top-panel-main{
            padding-left: 15px !important;
        }
        body.page-component-editor #builderIcon,
        body.page-component-editor #builderElementName {
            display: none !important;
        }
        @keyframes glowing {
            0% {
              background-color: #28a745;
              border-color: #28a745;
              box-shadow: 0 0 3px #28a745;
            }
            50% {
              background-color: #32cf56;
              box-shadow: 0 0 20px #32cf56;
              border-color: #32cf56;
            }
            100% {
              background-color: #28a745;
              box-shadow: 0 0 3px #28a745;
              border-color: #28a745;
            }
        }
    </style>    
</c:set> 
<c:set var="builderCode" scope="request" value="userview"/>
<c:set var="builderColor" scope="request" value="#f3b328"/>
<c:set var="builderIcon" scope="request" value="fas fa-desktop"/>
<c:set var="builderDef" scope="request" value="${userview}"/>
<c:set var="builderDefJson" scope="request" value="${json}"/>
<c:set var="systemTheme" scope="request" value="${systemTheme}"/>
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
                "getBuilderItemName" : "UserviewBuilder.getBuilderItemName",
                "saveBuilderProperties" : "UserviewBuilder.saveBuilderProperties",
                "getRuleObject" : "UserviewBuilder.getRuleObject",
                "previewViewInit" : "UserviewBuilder.previewViewInit",
                "previewViewBeforeClosed" : "UserviewBuilder.previewViewBeforeClosed",
                "screenshotViewInit" : "UserviewBuilder.screenshotViewInit",
                "builderSaved" : "UserviewBuilder.builderSaved",
                "getOverviewPathElementSelector" : "UserviewBuilder.getOverviewPathElementSelector"
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
                "ignore_classes" : ["org.joget.apps.userview.model.Userview", "org.joget.apps.userview.model.UserviewPage", "menu-component"],
                "permission_plugin" : "org.joget.apps.userview.model.UserviewAccessPermission",
                "render_elements_callback" : "UserviewBuilder.renderPermissionElements",
                "childs_properties" : ["elements", "categories", "menus"],
                "supportNoPermisisonMessage" : "true",
                "display_element_id" : true,
                "element_id_field" : "customId",
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