<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<style>
    #more_${dataListId} {border:none; border-top:solid 1px silver;margin-top:30px}
    #more_${dataListId} legend {cursor:pointer; text-decoration:underline; color:blue}
    #more_${dataListId} #advanced_${dataListId} {display:none}
    .filters { text-align:right; font-size:smaller }
</style>

<div class="dataList">
    <c:catch var="dataListException">
        <!-- Display Filters -->
        <c:set var="filterHeader"><%= org.joget.apps.datalist.model.DataListFilter.FILTER_HEADER%></c:set>
        <c:if test="${!empty properties.textfieldFilterMap || !empty properties.selectBoxFilterMap}">
            <form name="filters_${dataListId}" id="filters_${dataListId}" action="?" method="POST">
                <div class="filters">
                    <c:if test="${!empty properties.textfieldFilterMap}">
                        <fmt:message key="dbuilder.search"/>
                        <input type="textfield" name="${properties.filterValueParam}" value="${properties.filterValueParamValue}" />
                        <select name="${properties.filterNameParam}">
                            <c:forEach items="${properties.textfieldFilterMap}" var="mapEntry">
                                <c:set var="filterNameSelected" value="" />
                                <c:if test="${properties.filterNameParamValue == mapEntry.key}">
                                    <c:set var="filterNameSelected" value=" selected" />
                                </c:if>
                                <option value="${mapEntry.key}" ${filterNameSelected}>${mapEntry.value}</option>
                            </c:forEach>
                        </select>
                    </c:if>
                    <c:if test="${!empty properties.selectBoxFilterMap}">
                        <fmt:message key="dbuilder.filter"/>
                        <select name="${properties.filterOptionParam}">
                            <option value="">---</option>
                            <c:forEach items="${properties.selectBoxFilterMap}" var="mapEntry">
                                <c:choose>
                                    <c:when test="${mapEntry.key == filterHeader}">
                                        <optgroup label="${mapEntry.value}"></optgroup>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="filterOptionSelected" value="" />
                                        <c:if test="${properties.filterOptionParamValue == mapEntry.key}">
                                            <c:set var="filterOptionSelected" value=" selected" />
                                        </c:if>
                                        <option value="${mapEntry.key}" ${filterOptionSelected}>${mapEntry.value}</option>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </select>
                    </c:if>
                </div>
            </form>
        </c:if>

        <!-- Display Main Table -->
        <form name="form_${dataListId}" action="?" method="get">
            <c:if test="${param.embed}">
                <input type="hidden" name="embed" id="embed" value="true"/>
            </c:if>
            <c:set var="dataListRows" scope="request" value="${properties.dataListRows}"/>
            <c:set var="dataListSize" scope="request" value="${properties.dataListSize}"/>
            <c:set var="dataListPageSize" scope="request" value="${properties.dataListPageSize}"/>
            <c:set var="decorator" scope="request" value="${properties.decorator}"/>
            <c:set var="dataList" scope="request" value="${properties.dataList}"/>
            <display:table id="${dataListId}" name="dataListRows" pagesize="${dataListPageSize}" class="xrounded_shadowed" export="true" decorator="decorator" excludedParams="${properties.dataList.binder.primaryKeyColumnName}" requestURI="?" sort="external" partialList="true" size="dataListSize">
                <display:column property="checkbox" media="html" title="" />
                <c:forEach items="${properties.dataList.columns}" var="column">
                    <display:column
                        property="column(${column.name})"
                        title="${column.label}"
                        sortable="${column.sortable}"
                        />
                </c:forEach>
                <c:if test="${!empty properties.dataList.rowActions[0]}">
                    <display:column property="actions" media="html" title="" />
                </c:if>
            </display:table>

            <!-- Display Buttons -->
            <c:if test="${!empty properties.dataListRows[0]}">
                <div class="actions">
                    <c:forEach items="${properties.dataList.actions}" var="action">
                        <c:set var="buttonConfirmation" value="" />
                        <c:if test="${!empty action.confirmation}">
                            <c:set var="buttonConfirmation" value=" onclick=\"return confirm('${action.confirmation}')\""/>
                        </c:if>
                        <button name="action" value="${action.properties.id}" ${buttonConfirmation}">${action.label}</button>
                    </c:forEach>
                </div>
            </c:if>
        </form>
<%--
        <c:if test="${!empty properties.json}">
            <!-- Advanced Stuff -->
            <fieldset id="more_${dataListId}">
                <legend>Debug</legend>
                <c:if test="${!empty properties.dataListError}"><div id="error">${properties.dataListError}</div></c:if>
                <div id="advanced_${dataListId}">
                    <h3>JSON definition</h3>
                    <form method="get" action="?">
                        <textarea id="json" name="${properties.jsonParam}" cols="80" rows="24">${properties.json}</textarea>
                    </form>
                </div>
            </fieldset>
        </c:if>
--%>
    </c:catch>

    ${dataListException}
</div>

<script>
    DataListUtil = {
        mergeUrlParams: function(q1, q2) {
            var urlParams = {};
            (function () {
                var e,
                r = /([^&=]+)=?([^&]*)/g,
                d = function (s) { return decodeURIComponent(s); };
                //q = window.location.search.substring(1);
                if (q1[0] == '?') {
                    q1 = q1.substring(1);
                }
                while (e = r.exec(q1))
                    urlParams[d(e[1])] = d(e[2]);
                if (q2[0] == '?') {
                    q2 = q2.substring(1);
                }
                while (e = r.exec(q2))
                    urlParams[d(e[1])] = d(e[2]);
            })();
            var queryStr = "?";
            for (param in urlParams) {
                queryStr += param + "=" + urlParams[param] + "&";
                //                queryStr += param + "=" + encodeURIComponent(urlParams[param]) + "&";
            }
            return queryStr;
        },

        submitFilters: function(id) {
            var params = $(id).serialize();
            var queryStr = window.location.search;
            var newUrl = DataListUtil.mergeUrlParams(queryStr, params);
            window.location.href = newUrl;
        }
    }
    $(document).ready(function() {
        $("#filters_${dataListId}").submit(function(e) {
            e.preventDefault();
            DataListUtil.submitFilters(this);
        });
        $("#filters_${dataListId} select").change(function(e) {
            DataListUtil.submitFilters($("#filters_${dataListId}"));
        });
        $("#more_${dataListId} legend").click(function() {
            $("#advanced_${dataListId}").toggle();
        });
    });
</script>