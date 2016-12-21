ProcessBuilder = {};
ProcessBuilder.Model = {};

/* Participant Definition */
ProcessBuilder.Model.Participant = function() {
};
ProcessBuilder.Model.Participant.prototype = {
    class: "participant",
    id: "",
    name: "",
    type: "ROLE",
    propertyOptions: function() {
        var options = [{
            title: get_pbuilder_msg("pbuilder.label.participantProperties"),
            properties: [{
                name: 'id',
                label: get_pbuilder_msg("pbuilder.label.id"),
                type: 'textfield',
                required: 'True',
                regex_validation: '^[a-zA-Z0-9_]+$',
                validation_message: get_pbuilder_msg("pbuilder.label.invalidId")
            },{
                name: 'name',
                label: get_pbuilder_msg("pbuilder.label.name"),
                type: 'textfield',
                required: 'True',
                value: get_pbuilder_msg("pbuilder.label.participant")
            }]
        }];
        return options;
    },
    propertyUpdate: function(properties) {
        return ProcessBuilder.Actions.updateParticipant(this, properties);
    },
    toString: function() {
        return this.id + "; " + this.type + "; " + this.name;
    }
};

/* Process Definition */
ProcessBuilder.Model.Process = function() {
};
ProcessBuilder.Model.Process.prototype = {
    class: "process",
    id: "",
    name: "",
    swimlanes: "",
    durationUnit: "h",
    limit: null,
    dataFields: [], // workflow variables
    formalParameters: [], // subflow parameters
    propertyOptions: function() {
        var options = [{
            title: get_pbuilder_msg("pbuilder.label.processProperties"),
            properties: [{
                name: 'id',
                label: get_pbuilder_msg("pbuilder.label.id"),
                type: 'textfield',
                required: 'True',
                regex_validation: '^[a-zA-Z0-9_]+$',
                validation_message: get_pbuilder_msg("pbuilder.label.invalidId")
            },{
                name: 'name',
                label: get_pbuilder_msg("pbuilder.label.name"),
                type: 'textfield',
                required: 'True',
                value: get_pbuilder_msg("pbuilder.label.process")
            },{
                name: 'dataFields',
                label: get_pbuilder_msg("pbuilder.label.workflowVariables"),
                type: 'grid',
                columns: [{
                    key: 'variableId',
                    label: get_pbuilder_msg("pbuilder.label.variableId")
                }]
            }]
        },{
            title: get_pbuilder_msg("pbuilder.label.subflowProperties"),
            properties: [{
                name: 'formalParameters',
                label: get_pbuilder_msg("pbuilder.label.formalParameters"),
                type: 'grid',
                columns: [{
                    key: 'parameterId',
                    label: get_pbuilder_msg("pbuilder.label.parameterId")
                },{
                    key: 'mode',
                    label: get_pbuilder_msg("pbuilder.label.mode"),
                    options: [{
                        value: 'INOUT',
                        label: get_pbuilder_msg("pbuilder.label.inAndOut")
                    },{
                        value: 'IN',
                        label: get_pbuilder_msg("pbuilder.label.in")
                    },{
                        value: 'OUT',
                        label: get_pbuilder_msg("pbuilder.label.out")
                    }]
                }]
            }]
        },{
            title: get_pbuilder_msg("pbuilder.label.slaOptions"),
            properties: [{
                name: 'durationUnit',
                label: get_pbuilder_msg("pbuilder.label.durationUnit"),
                type: 'selectbox',
                options: [{
                    value: 'D',
                    label: get_pbuilder_msg("pbuilder.label.day")
                },{
                    value: 'h',
                    label: get_pbuilder_msg("pbuilder.label.hour")
                },{
                    value: 'm',
                    label: get_pbuilder_msg("pbuilder.label.minute")
                },{
                    value: 's',
                    label: get_pbuilder_msg("pbuilder.label.second")
                }]
            },{
                name: 'limit',
                label: get_pbuilder_msg("pbuilder.label.limit"),
                type: 'textfield',
                regex_validation: '^[0-9_]+$'
            }]
        }];
        return options;
    },
    propertyUpdate: function(properties) {
        return ProcessBuilder.Actions.updateProcess(this, properties);
    },
    toString: function() {
        return this.id + "; " + this.name;
    }
};

/* Activity Definition */
ProcessBuilder.Model.Activity = function() {
};
ProcessBuilder.Model.Activity.prototype = {
    class: "activity",
    id: "",
    name: "",
    type: "",
    performer: "",
    join: "",
    split: "",
    joinTransitions: null,
    splitTransitions: null,
    subflowId: "",
    limit: null,
    deadlines: [],
    x: "",
    y: "",
    propertyOptions: function() {
        var options = [{
            title: get_pbuilder_msg("pbuilder.label.activityProperties"),
            properties: [{
                name: 'id',
                label: get_pbuilder_msg("pbuilder.label.id"),
                type: 'textfield',
                required: 'True',
                regex_validation: '^[a-zA-Z0-9_]+$',
                validation_message: get_pbuilder_msg("pbuilder.label.invalidId")
            },{
                name: 'name',
                label: get_pbuilder_msg("pbuilder.label.name"),
                type: 'textfield',
                required: 'True',
                value: this.type
            }]
        },{
            title: get_pbuilder_msg("pbuilder.label.deadlines"),
            properties: [{
                name: 'deadlines',
                label: get_pbuilder_msg("pbuilder.label.deadlines"),
                type: 'grid',
                columns: [{
                    key: 'execution',
                    label: get_pbuilder_msg("pbuilder.label.execution"),
                    options: [{
                        value: 'ASYNCHR',
                        label: get_pbuilder_msg("pbuilder.label.asynchronous")
                    },{
                        value: 'SYNCHR',
                        label: get_pbuilder_msg("pbuilder.label.synchronous")
                    }]
                },{
                    key: 'durationUnit',
                    label: get_pbuilder_msg("pbuilder.label.durationUnit"),
                    options: [{
                        value: 'D',
                        label: get_pbuilder_msg("pbuilder.label.day")
                    },{
                        value: 'h',
                        label: get_pbuilder_msg("pbuilder.label.hour")
                    },{
                        value: 'm',
                        label: get_pbuilder_msg("pbuilder.label.minute")
                    },{
                        value: 's',
                        label: get_pbuilder_msg("pbuilder.label.second")
                    },{
                        value: 'd',
                        label: get_pbuilder_msg("pbuilder.label.dateFormat")
                    },{
                        value: 't',
                        label: get_pbuilder_msg("pbuilder.label.dateTimeFormat")
                    }]
                },{
                    key: 'deadlineLimit',
                    label: get_pbuilder_msg("pbuilder.label.deadlineLimit")
                },{
                    key: 'exceptionName',
                    label: get_pbuilder_msg("pbuilder.label.exceptionName")
                }]
            }]
        },{
            title: get_pbuilder_msg("pbuilder.label.slaOptions"),
            properties: [{
                name: 'limit',
                label: get_pbuilder_msg("pbuilder.label.limit"),
                type: 'textfield',
                regex_validation: '^[0-9_]+$'
            }]
        }];

        if (this.join !== "") {
            options[0].properties.push({
                name: 'join',
                label: get_pbuilder_msg("pbuilder.label.joinType"),
                type: "selectbox",
                options: [{
                    value: 'AND',
                    label: get_pbuilder_msg("pbuilder.label.and")
                },{
                    value: 'XOR',
                    label: get_pbuilder_msg("pbuilder.label.xor")
                }]
            });
        }
        if (this.split !== "") {
            options[0].properties.push({
                name: 'split',
                label: get_pbuilder_msg("pbuilder.label.splitType"),
                type: "selectbox",
                options: [{
                    value: 'AND',
                    label: get_pbuilder_msg("pbuilder.label.and")
                },{
                    value: 'XOR',
                    label: get_pbuilder_msg("pbuilder.label.xor")
                }]
            });
        }
        return options;
    },
    propertyUpdate: function(properties) {
        return ProcessBuilder.Actions.updateNode(this, properties);
    },
    toString: function() {
        return this.id + "; " + this.type + "; " + this.join + "; " + this.split + "; " + this.performer + "; " + this.subflowId + "; " + this.x + "," + this.y + "; " + this.name;
    }
};

/* Tool Definition */
ProcessBuilder.Model.Tool = function() {
};
ProcessBuilder.Model.Tool.prototype = {
    class: "tool",
    id: "",
    name: "",
    type: "",
    performer: "",
    join: "",
    split: "",
    joinTransitions: null,
    splitTransitions: null,
    subflowId: "",
    x: "",
    y: "",
    propertyOptions: function() {
        var options = [{
            title: get_pbuilder_msg("pbuilder.label.toolProperties"),
            properties: [{
                name: 'id',
                label: get_pbuilder_msg("pbuilder.label.id"),
                type: 'textfield',
                required: 'True',
                regex_validation: '^[a-zA-Z0-9_]+$',
                validation_message: get_pbuilder_msg("pbuilder.label.invalidId")
            },{
                name: 'name',
                label: get_pbuilder_msg("pbuilder.label.name"),
                type: 'textfield',
                required: 'True',
                value: this.type
            }]
        }];
        if (this.join !== "") {
            options[0].properties.push({
                name: 'join',
                label: get_pbuilder_msg("pbuilder.label.joinType"),
                type: "selectbox",
                options: [{
                    value: 'AND',
                    label: get_pbuilder_msg("pbuilder.label.and")
                },{
                    value: 'XOR',
                    label: get_pbuilder_msg("pbuilder.label.xor")
                }]
            });
        }
        if (this.split !== "") {
            options[0].properties.push({
                name: 'split',
                label: get_pbuilder_msg("pbuilder.label.splitType"),
                type: "selectbox",
                options: [{
                    value: 'AND',
                    label: get_pbuilder_msg("pbuilder.label.and")
                },{
                    value: 'XOR',
                    label: get_pbuilder_msg("pbuilder.label.xor")
                }]
            });
        }
        return options;
    },
    propertyUpdate: function(properties) {
        return ProcessBuilder.Actions.updateNode(this, properties);
    },
    toString: function() {
        return this.id + "; " + this.type + "; " + this.join + "; " + this.split + "; " + this.performer + "; " + this.subflowId + "; " + this.x + "," + this.y + "; " + this.name;
    }
};

/* Route Definition */
ProcessBuilder.Model.Route = function() {
};
ProcessBuilder.Model.Route.prototype = {
    class: "route",
    id: "",
    name: "",
    type: "route",
    performer: "",
    join: "",
    split: "",
    joinTransitions: null,
    splitTransitions: null,
    subflowId: "",
    x: "",
    y: "",
    propertyOptions: function() {
        var options = [{
            title: get_pbuilder_msg("pbuilder.label.routeProperties"),
            properties: [{
                name: 'id',
                label: get_pbuilder_msg("pbuilder.label.id"),
                type: 'textfield',
                required: 'True',
                regex_validation: '^[a-zA-Z0-9_]+$',
                validation_message: get_pbuilder_msg("pbuilder.label.invalidId")
            },{
                name: 'name',
                label: get_pbuilder_msg("pbuilder.label.name"),
                type: 'textfield'
            }]
        }];
        if (this.join !== "") {
            options[0].properties.push({
                name: 'join',
                label: get_pbuilder_msg("pbuilder.label.joinType"),
                type: "selectbox",
                options: [{
                    value: 'AND',
                    label: get_pbuilder_msg("pbuilder.label.and")
                },{
                    value: 'XOR',
                    label: get_pbuilder_msg("pbuilder.label.xor")
                }]
            });
        }
        if (this.split !== "") {
            options[0].properties.push({
                name: 'split',
                label: get_pbuilder_msg("pbuilder.label.splitType"),
                type: "selectbox",
                options: [{
                    value: 'AND',
                    label: get_pbuilder_msg("pbuilder.label.and")
                },{
                    value: 'XOR',
                    label: get_pbuilder_msg("pbuilder.label.xor")
                }]
            });
        }
        return options;
    },
    propertyUpdate: function(properties) {
        return ProcessBuilder.Actions.updateNode(this, properties);
    },
    toString: function() {
        return this.id + "; " + this.type + "; " + this.join + "; " + this.split + "; " + this.performer + "; " + this.subflowId + "; " + this.x + "," + this.y + "; " + this.name;
    }
};

/* Subflow Definition */
ProcessBuilder.Model.Subflow = function() {
};
ProcessBuilder.Model.Subflow.prototype = {
    class: "Subflow",
    id: "",
    name: "",
    type: "subflow",
    performer: "",
    join: "",
    split: "",
    joinTransitions: null,
    splitTransitions: null,
    subflowId: null,
    execution: null,
    actualParameters: [],
    limit: null,
    deadlines: [],
    x: "",
    y: "",
    propertyOptions: function() {
        var options = [{
            title: get_pbuilder_msg("pbuilder.label.subflowProperties"),
            properties: [{
                name: 'id',
                label: get_pbuilder_msg("pbuilder.label.id"),
                type: 'textfield',
                required: 'True',
                regex_validation: '^[a-zA-Z0-9_]+$',
                validation_message: get_pbuilder_msg("pbuilder.label.invalidId")
            },{
                name: 'name',
                label: get_pbuilder_msg("pbuilder.label.name"),
                type: 'textfield',
                required: 'True',
                value: this.type
            },{
                name: 'subflowId',
                label: get_pbuilder_msg("pbuilder.label.subProcessId"),
                type: 'textfield',
                required: 'True'
            },{
                name: 'execution',
                label: get_pbuilder_msg("pbuilder.label.execution"),
                type: "selectbox",
                options: [{
                    value: 'SYNCHR',
                    label: get_pbuilder_msg("pbuilder.label.synchronous")
                },{
                    value: 'ASYNCHR',
                    label: get_pbuilder_msg("pbuilder.label.asynchronous")
                }],
                value: "SYNCHR"
            },{
                name: 'actualParameters',
                label: get_pbuilder_msg("pbuilder.label.parameters"),
                type: 'grid',
                columns: [{
                    key: 'actualParameter',
                    label: get_pbuilder_msg("pbuilder.label.actualParameter")
                }]
            }]
        },{
            title: get_pbuilder_msg("pbuilder.label.deadlines"),
            properties: [{
                name: 'deadlines',
                label: get_pbuilder_msg("pbuilder.label.deadlines"),
                type: 'grid',
                columns: [{
                    key: 'execution',
                    label: get_pbuilder_msg("pbuilder.label.execution"),
                    options: [{
                        value: 'ASYNCHR',
                        label: get_pbuilder_msg("pbuilder.label.asynchronous")
                    },{
                        value: 'SYNCHR',
                        label: get_pbuilder_msg("pbuilder.label.synchronous")
                    }]
                },{
                    key: 'durationUnit',
                    label: get_pbuilder_msg("pbuilder.label.durationUnit"),
                    options: [{
                        value: 'D',
                        label: get_pbuilder_msg("pbuilder.label.day")
                    },{
                        value: 'h',
                        label: get_pbuilder_msg("pbuilder.label.hour")
                    },{
                        value: 'm',
                        label: get_pbuilder_msg("pbuilder.label.minute")
                    },{
                        value: 's',
                        label: get_pbuilder_msg("pbuilder.label.second")
                    },{
                        value: 'd',
                        label: get_pbuilder_msg("pbuilder.label.dateFormat")
                    },{
                        value: 't',
                        label: get_pbuilder_msg("pbuilder.label.dateTimeFormat")
                    }]
                },{
                    key: 'deadlineLimit',
                    label: get_pbuilder_msg("pbuilder.label.deadlineLimit")
                },{
                    key: 'exceptionName',
                    label: get_pbuilder_msg("pbuilder.label.exceptionName")
                }]
            }]
        }];
        if (this.join !== "") {
            options[0].properties.push({
                name: 'join',
                label: get_pbuilder_msg("pbuilder.label.joinType"),
                type: "selectbox",
                options: [{
                    value: 'AND',
                    label: get_pbuilder_msg("pbuilder.label.and")
                },{
                    value: 'XOR',
                    label: get_pbuilder_msg("pbuilder.label.xor")
                }]
            });
        }
        if (this.split !== "") {
            options[0].properties.push({
                name: 'split',
                label: get_pbuilder_msg("pbuilder.label.splitType"),
                type: "selectbox",
                options: [{
                    value: 'AND',
                    label: get_pbuilder_msg("pbuilder.label.and")
                },{
                    value: 'XOR',
                    label: get_pbuilder_msg("pbuilder.label.xor")
                }]
            });
        }
        return options;
    },
    propertyUpdate: function(properties) {
        return ProcessBuilder.Actions.updateNode(this, properties);
    },
    toString: function() {
        return this.id + "; " + this.type + "; " + this.join + "; " + this.split + "; " + this.performer + "; " + this.subflowId + "; " + this.x + "," + this.y + "; " + this.name;
    }
};

/* StartEndNode Definition */
ProcessBuilder.Model.StartEnd = function() {
};
ProcessBuilder.Model.StartEnd.prototype = {
    class: "startend",
    id: "",
    type: "",
    performer: "",
    x: "",
    y: "",
    toString: function() {
        return this.id + "; " + this.type + "; " + this.performer + "; " + this.x + "," + this.y;
    }
};

/* Transition Definition */
ProcessBuilder.Model.Transition = function() {
};
ProcessBuilder.Model.Transition.prototype = {
    class: "transition",
    id: "",
    name: "",
    from: "",
    to: "",
    type: "",
    condition: "",
    style: "",
    propertyOptions: function() {
        var options = [{
            title: get_pbuilder_msg("pbuilder.label.transitionProperties"),
            properties: [{
                name: 'name',
                label: get_pbuilder_msg("pbuilder.label.name"),
                type: 'textfield',
                required: 'False',
                value: get_pbuilder_msg("pbuilder.label.transition")
            },{
                name: 'style',
                label: get_pbuilder_msg("pbuilder.label.style"),
                type: 'radio',
                options: [{
                    value: 'straight',
                    label: get_pbuilder_msg("pbuilder.label.straight")
                },{
                    value: 'orthogonal',
                    label: get_pbuilder_msg("pbuilder.label.orthogonal")
                }],
                value: 'straight'
            },{
                name: 'type',
                label: get_pbuilder_msg("pbuilder.label.type"),
                type: 'selectbox',
                options: [{
                    value: '',
                    label: get_pbuilder_msg("pbuilder.label.normal")
                },{
                    value: 'CONDITION',
                    label: get_pbuilder_msg("pbuilder.label.condition")
                },{
                    value: 'OTHERWISE',
                    label: get_pbuilder_msg("pbuilder.label.otherwise")
                },{
                    value: 'EXCEPTION',
                    label: get_pbuilder_msg("pbuilder.label.exception")
                }],
                value: ''
            },{
                name: 'conditionHelper',
                label: get_pbuilder_msg("pbuilder.label.conditionHelper"),
                type: 'selectbox',
                options: [{
                    value: '',
                    label: get_pbuilder_msg("pbuilder.label.no")
                },{
                    value: 'yes',
                    label: get_pbuilder_msg("pbuilder.label.yes")
                }],
                value: (this.condition && this.condition !== '') ? '' : 'yes',
                control_field: 'type',
                control_value: 'CONDITION',
                control_use_regex: 'false'
            },{
                name: 'conditions',
                label: get_pbuilder_msg("pbuilder.label.conditions"),
                type: 'grid',
                columns : [{
                    key : 'join',
                    label : get_pbuilder_msg("pbuilder.label.join"),
                    options : [{
                        value : '&&',
                        label : get_pbuilder_msg("pbuilder.label.and")
                    },
                    {
                        value : '||',
                        label : get_pbuilder_msg("pbuilder.label.or")
                    }]
                },
                {
                    key : 'variable',
                    label : get_pbuilder_msg("pbuilder.label.variable"),
                    options_callback : "ProcessBuilder.Util.getVariableOptions"
                },
                {
                    key : 'operator',
                    label : get_pbuilder_msg("pbuilder.label.operation"),
                    options : [{
                        value : '===',
                        label : get_pbuilder_msg("pbuilder.label.equalTo")
                    },
                    {
                        value : '!==',
                        label : get_pbuilder_msg("pbuilder.label.notEqualTo")
                    },
                    {
                        value : '>',
                        label : get_pbuilder_msg("pbuilder.label.greaterThan")
                    },
                    {
                        value : '>=',
                        label : get_pbuilder_msg("pbuilder.label.greaterThanOrEqualTo")
                    },
                    {
                        value : '<',
                        label : get_pbuilder_msg("pbuilder.label.lessThan")
                    },
                    {
                        value : '<=',
                        label : get_pbuilder_msg("pbuilder.label.lessThanOrEqualTo")
                    },
                    {
                        value : '=== \'true\'',
                        label : get_pbuilder_msg("pbuilder.label.isTrue")
                    },
                    {
                        value : '=== \'false\'',
                        label : get_pbuilder_msg("pbuilder.label.isFalse")
                    },
                    {
                        value : '(',
                        label : get_pbuilder_msg("pbuilder.label.openParenthesis")
                    },
                    {
                        value : ')',
                        label : get_pbuilder_msg("pbuilder.label.closeParenthesis")
                    }]
                },
                {
                    key : 'value',
                    label : get_pbuilder_msg("pbuilder.label.value")
                }],
                required: 'True',
                js_validation: "ProcessBuilder.Designer.validateConditions",
                control_field: 'conditionHelper',
                control_value: 'yes',
                control_use_regex: 'false',
                value: ''
            },{
                name: 'condition',
                label: get_pbuilder_msg("pbuilder.label.condition"),
                type: 'textarea',
                required: 'True',
                js_validation: "ProcessBuilder.Designer.validateConditions",
                control_field: 'conditionHelper',
                control_value: '',
                control_use_regex: 'false',
                value: ''
            },{
                name: 'exceptionName',
                label: get_pbuilder_msg("pbuilder.label.exceptionName"),
                type: 'textfield',
                required: 'True',
                control_field: 'type',
                control_value: 'EXCEPTION',
                control_use_regex: 'false',
                value: ''
            }]
        }];
        return options;
    },
    propertyUpdate: function(properties) {
        return ProcessBuilder.Actions.updateTransition(this, properties);
    },
    toString: function() {
        return this.id + "; " + this.from + "; " + this.to + "; " + "; " + this.type + "; " + this.condition + "; " + this.style;
    }
};

/* Utility Functions */
ProcessBuilder.Util = {
    preventUndefined : function (string) {
        if (string === undefined) {
            return '';
        } else {
            return string;
        }
    },
    escapeXPDL: function(string) {
        var str = string;
        str = str.replace(/\&/g, "&amp;");
        str = str.replace(/xpdl\:/g, "");
        return str;
    },
    escapeXMLText: function(string) {
        var str = string;
        if (str) {
            str = str.replace(/\&/g, "&amp;");
            str = str.replace(/\</g, "&lt;");
        }
        return str;
    },
    encodeXML: function(value) {
        if (value) {
            value = $('<div />').text(value).html();
            return value.replace(/\"/g, "&quot;");
        } else {
            return '';
        }
    },
    decodeXML: function(value) {
        if (value) {
            return $('<div />').html(value).text();
        } else {
            return '';
        }
    },
    unescapeQuote: function(string) {
        var str = string;
        str = str.replace(/\&quot;/g, "\"");
        return str;
    },
    preventJS: function(html) {
        return html.replace(/<script(?=(\s|>))/i, '<script type="text/xml" ');
    },
    escapeHTML: function(c) {
        if (c === null || c === undefined) {
            return '';
        } else {
            var span = $('<span></span>').text(c);
            return span.html();
        }
    },
    getVariableOptions : function(properties) {
        try {
            var model = ProcessBuilder.Designer.model;
            var currentProcessDefId = ProcessBuilder.Designer.currentProcessDefId;
            var currentProcess = model.processes[currentProcessDefId];
            var variables = new Array();
            var empty = new Object();
            empty.value = "";
            empty.label = "";
            variables.push(empty);
            for (var df=0; df<currentProcess.dataFields.length; df++) {
                var dataField = currentProcess.dataFields[df];
                var variable = new Object();
                variable.value = dataField.variableId;
                variable.label = dataField.variableId;
                variables.push(variable);
            }
            return variables;
        } catch (err) {};

        return null;
    },
    adjustCanvasPosition : function() {
        var top = $("#header").height() + 15;
        $("#viewport").css("top", top + "px");
    },
    jsPlumb: jsPlumb,
    undoManager: new UndoManager()
};

/* Utility Functions */
ProcessBuilder.ApiClient = {
    baseUrl: "/jw",
    designerBaseUrl: "/jw",
    appId: "",
    appVersion: "",
    appName: null,
    httpGet: function(url, callback) {
        var loading = $('<div id="loading"><i class="icon-spinner icon-spin icon-2x"></i> ' + get_pbuilder_msg("pbuilder.label.loading") + '</div>');
        $("body").append(loading);
        var connCallback = {
            success: function(data) {
                $("#loading").remove();
                if (callback.success) {
                    callback.success.call(this, data);
                }
            },
            error: function(data) {
                $("#loading").remove();
                if (callback.error) {
                    callback.error.call(this, data);
                }
            }
        };
        // make request
        ConnectionManager.get(url, connCallback);
    },
    httpPost: function(url, callback, params) {
        var loading = $('<div id="loading"><i class="icon-spinner icon-spin icon-2x"></i> ' + get_pbuilder_msg("pbuilder.label.processing") + '</div>');
        $("body").append(loading);
        var connCallback = {
            success: function(data) {
                $("#loading").remove();
                if (callback.success) {
                    callback.success.call(this, data);
                }
            },
            error: function(data) {
                $("#loading").remove();
                if (callback.error) {
                    callback.error.call(this, data);
                }
            }
        };
        ConnectionManager.post(url, connCallback, params);
    },
    httpPostMultipart: function(url, callback, params) {
        var loading = $('<div id="loading"><i class="icon-spinner icon-spin icon-2x"></i> ' + get_pbuilder_msg("pbuilder.label.processing") + '</div>');
        $("body").append(loading);
        var connCallback = {
            success: function(data) {
                $("#loading").remove();
                if (callback.success) {
                    callback.success.call(this, data);
                }
            },
            error: function(data) {
                $("#loading").remove();
                if (callback.error) {
                    callback.error.call(this, data);
                }
            }
        };
        var thisWindow = this;
        $.support.cors = true;
        $.ajax({
            type: 'POST',
            url: url,
            data: params,
            cache: false,
            processData: false,
            contentType: false,
            beforeSend: function (request) {
               request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
            },
            success: function(data) {
                connCallback.success.call(thisWindow, data);
            },
            error: function(data) {
                try {
                    // do nothing for now
                    if (connCallback.error) {
                        connCallback.error.call(thisWindow, data);
                    }
                }
                catch (e) {
                }
            }
        });
    },
    list: function(packageList) {
        if (ProcessBuilder.Designer.isModified()) {
            if (!confirm(get_pbuilder_msg("pbuilder.label.confirmLoad"))) {
                return;
            }
        }
        var url = ProcessBuilder.ApiClient.baseUrl + "/web/json/workflow/package/list";
        var loginCallback = function() {
            $("#loginForm").dialog("close");
            ProcessBuilder.ApiClient.list(packageList);
        };
        var callback = {
            success: function(data) {
                try {
                    // parse data
                    var obj = JSON.decode(data);
                    // add containers
                    packageList = packageList || "#packageList";
                    var $packageListDiv = $(packageList);
                    var $packageUl = $("<ul></ul>");
                    if ($packageListDiv.length === 0) {
                        $packageListDiv = $('<div id="packageList"></div>');
                        $packageListDiv.append($('<div id="packageListHeader"></div>'));
                        $packageListDiv.append($packageUl);
                        $(document.body).append($packageListDiv);
                        // add list filter
                        (function($) {
                            jQuery.expr[':'].Contains = function(a, i, m) {
                                return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
                            };

                            function listFilter(header, list) {
                                var form = $("<form>").attr({"class": "filterform", "action": "#", "onsubmit": "return false"}),
                                input = $("<input>").attr({"class": "filterinput", "type": "text"});
                                $(form).append(input).append($("<span class='filterlabel'><i class='icon-search'></i></span>")).appendTo(header);
                                $(input).change(function() {
                                    var filter = $(this).val();
                                    if (filter) {
                                        $(list).find("a:not(:Contains(" + filter + "))").parent().slideUp();
                                        $(list).find("a:Contains(" + filter + ")").parent().slideDown();
                                    } else {
                                        $(list).find("li").slideDown();
                                    }
                                    return false;
                                }).keyup(function() {
                                    $(this).change();
                                });
                            }
                            listFilter($("#packageListHeader"), $("#packageList ul"));
                        }(jQuery));
                    }
                    $packageUl.empty();
                    // add list items
                    for (var p=0; p<obj.data.length; p++) {
                        var package = obj.data[p];
                        var $packageLink = $('<a href="#" onclick="return false" id="a_' + package.packageId + '">' + package.packageName + '</a>');
                        var $packageLi = $('<li id="li_' + package.packageId + '"></li>');
                        $packageLi.append($packageLink);
                        $packageUl.append($packageLi);
                        $packageLink.click(function() {
                            var packageId = $(this).attr("id").substring("a_".length);
                            ProcessBuilder.ApiClient.load(packageId);
                        });
                    }
                    // show dialog
                    $packageListDiv.dialog({title: 'Apps', width: 480, modal: true, height: 300});
                } catch(e) {
                    // invalid login
                    ProcessBuilder.ApiClient.showLogin(loginCallback);
                }
            },
            error: function(data) {
                alert(get_pbuilder_msg("pbuilder.label.invalidLogin"));
                ProcessBuilder.ApiClient.showLogin(loginCallback);
            }
        };
        ProcessBuilder.ApiClient.httpGet(url, callback);
    },
    load: function(appId, version, callback) {
        if (!appId || appId === '') {
            alert(get_pbuilder_msg("pbuilder.label.invalidApp"));
            return;
        }
        var loadUrl = ProcessBuilder.ApiClient.baseUrl + "/web/json/console/app/" + appId + "/" + version + "/package/xpdl?_=" + jQuery.now();
        var loadCallback = {
            success: function(data) {
                if (data.indexOf("loginForm") > 0) {
                    // handle login
                    var loginCallback = function() {
                        $("#loginForm").dialog("close");
                        ProcessBuilder.ApiClient.load(appId, version);
                    };
                    ProcessBuilder.ApiClient.showLogin(loginCallback);
                    return;
                }
                var xpdl = data;
                ProcessBuilder.Designer.init(xpdl);
                $("#packageList").dialog("close");
                ProcessBuilder.Actions.clearUndo();
                ProcessBuilder.ApiClient.appId = appId;
                ProcessBuilder.ApiClient.appVersion = (version) ? version: "";
                ProcessBuilder.ApiClient.appName = null;
                // set title
                document.title = document.title.substring(0, document.title.indexOf(":")) + ": " + UI.escapeHTML(ProcessBuilder.Designer.model.packageName);
                // callback
                if (callback) {
                    callback();
                }
            },
            error: function(e) {
                alert(get_pbuilder_msg("pbuilder.label.error") + ": " + e);
            }
        };
        ProcessBuilder.ApiClient.httpGet(loadUrl, loadCallback);
    },
    saveScreenshots: function(callback, show) {
        var packageId = ProcessBuilder.Designer.model.packageId;
        var processes = ProcessBuilder.Designer.model.processes;
        var packageUrl = ProcessBuilder.ApiClient.baseUrl + "/web/json/workflow/process/list?packageId=" + packageId;
        var processCount = 0;
        var processCounter = 0;
        var processCallback = {
            success: function(data) {
                var obj = JSON.decode(data);
                var process;
                if (obj.total > 1) {
                    process = obj.data[0];
                    processCount = obj.data.length;
                } else {
                    process = obj.data;
                    processCount = 1;
                }
                var processVersion = process.version;
                var currentProcessDefId = ProcessBuilder.Designer.currentProcessDefId;
                for (var processId in processes) {
                    (function() {
                        if (processId !== ProcessBuilder.Designer.currentProcessDefId) {
                            ProcessBuilder.Actions.viewProcess(processId);
                        }
                        var processDefId = packageId + "#" + processVersion + "#" + processId;
                        var saveUrl = ProcessBuilder.ApiClient.designerBaseUrl + "/web/console/app/" + ProcessBuilder.ApiClient.appId + "/" + ProcessBuilder.ApiClient.appVersion + "/process/builder/screenshot/submit?processDefId=" + encodeURIComponent(processDefId);
                        var screenshotCallback = function(imgData) {
                            var image = new Blob([imgData], {type : 'text/plain'});
                            var params = new FormData();
                            params.append("xpdlimage", image);
                            $.ajax({
                                type: "POST",
                                url: saveUrl,
                                data: params,
                                cache: false,
                                processData: false,
                                contentType: false,
                                beforeSend: function (request) {
                                   request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                                },
                                success: function() {
                                },
                                error: function(e) {
                                    alert(get_pbuilder_msg("pbuilder.label.errorSaving") + ": " + processDefId);
                                },
                                complete: function() {
                                    processCounter++;
                                    if (processCounter === processCount) {
                                        if (callback) {
                                            callback();
                                        } else {
                                            alert(get_pbuilder_msg("pbuilder.label.screenshotsSaved") + ": " + processCounter);
                                        }
                                    }
                                }
                            });
                        };
                        ProcessBuilder.Designer.screenshot(screenshotCallback, show);
                    })();
                }
                ProcessBuilder.Actions.viewProcess(currentProcessDefId);
            },
            error: function(e) {
                alert(get_pbuilder_msg("pbuilder.label.errorSavingScreenshot") + ": " + e);
            }
        };
        ProcessBuilder.ApiClient.httpGet(packageUrl, processCallback);
    },
    deploy: function() {
        if (typeof FormData === "undefined" || typeof Blob === "undefined") {
            alert(get_pbuilder_msg("pbuilder.label.browserNotSupported"));
            return;
        }
        if (!ProcessBuilder.Designer.validate()) {
            alert(get_pbuilder_msg("pbuilder.label.designInvalid"));
            return;
        } else if (ProcessBuilder.Designer.isModified()) {
            if (!confirm(get_pbuilder_msg("pbuilder.label.confirmDeployment"))) {
                return;
            }
        } else {
            alert(get_pbuilder_msg("pbuilder.label.designNotModified"));
            return;
        }
        var packageId = ProcessBuilder.Designer.model.packageId;
        var deployUrl = ProcessBuilder.ApiClient.baseUrl + "/web/json/console/app/" + ProcessBuilder.ApiClient.appId + "/"+ProcessBuilder.ApiClient.appVersion+"/package/deploy";
        var xpdl = new Blob([ProcessBuilder.Designer.xpdl], {type : 'text/xml'});
        var params = new FormData();
        params.append("packageXpdl", xpdl);
        var loginCallback = function() {
            $("#loginForm").dialog("close");
            ProcessBuilder.ApiClient.deploy();
        };
        var deployCallback = {
            success: function(data) {
                if (data && data.status === "complete") {
                    ProcessBuilder.Designer.originalXpdl = ProcessBuilder.Designer.xpdl;
                    alert(get_pbuilder_msg("pbuilder.label.deploymentSuccessful"));
                    // don't generate screenshot here, as it will be generated on-demand later
                    /*
                    var loading = $('<div id="loading"><i class="icon-spinner icon-spin icon-2x"></i> ' + get_pbuilder_msg("pbuilder.label.generating") + '</div>');
                    $("body").append(loading);
                    setTimeout(function() {
                        $("#loading").remove();
                        ProcessBuilder.ApiClient.saveScreenshots(function() {
                        });
                    }, 3000);
                    */
                } else if (data && data.errorMsg) {
                    // show only ERROR messages, ignore WARNING messages
                    var errorMsg = "";
                    var errorMsgArray = data.errorMsg.split("\r\n\r\n");
                    if (errorMsgArray.length <= 1) {
                        errorMsg = data.errorMsg;
                    } else {
                        for (var i=0; i<errorMsgArray.length; i++) {
                            var error = errorMsgArray[i];
                            if (error.indexOf("Type=ERROR") >= 0) {
                                errorMsg += error + "\r\n\r\n";
                            }
                        }
                    }
                    alert(get_pbuilder_msg("pbuilder.label.deploymentError") + ": " + errorMsg);
                } else if (!data || data.indexOf("Login") > 0) {
                    ProcessBuilder.ApiClient.showLogin(loginCallback);
                } else {
                    alert(get_pbuilder_msg("pbuilder.label.deploymentUnsuccessful"));
                }
            },
            error: function(e) {
                alert(get_pbuilder_msg("pbuilder.label.deploymentError") + ": " + e);
            }
        };
        ProcessBuilder.ApiClient.httpPostMultipart(deployUrl, deployCallback, params);
    },
    showLogin: function(callback) {
        var loginForm = "#loginForm";
        var $login = $(loginForm);
        if ($login.length === 0) {
            $login = $('<form id="loginForm"><dl><dt><label for="username">' + get_pbuilder_msg("pbuilder.label.username") + '</label></dt><dd><input name="username"/></dd><dt><label for="password">' + get_pbuilder_msg("pbuilder.label.password") + '</label></dt><dd><input type="password" name="password"/></dd></dl><p><button id="loginButton">' + get_pbuilder_msg("pbuilder.label.login") + '</button></p></form>');
            $(document.body).append($login);
            $(function() {
                var doLogin = function() {
                    var username = $("input[name=username]").val();
                    var password = $("input[name=password]").val();
                    ProcessBuilder.ApiClient.login(username, password, callback);
                    return false;
                };
                $("#loginForm").submit(doLogin);
                $("#loginButton").click(doLogin);
            });
        }
        $login.dialog({title: get_pbuilder_msg("pbuilder.label.login"), width: 480, modal: true});
    },
    login: function(username, password, callback) {
        var loginUrl = ProcessBuilder.ApiClient.baseUrl + "/web/json/directory/user/sso";
        var params = "username=" + encodeURIComponent(username) + "&password=" + encodeURIComponent(password);
        var loginCallback = {
            success: function() {
                callback.apply();
            },
            error: function() {
                alert(get_pbuilder_msg("pbuilder.label.invalidLogin"));
            }
        };
        ProcessBuilder.ApiClient.httpPost(loginUrl, loginCallback, params);
    }
};

/* Actions */
ProcessBuilder.Actions = {
    undoRedoInProgress: false,
    propertyEditor: null,
    editProperties: function(model) {
        var propertyOptions = model.propertyOptions();
        if (propertyOptions) {
            if (!ProcessBuilder.Actions.propertyEditor) {
                ProcessBuilder.Actions.propertyEditor = new Boxy('<div class="property-editor"></div>', {title:'&nbsp;', closeable:false, draggable:true, show:false, fixed:false});
            }
            var saveCallback = function(container, properties) {
                ProcessBuilder.Actions.execute(function() {
                    // update model
                    var error = model.propertyUpdate(properties);
                    if (error !== true) {
                        // re-init and show current process
                        var currentProcessDefId = ProcessBuilder.Designer.currentProcessDefId;
                        var xpdl = ProcessBuilder.Designer.generateXPDL();
                        ProcessBuilder.Designer.init(xpdl, currentProcessDefId);
                        ProcessBuilder.Designer.refresh();
                        // hide property editor
                        ProcessBuilder.Actions.propertyEditor.hide();
                    }
                });
            };
            var validationFailedCallback = function() {
            };
            var cancelCallback = function() {
                ProcessBuilder.Actions.propertyEditor.hide();
            };
            var options = {
                contextPath: ProcessBuilder.Designer.contextPath,
                tinyMceScript: ProcessBuilder.Designer.tinymceUrl,
                propertiesDefinition: propertyOptions,
                propertyValues: model,
                showCancelButton: true,
                closeAfterSaved: false,
                saveCallback: saveCallback,
                validationFailedCallback: validationFailedCallback,
                cancelCallback: cancelCallback
            };
            $('.property-editor').html("");
            $('.property-editor').attr('id', model.id);
            ProcessBuilder.Actions.propertyEditor.show();
            $('.property-editor').propertyEditor(options);
            $('.property-editor').find(".property-editor-container").css("width", "680px");
            $('.property-editor').find(".property-editor-container").css("max-height", "495px");
            var newHeight = $('.property-editor').find(".property-editor-container").height() - 100;
            $('.property-editor').find(".property-editor-property-container").css("height", newHeight + "px");
            ProcessBuilder.Actions.propertyEditor.center('x');
            ProcessBuilder.Actions.propertyEditor.center('y');
        }
    },
    addTransition: function(source, target, connection) {
        var $source = $(source);
        var $target = $(target);
        var sourceId = $source.attr("id").substring("node_".length);
        var targetId = $target.attr("id").substring("node_".length);
        // update model
        var currentProcessDefId = ProcessBuilder.Designer.currentProcessDefId;
        var model = ProcessBuilder.Designer.model;
        var process = model.processes[currentProcessDefId];
        var transition = (connection) ? connection.model : null;
        // update start end nodes
        if ($source.hasClass("start")) {
            var startId = targetId;
            var prevStartId = $source.attr("id");
            var newStartId = "start_" + startId;
            $source[0].model.id = startId;
            ProcessBuilder.Util.jsPlumb.setId($source, newStartId);
            var prevStartNode = process.startEndNodes[prevStartId];
            delete process.startEndNodes[prevStartId];
            process.startEndNodes[newStartId] = prevStartNode;
            connection.setPaintStyle({strokeStyle: "#000", lineWidth: 1});
            connection.setHoverPaintStyle({strokeStyle: "#000", lineWidth: 4});
        } else if ($target.hasClass("end")) {
            var endId = sourceId;
            var prevEndId = $target.attr("id");
            var newEndId = "end_" + endId;
            $target[0].model.id = endId;
            ProcessBuilder.Util.jsPlumb.setId($target, newEndId);
            var prevEndNode = process.startEndNodes[prevEndId];
            delete process.startEndNodes[prevEndId];
            process.startEndNodes[newEndId] = prevEndNode;
            connection.setPaintStyle({strokeStyle: "#000", lineWidth: 1});
            connection.setHoverPaintStyle({strokeStyle: "#000", lineWidth: 4});
        } else {
            // add transition
            var prevTransition;
            if (transition) {
                prevTransition = transition;
                // remove existing transition
                var prevSource = process.activities[transition.from];
                var prevTarget = process.activities[transition.to];
                ProcessBuilder.Actions.deleteTransition(prevSource, prevTarget, connection);
            }
            transition = new ProcessBuilder.Model.Transition();
            transition.name = "";
            process.transitions.push(transition);
            var count = $(".transition_label").length;
            var transitionId = "transition"+count;
            while ($("#transition_"+transitionId).length > 0) {
                count++;
                transitionId = "transition"+count;
            }
            transition.id = transitionId;
            transition.from = sourceId;
            transition.to = targetId;
            transition.process = process;
            if (prevTransition) {
                transition.name = prevTransition.name;
                transition.type = prevTransition.type;
                transition.style = prevTransition.style;
                transition.condition = prevTransition.condition;
                transition.conditions = prevTransition.conditions;
                transition.conditionHelper = prevTransition.conditionHelper;
                transition.exceptionName = prevTransition.exceptionName;
            }
            if (connection) {
                connection.model = transition;
            }
            // add join/split
            var source = process.activities[sourceId];
            var target = process.activities[targetId];
            if (!source.splitTransitions) {
                source.splitTransitions = [];
            }
            source.splitTransitions.push(transitionId);
            //set split type
            if (source.split === '' && source.splitTransitions.length > 1) {
                if (source.class === 'route') {
                    source.split = "XOR";
                } else {
                    source.split = "AND";
                }
            }
            if (!target.joinTransitions) {
                target.joinTransitions = [];
            }
            target.joinTransitions.push(transitionId);
            //set Join type
            //set split type
            if (target.join === '' && target.joinTransitions.length > 1) {
                target.join = "XOR";
            }
        }
        // update xpdl
        ProcessBuilder.Designer.refresh();
    },
    deleteTransition: function(source, target, connection) {
        var transition = connection.model;
        var currentProcessDefId = ProcessBuilder.Designer.currentProcessDefId;
        var model = ProcessBuilder.Designer.model;
        var process = model.processes[currentProcessDefId];
        for (var t=0; t<process.transitions.length; t++) {
            var pt = process.transitions[t];
            if (pt === transition) {
                if (transition.from) {
                    var fromNode = process.activities[transition.from];
                    var indexOf = fromNode.splitTransitions.indexOf(transition.id);
                    if (indexOf >= 0) {
                        fromNode.splitTransitions.splice(indexOf, 1);
                    }
                    if (fromNode.splitTransitions.length === 1) {
                        fromNode.split = "";
                    }
                }
                if (transition.to) {
                    var toNode =  process.activities[transition.to];
                    var indexOf = toNode.joinTransitions.indexOf(transition.id);
                    if (indexOf >= 0) {
                        toNode.joinTransitions.splice(indexOf, 1);
                    }
                    if (toNode.joinTransitions.length === 1) {
                        fromNode.join = "";
                    }
                }
                process.transitions.splice(t, 1);
                break;
            }
        }
        // remove related start end nodes
        if (source !== null && $(source).hasClass("start")) {
            var startEndNodes = process.startEndNodes;
            for (var startEndId in startEndNodes) {
                if (startEndId === $(source).attr("id")) {
                    delete startEndNodes[startEndId];
                }
            }
        }
        if (target !== null && $(target).hasClass("end")) {
            var startEndNodes = process.startEndNodes;
            for (var startEndId in startEndNodes) {
                if (startEndId === $(target).attr("id")) {
                    delete startEndNodes[startEndId];
                }
            }
        }

        // update xpdl
        ProcessBuilder.Designer.refresh();

        // remove start end elements only after a delay to prevent jsplumb redraw problem
        setTimeout(function() {
            if ($(source).hasClass("start")) {
                ProcessBuilder.Util.jsPlumb.remove($(source));
            }
            if ($(target).hasClass("end")) {
                ProcessBuilder.Util.jsPlumb.remove($(target));
            }
        }, 10);
    },
    updateTransition: function(transition, properties) {
        transition.name = properties.name;
        transition.type = properties.type;
        transition.style = properties.style;
        if (transition.type === "CONDITION") {
            if (properties.conditionHelper === "yes") {
                transition.conditions = properties.conditions;
                transition.condition = ProcessBuilder.Designer.buildConditions(properties.conditions);
            } else {
                if (transition.conditions  !== undefined) {
                    delete transition.conditions;
                }
                transition.condition = properties.condition;
            }
            transition.exceptionName = "";
        } else if (transition.type === "EXCEPTION") {
            transition.condition = "";
            transition.exceptionName = properties.exceptionName;
        } else {
            transition.condition = "";
            transition.exceptionName = "";
        }
        ProcessBuilder.Designer.refresh();
    },
    addNode: function(element, participant, top, left) {
        var $element = $(element);
        var $participant = $(participant);

        // remove palette classes
        $element.removeClass("palette_node");
        if ($element.hasClass("palette_start")) {
            $element.removeClass("palette_start");
            $element.addClass("start");
            $element.text("");
        } else if ($element.hasClass("palette_end")) {
            $element.removeClass("palette_end");
            $element.addClass("end");
            $element.text("");
        } else {
            $element.addClass("node");
        }
        $element.css("position", "absolute");

        // add node to participant element
        var zoom = ProcessBuilder.Designer.zoom;
        ProcessBuilder.Designer.setZoom(1);
        $participant.append($element);

        // calculate offsets to handle chrome and firefox
        var newLeft = Math.round(left / zoom);
        newLeft = newLeft - Math.round(($element.width()) / zoom);
        var newTop = Math.round(top);
        newTop = (newTop - Math.round(($element.height()))) * zoom;
        $element.offset({left: newLeft, top: newTop});

        // update model
        var nodeName = $element.find(".node_label").text();
        var performer = $participant[0].model.id;
        var currentProcessDefId = ProcessBuilder.Designer.currentProcessDefId;
        var model = ProcessBuilder.Designer.model;
        var process = model.processes[currentProcessDefId];
        var nodeId;
        var node;
        if ($element.hasClass("start")) {
            node = new ProcessBuilder.Model.StartEnd();
            node.type = "start";
            var nodeCount = $(".start." + node.type).length;
            nodeId = node.type + nodeCount;
            nodeName += " " + nodeCount;
            while (process.startEndNodes[nodeId]) {
                nodeId += "_1";
            }
        } else if ($element.hasClass("end")) {
            node = new ProcessBuilder.Model.StartEnd();
            node.type = "end";
            var nodeCount = $(".end." + node.type).length;
            nodeId = node.type + nodeCount;
            nodeName += " " + nodeCount;
            while (process.startEndNodes[nodeId]) {
                nodeId += "_1";
            }
        } else {
            if ($element.hasClass("tool")) {
                node = new ProcessBuilder.Model.Tool();
                node.type = "tool";
            } else if ($element.hasClass("route")) {
                node = new ProcessBuilder.Model.Route();
                node.type = "route";
            } else if ($element.hasClass("subflow")) {
                node = new ProcessBuilder.Model.Subflow();
                node.type = "subflow";
            } else {
                node = new ProcessBuilder.Model.Activity();
                node.type = "activity";
            }
            var nodeCount = $(".node." + node.type).length;
            nodeId = node.type + nodeCount;
            nodeName += " " + nodeCount;
            while (process.activities[nodeId]) {
                nodeId += "_1";
            }
        }
        
        if (node.type === "route" || node.type === "start" || node.type === "end") {
            nodeName = "";
        }
        
        node.id = nodeId;
        node.name = nodeName;
        node.performer = performer;
        node.process = process;
        node.join = "";
        node.split = "";
        node.x = $element.position().left / zoom;
        node.y = $element.position().top / zoom;
        if (node.class === "startend") {
            process.startEndNodes[node.type + "_" + nodeId] = node;
            $element.attr("id", node.type + "_" + nodeId);
        } else {
            process.activities[nodeId] = node;
            $element.attr("id", "node_" + nodeId);
        }
        $element[0].model = node;

        // init node elements
        $element.find(".node_label").text(nodeName);
        ProcessBuilder.Designer.initNodes($element);
        ProcessBuilder.Designer.setZoom(zoom);
        if (ProcessBuilder.Designer.editable) {
            ProcessBuilder.Designer.initEditable();
        }
    },
    moveNode: function(element, participant, top, left) {
        var $element = $(element);
        var zoom = ProcessBuilder.Designer.zoom;

        var $participant = $(participant);
        var newLeft = $element.offset().left;
        var newTop = $element.offset().top;
        if ($participant.find($element).length === 0) {
            $participant.append($element);
            $element.css("top", "0px"); // reset top to zero first to fix jquery positioning issue
            $element.offset({top: newTop, left: newLeft});
        }

        // update model
        var node = $element[0].model;
        node.x = $element.position().left / zoom;
        node.y = $element.position().top;
        var performer = $participant[0].model.id;
        node.performer = performer;
    },
    deleteNode: function(element) {
        var $element = $(element);

        // remove connections
        ProcessBuilder.Util.jsPlumb.detachAllConnections($element);

        // remove element
        ProcessBuilder.Util.jsPlumb.remove($element);

        // remove from model
        if ($element.length > 0) {
            var obj = $element[0].model;
            var process = obj.process;

            // remove node
            var id = obj.id;
            if (obj.class === 'startend') {
                delete process.startEndNodes[obj.type + "_" + obj.id];
            } else {
                delete process.activities[obj.id];
                // remove transitions
                for (var t=0; t<process.transitions.length; t++) {
                    var transition = process.transitions[t];
                    if (transition.from === id || transition.to === id) {
                        process.transitions.splice(t, 1);
                    }
                }
            }

            // remove start end nodes
            if (obj.class !== 'startend') {
                ProcessBuilder.Util.jsPlumb.remove($("#start_" + id + ", #end_" + id));
                var startEndNodes = process.startEndNodes;
                for (var startEndId in startEndNodes) {
                    if (startEndId === "start_" + id) {
                        delete startEndNodes[startEndId];
                    }
                    if (startEndId === "end_" + id) {
                        delete startEndNodes[startEndId];
                    }
                }
            }
        }

        // update xpdl
        ProcessBuilder.Designer.refresh();
    },
    updateNode: function(node, properties) {
        var currentId = node.id;
        var newId = properties.id;
        // check for modified ID
        if (newId !== currentId) {
//            // check for duplicate in all processes
//            var processes = ProcessBuilder.Designer.model.processes;
//            for (var processId in processes) {
//                var proc = processes[processId];
//                var act = proc.activities[newId];
//                if (act) {
//                    alert(get_pbuilder_msg("pbuilder.label.duplicateId"));
//                    return true;
//                }
//            }
            // check for duplicate only within the current process
            var proc = ProcessBuilder.Designer.model.processes[ProcessBuilder.Designer.currentProcessDefId];
            var act = proc.activities[newId];
            if (act) {
                alert(get_pbuilder_msg("pbuilder.label.duplicateId"));
                return true;
            }

            // replace current activity
            var process = node.process;
            delete process.activities[currentId];
            process.activities[newId] = node;

            // replace all transitions
            for (var t=0; t<process.transitions.length; t++) {
                var transition = process.transitions[t];
                if (transition.to === currentId) {
                    transition.to = newId;
                }
                if (transition.from === currentId) {
                    transition.from = newId;
                }
            }

            // replace startend node transitions
            var startEndNodes = process.startEndNodes;
            for (var startEndId in startEndNodes) {
                var startEnd = startEndNodes[startEndId];
                if (startEndId === "start_" + currentId) {
                    startEnd.id = newId;
                    startEndNodes["start_" + startEnd.id] = startEnd;
                    delete startEndNodes[startEndId];
                }
                if (startEndId === "end_" + currentId) {
                    startEnd.id = newId;
                    startEndNodes["end_" + startEnd.id] = startEnd;
                    delete startEndNodes[startEndId];
                }
            }
        }
        // update properties
        node.id = properties.id;
        node.name = properties.name;
        node.subflowId = properties.subflowId;
        node.execution = properties.execution;
        node.actualParameters = properties.actualParameters;
        node.limit = properties.limit;
        node.deadlines = properties.deadlines;
        node.join = properties.join;
        node.split = properties.split;
    },
    addParticipant: function(participant) {
        var $participant = $(participant);

        // update model
        var currentProcessDefId = ProcessBuilder.Designer.currentProcessDefId;
        var model = ProcessBuilder.Designer.model;
        var process = model.processes[currentProcessDefId];
        var participantId = $participant.attr("id");
        if (!participantId) {
            participantId = "participant" + $(".participant:not(.palette_participant)").length;
        }
        while (model.participants[participantId]) {
            participantId += "_1";
        }
        $participant.attr("id", "participant_" + participantId);
        var swimlanes = "";
        $(".participant").each(function(index) {
            var performerId = $(this).attr("id");
            if (performerId) {
                performerId = performerId.substring("participant_".length);
                if (index > 0) {
                    swimlanes += ";";
                }
                swimlanes += performerId;
            }
        });
        process.swimlanes = swimlanes;
        var participant = new ProcessBuilder.Model.Participant();
        participant.id = participantId;
        participant.name = $participant.find(".participant_label").text();
        model.participants[participantId] = participant;
        $participant[0].model = participant;
        $participant[0].process = process;

        // reinit swimlanes
        ProcessBuilder.Designer.initParticipants();

        // update xpdl
        ProcessBuilder.Designer.refresh();
    },
    moveParticipant: function() {
        // reinit swimlanes
        ProcessBuilder.Designer.initParticipants();

        // update model
        var swimlanes = "";
        $(".participant").each(function(index) {
            var participantId = $(this).attr("id");
            if (participantId) {
                participantId = participantId.substring("participant_".length);
                if (index > 0) {
                    swimlanes += ";";
                }
                swimlanes += participantId;
            }
        });
        var currentProcessDefId = ProcessBuilder.Designer.currentProcessDefId;
        var model = ProcessBuilder.Designer.model;
        var process = model.processes[currentProcessDefId];
        process.swimlanes = swimlanes;

        // update xpdl
        ProcessBuilder.Designer.refresh();
    },
    deleteParticipant: function(participant) {
        var $participant = $(participant);
        $participant.find(".node").each(function() {
            var $node = $(this);
            // remove connections
            ProcessBuilder.Util.jsPlumb.detachAllConnections($node);

            // remove element
            $node.remove();
        });

        // remove participant
        $participant.remove();

        // update model
        var performer = $participant[0].model;
        var process = performer.process;
        if (process) {
            // remove process swimlane
            var swimlanes = process.swimlanes;
            var newSwimlanes = "";
            if (swimlanes) {
                var swimlanes = swimlanes.split(";");
                for (var i = 0; i < swimlanes.length; i++) {
                    var performerId = swimlanes[i];
                    if (performerId !== performer.id) {
                        if (i > 0) {
                            newSwimlanes += ";";
                        }
                        newSwimlanes += performerId;
                    }
                }
            }
            process.swimlanes = newSwimlanes;
            // delete related activities, transitions and startend nodes
            var activities = process.activities;
            for (var actId in activities) {
                var act = activities[actId];
                if (act.performer === performer.id) {
                    delete process.activities[actId];
                }
            }
            var startEndNodes = process.startEndNodes;
            for (var startendId in startEndNodes) {
                var startend = startEndNodes[startendId];
                if (startend.performer === performer.id) {
                    delete process.startEndNodes[startendId];
                }
            }
        }

        // delete participant from package if not used in any process
        var participantUsed = false;
        var model = ProcessBuilder.Designer.model;
        var processes = model.processes;
        for (var processId in processes) {
            var proc = processes[processId];
            if (proc.swimlanes) {
                var sw = proc.swimlanes.split(";");
                for (var i = 0; i < sw.length; i++) {
                    var performerId = sw[i];
                    if (performerId === performer.id) {
                        participantUsed = true;
                        break;
                    }
                }
            }
        }
        if (!participantUsed) {
            delete model.participants[performer.id];
        }

        // update xpdl
        ProcessBuilder.Designer.refresh();
    },
    updateParticipant: function(participant, properties) {
        var currentId = participant.id;
        var newId = properties.id;
        // check for modified ID
        if (newId !== currentId) {
            // check for duplicate
            var part = ProcessBuilder.Designer.model.participants[newId];
            if (part) {
                alert(get_pbuilder_msg("pbuilder.label.duplicateId"));
                return true;
            }

            // remove current participant
            delete ProcessBuilder.Designer.model.participants[currentId];

            // replace all performers
            var processes = ProcessBuilder.Designer.model.processes;
            for (var processId in processes) {
                var process = processes[processId];
                // replace swimlanes
                var newSwimlanes = "";
                var swimlanes = process.swimlanes.split(";");
                for (var i = 0; i < swimlanes.length; i++) {
                    var performerId = swimlanes[i];
                    if (i > 0) {
                        newSwimlanes += ";";
                    }
                    if (performerId !== currentId) {
                        newSwimlanes += performerId;
                    } else {
                        newSwimlanes += newId;
                    }
                }
                process.swimlanes = newSwimlanes;
                // replace activity performers
                var activities = process.activities;
                for (var activityId in activities) {
                    var activity = activities[activityId];
                    if (activity.performer === currentId) {
                        activity.performer = newId;
                    }
                }
                // replace startend node performers
                var startEndNodes = process.startEndNodes;
                for (var startEndId in startEndNodes) {
                    var startEnd = startEndNodes[startEndId];
                    if (startEnd.performer === currentId) {
                        startEnd.performer = newId;
                    }
                }
            }
        }
        // update properties
        participant.id = properties.id;
        participant.name = properties.name;
        ProcessBuilder.Designer.model.participants[newId] = participant;
    },
    attachTransitionEvent: function (transition, connection) {
        var transitionId = "transition_" + transition.id;
        $("#" + transitionId).find(".transition_edit, .transition_delete").off("click");
        $("#" + transitionId).find(".transition_edit").on("click", function(e) {
            var transId = $(this).parent().attr("id").substring("transition_".length);
            var transitions = ProcessBuilder.Designer.model.processes[ProcessBuilder.Designer.currentProcessDefId].transitions;
            var selectedTransition;
            for (var t = 0; t < transitions.length; t++) {
                var ts = transitions[t];
                if (ts.id === transId) {
                    selectedTransition = ts;
                }
            }
            if (selectedTransition) {
                selectedTransition.condition = ProcessBuilder.Util.decodeXML(selectedTransition.condition);
                ProcessBuilder.Actions.editProperties(selectedTransition);
            }
        });
        $("#" + transitionId).find(".transition_delete").on("click", function(e) {
            ProcessBuilder.Actions.execute(function() {
                ProcessBuilder.Actions.deleteTransition(null, null, connection);
                ProcessBuilder.Util.jsPlumb.detach(connection);
            });
        });
        if (ProcessBuilder.Designer.editable) {
            $(connection.canvas).next(".transition_label").bind("click", function(e) {
                e.preventDefault();
                e.stopPropagation();
                var transId = $(this).find(".transition_editable").attr("id").substring("transition_".length);
                var transitions = ProcessBuilder.Designer.model.processes[ProcessBuilder.Designer.currentProcessDefId].transitions;
                var selectedTransition;
                for (var t = 0; t < transitions.length; t++) {
                    var ts = transitions[t];
                    if (ts.id === transId) {
                        selectedTransition = ts;
                    }
                }
                if (selectedTransition) {
                    selectedTransition.condition = ProcessBuilder.Util.decodeXML(selectedTransition.condition);
                    ProcessBuilder.Actions.editProperties(selectedTransition);
                }
            });
            $(connection.canvas).bind("click", function(e) {
                e.preventDefault();
                e.stopPropagation();
                var transId = $(this).next(".transition_label").find(".transition_editable").attr("id");
                if (transId) {
                    transId = transId.substring("transition_".length);
                }
                var transitions = ProcessBuilder.Designer.model.processes[ProcessBuilder.Designer.currentProcessDefId].transitions;
                var selectedTransition;
                for (var t = 0; t < transitions.length; t++) {
                    var ts = transitions[t];
                    if (ts.id === transId) {
                        selectedTransition = ts;
                    }
                }
                if (selectedTransition) {
                    selectedTransition.condition = ProcessBuilder.Util.decodeXML(selectedTransition.condition);
                    ProcessBuilder.Actions.editProperties(selectedTransition);
                }
            });
            connection.bind("mouseenter", function(connection) {
                $(connection.canvas).next(".transition_label").find(".transition_editable").addClass("hover");
            });
            connection.bind("mouseleave", function(connection) {
                $(".transition_editable").removeClass("hover");
            });
            // associate connection with transition model
            connection.model = transition;
        }
    },
    viewProcess: function(processId) {
        // render process
        var model = ProcessBuilder.Designer.model;
        if (typeof model.processes[processId] === "undefined") {
            return;
        }
        ProcessBuilder.Designer.renderModel(model, processId);

        // select process in header
        $("#subheader_list").removeClass("subheader_selected");
        $("#subheader_list").find("#" + processId).addClass("subheader_selected");

        // validate
        if (ProcessBuilder.Designer.autoValidate) {
            ProcessBuilder.Designer.validate();
        }
    },
    addProcess: function() {
        // create process
        var model = ProcessBuilder.Designer.model;
        var count = (Object.keys(model.processes).length + 1);
        var processId = "process" + count;
        while (model.processes[processId]) {
            processId += "_1";
        }
        var processName = get_pbuilder_msg("pbuilder.label.process") + " " + count;
        var process = new ProcessBuilder.Model.Process();
        process.id = processId;
        process.name = processName;
        // create participant
        var count = (Object.keys(model.participants).length + 1);
        var participantId = "participant" + count;
        while (model.participants[participantId]) {
            participantId += "_1";
        }
        var participant = new ProcessBuilder.Model.Participant();
        var participantName = get_pbuilder_msg("pbuilder.label.participant") + " " + count;
        participant.id = participantId;
        participant.name = participantName;
        model.participants[participantId] = participant;
        process.swimlanes = participantId;

        // add default "status" workflow variable
        var defaultVariableId = "status";
        var dataFields = new Array();
        var dataField = new Object();
        dataField.variableId = defaultVariableId;
        dataFields.push(dataField);
        process.dataFields = dataFields;

        // add process to model
        model.processes[processId] = process;

        // re-init and switch to process
        var xpdl = ProcessBuilder.Designer.generateXPDL();
        ProcessBuilder.Designer.init(xpdl);
        ProcessBuilder.Actions.viewProcess(processId);
        ProcessBuilder.Designer.refresh();

        // add a start node
        var $newNode = $('<div id="newNode" class="start"><div class="node_label">' + get_pbuilder_msg("pbuilder.label.start") + '</div></div>');
        $newNode.offset({left: 80, top: 40});
        ProcessBuilder.Actions.addNode($newNode, $(".participant:not(.palette_participant)"));
    },
    deleteProcess: function(processId) {
        var currentProcessId = ProcessBuilder.Designer.currentProcessDefId;

        // delete from model
        var model = ProcessBuilder.Designer.model;
        delete model.processes[processId];

        // switch to first process
        var processes = model.processes;
        if (Object.keys(processes).length > 0) {
            if (!model.processes[currentProcessId]) {
                var process = model.processes[Object.keys(processes)[0]];
                currentProcessId = process.id;
            }
            ProcessBuilder.Actions.viewProcess(currentProcessId);
        } else {
            // no process left, delete participants
            model.participants = new Object();
            delete ProcessBuilder.Designer.currentProcessDefId;

            // create a new one
            ProcessBuilder.Actions.addProcess();
        }
        ProcessBuilder.Designer.refresh();
    },
    duplicateProcess: function(processId) {
        var model = ProcessBuilder.Designer.model;
        var process = model.processes[processId];

        var count = (Object.keys(model.processes).length + 1);
        var newProcessId = "process" + count;
        while (model.processes[newProcessId]) {
            newProcessId += "_1";
        }

        var newProcess = new ProcessBuilder.Model.Process();
        newProcess.id = newProcessId;
        newProcess.name = process.name + get_pbuilder_msg("pbuilder.label.copy");
        newProcess.activities = process.activities;
        newProcess.dataFields = process.dataFields;
        newProcess.durationUnit = process.durationUnit;
        newProcess.formalParameters = process.formalParameters;
        newProcess.limit = process.limit;
        newProcess.startEndNodes = process.startEndNodes;
        newProcess.swimlanes = process.swimlanes;
        newProcess.transitions = process.transitions;

        model.processes[newProcessId] = newProcess;

        var xpdl = ProcessBuilder.Designer.generateXPDL();
        ProcessBuilder.Designer.init(xpdl, newProcessId);
        ProcessBuilder.Designer.refresh();

        ProcessBuilder.Actions.viewProcess(newProcessId);

        ProcessBuilder.Designer.refresh();
    },
    moveProcess: function() {

    },
    updateProcess: function(process, properties) {
        var currentId = process.id;
        var newId = properties.id;
        // check for modified ID
        if (newId !== currentId) {
            // check for duplicate
            var proc = ProcessBuilder.Designer.model.processes[newId];
            if (proc) {
                alert(get_pbuilder_msg("pbuilder.label.duplicateId"));
                return true;
            }

            // if currently viewing process, switch ID
            if (ProcessBuilder.Designer.currentProcessDefId === currentId) {
                ProcessBuilder.Designer.currentProcessDefId = newId;
            }

            // remove current process
            delete ProcessBuilder.Designer.model.processes[currentId];
        }
        // update properties
        process.id = properties.id;
        process.name = properties.name;
        process.durationUnit = properties.durationUnit;
        process.limit = properties.limit;
        process.dataFields = properties.dataFields;
        process.formalParameters = properties.formalParameters;
        ProcessBuilder.Designer.model.processes[newId] = process;
    },
    execute: function(action) {
        var isUndoRedoInProgress = ProcessBuilder.Actions.undoRedoInProgress;
        ProcessBuilder.Actions.undoRedoInProgress = true;

        // get previous xpdl
        var previousXpdl = ProcessBuilder.Designer.generateXPDL();
        var previousProcessId = ProcessBuilder.Designer.currentProcessDefId;

        // execute action
        action.apply(this);

        if (!isUndoRedoInProgress) {
            // get next xpdl
            var nextXpdl = ProcessBuilder.Designer.generateXPDL();
            var nextProcessId = ProcessBuilder.Designer.currentProcessDefId;

            // set undo and redo
            ProcessBuilder.Util.undoManager.add({
                undo: function() {
                    ProcessBuilder.Designer.init(previousXpdl, previousProcessId);
                },
                redo: function() {
                    ProcessBuilder.Designer.init(nextXpdl, nextProcessId);
                }
            });
        }
        ProcessBuilder.Actions.updateUndoRedo();
        ProcessBuilder.Actions.undoRedoInProgress = isUndoRedoInProgress;
    },
    undo: function() {
        if (ProcessBuilder.Actions.undoRedoInProgress || !ProcessBuilder.Actions.hasUndo()) {
            return;
        }
        ProcessBuilder.Actions.undoRedoInProgress = true;
        var loading = $('<div id="loading"><i class="icon-spinner icon-spin icon-2x"></i> ' + get_pbuilder_msg("pbuilder.label.undoing") + '</div>');
        $("body").append(loading);
        setTimeout(function() {
            ProcessBuilder.Actions.undoRedoInProgress = true;
            ProcessBuilder.Util.undoManager.undo();
            ProcessBuilder.Designer.refresh();
            $("#loading").remove();
            ProcessBuilder.Actions.updateUndoRedo();
            ProcessBuilder.Actions.undoRedoInProgress = false;
        }, 1);
    },
    redo: function() {
        if (ProcessBuilder.Actions.undoRedoInProgress || !ProcessBuilder.Actions.hasRedo()) {
            return;
        }
        ProcessBuilder.Actions.undoRedoInProgress = true;
        var loading = $('<div id="loading"><i class="icon-spinner icon-spin icon-2x"></i> ' + get_pbuilder_msg("pbuilder.label.redoing") + '</div>');
        $("body").append(loading);
        setTimeout(function() {
            ProcessBuilder.Actions.undoRedoInProgress = true;
            ProcessBuilder.Util.undoManager.redo();
            ProcessBuilder.Designer.refresh();
            $("#loading").remove();
            ProcessBuilder.Actions.updateUndoRedo();
            ProcessBuilder.Actions.undoRedoInProgress = false;
        }, 1);
    },
    hasUndo: function() {
        return ProcessBuilder.Util.undoManager.hasUndo();
    },
    hasRedo: function() {
        return ProcessBuilder.Util.undoManager.hasRedo();
    },
    updateUndoRedo: function() {
        if (ProcessBuilder.Actions.hasUndo()) {
            $(".action-undo").css("opacity", "1.0");
        } else {
            $(".action-undo").css("opacity", "0.3");
        }
        if (ProcessBuilder.Actions.hasRedo()) {
            $(".action-redo").css("opacity", "1.0");
        } else {
            $(".action-redo").css("opacity", "0.3");
        }
    },
    clearUndo: function() {
        ProcessBuilder.Util.undoManager.clear();
        ProcessBuilder.Actions.updateUndoRedo();
    }
};

/* Designer settings and functions */
ProcessBuilder.Designer = {
    source: "#xpdl",
    editable: true,
    autoValidate: true,
    participantLabelVertical: true,
    minWidth: 640,
    zoom: 1,
    originalXpdl: null,
    xpdl: null,
    model: null,
    currentProcessDefId: null,
    isCtrlKeyPressed: false,
    isAltKeyPressed : false,
    refresh: function(delay) {
        ProcessBuilder.Util.adjustCanvasPosition();
        if (!delay) {
            delay = 100;
        } else if (delay < 0) {
            delay = 0;
        }
        setTimeout(function() {
            // refresh transitions
            ProcessBuilder.Util.jsPlumb.recalculateOffsets("canvas");
            ProcessBuilder.Util.jsPlumb.repaintEverything();

            // refresh xml
            var xpdl = ProcessBuilder.Designer.generateXPDL();

            // update xml
            ProcessBuilder.Designer.xpdl = xpdl;
            $(ProcessBuilder.Designer.source).val(xpdl);
            $(ProcessBuilder.Designer.source).format({method: 'xml'});

            // validate
            if (ProcessBuilder.Designer.autoValidate) {
                ProcessBuilder.Designer.validate();
            }
        }, delay);
    },
    clear: function() {
        ProcessBuilder.Util.jsPlumb.unbind();
        ProcessBuilder.Util.jsPlumb.detachEveryConnection();
        ProcessBuilder.Util.jsPlumb.reset();
        $("#canvas").empty();
    },
    setZoom: function(z) {
        var p = ["-webkit-", "-moz-", "-ms-", "-o-", ""],
                s = "scale(" + z + ")";

        for (var i = 0; i < p.length; i++) {
            $("#canvas").css(p[i] + "transform", s);
            $("#canvas").css(p[i] + "transition", p[i] + "transform .5s ease-in-out");
        }

        ProcessBuilder.Util.jsPlumb.setZoom(z);
        ProcessBuilder.Designer.zoom = z;
    },
    parseXPDL: function(xpdl) {
        var model = new Object();

        // get XPDL
        var xml = ProcessBuilder.Util.escapeXPDL(xpdl);

        // parse XPDL
        var xmlDoc = $.parseXML(xml);
        var $xml = $(xmlDoc);
        var $package = $xml.find("Package");

        // get app details
        var appId = $package.attr("Id");
        var appName = (ProcessBuilder.ApiClient.appName) ? ProcessBuilder.ApiClient.appName : $package.attr("Name");
        model.packageId = appId;
        model.packageName = appName;

        // get participants
        var participants = {};
        var $participantElements = $package.find("Participant");
        for (var i = 0; i < $participantElements.length; i++) {
            var $participantElement = $($participantElements[i]);
            var participantId = $participantElement.attr("Id");
            var participantName = $participantElement.attr("Name");
            var participantType = $participantElement.find("ParticipantType").attr("Type");
            var participant = new ProcessBuilder.Model.Participant();
            participant.id = participantId;
            participant.name = ProcessBuilder.Util.decodeXML(participantName);
            participant.type = participantType;
            participants[participantId] = participant;
        }
        model.participants = participants;

        // get processes
        var processes = {};
        var $processElements = $package.find("WorkflowProcess");
        for (var p = 0; p < $processElements.length; p++) {
            var $processElement = $($processElements[p]);
            var processId = $processElement.attr("Id");
            var processName = $processElement.attr("Name");
            var dataFields;
            var formalParameters;
            var process = new ProcessBuilder.Model.Process();
            process.id = processId;
            process.name = ProcessBuilder.Util.decodeXML(processName);
            processes[processId] = process;

            // get workflow variables
            var dataFields = new Array();
            var $dataFields = $processElement.find("DataField");
            $dataFields.each(function() {
                var $dataField = $(this);
                var dataFieldId = $dataField.attr("Id");
                var dataField = new Object();
                dataField.variableId = dataFieldId;
                dataFields.push(dataField);
            });
            process.dataFields = dataFields;

            // get SLA properties
            var durationUnit;
            var limit;
            var $processHeader = $processElement.find("ProcessHeader");
            if ($processHeader) {
                durationUnit = $processHeader.attr("DurationUnit");
                limit = $processHeader.find("Limit").text();
            }
            process.durationUnit = durationUnit;
            process.limit = limit;

            // get formal parameters
            var formalParameters = new Array();
            var $formalParameters = $processElement.find("FormalParameter");
            $formalParameters.each(function() {
                var $formalParameter = $(this);
                var parameterId = $formalParameter.attr("Id");
                var mode = $formalParameter.attr("Mode");
                var formalParameter = new Object();
                formalParameter.parameterId = parameterId;
                formalParameter.mode = mode;
                formalParameters.push(formalParameter);
            });
            process.formalParameters = formalParameters;

            // get activities
            process.activities = {};
            var $activityElements = $processElement.find("Activity");
            for (var i = 0; i < $activityElements.length; i++) {
                // get activity details
                var $activityElement = $($activityElements[i]);
                var activityId = $activityElement.attr("Id");
                var activityName = $activityElement.attr("Name");
                var subflowId = null;
                if ($activityElement.find("SubFlow").length > 0) {
                    subflowId = $activityElement.find("SubFlow").attr("Id");
                    if (subflowId === "") {
                        subflowId = "none";
                    }
                }
                var activity;
                var activityType = "activity";
                if ($activityElement.find("Route").length > 0) {
                    activity = new ProcessBuilder.Model.Route();
                    activityType = "route";
                } else if ($activityElement.find("Tool").length > 0) {
                    activity = new ProcessBuilder.Model.Tool();
                    activityType = "tool";
                } else if (subflowId) {
                    activity = new ProcessBuilder.Model.Subflow();
                    activityType = "subflow";
                } else {
                    activity = new ProcessBuilder.Model.Activity();
                    activityType = "activity";
                }
                var join = $activityElement.find("Join").length > 0 ? $activityElement.find("Join").attr("Type") : "";
                var split = $activityElement.find("Split").length > 0 ? $activityElement.find("Split").attr("Type") : "";
                var joinTransitions = [];
                var $joinTransitionRefs = $activityElement.find("Join").find("TransitionRef");
                if ($joinTransitionRefs.length > 0) {
                    $joinTransitionRefs.each(function(index) {
                        var transitionId = $(this).attr("Id");
                        joinTransitions[index] = transitionId;
                    });
                }
                var splitTransitions = [];
                var $splitTransitionRefs = $activityElement.find("Split").find("TransitionRef");
                if ($splitTransitionRefs.length > 0) {
                    $splitTransitionRefs.each(function(index) {
                        var transitionId = $(this).attr("Id");
                        splitTransitions[index] = transitionId;
                    });
                }
                var performer = $activityElement.find("ExtendedAttribute[Name='JaWE_GRAPH_PARTICIPANT_ID']").attr("Value");
                var offset = $activityElement.find("ExtendedAttribute[Name='JaWE_GRAPH_OFFSET']").attr("Value");
                var coords = offset.split(",");
                var activityX = coords[0];
                var activityY = coords[1];
                activity.id = activityId;
                activity.name = ProcessBuilder.Util.decodeXML(activityName);
                activity.type = activityType;
                activity.join = join;
                activity.split = split;
                activity.performer = performer;
                activity.subflowId = subflowId;
                activity.x = activityX;
                activity.y = activityY;
                activity.joinTransitions = joinTransitions;
                activity.splitTransitions = splitTransitions;
                activity.process = process;
                process.activities[activityId] = activity;

                // get limit
                var $limit = $activityElement.find("Limit");
                var limit = ($limit.length > 0) ? $limit.text() : null;
                activity.limit = limit;

                // get deadlines
                var deadlines = new Array();
                var $deadlines = $activityElement.find("Deadline");
                $deadlines.each(function() {
                    var $deadline = $(this);
                    var deadline = new Object();
                    var durationUnit;
                    var deadlineLimit;
                    deadline.execution = $deadline.attr("Execution");
                    deadline.exceptionName = $deadline.find("ExceptionName").text();
                    var deadlineCondition = $deadline.find("DeadlineCondition").text();
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
                        deadline.durationUnit = durationUnit;
                        deadline.deadlineLimit = deadlineLimit;
                    }
                    deadlines.push(deadline);
                });
                activity.deadlines = deadlines;

                // get subflow parameters
                var $subflow = $activityElement.find("SubFlow");
                if ($subflow.length > 0) {
                    var execution = $subflow.attr("Execution");
                    activity.execution = execution;
                    var actualParameters = new Array();
                    var $actualParameters = $subflow.find("ActualParameter");
                    $actualParameters.each(function() {
                        var $actualParameter = $(this);
                        var parameterId = $actualParameter.text();
                        var actualParameter = new Object();
                        actualParameter.actualParameter = parameterId;
                        actualParameters.push(actualParameter);
                    });
                    activity.actualParameters = actualParameters;
                }
            }

            // get transitions
            process.transitions = new Array();
            var $transitionElements = $processElement.find("Transition");
            for (var i = 0; i < $transitionElements.length; i++) {
                var $transitionElement = $($transitionElements[i]);
                var transitionId = $transitionElement.attr("Id");
                var transitionName = $transitionElement.attr("Name");
                var transitionFrom = $transitionElement.attr("From");
                var transitionTo = $transitionElement.attr("To");
                var transitionType = $transitionElement.find("Condition").length > 0 ? $transitionElement.find("Condition").attr("Type") : "";
                var transitionCondition = $transitionElement.find("Condition[Type='CONDITION']").length > 0 ? $transitionElement.find("Condition[Type='CONDITION']").text() : "";
                var transitionException = $transitionElement.find("Condition[Type='EXCEPTION']").length > 0 ? $transitionElement.find("Condition[Type='EXCEPTION']").text() : "";
                var transitionStyle = $transitionElement.find("ExtendedAttribute[Name='JaWE_GRAPH_BREAK_POINTS']").length > 0 ? "orthogonal" : "straight";
                var transitionConditions = $transitionElement.find("ExtendedAttribute[Name='PBUILDER_TRANSITION_CONDITIONS']").length > 0 ? $transitionElement.find("ExtendedAttribute[Name='PBUILDER_TRANSITION_CONDITIONS']").attr("Value") : null;
                var transition = new ProcessBuilder.Model.Transition();
                transition.id = transitionId;
                transition.name = ProcessBuilder.Util.decodeXML(transitionName);
                transition.from = transitionFrom;
                transition.to = transitionTo;
                transition.type = transitionType;
                transition.condition = ProcessBuilder.Util.decodeXML(transitionCondition);
                transition.exceptionName = ProcessBuilder.Util.decodeXML(transitionException);
                transition.style = transitionStyle;
                transition.process = process;
                if (transitionConditions !== null) {
                    transition.conditionHelper = "yes";
                    transitionConditions = ProcessBuilder.Util.decodeXML(transitionConditions);
                    transitionConditions = ProcessBuilder.Util.unescapeQuote(transitionConditions);
                    transition.conditions = JSON.decode(transitionConditions);
                }
                // add join and split transitions
                var joinActivity = process.activities[transitionTo];
                if (!joinActivity.joinTransitions || joinActivity.joinTransitions.length === 0) {
                    joinActivity.joinTransitions = [];
                }
                if (joinActivity.joinTransitions.indexOf(transitionId) < 0) {
                    joinActivity.joinTransitions.push(transitionId);
                }
                var splitActivity = process.activities[transitionFrom];
                if (!splitActivity.splitTransitions || splitActivity.splitTransitions.length === 0) {
                    splitActivity.splitTransitions = [];
                }
                if (splitActivity.splitTransitions.indexOf(transitionId) < 0) {
                    splitActivity.splitTransitions.push(transitionId);
                }
                process.transitions[i] = transition;
            }

            // get start and end nodes
            process.startEndNodes = {};
            var $startEndElements = $processElement.find("ExtendedAttribute[Name='JaWE_GRAPH_START_OF_WORKFLOW'], ExtendedAttribute[Name='JaWE_GRAPH_END_OF_WORKFLOW']");
            for (var i = 0; i < $startEndElements.length; i++) {
                // get node values
                var swimlane;
                var activityId;
                var xOffset;
                var yOffset;
                var $startEndElement = $($startEndElements[i]);
                var value = $startEndElement.attr("Value");
                var type = ($startEndElement.attr("Name") === "JaWE_GRAPH_START_OF_WORKFLOW") ? "start" : "end";
                var options = value.split(",");
                for (var j = 0; j < options.length; j++) {
                    var option = options[j];
                    var keyvalue = option.split("=");
                    var key = keyvalue[0];
                    var value = keyvalue[1];
                    if (key === "CONNECTING_ACTIVITY_ID") {
                        activityId = value;
                    } else if (key === "JaWE_GRAPH_PARTICIPANT_ID") {
                        swimlane = value;
                    } else if (key === "X_OFFSET") {
                        xOffset = value;
                    } else if (key === "Y_OFFSET") {
                        yOffset = value;
                    }
                }
                var startEnd = new ProcessBuilder.Model.StartEnd();
                startEnd.id = activityId;
                startEnd.type = type;
                startEnd.performer = swimlane;
                startEnd.x = xOffset;
                startEnd.y = yOffset;
                startEnd.process = process;
                process.startEndNodes[startEnd.type + "_" + activityId] = startEnd;
            }

            // get participant swimlanes
            var swimlanes = $processElement.find("ExtendedAttribute[Name='JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER']").attr("Value");
            process.swimlanes = swimlanes;
        }
        model.processes = processes;
        return model;
    },
    renderModel: function(model, processDefId) {
        // clear canvas
        ProcessBuilder.Designer.clear();

        // set Package ID in header
        var appId = model.packageId;
        var appName = model.packageName;
        $("#header_name").remove();
        var $headerName = $("<div id='header_name'> </div>");
        $("#header").append($headerName);

        // get process
        var processes = model.processes;
        $("#config").append($("<ol></ol>"));
        var process = (processDefId) ? processes[processDefId] : processes[Object.keys(processes)[0]];
        ProcessBuilder.Designer.currentProcessDefId = process.id;

        // display processes in header
        $("#subheader_list").remove();
        var $processHeader = $("<ul id='subheader_list'></ul>");
        for (var processId in processes) {
            var subprocess = processes[processId];
            var processName = ProcessBuilder.Util.escapeHTML(subprocess.name);
            if (processName === "") {
                processName = subprocess.id;
            }
            var $processLi = $("<li id='" + subprocess.id + "' class='header_process'>" + processName + "</li>");
            $processLi.on("click", function() {
                var selectedProcessId = $(this).attr("id");
                ProcessBuilder.Actions.execute(function() {
                    ProcessBuilder.Actions.viewProcess(selectedProcessId);
                });
            });
            $processHeader.append($processLi);
        }
        $("#header").append($processHeader);
        $("#subheader_list").find("#" + ProcessBuilder.Designer.currentProcessDefId).addClass("subheader_selected");

        // display participants
        var participants = model.participants;
        var swimlanes = process.swimlanes;
        if (swimlanes) {
            var swimlanes = swimlanes.split(";");
            for (var i = 0; i < swimlanes.length; i++) {
                var participantId = swimlanes[i];
                var participant = participants[participantId];
                if (participant) {
                    var participantName = ProcessBuilder.Util.encodeXML(participant.name);
                    var $swimlane = $("<div id='participant_" + participantId + "' title='" + participantId + "' class='participant'><div class='participant_handle'><div class='participant_label'>" + participantName + "</div></div></div>");
                    var $canvas = $("#canvas");
                    $canvas.append($swimlane);
                    // associate swimlane with participant model
                    $swimlane[0].model = participant;
                    participant.process = process;
                    $swimlane.off("click");
                    $swimlane.on("click", function() {
                        // clicking anywhere on swimlane outside node de-selects any selected node
                        $(".node_selected").removeClass("node_selected");
                    });
                }
            }
            if (ProcessBuilder.Designer.participantLabelVertical) {
                $(".participant_handle").addClass("participant_handle_vertical");
            }
        }

        // display activities
        var activities = process.activities;
        for (var activityId in activities) {
            var activity = activities[activityId];

            // add activity debug info to config panel
            var $li = $("<li>" + ProcessBuilder.Util.escapeHTML(activity.toString()) + "</li>");
            $("#config ol").append($li);

            // add activity to swimlane
            var activityLabel = ProcessBuilder.Util.encodeXML(activity.name);
            var $activityNode = $("<div class='node " + activity.type + "' id='node_" + activity.id + "' title='" + activity.id + "'><div class='node_label'>" + activityLabel + "</div></div>");
            if (activity.type === 'route') {
                var $routeNode = $("<div class='node_route'></div>");
                $activityNode.append($routeNode);
                if (activity.join === 'AND' || activity.split === 'AND') {
                    $activityNode.find(".node_label").html(activityLabel + " <div class='node_route_icon'>+</div>");
                }
            }
            if (activity.limit !== undefined && activity.limit !== null) {
                $activityNode.find(".node_label").after("<div class='node_limit'>" + activity.limit + process.durationUnit.toLowerCase() + "</div>");
            }
            $activityNode.css("position", "absolute");
            $activityNode.css("left", activity.x + "px");
            $activityNode.css("top", activity.y + "px");
            var $participant = $("#participant_" + activity.performer);
            if ($participant.length > 0) {
                $participant.append($activityNode);

                // adjust participant height
                ProcessBuilder.Designer.adjustParticipantSize($participant);
            } else {
                var $canvas = $("#canvas");
                $canvas.append($activityNode);
            }
            // associate activity element with activity model
            $activityNode[0].model = activity;
        }

        // init jsPlumb
        ProcessBuilder.Util.jsPlumb.importDefaults({
            Container: "canvas",
            Anchor: "Continuous",
            Endpoint: ["Dot", {radius: 5}],
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
            ConnectionsDetachable: ProcessBuilder.Designer.editable
        });
        if (ProcessBuilder.Util.jsPlumb.setContainer) { // for jsPlumb 1.6.2 onwards
            ProcessBuilder.Util.jsPlumb.setContainer($("#canvas"));
        }

        // display transitions
        var transitions = process.transitions;
        ProcessBuilder.Util.jsPlumb.ready(function() {
            // draw transitions
            for (var i=0; i<transitions.length; i++) {
                var transition = transitions[i];

                // add transition debug info to config panel
                var $li = $("<li>" + transition.toString() + "</li>");
                $("#config ol").append($li);

                // add transition
                var label = (transition.name) ? ProcessBuilder.Util.encodeXML(transition.name) : "";
                var color = "#999";
                if (transition.type === 'CONDITION') {
                    if (label !== "") {
                        label += "<br/>";
                    }
                    label += (transition.condition) ? transition.condition : "";
                    color = "#80A2DB";
                } else if (transition.type === 'OTHERWISE') {
                    if (label !== "") {
                        label += "<br/>";
                    }
                    label += "[otherwise]";
                    color = "#D19D00";
                } else if (transition.type === 'EXCEPTION') {
                    if (label !== "") {
                        label += "<br/>";
                    }
                    label += "[exception] " + (transition.exceptionName) ? ProcessBuilder.Util.encodeXML(transition.exceptionName) : "";
                    color = "#E37F96";
                } else if (transition.type === 'DEFAULTEXCEPTION') {
                    if (label !== "") {
                        label += "<br/>";
                    }
                    label += "[defaultexception]";
                    color = "#E37F96";
                }
                var transitionId = "transition_" + transition.id;
                label += "<div id='" + transitionId + "' class='transition_editable'><span class='transition_edit'><i class='icon-edit'></i></span><span class='transition_delete'>x</span></div>";
                var connector = (transition.style === 'orthogonal') ?
                        ["Flowchart", {cornerRadius: 5, gap: 0}] :
                        ["StateMachine", {curviness:0.1}];
                var connection = ProcessBuilder.Util.jsPlumb.connect({
                    source: "node_" + transition.from,
                    target: "node_" + transition.to,
                    connector: connector,
                    paintStyle: {strokeStyle: color, lineWidth: 1, outlineWidth: 15, outlineColor: 'transparent'},
                    endpointStyle:{ fillStyle: "#EBEBEB" },
                    overlays: [
                        ["Label", {label: label, cssClass: "transition_label"}]
                    ]
                });
                ProcessBuilder.Actions.attachTransitionEvent(transition, connection);
            }
        });

        // display start and end nodes
        var startEndNodes = process.startEndNodes;
        for (var activityId in startEndNodes) {
            var startEndNode = startEndNodes[activityId];

            // add node to swimlane
            var type = startEndNode.type;
            var activityId = startEndNode.id;
            var xOffset = startEndNode.x;
            var yOffset = startEndNode.y;
            var swimlane = startEndNode.performer;
            if (type === "end") {
                xOffset = parseInt(xOffset) + 40;
            }
            var $startEndNode = $("<div class='" + type + "' id='" + type + '_' + activityId + "'></div>");
            $startEndNode.css("position", "absolute");
            $startEndNode.css("left", xOffset + "px");
            $startEndNode.css("top", yOffset + "px");
            var $participant = $("#participant_" + swimlane);
            if ($participant.length > 0) {
                $participant.append($startEndNode);
                if ($startEndNode.height() + parseInt(yOffset) > $participant.height()) {
                    var newHeight = $startEndNode.height() + parseInt(yOffset) + 20;
                    $participant.css("height", newHeight + "px");
                }
                // adjust participant width
                if ($startEndNode.width() + parseInt(xOffset) > $participant.width()) {
                    var newWidth = $startEndNode.width() + parseInt(xOffset) + 80;
                    $participant.css("width", newWidth + "px");
                    // reset all participant widths
                    $(".participant").each(function() {
                        if (newWidth > $(this).width()) {
                            $(this).css("width", newWidth + "px");
                        }
                    });
                }
                // adjust participant handle width
                $("#canvas .participant").each(function () {
                    var handle = $(this).find(".participant_handle_vertical");
                    if (handle) {
                        var handleWidth = $(this).height() - 20;
                        handle.css("width", handleWidth + "px");
                    }
                });
            }
            // associate startEndNode element with startEndNode model
            $startEndNode[0].model = startEndNode;

            // connect node to activity
            if (activityId) {
                ProcessBuilder.Util.jsPlumb.ready(function() {
                    var connector = ["StateMachine", {curviness:0.1}];
                    var source = (type === "start") ? "start_" + activityId : "node_" + activityId;
                    var target = (type === "start") ? "node_" + activityId : "end_" + activityId;
                    try {
                        if ($("#" + source).length > 0 && $("#" + target).length > 0) {
                            var connection = ProcessBuilder.Util.jsPlumb.connect({
                                source: source,
                                target: target,
                                paintStyle: {strokeStyle: "#000", lineWidth: 1},
                                endpointStyle:{ fillStyle: "#EBEBEB" },
                                connector: connector
                            });
                            connection.bind("mouseenter", function(connection) {
                            });
                            connection.bind("mouseleave", function(connection) {
                            });
                        }
                    } catch(e) {
                    }
                });
            }

        }
        
        // refresh transitions
        ProcessBuilder.Designer.refresh();
        
        for (var participantId in participants) {
            if($("#participant_"+participantId).length > 0) {
                ProcessBuilder.Designer.adjustParticipantSize($("#participant_"+participantId));
            }
        }

        if (ProcessBuilder.Designer.editable) {
            // make stuff editable
            ProcessBuilder.Designer.initEditable();

            // init palette
            ProcessBuilder.Designer.initPalette();

        }
    },
    isModified: function() {
        var mod = ProcessBuilder.Designer.originalXpdl !== ProcessBuilder.Designer.xpdl;
        return mod;
    },
    init: function(xpdl, processDefId) {
        // reset model
        ProcessBuilder.Designer.model = new Object();

        // parse xpdl and set into model
        var model = ProcessBuilder.Designer.parseXPDL(xpdl);
        ProcessBuilder.Designer.model = model;

        // render model
        ProcessBuilder.Designer.renderModel(model, processDefId);

        // store for comparison later
        ProcessBuilder.Designer.originalXpdl = ProcessBuilder.Designer.xpdl;
        $(ProcessBuilder.Designer.source).val(xpdl);
        $(ProcessBuilder.Designer.source).format({method: 'xml'});

        // validate
        if (ProcessBuilder.Designer.autoValidate) {
            ProcessBuilder.Designer.validate();
        }

        // shortcut keys
        $(document).on("keyup", function (e) {
            if (e.which === 17) {
                ProcessBuilder.Designer.isCtrlKeyPressed = false;
            } else if(e.which === 18){
                ProcessBuilder.Designer.isAltKeyPressed = false;
            }
        }).on("keydown", function (e) {
            if (e.which === 17) {
                ProcessBuilder.Designer.isCtrlKeyPressed = true;
            } else if(e.which === 18){
                ProcessBuilder.Designer.isAltKeyPressed = true;
            }
            if ($(".property-editor-container:visible").length === 0) {
                if (e.which === 90 && ProcessBuilder.Designer.isCtrlKeyPressed && !ProcessBuilder.Designer.isAltKeyPressed) { // CTRL+Z - undo
                    ProcessBuilder.Actions.undo();
                    return false;
                }
                if (e.which === 89 && ProcessBuilder.Designer.isCtrlKeyPressed && !ProcessBuilder.Designer.isAltKeyPressed) { // CTRL+Z - redo
                    ProcessBuilder.Actions.redo();
                    return false;
                }
            }
        });
    },
    initNodes: function(nodes, revert) {
        // append connector endpoints
        var $endpoints = $("<div class='endleft endpoint'></div><div class='endtop endpoint'></div><div class='endright endpoint'></div><div class='endbottom endpoint'></div>");
        $(nodes).find(".endpoint").remove();
        $(nodes).append($endpoints);

        // make nodes draggable
        $(nodes).removeClass("ui-draggable");
        var selectedObjs;
        function moveSelected(ol, ot){
            var zoom = ProcessBuilder.Designer.zoom;
            selectedObjs.each(function() {
                var $this = $(this);
                var p = $this.position();
                var l = p.left;
                var t = p.top;
                var newLeft = Math.round(ol + l / zoom);
                var newTop = Math.round(ot + t / zoom);
                $this.css('left', newLeft);
                $this.css('top', newTop);
                ProcessBuilder.Util.jsPlumb.setSuspendDrawing(false, true);
                ProcessBuilder.Util.jsPlumb.repaint($this);
            });
        }
        var revertFunction = function(valid) {
            if (!valid) {
                ProcessBuilder.Designer.refresh(200);
                return true;
            } else {
                return false;
            }
        };
        var dragging = false;
        ProcessBuilder.Util.jsPlumb.draggable($(nodes), {
            connectToSortable: ".participant",
            zIndex: 200,
            opacity: 0.7,
            revert: revert ? revertFunction : null,
            revertDuration: 100,
            cursor: "move",
            snap: false,
            start: function(event, ui) {
                jsPlumb.setSuspendDrawing(true); // needed for jsPlumb-1.6.2 onwards
                if (ui.helper.hasClass('node_selected'))
                    selectedObjs = $('.node_selected');
                else {
                    selectedObjs = $(ui.helper);
                    $('.node_selected').removeClass('node_selected');
                }
                dragging = true;
            },
            stop: function(event, ui) {
                setTimeout(function() {
                    dragging = false;
                }, 300);
                jsPlumb.setSuspendDrawing(false, true); // needed for jsPlumb-1.6.2 onwards
            },
            drag: function(event, ui) {
                var currentLoc = $(this).position();
                var prevLoc = $(this).data('prevLoc');
                if (!prevLoc) {
                    prevLoc = ui.originalPosition;
                }

                var offsetLeft = currentLoc.left - prevLoc.left;
                var offsetTop = currentLoc.top - prevLoc.top;

                moveSelected(offsetLeft, offsetTop);
                $(this).data('prevLoc', currentLoc);
            }
        });
        // Disable click to edit node for now, as dragging will trigger it
//        $(nodes).click(function(e) {
//            // open edit property dialog instead of selection in previous code // $(this).toggleClass("node_selected");
//            var $node = $(this).closest(".node");
//            var node = $node[0].model;
//            ProcessBuilder.Actions.editProperties(node);
//            e.stopPropagation();
//        });

        // append delete button
        var $deleteButton = $("<div class='node_delete'>x</div>");
        $(nodes).find(".node_delete").remove();
        $(nodes).append($deleteButton);
        $(nodes).find(".node_delete").off("click");
        $(nodes).find(".node_delete").on("click", function() {
            if (dragging) {
                return false;
            }
            var $node = $(this).parent();
            ProcessBuilder.Actions.execute(function() {
                ProcessBuilder.Actions.deleteNode($node);
            });
        });

        // append edit button
        if (!$(nodes).hasClass("end")) {
            var $editButton = $("<div class='node_edit'><i class='icon-edit'></i></div>");
            var $nodes = $(nodes);
            $nodes.find(".node_edit").remove();
            $nodes.prepend($editButton);
            $nodes.find(".node_edit").on("click", function(e) {
                if (dragging) {
                    return false;
                }
                var $node = $(this).closest(".node");
                var node = $node[0].model;
                ProcessBuilder.Actions.editProperties(node);
                e.stopPropagation();
            });
        } else {
            $(nodes).removeClass("node");
        }

        // make nodes connectable with transitions
        $(".node, .start").each(function(i, e) {
            var label = "<div class='transition_editable'><span class='transition_edit'><i class='icon-edit'></i></span><span class='transition_delete'>x</span></div>";
            ProcessBuilder.Util.jsPlumb.makeSource($(e), {
                filter: ".endpoint",
                anchor: "Continuous",
                connectorStyle: {strokeStyle: "#ccc", lineWidth: 2, outlineWidth: 15, outlineColor: 'transparent'},
                connectorOverlays: [
                    ["Label", {
                            label: label,
                            cssClass: "transition_label"
                        }]
                ],
                endpoint: ["Dot", {radius: 6, hoverClass: 'endpoint_hover'}],
                paintStyle: {fillStyle: "#EBEBEB"},
                isSource: true,
                isTarget: true,
                maxConnections: 20,
                onMaxConnections: function(info, e) {
                    alert(get_pbuilder_msg("pbuilder.label.maximumConnectionsReached") + ": " + info.maxConnections);
                },
                dragOptions: {
                    start: function() {
                    }
                }
            });
        });
        ProcessBuilder.Util.jsPlumb.makeTarget($(".node"), {
            dropOptions: {
                hoverClass: "activity_hover",
                drop: function(e, ui) {
                    ProcessBuilder.Designer.refresh();
                }
            },
            anchor: "Continuous",
            endpoint: ["Dot", {radius: 6, hoverClass: 'endpoint_hover'}],
            isSource: true,
            isTarget: true,
            paintStyle: {fillStyle: "#EBEBEB"}
        });
        ProcessBuilder.Util.jsPlumb.makeTarget($(".end"), {
            dropOptions: {
                hoverClass: "activity_hover",
                drop: function(e, ui) {
                    ProcessBuilder.Designer.refresh();
                }
            },
            anchor: "AutoDefault",
            endpoint: ["Dot", {radius: 6, hoverClass: 'endpoint_hover'}],
            isSource: true,
            isTarget: true,
            paintStyle: {fillStyle: "#EBEBEB"}
        });

//        // not required now, using property editor instead
//        if (ProcessBuilder.Designer.editable) {
//            // make node label editable
//            $(nodes).find(".node_label").editable(function(value, settings) {
//                return value;
//            }, {
//                submit: "OK",
//                onblur: "submit"
//            });
//        }
    },
    initParticipants: function(participants) {
        if (!participants) {
            participants = $(".participant:not(.palette_participant)");
        }
        // make participants droppable to move existing nodes
        $(participants).droppable({
            drop: function(event, ui) {
                var node = $(ui.draggable);
                if ($(node).hasClass("palette_node")) {
                    // handle new node from palette
                    var $newNode = $(node).clone();
                    var $participant = $(this);
                    var top = event.pageY;
                    var left = event.pageX;
                    ProcessBuilder.Actions.execute(function() {
                        ProcessBuilder.Actions.addNode($newNode, $participant, top, left);
                    });
                } else {
                    var $participant = $(this);
                    var top = ui.helper.offset().top;
                    var left = ui.helper.offset().left;
                    ProcessBuilder.Actions.execute(function() {
                        ProcessBuilder.Actions.moveNode(node, $participant, top, left);
                    });
                }

                ProcessBuilder.Designer.adjustParticipantSize($participant);

                // refresh transitions
                ProcessBuilder.Designer.refresh();
            },
            hoverClass: "participant_highlight",
            accept: ".node, .start, .end, .palette_node, .palette_start, .palette_end",
            greedy: true,
            tolerance: "fit"
        });

        // append delete button
        var $deleteButton = $("<div class='node_delete'>x</div>");
        var $participants = $(participants).find(".participant_handle");
        $participants.find(".node_delete").remove();
        $participants.prepend($deleteButton);
        $participants.find(".node_delete").on("click", function() {
            var $participant = $(this).closest(".participant");
            ProcessBuilder.Actions.execute(function() {
                ProcessBuilder.Actions.deleteParticipant($participant);
            });
        });

        // append add button
        var $addButton = $("<div class='participant_add'>+</div>");
        var $participants = $(participants).find(".participant_handle");
        $participants.find(".participant_add").remove();
        $participants.prepend($addButton);
        $participants.find(".participant_add").on("click", function() {
            var $participant = $(this).closest(".participant");
            var $newParticipant = $("<div class='participant'><div class='participant_handle'><div class='participant_label'>" + get_pbuilder_msg("pbuilder.label.participant") + "</div></div></div>");
            if (ProcessBuilder.Designer.participantLabelVertical) {
                $newParticipant.find(".participant_handle").addClass("participant_handle_vertical");
            }
            $participant.after($newParticipant);
            ProcessBuilder.Actions.execute(function() {
                ProcessBuilder.Actions.addParticipant($newParticipant);
            });
        });

        // append edit button
        var $editButton = $("<div class='node_edit'><i class='icon-edit'></i></div>");
        var $participants = $(participants).find(".participant_handle");
        $participants.find(".node_edit").remove();
        $participants.prepend($editButton);
        $participants.find(".node_edit").on("click", function(e) {
            var $participant = $(this).closest(".participant");
            var participant = $participant[0].model;
            ProcessBuilder.Actions.editProperties(participant);
            e.stopPropagation();
        });

    },
    adjustParticipantSize: function(participant) {
        var $participant = $(participant);
        // recalculate participant height
        var zoom = ProcessBuilder.Designer.zoom;
        var topOffset = 0, bottomOffset = 0, outer = true;
        $participant.children().each(function (i, e) {
            var $e = $(e),
                    eTopOffset = $e.offset().top,
                    eBottomOffset = eTopOffset + (outer ? $e.outerHeight() : $e.height());

            if (eTopOffset < topOffset) {
                topOffset = eTopOffset;
            }
            if (eBottomOffset > bottomOffset) {
                bottomOffset = eBottomOffset;
            }
        });
        var childrenHeight = (bottomOffset - topOffset - $participant.offset().top) / zoom + 50;
        $participant.css("height", childrenHeight + "px");
        var participantHeight = $participant.height();
        var participantHeightNoPadding = participantHeight - 20;
        // set partipant handle dimensions
        var handle = $participant.find(".participant_handle_vertical");
        handle.css("width", "" + participantHeightNoPadding + "px");
        handle.css("bottom", "-" + participantHeight + "px");

        // recalculate participant width
        var leftOffset = 0, rightOffset = 0, outer = true;
        $participant.children().each(function (i, e) {
            var $e = $(e),
                    eLeftOffset = $e.offset().left,
                    eRightOffset = eLeftOffset + (outer ? $e.outerWidth() : $e.width());

            if (eLeftOffset < leftOffset && eLeftOffset > 0) {
                leftOffset = eLeftOffset;
            }
            if (eRightOffset > rightOffset) {
                rightOffset = eRightOffset;
            }
        });
        var childrenWidth = (rightOffset - leftOffset - $participant.offset().left) / zoom + 80;
        if (childrenWidth > $participant.width()) {
            $(".participant").css("width", childrenWidth + "px");
        }
    },
    initEditable: function() {
        // add class to canvas
        $("#canvas").addClass("editable");

        // make participants draggable
        $("#canvas").sortable({
            handle: ".participant_handle",
            connectWith: "#canvas",
            items: ".participant",
            sort: function() {
                ProcessBuilder.Designer.refresh();
            },
            stop: function(event, ui) {
                var $participant = $(ui.item);
                if ($participant.hasClass("palette_participant")) {
                    // new participant
                    $participant.removeClass("palette_participant");
                    ProcessBuilder.Actions.execute(function() {
                        ProcessBuilder.Actions.addParticipant($participant);
                    });
                } else {
                    // existing participant
                    ProcessBuilder.Actions.execute(function() {
                        ProcessBuilder.Actions.moveParticipant();
                    });
                }
            },
            tolerance: "pointer",
            revertDuration: 100,
            revert: "invalid"
        }).disableSelection();

        // make participants droppable to move existing nodes
        ProcessBuilder.Designer.initParticipants();

        // make nodes draggable
        ProcessBuilder.Designer.initNodes($(".node"), true);
        ProcessBuilder.Designer.initNodes($(".start, .end"));

        // append add process button to header
        $("#process_add").remove();
        var $addButton = $("<li><span id='process_add'>+</span></li>");
        $("#header #subheader_list").append($addButton);
        $("#header").find("#process_add").off("click");
        $("#header").find("#process_add").on("click", function() {
            ProcessBuilder.Actions.execute(function() {
                ProcessBuilder.Actions.addProcess();
            });
        });

        // append delete button to processes
        var $deleteButton = $("<div class='node_delete'>x</div>");
        var $processes = $(".header_process");
        $processes.find(".node_delete").remove();
        $processes.prepend($deleteButton);
        $processes.find(".node_delete").on("click", function(e) {
            var $process = $(this).closest(".header_process");
            var processId = $process.attr("id");
            ProcessBuilder.Actions.execute(function() {
                ProcessBuilder.Actions.deleteProcess(processId);
            });
            e.stopPropagation();
        });

        var $buttons = $("<div class='node_buttons'></div>");
        var $processes = $(".header_process");
        $processes.find(".node_buttons").remove();
        $processes.prepend($buttons);
        $buttons = $processes.find(".node_buttons");

        // append duplicate button to processes
        var $copyButton = $("<div class='node_copy'><i class='icon-copy'></i></div>");
        $buttons.prepend($copyButton);
        $processes.find(".node_copy").on("click", function(e) {
            var $process = $(this).closest(".header_process");
            var processId = $process.attr("id");
            ProcessBuilder.Actions.duplicateProcess(processId);
            e.stopPropagation();
        });

        // append edit button to processes
        var $editButton = $("<div class='node_edit'><i class='icon-edit'></i></div>");
        $buttons.prepend($editButton);
        $processes.find(".node_edit").on("click", function(e) {
            var $process = $(this).closest(".header_process");
            var processId = $process.attr("id");
            var process = ProcessBuilder.Designer.model.processes[processId];
            if (ProcessBuilder.Designer.currentProcessDefId !== processId) {
                ProcessBuilder.Actions.viewProcess(processId);
            }
            ProcessBuilder.Actions.editProperties(process);
            e.stopPropagation();
        });

        // single click on any endpoint
        ProcessBuilder.Util.jsPlumb.unbind("endpointClick");
        ProcessBuilder.Util.jsPlumb.bind("endpointClick", function(endpoint, originalEvent) {
        });
        // check for invalid connections
        ProcessBuilder.Util.jsPlumb.unbind("beforeDrop");
        ProcessBuilder.Util.jsPlumb.bind("beforeDrop", function(info) {
            var connection = info.connection;
            var process = connection.source.model.process;
            var sourceId = info.sourceId;
            if (sourceId.indexOf("start_") === 0) {
                sourceId = sourceId.substring("start_".length);
            } else {
                sourceId = sourceId.substring("node_".length);
            }
            var targetId = info.targetId;
            if (targetId.indexOf("end_") === 0) {
                targetId = targetId.substring("end_".length);
            } else {
                targetId = targetId.substring("node_".length);
            }
            // disallow duplicate transitions from start node, or directly from start to end
            if (info.sourceId.indexOf("start_") === 0) {
                if (process.startEndNodes["start_" + targetId] || info.targetId.indexOf("end_") === 0) {
                    return false;
                }
            }
            // disallow duplicate transitions to end node
            if (info.targetId.indexOf("end_") === 0) {
                if (process.startEndNodes["end_" + sourceId]) {
                    return false;
                }
            }
            return true;
        });
        // bind event handling to new or moved connections
        ProcessBuilder.Util.jsPlumb.unbind("connection");
        ProcessBuilder.Util.jsPlumb.bind("connection", function(info) {
            var connection = info.connection;
            var source = info.source;
            var target = info.target;
            ProcessBuilder.Actions.execute(function() {
                ProcessBuilder.Actions.addTransition(source, target, connection);
            });
            // register edit event
            setTimeout(function() {
                var transition = connection.model;
                if (transition) {
                    var transitionId = "transition_" + transition.id;
                    var $label = $(connection.canvas).next(".transition_label").find(".transition_editable");
                    $label.attr("id", transitionId);
                    if ($label.length === 0) {
                        var label = "<span class='transition_edit'><i class='icon-edit'></i></span><span class='transition_delete'>x</span>";
                        var overlay = connection.getOverlay();
                        if (overlay) {
                            overlay.setLabel(label);
                        }
                    }
                    ProcessBuilder.Actions.attachTransitionEvent(transition, connection);
                }
            }, 100);
            // remove unused endpoints
            var endpoints = ProcessBuilder.Util.jsPlumb.getEndpoints($(source));
            if (endpoints.length > 0) {
                for (var i=0; i<endpoints.length; i++) {
                    if (endpoints[i].connections.length === 0) {
                        ProcessBuilder.Util.jsPlumb.deleteEndpoint(endpoints[i]);
                    }
                }
            }
        });
        // bind event handling to detached connections
        ProcessBuilder.Util.jsPlumb.unbind("connectionDetached");
        ProcessBuilder.Util.jsPlumb.bind("connectionDetached", function(info) {
            var connection = info.connection;
            var source = info.source;
            var target = info.target;
            if ($(target).attr("id").indexOf("jsPlumb") >= 0) {
                $("#node_dialog").remove();
                var offsetLeft = (target.offsetLeft);
                var offsetTop = $(target).offset().top;
                var swimlane;
                // determine swimlane
                $(".participant").each(function(index, participant) {
                    var participantTop = $(participant).offset().top; // * ProcessBuilder.Designer.zoom;
                    var participantHeight = $(participant).height() * ProcessBuilder.Designer.zoom;
                    if (offsetTop >= participantTop && offsetTop <= (participantTop + participantHeight)) {
                        target = participant;
                        swimlane = target;
                        return false;
                    }
                });
                if (!swimlane) {
                    return false;
                }
                // remove unused endpoints
                var endpoints = ProcessBuilder.Util.jsPlumb.getEndpoints($(source));
                if (endpoints.length > 0) {
                    for (var i=0; i<endpoints.length; i++) {
                        if (endpoints[i].connections.length === 0) {
                            ProcessBuilder.Util.jsPlumb.deleteEndpoint(endpoints[i]);
                        }
                    }
                }
                // display dialog to choose node type
                var nodeTop = connection.endpoints[1].endpoint.y + 100;
                var $nodeDialog = $('<div id="node_dialog"><ul><li type="activity">' + get_pbuilder_msg("pbuilder.label.activity") + '</li><li type="tool">' + get_pbuilder_msg("pbuilder.label.tool") + '</li><li type="route">' + get_pbuilder_msg("pbuilder.label.route") + '</li><li type="subflow">' + get_pbuilder_msg("pbuilder.label.subflow") + '</li><li type="end">' + get_pbuilder_msg("pbuilder.label.end") + '</li><ul></div>');
                $nodeDialog.dialog({
                    autoOpen: true,
                    modal: true,
                    width: 100,
                    open: function(event, ui) {
                        var dialogTop = (nodeTop - 50) * ProcessBuilder.Designer.zoom;
                        var dialogLeft = (offsetLeft + 50) * ProcessBuilder.Designer.zoom;
                        $nodeDialog.dialog("option", { position: [dialogLeft, dialogTop] });
                    }
                });
                // remove irrelevant node types
                var endNodeId = "end_" + source.model.id;
                var currentProcess = ProcessBuilder.Designer.model.processes[ProcessBuilder.Designer.currentProcessDefId];
                if (currentProcess.startEndNodes[endNodeId]) {
                    $nodeDialog.find("[type=end]").remove();
                }
                // handle node type selection
                $("#node_dialog li").on("click", function() {
                    $nodeDialog.dialog("close");
                    var nodeType = $(this).attr("type");
                    var nodeLabel =  $(this).text();
                    // create new node
                    var $participant = $(swimlane);
                    var zoom = ProcessBuilder.Designer.zoom;
                    ProcessBuilder.Designer.setZoom(1);
                    var $newNode = $('<div id="newNode" class="node ' + nodeType + '"><div class="node_label">' + nodeLabel + '</div></div>');
                    $newNode.offset({left: offsetLeft});
                    $newNode.offset({top: nodeTop});
                    ProcessBuilder.Designer.setZoom(zoom);
                    ProcessBuilder.Actions.execute(function() {
                        ProcessBuilder.Actions.addNode($newNode, $participant, nodeTop);
                        // connect nodes
                        var connector = ["StateMachine", {curviness:0.1}];
                        var label = "<div class='transition_editable'><span class='transition_edit'><i class='icon-edit'></i></span><span class='transition_delete'>x</span></div>";
                        ProcessBuilder.Util.jsPlumb.setSuspendDrawing(true);
                        var newConnection = ProcessBuilder.Util.jsPlumb.connect({
                            source: $(source).attr("id"),
                            target: $newNode.attr("id"),
                            container: "canvas",
                            connector: connector,
                            endpointStyle:{ fillStyle: "#EBEBEB" },
                            overlays: [
                                ["Label", {label: label, cssClass: "transition_label"}]
                            ]
                        });
                        ProcessBuilder.Designer.adjustParticipantSize($participant);
                        ProcessBuilder.Util.jsPlumb.setSuspendDrawing(false);
                    });
                });
                return false;
            } else {
                // delete existing transition
                ProcessBuilder.Actions.execute(function() {
                    ProcessBuilder.Actions.deleteTransition(source, target, connection);
                });
            }
        });
    },
    initPalette: function() {
        var click = {
            x: 0,
            y: 0
        };
        // make palette participant draggable
        $(".palette_participant").draggable({
            connectToSortable: "#canvas",
            appendTo: "#canvas",
            helper: "clone",
            zIndex: 200,
            opacity: 0.7,
            revert: "invalid",
            cursor: "move",
            start: function(event) {
                click.x = event.clientX;
                click.y = event.clientY;
            },
            drag: function(event, ui) {
                // This is the parameter for scale()
                var zoom = ProcessBuilder.Designer.zoom;
                var original = ui.originalPosition;
                // jQuery will simply use the same object we alter here
                ui.position = {
                    left: (event.clientX - click.x + original.left) / zoom,
                    top: (event.clientY - click.y + original.top) / zoom
                };
            }
        });

        // make palette nodes draggable
        $(".palette_node, .palette_start, .palette_end").draggable({
            connectToSortable: ".participant",
            appendTo: "#canvas",
            helper: "clone",
            zIndex: 200,
            opacity: 0.7,
            revert: "invalid",
            cursor: "move",
            snap: false,
            start: function(event) {
                click.x = event.clientX;
                click.y = event.clientY;
            },
            drag: function(event, ui) {
                // This is the parameter for scale()
                var zoom = ProcessBuilder.Designer.zoom;
                var original = ui.originalPosition;
                // jQuery will simply use the same object we alter here
                ui.position = {
                    left: (event.clientX - click.x + original.left) / zoom,
                    top: (event.clientY - click.y + original.top) / zoom
                };
            }
        });

        // make palette as dialog
        $(function() {
            $("#palette").show();
            $("#palette").dialog({
                title: '',
                width: "96px",
                position: ['left', 39],
                closeOnEscape: false,
                open: function(event, ui) {
                    $(".ui-dialog-titlebar-close", this.parentNode).hide();
                },
                appendTo: document.body,
                dialogClass: "palette",
                resizable: false,
                modal: false
            });
            $('.palette.ui-dialog').css({position: "fixed"});
//            var container = $("#viewport");
//            var dialog = $('.ui-dialog');
//            dialog.draggable("option", "containment", container);
        });
    },
    screenshot: function(callback, show) {
       (function() {
            // set zoom
            var zoom = ProcessBuilder.Designer.zoom;
            ProcessBuilder.Designer.setZoom(1);

            // replace connectors from svg to canvas (to support html2canvas)
            var $clonedBody = $(document.body).clone();
            var $canvas = $clonedBody.find("#canvas");
            $canvas.detach();

            $clonedBody.append($canvas);
            $clonedBody.find("#viewport").remove();
            $clonedBody.find("#header, #footer, #panel, #builder-header, #builder-footer, #palette, #loading, #adminBar, .ui-dialog, iframe").remove();
            //$clonedBody.find("#canvas, .participant").css("background", "white");
            $clonedBody.find("#canvas").css({
                "top":"0px"
            });
            $clonedBody.find("#canvas .participant").css({
                "border":"solid 1px #999"
            });
            $clonedBody.find("svg._jsPlumb_connector").each(function() {
                var $svg = $(this);
                var svg = $(this).clone().wrap('<p>').parent().html();
                var $tempCanvas = $('<canvas></canvas>');
                $tempCanvas.attr("style", $svg.attr("style"));
                $clonedBody.find("#canvas").append($tempCanvas);
                // fix duplicate xmlns
                svg = svg.replace('xmlns="http://www.w3.org/1999/xhtml"', '');
                // render
                canvg($tempCanvas[0], svg);
                $svg.remove();
            });
            $clonedBody.find(".participant_handle_vertical").each(function() {
                var label = $(this).find(".participant_label").text();
                var $cvs = $('<canvas></canvas>');
                $cvs.height(110);
                $(this).parent().append($cvs);
                var context = $cvs[0].getContext('2d');

                // rotate 90 degrees counter clockwise
                context.rotate(-90 * Math.PI/180);

                // draw new label
                context.font="14px Arial";
                context.fillText(label, -100, 20);

                // remove previous label
                $(this).find(".participant_label").remove();
                $(this).css("zIndex", "-1");

            });

            // create invisible iframe for canvas
            var iframe = document.createElement('iframe');
            var iwidth = $("#canvas").width();
            var iheight = 0; // $("#canvas").height();
            $(iframe).css({
                'visibility':'hidden'
            }).width(iwidth).height(iheight);
            $(document.body).append(iframe);
            var d = iframe.contentWindow.document;
            d.open();
            $(iframe.contentWindow).load(function() {
                var ibody = $(iframe).contents().find('body');

                // workaround: remove participant transform rotate for now, not supported yet
        //        $clonedBody.find(".participant_handle_vertical").removeClass("participant_handle_vertical");
                // append to body
                $($clonedBody).prepend($('<link href="' + ProcessBuilder.Designer.contextPath + '/pbuilder/css/pbuilder.css" rel="stylesheet" />'));
                $(ibody).append($clonedBody);

//                var $loading = $("<span>generating image... &nbsp;</span> ");
//                $("#controls").prepend($loading);
                var $loading = $('<div id="loading"><i class="icon-spinner icon-spin icon-2x"></i> ' + get_pbuilder_msg("pbuilder.label.generating") + '</div>');
                $("body").append($loading);

                // restore zoom
                ProcessBuilder.Designer.setZoom(zoom);

                setTimeout(function() {
                    // generate image
                    html2canvas($clonedBody, {
                        onrendered: function(canvas) {
                            // capture image
                            var imgData = canvas.toDataURL();
                            if (callback) {
                                callback.call(this, imgData);
                            }
                            if (show || !callback) {
                                // create screenshot div
                                var $screenshot = $("<div class='screenshot'></div>");
                                $(document.body).append($screenshot);
                                $screenshot.html("generating screenshot...");

                                var $img = $("<img src='" + imgData + "' width='640'>");
                                $screenshot.empty();
                                $screenshot.append($img);

                                // show dialog
                                $screenshot.dialog({title: 'Screenshot', width: 680, modal: true});
                            }

                            // cleanup
                            $clonedBody.remove();
                            $loading.remove();
                            $(iframe).remove();
                        }
                    });
                }, 300);
            });
            d.close();
        })();
    },
    generateXPDL: function(package) {
        var model = (!package) ? ProcessBuilder.Designer.model : package;

        // add package
        var xml = '<xpdl:Package xmlns:xpdl="http://www.wfmc.org/2002/XPDL1.0" xmlns="http://www.wfmc.org/2002/XPDL1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" Id="' + model.packageId + '" Name="' + model.packageName + '" xsi:schemaLocation="http://www.wfmc.org/2002/XPDL1.0 http://wfmc.org/standards/docs/TC-1025_schema_10_xpdl.xsd">';

        // add header
        xml += '<xpdl:PackageHeader>\
                <xpdl:XPDLVersion>1.0</xpdl:XPDLVersion>\
                <xpdl:Vendor/>\
                <xpdl:Created/>\
                </xpdl:PackageHeader>';

        // add script
        xml += '<xpdl:Script Type="text/javascript"/>';

        // add participants
        xml += '<xpdl:Participants>';
        var participants = model.participants;
        for (var id in participants) {
            var participant = participants[id];
            xml += '<xpdl:Participant Id="' + participant.id + '" Name="' + ProcessBuilder.Util.encodeXML(participant.name) + '">\
                    <xpdl:ParticipantType Type="' + ProcessBuilder.Util.preventUndefined(participant.type) + '"/>\
                    </xpdl:Participant>';
        }
        xml += '</xpdl:Participants>';

        // add applications
        xml += '<xpdl:Applications>\
                <xpdl:Application Id="default_application"/>\
                </xpdl:Applications>';

        // add processes
        xml += '<xpdl:WorkflowProcesses>';
        var processes = model.processes;
        for (var id in processes) {
            var process = processes[id];
            // add process header
            xml += '<xpdl:WorkflowProcess Id="' + process.id + '" Name="' + ProcessBuilder.Util.encodeXML(process.name) + '">\
                    <xpdl:ProcessHeader DurationUnit="' + ProcessBuilder.Util.preventUndefined(process.durationUnit) + '">';
            if (process.limit) {
                xml += '<xpdl:Limit>' + process.limit + '</xpdl:Limit>';
            }
            xml += "</xpdl:ProcessHeader>";

            // add formal parameters
            xml += '<xpdl:FormalParameters>';
            for (var df=0; df<process.formalParameters.length; df++) {
                var formalParameter = process.formalParameters[df];
                xml += '<xpdl:FormalParameter Id="' + formalParameter.parameterId + '" Mode="' + ProcessBuilder.Util.preventUndefined(formalParameter.mode) + '">\
                            <xpdl:DataType>\
                                <xpdl:BasicType Type="STRING"/>\
                            </xpdl:DataType>\
                        </xpdl:FormalParameter>';
            }
            xml += '</xpdl:FormalParameters>';

            // add workflow variables
            xml += '<xpdl:DataFields>';
            for (var df=0; df<process.dataFields.length; df++) {
                var dataField = process.dataFields[df];
                xml += '<xpdl:DataField Id="' + dataField.variableId + '" IsArray="FALSE">\
                            <xpdl:DataType>\
                                <xpdl:BasicType Type="STRING"/>\
                            </xpdl:DataType>\
                        </xpdl:DataField>';
            }
            xml += '</xpdl:DataFields>';

            // add activities
            xml += '<xpdl:Activities>';
            var activities = process.activities;
            for (var id in activities) {
                var activity = activities[id];
                xml += '<xpdl:Activity Id="' + activity.id + '" Name="' + ProcessBuilder.Util.encodeXML(activity.name) + '">';
                if (activity.limit) {
                    xml += '<xpdl:Limit>' + activity.limit + '</xpdl:Limit>';
                }
                if (activity.deadlines) {
                    for (var d=0; d<activity.deadlines.length; d++) {
                        var deadline = activity.deadlines[d];
                        xml += '<xpdl:Deadline Execution="' + deadline.execution + '">';
                        // determine condition
                        var deadlineCondition;
                        if (deadline.durationUnit === 'd') {
                            deadlineCondition = "var d = new java.text.SimpleDateFormat('dd/MM/yyyy'); d.parse(" + deadline.deadlineLimit + ");";
                        } else if (deadline.durationUnit ==='t') {
                            deadlineCondition = "var d = new java.text.SimpleDateFormat('dd/MM/yyyy HH:mm'); d.parse(" + deadline.deadlineLimit + ");";
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
                        xml += '    <xpdl:DeadlineCondition>' + deadlineCondition + '</xpdl:DeadlineCondition>';
                        xml += '    <xpdl:ExceptionName>' + deadline.exceptionName + '</xpdl:ExceptionName>';
                        xml += '</xpdl:Deadline>';
                    }
                }
                if (activity.type === 'tool') {
                    xml += '<xpdl:Implementation><xpdl:Tool Id="default_application"/></xpdl:Implementation>';
                } else if (activity.type === 'route') {
                    xml += '<xpdl:Route/>';
                } else if (activity.type === 'subflow') {
                    var execution = (activity.execution) ? activity.execution : "SYNCHR";
                    xml += '<xpdl:Implementation><xpdl:SubFlow Execution="' + ProcessBuilder.Util.preventUndefined(execution) + '" Id="' + activity.subflowId + '">';
                    xml += '<xpdl:ActualParameters>';
                    for (var p=0; p<activity.actualParameters.length; p++) {
                        var actualParameter = activity.actualParameters[p];
                        xml += '<xpdl:ActualParameter>' + actualParameter.actualParameter + '</xpdl:ActualParameter>';
                    }
                    xml += '</xpdl:ActualParameters>';
                    xml += '</xpdl:SubFlow></xpdl:Implementation>';
                } else {
                    xml += '<xpdl:Implementation><xpdl:No/></xpdl:Implementation>';
                }
                if (activity.performer) {
                    xml += '<xpdl:Performer>' + activity.performer + '</xpdl:Performer>';
                }
                if (activity.join || activity.split) {
                    xml += '<xpdl:TransitionRestrictions><xpdl:TransitionRestriction>';
                    if (activity.join) {
                        xml += '<xpdl:Join Type="' + ProcessBuilder.Util.preventUndefined(activity.join) + '">';
                        if (activity.joinTransitions.length > 0) {
                            xml += '<xpdl:TransitionRefs>';
                            for (var j=0; j<activity.joinTransitions.length; j++) {
                                var transition = activity.joinTransitions[j];
                                xml += '<xpdl:TransitionRef Id="' + transition + '"/>';
                            }
                            xml += '</xpdl:TransitionRefs>';
                        }
                        xml += '</xpdl:Join>';
                    }
                    if (activity.split) {
                        xml += '<xpdl:Split Type="' + ProcessBuilder.Util.preventUndefined(activity.split) + '">';
                        if (activity.splitTransitions.length > 0) {
                            xml += '<xpdl:TransitionRefs>';
                            for (var j=0; j<activity.splitTransitions.length; j++) {
                                var transition = activity.splitTransitions[j];
                                xml += '<xpdl:TransitionRef Id="' + transition + '"/>';
                            }
                            xml += '</xpdl:TransitionRefs>';
                        }
                        xml += '</xpdl:Split>';
                    }
                    xml += '</xpdl:TransitionRestriction></xpdl:TransitionRestrictions>';
                }
                xml += '<xpdl:ExtendedAttributes>\
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_PARTICIPANT_ID" Value="' + activity.performer + '"/>\
                        <xpdl:ExtendedAttribute Name="JaWE_GRAPH_OFFSET" Value="' + activity.x + ',' + activity.y + '"/>\
                    </xpdl:ExtendedAttributes>';
                xml += '</xpdl:Activity>';
            }

            // close activities
            xml += '</xpdl:Activities>';

            // add transitions
            xml += '<xpdl:Transitions>';
            var transitions = process.transitions;
            if (transitions) {
                for (var t=0; t<transitions.length; t++) {
                    var transition = transitions[t];
                    xml += '<xpdl:Transition From="' + transition.from + '" Id="' + transition.id + '" To="' + transition.to + '" Name="' + ProcessBuilder.Util.encodeXML(transition.name) + '">';
                    var condition = transition.condition;//ProcessBuilder.Util.encodeXML(transition.condition);
                    if (transition.type === 'CONDITION') {
                        xml += '<xpdl:Condition Type="CONDITION">' + ProcessBuilder.Util.escapeXMLText(condition) + '</xpdl:Condition>';
                    } else if (transition.type === 'OTHERWISE') {
                        xml += '<xpdl:Condition Type="OTHERWISE"/>';
                    } else if (transition.type === 'EXCEPTION') {
                        xml += '<xpdl:Condition Type="EXCEPTION">' + ProcessBuilder.Util.escapeXMLText(transition.exceptionName) + '</xpdl:Condition>';
                    } else if (transition.type === 'DEFAULTEXCEPTION') {
                        xml += '<xpdl:Condition Type="DEFAULTEXCEPTION">' + ProcessBuilder.Util.escapeXMLText(condition) + '</xpdl:Condition>';
                    }
                    xml += '<xpdl:ExtendedAttributes>\
                                <xpdl:ExtendedAttribute Name="JaWE_GRAPH_TRANSITION_STYLE" Value="NO_ROUTING_ORTHOGONAL"/>';
                    if (transition.style === 'orthogonal') {
                        xml += '<xpdl:ExtendedAttribute Name="JaWE_GRAPH_BREAK_POINTS" Value="orthogonal"/>';
                    }
                    if (transition.conditions !== undefined) {
                        var conditionsJson = JSON.encode(transition.conditions);
                        xml += '<xpdl:ExtendedAttribute Name="PBUILDER_TRANSITION_CONDITIONS" Value="'+ProcessBuilder.Util.encodeXML(conditionsJson)+'"/>';
                    }
                    xml += '</xpdl:ExtendedAttributes>';
                    xml += '</xpdl:Transition>';
                }
            }
            // close transitions
            xml += '</xpdl:Transitions>';

            // add extended attributes
            xml += '<xpdl:ExtendedAttributes>';

            // add swimlanes
            xml += '<xpdl:ExtendedAttribute Name="JaWE_GRAPH_WORKFLOW_PARTICIPANT_ORDER" Value="' + process.swimlanes + '"/>';

            // add start end nodes
            var startEndNodes = process.startEndNodes;
            for (var activityId in startEndNodes) {
                var startEndNode = startEndNodes[activityId];
                var name = (startEndNode.type === 'start') ? "JaWE_GRAPH_START_OF_WORKFLOW" : "JaWE_GRAPH_END_OF_WORKFLOW";
                var type = (startEndNode.type === 'start') ? 'START_DEFAULT' : 'END_DEFAULT';
                xml += '<xpdl:ExtendedAttribute Name="' + ProcessBuilder.Util.encodeXML(name) + '" Value="JaWE_GRAPH_PARTICIPANT_ID=' + startEndNode.performer + ',CONNECTING_ACTIVITY_ID=' + startEndNode.id + ',X_OFFSET=' + parseInt(startEndNode.x) + ',Y_OFFSET=' + parseInt(startEndNode.y) + ',JaWE_GRAPH_TRANSITION_STYLE=NO_ROUTING_ORTHOGONAL,TYPE=' + type +'"/>';
            }

            // close extended attributes
            xml += '</xpdl:ExtendedAttributes>';

            // close process
            xml += '</xpdl:WorkflowProcess>';
        }

        // close processes
        xml += '</xpdl:WorkflowProcesses>';

        // add package extended attributes
        var extAttr = '<xpdl:ExtendedAttributes>\
                <xpdl:ExtendedAttribute Name="EDITING_TOOL" Value="Web Workflow Designer"/>\
                <xpdl:ExtendedAttribute Name="EDITING_TOOL_VERSION" Value="5.0-pre-alpha"/>\
                </xpdl:ExtendedAttributes>';
        xml += extAttr;

        // close package
        xml += '</xpdl:Package>';

        // return xml
        var result = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n' + xml;
        return result;
    },
    validate: function() {
        var designInvalid = false;
        // clear previous validation errors
        $(".invalidProcess, .invalidNode").removeClass("invalidProcess").removeClass("invalidNode");
        $(".invalidNodeMessage").remove();
        // get model
        var model = ProcessBuilder.Designer.model;
        for (var processId in model.processes) {
            // validate process
            var processInvalid = false;
            var process = model.processes[processId];
            var activities = process.activities;
            var transitions = process.transitions;
            for (var actId in activities) {
                var activityInvalid = false;
                var deadlineInvalid = false;
                var act = activities[actId];
                // validate activity transitions
                if (!act.joinTransitions || act.joinTransitions.length === 0) {
                    // no incoming transitions, check for start node
                    var startId = "start_" + actId;
                    if (!process.startEndNodes[startId]) {
                        activityInvalid = true;
                    }
                }
                if (!act.splitTransitions || act.splitTransitions.length === 0) {
                    // no outgoing transitions, check for start node
                    var endId = "end_" + actId;
                    if (!process.startEndNodes[endId]) {
                        activityInvalid = true;
                    }
                }
                if (act.deadlines && act.deadlines.length > 0) {
                    if (!act.splitTransitions || act.splitTransitions.length === 0) {
                        deadlineInvalid = true;
                    } else {

                        for (var d in act.deadlines) {
                            var deadline = act.deadlines[d];
                            var name = deadline.exceptionName;
                            var found = false;

                            for (var t=0; t<transitions.length; t++) {
                                var pt = transitions[t];
                                if ($.inArray(pt.id, act.splitTransitions) > -1 && pt.type === 'EXCEPTION' && pt.exceptionName === name) {
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                deadlineInvalid = true;
                                break;
                            }
                        }
                    }
                }
                if (activityInvalid || deadlineInvalid) {
                    // only hilite in current process in canvas
                    if (ProcessBuilder.Designer.currentProcessDefId === processId) {
                        var $node = $("#canvas").find("#node_" + act.id);
                        $node.addClass("invalidNode");
                        var messageTransition = get_pbuilder_msg("pbuilder.label.missingTransition");
                        var messageDeadline = get_pbuilder_msg("pbuilder.label.unhandleDeadline");
                        var message = "";
                        if (activityInvalid) {
                            message += '<p>' + messageTransition +'</p>';
                        }
                        if (deadlineInvalid) {
                            message += '<p>' + messageDeadline +'</p>';
                        }
                        var $nodeMessage = $('<div class="invalidNodeMessage">' + message +'</div>');
                        $node.append($nodeMessage);
                    }
                    processInvalid = true;
                    designInvalid = true;
                }
            }
//            var startEndNodes = process.startEndNodes;
//            for (var startendId in startEndNodes) {
//                var startend = startEndNodes[startendId];
//                if (startend.performer === performer.id) {
//                    delete process.startEndNodes[startendId];
//                }
//            }
            if (processInvalid) {
               var $processLink = $("#subheader_list").find("#" + processId);
               $processLink.addClass("invalidProcess");
            }
        }
        return !designInvalid;
    },
    validateConditions : function (name, value) {
        try {
            var model = ProcessBuilder.Designer.model;
            var currentProcessDefId = ProcessBuilder.Designer.currentProcessDefId;
            var currentProcess = model.processes[currentProcessDefId];

            var executionStatement = "";

            //assign number as variable value for checking;
            for (var df=0; df<currentProcess.dataFields.length; df++) {
                var dataField = currentProcess.dataFields[df];

                executionStatement += "var " + dataField.variableId + " = \"0\";\n";
            }

            if ($.isArray(value)) {
                executionStatement += ProcessBuilder.Designer.buildConditions(value) + ";";
            } else {
                executionStatement += value + ";";
            }

            var result = eval(executionStatement);

            if (result === true || result === false) {
                return null;
            } else {
                return get_pbuilder_msg("pbuilder.label.invalidCondition");
            }
        } catch (err) {
            return get_pbuilder_msg("pbuilder.label.invalidCondition");
        };
        return null;
    },
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
    }
};
