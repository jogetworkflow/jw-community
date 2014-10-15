<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.process.config.label.updateProcess"/>
    </div>

    <div id="main-body-content" style="text-align: left">
        <form method="post" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/package/upload/submit" enctype="multipart/form-data">
            <c:if test="${!empty errorMessage}">
                <div class="form-errors">${errorMessage}</div>
            </c:if>
            <div class="form-row">
                <label for="field1" class="upload"><fmt:message key="console.process.config.label.updateProcess.processXpdlFile"/></label>
                <span class="form-input">
                    <input type="file" name="packageXpdl"/>
                </span>
            </div>
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<fmt:message key="general.method.label.upload"/>" />
            </div>
        </form>
    </div>
            
<commons:popupFooter />