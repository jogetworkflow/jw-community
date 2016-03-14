<%@ tag trimDirectiveWhitespaces="true" %>
<%@ tag import="java.net.URLDecoder"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ attribute name="value" required="true" %>

<%= URLDecoder.decode(value, "UTF-8") %>
