<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<p><i class="icon-edit"></i> <fmt:message key="adminBar.label.designApp"/>: <input id="appNameValue" type="hidden" value="<c:out value="${appDefinition.name}"/>"><span id="appName" class="nav-subtitle"><c:out value="${appDefinition.name}"/></span></p>
<p id="nav-links"><a href="#" onclick="version()" class="smallbutton"><fmt:message key="console.app.common.label.version"/> ${appDefinition.version}</a> <a href="#" onclick="version()" class="smallbutton"><c:choose><c:when test="${appDefinition.published}"><fmt:message key="console.app.common.label.published"/></c:when><c:otherwise><fmt:message key="console.app.common.label.notPublished"/></c:otherwise></c:choose></a></span></p>
<c:if test="${!empty appInfo}"><p>${appInfo}</p></c:if>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.jeditable.js"></script>

<script>
    $(document).ready(function(){
        $('#appName, #appNameProperty').editable(function(value, settings){
            if(value==""){
                return $('#appNameValue').val();
            }else{
                var callback = {
                    success : function() {
                        $('#appNameValue').val(value);
                    }
                }
                ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/${appDefinition.id}/${appDefinition.version}/rename/'+value, callback);
            }
            return value;
        },{
            type      : 'text',
            tooltip   : 'Click to edit' ,
            select    : true ,
            style     : 'inherit',
            cssclass  : 'LabelEditableField',
            onblur    : 'submit',
            rows      : 1,
            width     : '80%',
            minwidth  : 80
        });
    });

    <ui:popupdialog var="versionDialog" src="${pageContext.request.contextPath}/web/console/app/${appDefinition.id}/versioning"/>
    function version(){
        versionDialog.init();
    }
</script>