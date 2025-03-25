<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<%@ attribute name="var" %>
<%@ attribute name="divToUpdate" required="true" %>
<%@ attribute name="url" required="true" %>
<%@ attribute name="caption" %>
<%@ attribute name="jsonData" %>
<%@ attribute name="useRp" %>
<%@ attribute name="rowsPerPage"  %>
<%@ attribute name="width"  %>
<%@ attribute name="height"  %>
<%@ attribute name="href"  %>
<%@ attribute name="hrefParam"  %>
<%@ attribute name="hrefQuery"  %>
<%@ attribute name="hrefSuffix"  %>
<%@ attribute name="hrefDialog"  %>
<%@ attribute name="hrefDialogWidth"  %>
<%@ attribute name="hrefDialogHeight"  %>
<%@ attribute name="hrefDialogTitle"  %>
<%@ attribute name="hrefDialogWindowName"  %>
<%@ attribute name="hrefDialogTab"  %>
<%@ attribute name="hrefPost"  %>
<%@ attribute name="sort" %>
<%@ attribute name="desc" %>
<%@ attribute name="checkbox" %>
<%@ attribute name="checkboxId" %>
<%@ attribute name="checkboxButton" %>
<%@ attribute name="checkboxCallback" %>
<%@ attribute name="checkboxSelectSingle" %>
<%@ attribute name="checkboxSelection" %>
<%@ attribute name="checkboxSelectionTitle" %>
<%@ attribute name="fields" required="true" %>
<%@ attribute name="searchItems"%>
<%@ attribute name="dateComparison"%>
<%@ attribute name="customPreProcessor"%>
<%@ attribute name="dynamicColumn"%>
<%@ tag dynamic-attributes="attributeMap" %>

<%-- support dynamically set attribute using map--%>
<%@ attribute name="dynamicAttributes" type="java.util.Map"%>
<jsp:useBean id="attributeMap" class="java.util.HashMap" />
<c:if test="${not empty dynamicAttributes}">
    <c:forEach var="entry" items="${dynamicAttributes}">
        <c:set target="${attributeMap}" property="${entry.key}" value="${entry.value}" />
    </c:forEach>
</c:if>
<c:if test="${empty height && !empty attributeMap['height']}"><c:set var="height" value="${attributeMap['height']}"/></c:if>
<c:if test="${empty href && !empty attributeMap['href']}"><c:set var="href" value="${attributeMap['href']}"/></c:if>
<c:if test="${empty hrefParam && !empty attributeMap['hrefParam']}"><c:set var="hrefParam" value="${attributeMap['hrefParam']}"/></c:if>
<c:if test="${empty hrefQuery && !empty attributeMap['hrefQuery']}"><c:set var="hrefQuery" value="${attributeMap['hrefQuery']}"/></c:if>
<c:if test="${empty hrefSuffix && !empty attributeMap['hrefSuffix']}"><c:set var="hrefSuffix" value="${attributeMap['hrefSuffix']}"/></c:if>
<c:if test="${empty hrefDialogTitle && !empty attributeMap['hrefDialogTitle']}"><c:set var="hrefDialogTitle" value="${attributeMap['hrefDialogTitle']}"/></c:if>
<c:if test="${empty hrefDialogWindowName && !empty attributeMap['hrefDialogWindowName']}"><c:set var="hrefDialogWindowName" value="${attributeMap['hrefDialogWindowName']}"/></c:if>
<c:if test="${empty hrefDialogTab && !empty attributeMap['hrefDialogTab']}"><c:set var="hrefDialogTab" value="${attributeMap['hrefDialogTab']}"/></c:if>
<c:if test="${empty hrefPost && !empty attributeMap['hrefPost']}"><c:set var="hrefPost" value="${attributeMap['hrefPost']}"/></c:if>
<c:if test="${empty sort && !empty attributeMap['sort']}"><c:set var="sort" value="${attributeMap['sort']}"/></c:if>
<c:if test="${empty desc && !empty attributeMap['desc']}"><c:set var="desc" value="${attributeMap['desc']}"/></c:if>
<c:if test="${empty searchItems && !empty attributeMap['searchItems']}"><c:set var="searchItems" value="${attributeMap['searchItems']}"/></c:if>
<c:if test="${empty dateComparison && !empty attributeMap['dateComparison']}"><c:set var="dateComparison" value="${attributeMap['dateComparison']}"/></c:if>
<c:if test="${empty dynamicColumn && !empty attributeMap['dynamicColumn']}"><c:set var="dynamicColumn" value="${attributeMap['dynamicColumn']}"/></c:if>

<c:if test="${empty width}"><c:set var="width" value="${!empty attributeMap['width'] ? attributeMap['width'] : '100%' }"/></c:if>    
<c:if test="${empty var}"><c:set var="var" value="${!empty attributeMap['var'] ? attributeMap['var'] : 'jsonTable' }"/></c:if>
<c:if test="${empty useRp}"><c:set var="useRp" value="${!empty attributeMap['useRp'] ? attributeMap['useRp'] : 'true' }"/></c:if>
<c:if test="${empty rowsPerPage}"><c:set var="rowsPerPage" value="${!empty attributeMap['rowsPerPage'] ? attributeMap['rowsPerPage'] : '15' }"/></c:if>
<c:if test="${empty jsonData}"><c:set var="jsonData" value="${!empty attributeMap['jsonData'] ? attributeMap['jsonData'] : 'data' }"/></c:if>
<c:if test="${empty hrefQuery}"><c:set var="hrefQuery" value="${!empty attributeMap['hrefQuery'] ? attributeMap['hrefQuery'] : 'false' }"/></c:if>
<c:if test="${empty hrefDialog}"><c:set var="hrefDialog" value="${!empty attributeMap['hrefDialog'] ? attributeMap['hrefDialog'] : 'false' }"/></c:if>
<c:if test="${empty hrefDialogWidth && !hrefDialogTab}"><c:set var="hrefDialogWidth" value="450px"/></c:if>
<c:if test="${empty hrefDialogHeight && !hrefDialogTab}"><c:set var="hrefDialogHeight" value="400px"/></c:if>
<c:if test="${empty checkbox}"><c:set var="checkbox" value="${!empty attributeMap['checkbox'] ? attributeMap['checkbox'] : 'false' }"/></c:if>
<c:if test="${empty checkboxId}"><c:set var="checkboxId" value="${!empty attributeMap['checkboxId'] ? attributeMap['checkboxId'] : 'id' }"/></c:if>
<c:if test="${empty checkboxSelectSingle}"><c:set var="checkboxSelectSingle" value="${!empty attributeMap['checkboxSelectSingle'] ? attributeMap['checkboxSelectSingle'] : 'false' }"/></c:if>
<c:if test="${empty checkboxSelection}"><c:set var="checkboxSelection" value="${!empty attributeMap['checkboxSelection'] ? attributeMap['checkboxSelection'] : 'false' }"/></c:if>
<c:if test="${empty checkboxSelectionTitle}"><c:set var="checkboxSelectionTitle" value="${!empty attributeMap['checkboxSelectionTitle'] ? attributeMap['checkboxSelectionTitle'] : 'Selected Items' }"/></c:if>
<c:if test="${empty customPreProcessor}"><c:set var="customPreProcessor" value="${!empty attributeMap['customPreProcessor'] ? attributeMap['customPreProcessor'] : 'null' }"/></c:if>

<c:if test="${checkboxSelectSingle}"><c:set var="checkboxSelection" value="false"/></c:if>

<div id="${var}_${divToUpdate}-search" >
    <c:if test="${!empty searchItems}">
        <script type="text/javascript">
            var searchTimer_${var} = null;

            function triggerTimer_${var}(){
                clearTimeout(searchTimer_${var});
                searchTimer_${var} = setTimeout("search_${var}_${divToUpdate}()", 500);
            }


            function search_${var}_${divToUpdate}(){
                var term = document.getElementById("${var}_searchTerm");
                var condition = $('#${var}_searchCondition').val();
                filter(${var}, '&' + term.options[term.selectedIndex].value + '=', condition);
            }

            function clear_${var}_${divToUpdate}(){
                var condition = $('#${var}_searchCondition').val('');
                filter(${var}, '', '');
            }
        </script>

        <c:set var="searchTerms" value="${fn:split(searchItems, ',')}"/>

        <fmt:message key="general.method.label.search"/> <input id="${var}_searchCondition" type="text" onkeyup="triggerTimer_${var}()">
        <select id="${var}_searchTerm">
            <c:forEach var="termString" items="${searchTerms}">
                <c:set var="term" value="${fn:split(termString, '|')}"/>
                <c:set var="termlabel"><fmt:message key="${fn:trim(term[1])}"/></c:set>
                <c:set var="testLabel" value="???${fn:trim(term[1])}???" />
                <c:if test="${termlabel eq testLabel}"><c:set var="termlabel" value="${fn:trim(term[1])}"/></c:if>
                <option value="${fn:trim(term[0])}">${termlabel}</option>
            </c:forEach>
        </select>
        <%--
        <input type="button" onclick="search_${var}_${divToUpdate}()" value="show">
        <input type="button" onclick="clear_${var}_${divToUpdate}()" value="clear">
        --%>
        <div style="margin-bottom: 10px"></div>
    </c:if>

    <c:if test="${!empty dateComparison}">
        <c:set var="comparisonRequired" value="${dateComparison}"/>
        <c:if test="${comparisonRequired == 'true'}">
            <div id="main-body-content-filter">
                <form>
                    <div>
                        <span><fmt:message key="wflowAuditTrail.filter.by.date.from"/></span>
                        <span><input type="text" id="dateFromId" name="dateFrom"/></span>

                        <fmt:message key="wflowAuditTrail.filter.by.date.to"/>
                        <input type="text" id="dateToId" name="dateTo"/>

                        <input type="button" value="<ui:msgEscHTML key="general.method.label.search"/>" onclick="filterByDate(JsonDataTable, $('#dateFromId').val(), $('#dateToId').val())"/>
                    </div>
                </form>
            </div>

            <script>
                Calendar.show("dateFromId");
                Calendar.show("dateToId");

                function filterByDate(jsonTable, dateFrom, dateTo){
                    var validateDateFrom = dateFrom.split("-")[2];
                    var validateDateTo = dateTo.split("-")[2];

                    if(validateDateTo<validateDateFrom) alert('<ui:msgEscJS key="general.date.from.greater.than.to.exception"/>');
                    else jsonTable.load(jsonTable.url + '&dateFrom='+dateFrom+'&dateTo='+dateTo);
                }
            </script>
        </c:if>
    </c:if>
</div>

<table id="${divToUpdate}"></table>

<c:if test="${checkbox == true && checkboxSelectSingle == false}">
    <div id="${divToUpdate}_selectedIds" style="display:none"></div>
</c:if>

<div id="${var}_${divToUpdate}-buttons">
    <br/>
    <c:forEach var="count" begin="0" end="10">
        <c:set var="button">checkboxButton${count}</c:set>
        <c:set var="callback">checkboxCallback${count}</c:set>
        <c:set var="optional">checkboxOptional${count}</c:set>
        <c:if test="${!empty attributeMap[button]}">
            <script type="text/javascript">
                function ${var}_${divToUpdate}_${attributeMap[callback]}_callback() {
                    var selectedRows = ${var}.getSelectedRows();
                    <c:if test="${attributeMap[optional] != 'true'}">
                    if (!selectedRows || selectedRows.length === 0 || (selectedRows.length === 1 && selectedRows[0] === "")) {
                        alert('<ui:msgEscJS key="dbuilder.alert.noRecordSelected"/>');
                        return;
                    }
                    </c:if>
                    ${attributeMap[callback]}(selectedRows);
                }
            </script>
            <fmt:message var="checkboxButtonLabel" key="${attributeMap[button]}"/>
            <c:if test="${fn:startsWith(checkboxButtonLabel, '???') && fn:endsWith(checkboxButtonLabel, '???') && !fn:contains(attributeMap[button], '.')}">
                <c:set var="checkboxButtonLabel">${attributeMap[button]}</c:set>
            </c:if>  
            <button type="button" onclick="${var}_${divToUpdate}_${attributeMap[callback]}_callback()">${checkboxButtonLabel}</button>
        </c:if>
    </c:forEach>
</div>
    
<form id="ui_link_form" style="display:none"></form>

<script type="text/javascript">
    <c:set var="escapedUrl"><ui:escape value="${url}" format="html"/></c:set>
    <c:set var="escapedUrl" value="${fn:replace(escapedUrl, '&amp;', '&')}"/>
    var ${var} = new JsonTable("${divToUpdate}", "${escapedUrl}");
    var myColumnDefs = [
    <c:set var="first" value="true"/>

    <c:if test="${checkbox}">
        <c:choose>
                <c:when test="${!checkboxSelectSingle}">
                        {key: 'checkbox', label: '<input type="checkbox" id="${divToUpdate}-checkboxes" onclick="toggleCheckboxes(this, \'${divToUpdate}\')">', sortable: false, width: '30px'}
                <c:set var="first" value="false"/>
            </c:when>
            <c:otherwise>
                        {key: 'radio', label: '<input type="radio" disabled />', sortable: false, width: '30px'}
                <c:set var="first" value="false"/>
            </c:otherwise>
        </c:choose>
    </c:if>

    <c:choose>
        <c:when test="${!empty dynamicColumn}">
            <c:set var="dynamicColumn" value="${fn:substring(dynamicColumn, 1, fn:length(dynamicColumn) -1)}"/>
            <c:forEach var="col" items="${dynamicColumn}">
                <c:if test="${!first}">,</c:if>
                <c:set var="newAttr" value="${fn:replace(col, '|', ',')}"/>
                ${newAttr}
                <c:set var="first" value="false"/>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <c:forEach var="count" begin="0" end="10">
                <c:set var="attrName">column${count}</c:set>
                <c:if test="${!empty attributeMap[attrName]}">
                    <c:if test="${!first}">,</c:if>
                    <c:set var="s" value="${attributeMap[attrName]}"/>
                    <c:set var="labelMessageKey" value="${fn:substringAfter(attributeMap[attrName], 'label:')}"/>
                    <% String labelMessageKey = (String)jspContext.getAttribute("labelMessageKey");

                       try {
                           labelMessageKey = labelMessageKey.substring(labelMessageKey.indexOf("'")+1);
                           labelMessageKey = labelMessageKey.substring(0, labelMessageKey.indexOf("'")).trim();
                       } catch(Exception e) {}
                    %>
                    <c:set var="labelMessageKey" value="<%= labelMessageKey %>"/>
                    <c:set var="message"><ui:msgEscJS key="${labelMessageKey}"/></c:set>
                    <%-- allow hardcoded label --%>
                    <c:if test="${fn:startsWith(message, '???') && fn:endsWith(message, '???') && !fn:contains(labelMessageKey, '.')}">
                        <c:set var="message">${labelMessageKey}</c:set>
                    </c:if>    
                    <c:set var="finalMessage" value="${fn:replace(attributeMap[attrName], labelMessageKey, message)}"/>
                    ${finalMessage}
                    <c:set var="first" value="false"/>
                </c:if>
            </c:forEach>
        </c:otherwise>
    </c:choose>

            ];

    ${var}.columns = myColumnDefs;
    ${var}.useRp = ${useRp}
    ${var}.rowsPerPage = ${rowsPerPage};
    ${var}.checkbox = ${checkbox};
    ${var}.checkboxSelectSingle = ${checkboxSelectSingle};
    ${var}.customPreProcessor = ${customPreProcessor};

    <c:if test="${!empty width}">
        ${var}.width = '${width}';
    </c:if>
    <c:if test="${!empty height}">
        ${var}.height = '${height}';
    </c:if>
    <c:if test="${!empty checkboxId}">
        ${var}.key = '${checkboxId}';
    </c:if>
    <c:if test="${!empty sort}">
        ${var}.sort = '${sort}';
    </c:if>
    <c:if test="${!empty desc}">
        ${var}.desc = ${desc};
    </c:if>

        // NOTE: replaced by creating buttons outside and at the bottom of table
        // set buttons
        var buttons = new Array();
    <c:forEach var="count" begin="0" end="10">
        <c:set var="button">checkboxButton${count}</c:set>
        <c:set var="callback">checkboxCallback${count}</c:set>
        <c:if test="${!empty attributeMap[button]}">
            buttons.push({ name:'${attributeMap[button]}', callback:'${attributeMap[callback]}' });
        </c:if>
    </c:forEach>
        if (buttons.length > 0) {
            //${var}.buttons = buttons;
        }

        // set row link and popup dialog
        var ${var}_popupDialog = null;
    <c:if test="${!empty href && hrefDialog}">
        <c:if test="${!empty hrefDialogWindowName}">
            ${var}_popupDialog = new PopupDialog("${href}", "${hrefDialogTitle}", "${hrefDialogWindowName}");
        </c:if>
        <c:if test="${empty hrefDialogWindowName}">
            ${var}_popupDialog = new PopupDialog("${href}", "${hrefDialogTitle}");
        </c:if>
    </c:if>
    <c:if test="${!empty href}">
        ${var}.link = new Link("${href}", "${hrefParam}", ${hrefQuery}, ${var}_popupDialog);
        <c:if test="${!empty hrefSuffix}">
            ${var}.link.suffix = "${hrefSuffix}";
        </c:if>
        <c:if test="${!empty hrefPost}">
            ${var}.link.post = "${true}";
        </c:if>
    </c:if>
    <c:if test="${empty hrefDialogWidth}">${var}_popupDialog.width="${hrefDialogWidth}";</c:if>
    <c:if test="${empty hrefDialogHeight}">${var}_popupDialog.height="${hrefDialogHeight}";</c:if>
    ${var}.init();


        function toggleCheckboxes(checkbox, divToUpdate){
            $.each($("." + divToUpdate + "-checkbox-list"), function(i, v){
                if ($(checkbox).is(":checked")) {
                    $(v).prop("checked", true);
                } else {
                    $(v).prop("checked", false);
                }

                var tr = $(v).parent().parent().parent();
                if($(checkbox).prop("checked")){
                    $(tr).addClass("trSelected");
                    $("#"+divToUpdate+"_selectedIds").html($("#"+divToUpdate+"_selectedIds").html()+","+$(tr).attr("id").substring(3).replace(/__dot__/g, '.'));
                } else {
                    $(tr).removeClass("trSelected");
                    var removeString = $("#"+divToUpdate+"_selectedIds").html().replace(","+$(tr).attr("id").substring(3).replace(/__dot__/g, '.'), "");
                    $("#"+divToUpdate+"_selectedIds").html(removeString);
                }
            });
        }

        function toggleCheckbox(checkbox) {

            var tr = $('#' + checkbox).parent().parent().parent();
            var divToUpdate  = tr.parent().parent().attr("id");
            if($('#' + checkbox).prop("checked")){
                $(tr).addClass("trSelected");
                $("#"+divToUpdate+"_selectedIds").html($("#"+divToUpdate+"_selectedIds").html()+","+$(tr).attr("id").substring(3).replace(/__dot__/g, '.'));
            } else {
                $(tr).removeClass("trSelected");
                var removeString = $("#"+divToUpdate+"_selectedIds").html().replace(","+$(tr).attr("id").substring(3).replace(/__dot__/g, '.'), "");
                $("#"+divToUpdate+"_selectedIds").html(removeString);
            }
        }

        var ${divToUpdate}_trRemoved;
        var ${divToUpdate}_idRemoved;

        function ${divToUpdate}_toggleRadioButton(radio) {
            var tr = $('#' + radio).parent().parent().parent();

            if(radio != ${divToUpdate}_idRemoved) {
                $(tr).addClass("trSelected");
                $(${divToUpdate}_trRemoved).removeClass("trSelected");
            }

    ${divToUpdate}_trRemoved = tr;
    ${divToUpdate}_idRemoved = radio;
            }
</script>