<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="isQuickEditEnabled" value="<%= AppUtil.isQuickEditEnabled() %>"/>
<c:set var="isAdmin" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>

<commons:popupHeader /> 
    <c:if test="${isQuickEditEnabled && isAdmin}">    
        <script src="${pageContext.request.contextPath}/js/adminBar.js"></script>
        <script>
            AdminBar.cookiePath = '${pageContext.request.contextPath}/';
        </script>  
    </c:if>    
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/datalistBuilderView.css?build=<fmt:message key="build.number"/>" />
        <script src="${pageContext.request.contextPath}/js/json2.js"></script>
        
        <script>
        $(document).ready(function() {   
            // hide submit button and add insert button
            if ($(".actions").length > 0) {
                $(".actions button").hide();
            } else {
                $(".dataList .exportlinks").after('<div class="actions bottom left"></div>');
            }
            var button = $('<button id="insert" class=\"form-button btn button\"><ui:stripTag html="${buttonLabel}"/></button>');
            $(".actions").append(button);

            // set parent ID
            var gridId = "<ui:stripTag html="${id}"/>";

            // click handler
            $(button).click(function(e) {
                e.preventDefault();
                
                var data = getSelectedData();
                
                if (window.parent && $("th.select_checkbox").length > 0) {
                    var json = $("iframe#<ui:stripTag html="${param._frameId}" />", window.parent.document).attr("_cachedSelection");
                    if (json !== undefined) {
                        var cachedData = JSON.parse(json);
                        if (cachedData !== undefined) {
                            data = $.merge(cachedData, data);
                        }
                    }
                }

                <c:choose>
                    <c:when test="${!empty submitUrl}">
                        var values = new Array();
                        for (var i = 0; i < data.length; i++) {
                            values.push(data[i]['id']);
                        }
                        $.post('${submitUrl}', {values : values}, function(data){
                            if (data.parent === "true" && window.parent !== undefined) {
                                window.parent.location.reload(true);
                            } else {
                                document.location.reload(true);
                            }
                        }, "json");
                    </c:when>    
                    <c:otherwise>
                        // get selected rows
                        // formulate result
                        var setting = <ui:stripTag html="${setting}" relaxed="true"/>;
                        for (var i = 0; i < data.length; i++) {
                            setting['id'] = data[i]['id'];
                            setting['result'] = data[i]['result'];
                            if (window.parent && window.parent.<ui:stripTag html="${callback}"/>) {
                                window.parent.<ui:stripTag html="${callback}"/>(setting);        
                            }
                        }
                    </c:otherwise>
                </c:choose>
            });
            
            //to support presist selection for checkbox
            if (window.parent && $("th.select_checkbox").length > 0) {
                var iframe = $("iframe#<ui:stripTag html="${param._frameId}"/>", window.parent.document);
                
                //cache selection on sorting, filter and change page
                $("th a, .pagelinks a, .filter-cell input[type=submit]").click(function() {
                    cacheSelection(iframe);
                    return true;
                });
                
                loadCachedSelection(iframe);
            }
        });
        
        function loadCachedSelection(iframe) {
            var json = $(iframe).attr("_cachedSelection");
            
            if (json !== undefined) {
                var data = JSON.parse(json);
                for (var i = data.length - 1; i >= 0 ; i--) {
                    var id = data[i]['id'];
                    if ($("input:checkbox[value="+id+"]").length > 0) {
                        $("input:checkbox[value="+id+"]").prop("checked", true);
                        data.splice(i, 1);
                    }
                }
                var json = JSON.stringify(data);
                $(iframe).attr("_cachedSelection", json);
            }
        }
        
        function cacheSelection(iframe) {
            var data = getSelectedData();
            
            var json = $("iframe#<ui:stripTag html="${param._frameId}" />", window.parent.document).attr("_cachedSelection");
            if (json !== undefined) {
                var cachedData = JSON.parse(json);
                if (cachedData !== undefined) {
                    data = $.merge(data, cachedData);
                }
            }
                    
            json = JSON.stringify(data);
            
            $(iframe).attr("_cachedSelection", json);
        }
        
        function htmlDecode(input){
            var e = document.createElement('div');
            e.innerHTML = input;
            return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
        }
        
        function getSelectedData() {
            // get selected checkboxes
            var selected = new Array();
            $("#listGridPopup tbody tr").each(function(idx, row) {
                var input = $(row).find("input:checkbox, input:radio");
                if (input.length > 0 && input.is(":checked")) {
                    selected.push(idx);
                }
            });

            // find columns in datalist
            var columns = new Array();
            var json = "(${json})";
            var list = eval(json);
            for (i=0; i<list.columns.length; i++) {
                var column = list.columns[i];
                columns.push(column);
            }

            // get selected rows
            // formulate result
            var data = new Array();
            for (var i = 0; i < selected.length; i++) {
                var idx = selected[i];
                var row = $("#listGridPopup tbody tr:eq("+idx+")");
                var result = new Object();
                var id = $(row).find('input:checkbox, input:radio').val();
                $(row).find("td").each(function(idx2, col) {
                    if (idx2 > 0) {
                        if (columns[idx2-1]) {
                            var prop = columns[idx2-1].name;
                            var val = $('<div>'+$(col).html()+'</div>');
                            $(val).find(".footable-toggle").remove();
                            if ($(val).find("*").length > 0) { //check if html
                                result[prop] = $(val).html();
                            } else {
                                result[prop] = htmlDecode($(val).html());
                            }
                        }
                    }
                });
                if (result["id"] !== undefined) {
                    result["id"] = id;
                }
                var json = JSON.stringify(result);
                var d = new Object();
                d['id'] = id;
                d['result'] = json;
                data.push(d);
            }
            return data;
        }
        </script>
        <style>
            .exportlinks { display: none }
        </style>
        <div id="listGridPopup">
            <c:set scope="request" var="dataListId" value="${dataList.id}"/>
            <jsp:include page="/WEB-INF/jsp/dbuilder/dataListView.jsp" flush="true" />
        </div>
<commons:popupFooter /> 
