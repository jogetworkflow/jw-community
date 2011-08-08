<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>


<script>
    autoDetectJSLibrary(typeof jQuery == 'undefined', '${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/js/jquery/jquery-1.4.4.min.js');

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

<c:set var="id" value="1"/>
<c:if test="${!empty param.id}"><c:set var="id" value="${param.id}"/></c:if>

<c:set var="rowsPerPage" value="5"/>
<c:if test="${!empty param.rows}"><c:set var="rowsPerPage" value="${param.rows}"/></c:if>

<c:set var="packageId" value="-1"/>
<c:if test="${!empty param.packageId}"><c:set var="packageId" value="${param.packageId}"/></c:if>

<script>
            var assignmentArray_${id};

            var inboxUrlPath_${id}='${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}';
            var inboxNumber_${id}=0;
            var inboxPrevious_${id}=0;
            var inboxNext_${id};
            var inboxNoOfPaging_${id};
            var inboxTotal_${id};
            var inboxRows_${id}='${rowsPerPage}';
            var inboxPackageId_${id}='${packageId}';
            var inboxSort_${id}='sort';
            var inboxDesc_${id}=true;

            var assignmentListCallback_${id} = {
                success : function(data) {
                    var data = data.data;

                    $('#assignmentList_${id}').html('');


                    if(data!=null){
                        if(!data.length) {
                            data = [data];
                        }
                        assignmentArray_${id} = data;
                        for(i=0; i<data.length; i++){
                            var assignmentObj = data[i];

                            var activityId = assignmentObj.activityId;
                            var activityName = assignmentObj.activityName;
                            var processName = assignmentObj.processName;
                            var processVersion = assignmentObj.processVersion;
                            var dateCreated = assignmentObj.dateCreated;
                            var acceptedStatus = assignmentObj.acceptedStatus;

                            inboxNumber_${id}++;

                            var inbox = '<div class="portlet_table_data">';
                            inbox += '    <span class="portlet_table_number">' + inboxNumber_${id} + '.</span>';

                            var cssClass = '';

                            var assignmentCallback = "inboxPopupDialog_${id}('" + activityId + "')";

                            <c:if test="${!empty param.assignmentCallback}">
                                assignmentCallback = "${param.assignmentCallback}.success(assignmentArray_${id}[" + i + "])";
                            </c:if>

                            inbox += '    <a href="javascript: ' + assignmentCallback + ';" class="' + cssClass + '">' + activityName + '</a>';

                            inbox += '    <span class="portlet_table_date_created">'+dateCreated+'</span>';
                            inbox += '    <div class="portlet_table_process">'+processName+' - version '+processVersion+'</div>';
                            inbox += '</div>';

                            $('#assignmentList_${id}').append(inbox);
                        }
                    }
                    $('#inboxRefresh_${id}').attr("disabled", false);
                }
            }

            var assignmentCountCallback_${id} = {
                success : function(data) {
                    var count = data.total;
                    inboxTotal_${id} = count;
                    inboxNoOfPaging_${id} = count / inboxRows_${id};
                    if(inboxNoOfPaging_${id} > parseInt(count/inboxRows_${id})) inboxNoOfPaging_${id} = parseInt(count/inboxRows_${id}) + 1;
                    inboxNext_${id}=inboxNoOfPaging_${id};

                    if(inboxNext_${id}==1) $('#inboxNext_${id}').attr("disabled", true);

                    /*Reload page selection*/
                    $('#inboxPageTo_${id}').html('');
                    for(i=1; i<=inboxNoOfPaging_${id}; i++){
                        $('#inboxPageTo_${id}').append('<option value="'+i+'">' + i + '</option>');
                    }
                }
            }

            if(inboxPackageId_${id}!=-1 && inboxPackageId_${id}!='') {
                inboxPaging_${id}='packageId='+inboxPackageId_${id}+'&start=0&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};
                ConnectionManager.ajaxJsonp(inboxUrlPath_${id}+'/web/json/workflow/assignment/list/count?packageId='+inboxPackageId_${id}, assignmentCountCallback_${id}, null);
            } else {
                inboxPaging_${id}='start=0&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};
                ConnectionManager.ajaxJsonp(inboxUrlPath_${id}+'/web/json/workflow/assignment/list/count', assignmentCountCallback_${id}, null);
            }

            ConnectionManager.ajaxJsonp(inboxUrlPath_${id}+'/web/json/workflow/assignment/list?'+inboxPaging_${id}, assignmentListCallback_${id}, null);

            function doInboxRefresh_${id}(){
                $('#inboxRefresh_${id}').attr("disabled", true);

                inboxNumber_${id}=0;
                inboxPrevious_${id}=0;
                inboxDesc_${id}=true;
                $('#inboxNext_${id}').attr("disabled", false);
                $('#inboxPrevious_${id}').attr("disabled", true);
                $('#inboxFilterByDateCreated_${id}').val("newest");

                if(inboxPackageId_${id}!=-1 && inboxPackageId_${id}!='') inboxPaging_${id}='packageId='+inboxPackageId_${id}+'&start=0&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};
                else inboxPaging_${id}='start=0&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};

                ConnectionManager.ajaxJsonp(inboxUrlPath_${id}+'/web/json/workflow/assignment/list?'+inboxPaging_${id}, assignmentListCallback_${id}, null);
                ConnectionManager.ajaxJsonp(inboxUrlPath_${id}+'/web/json/workflow/assignment/list/count', assignmentCountCallback_${id}, null);
            }

            function doInboxNext_${id}(){
                if(inboxNext_${id}>0 && inboxNext_${id}<=inboxNoOfPaging_${id}) {
                    inboxPrevious_${id}++;
                    inboxNext_${id}--;
                    inboxNumber_${id}=inboxPrevious_${id}*inboxRows_${id};

                    $('#inboxNext_${id}').attr("disabled", false);
                    $('#inboxPrevious_${id}').attr("disabled", false);

                    $('#inboxPageTo_${id}').val(inboxPrevious_${id}+1);

                    if(inboxNext_${id}==1) $('#inboxNext_${id}').attr("disabled", true);

                    if(inboxPackageId_${id}!=-1 && inboxPackageId_${id}!='') inboxPaging_${id}='packageId='+inboxPackageId_${id}+'&start='+(inboxPrevious_${id}*inboxRows_${id})+'&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};
                    else inboxPaging_${id}='start='+(inboxPrevious_${id}*inboxRows_${id})+'&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};

                    ConnectionManager.ajaxJsonp(inboxUrlPath_${id}+'/web/json/workflow/assignment/list?'+inboxPaging_${id}, assignmentListCallback_${id}, null);
                } else $('#inboxNext_${id}').attr("disabled", true);
            }

            function doInboxPrevious_${id}(){
                if(inboxPrevious_${id}>0 && inboxPrevious_${id}<=inboxNoOfPaging_${id}) {
                    inboxNext_${id}++;
                    inboxPrevious_${id}--;
                    inboxNumber_${id}=inboxPrevious_${id}*inboxRows_${id};

                    $('#inboxNext_${id}').attr("disabled", false);
                    $('#inboxPrevious_${id}').attr("disabled", false);

                    $('#inboxPageTo_${id}').val(inboxPrevious_${id}+1);

                    if(inboxPrevious_${id}==0) $('#inboxPrevious_${id}').attr("disabled", true);

                    if(inboxPackageId_${id}!=-1 && inboxPackageId_${id}!='') inboxPaging_${id}='packageId='+inboxPackageId_${id}+'&start='+(inboxPrevious_${id}*inboxRows_${id})+'&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};
                    else inboxPaging_${id}='start='+(inboxPrevious_${id}*inboxRows_${id})+'&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};

                    ConnectionManager.ajaxJsonp(inboxUrlPath_${id}+'/web/json/workflow/assignment/list?'+inboxPaging_${id}, assignmentListCallback_${id}, null);
                } else $('#inboxPrevious_${id}').attr("disabled", true);
            }

            function inboxSortedByDateCreated_${id}(){
                var filterByDateCreated = $('#inboxFilterByDateCreated_${id}').val();

                inboxNumber_${id}=0;

                if(filterByDateCreated=='newest') inboxDesc_${id}=true;
                else inboxDesc_${id}=false;

                $('#inboxPageTo_${id}').val("0");

                if(inboxPackageId_${id}!=-1 && inboxPackageId_${id}!='') inboxPaging_${id}='packageId='+inboxPackageId_${id}+'&start=0&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};
                else inboxPaging_${id}='start=0&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};

                inboxPrevious_${id}=0;
                inboxNext_${id}=inboxNoOfPaging_${id};
                if(inboxNext_${id}!=1) $('#inboxNext_${id}').attr("disabled", false);
                $('#inboxPrevious_${id}').attr("disabled", true);

                ConnectionManager.ajaxJsonp(inboxUrlPath_${id}+'/web/json/workflow/assignment/list?'+inboxPaging_${id}, assignmentListCallback_${id}, null);
            }

            function doInboxPage_${id}(){
                var pageOf = $('#inboxPageTo_${id}').val();

                inboxNumber_${id}=(pageOf-1)*inboxRows_${id};

                if(inboxPackageId_${id}!=-1 && inboxPackageId_${id}!='') inboxPaging_${id}='packageId='+inboxPackageId_${id}+'&start='+((pageOf-1)*inboxRows_${id})+'&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};
                else inboxPaging_${id}='start='+((pageOf-1)*inboxRows_${id})+'&rows='+inboxRows_${id}+'&sort='+inboxSort_${id}+'&desc='+inboxDesc_${id};

                if(pageOf == inboxNoOfPaging_${id}) {
                    $('#inboxNext_${id}').attr("disabled", true);
                    $('#inboxPrevious_${id}').attr("disabled", false);
                    inboxNext_${id}=1;
                    inboxPrevious_${id}=inboxNoOfPaging_${id}-1;
                } else if(pageOf == 1) {
                    $('#inboxPrevious_${id}').attr("disabled", true);
                    $('#inboxNext_${id}').attr("disabled", false);
                    inboxPrevious_${id}=0;
                    inboxNext_${id}=inboxNoOfPaging_${id};
                } else {
                    $('#inboxPrevious_${id}').attr("disabled", false);
                    $('#inboxNext_${id}').attr("disabled", false);
                    inboxPrevious_${id}=pageOf-1;
                    inboxNext_${id}=inboxNoOfPaging_${id}-inboxPrevious_${id};
                }

                ConnectionManager.ajaxJsonp(inboxUrlPath_${id}+'/web/json/workflow/assignment/list?'+inboxPaging_${id}, assignmentListCallback_${id}, null);
            }

            function inboxPopupDialog_${id}(activityId){
                var url = inboxUrlPath_${id}+"/web/client/app/assignment/"+activityId;

                window.open(url,'_blank','height=500,width=800,scrollbars=1');
            }

            function closeDialog() {
                doInboxRefresh_${id}();
            }

</script>

<div id="portlet_inbox_${id}">
    <div class="portlet_div_search">
        <div class="portlet_div_sorting">
            <span class="portlet_label"><fmt:message key="client.protlet.inbox.label.sortedBy"/></span>
            <span class="portlet_input">
                <select id="inboxFilterByDateCreated_${id}" name="dateCreated" onchange="inboxSortedByDateCreated_${id}()">
                    <option value="newest"><fmt:message key="client.protlet.inbox.label.newest"/></option>
                    <option value="oldest"><fmt:message key="client.protlet.inbox.label.oldest"/></option>
                </select>
            </span>
            <span>
                <input class="button" type="button" id="inboxRefresh_${id}" value="<fmt:message key='general.method.label.refresh'/>" onclick="doInboxRefresh_${id}();">
            </span>
        </div>

        <div class="portlet_div_paging">
            <span class="portlet_label"><fmt:message key="client.protlet.inbox.label.page"/></span>
            <span class="portlet_input">
                <select id="inboxPageTo_${id}" name="pageTo" onchange="doInboxPage_${id}()"></select>
            </span>
            <span><input class="button" type="button" id="inboxPrevious_${id}" value="<fmt:message key='general.method.label.previous'/>" onclick="doInboxPrevious_${id}();" disabled></span>
            <span><input class="button" type="button" id="inboxNext_${id}" value="<fmt:message key='general.method.label.next'/>" onclick="doInboxNext_${id}();"></span>
        </div>
    </div>

    <div class="portlet_table_list" id="assignmentList_${id}"></div>
</div>
