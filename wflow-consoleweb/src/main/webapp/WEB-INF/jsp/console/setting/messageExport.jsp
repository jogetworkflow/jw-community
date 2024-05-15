<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.setting.message.export.label"/>
    </div>

    <div id="main-body-content">
        <form id="exportMessage" action="${pageContext.request.contextPath}/web/console/setting/message/export/submit" method="POST" cssClass="form blockui">
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.setting.message.common.label.locale"/> <span class="mandatory">*</span></label>
                    <span class="form-input">
                        <select id="locale" name="locale" class="<c:if test="${!empty errors}">form-input-error</c:if>">
                            <c:forEach var="locale" items="${localeList}">
                                <option value="${locale}" >${locale}</option>
                            </c:forEach>
                        </select>   
                    </span>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="submit" value="<ui:msgEscHTML key="console.setting.message.export.label.export"/>"/>
            </div>
        </form>
    </div>

    <script type="text/javascript">
       $(document).ready(function() {
            $("#exportMessage").submit(function(event) {
                closeDialog();
            });
        });

        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
    </script>
    
<commons:popupFooter />