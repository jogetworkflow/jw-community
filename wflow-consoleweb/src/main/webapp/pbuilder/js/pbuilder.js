ProcessBuilder = {
    currentProcessData : {},
    jsPlumb: null,
    readonly: false,
    refreshTimeout: null,
    
    /*
     * Intialize the builder, called from CustomBuilder.initBuilder
     */
    initBuilder: function (callback) {
        $("#save-btn").parent().after('<div class="btn-group mr-1 float-right" style="margin-top:-16px;" role="group"><button class="btn btn-secondary btn-icon" id="launch-btn" title="'+get_cbuilder_msg("pbuilder.label.runProcess")+'"><i class="las la-play"></i> <span>'+get_cbuilder_msg("pbuilder.label.runProcess")+'</span></button></div>');
        $("#launch-btn").on("click", function(){
            if(!CustomBuilder.isSaved()){
                alert(get_cbuilder_msg("cbuilder.pleaseSaveChangeToContinue"));
            } else {
                var url = CustomBuilder.contextPath + '/web/client/app' + CustomBuilder.appPath + '/process/' + ProcessBuilder.currentProcessData.properties.id;
                JPopup.show("runProcessDialog", url, {}, "");
            }
            return false;
        });
        
        ProcessBuilder.view = getUrlParam('view');
        ProcessBuilder.preSelect = getUrlParam('id');
        
        CustomBuilder.Builder.init({
            "enableViewport" : false,
            callbacks : {
                "initComponent" : "ProcessBuilder.initComponent",
                "renderElement" : "ProcessBuilder.renderElement",
                "updateElementId" : "ProcessBuilder.updateElementId",
                "unloadElement" : "ProcessBuilder.unloadElement",
                "renderXray" : "ProcessBuilder.renderXray"
            }
        }, function() {
            $("#builder_canvas").before('<div id="process-selector"></div>');
            
            $("#style-properties-tab-link").find("i").replaceWith('<i class="las la-handshake"></i>');
            $("#style-properties-tab-link").find("span").text(get_cbuilder_msg('pbuilder.label.mapping'));
            
            $("#design-btn").after('<button class="btn btn-light" title="'+get_cbuilder_msg('pbuilder.label.listView')+'" id="listviewer-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="listViewer" data-cbuilder-action="switchView"><i class="la la-list"></i> <span>'+get_cbuilder_msg('pbuilder.label.listView')+'</span></button>');
            
            $("#json-def-btn").after('<button class="btn btn-light" title="'+get_cbuilder_msg('pbuilder.label.xpdl')+'" id="xpdl-btn" type="button" data-toggle="button" aria-pressed="false" data-cbuilder-view="xpdl" data-cbuilder-action="switchView" data-hide-tool data-view-control><i class="la la-code"></i></button>');
            
            $(".responsive-buttons").after('<div class="btn-group mr-3 light-tools toolbar-group toolzoom-buttons float-right" role="group">\
                <button id="zoom-minus" class="btn btn-light"  title="'+get_cbuilder_msg('pbuilder.label.zoomOut')+' (90%)" data-cbuilder-action="zoomMinus"><i class="las la-search-minus"></i></button>\
                <button id="zoom-plus" class="btn btn-light"  title="'+get_cbuilder_msg('pbuilder.label.zoomIn')+' (110%)" data-cbuilder-action="zoomPlus"><i class="las la-search-plus"></i></button></div>');
            
            ProcessBuilder.initComponents();
            CustomBuilder.Builder.setHead('<link data-pbuilder-style href="' + CustomBuilder.contextPath + '/pbuilder/css/pbuilder.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<script data-jsPlumb-script src="' + CustomBuilder.contextPath + '/pbuilder/js/jquery.jsPlumb-1.6.4-min.js"></script>');

            //wait for jsplumb available
            while (!ProcessBuilder.jsPlumb) {
                ProcessBuilder.jsPlumb = CustomBuilder.Builder.iframe.contentWindow.jsPlumb;
            }
            
            // init jsPlumb
            ProcessBuilder.jsPlumb.importDefaults({
                Container: "canvas",
                Anchor: "Continuous",
                Endpoint: ["Dot", {radius: 4}],
                Connector: ["StateMachine", {curviness:0.1}],
                PaintStyle: {strokeStyle: "#999", lineWidth: 1, outlineWidth: 15, outlineColor: 'transparent'},
                HoverPaintStyle: {lineWidth: 4},
                ConnectionOverlays: [
                    ["Arrow", {
                        location: 0.99,
                        id: "arrow",
                        length: 10,
                        width: 10,
                        foldback: 0.8
                    }]
                ],
                ConnectionsDetachable: true
            });
            
            CustomBuilder.Builder.bindEvent("change.builder", function(){
                ProcessBuilder.refresh();
            });
            CustomBuilder.Builder.bindEvent("nodeAdditionalSelected nodeAdditionalAdded nodeAdditionalRemoved nodeAdditionalModeChanged", ProcessBuilder.refreshConnections);
            
            $(window).off('hashchange');
            $(window).on('hashchange', function(){
                var id = window.location.hash.replace("#", "");
                
                //when no current process data or current process data is not match with the id in URL hash
                if (ProcessBuilder.currentProcessData === null || ProcessBuilder.currentProcessData === undefined || (ProcessBuilder.currentProcessData.properties === undefined || (ProcessBuilder.currentProcessData.properties !== undefined && id !== ProcessBuilder.currentProcessData.properties.id))) {
                    ProcessBuilder.viewProcess();
                }
            });

            var deferreds = [];
            
            var wait = $.Deferred();
            deferreds.push(wait);
            
            var jsPlumbReady = $.Deferred();
            deferreds.push(jsPlumbReady);
            ProcessBuilder.jsPlumb.ready(function() {
                //make some delay for css to load
                setTimeout(function(){
                    jsPlumbReady.resolve();
                }, 20);
                
            });
            
            ProcessBuilder.getMultiToolsProps(deferreds);
            ProcessBuilder.getAssignmentFormModifier(deferreds);
            ProcessBuilder.getStartProcessFormModifier(deferreds);
            ProcessBuilder.getTools(deferreds);
            ProcessBuilder.getDecisionPlugin(deferreds);
            ProcessBuilder.getForms(deferreds);
            ProcessBuilder.getParticipants(deferreds);
            
            wait.resolve();
            
            $.when.apply($, deferreds).then(function() {
                if (callback) {
                    callback();
                }
            });
        });
    },
    
    /*
     * bind event for the js plumb library
     */
    initJsPlumb : function() {
        // single click on any endpoint
        ProcessBuilder.jsPlumb.unbind("endpointClick");
        ProcessBuilder.jsPlumb.bind("endpointClick", function(endpoint, originalEvent) {
        });
        //check for invalid connection
        ProcessBuilder.jsPlumb.unbind("beforeDrop");
        ProcessBuilder.jsPlumb.bind("beforeDrop", function(info) {
            var connection = info.connection;
            if ($(connection.source).hasClass("start")) {
                //disallow duplicate transitions from start node
                var connSet = ProcessBuilder.jsPlumb.getConnections({source: $(connection.source)});
                if (connSet.length > 0) {
                    return false;
                }
            } else if ($(connection.target).hasClass("end")) {
                //disallow duplicate transitions to end node
                var connSet = ProcessBuilder.jsPlumb.getConnections({target: $(connection.target)});
                if (connSet.length > 0) {
                    return false;
                }
            } else if ($(connection.source).hasClass("start") && $(connection.target).hasClass("end")) {
                //disallow from start to end
                return false;
            }
            return true;
        });
    },
    
    /*
     * Load and render data, called from CustomBuilder.loadJson
     */
    load: function (data) {
        ProcessBuilder.updateProcessSelector();
        ProcessBuilder.viewProcess();
    },
    
    /*
     * Create and update process selector
     */
    updateProcessSelector : function() {
        var selector = $('#process-selector select');
        if (selector.length === 0) {
            $('#process-selector').append('<select id="processes_list"></select> <div class="process_action"></div>');
            selector = $('#process-selector select');
            $(selector).chosen({ width: "250px", placeholder_text: " " });
            
            $('#process-selector .process_action').append(' <a id="process-edit-btn" href="" title="'+get_cbuilder_msg("ubuilder.edit")+'" style=""><i class="la la-pen"></i></a>');
            $('#process-selector .process_action').append(' <a id="process-delete-btn" title="'+get_cbuilder_msg("cbuilder.remove")+'" style=""><i class="la la-trash"></i></a>');
            $('#process-selector .process_action').append(' &nbsp;<a class="graybtn" id="process-additional-btn" title="'+get_cbuilder_msg("pbuilder.additionalInformation")+'" style="" onclick="ProcessBuilder.showAdvancedInfo();return false"><i class="zmdi zmdi-info-outline"></i></a>');
            $('#process-selector .process_action').append('&nbsp;&nbsp;&nbsp;<a class="graybtn" id="process-clone-btn" title="'+get_cbuilder_msg("cbuilder.clone")+'" style=""><i class="la la-copy"></i></a>');
            $('#process-selector .process_action').append(' <a class="graybtn" id="process-add-btn" title="'+get_cbuilder_msg("cbuilder.addnew")+'" style=""><i class="la la-plus"></i></a>');
            
            $("#process-edit-btn").on("click", function(event){
                ProcessBuilder.editProcess();
                event.preventDefault();
                return false;
            });
            $("#process-delete-btn").on("click", function(event){
                ProcessBuilder.deleteProcess();
                event.preventDefault();
                return false;
            });
            $("#process-clone-btn").on("click", function(event){
                ProcessBuilder.cloneProcess();
                event.preventDefault();
                return false;
            });
            $("#process-add-btn").on("click", function(event){
                ProcessBuilder.addProcess();
                event.preventDefault();
                return false;
            });
            
            var updateLabel = function(chosen) {
                $(chosen.container).find(".chosen-results li, .chosen-single > span, .search-choice > span").each(function() {
                    var index = $(this).attr("data-option-array-index");
                    var isError = false;
                    if (index === undefined) {
                        isError = $(selector).find("option[value='"+$(selector).val()+"']").hasClass("invalidProcess");
                    } else {
                        isError = $(selector).find("option:eq("+index+")").hasClass("invalidProcess");
                    }
                    
                    if (isError) {
                        $(this).html('<span style="color:red">'+$(this).html()+'</span>');
                    }
                });
            }
            $(selector).on("chosen:showing_dropdown chosen:hiding_dropdown chosen:ready chosen:updated change", function(evt) {
                updateLabel($(selector).data("chosen"));
            });
            setTimeout(function() {
                $($(selector).data("chosen").container).find(".chosen-search input").on("keydown", function() {
                    setTimeout(function() { updateLabel($(selector).data("chosen")); }, 5);
                });
            }, 1000);
        }
        $(selector).html('');
        
        var xpdl = CustomBuilder.data.xpdl['Package'];
        var xpdlProcesses = ProcessBuilder.getArray(xpdl['WorkflowProcesses']['WorkflowProcess']);
        
        for (var p in xpdlProcesses) {
            $(selector).append('<option value="'+xpdlProcesses[p]['-Id']+'">'+xpdlProcesses[p]['-Name']+'</option>');
        }
        $(selector).trigger("chosen:updated");
        $(selector).off("change");
        $(selector).on("change", function(){
            window.location.hash = $(selector).val();
        });
    },
    
    /*
     * To handle the xpdl data to always return in array even there is only single value
     */
    getArray : function(data, key) {
        if (data === undefined || (key !== undefined && key !== null && key !== "" && data[key] === undefined)) {
            return [];
        }
        if (key !== undefined && key !== null && key !== "") {
            data = data[key];
        }
        if (!$.isArray(data)) {
            return [data];
        }
        return data;
    },
    
    /*
     * To handle the xpdl data to always return in array even there is only single value
     */
    setArray : function(obj, key, arrayKey, values) {
        if (obj === undefined) {
            return;
        }
        var dataObj;
        if (key !== undefined && key !== null && key !== "") {
            dataObj = obj[key];
            if (dataObj === undefined) {
                dataObj = {};
                obj[key] = dataObj;
            }
        } else {
            dataObj = obj;
        }
        if (values.length > 1) {
            dataObj[arrayKey] = values;
        } else if (values.length === 1) {
            dataObj[arrayKey] = values[0];
        } else if (values.length === 0 && (key !== undefined && key !== null && key !== "")) {
            delete obj[key];
        }
    },
    
    /*
     * Based on selection and url hash, construct the process data from spdl data and view the process in canvas 
     */
    viewProcess : function() {
        var self = CustomBuilder.Builder;
        var id = window.location.hash.replace("#", "");
        
        //if id not empty or id is empty but there is remaining process in package. 
        //The generateProcessData method will redirect to the first remaining process when id is empty
        if (id !== "" || $("#processes_list option").length > 0) {
            ProcessBuilder.generateProcessData(id);
            if (ProcessBuilder.currentProcessData !== undefined && ProcessBuilder.currentProcessData !== null && ProcessBuilder.currentProcessData.properties !== undefined) {
                CustomBuilder.Builder.load(ProcessBuilder.currentProcessData, function(){
                    ProcessBuilder.validate();

                    setTimeout(function(){
                        if (ProcessBuilder.preSelect !== "") {
                            var node = self.frameBody.find("[data-cbuilder-id='"+ProcessBuilder.preSelect+"']");
                            self.selectNode(node);
                            ProcessBuilder.preSelect = "";
                        }
                        if (ProcessBuilder.view !== "") {
                            $("[data-cbuilder-view='"+ProcessBuilder.view+"']").trigger("click");
                        }
                    }, 1000);
                    
                    ProcessBuilder.updateAdvancedView();
                });
            }
        } else { //only redirect to `process1` when there is totally no process in package
            window.location.hash = "process1";
        }
    },
    
    /*
     * Update advanced view
     */
    updateAdvancedView: function () {
        if ($('#advancedView').length > 0) {
            $('#advancedView').remove();
        }

        var packageId = CustomBuilder.data.xpdl['Package']['-Id'];
        var processDefId = CustomBuilder.appId + ":" + CustomBuilder.config.builder.properties["packageVersion"] + ":" + ProcessBuilder.currentProcessData.properties.id;
        var runProcessLink = document.URL.substring(0, document.URL.indexOf("/web/console"));
        runProcessLink += "/web/client/app" + CustomBuilder.appPath + "/process/" + ProcessBuilder.currentProcessData.properties.id + "?start=true";

        $('#process-selector').append('<div id="advancedView" style="display:none;"><dl>'
            + '<dt>' + get_cbuilder_msg("pbuilder.packageID") + '</dt>'
            + '<dd><a class="copybtn" id="process-package-id-btn" title="' + get_cbuilder_msg("pbuilder.copyPackageId") + '" style="">' + packageId + '&nbsp;<i class="far fa-copy"></i></a></dd>'
            + '<dt>' + get_cbuilder_msg("pbuilder.processDefID") + '</dt>'
            + '<dd><a class="copybtn" id="process-copy-def-btn" title="' + get_cbuilder_msg("pbuilder.copyProcessDef") + '" style="">' + processDefId + '&nbsp;<i class="far fa-copy"></i></a></dd>'
            + '<dt>' + get_cbuilder_msg("pbuilder.linkToRunProcess") + '</dt>'
            + '<dd><a class="copybtn" id="process-copy-link-btn" title="' + get_cbuilder_msg("pbuilder.copyProcessStartLink") + '" style="">' + runProcessLink + '&nbsp;<i class="far fa-copy"></i></a></dd></dl>'
            + '<div id="advacendButton" class="form-buttons" style="display: block">'
            + '<a href="#" style="display: none" id="hideAdvancedInfo" onclick="ProcessBuilder.hideAdvancedInfo();return false">' + get_cbuilder_msg("pbuilder.hideAdditionalInfo") + '</a>'
            + '</div></div>');
        
        $('#process-package-id-btn').off('click');
        $("#process-package-id-btn").on("click", function (event) {
            CustomBuilder.copyTextToClipboard(packageId, true);
            CustomBuilder.showMessage(get_cbuilder_msg('pbuilder.copyPackageId.copied'), "info", true);

            event.preventDefault();
            return false;
        });
        
        $('#process-copy-link-btn').off('click');
        $("#process-copy-link-btn").on("click", function (event) {
            CustomBuilder.copyTextToClipboard(runProcessLink, true);
            CustomBuilder.showMessage(get_cbuilder_msg('pbuilder.copyProcessStartLink.copied'), "info", true);

            event.preventDefault();
            return false;
        });
        
        $('#process-copy-def-btn').off('click');
        $("#process-copy-def-btn").on("click", function (event) {
            CustomBuilder.copyTextToClipboard(processDefId, true);
            CustomBuilder.showMessage(get_cbuilder_msg('pbuilder.copyProcessDef.copied', [processDefId]), "info", true);

            event.preventDefault();
            return false;
        });
    },

    /*
     * Generate process model from XPDL
     */
    generateProcessData : function(id) {
        var xpdlProcess = null;
        ProcessBuilder.currentProcessData = null;
        var xpdl = CustomBuilder.data.xpdl['Package'];
        var xpdlProcesses = ProcessBuilder.getArray(xpdl['WorkflowProcesses'], 'WorkflowProcess');
        
        //check if the data is new process and set the process start whitelist
        ProcessBuilder.setNewProcessStartWhitelist(xpdlProcesses);
        
        for (var p in xpdlProcesses) {
            if (xpdlProcesses[p]["-Id"] === id) {
                xpdlProcess = xpdlProcesses[p];
                break;
            }
        }
        
        if (!ProcessBuilder.readonly) {
            if (xpdlProcess === null) {
                xpdlProcess = xpdlProcesses[0];
                id = xpdlProcess['-Id'];
                window.location.hash = id;
                return;
            }

            $('#process-selector select').val(id);
            $('#process-selector select').trigger("chosen:updated");
        }
        
        var process = {
            className : 'process',
            properties : {
                id : id,
                label : xpdlProcess['-Name']
            },
            participants : [],
            transitions : [],
            xpdlObj : xpdlProcess
        };
        ProcessBuilder.currentProcessData = process;
        
        //adding workflow variable
        if (xpdlProcess['DataFields'] !== undefined) {
            var dataFields = new Array();
            
            var xpdlDataFields = ProcessBuilder.getArray(xpdlProcess['DataFields'], 'DataField');
            for (var d in xpdlDataFields) {
                dataFields.push({
                    variableId : xpdlDataFields[d]['-Id']
                });
            }
            process.properties.dataFields = dataFields;
        }
        
        //adding subflow properties
        if (xpdlProcess['FormalParameters'] !== undefined) {
            var formalParameters = new Array();
            
            var xpdlFormalParameters = ProcessBuilder.getArray(xpdlProcess['FormalParameters'], 'FormalParameter');
            for (var p in xpdlFormalParameters) {
                formalParameters.push({
                    parameterId : xpdlFormalParameters[p]['-Id'],
                    mode : xpdlFormalParameters[p]['-Mode']
                });
            }
            process.properties.formalParameters = formalParameters;
        }
        
        //adding sla options
        if (xpdlProcess['ProcessHeader'] !== undefined && xpdlProcess['ProcessHeader']['-DurationUnit'] !== undefined) {
            process.properties.durationUnit =  xpdlProcess['ProcessHeader']['-DurationUnit'];
            if (xpdlProcess['ProcessHeader']['Limit'] !== undefined) {
                process.properties.limit = xpdlProcess['ProcessHeader']['Limit'];
            }
        }
        
        //add participant
        var xpdlParticipants = ProcessBuilder.getArray(xpdl['Participants'], 'Participant');
        var participants = {};
        for (var p in xpdlParticipants) {
            participants[xpdlParticipants[p]['-Id']] = {
                className : 'participant',
                properties : {
                    id : xpdlParticipants[p]['-Id'],
                    label : xpdlParticipants[p]['-Name']
                },
                activities : [],
                xpdlObj : xpdlParticipants[p]
            };
        }
        
        var xpdlProcessesAttrs = ProcessBuilder.getArray(xpdlProcess['ExtendedAttributes'], 'ExtendedAttribute');
        for (var p = 0; p < xpdlProcessesAttrs.length; p++) {
            if (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER") {
                var orders = xpdlProcessesAttrs[p]['-Value'].split(";");
                for (var o in orders) {
                    if (participants[orders[o]] === undefined) { //used to correct the corrupted process design
                        var label = orders[o];
                        if (label.indexOf(id) === 0) {
                            label = label.substring(id.length + 1);
                        }
                        label = label.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase()).replace(/(\d+)$/, ' $1');
                        participants[orders[o]] = {
                            className : 'participant',
                            properties : {
                                id : orders[o],
                                label : label
                            },
                            activities : []
                        };
                    }
                    
                    ProcessBuilder.currentProcessData['participants'].push(participants[orders[o]]);
                        
                    if (!ProcessBuilder.readonly) {
                        //find mapping
                        ProcessBuilder.populateParticipantMapping(participants[orders[o]]);
                    }
                }
                break;
            }
        }
        
        //populate activities
        var xpdlActivities = ProcessBuilder.getArray(xpdlProcess['Activities'], 'Activity');
        for (var a in xpdlActivities) {
            var act = xpdlActivities[a];
            var type = "activity";
            if (act['Route'] !== undefined) {
                type = "route";
            } else if (act['Implementation'] !== undefined && act['Implementation']['Tool'] !== undefined) {
                type = "tool";
            } else if (act['Implementation'] !== undefined && act['Implementation']['SubFlow'] !== undefined) {
                type = "subflow";
            }
            
            var participantId = "";
            var x = 0;
            var y = 0;
            
            var attrs = ProcessBuilder.getArray(act['ExtendedAttributes'], 'ExtendedAttribute');
            for (var at in attrs) {
                if (attrs[at]['-Name'] === "JaWE_GRAPH_PARTICIPANT_ID") {
                    participantId = attrs[at]['-Value'];
                } else if (attrs[at]['-Name'] === "JaWE_GRAPH_OFFSET") {
                    var values = attrs[at]['-Value'].split(",");
                    x = values[0];
                    y = values[1];
                }
            }
            
            //performer is not a required value for route, set it again when it is missing
            if (xpdlActivities[a]["Performer"] === undefined) {
                xpdlActivities[a]["Performer"] = participantId;
            }
            
            var obj = {
                className : type,
                properties : {
                    id : act['-Id']
                },
                x_offset : x,
                y_offset : y,
                xpdlObj : xpdlActivities[a]      
            };
            
            if (act['-Name'] !== undefined) {
                obj.properties.label = act['-Name'];
            }
            
            //set join & split
            var join = "", split = "";
            if (act['TransitionRestrictions'] !== undefined && act['TransitionRestrictions']['TransitionRestriction'] !== undefined) {
                var temp = act['TransitionRestrictions']['TransitionRestriction'];
                
                if (temp['Join'] !== undefined) {
                    join = temp['Join']['-Type'];
                }
                if (temp['Split'] !== undefined) {
                    split = temp['Split']['-Type'];
                }
            }
            obj.properties.join = join;
            obj.properties.split = split;
            
            //set limit
            if (act['Limit'] !== undefined) {
                obj.properties.limit = act['Limit'];
            }
            
            //set deadline
            var deadlines = new Array();
            var xpdlDeadlines = ProcessBuilder.getArray(act['Deadline']);
            for (var d in xpdlDeadlines) {
                var durationUnit;
                var deadlineLimit;
                
                var deadlineCondition = xpdlDeadlines[d]['DeadlineCondition'];
                if (deadlineCondition) {
                    if (deadlineCondition.indexOf("dd/MM/yyyy HH:mm") >= 0) {
                        durationUnit = "t";
                        var matches = deadlineCondition.match("parse\(.+\)");
                        if (matches.length > 1) {
                            deadlineLimit = matches[1].substring(1, matches[1].length-2);
                        }
                    } else if (deadlineCondition.indexOf("dd/MM/yyyy") >= 0) {
                        durationUnit = "d";
                        var matches = deadlineCondition.match("parse\(.+\)");
                        if (matches.length > 1) {
                            deadlineLimit = matches[1].substring(1, matches[1].length-2);
                        }
                    } else if (deadlineCondition.indexOf("yyyy-MM-dd HH:mm") >= 0) {
                        durationUnit = "2";
                        var matches = deadlineCondition.match("parse\(.+\)");
                        if (matches.length > 1) {
                            deadlineLimit = matches[1].substring(1, matches[1].length-2);
                        }
                    } else if (deadlineCondition.indexOf("yyyy-MM-dd") >= 0) {
                        durationUnit = "1";
                        var matches = deadlineCondition.match("parse\(.+\)");
                        if (matches.length > 1) {
                            deadlineLimit = matches[1].substring(1, matches[1].length-2);
                        }
                    } else {
                        var limitMatch = deadlineCondition.match("\\+\\(.+\\*");
                        if (limitMatch && limitMatch.length > 0) {
                            deadlineLimit = limitMatch[0].substring(2, limitMatch[0].length-1);
                        }
                        var unitMatch = deadlineCondition.match("\\*\\d+\\)");
                        if (unitMatch && unitMatch.length > 0) {
                            var millis = unitMatch[0].substring(1, unitMatch[0].length-1);
                            if (millis === "1000") {
                                durationUnit = "s";
                            } else if (millis === "60000") {
                                durationUnit = "m";
                            } else if (millis === "3600000") {
                                durationUnit = "h";
                            } else {
                                durationUnit = "D";
                            }
                        }
                    }
                }
                
                deadlines.push({
                    execution : xpdlDeadlines[d]['-Execution'],
                    exceptionName : xpdlDeadlines[d]['ExceptionName'],
                    durationUnit : durationUnit,
                    deadlineLimit : deadlineLimit
                });
                obj.properties.deadlines = deadlines;
            }
            
            //set subflow properties
            if (type === "subflow") {
                var subflow = act['Implementation']['SubFlow'];
                obj.properties.subflowId = subflow['-Id'];
                obj.properties.execution = subflow['-Execution'];
                
                if (subflow['ActualParameters'] !== undefined) {
                    var actualParameters = new Array();
                    var params = ProcessBuilder.getArray(subflow['ActualParameters'], 'ActualParameter');
                    for (var p in params) {
                        actualParameters.push({
                            actualParameter : params[p]
                        });
                    }
                    obj.properties.actualParameters = actualParameters;
                }
            }
            
            if (!ProcessBuilder.readonly) {
                //find mapping
                ProcessBuilder.populateActivityMapping(obj);
            }
            
            //if participant not exist add to the first participant
            if (participants[participantId] !== undefined) {
                participants[participantId]['activities'].push(obj);
            } else {
                participants[Object.keys(participants)[0]]['activities'].push(obj);
            }
        }
        
        //add start and end node
        for (var p in xpdlProcessesAttrs) {
            if (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_END_OF_WORKFLOW" || xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_START_OF_WORKFLOW") {
                var values = xpdlProcessesAttrs[p]['-Value'].split(",");
                var obj = {
                    className : (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_END_OF_WORKFLOW")?"end":"start",
                    properties : {},
                    xpdlObj : xpdlProcessesAttrs[p]
                };
                
                obj.properties.id = obj.className;
                
                for (var v in values) {
                    var attr = values[v].split("=");
                    if (attr[0] === "JaWE_GRAPH_PARTICIPANT_ID") {
                        participants[attr[1]]['activities'].push(obj);
                    } else if (attr[0] === "CONNECTING_ACTIVITY_ID") {
                        if (attr[1] !== "") {
                            obj.properties.id = obj.className + "_" + attr[1];

                            var transition = {
                                className :'transition',
                                properties : {
                                    id : "transition_" + obj.properties.id,
                                    type : 'startend'
                                }
                            };
                            if (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_END_OF_WORKFLOW") {
                                transition.properties.from = attr[1];
                                transition.properties.to = obj.properties.id;
                            } else {
                                transition.properties.from = obj.properties.id;
                                transition.properties.to = attr[1];
                            }

                            process['transitions'].push(transition);
                        }
                    } else if (attr[0] === "X_OFFSET") {
                        obj.x_offset = attr[1];
                    } else if (attr[0] === "Y_OFFSET") {
                        obj.y_offset = attr[1];
                    }
                }
                
                if (!ProcessBuilder.readonly) {
                    //find mapping
                    ProcessBuilder.populateActivityMapping(obj);
                    if (obj.className === "start") {
                        ProcessBuilder.populateParticipantMapping(obj);
                    }
                }
            }
        }
        
        //populate transitions
        var xpdlTransitions = ProcessBuilder.getArray(xpdlProcess['Transitions'], 'Transition');
        for (var t in xpdlTransitions) {
            var transition = {
                className :'transition',
                properties : {
                    id : xpdlTransitions[t]['-Id'],
                    label : (xpdlTransitions[t]['-Name'] !== undefined)?xpdlTransitions[t]['-Name']:"",
                    from : xpdlTransitions[t]['-From'],
                    to : xpdlTransitions[t]['-To']
                },
                xpdlObj : xpdlTransitions[t]
            };
            
            //type
            var type = "";
            var condition = "";
            var exceptionName = "";
            if (xpdlTransitions[t]['Condition'] !== undefined) {
                type = xpdlTransitions[t]['Condition']['-Type'];
                if (type === "CONDITION") {
                    condition = xpdlTransitions[t]['Condition']['#text'];
                } else if (type === "EXCEPTION") {
                    exceptionName = xpdlTransitions[t]['Condition']['#text'];
                }
            }
            transition.properties.type = type;
            transition.properties.condition = condition;
            transition.properties.exceptionName = exceptionName;
            
            var style = "straight";
            var transitionConditions = "";
            var extendedAttributes = ProcessBuilder.getArray(xpdlTransitions[t]['ExtendedAttributes'], 'ExtendedAttribute');
            for (var i in extendedAttributes) {
                if (extendedAttributes[i]['-Name'] === "JaWE_GRAPH_BREAK_POINTS") {
                    style = "orthogonal";
                } else if (extendedAttributes[i]['-Name'] === "PBUILDER_TRANSITION_CONDITIONS") {
                    transitionConditions = extendedAttributes[i]['-Value'];
                }
            }
            transition.properties.style = style;
            if (transitionConditions !== "") {
                transition.properties.conditions = JSON.decode(transitionConditions);
                transition.properties.conditionHelper = "yes";
            }

            process['transitions'].push(transition);
        }
    },
    
    /*
     * Check whether the process is a new process without any activities and set the process start whitelits to admin
     */
    setNewProcessStartWhitelist : function(xpdlProcesses) {
        if (xpdlProcesses.length === 1) { //check the package only have 1 process
            var xpdlActivities = ProcessBuilder.getArray(xpdlProcesses[0]['Activities'], 'Activity');
            
            if (xpdlActivities.length === 0) { //check the process only have 0 ativity
                //get process id
                var id = xpdlProcesses[0]["-Id"];
                
                if (CustomBuilder.data.participants === undefined || CustomBuilder.data.participants === null) {
                    CustomBuilder.data.participants = {};
                }
                    
                if (CustomBuilder.data.participants[id + "::processStartWhiteList"] === undefined) { //if there is no process start whitelist
                    //add process start whitelist to admin
                    CustomBuilder.data.participants[id + "::processStartWhiteList"] = {
                        "type": "role",
                        "value": "adminUser",
                        "properties": {}
                    };
                    
                    CustomBuilder.update(false);
                }
            }
        }
    },
    
    /*
     * Convert the process data back to xpdl in JSON definition
     */
    updateXpdl : function() {
        
        //make sure package id is still same
        CustomBuilder.data.xpdl['Package']['-Id'] = CustomBuilder.appId;
        
        var data = ProcessBuilder.currentProcessData;
        var xpdl = CustomBuilder.data.xpdl['Package'];
        
        if (data !== undefined && data !== null) {
            var xpdlProcess = data.xpdlObj;
            
            var xpdlActivities = ProcessBuilder.getArray(xpdlProcess['Activities'], 'Activity');
            var xpdlProcessesAttrs = ProcessBuilder.getArray(xpdlProcess['ExtendedAttributes'], 'ExtendedAttribute');
            
            xpdlProcess['-Id'] = data.properties.id;
            xpdlProcess['-Name'] = data.properties.label;

            //update duration unit
            if (data.properties.durationUnit !== undefined && data.properties.durationUnit !== "") {
                xpdlProcess['ProcessHeader']['-DurationUnit'] = data.properties.durationUnit
            }

            //update limit
            if (data.properties.limit !== undefined && data.properties.limit !== "") {
                xpdlProcess['ProcessHeader']['Limit'] = data.properties.limit;
                delete xpdlProcess['ProcessHeader']['-self-closing'];
            } else {
                delete xpdlProcess['ProcessHeader']['Limit'];
                xpdlProcess['ProcessHeader']['-self-closing'] = "true";
            }

            //update formal parameters
            if (data.properties.formalParameters !== undefined && data.properties.formalParameters.length > 0) {
                var formalParameters = [];
                for (var f in data.properties.formalParameters) {
                    formalParameters.push({
                        "-Id": data.properties.formalParameters[f].parameterId,
                        "DataType": {
                            "BasicType": {
                                "-Type": "STRING",
                                "-self-closing": "true"
                            }
                        },
                        "-Mode": data.properties.formalParameters[f].mode
                    });
                }
                ProcessBuilder.setArray(xpdlProcess, 'FormalParameters', 'FormalParameter', formalParameters);
            } else {
                delete xpdlProcess['FormalParameters'];
            }
        
            //update workflow variables
            if (data.properties.dataFields !== undefined && data.properties.dataFields.length > 0) {
                var dataFields = [];
                for (var f in data.properties.dataFields) {
                    dataFields.push({
                        "-IsArray": "FALSE",
                        "-Id": data.properties.dataFields[f].variableId,
                        "DataType": {
                            "BasicType": {
                                "-Type": "STRING",
                                "-self-closing": "true"
                            }
                        }
                    });
                }
                ProcessBuilder.setArray(xpdlProcess, 'DataFields', 'DataField', dataFields);
            } else {
                delete xpdlProcess['DataFields'];
            }
        
            //update participants
            var order = "";
            var xpdlParticipants = ProcessBuilder.getArray(xpdl['Participants'], 'Participant');
            for (var p in data.participants) {
                var participant = data.participants[p];
                order += (order !== ""?";":"") + participant.properties.id;
                if (participant.xpdlObj !== undefined) {
                    participant.xpdlObj['-Id'] = participant.properties.id;
                    participant.xpdlObj['-Name'] = participant.properties.label;
                } else {
                    participant.xpdlObj = {
                        "-Name": participant.properties.label,
                        "-Id": participant.properties.id,
                        "ParticipantType": {
                            "-Type": "ROLE",
                            "-self-closing": "true"
                        }
                    }
                    xpdlParticipants.push(participant.xpdlObj);
                }

                //update activities
                ProcessBuilder.updateXpdlActivities(xpdlActivities, xpdlProcessesAttrs, participant);
                
                ProcessBuilder.updateParticipantMapping(participant);
            }
            ProcessBuilder.setArray(xpdlProcess, 'Activities', 'Activity', xpdlActivities);
            ProcessBuilder.setArray(xpdl, 'Participants', 'Participant', xpdlParticipants);

            //update participant order
            for (var p = 0; p < xpdlProcessesAttrs.length; p++) {
                if (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER") {
                    xpdlProcessesAttrs[p]['-Value'] = order;
                    break;
                }
            }
            ProcessBuilder.setArray(xpdlProcess, 'ExtendedAttributes', 'ExtendedAttribute', xpdlProcessesAttrs);
            
            //update transitions
            var xpdlTransitions = ProcessBuilder.getArray(xpdlProcess['Transitions'], 'Transition');
            for (var t in data.transitions) {
                var transition = data.transitions[t];
                if (transition.properties.type === "startend") {
                    continue;
                }

                var transitionXpdlObj = transition.xpdlObj;
                if (transitionXpdlObj === undefined) {
                    transitionXpdlObj = {};
                    xpdlTransitions.push(transitionXpdlObj);
                    
                    transition.xpdlObj = transitionXpdlObj;
                }

                transitionXpdlObj['-Id'] = transition.properties.id;
                transitionXpdlObj['-To'] = transition.properties.to;
                transitionXpdlObj['-From'] = transition.properties.from;

                if (transition.properties.label !== undefined && transition.properties.label !== "") {
                    transitionXpdlObj['-Name'] = transition.properties.label;
                } else {
                    delete transitionXpdlObj['-Name'];
                }

                var extendedAttribute = [];

                extendedAttribute.push({
                    "-Name": "JaWE_GRAPH_TRANSITION_STYLE",
                    "-Value": "NO_ROUTING_ORTHOGONAL",
                    "-self-closing": "true"
                });

                if (transition.properties.style === 'orthogonal') {
                    extendedAttribute.push({
                        "-Name": "JaWE_GRAPH_BREAK_POINTS",
                        "-Value": "orthogonal",
                        "-self-closing": "true"
                    });
                }

                if (transition.properties.type === 'CONDITION') {
                    transitionXpdlObj['Condition'] = {
                        "#text": transition.properties.condition,
                        "-Type": "CONDITION"
                    };
                } else if(transition.properties.type === 'OTHERWISE') {
                    transitionXpdlObj['Condition'] = {
                        "-Type": "OTHERWISE",
                        "-self-closing": "true"
                    };
                } else if(transition.properties.type === 'EXCEPTION') {
                    transitionXpdlObj['Condition'] = {
                        "#text": transition.properties.exceptionName,
                        "-Type": "EXCEPTION"
                    };
                } else if(transition.properties.type === 'DEFAULTEXCEPTION') {
                    transitionXpdlObj['Condition'] = {
                        "-Type": "DEFAULTEXCEPTION",
                        "-self-closing": "true"
                    };
                } else {
                    delete transitionXpdlObj['Condition'];
                }

                if (transition.properties.type === 'CONDITION' && transition.properties.conditions !== undefined && transition.properties.conditions.length > 0) {
                    var conditionsJson = JSON.encode(transition.properties.conditions);
                    extendedAttribute.push({
                        "-Name": "PBUILDER_TRANSITION_CONDITIONS",
                        "-Value": conditionsJson,
                        "-self-closing": "true"
                    });
                }
                ProcessBuilder.setArray(transitionXpdlObj, 'ExtendedAttributes', 'ExtendedAttribute', extendedAttribute); 
            }
            ProcessBuilder.setArray(xpdlProcess, 'Transitions', 'Transition', xpdlTransitions);
        }
        
        //remove participants not used by any processes
        var participantKeys = {};
        var xpdlProcesses = ProcessBuilder.getArray(xpdl['WorkflowProcesses'], 'WorkflowProcess');
        for (var p in xpdlProcesses) {
            var xpdlProcessesAttrs = ProcessBuilder.getArray(xpdlProcesses[p]['ExtendedAttributes'], 'ExtendedAttribute');
            for (var a = 0; a < xpdlProcessesAttrs.length; p++) {
                if (xpdlProcessesAttrs[a]['-Name'] === "JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER") {
                    var ids = xpdlProcessesAttrs[a]['-Value'].split(";");
                    for (var i in ids) {
                        participantKeys[ids[i]] = "";
                    }
                    break;
                }
            }
        }
        
        var xpdlParticipants = ProcessBuilder.getArray(xpdl['Participants'], 'Participant');
        for (var i = xpdlParticipants.length - 1; i >= 0; i--) {
            if (participantKeys[xpdlParticipants[i]['-Id']] === undefined) {
                xpdlParticipants.splice(i, 1);
                
            }
        }
        ProcessBuilder.setArray(xpdl, 'Participants', 'Participant', xpdlParticipants);
        
        //remove unused participant mapping
        var keys = Object.keys(CustomBuilder.data.participants);
        for (var i in keys) {
            var pid = keys[i].substring(keys[i].indexOf("::") + 2);
            if (participantKeys[pid] === undefined && keys[i].indexOf("::processStartWhiteList") === -1) {
                delete CustomBuilder.data.participants[keys[i]];
            }
        }
        
        ProcessBuilder.validate();
    },
    
    /*
     * Set the participant mapping to object properties
     */
    populateParticipantMapping : function (participant) {
        var id = ProcessBuilder.currentProcessData.properties.id + "::" + ((participant.className === "start")?"processStartWhiteList":participant.properties.id);
        var mapping = CustomBuilder.data['participants'][id];
        if (mapping !== undefined) {
            participant.properties['mapping_par_type'] = mapping.type; //user, group, department, hod, performer, workflow variable, plugin, role, 
            if (mapping.type === "user") {
                participant.properties['mapping_par_users'] = mapping.value;
            } else if (mapping.type === "group") {
                participant.properties['mapping_par_groups'] = mapping.value;
            } else if (mapping.type === "department" || mapping.type === "hod") {
                participant.properties['mapping_par_department'] = mapping.value;
            } else if (mapping.type === "requester" || mapping.type === "requesterHod" || mapping.type === "requesterHodIgnoreReportTo" || mapping.type === "requesterSubordinates" || mapping.type === "requesterDepartment") {
                participant.properties['mapping_par_type'] = "performer";
                participant.properties['mapping_par_performer_type'] = mapping.type;
                participant.properties['mapping_par_performer_act'] = mapping.value;
            } else if (mapping.type === "workflowVariable") {
                var temp = mapping.value.split(",");
                participant.properties['mapping_par_workflowVariable'] = temp[0];
                participant.properties['mapping_par_wv_type'] = temp[1];
            } else if (mapping.type === "plugin") {
                participant.properties['mapping_par_plugin'] = {
                    className : mapping.value,
                    properties : mapping.properties
                };
            } else if (mapping.type === "role") {
                participant.properties['mapping_par_type'] = "";
                participant.properties['mapping_par_role'] = mapping.value;
            }

            participant.mapping = mapping;
        }
    },
    
    /*
     * Update the participant mapping back to data
     */
    updateParticipantMapping : function (participant) {
        var id = ProcessBuilder.currentProcessData.properties.id + "::" + ((participant.className === "start")?"processStartWhiteList":participant.properties.id);
        var mapping = CustomBuilder.data['participants'][id];
        if (mapping === undefined) {
            mapping = {};
            CustomBuilder.data['participants'][id] = mapping;
        }
        mapping.type = "";
        
        if (participant.properties['mapping_par_type'] === "user" && participant.properties['mapping_par_users'] !== "") {
            mapping.type = "user";
            mapping.value = participant.properties['mapping_par_users'];
        } else if (participant.properties['mapping_par_type'] === "group" && participant.properties['mapping_par_groups'] !== "") {
            mapping.type = "group";
            mapping.value = participant.properties['mapping_par_groups'];
        } else if ((participant.properties['mapping_par_type'] === "department" || participant.properties['mapping_par_type'] === "hod") && participant.properties['mapping_par_department'] !== "") {
            mapping.type = participant.properties['mapping_par_type'];
            mapping.value = participant.properties['mapping_par_department'];
        } else if (participant.properties['mapping_par_type'] === "performer" && participant.properties['mapping_par_performer_type'] !== "") {
            mapping.type = participant.properties['mapping_par_performer_type'];
            mapping.value = participant.properties['mapping_par_performer_act'];
        } else if (participant.properties['mapping_par_type'] === "workflowVariable") {
            mapping.type = "workflowVariable";
            mapping.value = participant.properties['mapping_par_workflowVariable'] + "," + participant.properties['mapping_par_wv_type'];
        } else if (participant.properties['mapping_par_type'] === "plugin") {
            if (participant.properties['mapping_par_plugin'] !== undefined
                    && participant.properties['mapping_par_plugin']['className'] !== undefined
                    && participant.properties['mapping_par_plugin']['className'] !== "") {
                mapping.type = "plugin";
                mapping.value = participant.properties['mapping_par_plugin']['className'];
                mapping.properties = $.extend(true, {}, participant.properties['mapping_par_plugin']['properties']);
            }
        } else if (participant.className === "start" && participant.properties['mapping_par_type'] === "" && participant.properties['mapping_par_role'] !== "") {
            mapping.type = "role";
            mapping.value = participant.properties['mapping_par_role'];
        }
        
        if (mapping.type === "") {
            delete CustomBuilder.data['participants'][id];
        } if (mapping.type === "plugin" && (mapping.value === "" || mapping.value === undefined)) {
            mapping.properties = {};
        }
    },
    
    /*
     * Set the activity mapping to object properties
     */
    populateActivityMapping : function (activity) {
        var id = ProcessBuilder.currentProcessData.properties.id + "::" + ((activity.className === "start")?"runProcess":activity.properties.id);
        var mapping = CustomBuilder.data['activityPlugins'][id];
        
        if (activity.className === "activity" || activity.className === "start") {
            var formMapping = CustomBuilder.data['activityForms'][id];
            
            if (formMapping !== undefined) {
                activity.properties['mapping_act_type'] = formMapping.type;
                activity.properties['mapping_act_formId'] = formMapping.formId;
                activity.properties['mapping_act_formUrl'] = formMapping.formUrl;
                activity.properties['mapping_act_formIFrameStyle'] = formMapping.formIFrameStyle;
                activity.properties['mapping_act_disableSaveAsDraft'] = formMapping.disableSaveAsDraft + "";
                activity.properties['mapping_act_autoContinue'] = formMapping.autoContinue + "";
                        
                activity.formMapping = formMapping;
            }
            if (mapping !== undefined) {
                activity.properties['mapping_act_modifier'] = {
                    className : mapping.className,
                    properties : $.extend(true, {}, mapping.properties)
                };
            }
        } else if (activity.className === "tool") {
            //convert to multi tools by default
            if (mapping !== undefined) {
                if (mapping.className !== "org.joget.apps.app.lib.MultiTools") {
                    activity.properties['tools'] = [
                        {
                            className : mapping.className,
                            properties : $.extend(true, {}, mapping.properties)
                        }
                    ];
                } else {
                    $.extend(true, activity.properties, mapping.properties);
                }
            }
        } else if (activity.className === "route") {
            if (mapping !== undefined) {
                activity.properties['mapping_act_plugin'] = {
                    className : mapping.className,
                    properties : $.extend(true, {}, mapping.properties)
                };
            }
        }
        
        if (mapping !== undefined) {
            activity.mapping = mapping;
        }
    },
    
    /*
     * Update the activity mapping back to data
     */
    updateActivityMapping : function (activity) {
        var id = ProcessBuilder.currentProcessData.properties.id + "::" + ((activity.className === "start")?"runProcess":activity.properties.id);
        if (activity.className === "activity" || activity.className === "start") {
            var formMapping = CustomBuilder.data['activityForms'][id];
            
            if (formMapping === undefined) {
                formMapping = {};
                CustomBuilder.data['activityForms'][id] = formMapping;
            }
            formMapping.type = activity.properties['mapping_act_type'];
            if (formMapping.type === undefined) {
                formMapping.type = "SINGLE";
            }
            if (formMapping.type === "SINGLE") {
                formMapping.formId = (activity.properties['mapping_act_formId'] !== undefined)?activity.properties['mapping_act_formId']:"";
                formMapping.disableSaveAsDraft = (activity.properties['mapping_act_disableSaveAsDraft'] === "true");
                
                delete formMapping['formUrl'];
                delete formMapping['formIFrameStyle'];
            } else {
                formMapping.formUrl = (activity.properties['mapping_act_formUrl'] !== undefined)?activity.properties['mapping_act_formUrl']:"";
                formMapping.formIFrameStyle = (activity.properties['mapping_act_formIFrameStyle'] !== undefined)?activity.properties['mapping_act_formIFrameStyle']:"";
            
                delete formMapping['formId'];
                delete formMapping['disableSaveAsDraft'];
            }
            formMapping.autoContinue = (activity.properties['mapping_act_autoContinue'] === "true");
            
            if (activity.properties['mapping_act_modifier'] !== undefined 
                    && activity.properties['mapping_act_modifier']['className'] !== undefined 
                    && activity.properties['mapping_act_modifier']['className'] !== "" ) {
                CustomBuilder.data['activityPlugins'][id] = $.extend(true, {}, activity.properties['mapping_act_modifier']);
            } else if (CustomBuilder.data['activityPlugins'][id] !== undefined) {
                delete CustomBuilder.data['activityPlugins'][id];
            }
        } else if (activity.className === "tool") {
            var mapping = CustomBuilder.data['activityPlugins'][id];
            
            if (activity.properties['tools'] !== undefined && activity.properties['tools'].length === 1 
                    && mapping !== undefined
                    && mapping.className === activity.properties['tools'][0]['className']
                    && (activity.properties['comment'] === "" || activity.properties['comment'] === undefined)
                    && (activity.properties['runInMultiThread'] === "" || activity.properties['runInMultiThread'] === undefined)) {
                //continue using single tool
                mapping.properties = $.extend(true, mapping.properties, activity.properties['tools'][0]['properties']);
            } else if ((activity.properties['tools'] === undefined || activity.properties['tools'].length === 0) && mapping !== undefined) {
                delete CustomBuilder.data['activityPlugins'][id];
            } else {
                //use multi tools
                if (mapping === undefined) {
                    mapping = {};
                    CustomBuilder.data['activityPlugins'][id] = mapping;
                }
                mapping.className = "org.joget.apps.app.lib.MultiTools";
                if (mapping.properties === undefined) {
                    mapping.properties = {};
                }
                
                for (var p in ProcessBuilder.multiToolProps) {
                    for (var i in ProcessBuilder.multiToolProps[p].properties) {
                        var property = ProcessBuilder.multiToolProps[p].properties[i];
                        mapping.properties[property.name] = activity.properties[property.name];
                    }
                }
            }
        } else if (activity.className === "route") {
            var mapping = CustomBuilder.data['activityPlugins'][id];
            if (activity.properties['mapping_act_plugin'] !== undefined 
                    && activity.properties['mapping_act_plugin']['className'] !== undefined
                    && activity.properties['mapping_act_plugin']['className'] !== "") {
                if (mapping === undefined) {
                    mapping = {};
                    CustomBuilder.data['activityPlugins'][id] = mapping;
                }
                mapping = $.extend(true, {}, activity.properties['mapping_act_plugin']);
                CustomBuilder.data['activityPlugins'][id] = mapping;
            } else if (mapping !== undefined){
                delete CustomBuilder.data['activityPlugins'][id];
            }
        }
    },
    
    /*
     * Update the activity properties back to xpdl data
     */
    updateXpdlActivities : function(xpdlActivities, xpdlProcessesAttrs, participant) {
        var self = CustomBuilder.Builder;
        
        for (var a in participant.activities) {
            var activity = participant.activities[a];
            
            if (activity.className !== "start" && activity.className !== "end") {
                var xpdlObj = activity.xpdlObj;
                if (xpdlObj === undefined) {
                    xpdlObj = {
                        "ExtendedAttributes": {"ExtendedAttribute": []}
                    };
                    activity.xpdlObj = xpdlObj;

                    xpdlActivities.push(xpdlObj);
                }
                
                xpdlObj['-Id'] = activity.properties.id;
                
                if (activity.properties.label !== undefined && activity.properties.label !== "") {
                    xpdlObj['-Name'] = activity.properties.label;
                } else {
                    delete xpdlObj['Name'];
                }
                
                xpdlObj['Performer'] = participant.properties.id;
                
                if (activity.properties.limit !== undefined && activity.properties.limit !== "") {
                    xpdlObj['Limit'] = activity.properties.limit;
                } else {
                    delete xpdlObj['Limit'];
                }
                
                //deadline
                if (activity.properties.deadlines !== undefined && activity.properties.deadlines.length > 0) {
                    var deadlines = [];
                    for (var d in activity.properties.deadlines) {
                        var deadline = activity.properties.deadlines[d];
                        var dObj = {
                            '-Execution' : deadline.execution
                        };
                        
                        // determine condition
                        var deadlineCondition;
                        
                        //if date, and the value is not quoted and it is not workflow variable, add quote for it
                        var pDataFields = ProcessBuilder.currentProcessData.properties.dataFields;
                        if ((deadline.durationUnit === 'd' || deadline.durationUnit ==='t' || deadline.durationUnit === '1' || deadline.durationUnit === '2')) {
                            if (!((deadline.deadlineLimit.substring(0, 1) === "\"" && deadline.deadlineLimit.substring(deadline.deadlineLimit.length - 1, deadline.deadlineLimit.length) === "\"") 
                                    || (deadline.deadlineLimit.substring(0, 1) === "'" && deadline.deadlineLimit.substring(deadline.deadlineLimit.length - 1, deadline.deadlineLimit.length) === "'"))) {
                                //check is workflow variable
                                var isWV = false;
                                for (var df=0; df<pDataFields.length; df++) {
                                    var dataField = pDataFields[df];
                                    if (dataField.variableId === deadline.deadlineLimit) {
                                        isWV = true;
                                        break;
                                    }
                                }
                                if (!isWV) {
                                    deadline.deadlineLimit = "\"" + deadline.deadlineLimit + "\"";
                                }
                            } else {
                                for (var df=0; df<pDataFields.length; df++) {
                                    var dataField = pDataFields[df];
                                    if ("\"" + dataField.variableId + "\"" === deadline.deadlineLimit || "'" + dataField.variableId + "'" === deadline.deadlineLimit) {
                                        deadline.deadlineLimit = dataField.variableId;
                                        break;
                                    }
                                }
                            }
                        }
                        
                        if (deadline.durationUnit === 'd') {
                            deadlineCondition = "var d = new java.text.SimpleDateFormat('dd/MM/yyyy'); d.parse(" + deadline.deadlineLimit + ");";
                        } else if (deadline.durationUnit ==='t') {
                            deadlineCondition = "var d = new java.text.SimpleDateFormat('dd/MM/yyyy HH:mm'); d.parse(" + deadline.deadlineLimit + ");";
                        } else if (deadline.durationUnit === '1') {
                            deadlineCondition = "var d = new java.text.SimpleDateFormat('yyyy-MM-dd'); d.parse(" + deadline.deadlineLimit + ");";
                        } else if (deadline.durationUnit === '2') {
                            deadlineCondition = "var d = new java.text.SimpleDateFormat('yyyy-MM-dd HH:mm'); d.parse(" + deadline.deadlineLimit + ");";
                        } else {
                            var limit = (deadline.deadlineLimit) ? deadline.deadlineLimit : "";
                            var duration = "";
                            if (deadline.durationUnit === 'D') {
                                duration += (24 * 60 * 60 * 1000);
                            } else if (deadline.durationUnit === 'h') {
                                duration += (60 * 60 * 1000);
                            } else if (deadline.durationUnit === 'm') {
                                duration += (60 * 1000);
                            } else {
                                duration += (1000);
                            }
                            duration = "(" + limit + "*" + duration + ")";
                            deadlineCondition = "var " + deadline.durationUnit + "=new java.util.Date(); " + deadline.durationUnit + ".setTime(ACTIVITY_ACTIVATED_TIME.getTime()+" + duration + "); " + deadline.durationUnit + ";";
                        }
                        dObj['DeadlineCondition'] = deadlineCondition;
                        dObj['ExceptionName'] = deadline.exceptionName;
                        deadlines.push(dObj);
                    }
                    ProcessBuilder.setArray(xpdlObj, null, 'Deadline', deadlines);
                } else {
                    delete xpdlObj['Deadline'];
                }
                
                if (activity.className === "tool") {
                    xpdlObj['Implementation'] = {
                        "Tool": {
                            "-Id": "default_application",
                            "-self-closing": "true"
                        }
                    };
                } else if (activity.className === "route") {
                    xpdlObj["Route"] = {
                        "-self-closing": "true"
                    };
                } else if (activity.className === "subflow") {
                    var parameters = [];
                    xpdlObj['Implementation'] = {
                        "SubFlow": {
                            "ActualParameters" : {},
                            "-Id" : activity.properties.subflowId,
                            "-Execution": activity.properties.execution
                        }
                    };
                    
                    for (var i  in activity.properties.actualParameters) {
                        parameters.push(activity.properties.actualParameters[i]['actualParameter']);
                    }
                    
                    ProcessBuilder.setArray(xpdlObj['Implementation']['SubFlow'], 'ActualParameters', 'ActualParameter', parameters);
                } else {
                    xpdlObj['Implementation'] = {
                        "No": {
                            "-self-closing": "true"
                        }
                    };
                }
                
                //join & split
                var transitionRestriction = {};
                if (xpdlObj['TransitionRestrictions'] !== undefined && xpdlObj['TransitionRestrictions']['TransitionRestriction'] !== undefined ) {
                    transitionRestriction = xpdlObj['TransitionRestrictions']['TransitionRestriction'];
                }
                var actElement = self.frameBody.find("#" + activity.properties.id);
                var sourceConnSet = ProcessBuilder.jsPlumb.getConnections({source: $(actElement)});
                for (var i = sourceConnSet.length - 1; i >= 0; i--) {
                    if ($(sourceConnSet[i].target).hasClass("end")) { 
                        sourceConnSet.splice(i, 1);
                    }
                }
                if (sourceConnSet.length > 1) {
                    if (activity.properties.split === "") {
                        activity.properties.split = "XOR";
                    }
                    if (transitionRestriction['Split'] === undefined) {
                        transitionRestriction['Split'] = {
                            "-Type": activity.properties.split,
                            "TransitionRefs": {
                                "TransitionRef": []
                            }
                        };
                    } else {
                        transitionRestriction['Split']['-Type'] = activity.properties.split;
                    }
                    if (transitionRestriction['Split']['-self-closing'] !== undefined) {
                        delete transitionRestriction['Split']['-self-closing'];
                        transitionRestriction['Split']['TransitionRefs'] = {"TransitionRef" : []};
                    }
                    var tids = [];
                    for (var c in sourceConnSet) {
                        tids.push($(sourceConnSet[c].canvas).data("data").properties.id);
                    }
                    var transitionRefs = ProcessBuilder.getArray(transitionRestriction['Split']["TransitionRefs"], "TransitionRef");
                    transitionRefs.forEach(function(transitionRef, index, object) {
                        var fi = $.inArray(transitionRef['-Id'], tids);
                        if (fi === -1) {
                            object.splice(index, 1);
                        } else {
                            tids.splice(fi, 1);
                        }
                    });
                    for (var tid in tids) {
                        transitionRefs.push({
                            "-Id": tids[tid],
                            "-self-closing": "true"
                        });
                    }
                    ProcessBuilder.setArray(transitionRestriction['Split'], "TransitionRefs", "TransitionRef", transitionRefs);
                } else {
                    activity.properties.split = "";
                    delete transitionRestriction['Split'];
                }
                var targetConnSet = ProcessBuilder.jsPlumb.getConnections({target: $(actElement)});
                if (targetConnSet.length > 1) {
                    if (activity.properties.join === "") {
                        activity.properties.join = "XOR";
                    }
                    if (transitionRestriction['Join'] === undefined) {
                        transitionRestriction['Join'] = {
                            "-Type": activity.properties.join,
                            "-self-closing": "true"
                        };
                    } else {
                        transitionRestriction['Join']['-Type'] = activity.properties.join;
                    }
                } else {
                    activity.properties.join = "";
                    delete transitionRestriction['Join'];
                }
                if (activity.properties.split === "" && activity.properties.join === "") {
                    delete xpdlObj['TransitionRestrictions'];
                } else {
                    xpdlObj['TransitionRestrictions'] = {
                        'TransitionRestriction' : transitionRestriction
                    };
                }
                
                xpdlObj["ExtendedAttributes"]['ExtendedAttribute'] = [{
                    "-Name": "JaWE_GRAPH_PARTICIPANT_ID",
                    "-Value": xpdlObj['Performer'],
                    "-self-closing": "true"
                },{
                    "-Name": "JaWE_GRAPH_OFFSET",
                    "-Value": activity.x_offset + "," + activity.y_offset,
                    "-self-closing": "true"
                }];
            } else {
                //start and end node
                var xpdlObj = activity.xpdlObj;
                if (xpdlObj === undefined) {
                    xpdlObj = {
                        "-Name": "JaWE_GRAPH_"+activity.className.toUpperCase()+"_OF_WORKFLOW",
                        "-Value": "",
                        "-self-closing": "true"
                    };
                    activity.xpdlObj = xpdlObj;
                    xpdlProcessesAttrs.push(xpdlObj);
                }
                
                var actElement = self.frameBody.find("#" + activity.properties.id);
                var actId = "";
                if (activity.className === "start") {
                    var connSet = ProcessBuilder.jsPlumb.getConnections({source: $(actElement)});
                    if (connSet.length > 0) {
                        actId = $(connSet[0].target).attr("id");
                    }
                    
                    ProcessBuilder.updateParticipantMapping(activity);
                } else {
                    var connSet = ProcessBuilder.jsPlumb.getConnections({target: $(actElement)});
                    if (connSet.length > 0) {
                        actId = $(connSet[0].source).attr("id");
                    }
                }
                xpdlObj['-Value'] = "JaWE_GRAPH_PARTICIPANT_ID="+participant.properties.id+",CONNECTING_ACTIVITY_ID="+actId+",X_OFFSET="+activity.x_offset+",Y_OFFSET="+activity.y_offset+",JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE="+activity.className.toUpperCase()+"_DEFAULT";
            }
            
            ProcessBuilder.updateActivityMapping(activity);
        }
    },
    
    /*
     * Show the process properties editor panel
     */
    editProcess : function(){
        var self = CustomBuilder.Builder;
        
        self.selectNode(false);
        CustomBuilder.Builder.selectedEl= self.frameBody.find(".process");
        self._showPropertiesPanel(self.frameBody.find(".process"), ProcessBuilder.currentProcessData, self.getComponent('process'));
    },
    
    /*
     * delete the process from xpdl, create empty process if it is the last process to delete
     */
    deleteProcess : function(){
        var self = CustomBuilder.Builder;
        
        var data = ProcessBuilder.currentProcessData;
        var xpdl = CustomBuilder.data.xpdl['Package'];
        var xpdlProcesses = ProcessBuilder.getArray(xpdl['WorkflowProcesses'], 'WorkflowProcess');
        
        $('#process-selector select [value="'+ProcessBuilder.currentProcessData.properties.id+'"]').remove();
        $('#process-selector select').trigger("chosen:updated");

        if (data.xpdlObj !== undefined) {
            var index = $.inArray(data.xpdlObj, xpdlProcesses);
            if (index !== -1) {
                xpdlProcesses.splice(index, 1);
            }
            ProcessBuilder.setArray(xpdl, 'WorkflowProcesses', 'WorkflowProcess', xpdlProcesses);
            
            if (xpdlProcesses.length === 0) {
                ProcessBuilder.addEmptyProcess();
            }
        }

        ProcessBuilder.currentProcessData = null;
        
        CustomBuilder.update();
        ProcessBuilder.updateProcessSelector();
        self.triggerChange();
        
        window.location.hash = "";
    },
    
    /*
     * Create a new empty process model to xpdl
     */
    addEmptyProcess : function() {
        var xpdl = CustomBuilder.data.xpdl['Package'];
        var xpdlProcesses = ProcessBuilder.getArray(xpdl['WorkflowProcesses'], 'WorkflowProcess');
        
        var id;
        var count = xpdlProcesses.length;
        if (count > 0) {
            var ids = [];
            for (var p in xpdlProcesses) {
                ids.push(xpdlProcesses[p]['-Id']);
            }
            do {
                id = "process" + ++count;
            } while ($.inArray(id, ids) !== -1)
        } else {
            id = "process1";
            count = 1;
        }
        
        //create a participant
        var xpdlParticipants = ProcessBuilder.getArray(xpdl['Participants'], 'Participant');
        var pcount = xpdlParticipants.length;
        var pid;
        if (pcount > 1) {
            var pids = [];
            for (var p in xpdlParticipants) {
                pids.push(xpdlParticipants[p]['-Id']);
            }
            do {
                pid = id + "_participant" + ++pcount;
            } while ($.inArray(pid, pids) !== -1)
        } else {
            pid = id + "_participant1";
        }
        var newParticipant = {
            "-Name": get_cbuilder_msg("pbuilder.label.participant"),
            "-Id": pid,
            "ParticipantType": {
                "-Type": "ROLE",
                "-self-closing": "true"
            }
        };
        xpdlParticipants.push(newParticipant);
        
        ProcessBuilder.setArray(xpdl, 'Participants', 'Participant', xpdlParticipants);
        
        var emptyProcess = {
            "ExtendedAttributes": {
                "ExtendedAttribute": [
                    {
                        "-Name": "JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER",
                        "-Value": pid,
                        "-self-closing": "true"
                    },
                    {
                        "-Name": "JaWE_GRAPH_START_OF_WORKFLOW",
                        "-Value": "JaWE_GRAPH_PARTICIPANT_ID="+pid+",CONNECTING_ACTIVITY_ID=,X_OFFSET=75,Y_OFFSET=46,JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=START_DEFAULT",
                        "-self-closing": "true"
                    }
                ]
            },
            "-Name": get_cbuilder_msg('pbuilder.label.process') + " " + count,
            "ProcessHeader": {
                "-DurationUnit": "h",
                "-self-closing": "true"
            },
            "-Id": id,
            "DataFields": {
                "DataField": {
                    "-IsArray": "FALSE",
                    "-Id": "status",
                    "DataType": {
                        "BasicType": {
                            "-Type": "STRING",
                            "-self-closing": "true"
                        }
                    }
                }
            },
        };
        xpdlProcesses.push(emptyProcess);
        
        ProcessBuilder.setArray(xpdl, 'WorkflowProcesses', 'WorkflowProcess', xpdlProcesses);
        
        //add process start whitelist to admin
        if (CustomBuilder.data.participants === undefined || CustomBuilder.data.participants === null) {
            CustomBuilder.data.participants = {};
        }
        CustomBuilder.data.participants[id + "::processStartWhiteList"] = {
            "type": "role",
            "value": "adminUser",
            "properties": {}
        };
        
        return emptyProcess;
    },
    
    /*
     * clone the current process and set as current process to edit
     */
    cloneProcess : function(){
        var self = CustomBuilder.Builder;
        var xpdl = CustomBuilder.data.xpdl['Package'];
        var xpdlProcesses = ProcessBuilder.getArray(xpdl['WorkflowProcesses'], 'WorkflowProcess');
        
        var cloneProcessData = $.extend(true, {}, ProcessBuilder.currentProcessData.xpdlObj);
        var oriId = ProcessBuilder.currentProcessData.properties.id;
                
        var id = cloneProcessData['-Id'];
        var count = 1;
        var ids = [];
        for (var p in xpdlProcesses) {
            ids.push(xpdlProcesses[p]['-Id']);
        }
        do {
            id = cloneProcessData['-Id'] + "_" + ++count;
        } while ($.inArray(id, ids) !== -1);
        cloneProcessData['-Id'] = id;  
        cloneProcessData['-Name'] = cloneProcessData['-Name'] + " " + get_cbuilder_msg("pbuilder.label.copy");
        
        //remove participants not used by any processes
        var participantKeys = {};
        var xpdlProcesses = ProcessBuilder.getArray(xpdl['WorkflowProcesses'], 'WorkflowProcess');
        for (var p in xpdlProcesses) {
            var xpdlProcessesAttrs = ProcessBuilder.getArray(xpdlProcesses[p]['ExtendedAttributes'], 'ExtendedAttribute');
            for (var a = 0; a < xpdlProcessesAttrs.length; p++) {
                if (xpdlProcessesAttrs[a]['-Name'] === "JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER") {
                    var ids = xpdlProcessesAttrs[a]['-Value'].split(";");
                    for (var i in ids) {
                        participantKeys[ids[i]] = "";
                    }
                    break;
                }
            }
        }
        
        var xpdlParticipants = ProcessBuilder.getArray(xpdl['Participants'], 'Participant');
        for (var i = xpdlParticipants.length - 1; i >= 0; i--) {
            if (participantKeys[xpdlParticipants[i]['-Id']] === undefined) {
                xpdlParticipants.splice(i, 1);
            }
        }
        
        //remove unused participant mapping
        var keys = Object.keys(CustomBuilder.data.participants);
        for (var i in keys) {
            var pid = keys[i].substring(keys[i].indexOf("::") + 2);
            if (participantKeys[pid] === undefined && keys[i].indexOf("::processStartWhiteList") === -1) {
                delete CustomBuilder.data.participants[keys[i]];
            }
        }
        
        //clone participant
        var currentParticipants = ProcessBuilder.currentProcessData.participants;
        for (var c in currentParticipants) {
            var xpdlObj = $.extend(true, {}, currentParticipants[c].xpdlObj);
            
            //check the participant is exist
            if (participantKeys[xpdlObj['-Id']] !== undefined) {
                var oriPid = xpdlObj['-Id'];
                if (xpdlObj['-Id'].indexOf(oriId) === 0) { //if keep prepending the process id, the participant id will become very long and over 255 chars
                    xpdlObj['-Id'] = id + xpdlObj['-Id'].substring(oriId.length);
                } else {
                    xpdlObj['-Id'] = id + "_" + xpdlObj['-Id'];
                }
                if (participantKeys[xpdlObj['-Id']] === undefined) {     
                    xpdlParticipants.push(xpdlObj);
                    participantKeys[xpdlObj['-Id']] = "";
                    participantKeys[oriPid] = xpdlObj['-Id'];
                }
            }
        }
        ProcessBuilder.setArray(xpdl, 'Participants', 'Participant', xpdlParticipants);
        
        //update participant in all activities and attributes
        var xpdlProcessesAttrs = ProcessBuilder.getArray(cloneProcessData['ExtendedAttributes'], 'ExtendedAttribute');
        for (var p = 0; p < xpdlProcessesAttrs.length; p++) {
            if (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER") {
                var porders = xpdlProcessesAttrs[p]['-Value'].split(";");
                for (var j in porders) {
                    porders[j] = participantKeys[porders[j]];
                }
                xpdlProcessesAttrs[p]['-Value'] = porders.join(";");
            } else if (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_START_OF_WORKFLOW" || xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_END_OF_WORKFLOW") {
                var keys = Object.keys(participantKeys);
                for (var i in keys) {
                    if (xpdlProcessesAttrs[p]['-Value'].indexOf("JaWE_GRAPH_PARTICIPANT_ID=" + keys[i] + ",") !== -1) {
                        xpdlProcessesAttrs[p]['-Value'] = xpdlProcessesAttrs[p]['-Value'].replace("JaWE_GRAPH_PARTICIPANT_ID="+keys[i] + ",", "JaWE_GRAPH_PARTICIPANT_ID="+participantKeys[keys[i]] + ",");
                        break;
                    }
                }
            }
        }
        ProcessBuilder.setArray(cloneProcessData, 'ExtendedAttributes', 'ExtendedAttribute', xpdlProcessesAttrs);
        
        var xpdlActivities = ProcessBuilder.getArray(cloneProcessData['Activities'], 'Activity');
        for (var a in xpdlActivities) {
            var newpid = participantKeys[xpdlActivities[a]['Performer']];
            xpdlActivities[a]['Performer'] = newpid;
            
            var attrs = ProcessBuilder.getArray(xpdlActivities[a]['ExtendedAttributes'], 'ExtendedAttribute');
            for (var at in attrs) {
                if (attrs[at]['-Name'] === "JaWE_GRAPH_PARTICIPANT_ID") {
                    attrs[at]['-Value'] = newpid;
                }
            }
        }
        ProcessBuilder.setArray(cloneProcessData, 'Activities', 'Activity', xpdlActivities);
        
        xpdlProcesses.push(cloneProcessData);
        ProcessBuilder.setArray(xpdl, 'WorkflowProcesses', 'WorkflowProcess', xpdlProcesses);
        
        //clone mappings
        var newParticipantMapping = {};
        for (var key in CustomBuilder.data.participants) {
            if (key.indexOf(oriId + "::") === 0) {
                var newpid = participantKeys[key.substring(key.indexOf("::") + 2)];
                if (key === oriId + "::processStartWhiteList") {
                    newpid = "processStartWhiteList";
                }
                newParticipantMapping[id + "::" + newpid] = $.extend({}, CustomBuilder.data.participants[key]);
            }
        }
        
        //force new clone process start whitelist to admin if it is unset
        if (newParticipantMapping[id + "::processStartWhiteList"] === undefined) {
            newParticipantMapping[id + "::processStartWhiteList"] = {
                "type": "role",
                "value": "adminUser",
                "properties": {}
            };
        }
        
        $.extend(CustomBuilder.data.participants, newParticipantMapping);
        
        var newFormMapping = {};
        for (var key in CustomBuilder.data.activityForms) {
            if (key.indexOf(oriId + "::") === 0) {
                newFormMapping[key.replace(oriId + "::", id + "::")] = $.extend({}, CustomBuilder.data.activityForms[key]);
            }
        }
        $.extend(CustomBuilder.data.activityForms, newFormMapping);
        
        var newPluginsMapping = {};
        for (var key in CustomBuilder.data.activityPlugins) {
            if (key.indexOf(oriId + "::") === 0) {
                newPluginsMapping[key.replace(oriId + "::", id + "::")] = $.extend({}, CustomBuilder.data.activityPlugins[key]);
            }
        }
        $.extend(CustomBuilder.data.activityPlugins, newPluginsMapping);
        
        CustomBuilder.update();
        ProcessBuilder.updateProcessSelector();
        self.triggerChange();
        
        window.location.hash = cloneProcessData['-Id'];
    },
    
    /*
     * add a new empty process to xpdl to edit
     */
    addProcess : function(){
        var self = CustomBuilder.Builder;
        
        var process = ProcessBuilder.addEmptyProcess();
        CustomBuilder.update();
        ProcessBuilder.updateProcessSelector();
        self.triggerChange();
        
        window.location.hash = process['-Id'];
    },
    
    /*
     * Prepare the components to render in canvas
     */
    initComponents : function() {
        //Process
        CustomBuilder.initPaletteElement("", "process", get_cbuilder_msg('pbuilder.label.process'), '<i class="fas fa-th-list"></i>',  
            [{
                title: get_cbuilder_msg("pbuilder.label.processProperties"),
                helplink: get_cbuilder_msg("pbuilder.label.processProperties.helplink"),
                properties: [{
                    name: 'id',
                    label: get_cbuilder_msg("pbuilder.label.id"),
                    type: 'textfield',
                    required: 'True',
                    js_validation: "ProcessBuilder.validateProcessDuplicateId",
                    regex_validation: '^[a-zA-Z0-9_]+$',
                    validation_message: get_cbuilder_msg("pbuilder.label.invalidId")
                },{
                    name: 'label',
                    label: get_cbuilder_msg("pbuilder.label"),
                    type: 'textfield',
                    required: 'True',
                    value: get_cbuilder_msg("pbuilder.label.process")
                },{
                    name: 'dataFields',
                    label: get_cbuilder_msg("pbuilder.label.workflowVariables"),
                    type: 'grid',
                    columns: [{
                        key: 'variableId',
                        label: get_cbuilder_msg("pbuilder.label.variableId")
                    }],
                    js_validation: "ProcessBuilder.validateVariables"
                }]
            },{
                title: get_cbuilder_msg("pbuilder.label.subflowProperties"),
                properties: [{
                    name: 'formalParameters',
                    label: get_cbuilder_msg("pbuilder.label.formalParameters"),
                    type: 'grid',
                    columns: [{
                        key: 'parameterId',
                        label: get_cbuilder_msg("pbuilder.label.parameterId")
                    },{
                        key: 'mode',
                        label: get_cbuilder_msg("pbuilder.label.mode"),
                        options: [{
                            value: 'INOUT',
                            label: get_cbuilder_msg("pbuilder.label.inAndOut")
                        },{
                            value: 'IN',
                            label: get_cbuilder_msg("pbuilder.label.in")
                        },{
                            value: 'OUT',
                            label: get_cbuilder_msg("pbuilder.label.out")
                        }]
                    }]
                }]
            },{
                title: get_cbuilder_msg("pbuilder.label.slaOptions"),
                helplink: get_cbuilder_msg("pbuilder.label.slaOptions.helplink"),
                properties: [{
                    name: 'durationUnit',
                    label: get_cbuilder_msg("pbuilder.label.durationUnit"),
                    type: 'selectbox',
                    options: [{
                        value: 'D',
                        label: get_cbuilder_msg("pbuilder.label.day")
                    },{
                        value: 'h',
                        label: get_cbuilder_msg("pbuilder.label.hour")
                    },{
                        value: 'm',
                        label: get_cbuilder_msg("pbuilder.label.minute")
                    },{
                        value: 's',
                        label: get_cbuilder_msg("pbuilder.label.second")
                    }]
                },{
                    name: 'limit',
                    label: get_cbuilder_msg("pbuilder.label.limit"),
                    type: 'textfield',
                    regex_validation: '^[0-9_]+$'
                }]
            }]
        , "", false, "", {builderTemplate: {
            'draggable' : false,
            'movable' : false,
            'deletable' : false,
            'copyable' : false,
            'navigable' : false,
            'renderNodeAdditional' : false,
            'render' : ProcessBuilder.renderProcess,
            'supportStyle' : false
        }});
    
        //Participant
        CustomBuilder.initPaletteElement("", "participant", get_cbuilder_msg('pbuilder.label.participant'), '<i class="las la-swimmer"></i>',  
            [{
                title: get_cbuilder_msg("pbuilder.label.participantProperties"),
                helplink: get_cbuilder_msg("pbuilder.label.participantProperties.helplink"),
                properties: [{
                    name: 'label',
                    label: get_cbuilder_msg("pbuilder.label"),
                    type: 'textfield',
                    required: 'True',
                    value: get_cbuilder_msg("pbuilder.label.participant")
                },{
                    name: 'id',
                    label: get_cbuilder_msg("pbuilder.label.id"),
                    type: 'textfield',
                    required: 'True',
                    js_validation: "ProcessBuilder.validateParticipantDuplicateId",
                    regex_validation: '^[a-zA-Z0-9_]+$',
                    validation_message: get_cbuilder_msg("pbuilder.label.invalidId"),
                    id_suggestion: 'label'
                }]
            }]
        , "", true, "", {builderTemplate: {
            'dragHtml' : '<div class="participant"><div class="participant_handle"><div class="participant_label">'+get_cbuilder_msg('pbuilder.label.participant')+'</div></div><div class="activities-container"></div></div>',    
            'draggable' : true,
            'movable' : true,
            'deletable' : true,
            'copyable' : true,
            'navigable' : false,
            'parentContainerAttr' : 'participants',
            'childsContainerAttr' : 'activities',
            'parentDataHolder' : 'participants',
            'childsDataHolder' : 'activities',
            'render' : ProcessBuilder.renderParticipant,
            'dragging' : ProcessBuilder.dragParticipant,
            'unload' : ProcessBuilder.unloadParticipant,
            'getStylePropertiesDefinition' : ProcessBuilder.getParticipantDef,
            'dropEnd' : ProcessBuilder.dropParticipantEnd
        }});
        
        //Activity
        CustomBuilder.initPaletteElement("", "activity", get_cbuilder_msg('pbuilder.label.activity'), '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="47" height="36" viewBox="0 0 47 36" ><rect height="35" width="46" x="0" y="0" stroke-width="0.5" stroke="#333333" fill="#fefefe" transform="translate(0.25 0.25)"/></svg>', 
            [], {'join' : '', 'split' : ''}, true, "", {builderTemplate: {
            'dragHtml' : '<div class="node activity"><div class="node_label">'+get_cbuilder_msg('pbuilder.label.activity')+'</div></div>',
            'draggable' : true,
            'movable' : false,
            'deletable' : true,
            'copyable' : true,
            'navigable' : false,
            'absolutePosition' : true,
            'parentContainerAttr' : 'activities',
            'parentDataHolder' : 'activities',
            'render' : ProcessBuilder.renderActivity,
            'dragging' : ProcessBuilder.dragActivity,
            'unload' : ProcessBuilder.unloadActivity,
            'getStylePropertiesDefinition' : ProcessBuilder.getActivityDef,
            'nodeDetailContainerColorNumber' : function() {
                return 3;
            },
            'customPropertyOptions' : function(elementOptions, element, elementObj, component) {
                var options = [{
                    title: get_cbuilder_msg("pbuilder.label.activityProperties"),
                    helplink : get_cbuilder_msg("pbuilder.label.activityProperties.helplink"),
                    properties: [{
                        name: 'label',
                        label: get_cbuilder_msg("pbuilder.label"),
                        type: 'textfield',
                        required: 'True',
                        value: this.type
                    },{
                        name: 'id',
                        label: get_cbuilder_msg("pbuilder.label.id"),
                        type: 'textfield',
                        required: 'True',
                        js_validation: "ProcessBuilder.validateDuplicateId",
                        regex_validation: '^[a-zA-Z0-9_]+$',
                        validation_message: get_cbuilder_msg("pbuilder.label.invalidId"),
                        id_suggestion: 'label'
                    }]
                },{
                    title: get_cbuilder_msg("pbuilder.label.deadlines"),
                    helplink : get_cbuilder_msg("pbuilder.label.deadlines.helplink"),
                    properties: [{
                        name: 'deadlines',
                        label: get_cbuilder_msg("pbuilder.label.deadlines"),
                        type: 'grid',
                        columns: [{
                            key: 'execution',
                            label: get_cbuilder_msg("pbuilder.label.execution"),
                            options: [{
                                value: 'ASYNCHR',
                                label: get_cbuilder_msg("pbuilder.label.asynchronous")
                            },{
                                value: 'SYNCHR',
                                label: get_cbuilder_msg("pbuilder.label.synchronous")
                            }]
                        },{
                            key: 'durationUnit',
                            label: get_cbuilder_msg("pbuilder.label.durationUnit"),
                            options: [{
                                value: 'D',
                                label: get_cbuilder_msg("pbuilder.label.day")
                            },{
                                value: 'h',
                                label: get_cbuilder_msg("pbuilder.label.hour")
                            },{
                                value: 'm',
                                label: get_cbuilder_msg("pbuilder.label.minute")
                            },{
                                value: 's',
                                label: get_cbuilder_msg("pbuilder.label.second")
                            },{
                                value: 'd',
                                label: get_cbuilder_msg("pbuilder.label.dateFormat")
                            },{
                                value: '1',
                                label: get_cbuilder_msg("pbuilder.label.dateFormat2")
                            },{
                                value: 't',
                                label: get_cbuilder_msg("pbuilder.label.dateTimeFormat")
                            },{
                                value: '2',
                                label: get_cbuilder_msg("pbuilder.label.dateTimeFormat2")
                            }]
                        },{
                            key: 'deadlineLimit',
                            label: get_cbuilder_msg("pbuilder.label.deadlineLimit")
                        },{
                            key: 'exceptionName',
                            label: get_cbuilder_msg("pbuilder.label.exceptionName")
                        }]
                    }]
                },{
                    title: get_cbuilder_msg("pbuilder.label.slaOptions"),
                    helplink: get_cbuilder_msg("pbuilder.label.slaOptions.helplink"),
                    properties: [{
                        name: 'limit',
                        label: get_cbuilder_msg("pbuilder.label.limit"),
                        type: 'textfield',
                        regex_validation: '^[0-9_]+$'
                    }]
                }];

                if (elementObj.properties.join !== "") {
                    options[0].properties.push({
                        name: 'join',
                        label: get_cbuilder_msg("pbuilder.label.joinType"),
                        type: "selectbox",
                        options: [{
                            value: 'AND',
                            label: get_cbuilder_msg("pbuilder.label.and")
                        },{
                            value: 'XOR',
                            label: get_cbuilder_msg("pbuilder.label.xor")
                        }]
                    });
                }
                if (elementObj.properties.split !== "") {
                    options[0].properties.push({
                        name: 'split',
                        label: get_cbuilder_msg("pbuilder.label.splitType"),
                        type: "selectbox",
                        options: [{
                            value: 'AND',
                            label: get_cbuilder_msg("pbuilder.label.and")
                        },{
                            value: 'XOR',
                            label: get_cbuilder_msg("pbuilder.label.xor")
                        }]
                    });
                }
                return options;
            }
        }});
    
        //Tool
        CustomBuilder.initPaletteElement("", "tool", get_cbuilder_msg('pbuilder.label.tool'), '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="47" height="36" viewBox="0 0 47 36" ><rect height="35" width="46" x="0" y="0" stroke-width="0.5" stroke="#333333" fill="#E1FFE0" transform="translate(0.25 0.25)"/></svg>', 
            [] , {'join' : '', 'split' : ''}, true, "", {builderTemplate: {
            'dragHtml' : '<div class="node tool"><div class="node_label">'+get_cbuilder_msg('pbuilder.label.tool')+'</div></div>',
            'draggable' : true,
            'movable' : false,
            'deletable' : true,
            'copyable' : true,
            'navigable' : false,
            'absolutePosition' : true,
            'parentContainerAttr' : 'activities',
            'parentDataHolder' : 'activities',
            'dragging' : ProcessBuilder.dragActivity,
            'render' : ProcessBuilder.renderActivity,
            'unload' : ProcessBuilder.unloadActivity,
            'getStylePropertiesDefinition' : ProcessBuilder.getToolDef,
            'nodeDetailContainerColorNumber' : function() {
                return 4;
            },
            'customPropertyOptions' : function(elementOptions, element, elementObj, component) {
                var options = [{
                    title: get_cbuilder_msg("pbuilder.label.toolProperties"),
                    helplink : get_cbuilder_msg("pbuilder.label.toolProperties.helplink"),
                    properties: [{
                        name: 'label',
                        label: get_cbuilder_msg("pbuilder.label"),
                        type: 'textfield',
                        required: 'True',
                        value: this.type
                    },{     
                        name: 'id',
                        label: get_cbuilder_msg("pbuilder.label.id"),
                        type: 'textfield',
                        required: 'True',
                        js_validation: "ProcessBuilder.validateDuplicateId",
                        regex_validation: '^[a-zA-Z0-9_]+$',
                        validation_message: get_cbuilder_msg("pbuilder.label.invalidId"),
                        id_suggestion: 'label'
                    }]
                }];
                if (elementObj.properties.join !== "") {
                    options[0].properties.push({
                        name: 'join',
                        label: get_cbuilder_msg("pbuilder.label.joinType"),
                        type: "selectbox",
                        options: [{
                            value: 'AND',
                            label: get_cbuilder_msg("pbuilder.label.and")
                        },{
                            value: 'XOR',
                            label: get_cbuilder_msg("pbuilder.label.xor")
                        }]
                    });
                }
                if (elementObj.properties.split !== "") {
                    options[0].properties.push({
                        name: 'split',
                        label: get_cbuilder_msg("pbuilder.label.splitType"),
                        type: "selectbox",
                        options: [{
                            value: 'AND',
                            label: get_cbuilder_msg("pbuilder.label.and")
                        },{
                            value: 'XOR',
                            label: get_cbuilder_msg("pbuilder.label.xor")
                        }]
                    });
                }
                return options;
            }
        }});
    
        //Route
        CustomBuilder.initPaletteElement("", "route", get_cbuilder_msg('pbuilder.label.route'), '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="43" height="43" viewBox="0 0 43 43" ><path d="M 21 0 L 0 21 L 21 42 L 42 21 Z" stroke-width="0.5" fill="#fdfde1" stroke="#333333" transform="translate(0.25 0.25)"/></svg>', 
            [] , {'join' : '', 'split' : ''}, true, "", {builderTemplate: {
            'dragHtml' : '<div class="node route"></div>',
            'draggable' : true,
            'movable' : false,
            'deletable' : true,
            'copyable' : true,
            'navigable' : false,
            'absolutePosition' : true,
            'parentContainerAttr' : 'activities',
            'parentDataHolder' : 'activities',
            'render' : ProcessBuilder.renderActivity,
            'dragging' : ProcessBuilder.dragActivity,
            'unload' : ProcessBuilder.unloadActivity,
            'getStylePropertiesDefinition' : ProcessBuilder.getRouteDef,
            'nodeDetailContainerColorNumber' : function() {
                return 5;
            },
            'customPropertyOptions' : function(elementOptions, element, elementObj, component) {
                var options = [{
                    title: get_cbuilder_msg("pbuilder.label.routeProperties"),
                    helplink : get_cbuilder_msg("pbuilder.label.routeProperties.helplink"),
                    properties: [{
                        name: 'id',
                        label: get_cbuilder_msg("pbuilder.label.id"),
                        type: 'textfield',
                        required: 'True',
                        js_validation: "ProcessBuilder.validateDuplicateId",
                        regex_validation: '^[a-zA-Z0-9_]+$',
                        validation_message: get_cbuilder_msg("pbuilder.label.invalidId")
                    },{
                        name: 'label',
                        label: get_cbuilder_msg("pbuilder.label"),
                        type: 'textfield'
                    }]
                }];
                if (elementObj.properties.join !== "") {
                    options[0].properties.push({
                        name: 'join',
                        label: get_cbuilder_msg("pbuilder.label.joinType"),
                        type: "selectbox",
                        options: [{
                            value: 'AND',
                            label: get_cbuilder_msg("pbuilder.label.and")
                        },{
                            value: 'XOR',
                            label: get_cbuilder_msg("pbuilder.label.xor")
                        }]
                    });
                }
                if (elementObj.properties.split !== "") {
                    options[0].properties.push({
                        name: 'split',
                        label: get_cbuilder_msg("pbuilder.label.splitType"),
                        type: "selectbox",
                        options: [{
                            value: 'AND',
                            label: get_cbuilder_msg("pbuilder.label.and")
                        },{
                            value: 'XOR',
                            label: get_cbuilder_msg("pbuilder.label.xor")
                        }]
                    });
                }
                return options;
            }
        }});
    
        //Subflow
        CustomBuilder.initPaletteElement("", "subflow", get_cbuilder_msg('pbuilder.label.subflow'), '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="50" height="50" viewBox="0 0 50 50"><rect fill="none" height="100" stroke="none" stroke-width="0" width="100" x="0" y="0" transform="translate(-0.5 -0.5)"/><rect height="35" width="46" x="0" y="0" stroke-width="0.5" stroke="#333333" fill="#fefefe" rx="2" transform="translate(1.5 6.5)"/><rect height="27" width="38" x="0" y="0" stroke-width="0.5" stroke="#333333" fill="#fefefe" rx="1" transform="translate(5.5 10.5)"/></svg>', 
            [] , {'join' : '', 'split' : ''}, true, "", {builderTemplate: {
            'dragHtml' : '<div class="node subflow"><div class="node_label">'+get_cbuilder_msg('pbuilder.label.subflow')+'</div></div>',
            'draggable' : true,
            'movable' : false,
            'deletable' : true,
            'copyable' : true,
            'navigable' : false,
            'absolutePosition' : true,
            'parentContainerAttr' : 'activities',
            'parentDataHolder' : 'activities',
            'render' : ProcessBuilder.renderActivity,
            'dragging' : ProcessBuilder.dragActivity,
            'unload' : ProcessBuilder.unloadActivity,
            'supportStyle' : false,
            'nodeDetailContainerColorNumber' : function() {
                return 6;
            },
            'customPropertyOptions' : function(elementOptions, element, elementObj, component) {
                var options = [{
                    title: get_cbuilder_msg("pbuilder.label.subflowProperties"),
                    helplink : get_cbuilder_msg("pbuilder.label.subflowProperties.helplink"),
                    properties: [{
                        name: 'label',
                        label: get_cbuilder_msg("pbuilder.label"),
                        type: 'textfield',
                        required: 'True',
                        value: this.type
                    },{
                        name: 'id',
                        label: get_cbuilder_msg("pbuilder.label.id"),
                        type: 'textfield',
                        required: 'True',
                        js_validation: "ProcessBuilder.validateDuplicateId",
                        regex_validation: '^[a-zA-Z0-9_]+$',
                        validation_message: get_cbuilder_msg("pbuilder.label.invalidId"),
                        id_suggestion: 'label'
                    },{
                        name: 'subflowId',
                        label: get_cbuilder_msg("pbuilder.label.subProcessId"),
                        type: 'textfield',
                        required: 'True'
                    },{
                        name: 'execution',
                        label: get_cbuilder_msg("pbuilder.label.execution"),
                        type: "selectbox",
                        options: [{
                            value: 'SYNCHR',
                            label: get_cbuilder_msg("pbuilder.label.synchronous")
                        },{
                            value: 'ASYNCHR',
                            label: get_cbuilder_msg("pbuilder.label.asynchronous")
                        }],
                        value: "SYNCHR"
                    },{
                        name: 'actualParameters',
                        label: get_cbuilder_msg("pbuilder.label.parameters"),
                        type: 'grid',
                        columns: [{
                            key: 'actualParameter',
                            label: get_cbuilder_msg("pbuilder.label.actualParameter")
                        }]
                    }]
                },{
                    title: get_cbuilder_msg("pbuilder.label.deadlines"),
                    helplink : get_cbuilder_msg("pbuilder.label.deadlines.helplink"),
                    properties: [{
                        name: 'deadlines',
                        label: get_cbuilder_msg("pbuilder.label.deadlines"),
                        type: 'grid',
                        columns: [{
                            key: 'execution',
                            label: get_cbuilder_msg("pbuilder.label.execution"),
                            options: [{
                                value: 'ASYNCHR',
                                label: get_cbuilder_msg("pbuilder.label.asynchronous")
                            },{
                                value: 'SYNCHR',
                                label: get_cbuilder_msg("pbuilder.label.synchronous")
                            }]
                        },{
                            key: 'durationUnit',
                            label: get_cbuilder_msg("pbuilder.label.durationUnit"),
                            options: [{
                                value: 'D',
                                label: get_cbuilder_msg("pbuilder.label.day")
                            },{
                                value: 'h',
                                label: get_cbuilder_msg("pbuilder.label.hour")
                            },{
                                value: 'm',
                                label: get_cbuilder_msg("pbuilder.label.minute")
                            },{
                                value: 's',
                                label: get_cbuilder_msg("pbuilder.label.second")
                            },{
                                value: 'd',
                                label: get_cbuilder_msg("pbuilder.label.dateFormat")
                            },{
                                value: 't',
                                label: get_cbuilder_msg("pbuilder.label.dateTimeFormat")
                            }]
                        },{
                            key: 'deadlineLimit',
                            label: get_cbuilder_msg("pbuilder.label.deadlineLimit")
                        },{
                            key: 'exceptionName',
                            label: get_cbuilder_msg("pbuilder.label.exceptionName")
                        }]
                    }]
                }];
                if (elementObj.properties.join !== "") {
                    options[0].properties.push({
                        name: 'join',
                        label: get_cbuilder_msg("pbuilder.label.joinType"),
                        type: "selectbox",
                        options: [{
                            value: 'AND',
                            label: get_cbuilder_msg("pbuilder.label.and")
                        },{
                            value: 'XOR',
                            label: get_cbuilder_msg("pbuilder.label.xor")
                        }]
                    });
                }
                if (elementObj.properties.split !== "") {
                    options[0].properties.push({
                        name: 'split',
                        label: get_cbuilder_msg("pbuilder.label.splitType"),
                        type: "selectbox",
                        options: [{
                            value: 'AND',
                            label: get_cbuilder_msg("pbuilder.label.and")
                        },{
                            value: 'XOR',
                            label: get_cbuilder_msg("pbuilder.label.xor")
                        }]
                    });
                }
                return options;
            }
        }});
    
        //Start
        CustomBuilder.initPaletteElement("", "start", get_cbuilder_msg('pbuilder.label.start'), '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="50" height="50" viewBox="0 0 50 50"><rect fill="none" height="100" stroke="none" stroke-width="0" width="100" x="0" y="0" transform="translate(-0.5 -0.5)"/><rect fill="#90ee90" height="42" rx="25" width="42" x="0" y="0" stroke-width="0.5" stroke="#333333" transform="translate(3.5 3.5)"/></svg>', [] , "", true, "", {builderTemplate: {
            'dragHtml' : '<div class="node start"></div>',
            'draggable' : true,
            'movable' : false,
            'deletable' : true,
            'copyable' : true,
            'navigable' : false,
            'supportProperties' : false,
            'absolutePosition' : true,
            'parentContainerAttr' : 'activities',
            'parentDataHolder' : 'activities',
            'render' : ProcessBuilder.renderActivity,
            'dragging' : ProcessBuilder.dragActivity,
            'unload' : ProcessBuilder.unloadActivity,
            'nodeDetailContainerColorNumber' : function() {
                return 7;
            },
            'getStylePropertiesDefinition' : ProcessBuilder.getStartDef
        }});
    
        //End
        CustomBuilder.initPaletteElement("", "end", get_cbuilder_msg('pbuilder.label.end'), '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="50" height="50" viewBox="0 0 50 50"><rect fill="none" height="100" stroke="none" stroke-width="0" width="100" x="0" y="0" transform="translate(-0.5 -0.5)"/><rect fill="#fefefe" height="42" rx="25" width="42" x="0" y="0" stroke-width="0.5" stroke="#ff4500" transform="translate(3.5 3.5)"/><rect height="34" rx="25" width="34" x="0" y="0" stroke-width="0.5" stroke="#ff4500" fill="#ff4500" transform="translate(7.5 7.5)"/></svg>', [] , "", true, "", {builderTemplate: {
            'dragHtml' : '<div class="node end"></div>',
            'draggable' : true,
            'movable' : false,
            'deletable' : true,
            'copyable' : true,
            'navigable' : false,
            'supportProperties' : false,
            'absolutePosition' : true,
            'supportProperties' : false,            
            'supportStyle' : false,
            'parentContainerAttr' : 'activities',
            'parentDataHolder' : 'activities',
            'render' : ProcessBuilder.renderActivity,
            'dragging' : ProcessBuilder.dragActivity,
            'unload' : ProcessBuilder.unloadActivity,
            'nodeDetailContainerColorNumber' : function() {
                return 8;
            }
        }});
    
        //Transition
        CustomBuilder.initPaletteElement("", "transition", get_cbuilder_msg('pbuilder.label.transition'), '<i class="las la-arrow-right"></i>', 
            [{
                title: get_cbuilder_msg("pbuilder.label.transitionProperties"),
                helplink : get_cbuilder_msg("pbuilder.label.transitionProperties.helplink"),
                properties: [{
                    name: 'label',
                    label: get_cbuilder_msg("pbuilder.label"),
                    type: 'textfield',
                    required: 'False',
                    value: get_cbuilder_msg("pbuilder.label.transition")
                },{
                    name: 'style',
                    label: get_cbuilder_msg("pbuilder.label.style"),
                    type: 'radio',
                    options: [{
                        value: 'straight',
                        label: get_cbuilder_msg("pbuilder.label.straight")
                    },{
                        value: 'orthogonal',
                        label: get_cbuilder_msg("pbuilder.label.orthogonal")
                    }],
                    value: 'straight'
                },{
                    name: 'type',
                    label: get_cbuilder_msg("cbuilder.type"),
                    type: 'selectbox',
                    options: [{
                        value: '',
                        label: get_cbuilder_msg("pbuilder.label.normal")
                    },{
                        value: 'CONDITION',
                        label: get_cbuilder_msg("pbuilder.label.condition")
                    },{
                        value: 'OTHERWISE',
                        label: get_cbuilder_msg("pbuilder.label.otherwise")
                    },{
                        value: 'EXCEPTION',
                        label: get_cbuilder_msg("pbuilder.label.exception")
                    }],
                    value: ''
                },{
                    name: 'conditionHelper',
                    label: get_cbuilder_msg("pbuilder.label.conditionHelper"),
                    type: 'selectbox',
                    options: [{
                        value: '',
                        label: get_cbuilder_msg("pbuilder.label.no")
                    },{
                        value: 'yes',
                        label: get_cbuilder_msg("pbuilder.label.yes")
                    }],
                    value: (this.condition && this.condition !== '') ? '' : 'yes',
                    control_field: 'type',
                    control_value: 'CONDITION',
                    control_use_regex: 'false'
                },{
                    name: 'conditions',
                    label: get_cbuilder_msg("pbuilder.label.conditions"),
                    type: 'grid',
                    columns : [{
                        key : 'join',
                        label : get_cbuilder_msg("pbuilder.label.join"),
                        options : [{
                            value : '&&',
                            label : get_cbuilder_msg("pbuilder.label.and")
                        },
                        {
                            value : '||',
                            label : get_cbuilder_msg("pbuilder.label.or")
                        }]
                    },
                    {
                        key : 'variable',
                        label : get_cbuilder_msg("pbuilder.label.variable"),
                        options_callback : "ProcessBuilder.getWorkflowVariablesOptions"
                    },
                    {
                        key : 'operator',
                        label : get_cbuilder_msg("pbuilder.label.operation"),
                        options : [{
                            value : '===',
                            label : get_cbuilder_msg("pbuilder.label.equalTo")
                        },
                        {
                            value : '!==',
                            label : get_cbuilder_msg("pbuilder.label.notEqualTo")
                        },
                        {
                            value : '>',
                            label : get_cbuilder_msg("pbuilder.label.greaterThan")
                        },
                        {
                            value : '>=',
                            label : get_cbuilder_msg("pbuilder.label.greaterThanOrEqualTo")
                        },
                        {
                            value : '<',
                            label : get_cbuilder_msg("pbuilder.label.lessThan")
                        },
                        {
                            value : '<=',
                            label : get_cbuilder_msg("pbuilder.label.lessThanOrEqualTo")
                        },
                        {
                            value : '=== \'true\'',
                            label : get_cbuilder_msg("pbuilder.label.isTrue")
                        },
                        {
                            value : '=== \'false\'',
                            label : get_cbuilder_msg("pbuilder.label.isFalse")
                        },
                        {
                            value : '(',
                            label : get_cbuilder_msg("pbuilder.label.openParenthesis")
                        },
                        {
                            value : ')',
                            label : get_cbuilder_msg("pbuilder.label.closeParenthesis")
                        }]
                    },
                    {
                        key : 'value',
                        label : get_cbuilder_msg("pbuilder.label.value")
                    }],
                    required: 'True',
                    js_validation: "ProcessBuilder.validateConditions", 
                    control_field: 'conditionHelper',
                    control_value: 'yes',
                    control_use_regex: 'false',
                    value: ''
                },{
                    name: 'condition',
                    label: get_cbuilder_msg("pbuilder.label.condition"),
                    type: 'textarea',
                    required: 'True',
                    js_validation: "ProcessBuilder.validateConditions",
                    control_field: 'conditionHelper',
                    control_value: '',
                    control_use_regex: 'false',
                    value: ''
                },{
                    name: 'exceptionName',
                    label: get_cbuilder_msg("pbuilder.label.exceptionName"),
                    type: 'textfield',
                    required: 'True',
                    control_field: 'type',
                    control_value: 'EXCEPTION',
                    control_use_regex: 'false',
                    value: ''
                }]
            }]
        , "", false, "", {builderTemplate: {
            'draggable' : false,
            'movable' : false,
            'deletable' : true,
            'copyable' : false,
            'navigable' : false,
            'supportProperties' : true,
            'supportStyle' : false,
            'parentDataHolder' : 'transitions',
            'render' : ProcessBuilder.renderTransition,
            'unload' : ProcessBuilder.unloadTransition,
            'customTreeMenu' : ProcessBuilder.renderTransitionTreeMenu,
            'nodeDetailContainerColorNumber' : function() {
                return 9;
            }
        }});
    },
    
    /*
     * Render the process
     */
    renderProcess : function(element, elementObj, component, callback) {
        var self = CustomBuilder.Builder;
        
        if (element.hasClass("process")) {
            var id = window.location.hash.replace("#", "");
            
            $('#process-selector select [value="'+id+'"]').text(elementObj.properties.label);
            if (id !== elementObj.properties.id) {
                $('#process-selector select [value="'+id+'"]').attr("value", elementObj.properties.id);
                $('#process-selector select').val(elementObj.properties.id);
                $('#process-selector select').trigger("chosen:updated");
                element.attr("id", "process_" + elementObj.properties.id);
                
                $(window).off('hashchange');
                window.location.hash = elementObj.properties.id;
                setTimeout(function(){
                    $(window).on('hashchange', ProcessBuilder.viewProcess);
                }, 10);
                
                //update info when process id changed.
                ProcessBuilder.updateAdvancedView();
            } 
            $('#process-selector select').trigger("chosen:updated");
            callback(element);
        } else {
            ProcessBuilder.jsPlumb.unbind("connection");
            ProcessBuilder.jsPlumb.unbind("connectionDetached");
            ProcessBuilder.jsPlumb.unbind();
            ProcessBuilder.jsPlumb.detachEveryConnection();
            ProcessBuilder.jsPlumb.deleteEveryEndpoint();
            ProcessBuilder.jsPlumb.unmakeEverySource();
            ProcessBuilder.jsPlumb.unmakeEveryTarget();
            ProcessBuilder.jsPlumb.reset();
            ProcessBuilder.initJsPlumb();
            
            element.addClass("process");
            element.attr("id", "process_" + elementObj.properties.id);
            element.html("");
            element.attr("data-cbuilder-uneditable", "").attr("data-cbuilder-participants", "");

            if (ProcessBuilder.jsPlumb.setContainer) { // for jsPlumb 1.6.2 onwards
                ProcessBuilder.jsPlumb.setContainer(element);
            }
             
            var deferreds = [];
            var dummy = $.Deferred();
            deferreds.push(dummy);

            //render participants
            for (var i in elementObj.participants) {
                var childComponent = self.parseDataToComponent(elementObj.participants[i]);
                var temp = $('<div></div>');
                $(element).append(temp);
                self.renderElement(elementObj.participants[i], temp, childComponent, false, deferreds);
            }

            dummy.resolve();

            $.when.apply($, deferreds).then(function() {
                ProcessBuilder.jsPlumb.setSuspendDrawing(true); //stop the rendering as it having slow performance when rendering large data
                //
                //render transitions
                for (var i in elementObj.transitions) {
                    var childComponent = self.parseDataToComponent(elementObj.transitions[i]);
                    var temp = $('<div></div>');
                    $(element).append(temp);
                    self.renderElement(elementObj.transitions[i], temp, childComponent, false, [""]); //add a dummy deferreds as no need it, and to stop it trigger change event
                }
                
                //resume and refresh the transition rendering here, else it block everything for very long time.
                ProcessBuilder.jsPlumb.setSuspendDrawing(false, true); 
                
                // bind event handling to new or moved connections
                ProcessBuilder.jsPlumb.bind("connection", function(info) {
                    var connection = info.connection;
                    ProcessBuilder.addConnection(connection);
                });

                // bind event handling to detached connections
                ProcessBuilder.jsPlumb.bind("connectionDetached", function(info) {
                    var connection = info.connection;
                    if ($(connection.target).attr("id").indexOf("jsPlumb") >= 0) {
                        ProcessBuilder.showConnectionDialog(connection);
                    } else {
                        ProcessBuilder.removeConnection(connection);
                    }
                });
                
                callback(element);
            });
        }
    },
    
    //Render the participant
    renderParticipant : function(element, elementObj, component, callback) {
        element.html('<div class="participant_inner"><div class="participant_handle"><div class="participant_label">'+elementObj.properties.label+'</div></div><div class="activities-container" data-cbuilder-activities></div></div>');
        element.addClass("participant");
        element.attr("id", "participant_" + elementObj.properties.id);
        
        callback(element);
    },
    
    //Render activity, tool, subflow, route, start & end
    renderActivity : function(element, elementObj, component, callback) {
        if (elementObj.className !== "end") {
            element.html('<div class="endleft endpoint"></div><div class="endtop endpoint"></div><div class="endright endpoint"></div><div class="endbottom endpoint"></div>');
        } else {
            element.html('<span></span>');
        }
        var label = "";
        
        if (elementObj.properties.label !== undefined) {
            label += elementObj.properties.label;
        }
        
        if (elementObj.className === "route") {
            if (elementObj.properties.join === 'AND' || elementObj.properties.split === 'AND') {
                label += "<div class='node_route_icon'>+</div>";
            }
        }
        
        if (label !== "") {
            element.append('<div class="node_label">'+label+'</div>');
        }
        
        if (elementObj.properties.limit !== undefined && elementObj.properties.limit !== null && elementObj.properties.limit !== "") {
            element.append("<div class='node_limit'>" + elementObj.properties.limit + ProcessBuilder.currentProcessData.properties.durationUnit.toLowerCase() + "</div>");
        }
        
        if ((elementObj.className === "activity" || elementObj.className === "start") 
                && elementObj.properties !== undefined && elementObj.properties.mapping_act_formId !== undefined
                && elementObj.properties.mapping_act_formId !== "") {
            element.append('<div class="node_mapping"><i class="fas fa-file-alt" style="color:#3f84f4;"></i></div>');
            element.find('.node_mapping').attr('title', ProcessBuilder.availableForms[elementObj.properties.mapping_act_formId]);
        } else if (elementObj.className === "route" && elementObj.properties !== undefined
                && elementObj.properties.mapping_act_plugin !== undefined && elementObj.properties.mapping_act_plugin.className !== undefined
                 && elementObj.properties.mapping_act_plugin.className !== "") {
            var plugin = ProcessBuilder.availableDecisionPlugin[elementObj.properties.mapping_act_plugin.className];
            if (plugin === undefined) {
                element.append('<div class="node_mapping"><i class="las la-exclamation-triangle" style="color:red;"></i></div>');
                element.find('.node_mapping').attr('title', elementObj.properties.mapping_act_plugin.className + " (" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")");
            } else {
                var icon = plugin.icon;
                if (icon === undefined || icon === "") {
                    icon = '<i class="las la-cog"></i>';
                }
                element.append('<div class="node_mapping" style="color:#394249;">'+icon+'</div>');
                element.find('.node_mapping').attr('title', plugin.label);
            }
        } else if (elementObj.className === "tool" && elementObj.properties !== undefined
                && elementObj.properties.tools !== undefined && elementObj.properties.tools.length > 0) {
            var icon = "";
            var label = "";
            for (var i in elementObj.properties.tools) {
                if (label !== "") {
                    label += "\n";
                }
                if (elementObj.properties.tools.length > 1) {
                    label += (parseInt(i) + 1) + ". ";
                }
                var p = elementObj.properties.tools[i];
                var plugin = ProcessBuilder.availableTools[p.className];
                if (plugin === undefined) {
                    icon = '<i class="las la-exclamation-triangle" style="color:red;"></i>';
                    label += p.className + " (" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")";
                } else {
                    if (icon === "" && plugin.icon !== undefined && plugin.icon !== "") {
                        icon = plugin.icon;
                    }
                    label += plugin.label;
                }
            }
            if (icon === undefined || icon === "") {
                icon = '<i class="las la-cog"></i>';
            }
            if (elementObj.properties.tools.length > 1) {
                icon += " " + elementObj.properties.tools.length;
            }
            element.append('<div class="node_mapping" style="color:#394249;">'+icon+'</div>');
            element.find('.node_mapping').attr('title', label);
        }
        
        element.addClass("node " + elementObj.className);
        element.attr("id", elementObj.properties.id);
        element.attr("data-cbuilder-visible", "");
        
        if (elementObj.className !== "end") {
            ProcessBuilder.jsPlumb.makeSource($(element), {
                filter: ".endpoint",
                anchor: "Continuous",
                connectorOverlays: [
                    ["Label", {
                        label: "",
                        cssClass: "transition_label"
                    }]
                ],
                endpoint: ["Dot", {radius: 4, hoverClass: 'endpoint_hover'}],
                paintStyle: {fillStyle: "#EBEBEB"},
                isSource: true,
                isTarget: true,
                maxConnections: 20,
                onMaxConnections: function(info, e) {
                    alert(get_cbuilder_msg("pbuilder.label.maximumConnectionsReached") + ": " + info.maxConnections);
                },
                dragOptions: {
                    start: function() {
                    }
                }
            });
        }
        if (elementObj.className !== "start") {
            ProcessBuilder.jsPlumb.makeTarget($(element), {
                dropOptions: {
                    hoverClass: "activity_hover"
                },
                anchor: "Continuous",
                endpoint: ["Dot", {radius: 4, hoverClass: 'endpoint_hover'}],
                isSource: true,
                isTarget: true,
                paintStyle: {fillStyle: "#EBEBEB"}
            });
        }
        
        callback(element);
    },
    
    //Render transition
    renderTransition : function(element, elementObj, component, callback) {
        var self = CustomBuilder.Builder;
        
        var label = elementObj.properties.label;
        var color = "#999";
        if (elementObj.properties.type === 'CONDITION') {
            if (label !== "") {
                label += "<br/>";
            }
            label += (elementObj.properties.condition) ? elementObj.properties.condition : "";
            color = "#80A2DB";
        } else if (elementObj.properties.type === 'OTHERWISE') {
            if (label !== "") {
                label += "<br/>";
            }
            label += "[otherwise]";
            color = "#D19D00";
        } else if (elementObj.properties.type === 'EXCEPTION') {
            if (label !== "") {
                label += "<br/>";
            }
            label += "[exception] " + (elementObj.properties.exceptionName) ? elementObj.properties.exceptionName : "";
            color = "#E37F96";
        } else if (elementObj.properties.type === 'DEFAULTEXCEPTION') {
            if (label !== "") {
                label += "<br/>";
            }
            label += "[defaultexception]";
            color = "#E37F96";
        } else if (elementObj.properties.type === 'startend') {
            color = "#000";
        }
        
        var transitionId = elementObj.properties.id;
        if (label === undefined) {
            label = "";
        }
        
        var connector = (elementObj.properties.style === 'orthogonal') ?
                ["Flowchart", {cornerRadius: 5, gap: 0}] :
                ["StateMachine", {curviness:0.1}];
        
        if (!$(element).is('div')) {
            var connection = elementObj.connection;
            
            connection.setPaintStyle({strokeStyle: color, lineWidth: 1, outlineWidth: 15, outlineColor: 'transparent'});
            connection.setHoverPaintStyle({lineWidth: 4}),
            connection.setConnector(connector);
            connection.removeOverlay(transitionId+"-label");
            connection.addOverlay([ 
                "Arrow", {
                    location: 0.99,
                    id: "arrow",
                    length: 10,
                    width: 10,
                    foldback: 0.8
                }
            ]); 
            connection.addOverlay([ 
                "Label", {
                    label: label, cssClass: "transition_label", id : transitionId+"-label"
                }
            ]); 
            $(connection._jsPlumb.overlays[1].canvas).attr("data-cbuilder-ignore-dragging", "");
            
            element = $(connection.canvas);
            
            $(connection.canvas).addClass("transition").attr("id", transitionId).attr("data-cbuilder-ignore-dragging", "");
            
            //add group select
            for (var e in connection.endpoints) {
                $(connection.endpoints[e].canvas).attr("data-cbuilder-group", transitionId).attr("data-cbuilder-ignore-dragging", "");
            }

            //to make the transition label not draggable
            for (var e in connection._jsPlumb.overlays) {
                $(connection._jsPlumb.overlays[e].canvas).attr("data-cbuilder-ignore-dragging", "");
            }
        } else {
            var connection = ProcessBuilder.jsPlumb.connect({
                source: self.frameBody.find("#" + elementObj.properties.from),
                target: self.frameBody.find("#" + elementObj.properties.to),
                connector: connector,
                paintStyle: {strokeStyle: color, lineWidth: 1, outlineWidth: 15, outlineColor: 'transparent'},
                endpointStyle:{ fillStyle: "#EBEBEB" },
                overlays: [
                    ["Label", {label: label, cssClass: "transition_label", id : transitionId+"-label"}]
                ]
            });

            element.remove();
            element = $(connection.canvas);
            $(connection.canvas).addClass("transition").attr("id", transitionId).attr("data-cbuilder-ignore-dragging", "");

            if (elementObj.properties.type === 'startend') {
                $(connection.canvas).attr("data-cbuilder-uneditable", "");
            }

            //add group select
            for (var e in connection.endpoints) {
                $(connection.endpoints[e].canvas).attr("data-cbuilder-group", transitionId).attr("data-cbuilder-ignore-dragging", "");
            }
            
            //to make the transition label not draggable
            for (var e in connection._jsPlumb.overlays) {
                $(connection._jsPlumb.overlays[e].canvas).attr("data-cbuilder-ignore-dragging", "");
            }
            
            elementObj.connection = connection;
        }
        
        callback(element);
    },
    
    //To redraw the connections when dragging participant
    dragParticipant : function(dragElement, component) {
        var self = CustomBuilder.Builder;
        ProcessBuilder.jsPlumb.setSuspendDrawing(true);
        ProcessBuilder.jsPlumb.recalculateOffsets(self.frameBody.find(".process"));
        ProcessBuilder.jsPlumb.setSuspendDrawing(false, true);
        return dragElement;
    },
    
    //To reposition the connections after moved participant
    dropParticipantEnd : function(dragElement) {
        ProcessBuilder.refreshConnections();
        return dragElement;
    },
    
    //To redraw the connection when dragging node (activity, tool, subflow, route, start & end)
    dragActivity : function(dragElement, component) {
        ProcessBuilder.jsPlumb.repaint(dragElement);
        return dragElement;
    },
    
    //To remove related transitions when participant deleted
    unloadParticipant : function(element, elementObj, component) {
        $(element).find(".node").each(function(){
            ProcessBuilder.removeNode($(this));
        });
    },
    
    //To remove related transitions when node (activity, tool, subflow, route, start & end) deleted
    unloadActivity : function(element, elementObj, component) {
        ProcessBuilder.removeNode($(element));
    },
    
    //to remove transition                
    unloadTransition : function(element, elementObj, component) {
        ProcessBuilder.jsPlumb.detach(elementObj.connection);
    },
    
    //Handling for node deleted
    removeNode : function (node) {
        var data = $(node).data("data");
        if (data.xpdlObj !== undefined) {
            //get process xpdl obj
            var xpdlProcess = ProcessBuilder.currentProcessData.xpdlObj;
            if (data.className !== "start" && data.className !== "end") {
                var xpdlActivities = ProcessBuilder.getArray(xpdlProcess['Activities'], 'Activity');
            
                var index = $.inArray(data.xpdlObj, xpdlActivities);
                if (index !== -1) {
                    xpdlActivities.splice(index, 1);
                }
                ProcessBuilder.setArray(xpdlProcess, 'Activities', 'Activity', xpdlActivities);
            } else {
                var xpdlProcessesAttrs = ProcessBuilder.getArray(xpdlProcess['ExtendedAttributes'], 'ExtendedAttribute');
                
                var index = $.inArray(data.xpdlObj, xpdlProcessesAttrs);
                if (index !== -1) {
                    xpdlProcessesAttrs.splice(index, 1);
                }
                ProcessBuilder.setArray(xpdlProcess, 'ExtendedAttributes', 'ExtendedAttribute', xpdlProcessesAttrs);
            }
            if (data.mapping !== undefined) {
                delete CustomBuilder.data['activityPlugins'][ProcessBuilder.currentProcessData.properties.id + "::" + data.properties.id];
            }
            if (data.formMapping !== undefined) {
                delete CustomBuilder.data['activityForms'][ProcessBuilder.currentProcessData.properties.id + "::" + data.properties.id];
            }
        }
        
        var connSet = ProcessBuilder.jsPlumb.getConnections({source: $(node)});
        for (var c in connSet) {
            ProcessBuilder.removeConnection(connSet[c]);
        }
        
        connSet = ProcessBuilder.jsPlumb.getConnections({target: $(node)});
        for (var c in connSet) {
            ProcessBuilder.removeConnection(connSet[c]);
        }
        
        // remove connections
        ProcessBuilder.jsPlumb.detachAllConnections($(node));
        // remove element
        ProcessBuilder.jsPlumb.remove($(node));
    },
    
    //Handling for a connection event triggered 
    addConnection : function(connection) {
        var self = CustomBuilder.Builder;
        
        if (self.frameBody.find(".process").data("data") !== undefined) {
            var source = connection.source;
            var target = connection.target;

            // update split & join
            var sourceConnSet = ProcessBuilder.jsPlumb.getConnections({source: $(source)});
            var sourceData = $(source).data("data");
            if (sourceConnSet.length > 1) {
                if (sourceData.properties.split === "") {
                    sourceData.properties.split = "XOR";
                }
            } else {
                sourceData.properties.split = "";
            }
            var targetConnSet = ProcessBuilder.jsPlumb.getConnections({target: $(target)});
            var targetData = $(target).data("data");
            if (targetConnSet.length > 1) {
                if (targetData.properties.join === "") {
                    targetData.properties.join = "XOR";
                }
            } else {
                targetData.properties.join = "";
            }

            var data = $(connection.canvas).data("data");
            if (data === undefined) {
                // new connection
                var parentDataArray = self.frameBody.find(".process").data("data")['transitions'];
                data = {
                    className :'transition',
                    properties : {
                        name : "",
                        type : "",
                        style : "straight"
                    }
                };

                self.updateElementId(data);
                parentDataArray.push(data);

                $(connection.canvas).addClass("transition").attr("id", data.properties.id).attr("data-cbuilder-ignore-dragging", "");

                //add group select
                for (var e in connection.endpoints) {
                    $(connection.endpoints[e].canvas).attr("data-cbuilder-group", data.properties.id).attr("data-cbuilder-ignore-dragging", "");
                }
                
                if (connection._jsPlumb.overlays[1] !==undefined) {
                    $(connection._jsPlumb.overlays[1].canvas).attr("data-cbuilder-ignore-dragging", "");
                }

                $(connection.canvas).attr("data-cbuilder-classname", 'transition');
                $(connection.canvas).attr("data-cbuilder-id", data.properties.id);
                $(connection.canvas).data("data", data);
                
                data.connection = connection;
            }

            data.properties.from = sourceData.properties.id;
            data.properties.to = targetData.properties.id;

            if (sourceData.className === "start" || targetData.className === "end") {
                data.properties.type = "startend";
                $(connection.canvas).attr("data-cbuilder-uneditable", "");

                connection.setPaintStyle({
                    strokeStyle: "#000", lineWidth: 1, outlineWidth: 15, outlineColor: 'transparent'
                });   
            } else {
                if (data.properties.type === "startend") {
                    data.properties.type = "";
                    $(connection.canvas).removeAttr("data-cbuilder-uneditable", "");

                    connection.setPaintStyle({
                        strokeStyle: "#999", lineWidth: 1, outlineWidth: 15, outlineColor: 'transparent'
                    }); 
                }
            }
            
            // remove unused endpoints
            var endpoints = ProcessBuilder.jsPlumb.getEndpoints($(source));
            if (endpoints.length > 0) {
                for (var i=0; i<endpoints.length; i++) {
                    if (endpoints[i].connections.length === 0) {
                        ProcessBuilder.jsPlumb.deleteEndpoint(endpoints[i]);
                    }
                }
            }
            endpoints = ProcessBuilder.jsPlumb.getEndpoints($(target));
            if (endpoints.length > 0) {
                for (var i=0; i<endpoints.length; i++) {
                    if (endpoints[i].connections.length === 0) {
                        ProcessBuilder.jsPlumb.deleteEndpoint(endpoints[i]);
                    }
                }
            }

            CustomBuilder.update();
            self._updateBoxes();
        }
    },
    
    //Handling for a remove connection event triggered 
    removeConnection : function(connection) {
        var self = CustomBuilder.Builder;
        var parentDataArray = self.frameBody.find(".process").data("data")['transitions'];
        
        var data = $(connection.canvas).data("data");
        
        if (data.xpdlObj !== undefined) {
            //get process xpdl obj
            var xpdlProcess = ProcessBuilder.currentProcessData.xpdlObj;
            var xpdlTransitions = ProcessBuilder.getArray(xpdlProcess['Transitions'], 'Transition');

            var index = $.inArray(data.xpdlObj, xpdlTransitions);
            if (index !== -1) {
                xpdlTransitions.splice(index, 1);
            }
            ProcessBuilder.setArray(xpdlProcess, 'Transitions', 'Transition', xpdlTransitions);
        }
        
        var index = $.inArray(data, parentDataArray);
        if (index !== -1) {
            parentDataArray.splice(index, 1);
        }
        
        var source = connection.source;
        var target = connection.target;
        
        // update split & join
        var sourceConnSet = ProcessBuilder.jsPlumb.getConnections({source: $(source)});
        var sourceData = $(source).data("data");
        if (sourceConnSet.length > 1) {
            if (sourceData.properties.split === "") {
                sourceData.properties.split = "XOR";
            }
        } else {
            sourceData.properties.split = "";
        }
        var targetConnSet = ProcessBuilder.jsPlumb.getConnections({target: $(target)});
        var targetData = $(target).data("data");
        if (targetConnSet.length > 1) {
            if (targetData.properties.join === "") {
                targetData.properties.join = "XOR";
            }
        } else {
            targetData.properties.join = "";
        }
        
        // remove unused endpoints
        var endpoints = ProcessBuilder.jsPlumb.getEndpoints($(source));
        if (endpoints !== undefined && endpoints.length > 0) {
            for (var i=0; i<endpoints.length; i++) {
                if (endpoints[i].connections.length === 0) {
                    ProcessBuilder.jsPlumb.deleteEndpoint(endpoints[i]);
                }
            }
        }
        endpoints = ProcessBuilder.jsPlumb.getEndpoints($(target));
        if (endpoints !== undefined && endpoints.length > 0) {
            for (var i=0; i<endpoints.length; i++) {
                if (endpoints[i].connections.length === 0) {
                    ProcessBuilder.jsPlumb.deleteEndpoint(endpoints[i]);
                }
            }
        }
        
        CustomBuilder.update();
        self._updateBoxes();
    },
      
    /*
     * Show a dialog to choose to add connection node
     */                
    showConnectionDialog : function(connection){
        var self = CustomBuilder.Builder;
        var source = $(connection.source);
        var target = $(connection.target);
        var viewportTop = self.frameDoc.scrollTop();
        var viewportLeft = self.frameDoc.scrollLeft();
        
        self.frameBody.find("#node_dialog").remove();
        
        var box = target.offset();
        
        var offsetLeft = box.left + viewportLeft;
        var offsetTop = box.top + viewportTop;
        
        var swimlane;
        // determine swimlane
        self.frameBody.find(".participant").each(function(index, participant) {
            var participantTop = $(participant).offset().top + viewportTop; 
            var participantHeight = $(participant).outerHeight() * self.zoom;
            if (offsetTop >= participantTop && offsetTop <= (participantTop + participantHeight)) {
                target = participant;
                swimlane = target;
                return false;
            }
        });
        if (!swimlane) {
            return false;
        }
        
        // display dialog to choose node type
        var $nodeDialog = $('<div id="node_dialog"><ul><li type="activity">' + get_cbuilder_msg("pbuilder.label.activity") + '</li><li type="tool">' + get_cbuilder_msg("pbuilder.label.tool") + '</li><li type="route">' + get_cbuilder_msg("pbuilder.label.route") + '</li><li type="subflow">' + get_cbuilder_msg("pbuilder.label.subflow") + '</li><li type="end">' + get_cbuilder_msg("pbuilder.label.end") + '</li><ul></div>');
        $nodeDialog.dialog({
            autoOpen: true,
            modal: true,
            width: 100,
            open: function(event, ui) {
                var iframeOffset = $("#iframe-wrapper").offset();
                var dialogTop = box.top - self.frameDoc.scrollTop() + iframeOffset.top - 85;
                var dialogLeft = box.left - self.frameDoc.scrollLeft() + iframeOffset.left - 50;
                $nodeDialog.parent().css("left", dialogLeft + "px");
                $nodeDialog.parent().css("top", dialogTop + "px");
                $("#node_dialog").parent().find(".ui-dialog-titlebar").remove();
                $("#node_dialog").parent().css("width", "100px");
                $("#node_dialog").parent().addClass("node_dialog_container");
                $('.ui-widget-overlay').off('click');
                $('.ui-widget-overlay').on('click',function(){
                    $nodeDialog.dialog("close");
                });
            }
        });
        if ($(source).hasClass("start") || $(source).hasClass("route")) {
            $nodeDialog.find("[type=end]").remove();
        }
        $("#node_dialog li").on("click", function() {
            $nodeDialog.dialog("close");
            var nodeType = $(this).attr("type");
            
            self.component = self.getComponent(nodeType);
            self.dragElement = $('<div></div>');
            $(swimlane).find('.activities-container').append(self.dragElement);
            
            var containerOffset = $(swimlane).offset();
            var x_offset = (offsetLeft - (containerOffset.left + viewportLeft)) * self.zoom;
            var y_offset = (offsetTop - (containerOffset.top + viewportTop)) * self.zoom;
            
            self.dragElement.css({
               "top" : y_offset + "px",
               "left" : x_offset + "px",
               "position" : "absolute"
            });
            
            self.addElement(function(){
                var connection = ProcessBuilder.jsPlumb.connect({
                    source: self.frameBody.find("#" + $(source).data("data").properties.id),
                    target: self.frameBody.find("#" + $(self.selectedEl).data("data").properties.id),
                    connector: ["StateMachine", {curviness:0.1}],
                    paintStyle: {strokeStyle: "#999", lineWidth: 1, outlineWidth: 15, outlineColor: 'transparent'},
                    endpointStyle:{ fillStyle: "#EBEBEB" }
                });

                ProcessBuilder.addConnection(connection);
            });
        });
    },
    
    /*
     * Move the transition under activity in tree viewer
     */
    renderTransitionTreeMenu : function(li, elementObj, component) {
        var sourceId = elementObj.properties.from;
        var sourceNodeConatiner = $(li).closest(".tree-container").find("[data-cbuilder-node-id='"+sourceId+"'] > ol");
        sourceNodeConatiner.append(li);
    },
    
    /*
     * On canvas changed, adjust the participants Size
     */
    adjustParticipantSize : function() {
        var self = CustomBuilder.Builder;
        var participantWidth = 0;
        self.frameBody.find(".participant").each(function(){
            var $participant = $(this);
            var bottomOffset = null;
            var rightOffset = 0;
            
            $participant.find(".activities-container").children().each(function (i, e) {
                var $e = $(e),
                eBottomOffset = ($e.offset().top / self.zoom) + $e.outerHeight(),
                eRightOffset = ($e.offset().left / self.zoom) + $e.outerWidth();
                
                if (eBottomOffset > bottomOffset) {
                    bottomOffset = eBottomOffset;
                }
                if (eRightOffset > rightOffset) {
                    rightOffset = eRightOffset;
                }
            });
            // recalculate participant height
            var childrenHeight = (bottomOffset - ($participant.offset().top / self.zoom)) + 50;
            $participant.find("> div").css("height", childrenHeight + "px");

            // recalculate participant width
            var childrenWidth = (rightOffset - ($participant.offset().left / self.zoom)) + 100;
            if (childrenWidth > participantWidth) {
                participantWidth = childrenWidth;
            }
        });
        self.frameBody.find(".process").css({"width" : participantWidth + "px", "min-width" : "100%"});
    },
    
    /*
     * udpate the id & name when element added
     */
    updateElementId : function(elementObj) {
        var self = CustomBuilder.Builder;
        var className = elementObj.className;
            
        if (elementObj.properties.id === undefined || elementObj.properties.id === "" || className === "participant") {
            var nodeCount = self.frameBody.find("."+className).length - 1;
            if (nodeCount < 0) {
                nodeCount = 0; //should always start with 0
            }
            var id;
            do {
                id = className + ++nodeCount;
            } while (self.frameBody.find("#"+id).length > 0);

            if (className === "participant") {
                id = ProcessBuilder.currentProcessData.properties.id + "_" + id; 
            }

            elementObj.properties.id = id;
        } else if (self.frameBody.find("#"+elementObj.properties.id).length > 0) {
            var nodeCount = self.frameBody.find("#"+elementObj.properties.id).length;
            
            var id = elementObj.properties.id;
            
            while (self.frameBody.find("#"+id).length > 0) {
                id = elementObj.properties.id + "_" + ++nodeCount;
            }
            elementObj.properties.id = id;
        }
        
        if ((className === "activity" || className === "tool" || className === "subflow" || className === "participant") 
                && (elementObj.properties.label === undefined || elementObj.properties.label === "")) {
            var componenet = self.getComponent(className);
            elementObj.properties.label = componenet.label + " " + nodeCount;
        }
        
        delete elementObj['xpdlObj'];
    },
    
    /*
     * triggered on canvas changed to update participants size and connection position
     */
    refresh : function() {
        var self = CustomBuilder.Builder;
        
        ProcessBuilder.adjustParticipantSize();
        ProcessBuilder.refreshConnections();
        setTimeout(function(){
            self._updateBoxes();
        }, 10);
    },
    
    /*
     * recalculate the connection position
     */
    refreshConnections : function() {
        var self = CustomBuilder.Builder;
        
        if (self.frameBody.find(".process").length > 0) {
            if (ProcessBuilder.refreshTimeout) {
                clearTimeout(ProcessBuilder.refreshTimeout);
            }
            
            ProcessBuilder.refreshTimeout = setTimeout(function(){
                ProcessBuilder.jsPlumb.setSuspendDrawing(true);
                ProcessBuilder.jsPlumb.recalculateOffsets(self.frameBody.find(".process"));
                ProcessBuilder.jsPlumb.setSuspendDrawing(false, true);
                
                ProcessBuilder.refreshTimeout = null;
            }, 30);
        }
    },
    
    /*
     * Action implementation for zoom minus icon
     */
    zoomMinus : function() {
        var self = CustomBuilder.Builder;
        self.setZoom("-");
        ProcessBuilder.jsPlumb.setZoom(self.zoom);
        ProcessBuilder.updateZoomLabel();
    },
    
    /*
     * Action implementation for zoom plus icon
     */
    zoomPlus : function() {
        var self = CustomBuilder.Builder;
        self.setZoom("+");
        ProcessBuilder.jsPlumb.setZoom(self.zoom);
        ProcessBuilder.updateZoomLabel();
    },
    
    /*
     * Update zoom label
     */
    updateZoomLabel: function() {
        var self = CustomBuilder.Builder;
        var strIn = self.zoom - 0.1;
        var strOut = self.zoom + 0.1;
        if (strIn < 0.4) {
            strIn = get_cbuilder_msg('pbuilder.label.disabled');
        } else {
            strIn = Math.round(strIn * 100) + "%";
        }
        if (strOut > 1.6) {
            strOut = get_cbuilder_msg('pbuilder.label.disabled');
        } else {
            strOut = Math.round(strOut * 100) + "%";
        }
        $("#zoom-minus").attr("title", get_cbuilder_msg('pbuilder.label.zoomOut') + " (" + strIn + ")");
        $("#zoom-plus").attr("title", get_cbuilder_msg('pbuilder.label.zoomIn') + " (" + strOut + ")");
    },       
     
    /*
     * validate before post to save
     */                
    beforeSaveValidation : function() {
        if (!ProcessBuilder.validate()) {
            CustomBuilder.showMessage(get_cbuilder_msg("pbuilder.label.designInvalid"), "danger");
            return false;
        }
        return true;
    },        
    
    /*
     * Validate the whole xpdl
     */
    validate : function() {
        var self = CustomBuilder.Builder;
        $('#process-selector select option').removeClass("invalidProcess");
        
        self.frameBody.find(".invalidNode").removeClass("invalidNode");
        self.frameBody.find(".invalidNodeMessage").remove();
        
        var valid = true;
        
        var xpdl = CustomBuilder.data.xpdl['Package'];
        var xpdlProcesses = ProcessBuilder.getArray(xpdl['WorkflowProcesses'], 'WorkflowProcess');
        for (var p in xpdlProcesses) {
            var xpdlProcess = xpdlProcesses[p];
            var validProcess = true;
            
            var xpdlActivities = ProcessBuilder.getArray(xpdlProcess['Activities'], 'Activity');
            var xpdlTransitions = ProcessBuilder.getArray(xpdlProcess['Transitions'], 'Transition');
            var xpdlProcessesAttrs = ProcessBuilder.getArray(xpdlProcess['ExtendedAttributes'], 'ExtendedAttribute');
            
            var starts = [], ends = [], fromTransition = {}, toTransition = {};
            for (var a in xpdlProcessesAttrs) {
                var attr = xpdlProcessesAttrs[a];
                if (attr['-Name'] === "JaWE_GRAPH_END_OF_WORKFLOW" || attr['-Name'] === "JaWE_GRAPH_START_OF_WORKFLOW") {
                    var actId = attr['-Value'].replace(/.*,CONNECTING_ACTIVITY_ID=([^,]+),.*/g, "$1");
                    if (attr['-Name'] === "JaWE_GRAPH_START_OF_WORKFLOW") {
                        starts.push(actId);
                    } else {
                        ends.push(actId);
                    }
                }
            }
            
            for (var t in xpdlTransitions) {
                var transition = xpdlTransitions[t];
                var from = transition['-From'];
                var to = transition['-To'];
                if (fromTransition[from] === undefined) {
                    fromTransition[from] = [transition];
                } else {
                    fromTransition[from].push(transition);
                }
                if (toTransition[to] === undefined) {
                    toTransition[to] = [transition];
                } else {
                    toTransition[to].push(transition);
                }
            }
            
            if (xpdlActivities !== null && xpdlActivities !== undefined && xpdlActivities.length > 0) {
                for (var a in xpdlActivities) {
                    var activityInvalid = false;
                    var deadlineInvalid = false;
                    var startInvalid = false;

                    var act = xpdlActivities[a];
                    var aid = act['-Id'];
                    if ((fromTransition[aid] === undefined && $.inArray(aid, ends) === -1)
                            || (toTransition[aid] === undefined && $.inArray(aid, starts) === -1)) {
                        activityInvalid = true;
                    }

                    if ($.inArray(aid, starts) !== -1 && toTransition[aid] !== undefined && toTransition[aid].length > 0) {
                        startInvalid = true;
                    }

                    var xpdlDeadlines = ProcessBuilder.getArray(act['Deadline']);
                    if (xpdlDeadlines.length > 0) {
                        if (fromTransition[aid] === undefined) {
                            deadlineInvalid = true;
                        } else {
                            var exceptionNames = [];
                            for (var t in fromTransition[aid]) {
                                var transition = fromTransition[aid][t];
                                if (transition['Condition'] !== undefined && transition['Condition']['-Type'] === "EXCEPTION") {
                                    exceptionNames.push(transition['Condition']['#text']);
                                }
                            }
                            for (var d in xpdlDeadlines) {
                                if ($.inArray(xpdlDeadlines[d]['ExceptionName'], exceptionNames) === -1) {
                                    deadlineInvalid = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (activityInvalid || deadlineInvalid || startInvalid) {
                        // only show in current process in canvas
                        if (ProcessBuilder.currentProcessData.properties.id === xpdlProcess['-Id']) {
                            var $node = self.frameBody.find("#"+ aid);
                            $node.addClass("invalidNode");
                            var messageTransition = get_cbuilder_msg("pbuilder.label.missingTransition");
                            var messageDeadline = get_cbuilder_msg("pbuilder.label.unhandleDeadline");
                            var messageStart = get_cbuilder_msg("pbuilder.label.invalidStart");
                            var message = "";
                            if (activityInvalid) {
                                message += '<p>' + messageTransition +'</p>';
                            }
                            if (deadlineInvalid) {
                                message += '<p>' + messageDeadline +'</p>';
                            }
                            if (startInvalid) {
                                message += '<p>' + messageStart +'</p>';
                            }
                            var $nodeMessage = $('<div class="invalidNodeMessage">' + message +'</div>');
                            $node.append($nodeMessage);
                        }
                        validProcess = false;
                    }
                }
            } else {
                validProcess = false;
            }
            
            if (starts.length === 0 || starts.length > 1) {
                validProcess = false;
            }
            
            if (!validProcess) {
                valid = false;
                $('#process-selector select option[value="'+xpdlProcess['-Id']+'"]').addClass("invalidProcess");
            }
            $('#process-selector select').trigger("chosen:updated");
        }
        
        return valid;
    },
    
    /*
     * Validation for duplicate id of process
     */
    validateProcessDuplicateId : function (name, value) {
        var self = CustomBuilder.Builder;
        var data = ProcessBuilder.currentProcessData;
        
        //find in the process list which is not a match
        var xpdl = CustomBuilder.data.xpdl['Package'];
        var xpdlProcesses = ProcessBuilder.getArray(xpdl['WorkflowProcesses'], 'WorkflowProcess');
        for (var p in xpdlProcesses) {
            var process = xpdlProcesses[p];
            if (process['-Id'] === value && (data.xpdlObj !== process)) {
                return get_cbuilder_msg("pbuilder.label.duplicateId");
            }
        }
        
        return null;
    },
    
    /*
     * Validation for duplicate id of participant
     */
    validateParticipantDuplicateId : function (name, value) {
        var self = CustomBuilder.Builder;
        var data = $(self.selectedEl).data("data");
        
        //find in the participant list which is not a match
        var xpdl = CustomBuilder.data.xpdl['Package'];
        var xpdlParticipants = ProcessBuilder.getArray(xpdl['Participants'], 'Participant');
        for (var p in xpdlParticipants) {
            var particpant = xpdlParticipants[p];
            if (particpant['-Id'] === value && (data.xpdlObj !== particpant)) {
                return get_cbuilder_msg("pbuilder.label.duplicateId");
            }
        }
        return null;
    },
    
    /*
     * Validation for duplicate id of activity node
     */
    validateDuplicateId : function (name, value) {
        var self = CustomBuilder.Builder;
        var found = self.frameBody.find("#"+value);
        if (found.length > 0 && !(found.length === 1 && found.is(self.selectedEl))) {
            return get_cbuilder_msg("pbuilder.label.duplicateId");
        }
        return null;
    },
    
    /*
     * Validation for process variables property
     */
    validateVariables : function (name, values) {
        try {
            var result = true;
            var regex = RegExp('^[$_a-zA-Z][$_a-zA-Z0-9]+$');
            
            if ($.isArray(values)) {
                for (var i=0; i<values.length; i++) {
                    if (!regex.test(values[i].variableId)) {
                        result = false;
                    }
                }
            }

            if (result) {
                return null;
            } else {
                return get_cbuilder_msg("pbuilder.label.invalidVariable");
            }
        } catch (err) {
            return get_cbuilder_msg("pbuilder.label.invalidVariable");
        };
        return null;
    },
    
    /*
     * Validation for transition conditions property
     */
    validateConditions : function (name, value) {
        try {
            var data = ProcessBuilder.currentProcessData;

            var executionStatement = "";

            //assign number as variable value for checking;
            for (var df=0; df<data.properties.dataFields.length; df++) {
                var dataField = data.properties.dataFields[df];

                executionStatement += "var " + dataField.variableId + " = \"0\";\n";
            }

            if ($.isArray(value)) {
                executionStatement += ProcessBuilder.buildConditions(value) + ";";
            } else {
                executionStatement += value + ";";
            }

            var result = eval(executionStatement);

            if (result === true || result === false) {
                return null;
            } else {
                return get_cbuilder_msg("pbuilder.label.invalidCondition");
            }
        } catch (err) {
            return get_cbuilder_msg("pbuilder.label.invalidCondition");
        };
        return null;
    },
    
    /*
     * Used to construct condition string from condition helper
     */
    buildConditions : function (values) {
        var conditions = "";

        for (var i=0; i<values.length; i++) {
            var value = values[i];
            if (conditions !== "" && conditions.substring(conditions.length-1) !== "(" && value.operator !== ")") {
                conditions += " " + value.join + " ";
            }
            if (value.operator === "(" || value.operator === ")") {
                conditions += value.operator;
            } else if ((value.operator === ">=" || value.operator === ">" || value.operator === "<=" || value.operator === "<")) {
                if (!isNaN(value.value)) {
                    conditions += "parseFloat("+value.variable+") " + value.operator + " parseFloat('" + value.value + "')";
                } else {
                    conditions += value.variable + " " + value.operator + " '" + value.value + "'";
                }
            } else if (value.operator === "=== 'true'" || value.operator === "=== 'false'") {
                conditions += value.variable + ".toLowerCase() " + value.operator;
            } else if (value.operator === "IN" || value.operator === "NOT IN") {
                conditions += "'" + value.value + "'.split(',').indexOf(" + value.variable + ") ";
                if (value.operator === "IN") {
                    conditions += "!== -1";
                } else {
                    conditions += "=== -1";
                }
            } else {
                conditions += value.variable + " " + value.operator + " '" + value.value + "'";
            }
        }
        return conditions;
    },
            
    setUrlVariables : function(elementObj) {
        if (PropertyEditor) {
            PropertyEditor.Util.setUrlVariables({
                "PROCESS_ID" : ProcessBuilder.currentProcessData.properties.id,
                "ACTIVITY_ID" : elementObj.properties.id,
                "processId" : ProcessBuilder.currentProcessData.properties.id,
                "actId" : elementObj.properties.id
            });
        }
    },        
    
    /*
     * Get the mapping properties options for process start whitelist
     */                
    getProcessStartWhiteListDef : function(elementObj, component) {
        ProcessBuilder.setUrlVariables(elementObj);
        
        var def = ProcessBuilder.getParticipantDef(elementObj, component);
        
        def[0].title = get_cbuilder_msg("pbuilder.label.processStartWhiteList");
        def[0].properties[0].options[0].label = get_cbuilder_msg("pbuilder.label.type.role");
        
        //remove variable and performer from options
        var i = def[0].properties[0].options.length;
        while (i--) {
            if (def[0].properties[0].options[i].value === "performer" || def[0].properties[0].options[i].value === "workflowVariable") { 
                def[0].properties[0].options.splice(i, 1);
            } 
        }
        
        def[0].properties.push({
            name: 'mapping_par_role',
            label: get_cbuilder_msg("pbuilder.label.type.role"),
            type : 'selectbox',
            options : [
                {value : "" , label : get_cbuilder_msg("pbuilder.label.type.role.everyone")},
                {value : "loggedInUser" , label : get_cbuilder_msg("pbuilder.label.loggedInUser")},
                {value : "adminUser" , label : get_cbuilder_msg("pbuilder.label.adminUser")}
            ],
            control_field: 'mapping_par_type',
            control_value: '',
            control_use_regex: 'false'
        });
        
        return def;
    },
    
    /*
     * Get the mapping properties options for participant
     */   
    getParticipantDef : function(elementObj, component) {
        ProcessBuilder.setUrlVariables(elementObj);
        
        var def = [
            {
                title: get_cbuilder_msg("pbuilder.label.configureMapping"),
                properties: [{
                    name: 'mapping_par_type',
                    label: get_cbuilder_msg("cbuilder.type"),
                    type : 'selectbox',
                    options : [
                        {value : "", label : ""},
                        {value : "user", label : get_cbuilder_msg("pbuilder.label.users")},
                        {value : "group", label : get_cbuilder_msg("pbuilder.label.groups")},
                        {value : "department", label : get_cbuilder_msg("pbuilder.label.department")},
                        {value : "hod", label : get_cbuilder_msg("pbuilder.label.hod")},
                        {value : "performer", label : get_cbuilder_msg("pbuilder.label.performer")},
                        {value : "workflowVariable", label : get_cbuilder_msg("pbuilder.label.workflowVariable")},
                        {value : "plugin", label : get_cbuilder_msg("pbuilder.label.plugin")}
                    ]
                },{
                    name : 'mapping_par_users',
                    label : get_cbuilder_msg("pbuilder.label.users"),
                    type : 'multiselect',
                    required : 'True',
                    options_ajax : CustomBuilder.contextPath + '/web/json/plugin/org.joget.apps.userview.lib.UserPermission/service?action=getUsers',
                    control_field: 'mapping_par_type',
                    control_value: 'user',
                    control_use_regex: 'false'
                },{
                    name : 'mapping_par_groups',
                    label : get_cbuilder_msg("pbuilder.label.groups"),
                    type : 'multiselect',
                    required : 'True',
                    options_ajax : CustomBuilder.contextPath + '/web/json/plugin/org.joget.apps.userview.lib.GroupPermission/service?action=getGroups',
                    control_field: 'mapping_par_type',
                    control_value: 'group',
                    control_use_regex: 'false'
                },{
                    name : 'mapping_par_department',
                    label : get_cbuilder_msg("pbuilder.label.department"),
                    type : 'selectbox',
                    required : 'True',
                    options_ajax : CustomBuilder.contextPath + '/web/json/plugin/org.joget.apps.userview.lib.DepartmentPermission/service?action=getDepts',
                    control_field: 'mapping_par_type',
                    control_value: 'department|hod',
                    control_use_regex: 'true'
                },{
                    name : 'mapping_par_performer_type',
                    label : get_cbuilder_msg("pbuilder.label.performerType"),
                    type : 'selectbox',
                    required : 'True',
                    options : [
                        {value : "requester" , label : get_cbuilder_msg("pbuilder.label.performerType.requester")},
                        {value : "requesterHod" , label : get_cbuilder_msg("pbuilder.label.performerType.requesterHod")},
                        {value : "requesterHodIgnoreReportTo" , label : get_cbuilder_msg("pbuilder.label.performerType.requesterHodIgnoreReportTo")},
                        {value : "requesterSubordinates" , label : get_cbuilder_msg("pbuilder.label.performerType.requesterSubordinates")},
                        {value : "requesterDepartment" , label : get_cbuilder_msg("pbuilder.label.performerType.requesterDepartment")}
                    ],
                    control_field: 'mapping_par_type',
                    control_value: 'performer',
                    control_use_regex: 'false'
                },{
                    name : 'mapping_par_performer_act',
                    label : get_cbuilder_msg("pbuilder.label.performerActivity"),
                    type : 'selectbox',
                    options_callback : "ProcessBuilder.getActivitiesOptions",
                    control_field: 'mapping_par_type',
                    control_value: 'performer',
                    control_use_regex: 'false'
                },{
                    name : 'mapping_par_workflowVariable',
                    label : get_cbuilder_msg("pbuilder.label.workflowVariable"),
                    type : 'selectbox',
                    required : 'True',
                    options_callback : "ProcessBuilder.getWorkflowVariablesOptions",
                    control_field: 'mapping_par_type',
                    control_value: 'workflowVariable',
                    control_use_regex: 'false'
                },{
                    name : 'mapping_par_wv_type',
                    label : get_cbuilder_msg("pbuilder.label.workflowVariableRepresent"),
                    type : 'selectbox',
                    required : 'True',
                    options : [
                        {value : "group" , label : get_cbuilder_msg("pbuilder.label.groups")},
                        {value : "user" , label : get_cbuilder_msg("pbuilder.label.users")},
                        {value : "department" , label : get_cbuilder_msg("pbuilder.label.department")},
                        {value : "hod" , label : get_cbuilder_msg("pbuilder.label.hod")}
                    ],
                    control_field: 'mapping_par_type',
                    control_value: 'workflowVariable',
                    control_use_regex: 'false'
                },{
                    name: 'mapping_par_plugin',
                    label: get_cbuilder_msg("pbuilder.label.plugin"),
                    type : 'elementselect',
                    required : 'True',
                    options_callback : function(props, values) {
                        var options = [{label : '', value : ''}];
                        var plugins = ProcessBuilder.availableParticipantPlugin;
                        for(var e in plugins){
                            options.push({label : UI.escapeHTML(plugins[e].label), value : e});
                        }
                        return options;
                    },
                    url : CustomBuilder.contextPath + '/web/property/json'+CustomBuilder.appPath+'/getPropertyOptions',
                    control_field: 'mapping_par_type',
                    control_value: 'plugin',
                    control_use_regex: 'false'
                }]
            }
        ];
        
        return def;
    },
    
    /*
     * Get the mapping properties options for activity node
     */   
    getActivityDef : function(elementObj, component) {
        ProcessBuilder.setUrlVariables(elementObj);
        
        var def = [
            {
                title: get_cbuilder_msg("pbuilder.label.configureMapping"),
                properties: [{
                    name: 'mapping_act_type',
                    label: get_cbuilder_msg("cbuilder.type"),
                    type : 'selectbox',
                    options : [
                        {value : "SINGLE", label : get_cbuilder_msg("pbuilder.label.form")},
                        {value : "EXTERNAL", label : get_cbuilder_msg("pbuilder.label.externalForm")},
                    ]
                },{
                    name : 'mapping_act_formId',
                    label : get_cbuilder_msg("pbuilder.label.formName"),
                    type : 'selectbox',
                    options_callback : function(props, values) {
                        var options = [{label : '', value : ''}];
                        var plugins = ProcessBuilder.availableForms;
                        for(var e in plugins){
                            options.push({label : UI.escapeHTML(plugins[e]), value : e});
                        }
                        return options;
                    },
                    options_callback_addoption : function() {
                        ProcessBuilder.getForms([]);
                    },
                    control_field: 'mapping_act_type',
                    control_value: 'SINGLE',
                    control_use_regex: 'false'
                },{
                    name : 'mapping_act_formUrl',
                    label : get_cbuilder_msg("pbuilder.label.url"),
                    type : 'textfield',
                    required: 'true',
                    control_field: 'mapping_act_type',
                    control_value: 'EXTERNAL',
                    control_use_regex: 'false'
                },{
                    name : 'mapping_act_formIFrameStyle',
                    label : get_cbuilder_msg("pbuilder.label.iframeStyle"),
                    type : 'codeeditor',
                    mode : 'css',
                    control_field: 'mapping_act_type',
                    control_value: 'EXTERNAL',
                    control_use_regex: 'false'
                },{
                    name: 'mapping_act_disableSaveAsDraft',
                    label: get_cbuilder_msg("pbuilder.label.removeSaveAsDraftButton"),
                    type : 'checkbox',
                    options : [
                        {value : "true", label : ''}
                    ],
                    control_field: 'mapping_act_type',
                    control_value: 'SINGLE',
                    control_use_regex: 'false'
                },{
                    name: 'mapping_act_autoContinue',
                    label: get_cbuilder_msg("pbuilder.label.showNextAssignment"),
                    type : 'checkbox',
                    options : [
                        {value : "true", label : ''}
                    ]
                }]
            }
        ];
        
        if (Object.keys(ProcessBuilder.availableAssignmentFormModifier).length > 0) {
            def[0].properties.push({
                name: 'mapping_act_modifier',
                label: get_cbuilder_msg("pbuilder.label.moreSettings"),
                type : 'elementselect',
                options_callback : function(props, values) {
                    var options = [{label : '', value : ''}];
                    var plugins = ProcessBuilder.availableAssignmentFormModifier;
                    for(var e in plugins){
                        options.push({label : UI.escapeHTML(plugins[e]), value : e});
                    }
                    return options;
                },
                url : CustomBuilder.contextPath + '/web/property/json'+CustomBuilder.appPath+'/getPropertyOptions'
            });
        }
        
        return def;
    },
    
    /*
     * Get the mapping properties options for tool node
     */   
    getToolDef : function(elementObj, component) {
        ProcessBuilder.setUrlVariables(elementObj);
        return ProcessBuilder.multiToolProps;
    },
    
    /*
     * Get the mapping properties options for route node
     */   
    getRouteDef : function(elementObj, component) {
        ProcessBuilder.setUrlVariables(elementObj);
        var def = [
            {
                title: get_cbuilder_msg("pbuilder.label.configureMapping"),
                properties: [{
                    name: 'mapping_act_plugin',
                    label: get_cbuilder_msg("pbuilder.label.plugin"),
                    type : 'elementselect',
                    options_callback : function(props, values) {
                        var options = [{label : '', value : ''}];
                        var plugins = ProcessBuilder.availableDecisionPlugin;
                        for(var e in plugins){
                            options.push({label : UI.escapeHTML(plugins[e].label), value : e});
                        }
                        return options;
                    },
                    url : CustomBuilder.contextPath + '/web/property/json'+CustomBuilder.appPath+'/getPropertyOptions'
                }]
            }
        ];
        return def;
    },
    
    /*
     * Get the mapping properties options for start node
     */   
    getStartDef : function(elementObj, component) {
        ProcessBuilder.setUrlVariables(elementObj);
        var def = [
            {
                title: get_cbuilder_msg("pbuilder.label.configureMapping"),
                properties: [{
                    name: 'mapping_act_type',
                    label: get_cbuilder_msg("cbuilder.type"),
                    type : 'selectbox',
                    options : [
                        {value : "SINGLE", label : get_cbuilder_msg("pbuilder.label.form")},
                        {value : "EXTERNAL", label : get_cbuilder_msg("pbuilder.label.externalForm")},
                    ]
                },{
                    name : 'mapping_act_formId',
                    label : get_cbuilder_msg("pbuilder.label.formName"),
                    type : 'selectbox',
                    options_callback : function(props, values) {
                        var options = [{label : '', value : ''}];
                        var plugins = ProcessBuilder.availableForms;
                        for(var e in plugins){
                            options.push({label : UI.escapeHTML(plugins[e]), value : e});
                        }
                        return options;
                    },
                    options_callback_addoption : function() {
                        ProcessBuilder.getForms([]);
                    },
                    control_field: 'mapping_act_type',
                    control_value: 'SINGLE',
                    control_use_regex: 'false'
                },{
                    name : 'mapping_act_formUrl',
                    label : get_cbuilder_msg("pbuilder.label.url"),
                    type : 'textfield',
                    required: 'true',
                    control_field: 'mapping_act_type',
                    control_value: 'EXTERNAL',
                    control_use_regex: 'false'
                },{
                    name : 'mapping_act_formIFrameStyle',
                    label : get_cbuilder_msg("pbuilder.label.iframeStyle"),
                    type : 'codeeditor',
                    mode : 'css',
                    control_field: 'mapping_act_type',
                    control_value: 'EXTERNAL',
                    control_use_regex: 'false'
                },{
                    name: 'mapping_act_autoContinue',
                    label: get_cbuilder_msg("pbuilder.label.showNextAssignment"),
                    type : 'checkbox',
                    options : [
                        {value : "true", label : ''}
                    ]
                }]
            }
        ];
        
        if (Object.keys(ProcessBuilder.availableStartProcessFormModifier).length > 0) {
            def[0].properties.push({
                name: 'mapping_act_modifier',
                label: get_cbuilder_msg("pbuilder.label.moreSettings"),
                type : 'elementselect',
                options_callback : function(props, values) {
                    var options = [{label : '', value : ''}];
                    var plugins = ProcessBuilder.availableStartProcessFormModifier;
                    for(var e in plugins){
                        options.push({label : UI.escapeHTML(plugins[e]), value : e});
                    }
                    return options;
                },
                url : CustomBuilder.contextPath + '/web/property/json'+CustomBuilder.appPath+'/getPropertyOptions'
            });
        }
        
        def.push(ProcessBuilder.getProcessStartWhiteListDef(elementObj, component)[0]);
        
        return def;
    },
    
    /*
     * return a list of available activities options
     */
    getActivitiesOptions : function() {
        var options = [
            {value : "", label : get_cbuilder_msg("pbuilder.label.previousActivity")}
        ];
        
        for (var i in ProcessBuilder.currentProcessData.participants) {
            for (var j in ProcessBuilder.currentProcessData.participants[i].activities) {
                var act = ProcessBuilder.currentProcessData.participants[i].activities[j];
                if (act.className === "activity") {
                    options.push({value : act.properties.id, label : act.properties.label});    
                }
            }
        }
            
        options.push({value : "runProcess", label : get_cbuilder_msg("pbuilder.label.runProcess")});    
        
        return options;
    },
   
    /*
     * return a list of available workflow variables options
     */                
    getWorkflowVariablesOptions : function() {
        var options = [];
        
        for (var i in ProcessBuilder.currentProcessData.properties.dataFields) {
            var id = ProcessBuilder.currentProcessData.properties.dataFields[i].variableId;
            options.push({value : id, label : id});    
        }
        
        return options;
    },
            
    /*
     * return a array of available workflow variables
     */                
    getWorkflowVariables : function() {
        var options = [];
        
        for (var i in ProcessBuilder.currentProcessData.properties.dataFields) {
            var id = ProcessBuilder.currentProcessData.properties.dataFields[i].variableId;
            options.push(id);    
        }
        
        return options;
    },
          
    /*
     * return a list of of outgoing transitions options of the current selected activity 
     */                
    getCurrentActivityOutgoingTransition : function() {
        var options = [];
        
        var act = CustomBuilder.Builder.selectedEl;
        var sourceConnSet = ProcessBuilder.jsPlumb.getConnections({source: $(act)});
        for (var i = sourceConnSet.length - 1; i >= 0; i--) {
            if (!$(sourceConnSet[i].target).hasClass("end")) { 
                var id = $(sourceConnSet[i].canvas).data("data").properties.id;
                var label = $(sourceConnSet[i].canvas).data("data").properties.label;
                if (label === undefined || label === "") {
                    label = id + " (" + $(sourceConnSet[i].target).data("data").properties.label +")";
                }
                
                options.push({
                    value : id,
                    label : label
                });
            }
        }
        
        return options;
    },
    
    /*
     * Retrive the multi tools properties options for tool mapping
     */
    getMultiToolsProps : function(deferreds) {
        var wait = $.Deferred();
        deferreds.push(wait);
        
        CustomBuilder.cachedAjax({
            type: "POST",
            data: {
                "value": "org.joget.apps.app.lib.MultiTools"
            },
            url: CustomBuilder.contextPath + '/web/property/json'+CustomBuilder.appPath+'/getPropertyOptions',
            dataType : "json",
            beforeSend: function (request) {
                request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            },
            success: function(response) {
                if (response !== null && response !== undefined && response !== "") {
                    try {
                        var data = eval(response);
                        
                        data[0].title = get_cbuilder_msg("pbuilder.label.configureMapping");
                        
                        ProcessBuilder.multiToolProps = data;
                    } catch (err) {}
                }
                wait.resolve();        
            },
            error: function() {
                //ignore
            }
        });
    },
        
    /*
     * Retrieve a list of available assignment form modifier
     */                
    getAssignmentFormModifier : function (deferreds) {
        var wait = $.Deferred();
        deferreds.push(wait);
        
        $.getJSON(
            CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.apps.app.model.ProcessFormModifier',
            function(returnedData){
                ProcessBuilder.availableAssignmentFormModifier = {};
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        ProcessBuilder.availableAssignmentFormModifier[returnedData[e].value] = returnedData[e].label;
                    }
                }
                wait.resolve();
            }
        );
    },
    
    /*
     * Retrieve a list of available start process form modifier
     */
    getStartProcessFormModifier: function(deferreds) {
        var wait = $.Deferred();
        deferreds.push(wait);
        
        $.getJSON(
            CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.apps.app.model.StartProcessFormModifier',
            function(returnedData){
                ProcessBuilder.availableStartProcessFormModifier = {};
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        ProcessBuilder.availableStartProcessFormModifier[returnedData[e].value] = returnedData[e].label;
                    }
                }
                wait.resolve();
            }
        );
    },  
         
    /*
     * Retrieve a list of available tools
     */                
    getTools : function(deferreds) {
        var wait = $.Deferred();
        deferreds.push(wait);
        
        $.getJSON(
            CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.plugin.base.ApplicationPlugin',
            function(returnedData){
                ProcessBuilder.availableTools = {};
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        ProcessBuilder.availableTools[returnedData[e].value] = returnedData[e];
                    }
                }
                wait.resolve();
            }
        );
    },   
            
    /*
     * Retrieve a list of available decision plugin
     */                
    getDecisionPlugin : function(deferreds) {
        var wait = $.Deferred();
        deferreds.push(wait);
        
        $.getJSON(
            CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.workflow.model.DecisionPlugin',
            function(returnedData){
                ProcessBuilder.availableDecisionPlugin = {};
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        ProcessBuilder.availableDecisionPlugin[returnedData[e].value] = returnedData[e];
                    }
                }
                wait.resolve();
            }
        );
    },
      
    /*
     * Retrieve a list of available participant plugin
     */                  
    getParticipants : function(deferreds) {
        var wait = $.Deferred();
        deferreds.push(wait);
        
        $.getJSON(
            CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.workflow.model.ParticipantPlugin',
            function(returnedData){
                ProcessBuilder.availableParticipantPlugin = {};
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        ProcessBuilder.availableParticipantPlugin[returnedData[e].value] = returnedData[e];
                    }
                }
                wait.resolve();
            }
        );
    },
    
    /*
     * Retrieve a list of available forms
     */                
    getForms : function(deferreds) {
        var wait = $.Deferred();
        deferreds.push(wait);
        
        $.getJSON(
            CustomBuilder.contextPath + '/web/json/console/app'+CustomBuilder.appPath+'/forms/options',
            function(returnedData){
                ProcessBuilder.availableForms = {};
                for (e in returnedData) {
                    if (returnedData[e].value !== "") {
                        ProcessBuilder.availableForms[returnedData[e].value] = returnedData[e].label;
                    }
                }
                wait.resolve();
            }
        );
    },  
    
    /*
     * Prepare and render the list view
     */
    listViewerViewInit: function(view) {
        $("body").addClass("no-left-panel");
        
        $(CustomBuilder.Builder.iframe).off("change.builder", ProcessBuilder.renderListViewer);
        $(CustomBuilder.Builder.iframe).on("change.builder", ProcessBuilder.renderListViewer);
        
        ProcessBuilder.renderListViewer();
    },
    
    /*
     * Reset the builder back to design view
     */
    listViewerViewBeforeClosed: function(view) {
        $("body").removeClass("no-left-panel");
    },
    
    /*
     * Render or update the list viewer
     */
    renderListViewer : function() {
        var self = CustomBuilder.Builder;
        var view = $("#listViewerView");
        
        if ($(view).find("ul.nav").length === 0) {
            $(view).find(".builder-view-body").html('<div class="search-container"><input class="form-control form-control-sm component-search" placeholder="'+get_cbuilder_msg('cbuilder.search')+'" type="text"><button class="clear-backspace"><i class="la la-close"></i></button></div><ul class="nav nav-tabs nav-fill" id="process-list-tabs" role="tablist"></ul><div class="tab-content"></div>');
        
            //render participants
            $(view).find('ul.nav').append('<li id="participants-tab-link" class="nav-item content-tab"><a class="nav-link show active" data-toggle="tab" href="#participants-list-tab" role="tab" aria-controls="participants-list-tab" aria-selected="true"><span>'+get_cbuilder_msg('pbuilder.label.participant')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="participants-list-tab" class="tab-pane fade active show"></div>');
            
            //render activities
            $(view).find('ul.nav').append('<li id="activities-tab-link" class="nav-item content-tab"><a class="nav-link show" data-toggle="tab" href="#activities-list-tab" role="tab" aria-controls="activities-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.activity')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="activities-list-tab" class="tab-pane fade show"></div>');
            
            //render tools
            $(view).find('ul.nav').append('<li id="tools-tab-link" class="nav-item content-tab"><a class="nav-link show" data-toggle="tab" href="#tools-list-tab" role="tab" aria-controls="tools-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.tool')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="tools-list-tab" class="tab-pane fade show"></div>');
            
            //render subflow
            $(view).find('ul.nav').append('<li id="subflows-tab-link" class="nav-item content-tab"><a class="nav-link show" data-toggle="tab" href="#subflows-list-tab" role="tab" aria-controls="subflows-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.subflow')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="subflows-list-tab" class="tab-pane fade show"></div>');
            
            //render routes
            $(view).find('ul.nav').append('<li id="routes-tab-link" class="nav-item content-tab"><a class="nav-link show" data-toggle="tab" href="#routes-list-tab" role="tab" aria-controls="routes-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.route')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="routes-list-tab" class="tab-pane fade show"></div>');
            
            //render transitions
            $(view).find('ul.nav').append('<li id="transitions-tab-link" class="nav-item content-tab"><a class="nav-link show" data-toggle="tab" href="#transitions-list-tab" role="tab" aria-controls="transitions-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.transition')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="transitions-list-tab" class="tab-pane fade show"></div>');
            
            //render variables
            $(view).find('ul.nav').append('<li id="variables-tab-link" class="nav-item content-tab"><a class="nav-link show" data-toggle="tab" href="#variables-list-tab" role="tab" aria-controls="variables-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.workflowVariables')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="variables-list-tab" class="tab-pane fade show"></div>');
            
            $(view).off("click", ".cbuilder-node-details-list");
            $(view).on("click", ".cbuilder-node-details-list", function(){
                $(view).find(".cbuilder-node-details-list").removeClass("active");
                $(this).addClass("active");
                var id = $(this).attr("data-cbuilder-select");
                var node = self.frameBody.find("[data-cbuilder-id='"+id+"']");
                self.selectNode(node);
            });
            
            $(view).find('.search-container input').off("keyup");
            $(view).find('.search-container input').on("keyup", function(){
                var searchText = $(this).val().toLowerCase();
                var regex = new RegExp(':"[^"]*'+searchText+'[^"]*"', 'gi'); //create regex to check the properties json value, `:"[zero or more chars not "]searchText[zero or more chars not "]"`
                
                $(view).find(".cbuilder-node-details-list").each(function(){
                    var match = false;
                    $(this).find('dd').each(function(){
                        if ($(this).text().toLowerCase().indexOf(searchText) > -1) {
                            match = true;
                        }
                    });
                    
                    var id = $(this).attr("data-cbuilder-select");
                    var node = self.frameBody.find("[data-cbuilder-id='"+id+"']");
                    if (node.length > 0 ) {
                        var data = $(node).data("data");
                        if (data !== undefined && data !== null && data.properties !== undefined && data.properties !== null) {
                            var text = JSON.encode(data.properties); //convert properties to json for regex checking
                            var match = text.match(regex);
                            if (match !== null && match.length > 0) {
                                match = true;
                            }
                        }
                    }
                    
                    if (match) {
                        $(this).parent().removeClass("searchHide").show();
                    } else {
                        $(this).parent().addClass("searchHide").hide();
                    }
                });
                
                $(view).find("#process-list-tabs li a .counter").remove();
                
                if (this.value !== "") {
                    $(this).next("button").show();
                    
                    //show counter for each tab
                    $(view).find("#process-list-tabs li a").each(function(){
                        var id = $(this).attr("href");
                        
                        var count = $(id).find('.cbuilder-node-details:not(.searchHide)').length;
                        if (count > 0) {
                            $(this).append(' <span class="counter badge rounded-pill bg-primary text-white">'+count+'</span>');
                        }
                    });
                } else {
                    $(this).next("button").hide();
                }
            });
            
            $(view).find('.search-container .clear-backspace').off("click");
            $(view).find('.search-container .clear-backspace').on("click", function(){
                $(this).hide();
                $(this).prev("input").val("");
                $(view).find(".cbuilder-node-details").show();
                $(view).find("#process-list-tabs li a .counter").remove();
            });
        }
        
        var process = ProcessBuilder.currentProcessData;
        $(view).find('.tab-content > div').html("");
        
        var start;
        
        for (var p in process.participants) {
            var par = process.participants[p];
            ProcessBuilder.renderListViewerDetails($(view), par);
            
            for (var act in par.activities) {
                var activity = par.activities[act];
                if (activity.className === "end") {
                    continue;
                } else if (activity.className !== "start") {
                    ProcessBuilder.renderListViewerDetails($(view), activity);
                } else {
                    start = activity;
                }
            }
        }
        
        for (var t in process.transitions) {
            var transition = process.transitions[t];
            if (transition.properties.type !== "startend") {
                ProcessBuilder.renderListViewerDetails($(view), transition);
            }
        }
        
        if (start !== undefined) {
            var clone = $.extend(true, {}, start);
            clone.className = "processStartWhitelist";
            ProcessBuilder.renderListViewerDetails($(view), clone);
            ProcessBuilder.renderListViewerDetails($(view), start);
        }
        
        //render variable list
        for (var df=0; df<process.properties.dataFields.length; df++) {
            var dataField = process.properties.dataFields[df];
            ProcessBuilder.renderVariableListViewerDetail($(view), dataField);
        }
    },
         
    /*
     * render the detail row in list view for variable
     */                
    renderVariableListViewerDetail : function(container, variable) {
        var self = CustomBuilder.Builder;
        var list = $(container).find("#variables-list-tab");
        
        var detailsDiv = $('<div class="cbuilder-node-details" ><dl class=\"cbuilder-node-details-list\" style="pointer-events:none;cursor:none;"></dl></div>');
        $(list).append(detailsDiv);
        var dl = detailsDiv.find('dl');
        
        dl.append('<dt class="header"></dt><dd><h6 class="header">'+variable.variableId+'</h6></dd>');
    },        
    
    /*
     * render the detail row in list view
     */
    renderListViewerDetails : function(container, obj) {
        var self = CustomBuilder.Builder;
        
        var listName = (obj.className === "start" || obj.className === "activity")?"activitie":(obj.className === "processStartWhitelist"?"participant":obj.className);
        var list = $(container).find("#"+listName+"s-list-tab");
        
        var detailsDiv = $('<div class="cbuilder-node-details"><dl class=\"cbuilder-node-details-list\"></dl></div>');
        $(list).append(detailsDiv);
        var dl = detailsDiv.find('dl');
        dl.attr("data-cbuilder-select", obj.properties.id);
        
        var id = obj.properties.id;
        if (self.selectedEl) {
            var selectedData = $(self.selectedEl).data("data");
            if (selectedData.properties.id === id) {
                $(detailsDiv).find(".cbuilder-node-details-list").addClass("active");
                var listId = $(list).attr("id");
                $('[aria-controls="'+listId+'"]').tab('show');
            }
        }
        
        if (obj.className === "start") {
            id = "runProcess";
        }
        
        var label = "";
        if (obj.properties.label !== undefined && obj.properties.label !== "") {
            label = obj.properties.label + ' (' + id + ')';
        } else if (obj.className === "start") {
            label = get_cbuilder_msg("pbuilder.label.runProcess") + ' (' + id + ')';
        } else if (obj.className === "processStartWhitelist") {
            label = get_cbuilder_msg("pbuilder.label.processStartWhiteList");
        } else {
            label = id;
        }
        
        dl.append('<dt class="header"></dt><dd><h6 class="header">'+label+'</h6></dd>');
        
        if (obj.properties.label !== undefined && obj.properties.label !== "") {
            dl.append('<dt><i class="las la-signature" title="'+get_cbuilder_msg('pbuilder.label')+'"></i></dt><dd>'+obj.properties.label+'</dd>');
        }
        if (obj.className === "start") {
            dl.append('<dt><i class="las la-signature" title="'+get_cbuilder_msg('pbuilder.label')+'"></i></dt><dd>'+get_cbuilder_msg("pbuilder.label.runProcess")+'</dd>');
        }
        
        var component = self.getComponent(obj.className);
        ProcessBuilder.renderXray(detailsDiv, detailsDiv, obj, component, function(){
            $(dl).find('dt i').each(function(){
                var i = $(this);
                var title = $(i).attr("title");
                $(i).after(' <span>'+title+'</span>');
                $(i).removeAttr("title");
            });
        });
    },
    
    /*
     * A callback method called from the CustomBuilder.Builder.renderNodeAdditional
     * It used to render the info of an element
     */
    renderXray: function(detailsDiv, element, elementObj, component , callback) {
        var dl = detailsDiv.find('dl');
        
        if (elementObj.className === "activity" || elementObj.className === "start") {
            if (elementObj.properties.deadlines !== undefined && elementObj.properties.deadlines.length > 0) {
                var deadlines = [];
                for (var d in elementObj.properties.deadlines) {
                    deadlines.push(elementObj.properties.deadlines[d].exceptionName);
                }
                $(dl).append('<dt><i class="las la-clock" title="'+get_cbuilder_msg('pbuilder.label.deadlines')+'"></i></dt><dd>'+deadlines.join(', ')+'</dd>');
            }
            if (elementObj.properties.limit !== undefined && elementObj.properties.limit !== "") {
                $(dl).append('<dt><i class="las la-user-clock" title="'+get_cbuilder_msg('pbuilder.label.sla')+'"></i></dt><dd>'+elementObj.properties.limit+ProcessBuilder.currentProcessData.properties.durationUnit.toLowerCase()+'</dd>');
            }
            if (elementObj.properties.mapping_act_type === "SINGLE" 
                    && elementObj.properties.mapping_act_formId !== undefined && elementObj.properties.mapping_act_formId !== "") {
                var label = ProcessBuilder.availableForms[elementObj.properties.mapping_act_formId];
                $(dl).append('<dt><i class="las la-file-alt" title="'+get_cbuilder_msg('pbuilder.label.form')+'"></i></dt><dd>'+label+'</dd>');
            } else if (elementObj.properties.mapping_act_formUrl !== undefined && elementObj.properties.mapping_act_formUrl !== "") {
                $(dl).append('<dt><i class="las la-link" title="'+get_cbuilder_msg('pbuilder.label.url')+'"></i></dt><dd>'+elementObj.properties.mapping_act_formUrl+'</dd>');
            }
            if (elementObj.properties.mapping_act_modifier !== undefined 
                    && elementObj.properties.mapping_act_modifier["className"] !== undefined 
                    && elementObj.properties.mapping_act_modifier["className"] !== "") {
                var label = '<span class="missing-plugin">' + elementObj.properties.mapping_act_modifier["className"] + " (" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")</span>";
                if (elementObj.className === "activity" && ProcessBuilder.availableAssignmentFormModifier[elementObj.properties.mapping_act_modifier["className"]] !== undefined) {
                    label = ProcessBuilder.availableAssignmentFormModifier[elementObj.properties.mapping_act_modifier["className"]];
                } else if (elementObj.className === "start" && ProcessBuilder.availableStartProcessFormModifier[elementObj.properties.mapping_act_modifier["className"]] !== undefined) {
                    label = ProcessBuilder.availableStartProcessFormModifier[elementObj.properties.mapping_act_modifier["className"]]
                }
                $(dl).append('<dt><i class="las la-plug" title="'+get_cbuilder_msg('pbuilder.label.moreSettings')+'"></i></dt><dd>'+label+'</dd>');
            }
        } else if (elementObj.className === "tool") {
            if (elementObj.properties.tools !== undefined 
                    && elementObj.properties.tools.length > 0) {
                var toolsLabel = "";
                var count = 1;
                for (var t in elementObj.properties.tools) {
                    var className = elementObj.properties.tools[t]['className'];
                    if (className !== undefined && className !== "") {
                        if (toolsLabel !== "") {
                            toolsLabel += "<br/>";
                        }
                        var plugin = ProcessBuilder.availableTools[className];
                        if (plugin === undefined) {
                            label = '<span class="missing-plugin">' + className + " (" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")</span>";
                        } else {
                            label = plugin.label;
                        }
                        toolsLabel += count + ". " +label;
                        count++;
                    }
                }
                $(dl).append('<dt><i class="las la-plug" title="'+get_cbuilder_msg('pbuilder.label.plugin')+'"></i></dt><dd>'+toolsLabel+'</dd>');
            }
        } else if (elementObj.className === "route") {
            if (elementObj.properties.mapping_act_plugin !== undefined 
                    && elementObj.properties.mapping_act_plugin["className"] !== undefined 
                    && elementObj.properties.mapping_act_plugin["className"] !== "") {
                var plugin = ProcessBuilder.availableDecisionPlugin[elementObj.properties.mapping_act_plugin["className"]];
                if (plugin === undefined) {
                    label = '<span class="missing-plugin">' + elementObj.properties.mapping_act_plugin["className"] + " (" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")</span>";
                } else {
                    label = plugin.label;
                }
                $(dl).append('<dt><i class="las la-plug" title="'+get_cbuilder_msg('pbuilder.label.plugin')+'"></i></dt><dd>'+label+'</dd>');
            }
        } else if (elementObj.className === "transition") {
            $(dl).append('<dt><i class="las la-play" title="'+get_cbuilder_msg('pbuilder.label.from')+'"></i></dt><dd>'+elementObj.properties.from+'</dd>');
            $(dl).append('<dt><i class="las la-stop" title="'+get_cbuilder_msg('pbuilder.label.to')+'"></i></dt><dd>'+elementObj.properties.to+'</dd>');
            if (elementObj.properties.type !== "") {
                $(dl).append('<dt><i class="las la-shapes" title="'+get_cbuilder_msg('cbuilder.type')+'"></i></dt><dd>'+elementObj.properties.type+'</dd>');
                if (elementObj.properties.type === "CONDITION") {
                    $(dl).append('<dt><i class="las la-bars" title="'+get_cbuilder_msg('pbuilder.label.condition')+'"></i></dt><dd>'+elementObj.properties.condition+'</dd>');
                } else if (elementObj.properties.type === "EXCEPTION") {
                    $(dl).append('<dt><i class="las la-exclamation-circle" title="'+get_cbuilder_msg('pbuilder.label.condition')+'"></i></dt><dd>'+elementObj.properties.exceptionName+'</dd>');
                }
            }
        } else if (elementObj.className === "subflow") {
            var label = elementObj.properties.subflowId;
            if ($("#processes_list option[value='"+label+"']").length > 0) {
                label = $("#processes_list option[value='"+label+"']").text();
            }
            $(dl).append('<dt><i class="las la-th-list" title="'+get_cbuilder_msg('pbuilder.label.process')+'"></i></dt><dd>'+label+'</dd>');
        } else if (elementObj.className === "participant" || elementObj.className === "processStartWhitelist") {
            var type = elementObj.properties.mapping_par_type;
            if (type !== undefined && type !== "") {
                if (type === "user" || type === "group") {
                    type += "s";
                }
                $(dl).append('<dt><i class="las la-shapes" title="'+get_cbuilder_msg('cbuilder.type')+'"></i></dt><dd>'+get_cbuilder_msg('pbuilder.label.'+type)+'</dd>');

                if (elementObj.properties.mapping_par_type === "user") {
                    $(dl).append('<dt><i class="las la-user" title="'+get_cbuilder_msg('pbuilder.label.'+type)+'"></i></dt><dd>'+elementObj.properties.mapping_par_users.replace(/;/g, ', ')+'</dd>');
                } else if (elementObj.properties.mapping_par_type === "group") {
                    $(dl).append('<dt><i class="las la-users" title="'+get_cbuilder_msg('pbuilder.label.'+type)+'"></i></dt><dd>'+elementObj.properties.mapping_par_groups.replace(/;/g, ', ')+'</dd>');
                } else if (elementObj.properties.mapping_par_type === "department" || elementObj.properties.mapping_par_type === "hod") {
                    $(dl).append('<dt><i class="las la-users" title="'+get_cbuilder_msg('pbuilder.label.'+type)+'"></i></dt><dd>'+elementObj.properties.mapping_par_department+'</dd>');
                } else if (elementObj.properties.mapping_par_type === "performer") {
                    $(dl).append('<dt><i class="las la-user-tie" title="'+get_cbuilder_msg('pbuilder.label.'+type)+'"></i></dt><dd>'+get_cbuilder_msg('pbuilder.label.performerType.'+elementObj.properties.mapping_par_performer_type)+'</dd>');
                    var options = ProcessBuilder.getActivitiesOptions();
                    var label = elementObj.properties.mapping_par_performer_act;
                    for (var o in options) {
                        if (options[o].value === label) {
                            label = options[o].label;
                        }
                    }
                    $(dl).append('<dt><i class="las la-check-square" title="'+get_cbuilder_msg('pbuilder.label.activity')+'"></i></dt><dd>'+label+'</dd>');
                } else if (elementObj.properties.mapping_par_type === "workflowVariable") {
                    $(dl).append('<dt><i class="las la-font" title="'+get_cbuilder_msg('pbuilder.label.variable')+'"></i></dt><dd>'+elementObj.properties.mapping_par_workflowVariable+'</dd>');
                    var r = elementObj.properties.mapping_par_wv_type;
                    if (r === "user" || r === "group") {
                        r += "s";
                    }
                    $(dl).append('<dt><i class="las la-user-tie" title="'+get_cbuilder_msg('pbuilder.label.represent')+'"></i></i></dt><dd>'+get_cbuilder_msg('pbuilder.label.'+r)+'</dd>');    
                } else if (elementObj.properties.mapping_par_type === "plugin") {
                    var plugin = ProcessBuilder.availableParticipantPlugin[elementObj.properties.mapping_par_plugin["className"]];
                    if (plugin === undefined) {
                        label = '<span class="missing-plugin">' + elementObj.properties.mapping_par_plugin["className"] + " (" + get_advtool_msg('dependency.tree.Missing.Plugin') + ")</span>";
                    } else {
                        label = plugin.label;
                    }
                    $(dl).append('<dt><i class="las la-plug" title="'+get_cbuilder_msg('pbuilder.label.plugin')+'"></i></dt><dd>'+label+'</dd>');
                } 
            } else if (elementObj.className === "processStartWhitelist") {
                $(dl).append('<dt><i class="las la-shapes" title="'+get_cbuilder_msg('cbuilder.type')+'"></i></dt><dd>'+get_cbuilder_msg('pbuilder.label.type.role')+'</dd>');
                if (elementObj.properties['mapping_par_role'] === undefined || elementObj.properties['mapping_par_role'] === "") {
                    $(dl).append('<dt><i class="las la-user-tie" title="'+get_cbuilder_msg('pbuilder.label.type.role')+'"></i></i></dt><dd>'+get_cbuilder_msg('pbuilder.label.type.role.everyone')+'</dd>'); 
                } else {
                    $(dl).append('<dt><i class="las la-user-tie" title="'+get_cbuilder_msg('pbuilder.label.type.role')+'"></i></i></dt><dd>'+get_cbuilder_msg('pbuilder.label.'+elementObj.properties['mapping_par_role'])+'</dd>'); 
                }
            }
        }
        
        callback();
    } ,
         
    /*
     * Used to render advance tool > xpdl
     */                
    xpdlViewInit : function(view) {
        $(view).addClass("ace_fullpage");
        $(view).html('');
        $(view).append('<pre id="xpdl_definition" style="height:100%"></pre><div class="sticky-buttons"><button class="upload-btn btn button btn-secondary">'+get_cbuilder_msg('pbuilder.label.uploadXpdl')+'</button> <button class="update-btn btn button btn-secondary">'+get_cbuilder_msg('cbuilder.update')+'</button></div>');
        
        var editor = ace.edit("xpdl_definition");
        editor.$blockScrolling = Infinity;
        if ($('body').attr('builder-theme') === "dark") {
            editor.setTheme("ace/theme/vibrant_ink");
        } else {
            editor.setTheme("ace/theme/textmate");
        }
        editor.getSession().setTabSize(4);
        editor.getSession().setMode("ace/mode/xml");
        editor.setAutoScrollEditorIntoView(true);
        editor.getSession().setValue(ProcessBuilder.toXpdl());
        editor.resize();
        
        $(view).find("button.update-btn").on("click", function() {
            var btn = this;
            var text = $(this).text();
            $(this).attr("disabled", true);
            
            ProcessBuilder.updateJsonFromXpdl(editor.getSession().getValue(), function(){
                $(btn).text(get_advtool_msg('adv.tool.updated'));
                setTimeout(function(){
                    $(btn).text(text);
                    $(btn).removeAttr("disabled");
                }, 1000);
            });
        });
        
        $(view).find("button.upload-btn").on("click", function() {
            JPopup.show("uploadXpdlDialog", CustomBuilder.contextPath + '/web/console/app'+CustomBuilder.appPath+'/package/upload', {}, "");
        });
    },   
        
    /*
     * escape unsafe char in xml attr value
     */                
    escapeXml : function(unsafe) {
        if (unsafe === undefined || unsafe === null || unsafe === "") {
            return unsafe;
        }
        return unsafe.replace(/[<>&'"]/g, function (c) {
            switch (c) {
                case '<': return '&lt;';
                case '>': return '&gt;';
                case '&': return '&amp;';
                case '\'': return '&apos;';
                case '"': return '&quot;';
            }
        });
    },        
        
    /*
     * Convert object to xml
     */                
    obj2Xml : function(obj, name, level) {
        var xml = '';
        var selfClosing = false;
        
        var attrs = '';
        var body = '';
        
        var space = '';
        if (level > 0) {
            for (var i = 0; i < (level * 4); i++) {
                space += ' ';
            }
        }
        
        if (typeof obj === "object" && !(obj instanceof String)) {
            for (var prop in obj) {
                if (prop === "-self-closing") {
                    selfClosing = obj['-self-closing'];
                } else if (prop.indexOf("-") === 0) {
                    attrs += " " + prop.substring(1) + "=\"" + ProcessBuilder.escapeXml(obj[prop]) + "\"";
                } else if (prop.indexOf("#") === 0) {
                    //ignore
                } else if (obj[prop] instanceof Array) {
                    for (var array in obj[prop]) {
                        body += ProcessBuilder.obj2Xml(new Object(obj[prop][array]), prop, level + 1);
                    }
                } else if (typeof obj[prop] == "object") {
                    body += ProcessBuilder.obj2Xml(new Object(obj[prop]), prop, level + 1);
                } else {
                    body += ProcessBuilder.obj2Xml(obj[prop], prop, level + 1);
                }
            }
        } else {
            body = obj;
        }
        
        if (name !== "") {
            if (selfClosing) {
                xml = space + "<" + name + attrs + "/>\n";
            } else if (typeof obj !== "object" || obj instanceof String) {
                xml = space + "<" + name + ">" + body + "</" + name + ">\n";
            } else {
                xml = space + "<" + name + attrs + ">\n" + body + space + "</" + name + ">\n";
            }
        } else {
            xml = body;
        }
        return xml
    },        
    
    /*
     * Generate xpdl from the json def
     */
    toXpdl : function () {
        return ProcessBuilder.obj2Xml(CustomBuilder.data.xpdl, "", -1);
    },
      
    /*
     * Callback from upload xpdl file. ConsoleWebConstroller.consolePackageUploadSubmit
     */                
    updateJsonFromUploadedXpdl : function(jsonStr) {
        if (jsonStr !== null && jsonStr !== undefined && jsonStr !== "") {
            try {
                var data = eval("["+jsonStr+"]")[0];
                if (data !== null && data["Package"] !== undefined) {
                    CustomBuilder.data.xpdl["Package"] = data["Package"];
                    CustomBuilder.update(true);
                    CustomBuilder.loadJson(CustomBuilder.getJson());
                    ProcessBuilder.xpdlViewInit($('#xpdlView .builder-view-body'));
                }
            } catch (err) {}
        }
    },        
    
    /*
     * Update json def based on xpdl
     */
    updateJsonFromXpdl : function(xpdl, callback) {
        var xpdlFile = new Blob([xpdl], {type : 'text/plain'});
        var params = new FormData();
        params.append("xpdlFile", xpdlFile);
        
        $.ajax({
            type: "POST",
            data: params,
            url: CustomBuilder.contextPath + '/web/console/app'+CustomBuilder.appPath+'/process/builder/xpdlJson',
            dataType : "json",
            cache: false,
            processData: false,
            contentType: false,
            beforeSend: function (request) {
                request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            },
            success: function(response) {
                if (response !== null && response !== undefined && response !== "") {
                    try {
                        var data = eval(response);
                        if (data !== null && data["Package"] !== undefined) {
                            CustomBuilder.data.xpdl["Package"] = data["Package"];
                            var json = JSON.encode(CustomBuilder.data);
                            CustomBuilder.loadJson(json, true); //update through loadJson addToUndo to make sure package id does not change.
                        }
                    } catch (err) {}
                }  
                if (callback) {
                    callback();
                }
            },
            error: function() {
                if (callback) {
                    callback();
                }
            }
        });
    },
      
    /*
     * remove dynamically added items    
     */            
    unloadBuilder : function() {
        ProcessBuilder.jsPlumb.unbind();
        ProcessBuilder.jsPlumb.detachEveryConnection();
        ProcessBuilder.jsPlumb.deleteEveryEndpoint();
        ProcessBuilder.jsPlumb.unmakeEverySource();
        ProcessBuilder.jsPlumb.unmakeEveryTarget();
        ProcessBuilder.jsPlumb.reset();
            
        $("#process-selector, .toolzoom-buttons, #listviewer-btn, #xpdl-btn").remove();
        $("#launch-btn").parent().remove();
        $(window).off('hashchange');        
    },
    
    /*
     * Render process graph for monitoring feature
     */
    loadGraph : function(json, processId, runningActivities) {
        CustomBuilder.data = JSON.decode(json);
        
        ProcessBuilder.readonly = true;
        
        CustomBuilder.Builder.init({
            "enableViewport" : false,
            callbacks : {
                "initComponent" : "ProcessBuilder.initComponent",
                "renderElement" : "ProcessBuilder.renderElement"
            }
        }, function() {
            ProcessBuilder.initComponents();
            CustomBuilder.Builder.setHead('<link data-pbuilder-style href="' + CustomBuilder.contextPath + '/pbuilder/css/pbuilder.css" rel="stylesheet" />');
            CustomBuilder.Builder.setHead('<script data-jsPlumb-script src="' + CustomBuilder.contextPath + '/pbuilder/js/jquery.jsPlumb-1.6.4-min.js"></script>');

            //wait for jsplumb available
            while (!ProcessBuilder.jsPlumb) {
                ProcessBuilder.jsPlumb = CustomBuilder.Builder.iframe.contentWindow.jsPlumb;
            }
            
            // init jsPlumb
            ProcessBuilder.jsPlumb.importDefaults({
                Container: "canvas",
                Anchor: "Continuous",
                Endpoint: ["Dot", {radius: 4}],
                Connector: ["StateMachine", {curviness:0.1}],
                PaintStyle: {strokeStyle: "#999", lineWidth: 1, outlineWidth: 15, outlineColor: 'transparent'},
                ConnectionOverlays: [
                    ["Arrow", {
                        location: 0.99,
                        id: "arrow",
                        length: 10,
                        width: 10,
                        foldback: 0.8
                    }]
                ],
                ConnectionsDetachable: true
            });
            
            var deferreds = [];
            
            var wait = $.Deferred();
            deferreds.push(wait);
            
            var jsPlumbReady = $.Deferred();
            deferreds.push(jsPlumbReady);
            ProcessBuilder.jsPlumb.ready(function() {
                //make some delay for css to load
                setTimeout(function(){
                    jsPlumbReady.resolve();
                }, 20);
                
            });
            
            wait.resolve();
            
            $.when.apply($, deferreds).then(function() {
                ProcessBuilder.generateProcessData(processId);
        
                CustomBuilder.Builder.load(ProcessBuilder.currentProcessData, function(){
                    CustomBuilder.Builder.frameBody.addClass("readonly");
                    CustomBuilder.Builder.frameBody.find('[data-cbuilder-classname]').attr('data-cbuilder-uneditable', "");
                    
                    for (var i in runningActivities) {
                        CustomBuilder.Builder.frameBody.find('#'+runningActivities[i]).addClass("running_activity");
                    }
                });
                
                ProcessBuilder.refresh();
            });
        });
    },
    
    /*
     * A callback method called from CustomBuilder.applyElementProperties when properties saved
     */
    saveEditProperties : function(container, elementProperty, elementObj, element) {
        if (elementProperty.id !== $(element).attr("id") && elementObj.className !== "process") {
            var self = CustomBuilder.Builder;

            ProcessBuilder.jsPlumb.unbind("connection");
            ProcessBuilder.jsPlumb.unbind("connectionDetached");
            ProcessBuilder.jsPlumb.unbind();

            // update transition
            var sourceConnSet = ProcessBuilder.jsPlumb.getConnections({source: $(element)});
            var targetConnSet = ProcessBuilder.jsPlumb.getConnections({target: $(element)});
            var transition = [];

            for (var i in sourceConnSet) {
                var data = $(sourceConnSet[i].canvas).data("data");
                data.properties.from = elementProperty.id;
                if (data['xpdlObj'] !== undefined) { //end node is target, xpdl object is undefined
                    data['xpdlObj']['-From'] = elementProperty.id;
                }
                ProcessBuilder.jsPlumb.detach(sourceConnSet[i]);
                transition.push(data);
            }
            for (var i in targetConnSet) {
                var data = $(targetConnSet[i].canvas).data("data");
                data.properties.to = elementProperty.id;
                if (data['xpdlObj'] !== undefined) { //start node is source, xpdl object is undefined
                    data['xpdlObj']['-To'] = elementProperty.id;
                }
                ProcessBuilder.jsPlumb.detach(targetConnSet[i]);
                transition.push(data);
            }

            $(element).attr("id", elementProperty.id);

            for (var i in transition) {
                var data = transition[i];
                var childComponent = self.parseDataToComponent(data);
                var temp = $('<div></div>');
                $(element).closest(".process").append(temp);
                self.renderElement(data, temp, childComponent, false, [""]); //add a dummy deferreds as no need it, and to stop it trigger change event
            }

            // bind event handling to new or moved connections
            ProcessBuilder.jsPlumb.bind("connection", function(info) {
                var connection = info.connection;
                ProcessBuilder.addConnection(connection);
            });

            // bind event handling to detached connections
            ProcessBuilder.jsPlumb.bind("connectionDetached", function(info) {
                var connection = info.connection;
                if ($(connection.target).attr("id").indexOf("jsPlumb") >= 0) {
                    ProcessBuilder.showConnectionDialog(connection);
                } else {
                    ProcessBuilder.removeConnection(connection);
                }
            });
        }
        
        if (elementObj.className === "transition") {
            if (elementObj.properties.type === "CONDITION") {
                if (elementObj.properties.conditionHelper === "yes") {
                    elementObj.properties.condition = ProcessBuilder.buildConditions(elementObj.properties.conditions);
                } else {
                    if (elementObj.properties.conditions !== undefined) {
                        delete elementObj.properties.conditions;
                    }
                }
                elementObj.properties.exceptionName = "";
            } else if (elementObj.properties.type === "EXCEPTION") {
                elementObj.properties.condition = "";
            } else {
                elementObj.properties.condition = "";
                elementObj.properties.exceptionName = "";
            }

        }
    },
            
    builderSaved : function() {
        ProcessBuilder.updateAdvancedView();
    },
    
    showAdvancedInfo : function() {
        $('#advancedView').slideToggle('slow');
        $('#hideAdvancedInfo').show();
    },
    
    hideAdvancedInfo : function() {
        $('#advancedView').slideToggle('slow');
        $('#hideAdvancedInfo').hide();
    }
};