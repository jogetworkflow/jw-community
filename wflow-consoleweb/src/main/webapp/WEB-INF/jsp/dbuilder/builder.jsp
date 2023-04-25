<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
        <title><fmt:message key="adminBar.label.list"/>: <c:out value="${datalist.name}"/> - <fmt:message key="dbuilder.title"/></title>
        <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
        <jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.ui.touch-punch.js"></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/js/boxy/javascripts/jquery.boxy.js'></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsondiffpatch/jsondiffpatch.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsondiffpatch/jsondiffpatch-formatters.min.js"></script>  
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jsondiffpatch/diff_match_patch_uncompressed.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.jeditable.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/builderutil.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/dbuilder?build=<fmt:message key="build.number"/>"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/dbuilder.core.js?build=<fmt:message key="build.number"/>"></script>

        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/boxy/stylesheets/boxy.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/dbuilder.css?build=<fmt:message key="build.number"/>"  />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jsondiffpatch/jsondiffpatchhtml.css" />
        
        <c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}">
            <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_rtl.css?build=<fmt:message key="build.number"/>">
        </c:if>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builder_custom.css?build=<fmt:message key="build.number"/>">
        <jsp:include page="/WEB-INF/jsp/includes/css.jsp" />
        <script type="text/javascript">
            window.onbeforeunload = function() {
                if(!DatalistBuilder.isSaved()){
                    return '<ui:msgEscJS key="dbuilder.saveBeforeClose"/>';
                }
            };

            $(document).ready(function() {
                let lockSocket = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "${pageContext.request.contextPath}/web/socket/plugin/org.kecak.apps.app.lib.UrlLockSocket");
                lockSocket.onmessage = function(event) {
                    let text = event.data;
                    if(text) {
                        alert(text);
                    }
                }

                lockSocket.onopen = () => lockSocket.send("${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/${datalist.id}");

                DatalistBuilder.appId = '<c:out value="${appId}"/>';
                DatalistBuilder.appVersion = '<c:out value="${appVersion}"/>';
                DatalistBuilder.saveUrl = '<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/builderSave/"/>';
                DatalistBuilder.previewUrl = '<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/builderPreview/"/>';
                DatalistBuilder.contextPath = '${pageContext.request.contextPath}';
                DatalistBuilder.appPath = '<c:out value="/${appId}/${appVersion}"/>';
                DatalistBuilder.filterParam = '<c:out value="${filterParam}"/>';

                //tabbed
                $('#builder-steps li').click( function(){
                    var div = $(this).find('a').attr('href');
                    if($(div).size() > 0){
                        $('#builder-steps li').removeClass("active");
                        $('#builder-steps li').removeClass("next");
                        $(this).addClass("active");
                        $(this).prev().addClass("next");
                        $('#tabpanels').children().hide();
                        $(div).show();
                        $("body").removeClass("stop-scrolling");
                    }
                    return false;
                });

                DatalistBuilder.init(function(){
                    DatalistBuilder.setJson(${json}, "<c:out value="${id}"/>");
                });
                
                $('#builder-steps-properties').click( function(){
                    DatalistBuilder.showDatalistProperties();
                    $("body").addClass("stop-scrolling");
                })
            });
        </script>
    </head>
    <body id="datalistbuilder">

        <div id="builder-container">
            <div id="builder-header">
                <a class="reload" onclick="location.reload(true);"></a>
                <i class="fas fa-2x fa-table"></i>
                <div id="builder-logo"></div>
                <div id="builder-title"><fmt:message key="dbuilder.title"/> <i><c:out value="${appDefinition.name}" /> v${appDefinition.version}: <c:out value="${datalist.name}"/> <c:if test="${appDefinition.published}">(<fmt:message key="console.app.common.label.published"/>)</c:if></i></div>
                <%--<jsp:include page="/web/console/app/${appId}/${appVersion}/builder/navigator/d/${id}" flush="true" />--%>
            </div>
            <div id="builder-body">
                <div id="builder-bar">
                    <ul id="builder-steps">
                        <li id="builder-steps-source" class="first-active active"><a href="#source"><span class="steps-bg"><span class="title"><fmt:message key="dbuilder.source"/></span><span class="subtitle"><fmt:message key="dbuilder.source.description"/></span></span></a></li>
                        <li id="builder-steps-designer"><a href="#designer"><span class="steps-bg"><span class="title"><fmt:message key="dbuilder.design"/></span><span class="subtitle"><fmt:message key="dbuilder.design.description"/></span></span></a></li>
                        <li id="builder-steps-properties"><a href="#properties"><span class="steps-bg"><span class="title"><fmt:message key="dbuilder.properties"/></span><span class="subtitle"><fmt:message key="dbuilder.properties.description"/></span></span></a></li>
                        <li id="builder-steps-preview"><a onclick="DatalistBuilder.preview()"><span class="steps-bg"><span class="title"><fmt:message key="dbuilder.preview"/></span><span class="subtitle"><fmt:message key="dbuilder.preview.description"/></span></span></a></li>
                        <li id="builder-steps-save" class="last-inactive"><a onclick="DatalistBuilder.mergeAndSave()"><span class="steps-bg"><span class="title"><fmt:message key="dbuilder.save"/></span><span class="subtitle"><fmt:message key="dbuilder.save.description"/></span></span></a></li>
                    </ul>
                    <div id="builder-bg"></div>
                </div>

                <div id="tabpanels">
                    <div id="source" class="fixed-height">

                    </div>

                    <div id="designer" style="display:none">
                        <table>
                            <tr>
                                <td width="185" valign="top">
                                    <fieldset id="builder-palette">
                                        <div id="builder-palette-top"></div>
                                        <div id="builder-palette-body">
                                            <h3><fmt:message key="dbuilder.columnsFilters"/></h3>
                                            <ul id="builder-palettle-items">
                                            </ul>
                                            <h3><fmt:message key="dbuilder.actions"/></h3>
                                            <ul id="builder-palettle-actions">
                                            </ul>
                                        </div>
                                    </fieldset>
                                    <div id="builder-palette-spacer"></div>
                                </td>
                                <td valign="top">
                                    <fieldset id="form-canvas" style="">
                                        <legend><fmt:message key="dbuilder.designerSpace"/></legend>
                                        <div class="builder-section">
                                            <table id="builder-canvas" style="width:100%">
                                                <tr>
                                                    <td colspan="2" id="tdDatabuilderContentFilters">
                                                        <span class="hint"><fmt:message key="dbuilder.dragFiltersHere"/></span>
                                                        <ul id="databuilderContentFilters">
                                                        </ul>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td id="tdDatabuilderContentColumns">
                                                        <span class="hint"><fmt:message key="dbuilder.dragColumnsHere"/></span>
                                                        <ul id="databuilderContentColumns">
                                                        </ul>
                                                    </td>
                                                    <td id="tdDatabuilderContentRowActions">
                                                        <span class="hint"><fmt:message key="dbuilder.dragRowActionsHere"/></span>
                                                        <ul id="databuilderContentRowActions">
                                                        </ul>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td colspan="2" id="tdDatabuilderContentActions">
                                                        <span class="hint"><fmt:message key="dbuilder.dragActionsHere"/></span>
                                                        <ul id="databuilderContentActions">
                                                        </ul>
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
                                    </fieldset>
                                </td>
                            </tr>
                        </table>
                        <div class="form-clear"></div>
                        
                        <div id="list-advanced">
                            <div id="list-info" style="display: none">
                                <form id="list-preview" action="?" method="post">
                                    <textarea id="list-json" name="json" cols="80" rows="10" style="font-size: smaller"><c:out value="${json}"/></textarea>
                                    <textarea id="list-json-original" name="json-original" cols="80" rows="10" style="display: none;"><c:out value="${json}"/></textarea>
                                    <textarea id="list-json-current" name="json-current" cols="80" rows="10" style="display: none;"><c:out value="${json}"/></textarea>
                                </form>
                                <button onclick="DatalistBuilder.updateList()"><fmt:message key="console.builder.update"/></button>
                            </div>
                        </div>
                        
                    </div>

                    <div id="properties" style="display:none">

                    </div>
                </div>               
            </div>
            <div id="builder-footer">
                <fmt:message key="console.builder.footer"/>
            </div>
        </div>
            
        <div id="builder-message"></div> 

        <script type="text/javascript">
            HelpGuide.base = "${pageContext.request.contextPath}"
            HelpGuide.attachTo = "#builder-bar";
            HelpGuide.key = "help.web.console.app.datalist.builder";
            setTimeout(function() { HelpGuide.show(); }, 2000);
        </script>
            
        <jsp:include page="/WEB-INF/jsp/console/apps/builder.jsp" flush="true">
            <jsp:param name="appId" value="${appId}"/>
            <jsp:param name="appVersion" value="${appDefinition.version}"/>
            <jsp:param name="elementId" value="${datalist.id}"/>
            <jsp:param name="jsonForm" value="#list-info"/>
            <jsp:param name="builder" value="datalist"/>
        </jsp:include>    
        <jsp:include page="/WEB-INF/jsp/console/apps/adminBar.jsp" flush="true">
            <jsp:param name="appId" value="${appId}"/>
            <jsp:param name="appVersion" value="${appVersion}"/>
            <jsp:param name="webConsole" value="true"/>
            <jsp:param name="builderMode" value="true"/>
        </jsp:include>
            
    </body>
</html>