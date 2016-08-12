<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.userview.create.label.title"/>
    </div>

    <div id="main-body-content">
        <c:set var="builderMode" value="${param.builderMode == 'true'}"/>
        <form:form id="createUserview" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/userview/submit/create?builderMode=${builderMode}" method="POST" commandName="userviewDefinition" cssClass="form">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.userview.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.userview.common.label.id"/></label>
                    <span class="form-input"><form:input path="id" cssErrorClass="form-input-error" /> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.userview.common.label.name"/></label>
                    <span class="form-input"><form:input path="name" cssErrorClass="form-input-error" /> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.userview.common.label.description"/></label>
                    <span class="form-input"><form:textarea path="description" cssErrorClass="form-input-error" cols="40" rows="10" /></span>
                </div>
                <div class="form-row">
                    <a href="#" id="showAdvancedInfo" onclick="showAdvancedInfo();return false"><fmt:message key="console.app.import.label.showAdvancedOptions"/></a>
                    <a href="#" style="display: none" id="hideAdvancedInfo" onclick="hideAdvancedInfo();return false"><fmt:message key="console.app.import.label.hideAdvancedOptions"/></a>
                </div>
                <div id="advancedView" style="display:none">
                    <h5><fmt:message key="console.userview.create.copy.header"/></h5>
                    <div class="form-row">
                        <label for="copyAppId" style="display:inline-block;width:auto;float:none;">
                            <fmt:message key="console.userview.create.copy.appId"/>
                            
                            <select id="copyAppId" name="copyAppId">
                                <option></option>
                                <c:forEach items="${appList}" var="app">
                                    <option value="${app.id}"><c:out value="${app.name}"/></option>
                                </c:forEach>
                            </select>
                        </label>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        <label for="copyUserviewId" style="display:inline-block;width:auto;float:none;v">
                            <fmt:message key="console.userview.create.copy.userviewId"/>
                            <select id="copyUserviewId" name="copyUserviewId"></select>
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
        $(document).ready(function(){
            $("#copyAppId").val("<c:out value="${appId}"/>");
            $("#copyAppId").change(function(){
                var populate = {
                    success : function(resp) {
                        $("#copyUserviewId option").remove();
                        
                        var options = $.parseJSON(resp);
                        $.each(options, function(i, option){
                            $("#copyUserviewId").append('<option value="'+option.value+'">'+UI.escapeHTML(option.label)+'</option>');
                        });
                    }
                };
                
                ConnectionManager.get('<c:out value="${pageContext.request.contextPath}"/>/web/json/console/app/'+$(this).val()+'/userview/options', populate);
            });
            $("#copyAppId").trigger("change");
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
            var idMatch = /^[a-zA-Z0-9_]+$/.test($("#id").attr("value"));
            if(!idMatch){
                var alertString = '';
                if(!idMatch){
                    alertString = '<fmt:message key="console.userview.error.label.idInvalid"/>';
                    $("#id").focus();
                }
                alert(alertString);
            }else{
                $("#createUserview").submit();
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
