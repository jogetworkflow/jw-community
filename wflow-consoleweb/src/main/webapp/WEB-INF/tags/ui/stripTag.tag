<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<%@ tag import="org.joget.commons.util.StringUtil"%>

<%@ attribute name="html" required="true" %>

<%= StringUtil.stripAllHtmlTag(html) %>
