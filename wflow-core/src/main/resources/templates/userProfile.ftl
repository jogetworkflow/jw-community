<style>
    .userProfile-body-content .userProfile-body-header {
        clear:both;
        color:#000000;
        font-family:Georgia,"Times New Roman",Times,serif;
        font-size:1.4em;
        font-style:normal;
        font-variant:normal;
        font-weight:normal;
        height:22px;
        padding:0.5em;
    }

    .form fieldset {
        background:none repeat scroll 0 0 transparent;
        border:1px dotted silver;
        clear:both;
        margin:0.5em 0;
        padding:0.5em;
    }

    .form-row {
        clear:both;
        display:block;
        line-height:25px;
        text-align:left;
    }

    .form-row label {
        display:inline-block;
        float:left;
        min-width:100px;
        padding:0.25em;
        text-align:left;
        vertical-align:top;
        width:40%;
    }

    .form-row .form-input {
        display:inline-block;
        padding:0.25em;
        text-align:left;
    }
</style>
<#if element.properties.isPreview! == 'true' >
    <script>
        $(document).ready(function() {
            $(".form-button").attr("disabled", "disabled");
        });
    </script>
</#if>
<div class="userProfile-body-content">
    <div class="userProfile-body-header">
        ${element.properties.headerTitle!}
    </div>
    <#if element.properties.view! == 'redirect'>
        <script>
            <#if element.properties.message?? >
                alert('${element.properties.message!}');
            </#if>
            window.location = "${element.properties.redirectURL!}";
        </script>
    <#elseif element.properties.view! == 'formView' && element.properties.user??>
        <#if element.properties.saved! == 'true'>
            <p class="form-message" style="display:block;color:blue;">
                <span>@@console.directory.user.message.saved@@</span>
            </p>
        </#if>
        
        <form id="profile" action="${element.properties.actionUrl!}" method="POST" class="form">
            <input type="hidden" name="id" value="${element.properties.user.id!?html}">
            <input type="hidden" name="username" value="${element.properties.user.username!?html}">
            <#if element.properties.errors??>
                <div class="form-errors">
                    <#list element.properties.errors! as error>
                        ${error!}<br/>
                    </#list>
                </div>
            </#if>
            <fieldset>
                <legend>@@console.directory.user.common.label.details@@</legend>
                
                <#if element.properties.f_username! != 'hide'>
                    <div class="form-row">
                        <label for="field1">@@console.directory.user.common.label.username@@</label>
                        <span class="form-input">${element.properties.user.username!?html}</span>
                    </div>
                </#if>

                <#if element.properties.f_firstName! != 'hide'>
                    <div class="form-row">
                        <label for="field1">@@console.directory.user.common.label.firstName@@</label>
                        <#if element.properties.f_firstName! == 'readonly'>
                            <span class="form-input">${element.properties.user.firstName!?html}</span>
                        <#else>
                            <span class="form-input"><input type="text" id="firstName" name="firstName" value="${element.properties.user.firstName!?html}"/> *</span>
                        </#if>
                    </div>
                </#if>

                <#if element.properties.f_lastName! != 'hide'>
                    <div class="form-row">
                        <label for="field1">@@console.directory.user.common.label.lastName@@</label>
                        <#if element.properties.f_lastName! == 'readonly'>
                            <span class="form-input">${element.properties.user.lastName!?html}</span>
                        <#else>
                            <span class="form-input"><input type="text" id="lastName" name="lastName" value="${element.properties.user.lastName!?html}"/></span>
                        </#if>
                    </div>
                </#if>

                <#if element.properties.f_email! != 'hide'>
                    <div class="form-row">
                        <label for="field1">@@console.directory.user.common.label.email@@</label>
                        <#if element.properties.f_email! == 'readonly'>
                            <span class="form-input">${element.properties.user.email!?html}</span>
                        <#else>
                            <span class="form-input"><input type="text" id="email" name="email" value="${element.properties.user.email!?html}"/></span>
                        </#if>
                    </div>
                </#if>

                <#if element.properties.f_timeZone! != 'hide'>
                    <div class="form-row">
                        <label for="field1">@@console.directory.user.common.label.timeZone@@</label>
                        <span class="form-input">
                            <select id="timeZone" name="timeZone">
                                <#list element.properties.timezones!?keys as key>
                                    <#assign selected = "">
                                    <#if key == element.properties.user.timeZone!>
                                        <#assign selected = "selected">
                                    </#if>
                                    <option value="${key}" ${selected}>${element.properties.timezones[key]!}</option>
                                </#list>
                            </select>
                        </span>
                    </div>
                </#if>

                <#if element.properties.f_locale! != 'hide' && element.properties.enableUserLocale! == 'true'>
                    <div class="form-row">
                        <label for="field1">@@console.directory.user.common.label.locale@@</label>
                        <span class="form-input">
                            <select id="locale" name="locale">
                                <option value=""></option>
                                <#list element.properties.localeStringList!?keys as key>
                                    <#assign selected = "">
                                    <#if key == element.properties.user.locale!>
                                        <#assign selected = "selected">
                                    </#if>
                                    <option value="${key}" ${selected}>${element.properties.localeStringList[key]!}</option>
                                </#list>
                            </select>
                        </span>
                    </div>
                </#if>
            </fieldset>
            <#if element.properties.f_password! != 'hide'>
                <fieldset>
                    <legend>@@console.directory.user.common.label.changePassword@@</legend>
                    <#if element.properties.passwordErrors??>
                        <div class="form-errors">
                            <#list element.properties.passwordErrors! as error>
                                ${error!}<br/>
                            </#list>
                        </div>
                    </#if>
                    <div class="form-row">
                        <label for="field1">@@console.directory.user.common.label.password@@</label>
                        <span class="form-input"><input type="password" id="password" name="password" autocomplete="off" /></span>
                    </div>
                    <div class="form-row">
                        <label for="field1">@@console.directory.user.common.label.confirmPassword@@</label>
                        <span class="form-input"><input type="password" id="confirmPassword" name="confirmPassword" autocomplete="off" /></span>
                    </div>
                    <div class="policies" style="display:block">
                        <#list element.properties.policies! as policy>
                            <span>${policy}</span>
                        </#list>
                    </div>
                </fieldset>
            </#if> 
            <fieldset>
                <legend>@@console.directory.user.common.label.authentication@@</legend>
                <div class="form-row">
                    <label for="password">@@console.directory.user.common.label.oldPassword@@</label>
                    <span class="form-input"><input id="oldPassword" name="oldPassword" type="password" value="" autocomplete="off" /></span>
                </div>
            </fieldset>
            ${element.properties.userProfileFooter!} 
            <div class="form-buttons">
                <input class="form-button" type="button" value="@@general.method.label.save@@"  onclick="validateField()"/>
            </div>
        </form>
        <script type="text/javascript">

            function validateField(){
                var valid = true;
                var alertString = "";
                <#if element.properties.f_firstName! != 'hide'>
                    if($("#firstName").val() == ""){
                        alertString += '@@User.firstName[not.blank]@@';
                        valid = false;
                    }
                </#if>
                <#if element.properties.f_password! != 'hide'>
                    if($("#password").val() != $("#confirmPassword").val()){
                        if(alertString != ""){
                            alertString += '\n';
                        }
                        alertString += '@@console.directory.user.error.label.passwordNotMatch@@';
                        valid = false;
                    }
                </#if>

                if(valid){
                    $("form#profile").submit();
                }else{
                    alert(alertString);
                }
            }
        </script>
    <#else>
        <p>
            @@general.content.unauthorized@@
        </p>
    </#if>
</div>
<div style="clear:both;"></div>




