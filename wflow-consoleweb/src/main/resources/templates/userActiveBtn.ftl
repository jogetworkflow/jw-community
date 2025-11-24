<li><button class="active-btn console-primary" onclick="toogleActive(1)" style="display:none;">@@console.directory.user.common.label.status.setActive@@</button></li>
<li><button class="active-btn console-danger" onclick="toogleActive(0)" style="display:none;">@@console.directory.user.common.label.status.setInactive@@</button></li>
<script>
    function setStatus(active) {
        $(".active-btn").hide();
        if (active === 1) {
            $(".active-btn.console-danger").show();
            $(".form-input.status").text("@@console.directory.user.common.label.status.active@@");
        } else {
            $(".active-btn.console-primary").show();
            $(".form-input.status").text("@@console.directory.user.common.label.status.inactive@@");
        }
    }
    function toogleActive(active){
        UI.blockUI(); 
        var call = function() {
            var callback = {
                success : function(response) {
                    setStatus(JSON.parse(response).active);
                    UI.unblockUI(); 
                }
            }
            var request = ConnectionManager.post('${request.contextPath}/web/json/console/directory/user/status/${user.id}/toggle', callback);
        }

        if (active === 1 || confirm('@@console.directory.user.common.label.status.setInactive.confirm@@')) {
            call();
        }
    }
    setStatus(${user.active});
</script>