${loginBackground!}
${login_before!}
<div class="login layui-anim layui-anim-up">
    ${logo!}
    <div class="message">${login_title!}</div>
    <div id="darkbannerwrap"></div>
    <#if userview.params.login_error?? >
        ${login_error_before!}
        <div id="main-body-message" class="${login_error_classes!}">
            ${login_error_inner_before!}
            ${login_exception!}
            ${login_error_inner_after!}
        </div>
        ${login_error_after!}
    </#if>
    ${login_form_before!}
    <form id="loginForm" name="loginForm" action="${context_path!}/j_spring_security_check" method="POST" target="_top">
        ${login_form_inner_before!}
        <input id='j_username' name="j_username" placeholder="@@ubuilder.login.username@@"  type="text" class="layui-input" >
        <hr class="hr15">
        <input id='j_password' name="j_password" placeholder="@@ubuilder.login.password@@"  type="password" class="layui-input">
        <hr class="hr15">
        <input value="@@ubuilder.login@@" class="form-button" style="width:100%;" type="submit">
        <hr class="hr20" >
        ${login_form_footer!}
        ${login_form_inner_after!}
    </form>
    ${login_form_after!}
    <script>
        $(function(){
            $("#loginForm input[type='submit']").click(function(){
                $.blockUI({ css: { 
                    border: 'none', 
                    padding: '15px', 
                    backgroundColor: '#000', 
                    '-webkit-border-radius': '10px', 
                    '-moz-border-radius': '10px', 
                    opacity: .3, 
                    color: '#fff' 
                }, message : "<h1>@@form.form.message.wait@@</h1>" }); 
                return true;
            });
        });
    </script>
</div>
${login_after!}

