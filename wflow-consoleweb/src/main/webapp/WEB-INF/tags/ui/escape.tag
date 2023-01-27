<%@ tag trimDirectiveWhitespaces="true" %>
<%@ tag import="org.joget.commons.util.StringUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="format" required="true" %>

<%= StringUtil.escapeString(value, format, null) %>
