<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<%@ tag import="org.joget.commons.util.TimeZoneUtil"%>
<%@ tag import="org.joget.apps.app.service.AppUtil"%>
<%@ tag trimDirectiveWhitespaces="true" %>

<%@ attribute name="date" rtexprvalue="true" required="true" type="java.util.Date" %>
<%@ attribute name="gmt" required="false" %>
<%@ attribute name="format" required="false" %>

<%= TimeZoneUtil.convertToTimeZone(date, gmt, (format==null?AppUtil.getAppDateFormat():format)) %>
