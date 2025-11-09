<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.apps.app.service.AppUtil"%>

<c:set var="status" value="<%= AppUtil.getDeleteAllCompletedProcessesStatus() %>"/>

<div id="deleteAllProcessesStatus" class="alert alert-info" style="margin-top:10px; display: none;">
    <p>
        <ui:msgEscJS key="console.monitoring.deleteAllProgress"/> <span id="percentageValue" style="display:inline-block; margin-right: 20px;"></span>
        <button id="resume" class="btn" style="display:none"><fmt:message key="console.monitoring.resume"/></button>
        <button id="pause" class="btn" style="display:none"><fmt:message key="console.monitoring.pause"/></button>
        <button id="dismiss" class="btn" style="display:none"><fmt:message key="console.monitoring.dismiss"/></button>
        <button id="abort" class="btn" style="display:none"><fmt:message key="console.monitoring.abort"/></button>
    </p>
    <div class="progress">
        <div class="progress-bar progress-bar-animated" style="width: 0%"></div>
    </div>
    <script>
        $(function(){
            var msg = {
                "resume" : "<ui:msgEscJS key="console.monitoring.resumeDeleteAll"/>",
                "pause" : "<ui:msgEscJS key="console.monitoring.pauseDeleteAll"/>",
                "abort" : "<ui:msgEscJS key="console.monitoring.abortDeleteAll"/>"
            };

            //update the progress bar, button and the precentage text
            var updateDeleteProgress = function(data){
                
                var precentage = parseFloat(data);
                if (precentage !== 0 && $("#deleteAllProcessesStatus").is(":hidden")) {
                    $("#deleteAllProcessesStatus").show();
                    $('#deleteAllProcessesStatus').get(0).scrollIntoView();
                }
                $("#deleteAllProcessesStatus .btn").hide();

                if (precentage >= 100) {
                    $("#deleteAllProcessesStatus #dismiss").show();

                    //reload the json table to get latest data
                    JsonDataTable.refresh();
                } else if (precentage > 0) {
                    $("#deleteAllProcessesStatus #pause, #deleteAllProcessesStatus #abort").show();

                    //retrieve latest progress
                    setTimeout(function(){
                        var callback = {
                            success : function(data) {
                                updateDeleteProgress(data);
                            }
                        };
                        var request = ConnectionManager.post('${pageContext.request.contextPath}/web/json/console/monitor/completed/process/deleteAll/status', callback, '');
                    }, 3000);
                } else if (precentage === 0) {
                    $("#deleteAllProcessesStatus").hide();
                    return;
                } else {
                    $("#deleteAllProcessesStatus #resume, #deleteAllProcessesStatus #abort").show();
                    precentage = precentage * -1;
                }
                $("#deleteAllProcessesStatus .progress-bar").css("width", precentage.toFixed(2) + "%");
                $("#deleteAllProcessesStatus #percentageValue").text(precentage.toFixed(0) + "%");
            };

            var callAction = function(action) {
                UI.blockUI(); 
                var callback = {
                    success : function(data) {
                        updateDeleteProgress(data);
                        UI.unblockUI(); 
                    }
                };
                ConnectionManager.post('${pageContext.request.contextPath}/web/json/console/monitor/completed/process/deleteAll/' + action, callback, '');
            };

            //handle button event
            $("#deleteAllProcessesStatus .btn").on('click', function(){
                var action = $(this).attr("id");

                if (action === "dismiss") {
                    callAction(action);
                } else {
                    UI.confirm(msg[action], 
                        () => {
                            callAction(action);
                        }, {
                            confirmButtonLabel: $(this).text()
                        }
                    );
                }
                return false;
            });

            //initial update
            updateDeleteProgress(${status});
            
            
            $("#JsonDataTable_processList-buttons").append('<button id="deleteAllBtn" type="button" class="console-danger"><ui:msgEscJS key="console.monitoring.deleteAll"/></button>');
            
            $("#deleteAllBtn").on("click", function(){
                UI.confirm('<ui:msgEscJS key="console.monitoring.deleteAll.comfirm"/>', 
                    () => {
                        callAction("start");
                    }, {
                        confirmButtonLabel: '<ui:msgEscJS key="console.monitoring.deleteAll"/>'
                    }    
                );
                return false;
            });
        });
    </script>  
</div>        