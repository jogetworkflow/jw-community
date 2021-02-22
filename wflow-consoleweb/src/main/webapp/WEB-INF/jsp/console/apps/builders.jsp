<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<jsp:useBean id="PropertyUtil" class="org.joget.plugin.property.service.PropertyUtil" scope="page"/>

<c:set var="appDef" scope="request" value="${appDefinition}"/>
<c:set var="builderLabel" scope="request"><fmt:message key="abuilder.title"/></c:set>
<c:set var="builderI18N" scope="request" value=""/>
<c:set var="builderJS" scope="request">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/nav.js?build=<fmt:message key="build.number"/>"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/abuilder.core.js?build=<fmt:message key="build.number"/>"></script>
    <script>
        $(function () {
            Nav.init($("#builder_canvas"), {
                contextPath : "${pageContext.request.contextPath}",
                infoBtn : "#showTags",
                search : "#builder_canvas .search-container",
                tagUrl : '${pageContext.request.contextPath}/web/json/console/app/${appDefinition.id}/${appDefinition.version}/tagging',
                message : {
                    'tags' : '<ui:msgEscJS key="console.tag.tags"/>',
                    'createNew' : '<ui:msgEscJS key="console.tag.create"/>',
                    'edit' : '<ui:msgEscJS key="console.tag.edit"/>',
                    'save' : '<ui:msgEscJS key="general.method.label.save"/>',
                    'create' : '<ui:msgEscJS key="general.method.label.create"/>',
                    'delete' : '<ui:msgEscJS key="general.method.label.delete"/>',
                    'name' : '<ui:msgEscJS key="console.tag.name"/>',
                    'color' : '<ui:msgEscJS key="console.tag.color"/>',
                    'search' : '<ui:msgEscJS key="console.tag.search"/>',
                    'show' : '<ui:msgEscJS key="console.tag.show"/>',
                    'hide' : '<ui:msgEscJS key="console.tag.hide"/>',
                    'red' : '<ui:msgEscJS key="console.tag.red"/>',
                    'pink' : '<ui:msgEscJS key="console.tag.pink"/>',
                    'orange' : '<ui:msgEscJS key="console.tag.orange"/>',
                    'yellow' : '<ui:msgEscJS key="console.tag.yellow"/>',
                    'green' : '<ui:msgEscJS key="console.tag.green"/>',
                    'lime' : '<ui:msgEscJS key="console.tag.lime"/>',
                    'blue' : '<ui:msgEscJS key="console.tag.blue"/>',
                    'sky' : '<ui:msgEscJS key="console.tag.sky"/>',
                    'purple' : '<ui:msgEscJS key="console.tag.purple"/>',
                    'black' : '<ui:msgEscJS key="console.tag.black"/>'
                }
            });
        });
    </script>    
</c:set>
<c:set var="builderCSS" scope="request">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/abuilder.css?build=<fmt:message key="build.number"/>">
</c:set>
<c:set var="builderCode" scope="request" value="app"/>
<c:set var="builderColor" scope="request" value="#6e9f4b"/>
<c:set var="builderIcon" scope="request" value="fas fa-th"/>
<c:set var="builderDefJson" scope="request" value=""/>
<c:set var="builderCanvas" scope="request" value=""/>
<c:set var="builderConfig" scope="request">
    {
        "builder" : {
            "callbacks" : {
                "initBuilder" : "AppBuilder.initBuilder",
                "unloadBuilder" : "AppBuilder.unloadBuilder",
                "load" : "AppBuilder.load",
                "getBuilderProperties" : "AppBuilder.getBuilderProperties",
                "saveBuilderProperties" : "AppBuilder.saveBuilderProperties",
                "publishApp" : "AppBuilder.publishApp",
                "unpublishApp" : "AppBuilder.unpublishApp",
                "envVariablesViewInit" : "AppBuilder.envVariablesViewInit",
                "resourcesViewInit" : "AppBuilder.resourcesViewInit",
                "pluginDefaultPropertiesViewInit" : "AppBuilder.pluginDefaultPropertiesViewInit",
                "performanceViewInit" : "AppBuilder.performanceViewInit",
                "logViewerViewInit" : "AppBuilder.logViewerViewInit",
                "logViewerViewBeforeClosed" : "AppBuilder.logViewerViewBeforeClosed",
                "versionsViewInit" : "AppBuilder.versionsViewInit",
                "exportApp" : "AppBuilder.exportApp"
            }
        },
        "advanced_tools" : {
            "tree_viewer" : {
                disabled : true
            },
            "usage" : {
                disabled : true
            },
            "permission" : {
                disabled : true
            },
            "diffChecker" : {
                disabled : true
            },
            "definition" : {
                disabled : true
            }
        }
    }
</c:set>
<c:set var="builderProps" scope="request" value="AppBuilder.getPropertiesDefinition()" />
<c:set var="saveUrl" scope="request" value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/builderSave/${datalist.id}"/>
<c:set var="previewUrl" scope="request" value=""/>

<jsp:include page="../../cbuilder/base.jsp" flush="true" />