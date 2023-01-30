${sidebar_before!}
${menus!}

<div class="page-content">
    <div class="layui-tab tab" lay-filter="xbs_tab" lay-allowclose="false">
        <ul class="layui-tab-title">
        </ul>
        <div class="layui-unselect layui-form-select layui-form-selected" id="tab_right">
            <dl>
                <dd data-type="this">@@xadmin.closeCurrent@@</dd>
                <dd data-type="other">@@xadmin.closeOthers@@</dd>
                <dd data-type="all">@@xadmin.closeAll@@</dd>
            </dl>
        </div>
        <div class="layui-tab-content">
        </div>
        <div id="tab_show"></div>
    </div>
</div>
<div class="page-content-bg"></div>
<#if preloadUrl??>
    <script>
        if (window.frameElement !== null && $("body", window.parent.document).hasClass("index-window") && window.parent['xadmin'].validateUrl('${preloadUrl?js_string}')) {
            window.location = '${preloadUrl?js_string}';
        } else if (xadmin.validateUrl('${preloadUrl?js_string}')) {
            $(function(){
                function initTheme() {
                    setTimeout(function(){
                        layui.use(['layer', 'element'], function(){
                            if (layer !== undefined && element !== undefined ) {
                                xadmin.add_tab('${preloadLabel?js_string}','${preloadUrl?js_string}',true);
                            } else {
                                initTheme();
                            }
                        });
                    }, 1000); 
                }
                initTheme();
                var scrollBar = function(selector, theme, mousewheelaxis) {
                    $(selector).mCustomScrollbar({
                        theme: theme,
                        scrollInertia: 100,
                        axis: "mousewheelaxis",
                        mouseWheel: {
                            enable: !0,
                            axis: mousewheelaxis,
                            preventDefault: !0
                        }
                    });
                };
                if ($("#navigation").length > 0) {
                    scrollBar("#navigation", "minimal-dark", "y");
                }
            });
        }
    </script>
</#if>
