<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.apps.datalist.model.DataList"%>
<%@ page import="org.joget.apps.datalist.model.DataListColumn"%>
<%@ page import="org.joget.apps.datalist.model.DataListColumnFormat"%>
<%@ page import="org.joget.commons.util.StringUtil"%>
<%@ page import="java.util.Collection"%>
<%@ page import="org.displaytag.util.ParamEncoder"%>
<%@ page import="org.displaytag.tags.TableTagParameters"%>

<c:set var="landingPage" value="landing"/>
<!DOCTYPE html>
<html class="ui-mobile" manifest="${pageContext.request.contextPath}/web/mobilecache/${appId}/${userview.properties.id}">
    <head>
        <title>
            <c:set var="html">
                ${userview.properties.name}
                <c:if test="${!empty userview.current}">
                     &nbsp;&gt;&nbsp; ${userview.current.properties.label}
                </c:if>
            </c:set>
            <ui:stripTag html="${html}"/>
        </title>
        <jsp:include page="mScripts.jsp" flush="true"/>

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
                        location.href = "<c:out value="${header['Referer']}"/>";
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
        </c:catch>

    </head>
    <body class="ui-mobile-viewport">

        <div id="userview" data-role="page" data-url="userview" tabindex="0" style="min-height: 377px; ">

            <div data-role="header" data-position="fixed" role="banner" style="top: 0px; ">
                <c:if test="${!empty menuId && menuId != landingPage}">
                    <a href="${pageContext.request.contextPath}/web/mobile/${appId}/${userview.properties.id}/<c:out value="${key}"/>/${landingPage}" data-icon="home" data-direction="reverse"><fmt:message key="console.header.menu.label.home"/></a>
                </c:if>
                <h1 class="ui-title" tabindex="0" role="heading" aria-level="1">
                <c:choose>
                    <c:when test="${!empty userview.setting.theme.header}">
                        <ui:stripTag html="${userview.setting.theme.header}"/>
                    </c:when>
                    <c:otherwise>
                        <ui:stripTag html="${userview.properties.name}"/>
                    </c:otherwise>
                </c:choose>                    
                </h1>
                <c:if test="${empty menuId || menuId == landingPage}">    
                    <c:choose>
                        <c:when test="${isAnonymous}">
                            <a href="${pageContext.request.contextPath}/web/mlogin/${appId}/${userview.properties.id}/<c:out value="${key}"/>" data-icon="gear" data-theme="a"><span id="loginText"><fmt:message key="console.login.label.login"/></span></a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/j_spring_security_logout" data-icon="back" data-theme="a" data-direction="reverse"><span id="logoutText"><c:out value="${userview.properties.logoutText}"/></span></a>
                        </c:otherwise>
                    </c:choose>                            
                </c:if>
            </div>
            <div id="logo"></div>
            <div data-role="content" class="ui-content" role="main">
                    
                <c:set var="columns" value="${dataList.columns}"/>
                
                <%-- Get first action as primary link --%>
                <c:forEach items="${columns}" var="column">
                    <c:if test="${empty firstDataListAction && !empty column.action}">
                            <c:set var="firstDataListAction" value="${column.action}"/>
                    </c:if>
                </c:forEach>
                <c:if test="${empty firstDataListAction && !empty dataList.rowActions && !empty dataList.rowActions[0]}">
                    <c:set var="firstDataListAction" value="${dataList.rowActions[0]}"/>
                </c:if>
                            
                <%-- Get second action as secondary link --%>
                <c:forEach items="${dataList.rowActions}" var="rowAction">
                    <c:choose>
                        <c:when test="${empty secondDataListAction || !empty rowAction}">
                            <c:set var="secondDataListAction" value="${rowAction}"/>
                        </c:when>
                    </c:choose>
                </c:forEach>
                <c:if test="${!empty firstDataListAction && empty secondDataListAction}">
                    <c:set var="secondDataListAction" value="${firstDataListAction}"/>
                </c:if>
                
                <%-- Determine data-split-icon --%>
                <c:set var="dataSplitIcon" value="gear"/>
                <c:if test="${fn:contains(secondDataListAction, 'Delete')}">
                    <c:set var="dataSplitIcon" value="delete"/>
                </c:if>
                            
                <%-- Calculate paging --%>
                <c:set var="dataListId" value="${dataList.id}"/>
                <c:set var="paramPage" value="<%= new ParamEncoder(pageContext.findAttribute(\"dataListId\").toString()).encodeParameterName(TableTagParameters.PARAMETER_PAGE) %>"/>
                <c:set var="currentPage" value="${param[paramPage]}"/>
                <c:if test="${empty currentPage}">
                    <c:set var="currentPage" value="${1}"/>
                </c:if>
                <c:set var="previousPage" value="${currentPage - 1}"/>
                <c:set var="nextPage" value="${currentPage + 1}"/>
                <c:set var="totalPages"><fmt:formatNumber type="number" maxFractionDigits="0" value="${(dataList.size / dataList.pageSize)}" /></c:set>
                <c:set var="hasNextPage" value="${(currentPage*1 < totalPages*1)}"/> <%-- multiply by 1 to compare as number instead of string --%>               

                <%-- Display datalist --%>
                <c:catch var="dataListBinderException">
                    <c:set var="dataListRows" scope="request" value="${dataList.rows}"/>
                </c:catch>
                <ul id="dataList" data-role="listview" data-filter="false" data-inset="true" data-split-icon="${dataSplitIcon}" data-split-theme="d" class="ui-listview" data-filter-theme="d"data-theme="d" data-divider-theme="d">
                    <li data-role="list-divider"><c:out value="${dataList.name}"/></li>
                    <c:forEach items="${dataListRows}" var="row" varStatus="status">
                        <li>
                            <c:if test="${!empty firstDataListAction}"><a href="<ui:datalistMobileAction action='${firstDataListAction}' row='${row}' menuId='${menuId}' />"></c:if>
                            <c:set var="column" value="${columns[0]}"/>
                            <c:set var="cellValue" value="${row[columns[0].name]}"/>
                            <c:set var="formattedValue" value="<%= formatColumn(pageContext) %>"/>
                            <h4><ui:stripTag html="${formattedValue}"/></h4>
                            <p>
                            <c:forEach var="column" items="${columns}" varStatus="cStatus">
                                <c:if test="${cStatus.index > 0}">
                                    <c:if test="${!columns[cStatus.index].hidden}">
                                        <c:set var="cellLabel" value="${columns[cStatus.index].label}"/>
                                        <c:set var="cellValue" value="${row[columns[cStatus.index].name]}"/>
                                        <c:if test="${!empty cellValue}">
                                            <c:set var="cellCleanValue" value="<%= formatColumn(pageContext) %>"/>
                                            <c:if test="${!empty cellLabel}"><c:out value="${cellLabel}"/>:</c:if> ${cellCleanValue}
                                        </c:if>
                                        <br>
                                    </c:if>    
                                </c:if>
                            </c:forEach>
                            </p>
                            <c:if test="${!empty firstDataListAction}"></a></c:if>
                            <c:if test="${!empty secondDataListAction}">
                                <c:set var="link"><ui:datalistMobileAction action='${secondDataListAction}' row='${row}' menuId='${menuId}' /></c:set>
                                <c:set var="onClickCode" value="$.mobile.changePage('${link}')"/>
                                <c:set var="confirmation" value=""/>
                                <c:if test="${!empty secondDataListAction.confirmation}">
                                    <c:set var="onClickCode" value=" if (confirm('${secondDataListAction.confirmation}')) { ${onClickCode} }"/>
                                </c:if>
                                <a href="#" onclick="<c:out value="${onClickCode}"/>"><c:out value="${secondDataListAction.linkLabel}"/></a>
                            </c:if>
                        </li>
                    </c:forEach>    
                    <c:if test="${!empty dataListBinderException}">
                    <%
                        String exceptionMessage = "";
                        Throwable cause = (Throwable) pageContext.findAttribute("dataListBinderException");
                        while (cause.getCause() != null) {
                            cause = cause.getCause();
                        }
                        exceptionMessage = cause.getMessage();
                    %>                        
                        <li>
                            <c:out value="<%= exceptionMessage %>"/>
                        </li>
                    </c:if>
                </ul>    
                    
                <%-- Display paging buttons --%>
                <div class="buttons">
                    <c:if test="${currentPage > 1}">
                        <c:url var="previousUrl" value="${menuId}?${paramPage}=${previousPage}" />
                        <button class="buttonPrevious" onclick="$.mobile.changePage('${previousUrl}')">&lt;&lt;</button>
                    </c:if>
                    <c:if test="${hasNextPage}">
                        <c:url var="nextUrl" value="${menuId}?${paramPage}=${nextPage}" />
                        <button class="buttonNext" onclick="$.mobile.changePage('${nextUrl}')">&gt;&gt;</button>
                    </c:if>
                </div>
            </div>		

        </div>

        <div class="ui-loader" style="top: 332px; "><h1><fmt:message key="mobile.apps.loading"/></h1></div>
        <jsp:include page="mFooter.jsp" flush="true" />   
    </body>    
</html>

<%!
    protected String formatColumn(PageContext pageContext) {
        DataListColumn column = (DataListColumn)pageContext.findAttribute("column");
        DataList dataList = (DataList)pageContext.findAttribute("dataList");
        Object row = pageContext.findAttribute("row");
        Object result = pageContext.findAttribute("cellValue");
        Collection<DataListColumnFormat> formats = column.getFormats();
        if (formats != null) {
            for (DataListColumnFormat format : formats) {
                if (format != null) {
                    result = format.format(dataList, column, row, result);
                }
            }
        }

        String text = (result != null) ? result.toString() : null;
        String cleanText = cleanText(text);
        return cleanText;
    }
    
    protected String cleanText(String text) {
        if (text == null) {
            return text;
        }
        String cleanText = StringUtil.stripHtmlTag(text, new String[]{ "a", "span" });
        return cleanText;
    }
%>
