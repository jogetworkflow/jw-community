<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<jsp:useBean id="PropertyUtil" class="org.joget.plugin.property.service.PropertyUtil" scope="page"/>

<c:set var="appDef" scope="request" value="${appDefinition}"/>
<c:set var="builderLabel" scope="request"><fmt:message key="fbuilder.title"/></c:set>
<c:set var="builderI18N" scope="request" value=""/>
<c:set var="builderJS" scope="request">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/fbuilder.core.js?build=<fmt:message key="build.number"/>"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/pbuilder/js/jquery.jsPlumb-1.6.4-min.js"></script>
    <script>
        $(function () {  
        <c:forEach items="${palette.elementCategoryMap}" var="categoryRow">
            <c:set var="category" value="${categoryRow.key}"/>
            <c:set var="elementList" value="${categoryRow.value}"/>
            <c:forEach items="${elementList}" var="element">
                ${palette.editableElementList.remove(element)}
                <c:if test="${!empty element.propertyOptions}">
                    try {
                        <c:set var="initScript"> 
                            var elementProps = ${PropertyUtil.injectHelpLink(element.helpLink, element.propertyOptions)};
                            <c:choose>
                                <c:when test="${fn:startsWith(element.formBuilderTemplate, '{') && fn:endsWith(element.formBuilderTemplate, '}')}">
                                    var elementTemplate = ${element.formBuilderTemplate};
                                </c:when>
                                <c:otherwise>
                                    var elementTemplate = { "dragHtml" : "<div class=\"form-cell\"><c:out value="${fn:replace(element.formBuilderTemplate, '\"', '\\\\\"')}" escapeXml="false"/></div>" };   
                                </c:otherwise>    
                            </c:choose>     
                            FormBuilder.initPaletteElement('<c:out value='${fn:replace(category, "\'", "\\\\\'")}' escapeXml='false'/>', '${element.className}', '<c:out value='${fn:replace(element.i18nLabel, "\'", "\\\\\'")}' escapeXml='false'/>', '<ui:escape value='${element.formBuilderIcon}' format='javascript'/>', elementProps, '<ui:escape value='${element.defaultPropertyValues}' format='javascript'/>', true, "", {'builderTemplate' :  elementTemplate, 'developer_mode' : '<ui:escape value='${element.developerMode}' format='javascript'/>'}); 
                        </c:set>
                        <c:set var="initScript"><ui:escape value="${initScript}" format="javascript"/></c:set>
                        eval("${initScript}");    
                    } catch (err) {
                        if (console && console.log) {
                            console.log("Error initializing ${element.className} : " + err);
                        }
                    }
                </c:if>
            </c:forEach>
        </c:forEach>
        <c:forEach items="${palette.editableElementList}" var="element">
            <c:if test="${!empty element.propertyOptions}">
                try {
                    <c:set var="initScript"> 
                        var elementProps = ${PropertyUtil.injectHelpLink(element.helpLink, element.propertyOptions)};
                        FormBuilder.initPaletteElement('', '${element.className}', '<c:out value='${fn:replace(element.i18nLabel, "\'", "\\\\\'")}' escapeXml='false'/>', '', elementProps, '', false, "", {'builderTemplate' :  {}}); 
                    </c:set>
                    <c:set var="initScript"><ui:escape value="${initScript}" format="javascript"/></c:set>
                    eval("${initScript}");    
                } catch (err) {
                    if (console && console.log) {
                        console.log("Error initializing ${element.className} : " + err);
                    }
                }
            </c:if>
        </c:forEach>  
        });
    </script>  
    <jsp:include page="extend.jsp" flush="true" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/formUtil.js?build=<fmt:message key="build.number"/>"></script>
</c:set>
<c:set var="builderCSS" scope="request">
    <style>
        .usage_content {margin-bottom: 25px;}
        #tableUsageView .tab-pane {padding: 20px;}
        #tableUsageView h5 .tableName {color: grey; font-size: 60%; text-transform: none;}
        #desc-tab .relations{padding: 0 0 20px 24px;}
        .diagram-actions {text-align: right;}
        .diagram-actions a {cursor: pointer;}
        #diagram-grid {position: relative;}
        #diagram-grid .col:nth-child(even) {padding-top:150px;}
        #diagram-grid .col .row {padding: 80px 100px;}
        #diagram-grid .col .row:nth-child(even) {padding-left:50px;}
        #diagram-grid .col .row:nth-child(odd) {padding-right:50px;}
        #diagram-grid .entity-container {max-width: 350px; margin: auto; border: 1px solid #333; border-radius: 5px; overflow: hidden; position: relative;}
        #diagram-grid .entity-container h5 {text-align: center; padding: 20px; cursor: pointer; margin-bottom: 0; background: #e9f2ff;}
        #diagram-grid .entity-container h5 .la-exclamation-circle {position: absolute; display: block; top: 3px; right: 3px; color: #fe9600;}
        #diagram-grid .entity-container.current h5 {background: #d9f8ea;}
        #diagram-grid .entity-container h5 span {display: block;}
        #diagram-grid .entity-container .forms {display: none; padding: 10px; border-top: 1px solid #ccc;}
        #diagram-grid .entity-container .forms ul {margin-bottom: 0;}
        #diagram-grid .entity-container .fields {border-top: 1px solid #ccc; height: auto; display: block; min-width: 350px;}
        #diagram-grid .entity-container .field:nth-child(odd){background: #f3f3f3;}
        #diagram-grid .entity-container .field span {display: inline-block; padding: 5px 10px; box-sizing: border-box; width: 60%; word-break: break-all; vertical-align: middle;}
        #diagram-grid .entity-container .field span.label {position:relative; padding-left:25px;}
        #diagram-grid .entity-container .field span.type {width: 40%; word-break: normal; font-weight: 300;}
        #diagram-grid .entity-container .field a {position:absolute; top:5px; left:6px;}
        #diagram-grid .entity-container .field a:not(.indexed) {visibility:hidden; color:#333;}
        #diagram-grid .entity-container:not(.external) .field:hover a:not(.indexed) {visibility:visible; cursor:pointer;}
        #diagram-grid .entity-container.showDetails .fields {border-top: 1px solid #ccc; height: auto; display: block; min-width: 350px;}
        #diagram-grid .entity-container:not(.showDetails) .fields{border-top:0px; max-height: 100%; min-width: 0; position: absolute; visibility: hidden; top:0; left:0; right: 0; bottom: 0; display: flex; align-content: space-around; flex-direction: column; justify-content: space-evenly;}
        #diagram-grid .entity-container:not(.showDetails) .field:not(.connection_endpoint) {display: none;}
        #diagram-grid .entity-container:not(.showDetails) .connection_endpoint {height: 20px !important; overflow: hidden;}
        #diagram-grid .entity-container.showDetails .forms {display: block; background: #fdfbe9;}
    </style>    
</c:set>
<c:set var="builderCode" scope="request" value="form"/>
<c:set var="builderColor" scope="request" value="#3f84f4"/>
<c:set var="builderIcon" scope="request" value="fas fa-file-alt"/>
<c:set var="builderDef" scope="request" value="${formDef}"/>
<c:set var="builderDefJson" scope="request" value="${elementJson}"/>
<c:set var="systemTheme" scope="request" value="${systemTheme}"/>
<c:set var="builderCanvas" scope="request" value=""/>
<c:set var="builderConfig" scope="request">
    {
        "builder" : {
            "options" : {
                "getDefinitionUrl" : "${pageContext.request.contextPath}/web/json/console/app/${appId}/${appDefinition.version}/form/${formId}/json",
                "rightPropertyPanel" : true,
                "defaultBuilder" : true
            },
            "callbacks" : {
                "initBuilder" : "FormBuilder.initBuilder",
                "unloadBuilder" : "FormBuilder.unloadBuilder",
                "load" : "FormBuilder.load",
                "saveEditProperties" : "FormBuilder.saveEditProperties",
                "tooltipViewInit" : "FormBuilder.tooltipViewInit",
                "tableUsageViewInit" : "FormBuilder.tableUsageViewInit",
                "afterUpdate" : "FormBuilder.afterUpdate",
                "builderSaved" : "FormBuilder.afterUpdate"
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
                "element_support_plugin" : ["org.joget.apps.form.model.Section"],
                "ignore_classes" : ["org.joget.apps.form.model.Form", "org.joget.apps.form.model.Column"],
                "permission_plugin" : "org.joget.apps.form.model.FormPermission",
                "display_element_id" : true,
                "unauthorized" : {
                    property : "permissionReadonly"
                }
            }
        }
    }
</c:set>
<c:set var="builderProps" scope="request" value="CustomBuilder.paletteElements['org.joget.apps.form.model.Form']['propertyOptions']" />
<c:set var="saveUrl" scope="request" value="${pageContext.request.contextPath}/web/fbuilder/app/${appId}/${appDefinition.version}/form/${formId}/save"/>
<c:set var="previewUrl" scope="request" value="${pageContext.request.contextPath}/web/fbuilder/app/${appId}/${appDefinition.version}/form/${formId}/preview/"/>

<jsp:include page="../cbuilder/base.jsp" flush="true" />