{
    isSectionVisibilitySummary: function() {
        return this.properties !== undefined && this.properties.name === "sectionVisibilitySummary";
    },
    addOnValidation: function(data, errors, checkEncryption) {
        if (this.isSectionVisibilitySummary()) {
            return;
        }
        var wrapper = $('#' + this.id + '_input');

        var hasError = false;
        $(wrapper).find(".error").removeClass("error");
        $(wrapper).find(".required").each(function(){
            if ($(this).val() === "") {
                $(this).addClass("error");
                hasError = true;
            }
        });
        if (hasError) {
            var obj = new Object();
            obj.field = this.properties.name;
            obj.fieldName = this.properties.label;
            obj.message = this.options.mandatoryMessage;
            errors.push(obj);
            $(wrapper).append('<div class="property-input-error">' + obj.message + '</div>');
        }
    },
    getData: function(useDefault) {
        if (this.isSectionVisibilitySummary()) {
            return {};
        }
        var field = this;
        var data = new Object();

        if (this.isDataReady) {
            var joins = [];
            var reverses = [];
            var visibilityControls = [];
            var visibilityValues = [];
            var regexs = [];
            
            var recursiveGetData = function(container, isGroup) {
                if (isGroup) { //open parentheses
                    joins.push($(container).find('> .buttons > .andOr').val());
                    reverses.push(($(container).find('> .buttons > .revert').hasClass("checked"))?"true":"");
                    visibilityControls.push("(");
                    visibilityValues.push("");
                    regexs.push("");
                }
                
                $(container).find("> .conditions-container > .perow").each(function(i, rowContainer){
                    if ($(rowContainer).hasClass("group")) {
                        recursiveGetData(rowContainer, true);
                    } else {
                        joins.push($(rowContainer).find('.andOr').val());
                        reverses.push(($(rowContainer).find('.revert').hasClass("checked"))?"true":"");
                        visibilityControls.push($(rowContainer).find('input.condition').val());
                        visibilityValues.push(field.replaceAllString($(rowContainer).find('input.visibility_value').val(), ";", "__"));
                        regexs.push($(rowContainer).find('select.operation').val());
                    }
                });
                
                if (isGroup) { //close parentheses
                    joins.push("");
                    reverses.push("");
                    visibilityControls.push(")");
                    visibilityValues.push("");
                    regexs.push("");
                }
            };
            
            recursiveGetData($("#" + field.id + "_input .visibilitywrapper"), false);
            
            data['join'] = joins.join(";");
            data['reverse'] = reverses.join(";");
            data['visibilityControl'] = visibilityControls.join(";");
            data['visibilityValue'] = visibilityValues.join(";");
            data['regex'] = regexs.join(";");
        }
        return data;
    },
    renderField : function() {
        if (this.isSectionVisibilitySummary()) {
            return this.renderSectionVisibilitySummaryField();
        }
        var thisObj = this;
        thisObj.isDataReady = false;
        
        var css = '.visibilitywrapper .pewrapper > .buttons > .andOr {float: left; margin-left: 5px; margin-top: 0 !important; width:auto !important;}';
        css += '.visibilitywrapper > .conditions-container {padding-bottom:15px;}';
        css += '.visibilitywrapper .conditions-container {padding-left:15px; padding-top:5px; overflow:hidden; min-height:20px;}';
        css += '.visibilitywrapper .buttons:after{content:""; display:block; clear:both;}';
        css += '.visibilitywrapper .buttons .sort{cursor:move;}';
        css += '.visibilitywrapper table{width:100%;}';
        css += '.visibilitywrapper .rulerow.pewrapper{background:var(--theme-primary-color-2, #edf2f5); border-color:var(--theme-border-color-2, #9aafbb); padding:2px 2px 2px 5px;}';
        css += '.visibilitywrapper .revert{width: 15px; color:#ccc; cursor:pointer;}';
        css += '.visibilitywrapper .revert.checked{width: 15px; color:red;}';
        css += '.visibilitywrapper a.revert{position: absolute; left: -3px; top: 11px;}';
        css += '.visibilitywrapper .col_input.small {flex-grow: inherit; min-width: 70px; width: 70px !important; max-width: inherit;}';
        css += '.visibilitywrapper .col_input span.label {transform: none; bottom: 40px; font-size:80%;}';
        css += '.visibilitywrapper .col_inputs{display: flex;flex-wrap: wrap;}';
        css += '.visibilitywrapper .col_input{flex-basis: 0;flex-grow: 1;max-width: 100%;position: relative;width: 100%;min-width:100px;}';
        css += '.visibilitywrapper .col_input input, .visibilitywrapper .col_input select{width: 98%; margin-top: 8px !important; margin-bottom: 8px !important;}';
        css += '.visibilitywrapper .conditions-container > .rulerow:first-child > .buttons > .andOr, .visibilitywrapper .conditions-container > .rulerow:first-child > table .andOr {opacity:0.3; pointer-events:none;}';
        css += '.visibilitywrapper .alignright{width: 27px; text-align:left;}';
        css += 'body.rtl .visibilitywrapper .alignright{text-align:right;}';
        css += '.visibilitywrapper .perow.condition .buttons{position:absolute; top:50%; right:5px; transform: translateY(-50%); z-index:5;}';
        css += 'body.rtl .visibilitywrapper .perow.condition .buttons{right: unset; left:5px;}';

        var html = '<div name="'+thisObj.id+'" class="visibilitywrapper pewrapper"><div class="buttons"><a class="addcondition"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addCondition@@</a><a class="addgroup"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addGroup@@</a></div><div class="conditions-container"></div></div>';

        return '<style>'+ css + '</style>' + html;
    },
    initScripting : function() {
        if (this.isSectionVisibilitySummary()) {
            this.initSectionVisibilitySummaryScripting();
            return;
        }
        var thisObj = this;
        
        thisObj.loadValues();
        
        $("#" + thisObj.id + "_input").on("click", ".addcondition", function(){
            thisObj.addCondition($(this).closest('.pewrapper'));
        });
        
        $("#" + thisObj.id + "_input").on("click", ".addgroup", function(){
            thisObj.addGroup($(this).closest('.pewrapper'));
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deletecondition", function(){
            thisObj.deleteCondition(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".deletegroup", function(){
            thisObj.deleteGroup(this);
        });
        
        $("#" + thisObj.id + "_input").on("click", ".revert", function(){
            $(this).toggleClass("checked");
        });
        
        thisObj.sortable($("#" + thisObj.id + "_input .visibilitywrapper > .conditions-container"));
    },
    loadValues : function() {
        var thisObj = this;
        
        var values = new Array();
        if (thisObj.options.propertyValues !== undefined && thisObj.options.propertyValues !== null && thisObj.options.propertyValues["visibilityControl"] !== undefined) {
            var joins = (thisObj.options.propertyValues["join"])?thisObj.options.propertyValues["join"].split(";"):[];
            var reverses = (thisObj.options.propertyValues["reverse"])?thisObj.options.propertyValues["reverse"].split(";"):[];
            var visibilityControls = (thisObj.options.propertyValues["visibilityControl"])?thisObj.options.propertyValues["visibilityControl"].split(";"):[];
            var visibilityValues = (thisObj.options.propertyValues["visibilityValue"])?thisObj.options.propertyValues["visibilityValue"].split(";"):[];
            var regexs = (thisObj.options.propertyValues["regex"])?thisObj.options.propertyValues["regex"].split(";"):[];
            
            for (var i in visibilityControls) {
                values.push({
                    join : (joins.length > i?joins[i]:""),
                    reverse : (reverses.length > i?reverses[i]:""),
                    visibilityControl : visibilityControls[i],
                    visibilityValue : (visibilityValues.length > i?visibilityValues[i]:""),
                    regex : (regexs.length > i?regexs[i]:"")
                });
            }
        }
        
        //render value
        if (values.length > 0) {
            var container = $("#" + thisObj.id + "_input > .pewrapper");
            $.each(values, function(i, row) {
                if (row.visibilityControl === "(") {
                    //add group
                    container = thisObj.loadGroup(container, row);
                } else if (row.visibilityControl === ")") {
                    //back to parent group
                    container = $(container).parent().closest(".pewrapper");
                } else {
                    //add condition
                    thisObj.loadCondition(container, row);
                }
            });
        }
        
        thisObj.isDataReady = true;
    },
    loadCondition : function(container, data) {
        var thisObj = this;
        var rowContainer = thisObj.addCondition(container);
        
        if (data["reverse"] === "true") {
            $(rowContainer).find(".revert").addClass("checked");
        }
        if (data["join"] === "or") {
            $(rowContainer).find(".andOr").val(data["join"]);
        }
        $(rowContainer).find("input.condition").val(data["visibilityControl"]);
        $(rowContainer).find("select.operation").val(data["regex"]);
        $(rowContainer).find("input.visibility_value").val(this.replaceAllString(data["visibilityValue"], "__", ";"));
        
        return rowContainer;
    },
    addCondition : function(container) {
        var thisObj = this;
        var container = $(container).find("> .conditions-container");
        var rowContainer = $('<div class="rulerow perow condition"><div class="buttons"><a class="sort" title="@@app.rulesdecision.sort@@"><i class="fas fa-ellipsis-v"></i><i class="fas fa-ellipsis-v"></i></a></div><table><tr><td class="revert"><i class="fas fa-exclamation" title="@@app.rulesdecision.revert@@"></i></td><td><div class="col_inputs"><div class="col_input small"><select class="andOr"><option value="">@@app.rulesdecision.and@@</option><option value="or">@@app.rulesdecision.or@@</option></select></div><div class="col_input"><input class="condition autocomplete required" placeholder="@@form.section.fieldIdControl@@"/><span class="label">@@form.section.fieldIdControl@@</span></div><div class="col_input"><select class="operation"><option value="">@@pbuilder.label.equalTo@@</option><option value=">">@@pbuilder.label.greaterThan@@</option><option value=">=">@@pbuilder.label.greaterThanOrEqualTo@@</option><option value="<">@@pbuilder.label.lessThan@@</option><option value="<=">@@pbuilder.label.lessThanOrEqualTo@@</option><option value="isTrue">@@pbuilder.label.isTrue@@</option><option value="isFalse">@@pbuilder.label.isFalse@@</option><option value="contains">@@app.rulesdecision.contains@@</option><option value="listContains">@@app.rulesdecision.listContains@@</option><option value="in">@@app.rulesdecision.in@@</option><option value="true">@@app.rulesdecision.regex@@</option></select><span class="label">@@app.rulesdecision.operation@@</span></div><div class="col_input"><input class="visibility_value" placeholder="@@form.section.fieldValueControl@@"/><span class="label">@@form.section.fieldValueControl@@</span></div></div></td><td class="alignright"><a class="deletecondition" title="@@app.rulesdecision.deleteCondition@@"><i class="fas fa-trash-alt"></i></a></td></tr></table></div>');
        container.append(rowContainer);
        
        $(rowContainer).find(".autocomplete").autocomplete({
            source:function (request, response) {
                var sources = [];
                var options = window['FormBuilder']['getFieldOptions']();
                for (var i in options) {
                    sources.push(options[i].value);
                }
                response(sources);
            },
            minLength: 0,
            open: function() {
                $(this).autocomplete('widget').css('z-index', 99999);
                return false;
            }
        });
        return rowContainer;
    },
    deleteCondition : function(button) {
        var thisObj = this;
        var container = $(button).closest(".rulerow").remove();
    },
    loadGroup : function(container, data) {
        var thisObj = this;
        var groupContainer = thisObj.addGroup(container);
        
        if (data["reverse"] === "true") {
            $(groupContainer).find("> .buttons > .revert").addClass("checked");
        }
        if (data["join"] === "or") {
            $(groupContainer).find("> .buttons > .andOr").val("or");
        }
        $(groupContainer).find("input.condition").val(data["visibilityControl"]);
        
        return groupContainer;
    },
    addGroup : function(container) {
        var thisObj = this;
        var container = $(container).find("> .conditions-container");
        var group = $('<div class="rulerow perow rulewrapper pewrapper group"><div class="buttons"><a class="revert"><i class="fas fa-exclamation" title="@@app.rulesdecision.revert@@"></i></a>&nbsp;&nbsp;<select class="andOr"><option value="">@@app.rulesdecision.and@@</option><option value="or">@@app.rulesdecision.or@@</option></select> <a class="addcondition"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addCondition@@</a>&nbsp;&nbsp;<a class="addgroup"><i class="fas fa-plus-circle"></i> @@app.rulesdecision.addGroup@@</a>&nbsp;&nbsp;<a class="deletegroup" title="@@app.rulesdecision.deleteGroup@@"><i class="fas fa-trash-alt"></i></a>&nbsp;&nbsp;<a class="sort" title="@@app.rulesdecision.sort@@"><i class="fas fa-ellipsis-v"></i><i class="fas fa-ellipsis-v"></i></a></div><div class="conditions-container"></div></div>');
        $(container).append(group);
        
        thisObj.sortable($(group).find("> .conditions-container"));
        
        return group;
    },
    deleteGroup : function(button) {
        var thisObj = this;
        var container = $(button).closest(".rulewrapper.group").remove();
    },
    sortable : function(sortableContainer) {
        $(sortableContainer).sortable({
            opacity: 0.8,
            items: '> .perow',
            axis: 'y',
            handle: '.sort',
            tolerance: 'intersect',
            connectWith: '.conditions-container'
        });
    },
    renderSectionVisibilitySummaryField : function() {
        var thisObj = this;
        var rules = thisObj.getSectionVisibilityRules();
        var css = '.section-visibility-summary{--visibility-surface:var(--theme-primary-color-1,#fff);--visibility-subtle:var(--theme-primary-color-3,#f7f8fa);--visibility-border:var(--theme-border-color-1,#dfe3e8);--visibility-text:var(--theme-label-color-2,#252f4a);--visibility-muted:var(--theme-input-color-1,#606874);--visibility-accent:var(--theme-active-color-2,#1677ff);--visibility-danger:var(--theme-danger-color,#dc3545);container-name:visibility-summary;container-type:inline-size;display:block;width:100%;box-sizing:border-box;color:var(--visibility-text);}';
        css += '.section-visibility-summary *{box-sizing:border-box;}';
        css += '.section-visibility-summary .visibility-overview{display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:5px 12px;margin-bottom:10px;padding:0 2px;color:var(--visibility-muted);font-size:12px;line-height:1.4;}';
        css += '.section-visibility-summary button{font:inherit;}';
        css += '.section-visibility-summary .visibility-action{display:inline-flex;align-items:center;gap:5px;padding:2px 0;border:0;background:transparent;color:var(--visibility-accent);font-size:12px;font-weight:600;line-height:1.35;white-space:nowrap;cursor:pointer;}';
        css += '.section-visibility-summary .visibility-action:hover{text-decoration:underline;}';
        css += '.section-visibility-summary .visibility-action:focus-visible{outline:2px solid var(--visibility-accent);outline-offset:2px;border-radius:2px;}';
        css += '.section-visibility-summary .visibility-message{display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:8px 12px;padding:11px 12px;border:1px solid var(--visibility-border);background:var(--visibility-subtle);color:var(--visibility-muted);border-radius:4px;line-height:1.4;}';
        css += '.section-visibility-summary .visibility-rule-card{border:1px solid var(--visibility-border);border-radius:4px;margin-bottom:8px;background:var(--visibility-surface);overflow:hidden;}';
        css += '.section-visibility-summary .visibility-rule-card:last-child{margin-bottom:0;}';
        css += '.section-visibility-summary .visibility-rule-head{display:grid;grid-template-columns:minmax(0,1fr) auto;gap:8px;align-items:center;padding:9px 10px;background:var(--visibility-subtle);border-bottom:1px solid var(--visibility-border);}';
        css += '.section-visibility-summary .visibility-rule-title-row{display:flex;align-items:center;gap:6px;min-width:0;flex-wrap:wrap;}';
        css += '.section-visibility-summary .visibility-rule-title{min-width:0;max-width:100%;font-weight:600;line-height:1.35;overflow-wrap:anywhere;}';
        css += '.section-visibility-summary .visibility-rule-meta{display:flex;align-items:center;gap:5px;margin-top:3px;font-size:11px;line-height:1.35;color:var(--visibility-muted);}';
        css += '.section-visibility-summary .visibility-rule-badge{display:inline-flex;align-items:center;flex-shrink:0;font-size:9px;line-height:1.4;text-transform:uppercase;color:#7a5800;background:#fff3cd;border:1px solid #e6cb76;border-radius:3px;padding:1px 5px;}';
        css += 'body[builder-theme="dark"] .section-visibility-summary .visibility-rule-badge{color:#ffe69c;background:#4a3a08;border-color:#705c1b;}';
        css += '.section-visibility-summary .visibility-rule-actions{align-self:start;padding-top:1px;}';
        css += '.section-visibility-summary .visibility-rule-body{padding:7px 10px;background:var(--visibility-surface);}';
        css += '.section-visibility-summary .visibility-condition-list{display:grid;grid-template-columns:minmax(72px,max-content) minmax(0,1fr);column-gap:10px;margin:0;padding:0;list-style:none;}';
        css += '.section-visibility-summary .visibility-condition{display:contents;line-height:1.4;overflow-wrap:anywhere;}';
        css += '.section-visibility-summary .visibility-condition-field{min-width:0;padding:3px 0;font-weight:600;color:var(--visibility-text);}';
        css += '.section-visibility-summary .visibility-condition-expression{display:flex;align-items:baseline;flex-wrap:wrap;gap:2px 5px;min-width:0;padding:3px 0;}';
        css += '.section-visibility-summary .visibility-condition-operation{color:var(--visibility-muted);}';
        css += '.section-visibility-summary .visibility-condition-value{color:var(--visibility-text);font-weight:600;}';
        css += '.section-visibility-summary .visibility-condition-join{display:flex;grid-column:1/-1;align-items:center;gap:8px;margin:2px 0;color:var(--visibility-muted);font-size:9px;font-weight:600;line-height:1;text-transform:uppercase;}';
        css += '.section-visibility-summary .visibility-condition-join:before,.section-visibility-summary .visibility-condition-join:after{content:"";height:1px;background:var(--visibility-border);flex:1;}';
        css += '.section-visibility-summary .visibility-condition-reverse{margin-left:5px;color:var(--visibility-danger);font-size:10px;font-weight:600;text-transform:uppercase;}';
        css += '.section-visibility-summary .visibility-condition-empty{padding:2px 0;color:var(--visibility-muted);font-style:italic;}';
        css += '.section-visibility-summary .visibility-condition-group-box{grid-column:1/-1;margin:3px 0;padding:7px 9px;border:1px solid var(--visibility-border);border-left:3px solid var(--visibility-accent);border-radius:4px;background:var(--visibility-subtle);}';
        css += '.section-visibility-summary .visibility-condition-group-box > .visibility-condition-reverse{display:block;margin:0 0 4px;}';
        css += '.section-visibility-summary .visibility-condition-group-box .visibility-condition-group-box{background:var(--visibility-surface);}';
        css += 'body.rtl .section-visibility-summary .visibility-condition-group-box{border-right:3px solid var(--visibility-accent);border-left:1px solid var(--visibility-border);}';
        css += 'body.rtl .section-visibility-summary .visibility-condition-reverse{margin-right:5px;margin-left:0;}';
        css += '@container visibility-summary (max-width:260px){.section-visibility-summary .visibility-rule-head{grid-template-columns:1fr;}.section-visibility-summary .visibility-rule-actions{padding-top:0;}.section-visibility-summary .visibility-message{align-items:flex-start;flex-direction:column;}.section-visibility-summary .visibility-condition-list{grid-template-columns:1fr;}.section-visibility-summary .visibility-condition-join{grid-column:1;}}';

        var html = '<style>' + css + '</style><div name="' + thisObj.id + '" class="section-visibility-summary">';
        if (rules.length === 0) {
            html += '<div class="visibility-message"><span>' + thisObj.escapeHtml('@@form.section.visibilityNoRules@@') + '</span>';
            html += '<button type="button" class="visibility-action section-visibility-open-tool"><i class="la la-eye" aria-hidden="true"></i> ' + thisObj.escapeHtml('@@form.section.visibilityOpenTool@@') + '</button></div>';
        } else {
            html += '<div class="visibility-overview"><span>' + thisObj.escapeHtml(thisObj.getVisibilityRuleCountText(rules.length)) + '</span>';
            html += '<button type="button" class="visibility-action section-visibility-open-tool"><i class="la la-eye" aria-hidden="true"></i> ' + thisObj.escapeHtml('@@form.section.visibilityManageRules@@') + '</button></div>';
            $.each(rules, function(i, rule) {
                var affectedText = thisObj.getVisibilityRuleUsageText(rule.affectedCount);
                html += '<div class="visibility-rule-card" data-rule-key="' + thisObj.escapeHtml(rule.visibility_key) + '">';
                html += '<div class="visibility-rule-head">';
                html += '<div><div class="visibility-rule-title-row"><span class="visibility-rule-title">' + thisObj.escapeHtml(rule.visibility_name) + '</span>';
                if (rule._pending) {
                    html += '<span class="visibility-rule-badge">' + thisObj.escapeHtml('@@form.section.visibilityPendingMigration@@') + '</span>';
                }
                html += '</div><div class="visibility-rule-meta"><i class="las la-link" aria-hidden="true"></i> ' + thisObj.escapeHtml(affectedText) + '</div></div>';
                html += '<div class="visibility-rule-actions"><button type="button" class="visibility-action section-visibility-open-rule" data-rule-key="' + thisObj.escapeHtml(rule.visibility_key) + '"><i class="las la-edit" aria-hidden="true"></i> ' + thisObj.escapeHtml('@@form.section.visibilityEditRule@@') + '</button></div>';
                html += '</div><div class="visibility-rule-body">';
                html += thisObj.renderSectionVisibilityRuleDetails(rule);
                html += '</div></div>';
            });
        }
        html += '</div>';
        return html;
    },
    getVisibilityRuleCountText : function(count) {
        if (count === 1) {
            return '@@form.section.visibilityRuleCountOne@@';
        }
        return '@@form.section.visibilityRuleCountMany@@'.replace('{0}', count);
    },
    getVisibilityRuleUsageText : function(count) {
        if (count === 1) {
            return '@@form.section.visibilityUsedByOne@@';
        }
        return '@@form.section.visibilityUsedByMany@@'.replace('{0}', count);
    },
    initSectionVisibilitySummaryScripting : function() {
        var thisObj = this;
        var wrapper = $("#" + thisObj.id + "_input");
        $(wrapper).off("click.sectionVisibilitySummary", ".section-visibility-open-rule");
        $(wrapper).on("click.sectionVisibilitySummary", ".section-visibility-open-rule", function() {
            thisObj.openSectionVisibilityRule($(this).data("rule-key"));
            return false;
        });
        $(wrapper).off("click.sectionVisibilitySummary", ".section-visibility-open-tool");
        $(wrapper).on("click.sectionVisibilitySummary", ".section-visibility-open-tool", function() {
            thisObj.openVisibilityRule(null, false);
            return false;
        });
    },
    getSectionVisibilityRules : function() {
        var thisObj = this;
        var sectionData = thisObj.getCurrentSectionData();
        var props = (sectionData && sectionData.properties) ? sectionData.properties : (thisObj.options.propertyValues || {});
        var rules = [];

        if (props["visibilityControl"] !== undefined && props["visibilityControl"] !== "" &&
                !props["_visibility_migrated"]) {
            var pendingKey = thisObj.getPendingRuleKey(props, sectionData, thisObj.getSectionPath(sectionData));
            rules.push({
                visibility_key: pendingKey,
                visibility_name: props["id"] || '@@form.section.visibilityLegacyRule@@',
                visibilityControl: props["visibilityControl"],
                visibilityValue: props["visibilityValue"] || "",
                regex: props["regex"] || "",
                join: props["join"] || "",
                reverse: props["reverse"] || "",
                affectedCount: 1,
                _pending: true
            });
        }

        var applied = thisObj.normalizeVisibilityAssignments(props["visibility_rules"]);
        $.each(applied, function(key, value) {
            if (value === true || value === "true") {
                var rule = thisObj.getFormVisibilityRule(key);
                if (rule !== null) {
                    var copy = $.extend({}, rule);
                    copy.affectedCount = thisObj.countRuleAffectedElements(key);
                    rules.push(copy);
                }
            }
        });

        return rules;
    },
    /**
     * Resolve the Section currently being rendered by this property editor.
     *
     * The property editor can receive a selected Section object that is not the
     * same object reference as the node inside CustomBuilder.data. When possible,
     * return the canonical builder-data node so the pending legacy key uses the
     * same path discriminator as the Visibility tool.
     *
     * @returns {Object|null} The selected/canonical Section data object.
     */
    getCurrentSectionData : function() {
        var selected = (typeof CustomBuilder !== "undefined" && CustomBuilder.Builder) ? CustomBuilder.Builder.selectedEl : null;
        if (selected && $(selected).length > 0) {
            var data = $(selected).data("data");
            if (data && data.className === "org.joget.apps.form.model.Section") {
                return this.findSectionDataInBuilder(data, data.properties, false) || data;
            }
        }

        return this.findSectionDataInBuilder(null, this.options.propertyValues, true);
    },
    /**
     * Find the canonical builder-data node for a Section.
     *
     * Exact object/property matches are safe even when section ids are duplicated.
     * Id matching is allowed only for fallback lookups where no selected Section is
     * available, and only succeeds when the id is unique in the builder data.
     *
     * @param {Object|null} sectionData Selected Section data, when available.
     * @param {Object|null} propertyValues Property-editor values for the Section.
     * @param {boolean} allowIdMatch Whether a unique id match is acceptable.
     * @returns {Object|null} Canonical Section data, or null when ambiguous/missing.
     */
    findSectionDataInBuilder : function(sectionData, propertyValues, allowIdMatch) {
        var exactMatch = null;
        var propertyMatch = null;
        var idMatches = [];
        var scan = function(data) {
            if (exactMatch || propertyMatch || data === null || data === undefined) {
                return;
            }
            if (data.className === "org.joget.apps.form.model.Section" && data.properties) {
                if (sectionData && data === sectionData) {
                    exactMatch = data;
                    return;
                } else if ((sectionData && data.properties === sectionData.properties) || data.properties === propertyValues) {
                    propertyMatch = data;
                    return;
                } else if (allowIdMatch !== false && propertyValues && propertyValues.id !== undefined && propertyValues.id !== "" &&
                        data.properties.id === propertyValues.id) {
                    idMatches.push(data);
                }
            }
            if (data.elements !== undefined && data.elements !== null) {
                $.each(data.elements, function(i, child) {
                    scan(child);
                });
            }
        };
        if (typeof CustomBuilder !== "undefined" && CustomBuilder.data) {
            scan(CustomBuilder.data);
        }
        if (exactMatch) {
            return exactMatch;
        }
        if (propertyMatch) {
            return propertyMatch;
        }
        return idMatches.length === 1 ? idMatches[0] : null;
    },
    /**
     * Compute a stable traversal path for a Section within CustomBuilder.data.
     *
     * The path is used only as a discriminator for pending, unmigrated legacy
     * rules; the final migrated rule still receives a generated real rule key.
     *
     * @param {Object} sectionData Canonical Section data.
     * @returns {string} A path like "0_1", or an empty string when unavailable.
     */
    getSectionPath : function(sectionData) {
        if (!sectionData || typeof CustomBuilder === "undefined" || !CustomBuilder.data) {
            return "";
        }
        var found = null;
        var scan = function(data, path) {
            if (found || data === null || data === undefined) {
                return;
            }
            if (data === sectionData) {
                found = path;
                return;
            }
            if (data.elements !== undefined && data.elements !== null) {
                $.each(data.elements, function(i, child) {
                    scan(child, path === "" ? i + "" : path + "_" + i);
                });
            }
        };
        scan(CustomBuilder.data, "");
        return found || "";
    },
    /**
     * Build the pending legacy rule key shared with VisibilityManager.
     *
     * The section id keeps the key readable; a builder metadata key or traversal
     * path prevents collisions when duplicate/empty section ids exist.
     *
     * @param {Object} props Section properties.
     * @param {Object} data Section data.
     * @param {string} path Canonical builder-data traversal path.
     * @returns {string} Pending rule key.
     */
    getPendingRuleKey : function(props, data, path) {
        var base = (props && props["id"]) ? props["id"] : "section";
        var discriminator = this.getSectionDiscriminator(data, path);
        return "pending_" + this.sanitizePendingKeyPart(base) + (discriminator ? "__" + discriminator : "");
    },
    /**
     * Prefer existing stable builder metadata when present, otherwise use the
     * traversal path supplied by getSectionPath().
     *
     * @param {Object} data Section data.
     * @param {string} path Canonical builder-data traversal path.
     * @returns {string} Sanitized discriminator, or an empty string.
     */
    getSectionDiscriminator : function(data, path) {
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
    sanitizePendingKeyPart : function(value) {
        value = (value === undefined || value === null) ? "" : value + "";
        return value.replace(/[^A-Za-z0-9_-]/g, "_");
    },
    normalizeVisibilityAssignments : function(value) {
        if (value === undefined || value === null || value === "") {
            return {};
        }
        if (typeof value === "string") {
            try {
                value = JSON.parse(value);
            } catch (e) {
                return {};
            }
        }
        if (typeof value !== "object" || Array.isArray(value)) {
            return {};
        }
        return value;
    },
    normalizeVisibilityRuleDefinitions : function(value) {
        if (value === undefined || value === null || value === "") {
            return [];
        }
        if (typeof value === "string") {
            try {
                value = JSON.parse(value);
            } catch (e) {
                return [];
            }
        }
        return Array.isArray(value) ? value : [];
    },
    getFormVisibilityRule : function(key) {
        var ruleObj = (typeof VisibilityManager !== "undefined" && VisibilityManager.getRuleElement) ? VisibilityManager.getRuleElement() :
                ((typeof CustomBuilder !== "undefined" && CustomBuilder.data && CustomBuilder.data.properties) ? CustomBuilder.data.properties : {});
        var rules = this.normalizeVisibilityRuleDefinitions(ruleObj["visibility_rules"]);
        for (var i = 0; i < rules.length; i++) {
            if (rules[i]["visibility_key"] === key) {
                return rules[i];
            }
        }
        return null;
    },
    countRuleAffectedElements : function(key) {
        var thisObj = this;
        var count = 0;
        var scan = function(data) {
            if (data === null || data === undefined) {
                return;
            }
            var props = data.properties || {};
            var applied = thisObj.normalizeVisibilityAssignments(props["visibility_rules"]);
            if (applied[key] === true || applied[key] === "true") {
                count++;
            }
            if (data.elements !== undefined && data.elements !== null) {
                $.each(data.elements, function(i, child) {
                    scan(child);
                });
            }
        };
        if (typeof CustomBuilder !== "undefined" && CustomBuilder.data) {
            scan(CustomBuilder.data);
        }
        return count;
    },
    renderSectionVisibilityRuleDetails : function(rule) {
        var thisObj = this;
        var controls = (rule["visibilityControl"] || "").split(";");
        var values = (rule["visibilityValue"] || "").split(";");
        var regexs = (rule["regex"] || "").split(";");
        var joins = (rule["join"] || "").split(";");
        var reverses = (rule["reverse"] || "").split(";");
        var root = {items: []};
        var stack = [root];
        var conditionCount = 0;
        var maxTokens = 2048;
        var maxGroupDepth = 64;
        var exceedsSafeLimits = controls.length > maxTokens;

        for (var i = 0; i < controls.length && !exceedsSafeLimits; i++) {
            var control = controls[i];
            if (control === undefined || control === "") {
                continue;
            }
            var join = (joins.length > i && joins[i] === "or") ? '@@app.rulesdecision.or@@' : '@@app.rulesdecision.and@@';
            var reverse = reverses.length > i && reverses[i] === "true";
            var current = stack[stack.length - 1];
            if (control === "(") {
                if (stack.length > maxGroupDepth) {
                    exceedsSafeLimits = true;
                    break;
                }
                var group = {
                    type: "group",
                    join: join,
                    reverse: reverse,
                    items: []
                };
                current.items.push(group);
                stack.push(group);
                continue;
            }
            if (control === ")") {
                if (stack.length > 1) {
                    stack.pop();
                }
                continue;
            }
            conditionCount++;
            current.items.push({
                type: "condition",
                join: join,
                reverse: reverse,
                field: control,
                operation: thisObj.getVisibilityOperationLabel(regexs.length > i ? regexs[i] : ""),
                value: thisObj.replaceAllString(values.length > i ? values[i] : "", "__", ";")
            });
        }

        if (conditionCount === 0 || exceedsSafeLimits) {
            return '<ul class="visibility-condition-list"><li class="visibility-condition-empty">' +
                    thisObj.escapeHtml('@@form.section.visibilityNoConditions@@') + '</li></ul>';
        }

        var hasConditions = function(node) {
            if (node.type === "condition") {
                return true;
            }
            for (var j = 0; j < node.items.length; j++) {
                if (hasConditions(node.items[j])) {
                    return true;
                }
            }
            return false;
        };
        var renderItems = function(items) {
            var html = '<ul class="visibility-condition-list">';
            var renderedCount = 0;
            for (var j = 0; j < items.length; j++) {
                var item = items[j];
                if (!hasConditions(item)) {
                    continue;
                }
                if (renderedCount > 0) {
                    html += '<li class="visibility-condition-join"><span>' +
                            thisObj.escapeHtml(item.join) + '</span></li>';
                }
                if (item.type === "group") {
                    html += '<li class="visibility-condition-group-box">';
                    if (item.reverse) {
                        html += '<span class="visibility-condition-reverse">' + thisObj.escapeHtml(thisObj.getVisibilityReverseLabel()) + '</span>';
                    }
                    html += renderItems(item.items);
                    html += '</li>';
                } else {
                    html += '<li class="visibility-condition">';
                    html += '<span class="visibility-condition-field">' + thisObj.escapeHtml(item.field);
                    if (item.reverse) {
                        html += '<span class="visibility-condition-reverse">' + thisObj.escapeHtml(thisObj.getVisibilityReverseLabel()) + '</span>';
                    }
                    html += '</span><span class="visibility-condition-expression">';
                    html += '<span class="visibility-condition-operation">' + thisObj.escapeHtml(item.operation) + '</span>';
                    html += '<span class="visibility-condition-value">' + thisObj.escapeHtml(thisObj.getVisibilityValueLabel(item.value)) + '</span>';
                    html += '</span></li>';
                }
                renderedCount++;
            }
            html += '</ul>';
            return html;
        };
        return renderItems(root.items);
    },
    getVisibilityOperationLabel : function(operation) {
        var labels = {
            "": "@@pbuilder.label.equalTo@@",
            ">": "@@pbuilder.label.greaterThan@@",
            ">=": "@@pbuilder.label.greaterThanOrEqualTo@@",
            "<": "@@pbuilder.label.lessThan@@",
            "<=": "@@pbuilder.label.lessThanOrEqualTo@@",
            "isTrue": "@@pbuilder.label.isTrue@@",
            "isFalse": "@@pbuilder.label.isFalse@@",
            "contains": "@@app.rulesdecision.contains@@",
            "listContains": "@@app.rulesdecision.listContains@@",
            "in": "@@app.rulesdecision.in@@",
            "true": "@@app.rulesdecision.regex@@"
        };
        return labels[operation] || labels[""];
    },
    getVisibilityReverseLabel : function() {
        return "@@form.section.visibilityNot@@";
    },
    getVisibilityValueLabel : function(value) {
        return value === "" ? "@@form.section.visibilityEmptyValue@@" : value;
    },
    openVisibilityRule : function(ruleKey, edit) {
        var openRule = function() {
            if (typeof VisibilityManager !== "undefined" && VisibilityManager.openRule) {
                VisibilityManager.openRule(ruleKey, edit);
                return;
            }
            if (typeof CustomBuilder !== "undefined" && CustomBuilder.enableEnhancedTools) {
                CustomBuilder.enableEnhancedTools();
            }
            $("#visibility-btn").trigger("click");
        };

        if (typeof CustomBuilder !== "undefined"
                && typeof CustomBuilder.checkChangeBeforeCloseElementProperties === "function") {
            CustomBuilder.checkChangeBeforeCloseElementProperties(openRule);
        } else {
            openRule();
        }
    },
    openSectionVisibilityRule : function(ruleKey) {
        var edit = !(typeof ruleKey === "string" && ruleKey.indexOf("pending_") === 0);
        this.openVisibilityRule(ruleKey, edit);
    },
    replaceAllString : function(value, search, replacement) {
        value = (value === undefined || value === null) ? "" : value + "";
        return value.split(search).join(replacement);
    },
    escapeHtml : function(value) {
        value = (value === undefined || value === null) ? "" : value + "";
        if (typeof PropertyEditor !== "undefined" && PropertyEditor.Util && PropertyEditor.Util.escapeHtmlTag) {
            return PropertyEditor.Util.escapeHtmlTag(value);
        }
        return value.replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#39;");
    }
}
