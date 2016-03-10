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
<form id="loginForm" name="loginForm" action="${context_path!}/j_spring_security_check" method="POST">
    ${login_form_inner_before!}
    <table align="center">
        <tr><td><label>@@ubuilder.login.username@@: </label></td><td><input type='text' id='j_username' name='j_username'/></td></tr>
        <tr><td><label>@@ubuilder.login.password@@: </label></td><td><input type='password' id='j_password' name='j_password'/></td></tr>
        <tr><td>&nbsp;</td><td><input name="submit" class="form-button" type="submit" value="@@ubuilder.login@@" /></td></tr>
        <tr><td colspan="2">
            ${login_form_footer!}
        </td></tr>
    </table>
    ${login_form_inner_after!}
</form>
${login_form_after!}