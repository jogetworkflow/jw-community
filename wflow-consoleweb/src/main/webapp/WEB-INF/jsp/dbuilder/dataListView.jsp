<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.commons.util.LogUtil"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<c:set scope="request" var="dataListId" value="${dataList.id}"/>
<style>
    .dataList .pagebanner, .dataList .pagebanner + .exportlinks {
        width: 50% !important;
        display: inline-block !important;
        margin: 0px !important;
        background: transparent !important;
        border: none !important;
    }
    .size_sm *:is(.dataList .pagebanner, .dataList .pagebanner + .exportlinks) {
        font-size: 11px;
    }
    .dataList .pagebanner {
        text-align: start !important;
    }
    .dataList .exportlinks {
        text-align: end !important;
    }
    @media (max-width: 425px) {
        .dataList .pagebanner, .dataList .pagebanner + .exportlinks {
            width: 100% !important;
            text-align: center !important;
            margin-bottom: 5px !important;
        }
    }
</style>
<div id="dataList_${dataList.id}" data-responsivemode="${dataList.responsiveMode}" class="dataList <c:if test="${!dataList.isAuthorized}">unauthorized</c:if> <c:if test="${empty dataList.actions}">no_action</c:if> <c:if test="${dataList.noExport}">no_export</c:if>">
    <c:choose>
        <c:when test="${dataList.isAuthorized}">
            <script type="text/javascript" src="${pageContext.request.contextPath}/js/footable/responsiveTable.js?build=<fmt:message key="build.number"/>" defer></script>
            <link rel="preload" href="${pageContext.request.contextPath}/js/footable/fonts/footable.woff" as="font" crossorigin />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles/datalist/datalist-mobile.css" />
            
            <c:set var="isQuickEditEnabled" value="<%= AppUtil.isQuickEditEnabled() %>"/>
            <c:if test="${isQuickEditEnabled && !dataList.disableQuickEdit}">
            <div class="quickEdit" style="display: none">
                <a href="<c:out value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/datalist/builder/${dataList.id}"/>" target="_blank"><i class="fas fa-pencil-alt"></i>  <fmt:message key="adminBar.label.list"/>: <c:out value="${dataList.name}"/></a>
            </div>
            </c:if>

            <c:catch var="dataListException">

                <c:set var="actionResult" value="${dataList.actionResult}" />
                <c:set var="redirected" value="false" />
                <c:if test="${!empty actionResult}">
                    <c:if test="${!empty actionResult.message}">
                        <script>
                            UI.alert("<c:out value="${actionResult.message}"/>");
                        </script>
                    </c:if>
                    <c:choose>
                        <c:when test="${actionResult.type == 'REDIRECT' && actionResult.url == 'REFERER'}">
                            <c:set var="redirected" value="true" />
                            <script>
                                location.href = "<ui:escape value="${header['Referer']}" format="javascript"/>";
                            </script>
                        </c:when>
                        <c:when test="${actionResult.type == 'REDIRECT'  && !empty actionResult.url}">
                            <c:set var="redirected" value="true" />
                            <script>
                                location.href = "<ui:escape value="${actionResult.url}" format="javascript"/>";
                            </script>
                        </c:when>
                        <c:otherwise>   

                        </c:otherwise>
                    </c:choose>
                </c:if>
                <c:if test="${!redirected}">
                    <c:catch var="dataListBinderException">
                        <c:set var="dataListRows" scope="request" value="${dataList.rows}"/>
                    </c:catch>
                    <c:set var="dataListSize" scope="request" value="${dataList.size}"/>
                    <c:set var="dataListPageSize" scope="request" value="${dataList.pageSize}"/>
                    <c:set var="decorator" scope="request" value="${dataList.primaryKeyDecorator}"/>

                    <!-- set default button position if value is null -->
                    <c:set var="buttonPosition" value="${dataList.actionPosition}" />

                    <c:set var="buttonFloat" value="left" />
                    <c:if test="${buttonPosition eq 'topRight' || buttonPosition eq 'bottomRight' || buttonPosition eq 'bothRight'}">
                        <c:set var="buttonFloat" value="right" />
                    </c:if>

                    <!-- set checkbox position if value is null -->
                    <c:set var="checkboxPosition" value="${dataList.checkboxPosition}" />

                    <c:set var="selectionType" value="multiple" />
                    <c:if test="${dataList.selectionType eq 'single'}">
                        <c:set var="selectionType" value="single" />
                    </c:if>

                    <c:if test="${!empty dataListBinderException}">
                        <%
                        String exceptionMessage = "";
                        Throwable cause = (Throwable)pageContext.findAttribute("dataListBinderException");
                        while(cause.getCause() != null) {
                            cause = cause.getCause();
                        }
                        exceptionMessage = cause.getMessage();
                        %>
                        <div class="datalist-error"><c:out value="<%= exceptionMessage %>"/></div>
                    </c:if>
                    <c:if test="${!empty dataList.binder && !empty dataList.binder.properties.errorMsg}">
                        <div class="datalist-error"><c:out value="${dataList.binder.properties.errorMsg}"/></div>
                    </c:if>
                        
                    <c:set var="templateHtml">
                        ${dataList.html}
                    </c:set>    
                        
                    <style>
                        ${dataList.styles}
                    </style>    

                    <!-- Display Filters -->        
                    <c:if test="${fn:length(dataList.filterTemplates) gt 1}">
                        <c:if test="${dataList.returnNoDataWhenFilterNotSet}">
                            <p class="msg setFilterToSeeData">
                                <fmt:message key="dbuilder.pleaseSubmitFilter"/>
                            </p>    
                        </c:if>    
                        <form name="filters_${dataListId}" class="filter_form" id="filters_${dataListId}" action="?" method="GET">
                            <a class="mobile_search_trigger" title="<ui:msgEscHTML key="dbuilder.filter"/>"><i class="fas fa-filter"></i> <fmt:message key="dbuilder.filter"/></a>
                            <div class="filters">
                                <c:forEach items="${dataList.filterTemplates}" var="template">
                                    ${template}
                                </c:forEach>
                                 <span class="filter-cell">
                                     <input type="submit" class="form-button btn btn-secondary btn-sm button" value="<ui:msgEscHTML key="general.method.label.show"/>"/>
                                 </span>
                            </div>
                        </form>
                    </c:if>

                    <!-- Display Main Table -->
                    <c:set var="qs"><ui:decodeurl value="${queryString}"/></c:set>
                    <form name="form_${dataListId}" action="?<c:out value="${qs}" escapeXml="true"/>" method="POST">
                        <!-- Display Buttons -->
                        <c:if test="${buttonPosition eq 'topLeft' || buttonPosition eq 'topRight' || buttonPosition eq 'bothLeft' || buttonPosition eq 'bothRight'}">
                            <c:if test="${!empty dataList.actions}">
                                <div class="actions top ${buttonFloat}">
                                    <c:forEach items="${dataList.actions}" var="action">
                                        <c:if test="${!(empty dataListRows[0] || checkboxPosition eq 'no') || action.visibleOnNoRecord}">
                                            <c:set var="buttonConfirmation" value="" />
                                            <c:if test="${!empty action.confirmation}">
                                                <c:set var="buttonConfirmation" value=" data-confirmation=\"${fn:escapeXml(action.confirmation)}\""/>
                                            </c:if>
                                            <c:set var="buttonCssClasses" value="" />
                                            <c:if test="${!empty action.properties.cssClasses}">
                                                <c:set var="buttonCssClasses" value="${action.properties.cssClasses}"/>
                                            </c:if>
                                            <button data-target="<c:out value="${action.target}" escapeXml="true"/>" data-href="<c:out value="${action.href}" escapeXml="true"/>" data-hrefParam="<c:out value="${action.hrefParam}" escapeXml="true"/>" name="<c:out value="${dataList.actionParamName}" escapeXml="true"/>" class="form-button btn button <c:out value="${buttonCssClasses}" escapeXml="true"/> <c:out value="${action.properties.id}" escapeXml="true"/> ${action.properties.BUILDER_GENERATED_CSS}" ${action.properties.BUILDER_GENERATED_ATTR} value="<c:out value="${action.properties.id}" escapeXml="true"/>" ${buttonConfirmation}>${action.linkLabel}</button>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </c:if>        
                        </c:if>
                        
                        <c:choose>
                            <c:when test="${!empty templateHtml}">
                                <div class="table-wrapper">
                                    <c:out value="${templateHtml}" escapeXml="false"/>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <c:if test="${!empty dataList.properties.cardCollapsible && dataList.properties.cardCollapsible eq 'true'}">
                                    <div class="collapsibleBtns" style="display:none">
                                        <a class="expandAll"><i class="fas fa-plus-square"></i> <fmt:message key="dbuilder.expandAll"/></a>
                                        <a class="collapseAll"><i class="fas fa-minus-square"></i> <fmt:message key="dbuilder.collapseAll"/></a>
                                    </div>    
                                </c:if>
                                <div class="table-wrapper" data-disableresponsive="${dataList.disableResponsive}">
                                    <c:set var="tableStyle" value=""/>
                                    <c:if test="${!empty dataList.properties.draggabletable && dataList.properties.draggabletable eq 'true'}">
                                        <c:set var="tableStyle" value="draggabletable"/>
                                    </c:if>
                                    <c:if test="${!empty dataList.properties.showhidecolumns && dataList.properties.showhidecolumns eq 'true'}">
                                        <c:set var="tableStyle" value="${tableStyle} showhidecolumns"/>
                                    </c:if>
                                    <c:if test="${!empty dataList.properties.cardCollapsible && dataList.properties.cardCollapsible eq 'true'}">
                                        <c:set var="tableStyle" value="${tableStyle} cardCollapsible"/>
                                        
                                        <c:if test="${!empty dataList.properties.cardCollapseByDefault && dataList.properties.cardCollapseByDefault eq 'true'}">
                                            <c:set var="tableStyle" value="${tableStyle} cardCollapseByDefault"/>
                                        </c:if>
                                    </c:if>
                                   <display:table id="${dataListId}" uid="${dataListId}" name="dataListRows" pagesize="${dataListPageSize}" class="xrounded_shadowed responsivetable ${tableStyle}" export="true" decorator="decorator" excludedParams="${dataList.binder.primaryKeyColumnName}" requestURI="?" sort="external" partialList="true" size="dataListSize">
                                       <c:if test="${checkboxPosition eq 'left' || checkboxPosition eq 'both'}">
                                           <c:choose>
                                               <c:when test="${selectionType eq 'single'}">
                                                   <display:column headerClass="select_radio" class="select_radio" property="radio" media="html" title="" />
                                               </c:when>
                                               <c:otherwise>
                                                   <fmt:message key="dbuilder.selectAll" var="selectAllLabel" />
                                                   <display:column headerClass="select_checkbox" class="select_checkbox" property="checkbox" media="html" title="<label><input class='form-check-input' type='checkbox' title='${selectAllLabel}' onclick='toggleAll(this)' style='float:left;'/><i></i></label>" />
                                               </c:otherwise>
                                           </c:choose>
                                       </c:if>
                                       <c:forEach items="${dataList.columns}" var="column">
                                           <c:set var="columnLabel"><c:out value="${column.label}"/></c:set>
                                           <c:set var="columnHiddenCss" value=""/>
                                           <c:set var="columnMedia" value="all"/>
                                           <c:choose> 
                                               <c:when test="${column.hidden}">
                                                   <c:set var="columnHiddenCss" value=" column-hidden"/>
                                                   <c:if test="${column.properties.include_export ne 'true'}">
                                                       <c:set var="columnMedia" value="html"/>
                                                   </c:if>
                                               </c:when>
                                               <c:otherwise>
                                                   <c:if test="${column.properties.exclude_export eq 'true'}">
                                                       <c:set var="columnMedia" value="html"/>
                                                   </c:if>
                                               </c:otherwise>
                                           </c:choose>
                                           <display:column
                                               property="column(${column.name})"
                                               title="${columnLabel}"
                                               sortable="${column.sortable}"
                                               headerClass="column_header column_${column.name} ${columnHiddenCss} ${column.headerAlignment} header_${column.properties.id} ${column.properties.BUILDER_GENERATED_HEADER_CSS} ${column.properties.extraClassIdentifier}"
                                               class="column_body  column_${column.name} ${columnHiddenCss} ${column.alignment} body_${column.properties.id} ${column.properties.BUILDER_GENERATED_CSS} ${column.properties.extraClassIdentifier}"
                                               style="${column.style}"
                                               media="${columnMedia}"
                                               />
                                       </c:forEach>
                                       <c:if test="${!empty dataListRows[0] && !empty dataList.rowActions[0]}">
                                           <c:set var="actionTitle" value="" />
                                           <c:set var="firstCssClass" value="" />
                                           <c:forEach items="${dataList.rowActions}" var="rowAction" varStatus="rowActionStatus" >
                                               <c:set var="headerTitle" value=""/>
                                               <c:if test="${!empty rowAction.properties.header_label}">
                                                   <c:set var="headerTitle" value="${rowAction.properties.header_label}"/>
                                               </c:if>
                                               <c:choose>
                                                   <c:when test="${rowActionStatus.index == 0}">
                                                       <c:set var="actionTitle" value="${headerTitle}" />
                                                       <c:set var="firstHeaderCssClass" value="rowaction_header footable-visible header_${rowAction.properties.id} ${rowAction.properties.BUILDER_GENERATED_HEADER_CSS}" />
                                                       <c:set var="firstBodyCssClass" value="rowaction_body row_action_inner body_${rowAction.properties.id} ${rowAction.properties.BUILDER_GENERATED_CSS} ${rowAction.properties.extraClassIdentifier}" />
                                                   </c:when>
                                                   <c:when test="${rowActionStatus.last}">
                                                       <c:set var="actionTitle" value="${actionTitle}</th><th class=\"row_action rowaction_header footable-last-column row_action_last footable-visible header_${rowAction.properties.id} ${rowAction.properties.BUILDER_GENERATED_HEADER_CSS}\">${headerTitle}" />
                                                   </c:when>
                                                   <c:otherwise>
                                                       <c:set var="actionTitle" value="${actionTitle}</th><th class=\"row_action rowaction_header footable-visible header_${rowAction.properties.id} ${rowAction.properties.BUILDER_GENERATED_HEADER_CSS}\">${headerTitle}" />
                                                   </c:otherwise>
                                                </c:choose>
                                           </c:forEach>
                                           <c:if test="${!empty dataList.properties.rowActionsMode && (dataList.properties.rowActionsMode eq 'true' || dataList.properties.rowActionsMode eq 'dropdown')}">
                                               <c:set var="actionTitle" value=""/>
                                               <c:set var="firstHeaderCssClass" value="rowaction_header"/>
                                               <c:set var="firstBodyCssClass" value="rowaction_body ${rowAction.properties.extraClassIdentifier}"/>
                                           </c:if>
                                           <display:column headerClass="row_action ${firstHeaderCssClass}" class="row_action ${firstBodyCssClass}" property="actions" media="html" title="${actionTitle}"/>
                                       </c:if>
                                       <c:if test="${checkboxPosition eq 'right' || checkboxPosition eq 'both'}">
                                           <c:choose>
                                               <c:when test="${selectionType eq 'single'}">
                                                   <display:column headerClass="select_radio" class="select_radio" property="radio" media="html" title="" />
                                               </c:when>
                                               <c:otherwise>
                                                   <fmt:message key="dbuilder.selectAll" var="selectAllLabel" />
                                                   <display:column headerClass="select_checkbox" class="select_checkbox" property="checkbox" media="html" title="<label><input class='form-check-input' type='checkbox' title='${selectAllLabel}' onclick='toggleAll(this)' style='float:left;'/><i></i></label>" />
                                               </c:otherwise>
                                           </c:choose>
                                       </c:if>
                                   </display:table>
                                </div>
                            </c:otherwise>
                        </c:choose>      
                        
                        <!-- Display Buttons -->
                        <c:if test="${buttonPosition eq 'bottomLeft' || buttonPosition eq 'bottomRight' || buttonPosition eq 'bothLeft' || buttonPosition eq 'bothRight'}">
                            <c:if test="${!empty dataList.actions}">
                                <div class="actions bottom ${buttonFloat}">
                                    <c:forEach items="${dataList.actions}" var="action">
                                        <c:if test="${!(empty dataListRows[0] || checkboxPosition eq 'no') || action.visibleOnNoRecord}">
                                            <c:set var="buttonConfirmation" value="" />
                                            <c:if test="${!empty action.confirmation}">
                                                <c:set var="buttonConfirmation" value=" data-confirmation=\"${fn:escapeXml(action.confirmation)}\""/>
                                            </c:if>
                                            <c:set var="buttonCssClasses" value="" />
                                            <c:if test="${!empty action.properties.cssClasses}">
                                                <c:set var="buttonCssClasses" value="${action.properties.cssClasses}"/>
                                            </c:if>
                                            <button data-target="<c:out value="${action.target}" escapeXml="true"/>" data-href="<c:out value="${action.href}" escapeXml="true"/>" data-hrefParam="<c:out value="${action.hrefParam}" escapeXml="true"/>" name="<c:out value="${dataList.actionParamName}" escapeXml="true"/>" class="form-button btn button <c:out value="${buttonCssClasses}" escapeXml="true"/> <c:out value="${action.properties.id}" escapeXml="true"/> ${action.properties.BUILDER_GENERATED_CSS}" ${action.properties.BUILDER_GENERATED_ATTR} value="<c:out value="${action.properties.id}" escapeXml="true"/>" ${buttonConfirmation}>${action.linkLabel}</button>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </c:if>    
                        </c:if>
                        
                        ${dataList.injectedHTML}
                    </form>
                </c:if>    
            </c:catch>

            <c:if test="${!empty dataListException}">
                <h1 id="title">
                    <fmt:message key="general.error.error500"/>
                </h1>
                <div id="error_content" style="font-size:13px">
                    <br><br>
                    <fmt:message key="general.error.error500Description"/>
                    <br><br>
                    <ul style="text-align:left; display:inline-block">
                        <li><fmt:message key="console.footer.label.revision"/></li>
                        <li><fmt:message key="general.error.date"/>: <fmt:formatDate pattern="d MMM yyyy HH:mm:ss" value="<%= new java.util.Date() %>"/></li>
                        <fmt:message key="general.error.errorDetails"/>
                    </ul>
                    <p>&nbsp;</p>
                </div>
        <%
        Throwable t =(Throwable)pageContext.findAttribute("dataListException");  
        LogUtil.error("/jsp/dbuilder/dataListView.jsp", t, "Error rendering datalist");
        %>
            </c:if>
        </c:when>
        <c:when test="${!empty dataList.unauthorizedMsg}">
            <h3>${dataList.unauthorizedMsg}</h3>
        </c:when>            
        <c:otherwise>    
            <h3><fmt:message key="form.form.message.noPermission"/></h3>
        </c:otherwise>    
    </c:choose>
</div>

<script>
    //serialize all inputs into a query string
    DataListUtil = {
        submitForm: function(form) {
            var params = UrlUtil.serializeForm($(form));
            var queryStr = window.location.search;
            params = params.replace(/\+/g, " ");
            var newUrl = UrlUtil.mergeRequestQueryString(queryStr, params);
            window.location.href = "?" + newUrl;
            return false;
        }
    }
    $(document).ready(function() {
        $("form[name='form_${dataListId}']").find("input[type='checkbox']").addClass('form-check-input');
        $("form[name='form_${dataListId}']").on(
            "click",
            "tbody .select_checkbox input[type='checkbox']",
            function () {
                var $form = $(this).closest("form");
                var value = $(this).val();
                var checked = $(this).is(":checked");

                // sync left + right checkbox for the same row (same value)
                $(this).closest("tr")
                    .find(".select_checkbox input[type='checkbox'][value='" + value + "']")
                    .prop("checked", checked);

                // update ALL header checkboxes
                var $rows = $form.find("tbody .select_checkbox input[type='checkbox']");
                var $headers = $form.find("thead .select_checkbox input[type='checkbox']");

                var allChecked =
                    $rows.length > 0 &&
                    $rows.filter(":checked").length === $rows.length;

                $headers.prop("checked", allChecked);
            }
        );       
        //intercept form's submit
        $("#filters_${dataListId}").submit(function(e) {
            e.preventDefault();
            $("#filters_${dataListId}").removeClass("show");
            DataListUtil.submitForm(this);
        });
        $('#filters_${dataListId} > .mobile_search_trigger').off("click").on("click", function(){
            $("#filters_${dataListId}").toggleClass("show");
        });
        $("#dataList_${dataList.id} .exportlinks").prepend("<span class='exportlabel'><fmt:message key='form.form.message.exportMessage'/></span>");
        $("#dataList_${dataList.id} .exportlinks").insertAfter("#dataList_${dataList.id} .pagebanner");
        $(".exportlinks a").attr("target", "_blank"); //download in new page so that it won't block access

        //button 'click' handler
        $("form[name='form_${dataListId}'] button").off("click");
        $("form[name='form_${dataListId}'] button").on("click",  async function(e){
            e.preventDefault();
            var target = $(this).data("target");
            var confirmation = $(this).data("confirmation");
            var href = $(this).data("href");
            var hrefParam = $(this).data("hrefparam");
            var thisObj = $(this);
            var thisForm = $(this).closest("form");
            
            if (target !== undefined && target.toLowerCase() !== "post" && href !== undefined && href !== "" && (hrefParam === undefined || hrefParam === "")) {
                //do Action function
                var doAction = function() {
                    if (target.toLowerCase() === "popup") {
                        if (popupActionDialog == null) {
                            popupActionDialog = new PopupDialog(href);
                        } else {
                            popupActionDialog.src = href;
                        }
                        popupActionDialog.init();
                    } else if (target.toLowerCase() === "_blank") {
                        var win = window.open(href, '_blank');
                        win.focus();
                    } else if (target.toLowerCase() === "_top") {
                        window.top.location = href;
                    } else if (target.toLowerCase() === "_parent") {
                        if (window.parent) {
                            window.parent.location = href;
                        } else {
                            document.location = href;
                        }
                    } else if (target === "" || target.toLowerCase() === "_self") {
                        document.location = href;
                    } else {
                        //iframe
                        var $iframe = $('#' + target);
                        if ( $iframe.length > 0) {
                            $iframe.attr('src', href);   
                        }
                    }
                };
                
                if (confirmation !== undefined && confirmation !== null && confirmation !== "") {
                    if (await showConfirm(this, confirmation)) {
                        doAction();
                    }
                } else {
                    doAction();
                }
                
                return false;
            } else {
                if (target === undefined || target === null || target === "" || target.toLowerCase() === "post") {
                    $("form[name='form_${dataListId}']").removeAttr("target");
                } else if (target.toLowerCase() === "popup") {
                    var url = "${pageContext.request.contextPath}/images/v3/cj.gif";
                    if (popupActionDialog == null) {
                        popupActionDialog = new PopupDialog(url);
                    } else {
                        popupActionDialog.src = url;
                    }
                    $("form[name='form_${dataListId}']").attr("target", "jqueryDialogFrame");
                    var submitForm = true;
                    if (confirmation !== undefined && confirmation !== null && confirmation !== "") {
                        submitForm = await showConfirm(this, confirmation);
                    }
                    if (submitForm) { 
                        popupActionDialog.init();
                        var name = $(this).attr("name");
                        var value = $(this).val();
                        setTimeout(function(){
                            $("form[name='form_${dataListId}']").append('<input name="'+name+'" value="'+value+'" class="temp_button_input"/>');
                            $("form[name='form_${dataListId}']").submit();
                            $("form[name='form_${dataListId}'] .temp_button_input").remove();
                        }, 1);
                    }
                    return false;
                } else {
                    $("form[name='form_${dataListId}']").attr("target", target);
                }
                if (confirmation !== undefined && confirmation !== null && confirmation !== "") {
                    const submitForm = await showConfirm(this, confirmation);
                    if (submitForm) {
                        var name = $(thisObj).attr("name");
                        var value = $(thisObj).val();
                        var hiddenInput = $('<input name="'+name+'" value="'+value+'" class="temp_button_input"/>');
                        thisForm.append(hiddenInput);
                        thisObj.focus(); //to set document.activeElement used in ajax-component
                        thisForm.submit();
                        hiddenInput.remove();
                    }
                    return submitForm;
                } else {
                    return true;
                }
            }
        });
        

        if('${checkboxPosition}' !== 'no'){
            if('${selectionType}' === 'single'){
                $("form[name='form_${dataListId}'] tbody .select_radio input[type='radio']").each(function() {
                    $(this).attr('title', $(this).val());
                });
            }else{
                $("form[name='form_${dataListId}'] tbody .select_checkbox input[type='checkbox']").each(function() {
                    $(this).attr('title', $(this).val());
                });
            }
        }

        if ($("#filters_${dataListId}").hasClass("filter_form")){
            let filtercells = $('#filters_${dataListId} .filter-cell:not(:has(.btn))');
            let allHidden = filtercells.length > 0 && filtercells.filter('.hidden-filter').length === filtercells.length;
            if (allHidden) {
                $("#filters_${dataListId}.filter_form").css({
                    'display': 'none'
                });   
            }
        }
    });
    function toggleAll(element) {
        var $form = $(element).closest("form");
        var checked = $(element).is(":checked");

        $form.find("thead .select_checkbox input[type=checkbox]").prop("checked", checked);
        $form.find("tbody .select_checkbox input[type=checkbox]").prop("checked", checked);
    }

    //rewrite to use the dialogues 
    async function showConfirm(element, message) {
        var table = $(element).closest("form");
        if ($(table).find("input[type=checkbox][name|=d]:checked, input[type=radio][name|=d]:checked").length > 0) {
            //if there is a "checked" option, ask the user
            return UI.asyncConfirm(message);
        } else {
            UI.alert('<ui:msgEscJS key="dbuilder.alert.noRecordSelected"/>');
            return false;
        }
    }
</script>