<%@ tag trimDirectiveWhitespaces="true" %>
<%@ tag import="org.joget.commons.util.StringUtil"%>
<%@ tag import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ attribute name="html" required="true" %>
<%@ attribute name="relaxed" required="false" %>
<%@ attribute name="processHashVariable" required="false" %>
<%@ attribute name="appDef" required="false" type="org.joget.apps.app.model.AppDefinition" %>

<%= (!"true".equals(relaxed)) ? StringUtil.stripAllHtmlTag((("true".equals(processHashVariable)) ? AppUtil.processHashVariable(html, null, null, null, appDef) : html)) : StringUtil.stripHtmlRelaxed((("true".equals(processHashVariable)) ? AppUtil.processHashVariable(html, null, null, null, appDef) : html)) %>
