<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="utf-8"%>


<script type="text/javascript" src="${pageContext.request.contextPath}/js/json/ui.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/json/util.js"></script>

<div class="uview_body">
    <c:set var="formHtml" scope="request" value="${formHtml}"/>
    <c:set var="errorCount" scope="request" value="${errorCount}"/>
    <c:set var="submitted" scope="request" value="${submitted}"/>
    <c:set var="stay" scope="request" value="${stay}"/>
    
    <c:choose>
        <c:when test="${!stay && submitted && errorCount == 0}">
            <script type="text/javascript">
                $(document).ready(function(){
                    var setting = <ui:stripTag html="${setting}" relaxed="true"/>;
                    setting['result'] = '${jsonResult}';
                    if(<ui:stripTag html="${callback}"/>){
                        <ui:stripTag html="${callback}"/>(setting);
                    }
                });
            </script>
        </c:when>
        <c:otherwise>
            <jsp:include page="../client/app/formView.jsp" flush="true" />    
        </c:otherwise>
    </c:choose>    
    <div class="clearfix"></div>  
</div>