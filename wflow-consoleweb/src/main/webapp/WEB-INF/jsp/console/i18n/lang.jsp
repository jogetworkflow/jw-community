<% response.setHeader("Cache-Control","private"); %>
<% response.setContentType("text/javascript;charset=UTF-8"); %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

var <c:out value="${name}"/>_lang = {
    <c:forEach var="key" items="${keys}">
        <fmt:message key="${key}" var="keyValue"/>
        '${key}' : '<c:out value='${fn:replace(keyValue, "\'", "\\\\\'")}' escapeXml="false"/>',
    </c:forEach>
<c:if test="${!empty bundle}">
    <c:forEach var="key" items="${bundle.keySet()}">
        '${key}' : '<c:out value='${fn:replace(bundle.getString(key), "\'", "\\\\\'")}' escapeXml="false"/>',
    </c:forEach>
</c:if>    
    lang_file_name : '<c:out value="${name}"/>.properties'
}

function get_<c:out value="${name}"/>_msg(key, args){
    var msg = (<c:out value="${name}"/>_lang[key] !== undefined) ? <c:out value="${name}"/>_lang[key] : '??'+key+'??';
    if (args !== undefined && args.length > 0) {
        for (var i in args) {
            msg = msg.replace('{'+i+'}', args[i]);
        }
    }
    return msg;
}