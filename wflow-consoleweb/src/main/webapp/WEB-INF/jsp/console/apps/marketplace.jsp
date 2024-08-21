<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="appName" value="${param.name}" />
<c:set var="appId" value="${param.appId}" />
<%@ page import="org.joget.workflow.util.WorkflowUtil" %>

<% String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
            pageContext.setAttribute("theme", theme);%>

<c:if test="${not empty theme and theme ne 'classic'}">
    <link rel="stylesheet" type="text/css"
          href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number" />" />
</c:if>

<commons:popupHeader bodyCssClass=" builder-popup" builderTheme="${theme}" />


<style>
    *,
    *:after,
    *:before {
        box-sizing: border-box;
    }
    html{
        height: 100%;
    }
    body{
        height: 100%;
        font-family: -apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,"Noto Sans",sans-serif,"Apple Color Emoji","Segoe UI Emoji","Segoe UI Symbol","Noto Color Emoji";
    }
    #plugin-container>article>div.card-footer>div.card-meta.card-meta--views>i {
        margin-right: 5px;
        margin-top: 2px;
    }
    #main-body-header .header-title {
        font-size: 21px;
        text-transform: none;
    }
    #selectContainer #pluginCategory {
        height: 35px;
        background-color: var(--theme-primary-color-3, #fcfcfc);
        border: 1px solid var(--theme-border-color-1);
        border-radius: 0;
    }
    #selectContainer {
        font-size: 15px;
        display: inline-block;
        float: right;
        margin-right: 60px;
        font-weight: 500;
    }
    .loginMarketplace {
        color: #fff;
        font-size: 30px;
        display: inline-block;
        float: right;
        margin-right: 30px;
    }
    .loginMarketplace span{
        font-size: 18px;
        vertical-align: middle;
        display: inline-block;
        padding: 5px 5px 12px;
    }
    .loginMarketplace:hover {
        color: #fff;
        opacity: 0.6;
    }
    .loginMarketplace + #selectContainer{
        margin-right: 10px;
    }
    #searchPlugin {
        width: 400px;
        border-radius: 0px;
        height: 35px;
        background-color: var(--theme-primary-color-3, #fff);
        float: right;
        margin-right: 10px;
        font-weight: 500;
    }
    body .page-header {
        padding: 1px 50px 0px;
        background-color: var(--theme-primary-color-1, #fff);
    }
    body .page-header #page-title {
        text-transform: none;
        font-weight: 600;
        margin-bottom: 5px;
        color: var(--theme-label-color-1, #071437);
        font-size: 17px;
        margin-top: 30px;
    }
    body {
        overflow: hidden;
    }
    body div.content {
        overflow: auto;
        height: 100%;
        background-color: var(--theme-primary-color-3, #fcfcfc);
    }
    #marketplaceAppFrame {
        position: absolute;
        top: 34px;
        left: 0;
        width: 100%;
        height: calc(100% - 34px);
        border: 0;
        margin-top: 7px
    }
    .marketplacPluginExplorerHeader {
        height: unset;
    }
    #plugin-container {
        width: 100%;
        display: flex;
        align-items: center;
        padding: 15px 50px;
        flex-wrap: wrap;
        border-top: 1px solid var(--theme-border-color-1, #e6e6e6);
    }
    #plugin-container.available > *:not(.available),
    #plugin-container.update > *:not(.update),
    #plugin-container.installed > *:not(.installed){
        display: none;
    }
    #plugin-container>article>div.card-header>a:nth-child(1) {
        padding: 0;
    }
    #plugin-container .card {
        background-color: var(--theme-primary-color-1, #fff);
        border-radius: 0.625rem;
        overflow: hidden;
        padding: 1.25rem 1.25rem 0.25rem;
        position: relative;
        transition: 0.15s ease-in;
        margin-right: 20px;
        width: calc(25% - 20px);
        margin-bottom: 20px;
        border: 1px solid var(--theme-border-color-1, #F1F1F4);
        box-shadow: 0px 3px 4px 0px rgba(0, 0, 0, 0.03);
    }
    #plugin-container .card:hover,
    #plugin-container .card:focus-within {
        box-shadow: 0 0 0 2px #16C79A, 0 10px 60px 0 rgba(0, 0, 0, 0.1);
        transform: translatey(-5px);
    }
    #plugin-container .card .card-image {
        border-radius: 10px;
        overflow: hidden;
        margin: 0;
        background: transparent no-repeat center;
        padding-bottom: 50%;
        background-size: cover;
    }
    
    #plugin-container .card .card-header .meta {
        height: 180px;
        overflow: hidden;
    }
    #plugin-container .card .card-header h3 {
        font-weight: 600;
        font-size: 16px;
        line-height: 1.35;
        padding-right: 1rem;
        text-decoration: none;
        color: var(--theme-label-color-2, #071437);
        will-change: transform;
        text-transform: capitalize;
    }
    #plugin-container .card .card-header .desc {
        padding: 0px;
        height: 70px;
        min-height: 70px;
        overflow: hidden;
        color: var(--theme-label-color-2, #99A1B7);
        font-weight: normal;
        text-align: justify;
    }
    #plugin-container .card .card-header .icon-button {
        border: 0;
        background-color: #fff;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-shrink: 0;
        transition: 0.25s ease;
        z-index: 1;
        cursor: pointer;
        color: #565656;
    }
    #plugin-container .card.installed:not(.update) .icon-button {
        visibility: hidden;
        pointer-events: none;
    }
    #plugin-container .card .card-header .icon-button svg {
        width: 1em;
        height: 1em;
    }
    #plugin-container .card .card-header .icon-button:hover,
    #plugin-container .card .card-header .icon-button:focus {
        background-color: var(--theme-active-color-2, #0069d9);
        color: #FFF;
    }
    #plugin-container .card .card-header .icon-button {
        border: 1px solid var(--theme-border-color-1, #e6e6e6);
        background-color: var(--theme-primary-color-1, #fff);
        color: var(--theme-label-color-1, #333);
    }
    #plugin-container .card .card-footer {
        margin-top: 10px;
        border-top: 1px solid var(--theme-border-color-1, #e6e6e6);
        padding-top: 0.7rem;
        display: flex;
        align-items: center;
        flex-wrap: wrap;
    }
    #plugin-container .card .card-footer .card-meta {
        display: flex;
        align-items: center;
        color: var(--theme-label-color-2, #6c757d);
        padding: 0 5px;
        margin-bottom: 5px;
    }
    #plugin-container .card .card-footer .card-meta:first-child:after {
        display: block;
        content: "";
        width: 4px;
        height: 4px;
        border-radius: 50%;
        background-color: currentcolor;
        margin-left: 0.75rem;
        margin-right: 0.75rem;
    }
    #plugin-container .card .card-footer .card-meta.version {
        margin-left: auto;
    }
    #plugin-container .card .card-footer .card-meta svg {
        flex-shrink: 0;
        width: 1em;
        height: 1em;
        margin-right: 0.25em;
    }
    #plugin-container .card .card-header .installing {
        margin-left: 5px;
    }
    #plugin-container.ajaxloading{
        min-height: 85%;
        background-repeat: repeat-y;
        background-size: 100%;
        background-image: url(data:image/svg+xml;base64,PHN2ZwogIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgCiAgcm9sZT0iaW1nIgogIHdpZHRoPSI4MjAiCiAgaGVpZ2h0PSI0NTAiCiAgYXJpYS1sYWJlbGxlZGJ5PSJsb2FkaW5nLWFyaWEiCiAgdmlld0JveD0iMCAwIDgyMCA0NTAiCiAgcHJlc2VydmVBc3BlY3RSYXRpbz0ibm9uZSIKPgogIDx0aXRsZSBpZD0ibG9hZGluZy1hcmlhIj5Mb2FkaW5nLi4uPC90aXRsZT4KICA8cmVjdAogICAgeD0iMCIKICAgIHk9IjAiCiAgICB3aWR0aD0iMTAwJSIKICAgIGhlaWdodD0iMTAwJSIKICAgIGNsaXAtcGF0aD0idXJsKCNjbGlwLXBhdGgpIgogICAgc3R5bGU9J2ZpbGw6IHVybCgiI2ZpbGwiKTsnCiAgPjwvcmVjdD4KICA8ZGVmcz4KICAgIDxjbGlwUGF0aCBpZD0iY2xpcC1wYXRoIj4KICAgICAgICA8cmVjdCB4PSIxMCIgeT0iMTAiIHJ4PSI1IiByeT0iNSIgd2lkdGg9IjI2MCIgaGVpZ2h0PSIxNDAiPjwvcmVjdD48cmVjdCB4PSIyODAiIHk9IjEwIiByeD0iNSIgcnk9IjUiIHdpZHRoPSIyNjAiIGhlaWdodD0iMjgwIj48L3JlY3Q+PHJlY3QgeD0iNTUwIiB5PSIxMCIgcng9IjUiIHJ5PSI1IiB3aWR0aD0iMjYwIiBoZWlnaHQ9IjE0MCI+PC9yZWN0PjxyZWN0IHg9IjEwIiB5PSIxNjAiIHJ4PSI1IiByeT0iNSIgd2lkdGg9IjI2MCIgaGVpZ2h0PSIyODAiPjwvcmVjdD48cmVjdCB4PSIyODAiIHk9IjMwMCIgcng9IjUiIHJ5PSI1IiB3aWR0aD0iMjYwIiBoZWlnaHQ9IjE0MCI+PC9yZWN0PjxyZWN0IHg9IjU1MCIgeT0iMTYwIiByeD0iNSIgcnk9IjUiIHdpZHRoPSIyNjAiIGhlaWdodD0iMjgwIj48L3JlY3Q+CiAgICA8L2NsaXBQYXRoPgogICAgPGxpbmVhckdyYWRpZW50IGlkPSJmaWxsIj4KICAgICAgPHN0b3AKICAgICAgICBvZmZzZXQ9IjAuNTk5OTY0IgogICAgICAgIHN0b3AtY29sb3I9IiNmM2YzZjMiCiAgICAgICAgc3RvcC1vcGFjaXR5PSIxIgogICAgICA+CiAgICAgICAgPGFuaW1hdGUKICAgICAgICAgIGF0dHJpYnV0ZU5hbWU9Im9mZnNldCIKICAgICAgICAgIHZhbHVlcz0iLTI7IC0yOyAxIgogICAgICAgICAga2V5VGltZXM9IjA7IDAuMjU7IDEiCiAgICAgICAgICBkdXI9IjFzIgogICAgICAgICAgcmVwZWF0Q291bnQ9ImluZGVmaW5pdGUiCiAgICAgICAgPjwvYW5pbWF0ZT4KICAgICAgPC9zdG9wPgogICAgICA8c3RvcAogICAgICAgIG9mZnNldD0iMS41OTk5NiIKICAgICAgICBzdG9wLWNvbG9yPSIjZWNlYmViIgogICAgICAgIHN0b3Atb3BhY2l0eT0iMSIKICAgICAgPgogICAgICAgIDxhbmltYXRlCiAgICAgICAgICBhdHRyaWJ1dGVOYW1lPSJvZmZzZXQiCiAgICAgICAgICB2YWx1ZXM9Ii0xOyAtMTsgMiIKICAgICAgICAgIGtleVRpbWVzPSIwOyAwLjI1OyAxIgogICAgICAgICAgZHVyPSIxcyIKICAgICAgICAgIHJlcGVhdENvdW50PSJpbmRlZmluaXRlIgogICAgICAgID48L2FuaW1hdGU+CiAgICAgIDwvc3RvcD4KICAgICAgPHN0b3AKICAgICAgICBvZmZzZXQ9IjIuNTk5OTYiCiAgICAgICAgc3RvcC1jb2xvcj0iI2YzZjNmMyIKICAgICAgICBzdG9wLW9wYWNpdHk9IjEiCiAgICAgID4KICAgICAgICA8YW5pbWF0ZQogICAgICAgICAgYXR0cmlidXRlTmFtZT0ib2Zmc2V0IgogICAgICAgICAgdmFsdWVzPSIwOyAwOyAzIgogICAgICAgICAga2V5VGltZXM9IjA7IDAuMjU7IDEiCiAgICAgICAgICBkdXI9IjFzIgogICAgICAgICAgcmVwZWF0Q291bnQ9ImluZGVmaW5pdGUiCiAgICAgICAgPjwvYW5pbWF0ZT4KICAgICAgPC9zdG9wPgogICAgPC9saW5lYXJHcmFkaWVudD4KICA8L2RlZnM+Cjwvc3ZnPg==);
    }
    body .boxy-wrapper .title-bar .close {
        top: 20px;
        right: 20px;
    }
    section#page-description {
        color: var(--theme-label-color-1, #99A1B7);
        font-weight: 500;
        font-size: 13px;
    }
    .jgt-tabs {
        margin-top: 20px;
    }
    .jgt-tabs li {
        display: inline-block
    }
    .jgt-tabs li a.jgt-tabs-anchor{
        display: block;
        padding: 5px 15px;
        border-bottom: 3px solid transparent;
        opacity: 0.8;
    }
    .jgt-tabs li.selected a.jgt-tabs-anchor {
        border-color: var(--theme-label-color-1, #071437);
        opacity: 1;
        padding-bottom: 12px;
    }
    .jgt-badge{
        background: var(--theme-active-color-1, #0069d9);
        color: var(--theme-label-color-1, #ffffff);
        display: inline-block;
        min-width: 18px;
        text-align: center;
        border-radius: 6px;
        margin-left: 2px;
        padding-inline: 6px;
        padding-block: 2px;
    }
</style>

<div id="main-body-header" class="marketplacPluginExplorerHeader">
    <span class="header-title"><ui:msgEscHTML key="cbuilder.seamless.marketplace.joget.marketplace"/></span>
    <c:if test="${isAnonymous eq true}">
        <a class="loginMarketplace" onclick="login()" title="<ui:msgEscHTML key="ubuilder.login"/>"><i class="fa fa-user"></i> <span><ui:msgEscHTML key="ubuilder.login"/></span></a>
    </c:if>
    <div id="selectContainer">
        <select id="pluginCategory" onChange="selectedCategory()">
            <option value="all"><fmt:message key="cbuilder.displayAll"/></option>
        </select>
    </div>
    <input id="searchPlugin" type="text" placeholder="<ui:msgEscHTML key="cbuilder.seamless.marketplace.plugin.search"/>">
</div>
<div class="content">
    <div class="page-header">
        <h1 id="page-title"></h1>
        <section id="page-description"></section>
        <ul id="plugins-tabs" role="tablist" class="jgt-tabs">
            <li class="selected jgt-tabs-tab">
                <a ref="available" tabindex="-1" class="jgt-tabs-anchor" id="ui-id-1"><span>Available Plugins</span> <span class="jgt-badge">0</span></a>
            </li>
            <li class=" jgt-tabs-tab">
                <a ref="installed" tabindex="-1" class="jgt-tabs-anchor" id="ui-id-3"><span>Installed Plugins</span> <span class="jgt-badge">0</span></a>
            </li>
            <li class="jgt-tabs-tab">
                <a ref="update" tabindex="-1" class="jgt-tabs-anchor" id="ui-id-2"><span>Update</span> <span class="jgt-badge">0</span></a>
            </li>
        </ul>    
    </div>
    <div id="plugin-container" class="available"></div>
</div>

<script>
    var buildeType = '<c:out value="${type}" escapeXml="true" />';
    var pluginType = '<c:out value="${pluginType}" escapeXml="true" />';
    
    jQuery.expr[':'].Contains = function(a,i,m){ 
        return (a.textContent || a.innerText || "").toUpperCase().indexOf(m[3].toUpperCase())>=0; 
    };

    /*
     * Load login page
     */
    function login() {
        if (parent.CustomBuilder){
            parent.CustomBuilder.Builder.loadSeamlessMarketplace(pluginType, true);
        }
    }

    //install plugin in seamless marketplace
    function installPlugin(id) {
        var installUrl = "${pageContext.request.contextPath}/web/json/apps/install";
        if (confirm('<ui:msgEscJS key="cbuilder.seamless.marketplace.confirmPluginInstallation"/>')) {
            var installCallback = {
                success: function (data) {
                    setTimeout(function(){
                        $("[data-id='installplugin_" + id + "']").html('<ui:msgEscJS key="appCenter.label.installApp"/>');
                        $("[data-id='installplugin_" + id + "']").removeAttr("disabled");
                        var selectedValue = $('#pluginCategory').val();
                        parent.CustomBuilder.Builder.reloadPaletteOrProperties(selectedValue);
                        var app = JSON.parse(data);
                        if (app.pluginName) {
                            $("[data-id='installplugin_" + id + "']").closest(".card").removeClass("available").removeClass("update").addClass("installed")
                                    .find(".currentVersion").remove();
                            updateTabs();
                            alert('<ui:msgEscJS key="appCenter.label.appInstalled"/>');
                        } else {
                            alert('<ui:msgEscJS key="appCenter.label.appNotInstalled"/>');
                        }
                    }, 5000); //delay for plugin to reload at backend
                },
                error: function (data) {
                    $("[data-id='installplugin_" + id + "']").html('<ui:msgEscJS key="appCenter.label.installApp"/>');
                    $("[data-id='installplugin_" + id + "']").removeAttr("disabled");
                    alert('<ui:msgEscJS key="appCenter.label.appNotInstalled"/>');
                }
            };
            $("[data-id='installplugin_" + id + "']").html('<i class="icon-spinner icon-spin fas fa-spinner fa-spin"></i><span class="installing"><ui:msgEscJS key="appCenter.label.installingApp"/></span>');
            $("[data-id='installplugin_" + id + "']").attr("disabled", "disabled");

            // invoke installation
            var installParams = "url=" + encodeURIComponent("<ui:msgEscJS key="appCenter.link.marketplace.url"/>/jw/web/json/plugin/org.joget.marketplace.ProtectedAppUpload/service?action=download&id=" + id);
            ConnectionManager.post(installUrl, installCallback, installParams);
        }
    }

    //show plugins based on selected category
    function selectedCategory() {
        $('#plugin-container').addClass('ajaxloading');
        var selected = $('#pluginCategory').find(':selected');
        var selectedClasses = $(selected).val();
        var selectedValue = $(selected).text();
        $('#plugin-container').empty();
        
        if (selectedClasses !== "all" && selectedClasses !== "undefined") {
            setTitle(selectedValue + ' ');
            
            getPluginList([selectedValue], [selectedClasses]);
        } else {
            setTitle(getBuilderLabel());
            
            var searchCategories = [];
            var searchClasses = [];
            $('#pluginCategory option').each(function(){
                if ($(this).val() !== "all") {
                    searchCategories.push($(this).text());
                    searchClasses.push($(this).val());
                }
            });
            
            getPluginList(searchCategories, searchClasses);
        }
    }

    //search plugin based on the search value
    var searchPlugin = function () {
        var searhText = $('#searchPlugin').val();
        
        if(searhText !== "") { 
            $("#plugin-container").find(".card .meta:not(:Contains(" + searhText + "))").closest(".card").addClass("search_hidden");
            $("#plugin-container").find(".card .meta:Contains(" + searhText + ")").closest(".card").removeClass("search_hidden");
        } else { 
            $("#plugin-container").find(".card").removeClass("search_hidden");
        }
        
        //update tabs
        updateTabs();
    };

    // get builder label from parent builder
    var getBuilderLabel = function () {
        if (parent.CustomBuilder) {
            return parent.CustomBuilder.builderLabel;
        } else {
            return '';
        }
    };

    //Set header title and description
    var setTitle = function (label) {
        var title = '<ui:msgEscJS key="cbuilder.seamless.marketplace.plugins"/>';
        var desc = '<ui:msgEscJS key="cbuilder.seamless.marketplace.plugin.related"/>';
        
        if (label !== "") {
            title = title.replace('{0}', label);
        } else {
            title = '<ui:msgEscJS key="console.setting.plugin.common.label.allplugins"/>';
        }
        
        $('body > div.content > div.page-header > #page-title').text(title);
        $('body > div.content > div.page-header > #page-description').text(desc.replace('{0}', label));
    };

    //get list of plugin categories
    var getPluginCategories = function (category) {
        //retrieve category options
        var pluginCategoriesUrl = "${pageContext.request.contextPath}/web/json/marketplace/plugin/listCategory";
        if (buildeType !== 'all' && buildeType !== '') {
            pluginCategoriesUrl += "?component=" + buildeType;
        }

        $.ajax({
            url: pluginCategoriesUrl,
            method: 'GET',
            dataType: 'json',
            success: function (data) {
                const select = $('#pluginCategory'); 
                // Loop through the options and add category options
                $.each(data.data, function (index, optionValue) {
                    const option = $('<option>').text(optionValue.name).val(optionValue.class_name.replace(/(\r\n|\n|\r)/gm, ""));
                    if (optionValue.class_name.indexOf(category) !== -1) {
                        option.prop('selected', true);
                    }
                    select.append(option);
                });
                
                selectedCategory();
            },
            error: function (data) {
                alert('Failed Getting plugin categories');
            }
        });
    };
    
    // the content is with html, strip all of it
    var removeHtml = function(desc) {
        return $('<div>' + desc + '</div>').find('span').html();
    };
    
    var formatSize = function(size) {
        if (size >= 1000) {
            return Math.round((size/100))/10 + "MB";
        } else {
            return size + "KB";
        }
    };

    //render list of plugins in card based on category
    var getPluginList = function (categories, classes) {
        var url = "${pageContext.request.contextPath}/web/json/marketplace/plugin/list";
        var data = {};
        
        if (categories !== null && categories !== undefined && categories.length > 0) {
            data['categories'] = categories.join(';');
            data['classes'] = classes.join(';');
        }

        $.ajax({
            url: url,
            data: data,
            method: 'GET',
            dataType: 'json',
            success: function (data) {
                var cardArray = data.data;
                if (!(cardArray === null || cardArray === undefined || cardArray.length === 0)) {
                    $.each(cardArray, function (index, card) {
                        const cardElement = $(
                                `<article class="card ` + (card.update?'update ':'') + (card.installed?'installed ':'available ') + `">
                                    <figure class="card-image" style="background-image:url('`+card.img+`')">
                                    </figure>
                                    <div class="card-header">
                                        <div class="meta">
                                            <a href="`+card.url+`" target="_blank"><h3>` + card.name + `</h3> <span class="version"><i class="fas fa-code-branch"></i> `+card.version+ (card.update?(' <span class="currentVersion">('+card.installed+')</span>'):'') + `</span></a>
                                            <p class="desc">` + removeHtml(card.brief) + `</p>
                                        </div>
                                        <button data-id="installplugin_` + card.id + `" onclick="installPlugin('` + card.id + `')" class="icon-button">
                                            ` + (card.update?'<span><ui:msgEscJS key="appCenter.label.updateApp"/></span>':'<span><ui:msgEscJS key="appCenter.label.installApp"/></span>') + `
                                        </button>
                                    </div>
                                    <div class="card-footer">
                                        <div class="card-meta card-meta--views">
                                            <i class="far fa-arrow-alt-circle-down"></i>` + card.count + `
                                        </div>
                                        <div class="card-meta card-meta--views">
                                            <i class="far fa-file"></i> ` + formatSize(card.size) + `
                                        </div>
                                    </div>
                                </article>`
                                );
                        cardElement.appendTo("#plugin-container");
                    });
                }
                
                searchPlugin();
                    
                $('#plugin-container').removeClass('ajaxloading');
            },
            error: function (data) {
                $('#plugin-container').removeClass('ajaxloading');
                alert('Failed loading plugins');
            }
        });
    };
    
    function updateTabs() {
        $('#plugin-container .no_result').remove();
                  
        //available
        if ($("#plugin-container .card.available:not(.search_hidden)").length > 0) {
            $("#plugins-tabs [ref='available']").removeAttr("disabled");
        } else {
            $('#plugin-container').append('<p class="no_result available"><ui:msgEscJS key="appCenter.link.marketplace.noResult"/></p>');
        }
        $("#plugins-tabs [ref='available'] .jgt-badge").text($("#plugin-container .card.available:not(.search_hidden)").length);
        
        //update
        if ($("#plugin-container .card.update:not(.search_hidden)").length > 0) {
            $("#plugins-tabs [ref='update']").removeAttr("disabled");
        } else {
            $('#plugin-container').append('<p class="no_result update"><ui:msgEscJS key="appCenter.link.marketplace.noResult"/></p>');
        }
        $("#plugins-tabs [ref='update'] .jgt-badge").text($("#plugin-container .card.update:visible").length);
        
        //installed
        if ($("#plugin-container .card.installed:not(.search_hidden)").length > 0) {
            $("#plugins-tabs [ref='installed']").removeAttr("disabled");
        } else {
            $('#plugin-container').append('<p class="no_result installed"><ui:msgEscJS key="appCenter.link.marketplace.noResult"/></p>');
        }
        $("#plugins-tabs [ref='installed'] .jgt-badge").text($("#plugin-container .card.installed:not(.search_hidden)").length);
    }
    $(document).ready(function () {
        $('#plugin-container').addClass('ajaxloading');
        getPluginCategories(pluginType);
        
        var timer;
        $("#searchPlugin").off("change").off("keyup").on("change",function () { 
            searchPlugin();
            return false; 
        }).on("keyup", function () {
            if (timer) clearTimeout(timer);
            var $this = $(this);
            timer = setTimeout(function() {
                $this.change(); 
            }, 50);
        });
        
        $(".jgt-tabs a").off("click").on("click", function(){
            $(".jgt-tabs li").removeClass("selected");
            $(this).parent().addClass("selected");
            $("#plugin-container").attr("class", $(this).attr("ref"));
        });
    });
</script>
<commons:popupFooter />