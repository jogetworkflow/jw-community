<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<script>

    autoDetectJSLibrary(typeof jQuery == 'undefined', '${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/js/jquery/jquery-1.4.4.min.js');

    function autoDetectJSLibrary(jsObjUndefined, src){
        if (jsObjUndefined) {
            var objHead = window.document.getElementsByTagName('head')[0];
            var objScript = window.document.createElement('script');
            objScript.src = src;
            objScript.type = 'text/javascript';
            objHead.appendChild(objScript);
        }
    }
    
</script>

<c:set var="id" value="1"/>
<c:if test="${!empty param.id}">
    <c:set var="id" value="${param.id}"/>
</c:if>

<c:set var="rowsPerPage" value="5"/>
<c:if test="${!empty param.rows}">
    <c:set var="rowsPerPage" value="${param.rows}"/>
</c:if>

<c:set var="packageId" value="-1"/>
<c:if test="${!empty param.packageId}">
    <c:set var="packageId" value="${param.packageId}"/>
</c:if>

<c:if test="${!empty param.divId}">
    <c:set var="divId" value="${param.divId}"/>
</c:if>

<c:set var="loginCallback" value="loginCallback"/>
<c:if test="${!empty param.loginCallback}">
    <c:set var="loginCallback" value="${param.loginCallback}"/>
</c:if>

<c:set var="login" value="-1"/>
<c:if test="${!empty param.login}">
    <c:set var="login" value="${param.login}"/>
</c:if>

<div id="loading_${id}" style="display:none">
    <center><img src="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/images/v3/portlet_loading.gif"/></center>
</div>
    
<script type="text/javascript" src="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/js/json/util.js"></script>
<script>

    var loginUrlPath_${id} = "${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}";
   
    function doInboxLogin_${id}(){
        
        var loginCallback_${id} = {
            success: function(){
                $('#loading_${id}').show();
                $('#${divId}').html( $('#loading_${id}').html());
                var param = 'id='+${id}+'&rows='+${rowsPerPage}+'&divId=${divId}&packageId=${packageId}&loginCallback=${loginCallback}';
                var url = '';
                
                if(${login} ==true){
                    url = '/web/js/client/processList.js?';
                }else{
                    url = '/web/js/client/inbox.js?';
                }
               
                url +=param;
                
                $.getScript(loginUrlPath_${id}+url,null);

                if(typeof ${loginCallback} != "undefined" && ${loginCallback} !='loginCallback'){
                     ${loginCallback}.success();
                }
            }
        };

        AssignmentManager.login(loginUrlPath_${id}, $('#j_username_${id}').val(), $('#j_password_${id}').val(),loginCallback_${id});
    }
</script>


<form id="loginForm_${id}" name="loginForm" action="javascript: doInboxLogin_${id}();" method="POST">
    <table>
        <tr><td><fmt:message key="console.login.label.username" />: </td><td><input type='text' id='j_username_${id}' name='j_username_${id}' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
        <tr><td><fmt:message key="console.login.label.password" />:</td><td><input type='password' id='j_password_${id}' name='j_password_${id}'></td></tr>
    </table>

    <div id="main-body-actions">
        <input name="submit" type="submit" value="<fmt:message key="console.login.label.login" />" />
    </div>
</form>