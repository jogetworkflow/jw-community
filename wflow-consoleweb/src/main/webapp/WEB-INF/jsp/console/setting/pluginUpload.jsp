<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.setting.plugin.upload.label.title"/>
    </div>

    <div id="main-body-content" style="text-align: left">
        <c:if test="${errorMessage != null}">
            <div class="form-errors">
                ${errorMessage}
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/web/console/setting/plugin/upload/submit" class="form blockui" enctype="multipart/form-data">
            <fieldset>
                <div class="form-row">
                    <label for="field1" class="upload"><fmt:message key="console.setting.plugin.upload.label.pluginFile"/></label>
                    <span class="form-input">
                        <input type="file" name="pluginFile" accept=".jar" />
                    </span>
                </div>
            </fieldset>    
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<ui:msgEscHTML key="general.method.label.upload"/>" />
            </div>
        </form>
    </div>
<commons:popupFooter />
