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
                '<div class="visibility-migration-notice-container"></div>' +
                '<div class="visibility_content">' +
                    '<div class="visibility_rules">' +
                        '<div class="buttons">' +
                            '<span class="rules-total-count"></span>' +
                            '<a class="add_visibility button"><i class="fas fa-plus"></i> ' + get_advtool_msg('adv.visibility.addRule') + '</a>' +
                        '</div>' +
                        '<div class="sortable"></div>' +
                    '</div>' +
                    '<div class="elements_container"></div>' +
                '</div>' +
            '</div>'
        );

        // Detect legacy section visibility rules without mutating the model, and show a
        // notice instead of migrating eagerly
        this.detectLegacyRules();
        this.renderMigrationNotice();
        this.renderRules();
        this.updateTotalRuleCount();
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
                            script_url: CustomBuilder.contextPath + '/web/json/app' + CustomBuilder.appPath + '/plugin/org.joget.apps.form.model.Section/service?version=' + CustomBuilder.buildNumber + '-visibility-summary-5'
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
            items: '.visibility_rule:not(.pending)',
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

        if (this.pendingLegacy && this.pendingLegacy.length > 0) {
            for (var j = 0; j < this.pendingLegacy.length; j++) {
                this.renderRule(this.pendingLegacy[j]);
            }
        }
    },

    /**
     * Update the total rule count display
     */
    updateTotalRuleCount: function() {
        var ruleObj = this.getRuleElement();
        var count = (ruleObj["visibility_rules"] && ruleObj["visibility_rules"].length) || 0;
        var $counter = $(this.container).find('.rules-total-count');
        if (count > 0) {
            $counter.text(count + ' ' + get_advtool_msg('adv.visibility.totalRules')).show();
        } else {
            $counter.hide();
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

        // un-migrated legacy card: flag it, add the "Pending migration" badge, make it non-draggable, and skip inline name editing
        if (ruleData['_pending']) {
            $rule.addClass("pending");
            $rule.find(".sort").remove();
            $rule.find(".rule-content").append(
                '<div class="pending-badge">' + get_advtool_msg('adv.visibility.pending') + '</div>'
            );
            return $rule;
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
     * Find a rendered rule card by its visibility key.
     *
     * Pending legacy rules are keyed with a deterministic section path suffix when the
     * full builder data path is available (for example "pending_section__path_0_1").
     * The Section summary can occasionally render from a cloned property-editor object
     * and only know the base key ("pending_section"). In that case, fall back to the
     * suffixed card only when there is exactly one match; duplicate/blank section ids
     * remain ambiguous and are intentionally left unresolved.
     *
     * @param {string} key The exact rule key, or a pending legacy base key.
     * @returns {jQuery} Matching rule card(s), or an empty set when no safe match exists.
     */
    findRuleCardByKey: function(key) {
        var $exact = this.container ? $(this.container).find(".visibility_rule").filter(function() {
            return $(this).data("key") === key;
        }) : $();
        if ($exact.length > 0 || !key || key.indexOf("pending_") !== 0) {
            return $exact;
        }

        var pendingPrefix = key + "__";
        var $pendingMatch = $(this.container).find(".visibility_rule").filter(function() {
            var cardKey = $(this).data("key");
            return typeof cardKey === "string" && cardKey.indexOf(pendingPrefix) === 0;
        });
        return $pendingMatch.length === 1 ? $pendingMatch : $();
    },

    /**
     * Add a new visibility rule
     */
    addRule: function() {
        if (this.pendingLegacy && this.pendingLegacy.length > 0) {
            this.doMigration();
            if (this.pendingLegacy && this.pendingLegacy.length > 0) {
                return;
            }
        }
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
        this.updateTotalRuleCount();
        CustomBuilder.update();
    },

    /**
     * Remove a visibility rule
     * @param {jQuery} $rule - The rule element to remove
     */
    removeRule: function($rule) {
        if ($rule.hasClass("pending")) {
            var selfRef = this;
            var pendingKey = $rule.data("key");
            this.doMigration(function(mapping) {
                var realKey = mapping ? mapping[pendingKey] : null;
                if (realKey) {
                    var $realCard = selfRef.findRuleCardByKey(realKey);
                    if ($realCard.length > 0) {
                        selfRef.removeRule($realCard);
                    }
                }
            });
            return;
        }

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
            this.updateTotalRuleCount();
            CustomBuilder.update();
        }
    },

    /**
     * Edit a visibility rule (open property editor)
     * @param {jQuery} $rule - The rule element to edit
     */
    editRule: function($rule) {
        if ($rule.hasClass("pending")) {
            var selfRef = this;
            var pendingKey = $rule.data("key");
            this.doMigration(function(mapping) {
                var realKey = mapping ? mapping[pendingKey] : null;
                if (realKey) {
                    var $realCard = selfRef.findRuleCardByKey(realKey);
                    if ($realCard.length > 0) {
                        selfRef.setActiveRule($realCard);
                        selfRef.editRule($realCard);
                    }
                }
            });
            return;
        }

        $("body").removeClass("no-right-panel");

        var data = $rule.data("data");
        CustomBuilder.Builder.selectedEl = $rule;
        CustomBuilder.editProperties("visibility-rule", data.properties, data, $rule);

        $("#style-properties-tab-link").hide();
        $("#right-panel #style-properties-tab").find(".property-editor-container").remove();
    },

    /**
     * Open the Visibility tool and optionally edit a specific rule.
     * @param {string} ruleKey - The rule key to focus, or null to only open the tool
     * @param {boolean} edit - true to open the rule property editor after focusing
     */
    openRule: function(ruleKey, edit) {
        var self = this;
        if (CustomBuilder.enableEnhancedTools) {
            CustomBuilder.enableEnhancedTools();
        }
        $("#visibility-btn").trigger("click");

        var attempts = 0;
        var focusRule = function() {
            attempts++;
            if (!ruleKey) {
                return;
            }

            var $ruleCard = self.findRuleCardByKey(ruleKey);
            if ($ruleCard.length === 0) {
                if (attempts < 20) {
                    setTimeout(focusRule, 100);
                } else if (CustomBuilder.showMessage) {
                    CustomBuilder.showMessage(get_advtool_msg('adv.visibility.ruleNotFound'));
                }
                return;
            }

            var $rulesPane = $(self.container).find(".visibility_rules");
            $rulesPane.animate({
                scrollTop: $rulesPane.scrollTop() + $ruleCard.position().top - $rulesPane.position().top
            }, 300);

            self.setActiveRule($ruleCard);
            if (edit) {
                self.editRule($ruleCard);
            }
        };

        setTimeout(focusRule, 100);
    },

    /**
     * Update rules order after drag and drop
     */
    updateRulesOrder: function() {
        var ruleObj = this.getRuleElement();
        var newRules = [];
        $(this.container).find(".visibility_rules .sortable .visibility_rule:not(.pending)").each(function() {
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

        var data = $rule.data("data");
        this.activePendingRule = (data && data.properties && data.properties['_pending']) ? data.properties : null;

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
        $('<span class="element-label"></span>').text(label).appendTo($container);

        if (props["id"] !== undefined && props["id"] !== "" && props["id"].length < 32) {
            $('<span class="element-id"></span>').text(props["id"]).appendTo($container);
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
            label = props.textContent;
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

        var visibilityRules = this.normalizeVisibilityAssignments(props["visibility_rules"]) || {};
        var isApplied = this.isRuleApplied(visibilityRules, key);
        if (this.activePendingRule && data === this.activePendingRule['_sectionRef']) {
            isApplied = true;
        }
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
        this.updateRulesCountIndicator($row, visibilityRules);

        // Handle apply button click
        $row.on("click", ".apply-btn", function(event, skipPendingMigration) {
            if ($(this).attr("disabled") === "disabled") {
                event.preventDefault();
                return false;
            }

            if (!skipPendingMigration && self.pendingLegacy && self.pendingLegacy.length > 0) {
                event.preventDefault();
                var desired = !$(this).hasClass("active"); // apply if it was not active
                var elementData = data;                    // live element ref (survives migration)
                var pendingKeyToMap = self.activePendingRule ? self.activePendingRule['visibility_key'] : null;
                self.doMigration(function(mapping) {
                    var realKey = pendingKeyToMap ? (mapping ? mapping[pendingKeyToMap] : null) : key;
                    if (!realKey) { return; }
                    var $realCard = self.findRuleCardByKey(realKey);
                    if ($realCard.length > 0) { self.setActiveRule($realCard); }
                    self.applyRuleToElementRow(elementData, desired, true);
                });
                return false;
            }

            var normalizedVisibilityRules = self.normalizeVisibilityAssignments(props["visibility_rules"]);
            if (normalizedVisibilityRules === null) {
                event.preventDefault();
                return false;
            }
            props["visibility_rules"] = normalizedVisibilityRules;
            $(this).toggleClass("active");
            var applied = $(this).hasClass("active");
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
     * @param {Object} elementData - The element data object to apply/unapply the rule on
     * @param {boolean} desired - true to apply, false to unapply
     * @param {boolean} skipPendingMigration - true after a migration attempt so
     *        malformed pending entries cannot recursively trigger migration again
     */
    applyRuleToElementRow: function(elementData, desired, skipPendingMigration) {
        var $tbody = $(this.container).find('.elements_container table tbody');
        var $targetRow = null;
        $tbody.find("tr").each(function() {
            if ($(this).data("element") === elementData) {
                $targetRow = $(this);
                return false;
            }
        });
        if ($targetRow === null) {
            return;
        }
        var $btn = $targetRow.find(".apply-btn");
        if ($btn.attr("disabled") === "disabled") {
            return; // e.g. a child of a Section that already has the rule applied
        }
        if ($btn.hasClass("active") !== desired) {
            // Reuse the normal toggle + parent/child cascade + CustomBuilder.update(),
            // while allowing the post-migration path to bypass migration exactly once.
            $btn.trigger("click", [skipPendingMigration === true]);
        }
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
                    '<i class="las la-list-ul"></i> ' +
                    '<span class="rules-count">' + appliedRules.length + '</span>' +
                    '<div class="rules-tooltip">' +
                        '<div class="rules-tooltip-title">' + get_advtool_msg('adv.visibility.appliedRules') + '</div>' +
                        '<ul class="rules-tooltip-list"></ul>' +
                    '</div>' +
                '</span>'
            );

            // Populate tooltip list
            var $list = $indicator.find('.rules-tooltip-list');
            $.each(appliedRules, function(i, rule) {
                $('<li></li>').attr('data-rule-key', rule.key).text(rule.name).appendTo($list);
            });

            // Handle click on tooltip rule items
            $list.on('click', 'li[data-rule-key]', function() {
                var ruleKey = $(this).data('rule-key');
                var $ruleCard = self.findRuleCardByKey(ruleKey);
                if ($ruleCard.length > 0) {
                    // Scroll the rules list to the rule card
                    var $rulesPane = $(self.container).find('.visibility_rules');
                    $rulesPane.animate({
                        scrollTop: $rulesPane.scrollTop() + $ruleCard.position().top - $rulesPane.position().top
                    }, 300);

                    // Set it as the active rule
                    self.setActiveRule($ruleCard);

                    // Open the property editor for the rule
                    self.editRule($ruleCard);
                }
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
                            appliedNames.push({
                                name: formRules[i]["visibility_name"] || get_advtool_msg('adv.visibility.unnamed'),
                                key: key
                            });
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
     * Normalize an element's applied-rule mapping without mutating its source.
     * Persisted forms can expose this property as either an object or a JSON string.
     *
     * @param {*} value Persisted visibility_rules value
     * @returns {Object|null} A cloned mapping, or null for an unsupported value
     */
    normalizeVisibilityAssignments: function(value) {
        if (value === undefined || value === null || value === "") {
            return {};
        }
        if (typeof value === "string") {
            try {
                value = JSON.parse(value);
            } catch (e) {
                return null;
            }
        }
        if (typeof value !== "object" || Array.isArray(value)) {
            return null;
        }
        return $.extend({}, value);
    },

    /**
     * Normalize the Form's named-rule definitions without mutating its source.
     *
     * @param {*} value Persisted visibility_rules value
     * @returns {Array|null} A cloned array, or null for an unsupported value
     */
    normalizeVisibilityRuleDefinitions: function(value) {
        if (value === undefined || value === null || value === "") {
            return [];
        }
        if (typeof value === "string") {
            try {
                value = JSON.parse(value);
            } catch (e) {
                return null;
            }
        }
        return Array.isArray(value) ? value.slice(0) : null;
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
     * scan the form for legacy Section visibility rules that have not
     * been migrated yet, populating this.pendingLegacy with synthetic display-only rule objects.
     */
    detectLegacyRules: function() {
        this.pendingLegacy = [];
        this.scanElementsForDetection(CustomBuilder.data, "");
    },

    /**
     * @param {Object} data - The element data node to scan (recurses into its children)
     */
    scanElementsForDetection: function(data, path) {
        var self = this;
        if (data === null || data === undefined) {
            return;
        }
        if (data["className"] === "org.joget.apps.form.model.Section") {
            var props = data.properties || {};
            if (props["visibilityControl"] && props["visibilityControl"] !== "" &&
                !props["_visibility_migrated"]) {
                var sectionId = props["id"] || "";
                var pendingKey = this.getPendingRuleKey(props, data, path);
                // Derive a deterministic pending key from the section id plus a stable section
                // discriminator so the Section property summary can open the matching pending card.
                // Must stay in sync with sectionVisibilityEditor.js getPendingRuleKey().
                this.pendingLegacy.push({
                    visibility_key: pendingKey,
                    visibility_name: (sectionId || get_advtool_msg('adv.visibility.unnamed')),
                    visibilityControl: props["visibilityControl"],
                    visibilityValue: props["visibilityValue"] || "",
                    regex: props["regex"] || "",
                    join: props["join"] || "",
                    reverse: props["reverse"] || "",
                    _pending: true,
                    _sectionRef: data
                });
            }
        }
        if (data.elements !== undefined && data.elements !== null && data.elements.length > 0) {
            $.each(data.elements, function(i, child) {
                self.scanElementsForDetection(child, path === "" ? i + "" : path + "_" + i);
            });
        }
    },

    /**
     * Build the pending legacy rule key shared with sectionVisibilityEditor.js.
     *
     * The key is deterministic so a Section summary can ask the Visibility tool
     * to open the matching pending card before migration. The discriminator
     * avoids collisions when sections have duplicate or empty ids.
     *
     * @param {Object} props Section properties.
     * @param {Object} data Section data.
     * @param {string} path Traversal path in CustomBuilder.data.
     * @returns {string} Pending rule key.
     */
    getPendingRuleKey: function(props, data, path) {
        var base = (props && props["id"]) ? props["id"] : "section";
        var discriminator = this.getSectionDiscriminator(data, path);
        return "pending_" + this.sanitizePendingKeyPart(base) + (discriminator ? "__" + discriminator : "");
    },

    /**
     * Prefer a stable builder metadata key when present, otherwise use the
     * traversal path collected during legacy-rule detection.
     *
     * @param {Object} data Section data.
     * @param {string} path Traversal path in CustomBuilder.data.
     * @returns {string} Sanitized discriminator, or an empty string.
     */
    getSectionDiscriminator: function(data, path) {
        var props = data && data.properties ? data.properties : {};
        var keys = ["elementUniqueKey", "uniqueKey", "_uuid", "uuid", "builderKey"];
        for (var i = 0; i < keys.length; i++) {
            if (props[keys[i]]) {
                return this.sanitizePendingKeyPart(keys[i] + "_" + props[keys[i]]);
            }
            if (data && data[keys[i]]) {
                return this.sanitizePendingKeyPart(keys[i] + "_" + data[keys[i]]);
            }
        }
        return path !== undefined && path !== null && path !== "" ? "path_" + this.sanitizePendingKeyPart(path) : "";
    },

    sanitizePendingKeyPart: function(value) {
        value = (value === undefined || value === null) ? "" : value + "";
        return value.replace(/[^A-Za-z0-9_-]/g, "_");
    },

    /**
     * Render (or clear) the inline banner telling the user that N legacy rules exist and will
     * be converted on their first change or via the "Migrate now" button. No-op when nothing
     * is pending.
     */
    renderMigrationNotice: function() {
        var self = this;
        var $noticeContainer = $(this.container).find('.visibility-migration-notice-container');
        $noticeContainer.empty();

        if (!this.pendingLegacy || this.pendingLegacy.length === 0) {
            return;
        }

        var count = this.pendingLegacy.length;
        var $notice = $(
            '<div class="alert alert-warning visibility-migration-notice">' +
                '<i class="las la-info-circle"></i>' +
                '<span class="notice-text">' + get_advtool_msg('adv.visibility.migrationNotice', [count]) + '</span>' +
                '<a class="migrate-now button">' + get_advtool_msg('adv.visibility.migrateNow') + '</a>' +
            '</div>'
        );
        $notice.find('.migrate-now').on('click', function() {
            self.doMigration();
        });
        $noticeContainer.append($notice);
    },

    /**
     * Do the deferred legacy migration: reuse migrateLegacyRules() to do the real model
     * mutation (and CustomBuilder.update()), then clear the pending/preview state and re-render.
     * @param {Function} [onDone] - Called with a {pendingKey: realKey} map (or null if nothing
     *        was pending), letting callers act on the rule the pending card became.
     */
    doMigration: function(onDone) {
        if (!this.pendingLegacy || this.pendingLegacy.length === 0) {
            if (onDone) { onDone(null); }
            return;
        }
        var pending = this.pendingLegacy;
        var priorKeys = [];
        for (var i = 0; i < pending.length; i++) {
            var beforeProps = pending[i]._sectionRef.properties || {};
            var beforeVr = this.normalizeVisibilityAssignments(beforeProps["visibility_rules"]) || {};
            priorKeys.push(Object.keys(beforeVr));
        }

        // Perform the real migration (mutates CustomBuilder.data + calls CustomBuilder.update()).
        var migratedCount = this.migrateLegacyRules();

        // Resolve pending -> real key: the newly added key on each origin Section (after - before).
        var mapping = {};
        for (var j = 0; j < pending.length; j++) {
            var afterProps = pending[j]._sectionRef.properties || {};
            var afterVr = this.normalizeVisibilityAssignments(afterProps["visibility_rules"]) || {};
            var before = priorKeys[j];
            var added = Object.keys(afterVr).filter(function(k) {
                return before.indexOf(k) === -1;
            });
            mapping[pending[j].visibility_key] = added.length > 0 ? added[0] : null;
        }

        // Re-scan so entries that could not be migrated retain their warning and
        // legacy data instead of being reported as complete.
        this.detectLegacyRules();
        this.activePendingRule = null;
        this.renderMigrationNotice();

        $(this.container).find(".visibility_rules .sortable").empty();
        this.renderRules();
        this.updateTotalRuleCount();

        var firstRule = $(this.container).find(".sortable .visibility_rule:eq(0)");
        if (firstRule.length > 0) {
            this.hideEmptyState();
            this.setActiveRule(firstRule);
        } else {
            this.renderEmptyState();
        }

        if (migratedCount > 0) {
            CustomBuilder.showMessage(get_advtool_msg('adv.visibility.migrationDone'));
            setTimeout(function() {
                CustomBuilder.showMessage("");
            }, 3500);
        }

        if (onDone) { onDone(mapping); }
    },

    /**
     * Migrate existing visibility rules
     * Scans all Sections for visibilityControl and creates named rules in Form's visibility_rules
     */
    migrateLegacyRules: function() {
        var self = this;
        var ruleObj = this.getRuleElement();
        var migratedCount = 0;

        // Scan all elements for legacy visibility
        this.scanElementsForMigration(CustomBuilder.data, ruleObj, function() {
            migratedCount++;
        });

        if (migratedCount > 0) {
            CustomBuilder.update();
        }
        return migratedCount;
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

                var formRules = this.normalizeVisibilityRuleDefinitions(ruleObj["visibility_rules"]);
                var elementRules = this.normalizeVisibilityAssignments(props["visibility_rules"]);

                // Preserve malformed persisted values for manual recovery. Migration
                // is only committed after both containers have been normalized.
                if (formRules !== null && elementRules !== null) {
                    var ruleName = this.getLegacyRuleName(props, data);
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

                    formRules.push(rule);
                    elementRules[ruleKey] = true;
                    ruleObj["visibility_rules"] = formRules;
                    props["visibility_rules"] = elementRules;

                    // Clear legacy properties from Section only after both new
                    // containers are ready to commit.
                    delete props["visibilityControl"];
                    delete props["visibilityValue"];
                    delete props["regex"];
                    delete props["join"];
                    delete props["reverse"];
                    props["_visibility_migrated"] = true;

                    if (onMigrate) {
                        onMigrate();
                    }
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
     * Generate a name for a legacy rule based on its section
     * @param {Object} props - The Section properties
     * @param {Object} data - The Section element data
     * @returns {string} The rule name
     */
    getLegacyRuleName: function(props, data) {
        var sectionId = "";
        if (data && data.properties) {
            sectionId = data.properties["id"] || "";
        }

        if (sectionId) {
            return sectionId + " [migrated]";
        }
        return get_advtool_msg('adv.visibility.migratedRule');
    }
};
