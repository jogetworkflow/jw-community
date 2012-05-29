<%@tag import="java.net.URLEncoder"%>
<%@tag import="org.displaytag.util.ParamEncoder"%>
<%@tag description="Displays a DataList" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@attribute name="id" required="true"%>
<%@attribute name="jsonDef" required="false"%>

<c:set var="queryStr" value=""/>
<c:set var="jsonParam"><%= new ParamEncoder(id).encodeParameterName("json") %></c:set>
<c:if test="${!empty jsonDef && empty param[jsonParam]}">
    <c:set var="jsonEncoded"><%= URLEncoder.encode(jsonDef, "UTF-8") %></c:set>
    <c:set var="queryStr" value="${jsonParam}=${jsonEncoded}"/>    
</c:if>

<jsp:include page="/web/client/app/~~/1/datalist/${id}?${queryStr}" flush="true" />

