<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<jsp:useBean id="PropertyUtil" class="org.joget.plugin.property.service.PropertyUtil" scope="page"/>

<c:set var="appDef" scope="request" value="${appDefinition}"/>
<c:set var="builderLabel" scope="request"><fmt:message key="dbuilder.title"/></c:set>
<c:set var="builderI18N" scope="request" value=""/>
<c:set var="builderJS" scope="request">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/dbuilder.core.js?build=<fmt:message key="build.number"/>"></script>
</c:set>
<c:set var="builderCSS" scope="request" value=""/>
<c:set var="builderCode" scope="request" value="datalist"/>
<c:set var="builderColor" scope="request" value="#6638b6"/>
<c:set var="builderIcon" scope="request" value="fas fa-table"/>
<c:set var="builderDef" scope="request" value="${datalist}"/>
<c:set var="builderDefJson" scope="request" value="${json}"/>
<c:set var="systemTheme" scope="request" value="${systemTheme}"/>
<c:set var="builderCanvas" scope="request" value=""/>
<c:set var="builderConfig" scope="request">
    {
        "builder" : {
            "options" : {
                "getDefinitionUrl" : "${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/datalist/${datalist.id}/json",
                "rightPropertyPanel" : true,
                "defaultBuilder" : true,
                "filterParam" : "<c:out value="${filterParam}"/>"
            },
            "callbacks" : {
                "initBuilder" : "DatalistBuilder.initBuilder",
                "unloadBuilder" : "DatalistBuilder.unloadBuilder",
                "load" : "DatalistBuilder.load",
                "saveEditProperties" : "DatalistBuilder.saveEditProperties",
                "getBuilderProperties" : "DatalistBuilder.getBuilderProperties",
                "saveBuilderProperties" : "DatalistBuilder.saveBuilderProperties",
                "dataBinderViewInit" : "DatalistBuilder.dataBinderViewInit",
                "getRuleObject" : "DatalistBuilder.getRuleObject",
                "builderBeforeMerge" : "DatalistBuilder.beforeMerge",
                "afterUpdate" : "DatalistBuilder.afterUpdate",
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
                "permission_plugin" : "org.joget.apps.datalist.model.DatalistPermission",
                "render_elements_callback": "DatalistBuilder.renderPermissionElements",
                "check_ignore_rendering_callback": "DatalistBuilder.isPermissionIgnoreRendering",
                "display_element_id" : true,
                "element_id_field" : "name",
            }
        }
    }
</c:set>
<c:set var="builderProps" scope="request" value="DatalistBuilder.getDatalistPropertiesDefinition()" />
<c:set var="saveUrl" scope="request" value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/builderSave/${datalist.id}"/>
<c:set var="previewUrl" scope="request" value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/builderPreview/${datalist.id}"/>

<jsp:include page="../cbuilder/base.jsp" flush="true" />