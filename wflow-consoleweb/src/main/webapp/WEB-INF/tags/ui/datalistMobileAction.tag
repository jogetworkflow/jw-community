<%@ tag trimDirectiveWhitespaces="true" %>
<%@ tag import="org.joget.apps.datalist.service.JsonUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ attribute name="action" required="true" type="java.lang.Object" %>
<%@ attribute name="row" required="true" type="java.lang.Object" %>
<%@ attribute name="menuId" required="true" type="java.lang.Object" %>

<%= JsonUtil.buildMobileActionLink(action, row, menuId) %>