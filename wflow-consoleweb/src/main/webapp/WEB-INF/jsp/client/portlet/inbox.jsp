<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>


<script>
    autoDetectJSLibrary(typeof jQuery == 'undefined', '${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/js/jquery/jquery-1.9.1.min.js');
    autoDetectJSLibrary(typeof jQuery == 'undefined', '${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/js/jquery/jquery-migrate-1.2.1.min.js');
    function autoDetectJSLibrary(jsObjUndefined, src){
        if (jsObjUndefined) {
            var objHead = window.document.getElementsByTagName('head')[0];
            var objScript = window.document.createElement('script');
            objScript.src = src;
            objScript.type = 'text/javascript';
            objHead.appendChild(objScript);
        }
    }
</script>

<script type="text/javascript" src="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/js/json/util.js"></script>
<script type="text/javascript" src="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/js/json/ui.js"></script>

<c:set var="id" value="1"/>
<c:if test="${!empty param.id}"><c:set var="id" value="${param.id}"/></c:if>

<c:set var="rowsPerPage" value="5"/>
<c:if test="${!empty param.rows}"><c:set var="rowsPerPage" value="${param.rows}"/></c:if>

<c:set var="packageId" value="-1"/>
<c:if test="${!empty param.packageId}"><c:set var="packageId" value="${param.packageId}"/></c:if>

<c:set var="processDefId" value="processDefId="/>
<c:if test="${!empty param.processDefId}"><c:set var="processDefId" value="processDefId=${param.processDefId}"/></c:if>

<script>
            var assignmentArray_${fn:escapeXml(id)};

            var inboxUrlPath_${fn:escapeXml(id)}='${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}';
            var inboxNumber_${fn:escapeXml(id)}=0;
            var inboxPrevious_${fn:escapeXml(id)}=0;
            var inboxNext_${fn:escapeXml(id)};
            var inboxNoOfPaging_${fn:escapeXml(id)};
            var inboxTotal_${fn:escapeXml(id)};
            var inboxRows_${fn:escapeXml(id)}='${fn:escapeXml(rowsPerPage)}';
            var inboxPackageId_${fn:escapeXml(id)}='${fn:escapeXml(packageId)}';
            var inboxSort_${fn:escapeXml(id)}='sort';
            var inboxDesc_${fn:escapeXml(id)}=true;

            var assignmentListCallback_${fn:escapeXml(id)} = {
                success : function(data) {
                    var data = data.data;

                    $('#assignmentList_${fn:escapeXml(id)}').html('');


                    if(data!=null){
                        if(!data.length) {
                            data = [data];
                        }
                        assignmentArray_${fn:escapeXml(id)} = data;
                        for(i=0; i<data.length; i++){
                            var assignmentObj = data[i];

                            var activityId = assignmentObj.activityId;
                            var activityName = assignmentObj.activityName;
                            var processName = assignmentObj.processName;
                            var processVersion = assignmentObj.processVersion;
                            var dateCreated = assignmentObj.dateCreated;
                            var acceptedStatus = assignmentObj.acceptedStatus;

                            inboxNumber_${fn:escapeXml(id)}++;

                            var inbox = '<div class="portlet_table_data">';
                            inbox += '    <span class="portlet_table_number">' + inboxNumber_${fn:escapeXml(id)} + '.</span>';

                            var cssClass = '';

                            var assignmentCallback = "inboxPopupDialog_${fn:escapeXml(id)}('" + activityId + "')";

                            <c:if test="${!empty param.assignmentCallback}">
                                assignmentCallback = "<c:out value="${param.assignmentCallback}" escapeXml="true"/>.success(assignmentArray_${fn:escapeXml(id)}[" + i + "])";
                            </c:if>

                            inbox += '    <a href="javascript: ' + assignmentCallback + ';" class="' + cssClass + '">' + UI.escapeHTML(activityName) + '</a>';

                            inbox += '    <span class="portlet_table_date_created">'+dateCreated+'</span>';
                            inbox += '    <div class="portlet_table_process">'+UI.escapeHTML(processName)+' - version '+processVersion+'</div>';
                            inbox += '</div>';

                            $('#assignmentList_${fn:escapeXml(id)}').append(inbox);
                        }
                    }
                    $('#inboxRefresh_${fn:escapeXml(id)}').attr("disabled", false);
                }
            }

            var assignmentCountCallback_${fn:escapeXml(id)} = {
                success : function(data) {
                    var count = data.total;
                    inboxTotal_${fn:escapeXml(id)} = count;
                    inboxNoOfPaging_${fn:escapeXml(id)} = count / inboxRows_${fn:escapeXml(id)};
                    if(inboxNoOfPaging_${fn:escapeXml(id)} > parseInt(count/inboxRows_${fn:escapeXml(id)})) inboxNoOfPaging_${fn:escapeXml(id)} = parseInt(count/inboxRows_${fn:escapeXml(id)}) + 1;
                    inboxNext_${fn:escapeXml(id)}=inboxNoOfPaging_${fn:escapeXml(id)};

                    if(inboxNext_${fn:escapeXml(id)}==1) $('#inboxNext_${fn:escapeXml(id)}').attr("disabled", true);

                    /*Reload page selection*/
                    $('#inboxPageTo_${fn:escapeXml(id)}').html('');
                    for(i=1; i<=inboxNoOfPaging_${fn:escapeXml(id)}; i++){
                        $('#inboxPageTo_${fn:escapeXml(id)}').append('<option value="'+i+'">' + i + '</option>');
                    }
                }
            }

            if(inboxPackageId_${fn:escapeXml(id)}!=-1 && inboxPackageId_${fn:escapeXml(id)}!='') {
                inboxPaging_${fn:escapeXml(id)}='packageId='+inboxPackageId_${fn:escapeXml(id)}+'&start=0&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};
                ConnectionManager.ajaxJsonp(inboxUrlPath_${fn:escapeXml(id)}+'/web/json/workflow/assignment/list/count?packageId='+inboxPackageId_${fn:escapeXml(id)}+'&${fn:escapeXml(processDefId)}', assignmentCountCallback_${fn:escapeXml(id)}, null);
            } else {
                inboxPaging_${fn:escapeXml(id)}='start=0&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};
                ConnectionManager.ajaxJsonp(inboxUrlPath_${fn:escapeXml(id)}+'/web/json/workflow/assignment/list/count?${fn:escapeXml(processDefId)}', assignmentCountCallback_${fn:escapeXml(id)}, null);
            }

            ConnectionManager.ajaxJsonp(inboxUrlPath_${fn:escapeXml(id)}+'/web/json/workflow/assignment/list?'+inboxPaging_${fn:escapeXml(id)}+'&${fn:escapeXml(processDefId)}', assignmentListCallback_${fn:escapeXml(id)}, null);

            function doInboxRefresh_${fn:escapeXml(id)}(){
                $('#inboxRefresh_${fn:escapeXml(id)}').attr("disabled", true);

                inboxNumber_${fn:escapeXml(id)}=0;
                inboxPrevious_${fn:escapeXml(id)}=0;
                inboxDesc_${fn:escapeXml(id)}=true;
                $('#inboxNext_${fn:escapeXml(id)}').attr("disabled", false);
                $('#inboxPrevious_${fn:escapeXml(id)}').attr("disabled", true);
                $('#inboxFilterByDateCreated_${fn:escapeXml(id)}').val("newest");

                if(inboxPackageId_${fn:escapeXml(id)}!=-1 && inboxPackageId_${fn:escapeXml(id)}!='') inboxPaging_${fn:escapeXml(id)}='packageId='+inboxPackageId_${fn:escapeXml(id)}+'&start=0&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};
                else inboxPaging_${fn:escapeXml(id)}='start=0&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};

                ConnectionManager.ajaxJsonp(inboxUrlPath_${fn:escapeXml(id)}+'/web/json/workflow/assignment/list?'+inboxPaging_${fn:escapeXml(id)}+'&${fn:escapeXml(processDefId)}', assignmentListCallback_${fn:escapeXml(id)}, null);
                ConnectionManager.ajaxJsonp(inboxUrlPath_${fn:escapeXml(id)}+'/web/json/workflow/assignment/list/count?${fn:escapeXml(processDefId)}', assignmentCountCallback_${fn:escapeXml(id)}, null);
            }

            function doInboxNext_${fn:escapeXml(id)}(){
                if(inboxNext_${fn:escapeXml(id)}>0 && inboxNext_${fn:escapeXml(id)}<=inboxNoOfPaging_${fn:escapeXml(id)}) {
                    inboxPrevious_${fn:escapeXml(id)}++;
                    inboxNext_${fn:escapeXml(id)}--;
                    inboxNumber_${fn:escapeXml(id)}=inboxPrevious_${fn:escapeXml(id)}*inboxRows_${fn:escapeXml(id)};

                    $('#inboxNext_${fn:escapeXml(id)}').attr("disabled", false);
                    $('#inboxPrevious_${fn:escapeXml(id)}').attr("disabled", false);

                    $('#inboxPageTo_${fn:escapeXml(id)}').val(inboxPrevious_${fn:escapeXml(id)}+1);

                    if(inboxNext_${fn:escapeXml(id)}==1) $('#inboxNext_${fn:escapeXml(id)}').attr("disabled", true);

                    if(inboxPackageId_${fn:escapeXml(id)}!=-1 && inboxPackageId_${fn:escapeXml(id)}!='') inboxPaging_${fn:escapeXml(id)}='packageId='+inboxPackageId_${fn:escapeXml(id)}+'&start='+(inboxPrevious_${fn:escapeXml(id)}*inboxRows_${fn:escapeXml(id)})+'&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};
                    else inboxPaging_${fn:escapeXml(id)}='start='+(inboxPrevious_${fn:escapeXml(id)}*inboxRows_${fn:escapeXml(id)})+'&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};

                    ConnectionManager.ajaxJsonp(inboxUrlPath_${fn:escapeXml(id)}+'/web/json/workflow/assignment/list?'+inboxPaging_${fn:escapeXml(id)}+'&${fn:escapeXml(processDefId)}', assignmentListCallback_${fn:escapeXml(id)}, null);
                } else $('#inboxNext_${fn:escapeXml(id)}').attr("disabled", true);
            }

            function doInboxPrevious_${fn:escapeXml(id)}(){
                if(inboxPrevious_${fn:escapeXml(id)}>0 && inboxPrevious_${fn:escapeXml(id)}<=inboxNoOfPaging_${fn:escapeXml(id)}) {
                    inboxNext_${fn:escapeXml(id)}++;
                    inboxPrevious_${fn:escapeXml(id)}--;
                    inboxNumber_${fn:escapeXml(id)}=inboxPrevious_${fn:escapeXml(id)}*inboxRows_${fn:escapeXml(id)};

                    $('#inboxNext_${fn:escapeXml(id)}').attr("disabled", false);
                    $('#inboxPrevious_${fn:escapeXml(id)}').attr("disabled", false);

                    $('#inboxPageTo_${fn:escapeXml(id)}').val(inboxPrevious_${fn:escapeXml(id)}+1);

                    if(inboxPrevious_${fn:escapeXml(id)}==0) $('#inboxPrevious_${fn:escapeXml(id)}').attr("disabled", true);

                    if(inboxPackageId_${fn:escapeXml(id)}!=-1 && inboxPackageId_${fn:escapeXml(id)}!='') inboxPaging_${fn:escapeXml(id)}='packageId='+inboxPackageId_${fn:escapeXml(id)}+'&start='+(inboxPrevious_${fn:escapeXml(id)}*inboxRows_${fn:escapeXml(id)})+'&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};
                    else inboxPaging_${fn:escapeXml(id)}='start='+(inboxPrevious_${fn:escapeXml(id)}*inboxRows_${fn:escapeXml(id)})+'&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};

                    ConnectionManager.ajaxJsonp(inboxUrlPath_${fn:escapeXml(id)}+'/web/json/workflow/assignment/list?'+inboxPaging_${fn:escapeXml(id)}+'&${fn:escapeXml(processDefId)}', assignmentListCallback_${fn:escapeXml(id)}, null);
                } else $('#inboxPrevious_${fn:escapeXml(id)}').attr("disabled", true);
            }

            function inboxSortedByDateCreated_${fn:escapeXml(id)}(){
                var filterByDateCreated = $('#inboxFilterByDateCreated_${fn:escapeXml(id)}').val();

                inboxNumber_${fn:escapeXml(id)}=0;

                if(filterByDateCreated=='newest') inboxDesc_${fn:escapeXml(id)}=true;
                else inboxDesc_${fn:escapeXml(id)}=false;

                $('#inboxPageTo_${fn:escapeXml(id)}').val("0");

                if(inboxPackageId_${fn:escapeXml(id)}!=-1 && inboxPackageId_${fn:escapeXml(id)}!='') inboxPaging_${fn:escapeXml(id)}='packageId='+inboxPackageId_${fn:escapeXml(id)}+'&start=0&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};
                else inboxPaging_${fn:escapeXml(id)}='start=0&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};

                inboxPrevious_${fn:escapeXml(id)}=0;
                inboxNext_${fn:escapeXml(id)}=inboxNoOfPaging_${fn:escapeXml(id)};
                if(inboxNext_${fn:escapeXml(id)}!=1) $('#inboxNext_${fn:escapeXml(id)}').attr("disabled", false);
                $('#inboxPrevious_${fn:escapeXml(id)}').attr("disabled", true);

                ConnectionManager.ajaxJsonp(inboxUrlPath_${fn:escapeXml(id)}+'/web/json/workflow/assignment/list?'+inboxPaging_${fn:escapeXml(id)}+'&${fn:escapeXml(processDefId)}', assignmentListCallback_${fn:escapeXml(id)}, null);
            }

            function doInboxPage_${fn:escapeXml(id)}(){
                var pageOf = $('#inboxPageTo_${fn:escapeXml(id)}').val();

                inboxNumber_${fn:escapeXml(id)}=(pageOf-1)*inboxRows_${fn:escapeXml(id)};

                if(inboxPackageId_${fn:escapeXml(id)}!=-1 && inboxPackageId_${fn:escapeXml(id)}!='') inboxPaging_${fn:escapeXml(id)}='packageId='+inboxPackageId_${fn:escapeXml(id)}+'&start='+((pageOf-1)*inboxRows_${fn:escapeXml(id)})+'&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};
                else inboxPaging_${fn:escapeXml(id)}='start='+((pageOf-1)*inboxRows_${fn:escapeXml(id)})+'&rows='+inboxRows_${fn:escapeXml(id)}+'&sort='+inboxSort_${fn:escapeXml(id)}+'&desc='+inboxDesc_${fn:escapeXml(id)};

                if(pageOf == inboxNoOfPaging_${fn:escapeXml(id)}) {
                    $('#inboxNext_${fn:escapeXml(id)}').attr("disabled", true);
                    $('#inboxPrevious_${fn:escapeXml(id)}').attr("disabled", false);
                    inboxNext_${fn:escapeXml(id)}=1;
                    inboxPrevious_${fn:escapeXml(id)}=inboxNoOfPaging_${fn:escapeXml(id)}-1;
                } else if(pageOf == 1) {
                    $('#inboxPrevious_${fn:escapeXml(id)}').attr("disabled", true);
                    $('#inboxNext_${fn:escapeXml(id)}').attr("disabled", false);
                    inboxPrevious_${fn:escapeXml(id)}=0;
                    inboxNext_${fn:escapeXml(id)}=inboxNoOfPaging_${fn:escapeXml(id)};
                } else {
                    $('#inboxPrevious_${fn:escapeXml(id)}').attr("disabled", false);
                    $('#inboxNext_${fn:escapeXml(id)}').attr("disabled", false);
                    inboxPrevious_${fn:escapeXml(id)}=pageOf-1;
                    inboxNext_${fn:escapeXml(id)}=inboxNoOfPaging_${fn:escapeXml(id)}-inboxPrevious_${fn:escapeXml(id)};
                }

                ConnectionManager.ajaxJsonp(inboxUrlPath_${fn:escapeXml(id)}+'/web/json/workflow/assignment/list?'+inboxPaging_${fn:escapeXml(id)}+'&${fn:escapeXml(processDefId)}', assignmentListCallback_${fn:escapeXml(id)}, null);
            }

            function inboxPopupDialog_${fn:escapeXml(id)}(activityId){
                var url = inboxUrlPath_${fn:escapeXml(id)}+"/web/client/app/assignment/"+activityId;

                window.open(url,'_blank','height=500,width=800,scrollbars=1');
            }

            function closeDialog() {
                doInboxRefresh_${fn:escapeXml(id)}();
            }

</script>

<div id="portlet_inbox_${fn:escapeXml(id)}">
    <div class="portlet_div_search">
        <div class="portlet_div_sorting">
            <span class="portlet_label"><fmt:message key="client.protlet.inbox.label.sortedBy"/></span>
            <span class="portlet_input">
                <select id="inboxFilterByDateCreated_${fn:escapeXml(id)}" name="dateCreated" onchange="inboxSortedByDateCreated_${fn:escapeXml(id)}()">
                    <option value="newest"><fmt:message key="client.protlet.inbox.label.newest"/></option>
                    <option value="oldest"><fmt:message key="client.protlet.inbox.label.oldest"/></option>
                </select>
            </span>
            <span>
                <input class="button" type="button" id="inboxRefresh_${fn:escapeXml(id)}" value="<fmt:message key='general.method.label.refresh'/>" onclick="doInboxRefresh_${fn:escapeXml(id)}();">
            </span>
        </div>

        <div class="portlet_div_paging">
            <span class="portlet_label"><fmt:message key="client.protlet.inbox.label.page"/></span>
            <span class="portlet_input">
                <select id="inboxPageTo_${fn:escapeXml(id)}" name="pageTo" onchange="doInboxPage_${fn:escapeXml(id)}()"></select>
            </span>
            <span><input class="button" type="button" id="inboxPrevious_${fn:escapeXml(id)}" value="<fmt:message key='general.method.label.previous'/>" onclick="doInboxPrevious_${fn:escapeXml(id)}();" disabled></span>
            <span><input class="button" type="button" id="inboxNext_${fn:escapeXml(id)}" value="<fmt:message key='general.method.label.next'/>" onclick="doInboxNext_${fn:escapeXml(id)}();"></span>
        </div>
    </div>

    <div class="portlet_table_list" id="assignmentList_${fn:escapeXml(id)}"></div>
</div>