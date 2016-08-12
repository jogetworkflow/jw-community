<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <c:if test="${!empty param.css}">
        <link rel="stylesheet" type="text/css" href="<c:out value="${param.css}"/>">
    </c:if>

    <div id="main-body-header">
        <c:out value="${process.packageName}" escapeXml="true"/> (version ${appVersion})
    </div>
    <div id="main-body-content">
        <p>&nbsp;</p>
        <c:url var="url" value="/web/client/app/${appId}/${appVersion}/process/${process.idWithoutVersion}/start?${queryString}" />
        <form id="processForm" name="processForm" method="POST" action="<c:out value="${url}"/>">
            <div id="main-body-message">
                <c:out value="${process.name}" escapeXml="true"/>
                <p id="main-body-submessage"><c:out value="${process.packageName}" escapeXml="true"/></p>
                <button onclick="return startProcess()" class="form-button-large"><fmt:message key="client.app.run.process.label.start"/></button>
            </div>
            <div style="text-align: center">
                <p>&nbsp;</p>
                <a id="cancel" href="javascript:closeDialog('<c:out value="${param.cancel}" />')"><fmt:message key="client.app.run.process.label.start.cancel"/></a>
            </div>
        </form>
    </div>

    <script type="text/javascript">
        $(document).ready(function(){
            var cancel = '<c:out value="${param.cancel}" />';

            if(cancel == '' || cancel == 'false'){
                if(window.parent == window)
                    $("#cancel").hide();

                try{
                    parent.PopupDialogCache;
                }catch(e){
                    $("#cancel").hide();
                }
            }
        })

        function closeDialog(cancel) {
            if(cancel == undefined || cancel == '' || cancel == 'false'){
                parent.PopupDialog.closeDialog();
                return false;
            }else{
                document.location = cancel;
            }
        }

        function startProcess(){
            if(confirm('<fmt:message key="client.app.run.process.label.start.confirm"/>')){
                setTimeout(function() { $('#start').attr('disabled', 'disabled') }, 0);
                return true;
            }
            else {
                return false;
            }
        }

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
    </script>

<commons:popupFooter />
