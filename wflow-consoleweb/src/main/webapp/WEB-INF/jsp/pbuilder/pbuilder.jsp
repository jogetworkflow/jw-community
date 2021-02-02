<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="appDef" scope="request" value="${appDefinition}"/>
<c:set var="builderLabel" scope="request"><fmt:message key="pbuilder.title"/></c:set>
<c:set var="builderI18N" scope="request" value=""/>
<c:set var="builderJS" scope="request">
    <script src="${pageContext.request.contextPath}/pbuilder/js/pbuilder.js?build=<fmt:message key="build.number"/>"></script>
</c:set>
<c:set var="builderCSS" scope="request">
    <style>
        #dragElement-clone{display: none !important;}
        body {--builder-header-top-height : 110px;}
        #process-selector {
            position: fixed; 
            top : 65px; 
            margin-right: 15vw;
            margin-left: 15vw;
            margin-right: var(--builder-right-panel-width);
            margin-left: var(--builder-left-panel-width);
            width: calc( 100vw - (var(--builder-left-panel-width) + var(--builder-right-panel-width) + var(--builder-canvas-margin)));
            background: #fafbfc;
            padding: 5px 10px 5px 23px;
            z-index: 9;
        }
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
        #listViewerView {top: var(--builder-header-top-height); right: var(--builder-right-panel-width); z-index: 8;}
        #listViewerView .nav-tabs .nav-link.active{border-color: #dee2e6 #dee2e6 #fff #dee2e6; border-top-left-radius: .25rem !important; border-top-right-radius: .25rem !important;}
        #listViewerView .nav-tabs .nav-item:first-child .nav-link.active{border-left: 1px solid #dee2e6;}
        #listViewerView .tab-content > div {padding: 15px 0;}
        #listViewerView .cbuilder-node-details-list {margin-bottom: 0; padding: 10px; cursor: pointer; font-size: 13px;}
        #listViewerView .cbuilder-node-details-list.active {background:#0069d9 !important; color:#fff;}
        #listViewerView .cbuilder-node-details-list:hover {background:#f6ffff;}
        #listViewerView .cbuilder-node-details + .cbuilder-node-details {border-top:1px solid #dee2e6;}
        #listViewerView .cbuilder-node-details-list dt {float: left;clear: left; padding-right: 5px; width: 120px;}
        #listViewerView .cbuilder-node-details-list.ac
    </style>    
</c:set>    
<c:set var="builderCode" scope="request" value="process"/>
<c:set var="builderColor" scope="request" value="#dc4438"/>
<c:set var="builderIcon" scope="request" value="fas fa-th-list"/>
<c:set var="builderDefJson" scope="request" value="${json}"/>
<c:set var="builderCanvas" scope="request" value=""/>
<c:set var="builderConfig" scope="request">
    {
        "builder" : {
            "options" : {
                "getDefinitionUrl" : "${pageContext.request.contextPath}/web/console/app/${appId}/${version}/process/builder/json"
            },
            "callbacks" : {
                "initBuilder" : "ProcessBuilder.initBuilder",
                "load" : "ProcessBuilder.load",
                "beforeUpdate" : "ProcessBuilder.updateXpdl",
                "zoomMinus" : "ProcessBuilder.zoomMinus",
                "zoomPlus" : "ProcessBuilder.zoomPlus",
                "builderBeforeSave" : "ProcessBuilder.beforeSaveValidation",
                "listViewerViewInit" : "ProcessBuilder.listViewerViewInit",
                "listViewerViewBeforeClosed" : "ProcessBuilder.listViewerViewBeforeClosed"
            }
        },
        "advanced_tools" : {
            "xray" : {
                "disabled" : false,
            },
            "permission" : {
                "disabled" : true,
            },
            "usage" : {
                "disabled" : true,
            }
        }
    }
</c:set>
<c:set var="builderProps" scope="request" value="" />
<c:set var="saveUrl" scope="request" value="${pageContext.request.contextPath}/web/console/app/${appId}/${version}/process/builder/save"/>
<c:set var="previewUrl" scope="request" value=""/>

<jsp:include page="../cbuilder/base.jsp" flush="true" />