<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.form.create.label.title"/>
    </div>
    <div id="main-body-content">

        <c:url var="url" value="" />
        <c:set var="builderMode" value="${param.builderMode == 'true'}"/>
        <form:form id="createForm" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/form/submit?builderMode=${builderMode}" method="POST" commandName="formDefinition" cssClass="form">
            <input type="hidden" name="activityDefId" value="<c:out value="${activityDefId}"/>"/>
            <input type="hidden" name="processDefId" value="<c:out value="${processDefId}"/>"/>
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.form.create.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.form.common.label.id"/></label>
                    <span class="form-input"><form:input path="id" cssErrorClass="form-input-error" /> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.form.common.label.name"/></label>
                    <span class="form-input"><form:input path="name" cssErrorClass="form-input-error" /> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.form.common.label.tableName"/></label>
                    <span class="form-input"><form:input path="tableName" cssErrorClass="form-input-error" maxlength="28" autocomplete="off"/> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="form.form.description"/></label>
                    <span class="form-input"><form:textarea path="description" cssErrorClass="form-input-error" cols="40" rows="10" /></span>
                </div>
                <div class="form-row">
                    <a href="#" id="showAdvancedInfo" onclick="showAdvancedInfo();return false"><fmt:message key="console.app.import.label.showAdvancedOptions"/></a>
                    <a href="#" style="display: none" id="hideAdvancedInfo" onclick="hideAdvancedInfo();return false"><fmt:message key="console.app.import.label.hideAdvancedOptions"/></a>
                </div>
                <div id="advancedView" style="display:none">
                    <h5><fmt:message key="console.form.create.copy.header"/></h5>
                    <div class="form-row">
                        <label for="copyAppId" style="display:inline-block;width:auto;float:none;">
                            <fmt:message key="console.form.create.copy.appId"/>
                            
                            <select id="copyAppId" name="copyAppId">
                                <option></option>
                                <c:forEach items="${appList}" var="app">
                                    <option value="${app.id}"><c:out value="${app.name}"/></option>
                                </c:forEach>
                            </select>
                        </label>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        <label for="copyFormId" style="display:inline-block;width:auto;float:none;v">
                            <fmt:message key="console.form.create.copy.formId"/>
                            <select id="copyFormId" name="copyFormId"></select>
                        </label>
                    </div>    
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="button" value="<fmt:message key="general.method.label.save"/>"  onclick="validateField()"/>
                <input class="form-button" type="button" value="<fmt:message key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>
    
    <script type="text/javascript">
        var preventRecursiveClick = 0;
        $(document).ready(function(){
            $("#copyAppId").val("<c:out value="${appId}"/>");
            $("#copyAppId").change(function(){
                var populateForm = {
                    success : function(resp) {
                        $("#copyFormId option").remove();
                        
                        var options = $.parseJSON(resp);
                        $.each(options, function(i, option){
                            $("#copyFormId").append('<option value="'+option.value+'">'+UI.escapeHTML(option.label)+'</option>');
                        });
                    }
                };
                
                ConnectionManager.get('<c:out value="${pageContext.request.contextPath}"/>/web/json/console/app/'+$(this).val()+'/forms/options', populateForm);
            });
            $("#copyAppId").trigger("change");
            
            var loadTableNameData = {
                success : function(response){
                    var data = eval('(' + response + ')');
                    $("#tableName").autocomplete({source : data.tableName, minLength : 0}).focus(function(){ 
                        $(this).data("uiAutocomplete").search($(this).val());
                    });
                }
            }
            ConnectionManager.get('<c:out value="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/form/tableNameList"/>', loadTableNameData);
        });
        
        function showAdvancedInfo(){
            $('#advancedView').slideToggle('slow');
            $('#showAdvancedInfo').hide();
            $('#hideAdvancedInfo').show();
        }

        function hideAdvancedInfo(){
            $('#advancedView').slideToggle('slow');
            $('#showAdvancedInfo').show();
            $('#hideAdvancedInfo').hide();
        }
        
        function validateField(){
            var idMatch = /^[0-9a-zA-Z_]+$/.test($("#id").attr("value"));
            var tableName = $("#tableName").attr("value");
            var tableNameMatch = /^[0-9a-zA-Z_]+$/.test(tableName);
            if(!idMatch || !tableNameMatch || tableName.length > 20){
                var alertString = '';
                if(!idMatch){
                    alertString = '<fmt:message key="console.form.error.label.idInvalid"/>';
                    $("#id").focus();
                }
                if(!tableNameMatch){
                    if(alertString == ''){
                        $("#tableName").focus();
                    }else{
                        alertString += "\n";
                    }
                    alertString += '<fmt:message key="console.form.error.label.tableNameInvalid"/>';
                }
                if(tableName.length > 20){
                    if(alertString == ''){
                        $("#tableName").focus();
                    }else{
                        alertString += "\n";
                    }
                    alertString += '<fmt:message key="form.form.invalidId"/>';
                }
                alert(alertString);
            }else{
                $("#createForm").submit();
            }
        }

        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
    </script>

<commons:popupFooter />

