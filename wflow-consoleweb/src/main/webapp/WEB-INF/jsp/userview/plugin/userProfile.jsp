<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<style>
    .userProfile-body-content .userProfile-body-header {
        clear:both;
        color:#000000;
        font-family:Georgia,"Times New Roman",Times,serif;
        font-size:1.4em;
        font-style:normal;
        font-variant:normal;
        font-weight:normal;
        height:22px;
        padding:0.5em;
    }

    .form fieldset {
        background:none repeat scroll 0 0 transparent;
        border:1px dotted silver;
        clear:both;
        margin:0.5em 0;
        padding:0.5em;
    }

    .form-row {
        clear:both;
        display:block;
        line-height:25px;
        text-align:left;
    }

    .form-row label {
        display:inline-block;
        float:left;
        min-width:100px;
        padding:0.25em;
        text-align:left;
        vertical-align:top;
        width:40%;
    }

    .form-row .form-input {
        display:inline-block;
        padding:0.25em;
        text-align:left;
    }
</style>
<c:if test="${requestParameters.isPreview eq 'true'}">
    <script>
        $(document).ready(function() {
            $(".form-button").attr("disabled", "disabled");
        });
    </script>
</c:if>
<div class="userProfile-body-content">
    <div class="userProfile-body-header">
        ${properties.headerTitle}
    </div>
<c:choose>
    <c:when test="${properties.view eq 'redirect'}">
        <script>
            <c:if test="${!empty properties.message}">
                alert('${properties.message}');
            </c:if>
            window.location = "${properties.redirectURL}";
        </script>
    </c:when>
    <c:when test="${properties.view eq 'formView'}">
         <c:if test="${!empty properties.saved && properties.saved eq 'true'}">
            <p class="form-message" style="display:block;color:blue;">
                <span><fmt:message key="console.directory.user.message.saved"/></span>
            </p>
        </c:if>

        <form id="profile" action="${properties.actionUrl}" method="POST" class="form">
            <input type="hidden" name="id" value="${properties.user.id}">
            <input type="hidden" name="username" value="${properties.user.username}">

            <c:if test="${!empty properties.errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${properties.errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.directory.user.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.username"/></label>
                    <span class="form-input">${properties.user.username}</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.firstName"/></label>
                    <span class="form-input"><input type="text" id="firstName" name="firstName" value="${properties.user.firstName}"/> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.lastName"/></label>
                    <span class="form-input"><input type="text" id="lastName" name="lastName" value="${properties.user.lastName}"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.email"/></label>
                    <span class="form-input"><input type="text" id="email" name="email" value="${properties.user.email}"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.password"/></label>
                    <span class="form-input"><input type="password" id="password" name="password"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.confirmPassword"/></label>
                    <span class="form-input"><input type="password" id="confirmPassword" name="confirmPassword"/></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.directory.user.common.label.timeZone"/></label>
                    <span class="form-input">
                        <select id="timeZone" name="timeZone">
                            <c:forEach items="${properties.timezones}" var="timezone">
                                <c:set var="selected" value=""/>
                                <c:if test="${timezone.key == properties.user.timeZone}"><c:set var="selected" value="selected"/></c:if>
                                <option value="${timezone.key}" ${selected}>${timezone.value}</option>
                            </c:forEach>
                        </select>
                    </span>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="button" value="<fmt:message key="general.method.label.save"/>"  onclick="validateField()"/>
            </div>
        </form>
        <script type="text/javascript">

            function validateField(){
                var valid = true;
                var alertString = "";
                if($("#firstName").val() == ""){
                    alertString += '<fmt:message key="User.firstName[not.blank]"/>';
                    valid = false;
                }
                if($("#password").val() != $("#confirmPassword").val()){
                    if(alertString != ""){
                        alertString += '\n';
                    }
                    alertString += '<fmt:message key="console.directory.user.error.label.passwordNotMatch"/>';
                    valid = false;
                }

                if(valid){
                    $("#profile").submit();
                }else{
                    alert(alertString);
                }
            }
        </script>
    </c:when>
</c:choose>
</div>
<div style="clear:both;"></div>




