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
        $(function(){
            setTimeout(function(){
                layui.use('layer', function(){
                    xadmin.add_tab('${preloadLabel}','${preloadUrl}',true);
                });
            }, 100);
        });
    </script>
</#if>
