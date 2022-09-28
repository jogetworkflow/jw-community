<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.app.create.label.title"/>
    </div>
    <div id="main-body-content">
        <style>
            .type-icon {
                padding: 0px;
                margin-bottom: 0px;
            }
            .type-icon li {
                display: inline-block;
                vertical-align: top;
                margin-right: 5px;
            }
            .type-icon li input {
                display: none;
            }
            .type-icon li div {
                display: block;
                border: 1px solid #ced4da;
                padding: 20px;
                border-radius: 5px;
                text-align: center;
                cursor: pointer;
                color: #ced4da !important;
                width: 120px;
                height: 130px;
                overflow-wrap: break-word;
                line-height: 15px;
                box-sizing: border-box;
                font-size: 13px;
            }
            .type-icon li div:hover {
                color: #495057 !important;
                border-color:#495057;
            }
            .type-icon li div i {
                font-size: 50px;
                font-style: normal;
                color:#ced4da !important;
            }
            .type-icon li div:hover i{
                color: #495057 !important;
            }
            .type-icon li div span {
                display: block;
                padding-top: 10px;
                word-break: break-word;
            }
            .type-icon li input:checked + div {
                border-color: #007bff;
                color: #007bff !important;
            }
            .type-icon li input:checked + div i{
                color: #007bff !important;
            }
            .form-input input::placeholder{
                color:#cfcfcf;
            }
            .form-input input:-ms-input-placeholder{
                color:#cfcfcf;
            }
            .form-input input::-ms-input-placeholder{
                color:#cfcfcf;
            }
        </style>    
        <c:url var="url" value="" />
        <form:form id="createApp" action="${pageContext.request.contextPath}/web/console/app/submit" method="POST" modelAttribute="appDefinition" cssClass="form blockui">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <div class="form-row">
                    <label for=""></label>
                    <span class="form-input">
                        <ul class="type-icon">
                            <li><label><input name="type" type="radio" value="" <c:if test="${type eq ''}">checked</c:if>><div><i class="far fa-file"></i><span><fmt:message key="console.app.create.blank"/></span></div></label></li>
                            <li><label><input name="type" type="radio" value="template" <c:if test="${type eq 'template'}">checked</c:if>><div><i class="far fa-file-archive"></i><span><fmt:message key="console.app.create.templateApp"/></span></div></label></li>
                            <li><label><input name="type" type="radio" value="duplicate" <c:if test="${type eq 'duplicate'}">checked</c:if>><div><i class="far fa-copy"></i><span><fmt:message key="console.app.create.cloneExisting"/></span></div></label></li>
                        </ul>
                    </span>
                </div>
                <div id="duplicateView" class="form-row" style="display:none">
                    <label for="copyAppId"><fmt:message key="console.app.create.clone"/> <span class="mandatory">*</span></label>
                    <span class="form-input">
                        <select id="copyAppId" name="copyAppId" disabled>
                            <c:forEach items="${appList}" var="app">
                                <option value="${app.id}" <c:if test="${copyAppId eq app.id}">selected</c:if>><c:out value="${app.name}"/></option>
                            </c:forEach>
                        </select>
                    </span>    
                </div> 
                <div class="form-row" id="templateView" style="display:none">
                    <label for="templateAppId"><fmt:message key="console.app.create.template"/> <span class="mandatory">*</span></label>
                    <span class="form-input">
                        <select id="templateAppId" name="templateAppId" disabled>
                            <c:forEach items="${templateAppList}" var="entry">
                                <option value="${entry.key}" <c:if test="${templateAppId eq entry.key}">selected</c:if>><c:out value="${entry.value}"/></option>
                            </c:forEach>
                        </select>
                    </span>    
                </div>        
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.app.common.label.id"/> <span class="mandatory">*</span></label>
                    <span class="form-input"><form:input path="id" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.app.common.label.name"/> <span class="mandatory">*</span></label>
                    <span class="form-input"><form:input path="name" cssErrorClass="form-input-error" /></span>
                </div>
                <div class="form-row" id="optionView" style="display:none">
                    <label for="field1"><fmt:message key="console.app.create.tablePrefix"/> </label>
                    <span class="form-input"><input type="text" name="tablePrefix" value="" placeholder="<fmt:message key="console.app.create.tablePrefix.eg"/>" /></span>
                </div>  
            </fieldset>
            <div class="form-buttons">
                <input class="form-button btn btn-primary" type="button" value="<ui:msgEscHTML key="general.method.label.save"/>"  onclick="validateField()"/>
                <input class="form-button btn btn-primary" type="button" value="<ui:msgEscHTML key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>
    
    <script type="text/javascript">
        function showDiv(div) {
            $(div).find('input, select, textarea').removeAttr('disabled');
            $(div).show();
            $(div).find('input, select, textarea').trigger("change");
            $(div).find('select').trigger("chosen:updated");
        };
        function hideDiv(div) {
            $(div).find('input, select, textarea').attr('disabled', 'disabled');
            $(div).find('select').trigger("chosen:updated");
            $(div).hide();
        };
        function validateField(){
            var idMatch = /^[0-9a-zA-Z_]+$/.test($("#id").val());
            if(!idMatch){
                var alertString = '';
                if(!idMatch){
                    alertString = '<ui:msgEscJS key="console.app.error.label.idInvalid"/>';
                    $("#id").focus();
                }
                alert(alertString);
            }else{
                $("#createApp").submit();
            }
        }

        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
        
        $(function() {
            $("input#id").focus();
            
            $("[name='type']").on("change", function(){
                var value = $("[name='type']:checked").val();
                hideDiv($("#duplicateView, #templateView, #optionView"));
                
                if (value === "template") {
                    showDiv($("#templateView, #optionView"));
                } else if (value === "duplicate") {
                    showDiv($("#duplicateView, #optionView"));
                }
            })
            $("[name='type']:first").trigger("change");
        });
    </script>

<commons:popupFooter />

