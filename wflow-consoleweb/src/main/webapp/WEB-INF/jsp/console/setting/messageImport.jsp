<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.setting.message.import.label.title"/>
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

        <form method="post" action="${pageContext.request.contextPath}/web/console/setting/message/import/submit" class="form blockui" enctype="multipart/form-data">
            <fieldset>
                <div class="form-row">
                    <label for="localeFile" class="upload"><fmt:message key="console.setting.message.import.label.POFile"/></label>
                    <span class="form-input">
                        <input id="localeFile" type="file" name="localeFile"/>
                    </span>
                </div>
            </fieldset>    
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<ui:msgEscHTML key="console.setting.message.import.label.upload"/>" />
            </div>
        </form>
    </div>
<commons:popupFooter />
