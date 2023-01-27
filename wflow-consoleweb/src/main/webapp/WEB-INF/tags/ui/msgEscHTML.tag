<%@ tag trimDirectiveWhitespaces="true" %>
<%@ tag import="org.joget.commons.util.StringUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ attribute name="key" required="true" %>
<fmt:message key="${key}" var="keyValue"/>
<c:out value="${fn:replace(keyValue, '\"', '&quot;' )}" escapeXml="true"/>