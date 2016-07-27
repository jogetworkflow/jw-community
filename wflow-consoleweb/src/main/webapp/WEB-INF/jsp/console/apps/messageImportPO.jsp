<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.app.message.import.po.label.title"/>
    </div>

    <div id="main-body-content">
        <c:if test="${errorMessage != null}">
            <div class="form-error">
                ${errorMessage}
            </div>
        </c:if>

        <c:if test="${errorList != null}">
            <div class="form-error">
                <div style="borderx: 1px dashed #aaa; backgroundx: #e5e5e5; padding: 0.5em">
                <c:forEach var="error" items="${errorList}">
                    <div style="border-bottomx: 1px solid #ccc; margin-bottom: 5px">${error}</div>
                </c:forEach>
                </div>
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/message/importpo/submit" class="form" enctype="multipart/form-data">
            <div class="form-row">
                <label for="localeFile" class="upload"><fmt:message key="console.app.message.import.po.label.poFile"/></label>
                <span class="form-input">
                    <input id="localeFile" type="file" name="localeFile"/>
                </span>
            </div>
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<fmt:message key="console.app.message.import.po.label.upload"/>" />
            </div>
        </form>
    </div>
<commons:popupFooter />
