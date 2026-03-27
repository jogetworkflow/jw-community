ProcessBuilder = {
    currentProcessData : {},
    draggingElementId : null,
    readonly: false,
    updatePasteElement: true,
    changeNodeId: false,
    disableUpdate: false,
    
    /*
     * Intialize the builder, called from CustomBuilder.initBuilder
     */
    initBuilder: function (callback) {
        $("#save-btn").parent().after('<div class="btn-group me-1 float-end" style="margin-top:-16px;" role="group"><button class="btn btn-secondary btn-icon" id="launch-btn" title="'+get_cbuilder_msg("pbuilder.label.runProcess")+'"><i class="las la-play"></i> <span>'+get_cbuilder_msg("pbuilder.label.runProcess")+'</span></button></div>');
        $("#launch-btn").on("click", function(){
            if(!CustomBuilder.isSaved()){
                UI.alert(get_cbuilder_msg("cbuilder.pleaseSaveChangeToContinue"));
            } else {
                var url = CustomBuilder.contextPath + '/web/client/app' + CustomBuilder.appPath + '/process/' + ProcessBuilder.currentProcessData.properties.id;
                if ($('body').attr('builder-theme') === undefined){
                    url += "?__a_="+CustomBuilder.appId+"&__u_=_builder_classic_mode";
                }
                if ($('body').attr('builder-theme') === "light") {
                    url += "?__a_="+CustomBuilder.appId+"&__u_=_builder_light_mode";
                }
                //dark mode
                if ($('body').attr('builder-theme') === "dark") {
                    url += "?__a_="+CustomBuilder.appId+"&__u_=_builder_dark_mode";
                }
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
                "updateElementId" : "ProcessBuilder.updateElementId",
                "copyNode" : "ProcessBuilder.copyNode",
                "renderXray" : "ProcessBuilder.renderXray",
                "pasteElement" : "ProcessBuilder.pasteElement",
                "renderTreeMenuAdditionalNode" : "ProcessBuilder.renderTreeMenuAdditionalNode",
                "pasteNode" : "ProcessBuilder.pasteNode",
                "removeAdditionalNode" : "ProcessBuilder.removeAdditionalNode",
                "getScreenshot" : "ProcessBuilder.getScreenshot",
                "renderNodeAddtionalData" : "ProcessBuilder.renderNodeAddtionalData",
                "afterRenderNodeAdditional" : "ProcessBuilder.afterRenderNodeAdditional",
                "beforeRenderNodeAdditional" : "ProcessBuilder.beforeRenderNodeAdditional",
                "changeNodeAddtionalTarget" : "ProcessBuilder.changeNodeAddtionalTarget",
                "modifyShowPropertiesData" : "ProcessBuilder.modifyShowPropertiesData",
                "parseDataToComponent" : "ProcessBuilder.parseDataToComponent",
                "adjustNodeAdditional" : "ProcessBuilder.adjustNodeAdditional" //set to dummy value, so the default implementation is not run for this builder
            }
        }, function() {
            $("#builder_canvas").before('<div id="process-selector"></div>');
            $("#builder_canvas #iframe-wrapper").css('display', 'none');
            $("#builder_canvas").append(`
                <div id="lf-container"></div>
            `);
            
            $("#style-properties-tab-link").find("i").replaceWith('<i class="las la-handshake"></i>');
            $("#style-properties-tab-link").find("span").text(get_cbuilder_msg('pbuilder.label.mapping'));
            
            $("#design-btn").after('<button class="btn btn-light" title="'+get_cbuilder_msg('pbuilder.label.listView')+'" id="listviewer-btn" type="button" data-bs-toggle="button" aria-pressed="false" data-cbuilder-view="listViewer" data-cbuilder-action="switchView"><i class="la la-list"></i> <span>'+get_cbuilder_msg('pbuilder.label.listView')+'</span></button>');
            
            $("#json-def-btn").after('<button class="btn btn-light" title="'+get_cbuilder_msg('pbuilder.label.xpdl')+'" id="xpdl-btn" type="button" data-bs-toggle="button" aria-pressed="false" data-cbuilder-view="xpdl" data-cbuilder-action="switchView" data-hide-tool data-view-control><i class="la la-code"></i></button>');
            
            $(".advanced-tools").after('<div class="btn-group toolbar-group" role="group">\
                <button id="auto-layout" class="btn btn-light"  title="'+get_cbuilder_msg('pbuilder.label.autoLayout')+'" data-cbuilder-action="autoLayout"><i class="las la-magic"></i></button>\
                <button id="hightlight" class="btn btn-light"  title="'+get_cbuilder_msg('pbuilder.label.highlight')+'" data-cbuilder-action="highlight"><i class="las la-highlighter"></i></button>\
                <button id="navigator" class="btn btn-light"  title="'+get_cbuilder_msg('pbuilder.label.navigator')+'" data-cbuilder-action="navigator"><i class="las la-map"></i></button></div>');

            $(".responsive-buttons").after('<div class="btn-group me-3 light-tools toolbar-group toolzoom-buttons float-end" role="group">\
                <button id="fit-screen" class="btn btn-light"  title="'+get_cbuilder_msg('pbuilder.label.fitScreen')+'" data-cbuilder-action="fitScreen"><i class="zmdi zmdi-aspect-ratio-alt"></i></button>\
                <button id="zoom-minus" class="btn btn-light"  title="'+get_cbuilder_msg('pbuilder.label.zoomOut')+' (90%)" data-cbuilder-action="zoomMinus"><i class="las la-search-minus"></i></button>\
                <button id="zoom-plus" class="btn btn-light"  title="'+get_cbuilder_msg('pbuilder.label.zoomIn')+' (110%)" data-cbuilder-action="zoomPlus"><i class="las la-search-plus"></i></button></div>');
            
            ProcessBuilder.initComponents();
            CustomBuilder.Builder.setHead('<link data-pbuilder-style href="' + CustomBuilder.contextPath + '/pbuilder/css/pbuilder.css" rel="stylesheet" />');

            ProcessBuilder.initialLogicFlow();
            
            $(window).off('hashchange');
            $(window).on('hashchange', function(){
                //remove all error messages when switch process
                $('.toast').each(function(){
                    $(this).toast("hide");
                });
                    
                var id = window.location.hash.replace("#", "");
                
                //when no current process data or current process data is not match with the id in URL hash
                if (ProcessBuilder.currentProcessData === null || ProcessBuilder.currentProcessData === undefined || (ProcessBuilder.currentProcessData.properties === undefined || (ProcessBuilder.currentProcessData.properties !== undefined && id !== ProcessBuilder.currentProcessData.properties.id))) {
                    ProcessBuilder.viewProcess();
                }
            });
            
            $("#lf-container").on("click", ".previewForm", function(e){
                ProcessBuilder.previewForm(e);
            });

            var deferreds = [];
            
            var wait = $.Deferred();
            deferreds.push(wait);
            
            ProcessBuilder.cachePlugins(deferreds);
            ProcessBuilder.getMultiToolsProps(deferreds);
            ProcessBuilder.getForms(deferreds);
            
            wait.resolve();
            
            $.when.apply($, deferreds).then(function() {
                if (callback) {
                    callback();
                }
            });
        });
    },

    initTools: function () {
        const tools = ProcessBuilder.availableTools;
        for (const key in tools) {
            if (tools.hasOwnProperty(key)) {
                const tool = tools[key];
                //Tool
                CustomBuilder.initPaletteElement(get_cbuilder_msg('pbuilder.label.presetTool'), "Tool_" + key, tool.label, tool.icon,
                    [], { 'join': '', 'split': '' }, true, "", {
                    builderTemplate: {
                        'dragHtml': '<div class="node tool"><div class="node_label">' + tool.label + '</div></div>',
                        'draggable': true,
                        'movable': false,
                        'deletable': true,
                        'copyable': true,
                        'navigable': false,
                        'absolutePosition': true,
                        'parentContainerAttr': 'activities',
                        'parentDataHolder': 'activities',
                        'getStylePropertiesDefinition': ProcessBuilder.getToolDef,
                        'nodeDetailContainerColorNumber': function () {
                            return 4;
                        }
                    }
                });

                setTimeout(() => { // Delay to ensure DOM has been updated
                    let newStr = key.replace(/\./g, "_");
                    const paletteElement = document.querySelector("#Tools_Tool_" + newStr);
        
                    paletteElement.addEventListener('mousedown', (event) => {
                        ProcessBuilder.lf.dnd.startDrag({
                            type: "bpmn:serviceTask",
                        });
                        ProcessBuilder.draggingElementId = "Tool_" + key;
                    });
                }, 100);
            }
        }
    },

    cachePlugins : function(deferreds) {
        ProcessBuilder.getAssignmentFormModifier(deferreds);
        ProcessBuilder.getStartProcessFormModifier(deferreds);
        ProcessBuilder.getTools(deferreds);
        ProcessBuilder.getDecisionPlugin(deferreds);
        ProcessBuilder.getParticipants(deferreds);
    },

    initialLogicFlow: function () {
        const { DndPanel } = Extension;
        const { MiniMap } = Extension;
        const { Snapshot } = Extension;
        const { DynamicGroup } = Extension;
        const { ContextMenu } = Extension;
        const { BpmnElement } = Extension;
        const { AutoLayout } = Extension;
        const { FlowPath } = Extension;
        const { BPMNElements } = Extension;
        const { Menu } = Extension;
        const { Group } = Extension;
        const { Highlight } = Extension;
        const {TaskNodeFactory} = Extension;
        const {sequenceFlowFactory} = Extension;
        const {h} = Core;
        const {RectNode} = Core;
        const {RectNodeModel} = Core;
        const {LineEdge} = Core;
        const {LineEdgeModel} = Core;
        const miniMapOptions = {
            isShowHeader: false,
            isShowCloseIcon: false,
            headerTitle: 'MiniMap',
            width: 200,
            height: 120,
            rightPosition: 10,
            topPosition: 5
        };
        const theme = {
            arrow: {
                offset: 8, // arrow length
                verticalLength: 4, // distance of the arrow perpendicular to the edge
                fill: "none",
                stroke: "#282828"
            },
            baseEdge: {
                strokeWidth: 1,
                stroke: "red"
            },
            polyline: {
                stroke: '#282828'
            },
            edgeText: {
                textWidth: 100,
                overflowMode: "default",
                fontSize: 20,
                background: {
                  fill: "transparent"
                }
            }
        };

        ProcessBuilder.lf = new Core.default({
            container: document.querySelector('#lf-container'),
            grid: true,
            grid: {
                type: 'dot',
                size: 20
            },
            style: theme,
            background: {
                backgroundColor: "#ffffff"
            },
            isSilentMode: ProcessBuilder.readonly,
            plugins: [
                AutoLayout,
                Group,
                BPMNElements,
                ContextMenu,
                DndPanel,
                Menu,
                MiniMap,
                Snapshot,
                DynamicGroup,
                BpmnElement,
                Highlight,
                TaskNodeFactory,
                sequenceFlowFactory,
                FlowPath],
            pluginsOptions: {
                label: {
                    isMultiple: false,
                    labelWidth: 90,
                    // textOverflowMode -> 'ellipsis' | 'wrap' | 'clip' | 'nowrap' | 'default'
                    textOverflowMode: 'default',
                    allowRotate: true,
                    allowResize: true,
                    style: {
                        "backgroundColor": "transparent"
                    }
                },
                miniMap: {
                    ...miniMapOptions
                }
            }
        });

        const icon = '';
        const activityNode = TaskNodeFactory('bpmn:userTask', icon);
        const toolNode = TaskNodeFactory('bpmn:serviceTask', icon);
        const startNode = TaskNodeFactory('bpmn:startEvent', icon);
        const routeNode = TaskNodeFactory('bpmn:exclusiveGateway', icon);
        const endNode = TaskNodeFactory('bpmn:endEvent', icon);
        const transition = sequenceFlowFactory('bpmn:sequenceFlow', icon);

        class activityNodeView extends activityNode.view{
            // customize node appearance
            getShape() {
                const { model } = this.props;
                const { x, y, width, height, properties, isHovered, isSelected} = model;
                const { limit, mapping_act_formId } = properties;
                var newWidth = 120;
                const style = model.getNodeStyle();
                const { opacity = 1 } = style;
                
                // Conditionally rendering the text element based on the 'limit' property
                const textElement = limit ? h('text', {
                    x: x + newWidth / 2 - 15,  // Positioning at the top-right (20px from the right edge)
                    y: y - height / 2 + 15,    // Positioning 15px down from the top edge
                    fill: style.stroke,         // Text color (same as stroke color)
                    fontSize: '12',             // Font size for the text
                    fontWeight: 'normal',       // Text style
                    textAnchor: 'middle',       // Horizontal alignment
                    alignmentBaseline: 'middle',// Vertical alignment
                    opacity
                }, limit + ProcessBuilder.currentProcessData.properties.durationUnit.toLowerCase()) : null;
                

                const xrayView = h('foreignObject', {
                    class: isSelected?'xray-view current':'xray-view',
                    x: x - newWidth / 2,
                    y: y - height / 2,
                    width: newWidth,
                    height: height,
                    opacity: isHovered ? 1 : 0.6
                });

                const invalidNode = h('foreignObject', {
                    class: 'invalidNodeMsg',
                    x: x - newWidth / 2,
                    y: y - height / 2,
                    width: newWidth,
                    height: height,
                    opacity: 1
                });
                
                return h('g', {id: model.id, 
                                class: 'node ' + properties.className, 
                                'data-cbuilder-visible': true,
                                'data-cbuilder-classname':properties.className,
                                'data-cbuilder-id': model.id,
                                'data-cbuilder-label': model.properties.label,
                                'data-cbuilder-absolute-position': true,
                                x: x - newWidth / 2,   // Centering the rectangle
                                y: y - height / 2,
                                height: height}, [
                                xrayView,
                                invalidNode,
                    h('rect', {
                        ...style,
                        x: x - newWidth / 2,   // Centering the rectangle
                        y: y - height / 2,
                        rx: 5,
                        ry: 5,
                        width: newWidth,
                        height
                    }),

                    // Optional font icon if `mapping_act_formId` is defined and non-empty
                    mapping_act_formId && mapping_act_formId !== "" ? h('foreignObject', {
                        x: x + (newWidth / 2) - 25, // Adjust to center horizontally
                        y: y + (height / 2) - 25, // Adjust to center vertically
                        width: 24,
                        height: 24
                    }, [
                        h('div', {
                            xmlns: 'http://www.w3.org/1999/xhtml', // Required for embedding HTML
                            title: ProcessBuilder.availableForms[mapping_act_formId],
                            style: {
                                fontSize: '18px',       // Icon size
                                color: style.stroke,       // Icon color
                                textAlign: 'center',
                                lineHeight: '24px',     // Vertically aligns the icon
                                width: '24px',
                                height: '24px',
                                opacity
                            }
                        }, [
                            h('i', {
                                className: 'las la-file-alt previewForm',
                                formId: mapping_act_formId
                            })
                        ])
                    ]) : null, // Render nothing if condition is not met
                    textElement      // Conditionally added text element
                ]);
            }
        }

        class toolNodeView extends toolNode.view {
            // Custom node appearance
            getShape() {
                const { model } = this.props;
                const { id, x, y, width, height, radius, properties, isHovered, isSelected} = model;
                const { tools } = properties;
        
                const newWidth = 120;
                const style = model.getNodeStyle();
                const { opacity = 1 } = style;
        
                // Variables for the icon and label
                let icon = "";
                let label = "";
                let iconClass = "";
                let toolLength = "";
                let adjustX = "";
                let iconColor = "#386f5c";
        
                // Process tools if they exist
                if (properties !== undefined && tools !== undefined && tools.length > 0) {
                    for (let i in tools) {
                        if (label !== "") {
                            label += "\n";
                        }
                        if (tools.length > 1) {
                            label += `${parseInt(i) + 1}. `;
                        }
        
                        const p = tools[i];
                        const plugin = ProcessBuilder.availableTools[p.className];
                        if (plugin === undefined) {
                            icon = '<i class="las la-exclamation-triangle" style="color:red;"></i>';
                            label += `${p.className} (${get_advtool_msg('dependency.tree.Missing.Plugin')})`;
                            iconColor = "#ff4d4f";
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
                    if (tools.length > 1) {
                        toolLength = ` ${tools.length}`;
                        adjustX = 5;
                    }
        
                    // Extract icon class from the HTML string
                    const tempElement = document.createElement('div');
                    tempElement.innerHTML = icon;
                    iconClass = tempElement.firstElementChild?.getAttribute('class') || '';
                }

                const xrayView = h('foreignObject', {
                    class: isSelected?'xray-view current':'xray-view',
                    x: x - newWidth / 2,
                    y: y - height / 2,
                    width: newWidth,
                    height: height,
                    opacity: isHovered ? 1 : 0.6
                });

                const invalidNode = h('foreignObject', {
                    class: 'invalidNodeMsg',
                    x: x - newWidth / 2,
                    y: y - height / 2,
                    width: newWidth,
                    height: height,
                    opacity: 1
                });
                
                $('#' + id + ' i').removeClass().addClass(iconClass + ' svg2Icon');
                // Return the shape with the custom icon and label
                return h('g', {id: model.id, class: 'node ' + properties.className, 
                    'data-cbuilder-visible': true,
                    'data-cbuilder-classname':properties.className,
                    'data-cbuilder-id': model.id,
                    'data-cbuilder-label': model.properties.label,
                    'data-cbuilder-absolute-position': true,
                    x: x - newWidth / 2, // Center the rectangle
                    y: y - height / 2,
                    height: height}, [
                    xrayView,
                    invalidNode,
                    h('rect', {
                        ...style,
                        x: x - newWidth / 2, // Center the rectangle
                        y: y - height / 2,
                        rx: 5,
                        ry: 5,
                        width: newWidth,
                        height
                    }),
                    h('foreignObject',
                        {
                            x: x + 30 - adjustX,
                            y: y + 10,
                            width: 32,
                            height: 32
                        },
                        [
                            h('div',
                                {
                                    xmlns: 'http://www.w3.org/1999/xhtml',
                                    title: label,
                                    style: {
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        fontSize: '18px',
                                        color: iconColor,
                                        textAlign: 'center',
                                        lineHeight: '24px',
                                        width: '32px',
                                        height: '32px',
                                        opacity,
                                        gap: '4px' // Spacing between icon and number
                                    }
                                },
                                [
                                    h('i', {
                                        className: iconClass + ' svg2Icon',
                                        opacity
                                    }),
        
                                    // Conditional rendering for the number (toolLength)
                                    ...(toolLength
                                        ? [
                                            h('span', {
                                                style: { fontSize: '12px', color: 'black' } // Style for the number
                                            }, toolLength)
                                        ]
                                        : []
                                    )
                                ]
                            )
                        ]
                    )
                ]);
            }
        }

        class startNodeView extends startNode.view {
            // Custom node appearance
            getShape() {
                const { model } = this.props;
                const { x, y, width, height, properties, isHovered, isSelected} = model;
                const style = model.getNodeStyle();
                const { opacity = 1 } = style;
                const { mapping_act_formId } = properties;
                
                const xrayView = h('foreignObject', {
                    class: isSelected?'xray-view current':'xray-view',
                    x: x - width / 2,
                    y: y - height / 2,
                    width: width,
                    height: height,
                    opacity: isHovered ? 1 : 0.6
                });

                const invalidNode = h('foreignObject', {
                    class: 'invalidNodeMsg',
                    x: x - width / 2,
                    y: y - height / 2,
                    width: width,
                    height: height,
                    opacity: 1
                });
        
                return h('g', {id: model.id, class: 'node ' + properties.className, 
                    'data-cbuilder-visible': true,
                    'data-cbuilder-classname':properties.className,
                    'data-cbuilder-id': model.id,
                    'data-cbuilder-label': model.properties.label,
                    'data-cbuilder-absolute-position': true,
                    x: x - width / 2, // Centering the rectangle
                    y: y - height / 2,
                    height: height}, [
                    xrayView,
                    invalidNode,
                    h('rect', {
                        ...style,
                        x: x - width / 2, // Centering the rectangle
                        y: y - height / 2,
                        rx: 20, // Rounded corners
                        ry: 20,
                        width: width,
                        height: height
                    }),
        
                    // Optional font icon if `mapping_act_formId` is defined and non-empty
                    mapping_act_formId && mapping_act_formId !== "" ? h('foreignObject', {
                        x: x - 12, // Adjust to center horizontally
                        y: y - 12, // Adjust to center vertically
                        width: 24,
                        height: 24
                    }, [
                        h('div', {
                            xmlns: 'http://www.w3.org/1999/xhtml', // Required for embedding HTML
                            title: ProcessBuilder.availableForms[mapping_act_formId],
                            style: {
                                fontSize: '18px',       // Icon size
                                color: '#52c41a',       // Icon color
                                textAlign: 'center',
                                lineHeight: '24px',     // Vertically aligns the icon
                                width: '24px',
                                height: '24px',
                                opacity
                            }
                        }, [
                            h('i', {
                                className: 'las la-file-alt', // Replace with your desired Font Awesome icon class
                                opacity
                            })
                        ])
                    ]) : null // Render nothing if condition is not met
                ]);
            }
        }

        class routeNodeView extends routeNode.view {
            // Custom icon for the gateway
            getLabelShape() {
                const { model } = this.props;
                const { x, y, width, height, properties} = model;
                const { join, split } = properties;
                const style = model.getNodeStyle();
                const { opacity = 1 } = style; // Default opacity

                const children = [
                    h('polygon', {
                        fill: style.fill,
                        stroke: style.stroke,
                        strokeWidth: 2,
                        points: '25,0 50,25 25,50 0,25', // Diamond coordinates
                        opacity
                    })
                ];

                if (join === 'AND' || split === 'AND') {
                    children.push(
                        h('path', {
                            d: 'm 16,15 7.42857142857143,9.714285714285715 -7.42857142857143,9.714285714285715 3.428571428571429,0 5.714285714285715,-7.464228571428572 5.714285714285715,7.464228571428572 3.428571428571429,0 -7.42857142857143,-9.714285714285715 7.42857142857143,-9.714285714285715 -3.428571428571429,0 -5.714285714285715,7.464228571428572 -5.714285714285715,-7.464228571428572 -3.428571428571429,0 z',
                            fill: style.fill,
                            stroke: style.stroke,
                            strokeWidth: 2,
                            opacity
                        })
                    );
                }

                return h(
                    'g',
                    { transform: `translate(${x - width / 2}, ${y - height / 2})` },
                    children
                );
            }

            // Custom node appearance
            getShape() {
                const { model } = this.props;
                const { x, y, width, height, properties, isHovered, isSelected} = model;
                const { mapping_act_plugin } = properties;

                const xrayView = h('foreignObject', {
                    class: isSelected?'xray-view current':'xray-view',
                    x: x - width / 2,
                    y: y - height / 2,
                    width: width,
                    height: height,
                    opacity: isHovered ? 1 : 0.6
                });

                const invalidNode = h('foreignObject', {
                    class: 'invalidNodeMsg',
                    x: x - width / 2,
                    y: y - height / 2,
                    width: width,
                    height: height,
                    opacity: 1
                });
            
                return h('g', {id: model.id, class: 'node ' + properties.className, 
                    'data-cbuilder-visible': true,
                    'data-cbuilder-classname':properties.className,
                    'data-cbuilder-id': model.id,
                    'data-cbuilder-label': model.properties.label,
                    'data-cbuilder-absolute-position': true,
                    x: x - width / 2,
                    y: y - height / 2,
                    height: height}, [
                    xrayView,
                    invalidNode,
                    this.getLabelShape(),
                    
                    // Add icon if `mapping_act_plugin` is defined
                    mapping_act_plugin &&
                    mapping_act_plugin !== "" &&
                    mapping_act_plugin.className !== "" &&
                    mapping_act_plugin.className !== undefined
                        ? (() => {
                              const plugin =
                                  ProcessBuilder.availableDecisionPlugin[
                                      mapping_act_plugin.className
                                  ];
                              if (plugin === undefined) {
                                  return h(
                                      'foreignObject',
                                      {
                                          x: x + 8,
                                          y: y + 8,
                                          width: 24,
                                          height: 24
                                      },
                                      [
                                          h(
                                              'div',
                                              {
                                                  xmlns: 'http://www.w3.org/1999/xhtml',
                                                  title:
                                                      mapping_act_plugin.className +
                                                      " (" +
                                                      get_advtool_msg(
                                                          'dependency.tree.Missing.Plugin'
                                                      ) +
                                                      ")",
                                                  style: {
                                                      fontSize: '14px',
                                                      color: '#ff4d4f',
                                                      textAlign: 'center',
                                                      lineHeight: '24px',
                                                      width: '24px',
                                                      height: '24px'
                                                  }
                                              },
                                              [
                                                  h('i', {
                                                      className:
                                                          'las la-exclamation-triangle'
                                                  })
                                              ]
                                          )
                                      ]
                                  );
                              } else {
                                  const icon = plugin.icon || '<i class="las la-cog"></i>';
                                  const tempElement = document.createElement('div');
                                  tempElement.innerHTML = icon;

                                  // Retrieve the class of the first child (the <i> element)
                                  const iconClass = tempElement.firstElementChild?.getAttribute('class') || '';
                                  return h(
                                      'foreignObject',
                                      {
                                          x: x + 8,
                                          y: y + 8,
                                          width: 24,
                                          height: 24
                                      },
                                      [
                                          h(
                                              'div',
                                              {
                                                  xmlns: 'http://www.w3.org/1999/xhtml',
                                                  title: plugin.label,
                                                  style: {
                                                      fontSize: '14px',
                                                      color: '#4c90ff',
                                                      textAlign: 'center',
                                                      lineHeight: '24px',
                                                      width: '24px',
                                                      height: '24px'
                                                  }
                                              },
                                              [
                                                  h('i', {
                                                      className: iconClass
                                                  })
                                              ]
                                          )
                                      ]
                                  );
                              }
                          })()
                        : null
                ]);
            }
        }

        class endNodeView extends endNode.view {
            // Custom label for the inner circles
            getLabelShape() {
                const { model } = this.props;
                const { x, y} = model; // Coordinates of the node
                const style = model.getNodeStyle();
                const { opacity = 1 } = style;
        
                return h('g', {}, [
                    // Outer circle
                    h('circle', {
                        cx: x,
                        cy: y,
                        r: 18, // Radius of the outer circle
                        fill: '#fff',
                        fillOpacity: 1,
                        stroke: style.stroke,
                        strokeWidth: 2,
                        strokeOpacity: 1,
                        opacity
                    }),
                    // Inner circle
                    h('circle', {
                        cx: x,
                        cy: y,
                        r: 13, // Radius of the inner circle
                        fill: style.stroke,
                        stroke: style.stroke,
                        strokeWidth: 2,
                        opacity
                    })
                ]);
            }
        
            // Node shape including the circles
            getShape() {
                const { model } = this.props;
                const { x, y, width, height, properties, isHovered, isSelected} = model;

                const xrayView = h('foreignObject', {
                    class: isSelected?'xray-view current':'xray-view',
                    x: x - width / 2,
                    y: y - height / 2,
                    width: width,
                    height: height,
                    opacity: isHovered ? 1 : 0.6
                });

                const invalidNode = h('foreignObject', {
                    class: 'invalidNodeMsg',
                    x: x - width / 2,
                    y: y - height / 2,
                    width: width,
                    height: height,
                    opacity: 1
                });
        
                return h('g', {id: model.id, class: 'node ' + properties.className,
                    'data-cbuilder-visible': true,
                    'data-cbuilder-classname':properties.className,
                    'data-cbuilder-id': model.id,
                    'data-cbuilder-label': model.properties.label,
                    'data-cbuilder-absolute-position': true,
                    x: x - width / 2,
                    y: y - (height/2),
                    height: height}, [
                    xrayView,
                    invalidNode,
                    // Outer container or other elements if needed
                    this.getLabelShape() // The double-circle shape
                ]);
            }
        }

        class subflowView extends RectNode {
            // Custom label for the inner rectangles
            getLabelShape() {
                const { model } = this.props;
                const { x, y, width, height } = model;
                const style = model.getNodeStyle();
                const { opacity = 1 } = style;
        
                return h('g', {}, [
                    // Outer rectangle
                    h('rect', {
                        x: x - width / 2,
                        y: y - height / 2,
                        width: width,
                        height: height,
                        fill: '#fff',
                        fillOpacity: 1,
                        stroke: style.stroke,
                        strokeWidth: 1,
                        strokeOpacity: 1,
                        opacity,
                        rx: 3, // Optional: Rounded corners
                        ry: 3
                    }),
                    // Inner rectangle
                    h('rect', {
                        x: (x - width / 2) + 5,
                        y: (y - height / 2) + 5,
                        width: width - 10,
                        height: height - 10,
                        fill: '#fff',
                        stroke: style.stroke,
                        strokeWidth: 1,
                        opacity,
                        rx: 3, // Rounded corners
                        ry: 3
                    })
                ]);
            }
        
            // Node shape including the rectangles
            getShape() {
                const { model } = this.props;
                const { x, y, width, height, properties, isHovered, isSelected} = model;

                const xrayView = h('foreignObject', {
                    class: isSelected?'xray-view current':'xray-view',
                    x: x - width / 2,
                    y: y - height / 2,
                    width: width,
                    height: height,
                    opacity: isHovered ? 1 : 0.6
                });

                const invalidNode = h('foreignObject', {
                    class: 'invalidNodeMsg',
                    x: x - width / 2,
                    y: y - height / 2,
                    width: width,
                    height: height,
                    opacity: 1
                });
        
                return h('g', {id: model.id, class: 'node ' + properties.className, 
                    'data-cbuilder-visible': true,
                    'data-cbuilder-classname':properties.className,
                    'data-cbuilder-id': model.id,
                    'data-cbuilder-label': model.properties.label,
                    'data-cbuilder-absolute-position': true,
                    x: x - width / 2,
                    y: y - height / 2,
                    height: height}, [
                    xrayView,
                    invalidNode,
                    // Outer container or other elements if needed
                    this.getLabelShape() // The double-rectangle shape
                ]);
            }
        }

        class transitionView extends transition.view {
            // Define how the edge should render
            getEdge() {
                const { model } = this.props;
                const style = model.getEdgeStyle();
                const { opacity = 1 } = style;
                const { properties, arrowConfig, points } = model;

                return h('g', {id: properties.id, class: properties.className},
                    h('polyline', { 
                        points,
                        fill: 'none',
                        stroke: style.stroke, // Default stroke color
                        opacity,
                        'stroke-width': style.strokeWidth, // Default stroke width
                        hoverStroke: style.fill, // Custom hover stroke
                        selectedStroke: style.fill, // Custom selected stroke
                        'marker-end': arrowConfig.markerEnd, // Marker for the end
                        'marker-start': arrowConfig.markerStart // Marker for the start
                    })
                );
            }
        }
        
        class straightTransitionView extends LineEdge {
            // Define how the edge should render
            getEdge() {
                const { model } = this.props;
                const edgeStyle = model.getEdgeStyle();
                const { opacity = 1 } = edgeStyle;
                const { startPoint, endPoint, properties, arrowConfig } = model;
                const lineData = {
                    x1: startPoint.x,
                    y1: startPoint.y,
                    x2: endPoint.x,
                    y2: endPoint.y,
                };
                return h('g', {id: properties.id, class: properties.className},
                    h("line", { ...lineData, ...edgeStyle, ...arrowConfig })
                );
            }
        }

        class activityNodeModel extends activityNode.model {
            initNodeData(data) {
                super.initNodeData(data);                
                this.text.editable = false; 
            }

            // Set the model shape attribute, triggered every time properties change, including during initialization.
            setAttributes() {
                const { scale = 1, width = 120, height = 80 } = this.properties;
                // manually set the shape attribute
                this.width = width * scale;
                this.height = height * scale;
            }

            // Customize text style
            getTextStyle() {
                const style = super.getTextStyle();
                const nodeStyle = super.getNodeStyle();
                const { opacity = 1 } = nodeStyle;

                style.opacity = opacity;
                style.fontSize = 12;
                style.overflowMode = 'autowrap';
                style.lineHeight = 1.2;
                style.width = '70px';
                const { isClicked } = this.properties;
                style.color = isClicked ? 'red' : '#000000';
                return style;
            }

            // Customize node style
            getNodeStyle() {
                const style = super.getNodeStyle();
                let { status } = this.properties;
                if (status === 'invalid') {
                    style.stroke = '#ff4d4f';
                } else {
                    style.stroke = '#282828';
                }
                style.fill = '#ffffff';
                style.strokeWidth = '1',
                style.rx = '18';
                style.zIndex = 1;
                return style;
            }

            // Customize anchor point style attributes
            getAnchorStyle() {
                const style = super.getAnchorStyle();
                const newStyle = Object.assign({}, style, {
                    stroke: 'rgb(24, 125, 255)',
                    r: 4.5,
                    hover: {
                        r: 7,
                        fill: 'rgb(24, 125, 255)',
                        stroke: 'rgb(24, 125, 255)'
                    }
                });
                return newStyle;
            }

            // Customize the style attributes of the connection line dragged from the node anchor point
            getAnchorLineStyle() {
                const style = super.getAnchorLineStyle();
                style.stroke = '#282828';
                return style;
            }

            // Customize the style attributes of the node outline box
            getOutlineStyle() {
                const { danger } = this.properties;
                const style = super.getOutlineStyle();
                style.strokeWidth = 2;
                style.strokeDasharray = '0';
                style.rx = '10';
                if (danger) {
                    style.stroke = '#FF4D4F ';
                    style.fill = 'rgba(255, 77, 79, 0.1)';
                    style.hover.stroke = '#FF4D4F';
                    style.hover.fill = 'rgba(255, 77, 79, 0.1)';
                } else {
                    style.stroke = '#4285f4';
                    style.fill = 'rgba(66, 133, 244, 0.1)';
                    style.hover.stroke = '#54c5fc';
                    style.hover.fill = 'rgba(255, 255, 255, 0.1)';
                }
            
                return style;
            }
        }

        class toolNodeModel extends toolNode.model {
            initNodeData(data) {
                super.initNodeData(data);                
                this.text.editable = false; 
            }

            // Set the model shape attribute, triggered every time properties change, including during initialization.
            setAttributes() {
                const { scale = 1, width = 120, height = 80 } = this.properties;
                // manually set the shape attribute
                this.width = width * scale;
                this.height = height * scale;
            }

            // Customize text style
            getTextStyle() {
                const style = super.getTextStyle();
                const nodeStyle = super.getNodeStyle();
                const { opacity = 1 } = nodeStyle;
                
                style.opacity = opacity;
                style.fontSize = 12;
                style.overflowMode = 'autowrap';
                style.width = '70px';
                style.lineHeight = 1.2;
                style.fontWeight = 500;
                style.color = '#386f5c';
                return style;
            }

            // Customize node style
            getNodeStyle() {
                const style = super.getNodeStyle();
                let { status } = this.properties;
                if (status === 'invalid') {
                    style.stroke = '#ff4d4f';
                } else {
                    style.stroke = '#386f5c';
                }
                if ($('body').attr('builder-theme') === 'dark') {
                    style.fill = '#c0eed2';
                } else {
                    style.fill = '#ebfdf2';
                }
                
                style.strokeWidth = '1';
                style.rx = '18';
                style.zIndex = 1;
                return style;
            }

            // Customize anchor point style attributes
            getAnchorStyle() {
                const style = super.getAnchorStyle();
                const newStyle = Object.assign({}, style, {
                    stroke: 'rgb(24, 125, 255)',
                    r: 4.5,
                    hover: {
                        r: 7,
                        fill: 'rgb(24, 125, 255)',
                        stroke: 'rgb(24, 125, 255)'
                    }
                });
                return newStyle;
            }

            // Customize the style attributes of the connection line dragged from the node anchor point
            getAnchorLineStyle() {
                const style = super.getAnchorLineStyle();
                style.stroke = '#282828';
                return style;
            }

            // Customize the style attributes of the node outline box
            getOutlineStyle() {
                const { danger } = this.properties;
                const style = super.getOutlineStyle();
                style.stroke = '#4285f4';
                style.fill = 'rgba(66, 133, 244, 0.1)';
                style.strokeWidth = 2;
                style.strokeDasharray = '0';
                style.rx = '10';
                if (danger) {
                    style.stroke = '#FF4D4F ';
                    style.fill = 'rgba(255, 77, 79, 0.1)';
                    style.hover.stroke = '#FF4D4F';
                    style.hover.fill = 'rgba(255, 77, 79, 0.1)';
                } else {
                    style.stroke = '#4285f4';
                    style.fill = 'rgba(66, 133, 244, 0.1)';
                    style.hover.stroke = '#54c5fc';
                    style.hover.fill = 'rgba(255, 255, 255, 0.1)';
                }
                return style;
            }
        }

        class subflowNodeModel extends RectNodeModel {
            initNodeData(data) {
                super.initNodeData(data);
                this.text.editable = false; 
            }

            // Set the model shape attribute, triggered every time properties change, including during initialization.
            setAttributes() {
                const { scale = 1, width = 120, height = 80 } = this.properties;
                // manually set the shape attribute
                this.width = width * scale;
                this.height = height * scale;
            }

            // Customize text style
            getTextStyle() {
                const style = super.getTextStyle();
                const nodeStyle = super.getNodeStyle();
                const { opacity = 1 } = nodeStyle;
                
                style.opacity = opacity;
                style.fontSize = 12;
                style.overflowMode = 'autowrap';
                style.lineHeight = 1.2;
                style.width = '70px';
                const { isClicked } = this.properties;
                style.color = isClicked ? 'red' : '#000000';
                return style;
            }

            // Customize node style
            getNodeStyle() {
                const style = super.getNodeStyle();
                let { status } = this.properties;
                if (status === 'invalid') {
                    style.stroke = '#ff4d4f';
                } else {
                    style.stroke = '#282828';
                }
                style.fill = '#ffffff';
                style.strokeWidth = '1',
                style.rx = '18';
                style.zIndex = 1;
                return style;
            }

            // Customize anchor point style attributes
            getAnchorStyle() {
                const style = super.getAnchorStyle();
                const newStyle = Object.assign({}, style, {
                    stroke: 'rgb(24, 125, 255)',
                    r: 4.5,
                    hover: {
                        r: 7,
                        fill: 'rgb(24, 125, 255)',
                        stroke: 'rgb(24, 125, 255)'
                    }
                });
                return newStyle;
            }

            // Customize the style attributes of the connection line dragged from the node anchor point
            getAnchorLineStyle() {
                const style = super.getAnchorLineStyle();
                style.stroke = '#282828';
                return style;
            }

            // Customize the style attributes of the node outline box
            getOutlineStyle() {
                const { danger } = this.properties;
                const style = super.getOutlineStyle();
            
                style.stroke = '#4285f4';
                style.fill = 'rgba(66, 133, 244, 0.1)';
                style.strokeWidth = 2;
                style.strokeDasharray = '0';
                style.rx = '10';
            
                if (danger) {
                    style.stroke = '#FF4D4F ';
                    style.fill = 'rgba(255, 77, 79, 0.1)';
                    style.hover.stroke = '#FF4D4F';
                    style.hover.fill = 'rgba(255, 77, 79, 0.1)';
                } else {
                    style.stroke = '#4285f4';
                    style.fill = 'rgba(66, 133, 244, 0.1)';
                    style.hover.stroke = '#54c5fc';
                    style.hover.fill = 'rgba(255, 255, 255, 0.1)';
                }
            
                return style;
            }
        }

        class startNodeModel extends startNode.model {
            initNodeData(data) {
                super.initNodeData(data);
                this.text.editable = false; 
            }

            // Set the model shape attribute, triggered every time properties change, including during initialization.
            setAttributes() {
                const { scale = 1, width = 36, height = 36 } = this.properties;
                // manually set the shape attribute
                this.width = width * scale;
                this.height = height * scale;
            }

            // Customize text style
            getTextStyle() {
                const style = super.getTextStyle();
                const nodeStyle = super.getNodeStyle();
                const { opacity = 1 } = nodeStyle;
                
                style.opacity = opacity;
                style.fontSize = 12;
                style.overflowMode = 'autowrap';
                style.lineHeight = 1.2;
                const { isClicked } = this.properties;
                style.color = isClicked ? 'red' : '#000000';
                return style;
            }

            // Customize node style
            getNodeStyle() {
                const style = super.getNodeStyle();
                let { status } = this.properties;
                if (status === 'invalid') {
                    style.stroke = '#ff4d4f';
                } else {
                    style.stroke = '#52c41a';
                }
                style.rx = '30';
                style.fill = '#f6ffed';
                style.zIndex = 1;
                return style;
            }

            // Customize anchor point style attributes
            getAnchorStyle() {
                const style = super.getAnchorStyle();
                const newStyle = Object.assign({}, style, {
                    stroke: 'rgb(24, 125, 255)',
                    r: 4.5,
                    hover: {
                        r: 7,
                        fill: 'rgb(24, 125, 255)',
                        stroke: 'rgb(24, 125, 255)'
                    }
                });
                return newStyle;
            }

            // Customize the style attributes of the connection line dragged from the node anchor point
            getAnchorLineStyle() {
                const style = super.getAnchorLineStyle();
                style.stroke = '#282828';
                return style;
            }

            // Customize the style attributes of the node outline box
            getOutlineStyle() {
                const { danger } = this.properties;
                const style = super.getOutlineStyle();
                style.strokeWidth = 2;
                style.strokeDasharray = '0';
                style.rx = '25';
                if (danger) {
                    style.stroke = '#FF4D4F ';
                    style.fill = 'rgba(255, 77, 79, 0.1)';
                    style.hover.stroke = '#FF4D4F';
                    style.hover.fill = 'rgba(255, 77, 79, 0.1)';
                } else {
                    style.stroke = '#4285f4';
                    style.fill = 'rgba(66, 133, 244, 0.1)';
                    style.hover.stroke = '#54c5fc';
                    style.hover.fill = 'rgba(255, 255, 255, 0.1)';
                }
                return style;
            }
        }

        class routeNodeModel extends routeNode.model {
            initNodeData(data) {
                super.initNodeData(data);
                this.text.editable = false; 
            }

            // Set the model shape attribute, triggered every time properties change, including during initialization.
            setAttributes() {
                const { scale = 1, width = 50, height = 50 } = this.properties;
                // manually set the shape attribute
                this.width = width * scale;
                this.height = height * scale;
            }

            // Customize text style
            getTextStyle() {
                const style = super.getTextStyle();
                const nodeStyle = super.getNodeStyle();
                const { opacity = 1 } = nodeStyle;
                
                style.opacity = opacity;
                style.fontSize = 12;
                style.overflowMode = 'autowrap';
                style.lineHeight = 1.2;
                const { isClicked } = this.properties;
                style.color = isClicked ? 'red' : '#000000';
                return style;
            }

            // Customize node style
            getNodeStyle() {
                const style = super.getNodeStyle();
                let { status } = this.properties;
                if (status === 'invalid') {
                    style.stroke = '#ff4d4f';
                } else {
                    style.stroke = '#faad14';
                }
                style.fill = '#fffbe6';
                style.zIndex = 1;
                return style;
            }

            // Customize anchor point style attributes
            getAnchorStyle() {
                const style = super.getAnchorStyle();
                const newStyle = Object.assign({}, style, {
                    stroke: 'rgb(24, 125, 255)',
                    r: 4.5,
                    hover: {
                        r: 7,
                        fill: 'rgb(24, 125, 255)',
                        stroke: 'rgb(24, 125, 255)'
                    }
                });
                return newStyle;
            }

            // Customize the style attributes of the connection line dragged from the node anchor point
            getAnchorLineStyle() {
                const style = super.getAnchorLineStyle();
                style.stroke = '#282828';
                return style;
            }

            // Customize the style attributes of the node outline box
            getOutlineStyle() {
                const { danger } = this.properties;
                const style = super.getOutlineStyle();
                style.strokeWidth = 2;
                style.strokeDasharray = '0';
                style.rx = '10';
                if (danger) {
                    style.stroke = '#FF4D4F ';
                    style.fill = 'rgba(255, 77, 79, 0.1)';
                    style.hover.stroke = '#FF4D4F';
                    style.hover.fill = 'rgba(255, 77, 79, 0.1)';
                } else {
                    style.stroke = '#4285f4';
                    style.fill = 'rgba(66, 133, 244, 0.1)';
                    style.hover.stroke = '#54c5fc';
                    style.hover.fill = 'rgba(255, 255, 255, 0.1)';
                }
                return style;
            }
        }

        class endNodeModel extends endNode.model {
            // Set the model shape attribute, triggered every time properties change, including during initialization.
            setAttributes() {
                const { scale = 1, width = 36, height = 36 } = this.properties;
                // manually set the shape attribute
                this.text.editable = false; 
                this.width = width * scale;
                this.height = height * scale;
            }

            // Customize text style
            getTextStyle() {
                const style = super.getTextStyle();
                const nodeStyle = super.getNodeStyle();
                const { opacity = 1 } = nodeStyle;
                
                style.opacity = opacity;
                style.fontSize = 12;
                style.overflowMode = 'autowrap';
                style.lineHeight = 1.2;
                const { isClicked } = this.properties;
                style.color = isClicked ? 'red' : '#000000';
                return style;
            }

            // Customize node style
            getNodeStyle() {
                const style = super.getNodeStyle();
                let { status } = this.properties;
                if (status === 'invalid') {
                    style.stroke = '#ff4d4f';
                } else {
                    style.stroke = '#ff4d4f';
                }
                style.zIndex = 1;
                return style;
            }

            // Customize anchor point style attributes
            getAnchorStyle() {
                return {
                  visibility: 'hidden' // Hide anchors
                };
              }

            getAnchorPoints() {
                return [];
            }

            // Customize the style attributes of the connection line dragged from the node anchor point
            getAnchorLineStyle() {
                return null;
            }

            // Customize the style attributes of the node outline box
            getOutlineStyle() {
                const { danger } = this.properties;
                const style = super.getOutlineStyle();
                style.strokeWidth = 2;
                style.strokeDasharray = '0';
                style.rx = '25';
                if (danger) {
                    style.stroke = '#FF4D4F ';
                    style.fill = 'rgba(255, 77, 79, 0.1)';
                    style.hover.stroke = '#FF4D4F';
                    style.hover.fill = 'rgba(255, 77, 79, 0.1)';
                } else {
                    style.stroke = '#4285f4';
                    style.fill = 'rgba(66, 133, 244, 0.1)';
                    style.hover.stroke = '#54c5fc';
                    style.hover.fill = 'rgba(255, 255, 255, 0.1)';
                }
                return style;
            }
        }

        class transitionModel extends transition.model {
            customTextPosition = true;

            setAttributes(data) {
                super.setAttributes(data);
                const { properties } = this;
                this.draggable = false; //disable resize the transition
                if(properties.type === "startend"){
                    this.isHitable = false;
                }
                this.text.editable = false; 
            }
            
            // Set transition style
            getEdgeStyle() {
                const style = super.getEdgeStyle();
                const { properties } = this;
                if (properties.type === "CONDITION") {
                    style.stroke = '#4096ff';
                    style.strokeWidth = 2;
                } else if (properties.type === "OTHERWISE") {
                    style.stroke = '#db973d';
                    style.strokeWidth = 2;
                } else if (properties.type === "EXCEPTION") {
                    style.stroke = '#ff4d4f';
                    style.strokeWidth = 2;
                } else if (properties.type === "startend") {
                    if ($('body').attr('builder-theme') === 'dark') {
                        style.stroke = '#9A9CAE';
                        style.strokeWidth = 2;
                    } else {
                        style.stroke = 'rgb(177, 177, 177)';
                        style.strokeWidth = 2;
                    }
                } else {
                    if ($('body').attr('builder-theme') === 'dark') {
                        style.stroke = '#ffffff';
                    } else {
                        style.stroke = '#000000';
                    }
                    
                }
                style.zIndex = 5;
                return style;
            }

            getArrowStyle() {
                const style = super.getArrowStyle();
                const { properties } = this;
                if (properties.type === "CONDITION") {
                    style.stroke = '#4096ff';
                } else if (properties.type === "OTHERWISE") {
                    style.stroke = '#db973d';
                } else if (properties.type === "EXCEPTION") {
                    style.stroke = '#ff4d4f';
                } else if (properties.type === "startend") {
                    if ($('body').attr('builder-theme') === 'dark') {
                        style.stroke = '#9A9CAE';
                        style.strokeWidth = 2;
                    } else {
                        style.stroke = 'rgb(177, 177, 177)';
                        style.strokeWidth = 2;
                    }
                } else {
                    if ($('body').attr('builder-theme') === 'dark') {
                        style.stroke = '#ffffff';
                        style.strokeWidth = 2;
                    } else {
                        style.stroke = '#000000';
                    }
                }
                style.zIndex = 5;

                return style;
            }

            // Set transition text style
            getTextStyle() {
                const style = super.getTextStyle();
                const edgeStyle = super.getEdgeStyle();
                const { opacity = 1 } = edgeStyle;
                
                style.opacity = opacity;
                if ($('body').attr('builder-theme') === 'dark') {
                    style.color = '#ffffff';
                } else {
                    style.color = '#000000';
                }
                
                style.fontSize = 12;
                style.background = Object.assign({}, style.background, {
                    fill: 'transparent'
                });
                style.zIndex = 5;
                return style;
            }

            getTextPosition() {
                const position = super.getTextPosition();
                position.y = position.y - 15;
                return position;
            }

            // Set hover outline style
            getOutlineStyle() {
                const { danger } = this.properties;
                const style = super.getOutlineStyle();
                style.stroke = '#4285f4';
                style.fill = 'rgba(66, 133, 244, 0.1)';
                style.strokeWidth = 2;
                style.strokeDasharray = '0';
                if (danger) {
                    style.stroke = '#FF4D4F ';
                    style.fill = 'rgba(255, 77, 79, 0.1)';
                    style.hover.stroke = '#FF4D4F';
                    style.hover.fill = 'rgba(255, 77, 79, 0.1)';
                } else {
                    style.stroke = '#4285f4';
                    style.fill = 'rgba(66, 133, 244, 0.1)';
                    style.hover.stroke = '#54c5fc';
                    style.hover.fill = 'rgba(255, 255, 255, 0.1)';
                }
                style.zIndex = 5;
                return style;
            }
        }

        class straightTransitionModel extends LineEdgeModel {
            customTextPosition = true;

            setAttributes(data) {
                super.setAttributes(data);
                const { properties } = this;
                if(properties.type === "startend"){
                    this.isHitable = false;
                }
                this.text.editable = false; 
            }
            
            // Set transition style
            getEdgeStyle() {
                const style = super.getEdgeStyle();
                const { properties } = this;
                if (properties.type === "CONDITION") {
                    style.stroke = '#4096ff';
                    style.strokeWidth = 2;
                } else if (properties.type === "OTHERWISE") {
                    style.stroke = '#db973d';
                    style.strokeWidth = 2;
                } else if (properties.type === "EXCEPTION") {
                    style.stroke = '#ff4d4f';
                    style.strokeWidth = 2;
                } else if (properties.type === "startend") {
                    if ($('body').attr('builder-theme') === 'dark') {
                        style.stroke = '#9A9CAE';
                        style.strokeWidth = 2;
                    } else {
                        style.stroke = 'rgb(177, 177, 177)';
                        style.strokeWidth = 2;
                    }
                } else {
                    if ($('body').attr('builder-theme') === 'dark') {
                        style.stroke = '#ffffff';
                    } else {
                        style.stroke = '#000000';
                    }
                    
                }
                style.zIndex = 5;
                return style;
            }

            getArrowStyle() {
                const style = super.getArrowStyle();
                const { properties } = this;
                if (properties.type === "CONDITION") {
                    style.stroke = '#4096ff';
                } else if (properties.type === "OTHERWISE") {
                    style.stroke = '#db973d';
                } else if (properties.type === "EXCEPTION") {
                    style.stroke = '#ff4d4f';
                } else if (properties.type === "startend") {
                    if ($('body').attr('builder-theme') === 'dark') {
                        style.stroke = '#9A9CAE';
                        style.strokeWidth = 2;
                    } else {
                        style.stroke = 'rgb(177, 177, 177)';
                        style.strokeWidth = 2;
                    }
                } else {
                    if ($('body').attr('builder-theme') === 'dark') {
                        style.stroke = '#ffffff';
                        style.strokeWidth = 2;
                    } else {
                        style.stroke = '#000000';
                    }
                }
                style.zIndex = 5;

                return style;
            }

            // Set transition text style
            getTextStyle() {
                const style = super.getTextStyle();
                const edgeStyle = super.getEdgeStyle();
                const { opacity = 1 } = edgeStyle;
                
                style.opacity = opacity;
                if ($('body').attr('builder-theme') === 'dark') {
                    style.color = '#ffffff';
                } else {
                    style.color = '#000000';
                }
                
                style.fontSize = 12;
                style.background = Object.assign({}, style.background, {
                    fill: 'transparent'
                });
                style.zIndex = 5;
                return style;
            }

            getTextPosition() {
                const position = super.getTextPosition();
                position.y = position.y - 15;
                return position;
            }

            // Set hover outline style
            getOutlineStyle() {
                const { danger } = this.properties;
                const style = super.getOutlineStyle();
                style.stroke = '#4285f4';
                style.fill = 'rgba(66, 133, 244, 0.1)';
                style.strokeWidth = 2;
                style.strokeDasharray = '0';
                if (danger) {
                    style.stroke = '#FF4D4F ';
                    style.fill = 'rgba(255, 77, 79, 0.1)';
                    style.hover.stroke = '#FF4D4F';
                    style.hover.fill = 'rgba(255, 77, 79, 0.1)';
                } else {
                    style.stroke = '#4285f4';
                    style.fill = 'rgba(66, 133, 244, 0.1)';
                    style.hover.stroke = '#54c5fc';
                    style.hover.fill = 'rgba(255, 255, 255, 0.1)';
                }
                style.zIndex = 5;
                return style;
            }
        }

        ProcessBuilder.lf.register({
            ...activityNode,
            view: activityNodeView,
            model: activityNodeModel
        });
        ProcessBuilder.lf.register({
            ...toolNode,
            view: toolNodeView,
            model: toolNodeModel
        });
        ProcessBuilder.lf.register({
            ...startNode,
            view: startNodeView,
            model: startNodeModel
        });
        ProcessBuilder.lf.register({
            ...routeNode,
            view: routeNodeView,
            model: routeNodeModel
        });
        ProcessBuilder.lf.register({
            ...endNode,
            view: endNodeView,
            model: endNodeModel
        });
        ProcessBuilder.lf.register({
            type: 'bpmn:subflow',
            view: subflowView,
            model: subflowNodeModel
        });
        ProcessBuilder.lf.register({
            ...transition,
            view: transitionView,
            model: transitionModel
        });
        ProcessBuilder.lf.register({
            type: 'bpmn:straightSequenceFlow',
            view: straightTransitionView,
            model: straightTransitionModel
        });

        ProcessBuilder.lf.extension.highlight.setMode('neighbour');
        ProcessBuilder.lf.extension.highlight.setEnable(false);

        // Define the common menu configuration
        const commonMenuConfig = {
            icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="17" height="17" fill="currentColor" style="color: var(--theme-danger-color);margin-left: 3px;margin-top: 3px;"><path pointer-events="none" d="M15.3,1.4 L12.6,1.4 L12.6,0 L5.4,0 L5.4,1.4 L0,1.4 L0,2.8 L2,2.8 L2,17.3 C2,17.6865993 2.31340068,18 2.7,18 L15.3,18 C15.6865993,18 16,17.6865993 16,17.3 L16,2.8 L18,2.8 L18,1.4 L15.3,1.4 Z M14.6,16.6 L3.4,16.6 L3.4,2.8 L14.6,2.8 L14.6,16.6 Z"></path><path pointer-events="none" d="M6,5.4 L7.4,5.4 L7.4,14.4 L6,14.4 L6,5.4 Z M10.6,5.4 L12,5.4 L12,14.4 L10.6,14.4 L10.6,5.4 Z"></path></svg>',
            // Callback function to delete the element and hide the context menu
            callback: function (data, type) {
                if (type == 'click') {
                    if (data.properties.className !== 'transition') {
                        ProcessBuilder.removeNode(data);
                    } else {
                        ProcessBuilder.removeConnection(data);
                    }
                    ProcessBuilder.lf.hideContextMenu();        // Hides the context menu
                } else if (type == 'hover') {
                    ProcessBuilder.lf.setProperties(data.id, { danger: true });
                } else if (type == 'unhover') {
                    ProcessBuilder.lf.setProperties(data.id, { danger: false });
                }
            }
        };

        ProcessBuilder.lf.extension.menu.setMenuConfig({
            nodeMenu: [],
            edgeMenu: false,
            graphMenu: []
        });

        ProcessBuilder.lf.setContextMenuByType('bpmn:userTask', [commonMenuConfig]);
        ProcessBuilder.lf.setContextMenuByType('bpmn:serviceTask', [commonMenuConfig]);
        ProcessBuilder.lf.setContextMenuByType('bpmn:exclusiveGateway', [commonMenuConfig]);
        ProcessBuilder.lf.setContextMenuByType('bpmn:startEvent', [commonMenuConfig]);
        ProcessBuilder.lf.setContextMenuByType('bpmn:subflow', [commonMenuConfig]);
        ProcessBuilder.lf.setContextMenuByType('bpmn:endEvent', [commonMenuConfig]);
        ProcessBuilder.lf.setContextMenuByType('bpmn:sequenceFlow', [commonMenuConfig]);
        ProcessBuilder.lf.setContextMenuByType('bpmn:straightSequenceFlow', [commonMenuConfig]);

        // Variable to track the previous selected node
        ProcessBuilder.previousSelectedNodeId = null;

        // Handle node drop
        ProcessBuilder.lf.on('node:drop', ({ data }) => {
            const { id: nodeId } = data;
            
            const node = ProcessBuilder.lf.getNodeDataById(nodeId);
            const nodeLane = ProcessBuilder.getActivityLane(nodeId);
            
            ProcessBuilder.moveNodeToCorrectLane(nodeId, node)

            const currentLaneId = ProcessBuilder.getNodeLaneID(nodeId);
            if (currentLaneId !== `laneID_${nodeLane.properties.id}`) {
                const targetLane = ProcessBuilder.getLane(currentLaneId);
                const sourceLane = ProcessBuilder.getLane(nodeLane.properties.id);
                const movingNode = ProcessBuilder.getActivity(nodeId);
                
                // Remove the node from the source lane
                sourceLane.activities = sourceLane.activities.filter(activity => activity.properties.id !== movingNode.properties.id);
        
                // Add the selected node to target lane
                if (!targetLane.activities) {
                    targetLane.activities = [];
                }
                targetLane.activities.push(movingNode);
            }
            
            const nodeModel = ProcessBuilder.lf.getNodeModelById(data.properties.id);
            if (nodeModel) {
                let node = ProcessBuilder.getActivity(data.properties.id);
                if (node) {
                    node.x_offset = nodeModel.x;
                    node.y_offset = nodeModel.y;
                }
            }
            
            setTimeout(() => {
                ProcessBuilder.adjustLane(true, true);
                
                //select the node back after move
                ProcessBuilder.selectElementById(nodeId);
            }, 0);
        });

        ProcessBuilder.lf.on('set-danger', ({ id }) => {
            ProcessBuilder.lf.setProperties(id, { danger: true });
        });

        ProcessBuilder.lf.on('remove-danger', ({ id }) => {
            ProcessBuilder.lf.setProperties(id, { danger: false });
        });
        
        // Handle adding a new edge
        ProcessBuilder.lf.on('edge:add', ({ data }) => {
            if (!ProcessBuilder.changeNodeId) {
                if (data.targetNodeId.startsWith("poolID_") || data.targetNodeId.startsWith("laneID_")) {
                    let sourceNode = ProcessBuilder.lf.getNodeDataById(data.sourceNodeId);
                    ProcessBuilder.lf.deleteEdge(data.id);
                    ProcessBuilder.showContextMenu(sourceNode);
                    return false;
                }
                ProcessBuilder.addConnection(data);
                
                if (!ProcessBuilder.disableUpdate) {
                    ProcessBuilder.adjustLane(true, true);
                }
            }
        });
        
        // Handle adding a new node from the drag-and-drop menu
        ProcessBuilder.lf.on('node:dnd-add', ({ data }) => {
            ProcessBuilder.renderLFNode(data);
            setTimeout(() => {
                ProcessBuilder.adjustLane(true, true);
            }, 0);
        });
        
        // Handle adding a new node
        ProcessBuilder.lf.on('node:add', ({ data }) => {
            if (!ProcessBuilder.changeNodeId) {
                if (data.type !== 'lane') {
                    ProcessBuilder.renderLFNode(data);
                    
                    if (!ProcessBuilder.disableUpdate) {
                        CustomBuilder.update();
                    }
                } else {
                    let laneID = ProcessBuilder.renderLFNode(data);
                    setTimeout(() => {
                        ProcessBuilder.resizePool();
                        ProcessBuilder.adjustLane(true, true);
                    }, 0);
                }
            }
        });

        // Handle for lane delete
        ProcessBuilder.lf.on('node:delete', ({ data }) => {
            if (!ProcessBuilder.changeNodeId) {
                if(data.type === 'lane'){
                    let previousParticipant;
                    let index = ProcessBuilder.getLaneIndex(data.id);
                    if (index > 0) {
                        let participants = ProcessBuilder.currentProcessData.participants;
                        previousParticipant = participants[index - 1].properties.id;
                    } else {
                        previousParticipant = data.id;
                    }
                    ProcessBuilder.deleteLane(data);
                    ProcessBuilder.removeNode(data);
                    setTimeout(() => {
                        ProcessBuilder.adjustLane(true, true);
                    }, 0);
                }
            }
        });
        
        // Handle for node and edge click
        ProcessBuilder.lf.on('node:click,edge:click', (node, e) => {
            if (node && node.data && node.data.id) {
                ProcessBuilder.selectElementById(node.data.id);
            }
            return false;
        });
    },
    
    /**
     * When move the node outside the land area, it does not assign to correct lane.
     * THis method is used to correct it.
     */
    moveNodeToCorrectLane: function(nodeId, node) {
        //find a closest lane, then move the node to that lane if it is not in that lane
        const targetLaneId = ProcessBuilder.getNodeLaneID(nodeId);
        let newLane = null;
        let graphData = ProcessBuilder.lf.getGraphData();
        let lanes = graphData.nodes.filter(lane => lane.type === "lane");
        lanes = lanes.sort((a, b) => b.y - a.y);

        lanes.forEach(lane => { 
            if (!newLane || node.y < lane.y + (lane.properties.height / 2)) {
                newLane = lane;
            }
        });

        if (targetLaneId !== newLane.id) {
            if (!newLane.children?.includes(nodeId)) {
                newLane.children.push(nodeId);
                ProcessBuilder.lf.updateAttributes(newLane.id, {
                    children: new Set(newLane.children)
                });
            }

            //remove from wrong target lane
            if (targetLaneId) {
                const targeLFLane = ProcessBuilder.getLFLane(targetLaneId);
                const updatedChildren = targeLFLane.children.filter(node => node !== nodeId);
                ProcessBuilder.lf.updateAttributes(targeLFLane.id, {
                    children: new Set(updatedChildren)
                });
            }
        }
    },
    
    /**
     * Gets the current mouse position relative to the LogicFlow canvas.
     */
    getMousePosition: function () {
        return ProcessBuilder.lf.getPointByClient(window.event.clientX, window.event.clientY);
    },
    
    /**
     * Adds a new node to the process builder via the menu
     */
    addNodeFromMenu: function (node, type, x, y) {
        let graphData = ProcessBuilder.lf.getGraphData();
        let lanes = graphData.nodes.filter(lane => lane.type === "lane");
        let targetLFLane;

        lanes = lanes.sort((a, b) => b.y - a.y);

        lanes.forEach(lane => { 
            if (!targetLFLane || y < lane.y + (lane.properties.height / 2)) {
                targetLFLane = lane;
            }
        });
        
        ProcessBuilder.disableUpdate = true; //to disable update
        let newNode = ProcessBuilder.lf.addNode({
            type: type, // Change based on your node type
            x: x,  // Adjust position
            y: y,
            text: 'Node',
            properties: {}
        });
        
        ProcessBuilder.lf.addEdge({
            type: "bpmn:sequenceFlow",
            sourceNodeId: node.id,
            targetNodeId: newNode.id
        });
        ProcessBuilder.disableUpdate = false; 
        
        targetLFLane.children.push(newNode.id);
        ProcessBuilder.lf.updateAttributes(targetLFLane.id, {
            children: new Set(targetLFLane.children)
        });

        ProcessBuilder.adjustLane(true, true);

        ProcessBuilder.lf.extension.menu.setMenuConfig({
            nodeMenu: [],
            edgeMenu: false,
            graphMenu: []
        });
    },
    
    /**
     * Displays the context menu
     */
    showContextMenu: function (node) {
        if (!node) return;
        const lfContainer = document.getElementById('lf-container'); // Replace with your actual container ID

        if (lfContainer) {
            let mousePosition = ProcessBuilder.getMousePosition();
            const rect = lfContainer.getBoundingClientRect();
            const x = mousePosition.canvasOverlayPosition.x;
            const y = mousePosition.canvasOverlayPosition.y;

            // Define the common menu configuration for activity node
            const activityConfig = {
                text: get_cbuilder_msg('pbuilder.label.activity'),
                callback(node) {
                    ProcessBuilder.addNodeFromMenu(node, 'bpmn:userTask', x, y);
                }
            };

            // Define the common menu configuration for tool node
            const toolConfig = {
                text: get_cbuilder_msg('pbuilder.label.tool'),
                callback(node) {
                    ProcessBuilder.addNodeFromMenu(node, 'bpmn:serviceTask', x, y);
                }
            };

            // Define the common menu configuration for subflow node
            const subflowConfig = {
                text: get_cbuilder_msg('pbuilder.label.subflow'),
                callback(node) {
                    ProcessBuilder.addNodeFromMenu(node, 'bpmn:subflow', x, y);
                }
            };

            // Define the common menu configuration for route node
            const routeConfig = {
                text: get_cbuilder_msg('pbuilder.label.route'),
                callback(node) {
                    ProcessBuilder.addNodeFromMenu(node, 'bpmn:exclusiveGateway', x, y);
                }
            };

            // Define the common menu configuration for end node
            const endConfig = {
                text: get_cbuilder_msg('pbuilder.label.end'),
                callback(node) {
                    ProcessBuilder.addNodeFromMenu(node, 'bpmn:endEvent', x, y);
                }
            };

            // Add the menu before showing the menu
            ProcessBuilder.lf.extension.menu.setMenuConfig({
                nodeMenu: [activityConfig, toolConfig, subflowConfig, routeConfig, endConfig],
                edgeMenu: false,
                graphMenu: []
            });
            
            const event = new MouseEvent('contextmenu', {
                bubbles: true,
                cancelable: true,
                clientX: mousePosition.domOverlayPosition.x + rect.left, // Adjust based on the element position
                clientY: mousePosition.domOverlayPosition.y + rect.top
            });

            const nodeElement = document.querySelector(`[data-cbuilder-id="${node.id}"]`);
            if (nodeElement) {
                nodeElement.dispatchEvent(event);
            }
        }
    },

    /*
     * Check if copied element is pastable
     */
    isPastable: function (elementObj, component) {
        var copied = CustomBuilder.getCopiedElement();
        if (copied !== null && copied !== undefined) {
            var copiedComponent = CustomBuilder.Builder.parseDataToComponent(copied.object.properties);
            if (copiedComponent !== null && copiedComponent !== undefined && component.builderTemplate.getChildsContainerAttr(elementObj, component) === copiedComponent.builderTemplate.getParentContainerAttr(copied.object, copiedComponent)) {
                return true;
            } else if (copiedComponent !== null && copiedComponent !== undefined && component.builderTemplate.getParentContainerAttr(elementObj, component) === copiedComponent.builderTemplate.getParentContainerAttr(copied.object, copiedComponent)) {
                return true; //sibling
            }
        }
        return false;
    },
    
    /**
     * Renders the LogicFlow graph with the provided data.
     */
    renderLFWithData: function (graphData, addToUndo = false) {
        graphData = ProcessBuilder.updatePoolHeight(graphData);
        ProcessBuilder.lf.render(graphData);
        ProcessBuilder.resizePool();
        ProcessBuilder.currentLFProcessData = ProcessBuilder.lf.getGraphData();
        ProcessBuilder.adjustLane(true, addToUndo);
    },
    
    /**
     * Adjusts the size and position of lanes and the pool dynamically based on child nodes.
     * Ensures that lanes expand or contract to fit their contained nodes while maintaining alignment.
     *
     * @param {boolean} adjustNode - Whether to adjust child node positions.
     * @param {boolean} addToUndo - Whether to add the change to undo, default is false
     */
    adjustLane: function (adjustNode, addToUndo = false) {
        let graphData = ProcessBuilder.lf.getGraphData();
        
        //before adjust the lane, make sure there is enough gap between the source & target node, adjust it if gap is very small
        graphData = ProcessBuilder.adjustNodeGap(graphData);
        
        let lanes = graphData.nodes.filter(lane => lane.type === "lane");
        let pool = graphData.nodes.filter(pool => pool.type === "pool");
        let poolHeight = 0;
        let finalLaneWidth = 0;
        let initialY;
        let initialX;
        let laneDefaultHeight = 216;
        let extraSpaceForLane = 64;
        let prevLaneYEnd;
        let laneXStart;
        lanes = lanes.sort((a, b) => a.y - b.y);
        // Adjust the lane height and width
        lanes.forEach(lane => { 
            let totalLaneHeightAdjusted = (prevLaneYEnd?(prevLaneYEnd - (lane.y - (lane.properties.height / 2))):0); //find the different for new y start position based on prev lane Y end
            let laneOriYStart = lane.y - (lane.properties.height / 2) + totalLaneHeightAdjusted;
            let laneYStart;
            let laneYEnd; 
            let laneXEnd;
            let totalLaneStartAdjusted = 0;
            
            if (lane.children.length > 0) {
                // check node is within the lane
                lane.children.forEach(child => {
                    let node = graphData.nodes.filter(node => node.id === child)[0];
                    let nodeYStart = node.y - (node.properties.height / 2) - extraSpaceForLane + totalLaneHeightAdjusted;
                    let nodeYEnd = node.y + (node.properties.height / 2) + extraSpaceForLane + totalLaneHeightAdjusted;
                    let nodeXStart = node.x - (node.properties.width / 2) - extraSpaceForLane;
                    let nodeXEnd = node.x + (node.properties.width / 2) + extraSpaceForLane;
                    if (laneYStart === undefined || laneYEnd  === undefined || nodeYStart < laneYStart || nodeYEnd > laneYEnd) {
                        if (laneYStart  === undefined || nodeYStart < laneYStart) {
                            laneYStart = nodeYStart;
                        }
                        if (laneYEnd  === undefined || nodeYEnd > laneYEnd) {
                            laneYEnd = nodeYEnd;
                        }
                    }

                    if (laneXStart  === undefined || laneXEnd  === undefined || nodeXStart < laneXStart || nodeXEnd > laneXEnd) {
                        if (laneXStart === undefined || nodeXStart < laneXStart) {
                            laneXStart = nodeXStart;
                        } 
                        if (laneXEnd  === undefined || nodeXEnd > laneXEnd) {
                            laneXEnd = nodeXEnd;
                        }
                    }
                });
                
                //check if lane start is changed, if yes, add the different to the adjust height
                if (laneYStart !== laneOriYStart) {
                    totalLaneStartAdjusted = (laneOriYStart - laneYStart);
                }
                
                //adjust the position of all nodes
                if (totalLaneStartAdjusted + totalLaneHeightAdjusted !== 0 && adjustNode) {
                    lane.children.forEach(child => {
                        let node = graphData.nodes.filter(node => node.id === child)[0];
                        node.y = node.y + totalLaneStartAdjusted + totalLaneHeightAdjusted;
                        if (node.text) {
                            node.text.y = node.text.y + totalLaneStartAdjusted + totalLaneHeightAdjusted;
                        }
                    });
                }

                let laneHeight = laneYEnd - laneYStart;
                //make sure the lane have min height
                if (laneHeight < laneDefaultHeight) {
                    laneYEnd = laneYEnd + laneDefaultHeight - laneHeight;
                    laneHeight = laneDefaultHeight;
                }
                
                //update lane y
                lane.y = laneOriYStart + (laneHeight/2);
                
                //update lane new height
                if (laneHeight > 0) {
                    lane.properties.height = laneHeight;
                    lane.properties.nodeSize.height = laneHeight;
                } 
                
                //update lane new width
                let laneWidth = laneXEnd - laneXStart + 150;
                if (laneWidth > 0) {
                    lane.properties.width = laneWidth;
                    lane.properties.nodeSize.width = laneWidth;
                }
            }
            
            prevLaneYEnd = laneOriYStart + lane.properties.height;
            
            //find the larger lane width
            if (finalLaneWidth < lane.properties.width) {
                finalLaneWidth = lane.properties.width;
            } 
            
            //find the smaller lane x position
            if (initialX === undefined || initialX > laneXStart) {
                initialX = laneXStart;
            }

            //find the smaller lane y position
            if (initialY === undefined || initialY > laneOriYStart) {
                initialY = laneOriYStart;
            }
            
            //calculate the pool height
            poolHeight += lane.properties.height;
        });
        
        if (finalLaneWidth < 1450) { // set min width
            finalLaneWidth = 1450;
        }
        
        // Adjust pool and lanes width
        if (finalLaneWidth > 0) {
            pool[0].properties.width = finalLaneWidth;
            pool[0].properties.nodeSize.width = finalLaneWidth;
            lanes.forEach(lane => {
                lane.properties.width = finalLaneWidth;
                lane.properties.nodeSize.width = finalLaneWidth;
                
                lane.x = initialX + (finalLaneWidth/2);
            });
            pool[0].x = initialX + (finalLaneWidth/2);
        }
        
        // Adjust the pool height and position
        if (poolHeight > 0) {
            pool[0].properties.height = poolHeight;
            pool[0].properties.nodeSize.height = poolHeight;
            pool[0].y = initialY + (poolHeight/2);
        }
        
        graphData = ProcessBuilder.updateEdges(graphData);
        ProcessBuilder.updateNodePosition(graphData, addToUndo);
    },
    
    /*
     * Make sure there is enough gap between the source & target node, adjust it if gap is very small
     */
    adjustNodeGap: function(graphData) {
        graphData.edges.forEach(edge => {
            let sameLane = graphData.nodes.find(node => node.type === 'lane' && node.children?.includes(edge.sourceNodeId) && node.children?.includes(edge.targetNodeId)) !== undefined;
            
            //check is in same lane
            if (sameLane) {
                let source = graphData.nodes.filter(node => node.id === edge.sourceNodeId)[0];
                let target = graphData.nodes.filter(node => node.id === edge.targetNodeId)[0];
            
                const dx = Math.abs(target.x - source.x);
                const dy = Math.abs(target.y - source.y);
                const distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 180) {
                    let xAdding = 0;
                    let yAdding = 0;
                    let xBreakpoint = 0;
                    let yBreakpoint = 0;

                    const {x, y} = source,
                    {x: x1, y: y1} = target;

                    if (dx > dy) {
                        xBreakpoint = Math.min(x, x1);
                        xAdding = 180 - dx;
                    } else {
                        yBreakpoint = Math.min(y, y1);
                        yAdding = 180 - dy;
                    }

                    //move the nodes 
                    graphData.nodes.forEach(node => {
                        if (!(node.type === "lane" || node.type === "pool")) {
                            if (xAdding !== 0 && node.x > xBreakpoint) {
                                node.x += xAdding;
                                if (node.text) {
                                    node.text.x += xAdding;
                                }
                            }
                            if (yAdding !== 0 && node.y > yBreakpoint) {
                                node.y += yAdding;
                                if (node.text) {
                                    node.text.y += yAdding;
                                }
                            }
                        }
                    });
                }
            }
        });
        
        return graphData;
    },

    /*
     * Resize Pool based on lane changed
     */
    resizePool: function () {
        const resizeDir = 'below';
        const deltaHeight = 0;
        const data = ProcessBuilder.lf.getGraphData();
        var poolId = ProcessBuilder.getPoolId(data);
        if (poolId !== null) {
            ProcessBuilder.lf.getNodeModelById(poolId).resizeChildren({
                resizeDir,
                deltaHeight
            });
        }
    },
    
    /*
     * Get Pool node ID
     */
    getPoolId: function (data) {
        // Find the node with type 'pool'
        const poolElement = data.nodes.find(node => node.type === 'pool');
        
        // Return the id if the pool element exists, otherwise return null
        return poolElement ? poolElement.id : null;
    },
    
    /*
     * Render new node and update it properties
     */
    renderLFNode: function (data) {
        let nodeID = null;
        
        if (ProcessBuilder.getNodeLaneID(data.id) === null && data.type !== 'lane') {
            //if node place outside lane, correct it
            ProcessBuilder.moveNodeToCorrectLane(data.id, data);
        }
        
        var toolClass = null;
        if (ProcessBuilder.draggingElementId !== null && ProcessBuilder.draggingElementId !== undefined && ProcessBuilder.draggingElementId.startsWith("Tool_")) {
            toolClass = ProcessBuilder.draggingElementId.replace("Tool_", "");
        }
        ProcessBuilder.addElement(data, ProcessBuilder.updatePasteElement);

        var result;
        if (data.type === 'lane') {
            result = ProcessBuilder.getLane(ProcessBuilder.draggingElementId);
        } else {
            result = ProcessBuilder.getActivity(ProcessBuilder.draggingElementId);
        }

        if (result) {
            let label = result.properties.label;
            // check if preset tool is added
            if (toolClass !== null) {
                result.properties.tools = result.properties.tools || [];
                const newTool = [
                    {
                        "className": toolClass
                    }
                ];
                result.properties.tools.push(...newTool);
                label = ProcessBuilder.availableTools[toolClass].label;
                result.properties.label = label;
            }

            ProcessBuilder.lf.setProperties(data.id, result.properties);
            ProcessBuilder.lf.setProperties(data.id, { xpdlObj: result.xpdlObj });
            ProcessBuilder.lf.updateText(data.id, label);
            if (data.type === 'lane') {
                ProcessBuilder.lf.updateAttributes(data.id, { id: "laneID_" + result.properties.id });
            } else {
                ProcessBuilder.lf.updateAttributes(data.id, { id: result.properties.id });

                //update the id in lane
                const targetLaneId = ProcessBuilder.getNodeLaneID(data.id);
                const targeLFLane = ProcessBuilder.getLFLane(targetLaneId);
                const updatedChildren = targeLFLane.children.filter(node => node !== data.id);
                updatedChildren.push(result.properties.id);
                ProcessBuilder.lf.updateAttributes(targetLaneId, {
                    children: new Set(updatedChildren)
                });
            }
            nodeID = result.properties.id;
        }
        return nodeID;
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
            };
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
            var hasChange = false;
            
            //check for changes
            if ($("body").hasClass("property-editor-right-panel") && !$("body").hasClass("no-right-panel")) {
                $(".element-properties .property-editor-container").each(function() {
                    var editor = $(this).parent().data("editor");
                    if (editor !== undefined && !editor.saved && editor.isChange()) {
                        hasChange = true;
                    }
                });
            }
            
            if (hasChange && !confirm(get_cbuilder_msg('ubuilder.saveBeforeClose'))) {
                //revert the selected value
                $(selector).val(ProcessBuilder.currentProcessData.properties.id);
                $(selector).trigger("chosen:updated");
            } else {
                window.location.hash = $(selector).val();
            }
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
        
        //select back the element when undo/redo
        var selectedEl = self.selectedEl;
        
        //if id not empty or id is empty but there is remaining process in package. 
        //The generateProcessData method will redirect to the first remaining process when id is empty
        if (id !== "" || $("#processes_list option").length > 0) {
            ProcessBuilder.generateProcessData(id);
            if (ProcessBuilder.currentProcessData !== undefined && ProcessBuilder.currentProcessData !== null && ProcessBuilder.currentProcessData.properties !== undefined) {
                ProcessBuilder.updateAdvancedView();
                CustomBuilder.Builder.load(ProcessBuilder.currentProcessData, function(){
                    ProcessBuilder.validate();
                    
                    setTimeout(function(){
                        if (ProcessBuilder.preSelect !== "") {
                            ProcessBuilder.selectElementById(ProcessBuilder.preSelect);
                            ProcessBuilder.preSelect = "";
                        } else if (selectedEl) {
                            ProcessBuilder.selectElementById(selectedEl[0].data.id);
                        }
                        if (ProcessBuilder.view !== "") {
                            $("[data-cbuilder-view='"+ProcessBuilder.view+"']").trigger("click");
                        }
                    }, 500);
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
        var graphData = {};
        var lfLane = [];
        var lfNode = [];
        var lfEdges = [];

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

        var pool = {
            id : 'poolID_' + id,
            type: "pool",
            x: 840,
            y: 280,
            properties: {
                nodeSize: {
                    width: 1479,
                    height: 432
                },
                width: 1479,
                height: 432
            }
        };
        
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
        
        let LFparticipant = [];
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
                    var label = participants[orders[o]].properties.label;
                    const LFLaneobject = {
                        id: "laneID_" + orders[o],
                        type: "lane",
                        x: 855,
                        y: -44,
                        properties: {
                            className: 'participant',
                            id: orders[o],
                            label: label,
                            activities: [],
                            nodeSize: {
                                "width": 1449,
                                "height": 216
                            },
                            processRef: "",
                            panels: [
                                "processRef"
                            ],
                            width: 1449,
                            height: 216,
                            xpdlObj : participants[orders[o]].xpdlObj
                        },
                        children: [],
                        text: {
                            value: label
                        }
                    };
                    lfNode.push(LFLaneobject);
                    LFparticipant.push("laneID_" + participants[orders[o]].properties.id);

                    ProcessBuilder.currentProcessData['participants'].push(participants[orders[o]]);
                        
                    if (!ProcessBuilder.readonly) {
                        //find mapping
                        ProcessBuilder.populateParticipantMapping(participants[orders[o]]);
                        const laneID = "laneID_" + participants[orders[o]].properties.id; // replace with the desired id
                        const foundObject = lfNode.find(item => item.id === laneID);
                        ProcessBuilder.populateParticipantMapping(foundObject);
                    }
                }
                break;
            }
        }
        pool.children = LFparticipant;
        lfNode.push(pool);
        
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

            var logicFlowType = ProcessBuilder.getLFNodeType(type);
            var logicFlowObj = {
                id: act['-Id'],
                type: logicFlowType,
                properties: {
                    id: act['-Id'],
                    className: type,
                    xpdlObj: xpdlActivities[a]
                },
                x: Number(x),
                y: Number(y)
            };

            logicFlowObj.text = {
                x: Number(x),
                y: Number(y),
                value: act['-Name']
            };
            
            if (act['-Name'] !== undefined) {
                obj.properties.label = act['-Name'];
                logicFlowObj.properties.label = act['-Name'];
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
            logicFlowObj.properties.join = join;
            logicFlowObj.properties.split = split;
            
            //set limit
            if (act['Limit'] !== undefined) {
                obj.properties.limit = act['Limit'];
                logicFlowObj.properties.limit = act['Limit'];
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
                logicFlowObj.properties.deadlines = deadlines;
            }
            
            //set subflow properties
            if (type === "subflow") {
                var subflow = act['Implementation']['SubFlow'];
                obj.properties.subflowId = subflow['-Id'];
                obj.properties.execution = subflow['-Execution'];
                logicFlowObj.properties.subflowId = subflow['-Id'];
                logicFlowObj.properties.execution = subflow['-Execution'];
                
                if (subflow['ActualParameters'] !== undefined) {
                    var actualParameters = new Array();
                    var params = ProcessBuilder.getArray(subflow['ActualParameters'], 'ActualParameter');
                    for (var p in params) {
                        actualParameters.push({
                            actualParameter : params[p]
                        });
                    }
                    obj.properties.actualParameters = actualParameters;
                    logicFlowObj.properties.actualParameters = actualParameters;
                }
            }
            
            if (!ProcessBuilder.readonly) {
                //find mapping
                ProcessBuilder.populateActivityMapping(obj);
                ProcessBuilder.populateActivityMapping(logicFlowObj);
            }
            
            var currentLFParticipants;
            //if participant not exist add to the first participant
            if (participants[participantId] !== undefined) {
                participants[participantId]['activities'].push(obj);
                currentLFParticipants = participants[participantId].properties.id;
                const lane = lfNode.find(lane => lane.id === 'laneID_' + currentLFParticipants);
                lane.properties.activities.push(obj);
            } else {
                participants[Object.keys(participants)[0]]['activities'].push(obj);
                currentLFParticipants = participants[Object.keys(participants)[0]].properties.id;
                const lane = lfNode.find(lane => lane.id === 'laneID_' + currentLFParticipants);
                lane.properties.activities.push(obj);
            }

            // Find the lane with id
            const laneRequester = $.grep(lfNode, function (element) {
                return element.type === 'lane' && element.id === 'laneID_'+currentLFParticipants;
            });

            // Check if the lane exists
            if (laneRequester.length > 0) {
                const requesterLane = laneRequester[0];
                // Check if the 'children' property exists, if not, initialize it as an array
                if (!requesterLane.children) {
                    requesterLane.children = [];
                }
                // Add the new value to the children array
                requesterLane.children.push(act['-Id']);
            } 
            lfNode.push(logicFlowObj);
        }
        //add start and end node
        for (var p in xpdlProcessesAttrs) {
            if (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_END_OF_WORKFLOW" || xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_START_OF_WORKFLOW") {
                let isValid = true;
                var participantId;
                var values = xpdlProcessesAttrs[p]['-Value'].split(",");
                var obj = {
                    className : (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_END_OF_WORKFLOW")?"end":"start",
                    properties : {},
                    xpdlObj : xpdlProcessesAttrs[p]
                };
                
                var logicFlowObj = {
                    className : (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_END_OF_WORKFLOW")?"end":"start",
                    type: (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_END_OF_WORKFLOW")?"bpmn:endEvent":"bpmn:startEvent",
                    properties : {
                        className : (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_END_OF_WORKFLOW")?"end":"start",
                        xpdlObj : xpdlProcessesAttrs[p]
                    }
                };

                obj.properties.id = obj.className;
                logicFlowObj.id = obj.className;
                logicFlowObj.properties.id = obj.className;
                        
                for (var v in values) {
                    var attr = values[v].split("=");
                    if (attr[0] === "JaWE_GRAPH_PARTICIPANT_ID") {
                        participants[attr[1]]['activities'].push(obj);
                        participantId = attr[1];
                        const lane = lfNode.find(lane => lane.id === 'laneID_' + participantId);
                        lane.properties.activities.push(obj);
                    } else if (attr[0] === "CONNECTING_ACTIVITY_ID") {
                        if (attr[1] !== "") {
                            obj.properties.id = obj.className + "_" + attr[1];
                            logicFlowObj.id = logicFlowObj.className + "_" + attr[1];
                            logicFlowObj.properties.id = logicFlowObj.className + "_" + attr[1];
                            
                            var transition = {
                                className :'transition',
                                properties : {
                                    id : "transition_" + obj.properties.id,
                                    type : 'startend'
                                }
                            };

                            var lfTransition = {
                                id: "transition_" + obj.properties.id,
                                type: 'bpmn:straightSequenceFlow',
                                properties : {
                                    className :'transition',
                                    id : "transition_" + obj.properties.id,
                                    type : 'startend'
                                }                                
                            };

                            if (xpdlProcessesAttrs[p]['-Name'] === "JaWE_GRAPH_END_OF_WORKFLOW") {
                                transition.properties.from = attr[1];
                                transition.properties.to = obj.properties.id;
                                lfTransition.sourceNodeId = attr[1];
                                lfTransition.targetNodeId = obj.properties.id;
                            } else {
                                transition.properties.from = obj.properties.id;
                                transition.properties.to = attr[1];
                                lfTransition.sourceNodeId = obj.properties.id;
                                lfTransition.targetNodeId = attr[1];
                            }

                            process['transitions'].push(transition);
                            lfEdges.push(lfTransition);
                        } else {
                            isValid = false;
                        }
                    } else if (attr[0] === "X_OFFSET") {
                        obj.x_offset = attr[1];
                        logicFlowObj.x = Number(attr[1]);
                    } else if (attr[0] === "Y_OFFSET") {
                        obj.y_offset = attr[1];
                        logicFlowObj.y = Number(attr[1]);
                    }
                }
                
                if (isValid || xpdlActivities.length === 0) { //valid start/end node connected to other node, or it is a new process without activity
                    const laneRequester = $.grep(lfNode, function (element) {
                        return element.type === 'lane' && element.id === 'laneID_'+participantId;
                    });

                    // Check if the lane exists
                    if (laneRequester.length > 0) {
                        const requesterLane = laneRequester[0];

                        // Check if the 'children' property exists, if not, initialize it as an array
                        if (!requesterLane.children) {
                            requesterLane.children = [];
                        }

                        // Add the new value to the children array
                        requesterLane.children.push(logicFlowObj.id);
                    }         

                    if (!ProcessBuilder.readonly) {
                        //find mapping
                        ProcessBuilder.populateActivityMapping(obj);
                        ProcessBuilder.populateActivityMapping(logicFlowObj);
                        if (obj.className === "start") {
                            ProcessBuilder.populateParticipantMapping(obj);
                            ProcessBuilder.populateParticipantMapping(logicFlowObj);
                        }
                    }
                    lfNode.push(logicFlowObj);
                }
            }
        }
        graphData["nodes"] = lfNode;
        
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

            var lfTransition = {
                id : xpdlTransitions[t]['-Id'],
                properties : {
                    className :'transition',
                    id : xpdlTransitions[t]['-Id'],
                    label : (xpdlTransitions[t]['-Name'] !== undefined)?xpdlTransitions[t]['-Name']:"",
                    from : xpdlTransitions[t]['-From'],
                    to : xpdlTransitions[t]['-To'],
                    xpdlObj : xpdlTransitions[t]
                },
                text : (xpdlTransitions[t]['-Name'] !== undefined)?xpdlTransitions[t]['-Name']:"",
                type :'bpmn:sequenceFlow',
                sourceNodeId : xpdlTransitions[t]['-From'],
                targetNodeId : xpdlTransitions[t]['-To'],
                zIndex: 2
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
            lfTransition.properties.type = type;
            lfTransition.properties.condition = condition;
            lfTransition.properties.exceptionName = exceptionName;
            
            var style = "straight";
            var transitionConditions = "";
            var extendedAttributes = ProcessBuilder.getArray(xpdlTransitions[t]['ExtendedAttributes'], 'ExtendedAttribute');
            for (var i in extendedAttributes) {
                if (extendedAttributes[i]['-Name'] === "JaWE_GRAPH_BREAK_POINTS" && extendedAttributes[i]['-Value'] === "orthogonal") {
                    style = "orthogonal";
                } else if (extendedAttributes[i]['-Name'] === "PBUILDER_TRANSITION_CONDITIONS") {
                    transitionConditions = extendedAttributes[i]['-Value'];
                }
            }
            transition.properties.transitionStyle = style;
            lfTransition.properties.transitionStyle = style;
            lfTransition.type = ProcessBuilder.getEdgeType(style);
            if (transitionConditions !== "") {
                transition.properties.conditions = JSON.decode(transitionConditions);
                transition.properties.conditionHelper = "yes";
                lfTransition.properties.conditions = JSON.decode(transitionConditions);
                lfTransition.properties.conditionHelper = "yes";
            }
            if (condition) {
                if(lfTransition.text){
                    lfTransition.text = lfTransition.text + '\n' + transition.properties.condition + '\n';
                } else {
                    lfTransition.text = transition.properties.condition;
                }
            } else if (lfTransition.properties.type === 'OTHERWISE' || lfTransition.properties.type === 'EXCEPTION'){
                if(lfTransition.text){
                    lfTransition.text = lfTransition.text + '\n' + '[' + transition.properties.type + ']' + '\n';
                } else {
                    lfTransition.text = transition.properties.type;
                }
            }
            
            process['transitions'].push(transition);
            lfEdges.push(lfTransition);
        }
        graphData["edges"] = lfEdges;
        ProcessBuilder.currentLFProcessData = graphData;
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
                xpdlProcess['ProcessHeader']['-DurationUnit'] = data.properties.durationUnit;
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
            
            //remove all start and end before update activities to regenerate
            xpdlProcessesAttrs = xpdlProcessesAttrs.filter(item => (item['-Name'] !== "JaWE_GRAPH_START_OF_WORKFLOW" && item['-Name'] !== "JaWE_GRAPH_END_OF_WORKFLOW"));
            ProcessBuilder.setArray(xpdlProcess, 'ExtendedAttributes', 'ExtendedAttribute', xpdlProcessesAttrs);
            
            //update participants
            var order = "";
            var xpdlParticipants = ProcessBuilder.getArray(xpdl['Participants'], 'Participant');
            
            for (var p in data.participants) {
                var participant = data.participants[p];
                order += (order !== ""?";":"") + participant.properties.id;
                if (participant.xpdlObj !== undefined) {
                    participant.xpdlObj['-Id'] = participant.properties.id;
                    participant.xpdlObj['-Name'] = participant.properties.label;
                    participant.xpdlObj['-Height'] = participant.properties.height;
                } else {
                    participant.xpdlObj = {
                        "-Name": participant.properties.label,
                        "-Id": participant.properties.id,
                        '-Height': participant.properties.height,
                        "ParticipantType": {
                            "-Type": "ROLE",
                            "-self-closing": "true"
                        }
                    };
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

                if (transition.properties.transitionStyle === 'orthogonal') {
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

                if (transition.properties.conditionHelper === "yes" && transition.properties.conditions !== undefined && transition.properties.conditions.length > 0) {
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
            for (var a = 0; a < xpdlProcessesAttrs.length; a++) {
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
        
        //to update the advanced view when changed
        $(CustomBuilder.Builder.iframe).trigger("change.builder");
    },
    
    /*
     * Set the participant mapping to object properties
     */
    populateParticipantMapping : function (participant) {
        var id = ProcessBuilder.currentProcessData.properties.id + "::" + ((participant.properties.className === "start")?"processStartWhiteList":participant.properties.id);
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
            participant.properties.mapping = mapping;
        }
    },
    
    /*
     * Update the participant mapping back to data
     */
    updateParticipantMapping : function (participant) {
        var id = ProcessBuilder.currentProcessData.properties.id + "::" + ((participant.properties.className === "start")?"processStartWhiteList":participant.properties.id);
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
        } else if (participant.properties.className === "start" && participant.properties['mapping_par_type'] === "" && participant.properties['mapping_par_role'] !== "") {
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
        var id;
        if (activity.className){
            id = ProcessBuilder.currentProcessData.properties.id + "::" + ((activity.className === "start")?"runProcess":activity.properties.id);
        } else {
            id = ProcessBuilder.currentProcessData.properties.id + "::" + ((activity.properties.className === "start")?"runProcess":activity.properties.id);
        }
        var mapping = CustomBuilder.data['activityPlugins'][id];
        
        if (activity.properties.className === "activity" || activity.properties.className === "start" || activity.className === "activity" || activity.className === "start") {
            var formMapping = CustomBuilder.data['activityForms'][id];
            
            if (formMapping !== undefined) {
                activity.properties['mapping_act_type'] = formMapping.type;
                activity.properties['mapping_act_formId'] = formMapping.formId;
                activity.properties['mapping_act_formUrl'] = formMapping.formUrl;
                activity.properties['mapping_act_formIFrameStyle'] = formMapping.formIFrameStyle;
                activity.properties['mapping_act_disableSaveAsDraft'] = formMapping.disableSaveAsDraft + "";
                activity.properties['mapping_act_autoContinue'] = formMapping.autoContinue + "";
                        
                activity.properties.formMapping = formMapping;
                activity.formMapping = formMapping;
            }
            if (mapping !== undefined) {
                activity.properties['mapping_act_modifier'] = {
                    className : mapping.className,
                    properties : $.extend(true, {}, mapping.properties)
                };
            }
        } else if (activity.properties.className === "tool" || activity.className === "tool") {
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
        } else if (activity.properties.className === "route" || activity.className === "route") {
            if (mapping !== undefined) {
                activity.properties['mapping_act_plugin'] = {
                    className : mapping.className,
                    properties : $.extend(true, {}, mapping.properties)
                };
            }
        }
        
        if (mapping !== undefined) {
            activity.properties.mapping = mapping;
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
                if (activity.properties['mapping_act_formId'] !== undefined) {
                    formMapping.formId = activity.properties['mapping_act_formId'];
                }
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
            } else if(activity.properties['tools'] !== undefined && activity.properties['tools'].length > 0) {
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
                    var actDeadline = ProcessBuilder.getArray(xpdlObj, 'Deadline');
                    var deadlines = [];
                    for (var d in activity.properties.deadlines) {
                        var deadline = activity.properties.deadlines[d];
                        
                        //use the existing obj to update is available
                        var dObj;
                        if (d < actDeadline.length) {
                            dObj = actDeadline[d];
                        } else {
                            var dObj = {};
                        }
                        
                        dObj['-Execution'] = deadline.execution;
                        
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
                var sourceConnSet =  ProcessBuilder.lf.getNodeOutgoingEdge(activity.properties.id);
                for (var i = sourceConnSet.length - 1; i >= 0; i--) {
                    let node = ProcessBuilder.getActivity(sourceConnSet[i].targetNodeId);
                    if (node.className === 'end') { 
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
                        tids.push(sourceConnSet[c].id);
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
                    if (transitionRestriction['Split'] !== undefined && transitionRestriction['Split']['-self-closing'] === undefined) {
                        delete transitionRestriction['Split'];
                    }
                }
                var targetConnSet = ProcessBuilder.lf.getNodeIncomingEdge(activity.properties.id);
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
                    if (transitionRestriction['Join'] !== undefined && transitionRestriction['Join']['-self-closing'] === undefined) {
                        delete transitionRestriction['Join'];
                    }
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
                }
                xpdlProcessesAttrs.push(xpdlObj);
                
                var actElement = self.frameBody.find("#" + activity.properties.id);
                var actId = "";
                if (activity.className === "start") {
                    var connSet =  ProcessBuilder.lf.getNodeOutgoingEdge(activity.properties.id);
                    if (connSet.length > 0) {
                        actId = connSet[0].targetNodeId;
                    }
                    
                    ProcessBuilder.updateParticipantMapping(activity);
                } else {
                    var connSet = ProcessBuilder.lf.getNodeIncomingEdge(activity.properties.id);
                    if (connSet.length > 0) {
                        actId = connSet[0].sourceNodeId;
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
            }
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
        ProcessBuilder.updateProcessSelector();
        self.triggerChange();
        
        let graphData = ProcessBuilder.lf.getGraphData();
        ProcessBuilder.renderLFWithData(graphData);
        window.location.hash = process['-Id'];
    },
    
    /*
     * Prepare the components to render in canvas
     */
    initComponents : function() {
        //Process
        CustomBuilder.initPaletteElement(get_cbuilder_msg("pbuilder.label.processElements"), "process", get_cbuilder_msg('pbuilder.label.process'), '<i class="fas fa-th-list"></i>',  
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
                    name: 'autoLayout',
                    type: 'Hidden'
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
        CustomBuilder.initPaletteElement(get_cbuilder_msg("pbuilder.label.processElements"), "participant", get_cbuilder_msg('pbuilder.label.participant'), '<i class="las la-swimmer"></i>',  
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
                    js_validation: "ProcessBuilder.validateDuplicateId",
                    regex_validation: '^[a-zA-Z0-9_]+$',
                    validation_message: get_cbuilder_msg("pbuilder.label.invalidId"),
                    id_suggestion: 'label'
                }]
            }]
        , "", false, "", {builderTemplate: {
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
            'getStylePropertiesDefinition' : ProcessBuilder.getParticipantDef,
            'dropEnd' : ProcessBuilder.dropParticipantEnd
        }});
        
        //Activity
        CustomBuilder.initPaletteElement(get_cbuilder_msg("pbuilder.label.processElements"), "activity", get_cbuilder_msg('pbuilder.label.activity'), '<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100%"><rect fill="#ffffff" stroke="#282828" stroke-width="1" radius="2" rx="2" x="1" y="1" ry="2" width="14px" height="12px"></rect></svg>', 
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
                            label: get_cbuilder_msg("pbuilder.label.deadlineLimit"),
                            required : 'true'
                        },{
                            key: 'exceptionName',
                            label: get_cbuilder_msg("pbuilder.label.exceptionName"),
                            required : 'true'
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
        CustomBuilder.initPaletteElement(get_cbuilder_msg("pbuilder.label.processElements"), "tool", get_cbuilder_msg('pbuilder.label.tool'), '<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100%"><rect fill="#ebfdf2" stroke="#386f5c" stroke-width="1" radius="2" rx="2" x="1" y="1" ry="2" width="14px" height="12px"></rect></svg>', 
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
        CustomBuilder.initPaletteElement(get_cbuilder_msg("pbuilder.label.processElements"), "route", get_cbuilder_msg('pbuilder.label.route'), '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 50 50"><polygon fill="#fffbe6" stroke="#faad14" stroke-width="2" points="25,0 50,25 25,50 0,25" opacity="1"></polygon><path d="m 16,15 7.42857142857143,9.714285714285715 -7.42857142857143,9.714285714285715 3.428571428571429,0 5.714285714285715,-7.464228571428572 5.714285714285715,7.464228571428572 3.428571428571429,0 -7.42857142857143,-9.714285714285715 7.42857142857143,-9.714285714285715 -3.428571428571429,0 -5.714285714285715,7.464228571428572 -5.714285714285715,-7.464228571428572 -3.428571428571429,0 z" fill="#fffbe6" stroke="#faad14" stroke-width="2" opacity="1"></path></svg>', 
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
        CustomBuilder.initPaletteElement(get_cbuilder_msg("pbuilder.label.processElements"), "subflow", get_cbuilder_msg('pbuilder.label.subflow'), '<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100%"><rect x="1" y="1" width="14" height="12" fill="#fff" fill-opacity="1" stroke="#282828" stroke-width="1" stroke-opacity="1" opacity="1" rx="2" ry="2"></rect><rect x="3" y="3" width="10" height="8" fill="#fff" stroke="#282828" stroke-width="1" opacity="1" rx="1" ry="1"></rect></svg>', 
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
                    },
                    {
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
        CustomBuilder.initPaletteElement(get_cbuilder_msg("pbuilder.label.processElements"), "start", get_cbuilder_msg('pbuilder.label.start'), '<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100%"><rect fill="#f6ffed" stroke="#52c41a" stroke-width="1" x="1" y="1" rx="20" ry="20" width="13px" height="13px"></rect></svg>', [] , "", true, "", {builderTemplate: {
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
            'nodeDetailContainerColorNumber' : function() {
                return 7;
            },
            'getStylePropertiesDefinition' : ProcessBuilder.getStartDef
        }});
    
        //End
        CustomBuilder.initPaletteElement(get_cbuilder_msg("pbuilder.label.processElements"), "end", get_cbuilder_msg('pbuilder.label.end'), '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 50 50"><circle cx="20" cy="20" r="18" fill="#fff" fill-opacity="1" stroke="#ff4d4f" stroke-width="2" stroke-opacity="1" opacity="1"></circle><circle cx="20" cy="20" r="13" fill="#ff4d4f" stroke="#ff4d4f" stroke-width="2" opacity="1"></circle></svg>', [] , "", true, "", {builderTemplate: {
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
            'nodeDetailContainerColorNumber' : function() {
                return 8;
            }
        }});
    
        //Transition
        CustomBuilder.initPaletteElement(get_cbuilder_msg("pbuilder.label.processElements"), "transition", get_cbuilder_msg('pbuilder.label.transition'), '<i class="las la-arrow-right"></i>', 
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
                    name: 'transitionStyle',
                    label: get_cbuilder_msg("pbuilder.label.style"),
                    type: 'radio',
                    options: [{
                        value: 'straight',
                        label: get_cbuilder_msg("pbuilder.label.straight")
                    },{
                        value: 'orthogonal',
                        label: get_cbuilder_msg("pbuilder.label.orthogonal")
                    }],
                    value: 'orthogonal'
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
            'nodeDetailContainerColorNumber' : function() {
                return 9;
            }
        }});

        setTimeout(() => { // Delay to ensure DOM has been updated
            const paletteElements = document.querySelectorAll('.builder-palette-element');
            paletteElements.forEach(paletteElement => {
                paletteElement.addEventListener('mousedown', (event) => {
                    var type = ProcessBuilder.getLFNodeType(paletteElement.getAttribute("element-class"));
                    ProcessBuilder.lf.dnd.startDrag({
                        type: type
                    });
                });
            });
        }, 0);
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
                    //remove all error messages when switch process
                    $('.toast').each(function(){
                        $(this).toast("hide");
                    });
                    
                    $(window).on('hashchange', ProcessBuilder.viewProcess);
                }, 10);
                
                //update info when process id changed.
                ProcessBuilder.updateAdvancedView();
            } 
            $('#process-selector select').trigger("chosen:updated");
            callback(element);
        } else {
            element.addClass("process");
            element.attr("id", "process_" + elementObj.properties.id);
            element.html("");
            element.attr("data-cbuilder-uneditable", "").attr("data-cbuilder-participants", "");
            
            ProcessBuilder.renderLFWithData(ProcessBuilder.currentLFProcessData);
            ProcessBuilder.recenter();
            
            callback(element);
        }
    },
            
    /*
     * Update Node Properties
     * 
     * @param {string} oldNodeId - used for change node id
     */ 
    updateActivityProperties: function (activityId, newProperties, oldNodeId) {
        if(newProperties.className === 'participant'){
            activityId = activityId.replace('laneID_', '');
            if (oldNodeId) {
                oldNodeId = oldNodeId.replace('laneID_', '');
            }
        }
        let ProcessData = ProcessBuilder.currentProcessData.participants;
        // Loop through each participant
        ProcessData.forEach(participant => {
            if (participant.activities && newProperties.className !== 'participant') {
                if (Array.isArray(participant.activities)) {
                    participant.activities.forEach(activity => {
                        if (activity.properties.id === activityId || activity.properties.id === oldNodeId) {
                            // Update the properties with the new values
                            Object.assign(activity.properties, newProperties);
                        }
                    });
                }
            } else {
                if (participant.properties.id === activityId || participant.properties.id === oldNodeId) {
                    // Update the properties with the new values
                    Object.assign(participant.properties, newProperties);
                    
                    if (oldNodeId) { //update activities performers
                        if (Array.isArray(participant.activities)) {
                            participant.activities.forEach(activity => {
                                if (activity.xpdlObj) {
                                    activity.xpdlObj.Performer = activityId;
                                }
                            });
                        }
                    }
                }
            }
        });
    },
    
    /*
     * Update Transition Properties
     */
    updateTransitionProperties: function (transitionId, newProperties) {
        let ProcessData = ProcessBuilder.currentProcessData.transitions;
        // Loop through each participant
        ProcessData.forEach(transition => {
            if (transition.properties.id === transitionId) {
                // Update the properties with the new values
                Object.assign(transition.properties, newProperties);
            }
        });
    },

    /*
     * Update logic flow node data
     * 
     * @param {string} oldNodeId - used for change node id
     */
    updateLFData: function (element, elementObj, oldNodeId) {
        let nodeId = element[0].data.id;
        if (elementObj.className === 'transition') {
            let label = "";
            if (elementObj.properties.label) {
                label = elementObj.properties.label;
            }
            if (elementObj.properties.condition) {
                if (label !== "") {
                    label += '\n';
                }
                label += elementObj.properties.condition;
            } else if (elementObj.properties.type === 'OTHERWISE' || elementObj.properties.type === 'EXCEPTION') {
                let typeLabel = '[' + elementObj.properties.type + ']';
                if (elementObj.properties.type === 'EXCEPTION') {
                    typeLabel = elementObj.properties.exceptionName;
                }
                if (label !== "") {
                    label += '\n';
                }
                label += typeLabel;
            }
            ProcessBuilder.lf.setProperties(nodeId, elementObj.properties);
            ProcessBuilder.lf.updateText(nodeId, label);
            element.attr("data-cbuilder-visible", "");
            ProcessBuilder.updateTransitionProperties(nodeId, elementObj.properties);
        } else {
            if (elementObj.className === 'participant') {
                elementObj.properties.id = elementObj.properties.id.replace('laneID_', '');
            }
            ProcessBuilder.lf.setProperties(nodeId, elementObj.properties);
            ProcessBuilder.lf.updateText(nodeId, elementObj.properties.label);
            element.attr("data-cbuilder-visible", "");
            ProcessBuilder.updateActivityProperties(nodeId, elementObj.properties, oldNodeId);
        }
    },
    
    // Find the index of an object in the array by matching the "-Id" property.
    findObjectIndexById: function (arr, id) {
        return arr.findIndex(function(item) {
            return item["-Id"] === id;
        });
    },
    
    //Handling for node deleted
    removeNode : function (data) {
        data = data.properties;
        
        //get process xpdl obj
        var xpdlProcess = ProcessBuilder.currentProcessData.xpdlObj;
        if (data.className !== "start" && data.className !== "end") {
            var xpdlActivities = ProcessBuilder.getArray(xpdlProcess['Activities'], 'Activity');
            var index = ProcessBuilder.findObjectIndexById(xpdlActivities, data.id);
            if (index !== -1) {
                xpdlActivities.splice(index, 1);
            }
            ProcessBuilder.setArray(xpdlProcess, 'Activities', 'Activity', xpdlActivities);
        }
        if (data.mapping !== undefined) {
            delete CustomBuilder.data['activityPlugins'][ProcessBuilder.currentProcessData.properties.id + "::" + data.id];
        }
        if (data.formMapping !== undefined) {
            delete CustomBuilder.data['activityForms'][ProcessBuilder.currentProcessData.properties.id + "::" + data.id];
        }
        
        var connSet =  ProcessBuilder.lf.getNodeOutgoingEdge(data.id);
        for (var c in connSet) {
            ProcessBuilder.removeConnection(ProcessBuilder.lf.getEdgeDataById(connSet[c].id), false);
        }
        
        connSet = ProcessBuilder.lf.getNodeIncomingEdge(data.id);
        for (var c in connSet) {
            ProcessBuilder.removeConnection(ProcessBuilder.lf.getEdgeDataById(connSet[c].id), false);
        }

        ProcessBuilder.removeActivity(data.id);
        ProcessBuilder.lf.deleteElement(data.id); 
        
        CustomBuilder.update();
        $("body").addClass("no-right-panel");
    },   
    
    //Handling for a connection event triggered 
    addConnection : function(connection) {
        if (ProcessBuilder.currentProcessData !== undefined) {
            var source = connection.sourceNodeId;
            var target = connection.targetNodeId;
            
            ProcessBuilder.changeNodeId = true; //prevent the add edge event trigger again
            
            // Remove the old edge
            ProcessBuilder.lf.deleteEdge(connection.id);

            // Add a new edge with a proper id and type but same source and target
            //change the id to joget style
            connection.id = ProcessBuilder.getTransitionId(source, target);
                
            var sourceData = ProcessBuilder.lf.getNodeDataById(source);
            var targetData = ProcessBuilder.lf.getNodeDataById(target);
            var isStartEnd = (sourceData.properties.className === "start" || targetData.properties.className === "end");

            connection.className = "transition";
            connection.type = (isStartEnd?"bpmn:straightSequenceFlow":"bpmn:sequenceFlow");
            connection.properties = {
                className :'transition',
                id: connection.id,
                type : isStartEnd?"startend":"",
                transitionStyle : "orthogonal",
                from: source,
                to: target
            };
            
            ProcessBuilder.lf.addEdge(connection);
            
            ProcessBuilder.changeNodeId = false;

            ProcessBuilder.updateSourceTargetData(source, target);
            
            var parentDataArray = ProcessBuilder.currentProcessData['transitions'];
            parentDataArray.push(connection);
            
            //update to logicflow
            ProcessBuilder.lf.updateAttributes(connection.id, connection);
        }
    },
      
    //update source node data and target node data
    updateSourceTargetData: function (source, target) {
        // update split & join
        var sourceConnSet = ProcessBuilder.lf.getNodeOutgoingEdge(source);
        var sourceData = ProcessBuilder.lf.getNodeDataById(source);
        if (sourceData) {
            if (sourceConnSet.length > 1) {
                if (sourceData.properties.split === "") {
                    sourceData.properties.split = "XOR";
                }
            } else {
                sourceData.properties.split = "";
            }
            ProcessBuilder.lf.setProperties(sourceData.id, sourceData.properties);
        }
        
        var targetConnSet = ProcessBuilder.lf.getNodeOutgoingEdge(target);
        var targetData = ProcessBuilder.lf.getNodeDataById(target);
        if (targetData) {
            if (targetConnSet.length > 1) {
                if (targetData.properties.join === "") {
                    targetData.properties.join = "XOR";
                }
            } else {
                targetData.properties.join = "";
            }
            ProcessBuilder.lf.setProperties(targetData.id, targetData.properties);
        }
    },
    
    /**
     * Generate Id for new transition
     * @param {type} startNodeId
     * @param {type} endNodeId
     * @returns {String}
     */
    getTransitionId : function(startNodeId, endNodeId) {
        var node = null;
        if (startNodeId.startsWith('start')) {
            node = ProcessBuilder.lf.getNodeDataById(startNodeId);
        } else if (endNodeId.startsWith('end')) {
            node = ProcessBuilder.lf.getNodeDataById(endNodeId);
        }
        
        if (node && (node.properties.className === "start" || node.properties.className === "end")) {
            return "transition_" + startNodeId + "_" + endNodeId;
        } else {
            var data = ProcessBuilder.currentProcessData;
            var xpdl = CustomBuilder.data.xpdl['Package'];
        
            if (data !== undefined && data !== null) {
                var xpdlProcess = data.xpdlObj;
                var xpdlTransitions = ProcessBuilder.getArray(xpdlProcess['Transitions'], 'Transition');
                
                var count = xpdlTransitions.length + 1;
                
                var allTranstionIds = [];
                for (var t in xpdlTransitions) {
                    var transition = xpdlTransitions[t];
                    allTranstionIds.push(transition['-Id']);
                }
                
                var id;
                do {
                    id = "transition" + count++;
                } while (allTranstionIds.indexOf(id) !== -1);
                
                return id;
            }
        }
    },        
    
    //Handling for a remove connection event triggered 
    removeConnection : function(connection, update = true) {
        var parentDataArray = ProcessBuilder.currentProcessData['transitions'];
        
        var data = connection.properties;
        //get process xpdl obj
        var xpdlProcess = ProcessBuilder.currentProcessData.xpdlObj;
        var xpdlTransitions = ProcessBuilder.getArray(xpdlProcess['Transitions'], 'Transition');
        var index = ProcessBuilder.findObjectIndexById(xpdlTransitions, data.id);
        if (index !== -1) {
            xpdlTransitions.splice(index, 1);
        }
        ProcessBuilder.setArray(xpdlProcess, 'Transitions', 'Transition', xpdlTransitions);
        
        var index = $.inArray(data, parentDataArray);
        if (index !== -1) {
            parentDataArray.splice(index, 1);
        }
        // Delete transition
        ProcessBuilder.lf.deleteElement(data.id);
        
        var source = connection.sourceNodeId;
        var target = connection.targetNodeId;
        
        ProcessBuilder.updateSourceTargetData(source, target);
       
        if (update) {
            CustomBuilder.update();
        }
    },
    
    /*
     * udpate the id & name when element added
     */
    updateElementId : function(elementObj) {
        var self = CustomBuilder.Builder;
        var className = elementObj.className;
            
        if (elementObj.properties.id === undefined || elementObj.properties.id === "" || className === "participant") {
            var nodeCount = $('#lf-container .' + className).length - 1;
            if (nodeCount < 0) {
                nodeCount = 0; //should always start with 0
            }
            var id;
            do {
                if (className === "participant") {
                    id = `${ProcessBuilder.currentProcessData.properties.id}_${className}${++nodeCount}`;
                } else {
                    id = `${className}${++nodeCount}`;
                }
            } while ($(`#lf-container #${className === "participant" ? "laneID_" : ""}${id}`).length > 0);
            elementObj.properties.id = id;
        } else if ($('#lf-container #' + elementObj.properties.id).length > 0) {
            var nodeCount = $('#lf-container #' + elementObj.properties.id).length;

            var id = elementObj.properties.id;

            while ($('#lf-container #' + id).length > 0) {
                id = elementObj.properties.id + "_" + ++nodeCount;
            }
            elementObj.properties.id = id;
            elementObj.id = id;
        } else if ((elementObj.properties.className === 'participant') && $('#lf-container #laneID_' + elementObj.properties.id).length > 0) {
            var nodeCount = $('#lf-container #laneID_' + elementObj.properties.id).length;

            var id = elementObj.properties.id;

            while ($('#lf-container #laneID_' + id).length > 0) {
                id = elementObj.properties.id + "_" + ++nodeCount;
            }
            elementObj.properties.id = id;
            elementObj.id = id;
        }
        
        if ((className === "activity" || className === "tool" || className === "subflow" || className === "participant") 
                && (elementObj.properties.label === undefined || elementObj.properties.label === "")) {
            var componenet = self.getComponent(className);
            elementObj.properties.label = componenet.label + " " + nodeCount;
        }
        
        delete elementObj['xpdlObj'];
    },
    
    
    /*
     * Action implementation for zoom minus icon
     */
    zoomMinus : function() {
        var self = CustomBuilder.Builder;
        self.setZoom("-");
        ProcessBuilder.updateZoomLabel();
        ProcessBuilder.lf.zoom(false);
    },
    
    /*
     * Action implementation for zoom plus icon
     */
    zoomPlus : function() {
        var self = CustomBuilder.Builder;
        self.setZoom("+");
        ProcessBuilder.updateZoomLabel();
        ProcessBuilder.lf.zoom(true);
    },
    
    /*
     * Update zoom label
     */
    updateZoomLabel: function() {
        $("#zoom-minus, #zoom-plus").prop("disabled", false);
        
        var self = CustomBuilder.Builder;
        var strIn = self.zoom - 0.1;
        var strOut = self.zoom + 0.1;
        if (strIn < 0.4) {
            strIn = get_cbuilder_msg('pbuilder.label.disabled');
            $("#zoom-minus").prop("disabled", true);
        } else {
            strIn = Math.round(strIn * 100) + "%";
        }
        if (strOut > 1.6) {
            strOut = get_cbuilder_msg('pbuilder.label.disabled');
            $("#zoom-plus").prop("disabled", true);
        } else {
            strOut = Math.round(strOut * 100) + "%";
        }
        $("#zoom-minus").attr("title", get_cbuilder_msg('pbuilder.label.zoomOut') + " (" + strIn + ")");
        $("#zoom-plus").attr("title", get_cbuilder_msg('pbuilder.label.zoomIn') + " (" + strOut + ")");
    },       

    /*
     * Reset Zoom
     */
    fitScreen: function(){
        var self = CustomBuilder.Builder;
        self.setZoom("");
        ProcessBuilder.updateZoomLabel();
        
        ProcessBuilder.lf.resetZoom();
        ProcessBuilder.recenter();
    },
            
    /**
     * Repositions the graph view to center on the pool node.
     * Ensures that the view is focused on the main pool element of the process.
     */
    recenter: function () {
        const { transformModel, width, height } = ProcessBuilder.lf.graphModel;
        const node = ProcessBuilder.lf.getNodeModelById("poolID_" + ProcessBuilder.currentProcessData.properties.id); // Get the node model
        if (!node) return;

        let { x, y } = node; // Node coordinates
        transformModel.focusOn(x, y, width, height);
    },

    /*
     * Auto layout the whole process design
     */
    autoLayout: function () {
        var self = CustomBuilder.Builder;
        var graphData = ProcessBuilder.lf.getGraphData();
        graphData = ProcessBuilder.autoLayoutNodePosition(graphData);
        ProcessBuilder.renderLFWithData(graphData, true);
        ProcessBuilder.recenter();
        
        if (self.selectedEl && self.selectedEl && self.selectedEl[0].data) {
            ProcessBuilder.selectElementById(self.selectedEl[0].data.properties.id);
        }
    },
            
    autoLayoutNodePosition: function(graphData) {
        const POSITION_TYPE = {
            LEFT_TOP: -1,
            LEFT: 0,
            LEFT_BOTTOM: 1,
            TOP: 2,
            BOTTOM: 3,
            RIGHT: 4,
            RIGHT_TOP: 5,
            RIGHT_BOTTOM: 6
        };
            
        //make the source & target node of a transaction in same vertical or horizontal are align 
        graphData.edges.forEach(edge => {
            let source = graphData.nodes.filter(node => node.id === edge.sourceNodeId)[0];
            let target = graphData.nodes.filter(node => node.id === edge.targetNodeId)[0];
            
            const positionType = ProcessBuilder.getRelativePosition(source, target);
            
            switch (positionType) {
                // LEFT or RIGHT
                case POSITION_TYPE.LEFT || POSITION_TYPE.RIGHT:
                    var diff = Math.abs(source.y - target.y);
                    var y = Math.min(source.y, target.y);
                    if (diff > 40) { //prevent node stack together when move too much
                        y += diff/2;
                    }
                    source.y = y;
                    if (source.text) {
                        source.text.y = y;
                    }
                    target.y = y;
                    if (target.text) {
                        target.text.y = y;
                    }
                    break
                // TOP or BOTTOM
                case POSITION_TYPE.TOP || POSITION_TYPE.BOTTOM:
                    var diff = Math.abs(source.x - target.x);
                    var x = Math.min(source.x, target.x);
                    if (diff > 60) { //prevent node stack together when move too much
                        x += diff/2;
                    }
                    source.x = x;
                    if (source.text) {
                        source.text.x = x;
                    }
                    target.x = x;
                    if (target.text) {
                        target.text.x = x;
                    }
                    break;
                default:
                    break;
            }
        });
        
        //proper align the node to grid
        let lanes = graphData.nodes.filter(lane => lane.type === "lane");
        lanes.forEach(lane => { 
            if (lane.children.length > 0) {
                let childs = graphData.nodes.filter(node => lane.children.indexOf(node.id) !== -1);
                
                childs.forEach(node => {
                    var x = ProcessBuilder.snapToGrid(node.x, 60);
                    var y = ProcessBuilder.snapToGrid(node.y, 40);
                    
                    node.x = x;
                    node.y = y;
                    if (node.text) {
                        node.text.x = x;
                        node.text.y = y;
                    }
                });
            }
        });
        return graphData;
    },        
    
    /**
     * Updates the x and y coordinates of activity nodes in the process data.
     * Ensures that node positions in the graphical representation are reflected in the process data.
     *
     * @param {Object} graphData - The current process graph data containing nodes.
     * @param {boolean} addToUndo - Whether to add the change to undo, default to true
     */
    updateNodePosition: function (graphData, addToUndo = true) {
        let processData = ProcessBuilder.currentProcessData;
        graphData.nodes.forEach(node => {
            if (node.type !== 'pool' && node.type !== 'lane') {
                let laneID = ProcessBuilder.getNodeLaneID(node.id);
                let participant = processData.participants.find(participant => participant.properties.id === ProcessBuilder.removeLanePrefix(laneID));
                if (participant && participant.activities) {
                    for (const activity of participant.activities) {
                        if (activity.properties && activity.properties.id === node.id) {
                            activity.x_offset = node.x;
                            activity.y_offset = node.y;
                        }
                    }
                }
            }
        });
        
        //required to render before calling update, so that the validate status is update correctly
        ProcessBuilder.lf.render(graphData);
        ProcessBuilder.resizePool();
        
        ProcessBuilder.currentProcessData = processData;
        if (!ProcessBuilder.readonly && !$("body").hasClass("initializing")) {
            CustomBuilder.update(addToUndo);
        }
    },
    
    /*
     * Select logic flow node/edge with id
     */
    selectElementById: function (id, showProperties = true) {
        if (id) {
            
            var selectNode = function() {
                ProcessBuilder.lf.selectElementById(id, false, false);

                var nodeData = ProcessBuilder.getSelectedNode();
                if (!nodeData) { 
                    if (id.includes("laneID_")) {
                        nodeData = ProcessBuilder.getLFLane(id);
                    } else {
                        //if not activity node, then try get transition
                        nodeData = ProcessBuilder.getLFEdge(id);
                    }
                }
                if (nodeData) {
                    CustomBuilder.Builder.selectedEl = $({'data' : nodeData});
                    
                    //to make selected for tree viewer & xray viewer
                    if ($(".treeRightPanel .tree-container").length > 0) {
                        let nodeId = id.replace('laneID_', ''); //remove the lane prefix if having it
                        
                        $(".treeRightPanel .tree-container .active").removeClass("active");
                        $(".treeRightPanel .tree-container [data-cbuilder-node-id='"+nodeId+"']").addClass("active");
                    }
                }
            };
            
            if (showProperties) {
                let self = CustomBuilder.Builder;
                try {
                    CustomBuilder.checkChangeBeforeCloseElementProperties(function (hasChange) {
                        if (!hasChange) {
                            selectNode();
                            CustomBuilder.Builder.selectNodeAndShowProperties(CustomBuilder.Builder.selectedEl, false, true);
                        }
                    });
                } catch (err) { }
            } else {
                selectNode();
            }
        }
    },
            
    /*
     * Adjust pool height after auto layout
     */
    updatePoolHeight: function (graphData) {
        const totalHeight = graphData.nodes
            .filter(lane => lane.type === "lane") // Filter objects with type "lane"
            .reduce((sum, lane) => sum + lane.properties.height, 0);

        const poolObject = graphData.nodes.find(node => node.type === "pool");

        if (poolObject) {
            // Update the height in both "properties" and "properties.nodeSize"
            poolObject.properties.height = totalHeight;
            poolObject.properties.nodeSize.height = totalHeight;
        }
        return graphData;
    },

    //Before the final render, adjust all the edges this is due to node changed during auto layout
    updateEdges: function (nodeData) {
        let transitionPosData = {};
        
        //sort edge based on start node & end node x & y
        nodeData.edges.sort((a, b) => {
            const aSourceNode = ProcessBuilder.lf.getNodeDataById(a.sourceNodeId);
            const aTargetNode = ProcessBuilder.lf.getNodeDataById(a.targetNodeId);
            const bSourceNode = ProcessBuilder.lf.getNodeDataById(b.sourceNodeId);
            const bTargetNode = ProcessBuilder.lf.getNodeDataById(b.targetNodeId);
            
            // Get top-left corner of each edge
            let ax = Math.min(aSourceNode.x, aTargetNode.x);
            let ay = Math.min(aSourceNode.y, aTargetNode.y);
            let bx = Math.min(bSourceNode.x, bTargetNode.x);
            let by = Math.min(bSourceNode.y, bTargetNode.y);

            // First sort by y, then by x
            if (ay !== by) return ay - by;
            if (ax !== bx) return bx - ax;
            
            //compare max y pos if both top-left corner is same
            ay = Math.max(aSourceNode.y, aTargetNode.y);
            by = Math.max(aSourceNode.y, aTargetNode.y);
            
            return by - ay;
        });
        
        nodeData.edges.forEach(existEdge => {
            nodeData.edges = nodeData.edges.filter(edge => edge.id !== existEdge.id);
            
            let text = existEdge.text;
            if(text){
                text = existEdge.text.value;
            }
            nodeData.edges.push({
                id: existEdge.id,
                type: existEdge.type,
                sourceNodeId: existEdge.sourceNodeId,
                targetNodeId: existEdge.targetNodeId,
                properties: existEdge.properties,
                text: text,
                ...ProcessBuilder.getEdgeDataPoints(existEdge.sourceNodeId, existEdge.targetNodeId, nodeData.nodes, transitionPosData)
            });
        });
        nodeData.edges = ProcessBuilder.adjustEdgePositions(nodeData.edges);
        return nodeData;
    },
    
    /**
     * Adjusts the positions of overlapping edges to avoid visual clutter.
     * Ensures that edges with the same X or Y coordinates are slightly shifted to improve readability.
     */
    adjustEdgePositions: function (edges) {
        // loop to change y
        let gap = 7;
        edges.forEach((currentEdge) => {
            edges.forEach((targetEdge) => {
                if (currentEdge.id !== targetEdge.id && ProcessBuilder.areStacked(currentEdge, targetEdge)) {
                    if (targetEdge.startPoint.x === targetEdge.endPoint.x) {
                        targetEdge.startPoint.x += gap;
                        targetEdge.endPoint.x += gap;
                    } else if (targetEdge.startPoint.y === targetEdge.endPoint.y) {
                        targetEdge.startPoint.y += gap;
                        targetEdge.endPoint.y += gap;
                    } else {
                        if (targetEdge.startPoint.position === "top" || targetEdge.startPoint.position === "bottom") {
                            targetEdge.startPoint.x += gap;
                        } else {
                            targetEdge.startPoint.y += gap;
                        }
                        if (targetEdge.endPoint.position === "top" || targetEdge.endPoint.position === "bottom") {
                            targetEdge.endPoint.x += gap;
                        } else {
                            targetEdge.endPoint.y += gap;
                        }
                    }
                }
            });
        });
        return edges;
    },
    
    /**
     * Checks if two edges stacked
     */
    areStacked(edge1, edge2) {
        function cross(v1, v2) {
            return v1.x * v2.y - v1.y * v2.x;
        }

        function isColinear(a, b, c) {
            const v1 = { x: b.x - a.x, y: b.y - a.y };
            const v2 = { x: c.x - a.x, y: c.y - a.y };
            return cross(v1, v2) === 0;
        }

        function isOverlap1D(a1, a2, b1, b2) {
            return Math.max(Math.min(a1, a2), Math.min(b1, b2)) <= Math.min(Math.max(a1, a2), Math.max(b1, b2));
        }

        // Step 1: Colinearity
        if (!isColinear(edge1.startPoint, edge1.endPoint, edge2.startPoint) || !isColinear(edge1.startPoint, edge1.endPoint, edge2.endPoint)) {
            return false;
        }

        // Step 2: Overlapping in x and y directions
        const xOverlap = isOverlap1D(edge1.startPoint.x, edge1.endPoint.x, edge2.startPoint.x, edge2.endPoint.x);
        const yOverlap = isOverlap1D(edge1.startPoint.y, edge1.endPoint.y, edge2.startPoint.y, edge2.endPoint.y);

        return xOverlap && yOverlap;
    },
    
    /*
     * Check the node position between source and target node
     */
    getRelativePosition: function (source, target) {
        // Define position types relative to the target
        const POSITION_TYPE = {
            LEFT_TOP: -1, // Source is to the top-left of the target
            LEFT: 0, // Source is directly to the left of the target
            LEFT_BOTTOM: 1, // Source is to the bottom-left of the target
            TOP: 2, // Source is directly above the target
            BOTTOM: 3, // Source is directly below the target
            RIGHT: 4, // Source is directly to the right of the target
            RIGHT_TOP: 5, // Source is to the top-right of the target
            RIGHT_BOTTOM: 6 // Source is to the bottom-right of the target
        };
        
        // Destructure x, y coordinates from source and target objects
        const {x, y} = source,
        {x: x1, y: y1} = target,
        w = source.properties.width,
        h = source.properties.height,
        w1 = target.properties.width,
        h1 = target.properties.height,
        xGap = 50 + Math.max(w, w1) / 2,
        yGap = 50 + Math.max(h, h1) / 2;
        
        // Determine the relative position of source with respect to target
        if (x < x1 - xGap)
            return y < y1 - yGap ? POSITION_TYPE.LEFT_TOP :
                    y > y1 + yGap ? POSITION_TYPE.LEFT_BOTTOM :
                    POSITION_TYPE.LEFT;

        if (x > x1 + xGap)
            return y < y1 - yGap ? POSITION_TYPE.RIGHT_TOP :
                    y > y1 + yGap ? POSITION_TYPE.RIGHT_BOTTOM :
                    POSITION_TYPE.RIGHT;

        // If x coordinates are equal, determine if source is above or below the target
        return y <= y1 - yGap ? POSITION_TYPE.TOP :
                y > y1 + yGap ? POSITION_TYPE.BOTTOM :
                POSITION_TYPE.LEFT; // Default case if source and target overlap
    },
    
    /*
     * Generate start point and end point position based on node position
     */
    getEdgeDataPoints: function (sourceNodeId, targetNodeId, nodeData, transitionPosData = {}) {
        const POSITION_TYPE = {
            LEFT_TOP: -1,
            LEFT: 0,
            LEFT_BOTTOM: 1,
            TOP: 2,
            BOTTOM: 3,
            RIGHT: 4,
            RIGHT_TOP: 5,
            RIGHT_BOTTOM: 6
        };
        var source;
        var target;
        if (nodeData) {
            source = nodeData.find(node => node.id === sourceNodeId);
            target = nodeData.find(node => node.id === targetNodeId);
        }
        const positionType = ProcessBuilder.getRelativePosition(source, target);
        
        // Variables to record entry/exit positions (top, bottom, left, right)
        let startTransitionPosition = '';
        let endTransitionPosition = '';
        
        switch (positionType) {
            // LEFT - Source node is to the left of the target node
            case POSITION_TYPE.LEFT:
                startTransitionPosition = 'right';
                endTransitionPosition = 'left';
                break
            // LEFT_TOP - Source node is to the left and above the target node
            case POSITION_TYPE.LEFT_TOP:
                startTransitionPosition = 'bottom';
                endTransitionPosition = 'left';
                break
            // LEFT_BOTTOM - Source node is to the left and below the target node
            case POSITION_TYPE.LEFT_BOTTOM:
                startTransitionPosition = 'right';
                endTransitionPosition = 'bottom';
                break
            // TOP - Source node is above the target node (same x-coordinate)
            case POSITION_TYPE.TOP:
                startTransitionPosition = 'bottom';
                endTransitionPosition = 'top';
                break;
            // BOTTOM - Source node is below the target node (same x-coordinate)
            case POSITION_TYPE.BOTTOM:
                startTransitionPosition = 'top';
                endTransitionPosition = 'bottom';
                break;
            // RIGHT - Source node is to the right of the target node
            case POSITION_TYPE.RIGHT:
                startTransitionPosition = 'left';
                endTransitionPosition = 'right';
                break;
            // RIGHT_TOP - Source node is to the right and above the target node
            case POSITION_TYPE.RIGHT_TOP:
                startTransitionPosition = 'left';
                endTransitionPosition = 'right';
                break;
            // RIGHT_BOTTOM - Source node is to the right and below the target node
            case POSITION_TYPE.RIGHT_BOTTOM:
                startTransitionPosition = 'left';
                endTransitionPosition = 'bottom';
                break;
            default:
                break;
        }
        
        const startPoint = ProcessBuilder.getPoint(sourceNodeId, source, startTransitionPosition, transitionPosData);
        const endPoint = ProcessBuilder.getPoint(targetNodeId, target, endTransitionPosition, transitionPosData);
        
        //to make sure the vertical/horizontal line remains vertical/horizontal
        if (startPoint.x === endPoint.x) {
            const xGap = startPoint.xGap !== 0?startPoint.xGap:endPoint.xGap;
            startPoint.x += xGap;
            endPoint.x += xGap;
        } else if (startPoint.y === endPoint.y) {
            const yGap = startPoint.yGap !== 0?startPoint.yGap:endPoint.yGap;
            startPoint.y += yGap;
            endPoint.y += yGap;
        } else {
            startPoint.x += startPoint.xGap;
            startPoint.y += startPoint.yGap;
            endPoint.x += endPoint.xGap;
            endPoint.y += endPoint.yGap;
        }
        
        return {
            startPoint,
            endPoint
        };
    },
    
    /**
     * Get the point position for the transition
     */                
    getPoint : function (nodeId, node, position, transitionPosData) {
        var x = ProcessBuilder.snapToGrid(node.x);
        var y = ProcessBuilder.snapToGrid(node.y);
        var w = node.properties.width;
        var h = node.properties.height;
        var xGap = 0;
        var yGap = 0;
        
        var positionCount = ProcessBuilder.countPosition(nodeId, position, transitionPosData);
        var gap = ProcessBuilder.checkPositionIndex(positionCount) * 10;
        
        switch (position) {
            case "top":
                xGap = gap;
                y = y - h/2;
                break;
            case "right":    
                x = x + w/2;
                yGap =  gap;
                break;
            case "bottom": 
                xGap = gap;
                y = y + h/2;
                break;
            case "left": 
                x = x - w/2;
                yGap =  gap;
                break;    
        }
        
        if (!transitionPosData[nodeId]) {
            transitionPosData[nodeId] = { positions: [] };
        }
        transitionPosData[nodeId].positions.push(position);
        
        return {
            x : x,
            y : y,
            xGap : xGap,
            yGap : yGap,
            position: position
        }
    },   
    
    /*
     * get the node position based on grid, grid size is 20 
     * @returns {undefined}
     */               
    snapToGrid : function(v, gridSize = 20) {
        return gridSize * Math.round(v / gridSize) || v;
    },        
    
    /*
     * Count the occurrences of a specific position for a given node ID in transitionPosData
     */
    countPosition: function (nodeId, position, transitionPosData) {
        if (transitionPosData[nodeId] && transitionPosData[nodeId].positions) {
            return transitionPosData[nodeId].positions.filter(pos => pos === position).length;
        }
        return 0; // Return 0 if node ID or positions are not found
    },
    
    // adjust the position based on center index 
    //  6   4   2  0  1  3  5  7
    // -3  -2  -1  0  1  2  3  4
    checkPositionIndex: function (index) {
        if (index === 0) return 0;
        const value = Math.ceil(index / 2);
        return index % 2 === 1 ? value : -value;
    },
        
    /*
     * Turn Higtlight on/off
     */
    highlight: function () {
        const highlightButton = $('button#hightlight');
        const isEnabled = ProcessBuilder.lf.extension.highlight.enable;
        if(!isEnabled){
            highlightButton.addClass('active');
        } else {
            highlightButton.removeClass('active');
        }
        ProcessBuilder.lf.extension.highlight.setEnable(!isEnabled);
    },

    /*
     * Show Minimap
     */
    navigator: function () {
        const navigatorButton = $('button#navigator');
        if(ProcessBuilder.lf.extension.miniMap.isShow === false){
            navigatorButton.addClass('active');
            ProcessBuilder.lf.extension.miniMap.show();
        } else {
            navigatorButton.removeClass('active');
            ProcessBuilder.lf.extension.miniMap.hide();
        }
    },
            
    previewForm: function (event) {

        var formId = $(event.target).attr('formid');

        if (formId) {
            var url = CustomBuilder.contextPath + '/web/fbuilder/app' + CustomBuilder.appPath + '/form/' + formId + '/previewForm';
            JPopup.show("previewForm", url, {}, "");
        }
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
        $('#process-selector select option').removeClass("invalidProcess");
        $('body').find(".invalidNode").each(function () {
            var y = $(this).attr("y");
            var height = $(this).attr("height");

            let msgNode = $(this).find('.invalidNodeMsg');
            msgNode.attr('y', y);
            msgNode.attr('height', height);

            $(this).removeClass("invalidNode");
            ProcessBuilder.lf.deleteProperty($(this).attr("id"), "status");
        });
        $('body').find(".invalidNodeMessage").remove();
        
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
                            var $node = $('body').find("#" + aid);
                            if ($node.find(".invalidNodeMessage").length === 0) {
                                $node.addClass("invalidNode");
                                var messageTransition = get_cbuilder_msg("pbuilder.label.missingTransition");
                                var messageDeadline = get_cbuilder_msg("pbuilder.label.unhandleDeadline");
                                var messageStart = get_cbuilder_msg("pbuilder.label.invalidStart");
                                var message = "";
                                if (activityInvalid) {
                                    message += '<p>' + messageTransition + '</p>';
                                }
                                if (deadlineInvalid) {
                                    message += '<p>' + messageDeadline + '</p>';
                                }
                                if (startInvalid) {
                                    message += '<p>' + messageStart + '</p>';
                                }

                                var $nodeMessage = $('<div class="invalidNodeMessage">' + message + '</div>');


                                $node = $node.find('.invalidNodeMsg');
                                if ($node.length > 0) {
                                    let nodeY = parseFloat($node.attr('y')) + (parseFloat($node.attr('height')) + 2);
                                    $node.attr('y', nodeY);
                                    $node.append($nodeMessage);

                                    let msgDiv = $node.find('.invalidNodeMessage');
                                    var width = msgDiv.outerWidth();
                                    var height = msgDiv.outerHeight();
                                    $node.attr('height', height);
                                    $node.attr('width', width);
                                    ProcessBuilder.lf.setProperties(aid, { status: 'invalid' });
                                }
                            }
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
     * Validation for duplicate id of participant & node
     */
    validateDuplicateId : function (name, value) {
        var self = CustomBuilder.Builder;
        var data = self.selectedEl[0].data;
        
        if (data && data.id !== value) { //check for duplicate id when id changed
            var nodeId = value;
            
            if (data.className === "participant") {
                nodeId = 'laneID_' + value;
            }
            
            //check if the node exist
            var node = ProcessBuilder.lf.getNodeDataById(nodeId);
            if (node) {
                return get_cbuilder_msg("pbuilder.label.duplicateId");
            }
        }
        
        return null;
    },
    
    // Validate if the original XPDL and the new XPDL are identical by comparing their JSON string representations
    compareXPDL: function (oriXPDL, newXPDL) {
        const result = JSON.stringify(oriXPDL) === JSON.stringify(newXPDL);
        return result;
    },
    
    /*
     * Check node is selected with node ID
     */
    isSelectedNode: function (nodeId) {
        const selectedElements = ProcessBuilder.lf.getSelectElements(); // Get selected nodes and edges
        return selectedElements.nodes.some(node => node.id === nodeId); // Check if any node matches the ID
    },

    /*
     * Get selected node
     */
    getSelectedNode: function () {
        const selectedElements = ProcessBuilder.lf.getSelectElements(); // Get selected nodes and edges
        return selectedElements.nodes[0]; // Check if any node matches the ID
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
                {value : "adminUser" , label : get_cbuilder_msg("pbuilder.label.adminUser")},
                {value : "appCreatorUser" , label : get_cbuilder_msg("pbuilder.label.appCreatorUser")},
                {value : "systemManagerUser" , label : get_cbuilder_msg("pbuilder.label.systemManagerUser")}
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
                    options_ajax : CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.workflow.model.ParticipantPlugin',
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
                helplink: get_cbuilder_msg("pbuilder.label.activityMapping.helplink"),
                properties: [{
                    name: 'mapping_act_type',
                    label: get_cbuilder_msg("cbuilder.type"),
                    type : 'selectbox',
                    options : [
                        {value : "SINGLE", label : get_cbuilder_msg("pbuilder.label.form")},
                        {value : "EXTERNAL", label : get_cbuilder_msg("pbuilder.label.externalForm")}
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
                options_ajax : CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.apps.app.model.ProcessFormModifier',
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
                    options_ajax : CustomBuilder.contextPath + '/web/property/json/getElements?classname=org.joget.workflow.model.DecisionPlugin',
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
                options_ajax : '[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.app.model.StartProcessFormModifier',
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
    * return a list of of outgoing transitions options of the current selected activity 
     */                
    getCurrentActivityOutgoingTransition : function() {
        var options = [];
        
        var act = CustomBuilder.Builder.selectedEl[0];
        var sourceConnSet = ProcessBuilder.lf.getNodeOutgoingEdge(act.data.properties.id);
        for (var i = sourceConnSet.length - 1; i >= 0; i--) {
            let node = ProcessBuilder.getActivity(sourceConnSet[i].targetNodeId);
            if (node.className !== 'end') {
                var id = sourceConnSet[i].id;
                var data = ProcessBuilder.lf.getEdgeDataById(id);
                var label = data.properties.label;
                if (label === undefined || label === "") {
                    label = id + " (" + node.properties.label + ")";
                }

                options.push({
                    value: id,
                    label: label
                });
            }
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
                        ProcessBuilder.availableAssignmentFormModifier[returnedData[e].value] = returnedData[e];
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
                        ProcessBuilder.availableStartProcessFormModifier[returnedData[e].value] = returnedData[e];
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
                    if (returnedData[e].value !== "" && returnedData[e].marketplace == undefined) {
                        ProcessBuilder.availableTools[returnedData[e].value] = returnedData[e];
                    }
                }
                ProcessBuilder.initTools();
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
     * Reset the builder back to design view
     */
    screenshotViewBeforeClosed: function(view) {
        $("body").removeClass("no-left-panel");
        ProcessBuilder.fontSVG2Icon();
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
            $(view).find('ul.nav').append('<li id="participants-tab-link" class="nav-item content-tab"><a class="nav-link show active" data-bs-toggle="tab" href="#participants-list-tab" role="tab" aria-controls="participants-list-tab" aria-selected="true"><span>'+get_cbuilder_msg('pbuilder.label.participant')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="participants-list-tab" class="tab-pane fade active show"></div>');
            
            //render activities
            $(view).find('ul.nav').append('<li id="activities-tab-link" class="nav-item content-tab"><a class="nav-link show" data-bs-toggle="tab" href="#activities-list-tab" role="tab" aria-controls="activities-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.activity')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="activities-list-tab" class="tab-pane fade show"></div>');
            
            //render tools
            $(view).find('ul.nav').append('<li id="tools-tab-link" class="nav-item content-tab"><a class="nav-link show" data-bs-toggle="tab" href="#tools-list-tab" role="tab" aria-controls="tools-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.tool')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="tools-list-tab" class="tab-pane fade show"></div>');
            
            //render subflow
            $(view).find('ul.nav').append('<li id="subflows-tab-link" class="nav-item content-tab"><a class="nav-link show" data-bs-toggle="tab" href="#subflows-list-tab" role="tab" aria-controls="subflows-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.subflow')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="subflows-list-tab" class="tab-pane fade show"></div>');
            
            //render routes
            $(view).find('ul.nav').append('<li id="routes-tab-link" class="nav-item content-tab"><a class="nav-link show" data-bs-toggle="tab" href="#routes-list-tab" role="tab" aria-controls="routes-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.route')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="routes-list-tab" class="tab-pane fade show"></div>');
            
            //render transitions
            $(view).find('ul.nav').append('<li id="transitions-tab-link" class="nav-item content-tab"><a class="nav-link show" data-bs-toggle="tab" href="#transitions-list-tab" role="tab" aria-controls="transitions-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.transition')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="transitions-list-tab" class="tab-pane fade show"></div>');
            
            //render variables
            $(view).find('ul.nav').append('<li id="variables-tab-link" class="nav-item content-tab"><a class="nav-link show" data-bs-toggle="tab" href="#variables-list-tab" role="tab" aria-controls="variables-list-tab"><span>'+get_cbuilder_msg('pbuilder.label.workflowVariables')+'</span></a></li>');
            $(view).find('.tab-content').append('<div id="variables-list-tab" class="tab-pane fade show"></div>');
            
            $(view).off("click", ".cbuilder-node-details-list");
            $(view).on("click", ".cbuilder-node-details-list", function(){
                $(view).find(".cbuilder-node-details-list").removeClass("active");
                $(this).addClass("active");
                var id = $(this).attr("data-cbuilder-select");
                
                //set the selected node
                ProcessBuilder.selectElementById(id);
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
        if (process.properties.dataFields) {
            for (var df=0; df<process.properties.dataFields.length; df++) {
                var dataField = process.properties.dataFields[df];
                ProcessBuilder.renderVariableListViewerDetail($(view), dataField);
            }
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
        
        var selectId =  obj.properties.id;
        if (obj.className === "participant") {
            selectId = 'laneID_' + selectId;       
        }
        dl.attr("data-cbuilder-select", selectId);
        
        var id = obj.properties.id;
        
        //the process object is not lofig flow data, need to check to prevent it
        if (self.selectedEl && self.selectedEl.length > 0 && self.selectedEl[0].data) {
            var selectedData = self.selectedEl[0].data;
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
                    label = ProcessBuilder.availableAssignmentFormModifier[elementObj.properties.mapping_act_modifier["className"]].label;
                } else if (elementObj.className === "start" && ProcessBuilder.availableStartProcessFormModifier[elementObj.properties.mapping_act_modifier["className"]] !== undefined) {
                    label = ProcessBuilder.availableStartProcessFormModifier[elementObj.properties.mapping_act_modifier["className"]].label;
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

        codeeditor = CodeMirror(document.getElementById("xpdl_definition"), {
            lineNumbers: true,
            mode: "text",
            autoRefresh:true,
            matchBrackets: true,
            theme: "default",
            gutters: ["CodeMirror-lint-markers", "CodeMirror-linenumbers", "CodeMirror-foldgutter"],
            lint: true,
            autoCloseTags: true,
            autoCloseBrackets: true,
            foldGutter: true,
            lint: true,
            lineWrapping: true,
            highlightSelectionMatches: {annotateScrollbar: true, minChars: 1},
            extraKeys: {
                "Ctrl-F": function(cm) {
                  cm.execCommand("replace")
                  $('#xpdl_definition').find(".CodeMirror-advanced-dialog").css({position:"fixed", zIndex:"2147483647", top: $("body #top-panel").outerHeight() + "px", left:"calc(80% - 320px)", display: 'block'})
                  $('#xpdl_definition').find(".CodeMirror-advanced-dialog").draggable({containment:'parent'})
                },
                "Cmd-F": function(cm) {
                  cm.execCommand("replace")
                  $('#xpdl_definition').find(".CodeMirror-advanced-dialog").css({position:"fixed", zIndex:"2147483647", top: $("body #top-panel").outerHeight() + "px", left:"calc(80% - 320px)", display: 'block'})
                  $('#xpdl_definition').find(".CodeMirror-advanced-dialog").draggable({containment:'parent'})
                },
                "Ctrl-=": function(cm) {
                  cm.increaseFontSize();
                },
                "Ctrl--": function(cm) {
                  cm.decreaseFontSize();
                },
                "Ctrl-/": function(cm) {
                  cm.toggleComment()
                }
              }
          });

        //Set Mode
        codeeditor.setOption("mode", "xml");
        
        //Make the replace appear
        codeeditor.execCommand("replace");

        //Set dark theme if dark theme mode is activated
        if ($('body').attr('builder-theme') === "dark") {
            codeeditor.setOption("theme", "ayu-mirage");
        }

        //Set height
        $('#xpdl_definition').find(".CodeMirror-advanced-dialog").css({display: 'none'})
        $("#xpdl_definition").find(".CodeMirror").css({"height":"auto"});
        $('#xpdl_definition').find(".CodeMirror-scroll").css({"maxHeight":"100%", "minHeight":"100%"});

        codeeditor.setValue(ProcessBuilder.toXpdl())
        
        $(view).find("button.update-btn").on("click", function() {
            var btn = this;
            var text = $(this).text();
            $(this).attr("disabled", true);
            
            ProcessBuilder.updateJsonFromXpdl(codeeditor.getValue(), function(){
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
        if (unsafe === undefined || unsafe === null || unsafe === "" || typeof(unsafe) !== "string") {
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

        let textContent = false;
        if (typeof obj === "object" && !(obj instanceof String)) {
            for (var prop in obj) {
                if (prop === "-self-closing") {
                    selfClosing = obj['-self-closing'];
                } else if (prop.indexOf("-") === 0) {
                    attrs += " " + prop.substring(1) + "=\"" + ProcessBuilder.escapeXml(obj[prop]) + "\"";
                } else if (prop.indexOf("#text") === 0) {
                    body += ProcessBuilder.escapeXml(obj[prop]);
                    textContent = true;
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
                xml = space + "<" + name + attrs + ">" + (textContent ? "" : "\n") + body + (textContent ? "" : space) +"</" + name + ">\n";
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
                        var data = response;
                        if (data !== null && data["Package"] !== undefined) {
                            CustomBuilder.data.xpdl["Package"] = data["Package"];
                            var json = JSON.encode(CustomBuilder.data);
                            CustomBuilder.loadJson(json, true); //update through loadJson addToUndo to make sure package id does not change.
                        }
                    } catch (e) {
                        console.log("Encountered an error in the request: ", e);
                    }
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
        $("#process-selector, .toolzoom-buttons, #listviewer-btn, #xpdl-btn").remove();
        $("#auto-layout, #hightlight, #navigator").parent(".toolbar-group").remove();
        $("#launch-btn").parent().remove();
        $("#lf-container").off("click", "i.previewForm");
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
                "initComponent" : "ProcessBuilder.initComponent"
            }
        }, function() {
            ProcessBuilder.initComponents();
            ProcessBuilder.initialLogicFlow();
            var deferreds = [];
            
            var wait = $.Deferred();
            deferreds.push(wait);
            wait.resolve();
            
            $.when.apply($, deferreds).then(function() {
                ProcessBuilder.generateProcessData(processId);
        
                CustomBuilder.Builder.load(ProcessBuilder.currentProcessData, function(){
                    $('body').addClass('readonly');
                    for (var i in runningActivities) {
                        $('body').find('#'+runningActivities[i]).addClass("running_activity");
                    }
                });
                ProcessBuilder.navigator(true);
                ProcessBuilder.lf.extension.miniMap.show();
                ProcessBuilder.lf.extension.highlight.setEnable(true);
            });
        });
    },
        
    /*
     * A callback method called from CustomBuilder.parseDataToComponent when get component based on data
     * Mainly because the transition fail to retrieve component correctly
     */                
    parseDataToComponent : function(data) {
        var self = CustomBuilder.Builder;
        
        var component = null;
        if (data !== undefined) {
            var type = data.className;
            if (!type && data.type) {
                type = ProcessBuilder.getNodeType(data.type);
            }
            if (type) {
                component = self.getComponent(type);
            }
        }
        return component;
    },
    
    /*
     * A callback method called from CustomBuilder.applyElementProperties when properties saved
     */
    saveEditProperties : function(container, elementProperty, elementObj, element) {
        var nodeId = elementProperty.id;
        
        if(elementProperty.className === "participant"){
            nodeId = 'laneID_' + elementProperty.id;
        }
        
        var oldElementId = null;
        //change node id
        if (elementObj.className !== "process" && nodeId !== $(element)[0].data.id) {
            ProcessBuilder.changeNodeId = true;
            var self = CustomBuilder.Builder;

            var data = $(element)[0].data;
            var oldNodeId = data.id;
            oldElementId = data.id.replace('laneID_', ''); //remove the lane prefix if having it
            var newElementId = data.properties.id;
            
            //update xpdl
            if (data.properties && data.properties.xpdlObj) {
                data.properties.xpdlObj['-Id'] = newElementId;
            }
            data.id = nodeId;
            
            //update mapping
            var participantMapping = CustomBuilder.data['participants'][ProcessBuilder.currentProcessData.properties.id + "::" + oldElementId];
            if (participantMapping) {
                delete CustomBuilder.data['participants'][ProcessBuilder.currentProcessData.properties.id + "::" + oldElementId];
                CustomBuilder.data['participants'][ProcessBuilder.currentProcessData.properties.id + "::" + newElementId] = participantMapping;
            }
            var pluginMapping = CustomBuilder.data['activityPlugins'][ProcessBuilder.currentProcessData.properties.id + "::" + oldElementId];
            if (pluginMapping) {
                delete CustomBuilder.data['activityPlugins'][ProcessBuilder.currentProcessData.properties.id + "::" + oldElementId];
                CustomBuilder.data['activityPlugins'][ProcessBuilder.currentProcessData.properties.id + "::" + newElementId] = pluginMapping;
            }
            var formMapping = CustomBuilder.data['activityForms'][ProcessBuilder.currentProcessData.properties.id + "::" + oldElementId];
            if (formMapping) {   
                delete CustomBuilder.data['activityForms'][ProcessBuilder.currentProcessData.properties.id + "::" + oldElementId];
                CustomBuilder.data['activityForms'][ProcessBuilder.currentProcessData.properties.id + "::" + newElementId] = formMapping;
            }
            
            if(elementProperty.className === "participant"){
                data.children = new Set(data.children);
                ProcessBuilder.lf.updateAttributes(oldNodeId, data);
                
                //update the pool
                const poolId = "poolID_" + ProcessBuilder.currentProcessData.properties.id;
                const poolModel = ProcessBuilder.lf.getNodeModelById(poolId);
                poolModel.removeChild(oldNodeId);
                poolModel.addChild(nodeId);
            } else {
                // retrieve transition
                var transition = [];
                var sourceConnSet = ProcessBuilder.lf.getNodeOutgoingEdge(oldNodeId);
                var targetConnSet = ProcessBuilder.lf.getNodeIncomingEdge(oldNodeId);

                //delete old node
                ProcessBuilder.lf.deleteElement(oldNodeId);

                //add new logic flow node
                ProcessBuilder.lf.addNode(data);

                //update transition logic flow data
                for (var i in sourceConnSet) {
                    var data = sourceConnSet[i];
                    data.sourceNodeId = nodeId;
                    transition.push(data);
                }
                for (var i in targetConnSet) {
                    var data = targetConnSet[i];
                    data.targetNodeId = nodeId;
                    transition.push(data);
                }

                //add transition and update transition xpdl data
                for (var i in transition) {
                    var data = transition[i];
                    data.properties.from = data.sourceNodeId;
                    data.properties.to = data.targetNodeId;
                    ProcessBuilder.lf.addEdge(data);
                    ProcessBuilder.updateTransitionProperties(data.id, data.properties);
                }
            }
            
            ProcessBuilder.changeNodeId = false;
        } else if (elementObj.className === "process" && nodeId !== $(element).attr("id").substring(8)) {
            //update the pool id
            const oldPoolId = "poolID_" + $(element).attr("id").substring(8);
            const newPoolId = "poolID_" + ProcessBuilder.currentProcessData.properties.id;
            
            const poolModel = ProcessBuilder.lf.getNodeModelById(oldPoolId);
            poolModel.id = newPoolId;
            
            ProcessBuilder.lf.updateAttributes(oldPoolId, poolModel);
        }
        
        if (elementObj.className === "transition") {
            //Check if the transition style has changed
            if ((ProcessBuilder.getEdgeType(elementObj.properties.transitionStyle) !== ProcessBuilder.lf.getEdgeModelById(elementObj.properties.id).type)) {
                ProcessBuilder.changeTransitionType(elementObj);
            }
            
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
        
        if (elementObj.className === "process") {
            $(element).attr("id", "process_" + elementProperty.id);
        } else {
            ProcessBuilder.updateLFData(element, elementObj, oldNodeId);
        }
        
        ProcessBuilder.adjustLane(true, true);
    },
    
    /**
     * Switch between Orthogonal and Straight transitions
     */
    changeTransitionType: function (elementObj) {
        ProcessBuilder.changeNodeId = true;
        // Get the edge
        const transitionId = elementObj.properties.id;
        const edge = ProcessBuilder.lf.getEdgeModelById(transitionId);

        // Remove the old edge
        ProcessBuilder.lf.deleteEdge(transitionId);

        // Add a new edge with a different type but same source and target
        edge.type = ProcessBuilder.getEdgeType(elementObj.properties.transitionStyle);
        ProcessBuilder.lf.addEdge(edge);
        ProcessBuilder.changeNodeId = false;
        
        ProcessBuilder.selectElementById(transitionId);
        
        //adjust the edges to prevent stack together, also prevent position changes after refresh
        const graphData = ProcessBuilder.updateEdges(ProcessBuilder.lf.getGraphData());
        ProcessBuilder.lf.render(graphData);
    },

    builderSaved : function(data) {
        ProcessBuilder.updateAdvancedView();
        
        ProcessBuilder.showProcessMigrationChecker(data);
    },
            
    builderSaveFailed : function(data) {
        ProcessBuilder.showProcessMigrationChecker(data);
    },
      
    /**
     * Show a message to block the save button if there is process migration in progress. Unblock when it is done.
     */                
    showProcessMigrationChecker: function(data) {
        if (data.processMigration) {
            if ($("#processMigrationLoader").length === 0) {
                $("#save-btn").parent().append('<div id="processMigrationLoader" class="alert alert-warning"><i class="las la-circle-notch fa-spin"></i> '+get_cbuilder_msg("pbuilder.migrationInProgress")+'</div>');
            }  
            
            //add a progress checker
            var checker = function() {
                $("#save-btn").attr("disabled", "disabled");
                
                $.ajax({ 
                    type: "POST", 
                    url: CustomBuilder.contextPath + '/web/json/console/app/' + CustomBuilder.appId + '/' + CustomBuilder.appVersion + '/process/builder/processUpdateCheck',
                    cache: false,
                    processData: false,
                    contentType: false,
                    beforeSend: function (request) {
                       request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                    },
                    success:function(data) {
                        if (data.processMigration) {
                            setTimeout(function(){
                                checker();
                            }, 5000);
                        } else {
                            $("#processMigrationLoader").remove();
                            $("#save-btn").removeAttr("disabled");
                        }
                    }
                });
            };
            
            setTimeout(function(){
                checker();
            }, 3500);
        }
    },        

    /*
     * Prepare the selector based on overview path parameter
     */
    getOverviewPathElementSelector : function(data, path) {
        var selector = "";
        var propertiesPath = path;
        
        if (path.indexOf("xpdl.") === 0) {
            var xpdl = CustomBuilder.data.xpdl['Package'];
            var xpdlProcesses = ProcessBuilder.getArray(xpdl['WorkflowProcesses']['WorkflowProcess']);
            
            var index = 0;
            if (propertiesPath.indexOf('WorkflowProcess[') !== -1) {
                index = parseInt(propertiesPath.substring(propertiesPath.indexOf('WorkflowProcess[') + 16, propertiesPath.indexOf('].')));
                propertiesPath = propertiesPath.substring(propertiesPath.indexOf('].') + 1);
            }
            var xpdlProcess = xpdlProcesses[index];
            
            if (path.indexOf(".Activity[") !== -1) { //is activity node
                var xpdlActivities = ProcessBuilder.getArray(xpdlProcess['Activities'], 'Activity');
                
                //find activity
                index = parseInt(propertiesPath.substring(propertiesPath.indexOf('.Activity[') + 10, propertiesPath.indexOf('].')));
                propertiesPath = propertiesPath.substring(propertiesPath.indexOf('].') + 1);
                var xpdlActivity = xpdlActivities[index];
                
                selector = "#" + xpdlActivity['-Id'];
            } else if (path.indexOf(".Transition[") !== -1) { //is transition
                var xpdlTransitions = ProcessBuilder.getArray(xpdlProcess['Transitions'], 'Transition');
                
                //find transition
                index = parseInt(propertiesPath.substring(propertiesPath.indexOf('.Transition[') + 12, propertiesPath.indexOf('].')));
                propertiesPath = propertiesPath.substring(propertiesPath.indexOf('].') + 1);
                var xpdlTransition = xpdlTransitions[index];
                
                selector = "#" + xpdlTransition['-Id'];
            } else { //is edit process
                setTimeout(function(){
                    $("#process-edit-btn").trigger("click");
                }, 1);
            
                return ["", propertiesPath];
            }
            
            //show properties tab
            setTimeout(function(){
                new bootstrap.Tab($('#element-properties-tab-link a')[0]).show();
            }, 1);

        } else { //it is mapping
            var temp = path.split("::");
            var id = temp[1].substring(0, temp[1].indexOf("."));
            propertiesPath = temp[1].substring(temp[1].indexOf(".") + 12);
            
            if (id === "processStartWhiteList") {
                selector = '[data-cbuilder-classname="start"]';
            } else if (path.indexOf("participants.") !== -1) {
                selector = "#participant_" + id;
            } else {
                selector = "#" + id;
            }
            
            //show mapping tab
            setTimeout(function(){
                new bootstrap.Tab($('#style-properties-tab-link a')[0]).show();
            }, 1);
        }
        
        return [selector, propertiesPath];
    },
    
    showAdvancedInfo : function() {
        $('#advancedView').slideToggle('slow');
        $('#hideAdvancedInfo').show();
    },
    
    hideAdvancedInfo : function() {
        $('#advancedView').slideToggle('slow');
        $('#hideAdvancedInfo').hide();
    },
      
    /*
     * Reload the cached plugins list
     */  
    marketplaceReloadPalette : function() {
        var deferreds = [];
        ProcessBuilder.cachePlugins(deferreds);
    },
    
    /*
     * Get the lane ID that contains the specified node
     * Searches for a lane node that has the given nodeId in its children
     */
    getNodeLaneID: function (nodeId) {
        const data = ProcessBuilder.lf.getGraphData();
        return data.nodes.find(node => node.type === 'lane' && node.children?.includes(nodeId))?.id || null;
    },

    getLFLane: function (laneID) {
        const data = ProcessBuilder.lf.getGraphData();
        return data.nodes.find(node => node.type === 'lane' && node.id === laneID);
    },
            
    getLFEdge: function (id) {
        const data = ProcessBuilder.lf.getGraphData();
        return data.edges.find(edge => edge.id === id);
    },        
    
    /*
     * Get the activity data from current process data with activityId
     */
    getActivity: function (activityId) {
        const data = ProcessBuilder.currentProcessData.participants;
        for (const participant of data) {
            if (participant.activities) {
                for (const activity of participant.activities) {
                    if (activity.properties && activity.properties.id === activityId) {
                        return activity;
                    }
                }
            }
        }
        return null;
    },
    
    /*
     * Get the activity data from current process data with activityId
     */
    getActivityLane: function (activityId) {
        const data = ProcessBuilder.currentProcessData.participants;
        for (const participant of data) {
            if (participant.activities) {
                for (const activity of participant.activities) {
                    if (activity.properties && activity.properties.id === activityId) {
                        return participant;
                    }
                }
            }
        }
        return null;
    },

    /*
     * Remove the activity data from current process data with activityId
     */
    removeActivity: function (activityId) {
        const data = ProcessBuilder.currentProcessData.participants;
        for (const participant of data) {
            if (participant.activities) {
                for (const activity of participant.activities) {
                    if (activity.properties && activity.properties.id === activityId) {
                        participant.activities.splice(participant.activities.indexOf(activity), 1);
                    }
                }
            }
        }
    },
    
    /*
     * Get the lane data with lane ID
     */
    getLane: function (laneID) {
        const data = ProcessBuilder.currentProcessData.participants;
        if (laneID) {
            if (laneID.includes("laneID_")) {
                laneID = laneID.replace("laneID_", "");
            }
            for (const participant of data) {
                if (participant.properties.id === laneID) {
                    return participant;
                }
            }
        }
        return null;
    },

    /*
     *  Get the index of a lane in the participants list based on the lane ID.
     */
    getLaneIndex: function (laneID) {
        const data = ProcessBuilder.currentProcessData.participants;
        if (laneID.includes("laneID_")) {
            laneID = laneID.replace("laneID_", "");
        }
        return data.findIndex(participant => participant.properties.id === laneID);
    },
    
    /*
     *  Get connection object by its ID from the process data
     */
    getConnection: function (connectionId) {
        const data = ProcessBuilder.currentProcessData.transitions;
        return data.find(transition => transition.connection && transition.properties.id === connectionId);
    },
            
    /*
     *  Get node type
     */
    getNodeType: function (type) {
        if (type === 'bpmn:userTask') {
            return 'activity';
        } else if (type === 'bpmn:serviceTask') {
            return 'tool';
        } else if (type === 'bpmn:startEvent') {
            return 'start';
        } else if (type === 'bpmn:exclusiveGateway') {
            return 'route';
        } else if (type === 'bpmn:subflow') {
            return 'subflow';
        } else if (type === 'bpmn:endEvent') {
            return 'end';
        } else if (type === 'bpmn:sequenceFlow' || type === 'bpmn:straightSequenceFlow') {
            return 'transition';
        } else if (type === 'lane') {
            return 'participant';
        }
    },
            
    /*
     *  Get logic flow node type
     */
    getLFNodeType: function (type) {
        if (type === 'activity') {
            return 'bpmn:userTask';
        } else if (type === 'tool') {
            return 'bpmn:serviceTask';
        } else if (type === 'start') {
            return 'bpmn:startEvent';
        } else if (type === 'route') {
            return 'bpmn:exclusiveGateway';
        } else if (type === 'subflow') {
            return 'bpmn:subflow';
        } else if (type === 'end') {
            return 'bpmn:endEvent';
        } else if (type === 'transition') {
            return 'bpmn:sequenceFlow';
        } else if (type === 'participant') {
            return 'lane';
        }
    },

    getEdgeType: function (type) {
        if (type === 'orthogonal') {
            return 'bpmn:sequenceFlow';
        } else {
            return 'bpmn:straightSequenceFlow';
        }
    },
    
    /*
     *  Override the addElement method from CustomBuilder to customize element addition behavior
     */
    addElement: function (data, updateNode, cloneIndex, callback) {
        let oriId = data.id;
        var self = CustomBuilder.Builder;
        var nodeType = ProcessBuilder.getNodeType(data.type);
        self.component = CustomBuilder.paletteElements[nodeType];

        var classname = self.component.className;
        var elementObj;
        if (updateNode) {
            var properties = {};
            if (self.component.properties !== undefined) {
                properties = $.extend(true, properties, self.component.properties);
            }
            if (self.component.builderTemplate.properties !== undefined) {
                properties = $.extend(true, properties, self.component.builderTemplate.properties);
            }
            elementObj = {
                className: classname,
                properties: properties,
                x_offset: data.x,
                y_offset: data.y
            };
            // Add properties dynamically
            elementObj.properties.className = classname;
            self.updateElementId(elementObj);
        } else {
            if (data.type === 'lane') {
                elementObj = {
                    className: data.properties.className,
                    properties: data.properties,
                    activities: data.properties.activities
                };
            } else {
                elementObj = {
                    className: data.properties.className,
                    properties: data.properties,
                    x_offset: data.x,
                    y_offset: data.y
                };
            }
            ProcessBuilder.updatePasteElement = true;
        }
        ProcessBuilder.draggingElementId = elementObj.properties.id;
        var childsDataHolder = self.component.builderTemplate.getChildsDataHolder(elementObj, self.component);
        var elements = [];
        if (self.component.builderTemplate[childsDataHolder] !== undefined) {
            elements = $.extend(true, elements, self.component.builderTemplate[childsDataHolder]);
            elementObj[childsDataHolder] = elements;
        }

        if (self.dragElement === null || self.dragElement === undefined) {
            self.dragElement = $('<div class="node ' + elementObj.className + '"></div>');
            self.selectedEl = self.dragElement;
        }

        var parent = $(self.dragElement).closest("[data-cbuilder-classname]");
        if ($(parent).length === 0) {
            parent = $(self.dragElement).closest("body");
        }

        if (elementObj.className !== 'participant' && elementObj.properties.className !== 'participant') {
            const parentLane = ProcessBuilder.getNodeLaneID(data.id).replace("laneID_", "");
            for (const item of ProcessBuilder.currentProcessData.participants) {
                if (item?.properties?.id === parentLane) {
                    data = item; // Return the object if the id matches
                }
            }
        } else {
            data = ProcessBuilder.currentProcessData;
        }

        var index = 0;
        var container = $(self.dragElement).parent().closest("[data-cbuilder-" + self.component.builderTemplate.getParentContainerAttr(elementObj, self.component) + "]");
        index = $(container).find("> *").index(self.dragElement);
        var parentDataArray = data[self.component.builderTemplate.getParentDataHolder(elementObj, self.component)];
        if (parentDataArray === undefined) {
            parentDataArray = [];
            data[self.component.builderTemplate.getParentDataHolder(elementObj, self.component)] = parentDataArray;
        }
        if ($(container).is('[data-cbuilder-single]')) {
            parentDataArray.splice(0, parentDataArray.length, elementObj);
            $(container).find("> [data-cbuilder-classname]").remove();
        } else {
            if (cloneIndex !== null && cloneIndex !== undefined) {
                index = cloneIndex;
            }
            if (elementObj.className === 'participant') {
                index = ProcessBuilder.getNewLaneIndex(oriId);
            }
            parentDataArray.splice(index, 0, elementObj);
        }
    },
            
    /**
     * Retrieves the index of a specific lane based on its vertical position.
     */
    getNewLaneIndex: function (targetLaneID) {
        let graphData = ProcessBuilder.lf.getGraphData();
        let lanes = graphData.nodes.filter(lane => lane.type === "lane");
        lanes = lanes.sort((a, b) => a.y - b.y);
        return lanes.findIndex(lane => lane.id === targetLaneID);
    },
    
    /*
     *  Handle for lane deletion
     */
    deleteLane: function (node) {
        var processData = ProcessBuilder.currentProcessData;
        const removeLane = ProcessBuilder.getLane(node.properties.id);
        if (removeLane.activities) {
            removeLane.activities.forEach(activity => {
                ProcessBuilder.removeNode(activity);
            });
        }

        // Reassign the filtered array back to participants
        processData.participants = processData.participants.filter(
            participant => participant.properties.id !== node.properties.id
        );

        CustomBuilder.update();
    },
    
    /*
     * A callback method called from the CustomBuilder.Builder.renderNodeAdditional
     * It is used to render the tree and handle the li click event to select the target node
     */
    renderTreeMenuAdditionalNode: function (container, target) {
        //render tree node
        let graphData = ProcessBuilder.lf.getGraphData();
        let lanes = graphData.nodes.filter(lane => lane.type === "lane");
        lanes = lanes.sort((a, b) => a.y - b.y);
        lanes.forEach(lane => { 
            let laneLi = ProcessBuilder.renderTreeNode(container, lane);
            if (lane.children.length > 0) {
                lane.children.forEach(child => {
                    let node = graphData.nodes.filter(node => node.id === child)[0];
                    let nodeLi = ProcessBuilder.renderTreeNode(laneLi, node);
                    
                    //render outgoing transition
                    var sourceConnSet = ProcessBuilder.lf.getNodeOutgoingEdge(child);
                    sourceConnSet.forEach(edge => {
                        let edgeData = ProcessBuilder.getLFEdge(edge.id);
                        ProcessBuilder.renderTreeNode(nodeLi, edgeData);
                    });
                });
            }
        });
        
        if (ProcessBuilder.getSelectedNode()) {
            let selectedElementId = ProcessBuilder.removeLanePrefix(ProcessBuilder.getSelectedNode().id);
            ProcessBuilder.selectActiveTreeItem(selectedElementId);
        }

        $(container).off("click", "li.tree-viewer-item  label");
        $(container).on("click", "li.tree-viewer-item  label", function (e) {
            let nodeData = $(this).parent().data("nodeData");            
            ProcessBuilder.selectElementById(nodeData.id, true);   
        });
        return container;
    },
            
    renderTreeNode: function(container, data) {
        if (container.find("> ol").length === 0) {
            container.append('<ol></ol>');
        }
        
        var self = CustomBuilder.Builder;
        var rid = "r" + (new Date().getTime());
        var component = self.parseDataToComponent(data);
        var props = self.parseElementProps(data);
        
        if (component.builderTemplate.customPropertiesData) {
            props = component.builderTemplate.customPropertiesData(props, data, component);
        }

        var label = component.label;
        if (component.builderTemplate.getLabel) {
            label = component.builderTemplate.getLabel(data, component);
        } else if (component.builderTemplate.isSupportProperties(data, component)) {
            if (props.label !== undefined && props.label !== "") {
                label = props.label;
            } else if (props.textContent !== undefined && props.textContent !== "") {
                label = props.textContent;
            } else if (props.id !== undefined && props.id !== "" && props.id.length < 32) {
                label = props.id;
            }
        }

        label = UI.stripHtmlTags(label);
        if (label.length > 30) {
            label += label.substring(0, 27) + "...";
        }

        var li = $('<li class="tree-viewer-item"><label>'+component.icon+' <a>'+label+'</a></label><input type="checkbox" id="'+rid+'" checked/></li>');
        $(li).data("nodeData", data);
        $(li).attr("data-cbuilder-node-id", props.id);

        container.find("> ol").append(li);
        
        return li;
    },        

    /*
     * Remove 'laneID_' prefix from lane ID if present
     * This function checks if a given lane ID starts with 'laneID_' and removes it
     * Used to standardize lane IDs when working with both prefixed and unprefixed versions
     */
    removeLanePrefix: function(laneId){  
        if (typeof laneId === 'string' && laneId.startsWith('laneID_')) {
            return laneId.substring(7); // Remove 'laneID_' prefix
        }   
        return laneId;
    }, 

    /*
     * Select the active tree item based on the given element ID
     * This function removes the 'active' class from all tree items and then adds it to the item with the matching ID
     */
    selectActiveTreeItem: function(selectedElementId){
        $(".tree-viewer-item").removeClass("active");
        $(".tree-viewer-item[data-cbuilder-node-id='" + selectedElementId + "']").addClass("active");
    },
    
    /**
     * Handles pasting an element into the process graph.
     */
    pasteElement: function (element, elementObj, component, copiedObj, copiedComponent) {
        let xPosition = element.x;
        let yPosition = element.y;
        let gap = 150;
        ProcessBuilder.updatePasteElement = false;
        if (copiedObj.type !== 'lane') {
            if (!copiedObj.text) copiedObj.text = {}; // Ensure "text" property exists. If not, initialize
            if (element.properties.className === 'participant') {
                xPosition = parseInt(copiedObj.x);
                copiedObj.x = xPosition + gap;
                copiedObj.y = yPosition;
                copiedObj.text.x = xPosition + gap;
                copiedObj.text.y = yPosition;
            } else {
                copiedObj.x = xPosition + gap;
                copiedObj.y = yPosition;
                copiedObj.text.x = xPosition + gap;
                copiedObj.text.y = yPosition;
            }
            ProcessBuilder.lf.addNode(copiedObj);
        } else {
            let index;
            if (element.properties.className === 'participant') {
                index = ProcessBuilder.getLaneIndex(element.properties.id) + 1;
            } else {
                const lane = ProcessBuilder.getActivityLane(element.properties.id);
                index = ProcessBuilder.getLaneIndex(lane.properties.id) + 1;
            }
            ProcessBuilder.addElement(copiedObj, ProcessBuilder.updatePasteElement, index);
        }
    },
    
    /**
     * Captures a snapshot, converts it to base64, displays it, and adds a download button.
     * @returns {Promise<{data: string}>} A promise that resolves with the base64-encoded snapshot.
     */
    getSnapshotBase64AndDisplayImage: async function () {
        try {
            // Get the process ID and generate a file name
            const id = ProcessBuilder.currentProcessData.properties.id;
            const fileName = `${CustomBuilder.appId}-${CustomBuilder.builderType}-${id}`;
            // Capture the snapshot as a Blob
            const snapshot = await ProcessBuilder.lf.getSnapshotBlob();
            // Ensure snapshot data is a valid Blob
            if (!(snapshot.data instanceof Blob))
                throw new Error('Snapshot data is not a Blob.');
            // Convert the Blob to a base64 string
            const base64 = await ProcessBuilder.blobToBase64(snapshot.data);
            // Display the image in the UI
            $("#screenshotViewImage").html(`<img style="max-width:98%; border:1px solid #ddd;" src="data:image/png;base64,${base64}"/>`);
            // Create and append the download button
            const link = $('<a>', {
                class: "btn button btn-secondary",
                html: get_cbuilder_msg('cbuilder.download'),
                click: () => ProcessBuilder.lf.getSnapshot(fileName, {backgroundColor: "#ffffff"})
            });
            $("#screenshotView .sticky-buttons").append(link);
            // Reset screenshot timeout
            CustomBuilder.screenshotTimeout = null;
            return {data: base64};
        } catch (error) {
            console.error('Error while getting snapshot:', error);
            throw error;
        }
    },

    /**
     * Converts a Blob object to a base64-encoded string.
     * @param {Blob} blob - The Blob to convert.
     * @returns {Promise<string>} A promise that resolves with the base64 string.
     */
    blobToBase64: function (blob) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => resolve(reader.result.split(',')[1]); // Extract only the base64 part
            reader.onerror = reject;
            reader.readAsDataURL(blob);
        });
    },

    /**
     * Triggers the snapshot capture and handles any errors.
     */
    getScreenShoot: async function () {
        try {
            await ProcessBuilder.getSnapshotBase64AndDisplayImage();
        } catch (error) {
            console.error('Error:', error);
        }
    },

    /*
    * A callback method called from the CustomBuilder.Builder.copyNode
    * It is used to handle the copy behavior of the Logic Flow node
    */
    copyNode: function (node) {
        var self = CustomBuilder.Builder;
        if (!node) {
            node = self.selectedEl;
        }
        var data = node[0].data;
        var component = self.parseDataToComponent(data.properties);
        var type = component.builderTemplate.getParentContainerAttr(data, component);

        // Custom copy logic or extend existing behavior
        CustomBuilder.copy(data, type);
        if (CustomBuilder.Builder.options.callbacks["copyElement"]) {
            CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["copyElement"], [data, type]);
        }

        self.selectNode(self.selectedEl);
        if (component.builderTemplate.isPastable(data, component)) {
            $("#paste-element-btn").removeClass("disabled");
        }
    },

    /*
    * A callback method called from the CustomBuilder.Builder.pasteNode
    * It is used to handle the copy behavior of the Logic Flow node
    */
    pasteNode: function (node) {
        var self = CustomBuilder.Builder;
        if (!node) {
            node = ProcessBuilder.getSelectedNode();
        }

        self.component = self.parseDataToComponent($(node).data("data"));
        var data = CustomBuilder.getCopiedElement();
        var copiedObj = $.extend(true, {}, data.object);
        var copiedComponent = self.parseDataToComponent(copiedObj.properties);

        ProcessBuilder.updateElementId(copiedObj);
        if (copiedObj.properties.className === 'participant') {
            const activities = copiedObj.properties.activities;
            // Loop through and print each activity object
            activities.forEach(activity => {
                ProcessBuilder.updateElementId(activity);
            });
        }

        if (copiedComponent.builderTemplate.customPasteData) {
            copiedComponent.builderTemplate.customPasteData(copiedObj, copiedComponent);
        }

        if (CustomBuilder.Builder.options.callbacks["pasteElement"] !== undefined && CustomBuilder.Builder.options.callbacks["pasteElement"] !== "") {
            CustomBuilder.callback(CustomBuilder.Builder.options.callbacks["pasteElement"], [node, $(node).data("data"), self.component, copiedObj, copiedComponent]);
        } else {
            self._pasteNode(node, copiedObj, copiedComponent);
        }

        CustomBuilder.update();
        if (copiedObj.properties.className === 'participant') {
            CustomBuilder.loadJson(ProcessBuilder.currentProcessData, false);
        }
        ProcessBuilder.selectElementById(copiedObj.id);
    },

    /*
    * A callback method called from the CustomBuilder.Builder.removeNodeAdditional
    * It is used to remove the node details render during x-ray mode
     */
    removeAdditionalNode: function () {
        $('link[href="' + CustomBuilder.contextPath + '/builder/editor-helpers.css"]').remove();
        let target = $('#lf-container');
        $("#node-details-toggle").hide();
        target.removeClass("show-node-details show-node-details-single");
        target.find(".cbuilder-node-details").remove();
        target.find(".cbuilder-node-details-reset-margin-top").removeClass("cbuilder-node-details-reset-margin-top");
    },

    /*
    * A callback method called from the CustomBuilder.Builder.renderScreenshot
    * It is used to handle the render screensho
    */
    getScreenshot: function () {
        // Convert font icon to SVG
        FontAwesome.dom.i2svg();
        ProcessBuilder.lineIcon2SVG();
        
        //Disable unnecesary screenshot on change
        $(CustomBuilder.Builder.iframe).off("change.builder", CustomBuilder.Builder.renderScreenshot);

        CustomBuilder.screenshotTimeout = setTimeout(function () {
            ProcessBuilder.getScreenShoot();
        }, 300);
    },

    /*
    * A callback method called from the CustomBuilder.Builder.beforeRenderNodeAdditional
    * It is used to handle the render X-ray node
    */
    beforeRenderNodeAdditional: function (type) {
        if (type === 'Xray') {
            $('head').append('<link rel="stylesheet" type="text/css" href="' + CustomBuilder.contextPath + '/builder/editor-helpers.css">');
        }
        let target = $('#lf-container');
        $("#node-details-toggle").find("input").off("click");
        $("#node-details-toggle").find("input").on("click", function () {
            if ($("#details-toggle-single").is(":checked")) {
                $('body').addClass("show-node-details-single");
            } else {
                $('body').removeClass("show-node-details-single");
            }
            self.triggerEvent("nodeAdditionalModeChanged");
        });
        $('body').addClass("show-node-details");
        return target;
    },

    /*
    * A callback method called from the CustomBuilder.Builder.afterRenderNodeAdditional
    * It is used to handle the after render node additional
    */
    afterRenderNodeAdditional: function (detailsDiv, target, data) {
        setTimeout(function () {
            const newWidth = $(detailsDiv).find('dl').outerWidth();
            const newHeight = $(detailsDiv).find('dl').outerHeight();
            var x = $(target).attr("x");
            var y = $(target).attr("y");
            var height = $(target).attr("height");
            if (data.className === 'participant') {
                $(target).find('.xray-view').attr("x", x - newWidth - 30);
                $(target).find('.xray-view').attr("y", y - (height / 2));
            } else {
                $(target).find('.xray-view').attr("y", y - newHeight);
            }
            $(target).find('.xray-view').attr("width", newWidth);
            $(target).find('.xray-view').attr("height", newHeight);
        }, 100);
    },

    /*
    * A callback method called from the CustomBuilder.Builder.renderNodeAddtionalData
    * It is used to handle the render node additional data
    */
    renderNodeAddtionalData: function (element) {
        var elementId = $(element[0]).attr('data-cbuilder-id');
        var data;
        if (elementId.includes("laneID_")) {
            elementId = elementId.replace("laneID_", "");
            data = ProcessBuilder.getLane(elementId);
        } else {
            data = ProcessBuilder.getActivity(elementId);
        }
        return data;
    },

    /*
    * A callback method called from the CustomBuilder.Builder.changeNodeAddtionalTarget
    * It is used to handle the change the node additional target
    */
    changeNodeAddtionalTarget: function (target, detailsDiv) {
        $(target).find('.xray-view').prepend(detailsDiv);
    },

    /*
    * A callback method called from the CustomBuilder.Builder.modifyShowPropertiesData
    * It is used to handle modify properties data
    */
    modifyShowPropertiesData: function (data, target) {
        if (typeof(target.data) === "function") {
            data = target.data("data");
        }
        if (!data) {
            data = target[0].data.properties;
            data = {
                className: data.className,
                properties: data,
                xpdlObj: data.xpdlObj
            };
        }
        return data;
    },
            
    lineIcon2SVG: function () {
        $('#lf-container i[class^="la"]').each(function () {
            const $icon = $(this);
            const classes = $icon.attr('class').split(' ');
            const laClass = classes.find(cls => cls.startsWith('la-') && cls !== 'las' && cls !== 'lab');
            if (laClass) {
                const iconName = laClass.replace('la-', '');
                const svgUrl = `/jw/js/line-awesome-1.3.0/svg/${iconName}-solid.svg`;
    
                $.get(svgUrl, function (data) {
                    if (data) {
                        const $svg = $(data.documentElement || data)
                            .attr('class', $icon.attr('class'))
                            .css({
                                width: $icon.css('width') || '1.2em',
                                height: $icon.css('height') || '1.2em',
                                fill: 'currentColor',
                                cursor: 'pointer' // Optional: to indicate it's clickable
                            });
    
                        // Comment out the original <i> element
                        const originalHTML = $icon[0].outerHTML;
                        const commentNode = document.createComment(originalHTML);

                        // Insert the SVG before the icon and comment the icon out
                        $icon.before($svg[0]);
                        $icon.replaceWith(commentNode);
                    }
                }).fail(function () {
                    console.warn(`Failed to load SVG: ${svgUrl}`);
                });
            }
        });
    },

    fontSVG2Icon: function () {
        $('svg.svg-inline--fa, svg.svg2Icon').each(function () {
            const $svg = $(this);
            const $parent = $svg.parent();
        
            const nodes = $parent.contents().toArray();
            const svgIndex = nodes.indexOf(this);
            const nextNode = nodes[svgIndex + 1];
        
            if (nextNode && nextNode.nodeType === Node.COMMENT_NODE) {
                const temp = $('<div>').html(nextNode.nodeValue);
                const $icon = temp.find('i');
        
                if ($icon.length) {
                    $svg.replaceWith($icon);
                    $(nextNode).remove();
                }
            }
        });
    }
};
