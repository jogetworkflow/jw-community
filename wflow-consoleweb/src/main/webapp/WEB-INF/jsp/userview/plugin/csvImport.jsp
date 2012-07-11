<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:if test="${requestParameters.isPreview eq 'true'}">
    <script>
        $(document).ready(function() {
            $(".form-button").attr("disabled", "disabled");
        });
    </script>
</c:if>

<div class="viewCsvImport-body-content">
    <c:choose>
        <c:when test="${empty properties.customHeader}">
            <div class="viewCsvImport-body-header">
                ${properties.label}
            </div>
        </c:when>
        <c:otherwise>
            ${properties.customHeader}
        </c:otherwise>
    </c:choose>
    <c:choose>
        <c:when test="${properties.view eq 'success'}">
            <c:if test="${!empty properties.messageOnSuccess}">
                <script>
                    alert("${properties.messageOnSuccess}");
                </script>
            </c:if>
            <c:if test="${!empty properties.redirectUrl}">
                <script>
                    parent.location = "${properties.redirectUrl}";
                </script>
            </c:if>
            <span>Total ${properties.totalImported} record(s) imported.</span>
        </c:when>
        <c:when test="${properties.view eq 'displayForm'}">
            <c:if test="${!empty properties.error && properties.error}">
                <div class="errors">
                    <span>
                        <c:choose>
                            <c:when test="${!empty properties.messageOnError}">
                                ${properties.messageOnError}
                            </c:when>
                            <c:otherwise>
                                Error! Please check your file and try it again.
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </c:if>
            <form method="post" action="${properties.url}" class="form" enctype="multipart/form-data">
                <div class="form-row">
                    <label for="csvImportFile" class="upload"><fmt:message key="general.method.label.selectFile"/></label>
                    <span class="form-input">
                        <input id="csvImportFile" type="file" name="csvImportFile"/>
                    </span>
                </div>
                <div class="form-buttons">
                    <input class="form-button" type="submit" value="<fmt:message key="general.method.label.upload"/>" />
                </div>
            </form>
        </c:when>
    </c:choose>
    <c:if test="${!empty properties.customFooter}">
        ${properties.customFooter}
    </c:if>
</div>
