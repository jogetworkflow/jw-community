<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader bodyCssClass=" builder-popup" builderTheme="true"/>

    <div id="main-body-header">
        <c:out value="${title}"/>
    </div>

    <div id="main-body-content" class="pluginList_container" style="text-align: initial">
        <div id="main-body-content-filter">
            <form>
                <fmt:message key="console.plugin.label.typeFilter"/>
                <select id="JsonDataTable1_filterbytype" onchange="filter(JsonDataTable1, '&className=', this.options[this.selectedIndex].value)">
                    <option></option>
                <c:forEach items="${pluginType}" var="t">
                    <c:set var="selected"><c:if test="${t.key == param.className}"> selected</c:if></c:set>
                    <option value="${t.key}" ${selected}>${t.value}</option>
                </c:forEach>
                </select>
            </form>
        </div>
        <ui:jsontable url="${pageContext.request.contextPath}/web/json/plugin/listBundlePlugins?${pageContext.request.queryString}"
            var="JsonDataTable1"
            divToUpdate="pluginList1"
            jsonData="data"
            rowsPerPage="15"
            width="100%"
            sort="name"
            desc="false"
            hrefQuery="false"
            hrefDialog="true"
            hrefDialogWidth="600px"
            hrefDialogHeight="400px"
            hrefDialogTitle=""
            searchItems="name|Name"
            fields="['id','name','description','version','plugintype']"
            column1="{key: 'name', label: 'console.plugin.label.name', sortable: false, width: 180}"
            column2="{key: 'description', label: 'console.plugin.label.description', sortable: false, width: 300}"
            column3="{key: 'version', label: 'console.plugin.label.version', sortable: false, width: 140}"
            column4="{key: 'plugintype', label: 'console.plugin.label.plugintype', sortable: false, width: 300}"
            />
    </div>
        
<script>
    $(document).ready(function(){
        $('#JsonDataTable1_searchTerm').hide();

        //Reposition the filter
        $("#main-body-content-filter").appendTo("#JsonDataTable1_pluginList1-search");
        $("#main-body-content-filter").show();
        
        var org_filter = window.filter;
        var filter = function(jsonTable, url, value){
            if(jsonTable == JsonDataTable1){
                url = "&className=" + encodeURIComponent($('#JsonDataTable1_filterbytype').val());
                url += "&name=" + encodeURIComponent($('#JsonDataTable1_searchCondition').val());
            }
            org_filter(jsonTable, url, '');
        }
    });
</script>
<commons:popupFooter />

