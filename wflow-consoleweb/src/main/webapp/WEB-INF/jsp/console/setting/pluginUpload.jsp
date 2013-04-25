<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.setting.plugin.upload.label.title"/>
    </div>

    <div id="main-body-content" style="text-align: left">
        <c:if test="${errorMessage != null}">
            <div class="form-error">
                ${errorMessage}
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/web/console/setting/plugin/upload/submit" class="form" enctype="multipart/form-data">
            <div class="form-row">
                <label for="field1" class="upload"><fmt:message key="console.setting.plugin.upload.label.pluginFile"/></label>
                <span class="form-input">
                    <input type="file" name="pluginFile"/>
                </span>
            </div>
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<fmt:message key="general.method.label.upload"/>" />
            </div>
        </form>
    </div>
<commons:popupFooter />
