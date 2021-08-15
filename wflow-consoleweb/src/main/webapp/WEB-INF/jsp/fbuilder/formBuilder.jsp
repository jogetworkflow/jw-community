<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<jsp:useBean id="PropertyUtil" class="org.joget.plugin.property.service.PropertyUtil" scope="page"/>

<c:set var="appDef" scope="request" value="${appDefinition}"/>
<c:set var="builderLabel" scope="request"><fmt:message key="fbuilder.title"/></c:set>
<c:set var="builderI18N" scope="request" value=""/>
<c:set var="builderJS" scope="request">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/fbuilder.core.js?build=<fmt:message key="build.number"/>"></script>
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
                            FormBuilder.initPaletteElement('<c:out value='${fn:replace(category, "\'", "\\\\\'")}' escapeXml='false'/>', '${element.className}', '<c:out value='${fn:replace(element.i18nLabel, "\'", "\\\\\'")}' escapeXml='false'/>', '<ui:escape value='${element.formBuilderIcon}' format='javascript'/>', elementProps, '<ui:escape value='${element.defaultPropertyValues}' format='javascript'/>', true, "", {'builderTemplate' :  elementTemplate}); 
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
    </style>    
</c:set>
<c:set var="builderCode" scope="request" value="form"/>
<c:set var="builderColor" scope="request" value="#3f84f4"/>
<c:set var="builderIcon" scope="request" value="fas fa-file-alt"/>
<c:set var="builderDef" scope="request" value="${formDef}"/>
<c:set var="builderDefJson" scope="request" value="${elementJson}"/>
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
                "ignore_classes" : ["org.joget.apps.form.model.Column"],
                "render_elements_callback" : "FormBuilder.renderPermission",
                "permission_plugin" : "org.joget.apps.form.model.FormPermission"
            }
        }
    }
</c:set>
<c:set var="builderProps" scope="request" value="CustomBuilder.paletteElements['org.joget.apps.form.model.Form']['propertyOptions']" />
<c:set var="saveUrl" scope="request" value="${pageContext.request.contextPath}/web/fbuilder/app/${appId}/${appDefinition.version}/form/${formId}/save"/>
<c:set var="previewUrl" scope="request" value="${pageContext.request.contextPath}/web/fbuilder/app/${appId}/${appDefinition.version}/form/${formId}/preview/"/>

<jsp:include page="../cbuilder/base.jsp" flush="true" />