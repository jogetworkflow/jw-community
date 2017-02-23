<%@ page import="org.joget.apps.app.service.MobileUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<c:set var="mobileView" value="<%= MobileUtil.isMobileView() %>"/>
<c:if test="${mobileView}">
    <c:set scope="request" var="dataList" value="${dataList}"/>
    <jsp:forward page="/WEB-INF/jsp/mobile/mDataListView.jsp"/>
</c:if>

<c:set scope="request" var="dataListId" value="${dataList.id}"/>
<style>
    .filters { text-align:right; font-size:smaller }
    .filter-cell{display:inline-block;padding-left:5px;}
</style>

<div class="dataList">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/footable/footable.min.js?build=<fmt:message key="build.number"/>"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/footable/responsiveTable.js?build=<fmt:message key="build.number"/>"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/js/footable/footable.core.min.css?build=<fmt:message key="build.number"/>" />    
    
    <c:set var="isQuickEditEnabled" value="<%= AppUtil.isQuickEditEnabled() %>"/>
    <c:if test="${isQuickEditEnabled}">
    <div class="quickEdit" style="display: none">
        <a href="<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/builder/${dataList.id}"/>" target="_blank"><i class="icon-edit"></i>  <fmt:message key="adminBar.label.list"/>: <c:out value="${dataList.name}"/></a>
    </div>
    </c:if>
    
    <c:catch var="dataListException">
        
        <c:set var="actionResult" value="${dataList.actionResult}" />
        <c:set var="redirected" value="false" />
        <c:if test="${!empty actionResult}">
            <c:if test="${!empty actionResult.message}">
                <script>
                    alert("<c:out value="${actionResult.message}"/>");
                </script>
            </c:if>
            <c:choose>
                <c:when test="${actionResult.type == 'REDIRECT' && actionResult.url == 'REFERER'}">
                    <c:set var="redirected" value="true" />
                    <script>
                        location.href = "<c:out value="${header['Referer']}"/>";
                    </script>
                </c:when>
                <c:when test="${actionResult.type == 'REDIRECT'  && !empty actionResult.url}">
                    <c:set var="redirected" value="true" />
                    <script>
                        location.href = "<c:out value="${actionResult.url}"/>";
                    </script>
                </c:when>
                <c:otherwise>   
                        
                </c:otherwise>
            </c:choose>
        </c:if>
        <c:if test="${!redirected}">
            <c:catch var="dataListBinderException">
                <c:set var="dataListRows" scope="request" value="${dataList.rows}"/>
            </c:catch>
            <c:set var="dataListSize" scope="request" value="${dataList.size}"/>
            <c:set var="dataListPageSize" scope="request" value="${dataList.pageSize}"/>
            <c:set var="decorator" scope="request" value="${dataList.primaryKeyDecorator}"/>

            <!-- set default button position if value is null -->
            <c:set var="buttonPosition" value="${dataList.actionPosition}" />

            <c:set var="buttonFloat" value="left" />
            <c:if test="${buttonPosition eq 'topRight' || buttonPosition eq 'bottomRight' || buttonPosition eq 'bothRight'}">
                <c:set var="buttonFloat" value="right" />
            </c:if>

            <!-- set checkbox position if value is null -->
            <c:set var="checkboxPosition" value="${dataList.checkboxPosition}" />

            <c:set var="selectionType" value="multiple" />
            <c:if test="${dataList.selectionType eq 'single'}">
                <c:set var="selectionType" value="single" />
            </c:if>

            <c:if test="${!empty dataListBinderException}">
                <%
                String exceptionMessage = "";
                Throwable cause = (Throwable)pageContext.findAttribute("dataListBinderException");
                while(cause.getCause() != null) {
                    cause = cause.getCause();
                }
                exceptionMessage = cause.getMessage();
                %>
                <div class="datalist-error"><c:out value="<%= exceptionMessage %>"/></div>
            </c:if>
            <c:if test="${!empty dataList.binder && !empty dataList.binder.properties.errorMsg}">
                <div class="datalist-error"><c:out value="${dataList.binder.properties.errorMsg}"/></div>
            </c:if>   

            <!-- Display Filters -->        
            <c:if test="${fn:length(dataList.filterTemplates) gt 1}">
                <form name="filters_${dataListId}" id="filters_${dataListId}" action="?" method="POST">
                    <div class="filters">
                        <c:forEach items="${dataList.filterTemplates}" var="template">
                            <span class="filter-cell">
                                ${template}
                            </span>
                        </c:forEach>
                         <span class="filter-cell">
                             <input type="submit" class="form-button btn button" value="<fmt:message key="general.method.label.show"/>"/>
                         </span>
                    </div>
                </form>
            </c:if>

            <!-- Display Main Table -->
            <form name="form_${dataListId}" action="?<c:out value="${queryString}" escapeXml="true"/>" method="POST">
                <!-- Display Buttons -->
                <c:if test="${buttonPosition eq 'topLeft' || buttonPosition eq 'topRight' || buttonPosition eq 'bothLeft' || buttonPosition eq 'bothRight'}">
                    <div class="actions top ${buttonFloat}">
                        <c:forEach items="${dataList.actions}" var="action">
                            <c:if test="${!(empty dataListRows[0] || checkboxPosition eq 'no') || action.visibleOnNoRecord}">
                                <c:set var="buttonConfirmation" value="" />
                                <c:if test="${!empty action.confirmation}">
                                    <c:set var="buttonConfirmation" value=" onclick=\"return showConfirm(this, '${fn:escapeXml(action.confirmation)}')\""/>
                                </c:if>
                                <button name="${dataList.actionParamName}" class="form-button btn button" value="${action.properties.id}" ${buttonConfirmation}><c:out value="${action.linkLabel}" escapeXml="true"/></button>
                            </c:if>
                        </c:forEach>
                    </div>
                </c:if>
                <div class="footable-buttons" style="display:none">
                    <button class="expandAll"><i></i> <fmt:message key="dbuilder.expandAll"/></button>
                    <button class="collapseAll"><i></i> <fmt:message key="dbuilder.collapseAll"/></button>
                    <span class="search_trigger"><fmt:message key="general.method.label.search"/> <i></i></span>
                </div>    
                <display:table id="${dataListId}" uid="${dataListId}" name="dataListRows" pagesize="${dataListPageSize}" class="xrounded_shadowed" export="true" decorator="decorator" excludedParams="${dataList.binder.primaryKeyColumnName}" requestURI="?" sort="external" partialList="true" size="dataListSize">
                    <c:if test="${checkboxPosition eq 'left' || checkboxPosition eq 'both'}">
                        <c:choose>
                            <c:when test="${selectionType eq 'single'}">
                                <display:column headerClass="select_radio" class="select_radio" property="radio" media="html" title="" />
                            </c:when>
                            <c:otherwise>
                                <display:column headerClass="select_checkbox" class="select_checkbox" property="checkbox" media="html" title="<input type='checkbox' onclick='toggleAll(this)' style='float:left;'/>" />
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                    <c:forEach items="${dataList.columns}" var="column">
                        <c:set var="columnLabel"><c:out value="${column.label}"/></c:set>
                        <c:set var="columnHiddenCss" value=""/>
                        <c:set var="columnMedia" value="all"/>
                        <c:choose> 
                            <c:when test="${column.hidden}">
                                <c:set var="columnHiddenCss" value=" column-hidden"/>
                                <c:if test="${column.properties.include_export ne 'true'}">
                                    <c:set var="columnMedia" value="html"/>
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <c:if test="${column.properties.exclude_export eq 'true'}">
                                    <c:set var="columnMedia" value="html"/>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                        <display:column
                            property="column(${column.name})"
                            title="${columnLabel}"
                            sortable="${column.sortable}"
                            headerClass="column_${column.name} ${columnHiddenCss}"
                            class="column_${column.name} ${columnHiddenCss}"
                            style="${column.style}"
                            media="${columnMedia}"
                            />
                    </c:forEach>
                    <c:if test="${!empty dataList.rowActions[0]}">
                        <c:set var="actionTitle" value="" />
                        <c:forEach items="${dataList.rowActions}" var="rowAction" begin="1">
                            <c:set var="actionTitle" value="${actionTitle}</th><th class=\"row_action\">" />
                        </c:forEach>
                        <display:column headerClass="row_action" class="row_action" property="actions" media="html" title="${actionTitle}"/>
                    </c:if>
                    <c:if test="${checkboxPosition eq 'right' || checkboxPosition eq 'both'}">
                        <c:choose>
                            <c:when test="${selectionType eq 'single'}">
                                <display:column headerClass="select_radio" class="select_radio" property="radio" media="html" title="" />
                            </c:when>
                            <c:otherwise>
                                <display:column headerClass="select_checkbox" class="select_checkbox" property="checkbox" media="html" title="<input type='checkbox' onclick='toggleAll(this)' style='float:left;'/>" />
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </display:table>

                <!-- Display Buttons -->
                <c:if test="${buttonPosition eq 'bottomLeft' || buttonPosition eq 'bottomRight' || buttonPosition eq 'bothLeft' || buttonPosition eq 'bothRight'}">
                    <div class="actions bottom ${buttonFloat}">
                        <c:forEach items="${dataList.actions}" var="action">
                            <c:if test="${!(empty dataListRows[0] || checkboxPosition eq 'no') || action.visibleOnNoRecord}">
                                <c:set var="buttonConfirmation" value="" />
                                <c:if test="${!empty action.confirmation}">
                                    <c:set var="buttonConfirmation" value=" onclick=\"return showConfirm(this, '${fn:escapeXml(action.confirmation)}')\""/>
                                </c:if>
                                <button name="${dataList.actionParamName}" class="form-button btn button" value="${action.properties.id}" ${buttonConfirmation}><c:out value="${action.linkLabel}" escapeXml="true"/></button>
                            </c:if>
                        </c:forEach>
                    </div>
                </c:if>
            </form>
        </c:if>    
    </c:catch>

    ${dataListException}
    <c:if test="${!empty dataListException}">
<pre>        
<%
Throwable t =(Throwable)pageContext.findAttribute("dataListException");      
t.printStackTrace(new java.io.PrintWriter(out));
%>                
</pre>
    </c:if>
    
</div>

<script>
    var popupActionDialog = null;
    
    DataListUtil = {
        submitForm: function(form) {
            var params = $(form).serialize();
            var queryStr = window.location.search;
            params = params.replace(/\+/g, " ");
            var newUrl = UrlUtil.mergeRequestQueryString(queryStr, params);
            window.location.href = "?" + newUrl;
            return false;
        }
    }
    $(document).ready(function() {
        $("#filters_${dataListId}").submit(function(e) {
            e.preventDefault();
            DataListUtil.submitForm(this);
        });
    });
    function toggleAll(element) {
        var table = $(element).closest("table");
        if ($(element).is(":checked")) {
            $(table).find("input[type=checkbox]").attr("checked", "checked");
        } else {
            $(table).find("input[type=checkbox]").removeAttr("checked");
        }
    }
    function dlPopupAction(element, message) {
        var url = $(element).attr("href");
        var showPopup = true;
        if (message != "") {
            showPopup = confirm(message);
        }
        if (showPopup) {
            if (popupActionDialog == null) {
                popupActionDialog = new PopupDialog(url);
            } else {
                popupActionDialog.src = url;
            }
            popupActionDialog.init();
        }
        return false;
    }
    function dlPostAction(element, message) {
        var url = $(element).attr("href");
        var showPopup = true;
        if (message != "") {
            showPopup = confirm(message);
        }
        if (showPopup) {
            var  orgAction = $(element).closest("form").attr("action");
            $(element).closest("form").find("input[type=checkbox]").removeAttr("checked");
            $(element).closest("form").attr("action", $(element).attr("href"));
            $(element).closest("form").submit();
            
            //reset the action
            $(element).closest("form").attr("action", orgAction);
        }
        return false;
    }
    function showConfirm(element, message) {
        var table = $(element).parent().parent().find('table');
        if ($(table).find("input[type=checkbox][name|=d]:checked", "input[type=radio][name|=d]:checked").length > 0) {
            return confirm(message);
        } else {
            alert("<fmt:message key="dbuilder.alert.noRecordSelected"/>");
            return false;
        }
    }
</script>