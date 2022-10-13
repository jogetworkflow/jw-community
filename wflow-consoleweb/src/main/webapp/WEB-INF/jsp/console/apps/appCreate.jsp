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
                            <c:if test="${templateAppList.size() > 0}">
                                <li><label><input name="type" type="radio" value="template" <c:if test="${type eq 'template'}">checked</c:if>><div><i class="far fa-file-archive"></i><span><fmt:message key="console.app.create.templateApp"/></span></div></label></li>
                            </c:if>
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
                <div id="templateConfig" style="display:none;">
                    <div class="form-row">
                        <a href="#" id="showAdvancedInfo" onclick="showAdvancedInfo();return false"><fmt:message key="console.app.import.label.showAdvancedOptions"/></a>
                        <a href="#" style="display: none" id="hideAdvancedInfo" onclick="hideAdvancedInfo();return false"><fmt:message key="console.app.import.label.hideAdvancedOptions"/></a>
                    </div>
                    <div id="templateConfigRows" style="display:none;margin-top: 20px;">
                        
                    </div>
                </div>    
            </fieldset>
            <div class="form-buttons">
                <input class="form-button btn btn-primary" type="button" value="<ui:msgEscHTML key="general.method.label.save"/>"  onclick="validateField()"/>
                <input class="form-button btn btn-primary" type="button" value="<ui:msgEscHTML key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>
    
    <script type="text/javascript">
        function showAdvancedInfo(){
            showDiv($("#templateConfigRows"));
            $('#showAdvancedInfo').hide();
            $('#hideAdvancedInfo').show();
        }
        function hideAdvancedInfo(){
            hideDiv($("#templateConfigRows"));
            $('#showAdvancedInfo').show();
            $('#hideAdvancedInfo').hide();
        }
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
        
        var advancedConfig = {};
        <c:if test="${!empty templateConfig}">
            advancedConfig = ${templateConfig};
        </c:if>
        
        //create config fields for template
        function createField(type, i, name, defaultValue, placeholder) {
            var id = "";
            if (type === "tablePrefix") {
                id = "tablePrefix";
            } else {
                id = 'rp_'+type+'_'+name.replace(/[^a-zA-Z0-9_]/ig, "_");
            }
            var value = '';
            if (defaultValue !== undefined) {
                value = defaultValue;
            }
            if (advancedConfig[id] !== undefined) {
                value = UI.escapeHTML(advancedConfig[id]);
            }
            var attr = "";
            if (placeholder !== undefined) {
                attr = " placeholder=\"" + placeholder + "\" ";
            }
            var field = $('<div class="form-row"><label for="'+id+'">'+name+'</label><span class="form-input"><input type="text" name="'+id+'" value="'+value+'" '+attr+'/></span></div>');
            $("#templateConfigRows").append(field);
        }       
        
        $(function() {
            $("input#id").focus();
            
            $("[name='templateAppId'], [name='copyAppId']").on("change", function(){
                var type, id, url;
                if ($(this).is("[name='copyAppId']")) {
                    type = "duplicate";
                    id = $("[name='copyAppId']").val();
                    url = "${pageContext.request.contextPath}/web/json/duplicate/app/config?id=" + encodeURIComponent(id);
                } else {
                    type = "template";
                    id = $("[name='templateAppId']").val();
                    url = "${pageContext.request.contextPath}/web/json/marketplace/template/config?id=" + encodeURIComponent(id);
                }
                
                if ($("#templateConfigRows").attr("data-id") === id && $("#templateConfigRows").attr("data-type") === type) {
                    showDiv($("#templateConfig"));
                } else {
                    $("#templateConfigRows").attr("data-id", "");
                    $("#templateConfigRows").attr("data-type", "");
                    $("#templateConfigRows").html("");
                    $.ajax(url)
                    .done(function(data){
                        if (data.ids !== undefined && data.ids.length > 0) {
                            $("#templateConfigRows").append('<h5 class="form-row main-body-content-subheader"><ui:msgEscJS key="console.app.create.idReplace"/></h5>');
                            for (var i=0; i<data.ids.length; i++) {
                                createField("ids", i, data.ids[i]);
                            }
                        }
                        $("#templateConfigRows").append('<h5 class="form-row main-body-content-subheader"><ui:msgEscJS key="console.app.create.tableNameReplace"/></h5>');
                        if (data.tables !== undefined && data.tables.length > 0) {
                            for (var i=0; i<data.tables.length; i++) {
                                createField("tables", i, data.tables[i]);
                            }
                        }
                        createField("tablePrefix", 0, '<ui:msgEscJS key="console.app.create.tablePrefix"/>', '<c:out value="${tablePrefix}"/>', '<ui:msgEscJS key="console.app.create.tablePrefix.eg"/>');
                        if (data.labels !== undefined && data.labels.length > 0) {
                            $("#templateConfigRows").append('<h5 class="form-row main-body-content-subheader"><ui:msgEscJS key="console.app.create.labelReplace"/></h5>');
                            for (var i=0; i<data.labels.length; i++) {
                                createField("labels", i, data.labels[i]);
                            }
                        }
                        $("#templateConfigRows").attr("data-id", id);
                        $("#templateConfigRows").attr("data-type", type);
                        showDiv($("#templateConfig"));
                    });
                }
            });

            $("[name='type']").on("change", function(){
                var value = $("[name='type']:checked").val();
                hideDiv($("#duplicateView, #templateView, #templateConfig"));
                
                if (value === "template") {
                    showDiv($("#templateView"));
                } else if (value === "duplicate") {
                    showDiv($("#duplicateView"));
                }
            })
            $("[name='type']:first").trigger("change");
        });
    </script>

<commons:popupFooter />

