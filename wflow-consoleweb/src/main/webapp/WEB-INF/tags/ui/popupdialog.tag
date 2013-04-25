<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<%@ attribute name="var" %>
<%@ attribute name="src" required="true" %>
<%@ attribute name="title" %>

<c:if test="${empty var}"><c:set var="var" value="popupDialog"/></c:if>
<c:if test="${empty title}"><c:set var="title" value=" "/></c:if>

UI.base = "${pageContext.request.contextPath}";
var ${var} = new PopupDialog("${src}", "${title}");