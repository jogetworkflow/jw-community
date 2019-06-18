<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<style>
    #main #main-body {
        padding: 0px;
    }
</style>
<div id="nv" style="min-width:<c:out value="${(32 * fn:length(builders))}" />%;">
    <c:forEach items="${builders}" var="builderEntry" varStatus="loop">
        <c:set var="builderType" value="${builderEntry.key}" />
        <c:set var="builder" value="${builderEntry.value}" />
        <div id="nv-${builderType}" class="nv-col <c:if test="${!loop.last}"> nv-border</c:if>">
            <button href="#" onclick="navCreate('${builderType}')" class="nv-button"><fmt:message key="console.builder.create.label"><fmt:param value="${builder.objectLabel}"/></fmt:message></button>
            <h4>${builder.objectLabel}</h4>
            <ul class="nv-list">
                <c:forEach items="${builderDefinitionList}" var="builderDef">
                    <c:if test="${builderDef.type eq builderType}">
                        <li data-id="${builderDef.id}">
                            <a class="nv-link nv-left" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/cbuilder/${builderType}/design/${builderDef.id}" target="_blank" title='<fmt:message key="console.userview.common.label.id"/>: ${builderDef.id};&#13;<fmt:message key="console.userview.common.label.dateCreated"/>: <ui:dateToString date="${builderDef.dateCreated}"/>;&#13;<fmt:message key="console.userview.common.label.dateModified"/>: <ui:dateToString date="${builderDef.dateModified}"/>;&#13;<fmt:message key="console.userview.common.label.description"/>: <c:out value="${builderDef.description}"/>'><button href="#" onclick="return checkBuilderUsageDelete('${builderDef.id}', '${builderType}', event)" class="nv-delete" title='<fmt:message key="general.method.label.delete"/>'><i class="fas fa-times"></i></button><span class="nv-link-name"><i class="${builder.icon}" style="color:${builder.color};"></i> <c:out value="${builderDef.name}"/></span>
                                <div class="nv-extra" style="display:none"><div class="nv-subinfo"><c:out value="${builderDef.description}"/></div></div>
                            </a>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </div>  
    </c:forEach>    
    <div id="nv-clear"></div>
</div>

<script type="text/javascript">
    <ui:popupdialog var="builderwCreateDialog" src=""/>
    function navCreate(type){
        showCreateForm(type);
    }
    function showCreateForm(type){
        builderwCreateDialog.src = "${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/cbuilder/" + type + "/create?builderMode=false";
        builderwCreateDialog.init();
    }
    function checkBuilderUsageDelete(id, type, event) {
        if (confirm('<fmt:message key="console.builder.delete.label.confirmation"/>')) {
            Usages.delete(id, type, {
                contextPath: '${pageContext.request.contextPath}',
                appId: '${appDef.id}',
                appVersion: '${appDef.version}',
                id: id,
                builder: type,
                confirmMessage: '<fmt:message key="dependency.usage.confirmDelete"/>',
                confirmLabel: '<fmt:message key="dependency.usage.confirmLabel"/>',
                cancelLabel: '<fmt:message key="dependency.usage.cencelLabel"/>'
            }, function () {
                builderDelete(id, type, event);
            });
        }

        event.preventDefault();
        event.stopPropagation();
        return false;
    }
    function builderDelete(selectedList, type, event) {
        UI.blockUI();
        Nav.deleteItem(selectedList, type);
        var callback = {
            success: function () {
                UI.unblockUI();
                refreshNavigator();
            }
        };
        ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/cbuilder/'+type+'/delete', callback, 'ids=' + selectedList);
    }
    $(function () {
        Nav.init($("#nv-container"), ${tagDef}, {
            contextPath : "${pageContext.request.contextPath}",
            buttons : "#nv-refresh",
            refreshBtn : "#refreshBtn",
            infoBtn : "#toggleInfo",
            search : "#nv-search",
            url : '${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/customBuilders?hidden=true',
            tagUrl : '${pageContext.request.contextPath}/web/json/console/app/${appDef.id}/${appDef.version}/tagging',
            message : {
                'tags' : '<fmt:message key="console.tag.tags"/>',
                'createNew' : '<fmt:message key="console.tag.create"/>',
                'edit' : '<fmt:message key="console.tag.edit"/>',
                'save' : '<fmt:message key="general.method.label.save"/>',
                'create' : '<fmt:message key="general.method.label.create"/>',
                'delete' : '<fmt:message key="general.method.label.delete"/>',
                'name' : '<fmt:message key="console.tag.name"/>',
                'color' : '<fmt:message key="console.tag.color"/>',
                'search' : '<fmt:message key="console.tag.search"/>',
                'show' : '<fmt:message key="console.tag.show"/>',
                'hide' : '<fmt:message key="console.tag.hide"/>',
                'red' : '<fmt:message key="console.tag.red"/>',
                'pink' : '<fmt:message key="console.tag.pink"/>',
                'orange' : '<fmt:message key="console.tag.orange"/>',
                'yellow' : '<fmt:message key="console.tag.yellow"/>',
                'green' : '<fmt:message key="console.tag.green"/>',
                'lime' : '<fmt:message key="console.tag.lime"/>',
                'blue' : '<fmt:message key="console.tag.blue"/>',
                'sky' : '<fmt:message key="console.tag.sky"/>',
                'purple' : '<fmt:message key="console.tag.purple"/>',
                'black' : '<fmt:message key="console.tag.black"/>'
            }
        });
    });
</script>
