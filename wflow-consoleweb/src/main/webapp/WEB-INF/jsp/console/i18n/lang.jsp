<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

var ${name}_lang = {
    <c:forEach var="key" items="${keys}">
        '${key}' : '<fmt:message key="${key}"/>',
    </c:forEach>
    lang_file_name : '${name}.properties'
}

function get_${name}_msg(key){
    return ${name}_lang[key] ? ${name}_lang[key] : '??'+key+'??';
}