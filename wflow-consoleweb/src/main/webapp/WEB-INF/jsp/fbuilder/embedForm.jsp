<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader /> 
        <c:set var="formHtml" scope="request" value="${formHtml}"/>
        <c:set var="errorCount" scope="request" value="${errorCount}"/>
        <c:set var="submitted" scope="request" value="${submitted}"/>
        <c:set var="stay" scope="request" value="${stay}"/>
        <jsp:include page="../client/app/formView.jsp" flush="true" />
        
        <c:if test="${!stay && submitted && errorCount == 0}">
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
        </c:if>
<commons:popupFooter /> 
