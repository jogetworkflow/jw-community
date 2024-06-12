<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.apps.app.service.AppUtil"%>

<c:set var="archivedProcessStatus" value="<%= AppUtil.getArchivedProcessStatus() %>"/>

<c:if test="${!empty archivedProcessStatus && archivedProcessStatus < 100}">
    <div id="archivedProcessStatus" class="alert alert-info" style="margin-top:10px;">
        <p>
            <ui:msgEscJS key="console.monitoring.archiveMigrationProgress"/> <span id="percentageValue" style="display:inline-block; margin-right: 20px;"></span>
            <button id="resumeArchive" class="btn" style="display:none"><fmt:message key="console.monitoring.resume"/></button>
            <button id="pauseArchive" class="btn" style="display:none"><fmt:message key="console.monitoring.pause"/></button>
            <c:choose>
                <c:when test="${archivedProcessStatus < 0}">
                    
                </c:when> 
                <c:otherwise>
                    
                </c:otherwise>    
            </c:choose>
        </p>
        <div class="progress">
            <div class="progress-bar progress-bar-animated" style="width: 0%"></div>
        </div>
        <script>
            $(function(){
                //update the progress bar, button and the precentage text
                var updateProgress = function(data){
                    var precentage = parseFloat(data);
                    
                    if (precentage >= 100) {
                        $("#archivedProcessStatus").remove();
                        return;
                    }
                    if (precentage > 0) {
                        $("#resumeArchive").hide();
                        $("#pauseArchive").show();
                        
                        //retrieve latest progress
                        setTimeout(function(){
                            var callback = {
                                success : function(data) {
                                    updateProgress(data);
                                }
                            };
                            var request = ConnectionManager.post('${pageContext.request.contextPath}/web/json/console/monitor/completed/process/archive/status', callback, '');
                        }, 10000);
                    } else {
                        $("#resumeArchive").show();
                        $("#pauseArchive").hide();
                        precentage = precentage * -1;
                    }
                    $("#archivedProcessStatus .progress-bar").css("width", precentage.toFixed(2) + "%");
                    $("#archivedProcessStatus #percentageValue").text(precentage.toFixed(0) + "%");
                };

                //handle resume button event
                $("#resumeArchive").on('click', function(){
                    if (confirm('<ui:msgEscJS key="console.monitoring.resumeArchive"/>')) {
                        UI.blockUI(); 
                        var callback = {
                            success : function(data) {
                                updateProgress(data);
                                UI.unblockUI(); 
                            }
                        };
                        var request = ConnectionManager.post('${pageContext.request.contextPath}/web/json/console/monitor/completed/process/archive/resume', callback, '');
                    }
                    return false;
                });
                
                //handle pause button event
                $("#pauseArchive").on('click', function(){
                    if (confirm('<ui:msgEscJS key="console.monitoring.pauseArchive"/>')) {
                        UI.blockUI(); 
                        var callback = {
                            success : function(data) {
                                updateProgress(data);
                                UI.unblockUI(); 
                            }
                        };
                        var request = ConnectionManager.post('${pageContext.request.contextPath}/web/json/console/monitor/completed/process/archive/pause', callback, '');
                    }
                    return false;
                });

                //initial update
                updateProgress(${archivedProcessStatus});
            });
        </script>  
    </div>      
</c:if>    