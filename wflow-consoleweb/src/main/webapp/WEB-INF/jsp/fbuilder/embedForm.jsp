<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="isQuickEditEnabled" value="<%= AppUtil.isQuickEditEnabled() %>"/>
<c:set var="isAdmin" value="<%= WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) %>"/>

<commons:popupHeader /> 
    <c:if test="${isQuickEditEnabled && isAdmin}">    
        <script src="${pageContext.request.contextPath}/js/adminBar.js"></script>
        <script>
            AdminBar.cookiePath = '${pageContext.request.contextPath}/';
        </script>  
    </c:if>
        <c:choose>
            <c:when test="${!stay && submitted && errorCount == 0}">
                <script type="text/javascript">
                    var setting = <ui:stripTag html="${setting}"  relaxed="true"/>;
                    setting['result'] = '${jsonResult}';
                    if (window.parent && window.parent.<ui:stripTag html="${callback}"/>){
                        window.parent.<ui:stripTag html="${callback}"/>(setting);
                    }else if (window.opener && window.opener.<ui:stripTag html="${callback}"/>){
                        window.opener.<ui:stripTag html="${callback}"/>(setting);
                    }else if(<ui:stripTag html="${callback}"/>){
                        <ui:stripTag html="${callback}"/>(setting);
                    }
                    window.close();
                </script>
            </c:when>
            <c:otherwise>
                <c:set var="formHtml" scope="request" value="${formHtml}"/>
                <c:set var="errorCount" scope="request" value="${errorCount}"/>
                <c:set var="submitted" scope="request" value="${submitted}"/>
                <c:set var="stay" scope="request" value="${stay}"/>
                <c:set var="readonly" scope="request" value="${readonly}"/>
                <jsp:include page="../client/app/formView.jsp" flush="true" />
            </c:otherwise> 
        </c:choose>        
<commons:popupFooter /> 
