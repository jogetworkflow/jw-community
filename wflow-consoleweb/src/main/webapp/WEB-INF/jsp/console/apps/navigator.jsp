<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<style>
    #main #main-body {
        padding: 0px;
    }
</style>
<script> 
    (function ($) { 
        jQuery.expr[':'].Contains = function(a,i,m){ 
            return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase())>=0; 
        };  
        
        function listFilter(header, list) { 
            var form = $("<form>").attr({"class":"filterform","action":"#"}), 
            input = $("<input>").attr({"class":"filterinput","type":"text"}); 
            $(form).append(input).append($("<span class='filterlabel'><i class='icon-search'></i></span>")).appendTo(header);  
            $(input) .change( function () { 
                var filter = $(this).val(); 
                if(filter) { 
                    $(list).find("a:not(:Contains(" + filter + "))").parent().slideUp(); 
                    $(list).find("a:Contains(" + filter + ")").parent().slideDown(); 
                } else { 
                    $(list).find("li").slideDown(); 
                } 
                return false; 
            }) .keyup( function () { 
                $(this).change(); 
            }); 
        }  
        
        $(function () { 
            listFilter($("#nv-form h4"), $("#nv-form ul")); 
            listFilter($("#nv-list h4"), $("#nv-list ul")); 
            listFilter($("#nv-userview h4"), $("#nv-userview ul")); 
        }); 
    }(jQuery)); 
</script>

<div id="nv">
    <div id="nv-form" class="nv-col nv-border">
        <button href="#" onclick="navCreate('form')" class="nv-button"><fmt:message key="console.form.create.label"/></button>
        <h4><fmt:message key="console.header.submenu.label.forms"/></h4>
        <ul class="nv-list">
            <c:forEach items="${formDefinitionList}" var="formDef">
                <li>
                    <a class="nv-link" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/form/builder/${formDef.id}" target="_blank" title='<fmt:message key="console.form.common.label.id"/>: ${formDef.id}&#13;<fmt:message key="console.form.common.label.dateCreated"/>: <c:out value="${formDef.dateCreated}"/>&#13;<fmt:message key="console.form.common.label.dateModified"/>: <c:out value="${formDef.dateModified}"/>'><button href="#" onclick="return formDelete('${formDef.id}', event)" class="nv-delete" title='<fmt:message key="general.method.label.delete"/>'><i class="icon-remove"></i></button><span class="nv-link-name"><i class="icon-file-alt"></i> ${formDef.name}</span> <span class="nv-form-table">${formDef.tableName}</span></a>
                </li>
            </c:forEach>
        </ul>
    </div>
    <div id="nv-list" class="nv-col nv-border">
        <button href="#" onclick="navCreate('datalist')" class="nv-button"><fmt:message key="console.datalist.create.label"/></button>
        <h4><fmt:message key="console.header.submenu.label.lists"/></h4>
        <ul class="nv-list">
            <c:forEach items="${datalistDefinitionList}" var="listDef">
                <li>
                    <a class="nv-link" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/datalist/builder/${listDef.id}" target="_blank" title='<fmt:message key="console.datalist.common.label.id"/>: ${listDef.id}&#13;<fmt:message key="console.datalist.common.label.dateCreated"/>: <c:out value="${listDef.dateCreated}"/>&#13;<fmt:message key="console.datalist.common.label.dateModified"/>: <c:out value="${listDef.dateModified}"/>'><button href="#" onclick="return datalistDelete('${listDef.id}', event)" class="nv-delete" title='<fmt:message key="general.method.label.delete"/>'><i class="icon-remove"></i></button><span class="nv-link-name"><i class="icon-calendar"></i> ${listDef.name}</span></a>
                </li>
            </c:forEach>
        </ul>
    </div>
    <div id="nv-userview" class="nv-col">
        <button href="#" onclick="navCreate('userview')" class="nv-button"><fmt:message key="console.userview.create.label"/></button>
        <h4><fmt:message key="console.header.submenu.label.userview"/></h4>
        <ul class="nv-list">
            <c:forEach items="${userviewDefinitionList}" var="userviewDef">
                <li>
                    <a class="nv-link nv-left" href="${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/userview/builder/${userviewDef.id}" target="_blank" title='<fmt:message key="console.userview.common.label.id"/>: ${userviewDef.id}&#13;<fmt:message key="console.userview.common.label.dateCreated"/>: <c:out value="${userviewDef.dateCreated}"/>&#13;<fmt:message key="console.userview.common.label.dateModified"/>: <c:out value="${userviewDef.dateModified}"/>'><button href="#" onclick="return userviewDelete('${userviewDef.id}', event)" class="nv-delete" title='<fmt:message key="general.method.label.delete"/>'><i class="icon-remove"></i></button><span class="nv-link-name"><i class="icon-desktop"></i> ${userviewDef.name}</span></a>
                    <c:if test="${appDef.published}">
                        <button class="nv-button-small" onclick="window.open('${pageContext.request.contextPath}/web/userview/${appDef.id}/${userviewDef.id}')" target="_blank"><fmt:message key="console.run.launch"/></button>
                    </c:if>
                </li>
            </c:forEach>
        </ul>
    </div>
    <div id="nv-clear"></div>
</div>

<script type="text/javascript">
    <ui:popupdialog var="builderwCreateDialog" src=""/>
    function navCreate(type){
        showCreateForm(type);
    }
    function showCreateForm(type){
        builderwCreateDialog.src = "${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/" + type + "/create?builderMode=false";
        builderwCreateDialog.init();
    }
    function formDelete(selectedList, event){        
        if (confirm('<fmt:message key="console.form.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    refreshNavigator();
                }
            }
            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/form/delete', callback, 'formId='+selectedList);
        }
        event.preventDefault();
        event.stopPropagation();
        return false;
    }
    function datalistDelete(selectedList, event){
        if (confirm('<fmt:message key="console.datalist.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    refreshNavigator();
                }
            }
            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/datalist/delete', callback, 'ids='+selectedList);
        }
        event.preventDefault();
        event.stopPropagation();
        return false;
    }
    function userviewDelete(selectedList, event){
        if (confirm('<fmt:message key="console.userview.delete.label.confirmation"/>')) {
            var callback = {
                success : function() {
                    refreshNavigator();
                }
            }
            ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appDef.id}/${appDef.version}/userview/delete', callback, 'ids='+selectedList);
        }
        event.preventDefault();
        event.stopPropagation();
        return false;
    }
</script>
