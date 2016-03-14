<% response.setHeader("Cache-Control","private"); %>
<% response.setContentType("text/javascript;charset=UTF-8"); %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

var ${name}_lang = {
    <c:forEach var="key" items="${keys}">
        '${key}' : '<fmt:message key="${key}"/>',
    </c:forEach>
    lang_file_name : '${name}.properties'
}

function get_${name}_msg(key){
    return ${name}_lang[key] ? ${name}_lang[key] : '??'+key+'??';
}