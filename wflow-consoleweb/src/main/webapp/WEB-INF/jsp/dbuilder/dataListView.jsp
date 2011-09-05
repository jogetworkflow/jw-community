<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<c:set scope="request" var="dataListId" value="${dataList.id}"/>
<style>
    .filters { text-align:right; font-size:smaller }
    .filter-cell{display:inline-block;padding-left:5px;}
</style>

<div class="dataList">
    <c:catch var="dataListException">
        
        <c:set var="actionResult" value="${dataList.actionResult}" />
        <c:if test="${!empty actionResult}">
            <c:if test="${!empty actionResult.message}">
                <script>
                    alert("${actionResult.message}");
                </script>
            </c:if>
            <c:choose>
                <c:when test="${actionResult.type == 'REDIRECT' && actionResult.url == 'REFERER'}">
                    <script>
                        location.href = "${header['Referer']}";
                    </script>
                </c:when>
                <c:when test="${actionResult.type == 'REDIRECT'  && !empty actionResult.url}">
                    <script>
                        location.href = "${actionResult.url}";
                    </script>
                </c:when>
                <c:otherwise>   
                        
                </c:otherwise>
            </c:choose>
        </c:if>
        <c:set var="dataListRows" scope="request" value="${dataList.rows}"/>
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
        
        <!-- Display Filters -->
        
        <c:if test="${!empty dataList.filterTemplates[0]}">
            <form name="filters_${dataListId}" id="filters_${dataListId}" action="?" method="POST">
                <div class="filters">
                    <c:forEach items="${dataList.filterTemplates}" var="template">
                        <span class="filter-cell">
                            ${template}
                        </span>
                    </c:forEach>
                     <span class="filter-cell">
                         <input type="submit" value="<fmt:message key="general.method.label.show"/>"/>
                     </span>
                </div>
            </form>
        </c:if>

        <!-- Display Main Table -->
        <form name="form_${dataListId}" action="?" method="get">
            <!-- Display Buttons -->
            <c:if test="${buttonPosition eq 'topLeft' || buttonPosition eq 'topRight' || buttonPosition eq 'bothLeft' || buttonPosition eq 'bothRight'}">
                <div class="actions bottom ${buttonFloat}">
                    <c:forEach items="${dataList.actions}" var="action">
                        <c:if test="${!empty dataListRows[0] || action.visibleOnNoRecord}">
                            <c:set var="buttonConfirmation" value="" />
                            <c:if test="${!empty action.confirmation}">
                                <c:set var="buttonConfirmation" value=" onclick=\"return confirm('${action.confirmation}')\""/>
                            </c:if>
                            <button name="${dataList.actionParamName}" value="${action.properties.id}" ${buttonConfirmation}">${action.linkLabel}</button>
                        </c:if>
                    </c:forEach>
                </div>
            </c:if>
            
            <c:if test="${param.embed}">
                <input type="hidden" name="embed" id="embed" value="true"/>
            </c:if>
            <display:table id="${dataListId}" name="dataListRows" pagesize="${dataListPageSize}" class="xrounded_shadowed" export="true" decorator="decorator" excludedParams="${dataList.binder.primaryKeyColumnName}" requestURI="?" sort="external" partialList="true" size="dataListSize">
                <c:if test="${checkboxPosition eq 'left' || checkboxPosition eq 'both'}"><display:column property="checkbox" media="html" title="" /></c:if>
                <c:forEach items="${dataList.columns}" var="column">
                    <display:column
                        property="column(${column.name})"
                        title="${column.label}"
                        sortable="${column.sortable}"
                        />
                </c:forEach>
                <c:if test="${!empty dataList.rowActions[0]}">
                    <display:column property="actions" media="html" title="" />
                </c:if>
                <c:if test="${checkboxPosition eq 'right' || checkboxPosition eq 'both'}"><display:column property="checkbox" media="html" title="" /></c:if>
            </display:table>

            <!-- Display Buttons -->
            <c:if test="${buttonPosition eq 'bottomLeft' || buttonPosition eq 'bottomRight' || buttonPosition eq 'bothLeft' || buttonPosition eq 'bothRight'}">
                <div class="actions bottom ${buttonFloat}">
                    <c:forEach items="${dataList.actions}" var="action">
                        <c:if test="${!empty dataListRows[0] || action.visibleOnNoRecord}">
                            <c:set var="buttonConfirmation" value="" />
                            <c:if test="${!empty action.confirmation}">
                                <c:set var="buttonConfirmation" value=" onclick=\"return confirm('${action.confirmation}')\""/>
                            </c:if>
                            <button name="${dataList.actionParamName}" value="${action.properties.id}" ${buttonConfirmation}">${action.linkLabel}</button>
                        </c:if>
                    </c:forEach>
                </div>
            </c:if>
        </form>
    </c:catch>

    ${dataListException}
</div>

<script>
    DataListUtil = {
        submitForm: function(form) {
            var params = $(form).serialize();
            var queryStr = window.location.search;
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
</script>