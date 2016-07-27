<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.app.message.generate.po.label.title"/>
    </div>

    <div id="main-body-content">
        <div class="form-row">
            <label class="upload" for="locale"><fmt:message key="console.app.message.common.label.locale"/></label>
            <span class="form-input">
                <select id="locale" name="locale">
                    <c:forEach var="l" items="${localeList}">
                        <option value="${l}" <c:if test="${l eq locale}">selected="selected"</c:if>>${l}</option>
                    </c:forEach>
                </select>
            </span>
        </div>
        <div class="form-buttons">
            <input type="button" id="generate" value="<fmt:message key="console.app.message.generate.po.label.generate"/>" class="form-button" />
        </div>
    </div>

    <script type="text/javascript">
        $(document).ready(function(){
            $("#generate").click(function(){
                document.location = '${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/message/generatepo/download?locale='+$("#locale").val();
            });
        });
    </script>
<commons:popupFooter />
