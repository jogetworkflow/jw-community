<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<%@ tag import="org.joget.commons.util.StringUtil"%>

<%@ attribute name="html" required="true" %>
<%@ attribute name="relaxed" required="false" %>

<%= (!"true".equals(relaxed)) ? StringUtil.stripAllHtmlTag(html) : StringUtil.stripHtmlRelaxed(html) %>
