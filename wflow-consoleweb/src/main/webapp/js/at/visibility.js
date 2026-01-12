VisibilityManager = {
    container: null,

    /**
     * Render the visibility manager UI
     * @param {jQuery} container - The container element to render
     */
    render: function(container) {
        if (CustomBuilder.paletteElements['visibility-rule'] === undefined) {
            this.initVisibilityComponent();
        }

        this.container = $(container);
        $(this.container).html(
            '<div class="visibility_view">' +
                '<div class="visibility_rules">' +
                    '<div class="buttons">' +
                        '<a class="add_visibility button"><i class="fas fa-plus"></i> ' + get_advtool_msg('adv.visibility.addRule') + '</a>' +
                    '</div>' +
                    '<div class="sortable"></div>' +
                '</div>' +
                '<div class="elements_container"></div>' +
            '</div>'
        );

        this.migrateLegacyRules();
        this.renderRules();
        this.renderElementsHeader();
        this.attachEvents();
    },

    /**
     * Initialize the visibility rule component for property editor
     */
    initVisibilityComponent: function() {
        var self = this;
        CustomBuilder.initPaletteElement("", "visibility-rule", "", "", [], "", false, "", {
            builderTemplate: {
                customPropertyOptions: function(elementOptions, element, elementObj, paletteElement) {
                    var ruleName = elementObj.properties.visibility_name || get_advtool_msg('adv.visibility.unnamed');
                    var propertiesDefinition = [{
                        title: get_advtool_msg('adv.tool.visibility') + " (" + ruleName + ")",
                        properties: [{
                            name: 'visibility_key',
                            type: 'hidden'
                        }, {
                            name: 'visibility_name',
                            type: 'textfield',
                            label: get_advtool_msg('dependency.tree.Name'),
                            required: "true"
                        }, {
                            name: 'rules',
                            label: get_advtool_msg('adv.visibility.conditions'),
                            type: 'custom',
                            script_url: CustomBuilder.contextPath + '/web/json/app' + CustomBuilder.appPath + '/plugin/org.joget.apps.form.model.Section/service'
                        }]
                    }];
                    return propertiesDefinition;
                },
                'render': function(element, elementObj, component, callback) {
                    var name = elementObj.properties['visibility_name'] || get_advtool_msg('adv.visibility.unnamed');
                    var conditionsSummary = get_advtool_msg('adv.visibility.noConditions');

                    // Get conditions summary
                    if (elementObj.properties['visibilityControl'] && elementObj.properties['visibilityControl'] !== "") {
                        var fields = elementObj.properties['visibilityControl'].split(";").filter(function(f) {
                            return f && f !== "(" && f !== ")";
                        });
                        conditionsSummary = fields.length + " " + get_advtool_msg('adv.visibility.condition') + (fields.length > 1 ? "s" : "");
                    }

                    $(element).find(".name").text(name);
                    $(element).find(".conditions_summary").text(conditionsSummary);
                    $("body").addClass("no-right-panel");
                    VisibilityManager.refreshAllTooltips();
                }
            }
        });
    },

    /**
     * Render the elements table header
     */
    renderElementsHeader: function() {
        var elemContainer = $(this.container).find('.elements_container');
        $(elemContainer).append(
            '<table class="visibility-table">' +
                '<thead><tr>' +
                    '<th width="70%"></th>' +
                    '<th class="apply" width="30%">' + get_advtool_msg('adv.visibility.applyRule') + '</th>' +
                '</tr></thead>' +
                '<tbody></tbody>' +
            '</table>'
        );
    },

    /**
     * Attach all event handlers
     */
    attachEvents: function() {
        var self = this;
        var $container = $(this.container);

        // Sortable rules
        $container.find(".sortable").sortable({
            opacity: 0.8,
            axis: 'y',
            handle: '.sort',
            tolerance: 'intersect',
            stop: function() {
                self.updateRulesOrder();
            }
        });

        // Rule click events 
        $container.off("click.visibility");
        $container.on("click.visibility", ".visibility_rule:not(.active)", function() {
            self.setActiveRule($(this));
        });
        $container.on("click.visibility", ".visibility_rule .edit_rule", function(event) {
            self.editRule($(this).closest(".visibility_rule"));
            event.stopImmediatePropagation();
        });
        $container.on("click.visibility", ".visibility_rule .delete_rule", function(event) {
            self.removeRule($(this).closest(".visibility_rule"));
            event.stopImmediatePropagation();
        });

        // Tree toggle
        $container.on("click.visibility", "tr .toggle-btn i", function() {
            self.toggleTreeNode($(this).closest("tr"));
        });

        // Add rule button
        $container.find(".add_visibility").off("click").on("click", function() {
            self.addRule();
        });

        // Select first rule if exists, otherwise show empty state
        var firstRule = $container.find(".sortable .visibility_rule:eq(0)");
        if (firstRule.length > 0) {
            this.setActiveRule(firstRule);
        } else {
            this.renderEmptyState();
        }
    },

    /**
     * Render empty state when no rules are defined
     */
    renderEmptyState: function() {
        var $elemContainer = $(this.container).find('.elements_container');
        $elemContainer.find('.visibility-table').hide();
        $elemContainer.find('.empty-state').remove();
        $elemContainer.append(
            '<div class="empty-state">' +
                '<i class="las la-eye-slash"></i>' +
                '<p>' + get_advtool_msg('adv.visibility.noRules') + '</p>' +
            '</div>'
        );
    },

    /**
     * Hide empty state when rules exist
     */
    hideEmptyState: function() {
        var $elemContainer = $(this.container).find('.elements_container');
        $elemContainer.find('.empty-state').remove();
        $elemContainer.find('.visibility-table').show();
    },

    /**
     * Toggle tree node expand/collapse
     * @param {jQuery} tr - The table row element
     */
    toggleTreeNode: function(tr) {
        $(tr).toggleClass("collapsed");
        var level = $(tr).data("level");
        var cssClass = this.buildLevelSelector(level);

        if ($(tr).hasClass("collapsed")) {
            $(tr).nextUntil(cssClass).hide();
        } else {
            $(tr).nextUntil(cssClass).removeClass("collapsed").show();
        }
    },

    /**
     * Build CSS selector for levels up to specified level
     * @param {number} level - The level number
     * @returns {string} CSS selector
     */
    buildLevelSelector: function(level) {
        var selectors = [];
        for (var i = level; i > 0; i--) {
            selectors.push("tr.level-" + i);
        }
        return selectors.join(", ");
    },

    /**
     * Get the object that holds visibility rules (form properties)
     * @returns {Object} The rule container object
     */
    getRuleElement: function() {
        var ruleObject = CustomBuilder.data.properties;
        var callback = CustomBuilder.config.builder.callbacks["getVisibilityRuleObject"];
        if (callback !== undefined && callback !== "") {
            ruleObject = CustomBuilder.callback(callback, []);
        }
        return ruleObject;
    },

    /**
     * Render all existing visibility rules
     */
    renderRules: function() {
        var ruleObj = this.getRuleElement();
        var rules = ruleObj["visibility_rules"];
        if (rules !== undefined) {
            for (var i = 0; i < rules.length; i++) {
                this.renderRule(rules[i]);
            }
        }
    },

    /**
     * Render a single visibility rule in the left pane
     * @param {Object} ruleData - The rule data object
     * @param {boolean} prepend - Whether to prepend (true) or append (false)
     * @returns {jQuery} The rendered rule element
     */
    renderRule: function(ruleData, prepend) {
        var self = this;
        var key = ruleData['visibility_key'];
        var name = ruleData['visibility_name'] || get_advtool_msg('adv.visibility.unnamed');

        // Get conditions summary
        var conditionsSummary = get_advtool_msg('adv.visibility.noConditions');
        if (ruleData['visibilityControl'] && ruleData['visibilityControl'] !== "") {
            var fields = ruleData['visibilityControl'].split(";").filter(function(f) {
                return f && f !== "(" && f !== ")";
            });
            if (fields.length > 0) {
                conditionsSummary = fields.length + " " + get_advtool_msg('adv.visibility.condition') + (fields.length > 1 ? "s" : "");
            }
        }

        var $rule = $(
            '<div class="visibility_rule">' +
                '<div class="sort"></div>' +
                '<div class="rule-content">' +
                    '<div class="name"></div>' +
                    '<div class="conditions_summary"></div>' +
                '</div>' +
                '<div class="rule-buttons">' +
                    '<a class="edit_rule btn"><i class="las la-edit"></i></a>' +
                    '<a class="delete_rule btn"><i class="la la-trash"></i></a>' +
                '</div>' +
            '</div>'
        );

        $rule.data("key", key);
        $rule.attr("id", "visibility-rule-" + key);
        $rule.data("data", { className: "visibility-rule", properties: ruleData });
        $rule.find(".name").text(name);
        $rule.find(".conditions_summary").text(conditionsSummary);

        // Add to sortable
        $rule.find(".name").addClass("visible");
        var $sortable = $(this.container).find(".visibility_rules .sortable");
        if (prepend) {
            $sortable.prepend($rule);
        } else {
            $sortable.append($rule);
        }

        // Make name inline editable
        $rule.find(".name").editable(function(value) {
            if (value === "") {
                value = get_advtool_msg('adv.visibility.unnamed');
            }
            ruleData['visibility_name'] = value;
            CustomBuilder.update();
            self.refreshAllTooltips();
            return value;
        }, {
            type: 'text',
            tooltip: '',
            select: true,
            style: 'inherit',
            cssclass: 'labelEditableField',
            onblur: 'submit',
            rows: 1,
            width: '80%',
            minwidth: 80,
            data: function(value) {
                return self.decodeHtml(value);
            }
        });

        return $rule;
    },

    /**
     * Add a new visibility rule
     */
    addRule: function() {
        this.hideEmptyState();
        var rule = {
            visibility_key: this.generateGuid(),
            visibility_name: get_advtool_msg('adv.visibility.unnamed'),
            visibilityControl: "",
            visibilityValue: "",
            regex: "",
            join: "",
            reverse: ""
        };

        var ruleObj = this.getRuleElement();
        if (ruleObj["visibility_rules"] === undefined) {
            ruleObj["visibility_rules"] = [];
        }
        ruleObj["visibility_rules"].unshift(rule);

        var $ruleElm = this.renderRule(rule, true);
        this.setActiveRule($ruleElm);
        CustomBuilder.update();
    },

    /**
     * Remove a visibility rule
     * @param {jQuery} $rule - The rule element to remove
     */
    removeRule: function($rule) {
        var ruleObj = this.getRuleElement();
        var key = $rule.data("key");
        var rules = ruleObj["visibility_rules"];

        // Find and remove the rule
        var index = this.findRuleIndex(rules, key);
        if (index > -1) {
            rules.splice(index, 1);
            this.removeElementsVisibility(key);

            var wasActive = $rule.hasClass("active");
            $rule.remove();

            if (wasActive) {
                var nextRule = $(this.container).find(".sortable .visibility_rule:eq(0)");
                if (nextRule.length > 0) {
                    this.setActiveRule(nextRule);
                } else {
                    this.renderEmptyState();
                }
            }
            this.refreshAllTooltips();
            CustomBuilder.update();
        }
    },

    /**
     * Edit a visibility rule (open property editor)
     * @param {jQuery} $rule - The rule element to edit
     */
    editRule: function($rule) {
        $("body").removeClass("no-right-panel");

        var data = $rule.data("data");
        CustomBuilder.Builder.selectedEl = $rule;
        CustomBuilder.editProperties("visibility-rule", data.properties, data, $rule);

        $("#style-properties-tab-link").hide();
        $("#right-panel #style-properties-tab").find(".property-editor-container").remove();
    },

    /**
     * Update rules order after drag and drop
     */
    updateRulesOrder: function() {
        var ruleObj = this.getRuleElement();
        var newRules = [];
        $(this.container).find(".visibility_rules .sortable .visibility_rule").each(function() {
            newRules.push($(this).data("data")["properties"]);
        });
        ruleObj["visibility_rules"] = newRules;
        CustomBuilder.update();
    },

    /**
     * Set a rule as active and refresh the elements table
     * @param {jQuery} $rule - The rule element to activate
     */
    setActiveRule: function($rule) {
        $(this.container).find(".visibility_rule").removeClass("active");
        $rule.addClass("active");

        var key = $rule.data("key");
        var $tbody = $(this.container).find('.elements_container table tbody');
        $tbody.html("");

        this.renderElement(CustomBuilder.data, $tbody, key);
        this.updateLastChildClasses($tbody);
    },

    /**
     * Update CSS classes for tree lines (last child indicators)
     * @param {jQuery} $container - The tbody container
     */
    updateLastChildClasses: function($container) {
        for (var i = 1; i < 9; i++) {
            $container.find(".level-" + i).each(function() {
                var last = $(this).nextUntil(".level-" + i, ".level-" + (i + 1)).last().addClass("last-" + i);
                $(last).nextUntil(".level-" + i).addClass("last-" + i);
            });
        }
    },

    /**
     * Render a form element row in the elements table
     * @param {Object} data - The element data
     * @param {jQuery} $container - The tbody container
     * @param {string} key - The active rule key
     * @param {number} level - The nesting level
     */
    renderElement: function(data, $container, key, level) {
        var builder = CustomBuilder.Builder;

        if (key === undefined) {
            key = $(".visibility_rules .visibility_rule.active").data("key");
        }
        if (level === undefined) {
            level = 0;
        }

        // Render element if not ignored
        if (data !== null && data !== undefined && !this.isIgnoreRendering(data)) {
            level = level + 1;
            var $row = this.createElementRow(level);
            $row.data("element", data);
            $container.append($row);

            // Add tree structure bars
            this.addTreeBars($row, level);
            this.markParentAsHasChild($row, level);

            // Render element info and apply button
            this.renderElementMeta($row.find(".element-meta > div"), data);
            var props = builder.parseElementProps(data);
            this.renderApplyButton($row, data, props, key);
        }

        // Recursively render child elements
        if (data.elements !== undefined && data.elements !== null && data.elements.length > 0) {
            var self = this;
            $.each(data.elements, function(i, child) {
                self.renderElement(child, $container, key, level);
            });
        }
    },

    /**
     * Create a table row for an element
     * @param {number} level - The nesting level
     * @returns {jQuery} The row element
     */
    createElementRow: function(level) {
        return $(
            '<tr class="level-' + level + '" data-level="' + level + '">' +
                '<td class="element-meta">' +
                    '<div>' +
                        '<div class="toggle">' +
                            '<span class="toggle-btn">' +
                                '<i class="las la-plus-square"></i>' +
                                '<i class="las la-minus-square"></i>' +
                            '</span>' +
                        '</div>' +
                    '</div>' +
                '</td>' +
            '</tr>'
        );
    },

    /**
     * Add tree structure bars to a row
     * @param {jQuery} $row - The row element
     * @param {number} level - The nesting level
     */
    addTreeBars: function($row, level) {
        if (level > 1) {
            var $toggle = $row.find('.toggle');
            for (var i = 1; i < level; i++) {
                $toggle.prepend('<span class="bar"></span>');
            }
        }
    },

    /**
     * Mark the previous row as having a child
     * @param {jQuery} $row - The current row
     * @param {number} level - The current level
     */
    markParentAsHasChild: function($row, level) {
        var $prev = $row.prev();
        if ($prev.length > 0 && $prev.hasClass("level-" + (level - 1))) {
            $prev.addClass("has-child");
        }
    },

    /**
     * Render element metadata (icon, label, id)
     * @param {jQuery} $container - The container for metadata
     * @param {Object} data - The element data
     */
    renderElementMeta: function($container, data) {
        var builder = CustomBuilder.Builder;
        var component = builder.parseDataToComponent(data);
        var props = builder.parseElementProps(data);

        if (component.builderTemplate !== undefined && component.builderTemplate.customPropertiesData) {
            props = component.builderTemplate.customPropertiesData(props, data, component);
        }

        var label = this.getElementLabel(component, props, data);

        $container.append('<span class="element-icon">' + component.icon + '</span>');
        $container.append('<span class="element-label">' + label + '</span>');

        if (props["id"] !== undefined && props["id"] !== "" && props["id"].length < 32) {
            $container.append('<span class="element-id">' + props["id"] + '</span>');
        }
    },

    /**
     * Get the display label for an element
     * @param {Object} component - The component definition
     * @param {Object} props - The element properties
     * @param {Object} data - The element data
     * @returns {string} The display label
     */
    getElementLabel: function(component, props, data) {
        var label = component.label;
        if (component.builderTemplate !== undefined && component.builderTemplate.getLabel) {
            label = component.builderTemplate.getLabel(data, component);
        }

        if (props.label !== undefined && props.label !== "") {
            label = props.label;
        } else if (props.textContent !== undefined && props.textContent !== "") {
            label = UI.escapeHTML(props.textContent);
            if (label.length > 30) {
                label = label.substring(0, 27) + "...";
            }
        }
        return label;
    },

    /**
     * Render the apply button for an element
     * @param {jQuery} $row - The table row
     * @param {Object} data - The element data
     * @param {Object} props - The element properties
     * @param {string} key - The active rule key
     */
    renderApplyButton: function($row, data, props, key) {
        var self = this;
        var level = $row.data("level");
        var isSection = data["className"] === "org.joget.apps.form.model.Section";
        var isColumns = data["className"] === "org.joget.apps.form.lib.Columns";
        var isColumnContainer = data["className"] === "org.joget.apps.form.lib.ColumnContainer";
        var isParentContainer = isSection || isColumns || isColumnContainer;

        // Check if element supports visibility
        if (!this.isElementSupportVisibility(data)) {
            $row.append('<td class="apply" width="30%"><div class="apply-btns">-</div></td>');
            return;
        }

        $row.append('<td class="apply" width="30%"><div class="apply-btns btn-group"></div></td>');

        if (props["visibility_rules"] === undefined) {
            props["visibility_rules"] = {};
        }

        var isApplied = this.isRuleApplied(props["visibility_rules"], key);
        var activeClass = isApplied ? "active" : "";

        // Parent containers (Sections, Columns) get data-disable-child attribute to control child elements
        var disableChildAttr = isParentContainer ? ' data-disable-child="true"' : '';

        $row.find(".apply-btns").append(
            '<button type="button" class="apply-btn btn btn-outline-primary btn-sm ' + activeClass + '"' + disableChildAttr + '>' +
                get_advtool_msg('adv.visibility.apply') +
            '</button>'
        );

        if (level > 1) {
            var shouldDisable = false;
            // Check all parent levels for active containers
            for (var parentLevel = level - 1; parentLevel > 0; parentLevel--) {
                var $parentRow = $row.prevAll(".level-" + parentLevel).first();
                if ($parentRow.length > 0) {
                    var $parentBtn = $parentRow.find(".apply-btns [data-disable-child]");
                    if ($parentBtn.length > 0 && ($parentBtn.hasClass("active") || $parentBtn.attr("disabled") === "disabled")) {
                        shouldDisable = true;
                        break;
                    }
                }
            }
            if (shouldDisable) {
                $row.find(".apply-btns .btn").attr("disabled", "disabled");
            }
        }

        // Show applied rules count indicator
        this.updateRulesCountIndicator($row, props["visibility_rules"]);

        // Handle apply button click
        $row.on("click", ".apply-btn", function(event) {
            if ($(this).attr("disabled") === "disabled") {
                event.preventDefault();
                return false;
            }

            $(this).toggleClass("active");
            var applied = $(this).hasClass("active");

            // Ensure visibility_rules exists
            if (props["visibility_rules"] === undefined) {
                props["visibility_rules"] = {};
            }
            // Toggle visibility_rules mapping
            if (applied) {
                props["visibility_rules"][key] = true;
            } else {
                delete props["visibility_rules"][key];
            }
            self.updateElementVisibilityProps(data, props);
            self.updateRulesCountIndicator($row, props["visibility_rules"]);
            self.cleanupEmptyVisibilityRules(data, props);

            // If this is a section, toggle child element buttons
            if ($(this).attr("data-disable-child") !== undefined) {
                var sectionLevel = $row.data("level");

                if (applied) {
                    // Disable all child rows and clear their applied states
                    $row.nextAll("tr").each(function() {
                        var $childRow = $(this);
                        var childLevel = $childRow.data("level");

                        if (childLevel <= sectionLevel) {
                            return false;
                        }

                        var $childBtn = $childRow.find(".apply-btns .btn");
                        $childBtn.attr("disabled", "disabled");

                        // If the child was applied, clear its state
                        if ($childBtn.hasClass("active")) {
                            $childBtn.removeClass("active");

                            // Clear the visibility rule from child element data
                            var childData = $childRow.data("element");
                            if (childData && childData.properties) {
                                var childProps = childData.properties;
                                if (childProps["visibility_rules"] && childProps["visibility_rules"][key]) {
                                    delete childProps["visibility_rules"][key];
                                    self.updateRulesCountIndicator($childRow, childProps["visibility_rules"]);
                                    self.cleanupEmptyVisibilityRules(childData, childProps);
                                }
                            }
                        }
                    });
                } else {
                    // Re-enable child rows
                    $row.nextAll("tr").each(function() {
                        var $childRow = $(this);
                        var childLevel = $childRow.data("level");
                        if (childLevel <= sectionLevel) {
                            return false;
                        }

                        var hasActiveParent = false;
                        $childRow.prevAll("tr").each(function() {
                            var $parentRow = $(this);
                            var parentLevel = $parentRow.data("level");

                            if (parentLevel <= sectionLevel) {
                                return false;
                            }

                            if (parentLevel < childLevel) {
                                var $parentBtn = $parentRow.find(".apply-btns [data-disable-child]");
                                if ($parentBtn.length > 0 && $parentBtn.hasClass("active")) {
                                    hasActiveParent = true;
                                    return false;
                                }
                            }
                        });

                        if (!hasActiveParent) {
                            $childRow.find(".apply-btns .btn").removeAttr("disabled");
                        }
                    });
                }
            }

            CustomBuilder.update();
            event.preventDefault();
            return false;
        });
    },

    /**
     * Update the rules indicator on a row - shows count with tooltip listing rule names
     * @param {jQuery} $row - The table row
     * @param {Object} visibilityRules - The visibility_rules object
     */
    updateRulesCountIndicator: function($row, visibilityRules) {
        var self = this;
        $row.find(".rules-indicator").remove();

        var appliedRules = this.getAppliedRuleNames(visibilityRules);
        if (appliedRules.length > 0) {
            var $indicator = $(
                '<span class="rules-indicator">' +
                    '<i class="las la-eye"></i> ' +
                    '<span class="rules-count">' + appliedRules.length + '</span>' +
                    '<div class="rules-tooltip">' +
                        '<div class="rules-tooltip-title">' + get_advtool_msg('adv.visibility.appliedRules') + '</div>' +
                        '<ul class="rules-tooltip-list"></ul>' +
                    '</div>' +
                '</span>'
            );

            // Populate tooltip list
            var $list = $indicator.find('.rules-tooltip-list');
            $.each(appliedRules, function(i, name) {
                $list.append('<li>' + name + '</li>');
            });

            $row.find(".element-meta > div").append($indicator);

            // Handle hover to keep tooltip open
            var hideTimeout;
            $indicator.on('mouseenter', function() {
                clearTimeout(hideTimeout);
                $(this).find('.rules-tooltip').addClass('visible');
            }).on('mouseleave', function() {
                var $tooltip = $(this).find('.rules-tooltip');
                hideTimeout = setTimeout(function() {
                    $tooltip.removeClass('visible');
                }, 100);
            });

            $indicator.find('.rules-tooltip').on('mouseenter', function() {
                clearTimeout(hideTimeout);
            }).on('mouseleave', function() {
                $(this).removeClass('visible');
            });
        }
    },

    /**
     * Get the names of applied rules from visibility_rules mapping
     * @param {Object} visibilityRules - The visibility_rules object
     * @returns {Array} Array of rule names
     */
    getAppliedRuleNames: function(visibilityRules) {
        var self = this;
        var ruleObj = this.getRuleElement();
        var formRules = ruleObj["visibility_rules"] || [];
        var appliedNames = [];

        if (visibilityRules) {
            $.each(visibilityRules, function(key, applied) {
                if (applied === true || applied === "true") {
                    // Find rule name by key
                    for (var i = 0; i < formRules.length; i++) {
                        if (formRules[i]["visibility_key"] === key) {
                            appliedNames.push(formRules[i]["visibility_name"] || get_advtool_msg('adv.visibility.unnamed'));
                            break;
                        }
                    }
                }
            });
        }

        return appliedNames;
    },

    /**
     * Refresh all tooltips in the elements table
     * Called after a rule is renamed to update tooltip content
     */
    refreshAllTooltips: function() {
        var self = this;
        var builder = CustomBuilder.Builder;

        $(this.container).find(".visibility-table tbody tr").each(function() {
            var $row = $(this);
            var data = $row.data("element");
            if (data !== null && data !== undefined) {
                var props = builder.parseElementProps(data);
                if (props["visibility_rules"]) {
                    self.updateRulesCountIndicator($row, props["visibility_rules"]);
                }
            }
        });
    },

    /**
     * Update element's visibility_rules mapping
     * @param {Object} data - The element data
     * @param {Object} props - The element properties
     */
    updateElementVisibilityProps: function(data, props) {
        if (data.properties === undefined) {
            data.properties = {};
        }
        data.properties["visibility_rules"] = props["visibility_rules"];
    },

    /**
     * Remove a visibility rule from all elements
     * @param {string} key - The rule key to remove
     */
    removeElementsVisibility: function(key) {
        var self = this;
        var builder = CustomBuilder.Builder;

        $(this.container).find(".visibility-table tbody tr").each(function() {
            var data = $(this).data("element");
            if (data !== null && data !== undefined) {
                var props = builder.parseElementProps(data);
                if (props["visibility_rules"] !== undefined && props["visibility_rules"][key] !== undefined) {
                    delete props["visibility_rules"][key];
                    self.updateElementVisibilityProps(data, props);
                }
            }
        });
    },

    /**
     * Check if an element should be ignored in rendering.
     * Uses configuration from CustomBuilder.config.advanced_tools.visibility like permission tool does.
     * @param {Object} element - The element to check
     * @returns {boolean} True if should be ignored
     */
    isIgnoreRendering: function(element) {
        // Check for custom callback first (like permission tool)
        var config = CustomBuilder.config.advanced_tools.visibility;
        if (config && config.check_ignore_rendering_callback !== undefined && config.check_ignore_rendering_callback !== "") {
            return CustomBuilder.callback(config.check_ignore_rendering_callback, [element]);
        }

        // Use configured ignore_classes or fall back to defaults
        var ignoreClasses = (config && config.ignore_classes) ? config.ignore_classes : [
            "org.joget.apps.form.model.Form",
            "org.joget.apps.form.model.Column"
        ];

        return element["className"] === null ||
               element["className"] === undefined ||
               $.inArray(element["className"], ignoreClasses) !== -1;
    },

    /**
     * Check if an element supports visibility rules.
     * Uses configuration from CustomBuilder.config.advanced_tools.visibility like permission tool does.
     * @param {Object} data - The element data
     * @returns {boolean} True if supports visibility
     */
    isElementSupportVisibility: function(data) {
        // Check for custom callback first
        var config = CustomBuilder.config.advanced_tools.visibility;
        if (config && config.check_element_support_visibility_callback !== undefined && config.check_element_support_visibility_callback !== "") {
            return CustomBuilder.callback(config.check_element_support_visibility_callback, [data]);
        }

        // Use configured element_support_visibility classes or default logic
        if (config && config.element_support_visibility) {
            return data["className"] !== undefined &&
                   $.inArray(data["className"], config.element_support_visibility) !== -1;
        }

        if (data["className"] === "org.joget.apps.form.model.Section" ||
            data["className"] === "org.joget.apps.form.lib.Columns" ||
            data["className"] === "org.joget.apps.form.lib.ColumnContainer") {
            return true;
        }

        // Get ignore classes from config or use defaults
        var ignoreClasses = (config && config.ignore_classes) ? config.ignore_classes : [
            "org.joget.apps.form.model.Form",
            "org.joget.apps.form.model.Column"
        ];

        // Other form elements with an id property support visibility (excluding ignored classes)
        if (data["className"] !== undefined &&
            $.inArray(data["className"], ignoreClasses) === -1 &&
            data["properties"] !== undefined &&
            data["properties"]["id"] !== undefined) {
            return true;
        }

        return false;
    },

    /**
     * Check if a rule is applied to an element
     * @param {Object} visibilityRules - The visibility_rules object
     * @param {string} key - The rule key to check
     * @returns {boolean} True if rule is applied
     */
    isRuleApplied: function(visibilityRules, key) {
        return visibilityRules[key] === true || visibilityRules[key] === "true";
    },

    /**
     * Count the number of applied rules
     * @param {Object} visibilityRules - The visibility_rules object
     * @returns {number} The count of applied rules
     */
    countAppliedRules: function(visibilityRules) {
        var self = this;
        return Object.keys(visibilityRules || {}).filter(function(k) {
            return self.isRuleApplied(visibilityRules, k);
        }).length;
    },

    /**
     * Find rule index by key
     * @param {Array} rules - The rules array
     * @param {string} key - The rule key to find
     * @returns {number} The index or -1 if not found
     */
    findRuleIndex: function(rules, key) {
        for (var i = 0; i < rules.length; i++) {
            if (rules[i]["visibility_key"] === key) {
                return i;
            }
        }
        return -1;
    },

    /**
     * Decode HTML entities
     * @param {string} value - The string to decode
     * @returns {string} The decoded string
     */
    decodeHtml: function(value) {
        if (value !== "") {
            var div = document.createElement('div');
            div.innerHTML = value;
            return div.firstChild ? div.firstChild.nodeValue : value;
        }
        return value;
    },

    /**
     * Generate a unique identifier
     * @returns {string} A GUID string
     */
    generateGuid: function() {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
    },

    /**
     * Clean up empty visibility_rules mappings from element and Form
     * @param {Object} data - The element data
     * @param {Object} props - The element properties
     */
    cleanupEmptyVisibilityRules: function(data, props) {
        // Clean element's visibility_rules if empty
        if (props["visibility_rules"] && Object.keys(props["visibility_rules"]).length === 0) {
            delete data.properties["visibility_rules"];
        }

        // Clean Form's visibility_rules array if empty
        var ruleObj = this.getRuleElement();
        if (ruleObj["visibility_rules"] && ruleObj["visibility_rules"].length === 0) {
            delete ruleObj["visibility_rules"];
        }
    },

    /**
     * Migrate existing visibility rules
     * Scans all Sections for visibilityControl and creates named rules in Form's visibility_rules
     */
    migrateLegacyRules: function() {
        var self = this;
        var ruleObj = this.getRuleElement();
        var migrated = false;

        // Scan all elements for legacy visibility
        this.scanElementsForMigration(CustomBuilder.data, ruleObj, function() {
            migrated = true;
        });

        if (migrated) {
            CustomBuilder.update();
        }
    },

    /**
     * Recursively scan elements for legacy visibility rules and migrate them
     * @param {Object} data - The element data
     * @param {Object} ruleObj - The rule container (Form properties)
     * @param {Function} onMigrate - Callback when migration occurs
     */
    scanElementsForMigration: function(data, ruleObj, onMigrate) {
        var self = this;
        if (data === null || data === undefined) {
            return;
        }
        // Check if this is a Section with legacy visibility
        if (data["className"] === "org.joget.apps.form.model.Section") {
            var props = data.properties || {};
            if (props["visibilityControl"] && props["visibilityControl"] !== "" &&
                !props["_visibility_migrated"]) {

                var ruleName = this.getLegacyRuleName(props);
                var ruleKey = this.generateGuid();

                var rule = {
                    visibility_key: ruleKey,
                    visibility_name: ruleName,
                    visibilityControl: props["visibilityControl"],
                    visibilityValue: props["visibilityValue"] || "",
                    regex: props["regex"] || "",
                    join: props["join"] || "",
                    reverse: props["reverse"] || ""
                };

                // Add to Form's visibility_rules
                if (ruleObj["visibility_rules"] === undefined) {
                    ruleObj["visibility_rules"] = [];
                }
                ruleObj["visibility_rules"].push(rule);

                if (props["visibility_rules"] === undefined) {
                    props["visibility_rules"] = {};
                }
                props["visibility_rules"][ruleKey] = true;

                // Clear legacy properties from Section
                delete props["visibilityControl"];
                delete props["visibilityValue"];
                delete props["regex"];
                delete props["join"];
                delete props["reverse"];

                // Mark as migrated
                props["_visibility_migrated"] = true;

                if (onMigrate) {
                    onMigrate();
                }
            }
        }

        // Recursively scan child elements
        if (data.elements !== undefined && data.elements !== null && data.elements.length > 0) {
            $.each(data.elements, function(i, child) {
                self.scanElementsForMigration(child, ruleObj, onMigrate);
            });
        }
    },

    /**
     * Generate a name for a legacy rule based on its conditions
     * @param {Object} props - The Section properties
     * @returns {string} The rule name
     */
    getLegacyRuleName: function(props) {
        var visibilityControl = props["visibilityControl"] || "";
        var fields = visibilityControl.split(";").filter(function(f) {
            return f && f !== "(" && f !== ")";
        });

        if (fields.length > 0) {
            // Use first field name as rule name
            var firstName = fields[0];
            if (fields.length > 1) {
                return firstName + " +" + (fields.length - 1) + " more";
            }
            return firstName;
        }
        return get_advtool_msg('adv.visibility.migratedRule');
    }
};