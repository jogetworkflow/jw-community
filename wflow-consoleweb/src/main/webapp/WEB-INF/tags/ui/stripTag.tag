<%@ tag trimDirectiveWhitespaces="true" %>
<%@ tag import="org.joget.commons.util.StringUtil"%>
<%@ tag import="org.joget.workflow.util.WorkflowUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ attribute name="html" required="true" %>
<%@ attribute name="relaxed" required="false" %>
<%@ attribute name="processHashVariable" required="false" %>

<%= (!"true".equals(relaxed)) ? StringUtil.stripAllHtmlTag((("true".equals(processHashVariable)) ? WorkflowUtil.processVariable(html, null, null) : html)) : StringUtil.stripHtmlRelaxed((("true".equals(processHashVariable)) ? WorkflowUtil.processVariable(html, null, null) : html)) %>
