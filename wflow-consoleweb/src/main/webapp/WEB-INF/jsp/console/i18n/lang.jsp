<% response.setHeader("Cache-Control","private"); %>
<% response.setContentType("text/javascript;charset=UTF-8"); %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

var <c:out value="${name}"/>_lang = {
    <c:forEach var="key" items="${keys}">
        '${key}' : '<fmt:message key="${key}"/>',
    </c:forEach>
    lang_file_name : '<c:out value="${name}"/>.properties'
}

function get_<c:out value="${name}"/>_msg(key){
    return <c:out value="${name}"/>_lang[key] ? <c:out value="${name}"/>_lang[key] : '??'+key+'??';
}