<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@page contentType="text/html" pageEncoding="utf-8"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.5.2.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui/jquery-ui-1.8.6.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.jeditable.js"></script>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery/themes/themeroller/jquery-ui-themeroller.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/datalistBuilderView.css?build=<fmt:message key="build.number"/>" />
        <script src="${pageContext.request.contextPath}/js/json2.js"></script>
        <script src="${pageContext.request.contextPath}/js/json/util.js"></script>      
            
        <c:set var="json" value="${fn:replace(json, '\\\\', '\\\\\\\\')}"/>
        <c:set var="json" value="${fn:replace(json, '\"', '\\\\\"')}"/>
        <script>
        $(document).ready(function() {   
            // hide submit button and add insert button
            $(".actions button").hide();
            var button = $('<button id="insert">${buttonLabel}</button>');
            $(".actions").append(button);

            // set parent ID
            var gridId = "${id}";

            // click handler
            $(button).click(function(e) {
                e.preventDefault();
                
                // get selected checkboxes
                var selected = new Array();
                $("#listGridPopup input:checkbox, #listGridPopup input:radio").each(function(idx, row) {
                    <c:if test="${dataList.selectionType != 'single'}">
                    idx--; // offset to ignore first selectall checkbox
                    </c:if>
                    if ($(row).is(':checked') && idx >= 0) {
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
                var setting = ${setting};
                for (var i = 0; i < selected.length; i++) {
                    var idx = selected[i];
                    var row = $("#listGridPopup tbody tr:eq("+idx+")");
                    var result = new Object();
                    var id = $(row).find('input:checkbox, input:radio').val();
                    $(row).find("td").each(function(idx2, col) {
                        if (idx2 > 0) {
                            if (columns[idx2-1]) {
                                var prop = columns[idx2-1].name;
                                var val = $(col).text();
                                result[prop] = val;
                            }
                        }
                    });
                    var json = JSON.stringify(result);
                    setting['id'] = id;
                    setting['result'] = json;
                    if (window.parent && window.parent.${callback}) {
                        window.parent.${callback}(setting);        
                    }
                }
            });
        });
        </script>
        <style>
            .exportlinks { display: none }
            body{font-size:13px;}
            <c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
                body{direction:rtl;}
            </c:if>
        </style>
    </head>
    <body>
        <div id="listGridPopup">
            <c:set scope="request" var="dataListId" value="${dataList.id}"/>
            <jsp:include page="/WEB-INF/jsp/dbuilder/dataListView.jsp" flush="true" />
        </div>
    </body>
</html>    
