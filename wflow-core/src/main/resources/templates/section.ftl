<div id="${elementParamName!}" class="form-section <#if !(element.properties.label?? && element.properties.label != "") >no_label</#if> section_${element.properties.elementUniqueKey!}" ${elementMetaData!} <#if visible == false && includeMetaData == false>style="display: none"</#if>>
    <style>
        .section_${element.properties.elementUniqueKey!} .form-section-title, .section_${element.properties.elementUniqueKey!} .subform-section-title {
            position: relative;
        }
        .section_${element.properties.elementUniqueKey!} .section-toggle-icon {
            position: absolute;
            top: 18px;
            right: 18px; 
            border-left: 5px solid transparent;
            border-right: 5px solid transparent;
            border-top: 5px solid #333;
            transform: rotate(0deg);
            transition: transform 0.2s;
            cursor: pointer;
            z-index: 2;
        }
        [dir="rtl"] .section_${element.properties.elementUniqueKey!} .section-toggle-icon {
            right: auto;
            left: 0;
        }
        .section_${element.properties.elementUniqueKey!}.collapsed .section-toggle-icon {
            transform: rotate(-180deg);
        }
        .section_${element.properties.elementUniqueKey!} .form-section-title.collapsible, .section_${element.properties.elementUniqueKey!} .subform-section-title.collapsible {
            cursor: pointer;
        }
        .section_${element.properties.elementUniqueKey!} .form-section-title.section-mandatory span:after, .section_${element.properties.elementUniqueKey!} .subform-section-title.section-mandatory span:after {
            content: ' *';
            color: red;
        }
    </style>

    <#assign isCollapsible = (element.properties.label?has_content && (element.properties.sectionCollapsible! == 'expanded' || element.properties.sectionCollapsible! == 'collapsed'))>
    <#assign defaultCollapsed = (element.properties.sectionCollapsible! == 'collapsed')>

    <div class="form-section-title <#if isCollapsible>collapsible</#if>">
        <#if isCollapsible>
            <i class="section-toggle-icon"></i>
        </#if>
        <#if element.properties.label?? && element.properties.label != ""><span>${element.properties.label!}</span></#if>
    </div>
    
    <#if includeMetaData == false>
    <div class="section-content" <#if isCollapsible && defaultCollapsed>style="display:none"</#if>>
        <#list element.children as e>
            ${e.render(formData, includeMetaData!false)}
        </#list>
        <div style="clear:both;"></div>
    </div>
    <#else>
        <#list element.children as e>
            ${e.render(formData, includeMetaData!false)}
        </#list>
    </#if>
    
    <script type="text/javascript">
        $(document).ready(function() {
            var sectionBox = $(document.getElementById('${elementParamName!}'));
            var header = sectionBox.children('.form-section-title, .subform-section-title');
            var content = sectionBox.children('.section-content');
            
            <#if isCollapsible>
                <#if includeMetaData == false>
                    header.on('click', function() {
                        content.slideToggle(200);
                        sectionBox.toggleClass('collapsed');
                    });
                    
                    <#if defaultCollapsed>
                        sectionBox.addClass('collapsed');
                    </#if>
                </#if>
            </#if>

            var updateMandatory = function() {
                var mandatoryFound = false;
                if (sectionBox.find('.form-cell-validator, .subform-cell-validator').text().indexOf('*') !== -1) {
                    mandatoryFound = true;
                }
                if (!mandatoryFound && sectionBox.find('[data-required="true"]').length > 0) {
                     mandatoryFound = true;
                }
                
                if (mandatoryFound) {
                    header.addClass('section-mandatory');
                } else {
                    header.removeClass('section-mandatory');
                }
            };
            
            updateMandatory();
            sectionBox.on('change jsection:show jsection:hide', function() {
                 setTimeout(updateMandatory, 100);
            });
            
            // For Builder: MutationObserver to detect changes in children (e.g., adding/removing fields)
            <#if includeMetaData == true>
            if (window.MutationObserver) {
                var observer = new MutationObserver(function(mutations) {
                    updateMandatory();
                });
                observer.observe(sectionBox[0], { childList: true, subtree: true, attributes: true, attributeFilter: ['class', 'data-required'] });
            }
            </#if>
        });
    </script>
    
    <#if rules?? && includeMetaData == false>
    <script type="text/javascript">
        $(document).ready(function() {
            new VisibilityMonitor($('.section_${element.properties.elementUniqueKey!}'), ${rules}).init();
        });
    </script>
    </#if>
    
    <#if includeMetaData == false>
    <div style="clear:both"></div>
    </#if>
</div>
