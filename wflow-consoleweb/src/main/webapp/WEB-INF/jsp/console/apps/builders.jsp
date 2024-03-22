<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.apps.app.service.AppDevUtil"%>
<c:set var="isGitDisabled" value="<%= AppDevUtil.isGitDisabled() %>"/>

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
<c:set var="builderIcon" scope="request" value="far fa-edit"/>
<c:set var="systemTheme" scope="request" value="${systemTheme}"/>
<c:set var="builderDefJson" scope="request">
    ${properties}
</c:set>
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
                "builderSaved" : "AppBuilder.builderSaved",
                "publishApp" : "AppBuilder.publishApp",
                "unpublishApp" : "AppBuilder.unpublishApp",
                "exportApp" : "AppBuilder.exportApp",
                "overviewViewBeforeClosed" : "AppBuilder.overviewViewBeforeClosed",
                "overviewMapViewInit" : "AppBuilder.overviewMapViewInit"
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
            },
            "i18n" : {
                disabled : true
            },
            "xray" : {
                 disabled : true
            }
        },
        "msg" : {
            'versions':'<ui:msgEscJS key="console.app.common.label.versions"/>',
            'export':'<ui:msgEscJS key="console.app.export.label"/>',
            'publish':'<ui:msgEscJS key="console.app.version.label.publish"/>',
            'unpublish':'<ui:msgEscJS key="console.app.version.label.unpublish"/>',
            'showTag':'<ui:msgEscJS key="console.tag.show"/>',
            'hideTag':'<ui:msgEscJS key="console.tag.hide"/>',
            'launch':'<ui:msgEscJS key="console.run.launch"/>',
            'runProcess':'<ui:msgEscJS key="client.app.run.process.label.start"/>',
            'addNewMessage':'<ui:msgEscJS key="console.app.common.label.addnew"/>',
            'publishConfirm':'<ui:msgEscJS key="console.app.publish.label.confirm"/>',
            'unpublishConfirm':'<ui:msgEscJS key="console.app.unpublish.label.confirm"/>',
            'appInfo':"<ui:escape value="${appInfo}" format="json" />",
            'published' : '<ui:msgEscJS key="console.app.common.label.published"/>'
        }
    }
</c:set>
<c:set var="builderProps" scope="request">
    [{
        title: '<ui:msgEscJS key="ubuilder.properties"/>',
        properties : [
            {
                label : '<ui:msgEscJS key="console.app.common.label.id"/>',
                name  : 'id',
                required : 'true',
                type : 'readonly'
            },
            {
                label : '<ui:msgEscJS key="console.app.common.label.name"/>',
                name  : 'name',
                required : 'true',
                type : 'textfield'
            }
        ]
    },
    {
        title: '<ui:msgEscJS key="console.app.dev.admin.settings"/>',
        properties : [
            {
                name: 'orgId',
                label: '<ui:msgEscJS key="userview.userpermission.selectOrg"/>',
                type: 'selectbox',
                options_ajax: '[CONTEXT_PATH]/web/json/plugin/org.joget.apps.userview.lib.UserPermission/service?action=getOrgs'
            },
            {
                name: 'ROLE_ADMIN',
                label: '<ui:msgEscJS key="userview.userpermission.selectUsers"/>',
                type: 'multiselect',
                size: '10',
                options_ajax_on_change: 'orgId',
                options_ajax: '[CONTEXT_PATH]/web/json/plugin/org.joget.apps.userview.lib.UserPermission/service?action=getUsers'
            },
            {
                name: 'ROLE_ADMIN_GROUP',
                label: '<ui:msgEscJS key="userview.grouppermission.selectGroups"/>',
                type: 'multiselect',
                size: '10',
                options_ajax_on_change: 'orgId',
                options_ajax: '[CONTEXT_PATH]/web/json/plugin/org.joget.apps.userview.lib.GroupPermission/service?action=getGroups'
            }
        ]
    }
    <c:if test="${!isGitDisabled}">
    ,{
        title: '<ui:msgEscJS key="console.app.dev.git.configuration"/>',
        properties: [
            {
                name: 'gitUri',
                label: '<ui:msgEscJS key="console.app.dev.git.uri"/>',
                type: 'textfield'
            },
            {
                name: 'gitUsername',
                label: '<ui:msgEscJS key="console.app.dev.git.username"/>',
                type: 'textfield'
            },
            {
                name: 'gitPassword',
                label: '<ui:msgEscJS key="console.app.dev.git.password"/>',
                type: 'password'
            },
            {
                label: '<ui:msgEscJS key="console.app.dev.git.deployment"/>',
                type: 'header'
            },
            {
                name: 'gitConfigExcludeCommit',
                label: '<ui:msgEscJS key="console.app.dev.git.configExcludeCommit"/>',
                type: 'checkbox',
                options: [{
                        value: 'true',
                        label: ''
                    }]
            },
            {
                name: 'gitConfigPull',
                label: '<ui:msgEscJS key="console.app.dev.git.configPull"/>',
                type: 'checkbox',
                options: [{
                        value: 'true',
                        label: ''
                    }]
            },
            {
                name : 'gitConfigAutoSync',
                label : '<ui:msgEscJS key="console.app.dev.git.configAutoSync"/>',
                type : 'checkbox',
                options : [{
                    value : 'true',
                    label : ''
                }]
            }
        ]
    }    
    </c:if>
    ]
</c:set>
<c:set var="saveUrl" scope="request" value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/dev/submit"/>
<c:set var="previewUrl" scope="request" value=""/>

<jsp:include page="../../cbuilder/base.jsp" flush="true" />