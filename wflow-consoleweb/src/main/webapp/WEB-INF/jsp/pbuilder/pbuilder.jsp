<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="appDef" scope="request" value="${appDefinition}"/>
<c:set var="builderLabel" scope="request"><fmt:message key="pbuilder.title"/></c:set>
<c:set var="builderI18N" scope="request" value=""/>
<c:set var="builderJS" scope="request">
    <script src="${pageContext.request.contextPath}/pbuilder/js/pbuilder.js?build=<fmt:message key="build.number"/>"></script>
</c:set>
<c:set var="builderCSS" scope="request">
    <style>
        body {--builder-content-top : calc(var(--builder-header-top-height) + 40px);}
        #cbuilder[data-browser="ie"] #builder_canvas{top:130px;}
        #dragElement-clone{display: none !important;}
        #process-selector {
            position: fixed; 
            top:85px;
            top : var(--builder-header-top-height); 
            margin-right: 20vw;
            margin-left: 15vw;
            margin-right: var(--builder-right-panel-width);
            margin-left: var(--builder-left-panel-width);
            width: calc(100vw - (13.5vw + 20vw + 30px));
            width: calc( 100vw - (var(--builder-left-panel-width) + var(--builder-right-panel-width) + var(--builder-canvas-margin)));
            background: #fafbfc;
            padding: 5px 10px 5px 23px;
            z-index: 9;
        }
        #cbuilder[data-browser="ie"] #process-selector {left:15px;}
        #process-selector .process_action {display:inline-block; padding: 0px 15px; font-size: 110%; vertical-align: middle;}
        #process-delete-btn {color:#ff4500}
        #process-delete-btn:hover {color:red}
        #process-selector .process_action .graybtn {color: #212529;}
        #process-selector .process_action .graybtn:hover {color: #666;}
        #node_dialog ul {list-style: none; padding: 0; margin: 0; min-height: 0;}
        #node_dialog li {padding: 5px; font-size: 13px; border: 1px solid #ccc; margin-bottom: 3px; border-radius: 3px; cursor: pointer}
        #node_dialog li:hover {background: #0069d9; color: #fff}
        [aria-describedby="node_dialog"] {width: 100px !important;}
        [aria-describedby="node_dialog"] .ui-dialog-titlebar{display:none}
        #screenshotView, #listViewerView {top: var(--builder-content-top); right: var(--builder-right-panel-width); z-index: 8;}
        #listViewerView .nav-tabs .nav-link.active{border-color: #dee2e6 #dee2e6 #fff #dee2e6; border-top-left-radius: .25rem !important; border-top-right-radius: .25rem !important;}
        #listViewerView .nav-tabs .nav-item:first-child .nav-link.active{border-left: 1px solid #dee2e6;}
        #listViewerView .tab-content > div {padding: 15px 0;}
        #listViewerView .cbuilder-node-details-list {margin-bottom: 0; padding: 15px; cursor: pointer; font-size: 13px;}
        #listViewerView .cbuilder-node-details-list.active {background:#0069d9 !important; color:#fff;}
        #listViewerView .cbuilder-node-details-list:hover {background:#f6ffff;}
        #listViewerView .cbuilder-node-details + .cbuilder-node-details {border-top:1px solid #dee2e6;}
        #listViewerView .cbuilder-node-details-list h6 {margin-bottom: 15px;}
        #listViewerView .cbuilder-node-details-list dt {float: left;clear: left; padding-left: 10px; padding-right: 5px; width: 120px; font-weight: 500;}
        #screenshotView .sticky-buttons {top: 125px;}
        #cbuilder.hide-tool:not(.screenshot-builder-view) {--builder-left-panel-width: 0px; --builder-content-top: var(--builder-header-top-height);}
        #xpdlView{top:85px !important;}
    </style>    
</c:set>    
<c:set var="builderCode" scope="request" value="process"/>
<c:set var="builderColor" scope="request" value="#dc4438"/>
<c:set var="builderIcon" scope="request" value="fas fa-project-diagram"/>
<c:set var="builderDefJson" scope="request" value="${json}"/>
<c:set var="builderCanvas" scope="request" value=""/>
<c:set var="builderConfig" scope="request">
    {
        "builder" : {
            "options" : {
                "getDefinitionUrl" : "${pageContext.request.contextPath}/web/console/app/${appId}/${version}/process/builder/json",
                "rightPropertyPanel" : true,
                "defaultBuilder" : true,
                "submitDiff" : true
            },
            "callbacks" : {
                "initBuilder" : "ProcessBuilder.initBuilder",
                "unloadBuilder" : "ProcessBuilder.unloadBuilder",
                "load" : "ProcessBuilder.load",
                "beforeUpdate" : "ProcessBuilder.updateXpdl",
                "zoomMinus" : "ProcessBuilder.zoomMinus",
                "zoomPlus" : "ProcessBuilder.zoomPlus",
                "xpdlViewInit" : "ProcessBuilder.xpdlViewInit",
                "builderBeforeSave" : "ProcessBuilder.beforeSaveValidation",
                "listViewerViewInit" : "ProcessBuilder.listViewerViewInit",
                "listViewerViewBeforeClosed" : "ProcessBuilder.listViewerViewBeforeClosed",
                "saveEditProperties" : "ProcessBuilder.saveEditProperties",
                "builderSaved" : "ProcessBuilder.builderSaved",
                "getOverviewPathElementSelector" : "ProcessBuilder.getOverviewPathElementSelector"
            },
            "properties" : {
                "packageVersion" : "${packageVersion}"
            }
        },
        "advanced_tools" : {
            "xray" : {
                "disabled" : false,
            },
            "screenshot" : {
                "disabled" : false,
            },
            "permission" : {
                "disabled" : true,
            },
            "usage" : {
                "disabled" : true,
            },
            "i18n" : {
                keywords : [
                    "-Name"
                ],
                options : {
                    sort : false,
                    i18nHash : false,
                    loadEnglish : true,
                    key : function(key, obj, options) {
                        return "plabel." + options.processId + "." + obj['-Id'];
                    },
                    label : function(label, obj, options) {
                        return label + " ("+options.processId + ":" + obj['-Id']+")";
                    },
                    skip : function(key, obj, keys, labels, options) {
                        if ($.inArray(key, ["xpdl", "Package", "WorkflowProcesses", "WorkflowProcess", "Activities", "Activity"]) !== -1
                                || (key === "-Name" && obj["Implementation"] !== undefined && obj["Implementation"]["No"] !== undefined)
                                || Array.isArray(obj)) {
                                
                            if (obj["ProcessHeader"] !== undefined) {
                                //add the process label first
                                labels.push({
                                    key : "plabel." + obj['-Id'],
                                    label : obj['-Name'] + " ("+obj['-Id']+")"
                                });
                                keys.push("plabel." + obj['-Id']);
                                options.processId = obj['-Id'];
                            }    
                            return false;
                        }
                        return true;
                    }
                }
            }
        }
    }
</c:set>
<c:set var="builderProps" scope="request" value="" />
<c:set var="saveUrl" scope="request" value="${pageContext.request.contextPath}/web/console/app/${appId}/${version}/process/builder/save"/>
<c:set var="previewUrl" scope="request" value=""/>

<jsp:include page="../cbuilder/base.jsp" flush="true" />
