<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>
<c:if test="${not empty theme and theme ne 'classic'}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
</c:if>
    
<commons:popupHeader  bodyCssClass=" builder-popup" builderTheme="${theme}"/>

    <div id="main-body-header">
        <fmt:message key="console.builder.create.label"><fmt:param value="${builder.objectLabel}"/></fmt:message>
    </div>
    <div id="main-body-content">

        <c:url var="url" value="" />
        <c:set var="builderMode" value="${param.builderMode == 'true'}"/>
        <form:form id="create${builder.objectName}" action="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/cbuilder/${builder.objectName}/submit?builderMode=${builderMode}" method="POST" modelAttribute="builderDefinition" cssClass="form blockui">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.builder.create.label.details"><fmt:param value="${builder.objectLabel}"/></fmt:message></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.datalist.common.label.id"/></label>
                    <span class="form-input withPrefix"><span class="prefix">${builder.idPrefix}</span><form:input path="id" cssErrorClass="form-input-error" /> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.datalist.common.label.name"/></label>
                    <span class="form-input"><form:input path="name" cssErrorClass="form-input-error" /> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.datalist.common.label.description"/></label>
                    <span class="form-input"><form:textarea path="description" cssErrorClass="form-input-error" cols="40" rows="10" /></span>
                </div>
                ${builder.createNewPageHtml}
                <div class="form-row">
                    <a href="#" id="showAdvancedInfo" onclick="showAdvancedInfo();return false"><fmt:message key="console.app.import.label.showAdvancedOptions"/></a>
                    <a href="#" style="display: none" id="hideAdvancedInfo" onclick="hideAdvancedInfo();return false"><fmt:message key="console.app.import.label.hideAdvancedOptions"/></a>
                </div>
                <div id="advancedView" style="display:none">
                    <h5><fmt:message key="console.builder.create.copy.header"><fmt:param value="${builder.objectLabel}"/></fmt:message></h5>
                    <div class="form-row">
                        <label for="copyAppId"><fmt:message key="console.datalist.create.copy.appId"/></label>
                        <span class="form-input">    
                            <select id="copyAppId" name="copyAppId">
                                <option></option>
                                <c:forEach items="${appList}" var="app">
                                    <option value="${app.id}"><c:out value="${app.name}"/></option>
                                </c:forEach>
                            </select>
                        </span>
                    </div>
                    <div class="form-row">    
                        <label for="copyId">${builder.objectLabel}</label> 
                        <span class="form-input">
                            <select id="copyId" name="copyId"></select>
                        </span>
                    </div>    
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.save"/>"  onclick="validateField()"/>
                <input class="form-button" type="button" value="<ui:msgEscHTML key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>
    
    <script type="text/javascript">
        $(document).ready(function(){
            $("#copyAppId").val("${appId}");
            $("#copyAppId").change(function(){
                var populate = {
                    success : function(resp) {
                        $("#copyId option").remove();
                        
                        var options = $.parseJSON(resp);
                        $.each(options, function(i, option){
                            $("#copyId").append('<option value="'+option.value+'">'+UI.escapeHTML(option.label)+'</option>');
                        });
                        $("#copyId").trigger("change").trigger("chosen:updated");
                    }
                };
                if ($("#copyAppId").val()==='${appId}') {
                    version ='/${appVersion}';
                } else {
                    version='';
                }
                ConnectionManager.get('<c:out value="${pageContext.request.contextPath}"/>/web/json/console/app/'+$(this).val()+version+'/cbuilder/${builder.objectName}/options', populate);
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
            var idMatch = /^[0-9a-zA-Z_]+$/.test($("#id").val());
            if(!idMatch){
                var alertString = '';
                if(!idMatch){
                    alertString = '<ui:msgEscJS key="console.datalist.error.label.idInvalid"/>';
                    $("#id").focus();
                }
                alert(alertString);
            }else{
                $("#create${builder.objectName}").submit();
            }
        }

        function closeDialog() {
            if (parent && parent.JPopup) {
                parent.JPopup.hide("navCreateNewDialog");
            }
            return false;
        }
    </script>

<commons:popupFooter />

